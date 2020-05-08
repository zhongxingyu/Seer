 package emblcmci;
 
 /* segmentation of yeast chromosome 
  * use 3D object counter to adjust threshold level of 3D stack. 
  * 3D object counter must be installed in ImageJ
  * 100614 main functions now in separate classes
  * @author Kota Miura  
  * @ cmci, embl miura@embl.de
  */
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 import ij.plugin.Duplicator;
 import ij.*;
 import Utilities.Counter3D;
 import Utilities.Object3D;
 import ij.gui.GenericDialog;
 import ij.gui.Line;
 import ij.gui.Roi;
 import ij.measure.Calibration;
 import ij.measure.ResultsTable;
 import ij.plugin.*;
 import ij.process.ImageProcessor;
 import ij.process.StackConverter;
 import ij.process.StackProcessor;
 
 public class AutoThresholdAdjuster3D {
 
 	private static boolean createComposite = true;	
 	int thr, minSize, maxSize, dotSize, fontSize;
 	boolean excludeOnEdges, showObj, showSurf, showCentro, showCOM, showNb, whiteNb, newRT, showStat, showMaskedImg, closeImg, showSummary, redirect;
 
 	ParamSetter para = new ParamSetter();
 	int maxspotvoxels = para.getMaxspotvoxels();
 	
 	/**Object volume minimum for volume-based segmentation*/
 	int minspotvoxels = para.getMinspotvoxels();	
 	
 	/**object volume minimum for measurement
 	 *  (maybe 7 is too small)*/
 	int minspotvoxels_measure = para.getMinspotvoxels_measure();
 	
 	/** maximum loop for exiting optimum threshold searching	 */
 	int maxloops = para.getMaxloops();
 	
 	int thadj_volmin = para.getThadj_volmin();
 	
 	int thadj_volmax = para.getThadj_volmax();
 	
 	int thadj_nummin = para.getThadj_nummin();
 	
 	int thadj_nummax = para.getThadj_nummax();
 	
 	int segMethod = para.getSegMethod();
 
 	String fullpathtoTrainedData0 = para.getTrainedDataFullPath0();
 	
 	String fullpathtoTrainedData1 = para.getTrainedDataFullPath1();
 	
 	
 	/** extended class of Object3D 
 	 * object3D + timepoint, channel, dotID
 	 * */
 	Object4D obj4d;	//Object3D added with time point and channel number fields. 
 	
 	/** Vector for storing detected dots in channel 0	 */
 	Vector<Object4D> obj4Dch0; 
 
 	/** Vector for storing detected dots in channel 1	 */
 	Vector<Object4D> obj4Dch1; 
 	
 	Calibration cal, calkeep;
 	
 	/**Factor to multiply for depth, to correct for xy pixel scale =1
 	 * 
 	 */
 	double zfactor;
 	
 	public void run() {
 
 		//copied and modified from image - color merge... (RGBStackMerge.java)
 		int[] wList = WindowManager.getIDList();
 		if (wList==null) {
 			IJ.error("bory dot analysis", "No images are open.");
 			return;
 		}
 
 		String[] titles = new String[wList.length+1];
 		for (int i=0; i<wList.length; i++) {
 			ImagePlus imp = WindowManager.getImage(wList[i]);
 			titles[i] = imp!=null?imp.getTitle():"";
 		}
 		String none = "*None*";
 		titles[wList.length] = none;
 
 		GenericDialog gd = new GenericDialog("Bory Dot Analysis");
 		gd.addChoice("Ch0:", titles, titles[0]);
 		gd.addChoice("Ch1:", titles, titles[1]);
 		//String title3 = titles.length>2&&!IJ.macroRunning()?titles[2]:none;
 		gd.addCheckbox("Create Merged Binary", createComposite);
 		//gd.addCheckbox("Keep Source Images", false);
 		gd.showDialog();
 		if (gd.wasCanceled())
 			return;
 		int[] index = new int[2];
 		index[0] = gd.getNextChoiceIndex();
 		index[1] = gd.getNextChoiceIndex();
 		createComposite = gd.getNextBoolean();
 		ImagePlus imp0 = WindowManager.getImage(wList[index[0]]);
 		ImagePlus imp1 = WindowManager.getImage(wList[index[1]]);		
 		//cal.set
 		if (imp0 == null) return;
 		if (imp0.getStackSize() == 1) {
 			IJ.error("Channel 0 is not a stack");
 			return;		
 		}
 		if (imp1.getStackSize() == 1) {
 			IJ.error("Channel 1 is not a stack");
 			return;
 		}
 		cal = imp0.getCalibration();
 		if (Double.isNaN(cal.pixelDepth)) {
 			IJ.error("Voxel Depth(z)is not defined correctly: check [Image -> properties]");
 			return;
 		}
 		
 		calkeep = cal.copy();
 		zfactor = cal.pixelDepth / cal.pixelWidth;
 		
 		IJ.log("min vox segment: " + Integer.toString(minspotvoxels) + 
 				"\n min vox measure : " + Integer.toString(minspotvoxels_measure));
 	
 		Roi r0 = imp0.getRoi();
 		Roi r1 = imp1.getRoi();
 		Roi r = null;
 
 		if ((r0 == null) && (r1 == null))				
 			segAndMeasure( imp0, imp1);
 		else {
 			ImagePlus imp0roi = null;
 			ImagePlus imp1roi = null;
 			IJ.log("... ROI found ... ");			
 			if (r0 == null) 
 				r =r1;
 			else
 				//if (r1 == null)
 				r =r0;
 			if (r == null) {
 					IJ.error("need a rectangular ROI");
 					return;
 			}
 			if (!r.isArea()) {
 				IJ.error("need a rectangular ROI");
 				return;
 			}
 			Rectangle rb = r.getBounds();
 			imp0.killRoi();
 			imp1.killRoi();			
 
 			ImagePlus tempdup = null;
 			StackProcessor tempstackproc = null;
 			ImageStack cropstack = null;
 
 			tempdup = new Duplicator().run(imp0);
 			tempstackproc  = new StackProcessor(tempdup.getStack(),tempdup.getStack().getProcessor(1));
 			cropstack = tempstackproc.crop(rb.x, rb.y, rb.width, rb.height);
 			imp0roi = new ImagePlus("croppedCh0", cropstack);
 			imp0roi.setCalibration(cal);
 			imp0roi.setDimensions(imp0.getNChannels(), imp0.getNSlices(), imp0.getNFrames());
 			imp0roi.show();
 			
 			tempdup = new Duplicator().run(imp1);
 			tempstackproc  = new StackProcessor(tempdup.getStack(),tempdup.getStack().getProcessor(1));
 			cropstack = tempstackproc.crop(rb.x, rb.y, rb.width, rb.height);
 			imp1roi = new ImagePlus("croppedCh1", cropstack);
 			imp1roi.setCalibration(cal);
 			imp1roi.setDimensions(imp1.getNChannels(), imp1.getNSlices(), imp1.getNFrames());
 			imp1roi.show();
 						
 			segAndMeasure(imp0roi, imp1roi);
 		}
 	}
 	
 	public boolean segAndMeasure(ImagePlus imp0, ImagePlus imp1){
 		ImagePlus binimp0, binimp1;
 		obj4Dch0 = new Vector<Object4D>();
 		obj4Dch1 = new Vector<Object4D>();
 		//auto adjusted threshold segmentation
 		if (segMethod == 0) {		
 			binimp0 = segmentaitonByObjectSize(imp0);
 			binimp1 = segmentaitonByObjectSize(imp1);
 		} else {
 		//Trainable Segmentation
 			if (segMethod == 1){
 				DotSegmentByTrained train = new DotSegmentByTrained();
 				train.Setfullpathdata(fullpathtoTrainedData0);
 				binimp0 = train.core(imp0);
 				train.Setfullpathdata(fullpathtoTrainedData1);
 				binimp1 = train.core(imp1);
 			} else {
 		//3D particle detection
 				if (segMethod == 2){			
 					IJ.log("Segmentation using Particle Tracker 3D is not completed Yet");
 					//particle tracker3D
 					DotSegmentByParticletracker3D dpt3D = new DotSegmentByParticletracker3D();
 					IJ.log("--- channel0 ---");
 					dpt3D.setup("", imp0);
 					dpt3D.InitiUserDefinedPara();
 					IJ.log("Radius:" + Double.toString(dpt3D.radius));
 					IJ.log("Cutoff:" + Double.toString(dpt3D.cutoff));
 					IJ.log("Percentile:" + Double.toString(dpt3D.percentile));	
 					//if (!dpt3D.parameterDialog()) return false; //this line must be changed as parameter setter for particle3D
 					String particles = dpt3D.DetectDots3D(imp0);
 					storeParticleInfoInObj4D(particles, obj4Dch0, "ch0");
 					IJ.log(particles);
 					//for (int i = 0; i < obj4Dch0.size(); i++) IJ.log("frame"+Integer.toString(obj4Dch0.get(i).timepoint) + ":"+ Float.toString(obj4Dch0.get(i).score));
 					IJ.log("--- channel1 ---");
 					DotSegmentByParticletracker3D dpt3D2 = new DotSegmentByParticletracker3D();
 					dpt3D2.setup("", imp1);
 					dpt3D2.InitiUserDefinedPara();
					// at least percentile should be able to be controlled differently from ch1
 					IJ.log("Radius:" + Double.toString(dpt3D2.radius));
 					IJ.log("Cutoff:" + Double.toString(dpt3D2.cutoff));
 					IJ.log("Percentile:" + Double.toString(dpt3D2.percentile));					
 					particles = dpt3D2.DetectDots3D(imp1);
 					storeParticleInfoInObj4D(particles, obj4Dch1, "ch1");
 					IJ.log(particles);
 					//TODO there should be another process to generates bin stacks for visualization. 
 					//return false;
 					binimp0 = null;
 					binimp1 = null;
 				} else return false;	
 			}
 		}
 		//binimp0.show();
 		//binimp1.show();
 
 		ImagePlus rgbbin=null;
 		/* in case of particle 3D, no binary images are produced so no composite image. */
 		if ((segMethod != 2) && (createComposite)) {
 			ImagePlus ch0proj=null;
 			ImagePlus ch1proj=null;
 			ch0proj = createZprojTimeSeries(binimp0, imp0.getNSlices(), imp0.getNFrames());
 			ch1proj = createZprojTimeSeries(binimp1, imp1.getNSlices(), imp1.getNFrames());
 			ImageStack dummy = null;
 			RGBStackMerge rgbm = new RGBStackMerge();
 			ImageStack rgbstack = rgbm.mergeStacks(ch0proj.getWidth(), ch0proj.getHeight(), ch0proj.getStackSize(), ch0proj.getStack(), ch1proj.getStack(), dummy, true);
 			rgbbin = new ImagePlus("binProjMerged", rgbstack);
 			rgbbin.show();
 			
 		}
 		//3D object measurement part
 		int ch0objnum = 0; 
 		int ch1objnum = 0;
 		if (segMethod != 2) { 
 			ch0objnum = measureDots(binimp0, "Ch0", obj4Dch0, imp0.getNSlices());
 			ch1objnum = measureDots(binimp1, "Ch1", obj4Dch1, imp1.getNSlices());
 		} 
 		showStatistics(obj4Dch0);
 		showStatistics(obj4Dch1);		
 		//analysis 
 		
 		Object4D[][] linkedArray = dotLinker(obj4Dch0,  obj4Dch1, imp0.getNFrames());
 		
 		showDistances(linkedArray);
 		 //if (rgbbin != null) drawlinks(linkedArray, rgbbin);
 		drawlinksGrayscale(linkedArray, imp0, imp1);
 		plotDetectedDots(obj4Dch0, imp0, Color.yellow);
 		plotDetectedDots(obj4Dch1, imp1, Color.red);
 		
 		
 		return true; 
 	}
 	/** Stores particle parameters (centroid coordinates, moments) in String form derived from 
 	 * particle3D plugin is stored in Object4D array
 	 * @param particles String variable exported from particle3D plugin
 	 * @param obj4dv Vector<Object4D> to store all detected particles
 	 * @param chnum String indicating the name of channel
 	 */
 	void storeParticleInfoInObj4D(String particles, Vector<Object4D> obj4dv, String chnum){
 		String[] lines;
 		String line;
 		String[] frame_number_info;
 		lines = particles.split("\n");
 		int currentframe = 0;
 		int dotID = 1;
 		int dummysize = 1;
 		for (int i = 0; i < lines.length; i++){
 			if (lines[i] == null) break;
 			line = lines[i].trim();
 	        frame_number_info = line.split("\\s+");
 	        //for (int j = 0; j < frame_number_info.length; j++) IJ.log(frame_number_info[j]);	        
 	        int framenum = Integer.parseInt(frame_number_info[0]);
 	        if (framenum != currentframe) {
 	        	dotID = 1;
 	        	currentframe = framenum;
 	        }
	        
 	        
 	        float[] centroid = {0, 0, 0};
 	        //for (int j =0; j < 3; j++) 
 	        centroid[0]= Float.parseFloat(frame_number_info[2]); //xy order is opposite
 	        centroid[1]= Float.parseFloat(frame_number_info[1]);
 	        centroid[2]= Float.parseFloat(frame_number_info[3]);
 	        float m0 = Float.parseFloat(frame_number_info[4]);
 	        float m1 = Float.parseFloat(frame_number_info[5]);
 	        float m2 = Float.parseFloat(frame_number_info[6]);
 	        float m3 = Float.parseFloat(frame_number_info[7]);
 	        float m4 = Float.parseFloat(frame_number_info[8]);
 	        float score = Float.parseFloat(frame_number_info[9]);
 	        	        
 	        Object4D obj4d = new Object4D(dummysize, framenum, chnum, dotID, centroid, m0, m1, m2, m3, m4, score);
 	        obj4dv.add(obj4d);
	        dotID++;
 		}
 		sortbyScore(obj4dv);
         /* go over all lines, count number of particles and save the information as String */
  /*       while (true) {
             line = r.readLine();		            
             if (line == null) break;
             line = line.trim();
 			if (line.startsWith("%"))	line = line.substring(1);
 			line = line.trim();
 			particles_info.addElement(line.split("\\s+"));
 			this.particles_number++;
         }
 */	}
 	
 	void sortbyScore(Vector<Object4D> obj4dv){
 		
 		int currentframe = 0;
 		int counter = 0;
 		Vector<Object4D> obj4dVpertime = new Vector<Object4D>();
 		for (int i = 0; i < obj4dv.size(); i++){
 			if ((i == 0) || (obj4dv.get(i).timepoint != currentframe)){
 				currentframe = obj4dv.get(i).timepoint;
 				for (int j = 0; j < obj4dv.size(); j++){
 					if (obj4dv.get(j).timepoint == currentframe) {
 						obj4dVpertime.add(obj4dv.get(j));
 					}
 				}
 				Collections.sort(obj4dVpertime,  new ComparerByscore4D(ComparerByscore4D.DESC));
 				for (int j = 0; j < obj4dVpertime.size(); j++){
 					obj4dv.setElementAt(obj4dVpertime.get(j), counter);
 					//IJ.log(Integer.toString(obj4dVpertime.get(j).dotID));
 					counter++;
 				}
 				obj4dVpertime.clear();
 			}
 		}	
 	}
 	
 	// to print out linked dots and infromation in log window. 
 	void linkresultsPrinter(Object4D[][] linkedArray){
 		 for (int j = 0; j < linkedArray.length; j++){
 			 IJ.log("tframe = "+Integer.toString(j));
 			 for (int i = 0; i< linkedArray[0].length; i++){
 				 if (linkedArray[j][i] == null){
 					 IJ.log("...");					 
 				 } else {
 				 IJ.log("... ID = " + Integer.toString(linkedArray[j][i].dotID)
 						 + " ... " +  linkedArray[j][i].chnum
 						 + " ...Volume = " + Integer.toString(linkedArray[j][i].size));
 				 }
 			 }
 		 }
 	}
 	// not used anymore
 	public void drawlinks(Object4D[][] linked, ImagePlus imp){
 		IJ.run("Colors...", "foreground=white background=white selection=yellow");
 		for(int i = 0;  i < linked.length; i++) {
 			for(int j = 0;  j < linked[0].length; j += 2) {
 				if (linked[i][j] != null){
 					imp.setSlice(linked[i][j].timepoint + 1);
 					imp.setRoi(new Line(linked[i][j].centroid[0], linked[i][j].centroid[1], linked[i][j+1].centroid[0], linked[i][j+1].centroid[1]));
 				}	IJ.run(imp, "Draw", "slice");
 			}
 		}
 		imp.updateAndDraw();
 	}
 
 	//plotting linked lines, but with original grayscale image (will be converted to RGB).
 	public void drawlinksGrayscale(Object4D[][] linked, ImagePlus imp0, ImagePlus imp1){
 		ImagePlus ch0proj = null;
 		ImagePlus ch1proj = null;
 		ch0proj = createZprojTimeSeries(imp0, imp0.getNSlices(), imp0.getNFrames());
 		ch1proj = createZprojTimeSeries(imp1, imp1.getNSlices(), imp1.getNFrames());
 		new StackConverter(ch0proj).convertToRGB();
 		new StackConverter(ch1proj).convertToRGB();
 		
 		int offset = 0;
 		int ch0x, ch0y, ch1x, ch1y;
 		for(int i = 0;  i < linked.length; i++) {
 			for(int j = 0;  j < linked[0].length; j += 2) {
 				if (linked[i][j] != null){
 					ch0x = Math.round(linked[i][j].centroid[0] - offset);
 					ch0y = Math.round(linked[i][j].centroid[1] - offset);
 					ch1x = Math.round(linked[i][j + 1].centroid[0] - offset);
 					ch1y = Math.round(linked[i][j + 1].centroid[1] - offset);
 
 					ImageProcessor ip0 = ch0proj.getStack().getProcessor(linked[i][j].timepoint + 1);
 					ip0.setColor(Color.blue);
 					ip0.drawLine(ch0x, ch0y, ch1x, ch1y);
 					ip0.setColor(Color.yellow);
 					ip0.drawPixel(ch0x, ch0y);
 					ip0.setColor(Color.red);
 					ip0.drawPixel(ch1x, ch1y);					
 
 					ImageProcessor ip1 = ch1proj.getStack().getProcessor(linked[i][j].timepoint + 1);
 					ip1.setColor(Color.blue);
 					ip1.drawLine(ch0x, ch0y, ch1x, ch1y);
 					ip1.setColor(Color.yellow);
 					ip1.drawPixel(ch0x, ch0y);
 					ip1.setColor(Color.red);
 					ip1.drawPixel(ch1x, ch1y);
 					
 				}	
 			}
 		}
 		ImageStack combined = new StackCombiner().combineHorizontally(ch0proj.getStack(), ch1proj.getStack());
 		ImagePlus combimp = new ImagePlus("DetectedDots", combined);
 		
 		combimp.show();
 	}
 	/* for plotting Object4Ds detected by segmentation. 
 	 * Creates a new RGB 
 	 * imp  grayscale image
 	 */
 	public void plotDetectedDots(Vector<Object4D> obj4dv, ImagePlus imp, Color color){
 		Duplicator dup = new Duplicator();
 		ImagePlus dupimp = dup.run(imp);
 		new StackConverter(dupimp).convertToRGB();
 		float x, y, z;
 		int timepoint;
 		int nSlices = imp.getNSlices();
 		int nFrames = imp.getNFrames();
 		if (nFrames <= 1) return;
 		ImageProcessor ip = null;
 		for (int i = 0; i < obj4dv.size(); i++) {
 			x = obj4dv.get(i).centroid[0];
 			y = obj4dv.get(i).centroid[1];
 			z = obj4dv.get(i).centroid[2];
 			timepoint = obj4dv.get(i).timepoint;
 			ip = dupimp.getStack().getProcessor(timepoint * nSlices + Math.round(z)); //TODO
 			ip.setColor(color);
 			ip.drawPixel(Math.round(x), Math.round(y));
 		}
 		dupimp.show();
 		
 	}
 
 
 	public ImagePlus createZprojTimeSeries(ImagePlus imp, int zframes, int tframes){
 		ImageStack zprostack = new ImageStack();
 		zprostack = imp.createEmptyStack();
 		ZProjector zpimp = new ZProjector(imp);
 		zpimp.setMethod(1); //1 is max intensity projection	
 		for (int i=0; i<tframes;i++){
 			zpimp.setStartSlice(i*zframes+1);
 			zpimp.setStopSlice((i+1)*zframes);
 			zpimp.doProjection();
 			zprostack.addSlice("t="+Integer.toString(i+1), zpimp.getProjection().getProcessor());
 		}
 		ImagePlus projimp = new ImagePlus("proj" + imp.getTitle(), zprostack);
 		//projimp.setStack(zprostack);
 		
 		return projimp;				
 	}
 	
 	// processes each time point separated. 
 	// this part could have two options, whether to use trainable segmentation or simple threshold. 
 	// "particletracker3D" could also be implemented (segmentation part).
 	public ImagePlus segmentaitonByObjectSize(ImagePlus imp){
 
 		Duplicator bin = new Duplicator();	//this duplication may not be necessary
 		ImagePlus binimp = bin.run(imp);		
 		int nSlices = imp.getImageStackSize();
 		int zframes = imp.getNSlices();
 		int tframes = nSlices/zframes;
 		double minth =0.0;
 		int adjth =0;
 		Duplicator dup = new Duplicator();	//this duplication may not be necessary
 		ImagePlus impcopy = null;
 		int maxth = (int) Math.pow(2,imp.getBitDepth());
 		for(int i =0; i<tframes; i++){
 			impcopy = dup.run(imp, (i*zframes+1), (i+1)*zframes);
 			minth = initializeThresholdLevel(impcopy);
 			IJ.log(Integer.toString(i)+": initial threshold set to "+Double.toString(minth));
 			adjth = (int) ThresholdAdjusterBy3Dobj(imp, (int)minth, this.thadj_volmin, this.thadj_volmax, this.thadj_nummin, this.thadj_nummax);
 			IJ.log("... ... Adjusted to "+Integer.toString(adjth));
 			
 			for (int j=0; j<zframes; j++)
 				binimp.getStack().getProcessor(i*zframes+1+j).threshold(adjth);
 		}	
 		return binimp;
 	}
 	
 	// this basically initializes the threshold value using Shanbhag autothreshold
 	public int initializeThresholdLevel(ImagePlus imp){
 		ZProjector zpimp = new ZProjector(imp);
 		zpimp.setMethod(1); //1 is max intensity projection
 			//setStartSlice(int slice);
 			//setStopSlice(int slice);
 		zpimp.doProjection();
 			//zpimp.getProjection().show();
 			//IJ.setAutoThreshold(zpimp.getProjection(), "Shanbhag dark");
 		//IJ.setAutoThreshold(zpimp.getProjection(), "Minimum dark");
 		//double minth = zpimp.getProjection().getProcessor().getMinThreshold();
 		int[] hist = zpimp.getProjection().getProcessor().getHistogram();	//simpler strategy
 		int sumpixels =0;
 		int i = hist.length-1;
 		while (sumpixels < 25){
 			sumpixels += hist[i--];
 		}
 		return i;
 	}
 	
 	public double ThresholdAdjusterBy3Dobj(ImagePlus imp, 
 			int initTh, 
 			int thadj_volmin, 
 			int thadj_volmax, 
 			int thadj_nummin,
 			int thadj_nummax){
 		// somehow this part causes error in osx
 		//TODO implement initial setting more directly
 //		IJ.run("3D OC Options", "volume surface nb_of_obj._voxels nb_of_surf._voxels " +
 //				"integrated_density mean_gray_value std_dev_gray_value median_gray_value " +
 //				"minimum_gray_value maximum_gray_value centroid mean_distance_to_surface " +
 //				"std_dev_distance_to_surface median_distance_to_surface centre_of_mass " +
 //				"bounding_box dots_size=5 font_size=10 redirect_to=none");
 		int localthres =0;
 		Duplicator dup = new Duplicator();	//this duplication may not be necessary
 		ImagePlus impcopy = dup.run(imp);
 		// check initial condition
 		excludeOnEdges = false;
 		redirect = false; 
 		Counter3D OC = new Counter3D(impcopy, initTh, minspotvoxels, (int) maxspotvoxels*2, excludeOnEdges, redirect);
 		Vector<Object3D> obj = OC.getObjectsList();
 		int nobj = obj.size();
 		int volumesum=0; 
 		for (int i=0; i<nobj; i++){
 			 Object3D currObj=obj.get(i);
 			 volumesum += currObj.size;
 		}
 		IJ.log("Threshold Adjuster initial th: "+ Integer.toString(initTh) +" ObjNum: "+Integer.toString(nobj)+"Volume Sum: "+Integer.toString(volumesum));
 		localthres = initTh;
 		int loopcount =0;
 //		while ( (((nobj != 2) && (nobj !=4))|| ((volumesum > maxspotvoxels*nobj) 
 //				|| (volumesum < minspotvoxels*nobj))) && (loopcount>maxloops)) {
 		while ( 
 				(nobj < thadj_nummin || nobj > thadj_nummax || volumesum > thadj_volmax || volumesum <thadj_volmin) 
 				&& 
 				(loopcount<maxloops)
 				) {
 
 			if ((nobj<thadj_nummin) && (volumesum < thadj_volmin)) localthres--;
 			if ((nobj<thadj_nummin) && (volumesum > thadj_volmax)) localthres++;			
 			if ((nobj>thadj_nummax) && (volumesum > thadj_volmax)) localthres--;
 			if ((nobj>thadj_nummax) && (volumesum < thadj_volmin)) localthres++;
 			if ((nobj >= thadj_nummin) && (nobj <= thadj_nummax)){
 				if (volumesum < thadj_volmin) localthres--;
 				else localthres++;
 			}
 			// this part is a bit not clear
 			if ((volumesum >= thadj_volmin) && (volumesum <= thadj_volmax)){
 				if (nobj < thadj_nummin) localthres++;
 				else localthres--;
 			}			
 			
 			OC = new Counter3D(impcopy, localthres, minspotvoxels, (int) (maxspotvoxels*1.5), excludeOnEdges, redirect);
 			obj = OC.getObjectsList();
 			nobj = obj.size();
 			volumesum=0;
 			for (int i=0; i<nobj; i++){
 				 Object3D currObj=obj.get(i);
 				 volumesum += currObj.size;
 			}
 			loopcount++;
 		}
 		if (loopcount>0) IJ.log("... New Th="+ Integer.toString(localthres)+" Iter="+Integer.toString(loopcount)+ " ObjNo:"+Integer.toString(nobj)+"Volume Sum:"+Integer.toString(volumesum));
 		
 		return localthres;
 	}
 	
 	public int measureDots(ImagePlus imp, String chnum, 
 			Vector<Object4D> obj4dv, int zframes) {
 		
 		int nSlices = imp.getStackSize();
 		if (nSlices ==1) return -1;
 		//int zframes =8; // TODO
 		int tframes = nSlices/zframes;
 	
 		//IJ.log(Integer.toString(imp.getHeight()));
 		ImagePlus imps = null;
 		Duplicator singletime = new Duplicator();
 		
 		thr = 128;
 		minSize = minspotvoxels_measure;
 		maxSize = 1000;
 		excludeOnEdges = false;
 		redirect = false;
 		
 		for (int j=0; j<tframes; j++){
 			//coords.clear();
 			//vols.clear();
 			//IJ.log("====frame "+Integer.toString(j)+" ==========");
 			imps = singletime.run(imp, j*zframes+1, j*zframes+zframes); 
 			Counter3D OC=new Counter3D(imps, thr, minSize, maxSize, excludeOnEdges, redirect);
 			newRT = true;
 			 //OC.showStatistics(newRT);
 			 //if (!Counter3D.getObjects) Counter3DgetObjects();
 
 			 Vector<Object3D> obj = OC.getObjectsList();
 			 int nobj = obj.size();
 			 //IJ.log(Integer.toString(nobj));
 			 //sort obj in size-descending order. 
 			 Collections.sort(obj,  new ComparerBysize3D(ComparerBysize3D.DESC));
 			 for (int i=0; i<nobj; i++){			 
 				 Object3D cObj=obj.get(i);
 				 //IJ.log(LogObject3D(cObj, i));
 				 obj4d = new Object4D(cObj.size);
 				 obj4d.CopyObj3Dto4D(cObj, j, chnum, i+1); //adds additional 4d parameters, timepoint, channel & dotID 
 				 obj4dv.add(obj4d);
 			 }
 			 
 		} 
 		
 		return obj4dv.size();
 	}
 	
 	String LogObject3D(Object3D cObj, int i){
 		 String opt ="";
 		 String Cent ="";
 		 Cent = "("
 			 +Float.toString(cObj.centroid[0])+","
 		 	 +Float.toString(cObj.centroid[1])+","
 		 	 +Float.toString(cObj.centroid[2])
 		 	 +")";
 		 opt = "Object"+Integer.toString(i)
 		 	+" vol="+Integer.toString(cObj.size) 
 		 	+ "\t "+Cent
 		 	+" : IntDen"+Float.toString(cObj.int_dens);
 		 return opt;
 	}
		/**Show content of Object4D vector in Results window. 
		 * 
		 * @param obj4Dv Vector<Object4D>
		 */
 	   public void showStatistics(Vector<Object4D> obj4Dv){
 	        ResultsTable rt;        
 	        rt=new ResultsTable();	        
 	        for (int i=0; i<obj4Dv.size(); i++){
 	            if (obj4Dv.get(i).centroid.length > 1){
 	            	rt.incrementCounter();
 	            	rt.setValue("frame", i, obj4Dv.get(i).timepoint);
	            	rt.setValue("dotID", i, obj4Dv.get(i).dotID);
 	            	rt.setValue("Volume", i, obj4Dv.get(i).size);
 	            	rt.setValue("x", i, obj4Dv.get(i).centroid[0]);
 		            rt.setValue("y", i, obj4Dv.get(i).centroid[1]);
 		            rt.setValue("z", i, obj4Dv.get(i).centroid[2]);
 		            rt.setValue("Intden", i, obj4Dv.get(i).int_dens);
 	            }
 	        }
 	       
 	        rt.show("Statistics_"+obj4Dv.get(0).chnum);     
 	    }
 	   public void showDistances(Object4D[][] linked){
 	        ResultsTable rt;        
 	        rt=new ResultsTable();
 	        int ct = 0;
 	        double ch0ch1dist = -1;
 	        for (int i=0; i<linked.length; i++){
 	        	for (int j = 0; j < linked[0].length; j+=2){
 		        	if ((linked[i][j] != null) && (linked[i][j+1] != null)){
 		        		rt.incrementCounter();
 		        		ch0ch1dist = returnDistance(linked[i][j], linked[i][j+1]);
 		        		rt.setValue("frame", ct, linked[i][j].timepoint);
 			            rt.setValue("ch0-ch1_dist", ct, ch0ch1dist);
 			            double ch0dist = 0;
 			            double ch1dist = 0;
 						if (linked[i][3] != null) {
 			            	ch0dist = returnDistance(linked[i][0], linked[i][2]);
 							ch1dist = returnDistance(linked[i][1], linked[i][3]);
 						}
 			            rt.setValue("ch0-ch0_dist", ct, ch0dist);
 			            rt.setValue("ch1-ch1_dist", ct, ch1dist);
 			            rt.setValue("ch0vol", ct, linked[i][j].size);
 			            rt.setValue("ch1vol", ct, linked[i][j+1].size);
 			            
 			            ct++;
 		        	}
 	        	}
 	        }
 	       
 	        rt.show("Statistics_Distance");     
 	    }
 	   //for calculating distance from index
 	   public double returnDistance(Object4D obj1, Object4D obj2){
 		   double dist = -1.0;
 		   if ((obj1.centroid.length > 1) && (obj2.centroid.length > 1)) {
 			   double sqd = (
 					   Math.pow(obj1.centroid[0] - obj2.centroid[0], 2) 
 					   + Math.pow(obj1.centroid[1] - obj2.centroid[1], 2) 
 					   + Math.pow((obj1.centroid[2] - obj2.centroid[2])*zfactor, 2)
 					);
 			   dist = Math.pow(sqd, 0.5);
 		   }	
 		   return dist;
 		}
 
 	   // returns number of dots at single time point
 	   int returnDotNumber(Vector<Object4D> obj4D, int timepoint){
 		   int counter =0;
 		   for (int i=0; i<obj4D.size(); i++){
 			   if (obj4D.get(i).timepoint == timepoint) counter++;
 		   }
 		   return counter;
 	   }
 	   // dotID could only be 1 or 2 (0 does not exist)
 	   Object4D returnObj4D(Vector<Object4D> obj4Dv, int tpoint, int dotID){
 		   Object4D retobj4D = null;
 		   for (int i=0; i<obj4Dv.size(); i++){
 			   if ((obj4Dv.get(i).timepoint == tpoint) 
 				  && (obj4Dv.get(i).dotID == dotID)){
 				  
 				   retobj4D = obj4Dv.get(i);
 			   }
 		   }		   
 		   return retobj4D;
 	   }
 	   
 	   //since there is only one dot in a channel, there could be only one link, with three cases
 	   int compare2x1(int tpoint){
 		   int flag = 0;
 		   int ch0dots = returnDotNumber(obj4Dch0, tpoint);
 		   int ch1dots = returnDotNumber(obj4Dch1, tpoint);
 		   Object4D ch0id1  = returnObj4D(obj4Dch0, tpoint, 1);
 		   Object4D ch1id1  = returnObj4D(obj4Dch1, tpoint, 1);
 		   if (ch0dots == 1) {
 			   Object4D ch1id2  = returnObj4D(obj4Dch1, tpoint, 2);
 			   double dist1 =returnDistance(ch0id1, ch1id1);
 			   double dist2 =returnDistance(ch0id1, ch1id2);
 			   if (dist1 < dist2) flag = 1;
 			   else flag = 2;
 		   } else {
 			   Object4D ch0id2  = returnObj4D(obj4Dch0, tpoint, 2);
 			   double dist1 =returnDistance(ch0id1, ch1id1);
 			   double dist2 =returnDistance(ch0id2, ch1id1);
 			   if (dist1 < dist2) flag = 1;
 			   else flag = 3;			   
 		   }
 		   return flag;
 	   }
 	   
 	   //only two cases  of combinations
 	   int compare2x2(int tpoint){
 		   int flag =0;
 		   Object4D ch0id1  = returnObj4D(obj4Dch0, tpoint, 1);
 		   Object4D ch0id2  = returnObj4D(obj4Dch0, tpoint, 2);
 		   Object4D ch1id1  = returnObj4D(obj4Dch1, tpoint, 1);
 		   Object4D ch1id2  = returnObj4D(obj4Dch1, tpoint, 2);
 		   double dist1 = returnDistance(ch0id1, ch1id1) + returnDistance(ch0id2, ch1id2);
 		   double dist2 = returnDistance(ch0id1, ch1id2) + returnDistance(ch0id2, ch1id1);
 		   if (dist1 < dist2) flag = 1;
 		   else flag = 2;		   
 		   return flag;
 	   }
 	   /* Link objects in two channels
 	    * 
 	    * assumes that there is only one or two pairs.
 	    * Picks up largest and/or nearest particle first.  
 	    * TODO in one case, dots in different daughter cells were linked. This should be avoided. 
 	    */
 	   Object4D[][] dotLinker(Vector<Object4D> obj4Dch0,  Vector<Object4D> obj4Dch1, int tframes){
 		   Object4D[][] linked = new Object4D[tframes][4]; 
 		   Object4D obj4Dch0id1, obj4Dch1id1;
 		   Object4D obj4Dch0id2, obj4Dch1id2;		   
 		   int flag = 0;
 		   
 		   for (int i = 0; i < tframes; i++){
 			   obj4Dch0id1 = returnObj4D(obj4Dch0, i, 1);	
 			   obj4Dch1id1 = returnObj4D(obj4Dch1, i, 1);
 			   obj4Dch0id2 = returnObj4D(obj4Dch0, i, 2);	
 			   obj4Dch1id2 = returnObj4D(obj4Dch1, i, 2);
 			   if ((obj4Dch0id1 != null) && (obj4Dch1id1 != null)) {
 				   
 				   // 1x1 case
 				   if ((obj4Dch0id2 == null) && (obj4Dch1id2 == null)) { 
 					   linked[i][0] = obj4Dch0id1;
 					   linked[i][1] = obj4Dch1id1;				   
 				   } else {
 					   if ((obj4Dch0id2 != null) && (obj4Dch1id2 != null)) { // 2x2 both channels contain multiple dots
 						   flag = compare2x2(i);
 						   if (flag == 1) {
 							   linked[i][0] = obj4Dch0id1;
 							   linked[i][1] = obj4Dch1id1;
 							   linked[i][2] = obj4Dch0id2;
 							   linked[i][3] = obj4Dch1id2;							   
 						   } else {
 							   linked[i][0] = obj4Dch0id1;
 							   linked[i][1] = obj4Dch1id2;
 							   linked[i][2] = obj4Dch0id2;
 							   linked[i][3] = obj4Dch1id1;
 						   }
 					   } else {	//2x1 one channel contains only one dots
 						   flag = compare2x1(i);
 						   if (flag == 1) {
 							   linked[i][0] = obj4Dch0id1;
 							   linked[i][1] = obj4Dch1id1;
 						   } else {
 							   if (flag == 2){
 								   linked[i][0] = obj4Dch0id1;
 								   linked[i][1] = obj4Dch1id2;
 							   } else {
 								   linked[i][0] = obj4Dch0id2;
 								   linked[i][1] = obj4Dch1id1;
 							   }
 						   }
 					   }
 				   }
 			   }
 		   }
 		   return linked;
 	   }
 	   
 
 }
 
 //for sorting Object3D Vector, descending order by size (volume)
 class ComparerBysize3D implements Comparator<Object3D> {
 	public static final int ASC = 1;
 	public static final int DESC = -1;
 	private int sort = ASC;
 	
 	public ComparerBysize3D(){
 	}
 	public ComparerBysize3D(int sort){
 		this.sort = sort;
 	}	
     public int compare(Object3D o1, Object3D o2) {
     	
         Object3D obj3d1 = (Object3D) o1;
         Object3D obj3d2 = (Object3D) o2;
         int i = 0;
         if (obj3d1.size < obj3d2.size) 
             i = -1*sort;
         if (obj3d1.size == obj3d2.size)
             i = 0;
         if (obj3d1.size > obj3d2.size)
             i = 1*sort;
         return i;
     }
 }
 
 //for sorting Object4D, descending order by score (of none-particle criteria)
 class ComparerByscore4D implements Comparator<Object4D> {
 	public static final int ASC = 1;
 	public static final int DESC = -1;
 	private int sort = ASC;
 	
 	public ComparerByscore4D(){
 	}
 	
 	public ComparerByscore4D(int sort){
 		this.sort = sort;
 	}	
 	
     public int compare(Object4D o1, Object4D o2) {
         Object4D obj4d1 = (Object4D) o1;
         Object4D obj4d2 = (Object4D) o2;
         int i = 0;
         if (obj4d1.score < obj4d2.score) 
             i = -1 * sort;
         if (obj4d1.score == obj4d2.score)
             i = 0;
         if (obj4d1.score > obj4d2.score)
             i = 1 * sort;
         return i;
     }
 }
 
 
 
 /*
  * macro "connect dots in different channels"{
 	LinkerCore();	
 }
 
 function LinkerCore(){
 	if (nResults==0) exit("not measured??");
 	for(i=0; i<connectArray.length; i++) connectArray[i] =-1;
 	 DotLinker(tframes);
 	setColor(255, 255, 255);
 	for (i=0; i<tframes; i++){
 		//setSlice(i+1);
 		pstr = "time:" + i  + "\n"
 			+ "   ch0: "	+ connectArray[i*14 ]
 				+", "	+ connectArray[i*14 +1] 
  				+", "	+ connectArray[i*14 +2]
 			+ "\n   ch1: "	+ connectArray[i*14 +3]
 				+", "	+ connectArray[i*14 +3 +1] 
  				+", "	+ connectArray[i*14 +3 +2];
 		//drawLine(connectArray[i*14], connectArray[i*14+1], connectArray[i*14+3], connectArray[i*14+4]);
 		if (connectArray[i*14 +7] !=-1) {
 			pstr = pstr + "\n   ch0: "+   connectArray[i*14 +7]
 				+", "+   connectArray[i*14 +7+1]
 				+", "+   connectArray[i*14 +7+2]
 				+ "\n   ch1: "+   connectArray[i*14 +7+3]
 				+", "+   connectArray[i*14 +7+3+1]
 				+", "+   connectArray[i*14 +7+3+2];
 				//drawLine(connectArray[i*14+7], connectArray[i*14+7+1], connectArray[i*14+7+3], connectArray[i*14+7+4]);
 
 		}
 		print(pstr);
  
 	}
 }
 
 function DotLinker(tframes){
 	for (i=0; i<tframes; i++) {
 		ch0dots = returnDotNumber(0, i);
 		ch1dots = returnDotNumber(1, i);
 		//print(i +":  Ch0 dots:", ch0dots, "- Ch1 dots:", ch1dots);
 		
 		if ((ch0dots != 0) && (ch1dots != 0)) {
 
 			if ((ch0dots == 1) && (ch1dots == 1)) {
 				StoreCoordinates(connectArray, i, 0, 0, 0);
 				StoreCoordinates(connectArray, i, 1, 0, 0);
 			} else {
 				if ((ch0dots >= 2) && (ch1dots >= 2)) {
 					flag = compare2x2(i);
 					if (flag ==1) {
 						StoreCoordinates(connectArray, i, 0, 0, 0);
 						StoreCoordinates(connectArray, i, 1, 0, 0);
 						StoreCoordinates(connectArray, i, 0, 1, 1);
 						StoreCoordinates(connectArray, i, 1, 1, 1);
 					} else {
 						StoreCoordinates(connectArray, i, 0, 0, 0);
 						StoreCoordinates(connectArray, i, 1, 0, 1);
 						StoreCoordinates(connectArray, i, 0, 1, 1);
 						StoreCoordinates(connectArray, i, 1, 1, 0);
 					}
 				} else {		//either one of them is only one. 
 					flag = compare2x1(i);
 					if (flag ==1) {
 						StoreCoordinates(connectArray, i, 0, 0, 0);
 						StoreCoordinates(connectArray, i, 1, 0, 0);
 					} else {
 						if (flag ==2) {
 							StoreCoordinates(connectArray, i, 0, 0, 0);
 							StoreCoordinates(connectArray, i, 1, 1, 0);
 						} else {
 							StoreCoordinates(connectArray, i, 0, 1, 0);
 							StoreCoordinates(connectArray, i, 1, 0, 0);
 						}
 					}
 				}
 			}
 		}
 
 	}
 }
 
 function StoreCoordinates(sA, timepoints, chrchannel, dotID, offset){
 	key = timepoints * 14;
 	index =  returnDotIndex(chrchannel, timepoints, dotID);
 	sA[key+ offset*7 +chrchannel*3 ] =  getResult("x", index);
 	sA[key+ offset*7+chrchannel*3+1] =  getResult("y", index);
 	sA[key+ offset*7+chrchannel*3+2] =  getResult("z", index);
 }
 
 function compare2x2(timepoint){
 	ch0id0 = returnDotIndex(0, timepoint, 0);
 	ch0id1 = returnDotIndex(0, timepoint, 1);
 	ch1id0 = returnDotIndex(1, timepoint, 0);
 	ch1id1 = returnDotIndex(1, timepoint, 1);
 	combi01 = returnDistance(ch0id0,  ch1id0) + returnDistance(ch0id1,  ch1id1);
 	combi02 = returnDistance(ch0id0,  ch1id1) + returnDistance(ch0id1,  ch1id0);
 	flag =0;
 	if (combi01 < combi02 ) flag =1;
 	else flag =2;
 	return flag;
 }
 
 function compare2x1(timepoint){
 	ch0dots = returnDotNumber(0, timepoint);
 	ch1dots = returnDotNumber(1, timepoint);
 	ch0id0 = returnDotIndex(0, timepoint, 0);
 	ch1id0 = returnDotIndex(1, timepoint, 0);
 	flag =0;
 	if (ch0dots ==1) {
 		ch1id1 = returnDotIndex(1, timepoint, 1);
 		combi01 = returnDistance(ch0id0,  ch1id0) ;
 		combi02 = returnDistance(ch0id0,  ch1id1) ;
 		if (combi01<combi02) flag= 1;
 		else flag = 2;
 	} else {
 		ch0id1 = returnDotIndex(0, timepoint, 1);
 		combi01 = returnDistance(ch0id0,  ch1id0) ;
 		combi02 = returnDistance(ch0id1,  ch1id0) ;
 		if (combi01<combi02) flag= 1;
 		else flag = 3;
 	}
 	return flag;
 }
 */
 
 /*
  * 
  * 
  * 
  * 
  */
 
