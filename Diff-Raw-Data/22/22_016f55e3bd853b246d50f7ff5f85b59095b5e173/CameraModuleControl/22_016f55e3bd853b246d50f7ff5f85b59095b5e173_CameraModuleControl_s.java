 package com.buglabs.bug.module.camera;
 
 import java.io.IOException;
 
 import com.buglabs.bug.jni.camera.CameraControl;
 import com.buglabs.bug.module.camera.pub.ICameraModuleControl;
 
 
public class CameraModuleControl implements ICameraModuleControl {
 
 	private CameraControl cc;
 
 	public CameraModuleControl(CameraControl cameraControl) {
 		this.cc = cameraControl;
 	}
 
 	public int getSelectedCamera() throws IOException {
 		int result = cc.ioctl_BMI_CAM_GET_SELECTED();
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_GET_SELECTED failed");
 		}
 
 		return result;
 	}
 
 
 	public int setSelectedCamera(int slot) throws IOException {
 		int result = cc.ioctl_BMI_CAM_SELECT(slot);
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_SELECT failed");
 		}
 
 		return result;
 	}
 
 	public int setFlashBeamIntensity(int intensity) throws IOException {
 
 		int result;
 
 		if(intensity > 0) {
 			result = cc.ioctl_BMI_CAM_FLASH_HIGH_BEAM();
 		} else {
 			result = cc.ioctl_BMI_CAM_FLASH_LOW_BEAM();
 		}
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_GET_SELECTED failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDFlashOff() throws IOException {
 		int result = cc.ioctl_BMI_CAM_FLASH_LED_OFF();
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 		}
 
 		return result;
 	}
 
 	public int setLEDFlashOn() throws IOException {
 		int result = cc.ioctl_BMI_CAM_FLASH_LED_ON();
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 		}
 
 		return result;
 	}
 
 
 	public int setLEDFlash(boolean state) throws IOException {
 		int result = -1;
 		if(state) {
 			result = cc.ioctl_BMI_CAM_FLASH_LED_ON();
 			if(result < 0) {
 				throw new IOException("ioctl BMI_CAM_FLASH_LED_ON failed");
 			}
 		} else {
 			result = cc.ioctl_BMI_CAM_FLASH_LED_OFF();
 			if(result < 0) {
 				throw new IOException("ioctl BMI_CAM_FLASH_LED_OFF failed");
 			}
 		}
 
 		return result;
 	}
 
 	public int LEDGreenOff() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_GREEN_LED_OFF();
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_OFF failed");
 		}
 
 		return result;
 
 	}
 
 	public int LEDGreenOn() throws IOException {
 		int result = -1;
 
 		result = cc.ioctl_BMI_CAM_GREEN_LED_ON();
 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_ON failed");
 		}
 
 		return result;
 	}
 
 	public int LEDRedOff() throws IOException {
 		int result = -1;
 		
 		result = cc.ioctl_BMI_CAM_RED_LED_OFF();
 		 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_RED_LED_OFF failed");
 		}
 		
 		return result;
 	}
 
 	public int LEDRedOn() throws IOException {
 		int result = -1;
 		
 		result = cc.ioctl_BMI_CAM_RED_LED_ON();
 		 
 		if(result < 0) {
 			throw new IOException("ioctl BMI_CAM_GREEN_LED_ON failed");
 		}
 		
 		return result;
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if(state) {
 			return LEDGreenOn();
 		} else {
 			return LEDGreenOff();
 		}
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if(state) {
 			return LEDRedOn();
 		} else {
 			return LEDRedOff();
 		}
 	}
 }
