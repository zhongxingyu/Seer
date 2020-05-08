 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.adhoc.ConvertSublimaResources;
 import com.computas.sublima.app.adhoc.ImportData;
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.IndexService;
 import com.computas.sublima.app.service.LanguageService;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.SettingsService;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationUtil;
 import org.apache.cocoon.auth.User;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.net.URLEncoder;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class AdminController implements StatelessAppleController {
 
   private SparulDispatcher sparulDispatcher;
   AdminService adminService = new AdminService();
   private ApplicationUtil appUtil = new ApplicationUtil();
   private User user;
   private String userPrivileges = "<empty/>";
   String[] completePrefixArray = {
           "PREFIX dct: <http://purl.org/dc/terms/>",
           "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
           "PREFIX sub: <http://xmlns.computas.com/sublima#>",
           "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
           "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
           "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
           "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
 
   private static Logger logger = Logger.getLogger(AdminController.class);
   ConvertSublimaResources convert = new ConvertSublimaResources();
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     String mode = req.getSitemapParameter("mode");
     String submode = req.getSitemapParameter("submode");
 
     if (appUtil.getUser() != null) {
       user = appUtil.getUser();
       userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
     }
 
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("AdminController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("AdminController: Language from service is " + language);
 
     if ("".equalsIgnoreCase(mode)) {
       Map<String, Object> bizData = new HashMap<String, Object>();
       bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
       System.gc();
       res.sendPage("xml2/admin", bizData);
     } else if ("testsparql".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode)) {
         System.gc();
         res.sendPage("xhtml/testsparql", null);
       } else {
         String query = req.getCocoonRequest().getParameter("query");
         res.redirectTo(req.getCocoonRequest().getContextPath() + "/sparql?query=" + URLEncoder.encode(query, "UTF-8"));
       }
     } else if ("testsparul".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode)) {
         System.gc();
         res.sendPage("xhtml/testsparul", null);
       } else {
         String query = req.getCocoonRequest().getParameter("query");
         boolean deleteResourceSuccess = sparulDispatcher.query(query);
 
         logger.trace("TestSparul:\n" + query);
         logger.trace("TestSparul result: " + deleteResourceSuccess);
         System.gc();
         res.sendPage("xhtml/testsparul", null);
       }
     } else if ("database".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode)) {
         uploadForm(res, req);
       } else if ("upload".equalsIgnoreCase(submode)) {
         uploadForm(res, req);
       } else if ("export".equalsIgnoreCase(submode)) {
         exportOntologyToXML(res, req);
       }
     } else if ("index".equalsIgnoreCase(mode)) {
      if ("".equals(submode)) {
         if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
           index(res, req);
         } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
           showIndexStatus(res, req);
         }
       }
 
     } else {
       res.sendStatus(404);
     }
   }
 
   private void showIndexStatus(AppleResponse res, AppleRequest req) {
     // Les indexstatistikk fra databasen
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("index", adminService.getIndexStatisticsAsXML());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     bizData.put("userprivileges", userPrivileges);
     res.sendPage("xml2/index", bizData);
   }
 
   private void index(AppleResponse res, AppleRequest req) {
     IndexService is = new IndexService();
 
     is.createResourceIndex();
     is.createTopicIndex();
 
     showIndexStatus(res, req);
 
   }
 
   private void exportOntologyToXML(AppleResponse res, AppleRequest req) throws Exception {
     adminService.insertSubjectOf();
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     String type = req.getCocoonRequest().getParameter("type");
 
     String query ="CONSTRUCT {?s ?p ?o} FROM <" + SettingsService.getProperty("sublima.basegraph") + "> WHERE {?s ?p ?o}";
     String url = SettingsService.getProperty("sublima.sparql.endpoint") + "?query=" + URLEncoder.encode(query, "UTF-8");
     URL u = new URL(url);
     HttpURLConnection con = (HttpURLConnection) u.openConnection();
     Model model = ModelFactory.createDefaultModel();
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     model.read(con.getInputStream(), "");
     model.write(out, type);
     bizData.put("ontology", out.toString());
     out.close();
     model.close();
     System.gc();
     res.sendPage("nostyle/export", bizData);
     adminService.deleteSubjectOf();
   }
 
   private void uploadForm(AppleResponse res, AppleRequest req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     bizData.put("userprivileges", userPrivileges);
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       System.gc();
       res.sendPage("xml2/upload", bizData);
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
 
       if (req.getCocoonRequest().getParameter("location") != null) {
         String type = req.getCocoonRequest().getParameter("type");
         File file = new File(req.getCocoonRequest().getParameter("location"));
         ImportData id = new ImportData();
 
         try {
           ConvertSublimaResources.applyRules(file.toURL().toString(), type, file.getCanonicalPath(), type);
           id.load(file.toURL().toString(), type);
         } catch (Exception e) {
           logger.trace("AdminController.uploadForm --> Error during loading of resource");
           e.printStackTrace();
         }
       }
       System.gc();
       res.sendPage("xml2/upload", bizData);
       adminService.deleteSubjectOf();
     }
   }
 
   public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 }
 
