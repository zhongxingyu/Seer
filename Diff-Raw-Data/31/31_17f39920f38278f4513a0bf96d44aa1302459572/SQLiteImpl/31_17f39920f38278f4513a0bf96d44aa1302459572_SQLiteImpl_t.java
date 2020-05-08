 package Modele;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class SQLiteImpl implements SQLInterface{
 	// register the driver 
 	String sDriverName = "org.sqlite.JDBC";
 	
 	// now we set up a set of fairly basic string variables to use in the body of the code proper
 	String sTempDb = "database.sqlite";
 	String sJdbc = "jdbc:sqlite";
 	String sDbUrl = sJdbc + ":" + sTempDb;
 	
 	int iTimeout = 30;	 
 
 	
 	public SQLiteImpl(String name) throws Exception{
 		sTempDb = name;
 		Class.forName(sDriverName);
 	}
 	
 	
 	/**
 	 * Create a database connection. BTW, this method may need more param
 	 * @param name of the database
 	 * @return Connection
 	 */
 	public Connection connect() throws SQLException {
 		return DriverManager.getConnection(sDbUrl);	
 	}
 
 	
 	/**
 	 * Create and send a query to the database
 	 * @param query
 	 * @return ResultSet with the information
 	 */
 	public List<Map<String, Object>> query(String query) throws SQLException {
 		// Check if this a SELECT query, if not, throw an exception
 		String pattern = "^SELECT.*";
 		List<Map<String, Object>> resul = new ArrayList<Map<String, Object>>();
 		
 		if(query.matches(pattern)){
 			
 			try {
 				// Creation statement
 				Connection conn = connect();
 				ResultSet res = null; 
 				Statement stmt = conn.createStatement();
 				
 				// Execute query
 				stmt.setQueryTimeout(iTimeout);
 				res = stmt.executeQuery(query);
 				resul = this.transforme(res);
 				
 				// Fermeture des statements ...
 				res.close(); 
 			    stmt.close();
 			    conn.close();
 			} catch (SQLException e){
 				e.printStackTrace();
 			}
 			return resul;
 		} else {
 			throw new SQLException("The query is not a SELECT query : " + query);
 		}
 	}
 
 	
 	/**
 	 * Transforme un ResultSet en une List de Map.
 	 * @param ResultSet
 	 * @ return List
 	 */
 	public List<Map<String, Object>> transforme (ResultSet rs) {
 		List<Map<String, Object>> resul = new ArrayList<Map<String, Object>>();
 		ResultSetMetaData metadata;
 
 		try {
 			metadata = rs.getMetaData();
 			int nombreColonnes = metadata.getColumnCount();
 			while (rs.next()) {
 				Map<String, Object> record = new HashMap<String, Object>();
 				for (int i = 1; i <= nombreColonnes; i++){
 					record.put(metadata.getColumnName(i), rs.getObject(i));
 				}
 				resul.add(record);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return resul;
 	}
 	
 	
 	/**
 	 * Update a database
 	 * @param query
 	 */
 	public void update(String query) throws SQLException {
 		// Check if this a UPDATE query, if not, throw an exception
 		String pattern = "^['UPDATE''INSERT INTO'].*";
 		
 		if(query.matches(pattern)){
 			Connection conn = connect();
 			try {
 				// Creation statement
 				Statement stmt = conn.createStatement();
 				
 				// Execute query
 				stmt.setQueryTimeout(iTimeout);
 				stmt.executeUpdate(query);
 				
 				//CREATION MULTIBLE D ENTREES POUR METHODE UPDATE
 				/*for (int w = 21000; w < 32000; w++){
 					String sql = "INSERT INTO people \nSELECT " + w + ", 'pierre', 'Durand'";
 
 					for (int y = w + 1 ; y < w + 500; y++){
 						sql = sql.concat(" \n UNION \n SELECT " + y + ", 'pierre', 'durand'");
 					}
 					w += 500;
 					System.out.println(sql);
 					stmt.executeUpdate(sql);
 				}*/
 				
 				// Fermeture des statements
 			    stmt.close();
 			    conn.close();
 			} catch (SQLException e){
 				e.printStackTrace();
 			}
 		} else {
 			throw new SQLException("The query is not a UPDATE query : " + query);
 		}
 	}
 	
 	
 	/**
 	 * MAJ un objet dans la bonne table
 	 * @param table
 	 * @param map
 	 */
 	public void enregistreBDD (String table, Map<String, Object> map) throws SQLException {
 		if (map.containsKey("id")) {
 			String query = "UPDATE " + table + " SET ";
 			boolean first = true;
 			Set set = map.keySet();
 			Iterator it = set.iterator();
 			while (it.hasNext()) {
 				if (first) {
 					first = false;
 				} else {
 					query = query.concat(", ");
 				}
 				Object key = it.next();
 				query = query.concat( key + "='" + map.get(key) + "'");
 			}
 			
 			query = query.concat(" WHERE id=" + map.get("id"));
 			
 			try {
 				this.update(query);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			throw new SQLException("Pas de champ id dans la map");
 		}
 	}
 	
 
 	/**
 	 * Ajoute un objet dans la bonne table
 	 * @param table
 	 * @param map
 	 */
 	public void ajoutBDD (String table, Map<String, Object> map) throws SQLException {
		if (map.containsKey("id") || map.containsKey("id_personne")) {
 			String query = "INSERT INTO " + table + " (";
 			String fin = ") VALUES (";
 			boolean first = true;
 			Set set = map.keySet();
 			Iterator it = set.iterator();
 			while (it.hasNext()) {
 				if (first) {
 					first = false;
 				} else {
 					query = query.concat(", ");
 					fin = fin.concat(", ");
 				}
 				Object key = it.next();
 				query = query.concat(key.toString());
 				fin = fin.concat("'"+map.get(key).toString()+"'");
 			}
 			
 			query = query.concat(fin);
 			query = query.concat(")");
 			try {
 				this.update(query);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			throw new SQLException("Pas de champ id dans la map");
 		}
 	}
 	
 	
 
 	
 	public static void mainTest (String[] args) throws SQLException{
 		long start; 
 		start = System.nanoTime();
 		
 		Billeterie bill = new Billeterie("database.sqlite");
 		
 		HashMap<String, Object> personne = new HashMap<String, Object>();
 		personne.put("id", 4);
 		personne.put("name", "Le Gaulois");
 		personne.put("first_name", "Guy");
 
 		List<Map<String, Object>> list = bill.getBdd().query("SELECT * FROM people");
 		for(int i = 0; i < list.size(); i++){
 			System.out.println(list.get(i));
 		}
 		
 		bill.getBdd().ajoutBDD("people",personne);
 		bill.getBdd().enregistreBDD("people", list.get(0));
 		
 		long duree = System.nanoTime() - start;
 		System.out.println(duree);
 	}
 }
