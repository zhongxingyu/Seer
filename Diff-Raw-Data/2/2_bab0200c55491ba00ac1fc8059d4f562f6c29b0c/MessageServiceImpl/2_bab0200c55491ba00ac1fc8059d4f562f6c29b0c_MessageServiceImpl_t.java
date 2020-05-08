 package at.ac.tuwien.swag.webapp.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import at.ac.tuwien.swag.model.dao.MessageDAO;
 import at.ac.tuwien.swag.model.dao.UserDAO;
 import at.ac.tuwien.swag.model.domain.Message;
 import at.ac.tuwien.swag.model.domain.User;
 import at.ac.tuwien.swag.model.dto.MessageDTO;
 import at.ac.tuwien.swag.model.dto.UserDTO;
 import at.ac.tuwien.swag.webapp.service.MessageService;
 
 import com.google.inject.Inject;
 
 public class MessageServiceImpl implements MessageService {
 
     private MessageDAO messages;
     private UserDAO users;
 
     @Inject
     public MessageServiceImpl(EntityManager em) {
         messages = new MessageDAO(em);
         users = new UserDAO(em);
     }
 
     @Override
     public List<MessageDTO> getInMessages(String user) {
         String query =
             "SELECT m FROM Message m LEFT JOIN FETCH m.from LEFT JOIN FETCH m.to AS y WHERE y.name = :username";
 
         Map<String, String> values = new HashMap<String, String>();
         values.put("username", user);
 
         List<Message> inMessages = messages.findByQuery(query, values);
         List<MessageDTO> result = new ArrayList<MessageDTO>();
 
         for (Message m : inMessages) {
             result.add(
             	new MessageDTO(
             		m.getTimestamp(),
             		m.getSubject(),
             		"",
             		m.getRead(),
             		new UserDTO(
             			m.getFrom().getUsername(),
             			"",
             			"",
             			"",
             			"",
             			null,
             			null,
             			null
             		),
             		null
             	)
             );
         }
 
         return result;
     }
 
     @Override
     public List<MessageDTO> getOutMessages(String user) {
         String query =
             "SELECT m FROM Message m LEFT JOIN FETCH m.from WHERE m.from.name = :username";
 
         Map<String, String> values = new HashMap<String, String>();
         values.put("username", user);
 
         List<Message> inMessages = messages.findByQuery(query, values);
         List<MessageDTO> result = new ArrayList<MessageDTO>();
 
         for (Message m : inMessages) {
             result.add(
             	new MessageDTO(
             		m.getTimestamp(), 
             		m.getSubject(), 
             		"",
             		m.getRead(),
             		new UserDTO(
             			m.getFrom().getUsername(),
             			"",
             			"",
             			"",
             			"",
             			null,
             			null,
             			null
             		),
             		null
             	)
             );
         }
 
         return result;
     }
 
     @Override
     public List<MessageDTO> getNotifications(String user) {
         String query =
             "SELECT m FROM Message m LEFT JOIN FETCH m.from LEFT JOIN FETCH m.to AS y WHERE y.name = :username AND m.from.name = :system";
 
         Map<String, String> values = new HashMap<String, String>();
         values.put("username", user);
         values.put("system", "system");
 
         List<Message> inMessages = messages.findByQuery(query, values);
         List<MessageDTO> result = new ArrayList<MessageDTO>();
 
         for (Message m : inMessages) {
             result.add(
             	new MessageDTO(
             		m.getTimestamp(),
             		m.getSubject(),
             		"",
             		m.getRead(),
             		new UserDTO(
             			m.getFrom().getUsername(),
             			"",
             			"",
             			"",
             			"",
             			null,
             			null,
             			null
             		),
             		null
             	)
             );
         }
 
         return result;
     }
 
     @Override
     public MessageDTO getMessagebyId(Long id, String user) {
 
         String query =
             "SELECT m FROM Message m LEFT JOIN FETCH m.from LEFT JOIN FETCH m.to WHERE m.id = :number";
 
         Map<String, String> values = new HashMap<String, String>();
        values.put("number", id.toString());
 
         List<Message> message = messages.findByQuery(query, values);
 
         if (message.isEmpty()) {
             return null;
         }
 
         Message m = message.get(0);
 
         return new MessageDTO(
         		m.getTimestamp(),
         		m.getSubject(),
         		m.getText(),
         		m.getRead(),
         		new UserDTO(
             		m.getFrom().getUsername(),
             		"",
             		"",
             		"",
             		"",
             		null,
             		null,
             		null
             	),
         		null
         );
     }
 
     @Override
     public void sendMessage(String subject, String text, Set<String> reciever, String sender) {
 
         Message message = new Message();
         message.setTimestamp(new Date());
         message.setSubject(subject);
         message.setText(text);
         message.setRead(false);
         message.setFrom(users.findByUsername(sender));
 
         Set<User> recieverAsUser = new HashSet<User>();
         for (String rec : reciever) {
             recieverAsUser.add(users.findByUsername(rec));
         }
 
         message.setTo(recieverAsUser);
 
         messages.insert(message);
         // TODO check online status and send mails
 
     }
 
     @Override
     public void sendNotification(String subject, String text, String reciever) {
 
         checkPostmaster();
 
         Message message = new Message();
         message.setTimestamp(new Date());
         message.setSubject(subject);
         message.setText(text);
         message.setRead(false);
         message.setFrom(users.findByUsername("postmaster"));
 
         Set<User> recieverAsUser = new HashSet<User>();
         recieverAsUser.add( users.findByUsername(reciever) );
 
         message.setTo(recieverAsUser);
 
         messages.insert(message);
 
         // TODO check online status and send mails
 
     }
 
     private void checkPostmaster() {
         // create postmaster aka root oder so
 
     	try {
     		users.findByUsername( "postmaster" );
     	} catch ( NoResultException e ) {
     		users.beginTransaction();
     			User user = new User();
     			user.setUsername("postmaster");
     			users.insert(user);
     		users.commitTransaction();
     	}
     }
 
     @Override
     public void updateReadStatus(Long id) {
 
         Message message = messages.findById(id);
         message.setRead(true);
         messages.update(message);
 
     }
 
 }
