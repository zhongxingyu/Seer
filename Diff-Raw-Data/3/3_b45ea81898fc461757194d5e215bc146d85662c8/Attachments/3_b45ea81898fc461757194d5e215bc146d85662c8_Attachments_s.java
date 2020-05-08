 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 import java.io.File;
 
 import org.jcrom.JcrFile;
 import org.jcrom.JcrMappingException;
 
 import play.modules.cream.JCR;
 import play.modules.cream.ocm.JcrMapper;
 import play.modules.cream.ocm.JcrQueryResult;
 import play.modules.cream.ocm.JcrVersionMapper;
 import play.libs.MimeTypes;
 
 import models.User;
 import models.Node;
 import models.Attachment;
 
 @With(Secure.class)
 public class Attachments extends Controller {
 
   public static void index(Long budget_id) {
    List<Attachment> attachments = Attachment.findAll();
     renderJSON(attachments);
   }
 
   public static void create(String label, String description, long budgetId, File attachment){
     long userId = Long.parseLong(Security.connected());
     Attachment a = new Attachment(label, description, userId, budgetId, attachment);
     a.save();
   }
 
   public static void showFile(long attachmentId){
     Attachment a = Attachment.findById(attachmentId);
     JcrFile j = a.getFile();
     response.setContentTypeIfNotSet(j.getMimeType());
     renderBinary(j.getDataProvider().getInputStream());
   }
 
 }
