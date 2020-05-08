 package swag49.messaging.transformer.impl;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import swag49.dao.DataAccessObject;
 import swag49.messaging.model.Message;
 import swag49.messaging.model.MessageDTO;
 import swag49.messaging.transformer.MessageTransformer;
 import swag49.model.User;
 import swag49.util.Log;
 
 import java.util.List;
 
 @Controller("messageTransformer")
 public class MessageTransformerImpl implements MessageTransformer {
     @Log
     private Logger log;
 
     @Autowired
     @Qualifier("userDAO")
     private DataAccessObject<User, Long> userDAO;
 
     @Transactional("swag49.user")
     private String getUserId(MessageDTO.UserDTO userDTO) {
         userDTO = Preconditions.checkNotNull(userDTO);
 
         if (userDTO.getId() != null)
             return userDTO.getId();
 
         Preconditions.checkArgument(!Strings.isNullOrEmpty(userDTO.getUsername()),
                 "if id is null username mustn't be null or empty");
 
         User user = new User();
         user.setUsername(userDTO.getUsername());
        user.setMapLocations(null);
 
         List<User> users = userDAO.queryByExample(user);
 
         Preconditions.checkArgument(users != null && users.size() == 1, "no user with name %s found",
                 userDTO.getUsername());
 
         return users.get(0).getUsername();
     }
 
     @Transformer
    @Override
    public Message apply(MessageDTO input) {
         Message output = new Message();
 
         output.setId(input.getId());
         output.setContent(input.getContent());
         output.setSendDate(input.getSent());
         output.setReceiveDate(input.getReceived());
         output.setReceiverUserId(getUserId(input.getReceiver()));
         output.setSenderUserId(getUserId(input.getSender()));
         output.setSubject(input.getSubject());
         output.setMapUrl(input.getMapUrl());
 
         return output;
     }
 }
