 package net.kuehldesign.backuptube.app.console;
 
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonWriter;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import net.kuehldesign.backuptube.BackupHelper;
 import net.kuehldesign.backuptube.app.common.BackupTubeCommon;
 import net.kuehldesign.backuptube.app.common.datafile.BackupTubeDataFile;
 import net.kuehldesign.backuptube.app.common.stored.StoredVideo;
 import net.kuehldesign.backuptube.app.common.stored.youtube.StoredYouTubeVideo;
 import net.kuehldesign.backuptube.app.console.exception.UnableToReadFromConsoleException;
 import net.kuehldesign.backuptube.exception.BadVideoException;
 import net.kuehldesign.backuptube.exception.FatalBackupException;
 import net.kuehldesign.backuptube.exception.UnableToOpenURLConnectionException;
 import net.kuehldesign.backuptube.site.youtube.YouTubeVideo;
 import net.kuehldesign.jnetutils.FileDownloader;
 import net.kuehldesign.jnetutils.exception.FileAlreadyExistsException;
 
 public class BackupTubeApp {
     private static final String badSaveDirMessage = "Unable to create directory there, please choose a different location";
 
     private static void showHelp(PrintStream out) {
         String[] lines = {
                             "  usage: BackupTubeApp.jar",
                             "options:",
                             "         --help                 Dislays this help message",
                             "",
                             "         --username [username]",
                             "         -u [username]          Specify a username to backup",
                             "",
                             "         --savedir [directory]",
                             "         -d [directory]         Specify a directory to save data to"
                          };
 
         for (String line : lines) {
             out.println(line);
         }
     }
 
     private static String getLineFromConsole(BufferedReader reader, String prompt) throws UnableToReadFromConsoleException {
         String line = null;
 
         while (line == null || line.length() <= 0) {
             System.out.println(prompt);
 
             try {
                 line = reader.readLine();
             } catch (IOException ex) {
                 throw new UnableToReadFromConsoleException();
             }
         }
 
         return line;
     }
 
     private static void sendMessageIfBadSaveDir(boolean success) {
         if (! success) {
             System.err.println(badSaveDirMessage);
         }
     }
 
     private static BackupTubeDataFile getDataFile(File dataFeedFile) throws NullPointerException {
         try {
             return new Gson().fromJson(new BufferedReader(new FileReader(dataFeedFile)), BackupTubeDataFile.class);
         } catch (FileNotFoundException ex) {
             return null;
         }
     }
 
     public static void saveDataFile(File dataFeedFile, BackupTubeDataFile dataFile) {
         JsonWriter writer;
         try {
             writer = new JsonWriter(new FileWriter(dataFeedFile));
             new Gson().toJson(dataFile, BackupTubeDataFile.class, writer);
         } catch (IOException ex) {
             System.err.println("Fatal error: Unable to write the main data file");
             System.exit(0);
         }
     }
 
     public static void main(String[] args) {
         boolean isError = false;
         boolean showHelp = false;
         String user = null;
         String saveDir = null;
         String expecting = null;
 
         for (String arg : args) {
             if (expecting != null) {
                 if (expecting.equals("user")) {
                     user = arg;
                 } else if (expecting.equals("saveDir")) {
                     saveDir = arg;
                 } else {
                     System.err.println("Unexpected: " + arg);
                     isError = true;
                     break;
                 }
 
                 expecting = null;
                 continue;
             }
 
             if (arg.equals("--help")) {
                 showHelp = true;
             } else if (arg.startsWith("-u")) {
                 if (arg.equals("-u")) {
                     expecting = "user";
                 } else {
                     user = arg.substring(2);
                 }
             } else if (arg.startsWith("-d")) {
                 if (arg.equals("-d")) {
                     expecting = "saveDir";
                 } else {
                     saveDir = arg.substring(2);
                 }
             } else if (arg.equals("--username")) {
                 expecting = "user";
             } else if (arg.equals("--savedir")) {
                 expecting = "saveDir";
             } else {
                 System.err.println("Unexpected: " + arg);
                 isError = true;
                 break;
             }
         }
 
         if (isError || showHelp) {
             showHelp(isError ? System.err : System.out);
             System.exit(0);
         }
 
         InputStreamReader inreader = new InputStreamReader(System.in);
         BufferedReader reader = new BufferedReader(inreader);
 
         if (user == null) {
             try {
                 user = getLineFromConsole(reader, "Enter the user to backup:");
             } catch (UnableToReadFromConsoleException ex) {
                 ex.printStackTrace();
                 System.exit(0);
             }
         }
 
         boolean success = false;
 
         // if a save directory was given in the command, try to use it, otherwise ask for a new one
         if (saveDir != null) {
             saveDir = BackupTubeCommon.fixDir(saveDir);
             success = BackupTubeCommon.isGoodSaveDir(saveDir);
             sendMessageIfBadSaveDir(success);
         }
 
         // if no save dir was provided or if it was a bad save dir, ask for a new one
         while (! success) {
             try {
                 saveDir = getLineFromConsole(reader, "Enter the directory to save the backed up data to:");
                 saveDir = BackupTubeCommon.fixDir(saveDir);
                 success = BackupTubeCommon.isGoodSaveDir(saveDir);
                 sendMessageIfBadSaveDir(success);
             } catch (UnableToReadFromConsoleException ex) {
                 ex.printStackTrace();
                 System.exit(0);
             }
         }
 
         // check if there's already a data feed; if so, load from it
         File dataFeedFile = new File(saveDir + BackupTubeCommon.LOCATION_DATAFILE);
         try {
             BackupTubeDataFile prevDataFile = getDataFile(dataFeedFile);
 
             for (StoredVideo video : prevDataFile.getVideos()) {
                 if (video.hasBeenDeleted()) {
                     // has been deleted
                     System.out.println("Found deleted video: " + video.getFolderName());
                 }
             }
         } catch (NullPointerException ex) {
             // data file doesn't exist yet
         }
 
         BackupHelper helper = new BackupHelper();
         helper.setUser(user);
 
         LinkedList<YouTubeVideo> videos = null;
 
         try {
             videos = helper.getVideos();
         } catch (FatalBackupException ex) {
             System.err.println("Fatal exception: " + ex.getMessage());
             ex.printStackTrace();
         } catch (UnableToOpenURLConnectionException ex) {
             System.err.println("Unable to open URL connection; does YouTube account exist?");
             ex.printStackTrace();
         }
 
         // now, start downloading the videos
         try {
             int videoCount = 0;
             int totalVideoCount = videos.size();
 
             for (YouTubeVideo video : videos) {
                 for (int downloadTry = 0; downloadTry < 3; downloadTry ++) {
                     //System.out.println("Starting try " + (downloadTry + 1) + "/3 to download \"" + video.getTitle() + "\"");
 
                     try {
                         video.init();
                         String downloadURL = video.getDownloadURL();
                         FileDownloader d = null;
                         String safeVideoTitle = BackupTubeCommon.escapeFileName(video.getTitle());
                         SimpleDateFormat published = new SimpleDateFormat("yyyy_MM_dd");
                         Date date = new Date(video.getPublished());
                         String videoFolder = published.format(date) + " " + safeVideoTitle;
 
                         try {
                             d = new FileDownloader(new URL(downloadURL), saveDir + videoFolder + "/" + safeVideoTitle + "." + video.getExtension());
                         } catch (FileAlreadyExistsException ex) {
                             System.err.println("Unable to download video \"" + video.getTitle() + "\"; file already exists");
                             break;
                         }
 
                         d.startDownload();
                         boolean hasError = false;
 
                         while (! d.isFinished()) {
                             if (d.hasError()) {
                                 hasError = true;
                                 break;
                             }
 
                             double progress = d.getProgress();
                             progress *= 10000;
                             progress = Math.round(progress);
                             progress /= 100;
                             String progressMessage = "Progress (" + videoCount + "/" + totalVideoCount + " \"" + video.getTitle() + "\"): " + progress + "%    \r";
 
                             System.out.print(progressMessage); // using print instead of println since using \r for progress
 
                             try {
                                 Thread.sleep(1000);
                             } catch (InterruptedException ex) {
 
                             }
                         }
 
                         if (hasError) {
                             if (downloadTry == 2) {
                                 System.err.println("Unable to download video \"" + video.getTitle() + "\"");
                             } else {
                                 System.err.println("Has error, starting new try.");
                             }
                             continue;
                         }
 
                         System.out.println("Successfully downloaded video \"" + video.getTitle() + "\"");
 
                         // now update the JSON file since the file has been downloaded
 
                         // created the StoredVideo object
                         // TODO: add a better way to deal with type (e.g. YouTube vs another site)
                         StoredYouTubeVideo storedVideo = new StoredYouTubeVideo();
 
                         storedVideo.setTitle(video.getTitle());
                         storedVideo.setDownloadedTime(BackupTubeCommon.getCurrentTime());
                         storedVideo.setFolderName(videoFolder);
                         storedVideo.setVideoID(video.getID());
 
                         // get the current data object
                         BackupTubeDataFile dataFile;
 
                         try {
                             dataFile = getDataFile(dataFeedFile);
                         } catch (NullPointerException ex) {
                             dataFile = new BackupTubeDataFile();
                         }
 
                         LinkedList<StoredVideo> storedVideos = dataFile.getVideos();
                         storedVideos.add(storedVideo);
                         
                         saveDataFile(dataFeedFile, dataFile);
                         break;
                     } catch (BadVideoException ex) {
                         ex.printStackTrace();
                     } catch (MalformedURLException ex) {
                         ex.printStackTrace();
                     }
                 }
 
                 videoCount ++;
             }
         } catch (FatalBackupException ex) {
             System.err.println("Fatal exception: " + ex.getMessage());
             ex.printStackTrace();
         }
     }
 }
