 package com.cruzroja.android.database;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public final class CreuRojaContract {
 
     public static final class Locations implements BaseColumns {
         public static final Uri CONTENT_LOCATIONS = Uri.parse("content://" +
                 CreuRojaProvider.CONTENT_NAME + "/locations");
 
         public static final String TABLE_NAME = "locations";
         public static final String DEFAULT_ORDER = _ID + " DESC";
 
         public static final String LATITUD = "latitud";
         public static final String LONGITUD = "longitud";
        public static final String ICON = "location_type";
         public static final String NAME = "name";
         public static final String ADDRESS = "address";
         public static final String DETAILS = "details";
        public static final String LAST_MODIFIED = "updated_at";
         public static final String EXPIRE_DATE = "expiredate";
     }
 
 }
