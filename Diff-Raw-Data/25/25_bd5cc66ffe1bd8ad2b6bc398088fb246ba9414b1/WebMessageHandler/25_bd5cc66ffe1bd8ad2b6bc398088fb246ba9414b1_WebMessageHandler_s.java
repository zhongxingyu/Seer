 package swag49.messaging.handler;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.client.RestTemplate;
 import swag49.dao.DataAccessObject;
 import swag49.messaging.model.Message;
 import swag49.messaging.model.MessageDTO;
 import swag49.model.User;
 import swag49.util.Log;
 
 import java.util.Date;
 
 @Component("webMessageHandler")
 public class WebMessageHandler {
     @Log
     private Logger logger;
 
     @Autowired
     @Qualifier("messageDAO")
     private DataAccessObject<Message, Long> messageDAO;
 
     @Autowired
     @Qualifier("userDAO")
     private DataAccessObject<User, String> userDAO;
 
     @Autowired
     private RestTemplate restTemplate;
 
    @Transactional("swag49.user")
    public String getUsername(String userId) {
        return userDAO.get(userId).getUsername();
    }

    @Transactional("swag49.messaging")
    public Message storeAndReceiveMessage(Message message) {
        message.setReceiveDate(new Date());
        return messageDAO.create(message);
    }

     @ServiceActivator
     public void handleMessage(Message message) {
         logger.info("Sending message {} to web-server", message);
        message = storeAndReceiveMessage(message);

        String senderUsername = getUsername(message.getSenderUserId());
        String receiverUsername = getUsername(message.getReceiverUserId());
 
         MessageDTO messageDTO =
                 new MessageDTO(message.getId(), message.getSubject(), message.getContent(), message.getSenderUserId(),
                        senderUsername, message.getReceiverUserId(), receiverUsername, message.getSendDate(),
                        message.getReceiveDate(), message.getMapUrl());
 
         String requestUri = message.getMapUrl() + "/swag-api/messaging/receive";
         restTemplate.put(requestUri, messageDTO);
     }
 }
