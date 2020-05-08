 package edu.hawaii.ihale.backend.restserver.resource;
 
 import java.util.Map;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Put;
 import org.restlet.resource.ServerResource;
 import edu.hawaii.ihale.api.ApiDictionary;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.backend.IHaleBackend;
 
 /**
  * A server resource that will handle requests to System H. Supported operations: PUT. Supported
  * representations: XML.
  * 
  * @author Michael Cera
  */
 public class PutResource extends ServerResource {
 
   private IHaleBackend backend = IHaleBackend.getInstance();
   private Object commandArg;
 
   /**
    * Sends command to system.
    * 
    * @param representation Representation.
    * @return Null.
    */
   @Put
   public Representation sendCommand(Representation representation) {
 
     Map<String, String> queryMap = getQuery().getValuesMap();
     Status status = Status.CLIENT_ERROR_NOT_ACCEPTABLE;
 
     String systemParam = this.getRequestAttributes().get("system").toString();
     String commandParam = this.getRequestAttributes().get("command").toString();
 
     // Retrieve IHaleSystem, IHaleRoom, IHaleCommandType, and arg for doCommand.
     IHaleSystem system = IHaleSystem.valueOf(systemParam);
     IHaleRoom room = queryMap.containsKey("room") ? IHaleRoom.valueOf(queryMap.get("room")) : null;
     IHaleCommandType command = IHaleCommandType.valueOf(commandParam);
 
     String arg = queryMap.containsKey("arg") ? queryMap.get("arg") : null;
 
     // Add "#" symbol in front of arg for setting the color.
     if (command.equals(IHaleCommandType.SET_LIGHTING_COLOR) && arg != null) {
       arg = "#" + arg;
     }
     IHaleState state = ApiDictionary.iHaleCommandType2State(command);
 
     if (arg != null && state.isType(arg)) {
       handleArg(command, arg);
 
       if (system.equals(IHaleSystem.LIGHTING)) {
         if (room != null && commandArg != null) {
           status = Status.SUCCESS_OK;
           backend.doCommand(system, room, command, commandArg);
         }
       }
       else if (commandArg != null) {
         status = Status.SUCCESS_OK;
         backend.doCommand(system, null, command, commandArg);
       }
     }
     getResponse().setStatus(status);
     return representation;
   }
 
   /**
    * Changes commandArg to correct type.
    * 
    * @param command The IHaleCommandType
    * @param arg The String to be parsed into the correct Object type.
    */
   private void handleArg(IHaleCommandType command, String arg) {
 
     switch (command) {
     // HVAC, Aquaponics
     case SET_TEMPERATURE:
       commandArg = Integer.parseInt(arg);
       break;
     // Aquaponics
     case HARVEST_FISH:
       commandArg = Integer.parseInt(arg);
       break;
     case FEED_FISH:
       commandArg = Double.parseDouble(arg);
       break;
     case SET_NUTRIENTS:
       commandArg = Double.parseDouble(arg);
       break;
     case SET_PH:
       commandArg = Double.parseDouble(arg);
       break;
     case SET_WATER_LEVEL:
      commandArg = Integer.parseInt(arg);
       break;
     // Lighting
     case SET_LIGHTING_LEVEL:
       commandArg = Integer.parseInt(arg);
       break;
     case SET_LIGHTING_ENABLED:
       commandArg = Boolean.parseBoolean(arg);
       break;
     case SET_LIGHTING_COLOR:
       commandArg = arg;
       break;
     default:
       commandArg = null;
       break;
     }
   }
 }
