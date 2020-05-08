 package uy.com.elsubonline.service;
 
 import java.util.Date;
 import javax.annotation.Resource;
 import javax.ejb.Stateless;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.persistence.NoResultException;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import uy.com.elsubonline.api.IUserService;
 import uy.com.elsubonline.api.exceptions.AddressAlreadyInUseException;
 import uy.com.elsubonline.api.exceptions.NotificationException;
 import uy.com.elsubonline.domain.User;
 import uy.com.elsubonline.domain.UserStatus;
 
 public @Stateless class UserService implements IUserService {
 
     private final static Logger logger = Logger.getLogger(UserService.class);
 
     @javax.persistence.PersistenceContext(unitName="uy.com.elsubonline.persistence")
     private javax.persistence.EntityManager em;
 
     @Resource(name = "java:/ConnectionFactory")
     private static ConnectionFactory connectionFactory;
 
     @Resource(name = "java:/jms/elsubonline/registration")
     private static Queue registrationQueue;
 
     @Override
     public void create(String email, String nick_name, String first_name, String last_name, String password, String phone, boolean subscribed) throws AddressAlreadyInUseException, NotificationException {
         logger.info("UserService.create " + email);
         User user = new User();
         user.setEmail(email);
         user.setNick_name(nick_name);
         user.setFirst_name(first_name);
         user.setLast_name(last_name);
         user.setPhone(phone);
         user.setSubscribed(subscribed);
         user.setPassword("SHA1:" + DigestUtils.shaHex(password));
         user.setStatus(UserStatus.NEW_USER);
         user.setCreation_time(new Date());
 
         try {
             em.persist(user);
             em.flush();
         } catch (Exception ex) {
             throw(new AddressAlreadyInUseException(ex.getMessage()));
         }
 
         logger.info("UserService.created user: " + email);
 
         // Create the needed artifacts to connect to the queue
         Connection connection;
         try {
             connection = connectionFactory.createConnection();
         } catch (JMSException ex) {
             logger.error("Error trying to createConnection", ex);
             throw(new NotificationException(email));
         }
         Session session;
         try {
             session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         } catch (JMSException ex) {
             logger.error("Error trying to createSession", ex);
             throw(new NotificationException(email));
         }
         MessageProducer producer;
         try {
             producer = session.createProducer(registrationQueue);
         } catch (JMSException ex) {
             logger.error("Error trying to createProducer", ex);
             throw(new NotificationException(email));
         }
 
         // Send text message to the queue
         TextMessage message;
         try {
             message = session.createTextMessage();
         } catch (JMSException ex) {
             logger.error("Error trying to createTextMessage", ex);
             throw(new NotificationException(email));
         }
         try {
             message.setText(email);
         } catch (JMSException ex) {
             logger.error("Error trying to setText", ex);
             throw(new NotificationException(email));
         }
         try {
             producer.send(message);
         } catch (JMSException ex) {
             logger.error("Error trying to send", ex);
             throw(new NotificationException(email));
         }
         try {
             connection.close();
         } catch (JMSException ex) {
             throw(new NotificationException(email));
         }
         logger.info("UserService.created sent to notification queue: " + email);
 
     }
     
     @Override
     public boolean validate_credentials(String username, String password) {
         password = "SHA1:" + DigestUtils.shaHex(password);
         
         try {
             String result = (String) em.createNamedQuery("validate_credentials")
                     .setParameter("username", username)
                     .setParameter("password", password).getSingleResult();
             logger.info("Got user:" + result);
         } catch (NoResultException ex) {
             return false;
         }
         return true;
     }
 }
