 import db.Db;
 import db.DbConfig;
 import db.DbConnector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class HostsListServlet extends HttpServlet {
     private static final String ATTR_DB_CONNECTOR = "db.connector";
 
     private static final String RESP_OK         = "On";
     private static final String RESP_FAILED     = "Off";
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         // Database
 
         HttpSession session = req.getSession();
 
         DbConnector db = (DbConnector)session.getAttribute(ATTR_DB_CONNECTOR);
         if (db == null) {
             DbConfig config = Db.getConfig();
             db = new DbConnector(config);
 
             session.setAttribute(ATTR_DB_CONNECTOR, db);
         }
 
         // Response
 
         resp.setContentType("text/json");
         resp.setStatus(HttpServletResponse.SC_OK);
 
         PrintWriter out = resp.getWriter();
 
         // Database "response"
 
         ResultSet res = db.query(String.format("SELECT * FROM %s", "hosts"));
 
         StringBuilder resultJson = new StringBuilder(); // Yep, I know about Json tools =)
         try {
             resultJson.append("[");
 
            if (res != null) {
                while (res.next()) {
                    resultJson.append("{");
                    resultJson.append("\"status\": " + "\""
                            + (res.getBoolean("state") ? RESP_OK : RESP_FAILED)
                            + "\",");
                    resultJson.append("\"ip\": " + "\"" + res.getString("url") + "\"");
                    resultJson.append("}");

                    if (! res.isLast()) resultJson.append(",");
                }
             }
 
             resultJson.append("]");
         } catch (SQLException e) {
             e.printStackTrace(System.err);
         }
 
         System.out.println(resultJson);
 
         out.println(resultJson);
 
         // Close all resources
 
         out.close();
     }
 }
