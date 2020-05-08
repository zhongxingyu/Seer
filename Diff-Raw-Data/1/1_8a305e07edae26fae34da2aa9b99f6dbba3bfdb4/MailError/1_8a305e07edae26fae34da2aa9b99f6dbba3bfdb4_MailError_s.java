 package de.deepamehta.plugins.mail;
 
 public enum MailError {
 
     ATTACHMENTS("File errors"), //
     CONTENT("Invalid content"), //
     RECIPIENT_TYPE("Unsupported recipient type"), //
     RECIPIENTS("Invalid recpients"), //
     SENDER("Invalid sender"), //
     UPDATE("Update error");
 
     private final String message;
 
     private MailError(String message) {
         this.message = message;
     }
 
     public String getMessage() {
         return message;
     }
 
 }
