 package com.zibea.parser.dao;
 
 import com.zibea.parser.core.exception.BatchException;
 import com.zibea.parser.model.domain.*;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.*;
 
 /**
  * @author: Mikhail Bragin
  */
 public class RealtyDao {
 
     private DataSourceKeeper dataSource;
 
     public RealtyDao() throws Exception {
         dataSource = new DataSourceKeeper();
     }
 
     public void saveBatch(List<RealtyObject> objects) {
         Connection con = dataSource.getConnection();
         try {
             String tableName = null;
             String schemaName = "realty";
 
             String SQL = null;
             if (objects != null && !objects.isEmpty()) {
                 if (objects.get(0) instanceof Apartment) {
                     tableName = schemaName + ".Apartments";
                     SQL = "INSERT INTO " + tableName + " VALUES (?,?,?)";
                 }
                 if (objects.get(0) instanceof State) {
                     tableName = schemaName + ".States";
                     SQL = "INSERT INTO " + tableName + " VALUES (?,?,?)";
                 }
                 if (objects.get(0) instanceof City) {
                     tableName = schemaName + ".Cities";
                     SQL = "INSERT INTO " + tableName + "(id, state_id, title, url_param) VALUES (?,?,?,?)";
                 }
 
                 if (objects.get(0) instanceof District) {
                     tableName = schemaName + ".Districts";
                     SQL = "INSERT INTO " + tableName + "(id, city_id, title, url_param) VALUES (?,?,?,?)";
                 }
             }
             if (tableName != null) {
 
                 PreparedStatement stmt = con.prepareStatement(SQL);
 
                 for (RealtyObject object : objects) {
                     if (object instanceof City) {
                         stmt.setLong(1, object.getId());
                         stmt.setLong(2, ((City) object).getStateId());
                         stmt.setString(3, object.getTitle());
                         stmt.setString(4, object.getUrlParam());
                     } else if (object instanceof District) {
                         stmt.setLong(1, object.getId());
                         stmt.setLong(2, ((District) object).getCityId());
                         stmt.setString(3, object.getTitle());
                         stmt.setString(4, object.getUrlParam());
                     } else {
                         stmt.setLong(1, object.getId());
                         stmt.setString(2, object.getTitle());
                         stmt.setString(3, object.getUrlParam());
                     }
                     stmt.addBatch();
                 }
 
                 int[] rs = stmt.executeBatch();
                 con.commit();
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
     }
 
     public List<State> getAllStates() {
         List<State> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.States";
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new State(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param")));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public List<City> getAllCities() {
         List<City> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.Cities";
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new City(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param"),
                         rs.getLong("state_id")));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public List<City> getCityByState(State state) {
         List<City> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.Cities WHERE state_id = " + state.getId();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new City(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param"),
                         rs.getLong("state_id")));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public List<Apartment> getAllApartments() {
         List<Apartment> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.Apartments";
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new Apartment(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param")));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public List<Transaction> getAllTransactions() {
         List<Transaction> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.Transactions";
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new Transaction(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param")));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public List<District> getDistrictsByCity(City city) {
         List<District> result = new ArrayList<>();
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT * FROM realty.Districts WHERE city_id = " + city.getId();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(new District(
                         rs.getLong("id"),
                         rs.getString("title"),
                         rs.getString("url_param"),
                         city));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
 
         return result;
     }
 
     public Set<Long> getAllSavedOfferIds() {
         System.out.println("Getting saved offers ids");
         Set<Long> result = new HashSet<>();
 
         Connection con = dataSource.getConnection();
         try {
 
             String SQL = "SELECT id FROM realty.Offers";
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL);
 
             while (rs.next()) {
                 result.add(rs.getLong("id"));
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         } finally {
             DataSourceKeeper.closeConnection(con);
         }
         System.out.println("Got saved offers ids size=" + result.size());
         return result;
 
 
     }
 
     public Set<Long> saveBatch(Set<Offer> batch) throws SQLException {
         Set<Long> savedOffers = new HashSet<>(batch.size());
        System.out.println("Flushing batch size=" + batch.size());
         Connection connection = dataSource.getConnection();
         PreparedStatement stmt = null;
         try {
             String tableName = null;
             String schemaName = "realty";
 
             String SQL = null;
             if (!batch.isEmpty()) {
                 tableName = schemaName + ".Offers";
                 SQL = "INSERT INTO " + tableName + "(" +
                         "id, " +
                         "state_id, " +
                         "city_id, " +
                         "district_id, " +
                         "apartment_id, " +
                         "transaction_id," +
                         "zap_url," +
                         "price," +
                         "service_fee," +
                         "room_number," +
                         "vaga_number," +
                         "total_area," +
                         "year_built," +
                         "floor_number," +
                         "iptu_fee," +
                         "square_meter_price," +
                         "google_map_url," +
                         "ts_published," +
                         "street," +
                         "images," +
                         "contact_phones," +
                         "contact_name ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             }
 
 
             if (tableName != null) {
 
                 stmt = connection.prepareStatement(SQL);
 
                 for (Offer offer : batch) {
 
                     if (savedOffers.contains(offer.getId()))
                         continue;
 
                     stmt.setLong(1, offer.getId());
                     stmt.setLong(2, (offer.getState().getId()));
                     stmt.setLong(3, (offer.getCity().getId()));
                     stmt.setLong(4, (offer.getDistrict().getId()));
                     stmt.setLong(5, (offer.getApartment().getId()));
                     stmt.setLong(6, (offer.getTransaction().getId()));
                     stmt.setString(7, offer.getUrl());
 
                     if (offer.getPrice() != null)
                         stmt.setDouble(8, offer.getPrice());
                     else
                         stmt.setNull(8, Types.DOUBLE);
 
                     if (offer.getServiceFee() != null)
                         stmt.setDouble(9, offer.getServiceFee());
                     else
                         stmt.setNull(9, Types.DOUBLE);
 
                     if (offer.getRoomNumber() != null)
                         stmt.setInt(10, offer.getRoomNumber());
                     else
                         stmt.setNull(10, Types.INTEGER);
 
                     if (offer.getVagaNumber() != null)
                         stmt.setInt(11, offer.getVagaNumber());
                     else
                         stmt.setNull(11, Types.INTEGER);
 
                     if (offer.getTotalArea() != null)
                         stmt.setInt(12, offer.getTotalArea());
                     else
                         stmt.setNull(12, Types.INTEGER);
 
                     if (offer.getYearBuilt() != null)
                         stmt.setInt(13, offer.getYearBuilt());
                     else
                         stmt.setNull(13, Types.INTEGER);
 
                     if (offer.getFloorNumber() != null)
                         stmt.setInt(14, offer.getFloorNumber());
                     else
                         stmt.setNull(14, Types.INTEGER);
 
                     if (offer.getIptuFee() != null)
                         stmt.setInt(15, offer.getIptuFee());
                     else
                         stmt.setNull(15, Types.INTEGER);
 
                     if (offer.getPricePerSquareMeter() != null)
                         stmt.setDouble(16, offer.getPricePerSquareMeter());
                     else
                         stmt.setNull(16, Types.DOUBLE);
 
                     if (offer.getGoogleMapUrl() != null)
                         stmt.setString(17, offer.getGoogleMapUrl());
                     else
                         stmt.setNull(17, Types.VARCHAR);
 
                     if (offer.getTsPublished() != null)
                         stmt.setLong(18, offer.getTsPublished());
                     else
                         stmt.setLong(18, System.currentTimeMillis());
 
                     if (offer.getStreetAddress() != null)
                         stmt.setString(19, offer.getStreetAddress());
                     else
                         stmt.setNull(19, Types.VARCHAR);
 
                     if (offer.getImagesHashes() != null && !offer.getImagesHashes().isEmpty()) {
                         StringBuilder sb = new StringBuilder();
                         for (String hash : offer.getImagesHashes()) {
                             sb.append(hash);
                             sb.append(",");
                         }
                         sb.deleteCharAt(sb.length() - 1); //remove last index
                         stmt.setString(20, sb.toString());
                     } else
                         stmt.setNull(20, Types.VARCHAR);
 
                     if (offer.getContactPhones() != null && !offer.getContactPhones().isEmpty()) {
                         StringBuilder sb = new StringBuilder();
                         for (String phone : offer.getContactPhones()) {
                             sb.append(phone);
                             sb.append(",");
                         }
                         sb.deleteCharAt(sb.length() - 1); //remove last index
                         stmt.setString(21, sb.toString());
                     } else
                         stmt.setNull(21, Types.VARCHAR);
 
                     if (offer.getContactName() != null)
                         stmt.setString(22, offer.getContactName());
                     else
                         stmt.setNull(22, Types.VARCHAR);
 
                     stmt.addBatch();
                 }
                 int[] rs = stmt.executeBatch();
             }
             connection.commit();
 
             for (Offer offer : batch)
                 savedOffers.add(offer.getId());
 
         } finally {
             if (stmt != null) {
                 try {
                     stmt.close();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
             dataSource.closeConnection(connection);
         }
 
         System.out.println("Flushed");
         return savedOffers;
     }
 }
