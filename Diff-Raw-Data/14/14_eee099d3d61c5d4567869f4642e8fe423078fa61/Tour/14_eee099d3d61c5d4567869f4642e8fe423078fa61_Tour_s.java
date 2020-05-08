 package models;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.CopyOption;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import org.codehaus.jackson.annotate.JsonBackReference;
 import org.codehaus.jackson.annotate.JsonManagedReference;
 
 import models.form.TourForm;
 import play.Logger;
 import play.data.format.Formats;
 import play.db.ebean.Model;
 import utils.DateUtils;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.SqlRow;
 
 @Entity
 public class Tour extends Model {
 
     public static final String DATE_FORMAT = "yyyy-MM-dd";
 
     private static final long serialVersionUID = 1L;
 
     public static Finder<Long, Tour> find = new Finder<Long, Tour>(Long.class, Tour.class);
 
     @Id
     public long id;
 
     public String name;
 
     @Formats.DateTime(pattern = DATE_FORMAT)
     public Date date;
 
     public double price;
 
     @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
     public Location location;
 
     @ManyToOne(fetch = FetchType.EAGER)
     public User guide;
 
     @Column(columnDefinition = "TEXT")
     public String descriptionFull;
 
     @Column(columnDefinition = "TEXT")
     public String descriptionShort;
 
     public String descriptionMini;
 
     public String photoName;
 
     @JsonManagedReference
     @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "tour")
     public List<Review> reviews;
 
     @JsonBackReference
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "tour_to_tourist", joinColumns = @JoinColumn(name = "tour_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "username", referencedColumnName = "username"))
     public List<User> tourists;
 
     public String getDateFormatted() {
         DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, new Locale("de"));
         return df.format(date);
     }
 
     public Boolean join(User user) {
         try {
             Ebean.beginTransaction();
             tourists.add(user);
             user.tours.add(this);
             update();
             user.update();
             Ebean.commitTransaction();
             return true;
         } finally {
             Ebean.endTransaction();
         }
     }
 
     public Boolean leave(User user) {
         try {
             Ebean.beginTransaction();
             tourists.remove(user);
             user.tours.remove(this);
             update();
             user.update();
             Ebean.commitTransaction();
             return true;
         } finally {
             Ebean.endTransaction();
         }
     }
 
     public static List<String> findActiveTourCountries() {
         String formattedDate = DateUtils.getCurrentFormattedDate();
 
         String sql = "SELECT DISTINCT country FROM Location WHERE id IN (SELECT location_id FROM Tour WHERE date >= '"
                 + formattedDate + "')";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<String> countries = new ArrayList<String>();
         for (SqlRow row : sqlRows) {
             countries.add(row.getString(Location.COUNTRY_FIELD));
         }
 
         return countries;
     }
 
     public static List<String> findActiveTourCitiesByCountry(String country) {
         String formattedDate = DateUtils.getCurrentFormattedDate();
 
         String sql = "SELECT DISTINCT city FROM Location WHERE id IN (SELECT location_id FROM Tour WHERE date >= '"
                 + formattedDate + "') AND country = '" + country + "'";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<String> cities = new ArrayList<String>();
         for (SqlRow row : sqlRows) {
             cities.add(row.getString(Location.CITY_FIELD));
         }
 
         return cities;
     }
 
     public static List<Tour> findActiveToursByCountry(String country) {
         String formattedDate = DateUtils.getCurrentFormattedDate();
 
         String sql = "SELECT id FROM Location WHERE country='" + country + "'";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<Integer> ids = new ArrayList<>();
         for (SqlRow row : sqlRows) {
             ids.add(row.getInteger("id"));
         }
 
         List<Tour> tours = Ebean.createQuery(Tour.class).where().gt("date", formattedDate).in("location_id", ids)
                 .findList();
 
         return tours;
     }
 
     public static List<Tour> findActiveToursByCountryAndCity(String country, String city) {
         String formattedDate = DateUtils.getCurrentFormattedDate();
 
        String sql = "SELECT id FROM Location WHERE country='" + country + "' AND city='" + city + "'";
         List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
 
         List<Integer> ids = new ArrayList<>();
         for (SqlRow row : sqlRows) {
            ids.add(row.getInteger("id"));
         }
 
         List<Tour> tours = Ebean.createQuery(Tour.class).where().gt("date", formattedDate).in("location_id", ids)
                 .findList();
 
         return tours;
     }
 
     public static void create(TourForm tourForm, User guide) {
         Tour tour = new Tour();
         tour.date = tourForm.date;
         tour.descriptionFull = tourForm.description;
         tour.descriptionShort = tourForm.description.substring(0, tourForm.description.length()/2);
         tour.descriptionMini = tourForm.description.substring(0, tourForm.description.length() > 1020 ? 255 : tourForm.description.length()/4);
         tour.guide = guide;
         Location location = new Location();
         location.country = tourForm.country;
         location.city = tourForm.city;
         tour.location = location;
         tour.name = tourForm.name;
         tour.price = tourForm.price;
         tour.reviews = new ArrayList<>();
         tour.tourists = new ArrayList<>();
 
         // making sure we don't have two files with the same name
         tourForm.name = tourForm.name.replaceAll("\\s", "_").toLowerCase();
         String targetFileName = tourForm.name + ".jpg";
         File locationDirectory = new File("public/images/locations");
         List<String> files = Arrays.asList(locationDirectory.list());
         int i = 1;
         while (files.contains(targetFileName)) {
             targetFileName = tourForm.name + "_" + i++ + ".jpg";
         }
 
         String targetFile = locationDirectory.getAbsolutePath() + "\\" + targetFileName;
         tour.photoName = "/assets/images/locations/" + targetFileName;
         Logger.info("tour.photoName: " + tour.photoName);
 
         Ebean.save(tour);
 
         // saving file to DB
         Path source = Paths.get(tourForm.photoName);
         Path target = Paths.get(targetFile);
         CopyOption[] options = new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES };
         try {
             Files.copy(source, target, options);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 3;
         result = prime * result + ((date == null) ? 0 : date.hashCode());
         result = prime * result + ((descriptionFull == null) ? 0 : descriptionFull.hashCode());
         result = prime * result + ((descriptionMini == null) ? 0 : descriptionMini.hashCode());
         result = prime * result + ((descriptionShort == null) ? 0 : descriptionShort.hashCode());
         result = prime * result + (int) (id ^ (id >>> 32));
         result = prime * result + ((location == null) ? 0 : location.hashCode());
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         result = prime * result + ((photoName == null) ? 0 : photoName.hashCode());
         long temp;
         temp = Double.doubleToLongBits(price);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         result = prime * result + ((reviews == null) ? 0 : reviews.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!super.equals(obj)) return false;
         if (getClass() != obj.getClass()) return false;
         Tour other = (Tour) obj;
         if (date == null) {
             if (other.date != null) return false;
         } else if (!date.equals(other.date)) return false;
         if (descriptionFull == null) {
             if (other.descriptionFull != null) return false;
         } else if (!descriptionFull.equals(other.descriptionFull)) return false;
         if (descriptionMini == null) {
             if (other.descriptionMini != null) return false;
         } else if (!descriptionMini.equals(other.descriptionMini)) return false;
         if (descriptionShort == null) {
             if (other.descriptionShort != null) return false;
         } else if (!descriptionShort.equals(other.descriptionShort)) return false;
         if (guide == null) {
             if (other.guide != null) return false;
         }
         if (id != other.id) return false;
         if (location == null) {
             if (other.location != null) return false;
         } else if (!location.equals(other.location)) return false;
         if (name == null) {
             if (other.name != null) return false;
         } else if (!name.equals(other.name)) return false;
         if (photoName == null) {
             if (other.photoName != null) return false;
         } else if (!photoName.equals(other.photoName)) return false;
         if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price)) return false;
         if (reviews == null) {
             if (other.reviews != null) return false;
         } else if (!reviews.equals(other.reviews)) return false;
         if (tourists == null) {
             if (other.tourists != null) return false;
         } else if (!tourists.equals(other.tourists)) return false;
         return true;
     }
 
 }
