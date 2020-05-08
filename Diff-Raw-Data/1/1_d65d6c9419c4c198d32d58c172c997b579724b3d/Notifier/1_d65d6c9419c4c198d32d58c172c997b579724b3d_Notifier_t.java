 package fr.univnantes.atal.web.piubella.servlets;
 
 import com.google.appengine.api.xmpp.JID;
 import com.google.appengine.api.xmpp.MessageBuilder;
 import com.google.appengine.api.xmpp.SendResponse;
 import com.google.appengine.api.xmpp.XMPPService;
 import com.google.appengine.api.xmpp.XMPPServiceFactory;
 import fr.univnantes.atal.web.piubella.app.Constants;
 import fr.univnantes.atal.web.piubella.model.Address;
 import fr.univnantes.atal.web.piubella.model.CollectDay;
 import fr.univnantes.atal.web.piubella.model.Notification;
 import fr.univnantes.atal.web.piubella.model.NotificationTransport;
 import fr.univnantes.atal.web.piubella.model.User;
 import fr.univnantes.atal.web.piubella.persistence.PMF;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Cron to handle notifications.
  */
 public class Notifier extends HttpServlet {
 
     /**
      * Processes requests for the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try (PrintWriter out = response.getWriter()) {
             final Calendar c = Calendar.getInstance();
             List<CollectDay> toTest = new ArrayList<>();
             int day = c.get(Calendar.DAY_OF_WEEK);
             switch (day) {
                 case Calendar.MONDAY:
                     toTest.add(CollectDay.TUESDAY);
                     break;
                 case Calendar.TUESDAY:
                     toTest.add(CollectDay.WEDNESDAY);
                     toTest.add(CollectDay.WEDNESDAY_EVEN);
                     toTest.add(CollectDay.WEDNESDAY_ODD);
                     break;
                 case Calendar.WEDNESDAY:
                     toTest.add(CollectDay.THURSDAY);
                     break;
                 case Calendar.THURSDAY:
                     toTest.add(CollectDay.FRIDAY);
                     break;
                 case Calendar.FRIDAY:
                     toTest.add(CollectDay.FRIDAY);
                     break;
                 case Calendar.SATURDAY:
                     toTest.add(CollectDay.SUNDAY);
                     break;
                 case Calendar.SUNDAY:
                     toTest.add(CollectDay.MONDAY);
                     break;
             }
             out.println(toTest);
             PersistenceManager pm = PMF.get().getPersistenceManager();
             Query q = pm.newQuery(Notification.class);
             try {
                 List<Notification> results =
                         (List<Notification>) q.execute();
                 for (Notification notification : results) {
                     boolean blue = false, yellow = false;
                     Address address = notification.getAddress();
                     innerLoop:
                     for (CollectDay cd : toTest) {
                         if (address.getBlueDays().contains(cd)) {
                             blue = true;
                             break innerLoop;
                         }
                         if (address.getYellowDays().contains(cd)) {
                             yellow = true;
                             break innerLoop;
                         }
                     }
                     sendMail(notification,
                             notification.getNotificationsOnBlueDay()
                             .contains(NotificationTransport.EMAIL),
                             notification.getNotificationsOnYellowDay()
                             .contains(NotificationTransport.EMAIL));
                     sendXMPP(notification,
                             notification.getNotificationsOnBlueDay()
                             .contains(NotificationTransport.EMAIL),
                             notification.getNotificationsOnYellowDay()
                             .contains(NotificationTransport.EMAIL));
                     out.println(notification.getUser().getEmail());
                 }
             } finally {
                 pm.close();
             }
         }
     }
 
     private void sendMail(
             Notification notification,
             boolean blue,
             boolean yellow) {
         String type;
         if (blue && yellow) {
             type = "bleues et jaunes";
         } else if (blue) {
             type = "bleues";
         } else if (yellow) {
             type = "jaunes";
         } else {
             return;
         }
         Session session = Session.getDefaultInstance(new Properties(), null);
         User recipient = notification.getUser();
         try {
             Message msg = new MimeMessage(session);
             msg.setFrom(new InternetAddress(
                     Constants.MAILER_ADDRESS,
                     "Piu-bella update notifier"));
             msg.addRecipient(Message.RecipientType.TO,
                     new InternetAddress(recipient.getEmail()));
             msg.setSubject("Sortez vos poubelles !");
             msg.setText("Bonjour\n\n"
                     + "Demain, c'est le jour de collecte des poubelles "
                     + type
                     + " pour votre adresse "
                     + notification.getAddress().getStreet()
                     + ". N'oubliez pas de les sortir :)"
                     + "\n\n"
                     + "La team de Piu Bella");
             Transport.send(msg);
 
 
 
         } catch (MessagingException | UnsupportedEncodingException e) {
             Logger.getLogger(Notifier.class
                     .getName())
                     .log(Level.SEVERE, null, e);
         }
 
     }
 
     private void sendXMPP(
             Notification notification,
             boolean blue,
             boolean yellow) {
         String type;
         if (blue && yellow) {
             type = "bleues et jaunes";
         } else if (blue) {
             type = "bleues";
         } else if (yellow) {
             type = "jaunes";
         } else {
             return;
         }
         String email = notification.getUser().getEmail();
         JID jid = new JID(email);
         String msgBody = "Bonjour\n\n"
                 + "Demain, c'est le jour de collecte des poubelles "
                 + type
                 + " pour votre adresse "
                 + notification.getAddress().getStreet()
                 + ". N'oubliez pas de les sortir :)"
                 + "\n\n"
                 + "La team de Piu Bella";
         com.google.appengine.api.xmpp.Message msg = new MessageBuilder()
                 .withRecipientJids(jid)
                 .withBody(msgBody)
                 .build();
 
         XMPPService xmpp = XMPPServiceFactory.getXMPPService();
         if (xmpp.getPresence(jid).isValid()) {
            xmpp.sendInvitation(jid);
             SendResponse status = xmpp.sendMessage(msg);
             if (status.getStatusMap().get(jid) != SendResponse.Status.SUCCESS) {
                 Logger.getLogger(Notification.class.getName()).log(Level.SEVERE, "{0} not sent", email);
             }
         }
     }
 }
