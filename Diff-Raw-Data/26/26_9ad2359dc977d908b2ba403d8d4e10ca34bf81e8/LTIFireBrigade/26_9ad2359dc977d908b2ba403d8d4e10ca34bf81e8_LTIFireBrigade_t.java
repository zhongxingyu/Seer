 package lti.agent.fire;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import lti.agent.AbstractLTIAgent;
 import lti.message.Message;
 import lti.utils.BuildingPoint;
 import lti.utils.EntityIDComparator;
 import lti.utils.GrahamScan;
 import lti.utils.Point2D;
 import rescuecore2.messages.Command;
 import rescuecore2.misc.Pair;
 import rescuecore2.standard.entities.AmbulanceTeam;
 import rescuecore2.standard.entities.Building;
 import rescuecore2.standard.entities.Civilian;
 import rescuecore2.standard.entities.FireBrigade;
 import rescuecore2.standard.entities.GasStation;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.standard.entities.Hydrant;
 import rescuecore2.standard.entities.PoliceOffice;
 import rescuecore2.standard.entities.Refuge;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.standard.entities.StandardEntityConstants;
 import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
 import rescuecore2.standard.entities.StandardEntityURN;
 import rescuecore2.worldmodel.ChangeSet;
 import rescuecore2.worldmodel.EntityID;
 
 public class LTIFireBrigade extends AbstractLTIAgent<FireBrigade> {
 
 	private static final String MAX_WATER_KEY = "fire.tank.maximum";
 	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
 	private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";
 
 	private int maxWater;
 	private int maxDistance;
 	private int maxPower;
 	private List<EntityID> refuges;
 	private List<EntityID> fireBrigadesList;
 	private List<EntityID> gasStationList;
 	private Set<EntityID> gasStationNeighbours;
 	private int dangerousDistance;
 	private Set<EntityID> burntBuildings;
 	
 	private HashMap<Fieryness, Double> fierynessPriority;
 
 	private static enum State {
 		MOVING_TO_REFUGE, MOVING_TO_HYDRANT, MOVING_TO_FIRE, 
 		MOVING_TO_GAS, MOVING_CLOSE_TO_GAS, RANDOM_WALKING, TAKING_ALTERNATE_ROUTE,
 		EXTINGUISHING_FIRE, REFILLING, DEAD, BURIED
 	};
 
 	private State state;
 	
 	private DistanceComparator DISTANCE_COMPARATOR;
 
 	@Override
 	protected void postConnect() {
 		super.postConnect();
 		currentX = me().getX();
 		currentY = me().getY();
 		
 		DISTANCE_COMPARATOR = new DistanceComparator(this.getID());
 		
 		Set<EntityID> fireBrigades = new TreeSet<EntityID>(
 				new EntityIDComparator());
 
 		for (StandardEntity e : model
 				.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
 			fireBrigades.add(e.getID());
 		}
 
 		fireBrigadesList = new ArrayList<EntityID>(fireBrigades);
 		
 		internalID = fireBrigadesList.indexOf(me().getID()) + 1;
 
 		model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
 		maxWater = config.getIntValue(MAX_WATER_KEY);
 		maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
 		maxPower = config.getIntValue(MAX_POWER_KEY);
 
 		refuges = new ArrayList<EntityID>();
 		List<Refuge> ref = getRefuges();
 
 		for (Refuge r : ref) {
 			refuges.add(r.getID());
 		}
 		
 		dangerousDistance = 25000;
 		
 		gasStationList = new ArrayList<EntityID>();
 		for(StandardEntity next : model.getEntitiesOfType(StandardEntityURN.GAS_STATION))
 			gasStationList.add(next.getID());
 		
 		gasStationNeighbours = calculateGasStationNeighbours();
 		
 		fierynessPriority = setFierynessPriority();
 		burntBuildings = new HashSet<EntityID>();
 		
 		changeState(State.RANDOM_WALKING);
 	}
 
 	private HashMap<Fieryness, Double> setFierynessPriority() {
 		HashMap<Fieryness, Double> fP = new HashMap<Fieryness, Double>();
 		fP.put(StandardEntityConstants.Fieryness.UNBURNT, 0.0);
 		fP.put(StandardEntityConstants.Fieryness.WATER_DAMAGE, 0.0);
 		fP.put(StandardEntityConstants.Fieryness.HEATING, 0.3);
 		fP.put(StandardEntityConstants.Fieryness.BURNING, 0.8);
 		fP.put(StandardEntityConstants.Fieryness.INFERNO, 0.7);
 		fP.put(StandardEntityConstants.Fieryness.MINOR_DAMAGE, 0.0);
 		fP.put(StandardEntityConstants.Fieryness.MODERATE_DAMAGE, 0.0);
 		fP.put(StandardEntityConstants.Fieryness.SEVERE_DAMAGE, 0.0);
 		fP.put(StandardEntityConstants.Fieryness.BURNT_OUT, 0.0);
 		
 		return fP;
 	}
 
 	@Override
 	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
 		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
 	}
 
 	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
 		super.think(time, changed, heard);
 
 		if (me().getHP() == 0) {
 			changeState(State.DEAD);
 			return;
 		}
 
 		sendMessageAboutPerceptions(changed);
 		
 		if (me().getBuriedness() != 0) {
 			changeState(State.BURIED);
 			return;
 		}
 		
 		// Verify if you are blocked
 		if (amIBlocked(time)) {
 			log("Blocked! Random walk to escape");
 			changeState(State.RANDOM_WALKING);
 			List<EntityID> path = randomWalk();
 			sendMove(time, path);
 			return;
 		}
 
 		dropTask(time, changed);
 
 		if (target == null) {
 			target = selectTask();
 		}
 
 		// There is no need to stay inside a burning building, right?
 		if (location() instanceof Building) {
 			if (((Building) location()).isOnFire()) {
 				List<EntityID> path = randomWalk();
 
 				sendMove(time, path);
 				changeState(State.RANDOM_WALKING);
 				log("Leaving a burning building");
 				return;
 			}
 		}
 
 		// Am I at a refuge or a hydrant?
 		if ((location() instanceof Refuge || location() instanceof Hydrant) && me().isWaterDefined()
 				&& me().getWater() < maxWater) {
 			sendRest(time);
 			changeState(State.REFILLING);
 			return;
 		}
 
 		// Am I out of water?
 		if (me().isWaterDefined() && me().getWater() == 0) {
 			List<EntityID> path = search.breadthFirstSearch(location().getID(),
 					refuges);
 			changeState(State.MOVING_TO_REFUGE);
 
 			if (path == null) {
 				path = randomWalk();
 				log("Trying to move to refugee, but couldn't find path");
 				changeState(State.RANDOM_WALKING);
 			}
 			target = path.get(path.size() - 1);
 			sendMove(time, path);
 			return;
 		}
 
 		if (target != null) {
 			LinkedList<EntityID> targetCluster = getFireCluster(target);
 			
 			if(targetCluster.size() > 1){
 				List<EntityID> convexHull = getConvexHull(targetCluster);
 				List<EntityID> aux = new ArrayList<EntityID>(convexHull);
 				
 				for(EntityID id : convexHull){
 					log("Convex Hull contains: " + id);
 				}
 				
 				// we then check for the closest building on fire
 				for (EntityID entityID : convexHull) {
 					Building building = (Building) model.getEntity(entityID);
 					
 					if(building.isFierynessDefined()){
 						if (building.getFierynessEnum().equals(StandardEntityConstants.Fieryness.BURNT_OUT)){
 							aux.remove(entityID);
 						}
 					}
 				}
 				
 				target = aux.get(this.internalID % aux.size());
 				
 				log("Convex Hull - Target from: " + target);
 			}
 			
 			// Once the target is determined, we refresh the tasks
 			this.refreshMyTasks(target);
 			
 			if (model.getDistance(location().getID(), target) < maxDistance) {
 				sendExtinguish(time, target, maxPower);
 				changeState(State.EXTINGUISHING_FIRE);
 				return;
 			}
 			
 			List<EntityID> path = search.breadthFirstSearch(location().getID(), target);
 
 			if (path != null) {
 				path.remove(path.size() - 1);
 				sendMove(time, path);
 				
 				if(model.getEntity(target) instanceof GasStation)
 					changeState(State.MOVING_TO_GAS);
 				else if(closeToGas(target))
 					changeState(State.MOVING_CLOSE_TO_GAS);
 				else
 					changeState(State.MOVING_TO_FIRE);
 
 				if (!path.isEmpty()) {
 					target = path.get(path.size() - 1);
 				}
 
 				return;
 			}
 		}
 		
 		// If it has nothing to do and water level is below 80%, start finding a place to refill it
 		if(me().isWaterDefined() && me().getWater() < 0.8*this.maxWater){
 			log("I have nothing to do, i'll reffil, my water level is at " + ((float) me().getWater()/this.maxWater) + "!");
 			
 			// We analyze first if the level is below 50%
 			// If it is, we search for a refuge
 			if(me().getWater() < 0.5*this.maxWater){
 				log("Finding a refuge");
 				List<EntityID> path = search.breadthFirstSearch(location().getID(),
 						refuges);
 				changeState(State.MOVING_TO_REFUGE);
 
 				if (path == null) {
 					path = randomWalk();
 					log("Trying to move to refugee, but couldn't find path");
 					changeState(State.RANDOM_WALKING);
 				}
 				target = path.get(path.size() - 1);
 				sendMove(time, path);
 				return;
 			}
 			else{
 				log("Finding a hydrant");
 				List<EntityID> path = search.breadthFirstSearch(location().getID(),
 						getHydrants());
 				changeState(State.MOVING_TO_HYDRANT);
 
 				if (path == null) {
 					path = randomWalk();
 					log("Trying to move to hydrant, but couldn't find path");
 					changeState(State.RANDOM_WALKING);
 				}
 				target = path.get(path.size() - 1);
 				sendMove(time, path);
 				return;
 			}
 		}
 
 		List<EntityID> path = randomWalk();
 		sendMove(time, path);
 		changeState(State.RANDOM_WALKING);
 		return;
 	}
 	
 	private void sendMessageAboutPerceptions(ChangeSet changed) {
 		// Send a message about all the perceptions
 		Message msg = composeMessage(changed);
 
 		if (this.channelComm) {
 			if (!msg.getParameters().isEmpty() && !channelList.isEmpty()) {
 				for (Pair<Integer,Integer> channel : channelList) {
 					sendSpeak(currentTime, channel.first(),
 							msg.getMessage(channel.second().intValue()));
 				}
 			}
 		}
 	}
 	
 	protected List<EntityID> getHydrants(){
 		List<EntityID> result = new ArrayList<EntityID>();
 		Collection<StandardEntity> b = model.
 				getEntitiesOfType(StandardEntityURN.HYDRANT); // List of hydrants
 		
 		for(StandardEntity next : b){
 			if(next instanceof Hydrant){
 				result.add(next.getID());;
 			}
 		}
 		
 		return result;
 	}
 
 	@Override
 	protected boolean amIBlocked(int time) {
 		return math.geom2d.Point2D.distance(lastX, lastY, currentX, currentY) < MIN_WALK_LENGTH
 					&& isMovingState()
 					&& time > 3;
 	}
 
 	@Override
 	protected void refreshTaskTable(ChangeSet changed) {
 		Set<EntityID> fires = new HashSet<EntityID>();
 		burntBuildings = new HashSet<EntityID>();
 
 		for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.BUILDING)) {
 			Building b = (Building) next;
 			if (b.isOnFire())
 				fires.add(next.getID());
 			if (b.isFierynessDefined()
 					&& b.getFierynessEnum()
 					.equals(StandardEntityConstants.Fieryness.BURNT_OUT))
 				burntBuildings.add(next.getID());
 		}
 
 		taskTable.keySet().retainAll(fires);
 
 		for (EntityID next : fires) {
 			if (!taskTable.containsKey(next)) {
 				taskTable.put(next, new HashSet<EntityID>());
 			}
 		}
 	}
 	
 	@Override
 	protected EntityID selectTask() {
 		EntityID result = null;
 		
 		List<EntityID> onFireList = new ArrayList<EntityID>(taskTable.keySet());
 		
 		// Get the closest building
 		if(!onFireList.isEmpty()){
 			Collections.sort(onFireList, DISTANCE_COMPARATOR);
 			
 			result = onFireList.get(0);
 			log("Closest building:" + result);
 		}
 
 		return result;
 	}
 	
 	private void refreshMyTasks(EntityID target){
 		for (Set<EntityID> agents : taskTable.values()) {
 			if (agents != null) {
 				agents.remove(me().getID());
 			}
 		}
 
 		taskTable.get(target).add(me().getID());
 	}
 	
 	// Not used in the moment => because of ConvexHull
 	@SuppressWarnings("unused")
 	private double getBuildingPriority(EntityID building){
 		int distanceToBuilding; // The distace to the target
 		int nbSafeNeighbours = 0; // The number of neighbours that haven't start burning
 		boolean presenceOfCivilians = false;
 		boolean presenceOfAgents = false;
 		boolean closeToGas = false;
 		boolean nearGas = false;
 		boolean isGasStation = false;
 		
 		double result;
 		
 		Collection<StandardEntity> buildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
 		Collection<StandardEntity> people = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM,
 																StandardEntityURN.FIRE_BRIGADE,
 																StandardEntityURN.POLICE_FORCE,
 																StandardEntityURN.CIVILIAN);
 		
 		distanceToBuilding = model.getDistance(me().getID(), building);
 		
 		for(StandardEntity bd : buildings){
 			if(bd instanceof Building){
 				// If it is in a dangerous distance, that may allow the fire to spread
 				// And it's not on fire then give high priority
 				if(model.getDistance(building, bd.getID()) < dangerousDistance && !((Building) bd).isOnFire()){
 						nbSafeNeighbours++;
 				}
 				if(gasStationNeighbours.contains(bd.getID())){
 					closeToGas = true;
 				}
 			}
 		}
 		
 		for(StandardEntity person : people){
 			// We get only buildings that have alive humans
 			if(((Human) person).isHPDefined() && ((Human) person).getHP() > 0){
 				// We check if it's a Ambulance, Fire Brigade or Police Force
 				if(person instanceof AmbulanceTeam || person instanceof FireBrigade || person instanceof PoliceOffice){
 					presenceOfAgents = true;
 				}
 				// We check if there are civilians in the building
 				if(person instanceof Civilian){
 					presenceOfCivilians = true;
 				}
 			}
 		}
 		
 		if(gasStationNeighbours.contains(building)){
 			nearGas = true;
 		}
 		
 		if(gasStationList.contains(building)){
 			isGasStation = true;
 		}
 		
 		result = 0;
 		if (distanceToBuilding >= 0){
 			result -= distanceToBuilding/maxDistance*maxPower;
 		}
 		result += nbSafeNeighbours/2*maxPower;
 		result += closeToGas ? 3*maxPower : 0;
 		result += nearGas ? 5*maxPower : 0;
 		result += isGasStation ? 10*maxPower : 0;
 		result += presenceOfAgents ? 2*maxPower : 0;
 		result += presenceOfCivilians ? maxPower : 0;
 		if(((Building) model.getEntity(building)).isFierynessDefined()){
 			result += fierynessPriority.get(((Building) model.getEntity(building)).getFierynessEnum())*maxPower;
 		}
 		
 		return result;
 	}
 
 	@Override
 	protected void dropTask(int time, ChangeSet changed) {
 		if (!taskTable.containsKey(target)) {
 			taskDropped = target;
 			target = null;
 		}
 
 		if (amIBlocked(time)) {
 			taskDropped = target;
 			target = null;
 		}
 
 	}
 
 	private boolean isMovingState() {
 		List<State> ss = new ArrayList<State>();
 		ss.add(State.MOVING_TO_FIRE);
 		ss.add(State.MOVING_TO_REFUGE);
 		ss.add(State.RANDOM_WALKING);
 		ss.add(State.TAKING_ALTERNATE_ROUTE);
 
 		return ss.contains(state);
 	}
 	
 	private void changeState(State state) {
 		this.state = state;
 		log("Changed state to: " + this.state);
 	}
 	
 	// Used to calculate all the buildings near gas stations
 	private Set<EntityID> calculateGasStationNeighbours(){
 		Set<EntityID> result = new HashSet<EntityID>();
 		
 		Collection<StandardEntity> gasStations = model.
 				getEntitiesOfType(StandardEntityURN.GAS_STATION); // Getting the list of Gas Stations
 		Collection<StandardEntity> buildings = model.
 				getEntitiesOfType(StandardEntityURN.BUILDING); // Getting the list of buildings
 		for(StandardEntity gs : gasStations){
 			if(gs instanceof GasStation){
 				log("GasStation: " + gs.getID());
 				
 				for(StandardEntity bd : buildings){
 					if(bd instanceof Building){
 						if(model.getDistance(gs.getID(), bd.getID()) < dangerousDistance){
 							log("Close building! Adding " + bd.getID() + " to neighbours!");
 							result.add(bd.getID());
 						}
 					}
 				}
 			}
 		}
 
 		
 		return result;
 	}
 	
 	// Verify if is a building is close to a gas station
 	private boolean closeToGas(EntityID ent){
 		if(gasStationNeighbours.contains(ent))
 			return true;
 		
 		return false;
 	}
 	
 	
 	// Distance comparator
 	private class DistanceComparator implements Comparator<EntityID>{
 		private EntityID agent;
 		
 		public DistanceComparator(EntityID agent){
 			this.agent = agent;
 		}
 		
 		@Override
 		public int compare(EntityID o1, EntityID o2) {
 			int distance1 = model.getDistance(agent, o1);
 			int distance2 = model.getDistance(agent, o2);
 			
 			if(distance1 < distance2)	return -1;
 			if(distance1 > distance2)	return 1;
 			return 0;
 		}
 		
 	}
 	
 	// Return a cluster of fire => All the buildings close to a building on fire
 	private LinkedList<EntityID> getFireCluster(EntityID onFireBuilding){
 		LinkedList<EntityID> cluster = new LinkedList<EntityID>();
 		Set<EntityID> onFire = new HashSet<EntityID>(taskTable.keySet());
 		onFire.addAll(burntBuildings);
 		//for(EntityID bb : onFire)
 		//	log("Building which is candidate for cluster: " + model.getEntity(bb));
 		
 		cluster.addLast(onFireBuilding);
 		onFire.remove(onFireBuilding);
 		
 		for(int i = 0; i < cluster.size(); i++) {
 			EntityID clusterBuilding = cluster.get(i);
 			for(EntityID otherBuilding : onFire){
 				if(model.getDistance(clusterBuilding, otherBuilding) < dangerousDistance){
 					cluster.addLast(otherBuilding);
 				}
 			}
 			onFire.removeAll(cluster);
 		}
 		
 		return cluster;
 	}
 	
 	private List<EntityID> getConvexHull(LinkedList<EntityID> cluster){
 		List<BuildingPoint> points = new ArrayList<BuildingPoint>();
 		
 		for(EntityID buildingID : cluster){
 			Building building = (Building) model.getEntity(buildingID);
 			
 			int[] apexes = building.getApexList();
 			for(int i = 0; i < apexes.length; i = i + 2){
 				points.add(new BuildingPoint(apexes[i], apexes[i+1], buildingID));
 			}
 		}
 		
 		Point2D[] pointArray = new Point2D[points.size()];
 		int i = 0;
 		for(BuildingPoint point : points){
 			pointArray[i] = point;
 			i++;
 		}
 		
 		GrahamScan convexHull = new GrahamScan(pointArray);
 		List<EntityID> convexList = new ArrayList<EntityID>(convexHull.getBuildings());
 		
 		// Collections.sort(convexList, DISTANCE_COMPARATOR);
 		
 		return convexList;
 	}
 }
