 package edacc.model;
 
 import java.util.LinkedList;
 import java.util.Hashtable;
 import java.sql.*;
 
 /**
  * data access object for the Instance class
  * @author daniel
  */
 public class InstanceDAO {
     protected static final String table = "Instances";
     private static final Hashtable<Instance, Instance> cache = new Hashtable<Instance, Instance>();
 
     /**
      * Instance factory method, ensures that the created instance is persisted and assigned an ID
      * so it can be referenced by related objects
      * @return new Instance object
      */
      public static Instance createInstance() throws SQLException {
         Instance i = new Instance();
         save(i);
         cacheInstance(i);
         return i;
      }
 
      /**
       * Instance factory methode for an Instance which is temporary and therfor requires no
       *  datebase item
       * @return new Instance object
       */
      public static Instance createInstanceTemp(){
         Instance i = new Instance();
         return i;
      }
 
      /**
       * persists an temporary instance object to the database
       * @param instance The temporary instance object to persist
       */
      public static void saveTempInstance(Instance instance) throws SQLException{
         save(instance);
         cacheInstance(instance);
      }
 
     /**
      * persists an instance object in the database
      * @param instance The instance object to persist
      */
     public static void save(Instance instance) throws SQLException {
         PreparedStatement ps;
         if (instance.isNew()) {
             // insert query, set ID!
             // TODO insert instance blob
             // insert instance into db
             final String insertQuery = "INSERT INTO " + table + " (name, md5, numAtoms, numClauses, ratio, maxClauseLength) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
             ps = DatabaseConnector.getInstance().conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);       
         }
         else if (instance.isModified()) {
             // update query
             final String updateQuery = "UPDATE " + table + " SET name=?, md5=?, numAtoms=?, numClauses=?, ratio=?, maxClauseLength=? " +
                     "WHERE idInstance=?";
             ps = DatabaseConnector.getInstance().conn.prepareStatement(updateQuery);
            
             ps.setInt(7, instance.getId());
             
         } else
             return;
 
         ps.setString(1, instance.getName());
         ps.setString(2, instance.getMd5());
         ps.setInt(3, instance.getNumAtoms());
         ps.setInt(4, instance.getNumClauses());
         ps.setInt(5, instance.getRatio());
         ps.setInt(6, instance.getMaxClauseLength());
         ps.executeUpdate();
 
         // set id
         if (instance.isNew()) {
             ResultSet rs = ps.getGeneratedKeys();
             if (rs.next())
                 instance.setId(rs.getInt(1));
         }
 
         instance.setSaved();
 
         instance.setSaved();
     }
 
     private static Instance getCached(Instance i) {
         if (cache.containsKey(i)) {
             return cache.get(i);
         }
         else return null;
     }
 
     private static void cacheInstance(Instance i) {
         if (cache.containsKey(i)) return;
         else cache.put(i, i);
     }
 
     /**
      * retrieves an instance from the database
      * @param id the id of the instance to be retrieved
      * @return the instance specified by its id
      * @throws SQLException
      */
     public static Instance getById(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().conn.prepareStatement("SELECT * FROM " + table + " WHERE idInstance=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         Instance i = new Instance();
         if (rs.next()) {
             i.setId(rs.getInt("idInstance"));
             i.setMaxClauseLength(rs.getInt("maxClauseLength"));
             i.setMd5(rs.getString("md5"));
             i.setName(rs.getString("name"));
             i.setNumAtoms(rs.getInt("numAtoms"));
             i.setNumClauses(rs.getInt("numClauses"));
             i.setRatio(rs.getInt("ratio"));
 
             Instance c = getCached(i);
             if (c != null) return c;
             else {
                 i.setSaved();
                 cacheInstance(i);
                 return i;
             }
         }
         return null;
     }
 
     /**
      * retrieves all instances from the database
      * @return all instances in a List
      * @throws SQLException
      */
     public static LinkedList<Instance> getAll() throws SQLException {
         // return linked list with all instances
         Statement st = DatabaseConnector.getInstance().conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM " + table);
         LinkedList<Instance> res = new LinkedList<Instance>();
         while (rs.next()) {
             Instance i = new Instance();
             i.setId(rs.getInt("idInstance"));
             i.setMaxClauseLength(rs.getInt("maxClauseLength"));
             i.setMd5(rs.getString("md5"));
             i.setName(rs.getString("name"));
             i.setNumAtoms(rs.getInt("numAtoms"));
             i.setNumClauses(rs.getInt("numClauses"));
             i.setRatio(rs.getInt("ratio"));
             
             Instance c = getCached(i);
             if (c != null) res.add(c);
             else {
                 i.setSaved();
                 cacheInstance(i);
                 res.add(i);
             }
         }
         rs.close();
         return res;
     }
 
     public static LinkedList<Instance> getAllByExperimentId(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().conn.prepareStatement(
                 "SELECT i.* FROM " + table + " as i JOIN Experiment_has_Instances as ei ON " +
                "i.idInstance = ei.Instances_idInstance WHERE ei.Experiments_idExperiment = ?"
                 );
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         LinkedList<Instance> res = new LinkedList<Instance>();
         while (rs.next()) {
             Instance i = new Instance();
             i.setId(rs.getInt("i.idInstance"));
             i.setMaxClauseLength(rs.getInt("i.maxClauseLength"));
             i.setMd5(rs.getString("i.md5"));
             i.setName(rs.getString("i.name"));
             i.setNumAtoms(rs.getInt("i.numAtoms"));
             i.setNumClauses(rs.getInt("i.numClauses"));
             i.setRatio(rs.getInt("i.ratio"));
 
             Instance c = getCached(i);
             if (c != null) res.add(c);
             else {
                 i.setSaved();
                 cacheInstance(i);
                 res.add(i);
             }
         }
         rs.close();
         return res;
     }
 
     public static void main(String[] args) throws ClassNotFoundException, SQLException {
         DatabaseConnector db = DatabaseConnector.getInstance();
         db.connect("localhost", 3306, "root", "EDACC", "sopra");
         Instance i = new Instance();
         i.setName("test1234");
         i.setNew();
         i.setMd5("12345");
         save(i);
         cacheInstance(i);
         System.out.println(i.getId());
         db.conn.close();
     }
 }
