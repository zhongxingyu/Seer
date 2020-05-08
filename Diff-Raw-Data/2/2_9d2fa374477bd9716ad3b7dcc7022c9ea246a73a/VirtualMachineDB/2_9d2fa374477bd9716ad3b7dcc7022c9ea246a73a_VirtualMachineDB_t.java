 /**
  * 
  */
 package server.database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.VirtualMachine;
 
 /**
  * @author Derek Carr
  *
  */
 public class VirtualMachineDB {
 	
 	private Database db;
 	
 	
 	public VirtualMachineDB(Database db) {
 		this.db = db;
 	}
 	
 	public List<VirtualMachine> getAll() throws SQLException{
 		ArrayList<VirtualMachine> VMList = new ArrayList<VirtualMachine>();
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     try {
     	String sql = "SELECT * FROM vm_cloud";
     	stmt = db.getConnection().prepareStatement(sql);
     	
     	rs = stmt.executeQuery();
     	while (rs.next()) {
     		int id = rs.getInt(1);
     		String hostname = rs.getString(2);
     		String ip = rs.getString(3);
     		int osId = rs.getInt(4);
     		boolean available = rs.getBoolean(5);
     		boolean inQueue = rs.getBoolean(6);
     		double qTime = rs.getDouble(7);
     		int numJobs = rs.getInt(8);
     		Timestamp createdDate = rs.getTimestamp(9);
     		Timestamp modifiedDate = rs.getTimestamp(10);
     		VirtualMachine vm = new VirtualMachine(id, hostname, ip, osId, available, inQueue, qTime, numJobs, modifiedDate, createdDate);
     		VMList.add(vm);
     	}
     } catch (SQLException e) {
     	
     } finally {
     	if (rs != null) rs.close();
     	if (stmt != null) stmt.close();
     }
 		return VMList;
 	}
 	
 	public List<VirtualMachine> getByBrowser(String browser, boolean isInQueue) throws SQLException {
 		assert browser != null;
 		ArrayList<VirtualMachine> VMList = new ArrayList<VirtualMachine>();
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     try {
     	String sql = "SELECT * FROM vm_cloud a " +
     							 "INNER JOIN vm_cloud2browser b ON b.vm_cloud_id = a.id " +
     							 "INNER JOIN vm_browsers c ON b.vm_browser_id = c.id " +
     							 "WHERE c.name = ?";
     	stmt = db.getConnection().prepareStatement(sql);
     	stmt.setString(1, browser);
 
     	rs = stmt.executeQuery();
     	while (rs.next()) {
     		int id = rs.getInt(1);
     		String hostname = rs.getString(2);
     		String ip = rs.getString(3);
     		int osId = rs.getInt(4);
     		boolean available = rs.getBoolean(5);
     		boolean inQueue = rs.getBoolean(6);
     		double qTime = rs.getDouble(7);
     		int numJobs = rs.getInt(8);
     		Timestamp createdDate = rs.getTimestamp(9);
     		Timestamp modifiedDate = rs.getTimestamp(10);
     		
     		VirtualMachine vm = new VirtualMachine(id, hostname, ip, osId, available, inQueue,
     				qTime, numJobs, modifiedDate, createdDate);
     		if (isInQueue) {
     			if (inQueue) {
         		VMList.add(vm);
     			}
     		} else {
       		VMList.add(vm);
     		}
     	}
     } catch (SQLException e) {
     	
     } finally {
     	if (rs != null) rs.close();
     	if (stmt != null) stmt.close();
     }
 		return VMList;
 	}
 	
 	@SuppressWarnings("null")
 	public List<VirtualMachine> getByBrowserAndVersion(String browser, String version, boolean isInQueue) throws SQLException {
 		assert browser!=null && version!=null;
 		ArrayList<VirtualMachine> VMList = new ArrayList<VirtualMachine>();
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     try {
     	String sql = "SELECT * FROM vm_cloud a " +
     							 "INNER JOIN vm_cloud2browser b ON b.vm_cloud_id = a.id " +
     							 "INNER JOIN vm_browsers c ON b.vm_browser_id = c.id " +
     							 "WHERE c.name = ? and c.version = ?";
     	stmt.setString(1, browser);
     	stmt.setString(2, version);
     	stmt = db.getConnection().prepareStatement(sql);
     	
     	rs = stmt.executeQuery();
     	while (rs.next()) {
     		int id = rs.getInt(1);
     		String hostname = rs.getString(2);
     		String ip = rs.getString(3);
     		int osId = rs.getInt(4);
     		boolean available = rs.getBoolean(5);
     		boolean inQueue = rs.getBoolean(6);
     		double qTime = rs.getDouble(7);
     		int numJobs = rs.getInt(8);
     		Timestamp createdDate = rs.getTimestamp(9);
     		Timestamp modifiedDate = rs.getTimestamp(10);
     		
     		VirtualMachine vm = new VirtualMachine(id, hostname, ip, osId, available, inQueue, 
     				qTime, numJobs, modifiedDate, createdDate);
     		if (isInQueue) {
     			if (inQueue) {
         		VMList.add(vm);
     			}
     		} else {
       		VMList.add(vm);
     		}
     	}
     } catch (SQLException e) {
     	
     } finally {
     	if (rs != null) rs.close();
     	if (stmt != null) stmt.close();
     }
 		return VMList;
 	}
 	
 	public VirtualMachine getVirtualMachine(int id) {
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     VirtualMachine vm = null;
     try {
     	String sql = "SELECT * FROM vm_cloud where id = ?";
     	stmt = db.getConnection().prepareStatement(sql);
     	stmt.setInt(1, id);
     	rs = stmt.executeQuery();
     	while (rs.next()) {
     		int vid = rs.getInt(1);
     		String hostname = rs.getString(2);
     		String ip = rs.getString(3);
     		int osId = rs.getInt(4);
     		boolean available = rs.getBoolean(5);
     		boolean inQueue = rs.getBoolean(6);
     		double qTime = rs.getDouble(7);
     		int numJobs = rs.getInt(8);
     		Timestamp createdDate = rs.getTimestamp(9);
     		Timestamp modifiedDate = rs.getTimestamp(10);
     		
     		vm = new VirtualMachine(vid, hostname, ip, osId, available, inQueue, 
     				qTime, numJobs, modifiedDate, createdDate);
     	}
     } catch (SQLException e) {
     	
     } finally {
 			try {
 				if (rs != null) rs.close();
 				if (stmt != null) stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     }
     return vm;
 	}
 	
 	public boolean getVMAvailable(int id) throws SQLException {
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     boolean available = false;
     try {
     	String sql = "SELECT * FROM vm_cloud where id = ?";
     	stmt = db.getConnection().prepareStatement(sql);
     	stmt.setInt(1, id);
     	rs = stmt.executeQuery();
     	while (rs.next())	{
     		available = rs.getBoolean(5);
     	}
     } catch (SQLException e) {
     	
     } finally {
     	if (rs != null) rs.close();
     	if (stmt != null) stmt.close();
     }
     return available;
 	}
 	
 	public boolean getInQueue(int id) throws SQLException {
 		PreparedStatement stmt = null;
     ResultSet rs = null;
     boolean inQueue = false;
     try {
     	String sql = "SELECT * FROM vm_cloud where id = ?";
     	stmt = db.getConnection().prepareStatement(sql);
     	stmt.setInt(1, id);
     	rs = stmt.executeQuery();
     	while (rs.next())	{
     		inQueue = rs.getBoolean(6);
     	}
     } catch (SQLException e) {
     	
     } finally {
     	if (rs != null) rs.close();
     	if (stmt != null) stmt.close();
     }
     return inQueue;
 	}
 	
 	public void updateAvailable() {
 		PreparedStatement stmt = null;
 		try {
 			String sql = "UPDATE vm_cloud SET available=1 WHERE inQueue=1";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.executeUpdate(); 
 		} catch (SQLException e) {
 			e.printStackTrace();
 	  } finally {
     	if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 	  }
   }
 	
 	public void setAvailable(int id) {
 		PreparedStatement stmt = null;
 		try {
			String sql = "UPDATE vm_cloud SET available=0 WHERE id = ?";
 			stmt = db.getConnection().prepareStatement(sql);
 			stmt.setInt(1, id);
 			stmt.executeUpdate(); 
 		} catch (SQLException e) {
 			e.printStackTrace();
 	  } finally {
     	if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 	  }
   }
 
 
 }
