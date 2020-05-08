 package com.drexelexp.baseDAO;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public abstract class SearchableDAO<T> extends JdbcBaseDAO<T> {
 	protected String getSearchTable(){
 		return getTableName();
 	}
 	protected abstract List<String> getSearchableColumns();
 	
 	public List<T> search(String query) {
 		//TODO update to search multiple columns
 		LinkedList<T> items = new LinkedList<T>();
 		
 		String sql = "SELECT "+getTableName()+".* FROM " + getSearchTable() + " WHERE "+getSearchableColumns().get(0)+" LIKE ?";
 
 		System.out.println(sql);
 		
 		for (String queryPart : Arrays.asList(query.split(" "))) {
 			Connection conn = null;
 			
 			try {
 				conn = dataSource.getConnection();
 				PreparedStatement ps = conn.prepareStatement(sql);
 			
 				ps.setString(1, "%" + queryPart + "%");
 
 				ResultSet rs = ps.executeQuery();
 			
 				while (rs.next()) {
 					T item = parseResultSetRow(rs);
 					
 					T toMod = null;
 					boolean itemNotFound = true;
 					for (T i : items) {
 						if (getId(i) == getId(item)) {
 							toMod = i; // Because modifying while iterating does
 										// stupid things...
 							itemNotFound = false;
 						}
 					}
 
 					if (itemNotFound)
 						items.add(item);
 					
 					else {
 						items.remove(toMod);
 						items.addFirst(toMod);
 					}
 				}
 
 				rs.close();
 				ps.close();
 				conn.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			} finally {
 				if (conn != null) {
 					try {
 						conn.close();
 					} catch (SQLException e) {
 					}
 				}
 			}
 		}

 		return items;
 	}
 }
