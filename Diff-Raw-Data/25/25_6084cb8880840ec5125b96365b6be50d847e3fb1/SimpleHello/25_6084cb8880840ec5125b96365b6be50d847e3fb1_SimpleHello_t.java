 import freemarker.template.*;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import javax.xml.transform.*;;
 import java.lang.*;
 import java.io.*;
 import java.util.*;
 
 
 
 public class SimpleHello extends HttpServlet
 {
     public void doGet (HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException
     {
         PrintWriter out = resp.getWriter();

         MFile file = new MFile();
         String content = null;
        /*
        content = file.GetContent ("/opt/apache-tomcat-7.0.27/webapps/a/template/header.html");
         out.println(content);
         out.close();*/
         Configuration cfg = new Configuration();

         cfg.setDirectoryForTemplateLoading (
         		new File("/opt/apache-tomcat-7.0.27/webapps/a/template/"));
         cfg.setObjectWrapper (new DefaultObjectWrapper());
        cfg.setDefaultEncoding("ISO-8859-1");

         Template temp = cfg.getTemplate("header.html");
        //temp.setEncoding("UTF-8");
        //out.println (temp.toString());

         Map root = new HashMap();
         root.put("title","aaaaaaaaaaaaaaaaaaaaaaaaaa");
         try {
             temp.process(root, out);		
            //out.flush();
         } catch (Exception e) {
 			// TODO: handle exception
 		}
 
 
     }
 }
