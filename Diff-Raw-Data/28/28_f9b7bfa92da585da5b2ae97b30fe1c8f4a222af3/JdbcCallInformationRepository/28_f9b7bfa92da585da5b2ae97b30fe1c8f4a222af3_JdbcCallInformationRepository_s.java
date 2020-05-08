 package dk.drb.blacktiger.repository.jdbc;
 
 import dk.drb.blacktiger.repository.CallInformationRepository;
 import dk.drb.blacktiger.model.CallInformation;
import dk.drb.blacktiger.util.Phonenumber;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import javax.sql.DataSource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 /**
  *
  */
 public class JdbcCallInformationRepository implements CallInformationRepository {
 
     private JdbcTemplate jdbcTemplate;
     private CallInformationMapper mapper = new CallInformationMapper();
 
     private class CallInformationMapper implements RowMapper<CallInformation> {
 
         @Override
         public CallInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
            String phoneNumber = Phonenumber.normalize(rs.getString("phoneNumber"));
             return new CallInformation(phoneNumber, null, rs.getInt("numberOfCalls"),
                     rs.getInt("totalDuration"), rs.getTimestamp("firstCallTimestamp"));
         }
     }
 
     public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public List<CallInformation> findByRoomNoAndPeriodAndDuration(String roomNo, Date start, Date end, int minimumDuration) {
         String roomSearch = "%" + roomNo;
         String sql = "SELECT count(*) as numberOfCalls,sum(billsec) as totalDuration, min(calldate) as firstCallTimeStamp,src as phoneNumber "
                 + "FROM cdr where calldate > ? and calldate < ? and dcontext LIKE ? group by src having totalDuration > ? ORDER BY firstCallTimeStamp";
         return this.jdbcTemplate.query(sql, new Object[]{start, end, roomSearch, minimumDuration}, mapper);
     }
     
     
 }
