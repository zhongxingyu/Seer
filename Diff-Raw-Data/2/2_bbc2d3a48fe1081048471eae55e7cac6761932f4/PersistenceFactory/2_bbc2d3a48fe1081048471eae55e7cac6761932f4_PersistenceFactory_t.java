 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Persistence;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 
 /**
  *
  * @author i060752
  */
 public class PersistenceFactory {
     
     private PersistenceFactory() {
         
         //vai ao ficheiro propriedades - expensemanager.properties- obter a
         //a factory associada ao tipo de persistencia a usar.
         //Por omissão JpaRepositoryFactory
         try{
             FileInputStream propFile = new FileInputStream("expensemanager.properties");
             Properties p = new Properties(System.getProperties());
             p.load(propFile);
             System.setProperties(p);
         } catch(Exception e) {
            System.setProperty("PERSIST", "Persistence.JpaRepositoryFactory");
             //System.setProperty("PERSIST", "persistence.InMemoryRepositoryFactory");
         }
     }
       
       //LAZY LOADING – create only when needed
       private static PersistenceFactory instance = null;
 
       public static PersistenceFactory getInstance() {
             if (instance == null) {
                   instance = new PersistenceFactory ();
             }
             return instance;
       }    
 
       public IRepositoryFactory buildRepositoryFactory(){
           String desc = System.getProperty("PERSIST");
           try
           {
               return (IRepositoryFactory) Class.forName(desc).newInstance();
           } catch(Exception ex)
               
           {
               return null;
           }
       }
 }
