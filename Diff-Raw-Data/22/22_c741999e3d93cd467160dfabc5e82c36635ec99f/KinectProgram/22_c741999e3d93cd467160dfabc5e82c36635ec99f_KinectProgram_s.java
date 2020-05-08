 package be.wide.main;
 
 import java.awt.Color;
 import java.io.*;
 import java.util.*;
 import org.OpenNI.*;
 import processing.core.*;
 import wblut.core.processing.*;
 import wblut.geom.core.*;
 import wblut.hemesh.core.*;
 import wblut.hemesh.creators.*;
 import wblut.hemesh.subdividors.*;
 import wblut.hemesh.tools.*;
 import be.wide.controller.*;
 import be.wide.detector.*;
 import be.wide.dom.*;
 
 import com.primesense.NITE.*;
 
 /**
  * 
  * @author Maarten Taeymans
  *
  */
 @SuppressWarnings("serial")
 public class KinectProgram extends PApplet{
 
 	private KinectController kinect;
 	private DetectionHandler opencv;
 	private GestureController gest;
 	private SphereCamera cam;
 	private BodyPoseHandler bpHandler;
 	private BodyPoseDetector pd;
 	private BodyPoseDetector pd2;
 
 	
 	private HE_Mesh mesh;
 	float[][] points;
 
 	private WB_Render render;
 	private boolean isZooming = false;
 	private boolean isNavigation = false;
 	private boolean isModeling = false;
 	private boolean isChoosing = true;
 	private Point3D prevPos = null;
 	private PImage image;
 	private PImage poseImage;
 	private String modeString;
 	private HET_Selector selector;
 	private PImage handImage;
 	private String imagePath = "../resources/openHand.png";
 	private HE_Face selected;
 	private HashMap<Integer, List<HE_Face>> selectedList;
 	private List<HE_Vertex> vertMoved;
 	private int bgColor = 135;
 	private Slider rotateSlider;
 	private Slider modelSlider;
 	private Button menuButton;
 	private Button modelButton;
 	private Button zoomButton;
 	private boolean explosionStarting = false;
 	private boolean leftFist = false;
 	private boolean rightFist = false;
 
 	// Explosion vars
 	private HE_Mesh[] voroMesh;
 	private boolean explosionDone = false;
 	private int numberOfParticles = 500;
 	private boolean isExploding = false;
 	private int move = 20;
 	private Particle[] particles;
 	private int exploCounter = 0;
 	private int numberOfCircles = 50;
 
 	private int xPos, yPos;
 	private float rotateDistanceX = 5;
 	private float rotateDistanceY = 5;
 	private int selectedModel = 0;
 	private HE_Mesh[] startMeshes;
 	private Button selectionButton;
 
 	private String savedFilename;
 	private List<AnimatedCircle> introExplo;
 
 	private Color modelColor;
 	private HE_Face faceSize;
 
 	private PImage rotateIcon;
 	private PImage zoomIcon;
 	private PImage softSelIcon;
 	private PImage questionIcon;
 	
 	private HelpOverlay help;
 	private float tposeTimer = 0;
 
 	// Non-applet starting point
 //		static public void main(String args[])
 //		{
 //			PApplet.main(new String[] {"be.wide.main.KinectProgram"});
 //		}
 
 	/**
 	 * Processing's PApplet setup method.
 	 * Called when applet created.
 	 */
 	public void setup()
 	{
 		size(1000, 800, OPENGL);
 		background(bgColor);
 		//this.frame.setTitle("KinectModeling");
 		PFont font = createFont("Verdana", 30);
 		textFont(font);
 		textMode(SCREEN);
 		text("Loading..", width/2 - 20, height/2);
 		redraw();
 
 		kinect = KinectController.getInstance();
 		opencv = new DetectionHandler();
 		bpHandler = BodyPoseHandler.getInstance();
 		gest = kinect.getGestureCont();
 		//kinect.setToggleGesture(false);
 
 		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);
 		cam.zoomOut(-300);
 		cam.left(50);
 
 		HEC_Box boxCreator = new HEC_Box(300, 300, 300, 5, 5, 5);
 
 		mesh = new HE_Mesh(boxCreator);	
 		render = new WB_Render(this);
 
 		image = new PImage();
 		modeString = "";
 		selector = new HET_Selector(this);
 		handImage = new PImage();
 		handImage = loadImage(imagePath, "png");
 		handImage.resize(handImage.width/3, handImage.height/3);
 
 		poseImage = new PImage();
 		poseImage = loadImage("../resources/pose.png", "png");
 
 		zoomIcon = new PImage();
 		zoomIcon = loadImage("../resources/zoom.png", "png");
 		zoomIcon.resize(80, 80);
 
 		rotateIcon = new PImage();
 		rotateIcon = loadImage("../resources/rotation.png", "png");
 		rotateIcon.resize(80, 80);
 
 		softSelIcon = new PImage();
 		softSelIcon = loadImage("../resources/soft_selection.png", "png");
 		softSelIcon.resize(80, 80);
 		
 		questionIcon = new PImage();
 		questionIcon = loadImage("../resources/questionmark.png", "png");
 		questionIcon.resize(80, 80);
 
 		help = new HelpOverlay(800, 600, 520, 160, 
 				new String[] {"Poses:", "T-Pose: Triangulate biggest faces", 
 				"X-Pose: Save and exit program", "", "", "Author: Maarten Taeymans", "Internship project at Dept. ASRO, KULeuven"}, questionIcon);
 		
 		selectedList = new HashMap<Integer, List<HE_Face>>();
 		vertMoved = new ArrayList<HE_Vertex>();
 
 		rotateSlider = new Slider(50, 200, 200, 50);
 		rotateSlider.setVisible(false);
 		modelSlider = new Slider(50, 200, 200, 50);
 		modelSlider.setVisible(false);
 		menuButton = new Button("NAVIGATION", 900, 200, 120, 1000, rotateIcon);
 		menuButton.setVisible(false);
 		modelButton = new Button("MODELING", 900, 350, 120, 1000, softSelIcon);
 		modelButton.setVisible(false);
 		zoomButton = new Button("ZOOM", 900, 500, 120, 1000, zoomIcon);
 		zoomButton.setVisible(false);
 		selectionButton = new Button("SELECT", 900, 350, 120, 1000, null);
 		selectionButton.setVisible(false);
 
 		modelColor = new Color(60, 179, 113);
 		//modelColor = new Color(32, 178, 170);
 
 		startMeshes = new HE_Mesh[5];
 		loadMeshes();
 		setObservers();
 
 		introExplo = new ArrayList<AnimatedCircle>();
 		for (int i = 0; i < numberOfCircles; ++i)
 		{
 			AnimatedCircle temp = new AnimatedCircle(width, height);
 			introExplo.add(temp);
 		}
 
 		particles = new Particle[numberOfParticles];
 		for (int i = 0; i < particles.length; i++) 
 		{
 			PVector pos = new PVector(random(-40, 40), random(-40, 40), random(-40, 40));
 			pos.limit(50);
 			PVector v = new PVector();
 			v.set(pos);
 			v.normalize();
 			v.mult(random(10));
 			particles[i] = new Particle(pos, v);
 		}
 	}
 
 	/**
 	 * Loads the different meshes in a picklist.
 	 */
 	private void loadMeshes()
 	{
 		HEC_Geodesic geo = new HEC_Geodesic();
 		geo.setRadius(300).setLevel(3); 
 		HE_Mesh geoMesh = new HE_Mesh(geo);
 		startMeshes[0] = geoMesh;
 
 		faceSize = startMeshes[0].getFaceByKey(0);
 
 		HEC_Box boxCreator = new HEC_Box(400, 400, 400, 10, 10, 10);
 		HE_Mesh boxMesh = new HE_Mesh(boxCreator);
 		startMeshes[1] = boxMesh;
 
 		HEC_Cone cone = new HEC_Cone().setFacets(10).setRadius(400).setHeight(600).setSteps(12);
 		HE_Mesh coneMesh = new HE_Mesh(cone);
 		coneMesh.rotateAboutAxis(90, new WB_Point3d(0, 0, 0), new WB_Normal3d(1, 0, 0));
 		startMeshes[2] = coneMesh;
 
 		HEC_Cylinder cyl = new HEC_Cylinder().setFacets(10).setHeight(600).setRadius(300).setSteps(12);
 		HE_Mesh cylMesh = new HE_Mesh(cyl);
 		startMeshes[3] = cylMesh;
 
 		HEC_Dodecahedron dodec = new HEC_Dodecahedron().setEdge(300);
 		HE_Mesh dodecMesh = new HE_Mesh(dodec);
 		HES_PlanarMidEdge subdiv = new HES_PlanarMidEdge();
 		dodecMesh.subdivide(subdiv, 3);
 		startMeshes[4] = dodecMesh;
 	}
 
 	/**
 	 * Creates and adds the observers.
 	 */
 	private void setObservers()
 	{
 		try {
 			// SWIPES
 			gest.getSwipeDetector().getSwipeLeftEvent().addObserver(new SwipeLeftObserver());
 			//gest.getSwipeDetector().getSwipeRightEvent().addObserver(new SwipeRightObserver());
 			//gest.getSwipeDetector().getSwipeUpEvent().addObserver(new SwipeUpObserver());
 			//gest.getSwipeDetector().getSwipeDownEvent().addObserver(new SwipeDownObserver());
 			//gest.getSwipeDetector().setUseSteady(true);
 			
 
 			// WAVE
 			gest.getWaveDetector().getWaveEvent().addObserver(new WaveObserver());
 
 			// CIRCLE
 			gest.getCircleDetector().getCircleEvent().addObserver(new CircleObserver());
 			gest.getCircleDetector().getNoCircleEvent().addObserver(new NoCircleObserver());
 
 			// PUSH
 			gest.getPushDetector().getPushEvent().addObserver(new PushObserver());
 
 			// STEADY
 			gest.getSteadyDetector().getSteadyEvent().addObserver(new SteadyObserver());
 			gest.getSteadyDetector().getNotSteadyEvent().addObserver(new NotSteadyObserver());
 
 			// BODY POSE STUP
 			pd = new BodyPoseDetector("x-pose");
 			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.RIGHT_OF, SkeletonJoint.LEFT_ELBOW);
 			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.LEFT_ELBOW);
 			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.RIGHT_OF, SkeletonJoint.TORSO);
 			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.LEFT_OF, SkeletonJoint.RIGHT_ELBOW);
 			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.RIGHT_ELBOW);
 			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.LEFT_OF, SkeletonJoint.TORSO);
 			bpHandler.addPose(pd);
 
 			pd2 = new BodyPoseDetector("t-pose");
 			pd2.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.LEFT_OF, SkeletonJoint.LEFT_ELBOW);
 			pd2.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.LEFT_ELBOW);
 			pd2.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.LEFT_SHOULDER);
			pd2.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.DISTANCE, 1100, SkeletonJoint.RIGHT_HAND);
 			pd2.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.RIGHT_OF, SkeletonJoint.RIGHT_ELBOW);
 			pd2.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.RIGHT_ELBOW);
 			pd2.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.RIGHT_SHOULDER);
 			bpHandler.addPose(pd2);
 
 			bpHandler.addObserver(new BodyPoseObserver());
 
 			// BUTTONS
 			menuButton.addObserver(new ButtonObserver());
 			modelButton.addObserver(new ButtonObserver());
 			zoomButton.addObserver(new ButtonObserver());
 			selectionButton.addObserver(new ButtonObserver());
 
 			opencv.addObserver(new HandDetectionObserver());
 
 		} catch (StatusException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Processing's PApplet drawing method.
 	 * Gets called every frame.
 	 */
 	public void draw()
 	{
 		background(bgColor);
 		smooth();
 		lights();
 
 		hint(ENABLE_DEPTH_TEST);
 		translate(width/2, height/2, 0);
 		rotateX(radians(180));
 
 		xPos = 0;
 		yPos = 0;
 
 		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
 				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
 				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);
 
 		if (kinect.isTracking())
 		{
 
 			Point3D right = kinect.getRightHandPositionProj();
 			if (right != null)
 			{
 				int xvar = width - 640;
 				int yvar = height - 480;
 				xPos = (int) DetectionHandler.mapTo(right.getX(), 0, 640, 0, width + xvar*2);
 				yPos = (int) DetectionHandler.mapTo(right.getY(), 0, 480, 0, height + yvar*2);
 
 				if (prevPos != null)
 				{
 					Point3D prevProj = kinect.convertRealWorldToProjective(prevPos);
 					xPos = (int) lerp(prevProj.getX(), xPos, 0.3f);
 					yPos = (int) lerp(prevProj.getY(), yPos, 0.3f);
 				}
 
 				if (xPos > width) xPos = width;
 				if (xPos < 0) xPos = 0;
 				if (yPos > height) yPos = height;
 				if (yPos < 0) yPos = 0;
 
 				if (xPos >= width - 200)
 				{
 					modelButton.setAlpha(185);
 					menuButton.setAlpha(185);
 					zoomButton.setAlpha(185);
 				}
 				else
 				{
 					modelButton.setAlpha(70);
 					menuButton.setAlpha(70);
 					zoomButton.setAlpha(70);
 				}
 				
 				if (help.hitTest(xPos, yPos) && help.isActive())
 				{
 					help.setVisible(true);
 				}
 				else
 				{
 					help.setVisible(false);
 				}
 
 			}
 
 			if (!isExploding && !explosionStarting)
 			{
 				drawAxes(1000, true);
 				opencv.update();
 			}
 
 			modeString = "";
 
 			if (isChoosing)
 			{
 				selectionButton.setVisible(true);
 				selectionButton.update(new Point3D(xPos, yPos, 0));
 
 				fill(modelColor.getRed(), modelColor.getGreen(), modelColor.getBlue());
 				stroke(0);
 				strokeWeight(2);
 				render.drawEdges(startMeshes[selectedModel]);
 				render.drawFaces(startMeshes[selectedModel]);
 				modeString = "Swipe left to choose model";
 			}
 			else
 			{
 				menuButton.update(new Point3D(xPos, yPos, 0));
 				modelButton.update(new Point3D(xPos, yPos, 0));
 				zoomButton.update(new Point3D(xPos, yPos, 0));
 
 				if (isNavigation)
 				{
 					drawNavigation();
 				}
 				else if (isZooming)
 				{
 					drawZooming();
 				}
 				else if (isModeling)
 				{
 					drawModeling();
 				}
 				else if (isExploding)
 				{			
 					drawExplosion();
 				}
 				else if (explosionStarting)
 				{
 					modeString = "Wait for it...";
 				}
 
 				if (!isExploding && !explosionStarting)
 				{
 					image = createSkelImage();
 				}
 			}
 
 			if (right != null)
 			{
 				prevPos = kinect.getRightHandPositionReal();
 			}
 		}
 		else
 		{
 			modeString = "Make the Psi-pose to start!";
 		}
 
 		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
 				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
 				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);
 
 		// Normal draw if nothing is selected		
 		if (!isModeling && !explosionDone && !isExploding && !isChoosing)
 		{
 			fill(modelColor.getRed(), modelColor.getGreen(), modelColor.getBlue());
 			stroke(0);
 			strokeWeight(2);
 			render.drawEdges(mesh);
 			render.drawFacesSmooth(mesh);
 			for (HE_Face face : mesh.getFacesAsList())
 			{
 				//System.out.println(face.key());
 				if (face.key() <= 120)
 				{
 					fill(0);
 				}
 				else fill(modelColor.getRed(), modelColor.getGreen(), modelColor.getBlue());
 				render.drawFace(face);
 			}
 		}
 
 		// 2D overlay
 		hint(DISABLE_DEPTH_TEST);
 		camera();
 		noStroke();
 		noFill();
 
 		if (explosionStarting)
 		{
 			for (AnimatedCircle anC : introExplo)
 			{
 				anC.draw(g);
 			}
 		}
 
 		if (!isExploding && !explosionDone)
 		{
 			if (!explosionStarting)
 			{
 				//tint(255, 128);
 				image(image, 0, 0);
 				//noTint();
 			}
 			fill(255);
 			PFont font = createFont("Verdana", 40);
 			textFont(font);
 			text(modeString, image.width + 30, 40);
 			font = createFont("Verdana", 20);
 			textFont(font);
 			rotateSlider.draw(g);
 			modelSlider.draw(g);
 			menuButton.draw(g);
 			modelButton.draw(g);
 			zoomButton.draw(g);
 			selectionButton.draw(g);
 			help.draw(g);
 			noTint();
 			if (!kinect.isTracking())
 			{
 				fill(0);
 				stroke(0);
 				image(poseImage, width/2 - poseImage.width/2, height/2 - poseImage.height/2);
 			}
 			else if (!explosionStarting)
 			{
 				fill(255);
 				noStroke();
 				image(handImage, xPos, yPos);
 			}
 
 			if (explosionStarting)
 			{
 				fill(255);
 				text("File saved as: " + savedFilename, 50, height - 50);
 			}
 		}
 		tposeTimer++;
 		if(explosionDone) System.exit(0);
 		
 	}
 
 	/**
 	 * Draws/Handles the navigation.
 	 */
 	private void drawNavigation()
 	{
 		if (prevPos != null)
 		{
 			Point3D lproj = kinect.getLeftHandPositionProj();
 			if (lproj != null)
 			{
 				rotateSlider.setCurrentSelection(lproj.getY(), height);
 			}
 			Point3D rProj = kinect.getRightHandPositionProj();
 			Point3D pProj = kinect.convertRealWorldToProjective(prevPos);
 
 			if (rightFist && leftFist && rProj != null)
 			{
 				float xDist = (PApplet.map(rProj.getX(), 0, 640, 50, width - 50) 
 						- PApplet.map(pProj.getX(), 0, 640, 50, width - 50)) * rotateSlider.getSliderValue();
 				float yDist = (PApplet.map(rProj.getY(), 0, 480, 50, height - 50) 
 						- PApplet.map(pProj.getY(), 0, 480, 50, height - 50)) * rotateSlider.getSliderValue();
 
 				xDist = xDist / 2;
 				yDist = yDist / 2;
 
 				cam.horizontalRotation(-xDist);
 				cam.verticalRotation(yDist);
 
 				rotateDistanceX = xDist/2;
 				rotateDistanceY = yDist/2;
 			}
 			else
 			{
 				cam.horizontalRotation(-rotateDistanceX);
 				cam.verticalRotation(rotateDistanceY);
 			}
 		}
 	}
 
 	/**
 	 * Draws/Handles the zooming.
 	 */
 	private void drawZooming()
 	{
 		if (prevPos != null)
 		{
 			Point3D lproj = kinect.getLeftHandPositionProj();
 			if (lproj != null)
 			{
 				rotateSlider.setCurrentSelection(lproj.getY(), height);
 			}
 			Point3D rProj = kinect.getRightHandPositionProj();
 
 			if (rightFist && leftFist && rProj != null)
 			{
 				Point3D pProj = kinect.convertRealWorldToProjective(prevPos);
 				float zDist = (PApplet.map(rProj.getZ(), 0, 480, 0, height) 
 						- PApplet.map(pProj.getZ(), 0, 480, 0, height))*rotateSlider.getSliderValue();
 
 				if (zDist >= 0)
 				{
 					cam.zoomOut(zDist);
 				}
 				else
 				{
 					cam.zoomOut(zDist);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws/Handles the modeling.
 	 */
 	private void drawModeling()
 	{
 		// Draw on screen
 		stroke(0);
 		strokeWeight(2);
 		render.drawEdges(mesh);
 		fill(modelColor.getRed(), modelColor.getGreen(), modelColor.getBlue());
 		render.drawFaces(selector, mesh);
 
 		if (prevPos != null)
 		{	
 			Point3D lproj = kinect.getLeftHandPositionProj();
 			if (lproj != null)
 			{
 				modelSlider.setCurrentSelection(lproj.getY(), height);
 			}
 			int maxLvl = (int) PApplet.map(modelSlider.getSliderValue(), 0, 1, 1, 8);
 
 			if (!rightFist)
 			{
 				if (selector.get(xPos, yPos) != null)
 				{
 					selected = mesh.getFaceByKey(selector.get(xPos, yPos));
 				}
 			}
 
 			if(rightFist && leftFist)
 			{
 				resetList();
 				softSelection(selected, 1, maxLvl);
 
 				Integer[] keyList = new Integer[maxLvl];
 				keyList = selectedList.keySet().toArray(keyList);
 				if (!selectedList.isEmpty()){
 					for (int i = 1; i <= keyList.length; ++i)
 					{
 						if (i == 1)
 						{
 							fill(255, 0, 0);
 						}
 						else
 						{
 							fill(255, 99, 71);
 						}
 
 						List<HE_Face> faces = selectedList.get(i);
 						for (HE_Face f : faces)
 						{
 							if (f.key() <= 5) fill(0);
 							render.drawFaceSmooth(f.key(), mesh);
 						}
 					}
 				}
 				double movement = (kinect.getRightHandPositionReal().getZ() - prevPos.getZ())/2;
 
 				if (!selectedList.isEmpty() && selectedList.containsKey(1) && !selectedList.get(1).isEmpty())
 				{
 					WB_Normal3d normal = selectedList.get(1).get(0).getFaceNormal();
 
 					vertMoved.clear();
 					for (int i = 1; i <= keyList.length; ++i)
 					{
 						float mod = DetectionHandler.mapTo(i, 1, 20, 1, 0.1f);
 						moveFace(movement*mod, selectedList.get(i), normal);
 					}
 				}
 			}
 			else
 			{
 				resetList();
 				softSelection(selected, 1, maxLvl);
 
 				Integer[] keyList = new Integer[maxLvl];
 				keyList = selectedList.keySet().toArray(keyList);
 				if (!selectedList.isEmpty()){
 					for (int i = 1; i <= keyList.length; ++i)
 					{
 						if (i == 1)
 						{
 							fill(0, 0, 255);
 						}
 						else
 						{
 							fill(132, 112, 255);
 						}
 
 						List<HE_Face> faces = selectedList.get(i);
 						for (HE_Face f : faces)
 						{
 							if (f.key() <= 5) fill(0);
 							render.drawFaceSmooth(f.key(), mesh);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draw/Handles the explosion.
 	 */
 	private void drawExplosion()
 	{
 		for ( int i = 0; i < voroMesh.length; i++) 
 		{
 			WB_Point3d c = voroMesh[i].getCenter();
 			PVector d = new PVector( (float)c.x,(float)c.y,(float)c.z);
 			d.normalize(); 
 			d.mult((float) (move));
 			voroMesh[i].move(d.x, d.y, d.z);
 			fill(modelColor.getRed(), modelColor.getGreen(), modelColor.getBlue());
 			render.drawFaces(voroMesh[i]);
 			stroke(0);
 			render.drawEdges(voroMesh[i]);
 			//move -= 0.01f;
 		}
 
 		for( int i =0; i < particles.length; i++) {
 			particles[i].update();
 			particles[i].draw(this);
 		}
 		exploCounter++;
 		if (exploCounter >= 75)
 		{
 			explosionDone = true;
 			isExploding = false;
 		}
 	}
 
 	/**
 	 * Moves faces and adds it to the list of moved faces.
 	 * @param move distance to move
 	 * @param faces list of faces to be moved
 	 * @param norm direction of movement
 	 */
 	private void moveFace(double move, List<HE_Face> faces, WB_Normal3d norm)
 	{
 		norm.normalize();
 		for (HE_Face face : faces)
 		{
 			for (HE_Vertex vert : face.getFaceVertices())
 			{		
 				if (!vertMoved.contains(vert))
 				{
 					vert.moveBy(norm.toPoint().mult(move));
 					vertMoved.add(vert);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates the voronoi cells and starts the explosion.
 	 */
 	private void startExplosion()
 	{
 		if (!isExploding && !explosionDone)
 		{
 			explosionStarting = true;
 			isNavigation = false;
 			isModeling = false;
 			isZooming = false;
 			
 			HEMC_VoronoiCells vc = new HEMC_VoronoiCells();
 			vc.setContainer(mesh);
 			float[][] points;
 
 			int numpoints=50;
 			points=new float[numpoints][3];
 			for (int i=0;i<numpoints;i++) {
 				points[i][0]=random(-80, 80);
 				points[i][1]=random(-80, 80);
 				points[i][2]=random(-80, 80);
 			}
 			vc.setPoints(points).setOffset(1);
 			voroMesh = new HE_Mesh[50];
 			modelButton.setVisible(false);
 			menuButton.setVisible(false);
 			rotateSlider.setVisible(false);
 			modelSlider.setVisible(false);
 			zoomButton.setVisible(false);
 			help.setActive(false);
 			help.setVisible(false);
 
 			try
 			{
 				voroMesh = vc.create();
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 
 			explosionStarting = false;
 			isExploding = true;
 		}
 	}
 
 	/**
 	 * Clears the map of selected faces.
 	 */
 	private void resetList()
 	{
 		selectedList.clear();
 		List<HE_Face> fir = new ArrayList<HE_Face>();
 		List<HE_Face> sec = new ArrayList<HE_Face>();
 		List<HE_Face> tri = new ArrayList<HE_Face>();
 		List<HE_Face> fou = new ArrayList<HE_Face>();
 		List<HE_Face> fif = new ArrayList<HE_Face>();
 		List<HE_Face> six = new ArrayList<HE_Face>();
 		List<HE_Face> sev = new ArrayList<HE_Face>();
 		List<HE_Face> eig = new ArrayList<HE_Face>();
 		List<HE_Face> nin = new ArrayList<HE_Face>();
 		List<HE_Face> ten = new ArrayList<HE_Face>();
 
 		selectedList.put(1, fir);
 		selectedList.put(2, sec);
 		selectedList.put(3, tri);
 		selectedList.put(4, fou);
 		selectedList.put(5, fif);
 		selectedList.put(6, six);
 		selectedList.put(7, sev);
 		selectedList.put(8, eig);
 		selectedList.put(9, nin);
 		selectedList.put(10, ten);
 	}
 
 	/**
 	 * Selects faces for soft selection recursively.
 	 * @param startFace face to start with
 	 * @param lvl current selection level
 	 * @param maxLvl maximum selection level
 	 */
 	private void softSelection(HE_Face startFace, int lvl, int maxLvl)
 	{
 		// Recursive selection of neighboring faces
 		if (startFace != null)
 		{
 			if (lvl <= maxLvl)
 			{
 				selectedList.get(lvl).add(startFace);		
 				if (lvl >= 2)
 				{
 					if (selectedList.get(1).get(0) != null) // removes the middle/selected face from other lists for highlighting
 					{
 						selectedList.get(lvl).remove(selectedList.get(1).get(0));
 					}
 				}
 				for (HE_Face face : startFace.getNeighborFaces())
 				{
 					softSelection(face, lvl + 1, maxLvl);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws the axes on screen.
 	 * @param length length of the axes
 	 * @param multidir true if negative axes has to be drawn, false otherwise
 	 */
 	public void drawAxes(int length, boolean multidir)
 	{
 		strokeWeight(1);
 		stroke(0, 255, 0);
 		line(0, 0, 0, length, 0, 0);
 		stroke(255, 0, 0);
 		line(0, 0, 0, 0, length, 0);
 		stroke(0, 0, 255);
 		line(0, 0, 0, 0, 0, length);
 
 		if (multidir)
 		{
 			stroke(85);
 			line(0, 0, 0, -length, 0, 0);
 			line(0, 0, 0, 0, -length, 0);
 			line(0, 0, 0, 0, 0, -length);
 		}
 		noStroke();
 	}
 
 	/**
 	 * Creates a skeletonImage based on skeleton data.
 	 * @return skeleton image as PImage
 	 */
 	public PImage createSkelImage()
 	{
 		PGraphics temp = createGraphics(640, 480, P2D);
 		temp.beginDraw();
 		temp.background(255, 255, 255, 0);
 		temp.stroke(255);
 		temp.fill(255);
 		temp.strokeWeight(10);
 		temp.smooth();
 		PImage ret = new PImage();
 
 		HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> allSkel = kinect.getUserSkels();
 
 		if (allSkel.containsKey(1))
 		{
 			HashMap<SkeletonJoint, SkeletonJointPosition> userSkel = (HashMap<SkeletonJoint, SkeletonJointPosition>) allSkel.get(1);
 
 			Point3D p1 = userSkel.get(SkeletonJoint.HEAD).getPosition();
 			Point3D p2 = userSkel.get(SkeletonJoint.NECK).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p2 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.LEFT_ELBOW).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p2 = userSkel.get(SkeletonJoint.LEFT_HAND).getPosition();
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
 			p2 = userSkel.get(SkeletonJoint.RIGHT_ELBOW).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.RIGHT_HAND).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
 			p2 = userSkel.get(SkeletonJoint.TORSO).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.LEFT_HIP).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p1 = userSkel.get(SkeletonJoint.RIGHT_HIP).getPosition();
 			p1 = kinect.convertRealWorldToProjective(p1);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 			p2 = userSkel.get(SkeletonJoint.LEFT_HIP).getPosition();
 			p2 = kinect.convertRealWorldToProjective(p2);
 			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 
 			temp.noStroke();
 
 			if (leftFist)
 			{
 				temp.fill(0, 255, 0);
 			}
 			else
 			{
 				temp.fill(255, 0, 0);
 			}
 
 			Point3D punt = kinect.getLeftHandPositionProj();
 			if (punt != null)
 			{
 				temp.ellipse(punt.getX(), punt.getY(), 50, 50);
 			}
 
 			if (rightFist)
 			{
 				temp.fill(0, 255, 0);
 			}
 			else
 			{
 				temp.fill(255, 0, 0);
 			}
 
 			Point3D punt2 = kinect.getRightHandPositionProj();
 			if (punt2 != null)
 			{
 				temp.ellipse(punt2.getX(), punt2.getY(), 50, 50);
 			}
 
 			temp.endDraw();
 			ret = temp.get();
 			ret.resize(240, 180);
 		}
 		return ret;
 	}
 
 	/**
 	 * Splits big faces in half.
 	 */
 	private void triangulateBigFaces()
 	{
 		HE_Selection sel = new HE_Selection(mesh);
 		sel.clear();
 		for (HE_Face fac : mesh.getFacesAsList())
 		{
 			if (fac.getFaceArea() > (faceSize.getFaceArea()*1.5))
 			{
 				sel.add(fac);
 			}
 		}
 		
 		try 
 		{
 			mesh.midSplitFaces(sel);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Saves the mesh with an incremental filename.
 	 */
 	private void saveMesh()
 	{
 		String path = "../resources/";
 		File file = new File(path);
 		String[] names = null;
 		String pathname = "";
 		if (file.isDirectory())
 		{
 			names = file.list();
 		}
 
 		String newNum = "001";
 		CharSequence s = "KinectScreenCapture";
 
 		if (names != null)
 		{
 			for (int i = 0; i < names.length; ++i)
 			{
 				String temp = names[i];
 				if (temp.contains(s))
 				{
 					String nummer = temp.substring(19, 22);
 					int num = Integer.valueOf(nummer);
 					num++;
 
 					newNum = "";
 
 					if (num < 10)
 					{
 						newNum += "00";
 					}
 					else if (num < 100)
 					{
 						newNum += "0";
 					}
 					newNum += String.valueOf(num);
 				}
 			}
 		}
 		pathname += path;
 		pathname += s;
 		pathname += newNum;
 		pathname += ".stl";
 		HET_Export.saveToSTL(mesh, pathname, 1);
 		savedFilename = s + newNum + ".stl";
 	}
 
 	//-------------------
 	// OBSERVER CLASSES
 	//-------------------
 
 	/**
 	 * Called when a left swipe is detected.
 	 */
 	public class SwipeLeftObserver implements IObserver<VelocityAngleEventArgs>
 	{
 		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {
 
 			if (kinect.isTracking() && isChoosing)
 			{
 				selectedModel++;
 				if (selectedModel > 4)
 				{
 					selectedModel = 0;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when right swipe is detected.
 	 */
 	public class SwipeRightObserver implements IObserver<VelocityAngleEventArgs>
 	{
 		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {
 
 			if (kinect.isTracking() && isChoosing)
 			{
 				selectedModel--;
 				if (selectedModel < 0)
 				{
 					selectedModel = 4;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when swipe up is detected.
 	 */
 	class SwipeUpObserver implements IObserver<VelocityAngleEventArgs>
 	{
 		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {
 
 		}
 	}
 
 	/**
 	 * Called when swipe down is detected.
 	 */
 	class SwipeDownObserver implements IObserver<VelocityAngleEventArgs>
 	{
 		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {
 
 		}
 	}
 
 	/**
 	 * Called when wave gesture is detected.
 	 */
 	public class WaveObserver implements IObserver<NullEventArgs>
 	{
 		public void update(IObservable<NullEventArgs> arg0, NullEventArgs arg1) {
 
 			System.out.println("WAVE");
 		}
 	}
 
 	/**
 	 * Called when circle gesture is detected.
 	 */
 	public class CircleObserver implements IObserver<CircleEventArgs>
 	{
 		public void update(IObservable<CircleEventArgs> arg0, CircleEventArgs arg1) {
 
 			System.out.println("CIRCLE");		
 		}
 	}
 
 	/**
 	 * Called when no circle gesture is detected.
 	 * Prints the gesture to console.
 	 */
 	public class NoCircleObserver implements IObserver<NoCircleEventArgs>
 	{
 		public void update(IObservable<NoCircleEventArgs> arg0,
 				NoCircleEventArgs arg1) {
 			System.out.println("no circle");
 		}
 	}
 
 	/**
 	 * Called when push/click gesture is detected.
 	 */
 	public class PushObserver implements IObserver<VelocityAngleEventArgs>
 	{
 		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {
 
 		}
 	}
 
 	/**
 	 * Called when steady gesture is detected.
 	 */
 	public class SteadyObserver implements IObserver<IdValueEventArgs>
 	{
 		public void update(IObservable<IdValueEventArgs> arg0,
 				IdValueEventArgs arg1) {
 			//	steady = true;
 		}
 	}
 
 	/**
 	 * Called when not steady gesture is detected.
 	 */
 	public class NotSteadyObserver implements IObserver<IdValueEventArgs>
 	{
 		public void update(IObservable<IdValueEventArgs> arg0,
 				IdValueEventArgs arg1) {
 			//	steady = false;
 		}
 	}
 
 	/**
 	 * Called when a body pose is detected.
 	 */
 	public class BodyPoseObserver implements IObserver<BodyPoseEventArgs>
 	{
 		public void update(IObservable<BodyPoseEventArgs> arg0,
 				BodyPoseEventArgs arg1) {
			System.out.println("body pose: " + arg1.getPose().getName());
 			if (kinect.isTracking() && (isNavigation || isModeling || isZooming))
 			{
 				if (arg1.getPose() == pd)
 				{
 					saveMesh();
 					startExplosion();
 				}
 
				if (arg1.getPose() == pd2 && tposeTimer > 90)
 				{
 					triangulateBigFaces();
 					tposeTimer = 0;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when a button click is detected.
 	 */
 	public class ButtonObserver implements IObserver<ButtonEventArgs>
 	{
 		public void update(IObservable<ButtonEventArgs> arg0,
 				ButtonEventArgs arg1) {
 
 			if (kinect.isTracking())
 			{
 				if (arg1.getButton().getName().equals(modelButton.getName()))
 				{
 					System.out.println("modeling");
 					isNavigation = false;
 					isModeling = true;
 					isZooming = false;
 					rotateSlider.setVisible(false);
 					modelSlider.setVisible(true);
 					menuButton.setSelected(false);
 					zoomButton.setSelected(false);
 					bgColor = 135;
 				}
 				else if (arg1.getButton().getName().equals(menuButton.getName()))
 				{
 					System.out.println("navigation");
 					isNavigation = true;
 					isModeling = false;
 					isZooming = false;
 					rotateSlider.setVisible(true);
 					modelSlider.setVisible(false);
 					modelButton.setSelected(false);
 					zoomButton.setSelected(false);
 					bgColor = 135;
 				}
 				else if (arg1.getButton().getName().equals(zoomButton.getName()))
 				{
 					System.out.println("zooming");
 					isZooming = true;
 					isNavigation = false;
 					isModeling = false;
 					rotateSlider.setVisible(false);
 					modelSlider.setVisible(false);
 					modelButton.setSelected(false);
 					menuButton.setSelected(false);
 					bgColor = 135;
 				}
 				else if (arg1.getButton().getName().equals(selectionButton.getName()))
 				{
 					System.out.println("select");
 					isChoosing = false;
 					selectionButton.setVisible(false);
 					zoomButton.setVisible(true);
 					menuButton.setVisible(true);
 					modelButton.setVisible(true);
 					mesh = startMeshes[selectedModel];
 
 					double size = 0;
 					for (HE_Face fac : mesh.getFacesAsList())
 					{
 						if (fac.getFaceArea() >= size)
 						{
 							size = fac.getFaceArea();
 							faceSize = fac;
 						}
 					}
 					
 					help.setActive(true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when the hand detection data updates.
 	 */
 	public class HandDetectionObserver implements IObserver<HandDetectionEventArgs>
 	{
 		@Override
 		public void update(IObservable<HandDetectionEventArgs> arg0, HandDetectionEventArgs arg1) 
 		{
 			if (arg1.getHand().equals("left"))
 			{
 				if (arg1.isFist())
 				{
 					leftFist = true;
 				}
 				else leftFist = false;
 			}
 			else if (arg1.getHand().equals("right"))
 			{
 				if (arg1.isFist())
 				{
 					rightFist = true;
 				}
 				else rightFist = false;
 			}
 		}
 	}
 }
 
