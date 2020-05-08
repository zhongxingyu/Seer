 /**
  * @author slava
  * @version $Id $
  */
 package netflow;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 import java.io.FileInputStream;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 //TODO: externalize url and driver and so on...
 public class DatabaseProxy {
     private Connection con;
     private static final String url = "jdbc:postgresql://localhost/traffic";
     private static final String driver = "org.postgresql.Driver";
     private static final String userName = "root";
     private static final String password = "test12~";
     private static DatabaseProxy ourInstance = new DatabaseProxy();
     private static final Log log = LogFactory.getLog(DatabaseProxy.class);
     private static final String CONFIGURATION = "configuration";
 
 
     private DatabaseProxy() {
         try {
             Properties props;
             try {
                 props = readProperties();
             }catch(IOException e){
                 log.warn("Cannot read properties from file " + CONFIGURATION);
                 props = fillDefaultProperties();
             }
             con = createConnection(props);
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException("Database driver not found", e);
         } catch (SQLException e) {
             throw new IllegalArgumentException("SQL Exception", e);
         }
     }
 
     private Properties fillDefaultProperties() {
         return new Properties();
     }
 
     private Connection createConnection(Properties properties) throws SQLException, ClassNotFoundException {
         Class.forName(properties.getProperty("driver", driver));
         return DriverManager.getConnection(properties.getProperty("url", url),
                 properties.getProperty("userName", userName),
                 properties.getProperty("password", password));
     }
 
 
     private Properties readProperties() throws IOException {
        Properties props = new Properties();
             String configFileName = System.getProperty(CONFIGURATION);
             if (configFileName != null){
                 File f = new File(configFileName);
                 if (f.exists() && f.isFile() && f.canRead()){
                     props.load(new FileInputStream(f));
                 }
             }
         return props;
     }
 
     public static DatabaseProxy getInstance() {
         if (ourInstance == null){
             ourInstance = new DatabaseProxy();
         }
         return ourInstance;
     }
 
 
     public List<NetworkDefinition> getNetworks() {
         String sql = "select net, mask, nat_addr, id from networks where client is not null";
         List<NetworkDefinition> tmp = new ArrayList<NetworkDefinition>();
         try {
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery();
             while (rs.next()) {
                 NetworkDefinition nd = new NetworkDefinition(rs.getInt(4), rs.getString(1), rs.getString(2), rs.getString(3));
                 tmp.add(nd);
             }
             rs.close();
             pstmt.close();
         } catch (SQLException e) {
             throw new Error(e);
         }
         return tmp;
     }
 
     public void saveNetworks(Map cache, java.util.Date dat) {
         if (cache.size() == 0) {
             log.debug("Nothing to save");
             return;
         }
         log.debug("cache size: " + cache.size() + " " + dat);
         String sql = "insert into netflow_networks_details (network_id, dat, input, output) values (?, ?, ?, ?)";
         try {
             PreparedStatement pstmt = con.prepareStatement(sql);
             pstmt.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
             for (Object key : cache.keySet()) {
                 NetworkTraffic traffic = (NetworkTraffic) cache.get(key);
                 pstmt.setInt(1, traffic.getNetworkId());
                 pstmt.setLong(3, traffic.getInputBytes());
                 pstmt.setLong(4, traffic.getOutputBytes());
                 pstmt.executeUpdate();
             }
             pstmt.close();
         } catch (SQLException e) {
             System.err.println(e.getMessage());
         }
     }
     
     public java.util.Date getMaxDate(){
     	java.util.Date result = null;
     	String sql = "select max(dat) from netflow_details";
     	try{
     		PreparedStatement pstmt = con.prepareStatement(sql);
     		ResultSet rs = pstmt.executeQuery();
     		if (rs.next()){
     			Timestamp t = rs.getTimestamp(1);
           if (t != null){
     			  result = new java.util.Date();
     			  result.setTime(t.getTime());
           }
     		}
     	}catch(SQLException e){
     		log.error(e);
     		e.printStackTrace();
     	}
     	return result;
     }
     
     public void saveHosts(Map<String, HostTraffic> cache, java.util.Date date) {
         if (cache.size() == 0) {
             log.debug("Host cache empty");
             return;
         }
         log.debug("Saving "  + cache.size() + " records for " + date);
         String sql = "insert into netflow_details(dat, host, network_id, input, output) values (?, ?, ?, ?, ?)";
         try {
             PreparedStatement pstmt = con.prepareStatement(sql);
             Timestamp t = new java.sql.Timestamp(date.getTime());
             for (String key : cache.keySet()) {
                 HostTraffic traffic = cache.get(key);
                 if (!hasRecord(t, traffic.getHostAddress(), traffic.getNetworkId())) {
                     pstmt.setTimestamp(1, t);
                     pstmt.setString(2, traffic.getHostAddress());
                     pstmt.setInt(3, traffic.getNetworkId());
                     pstmt.setLong(4, traffic.getInputBytes());
                     pstmt.setLong(5, traffic.getOutputBytes());
                     pstmt.addBatch();
                 }
             }
             int[] results = pstmt.executeBatch();
             log.info("saveHosts(): saved " + results.length + " records");
             pstmt.close();
             pstmt.clearParameters();
         } catch (SQLException e) {
             log.error("Saving hosts error: " + e.getMessage());
             SQLException ex = e.getNextException();
             if (ex != null){
                 log.error(ex.getMessage());
             }
             e.printStackTrace(System.err);
         }
     }
 
 
     private Collection<AggregationRecord> askForData(Integer clientId) throws SQLException {
         String sql = "select nn_summ.dat, sum(nn_summ.input), sum(nn_summ.output) from nn_summ where " +
                 " nn_summ.network_id in (select id from networks where client= ?) " +
                 " and nn_summ.dat > (select max(dat) from client_ntraffic) group by 1";
         PreparedStatement pst = con.prepareStatement(sql);
         pst.setInt(1, clientId);
         final ResultSet set = pst.executeQuery();
         Collection<AggregationRecord> result = new LinkedList<AggregationRecord>();
         while(set.next()){
            AggregationRecord ar = new AggregationRecord(clientId, set.getTimestamp(1), set.getLong(2), set.getLong(2));
             result.add(ar);
         }
         set.close();
         pst.close();
         return result;
     }
 
     public void doAggregation(){
        //todo: the same for doAggregation(Date)
       String sql = "insert into client_ntraffic(client, dat, incoming, outcoming) " +
                "(?, ?, ?, ?)";
         String logStr = "doAggregation(): ";
         log.info(logStr + " <<<<");
         try{
             List<Integer> clients = getNetworkedClients();
             PreparedStatement pst = con.prepareStatement(sql);
 
             for (Integer client : clients) {
                 Collection<AggregationRecord> records = askForData(client);
                 for (AggregationRecord record : records) {
                     pst.setInt(1, record.getClientId());
                     pst.setTimestamp(2, record.getStamp());
                     pst.setLong(3, record.getInput());
                     pst.setLong(4, record.getOutput());
                     pst.addBatch();
                 }
             }
 
             pst.executeBatch();
             pst.close();
         } catch (SQLException e) {
             log.error(logStr + " Aggregation error: " + e.getMessage());
             e.printStackTrace(System.err);
         }
         log.info(logStr + " >>>>");
     }
 
     public void doAggregation(Date date){
        if (date == null){
            doAggregation();
            return;
        }
 
         String logStr = "doAggregation(): ";
         Timestamp start = Utils.getStartDate(date);
         Timestamp end = Utils.getEndDate(date);
         try{
 
             String sql = "insert into client_ntraffic(client, dat, incoming, outcoming) " +
                    "select cl.id, nn_summ.dat, sum(nn_summ.input), sum(nn_summ.output) from cl, nn_summ where " +
                    "nn_summ.network_id in (select id from networks where client=cl.id) " +
                    "and nn_summ.dat > ? and nn_summ.dat < ? and cl.id = ? group by 1, 2";
             log.info(logStr + " <<<<");
             List<Integer> clients = getNetworkedClients();
             PreparedStatement pstmt = con.prepareStatement(sql);
             for (Integer client : clients) {
                 log.debug("Client " + client);
                 start = getStartTimestamp(start, end, client);
                 pstmt.setTimestamp(1, start);
                 pstmt.setTimestamp(2, end);
                 pstmt.setInt(3, client);
                 log.debug("Minutes aggregation");
                 pstmt.executeUpdate();
                 log.debug("Minutes aggregation done");
             }
             pstmt.close();
         } catch (SQLException e) {
             log.error(logStr + " Aggregation error: " + e.getMessage());
             e.printStackTrace(System.err);
         }
         log.info(logStr + " >>>>");
     }
 
     private Timestamp getStartTimestamp(Timestamp start, Timestamp end, Integer client) {
         Timestamp result = null;
         log.debug("Getting real start ts");
         String maxDate = "select max(dat) from client_ntraffic where dat between ? and ? and client = ?";
         try{
             PreparedStatement pst = con.prepareStatement(maxDate);
             pst.setTimestamp(1, start);
             pst.setTimestamp(2, end);
             pst.setInt(3, client);
             ResultSet rs = pst.executeQuery();
 
             if (rs.next()){
                 result = rs.getTimestamp(1);
             }
         }catch (SQLException e){
             log.error(" Aggregation error: " + e.getMessage());
             e.printStackTrace(System.err);
         }
 
         if (result == null){
             log.debug("Impossible to find start within interval: " + start + " " + end);
             result = start;
         }
         log.debug("Real start is: " + result);
         return result;
     }
 
     private boolean hasRecord(Timestamp dat, String host, Integer networkId){
         boolean result = false;
         try {
             PreparedStatement pstmt = con.prepareStatement("select count(*) from netflow_details where dat=? and host=? and network_id = ?");
             pstmt.setTimestamp(1, dat);
             pstmt.setString(2, host);
             pstmt.setInt(3, networkId);
             ResultSet rs = pstmt.executeQuery();
             if (rs.next()){
                 int count = rs.getInt(1);
                 result = count > 0;
             }
             rs.close();
             pstmt.close();
         } catch (SQLException e) {
             log.error("Query failed: " + e.getMessage());
         }
         return result;
     }
 
     public void doDailyAggregation(){
         log.debug("doDailyAggregation(): <<<<");
         try{
             List<AggregationRecord> results = getAggregationResults();
             List<AggregationRecord> toInsert = new ArrayList<AggregationRecord>();
             List<AggregationRecord> toUpdate = new ArrayList<AggregationRecord>();
             for (AggregationRecord result : results) {
                if (aggregationAlreadyStored(result)){
                    toUpdate.add(result);
                }else{
                    toInsert.add(result);
                }
             }
             addAggregationResults(toInsert);
             updateAggregationResults(toUpdate);
        } catch (SQLException e) {
            log.error("Query falied: " + e.getMessage());
        }
         log.debug("doDailyAggregation(): >>>>");
     }
 
     public void doDailyAggregation(Date d){
         log.debug("doDailyAggregation(): <<<<");
         try{
             List<AggregationRecord> results = getAggregationResults(d);
             List<AggregationRecord> toInsert = new ArrayList<AggregationRecord>();
             List<AggregationRecord> toUpdate = new ArrayList<AggregationRecord>();
             for (AggregationRecord result : results) {
                if (aggregationAlreadyStored(result)){
                    toUpdate.add(result);
                }else{
                    toInsert.add(result);
                }
             }
             addAggregationResults(toInsert);
             updateAggregationResults(toUpdate);
        } catch (SQLException e) {
            log.error("Query falied: " + e.getMessage());
        }
         log.debug("doDailyAggregation(): >>>>");
     }
 
     private void updateAggregationResults(List<AggregationRecord> records) throws SQLException {
         if (records.isEmpty()){
             log.debug("Nothing to update");
             return;
         }
         log.debug("updateAggregationResults(): <<<<");
         log.debug(records.size() + " to update");
         PreparedStatement pstmt = con.prepareStatement("update ntraffic_by_day set input = ?, output = ? where client_id = ? and dat = ?");
         for (AggregationRecord record : records) {
             pstmt.setLong(1, record.getInput());
             pstmt.setLong(2, record.getOutput());
             pstmt.setInt(3, record.getClientId());
             pstmt.setDate(4, record.getDate());
             pstmt.addBatch();
         }
         final int[] ints = pstmt.executeBatch();
         log.debug(ints.length + " records updated");
         log.debug("updateAggregationResults(): >>>>");
 
     }
 
     private void addAggregationResults(List<AggregationRecord> records) throws SQLException {
         if (records.isEmpty()){
             log.debug("Nothing to insert");
             return;
         }
 
         log.debug("insertAggregationResults(): <<<<");
         log.debug(records.size() + " to insert");
         PreparedStatement pstmt = con.prepareStatement("insert into ntraffic_by_day(input, output, client_id, dat) values(?, ?, ?, ?)");
         for (AggregationRecord record : records) {
             pstmt.setLong(1, record.getInput());
             pstmt.setLong(2, record.getOutput());
             pstmt.setInt(3, record.getClientId());
             pstmt.setDate(4, record.getDate());
             pstmt.addBatch();
         }
         final int[] ints = pstmt.executeBatch();
         log.debug(ints.length + " records inserted");
         log.debug("insertAggregationResults(): >>>>");
     }
 
     private List<AggregationRecord> getAggregationResults() throws SQLException {
         log.debug("getAggregationResults(): <<<");
         List<Integer> clients = getNetworkedClients();
         
         String collect = "select client, date_trunc('day', dat)::date as dat,  sum(incoming) as input, sum(outcoming) " +
                 "as output from client_ntraffic where dat >= date_trunc('day', now())::timestamp and client = ? group by 1,2";
 
         
         PreparedStatement ps = con.prepareStatement(collect);
 
         List<AggregationRecord> results = new ArrayList<AggregationRecord>();
         for (Integer id : clients){
                 ps.setInt(1, id);
           ResultSet rs = ps.executeQuery();
           if (rs.next()){
             results.add(new AggregationRecord(rs.getInt(1), rs.getDate(2), rs.getLong(3), rs.getLong(4)));
           }
           rs.close();
         }
           ps.close();
         log.debug("getAggregationResults(): >>>");
         return results;
     }
 
     private List<Integer> getNetworkedClients() throws SQLException {
         log.debug("Getting user list");
 
         String unq = "select distinct client from networks";
 
         List<Integer> clients = new ArrayList<Integer>();
         PreparedStatement pst = con.prepareStatement(unq);
         ResultSet rst = pst.executeQuery();
 
         while(rst.next()){
                 clients.add(rst.getInt(1));
         }
 
         rst.close();
         pst.close();
         return clients;
     }
 
     private List<AggregationRecord> getAggregationResults(Date date) throws SQLException {
         if (date == null){
             return getAggregationResults();
         }
         log.debug("getAggregationResults(date): <<<");
         log.debug("Getting user list");
 
         Timestamp start = Utils.getStartDate(date);
         Timestamp end = Utils.getEndDate(date);
         log.debug("Parameters: " + start + ", " + end);
 
         List<Integer> clients = getNetworkedClients();
         String collect = "select client, date_trunc('day', dat)::date as dat,  sum(incoming) as input, sum(outcoming) " +
                 "as output from client_ntraffic where dat between ? and ? and client = ? group by 1,2";
         PreparedStatement ps = con.prepareStatement(collect);
 
         List<AggregationRecord> results = new ArrayList<AggregationRecord>();
         for (Integer id : clients){
             ps.setTimestamp(1, start);
             ps.setTimestamp(2, end);
             ps.setInt(3, id);
           ResultSet rs = ps.executeQuery();
           if (rs.next()){
             results.add(new AggregationRecord(rs.getInt(1), rs.getDate(2), rs.getLong(3), rs.getLong(4)));
           }
           rs.close();
         }
           ps.close();
         log.debug("getAggregationResults(): >>>");
         return results;
     }
 
 
     private boolean aggregationAlreadyStored(AggregationRecord record) throws SQLException {
        String query = "select count(*) from ntraffic_by_day where client_id = ? and dat = ?";
         PreparedStatement ps = con.prepareStatement(query);
         ps.setInt(1, record.getClientId());
         ps.setDate(2, record.getDate());
         ResultSet rs = ps.executeQuery();
         rs.next();
         int result = rs.getInt(1);
         return result > 0;
     }
 
     public void close(){
         try{
             con.close();
         } catch (SQLException e) {
             e.printStackTrace(System.err);
         }
     }
 
     private class AggregationRecord {
         private int clientId;
         private java.sql.Date date;
         private java.sql.Timestamp stamp;
         private long input;
         private long output;
 
         public AggregationRecord(int clientId, java.sql.Date date, long input, long output) {
             this.clientId = clientId;
             this.date = date;
             this.input = input;
             this.output = output;
         }
 
         public AggregationRecord(int clientId, Timestamp date, long input, long output) {
             this.clientId = clientId;
             this.stamp = date;
             this.input = input;
             this.output = output;
         }
 
         public int getClientId() {
             return clientId;
         }
 
         public java.sql.Date getDate() {
             return date;
         }
 
         public long getInput() {
             return input;
         }
 
         public long getOutput() {
             return output;
         }
 
         public Timestamp getStamp() {
             return stamp;
         }
 
         public void setStamp(Timestamp stamp) {
             this.stamp = stamp;
         }
     }
 }
