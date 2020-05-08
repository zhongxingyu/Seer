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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.jni.camera.Camera;
 import com.buglabs.bug.jni.camera.CameraControl;
 import com.buglabs.bug.module.camera.pub.ICamera2Device;
 import com.buglabs.bug.module.camera.pub.ICamera2ModuleControl;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.PublicWSProviderWithParams;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.XmlNode;
 
 public class CameraModuleControl implements ICameraModuleControl, ICamera2ModuleControl, IModuleLEDController, PublicWSProviderWithParams {
 
 	private CameraControl cc;
 	private ICamera2Device camera2Device;
 	
 	public CameraModuleControl(CameraControl cameraControl, ICamera2Device camera2Device) {
 		this.cc = cameraControl;
 		this.camera2Device = camera2Device;
 	}
 	
	public int setSelectedCamera(int slot) throws IOException {
		// TODO Not implemented
		return -1;
	}
	
 	public int getSelectedCamera() throws IOException {
 		int result = cc.ioctl_BMI_CAM_GET_SELECTED();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_GET_SELECTED failed");
 		}
 
 		return result;
 	}
 
 	public int setFlashBeamIntensity(int intensity) throws IOException {
 
 		int result;
 
 		if (intensity > 0) {
 			result = cc.ioctl_BMI_CAM_FLASH_HIGH_BEAM();
 		} else {
 			result = cc.ioctl_BMI_CAM_FLASH_LOW_BEAM();
 		}
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_GET_SELECTED failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDFlashOff() throws IOException {
 		int result = cc.ioctl_BMI_CAM_FLASH_LED_OFF();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDFlashOn() throws IOException {
 		int result = cc.ioctl_BMI_CAM_FLASH_LED_ON();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDFlash(boolean state) throws IOException {
 		int result = -1;
 		if (state) {
 			result = cc.ioctl_BMI_CAM_FLASH_LED_ON();
 			if (result < 0) {
 				throw new IOException("ioctl BMI_CAM_FLASH_LED_ON failed");
 			}
 		} else {
 			result = cc.ioctl_BMI_CAM_FLASH_LED_OFF();
 			if (result < 0) {
 				throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 			}
 		}
 
 		return result;
 	}
 
 	public int LEDGreenOff() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_GREEN_LED_OFF();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_OFF failed");
 		}
 
 		return result;
 
 	}
 
 	public int LEDGreenOn() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_GREEN_LED_ON();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_ON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOff() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_RED_LED_OFF();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_RED_LED_OFF failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOn() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_RED_LED_ON();
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_ON failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if (state) {
 			return LEDGreenOn();
 		} else {
 			return LEDGreenOff();
 		}
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (state) {
 			return LEDRedOn();
 		} else {
 			return LEDRedOff();
 		}
 	}
 
 	public int getTestPattern() {
 		return cc.getTestPattern();
 	}
 	
 	public int setTestPattern(int testPattern) {
 		return cc.setTestPattern(testPattern);
 	}
 
 	public int getColorEffects() {
 		return cc.getColorEffects();
 	}
 	
 	public int setColorEffects(int colorEffects) {
 		return cc.setColorEffects(colorEffects);
 	}
 
 	public int getVerticalFlip() {
 		return cc.getVerticalFlip();
 	}
 	
 	public int setVerticalFlip(int verticalFlip) {
 		return cc.setVerticalFlip(verticalFlip);
 	}
 
 	public int getHorizontalMirror() {
 		return cc.getHorizontalMirror();
 	}
 	
 	public int setHorizontalMirror(int horizontalMirror) {
 		return cc.setHorizontalMirror(horizontalMirror);
 	}
 
 	public int getExposureLevel() {
 		return cc.getExposureLevel();
 	}
 	
 	public int setExposureLevel(int exposureLevel) {
 		return cc.setExposureLevel(exposureLevel);
 	}
 
 	
 	// For WS:
 	private String serviceName = "CameraControl";
 	private ServiceRegistration wsReg;
 	public void setPublicName(String name) {
 		serviceName = name;
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
 		// not executed; we use the one below
 		return null;
 	}
 	
 	public IWSResponse execute(int operation, String input, Map get, Map post) {
 		if (operation == PublicWSProvider2.GET) {
 			// allow setting via http://bugip/service/CameraControl?testPattern=2 etc
 
 			String v = (String) get.get("flash");
 			if (v != null) {
 				final int i = Integer.parseInt(v);
 				System.out.println("setting flash " + ((i == 1) ? "ON" : "OFF"));
 				try {
 					setLEDFlash((i == 1) ? true : false);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			v = (String) get.get("flashIntensity");
 			if (v != null) {
 				System.out.println("setting flash intensity to " + v);
 				try {
 					this.setFlashBeamIntensity(Integer.parseInt(v));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			if (get.containsKey("cameraOpen")) {
 				System.out.println("opening camera");
 				camera2Device.bug_camera_open_default();
 			}
 
 			v = (String) get.get("testPattern");
 			if (v != null) {
 				System.out.println("Setting test pattern to " + v);
 				setTestPattern(Integer.parseInt(v));
 			}
 			v = (String) get.get("colorEffects");
 			if (v != null) {
 				System.out.println("Setting color effects to " + v);
 				setColorEffects(Integer.parseInt(v));
 			}
 			v = (String) get.get("verticalFlip");
 			if (v != null) {
 				System.out.println("Setting vertical flip to " + v);
 				setVerticalFlip(Integer.parseInt(v));
 			}
 			v = (String) get.get("horizontalMirror");
 			if (v != null) {
 				System.out.println("Setting horizontal mirror to " + v);
 				setHorizontalMirror(Integer.parseInt(v));
 			}
 			v = (String) get.get("exposureLevel");
 			if (v != null) {
 				System.out.println("Setting exposure level to " + v);
 				setExposureLevel(Integer.parseInt(v));
 			}
 			
 			if (get.containsKey("cameraStart")) {
 				System.out.println("starting camera");
 				camera2Device.bug_camera_start();
 			}
 			
 			if (get.containsKey("cameraStop")) {
 				System.out.println("stopping camera");
 				camera2Device.bug_camera_stop();
 			}
 			
 			if (get.containsKey("cameraClose")) {
 				System.out.println("closing camera");
 				camera2Device.bug_camera_close();
 			}
 			
 			return new WSResponse(getCameraInfoXml(), "text/xml");
 		}
 		return null;
 	}
 
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	public String getDescription() {
 		return "This service can retrieve and set camera properties.";
 	}
 
 	private static String testPatternString(int testPattern) {
 		switch (testPattern) {
 		case 0: return "Disabled";
 		case 1: return "Walking 1s";
 		case 2: return "Solid White";
 		case 3: return "Grey Ramp";
 		case 4: return "Color Bars";
 		case 5: return "Black/White Bars";
 		case 6: return "PseudoRandom";
 		default: return "ERROR: " + testPattern;
 		}
 	}
 	
 	private static String colorEffectsString(int colorEffects) {
 		switch (colorEffects) {
 		case 0: return "Disabled";
 		case 1: return "Black and White";
 		case 2: return "Sepia";
 		case 3: return "Negative";
 		case 4: return "Solarize";
 		default: return "ERROR: " + colorEffects;
 		}
 	}
 	
 	private static String positiveValueOkayString(int value) {
 		if (value < 0) {
 			return "ERROR: " + value;
 		} else {
 			return Integer.toString(value);
 		}
 	}
 	
 	private String getCameraInfoXml() {
 		XmlNode root = new XmlNode("CameraInfo");
 		try {
 			if (camera2Device.is_camera_open()) {
 				if (camera2Device.is_camera_started()) {
 					root.addChildElement(new XmlNode("CameraInfo", "Camera is open and started"));
 				} else {
 					root.addChildElement(new XmlNode("CameraInfo", "Camera is open but not yet started"));
 				}
 			} else {
 				root.addChildElement(new XmlNode("CameraInfo", "Camera is not open."));
 			}
 			
 			root.addChildElement(new XmlNode("TestPattern", testPatternString(getTestPattern())));
 			root.addChildElement(new XmlNode("ColorEffects", colorEffectsString(getColorEffects())));
 			root.addChildElement(new XmlNode("VerticalFlip", positiveValueOkayString(getVerticalFlip())));
 			root.addChildElement(new XmlNode("HorizontalMirror", positiveValueOkayString(getHorizontalMirror())));
 			root.addChildElement(new XmlNode("ExposureLevel", positiveValueOkayString(getExposureLevel())));
 		} catch (SelfReferenceException e) {
 			System.err.println("Xml error " + e);
 		}
 		return root.toString();
 	}
 }
