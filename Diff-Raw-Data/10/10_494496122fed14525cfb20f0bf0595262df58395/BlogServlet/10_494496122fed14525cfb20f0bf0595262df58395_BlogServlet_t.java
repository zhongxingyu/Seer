 package net._87k.blog;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.utils.SystemProperty;
 
 @SuppressWarnings("serial")
 public class BlogServlet extends HttpServlet
 {
 
 private void
 doPostEntry(HttpServletRequest req,
             HttpServletResponse resp,
             User user)
     throws IOException
 {
     Entity blog = new Entity("Blog", "diary");
     Entity entry;
 
     String entryKey = req.getParameter("entry");
     DatastoreService store = DatastoreServiceFactory.getDatastoreService();
     if (entryKey != null) {
         try {
             long id = Integer.parseInt(req.getParameter("entry"));
             entry = new Entity("Entry", id, blog.getKey());
             if ("Delete".equals(req.getParameter("submit"))) {
                 store.delete(entry.getKey());
                 resp.setContentType("text/plain");
                 resp.getWriter().println("Entry deleted.");
                 return;                
             }
         } catch (NumberFormatException e) {
             resp.setContentType("text/plain");
             resp.getWriter().println("Invalid entry.");
             return;
         }
     } else {
         entry = new Entity("Entry", blog.getKey()); 
     }
 
     Date date;
     try {
         DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
         date = df.parse(req.getParameter("date"));
     } catch (ParseException e) {
         date = new Date();
     }
     
     boolean published = "Publish!".equals(req.getParameter("submit"));
     entry.setProperty("title", req.getParameter("title"));
     entry.setProperty("published", published);
     entry.setProperty("content", new Text(req.getParameter("content")));
     entry.setProperty("user", user);
     entry.setProperty("date", date);
     entry.setProperty("mtime", new Date());
 
     store.put(entry);
     
     resp.sendRedirect((published ? "/" : "/post/") + new Path(entry));    
 }
 
 private void
 doImport(HttpServletRequest req,
          HttpServletResponse resp,
          User user)
     throws IOException
 {
     Date date;
     try {
         DateFormat df = new SimpleDateFormat("MMMM yyyy");
         date = df.parse(req.getParameter("date"));
     } catch (ParseException e) {
         resp.setContentType("text/plain");
         resp.getWriter().println("Invalid date.");
         return;
     }
     String path = new SimpleDateFormat("yyyy/M").format(date);
     String diaryPrefix = "http://wednesdaynight.org/diary";
     String url = diaryPrefix + "/" + path;
     Document doc = Jsoup.connect(url).get();
     resp.setContentType("text/plain");
     DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
 
     Entity blog = new Entity("Blog", "diary");
     ArrayList<Entity> entries = new ArrayList<Entity>();
 
     for (Element elem : doc.select("div.entry")) {
         Element p = elem.getElementsByTag("p").first();
         Element a = p.getElementsByTag("a").first();
         try {
             date = df.parse(a.text());
         } catch (ParseException e) {
             resp.setContentType("text/plain");
             resp.getWriter().println("Bad date: " + a.text() + ": " + e);
             return;
         }
         a.remove();
         String title = p.ownText();
         p.remove();
         for (Element href : elem.getElementsByTag("a")) {
             String attr = href.attr("href");
             if (attr != null && attr.startsWith(diaryPrefix)) {
                 href.attr("href", attr.substring(diaryPrefix.length()));
             }
         }
         Entity entry = new Entity("Entry", blog.getKey());
         entry.setProperty("date", date);
         entry.setProperty("title", title);
         entry.setProperty("published", Boolean.FALSE);
         entry.setProperty("importing", Boolean.TRUE);
         entry.setProperty("content", new Text(elem.html()));
         entry.setProperty("user", user);
         entries.add(entry);
     }
 
     DatastoreServiceFactory.getDatastoreService().put(entries);
     resp.sendRedirect("/" + path);
 }
 
 
 public void
 doPost(HttpServletRequest req,
        HttpServletResponse resp)
     throws IOException
 {
     if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
         if (!req.isSecure()) {
             resp.setContentType("text/plain");
             resp.getWriter().println("Forbidden.");
             return;
         }
     }
 
     if (!Xrsf.hasMatchingCookie(req)) {
         resp.setContentType("text/plain");
         resp.getWriter().println("Forbidden.");
         return;
     }
     
     UserService userService = UserServiceFactory.getUserService();
     User user = userService.getCurrentUser(); 
     if (!Private.canUserPost(user)) {
         resp.setContentType("text/plain");
         resp.getWriter().println("Forbidden.");
         return;
     }
     
     if ("/post".equals(req.getPathInfo())) {
         doPostEntry(req, resp, user);
     } else if ("/import".equals(req.getPathInfo())) {
         doImport(req, resp, user);
     } else {
         resp.setContentType("text/plain");
         resp.getWriter().println("Not Implemented.");
         return;
     }
 }
 
 }
