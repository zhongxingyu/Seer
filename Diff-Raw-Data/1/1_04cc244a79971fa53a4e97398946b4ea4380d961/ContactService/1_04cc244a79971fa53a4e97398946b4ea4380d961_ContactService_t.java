 package gov.usgs.service;
 
 import gov.usgs.cida.gdp.communication.EmailMessage;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ContactService extends HttpServlet {
 
     private static final long serialVersionUID = -8308644813099149346L;
     private final static Logger log = LoggerFactory.getLogger(ContactService.class);
     private static final String USGS_REMEDY = "servicedesk@usgs.gov";
     private static final String DEFAULT_SUBJECT = "Geo Data Portal User Comments";
     private static final String DEFAULT_GDP_ADDRESS = "gdp_help_noreply@usgs.gov";
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
 
         String comments = req.getParameter("comments");
         String email = req.getParameter("email");
         String emailResponseRequired = req.getParameter("replyrequired");
 
         String emailResponseRequiredText = ("true".equals(emailResponseRequired)) ? "*** User (" + email + ") Requires a Response ***\n\n" : "";
         String autoInsertedContent = "This is an auto-generated email from the USGS Geo Data Portal.  "
                 + "Below is a copy of the message you (" + email + ") submitted.  If you feel you have received this message erroneously, "
                 + "please contact servicedesk@usgs.gov.\n\n";
 
         log.info("Serving: " + req.getQueryString());
         log.info("ContactService Request Email: " + email + " ResponseRequired:" + emailResponseRequired);
 
         email = (StringUtils.isBlank(email)) ? DEFAULT_GDP_ADDRESS : email;
 
         EmailMessage msg = new EmailMessage();
         {
             msg.setTo(USGS_REMEDY);
             msg.setSubject(DEFAULT_SUBJECT);
             msg.setContent(autoInsertedContent + emailResponseRequiredText + comments);
 
             // set the from and reply to address if there is one.
             try {
                 msg.setFrom(email);
                 msg.setReplyTo(new InternetAddress[]{new InternetAddress(email)});
             } catch (AddressException ex) {
                 try {
                     log.error(email + " could not be parsed as a valid reply-to email address. Setting reply-to to " + DEFAULT_GDP_ADDRESS, ex);
                     msg.setReplyTo(new InternetAddress[]{new InternetAddress(DEFAULT_GDP_ADDRESS)});
                 } catch (AddressException ex1) {
                     log.error("Could not properly set e-mail reply-to field.", ex1);
                 }
             }
 
         }
         PrintWriter writer = resp.getWriter();
         try {
             msg.send();
             log.info("Email sent to GDP");
 
             try {
                 writer.append("{ \"status\" : \"success\" }");
                 writer.flush();
             } finally {
                 if (writer != null) {
                     writer.flush();
                 }
             }
         } catch (Exception ex) {
             log.error("Could not send email message.", ex);
             try {
                 writer.append("{ \"status\" : \"fail\" }");
                 writer.flush();
             } finally {
                 if (writer != null) {
                     writer.flush();
                 }
             }
         }
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         doGet(req, resp);
     }
 }
