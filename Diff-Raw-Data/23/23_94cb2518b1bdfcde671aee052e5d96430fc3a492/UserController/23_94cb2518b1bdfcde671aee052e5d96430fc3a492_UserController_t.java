 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.service.LanguageService;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationUtil;
 import org.apache.cocoon.auth.User;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class UserController implements StatelessAppleController {
 
   private SparulDispatcher sparulDispatcher;
   AdminService adminService = new AdminService();
   DatabaseService dbService = new DatabaseService();
   private ApplicationUtil appUtil = new ApplicationUtil();
   private User user;
   private String userPrivileges = "<empty/>";
   private boolean loggedIn = false;
   private String mode;
   private String submode;
   String[] completePrefixArray = {
           "PREFIX dct: <http://purl.org/dc/terms/>",
           "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
           "PREFIX sub: <http://xmlns.computas.com/sublima#>",
           "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
           "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
           "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
           "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>",
           "PREFIX sioc: <http://rdfs.org/sioc/ns#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
 
  private static Logger logger = Logger.getLogger(UserController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
     this.submode = req.getSitemapParameter("submode");
     if (appUtil.getUser() != null) {
       user = appUtil.getUser();
       userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
     }
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("UserController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("UserController: Language from service is " + language);
 
 
     if ("brukere".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode)) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
         System.gc();
         res.sendPage("xml2/brukere", bizData);
       } else if ("ny".equalsIgnoreCase(submode)) {
         editUser(req, res, "ny", null);
       } else if ("bruker".equalsIgnoreCase(submode)) {
         editUser(req, res, "edit", null);
       } else if ("alle".equalsIgnoreCase(submode)) {
         listUsers(req, res);
       } else {
         res.sendStatus(404);
       }
     } else if ("roller".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
         System.gc();
         res.sendPage("xml2/roller", bizData);
       } else if ("rolle".equalsIgnoreCase(submode)) {
         editRole(req, res, null);
       } else if ("alle".equalsIgnoreCase(submode)) {
         listRoles(req, res);
       }
     }
   }
 
   private void listRoles(AppleRequest req, AppleResponse res) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("allroles", adminService.getAllRoles());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     System.gc();
     res.sendPage("xml2/roller_alle", bizData);
   }
 
   public void editUser(AppleRequest req, AppleResponse res, String type, String messages) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("allroles", adminService.getAllRoles());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
 
     // Check whether the logged in user requests his/hers own page. If so, allow editing.
     if (adminService.getUserByURI(
             req.getCocoonRequest().getParameter("the-resource")).contains(
             user.getId())) {
       userPrivileges = userPrivileges.replace("</c:privileges>", "<c:privilege>user.edit</c:privilege></c:privileges>");
     }
 
     bizData.put("userprivileges", userPrivileges);
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       logger.trace("UserController.editUser --> Serve GET requests\n");
       bizData.put("tempvalues", "<empty></empty>");
 
       if ("ny".equalsIgnoreCase(type)) {
         bizData.put("userdetails", "<empty></empty>");
       } else {
         bizData.put("userdetails", adminService.getUserByURI(req.getCocoonRequest().getParameter("the-resource")));
       }
 
       bizData.put("mode", "useredit");
       bizData.put("messages", "<empty></empty>");
       System.gc();
       res.sendPage("xml2/bruker", bizData);
 
       // When POST try to save the user. Return error messages upon failure, and success message upon great success
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
       logger.trace("UserController.editUser --> Serve POST requests\n");
       // 1. Mellomlagre alle verdier
       // 2. Valider alle verdier
       // 3. Forsk  lagre
 
       StringBuilder tempValues = getUserTempValues(req);
       String tempPrefixes = "<c:tempvalues \n" +
               "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
               "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
               "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
               "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
               "xmlns:sioc= \"http://rdfs.org/sioc/ns#\">\n";
 
       String validationMessages = validateUserRequest(req);
       if (!"".equalsIgnoreCase(validationMessages)) {
         messageBuffer.append(validationMessages + "\n");
         messageBuffer.append("</c:messages>\n");
 
         bizData.put("userdetails", "<empty></empty>");
         bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
         bizData.put("messages", messageBuffer.toString());
         bizData.put("mode", "usertemp");
         System.gc();
         res.sendPage("xml2/bruker", bizData);
 
       } else {
 
         boolean newUser = false;
 
         // NEW USER
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("the-resource")) || req.getCocoonRequest().getParameter("the-resource") == null) {
 
           if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("password1")) || req.getCocoonRequest().getParameter("password1").equalsIgnoreCase("passwordplaceholder")) { // NO PASSWORD GIVEN
             logger.trace("UserController.editUser --> NEW USER: Password not given\n");
             messageBuffer.append("<c:message><i18n:text xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" key=\"validation.user.new.nopassword\">Et nytt passord må angis</i18n:text></c:message>\n</c:messages>\n");
 
             bizData.put("userdetails", "<empty></empty>");
             bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
             bizData.put("messages", messageBuffer.toString());
             bizData.put("mode", "usertemp");
             System.gc();
             res.sendPage("xml2/bruker", bizData);
             return;
           }
 
           if (!req.getCocoonRequest().getParameter("password1").equals(req.getCocoonRequest().getParameter("password2"))) { // PASSWORDS DON'T MATCH
             logger.trace("UserController.editUser --> NEW USER: Password not given\n");
             messageBuffer.append("<c:message><i18n:text xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" key=\"validation.user.new.passworddontmatch\">Passordet må være likt i begge feltene.</i18n:text></c:message>\n</c:messages>\n");
 
             bizData.put("userdetails", "<empty></empty>");
             bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
             bizData.put("messages", messageBuffer.toString());
             bizData.put("mode", "usertemp");
             System.gc();
             res.sendPage("xml2/bruker", bizData);
             return;
           }
 
           try {
             String password = adminService.generateSHA1(req.getCocoonRequest().getParameter("password1"));
             String insertSql = "INSERT INTO users "
                     + "(username, password) "
                     + "VALUES "
                     + "('" + req.getCocoonRequest().getParameter("sioc:email") + "', '" + password + "')";
 
             int insertedRows = dbService.doSQLUpdate(insertSql);
             logger.debug("UserController.editUser --> We have a new user.\n");
 
 
           } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
           } catch (UnsupportedEncodingException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
           } catch (SQLException e) { // Cause most probably that user already exists
             logger.trace("UserController.editUser --> NEW USER: INSERT USERNAME FAILED\n" + e.toString() + "\n");
             messageBuffer.append("<c:message><i18n:text xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" key=\"validation.user.new.existingusername\">En bruker med dette brukernavnet finnes allerede.</i18n:text></c:message>\n</c:messages>\n");
 
             bizData.put("userdetails", "<empty></empty>");
             bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
             bizData.put("messages", messageBuffer.toString());
             bizData.put("mode", "usertemp");
             System.gc();
             res.sendPage("xml2/bruker", bizData);
             return;
           }
         } else { // EXISTING USER
 
           // If the user has changed her e-mail address (username), we must update it in the USER-table
           if (!(req.getCocoonRequest().getParameter("sioc:email")).equalsIgnoreCase(req.getCocoonRequest().getParameter("oldusername"))) {
             //Strip away the sioc mailto
             String insertSql;
 
             //String[] username = req.getCocoonRequest().getParameter("oldusername").split(":");
             insertSql = "UPDATE users "
                     + "SET username = '" + req.getCocoonRequest().getParameter("sioc:email") + "' "
                     + "WHERE username = '" + req.getCocoonRequest().getParameter("oldusername") + "'";
 
             try {
               int insertedRows = dbService.doSQLUpdate(insertSql);
             } catch (SQLException e) {
               e.printStackTrace();
               logger.error("UserController.editUser --> EXISTING USER: INSERT USERINFO FAILED\n");
             }
 
           }
 
           // If the given password is "passwordplaceholder", then the user hasn't changed her password and we leave it as it is
           if (!req.getCocoonRequest().getParameter("password1").equalsIgnoreCase("passwordplaceholder")) {
             // Encrypt the password and store it in a database table
             int insertedRows = 0;
             try {
               String password = adminService.generateSHA1(req.getCocoonRequest().getParameter("password1"));
               logger.trace("UserController.editUser --> GENERATE PASSWORD SHA1 " + password + "\n");
 
               String insertSql = "UPDATE users "
                       + "SET password = '" + password + "' "
                       + "WHERE username = '" + req.getCocoonRequest().getParameter("sioc:email") + "'";
 
               logger.trace("UserController.editUser --> INSERT USERINFO:\n" + insertSql);
 
               try {
                 insertedRows = dbService.doSQLUpdate(insertSql);
               } catch (SQLException e) {
                 e.printStackTrace();
                 logger.error("UserController.editUser --> INSERT USERINFO FAILED\n");
               }
 
 
             } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
             } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
             }
 
             // If insertedRows is 0, then the insert failed and we give the user a proper feedback
             if (insertedRows == 0) {
               messageBuffer.append("<c:message><i18n:text key=\"user.login.failed\">En feil skjedde ved innlegging av passordet, vennligst kontroller alle felter og prv igjen</i18n:text></c:message>" + "\n");
               messageBuffer.append("</c:messages>\n");
               bizData.put("userdetails", "<empty></empty>");
               bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
               bizData.put("messages", messageBuffer.toString());
               bizData.put("mode", "usertemp");
               System.gc();
               res.sendPage("xml2/bruker", bizData);
               return;
             }
 
           }
         }
 
         Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
 
         Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
         parameterMap.remove("prefix"); // The prefixes are magic variables
         parameterMap.remove("password1"); // The passwords as stored seperatly in an RDB
         parameterMap.remove("password2"); // The passwords as stored seperatly in an RDB
         parameterMap.remove("oldusername"); // Field to check wether the user changes the username (email) or not
         parameterMap.remove("actionbutton"); // The name of the submit button
         String email = req.getCocoonRequest().getParameter("sioc:email");
         parameterMap.remove("sioc:email");
         parameterMap.put("sioc:email", new String[]{"mailto:" + email + ""});
         if (parameterMap.get("subjecturi-prefix") != null) {
           parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                   parameterMap.get("subjecturi-prefix")[0]});
         }
 
         String sparqlQuery = null;
         try {
           sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
         }
         catch (IOException e) {
           messageBuffer.append("<c:message><i18n:text key=\"user.save.error\">Feil ved lagring av bruker</i18n:text></c:message>\n");
         }
 
         boolean insertSuccess = sparulDispatcher.query(sparqlQuery);
 
         logger.trace("UserController.editUser --> INSERT QUERY:\n" + sparqlQuery);
 
         logger.debug("UserController.editUser --> INSERT QUERY RESULT: " + insertSuccess);
 
         if (insertSuccess) {
           messageBuffer.append("<c:message><i18n:text key=\"user.updated\">Bruker oppdatert!</i18n:text></c:message>\n");
           bizData.put("userdetails", adminService.getUserByURI(form2SparqlService.getURI()));
           bizData.put("tempvalues", "<empty></empty>");
           bizData.put("mode", "useredit");
 
         } else {
           messageBuffer.append("<c:message><i18n:text key=\"user.update.failed\">Feil ved oppdatering av bruker</i18n:text></c:message>\n");
           bizData.put("userdetails", "<empty></empty>");
           bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
           bizData.put("mode", "usertemp");
         }
 
         messageBuffer.append("</c:messages>\n");
         bizData.put("messages", messageBuffer.toString());
         System.gc();
         res.sendPage("xml2/bruker", bizData);
       }
     }
   }
 
 
   public void editRole
           (AppleRequest
                   req, AppleResponse
                   res, String
                   messages) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("allroles", adminService.getAllRoles());
     bizData.put("allstatuses", adminService.getAllStatuses());
     bizData.put("userprivileges", userPrivileges);
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
 
 //bizData.put("allanguages", adminService.getAllLanguages());
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       bizData.put("tempvalues", "<empty></empty>");
 
       if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("uri")) || req.getCocoonRequest().getParameter("uri") == null) {
         bizData.put("roledetails", "<empty></empty>");
         bizData.put("roleprivileges", "<empty></empty>");
       } else {
         bizData.put("roledetails", adminService.getRoleByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("roleprivileges", adminService.getRolePrivilegesAsXML(req.getCocoonRequest().getParameter("uri")));
       }
 
 
       bizData.put("mode", "roleedit");
       bizData.put("messages", "<empty></empty>");
       System.gc();
       res.sendPage("xml2/rolle", bizData);
 
       // When POST try to save the user. Return error messages upon failure, and success message upon great success
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
 
       // 1. Mellomlagre alle verdier
       // 2. Valider alle verdier
       // 3. Forsk  lagre
 
       StringBuilder tempValues = getRoleTempValues(req);
       String tempPrefixes = "<c:tempvalues \n" +
               "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
               "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
               "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
               "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
               "xmlns:sioc= \"http://rdfs.org/sioc/ns#\">\n";
 
       String validationMessages = validateRoleRequest(req);
       if (!"".equalsIgnoreCase(validationMessages)) {
         messageBuffer.append(validationMessages + "\n");
         messageBuffer.append("</c:messages>\n");
 
         bizData.put("allstatuses", adminService.getAllStatuses());
         bizData.put("roledetails", "<empty></empty>");
         bizData.put("roleprivileges", "<empty></empty>");
         bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
         bizData.put("messages", messageBuffer.toString());
         bizData.put("mode", "roletemp");
         System.gc();
         res.sendPage("xml2/rolle", bizData);
 
       } else {
 
         // Generate an identifier if a uri is not given and insert the user in the USER-table
         String uri;
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("uri")) || req.getCocoonRequest().getParameter("uri") == null) {
           uri = req.getCocoonRequest().getParameter("rdfs:label").replace(" ", "_");
           uri = uri.replace(",", "_");
           uri = uri.replace(".", "_");
           uri = getProperty("sublima.base.url") + "role/" + uri;
         } else {
           uri = req.getCocoonRequest().getParameter("uri");
         }
 
         StringBuilder insertString = new StringBuilder();
         insertString.append(completePrefixes);
         insertString.append("\nDELETE {\n");
         insertString.append("<" + uri + "> ?o ?p .\n}\n");
         insertString.append("WHERE {\n");
         insertString.append("<" + uri + "> ?o ?p .\n}\n");
         insertString.append("INSERT DATA {\n");
         insertString.append("<" + uri + "> a sioc:Role ;\n");
         insertString.append("    rdfs:label \"" + req.getCocoonRequest().getParameter("rdfs:label") + "\"@no ;\n}");
 
         boolean insertSuccess = sparulDispatcher.query(insertString.toString());
 
 
         logger.trace("UserController.editRole --> INSERT QUERY:\n" + insertString.toString());
         logger.debug("UserController.editRole --> INSERT QUERY RESULT: " + insertSuccess);
 
         if (insertSuccess) {
 
           // Delete old privileges to shortcut INSERT/UPDATE check. Add the role privilegies to the database
           try {
             String deletePrivilegeSql = "DELETE FROM roleprivilege WHERE role ='" + uri + "';";
             dbService.doSQLUpdate(deletePrivilegeSql);
             String insertPrivilegesSql;
 
             if (req.getCocoonRequest().getParameterValues("privileges") != null) {
               for (String s : req.getCocoonRequest().getParameterValues("privileges")) {
                 insertPrivilegesSql = "INSERT INTO roleprivilege(role, privilege) VALUES('" + uri + "','" + s + "');";
                 logger.trace(insertPrivilegesSql);
                 dbService.doSQLUpdate(insertPrivilegesSql);
               }
             }
 
             if (req.getCocoonRequest().getParameterValues("resource.status") != null) {
               for (String s : req.getCocoonRequest().getParameterValues("resource.status")) {
                 insertPrivilegesSql = "INSERT INTO roleprivilege(role, privilege) VALUES('" + uri + "','resource.status." + s + "');";
                 logger.trace(insertPrivilegesSql);
                 dbService.doSQLUpdate(insertPrivilegesSql);
               }
             }
 
             if (req.getCocoonRequest().getParameterValues("topic.status") != null) {
               for (String s : req.getCocoonRequest().getParameterValues("topic.status")) {
                 insertPrivilegesSql = "INSERT INTO roleprivilege(role, privilege) VALUES('" + uri + "','topic.status." + s + "');";
                 logger.trace(insertPrivilegesSql);
                 dbService.doSQLUpdate(insertPrivilegesSql);
               }
             }
           }
           catch (SQLException e) {
             e.printStackTrace();
           }
 
           messageBuffer.append("<c:message><i18n:text key=\"role.updated\">Rolle oppdatert!</i18n:text></c:message>\n");
           bizData.put("roledetails", adminService.getRoleByURI(uri));
           bizData.put("roleprivileges", adminService.getRolePrivilegesAsXML(uri));
           bizData.put("tempvalues", "<empty></empty>");
           bizData.put("mode", "roleedit");
 
         } else {
           messageBuffer.append("<c:message><i18n:text key=\"role.update.failed\">Feil ved oppdatering av rolle</i18n:text></c:message>\n");
           bizData.put("roledetails", "<empty></empty>");
           bizData.put("roleprivileges", "<empty></empty>");
           bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
           bizData.put("mode", "roletemp");
         }
 
         bizData.put("allstatuses", adminService.getAllStatuses());
         messageBuffer.append("</c:messages>\n");
         bizData.put("messages", messageBuffer.toString());
         System.gc();
         res.sendPage("xml2/rolle", bizData);
       }
     }
   }
 
   public void listUsers
           (AppleRequest
                   req, AppleResponse
                   res) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("allusers", adminService.getAllUsers());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     System.gc();
     res.sendPage("xml2/brukere_alle", bizData);
   }
 
   private String validateUserRequest
           (AppleRequest
                   req) {
     StringBuilder validationMessages = new StringBuilder();
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("sioc:email")) || req.getCocoonRequest().getParameter("sioc:email") == null) {
       validationMessages.append("<c:message><i18n:text key=\"user.username.blank\">E-post (brukernavn) kan ikke være blank</i18n:text></c:message>\n");
     }
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("rdfs:label")) || req.getCocoonRequest().getParameter("rdfs:label") == null) {
       validationMessages.append("<c:message><i18n:text key=\"user.name.blank\">Navn kan ikke være blank</i18n:text></c:message>\n");
     }
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("password1")) ||
             req.getCocoonRequest().getParameter("password1") == null ||
             "".equalsIgnoreCase(req.getCocoonRequest().getParameter("password2")) ||
             req.getCocoonRequest().getParameter("password2") == null) {
       validationMessages.append("<c:message><i18n:text key=\"user.password.blank\">Passordfeltene kan ikke være blanke, og må være like</i18n:text></c:message>\n");
     }
 
     if (!req.getCocoonRequest().getParameter("password1").equals(req.getCocoonRequest().getParameter("password2"))) {
       validationMessages.append("<c:message><i18n:text key=\"user.password.equal\">Passordfeltene m vre like</i18n:text></c:message>\n");
     }
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("sioc:has_function")) || req.getCocoonRequest().getParameter("sioc:has_function") == null) {
       validationMessages.append("<c:message><i18n:text key=\"user.norole\">En rolle m vre valgt</i18n:text></c:message>\n");
     }
 
     return validationMessages.toString();
   }
 
   private String validateRoleRequest
           (AppleRequest
                   req) {
     StringBuilder validationMessages = new StringBuilder();
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("rdfs:label")) || req.getCocoonRequest().getParameter("rdfs:label") == null) {
       validationMessages.append("<c:message><i18n:text key=\"user.name.blank\">Navn kan ikke vre blank</i18n:text></c:message>\n");
     }
 
     return validationMessages.toString();
   }
 
   private StringBuilder getUserTempValues
           (AppleRequest
                   req) {
     StringBuilder tempValues = new StringBuilder();
 
     String uri = req.getCocoonRequest().getParameter("the-resource");
     String temp_email = req.getCocoonRequest().getParameter("sioc:email");
     String temp_name = req.getCocoonRequest().getParameter("rdfs:label");
     String temp_oldusername = req.getCocoonRequest().getParameter("oldusername");
     String temp_role = req.getCocoonRequest().getParameter("sioc:has_function");
 
     //Create an XML structure for the selected values, to use in the JX template
     tempValues.append("<rdf:about>" + uri + "</rdf:about>\n");
     tempValues.append("<sioc:email>" + temp_email + "</sioc:email>\n");
     tempValues.append("<rdfs:label>" + temp_name + "</rdfs:label>\n");
     tempValues.append("<c:oldusername>" + temp_oldusername + "</c:oldusername>");
     tempValues.append("<sioc:has_function rdf:resource=\"" + temp_role + "\"/>");
 
     return tempValues;
   }
 
   private StringBuilder getRoleTempValues
           (AppleRequest
                   req) {
     StringBuilder tempValues = new StringBuilder();
 
     String uri = req.getCocoonRequest().getParameter("uri");
     String temp_name = req.getCocoonRequest().getParameter("rdfs:label");
 
 //Create an XML structure for the selected values, to use in the JX template
     tempValues.append("<rdf:about>" + uri + "</rdf:about>\n");
     tempValues.append("<rdfs:label>" + temp_name + "</rdfs:label>\n");
 
     tempValues.append("<c:privileges>\n");
     if (req.getCocoonRequest().getParameterValues("privileges") != null) {
       for (String s : req.getCocoonRequest().getParameterValues("privileges")) {
         tempValues.append("<c:privilege>" + s + "</c:privilege>\n");
       }
     }
     tempValues.append("</c:privileges>\n");
 
 
     return tempValues;
   }
 
   public void setSparulDispatcher
           (SparulDispatcher
                   sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   //todo Move to a Service-class
   private Map<String, String[]> createParametersMap(Request request) {
     Map<String, String[]> result = new HashMap<String, String[]>();
     Enumeration parameterNames = request.getParameterNames();
     while (parameterNames.hasMoreElements()) {
       String paramName = (String) parameterNames.nextElement();
       result.put(paramName, request.getParameterValues(paramName));
     }
     return result;
   }
 }
 
