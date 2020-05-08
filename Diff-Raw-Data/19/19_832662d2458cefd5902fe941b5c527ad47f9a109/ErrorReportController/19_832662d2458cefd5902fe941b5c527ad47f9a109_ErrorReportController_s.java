 package ru.terra.jbrss.controller;
 
 import com.sun.jersey.api.core.HttpContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.terra.jbrss.constants.URLConstants;
 import ru.terra.server.config.Config;
 import ru.terra.server.controller.AbstractResource;
 import ru.terra.server.dto.SimpleDataDTO;
 
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import java.util.Properties;
 
 /**
  * User: Vadim Korostelev
  * Date: 20.09.13
  * Time: 14:18
  */
 @Path(URLConstants.DoJson.ErrorReports.REPORT)
 public class ErrorReportController extends AbstractResource {
     private Logger logger = LoggerFactory.getLogger(this.getClass());
     private String mailServer = Config.getConfig().getValue("errorreport.mail.server", null);
     private String mailPort = Config.getConfig().getValue("errorreport.mail.server.port", "465");
     private String mailUser = Config.getConfig().getValue("errorreport.mail.user", null);
     private String mailPass = Config.getConfig().getValue("errorreport.mail.pass", null);
     private String mailTo = Config.getConfig().getValue("errorreport.mail.to", "jbrss@terranz.ath.cx");
     private String mailFrom = Config.getConfig().getValue("errorreport.mail.from", "jbrss@terranz.ath.cx");
     private StringBuilder sb = new StringBuilder();
 
     @POST
     @Path(URLConstants.DoJson.ErrorReports.DO_REPORT)
     public SimpleDataDTO<Boolean> get(@Context HttpContext hc) {
         logger.info("Error reported!");
         if (mailServer != null && mailUser != null && mailPass != null) {
             for (String param : hc.getRequest().getFormParameters().keySet()) {
                 sb.append(param);
                 sb.append(" : ");
                 sb.append(hc.getRequest().getFormParameters().getFirst(param));
                 sb.append("\n");
             }
             logger.info("Received error log: " + sb.toString());
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                    logger.info("Starting error reporter stream");
                     sendError(sb.toString());
                 }
             }).start();
         }
         return new SimpleDataDTO<>(true);
     }
 
     public void sendError(String error) {
         logger.info("sending error");
         try {
             Properties props = new Properties();
 
             props.put("mail.transport.protocol", "smtps");
             props.put("mail.smtps.auth", "true");
 
             Session mailSession = Session.getDefaultInstance(props);
             mailSession.setDebug(true);
             Transport transport = mailSession.getTransport();
 
             MimeMessage message = new MimeMessage(mailSession);
             message.setSubject("Error report from jbrss android client");
             message.setContent(error, "text/plain");
 
             message.addRecipient(Message.RecipientType.TO,
                     new InternetAddress(mailTo));
 
             transport.connect(mailServer, Integer.parseInt(mailPort), mailUser, mailPass);
 
             transport.sendMessage(message,
                     message.getRecipients(Message.RecipientType.TO));
             transport.close();
         } catch (Exception e) {
             logger.error("error while sending email", e);
         }
     }
 }
