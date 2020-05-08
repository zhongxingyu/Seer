 package kinect.kinect;
 
 import april.vis.*;
 import april.jmat.*;
 import april.util.*;
 
 import lcm.lcm.*;
 import kinect.lcmtypes.*;
 
 import kinect.classify.*;
 import kinect.classify.FeatureExtractor.FeatureType;
 
 import java.io.*;
 import java.nio.*;
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.awt.image.*;
 
 /* To Do:
  *  - Add features for shape recognition to FeatureVec
  */
 
 
 public class NounjectiveLearning implements LCMSubscriber
 {
     final static int FRAME_WIDTH = 640;
     final static int FRAME_HEIGHT = 480;
 
     static int initialColorThresh = 13;
     static double initialUnionThresh = 0.05;
     static double  initialRansacThresh = .02;
     static double  initialRansacPercent = .1;
 
 
     double intensityThreshold = .12;
     int divider = 320;
 
     static DataAggregator da;
     static Segment segment;
     GetOpt opts;
     VisWorld vw, vw2;
     VisLayer vl, vl2;
     static VisWorld.Buffer vb;
     kinect_status_t ks;
     static LCM lcm = LCM.getSingleton();
     KNN knnColor = new KNN(30, 6, "color_features.dat");
     KNN knnShape = new KNN(10, 15, "shape_features.dat");
     //Classifier classColor = new Classifier();
 
     final static int[] viewBorders = new int[]{75, 150, 575, 400};
     final static Rectangle viewRegion = new Rectangle(viewBorders[0], viewBorders[1], viewBorders[2] - viewBorders[0], viewBorders[3] - viewBorders[1]);
 
     public NounjectiveLearning(GetOpt opts_)
     {
         opts = opts_;
 
         knnColor.loadData(false);
         knnShape.loadData(true);
 
         // Initialize all ov Vis stuff.
         vw = new VisWorld();
         vw2 = new VisWorld();
         vl = new VisLayer(vw);
         vl2 = new VisLayer(vw2);
         double pct = 0.3;
         vl2.layerManager = new DefaultLayerManager(vl2, new double[]{0.0, 1.0-pct, pct, pct});
         vl2.cameraManager.fit2D(new double[2], new double[] {kinect_status_t.WIDTH, kinect_status_t.HEIGHT}, true);
         VisCanvas vc = new VisCanvas(vl);
         vc.addLayer(vl2);
         vb = vw.getBuffer("Point_Buffer");
 
         //Set up initial camera view
         vl.cameraManager.uiLookAt(new double[] {0.0, 0.0, -3.0},// Camera position
                                   new double[] {0.0, 0.0, 0.0},// Point looking at
                                   new double[] {0.0, -1.0, 0.0},// Up
                                   false);
 
         // Set up adjustable parameters, buttons
         ParameterGUI pg = new ParameterGUI();
         pg.addIntSlider("cThresh", "Color Threshold", 1, 100, initialColorThresh);
         pg.addDoubleSlider("uThresh", "Union Threshold", 0.001, 0.305, initialUnionThresh);
         pg.addDoubleSlider("rThresh", "Ransac Threshold", .001, .1, initialRansacThresh);
         pg.addDoubleSlider("rPercent", "Ransac Percent", .01, .3, initialRansacPercent);
         pg.addDoubleSlider("iThresh", "Intensity Threshold", 0.001, 0.999, .12);
         pg.addIntSlider("dSlider", "Divider Threshold", 1, 640, 200);
 
         pg.addListener(new ParameterListener() {
                 public void parameterChanged(ParameterGUI pg, String name) {
                     if (name.equals("cThresh")) {
                         da.colorThresh = pg.gi("cThresh");
                     }
                     else if (name.equals("uThresh")) {
                         da.unionThresh = pg.gd("uThresh");
                     }
                     else if (name.equals("rThresh")) {
                         da.ransacThresh = pg.gd("rThresh");
                     }
                     else if (name.equals("rPercent")) {
                         da.ransacPercent = pg.gd("rPercent");
                     }
                     else if (name.equals("iThresh")) {
                     	intensityThreshold = pg.gd("iThresh");
                     }
                     else if (name.equals("dSlider")) {
                     	divider =  pg.gi("dSlider");
                     }
 
                 }
             });
 
         // JFrame set up
         JFrame frame = new JFrame("Learning Objects");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLayout(new BorderLayout());
         frame.add(vc, BorderLayout.CENTER);
         frame.setSize(2*FRAME_WIDTH, FRAME_HEIGHT);
         vb = vw.getBuffer("object segmenting");
         frame.add(pg, BorderLayout.SOUTH);
         frame.setVisible(true);
 
         //initialize lcm and start receiving data
         LCM myLCM = LCM.getSingleton();
         myLCM.subscribe("KINECT_STATUS", this);
     }
 
 
     /** Upon recieving a message from the Kinect, translate each depth point into
      ** x,y,z space and find the pixel color for it.  Then draw it.
      **/
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
         try {
             ks = new kinect_status_t(ins);
         }catch (IOException e){
             e.printStackTrace();
             return;
         }
 
         segment.points = new ArrayList<double[]>();
         segment.coloredPoints = new ArrayList<double[]>();
 
 //        System.out.println("mat");
 //        for(int i = 0; i < 4; i++){
 //        	System.out.println(KinectCalibrator.v2s(KUtils.kinectToWorldXForm[i]));
 //        }
 
         for(int y= (int)viewRegion.getMinY(); y<viewRegion.getMaxY(); y++){
             for(int x=(int)viewRegion.getMinX(); x<viewRegion.getMaxX(); x++){
                 int i = y*ks.WIDTH + x;
                 int d = ((ks.depth[2*i+1]&0xff) << 8) |
                         (ks.depth[2*i+0]&0xff);
                double[] pKinect = KUtils.getRegisteredXYZRGB(x,y, ks); //KUtils.getXYZRGB(x, y, KUtils.depthLookup[d], ks);
                 double[] pWorld = KUtils.getWorldCoordinates(new double[]{
                         pKinect[0], pKinect[1], pKinect[2]});
 
                 //da.currentPoints.add(new double[]{pWorld[0], pWorld[1], pWorld[2], pKinect[3]});
                 segment.points.add(pKinect);
             }
         }
 
         HashMap<Integer, String> features = sendMessage();
         draw3DImage(features);
         draw2DImage();
     }
 
 
 
     public HashMap<Integer, String> sendMessage()
     {
         observations_t obs = new observations_t();
         obs.utime = TimeUtil.utime();
 
         ArrayList<object_data_t> obsList = new ArrayList<object_data_t>();
         ArrayList<String> sensList = new ArrayList<String>();
 
         // XXX -Temporary - allow us to check how each object is being labeled
         HashMap<Integer, String> features = new HashMap<Integer, String>();
 
         Collection c = segment.objects.values();
         for(Iterator itr = c.iterator(); itr.hasNext(); ){
             ObjectInfo obj = (ObjectInfo)itr.next();
             double[] bb = FeatureExtractor.boundingBox(obj.points);
             double[] min = KUtils.getWorldCoordinates(new double[]{bb[0], bb[1], bb[2]});
             double[] max = KUtils.getWorldCoordinates(new double[]{bb[3], bb[4], bb[5]});
 
 
             double[] xyzrpy = new double[]{0, 0, 0, 0, 0, 0};
             for(int i = 0; i < 3; i++){
             	xyzrpy[i] = (min[i] + max[i])/2;
             }
 
             // Get features and corresponding classifications for this object
             String colorInput = FeatureExtractor.getFeatureString(obj, FeatureType.COLOR);
             String shapeInput = FeatureExtractor.getFeatureString(obj, FeatureType.SHAPE);
 		    //String color = classColor.classify(colorInput);
 		    ConfidenceLabel colorCL = knnColor.classify(colorInput);
 		    String color = colorCL.getLabel(); // Currenty only one being returned
 		    ConfidenceLabel shapeCL = knnShape.classify(shapeInput);
 		    String shape = shapeCL.getLabel();
             categorized_data_t[] data = new categorized_data_t[2];
             categorized_data_t d = new categorized_data_t();
             d.cat = new category_t();
             d.cat.cat = category_t.CAT_COLOR;
             d.label = new String[]{color};
             d.len = 1;
             d.confidence = new double[]{.9};
             data[0] = d;
 
             categorized_data_t dS = new categorized_data_t();
             dS.cat = new category_t();
             dS.cat.cat = category_t.CAT_SHAPE;
             dS.label = new String[]{shape};
             dS.len = 1;
             dS.confidence = new double[]{.9};
             data[1] = dS;
 
             // Create object data for lcm
             object_data_t obj_data = new object_data_t();
             obj_data.utime = TimeUtil.utime();
             obj_data.id = obj.repID;
             obj_data.cat_dat = data;
             obj_data.num_cat = obj_data.cat_dat.length;
             obj_data.pos = xyzrpy;
             obj_data.bbox = new double[][]{min, max};
             obsList.add(obj_data);
 
             // XXX -Temporary - allow us to check how each object is being labeled
             features.put(obj.repID, (color+" "+shape));
         }
 
         obs.click_id = -1;
         obs.sensables = sensList.toArray(new String[0]);
         obs.nsens = obs.sensables.length;
         obs.observations = obsList.toArray(new object_data_t[0]);
         obs.nobs = obs.observations.length;
 
         lcm.publish("OBSERVATIONS",obs);
 
         // XXX -Temporary - allow us to check how each object is being labeled
         return features;
     }
 
 
     public void draw3DImage(HashMap<Integer, String> features)
     {
         if(segment.points.size() <= 0 && segment.coloredPoints.size() <= 0){
             System.out.println("No points found.");
             return;
         }
 
         segment.segmentFrame(segment.points);
 
         VisColorData cd = new VisColorData();
         VisVertexData vd = new VisVertexData();
         for(double[] p: segment.coloredPoints){
             vd.add(new double[]{p[0], p[1], p[2]});
             cd.add((int) p[3]);
         }
 
         Collection c = segment.objects.values();
         for(Iterator itr = c.iterator(); itr.hasNext(); ){
             ObjectInfo obj = (ObjectInfo)itr.next();
             VzText text = new VzText(Integer.toString(obj.repID));//+"-"+*/features.get(obj.repID));
             double[] bb = FeatureExtractor.boundingBox(obj.points);
             double[] xyz = new double[]{(bb[0]+bb[3])/2.0,
                                            (bb[1]+bb[4])/2.0,
                                            (bb[2]+bb[5])/2.0};
             VisChain chain = new VisChain(LinAlg.translate(xyz),LinAlg.translate(0, 0, -.2),
             		LinAlg.scale(.0015),LinAlg.scale(1, -1, 1), text);
             vb.addBack(chain);
         }
 
         vb.addBack(new VisLighting(false, new VzPoints
                                    (vd, new VzPoints.Style(cd, 1.0))));
         vb.swap();
     }
 
     public double[][][] getColorData(BufferedImage image, int width, int height){
         double[][][] colorData = new double[height][width][];
         for(int x = 0; x < image.getWidth(); x++){
         	for(int y = 0; y < image.getHeight(); y++){
         		Color color = new Color(image.getRGB(x,  y));
         		double lum = .3*color.getRed() + .59*color.getGreen() + .11 * color.getBlue();
         		if(x < width && y < height){
             		colorData[y][x] = new double[]{color.getRed(), color.getGreen(), color.getBlue(), lum};
         		}
         		//image.setRGB(x, y, new Color((int)lum, (int)lum, (int)lum).getRGB());
         	}
         }
         return colorData;
     }
 
     public double getDelta(double[][][] data, int x, int y, int scale, int channel){
     	double min = data[y][x][channel];
     	double max = data[y][x][channel];
     	int height = data.length;
     	int width = data[0].length;
     	for(int dX = -scale; dX <= scale; dX+=scale){
     		for(int dY = -scale; dY <= scale; dY+=scale){
 //    			if(dX != 0 && dY != 0){
 //    				dX *= ;
 //    				dY *= 1.414;
 //    			}
     			int xp = x + dX;
     			int yp = y + dY;
     	    	if(xp >= 0 && xp < width && yp >= 0 && yp < height){
     	    		if(data[yp][xp][channel] < min){
     	    			min = data[yp][xp][channel];
     	    		} else if(data[yp][xp][channel] > max){
     	    			max = data[yp][xp][channel];
     	    		}
     	    	}
 
     		}
     	}
     	return max - min;
     }
 
     public void draw2DImage()
     {
         if(segment.points.size() <= 0 && segment.coloredPoints.size() <= 0){
             System.out.println("No points found.");
             return;
         }
 
         BufferedImage im = new BufferedImage(ks.WIDTH, ks.HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
         byte[] buf = ((DataBufferByte)(im.getRaster().getDataBuffer())).getData();
         for (int i = 0; i < buf.length; i+=3) {
             buf[i] = ks.rgb[i+2];   // B
             buf[i+1] = ks.rgb[i+1]; // G
             buf[i+2] = ks.rgb[i];   // R
         }
 
         for(Map.Entry<Integer, ObjectInfo> entry : segment.objects.entrySet()){
         	//im = entry.getValue().getImage();
         }
         //double[] features = PCA.getFeatures(im, 5);            System.err.println("ERR: Opts error - " + opts.getReason());
 
         double[][][] colorData = getColorData(im, im.getWidth(), im.getHeight());
 
         for(int x = 0; x < im.getWidth(); x++){
     		for(int y = 0; y < im.getHeight(); y++){
             	float intensity;
         		double maxDelta = 0;
         		for(int channel = 0; channel < 3; channel++){
         			double delta = getDelta(colorData, x, y, 1, channel)/255;
         			maxDelta = (maxDelta < delta ? delta : maxDelta);
         		}
         		if(x < 0){
         			// Use all 3 color channels
             		intensity = (float)maxDelta;
         		} else{
         			// Only use intensity (grayscale)
             		intensity = (float)getDelta(colorData, x, y, 1, 3)/255;
         		}
 
 
         		if(x < divider){
         			intensity = (intensity > intensityThreshold ? 1 : 0);
         		} else {
 
         		}
 
         		if(viewRegion.contains(x, y)){
             		//im.setRGB(x, y, (new Color(intensity, intensity, intensity)).getRGB());
         		} else {
         			//im.setRGB(x, y, Color.black.getRGB());
         		}
 
         		//
 
         	}
         }
 
         VisWorld.Buffer vbim = vw2.getBuffer("pictureInPicture");
         vbim.addBack(new VzImage(im, VzImage.FLIP));
 //        for(ObjectInfo object : da.objects.values()){
 //        	BufferedImage img = object.getImage();
 //        	vbim.addBack(new VzImage(im, VzImage.FLIP));
 //        }
 
 
         vbim.swap();
     }
 
 
     public static void main(String args[])
     {
         // Define possible commandline arguments
         GetOpt opts = new GetOpt();
         opts.addBoolean('h', "help", false, "Show this help screen");
 
         if (!opts.parse(args)) {
             System.err.println("ERR: Opts error - " + opts.getReason());
             System.exit(1);
         }
         if (opts.getBoolean("help")) {
             opts.doHelp();
             System.exit(1);
         }
 
         // Set up data aggregator and segmenter
         da = new DataAggregator(false);
         da.colorThresh = initialColorThresh;
         da.unionThresh = initialUnionThresh;
         da.ransacThresh = initialRansacThresh;
         da.ransacPercent = initialRansacPercent;
         da.depthLookUp = KUtils.createDepthMap();
         da.WIDTH = (int) NounjectiveLearning.viewRegion.getWidth();
         da.HEIGHT = (int) NounjectiveLearning.viewRegion.getHeight();
 
 
         segment = new Segment((int)(viewRegion.getMaxX()-viewRegion.getMinX()),
                               (int)(viewRegion.getMaxY()-viewRegion.getMinY()));
 
         NounjectiveLearning nl = new NounjectiveLearning(opts);
     }
 }
