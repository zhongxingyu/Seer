 package eu.pinnoo.garbagecalendar.data;
 
 import android.content.Context;
 import android.graphics.Color;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class LocalConstants {
 
     public static final boolean DEBUG = true;
     public static final String ENCODING = "iso-8859-1";
     public static final String CALENDAR_URL = "http://datatank.gent.be/MilieuEnNatuur/IVAGO-Inzamelkalender.json";
     public static final String STREETS_URL = "http://datatank.gent.be/MilieuEnNatuur/IVAGO-Stratenlijst.json";
     public static final String GOOGLE_MAPS_API = "http://maps.googleapis.com/maps/api/geocode/json";
     public static final String DEFAULT_SECTOR = "-00";
     public static final String NOTIF_INTENT_COL = "collection";
    public static final String LOG = "eu.pinnoo.garbagecalendar";
     public static final int COLOR_TABLE_EVEN_ROW = Color.rgb(240, 240, 240);
     public static final int COLOR_TABLE_ODD_ROW = Color.rgb(219, 219, 219);
     public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
 
     public enum DateFormatType {
 
         MAIN_TABLE, WEEKDAY, SHORT_WEEKDAY, FULL
     }
 
     public enum CacheName {
 
         COLLECTIONS_DATA("collectionsdata"),
         USER_DATA("userdata"),
         ADDRESS_DATA("addressdata"),
         LAST_MOD_COL("lastmodcol"),
         LAST_MOD_ADDRESSES("lastmodaddresses"),
         COL_REFRESH_NEEDED("colrefreshneeded");
         private String s;
 
         private CacheName(String s) {
             this.s = s;
         }
 
         @Override
         public String toString() {
             return s;
         }
     }
 
     public static SimpleDateFormat getDateFormatter(DateFormatType type, Context c) {
         switch (type) {
             case MAIN_TABLE:
                 return new SimpleDateFormat("EEE, d MMMM", c.getResources().getConfiguration().locale);
             case WEEKDAY:
                 return new SimpleDateFormat("EEEE", c.getResources().getConfiguration().locale);
             case SHORT_WEEKDAY:
                 return new SimpleDateFormat("EEE", c.getResources().getConfiguration().locale);
             case FULL:
                 return new SimpleDateFormat("EEEE d MMMM", c.getResources().getConfiguration().locale);
         }
         return null;
     }
 }
