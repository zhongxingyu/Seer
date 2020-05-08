 package edu.gatech.oad.rocket.findmythings.server.service;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import edu.gatech.oad.rocket.findmythings.server.TemplateServlet;
 import edu.gatech.oad.rocket.findmythings.server.util.Config;
 import edu.gatech.oad.rocket.findmythings.server.util.Envelope;
 
 @Singleton
 public class MailmanServlet extends TemplateServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7698678204988659041L;
    static final Logger LOG = Logger.getLogger(MailmanServlet.class.getName());
 
    private final Envelope emailWrapper;
 
     @Inject
     MailmanServlet(Envelope emailWrapper) {
         super();
         this.emailWrapper = emailWrapper;
     }
     
     public Envelope getEmailWrapper() {
     	return emailWrapper;
     }
 
     private String urlFor(HttpServletRequest request, String code, String userName, boolean forgot) {
         try {
             URI url = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), "/activate",
             		Config.TICKET_PARAM+"="+code+"&"+getUsernameParam()+"="+userName+"&"+Config.FORGOTPASSWORD_PARAM+"="+Boolean.toString(forgot), null);
             return url.toString();
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String username = request.getParameter(getUsernameParam());
         try {
             String registrationString = request.getParameter(Config.TICKET_PARAM);
             boolean forgot = getBoolRequestParam(request, Config.FORGOTPASSWORD_PARAM, false);
             String url = urlFor(request, registrationString, username, forgot);
             LOG.info("Link URL is " + url);
             
             String subject = (forgot ? "Password Information" : "Complete Registration") + " for Find My Things";
             String htmlMessage = createDocument("inc/email.ftl",
                         "email", username,
                         "href", url,
                         Config.FORGOTPASSWORD_PARAM, Boolean.toString(forgot));
             getEmailWrapper().send(username, subject, htmlMessage);
             
             LOG.info("Registration email sent to " + username + " with return url " + url);
         } catch (Exception e) {
             LOG.severe("Error sending mail to " + username + ": " + e.getMessage());
         }
     }
 
 }
