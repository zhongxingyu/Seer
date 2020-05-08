 package lq;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ViewQuote extends HttpServlet {
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         String idParam = req.getParameter("id");
         Long id = Long.parseLong(idParam);
         Quote quote = QuoteUtil.getQuoteWithId(id);
 
         resp.setContentType("text/html");
         resp.getWriter().println(header);
 
         if (quote != null) {
             Printer printer = new Printer(resp.getWriter());
             printer.printQuote(quote);
         }
         else {
             resp.getWriter().println("Quote not found."); 
         }
     }
 
     private final String header = 
         "<html> " +
         "<head> " +
         "<meta name=\"robots\" content=\"noindex, nofollow\" /> " +
         "<title>Quotes fra #linux.no på freenode</title> " +
         "<style type=\"text/css\"> " +
         "body {font-family: monospace;}" +
         "hr {" +
         "    border-style: solid;" +
         "    border-color: black;" +
         "    border-width: 1px; " +
         "}" +
         "</style> " +
         "</head> " +
         "<body bgcolor=\"#FFFFFF\" text=\"#000000\" link=\"#000000\" vlink=\"#000000\"> ";
 
 }
