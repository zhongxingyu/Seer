 package net.sf.taverna.portal.wireit;
 
 import net.sf.taverna.portal.wireit.exception.WireItRunException;
 import java.io.IOException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import net.sf.taverna.portal.wireit.event.OutputListener;
 import net.sf.taverna.portal.wireit.module.*;
 import net.sf.taverna.portal.commandline.TavernaException;
 import net.sf.taverna.portal.utils.Resolver;
 
 /**
  * This class takes a Json pipe and converts it into a number of connected modules which can then be run.
  * 
  * A network of modules is built in the Constructor. 
  * Modules will be connected together using the listener pattern.
  * These can then be run, 
  * and finally cobverted back to a Json object with the updated values.
  * @author Christian
  */
 public class Wiring {
     
     /** An array of modules, which will be connected using Listeners. 
      *  The values  inside the object will change when the modules are run.
      */
     Module[] modules;
     /** The properties part of the original pipe which will be returned unchanged. */
     JSONObject properties;
     /** The wires part of the original pipe which will be returned unchanged. */
     JSONArray wires;
     
     /**
      * Converts the jsonObject into a connected array of Modules and save other part of the Pipe.
      * <p>
      * There are three parts to a WireIt Json pipe, "modules", "properties" and "wires".
      * <p>
      * The "modules" array is converted into an array of uk.ac.manchester.cs.wireit.module Modules.
      * The type of module used will depend on the name and xtype.
      * Each element of the "modules" array will result in one element in the uk.ac.manchester.cs.wireit.module array.
      * <p>
      * The "properties" object is not used by the run so is simply saved so it can be included in the final output.
      * <p>
      * The "wires" array represent the connections between the modules.
      * As these are not changed by a run the Json array is saved to be included as is in the final output.
      * <p>
      * The "wires" array is also used to create links between the modules.
      * Modules are connected together using a Listener model.
      * For each wire the "tgt" (Target) module is asked to provide a Listener for the relevant "terminal".
      * The "src" (Source) modules is then asked to add this Listener to the relevant "terminal".
      * @param jsonInput The pipe converted to json
      * @param resolver Util to convert between files, absolute uri and relative uris
      * @throws JSONException Thrown it the json is not in the expected format.
      * @throws TavernaException Thrown by the TavernaModule if the information is inconsistant. 
      * @throws IOException Thrown by the TavernaModule if the workflow is unreadable.
      */
    public Wiring(JSONObject jsonInput, Resolver resolver) 
             throws JSONException, TavernaException, IOException{
         JSONArray jsonArray = jsonInput.getJSONArray("modules");
         modules = new Module[jsonArray.length()];
         for (int i = 0; i < jsonArray.length(); i++){
             Object json = jsonArray.get(i);
             if (json instanceof JSONObject){
                 JSONObject jsonObject = (JSONObject)json;
                 String name = jsonObject.getString("name");
                 if (name.equalsIgnoreCase("simple input")){
                    modules[i] = new InputStringModule(jsonObject); 
                 } else if (name.equalsIgnoreCase("list input")){
                    modules[i] = new InputListModule(jsonObject); 
                } else if (name.equalsIgnoreCase("url input")){
                    modules[i] = new InputURIModule(jsonObject); 
                 } else if (name.equalsIgnoreCase("url list input")){
                    modules[i] = new InputDelimiterURIModule(jsonObject); 
                } else if (name.equalsIgnoreCase("simple output") || name.equalsIgnoreCase("url output")){
                    modules[i] = new OutputModule(jsonObject); 
                 } else if (name.equalsIgnoreCase("list output")){
                    modules[i] = new OutputListModule(jsonObject); 
                 } else if (name.equalsIgnoreCase("PassThrough")){
                    modules[i] = new PassThroughModule(jsonObject); 
                 } else if (name.equalsIgnoreCase("comment")){
                    modules[i] = new CommentModule(jsonObject); 
                 } else if (jsonObject.has("config")){
                     JSONObject config = jsonObject.getJSONObject("config");
                     String xtype = config.optString("xtype");
                     if ("WireIt.TavernaWFContainer".equalsIgnoreCase(xtype)){
                        modules[i] = new TavernaModule(jsonObject, resolver); 
                     } else if ("WireIt.URILinkContainer".equalsIgnoreCase(xtype) || "WireIt.BaclavaContainer".equalsIgnoreCase(xtype)){
                        modules[i] = new URILinkModule(jsonObject); 
                     } else {
                         throw new JSONException("Unexpected name " + name + " and xtype " + xtype + " in modules");
                     }
                 } else {
                     throw new JSONException("Unexpected name " + name + "and no config in modules");
                 }
             } else {
                 throw new JSONException("Unexpected type " + json.getClass() + " in modules");
             }
         }
         properties = jsonInput.getJSONObject("properties");
         wires = jsonInput.getJSONArray("wires");
         for (int i = 0; i < wires.length(); i++){
             JSONObject wire = wires.optJSONObject(i);
             JSONObject tgt = wire.getJSONObject("tgt");
             int tgtNum = tgt.getInt("moduleId");
             Module target = modules[tgtNum];
             String terminal = tgt.getString("terminal");
             System.out.println(tgtNum + " " + terminal + " " + target);
             OutputListener outputListener = target.getOutputListener(terminal);
             JSONObject src = wire.getJSONObject("src");
             int srcNum = src.getInt("moduleId");
             Module source = modules[srcNum];
             terminal = src.getString("terminal");
             source.addOutputListener(terminal, outputListener);           
         }
     }
     
     /**
      * Runs the pipe by asking each module to run, logging can be done to the StringBuilder.
      * <p>
      * The individual modules are responsible for determining if they should run based on this call,
      * or if they will run after receiving the expected input on their Listeners.
      * 
      * @param outputBuilder Logging buffer.
      * @throws WireItRunException Thrown by any module that encounter problems running itself, 
      *     or when an exception is throw by a Listening module. 
      */
     public void run(StringBuilder outputBuilder) throws WireItRunException{
         for (int i = 0; i < modules.length; i++){
             modules[i].run(outputBuilder);
         }
     }
     
     /**
      * Converts the state of the modules back to json for returning to WireIt.
      * <p>
      * Typically called after the modules have been run.
      * <p>
      * Creates a new JSONObject adding the "wires" and "properties" saved during construction.
      * <p>
      * Creates a "modules" array including the JSON value of each module's current state.
      * @return The current state as a json object.
      * @throws JSONException 
      */
     public JSONObject getJsonObject() throws JSONException{
         JSONObject me = new JSONObject();
         me.put("wires", wires);
         me.put("properties", properties);
         for (int i = 0; i < modules.length; i++){
             me.append("modules", modules[i].getJsonObject());
         }       
         return me;
     }
 }
