 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import puf.m2.hms.db.Database;
 import puf.m2.hms.db.DatabaseImpl;
 
 
 public class Physician {
 
     private static final Database DB = DatabaseImpl.defaultDb;
     private static final Map<Integer, Physician> PHYSICIAN_MAP = new HashMap<Integer, Physician>();
     
     private int id;
     private String name, role;
     private boolean available;
 
     public Physician(String name, String role, boolean available) {
 
     	this.name = name;
 		this.role = role;
 		this.available = available;
 	}
     
     public void save() throws HmsException {
     	id = getNextFreeId();
     	final String queryTemple = "insert into Physician values({0}, ''{1}'', ''{2}'', ''{3}'')";
     	try {
     		DB.createConnection();
     		
     		int available = this.available ? 1 : 0;
     		DB.executeUpdate(MessageFormat.format(queryTemple, id, name, role, available));
     		DB.closeConnection();
     		
     		PHYSICIAN_MAP.put(id, this);
     	} catch (SQLException e) {
     		throw new HmsException(e);
     	}
     }
     
     public void update() throws HmsException {
     	final String queryTemple = "update Physician set name = ''{0}'', role = ''{1}'', available = ''{2}'' where id = {4}";
     	try {
     		DB.createConnection();
     		
     		int available = this.available ? 1 : 0;
     		DB.executeUpdate(MessageFormat.format(queryTemple, name, role, available, id));
     		DB.closeConnection();
     	} catch (SQLException e) {
     		throw new HmsException(e);
     	}
     }
 
     public static List<Physician> getDoctors() throws HmsException {
         List<Physician> doctorList = new ArrayList<Physician>();
 
         final String query = "select * from Physician where role = 'Doctor'";
 
         try {
             DB.createConnection();
 
             ResultSet rs = DB.executeQuery(query);
 
             while (rs.next()) {
                 int id = rs.getInt("id");
                 Physician physician = PHYSICIAN_MAP.get(id);
                 
                 if (physician == null) {
                     boolean available = true;
                     if (rs.getInt("available") == 0) {
                         available = false;
                     }
                     physician = new Physician(rs.getString("name"), rs.getString("role"), available);
                     physician.id = id;
                     
                     PHYSICIAN_MAP.put(id, physician);
                 }
                 
                 doctorList.add(physician);
             }
 
             DB.closeConnection();
         } catch (SQLException e) {
             throw new HmsException(e);
         }
 
         return doctorList;
     }
     
     public static List<Physician> getNurses() throws HmsException {
         List<Physician> doctorList = new ArrayList<Physician>();
 
         final String query = "select * from Physician where role = 'Nurse'";
 
         try {
             DB.createConnection();
 
             ResultSet rs = DB.executeQuery(query);
 
             while (rs.next()) {
                 int id = rs.getInt("id");
                 Physician physician = PHYSICIAN_MAP.get(id);
                 
                 if (physician == null) {
                     boolean available = true;
                     if (rs.getInt("available") == 0) {
                         available = false;
                     }
                     physician = new Physician(rs.getString("name"), "Nurse", available);
                     physician.id = id;
                     
                     PHYSICIAN_MAP.put(id, physician);
                 }
                 
                 doctorList.add(physician);
             }
 
             DB.closeConnection();
         } catch (SQLException e) {
             throw new HmsException(e);
         }
 
         return doctorList;
     }
     
     public static Physician getPhysicianById(int id) throws HmsException {
         Physician physician = PHYSICIAN_MAP.get(id);
         if (physician != null) {
             return physician;
         }
         try {
             DB.createConnection();
 
            final String queryTempl = "SELECT * FROM Physician WHERE id = {0}";
             ResultSet rs = DB.executeQuery(MessageFormat.format(queryTempl, id));
 
             if (rs.next()) {
                 boolean available = true;
                 if (rs.getInt("available") == 0) {
                     available = false;
                 }
                 
                 physician = new Physician(rs.getString("name"), rs.getString("role"), available);
                 physician.id = rs.getInt("id");
             }
             PHYSICIAN_MAP.put(physician.getId(), physician);
             DB.closeConnection();
         } catch (SQLException e) {
             throw new HmsException(e);
         }
 
         return physician;
     }
     
     private int getNextFreeId() throws HmsException {
     	int freeId = 1;
 		try {
 			DB.createConnection();
 		
 		String query = "select max(id) as maxId from Physician";
 
 		ResultSet rs = DB.executeQuery(query);
 
 		if (rs.next()) {
 			freeId = rs.getInt("maxId") + 1;
 		}
 
 		DB.closeConnection();
 		} catch (SQLException e) {
 			throw new HmsException(e);
 		}
 
 		return freeId;
     }
     
 	public int updateStatus(String physician) throws SQLException {
 
         int result = -1;
         int physicianID = Integer.parseInt(physician);
         // Update physician avaiable
         DB.createConnection();
         
         String query = "UPDATE Physician" + " SET Avaiable = Not(Avaiable)"
                 + " WHERE PhysicianID = " + physicianID;
         result = DB.executeUpdate(query);
         DB.closeConnection();
 
         return result;
     }
 
 	public boolean isExist() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isDoctor() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isAvaiable(Date dateAffect) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getRole() {
 		return role;
 	}
 
 	public void setRole(String role) {
 		this.role = role;
 	}
 
 	public boolean isAvailable() {
 		return available;
 	}
 
 	public void setAvailable(boolean available) {
 		this.available = available;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 }
