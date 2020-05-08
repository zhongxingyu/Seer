 package jp.troter.servlet.httpsession.spi;
 
 import com.mongodb.DB;
 
 public abstract class MongoDBInitializer {
 
     private static final String SERVICE_CLASS_NAME
        = "jp.troter.servlet.httpsession.spi.MongoDbInitializer";
 
     private static SingleServiceLoader<MongoDBInitializer> loader;
 
     private static synchronized SingleServiceLoader<MongoDBInitializer> getLoader() {
         if (loader == null) {
             loader = new SingleServiceLoader<MongoDBInitializer>(SERVICE_CLASS_NAME, null);
         }
         return loader;
     }
 
     public static MongoDBInitializer newInstance() {
         return getLoader().newService();
     }
 
     public abstract DB getDB();
 
     public abstract int getSessionTimeout();
 }
