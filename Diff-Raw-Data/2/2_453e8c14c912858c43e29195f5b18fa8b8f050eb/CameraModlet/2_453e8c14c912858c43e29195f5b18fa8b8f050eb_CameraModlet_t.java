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
 package com.buglabs.bug.module.camera;
 
 import java.awt.Rectangle;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.input.pub.InputEventProvider;
 import com.buglabs.bug.jni.camera.Camera;
 import com.buglabs.bug.jni.camera.CameraControl;
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.module.camera.pub.ICameraButtonEventProvider;
 import com.buglabs.bug.module.camera.pub.ICameraDevice;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 /**
  * 
  * @author kgilmer
  * 
  */
 public class CameraModlet implements IModlet, ICameraDevice, PublicWSProvider2, IModuleControl {
 	private static final String IMAGE_MIME_TYPE = "image/jpg";
 	private static final String DEVNODE_INPUT_DEVICE = "/dev/input/bmi_cam";
 	private static final String CAMERA_DEVICE_NODE = "/dev/v4l/video0";
 	private static final String CAMERA_CONTROL_DEVICE_NODE = "/dev/bug_camera_control";
 	private static boolean suspended = false;
 
 	private ServiceTracker wsTracker;
 
 	private List modProps;
 
 	private final BundleContext context;
 
 	private final int slotId;
 
 	private final String moduleName;
 
 	private ServiceRegistration moduleRef;
 
 	private ServiceRegistration cameraService;
 
 	private ServiceRegistration bepReg;
 
 	private LogService logService;
 
 	private Camera camera;
 	
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	private String moduleId;
 
 	private CameraModuleControl cameraControl;
 
 	private ServiceRegistration cameraControlRef;
 
 	private CameraControl cc;
 
 	private InputEventProvider bep;
 	private ServiceRegistration ledRef;
 	private String serviceName = "Picture";
 	private BMIModuleProperties properties;
 
 	public CameraModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
		this.moduleName = "Camera";
 		this.properties = null;
 	}
 
 	public CameraModlet(BundleContext context, int slotId, String moduleId, BMIModuleProperties properties) {
 		this(context, slotId, moduleId);
 		this.properties = properties;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public void setup() throws Exception {
 		logService = LogServiceUtil.getLogService(context);
 	}
 
 	public void start() throws Exception {
 		modProps = new ArrayList();
 
 		camera = new Camera();
 		// TODO: Change this when we move to linux 2.6.22 or greater since
 		// BMI agent should listen to UDEV ACTION=="online" before starting
 		// modlets
 		try {
 			CharDeviceUtils.openDeviceWithRetry(camera, CAMERA_DEVICE_NODE, 2);
 		} catch (IOException e) {
 			String errormsg = "Unable to open camera device node: " + CAMERA_DEVICE_NODE + "\n trying again...";
 			logService.log(LogService.LOG_ERROR, errormsg);
 			throw e;
 		}
 
 		cc = new CameraControl();
 		try {
 			CharDeviceUtils.openDeviceWithRetry(cc, CAMERA_CONTROL_DEVICE_NODE, 2);
 		} catch (IOException e) {
 			String errormsg = "Unable to open camera device node: " + CAMERA_CONTROL_DEVICE_NODE + "\n trying again...";
 			logService.log(LogService.LOG_ERROR, errormsg);
 			throw e;
 		}
 		cameraControl = new CameraModuleControl(cc);
 		cameraControlRef = context.registerService(ICameraModuleControl.class.getName(), cameraControl, createBasicServiceProperties());
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended": "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 		cameraService = context.registerService(ICameraDevice.class.getName(), this, createBasicServiceProperties());
 		ledRef = context.registerService(IModuleLEDController.class.getName(), cameraControl, createBasicServiceProperties());
 
 		bep = new CameraInputEventProvider(DEVNODE_INPUT_DEVICE, logService);
 		bep.start();
 
 		bepReg = context.registerService(ICameraButtonEventProvider.class.getName(), bep, createRemotableProperties(getButtonServiceProperties()));
 
 		List wsProviders = new ArrayList();
 		wsProviders.add(this);
 
 		wsTracker = PublicWSAdminTracker.createTracker(context, wsProviders);
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
 
 	public void stop() throws Exception {
 		cameraControlRef.unregister();
 		cameraService.unregister();
 		moduleRef.unregister();
 		ledRef.unregister();
 		bep.tearDown();
 		bepReg.unregister();
 		wsTracker.close();
 		camera.close();
 		cc.close();
 	}
 
 	/**
 	 * @return a dictionary of properties for the IButtonEventProvider service.
 	 */
 	private Dictionary getButtonServiceProperties() {
 		Dictionary props = new Hashtable();
 
 		props.put("ButtonEventProvider", this.getClass().getName());
 		props.put("ButtonsProvided", "Camera");
 
 		return props;
 	}
 
 	public PublicWSDefinition discover(int operation) {
 		if (operation == PublicWSProvider2.GET) {
 			return new PublicWSDefinition() {
 
 				public List getParameters() {
 					return null;
 				}
 
 				public String getReturnType() {
 					return IMAGE_MIME_TYPE;
 				}
 			};
 		}
 
 		return null;
 	}
 
 	public IWSResponse execute(int operation, String input) {
 		if (operation == PublicWSProvider2.GET) {
 			return new WSResponse(getImageInputStream(), IMAGE_MIME_TYPE);
 		}
 		return null;
 	}
 
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	public List getModuleProperties() {
 		modProps.clear();
 
 		modProps.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		modProps.add(new ModuleProperty("Slot", "" + slotId));
 		modProps.add(new ModuleProperty("Power State", suspended ? "Suspended": "Active", "String", true));
 		
 		if (properties != null) {
 			modProps.add(new ModuleProperty("Module Description", properties.getDescription()));
 			modProps.add(new ModuleProperty("Module SN", properties.getSerial_num()));
 			modProps.add(new ModuleProperty("Module Vendor ID", "" + properties.getVendor()));
 			modProps.add(new ModuleProperty("Module Revision", "" + properties.getRevision()));
 		}
 		
 		return modProps;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 		if (property.getName().equals("Power State")) {
 			if (((String) property.getValue()).equals("Suspend")){
 				try{
 				suspend();
 				}
 			 catch (IOException e) {
 				 LogServiceUtil.logBundleException(logService, e.getMessage(), e);
 			}
 			}
 			else if (((String) property.getValue()).equals("Resume")){
 				
 				try {
 					resume();
 				} catch (IOException e) {
 					LogServiceUtil.logBundleException(logService, e.getMessage(), e);
 				}
 			}
 			
 				
 			
 		}
 		
 		return false;
 	}
 	
 	public int resume() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_RESUME();
 		suspended = false;
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_RESUME failed");
 		}
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 	}
 	
 
 	public int suspend() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_SUSPEND();
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_SUSPEND failed");
 		}
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 	}
 
 	public byte[] getImage() {
 		return camera.grabFrame();
 	}
 
 	public byte[] getImage(int sizeX, int sizeY, int format, boolean highQuality) {
 		return camera.grabFrameExt(sizeX, sizeY, format, highQuality);
 	}
 
 	public boolean initOverlay(Rectangle pbounds) {
 
 		if (camera.overlayinit(pbounds.x, pbounds.y, pbounds.width, pbounds.height) < 0)
 			return false;
 		else
 			return true;
 	}
 
 	public boolean startOverlay() {
 		if (camera.overlaystart() < 0)
 			return false;
 		else
 			return true;
 	}
 
 	public boolean stopOverlay() {
 		if (camera.overlaystop() < 0)
 			return false;
 		else
 			return true;
 	}
 
 	public InputStream getImageInputStream() {
 		return new ByteArrayInputStream(camera.grabFrame());
 	}
 
 	public String getFormat() {
 
 		return IMAGE_MIME_TYPE;
 	}
 
 	public String getDescription() {
 		return "This service can return image data from a hardware camera.";
 	}
 
 	public void setPublicName(String name) {
 		serviceName = name;
 	}
 
 	
 }
