 package jewas.persistence.rowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author driccio
  */
 public class LongRowMapper implements RowMapper<Long> {
     @Override
     public Long processRow(ResultSet rs) throws SQLException {
        return rs.getLong(1);
     }
 }
