 package org.tak.servelets;
 
 
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONWriter;
 import org.tak.Server;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * User: Tommy
  * 6/2/13
  */
 public class LoadId extends HttpServlet {
 
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         if (req.getParameter("ids")!=null) {
             String[] lookups = req.getParameter("ids").split(",");
             JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
             try {
                 jsonWriter.array();
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             for (String lookup : lookups) {
                int i = Integer.parseInt(lookup);
                if (i < Server.getIdentifiables().size()) {
                    Server.getIdentifiables().get(i).writeJSON(jsonWriter);
                }
             }
             try {
                 jsonWriter.endArray();
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
     }
 }
