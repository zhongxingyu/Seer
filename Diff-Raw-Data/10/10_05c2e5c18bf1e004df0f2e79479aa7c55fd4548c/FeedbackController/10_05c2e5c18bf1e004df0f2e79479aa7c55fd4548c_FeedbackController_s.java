 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.URLActions;
 import com.computas.sublima.app.service.LanguageService;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import net.sf.akismet.Akismet;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.log4j.Logger;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class FeedbackController implements StatelessAppleController {
 
   private static Logger logger = Logger.getLogger(FeedbackController.class);
   private String mode;
   private SparulDispatcher sparulDispatcher;
   boolean success = false;
   private ApplicationManager appMan;
   private AdminService adminService = new AdminService();
   private SearchService searchService = new SearchService();
   boolean loggedIn;
 
   //todo Check how to send error messages with Cocoon (like Struts 2's s:actionmessage)
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
     loggedIn = appMan.isLoggedIn("Sublima");
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("FeedbackController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("FeedbackController: Language from service is " + language);
 
     if ("resourcecomment".equalsIgnoreCase(mode)) {
       commentOnResource(req, res);
     } else if ("visTipsForm".equalsIgnoreCase(mode)) {
       res.sendPage("xhtml/tips", null);
     } else if ("tips".equalsIgnoreCase(mode)) {
       showTipsForm(res);
     } else if ("sendtips".equalsIgnoreCase(mode)) {
       sendTips(req, res);
     }
   }
 
   private void sendTips(AppleRequest req, AppleResponse res) {
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
     String url = req.getCocoonRequest().getParameter("url");
     String tittel = req.getCocoonRequest().getParameter("tittel");
     String beskrivelse = req.getCocoonRequest().getParameter("beskrivelse");
     String stikkord = req.getCocoonRequest().getParameter("stikkord");
     String status;
 
     if (isSpam(req, loggedIn, url, tittel + "\n" + beskrivelse, "", "")) {
       logger.debug("FeedbackController.java --> Akismet said " + url + " was spam.");
       messageBuffer.append("<c:message><i18n:text key=\"feedback.spam\">Ressursen du sendte inn er markert i vår spam-database. Vennligst kontakt administrator!</i18n:text></c:message>");
       messageBuffer.append("</c:messages>\n");
       bizData.put("messages", messageBuffer.toString());
       bizData.put("mode", "form");
       bizData.put("loggedin", loggedIn);
 
       res.sendPage("xml/tips", bizData);
       return;
     } else {
       logger.trace("FeedbackController.java --> Akismet said " + url + " is ham.");
     }
 
     if ((!"".equals(url)) && adminService.checkForDuplicatesByURI(url)) {
       messageBuffer.append("<c:message><i18n:text key=\"tips.resourceexists\">Ressursen du tipset om finnes allerede.</i18n:text></c:message>");
       messageBuffer.append("</c:messages>\n");
       bizData.put("messages", messageBuffer.toString());
       bizData.put("mode", "form");
       bizData.put("loggedin", loggedIn);
 
       res.sendPage("xml/tips", bizData);
       return;
     }
 
     try {
       // Do a URL check so that we know we have a valid URL
       URLActions urlAction = new URLActions(url);
       status = urlAction.getCode();
     }
     catch (Exception e) {
       e.printStackTrace();
       messageBuffer.append("<c:message><i18n:text key=\"tips.invalidurl\">Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</i18n:text></c:message>");
       messageBuffer.append("</c:messages>\n");
       bizData.put("messages", messageBuffer.toString());
       bizData.put("mode", "form");
       bizData.put("loggedin", loggedIn);
 
       res.sendPage("xml/tips", bizData);
       return;
     }
 
    //todo We have to get the interface-language @no from somewhere
     //todo Move the 30x check to a private static method
     if (status != null && ("302".equals(status) ||
             "303".equals(status) ||
             "304".equals(status) ||
             "305".equals(status) ||
             "307".equals(status) ||
             status.startsWith("2"))) {
 
       String dctidentifier = searchService.sanitizeStringForURI(tittel);
       String insertTipString =
               "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                       "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                       "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                       "INSERT\n" +
                       "{\n" +
                       "<" + url + "> a sub:Resource .\n" +
                      "<" + url + "> dct:title " + "\"\"\"" + tittel + "\"\"\"@no . \n" +
                      "<" + url + "> dct:description " + "\"\"\"" + beskrivelse.replace("\\", "\\\\") + "\"\"\"@no . \n" +
                      "<" + url + "> sub:keywords " + "\"\"\"" + stikkord + "\"\"\"@no . \n" +
                       "<" + url + "> wdr:describedBy <http://sublima.computas.com/status/nytt_forslag> .\n" +
                       "<" + url + "> sub:url <" + url + "> . \n" +
                       "<" + url + "> dct:identifier <" + SettingsService.getProperty("sublima.base.url") + "resource/" + dctidentifier + url.hashCode() + "> . }";
 
 
       success = sparulDispatcher.query(insertTipString);
       logger.trace("sendTips --> RESULT: " + success);
 
       if (success) {
         messageBuffer.append("<c:message><i18n:text key=\"tips.received\">Ditt tips er mottatt. Tusen takk :)</i18n:text></c:message>");
         messageBuffer.append("</c:messages>\n");
         bizData.put("messages", messageBuffer.toString());
         bizData.put("mode", "ok");
         bizData.put("loggedin", loggedIn);
 
         res.sendPage("xml/tips", bizData);
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"tips.error\">Det skjedde noe galt. Kontroller alle feltene og prøv igjen</i18n:text></c:message>");
         messageBuffer.append("</c:messages>\n");
         bizData.put("mode", "form");
         bizData.put("loggedin", loggedIn);
         bizData.put("messages", messageBuffer.toString());
 
         res.sendPage("xml/tips", bizData);
       }
 
     } else {
       messageBuffer.append("<c:message><i18n:text key=\"tips.linkerror\">Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</i18n:text></c:message>");
       messageBuffer.append("</c:messages>\n");
       bizData.put("mode", "form");
       bizData.put("loggedin", loggedIn);
       bizData.put("messages", messageBuffer.toString());
 
       res.sendPage("xml/tips", bizData);
     }
   }
 
   private StringBuilder validateCommentForm(AppleRequest req) {
     StringBuilder validationMessages = new StringBuilder();
 
     String email = req.getCocoonRequest().getParameter("email");
     String comment = req.getCocoonRequest().getParameter("comment");
 
     if ("".equalsIgnoreCase(email)) {
       validationMessages.append("<c:message><i18n:text key=\"validation.comment.noemail\">uoversatt</i18n:text></c:message>\n");
     }
 
     if ("".equalsIgnoreCase(comment.trim())) {
       validationMessages.append("<c:message><i18n:text key=\"validation.comment.nocomment\">uoversatt</i18n:text></c:message>\n");
     }
 
     return validationMessages;
 
   }
 
   private void showTipsForm(AppleResponse res) {
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\"></c:messages>");
     bizData.put("messages", messageBuffer.toString());
     bizData.put("mode", "form");
     bizData.put("loggedin", loggedIn);
 
     res.sendPage("xml/tips", bizData);
   }
 
   private void commentOnResource(AppleRequest req, AppleResponse res) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     String uri = req.getCocoonRequest().getParameter("uri");
     String email = req.getCocoonRequest().getParameter("email");
     String comment = req.getCocoonRequest().getParameter("comment");
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\">\n");
 
     StringBuilder validationMessages = validateCommentForm(req);
 
     if (!"".equalsIgnoreCase(validationMessages.toString())) {
       messageBuffer.append(validationMessages).append("\n");
     } else if (isSpam(req, loggedIn, "", comment, email, uri)) {
       logger.info("FeedbackController.java --> Akismet said comment from " + email + " was spam. Silently ignored.");
       messageBuffer.append("<c:message><i18n:text key=\"validation.comment.isspam\">uoversatt</i18n:text></c:message>\n");
     } else {
       logger.trace("FeedbackController.java --> Akismet said comment from " + email + " is ham.");
 
       String generatedCommentURI = getProperty("sublima.base.url") + "comment/resource/" + email.hashCode() + comment.hashCode();
 
       Date date = new Date();
       DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
       String dateCommented = dateFormat.format(date); //"2008-18-09T13:39:38";
 
       String commentString = StringUtils.join("\n", new String[]{
               "PREFIX dct: <http://purl.org/dc/terms/>",
               "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
               "PREFIX sub: <http://xmlns.computas.com/sublima#>",
               "PREFIX sioc:<http://rdfs.org/sioc/ns#>",
               "INSERT",
               "{",
               "    <" + uri + ">" + " sub:comment <" + generatedCommentURI + "> .",
               "    <" + generatedCommentURI + "> a sioc:Item ;",
               "        sioc:content \"\"\"" + comment + "\"\"\" ;",
               "        dct:dateAccepted \"" + dateCommented + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;",
               "        sioc:has_creator \"\"\"" + email + "\"\"\" ;",
               "        sioc:has_owner <" + uri + "> .",
               "}"});
 
       success = sparulDispatcher.query(commentString);
       logger.trace("FeedbackController.java --> Comment on resource: " + commentString + "\nResult: " + success);
       messageBuffer.append("<c:message><i18n:text key=\"comment.received\">uoversatt</i18n:text></c:message>\n");
     }
     messageBuffer.append("</c:messages>\n");
     bizData.put("messages", messageBuffer.toString());
     bizData.put("result-list", adminService.describeResource(req.getCocoonRequest().getParameter("uri")));
     bizData.put("navigation", "<empty/>");
     bizData.put("mode", "resource");
     bizData.put("searchparams", "<empty/>");
     bizData.put("request", "<empty/>");
     bizData.put("loggedin", loggedIn);
     bizData.put("abovemaxnumberofhits", "false");
     res.sendPage("xml/sparql-result", bizData);
   }
 
   private boolean isSpam(AppleRequest req, boolean loggedIn, String url, String content, String email, String resource) {
     // If the user is not logged in, do a spam check if configured
     boolean spam = false;
     if (!loggedIn && getProperty("sublima.akismet.key") != null) {
       logger.info("FeedbackController.java --> Akismet key set");
       Akismet akismet = new Akismet(getProperty("sublima.akismet.key"),
               getProperty("sublima.base.url"));
       if (akismet.verifyAPIKey()) {
         spam = akismet.commentCheck(
                 req.getCocoonRequest().getRemoteAddr(),
                 "",
                 "",
                 resource,
                 "comment",
                 "",
                 email,
                 url,
                 content,
                 null
         );
 
       } else {
         logger.error("FeedbackController.java --> Akismet key not valid, disabling.");
       }
     }
     return spam;
   }
 
 
   public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   public void setAppMan(ApplicationManager appMan) {
     this.appMan = appMan;
   }
 
 }
