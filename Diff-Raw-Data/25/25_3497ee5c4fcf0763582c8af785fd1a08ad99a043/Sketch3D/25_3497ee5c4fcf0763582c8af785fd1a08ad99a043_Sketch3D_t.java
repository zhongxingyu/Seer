 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javax.media.j3d.Alpha;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Vector3d;
 
 public class Sketch3D implements PoseUpdatedNotification, MouseListener {
 	//public final static String COMPONENT_DIRECTORY = System.getProperty("user.dir") + File.separator + "libs" + File.separator + "ubitrack" + File.separator + "bin" + File.separator + "ubitrack";
 	public final static String DATAFLOW_PATH = System.getProperty("user.dir") + File.separator + "dataflow" + File.separator + "3D-UI-SS-2010-Markertracker.dfg";
 
 	//paths for felix, comment if not needed
 	public final static String COMPONENT_DIRECTORY = System.getProperty("user.dir") + File.separator + "libs" + File.separator + "ubitrack";// + File.separator + "bin" + File.separator + "ubitrack";
 	
 	public static final String WINDOW_TITLE = "Sketch3D";
 
 	private UbitrackFacade ubitrackFacade;
 
 	/** Pose receiver for the editing volume marker. */
 	private PoseReceiver poseReceiverEditingVolume;
 
 	/** Pose receiver for the pen marker. */
 	private PoseReceiver poseReceiverPen;
 
 	/** The main paint controller. */
 	private static PaintController paintController;
 	
 	private boolean isDrawing = false;
 
 	private EditingVolume editingVolume = null;
 
 	public static final double EDITING_VOLUME_RADIUS = 1;
 
 	private Vector3d latestPenTranslation = null;
 	private Vector3d latestEditingVolumeTranslation = null;
 	//private Vector3d latestPixel = null;
 	private LinkedList<Vector3d> latestPixels = null;
 
 	private ImageReceiver imageReceiver;
 	private Viewer viewer;
 
 	private Transform3D latestEditingVolumeTransformation;
 
 	private Transform3D latestPenTransformation;
 
 	public static PaintController getPaintController() {
 		return paintController;
 	}
 
 	public Sketch3D() {
 		ubitrackFacade = new UbitrackFacade();
 		latestPixels = new LinkedList<Vector3d>();
 		initializeJava3D();
 		initializeUbitrack();
 		linkUbitrackToViewer();
 		initSketchStuff();
 	}
 
 	private void initSketchStuff() {
 		paintController = new PaintController();
 		this.editingVolume = new EditingVolume(viewer);
 	}
 
 	public static void main(String[] args) {
 		Sketch3D sketch3D = new Sketch3D();	
 	}
 
 	private void initializeUbitrack() {
 		ubitrackFacade.initUbitrack();
 
 		poseReceiverEditingVolume = new PoseReceiver(this, 1);	
 		if (!ubitrackFacade.setPoseCallback("posesink", poseReceiverEditingVolume)) {
 			return;
 		}
 
 		poseReceiverPen = new PoseReceiver(this, 2);
 		if (!ubitrackFacade.setPoseCallback("posesink2", poseReceiverPen)) {
 			return;
 		}
 
 		imageReceiver = new ImageReceiver();
 		if (!ubitrackFacade.setImageCallback("imgsink", imageReceiver)) {
 			return;
 		}
 		ubitrackFacade.startDataflow();
 	}
 
 	private void linkUbitrackToViewer() {
 		BackgroundObject backgroundObject = new BackgroundObject();
 		viewer.addObject(backgroundObject);
 		imageReceiver.setBackground(backgroundObject.getBackground());
 	}
 
 	private void initializeJava3D() {
 		System.out.println("Creating Viewer - " + WINDOW_TITLE);
 		viewer = new Viewer(WINDOW_TITLE, ubitrackFacade);
 
 		Transform3D cameraTransform = new Transform3D();
 		cameraTransform.setTranslation(new Vector3d(0, 0, 0));
 		TransformGroup cameraTG = viewer.getCameraTransformGroup();
 		cameraTG.setTransform(cameraTransform);
 		
 		viewer.canvas3D.addMouseListener(this);
 
 		System.out.println("Done");
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		isDrawing = true;
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		isDrawing = false;
 	}
 
 	@Override
 	public void handlePoseUpdatedNotification(PoseReceiver poseReceiver) {
 		//System.out.println("Update notification from marker " + poseReceiver.getTag() + " -- pos: " + poseReceiver.getTranslationVector());
 
 		switch (poseReceiver.getTag()) {
 		case 1:
 			latestEditingVolumeTranslation = poseReceiver.getTranslationVector();
 
 			latestEditingVolumeTransformation = poseReceiver.getTransformation();
 			//			AxisAngle4d aa = new AxisAngle4d();
 			//			aa.set(poseReceiver.getRotationQuaternion());
 			//			System.out.println(aa);
 
 			editingVolume.updateLocation(latestEditingVolumeTranslation, poseReceiver.getRotationQuaternion());
 			break;
 
 		case 2:
 			latestPenTranslation = poseReceiver.getTranslationVector();
 			latestPenTransformation = poseReceiver.getTransformation();
 			break;
 
 		default:
 			throw new RuntimeException("Unknown tag value for poseReceiver");
 		}
 
 //		 && paintController.shouldDraw(latestPenTranslation, latestEditingVolumeTranslation, EDITING_VOLUME_RADIUS)
 		if (isDrawing) {
 			//Vector3d drawCoords = paintController.getDrawCoords(latestPenTranslation, latestEditingVolumeTranslation);
 			Transform3D drawCoords = paintController.getDrawCoords(latestPenTransformation, latestEditingVolumeTransformation);
 			//System.out.println("Drawing at coords " + drawCoords);
 			
 			if (latestPixels.size() > 5){
 				latestPixels.removeFirst();
 			}
 
 			Vector3d distPixels = new Vector3d();
 //			for (Vector3d pixel : latestPixels) {
 //				distPixels.sub(pixel, drawCoords);
 //				
 //				//dont draw very near pixels
 //				if (distPixels.length()<0.002)
 //					return;
 //			}
 			
 			editingVolume.drawDot(drawCoords);
 			
 //			if (!latestPixels.isEmpty()){
 //				distPixels.sub(latestPixels.getLast(), drawCoords);
 //				double step = 0.01/distPixels.length();
 //				//System.out.println("dist between pixels: " + distPixels.length());
 //				//System.out.println("step " + step);
 //				if (distPixels.length()>0.02){
 //					//draw pixels in between --> interpolate
 //					for (double i = 0; i<1; i = i+step){
 //						Vector3d tmp = new Vector3d(distPixels);
 //						tmp.scale(i);
 //						tmp.add(drawCoords);
 //						editingVolume.drawDot(tmp);
 //						//System.out.println("draw interpolated pixel " + tmp);
 //					}
 //				}
 //			}
 			
 //			latestPixels.addLast(drawCoords);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
