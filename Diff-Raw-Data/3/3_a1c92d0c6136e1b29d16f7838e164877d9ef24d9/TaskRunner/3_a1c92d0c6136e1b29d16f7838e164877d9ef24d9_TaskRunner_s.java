 package regionals2012;
 
 import java.util.List;
 
 
 import robot.LegoRobot;
 import robot.BlockRobot;
 import robot.Robot;
 import robot.BlockRobot;
 import robot.KelpRobot;
 import robot.extentions.AdjustBlockRobot;
 import robot.extentions.GrabRobot;
 
 import utils.Constants;
 import utils.KelpConstants;
 import utils.pathfinding.DriveTask;
 import utils.pathfinding.GrabTask;
 import utils.pathfinding.KelpTask;
 import utils.pathfinding.ReleaseTask;
 import utils.pathfinding.AdjustBlockTask;
 import utils.pathfinding.GetBlockTask;
 import utils.pathfinding.DriveTask;
 import utils.pathfinding.GrabTask;
 import utils.pathfinding.ListTask;
 import utils.pathfinding.Task;
 import utils.pathfinding.TurnTask;
 import utils.vision.Block;
 
 public class TaskRunner implements Runnable, KelpConstants {
 
 	private Robot robot;
 	private List<Task> taskChain;
 
 	public TaskRunner (Robot robot, List<Task> taskChain) {
		this.taskChain = taskChain;	
 	}
 
 	@Override
 	public void run() {
 		//How to use this code for KelpBot?
 		// Loop through all tasks in chain
 		for (Task task : taskChain) {
 			// Handle task type
 			if (task instanceof DriveTask) {
 				DriveTask driveTask = (DriveTask) task;
 
 				//Added code because create moves forward w/ negative values and lego with positive
 				//or is it ok since we can just flip motor terminals for lego?
 				if (driveTask.getETValue() == 0){
 					if (robot instanceof BlockRobot)
 						robot.getDriveTrain().moveCm(Constants.inchesToCentimeters(driveTask.getDistance()), -driveTask.getSpeed());
 					if (robot instanceof KelpRobot)
 						robot.getDriveTrain().moveCm(Constants.inchesToCentimeters(driveTask.getDistance()), driveTask.getSpeed());
 				} else {
 					if (robot instanceof KelpRobot)
 						robot.getDriveTrain().moveAtCmps(driveTask.getSpeed());
 					while(driveTask.getETValue() > ET_STOP_VALUE){}
 					robot.getDriveTrain().kill();
 				}
 			} else if (task instanceof TurnTask) {
 				TurnTask turnTask = (TurnTask) task;
 				robot.getDriveTrain().rotateDegrees(turnTask.getAngle(), turnTask.getSpeed());
 			} else if (task instanceof GrabTask && robot instanceof GrabRobot) {
 				System.out.println("Claw, go!");
 				((GrabRobot)robot).grab();
 				System.out.println("Grabbed.");
 			} else if (task instanceof ReleaseTask && robot instanceof GrabRobot){
 				((GrabRobot)robot).release();
 			} else if (task instanceof GetBlockTask && robot instanceof BlockRobot) {
 				GetBlockTask getBlockTask = (GetBlockTask) task;
 				Block.setBlock(getBlockTask.getLocation(), getBlockTask.getBlock());
 			} else if (task instanceof AdjustBlockTask && robot instanceof BlockRobot) {
 				AdjustBlockRobot adjRobot = (AdjustBlockRobot)robot;
 				adjRobot.adjustBlock();
 			} else if (task instanceof ListTask) {
 				ListTask tList = (ListTask)task;
 				new TaskRunner(robot, tList.getTaskChain()).run();
 			}
 		}
 	}
 }
 
 
