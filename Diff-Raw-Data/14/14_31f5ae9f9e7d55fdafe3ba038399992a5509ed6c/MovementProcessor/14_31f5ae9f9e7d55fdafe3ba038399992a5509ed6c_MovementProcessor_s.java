 package ee.ttu.kinect.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ee.ttu.kinect.calc.Calculator;
 
 public class MovementProcessor {
 
 	private final static double TRAJECTORY_MIN_VALUE = 0.15;
 	
 	private final int windowSize = 30; 
 	
 	private double trajectorySummary;
 	
 	private double velocitySummary;
 	
 	private double accelerationSummary;
 	
 	private List<Body> data;
 	
 	private List<Body> allTimeData;
 	
 	public MovementProcessor() {
 		data = new ArrayList<Body>();
 		allTimeData = new ArrayList<Body>();
 		clean();
 	}
 	
 //	private void analyze(List<Body> data, JointType selectedType) {
 //		this.trajectorySummary = new ArrayList<Double>();
 //		this.velocitySummary = new ArrayList<Double>();
 //		this.accelerationSummary = new ArrayList<Double>();
 //		
 //		for (int i = 0; i < data.size(); i++) {
 //			double trajectorySummary = 0;
 //			double velocitySummary = 0;
 //			double accelerationSummary = 0;
 //			for (int j = 0; j < this.windowSize; j++) {
 //				if ((i + j) < data.size() && (i + j + 1) < data.size() && (j + 1) < this.windowSize) {
 //					Joint joint1 = data.get(i + j).getJoint(selectedType);
 //					Joint joint2 = data.get(i + j + 1).getJoint(selectedType);
 //					long time1 = data.get(i + j).getTimestamp();
 //					long time2 = data.get(i + j + 1).getTimestamp();
 //					trajectorySummary += Calculator.calculateTrajectoryLength3D(joint1, joint2);
 //					velocitySummary += Math.abs(Calculator.calculateVelocity3D(joint1, joint2, time1, time2));
 //					accelerationSummary += Math.abs(Calculator.calculateAcceleration3D(joint1, joint2, time1, time2));
 //				}
 //			}
 //			this.trajectorySummary.add(trajectorySummary);
 //			this.velocitySummary.add(velocitySummary);
 //			this.accelerationSummary.add(accelerationSummary);
 //		}
 //	}
 	
 	private void analyze(List<Body> data, JointType type) {
 		trajectorySummary = 0;
 		velocitySummary = 0;
 		accelerationSummary = 0;
 		for (int i = 0; i < data.size(); i++) {
 			if ((i + 1) < data.size()) {
 				Joint joint1 = data.get(i).getJoint(type);
 				Joint joint2 = data.get(i + 1).getJoint(type);
 				long time1 = data.get(i).getTimestamp();
 				long time2 = data.get(i + 1).getTimestamp();
 				trajectorySummary += Calculator.calculateTrajectoryLength3D(joint1, joint2);
 				velocitySummary += Math.abs(Calculator.calculateVelocity3D(joint1, joint2, time1, time2));
 				accelerationSummary += Math.abs(Calculator.calculateAcceleration3D(joint1, joint2, time1, time2));
 			}
 		}
 	}
 
 	public void clean() {
 		trajectorySummary = 0;
 	}
 	
 	public boolean process(Body body, JointType type) {
 		allTimeData.add(body);
 		data.add(body);
 		if (data.size() == windowSize) {
 			analyze(data, type);
 			data.remove(0);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isMovementEnded() {
 		//System.out.println(trajectorySummary);
 		return trajectorySummary < MovementProcessor.TRAJECTORY_MIN_VALUE;
 	}
 	
 	public double getTrajectorySummary() {
 		return trajectorySummary;
 	}
 	
 	public void outputSummaryToConsole(JointType type) {
 		double startTime = allTimeData.get(0).getTimestamp() / 1000;
 		double endTime = allTimeData.get(allTimeData.size() - 1).getTimestamp() / 1000;
 		double time = endTime - startTime;
 		
 		analyze(allTimeData, type);
 		double trajectoryMass = trajectorySummary;
 		double accelerationMass = accelerationSummary;
 		double eucledianDistance = Calculator.calculateTrajectoryLength3D(allTimeData.get(0).getJoint(type), 
 				allTimeData.get(allTimeData.size() - 1).getJoint(type));
 		double ratio = eucledianDistance / trajectoryMass;
 		
		System.out.println("Frame start: " + startTime);
		System.out.println("Frame end: " + endTime);
 		System.out.println("Time elapsed: " + time);
 		System.out.println("Trajectory mass: " + trajectoryMass);
 		System.out.println("Accelearation mass: " + accelerationMass);
 		System.out.println("Eucledian distance: " + eucledianDistance);
 		System.out.println("Eucl / Traj ratio: " + ratio);
 	}
 	
 }
