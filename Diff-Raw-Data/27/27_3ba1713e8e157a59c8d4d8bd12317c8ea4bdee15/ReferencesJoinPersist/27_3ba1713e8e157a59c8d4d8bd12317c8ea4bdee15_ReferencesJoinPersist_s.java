 package ro.finsiel.eunis.jrfTables.species.references;
 
 
 /**
  * Date: Jul 15, 2003
  * Time: 11:15:08 AM
  */
 
 import net.sf.jrf.domain.PersistentObject;
 
 import java.util.Date;
 
 
 public class ReferencesJoinPersist extends PersistentObject {
 
     private String Source = null;
     private String Editor = null;
    private Date created = null;
     private String Title = null;
     private String Publisher = null;
     private String url = null;
 
     /**
      * This is a database field.
      **/
     private Integer i_idDc = null;
 
     /**
      * This is a database field.
      **/
     private String i_comment = null;
 
     public ReferencesJoinPersist() {
         super();
     }
 
     /**
      * Getter for a database field.
      **/
     public String getComment() {
         return i_comment;
     }
 
     /**
      * Getter for a database field.
      **/
     public Integer getIdDc() {
         return i_idDc;
     }
 
     /**
      * Setter for a database field.
      * @param comment
      **/
     public void setComment(String comment) {
         i_comment = comment;
         this.markModifiedPersistentState();
     }
 
     /**
      * Setter for a database field.
      * @param idDc
      **/
     public void setIdDc(Integer idDc) {
         i_idDc = idDc;
         // Changing a primary key so we force this to new.
         this.forceNewPersistentState();
     }
 
     public void setSource(String comment) {
         Source = comment;
         // this.markModifiedPersistentState();
     }
 
     public void setEditor(String comment) {
         Editor = comment;
         // this.markModifiedPersistentState();
     }
 
     public void setTitle(String comment) {
         Title = comment;
         // this.markModifiedPersistentState();
     }
 
     public void setPublisher(String comment) {
         Publisher = comment;
         // this.markModifiedPersistentState();
     }
 
     public String getSource() {
         return Source;
     }
 
     public String getEditor() {
         return Editor;
     }
 
     public String getTitle() {
         return Title;
     }
 
     public String getPublisher() {
         return Publisher;
     }
 
     public String getUrl() {
         return url;
     }
 
    public Date getCreated() {
         return created;
     }
 
    public void setCreated(Date ts) {
        created = ts;
     }
 
     public void setUrl(String ts) {
         this.url = ts;
     }
 }
 
