 package info.bytecraft.database.db;
 
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import info.bytecraft.database.IReportDAO;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.PlayerReport;
 import info.bytecraft.database.DAOException;
 
 public class DBReportDAO implements IReportDAO
 {
     private Connection conn;
 
     public DBReportDAO(Connection conn)
     {
         this.conn = conn;
     }
 
     @Override
     public List<PlayerReport> getReports(BytecraftPlayer player)
             throws DAOException
     {
         String sql = "SELECT * FROM player_report ";
         sql += "WHERE subject_name = ? ";
         sql += "ORDER BY report_timestamp DESC";
 
         List<PlayerReport> reports = new ArrayList<PlayerReport>();
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player.getId());
             stmt.execute();
 
             try (ResultSet rs = stmt.getResultSet()) {
                 while (rs.next()) {
                     PlayerReport report = new PlayerReport();
                     report.setId(rs.getInt("report_id"));
                     report.setSubjectName(rs.getString("subject_name"));
                     report.setIssuerName(rs.getString("issuer_name"));
                     report.setAction(PlayerReport.Action.fromString(rs
                             .getString("report_action")));
                     report.setMessage(rs.getString("report_message"));
                     report.setTimestamp(new Date(
                             rs.getInt("report_timestamp") * 1000l));
 
                     int validUntil = rs.getInt("report_validuntil");
                     if (validUntil != 0) {
                         report.setValidUntil(new Date(validUntil * 1000l));
                     }
 
                     reports.add(report);
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
 
         return reports;
     }
 
     @Override
     public void insertReport(PlayerReport report) throws DAOException
     {
         String sql =
                 "INSERT INTO player_report (subject_name, issuer_name, "
                         + "report_action, report_message, report_timestamp, report_validuntil) ";
         sql += "VALUES (?, ?, ?, ?, ?, ?)";
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
 
             stmt.setString(1, report.getSubjectName());
             stmt.setString(2, report.getIssuerName());
             PlayerReport.Action action = report.getAction();
             stmt.setString(3, action.toString());
             stmt.setString(4, report.getMessage());
             Date timestamp = report.getTimestamp();
             stmt.setLong(5, timestamp.getTime() / 1000l);
             Date validUntil = report.getValidUntil();
             stmt.setLong(6, validUntil != null ? validUntil.getTime() / 1000l
                     : 0);
             stmt.execute();
 
             stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
             try (ResultSet rs = stmt.getResultSet()) {
                 if (rs.next()) {
                     report.setId(rs.getInt(1));
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
 }
