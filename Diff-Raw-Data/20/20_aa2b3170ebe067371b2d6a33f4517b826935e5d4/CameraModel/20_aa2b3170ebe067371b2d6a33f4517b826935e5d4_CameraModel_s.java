 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package littlesmarttool2.model;
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author marcher89
  */
 public class CameraModel {
     private String identifier;
     private String modelName;
     public ConnectionType[] connectionTypes;
     private List<WireCommand> wireCommands = new ArrayList<>();
     private List<IRCommand> irCommands = new ArrayList<>();
     private List<LANCCommand> lancCommands = new ArrayList<>();
     
     @JsonCreator
     public CameraModel(@JsonProperty("identifier") String identifier, @JsonProperty("models") String modelName, @JsonProperty("connectionTypes") ConnectionType[] connectionTypes) {
         this.identifier = identifier;
         this.modelName = modelName;
         this.connectionTypes = connectionTypes;
     }
 
     public String getIdentifier() {
         return identifier;
     }
     
     public String getModelName() {
         return modelName;
     }
     
     public ConnectionType[] getConnectionTypes() {
         return connectionTypes;
     }
     
     public List<WireCommand> getWireCommands(){
         return wireCommands;
     }
     
     public List<IRCommand> getIRCommands(){
         return irCommands;
     }
     
     public List<LANCCommand> getLANCCommands(){
         return lancCommands;
     }
     
     void addWireCommand(WireCommand command){
         wireCommands.add(command);
     }
     
     void addIRCommand(IRCommand command){
         irCommands.add(command);
     }
     
     void addLANCCommand(LANCCommand command){
         lancCommands.add(command);
     }
     
     @Override
     public String toString() {
         return getModelName();
     }
 
 }
