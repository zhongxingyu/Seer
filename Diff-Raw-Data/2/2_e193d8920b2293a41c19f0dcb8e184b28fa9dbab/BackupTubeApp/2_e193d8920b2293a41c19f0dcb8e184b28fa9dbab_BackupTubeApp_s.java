 package net.kuehldesign.backuptube.app.console;
 
 import com.google.gson.Gson;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import net.kuehldesign.backuptube.BackupHelper;
 import net.kuehldesign.backuptube.app.common.BackupTubeCommon;
 import net.kuehldesign.backuptube.app.common.datafile.BackupTubeDataFile;
 import net.kuehldesign.backuptube.app.common.listed.ListedVideo;
 import net.kuehldesign.backuptube.app.common.listed.site.youtube.ListedYouTubeVideo;
 import net.kuehldesign.backuptube.app.common.stored.StoredVideo;
 import net.kuehldesign.backuptube.app.common.stored.StoredVideoSiteInfo;
 import net.kuehldesign.backuptube.app.common.stored.site.youtube.StoredYouTubeResponseInfo;
 import net.kuehldesign.backuptube.app.common.stored.site.youtube.StoredYouTubeVideo;
 import net.kuehldesign.backuptube.app.console.exception.UnableToReadFromConsoleException;
 import net.kuehldesign.backuptube.exception.BadVideoException;
 import net.kuehldesign.backuptube.exception.FatalBackupException;
 import net.kuehldesign.backuptube.exception.UnableToOpenURLConnectionException;
 import net.kuehldesign.backuptube.site.youtube.video.YouTubeVideo;
 import net.kuehldesign.backuptube.video.DownloadableVideo;
 import net.kuehldesign.jnetutils.FileDownloader;
 import net.kuehldesign.jnetutils.exception.FileAlreadyExistsException;
 
 public class BackupTubeApp {
     private static final String CLIENT_TYPE = "command-line";
     private static final String CLIENT_VERSION = "0.1";
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
                             "         -s [siteid]",
                             "         --site [siteid]        Specify a site ID (e.g. \"youtube\")",
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
 
     private static BackupTubeDataFile getDataFile(File dataFeedFile) throws NullPointerException, FileNotFoundException {
         return new Gson().fromJson(new BufferedReader(new FileReader(dataFeedFile)), BackupTubeDataFile.class);
     }
 
     public static void saveDataFile(File dataFeedFile, Object dataFile) {
         if (dataFeedFile.exists()) {
             dataFeedFile.delete();
         }
 
         try {
             String json = BackupTubeCommon.getPrettyGson().toJson(dataFile, dataFile.getClass());
             PrintWriter writer = new PrintWriter(dataFeedFile);
             writer.write(json);
             writer.close();
         } catch (IOException ex) {
             System.err.println("Fatal error: Unable to write the data file");
             System.exit(0);
         }
     }
 
     public static void main(String[] args) {
         boolean isError = false;
         boolean showHelp = false;
         String user = null;
         String saveDir = null;
         String expecting = null;
         String site = null;
 
         for (String arg : args) {
             if (expecting != null) {
                 if (expecting.equals("user")) {
                     user = arg;
                 } else if (expecting.equals("saveDir")) {
                     saveDir = arg;
                 } else if (expecting.equals("site")) {
                     site = arg;
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
             } else if (arg.startsWith("-s")) {
                 if (arg.equals("-s")) {
                     expecting = "site";
                 } else {
                     site = arg.substring(2);
                 }
             } else if (arg.equals("--username")) {
                 expecting = "user";
             } else if (arg.equals("--savedir")) {
                 expecting = "saveDir";
             } else if (arg.equals("--site")) {
                 expecting = "site";
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
 
         if (site == null) {
             try {
                 site = getLineFromConsole(reader, "Enter the site to backup from (e.g. \"youtube\"):");
             } catch (UnableToReadFromConsoleException ex) {
                 ex.printStackTrace();
                 System.exit(0);
             }
         }
 
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
 
         try { // TODO: add logic for moving videos once found
             BackupTubeDataFile prevDataFile = getDataFile(dataFeedFile);
 
             for (ListedVideo video : prevDataFile.getVideos()) {
                 if (video.hasBeenDeleted()) {
                     // has been deleted
                     System.out.println("Found deleted video: " + video.getFolderName());
                 }
             }
         } catch (FileNotFoundException ex) {
             // data file doesn't exist yet
         }
 
         BackupHelper helper = new BackupHelper(site);
         helper.setUser(user);
 
         LinkedList<DownloadableVideo> videos = null;
 
         try {
             videos = helper.getVideos();
         } catch (FatalBackupException ex) {
             System.err.println("Fatal exception: " + ex.getMessage());
             ex.printStackTrace();
             System.exit(0);
         } catch (UnableToOpenURLConnectionException ex) {
             System.err.println("Unable to open URL connection; does account exist?");
             ex.printStackTrace();
             System.exit(0);
         }
 
         // now, start downloading the videos
         try {
             int videoCount = 0;
             int totalVideoCount = videos.size();
 
             for (DownloadableVideo video : videos) {
                 // TODO: check if it exists already in the folder, if so, skip
 
                 for (int downloadTry = 0; downloadTry < 3; downloadTry ++) {
                     try {
                         video.init();
                         String downloadURL = video.getDownloadURL();
                         FileDownloader d = null;
                         String safeVideoTitle = BackupTubeCommon.escapeFileName(video.getTitle());
                         SimpleDateFormat published = new SimpleDateFormat("yyyy_MM_dd");
                         Date date = new Date(video.getPublished());
                         String videoFolder = published.format(date) + " " + safeVideoTitle;
                         String videoFileName = safeVideoTitle + "." + video.getExtension();
                         String videoSaveLocation = saveDir + videoFolder + "/" + videoFileName;
 
                         File videoFile = new File(videoSaveLocation);
 
                         // it was created in a previous try and needs to
                         // be deleted so it can be downloaded again
                         if (videoFile.exists()) {
                             videoFile.delete();
                         }
 
                         try {
                             d = new FileDownloader(new URL(downloadURL), videoSaveLocation);
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
 
                         // create the video HTML file
                         File singleVideoDataFeedFile = new File(saveDir + videoFolder + "/" + BackupTubeCommon.LOCATION_VIDEO_DATAFILE);
 
                         if (singleVideoDataFeedFile.exists()) {
                             singleVideoDataFeedFile.delete();
                         }
 
                         StoredVideo storedVideo;
                         StoredVideoSiteInfo siteInfo = new StoredVideoSiteInfo();
 
                         siteInfo.setSiteID(video.getSiteID());
                         siteInfo.setVideoID(video.getVideoID());
 
                         if (video.getSiteID().equals(BackupHelper.SITE_YOUTUBE)) {
                             storedVideo = new StoredYouTubeVideo();
                             
                             YouTubeVideo siteVideo = (YouTubeVideo) video;
                             StoredYouTubeVideo storedSiteVideo = (StoredYouTubeVideo) storedVideo;
 
                             storedSiteVideo.setCategory(siteVideo.getCategory());
                             storedSiteVideo.setTags(siteVideo.getTags());
 
                             // video response
                             YouTubeVideo responseVideo = siteVideo.getResponseVideo();
 
                             if (responseVideo != null) {
                                 StoredYouTubeResponseInfo responseInfo = new StoredYouTubeResponseInfo();
                                 responseInfo.setTitle(responseVideo.getTitle());
                                 responseInfo.setUrl(responseVideo.getURL());
                                 responseInfo.setUser(responseVideo.getUploader());
                                
                                 storedSiteVideo.setVideoResponse(responseInfo);
                             }
                         } else {
                             storedVideo = new StoredVideo();
                         }
 
                         storedVideo.setDescription(video.getDescription());
                         storedVideo.setDownloadedOn(BackupTubeCommon.getTimeString(BackupTubeCommon.getCurrentTime()));
                         storedVideo.setHasBeenDeleted(false);
                         storedVideo.setPublishedOn(BackupTubeCommon.getTimeString(video.getPublished()));
                         storedVideo.setSiteInfo(siteInfo);
                         storedVideo.setTitle(video.getTitle());
                         storedVideo.setUploader(video.getUploader());
                         storedVideo.setUrl(video.getURL());
 
                         try {
                             storedVideo.saveHTML(singleVideoDataFeedFile, videoFileName, CLIENT_TYPE, CLIENT_VERSION);
                         } catch (FileNotFoundException ex) {
                             ex.printStackTrace();
                         }
 
                         // now update the main JSON file since the file has been downloaded
 
                         // created the ListedVideo object
                         ListedVideo listedVideo;
 
                         if (video.getSiteID().equals(BackupHelper.SITE_YOUTUBE)) {
                             listedVideo = new ListedYouTubeVideo();
                         } else {
                             listedVideo = new ListedVideo();
                         }
 
                         listedVideo.setTitle(video.getTitle());
                         listedVideo.setDownloadedTime(BackupTubeCommon.getCurrentTime());
                         listedVideo.setFolderName(videoFolder);
                         listedVideo.setSiteID(video.getSiteID());
                         listedVideo.setVideoID(video.getVideoID());
 
                         // get the current data object
                         BackupTubeDataFile dataFile;
 
                         try {
                             dataFile = getDataFile(dataFeedFile);
                         } catch (FileNotFoundException ex) {
                             dataFile = new BackupTubeDataFile();
                         }
 
                         LinkedList<ListedVideo> storedVideos = dataFile.getVideos();
 
                         if (storedVideos == null) {
                             storedVideos = new LinkedList();
                         }
 
                         storedVideos.add(listedVideo);
                         
                         dataFile.setLastUpdated(BackupTubeCommon.getCurrentTime());
                         dataFile.setVideos(storedVideos);
                         
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
