 package net.codefactor.spacefighters.servlet;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.codefactor.spacefighters.utils.MD5;
 import net.codefactor.spacefighters.utils.RSA;
 
 @SuppressWarnings("serial")
 public class PointServlet extends HttpServlet {
   /**
    * A logger object.
    */
   private static final Logger LOG = Logger.getLogger(PointServlet.class
       .getName());
   private static final int MAX_ACTIONS = 10;
   public static RSA rsa;
 
   /**
    * @override
    */
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     initRSA();
 
     try {
       String userName = req.getParameter("userName");
       long points = Long.parseLong(req.getParameter("points"));
       String actions = req.getParameter("actions");
       String token = req.getParameter("token");
       String callback = req.getParameter("callback");
       if (points != 0 && !getToken(userName, points).equals(token)) {
         resp.sendError(500);
       } else {
         String[] actionList = actions.split(",");
         for (int i = 0; i < actionList.length && i < MAX_ACTIONS; i++) {
           String action = actionList[i];
           if (POINTS.containsKey(action)) {
             points += POINTS.get(action);
           } else if (action.indexOf("purchase:") == 0) {
             long count = Math.abs(Long.parseLong(action.substring("purchase:"
                 .length())));
             points -= count;
           }
           if (points < 0) {
             points = 0;
           }
         }
         token = getToken(userName, points);
         PrintWriter out = resp.getWriter();
         String json = "{\"points\":" + points + ",\"token\":\"" + token + "\"}";
         if (callback != null) {
           resp.setContentType("text/javascript");
           out.print(callback);
           out.print("(" + json + ")");
         } else {
           out.print(json);
         }
         out.flush();
       }
     } catch (Exception e) {
       e.printStackTrace();
       resp.sendError(500);
     }
   }
 
   private String getToken(String userName, long points)
       throws NoSuchAlgorithmException, UnsupportedEncodingException {
     String input = userName + ":" + points;
     String md5 = MD5.md5(input);
     BigInteger m = new BigInteger(md5, 16);
    BigInteger result = rsa.encrypt(m);
     return result.toString(16);
   }
 
   private void initRSA() throws IOException {
     synchronized (PointServlet.class) {
       if (rsa == null) {
         ServletContext context = getServletContext();
         String fullPath = context
             .getRealPath("/WEB-INF/private-key.properties");
         Properties keyProps = new Properties();
         keyProps.load(new FileInputStream(fullPath));
        LOG.log(Level.WARNING, "Loading RSA: " + keyProps);
         rsa = new RSA(keyProps);
       }
     }
   }
 
   public static Map<String, Long> POINTS = new HashMap<String, Long>();
   static {
     POINTS.put("pickup:points", 2000L);
     POINTS.put("pickup:extramine", 500L);
     POINTS.put("pickup:weapon", 500L);
     POINTS.put("pickup:death", -500L);
     POINTS.put("pickup:life", 500L);
     POINTS.put("pickup:moveable", 500L);
     POINTS.put("pickup:bullets", 100L);
    POINTS.put("pickup:points", 100L);
     POINTS.put("destroy:ship", 500L);
     POINTS.put("destroy:bullet", 50L);
     POINTS.put("destroy:mine", 100L);
     POINTS.put("destroy:grave", 500L);
     POINTS.put("destroy:anything", 10L);
   }
 }
