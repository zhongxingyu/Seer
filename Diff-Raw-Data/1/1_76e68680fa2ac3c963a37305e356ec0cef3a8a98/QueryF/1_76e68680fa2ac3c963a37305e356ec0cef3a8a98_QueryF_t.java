 package de.softwarekollektiv.dbs.queries.complex;
 
 import java.io.PrintStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import de.softwarekollektiv.dbs.app.MenuItem;
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 
 class QueryF implements MenuItem {
 
 	private final PrintStream out;
 	private final DbConnection dbcon;
 	
 	private PreparedStatement actIdStmt;
 	private PreparedStatement hasWorkedWithStmt;
 	
 	QueryF(PrintStream out, DbConnection dbcon) {
 		this.out = out;
 		this.dbcon = dbcon;
 	}
 
 	@Override
 	public String getTitle() {
 		return "Shortest path";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Beantwortet Aufgabe 5.2.b:\n[...]\nErmitteln Sie nun die kürzeste Verbindung zwischen folgenden Paaren:\ni. Johnny Depp und Timothy Dalton\nii. Johnny Depp und August Diehl\niii. Bill Murray und Sylvester Stallone\niv. Edward Norton und Don Cheadle";
 	}
 
 	private Queue<Integer> queue = new LinkedList<Integer>();
 	private Set<Integer> visited = new TreeSet<Integer>();
 	private Map<Integer, Integer> predecessor = new TreeMap<Integer, Integer>();
 	
 	@Override
 	public boolean run() throws Exception {
 		
 		String[][] pairs = {
 				{"Depp, Johnny", "Dalton, Timothy"},
 				{"Depp, Johnny", "Diehl, August"},
 				{"Murray, Bill (I)", "Stallone, Sylvester"},
 				{"Norton, Edward (I)", "Cheadle, Don"}
 		};
 		
 		
 		actIdStmt = dbcon.getConnection().prepareStatement("SELECT act_id FROM actors WHERE name = ?");
 		hasWorkedWithStmt = dbcon.getConnection().prepareStatement(
 				"SELECT act_id FROM features WHERE mov_id IN (SELECT DISTINCT mov_id FROM features WHERE act_id = ?)"
 		);
 		out.println();
 		for(String[] pair : pairs) {
 			int startId = getId(pair[0]);
 			int endId = getId(pair[1]);
 			out.println("Shortest path between " + pair[0] + " and " + pair[1] + " is: " +actIdsToNames( shortestPath(startId, endId)));
 		}
 		
 		actIdStmt.close();
 		hasWorkedWithStmt.close();
 		return true;
 	}
 	
 	/**
 	 * BFS
 	 * @throws SQLException 
 	 */
 	private List<Integer> shortestPath(int startId, int endId) throws SQLException {
 		
 			queue.clear();
 			visited.clear();
 			predecessor.clear();
 		
 			queue.offer(startId);
 			visited.add(startId);
 			predecessor.put(startId, null);
 			
 			out:
 			while (!queue.isEmpty()){
 				int node = queue.poll();
 				if (node == endId){
 					return null;
 				}
 				for (int neighbor : getActorsWhoWorkedWith(node)){
 					if (neighbor == endId) {
 						predecessor.put(neighbor, node);
 						break out;
 					}
 					if (!visited.contains(neighbor)){
 						queue.offer(neighbor);
 						visited.add(neighbor);
 						predecessor.put(neighbor, node);
 					}
 				}
 			}
 			
 			return shortestPathAsList(predecessor, endId);
 	}
 	
 	private List<String> actIdsToNames(List<Integer> shortestPathAsList) throws SQLException {
 		PreparedStatement actIdToNameStmt = dbcon.getConnection().prepareStatement(
 		"SELECT name FROM actors WHERE act_id = ?");
 		
 		List<String> list = new LinkedList<String>();
 		for (Integer id : shortestPathAsList){
 			actIdToNameStmt.setInt(1, id);
 			ResultSet result = actIdToNameStmt.executeQuery();
 			result.next();
 			list.add("\""+result.getString(1)+"\"");
 			result.close();
 		}
 		
 		actIdToNameStmt.close();
 		
 		return list;
 	}
 
 	private List<Integer> shortestPathAsList(
 			Map<Integer, Integer> predecessors, int endId) {
 		List<Integer> result = new LinkedList<Integer>();
 		result.add(endId);
 		Integer l_pred = endId;
 		do {
 			l_pred = predecessors.get(l_pred);
 			result.add(l_pred);
 			
 		}while (l_pred != null);
 		Collections.reverse(result);	
 		result.remove(0);
 		return result;
 	}
 
 	private List<Integer> getActorsWhoWorkedWith(int id) throws SQLException {
 		List<Integer> result = new LinkedList<Integer>();
 		
 		hasWorkedWithStmt.setInt(1, id);
 		ResultSet rs = hasWorkedWithStmt.executeQuery();
 		while(rs.next())
 			result.add(rs.getInt("act_id"));
 		rs.close();
 		
 		return result;
 	}
 	
 	private int getId(String name) throws SQLException {
 		actIdStmt.setString(1, name);
 		ResultSet rs = actIdStmt.executeQuery();
 		// must exist
 		rs.next();
 		int result = rs.getInt(1);
 		rs.close();
 		return result;
 	}
 }
