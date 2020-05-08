 package org.uiautomation;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 import org.expressme.openid.Association;
 import org.expressme.openid.Authentication;
 import org.expressme.openid.Endpoint;
 import org.expressme.openid.OpenIdException;
 import org.expressme.openid.OpenIdManager;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 public class HelloWorld extends HttpServlet {
 
   static final long ONE_HOUR = 3600000L;
   static final long TWO_HOUR = ONE_HOUR * 2L;
   static final String ATTR_MAC = "openid_mac";
   static final String ATTR_ALIAS = "openid_alias";
 
   OpenIdManager manager;
 
   @Override
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
     manager = new OpenIdManager();
     manager.setRealm("http://rocky-reef-2111.herokuapp.com/"); // change to your domain
     manager.setReturnTo("http://rocky-reef-2111.herokuapp.com/"); // change to your servlet url
    //manager.setRealm("http://localhost:5000/"); // change to your domain
    //manager.setReturnTo("http://localhost:5000/"); // change to your servlet url

   }
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
     String op = request.getParameter("op");
     if (op == null) {
       // check nonce:
       checkNonce(request.getParameter("openid.response_nonce"));
       // get authentication:
       byte[] mac_key = (byte[]) request.getSession().getAttribute(ATTR_MAC);
       String alias = (String) request.getSession().getAttribute(ATTR_ALIAS);
       Authentication authentication = manager.getAuthentication(request, mac_key, alias);
       String identity = authentication.getIdentity();
       String email = authentication.getEmail();
       // TODO: create user if not exist in database:
       showAuthentication(response.getWriter(), identity, email);
     } else if ("Google".equals(op)) {
       // redirect to Google sign on page:
       Endpoint endpoint = manager.lookupEndpoint("Google");
       Association association = manager.lookupAssociation(endpoint);
       request.getSession().setAttribute(ATTR_MAC, association.getRawMacKey());
       request.getSession().setAttribute(ATTR_ALIAS, endpoint.getAlias());
       String url = manager.getAuthenticationUrl(endpoint, association);
       response.sendRedirect(url);
     } else {
       throw new ServletException("Bad parameter op=" + op);
     }
   }
 
   void showAuthentication(PrintWriter pw, String identity, String email) {
     pw.print("<html><body><h1>Identity</h1><p>");
     pw.print(identity);
     pw.print("</p><h1>User</h1><p>");
     pw.print(getUserName(email));
     pw.print("</p></body></html>");
     pw.flush();
   }
 
   void checkNonce(String nonce) {
     // check response_nonce to prevent replay-attack:
     if (nonce == null || nonce.length() < 20) {
       throw new OpenIdException("Verify failed.");
     }
     long nonceTime = getNonceTime(nonce);
     long diff = System.currentTimeMillis() - nonceTime;
     if (diff < 0) {
       diff = (-diff);
     }
     if (diff > ONE_HOUR) {
       throw new OpenIdException("Bad nonce time.");
     }
     if (isNonceExist(nonce)) {
       throw new OpenIdException("Verify nonce failed.");
     }
     storeNonce(nonce, nonceTime + TWO_HOUR);
   }
 
   boolean isNonceExist(String nonce) {
     // TODO: check if nonce is exist in database:
     return false;
   }
 
   void storeNonce(String nonce, long expires) {
     // TODO: store nonce in database:
   }
 
   long getNonceTime(String nonce) {
     try {
       return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
           .parse(nonce.substring(0, 19) + "+0000")
           .getTime();
     } catch (ParseException e) {
       throw new OpenIdException("Bad nonce time.");
     }
   }
 
 
   private String getUserName(String email) {
     try {
       Connection connection = Database.getConnection();
       PreparedStatement stmt = connection.prepareStatement("SELECT name FROM users WHERE email=?");
       stmt.setString(1, email);
       ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
         return rs.getString(1);
       }
       return null;
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     }
   }
 
   private void db() throws URISyntaxException, SQLException {
     Connection connection = Database.getConnection();
 
     Statement stmt = connection.createStatement();
     stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
     stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
     stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
     ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
     while (rs.next()) {
       //resp.getWriter().print("Read from DB: " + rs.getTimestamp("tick"));
     }
   }
 
 
   private void openId() {
     OpenIdManager manager = new OpenIdManager();
     manager.setReturnTo("http://94.15.106.219:5000/test");
     manager.setRealm("http://94.15.106.219:5000");
 
     Endpoint endpoint = manager.lookupEndpoint("Google");
     System.out.println(endpoint);
 
     Association association = manager.lookupAssociation(endpoint);
     System.out.println(association);
 
     String url = manager.getAuthenticationUrl(endpoint, association);
     System.out.println("Copy the authentication URL in browser:\n" + url);
 
     System.out
         .println("After successfully sign on in browser, enter the URL of address bar in browser:");
 
     //Authentication authentication = manager.getAuthentication(request, association.getRawMacKey());
     //System.out.println(authentication);
     //System.out.println("Identity: " + authentication.getIdentity());
   }
 
   public static void main(String[] args) throws Exception {
     String port = System.getenv("PORT");
     if (port == null) {
       port = "5000";
     }
     Server server = new Server(Integer.valueOf(port));
     ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
     context.setContextPath("/");
     server.setHandler(context);
     context.addServlet(new ServletHolder(new HelloWorld()), "/*");
     server.start();
     server.join();
   }
 }
