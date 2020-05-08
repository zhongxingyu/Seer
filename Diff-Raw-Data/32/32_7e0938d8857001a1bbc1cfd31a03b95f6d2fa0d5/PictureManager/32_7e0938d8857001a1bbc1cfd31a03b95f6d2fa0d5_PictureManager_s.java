 package be.noselus.pictures;
 
 import be.noselus.db.DatabaseHelper;
import be.noselus.service.Service;
 import net.coobird.thumbnailator.Thumbnails;
 import net.coobird.thumbnailator.geometry.Positions;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.TreeMap;
 
 @Singleton
 public class PictureManager implements Service {
 
     private final DatabaseHelper dbHelper;
     private Map<Integer, Integer> mapping = null;
 
     @Inject
     public PictureManager(final DatabaseHelper dbHelper) {
         this.dbHelper = dbHelper;
     }
 
     @Override
     public void start() {
         try (Connection db = dbHelper.getConnection(false, true);
              PreparedStatement stat = db.prepareStatement("SELECT id, assembly_id FROM person;")) {
 
             stat.execute();
 
             mapping = new TreeMap<>();
             while (stat.getResultSet().next()) {
                 int id = stat.getResultSet().getInt("id");
                 int assembly_id = stat.getResultSet().getInt("assembly_id");
                 mapping.put(id, assembly_id);
             }
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void stop() {
     }
 
     public InputStream get(int id) {
         final ImageInfo imageInfo = new ImageInfo(id).invoke();
         final String path = imageInfo.getPath();
         final String ext = imageInfo.getExt();
 
         if (path != null && ext != null) {
             return PictureManager.class.getResourceAsStream(path + mapping.get(id) + ext);
         } else {
             return null;
         }
     }
 
     public void get(int id, int width, int height, OutputStream os) throws IOException {
         final ImageInfo imageInfo = new ImageInfo(id).invoke();
         final String path = imageInfo.getPath();
         final String ext = imageInfo.getExt();
 
         if (path != null && ext != null) {
             Thumbnails.of(PictureManager.class.getResourceAsStream(path + mapping.get(id) + ext))
                     .size(width, height)
                     .crop(Positions.CENTER)
                     .outputFormat("jpg")
                     .toOutputStream(os);
         }
     }
 
     private class ImageInfo {
         private final int id;
         private String path;
         private String ext;
 
         public ImageInfo(final int id) {
             this.id = id;
         }
 
         public String getPath() {
             return path;
         }
 
         public String getExt() {
             return ext;
         }
 
         public ImageInfo invoke() {
             if (id >= 77 && id <= 150) {
                 path = "/pictures/parlement/";
                 ext = ".jpg";
             } else if (id >= 151 && id <= 158) {
                 path = "/pictures/minister/";
                 ext = ".jpg";
             } else if (id >= 849 && id <= 998) {
                 path = "/pictures/chamber/";
                 ext = ".gif";
             }
             return this;
         }
     }
 }
