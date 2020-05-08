 package datalayer;
 
 import entities.Entity;
 import entities.Ip;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.TreeSet;
 import javax.swing.JLabel;
 import sql.Connector;
 
 /**
  * The Ip Entity Data Layer Class
  *
  * @author alexhughes
  */
 public class IpDL extends DataLayer {
 
     private ArrayList<Connector> connections;
     private JLabel statusL = null;
 
     public IpDL(Connector aConnector) {
         super(aConnector);
     }
 
     public IpDL(Connector aConnector, Entity anEntity) {
         super(aConnector, anEntity);
     }
 
     @Override
     public Entity fetch() throws SQLException {
         Ip ip = (Ip) e;
 
         String query = ""
                 + "SELECT * "
                 + "FROM ip "
                 + "WHERE ip = ? AND agent = ? AND domain = ? ";
 
         PreparedStatement ps = c.prepareStatement(query);
 
         ps.setString(1, ip.getIp());
         ps.setString(2, ip.getAgent());
         ps.setString(3, ip.getDomain());
 
         ResultSet ipR = ps.executeQuery();
         e = resultSetToEntity(ipR).get(0);
 
         return e;
     }
 
     @Override
     public ArrayList<Entity> fetchList(String aSorting) throws SQLException {
         String query = ""
                 + "SELECT * "
                 + "FROM ip "
                 + "ORDER BY " + aSorting;
 
         ResultSet ipR = c.sendQuery(query);
         entities = resultSetToEntity(ipR);
 
         return entities;
     }
 
     @Override
     public String prepareSearchQuery(String aSorting) {
         Ip ip = (Ip) e;
 
         String query = ""
                 + "SELECT * "
                 + "FROM ip "
                 + "WHERE 1=1 ";
 
         if (ip.getIp() != null && !ip.getIp().equals("")) {
             query += " AND ip LIKE '%" + ip.getIp() + "%' ";
         }
 
         if (ip.getAgent() != null && !ip.getAgent().equals("")) {
             query += " AND agent LIKE '%" + ip.getAgent() + "%' ";
         }
 
         if (ip.getDomain() != null && !ip.getDomain().equals("")) {
             query += " AND domain LIKE '%" + ip.getDomain() + "%' ";
         }
 
         if (ip.getProcessed() != Entity.NIL) {
             query += " AND Processed = " + ip.getProcessed();
         }
 
         int floor = ip.getHits() - 10;
         int ceiling = ip.getHits() + 10;
         if (ip.getHits() != Entity.NIL) {
             query += " AND Hits BETWEEN " + floor + " AND " + ceiling;
         }
 
         if (ip.getRequest() != Entity.NIL) {
             query += " AND Request = " + ip.getRequest();
         }
 
         if (ip.getHostname() != null && !ip.getHostname().equals("")) {
             query += " AND Hostname LIKE '%" + ip.getHostname() + "%' ";
         }
 
         if (ip.getCity() != null && !ip.getCity().equals("")) {
             query += " AND City LIKE '%" + ip.getCity() + "%' ";
         }
 
         if (ip.getRegion() != null && !ip.getRegion().equals("")) {
             query += " AND Region LIKE '%" + ip.getRegion() + "%' ";
         }
 
         if (ip.getCountry() != null && !ip.getCountry().equals("")) {
             query += " AND Country LIKE '%" + ip.getCountry() + "%' ";
         }
 
         if (ip.getCountryCode() != null && !ip.getCountryCode().equals("")) {
             query += " AND CountryCode LIKE '%" + ip.getCountryCode() + "%' ";
         }
 
         double latFloor = ip.getLatitude() - 100;
         double latCeiling = ip.getLongitude() + 100;
         if (ip.getLatitude() != Entity.NIL) {
             query += " AND Latitude BETWEEN " + latFloor + " AND " + latCeiling;
         }
 
         double longFloor = ip.getLongitude() - 100;
         double longCeiling = ip.getLongitude() + 100;
         if (ip.getLongitude() != Entity.NIL) {
             query += " AND Longitude BETWEEN " + longFloor + " AND " + longCeiling;
         }
 
         if (ip.getDateCreated() != null) {
             query += " AND DateCreated LIKE '" + ip.getDateCreated().toString() + "%' ";
         }
 
         if (ip.getDateModified() != null) {
             query += " AND _dateModified LIKE '" + ip.getDateModified().toString() + "%' ";
         }
 
         if (aSorting != null) {
             query += "ORDER BY " + aSorting;
         }
 
         return query;
     }
 
     @Override
     public ArrayList<Entity> search() throws SQLException {
         String query = prepareSearchQuery("");
 
         ResultSet entityR = c.sendQuery(query);
         entities = resultSetToEntity(entityR);
 
         return entities;
     }
 
     @Override
     public ResultSet search(String aSorting) throws SQLException {
         String query = prepareSearchQuery(aSorting);
 
         return c.sendQuery(query);
     }
 
     @Override
     public int insert() throws SQLException {
         int id = Entity.NIL;
         Ip ip = (Ip) e;
 
         String query = ""
                 + "INSERT INTO ip (ip, agent, domain, Processed, Hits, Request, "
                 + "RequestSource, Hostname, City, Region, Country, CountryCode, "
                 + "Latitude, Longitude, PostCode, Timezone, DateCreated) VALUES "
                 + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) ";
 
         PreparedStatement ps = c.prepareStatement(query);
 
         ps.setString(1, ip.getIp());
         ps.setString(2, ip.getAgent());
         ps.setString(3, ip.getDomain());
         ps.setInt(4, ip.getProcessed());
         ps.setInt(5, ip.getHits());
         ps.setInt(6, ip.getRequest());
         ps.setString(7, ip.getRequestSource());
         ps.setString(8, ip.getHostname());
         ps.setString(9, ip.getCity());
         ps.setString(10, ip.getRegion());
         ps.setString(11, ip.getCountry());
         ps.setString(12, ip.getCountryCode());
         ps.setDouble(13, ip.getLatitude());
         ps.setDouble(14, ip.getLongitude());
         ps.setString(15, ip.getPostCode());
         ps.setString(16, ip.getTimezone());
 
         ps.executeUpdate();
 
         ResultSet ipR = ps.getGeneratedKeys();
         while (ipR.next()) {
             id = ipR.getInt(1);
         }
 
         return id;
     }
 
     @Override
     public void update() throws SQLException {
         Ip ip = (Ip) e;
         checkID(ip);
 
         String query = ""
                 + "UPDATE ip SET "
                 + "Processed = ?, "
                 + "Hits = ?, "
                 + "Request = ?, "
                 + "RequestSource = ?, "
                 + "Hostname = ?, "
                 + "City = ?, "
                 + "Region = ?, "
                 + "Country = ?, "
                 + "CountryCode = ?, "
                 + "Latitude = ?, "
                 + "Longitude = ?, "
                 + "PostCode = ?, "
                 + "Timezone = ? "
                 + "WHERE ip = ? AND domain = ? AND agent = ? ";
 
         PreparedStatement ps = c.prepareStatement(query);
 
         ps.setInt(1, ip.getProcessed());
         ps.setInt(2, ip.getHits());
         ps.setInt(3, ip.getRequest());
         ps.setString(4, ip.getRequestSource());
         ps.setString(5, ip.getHostname());
         ps.setString(6, ip.getCity());
         ps.setString(7, ip.getRegion());
         ps.setString(8, ip.getCountry());
         ps.setString(9, ip.getCountryCode());
         ps.setDouble(10, ip.getLatitude());
         ps.setDouble(11, ip.getLongitude());
         ps.setString(12, ip.getPostCode());
         ps.setString(13, ip.getTimezone());
         ps.setString(14, ip.getIp());
         ps.setString(15, ip.getDomain());
         ps.setString(16, ip.getAgent());
 
         ps.executeUpdate();
     }
 
     @Override
     public void delete() throws SQLException {
         Ip ip = (Ip) e;
         checkID(ip);
 
         String query = ""
                 + "DELETE "
                 + "FROM ip "
                 + "WHERE ip = ? AND domain = ? AND agent = ? ";
 
         PreparedStatement ps = c.prepareStatement(query);
 
         ps.setString(1, ip.getIp());
         ps.setString(2, ip.getDomain());
         ps.setString(3, ip.getAgent());
 
         ps.executeUpdate();
     }
 
     @Override
     public ArrayList<Entity> resultSetToEntity(ResultSet aR) throws SQLException {
         ArrayList<Entity> entityL = new ArrayList();
         Ip ip;
 
         while (aR.next()) {
             ip = new Ip(
                     aR.getString("ip"),
                     aR.getString("agent"),
                     aR.getString("domain"),
                     aR.getInt("Processed"),
                     aR.getInt("Hits"),
                     aR.getInt("Request"),
                     aR.getString("RequestSource"),
                     aR.getString("Hostname"),
                     aR.getString("City"),
                     aR.getString("Region"),
                     aR.getString("Country"),
                     aR.getString("CountryCode"),
                     aR.getDouble("Latitude"),
                     aR.getDouble("Longitude"),
                     aR.getString("PostCode"),
                     aR.getString("Timezone"),
                     aR.getTimestamp("DateCreated"),
                     aR.getTimestamp("_dateModified"));
             entityL.add(ip);
         }
 
         return entityL;
     }
 
     /**
      * Synchronises all the ips that reside to some of the partners with the
      * central database
      *
      * @throws SQLException
      */
     public void syncIps() throws SQLException {
         int ctr = 0;
         int size = 0;
         TreeSet<Ip> ipT = new TreeSet();
 
         //first fetching the central addresses
         ArrayList<Entity> ipL = fetchList("ip ASC ");
         for (Entity ent : ipL) {
             ipT.add((Ip) ent);
         }
 
         //moving to the partners
         for (Connector con : connections) {
             IpDL partnerIpDL = new IpDL(con);
 
             //fetching ips for each partner
             ArrayList<Entity> partnerIpL = partnerIpDL.fetchList("ip ASC ");
             ctr = 0;
             for (Entity partnerEnt : partnerIpL) {
                 ctr++;
                 size++;
                 Ip ip = (Ip) partnerEnt;
 
                 //updating status
                 if (statusL != null) {
                     String text = "Processing: " + ctr + "/" + partnerIpL.size() + " || " + ip.getIp() + " - "
                             + ip.getHostname() + " | " + ip.getDomain();
                    if (text != null && text.length() > 50) {
                        text = text.substring(0, 50);
                    }
                    statusL.setText(text);
                 }
 
                 //if the central repository does not have the ip, then we insert it
                 if (!ipT.contains(ip)) {
                     this.e = ip;
                     this.insert();
                     ipT.add(ip);
 
                     //finally deleting the newly added one
                     partnerIpDL = new IpDL(con, ip);
                     partnerIpDL.delete();
                 } else {
                     //if the central repository already contains the ip, then we 
                     //update the hits
                     addHits(ip);
                 }
             }
         }
         if (statusL != null) {
             statusL.setText(size + " entries processed. ");
             statusL = null;
         }
     }
 
     /**
      * Gathers hits from all connections and updates the central one.
      *
      * @param aHitN
      * @return
      * @throws SQLException
      */
     public int addHits(Ip anIp) throws SQLException {
         Ip ip = anIp;
         int totalHits = 0;
         PreparedStatement ps;
 
         String selQ = ""
                 + "SELECT Hits "
                 + "FROM ip "
                 + "WHERE ip = ? AND domain = ? AND agent = ? ";
 
         String updQ = ""
                 + "UPDATE ip "
                 + "SET Hits = ? "
                 + "WHERE ip = ? AND domain = ? AND agent = ? ";
 
         String delQ = ""
                 + "DELETE "
                 + "FROM ip "
                 + "WHERE ip = ? AND domain = ? AND agent = ? ";
 
         //getting hits from central server
         ps = c.prepareStatement(selQ);
 
         ps.setString(1, ip.getIp());
         ps.setString(2, ip.getDomain());
         ps.setString(3, ip.getAgent());
 
         ResultSet hitR = ps.executeQuery();
         while (hitR.next()) {
             totalHits += hitR.getInt(1);
         }
 
         //getting hits from all the agent servers
         for (Connector con : connections) {
             ps = con.prepareStatement(selQ);
 
             ps.setString(1, ip.getIp());
             ps.setString(2, ip.getDomain());
             ps.setString(3, ip.getAgent());
 
             hitR = ps.executeQuery();
             while (hitR.next()) {
                 totalHits += hitR.getInt(1);
             }
 
             //now setting them to be zero in order not to be counted again next time
             ps = con.prepareStatement(delQ);
 
             ps.setString(1, ip.getIp());
             ps.setString(2, ip.getDomain());
             ps.setString(3, ip.getAgent());
 
             ps.executeUpdate();
         }
 
         //finally updating total hits centrally
         ps = c.prepareStatement(updQ);
 
         ps.setInt(1, totalHits);
         ps.setString(2, ip.getIp());
         ps.setString(3, ip.getDomain());
         ps.setString(4, ip.getAgent());
 
         ps.executeUpdate();
 
         return totalHits;
     }
 
     /**
      * Helper function that checks whether an IP has everything it takes to be
      * updated or deleted
      *
      * @param anIp
      * @throws SQLException
      */
     private void checkID(Ip anIp) throws SQLException {
         if (anIp.getIp() == null || anIp.getAgent() == null || anIp.getDomain() == null) {
             throw new SQLException();
         }
     }
 
     public void setConnections(ArrayList<Connector> aConnectionL) {
         connections = aConnectionL;
     }
 
     public ArrayList<Connector> getConnections() {
         return connections;
     }
 
     public void setStatusL(JLabel aLabel) {
         statusL = aLabel;
     }
 }
