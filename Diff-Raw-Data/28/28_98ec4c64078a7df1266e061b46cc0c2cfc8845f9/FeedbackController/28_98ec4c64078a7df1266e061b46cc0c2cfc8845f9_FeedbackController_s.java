 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.URLActions;
 import com.computas.sublima.query.SparulDispatcher;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.log4j.Logger;
 
 import java.util.Map;
 import java.util.HashMap;
 
 public class FeedbackController implements StatelessAppleController {
 
   private static Logger logger = Logger.getLogger(FeedbackController.class);
   private String mode;
   private SparulDispatcher sparulDispatcher;
   boolean success = false;
   private ApplicationManager appMan;
 
   //todo Check how to send error messages with Cocoon (like Struts 2's s:actionmessage)
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
     boolean loggedIn = appMan.isLoggedIn("Sublima");
 
     if ("resourcecomment".equalsIgnoreCase(mode)) {
       String uri = req.getCocoonRequest().getParameter("uri");
       String email = req.getCocoonRequest().getParameter("email");
       String comment = req.getCocoonRequest().getParameter("comment");
 
       String commentString = StringUtils.join("\n", new String[]{
               "PREFIX dct: <http://purl.org/dc/terms/>",
               "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
               "PREFIX sub: <http://xmlns.computas.com/sublima#>",
               "PREFIX sioc:<http://rdfs.org/sioc/ns#>",
               "INSERT",
               "{",
              "    <" + uri + ">" + " sub:comment ?a .",
              "        ?a a sioc:Item ;",
              "            sioc:content \"" + comment + "\" ;",
              "        sioc:has_creator ?b .",
              "        ?b a sioc:User ;" +
              "            sioc:email <mailto:" + email + "> .",
              "}",
              "WHERE",
              "{",
              "    <" + uri + ">" + " sub:comment ?a .",
              "        ?a a sioc:Item ;",
              "            sioc:content \"" + comment + "\" ;",
              "        sioc:has_creator ?b .",
              "        ?b a sioc:User ;" +
               "            sioc:email <mailto:" + email + "> .",
               "}"});
 
       success = sparulDispatcher.query(commentString);
       logger.trace("FeedbackController.java --> Comment on resource: " + commentString + "\nResult: " + success);
 
       //todo Back to the resource details page
       if (success) {
         res.redirectTo(req.getCocoonRequest().getParameter("resource"));
         return;
       } else {
         res.redirectTo(req.getCocoonRequest().getParameter("resource"));
         return;
       }
     }
 
     if ("visTipsForm".equalsIgnoreCase(mode)) {
       res.sendPage("xhtml/tips", null);
       return;
     }
 
     if ("tips".equalsIgnoreCase(mode)) {
       Map<String, Object> bizData = new HashMap<String, Object>();
 
       StringBuffer messageBuffer = new StringBuffer();
       messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\"></c:messages>");
       bizData.put("messages", messageBuffer.toString());
       bizData.put("mode", "form");
       bizData.put("loggedin", loggedIn);
       res.sendPage("xml/tips", bizData);
       return;
     }
 
     if ("sendtips".equalsIgnoreCase(mode)) {
 
       Map<String, Object> bizData = new HashMap<String, Object>();
 
       StringBuffer messageBuffer = new StringBuffer();
       messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
       String url = req.getCocoonRequest().getParameter("url");
       String tittel = req.getCocoonRequest().getParameter("tittel");
       String beskrivelse = req.getCocoonRequest().getParameter("beskrivelse");
       String[] stikkord = req.getCocoonRequest().getParameter("stikkord").split(",");
       String status;
 
       try {
         // Do a URL check so that we know we have a valid URL
         URLActions urlAction = new URLActions(url);
         status = urlAction.getCode();
       }
       catch (NullPointerException e) {
         e.printStackTrace();
         messageBuffer.append("<c:message>Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</c:message>");
         messageBuffer.append("</c:messages>\n");
         bizData.put("messages", messageBuffer.toString());
         bizData.put("mode", "form");
         bizData.put("loggedin", loggedIn);
         res.sendPage("xml/tips", bizData);
         return;  
       }
 
       //todo We have to get the interface-language @no from somewhere
       if ("200".equals(status)) {
         String insertTipString =
                 "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                         "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                         "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                         "INSERT\n" +
                         "{\n" +
                         "<" + url + "> a sub:Resource .\n" +
                         "<" + url + "> dct:title " + "\"" + tittel + "\"@no . \n" +
                         "<" + url + "> dct:description " + "\"" + beskrivelse + "\"@no . \n" +
                         "<" + url + "> sub:keywords " + "\"" + stikkord.toString() + "\"@no . \n" +
                        "<" + url + "> wdr:describedBy <http://sublima.computas.com/status/til_godkjenning> . }\n";
 
         success = sparulDispatcher.query(insertTipString);
         logger.trace("sendTips --> RESULT: " + success);
 
         if (success) {
           messageBuffer.append("<c:message>Ditt tips er mottatt. Tusen takk :)</c:message>");
           messageBuffer.append("</c:messages>\n");
           bizData.put("messages", messageBuffer.toString());
           bizData.put("mode", "ok");
           bizData.put("loggedin", loggedIn);
           res.sendPage("xml/tips", bizData);
           return;
         } else {
           messageBuffer.append("<c:message>Det skjedde noe galt. Kontroller alle feltene og prv igjen</c:message>");
           messageBuffer.append("</c:messages>\n");
           bizData.put("mode", "form");
           bizData.put("loggedin", loggedIn);
           bizData.put("messages", messageBuffer.toString());
           res.sendPage("xml/tips", bizData);
           return;
         }
 
       } else {
         messageBuffer.append("<c:message>Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</c:message>");
         messageBuffer.append("</c:messages>\n");
         bizData.put("mode", "form");
         bizData.put("loggedin", loggedIn);
         res.sendPage("xml/tips", bizData);
         return;
       }
     }
 
     return;
   }
 
 
   public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   public void setAppMan(ApplicationManager appMan) {
     this.appMan = appMan;
   }
 
 }
