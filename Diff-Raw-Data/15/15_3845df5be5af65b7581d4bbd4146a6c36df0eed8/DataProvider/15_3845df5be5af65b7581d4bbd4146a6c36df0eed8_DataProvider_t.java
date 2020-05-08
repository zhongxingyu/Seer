 package ru.exorg.core.service;
 
 import org.springframework.jdbc.core.JdbcOperations;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import ru.exorg.core.model.Location;
 import ru.exorg.core.model.POI;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import ru.exorg.core.model.City;
 import ru.exorg.core.model.LatLng;
 
 // ================================================================================
 
 public class DataProvider {
     private JdbcTemplate jdbc;
     private Connection conn;
 
     private POIProvider poiProvider;
     private CafeProvider cafeProvider;
 
     private static CityMapper cityMapper = new CityMapper();
 
     private static class CityMapper implements RowMapper<City> {
         public City mapRow(ResultSet rs, int rowNum) throws SQLException {
             City city = new City();
 
             city.setId(rs.getLong("id"));
             city.setName(rs.getString("name"));
             city.setNeLatLng(new LatLng(rs.getDouble("ne_lat"), rs.getDouble("ne_lng")));
             city.setSwLatLng(new LatLng(rs.getDouble("sw_lat"), rs.getDouble("sw_lng")));
             city.setLatSubdivs(rs.getInt("lat_subdiv"));
             city.setLngSubdivs(rs.getInt("lng_subdiv"));
 
             return city;
         }
     }
 
     public DataProvider(JdbcTemplate jdbcTemplate) throws Exception {
         this.jdbc = jdbcTemplate;
         this.conn = this.jdbc.getDataSource().getConnection();
 
         this.poiProvider = new POIProvider(this);
         this.cafeProvider = new CafeProvider(this);
     }
 
     public POIProvider getPOIProvider() {
         return poiProvider;
     }
 
     public CafeProvider getCafeProvider() {
         return cafeProvider;
     }
 
     public JdbcOperations getJdbcOperations() {
         return jdbc;
     }
     
     public JdbcTemplate getJdbcTemplate() {
         return jdbc;
     }
 
     public Connection getConnection() {
         return conn;
     }
 
     public boolean isWithinCity(long cityId, final Location loc) {
         City city = this.queryCity(cityId);
 
         LatLng sw = city.getSwLatLng();
         LatLng ne = city.getNeLatLng();
 
         return 
             (loc.getLat() >= sw.getLat() && loc.getLat() <= ne.getLat())
             && 
             (loc.getLng() >= sw.getLng() && loc.getLng() <= ne.getLng());
     }
 
     public long getCityId(final String cityName) throws Exception {
         return jdbc.queryForLong(
                                  "SELECT id FROM city WHERE name=?;",
                                  new Object[]{cityName});
     }
 
     public City queryCity(long cityId) {
         return this.jdbc.query("SELECT * FROM city WHERE id=?;", new Object[]{cityId}, cityMapper).get(0);
     }
 
     public void guessPOIType(POI poi) {
         try {
            int t = this.jdbc.queryForInt("SELECT id FROM poi_type WHERE lcase(?) rlike poi_type.guess_rx;",
                                           poi.getName());
             if (t != 0) {
                 poi.setType(t);
             }
         } catch (Exception e) {
             /* Query result is empty */
             poi.setType(1);
         }
     }
 }
