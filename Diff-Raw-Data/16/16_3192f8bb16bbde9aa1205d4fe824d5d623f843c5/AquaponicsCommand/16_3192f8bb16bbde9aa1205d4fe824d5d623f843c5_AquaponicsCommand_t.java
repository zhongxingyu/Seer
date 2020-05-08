 package edu.hawaii.ihale.backend.restserver.resource.aquaponics;
 
 import java.util.Map;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Put;
 import org.restlet.resource.ServerResource;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.backend.IHaleBackend;
 
 /**
  * A server resource that will handle requests regarding the Aquaponics system. Supported
  * operations: PUT. Supported representations: XML.
  * 
  * @author Michael Cera
  */
 public class AquaponicsCommand extends ServerResource {
 
   private IHaleBackend backend = new IHaleBackend();
 
   /**
    * Sends command to system.
    * 
    * @param representation Representation.
    * @return Null.
    */
   @Put
   public Representation sendCommand(Representation representation) {
 
     Map<String, String> queryMap = getQuery().getValuesMap();
     String command = (String) this.getRequestAttributes().get("command");
     IHaleSystem system = IHaleSystem.AQUAPONICS;
     IHaleCommandType commandType;
    Object commandArg;
 
     if (queryMap.containsKey("arg")) {
 
       String arg = queryMap.get("arg");
       if ((command.equals(IHaleCommandType.SET_TEMPERATURE.toString()) &&
           (IHaleState.SET_TEMPERATURE_COMMAND.isType(arg)))) {
         commandType = IHaleCommandType.SET_TEMPERATURE;
        commandArg = Integer.parseInt(arg);
         backend.doCommand(system, null, commandType, commandArg);
       }
       else if ((command.equals(IHaleCommandType.FEED_FISH.toString()) &&
           (IHaleState.FEED_FISH_COMMAND.isType(arg)))) {
         commandType = IHaleCommandType.FEED_FISH;
        commandArg = Double.parseDouble(arg);
         backend.doCommand(system, null, commandType, commandArg);
       }
       else if ((command.equals(IHaleCommandType.HARVEST_FISH.toString()) &&
           (IHaleState.HARVEST_FISH_COMMAND.isType(arg)))) {
         commandType = IHaleCommandType.HARVEST_FISH;
        commandArg = Integer.parseInt(arg);
         backend.doCommand(system, null, commandType, commandArg);
       }
       else if ((command.equals(IHaleCommandType.SET_NUTRIENTS.toString()) &&
           (IHaleState.SET_NUTRIENTS_COMMAND.isType(arg)))) {
         commandType = IHaleCommandType.SET_NUTRIENTS;
        commandArg = Double.parseDouble(arg);
         backend.doCommand(system, null, commandType, commandArg);
       }
       else if ((command.equals(IHaleCommandType.SET_PH.toString()) &&
           (IHaleState.SET_PH_COMMAND.isType(arg)))) {
         commandType = IHaleCommandType.HARVEST_FISH;
        commandArg = Double.parseDouble(arg);
         backend.doCommand(system, null, commandType, commandArg);
       }
       else {
         getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
       }
     }
     else {
       getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
     }
     return null;
   }
 }
