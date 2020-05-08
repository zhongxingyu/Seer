 package daniel.nagger;
 
 public final class Config {
   private Config() {}
 
   public static String getBaseUrl() {
     return inDevMode()
        ? "http://nagger.lubarov.com.dlubarov.local:12345"
         : "http://nagger.lubarov.com";
   }
 
   public static String getStaticContentRoot() {
     return System.getProperty("user.dir") + "/nagger/static/";
   }
 
   public static String getDatabaseHome(String dbName) {
     return System.getProperty("user.dir") + "/nagger/db/" + dbName;
   }
 
   private static boolean inDevMode() {
     return System.getenv("ENVIRONMENT").equals("development");
   }
 }
