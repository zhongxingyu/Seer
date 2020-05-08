 package ca.etsmtl.capra.purifier.implementations.jaus.delegates;
 
import java.util.Date;

 import ca.etsmtl.capra.digitizer.sensors.DeadReckoning;
 import ca.etsmtl.capra.purifier.services.RobotPositionService;
 import ca.etsmtl.capra.purifier.services.TrajectoryService;
 import ca.etsmtl.octets.jaus.lib.core.service.ServiceDelegate;
 import ca.etsmtl.octets.jaus.lib.core.transport.ReceiveEvent;
 import ca.etsmtl.octets.jaus.lib.message.field.PresenceVector;
 import ca.etsmtl.octets.jaus.lib.mobility.message.record.LocalPoseRec;
 import ca.etsmtl.octets.jaus.lib.mobility.message.record.LocalWaypointRec;
 
 public class LocalPoseDelegate implements ServiceDelegate<LocalPoseRec>{
 	private LocalPoseRec localPoseRecDiff = new LocalPoseRec();
 	
 	RobotPositionService robotService;
 	DeadReckoning deadReckoning;
 	TrajectoryService trajectoryService;
 	
 	public LocalPoseDelegate(RobotPositionService robotService, DeadReckoning deadReckoning, TrajectoryService trajectoryService)
 	{
 		 this.robotService = robotService;
 		 this.deadReckoning = deadReckoning;
 		 this.trajectoryService = trajectoryService;
 		 localPoseRecDiff.setX(0);
 		 localPoseRecDiff.setY(0);
 		 localPoseRecDiff.setYaw(0);
 	}
 	
 	@Override
 	public LocalPoseRec getRecord(PresenceVector presenceVector) {	
 		System.out.println("RECORD :: LocalPoseRec received");
 		LocalPoseRec localPoseRec = new LocalPoseRec();
		localPoseRec.setX(robotService.getRobotPosition().getX() + localPoseRecDiff.getX());
		localPoseRec.setY(-robotService.getRobotPosition().getY() + localPoseRecDiff.getY());
		localPoseRec.setYaw(robotService.getRobotPosition().getTheta() + localPoseRecDiff.getYaw());
		localPoseRec.setTimeStamp((new Date()).getTime());
 		return localPoseRec;
 	}
 
 	@Override
 	public boolean handleSpecificEvent(ReceiveEvent receiveEvent) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public void setRecord(LocalPoseRec record) {
 		System.out.println(record.getName());
 		if(record.getPresenceVector().isPresent(LocalPoseRec.X))
 			localPoseRecDiff.setX(record.getX() - robotService.getRobotPosition().getX());
 		
 		if(record.getPresenceVector().isPresent(LocalPoseRec.Y))
 			localPoseRecDiff.setY(record.getY()- robotService.getRobotPosition().getY());
 		
 		if(record.getPresenceVector().isPresent(LocalPoseRec.YAW))
 			localPoseRecDiff.setYaw(record.getYaw() - robotService.getRobotPosition().getTheta());
 	}
 	
 	public LocalPoseRec translateToRobotPosition(LocalPoseRec originalRec)
 	{
 		LocalPoseRec translatedRec = new LocalPoseRec();
 		
 		translatedRec.getPresenceVector().setPresent(LocalWaypointRec.X);
 		translatedRec.getPresenceVector().setPresent(LocalWaypointRec.Y);
 		
 		if(originalRec.getPresenceVector().isPresent(LocalPoseRec.X))
 			translatedRec.setX(Math.round(originalRec.getX() - localPoseRecDiff.getX()));
 		
 		if(originalRec.getPresenceVector().isPresent(LocalPoseRec.Y))
 			translatedRec.setY(-Math.round(originalRec.getY()- localPoseRecDiff.getY()));
 		
 //		if(originalRec.getPresenceVector().isPresent(LocalPoseRec.YAW))
 //			translatedRec.setYaw(Math.round(originalRec.getYaw() - localPoseRecDiff.getYaw()));
 //	
 		return translatedRec;
 	}
 	
 	
 
 }
