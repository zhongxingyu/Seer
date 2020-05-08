 package ex3.navigation;
 
 import ex3.PRM;
 import ex3.PRMUtil;
 import ex3.pid.PID;
 import ex4.Printer;
 import geometry_msgs.Point;
 import geometry_msgs.Pose;
 import geometry_msgs.PoseArray;
 import geometry_msgs.PoseStamped;
 import geometry_msgs.PoseWithCovariance;
 import geometry_msgs.PoseWithCovarianceStamped;
 import geometry_msgs.Quaternion;
 import geometry_msgs.Twist;
 import java.util.ArrayList;
 import java.util.List;
 import launcher.RunParams;
 import nav_msgs.OccupancyGrid;
 
 
 import nav_msgs.Odometry;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 import org.ros.message.MessageFactory;
 import org.ros.message.MessageListener;
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 
 import pf.AbstractLocaliser;
 import sensor_msgs.LaserScan;
 import std_msgs.Bool;
 import util.LaserUtil;
 import visualization_msgs.Marker;
 
 public class Navigator extends AbstractNodeMain {
 
     public static final double MAX_ROTATION_SPEED = 0.3;
     public static final double MIN_ROTATION_SPEED = 0.05;
     public static final double MAX_MOVE_SPEED = 0.3;
     public static final double MIN_MOVE_SPEED = 0.1;
     public static final double POINT_REACHED_THRESHOLD = 0.5;
     public static final double POINT_PROXIMITY_THRESHOLD = 2.0;
     public static final double DISTANCE_FROM_LAST_ESTIMATED_POSE = 3.0;
     // Will attempt to align the robot within this range of the actual angle
     // to the next waypoint.
     public static final double NEXT_WAYPOINT_HEADING_ERROR = 0.15;
     // Obstacle avoidance params
     public static final float SAFE_DISTANCE = 0.75f; // In metres
     public static final float OBSTACLE_ZONE = SAFE_DISTANCE + 0.25f; // In metres
     public static final int SECTORS_CHECKED = 11; // Check this many sectors
     public static final int READINGS_PER_SECTOR_OBSTACLE = 20; // Resolution for obstacle marker creation
     public static final float MARKER_POINT_WIDTH = 0.2f;
     // How many receipts of an estimatedPose we should wait before attempting to prune the obstacle array.
     public static final int OBSTACLE_ARRAY_CHECK_TIME = 20;
     public static final int OBSTACLE_INFLATION_RADIUS = RunParams.getInt("OBSTACLE_INFLATION_RADIUS");
     public static final float LASER_IGNORE_THRESHOLD = RunParams.getFloat("LASER_IGNORE_THRESHOLD");
     public static final boolean OBSTACLE_DETECTION_ACTIVE = RunParams.getBool("OBSTACLE_DETECTION_ACTIVE");
     public static final float OBSTACLE_EXPIRY_DISTANCE = RunParams.getFloat("OBSTACLE_EXPIRY_DISTANCE");
 
     public enum ObstacleAction {
 
         ADD, REMOVE
     };
     
     OccupancyGrid inflatedMap;
     OccupancyGrid obstacleInflatedMap = null;
     MessageFactory factory;
     Pose wayPoint;
     Pose lastEstimate;
     PoseWithCovariance lastEstimatedPoseWithConvariance;
     Pose goalPoint;
     boolean active = false;
     boolean turnOnSpot = true;
     double distanceToWaypoint;
     double rotationToWaypoint;
     PRM prm;
     PID pid;
     PoseArray route;
     ArrayList<Point> obstacleMarkers;
     boolean obstacleWithinSafeDistance = false;
 //    float[] lastScanMedians;
     LaserScan lastScan;
     Subscriber<Odometry> odomPub;
     Subscriber<PoseWithCovarianceStamped> estimatedPoseSub;
     Subscriber<LaserScan> laserSub;
     Subscriber<PoseArray> routeSub;
     Subscriber<PoseWithCovarianceStamped> initialPositionSub;
     Publisher<Twist> movementPub;
     Publisher<Marker> obstacleMarkersPub;
     Publisher<PoseWithCovariance> initialPosePub;
     Publisher<std_msgs.Int32> prmInfoSub;
     Subscriber<std_msgs.Bool> navActiveSub;
     Publisher<PoseStamped> goalPub;
 
     public Navigator(PRM prm) {
         this.prm = prm;
     }
 
     public Navigator(PRM prm, PID pid) {
         this.prm = prm;
         this.pid = pid;
         pid.setOutputLimits(MIN_ROTATION_SPEED, MAX_ROTATION_SPEED);
     }
 
     @Override
     public void onStart(ConnectedNode connectedNode) {
         factory = connectedNode.getTopicMessageFactory();
         AbstractLocaliser.setFactory(factory);
         obstacleMarkers = new ArrayList<Point>();
 
         System.out.println("Obstacle detection active: " + OBSTACLE_DETECTION_ACTIVE);
 
         movementPub = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
         odomPub = connectedNode.newSubscriber("odom", Odometry._TYPE);
         estimatedPoseSub = connectedNode.newSubscriber("amcl_pose", PoseWithCovarianceStamped._TYPE);
         laserSub = connectedNode.newSubscriber("base_scan", LaserScan._TYPE);
         routeSub = connectedNode.newSubscriber("route", PoseArray._TYPE);
         obstacleMarkersPub = connectedNode.newPublisher("obstacleMarkers", Marker._TYPE);
         initialPosePub = connectedNode.newPublisher("initialpose", PoseWithCovarianceStamped._TYPE);
         initialPositionSub = connectedNode.newSubscriber("initialpose", PoseWithCovarianceStamped._TYPE);
         prmInfoSub = connectedNode.newPublisher("goalInfo", std_msgs.Int32._TYPE);
         navActiveSub = connectedNode.newSubscriber("nav_active", std_msgs.Bool._TYPE);
         goalPub = connectedNode.newPublisher("goal", PoseStamped._TYPE);
 
         navActiveSub.addMessageListener(new MessageListener<Bool>() {
             @Override
             public void onNewMessage(Bool t) {
                 boolean val = t.getData();
                 if(val){
                     // We may have moved since we deactivated the navigator, so
                     // replan the route.
                     System.out.println("Navigator activated.");
                     route = null;
                     PoseStamped goal = goalPub.newMessage();
                     goal.setPose(goalPoint);
                     goalPub.publish(goal);
                 } else {
                     System.out.println("Navigator deactivated.");
                 }
                 active = val;
             }
         });
 
         odomPub.addMessageListener(new MessageListener<Odometry>() {
             @Override
             public void onNewMessage(Odometry t) {
                 // Each time we receive an odometry message, publish a movement to cmd_vel.
                 // This is probably not how we should do things - odometry is published even
                 // when the robot is not moving which could cause all sorts of weird problems.
                 if (active && route != null) {
                     distanceToWaypoint = PRMUtil.getEuclideanDistance(lastEstimate.getPosition(), wayPoint.getPosition());
                     if (distanceToWaypoint <= POINT_REACHED_THRESHOLD) {
                         if (nextWayPoint() == false) { // we have reached the goal.
                             System.out.println("Goal reached.");
                             active = false;
                             route = null;
                             // When we reach the goal, we get rid of all obstacles on the map.
                             // and reset the obstacle map.
                             prm.setInflatedMap(inflatedMap);
                             obstacleInflatedMap = null;
                             std_msgs.Int32 info = prmInfoSub.newMessage();
                             info.setData(PRM.GOAL_REACHED); // we reached the goal - send info
                             prmInfoSub.publish(info);
                         } else { // Not yet reached the end of the path
                             turnOnSpot = true;
                             System.out.println("Proceeding to next waypoint.");
                         }
                     }
                     if (obstacleWithinSafeDistance && OBSTACLE_DETECTION_ACTIVE) {
                         // Stopped moving
                         float[][] sectors = LaserUtil._getSectors(
                                 SECTORS_CHECKED, READINGS_PER_SECTOR_OBSTACLE, lastScan);
                         ArrayList<Point> obstacles = getMarkersForObstaclesInLaserScan(sectors);
                         obstacleMarkers.addAll(obstacles); // Track the obstacles in a list
                         publishObstacleMarkers(obstacles);
 
                         // Add obtstacle(s) to map
                         OccupancyGrid mapToInflate = obstacleInflatedMap == null ? inflatedMap : obstacleInflatedMap;
                         // Add obstacles to the map
                         obstaclesOntoMap(mapToInflate, obstacles, ObstacleAction.ADD);
 
                         prm.setInflatedMap(obstacleInflatedMap);
 
                         // Regen route (also publishes)
                         prm.generateRoute();
 
                         obstacleWithinSafeDistance = false;
                         turnOnSpot = true; // New path so new movement
                     } else {
                         if (obstacleMarkers.size() > 0) {
                             _pruneObstacleMarkers(obstacleMarkers, lastEstimate.getPosition());
                         }
                         movementPub.publish(computeMovementValues());
                     }
 //                    movement.publish(PIDcontrol());
                 }
             }
         });
 
         if (OBSTACLE_DETECTION_ACTIVE) { // only subscribe to laser if we are doing avoidance
             laserSub.addMessageListener(new MessageListener<LaserScan>() {
 
                 @Override
                 public void onNewMessage(LaserScan scan) {
                     // If obstacle is too close and we are moving forward, stop
                     if (!turnOnSpot && checkObstacleWithinSafeDistance(scan)) {
                         System.out.println("Setting obstacleWithinSafeDistance = true");
                         obstacleWithinSafeDistance = true;
                     }
                 }
             });
         }
 
         routeSub.addMessageListener(new MessageListener<PoseArray>() {
             @Override
             public void onNewMessage(PoseArray t) {
                 route = t;
                 initRoute();
                 // if this is the first run
                 if (obstacleInflatedMap == null && inflatedMap == null) {
                     inflatedMap = prm.getInflatedMap();
                 }
             }
         });
 
         estimatedPoseSub.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
             @Override
             public void onNewMessage(PoseWithCovarianceStamped t) {
                 // Each time we get an update for the pose estimate, we update our position
                 Pose newEstimatedPose = t.getPose().getPose();
 
                 if (lastEstimatedPoseWithConvariance != null) {
                     double distanceFromLastPose = PRMUtil.getEuclideanDistance(newEstimatedPose.getPosition(), lastEstimatedPoseWithConvariance.getPose().getPosition());
 //                    System.out.println("----------DISTANCE IS ----------- " + distanceFromLastPose);
   //                  System.out.println("1) newEstimatedPose: " + newEstimatedPose.getPosition().getX() + "     lastEstimatedPose: " + lastEstimatedPoseWithConvariance.getPose().getPosition().getX());
                     if (distanceFromLastPose > DISTANCE_FROM_LAST_ESTIMATED_POSE) {
     //                    System.out.println("2) newEstimatedPose: " + newEstimatedPose.getPosition().getX() + "     lastEstimatedPose: " + lastEstimatedPoseWithConvariance.getPose().getPosition().getX());
                         PoseWithCovariance initialPoseWithCS = initialPosePub.newMessage();
                         initialPoseWithCS.setPose(lastEstimatedPoseWithConvariance.getPose());
                         initialPosePub.publish(initialPoseWithCS);
                         Printer.println("DETECTED AMCL JUMP.", "REDF");
                     } else {
                         lastEstimatedPoseWithConvariance = t.getPose();
                     }
                 } else {
                     lastEstimatedPoseWithConvariance = t.getPose();
                 }
 
                 lastEstimate = newEstimatedPose;
 
                 prm.setCurrentPosition(lastEstimate);
 
 //                if (pid != null && route != null){
 //                    pid.setSetpoint(bearingFromZero(lastEstimate.getPosition(), wayPoint.getPosition()));
 //                }
 
             }
         });
 
         initialPositionSub.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
 
             @Override
             public void onNewMessage(PoseWithCovarianceStamped t) {
                 lastEstimatedPoseWithConvariance = null;
                 //              System.out.println("Resetting estimated pose");
             }
         });
 
 
     }
 
     public ArrayList<Point> getMarkersForObstaclesInLaserScan(float[][] sectors) {
         ArrayList<Point> markers = new ArrayList<Point>();
 
         LaserUtil.printSectors(sectors);
 
         float[] medians = LaserUtil.medianOfEachSector(sectors);
         for (int i = 0; i < medians.length; i++) {
             if (medianIsAnObstacle(medians[i])) {
                 double robotHeading = AbstractLocaliser.getHeading(this.lastEstimate.getOrientation());
                 double headingOfSector = LaserUtil.headingOfSector(
                         SECTORS_CHECKED, i, READINGS_PER_SECTOR_OBSTACLE);
                 Point marker = calculateMarkerPosition(robotHeading + headingOfSector, medians[i], robotHeading);
                 markers.add(marker);
             }
         }
         System.out.println();
         return markers;
     }
 
     public boolean medianIsAnObstacle(float median) {
         return median < OBSTACLE_ZONE && median > LASER_IGNORE_THRESHOLD;
     }
 
     /** 
      * @param heading The heading of the point in RADIANS */
     public Point calculateMarkerPosition(double heading, float distanceReading,
             double robotHeading) {
         double xDisplacement = Math.cos(robotHeading) * distanceReading;
         double yDisplacement = Math.sin(robotHeading) * distanceReading;
 
 //        if ((robotHeading > Math.PI/2  && robotHeading < Math.PI) ||
 //                (robotHeading > -Math.PI/2 && robotHeading < 0)) {
 //            xDisplacement = -xDisplacement;
 //            yDisplacement = -yDisplacement;
 //        }
 
         Point currentPoint = this.lastEstimate.getPosition();
         Point newPoint = factory.newFromType(Point._TYPE);
         newPoint.setX(currentPoint.getX() + xDisplacement);
         newPoint.setY(currentPoint.getY() + yDisplacement);
 
 //        System.out.println("Distance to point at heading " + heading + " is " + distanceReading);
 //        System.out.println("Robot's heading: "+AbstractLocaliser.getHeading(lastEstimate.getOrientation()));
 //        System.out.println("xDisp: "+xDisplacement+" yDisp: "+yDisplacement);
 //        System.out.println("NewPoint x: "+newPoint.getX()+" y: "+newPoint.getY());
         return newPoint;
     }
 
     public boolean checkObstacleWithinSafeDistance(LaserScan scan) {
         float[][] sectors = LaserUtil.getSectors(SECTORS_CHECKED, scan);
         float[] medians = LaserUtil.medianOfEachSector(sectors);
         for (float median : medians) {
             // If obstacle
             if (medianIsAnObstacle(median)) {
                 //System.out.println("Oh noes. Obstacle detected. Stopping");
                 lastScan = scan;
                 return true;
             }
         }
         return false;
     }
 
     public OccupancyGrid inflateObstaclesOntoMap(OccupancyGrid inflatedMap,
             ArrayList<Point> obstacles) {
         // Copy data in the grid to a new channel buffer
         ChannelBuffer original = ChannelBuffers.copiedBuffer(inflatedMap.getData());
 
         // Get an occupancy grid for us to put modified data into.
         obstacleInflatedMap = factory.newFromType(OccupancyGrid._TYPE);
 
         // Copy the original buffer into the newly created grid.
         obstacleInflatedMap.setInfo(inflatedMap.getInfo());
         obstacleInflatedMap.setData(ChannelBuffers.copiedBuffer(inflatedMap.getData()));
 
         final int mapHeight = inflatedMap.getInfo().getHeight();
         final int mapWidth = inflatedMap.getInfo().getWidth();
         final float mapRes = inflatedMap.getInfo().getResolution();
 
         for (Point obstacle : obstacles) {
             int scaledX = (int) Math.round(obstacle.getX() / mapRes);
             int scaledY = (int) Math.round(obstacle.getY() / mapRes);
 
             // Data in the map indicates an obstacle, widen the obstacle by some amount
             for (int yOffset = -OBSTACLE_INFLATION_RADIUS; yOffset <= OBSTACLE_INFLATION_RADIUS; yOffset++) {
                 int xOffset = mapWidth * yOffset;
 
                 int index = PRMUtil.getMapIndex(scaledX, scaledY, mapWidth, mapHeight);
 
                 for (int j = index + xOffset - OBSTACLE_INFLATION_RADIUS; j <= index + xOffset + OBSTACLE_INFLATION_RADIUS; j++) {
                     // If there is an obstacle very close to the zeroth index, avoid
                     // exceptions
                     if (j < 0) {
                         j = 0;
                     }
                     // Also avoid going over capacity
                     if (j >= original.capacity()) {
                         break;
                     }
                     // No point widening obstacles into unknown space or something
                     // which is already an obstacle
                     if (original.getByte(j) == -1 || original.getByte(j) == 100) {
                         continue;
                     }
                     // Set the byte to an obstacle in the inflated map
                     obstacleInflatedMap.getData().setByte(j, 100);
                 }
             }
         }
 
         return obstacleInflatedMap;
     }
 
     public OccupancyGrid obstaclesOntoMap(OccupancyGrid inflatedMap,
             ArrayList<Point> obstacles, ObstacleAction action) {
         // Copy data in the grid to a new channel buffer
         ChannelBuffer original = ChannelBuffers.copiedBuffer(inflatedMap.getData());
 
         // Get an occupancy grid for us to put modified data into.
         obstacleInflatedMap = factory.newFromType(OccupancyGrid._TYPE);
 
         // Copy the original buffer into the newly created grid.
         obstacleInflatedMap.setInfo(inflatedMap.getInfo());
         obstacleInflatedMap.setData(ChannelBuffers.copiedBuffer(inflatedMap.getData()));
 
         final int mapHeight = inflatedMap.getInfo().getHeight();
         final int mapWidth = inflatedMap.getInfo().getWidth();
         final float mapRes = inflatedMap.getInfo().getResolution();
 
         for (Point obstacle : obstacles) {
             int scaledX = (int) Math.round(obstacle.getX() / mapRes);
             int scaledY = (int) Math.round(obstacle.getY() / mapRes);
 
             // Data in the map indicates an obstacle, widen the obstacle by some amount
             for (int yOffset = -OBSTACLE_INFLATION_RADIUS; yOffset <= OBSTACLE_INFLATION_RADIUS; yOffset++) {
                 int xOffset = mapWidth * yOffset;
 
                 int index = PRMUtil.getMapIndex(scaledX, scaledY, mapWidth, mapHeight);
 
                 for (int j = index + xOffset - OBSTACLE_INFLATION_RADIUS; j <= index + xOffset + OBSTACLE_INFLATION_RADIUS; j++) {
                     // If there is an obstacle very close to the zeroth index, avoid
                     // exceptions
                     if (j < 0) {
                         j = 0;
                     }
                     // Also avoid going over capacity
                     if (j >= original.capacity()) {
                         break;
                     }
 
                     if (action == ObstacleAction.ADD) {
                         // Don't change unknown and occupied cells
                         if (original.getByte(j) == -1) {
                             continue; // Don't change the value of unknown cells
                         } else if (original.getByte(j) == 100) {
                             // Change the value of occupied cells so we can tell when we come to remove later
                             obstacleInflatedMap.getData().setByte(j, 200);
                         } else { // make free cells occupied
                             obstacleInflatedMap.getData().setByte(j, 100);
                         }
                     } else { // we are removing from the map
                         if (original.getByte(j) == 100) {
                             // set obstacles that we added back to free space
                             obstacleInflatedMap.getData().setByte(j, 0);
                         } else if (original.getByte(j) == 200) {
                             // put obstacles back into the places they were before
                             obstacleInflatedMap.getData().setByte(j, 100);
                         } else {
                             // ignore everything else
                             continue;
                         }
                     }
                 }
             }
         }
 
         return obstacleInflatedMap;
     }
 
     /*
      * Checks the given list of obstacle markers (points) on the map for points
      * which are more than specified euclidean distance from the current location
      * of the robot.
      * ****DESTRUCTIVE******
      */
     public void _pruneObstacleMarkers(ArrayList<Point> markers, Point currentLocation) {
         ArrayList<Point> invalidMarkers = new ArrayList<Point>();
 
         for (Point point : markers) {
             double pointDist = PRMUtil.getEuclideanDistance(point, currentLocation);
             if (pointDist > OBSTACLE_EXPIRY_DISTANCE) {
                 System.out.printf("Obstacle at point [%f, %f] is over %f metres away. Removing\n", point.getX(), point.getY(), OBSTACLE_EXPIRY_DISTANCE);
                 invalidMarkers.add(point); // add the invalidated point
             }
         }
 
 
         // Remove the obstacles on the map which represent the points we removed.
         if (invalidMarkers.size() > 0) {
             markers.removeAll(invalidMarkers);
             OccupancyGrid mapToInflate = obstacleInflatedMap == null ? inflatedMap : obstacleInflatedMap;
             prm.setInflatedMap(obstaclesOntoMap(mapToInflate, invalidMarkers, ObstacleAction.REMOVE));
             // we want to reconnect the graph once we remove the obstacles, otherwise
             // we may end up cutting off areas of the graph, resulting in worse
             // paths
             prm.reconnectGraph(obstacleInflatedMap);
         }
     }
 
     public void publishObstacleMarkers(List<Point> markers) {
         Marker m = PRMUtil.setUpMarker("/map", "obstacle", 15, Marker.ADD, Marker.POINTS, null, null, null, factory);
 
         m.getScale().setX(MARKER_POINT_WIDTH);
         m.getScale().setY(MARKER_POINT_WIDTH);
         m.setPoints(markers);
         m.getColor().setA(1.0f);
         m.getColor().setB(1.0f);
         obstacleMarkersPub.publish(m);
     }
 
     /*
      * Initialises the navigator to follow the route from the current location to
      * the end point. New routes may be received when the prm receives a new map,
      * and in these cases it is important not to lose the original inflated map.
      */
     public void initRoute() {
         nextWayPoint();
         goalPoint = route.getPoses().get(route.getPoses().size() - 1);
         active = true;
 
 //        if (pid != null){
 //            pid.activate();
 //        }
     }
 
 
     /*
      * Gets the next waypoint on the route by removing the head of the list. Returns
      * true if there is still a waypoint left, setting the waypoint variable to
      * the node at the head of the list. Returns false if the list is empty,
      * implying that we have reached the goal.
      */
     public boolean nextWayPoint() {
         if (route.getPoses().isEmpty()) {
             return false;
         } else {
             wayPoint = route.getPoses().remove(0);
             return true;
         }
     }
 
     /*
      * Gets a twist message that should be sent to the robot in order to proceed to
      * the next point.
      */
     public Twist computeMovementValues() {
         Twist pub = movementPub.newMessage();
 
         //System.out.println("Next point: " + wayPoint.getPosition().getX() + "," + wayPoint.getPosition().getY());
 
         double rotReq = computeAngularMovement();
         double moveReq = computeLinearMovement();
 
         //System.out.println("Publishing rotation: " + rotReq + " and movement: " + moveReq);
 
         pub.getAngular().setZ(rotReq);
 
         //System.out.println("Rotating on waypoint: " + turnOnSpot);
 
         if (turnOnSpot) {
             //System.out.println("Rotation to next waypoint: " + rotationToWaypoint);
             if (Math.abs(rotationToWaypoint) < NEXT_WAYPOINT_HEADING_ERROR) {
 
                 turnOnSpot = false;
             }
         } else {
             pub.getLinear().setX(moveReq);
         }
 
         return pub;
     }
 
     /*
      * Calculates the bearing from zero from one point to another. Zero is 
      * facing directly east.
      */
     public double bearingFromZero(Point p1, Point p2) {
         return Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
     }
 
     /*
      * Uses a PID controller to set the rotation value sent to cmd_vel. 
      * The standard method for linear movement calculation is used.
      */
     public Twist PIDcontrol() {
         Twist pub = movementPub.newMessage();
 
         double rotReq = pid.getOutput();
 
         pub.getAngular().setZ(rotReq);
         pub.getLinear().setX(computeLinearMovement());
 
         return pub;
     }
 
     /*
      * Computes the value of linear movement that should be sent to the robot in a naive way.
      */
     public double computeLinearMovement() {
         if (distanceToWaypoint < POINT_REACHED_THRESHOLD) {
             // If we have reached the point, stop.
             return 0;
         } else if (distanceToWaypoint <= POINT_PROXIMITY_THRESHOLD) {
             // If we are in the proximity of the waypoint, adjust our speed relative to our distance
             // from it. Since we don't really care about precision, round the distance value.
             return boundedSpeed(Math.round(distanceToWaypoint));
         } else {
             // If we are not in the proximity of the waypoint, move as fast as we are allowed to.
             return MAX_MOVE_SPEED;
         }
     }
 
     /*
      * Returns a speed value that is within the default bounds.
      */
     public double boundedSpeed(double speedReq) {
         if (speedReq < MIN_MOVE_SPEED) {
             return MIN_MOVE_SPEED;
         } else if (speedReq > MAX_MOVE_SPEED) {
             return MAX_MOVE_SPEED;
         } else {
             return speedReq;
         }
     }
 
     /*
      * Computes the angular movement required to keep the robot on course for the point
      * that it is heading towards.
      */
     public double computeAngularMovement() {
         Quaternion rot = lastEstimate.getOrientation();
         double robotHeading = AbstractLocaliser.getHeading(rot);
         double bearing = bearingFromZero(lastEstimate.getPosition(), wayPoint.getPosition());
 //        System.out.println("curheading: " + robotHeading);
 //        System.out.println("bearing to point: " + bearing);
         double req = 0;
         double diff = bearing - robotHeading;
         // If we're not aligned particularly well with the heading to the waypoint
         // rotate a bit.
         if (Math.toDegrees(diff) > 180) {
             req = diff - Math.toRadians(360);
         } else if (Math.toDegrees(diff) < -180) {
             req = diff + Math.toRadians(360);
         } else {
             req = diff;
         }
 
         rotationToWaypoint = req;
 
        if (req > MAX_ROTATION_SPEED) {
             req = MAX_ROTATION_SPEED;
         }
 
         return req;
     }
 
     @Override
     public GraphName getDefaultNodeName() {
         return GraphName.of("Navigator");
     }
 }
