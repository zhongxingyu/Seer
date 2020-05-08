 package de.holidayinsider.skimmy;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 
 /**
  * User: martinstolz
  * Date: 03.07.12
  */
 public class SkimmyTest {
 
     private Properties uriList = new Properties();
     private Properties hosts = new Properties();
 
     private String suiteName;
     private String targetServer;
 
     public SkimmyTest(String suiteName, String host) {
         this.suiteName = suiteName;
 
         try {
             uriList.load(new FileInputStream(new File("suites/" + suiteName + "/urilist.properties")));
             hosts.load(new FileInputStream(new File("suites/" + suiteName + "/hosts.properties")));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         this.targetServer = hosts.getProperty(host);
         if (targetServer == null) {
             throw new IllegalArgumentException("domain not found! " + host);
         }
     }
 
     /**
      * Runs the tests.
      */
     public void runTests() {
 
         File wantedDir = new File("suites/" + suiteName + "/wanted");
         if (!wantedDir.exists()) {
             wantedDir.mkdirs();
         }
 
         // check wanted pictures exist for all uris
         for (Object keyObj : uriList.keySet()) {
             String key = (String) keyObj;
             String uri = buildUri(uriList.getProperty(key));
             String wantedFile = "suites/" + suiteName + "/wanted/" + key + ".png";
             File wanted = new File(wantedFile);
             // get initial images for those urls and put them in wanted.
             if (!wanted.exists()) {
                 generateImage(key, uri, "suites/" + suiteName + "/wanted");
             }
         }
 
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         String runTargetDir = "suites/" + suiteName + "/runs/" + format.format(new Date());
 
         SkimmyReport report = new SkimmyReport();
         report.setSuiteName(suiteName);
         report.setHostName(targetServer);
 
         new File(runTargetDir).mkdirs();
 
         // for all uris
         List failed = new ArrayList<String>();
         for (Object keyObj : uriList.keySet()) {
             String key = (String) keyObj;
             String url = buildUri(uriList.getProperty(key));
 
             SkimmyReportItem item = new SkimmyReportItem();
             item.setKey(key);
             item.setTimestamp(System.currentTimeMillis());
             item.setUrl(url);
             report.getItems().add(item);
 
             // load wanted image
             String wantedImgFile = "suites/" + suiteName + "/wanted/" + key + ".png";
             BufferedImage wantedImg = loadImage(wantedImgFile);
 
             // relative path of report.html to the wanted images...
             item.setWantedImgPath("../../wanted/" + key + ".png");
 
             // load live image into suite run dir
             generateImage(key, url, runTargetDir);
             BufferedImage currentImg = loadImage(runTargetDir + "/" + key + ".png");
 
             item.setCurrentImgPath(key + ".png");
 
             int width = Math.min(wantedImg.getWidth(), currentImg.getWidth());
             int height = Math.min(wantedImg.getHeight(), currentImg.getHeight());
 
             boolean failure = false;
             int totalPixel = 0;
             int failedPixel = 0;
 
             // if the width differs more than 30% its failed in any case.
             if (Math.abs(width - wantedImg.getWidth()) / width > 0.1 || Math.abs(height - wantedImg.getHeight()) / height > 0.1) {
                 failure = true;
                totalPixel = 1;
             } else {
                 for (int x = 0; x < width; x++) {
                     for (int y = 0; y < height; y++) {
                         int wantedRGB = wantedImg.getRGB(x, y);
                         int currentRGB = 1;
                         if (currentImg.getHeight() > y && currentImg.getWidth() > x) {
                             currentRGB = currentImg.getRGB(x, y);
                         }
 
                         // if its pitch black or blacked out we dont want to compare it.
                         if (wantedRGB == 0xFF000000) {
                             continue;
                         }
 
                         if (wantedRGB != currentRGB) {
                             failure = true;
                             failedPixel ++;
                             currentImg.setRGB(x, y, 0xFFFF0000);
                         }
                         totalPixel ++;
                     }
 
                 }
 
                 if (failure) {
                     failure = false;
                     for (int x = 0; x < width; x++) {
                         for (int y = 0; y < height; y++) {
                             int rgb = currentImg.getRGB(x, y);
 
                             if (rgb == 0xFFFF0000) {
                                 failure = failure || checkAreaForFail(x, y, currentImg);
                             }
 
                             if (failure) {
                                 break;
                             }
                         }
                     }
                 }
             }
 
             if (failure) {
                 File failImage = new File(runTargetDir + "/failed");
                 failImage.mkdirs();
                 failImage = new File(failImage, key + ".png");
 
                 item.setFailed(true);
                 item.setFailedImgPath("failed/" + key + ".png");
 
                 // make all non-failed areas very much lighter to make the error stand out...
                 for (int x = 0; x < currentImg.getWidth(); x++) {
                     for (int y = 0; y < currentImg.getHeight(); y++) {
                         int rgb = currentImg.getRGB(x, y);
                         if (rgb != 0xFFFF0000) {
                             // make shadow of the page content so you see the errors better.
                             Color c = new Color(rgb);
                             c = c.brighter();
                             // dont make it too bright
                             if (c.getRGB() != 0xFFFFFFFF) {
                                 c = c.brighter();
                             }
                             rgb = c.getRGB();
                             currentImg.setRGB(x, y, rgb);
                         }
                     }
                 }
 
                 try {
                     ImageIO.write(currentImg, "png", failImage);
                 }  catch (IOException io) {
                     throw new RuntimeException(io);
                 }
 
                 StringBuilder build = new StringBuilder();
                 build.append("FAIL " + url + "\n");
                 build.append(" -> " + ((failedPixel / totalPixel) * 100)  + " percent");
                 build.append(" -> saved to " + failImage.getPath());
                 failed.add(build.toString());
             }
         }
 
         // generate report and fin.
         try {
             generateReport(report, runTargetDir);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 
     public void generateReport(Object model, String targetDir) throws Exception {
         Configuration cfg = new Configuration();
         ////cfg.setDirectoryForTemplateLoading(
             ////    new File("/where/you/store/templates"));
         cfg.setObjectWrapper(new DefaultObjectWrapper());
         cfg.setTemplateLoader(new FlexibleTemplateLoader());
         Template tpl = cfg.getTemplate("results.ftl");
         tpl.process(model, new FileWriter(targetDir + "/report.html"));
     }
 
 
 
     /**
      * Checks all surrounding pixels for a failure.
      * Only if a certain percentage is "red" we decide its really a failure
      * to account for anti-aliasing differences which generate false positives.
      *
      * @param xPoint
      * @param yPoint
      * @param image
      * @return
      */
     private boolean checkAreaForFail(int xPoint, int yPoint, BufferedImage image) {
         int failCount = 0;
 
         int startX = xPoint - 1;
         int startY = yPoint - 1;
 
         int endX = xPoint + 1;
         int endY = yPoint + 1;
 
         if (startX < 0) startX = 0;
         if (endX > image.getWidth() - 1) endX = image.getWidth() -1;
 
         if (startY < 0) startY = 0;
         if (endY > image.getWidth() - 1) endX = image.getWidth() -1;
 
         for (int x = startX; x <= endX; x++) {
                 for (int y = startY; y <= endY; y++) {
                     int rgb = image.getRGB(x, y);
                     if (rgb == 0xFFFF0000) {
                         failCount ++;
                     }
                 }
         }
         return failCount > 7;
     }
 
     /**
      * Does neccessary replacements in the uri to account for dates etc.
      * @param uri
      * @return
      */
     private String buildUri(String uri) {
         uri = uri.replace("{DOMAIN}", targetServer);
         return uri;
     }
 
     /**
      * Well, it loads an image.
      * @param file
      * @return
      */
     private BufferedImage loadImage(String file) {
         try {
             BufferedImage in = ImageIO.read(new File(file));
             return in;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Calls webkit2png to generate a image from the given url to be saved in targetDir
      * with the given tag.
      *
      * @param tag
      * @param url
      * @param targetDir
      * @return
      */
     private boolean generateImage(String tag, String url, String targetDir) {
         StringBuilder cmd = new StringBuilder();
         cmd.append("python src/main/resources/webkit2png.py -F -W 1280 -H 1024 --delay 10 ");
         cmd.append("-o current");
         cmd.append(" ");
         cmd.append(url);
 
         System.out.println("Fetching image: " + url);
         execute(cmd.toString());
         execute("mv current-full.png " + targetDir + "/" + tag + ".png");
         return true;
     }
 
     /**
      * A wrapper function for executing shell commands.
      *
      * @param command
      */
     private void execute(String command) {
         try {
 
             System.out.println("=> Execute " + command);
             Process process = Runtime.getRuntime().exec(command);
             int c;
             InputStream in = process.getInputStream();
             while ((c = in.read()) != -1) {
                 System.out.print((char) c);
             }
             in.close();
             System.out.println("=> Execute finished");
             process.waitFor();
             if (process.exitValue() != 0) {
                 throw new RuntimeException("execute failed " + command + " with " + process.exitValue());
             }
 
         } catch (Exception e) {
             throw new RuntimeException("execute failed " + command + " with exception", e);
         }
     }
 
 
 }
