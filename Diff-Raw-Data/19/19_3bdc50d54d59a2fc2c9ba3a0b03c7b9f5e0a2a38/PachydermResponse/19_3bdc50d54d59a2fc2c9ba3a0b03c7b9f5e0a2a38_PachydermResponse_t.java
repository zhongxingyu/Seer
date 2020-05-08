 package org.tortiepoint.pachyderm;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.tortiepoint.pachyderm.dependency.DependencyResolver;
 import sun.org.mozilla.javascript.internal.NativeObject;
 
 import javax.script.ScriptContext;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.xml.bind.JAXB;
import java.io.FileInputStream;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 public class PachydermResponse {
 
     private String body = "";
     private String contentType = "";
     private int statusCode = 200;
     private final ScriptEngine scriptEngine;
 
     public PachydermResponse(ScriptEngine scriptEngine) {
         this.scriptEngine = scriptEngine;
     }
 
     public void renderStatus(int statusCode) {
         this.statusCode = statusCode;
     }
 
     public void renderJson(Object responseBody) throws Exception {
         contentType = "application/json";
         responseBody = responseBody instanceof NativeObject
                 ? PachydermUtils.objectToMap((NativeObject) responseBody)
                 : responseBody;
         body = new ObjectMapper().writeValueAsString(responseBody);
     }
 
     public void renderXml(Object responseBody) throws Exception {
         contentType = "application/xml";

         JAXB.marshal(responseBody, body);
     }
 
    public void render(String view, NativeObject model) throws Exception {
        Map data = PachydermUtils.objectToMap(model);
    }

     public String getBody() {
         return body;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     public String toString() {
         return body;
     }
 }
