 package org.motechproject.ananya.kilkari.service;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.motechproject.ananya.kilkari.domain.report.SubscriptionStatusMeasure;
 import org.motechproject.ananya.kilkari.utils.TimedRunner;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 @Component
 public class ReportServiceAsync {
 
     private BasicDataSource dataSource;
 
 
     @Autowired
     public ReportServiceAsync(BasicDataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public List<SubscriptionStatusMeasure> getSubscriptionStatusMeasure() {
         return new TimedRunner(5, 1000) {
 
             @Override
             protected List<SubscriptionStatusMeasure> run() {
                 List<SubscriptionStatusMeasure> subscriptionStatusMeasures = null;
                 try {
                     String query = "select ssm.status,s.msisdn,spd.subscription_pack,cd.channel " +
                             "from report.subscription_status_measure ssm " +
                             "join report.subscriptions sc on sc.id = ssm.subscription_id " +
                             "join report.subscribers s on s.id=sc.subscriber_id " +
                             "join report.channel_dimension cd on cd.id = sc.channel_id " +
                             "join report.subscription_pack_dimension spd on spd.id = sc.subscription_pack_id";
                     subscriptionStatusMeasures = executeQuery(query);
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
 
                 return subscriptionStatusMeasures;
             }
 
             private List<SubscriptionStatusMeasure> executeQuery(String query) throws SQLException {
                 Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query);
 
                 try {
                     List<SubscriptionStatusMeasure> subscriptionStatusMeasures = new ArrayList<>();
                     while (resultSet.next()) {
                         String msisdn = resultSet.getString("msisdn");
                         String status = resultSet.getString("status");
                         String pack = resultSet.getString("subscription_pack");
                         String channel = resultSet.getString("channel");
                         subscriptionStatusMeasures.add(new SubscriptionStatusMeasure(msisdn, status, pack, channel));
                     }
                    return subscriptionStatusMeasures.size() == 2 ?  subscriptionStatusMeasures : null;
                 } finally {
                     resultSet.close();
                     statement.close();
                     connection.close();
                 }
             }
         }.executeWithTimeout();
     }
 }
