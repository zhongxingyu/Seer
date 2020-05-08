 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package periodic;
 
 
 import controller.traffic.PMVController;
 import controller.traffic.StatsPMVController;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.Resource;
 import javax.ejb.LocalBean;
 import javax.ejb.Schedule;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.ejb.TimerService;
 import org.jdom2.JDOMException;
 
 /**
  * This class Provides method automatically called for importation of datas in application database. 
  * @author mael
  */
 @Singleton
 @Startup
 @LocalBean
 public class PeriodicImport {
 
   @Resource
   TimerService timerService;
   
   private DateFormat mediumDateFormat = 
     DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
 
 
   /**
    * Imports all Nantes PMVs on the application database once by day.
    */
   @Schedule(second="0", minute="0", hour="*/23", persistent = false)
   public synchronized void importEveryDay(){
        PMVController pmvContr = new PMVController();
         try {
             pmvContr.removeAll();
             pmvContr.importPMV();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         }
     Logger.getLogger(PeriodicImport.class.getName()).log(Level.INFO, 
       "Execution import PMV " + mediumDateFormat.format(new Date()));
    }
   
 /**
  * Add Statistics datas about itineraries in the application database every 5 minutes
  */
 @Schedule(second="0",minute="*/5", hour="*", persistent = false)
   public synchronized void importStatsEvery5Min(){
        StatsPMVController pmvContr = new StatsPMVController();
 
         try {
             pmvContr.importAPI();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JDOMException ex) {
             Logger.getLogger(PeriodicImport.class.getName()).log(Level.SEVERE, null, ex);
         }
        
     Logger.getLogger(PeriodicImport.class.getName()).log(Level.INFO, 
       "Execution importAPI " + mediumDateFormat.format(new Date()));
    }
 
   
   
 }
