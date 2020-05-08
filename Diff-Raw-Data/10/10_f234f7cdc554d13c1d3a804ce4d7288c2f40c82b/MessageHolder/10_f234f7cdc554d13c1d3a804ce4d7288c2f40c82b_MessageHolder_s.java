 package me.josvth.bukkitformatlibrary.message;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class MessageHolder {
 
     private boolean keyWhenMissing = true;
 
     private final Map<String, FormattedMessage> messages = new HashMap<String, FormattedMessage>();
 
     public void clear() {
         messages.clear();
     }
 
     public Map<String, FormattedMessage> getMessages() {
         return messages;
     }
 
     public FormattedMessage addMessage(String key, String message) {
 
         FormattedMessage formattedMessage = new FormattedMessage(message);
         addMessage(key.toLowerCase(), formattedMessage);
 
         return formattedMessage;
     }
 
     public void addMessage(String key, FormattedMessage message) {
         messages.put(key.toLowerCase(), message);
     }
 
     public boolean hasMessage(String key) {
         return messages.containsKey(key);
     }
 
     public boolean isKeyWhenMissing() {
         return keyWhenMissing;
     }
 
     public void setKeyWhenMissing(boolean keyWhenMissing) {
         this.keyWhenMissing = keyWhenMissing;
     }
 
     public FormattedMessage getMessage(String key) {
         FormattedMessage message = messages.get(key.toLowerCase());
        if (message == null && isKeyWhenMissing()) {
            message = new FormattedMessage(key);
        } else {
            message = new FormattedMessage(null);
         }
         return message;
     }
 
 
 }
