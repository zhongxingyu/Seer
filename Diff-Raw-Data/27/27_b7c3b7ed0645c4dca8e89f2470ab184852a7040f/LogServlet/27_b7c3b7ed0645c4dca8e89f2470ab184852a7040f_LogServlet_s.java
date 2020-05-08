 package org.makumba.parade.access;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.makumba.HtmlUtils;
 import org.makumba.parade.init.ParadeProperties;
 
 public class LogServlet extends HttpServlet {
 
    static File log = new File((String) ParadeProperties.getProperty("parade.paradeBase")
             + ((String) ParadeProperties.getProperty("tomcat.output")).replace('/', File.separatorChar));
 
     /**
      * this file shows the parts of server-output.txt that are relevant to a certain context. can be improved a lot
      * (e.g. never finish the http transaction, keep sending lines appended to server-output.txt)"
      */
     public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         String context = req.getParameterValues("context")[0];
         if (context.equals(""))
             context = "parade";
         resp.setContentType("text/html");
         PrintWriter wr = resp.getWriter();
         wr.println("<html><head><title>Log of context \"" + context + "\"</title>");
         wr.println("<style type=\"text/css\">");
         wr.println("PRE{font-size:.8em}");
         wr.println(".init{color:#888888; background:#f6f6f6}");
         wr.println(".FINEST{color:#666666; font-size:x-small}");
         wr.println(".FINER{color:#666666; font-size:small}");
         wr.println(".FINE{color:#666666}");
         wr.println(".CONFIG{color:orange}");
         wr.println(".INFO{color:green}");
         wr.println(".WARNING{color:blue; background:pink}");
         wr.println(".SEVERE{color:yellow; background:red}");
         wr.println("</style></head><body>");
         wr.println("<h2>Log of context \"" + context + "\":</h2>");
         wr.println("<PRE>");
         BufferedReader r = new BufferedReader(new FileReader(log));
         String line = null;
         while ((line = r.readLine()) != null) {
             line = HtmlUtils.string2html(line);
             int n = line.indexOf('@' + context + ':');
             if (n == -1) {
                 if (line.startsWith("logging:") || line.startsWith("init:"))
                     wr.println("<span class=\"init\">" + line + "</span>");
                 continue;
             }
             Level level = Level.ALL;
             try {
                 String s = line.substring(line.indexOf(": ") + 2);
                 s = s.substring(0, s.indexOf(": "));
                 level = Level.parse(s);
             } catch (Exception e) {
             }
 
             wr.print("");
             wr.print(line.substring(0, line.indexOf("@")));
             wr.print(": <span class=\"" + level + "\">" + line.substring(n + context.length() + 3));
             wr.println("</span>");
         }
         wr.println("</PRE>");
         wr.println("</body></html>");
         r.close();
     }
 }
