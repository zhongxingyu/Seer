 package ru.gosuslugi.geps.ng.dao;
 
 import org.junit.Assert;
 import org.junit.Test;
 import ru.gosuslugi.geps.ng.dao.impl.MessageDaoImpl;
 import ru.gosuslugi.geps.ng.model.Message;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: rnasyrov
  * Date: 08.01.13
  * Time: 13:34
  */
 public class MessageDaoTest {
 
     @Test
     public void shouldGetMessage() throws Exception {
         MessageDao dao = new MessageDaoImpl();
         Message message = dao.getById(123L);
         Assert.assertEquals(Long.valueOf(123L), message.getMessageId());
         Assert.assertEquals(Long.valueOf(4444L), message.getFromId());
         Assert.assertEquals(Long.valueOf(1L), message.getToId());
         Assert.assertNotNull(message.getSubject());
         Assert.assertNotNull(message.getText());
         Assert.assertEquals(message.getSendDate(), message.getUpdateDate());
         Assert.assertNull(dao.getById(555L));
     }
 
 
     @Test
     public void shouldCreateMessage() throws Exception {
         MessageDao dao = new MessageDaoImpl();
 
         Message message = new Message();
         message.setSubject("Hello World!");
         message.setSendDate(new Date());
         message.setFromId(222L);
 
         Message createdMessage = dao.create(message);
         Assert.assertNotNull(createdMessage.getMessageId());
         Assert.assertEquals(message.getSubject(), createdMessage.getSubject());
     }
 
     @Test
     public void shouldGetUserMessages() throws Exception {
         MessageDao dao = new MessageDaoImpl();
         List<Message> messages = dao.getUserMessages(4444L);
        Assert.assertEquals(3, messages.size());
     }
 
 }
