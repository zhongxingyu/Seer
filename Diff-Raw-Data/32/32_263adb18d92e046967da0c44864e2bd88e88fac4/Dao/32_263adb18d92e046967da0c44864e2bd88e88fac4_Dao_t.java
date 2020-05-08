 package toctep.skynet.backend.dal.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.impl.mysql.DaoConnectionImpl;
 import toctep.skynet.backend.dal.domain.Domain;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 public abstract class Dao {
 
 	private Object connection;
 	
 	protected String tableName;
 	
 	public Dao() {
 		connection = DaoConnectionImpl.getInstance().getConnection();
 		
 		setTableName();
 	}
 	
 	protected abstract void setTableName();
 	
 	public abstract void insert(Domain domain);
 	public abstract void update(Domain domain);
 	public abstract void delete(Domain domain);
 	
 	public int count() {
 		Connection conn = (Connection) this.getConnection();
 		
 		int count = 0;
 		
 		Statement stmt = null;
 		ResultSet rs = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
 		    rs.next();
 		    count = rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 				rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return count;
 	}
 	
 	protected Object getConnection() {
 		return connection;
 	}
 }
