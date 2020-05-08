 package edacc.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.util.Vector;
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 public class ExperimentResultDAO {
 
     protected static PreparedStatement curSt = null;
     protected static final String table = "ExperimentResults";
     protected static final String insertQuery = "INSERT INTO " + table + " (SolverConfig_idSolverConfig, Experiment_idExperiment," +
             "Instances_idInstance, run, status, seed, solverOutputFN, launcherOutputFN, watcherOutputFN, verifierOutputFN) VALUES (?,?,?,?,?,?,?,?,?,?)";
     protected static final String deleteQuery = "DELETE FROM " + table + " WHERE idJob=?";
     protected static final String selectQuery = "SELECT SolverConfig_idSolverConfig, Experiment_idExperiment, Instances_idInstance, " +
             "idJob, run, seed, status, resultTime, resultCode, solverOutputFN, launcherOutputFN, watcherOutputFN, verifierOutputFN, " +
            "solverExitCode, watcherExitCode, verifierExitCode, computeQueue, TIMEDIFF(curTime(), startTime) AS runningTime " +
             "FROM " + table + " ";
 
     public static ExperimentResult createExperimentResult(int run, int status, int seed, float time, int SolverConfigId, int ExperimentId, int InstanceId) throws SQLException {
         ExperimentResult r = new ExperimentResult(run, status, seed, time, SolverConfigId, ExperimentId, InstanceId);
         r.setNew();
         return r;
     }
 
     public static void batchSave(Vector<ExperimentResult> v) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery);
             curSt = st;
             for (ExperimentResult r : v) {
                 st.setInt(1, r.getSolverConfigId());
                 st.setInt(2, r.getExperimentId());
                 st.setInt(3, r.getInstanceId());
                 st.setInt(4, r.getRun());
                 st.setInt(5, r.getStatus().getValue());
                 st.setInt(6, r.getSeed());
                 st.setString(7, r.getSolverOutputFilename());
                 st.setString(8, r.getLauncherOutputFilename());
                 st.setString(9, r.getWatcherOutputFilename());
                 st.setString(10, r.getVerifierOutputFilename());
                 st.addBatch();
                 r.setSaved();
                 /* this should only be done if the batch save actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
 
     }
 
     /**
      * Updates the run property of the ExperimentResults at once (batch).
      * @param v vector of ExperimentResults to be updated
      * @throws SQLException
      */
     public static void batchUpdateRun(Vector<ExperimentResult> v) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             final String query = "UPDATE " + table + " SET run=? WHERE idJob=?";
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query);
             curSt = st;
             for (ExperimentResult r : v) {
                 st.setInt(1, r.getRun());
                 st.setInt(2, r.getId());
                 st.addBatch();
                 r.setSaved();
                 /* this should only be done if the batch update actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
     }
 
     /**
      * Deletes all experiment results at once (batch).
      * @param experimentResults the experiment results to be deleted
      * @throws SQLException
      */
     public static void deleteExperimentResults(Vector<ExperimentResult> experimentResults) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(deleteQuery);
             curSt = st;
             for (ExperimentResult r : experimentResults) {
                 st.setInt(1, r.getId());
                 st.addBatch();
                 r.setDeleted();
                 /* this should only be done if the batch delete actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
     }
 
     private static ExperimentResult getExperimentResultFromResultSet(ResultSet rs) throws SQLException {
         ExperimentResult r = new ExperimentResult();
         r.setSolverConfigId(rs.getInt("SolverConfig_idSolverConfig"));
         r.setInstanceId(rs.getInt("Instances_idInstance"));
         r.setExperimentId(rs.getInt("Experiment_idExperiment"));
         r.setId(rs.getInt("idJob"));
         r.setRun(rs.getInt("run"));
         r.setSeed(rs.getInt("seed"));
         r.setStatus(rs.getInt("status"));
         r.setResultTime(rs.getFloat("resultTime"));
         Integer resultCode = rs.getInt("resultCode");
         if (resultCode == null) resultCode = -1;
         r.setResultCode(resultCode);
         r.setSolverOutputFilename(rs.getString("solverOutputFN"));
         r.setLauncherOutputFilename(rs.getString("launcherOutputFN"));
         r.setWatcherOutputFilename(rs.getString("watcherOutputFN"));
         r.setVerifierOutputFilename(rs.getString("verifierOutputFN"));
         r.setSolverExitCode(rs.getInt("solverExitCode"));
         r.setWatcherExitCode(rs.getInt("watcherExitCode"));
         r.setVerifierExitCode(rs.getInt("verifierExitCode"));
         r.setComputeQueue(rs.getInt("computeQueue"));
 
         if (r.getStatus() == ExperimentResultStatus.RUNNING) {
             try {
                 r.setRunningTime(rs.getTime("runningTime"));
             } catch (Exception e) {
                 r.setRunningTime(null);
             }
         } else {
             r.setRunningTime(null);
         }
         return r;
     }
 
     /**
      * returns the number of jobs in the database for the given experiment
      * @param id experiment id
      * @return
      * @throws SQLException
      */
     public static int getCountByExperimentId(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT COUNT(*) as count FROM " + table + " WHERE Experiment_idExperiment=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         rs.next(); // there will always be a count
         int count = rs.getInt("count");
         rs.close();
         return count;
     }
 
     /**
      * checks the database if a job with the given parameters already exists
      * @param run
      * @param solverConfigId
      * @param InstanceId
      * @param ExperimentId
      * @return bool
      * @throws SQLException
      */
     public static boolean jobExists(int run, int solverConfigId, int InstanceId, int ExperimentId) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT COUNT(*) as count FROM " + table + " " +
                 "WHERE run=? AND SolverConfig_idSolverConfig=? AND Instances_idInstance=? AND Experiment_idExperiment=? ;");
         st.setInt(1, run);
         st.setInt(2, solverConfigId);
         st.setInt(3, InstanceId);
         st.setInt(4, ExperimentId);
         ResultSet rs = st.executeQuery();
         rs.next();
         int count = rs.getInt("count");
         rs.close();
         return count > 0;
     }
 
     /**
      * returns the seed value of the job specified by the given parameters
      * @param run
      * @param solverConfigId
      * @param InstanceId
      * @param ExperimentId
      * @return bool
      * @throws SQLException
      */
     public static int getSeedValue(int run, int solverConfigId, int InstanceId, int ExperimentId) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT seed FROM " + table + " " +
                 "WHERE run=? AND SolverConfig_idSolverConfig=? AND Instances_idInstance=? AND Experiment_idExperiment=? ;");
         st.setInt(1, run);
         st.setInt(2, solverConfigId);
         st.setInt(3, InstanceId);
         st.setInt(4, ExperimentId);
         ResultSet rs = st.executeQuery();
         rs.next();
         int seed = rs.getInt("seed");
         rs.close();
         return seed;
     }
 
     /**
      * returns all jobs of the given Experiment
      * @param id
      * @return ExperimentResults vector
      * @throws SQLException
      */
     public static Vector<ExperimentResult> getAllByExperimentId(int id) throws SQLException {
         Vector<ExperimentResult> v = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery +
                 "WHERE Experiment_idExperiment=?;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             v.add(er);
             er.setSaved();
         }
         rs.close();
         st.close();
         return v;
     }
 
     /**
      * Returns all runs for an experiment specified by id
      * @param id the experiment id
      * @return vector of run numbers
      * @throws SQLException
      */
     public static Vector<Integer> getAllRunsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT run " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY run ORDER BY run;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     /**
      * Returns all instance ids which have jobs in the database and are associated
      * with the experiment specified by id
      * @param id the experiment id
      * @return vector of solver config ids
      * @throws SQLException
      */
     public static Vector<Integer> getAllInstanceIdsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT Instances_idInstance " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY Instances_idInstance ORDER BY Instances_idInstance;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     /**
      * Returns all solver config ids which have jobs in the database and are associated
      * with the experiment specified by id.
      * @param id the experiment id
      * @return vector of solver config ids
      * @throws SQLException
      */
     public static Vector<Integer> getAllSolverConfigIdsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT SolverConfig_idSolverConfig " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY SolverConfig_idSolverConfig ORDER BY SolverConfig_idSolverConfig;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     public static Vector<ExperimentResult> getAllByExperimentIdAndRun(int eid, int run) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery + "WHERE Experiment_idExperiment=? AND run=?;");
         st.setInt(1, eid);
         st.setInt(2, run);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static int getInstanceCountByExperimentId(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT COUNT(*) FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? " +
                 "GROUP BY Instance_idInstance");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         if (rs.next()) {
             return rs.getInt(1);
         }
         return 0;
     }
 
     public static int getSolverConfigCountByExperimentId(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT COUNT(*) FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? " +
                 "GROUP BY SolverConfig_idSolverConfig");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         if (rs.next()) {
             return rs.getInt(1);
         }
         return 0;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfiguration(SolverConfiguration sc) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery + "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=?;");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfigurationAndStatus(SolverConfiguration sc, int status) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery + "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=? AND status=?;");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         st.setInt(3, status);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static double getMaxCalculationTimeForSolverConfiguration(SolverConfiguration sc, int status, int run) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT MAX(time) " +
                 "FROM " + table + " " +
                 "WHERE SolverConfig_idSolverConfig=? AND status=? AND run=?;");
         st.setInt(1, sc.getId());
         st.setInt(2, status);
         st.setInt(3, run);
         ResultSet rs = st.executeQuery();
         if (rs.next()) {
             return rs.getDouble(1);
         } else {
             return 0.;
         }
     }
 
     public static Vector<ExperimentResult> getAllByExperimentHasInstance(ExperimentHasInstance ehi) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery + "WHERE Experiment_idExperiment=? AND Instances_idInstance=?;");
         st.setInt(1, ehi.getExperiment_id());
         st.setInt(2, ehi.getInstances_id());
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfigurationAndRunAndStatusOrderByTime(SolverConfiguration sc, int run, int status) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery +
                 "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=? AND status=? AND run=? " +
                 "ORDER BY time");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         st.setInt(3, status);
         st.setInt(4, run);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static void setAutoCommit(boolean commit) throws SQLException {
         DatabaseConnector.getInstance().getConn().setAutoCommit(commit);
     }
 
     public static void cancelStatement() throws SQLException {
         if (curSt != null) {
             try {
                 curSt.cancel();
             } catch (Exception _) {
             }
         }
     }
 
     /**
      *
      * @param id of the requested ExperimentResult
      * @return the ExperimentResult object with the given id
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws ExperimentResultNotInDBException
      * @author rretz
      */
     public static ExperimentResult getById(int id) throws NoConnectionToDBException, SQLException, ExperimentResultNotInDBException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 selectQuery +
                 "WHERE idJob=?;");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (!rs.next()) {
             throw new ExperimentResultNotInDBException();
         }
         return getExperimentResultFromResultSet(rs);
     }
 
     /**
      * Copies the binary file of the client output of a ExperimentResult to a temporary location on the file system and retuns a File
      * reference on it.
      * @param expRes ExperimentResult from which the binary file is copied
      * @return reference of the binary file of the clinet output of the given ExperimentResult
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static File getSolverOutputFile(ExperimentResult expRes) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         File f = new File("tmp" + System.getProperty("file.separator") + expRes.getId() + "_" + expRes.getSolverOutputFilename());
         // create missing directories
         f.getParentFile().mkdirs();
         getSolverOutput(expRes.getId(), f);
         return f;
     }
 
     /**
      * Copies the binary file of the launcher file of a ExperimentResult to a temporary location on the file system and retuns a File
      * reference on it.
      * @param expRes expRes ExperimentResult from which the binary file is copied
      * @return reference of the binary file of the result file of the given ExperimentResult
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static File getLauncherOutputFile(ExperimentResult expRes) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         File f = new File("tmp" + System.getProperty("file.separator") + expRes.getId() + "_" + expRes.getLauncherOutputFilename());
         // create missing directories
         f.getParentFile().mkdirs();
         getLauncherOutput(expRes.getId(), f);
         return f;
     }
 
     /**
      * Copies the binary file of the verifier file of a ExperimentResult to a temporary location on the file system and retuns a File
      * reference on it.
      * @param expRes expRes ExperimentResult from which the binary file is copied
      * @return reference of the binary file of the result file of the given ExperimentResult
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static File getVerifierOutputFile(ExperimentResult expRes) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         File f = new File("tmp" + System.getProperty("file.separator") + expRes.getId() + "_" + expRes.getVerifierOutputFilename());
         // create missing directories
         f.getParentFile().mkdirs();
         getVerifierOutput(expRes.getId(), f);
         return f;
     }
 
     /**
      * Copies the binary file of the watcher file of a ExperimentResult to a temporary location on the file system and retuns a File
      * reference on it.
      * @param expRes expRes ExperimentResult from which the binary file is copied
      * @return reference of the binary file of the result file of the given ExperimentResult
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static File getWatcherOutputFile(ExperimentResult expRes) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         File f = new File("tmp" + System.getProperty("file.separator") + expRes.getId() + "_" + expRes.getWatcherOutputFilename());
         // create missing directories
         f.getParentFile().mkdirs();
         getWatcherOutput(expRes.getId(), f);
         return f;
     }
 
     /**
      * Copies the binary file of a result file of an ExperimentResult to a specified location on the filesystem.
      * @param id the id of the ExperimentResult
      * @param f the file in which the binary file is copied
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static void getSolverOutput(int id, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT solverOutput " +
                 "FROM " + table + " " +
                 "WHERE idJob=?;");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             FileOutputStream out = new FileOutputStream(f);
             InputStream in = rs.getBinaryStream("solverOutput");
             int len;
             byte[] buf = new byte[256 * 1024];
             while ((len = in.read(buf)) > -1) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
         }
     }
 
     /**
      * Copies the binary file of the launcher output of an ExperimentResult to a specified location on the filesystem.
      * @param id th id of the ExperimentResult
      * @param f the file in which the binary file is copied
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     private static void getLauncherOutput(int id, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT lancherOutput " +
                 "FROM " + table + " " +
                 "WHERE idJob=?;");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             FileOutputStream out = new FileOutputStream(f);
             InputStream in = rs.getBinaryStream("lancherOutput");
             int len;
             byte[] buf = new byte[256 * 1024];
             while ((len = in.read(buf)) > -1) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
         }
     }
 
     /**
      * Copies the binary file of the watcher output of an ExperimentResult to a specified location on the filesystem.
      * @param id th id of the ExperimentResult
      * @param f the file in which the binary file is copied
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     private static void getWatcherOutput(int id, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT watcherOutput " +
                 "FROM " + table + " " +
                 "WHERE idJob=?;");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             FileOutputStream out = new FileOutputStream(f);
             InputStream in = rs.getBinaryStream("watcherOutput");
             int len;
             byte[] buf = new byte[256 * 1024];
             while ((len = in.read(buf)) > -1) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
         }
     }
 
     /**
      * Copies the binary file of the verifier output of an ExperimentResult to a specified location on the filesystem.
      * @param id th id of the ExperimentResult
      * @param f the file in which the binary file is copied
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     private static void getVerifierOutput(int id, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT verifierOutput " +
                 "FROM " + table + " " +
                 "WHERE idJob=?;");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             FileOutputStream out = new FileOutputStream(f);
             InputStream in = rs.getBinaryStream("verifierOutput");
             int len;
             byte[] buf = new byte[256 * 1024];
             while ((len = in.read(buf)) > -1) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
         }
     }
 }
