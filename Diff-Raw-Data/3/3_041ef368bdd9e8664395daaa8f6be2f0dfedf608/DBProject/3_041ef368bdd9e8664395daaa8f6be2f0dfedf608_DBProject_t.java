 package it.polito.atlas.alea2.db;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 
 import it.polito.atlas.alea2.Annotation;
 import it.polito.atlas.alea2.Project;
 
 public class DBProject {
 
 	protected static boolean write(Project p, DBInstance db) throws SQLException, DBRuntimeException {
         if(p == null)
             return false;
         boolean allOK = true;
         
 		String name = p.getName();
 		String tags = "";
 		for (String t : p.getTags())
 			tags += t + " ";
 		String sql = "insert into Project (name, tags) values('" + 
 			name + "', '" + 
 			tags.trim() + "')";
		
         ResultSet rs = null;
 		long id_project = -1;
 		Statement stmt = db.getStatement();
 		if (stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) != 1)
 			throw new DBRuntimeException("Insert project failed (" + name + ")");
 	
 		rs = stmt.getGeneratedKeys();
 
 		if (rs.next()) {
 			id_project = rs.getLong(1);
 		} else {
 			throw new DBRuntimeException("Can't get id_project of (" + name + ")");
 		}
 
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			// ignore
 			e.printStackTrace();
 		}
 		
 		for (Annotation a : p.getAnnotations()) {
 			try {
 				allOK &= DBAnnotation.write(a, id_project, db);
 			} catch (SQLException e) {
 				e.printStackTrace();
 				allOK = false;
 			} catch (DBRuntimeException e) {
 				e.printStackTrace();
 				System.err.println(e.getMessage());
 				allOK = false;
 			}
 		}
         return allOK;
 	}
 
 	protected static List<String> listProjectNames(DBInstance db) throws SQLException {
 		List<String> p = new ArrayList<String>();
 		String sql = "select name from project order by name";
 		ResultSet rs = null;
 		rs = db.getStatement().executeQuery(sql);
 		while (rs.next()) {
 			try {
 				p.add(rs.getString(1));
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			// ignore
 		}
 		return p;
 	}
 
 	protected static Dictionary<String, String> getProjectNamesTagsList(DBInstance db) throws SQLException {
 		Dictionary<String, String> p = new Hashtable<String, String>();
 		String sql = "select name, tags from project order by name";
 		ResultSet rs = null;
 		rs = db.getStatement().executeQuery(sql);
 		while (rs.next()) {
 			String tags;
 			try {
 				tags = rs.getString(2);
 			} catch (SQLException e) {
 				e.printStackTrace();
 				tags = "";
 			}
 			try {
 				p.put(rs.getString(1), tags);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			// ignore
 		}
 		return p;
 	}
 
 	protected static boolean renameProject(String oldProjectName, String newProjectName, DBInstance db) throws SQLException {
 		String sql = "update project set name = '" + newProjectName + "' where name = '" + oldProjectName + "'";
 		db.update(sql);
 		return true;
 	}
 
 	protected static Project read(String projectName, DBInstance db) throws SQLException {
 		long id_project = -1;
 		String sql = "select id_project, tags from project where name = '" + projectName + "'";
 		Project p = null;
 		Collection<Annotation> annotations = new ArrayList<Annotation>();
 
 		// Legge l'id e i tags del progetto
 		Statement stmt = db.getStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		
 		if (rs.next()) {
 			id_project = rs.getLong(1);
 			p = new Project(projectName);
 			String tags;
 			try {
 				tags = rs.getString(2);
 			} catch (Exception SQLException) {
 				tags = "";
 			}
 			p.addTags(tags.split(" "));
 		}
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			// ignore
 			e.printStackTrace();
 		}
 
 		// Legge le annotazioni del progetto
 		annotations = DBAnnotation.readAll(id_project, db);
 	
 		// Assegna le annotazioni al progetto
 		for (Annotation a : annotations) {
 			p.addAnnotation(a);
 		}
 		
 		return p;
 	}
 }
