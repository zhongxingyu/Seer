 package util;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 public class JpaUtil {
 
     private static EntityManagerFactory emf;
 
     static {
        emf = Persistence.createEntityManagerFactory("my-hsql-unit",
                 new PropertyLoader().getPropertiesAsMap());
     }
 
     public static EntityManagerFactory getFactory() {
         return emf;
     }
 
     public static void closeFactory() {
         emf.close();
     }
 
 }
