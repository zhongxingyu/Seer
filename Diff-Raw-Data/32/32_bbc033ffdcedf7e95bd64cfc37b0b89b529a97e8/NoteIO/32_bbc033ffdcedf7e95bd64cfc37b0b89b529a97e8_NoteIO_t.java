 /*
  * Copyright 2012 - 2014 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.powersurgepub.psdatalib.notenik;
 
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psdatalib.pstags.*;
   import com.powersurgepub.psutils.*;
   import java.io.*;
   import java.util.*;
 
 /**
  A class to perform input and output for note files. 
 
  @author Herb Bowie
  */
 public class NoteIO {
   
   public static final String              PARMS_TITLE         = "Collection Parms";
   public static final String              FILE_EXT            = ".txt";
   
   private             File                homeFolder          = null;
   private             File                altFolder           = null;
   private             NoteList            list                = null;
   private             int                 notesLoaded         = 0;
   
   private             DataDictionary      dict = null;
   private             RecordDefinition    recDef = null;
   
   private             ArrayList<DirToExplode> dirList;
   private             int							    dirNumber = 0;
   
   private             File                currDirAsFile = null;
   private             int                 currDirDepth = 0;
   
   private             ArrayList<String>   dirEntries;
   private             int							    entryNumber = 0;
   
   private             File                dirEntryFile = null;
   private             Note                nextNote = null;
   
   private             BufferedReader      inBuffered;
   private             BufferedWriter      outBuffered;
   
   private             int                 ioStyle = IO_STYLE_UNDETERMINED;
   public static final int IO_STYLE_UNDETERMINED   = -1;
   public static final int IO_EXPLICIT             = 0;
   public static final int IO_IMPLICIT             = 1;
   public static final int IO_IMPLICIT_UNDERLINES  = 2;
   public static final int IO_IMPLICIT_FILENAME    = 3;
   
   public NoteIO (RecordDefinition recDef, File folder) {
     this.recDef = recDef;
     dict = recDef.getDict();
     this.homeFolder = folder;
   } 
   
   public void setHomeFolder (File homeFolder) {
     this.homeFolder = homeFolder;
   }
   
   /* =======================================================================
    * 
    * This section of the class contains input routines. 
    *
    * ======================================================================= */
   
   public void load (NoteList list) 
       throws IOException {
     
     notesLoaded = 0;
     this.list = list;
     openForInput();
     Note note = readNextNote();
     while (note != null) {
       list.add(note);
       notesLoaded++;
       note = readNextNote();
     }
     close();
 
     Logger.getShared().recordEvent(LogEvent.NORMAL, 
         String.valueOf(notesLoaded) + " Notes loaded", false);
   }
   
   public int getNotesLoaded() {
     return notesLoaded;
   }
   
   public void openForInput () 
       throws IOException {
     
     dict = new DataDictionary();
     recDef = new RecordDefinition(dict);
     recDef.addColumn(Note.TITLE_DEF);
     recDef.addColumn(Note.TAGS_DEF);
     recDef.addColumn(Note.LINK_DEF);
     recDef.addColumn(Note.BODY_DEF);
     notesLoaded = 0;
     dirList = new ArrayList();
     dirList.add (new DirToExplode (1, homeFolder.getAbsolutePath()));
     dirNumber = -1;
     dirEntryFile = null;
     nextNote = null;
     entryNumber = 0;
     dirEntries = new ArrayList<String>();
     nextNote();
 
   }
   
   /**
    Read the next note.
   
    @return Next note, or null if no more notes left. 
   
    @throws IOException 
   */
   public Note readNextNote() 
       throws IOException {
     
     Note noteToReturn = nextNote;
     nextNote();
     return noteToReturn;
   }
   
   public void close() {
     dirEntryFile = null;
     nextNote = null;
   }
   
   private void nextNote() 
       throws IOException {
     nextNote = null;
     while (nextNote == null && dirNumber < dirList.size()) {
       nextDirEntry();
     }
   }
   
   private void nextDirEntry () 
       throws IOException {
     
     if (entryNumber >= 0 && entryNumber < dirEntries.size()) {
       String nextDirEntry = dirEntries.get (entryNumber);
       dirEntryFile = new File (currDirAsFile, nextDirEntry);
       if (dirEntryFile.isDirectory()) {
         if (nextDirEntry.equalsIgnoreCase("templates")
             || nextDirEntry.equalsIgnoreCase("publish")) {
           // skip
         } else {
           DirToExplode newDirToExplode = new DirToExplode 
               (currDirDepth + 1, dirEntryFile.getAbsolutePath());
           dirList.add (newDirToExplode);
         }
       } 
       else
       if (isInterestedIn (dirEntryFile)) {
         nextNote = getNote(dirEntryFile);
         if (nextNote == null) {
           // System.out.println("  - No note built");
         }
       } else {
         // System.out.println("  - not interested");
       }
       entryNumber++;
     } else {
       dirNumber++;
       nextDirectory();
     }
   }
   
   /**
    Let's explode the next directory, if we have any more.
   */
   private void nextDirectory() 
       throws IOException {
 
     if (dirNumber >= 0 && dirNumber < dirList.size()) {
       DirToExplode currDir = dirList.get(dirNumber);
       currDirAsFile = new File (currDir.path);
       currDirDepth = currDir.depth;
       String[] dirEntry = currDirAsFile.list();
       if (dirEntry != null) {
         dirEntries = new ArrayList (Arrays.asList(dirEntry));
       }
       entryNumber = 0;
     }
   }
   
   /**
    Is this input module interested in processing the specified file?
   
    @param candidate The file being considered. 
   
    @return True if the input module thinks this file is worth processing,
            otherwise false. 
   */
   public static boolean isInterestedIn(File candidate) {
     if (candidate.isHidden()) {
       return false;
     }
     else
     if (candidate.getName().startsWith(".")) {
       return false;
     }
     else
     if (! candidate.canRead()) {
       return false;
     }
     else
     if (candidate.isFile() 
         && candidate.length() == 0
         && candidate.getName().equals("Icon\r")) {
       return false;
     }
     else
     if (candidate.isDirectory()) {
       return false;
     }
     else
     if (candidate.getParent().endsWith("templates")) {
       return false;
     }
     else
     if (candidate.getName().equalsIgnoreCase("New Event.txt")) {
       return false;
     }
     else
     if (candidate.getName().contains("conflicted copy")) {
       return false;
     }
     else
     if (candidate.getName().contains(PARMS_TITLE)) {
       return false;
     }
     else
     if (candidate.getName().endsWith (".txt")) {
       return true;
     } else {
       return false;
     }
   }
   
   public Note getNote(String fileName) 
       throws IOException, FileNotFoundException {
     return getNote(getFile(fileName));
   }
   
   /**
    Read one note from disk and return it as a note object. 
   
    @param noteFile The file containing the note on disk. 
    @return A Note object. 
    @throws IOException
    @throws FileNotFoundException 
   */
   public Note getNote(File noteFile) 
       throws IOException, FileNotFoundException {
     
     Note note = null;
     
     if (noteFile.exists()
         && noteFile.canRead()
         && noteFile.isFile()) {
       
       FileName noteFileName = new FileName(noteFile);
       String fileNameIn = noteFileName.getBase();
       note = new Note(recDef);
       note.setDiskLocation(noteFile);
       
       // Use the file name (minus the path and extension) as the default title
       note.setTitle(fileNameIn);
       
       // Set the last modified date
       Date lastModDate = new Date(noteFile.lastModified());
       note.setLastModDate(lastModDate);
       
       // Get ready to read the text file
       FileInputStream fileInputStream = new FileInputStream(noteFile);
       InputStreamReader inReader = new InputStreamReader (fileInputStream);
       inBuffered = new BufferedReader (inReader);
       boolean bodyStarted = false;
       String line = inBuffered.readLine();
       int lineCount = 0;
       
       // For each line in the file
       while (line != null) {
         lineCount++;
        // Find the beginning and end of the data on the line
        int start = 0;
        while (start < line.length() 
            && Character.isWhitespace(line.charAt(start))) {
          start++;
        }
        int end = line.length();
        while (end > 0 
            && Character.isWhitespace(line.charAt(end - 1))) {
          end--;
        }
        
         if (bodyStarted) {
          if (note.hasBody() || start < end) {
            // Once we've started the body, assume the rest is all body
            note.appendLineToBody(line);
          }
         } else {
           
           // Haven't started the body yet -- look for metadata
           

           
           // See if the line underlines a heading
           char underlineChar = ' ';
           int underlineCount = 0;
           boolean underlines = false;
           if (start < end
               && (line.charAt(start) == '-' || line.charAt(start) == '=')) {
             underlineChar = line.charAt(start);
             underlines = true;
             underlineCount = 1;
             while (underlines && (start + underlineCount) < end) {
               if (line.charAt(start + underlineCount) == underlineChar) {
                 underlineCount++;
               } else {
                 underlines = false;
               }
             } // end while finding more underline characters
           }
           
           // If the line contains a colon, look for field name and value
           int colonPosition = line.indexOf(":", start);
           if (colonPosition >= 0) {
             if ((end - colonPosition) >= 2
                 && (! Character.isWhitespace(line.charAt(colonPosition + 1)))) {
               colonPosition = -1;
             }
           }
           String fieldName = "";
           
           // See if the line contains a level 1 heading
           if ((start + 2) < end
               && line.charAt(start) == '#'
               && line.charAt(start + 1) == ' '
               && lineCount == 1
               && fileNameIn.equals 
                 (StringUtils.makeReadableFileName(line.substring(start + 2, end)))) {
             note.setTitle(line.substring(start + 2, end));
             if (ioStyle == IO_STYLE_UNDETERMINED) {
               ioStyle = IO_IMPLICIT;
             }
           }
           else
           if (colonPosition > 0) {
             int fieldNameEnd = colonPosition;
             while (fieldNameEnd > start 
                 && Character.isWhitespace(line.charAt(fieldNameEnd - 1))) {
               fieldNameEnd--;
             }
             fieldName = StringUtils.commonName
                 (line.substring(start, fieldNameEnd));
             int fieldValueStart = colonPosition + 1;
             while (fieldValueStart < end
                 && Character.isWhitespace(line.charAt(fieldValueStart))) {
               fieldValueStart++;
             }
             if (fieldName.equals(Note.TITLE_COMMON_NAME)) {
               note.setTitle(line.substring(fieldValueStart, end));
             }
             else
             if (fieldName.equals(Note.LINK_COMMON_NAME)) {
               note.setLink(line.substring(fieldValueStart, end));
             }
             else
             if (fieldName.equals(Note.TAGS_COMMON_NAME)) {
               note.setTags(line.substring(fieldValueStart, end));
             }
             else
             if (fieldName.equals(Note.BODY_COMMON_NAME)) {
               note.setBody(line.substring(fieldValueStart, end));
               bodyStarted = true;
             // }
             // else 
             // if (fieldName.length() > 0) {
             //   note.storeField
             //       (line.substring(start, fieldNameEnd), 
             //        line.substring(fieldValueStart, end));
             } else {
               note.appendLineToBody(line);
               bodyStarted = true;
             }
           } // end if colon found on line
           else
           if (start >= end) {
             // If the line is blank, then just ignore it
           }
           else
           if (underlines) {
             // If the line underlines a heading, then just ignore it
           }
           else
           if (lineCount == 1
               && fileNameIn.equals 
                 (StringUtils.makeReadableFileName(line.substring(start, end)))) {
             note.setTitle(line.substring(start, end));
           }
           else
           if (line.substring(start, end).startsWith("http://")
               || line.substring(start, end).startsWith("https://")
               || line.substring(start, end).startsWith("mailto:")) {
             note.setLink(line.substring(start, end));
           } else {
             note.appendLineToBody(line);
           }
           
         } // end if not body started
         line = inBuffered.readLine();
       }
       inBuffered.close();
     }
     
     return note;
   }
   
   public void  save (NoteList noteList) 
       throws IOException {
     
     save (homeFolder, noteList, true);
     
   }
   
   /* =======================================================================
    * 
    * This section of the class contains output routines. 
    *
    * ======================================================================= */
  
   public void save (File folder, NoteList noteList, boolean primaryLocation) 
       throws IOException {
     for (int i = 0; i < noteList.size(); i++) {
       Note nextNote = noteList.get(i);
       save (folder, nextNote, primaryLocation);
     }
   } // end method save
   
   public void save (Note note, boolean primaryLocation) 
       throws IOException {
     save (homeFolder, note, primaryLocation);
   }
  
   public void save (File folder, Note note, boolean primaryLocation) 
       throws IOException {
  
     File file = getFile(folder, note);
     // System.out.println ("NoteWriter.save to " + file.toString());
     openOutput (file);
     String oldDiskLocation = note.getDiskLocation();
     saveOneItem (note);
     if (primaryLocation) {
       note.setDiskLocation (file);
     }
     closeOutput();
  
   }
  
   /**
    Open the output writer.
  
    @param outFile The file to be opened.
  
   */
   private void openOutput (File outFile) 
       throws IOException {
 
     FileOutputStream outStream = new FileOutputStream (outFile);
     OutputStreamWriter outWriter = new OutputStreamWriter (outStream, "UTF-8");
     outBuffered = new BufferedWriter (outWriter);
   }
  
   private void saveOneItem (Note note) 
       throws IOException {
     for (int i = 0; i < note.getNumberOfFields(); i++) {
       DataField nextField = note.getField(i);
       if (nextField != null) {
         writeFieldName (nextField.getProperName());
         if (nextField.getCommonFormOfName().equals("body")
             || nextField.getCommonFormOfName().equals("comments")) {
           writeLine("");
           writeLine(" ");
         }
         writeFieldValue (nextField.getData());
         writeLine("");
       }
     }
 
   } // end of method saveOneItem
  
   private void writeFieldName (String fieldName) 
       throws IOException {
     write(fieldName);
     write(": ");
     for (int i = fieldName.length(); i < 6; i++) {
       write (" ");
     }
   }
  
   private void writeFieldValue (String fieldValue) 
       throws IOException {
     writeLine (fieldValue);
   }
  
   private void writeLine (String s) 
       throws IOException {
     outBuffered.write (s);
     outBuffered.newLine();
   }
  
   private void write (String s) 
       throws IOException {
     outBuffered.write (s);
   }
  
   /**
    Close the output writer.
  
    @return True if close worked ok.
   */
   public void closeOutput() 
       throws IOException {
     outBuffered.close();
   }
   
   /* =======================================================================
    * 
    * This section of the class contains other file-related routines. 
    *
    * ======================================================================= */
   
   /**
    Does the given note exist on disk?
   
    @param note The note to be evaluated. 
   
    @return True if a disk file is found, false otherwise. 
   */
   public boolean exists (Note note) {
     return getFile(homeFolder, note).exists();
   }
 
    /**
    Does the given Note already exist on disk?
  
    @param folder    The folder in which the item is to be stored.
    @param Note      The Note to be stored.
  
    @return True if a disk file with the same path already exists,
            false if not.
    */
   public boolean exists (File folder, Note note) {
     return getFile(folder, note).exists();
   }
  
   /**
    Does the given Note already exist on disk?
  
    @param folder    The folder in which the item is to be stored.
    @param localPath The local path (folder plus file name) for the
                     Note to be stored.
  
    @return True if a disk file with the same path already exists,
            false if not.
    */
   public boolean exists (File folder, String localPath) {
     return getFile(folder, localPath).exists();
   }
   
   public boolean exists (String localPath) {
     return getFile(homeFolder, localPath).exists();
   }
   
   public boolean delete (Note note) {
     return getFile(homeFolder, note).delete();
   }
  
   /**
    Delete the passed note from disk.
  
    @param folder    The folder in which the note is to be stored.
    @param Note      The Note to be stored.
  
    @return True if the file was deleted successfully,
            false if not.
    */
   public boolean delete (File folder, Note Note) {
     return getFile(folder, Note).delete();
   }
  
   /**
    Delete the passed note from disk.
  
    @param folder    The folder in which the item is to be stored.
    @param localPath The local path (folder plus file name) for the
                     Note to be stored.
  
    @return True if the file was deleted successfully,
            false if not.
    */
   public boolean delete (File folder, String localPath) {
     return getFile(folder, localPath).delete();
   }
   
   public File getFile (Note note) {
     return new File (homeFolder, note.getFileName() + FILE_EXT);
   }
   
   public File getFile(String localPath) {
 
     return getFile (homeFolder, localPath);
 
   }
  
   /**
    Return a standard File object representing the note's stored location on disk.
  
    @param folder  The folder in which the item is to be stored.
    @param Note    The Note to be stored.
  
    @return The File pointing to the intended disk location for the given note.
    */
   public File getFile (File folder, Note note) {
     return new File (folder, note.getFileName() + FILE_EXT);
   }
  
   /**
    Return a standard File object representing the item's stored location on disk.
  
    @param folder    The folder in which the item is to be stored.
    @param localPath The local path (folder plus file name) for the
                     Note to be stored.
  
    @return The File pointing to the intended disk location for the given item.
    */
   public File getFile (File folder, String localPath) {
     StringBuilder completePath = new StringBuilder();
     try {
       completePath = new StringBuilder (folder.getCanonicalPath());
     } catch (Exception e) {
       completePath = new StringBuilder (folder.getAbsolutePath());
     }
     completePath.append('/');
     completePath.append(localPath);
     completePath.append(FILE_EXT);
     return new File (completePath.toString());
   }
   
   /**
      Inner class to define a directory to be processed.
    */
   class DirToExplode {
     int 		depth = 0;
     String	path  = "";
     
     DirToExplode (int depth, String path) {
       this.depth = depth;
       this.path = path;
     } // DirToExplode constructor
   } // end DirToExplode inner class
 
 }
