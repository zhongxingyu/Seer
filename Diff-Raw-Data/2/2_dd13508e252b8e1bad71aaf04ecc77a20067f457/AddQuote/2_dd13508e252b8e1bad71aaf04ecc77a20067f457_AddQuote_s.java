 package lq;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class AddQuote extends HttpServlet {
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
       String nick = req.getParameter("nick");
       String text = req.getParameter("quote");
       String date = req.getParameter("date");
       String forward = req.getHeader("X-Forwarded-For");
       String ip = req.getRemoteAddr();
 
       resp.setContentType("text/html");
 
       boolean validInput = true;
       Date quoteDate = new Date();
 
       if (Strings.nullOrEmpty(nick)) {
           nick = "Anonym";
       }
       if (Strings.nullOrEmpty(text)) {
           validInput = false;
       }
       if (!Strings.nullOrEmpty(date)) {
           try {
               quoteDate = DateUtil.dateFormat.parse(date);
           }
           catch (ParseException exception) {
               resp.getWriter().println("Dato må være på formatet YYYY-MM-DD");
               validInput = false;
           }
       }
       if (!Strings.nullOrEmpty(forward)) {
           ip = ip + " (" + forward + ")";
       }
 
       if (!validInput) {
         resp.getWriter().println("Eskje du lidt kniben nå");
         return;
       }
 
       Quote newQuote = new Quote(quoteDate, nick, text, ip);
       PersistenceManager pm = PMF.get().getPersistenceManager();
       try {
           pm.makePersistent(newQuote);
       }
       finally {
           pm.close();
       }
 
       resp.getWriter().println(successString);
     }
 
   public final String successString = 
           "<center>\n" +
          "<img src=\"roflcopter.gif\" /><p />\n" +
           "<pre> Takk, quoten venter nå på godkjenning.</pre>\n" +
           "<pre> <a href=\"/quotes.jsp\">Tilbake til quotes</a> </pre>\n" +
           "</center>\n";
 }
