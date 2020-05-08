 package ex2;
 
 import geometry_msgs.PoseWithCovarianceStamped;
 import geometry_msgs.Twist;
 import logging.Logger;
 import nav_msgs.Odometry;
 import org.ros.message.MessageListener;
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
 import org.ros.node.Node;
 import org.ros.node.topic.Publisher;
 import org.ros.node.topic.Subscriber;
 import pf.AbstractLocaliser;
 
 public class ExperimentNav extends AbstractNodeMain {
 
     public static final int NSECS_DIVIDER = 100000000;
 
     Subscriber<Odometry> odom;
     Subscriber<PoseWithCovarianceStamped> expectedPose;
     Publisher<Twist> move;
     Logger logger;
     Logger checkpointLogger;
     boolean newAmclData = false;
 
     private Odometry[] odomArray = new Odometry[10];
     private PoseWithCovarianceStamped amclData;
     private Odometry lastMatchingOdom;
 
     @Override
     public void onStart(ConnectedNode connectedNode) {
         try {
             System.out.println("Initialising logger...");
             logger = new Logger();
             logger.setAutoFlushing(true);
             logger.logLine(getExperimentDescription());
             checkpointLogger = new Logger("CheckpointLog-", true);
             checkpointLogger.setAutoFlushing(true);
             checkpointLogger.logLine(getExperimentDescription());
         } catch (Exception ex) {
             System.out.println("Logger couldn't initialise. Error: "+ex.toString());
             System.exit(1);
         }
         move = connectedNode.newPublisher("cmd_vel", Twist._TYPE);
         odom = connectedNode.newSubscriber("odom", Odometry._TYPE);
         expectedPose = connectedNode.newSubscriber("/amcl_pose", PoseWithCovarianceStamped._TYPE);
 
         expectedPose.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
 
             @Override
             public void onNewMessage(PoseWithCovarianceStamped t) {
                amclData = t;
                newAmclData = true;
             }
         });
 
         odom.addMessageListener(new MessageListener<Odometry>() {
 
             int moveflag = 0;
 
             @Override
             public void onNewMessage(Odometry odom) {
                 odomArray[odom.getHeader().getStamp().nsecs / NSECS_DIVIDER] = odom;
                 if (amclData != null
                         && odomArray[amclData.getHeader().getStamp().nsecs / NSECS_DIVIDER] != null
                         && newAmclData) {
                     int amclTime = amclData.getHeader().getStamp().nsecs / NSECS_DIVIDER;
                     System.out.println(getTimeStampWithPoseString(amclData) + "\t\t" + getTimeStampWithPoseString(odomArray[amclTime]));
                     if (LocalisationUtil.timeStampEqual(odomArray[amclTime].getHeader().getStamp(), amclData.getHeader().getStamp())) {
                         System.out.println("logging");
                         logger.logLine(getTimeStampWithPoseString(amclData) + "\t" +
                                 getTimeStampWithPoseString(odomArray[amclTime]));
                         lastMatchingOdom = odomArray[amclTime];
                     } else {
                         System.out.println("ERROR! Something bad is happening inside onNewMessage()");
                         System.err.println("ERROR! Something bad is happening inside onNewMessage()");
                         logger.logLine("ERROR! Encountered inequality in timestamps: ");
                         logger.logLine("AMCL: "+getTimeStampWithPoseString(amclData) + "\t\t" +
                                 "ODOM: " + getTimeStampWithPoseString(odomArray[amclTime]));
                         checkpointLogger.logLine("ERROR! Encountered inequality in timestamps: ");
                         checkpointLogger.logLine("AMCL: "+getTimeStampWithPoseString(amclData) + "\t\t" +
                                 "ODOM: " + getTimeStampWithPoseString(odomArray[amclTime]));
                     }
                     newAmclData = false;
                 }
                 double curX = odom.getPose().getPose().getPosition().getX();
                 double curY = odom.getPose().getPose().getPosition().getY();
                 double curHeading = AbstractLocaliser.getHeading(odom.getPose().getPose().getOrientation());
 
                 Twist fwd = move.newMessage();
                 fwd.getLinear().setX(0.5);
 
                 Twist ccwRot = move.newMessage();
                 ccwRot.getAngular().setZ(0.5);
                 Twist cwRot = move.newMessage();
                 cwRot.getAngular().setZ(-0.5);
                 
                 if(curX < 6.10 && curY < 0.01 && moveflag == 0) {
                     move.publish(fwd);
                 }
 
                 if (curX < 6.26 && curX > 6.24 && curY < 0.01 && moveflag == 0){
                     moveflag = 1;
                     checkpointReached();
                 }
 
                 if (curHeading > -1.00 && moveflag == 1) {
                     move.publish(cwRot);
                 }
 
                 if (curHeading < -1.10 && curHeading > -1.20 && moveflag == 1){
                     moveflag = 2;
                     checkpointReached();
                 }
 
                 if (curX < 8.15 && moveflag == 2){
                     move.publish(fwd);
                 }
 
                 if (curX < 8.22 && curX > 8.20 && curY < -4.37 && curY > -4.39 && moveflag == 2){
                     moveflag = 3;
                     checkpointReached();
                 }
 
                 if (curHeading > -2.45 && moveflag == 3){
                     move.publish(cwRot);
                 }
 
                 if (curHeading > -2.60 && curHeading < -2.50 && moveflag == 3){
                     moveflag = 4;
                     checkpointReached();
                 }
 
                 if (curX > 5.81 && moveflag == 4){
                     move.publish(fwd);
                 }
 
                 if (curX > 5.65 && curX < 5.75 && moveflag == 4){
                     moveflag = 5;
                     checkpointReached();
                 }
 
                 if (curHeading < -1.80 && moveflag == 5){
                     move.publish(ccwRot);
                 }
 
                 if (curHeading < -1.60 && curHeading > -1.70 && moveflag == 5){
                     moveflag = 6;
                     checkpointReached();
                 }
 
                 if (curY > -9.25 && moveflag == 6){
                     move.publish(fwd);
                 }
 
                 if (curY > -9.45 && curY < -9.35 && moveflag == 6){
                     moveflag = 7;
                     checkpointReached();
                 }
 
                 if (curHeading < -0.60 && moveflag == 7){
                     move.publish(ccwRot);
                 }
 
                 if (curHeading > -0.55 && curHeading < -0.45 && moveflag == 7){
                     moveflag = 8;
                     checkpointReached();
                 }
 
                 if (curX < 8.50 && moveflag == 8) {
                     move.publish(fwd);
                 }
 
                checkpointReached();
 
             }
         });
     }
 
     public String getExperimentDescription() {
         final String NL = "\n";
 
         String desc = "";
         desc += "PFLocaliser Rotation Noise: "+PFLocaliser.ROTATION_NOISE + NL;
         desc += "PFLocaliser Position Noise: "+PFLocaliser.POSITION_NOISE + NL;
         desc += "PFLocaliser NumOfParticles: "+PFLocaliser.PARTICLE_NUMBER + NL;
         desc += "PFLocaliser ClusterThreshold: "+PFLocaliser.CLUSTER_THRESHOLD + NL;
         desc += "AbstractLocaliser RotationNoise: "+AbstractLocaliser.getRotationNoise() + NL;
         desc += "AbstractLocaliser PositionNoise: "+AbstractLocaliser.getPositionNoise();
         desc += "\n\n";
         return desc;
     }
 
     public void checkpointReached() {
         checkpointLogger.logLine(getTimeStampWithPoseString(amclData)
                 + "\t" + getTimeStampWithPoseString(lastMatchingOdom));
     }
 
     public String getTimeStampWithPoseString(PoseWithCovarianceStamped pose) {
         return LocalisationUtil.getTimeStampWithPose(
                 pose.getPose(),
                 pose.getHeader().getStamp());
     }
     
     public String getTimeStampWithPoseString(Odometry pose) {
         return LocalisationUtil.getTimeStampWithPose(
                 pose.getPose(),
                 pose.getHeader().getStamp());
     }
 
     @Override
     public GraphName getDefaultNodeName() {
         return GraphName.of("experimentnav");
     }
 
     @Override
     public void onShutdown(Node node) {
         logger.close();
     }
 }
