 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.SqlRow;
 
 @Entity
 public class Location extends Model {
 
     public static final String COUNTRY_FIELD = "country";
    public static final String CITY_FIELD = "city";
 
     private static final long serialVersionUID = -5592660318772583L;
 
     public static Finder<Long, Location> find = new Finder<Long, Location>(Long.class, Location.class);
 
     @Id
     public long id;
 
     public int ltd;
     public int lng;
 
     @Column(name = COUNTRY_FIELD)
     public String country;
 
     @Column(name = CITY_FIELD)
     public String city;
 
     public static List<String> findUniqueCountries() {
         String sql = "SELECT DISTINCT country FROM Location";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<String> countries = new ArrayList<String>();
         for (SqlRow row : sqlRows) {
             countries.add(row.getString(COUNTRY_FIELD));
         }
 
         return countries;
     }
 
     public static List<String> findUniqueCitiesOfCountry(String country) {
         String sql = "SELECT DISTINCT city FROM Location WHERE country = '" + country + "'";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<String> cities = new ArrayList<String>();
         for (SqlRow row : sqlRows) {
             cities.add(row.getString(CITY_FIELD));
         }
 
         return cities;
     }
 
     @Override
     public String toString() {
         return "Location [id=" + id + ", ltd=" + ltd + ", lng=" + lng + ", country=" + country + ", city=" + city + "]";
     }
 
 }
