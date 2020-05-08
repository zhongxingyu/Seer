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
 
 import java.awt.*;
 import java.util.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.*;
 
 /* To Do:
  *  - Add features for shape recognition to FeatureVec
  */
 enum ISpyMode {
 	NORMAL, ADD_COLOR, ADD_SHAPE, ADD_SIZE
 }
 
 public class ISpy extends JFrame implements LCMSubscriber {
 	final static int WIDTH = 800;
 	final static int HEIGHT = 600;
 	final static int K_WIDTH = kinect_status_t.WIDTH;
 	final static int K_HEIGHT = kinect_status_t.HEIGHT;
 
 	static int initialColorThresh = 13;
 	static double initialUnionThresh = 0.5;
 	static double initialRansacThresh = .02;
 	static double initialRansacPercent = .1;
 
 	// Subset of the image used
 	public final static int[] viewBorders = new int[] { 75, 150, 620, 400 };
 	public final static Rectangle viewRegion = new Rectangle(viewBorders[0],
 			viewBorders[1], viewBorders[2] - viewBorders[0], viewBorders[3]
 					- viewBorders[1]);
 
 	private SceneRenderer sceneRenderer;
 	private JLabel ispyLabel;
 	private JTextField inputField;
 	private JButton addColorButton;
 	private JButton addShapeButton;
 	private JButton addSizeButton;
 	private TrainingBox trainingBox;
 
 	static LCM lcm = LCM.getSingleton();
 
 	private kinect_status_t kinectData = null;
 	private DataAggregator da;
 	private Segment segmenter;
 
 	private Map<Integer, SpyObject> objects;
 	private KNN colorKNN;
 	private KNN shapeKNN;
 	private KNN sizeKNN;
 
 	private ISpyMode curMode = ISpyMode.NORMAL;
 
 	public ISpy(DataAggregator da, Segment segmenter) {
 		super("ISpy");
 		this.setSize(WIDTH, HEIGHT);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		addColorButton = new JButton("Add Color");
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.weightx = .3;
 		gbc.weighty = .05;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		this.add(addColorButton, gbc);
 		addColorButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				curMode = ISpyMode.ADD_COLOR;
 				trainingBox.setTitle("Add Color Label");
 				trainingBox.clearText();
 				trainingBox.setVisible(true);
 			}
 		});
 
 		addShapeButton = new JButton("Add Shape");
 		gbc.gridx = 1;
 		this.add(addShapeButton, gbc);
 		addShapeButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				curMode = ISpyMode.ADD_SHAPE;
 				trainingBox.setTitle("Add Shape Label");
 				trainingBox.clearText();
 				trainingBox.setVisible(true);
 			}
 		});
 		
 		addSizeButton = new JButton("Add Size");
 		gbc.gridx = 2;
 		this.add(addSizeButton, gbc);
 		addSizeButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				curMode = ISpyMode.ADD_SIZE;
 				trainingBox.setTitle("Add Size Label");
 				trainingBox.clearText();
 				trainingBox.setVisible(true);
 			}
 		});
 
 		VisWorld visWorld = new VisWorld();
 		sceneRenderer = new SceneRenderer(visWorld, this);
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridwidth = 3;
 		gbc.weighty = .95;
 		gbc.weightx = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		this.add(sceneRenderer.getCanvas(), gbc);
 
 		ispyLabel = new JLabel("I spy something");
 		gbc.ipadx = 20;
 		gbc.gridwidth = 1;
 		gbc.weighty = .05;
 		gbc.weightx = .3;
 		gbc.gridx = 0;
 		gbc.gridy = 2;
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
 				findObject(inputField.getText());
 				inputField.setText("");
 			}
 		});
 
 		trainingBox = new TrainingBox();
 		trainingBox.addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 				curMode = ISpyMode.NORMAL;
 			}
 		});
 
 		this.da = da;
 		this.segmenter = segmenter;
 		objects = new HashMap<Integer, SpyObject>();
 
 		lcm.subscribe("KINECT_STATUS", this);
 
 		colorKNN = new KNN(30, 6,
 				"/home/bolt/mlbolt/code/java/color_features.dat");
 		shapeKNN = new KNN(10, 15,
 				"/home/bolt/mlbolt/code/java/shape_features.dat");
 		sizeKNN = new KNN(5, 2, "/home/bolt/mlbolt/code/java/size_features.dat");
 		colorKNN.loadData(false);
 		shapeKNN.loadData(true);
 		sizeKNN.loadData(false);
 
 		this.setVisible(true);
 	}
 
 	public void findObject(String desc) {
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
 				pointToObject(obj);
 				return;
 			}
 		}
 		System.out.println("NO INITIAL MATCH!!");
 		// No match initially found: create list of objects to consider
 		ArrayList<SpyObject> consider = new ArrayList<SpyObject>();
 		// best options to consider match atleast one best
 
 		for (SpyObject obj : objects.values())
 		{
 		    if (obj.matchesBestShape(labels))
 		    {
 
 		    }
 		    else if (obj.matchesBestColor(labels))
 		    {
 
 		    }
 		}
 
 		//if matches bestcolor
 /*
 		for (SpyObject obj : objects.values()) {
 			if (!consider.contains(obj)
 					&& obj.matchesOneAndSecondBest(labels, 1))
 			{
 				System.out.println("HAHA");
 				consider.add(obj);
 			}
 		}
 
 		// matches or very unconfident
 		for (SpyObject obj : objects.values()) {
 			if (!consider.contains(obj) && obj.matchesOrUnconfident(labels))
 			{
 				consider.add(obj);
 				System.out.println(obj.getShape() + " " + obj.getColor());
 			}
 		}
 */
 		Collections.sort(consider);
 
 		// secondary options accept both second best
 		/*
 		 * for(SpyObject obj : objects.values()) { if (!consider.contains(obj)
 		 * && obj.matchesOneAndSecondBest(labels, 0)) consider.add(obj); }
 		 */
 		// Manipulate considered objects
 
 		for (SpyObject obj : consider) {
 			// manipulate each objects
 			sweepObject(obj);
 			System.out.println("POINT");
 			System.out.println(obj.getShape() + " " + obj.getColor());
 			// TODO change only sweeps first object
 			boolean success = true;
 			if (success) {
 				shapeKNN.adjustThreshold(obj.getShape(), 1);
 				return;
 			}
 		}
 		System.out.println("NO MATCH");
 	}
 
 	public void sweepObject(SpyObject obj) {
 		robot_command_t command = new robot_command_t();
 		command.utime = TimeUtil.utime();
 		command.updateDest = true;
 		command.dest = new double[6];
 		double[] center = KUtils
 				.getWorldCoordinates(obj.lastObject.getCenter());
 		for (int i = 0; i < 3; i++) {
 			command.dest[i] = center[i];
 		}
 		command.action = "POINT";
 		lcm.publish("ROBOT_COMMAND", command);
 		return;
 	}
 
 	public void pointToObject(SpyObject obj) {
 		robot_command_t command = new robot_command_t();
 		command.utime = TimeUtil.utime();
 		command.updateDest = true;
 		command.dest = new double[6];
 		double[] center = KUtils
 				.getWorldCoordinates(obj.lastObject.getCenter());
 		for (int i = 0; i < 3; i++) {
 			command.dest[i] = center[i];
 		}
		command.action = "POINT";
 		lcm.publish("ROBOT_COMMAND", command);
 		return;
 	}
 
 	public void mouseClicked(double x, double y) {
 		if (curMode != ISpyMode.NORMAL) {
 			System.out.println("CLICKED");
 		}
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
 					obj.boxColor = Color.cyan;
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
 					label = String.format("%s {%s}", obj.lastObject.sizeFeatures,
 							trainingBox.getText());
 					synchronized (sizeKNN) {
 						sizeKNN.add(label, true);
 						}
 						obj.boxColor = Color.cyan;
 					break;
 				}
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Upon recieving a message from the Kinect, translate each depth point into
 	 * x,y,z space and find the pixel color for it. Then draw it.
 	 **/
 	@Override
 	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
 		try {
 			kinectData = new kinect_status_t(ins);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		extractPointCloudData();
 		updateObjects();
 
 		sceneRenderer.drawScene(kinectData, objects, da);
 	}
 
 	private void extractPointCloudData() {
 		da.currentPoints = new ArrayList<double[]>();
 		da.coloredPoints = new ArrayList<double[]>();
 
 		for (int y = (int) viewRegion.getMinY(); y < viewRegion.getMaxY(); y++) {
 			for (int x = (int) viewRegion.getMinX(); x < viewRegion.getMaxX(); x++) {
 				int i = y * kinect_status_t.WIDTH + x;
 				int d = ((kinectData.depth[2 * i + 1] & 0xff) << 8)
 						| (kinectData.depth[2 * i + 0] & 0xff);
 				double[] pKinect = KUtils.getXYZRGB(x, y, da.depthLookUp[d],
 						kinectData);
 				da.currentPoints.add(pKinect);
 			}
 		}
 	}
 
 	private void updateObjects() {
 		if (da.currentPoints.size() <= 0) {
 			return;
 		}
 		ArrayList<ObjectInfo> objs = new ArrayList<ObjectInfo>();
 		segmenter.segmentFrame();
 
 		Set<Integer> objsToRemove = new HashSet<Integer>();
 		for (Integer id : objects.keySet()) {
 			objsToRemove.add(id);
 		}
 
 		for (ObjectInfo obj : da.objects.values()) {
 			Rectangle projBBox = obj.getProjectedBBox();
 			double[] pos = new double[] { projBBox.getCenterX(),
 					projBBox.getCenterY() };
 
 			obj.colorFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.COLOR);
 			obj.shapeFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.SHAPE);
 			obj.sizeFeatures = FeatureExtractor.getFeatureString(obj, FeatureType.SIZE);
 			ConfidenceLabel color, shape, size;
 			ArrayList<ConfidenceLabel> shapeThresholds;
 			synchronized (colorKNN) {
 				color = colorKNN.classify(obj.colorFeatures);
 				shapeThresholds = shapeKNN.getThresholds();
 			}
 			synchronized (shapeKNN) {
 				shape = shapeKNN.classify(obj.shapeFeatures);
 			}
 			synchronized (sizeKNN){
 				size = sizeKNN.classify(obj.sizeFeatures);
 			}
 
 			if (color.getLabel().equals("black")) {
 				continue;
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
 			spyObject.updateColorConfidence(color);
 			spyObject.updateShapeConfidence(shape, shapeThresholds);
 			spyObject.updateSizeConfidence(size);
 			spyObject.pos = pos;
 			spyObject.bbox = projBBox;
 			spyObject.lastObject = obj;
 		}
 
 		for (Integer id : objsToRemove) {
 			objects.remove(id);
 		}
 	}
 
 	public static void main(String args[]) {
 
 		// Set up data aggregator and segmenter
 		DataAggregator da = new DataAggregator(false);
 		da.colorThresh = initialColorThresh;
 		da.unionThresh = initialUnionThresh;
 		da.ransacThresh = initialRansacThresh;
 		da.ransacPercent = initialRansacPercent;
 		da.depthLookUp = KUtils.createDepthMap();
 		da.WIDTH = (int) ISpy.viewRegion.getWidth();
 		da.HEIGHT = (int) ISpy.viewRegion.getHeight();
 		boolean colorSegments = true;
 		Segment segment = new Segment(da, colorSegments);
 
 		ISpy ispy = new ISpy(da, segment);
 	}
 }
