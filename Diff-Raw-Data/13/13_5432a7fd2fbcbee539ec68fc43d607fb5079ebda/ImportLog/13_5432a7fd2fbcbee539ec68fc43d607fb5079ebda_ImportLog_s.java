 package models;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
 import play.db.jpa.Model;
 import play.i18n.Messages;
 
 @Entity
 public class ImportLog extends Model {
 
     @ManyToOne
     public Import importEntity;
 
     @Enumerated(EnumType.STRING)
     public Error error;
 
     public String messageKey;
 
     @Transient
     public String message;
 
     public Date logDate = new Date();
 
     public String jsonObject;
 
    public ImportLog(Import currentImport, Error error, String messageKey, String jsonObject) {
         this.importEntity = currentImport;
         this.error = error;
         this.messageKey = messageKey;
         this.jsonObject = jsonObject;
     }
 
     public String getMessage() {
         return Messages.get(this.messageKey);
     }
 
     public static enum Error {
         NONE, FORMAT, ALREADY_EXISTS
     }
 
 }
