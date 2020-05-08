 package com.p1software.p1lapchart;
 
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.client.RestTemplate;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 @Controller
 public class P1LapchartController implements InitializingBean {
   private static ConcurrentHashMap<String, JsonNode> cache = null;
   private static ObjectMapper mapper = null;
   private static P1LapchartLogger logger = null;
 
   public void afterPropertiesSet() {
     cache = new ConcurrentHashMap<String, JsonNode>();
     mapper = new ObjectMapper();
     logger = new P1LapchartLogger();
   }
   
   public static void addCORSHeaders(HttpServletResponse resp) {
     resp.addHeader("Access-Control-Allow-Origin", "*");
     resp.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
     resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
   }
 
   // e.g., http://localhost:8080/p1lapchartj/api/12345
   // @see http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/mvc.html#mvc-config
   // @see https://gist.github.com/kdonald/2012289/raw/363289ee8652823f770ef82f594e9a8f15048090/ExampleController.java
   @RequestMapping(value="/api/{id}", method=RequestMethod.GET)
   @ResponseBody
   public JsonNode getByID(@PathVariable String id, HttpServletRequest req, HttpServletResponse resp) {
 	JsonNode json = null;
     try {
       String sourceurl = getSource(id);  
       if ((json = cache.get(sourceurl)) != null) {
         logger.audit(req.getRemoteAddr(), id, "cached");
       } else {
         RestTemplate restTemplate = new RestTemplate();
         String mylaps_json = restTemplate.getForObject(sourceurl, String.class);
 	    logger.audit(req.getRemoteAddr(), id, "live");
         json = enhance(mylaps_json, sourceurl);
         cache.put(sourceurl,  json);
       }
       addCORSHeaders(resp);
     } catch (Exception e) {
       try {
         json = mapper.readTree("{'p1meta': {'status': 404}}");
       } catch (Exception ee) {
   	    logger.error("mapper.readTree", ee);
       }
     }
     return json;
   }
   
   private String getSource(String id) {
     return "http://www.mylaps.com/api/eventlapchart?id=" + id;
   }
 
   // @see http://wiki.fasterxml.com/JacksonInFiveMinutes
   private JsonNode enhance(String mylaps_json, String source) {
 	JsonNode rootNode = null;
     try {
       rootNode = mapper.readTree(mylaps_json);
 
       // Add data.meta
       ObjectNode metaObj = ((ObjectNode)rootNode).putObject("p1meta");
       metaObj.put("status", "0");
       metaObj.put("source", source);
 
       JsonNode lapchartNode = rootNode.path("lapchart");
       if (lapchartNode instanceof ObjectNode) {
         // Add data.laps by parsing data.lapchart.positions
         ObjectNode p1lapsObj = ((ObjectNode)rootNode).putObject("p1laps");
         int position = 0;
         for (JsonNode positionNode : lapchartNode.path("positions")) {
           if (positionNode.isArray()) {
             int lap = 0;
             for (JsonNode p : (ArrayNode)positionNode) {
               String startNumber = p.path("startNumber").textValue();	// car number
               try {
                 JsonNode p1lapsStartNumberNode = p1lapsObj.path(startNumber);
                 if (p1lapsStartNumberNode.isMissingNode()) {
                   p1lapsStartNumberNode = p1lapsObj.putArray(startNumber);
                 }
                ArrayNode p1lapsStartNumberArrayNode = (ArrayNode)p1lapsStartNumberNode;
                if (lap + 1 <= p1lapsStartNumberArrayNode.size()) {
                  p1lapsStartNumberArrayNode.remove(lap);	// remove old value so insert() is replace
                } else {
                  while (p1lapsStartNumberArrayNode.size() < lap) {
                    p1lapsStartNumberArrayNode.add(-1);		// dummy pad
                  }
                }
                p1lapsStartNumberArrayNode.insert(lap, position+1);
               } catch (Exception e) {
             	logger.error("Bad startNumberStr '" + startNumber + "' " + p, e);
               }
               lap++;
             } // lap
           }
           position++;
         } // position
       
         // Delete properties from original mylaps.com JSON that we don't use
         ((ObjectNode)lapchartNode).remove("laps");
         ((ObjectNode)lapchartNode).remove("positions");
         JsonNode participantsNode = lapchartNode.path("participants");
         if (participantsNode.isArray()) {
           for (JsonNode participantNode : (ArrayNode)participantsNode) {
             if (participantNode.isObject()) {
     	      ((ObjectNode)participantNode).remove("color");
             }
           }
         }
       } // lapchartNode
 
     } catch (IOException e) {
       logger.error("Unexpected IOException", e);
     }
     
     return rootNode;
   }
 }
