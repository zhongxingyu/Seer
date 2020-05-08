 package pl.edu.mimuw.cloudatlas.agent;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import pl.edu.mimuw.cloudatlas.attributes.ContactValue;
 import pl.edu.mimuw.cloudatlas.attributes.IntegerValue;
 import pl.edu.mimuw.cloudatlas.attributes.StringValue;
 import pl.edu.mimuw.cloudatlas.attributes.TimeValue;
 import pl.edu.mimuw.cloudatlas.attributes.ValueFormatException;
 import pl.edu.mimuw.cloudatlas.islands.ChildEndpoint;
 import pl.edu.mimuw.cloudatlas.islands.ChildIsland;
 import pl.edu.mimuw.cloudatlas.islands.MotherEndpoint;
 import pl.edu.mimuw.cloudatlas.islands.PluggableIsland;
 import pl.edu.mimuw.cloudatlas.islands.TimerEndpoint;
 import pl.edu.mimuw.cloudatlas.islands.TimerFeedbackEndpoint;
 import pl.edu.mimuw.cloudatlas.islands.TimerFeedbackIsland;
 import pl.edu.mimuw.cloudatlas.zones.Attribute;
 import pl.edu.mimuw.cloudatlas.zones.AttributeNames;
 import pl.edu.mimuw.cloudatlas.zones.ZMI;
 import pl.edu.mimuw.cloudatlas.zones.Zone;
 import pl.edu.mimuw.cloudatlas.zones.ZoneNames;
 
 public class StateIsland extends PluggableIsland implements
 		ChildIsland,
 		StateProviderIsland,
 		GossipListenerIsland,
 		TimerFeedbackIsland<Runnable> {
 	
 	public static long DEFAULT_ZONE_REFRESH_INTERVAL = 5000;
 	public static long DEFAULT_MAX_ZONE_AGE = 60000;
 	
 	private static Logger log = LogManager.getFormatterLogger(StateIsland.class);
 	
 	private Random random = new Random();
 	private Zone rootZone;
 	private Zone myZone;
 	private List<ContactValue> fallbackContacts = new ArrayList<ContactValue>();
 	private long zoneRefreshInterval;
 	private long maxZoneAge;
 	
 	private ZoneSiblingsPurger zoneSiblingsPurger;
 	private ZoneAncestorsRefresher zoneAncestorsRefresher;
 	private ContactSelector contactSelector;
 	
 	private TimerEndpoint<Runnable> timerEndpoint;
 	
 	public StateIsland(String zoneName, Properties properties) {
 		ContactValue myContact = new ContactValue(
 				PropertyReader.getHost(properties),
 				PropertyReader.getPort(properties));
 		rootZone = Zone.createRootWithOwner(zoneName, myContact);
 		
 		Zone zone = rootZone;
 		for (String localName : ZoneNames.splitGlobalName(zoneName)) {
 			zone = zone.addChildWithOwner(localName, zoneName, myContact);
 		}
 		
 		myZone = zone;
 		myZone.getZMI().setAttribute("cardinality", new IntegerValue(1));
 		
 		String zoneRefreshIntervalString = properties.getProperty("zoneRefreshInterval");
 		if (zoneRefreshIntervalString == null) {
 			zoneRefreshInterval = DEFAULT_ZONE_REFRESH_INTERVAL;
 		} else {
 			zoneRefreshInterval = Long.parseLong(zoneRefreshIntervalString);
 		}
 		
 		String maxZoneAgeString = properties.getProperty("maxZoneAge");
 		if (maxZoneAgeString == null) {
 			maxZoneAge = DEFAULT_MAX_ZONE_AGE;
 		} else {
 			maxZoneAge = Long.parseLong(maxZoneAgeString);
 		}
 		
 		zoneSiblingsPurger = new ZoneSiblingsPurger(zoneName, maxZoneAge);
 		zoneAncestorsRefresher = new ZoneAncestorsRefresher(zoneName);
 		contactSelector = new ContactSelector(zoneName, properties);
 		
 		initFallbackContacts(properties.getProperty("fallbackContacts"));
 	}
 	
 	private void initFallbackContacts(String contacts) {
 		if (contacts == null) {
 			return;
 		}
 		for (String contact : StringUtils.trim(contacts).split(",\\s*")) {
 			try {
 				fallbackContacts.add(ContactValue.parseContact(contact));
 			} catch (ValueFormatException e) {
 				throw new IllegalArgumentException(e);
 			}
 		}
 	}
 	
 	private void refreshZones() {
		log.info("Refreshing zones");
 		zoneSiblingsPurger.purgeSiblings(rootZone);
 		zoneAncestorsRefresher.refreshAncestors(rootZone);
 		myZone.getZMI().setAttribute("timestamp", TimeValue.now());
 	}
 
 	@Override
 	public ChildEndpoint mountMother(final MotherEndpoint motherEndpoint) {
 		return new ChildEndpoint() {
 
 			@Override
 			public void ignite() {
 				timerEndpoint.schedule(new Runnable() {
 
 					@Override
 					public void run() {
 						refreshZones();
 						timerEndpoint.schedule(this, zoneRefreshInterval);
 					}
 					
 				}, zoneRefreshInterval);
 			}
 
 			@Override
 			public void extinguish() {
 				motherEndpoint.childExtinguished();
 			}
 			
 		};
 	}
 
 	@Override
 	public TimerFeedbackEndpoint<Runnable> mountTimer(TimerEndpoint<Runnable> timerEndpoint) {
 		this.timerEndpoint = timerEndpoint;
 		
 		return new TimerFeedbackEndpoint<Runnable>() {
 
 			@Override
 			public void fire(Runnable object) {
 				object.run();
 			}
 			
 		};
 	}
 	
 	@Override
 	public <RId> StateProviderEndpoint<RId> mountStateReceiver(final StateReceiverEndpoint<RId> receiverEndpoint) {
 		return new StateProviderEndpoint<RId>() {
 
 			@Override
 			public void fetchZoneAttribute(RId requestId, String zoneName,
 					String attributeName) {
 				Zone requestedZone = rootZone.findZone(zoneName);
 				if (requestedZone == null) {
 					receiverEndpoint.zoneNotFound(requestId);
 				} else {
 					Attribute attribute = requestedZone.getZMI().getAttribute(attributeName);
 					if (attribute == null) {
 						receiverEndpoint.attributeNotFound(requestId);
 					} else {	
 						receiverEndpoint.zoneAttributeFetched(requestId, attribute);
 					}
 				}
 			}
 
 			@Override
 			public void updateMyZoneAttributes(RId requestId, List<Attribute> attributes) {
 				for (Attribute attribute : attributes) {
 					myZone.getZMI().setAttribute(attribute.getName(), attribute.getType(), attribute.getValue());
 				}
 				myZone.getZMI().setAttribute("timestamp", TimeValue.now());
 				
 				receiverEndpoint.myZoneAttributesUpdated(requestId);
 			}
 
 			@Override
 			public void fetchZoneAttributeNames(RId requestId, String zoneName) {
 				Zone requestedZone = rootZone.findZone(zoneName);
 				if (requestedZone == null) {
 					receiverEndpoint.zoneNotFound(requestId);
 				} else {
 					receiverEndpoint.zoneAttributeNamesFetched(requestId, requestedZone.getZMI().getAttributeNames());
 				}
 			}
 
 			@Override
 			public void fetchZoneNames(RId requestId) {
 				List<String> zoneNames = new ArrayList<String>();
 				addZoneNames(zoneNames, rootZone);
 				
 				receiverEndpoint.zoneNamesFetched(requestId, zoneNames);
 			}
 			
 			private void addZoneNames(List<String> zoneNames, Zone zone) {
 				zoneNames.add(zone.getGlobalName());
 				for (Zone childZone : zone.getChildren()) {
 					addZoneNames(zoneNames, childZone);
 				}
 			}
 
 			@Override
 			public void fetchMyZoneName(RId requestId) {
 				receiverEndpoint.myZoneNameFetched(requestId, myZone.getGlobalName());
 			}
 
 			@Override
 			public void updateFallbackContacts(
 					Collection<ContactValue> fallbackContacts) {
 				StateIsland.this.fallbackContacts.clear();
 				StateIsland.this.fallbackContacts.addAll(fallbackContacts);
 			}
 
 			@Override
 			public void installQuery(RId requestId, String attributeName,
 					String zoneName, String query) {
 				StringValue queryValue = new StringValue(query);
 				
 				if (zoneName == null) {
 					for (Zone zone = myZone.getParent(); zone != null; zone = zone.getParent()) {
 						zone.getZMI().setAttribute(attributeName, queryValue);
 					}
 					receiverEndpoint.queryInstalled(requestId);
 				} else {
 					Zone zone = rootZone.findZone(zoneName);
 					if (zone == null) {
 						receiverEndpoint.zoneNotFound(requestId);
 					} else {
 						zone.getZMI().setAttribute(attributeName, queryValue);
 						receiverEndpoint.queryInstalled(requestId);
 					}
 				}
 			}
 
 			@Override
 			public void uninstallQuery(RId requestId, String attributeName,
 					String zoneName) {
 				if (zoneName == null) {
 					for (Zone zone = myZone.getParent(); zone != null; zone = zone.getParent()) {
 						zone.getZMI().removeAttribute(attributeName);
 					}
 					receiverEndpoint.queryUninstalled(requestId);
 				} else {
 					Zone zone = rootZone.findZone(zoneName);
 					if (zone == null) {
 						receiverEndpoint.zoneNotFound(requestId);
 					} else {
 						zone.getZMI().removeAttribute(attributeName);
 						receiverEndpoint.queryUninstalled(requestId);
 					}
 				}
 			}
 
 			@Override
 			public void fetchZoneAttributes(RId requestId, String zoneName) {
 				Zone zone = rootZone.findZone(zoneName);
 				if (zone == null) {
 					receiverEndpoint.zoneNotFound(requestId);
 				} else {
 					receiverEndpoint.zoneAttributesFetched(requestId, zone.getZMI().getAttributes());
 				}
 			}
 		};
 	}
 
 	@Override
 	public <RId> GossipListenerEndpoint<RId> mountGossipTransmitter(
 			final GossipTransmitterEndpoint<RId> transmitterEndpoint) {
 		return new GossipListenerEndpoint<RId>() {
 
 			@Override
 			public void getContactForGossiping(RId requestId) {
 				ContactValue contact = contactSelector.nextContact(rootZone);
 				if (contact == null && !fallbackContacts.isEmpty()) {
 					contact = fallbackContacts.get(random.nextInt(fallbackContacts.size()));
 				}
 				transmitterEndpoint.contactForGossipingReceived(requestId, contact);
 			}
 
 			@Override
 			public void offerTimestamps(RId requestId, String peerName) {
 				Map<String, TimeValue> timestamps = new HashMap<String, TimeValue>();
 				Zone zone = rootZone.findZone(ZoneNames.getCommonName(myZone.getGlobalName(), peerName));
 				while (zone != null) {
 					for (Zone childZone : zone.getChildren()) {
 						if (!ZoneNames.isAncestorOrSelf(peerName, childZone.getGlobalName())) {
 							timestamps.put(
 									childZone.getGlobalName(),
 									getTimestamp(childZone.getZMI()));
 						}
 					}
 					zone = zone.getParent();
 				}
 				
 				transmitterEndpoint.timestampsOffered(requestId, timestamps);
 			}
 
 			@Override
 			public void exchangeTimestampsForZMIs(RId requestId,
 					String peerName, Map<String, TimeValue> timestamps) {
 				Map<String, ZMI> zmis = exchangeTimestampsForZMIs(peerName, timestamps);
 				
 				transmitterEndpoint.timestampsForZMIsExchanged(requestId, zmis);
 			}
 			
 			private Map<String, ZMI> exchangeTimestampsForZMIs(
 					String peerName, Map<String, TimeValue> timestamps) {
 				Map<String, ZMI> zmis = new HashMap<String, ZMI>();
 				Zone zone = rootZone.findZone(ZoneNames.getCommonName(myZone.getGlobalName(), peerName));
 				while (zone != null) {
 					for (Zone childZone : zone.getChildren()) {
 						if (
 								!ZoneNames.isAncestorOrSelf(peerName, childZone.getGlobalName()) &&
 								isNewerThan(childZone, timestamps.get(childZone.getGlobalName()))
 						) {
 							zmis.put(childZone.getGlobalName(), removeQueries(childZone.getZMI()));
 						}
 					}
 					zone = zone.getParent();
 				}
 				
 				return zmis;
 			}
 			
 			private ZMI removeQueries(ZMI zmi) {
 				ZMI result = new ZMI();
 				for (Attribute attr : zmi.getAttributes()) {
 					if (AttributeNames.isRegularName(attr.getName())) {
 						result.addAttribute(attr.getName(), attr.getType(), attr.getValue());
 					}
 				}
 				return result;
 			}
 			
 			private boolean isNewerThan(Zone zone, TimeValue timestamp) {
 				if (timestamp == null) {
 					return true;
 				} else {
 					TimeValue zoneTimestamp = getTimestamp(zone.getZMI());
 					return zoneTimestamp.getTimestamp() > timestamp.getTimestamp();
 				}
 			}
 			
 			private TimeValue getTimestamp(ZMI zmi) {
 				return (TimeValue) zmi.getAttributeValue("timestamp");
 			}
 			
 			private Map<String, TimeValue> getTimestamps(Map<String, ZMI> zmis) {
 				Map<String, TimeValue> timestamps = new HashMap<String, TimeValue>();
 				for (Map.Entry<String, ZMI> entry : zmis.entrySet()) {
 					timestamps.put(entry.getKey(), getTimestamp(entry.getValue()));
 				}
 				return timestamps;
 			}
 
 			@Override
 			public void exchangeZMIs(RId requestId, String peerName, Map<String, ZMI> zmis) {
 				Map<String, ZMI> returnedZmis = exchangeTimestampsForZMIs(peerName, getTimestamps(zmis));
 				acceptZMIs(zmis);
 				
 				transmitterEndpoint.zmisExchanged(requestId, returnedZmis);
 			}
 
 			@Override
 			public void acceptZMIs(Map<String, ZMI> zmis) {
 				for (Map.Entry<String, ZMI> entry : zmis.entrySet()) {
 					if (
 							ZoneNames.isAncestorOrSelf(myZone.getGlobalName(), entry.getKey()) ||
 							!ZoneNames.isAncestor(myZone.getGlobalName(), ZoneNames.getParentName(entry.getKey()))
 					) {
 						log.warn("Uninteresting zone name: %s", entry.getKey());
 						continue;
 					}
 					
 					Zone zone = rootZone.findZone(entry.getKey());
 					if (zone == null) {
 						Zone parentZone = rootZone.findZone(ZoneNames.getParentName(entry.getKey()));
 						assert parentZone != null;
 
 						parentZone.addChild(ZoneNames.getLocalName(entry.getKey()), entry.getValue());
 					} else if (!isNewerThan(zone, getTimestamp(entry.getValue()))) {
 						zone.setZMI(entry.getValue());
 					}
 				}
 			}
 		};
 	}
 }
