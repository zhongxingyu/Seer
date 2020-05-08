 package edacc.model;
 
 import edacc.manageDB.Util;
 import edacc.properties.PropertyTypeNotExistException;
 import edacc.satinstances.InvalidVariableException;
 import edacc.satinstances.SATInstance;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * data access object for the Instance class
  * @author daniel
  */
 public class InstanceDAO {
 
     protected static final String table = "Instances";
     private static final ObjectCache<Instance> cache = new ObjectCache<Instance>();
 
     private static String getPropertySelect(Vector<Property> props) {
         String select = " ";
         int tbl = 0;
         for (Property p : props) {
             select += ", tbl_" + tbl++ + ".value";
         }
         return select + " ";
     }
 
     private static String getPropertyFrom(Vector<Property> props) throws IOException, NoConnectionToDBException, SQLException {
         String from = " ";
         int tbl = 0;
         for (Property p : props) {
             // from += "LEFT JOIN (SELECT idInstance, value FROM Instance_has_Property WHERE idProperty = \"" + p.getId() + "\") AS tbl_" + tbl++ + " USING (idInstance) ";
             from += "LEFT OUTER JOIN Instance_has_Property AS tbl_" + tbl++ + " ON i.idInstance=tbl_" + (tbl - 1) + ".idInstance AND tbl_" + (tbl - 1) + ".idProperty=" + p.getId() + " ";
         }
         return from;
     }
 
     private static Instance getInstance(ResultSet rs, Vector<Property> props) throws IOException, NoConnectionToDBException, SQLException {
         Instance i = new Instance();
         i.setId(rs.getInt("idInstance"));
         i.setMd5(rs.getString("md5"));
         i.setName(rs.getString("name"));
         i.setPropertyValues(new HashMap<Integer, InstanceHasProperty>());
         for (int prop = 0; prop < props.size(); prop++) {
             i.getPropertyValues().put(props.get(prop).getId(), new InstanceHasProperty(i, props.get(prop), rs.getString("tbl_" + prop + ".value")));
         }
         return i;
     }
 
     /**
      * Instance factory method. Checks if the instance is already in the Datebase and if so,
      * throws an InstanceAlreadyInDBException
      * @param file
      * @param name
      * @param md5
      * @param instanceClass
      * @return new Instance object
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws InstanceAlreadyInDBException
      */
     public static Instance createInstance(File file, String name, String md5) throws SQLException, FileNotFoundException,
             InstanceAlreadyInDBException, InstanceDuplicateInDBException {
         PreparedStatement ps;
         final String Query = "SELECT idInstance FROM " + table + " WHERE md5 = ? or name = ?";
         ps = DatabaseConnector.getInstance().getConn().prepareStatement(Query);
         ps.setString(1, md5);
         ps.setString(2, name);
         ResultSet rs = ps.executeQuery();
         alreadyInDB(rs, name, md5);
         Instance i = new Instance();
         i.setFile(file);
         i.setName(name);
         i.setMd5(md5);
         rs.close();
         ps.close();
         return i;
     }
 
     public static Instance createInstance(String name, String formula, InstanceClass instanceClass) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoConnectionToDBException, SQLException, InstanceAlreadyInDBException, InstanceDuplicateInDBException {
         String md5 = edacc.manageDB.Util.calculateMD5(formula);
         PreparedStatement ps;
        //final String Query = "SELECT idInstance FROM " + table + " WHERE md5 = ? or name = ?";
        final String Query = "SELECT idInstance FROM " + table + " WHERE md5 = ?";
         ps = DatabaseConnector.getInstance().getConn().prepareStatement(Query);
         ps.setString(1, md5);
         ResultSet rs = ps.executeQuery();
         alreadyInDB(rs, name, md5);
         Instance i = new Instance();
         i.setName(name);
         i.setMd5(md5);
         save(i, formula, instanceClass);
         rs.close();
         ps.close();
         return i;
     }
 
     public static void delete(Instance i) throws NoConnectionToDBException, SQLException, InstanceIsInExperimentException {
         if (!IsInAnyExperiment(i.getId())) {
             PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement("DELETE FROM Instances WHERE idInstance=?");
             ps.setInt(1, i.getId());
             ps.executeUpdate();
             cache.remove(i);
             i.setDeleted();
             ps.close();
         } else {
             throw new InstanceIsInExperimentException();
         }
 
     }
 
     /**
      *
      * @param instance
      * @param formula
      * @param instanceClass The instance class object, the instance is related to.
      */
     private static void save(Instance instance, String formula, InstanceClass instanceClass) {
         if (instance.isNew()) {
             try {
 
                 // insert query, set ID!
                 // TODO insert instance blob
                 // insert instance into db
                 PreparedStatement ps;
                 final String insertQuery = "INSERT INTO " + table + " (name, md5, instance) "
                         + "VALUES (?, ?, ?)";
                 ps = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                 ps.setString(1, instance.getName());
                 ps.setString(2, instance.getMd5());
                 if (!formula.isEmpty()) {
                     ByteArrayInputStream input = new ByteArrayInputStream(formula.getBytes());
                     //output = new File(instance.getFile().getName());
                     //Util.sevenZipEncode(input, output);
 
                     ps.setBinaryStream(3, input);
 
                 } else {
                     ps.setNull(3, Types.BLOB);
                 }
 
                 ps.executeUpdate();
 
 
                 ResultSet rs = ps.getGeneratedKeys();
                 if (rs.next()) {
                     instance.setId(rs.getInt(1));
                 }
                 cache.cache(instance);
 
                 ps.close();
                 instance.setSaved();
                 // Add Instance to InstanceClass
                 InstanceHasInstanceClassDAO.createInstanceHasInstance(instance, instanceClass);
 
 
                 //                output.delete();
                 //input.delete();
             } catch (Exception ex) {
                 Logger.getLogger(InstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * persists an instance object in the database
      * @param instance The instance object to persist
      * @param instanceClass The instance class object, the instance is related to.
      * @throws SQLException if an SQL error occurs while saving the instance.
      * @throws FileNotFoundException if the file of the instance couldn't be found.
      */
     public static void save(Instance instance, boolean compressBinary, InstanceClass instanceClass) throws SQLException, FileNotFoundException, IOException {
         PreparedStatement ps;
         if (instance.isNew()) {
             try {
                 // insert query, set ID!
                 // TODO insert instance blob
                 // insert instance into db
                 final String insertQuery = "INSERT INTO " + table + " (name, md5, instance) "
                         + "VALUES (?, ?, ?)";
                 ps = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                 ps.setString(1, instance.getName());
                 ps.setString(2, instance.getMd5());
                 File input = null;
                 //      File output = null;
                 FileInputStream fInStream = null;
 
                 if (instance.getFile() != null) {
 
                     input = instance.getFile();
                     fInStream = new FileInputStream(input);
                     if (compressBinary) {
                         java.sql.Blob b = DatabaseConnector.getInstance().getConn().createBlob();
                         TaskICodeProgress progress = new TaskICodeProgress(input.length(), "Compressing " + input.getName());
                         Util.sevenZipEncode(fInStream, b.setBinaryStream(1), input.length(), progress);
                         progress.finished();
                         ps.setBlob(3, b);
                     } else {
                         ps.setBinaryStream(3, fInStream);
                     }
 
                 } else {
                     ps.setNull(3, Types.BLOB);
                 }
                 ps.executeUpdate();
 
 
                 ResultSet rs = ps.getGeneratedKeys();
                 if (rs.next()) {
                     instance.setId(rs.getInt(1));
                 }
                 cache.cache(instance);
 
                 ps.close();
                 instance.setSaved();
 
                 fInStream.close();
 
                 // Add instance to the given InstanceClass
                 InstanceHasInstanceClassDAO.createInstanceHasInstance(instance, instanceClass);
                 //                output.delete();
                 //input.delete();
             } catch (Exception ex) {
                 Logger.getLogger(InstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else if (instance.isModified()) {
             // update query
             final String updateQuery = "UPDATE " + table + " SET name=?, md5=? "
                     + "WHERE idInstance=?";
             ps = DatabaseConnector.getInstance().getConn().prepareStatement(updateQuery);
             ps.setString(1, instance.getName());
             ps.setString(2, instance.getMd5());
             ps.setInt(3, instance.getId());
             ps.executeUpdate();
 
         } else {
             return;
         }
     }
 
     private static void save(Instance instance, InstanceClass instanceClass) {
         if (instance.isNew()) {
             try {
 
                 // insert query, set ID!
                 // TODO insert instance blob
                 // insert instance into db
                 PreparedStatement ps;
                 final String insertQuery = "INSERT INTO " + table + " (name, md5, instance) "
                         + "VALUES (?, ?, ?)";
                 ps = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                 File input = null;
                 //      File output = null;
                 FileInputStream fInStream = null;
 
                 if (instance.getFile() != null) {
 
                     input = instance.getFile();
                     fInStream = new FileInputStream(input);
                     java.sql.Blob b = DatabaseConnector.getInstance().getConn().createBlob();
                     TaskICodeProgress progress = new TaskICodeProgress(input.length(), "Compressing " + input.getName());
                     Util.sevenZipEncode(fInStream, b.setBinaryStream(1), input.length(), progress);
                     progress.finished();
                     ps.setBlob(3, b);
 
 
                 } else {
                     ps.setNull(3, Types.BLOB);
                 }
                 ps.setString(1, instance.getName());
                 ps.setString(2, instance.getMd5());
                 ps.executeUpdate();
                 ResultSet rs = ps.getGeneratedKeys();
                 if (rs.next()) {
                     instance.setId(rs.getInt(1));
                 }
                 cache.cache(instance);
 
                 ps.close();
                 instance.setSaved();
                 // Add Instance to InstanceClass
                 InstanceHasInstanceClassDAO.createInstanceHasInstance(instance, instanceClass);
 
                 //                output.delete();
                 //input.delete();
             } catch (Exception ex) {
                 Logger.getLogger(InstanceDAO.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * retrieves an instance from the database
      * @param id the id of the instance to be retrieved
      * @return the instance specified by its id
      * @throws SQLException
      */
     public static Instance getById(int id) throws SQLException, InstanceClassMustBeSourceException {
         Instance c = cache.getCached(id);
         if (c != null) {
             return c;
         }
 
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idInstance,  md5, name FROM " + table + " WHERE idInstance=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         Instance i = new Instance();
         if (rs.next()) {
             i.setId(rs.getInt("idInstance"));
             i.setMd5(rs.getString("md5"));
             i.setName(rs.getString("name"));
             ArrayList<Instance> tmp = new ArrayList<Instance>();
             tmp.add(i);
             InstanceHasPropertyDAO.assign(tmp);
 
             i.setSaved();
             cache.cache(i);
             return i;
         }
 
         rs.close();
         return null;
     }
 
     /**
      * retrieves all instances from the database
      * @return all instances in a List
      * @throws SQLException
      */
     public static LinkedList<Instance> getAll() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         // return linked list with all instances
         // TODO: fix!
         Vector<Property> props = PropertyDAO.getAllInstanceProperties();
         Statement st = DatabaseConnector.getInstance().getConn().createStatement();
         ResultSet rs = st.executeQuery("SELECT i.idInstance, i.md5, i.name" + getPropertySelect(props)
                 + "FROM " + table + " AS i " + getPropertyFrom(props));
         LinkedList<Instance> res = new LinkedList<Instance>();
         while (rs.next()) {
             Instance c = cache.getCached(rs.getInt("i.idInstance"));
             if (c != null) {
                 res.add(c);
                 continue;
             }
             Instance i = getInstance(rs, props);
             i.setSaved();
             cache.cache(i);
             res.add(i);
         }
         rs.close();
         st.close();
         return res;
     }
 
     public static LinkedList<Instance> getAllByExperimentId(int id) throws SQLException, InstanceClassMustBeSourceException, IOException {
         // TODO: fix!
         Vector<Property> props = new Vector<Property>();//InstancePropertyManager.getInstance().getAll();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT DISTINCT i.idInstance, i.md5, i.name" + getPropertySelect(props)
                 + "FROM " + table + " as i JOIN Experiment_has_Instances as ei ON "
                 + "i.idInstance = ei.Instances_idInstance " + getPropertyFrom(props) + " WHERE ei.Experiment_idExperiment = ?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         LinkedList<Instance> res = new LinkedList<Instance>();
         while (rs.next()) {
             Instance c = cache.getCached(rs.getInt("i.idInstance"));
             if (c != null) {
                 res.add(c);
             } else {
                 Instance i = getInstance(rs, props);
                 i.setSaved();
                 cache.cache(i);
                 res.add(i);
             }
         }
         rs.close();
         st.close();
 
         return res;
     }
 
     /**
      * @author rretz
      * retrieves instances from the database.
      * @return Hashtable with all instances which belong to a experiment.
      * @throws NoConnectionToDBException if no connection to database exists.
      * @throws SQLException if an SQL error occurs while reading the instances from the database.
      */
     public static boolean IsInAnyExperiment(int id) throws NoConnectionToDBException, SQLException {
         Statement st = DatabaseConnector.getInstance().getConn().createStatement();
 
         ResultSet rs = st.executeQuery("SELECT idEI FROM Experiment_has_Instances WHERE Instances_idInstance = " + id + " LIMIT 1;");
         return rs.next();
     }
 
     /**
      * @author rretz
      * Get the binary of a instance with the given id as a Blob from the database.
      * @param id
      * @return Blob of the instance binary.
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws InstanceNotInDBException
      */
     public static InputStream getBinary(int id) throws NoConnectionToDBException, SQLException, InstanceNotInDBException, IOException {
         Statement st = DatabaseConnector.getInstance().getConn().createStatement();
 
         ResultSet rs = st.executeQuery("SELECT i.instance FROM " + table + " AS i WHERE i.idInstance = " + id);
         try {
             if (rs.next()) {
                 return Util.getDecompressedInputStream(rs.getBlob("instance").getBinaryStream());
             } else {
                 throw new InstanceNotInDBException();
             }
         } finally {
             st.close();
         }
     }
 
     /**
      * 
      * @param allChoosen
      * @return all instances from the database which have one of the given instance classes and returns them.
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public static LinkedList<Instance> getAllByInstanceClasses(Vector<InstanceClass> allChoosen) throws NoConnectionToDBException, SQLException {
         if (!allChoosen.isEmpty()) {
             String query = "SELECT i.idInstance, i.md5, i.name"
                     + " FROM " + table + " as i "
                     + " LEFT JOIN Instances_has_instanceClass as ii ON i.idInstance = ii.Instances_idInstance "
                     + " WHERE ii.instanceClass_idinstanceClass = " + allChoosen.get(0).getInstanceClassID();
             for (int i = 1; i < allChoosen.size(); i++) {
                 query += " OR ii.instanceClass_idinstanceClass = " + allChoosen.get(i).getInstanceClassID();
             }
             Statement st = DatabaseConnector.getInstance().getConn().createStatement();
             ResultSet rs = st.executeQuery(query);
             LinkedList<Instance> res = new LinkedList<Instance>();
             ArrayList<Instance> instanceHasPropertyAssignList = new ArrayList<Instance>();
             while (rs.next()) {
 
                 Instance c = cache.getCached(rs.getInt("i.idInstance"));
                 if (c != null) {
                     res.add(c);
                     continue;
                 }
                 Instance i = new Instance();
                 i.setId(rs.getInt("i.idInstance"));
                 i.setMd5(rs.getString("i.md5"));
                 i.setName(rs.getString("i.name"));
                 i.setSaved();
                 cache.cache(i);
                 res.add(i);
                 instanceHasPropertyAssignList.add(i);
             }
             rs.close();
             InstanceHasPropertyDAO.assign(instanceHasPropertyAssignList);
             return res;
         }
 
         return null;
     }
 
     /**
      * Copies the binary file of an instance to a temporary location on the file system
      * and returns a File reference on it.
      * @param i
      * @return
      */
     public static File getBinaryFileOfInstance(Instance i) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException, InstanceNotInDBException {
         File f = new File("tmp" + System.getProperty("file.separator") + i.getId() + "_" + i.getName());
         // create missing directories
         f.getParentFile().mkdirs();
         getBinaryFileOfInstance(i, f);
         return f;
     }
 
     /**
      * Copies the binary file of an instance to a specified location on the filesystem.
      * @param i
      * @param f
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static void getBinaryFileOfInstance(Instance i, File f) throws FileNotFoundException, IOException, NoConnectionToDBException, InstanceNotInDBException {
         FileOutputStream out = new FileOutputStream(f);
         try {
             Util.sevenZipDecode(getBinary(i.getId()), out);
         } catch (SQLException e) {
             // TODO: error
             e.printStackTrace();
         } finally {
             out.close();
         }
     }
 
     public static void clearCache() {
         cache.clear();
     }
 
     public static SATInstance getSATFormulaOfInstance(Instance i) throws IOException, InvalidVariableException, InstanceNotInDBException, SQLException {
         return edacc.satinstances.InstanceParser.getInstance().parseInstance(getBinary(i.getId()));
     }
 
     /**
      * Returns the name of the benchmark type of an instance or null if it doesn't have one
      * @param i
      * @return
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public static String getBenchmarkType(Instance i) throws NoConnectionToDBException, SQLException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT BenchmarkType.name as name FROM BenchmarkType JOIN Instances ON idBenchmarkType=BenchmarkType_idBenchmarkType "
                 + "WHERE idInstance=?");
         ps.setInt(1, i.getId());
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             return rs.getString("name");
         }
         return null;
     }
 
     /**
      * Returns a HashMap mapping instance IDs to their benchmark type names.
      * @return
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public static HashMap<Integer, String> getBenchmarkTypes() throws NoConnectionToDBException, SQLException {
         HashMap<Integer, String> res = new HashMap<Integer, String>();
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT idInstance, BenchmarkType.name as name "
                 + "FROM BenchmarkType JOIN Instances ON idBenchmarkType=BenchmarkType_idBenchmarkType");
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             res.put(new Integer(rs.getInt("idInstance")), rs.getString("name"));
         }
         return res;
     }
 
     /**
      * Deletes the given instance objects from the database and the cache. Sets their PersistenceState
      * to deleted.
      * @param lastRelated
      * @throws SQLException
      */
     public static void deleteAll(Vector<Instance> lastRelated) throws SQLException {
         if (lastRelated.isEmpty()) {
             return;
         }
         String query = "DELETE FROM Instances WHERE idInstance=" + lastRelated.get(0).getId();
         for (int i = 1; i < lastRelated.size(); i++) {
             query += " OR idInstance=" + lastRelated.get(i).getId();
         }
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.executeUpdate();
         for (int i = 0; i < lastRelated.size(); i++) {
             cache.remove(lastRelated.get(i));
             lastRelated.get(i).setDeleted();
         }
     }
 
     /**
      * 
      * @param i
      * @return
      * @throws SQLException
      */
     public static Vector<Instance> getLastRelatedInstances(InstanceClass i) throws SQLException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT COUNT(Instances_idInstance) AS sum, Instances_idInstance, instanceClass_idinstanceClass "
                 + "FROM Instances_has_instanceClass as ihi "
                 + "GROUP BY Instances_idInstance "
                 + "HAVING sum =1 AND instanceClass_idinstanceClass =?");
         ps.setInt(1, i.getId());
         ResultSet rs = ps.executeQuery();
         Vector<Instance> lastRelated = new Vector<Instance>();
         while (rs.next()) {
             lastRelated.add(InstanceDAO.getById(rs.getInt("Instances_idInstance")));
         }
 
         return lastRelated;
     }
 
     /**
      * 
      * @param md5
      * @return the instance object with the given md5 sum.
      */
     public static Instance getByMd5(String md5) throws SQLException {
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idInstance FROM " + table + " WHERE md5=?");
         ps.setString(1, md5);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             return getById(rs.getInt(1));
         }
         return null;
     }
 
     /**
      *  Throws an exception, depending on the state of equality of the instances.
      * @param rs ResultSet with the duplicate entries.
      * @param name Name of the new created instance.
      * @param md5 MD5 sum of the new created instance.   
      * @throws SQLException
      * @throws InstanceAlreadyInDBException
      * @throws InstanceDuplicateNameException
      * @throws InstanceDuplicateMd5Exception 
      */
     private static void alreadyInDB(ResultSet rs, String name, String md5) throws SQLException, InstanceAlreadyInDBException, InstanceDuplicateInDBException {
         ArrayList<Instance> duplicates = new ArrayList<Instance>();
         while (rs.next()) {
             int instanceId = rs.getInt("idInstance");
             Instance duplicate = getById(instanceId);
             if (duplicate.getName().equals(name) && duplicate.getMd5().equals(md5)) {
                 throw new InstanceAlreadyInDBException(duplicate);
             } else {
                 duplicates.add(duplicate);
             }
         }
 
         if (!duplicates.isEmpty()) {
             throw new InstanceDuplicateInDBException(duplicates);
         }
     }
 
     public static void createDuplicateInstance(Instance i, InstanceClass iClass) {
         save(i, iClass);
     }
 }
