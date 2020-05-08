 package com.triposo.automator;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import org.openqa.selenium.*;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 public abstract class Task {
   protected WebDriver driver;
   private final Properties properties = new Properties();
 
   public final void run() {
     try {
       properties.load(new FileInputStream("local.properties"));
 
       if (getProperty("browser", "firefox").equals("chrome")) {
         driver = new ChromeDriver();
       } else {
         driver = new FirefoxDriver();
       }
       driver.manage().window().setSize(new Dimension(1200, 1000));
       driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 
       doRun();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   protected String getProperty(String name) {
     if (System.getProperties().containsKey(name)) {
       return System.getProperty(name);
     } else if (properties.containsKey(name)) {
       return properties.getProperty(name);
     } else {
       throw new IllegalStateException(
           String.format("Need to specify property %s on command line or as a system property.", name));
     }
   }
 
   protected String getProperty(String name, String defaultValue) {
     try {
       return getProperty(name);
     } catch (IllegalStateException e) {
       return defaultValue;
     }
   }
 
   protected Map getAllGuides() throws FileNotFoundException {
     Yaml yaml = new Yaml();
     String yamlPath = getProperty("yaml", "../triposo3/pipeline/config/guides.yaml");
     File yamlFile = new File(yamlPath);
     return (Map) yaml.load(new FileInputStream(yamlFile));
   }
 
   protected Map getAllValidGuides() throws FileNotFoundException {
     Map guides = getAllGuides();
     Iterator guidesIterator = guides.entrySet().iterator();
     while (guidesIterator.hasNext()) {
       Map.Entry guideEntry = (Map.Entry) guidesIterator.next();
       Map guide = (Map) guideEntry.getValue();
       if (!isGuideValid(guide)) {
         guidesIterator.remove();
       }
     }
     return guides;
   }
 
   protected abstract boolean isGuideValid(Map guide);
 
   /**
    * Get the guides that should be touched.
    *
    * The only.guides property can be used to specify a list of guide ids
    * separated by space/comma.
    */
   protected Map getGuides() throws FileNotFoundException {
     Map guides = getAllValidGuides();
     List<String> only = getOnly();
     if (only.isEmpty()) {
       return guides;
     }
     Iterator guidesIterator = guides.entrySet().iterator();
     while (guidesIterator.hasNext()) {
       Map.Entry guideEntry = (Map.Entry) guidesIterator.next();
       if (!only.contains(guideEntry.getKey())) {
         guidesIterator.remove();
       }
     }
     return guides;
   }
 
   private List<String> getOnly() {
     String only = getProperty("only.guides", "");
     if (only.trim().length() == 0) {
       // Avoid returning a list with a single empty string.
       return Lists.newArrayList();
     }
     return Lists.newArrayList(only.split("[\\s,]"));
   }
 
   protected List<File> getGuideScreenshots(File dir) {
     if (!dir.isDirectory()) {
       // No biggie.
       System.out.println("Screenshots directory missing: " + dir);
       return Lists.newArrayList();
     }
     File doneFile = getDoneFileForScreenshotsDir(dir);
     if (doneFile.exists()) {
       System.out.println("Already uploaded: " + dir);
      System.out.println("(Delete " + dir.getAbsolutePath() + " if incorrect.)");
       return Lists.newArrayList();
     }
     List<File> images = Lists.newArrayList(dir.listFiles());
     Collections.sort(images);
     return Lists.newArrayList(
         Iterators.filter(images.iterator(),
         new Predicate<File>() {
           @Override
           public boolean apply(File file) {
             return file != null && file.getName().endsWith(".png");
           }
         }));
   }
 
   private File getDoneFileForScreenshotsDir(File dir) {
     return new File(dir, "DONE");
   }
 
   protected void markGuideScreenshotsUploaded(File dir) {
     touch(getDoneFileForScreenshotsDir(dir));
   }
 
   private void touch(File doneFile) {
     try {
       FileOutputStream out = new FileOutputStream(doneFile);
       out.close();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   public abstract void doRun() throws Exception;
 }
