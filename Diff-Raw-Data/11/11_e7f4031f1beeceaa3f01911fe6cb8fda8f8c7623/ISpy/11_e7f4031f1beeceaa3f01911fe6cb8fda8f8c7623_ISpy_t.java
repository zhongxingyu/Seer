 package kinect.ispy;
 
 import april.util.TimeUtil;
 import april.vis.*;
 import april.jmat.*;
 import april.jmat.geom.GRay3D;
 import lcm.lcm.*;
 import kinect.lcmtypes.*;
 import kinect.kinect.*;
 
 import kinect.classify.*;
 import kinect.classify.FeatureExtractor.FeatureType;
 
 import java.io.*;
 import javax.swing.*;
 
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Color;
 import java.awt.Insets;
 import java.util.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.image.*;
 
 /* To Do:
  *  - Add features for shape recognition to FeatureVec
  */
 enum ISpyMode {
     STANDBY, MANIPULATING, ADD_COLOR, ADD_SHAPE, ADD_SIZE, SEARCHING, FEEDBACK, GET_COLOR, GET_SHAPE, GET_SIZE, NOT_FOUND
 	}
 
 public class ISpy extends JFrame implements LCMSubscriber {
     final static int WIDTH = 800;
     final static int HEIGHT = 600;
     final static int K_WIDTH = kinect_status_t.WIDTH;
     final static int K_HEIGHT = kinect_status_t.HEIGHT;
 
     static int initialColorThresh = 13;
     static double initialUnionThresh = 0.5;
     static double initialRansacThresh = .025;
     static double initialRansacPercent = .1;
 
     static String colorDataFile = "/home/bolt/mlbolt/code/java/dat/color_features.dat";
     static String shapeDataFile = "/home/bolt/mlbolt/code/java/dat/shape_features.dat";
     static String sizeDataFile = "/home/bolt/mlbolt/code/java/dat/size_features.dat";
 
 
 
     public final static int[] viewBorders = new int[] {0, 150, 640, 440 };
     public final static Rectangle viewRegion =
 	new Rectangle(viewBorders[0],
 		      viewBorders[1], viewBorders[2] - viewBorders[0],
 		      viewBorders[3] - viewBorders[1]);
 
 
     private SceneRenderer sceneRenderer;
     private JLabel ispyLabel;
     private JTextField inputField;
     private JMenuItem colorMenuItem;
     private JMenuItem shapeMenuItem;
     private JMenuItem sizeMenuItem;
     private JMenuItem clearData;
     private JMenuItem reloadData;
     private JCheckBoxMenuItem filterDarkCB;
     private JCheckBoxMenuItem showSegmentationCB;
     private TrainingBox trainingBox;
 
     static LCM lcm = LCM.getSingleton();
 
     private kinect_status_t kinectData = null;
 //    private DataAggregator da;
     private Segment segmenter;
 
     private Map<Integer, SpyObject> objects;
     private KNN colorKNN;
     private KNN shapeKNN;
     private KNN sizeKNN;
 
     //confidence thresholds
     ArrayList<ConfidenceLabel> shapeThresholds;
     ArrayList<ConfidenceLabel> colorThresholds;
     ArrayList<ConfidenceLabel> sizeThresholds;
 
     // trained labels
     List<String> shapeLabels;
     List<String> colorLabels;
     List<String> sizeLabels;
 
     //TODO better way than this?
     private Queue<SpyObject> consider;
     SpyObject lastReferenced;
     String lastReferencedShape;
     String lastReferencedColor;
     String lastReferencedSize;
     String lastText;
     boolean adjustDown;
 
     private ISpyMode curMode = ISpyMode.STANDBY;
     private boolean filterDark = true;
     private double darkThreshold = .42;
     private boolean showSegmentation = false;
 
     public ISpy(Segment segmenter) {
         super("ISpy");
         this.setSize(WIDTH, HEIGHT);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
 
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
 
         clearData = new JMenuItem("Clear All Data");
         clearData.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     System.out.println("CLEARED DATA");
 
 
                     synchronized(colorKNN){
                         colorKNN.clearData();
                         colorThresholds = colorKNN.getThresholds();
                         colorLabels = colorKNN.getListofLabels();
                     }
                     synchronized(shapeKNN){
                         shapeKNN.clearData();
                         shapeThresholds = shapeKNN.getThresholds();
                         shapeLabels = shapeKNN.getListofLabels();
                     }
                     synchronized(sizeKNN){
                         sizeKNN.clearData();
                         sizeThresholds = sizeKNN.getThresholds();
                         sizeLabels = sizeKNN.getListofLabels();
                     }
                 }
             });
         controlMenu.add(clearData);
 
         reloadData = new JMenuItem("Reload Data");
         reloadData.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     System.out.println("RELOAD DATA");
 
                     synchronized(colorKNN){
                         colorKNN.clearData();
                         colorKNN.loadData(false);
                         colorThresholds = colorKNN.getThresholds();
                         colorLabels = colorKNN.getListofLabels();
                     }
                     synchronized(shapeKNN){
                         shapeKNN.clearData();
                         shapeKNN.loadData(true);
                         shapeThresholds = shapeKNN.getThresholds();
                         shapeLabels = shapeKNN.getListofLabels();
                     }
                     synchronized(sizeKNN){
                         sizeKNN.clearData();
                         sizeKNN.loadData(false);
                         sizeThresholds = sizeKNN.getThresholds();
                         sizeLabels = sizeKNN.getListofLabels();
                     }
                 }
             });
         controlMenu.add(reloadData);
 
 
         JMenu labelsMenu = new JMenu("Add Labels");
         menuBar.add(labelsMenu);
 
         colorMenuItem = new JMenuItem("Color");
         colorMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gotoStandbyMode();
                     curMode = ISpyMode.ADD_COLOR;
                     trainingBox.setTitle("Add Color Label");
                     trainingBox.clearText();
                     trainingBox.setVisible(true);
                 }
             });
         labelsMenu.add(colorMenuItem);
 
         shapeMenuItem = new JMenuItem("Shape");
         shapeMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gotoStandbyMode();
                     curMode = ISpyMode.ADD_SHAPE;
                     trainingBox.setTitle("Add Shape Label");
                     trainingBox.clearText();
                     trainingBox.setVisible(true);
                 }
             });
         labelsMenu.add(shapeMenuItem);
 
         sizeMenuItem = new JMenuItem("Size");
         sizeMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gotoStandbyMode();
                     curMode = ISpyMode.ADD_SIZE;
                     trainingBox.setTitle("Add Size Label");
                     trainingBox.clearText();
                     trainingBox.setVisible(true);
                 }
             });
         labelsMenu.add(sizeMenuItem);
 
         this.setJMenuBar(menuBar);
 
 
         VisWorld visWorld = new VisWorld();
         sceneRenderer = new SceneRenderer(visWorld, this);
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridwidth = 3;
         gbc.weighty = 1;
         gbc.weightx = 1;
         gbc.gridx = 0;
         gbc.gridy = 0;
         this.add(sceneRenderer.getCanvas(), gbc);
 
         ispyLabel = new JLabel("I spy something");
         gbc.ipadx = 20;
         gbc.gridwidth = 1;
         gbc.weighty = .05;
         gbc.weightx = .3;
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.insets = new Insets(20, 20, 10, 20);
         this.add(ispyLabel, gbc);
 
         inputField = new JTextField();
         gbc.gridwidth = 2;
         gbc.weightx = .6;
         gbc.gridx = 1;
         gbc.insets = new Insets(10, 20, 10, 20);
         this.add(inputField, gbc);
         inputField.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     textEntered(inputField.getText());
                     inputField.setText("");
                 }
             });
 
         trainingBox = new TrainingBox();
         trainingBox.addComponentListener(new ComponentAdapter() {
                 @Override
                 public void componentHidden(ComponentEvent arg0) {
                     curMode = ISpyMode.STANDBY;
                 }
             });
 
         this.segmenter = segmenter;
         objects = new HashMap<Integer, SpyObject>();
 
         lcm.subscribe("KINECT_STATUS", this);
         lcm.subscribe("ALLDONE", this);
 
         colorKNN = new KNN(30, 6, colorDataFile);
         shapeKNN = new KNN(10, 15,shapeDataFile);
         sizeKNN = new KNN(5, 2, sizeDataFile);
         colorKNN.loadData(false);
         shapeKNN.loadData(true);
         sizeKNN.loadData(false);
         colorThresholds = colorKNN.getThresholds();
         shapeThresholds = shapeKNN.getThresholds();
         sizeThresholds = sizeKNN.getThresholds();
 
         colorLabels = colorKNN.getListofLabels();
         sizeLabels = sizeKNN.getListofLabels();
         shapeLabels = shapeKNN.getListofLabels();
 
         this.setVisible(true);
     }
 
     public ISpyMode findObject(String desc) {
         ArrayList<String> labels = new ArrayList<String>();
         String[] splitDesc = desc.toLowerCase().trim().split(" ");
         for (int i = 0; i < splitDesc.length; i++) {
             if (splitDesc[i].trim().equals("")
 		|| splitDesc[i].trim().equals("and")) {
                 continue;
             }
             labels.add(splitDesc[i]);
         }
 
         for (SpyObject obj : objects.values()) {
             if (obj.matches(labels)) {
                 // if found during manipulation may adjust training
                 if ((curMode == ISpyMode.MANIPULATING) &&
                     (lastReferenced != null))
                 {
                     /*
                       if ((!lastReferencedColor.equals(obj.getColor())) && labels.contains(obj.getColor()))
                       {
                       colorKNN.adjustThreshold(
                       lastReferencedColor, 1);
                       colorThresholds = colorKNN.getThresholds();
                       }
                     */
                     lastReferenced = null;
                     consider.clear();
                     adjustDown = false;
                 }
                 // For 1. Ask->correct record labels
                 if (curMode != ISpyMode.MANIPULATING)
                 {
                     adjustDown = false;
                     lastReferencedColor ="";
                     lastReferencedShape ="";
                     lastReferencedSize = "";
                     // check if any labels were below had conf < threshold
                     if ((labels.contains(obj.getColor())) &&
                         (obj.colorConfidenceThresholdDif() < 0))
                     {
                         adjustDown = true;
                         lastReferencedColor = obj.getColor();
                     }
                     if ((labels.contains(obj.getShape())) &&
                         (obj.shapeConfidenceThresholdDif() < 0))
                     {
                         adjustDown = true;
                         lastReferencedShape = obj.getShape();
                     }
                     if ((labels.contains(obj.getSize())) &&
                         (obj.sizeConfidenceThresholdDif() < 0))
                     {
                         adjustDown = true;
                         lastReferencedSize = obj.getSize();
                     }
                 }
 
                 pointToObject(obj);
                 return ISpyMode.SEARCHING;
             }
         }
         System.out.println("NO INITIAL MATCH");
 
         //if not already in the middle of manipulation find objects to
         // consider otherwise look at next object to consider
         if (curMode != ISpyMode.MANIPULATING)
         {
             System.out.println("first search");
             // No match found:create list of objects to consider
             ArrayList<SpyObject> considerCalc =
                 new ArrayList<SpyObject>();
             // best options to consider match atleast one best
             ArrayList<Double> considerConf = new ArrayList<Double>();
             //TODO sort hack
             ArrayList<Double> considerConfSortHack = new ArrayList<Double>();
 
             for (SpyObject obj : objects.values())
             {
                 if (obj.matchesBestShape(labels))
                 {
                     double wrongconf = 0.0;
                     boolean colorLabeled = false;
                     boolean sizeLabeled = false;
                     for (String alabel : labels)
                     {
                         if (colorLabels.contains(alabel))
                         {
                             colorLabeled = true;
                         }
                         else if (sizeLabels.contains(alabel))
                         {
                             sizeLabeled = true;
                         }
                     }
 
                     if (obj.matchesBestColor(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (colorLabeled)
                     {
                         double conf;
                         conf = obj.colorConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     if (obj.matchesBestSize(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (sizeLabeled)
                     {
                         double conf;
                         conf = obj.sizeConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     considerCalc.add(obj);
                     considerConf.add(wrongconf);
                     considerConfSortHack.add(wrongconf);
                 }
                 else if (obj.matchesBestColor(labels))
                 {
                     double wrongconf = 0.0;
                     boolean shapeLabeled = false;
                     boolean sizeLabeled = false;
                     for (String alabel : labels)
                     {
                         if (shapeLabels.contains(alabel))
                         {
                             shapeLabeled = true;
                         }
                         else if (sizeLabels.contains(alabel))
                         {
                             sizeLabeled = true;
                         }
                     }
 
                     if (obj.matchesBestShape(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (shapeLabeled)
                     {
                         double conf;
                         conf = obj.shapeConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     if (obj.matchesBestSize(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (sizeLabeled)
                     {
                         double conf;
                         conf = obj.sizeConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     considerCalc.add(obj);
                     considerConf.add(wrongconf);
                     considerConfSortHack.add(wrongconf);
                 }
                 else if (obj.matchesBestSize(labels))
                 {
                     double wrongconf = 0.0;
                     boolean colorLabeled = false;
                     boolean shapeLabeled = false;
                     for (String alabel : labels)
                     {
                         if (colorLabels.contains(alabel))
                         {
                             colorLabeled = true;
                         }
                         else if (shapeLabels.contains(alabel))
                         {
                             shapeLabeled = true;
                         }
                     }
 
                     if (obj.matchesBestColor(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (colorLabeled)
                     {
                         double conf;
                         conf = obj.colorConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     if (obj.matchesBestShape(labels))
                     {
                         wrongconf += -1.0;
                     }
                     else if (shapeLabeled)
                     {
                         double conf;
                         conf = obj.shapeConfidenceThresholdDif();
                         //if confident about wrong label continue
                         if (conf > 0)
                         {
                             continue;
                         }
                         wrongconf += conf;
                     }
 
                     considerCalc.add(obj);
                     considerConf.add(wrongconf);
                     considerConfSortHack.add(wrongconf);
                 }
             }
 
             consider = new LinkedList<SpyObject>();
             Collections.sort(considerConfSortHack);
             for (Double d : considerConfSortHack)
             {
                 System.out.print(considerConf.indexOf(d) + " with id ");
                 SpyObject so = considerCalc.get(considerConf.indexOf(d));
                 System.out.println(so.id);
                 consider.add(considerCalc.get(considerConf.indexOf(d)));
             }
         }
         //System.out.println("size: " + consider.size());
         if (consider != null && ((lastReferenced = consider.poll()) != null)) {
             // manipulate objects
             System.out.println("just removed id " + lastReferenced.id);
             //System.out.println("aftersize: " + consider.size());
             lastReferencedColor = lastReferenced.getColor();
             lastReferencedShape = lastReferenced.getShape();
             lastReferencedSize = lastReferenced.getSize();
 
             sweepObject(lastReferenced);
             System.out.print("SWEEP the ");
             System.out.println(lastReferenced.getColor() + " " +
                                lastReferenced.getShape());
             return ISpyMode.MANIPULATING;
         }
         //cannot find should request for training
         System.out.println("NOT FOUND");
         return ISpyMode.NOT_FOUND;
     }
 
     public void sweepObject(SpyObject obj)
     {
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.updateDest = true;
         command.dest = new double[6];
         System.out.println("SWEEPING lastobject id " + obj.lastObject.repID);
         double[] center = KUtils.getWorldCoordinates(obj.lastObject.getCenter());
         for (int i = 0; i < 3; i++) {
             command.dest[i] = center[i];
         }
         command.action = "SWEEP";
         lcm.publish("ROBOT_COMMAND", command);
         return;
     }
 
     public void pointToObject(SpyObject obj)
     {
         robot_command_t command = new robot_command_t();
         command.utime = TimeUtil.utime();
         command.updateDest = true;
         command.dest = new double[6];
         double[] center = KUtils.getWorldCoordinates(obj.lastObject.getCenter());
         for (int i = 0; i < 3; i++) {
             command.dest[i] = center[i];
         }
         command.action = "POINT";
         lcm.publish("ROBOT_COMMAND", command);
         return;
     }
 
     public void mouseClicked(double x, double y) {
         for (SpyObject obj : objects.values()) {
             if (obj.bbox.contains(x, y)) {
                 switch (curMode) {
                     case ADD_COLOR:
                         String label = String
                             .format("%s {%s}", obj.lastObject.colorFeatures,
                                     trainingBox.getText());
                         System.out.println(label);
                         synchronized (colorKNN) {
                             colorKNN.add(label, false);
                         }
                         obj.boxColor  = Color.cyan;
                         break;
                     case ADD_SHAPE:
                         label = String
                             .format("%s {%s}", obj.lastObject.shapeFeatures,
                                     trainingBox.getText());
                         synchronized (shapeKNN) {
                             shapeKNN.add(label, true);
                         }
                         obj.boxColor = Color.cyan;
                         break;
                     case ADD_SIZE:
                         label = String.format("%s {%s}",
                                               obj.lastObject.sizeFeatures,
                                               trainingBox.getText());
                         synchronized (sizeKNN) {
                             sizeKNN.add(label, false);
                         }
                         obj.boxColor = Color.cyan;
                         break;
                     case NOT_FOUND:
                         ArrayList<String> labels = new ArrayList<String>();
                         String[] splitDesc = lastText.toLowerCase().trim().split(" ");
                         for (int i = 0; i < splitDesc.length; i++) {
                             if (splitDesc[i].trim().equals("")
                                 || splitDesc[i].trim().equals("and")) {
                                 continue;
                             }
                             labels.add(splitDesc[i]);
                         }
                         for (String alabel : labels)
                         {
                             if (colorLabels.contains(alabel))
                             {
                                 String data = String
                                     .format("%s {%s}", obj.lastObject.colorFeatures,
                                             alabel);
                                 //add to training and adjust threshold
                                 synchronized (colorKNN) {
                                     colorKNN.add(data, false);
                                     if (!alabel.equals(obj.getColor()))
                                         colorKNN.updateThreshold(obj.getColor(), 1);
                                 }
                             }
                             else if (shapeLabels.contains(alabel))
                             {
                                 String data = String
                                     .format("%s {%s}", obj.lastObject.shapeFeatures,
                                             alabel);
                                 //add to training and adjust threshold
                                 synchronized (shapeKNN) {
                                     shapeKNN.add(data, true);
                                     if (!alabel.equals(obj.getShape()))
                                         shapeKNN.updateThreshold(obj.getShape(), 1);
                                 }
                             }
                             else if (sizeLabels.contains(alabel))
                             {
                                 String data = String
                                     .format("%s {%s}", obj.lastObject.sizeFeatures,
                                             alabel);
                                 //add to training and adjust threshold
                                 synchronized (sizeKNN) {
                                     sizeKNN.add(data, false);
                                     if (!alabel.equals(obj.getSize()))
                                         sizeKNN.updateThreshold(obj.getSize(), 1);
                                 }
                             }
                         }
                         gotoStandbyMode();
                         break;
                 }
                 break;
             }
         }
         synchronized (colorKNN) {
             colorLabels = colorKNN.getListofLabels();
             colorThresholds = colorKNN.getThresholds();
         }
         synchronized (shapeKNN) {
             shapeLabels = shapeKNN.getListofLabels();
             shapeThresholds = shapeKNN.getThresholds();
         }
         synchronized (sizeKNN) {
             sizeLabels = sizeKNN.getListofLabels();
             sizeThresholds = sizeKNN.getThresholds();
         }
     }
 
     public void textEntered(String text){
         if(text.equals("")){
             return;
         }
         switch(curMode){
             case STANDBY:
                 curMode = findObject(text);
                 lastText = text;
                 //curMode = ISpyMode.SEARCHING;
                 if (curMode == ISpyMode.NOT_FOUND)
                 {
                     ispyLabel.setText("Cannot find object. Please click on the appropriate object (x to cancel)");
                 }
                 else
                 {
                     ispyLabel.setText("Searching for object");
                 }
                 break;
             case SEARCHING:
                 if(text.toLowerCase().equals("stop") || text.toLowerCase().equals("quit") || text.toLowerCase().equals("x")){
                     gotoStandbyMode();
                 }
                 break;
             case MANIPULATING:
                 if(text.toLowerCase().equals("stop") || text.toLowerCase().equals("quit") || text.toLowerCase().equals("x")){
                     gotoStandbyMode();
                 }
                 break;
             case FEEDBACK:
                 if(text.toLowerCase().charAt(0) == 'y'){
                     System.out.println("Added new label for " + text);
                     //adjust thresholds for labels down if needed
 		if (adjustDown)
 		{
 		    if (!lastReferencedSize.equals(""))
 		    {
                 sizeKNN.updateThreshold(lastReferencedSize, -1);
                 sizeThresholds=sizeKNN.getThresholds();
 		    }
 		    if (!lastReferencedShape.equals(""))
 		    {
                 shapeKNN.updateThreshold(lastReferencedShape, -1);
                 shapeThresholds=shapeKNN.getThresholds();
 		    }
 		    if (!lastReferencedColor.equals(""))
 		    {
                 colorKNN.updateThreshold(lastReferencedColor, -1);
                 colorThresholds=colorKNN.getThresholds();
 		    }
 		    adjustDown = false;
 		}
 		// MODIFY: save training label
 		gotoStandbyMode();
                 } else if(text.toLowerCase().charAt(0) == 'n'){
                     ispyLabel.setText("What color is it? (Enter X to skip)");
                     curMode = ISpyMode.GET_COLOR;
                 } else if(text.toLowerCase().charAt(0) == 'x'){
                     gotoStandbyMode();
                 }
                 break;
             case GET_COLOR:
                 if(!text.toLowerCase().equals("x")){
                     System.out.println("Added color label for " + text);
                     // MODIFY: Add color example
                 }
                 ispyLabel.setText("What shape is it? (Enter X to skip)");
                 curMode = ISpyMode.GET_SHAPE;
                 break;
             case GET_SHAPE:
                 if(!text.toLowerCase().equals("x")){
                     System.out.println("Added shape label for " + text);
                     // MODIFY: Add shape example
                 }
                 ispyLabel.setText("What size is it? (Enter X to skip)");
                 curMode = ISpyMode.GET_SIZE;
                 break;
             case GET_SIZE:
                 if(!text.toLowerCase().equals("x")){
                     System.out.println("Added size label for " + text);
                     // MODIFY: Add size example
                 }
                 gotoStandbyMode();
                 break;
             case NOT_FOUND:
                 if(text.toLowerCase().equals("stop") || text.toLowerCase().equals("quit") || text.toLowerCase().equals("x")){
                     gotoStandbyMode();
                 }
                 break;
         }
 
 
     }
 
     public void gotoStandbyMode()
     {
         if(curMode == ISpyMode.SEARCHING){
             robot_command_t command = new robot_command_t();
             command.utime = TimeUtil.utime();
             command.updateDest = false;
             command.dest = new double[6];
             command.action = "RESET";
             lcm.publish("ROBOT_COMMAND", command);
 
             curMode = ISpyMode.STANDBY;
         } else if(curMode == ISpyMode.FEEDBACK || curMode == ISpyMode.GET_COLOR
                   || curMode == ISpyMode.GET_SHAPE || curMode == ISpyMode.GET_SIZE){
             // MODIFY: Stuff here to clear information
         }
         else if (curMode == ISpyMode.MANIPULATING)
         {
             lastReferenced = null;
             consider.clear();
         }
         lastReferenced = null;
         adjustDown = false;
         curMode = ISpyMode.STANDBY;
         ispyLabel.setText("I spy something:");
     }
 
     /**
      * Upon recieving a message from the Kinect, translate each depth point into
      * x,y,z space and find the pixel color for it. Then draw it.
      **/
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
         if(channel.equals("ALLDONE")){
             System.out.println("I just got a message");
             if(curMode == ISpyMode.SEARCHING){
                 curMode = ISpyMode.FEEDBACK;
                 ispyLabel.setText("Is this it? (y/n/x)");
             }
             else if(curMode == ISpyMode.MANIPULATING){
 
                 curMode = findObject(lastText);
                 if (curMode == ISpyMode.NOT_FOUND)
                 {
                     ispyLabel.setText("Cannot find object. Please click on the appropriate object (x to cancel)");
                 }
             }
         } else if(channel.equals("KINECT_STATUS")){
             try {
                 kinectData = new kinect_status_t(ins);
             } catch (IOException e) {
                 e.printStackTrace();
                 return;
             }
 
             extractPointCloudData();
             updateObjects();
 
             sceneRenderer.drawScene(kinectData, objects);
         }
     }
 
 
     private void extractPointCloudData()
     {
         segmenter.points = new ArrayList<double[]>();
         segmenter.points = new ArrayList<double[]>();
 
         for (int y = (int) viewRegion.getMinY(); y < viewRegion.getMaxY(); y++) {
             for (int x = (int) viewRegion.getMinX(); x < viewRegion.getMaxX(); x++) {
                 int i = y * kinect_status_t.WIDTH + x;
                 int d = ((kinectData.depth[2*i+1] & 0xff) << 8) |
                         (kinectData.depth[2*i+0] & 0xff);
                double[] pKinect = KUtils.getRegisteredXYZRGB(x,y, kinectData);
                //KUtils.getXYZRGB(x, y, KUtils.depthLookup[d], kinectData);
                 segmenter.points.add(pKinect);
             }
         }
     }
 
     private void updateObjects() {
         if (segmenter.points.size() <= 0) {
             return;
         }
         ArrayList<ObjectInfo> objs = new ArrayList<ObjectInfo>();
         segmenter.segmentFrame(segmenter.points);
 
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
             SpyObject spyObject;
             if (objects.containsKey(id)) {
                 spyObject = objects.get(id);
                 objsToRemove.remove(id);
             } else {
                 spyObject = new SpyObject(id);
                 objects.put(id, spyObject);
             }
             spyObject.updateColorConfidence(color, colorThresholds);
             spyObject.updateShapeConfidence(shape, shapeThresholds);
             spyObject.updateSizeConfidence(size, sizeThresholds);
             spyObject.pos = pos;
             spyObject.bbox = projBBox;
             spyObject.lastObject = obj;
         }
 
         for (Integer id : objsToRemove) {
             objects.remove(id);
         }
     }
 
     public static void main(String args[])
     {
         // Set up data aggregator and segmenter
         Segment segment = new Segment((int)(viewRegion.getMaxX()-viewRegion.getMinX()),
                               (int)(viewRegion.getMaxY()-viewRegion.getMinY()));
         KUtils.createDepthMap();
         ISpy ispy = new ISpy(segment);
     }
 }
