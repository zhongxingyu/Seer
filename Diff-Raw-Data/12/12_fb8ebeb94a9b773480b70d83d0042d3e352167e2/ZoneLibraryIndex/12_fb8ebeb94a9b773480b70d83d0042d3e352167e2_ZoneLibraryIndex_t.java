 /*
  * a singleton class for managing and allowing access to the zone library search index
  * only indexes one copy of media with a certain filename
  *
  * support for grabbing ID3 metadata on MP3 files only
  *
  * NOTE: recursive symlinks in path to index kills this
  */
 package zonecontrol;
 
 import audio.MediaPlayerImpl;
 import contrib.ID3MetaData;
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jcifs.smb.SmbException;
 import jcifs.smb.SmbFile;
 import musiczones.MusicZones;
 import netutil.CIFSNetworkInterface;
 import netutil.IpAddressType;
 import netutil.Layer3Info;
 import org.blinkenlights.jid3.ID3Exception;
 
 /**
  * @author Jason Zerbe
  */
 public class ZoneLibraryIndex {
 
     private static ZoneLibraryIndex zli_SingleInstance = null;
     protected HashMap<String, String> zli_FileMap = null; //<filename, full file path>
     protected TreeMap<String, LinkedList<String>> zli_TitleMap = null; //<title, LinkedList<filenames>>
     protected TreeMap<String, LinkedList<String>> zli_GenreMap = null; //<genre, LinkedList<filenames>>
     protected TreeMap<String, LinkedList<String>> zli_AlbumMap = null; //<album, LinkedList<filenames>>
     protected TreeMap<String, LinkedList<String>> zli_ArtistMap = null; //<artist, LinkedList<filenames>>
     protected Timer zli_Timer = null;
     protected boolean debugEventsOn = false;
     private RefreshCIFSMediaTask zli_RCMT = null;
     private boolean zli_isIndexScheduled = false;
     protected int zli_RefreshCIFSMediaSeconds = 3600; //60 minutes
     protected String[] zli_CIFSPathBlackListArray = {"$"};
     protected int zli_IPv4ScanMin = 1;
     protected int zli_IPv4ScanMax = 10;
     private boolean zli_isBuilding = false;
 
     protected ZoneLibraryIndex(boolean theDebugIsOn) {
         debugEventsOn = theDebugIsOn;
         zli_FileMap = new HashMap<String, String>();
         zli_TitleMap = new TreeMap<String, LinkedList<String>>();
         zli_GenreMap = new TreeMap<String, LinkedList<String>>();
         zli_AlbumMap = new TreeMap<String, LinkedList<String>>();
         zli_ArtistMap = new TreeMap<String, LinkedList<String>>();
         zli_Timer = new Timer();
         zli_RCMT = new RefreshCIFSMediaTask();
         addIndexBuild();
     }
 
     public static ZoneLibraryIndex getInstance() {
         if (zli_SingleInstance == null) {
             zli_SingleInstance = new ZoneLibraryIndex(false);
         }
         return zli_SingleInstance;
     }
 
     public static ZoneLibraryIndex getInstance(boolean theDebugIsOn) {
         if (zli_SingleInstance == null) {
             zli_SingleInstance = new ZoneLibraryIndex(theDebugIsOn);
         }
         return zli_SingleInstance;
     }
 
     public void setScanMin(int theScanMin) {
         zli_IPv4ScanMin = theScanMin;
     }
 
     public void setScanMax(int theScanMax) {
         zli_IPv4ScanMax = theScanMax;
     }
     
     public void addIndexBuild() {
     	if (!zli_isIndexScheduled) {
     		zli_Timer.schedule(zli_RCMT, 0, zli_RefreshCIFSMediaSeconds * 1000);
     		zli_isIndexScheduled = true;
     		System.out.println("ZLI RefreshCIFSMediaTask added");
     	}
     }
     
     public void removeIndexBuild() {
     	zli_Timer.cancel();
     	zli_isIndexScheduled = false;
     }
 
     public void manualRebuildIndex() {
         zli_RCMT.run();
     }
 
     public boolean getIndexIsBuilding() {
         return zli_isBuilding;
     }
 
     protected void setIndexIsBuilding(boolean theIndexIsBuilding) {
         zli_isBuilding = theIndexIsBuilding;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * of all genres with available files
      * @return TreeMap<String - genre, LinkedList<String - filename>>
      */
     public TreeMap<String, LinkedList<String>> getGenreMap() {
         TreeMap<String, LinkedList<String>> returnFileMap = new TreeMap<String, LinkedList<String>>();
         for (Entry<String, LinkedList<String>> aTempEntry : zli_GenreMap.entrySet()) {
             for (String aTempFileName : aTempEntry.getValue()) {
                 if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                     if (returnFileMap.containsKey(aTempEntry.getKey())) {
                         if (returnFileMap.get(aTempEntry.getKey()) == null) {
                             LinkedList<String> aFileNameLL = new LinkedList<String>();
                             aFileNameLL.add(aTempFileName);
                             returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                         } else {
                             returnFileMap.get(aTempEntry.getKey()).add(aTempFileName);
                         }
                     } else {
                         LinkedList<String> aFileNameLL = new LinkedList<String>();
                         aFileNameLL.add(aTempFileName);
                         returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                     }
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * that contains the song title (or fails to filename)
      * and the complete path information from the genre
      * @param theGenre String
      * @return TreeMap<String - title, String - file path>
      */
     public TreeMap<String, String> getTitlesFromGenre(String theGenre) {
         TreeMap<String, String> returnFileMap = new TreeMap<String, String>();
         LinkedList<String> aTempFileNameList = zli_GenreMap.get(theGenre);
         for (String aTempFileName : aTempFileNameList) {
             if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                 String aTempTitle = getTitleFromFileName(aTempFileName);
                 if (aTempTitle == null) {
                     aTempTitle = aTempFileName;
                 }
                 if (!returnFileMap.containsKey(aTempTitle)) {
                     returnFileMap.put(aTempTitle, zli_FileMap.get(aTempFileName));
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * of all albums with available files
      * @return TreeMap<String - album, LinkedList<String - filename>>
      */
     public TreeMap<String, LinkedList<String>> getAlbumMap() {
         TreeMap<String, LinkedList<String>> returnFileMap = new TreeMap<String, LinkedList<String>>();
         for (Entry<String, LinkedList<String>> aTempEntry : zli_AlbumMap.entrySet()) {
             for (String aTempFileName : aTempEntry.getValue()) {
                 if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                     if (returnFileMap.containsKey(aTempEntry.getKey())) {
                         if (returnFileMap.get(aTempEntry.getKey()) == null) {
                             LinkedList<String> aFileNameLL = new LinkedList<String>();
                             aFileNameLL.add(aTempFileName);
                             returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                         } else {
                             returnFileMap.get(aTempEntry.getKey()).add(aTempFileName);
                         }
                     } else {
                         LinkedList<String> aFileNameLL = new LinkedList<String>();
                         aFileNameLL.add(aTempFileName);
                         returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                     }
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * that contains the song title (or fails to filename)
      * and the complete path information from the album name
      * @param theAlbumName String
      * @return TreeMap<String - title, String - file path>
      */
     public TreeMap<String, String> getTitlesFromAlbum(String theAlbumName) {
         TreeMap<String, String> returnFileMap = new TreeMap<String, String>();
         LinkedList<String> aTempFileNameList = zli_AlbumMap.get(theAlbumName);
         for (String aTempFileName : aTempFileNameList) {
             if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                 String aTempTitle = getTitleFromFileName(aTempFileName);
                 if (aTempTitle == null) {
                     aTempTitle = aTempFileName;
                 }
                 if (!returnFileMap.containsKey(aTempTitle)) {
                     returnFileMap.put(aTempTitle, zli_FileMap.get(aTempFileName));
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * of all artists with available files
      * @return TreeMap<String - artist, LinkedList<String - filename>>
      */
     public TreeMap<String, LinkedList<String>> getArtistMap() {
         TreeMap<String, LinkedList<String>> returnFileMap = new TreeMap<String, LinkedList<String>>();
         for (Entry<String, LinkedList<String>> aTempEntry : zli_ArtistMap.entrySet()) {
             for (String aTempFileName : aTempEntry.getValue()) {
                 if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                     if (returnFileMap.containsKey(aTempEntry.getKey())) {
                         if (returnFileMap.get(aTempEntry.getKey()) == null) {
                             LinkedList<String> aFileNameLL = new LinkedList<String>();
                             aFileNameLL.add(aTempFileName);
                             returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                         } else {
                             returnFileMap.get(aTempEntry.getKey()).add(aTempFileName);
                         }
                     } else {
                         LinkedList<String> aFileNameLL = new LinkedList<String>();
                         aFileNameLL.add(aTempFileName);
                         returnFileMap.put(aTempEntry.getKey(), aFileNameLL);
                     }
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * build a TreeMap (ordered according to the natural ordering of its keys)
      * that contains the song title (or fails to filename)
      * and the complete path information
      * @param theArtistName String
      * @return TreeMap<String - title, String - file path>
      */
     public TreeMap<String, String> getTitlesFromArtist(String theArtistName) {
         TreeMap<String, String> returnFileMap = new TreeMap<String, String>();
         LinkedList<String> aTempFileNameList = zli_ArtistMap.get(theArtistName);
         for (String aTempFileName : aTempFileNameList) {
             if (zli_FileMap.containsKey(aTempFileName)) { //check to be sure online copy of file
                 String aTempTitle = getTitleFromFileName(aTempFileName);
                 if (aTempTitle == null) {
                     aTempTitle = aTempFileName;
                 }
                 if (!returnFileMap.containsKey(aTempTitle)) {
                     returnFileMap.put(aTempTitle, zli_FileMap.get(aTempFileName));
                 }
             }
         }
         return returnFileMap;
     }
 
     /**
      * return the title based on the given file name
      * @param theFileName String
      * @return String
      */
     protected String getTitleFromFileName(String theFileName) {
         for (Entry<String, LinkedList<String>> aTempEntry : zli_TitleMap.entrySet()) {
             for (String aTempFileName : aTempEntry.getValue()) {
                 if (aTempFileName.equals(theFileName)) {
                     return aTempEntry.getKey();
                 }
             }
         }
         return null;
     }
 
     /**
      * dump the title TreeMap
      * @return <String - title, LinkedList<String - filenames>>
      */
     public TreeMap<String, LinkedList<String>> getAllTitles() {
         return zli_TitleMap;
     }
 
     public String getFullPathFromFileName(String theFileName) {
         return (zli_FileMap.get(theFileName));
     }
 
     /**
      * return a TreeMap<String - filename, String - full file path> of filenames
      * that match the given search parameters in the library index
      * @param theKeywordStrArray String[]
      * @param matchAllKeywords boolean
      * @param theStartIndexInt Integer
      * @param theEndIndexInt Integer
      * @return TreeMap<String, String>
      */
     public TreeMap<String, String> getFiles(String[] theKeywordStrArray,
             boolean matchAllKeywords, int theStartIndexInt, int theEndIndexInt) {
         TreeMap<String, String> returnFileMap = new TreeMap<String, String>();
         if (zli_FileMap.size() > 0) {
             int i = 0;
             int aOutputCount = 0;
             List<String> aFullFilePathArray = Arrays.asList(zli_FileMap.values().toArray(new String[0]));
             for (String aTempFileName : zli_FileMap.keySet()) {
                 if (stringMatchesKeywords(aTempFileName, theKeywordStrArray, matchAllKeywords)) {
                     if ((aOutputCount >= theStartIndexInt) && (aOutputCount <= theEndIndexInt)) {
                         returnFileMap.put(aTempFileName, aFullFilePathArray.get(i));
                     }
                     aOutputCount++;
                 }
                 i++;
             }
 
             if (debugEventsOn) {
                 System.out.println("ZLI getFiles - output " + String.valueOf(i) + " files");
             }
         }
         return returnFileMap;
     }
 
     public HashMap<String, String> getAllFiles() {
         return zli_FileMap;
     }
 
     /**
      * iteratively index the raw paths of the file system
      * currently supports: CIFS and local files
      * @param thePathStr String
      */
     protected void indexPath(String thePathStr) {
         if (debugEventsOn) {
             System.out.println("ZLI indexPath - will now index " + thePathStr);
         }
 
         if (thePathStr.contains(FileSystemType.smb.toString().concat(ZoneServerUtility.prefixUriStr))) { //CIFS share
             ArrayList<SmbFile> aCIFSDirList = CIFSNetworkInterface.getInstance().getDirectoryList(thePathStr);
             LinkedList<SmbFile> aSmbFileLinkedList = new LinkedList<SmbFile>(aCIFSDirList);
 
             while (aSmbFileLinkedList.peek() != null) {
                 SmbFile iSmbFile = aSmbFileLinkedList.removeFirst();
 
                 if (iSmbFile.getPath().endsWith("/")) { //recurse into directory during search
                     ArrayList<SmbFile> tempSmbFiles = CIFSNetworkInterface.getInstance().getDirectoryList(iSmbFile.getPath());
                     if ((tempSmbFiles != null) && (tempSmbFiles.size() > 0)) {
                         for (SmbFile tempSmbFile : tempSmbFiles) {
                             aSmbFileLinkedList.addFirst(tempSmbFile);
 
                             if (debugEventsOn) {
                                 System.out.println("ZLI indexPath - will follow " + tempSmbFile.toString());
                             }
                         }
                     }
                 } else { //have a file, add it to the various index TreeMaps
                     addFileToMaps(iSmbFile.getPath(), iSmbFile.getName());
                 }
             }
 
             if (debugEventsOn) {
                 System.out.println("ZLI indexPath - done indexing " + thePathStr);
             }
         } else { //local filesytem
             File dir = new File(thePathStr);
             File[] files = dir.listFiles();
             LinkedList<File> aFileLinkedList = new LinkedList<File>();
             aFileLinkedList.addAll(Arrays.asList(files));
 
             while (aFileLinkedList.peek() != null) {
                 File iFile = aFileLinkedList.removeFirst();
 
                 if (iFile.isDirectory()) { //recurse into directory during search
                     File[] tempFileArray = iFile.listFiles();
                     if (tempFileArray != null) {
                         for (File tempFile : tempFileArray) {
                             aFileLinkedList.addFirst(tempFile);
                         }
                     }
                 } else { //have a file, add it to the file index map
                     String tempFilePathStr = iFile.getAbsolutePath();
                     if (tempFilePathStr.contains("\\")) {
                         tempFilePathStr = tempFilePathStr.replaceAll("\\\\+", "/");
                         //see http://www.java-forums.org/advanced-java/16452-replacing-backslashes-string-object.html#post59396
                     }
 
                     addFileToMaps(tempFilePathStr, iFile.getName());
                 }
             }
 
             if (debugEventsOn) {
                 System.out.println("ZLI indexPath - done indexing " + thePathStr);
             }
         }
     }
 
     /**
      * actually add the file and its information to the various TreeMaps
      * @param theRawFullFilePath String
      * @param theRawFileName String
      */
     protected void addFileToMaps(String theRawFullFilePath, String theRawFileName) {
         String aFullFilePathStr = null;
         try {
             aFullFilePathStr = URLEncoder.encode(theRawFullFilePath, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (aFullFilePathStr != null) {
             if (theContainerIsSupported(aFullFilePathStr)) {
                 if (!zli_FileMap.containsKey(theRawFileName)) {
                     zli_FileMap.put(theRawFileName, aFullFilePathStr);
                     if (debugEventsOn) {
                         System.out.println("ZLI addFileToMaps - added " + aFullFilePathStr);
                     }
 
                     if ((!MusicZones.getIsLowMem()) && (theContainerIsMp3(theRawFullFilePath))) {
                         ID3MetaData aID3MetaData = null;
                         try {
                             aID3MetaData = new ID3MetaData(theRawFullFilePath);
                         } catch (MalformedURLException ex) {
                             if (debugEventsOn) {
                                 Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                             }
                         } catch (ID3Exception ex) {
                             if (debugEventsOn) {
                                 Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                             }
                         }
 
                         if (aID3MetaData != null) {
                             String aFieldAlbum = aID3MetaData.getAlbum();
                             if (aFieldAlbum != null) {
                                 if (debugEventsOn) {
                                     System.out.println("ZLI addFileToMaps - album = '" + aFieldAlbum + "'");
                                 }
                                 addToAlbum(theRawFileName, aFieldAlbum);
                             }
 
                             List<String> aFieldArtistList = aID3MetaData.getArtistsAsList();
                             if (aFieldArtistList != null) {
                                 addToArtist(theRawFileName, aFieldArtistList);
                             }
 
                             String aFieldTitle = aID3MetaData.getTitle();
                             if (aFieldTitle != null) {
                                 if (debugEventsOn) {
                                     System.out.println("ZLI addFileToMaps - title = '" + aFieldTitle + "'");
                                 }
                                 addToTitle(theRawFileName, aFieldTitle);
                             }
 
                             ArrayList<String> aFieldGenresList = aID3MetaData.getGenresAsList();
                             if (aFieldGenresList != null) {
                                 addToGenre(theRawFileName, aFieldGenresList);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * add a filename to a album group, create if !exists
      * @param theFileName String
      * @param theAlbumTitle String
      */
     protected void addToAlbum(String theFileName, String theAlbumTitle) {
         if ((theFileName == null) || (theAlbumTitle == null)) {
             return;
         }
 
         if (theAlbumTitle.equals("")) {
             theAlbumTitle = "Unknown";
         }
         if (zli_AlbumMap.containsKey(theAlbumTitle)) {
             if (zli_AlbumMap.get(theAlbumTitle) == null) {
                 LinkedList<String> aFileNameLL = new LinkedList<String>();
                 aFileNameLL.add(theFileName);
                 zli_AlbumMap.put(theAlbumTitle, aFileNameLL);
             } else {
                 zli_AlbumMap.get(theAlbumTitle).add(theFileName);
             }
         } else {
             LinkedList<String> aFileNameLL = new LinkedList<String>();
             aFileNameLL.add(theFileName);
             zli_AlbumMap.put(theAlbumTitle, aFileNameLL);
         }
     }
 
     /**
      * add a filename to an artist group, create if it does not exist
      * @param theFileName String
      * @param theArtistNameList List<String>
      */
     protected void addToArtist(String theFileName, List<String> theArtistNameList) {
         if ((theFileName == null) || (theArtistNameList == null)) {
             return;
         }
 
         for (String theArtistName : theArtistNameList) {
             if (zli_ArtistMap.containsKey(theArtistName)) {
                 if (zli_ArtistMap.get(theArtistName) == null) {
                     LinkedList<String> aFileNameLL = new LinkedList<String>();
                     aFileNameLL.add(theFileName);
                     zli_ArtistMap.put(theArtistName, aFileNameLL);
                 } else {
                     zli_ArtistMap.get(theArtistName).add(theFileName);
                 }
             } else {
                 LinkedList<String> aFileNameLL = new LinkedList<String>();
                 aFileNameLL.add(theFileName);
                 zli_ArtistMap.put(theArtistName, aFileNameLL);
             }
         }
     }
 
     /**
      * add a filename to multiple genres
      * @param theFileName String
      * @param theGenreList ArrayList<String>
      */
     protected void addToGenre(String theFileName, ArrayList<String> theGenreList) {
         if ((theFileName == null) || (theGenreList == null)) {
             return;
         }
 
         for (String aGenre : theGenreList) {
             if (aGenre.contains("(") || aGenre.contains(")")) {
                 continue; //do not add corrupted genres
             }
             if (aGenre.equals("")) {
                 aGenre = "Unknown";
             }
             if (zli_GenreMap.containsKey(aGenre)) {
                 if (zli_GenreMap.get(aGenre) == null) {
                     LinkedList<String> aFileNameLL = new LinkedList<String>();
                     aFileNameLL.add(theFileName);
                     zli_GenreMap.put(aGenre, aFileNameLL);
                 } else {
                     zli_GenreMap.get(aGenre).add(theFileName);
                 }
             } else {
                 LinkedList<String> aFileNameLL = new LinkedList<String>();
                 aFileNameLL.add(theFileName);
                 zli_GenreMap.put(aGenre, aFileNameLL);
             }
         }
     }
 
     /**
      * add a filename to an title group, create if it does not exist
      * @param theFileName String
      * @param theSongTitle String
      */
     protected void addToTitle(String theFileName, String theSongTitle) {
         if ((theFileName == null) || (theSongTitle == null)) {
             return;
         }
 
         if (zli_TitleMap.containsKey(theSongTitle)) {
             if (zli_TitleMap.get(theSongTitle) == null) {
                 LinkedList<String> aFileNameLL = new LinkedList<String>();
                 aFileNameLL.add(theFileName);
                 zli_TitleMap.put(theSongTitle, aFileNameLL);
             } else {
                 zli_TitleMap.get(theSongTitle).add(theFileName);
             }
         } else {
             LinkedList<String> aFileNameLL = new LinkedList<String>();
             aFileNameLL.add(theFileName);
             zli_TitleMap.put(theSongTitle, aFileNameLL);
         }
     }
 
     /**
      * remove all files from the library index that contain the path string
      * @param thePathStr String
      */
     protected void removePath(String thePathStr) {
         thePathStr = thePathStr.toLowerCase(Locale.ENGLISH);
         try {
             thePathStr = URLEncoder.encode(thePathStr, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
             if (debugEventsOn) {
                 Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
                 return;
             }
         }
 
         for (Entry<String, String> aTempEntry : zli_FileMap.entrySet()) {
             String aTempFullFilePath = aTempEntry.getValue();
             if (aTempFullFilePath.contains(thePathStr)) {
                 zli_FileMap.remove(aTempEntry.getKey());
 
                 if (debugEventsOn) {
                     System.out.println("ZLI removePath - removed " + aTempFullFilePath);
                 }
             }
         }
     }
 
     /**
      * remove all indexed media that does not have an online host
      * @param theHostList LinkedList<SmbFile>
      */
     protected void removeOffline(LinkedList<SmbFile> theHostList) {
         if ((theHostList == null) || (zli_FileMap == null)) {
             return;
         }
 
         Iterator<SmbFile> aServerSmbFileIter = theHostList.iterator();
         while (aServerSmbFileIter.hasNext()) {
             SmbFile aServerSmbFile = aServerSmbFileIter.next();
             String aServerStrEncoded;
             try {
                 aServerStrEncoded = URLEncoder.encode(aServerSmbFile.toString(), "UTF-8");
             } catch (UnsupportedEncodingException ex) {
                 if (debugEventsOn) {
                     Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 continue;
             }
 
             boolean aKeepFile = false;
 
             for (Entry<String, String> aTempEntry : zli_FileMap.entrySet()) {
                 String aTempFullFilePath = aTempEntry.getValue();
                 if (aTempFullFilePath.contains(aServerStrEncoded)) {
                     aKeepFile = true;
                 }
                 if (!aKeepFile) {
                     zli_FileMap.remove(aTempEntry.getKey());
 
                     if (debugEventsOn) {
                         System.out.println("ZLI removeOffline - removed " + aTempFullFilePath);
                     }
                 }
             }
         }
     }
 
     /**
      * function to see if the given string matches (all or any) of the keyword array strings
      * returns true if no keywords are given
      *
      * @param theString String
      * @param theKeywords String[]
      * @param matchAllKeywords boolean
      * @return boolean - did theString match the keywords appropriately?
      */
     public boolean stringMatchesKeywords(String theString, String[] theKeywords, boolean matchAllKeywords) {
         if ((theString == null) || (theKeywords == null)) {
             return true;
         } else {
             theString = theString.toLowerCase(Locale.ENGLISH);
             boolean matchOneKeywordBoolean = false;
             for (String aKeyword : theKeywords) {
                 aKeyword = aKeyword.toLowerCase(Locale.ENGLISH);
                 if ((matchAllKeywords) && (!theString.contains(aKeyword))) {
                     return false;
                 } else if (theString.contains(aKeyword)) {
                     matchOneKeywordBoolean = true;
                 }
             }
             if ((!matchAllKeywords) && (!matchOneKeywordBoolean)) {
                 return false;
             }
             return true;
         }
     }
 
     /**
      * check the extension for ".mp3"
      * @param theFileName String
      * @return boolean - is the extension ".mp3"?
      */
     public boolean theContainerIsMp3(String theFileName) {
         return theFileName.contains(".mp3");
     }
 
     /**
      * exact string matching of filename extension to see if file is in
      * one of the supported container formats
      * @param theFileName String
      * @return boolean - is it supported?
      */
     public boolean theContainerIsSupported(String theFileName) {
         if (theFileName.contains(".")) {
             for (String aSupportedContainer : MediaPlayerImpl.theSupportedContainers) {
                 if (theFileName.contains("." + aSupportedContainer)) {
                     return true;
                 }
             }
             return theContainerIsPlayList(theFileName);
         }
         return false;
     }
 
     /**
      * check to see if file is a valid playlist container file based
      * on exact string extension matching
      * @param theFileName String
      * @return boolean - is it a playlist file?
      */
     public boolean theContainerIsPlayList(String theFileName) {
         for (String aPlayListContainer : MediaPlayerImpl.thePlayListContainers) {
             if (theFileName.contains("." + aPlayListContainer)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * check if the passed path string is a blacklisted one
      * @param thePathStr String
      * @return boolean - is it blacklisted?
      */
     protected boolean thePathIsBlackListed(String thePathStr) {
         for (String aPathStr : zli_CIFSPathBlackListArray) {
             if (thePathStr.contains(aPathStr)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * check if netbios-ssn (TCP 139) is open on machine
      *
      * @param theValidIPv4Addr String
      * @return boolean - does host possibly have Samba shares?
      */
     protected boolean isPossibleSambaHost(String theValidIPv4Addr) {
         InetSocketAddress aTestInetSocketAddress = new InetSocketAddress(theValidIPv4Addr, 139);
         Socket aTestSocket = new Socket();
         try {
             aTestSocket.connect(aTestInetSocketAddress, 100); //milliseconds
             aTestSocket.close();
             return true;
         } catch (Exception ex) {
             return false; //socket not open
         }
     }
 
     /**
      * private timed task that adds all supported media from all CIFS shares on local network
      * if we are unable to raise the Master Browser, then scrape the local subnet for shares
      */
     private class RefreshCIFSMediaTask extends TimerTask {
 
         private String kSmbPrefix = "smb://";
 
        @SuppressWarnings("unused") //"aWorkGroupSmbFile == null" is not dead code
		@Override
         public void run() {
             if (getIndexIsBuilding()) {
                 return; //stop if index is already building
             } else {
                 setIndexIsBuilding(true);
             }
 
             LinkedList<SmbFile> aHostList = new LinkedList<SmbFile>();
 
             //query CIFS master browser for any workgroups
             String[] aWorkGroupArray = null;
             if (MusicZones.getIsOnline()) {
                 try {
                     SmbFile aRootSmbFile = new SmbFile(kSmbPrefix);
                     try {
                         aWorkGroupArray = aRootSmbFile.list();
                     } catch (SmbException ex) {
                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                     }
                 } catch (MalformedURLException ex) {
                     Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             //check localhost for possible SMB shares
             if (MusicZones.getIsIndexLocalHost()) {
                 String zoneIPv4Addr = Layer3Info.getInstance().getValidIPAddress(IpAddressType.IPv4).trim();
                 if (zoneIPv4Addr != null) {
                     SmbFile aServerSmbFile = null;
                     try {
                         aServerSmbFile = new SmbFile(kSmbPrefix + zoneIPv4Addr + "/");
                     } catch (MalformedURLException ex) {
                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     if ((aServerSmbFile != null) && (!aHostList.contains(aServerSmbFile))) {
                         aHostList.add(aServerSmbFile);
 
                         if (debugEventsOn) {
                             System.out.println("ZLI RefreshSearchIndexTask - added "
                                     + aServerSmbFile.toString() + " to host cache");
                         }
                     }
                 }
             }
             //scrape subnet for servers if none found with master browsers
             if (MusicZones.getIsOnline() && (aWorkGroupArray == null)) {
                 System.err.println("ZLI RefreshSearchIndexTask - no workgroups found");
                 String zoneIPv4Addr = Layer3Info.getInstance().getValidIPAddress(IpAddressType.IPv4).trim();
                 System.out.println("ZLI RefreshSearchIndexTask - " + zoneIPv4Addr + " will now scrape subnet ...");
                 String[] zoneIPv4AddrOctets = zoneIPv4Addr.split("\\."); //dot is reserved char in regex
                 if (zoneIPv4AddrOctets.length == 4) {
                     String aValidIPv4Prefix = zoneIPv4AddrOctets[0] + "."
                             + zoneIPv4AddrOctets[1] + "." + zoneIPv4AddrOctets[2] + ".";
                     for (int i = zli_IPv4ScanMin; i <= zli_IPv4ScanMax; i++) {
                         String aNewValidIPv4Addr = aValidIPv4Prefix + String.valueOf(i);
 
                         if (isPossibleSambaHost(aNewValidIPv4Addr)) {
                             //if TCP 139 is active then add to host list
                             SmbFile aServerSmbFile = null;
                             try {
                                 aServerSmbFile = new SmbFile(kSmbPrefix + aNewValidIPv4Addr + "/");
                             } catch (MalformedURLException ex) {
                                 Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.SEVERE, null, ex);
                             }
                             if ((aServerSmbFile != null) && (!aHostList.contains(aServerSmbFile))) {
                                 aHostList.add(aServerSmbFile);
 
                                 if (debugEventsOn) {
                                     System.out.println("ZLI RefreshSearchIndexTask - added "
                                             + aServerSmbFile.toString() + " to host cache");
                                 }
                             }
                         }
                     }
                     System.out.println("ZLI RefreshSearchIndexTask - subnet scraping done");
                 } else {
                     System.err.println("ZLI RefreshSearchIndexTask - unable to build subnet "
                             + "prefix for scraping - got " + zoneIPv4AddrOctets.length + " octets");
                 }
             } else if (aWorkGroupArray != null) { //check workgroups for servers if found with master browser
                 for (String aWorkGroup : aWorkGroupArray) {
                     SmbFile aWorkGroupSmbFile = null;
                     try {
                         aWorkGroupSmbFile = new SmbFile(kSmbPrefix + aWorkGroup);
                     } catch (MalformedURLException ex) {
                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                         continue;
                     }
                     if (aWorkGroupSmbFile == null) {
                         continue;
                     }
 
                     String[] aServerArray = null; //get a list of servers in workgroup
                     try {
                         aServerArray = aWorkGroupSmbFile.list();
                     } catch (SmbException ex) {
                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                         continue;
                     }
                     if (aServerArray == null) {
                         System.err.println("ZLI RefreshSearchIndexTask - no hosts found in '" + aWorkGroup + "' workgroup");
                     } else {
                         for (String aServer : aServerArray) {
                             SmbFile aServerSmbFile = null;
                             try {
                                 aServerSmbFile = new SmbFile(kSmbPrefix + aServer);
                             } catch (MalformedURLException ex) {
                                 if (debugEventsOn) {
                                     Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                                 }
                             }
                             if (aServerSmbFile == null) {
                                 continue;
                             } else if (!aServerSmbFile.toString().endsWith("/")) { //pre-format each server address
                                 String aServerStr = aServerSmbFile.toString() + "/";
                                 aServerStr = aServerStr.toLowerCase(Locale.ENGLISH);
                                 try {
                                     aServerSmbFile = new SmbFile(aServerStr);
                                 } catch (MalformedURLException ex) {
                                     if (debugEventsOn) {
                                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                                     }
                                     continue;
                                 }
                             }
 
                             if (!aHostList.contains(aServerSmbFile)) { //do not have server in list, add it
                                 aHostList.add(aServerSmbFile);
 
                                 if (debugEventsOn) {
                                     System.out.println("ZLI RefreshSearchIndexTask - added "
                                             + aServerSmbFile.toString() + " to host cache");
                                 }
                             }
                         }
                     }
                 }
             }
 
             //remove all indexed media that does not have an online host
             removeOffline(aHostList);
 
             //spider all open shares on indexed servers for media
             Iterator<SmbFile> aServerSmbFileIter = aHostList.iterator();
             while (aServerSmbFileIter.hasNext()) {
                 SmbFile aServerSmbFile = aServerSmbFileIter.next();
 
                 //malformed fast scanning of TCP 139 earlier causes RST here, needed retry after timeout
                 String[] aSharePathArray = null;
                 int aRetryCnt = 0;
                 int aMaxRetryCnt = 3;
                 while ((aRetryCnt < aMaxRetryCnt) && (aSharePathArray == null)) {
                     if (debugEventsOn) {
                         System.out.println("ZLI RefreshSearchIndexTask - will "
                                 + "now connect to " + aServerSmbFile.toString()
                                 + " try #" + (aRetryCnt + 1));
                     }
                     try {
                         aSharePathArray = aServerSmbFile.list();
                     } catch (SmbException ex) {
                         Logger.getLogger(ZoneLibraryIndex.class.getName()).log(Level.WARNING, null, ex);
                     }
                     aRetryCnt++;
                 }
 
                 if (aSharePathArray == null) {
                     aHostList.remove(aServerSmbFile);
                 } else {
                     for (String aSharePath : aSharePathArray) {
                         aSharePath = aSharePath.toLowerCase(Locale.ENGLISH);
                         if (!thePathIsBlackListed(aSharePath)) {
                             if (!aSharePath.endsWith("/")) {
                                 aSharePath = aSharePath + "/";
                             }
                             indexPath(aServerSmbFile.toString() + aSharePath);
                         }
                     }
                 }
             }
 
             //done building index
             setIndexIsBuilding(false);
         }
     }
 }
