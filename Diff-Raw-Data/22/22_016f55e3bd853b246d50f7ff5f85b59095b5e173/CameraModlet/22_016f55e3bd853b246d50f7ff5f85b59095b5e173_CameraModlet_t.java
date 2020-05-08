 /* Copyright (c) 2007, 2008 Bug Labs, Inc.
  * All rights reserved.
  *   
  * This program is free software; you can redistribute it and/or  
  * modify it under the terms of the GNU General Public License version  
  * 2 only, as published by the Free Software Foundation.   
  *   
  * This program is distributed in the hope that it will be useful, but  
  * WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
  * General Public License version 2 for more details (a copy is  
  * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
  *   
  * You should have received a copy of the GNU General Public License  
  * version 2 along with this work; if not, write to the Free Software  
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
  * 02110-1301 USA   
  *
  */
 package com.buglabs.bug.module.camera;
 
 import java.awt.Rectangle;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.input.pub.InputEventProvider;
 import com.buglabs.bug.jni.camera.Camera;
 import com.buglabs.bug.jni.camera.CameraControl;
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.camera.pub.ICameraButtonEventProvider;
 import com.buglabs.bug.module.camera.pub.ICameraDevice;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 /**
  * 
  * @author kgilmer
  * 
  */
 public class CameraModlet implements IModlet, ICameraDevice, PublicWSProvider, IModuleControl {
 	private static final String IMAGE_MIME_TYPE = "image/jpg";
 	private static final String DEVNODE_INPUT_DEVICE = "/dev/input/bmi_cam";
 	private static final String CAMERA_DEVICE_NODE = "/dev/v4l/video0";
 	private static final String CAMERA_CONTROL_DEVICE_NODE = "/dev/bug_camera_control";
 
 	private ServiceTracker wsTracker;
 
 	private int megapixels;
 
 	private List modProps;
 
 	private final BundleContext context;
 
 	private final int slotId;
 
 	private final String moduleName;
 
 	private ServiceRegistration moduleControl;
 
 	private ServiceRegistration cameraService;
 	
 	private ServiceRegistration bepReg;
 
 	private LogService logService;
 
 	private Camera camera;
 
 	private String moduleId;
 
 	private String regionKey;
 
 	private static boolean icon[][] = { { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false },
 		{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 		{ false, true, false, false, false, false, false, false, false, false, false, true, true, false, true, false },
 		{ false, true, false, false, true, true, true, true, true, true, true, true, true, false, true, false },
 		{ false, true, false, true, false, false, false, false, false, false, false, false, true, false, true, false },
 		{ false, true, false, true, false, false, false, false, true, true, true, true, true, false, true, false },
 		{ false, true, false, true, false, false, true, true, false, true, true, true, true, false, true, false },
 		{ false, true, false, true, false, false, true, true, false, true, true, true, true, false, true, false },
 		{ false, true, false, true, false, true, false, false, true, true, true, true, true, false, true, false },
 		{ false, true, false, true, false, true, true, true, true, true, true, true, true, false, true, false },
 		{ false, true, false, true, true, true, true, true, true, true, true, true, true, false, true, false },
 		{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 		{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 		{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false } };
 
 	private CameraModuleControl cameraControl;
 
 	private ServiceRegistration cameraControlRef;
 
 	private CameraControl cc;
 
 	private InputEventProvider bep;
 	private ServiceRegistration ledRef;
 
 	public CameraModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.moduleName = "CAMERA";
 		// TODO See if we can get this from the Camera driver.
 		this.megapixels = 2;
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
 		//TODO: Change this when we move to linux 2.6.22 or greater since
 		//BMI agent should listen to UDEV ACTION=="online" before starting modlets
 		try {
 			CharDeviceUtils.openDeviceWithRetry(camera, CAMERA_DEVICE_NODE, 2);
 		} catch (IOException e) {
 			String errormsg =  "Unable to open camera device node: " + CAMERA_DEVICE_NODE +
 			"\n trying again...";
 			logService.log(LogService.LOG_ERROR, errormsg);
 			throw e;
 		}
 		
 	
 		cc = new CameraControl();
 		try {
 			CharDeviceUtils.openDeviceWithRetry(cc, CAMERA_CONTROL_DEVICE_NODE, 2);
 		} catch (IOException e){
 			String errormsg =  "Unable to open camera device node: " + CAMERA_CONTROL_DEVICE_NODE +
 			"\n trying again...";
 			logService.log(LogService.LOG_ERROR, errormsg);
 			throw e;
 		}
		System.out.println("I'm here!");
		System.out.println("I'm here!");
		System.out.println("I'm here!");
		System.out.println("I'm here!");
		System.out.println("I'm here!");
 		cameraControl = new CameraModuleControl(cc);
 		cameraControlRef = context.registerService(ICameraModuleControl.class.getName(), cameraControl, createRemotableProperties(null));
 		moduleControl = context.registerService(IModuleControl.class.getName(), this, createRemotableProperties(null));
 		cameraService = context.registerService(ICameraDevice.class.getName(), this, createRemotableProperties(null));
 		ledRef = context.registerService(IModuleLEDController.class.getName(), cameraControl, createRemotableProperties(null));
 		
 		bep = new CameraInputEventProvider(DEVNODE_INPUT_DEVICE, logService);
 		bep.start();
 		
 		bepReg = context.registerService(ICameraButtonEventProvider.class.getName(), bep, createRemotableProperties(getButtonServiceProperties()));
 		// Display the camera icon
 		regionKey = StatusBarUtils.displayImage(context, icon, this.getModuleName());
 
 		List wsProviders = new ArrayList();
 		wsProviders.add(this);
 
 		wsTracker = PublicWSAdminTracker.createTracker(context, wsProviders);
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
 		StatusBarUtils.releaseRegion(context, regionKey);
 		cameraControlRef.unregister();
 		cameraService.unregister();
 		moduleControl.unregister();
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
 		if (operation == PublicWSProvider.GET) {
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
 		if (operation == PublicWSProvider.GET) {
 			return new WSResponse(getImageInputStream(), IMAGE_MIME_TYPE);
 		}
 		return null;
 	}
 
 	public String getPublicName() {
 		return "Picture";
 	}
 
 	public List getModuleProperties() {
 		modProps.clear();
 
 		//Removing...this information needs to come from the device.
 		//modProps.add(new ModuleProperty("MP", "" + megapixels, "Number", false));
 		modProps.add(new ModuleProperty("Slot", "" + slotId));
 
 		return modProps;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		return false;
 	}
 
 	public byte[] getImage() {
 		return camera.grabFrame();
 	}
 	
 	public boolean initOverlay(Rectangle pbounds){
 		
 		if (camera.overlayinit(pbounds.x, pbounds.y, pbounds.width, pbounds.height) < 0)
 			return false;
 		else
 			return true;		
 	}
 	
 	public boolean startOverlay(){
 		if (camera.overlaystart() < 0)
 			return false;
 		else
 			return true;
 	}
 	
 	public boolean stopOverlay(){
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
 }
