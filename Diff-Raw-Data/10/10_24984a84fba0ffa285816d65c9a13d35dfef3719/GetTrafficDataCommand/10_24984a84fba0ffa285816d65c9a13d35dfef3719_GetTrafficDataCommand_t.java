 package net.paguo.trafshow.backend.snmp.summary.commands;
 
 import net.paguo.trafshow.backend.snmp.summary.database.DBProxy;
 import net.paguo.trafshow.backend.snmp.summary.database.DBProxyFactory;
 import net.paguo.trafshow.backend.snmp.summary.model.TrafficCollector;
 import net.paguo.trafshow.backend.snmp.summary.model.TrafficRecord;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 
 public class GetTrafficDataCommand implements DatabaseCommand<TrafficCollector> {
     private static final String SQL = "select dt, cisco, interface, inoctets, outoctets, uptime from tr_with_uptime t where dt between ? and ? order by cisco, interface, dt";
     private static final Log log = LogFactory.getLog(GetTrafficDataCommand.class);
     private Date start;
     private Date end;
 
     public GetTrafficDataCommand(Date from, Date to) {
         this.start = from;
         this.end = to;
     }
 
     public TrafficCollector getData() {
         log.debug("getData(): <<<<");
         log.debug("Parameters are: " + start.toString() + " " + end.toString());
         DBProxy proxy = DBProxyFactory.getDBProxy();
         Connection conn = null;
         TrafficCollector collector = new TrafficCollector();
         try {
             conn = proxy.getConnection();
             PreparedStatement pst = conn.prepareStatement(SQL);
             pst.setTimestamp(1, new Timestamp(start.getTime()));
             pst.setTimestamp(2, new Timestamp(end.getTime()));
             ResultSet rs = pst.executeQuery();
             int processed = 0;
             while (rs.next()) {
                 TrafficRecord record = new TrafficRecord();
                 record.setDatetime(new Date(rs.getTimestamp(1).getTime()));
                 record.setRouter(rs.getString(2));
                 record.setIface(rs.getString(3));
                 record.setInput(rs.getLong(4));
                 record.setOutput(rs.getLong(5));
                 record.setUptime(rs.getLong(6));
                 collector.addTrafficRecord(record);
                 processed++;
             }
             log.debug(processed + " records processed");
             rs.close();
             pst.close();
         } catch (SQLException e) {
             log.error(e);
         } finally {
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     log.error("Error when closing connection", e);
                 }
             }
         }
        log.debug("Result size: " + collector.getTraffic().values().size());
         log.debug("getData(): >>>>");
         return collector;
     }
 }
