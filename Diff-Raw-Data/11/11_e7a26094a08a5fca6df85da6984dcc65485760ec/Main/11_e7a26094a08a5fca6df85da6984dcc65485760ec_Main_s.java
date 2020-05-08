 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.regex.*;
 
 public class Main {
 
     private static final Pattern pattern = Pattern.compile("https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?");
     private static List<String> endings = new ArrayList<>();
 
     static {
         Main.endings.add("_1280");
         Main.endings.add("_700");
         Main.endings.add("_700");
         Main.endings.add("_500");
         Main.endings.add("_400");
         Main.endings.add("_250");
         Main.endings.add("_100");
     }
     private static Gui gui = null;
     private static HashMap<Picture, Picture> pic_pic_hash = new HashMap<>();
     private static List<URL> checked = new ArrayList<>();
     private static String blogname = "";
     private static File blogdir = null;
 
     public static Set<Picture> getPictures() {
         return Main.pic_pic_hash.keySet();
     }
 
     public static File getBlogDir() {
         return Main.blogdir;
     }
 
     public static void setBlogName(String blogname) {
         Main.blogname = blogname;
         Main.blogdir = new File("." + File.separator + Main.blogname + File.separator);
     }
 
     public static void main(String[] args) {
         if (args.length == 0) {
             gui = new Gui();
             gui.setVisible(true);
         } else if (args.length == 3 || args.length == 4) {
             Main.setBlogName(args[0]);
             Main.load();
             if (args.length == 3) {
                 int start, limit;
                 try {
                     start = Integer.parseInt(args[1]);
                     limit = Integer.parseInt(args[2]);
                 } catch (Exception e) {
                     Main.status("usage: Main <blogname> <start_page> <end_page> -hires");
                     Main.status("start_page and end_page must be integers >= 1");
                     return;
                 }
                 Main.run(start, limit);
                 Main.save();
             } else if (args.length == 4 && args[3].equals("-hires")) {
                 Main.downloadHiRes();
                 Main.save();
             } else {
                 Main.status("usage: Main <blogname> <start_page> <end_page> -hires");
             }
         } else {
             Main.status("usage: Main <blogname> <start_page> <end_page> -hires");
         }
     }
 
     public static void reset() {
         Helper.removeDirectoryIfItExists(Helper.temp);
         Helper.removeDirectoryIfItExists(blogdir);
         Main.pic_pic_hash.clear();
         Main.checked.clear();
     }
 
     public static void run(int start_page, int end_page) {
         if (start_page < 1 || end_page < 1) {
             Main.status("start_page and end_page must be integers >= 1");
             return;
         }
         int progress = 0;
         if (gui != null) {
             gui.setProgress(progress);
         }
         if (end_page >= start_page) {
             if (gui != null) {
                 gui.setMaxProgress(end_page - start_page);
             }
             for (int i = start_page; i <= end_page; i++) {
                 Main.handleURL(String.format("http://%s.tumblr.com/page/%s", Main.blogname, i));
                 if (gui != null) {
                     gui.setProgress(progress);
                     progress++;
                 }
             }
         } else {
             if (gui != null) {
                 gui.setMaxProgress(start_page - end_page);
             }
            for (int i = end_page; i >= start_page; i--) {
                 Main.handleURL(String.format("http://%s.tumblr.com/page/%s", Main.blogname, i));
                 if (gui != null) {
                     gui.setProgress(progress);
                     progress++;
                 }
             }
         }
         if (gui != null) {
             gui.setProgress(progress);
         }
     }
 
     public static void load() {
         Main.status("Loading databases.");
         File file = new File(blogdir, "picpic.db");
         List<Object> objects = Helper.loadObjectFromFile(file);
         if (objects == null || objects.size() != 2) {
             Main.error("Unable to load database files so creating new database.");
             reset();
         } else {
             Main.pic_pic_hash = (HashMap<Picture, Picture>) objects.get(0);
             Main.checked = (List<URL>) objects.get(1);
         }
         Main.status("Done loading databases.");
     }
 
     public static void save() {
         Main.status("Saving databases.");
         File file = new File(blogdir, "picpic.db");
         List<Object> objects = new ArrayList<>();
         objects.add(Main.pic_pic_hash);
         objects.add(Main.checked);
         Helper.saveObjectToFile(file, objects);
         Main.status("Done saving databases.");
     }
 
     private static void handleURL(String address) {
         try {
             Main.status(String.format("Processing page \"%s\".", address));
             URL url = new URL(address);
             String html = Helper.downloadHTMLURLtoString(url);
             if (html != null) {
                 List<URL> urls = getUrls(html);
                 List<URL> media_urls = getMediaUrls(urls);
                 List<Picture> get_pics = getPictures(media_urls);
                 for (Picture picture : get_pics) {
                     if (!Main.checked.contains(url)) {
                         Helper.downloadFileFromURLToFileInTemp(picture.thumb_url, picture.thumb_name);
                         picture.md5_id = Helper.createMD5FromFileInTemp(picture.thumb_name);
                         Helper.moveTempImageToStore(picture.thumb_name, new File(Main.blogdir, picture.md5_id));
                         if (!Main.pic_pic_hash.containsKey(picture)) {
                             Main.pic_pic_hash.put(picture, picture);
                             Helper.downloadFileFromURLToFileInTemp(picture.media_url, picture.media_name);
                             Helper.moveTempImageToStore(picture.media_name, new File(Main.blogdir, picture.md5_id));
                         }
                         Main.checked.add(picture.thumb_url);
                     }
                 }
             }
         } catch (MalformedURLException ex) {
             Main.error(String.format("Page address \"%s\" is a malformed URL.", address));
         }
     }
 
     private static List<URL> getUrls(String html) {
         Matcher matcher = pattern.matcher(html);
         List<URL> urls = new ArrayList<>();
         String match = "";
         while (matcher.find()) {
             try {
                 match = matcher.group();
                 urls.add(new URL(match));
             } catch (MalformedURLException ex) {
                 Main.error(String.format("Matched URL \"%s\" is a malformed URL.", match));
             }
         }
         return urls;
     }
 
     private static List<URL> getMediaUrls(List<URL> urls) {
         List<URL> media_urls = new ArrayList<>();
         for (URL url : urls) {
             if (url.getHost().contains(".media.tumblr.com") && url.getFile().contains("/tumblr_")) {
                 media_urls.add(url);
             }
         }
         return media_urls;
     }
 
     private static List<Picture> getPictures(List<URL> urls) {
         List<Picture> pictures = new ArrayList<>();
         String thumb = "";
         for (URL url : urls) {
             try {
                 String media_url = url.toString();
                 String type = media_url.substring(media_url.lastIndexOf("."));
                 if (type.equals(".jpg") || type.equals(".jpeg") || type.equals(".gif") || type.equals(".png")) {
                 } else {
                     Main.error(String.format("Unknown file type \"%s\" addressed by URL \"%s\".", type, url));
                 }
                 thumb = media_url.replace(media_url.substring(media_url.lastIndexOf("_"), media_url.lastIndexOf(".")), "_75sq");
                 URL thumb_url = new URL(thumb);
                 pictures.add(new Picture(url, thumb_url));
             } catch (MalformedURLException ex) {
                 Main.error(String.format("Attempted thumbnail URL \"%s\" is a malformed URL.", thumb));
             }
         }
         return pictures;
     }
 
     public static void downloadHiRes() {
         Main.status("Downloading hi res versions of photos in database.");
         if (gui != null) {
             gui.setProgress(0);
             gui.setMaxProgress(pic_pic_hash.keySet().size());
         }
         int progress = 0;
         for (Picture picture : pic_pic_hash.keySet()) {
             if (!picture.downloaded_hi) {
                 tryResUrls(picture);
             }
             if (gui != null) {
                 gui.setProgress(progress);
                 progress++;
             }
         }
         if (gui != null) {
             gui.setProgress(progress);
             progress++;
         }
         Main.status("Done downloading hi res versions.");
     }
 
     private static void tryResUrls(Picture picture) {
         String hi_res = "";
         String url = picture.media_url.toString();
         for (String ending : Main.endings) {
             try {
                 hi_res = url.replace(url.substring(url.lastIndexOf("_"), url.lastIndexOf(".")), ending);
                 URL hi_url = new URL(hi_res);
                 File hi_name = Helper.extractMediaFileNameFromURL(hi_url);
                 if (hi_name.equals(picture.media_name)) {
                     picture.hi_url = hi_url;
                     picture.hi_name = hi_name;
                     picture.downloaded_hi = true;
                     break;
                 } else {
                     boolean success = Helper.downloadFileFromURLToFileInTemp(hi_url, hi_name);
                     if (success) {
                         picture.hi_url = hi_url;
                         picture.hi_name = hi_name;
                         picture.downloaded_hi = true;
                         Helper.moveTempImageToStore(hi_name, new File(Main.blogdir, picture.md5_id));
                         break;
                     }
                 }
             } catch (MalformedURLException ex) {
                 Main.error(String.format("Attempted hi res url %s is a malformed URL.", hi_res));
             }
         }
     }
 
     private static void status(String status) {
         if (gui == null) {
             System.out.println(status);
         } else {
             gui.setStatus(status);
         }
     }
 
     private static void error(String error) {
         if (gui == null) {
             System.err.println(error);
         } else {
             gui.setStatus(error);
         }
     }
 }
