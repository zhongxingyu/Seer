 package lti.agent.police;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 
 import lti.agent.AbstractLTIAgent;
 import lti.message.Message;
 import lti.message.type.BuildingEntranceCleared;
 import lti.utils.EntityIDComparator;
 import math.geom2d.Point2D;
 import math.geom2d.polygon.Polygons2D;
 import math.geom2d.polygon.SimplePolygon2D;
 import area.Sector;
 import area.Sectorization;
 import rescuecore2.messages.Command;
 import rescuecore2.misc.Pair;
 import rescuecore2.misc.geometry.Line2D;
 import rescuecore2.misc.geometry.Vector2D;
 import rescuecore2.standard.entities.Area;
 import rescuecore2.standard.entities.Blockade;
 import rescuecore2.standard.entities.Building;
 import rescuecore2.standard.entities.Edge;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.standard.entities.PoliceForce;
 import rescuecore2.standard.entities.Refuge;
 import rescuecore2.standard.entities.Road;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.standard.entities.StandardEntityURN;
 import rescuecore2.worldmodel.ChangeSet;
 import rescuecore2.worldmodel.EntityID;
 
 public class LTIPoliceForce extends AbstractLTIAgent<PoliceForce> {
 
 	private static final String DISTANCE_KEY = "clear.repair.distance";
 
 	private static final String REPAIR_RATE_KEY = "clear.repair.rate";
 	
 	private static final String REPAIR_RAD_KEY = "clear.repair.rad";
 
 	private int minClearDistance;
 
 	private int repairRate;
 	
 	private int repairRad;
 
 	private Sectorization sectorization;
 	
 	private Sector sector;
 	
 	private List<Sector> sectorsLeftToSearch;
 
 	private State state = null;
 
 	private static enum State {
 		RETURNING_TO_SECTOR, MOVING_TO_TARGET,
 		MOVING_TO_ENTRANCE_BUILDING, RANDOM_WALKING, MOVING_TO_UNBLOCK,
 		CLEARING, BURIED, DEAD, CLEARING_PATH
 	};
 
 	private EntityID obstructingBlockade;
 
 	private List<EntityID> policeForcesList;
 
 	private Set<EntityID> buildingEntrancesToBeCleared;
 
 	private boolean clearEntranceTask;
 
 	private int lastRepairCost;
 
 	private State lastState;
 
 	private EntityID lastTarget;
 	
 	//private EntityID lastObstructingBlockade;
 
 	private List<EntityID> path;
 	
 	private List<EntityID> refuges;
 	
 	private Set<EntityID> clearedPathTo;
 
 	@Override
 	protected void postConnect() {
 		super.postConnect();
 
 		inicializaVariaveis();
 
 		changeState(State.RANDOM_WALKING);
 
 		defineSectorRelatedVariables();
 
 		buildingEntrancesToBeCleared = getBuildingEntrancesToBeCleared(this.sector);
 		
 		refuges = new ArrayList<EntityID>();
 		
 		for(Refuge refuge: getRefuges()){
 			refuges.add(refuge.getID());
 		}
 	}
 
 	/**
 	 * Define the number of divisions, sectorize the world, print the sectors
 	 * into a file, define the working sector of this instance of the agent and
 	 * keep the list of the sectors that can be used during the simulation as a
 	 * working sector
 	 */
 	private void defineSectorRelatedVariables() {
 		sectorization = new Sectorization(model, neighbours,
 				policeForcesList.size(), verbose);
 
 		sector = sectorization.getSector(internalID);
 
 		sectorsLeftToSearch = sectorization.getSectorsAsList();
 		sectorsLeftToSearch.remove(sector);
 
 		log("Defined sector: " + sector);
 	}
 
 	/**
 	 * Inicializa as variáveis utilizadas pelo agente
 	 */
 	private void inicializaVariaveis() {
 		currentX = me().getX();
 		currentY = me().getY();
 		clearEntranceTask = true;
 		path = null;
 		lastRepairCost = -1;
 
 		Set<EntityID> policeForces = new TreeSet<EntityID>(
 				new EntityIDComparator());
 
 		for (StandardEntity e : model
 				.getEntitiesOfType(StandardEntityURN.POLICE_FORCE)) {
 			policeForces.add(e.getID());
 		}
 
 		policeForcesList = new ArrayList<EntityID>(policeForces);
 
 		internalID = policeForcesList.indexOf(me().getID()) + 1;
 
 		minClearDistance = config.getIntValue(DISTANCE_KEY, 10000);
 
 		repairRate = config.getIntValue(REPAIR_RATE_KEY, 10);
 		
 		repairRad = config.getIntValue(REPAIR_RAD_KEY, 1250);
 
 		obstructingBlockade = null;
 		
 		clearedPathTo = new HashSet<EntityID>();
 	}
 
 	private Set<EntityID> getBuildingEntrancesToBeCleared(Sector s) {
 		Set<EntityID> buildingEntrances = new HashSet<EntityID>();
 		if (clearEntranceTask) {
 			for (EntityID buildingID : buildingIDs) {
 				if (s.getLocations().keySet().contains(buildingID)) {
 					Building building = (Building) model.getEntity(buildingID);
 					if (building != null)
 						for (EntityID neighbourID : building.getNeighbours())
 							if (model.getEntity(neighbourID) instanceof Road)
 								buildingEntrances.add(neighbourID);
 				}
 			}
 			log("There are " + buildingEntrances.size()
 					+ " building entrances to be cleared");
 		}
 
 		return buildingEntrances;
 	}
 
 	@Override
 	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
 		super.think(time, changed, heard);
 		recalculaVariaveisCiclo();
 
 		if (me().getHP() == 0) {
 			changeState(State.DEAD);
 			return;
 		}
 
 		if (me().getBuriedness() != 0) {
 			if (target != null) {
 				taskDropped = target;
 				target = null;
 				log("Dropped task: " + taskDropped);
 			}
 			sendMessageAboutPerceptions(changed, new Message());
 			changeState(State.BURIED);
 			return;
 		}
 		
		if (currentTime >= 100 && (currentTime+2*internalID) % 100 == 0) {
			clearedPathTo.removeAll(refuges);
		}
		
 		if (model.getEntity(currentPosition) instanceof Building) {
 			Building b = (Building)model.getEntity(currentPosition);
 			if (b.isFierynessDefined() && b.isOnFire()) {
 				path = randomWalk();
 				if (path != null) {
 					moveIfPathClear(changed, path);
 					return;
 				}
 			}
 		}
 		
 		log("clearedPathTo: " + clearedPathTo);
 
 		// Keep order between methods because verifyBuildingEntrancesToBeCleared
 		// can decide that agent has to drop the task and get another one
 		// but sendMessageAboutPerceptions also sends messages about task dropping
 		// and selection
 		Message msg = verifyBuildingEntrancesToBeCleared(new Message());
 
 		sendMessageAboutPerceptions(changed, msg);
 		
 		evaluateTaskDroppingAndSelection(changed);
 		
 		// If I'm blocked it's probably because there's an obstructing blockade
 		if (amIBlocked(time)) {
 			obstructingBlockade = getBestClosestBlockadeToClear(changed);
 			if (obstructingBlockade != null)
 				clearObstructingBlockade();
 			else
 				movingToUnblock();
 			return;
 		}
 
 		int repet = 0;
 		while (target != null && repet < 3) {
 			// Work on the task, if you have one
 			StandardEntity e = model.getEntity(target);
 			if (e instanceof Human && ((Human)e).isPositionDefined()) {
 				if (!(model.getEntity(((Human)e).getPosition()) instanceof Area)) {
 					clearedPathTo.add(target);
 					target = null;
 				}
 			}
 					
 			if (target != null) {	
 				e = model.getEntity(target);
 				if (e instanceof Blockade) {			
 					if (workedOnTargetBlockade(changed))
 						return;
 				} else if (e instanceof Human
 						&& !victimTrappedInBuilding(target)) {
 					if (workedOnTargetVictim(changed))
 						return;
 				} else if (e instanceof Building
 						|| victimTrappedInBuilding(target)) {
 					if (workedOnTargetBuilding(changed))
 						return;
 				}
 			}
 			
 			if (target == null) {
 				repet++;
 				target = selectTask();
 			} else
 				break;
 		}
 
 		// Move around the map
 
 		if (clearEntranceTask)
 			path = getPathToEntranceTarget();
 		else
 			path = randomWalk();
 
 		if (path != null) {
 			moveIfPathClear(changed, path);
 			return;
 		}
 	}
 
 	private boolean workedOnTargetVictim(ChangeSet changed) {
 		Human victim = (Human) model.getEntity(target);
 		
 		Area areaEntity = (Area) model.getEntity(victim.getPosition());
 		List<EntityID> closestRoadIds = new ArrayList<EntityID>();
 		if (areaEntity instanceof Building) {
 			for (EntityID e : areaEntity.getNeighbours())
 				if (model.getEntity(e) instanceof Road)
 					closestRoadIds.add(e);
 		} else if (areaEntity instanceof Road) {
 			closestRoadIds.add(areaEntity.getID());
 		}
 		if (closestRoadIds.size() > 0) {
 			path = getPathToTarget(closestRoadIds);
 		
 			if (path != null && path.size() > 0) {
 				StandardEntity e = model.getEntity(path.get(path.size()-1));
 				if (e instanceof Road) {
 					if (path.size() == 1 && currentPosition.equals(path.get(0))) {
 						return clearPathToTargetIfNotCleared();
 					}
 					if (path.size() == 1 && !currentPosition.equals(path.get(0))) {
 						if (isTargetAlreadyCleared())
 							return false;
 					}
 					
 					Point2D victimLocation = new Point2D(areaEntity.getX(), areaEntity.getY());
 					if (victim.isXDefined() && victim.isYDefined()) {
 						victimLocation = new Point2D(victim.getX(), victim.getY());
 					}
 					
 					moveToTargetIfPathClear(changed, path, victimLocation);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean isTargetAlreadyCleared() {
 		EntityID usefulTarget = target;
 		StandardEntity stdEnt = model.getEntity(target);
 		if (stdEnt instanceof Human && victimTrappedInBuilding(target))
 			usefulTarget = getBuildingFromVictim((Human)stdEnt);
 		
 		List<EntityID> path2 = new ArrayList<EntityID>(path);
 		path2.add(usefulTarget);
 		if (hasClearPathUntilTarget(path2)) {
 			if (path != null && path.size() >= 1 && 
 					buildingEntrancesToBeCleared.contains(path.get(path.size()-1))) {
 				buildingEntrancesCleared.add(path.get(path.size()-1));
 				buildingEntrancesToBeCleared.remove(path.get(path.size()-1));
 			}
 			
 			clearedPathTo.add(usefulTarget);
 			log("Cleared path to target " + model.getEntity(target) +" before getting to it ");
 			target = null;
 			
 			return true;
 		}
 		return false;
 	}
 
 	private boolean hasClearPathUntilTarget(List<EntityID> path) {
 		int nextTargetX = 0, nextTargetY = 0, lastTargetX = 0, lastTargetY = 0;
 		
 		if (path.size() < 2)
 			return false;
 		
 		Area currentArea = (Area) model.getEntity(currentPosition);
 		Area nextArea = null, lastArea = null;
 		StandardEntity e = model.getEntity(path.get(path.size() - 1));
 		int clearLength = 80*minClearDistance/100;
 		
 		if (e instanceof Area) {
 			Edge edge = ((Area) e).getEdgeTo(path.get(path.size() - 2));
 			if (edge == null) {
 				log("ERRO: Edge vazio");
 				return false;
 			}
 			lastArea = (Area) e;
 			
 			lastTargetX = (edge.getEndX()+edge.getStartX())/2;
 			lastTargetY = (edge.getEndY()+edge.getStartY())/2;
 		} else if (e instanceof Human) {
 			if (!((Human) e).isXDefined() || !((Human) e).isYDefined())
 				return false;
 			
 			lastArea = (Area) model.getEntity(((Human) e).getPosition());
 			
 			lastTargetX = ((Human) e).getX();
 			lastTargetY = ((Human) e).getY();
 			
 			double dx = Math.abs(lastTargetX - currentX);
 			double dy = Math.abs(lastTargetY - currentY);
 			clearLength = Math.min(clearLength, (int)Math.hypot(dx, dy) + 50);
 		} else {
 			log("ERRO: Path with unknown step");
 		}
 		
 		Edge edge = currentArea.getEdgeTo(path.get(0));
 		if (edge == null) {
 			log("ERRO: Edge vazio");
 			return false;
 		}
 		nextArea = (Area) model.getEntity(edge.getNeighbour());
 		
 		nextTargetX = (edge.getEndX()+edge.getStartX())/2;
 		nextTargetY = (edge.getEndY()+edge.getStartY())/2;
 		
 		SimplePolygon2D clearArea = getClearArea(me(), nextTargetX,
 				nextTargetY, clearLength, 20*repairRad/100, false);
 		
 		log("ClearArea-until-target: length:" + clearLength + ", target=(" + nextTargetX + ", " + nextTargetY + ")");
 		
 		if (!clearArea.contains(lastTargetX, lastTargetY))
 			return false;
 		
 		log("I can tell if the path is already cleared from here to @(" + lastTargetX + ", " + lastTargetY + ")");
 		
 		Set<EntityID> possibleObstructingBlockades = new HashSet<EntityID>();
 		if (currentArea.getBlockades() != null)
 			possibleObstructingBlockades.addAll(currentArea.getBlockades());
 		if (nextArea.getBlockades() != null)
 			possibleObstructingBlockades.addAll(nextArea.getBlockades());
 		if (lastArea.getBlockades() != null)
 			possibleObstructingBlockades.addAll(lastArea.getBlockades());
 		
 		for (EntityID id : possibleObstructingBlockades) {
 			Blockade b = (Blockade) model.getEntity(id);
 			SimplePolygon2D blockadePolygon = getBlockadePolygon(b);
 			if (blockadePolygon != null && !Polygons2D.intersection(clearArea, blockadePolygon).isEmpty()) {
 				log("Cl-Blockade: " + b.getID() + ", " + b.getRepairCost());
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	private boolean workedOnTargetBuilding(ChangeSet changed) {
 		EntityID usefulTarget = target;
 		StandardEntity stdEnt = model.getEntity(target);
 		if (stdEnt instanceof Human)
 			usefulTarget = getBuildingFromVictim((Human)stdEnt);
 		
 		if (usefulTarget == null) {
 			log("ERRO: usefulTarget nulo");
 			return false;
 		}
 		
 		Building areaEntity = (Building) model.getEntity(usefulTarget);
 		List<EntityID> closestRoadIds = new ArrayList<EntityID>();
 		closestRoadIds.addAll(areaEntity.getNeighbours());
 		if (closestRoadIds.size() > 0) {
 			path = getPathToTarget(closestRoadIds);
 		
 			if (path != null && path.size() > 0) {
 				StandardEntity e = model.getEntity(path.get(path.size()-1));
 				if (e instanceof Road) {
 					if (path.size() == 1 && currentPosition.equals(path.get(0))) {
 						return clearPathToTargetIfNotCleared();
 					}
 					if (path.size() == 1 && !currentPosition.equals(path.get(0))) {
 						if (isTargetAlreadyCleared())
 							return false;
 					}
 					
 					Edge edge = areaEntity.getEdgeTo(e.getID());
 					int edge_x = (edge.getStartX()+edge.getEndX())/2;
 					int edge_y = (edge.getStartY()+edge.getEndY())/2;
 					Point2D center_edge = new Point2D(edge_x, edge_y);
 					Point2D center_entrance = new Point2D(((Road) e).getX(), ((Road) e).getY());
 					Point2D target_point = center_entrance;
 					
 					if (center_edge.distance(center_entrance) > 5*minClearDistance/100) {
 						math.geom2d.Vector2D v = new math.geom2d.Vector2D(center_edge, center_entrance);
 						target_point = center_edge.plus(v.normalize().times(5*minClearDistance/100));
 					}
 					moveToTargetIfPathClear(changed, path, target_point);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean clearPathToTargetIfNotCleared() {
 		EntityID usefulTarget = target;
 		StandardEntity stdEnt = model.getEntity(target);
 		if (stdEnt instanceof Human && victimTrappedInBuilding(target))
 			usefulTarget = getBuildingFromVictim((Human)stdEnt);
 		
 		List<EntityID> targetAsList = new ArrayList<EntityID>();
 		targetAsList.add(usefulTarget);
 		if (hasObstructingBlockade(targetAsList)) {
 			clearPathToNextStep(targetAsList);
 			return true;
 		} else {
 			if (buildingEntrancesToBeCleared.contains(currentPosition)) {
 				buildingEntrancesCleared.add(currentPosition);
 				buildingEntrancesToBeCleared.remove(currentPosition);
 			}
 			
 			clearedPathTo.add(usefulTarget);
 			log("Cleared path to target " + model.getEntity(target));
 			target = null;
 			
 			return false;
 		}
 	}
 
 	// Convert task to save Human trapped in building to
 	// task to clear building entrance
 	private boolean victimTrappedInBuilding(EntityID id) {
 		if (!(model.getEntity(id) instanceof Human))
 			return false;
 		
 		return getBuildingFromVictim((Human) model.getEntity(id)) != null;
 	}
 	
 	private EntityID getBuildingFromVictim(Human h) {
 		if (!h.isPositionDefined())
 			return null;
 		
 		Area a = (Area)model.getEntity(h.getPosition());
 		
 		if (!(a instanceof Building))
 			return null;
 		
 		return a.getID();
 	}
 	
 	private boolean workedOnTargetBlockade(ChangeSet changed) {
 		// Is the target visible and inside clearing range?
 		if (blockadeInRange(target, changed)) {
 			clearBlockade(target);
 			return true;
 		}
 
 		log("Target " + target + " out of direct reach");
 		Blockade targetBlockade = (Blockade) model.getEntity(target);
 
 		path = getPathToTarget(targetBlockade.getPosition());
 		if (path != null) {
 			moveToTarget(targetBlockade.getX(), targetBlockade.getY());
 			return true;
 		}
 
 		log("No path to target: " + target + ", dropping this task");
 		target = null;
 		return false;
 	}
 
 	private void movingToUnblock() {
 		if (path != null && path.size() > 0 &&
 				model.getEntity(path.get(0)) instanceof Road) {
 			Rectangle2D rect = ((Road) model.getEntity(path.get(0)))
 					.getShape().getBounds2D();
 			Random rdn = new Random();
 			int x = (int) (rect.getMinX() + rdn.nextDouble()
 					* (rect.getMaxX() - rect.getMinX()));
 			int y = (int) (rect.getMinY() + rdn.nextDouble()
 					* (rect.getMaxY() - rect.getMinY()));
 	
 			if (rect.contains(x, y) && currentTime % 3 == 0) {
 				EntityID e = path.get(0);
 				path = new ArrayList<EntityID>();
 				path.add(e);
 				sendMove(currentTime, path, x, y);
 				changeState(State.MOVING_TO_UNBLOCK);
 				log("Found path: " + path + " and sent move to dest: " + x
 						+ "," + y);
 				return;
 			}
 		}
 		path = randomWalk();
 		sendMove(currentTime, path);
 		log("Path calculated to unblock and sent move: " + path);
 	}
 
 	private List<EntityID> getPathToEntranceTarget() {
 		List<EntityID> path;
 
 		path = search.breadthFirstSearch(currentPosition,
 				buildingEntrancesToBeCleared);
 		
 		if (path != null && path.size() > 0)
 			changeState(State.MOVING_TO_ENTRANCE_BUILDING);
 		else
 			path = randomWalk();
 		
 		return path;
 	}
 
 	private boolean moveIfPathClear(ChangeSet changed, List<EntityID> path) {
 		/*obstructingBlockade = getPossibleObstructingBlockade(changed, path);
 		if (obstructingBlockade != null) {
 			clearObstructingBlockade();
 			return false;
 		}*/
 		if (hasObstructingBlockade(path)) {
 			clearPathToNextStep(path);
 			return false;
 		}
 		log("moveIfPathClear: No obstructing blockade to path");
 		log("Path calculated and sent move: " + path);
 		sendMove(currentTime, path);
 		return true;
 	}
 	
 	private boolean moveToTargetIfPathClear(ChangeSet changed, List<EntityID> path,
 			Point2D placeToMove) {
 		/*obstructingBlockade = getPossibleObstructingBlockade(changed, path);
 		if (obstructingBlockade != null) {
 			clearObstructingBlockade();
 			return false;
 		}*/
 		if (hasObstructingBlockade(path)) {
 			clearPathToNextStep(path);
 			return true;
 		}
 		
 		log("moveToTargetIfPathClear: No obstructing blockade to path");
 		moveToTarget((int)placeToMove.x(), (int)placeToMove.y());
 		return true;
 	}
 	
 	/*private EntityID getPossibleObstructingBlockade(ChangeSet changed, List<EntityID> path) {
 		int maxRepairCost = 0, dist, repairCost;
 		EntityID result = null;
 		Set<EntityID> blockades = getVisibleEntitiesOfType(
 				StandardEntityURN.BLOCKADE, changed);
 		
 		for (EntityID next : blockades) {
 			Blockade block = (Blockade) model.getEntity(next);
 				
 			dist = getClosestDistanceFromMe(next);
 			repairCost = block.getRepairCost();
 
 			if (dist <= minClearDistance*3/4.0 &&
 				(path.contains(block.getPosition())
 						|| currentPosition.equals(block.getPosition())) &&
 				repairCost >= maxRepairCost) {
 					result = next;
 					maxRepairCost = repairCost;
 			}
 		}
 		
 		if (state != null && state.equals(lastState)
 				&& result != null && result.equals(lastObstructingBlockade)
 				&& lastRepairCost == maxRepairCost) {
 			log("Last time clearing blockade was ineffective, so need to get closer");
 			return null;
 		}
 			
 		return result;
 	}*/
 
 	private void moveToTarget(int x, int y) {
 		changeState(State.MOVING_TO_TARGET);
 		sendMove(currentTime, path, x, y);
 		log("Found path: " + path + " and sent move to target: " +
 				model.getEntity(target) + " @(" + x + ", " + y + ")");
 	}
 
 	/**
 	 * @param targetPositions
 	 */
 	private List<EntityID> getPathToTarget(EntityID... targetPositions) {
 		return getPathToTarget(Arrays.asList(targetPositions));
 	}
 	
 	/**
 	 * @param targetPositions
 	 */
 	private List<EntityID> getPathToTarget(Collection<EntityID> targetPositions) {
 		return search.breadthFirstSearch(currentPosition, targetPositions);
 	}
 
 	/**
 	 * @param time
 	 */
 	private void clearBlockade(EntityID blockadeID) {
 		changeState(State.CLEARING);
 		sendClearArea(currentTime, blockadeID);
 
 		int repairCost = ((Blockade) model.getEntity(blockadeID)).getRepairCost();
 		lastRepairCost = repairCost;
 		log("Sent clear to remove " + repairRate + "/" + repairCost
 				+ " of the target: " + blockadeID);
 	}
 
 	/**
 	 * If I'm blocked it's probably because there's an obstructing blockade
 	 * 
 	 * @param time
 	 */
 	private void clearObstructingBlockade() {
 		changeState(State.CLEARING_PATH);
 		sendClearArea(currentTime, obstructingBlockade);
 		int repairCost = ((Blockade) model.getEntity(obstructingBlockade))
 				.getRepairCost();
 		lastRepairCost = repairCost;
 		log("Sent clear to remove " + repairRate + "/" + repairCost
 				+ " of the obstructing blockade: " + obstructingBlockade);
 	}
 	
 	private void clearPathToNextStep(List<EntityID> path) {
 		changeState(State.CLEARING_PATH);
 		
 		int targetX = 0, targetY = 0;
 		Area currentArea = (Area) model.getEntity(currentPosition);
 		StandardEntity e = model.getEntity(path.get(0));
 		
 		if (e instanceof Area) {
 			Edge edge = currentArea.getEdgeTo(path.get(0));
 			if (edge == null) {
 				log("ERRO: Edge vazio");
 				return;
 			}
 			
 			targetX = (edge.getEndX()+edge.getStartX())/2;
 			targetY = (edge.getEndY()+edge.getStartY())/2;
 		} else if (e instanceof Human) {
 			targetX = ((Human) e).getX();
 			targetY = ((Human) e).getY();
 		} else {
 			log("ERRO: Path with unknown step");
 		}
 		
 		
 		sendClearArea(currentTime, targetX, targetY);
 		log("Sent clear to remove the obstructing blockade from the path");
 	}
 	
 	private boolean hasObstructingBlockade(List<EntityID> path) {
 		int targetX = 0, targetY = 0;
 		
 		Area currentArea = (Area) model.getEntity(currentPosition);
 		Area nextArea = null;
 		StandardEntity e = model.getEntity(path.get(0));
 		int clearLength = 80*minClearDistance/100;
 		
 		if (e instanceof Area) {
 			Edge edge = currentArea.getEdgeTo(path.get(0));
 			if (edge == null) {
 				log("ERRO: Edge vazio");
 				return false;
 			}
 			nextArea = (Area) model.getEntity(edge.getNeighbour());
 			
 			targetX = (edge.getEndX()+edge.getStartX())/2;
 			targetY = (edge.getEndY()+edge.getStartY())/2;
 		} else if (e instanceof Human) {
 			if (!((Human) e).isXDefined() || !((Human) e).isYDefined())
 				return false;
 			
 			nextArea = (Area) model.getEntity(((Human) e).getPosition());
 			
 			targetX = ((Human) e).getX();
 			targetY = ((Human) e).getY();
 			
 			double dx = Math.abs(targetX - currentX);
 			double dy = Math.abs(targetY - currentY);
 			clearLength = Math.min(clearLength, (int)Math.hypot(dx, dy) + 50);
 		} else {
 			log("ERRO: Path with unknown step");
 		}
 		
 		SimplePolygon2D clearArea = getClearArea(me(), targetX, targetY,
 				clearLength, 20*repairRad/100, true);
 		
 		log("ClearArea-obst-block: length:" + clearLength + ", target=(" + targetX + ", " +targetY + ")");
 		
 		Set<EntityID> possibleObstructingBlockades = new HashSet<EntityID>();
 		if (currentArea.getBlockades() != null)
 			possibleObstructingBlockades.addAll(currentArea.getBlockades());
 		if (nextArea.getBlockades() != null)
 			possibleObstructingBlockades.addAll(nextArea.getBlockades());
 		
 		for (EntityID id : possibleObstructingBlockades) {
 			Blockade b = (Blockade) model.getEntity(id);
 			SimplePolygon2D blockadePolygon = getBlockadePolygon(b);
 			if (blockadePolygon != null && !Polygons2D.intersection(clearArea, blockadePolygon).isEmpty()) {
 				log("Cl-Blockade: " + b.getID() + ", " + b.getRepairCost());
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	private SimplePolygon2D getBlockadePolygon(Blockade b) {
		if (b == null || !b.isApexesDefined())
 			return null;
 		int[] listApexes = b.getApexes();
 		int len = listApexes.length / 2;
 		double[] xPoints = new double[len];
 		double[] yPoints = new double[len];
 		for (int i = 0; i < len; i++) {
 			xPoints[i] = listApexes[2*i];
 			yPoints[i] = listApexes[2*i+1];
 		}
 		return new SimplePolygon2D(xPoints, yPoints);
 	}
 	
 	public SimplePolygon2D getClearArea(Human agent, int targetX, int targetY,
 			int clearLength, int clearRad, boolean limitClearLengthToTarget) {
 		Vector2D agentToTarget = new Vector2D(targetX - agent.getX(), targetY
 				- agent.getY());
 
 		if (!limitClearLengthToTarget || agentToTarget.getLength() > clearLength)
 			agentToTarget = agentToTarget.normalised().scale(clearLength);
 
 		Vector2D backAgent = (new Vector2D(agent.getX(), agent.getY()))
 				.add(agentToTarget.normalised().scale(-110)); // Subtrai 510 - offset (=400)
 		Line2D line = new Line2D(backAgent.getX(), backAgent.getY(),
 				agentToTarget.getX(), agentToTarget.getY());
 
 		Vector2D dir = agentToTarget.normalised().scale(clearRad);
 		Vector2D perpend1 = new Vector2D(dir.getY(), -dir.getX());
 		Vector2D perpend2 = new Vector2D(-dir.getY(), dir.getX());
 
 		rescuecore2.misc.geometry.Point2D points[] = new rescuecore2.misc.geometry.Point2D[] {
 				line.getOrigin().plus(perpend1),
 				line.getEndPoint().plus(perpend1),
 				line.getEndPoint().plus(perpend2),
 				line.getOrigin().plus(perpend2) };
 		double[] xPoints = new double[points.length];
 		double[] yPoints = new double[points.length];
 		for (int i = 0; i < points.length; i++) {
 			xPoints[i] = points[i].getX();
 			yPoints[i] = points[i].getY();
 		}
 		return new SimplePolygon2D(xPoints, yPoints);
 	}
 
 	/**
 	 * @param time
 	 * @param changed
 	 */
 	private void sendMessageAboutPerceptions(ChangeSet changed, Message msg) {
 		// Send a message about all the perceptions
 		msg = composeMessage(changed, msg);
 		if (this.channelComm) {
 			if (!msg.getParameters().isEmpty() && !channelList.isEmpty()) {
 				for (Pair<Integer, Integer> channel : channelList) {
 					sendSpeak(currentTime, channel.first(),
 							msg.getMessage(channel.second().intValue()));
 				}
 			}
 		}
 	}
 
 	private void evaluateTaskDroppingAndSelection(ChangeSet changed) {
 		// Evaluate task dropping
 		if (target != null) {
 			dropTask(currentTime, changed);
 			if (target == null)
 				log("Dropped task: " + taskDropped);
 		}
 
 		// Pick a task to work upon, if you don't have one
 		if (target == null) {
 			target = selectTask();
 			if (target != null)
 				log("Selected task: " + model.getEntity(target));
 		}
 	}
 
 	/**
 	 * Verify if I just visited another building entrance and if I'm done
 	 * looking for building entrances in this sector, I can help in other
 	 * sectors. If there are no other sectors to visit, I'm done.
 	 */
 	private Message verifyBuildingEntrancesToBeCleared(Message msg) {
 		int size_before = buildingEntrancesToBeCleared.size();
 		buildingEntrancesToBeCleared.removeAll(buildingEntrancesCleared);
 		int size_after = buildingEntrancesToBeCleared.size();
 		if (size_after < size_before)
 			log("Other have cleared some entrances. Yet " + size_after
 					+ " to come");
 		
 		if (model.getEntity(target) instanceof Building) {
 			boolean target_util = false;
 			Building areaEntity = (Building) model.getEntity(target);
 			for (EntityID e : areaEntity.getNeighbours())
 				if (model.getEntity(e) instanceof Road &&
 						!buildingEntrancesCleared.contains(e)) {
 					target_util = true;
 					break;
 				}
 			if(!target_util) {
 				taskDropped = target;
 				target = null;
 				log("Dropped task: " + taskDropped);
 			}
 		}
 		
 		// Done with blockades in this position
 		if (countBlockades(currentPosition) == 0) {
 			if (buildingEntrancesToBeCleared.contains(currentPosition)) {
 				msg.addParameter(new BuildingEntranceCleared(currentPosition
 						.getValue()));
 				buildingEntrancesCleared.add(currentPosition);
 				buildingEntrancesToBeCleared.remove(currentPosition);
 				log("Just cleared one more entrance. Yet "
 						+ buildingEntrancesToBeCleared.size()
 						+ " building entrances to come");
 			} else if (model.getEntity(currentPosition) instanceof Road &&
 					!buildingEntrancesCleared.contains(currentPosition)) {
 				Road r = (Road) model.getEntity(currentPosition);
 				for (EntityID neighbourID : r.getNeighbours())
 					if (model.getEntity(neighbourID) instanceof Building) {
 						msg.addParameter(new BuildingEntranceCleared(currentPosition
 								.getValue()));
 						buildingEntrancesCleared.add(currentPosition);
 						log("Just cleared one more entrance, " +
 								"not from my sector but to save victims. " +
 								"Yet " + buildingEntrancesToBeCleared.size() + " to come.");
 						break;
 					}
 			}
 		}
 		
 
 		if (buildingEntrancesToBeCleared.size() == 0) {
 			if (sectorsLeftToSearch.size() > 0) {
 				Collections.shuffle(sectorsLeftToSearch);
 				Sector s = sectorsLeftToSearch.get(0);
 				sectorsLeftToSearch.remove(s);
 				buildingEntrancesToBeCleared = getBuildingEntrancesToBeCleared(s);
 				buildingEntrancesToBeCleared
 						.removeAll(buildingEntrancesCleared);
 			} else {
 				clearEntranceTask = false;
 			}
 		}
 
 		return msg;
 	}
 
 	private int countBlockades(EntityID curPos) {
 		if (curPos != null)
 			if (model.getEntity(curPos) instanceof Area)
 				if (((Area)model.getEntity(curPos)).isBlockadesDefined())
 					return ((Area)model.getEntity(curPos)).getBlockades().size();
 		return -1;
 	}
 
 	private void recalculaVariaveisCiclo() {
 		lastState = this.state;
 		lastTarget = target;
 		//lastObstructingBlockade = obstructingBlockade;
 	}
 
 	protected List<EntityID> randomWalk() {
 		List<EntityID> result = new ArrayList<EntityID>();
 		EntityID current = currentPosition;
 		
 		if (!sector.getLocations().keySet().contains(currentPosition)) {
 			List<EntityID> local = new ArrayList<EntityID>(sector
 					.getLocations().keySet());
 			changeState(State.RETURNING_TO_SECTOR);
 			return search.breadthFirstSearch(currentPosition, local);
 		}
 
 		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
 			result.add(current);
 			List<EntityID> possible = new ArrayList<EntityID>();
 
 			for (EntityID next : sector.getNeighbours(current))
 				if (model.getEntity(next) instanceof Road)
 					possible.add(next);
 
 			Collections.shuffle(possible, new Random(me().getID().getValue()
 					+ currentTime));
 			boolean found = false;
 
 			for (EntityID next : possible) {
 				if (!result.contains(next)) {
 					current = next;
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 				break; // We reached a dead-end.
 		}
 
 		result.remove(0); // Remove actual position from path
 		changeState(State.RANDOM_WALKING);
 		return result;
 	}
 
 	/**
 	 * Determines if the target blockade can be cleared in this turn.
 	 * 
 	 * @param changed
 	 *            The agent's Change Set of the current turn.
 	 * @return True, if the blockade can be cleared in this turn; false
 	 *         otherwise.
 	 */
 	private boolean blockadeInRange(EntityID blockade, ChangeSet changed) {
 		if (!(model.getEntity(blockade) instanceof Blockade))
 			return false;
 		
 		int repairCost = ((Blockade) model.getEntity(blockade)).getRepairCost();
 		boolean stuck = state != null && state.equals(lastState)
 				&& blockade != null && blockade.equals(lastTarget)
 				&& lastRepairCost == repairCost;
 		if (stuck)
 			log("Last time clearing blockade was ineffective, so need to get closer");
 
 		return getVisibleEntitiesOfType(StandardEntityURN.BLOCKADE, changed)
 				.contains(blockade)
 				&& getClosestDistanceFromMe(blockade) < 70*minClearDistance/100
 				&& !stuck;
 	}
 
 	/**
 	 * Locates the closest blockade to the agent.
 	 * 
 	 * @return The closest blockade.
 	 */
 	private EntityID getBestClosestBlockadeToClear(ChangeSet changed) {
 		int maxRepairCost = 0, dist, repairCost;
 		EntityID result = null;
 		Set<EntityID> blockades = getVisibleEntitiesOfType(
 				StandardEntityURN.BLOCKADE, changed);
 
 		for (EntityID next : blockades) {
 			Blockade block = (Blockade) model.getEntity(next);
 				
 			dist = getClosestDistanceFromMe(next);
 			repairCost = block.getRepairCost();
 
 			if (dist <= 70*minClearDistance/100) {
 				if (buildingEntrancesToBeCleared.contains(block.getPosition())) {
 					return next;
 				}
 				if (repairCost >= maxRepairCost) {
 					result = next;
 					maxRepairCost = repairCost;
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get the blockade with the best scoring function.
 	 * 
 	 * @param blockades
 	 *            The set of blockades known.
 	 * @return The chosen blockade.
 	 */
 	@Override
 	protected EntityID selectTask() {
 		EntityID result = null;
 		double Pb = Double.NEGATIVE_INFINITY;
 
 		double importance;
 
 		log("taskTable: " + taskTable);
 		log("knownVictims: " + knownVictims);
 		//log("knownBlockades: " + knownBlockades);
 		for (EntityID next : taskTable.keySet()) {	
 			if (tooManyForATask(next))
 				continue;
 			StandardEntity taskEntity = model.getEntity(next);
 			
 			importance = calculateImportanceTask(taskEntity);
 				
 			if (importance > Pb) {
 				result = next;
 				Pb = importance;
 			}
 		}
 		
 		log("FINAL_SCORE: " + result + " -> " + Pb);
 		
 		if (result != null && Pb > 0) {
 			for (Set<EntityID> agents : taskTable.values())
 				if (agents != null)
 					agents.remove(me().getID());
 			if (!taskTable.containsKey(result))
 				taskTable.put(result, new HashSet<EntityID>());
 			taskTable.get(result).add(me().getID());
 		} else {
 			result = null;
 		}
 
 		return result;
 	}
 
 	private int calculateImportanceTask(StandardEntity taskEntity) {
 		int thisDistance, benefit = 0, importance;
 		boolean samePosition, isImportantPosition = false, isInSector;
 		EntityID pos = null;
 		
 		
 		if (taskEntity instanceof Blockade) {
 			Blockade blockade = (Blockade) taskEntity;
 			// Benefit for a blockade is its size, so the repaircost
 			benefit = 2 + blockade.getRepairCost() / repairRate;
 			pos = blockade.getPosition();
 			if (model.getEntity(pos) instanceof Road) {
 				Road areaEntity = (Road) model.getEntity(pos);
 				for (EntityID e : areaEntity.getNeighbours())
 					if (model.getEntity(e) instanceof Building) {
 						isImportantPosition = true;
 						break;
 					}
 			}
 			if (model.getEntity(blockade.getPosition()) instanceof Road) {
 				Road rd = (Road)model.getEntity(blockade.getPosition());
 				for(EntityID e : rd.getNeighbours())
 					if (model.getEntity(e) instanceof Building)
 						if(pos.equals(currentPosition))
 							benefit += 8;
 			}
 		} else if (taskEntity instanceof Human) {
 			
 			if (clearedPathTo.contains(taskEntity.getID()))
 				return 0;
 			Human victim = (Human) taskEntity;
 			if (victim.isPositionDefined() &&
 					!(model.getEntity(victim.getPosition()) instanceof Area))
 				return 0;
 			if (victim.isPositionDefined() &&
 					model.getEntity(victim.getPosition()) instanceof Building &&
 					clearedPathTo.contains(victim.getPosition()))
 				return 0;
 			
 			boolean hurt = false;
 			if (victim.isDamageDefined() && victim.getDamage() > 0)
 				hurt = true;
 			if (victim.isBuriednessDefined() && victim.getBuriedness() > 0)
 				hurt = true;
 			benefit = victim.getStandardURN()
 					.equals(StandardEntityURN.CIVILIAN) ? 5 : 10;
 			benefit += hurt ? 2 : 0;
 			pos = victim.getPosition();
 			if (model.getEntity(pos) instanceof Building) {
 				
 				Building areaEntity = (Building) model.getEntity(pos);
 				
 				isImportantPosition = true;
 				// Evaluate as 0 those which are already cleared
 				if (buildingEntrancesCleared.containsAll(areaEntity.getNeighbours()))
 					return 0;
 			}
 		} else if (taskEntity instanceof Building) {
 			if (clearedPathTo.contains(taskEntity.getID()))
 				return 0;
 			
 			Building b = (Building) taskEntity;
 			benefit = 12;
 			pos = b.getID();
 			isImportantPosition = true;
 			// Evaluate as 0 those which are already cleared
 			if (buildingEntrancesCleared.containsAll(b.getNeighbours()) ||
 					clearedPathTo.contains(taskEntity.getID()))
 				return 0;
 		}
 		
 		thisDistance = getClosestDistanceFromMe(taskEntity.getID());
 		samePosition = pos.equals(currentPosition);
 		isInSector = sector.getLocations().keySet().contains(pos);
 		
 		importance = benefit;
 		if (thisDistance >= 0 && thisDistance <= 110000)
 			importance += 5 - (thisDistance - 10000) / 20000;
 		importance += isImportantPosition ? 2 : 0;
 		importance += isInSector ? 1 : 0;
 		importance += samePosition ? 2 : 0;
 		
 		return importance;
 	}
 
 	@Override
 	protected void refreshWorldModel(ChangeSet changed,
 			Collection<Command> heard) {
 		Set<EntityID> visibleBlockades = getVisibleEntitiesOfType(
 				StandardEntityURN.BLOCKADE, changed);
 
 		// Remove the blockade the agent has finished clearing
 		if (state.equals(State.CLEARING) && !visibleBlockades.contains(target)) {
 			model.removeEntity(target);
 		} else if (state.equals(State.CLEARING_PATH)
 				&& !visibleBlockades.contains(obstructingBlockade)) {
 			model.removeEntity(obstructingBlockade);
 			obstructingBlockade = null;
 		}
 
 		super.refreshWorldModel(changed, heard);
 	}
 
 	@Override
 	protected void refreshTaskTable(ChangeSet changed) {
 		Set<EntityID> remainingIDs = new HashSet<EntityID>();
 		
 		//Set<StandardEntity> blockades = addBlockades(remainingIDs);
 		remainingIDs.addAll(knownVictims);
 		remainingIDs.addAll(refuges);
 
 		// Discard blockades and humans that do not exist anymore
 		taskTable.keySet().retainAll(remainingIDs);
 
 		/*// Add new blockades to the task table
 		for (StandardEntity blockade : blockades) {
 			Blockade block = (Blockade) blockade;
 			EntityID blockID = block.getID();
 
 			if (!taskTable.containsKey(blockID))
 				taskTable.put(blockID, new HashSet<EntityID>());
 		}*/
 		
 		// Add new victims to the task table
 		for (EntityID victimID : knownVictims)
 			if (!taskTable.containsKey(victimID))
 				taskTable.put(victimID, new HashSet<EntityID>());
 		
 		// Add new refuges to the task table
 		for (EntityID ref : refuges)
 			if (!taskTable.containsKey(ref))
 				taskTable.put(ref, new HashSet<EntityID>());
 	}
 
 	/*private Set<StandardEntity> addBlockades(Set<EntityID> remainingIDs) {
 		Set<StandardEntity> blockades = new HashSet<StandardEntity>();
 		blockades.addAll(model.getEntitiesOfType(
 				StandardEntityURN.BLOCKADE
 		));
 		
 		for (StandardEntity next : blockades)
 			remainingIDs.add(next.getID());
 		
 		return blockades;
 	}*/
 
 	@Override
 	protected boolean amIBlocked(int time) {
 		return Point2D.distance(lastX, lastY, currentX, currentY) < MIN_WALK_LENGTH
 				&& isMovingState() && time > 3;
 	}
 
 	@Override
 	protected void dropTask(int time, ChangeSet changed) {
 		if (!taskTable.containsKey(target) ||
 				buildingEntrancesCleared.contains(currentPosition)) {
 			taskDropped = target;
 			target = null;
 		}
 	}
 
 	private boolean tooManyForATask(EntityID targ) {
 		//Let refuges have more than one agent
 		if (targ != null && model.getEntity(targ) instanceof Refuge)
 			return false;
 		
 		if (taskTable.get(targ).size() >= 1) {
 			int minimo = Integer.MAX_VALUE - 1;
 			for (EntityID t: taskTable.get(targ))
 					minimo = Math.min(t.getValue(), minimo);
 			if (minimo != getID().getValue()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
 		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
 	}
 	
 	private int getClosestDistanceFromMe(EntityID e) {
 		StandardEntity a = model.getEntity(getID());
         StandardEntity b = model.getEntity(e);
         if (a == null || b == null) {
             return -1;
         }
         
         if (!(b instanceof Blockade && ((Blockade)b).isApexesDefined()))
         	return model.getDistance(a, b);
         
         Pair<Integer, Integer> a2 = a.getLocation(model);
         if (a2 == null) {
             return -1;
         }
         
         double dx, dy;
         int dist, minDist = model.getDistance(a, b);
         int[] apexList = ((Blockade)b).getApexes();
         for (int i = 0; i < apexList.length; i+=2) {
         	dx = Math.abs(a2.first() - apexList[i]);
             dy = Math.abs(a2.second() - apexList[i+1]);
         	dist = (int)Math.hypot(dx, dy);
         	if (dist < minDist)
         		minDist = dist;
 		}
         
         return minDist;
 	}
 	
 	private Pair<Integer, Integer> getDirectionToClosestBlockadeFromMe(Blockade b) {
         if (!b.isApexesDefined())
           return new Pair<Integer, Integer>(b.getX(), b.getY());
         
         double dx, dy;
         int dist, minDist = model.getDistance(getID(), b.getID());
         int dir_x = b.getX(), dir_y = b.getY();
         int[] apexList = b.getApexes();
         for (int i = 0; i < apexList.length; i+=2) {
 		    dx = Math.abs(currentX - apexList[i]);
 		    dy = Math.abs(currentY - apexList[i+1]);
 		    dist = (int)Math.hypot(dx, dy);
 		    if (dist < minDist) {
 		    	minDist = dist;
 		    	dir_x = apexList[i];
 		    	dir_y = apexList[i+1];
 		    }
 	    }
         
         return new Pair<Integer, Integer>(dir_x, dir_y);
 	}
 	
 	private void sendClearArea(int time, EntityID target) {
 		StandardEntity e = model.getEntity(target);
 		int x = 0, y = 0;
 		if (e instanceof Area) {
 			x = ((Area)e).getX();
 			y = ((Area)e).getY();
 		} else if (e instanceof Blockade) {
 			Pair<Integer, Integer> dir = getDirectionToClosestBlockadeFromMe((Blockade)e);
 			x = dir.first();
 			y = dir.second();
 		} else {
 			log("ERRO: sendClearArea to unknown element");
 			return;
 		}
 
 		sendClearArea(time, x, y);
 	}
 	
 	private void sendClearArea(int time, int targetX, int targetY) {
 		Point2D origin = new Point2D(currentX, currentY);
 		Point2D targetPoint = new Point2D(targetX, targetY);
 		math.geom2d.Vector2D direction = new math.geom2d.Vector2D(origin, targetPoint);
 		Point2D newTargetPoint = origin.plus(direction.normalize().times(minClearDistance));
 
 		sendClear(time, (int)newTargetPoint.x(), (int)newTargetPoint.y());
 	}
 
 	private boolean isMovingState() {
 		List<State> ss = new ArrayList<State>();
 		ss.add(State.RETURNING_TO_SECTOR);
 		ss.add(State.MOVING_TO_TARGET);
 		ss.add(State.MOVING_TO_ENTRANCE_BUILDING);
 		ss.add(State.RANDOM_WALKING);
 		ss.add(State.MOVING_TO_UNBLOCK);
 
 		return ss.contains(state);
 	}
 
 	private void changeState(State state) {
 		this.state = state;
 		log("Changed state to: " + this.state);
 	}
 }
