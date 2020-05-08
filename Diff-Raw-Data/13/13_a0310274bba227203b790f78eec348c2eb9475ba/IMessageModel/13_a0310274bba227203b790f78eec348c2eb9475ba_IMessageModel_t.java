 package org.jtheque.views.impl.models;
 
import org.jtheque.messages.Message;
 import org.jtheque.ui.able.IModel;
 
 /**
  * @author Baptiste Wicht
  */
 public interface IMessageModel extends IModel {
     /**
      * Return the next message to display.
      *
      * @return The next message to display.
      */
     Message getNextMessage();
 
     /**
      * Return the current message.
      *
      * @return The current message.
      */
     Message getCurrentMessage();
 
     /**
      * Return the previous message.
      *
      * @return The previous message.
      */
     Message getPreviousMessage();
 
     /**
      * Indicate if the model is currently displaying the default message or a real message.
      *
      * @return true if the current message is the default message else false.
      */
     boolean isDefaultMessage();
 }
