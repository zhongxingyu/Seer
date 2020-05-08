 package entities;
 
 import pathfind.Arc;
 import pathfind.Path;
 import pathfind.Node;
 import pathfind.Side;
 import pathfind.TypeCouloir;
 
 public class VirtualRobot {
 
 	private Node lastNode;
 	private Arc currentArc;
 	private double clock;	//TODO
 	private int orientation;	//to abscissa/ordinate axis
 	private int speed;
 	private Robot robot;
 	
 	public VirtualRobot(Node start, int orientationInit, Robot r) {
 		lastNode = start;
 		currentArc = null;
 		clock = 0;
 		orientation = orientationInit;
 		speed = 75;
 		robot = r;
 	}
 
 	public double[] getPosition() {
 		double[] position = new double[2];
 		
 		if (currentArc==null) {
 			//moving
 			position[0] = lastNode.getAbscissa();
 			position[1] = lastNode.getOrdinate();
 		} else {
 			//not moving
 			//TODO : better feedback with path instead of node
 			double distance = currentArc.getPath().getDistance();
 			position[0] = currentArc.getNodeStart().getAbscissa()
 					+ (currentArc.getNodeTarget().getAbscissa() - currentArc.getNodeStart().getAbscissa())
 					*clock*speed/distance;
 			position[1] = currentArc.getNodeStart().getOrdinate()
 					+ (currentArc.getNodeTarget().getOrdinate() - currentArc.getNodeStart().getOrdinate())
 					*clock*speed/distance;
 		}
 		
 		return position;
 	}
 	
 	/**
 	 * Send the needed instructions to the robot
 	 * @param path the path calculated by GraphSearc.shortherDistance
 	 */
 	//TODO : working with path instead of arc
 	public void sendInstruction(Path path) {
 		int i;
 		int angle;
 		int distance;
 		Command command;
 		Node depart;
 		Node arrive;
 		
 		for (i = 0; i < path.getPath().size() - 1; i++) {
 			depart = path.getPath().get(i);
 			arrive = path.getPath().get(i + 1);
 			
 			if (!(depart.getType() instanceof TypeCouloir)
 					||!(arrive.getType() instanceof TypeCouloir)) {
 				//moving in/out a room
 				//orientation
 				command = new Command();
 				command.setAction(Command.TURN);
 				angle = depart.angleAutreNode(arrive) - orientation;
 				command.addParameter(new Parameter("angle", angle), 0);
 				robot.addCommand(command);
 				orientation += angle;
 				System.out.println(angle);
 				//move
 				command = new Command();
 				command.setAction(Command.FORWARD);
 				distance = (int) depart.calculateDistance(arrive);
 				command.addParameter(new Parameter("distance", distance), 0);
 				robot.addCommand(command);
 				
 			} else {
 				//follow a wall
 				//orientation
 				command = new Command();
 				command.setAction(Command.TURN);
 				angle = depart.angleAutreNode(arrive) - orientation;
 				command.addParameter(new Parameter("angle", angle), 0);
 				robot.addCommand(command);
 				orientation += angle;
 				System.out.println(angle);
 				//move
 				command = new Command();
 				command.setAction(Command.FOLLOW_WALL);
 				distance = (int) depart.calculateDistance(arrive);
 				if (arrive.getType().getSide()==Side.Left) {
 					command.addParameter(new Parameter("direction", 2), 0);
				} else {
					command.addParameter(new Parameter("direction", 1), 0);
 				}
 				command.addParameter(new Parameter("distance", distance), 1);
 				robot.addCommand(command);
 			}
 		}
 		command = new Command();
 		command.setAction(Command.STOP);
 		robot.addCommand(command);
 		robot.executeStack();
 	}
 	
 	public Node getLastNode() {
 		return lastNode;
 	}
 
 	public Arc getCurrentArc() {
 		return currentArc;
 	}
 
 	public double getClock() {
 		return clock;
 	}
 
 	public double getOrientation() {
 		return orientation;
 	}
 	
 }
