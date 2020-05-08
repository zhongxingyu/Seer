 package usecase;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import robot.ExploreRobot;
 import robot.GripperRobot;
 import data.Position;
 import device.Blackboard;
 import device.Device;
 import device.IDevice;
 import device.RobotClient;
 import device.Simulation;
 
 public class FindCollectExample {
 
 	private static BufferedReader in = new BufferedReader(
             new InputStreamReader(System.in));
 
 	
 	public static void main (String[] args) {
 		try {
 			RobotClient explDevices = new RobotClient("localhost", 6665);
 			explDevices.runThreaded();
 			
 			RobotClient gripDevices = new RobotClient("localhost", 6667);
 			RobotClient gripDevices2 = new RobotClient("localhost", 6668);
 			gripDevices.runThreaded();
 			gripDevices2.runThreaded();
 			Device gripperDevices = new Device( new Device[]{gripDevices, gripDevices2} );
 
 			ExploreRobot explorer = new ExploreRobot(explDevices);	
 			GripperRobot gripper = new GripperRobot(gripperDevices);
 			
 			gripper.setPosition(new Position(-3,-5,Math.toRadians(90)));
 			explorer.setPosition(new Position(-6,-5,Math.toRadians(90)));
 
 			explorer.runThreaded();
 			gripper.runThreaded();	
 			
 			// Task synchronization
 			Blackboard blackb= Blackboard.getInstance(gripper);
 			// wants to write notes
 			explorer.setBlackboard(blackb);
 			
			Simulation simu = (Simulation) explDevices.getDevice(new Device(IDevice.DEVICE_SIMULATION_CODE,null, 6665, 0));
 			
 			// for modifying world
 //			Simulator simu   = Simulator.getInstance("localhost", 6665);
 			blackb.setSimulation(simu);

 			blackb.runThreaded();
 //			Tracker tracker  = Tracker.getInstance(simu, null);
 //			tracker.addObject("r0", pionSB);
 //			tracker.addObject("r1", pionLG);
 //			while (true) {
 //				Thread.sleep(1000);
 //				System.out.println(pionRG.getPosition().toString());
 //			}
 			// Wait until enter is pressed
 			in.readLine();
 //			tracker.shutdown()
 			blackb.shutdown();
 						
 			explorer.shutdown();
 			explDevices.shutdown();
 			
 			gripper.shutdown();
 			gripDevices.shutdown();
 			gripDevices2.shutdown();
 
 			simu.shutdown();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Exit with errors");
 		}
 	}
 }
