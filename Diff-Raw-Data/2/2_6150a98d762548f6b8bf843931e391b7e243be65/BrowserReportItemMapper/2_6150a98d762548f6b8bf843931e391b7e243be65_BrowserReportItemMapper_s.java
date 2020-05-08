 package org.woehlke.logfileloader.core.dao.rowmapper;
 
 import org.springframework.jdbc.core.RowMapper;
 import org.woehlke.logfileloader.core.model.BrowserReportItem;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tw
  * Date: 04.09.13
  * Time: 13:42
  * To change this template use File | Settings | File Templates.
  */
 public class BrowserReportItemMapper implements RowMapper<BrowserReportItem> {
 
     @Override
     public BrowserReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
         BrowserReportItem b = new BrowserReportItem();
         b.setBrowser(rs.getString("browser"));
        b.setNr(rs.getShort("nr"));
         b.setId(rs.getLong("id"));
         return b;
     }
 }
