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
        updateConnectionTypes();
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
        updateConnectionTypes();
     }
     
     void addIRCommand(IRCommand command){
         irCommands.add(command);
        updateConnectionTypes();
     }
     
     void addLANCCommand(LANCCommand command){
         lancCommands.add(command);
        updateConnectionTypes();
     }
     
     @Override
     public String toString() {
         return getModelName();
     }
 
    private void updateConnectionTypes() {
        int i = 0;
        if(wireCommands.size() > 0) i++;
        if(irCommands.size() > 0) i++;
        if(lancCommands.size() > 0) i++;
        connectionTypes = new ConnectionType[i];
        i = 0;
        if(wireCommands.size() > 0) connectionTypes[i++] = ConnectionType.Wire;
        if(irCommands.size() > 0) connectionTypes[i++] = ConnectionType.IR;
        if(lancCommands.size() > 0) connectionTypes[i++] = ConnectionType.LANC;
    }

 }
