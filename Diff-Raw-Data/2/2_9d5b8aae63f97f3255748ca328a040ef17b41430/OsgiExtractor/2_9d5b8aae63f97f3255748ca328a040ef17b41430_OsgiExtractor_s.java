 package eu.europeana.uim.plugin.solr.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.ctc.wstx.stax.WstxInputFactory;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.definitions.jibx.AgentType;
 import eu.europeana.corelib.definitions.jibx.Concept;
 import eu.europeana.corelib.definitions.jibx.LiteralType;
 import eu.europeana.corelib.definitions.jibx.PlaceType;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
 import eu.europeana.corelib.definitions.jibx.ResourceType;
 import eu.europeana.corelib.definitions.jibx.TimeSpanType;
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 import eu.europeana.corelib.dereference.impl.EdmMappedField;
 import eu.europeana.corelib.dereference.impl.EntityImpl;
 import eu.europeana.corelib.dereference.impl.Extractor;
 
 public class OsgiExtractor extends Extractor {
 
 	// private static OsgiExtractor extractor;
 
 	public OsgiExtractor() {
 		memCache = MemCache.getInstance();
 	}
 
 	// public static OsgiExtractor getInstance(final Datastore datastore) {
 	//
 	// if (extractor == null) {
 	// extractor = new OsgiExtractor();
 	//
 	//
 	// }
 	// return extractor;
 	// }
 
 	public List<EdmMappedField> getEdmLabel(String field) {
 
 		if (vocabulary != null) {
 			if (vocabulary.getElements().get(field) != null) {
 				return vocabulary.getElements().get(field);
 			}
 		}
 		return new ArrayList<EdmMappedField>();
 	}
 
 	private static MemCache memCache;
 	private ControlledVocabularyImpl vocabulary;
 	private Datastore datastore;
 	private final static long UPDATETIMESTAMP = 5184000000l;
 
 	@SuppressWarnings("rawtypes")
 	public Map<String, List> denormalize(String resource,
 			ControlledVocabularyImpl controlledVocabulary, int iterations,
 			boolean iterFromVocabulary) throws SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 
 		if (controlledVocabulary != null) {
 			vocabulary = controlledVocabulary;
 			String suffix = controlledVocabulary.getSuffix() != null ? controlledVocabulary
 					.getSuffix() : "";
 			int iters = iterFromVocabulary ? controlledVocabulary
 					.getIterations() : iterations;
 			if (resource + suffix != null) {
 				String fullUri = resource + suffix;
 				if (!fullUri.contains("/.")) {
 					Map<String, List> retMap = retrieveMapFromCache(fullUri);
 					if (retMap != null) {
 						return retMap;
 					}
 					EntityImpl entity = retrieveValueFromResource(fullUri);
 
 					if (entity != null && entity.getContent().length() > 0) {
 
 						Map<String, List> entityCache = createDereferencingMap(
 								entity.getContent(), iterations);
 						memCache.getEntityCache().put(entity.getUri(),
 								entityCache);
 						return entityCache;
 					}
 				}
 			}
 		}
 
 		return new HashMap<String, List>();
 	}
 
 	private synchronized Map<String, List> retrieveMapFromCache(String fullUri) {
 		return memCache.getEntityCache().containsKey(fullUri) ? memCache
 				.getEntityCache().get(fullUri) : null;
 	}
 
 	private synchronized Map<String, List> createDereferencingMap(
 			String xmlString, int iterations) throws SecurityException,
 			IllegalArgumentException, InstantiationException,
 			IllegalAccessException, NoSuchMethodException,
 			InvocationTargetException {
 		XMLInputFactory inFactory = new WstxInputFactory();
 		Map<String, List> denormalizedValues = new HashMap<String, List>();
 		List<Concept> concepts = new ArrayList<Concept>();
 		List<AgentType> agents = new ArrayList<AgentType>();
 		List<TimeSpanType> timespans = new ArrayList<TimeSpanType>();
 		List<PlaceType> places = new ArrayList<PlaceType>();
 
 		Concept lastConcept = null;
 		AgentType lastAgent = null;
 		TimeSpanType lastTimespan = null;
 		PlaceType lastPlace = null;
 		try {
 			Source source = new StreamSource(new ByteArrayInputStream(
 					xmlString.getBytes()), "UTF-8");
 			XMLEventReader xml = inFactory.createXMLEventReader(source);
 
 			String element = "";
 			while (xml.hasNext()) {
 				XMLEvent evt = xml.nextEvent();
 				if (evt.isStartElement()) {
 					StartElement sElem = evt.asStartElement();
 					element = (sElem.getName().getPrefix() != null ? sElem
 							.getName().getPrefix() + ":" : "")
 							+ sElem.getName().getLocalPart();
 					// If it is mapped then
 					if (isMapped(element)) {
 
 						for (EdmMappedField edmLabel : getEdmLabel(element)) {
 
 							if (sElem.getAttributes().hasNext()) {
 								Attribute attr = (Attribute) sElem
 										.getAttributes().next();
 								String attribute = attr.getName().getPrefix()
 
 								+ ":" + attr.getName().getLocalPart();
 								// Is the attribute mapped?
 								if (isMapped(element + "_" + attribute)) {
 									for (EdmMappedField label : getEdmLabel(element
 											+ "_" + attribute)) {
 										String attrVal = attr.getValue();
 										String elem = null;
 										if (xml.peek().isCharacters()) {
 											elem = xml.nextEvent()
 													.asCharacters().getData();
 										}
 
 										if (StringUtils.equals(label.getLabel()
 												.toString(), "skos_concept")) {
 											if (lastConcept != null) {
 												if (lastConcept.getAbout() != null) {
 													concepts.add(lastConcept);
 													lastConcept = createNewEntity(
 															Concept.class,
 															attrVal);
 												} else {
 													lastConcept
 															.setAbout(attrVal);
 												}
 											} else {
 												lastConcept = createNewEntity(
 														Concept.class, attrVal);
 											}
 
 										} else if (StringUtils.equals(label
 												.getLabel().toString(),
 												"edm_agent")) {
 											if (lastAgent != null) {
 												if (lastAgent.getAbout() != null) {
 													agents.add(lastAgent);
 
 													lastAgent = createNewEntity(
 															AgentType.class,
 															attrVal);
 												} else {
 													lastAgent.setAbout(attrVal);
 												}
 											} else {
 												lastAgent = createNewEntity(
 														AgentType.class,
 														attrVal);
 											}
 
 										} else if (StringUtils.equals(label
 												.getLabel().toString(),
 												"edm_timespan")) {
 											if (lastTimespan != null) {
 												if (lastTimespan.getAbout() != null) {
 													timespans.add(lastTimespan);
 													lastTimespan = createNewEntity(
 															TimeSpanType.class,
 															attrVal);
 												} else {
 													lastTimespan
 															.setAbout(attrVal);
 												}
 
 											} else {
 												lastTimespan = createNewEntity(
 														TimeSpanType.class,
 														attrVal);
 											}
 
 										} else if (StringUtils.equals(label
 												.getLabel().toString(),
 												"edm_place")) {
 											if (lastPlace != null) {
 												if (lastPlace.getAbout() != null) {
 													places.add(lastPlace);
 													lastPlace = createNewEntity(
 															PlaceType.class,
 															attrVal);
 												} else {
 													lastPlace.setAbout(attrVal);
 												}
 											} else {
 												lastPlace = createNewEntity(
 														PlaceType.class,
 														attrVal);
 											}
 
 										} else {
 											if (StringUtils.startsWith(label
 													.getLabel().toString(),
 													"cc")) {
 
 												appendConceptValue(
 														lastConcept == null ? new Concept()
 																: lastConcept,
 														label.getLabel()
 																.toString(),
 														elem,
 														label.getAttribute(),
 														attrVal, iterations);
 											}
 
 											else if (StringUtils
 													.startsWith(label
 															.getLabel()
 															.toString(), "ts")) {
 
 												appendValue(
 														TimeSpanType.class,
 														lastTimespan == null ? new TimeSpanType()
 																: lastTimespan,
 														label.getLabel()
 																.toString(),
 														elem,
 														label.getAttribute(),
 														attrVal, iterations);
 											} else if (StringUtils
 													.startsWith(label
 															.getLabel()
 															.toString(), "ag")) {
 
 												appendValue(
 														AgentType.class,
 														lastAgent == null ? new AgentType()
 																: lastAgent,
 														label.getLabel()
 																.toString(),
 														elem,
 														label.getAttribute(),
 														attrVal, iterations);
 											} else if (StringUtils
 													.startsWith(label
 															.getLabel()
 															.toString(), "pl")) {
 
 												appendValue(
 														PlaceType.class,
 														lastPlace == null ? new PlaceType()
 																: lastPlace,
 														label.getLabel()
 																.toString(),
 														elem,
 														label.getAttribute(),
 														attrVal, iterations);
 											}
 										}
 									}
 								}
 								// Since the attribute is not mapped
 								else {
 									if (xml.peek().isCharacters()) {
 										
 										if (StringUtils.equals(edmLabel
 												.getLabel().toString(),
 												"skos_concept")) {
 											if (lastConcept != null) {
 												if (lastConcept.getAbout() != null) {
 													concepts.add(lastConcept);
 													lastConcept = createNewEntity(
 															Concept.class,
 															xml.getElementText());
 												} else {
 													lastConcept.setAbout(xml
 															.getElementText());
 												}
 
 											} else {
 												lastConcept = createNewEntity(
 														Concept.class,
 														xml.getElementText());
 											}
 
 										} else if (StringUtils.equals(edmLabel
 												.getLabel().toString(),
 												"edm_agent")) {
 											if (lastAgent != null) {
 												if (lastAgent.getAbout() != null) {
 													agents.add(lastAgent);
 													lastAgent = createNewEntity(
 															AgentType.class,
 															xml.getElementText());
 												} else {
 													lastAgent.setAbout(xml
 															.getElementText());
 												}
 
 											} else {
 												lastAgent = createNewEntity(
 														AgentType.class,
 														xml.getElementText());
 											}
 
 										} else if (StringUtils.equals(edmLabel
 												.getLabel().toString(),
 												"edm_timespan")) {
 											if (lastTimespan != null) {
 												if (lastTimespan.getAbout() != null) {
 													timespans.add(lastTimespan);
 													lastTimespan = createNewEntity(
 															TimeSpanType.class,
 															xml.getElementText());
 												} else {
 													lastTimespan.setAbout(xml
 															.getElementText());
 												}
 											} else {
 												lastTimespan = createNewEntity(
 														TimeSpanType.class,
 														xml.getElementText());
 											}
 
 										} else if (StringUtils.equals(edmLabel
 												.getLabel().toString(),
 												"edm_place")) {
 											if (lastPlace != null) {
 												if (lastPlace.getAbout() != null) {
 													places.add(lastPlace);
 													lastPlace = createNewEntity(
 															PlaceType.class,
 															xml.getElementText());
 												} else {
 													lastPlace.setAbout(xml
 															.getElementText());
 												}
 											} else {
 												lastPlace = createNewEntity(
 														PlaceType.class,
 														xml.getElementText());
 											}
 
 										} else {
 											if (StringUtils.startsWith(edmLabel
 													.getLabel().toString(),
 													"cc")) {
 												appendConceptValue(
 														lastConcept == null ? new Concept()
 																: lastConcept,
 														edmLabel.getLabel()
 																.toString(),
 														xml.getElementText(),
 														"", null, iterations);
 											}
 
 											else if (StringUtils.startsWith(
 													edmLabel.getLabel()
 															.toString(), "ts")) {
 												lastTimespan = appendValue(
 														TimeSpanType.class,
 														lastTimespan == null ? new TimeSpanType()
 																: lastTimespan,
 														edmLabel.getLabel()
 																.toString(),
 														xml.getElementText(),
 														"", null, iterations);
 											} else if (StringUtils.startsWith(
 													edmLabel.getLabel()
 															.toString(), "ag")) {
 												lastAgent = appendValue(
 														AgentType.class,
 														lastAgent == null ? new AgentType()
 																: lastAgent,
 														edmLabel.getLabel()
 																.toString(),
 														xml.getElementText(),
 														"", null, iterations);
 											} else if (StringUtils.startsWith(
 													edmLabel.getLabel()
 															.toString(), "pl")) {
 												lastPlace = appendValue(
 														PlaceType.class,
 														lastPlace == null ? new PlaceType()
 																: lastPlace,
 														edmLabel.getLabel()
 																.toString(),
 														xml.getElementText(),
 														"", null, iterations);
 
 											}
 										}
 									}
 								}
 							}
 							// Since it does not have attributes
 							else {
 								XMLEvent evt2 = xml.nextEvent();
 								if (evt2.isCharacters()) {
 									if (StringUtils.equals(edmLabel.getLabel()
 											.toString(), "skos_concept")) {
 										if (lastConcept != null) {
 											concepts.add(lastConcept);
 										}
 										lastConcept = createNewEntity(
 												Concept.class, evt2
 														.asCharacters()
 														.getData());
 
 									} else if (StringUtils
 											.equals(edmLabel.getLabel()
 													.toString(), "edm_agent")) {
 										if (lastAgent != null) {
 											if (lastAgent.getAbout() != null) {
 												agents.add(lastAgent);
 												lastAgent = createNewEntity(
 														AgentType.class, evt2
 																.asCharacters()
 																.getData());
 											} else {
 												lastAgent.setAbout(evt2
 														.asCharacters()
 														.getData());
 											}
 										}
 
 										else {
 											lastAgent = createNewEntity(
 													AgentType.class, evt2
 															.asCharacters()
 															.getData());
 										}
 									} else if (StringUtils.equals(edmLabel
 											.getLabel().toString(),
 											"edm_timespan")) {
 										if (lastTimespan != null) {
 											if (lastTimespan.getAbout() != null) {
 												timespans.add(lastTimespan);
 												lastTimespan = createNewEntity(
 														TimeSpanType.class,
 														evt2.asCharacters()
 																.getData());
 											} else {
 												lastTimespan.setAbout(evt2
 														.asCharacters()
 														.getData());
 											}
 
 										} else {
 											lastTimespan = createNewEntity(
 													TimeSpanType.class, evt2
 															.asCharacters()
 															.getData());
 										}
 
 									} else if (StringUtils
 											.equals(edmLabel.getLabel()
 													.toString(), "edm_place")) {
 										if (lastPlace != null) {
 											if (lastPlace.getAbout() != null) {
 												places.add(lastPlace);
 												lastPlace = createNewEntity(
 														PlaceType.class, evt2
 																.asCharacters()
 																.getData());
 											} else {
 												lastPlace.setAbout(evt2
 														.asCharacters()
 														.getData());
 											}
 										} else {
 											lastPlace = createNewEntity(
 													PlaceType.class, evt2
 															.asCharacters()
 															.getData());
 										}
 
 									} else {
 
 										if (StringUtils.startsWith(edmLabel
 												.getLabel().toString(), "cc")) {
 											appendConceptValue(
 													lastConcept == null ? new Concept()
 															: lastConcept,
 													edmLabel.getLabel()
 															.toString(), evt2
 															.asCharacters()
 															.getData(), "",
 													null, iterations);
 										}
 
 										else if (StringUtils.startsWith(
 												edmLabel.getLabel().toString(),
 												"ts")) {
 											lastTimespan = appendValue(
 													TimeSpanType.class,
 													lastTimespan == null ? new TimeSpanType()
 															: lastTimespan,
 
 													edmLabel.getLabel()
 															.toString(), evt2
 															.asCharacters()
 															.getData(), "",
 													null, iterations);
 										} else if (StringUtils.startsWith(
 												edmLabel.getLabel().toString(),
 												"ag")) {
 											lastAgent = appendValue(
 													AgentType.class,
 													lastAgent == null ? new AgentType()
 															: lastAgent,
 													edmLabel.getLabel()
 															.toString(), evt2
 															.asCharacters()
 															.getData(), "",
 													null, iterations);
 										} else if (StringUtils.startsWith(
 												edmLabel.getLabel().toString(),
 												"pl")) {
 											lastPlace = appendValue(
 													PlaceType.class,
 													lastPlace == null ? new PlaceType()
 															: lastPlace,
 													edmLabel.getLabel()
 															.toString(), evt2
 															.asCharacters()
 															.getData(), "",
 													null, iterations);
 										}
 									}
 								}
 							}
 						}
 					}
 					// The element is not mapped, but does it have any
 					// mapped attributes?
 					else {
 						if (sElem.getAttributes().hasNext()) {
 							Attribute attr = (Attribute) sElem.getAttributes()
 									.next();
 							String attribute = attr.getName().getPrefix()
 
 							+ ":" + attr.getName().getLocalPart();
 							// Is the attribute mapped?
 							xml.next();
 
 							// Is the attribute mapped?
 							if (isMapped(element + "_" + attribute)) {
 								for (EdmMappedField label : getEdmLabel(element
 										+ "_" + attribute)) {
 									if (StringUtils.equals(label.getLabel()
 											.toString(), "skos_concept")) {
 										if (lastConcept != null) {
 											if (lastConcept.getAbout() != null) {
 												concepts.add(lastConcept);
 												lastConcept = createNewEntity(
 														Concept.class,
 														attr.getValue());
 											} else {
 												lastConcept.setAbout(attr
 														.getValue());
 											}
 										} else {
 											lastConcept = createNewEntity(
 													Concept.class,
 													attr.getValue());
 										}
 									}
 
 									else
 
 									if (StringUtils.equals(label.getLabel()
 											.toString(), "edm_agent")) {
 										if (lastAgent != null) {
 											if (lastAgent.getAbout() != null) {
 												agents.add(lastAgent);
 												lastAgent = createNewEntity(
 														AgentType.class,
 														attr.getValue());
 											} else {
 												lastAgent.setAbout(attr
 														.getValue());
 											}
 										} else {
 											lastAgent = createNewEntity(
 													AgentType.class,
 													attr.getValue());
 										}
 
 									} else
 
 									if (StringUtils.equals(label.getLabel()
 											.toString(), "edm_timespan")) {
 										if (lastTimespan != null) {
 											if (lastTimespan.getAbout() != null) {
 												timespans.add(lastTimespan);
 												lastTimespan = createNewEntity(
 														TimeSpanType.class,
 														attr.getValue());
 											} else {
 												lastTimespan.setAbout(attr
 														.getValue());
 											}
 										} else {
 											lastTimespan = createNewEntity(
 													TimeSpanType.class,
 													attr.getValue());
 										}
 
 									} else
 
 									if (StringUtils.equals(label.getLabel()
 											.toString(), "edm_place")) {
 										if (lastPlace != null) {
 											if (lastPlace.getAbout() != null) {
 												places.add(lastPlace);
 												lastPlace = createNewEntity(
 														PlaceType.class,
 														attr.getValue());
 											} else {
 												lastPlace.setAbout(attr
 														.getValue());
 											}
 
 										} else {
 											lastPlace = createNewEntity(
 													PlaceType.class,
 													attr.getValue());
 										}
 
 									} else {
 										if (StringUtils.startsWith(label
 												.getLabel().toString(), "cc")) {
 											String elem = null;
 											if (xml.peek().isCharacters()) {
 												elem = xml.nextEvent()
 														.asCharacters()
 														.getData();
 											}
 											String attrVal = attr.getValue();
 
 											appendConceptValue(
 													lastConcept == null ? new Concept()
 															: lastConcept,
 													null, elem,
 													label.getAttribute(),
 													attrVal, iterations);
 										}
 
 										else if (StringUtils.startsWith(label
 												.getLabel().toString(), "ts")) {
 											String elem = null;
 											if (xml.peek().isCharacters()) {
 												elem = xml.nextEvent()
 														.asCharacters()
 														.getData();
 											}
 											String attrVal = attr.getValue();
 
 											lastTimespan = appendValue(
 													TimeSpanType.class,
 													lastTimespan == null ? new TimeSpanType()
 															: lastTimespan,
 													null, elem,
 													label.getAttribute(),
 													attrVal, iterations);
 										} else if (StringUtils.startsWith(label
 												.getLabel().toString(), "ag")) {
 											String elem = null;
 											if (xml.peek().isCharacters()) {
 												elem = xml.nextEvent()
 														.asCharacters()
 														.getData();
 											}
 											String attrVal = attr.getValue();
 
 											lastAgent = appendValue(
 													AgentType.class,
 													lastAgent == null ? new AgentType()
 															: lastAgent, null,
 													elem, label.getAttribute(),
 													attrVal, iterations);
 										} else if (StringUtils.startsWith(
 												label.toString(), "pl")) {
 											String elem = null;
 											if (xml.peek().isCharacters()) {
 												elem = xml.nextEvent()
 														.asCharacters()
 														.getData();
 											}
 											String attrVal = attr.getValue();
 
 											lastPlace = appendValue(
 													PlaceType.class,
 													lastPlace == null ? new PlaceType()
 															: lastPlace, null,
 													elem, label.getAttribute(),
 													attrVal, iterations);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 
 			}
 			if (lastConcept != null)
 				concepts.add(lastConcept);
 			if (lastAgent != null)
 				agents.add(lastAgent);
 			if (lastTimespan != null)
 				timespans.add(lastTimespan);
 			if (lastPlace != null)
 				places.add(lastPlace);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 
 		denormalizedValues.put("concepts", concepts);
 		denormalizedValues.put("agents", agents);
 		denormalizedValues.put("timespans", timespans);
 		denormalizedValues.put("places", places);
 
 		return denormalizedValues;
 	}
 
 	public void setDatastore(Datastore datastore) {
 		this.datastore = datastore;
 	}
 
 	public boolean isMapped(String field) {
 
 		if (vocabulary != null) {
 			for (Entry<String, List<EdmMappedField>> entry : vocabulary
 					.getElements().entrySet()) {
 				if (StringUtils.contains(entry.getKey(), field)
 						&& entry.getValue() != null) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private synchronized EntityImpl retrieveValueFromResource(String resource) {
 
 		EntityImpl entity = datastore.find(EntityImpl.class)
 				.filter("uri", resource).get();
 
 		if (entity == null) {
 			String val = retrieveValue(resource);
 			if (val.length() > 0) {
 				EntityImpl newEntity = new EntityImpl();
 				newEntity.setUri(resource);
 				newEntity.setTimestamp(new Date().getTime());
 				newEntity.setContent(val);
 				datastore.save(newEntity);
 				System.out.println("New Entity :" + newEntity.getUri());
 				return newEntity;
 			}
 			return null;
 		} else {
 			if (new Date().getTime() - entity.getTimestamp() < UPDATETIMESTAMP) {
 				return entity;
 			} else {
 				String val = retrieveValue(resource);
 				Query<EntityImpl> updateQuery = datastore
 						.createQuery(EntityImpl.class).field("uri")
 						.equal(resource);
 				UpdateOperations<EntityImpl> ops = datastore
 						.createUpdateOperations(EntityImpl.class).set(
 								"content", val);
 				datastore.update(updateQuery, ops);
 				entity.setContent(val);
 				return entity;
 			}
 		}
 	}
 
 	private String retrieveValue(String resource) {
 		URLConnection urlConnection;
 		if (resource != null && vocabulary != null) {
 			try {
 
 				if (StringUtils.isNotBlank(vocabulary.getReplaceUrl())) {
 					resource = StringUtils.replace(resource,
 							vocabulary.getURI(), vocabulary.getReplaceUrl());
 				}
 				urlConnection = new URL(resource).openConnection();
 
 				InputStream inputStream = urlConnection.getInputStream();
 				StringWriter writer = new StringWriter();
 				IOUtils.copy(inputStream, writer, "UTF-8");
 				return writer.toString();
 
 			} catch (MalformedURLException e) {
 				return "";
 			} catch (IOException e) {
 				return "";
 			}
 		}
 		return "";
 	}
 
 	private <T> T createNewEntity(Class<T> clazz, String val)
 			throws InstantiationException, IllegalAccessException,
 			SecurityException, NoSuchMethodException, IllegalArgumentException,
 			InvocationTargetException {
 		T obj = clazz.newInstance();
 		Class<?>[] cls = new Class<?>[1];
 		cls[0] = (String.class);
 		Method method = clazz.getMethod("setAbout", cls);
 		method.invoke(obj, val);
 		return obj;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T appendValue(Class<T> clazz, T obj, String edmLabel,
 			String val, String edmAttr, String valAttr, int iterations)
 			throws SecurityException, NoSuchMethodException,
 			IllegalArgumentException, IllegalAccessException,
 			InvocationTargetException {
 
 		RdfMethod RDF = null;
 		for (RdfMethod rdfMethod : RdfMethod.values()) {
 			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
 				RDF = rdfMethod;
 			}
 		}
 
 		//
 		if (RDF.getMethodName().endsWith("List")) {
 
 			Method mthd = clazz.getMethod(RDF.getMethodName());
 
 			@SuppressWarnings("rawtypes")
 			List lst = mthd.invoke(obj) != null ? (ArrayList) mthd.invoke(obj)
 					: new ArrayList();
 			if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceType.class)) {
 
 				ResourceType rs = new ResourceType();
 				rs.setResource(val != null ? val : valAttr);
 				// if (isURI(rs.getResource())) {
 				// denormalize(rs.getResource(), iterations - 1);
 				// }
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceOrLiteralType.class)) {
 				ResourceOrLiteralType rs = new ResourceOrLiteralType();
 				if (isURI(val)) {
 					rs.setResource(val);
 					// denormalize(val, iterations - 1);
 				} else {
 					rs.setString(val);
 				}
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						Lang lang = new Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 
 				}
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 			} else if (RDF.getClazz().getSuperclass()
 					.isAssignableFrom(LiteralType.class)) {
 				LiteralType rs = new LiteralType();
 				rs.setString(val);
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						LiteralType.Lang lang = new LiteralType.Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 				}
 				lst.add(RDF.returnObject(RDF.getClazz(), rs));
 			}
 
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = List.class;
 			Method method = obj.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(obj, lst);
 		} else {
 			if (RDF.getClazz().isAssignableFrom(ResourceType.class)) {
 				ResourceType rs = new ResourceType();
 				rs.setResource(val != null ? val : valAttr);
 				// if (isURI(rs.getResource())) {
 				// denormalize(rs.getResource(), iterations - 1);
 				// }
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = RDF.getClazz();
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 			} else if (RDF.getClazz().isAssignableFrom(LiteralType.class)) {
 				LiteralType rs = new LiteralType();
 				rs.setString(val);
 				// if (isURI(val)) {
 				// denormalize(val, iterations - 1);
 				// }
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						LiteralType.Lang lang = new LiteralType.Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 
 					}
 				}
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = RDF.getClazz();
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 
 			} else if (RDF.getClazz().isAssignableFrom(
 					ResourceOrLiteralType.class)) {
 				ResourceOrLiteralType rs = new ResourceOrLiteralType();
 				if (isURI(val)) {
 					rs.setResource(val);
 					// denormalize(val, iterations - 1);
 				} else {
 					rs.setString(val);
 				}
 				if (edmAttr != null) {
 
 					if (StringUtils.equals(edmAttr, "xml:lang")) {
 						Lang lang = new Lang();
 						lang.setLang(valAttr);
 						rs.setLang(lang);
 					}
 				}
 				Class<?>[] cls = new Class<?>[1];
 				cls[0] = clazz;
 				Method method = obj.getClass().getMethod(
 						StringUtils.replace(RDF.getMethodName(), "get", "set"),
 						cls);
 				method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
 			}
 		}
 
 		return obj;
 	}
 
 	private Concept appendConceptValue(Concept concept, String edmLabel,
 			String val, String edmAttr, String valAttr, int iterations)
 			throws SecurityException, NoSuchMethodException,
 			IllegalArgumentException, IllegalAccessException,
 			InvocationTargetException {
 		RdfMethod RDF = null;
 		for (RdfMethod rdfMethod : RdfMethod.values()) {
 			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
 				RDF = rdfMethod;
 				break;
 			}
 		}
 		List<Concept.Choice> lst = concept.getChoiceList() != null ? concept
 				.getChoiceList() : new ArrayList<Concept.Choice>();
 		if (RDF.getClazz().getSuperclass().isAssignableFrom(ResourceType.class)) {
 
 			ResourceType obj = new ResourceType();
 			obj.setResource(val != null ? val : valAttr);
 			if (isURI(obj.getResource())) {
 				denormalize(obj.getResource(), iterations - 1);
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 
 		} else if (RDF.getClazz().getSuperclass()
 				.isAssignableFrom(ResourceOrLiteralType.class)) {
 
 			ResourceOrLiteralType obj = new ResourceOrLiteralType();
 
 			if (isURI(val)) {
 				obj.setResource(val);
 				denormalize(val, iterations - 1);
 			} else {
 				obj.setString(val);
 			}
 			if (edmAttr != null) {
 
 				if (StringUtils.equals(edmAttr, "xml:lang")) {
 					Lang lang = new Lang();
 					lang.setLang(valAttr);
 					obj.setLang(lang);
 				}
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 
 		} else if (RDF.getClazz().getSuperclass()
 				.isAssignableFrom(LiteralType.class)) {
 			LiteralType obj = new LiteralType();
 			obj.setString(val);
 			if (isURI(val)) {
 				denormalize(val, iterations - 1);
 			}
 			if (edmAttr != null) {
 
 				if (StringUtils.equals(edmAttr, "xml:lang")) {
 					LiteralType.Lang lang = new LiteralType.Lang();
 					lang.setLang(valAttr);
 					obj.setLang(lang);
 				}
 			}
 			Class<?>[] cls = new Class<?>[1];
 			cls[0] = RDF.getClazz();
 			Concept.Choice choice = new Concept.Choice();
 			Method method = choice.getClass()
 					.getMethod(
 							StringUtils.replace(RDF.getMethodName(), "get",
 									"set"), cls);
 			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
 			lst.add(choice);
 		}
 		concept.setChoiceList(lst);
 		return concept;
 	}
 
 	private Map<String, List> denormalize(String val, int iterations) {
 		try {
 			if (iterations > 0) {
 				ControlledVocabularyImpl controlledVocabulary = getControlledVocabulary(
 						datastore, "URI", val);
 				return denormalize(val, controlledVocabulary, iterations, false);
 			}
 			return new HashMap<String, List>();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static boolean isURI(String uri) {
 
 		try {
 			new URL(uri);
 			return true;
 		} catch (MalformedURLException e) {
 			return false;
 		}
 
 	}
 
 	public void setMappedField(String fieldToMap, EdmLabel europeanaField,
 			String attribute) {
 		HashMap<String, List<EdmMappedField>> elements = vocabulary
 				.getElements() != null ? (HashMap<String, List<EdmMappedField>>) vocabulary
 				.getElements() : new HashMap<String, List<EdmMappedField>>();
 		List<EdmMappedField> element = elements.get(fieldToMap);
 		if (!element.contains(europeanaField)) {
 			EdmMappedField edmField = new EdmMappedField();
			edmField.setLabel(europeanaField);
 			edmField.setAttribute(StringUtils.isNotBlank(attribute) ? attribute
 					: "");
 
 			element.add(edmField);
 			elements.put(fieldToMap, element);
 			vocabulary.setElements(elements);
 		}
 
 	}
 
 	public String getMappedField(EdmMappedField europeanaField) {
 		for (String key : vocabulary.getElements().keySet()) {
 			if (europeanaField.equals(vocabulary.getElements().get(key))) {
 				return key;
 			}
 		}
 		return null;
 	}
 
 	public ControlledVocabularyImpl getControlledVocabulary(
 			Datastore datastore, String field, String filter)
 			throws UnknownHostException, MongoException {
 		String[] splitName = filter.split("/");
 		if (splitName.length > 3) {
 			String vocabularyName = splitName[0] + "/" + splitName[1] + "/"
 					+ splitName[2] + "/";
 			List<ControlledVocabularyImpl> vocabularies = datastore
 					.find(ControlledVocabularyImpl.class)
 					.filter(field, vocabularyName).asList();
 			vocabularies.addAll(datastore.find(ControlledVocabularyImpl.class)
 					.filter("replaceUrl", vocabularyName).asList());
 			for (ControlledVocabularyImpl vocabulary : vocabularies) {
 				for (String rule : vocabulary.getRules()) {
 					if (StringUtils.equals(rule, "*")
 							|| StringUtils.contains(filter, rule)) {
 						return vocabulary;
 					}
 				}
 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Retrieve all the stored controlled vocabularies
 	 * 
 	 * @return A list with all the stored controlled vocabularies
 	 */
 
 	public List<ControlledVocabularyImpl> getControlledVocabularies(
 			final Datastore datastore) {
 
 		return datastore.find(ControlledVocabularyImpl.class) != null ? datastore
 				.find(ControlledVocabularyImpl.class).asList() : null;
 	}
 
 }
