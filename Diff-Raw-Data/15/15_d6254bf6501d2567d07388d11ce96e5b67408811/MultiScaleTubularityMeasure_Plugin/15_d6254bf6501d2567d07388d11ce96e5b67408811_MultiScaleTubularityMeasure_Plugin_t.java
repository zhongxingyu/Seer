 package FijiITKInterface;
 
 import ij.IJ;
 import ij.plugin.PlugIn;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.process.ByteProcessor;
 import ij.process.FloatProcessor;
 import ij.io.FileInfo;
 import ij.gui.GenericDialog;
 import ij.gui.ImageCanvas;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 
 import java.lang.Math;
 
 import com.sun.j3d.utils.applet.MainFrame;
 import com.sun.j3d.utils.universe.*;
 import com.sun.j3d.utils.geometry.*;
 import javax.media.j3d.*;
 import javax.vecmath.*;
 import javax.media.j3d.ColoringAttributes;
 
 import ij.measure.Calibration;
 
 import ij3d.*;
 
 public class MultiScaleTubularityMeasure_Plugin implements MouseListener, MouseMotionListener, PlugIn {
 
     MultiScaleTubularityMeasure ti;
     final static boolean MULTICOLORED = true;
     JPanel mainPane;
     ImagePlus imagePlus;
 
     int Slice1, Slice2, NSlices, Nscales, imageType, width, height;
     double maximumScale, minimumScale;
     float PxlDth;
     boolean position_checked = false, showFilteredImages;
     boolean ROIpt_1_2 = true;
     Point p, ROI_p1, ROI_p2;
 
     float [] StackPixelDataOut;
     byte [] StackpixelData;
     Calibration Calib;
 
     Image3DUniverse univ;
 
 
 
 
     public MultiScaleTubularityMeasure_Plugin() {
         ti = new MultiScaleTubularityMeasure();
     }
 
     public void run( String ignored ) {
 
 	///////initialisation////////////
     	imagePlus = IJ.getImage();
 	ImageStack stack = imagePlus.getImageStack();
 	ImageCanvas imagecanvas = imagePlus.getCanvas();
 
 	ROIpt_1_2 = true;
 	ROI_p1 = new Point(); ROI_p2 = new Point();
 	ROI_p1.x = 0; ROI_p1.y = 0; ROI_p2.x = imagePlus.getWidth(); ROI_p2.y = imagePlus.getHeight();
 	
     	if( imagePlus == null ) {
        	 IJ.error("No image was open...");
        	 return;
    	 }
 
 	//TODO: possible types are : GRAY8 GRAY16 and GRAY32.
     	imageType = imagePlus.getType();
     	if( ! (imageType == ImagePlus.GRAY8 || imageType == ImagePlus.COLOR_256) ) {
        	 IJ.error("Not an 8-bit image");
        	 return;
    	}
 
    	ByteProcessor bp = (ByteProcessor)imagePlus.getProcessor();
     	byte [] pixelData = (byte [])bp.getPixels();
 
 	imagecanvas.addMouseListener(this);
    	imagecanvas.addMouseMotionListener(this);
 
 	FileInfo Info = imagePlus.getOriginalFileInfo();
 	Calib = imagePlus.getCalibration();
 
 	NSlices = stack.getSize(); width = imagePlus.getWidth(); height = imagePlus.getHeight();
 	PxlDth = (float)Calib.pixelDepth; 
 	
 	ByteProcessor bpStack;
     	byte [] SlicepixelData = new byte[width*height];
 	StackpixelData = new byte[width*height*NSlices];
 
 	///////////////////////////////////
 
 
 	//Fill intput stack
 	for(int i=0;i<NSlices;i++){
 		bpStack = (ByteProcessor)stack.getProcessor(i+1);
 		SlicepixelData = (byte [])bpStack.getPixels();
 		for(int j=0;j<width*height;j++)
 			StackpixelData[width*height*i + j] = SlicepixelData[j];
 	}
 
 	/////////////IDE//////////////////////
 	GenericDialog gd = new GenericDialog("Optimally Oriented Flux Options");
 
 	double pixelWidth = Math.abs(Calib.pixelWidth);
 
     	gd.addNumericField("Number of scales", 1, 0);
 	gd.addNumericField("Minimum scale", pixelWidth, 6);
  	gd.addNumericField("Maximum scale", pixelWidth, 6);
 	gd.addCheckbox("Show filtered images at each scale:",false);
 	
 	gd.showDialog();	
 		
 	if( gd.wasCanceled() ) return;
 
  	Nscales = (int)Math.round(gd.getNextNumber());
 
 	if( Nscales < 1 ) {
 		IJ.error("The minimum number of scales to try is 1");
 		return;
         }
 
     	minimumScale = gd.getNextNumber();
         maximumScale = gd.getNextNumber();
 
         if( maximumScale < minimumScale ) {
 		 IJ.error("The maximum scale cannot be less than the minimum scale");
 			return;
 	}
     
 	showFilteredImages = gd.getNextBoolean();
 	///////////////////////////////////////
 
 
 	//Create output image (need to know number of scales before)
 	StackPixelDataOut = new float[Nscales*width*height*NSlices];
 
 	// Create a universe and show it
 	univ = new Image3DUniverse();
 	univ.getViewingPlatform().setNominalViewingTransform();
 	univ.show();
 	 
 	// Add the image as an isosurface
 	Content c = univ.addVoltex(imagePlus);
 	c.setLocked(true);
 
     }
 
 	public void mouseClicked(MouseEvent me) {
   	}
 
   	public void mouseEntered(MouseEvent me) {
   	}
 
   	public void mouseExited(MouseEvent me) {
   	}
 
   	public void mousePressed(MouseEvent me) {
    	 	p = me.getPoint();
 
 		if(ROIpt_1_2){
 	 		Slice1 = imagePlus.getCurrentSlice();
 			ROI_p1.x = p.x; ROI_p1.y = p.y;
 			ROIpt_1_2  = false;
 	 		IJ.error("Point1: " + ROI_p1.x + "," + ROI_p1.y + " Slice :" + Slice1);
 		}else{
 			Slice2 = imagePlus.getCurrentSlice();
 			ROI_p2.x = p.x; ROI_p2.y = p.y;
 			ROIpt_1_2  = true;
 			position_checked = true;
 			IJ.error("Point2: " + ROI_p2.x + "," + ROI_p2.y + " Slice :" + Slice2);
 		}
 
 		if(position_checked){
 
 			float [] pt1 = {ROI_p1.x, ROI_p1.y, Slice1};
 			float [] pt2 = {ROI_p2.x, ROI_p2.y, Slice2}; 
 
 			ti.OrientedFlux(StackpixelData, StackPixelDataOut, imageType, width, height, NSlices, Calib.pixelWidth, Calib.pixelHeight, Calib.pixelDepth, minimumScale, maximumScale, Nscales, pt1, pt2);
 
 			//Print Output image(s)
 			int offset = 0;
 			int w = width; int h = height;
 
 			ImagePlus result_image = new ImagePlus();
 			ImageStack res_stack = new ImageStack(width,height);
 
 			for(int i=0;i<NSlices;i++){
 				float[] pix = new float[w*h]; // get your bytes somehow
 				for(int j=0;j<w*h;j++){
 					pix[j] = StackPixelDataOut[offset + w*h*i + j];
 				}
 				FloatProcessor proc = new FloatProcessor(w, h, pix, null);
 				res_stack.addSlice("", proc);
 			}	
 
 			result_image.setStack(res_stack);
    			result_image.show();
 
 			//Display scale Images if users said
 			int scales;if(showFilteredImages) scales = Nscales; else scales = 1;
 			for(int k=1;k<scales;k++){
 				if(k>0){offset += w*h*NSlices;}
 				ImageStack newstack = new ImageStack(w,h);
 				for(int i=0;i<NSlices;i++){
 					float[] pix = new float[w*h];
 					for(int j=0;j<w*h;j++){
 						pix[j] = StackPixelDataOut[offset + w*h*i + j];
 					}
 					FloatProcessor proc = new FloatProcessor(w, h, pix, null);
 					newstack.addSlice("", proc);
 				}	
 				String nameout = "Tubularity" + k;
 				ImagePlus imp = new ImagePlus(nameout, newstack);
 				imp.show();
 			}
 
 	
 			////////Display all path using cylinders///////
 			float radius1, radius2;int ind =0;
 			float [] Path = new float[w*h*NSlices];
 			int size = ti.GetPath(Path);
 
 			boolean resample = false;
 			Color3f realColor = new Color3f(Color.magenta);
 			ArrayList<Color3f> tubeColors = new ArrayList<Color3f>();
 
 			int numberOfPoints = size / 4;
			double [] x_points = new double[numberOfPoints];
			double [] y_points = new double[numberOfPoints];
			double [] z_points = new double[numberOfPoints];
			double [] radiuses = new double[numberOfPoints];
 
			for (int i = 0; i < numberOfPoints; ++i) {
 				ind = i * 4;
 				x_points[i] = Path[ind];
 				y_points[i] = Path[ind+1];
 				z_points[i] = Path[ind+2];
				radiuses[i] = Path[ind+3];

 			}
 
 			double [][][] allPoints = Pipe.makeTube(x_points, // double[]
 													y_points, // double[]
 													z_points, // double[]
 													radiuses, // double[]
 													resample ? 2 : 1,	   // resample - 1 means just "use mean distance between points", 3 is three times that, etc.
 													12,		 // "parallels" (12 means cross-sections are dodecagons)
 													resample,   // do_resample
 													realColor,
 													null,
 													tubeColors);
 
 			if (allPoints == null) {
 				IJ.error("Generating the 3D surface visualization failed");
 				return;
 			}
 
 			java.util.List<Color3f> vertexColorList = new ArrayList<Color3f>();
 			java.util.List<Point3f> triangles = Pipe.generateTriangles(allPoints,
 																	   1, // scale
 																	   tubeColors,
 																	   vertexColorList);
 
 			Content content3D = univ.addTriangleMesh(triangles,
 													 vertexColorList,
 													 "Example Trace");
 			content3D.setLocked(true);
 		
 			position_checked = false;
 		}
   	}
 
   	public void mouseReleased(MouseEvent me) {
     	p = null;
   	}
 
   	public void mouseDragged(MouseEvent me) {
     	p = me.getPoint();
   	}
 
  	public void mouseMoved(MouseEvent me) {
   	}
 	/////////////////////////////////////////
 }
 
