 package edacc.model;
 
 import edacc.manageDB.Util;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
import java.io.File;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author simon
  */
 public class VerifierDAO {
 
     private static final String table = "Verifier";
     private static final String selectQuery = "SELECT idVerifier, name, description, md5, runCommand, runPath FROM " + table;
     private static final ObjectCache<Verifier> cache = new ObjectCache<Verifier>();
 
     private static String getInsertQuery(int count) {
         StringBuilder query = new StringBuilder("INSERT INTO " + table + " "
                 + "(name,binaryArchive,description,md5,runCommand,runPath) "
                 + "VALUES (?,?,?,?,?,?)");
         count--;
         for (int i = 0; i < count; i++) {
             query.append(",(?,?,?,?,?,?)");
         }
         return query.toString();
     }
 
     private static String getUpdateQuery(int count) {
         // create a dummy header (there is no dataset with idVerifier == -1)
         StringBuilder constTable = new StringBuilder("(SELECT -1 AS idVerifier, \"\" AS name, \"\" AS description, \"\" AS md5, \"\" AS runCommand, \"\" AS runPath");
         // append rows to constTable
         for (int i = 0; i < count; i++) {
             constTable.append(" UNION SELECT ?,?,?,?,?,?");
         }
         constTable.append(")");
 
         StringBuilder query = new StringBuilder("UPDATE " + table + " AS t1, ");
         query.append(constTable);
         query.append(" AS t2 ");
         query.append("SET t1.name=t2.name, t1.description=t2.description,t1.md5=t2.md5,t1.runCommand=t2.runCommand,t1.runPath=t2.runPath ");
         query.append("WHERE t1.idVerifier=t2.idVerifier");
         return query.toString();
     }
 
     private static String getUpdateFilesQuery(int count) {
         // create a dummy header (there is no dataset with idVerifier == -1)
         StringBuilder constTable = new StringBuilder("(SELECT -1 AS idVerifier, \"\" AS binaryArchive");
         // append rows to constTable
         for (int i = 0; i < count; i++) {
             constTable.append(" UNION SELECT ?,?");
         }
         constTable.append(")");
 
         StringBuilder query = new StringBuilder("UPDATE " + table + " AS t1, ");
         query.append(constTable);
         query.append(" AS t2 ");
         query.append("SET t1.binaryArchive=t2.binaryArchive ");
         query.append("WHERE t1.idVerifier=t2.idVerifier");
         return query.toString();
     }
 
     private static String getDeleteQuery(int count) {
         StringBuilder query = new StringBuilder("DELETE FROM " + table + " WHERE idVerifier IN (?");
         count--;
         for (int i = 0; i < count; i++) {
             query.append(",?");
         }
         query.append(')');
         return query.toString();
     }
 
    private static File getBasePath(File[] files) {
        File res = null;
        for (File f : files) {
            if (f.isDirectory()) {
                res = f.getParentFile();
            } else {
                res = f;
            }
        }
        return res;
    }

     public static void saveAll(List<Verifier> verifiers) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         DatabaseConnector.getInstance().getConn().setAutoCommit(false);
         try {
             List<Verifier> newVerifiers = new LinkedList<Verifier>();
             List<Verifier> modifiedVerifiers = new LinkedList<Verifier>();
             List<Verifier> verifiersWithModifiedFiles = new LinkedList<Verifier>();
             List<Verifier> deletedVerifiers = new LinkedList<Verifier>();
             for (Verifier v : verifiers) {
                 if (v.isNew()) {
                     newVerifiers.add(v);
                 } else if (v.isModified()) {
                     modifiedVerifiers.add(v);
                     if (v.getFiles() != null && v.getFiles().length != 0) {
                         verifiersWithModifiedFiles.add(v);
                     }
                 } else if (v.isDeleted()) {
                     deletedVerifiers.add(v);
                 }
             }
 
             if (!newVerifiers.isEmpty()) {
                 String query = getInsertQuery(newVerifiers.size());
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                 int curCount = 1;
                 for (Verifier v : newVerifiers) {
                     st.setString(curCount++, v.getName());
                    ByteArrayOutputStream zipped = Util.zipFileArrayToByteStream(v.getFiles(), getBasePath(v.getFiles()));
                     st.setBinaryStream(curCount++, new ByteArrayInputStream(zipped.toByteArray()));
                     st.setString(curCount++, v.getDescription());
                     st.setString(curCount++, v.getMd5());
                     st.setString(curCount++, v.getRunCommand());
                     st.setString(curCount++, v.getRunPath());
                 }
                 st.executeUpdate();
                 ResultSet rs = st.getGeneratedKeys();
                 int i = 0;
                 while (rs.next()) {
                     newVerifiers.get(i).setId(rs.getInt(1));
                     newVerifiers.get(i).setSaved();
                     final Verifier v = newVerifiers.get(i);
                     cache.cache(v);
                     DatabaseConnector.getInstance().addRollbackOperation(new Runnable() {
 
                         @Override
                         public void run() {
                             v.setNew();
                             cache.remove(v);
                         }
                     });
                     i++;
                 }
                 rs.close();
                 st.close();
             }
             if (!modifiedVerifiers.isEmpty()) {
                 String query = getUpdateQuery(modifiedVerifiers.size());
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query);
                 int curCount = 1;
                 for (Verifier v : modifiedVerifiers) {
                     st.setInt(curCount++, v.getId());
                     st.setString(curCount++, v.getName());
                     st.setString(curCount++, v.getDescription());
                     st.setString(curCount++, v.getMd5());
                     st.setString(curCount++, v.getRunCommand());
                     st.setString(curCount++, v.getRunPath());
 
                     v.setSaved();
                     final Verifier ve = v;
                     DatabaseConnector.getInstance().addRollbackOperation(new Runnable() {
 
                         @Override
                         public void run() {
                             ve.setModified();
                         }
                     });
                 }
                 st.executeUpdate();
                 st.close();
             }
             if (!verifiersWithModifiedFiles.isEmpty()) {
                 String query = getUpdateFilesQuery(verifiersWithModifiedFiles.size());
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query);
                 int curCount = 1;
                 for (Verifier v : verifiersWithModifiedFiles) {
                     st.setInt(curCount++, v.getId());
                    ByteArrayOutputStream zipped = Util.zipFileArrayToByteStream(v.getFiles(), getBasePath(v.getFiles()));
                     st.setBinaryStream(curCount++, new ByteArrayInputStream(zipped.toByteArray()));
                     v.setSaved();
 
                     final Verifier ve = v;
                     DatabaseConnector.getInstance().addRollbackOperation(new Runnable() {
 
                         @Override
                         public void run() {
                             ve.setModified();
                         }
                     });
                 }
                 st.executeUpdate();
                 st.close();
             }
             if (!deletedVerifiers.isEmpty()) {
                 String query = getDeleteQuery(deletedVerifiers.size());
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query);
                 int curCount = 1;
                 for (Verifier v : deletedVerifiers) {
                     st.setInt(curCount++, v.getId());
                 }
                 st.executeUpdate();
                 st.close();
             }
 
             for (Verifier v : verifiers) {
                 for (VerifierParameter vp : v.getParameters()) {
                     vp.setIdVerifier(v.getId());
                 }
             }
 
             VerifierParameterDAO.saveAll(verifiers);
         } catch (Throwable t) {
             if (autoCommit) {
                 DatabaseConnector.getInstance().rollback();
             }
             if (t instanceof Error) {
                 throw (Error) t;
             } else if (t instanceof SQLException) {
                 throw (SQLException) t;
             }
         } finally {
             if (autoCommit) {
                 DatabaseConnector.getInstance().commit();
                 DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
             }
         }
     }
 
     private static Verifier getVerifierFromResultSet(ResultSet rs, Map<Integer, List<VerifierParameter>> verifierParameterMap) throws SQLException {
         Verifier verifier = new Verifier();
         verifier.setId(rs.getInt("idVerifier"));
         verifier.setName(rs.getString("name"));
         verifier.setDescription(rs.getString("description"));
         verifier.setMd5(rs.getString("md5"));
         verifier.setRunCommand(rs.getString("runCommand"));
         verifier.setRunPath(rs.getString("runPath"));
 
         verifier.setParameters(verifierParameterMap.get(verifier.getId()));
         return verifier;
     }
 
     public static List<Verifier> getAllVerifiers() throws SQLException {
         List<Verifier> res = new ArrayList<Verifier>();
         Statement st = DatabaseConnector.getInstance().getConn().createStatement();
         Map<Integer, List<VerifierParameter>> verifierParameterMap = VerifierParameterDAO.getAll();
         ResultSet rs = st.executeQuery(selectQuery);
         while (rs.next()) {
             int id = rs.getInt("idVerifier");
             Verifier v = cache.getCached(id);
             if (v == null) {
                 v = getVerifierFromResultSet(rs, verifierParameterMap);
                 v.setSaved();
                 cache.cache(v);
             }
             res.add(v);
         }
         rs.close();
         st.close();
         return res;
     }
 
     public static Verifier getById(int id) throws SQLException {
         Verifier v = cache.getCached(id);
         if (v == null) {
             getAllVerifiers();
             v = cache.getCached(id);
         }
         return v;
     }

     public static void clearCache() {
         cache.clear();
     }
 
     public static void saveAllCached() throws SQLException {
         List<Verifier> v = new ArrayList<Verifier>();
         v.addAll(cache.values());
         saveAll(v);
     }
 }
