 package cs.wintoosa.controller.interceptor;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import cs.wintoosa.service.ChecksumChecker;
 import java.io.IOException;
 import java.util.logging.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 /**
  *
  * @author jonimake
  */
 public class ValidationInterceptor extends HandlerInterceptorAdapter {
     
    private static final Logger logger = Logger.getLogger(ValidationInterceptor.class .getName()); 
     private final int BAD_REQUEST = 400;
     
     public ValidationInterceptor() {
         super();
         
     }
     
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         
         boolean isOk = super.preHandle(request, response, handler);
         if(request == null)
             return false;
         if(!request.getMethod().equalsIgnoreCase("PUT"))
             return isOk; //only handle PUTs
         
         JsonObject json = convertToJsonObject(request);
        
         if(!isValid(json)) {
             
             logger.log(Level.SEVERE,"Invalid json: \n"
                     + json.toString());
             
             response.getWriter().write("json validation failed");
             return false;
         }
         return true;
     }
     
     private JsonObject convertToJsonObject(HttpServletRequest request) throws IOException {
         if(request.getContentLength() == -1) {
             return null;
         }
         
         byte[] buffer = new byte[request.getContentLength()];
         request.getInputStream().read(buffer, 0, request.getContentLength());
         
         String data = new String(buffer);
         logger.info("data = ".concat(data));
         
         JsonParser parser = new JsonParser();
         JsonObject json = parser.parse(data).getAsJsonObject();
         logger.info("json = ".concat(json.toString()));
         return json;
     }
     
     private boolean isValid(JsonObject json) {
         if(json == null) {
             logger.info("json is null");
             return false;
         }
         
         JsonElement checksumJson = json.get("checksum");
         
         if(checksumJson == null) {
             logger.info("checksum json is null");
             return false;
         }
         
         String checksum = checksumJson.getAsString();
         json.remove("checksum");
         String calculatedChecksum = ChecksumChecker.calcSHA1(json.toString());
         
         logger.info(checksum + " equals " + calculatedChecksum + ": " + checksum.equalsIgnoreCase(calculatedChecksum));
         
         return calculatedChecksum.equalsIgnoreCase(checksum);
     }
 }
