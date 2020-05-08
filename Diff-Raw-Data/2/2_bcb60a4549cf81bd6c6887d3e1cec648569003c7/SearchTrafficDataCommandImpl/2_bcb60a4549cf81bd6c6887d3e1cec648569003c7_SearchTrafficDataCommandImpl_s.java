 package net.paguo.trafshow.backend.snmp.summary.commands.impl;
 
 import net.paguo.trafshow.backend.snmp.summary.database.DBProxy;
 import net.paguo.trafshow.backend.snmp.summary.database.DBProxyFactory;
 import net.paguo.trafshow.backend.snmp.summary.model.RouterSummaryTraffic;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author Reyentenko
  */
 public class SearchTrafficDataCommandImpl {
     private static final Log log = LogFactory.getLog(SearchTrafficDataCommandImpl.class);
     private Connection connection;
 
     public SearchTrafficDataCommandImpl() {
     }
 
 
     public final Long findRecordId(RouterSummaryTraffic record) {
         if (connection == null) {
             openConnection();
         }
         Long result = null;
         try {
             PreparedStatement pst = connection.prepareStatement("select a_id from aggreg where cisco = ? and" +
                    " iface = ? and date = ?");
             pst.setString(1, record.getRouter());
             pst.setString(2, record.getIface());
             pst.setDate(3, new java.sql.Date(record.getDate().getTime()));
             ResultSet rs = pst.executeQuery();
             if (rs.next()) {
                 result = rs.getLong(1);
             }
             rs.close();
             pst.close();
         } catch (SQLException e) {
             log.error(e);
         }
         return result;
     }
 
     private void openConnection() {
         DBProxy proxy = DBProxyFactory.getDBProxy();
         try {
             connection = proxy.getConnection();
         } catch (SQLException e) {
             log.error(e);
         }
     }
 
     public void closeConnection() {
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException e) {
                 log.error(e);
             }
         }
     }
 }
