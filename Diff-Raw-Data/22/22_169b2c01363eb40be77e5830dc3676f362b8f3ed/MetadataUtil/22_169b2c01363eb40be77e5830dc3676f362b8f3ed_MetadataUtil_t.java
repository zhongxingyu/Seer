 package ch.bergturbenthal.image.server;
 
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import com.drew.metadata.Directory;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.exif.ExifDirectory;
 import com.drew.metadata.exif.GpsDirectory;
 
 public class MetadataUtil {
   private static class TagId {
     private final Class<? extends Directory> directory;
     private final int tagId;
 
     public TagId(final Class<? extends Directory> directory, final int tagId) {
       this.directory = directory;
       this.tagId = tagId;
     }
 
   }
 
  private static Logger logger = LoggerFactory.getLogger(MetadataUtil.class);

   public static Date readCreateDate(final Metadata metadata) {
     final Date gpsDate = readGpsDate(metadata);
    if (gpsDate != null) {
      // logger.info("GPS-Date: " + gpsDate);
       return gpsDate;
    }
     for (final TagId index : Arrays.asList(new TagId(ExifDirectory.class, ExifDirectory.TAG_DATETIME_ORIGINAL), new TagId(ExifDirectory.class,
                                                                                                                           ExifDirectory.TAG_DATETIME))) {
       final Date date = readDate(metadata, index.directory, index.tagId);
      if (date != null) {
        // logger.info(index.directory.getSimpleName() + ":" + index.tagId +
        // ": " + date);
         return date;
      }
     }
     return null;
 
   }
 
   private static Date readDate(final Metadata metadata, final Class<? extends Directory> directory, final int tag) {
     try {
       if (metadata.containsDirectory(directory)) {
         final Directory directory2 = metadata.getDirectory(directory);
         if (directory2.containsTag(tag))
           try {
             return directory2.getDate(tag);
           } catch (final MetadataException e) {
             throw new RuntimeException("Cannot read " + directory.getName() + ":" + directory2.getDescription(tag), e);
           }
       }
       return null;
     } catch (final MetadataException e) {
       throw new RuntimeException("Cannot read " + directory.getName() + ":" + tag, e);
     }
   }
 
   private static Date readGpsDate(final Metadata metadata) {
     try {
       if (!metadata.containsDirectory(GpsDirectory.class))
         return null;
       final Directory directory = metadata.getDirectory(GpsDirectory.class);
       if (!directory.containsTag(GpsDirectory.TAG_GPS_TIME_STAMP))
         return null;
       final int[] time = directory.getIntArray(7);
       final String date = directory.getString(29);
       final Object[] values = new MessageFormat("{0,number}:{1,number}:{2,number}").parse(date);
       final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      calendar.set(((Number) values[0]).intValue(), ((Number) values[1]).intValue() - 1, ((Number) values[2]).intValue(), time[0], time[1], time[2]);
       return calendar.getTime();
     } catch (final MetadataException e) {
       throw new RuntimeException("Cannot read Gps-Date", e);
     } catch (final ParseException e) {
       throw new RuntimeException("Cannot read Gps-Date", e);
     }
   }
 
 }
