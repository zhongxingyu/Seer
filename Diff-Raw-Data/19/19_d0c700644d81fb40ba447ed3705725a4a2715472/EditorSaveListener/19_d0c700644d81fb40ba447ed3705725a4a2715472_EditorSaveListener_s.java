 package de.cismet.cids.editors;
 
 /**
  *
  * @author stefan
  */
 public interface EditorSaveListener {
 
     public enum EditorSaveStatus {
 
         CANCELED, SAVE_SUCCESS, SAVE_ERROR
     }
 
     void editorClosed(EditorSaveStatus status);
    void prepareForSave();
 }
