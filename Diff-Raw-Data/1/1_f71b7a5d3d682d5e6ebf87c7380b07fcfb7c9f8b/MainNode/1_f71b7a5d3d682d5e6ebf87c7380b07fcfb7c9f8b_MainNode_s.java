 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ex4;
 
 import ex3.PRM;
 import ex3.PRMUtil;
 import ex3.Vertex;
 import geometry_msgs.Point;
 import geometry_msgs.Pose;
 import geometry_msgs.PoseStamped;
 import geometry_msgs.PoseWithCovarianceStamped;
 import geometry_msgs.Twist;
 import java.awt.Dimension;
 import java.awt.geom.Path2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import launcher.RunParams;
 import nav_msgs.OccupancyGrid;
 import nav_msgs.Odometry;
 import org.ros.message.MessageFactory;
 import org.ros.message.MessageListener;
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
 import org.ros.node.NodeConfiguration;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 import pf.AbstractLocaliser;
 import std_msgs.Float32MultiArray;
 import std_msgs.Int32;
 import visualization_msgs.Marker;
 import visualization_msgs.MarkerArray;
 
 /**
  *
  * @author HammyG
  */
 public class MainNode extends AbstractNodeMain {
 
     private enum Phase {
 
         INITIALISATION,
         FINDROOM,
         SCANNINGROOM,
         FACECHECKINROOM,
         EXPLORING,
         FACECHECK,
         ROTATETOPERSON,
         PRMTOPERSON,
         PRMTOROOM,
         INMEETINGROOM,
         COMPLETED
     }
     public static Double INITIAL_EXPLORATION_GRID_STEP = RunParams.getDouble("INITIAL_EXPLORATION_GRID_STEP");
     public static Double MINIMUM_EXPLORATION_GRID_STEP = RunParams.getDouble("MINIMUM_EXPLORATION_GRID_STEP");
     public static Double GRID_SIZE_REDUCTION_STEP = RunParams.getDouble("GRID_SIZE_REDUCTION_STEP");
     public static Integer FACE_CONFIRM_DETECTIONS = RunParams.getInt("FACE_CONFIRM_DETECTIONS");
     public static Double MAX_RECTANGLE_CENTRE_DISPARITY = RunParams.getDouble("MAX_RECTANGLE_CENTRE_DISPARITY");
     public static Double MAX_RECTANGLE_DEPTH_DISPARITY = RunParams.getDouble("MAX_RECTANGLE_DEPTH_DISPARITY");
     public static Double FACE_CENTRED_THRESHOLD = RunParams.getDouble("FACE_CENTRED_THRESHOLD");
     public static Double INITIAL_EXPLORATION_CELL_SIZE = RunParams.getDouble("INITIAL_EXPLORATION_CELL_SIZE");
     public static Double MINIMUM_EXPLORATION_CELL_SIZE = RunParams.getDouble("MINIMUM_EXPLORATION_CELL_SIZE");
     public static Double CELL_SIZE_REDUCTION_STEP = RunParams.getDouble("CELL_SIZE_REDUCTION_STEP");
     public static Integer EXPLORATION_TARGET_PER_CELL = RunParams.getInt("EXPLORATION_TARGET_PER_CELL");
     public static String EXPLORATION_SAMPLING = RunParams.get("EXPLORATION_SAMPLING");
     private float[] lastCameraData;
     private Pose lastEstimatedPose;
     public double currentGridStep = INITIAL_EXPLORATION_GRID_STEP;
     public double currentCellSize = INITIAL_EXPLORATION_CELL_SIZE;
     private Phase currentPhase = Phase.INITIALISATION;
     private int pplCount = 0;
     private int targetPplCount = 3;
     public ArrayList<Vertex> explorationVertices;
     public int faceCheckCount;
     RectangleWithDepth lastFaceRectangle;
     public static MessageFactory messageFactory;
     public static Dimension CAMERA_DIMENSIONS = new Dimension(640, 480);
     protected Driver driver;
     public PRMUtil prmUtil;
     public OccupancyGrid map;
     private Polygon2D[] meetingRooms;
     private PoseStamped[] centreOfMeetingRooms;
     private int meetingRoomIndex = -1;
     private double turnRemaining = 0;
     Publisher<Twist> twist_pub;
     Publisher<PoseStamped> goal;
     Subscriber<Odometry> odom;
     Subscriber<std_msgs.Int32> prmInfo;
     Subscriber<std_msgs.Float32MultiArray> cameraRectSubscriber;
     Subscriber<PoseWithCovarianceStamped> estimatedPose;
     Subscriber<OccupancyGrid> mapSub;
     Publisher<std_msgs.Bool> navActivePub;
     Publisher<MarkerArray> explorationMarkerPub;
 
     int a = 1; // DEBUGGING. PLEASE DELETE
 
     @Override
     public void onStart(ConnectedNode connectedNode) {
         //set up the message factory
         messageFactory = NodeConfiguration.newPrivate().getTopicMessageFactory();
         StaticMethods.messageFactory = messageFactory;
 
         //set publisher for moving the robot
         twist_pub = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
 
         // Publisher for the goal which prm shall use
         goal = connectedNode.newPublisher("goal", PoseStamped._TYPE);
 
         // Publisher to activate or deactivate navigator.
         navActivePub = connectedNode.newPublisher("nav_active", std_msgs.Bool._TYPE);
 
         explorationMarkerPub = connectedNode.newPublisher("exp_vert", MarkerArray._TYPE);
         explorationMarkerPub.setLatchMode(true);
         //instantiate the driver with the twist publisher
         driver = new Driver(twist_pub);
 
         // This section is initialising the position of the rooms and the rectangles of the room 
         // We are also hard coding the centre of the rooms
         meetingRooms = new Polygon2D[2];
         meetingRooms[0] = new Polygon2D(new double[]{16.115, 20.435, 18.427, 13.835}, new double[]{15.175, 10.499, 8.38, 12.992}, 4);
         meetingRooms[1] = new Polygon2D(new double[]{11.993, 15.885, 13.793, 9.703}, new double[]{19.136, 15.258, 12.987, 17.096}, 4);
 
         PoseStamped centreMeetingRoomOne = messageFactory.newFromType(PoseStamped._TYPE);
         PoseStamped centreMeetingRoomTwo = messageFactory.newFromType(PoseStamped._TYPE);
         centreMeetingRoomOne.getPose().getPosition().setX(17.1);
         centreMeetingRoomOne.getPose().getPosition().setY(11.75);
         centreMeetingRoomTwo.getPose().getPosition().setX(12.4);
         centreMeetingRoomTwo.getPose().getPosition().setY(16.6);
 
         centreOfMeetingRooms = new PoseStamped[2];
         centreOfMeetingRooms[0] = centreMeetingRoomOne;
         centreOfMeetingRooms[1] = centreMeetingRoomTwo;
 
         odom = connectedNode.newSubscriber("odom", Odometry._TYPE);
         odom.addMessageListener(new MessageListener<Odometry>() {
 
             @Override
             public void onNewMessage(Odometry t) {
                 driver.onNewOdomMessage(t);
 
                 if (currentPhase == Phase.SCANNINGROOM) {
                     if (driver.isTargetReached()) {
                         Printer.println("Driver rotation target reached", "REDB");
                         Printer.println("Room is free. Exploring for humans!");
                         initialiseExploration();
                     }
                 }
 
 //                if (lastEstimatedPose == null){
 //                    return;
 //                }
 //                double currentHeading = getHeadingFromLastPos();
 //                Printer.println("Current heading is " + currentHeading, "REDF");
 //                if (a == 1 || driver.isTargetReached()) {
 //                    a = 2;
 //                    Printer.println("Initialisation rotation test...", "REDF");
 //                    int angle = -360;
 //                    currentHeading = getHeadingFromLastPos();
 //                    driver.turn(currentHeading, Math.toRadians(angle));
 //                }
 
 //                if (currentPhase == Phase.SCANNINGROOM) {
 //                    if (turnRemaining >= 0) {
 //                        Printer.println("Got initial pose... Initialising exploratio", "CYANF");
 //
 //                    }
 //                }
             }
         });
 
         prmInfo = connectedNode.newSubscriber("goalInfo", std_msgs.Int32._TYPE);
         prmInfo.addMessageListener(new MessageListener<Int32>() {
 
             @Override
             public void onNewMessage(Int32 t) {
                 if (t.getData() == PRM.GOAL_REACHED) {
                     if (currentPhase == Phase.FINDROOM) {
                         //start scanning the room (360);
                         scanRoomForFace();
                     } else if (currentPhase == Phase.EXPLORING) {
                         if (explorationVertices.size() > 0) {
                             goToNextExplorationVertex();
                         } else {
                             // Exploration path done. Let's go again, but increase
                             // the granularity
                             if (EXPLORATION_SAMPLING.equals("cell")) {
                                 if (currentCellSize > MINIMUM_EXPLORATION_CELL_SIZE){
                                     currentCellSize -= CELL_SIZE_REDUCTION_STEP;
                                 }
                             } else if (EXPLORATION_SAMPLING.equals("grid")) {
                                 if (currentGridStep > MINIMUM_EXPLORATION_GRID_STEP){
                                     currentGridStep -= GRID_SIZE_REDUCTION_STEP;
                                 }
                             }
                             initialiseExploration();
                         }
                     } else if (currentPhase == Phase.PRMTOPERSON) {
                         //Ask person if they want to go to meeting room, if yes prm to room else if no turn and continue exploring
                         Printer.println("Person accepted invite, PRMing to meeting room", "CYANF");
                         prmToMeetingRoom();
                     } else if (currentPhase == Phase.PRMTOROOM) {
                         // we are at the room now, drop person off and tell them they are at meeting room and then begin exploring again
                         Printer.println("Currently in a room, returning to exploration", "CYANF");
                         pplCount++;
                         // Before leaving meeting room check if the task is complete, if so then print statement else continue
                         if (isTaskComplete()) {
                             Printer.println("I have completed the whole task", "GREENB");
                             currentPhase = Phase.COMPLETED;
                         } else {
                             returnToExploration();
                         }
                     }
                 } else if (t.getData() == PRM.NO_PATH) {
                     // If we can't find a route to the exploration node, give up
                     // and go to the next one.
                     if (currentPhase == Phase.EXPLORING) {
                         Printer.println("PRM could not find path. Popping next vertex.", "REDF");
                         goToNextExplorationVertex();
                     }
                 } else if (t.getData() == PRM.PATH_FOUND) {
                     System.out.println("Path found.");
                 }
             }
         });
 
         mapSub = connectedNode.newSubscriber("inflatedMap", OccupancyGrid._TYPE);
         mapSub.addMessageListener(new MessageListener<OccupancyGrid>() {
             @Override
             public void onNewMessage(OccupancyGrid t) {
                 if (currentPhase == Phase.INITIALISATION) {
                     Printer.println("Got map in MainNode", "CYANF");
                     map = t;
                     prmUtil = new PRMUtil(new Random(), messageFactory, map);
                 }
             }
         });
 
         //set up subscriber for the rectangles from the opencv node. Messages
         // are published even if there are no faces detected, but they will be
         // empty (length 0)
         cameraRectSubscriber = connectedNode.newSubscriber("face_rects", std_msgs.Float32MultiArray._TYPE);
         cameraRectSubscriber.addMessageListener(new MessageListener<Float32MultiArray>() {
 
             @Override
             public void onNewMessage(Float32MultiArray t) {
                 onNewCameraRectanglePoints(t.getData());
                 if (t.getData().length != 0) {
                     if (currentPhase == Phase.SCANNINGROOM) {
                         Printer.println("Scanning room found face. Pausing and investigating face", "REDF");
                         driver.pauseTurning();
                         currentPhase = Phase.FACECHECKINROOM;
                     }
 
                     if (currentPhase == Phase.EXPLORING) {
                         std_msgs.Bool deactivate = navActivePub.newMessage();
                         deactivate.setData(false);
                         navActivePub.publish(deactivate);
                         currentPhase = Phase.FACECHECK;
                     }
                     Printer.println("Face seen. Stopping and investigating face", "CYANF");
                 }
 
                 if (currentPhase == Phase.FACECHECK || currentPhase == Phase.FACECHECKINROOM) { // if we are checking faces
                     // if we have not received enough messages to confirm a face
                     if (faceCheckCount < FACE_CONFIRM_DETECTIONS) {
                         if (t.getData().length == 0) {
                             // If we receive a zero length array while we are
                             // attempting to confirm a face, we abandon the check.
 
                             if (currentPhase == Phase.FACECHECK) {
                                 Printer.println("Lost face (no faces). Returning to exploration", "CYANF");
                                 returnToExploration();
                             } else if (currentPhase == Phase.FACECHECKINROOM) {
                                 Printer.println("Lost face (no faces). Continuing to scan room", "CYANF");
                                 returnToScanningRoom();
                             }
                         }
                         RectangleWithDepth newFaceRect = findPerson(lastFaceRectangle);
                         // Check whether the rectangle received is close to the
                         // one we received in the previous message.
                         if (newFaceRect != null && (lastFaceRectangle == null || rectangleOverlapValid(lastFaceRectangle, newFaceRect))) {
                             faceCheckCount++;
                             lastFaceRectangle = newFaceRect;
                             Printer.println("Face matches last seen. FaceCheckCount=" + faceCheckCount, "CYANF");
                         } else {
                             // If the rectangles are too dissimilar, we return to
                             // the exploration phase
                             Printer.println("Lost face (dissimilar). Returning to exploration", "CYANF");
                             if (currentPhase == Phase.FACECHECKINROOM) {
                                 returnToScanningRoom();
                             } else if (currentPhase == Phase.FACECHECK) {
                                 returnToExploration();
                             }
 
                             return;
                         }
                     }
 
                     // If we have made enough detections to confirm a face,
                     // set phase to rotate to person
                     if (faceCheckCount == FACE_CONFIRM_DETECTIONS) {
                         faceCheckCount = 0;
                         if (currentPhase == Phase.FACECHECKINROOM) {
                             if (meetingRoomIndex < meetingRooms.length) {
                                 findEmptyRoom();
                                 Printer.println("Face confirmed, therefore meeting room is not empty. Finding new meeting room.", "CYANF");
                             } else {
                                 Printer.println("Face confirmed, therefore meeting room is not empty. No more rooms remaining.", "REDB");
                             }
                         } else if (currentPhase == Phase.FACECHECK) {
                             currentPhase = Phase.ROTATETOPERSON;
                             rotateTowardsPerson(findPerson(lastFaceRectangle));
                             Printer.println("Face confirmed. Rotating to person", "CYANF");
                         }
                     }
                 }
 
                 if (currentPhase == Phase.ROTATETOPERSON) {
                     if (t.getData().length == 0) {
                         Printer.println("Person lost while rotating - returning to exploration.", "CYANF");
                         driver.stopTurning();
                         returnToExploration();
                         return;
                     }
 
                     // If we've not yet reached the target angle, then return.
                     // note that the rotation is done in small increments, so
                     // the initial target is not necessarily the full rotation to
                     // the heading which faces the person.
                     if (!driver.isTargetReached()) {
                         return;
                     }
                     if (isFaceCentred(lastFaceRectangle)) {
                         Printer.println("Face in centre. PRMing to person", "CYANF");
                         currentPhase = Phase.PRMTOPERSON;
                         setPRMGoal(getObjectLocation(lastEstimatedPose, lastFaceRectangle.depth));
                     } else {
                         RectangleWithDepth rect = findPerson(lastFaceRectangle);
                         if (rect != null){
                             Printer.println("Face rectangle received was null. Returning to exploration.", "CYANF");
                         } else {
                             Printer.println("Face not in centre. Rotating towards person again", "CYANF");
                             rotateTowardsPerson(rect);
                         }
                     }
                 }
             }
         });
 
         //set the subscriber for the estimated pose
         estimatedPose = connectedNode.newSubscriber("amcl_pose", PoseWithCovarianceStamped._TYPE);
         estimatedPose.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
 
             @Override
             public void onNewMessage(PoseWithCovarianceStamped message) {
                 lastEstimatedPose = StaticMethods.copyPose(message.getPose().getPose());
                 
                 if (currentPhase == Phase.INITIALISATION && map != null) {
                     //driver.onNewEstimatedPose(lastEstimatedPose);
                     findEmptyRoom();
                     //initialiseExploration();
                 }
 
             }
         });
         Printer.println("MainNode initialised", "CYANF");
     }
 
     public double getHeadingFromLastPos() {
         return AbstractLocaliser.getHeading(lastEstimatedPose.getOrientation());
     }
 
     public void findEmptyRoom() {
         Printer.println("Kicking off find-room phase","CYANF");
         currentPhase = Phase.FINDROOM;
         meetingRoomIndex++;
         if (PRMUtil.getEuclideanDistance(centreOfMeetingRooms[0].getPose().getPosition(), lastEstimatedPose.getPosition()) >
                 PRMUtil.getEuclideanDistance(centreOfMeetingRooms[1].getPose().getPosition(), lastEstimatedPose.getPosition())){
             Printer.println("Room 1 was closer than room 0. Going to room 1.", "CYANF");
             PoseStamped tmp = centreOfMeetingRooms[0];
             centreOfMeetingRooms[0] = centreOfMeetingRooms[1];
             centreOfMeetingRooms[1] = tmp;
         }
         if (meetingRoomIndex >= centreOfMeetingRooms.length) {
             // No more free rooms! Panic
             Printer.println("NO FREE ROOMS! Exiting :(", "REDB");
             System.exit(0);
         }
         setPRMGoal(centreOfMeetingRooms[meetingRoomIndex]);
     }
 
     public void scanRoomForFace() {
         Printer.println("Scanning room for face","CYANF");
         currentPhase = Phase.SCANNINGROOM;
         turnRemaining = Math.PI * 2;
         driver.turn(getHeadingFromLastPos(), turnRemaining);
     }
 
     public void returnToScanningRoom() {
         //driver.turn(turnRemaining, false, false);
         //driver.turn(getHeadingFromLastPos(), turnRemaining);
         currentPhase = Phase.SCANNINGROOM;
         driver.resumeTurning();
         lastFaceRectangle = null;
         faceCheckCount = 0;
     }
 
     /*
      * Returns the node state to exploration phase from the facecheck phase
      */
     public void returnToExploration() {
         std_msgs.Bool activate = navActivePub.newMessage();
         activate.setData(true);
         navActivePub.publish(activate);
         currentPhase = Phase.EXPLORING;
         lastFaceRectangle = null;
         faceCheckCount = 0;
     }
 
     /*
      * Called when we have received a map in order to initialise the structures
      * needed to perform exploration of the map.
      */
     public void initialiseExploration() {
         ArrayList<Vertex> vertices = null;
         if (EXPLORATION_SAMPLING.equals("grid")) {
             vertices = prmUtil.gridSample(map,
                     currentGridStep, currentGridStep);
         } else if (EXPLORATION_SAMPLING.equals("cell")) {
             vertices = prmUtil.cellSample(map,
                     currentCellSize, EXPLORATION_TARGET_PER_CELL);
         }
 
         explorationVertices = getExplorationPath(lastEstimatedPose, vertices);
         removeVerticesInMeetingRooms(explorationVertices);
         currentPhase = Phase.EXPLORING;
 
         MarkerArray markers = explorationMarkerPub.newMessage();
         Marker m = prmUtil.makePathMarker(explorationVertices, "expvert", null, 23);
         ArrayList<Marker> mlist = new ArrayList<Marker>();
         mlist.add(m);
         markers.setMarkers(mlist);
         explorationMarkerPub.publish(markers);
         goToNextExplorationVertex();
         Printer.println("Exploratio initialised and markers published", "CYANF");
     }
 
     public ArrayList<Vertex> getExplorationPath(Pose startPose, ArrayList<Vertex> vertices) {
         ArrayList<Vertex> explorePath = new ArrayList<Vertex>(vertices.size());
         Point curPoint = startPose.getPosition();
 
         while (vertices.size() > 0) {
             int minIndex = -1;
             double minDist = Double.MAX_VALUE;
             for (int i = 0; i < vertices.size(); i++) {
                 double thisDist = PRMUtil.getEuclideanDistance(curPoint, vertices.get(i).getLocation());
                 if (thisDist < minDist) {
                     minIndex = i;
                     minDist = thisDist;
                 }
             }
             Vertex curVertex = vertices.remove(minIndex);
             curPoint = curVertex.getLocation();
             explorePath.add(curVertex);
         }
         return explorePath;
     }
 
     /*
      * Send a goal message to the PRM containing the next position that the
      * explorer should go to.
      */
     public void goToNextExplorationVertex() {
         Printer.println("Going to next exploration vertex.", "REDF");
         Vertex nextVertex = this.explorationVertices.remove(0);
         PoseStamped goalPose = messageFactory.newFromType(PoseStamped._TYPE);
         goalPose.getPose().setPosition(nextVertex.getLocation());
         setPRMGoal(goalPose);
     }
 
     /**
      * Removes exploration vertices which exist within the meeting room as meeting room does not need to be explored
      * counter used to tell us how many have been removed
      */
     public void removeVerticesInMeetingRooms(ArrayList<Vertex> vertices) {
         int removedCounter = 0;
         for (int i = vertices.size() - 1; i >= 0; i--) {
             // if vertex is within meeting room, then remove the vertex so it is not explored   
             Vertex nextVertex = vertices.get(i);
             for (int j = 0; j < meetingRooms.length; j++) {
                 if (meetingRooms[j].contains(nextVertex.getLocation().getX(), nextVertex.getLocation().getY())) {
                     vertices.remove(i);
                     removedCounter++;
                     // A vertex can only be in a single meeting room (assuming 
                     // non-overlapping rooms), so don't bother checking other ones
                     break;
                 }
             }
         }
         Printer.println("removed " + removedCounter + " vertices found inside meeting rooms", "CYANF");
     }
 
     /**
      * Change current phase to prm to meeting room
      * set the PRM goal to be the location of the empty meeting room
      */
     private void prmToMeetingRoom() {
         currentPhase = Phase.PRMTOROOM;
         setPRMGoal(centreOfMeetingRooms[meetingRoomIndex]);
     }
     /*
     public void start() {
     meetingRoomLocation = messageFactory.newFromType(PoseStamped._TYPE);
     while (meetingRoomLocation == null) {
     //find room
     }
     while (currentPhase != Phase.COMPLETED) {
     currentPhase = Phase.EXPLORING;
     RectangleWithDepth areaOfPerson = findPerson(null);
     double areaCenterX = areaOfPerson.getCenterX();
     double fromCenterX = areaCenterX - (CAMERA_DIMENSIONS.width / 2);
     double turnAngle = Math.toRadians(10);
     while (Math.abs(fromCenterX) > 10) {
     if (fromCenterX > 0) {
     //rectangle on the right
     driver.turn(-turnAngle, true, true);
     } else {
     //rectangle on left
     driver.turn(turnAngle, true, true);
     }
     areaOfPerson = findPerson(null);
     }
     currentPhase = Phase.PRMTOPERSON;
     Pose estimatedPoseCopy = StaticMethods.copyPose(lastEstimatedPose);
     PoseStamped personLocation = getObjectLocation(estimatedPoseCopy, areaOfPerson.depth);
     
     setPRMGoal(personLocation);
     if (personLost()) {
     continue;
     }
     if (personAcceptsInvite()) {
     currentPhase = Phase.PRMTOROOM;
     setPRMGoal(meetingRoomLocation);
     pplCount++;
     currentPhase = Phase.INMEETINGROOM;
     if (isTaskComplete()) {
     currentPhase = Phase.COMPLETED;
     } else {
     //currently within meeting room
     //get out of meeting room
     }
     } else {
     //person did not accept invite
     //turn away from the person
     }
     }
     }
      */
 
     private boolean isTaskComplete() {
         //extend to add 15min time limit and/or other limits
         return pplCount == targetPplCount;
     }
 
     /*
      * Find the rectangle in the array closest to the one that we received
      * previously. Otherwise, we select the closest rectangle to track.
      */
     private RectangleWithDepth findPerson(RectangleWithDepth previousRect) {
         float[] cameraDataCopy = Arrays.copyOf(lastCameraData, lastCameraData.length);
         RectangleWithDepth[] rectangles = convert(cameraDataCopy);
         if (previousRect == null) {
             return getClosestRectangle(rectangles);
         } else {
             // find the rectangle that is the closest match to the one that we
             // received as a parameter
             return getMostSimilarRectangle(rectangles, previousRect);
         }
     }
 
     /*
      * Checks the disparity between two rectangles is below the value specified
      * in the parameter file.
      */
     public boolean rectangleOverlapValid(RectangleWithDepth lastRect, RectangleWithDepth curRect) {
         Point lastCentre = getRectCentre(lastRect);
         Point curCentre = getRectCentre(curRect);
 
         double xDisparity = Math.abs(curCentre.getX() - lastCentre.getX());
         double yDisparity = Math.abs(curCentre.getY() - lastCentre.getY());
 
         boolean xValid = xDisparity <= (MAX_RECTANGLE_CENTRE_DISPARITY * curRect.width);
         boolean yValid = yDisparity <= (MAX_RECTANGLE_CENTRE_DISPARITY * curRect.height);
 
         return xValid && yValid;
     }
 
     public Point getRectCentre(RectangleWithDepth rect) {
         Point centre = messageFactory.newFromType(Point._TYPE);
         double lastX = rect.x + rect.width / 2;
         double lastY = rect.y + rect.height / 2;
         centre.setX(lastX);
         centre.setY(lastY);
         return centre;
     }
 
     /*
      * Checks whether the centre point of curRect is within the rectangle lastRect.
      */
     public boolean checkPointCentreInRectangle(RectangleWithDepth lastRect, RectangleWithDepth curRect) {
         Point centre = getRectCentre(curRect);
 
         boolean xInRect = centre.getX() > lastRect.x && centre.getX() < lastRect.x + lastRect.width;
         boolean yInRect = centre.getY() > lastRect.y && centre.getY() < lastRect.y + lastRect.height;
 
         return xInRect && yInRect;
     }
 
     /*
      * Checks whether the rectangle defining the face detection is withing some
      * distance of the centre of the image.
      */
     public boolean isFaceCentred(RectangleWithDepth personDetection) {
         Point centre = getRectCentre(personDetection);
 
         return Math.abs(centre.getX() - (CAMERA_DIMENSIONS.width / 2)) < FACE_CENTRED_THRESHOLD * CAMERA_DIMENSIONS.width;
     }
 
     public void rotateTowardsPerson(RectangleWithDepth lastRectangle) {
         double areaCentreX = lastRectangle.getCenterX();
         double distFromCentreX = areaCentreX - (CAMERA_DIMENSIONS.width / 2);
         Printer.println("DistFromCentreX: " + distFromCentreX, "CYANF");
         double turnAngle = Math.toRadians(10);
         if (distFromCentreX > 0) {
             //rectangle on the right
             //driver.turn(-turnAngle, true, false);
             driver.turn(getHeadingFromLastPos(), -turnAngle);
         } else {
             //rectangle on left
             //driver.turn(turnAngle, true, false);
             driver.turn(getHeadingFromLastPos(), turnAngle);
         }
     }
 
     private RectangleWithDepth[] convert(float[] data) {
         int numberOfRectangles = (int) (data.length / 5.0);
         RectangleWithDepth[] result = new RectangleWithDepth[numberOfRectangles];
         for (int i = 0; i < numberOfRectangles; i++) {
             int index = i * 5;
             result[i] = new RectangleWithDepth(
                     data[index],
                     data[index + 1],
                     data[index + 2],
                     data[index + 3],
                     data[index + 4]);
         }
         return result;
     }
 
     /*
      * Find the rectangle in the array that has the smallest depth.
      */
     private RectangleWithDepth getClosestRectangle(RectangleWithDepth[] rectangles) {
         RectangleWithDepth closestRect = null;
         for (RectangleWithDepth rect : rectangles) {
             if (closestRect == null || rect.getDepth() < closestRect.getDepth()) {
                 closestRect = rect;
             }
         }
         return closestRect;
     }
 
     /*
      * Finds the rectangle in the array that is most similar to the rectangle
      * given.
      */
     private RectangleWithDepth getMostSimilarRectangle(RectangleWithDepth[] rectangles,
             RectangleWithDepth testRect) {
         RectangleWithDepth mostSimilar = null;
         double similarDist = Double.MAX_VALUE;
         for (RectangleWithDepth rect : rectangles) {
             Point thisCentre = getRectCentre(rect);
             Point testCentre = getRectCentre(testRect);
 
             double msgDepth = rect.depth;
             double testDepth = testRect.depth;
             double thisDist = PRMUtil.getEuclideanDistance(testCentre, thisCentre);
 
             if (thisDist < similarDist && Math.abs(msgDepth - testDepth) <= MAX_RECTANGLE_DEPTH_DISPARITY) {
                 mostSimilar = rect;
                 similarDist = thisDist;
             }
         }
         return mostSimilar;
     }
 
     /*
      * Calculates the position of a detected visual feature which is directly
      * in front of the robot.
      */
     private PoseStamped getObjectLocation(Pose estimatedPoseCopy, double depth) {
         PoseStamped personLocation = messageFactory.newFromType(PoseStamped._TYPE);
         double heading = StaticMethods.getHeading(estimatedPoseCopy.getOrientation());
         personLocation.getPose().getPosition().setX(
                 estimatedPoseCopy.getPosition().getX()
                 + (depth * Math.cos(heading)));
         personLocation.getPose().getPosition().setY(
                 estimatedPoseCopy.getPosition().getY()
                 + (depth * Math.sin(heading)));
         return personLocation;
     }
 
     private boolean personLost() {
         return false;
     }
 
     private boolean personAcceptsInvite() {
         return false;
     }
 
     private void setPRMGoal(PoseStamped targetLocation) {
         goal.publish(targetLocation);
     }
 
     /*
     public void updateMeetingRoomLocation(Point location) {
     meetingRoomLocation.getPose().setPosition(location);
     }
      */
     public void onNewCameraRectanglePoints(float[] data) {
         lastCameraData = data;
     }
 
     public void onNewEstimatedPose(Pose estimatedPose) {
         lastEstimatedPose = estimatedPose;
     }
 
     @Override
     public GraphName getDefaultNodeName() {
         return GraphName.of("Arbitrator");
     }
 }
