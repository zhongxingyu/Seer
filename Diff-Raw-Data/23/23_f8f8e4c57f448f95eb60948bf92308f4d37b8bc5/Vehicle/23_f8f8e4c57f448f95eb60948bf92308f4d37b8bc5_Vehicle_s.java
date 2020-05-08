 package org.uci.luci.interchange.Vehicles;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.uci.luci.interchange.Driver.Navigation;
 import org.uci.luci.interchange.Driver.RandomNavigation;
 import org.uci.luci.interchange.Exception.NoPathToDestinationException;
 import org.uci.luci.interchange.Graph.*;
 import org.uci.luci.interchange.Intersections.*;
 import org.uci.luci.interchange.Registry.*;
 import org.uci.luci.interchange.Util.*;
 
 public class Vehicle {
 	public static double DISTANCE_TO_CONSIDER_AS_SAME = 0.003048;// in km
 	public static double MAX_ACCELERATION = 10; // 0-60mph in 10sec - in km/s^2
 	public static double MAX_NEG_ACCELERATION = -34.995; // 60-0mph in 120ft -
 															// in m/s^2
 	public static double MAX_SPEED = 80 * 1.609344; // 80mph
 	private NodeTraversingMehanism nodeTraversingMehanism;
 	public int vin;
 	public double lat, lon;
 	// public double x, y;
 	public double acceleration;
 	// don't touch these, only actuate acceleration
 	private double speed; // in km/h
 	public Vector2d velocity;
 	// lanes are numbered 0-(lanes-1) with 0 being the left-most lane.
 	// the highest number is the lane on the right shoulder of the street
 	private int onLaneNumber;
 	public String state = "";
 	boolean paused;
 	public boolean flagForRemoval = false;
 
 	// this is how the car will traverse terrain - by default randomly but you
 	// can set this to something that actually routes the car to a destination.
 	public Navigation navigation;
 	public double vehicleTotalWaitTime = 0;
 	public double vehicleTotalTraveledDistance = 0;
 	public int spawnedAtSpawnRate = -1;
 	public boolean isBeingCreated = true;
 	
 	// cached
 	private Intersection nextIntersection;
 	private double distanceToNextIntersection;
 	private Node nodeAfterNextIntersection;
 	private Vehicle vehicleInFront;
 	private double distanceToVehicleInFront;
 	private boolean arrivedAtDestination;
 
 	public Vehicle(double lat, double lon, String anOriginNodeId, int laneNumber)
 			throws NoPathToDestinationException {
 		this.nodeTraversingMehanism = new NodeTraversingMehanism();
 
 		this.paused = false;
 		this.lat = lat;
 		this.lon = lon;
 		this.arrivedAtDestination = false;
 
 		// x = Global.projection.convertLongToX(lon);
 		// y = Global.projection.convertLatToY(lat);
 
 		setOriginNodeId(anOriginNodeId);
 		// setDestinationNodeId(aDestinationNodeId);
 		setOnLaneNumber(laneNumber);
 
 		this.navigation = null;
 	}
 
 	public double speed() {
 		return speed;
 	}
 
 	public NodeTraversingMehanism getNodeTraversingMehanism() {
 		return nodeTraversingMehanism;
 	}
 
 	public boolean vehicleOnLeft() {
 		return false;
 	}
 
 	public boolean vehicleOnRight() {
 		return false;
 	}
 
 	public void pause() {
 		paused = true;
 	}
 
 	public boolean paused() {
 		return paused;
 	}
 
 	public void setOnLaneNumber(int laneNumber) {
 		onLaneNumber = laneNumber;
 	}
 
 	public int getOnLaneNumber() {
 		return onLaneNumber;
 	}
 
 	private Node getNextNode() {
 		return navigation.nextNodeOnPath(nodeTraversingMehanism
 				.getDestinationNode().id);
 	}
 
 	// we're lying, the vehicle does some magic internally
 	// to handle going from node to node. this is only
 	// for node-to-node movements that aren't actual
 	// transportation infrastructure (intersections, stop signs, etc)
 	// for those situations the vehicle does nothing. the driver
 	// must figure out where he wants to take the car
 	// *this should only be called by the driver*
 	public void tick(double simTime, double tickLength, int tick) {
 	  if (hasArrivedAtDestination())
   	  return;
   	
 		if (isAtDestinationNode()) {
 			Node nextNode = getNextNode();
 			
 			setOriginNodeId(getDestinationNode().id);
 
 			if (nextNode != null) {
 				setDestinationNodeId(nextNode.id);
 
 				if (getOnLaneNumber() > getWay().lanes - 1)
 					setOnLaneNumber(getWay().lanes - 1);
 			} else {
 			  setDestinationNodeId(null);
 				this.flagForRemoval = true;
 				arrivedAtDestination = true;
 			}
 		}
 		
 		if (hasArrivedAtDestination())
   	  return;
 	  
 		_getNextIntersection();
 		_getVehicleInFront();
 	}
 	
 	public boolean hasArrivedAtDestination() {
 	  return arrivedAtDestination;
 	}
 
 	private double angleOfTravel() {
 	  if (hasArrivedAtDestination()) {
 	    return 0;
     }
     
 		Node lastNode = getOriginNode();
 		Node nextNode = getDestinationNode();
 		double angle = -Math.atan2((nextNode.lat - lastNode.lat),
 				(nextNode.lon - lastNode.lon));
 		angle = Math.toDegrees(angle);
 		if (angle < 0)
 			angle = 360 + angle;
 		return Math.toRadians(angle);
 	}
 
 	public void setAcceleration(double acceleration) {
 		if (acceleration > MAX_ACCELERATION)
 			this.acceleration = MAX_ACCELERATION;
 		else if (acceleration < MAX_NEG_ACCELERATION)
 			this.acceleration = MAX_NEG_ACCELERATION;
 		else
 			this.acceleration = acceleration;
 	}
 
 	private void determineCurrentVelocity(double tickLength) {
 		speed = speed + acceleration * tickLength;
 		if (speed > MAX_SPEED)
 			speed = MAX_SPEED;
 		else if (speed < 0 || speed < 0.01)
 			speed = 0;
 		setVelocity(this.speed * tickLength);
 	}
 
 	// move by velocity in the right direction
 	private void setVelocity(double speed) {
 		velocity = Utils.getVelocityVector(angleOfTravel(), speed,
 				getOriginNode(), getDestinationNode());
 	}
 
 	public boolean isCollidingWith(Vehicle v) {
 		// if (Math.abs(x-v.x) < 0.0000005 && Math.abs(y-v.y) < 0.0000005) {
 		// return true;
 		// }
 		return false;
 	}
 
 	public Way getWay() {
 		return Oracle.wayBetweenNodes(
 				nodeTraversingMehanism.getOriginNode().id,
 				nodeTraversingMehanism.getDestinationNode().id);
 	}
 
 	public boolean isGoingForwardOnWay() {
		if (nodeTraversingMehanism.getOriginNode() == null
				|| nodeTraversingMehanism.getDestinationNode() == null) {
 			// this is okay because it just means we haven't been able to
 			// determine it yet.
 			return true;
 		}
 
 		Way w = Oracle.wayBetweenNodes(
				nodeTraversingMehanism.getOriginNode().id,
				nodeTraversingMehanism.getDestinationNode().id);
 
 		if (w == null) {
 			System.out
 					.println("Could not determine direction of vehicle on way. ("
							+ nodeTraversingMehanism.getOriginNode().id
 							+ " - "
							+ nodeTraversingMehanism.getDestinationNode().id
 							+ " vehicle " + vin + ")");
 			return true;
 		} else {
			int oI = w.nd.indexOf(nodeTraversingMehanism.getOriginNode().id);
 			int dI = w.nd
					.indexOf(nodeTraversingMehanism.getDestinationNode().id);
 			return oI < dI;
 		}
 	}
 
 	public void setOriginNodeId(String nodeId) {
 		if (nodeTraversingMehanism.getOriginNode() != null)
 			Oracle.deregisterVehicleOrigin(vin,
 					nodeTraversingMehanism.getOriginNode().id);
 		nodeTraversingMehanism.setOriginNodeId(nodeId);
 		Oracle.registerVehicleOrigin(vin, nodeId);
 	}
 
 	public void setDestinationNodeId(String nodeId) {
 		nodeTraversingMehanism.setDestinationNodeId(nodeId);
 	}
 
 	public boolean isAtDestinationNode() {
 		// return nodeTraversingMehanism.distanceToDestinationNode(lat, lon) <
 		// DISTANCE_TO_CONSIDER_AS_SAME;
 		// two checks now..
 		if (nodeTraversingMehanism.distanceToDestinationNode(lat, lon) < DISTANCE_TO_CONSIDER_AS_SAME) {
 			return true;
 		} else if (nodeTraversingMehanism.distanceFromOriginNode(lat, lon) >= nodeTraversingMehanism
 				.distanceBetweenDestinationAndOriginNode()) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isAtOriginNode() {
 		return nodeTraversingMehanism.distanceFromOriginNode(lat, lon) < DISTANCE_TO_CONSIDER_AS_SAME;
 	}
 
 	public Node getOriginNode() {
 		return nodeTraversingMehanism.getOriginNode();
 	}
 
 	public Node getDestinationNode() {
 		return nodeTraversingMehanism.getDestinationNode();
 	}
 	
 	public Intersection getNextIntersection() {
 		return nextIntersection;
 	}
 	
 	public double getDistanceToNextIntersection() {
 		return distanceToNextIntersection;
 	}
 	
 	public Node getNodeAfterNextIntersection() {
 	  return nodeAfterNextIntersection;
 	}
 	
 	private void _getNextIntersection() {
 		Node destNode = Global.openStreetMap.getNode(nodeTraversingMehanism
 				.getDestinationNode().id);
 		// this is an intersection
 		if (destNode.connectedNodes.size() > 2) {
 			nextIntersection = IntersectionRegistry.getIntersectionAtNode(destNode);
 			distanceToNextIntersection = nodeTraversingMehanism.distanceToDestinationNode(lat, lon);
 			nodeAfterNextIntersection = navigation.nextNodeOnPath(nodeTraversingMehanism.getDestinationNode().id);
   		
 			if (nextIntersection == null) {
 				pause();
 				System.out.println("nextIntersection = " + nextIntersection);
 				System.out.println("d = " + distanceToNextIntersection);
 			}
 		}
 		// this is a simple node
 		else if (destNode.connectedNodes.size() == 2) {
 			nextIntersection = null;
 			distanceToNextIntersection = Double.MAX_VALUE;
 			nodeAfterNextIntersection = null;
 		}
 		// this is the end of the road
 		else if (destNode.connectedNodes.size() == 1) {
 			nextIntersection = null;
 			distanceToNextIntersection = -1;
 			nodeAfterNextIntersection = null;
 		}
 		else {
 			System.out.println("this also shouldn't happen...");
 			nextIntersection = null;
 			distanceToNextIntersection = -1;
 			nodeAfterNextIntersection = null;
 		}
 	}
 
 	public double getDistanceToVehicleInFront() {
 	  return distanceToVehicleInFront;
 	}
 	
 	public Vehicle getVehicleInFront() {
 	  return vehicleInFront;
 	}
 	
 	private void _getVehicleInFront() {
 		Vehicle vehicle = null;
 
 		List<String> vehicles = Oracle
 				.vehiclesWithNodeAsOrigin(nodeTraversingMehanism
 						.getOriginNode().id);
 		
 		if (vehicles == null) {
 			vehicles = new ArrayList<String>();
 		}
 		
 		for (String vin : vehicles) {
 			Vehicle v = VehicleRegistry.getVehicle(vin);
 
 			if (v == null) {
 				// System.out.println("vehicle with VIN " + vin +
 				// " is null (i am vehicle " + this.vin +
 				// ") (node "+originNodeId+")");
 				continue;
 			}
 
 			if (v == this
 			    || v.getDestinationNode() == null
 					|| !v.getDestinationNode().equals(getDestinationNode())
 					|| v.getOnLaneNumber() != getOnLaneNumber())
 				continue;
 
 			if (nodeTraversingMehanism.distanceToDestinationNode(lat, lon) > v
 					.getNodeTraversingMehanism().distanceToDestinationNode(
 							v.lat, v.lon)) {
 				if (vehicle == null
 						|| v.getNodeTraversingMehanism()
 								.distanceToDestinationNode(v.lat, v.lon) > vehicle
 								.getNodeTraversingMehanism()
 								.distanceToDestinationNode(vehicle.lat,
 										vehicle.lon))
 					vehicle = v;
 			}
 		}
 
 		// check ahead
 		if (vehicle == null)
 			vehicle = closestVehicleToNode(
 					nodeTraversingMehanism.getOriginNode(),
 					nodeTraversingMehanism.getDestinationNode());
 		
 		vehicleInFront = vehicle;
 		
 		if (vehicleInFront == null) {
 		  distanceToVehicleInFront = -1;
 		}
 		else {
 		  distanceToVehicleInFront = Utils.distance(lat, lon, vehicleInFront.lat, vehicleInFront.lon, 'K');
 		}
 	}
 
 	private Vehicle closestVehicleToNode(Node from, Node n) {
 		Node next = null;
 		if (n.connectedNodes.size() == 2) {
 			int i = n.connectedNodes.indexOf(from);
 			next = n.connectedNodes.get(i == 0 ? 1 : 0);
 		}
 
 		List<String> vehicles = Oracle.vehiclesWithNodeAsOrigin(n.id);
 
 		Vehicle vehicle = null;
 
 		if (vehicles != null) {
 			for (String vin : vehicles) {
 				Vehicle v = VehicleRegistry.getVehicle(vin);
 				
 				if (v == null
 				    || v.nodeTraversingMehanism.getDestinationNode() == null
 						|| v == this
 						|| v.getOnLaneNumber() != getOnLaneNumber()
 						|| !v.nodeTraversingMehanism.getDestinationNode()
 								.equals(next))
 					continue;
 
 				if (vehicle == null
 						|| v.nodeTraversingMehanism.distanceFromOriginNode(
 								v.lat, v.lon) < vehicle.nodeTraversingMehanism
 								.distanceFromOriginNode(vehicle.lat,
 										vehicle.lon))
 					vehicle = v;
 			}
 		}
 
 		if (vehicle == null) {
 			if (n.connectedNodes.size() == 2) {
 				return closestVehicleToNode(n, next);
 			} else {
 				return null;
 			}
 		}
 
 		return vehicle;
 	}
 
 	public void commit(double simTime, double tickLength) {
 		if (paused()) {
 			System.out.println("skipping " + vin + " p = " + paused());
 			return;
 		}
 		
 		determineCurrentVelocity(tickLength);
 		Vector2d curVelocity = velocity;
 		
 		double distToDestination;
 		
 		if (hasArrivedAtDestination()) {
 		  distToDestination = 0;
 		}
 		else {
 		  distToDestination = nodeTraversingMehanism
   				.distanceToDestinationNode(lat, lon);
 		}
 
 		if (velocity.mag() > distToDestination) {
 			if (distToDestination > DISTANCE_TO_CONSIDER_AS_SAME) {
 				lat = getDestinationNode().lat;
 				lon = getDestinationNode().lon;
 			}
 			return;
 		} else {
 			lat = lat + curVelocity.x;
 			lon = lon + curVelocity.y;
 		}
 
 		trackStatistics(simTime, tickLength);
 	}
 
 	public void trackStatistics(double simTime, double tickLength) {
 		Way w = getWay();
 		if (!paused() && w != null && speed < w.getSpeedLimit() * 0.9)
 			vehicleTotalWaitTime += tickLength;
 		vehicleTotalTraveledDistance += (speed * tickLength)/(60*60);
 	}
 }
