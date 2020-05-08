 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.ResultSetExtractor;
 import org.springframework.jdbc.core.RowMapper;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 public class LegacyUserProvisioningRecordDao {
     private DataSource dataSource;
 
     public LegacyUserProvisioningRecordDao(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     @SuppressWarnings("unchecked")
     public List<LegacyUserProvisioningRecord> getAll() {
         JdbcTemplate template = new JdbcTemplate(dataSource);
        return template.query("select name, first_name, last_name, name as csm_group_name, name as site_name, name as study_name, active_flag from Users order by name", new PersonRowMapper());
     }
 
     public class PersonRowMapper implements RowMapper {
         public Object mapRow(ResultSet rs, int i) throws SQLException {
             LegacyUserProvisioningRecordResultSetExtractor extractor = new LegacyUserProvisioningRecordResultSetExtractor();
             return extractor.extractData(rs);
 
         }
 
     }
 
     public class LegacyUserProvisioningRecordResultSetExtractor implements ResultSetExtractor {
         public Object extractData(ResultSet rs) throws SQLException {
             return new LegacyUserProvisioningRecord(
                 rs.getString("name"),
                 rs.getString("first_name"),
                 rs.getString("last_name"),
                 rs.getString("name"),
                 rs.getString("name"),
                 rs.getString("name"),
                 rs.getString("active_flag")
             );
         }
 
     }
 
 
 }
