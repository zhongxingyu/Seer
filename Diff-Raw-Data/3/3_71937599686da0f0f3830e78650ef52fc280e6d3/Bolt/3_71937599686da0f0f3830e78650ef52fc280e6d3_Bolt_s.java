 package kinect.bolt;
 
 import april.config.*;
 import april.util.*;
 import april.vis.*;
 import april.jmat.*;
 import april.jmat.geom.GRay3D;
 import lcm.lcm.*;
 
 import kinect.lcmtypes.*;
 import kinect.kinect.*;
 import kinect.ispy.*;
 import kinect.classify.*;
 import kinect.classify.FeatureExtractor.FeatureType;
 
 import java.io.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import abolt.arm.*;
 
 enum ISpyMode {
     STANDBY, MANIPULATING, ADD_COLOR, ADD_SHAPE, ADD_SIZE, SEARCHING, FEEDBACK, GET_COLOR, GET_SHAPE, GET_SIZE, NOT_FOUND
         }
 
 
 public class Bolt extends JFrame implements LCMSubscriber
 {
     final static int K_WIDTH = kinect_status_t.WIDTH;
     final static int K_HEIGHT = kinect_status_t.HEIGHT;
     // Location of training data
     static String colorDataFile;// = "/home/rgoeddel/class/EECS545-Doc/code/java/dat/color_features.dat";
     static String shapeDataFile;// = "/home/rgoeddel/class/EECS545-Doc/code/java/dat/shape_features.dat";
     static String sizeDataFile;// = "/home/rgoeddel/class/EECS545-Doc/code/java/dat/size_features.dat";
     // objects for visualization
     private RenderScene sceneRenderer;
     private JMenuItem clearData, reloadData;
     private JCheckBoxMenuItem filterDarkCB;
     private JCheckBoxMenuItem drawSegmentationCB;
     public final static int[] viewBorders = new int[] {130, 100, 560, 440 };
     public final static Rectangle viewRegion = new Rectangle(viewBorders[0],
                                                              viewBorders[1],
                                                              viewBorders[2] - viewBorders[0],
                                                              viewBorders[3] - viewBorders[1]);
     // Objects for classifying
     private KNN colorKNN, shapeKNN, sizeKNN;
     ArrayList<ConfidenceLabel> shapeThresholds, colorThresholds, sizeThresholds;
     private Segment segmenter;
     private Map<Integer, SpyObject> objects;
     private boolean filterDark = true;
     private double darkThreshold = .4;
     private boolean drawSegmentation = false;
 
     // LCM
     static LCM lcm = LCM.getSingleton();
     private kinect_status_t kinectData = null;
     int selectedObject;
     ArrayList<double[]> currentPoints;
 
     // Opts/Config
     GetOpt opts;
     Config config;
 
     public Bolt(GetOpt opts_, Segment segmenter)
     {
         super("BOLT");
         this.setSize(K_WIDTH, K_HEIGHT);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         // Handle Options
         opts = opts_;
         try {
             config = new ConfigFile(opts.getString("config"));
         } catch (IOException ioex) {
             System.err.println("ERR: Could not load configuration from file");
             ioex.printStackTrace();
         }
 
         // Load calibration
         try {
             KUtils.loadCalibFromConfig(new ConfigFile(config.requireString("calibration.filepath")));
         } catch (IOException ioex) {
             System.err.println("ERR: Could not load calibration from file");
             ioex.printStackTrace();
         }
 
         // Load .dat files
         try {
             colorDataFile = config.requireString("training.color_data");
             shapeDataFile = config.requireString("training.shape_data");
             sizeDataFile = config.requireString("training.size_data");
         } catch (Exception ex) {
             System.err.println("ERR: Could not load all .dat files");
             ex.printStackTrace();
         }
 
 
         this.segmenter = segmenter;
         objects = new HashMap<Integer, SpyObject>();
         selectedObject = -1;
 
         JMenuBar menuBar = new JMenuBar();
         JMenu controlMenu = new JMenu("Control");
         menuBar.add(controlMenu);
 
         filterDarkCB = new JCheckBoxMenuItem("Filter Dark Objects");
         filterDarkCB.setState(true);
         filterDarkCB.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     filterDark = filterDarkCB.getState();
                 }
             });
         controlMenu.add(filterDarkCB);
 
         drawSegmentationCB = new JCheckBoxMenuItem("Draw Segmentation");
         drawSegmentationCB.setState(false);
         drawSegmentationCB.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent arg0) {
             	sceneRenderer.setDrawSegmentation(drawSegmentationCB.getState());
             }
         });
         controlMenu.add(drawSegmentationCB);
 
         // Remove all data (no built in info)
         clearData = new JMenuItem("Clear All Data");
         clearData.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     System.out.println("CLEARED DATA");
                     synchronized(colorKNN){
                         colorKNN.clearData();
                     }
                     synchronized(shapeKNN){
                         shapeKNN.clearData();
                     }
                     synchronized(sizeKNN){
                         sizeKNN.clearData();
                     }
                 }
             });
         controlMenu.add(clearData);
 
         // Remove all data (including training)
         reloadData = new JMenuItem("Reload Data");
         reloadData.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     System.out.println("RELOAD DATA");
 
                     synchronized(colorKNN){
                         colorKNN.clearData();
                         colorKNN.loadData(false);
                     }
                     synchronized(shapeKNN){
                         shapeKNN.clearData();
                         shapeKNN.loadData(true);
                     }
                     synchronized(sizeKNN){
                         sizeKNN.clearData();
                         sizeKNN.loadData(false);
                     }
                 }
             });
         controlMenu.add(reloadData);
         this.setJMenuBar(menuBar);
 
         // Set up Vis
         VisWorld visWorld = new VisWorld();
         sceneRenderer = new RenderScene(visWorld, this);
         this.add(sceneRenderer.getCanvas());
 
         // Subscribe to LCM
         lcm.subscribe("KINECT_STATUS", this);
         lcm.subscribe("TRAINING_DATA", this);
         lcm.subscribe("ALLDONE", this);
 
         // Prepare for classification
         colorKNN = new KNN(1, 6, colorDataFile, 0.2);
         shapeKNN = new KNN(10, 15,shapeDataFile, 1);
         sizeKNN = new KNN(5, 2, sizeDataFile, 1);
 
         colorKNN.loadData(false);
         shapeKNN.loadData(true);
         sizeKNN.loadData(false);
         colorThresholds = colorKNN.getThresholds();
         shapeThresholds = shapeKNN.getThresholds();
         sizeThresholds = sizeKNN.getThresholds();
         BoltArmCommandInterpreter interpreter = new BoltArmCommandInterpreter(segmenter, opts.getBoolean("debug"));
 
         this.setVisible(true);
     }
 
     /** Use the most recent frame from the kinect to extract a 3D point cloud
         and map it to the frame of the arm. **/
     private ArrayList<double[]> extractPointCloudData()
     {
         currentPoints = new ArrayList<double[]>();
 
         for (int y = (int) viewRegion.getMinY(); y < viewRegion.getMaxY(); y++) {
             for (int x = (int) viewRegion.getMinX(); x < viewRegion.getMaxX(); x++) {
                 int i = y * kinect_status_t.WIDTH + x;
                 int d = ((kinectData.depth[2 * i + 1] & 0xff) << 8)
                     | (kinectData.depth[2 * i + 0] & 0xff);
                 double[] pKinect = KUtils.getRegisteredXYZRGB(x,y, kinectData);
 
                 // Disabled to switch to registered view
                 // KUtils.getXYZRGB(x, y, KUtils.depthLookup[d],
                 //                                 kinectData);
                 currentPoints.add(pKinect);
             }
         }
         return currentPoints;
     }
 
     /** Segment the objects from the most recent frame and check whether they
         were in the last frame. Update the information about them (their labels
         and confidences. **/
     private void updateObjects(ArrayList<double[]> currentPoints) {
         if (currentPoints.size() <= 0) {
             return;
         }
         segmenter.segmentFrame(currentPoints);
 
         Set<Integer> objsToRemove = new HashSet<Integer>();
         for (Integer id : objects.keySet()) {
             objsToRemove.add(id);
         }
 
         for (ObjectInfo obj : segmenter.objects.values()) {
             Rectangle projBBox = obj.getProjectedBBox();
             double[] pos = new double[] { projBBox.getCenterX(),
                                           projBBox.getCenterY() };
 
             obj.colorFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.COLOR);
             obj.shapeFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.SHAPE);
             obj.sizeFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.SIZE);
             ConfidenceLabel color, shape, size;
 
             synchronized (colorKNN) {
                 color = colorKNN.classify(obj.colorFeatures);
             }
             synchronized (shapeKNN) {
                 shape = shapeKNN.classify(obj.shapeFeatures);
             }
             synchronized (sizeKNN){
                 size = sizeKNN.classify(obj.sizeFeatures);
             }
 
             if (filterDark) {
                 String[] params = obj.colorFeatures.substring(1).split(" ");
                 if(Double.parseDouble(params[0]) < darkThreshold &&
                    Double.parseDouble(params[1]) < darkThreshold &&
                    Double.parseDouble(params[2]) < darkThreshold){
                     continue;
                 }
             }
 
             int id = obj.repID;
             SpyObject bObject;
             if (objects.containsKey(id)) {
                 bObject = objects.get(id);
                 objsToRemove.remove(id);   // Why are we removing this?
             } else {
                 bObject = new SpyObject(id);
                 objects.put(id, bObject);
             }
             bObject.updateColorConfidence(color, colorThresholds);
             bObject.updateShapeConfidence(shape, shapeThresholds);
             bObject.updateSizeConfidence(size, sizeThresholds);
             bObject.pos = pos;
             bObject.bbox = projBBox;
 //            bObject.bbox3D = FeatureExtractor.boundingBox(obj.object.points);
             bObject.lastObject = bObject.object;
             bObject.object = obj;
         }
 
         for (Integer id : objsToRemove) {
             objects.remove(id);
         }
     }
 
     /** When the user clicks on an object, we notify Soar about which object
         they have selected. **/
     public void mouseClicked(double x, double y)
     {
         for (SpyObject obj : objects.values()) {
             if (obj.bbox.contains(x, y)) {
                 selectedObject = obj.id;
                 break;
             }
         }
         // Colors the selected object cyan, all others white
         for(SpyObject obj : objects.values()){
         	if(obj.id == selectedObject){
         		obj.boxColor = Color.yellow;
         	} else {
         		obj.boxColor = Color.white;
         	}
         }
     }
 
 
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
         if(channel.equals("ALLDONE")){
             // XXX - Do we need this here?
         }
         else if(channel.equals("KINECT_STATUS")){
             try {
                 kinectData = new kinect_status_t(ins);
             } catch (IOException e) {
                 e.printStackTrace();
                 return;
             }
 
             ArrayList<double[]> currentPoints = extractPointCloudData();
             updateObjects(currentPoints);
             sceneRenderer.drawScene(kinectData, objects);
             sendMessage();
         }
         else if(channel.equals("TRAINING_DATA")){
             try{
                 training_data_t training = new training_data_t(ins);
 
                 for(int i=0; i<training.num_labels; i++){
                     training_label_t tl = training.labels[i];
                     String label = tl.label;
                     int id = tl.id;
                     SpyObject obj = objects.get(id);
                     category_t category = tl.cat;
                     switch(category.cat){
                         case category_t.CAT_COLOR:// Color
                             String newLabel = obj.lastObject.colorFeatures+" {"+label+"}";
                             colorKNN.add(newLabel, false);
                             System.out.println("Added a new color label.");
                             break;
                         case category_t.CAT_SHAPE:// Shape
                             newLabel = obj.lastObject.shapeFeatures+" {"+label+"}";
                             shapeKNN.add(newLabel, false);
                             System.out.println("Added a new shape label.");
                             break;
                         case category_t.CAT_SIZE:// Size
                             newLabel = obj.lastObject.sizeFeatures+" {"+label+"}";
                             sizeKNN.add(newLabel, false);
                             System.out.println("Added a new size label.");
                             break;
                     }
                 }
 
             }catch (IOException e) {
                 e.printStackTrace();
                 return;
             }
         }
     }
 
 
     public void sendMessage()
     {
         observations_t obs = new observations_t();
         obs.utime = TimeUtil.utime();
 
         ArrayList<object_data_t> obsList = new ArrayList<object_data_t>();
         ArrayList<String> sensList = new ArrayList<String>();
 
         Collection c = objects.values();
         for(Iterator itr = c.iterator(); itr.hasNext(); ){
             SpyObject obj = (SpyObject)itr.next();
 
             // Bounding box and location
             double[] bb = FeatureExtractor.boundingBox(obj.object.points);
             double[] min = KUtils.getWorldCoordinates(new double[]{bb[0], bb[1], bb[2]});
             double[] max = KUtils.getWorldCoordinates(new double[]{bb[3], bb[4], bb[5]});
             double[] center = KUtils.getWorldCoordinates(obj.object.getCenter());
             double[] xyzrpy = new double[]{0, 0, 0, 0, 0, 0};
             for(int i = 0; i < 3; i++){
                 xyzrpy[i] = (min[i] + max[i])/2;
             }

 
             // Color for LCM
             categorized_data_t[] data = new categorized_data_t[3];
             categorized_data_t dColor = new categorized_data_t();
             dColor.cat = new category_t();
             dColor.cat.cat = category_t.CAT_COLOR;
             dColor.label = new String[]{obj.bestColor};
             dColor.len = 1;
             dColor.confidence = new double[]{obj.getColorConfidence()};
             data[0] = dColor;
 
             // Shape for LCM
             categorized_data_t dShape = new categorized_data_t();
             dShape.cat = new category_t();
             dShape.cat.cat = category_t.CAT_SHAPE;
             dShape.label = new String[]{obj.bestShape};
             dShape.len = 1;
             dShape.confidence = new double[]{obj.getShapeConfidence()};
             data[1] = dShape;
 
             // Size for LCM
             categorized_data_t dSize = new categorized_data_t();
             dSize.cat = new category_t();
             dSize.cat.cat = category_t.CAT_SIZE;
             dSize.label = new String[]{obj.bestSize};
             dSize.len = 1;
             dSize.confidence = new double[]{obj.getSizeConfidence()};
             data[2] = dSize;
 
             // Create object data for lcm
             object_data_t obj_data = new object_data_t();
             obj_data.utime = TimeUtil.utime();
             obj_data.id = obj.id;
             obj_data.cat_dat = data;
             obj_data.num_cat = obj_data.cat_dat.length;
             obj_data.pos = xyzrpy;
             obj_data.bbox = new double[][]{min, max};
             obsList.add(obj_data);
         }
 
         if(!objects.containsKey(selectedObject)){
             selectedObject = -1;
         }
         obs.click_id = selectedObject;
         obs.sensables = sensList.toArray(new String[0]);
         obs.nsens = obs.sensables.length;
         obs.observations = obsList.toArray(new object_data_t[0]);
         obs.nobs = obs.observations.length;
 
         lcm.publish("OBSERVATIONS",obs);
 
     }
 
 
     public static void main(String args[])
     {
         GetOpt opts = new GetOpt();
 
         opts.addBoolean('h', "help", false, "Show this help screen");
         opts.addString('c', "config", null, "Specify the configuration file");
         opts.addBoolean('d', "debug", false, "Toggle debugging mode");
 
         if (!opts.parse(args)) {
             System.err.println("ERR: GetOpt - "+opts.getReason());
             return;
         }
 
         if (opts.getBoolean("help") || opts.getString("config") == null) {
             System.out.println("Usage: Must specify a configuration file");
             opts.doHelp();
             return;
         }
 
         KUtils.createDepthMap();
         Bolt bolt = new Bolt(opts,
                              new Segment((int)(viewRegion.getMaxX()-viewRegion.getMinX()),
                                          (int)(viewRegion.getMaxY()-viewRegion.getMinY())));
     }
 }
