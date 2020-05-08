 //FILE:          PalmParameters.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 3/1/13
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 //
 package edu.uchc.octane;
 
 import ij.gui.GenericDialog;
 import java.util.prefs.Preferences;
 
 /**
  * Parameters for PALM plot
  * @author Ji-Yu
  *
  */
 public class PalmParameters {
 
 	final private static String PALM_TYPE_KEY = "PalmType";
 	final private static String PALM_PIXEL_SIZE_KEY = "PalmPixelSize";
 	final private static String PALM_RESOLUTION_KEY = "PalmResolution";
 	final private static String RENDER_IN_COLOR_KEY = "RenderInColor";
 	final private static String LUT_MAX_KEY = "LutMax";
 	final private static String LUT_MIN_KEY = "LutMin";
 	final private static String Z_BOTTOM_KEY = "ZBottom";
 	final private static String Z_TOP_KEY = "ZTop";
 	final private static String Z_RESOLUTION_KEY = "ZResolution";
 	
 	private static boolean bRenderStack_;
 	
 	private static Preferences prefs_ = GlobalPrefs.getRoot().node(PalmParameters.class.getName());
 	
 	private static int palmType_ = prefs_.getInt(PALM_TYPE_KEY, 0);
 	private static boolean bRenderInColor_ = prefs_.getBoolean(RENDER_IN_COLOR_KEY, false);
 	
	static double pixelSize_ = 0;
 	static double palmPixelSize_ = prefs_.getDouble(PALM_PIXEL_SIZE_KEY, 16);
 	static double palmResolution_ = prefs_.getDouble(PALM_RESOLUTION_KEY, 30);
 	static double lutMax_ = prefs_.getDouble(LUT_MAX_KEY, 3.0);
 	static double lutMin_ = prefs_.getDouble(LUT_MIN_KEY, 0);
 	static double palmZMin_ = prefs_.getDouble(Z_BOTTOM_KEY, 0);
 	static double palZMax_ = prefs_.getDouble(Z_TOP_KEY, 3);
 	static double zResolution_ = prefs_.getDouble(Z_RESOLUTION_KEY, 100);
 	
 	private static Palm.PalmType [] typeList_ = {
 			
 		Palm.PalmType.AVERAGE,
 		Palm.PalmType.HEAD,
 		Palm.PalmType.TAIL,
 		Palm.PalmType.ALLPOINTS,
 		Palm.PalmType.TIMELAPSE
 	
 	};
 	
 	/**
 	 * Opens a dialog to input parameters 
 	 * @param data Trajectory dataset
 	 * @param b Whether this is to generate a 3D PALM stack
 	 * @return True if user pressed OK
 	 */
 	static public boolean openDialog(TrajDataset data, boolean b) {
 	
 		if (prefs_ == null) {
 		
 			prefs_ = GlobalPrefs.getRoot().node(PalmParameters.class.getName());
 		
 		}
 
 		bRenderStack_ = b;
 		
 		GenericDialog dlg = b ? new GenericDialog("Construct PALM Stack") : new GenericDialog("Construct PALM");
 		
 		String[] strPalmTypes = { "Average", "Head", "Tail", "All Points", "Movie"};
 		String[] strPalmTypes2 = { "Average", "Head", "Tail", "All Points"};
 		
 		if (b) {
 
 			if (palmType_ >= strPalmTypes2.length) {
 				palmType_ = 0;
 			}
 			
 			dlg.addChoice("PALM Type", strPalmTypes2, strPalmTypes[palmType_]);
 		
 		} else {
 		
 			dlg.addChoice("PALM Type", strPalmTypes, strPalmTypes[palmType_]);
 		}
 		
 		dlg.addMessage("- Size Parameters -");
 		
 		dlg.addNumericField("Image Pixel Size (nm)", data.getPixelSize(), 1);
 		dlg.addNumericField("PALM Pixel Size (nm)", palmPixelSize_, 1);
 		dlg.addNumericField("PALM Resolution (nm)", palmResolution_, 1);
 		
 		dlg.addMessage("- Color Parameters -");
 		
 		dlg.addCheckbox("Render Z coordinates in pseudo-color", bRenderInColor_);
 		dlg.addNumericField("Min Z value (nm)", lutMin_, 1);
 		dlg.addNumericField("Max Z value (nm)", lutMax_, 1);
 
 		if (b) {
 		
 			dlg.addMessage("- Stack Parameters -");
 			dlg.addNumericField("Min Z coordinates (nm)", palmZMin_, 1);
 			dlg.addNumericField("Max Z coordinates (nm)", palZMax_, 1);
 			dlg.addNumericField("Z resolution (nm)", zResolution_, 1);
 		
 		}
 		
 		dlg.showDialog();
 		if (dlg.wasCanceled()) {
 
 			return false;
 		}
 
 		palmType_ = dlg.getNextChoiceIndex();
		pixelSize_ = dlg.getNextNumber(); 
 		palmPixelSize_ = dlg.getNextNumber();
 		palmResolution_ = dlg.getNextNumber();
 		bRenderInColor_ = dlg.getNextBoolean();
 		lutMin_ = dlg.getNextNumber();
 		lutMax_ = dlg.getNextNumber();
 		
 		if (lutMin_ >= lutMax_) {
 		
 			bRenderInColor_ = false;
 		
 		}
 		
 		if (b) {
 		
 			palmZMin_ = dlg.getNextNumber();
 			palZMax_ = dlg.getNextNumber();
 			zResolution_ = dlg.getNextNumber();
 		
 		}
 
 		prefs_.putInt(PALM_TYPE_KEY, palmType_);
 
 		prefs_.putBoolean(RENDER_IN_COLOR_KEY, bRenderInColor_);
 		prefs_.putDouble(PALM_PIXEL_SIZE_KEY, palmPixelSize_);
 		prefs_.putDouble(PALM_RESOLUTION_KEY, palmResolution_);
 		prefs_.putDouble(LUT_MIN_KEY, lutMin_);
 		prefs_.putDouble(LUT_MAX_KEY, lutMax_);
 		prefs_.putDouble(Z_BOTTOM_KEY, palmZMin_);
 		prefs_.putDouble(Z_TOP_KEY, palZMax_);
 		prefs_.putDouble(Z_RESOLUTION_KEY, zResolution_);
 		
 		return true;
 		
 	}
 	
 	/**
 	 * Get Palm type
 	 * @return Palm Type specified by user
 	 */
 	static Palm.PalmType getPalmType() {
 		return typeList_[palmType_];
 	}
 	
 	/**
 	 * Get whether to render in color
 	 * @return Whether to render in color
 	 */
 	static boolean isRenderInColor() {
 		return bRenderInColor_;
 	}
 
 	/**
 	 * Get whether to render 3d PALM stack
 	 * @return Whether to render 3d PALM stack
 	 */
 	static boolean isRenderStack() {
 		return bRenderStack_;
 	}
 
 }
