 package org.einherjer.twitter.tickets.websocket;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import lombok.AllArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.velocity.app.VelocityEngine;
 import org.eclipse.jetty.websocket.api.Session;
 import org.einherjer.twitter.tickets.model.Ticket;
 import org.einherjer.twitter.tickets.model.User;
 import org.einherjer.twitter.tickets.model.User.Role;
 import org.einherjer.twitter.tickets.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.env.Environment;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializerProvider;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.fasterxml.jackson.databind.ser.std.StdSerializer;
 
 @Slf4j
 @Service
 public final class NotificationService {
 
     @Autowired
     private NotificationsWebSocketPool pool;
     @Autowired
     private UserRepository userRepository;
     @Autowired
     private JavaMailSender mailSender;
     @Autowired
     private Environment environment;
     @Autowired
     private VelocityEngine velocityEngine;
 
     private final ObjectMapper objectMapper = new ObjectMapper();
 
     public NotificationService() {
         objectMapper.registerModule(new SimpleModule() {
             {
                 addSerializer(NotificationType.class, new StdSerializer<NotificationType>(NotificationType.class) {
                     @Override
                     public void serialize(NotificationType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
                         switch (value) {
                             case NOTICE:
                                 jgen.writeString("");
                                 break;
                             case INFO:
                                 jgen.writeString("info");
                                 break;
                             case SUCCESS:
                                 jgen.writeString("sucess");
                                 break;
                             case ERROR:
                                 jgen.writeString("error");
                                 break;
                             default:
                                 throw new UnsupportedOperationException("method not implemented for value: " + this);
                         }
                     }
                 });
             }
         });
     }
 
     public void notifyCreation(Ticket ticket) {
         try {
             this.addToUserUnreadList(ticket, Role.APPROVER);
             this.send(pool.getApproverSessions(), objectMapper.writeValueAsString(new Notification(NotificationType.INFO, "", "A new ticket has been created")));
            this.sendEmailToCreator(ticket, "Ticket creation confirmation");
         }
         catch (JsonProcessingException e) {
             log.error("Unexpected error", e);
             throw new RuntimeException("Unexpected error", e);
         }
     }
 
     public void notifyApproval(Ticket ticket) {
         try {
             this.removeFromUserUnreadList(ticket); //remove from the approvers
             this.addToUserUnreadList(ticket, Role.EXECUTOR); //add to the executors
             this.send(pool.getExecutorSessions(), objectMapper.writeValueAsString(new Notification(NotificationType.INFO, "", "There's a new approved ticket")));
             this.notifyCreator(ticket, "Your ticket " + ticket.getTicketId() + " has been approved");
         }
         catch (JsonProcessingException e) {
             log.error("Unexpected error", e);
             throw new RuntimeException("Unexpected error", e);
         }
     }
 
     public void notifyCreatorCancel(Ticket ticket) {
         this.removeFromUserUnreadList(ticket); //remove from approvers
        this.sendEmailToCreator(ticket, "Ticket cancellation confirmation");
     }
 
     public void notifyApproverCancel(Ticket ticket) {
         this.removeFromUserUnreadList(ticket); //remove from approvers
         this.notifyCreator(ticket, "Your ticket " + ticket.getTicketId() + " has been cancelled");
     }
 
     public void notifyReject(Ticket ticket) {
         this.removeFromUserUnreadList(ticket); //remove from approvers
         this.notifyCreator(ticket, "Your ticket " + ticket.getTicketId() + " has been rejected");
     }
 
     public void notifyDone(Ticket ticket) {
         this.removeFromUserUnreadList(ticket); //remove from executors
         this.notifyCreator(ticket, "Your ticket " + ticket.getTicketId() + " has been completed");
     }
 
     public void notifyChangePriority(Ticket ticket) {
         this.notifyCreator(ticket, "The priority of your ticket " + ticket.getTicketId() + " has been updated");
     }
 
     private void removeFromUserUnreadList(Ticket ticket) {
         //TODO: this is very inefficient, read all users every time a ticket is cancelled, rejected, etc; we should have an object cache or some pre populated structure, updated by the user CRUD that should be infrequent
         for (User user : userRepository.findAll()) {
             user.read(ticket);
         }
     }
 
     private void addToUserUnreadList(Ticket ticket, Role role) {
         //TODO: this is very inefficient, read all users (by role) every time a ticket is created, we should have an object cache or some pre populated structure, updated by the user CRUD that should be infrequent
         for (User user : userRepository.findByRole(role)) {
             user.addUnread(ticket);
         }
     }
 
     private void send(Set<Session> sessions, String message) {
         for (Session session : sessions) {
             this.send(session, message);
         }
     }
 
     private void send(Session session, String message) {
         if (session.isOpen()) {
             session.getRemote().sendStringByFuture(message);
         }
     }
 
     /**
      * sends a popup notification to the user, as well as an email
      * should be used when an approver or executor performs an action on the creator ticket
      * for the cases when the creator is the one performing the action and a confirmation email should be sent but not a popup notification use sendEmailToCreator instead of this method
      */
     private void notifyCreator(Ticket ticket, String message) {
         Set<Session> sessions = pool.getRequestorSessions(ticket.createdBy());
         if (sessions.isEmpty()) {
             log.info("No sessions found for user: " + ticket.createdBy() + ". It might be offline");
         }
         else {
             try {
                 this.send(sessions, objectMapper.writeValueAsString(new Notification(NotificationType.INFO, "", message)));
             }
             catch (JsonProcessingException e) {
                 log.error("Unexpected error", e);
                 throw new RuntimeException("Unexpected error", e);
             }
         }
         this.sendEmailToCreator(ticket, message);
     }
 
     private void sendEmailToCreator(Ticket ticket, String subject) {
         try {
             MimeMessage message = mailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8"); //if not set encoding defaults to us-ascii and links in the email do not work (use "Show Original" option in gmail)
             helper.setSubject(subject);
 
             //using mail.to property (if set) instead of the actual user email to ease testing
             String mailToProperty = environment.getProperty("mail.to");
             if (StringUtils.isBlank(mailToProperty)) {
                 helper.setTo(ticket.createdBy().getUsername().toString());
             }
             else {
                 helper.setTo(mailToProperty);
             }
 
             Map<String, Object> model = new HashMap<String, Object>();
            model.put("message", subject.replace(ticket.getTicketId(), "<a href=\"http://localhost:8080/" + ticket.getTicketId() + "\">" + ticket.getTicketId() + "</a>"));
             String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "email.vm", "UTF-8", model);
             // use the true flag to indicate the text included is HTML
 
             helper.setText(body, true);
 
             mailSender.send(message);
         }
         catch (MessagingException e) {
             log.error("Unexpected error", e);
             throw new RuntimeException("Unexpected error", e);
         }
     }
 
     @AllArgsConstructor
     public static class Notification {
         public NotificationType type;
         public String title;
         public String text;
     }
     
     private static enum NotificationType {
         NOTICE,
         INFO,
         SUCCESS,
         ERROR;
 
         public String toString(){
             switch(this){
                 case NOTICE:
                     return "";
                 case INFO:
                     return "info";
                 case SUCCESS:
                     return "success";
                 case ERROR:
                     return "error";
                 default:
                     throw new UnsupportedOperationException("method not implemented for value: " + this);
             }
         }
     }
     
 }
