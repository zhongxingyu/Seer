 /**
  * Kuebiko - NoteManager.java
  * Copyright 2013 Dave Huffman (dave dot huffman at me dot com).
  * Open source under the BSD 3-Clause License.
  */
 
 package dmh.kuebiko.controller;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Observable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 import dmh.kuebiko.model.Note;
 import dmh.kuebiko.model.Note.State;
 import dmh.kuebiko.model.NoteDao;
 import dmh.kuebiko.model.PersistenceException;
 import dmh.kuebiko.model.ValidationException;
 import dmh.kuebiko.util.NoteTitleFunction;
 
 /**
  * Management class for notes. Acts as the note controller.
  *
  * @author davehuffman
  */
 public class NoteManager extends Observable {
     private static final Logger log = Logger.getLogger(NoteManager.class);
 
     static final String DEFAULT_NOTE_TITLE = "Untitled Note";
 
     private final NoteDao noteDao;
 
     private List<Note> notes = null;
     private final Collection<Note> deletedNotes;
     private boolean unsavedChanges = false;
 
     /**
      * Constructor.
      * @param noteDao The DAO to use for note persistence.
      */
     public NoteManager(NoteDao noteDao) {
         this.noteDao = noteDao;
 
         deletedNotes = Lists.newArrayList();
         loadAllNotes();
     }
 
     private void loadAllNotes() {
         try {
             notes = Lists.newArrayList(noteDao.readNotes());
             setUnsavedChangesAndNotify(false);
         } catch (PersistenceException e) {
             throw new DataStoreException("Could not read notes.", e);
         }
     }
 
     /**
      * @return An immutable view of all notes in the stack.
      */
     public List<Note> getNotes() {
         return Collections.unmodifiableList(notes);
     }
 
     /**
      * @return A view containing the titles for all notes in the stack.
      */
     public List<String> getNoteTitles() {
         return Lists.transform(notes, NoteTitleFunction.getInstance());
     }
 
     /**
      * @return True if there are no notes.
      */
     public boolean isEmpty() {
         return notes.isEmpty();
     }
 
     public boolean doesNoteExist(String title) {
         return getNoteTitles().contains(title);
     }
 
     public int getNoteCount() {
         return notes.size();
     }
 
     public Note getNoteAt(int index) {
         return notes.get(index);
     }
 
     /**
      * Determine if there are changes that have not been saved to the data store.
      * @return True if there are unsaved changes.
      */
     public boolean hasUnsavedChanges() {
         // First check the cached value.
         if (unsavedChanges) {
             return unsavedChanges;
         }
         // A false value does not necessarily indicate to changes are present;
         // we need to check each note to determine if it has been updated.
         for (Note note: notes) {
             if (note.getState() == State.DIRTY) {
                 unsavedChanges = true;
                 setChanged();
                 break;
             }
         }
         return unsavedChanges;
     }
 
     /**
      * Flip the switch indicating if there are unsaved changes to the notes.
      * @param unsavedChanges True if there are unsaved notes.
      */
     private void setUnsavedChangesAndNotify(boolean unsavedChanges) {
         log.debug(String.format("setUnsavedChangesAndNotify(%s).", unsavedChanges));
 
         final boolean prevValue = this.unsavedChanges;
         this.unsavedChanges = unsavedChanges;
         final boolean currentValue = hasUnsavedChanges();
 
         // Notify observers if the new value is false or if the value has changed.
         if (currentValue == false || prevValue != currentValue) {
             setChanged();
             notifyObservers(currentValue);
         }
     }
 
     /**
      * Add a new note with a default title.
      * @return The title of the new note.
      */
     public String addNewNote() {
         final String title = genUniqueNoteTitle();
         addNewNote(title);
         return title;
     }
 
     /**
      * Generate a unique note title for a new note.
      * @return A note title that is guaranteed to be unique among all notes
      *         in the stack.
      */
     String genUniqueNoteTitle() {
         // Get a list of default note titles.
         final Collection<String> defaultTitles =
                 Collections2.filter(getNoteTitles(),
                         new Predicate<String>() {
                             @Override
                             public boolean apply(String input) {
                                 return input.startsWith(DEFAULT_NOTE_TITLE);
                             }
                         });
 
         if (defaultTitles.isEmpty()) {
             return DEFAULT_NOTE_TITLE;
         }
 
         // Default note titles exist; work thought the list and create a new,
         // unique default note title.
         Pattern titleRegex = Pattern.compile(DEFAULT_NOTE_TITLE + " (\\d+)");
         int maxSuffix = 1;
         for (String title: defaultTitles) {
             Matcher matcher = titleRegex.matcher(title);
             if (matcher.matches()) {
                 int suffix = Integer.parseInt(matcher.group(1));
                 if (suffix > maxSuffix) {
                     maxSuffix = suffix;
                 }
             }
         }
         String title = String.format("%s %d", DEFAULT_NOTE_TITLE, maxSuffix + 1);
         return title;
     }
 
     public void addNewNote(String title) {
         Note note = new Note();
         note.setTitle(title);
         addNote(note);
     }
 
     void addNote(Note newNote) {
         notes.add(newNote);
         setUnsavedChangesAndNotify(true);
     }
 
     public void deleteNote(Note note) {
         if (!notes.remove(note)) {
             throw new IllegalArgumentException(String.format(
                     "Note [%s] does not exist.", note));
         }
         deletedNotes.add(note);
         setUnsavedChangesAndNotify(true);
     }
 
     /**
      * Save any changes made to the notes.
      */
     public void saveAll() {
		log.debug("saveAll().");
         try {
             for (Note note: deletedNotes) {
                 log.debug(String.format("Deleting note [%s].", note));
                 if (!note.isNew()) {
                     // Only previously saved notes need to be deleted.
                     noteDao.deleteNote(note);
                 }
             }
             deletedNotes.clear();
 
             for (Note note: notes) {
                 log.debug(String.format("Saving note [%s].", note));
                 switch (note.getState()) {
                 case DIRTY:
                     noteDao.updateNote(note);
                     break;
                 case NEW:
                     noteDao.addNote(note);
                     break;
                 default:
                     continue;
                 }
             }
            loadAllNotes();
         } catch (PersistenceException e) {
             throw new DataStoreException("Could not read/write notes.", e);
         } catch (ValidationException e) {
             throw new DataStoreException("Invalid note.", e);
         }
     }
 }
