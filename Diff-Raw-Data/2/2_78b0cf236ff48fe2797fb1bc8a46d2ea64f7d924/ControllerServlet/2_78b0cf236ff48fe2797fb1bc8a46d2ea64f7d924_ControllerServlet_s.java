 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mwr.controller;
 
 import com.mwr.businesslogic.ScanSummary;
 import com.mwr.businesslogic.TokenGenerator;
 import com.mwr.database.DeviceId;
 import com.mwr.database.Scanresult;
 import com.mwr.database.Setting;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.HttpConstraint;
 import javax.servlet.annotation.ServletSecurity;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 
 /**
  *
  * @author madenem
  */
 @WebServlet(name = "ControllerServlet",
         loadOnStartup = 1,
         urlPatterns = {"/requestRegistration", "/scanResults", "/status","/restricted","/login","/mobileLogout","/logout"})
 public class ControllerServlet extends HttpServlet {
 
     /**
      *
      * @throws ServletException
      */
     @Override
     public void init() throws ServletException {
         // store category list in servlet context
         //System.out.println(techFacade.findAll().size());
     }
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 //        String userPath = request.getServletPath();
         String userPath = request.getServletPath();
         Logger.getLogger(ControllerServlet.class.getName()).info(userPath);
         String url = "";
        if (userPath.equals("/logout")) {
             url = "/faces/index.xhtml";
             request.logout();
         } else if (userPath.equals("/restricted")) {
             
             DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
             if (bean == null) {
                 bean = new DatabaseJSFManagedBean();
             }
             if (bean.isActiveUser(request.getRemoteAddr())) {
                 url = "/faces/view/restricted.html";
             } else {
                 url = "/faces/view/denied.html";
             }
 
 
         }
 
 
         try {
             System.out.println(url);
             request.getRequestDispatcher(url).forward(request, response);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         String userPath = request.getServletPath();
 
         if (userPath.equals("/requestRegistration")) {
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();
             JSONParser parser = new JSONParser();
 
             ContainerFactory containerFactory = new ContainerFactory() {
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
 
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
 
             Logger.getLogger(ControllerServlet.class.getName()).info(jsonText);
             String mac = "";
             String serial = "";
             String androidID = "";
             String make = "";
             String model = "";
             String username = "";
             String password = "";
             String name = "";
             String surname = "";
             String id = "";
 
 
             try {
                 Map json = (Map) parser.parse(jsonText, containerFactory);
                 Iterator iter = json.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry entry = (Map.Entry) iter.next();
                     String key = entry.getKey().toString();
                     String value = entry.getValue().toString();
                     Logger.getLogger(ControllerServlet.class.getName()).info(key);
                     Logger.getLogger(ControllerServlet.class.getName()).info(value);
                     if (key.equals("mac")) {
                         mac = value;
                     } else if (key.equals("serial")) {
                         serial = value;
                     } else if (key.equals("android")) {
                         androidID = value;
                     } else if (key.equals("make")) {
                         make = value;
                     } else if (key.equals("model")) {
                         model = value;
                     } else if (key.equals("username")) {
                         username = value;
                     } else if (key.equals("password")) {
                         password = value;
                     } else if (key.equals("name")) {
                         name = value;
                     } else if (key.equals("surname")) {
                         surname = value;
                     } else if (key.equals("id")) {
                         id = value;
                     }
 
                 }
 
                 Logger.getLogger(ControllerServlet.class.getName()).info(mac);
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             try {
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null) {
                     bean = new DatabaseJSFManagedBean();
                 }
                 bean.addToWaitingList(mac, androidID, serial, make, model, username, password, id, name, surname);
             } catch (NoSuchAlgorithmException e) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.INFO, null, e);
             }
 
 
         //ScanResults
         } else if (userPath.equals("/scanResults")) {
 
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();
             JSONParser parser = new JSONParser();
 
             ContainerFactory containerFactory = new ContainerFactory() {
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
 
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
 
             Logger.getLogger(ControllerServlet.class.getName()).info(jsonText);
             boolean root = false;
             boolean debug = false;
             boolean unknown = false;
             String apps = "";
             String mac = "";
             String serial = "";
             String androidID = "";
             String password = "";
             int api = 0;
 
             try {
                 Map json = (Map) parser.parse(jsonText, containerFactory);
                 Iterator iter = json.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry entry = (Map.Entry) iter.next();
                     String key = entry.getKey().toString();
                     String value = entry.getValue().toString();
                     Logger.getLogger(ControllerServlet.class.getName()).info(key);
                     Logger.getLogger(ControllerServlet.class.getName()).info(value);
                     if (key.equals("rooted")) {
                         root = Boolean.parseBoolean(value);
                     } else if (key.equals("debug")) {
                         debug = Boolean.parseBoolean(value);
                     } else if (key.equals("unknown")) {
                         unknown = Boolean.parseBoolean(value);
                     } else if (key.equals("apps")) {
                         apps = value.substring(1, value.length() - 1);
                     } else if (key.equals("mac")) {
                         mac = value;
                     } else if (key.equals("serial")) {
                         serial = value;
                     } else if (key.equals("android")) {
                         androidID = value;
                     } else if (key.equals("os")) {
                         api = Integer.parseInt(value);
                     } else if (key.equals("password")) {
                         password = value;
                     }
 
                 }
 
 
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null) {
                     bean = new DatabaseJSFManagedBean();
                 }
 
                 boolean registered = bean.deviceRegistered(mac, serial, androidID);
 
                 if (registered) {
                     Logger.getLogger(ControllerServlet.class.getName()).info("registered");
                     TokenGenerator gen = new TokenGenerator();
                     String deviceToken = bean.getToken(mac, androidID, serial);
                     String calculatedToken = "";
                     try {
                         calculatedToken = gen.generateToken(mac, androidID, serial, password);
                         Logger.getLogger(DatabaseJSFManagedBean.class.getName()).info(calculatedToken + " " + deviceToken);
 
                     } catch (NoSuchAlgorithmException ex) {
                         Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                     boolean match = calculatedToken.equals(deviceToken);
                     Logger.getLogger(ControllerServlet.class.getName()).info("Password: " + password);
                     Logger.getLogger(DatabaseJSFManagedBean.class.getName()).info(calculatedToken + " " + deviceToken);
                     if (match) {
                         Logger.getLogger(DatabaseJSFManagedBean.class.getName()).info("match");
                         boolean allowed = bean.addScanResults(mac, serial, androidID, root, debug, unknown, apps, api);
 
                         if (allowed) {
                             Logger.getLogger(DatabaseJSFManagedBean.class.getName()).info("allowed");
                             response.getOutputStream().print("allowed");
                         } else {
                             response.getOutputStream().print("denied;");
                             ScanSummary summary = new ScanSummary();
                             Scanresult scan = bean.getLatestScan(mac, serial, androidID);
                             response.getOutputStream().print(summary.getSummary(scan.getRootedScore(), scan.getDebuggingEnabledScore(), scan.getUnknownSourcesScore(), scan.getApiscore(),Integer.parseInt(scan.getApilevel()), scan.getBlacklistedApps(), scan.getAppsScore(), scan.getTotalScore()));
                         }
                         response.getOutputStream().flush();
                     }
 
 
                 }
 
 
 
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             //Device status
         } else if (userPath.equals("/status")) {
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();
             JSONParser parser = new JSONParser();
 
             ContainerFactory containerFactory = new ContainerFactory() {
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
 
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
 
             Logger.getLogger(ControllerServlet.class.getName()).info(jsonText);
             String mac = "";
             String serial = "";
             String androidID = "";
 
             try {
                 Map json = (Map) parser.parse(jsonText, containerFactory);
                 Iterator iter = json.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry entry = (Map.Entry) iter.next();
                     String key = entry.getKey().toString();
                     String value = entry.getValue().toString();                    
                     if (key.equals("mac")) {
                         mac = value;
                     } else if (key.equals("serial")) {
                         serial = value;
                     } else if (key.equals("android")) {
                         androidID = value;
                     }
 
                 }
 
 
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null) {
                     bean = new DatabaseJSFManagedBean();
                 }
                 boolean registered = bean.deviceRegistered(mac, serial, androidID);
                 Logger.getLogger(ControllerServlet.class.getName()).info(Boolean.toString(registered));
 
                 if (registered) {
                       Logger.getLogger("Active=" + Boolean.toString(bean.isActiveUser(request.getRemoteAddr())));
                     if (bean.isActiveUser(request.getRemoteAddr()))
                         response.getOutputStream().print("loggedIn");
                     else response.getOutputStream().print("registered");
                 } else {
                     boolean waiting = bean.deviceWaiting(mac, serial, androidID);
                     if (waiting) {
                         response.getOutputStream().print("waiting");
                     } else {
                         response.getOutputStream().print("not registered");
                     }
                 }
 
                 response.getOutputStream().flush();
                 response.getOutputStream().close();
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         else if (userPath.equals("/login")) {
             Logger.getLogger(ControllerServlet.class.getName()).info("login");
            
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();
             JSONParser parser = new JSONParser();
 
             ContainerFactory containerFactory = new ContainerFactory() {
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
 
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
 
             Logger.getLogger(ControllerServlet.class.getName()).info(jsonText);
             String mac = "";
             String serial = "";
             String androidID = "";
             String username = "";
             String password = "";
 
             try {
                 Map json = (Map) parser.parse(jsonText, containerFactory);
                 Iterator iter = json.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry entry = (Map.Entry) iter.next();
                     String key = entry.getKey().toString();
                     String value = entry.getValue().toString();
                     Logger.getLogger(ControllerServlet.class.getName()).info(key);
                     Logger.getLogger(ControllerServlet.class.getName()).info(value);
                     if (key.equals("mac")) {
                         mac = value;
                     } else if (key.equals("serial")) {
                         serial = value;
                     } else if (key.equals("android")) {
                         androidID = value;
                     }
                     else if (key.equals("password")) {
                         password = value;
                     }
                     else if (key.equals("username")) {
                         username = value;
                     }
 
                 }
 
 
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null) {
                     bean = new DatabaseJSFManagedBean();
                 }
                 boolean access = bean.login(username,password,mac, androidID, serial, request.getRemoteAddr());
                 if (access)
                     response.getOutputStream().print("allowed");
                 else
                     response.getOutputStream().print("denied");
                 response.getOutputStream().flush();
                 response.getOutputStream().close();
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         else if (userPath.equals("/mobileLogout")) {
             Logger.getLogger(ControllerServlet.class.getName()).info("logout");           
             BufferedReader reader = request.getReader();
             String jsonText = reader.readLine();
             JSONParser parser = new JSONParser();
             ContainerFactory containerFactory = new ContainerFactory() {
                 public List creatArrayContainer() {
                     return new LinkedList();
                 }
 
                 public Map createObjectContainer() {
                     return new LinkedHashMap();
                 }
             };
 
             Logger.getLogger(ControllerServlet.class.getName()).info(jsonText);
             String mac = "";
             String serial = "";
             String androidID = "";
 
             try {
                 Map json = (Map) parser.parse(jsonText, containerFactory);
                 Iterator iter = json.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry entry = (Map.Entry) iter.next();
                     String key = entry.getKey().toString();
                     String value = entry.getValue().toString();
                     Logger.getLogger(ControllerServlet.class.getName()).info(key);
                     Logger.getLogger(ControllerServlet.class.getName()).info(value);
                     if (key.equals("mac")) {
                         mac = value;
                     } else if (key.equals("serial")) {
                         serial = value;
                     } else if (key.equals("android")) {
                         androidID = value;
                     }
 
 
                 }
 
 
                 DatabaseJSFManagedBean bean = (DatabaseJSFManagedBean) request.getSession().getAttribute("bean");
                 if (bean == null) {
                     bean = new DatabaseJSFManagedBean();
                 }
                 bean.logout(mac, androidID, serial, request.getRemoteAddr());
                 response.getOutputStream().print("registered");
                 response.getOutputStream().flush();
                 response.getOutputStream().close();
             } catch (org.json.simple.parser.ParseException ex) {
                 Logger.getLogger(ControllerServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
 
 
 //       
     }
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
 }
