 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.java.dev.sommer.foafssl.login;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.cert.X509Certificate;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.java.dev.sommer.foafssl.principals.FoafSslPrincipal;
 import net.java.dev.sommer.foafssl.verifier.DereferencingFoafSslVerifier;
 
 /**
  * A very simple Insecure Login Servlet for sites that want to play with
  * foaf+ssl but do not have https, or do not want to bother with the setup yet.
  * (It is of course recommended to later own the infrastructure)
  * 
  * This is based on Melvin Carvalho's initial php code
  * http://lists.foaf-project.org/pipermail/foaf-protocols/2009-March/000386.html
  * 
  * @author hjs
  */
 public class InsecureLogin extends HttpServlet {
 
     public static final transient Logger log = Logger.getLogger(InsecureLogin.class.getName());
 
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
      * methods.
      * 
      * @param request
      *            servlet request
      * @param response
      *            servlet response
      * @throws ServletException
      *             if a servlet-specific error occurs
      * @throws IOException
      *             if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         String return_to_str = req.getParameter("return_to");
         URL rt = null;
         if (return_to_str != null && return_to_str.length() > 0) {
             try {
                 rt = new URL(return_to_str);
             } catch (MalformedURLException ex) {
                 log.info("request had malformed return_to url:" + return_to_str);
             }
         }
         if (rt == null) {
             rt = getReferer(req);
         }
 
         X509Certificate[] certs = (X509Certificate[]) req
                 .getAttribute("javax.servlet.request.X509Certificate");
         if (certs == null) {
             // no login
             try {
                 rt = new URL(rt.getProtocol(), rt.getHost(), rt.getPort(), rt.getPath()
                         + "?failure=nocert");
             } catch (MalformedURLException ex) {
                 log.info("cannot build failure url for " + rt);
             }
         } else {
             try {
                 X509Certificate clientCert = certs[0];
                 DereferencingFoafSslVerifier verifier = new DereferencingFoafSslVerifier();
                 for (FoafSslPrincipal webid : verifier.verifyFoafSslCertificate(clientCert)) {
                     if (webid == null)
                         continue; // should not happen
                     try {
                         rt = new URL(rt.getProtocol(), rt.getHost(), rt.getPort(), rt.getPath()
                                 + "?webid=" + URLEncoder.encode(webid.getUri().toString(), "UTF-8"));
                         break;
                     } catch (MalformedURLException ex) {
                         log.info("cannot build failure url for " + rt + " responding with webid="
                                 + webid);
                     }
                 }
            } catch (Exception ex) {
                 log.log(Level.INFO, "exception trying to login client", ex);
             }
         }
         // if rt is still null here, then the user has to use the back button
         // else redirect
         res.sendRedirect(rt.toExternalForm());
     }
 
     // <editor-fold defaultstate="collapsed"
     // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP <code>GET</code> method.
      * 
      * @param request
      *            servlet request
      * @param response
      *            servlet response
      * @throws ServletException
      *             if a servlet-specific error occurs
      * @throws IOException
      *             if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      * 
      * @param request
      *            servlet request
      * @param response
      *            servlet response
      * @throws ServletException
      *             if a servlet-specific error occurs
      * @throws IOException
      *             if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      * 
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "A simple Insecure login service. \n"
                 + " Initial request should have a return_to=${ResponseUrl} attribute value.\n"
                 + " On successful authentication the  client browser will be redirected to ${ResponseUrl}?webid=${webid}.\n"
                 + " On failre the client will be redirected to ${ResponseUrl}?failure \n"
                 + " The service is insecure because there is a risk of man in the middle attacks in the response.";
     }// </editor-fold>
 
     private URL getReferer(HttpServletRequest req) {
         String referer_str = req.getHeader("Referer");
         try {
             URL result = new URL(referer_str); // todo: this could be a relative
                                                // URL, in which case it needs to
                                                // be absolutized.
             return result;
         } catch (MalformedURLException ex) {
             log.log(Level.INFO, "malformed referer url:" + referer_str, ex);
             return null;
         }
     }
 }
