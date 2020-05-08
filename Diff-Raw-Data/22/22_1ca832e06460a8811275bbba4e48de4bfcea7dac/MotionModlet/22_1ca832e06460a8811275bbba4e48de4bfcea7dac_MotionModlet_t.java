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
 import org.osgi.util.tracker.ServiceTracker;
 
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
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.motion.pub.AccelerationWS;
 import com.buglabs.bug.module.motion.pub.IMDACCModuleControl;
 import com.buglabs.bug.module.motion.pub.IMotionRawFeed;
 import com.buglabs.bug.module.motion.pub.IMotionSubject;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.util.IStreamMultiplexerListener;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.StreamMultiplexerEvent;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 public class MotionModlet implements IModlet, IMDACCModuleControl, IModuleControl, IStreamMultiplexerListener, IModuleLEDController {
	// default is 0; make ours +1 since we're btter than BUGview at accelerometer so nyah
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
 
 	private ServiceTracker wsMotionTracker, wsAccTracker;
 
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	public static final String MODULE_ID = "0002";
 
 	private final String moduleName;
 
 	private MotionRawFeed motiond;
 
 	private AccelerometerRawFeed acceld;
 
 	private Motion motionDevice;
 
 	private InputStream motionIs;
 
 	private InputStream accIs;
 
 	private MotionSubject motionSubject;
 
 	private Accelerometer accDevice;
 
 	private ServiceRegistration accRawFeedRef;
 
 	private String regionKey;
 
 	private MDACCControl mdaccControlDevice;
 
 	private ServiceRegistration accControlRef;
 
 	private static boolean icon[][] = { { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, false, false, false, true, true, true, false, false, false, false, false, false, true, false },
 			{ false, true, false, false, false, true, true, true, false, false, false, false, false, false, true, false },
 			{ false, true, false, false, false, true, true, true, false, false, false, false, false, false, true, false },
 			{ false, true, false, false, false, false, true, true, true, false, false, false, false, false, true, false },
 			{ false, true, false, false, false, false, true, true, true, true, true, false, false, false, true, false },
 			{ false, true, false, false, false, false, true, true, true, true, true, true, false, false, true, false },
 			{ false, true, false, false, false, false, true, true, true, true, false, true, true, false, true, false },
 			{ false, true, false, true, true, false, true, true, true, true, false, false, true, false, true, false },
 			{ false, true, false, false, true, true, true, true, true, true, false, false, true, true, true, false },
 			{ false, true, false, false, false, false, false, true, true, true, true, false, false, false, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false } };
 
 	private AccelerometerControl accControl;
 
 	private ServiceRegistration accSampleFeedRef;
 
 	private ServiceRegistration ledRef;
 
 	public MotionModlet(BundleContext context, int slotId, String moduleId, String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 	}
 
 	public void start() throws Exception {
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, null);
 		motiond.start();
 		motionSubject.start();
 
 		motionSubjectRef = context.registerService(IMotionSubject.class.getName(), motionSubject, createBasicServiceProperties());
 		motionRawFeedRef = context.registerService(IMotionRawFeed.class.getName(), motiond, createBasicServiceProperties());
 		ledRef = context.registerService(IModuleLEDController.class.getName(), this, null);
 
 		MotionWS motionWS = new MotionWS();
 		motionSubject.register(motionWS);
 		wsMotionTracker = PublicWSAdminTracker.createTracker(context, motionWS);
 
 		configureAccelerometer();
 
 		acceld.start();
 		acceld.register(this);
		accRawFeedRef = context.registerService(IAccelerometerRawFeed.class.getName(), acceld, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		IAccelerometerSampleProvider asp = new AccelerometerSampleProvider(acceld, accDevice);
		accSampleProvRef = context.registerService(IAccelerometerSampleProvider.class.getName(), asp, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
		accSampleFeedRef = context.registerService(IAccelerometerSampleFeed.class.getName(), acceld, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
		accControlRef = context.registerService(IAccelerometerControl.class.getName(), accControl, createServicePropertiesWithRanking(OUR_ACCELEROMETER_SERVICES_RANKING));
 		AccelerationWS accWs = new AccelerationWS(asp, LogServiceUtil.getLogService(context));
 		wsAccTracker = PublicWSAdminTracker.createTracker(context, accWs);
 
 		mdaccRef = context.registerService(IMDACCModuleControl.class.getName(), this, createBasicServiceProperties());
 		regionKey = StatusBarUtils.displayImage(context, icon, this.getModuleName());
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
 		StatusBarUtils.releaseRegion(context, regionKey);
 		if (wsMotionTracker != null) {
 			wsMotionTracker.close();
 		}
 
 		if (wsAccTracker != null) {
 			wsAccTracker.close();
 		}
 
 		// TODO: Throw exception at some point if we encounter a failure
 		moduleRef.unregister();
 		motionSubjectRef.unregister();
 		motionRawFeedRef.unregister();
 		accSampleProvRef.unregister();
 		accSampleFeedRef.unregister();
 		mdaccRef.unregister();
 		ledRef.unregister();
 
 		motionSubject.interrupt();
 		motiond.interrupt();
 		motionDevice.ioctl_BMI_MDACC_MOTION_DETECTOR_STOP();
 		motionIs.close();
 		motionDevice.close();
 		mdaccControlDevice.close();
 		accControlRef.unregister();
 		accRawFeedRef.unregister();
 		acceld.unregister(this);
 		acceld.interrupt();
 		accDevice.ioctl_BMI_MDACC_ACCELEROMETER_STOP();
 		accIs.close();
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 		p.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 		return p;
 	}
	
	private Properties createServicePropertiesWithRanking(final int serviceRanking) {
		final Properties p = createBasicServiceProperties();
		p.put(Constants.SERVICE_RANKING, new Integer(serviceRanking));
		return p;
	}
 
 	public List getModuleProperties() {
 		List properties = new ArrayList();
 
 		properties.add(new ModuleProperty("Slot", "" + slotId));
 		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn), "Boolean", true));
 
 		return properties;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 
 		if (property.getName().equals("State")) {
 			deviceOn = Boolean.valueOf((String) property.getValue()).booleanValue();
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
 		acceld = new AccelerometerRawFeed(accIs, accControl);
 
 		mdaccControlDevice = new MDACCControl();
 		CharDeviceUtils.openDeviceWithRetry(mdaccControlDevice, devnode_mdacc_control, 2);
 
 		motionIs = new CharDeviceInputStream(motionDevice);
 		motiond = new MotionRawFeed(motionIs);
 		motionSubject = new MotionSubject(motiond.getInputStream());
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
 
 	public void streamNotification(StreamMultiplexerEvent event) {
 		switch (event.type) {
 		case StreamMultiplexerEvent.EVENT_STREAM_ADDED:
 			if (event.numberOfStreams == 1) {
 				// accDevice.ioctl_BMI_MDACC_ACCELEROMETER_RUN();
 			}
 			break;
 		case StreamMultiplexerEvent.EVENT_STREAM_REMOVED:
 			if (event.numberOfStreams == 0) {
 				// accDevice.ioctl_BMI_MDACC_ACCELEROMETER_STOP();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 }
