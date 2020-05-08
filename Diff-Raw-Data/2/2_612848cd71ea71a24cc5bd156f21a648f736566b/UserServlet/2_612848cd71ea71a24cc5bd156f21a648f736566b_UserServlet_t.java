 package org.makumba.parade.view;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Parade;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class UserServlet  extends HttpServlet {
     
     private static Logger logger = Logger.getLogger(UserServlet.class);
 
     public void init() {
     }
 
     public void service(ServletRequest req, ServletResponse resp) throws java.io.IOException, ServletException {
         resp.setContentType("text/html");
         resp.setCharacterEncoding("UTF-8");
         
         String opResult = (String) req.getAttribute("result");
         
         PrintWriter out = resp.getWriter();
         
         Session s = null;
         Transaction tx = null;
         try {
             s = InitServlet.getSessionFactory().openSession();
             tx = s.beginTransaction();
 
             Parade p = (Parade) s.get(Parade.class, new Long(1));
 
             
             out.print(getNewUserView(p, (String) ((HttpServletRequest)req).getSession(true).getAttribute("org.makumba.parade.user"), opResult));
 
 
         } finally {
             tx.commit();
             s.close();
         }
         
     }
     
 
     private String getNewUserView(Parade p, String username, String opResult) {
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("newUser.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         // Creating the data model
         SimpleHash root = new SimpleHash();
         
         root.put("login", username);
         
         String name = "", surname = "";
         int n = username.indexOf(".");
         if(n > -1) {
             name = username.substring(0, n);
            root.put("niceUserName", name.substring(0, 1).toUpperCase() + name.substring(1));
         } else {
             root.put("niceUserName", username);
         }
         
         root.put("opResult", opResult == null ? "" : opResult);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         out.flush();
                 
         return result.toString();
 
     }
 
 
 }
