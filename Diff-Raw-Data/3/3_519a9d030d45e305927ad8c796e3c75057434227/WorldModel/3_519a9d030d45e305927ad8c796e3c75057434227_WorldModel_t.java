 package arch;
 
 import jason.asSyntax.Literal;
 import jason.asSyntax.NumberTerm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import model.Entity;
 import model.graph.Graph;
 import model.graph.Vertex;
 import env.Percept;
 
 /**
  * Class used to model the scenario (for an agent view).
  * 
  * @author mafranko
  */
 public class WorldModel {
 
 	private Graph graph;
 
 	private HashMap<String, Entity> opponents;
 	private HashMap<String, Entity> coworkers;
 
 	public static String myTeam = "A";
 	public static String usernamePrefix = "a";
 
 	public final static int numOfOpponents = 20;
 
 	private Entity agent;
 
 	public WorldModel(String agentName) {
 		graph = new Graph();
 		opponents = new HashMap<String, Entity>();
 		coworkers = new HashMap<String, Entity>();
 		agent = new Entity(agentName);
 		agent.setTeam(myTeam);
 		agent.setStatus(Percept.STATUS_NORMAL);
 	}
 
 	public void restart() {
 		graph = new Graph();
 		opponents = new HashMap<String, Entity>();
 		coworkers = new HashMap<String, Entity>();
 	}
 
 	public List<Literal> update(List<Literal> percepts) {
 		List<Literal> newPercepts = new ArrayList<Literal>();
 		for (Literal percept : percepts) {
 			String functor = percept.getFunctor();
 			if (functor.equals(Percept.visibleVertex)) {
 				if (graph.getVertices().size() < graph.getMaxNumOfVertices()) {
 					String vertexName = percept.getTerm(0).toString();
 					int id = Integer.parseInt(vertexName.replace("vertex", ""));
 					String team = percept.getTerm(1).toString();
 					if (!graph.containsVertex(id, team)) {
 						graph.addVertex(id, team);
 						newPercepts.add(percept);
 					}
 				}
 			} else if (functor.equals(Percept.probedVertex)) {
 				String vertexName = percept.getTerm(0).toString();
 				int id = Integer.parseInt(vertexName.replace("vertex", ""));
 				int vValue = (int) ((NumberTerm) percept.getTerm(1)).solve();
 				if (graph.getVertexValue(id) != vValue || vValue == 1) {
 					graph.addVertexValue(id, vValue);
 					newPercepts.add(percept);
 				}
 			} else if (functor.equals(Percept.visibleEdge)) {
 				if (graph.getEdges().size() < graph.getMaxNumOfEdges()) {
 					String vertex1 = percept.getTerm(0).toString();
 					String vertex2 = percept.getTerm(1).toString();
 					int v1 = Integer.parseInt(vertex1.replace("vertex", ""));
 					int v2 = Integer.parseInt(vertex2.replace("vertex", ""));
 					if (!graph.containsEdge(v1, v2)) {
 						graph.addEdge(v1, v2);
 						newPercepts.add(percept);
 					}
 				}
 			} else if (functor.equals(Percept.surveyedEdge)) {
 				String vertex1 = percept.getTerm(0).toString();
 				String vertex2 = percept.getTerm(1).toString();
 				int v1 = Integer.parseInt(vertex1.replace("vertex", ""));
 				int v2 = Integer.parseInt(vertex2.replace("vertex", ""));
 				int eValue = (int) ((NumberTerm) percept.getTerm(2)).solve();
 				if (graph.getEdgeValue(v1, v2) != eValue) {
 					graph.addEdgeValue(v1, v2, eValue);
 					newPercepts.add(percept);
 				}
 			} else if (functor.equals(Percept.vertices)) {
 				int vertices = (int) ((NumberTerm) percept.getTerm(0)).solve();
 				graph.setMaxNumOfVertices(vertices);
 			} else if (functor.equals(Percept.edges)) {
 				int edges = (int) ((NumberTerm) percept.getTerm(0)).solve();
 				graph.setMaxNumOfEdges(edges);
 			} else if (functor.equals(Percept.visibleEntity)) {
 				String name = percept.getTerm(0).toString();
				name = name.replaceAll("\"", "");
 				String vertex = percept.getTerm(1).toString();
 				int v = Integer.parseInt(vertex.replace("vertex", ""));
 				String team = percept.getTerm(2).toString();
 				team = team.replaceAll("\"", "");
 				String status = percept.getTerm(3).toString();
 				if (!team.equals(myTeam) && !containsOpponent(name, v, team, status)) {
 					addOpponent(name, v, team, status);
 					newPercepts.add(percept);
 				}
 			} else if (functor.equals(Percept.coworkerPosition)) {
 				String aName = percept.getTerm(0).toString();
 				String position = percept.getTerm(1).toString();
 				position = position.replace("vertex", "");
 				int pos = Integer.parseInt(position);
 				if (!containsCoworker(aName, pos)) {
 					addCoworker(aName, pos);
 				}
 			} else if (functor.equals(Percept.coworkerStatus)) {
 				String aName = percept.getTerm(0).toString();
 				String status = percept.getTerm(1).toString();
 				if (coworkers.containsKey(aName)) {
 					Entity e = coworkers.get(aName);
 					e.setStatus(status);
 				}
 			} else if (functor.equals(Percept.coworker)) {
 				String aName = percept.getTerm(0).toString();
 				String role = percept.getTerm(1).toString();
 				String mission = percept.getTerm(2).toString();
 				Entity e = null;
 				if (coworkers.containsKey(aName)) {
 					e = coworkers.get(aName);
 				} else {
 					e = new Entity(aName);
 					coworkers.put(aName, e);
 				}
 				e.setRole(role);
 				e.setMission(mission);
 			} else if (functor.equals(Percept.position)) {
 				String myPosition = percept.getTerm(0).toString();
 				myPosition = myPosition.replace("vertex", "");
 				int myPos = Integer.parseInt(myPosition);
 				Vertex myVertex = agent.getVertex();
 				if (null == myVertex || myVertex.getId() != myPos) {
 					Vertex vtx = graph.getVertices().get(myPos);
 					if (null == vtx) {
 						vtx = new Vertex(myPos, myTeam);
 						graph.addVertex(vtx);
 					}
 					agent.setVertex(vtx);
 					vtx.addVisited();
 					newPercepts.add(percept);
 				}
 			} else if (functor.equals(Percept.saboteur)) {
 				String saboteurName = percept.getTerm(0).toString();
 				saboteurName = saboteurName.replaceAll("\"", "");
 				String saboteurPosition = percept.getTerm(1).toString();
 				saboteurPosition = saboteurPosition.replace("vertex", "");
 				int saboteurPos = Integer.parseInt(saboteurPosition);
 				Entity saboteur = opponents.get(saboteurName);
 				if (null == saboteur) {
 					saboteur = new Entity(saboteurName);
 					opponents.put(saboteurName, saboteur);
 				}
 				saboteur.setRole("saboteur");
 				Vertex saboteurVtx = graph.getVertices().get(saboteurPosition);
 				if (null == saboteurVtx) {
 					saboteurVtx = new Vertex(saboteurPos);
 					graph.addVertex(saboteurVtx);
 				}
 				saboteur.setVertex(saboteurVtx);
 			} else if (functor.equals(Percept.inspectedEntity)) {
 				String entityName = percept.getTerm(0).toString();
				entityName = entityName.replaceAll("\"", "");
 				String entityTeam = percept.getTerm(1).toString();
 				entityTeam = entityTeam.replaceAll("\"", "");
 				if (entityTeam.equals(myTeam)) {
 					break;
 				}
 				String entityRole = percept.getTerm(2).toString();
 				entityRole = entityRole.replaceAll("\"", "");
 				String entityVertex = percept.getTerm(3).toString();
 				entityVertex = entityVertex.replace("vertex", "");
 				int entityPos = Integer.parseInt(entityVertex);
 				int entityEnergy =  (int) ((NumberTerm) percept.getTerm(4)).solve();
 				int maxEnergy =  (int) ((NumberTerm) percept.getTerm(5)).solve();
 				int entityHealth =  (int) ((NumberTerm) percept.getTerm(6)).solve();
 				int maxHealth =  (int) ((NumberTerm) percept.getTerm(7)).solve();
 				int entityStrenght =  (int) ((NumberTerm) percept.getTerm(8)).solve();
 				int entityVisibility =  (int) ((NumberTerm) percept.getTerm(9)).solve();
 				Entity entity =  opponents.get(entityName);
 				if (null == entity) {
 					entity = new Entity(entityName);
 					opponents.put(entityName, entity);
 				}
 				entity.setInspected(true);
 				entity.setRole(entityRole);
 				entity.setTeam(entityTeam);
 				if (null == entity.getVertex() || entity.getVertex().getId() != entityPos) {
 					Vertex entityVtx = graph.getVertices().get(entityPos);
 					if (null == entityVtx) {
 						entityVtx = new Vertex(entityPos);
 						graph.addVertex(entityVtx);
 					}
 					entity.setVertex(entityVtx);
 					newPercepts.add(percept);
 				}
 				entity.update(entityEnergy, maxEnergy, entityHealth, maxHealth,
 						entityStrenght, entityVisibility);
 				newPercepts.add(percept);
 			} else {
 				newPercepts.add(percept);
 			}
 		}
 		return newPercepts;
 	}
 
 	private void addOpponent(String name, int vertex, String team, String status) {
 		Vertex v = graph.getVertices().get(vertex);
 		if (null == v) {
 			v = new Vertex(vertex);
 			graph.addVertex(vertex, team);
 		}
 		Entity e = opponents.get(name);
 		if (null == e) {
 			e = new Entity(name, team, v, status);
 			opponents.put(name, e);
 		} else {
 			e.setVertex(v);
 			if (!team.equals(Percept.TEAM_NONE) && !team.equals(Percept.TEAM_UNKNOWN)) {
 				e.setTeam(team);
 			}
 			e.setStatus(status);
 		}
 	}
 
 	private boolean containsOpponent(String name, int vertex, String team, String status) {
 		if (opponents.containsKey(name)) {
 			Entity opponent = opponents.get(name);
 			if (opponent.getVertex().getId() == vertex && opponent.getTeam().equals(team)
 					&& opponent.getStatus().equals(status)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void addCoworker(String name, int vertex) {
 		Vertex v = graph.getVertices().get(vertex);
 		if (null == v) {
 			v = new Vertex(vertex);
 			graph.addVertex(vertex, myTeam);
 		}
 		v.addVisited();
 		Entity coworker = coworkers.get(name);
 		if (null == coworker) {
 			coworker = new Entity(name);
 		}
 		coworker.setVertex(v);
 		coworkers.put(name, coworker);
 	}
 
 	private boolean containsCoworker(String name, int vertex) {
 		if (coworkers.containsKey(name)) {
 			Entity coworker = coworkers.get(name);
 			if (coworker.getVertex().getId() == vertex) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public List<Vertex> getBestVertices() {
 		List<Vertex> bestVertices = new ArrayList<Vertex>();
 		int bestValue = -1;
 		Set<Vertex> vertices = (Set<Vertex>) graph.getVertices().values();
 		for (Vertex v : vertices) {
 			if (v.getValue() > bestValue) {
 				bestValue = v.getValue();
 				bestVertices.clear();
 				bestVertices.add(v);
 			} else if (v.getValue() == bestValue) {
 				bestVertices.add(v);
 			}
 		}
 		return bestVertices;
 	}
 
 	public List<Vertex> getBestZone() {
 		graph.removeVerticesColor();
 		// step 1: coloring vertices that have agents standing on them
 		coloringVertices();
 		// step 2: coloring empty neighbors
 		coloringNeighbors();
 		// step 3: frontier
 		coloringIsolatedVertices();
 		// step 4: get zones
 		List<List<Vertex>> zones = graph.getZones();
 		// step 5: get best zone
 		return getBestZone(zones);
 	}
 
 	private void coloringVertices() {
 		// color my vertices
 		Set<Entity> teamAgents = new HashSet<Entity>(coworkers.values());
 		teamAgents.add(agent);
 		for (Entity e : teamAgents) {
 			Vertex v = e.getVertex();
 			if (v.getTeam().equals(myTeam) && !e.getStatus().equals(Percept.STATUS_DISABLED)) {
 				v.setColor(Vertex.BLUE);
 			} else if (!v.getTeam().equals(Percept.TEAM_NONE)
 					&& !v.getTeam().equals(Percept.TEAM_UNKNOWN)) {
 				v.setColor(Vertex.RED);
 			}
 		}
 		// color opponents vertices
 		for (Entity e : opponents.values()) {
 			Vertex v = e.getVertex();
 			if (!e.getStatus().equals(Percept.STATUS_DISABLED)
 					&& !v.getTeam().equals(myTeam) && !v.getTeam().equals(Percept.TEAM_NONE)
 					&& !v.getTeam().equals(Percept.TEAM_UNKNOWN)) {
 				v.setColor(Vertex.RED);
 			}
 		}
 	}
 
 	private void coloringNeighbors() {
 		List<Vertex> blueVertices = graph.getBlueVertices();
 		List<Vertex> redVertices = graph.getRedVertices();
 
 		Set<Entity> teamAgents = new HashSet<Entity>(coworkers.values());
 		teamAgents.add(agent);
 
 		Set<Vertex> neighbors = null;
 		for (Entity e : teamAgents) {
 			Vertex coworkerV = e.getVertex();
 			if (coworkerV.getColor() == Vertex.BLUE) {
 				neighbors = coworkerV.getNeighbors();
 				for (Vertex v : neighbors) {
 					if (v.getColor() == Vertex.WHITE) {
 						int numOfBlueNeighbors = 0;
 						int numOfRedNeighbors = 0;
 						for (Vertex vv : v.getNeighbors()) {
 							if (blueVertices.contains(vv)) {
 								numOfBlueNeighbors++;
 							} else if (redVertices.contains(vv)) {
 								numOfRedNeighbors++;
 							}
 						}
 						if (numOfBlueNeighbors > numOfRedNeighbors && numOfBlueNeighbors > 1) {
 							v.setColor(Vertex.BLUE);
 						} else if (numOfBlueNeighbors < numOfRedNeighbors && numOfRedNeighbors > 1) {
 							v.setColor(Vertex.RED);
 						}	
 					}
 				}
 			}
 		}
 	}
 
 	private void coloringIsolatedVertices() {
 		List<Vertex> notColoredVertices = graph.getNotColoredVertices();
 		for (Vertex v : notColoredVertices) {
 			boolean existsFrontier = true;
 			for (Entity e : opponents.values()) {
 				if (!e.getStatus().equals(Percept.STATUS_DISABLED)
 						&& !graph.existsFrontier(v, e.getVertex())) {
 					existsFrontier = false;
 				}
 			}
 			if (existsFrontier) {
 				v.setColor(Vertex.BLUE);
 			}
 		}
 	}
 
 	private List<Vertex> getBestZone(List<List<Vertex>> zones) {
 		int bestZoneValue = -1;
 		List<Vertex> bestZone = null;
 		for (List<Vertex> zone : zones) {
 			int value = 0;
 			for (Vertex v : zone) {
 				value += v.getValue();
 			}
 			if (value > bestZoneValue) {
 				bestZone = zone;
 			}
 		}
 		return bestZone;
 	}
 
 	public List<Vertex> getZoneNeighbors(List<Vertex> zone) {
 		List<Vertex> neighbors = new ArrayList<Vertex>();
 		for (Vertex v : zone) {
 			for (Vertex neighbor : v.getNeighbors()) {
 				if (neighbor.getColor() != Vertex.BLUE && !neighbors.contains(neighbor)) {
 					neighbors.add(neighbor);
 				}
 			}
 		}
 		return neighbors;
 		
 	}
 
 	public List<Vertex> getBestZoneNeighbors(List<Vertex> neighbors) {
 		List<Vertex> bestNeighbors = new ArrayList<Vertex>();
 		for (Vertex v : neighbors) {
 			Set<Vertex> vNeighbors = v.getNeighbors();
 			int count = 0;
 			for (Vertex vNeighbor : vNeighbors) {
 				if (vNeighbor.getColor() == Vertex.BLUE) {
 					count++;
 				}
 			}
 			if (count >= 2) {
 				bestNeighbors.add(v);
 			}
 		}
 		return bestNeighbors;
 	}
 
 	public Vertex closerVertex(Vertex position, List<Vertex> vertices) {
 		Vertex closerVertex = null;
 		int closerDistance = Integer.MAX_VALUE;
 		for (Vertex v : vertices) {
 			int dist = graph.getDistance(position, v);
 			if (dist < closerDistance) {
 				closerVertex = v;
 			}
 		}
 		return closerVertex;
 	}
 
 	public boolean isFrontier(Vertex v) {
 		for (Vertex neighbor : v.getNeighbors()) {
 			if (neighbor.getColor() != Vertex.BLUE) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public List<Vertex> getBestOpponentZone() {
 		graph.removeVerticesColor();
 		// step 1: coloring vertices that have agents standing on them
 		coloringVertices();
 		// step 2: coloring empty neighbors
 		coloringNeighbors();
 		// step 3: frontier
 		coloringOpponentIsolatedVertices();
 		// step 4: get zones
 		List<List<Vertex>> zones = graph.getOpponentZones();
 		// step 5: get best zone
 		return getBestZone(zones);
 	}
 
 	private void coloringOpponentIsolatedVertices() {
 		List<Vertex> notColoredVertices = graph.getNotColoredVertices();
 		for (Vertex v : notColoredVertices) {
 			boolean existsFrontier = true;
 			for (Entity e : coworkers.values()) {
 				if (!e.getStatus().equals(Percept.STATUS_DISABLED)
 						&& !graph.existsOpponentFrontier(v, e.getVertex())) {
 					existsFrontier = false;
 				}
 			}
 			if (existsFrontier) {
 				v.setColor(Vertex.BLUE);
 			}
 		}
 	}
 
 	public boolean hasActiveCoworkerOnVertex(int v) {
 		for (Entity coworker : coworkers.values()) {
 			if (coworker.getVertex().getId() == v
 					&& !coworker.getStatus().toLowerCase().equals(Percept.STATUS_DISABLED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasActiveCoworkerOnVertex(Vertex v) {
 		for (Entity coworker : coworkers.values()) {
 			if (coworker.getVertex().equals(v)
 					&& !coworker.getStatus().toLowerCase().equals(Percept.STATUS_DISABLED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasOpponentOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasOpponentWithoutRoleOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& opponent.getRole().equals(Percept.ROLE_UNKNOWN)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasActiveOpponentOnVertex(int v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().getId() == v
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasActiveOpponentOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& !opponent.getStatus().toLowerCase().equals(Percept.STATUS_DISABLED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasActiveSaboteurOpponentOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& !opponent.getStatus().toLowerCase().equals(Percept.STATUS_DISABLED)
 					&& opponent.getRole().equals(Percept.ROLE_SABOTEUR)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasUniqueActiveOpponentOnVertex(int v) {
 		int count = 0;
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().getId() == v
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				count++;
 			}
 		}
 		if (count == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean hasSaboteurOnVertex(int v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().getId() == v
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)
 					&& opponent.getRole().equals("saboteur")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasSaboteurOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)
 					&& opponent.getRole().equals("saboteur")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getOpponentName(int v) {
 		String opponentName = null;
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().getId() == v) {
 				opponentName = opponent.getName();
 				return opponentName;
 			}
 		}
 		return opponentName;
 	}
 
 	public List<Entity> getActiveOpponentsOnVertex(int v) {
 		List<Entity> opponentsOnVertex = new ArrayList<Entity>();
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().getId() == v
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				opponentsOnVertex.add(opponent);
 			}
 		}
 		return opponentsOnVertex;
 	}
 
 	public String getOpponentName(Vertex v) {
 		String opponentName = null;
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				opponentName = opponent.getName();
 				return opponentName;
 			}
 		}
 		return opponentName;
 	}
 
 	public Vertex getCoworkerPosition(String agentName) {
 		Entity agent = coworkers.get(agentName);
 		if (null == agent) {
 			return null;
 		}
 		return agent.getVertex();
 	}
 
 	public List<Entity> getCoworkersByRole(String role) {
 		List<Entity> agents = new ArrayList<Entity>();
 		for (Entity coworker : coworkers.values()) {
 			if (coworker.getRole().toLowerCase().equals(role)) {
 				agents.add(coworker);
 			}
 		}
 		return agents;
 	}
 
 	public List<Entity> getDisabledCoworkersAt(Vertex v) {
 		List<Entity> disabledCoworkers = new ArrayList<Entity>();
 		
 		for (Entity coworker : coworkers.values()) {
 			if (coworker.getVertex().equals(v) && coworker.getStatus().equals(Percept.STATUS_DISABLED)) {
 				disabledCoworkers.add(coworker);
 			}
 		}
 		
 		return disabledCoworkers;
 	}
 
 	public List<Entity> getCoworkersOnSameVertex(Entity e) {
 		List<Entity> agents = new ArrayList<Entity>();
 		List<Entity> coworkersList = new ArrayList<Entity>(coworkers.values());
 		coworkersList.add(agent);
 		Vertex v = e.getVertex();
 		for (Entity ag : coworkersList) {
 			if (ag.getVertex().equals(v) && !ag.equals(e)
 					&& !ag.getStatus().equals(Percept.STATUS_DISABLED)) {
 				agents.add(ag);
 			}
 		}
 		return agents;
 	}
 
 	public List<Entity> getActiveOpponentByRole(String role) {
 		List<Entity> agents = new ArrayList<Entity>();
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getRole().toLowerCase().equals(role)
 					&& !opponent.getStatus().toLowerCase().equals(Percept.STATUS_DISABLED)) {
 				agents.add(opponent);
 			}
 		}
 		return agents;
 	}
 
 	public List<Entity> getUninspectedOpponents() {
 		List<Entity> agents = new ArrayList<Entity>();
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getRole().equals(Percept.ROLE_UNKNOWN)) {
 				agents.add(opponent);
 			}
 		}
 		return agents;
 	}
 
 	public List<Entity> getCoworkersToOccupyZone() {
 		List<Entity> agents = new ArrayList<Entity>();
 		for (Entity e : coworkers.values()) {
 			if (e.getMission().equals("mOccupyZone") || e.getMission().equals("mRepair")) {
 				agents.add(e);
 			}
 		}
 		return agents;
 	}
 
 	public boolean hasUninspectedOpponent() {
 		if (opponents.size() < numOfOpponents) {
 			return true;
 		}
 		List<Entity> agents = new ArrayList<Entity>();
 		for (Entity e : opponents.values()) {
 			if (e.getRole().equals(Percept.ROLE_UNKNOWN)) {
 				agents.add(e);
 			}
 		}
 		if (agents.isEmpty()) {
 			return false;
 		}
 		if (agents.size() > 1) {
 			return true;
 		} else if (agents.size() == 1) {
 			// we can infer the agents role
 			inferOpponentsRole(agents.get(0));
 			return false;
 		}
 		return false;
 	}
 
 	private void inferOpponentsRole(Entity agent) {
 		int numOfExplorers = 0;
 		int numOfInspectors = 0;
 		int numOfRepairers = 0;
 		int numOfSentinels = 0;
 		int numOfSaboteurs = 0;
 		for (Entity opponent : opponents.values()) {
 			String role = opponent.getRole();
 			if (role.equals(Percept.ROLE_EXPLORER)) {
 				numOfExplorers++;
 			} else if (role.equals(Percept.ROLE_INSPECTOR)) {
 				numOfInspectors++;
 			} else if (role.equals(Percept.ROLE_REPAIRER)) {
 				numOfRepairers++;
 			} else if (role.equals(Percept.ROLE_SABOTEUR)) {
 				numOfSaboteurs++;
 			} else if (role.equals(Percept.ROLE_SENTINEL)) {
 				numOfSentinels++;
 			}
 		}
 		int numOfAgentsByRole = numOfOpponents/4;
 		if (numOfExplorers < numOfAgentsByRole) {
 			agent.setRole(Percept.ROLE_EXPLORER);
 		} else if (numOfInspectors < numOfAgentsByRole) {
 			agent.setRole(Percept.ROLE_INSPECTOR);
 		} else if (numOfRepairers < numOfAgentsByRole) {
 			agent.setRole(Percept.ROLE_REPAIRER);
 		} else if (numOfSaboteurs < numOfAgentsByRole) {
 			agent.setRole(Percept.ROLE_SABOTEUR);
 		} else if (numOfSentinels < numOfAgentsByRole) {
 			agent.setRole(Percept.ROLE_SENTINEL);
 		} else {
 			// error
 			System.out.println("[ERROR] Could not infer the agents role.");
 		}
 	}
 
 	public Entity getCloserAgent(List<Entity> agents) {
 		int minDist = Integer.MAX_VALUE;
 		Entity ag = null;
 		Vertex myVertex = agent.getVertex();
 		for (Entity e : agents) {
 			Vertex v = e.getVertex();
 			int dist = graph.getDistance(myVertex, v);
 			if (dist < minDist) {
 				ag = e;
 			}
 		}
 		return ag;
 	}
 
 	public Entity getCloserOpponent() {
 		int minDist = Integer.MAX_VALUE;
 		Entity ag = null;
 		Vertex myVertex = agent.getVertex();
 		for (Entity e : opponents.values()) {
 			Vertex v = e.getVertex();
 			int dist = graph.getDistance(myVertex, v);
 			if (dist < minDist) {
 				ag = e;
 			}
 		}
 		return ag;
 	}
 
 	public Entity getCloserActiveOpponentNotSaboteur() {
 		int minDist = Integer.MAX_VALUE;
 		Entity ag = null;
 		Vertex myVertex = agent.getVertex();
 		for (Entity e : opponents.values()) {
 			if (!e.getRole().equals(Percept.ROLE_SABOTEUR)
 					&& !e.getStatus().equals(Percept.STATUS_DISABLED)) {
 				Vertex v = e.getVertex();
 				int dist = graph.getDistance(myVertex, v);
 				if (dist < minDist) {
 					ag = e;
 				}
 			}
 		}
 		return ag;
 	}
 
 	public Entity getCloserActiveOpponent() {
 		int minDist = Integer.MAX_VALUE;
 		Entity ag = null;
 		Vertex myVertex = agent.getVertex();
 		for (Entity e : opponents.values()) {
 			if (!e.getStatus().equals(Percept.STATUS_DISABLED)) {
 				Vertex v = e.getVertex();
 				int dist = graph.getDistance(myVertex, v);
 				if (dist < minDist) {
 					ag = e;
 				}
 			}
 		}
 		return ag;
 	}
 
 	public boolean containsActiveOpponentSaboteurOnVertex(Vertex v) {
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v)
 					&& opponent.getRole().equals(Percept.ROLE_SABOTEUR)
 					&& !opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int numOfActiveOpponentsAt(Vertex v) {
 		int num = 0;
 		for (Entity opponent : opponents.values()) {
 			if (opponent.getVertex().equals(v) && 
 					!opponent.getStatus().equals(Percept.STATUS_DISABLED)) {
 				num++;
 			}
 		}
 		return num;
 	}
 	
 	/* Getters and Setters */
 
 	public Graph getGraph() {
 		return graph;
 	}
 
 	public Vertex getMyVertex() {
 		return agent.getVertex();
 	}
 
 	public void setAgentStatus(String status) {
 		agent.setStatus(status);
 	}
 
 	public HashMap<String, Entity> getCoworkers() {
 		return coworkers;
 	}
 
 	public HashMap<String, Entity> getOpponents() {
 		return opponents;
 	}
 
 	public Entity getAgentEntity() {
 		return agent;
 	}
 }
