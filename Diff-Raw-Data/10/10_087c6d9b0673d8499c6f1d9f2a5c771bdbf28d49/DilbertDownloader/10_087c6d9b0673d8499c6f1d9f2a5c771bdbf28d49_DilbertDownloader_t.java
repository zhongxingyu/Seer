 package dilbertdownloader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Eugene Susla
  */
 public class DilbertDownloader {
 
     private static final String beginSequence = "src=\"/dyn/str_strip/";
     private static final String endSequence = ".gif\"";
     private static final String targetFolder = "D:\\pics\\fun\\Dilbert";
     
     private static volatile String input = "";
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         InputStreamReader converter = new InputStreamReader(System.in);
         BufferedReader in = new BufferedReader(converter);
         
         System.out.println("\nDownloading lastest comics:");
         downloadLastest();
         
         Thread thread = new Thread(new Runnable() {
             public void run() {
                 System.out.println("\nDownloading comics:");
                 Calendar counter = findFirstPresentComic();
                 int comicsDownloaded = 0;
                 while (!Thread.interrupted()) {
                     downloadComic(counter, false, true);
                     counter.add(Calendar.DAY_OF_MONTH, -1);
                     comicsDownloaded++;
                 }
                 System.out.println("Finished downloading. Comics downloaded: " 
                         + comicsDownloaded);
             }
         });
         thread.start();
         input = "";
         while (input.isEmpty()) {
             try {
                 input = in.readLine();
             } catch (IOException ex) {
                 Logger.getLogger(DilbertDownloader.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         thread.interrupt();
     }
 
     /**
      * 
      * @param date yyyy-mm-dd
      * @return URL to png
      */
     private static String getURL(String date) {
         String html;
         try {
             html = util.net.Http.getResponsePart("http://dilbert.com/strips/" + date + "/", 
                     beginSequence, endSequence);
         } catch (IOException ex) {
             return null;
         }
         int beginIndex = html.indexOf(beginSequence) + beginSequence.indexOf('/');
         int endIndex = html.indexOf(endSequence, beginIndex) + endSequence.indexOf('\"');
         String result = "http://dilbert.com" + html.substring(beginIndex, endIndex);
         return result;
     }
 
     /**
      * 
      * @param date yyyy-mm-dd
      * @return 
      */
     private static boolean isComicForDateDownloaded(String date) {
         String file = targetFolder + "\\" + date.substring(0, 4) + "\\" + date + ".gif";
         return new File(file).exists();
     }
 
     private static boolean isComicForDateDownloaded(Calendar date) {
         return isComicForDateDownloaded(calendarToString(date));
     }
 
     private static boolean downloadComic(String date, boolean checkForAlreadyDownloaded,
             boolean verbose) {
         String year = date.substring(0, 4);
         String folderName = targetFolder + "\\" + year;
         String fileName = folderName + "\\" + date + ".gif";
         if (checkForAlreadyDownloaded && (new File(fileName)).exists()) {
             return false;
         }
         String url = getURL(date);
         try {
             (new File(folderName)).mkdir();
             util.net.Http.writeResponseToFile(url, fileName);
             if (verbose) System.out.println("Comic downloaded for date: " + date);
             return true;
         } catch (Exception ex) {
             System.out.println("Error downloading comic for date " + date + " : " + ex);
             return false;
         }
     }
 
     private static boolean downloadComic(Calendar date, boolean checkForAlreadyDownloaded, 
             boolean verbose) {
         return downloadComic(calendarToString(date), checkForAlreadyDownloaded, verbose);
     }
 
     private static String calendarToString(Calendar cal) {
         String result = "" + cal.get(Calendar.YEAR)
                 + "-" + String.format("%02d", cal.get(Calendar.MONTH) + 1)
                 + "-" + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
         return result;
     }
 
     /**
      * downloads up to <code>limit</code> lastest comics starting from 
      * today's day, going backwards
      * @param limit 
      */
     private static void downloadLastest(int limit) {
         Calendar counter = Calendar.getInstance();  //today
         int count = 0;
         while (!isComicForDateDownloaded(counter) && (limit <= 0 || count < limit)) {
             downloadComic(counter, false, true);
             counter.add(Calendar.DAY_OF_MONTH, -1);
             count++;
             if (counter.get(Calendar.HOUR) >= 11) {
                 counter.set(Calendar.HOUR, 1);
             }
         }
     }
     
     private static void downloadLastest() {
         downloadLastest(0);
     }
 
     //TODO: Algorithm flawed - sadly can't use any binary search here. use linear
     private static Calendar findFirstPresentComic() {
         Calendar counter = Calendar.getInstance();
         int daysAdded = 1;
         Calendar firstPresent = Calendar.getInstance();
         while (isComicForDateDownloaded(counter)) {
             counter.add(Calendar.DAY_OF_MONTH, -daysAdded);
             daysAdded *= 2;
         }
         daysAdded /= 2;
         firstPresent.setTime(counter.getTime());
         firstPresent.add(Calendar.DAY_OF_MONTH, daysAdded);
         Calendar lastMissing = counter;
 
         Calendar middle = Calendar.getInstance();
         do {
             daysAdded /= 2;
             middle.setTime(lastMissing.getTime());
             middle.add(Calendar.DAY_OF_MONTH, daysAdded);
             if (isComicForDateDownloaded(middle)) {
                 firstPresent.setTime(middle.getTime());
             } else {
                 lastMissing.setTime(middle.getTime());
             }
         } while (daysAdded > 1);
 
         //DEBUG
         if (!isComicForDateDownloaded(firstPresent)) {
             System.out.println("Assertion failed: not present in firstPresent");
         }
 
         if (isComicForDateDownloaded(lastMissing)) {
             System.out.println("Assertion failed: present in lastMissing");
         }
 
         lastMissing.add(Calendar.DAY_OF_MONTH, 1);
         if (!firstPresent.getTime().equals(lastMissing.getTime())) {
             System.out.println("Assertion failed: dates not 1 day apart");
             System.out.println(firstPresent.getTime());
             System.out.println(lastMissing.getTime());
         }
 
         lastMissing.set(Calendar.HOUR, 0);
         lastMissing.set(Calendar.MINUTE, 1);
         return lastMissing;
     }
 }
