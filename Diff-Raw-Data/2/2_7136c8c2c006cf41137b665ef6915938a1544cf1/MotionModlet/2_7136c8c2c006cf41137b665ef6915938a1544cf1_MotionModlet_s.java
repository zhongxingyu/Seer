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
 package com.buglabs.bug.module.motion;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerControl;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleProvider;
 import com.buglabs.bug.jni.accelerometer.Accelerometer;
 import com.buglabs.bug.jni.common.CharDeviceInputStream;
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.motion.MDACCControl;
 import com.buglabs.bug.jni.motion.Motion;
 import com.buglabs.bug.module.motion.pub.AccelerationWS;
 import com.buglabs.bug.module.motion.pub.IMDACCModuleControl;
 import com.buglabs.bug.module.motion.pub.IMotionRawFeed;
 import com.buglabs.bug.module.motion.pub.IMotionSubject;
 import com.buglabs.bug.module.motion.pub.MotionWS;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 
 public class MotionModlet implements IModlet, IMDACCModuleControl, IModuleControl, IModuleLEDController {
 	// default is 0; make ours +1 since better than BUGview at accelerometer functionality.
 	private final static int OUR_ACCELEROMETER_SERVICES_RANKING = 1;
 	private BundleContext context;
 
 	private boolean deviceOn = true;
 
 	private int slotId;
 
 	private final String moduleId;
 
 	private ServiceRegistration moduleRef;
 	private ServiceRegistration motionSubjectRef;
 	private ServiceRegistration motionRawFeedRef;
 	private ServiceRegistration accSampleProvRef;
 	private ServiceRegistration mdaccRef;
 
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	public static final String MODULE_ID = "0002";
 
 	private final String moduleName;
 
 	private MotionRawFeed motiond;
 
 	private AccelerometerSimpleRawFeed acceld;
 
 	private Motion motionDevice;
 
 	private InputStream motionIs;
 
 	private InputStream accIs;
 
 	private MotionSubject motionSubject;
 
 	private Accelerometer accDevice;
 
 	private ServiceRegistration accRawFeedRef;
 
 	private MDACCControl mdaccControlDevice;
 
 	private ServiceRegistration accControlRef;
 
 	private AccelerometerControl accControl;
 
 	private ServiceRegistration accSampleFeedRef;
 
 	private ServiceRegistration ledRef;
 	private LogService log;
 	private AccelerometerSampleProvider asp;
 	private boolean suspended;
 	private final BMIModuleProperties properties;
 	private ServiceRegistration motionWSReg;
 	private ServiceRegistration accelWSReg;
 
 	public MotionModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.properties = null;
 	}
 
 	public MotionModlet(BundleContext context, int slotId, String moduleId, String moduleName, BMIModuleProperties properties) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 		this.properties = properties;
 	}
 
 	public void start() throws Exception {
 		log = LogServiceUtil.getLogService(context);
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended": "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 
 		motionSubject.start();
 
 		motionSubjectRef = context.registerService(IMotionSubject.class.getName(), motionSubject, createBasicServiceProperties());
 		motionRawFeedRef = context.registerService(IMotionRawFeed.class.getName(), motiond, createBasicServiceProperties());
 		ledRef = context.registerService(IModuleLEDController.class.getName(), this, createBasicServiceProperties());
 
 		MotionWS motionWS = new MotionWS();
 		motionSubject.register(motionWS);
 		motionWSReg = context.registerService(PublicWSProvider.class.getName(), motionWS, null);		
 
 		configureAccelerometer();
 
 		accRawFeedRef = context.registerService(IAccelerometerRawFeed.class.getName(), acceld, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		asp = new AccelerometerSampleProvider(acceld);
 		accSampleProvRef = context.registerService(IAccelerometerSampleProvider.class.getName(), asp, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		accSampleFeedRef = context.registerService(IAccelerometerSampleFeed.class.getName(), acceld, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		accControlRef = context.registerService(IAccelerometerControl.class.getName(), accControl, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		AccelerationWS accWs = new AccelerationWS(asp, log);
 		accelWSReg = context.registerService(PublicWSProvider.class.getName(), accWs, null);
 		mdaccRef = context.registerService(IMDACCModuleControl.class.getName(), this, createBasicServiceProperties());
 	}
 
 	private void configureAccelerometer() {
 		AccelerometerConfiguration config = accDevice.ioctl_BMI_MDACC_ACCELEROMETER_GET_CONFIG();
 		config.setDelay((short) 250);
 		config.setDelayResolution((byte) 5);
 		config.setDelayMode((byte) 1);
 		config.setRun((byte) 1);
 		accDevice.ioctl_BMI_MDACC_ACCELEROMETER_SET_CONFIG(config);
 	}
 
 	public void stop() throws Exception {
 		motionWSReg.unregister();
 		accelWSReg.unregister();
 
 		// TODO: Throw exception at some point if we encounter a failure
 		moduleRef.unregister();
 		motionSubjectRef.unregister();
 		motionRawFeedRef.unregister();
 		accSampleProvRef.unregister();
 		accSampleFeedRef.unregister();
 		mdaccRef.unregister();
 		ledRef.unregister();
 
 		motionSubject.interrupt();
 		motionDevice.ioctl_BMI_MDACC_MOTION_DETECTOR_STOP();
 		motionIs.close();
 		motionDevice.close();
 		mdaccControlDevice.close();
 		accControlRef.unregister();
 		accRawFeedRef.unregister();
 		asp.close();
 		accDevice.ioctl_BMI_MDACC_ACCELEROMETER_STOP();
 		accIs.close();
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 		p.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 		
 		if (properties != null) {
 			if (properties.getDescription() != null) {
 				p.put("ModuleDescription", properties.getDescription());
 			}
 			if (properties.getSerial_num() != null) {
 				p.put("ModuleSN", properties.getSerial_num());
 			}
 
 			p.put("ModuleVendorID", "" + properties.getVendor());
 
 			p.put("ModuleRevision", "" + properties.getRevision());
 
 		}
 		
 		return p;
 	}
 	
 	private Properties createServicePropertiesWithRanking(final int serviceRanking) {
 		final Properties p = createBasicServiceProperties();
 		p.put(Constants.SERVICE_RANKING, new Integer(serviceRanking));
 		return p;
 	}
 	private void updateIModuleControlProperties(){
 		if (moduleRef!=null){
 			Properties modProperties = createBasicServiceProperties();
 			modProperties.put("Power State", suspended ? "Suspended": "Active");
 			moduleRef.setProperties(modProperties);
 		}
 	}
 
 	public List getModuleProperties() {
 		List mprops = new ArrayList();
 
 		mprops.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		mprops.add(new ModuleProperty("Slot", "" + slotId));
 		mprops.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
 		mprops.add(new ModuleProperty("Power State", suspended ? "Suspended": "Active", "String", true));
 		
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
 			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
 			return true;
 		}
 		if (property.getName().equals("Power State")) {
 			if (((String) property.getValue()).equals("Suspend")){
 				try{
 				suspend();
 				}
 			 catch (IOException e) {
 				e.printStackTrace();
 			}
 			}
 			else if (((String) property.getValue()).equals("Resume")){
 				
 				try {
 					resume();
 				} catch (IOException e) {
 					e.printStackTrace();
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
 
 		result = mdaccControlDevice.ioctl_BMI_MDACC_CTL_RESUME();
 		if (result < 0) {
 			throw new IOException("ioctl BMI_MDACC_CTL_RESUME failed");
 		}
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 	}
 	
 
 	public int suspend() throws IOException {
 		int result = -1;
 
 		result = mdaccControlDevice.ioctl_BMI_MDACC_CTL_SUSPEND();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_MDACC_CTL_SUSPEND failed");
 		}
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 
 	public void setup() throws Exception {
		int slot = slotId + 1;
 		String devnode_motion = "/dev/bmi_mdacc_mot_m" + slot;
 		String devnode_acc = "/dev/bmi_mdacc_acc_m" + slot;
 		String devnode_mdacc_control = "/dev/bmi_mdacc_ctl_m" + slot;
 		motionDevice = new Motion();
 		CharDeviceUtils.openDeviceWithRetry(motionDevice, devnode_motion, 2);
 
 		int retval = motionDevice.ioctl_BMI_MDACC_MOTION_DETECTOR_RUN();
 		if (retval < 0) {
 			throw new IOException("IOCTL Failed on: " + devnode_motion);
 		}
 
 		accDevice = new Accelerometer();
 		CharDeviceUtils.openDeviceWithRetry(accDevice, devnode_acc, 2);
 
 		accIs = new CharDeviceInputStream(accDevice);
 		
 		accControl = new AccelerometerControl(accDevice);
 		acceld = new AccelerometerSimpleRawFeed(accIs, accControl);
 
 		mdaccControlDevice = new MDACCControl();
 		CharDeviceUtils.openDeviceWithRetry(mdaccControlDevice, devnode_mdacc_control, 2);
 
 		motionIs = new CharDeviceInputStream(motionDevice);
 		motiond = new MotionRawFeed(motionIs);
 		motionSubject = new MotionSubject(motiond.getInputStream(), this, log);
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if (mdaccControlDevice == null) {
 			return -1;
 		} else if (state) {
 			return mdaccControlDevice.ioctl_BMI_MDACC_CTL_GREEN_LED_ON();
 		} else {
 			return mdaccControlDevice.ioctl_BMI_MDACC_CTL_GREEN_LED_OFF();
 		}
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (mdaccControlDevice == null) {
 			return -1;
 		} else if (state) {
 			return mdaccControlDevice.ioctl_BMI_MDACC_CTL_RED_LED_ON();
 		} else {
 			return mdaccControlDevice.ioctl_BMI_MDACC_CTL_RED_LED_OFF();
 		}
 	}
 }
