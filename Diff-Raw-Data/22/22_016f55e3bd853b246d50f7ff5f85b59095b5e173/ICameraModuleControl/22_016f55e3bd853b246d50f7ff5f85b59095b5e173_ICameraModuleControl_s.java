 package com.buglabs.bug.module.camera.pub;
 
 import java.io.IOException;
 
 import com.buglabs.module.IModuleLEDController;
 
 /**
  * Provides hardware control of the Camera Module
  * 
  * @author Angel Roman
  */
public interface ICameraModuleControl extends IModuleLEDController {
 	/**
 	 * Sets the beam intensity
 	 * 
 	 * @param intensity The intensity of the beam. 0 = low, 1 = high 
 	 * @return negative value if request was not successful.
 	 */
 	public int setFlashBeamIntensity(int intensity) throws IOException;
 	
 	/**
 	 * Turns Flash LED off
 	 * 
 	 * @return negative value if request was not sucessful.
 	 */
 	public int setLEDFlash(boolean state) throws IOException;
 	
 	
 	/**
 	 * Selects a camera based on slot number.
 	 * 
 	 * @param slot the slot number of the desired camera.
 	 * @return negative value if request was not sucessful.
 	 */
 	public int setSelectedCamera(int slot) throws IOException;
 	
 	/**
 	 * Returns the slot number of the selected camera.
 	 * @return negative value if request was not sucessful.
 	 */
 	public int getSelectedCamera() throws IOException;
 }
