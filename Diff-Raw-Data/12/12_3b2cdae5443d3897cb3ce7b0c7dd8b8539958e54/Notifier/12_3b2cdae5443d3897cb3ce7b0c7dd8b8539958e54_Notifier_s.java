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
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
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
                 toTest.add(CollectDay.SATURDAY);
                 break;
             case Calendar.SATURDAY:
                 toTest.add(CollectDay.SUNDAY);
                 break;
             case Calendar.SUNDAY:
                 toTest.add(CollectDay.MONDAY);
                 break;
         }
         PersistenceManager pm = PMF.get().getPersistenceManager();
         Query q = pm.newQuery(User.class);
         try {
             List<User> results =
                     (List<User>) q.execute();
             for (User user : results) {
                 Map<String, String> email = new HashMap<>(),
                         xmpp = new HashMap<>();
                 Set<Notification> notifications = user.getNotifications();
                 for (Notification notification : notifications) {
                     boolean blueEmail = false,
                             blueXmpp = false,
                             yellowEmail = false,
                             yellowXmpp = false;
                     Address address = notification.getAddress();
                     String street = address.getStreet();
                     innerLoop:
                     for (CollectDay cd : toTest) {
                         boolean found = false;
                         if (address.getBlueDays().contains(cd)) {
                             if (notification.getNotificationsOnBlueDay()
                                     .contains(NotificationTransport.EMAIL)) {
                                 blueEmail = true;
                             }
                             if (notification.getNotificationsOnBlueDay()
                                     .contains(NotificationTransport.XMPP)) {
                                 blueXmpp = true;
                             }
                             found = true;
                         }
                         if (address.getYellowDays().contains(cd)) {
                             if (notification.getNotificationsOnYellowDay()
                                     .contains(NotificationTransport.EMAIL)) {
                                 yellowEmail = true;
                             }
                             if (notification.getNotificationsOnYellowDay()
                                     .contains(NotificationTransport.XMPP)) {
                                 yellowXmpp = true;
                             }
                             found = true;
                         }
                        break innerLoop;
                     }
                     if (blueEmail && yellowEmail) {
                         email.put(street, "bleues et jaunes");
                     } else if (blueEmail) {
                         email.put(street, "bleues");
                     } else if (yellowEmail) {
                         email.put(street, "jaunes");
                     }
                     if (blueXmpp && yellowXmpp) {
                         xmpp.put(street, "bleues et jaunes");
                     } else if (blueXmpp) {
                         xmpp.put(street, "bleues");
                     } else if (yellowXmpp) {
                         xmpp.put(street, "jaunes");
                     }
                 }
                 sendMail(user, email);
                 sendXMPP(user, xmpp);
             }
         } finally {
             pm.close();
         }
     }
 
     private void sendMail(User user, Map<String, String> toSend) {
         if (toSend.isEmpty()) {
             return;
         }
         Session session = Session.getDefaultInstance(new Properties(), null);
         StringBuilder sb = new StringBuilder();
         sb.append("Bonjour\n\nDemain, c'est le jour de collecte des poubelles ")
                 .append("pour ");
         if (toSend.size() > 1) {
             sb.append("vos adresses :\n\n");
         } else {
             sb.append("votre adresse :\n\n");
         }
         for (String address : toSend.keySet()) {
             sb.append("- ")
                     .append(address)
                     .append(" (poubelles ")
                     .append(toSend.get(address))
                     .append(")\n");
         }
         sb.append("\nN'oubliez pas des les sortir :)\n\n")
                 .append("La team de Piu Bella");
         String from = Constants.MAILER_ADDRESS,
                 recipient = user.getEmail(),
                 subject = "Sortez vos poubelles !",
                 message = sb.toString();
         try {
             Message msg = new MimeMessage(session);
             msg.setFrom(new InternetAddress(from, "Piu-bella update notifier"));
             msg.addRecipient(Message.RecipientType.TO,
                     new InternetAddress(recipient));
             msg.setSubject(subject);
             msg.setText(message);
             Transport.send(msg);
 
 
 
 
 
         } catch (MessagingException | UnsupportedEncodingException e) {
             Logger.getLogger(Notifier.class
                     .getName())
                     .log(Level.SEVERE, null, e);
         }
 
     }
 
     private void sendXMPP(User user, Map<String, String> toSend) {
         if (toSend.isEmpty()) {
             return;
         }
         String email = user.getEmail();
         JID jid = new JID(email);
         StringBuilder sb = new StringBuilder();
         sb.append("Bonjour\n\nDemain, c'est le jour de collecte des poubelles ")
                 .append("pour ");
         if (toSend.size() > 1) {
             sb.append("vos adresses :\n\n");
         } else {
             sb.append("votre adresse :\n\n");
         }
         for (String address : toSend.keySet()) {
             sb.append("- ")
                     .append(address)
                     .append(" (poubelles ")
                     .append(toSend.get(address))
                     .append(")\n");
         }
         sb.append("\nN'oubliez pas des les sortir :)\n\n")
                 .append("La team de Piu Bella");
         String msgBody = sb.toString();
         com.google.appengine.api.xmpp.Message msg = new MessageBuilder()
                 .withRecipientJids(jid)
                 .withBody(msgBody)
                 .build();
 
         XMPPService xmpp = XMPPServiceFactory.getXMPPService();
         if (xmpp.getPresence(jid).isValid()) {
             xmpp.sendInvitation(jid);
             SendResponse status = xmpp.sendMessage(msg);
 
 
             if (status.getStatusMap().get(jid) != SendResponse.Status.SUCCESS) {
                 Logger.getLogger(Notification.class
                         .getName()).log(Level.SEVERE, "{0} not sent", email);
             }
         }
     }
 }
