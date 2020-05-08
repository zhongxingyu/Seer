 package jDistsim.core.simulation.distributed;
 
 import jDistsim.utils.pattern.observer.Observable;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.3.13
  * Time: 23:14
  */
 public class DistributedModelDefinition extends Observable {
 
     private String modelName;
     private String rmiModelName;
     private String address;
     private int port;
     private boolean lookahead;
 
     public DistributedModelDefinition(String rmiModelName, String address, int port) {
         this.rmiModelName = rmiModelName;
         this.address = address;
         this.port = port;
     }
 
     public DistributedModelDefinition(String modelName, String rmiModelName, String address, int port, boolean lookahead) {
         this.modelName = modelName;
         this.rmiModelName = rmiModelName;
         this.address = address;
         this.port = port;
         this.lookahead = lookahead;
     }
 
     public String getModelName() {
         return modelName;
     }
 
     public void setModelName(String modelName) {
         this.modelName = modelName;
         notifyObservers("modelName");
     }
 
     public String getRmiModelName() {
         return rmiModelName;
     }
 
     public void setRmiModelName(String rmiModelName) {
         this.rmiModelName = rmiModelName;
         notifyObservers("rmiModelName");
     }
 
     public boolean isLookahead() {
         return lookahead;
     }
 
     public void setLookahead(boolean lookahead) {
         this.lookahead = lookahead;
         notifyObservers("lookahead");
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
         notifyObservers("port");
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
         notifyObservers("address");
     }
 
     public static DistributedModelDefinition createDefault() {
         return new DistributedModelDefinition("Remote model", "remote_model1", "localhost", 1089, false);
     }
 
     public static DistributedModelDefinition createNull() {
        return new DistributedModelDefinition("null", "null", "null", 0, false);
     }
 
     @Override
     public String toString() {
         return getAddress() + ":" + getPort() + "/" + getRmiModelName() + "." + isLookahead();
     }
 }
