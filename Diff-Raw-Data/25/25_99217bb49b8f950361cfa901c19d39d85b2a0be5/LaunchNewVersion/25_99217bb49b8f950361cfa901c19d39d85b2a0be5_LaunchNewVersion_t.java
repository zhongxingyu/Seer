 package com.triposo.automator.androidmarket;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class LaunchNewVersion {
 
   private WebDriver driver;
   private final Properties properties = new Properties();
 
   public static void main(String[] args) throws Exception {
     try {
       new LaunchNewVersion().run();
     } finally {
       System.exit(0);
     }
   }
 
   public void run() throws Exception {
     properties.load(new FileInputStream("local.properties"));
 
     Logger logger = Logger.getLogger("");
     logger.setLevel(Level.OFF);
     driver = new FirefoxDriver();
     driver.manage().window().setSize(new Dimension(1200, 1000));
     driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 
    String whatsnew = "* new suggestion engine\n" +
        "* bug fixes";
 
    String versionName = "1.6.6";
    String versionCode = "126";
    String sheetName = "1_7";
     String folderName = sheetName + "_" + versionName;
     File apksFolder = new File("../../Dropbox/apks/" + folderName);
 
     Yaml yaml = new Yaml();
     Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
     for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
       Map.Entry entry = (Map.Entry) iterator.next();
       String location = (String) entry.getKey();
       Map guide = (Map) entry.getValue();
       if (Boolean.TRUE.equals(guide.get("apk"))) {
         System.out.println("Processing " + location);
 
         try {
           launchNewVersion(location, guide, apksFolder, versionCode, whatsnew);
         } catch (Exception e) {
           e.printStackTrace();
           System.out.println("Error processing, skipping: " + location);
         }
 
         System.out.println("Done " + location);
       }
     }
 
     System.out.println("All done.");
   }
 
   private void launchNewVersion(String location, Map guide, File versionRoot, String versionCode, String recentChanges) {
     String packageName = "com.triposo.droidguide." + location.toLowerCase();
     AppEditorPage appEditorPage = gotoAppEditor(packageName);
     appEditorPage.clickApkFilesTab();
     String appName = (String) guide.get("app_name");
     if (appName == null) {
       appName = location;
     }
     File apkFile = new File(versionRoot, appName + ".apk");
     if (!apkFile.isFile()) {
       System.out.println("Could not find apk for " + location + ". Skipping.");
       return;
     }
     appEditorPage.uploadApk(versionCode, apkFile);
     appEditorPage.clickActivate(versionCode);
     appEditorPage.clickProductDetailsTab();
     appEditorPage.enterRecentChanges(recentChanges);
     appEditorPage.theLegalBlahBlah();
    appEditorPage.enterPrivacyPolicyLink("http://triposo.com/tandc.html");
     appEditorPage.clickSave();
   }
 
   private AppEditorPage gotoAppEditor(String packageName) {
     driver.get("https://market.android.com/publish/Home#AppEditorPlace:p=" + packageName);
     if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
       SigninPage signinPage = new SigninPage(driver);
       signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
       driver.get("https://market.android.com/publish/Home#AppEditorPlace:p=" + packageName);
     }
     return new AppEditorPage(driver);
   }
 }
