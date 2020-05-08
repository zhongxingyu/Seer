 package edu.mines.alterego;
 
 class NotesData {
     int mNoteId;
     String mSubject;
     String mDescription;
 
     NotesData(int notes_id, String subject, String description) {
         mNoteId = notes_id;
         mSubject = subject;
         mDescription = description;
     }
 
     public int getNoteId() { return mNoteId; }
     public String getSubject() { return mSubject; }
     public String getDescription() { return mDescription; }
   
 }
