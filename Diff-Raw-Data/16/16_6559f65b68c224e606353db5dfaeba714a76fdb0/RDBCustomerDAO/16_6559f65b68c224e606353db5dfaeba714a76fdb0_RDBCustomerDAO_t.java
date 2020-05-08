 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package EJB_Data_Access;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  *
  * @author JJ & Baron & Marcel
  */
 public class RDBCustomerDAO implements CustomerDAO, Serializable{
 
     private Connection dbConnection = null;
     
     public RDBCustomerDAO(Connection connection) {
         this.dbConnection = connection;
     }
     
     @Override
     public void createCustomer(Customer customer) {
         try {
             PreparedStatement sqlStatement;
             
             sqlStatement = dbConnection.prepareStatement(
                 "INSERT INTO ACMEDB.CUSTOMER(FIRST_NAME, LAST_NAME, DOB, ADDRESS)"
                 +"VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
             
             sqlStatement.setString(1, customer.getFirstName());
             sqlStatement.setString(2, customer.getLastName());
             sqlStatement.setDate(3, (Date)customer.getDOB());
             sqlStatement.setString(4, customer.getAddress());
             
            sqlStatement.executeUpdate();
             
             ResultSet result = sqlStatement.getGeneratedKeys();
             result.next();
             customer.setCID(result.getInt(1));
         }
         catch(SQLException sqle) {
             System.out.println("Customer creation failed.");
         }
     }
     
     @Override
     public Customer readCustomer(int c_id) {
         try {
             PreparedStatement sqlStatement = dbConnection.prepareStatement(
                     "SELECT * FROM ACMEDB.CUSTOMER WHERE C_ID="+c_id);
             
             sqlStatement.executeQuery();
             
             ResultSet result = sqlStatement.getResultSet();
             
             result.next();
             
             int cust_id = result.getInt("C_ID");
             String first_name = result.getString("FIRST_NAME");
             String last_name = result.getString("LAST_NAME");
             Date dob = result.getDate("DOB");
             String address = result.getString("ADDRESS");
             
             Customer customer = new Customer(cust_id, first_name, last_name, dob, address);
             
             return customer;
         }
         catch(SQLException sqle) {
             System.out.println("Customer reading failed.");
         }
         
         return null;
     }
     
     @Override
     public void updateCustomer(Customer customer) {
         try {
             PreparedStatement sqlStatement = dbConnection.prepareStatement(
                     "UPDATE ACMEDB.CUSTOMER"+
                     "SET FIRST_NAME = ?, LAST_NAME = ?, DOB = ?, ADDRESS = ?"+
                     "WHERE C_ID = "+customer.getCID());
             
             sqlStatement.setString(1, customer.getFirstName());
             sqlStatement.setString(2, customer.getLastName());
             sqlStatement.setDate(3, (Date) customer.getDOB());
             sqlStatement.setString(4, customer.getAddress());
             
             sqlStatement.executeUpdate();
             
         }
         catch(SQLException sqle) {
             System.out.println("Customer updating failed.");
         }
     }
     
     @Override
     public void deleteCustomer(Customer customer) {
         try {
             PreparedStatement sqlStatement = dbConnection.prepareStatement(
                     "DELETE FROM ACMEDB.CUSTOMER"+
                     "WHERE C_ID = "+customer.getCID());
             
             sqlStatement.executeUpdate();
         }
         catch(SQLException sqle) {
             System.out.println("Customer deleting failed.");
         }
     }
     
     @Override
     public ResultSet getAllCustomers()
     {
        try
         {   
            PreparedStatement sqlStatement;
            sqlStatement = dbConnection.prepareStatement(
            "SELECT * FROM ACMEDB.CUSTOMER");
            
            sqlStatement.executeQuery();
            
            ResultSet rs = sqlStatement.getResultSet();
            
            return rs;
         }
         catch (SQLException sqlException) 
         { 
            System.out.println("Could not find any customer.");
         } 
        
         return null;
     }
     
 }
