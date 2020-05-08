 package net.sf.taverna.portal.wireit;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.json.JSONException;
 import org.json.JSONObject;
 import net.sf.taverna.portal.wireit.exception.WireItRunException;
 import net.sf.taverna.portal.commandline.TavernaException;
 import net.sf.taverna.portal.utils.Resolver;
 
 /**
  * This is the servlet that receives the run call.
  * 
  * @author Christian
  */
 public class RunWireit extends WireitSQLBase {
     
     public final String TAVERNA_CMD_HOME_PARAMETER = "TAVERNA_CMD_HOME";
     private static String tavernaHome;
     
     /** 
      * Constructor is called first time servlet is called.
      * @throws ServletException 
      */
     public RunWireit() throws ServletException{
         super();
     }
  
     /** 
      * This is called by Tomcat.
      * Gets Taverna Home parameter is available.
      */
     public void init(){
         tavernaHome = getServletContext().getInitParameter(TAVERNA_CMD_HOME_PARAMETER);
     }
     
     /**
      * 
      * <p>
      * <ul>
      *    <li>Logs the start of the run.
      *    <li>Sets up a Resolver for converting between absolute and relative uris
      *    <li>Extracts the Json from the request body,s "working" parameter.
      *    <li>Sets up the reply
      *    <li>Runs the Pipe See doRun
      *    <li>Logs that the run is finished.
      *    <li>Adds the result (json with new values) to the output
      * </ul>
      * @param request 
      * @param response .
      * @throws ServletException Error generating the Json
      * @throws IOException Error reading request body
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
         System.out.println();
         System.out.println((new Date()) + "in runWireit.doPost");
         StringBuilder outputBuilder = new StringBuilder();
         outputBuilder.append("Run posted at ");
         outputBuilder.append(new Date());
         outputBuilder.append("\n");
         
         Resolver resolver = new Resolver(request, getServletContext());
 
         String input = readRequestBody(request);
         HashMap<String, String> parameters = convertBody(input);
         JSONObject jsonInput = getInputJson(parameters);
 
         // Set the MIME type for the response message
         response.setContentType("text/x-json;charset=UTF-8");  
         // Get a output writer to write the response message into the network socket
         PrintWriter out = response.getWriter();
         
         JSONObject jsonReply;
         try {
             jsonReply = doRun(jsonInput, outputBuilder, resolver);
             addRunResult(jsonReply, outputBuilder);
             String output = getOutput(parameters.get("name"), jsonReply, parameters.get("language"));
             out.println(output);
         } catch (Exception ex) {
             addRunFailed(jsonInput, ex, outputBuilder);
             String output = getOutput(parameters.get("name"), jsonInput, parameters.get("language"));
             out.println(output);
         }        
     }
   
     /**
      * @return TavernaHome retreived from the paramters. May be null;
      */
     public static String getTavernaHome(){
         return tavernaHome;
     }
     
     /**
      * Extracts the "working" paramter and converts it to a Json object
      * @param parameters The paramters in the bidy of the message
      * @return Pipe in json format
      * @throws ServletException  Any exception extracting the json.
      */
     private JSONObject getInputJson(HashMap<String, String> parameters) throws ServletException{
         String workingString = parameters.get("working");
         JSONObject jsonInput;
         try {
             jsonInput = new JSONObject(workingString);
             System.out.println(jsonInput.toString(4));     
         } catch (Exception ex) {
             System.err.println("Error reading input json");
             ex.printStackTrace();
             throw new ServletException(ex);
         }        
         return jsonInput;
     }
     
     /**
     * Logs the finished pipe and adds run log to the Json output.
      * @param jsonReply Pipe in Json format after the run.
      * @param outputBuilder Log from the run to be added to the json.
      * @throws JSONException 
      */
     private void addRunResult(JSONObject jsonReply, StringBuilder outputBuilder) throws JSONException {
         JSONObject properties = jsonReply.getJSONObject("properties"); 
         System.out.println(jsonReply.toString(4));
        properties.put("status", "Finished");
         outputBuilder.append("Run finished at ");
         outputBuilder.append(new Date());
         outputBuilder.append("\n");
         properties.put("details", outputBuilder.toString());
         properties.remove("error");
     }
     
     /**
      * Oops something has gone wrong. Lets pass something useful back to the client.
      * <p>
      * This current prototype pass a lot of information back to the client.
      * Future version may want to pass less back.
      * @param main
      * @param ex
      * @param outputBuilder
      * @throws ServletException 
      */
     private void addRunFailed(JSONObject main, Exception ex, StringBuilder outputBuilder) throws ServletException{
         System.err.println("Error running pipe");
         ex.printStackTrace();  
         String message;
         if (ex.getMessage() != null  && !ex.getMessage().isEmpty()){
             message = ex.getMessage();
         } else {
             message = ex.getClass().getName();
         }
         outputBuilder.append(message);
         try {
             JSONObject properties = main.getJSONObject("properties"); 
             properties.put("status", "Pipe Failed");
             properties.put("details",outputBuilder.toString());
             properties.put("error", message);
         } catch (JSONException newEx) {
             System.err.println("Error writing error to json");
             newEx.printStackTrace();
             throw new ServletException(newEx);
         }
     }
 
     /**
      * Converts the Json object back into string to pass to the client.
      * @param name Name of the pipe
      * @param working The Pipe as json (after it was run)
      * @param language The WireIt language the working belongs to.
      * @return String represention (encoded) or the pipe.
      */
     private String getOutput(String name, JSONObject working, String language){
         StringBuilder builder = new StringBuilder();
         builder.append("{\"id\":\"0\",\n");
         builder.append("\"name\":\"");
         builder.append(name);
         builder.append("\",\n");
         String workingSt = URLEncoder.encode(working.toString());
         workingSt = workingSt.replace("\"","\\\"");
         builder.append("\"working\":\"");
         builder.append(workingSt);
         builder.append("\",\n");
         builder.append("\"language\":\"");
         builder.append(language);
         builder.append("\"}");
         return builder.toString();
     }
     
     /**
      * Splits the request body into a map of paramters.
      * @param input Body as a Single String
      * @return Body as a map of Names to values.
      */
     private HashMap<String, String> convertBody(String input) {
         StringTokenizer tokens = new StringTokenizer(input, "&");
         HashMap<String, String> parameters = new HashMap<String, String>();
         while (tokens.hasMoreElements()){
             String token = tokens.nextToken();
             String key = token.substring(0,token.indexOf("="));
             String encoded = token.substring(token.indexOf("=")+1,token.length());
             String decoded = URLDecoder.decode(encoded);
             parameters.put(key, decoded);
         }
         return parameters;
     }
 
     /**
      * Workhorse method that does most of the actual work running a Pipe.
      * <p>
      * See Wiring which does (and explains) the construction and running of the modules.
      * 
      * @param jsonInput The json as received from the request.
      * @param outputBuilder Logger for any information modules may add.
      * @param resolver Util to convert between files, absolute uri and relative uris
      * @return The json after execution.
      * @throws JSONException Thrown it the json is not in the expected format.
      * @throws TavernaException Thrown by the TavernaModule if the information is inconsistant. 
      * @throws IOException Thrown by the TavernaModule if the workflow is unreadable.
      * @throws WireItRunException Any Exception while running will be caught and wrapped in a single Exception type.
      */
     private JSONObject doRun(JSONObject jsonInput, StringBuilder outputBuilder, Resolver resolver) 
             throws WireItRunException, JSONException, TavernaException, IOException{
         Wiring wiring = new Wiring(jsonInput, resolver);
        outputBuilder.append("Workflow loaded at ");
         outputBuilder.append(new Date());
         outputBuilder.append("\n");
         wiring.run(outputBuilder);
         return wiring.getJsonObject();
 
     }
 
 }
