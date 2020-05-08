 package org.valabs.stdobj.webcon;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.MessageHandler;
 import org.valabs.odisp.common.StandartODObject;
 import org.valabs.stdmsg.ODCleanupMessage;
 import org.valabs.stdmsg.ODObjectLoadedMessage;
 import org.valabs.stdmsg.webcon.WCAddServletMessage;
 import org.valabs.stdmsg.webcon.WCListServletsMessage;
 import org.valabs.stdmsg.webcon.WCListServletsReplyMessage;
 import org.valabs.stdmsg.webcon.WCRemoveServletMessage;
 import org.valabs.stdobj.webcon.servlet.Servlet;
 import org.valabs.stdobj.webcon.servlet.ServletException;
 import org.valabs.stdobj.webcon.servlet.ServletOutputStream;
 import org.valabs.stdobj.webcon.servlet.http.HttpServlet;
 import org.valabs.stdobj.webcon.servlet.http.HttpServletRequest;
 import org.valabs.stdobj.webcon.servlet.http.HttpServletResponse;
 
 /** ODISP-  ACME .
  * @author (C) 2004 <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
 * @version $Id: WebCon.java,v 1.13 2004/08/23 07:42:38 valeks Exp $
  */
 
 public class WebCon extends StandartODObject implements MessageHandler {
   /**    . */
   private Serve acmeServe = null;
   /**   . */
   public void registerHandlers() {
     addHandler(ODObjectLoadedMessage.NAME, this);
     addHandler(ODCleanupMessage.NAME, this);
     addHandler(WCAddServletMessage.NAME, this);
     addHandler(WCRemoveServletMessage.NAME, this);
     addHandler(WCListServletsMessage.NAME, this);
   }
 
   /**   . */
   public final void messageReceived(final Message msg) {
     if (ODObjectLoadedMessage.equals(msg)) {
       if (acmeServe != null) {
         return;
       }
      acmeServe = new Serve();
      IndexServlet idx = new IndexServlet();
      acmeServe.addServlet("/", idx);
      acmeServe.addServlet("/index.html", idx);
      new Thread() {
         public void run() {
          setDaemon(true);
           acmeServe.serve();
         }
      }
      .run();
     } else if (WCAddServletMessage.equals(msg)) {
       if (WCAddServletMessage.getServletHandler(msg) instanceof Servlet) {
         //   
         if (acmeServe != null) {
           acmeServe.addServlet(
             WCAddServletMessage.getServletMask(msg),
             (Servlet) WCAddServletMessage.getServletHandler(msg));
         } else {
           logger.warning(
             "attempting to add servlet while container is not started");
         }
       } else {
         logger.warning("handler is not an Servlet extension");
       }
     } else if (WCRemoveServletMessage.equals(msg)) {
       if (WCRemoveServletMessage.getServletHandler(msg) instanceof Servlet) {
         //   
         if (acmeServe != null) {
           acmeServe.removeServlet(
             (Servlet) WCRemoveServletMessage.getServletHandler(msg));
         } else {
           logger.warning(
             "attempting to add servlet while container is not started");
         }
       } else {
         logger.warning("handler is not an Servlet extension");
       }
     } else if (WCListServletsMessage.equals(msg)) {
       if (acmeServe == null) {
         logger.warning(
           "servlet enumeration request before container was started");
         return;
       }
       Message m = dispatcher.getNewMessage();
       WCListServletsReplyMessage.setup(
         m,
         msg.getOrigin(),
         getObjectName(),
         msg.getId());
       List result = new ArrayList();
       Enumeration e = acmeServe.getServletNames();
       while (e.hasMoreElements()) {
         result.add(e.nextElement());
       }
       WCListServletsReplyMessage.setServletsList(m, result);
       dispatcher.send(m);
     } else if (ODCleanupMessage.equals(msg)) {
       cleanUp(ODCleanupMessage.getReason(msg).intValue());
     }
   }
 
   /**  .
    * @param id  
    */
   public WebCon(Integer id) {
     super("webcon" + id);
   }
 
   /**   . */
   public String[] getProviding() {
     String[] result = { "webcon", };
     return result;
   }
 
   /**   . */
   public String[] getDepends() {
     String[] result = { "dispatcher", };
     return result;
   }
 
   /**  .
    * @param exitCode  
    */
   public int cleanUp(int exitCode) {
     return exitCode;
   }
 
   /**  index . */
   private class IndexServlet extends HttpServlet {
     /**   . */
     public String getServletInfo() {
       return "IndexServlet: servlet that act as front page for WebCon";
     }
     /**  . */
     public void service(
       final HttpServletRequest req,
       final HttpServletResponse res)
       throws ServletException, IOException {
       res.setStatus(HttpServletResponse.SC_OK);
       res.setContentType("text/xhtml");
       ServletOutputStream p = res.getOutputStream();
       p.println(
         "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
       p.println("<html>");
       p.println("\t<head>");
       p.println("\t\t<title>ODISP WebCon default index page</title>");
       p.println("\t\t<meta http-equiv='Content-type' content='text/html'/>");
       p.println("\t</head>");
       p.println("\t<body>");
       p.println("\t\t<h3>ODISP WebCon default index page</h3>");
       //   
       Enumeration e = acmeServe.getServlets();
       if (e.hasMoreElements()) {
         p.println("\t\t<hr/>");
         p.println("\t\t<p>List of known servlets");
         p.println("\t\t<ul>");
         while (e.hasMoreElements()) {
           p.println(
             "\t\t\t<li>"
               + ((Servlet) e.nextElement()).getServletInfo()
               + "</li>");
         }
         p.println("\t\t</ul>");
         p.println("\t\t</p>");
       }
       p.println("\t</body>");
       p.println("</html>");
       p.flush();
       p.close();
     }
   }
 } // WebCon
