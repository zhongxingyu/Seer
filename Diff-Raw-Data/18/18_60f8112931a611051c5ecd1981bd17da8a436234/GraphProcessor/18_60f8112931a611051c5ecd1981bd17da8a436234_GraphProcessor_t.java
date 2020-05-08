 package ch.epfl.ad.db.querytackling;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import ch.epfl.ad.bloomjoin.SuperDuper;
 import ch.epfl.ad.db.AbstractDatabaseManager;
 import ch.epfl.ad.db.DatabaseManager;
 import ch.epfl.ad.db.parsing.NamedRelation;
 import ch.epfl.ad.db.parsing.QueryRelation;
 import ch.epfl.ad.db.queryexec.ExecStep;
 import ch.epfl.ad.db.queryexec.StepGather;
 import ch.epfl.ad.db.queryexec.StepRunSubq;
 import ch.epfl.ad.db.queryexec.StepSuperDuper;
 import ch.epfl.ad.db.queryexec.ExecStep.StepPlace;
 
 public class GraphProcessor {
 	
 	public static class QueryNotSupportedException extends Exception {
 		public QueryNotSupportedException(String string) {
 			super(string);
 		}
 		private static final long serialVersionUID = 9133124936966073467L;
 	}
 	
 	static class NDQueryVertex extends PhysicalQueryVertex {
 		public static NDQueryVertex newInstance(String name) {
 			return new NDQueryVertex(new NamedRelation(name));
 		}
 		public NDQueryVertex(NamedRelation relation) {
 			super(relation);
 		}
 	}
 	
 	QueryGraph graph;
 	Map<QueryVertex, List<QueryEdge>> edges;
 	List<ExecStep> execSteps;
 	
 	public GraphProcessor(QueryGraph g) {
 		graph = new QueryGraph(g);
 		edges = graph.getEdges();
 		execSteps = new LinkedList<ExecStep>();
 		try {
 			if(graph.getVertices().size() == 1 &&
 					graph.getVertices().iterator().next() instanceof SuperQueryVertex &&
 					((SuperQueryVertex) graph.getVertices().iterator().next()).isAggregate()) {
 				// if the graph is one big aggregate bubble, then process it directly
 				process(graph.getVertices().iterator().next());
 			} else {
 				// otherwise, put the whole thing in a single big bubble
 				process(new SuperQueryVertex(graph.getQuery(), graph.getVertices(), "whole_query"));
 			}
 			//execSteps.add(new StepRunSubq(graph.getQuery().toString(), "<return_results>"));
 		} catch (QueryNotSupportedException e) {
 			System.out.println("Oh! Ow, this query is not supported :/");
 		}
 		System.out.println(Arrays.toString(execSteps.toArray()));
 	}
 	
 	public List<ExecStep> getSteps() {
 		return execSteps;
 	}
 
 	public String executeSteps(DatabaseManager dbManager, List<String> allNodes) throws SQLException, InterruptedException {
 		String outRelationName = null;
 		for(ExecStep step : execSteps) {
 			if(step instanceof StepGather) {
 				System.out.println("Executing Gather");
 				outRelationName = ((StepGather) step).outRelation;
 				ResultSet dummyRS = dbManager.fetch("SELECT * FROM " + ((StepGather) step).fromRelation + " WHERE 1=2", allNodes.get(0));
 				String outSchema = AbstractDatabaseManager.tableSchemaFromMetaData(dummyRS.getMetaData());
 				dbManager.execute("CREATE TABLE " + outRelationName + " (" + outSchema + ")", allNodes.get(0));
 				dbManager.execute("SELECT * FROM " + ((StepGather) step).fromRelation, allNodes, outRelationName, allNodes.get(0));
 			} else if(step instanceof StepRunSubq) {
 				System.out.println("Executing RunSubq");
 				outRelationName = ((StepRunSubq) step).outRelation;
 				// TODO make the following line not return the whole results
 				ResultSet dummyRS = dbManager.fetch(((StepRunSubq) step).query, allNodes.get(0));
 				String outSchema = AbstractDatabaseManager.tableSchemaFromMetaData(dummyRS.getMetaData());
 				if(((StepRunSubq) step).stepPlace == StepPlace.ON_WORKERS) {
 					dbManager.execute("CREATE TABLE " + outRelationName + " (" + outSchema + ")", allNodes);
 					dbManager.execute(((StepRunSubq) step).query, allNodes, outRelationName);
 				} else if(((StepRunSubq) step).stepPlace == StepPlace.ON_MASTER) {
 					dbManager.execute("CREATE TABLE " + outRelationName + " (" + outSchema + ")", allNodes.get(0));
 					dbManager.execute(((StepRunSubq) step).query, allNodes.get(0), outRelationName);
 				}
 			} else if(step instanceof StepSuperDuper) {
 				System.out.println("Executing SuperDuper");
 				outRelationName = ((StepSuperDuper) step).outRelation.getName();
 				ResultSet dummyRS = dbManager.fetch("SELECT * FROM " + ((StepSuperDuper) step).fromRelation.getName() + " WHERE 1=2", allNodes.get(0));
 				String outSchema = AbstractDatabaseManager.tableSchemaFromMetaData(dummyRS.getMetaData());
 				SuperDuper sd = new SuperDuper(dbManager);
 				List<String> fromNodes = null;
 				if(((StepSuperDuper) step).distributeOnly) {
 					fromNodes = Arrays.asList(new String[]{allNodes.get(0)});
 				}else {
 					fromNodes = allNodes;
 				}
 				dbManager.execute("CREATE TABLE " + outRelationName + " (" + outSchema + ")", allNodes);
 				sd.runSuperDuper(fromNodes, allNodes, ((StepSuperDuper) step).fromRelation.getName(), ((StepSuperDuper) step).toRelation.getName(), ((StepSuperDuper) step).fromColumn, ((StepSuperDuper) step).toColumn, outRelationName);
 			}
 		}
 		return outRelationName;
 	}
 
 	public QueryVertex process(QueryVertex sqv1) throws QueryNotSupportedException {
 		// if it is not a super node, no need to do anything
 		if(!(sqv1 instanceof SuperQueryVertex))
 			return sqv1;
 		// check if query is valid (do not allow edges crossing boundaries)
 		SuperQueryVertex sqv = (SuperQueryVertex) sqv1;
 		for(QueryVertex qv : sqv.getVertices()){
 			if(edges.get(qv) != null)
 				for(QueryEdge qe : edges.get(qv))
 					if(!sqv.getVertices().contains(qe.getEndPoint()))
 						throw new QueryNotSupportedException("Edge Crossing Bubble Boundary");
 		}
 		// get rid of all super nodes in the current level
 		Set<QueryVertex> toAdd = new HashSet<QueryVertex>();
 		for(Iterator<QueryVertex> it = sqv.getVertices().iterator(); it.hasNext(); ) {
 			QueryVertex qv = it.next();
 			if(qv instanceof SuperQueryVertex) {
 				PhysicalQueryVertex pqv = (PhysicalQueryVertex) process(qv);
 				((QueryRelation) sqv.getQuery()).replaceRelation(((SuperQueryVertex) qv).getQuery(), (pqv.getRelation()));
 				graph.inheritVertex(pqv, qv);
 				it.remove();
 				toAdd.add(pqv);
 			}
 		}
 		sqv.getVertices().addAll(toAdd);
 		// now that we dont have super nodes, do the logic
 		Set<QueryVertex> nodeChildren = new HashSet<QueryVertex>();
 		nodeChildren.addAll(sqv.getVertices());
 		List<StepSuperDuper> eatenEdgesSteps = new LinkedList<StepSuperDuper>();
 		PhysicalQueryVertex picked;
 		while((picked = pickConnectedPhysical(sqv.getVertices())) != null) {
 			eatAllEdgesPhysical(sqv.getVertices(), picked, eatenEdgesSteps);
 			//System.out.println("####### NEW STATE");
 			//System.out.println(graph);
 		}
 		while((picked = pickConnectedND(sqv.getVertices())) != null) {
 			eatAllEdgesND(sqv.getVertices(), picked, eatenEdgesSteps);
 			//System.out.println("####### NEW STATE");
 			//System.out.println(graph);
 		}
 		// now we have disconnected physical tables and NDs
 		
 		
 		/*execSteps.add(new StepRunSubq(((SuperQueryVertex) qv).getQuery().toString(), ((SuperQueryVertex) qv).getAlias() + "_" + new Random().nextInt(1000)));
 		System.out.println("COUCOU " + 
 				( ((SuperQueryVertex) qv).getQuery() ) );
 		if(((SuperQueryVertex) qv).getQuery() instanceof QueryRelation) {
 			System.out.println("REPLACEMENT " + 
 					((QueryRelation) sqv.getQuery()).replaceRelation(
 							(QueryRelation) ((SuperQueryVertex) qv).getQuery(), 
 							new NamedRelation(pqv.getName()))
 			);
 		}*/
 		
 		
 		// if we only have 1, we return
 		if(sqv.getVertices().size() == 1) {
 			for(StepSuperDuper es : eatenEdgesSteps) {
 				((QueryRelation) sqv.getQuery()).replaceRelation(es.fromRelation, (es.outRelation));
 			}
 			execSteps.addAll(eatenEdgesSteps);
 			PhysicalQueryVertex singleVertex = (PhysicalQueryVertex) sqv.getVertices().iterator().next();
 			if(!sqv.isAggregate()) { // if not aggregate
 				PhysicalQueryVertex retVert = null;
 				if(!(singleVertex instanceof NDQueryVertex)) { // if distributed
 					retVert = PhysicalQueryVertex.newInstance(tempName(sqv.getAlias()));
 					execSteps.add(new StepRunSubq(sqv.getQuery().toString(), false, retVert.getName(), StepPlace.ON_WORKERS));
 					if(sqv.getAlias().equals("whole_query")) // if top level
 						execSteps.add(new StepGather(retVert.getName(), tempName(retVert.getName())));
 				} else {
 					retVert = NDQueryVertex.newInstance(tempName(sqv.getAlias()));
 					execSteps.add(new StepRunSubq(sqv.getQuery().toString(), false, retVert.getName(), StepPlace.ON_MASTER));
 				}
 				return retVert;
 			}
 			NDQueryVertex newVertex = NDQueryVertex.newInstance(tempName(singleVertex.getName()));
 			//String oldVertexName = singleVertex.getName();
 			if(!(singleVertex instanceof NDQueryVertex)) { // if distributed
 				//oldVertexName += "_" + new Random().nextInt(1000);
 				String intermediateTableName = tempName(singleVertex.getName());
 				execSteps.add(new StepRunSubq(sqv.getQuery().toIntermediateString(), true, intermediateTableName, StepPlace.ON_WORKERS));
 				NDQueryVertex gathered = NDQueryVertex.newInstance(tempName(singleVertex.getName()));
 				execSteps.add(new StepGather(intermediateTableName, gathered.getName()));
				//((QueryRelation) sqv.getQuery()).replaceRelation(singleVertex.getRelation(), gathered.getRelation());
				// if agg query joins several tables, tables then the replace fails !!!
				// instead of replacing, now the toFinalString method does the job
 				//System.out.println("************* replace " + singleVertex.getRelation().getName() + " with " + gathered.getRelation().getName() + " in " + sqv.getQuery());
				execSteps.add(new StepRunSubq(sqv.getQuery().toFinalString(gathered.getRelation()), true, newVertex.getName(), StepPlace.ON_MASTER));
 				return newVertex;
 				//execSteps.add(new StepAggregate("todo " + singleVertex.getName(), , StepPlace.ON_WORKERS));
 				//System.out.println("Aggregate everywhere " + singleVertex.getName() + " INTO " + oldVertexName + " on master");
 			}
 			execSteps.add(new StepRunSubq(sqv.getQuery().toString(), true, newVertex.getName(), StepPlace.ON_MASTER));
 			//execSteps.add(new StepAggregate("todo " + oldVertexName, newVertex.getName(), StepPlace.ON_MASTER));
 			//System.out.println("Aggregate on master " + oldVertexName + " INTO " + newVertex.getName());
 			return newVertex;
 		}
 		
 		// We reach here if we have disconnected components.. Ship everything to Master
 		for(QueryVertex qv : nodeChildren){
 			if(!(qv instanceof NDQueryVertex)) {
 				NDQueryVertex gathered = NDQueryVertex.newInstance(tempName(  ((PhysicalQueryVertex) qv).getName()  ));
 				execSteps.add(new StepGather(((PhysicalQueryVertex) qv).getName(), gathered.getName()));
 				((QueryRelation) sqv.getQuery()).replaceRelation(((PhysicalQueryVertex) qv).getRelation(), gathered.getRelation());
 			}
 		}
 		if(!sqv.isAggregate()) { // if not aggregate
 			PhysicalQueryVertex retVert = null;
 			retVert = NDQueryVertex.newInstance(tempName(sqv.getAlias()));
 			execSteps.add(new StepRunSubq(sqv.getQuery().toString(), false, retVert.getName(), StepPlace.ON_MASTER));
 			return retVert;
 		}
 		PhysicalQueryVertex newVertex = null;
 		newVertex = NDQueryVertex.newInstance(tempName(sqv.getAlias()));
 		execSteps.add(new StepRunSubq(sqv.getQuery().toString(), true, newVertex.getName(), StepPlace.ON_MASTER));
 		return newVertex;
 		
 		
 		
 		/*System.out.println("NOOOO WE HAVE DISCONNECTED COMPONENTS, should revert SuperDupers");
 		StringBuilder joinedTblName = new StringBuilder();
 		List<String> toCrossJoin = new ArrayList<String>();
 		for(QueryVertex qv : sqv.getVertices()){
 			if(qv instanceof NDQueryVertex) {
 				toCrossJoin.add(((PhysicalQueryVertex) qv).getName());
 			} else {
 				String tempTblName = ((PhysicalQueryVertex) qv).getName() + "_" + new Random().nextInt(1000);
 				execSteps.add(new StepGather(((PhysicalQueryVertex) qv).getName(), tempTblName));
 				//System.out.println("Gather on master " + ((PhysicalQueryVertex) qv).getName() +
 						//" INTO " + tempTblName);
 				toCrossJoin.add(tempTblName);
 			}
 			joinedTblName.append(((PhysicalQueryVertex) qv).getName() + "_");
 		}
 		joinedTblName.append(new Random().nextInt(1000));
 		execSteps.add(new StepJoin(toCrossJoin, null, joinedTblName.toString(), StepPlace.ON_MASTER));
 		//System.out.println("Join on master " + Arrays.toString(toCrossJoin.toArray()) +
 				//" INTO " + joinedTblName.toString());
 		if(sqv.isAggregate()) {
 			String oldName = joinedTblName.toString();
 			joinedTblName.append("_" + new Random().nextInt(1000));
 			execSteps.add(new StepAggregate("todo " + oldName, joinedTblName.toString(), StepPlace.ON_MASTER));
 			//System.out.println("Aggregate on master " + oldName + " INTO " + joinedTblName.toString());
 		}
 		return new NDQueryVertex(joinedTblName.toString());*/
 		
 		
 		/*boolean containsD = false;
 		for(QueryVertex qv : sqv.getVertices()){
 			if(!(qv instanceof NDQueryVertex)) {
 				containsD = true;
 				break;
 			}
 		}
 		String tableName = "_" + sqv.getAlias();
 		boolean skip = true;
 		for(QueryVertex qv : sqv.getVertices()){
 			if(qv instanceof NDQueryVertex) {
 				if(containsD)
 					System.out.println("Replicate " + ((NDQueryVertex) qv).getName());
 			} else {
 				if(!skip)
 					System.out.println("Gather and Replicate " + ((PhysicalQueryVertex) qv).getName());
 				skip = false;
 			}
 			tableName += "_" + ((PhysicalQueryVertex) qv).getName();
 		}
 		if(containsD)
 			System.out.println("Run sub query (whose alias is " + sqv.getAlias() + "), and send results to " + tableName + " on master");
 		else
 			System.out.println("Run sub query (whose alias is " + sqv.getAlias() + ") on master, and you're done!");
 		sqv.getVertices().clear();
 		NDQueryVertex ndqv = new NDQueryVertex(tableName);
 		sqv.getVertices().add(ndqv);
 		return ndqv;*/
 	}
 	
 	private PhysicalQueryVertex pickConnectedPhysical(Set<QueryVertex> vertices) {
 		// TODO pick smartly (e.g. choose largest table)
 		for(QueryVertex qv : vertices)
 			if(!(qv instanceof NDQueryVertex))
 				if(edges.get(qv) != null)
 					return (PhysicalQueryVertex) qv;
 		return null;
 	}
 	
 	private PhysicalQueryVertex pickConnectedND(Set<QueryVertex> vertices) {
 		// TODO pick smartly (e.g. choose largest table)
 		for(QueryVertex qv : vertices)
 			if(edges.get(qv) != null)
 				return (PhysicalQueryVertex) qv;
 		return null;
 	}
 	
 	private void eatAllEdgesPhysical(Set<QueryVertex> vertices, PhysicalQueryVertex pqv, List<StepSuperDuper> execSteps) {
 		List<String> toJoinStr = new ArrayList<String>();
 		List<String> joinCondStr = new ArrayList<String>();
 		PhysicalQueryVertex isp = (PhysicalQueryVertex) edges.get(pqv).get(0).getStartPoint();
 		toJoinStr.add(isp.getName());
 		Map<QueryEdge, PhysicalQueryVertex> history = new HashMap<QueryEdge, PhysicalQueryVertex>();
 		while(edges.get(pqv) != null) {
 			QueryEdge edge = edges.get(pqv).get(0);
 			PhysicalQueryVertex sp = (PhysicalQueryVertex) edge.getStartPoint();
 			PhysicalQueryVertex ep = (PhysicalQueryVertex) edge.getEndPoint();
 			PhysicalQueryVertex tempEPTbl = PhysicalQueryVertex.newInstance(tempName(ep.getName()));
 			toJoinStr.add(tempEPTbl.getName());
 			joinCondStr.add(edge.getJoinCondition().toString());
 			PhysicalQueryVertex joined = PhysicalQueryVertex.newInstance(sp.getName() + "_" + tempEPTbl.getName());
 			PhysicalQueryVertex shipTo = sp;
 			if(history.get(edge) != null)
 				shipTo = history.get(edge);
 			if(ep instanceof NDQueryVertex) {
 				execSteps.add(new StepSuperDuper(ep.getRelation(), shipTo.getRelation(), edge.getJoinCondition().getEndPointField(), edge.getJoinCondition().getStartPointField(), true, new NamedRelation(tempEPTbl.getName())));
 				/*System.out.println("Distribute (using bloom) " + ep.getName() +
 						" TO " + shipTo.getName() +  
 						" ON " + edge.getJoinCondition() +
 						" INTO " + tempEPTbl.getName());*/
 			} else {
 				execSteps.add(new StepSuperDuper(ep.getRelation(), shipTo.getRelation(), edge.getJoinCondition().getEndPointField(), edge.getJoinCondition().getStartPointField(), false, new NamedRelation(tempEPTbl.getName())));
 				/*System.out.println("SuperDuper " + ep.getName() +
 						" TO " + shipTo.getName() +  
 						" ON " + edge.getJoinCondition() +
 						" INTO " + tempEPTbl.getName());*/
 			}
 			graph.removeEdge(sp, ep);
 			for(QueryEdge e : graph.inheritVertex(joined, ep)) {
 				history.put(e, tempEPTbl);
 			}
 			for(QueryEdge e : graph.inheritVertex(joined, sp)) {
 				history.put(e, isp);
 			}
 			vertices.remove(sp);
 			vertices.remove(ep);
 			vertices.add(joined);
 			pqv = joined;
 		}
 		//execSteps.add(new StepJoin(toJoinStr, joinCondStr, pqv.getName(), StepPlace.ON_WORKERS));
 		/*System.out.println("Join everywhere " + Arrays.toString(toJoinStr.toArray()) +
 				" ON " + Arrays.toString(joinCondStr.toArray()) +
 				" INTO " + pqv.getName());*/
 	}
 	
 	private void eatAllEdgesND(Set<QueryVertex> vertices, PhysicalQueryVertex pqv, List<StepSuperDuper> execSteps) { // both are ND
 		List<String> toJoinStr = new ArrayList<String>();
 		List<String> joinCondStr = new ArrayList<String>();
 		toJoinStr.add(((PhysicalQueryVertex) edges.get(pqv).get(0).getStartPoint()).getName());
 		while(edges.get(pqv) != null) {
 			QueryEdge edge = edges.get(pqv).get(0);
 			PhysicalQueryVertex sp = (PhysicalQueryVertex) edge.getStartPoint();
 			PhysicalQueryVertex ep = (PhysicalQueryVertex) edge.getEndPoint();
 			toJoinStr.add(ep.getName());
 			joinCondStr.add(edge.getJoinCondition().toString());
 			PhysicalQueryVertex joined = NDQueryVertex.newInstance(tempName(sp.getName() + "_" + ep.getName()));
 			graph.removeEdge(sp, ep);
 			graph.inheritVertex(joined, ep);
 			graph.inheritVertex(joined, sp);
 			vertices.remove(sp);
 			vertices.remove(ep);
 			vertices.add(joined);
 			pqv = joined;
 		}
 		//execSteps.add(new StepJoin(toJoinStr, joinCondStr, pqv.getName(), StepPlace.ON_MASTER));
 		/*System.out.println("Join on master " + Arrays.toString(toJoinStr.toArray()) +
 				" ON " + Arrays.toString(joinCondStr.toArray()) +
 				" INTO " + pqv.getName());*/
 	}
 	
 	private static String tempName(String orig) {
 		return "tmp_" + orig + "_" + new Random().nextInt(1000);
 	}
 
 }
