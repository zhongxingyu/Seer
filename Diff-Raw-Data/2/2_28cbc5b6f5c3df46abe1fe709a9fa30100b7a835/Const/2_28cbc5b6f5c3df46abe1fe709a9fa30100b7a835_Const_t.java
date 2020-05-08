 package ru.roman.bim.util;
 
 import ru.roman.bim.service.gae.wsclient.BimItemType;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * User: Roman
  * DateTime: 01.09.12 12:08
  */
 public interface Const {
 
     boolean DEV_MODE = false;
 
    String VERSION = "1.6";
     String APP_NAME = "Bim";
     String APP_DATA_DIR_NAME = "/." + APP_NAME;
 
     boolean SHOW_TRAY_NOTIFICATIONS = true;
     /*
      
      */
     Integer CACHE_MAX_SIZE = 100;
     /*
       
      */
     String DEFAULT_SORTING_FIELD = "editDate";
     String DEFAULT_SORTING_DIRECTION = "DESCENDING";  // ASCENDING, DESCENDING
 
     Integer DEFAULT_LANG_ID = 1;
     List<BimItemType> DEFAULT_TYPES = Arrays.asList(BimItemType.values());
     Long DEFAULT_OWNER_ID = 1L;
     Integer DEFAULT_MINIMAL_RATING = 1;
     Collection<? extends Integer> DEFAULT_RATINGS = Arrays.asList(1, 2, 3, 4, 5);
     Long DEFAULT_RATING = 3l;
 }
 
