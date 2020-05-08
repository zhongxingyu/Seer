 package com.omgren.apps.smsgcm.server;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.LinkedList;
 import com.omgren.apps.smsgcm.server.DSDevice;
 
 public class DSUser {
 
   private String dn;
   private List<DSDevice> devices;
 
   public DSUser(String dn) {
     this.dn = dn;
 
     this.devices = new LinkedList<DSDevice>();
   }
 
   public void register(String regId){
     synchronized(this.devices){
       this.devices.add(new DSDevice(regId));
     }
   }
 
   public void unregister(String regId){
     synchronized(this.devices){
       for(Iterator<DSDevice> it = this.devices.iterator(); it.hasNext(); ){
         if(it.next().getRegId().equals(regId))
           it.remove();
       }
     }
   }
 
   public void updateRegistration(String oldId, String newId){
     synchronized(this.devices){
       for(Iterator<DSDevice> it = this.devices.iterator(); it.hasNext(); ){
         DSDevice d = it.next();
         if(d.getRegId().equals(oldId))
           d.setRegId(newId);
       }
     }
   }
 
   public List<String> getDeviceIds(){
     List<String> devs = new LinkedList<String>();
     synchronized(this.devices){
       for(Iterator<DSDevice> it = this.devices.iterator(); it.hasNext(); ){
         DSDevice d = it.next();
         devs.add(d.getRegId());
       }
     }
     return devs;
   }
 
   public List<DSDevice> getDevices(){
     synchronized(this.devices){
       return new LinkedList<DSDevice>(this.devices);
     }
   }
 
   public DSDevice getDeviceById(String regId){
     synchronized(this.devices){
       for(Iterator<DSDevice> it = this.devices.iterator(); it.hasNext(); ){
        if(it.next().getRegId().equals(regId))
          return it;
       }
       return null;
     }
   }
 
   public DSDevice getDevice(int x){
     synchronized(this.devices){
       try {
         return this.devices.get(x);
       } catch (IndexOutOfBoundsException e) {
         return null;
       }
     }
   }
 
   public String getDN(){
     return this.dn;
   }
 }
