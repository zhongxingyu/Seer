 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.sql.Connection;
 
 
 public abstract class BaseModel {
 	protected Integer id = null;
 	
 	public int getId() {
 		return id;
 	}
 	
 	public boolean isNew(){
 		return id == null;
 	}
 
 	public void save() throws SQLException {
 		if (this.id != null){
 			update();
 		} else {
 			create();
 		}
 	}
 	
 	public void destroy() throws SQLException {
 		if (this.id == null) return;
 		
 		PreparedStatement stmt	= null;
 		
 		try {			
 			stmt = getDeleteStatement();
 			stmt.setInt(1, this.id);
 			stmt.executeUpdate();
 		} catch (Exception e){
 			throw new DatabaseException("Can't destroy", e);
 		} finally {
 			if (stmt != null){
 				stmt.close();
 			}
 		}
 	}
 	
 	private void update() throws SQLException {
 		PreparedStatement stmt	= null;
 		
 		try {			
 			stmt = getUpdateStatement();
 			stmt.executeUpdate();
 		} catch (Exception e){
 			throw new DatabaseException("Can't update", e);
 		} finally {
 			if (stmt != null){
 				stmt.close();
 			}
 		}
 	}
 	
 	private void create() throws SQLException {
 		PreparedStatement stmt	= null;
 		ResultSet rs			= null;
		Connection c = DatabaseConnection.getConnection(); 
 		
		try {			
 			c.setAutoCommit(false);
 			
 			stmt = getCreateStatement();
 			stmt.executeUpdate();
 			
			c.commit();
			
 			rs = c.createStatement().executeQuery("SELECT last_insert_rowid() AS [ID]");
 			rs.next();
 			
			this.id = rs.getInt("ID");			
 		} catch (Exception e){
 			c.rollback();
 			throw new DatabaseException("Can't create", e);
 		} finally {
 			if (stmt != null){
 				stmt.close();
 			}
 			
 			if (rs != null){
 				rs.close();
 			}
 				
 			c.setAutoCommit(true);
 		}
 	}
 
 	protected abstract PreparedStatement getCreateStatement() throws SQLException;
 	protected abstract PreparedStatement getUpdateStatement() throws SQLException;
 	protected abstract PreparedStatement getDeleteStatement() throws SQLException;
 }
