 package models;
 
 //Default play stuff 
 import play.data.validation.*;
 
 //Morphia stuff
 import play.modules.morphia.Model;
 import play.modules.morphia.Model.AutoTimestamp;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 
 //Jackrabbit stuff
 import org.jcrom.JcrFile;
 import org.jcrom.JcrMappingException;
 
 import java.io.File;
 import play.libs.MimeTypes;
 
 /**
  * The attachment model
  */
 @AutoTimestamp
 @Entity
 public class Attachment extends Model {
 
   private long budgetId;
   public String nodeId;
   private long userId;
 
   // @Required
   // public int id;
 
   @Required
   public String name;
 
   @Required
  @Match("^[A-Za-z0-9_/-]+$")
   //searchText.matches("[A-Za-z0-9_\\-]+")
   public String label;
 
   @Required
   public String description;
 
   @Required
   public String user;
 
   @Required
   public Node node;
 
   /**
    * Attachment object constructor.
    *
    * @param label Jackrabbit label of the attachment
    * @param description The description of the attachment set by the
    * user
    * @param userId The id of the user who uploaded the attachment
    * @param budgetId The id of the associated budget
    * @param attachment The binary file of the uploaded attachment
    */
   public Attachment(String label, String description, long userId, String user, long budgetId, File attachment) {
     this.name = attachment.getName();
     this.label = parseLabel(label);
     this.description = description;
     this.user = user;
     this.userId = userId;
     this.budgetId = budgetId;
     this.nodeId = createNode(attachment);
   }
 
   /**
    * Fetch associated file from Jackrabbit
    *
    * @return The jackrabbit representation of the file
    */
   public JcrFile getFile() {
     Node n = getNode();
     return n.file;
   }
 
   public Node getNode() {
 	
     return Node.findById(nodeId);
 	
   }
 
   /**
    * Create a node for the supplied file.
    *
    * @param attachment The binary file uploaded the controller
    *
    * @return the jackrabbit id of the node the file was uploaded to
    */
   public String createNode(File attachment) {
     Node n = new Node(this.label, this.description);
     n.file = JcrFile.fromFile(this.label, attachment,MimeTypes.getContentType(attachment.getName()));
     n.save();
     return n.getId();
   }
 
 
   public String toString() {
     return label;
   }
 
   /**
    * Helper function to sanitize label input. Removes tabs, forms,
    * carriage returns in new lines. Also ensure that label starts with a
    * "/"
    *
    * @param label The label we are sanitizing.
    *
    * @return The sanitized label
    */
   public String parseLabel(String label) {
     label = label.toLowerCase();
     label = label.replaceAll("\t", " ");
     label = label.replaceAll("\f", "");
     label = label.replaceAll("\r", "");
     label = label.replaceAll("\n", "");
     if(label.startsWith("/")){
       return label;
     }
     else {
       String newLabel = "/" + label;
       return newLabel;
     }
 
   }
 
 }
