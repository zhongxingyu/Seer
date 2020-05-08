 package model.backbone.utils;
 
 import java.util.ArrayList;
 
 import model.backbone.agent.Agent;
 import model.backbone.building.elements.Danger;
 import model.backbone.building.elements.Wall;
 import model.backbone.building.helpers.Point;
 import resources.SimulationResources;
 
 public class CollisionUtils {
 	
 	public static final int agentToWallMinimumDistance = 25;
 	public static final int agentToAgentMinimumDistance = 25;
 	public static final int agentToDangerMinimumDistance = 25;
 	public static final int rerouteOffset = 50;
 	
 	public static boolean areThereObstaclesInMyPath(Agent me, Point destination) {
 		
 		ArrayList<Danger> dangers = (ArrayList<Danger>) SimulationResources.building.getFloors().get(me.getFloor()).getDangers();	
 		for (Danger d : dangers) {
 			if (MathUtils.getDistanceBetweenPointAndLine(me.getLocation(), destination, d.getCenter()) < agentToDangerMinimumDistance
 					&& AlgorithmUtilities.canISeeIt(me, d.getCenter())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void rerouteMe(Agent me) {
 
 		Danger danger = getClosestObstacleInMyPath(me);
 		ArrayList<Point> possibleReroutePoints = getReroutePoints(danger, me.getFloor()); 
 		ArrayList<Point> sortedByPriorityPoints = new ArrayList<Point>();
 		
 		while (true) {
 			
 			int closestDistance = -1;
 			Point bestPoint = null;
 			
 			for (Point p : possibleReroutePoints) {
 				 
 				if (sortedByPriorityPoints.contains(p)) continue;
 				if (bestPoint == null || closestDistance > MathUtils.getDistanceBetweenTwoPoints(p, me.getDestinationPoint())) {
 					bestPoint = p;
 					closestDistance = MathUtils.getDistanceBetweenTwoPoints(p, me.getDestinationPoint());
 				}
 			}
 			
 			sortedByPriorityPoints.add(bestPoint);
 			
 			if (sortedByPriorityPoints.size() == possibleReroutePoints.size()) {
 				break;
 			}
 			
 		}
 		
 		me.setAvoidingCollision(true);
 		me.setTemporaryDestinationPoints(sortedByPriorityPoints);
 	}
 
 	private static Danger getClosestObstacleInMyPath(Agent me) {
 		
 		ArrayList<Danger> dangersInPath = new ArrayList<Danger>();
 		ArrayList<Danger> dangers = (ArrayList<Danger>) SimulationResources.building.getFloors().get(me.getFloor()).getDangers();	
 		for (Danger d : dangers) {
 			if (MathUtils.getDistanceBetweenPointAndLine(me.getLocation(), me.getDestinationPoint(), d.getCenter()) < agentToDangerMinimumDistance
 					&& AlgorithmUtilities.canISeeIt(me, d.getCenter()))
 				dangersInPath.add(d);
 		}
 		
 		if (dangersInPath.size() > 1) {
 			Danger closestDanger = dangersInPath.get(0);
 			int distance = MathUtils.getDistanceBetweenTwoPoints(me.getLocation(), closestDanger.getCenter()) - closestDanger.getRadius();
 			for (int i = 1; i < dangersInPath.size(); i++) {
 				if ((MathUtils.getDistanceBetweenTwoPoints(me.getLocation(), dangersInPath.get(i).getCenter()) -  dangersInPath.get(i).getRadius())  < distance) {
 					closestDanger = dangersInPath.get(i);
 					distance = MathUtils.getDistanceBetweenTwoPoints(me.getLocation(), dangersInPath.get(i).getCenter()) -  dangersInPath.get(i).getRadius();
 				}
 			}
 			return closestDanger;
 		} else {
 			return dangersInPath.get(0);
 		}
 		
 	}
 	
 	private static ArrayList<Point> getReroutePoints(Danger d, int floor) {
 	
 		int desiredDistanceFromDanger = rerouteOffset + d.getRadius();
 		ArrayList<Point> result = new ArrayList<Point>();
 		Point reroutePoint = new Point(d.getCenter(),desiredDistanceFromDanger,0);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(), -desiredDistanceFromDanger,0);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(), 0,desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(),0,-desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		
 		reroutePoint = new Point(d.getCenter(), -desiredDistanceFromDanger,-desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(), -desiredDistanceFromDanger,desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(), desiredDistanceFromDanger,desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		reroutePoint = new Point(d.getCenter(),desiredDistanceFromDanger,-desiredDistanceFromDanger);
 		if (couldIStandThere(reroutePoint, floor)) {
 			result.add(reroutePoint);
 		}
 		return result;
 	}
 	
 	public static boolean canIMoveThere(Agent me, int xDest, int yDest) {
 		ArrayList<Agent> agents = (ArrayList<Agent>) SimulationResources.building.getAgents();
 		ArrayList<Wall> walls = (ArrayList<Wall>) SimulationResources.building.getFloors().get(me.getFloor()).getWalls();
 		ArrayList<Danger> dangers = (ArrayList<Danger>) SimulationResources.building.getFloors().get(me.getFloor()).getDangers();
 		
 		Point myDestination = new Point(me.getLocation().getX()+xDest, me.getLocation().getY()+yDest);
 		if (me.getLastLocation() != null &&
 				myDestination.stringRepresentation().equals(me.getLastLocation().stringRepresentation())) return false;
 		//Check if agent will not come too close to the wall
 		for (Wall w : walls) {
 			if (MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), myDestination) < agentToWallMinimumDistance
 					&& MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), myDestination)
 					< MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), me.getLocation()) ) {
 				return false;
 			}
 		}
 		//Check if agent will not come too close to the danger
 		for (Danger d : dangers) {
 			if (MathUtils.getDistanceBetweenTwoPoints(d.getCenter(), myDestination) < (d.getRadius()+ agentToDangerMinimumDistance)
 					&& MathUtils.getDistanceBetweenTwoPoints(d.getCenter(), myDestination)
 					< MathUtils.getDistanceBetweenTwoPoints(d.getCenter(), me.getLocation())  ) {
 				return false;
 			}
 		}
 		
 		//Check if any other agent will block this agent's path
 		for (Agent a : agents) {
 			//check if its not the same agent!
 			if (a.equals(me) || a.isEscaped()) continue;
 			//Are the agents on the same floor
 			if (a.getFloor() == me.getFloor()) {
 					//Is any other agent too close to our destination
 				if (MathUtils.getDistanceBetweenTwoPoints(a.getLocation(), myDestination) < agentToAgentMinimumDistance
 						&& MathUtils.getDistanceBetweenTwoPoints(a.getLocation(), myDestination)
 						< MathUtils.getDistanceBetweenTwoPoints(a.getLocation(), me.getLocation())) {
 					return false;
 				}	
 			}
 		}
 		
 		return true;
 	}
 	
 	public static boolean couldIStandThere(Point destination, int floor ) {
 		ArrayList<Wall> walls = (ArrayList<Wall>) SimulationResources.building.getFloors().get(floor).getWalls();
 		ArrayList<Danger> dangers = (ArrayList<Danger>) SimulationResources.building.getFloors().get(floor).getDangers();
 		
 		for (Wall w : walls) {
 			if (MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), destination) < agentToWallMinimumDistance ) {
 				return false;
 			}
 		}
 		
 		for (Danger d : dangers) {
 			if (MathUtils.getDistanceBetweenTwoPoints(d.getCenter(), destination) < (d.getRadius()+ agentToDangerMinimumDistance)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public static boolean canCreateAgentAtPoint(Point destination, int floor) {
 		ArrayList<Wall> walls = (ArrayList<Wall>) SimulationResources.building.getFloors().get(floor).getWalls();
 		ArrayList<Danger> dangers = (ArrayList<Danger>) SimulationResources.building.getFloors().get(floor).getDangers();
 		ArrayList<Agent> agents = (ArrayList<Agent>) SimulationResources.building.getAgents();
 		
 		for (Wall w : walls) {
 			if (MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), destination) < agentToWallMinimumDistance ) {
 				return false;
 			}
 		}
 		
 		for (Danger d : dangers) {
 			if (MathUtils.getDistanceBetweenTwoPoints(d.getCenter(), destination) < (d.getRadius()+ agentToDangerMinimumDistance)) {
 				return false;
 			}
 		}
 		
 		for (Agent a : agents) {
 			if (a.getFloor() == floor) {
 				if (MathUtils.getDistanceBetweenTwoPoints(a.getLocation(), destination) < agentToAgentMinimumDistance) {		
 					return false;
 				}	
 			}
 		}
 		return true;
 	}
 	
 	public static boolean canCreateDangerAtPoint(Point destination, int floor) {
 		ArrayList<Wall> walls = (ArrayList<Wall>) SimulationResources.building.getFloors().get(floor).getWalls();
 		ArrayList<Agent> agents = (ArrayList<Agent>) SimulationResources.building.getAgents();
 		
 		for (Wall w : walls) {
 			if (MathUtils.getDistanceBetweenPointAndLine(w.getBegin(), w.getEnd(), destination) < 3 ) {
 				return false;
 			}
 		}
 			
 		for (Agent a : agents) {
 			if (a.getFloor() == floor) {
				if (MathUtils.getDistanceBetweenTwoPoints(a.getLocation(), destination) < agentToDangerMinimumDistance) {		
 					return false;
 				}	
 			}
 		}
 		return true;
 	}
 	
 }
