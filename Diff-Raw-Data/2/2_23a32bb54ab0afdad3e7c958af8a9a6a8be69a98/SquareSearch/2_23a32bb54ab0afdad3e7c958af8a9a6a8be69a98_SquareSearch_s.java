 package ru.spbau.bioinf.evalue;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.Scan;
 
 public class SquareSearch {
 
     private static Logger log = Logger.getLogger(SquareSearch.class);
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         EValueServer.init(args);
         Map<Integer,Scan> scans = conf.getScans();
         DbUtil.initDatabase();
         Connection con = DbUtil.getConnection();
         PreparedStatement ps = null;
         ResultSet rs = null;
         List<Integer> proteinIds = new ArrayList<Integer>();
         try {
             ps = con.prepareStatement("select protein_id from t_status where evalue < 0.0024 group by protein_id order by count(*)");
             rs = ps.executeQuery();
             while (rs.next()) {
                 proteinIds.add(rs.getInt(1));
             }
         } catch (SQLException e) {
             log.error("Error loading proteins from database", e);
             throw new RuntimeException(e);
         } finally {
             DbUtil.close(con, ps, rs);
         }
 
         for (Integer proteinId : proteinIds) {
            log.debug("Processing protein " + proteinId);
             for (Integer scanId : scans.keySet()) {
                 EValueServer.getEvalue(scanId, proteinId);
             }
         }
     }
 }
