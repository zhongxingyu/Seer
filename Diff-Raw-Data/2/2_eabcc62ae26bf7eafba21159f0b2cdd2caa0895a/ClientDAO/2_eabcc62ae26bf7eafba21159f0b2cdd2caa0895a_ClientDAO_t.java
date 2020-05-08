 package edacc.model;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  *
  * @author simon
  */
 public class ClientDAO {
 
     protected static final String table = "Client";
     protected static final String selectQuery = "SELECT * FROM Clients";
     protected static final String updateQuery = "UPDATE " + table + " SET message =? WHERE idClient=?";
     private static final ObjectCache<Client> cache = new ObjectCache<Client>();
 
     private static HashSet<Integer> getClientIds() throws SQLException {
         HashSet<Integer> res = new HashSet<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT idClient FROM " + table);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt("idClient"));
         }
         rs.close();
         st.close();
         return res;
     }
 
     private static String getIntArray(Collection<Integer> c) {
         String res = "(";
         Iterator<Integer> it = c.iterator();
         while (it.hasNext()) {
             res += "" + it.next();
             if (it.hasNext()) {
                 res += ",";
             }
         }
         res += ")";
         return res;
     }
 
     public static synchronized ArrayList<Client> getClients() throws SQLException {
         ArrayList<Client> clients = new ArrayList<Client>();
 
         HashSet<Integer> clientIds = getClientIds();
         ArrayList<Integer> idsModified = new ArrayList<Integer>();
         ArrayList<Client> deletedClients = new ArrayList<Client>();
 
         for (Client c : cache.values()) {
             if (clientIds.contains(c.getId())) {
                 idsModified.add(c.getId());
             } else {
                 deletedClients.add(c);
             }
             clientIds.remove(c.getId());
         }
 
         if (!idsModified.isEmpty()) {
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT idClient, message, jobs_wait_time, current_wait_time, TIMESTAMPDIFF(SECOND, lastReport, NOW()) > 20 AS dead FROM " + table + " WHERE idClient IN " + getIntArray(idsModified));
             ResultSet rs = st.executeQuery();
             while (rs.next()) {
                 Client c = cache.getCached(rs.getInt("idClient"));
                 c.setMessage(rs.getString("message"));
                 c.setDead(rs.getBoolean("dead"));
                 c.setWait_time(rs.getInt("jobs_wait_time"));
                 c.setCurrent_wait_time(rs.getInt("current_wait_time"));
             }
             rs.close();
             st.close();
         }
 
         if (!clientIds.isEmpty()) {
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT idClient, numCores, numThreads, hyperthreading, turboboost, CPUName, cacheSize, cpuflags, memory, memoryFree, cpuinfo, meminfo, message, gridQueue_idgridQueue, lastReport, TIMESTAMPDIFF(SECOND, lastReport, NOW()) > 20 AS dead, jobs_wait_time, current_wait_time FROM " + table + " WHERE idClient IN " + getIntArray(clientIds));
             ResultSet rs = st.executeQuery();
             while (rs.next()) {
                 Client c = new Client(rs);
                 cache.cache(c);
             }
             rs.close();
             st.close();
         }
         for (Client c : cache.values()) {
             clients.add(c);
         }
         for (Client c : deletedClients) {
             c.setDeleted();
             c.notifyObservers();
             cache.remove(c);
         }
 
        PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT Experiment_idExperiment, Client_idClient, numCores FROM Experiment_has_Client WHERE numCores > 0");
         ResultSet rs = st.executeQuery();
         HashMap<Client, HashMap<Experiment, Integer>> map = new HashMap<Client, HashMap<Experiment, Integer>>();
         while (rs.next()) {
             int clientId = rs.getInt("Client_idClient");
             int numCores = rs.getInt("numCores");
             Client c = cache.getCached(clientId);
             if (c == null) {
                 continue;
             }
             Experiment exp = ExperimentDAO.getById(rs.getInt("Experiment_idExperiment"));
             HashMap<Experiment, Integer> tmp = map.get(c);
             if (tmp == null) {
                 tmp = new HashMap<Experiment, Integer>();
                 map.put(c, tmp);
             }
             tmp.put(exp, numCores);
         }
         st.close();
         for (Client c : cache.values()) {
             HashMap<Experiment, Integer> tmp = map.get(c);
             if (tmp != null) {
                 c.setComputingExperiments(tmp);
             }
             if (c.isModified()) {
                 c.notifyObservers();
                 c.setSaved();
             }
         }
         return clients;
     }
 
     public static int getJobCount(Client client) throws SQLException {
         final String query = "SELECT COUNT(idJob) FROM ExperimentResults WHERE Client_idClient = ?";
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.setInt(1, client.getId());
         ResultSet rs = ps.executeQuery();
         int res = 0;
         if (rs.next()) {
             res = rs.getInt(1);
         } else {
             res = 0;
         }
         rs.close();
         ps.close();
         return res;
     }
 
     public static void sendMessage(Integer clientId, String message) throws SQLException {
         if (message.equals("")) {
             return;
         }
         if (message.charAt(message.length() - 1) != '\n') {
             message += '\n';
         }
         final String query = "UPDATE Client SET message = CONCAT(message, ?) WHERE idClient = ?";
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.setString(1, message);
         ps.setInt(2, clientId);
 
         ps.executeUpdate();
         ps.close();
     }
 
     public static void sendMessage(Client client, String message) throws SQLException {
         sendMessage(client.getId(), message);
     }
 
     public static void removeDeadClients() throws SQLException {
         final String query = "DELETE FROM Client WHERE TIMESTAMPDIFF(SECOND, lastReport, NOW()) > 20";
         PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement(query);
         ps.executeUpdate();
         ps.close();
         // update cache; will notify client browser
         getClients();
     }
 
     public static void clearCache() {
         cache.clear();
     }
 }
