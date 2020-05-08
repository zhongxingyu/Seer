 package org.amplafi.dsl;
 
 import groovy.lang.Closure;
 
 import java.util.List;
 import java.util.Map;
 
import org.amplafi.flow.utils.AdminTool;
 import org.amplafi.flow.utils.FlowResponse;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * This class defines the methods that are callable within the flow test DSL.
  */
 public class DescribeScriptDSL {
     String name;
     String usage;
     String description;
     List<ParameterUsage> usages;
     ScriptDescription scriptDescription;
     
     /** This stores the base uri including the host,port,apikey */
     private String serviceInfo;
 
     /** This stores the last request to the server */
     private String lastRequestString;
 
     /**
      * Contains the last response from the server.
      */
     public String lastRequestResponse = null;
     private Log log;
 
     public DescribeScriptDSL(){
     }
 
     public String getKey(){
         return null;
     }
 
     String getApiVersion(){
         return null;
     }
 
     void setApiVersion(String apiVersion){
     }
 
 
     /**
      * The description of the name.
      *@param name - the name to description.
      *@param description - the name 's description.
      *@throw EarlyExitException if it just need description.
      */
     public void description (String name, String description){
         this.name = name;
         this.description = description;
         this.scriptDescription = new ScriptDescription(name, description, "");
         // This pevents the other commands in the script fom being executed.
         throw new EarlyExitException(scriptDescription);
     }
 
     /**
      * The description of the name.
      *@param name - the name to description.
      *@param description - the name 's description.
      *@param usage is the usage of the script.
      *@throw EarlyExitException  if it just need description.
      */
     public void description (String name, String description, String usage){
         this.usage = usage;
         this.name = name;
         this.description = description;
         this.scriptDescription = new ScriptDescription(name , description, usage);
         // This pevents the other commands in the script fom being executed.
         throw new EarlyExitException(scriptDescription);
     }
 
     /**
      * The description of the name.
      *@param name - the name to description.
      *@param description - the name 's description.
      *@param usage is the list of the cript usages.
      *@throw EarlyExitException  if it just need description.
      */
      public void description (String name, String description, List<ParameterUsage> usages){
         String newLine = System.getProperty("line.separator");
         StringBuffer usageSb = new StringBuffer(newLine);
         for(ParameterUsage paramUsage : usages){
             if(paramUsage.getName() != null && paramUsage.getDescription() != null){
                 usageSb.append(String.format("%-15s = <%-15s>", paramUsage.getName(), paramUsage.getDescription()));
                 // usageSb.append(paramUsage.getName() + " = " +"<" +paramUsage.getDescription() + ">");
                 if(paramUsage.isOptional()){
                     usageSb.append(" , optional");
                 }else{
                     usageSb.append(" , required");
                 }
                 if(paramUsage.getDefaultValue() != null){
                     usageSb.append(" , defaultValue = " + paramUsage.getDefaultValue());
                 }
                 usageSb.append(newLine);
             }
         }
         this.usage =  usageSb.toString();
         this.name = name;
         this.description = description;
         this.scriptDescription = new ScriptDescription(name, description, usage, usages);
         // This pevents the other commands in the script fom being executed.
         throw new EarlyExitException(scriptDescription);
      }
 
     /**
      * send a post request.
      */
     String requestPost(String flowName, Map paramsMap){
         throw new NoDescriptionException();
     }
 
     /**
      * Sends a request to the named flow with the specified parameters.
      * @param flowName to call.
      * @param paramsMap key value map of parameters to send.
      */
     String request(String flowName, Map paramsMap){
         throw new NoDescriptionException();
     }
 
     /**
      * This method will automatically add a callbackParam into params and send the request.
      * With a callback uri It will then use openPort to call the flow and return the response.
      * @param flowName to call.
      * @param [params] key value map of parameters to send.
      * @param dataReturnProperty the property we want to return.
      * @return response string.
      */
     String asyncRequest(String flowName, Map paramsMap, String dataReturnProperty){
         throw new NoDescriptionException();
     }
 
     /**
      * Sends a request to the named flow with the specified parameters.
      * @param flowName to call.
      * @param paramsMap key value map of parameters to send.
      * @return response object.
      */
     FlowResponse requestResponse(String flowName, Map paramsMap){
         throw new NoDescriptionException();
     }
 
     /**
      * Throws a test error if the actual data returned from the server is not the same as.
      * the expected JSON.
      * @param expectedJSONData.
      */
     void expect(String expectedJSONData){
         throw new NoDescriptionException();
     }
 
     /**
      * Throws a test error if the actual data returned from the server is not the same as.
      * the expected JSON, ignorePathList.
      * @param expectedJSONData.
      */
     void expect(String expectedJSONData,List<String> ignorePathList){
         throw new NoDescriptionException();
     }
 
     /**
      * @param expectedJSONData.
      */
     Object checkReturnedValidJson(){
         throw new NoDescriptionException();
     }
     
     /**
      * @param expectedJSONData.
      */
     void prettyPrintResult(){
         throw new NoDescriptionException();
     }
 
     /**
      * Call a script with params.
      * @param scriptName script name.
      * @param callParamsMap script parameters.
      */
     Object callScript(String scriptName, Map callParamsMap){
         throw new NoDescriptionException();
     }
 
     Object openPort(int portNo, int timeOutSeconds, Closure doNow, Closure handleRequest){
         throw new NoDescriptionException();
     }
 
     /**
      * Call a script with no params.
      * @param scriptName script name.
      */
     Object callScript(String scriptName){
         throw new NoDescriptionException();
     }
 
     ScriptDescription getscriptDescription(){
         return scriptDescription;
     }
 
     /**
      * @param message.
      * Print a message.
      */
     void log(Object msg){
          getLog().info(msg);
     }
 
     /**
      * @param msg is message to debug log.
      */
     private void debug(String msg){
         getLog().debug(msg);
     }
 
     /**
      * Get the logger for this class.
      * @return log.
      */
     public Log getLog(){
         if ( this.log == null ) {
            this.log = LogFactory.getLog(AdminTool.class);
         }
         return this.log;
     }
 
     /**
      * This method returns a ParameterUsge.
      * @param name is name of the param.
      * @param description is description of the param.
      * @param optional is optional of the param.
      * @param defaultValue is default value of the param.
      * @return a ParameterUsge.
      */
     public ParameterUsage paramDef(String name,String description,boolean optional,Object defaultValue){
         return new ParameterUsage(name,description,optional,defaultValue);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getUsage() {
         return usage;
     }
 
     public void setUsage(String usage) {
         this.usage = usage;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public List<ParameterUsage> getUsages() {
         return usages;
     }
 
     public void setUsages(List<ParameterUsage> usages) {
         this.usages = usages;
     }
 
     public ScriptDescription getScriptDescription() {
         return scriptDescription;
     }
 
     public void setScriptDescription(ScriptDescription scriptDescription) {
         this.scriptDescription = scriptDescription;
     }
 
     public String getServiceInfo() {
         return serviceInfo;
     }
 
     public void setServiceInfo(String serviceInfo) {
         this.serviceInfo = serviceInfo;
     }
 
     public String getLastRequestString() {
         return lastRequestString;
     }
 
     public void setLastRequestString(String lastRequestString) {
         this.lastRequestString = lastRequestString;
     }
 
     public String getLastRequestResponse() {
         return lastRequestResponse;
     }
 
     public void setLastRequestResponse(String lastRequestResponse) {
         this.lastRequestResponse = lastRequestResponse;
     }
 
     public void setLog(Log log) {
         this.log = log;
     }
 }
