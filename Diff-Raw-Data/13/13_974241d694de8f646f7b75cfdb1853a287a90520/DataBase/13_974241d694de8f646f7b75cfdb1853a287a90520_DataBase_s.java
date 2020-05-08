 package modele;
 
 import general.Constantes;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class DataBase {
 	private SQLInterface bdd;
 	
 	/**
 	 * Constructeur de DataBase a partir du nom de la database
 	 * @param nomBdd
 	 */
 	public DataBase(String nomBdd) {
 		try{
 			bdd = new SQLiteImpl(nomBdd);
 		} catch (Exception e) {
 			Constantes.afficherException(e);
 		}
 	}
 	
 	/**
 	 * Cette methode permet de supprimer un objet defini par son id dans la table
 	 * @param table
 	 * @param id
 	 */
 	public void supprimer (String table, int id){
 		String query = "Delete From " + table + " Where id='" + id + "'";
 		try {
 			bdd.update(query);
 		} catch (SQLException e) {
 			Constantes.afficherException(e);
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
 			Set<String> set = map.keySet();
 			Iterator<String> it = set.iterator();
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
 			bdd.update(query);
 		} else {
 			throw new SQLException("Pas de champ id dans la map");
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
 			Set<String> set = map.keySet();
 			Iterator<String> it = set.iterator();
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
 			
 			bdd.update(query);
 		} else {
 			throw new SQLException("Pas de champ id dans la map");
 		}
 	}
 	
 	/**
 	 * @param query
 	 * @return La liste des objets
 	 */
 	public List<Map<String, Object>> getObjets(String query) {
 		try {
 			return bdd.query(query);
 		} catch (SQLException e) {
 			Constantes.afficherException(e);
 		}
 		return null;
 	}
 	
 	/**
 	 * @param table
 	 * @return la liste des attributs (nom et type) d'une table
 	 */
 	public Map<String, Integer> getAttributs (String table) {
 		return bdd.getAttributs(table);
 	}
 
 }
