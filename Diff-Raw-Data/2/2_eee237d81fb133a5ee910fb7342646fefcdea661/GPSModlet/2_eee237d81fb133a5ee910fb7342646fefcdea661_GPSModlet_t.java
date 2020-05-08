 package com.buglabs.bug.module.gps;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Timer;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.measurement.Measurement;
 import org.osgi.util.measurement.Unit;
 import org.osgi.util.position.Position;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.jni.common.CharDeviceInputStream;
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.gps.GPS;
 import com.buglabs.bug.jni.gps.GPSControl;
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.gps.pub.IGPSModuleControl;
 import com.buglabs.bug.module.gps.pub.INMEARawFeed;
 import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
 import com.buglabs.bug.module.gps.pub.IPositionProvider;
 import com.buglabs.bug.module.gps.pub.LatLon;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.nmea.DegreesMinutesSeconds;
 import com.buglabs.nmea.sentences.RMC;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.StreamMultiplexer;
 import com.buglabs.util.XmlNode;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 public class GPSModlet implements IModlet, IGPSModuleControl, IModuleControl, PublicWSProvider, IPositionProvider {
 
 	private BundleContext context;
 
 	private boolean deviceOn = true;
 
 	private int slotId;
 
 	private final String moduleId;
 
 	private ServiceRegistration moduleRef;
 
 	private ServiceRegistration positionRef;
 
 	private ServiceTracker wsTracker;
 
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 	protected static final String PROPERTY_IOX = "IOX";
 	protected static final String PROPERTY_GPS_FIX = "GPS Fix";
 	protected static final String PROPERTY_ANTENNA = "Antenna";
 	protected static final String PROPERTY_ANTENNA_PASSIVE = "Passive";
 	protected static final String PROPERTY_ANTENNA_ACTIVE = "Active";
 
 	public static final String MODULE_ID = "0001";
 
 	private ServiceRegistration nmeaRef;
 	private ServiceRegistration nmeaProviderRef;
 
 	private final String moduleName;
 
 	private StreamMultiplexer gpsd;
 	private NMEASentenceProvider nmeaProvider;
 
 	private CharDeviceInputStream gpsis;
 
 	private String regionKey;
 
 	private static boolean icon[][] = { { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false },
 		{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 		{ false, true, true, true, true, false, false, false, false, false, false, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, false, false, false, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, false, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, true, false, false, true, false },
 		{ false, true, true, false, false, false, true, false, false, false, true, false, false, false, true, false },
 		{ false, true, false, true, true, true, true, false, true, false, true, false, true, true, true, false },
 		{ false, true, false, true, false, false, true, false, false, false, true, false, false, false, true, false },
 		{ false, true, false, true, true, false, true, false, true, true, true, true, true, false, true, false },
 		{ false, true, true, false, false, false, true, false, true, true, true, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false } };
 
 	private GPSControl gpscontrol;
 
 	private ServiceRegistration gpsControlRef;
 
 	private Timer timer;
 
 	public GPSModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 	}
 
 	public void start() throws Exception {
 
 		gpsd.start();
 		nmeaProvider.start();
 
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, createRemotableProperties(null));
 		gpsControlRef = context.registerService(IGPSModuleControl.class.getName(), this, createRemotableProperties(null));
 		nmeaRef = context.registerService(INMEARawFeed.class.getName(), gpsd, createRemotableProperties(null));
 		nmeaProviderRef = context.registerService(INMEASentenceProvider.class.getName(), nmeaProvider, createRemotableProperties(null));
 		wsTracker = PublicWSAdminTracker.createTracker(context, this);
		regionKey = StatusBarUtils.displayImage(context, icon, this.getModuleName());
 
 		timer = new Timer();
 		timer.schedule(new GPSFIXLEDStatusTask(this), 500, 5000);
 		//TODO: Start position and sentence services when we achieve GPS lock
 
 		positionRef = context.registerService(IPositionProvider.class.getName(), this, createRemotableProperties(null));
 
 	}
 	
 	/**
 	 * @return A dictionary with R-OSGi enable property.
 	 */
 	private Dictionary createRemotableProperties(Dictionary ht) {
 		if (ht == null) {
 			ht = new Hashtable();
 		}
 		
 		ht.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 		
 		return ht;
 	}
 
 	public void stop() throws Exception {
 		timer.cancel();
 
 		StatusBarUtils.releaseRegion(context, regionKey);
 		if (wsTracker != null) {
 			wsTracker.close();
 		}
 
 		moduleRef.unregister();
 		gpsControlRef.unregister();
 		nmeaRef.unregister();
 
 		positionRef.unregister();
 		nmeaProviderRef.unregister();
 		nmeaProvider.interrupt();
 		gpsd.interrupt();
 		gpsis.close();
 		gpscontrol.close();
 	}
 
 	public Position getPosition() {
 		RMC rmc = nmeaProvider.getRMC();
 		if (rmc != null) {
 			try {
 				Position pos = new Position(new Measurement(rmc.getLatitudeAsDMS().toDecimalDegrees() * Math.PI / 180.0, Unit.rad), new Measurement(rmc.getLongitudeAsDMS()
 						.toDecimalDegrees()
 						* Math.PI / 180.0, Unit.rad), new Measurement(0.0d, Unit.m), null, null);
 				return pos;
 			} catch (NumberFormatException e) {
 				// TODO: Log this and/or handle it properly
 				return null;
 			}
 		} else {
 			return null;
 		}
 
 	}
 
 	public List getModuleProperties() {
 		List properties = new ArrayList();
 
 		//properties.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		properties.add(new ModuleProperty("Slot", "" + slotId));
 
 		/*Position pos = getPosition();
 		if (pos != null) {
 			properties.add(new ModuleProperty("Location", getPosition().toString()));
 		}*/
 
 		//properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
 
 		try {
 			int status = getStatus();
 			status &= 0xFF;
 
 			String status_str = Integer.toHexString(status);
 			properties.add(new ModuleProperty(PROPERTY_IOX, "0x" + status_str, "Integer", false));
 
 			properties.add(new ModuleProperty(PROPERTY_GPS_FIX, Boolean.toString((status &= 0x1) == 0)));
 
 			String antenna = PROPERTY_ANTENNA_ACTIVE;
 			if((status &= 0xC0) == IGPSModuleControl.STATUS_PASSIVE_ANTENNA) {
 				antenna = PROPERTY_ANTENNA_PASSIVE;
 			} 
 			properties.add(new ModuleProperty(PROPERTY_ANTENNA, antenna, "String", false));
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return properties;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 
 		if (property.getName().equals("State")) {
 			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
 			return true;
 		} else if(property.getName().equals(PROPERTY_ANTENNA)) {
 			if(property.getValue().toString().equals(PROPERTY_ANTENNA_PASSIVE)) {
 				gpscontrol.ioctl_BMI_GPS_PASSIVE_ANT();
 				System.out.println("Passive");
 			} else {
 				gpscontrol.ioctl_BMI_GPS_ACTIVE_ANT();
 				System.out.println("Active");
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public PublicWSDefinition discover(int operation) {
 		if (operation == PublicWSProvider.GET) {
 			return new PublicWSDefinition() {
 
 				public List getParameters() {
 					return null;
 				}
 
 				public String getReturnType() {
 					return "text/xml";
 				}
 			};
 		}
 
 		return null;
 	}
 
 	public IWSResponse execute(int operation, String input) {
 		if (operation == PublicWSProvider.GET) {
 			return new WSResponse(getPositionXml(), "text/xml");
 		}
 		return null;
 	}
 
 	private String getPositionXml() {
 		Position p = getPosition();
 
 		XmlNode root = new XmlNode("Location");
 		try {
 
 			if (p != null) {
 				if (p.getLatitude() != null) {
 					root.addChildElement(new XmlNode("Latitude", p.getLatitude().toString()));
 				}
 
 				if (p.getLongitude() != null) {
 					root.addChildElement(new XmlNode("Longitude", p.getLongitude().toString()));
 				}
 
 				if (p.getAltitude() != null) {
 					root.addChildElement(new XmlNode("Altitude", p.getAltitude().toString()));
 				}
 
 				RMC rmc = nmeaProvider.getRMC();
 
 				DegreesMinutesSeconds dmsLat = rmc.getLatitudeAsDMS();
 				DegreesMinutesSeconds dmsLon = rmc.getLongitudeAsDMS();
 
 				if (dmsLat != null) {
 					root.addChildElement(new XmlNode("LatitudeDegrees", Double.toString(dmsLat.toDecimalDegrees())));
 				}
 
 				if (dmsLon != null) {
 					root.addChildElement(new XmlNode("LongitudeDegrees", Double.toString(dmsLon.toDecimalDegrees())));
 				}
 			}
 		} catch (SelfReferenceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return root.toString();
 	}
 
 	public String getPublicName() {
 		return "Location";
 	}
 
 	public String getDescription() {
 		return "Returns location as provided by GPS module.";
 	}
 
 	public void setup() throws Exception {
 		String devnode_gps = "/dev/ttymxc" + Integer.toString(slotId);
 		String devnode_gpscontrol = "/dev/bmi_gps_control_m" + Integer.toString(slotId + 1);
 
 		GPS gps = new GPS();
 		CharDeviceUtils.openDeviceWithRetry(gps, devnode_gps, 2);
 
 		int result = gps.init();
 		if(result < 0) {
 			throw new RuntimeException("Unable to initialize gps device: " + devnode_gpscontrol);
 		}
 
 		gpsis = new CharDeviceInputStream(gps);
 		gpsd = new NMEARawFeed(gpsis);
 		nmeaProvider = new NMEASentenceProvider(gpsd.getInputStream());
 
 		gpscontrol = new GPSControl();
 
 		CharDeviceUtils.openDeviceWithRetry(gpscontrol, devnode_gpscontrol, 2);
 	}
 
 	public LatLon getLatitudeLongitude() {
 		RMC rmc = nmeaProvider.getRMC();
 		return new LatLon(Double.parseDouble(rmc.getLatitude()), Double.parseDouble(rmc.getLongitude()));
 	}
 
 	public int LEDGreenOff() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GLEDOFF();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDOFF failed");
 		}
 
 		return result;
 	}
 
 	public int LEDGreenOn() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GLEDON();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOff() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_RLEDOFF();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOn() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_RLEDON();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_RLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int getStatus() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GETSTAT();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GETSTAT failed");
 		}
 
 		return result;
 	}
 
 	public int setActiveAntenna() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_ACTIVE_ANT();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int setPassiveAntenna() throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_PASSIVE_ANT();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			if(state) {
 				return LEDGreenOn();
 			} else {
 				return LEDGreenOff();
 			}
 		}
 
 		return result;
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		int result = -1;
 
 		if(gpscontrol != null) {
 			if(gpscontrol != null) {
 				if(state) {
 					return LEDRedOn();
 				} else {
 					return LEDRedOff();
 				}
 			}
 		}
 		return result;
 	}
 }
