 package emblcmci;
 
 /** Preprocess image stack by several steps of filtering and corrections
  * 1. 16 bit to 8 bit
  * 2. FFT band pass with parameters.
  * 3. Matching Histogram Bleaching Correction 
  * 
  * @author Kota Miura
  * @author CMCI EMBL, 2010
  */
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.WindowManager;
 import ij.plugin.Duplicator;
 import ij.plugin.PlugIn;
 import ij.process.ImageConverter;
 import ij.process.StackConverter;
 
 public class PreprocessChromosomeDots {
 	//FFT parameters
 	private int filterlarge =10;
 	private int filtersmall =2;
 	private int tolerance =5;
 	private String suppress ="None";
 	private String fftargument;
 	
 	ImagePlus imp;
 	public void run() {
 		if (null == WindowManager.getCurrentImage()) 
 			imp = IJ.openImage(); 
 		else 		
 			imp = new Duplicator().run(WindowManager.getCurrentImage());
 		if (null == imp) return;
 		ImageConverter.setDoScaling(true);
 		IJ.run(imp, "Enhance Contrast", "saturated=0.001 use");
 		StackConverter sc = new StackConverter(imp);
 		sc.convertToGray8();
 		//IJ.run(imp, "8-bit", "");
 		fftbandPssSpec(imp);
		BleachCorrection_MH BMH = new BleachCorrection_MH(imp);
		BMH.doCorrection();
 		//imp.show();
 	}
 	public void setFFTparameters(int fl, int fs, int tol, String sups){
 		filterlarge = fl;
 		filtersmall = fs;
 		tolerance = tol;
 		suppress = sups;
 	}	
 	
 	public void fftbandPssSpec(ImagePlus imp) {
 		fftargument = "filter_large="+Integer.toString(filterlarge)
 						+" filter_small="+Integer.toString(filtersmall)
 						+" suppress="+suppress
 						+" tolerance="+Integer.toString(tolerance)
 						+" process";
 		IJ.log(fftargument);
 		IJ.run(imp, "Bandpass Filter...", fftargument); 		
 	}	
 
 }
