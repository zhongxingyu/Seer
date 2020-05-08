 package edacc.model;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.util.Hashtable;
 import java.util.Vector;
 
 public class ExperimentResultDAO {
     protected static final String table = "ExperimentResults";
     protected static final String insertQuery = "INSERT INTO " + table + " (run, status, seed, resultFileName, time, statusCode, " +
                                                 "SolverConfig_idSolverConfig, Experiment_idExperiment, Instances_idInstance) VALUES (?,?,?,?,?,?,?,?,?)";
     protected static final String deleteQuery = "DELETE FROM " + table + " WHERE idJob=?";
     private static final Hashtable<ExperimentResult, ExperimentResult> cache = new Hashtable<ExperimentResult, ExperimentResult>();
 
     public static ExperimentResult createExperimentResult(int run, int status, int seed, String resultFileName, float time, int statusCode, int SolverConfigId, int ExperimentId, int InstanceId) throws SQLException {
         ExperimentResult r = new ExperimentResult(run, status, seed, resultFileName, time, statusCode, SolverConfigId, ExperimentId, InstanceId);
         r.setNew();
         save(r);
         cacheExperimentResult(r);
         return r;
     }
 
     private ExperimentResult getExperimentResultFromResultSet(ResultSet rs) throws SQLException {
         ExperimentResult r = new ExperimentResult();
         r.setId(rs.getInt("idJob"));
         r.setRun(rs.getInt("run"));
         r.setStatus(rs.getInt("status"));
         r.setSeed(rs.getInt("seed"));
         r.setResultFileName(rs.getString("resultFileName"));
         r.setTime(rs.getFloat("time"));
         r.setStatusCode(rs.getInt("statusCode"));
         r.setSolverConfigId(rs.getInt("SolverConfig_idSolverConfig"));
         r.setInstanceId(rs.getInt("Instances_idInstance"));
         r.setExperimentId(rs.getInt("Experiment_idExperiment"));
         return r;
     }
 
     private static void save(ExperimentResult r) throws SQLException {
         if (r.isNew()) {
             PreparedStatement st = DatabaseConnector.getInstance().conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(2, r.getRun());
            st.setInt(3, r.getStatus());
            st.setInt(4, r.getSeed());
            st.setString(5, r.getResultFileName());
            st.setFloat(6, r.getTime());
            st.setInt(7, r.getStatusCode());
            st.setInt(8, r.getSolverConfigId());
            st.setInt(9, r.getExperimentId());
            st.setInt(10, r.getInstanceId());
             st.executeUpdate();
             ResultSet generatedKeys = st.getGeneratedKeys();
             if (generatedKeys.next()) {
                 r.setId(generatedKeys.getInt(1));
             }
             r.setSaved();
         }
         else if (r.isModified()) {
             // TODO: implement if needed
         }
 
     }
 
     private static ExperimentResult getCached(ExperimentResult i) {
         if (cache.containsKey(i)) {
             return cache.get(i);
         } else {
             return null;
         }
     }
 
     private static void cacheExperimentResult(ExperimentResult i) {
         if (cache.containsKey(i)) {
             return;
         } else {
             cache.put(i, i);
         }
     }
 
     public Vector<ExperimentResult> getAllByExperimentId(int id) throws SQLException {
         Vector<ExperimentResult> v = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().conn.prepareStatement("SELECT * FROM " + table + " WHERE Experiment_idExperiment");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult i = getExperimentResultFromResultSet(rs);
             
             ExperimentResult c = getCached(i);
             if (c != null) {
                 v.add(c);
             } else {
                 i.setSaved();
                 cacheExperimentResult(i);
                 v.add(i);
             }
         }
 
         return v;
     }
 
     
 }
