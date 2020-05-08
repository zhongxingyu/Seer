 package edu.mines.alterego;
 
 class NotesData {
     int mNoteId;
     String mSubject;
     String mDescription;
    public static int showableDescLength = 25;
    
 
     NotesData(int notes_id, String subject, String description) {
         mNoteId = notes_id;
         mSubject = subject;
         mDescription = description;
     }
 
     public int getNoteId() { return mNoteId; }
     public String getSubject() { return mSubject; }
     public String getDescription() { return mDescription; }
 
    @Override
    public String toString() {
        int cutIndex = showableDescLength;
        if (mDescription.length() <= cutIndex)
            cutIndex = mDescription.length();
        return mSubject + ": " + mDescription.substring(0,cutIndex);
    }
 }
