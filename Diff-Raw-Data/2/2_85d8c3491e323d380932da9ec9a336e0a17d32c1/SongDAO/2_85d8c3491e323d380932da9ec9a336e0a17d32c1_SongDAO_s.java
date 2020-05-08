 package data;
 
 import data.domain.Song;
 import util.BackUp;
 import util.Preferences;
 import util.SongStorageParser;
 
 import java.io.*;
 import java.text.ParseException;
 import java.util.*;
 
 import static util.SongStorageParser.*;
 
 /**
  * User: jpipe
  * Date: 11/19/12
  *
  * Handles all reading and writing to the library.
  * "DAO" stands for Data Access Object.
  */
 public class SongDAO {
 
     public static final String LIBRARY_DIR_NAME = "songs";
     public static final String INDEX_FILE_NAME = "index.lib";
 
     public static final String STORAGE_URL = Preferences.getLibraryURL();
 
 
     private static SongDAO instance = null;
 
     /**
      * Gets the singleton instance of this class.
      */
     public static SongDAO getInstance() {
         if (instance == null) {
             instance = new SongDAO();
         }
         return instance;
     }
 
     private File library;
     private File index;
 
     //private constructor; use the getInstance() to access this singleton.
     private SongDAO() {
         library = new File(STORAGE_URL + LIBRARY_DIR_NAME);
         index = new File(STORAGE_URL + INDEX_FILE_NAME);
         library.mkdirs();
         if (!library.canRead() || !library.canWrite()) {
             throw new RuntimeException("Library is not readable!");
         }
     }
 
     /**
      * Adds a song to the library.
      *
      * @param song new {@link Song} to be written to the library
 
      * @throws LibraryConflictException if a song with the given title already exists.
      */
     public void addSong(Song song) throws IOException, LibraryConflictException {
         writeToIndex(song);
         writeToLibrary(song);
     }
 
     /**
      * Gets all titles of songs currently in the library.
      * @return ArrayList of Strings, representing each song title currently stored in the library
      */
     public List<String> getAllTitles() {
         ArrayList<String> titles = new ArrayList<String>();
         titles.addAll(Arrays.asList(library.list()));
         return titles;
     }
 
     /**
      * Gets the song with the given title.
      * @param title the song title of the desired song
      * @return {@link Song} object fully instantiated with all its relevant information.
      * @throws FileNotFoundException if the file for the associate song is not found.
      * @throws ParseException if the file could not be parsed. Probably means files were
      * corrupted
      */
     public Song getSong(String title) throws FileNotFoundException, ParseException {
         Song song = new Song();
         song.setTitle(title);
 
         Scanner scanner = getLibraryScanner(title);
 
         String metaLine = scanner.nextLine();
 
         try {
             song.addAllKeywords(SongStorageParser.extractAllKeywords(metaLine));
             song.setAuthor(SongStorageParser.extractTagDataFromString(metaLine, Tag.AUTHOR));
             song.setLyricist(SongStorageParser.extractTagDataFromString(metaLine, Tag.LYRICIST));
             song.setCopyright(SongStorageParser.extractTagDataFromString(metaLine, Tag.COPYRIGHT));
             song.setLastUsed(Long.parseLong(SongStorageParser.extractTagDataFromString(metaLine, Tag.DATE)));
 
             String lyrics = "";
             while(scanner.hasNext()) {
                 lyrics += scanner.nextLine() + "\n";
             }
 
             song.setLyrics(lyrics);
 
         } finally {
             scanner.close();
         }
 
         return song;
     }
 
     /**
      * Determines which songs have the given keyword, and returns a list of all those song's titles.
      * @param key keyword by which to filter song titles.
      * @return {@link List} of each song title corresponding to each song which has the given keyword.
      * @throws ParseException if there was an error parsing the keywords from the index (could mean index
      * is corrupt)
      */
     public List<String> getTitlesWithKeyword(String key) throws ParseException {
         ArrayList<String> titles =  new ArrayList<String>();
 
         Scanner indexScanner = getIndexScanner();
 
         try {
             while (indexScanner.hasNext()){
                 String currentTitle = indexScanner.nextLine();
                 if (SongStorageParser.extractAllKeywords(indexScanner.nextLine()).contains(key)) {
                     titles.add(currentTitle);
                 }
             }
         } finally {
             indexScanner.close();
         }
 
         return titles;
     }
 
     public List<String> getAllSongsWithAttribute(String val, Tag tag) throws ParseException {
         List<String> titles = new ArrayList<String>();
 
         Scanner indexScanner = getIndexScanner();
 
         try {
             while (indexScanner.hasNext()){
                 String currentTitle = indexScanner.nextLine();
                 if (SongStorageParser.extractTagDataFromString(indexScanner.nextLine(), tag).contains(val)) {
                     titles.add(currentTitle);
                 }
             }
         } finally {
             indexScanner.close();
         }
 
         return titles;
     }
 
     /**
      * Gets a {@link Map} of all song's {@link Song#lastUsed} to the respective song's title.
      *
      * @return {@link Map} of {@link Date} objects to Strings.
      * @throws ParseException if the index file could not be parses (may mean index is corrupt).
      */
     public Map<Date, String> getAllTitlesToLastUsedDatesMap() throws ParseException {
         Map<Date, String> titleToDate = new HashMap<Date, String>();
 
         Scanner indexScanner = getIndexScanner();
 
         try {
             while (indexScanner.hasNext()) {
                 String title = indexScanner.nextLine();
                 titleToDate.put(new Date(
                         Long.parseLong(SongStorageParser.extractTagDataFromString(indexScanner.nextLine(), Tag.DATE))
                 ), title);
             }
         } finally {
             indexScanner.close();
         }
 
         return titleToDate;
     }
 
     /**
      * Updates a song that already exists in the library.
      * @param song {@link Song} to be updated
      */
     public void updateSong(Song song) throws IOException {
         updateIndex(song, false);
         updateLibrary(song);
     }
 
     /**
      * Deletes the song with the given title from the library
      * @param title title of the song to be deleted
      * @throws IOException if there was an error deleting the file from the index or song library
      */
     public void deleteSong(String title) throws IOException {
         Song toDelete = new Song(title);
         updateIndex(toDelete, true);
         deleteStorageFile(toDelete);
     }
 
     private void writeToIndex(Song song) throws IOException {
         FileWriter writer = null;
         try {
             writer = new FileWriter(STORAGE_URL + INDEX_FILE_NAME, true);
             writer.append(toIndexString(song));
         } finally {
             closeQuietly(writer);
         }
     }
 
     private void writeToLibrary(Song song) throws IOException, LibraryConflictException {
         FileWriter writer = null;
         try {
             File newStorageFile = getStorageFileThrowIfExists(song);
 
             if (newStorageFile.createNewFile()) {
                 writer = new FileWriter(newStorageFile);
                 writer.write(toFullStorageString(song));
             } else {
                 throw new IOException("Could not create storage file: " + newStorageFile.getAbsolutePath());
             }
         } finally {
             closeQuietly(writer);
         }
     }
 
     private void updateIndex(Song song, boolean delete) throws IOException {
         File tempIndex = new File(STORAGE_URL + "tempIndex");
 
         FileWriter tempIndexWriter;
 
         tempIndexWriter = new FileWriter(tempIndex);
 
 
         Scanner indexScanner = getIndexScanner();
 
         try {
             while(indexScanner.hasNext()) {
                 String titleLine = indexScanner.nextLine();
                 if (titleLine.equalsIgnoreCase(song.getTitle())) {
                     if (!delete)
                         tempIndexWriter.append(SongStorageParser.toIndexString(song));
                     indexScanner.nextLine();
                 } else {
                     tempIndexWriter.append(titleLine).append(System.getProperty("line.separator"))
                             .append(indexScanner.nextLine()).append(System.getProperty("line.separator"));
                 }
             }
         } finally {
             closeQuietly(tempIndexWriter);
             indexScanner.close();
         }
 
         if(!index.delete()) {
             throw new IOException("Could not update Index\nFailed to delete Index.");
         }
         if(!tempIndex.renameTo(index)) {
             throw new IOException("Could not update Index\nFailed to rename Index.");
         }
 
     }
 
     private void updateLibrary(Song song) throws IOException {
         deleteStorageFile(song);
 
         try {
             writeToLibrary(song);
         } catch (LibraryConflictException e) {
             //should not happen, since the file should be deleted above
             throw new RuntimeException("Storage file for " + song.getTitle() + " still exists " +
                     "after deletion!");
         }
     }
 
     private void deleteStorageFile(Song song) throws IOException {
         if (!new File(library, song.getTitle()).delete()) {
             throw new IOException("Could not save song.\nFailed to delete old song file");
         }
     }
 
     private Scanner getIndexScanner() {
         try {
             return new Scanner(index);
         } catch (FileNotFoundException e) {
             System.err.println("Index not found!");
             throw new RuntimeException("Index not found", e);
         }
     }
 
     private Scanner getLibraryScanner(String title) throws FileNotFoundException {
         return new Scanner(new File(library, title));
     }
 
     private File getStorageFileThrowIfExists(Song song) throws LibraryConflictException {
         File newStorageFile = new File(library, song.getTitle());
         if (newStorageFile.exists()) {
             throw new LibraryConflictException(song.getTitle() + " already exists!");
         }
         return newStorageFile;
     }
 
     private void closeQuietly(Closeable io) {
         try {
             if (io != null)
                 io.close();
         } catch (IOException e) {
             System.err.println("Error closing writer");
             e.printStackTrace();
         }
     }
 
     //TODO: remove for prod
     public static void main(String[] args) {
         SongDAO dao = SongDAO.getInstance();
 
         Song song = getTestSong();
 
         try {
             dao.addSong(song);
             System.out.println("First:");
             Song storedSong = dao.getSong(song.getTitle());
             System.out.println("title: " + storedSong.getTitle());
             System.out.println("author: " + storedSong.getAuthor());
             System.out.println("copyright: " + storedSong.getCopyright());
 
             Song otherSong = new Song("other song");
             otherSong.setAuthor("other auth");
             otherSong.setLyrics("all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n" +
                     "all your base\n");
             dao.addSong(otherSong);
 
             song.setCopyright("CCLI");
             dao.updateSong(song);
 
             System.out.println("\nChanged:");
             storedSong = dao.getSong(song.getTitle());
             System.out.println("title: " + storedSong.getTitle());
             System.out.println("author: " + storedSong.getAuthor());
             System.out.println("copyright: " + storedSong.getCopyright());
 
             BackUp.BackUpAll(new File("backup"));
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
 //        deleteStuff();
 
     }
 
     public static Song getTestSong() {
         Song song = new Song();
         song.setTitle("testing");
         song.setAuthor("author");
         song.setLyricist("lyricss sdsstttrs");
         song.setCopyright("pirates");
         song.setLastUsedToNow();
         song.addKeyword("keywuuuuuuuuuuuurd", "another won");
         song.setLyrics("A thousand times I've failed\n" +
                 "Still your mercy remains\n" +
                 "And should I stumble again\n" +
                 "Still I'm caught in your grace\n" +
                 "\n" +
                 "Everlasting, Your light will shine when all else fades\n" +
                 "Never ending, Your glory goes beyond all fame\n" +
                 "\n" +
                 "My heart and my soul, I give You control\n" +
                 "Consume me from the inside out Lord\n" +
                 "Let justice and praise, become my embrace\n" +
                 "To love You from the inside out\n" +
                 "\n" +
                 "Your will above all else, my purpose remains\n" +
                 "The art of losing myself in bringing you praise\n" +
                 "\n" +
                 "Everlasting, Your light will shine when all else fades\n" +
                 "Never ending, Your glory goes beyond all fame\n" +
                 "\n" +
                 "My heart, my soul, Lord I give you control\n" +
                 "Consume me from the inside out Lord\n" +
                 "Let justice and praise become my embrace\n" +
                 "To love You from the inside out\n" +
                 "\n" +
                 "Everlasting, Your light will shine when all else fades\n" +
                 "Never ending, Your glory goes beyond all fame\n" +
                 "And the cry of my heart is to bring You praise\n" +
                 "From the inside out, O my soul cries out\n" +
                 "\n" +
                 "My Soul cries out to You\n" +
                 "My Soul cries out to You\n" +
                 "to You, to You\n" +
                 "\n" +
                 "My heart, my soul, Lord I give you control\n" +
                 "Consume me from the inside out Lord\n" +
                 "Let justice and praise become my embrace\n" +
                 "To love You from the inside out\n" +
                 "\n" +
                 "Everlasting, Your light will shine when all else fades\n" +
                 "Never ending, Your glory goes beyond all fame\n" +
                 "And the cry of my heart is to bring You praise\n" +
                 "From the inside out, O my soul cries out\n" +
                 "\n" +
                 "Everlasting, Your light will shine when all else fades\n" +
                 "Never ending, Your glory goes beyond all fame\n" +
                 "And the cry of my heart is to bring You praise\n" +
                 "From the inside out, O my soul cries out\n" +
                 "From the inside out, O my soul cries out\n" +
                 "From the inside out, O my soul cries out.");
         return song;
     }
 
     public static void deleteStuff() {
         File lib = new File(STORAGE_URL + LIBRARY_DIR_NAME);
 
         for (String title: instance.getAllTitles()) {
             if(!new File(lib, title).delete())
                 System.err.println("\""+title + "\" storage not deleted!");
         }
 
         if(!lib.delete())
             System.err.println("lib not deleted!");
 
         if(!new File(STORAGE_URL + INDEX_FILE_NAME).delete())
             System.err.println("index not deleted!");
 
         if(!new File(STORAGE_URL).delete())
             System.err.println("test storage dir not deleted!");
     }
 }
