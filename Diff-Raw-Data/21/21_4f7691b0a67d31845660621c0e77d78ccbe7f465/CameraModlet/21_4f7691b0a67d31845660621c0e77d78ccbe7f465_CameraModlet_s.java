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
 		cameraControlRef = context.registerService(ICameraModuleControl.class.getName(), cameraControl, createRemotableProperties(null));
 		moduleControl = context.registerService(IModuleControl.class.getName(), this, null);
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
 
 		// Removing...this information needs to come from the device.
 		// modProps.add(new ModuleProperty("MP", "" + megapixels, "Number",
 		// false));
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
 }
