 package org.motechproject.ananya.reports.smoke.kilkari.repository;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.motechproject.ananya.reports.smoke.TimedRunner;
 import org.motechproject.ananya.reports.smoke.kilkari.repository.domain.SubscriptionStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Component;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 @Component
 public class ReportingService {
 
     private final JdbcTemplate jdbcTemplate;
 
     @Autowired
     public ReportingService(BasicDataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public List<SubscriptionStatus> getSubscriptionStatusMeasureForMsisdn(final String msisdn) {
         return new TimedRunner<List<SubscriptionStatus>>(10, 1000) {
             @Override
             protected List<SubscriptionStatus> run() {
                 List<SubscriptionStatus> subscriptionStatuses;
                String query = "select ssm.status,s.msisdn,s.name,s.age_of_beneficiary,s.estimated_date_of_delivery,s.date_of_birth,spd.subscription_pack,cd.channel " +
                         "from report.subscription_status_measure ssm " +
                         "join report.subscriptions sc on sc.id = ssm.subscription_id " +
                         "join report.subscribers s on s.id=sc.subscriber_id " +
                         "join report.channel_dimension cd on cd.id = sc.channel_id " +
                         "join report.subscription_pack_dimension spd on spd.id = sc.subscription_pack_id where msisdn = " + msisdn;
                 subscriptionStatuses = executeQuery(query);
 
                 return subscriptionStatuses;
             }
         }.executeWithTimeout();
     }
 
     public void createNewLocation(String district, String block, String panchayat) throws SQLException {
         jdbcTemplate.execute("insert into report.location_dimension(district,block,panchayat) values ('" + district + "','" + block + "','" + panchayat + "')");
     }
 
 
     public void deleteAll() {
         jdbcTemplate.execute("delete from report.subscription_status_measure");
         jdbcTemplate.execute("delete from report.subscriptions");
         jdbcTemplate.execute("delete from report.subscribers");
         jdbcTemplate.execute("delete from report.location_dimension");
     }
 
     private List<SubscriptionStatus> executeQuery(String query) {
         List<SubscriptionStatus> subscriptionStatuses = jdbcTemplate.query(query, new RowMapper<SubscriptionStatus>() {
             @Override
             public SubscriptionStatus mapRow(ResultSet resultSet, int i) throws SQLException {
                 String msisdn = resultSet.getString("msisdn");
                 String status = resultSet.getString("status");
                 String pack = resultSet.getString("subscription_pack");
                 String channel = resultSet.getString("channel");
                 String name = resultSet.getString("name");
                 String age = resultSet.getString("age_of_beneficiary");
                 String estimatedDateOfDelivery = resultSet.getString("estimated_date_of_delivery");
                 String dateOfBirth = resultSet.getString("date_of_birth");
                 return new SubscriptionStatus(msisdn, status, pack, channel, name, age, estimatedDateOfDelivery, dateOfBirth);
             }
         });
         return subscriptionStatuses.size() > 0 ? subscriptionStatuses : null;
     }
 }
