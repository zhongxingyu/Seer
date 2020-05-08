 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package businessDomainObjects;
 
 import java.util.HashSet;
 import persistance.PersistanceRepositoryHandset;
 
 /**
  *
  * @author neil
  */
 public class HandsetAccessManager {
     
     private PersistanceRepositoryHandset persistance;
     private HashSet<String> allowedDevices;
     
     public HandsetAccessManager(PersistanceRepositoryHandset persistance){
         this.persistance = persistance;
         this.initialise();
     }
     
     public synchronized void addDevice(String macAddress){
         if (this.allowedDevices.add(macAddress)){
             persistance.addDevice(macAddress);
         }
     }
     
     public synchronized void removeDevice(String macAddress){
         if (this.allowedDevices.remove(macAddress)){
             persistance.removeDevice(macAddress);
         }
     }
     
     public boolean deviceHasAccess(String macAddress){
         return allowedDevices.contains(macAddress);
     }
     
     public String[] getDeviceList(){
        return this.allowedDevices.toArray(new String[0]);
     }
     
     private void initialise(){
         this.allowedDevices = this.persistance.getAllowedDevices();
     }
     
 }
