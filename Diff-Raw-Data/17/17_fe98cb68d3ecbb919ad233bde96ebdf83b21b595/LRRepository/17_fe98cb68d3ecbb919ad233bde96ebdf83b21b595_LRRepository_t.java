 package com.iway.jbpm.domain;
 
 import org.exoplatform.services.naming.InitialContextInitializer;
 
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
  * @date 5/19/13
  */
 public class LRRepository {
 
     private DataSource dataSource;
 
     private final Connection connection;
 
     private PreparedStatement createLRStatement;
 
     private PreparedStatement findAllLRsStatement;
 
     private PreparedStatement findLRsOfEmployeeStatement;
 
     private PreparedStatement updateLRStatement;
 
     public LRRepository(InitialContextInitializer ctxInitializer) throws Exception {
         InitialContext ctx = ctxInitializer.getInitialContext();
         dataSource = (DataSource) ctx.lookup("java:/iway_jbpm");
         connection = dataSource.getConnection();
         initStatements();
     }
 
     private void initStatements() throws Exception {
         findAllLRsStatement = connection.prepareStatement("SELECT * FROM leave_requests");
 
        String findLRsOfEmployeeSQL = "SELECT * FROM leave_requests WHERE username=?";
         findLRsOfEmployeeStatement = connection.prepareStatement(findLRsOfEmployeeSQL);
 
         String createSQL = "INSERT INTO leave_requests(instance_id, username,start_date,end_date, number_of_days,status) VALUES(?,?,?,?,?,?)";
         createLRStatement = connection.prepareStatement(createSQL);
 
        String updateSQL = "UPDATE leave_requests SET status=? WHERE username=? AND start_date=? AND end_date=? AND instance_id=?";
         updateLRStatement = connection.prepareStatement(updateSQL);
     }
 
     public synchronized void create(LeaveRequest lr) {
         try {
             createLRStatement.setLong(1, lr.instanceID);
             createLRStatement.setString(2, lr.username);
             createLRStatement.setString(3, lr.startDate);
             createLRStatement.setString(4, lr.endDate);
             createLRStatement.setInt(5, lr.numberOfDays);
             createLRStatement.setString(6, lr.status.toString());
 
             createLRStatement.executeUpdate();
         } catch (SQLException sqlEx) {
             sqlEx.printStackTrace();
         }
 
     }
 
     public synchronized void update(LeaveRequest lr) {
         try {
            updateLRStatement.setString(1, "'" + lr.status.toString() + "'");
            updateLRStatement.setString(2, "'" + lr.username + "'");
            updateLRStatement.setString(3, "'" + lr.startDate + "'");
            updateLRStatement.setString(4, "'" + lr.endDate + "'");
             updateLRStatement.setLong(5, lr.instanceID);
 
             updateLRStatement.executeUpdate();
         } catch (SQLException sqlEx) {
             sqlEx.printStackTrace();
         }
     }
 
     public synchronized void delete(LeaveRequest lr) {
     }
 
     public synchronized List<LeaveRequest> getLeaveRequests() {
         List<LeaveRequest> results = new LinkedList<LeaveRequest>();
         try {
             ResultSet resultSet = findAllLRsStatement.executeQuery();
             while (resultSet.next()) {
                 LeaveRequest lr = new LeaveRequest(resultSet.getLong("instance_id"), resultSet.getString("username"), resultSet.getString("start_date"), resultSet.getString("end_date"));
                 lr.status = LRStatus.valueOf(resultSet.getString("status"));
                 lr.numberOfDays = resultSet.getInt("number_of_days");
                 results.add(lr);
             }
         } catch (SQLException sqlEx) {
             sqlEx.printStackTrace();
         }
 
         return results;
     }
 
     public synchronized List<LeaveRequest> getLeaveRequestsOfEmployee(String employee) {
         List<LeaveRequest> results = new LinkedList<LeaveRequest>();
         try {
            findLRsOfEmployeeStatement.setString(1, "'" + employee + "'");
             ResultSet resultSet = findLRsOfEmployeeStatement.executeQuery();
             while (resultSet.next()) {
                 LeaveRequest lr = new LeaveRequest(resultSet.getLong("instance_id"), resultSet.getString("username"), resultSet.getString("start_date"), resultSet.getString("end_date"));
                 lr.status = LRStatus.valueOf(resultSet.getString("status"));
                 lr.numberOfDays = resultSet.getInt("number_of_days");
                 results.add(lr);
             }
         } catch (SQLException sqlEx) {
             sqlEx.printStackTrace();
         }
 
         return results;
     }
 
     public List<LeaveRequest> getLeaveRequests(LRFilter filter) {
         List<LeaveRequest> result = new LinkedList<LeaveRequest>();
         for (LeaveRequest lr : getLeaveRequests()) {
             if (filter.accept(lr)) {
                 result.add(lr);
             }
         }
         return result;
     }
 }
