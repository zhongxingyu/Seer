 package emblcmci.foci3Dtracker;
 
 /** Preprocess image stack by several steps of filtering and corrections
  * 1. 16 bit to 8 bit
  * 2. FFT band pass with parameters.
  * 3. Matching Histogram Bleaching Correction 
  * 
  * @author Kota Miura
  * @author CMCI EMBL, 2010
  */
 
 import emblcmci.BleachCorrection_MH;
 import emblcmci.FFTFilter_NoGenDia;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.WindowManager;
 import ij.gui.GenericDialog;
 import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
 import ij.process.ImageConverter;
 import ij.process.ImageProcessor;
 import ij.process.ImageStatistics;
 import ij.process.StackConverter;
 import ij.process.StackStatistics;
 
 public class PreprocessChromosomeDots {
 	//FFT parameters 
 	private static double filterlarge = 10.0;
 	private static double filtersmall = 2.0;
 	private static double tolerance = 5.0;
 	private static String suppress ="None";
 	private String fftargument;
 	
 	ImagePlus imp;
 
 	public boolean fftparaSetter(){
 		GenericDialog gd = new GenericDialog("FFT Bandpass Filter");
 		gd.addNumericField("Filter_large structures down to", filterlarge, 0, 4, "pixels");
 		gd.addNumericField("Filter_small structures up to", filtersmall, 0, 4, "pixels");
 		gd.addNumericField("Tolerance of direction:", tolerance, 0, 4, "%");
 //		gd.addCheckbox("Autoscale after filtering", doScalingDia);
 //		gd.addCheckbox("Saturate image when autoscaling", saturateDia);
 //		gd.addCheckbox("Display filter", displayFilter);
 //		if (stackSize>1)
 //			gd.addCheckbox("Process entire stack", processStack);	
 		gd.addHelp(IJ.URL+"/docs/menus/process.html#fft-bandpass");
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return false;
 		if(gd.invalidNumber()) {
 			IJ.error("Error", "Invalid input number");
 			return false;
 		}				
 		filterlarge = gd.getNextNumber();
 		filtersmall = gd.getNextNumber();	
 		tolerance = gd.getNextNumber();
 //		choiceDia = choices[choiceIndex];
 //		toleranceDia = gd.getNextNumber();
 //		doScalingDia = gd.getNextBoolean();
 //		saturateDia = gd.getNextBoolean();
 //		displayFilter = gd.getNextBoolean();
 //		if (stackSize>1)
 //			processStack = gd.getNextBoolean();
 		return true;
 		
 	}
 	
 	public void run() {
		if (null == WindowManager.getCurrentImage()) 
			imp = IJ.openImage(); 
		else 		
			imp = new Duplicator().run(WindowManager.getCurrentImage());
 		if (null == imp) return;
 		long startTime = System.currentTimeMillis();
 		ImageConverter.setDoScaling(true);
 		
 		//works OK with the flag -Djava.awt.headless=true
 		IJ.run(imp, "Enhance Contrast", "saturated=0.001 use");
 		
 		//follwoing two lines were used when I did not use the flag -Djava.awt.headless=true
 		//stretchStackHistogram(imp,0.001);
 		//imp.updateAndDraw();
 		
 		StackConverter sc = new StackConverter(imp);
 		sc.convertToGray8();
 		//IJ.run(imp, "8-bit", "");
 
 		// below is the method to access FFT by run() method, and did not work before 
 		//but it might be OK with the flag -Djava.awt.headless=true  
 		//fftbandPssSpec(imp);
 
 		//setparam(double filterLargeDia, double filterSmallDia, int choiceIndex, double toleranceDia, 
 		//		boolean doScalingDia, boolean saturateDia, boolean displayFilter, boolean processStack)
 		FFTFilter_NoGenDia fft = new FFTFilter_NoGenDia();
 		fft.setparam(filterlarge, filtersmall, 0, tolerance, false, false, false, true);
 		fft.core(imp);
 		
 		BleachCorrection_MH BMH = new BleachCorrection_MH(imp);
 		BMH.doCorrection();
 		imp.show();
 		long endTime = System.currentTimeMillis();
 		System.out.println("calculation time  = " + (endTime - startTime) + "ms."); 
 	}
 	public ImagePlus getImp() {
 		return imp;
 	}
 	public void setImp(ImagePlus imp) {
 		this.imp = imp;
 	}
 	public void setFFTparameters(int fl, int fs, int tol, String sups){
 		filterlarge = fl;
 		filtersmall = fs;
 		tolerance = tol;
 		suppress = sups;
 	}	
 	
 	//@deprecated. 
 	public void fftbandPssSpec(ImagePlus imp) {
 		fftargument = "filter_large="+Double.toString(filterlarge)
 						+" filter_small="+Double.toString(filtersmall)
 						+" suppress="+suppress
 						+" tolerance="+Double.toString(tolerance)
 						+" process";
 		IJ.log(fftargument);
 		IJ.run(imp, "Bandpass Filter...", fftargument); 		
 	}
 
 // Following added for headless computing. 
 	
 	//from contrast enhancer
 	public void stretchStackHistogram(ImagePlus imp, double saturated) {
 		
 		int stackSize = imp.getStackSize();
 		ImageStatistics stats = null;
 		stats = new StackStatistics(imp);
 		ImageStack stack = imp.getStack();
 		int[] a = getMinAndMax(saturated, stats);
 		int hmin=a[0], hmax=a[1];
 		double min = stats.histMin+hmin*stats.binSize;
 		double max = stats.histMin+hmax*stats.binSize;
 		System.out.println("hmin = "+ Integer.toString(hmin));
 		System.out.println("hman = "+ Integer.toString(hmax));
 		System.out.println("pix value min = "+ Double.toString(min));
 		System.out.println("pix value hmin = "+ Double.toString(max));
 		for (int i=1; i<=stackSize; i++) {
 			IJ.showProgress(i, stackSize);
 			ImageProcessor ip = stack.getProcessor(i);
 			stretchHistogram(ip, saturated, stats, hmin, hmax, min, max);
 		}
 		imp.updateAndDraw();
 	}
 
 	//modified (simplified) version of ContrastEnhancer
 	public void stretchHistogram(ImageProcessor ip, double saturated, ImageStatistics stats) {		
 		//int[] a = getMinAndMax(ip, saturated, stats);
 		int[] a = getMinAndMax(saturated, stats);
 		int hmin=a[0], hmax=a[1];
 		System.out.println("hmin = "+ Integer.toString(hmin));
 		System.out.println("hmin = "+ Integer.toString(hmax));
 		if (hmax>hmin) {
 			double min = stats.histMin+hmin*stats.binSize;
 			double max = stats.histMin+hmax*stats.binSize;
 			ip.resetRoi();
 			ip.setMinAndMax(min, max);
 		}
 	}
 	public void stretchHistogram(ImageProcessor ip, double saturated, ImageStatistics stats, int hmin, int hmax, double min, double max) {		
 		if (hmax>hmin) {
 			//ip.resetRoi();
 			ip.setMinAndMax(min, max);
 		}
 	}	
 
 	//deleted ImageProcessor from argument since not used. 
 //	int[] getMinAndMax(ImageProcessor ip, double saturated, ImageStatistics stats) {
 	int[] getMinAndMax(double saturated, ImageStatistics stats) {
 
 		int hmin, hmax;
 		int threshold;
 		int[] histogram = stats.histogram;		
 		if (saturated>0.0)
 			threshold = (int)(stats.pixelCount*saturated/200.0);
 		else
 			threshold = 0;
 		int i = -1;
 		boolean found = false;
 		int count = 0;
 		do {
 			i++;
 			count += histogram[i];
 			found = count>threshold;
 		} while (!found && i<255);
 		hmin = i;
 				
 		i = 256;
 		count = 0;
 		do {
 			i--;
 			count += histogram[i];
 			found = count>threshold;
 			//IJ.log(i+" "+count+" "+found);
 		} while (!found && i>0);
 		hmax = i;
 		int[] a = new int[2];
 		a[0]=hmin; a[1]=hmax;
 		return a;
 	}
 	
 
 
 }
