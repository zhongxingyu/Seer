 /**
  * Kuebiko - Note.java
  * Copyright 2011 Dave Huffman (daveh303 at yahoo dot com).
  * TODO license info.
  */
 
 package dmh.kuebiko.model;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 /**
  * Value object representing a note.
  *
  * @author davehuffman
  */
 public class Note implements Serializable {
     private static final long serialVersionUID = 1L;
     
     public enum State {
         CLEAN, DELETED, DIRTY, NEW; 
     }
 
     private final int id;
     
     private State state;
     
     private String title;
     private String text;
     private Date createDate;
     private Date modifiedDate;
     
     private List<String> tags = Lists.newArrayList();
     
     /**
      * Create a new, unsaved note with default values.
      */
     public Note() {
         this(0, State.NEW);
         assignDefaults();
     }
     
     Note(int id, String title, String text, Date createDate, Date modifiedDate) {
         this(id, State.CLEAN);
         
         this.title = title;
         this.text = text;
         this.createDate = createDate;
         this.modifiedDate = modifiedDate;
     }
     
     /**
      * Copy constructor.
      * @param id The ID of the new note. 
      * @param source The note to copy.
      */
     Note(int id, Note source) {
         this(id, source.getTitle(), source.getText(), source.getCreateDate(), 
                 source.getModifiedDate());
     }
 
     private Note(int id, State state) {
         this.id = id;
         this.state = state;
     }
     
     private void assignDefaults() {
         this.createDate = new Date();
     }
     
     /**
      * Reset the dirty flag on this note, which signifies that it's data is 
      * consistent with the data store. This method should only be called from 
      * the model layer.
      */
     void reset() {
         state = State.CLEAN;
     }
 
     public boolean isNew() {
         return (id == 0 || state == State.NEW);
     }
     
     public boolean isDirty() {
         return state == State.DIRTY;
     }
 
     @Override
     public String toString() {
         return "Note [state=" + state + ", id=" + id + ", title=" + title
                 + ", text=" + text + ", createDate=" + createDate
                 + ", modifiedDate=" + modifiedDate + ", tags=" + tags + "]";
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((createDate == null) ? 0 : createDate.hashCode());
         result = prime * result + id;
         result = prime * result
                 + ((modifiedDate == null) ? 0 : modifiedDate.hashCode());
         result = prime * result + ((state == null) ? 0 : state.hashCode());
         result = prime * result + ((tags == null) ? 0 : tags.hashCode());
         result = prime * result + ((text == null) ? 0 : text.hashCode());
         result = prime * result + ((title == null) ? 0 : title.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Note other = (Note) obj;
         if (createDate == null) {
             if (other.createDate != null)
                 return false;
         } else if (!createDate.equals(other.createDate))
             return false;
         if (id != other.id)
             return false;
         if (modifiedDate == null) {
             if (other.modifiedDate != null)
                 return false;
         } else if (!modifiedDate.equals(other.modifiedDate))
             return false;
         if (state != other.state)
             return false;
         if (tags == null) {
             if (other.tags != null)
                 return false;
         } else if (!tags.equals(other.tags))
             return false;
         if (text == null) {
             if (other.text != null)
                 return false;
         } else if (!text.equals(other.text))
             return false;
         if (title == null) {
             if (other.title != null)
                 return false;
         } else if (!title.equals(other.title))
             return false;
         return true;
     }
     
     public State getState() {
         return state;
     }
 
     public int getId() {
         return id;
     }
     
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
        state = State.DIRTY;
         this.title = title;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
        state = State.DIRTY;
         this.text = text;
     }
 
     public Date getCreateDate() {
         return createDate;
     }
     
     void setCreateDate(Date createDate) {
        state = State.DIRTY;
         this.createDate = createDate;
     }
 
     public Date getModifiedDate() {
         return modifiedDate;
     }
 
     void setModifiedDate(Date modifiedDate) {
        state = State.DIRTY;
         this.modifiedDate = modifiedDate;
     }
     
     public List<String> getTags() {
         return Collections.unmodifiableList(tags);
     }
     
     public void setTags(List<String> tags) {
        state = State.DIRTY;
         this.tags = tags;
     }
 }
