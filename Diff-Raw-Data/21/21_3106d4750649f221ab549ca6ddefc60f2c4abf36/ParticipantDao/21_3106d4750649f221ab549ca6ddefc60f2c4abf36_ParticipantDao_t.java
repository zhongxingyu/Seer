 //Code inspired by http://danielniko.wordpress.com/2012/04/17/simple-crud-using-jsp-servlet-and-mysql/
 
 package com.ems.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 import com.ems.model.Participant;
 import com.ems.tools.Email;
 
 
 /**
 * ParticipantDao is the class that performs actions on the table Participant of the database
 * 
 * @author Luca Barazzuol
 */
 public class ParticipantDao {
 	
 	// commons logging references
 	static Logger log = Logger.getLogger(ParticipantDao.class.getName());
 
     /**
 	 * @uml.property  name="connection"
 	 */
     private Connection connection;
 
     /**
      * Constructor with a Connection as parameter
      * used by JUnit
      * 
      * @param  c A connection object used to access database by test units
      */
     public ParticipantDao(Connection c){
     	connection = c;
     }
     
     /**
      * Constructor with no parameters
      * It initializes the connection to the database
      * 
      */
     public ParticipantDao(){
         Context initialContext;
 		try {
 			initialContext = new InitialContext();
 	        Context envContext;
 			try {
 				envContext = (Context)initialContext.lookup("java:/comp/env");
 		        DataSource ds = (DataSource)envContext.lookup("jdbc/ems");
 		        connection = ds.getConnection();
 			} catch (NamingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     /**
      * Add a record to table 
      * 
      * @param user A user
      */
     public void addRecord(int anId_group, Participant aRecord) {
     	log.trace("START");
         try {
         	
             PreparedStatement preparedStatement = connection
                     .prepareStatement("insert into participant(id_group,fname,lname,email,uuid,date_of_birth,approved) " +
                     					"values (?, ?, ?, ?, ?, ?, ? )");
             preparedStatement.setInt(1, anId_group);
             preparedStatement.setString(2, aRecord.getFname());
             preparedStatement.setString(3, aRecord.getLname());
             preparedStatement.setString(4, aRecord.getEmail());
             preparedStatement.setString(5, UUID.randomUUID().toString());            
             preparedStatement.setString(6, aRecord.getDate_of_birth());
 //            preparedStatement.setString(7, aRecord.getRegistration_date());
             preparedStatement.setBoolean(7, false);
             //preparedStatement.setBoolean(9, false);
             log.debug(preparedStatement.toString());
         	log.debug("addRecord Execute Update");
             preparedStatement.executeUpdate();
             
             preparedStatement.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
     }
 
     /**
      * Delete a record using its id
      * 
      * @param id It is the id of the record to delete
      */
     public void deleteRecord(int id) {
     	log.trace("START");
         try {
             PreparedStatement preparedStatement = connection
                     .prepareStatement("delete from participant where id=?");
             preparedStatement.setInt(1, id);
             log.debug(preparedStatement.toString());
             preparedStatement.executeUpdate();
             
             preparedStatement.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
     }
 
     /**
      * Update the fields of a record
      * 
      * @param aRecord The record to update
      */
     public void updateRecord(Participant aRecord) {
     	log.debug("START");
         try {
             PreparedStatement preparedStatement = connection
                     .prepareStatement("update participant " +
                     					"set " +
                     					"id_group=?, " +
                     					"fname=?, " +
                     					"lname=?, " +
                     					"email=?, " +
                     					"date_of_birth=?," +
                       					"approved=?, " +
                     					"blocked=? " +
                             			"where id=?");
             log.debug(aRecord.getDate_of_birth()); 
             log.debug(aRecord.getRegistration_date());
             preparedStatement.setInt(1, aRecord.getId_group());
             preparedStatement.setString(2, aRecord.getFname());
             preparedStatement.setString(3, aRecord.getLname());
             preparedStatement.setString(4, aRecord.getEmail());            
             preparedStatement.setString(5, aRecord.getDate_of_birth());
 //            preparedStatement.setString(6, aRecord.getRegistration_date());
             preparedStatement.setBoolean(6, aRecord.isApproved());
             preparedStatement.setBoolean(7, aRecord.isBlocked());
             preparedStatement.setInt(8, aRecord.getId());
             preparedStatement.executeUpdate();
             
             preparedStatement.close();
 
             // email is sent only when the button Approve/Disapprove is clicked
 /*            Email e = new Email();
             if(aRecord.isApproved()){
             	e.sendEmail(aRecord.getEmail(), "Enrollment approved", "Congratulations - Your enrollment to the event has been Approved");
             }
             else{
             	e.sendEmail(aRecord.getEmail(), "Enrollment disapproved", "Your enrollment has been canceled");	
             }*/
             
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
     }
 
     /**
      * Returns the list of all records stored in table
      * 
      * @return List<User> List of objects Participant
      */
     public List<Participant> getAllRecords() {
         log.trace("START");
     	List<Participant> records = new ArrayList<Participant>();
         try {
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select * from participant");
             while (rs.next()) {
                 Participant record = new Participant();
                 record.setId(rs.getInt("id"));
                 record.setId_group(rs.getInt("id_group"));
                 record.setFname(rs.getString("fname"));
                 record.setLname(rs.getString("lname"));
                 record.setEmail(rs.getString("email"));
                 record.setUuid(rs.getString("uuid"));
                 record.setDate_of_birth(rs.getString("date_of_birth"));
                 record.setRegistration_date(rs.getString("registration_date"));
                 record.setApproved(rs.getBoolean("approved"));
                 record.setBlocked(rs.getBoolean("blocked"));
                 records.add(record);
             }
             rs.close();
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
             return records;
         }
     	log.trace("END");
         return records;
 
 
     }
     
     /**
      * Returns the list of all records stored in table associated with a group
      * 
      * @param anId_group
      * @return List<User> List of objects Participant
      */
     public List<Participant> getAllRecordsById_group(int anId_group) {
        log.debug("START");
     	List<Participant> records = new ArrayList<Participant>();
         try {
             PreparedStatement preparedStatement = connection.
                     prepareStatement("select * from participant where id_group=?");
             preparedStatement.setInt(1, anId_group);
             ResultSet rs = preparedStatement.executeQuery();
             
             while (rs.next()) {
                 Participant record = new Participant();
                 record.setId(rs.getInt("id"));
                 record.setId_group(rs.getInt("id_group"));
                 record.setFname(rs.getString("fname"));
                 record.setLname(rs.getString("lname"));
                 record.setEmail(rs.getString("email"));
                 record.setUuid(rs.getString("uuid"));
                 record.setDate_of_birth(rs.getString("date_of_birth"));
                 record.setRegistration_date(rs.getString("registration_date"));
                 record.setApproved(rs.getBoolean("approved"));
                 record.setBlocked(rs.getBoolean("blocked"));
                 records.add(record);
             }
             rs.close();
             preparedStatement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
         return records;
     }
 
     /**
      * Returns the list of all records stored in table associated with an event
      * 
      * @param anId_group
      * @return List<User> List of objects Participant
      */
     public List<Participant> getAllRecordsById_event(int anId_event) {
         log.trace("START");
     	List<Participant> records = new ArrayList<Participant>();
         try {
             PreparedStatement preparedStatement = connection.
                     prepareStatement("SELECT participant.*" +
                     				", DATE_FORMAT(participant.date_of_birth,'%d/%m/%Y') AS date_of_birth_FORMATTED" +                    		
                     				", DATE_FORMAT(participant.registration_date,'%d/%m/%Y %hh:%mm:%ss') AS registration_date_FORMATTED" +
                     				" FROM participant, ems.group, event" + 
                     				" WHERE participant.id_group = ems.group.id " +
                     				" AND ems.group.id_event = event.id " +
                     				" AND event.id = ?;");
             preparedStatement.setInt(1, anId_event);
             ResultSet rs = preparedStatement.executeQuery();
             
             while (rs.next()) {
                 Participant record = new Participant();
                 record.setId(rs.getInt("id"));
                 record.setId_group(rs.getInt("id_group"));
                 record.setFname(rs.getString("fname"));
                 record.setLname(rs.getString("lname"));
                 record.setEmail(rs.getString("email"));
                 record.setUuid(rs.getString("uuid"));
                 record.setDate_of_birth(rs.getString("date_of_birth_FORMATTED"));
                 record.setRegistration_date(rs.getString("registration_date_FORMATTED"));
                 record.setApproved(rs.getBoolean("approved"));
                 record.setBlocked(rs.getBoolean("blocked"));
                 records.add(record);
             }
             rs.close();
             preparedStatement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
         return records;
     }
     
     /**
      * Returns the record passing its id
      * 
      * @param id Identifier of the record to get
      * @return Participant An object Participant
      */
     public Participant getRecordById(int id) {
     	log.trace("START");
     	Participant record = new Participant();
         try {
             PreparedStatement preparedStatement = connection.
                     prepareStatement("select * from participant where id=?");
             preparedStatement.setInt(1, id);
             ResultSet rs = preparedStatement.executeQuery();
 
             if (rs.next()) {
                 record.setId(rs.getInt("id"));
                 record.setId_group(rs.getInt("id_group"));
                 record.setFname(rs.getString("fname"));
                 record.setLname(rs.getString("lname"));
                 record.setEmail(rs.getString("email"));
                 record.setUuid(rs.getString("uuid"));                
                 record.setDate_of_birth(rs.getString("date_of_birth"));
                 record.setRegistration_date(rs.getString("registration_date"));
                 record.setApproved(rs.getBoolean("approved"));
                 record.setBlocked(rs.getBoolean("blocked"));
             }
             rs.close();
             preparedStatement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
         return record;
     }
     
     /**
      * Approve a participant 
      * 
      * @param id an id of a participant
      */
     public void approve(int anId, boolean approved) {
     	log.trace("START");
     	PreparedStatement preparedStatement;
         try {
         	if(approved){
         		preparedStatement = connection
                     .prepareStatement("UPDATE participant SET approved=TRUE WHERE id =?;");
         	}
         	else{
         		preparedStatement = connection
                         .prepareStatement("UPDATE participant SET approved=FALSE WHERE id =?;");
         	}
             preparedStatement.setInt(1, anId);
             log.debug(preparedStatement.toString());
         	log.debug("addRecord Execute Update");
             preparedStatement.executeUpdate();
             
             preparedStatement.close();
             
             Email e = new Email();
             Participant p = getRecordById(anId);          
             if(approved){
             	e.sendEmail(p.getEmail(), "Enrollment approved", "Congratulations - Your enrollment to the event has been Approved");
             }
             else{
             	e.sendEmail(p.getEmail(), "Enrollment disapproved", "Your enrollment has been canceled");	
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
     }
     
     /**
      * Return a list of user that can modify the record identified by the passed id 
      * 
      * @param anId_participant an id of a participant
      * @return List<Integer> List of id
      */
     public List<Integer>  canBeChangedBy(int anId_participant) {
     	log.trace("START");
         List<Integer> listOfId = new ArrayList<Integer>();
         try {
         	
         	//look for group_referent
             PreparedStatement preparedStatement1 = connection
                     .prepareStatement("SELECT ems.group.id_group_referent, ems.group.id " +
                     					" FROM participant, ems.group" +
                     					" WHERE participant.id_group = ems.group.id" +
                     					" AND participant.id = ?");
             preparedStatement1.setInt(1, anId_participant);
             log.debug(preparedStatement1.toString());
             ResultSet rs1 = preparedStatement1.executeQuery();
 
             
             int id_group = 0;
 
             if (rs1.next()) {
                 listOfId.add(rs1.getInt("id_group_referent"));
                 log.debug("id_group_referent: " + rs1.getInt("id_group_referent"));
                 id_group = rs1.getInt("id");
                 log.debug("id_group: " + rs1.getInt("id"));
             }
 
             
             //look for id_event manager
             PreparedStatement preparedStatement2 = connection
                     .prepareStatement("SELECT event.id_manager " +
                     					" FROM ems.group, event" +
                     					" WHERE ems.group.id_event = event.id" +
                     					" AND ems.group.id = ?");
             preparedStatement2.setInt(1, id_group);
             log.debug(preparedStatement2.toString());
             ResultSet rs2 = preparedStatement2.executeQuery();
             if (rs2.next()) {
                 listOfId.add(rs2.getInt("id_manager"));
                 log.debug("id_manager: " + rs2.getInt("id_manager"));
             }
 
             
             //look for admins
             PreparedStatement preparedStatement3= connection
                     .prepareStatement("SELECT id " +
                     					" FROM ems.user, ems.user_role" +
                     					" WHERE ems.user.email = ems.user_role.email" +
                     					" AND ems.user_role.ROLE_NAME = 'admin'");
             log.debug(preparedStatement3.toString());
             ResultSet rs3 = preparedStatement3.executeQuery();
             while (rs3.next()) {
             	log.debug("there are admins");
                 listOfId.add(rs3.getInt("id"));
                 log.debug("id_admin: " + rs3.getInt("id"));
             }
             
             for (int  i = 0; i < listOfId.size(); i++){
             	log.debug("listOfId[" + i + "]: " + listOfId.get(i));
             }
             
             rs1.close();
             rs2.close();
             rs3.close();
             preparedStatement1.close();
             preparedStatement2.close();
             preparedStatement3.close();
             
         } catch (SQLException e) {
             e.printStackTrace();
         }
     	log.trace("END");
     	return listOfId;
     }
 }
