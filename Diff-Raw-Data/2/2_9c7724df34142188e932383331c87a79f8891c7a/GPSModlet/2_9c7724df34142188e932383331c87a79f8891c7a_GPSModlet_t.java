 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.module.gps;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 import java.util.Timer;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.util.measurement.Measurement;
 import org.osgi.util.measurement.Unit;
 import org.osgi.util.position.Position;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.common.FCNTL_H;
 import com.buglabs.bug.jni.gps.GPS;
 import com.buglabs.bug.jni.gps.GPSControl;
 import com.buglabs.bug.module.gps.pub.IGPSModuleControl;
 import com.buglabs.bug.module.gps.pub.INMEARawFeed;
 import com.buglabs.bug.module.gps.pub.INMEASentenceProvider;
 import com.buglabs.bug.module.gps.pub.INMEASentenceSubscriber;
 import com.buglabs.bug.module.gps.pub.IPositionProvider;
 import com.buglabs.bug.module.gps.pub.IPositionSubscriber;
 import com.buglabs.bug.module.gps.pub.LatLon;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.nmea.DegreesMinutesSeconds;
 import com.buglabs.nmea2.RMC;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.XmlNode;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 /**
  * The Modlet exports the hardware-level services to the OSGi runtime.
  * 
  * @author kgilmer
  * 
  */
 public class GPSModlet implements IModlet, IGPSModuleControl, IModuleControl, PublicWSProvider2, IPositionProvider, IModuleLEDController {
 
 	private BundleContext context;
 
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
 	private NMEASentenceProvider nmeaProvider;
 	private GPSControl gpscontrol;
 
 	private ServiceRegistration gpsControlRef;
 
 	private Timer timer;
 
 	private LogService log;
 
 	private ServiceRegistration ledRef;
 
 	private String serviceName = "Location";
 
 	private boolean suspended;
 
 	private final BMIModuleProperties properties;
 
 	private InputStream gpsIs;
 
 	/**
 	 * @param context
 	 * @param slotId
 	 * @param moduleId
 	 * @param moduleName
 	 */
 	public GPSModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.properties = null;
 		this.log = LogServiceUtil.getLogService(context);
 	}
 
 	public GPSModlet(BundleContext context, int slotId, String moduleId, String moduleName, BMIModuleProperties properties) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.properties = properties;
 		this.log = LogServiceUtil.getLogService(context);	
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.module.pub.IModlet#start()
 	 */
 	public void start() throws Exception {
 		boolean retry = false;
 		int count = 0;
 		do {
 			log.log(LogService.LOG_INFO, "GPSModlet setting active (external) antenna");
 			try {
 				setActiveAntenna();
 				retry = false;
 			} catch (IOException e) {
 				log.log(LogService.LOG_ERROR, "Failed to set GPS antenna to active (external) antenna", e);
 				retry = true;
 				Thread.sleep(200);
 				count++;
 			}
 		} while (retry && count < 10);
 
 		//gpsd.start();
 		nmeaProvider.start();
 
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended": "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 		ledRef = context.registerService(IModuleLEDController.class.getName(), this, createBasicServiceProperties());
 		gpsControlRef = context.registerService(IGPSModuleControl.class.getName(), this, createBasicServiceProperties());
 		nmeaRef = context.registerService(INMEARawFeed.class.getName(), new NMEARawFeed(gpsIs), createBasicServiceProperties());
 		nmeaProviderRef = context.registerService(INMEASentenceProvider.class.getName(), nmeaProvider, createBasicServiceProperties());
 		wsTracker = PublicWSAdminTracker.createTracker(context, this);
 
 		timer = new Timer();
 		timer.schedule(new GPSFIXLEDStatusTask(this, log), 500, 5000);
 
 		positionRef = context.registerService(IPositionProvider.class.getName(), this, createRemotableProperties(null));
 		context.addServiceListener(nmeaProvider, "(|(" + Constants.OBJECTCLASS + "=" + INMEASentenceSubscriber.class.getName() + ") (" + Constants.OBJECTCLASS + "="
 				+ IPositionSubscriber.class.getName() + "))");
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 
 		if (properties != null) {
 			p.put("ModuleDescription", properties.getDescription());
 			p.put("ModuleSN", properties.getSerial_num());
 			p.put("ModuleVendorID", "" + properties.getVendor());
 			p.put("ModuleRevision", "" + properties.getRevision());
 		}
 		
 		try {
 			p.put("gps.antenna.external", "" + isAntennaExternal());
 		} catch (IOException e) {
 			log.log(LogService.LOG_ERROR, "Unable to access GPS antenna state.", e);
 		}
 		return p;
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
 	private void updateIModuleControlProperties(){
 		if (moduleRef!=null){
 			Properties modProperties = createBasicServiceProperties();
 			modProperties.put("Power State", suspended ? "Suspended": "Active");
 			moduleRef.setProperties(modProperties);
 		}
 	}
 
 	private boolean isAntennaExternal() throws IOException {
 		return (getStatus() & 0xC0) == 0x40;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.module.pub.IModlet#stop()
 	 */
 	public void stop() throws Exception {
 		timer.cancel();
 
 		if (wsTracker != null) {
 			wsTracker.close();
 		}
 
 		context.removeServiceListener(nmeaProvider);
 		moduleRef.unregister();
 		gpsControlRef.unregister();
 		nmeaRef.unregister();
 		ledRef.unregister();
 		positionRef.unregister();
 		nmeaProviderRef.unregister();
 		nmeaProvider.interrupt();
 		gpsIs.close();
 		gpscontrol.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.module.gps.pub.IPositionProvider#getPosition()
 	 */
 	public Position getPosition() {
 		RMC rmc = nmeaProvider.getLastRMC();
 
 		if (rmc != null) {
 			try {
 				Position pos = new Position(new Measurement(rmc.getLatitudeAsDMS().toDecimalDegrees() * Math.PI / 180.0, Unit.rad), new Measurement(rmc.getLongitudeAsDMS()
 						.toDecimalDegrees()
 						* Math.PI / 180.0, Unit.rad), new Measurement(0.0d, Unit.m), null, null);
 				return pos;
 			} catch (NumberFormatException e) {
 				log.log(LogService.LOG_ERROR, "Unable to parse position.", e);
 				return null;
 			}
 		} else {
 			return null;
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.module.IModuleControl#getModuleProperties()
 	 */
 	public List getModuleProperties() {
 		List mprops = new ArrayList();
 
 		mprops.add(new ModuleProperty("Slot", "" + slotId));
 
 		try {
 			int status = getStatus();
 			status &= 0xFF;
 
 			String status_str = Integer.toHexString(status);
 			mprops.add(new ModuleProperty(PROPERTY_IOX, "0x" + status_str, "Integer", false));
 
 			mprops.add(new ModuleProperty(PROPERTY_GPS_FIX, Boolean.toString((status &= 0x1) == 0)));
 
 			String antenna = PROPERTY_ANTENNA_ACTIVE;
 
 			if ((status & 0xC0) == IGPSModuleControl.STATUS_PASSIVE_ANTENNA) {
 				antenna = PROPERTY_ANTENNA_PASSIVE;
 			}
 			mprops.add(new ModuleProperty(PROPERTY_ANTENNA, antenna, "String", false));
 			mprops.add(new ModuleProperty("Power State", suspended ? "Suspended" : "Active", "String", true));
 		} catch (IOException e) {
 			log.log(LogService.LOG_ERROR, "Exception occured while getting module properties.", e);
 		}
 		
 		if (properties != null) {
 			mprops.add(new ModuleProperty("Module Description", properties.getDescription()));
 			mprops.add(new ModuleProperty("Module SN", properties.getSerial_num()));
 			mprops.add(new ModuleProperty("Module Vendor ID", "" + properties.getVendor()));
 			mprops.add(new ModuleProperty("Module Revision", "" + properties.getRevision()));
 		}
 
 		return mprops;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 
 		if (property.getName().equals("State")) {
 			return true;
 		} else if (property.getName().equals(PROPERTY_ANTENNA)) {
 			if (property.getValue().toString().equals(PROPERTY_ANTENNA_PASSIVE)) {
 				gpscontrol.ioctl_BMI_GPS_PASSIVE_ANT();
 				System.out.println("Passive");
 			} else {
 				gpscontrol.ioctl_BMI_GPS_ACTIVE_ANT();
 				System.out.println("Active");
 			}
 			return true;
 		}
 		if (property.getName().equals("Power State")) {
 			if (((String) property.getValue()).equals("Suspend")) {
 				try {
 					suspend();					
 				} catch (IOException e) {
 					LogServiceUtil.logBundleException(log, "Error while changing suspend state.", e);
 				}
 			} else if (((String) property.getValue()).equals("Resume")) {
 
 				try {
 					resume();
 				} catch (IOException e) {
 					LogServiceUtil.logBundleException(log, "Error while changing suspend state.", e);
 				}
 			}
 
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
 
 	public int resume() throws IOException {
 		int result = -1;
 
 		result = gpscontrol.ioctl_BMI_GPS_RESUME();
 		suspended = false;
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_RESUME failed");
 		}
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 	public int suspend() throws IOException {
 		int result = -1;
 
 		result = gpscontrol.ioctl_BMI_GPS_SUSPEND();
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_SUSPEND failed");
 		}
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 	public PublicWSDefinition discover(int operation) {
 		if (operation == PublicWSProvider2.GET) {
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
 		if (operation == PublicWSProvider2.GET) {
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
 
 				RMC rmc = nmeaProvider.getLastRMC();
 
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
 			log.log(LogService.LOG_ERROR, "Xml error", e);
 		}
 		return root.toString();
 	}
 
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	public String getDescription() {
 		return "Returns location as provided by GPS module.";
 	}
 
 	public void setup() throws Exception {
		String devnode_gps = "/dev/ttyBMI" + Integer.toString(slotId);
 		String devnode_gpscontrol = "/dev/bmi_gps_control_m" + Integer.toString(slotId + 1);
 
 		//Creation and initialization of this device is necessary to access the control device, below.
 		GPS gps = new GPS();
 		CharDeviceUtils.openDeviceWithRetry(gps, devnode_gps, FCNTL_H.O_RDWR | FCNTL_H.O_NONBLOCK, 2);
 
 		int result = gps.init();
 		if (result < 0) {
 			throw new RuntimeException("Unable to initialize gps device: " + devnode_gpscontrol);
 		}
 
 		gpscontrol = new GPSControl();
 		log.log(LogService.LOG_DEBUG, "Opening GPS control port: " + devnode_gpscontrol);
 		CharDeviceUtils.openDeviceWithRetry(gpscontrol, devnode_gpscontrol, 2);
 		gps.close();
 		log.log(LogService.LOG_DEBUG, "Opening GPS data port: " + devnode_gps);
 		gpsIs = new FileInputStream(devnode_gps);
 		nmeaProvider = new NMEASentenceProvider(gpsIs, context);
 	}
 
 	public LatLon getLatitudeLongitude() {
 		com.buglabs.nmea2.RMC rmc = nmeaProvider.getLastRMC();
 
 		if (rmc != null) {
 			return new LatLon(rmc.getLatitudeAsDMS().toDecimalDegrees(), rmc.getLongitudeAsDMS().toDecimalDegrees());
 		}
 		return null;
 
 	}
 
 	public int LEDGreenOff() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GLEDOFF();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDOFF failed");
 		}
 
 		return result;
 	}
 
 	public int LEDGreenOn() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GLEDON();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOff() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_RLEDOFF();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOn() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_RLEDON();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_RLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int getStatus() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_GETSTAT();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GETSTAT failed");
 		}
 
 		return result;
 	}
 
 	public int setActiveAntenna() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_ACTIVE_ANT();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int setPassiveAntenna() throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			result = gpscontrol.ioctl_BMI_GPS_PASSIVE_ANT();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_GPS_GLEDON failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			if (state) {
 				return LEDGreenOn();
 			} else {
 				return LEDGreenOff();
 			}
 		}
 
 		return result;
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		int result = -1;
 
 		if (gpscontrol != null) {
 			if (gpscontrol != null) {
 				if (state) {
 					return LEDRedOn();
 				} else {
 					return LEDRedOff();
 				}
 			}
 		}
 		return result;
 	}
 
 	public void setPublicName(String name) {
 		serviceName = name;
 	}
 }
