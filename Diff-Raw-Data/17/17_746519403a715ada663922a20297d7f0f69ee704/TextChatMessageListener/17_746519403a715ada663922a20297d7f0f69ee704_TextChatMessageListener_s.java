 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package org.jdesktop.wonderland.modules.textchat.server;
 
 import com.sun.sgs.app.ManagedObject;
 import java.io.Serializable;
 import org.jdesktop.wonderland.modules.textchat.common.TextChatMessage;
 
 /**
  * For classes that want to receive notices about new TextChatMessages.
  *
  * @author drew_harry
  */
 public interface TextChatMessageListener extends Serializable, ManagedObject {
 
     public void handleMessage(TextChatMessage msg);
 }
