 package suncertify.constants;
 
 public class Variables {
  public static final boolean IS_TEMP_MODE = true;
  // public static final boolean IS_TEMP_MODE = false;
   public static final String ENCODING = "US-ASCII";
   public static final String FILE_PATH = "C:\\projects\\sun\\suncertify\\";
   public static final String FILE_NAME = "db-2x3-ext.db";
   public static final String FILE_NAME_TEMP = "db-2x3-ext-temp.db";
   public static final String FILE_NAME_TEMP2 = "db-2x3-ext-temp2.db";
   public static final String FILE_NAME_TEMP3 = "db-2x3-ext-temp3.db";
   public static final String TERMINATOR = "\0";
 
   public static String getFullFilePath() {
     return FILE_PATH + FILE_NAME;
   }
 
   public static String getFullFilePathTemp() {
     return FILE_PATH + FILE_NAME_TEMP;
   }
 
   public static String getFullFilePathTemp2() {
     return FILE_PATH + FILE_NAME_TEMP2;
   }
 
   public static String getFullFilePathTemp3() {
     return FILE_PATH + FILE_NAME_TEMP3;
   }
 
   public static String getOriginalFilePath() {
     if (IS_TEMP_MODE) {
       return getFullFilePathTemp();
     }
     return getFullFilePath();
   }
 
   public static String getWorkedFilePath() {
     if (IS_TEMP_MODE) {
       return getFullFilePathTemp2();
     }
     return getFullFilePathTemp();
   }
 }
