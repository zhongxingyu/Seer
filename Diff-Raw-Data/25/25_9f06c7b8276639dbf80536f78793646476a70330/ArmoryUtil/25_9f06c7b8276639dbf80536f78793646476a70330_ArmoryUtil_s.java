 package com.maritatf.wow;
 /**
  * @author fmaritato
  */
 
 import com.maritatf.wow.value.*;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.Version;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.module.SimpleModule;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ArmoryUtil {
 
     private static transient Log log = LogFactory.getLog(ArmoryUtil.class);
 
     public static final String ARMORY_BASE = "http://us.battle.net/api/wow";
 
 
     private static ObjectMapper objectMapper = new ObjectMapper();
     private static SimpleModule testModule = new SimpleModule("PopulationEnumModule",
                                                               new Version(1, 0, 0, null))
         .addDeserializer(PopulationEnum.class, new PopulationEnumDeserializer());
 
     static {
         objectMapper.registerModule(testModule);
     }
 
     public static Realm getRealmStatus(String realm) throws ArmoryException, IOException {
         StringBuilder options = new StringBuilder();
         options.append("realm=").append(realm);
         String json = getFromArmory("realm/status", options.toString());
         RealmList rl = objectMapper.readValue(json, RealmList.class);
         if (rl != null && rl.getRealms() != null && rl.getRealms().size() > 0) {
             return rl.getRealms().get(0);
         }
         throw new ArmoryException(json);
     }
 
     public static WowChar getCharacterInfo(String name, String realm) throws IOException {
        StringBuilder options = new StringBuilder();
        options.append("/").append(realm).append("/").append(name);
        String json = getFromArmory("character", options.toString());
         if (log.isDebugEnabled()) {
             log.debug(json);
         }
         WowChar toon = objectMapper.readValue(json, WowChar.class);
         if (log.isDebugEnabled()) {
             log.debug(toon.toString());
         }
         return toon;
     }
 
    public static String getGuildInfo(String name, String realm) {
        StringBuilder options = new StringBuilder();
        options.append("/").append(realm).append("/").append(name);
        String json = getFromArmory("guild", options.toString());
        // TODO: need to see what the output is going to be
        return null;
     }
 
     public static String getFromArmory(String command, String options) {
 
         HttpClient client = new HttpClient();
         client.getParams()
             .setParameter(HttpMethodParams.USER_AGENT, "Firefox/2.0");
 
         StringBuilder buf = new StringBuilder()
             .append(ARMORY_BASE).append("/")
             .append(command);
         if (options != null) {
             buf.append("/?").append(options);
         }
 
         if (log.isDebugEnabled()) {
             log.debug(buf.toString());
         }
 
         HttpMethod method = new GetMethod(buf.toString());
         try {
             // Execute the method.
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                 log.error("Method failed: " + method.getStatusLine());
             }
 
             // Read the response body.
             String resp = new String(method.getResponseBody());
             if (log.isDebugEnabled()) {
                 log.debug(resp);
             }
 
             return resp;
         }
         catch (IOException e) {
             log.error(e.getMessage(), e);
         }
 
         return null;
     }
 
     @Option(name = "--realm", usage = "Realm name")
     private String realm;
 
     @Argument
     private List<String> arguments = new ArrayList<String>();
 
     public void run(String[] args) throws CmdLineException, ArmoryException {
         CmdLineParser parser = new CmdLineParser(this);
         parser.parseArgument(args);
 
         if (realm != null) {
             Realm result = null;
             try {
                 result = getRealmStatus(realm);
             }
             catch (IOException e) {
                 log.error(e.getMessage(), e);
             }
             System.out.println(result);
         }
     }
 
     public static void main(String[] args) throws CmdLineException, ArmoryException {
         ArmoryUtil util = new ArmoryUtil();
         util.run(args);
     }
 }
