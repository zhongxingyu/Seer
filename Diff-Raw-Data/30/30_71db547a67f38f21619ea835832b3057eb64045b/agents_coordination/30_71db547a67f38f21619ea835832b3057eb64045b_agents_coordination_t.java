 package jia;
 
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Term;
 
 import java.util.Collections;
 import java.util.List;
import java.util.logging.Logger;
 
 import model.Entity;
 import model.graph.Graph;
 import model.graph.Vertex;
 import model.graph.VertexComparator;
 import arch.MarcianArch;
 import arch.WorldModel;
 
 /**
  * Verify which is the team best zone in a determined time and returns a list of
  * agents and positions to increase that zone.
  * </p>
  * Use: jia.agents_coordination(-A,-P); </br>
  * Where: A is the list of agents and P the list of target positions.
  * 
  * @author mafranko
  */
 public class agents_coordination extends DefaultInternalAction {
 
 	private static final long serialVersionUID = -6858228332440013608L;
 
	static Logger logger = Logger.getLogger(agents_coordination.class.getName());

 	@Override
 	public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
 		synchronized (this) {
 			ListTerm positions = new ListTermImpl();
 			ListTerm agents = new ListTermImpl();
 
 			WorldModel model = ((MarcianArch) ts.getUserAgArch()).getModel();
 			Graph graph = model.getGraph();
 			List<Vertex> bestZone = model.getBestZone();	// zone with the greatest value
 
//			logger.info("BEST ZONE: " + bestZone);
 			if (null == bestZone || bestZone.isEmpty()) {
 				return un.unifies(terms[0], agents) & un.unifies(terms[1], positions);
 			}
 
 			List<Vertex> zoneNeighbors = model.getZoneNeighbors(bestZone);
 
 			// order neighbors by vertex value
 			VertexComparator comparator = new VertexComparator();
 			Collections.sort(zoneNeighbors, comparator);
 
 			List<Vertex> bestNeighbors = model.getBestZoneNeighbors(zoneNeighbors);
 			zoneNeighbors.removeAll(bestNeighbors);
 
 			if (zoneNeighbors.isEmpty() && bestNeighbors.isEmpty()) {
 				return un.unifies(terms[0], agents) & un.unifies(terms[1], positions);
 			}
 
 			List<Entity> coworkers = model.getCoworkersToOccupyZone();	// TODO order by agent type
 
 			for (Entity coworker : coworkers) {
 				Vertex target = null;
 				Vertex agentPosition = coworker.getVertex();
 				if (bestZone.contains(agentPosition)) {	// the agent is part of the best zone
 					if (model.isFrontier(agentPosition)) {
 						// TODO verify if the agent can move to a neighbor without break the zone
 					} else {
 						if (!bestNeighbors.isEmpty()) {
 							target = model.closerVertex(agentPosition, bestNeighbors);
 							if (null != target) {
 								bestNeighbors.remove(target);
 							}
 						} else if (!zoneNeighbors.isEmpty()) {
 							target = model.closerVertex(agentPosition, bestNeighbors);
 							if (null != target) {
 								zoneNeighbors.remove(target);
 							}
 						}
 					}
 				} else {
 					if (!bestNeighbors.isEmpty()) {
 						target = model.closerVertex(agentPosition, bestNeighbors);
 						if (null != target) {
 							bestNeighbors.remove(target);
 						}
 					} else if (!zoneNeighbors.isEmpty()) {
 						target = model.closerVertex(agentPosition, bestNeighbors);
 						if (null != target) {
 							zoneNeighbors.remove(target);
 						}
 					}
 				}
 
 				if (null != target) {
 					agents.add(ASSyntax.createString(coworker.getName()));
 					positions.add(ASSyntax.createString("vertex" + target.getId()));
 				}
 			}
 
 			
 			// my move
 			Vertex myVertex = model.getMyVertex();
 			Vertex myTarget = null;
 			if (bestZone.contains(myVertex)) {
 				if (model.isFrontier(myVertex)) {
 					// TODO verify if the agent can move to a neighbor without break the zone
 				} else {
 					List<Vertex> notProbedNeighbors = graph.returnNotProbedNeighbors(myVertex);
 					if (!notProbedNeighbors.isEmpty()) {
 						for (Vertex notProbedNeighbor : notProbedNeighbors) {
 							if (bestZone.contains(notProbedNeighbor)) {
 								myTarget = notProbedNeighbor;
 							}
 						}
 					}
 
 					if (myTarget != null) {
 						notProbedNeighbors = graph.returnNotProbedNeighbors(bestZone);
 						if (!notProbedNeighbors.isEmpty()) {
 							myTarget = notProbedNeighbors.get(0);	// TODO get the more closer vertex
 						}
 						// TODO else?
 					}
 				}
 			} else {
 				if (!bestNeighbors.isEmpty()) {
 					myTarget = model.closerVertex(myVertex, bestNeighbors);
 					if (null != myTarget) {
 						bestNeighbors.remove(myTarget);
 					}
 				} else if (!zoneNeighbors.isEmpty()) {
 					myTarget = model.closerVertex(myTarget, bestNeighbors);
 					if (null != myTarget) {
 						zoneNeighbors.remove(myTarget);
 					}
 				} else {
 					List<Vertex> notProbedNeighbors = graph.returnNotProbedNeighbors(bestZone);
 					if (!notProbedNeighbors.isEmpty()) {
 						myTarget = notProbedNeighbors.get(0);	// TODO get the more closer vertex
 					}
 					// TODO else?
 				}
 			}
 
 			if (null != myTarget) {
 				agents.add(ASSyntax.createString(ts.getUserAgArch().getAgName()));
 				positions.add(ASSyntax.createString("vertex" + myTarget.getId()));
 			}
 			return un.unifies(terms[0], agents) & un.unifies(terms[1], positions);
 		}
 	}
 }
