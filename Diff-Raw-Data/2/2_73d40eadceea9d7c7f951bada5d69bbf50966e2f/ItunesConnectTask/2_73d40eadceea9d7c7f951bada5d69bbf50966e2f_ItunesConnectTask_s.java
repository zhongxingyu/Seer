 package com.triposo.automator.itunesconnect;
 
 import com.triposo.automator.Task;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 
 import java.io.FileNotFoundException;
 import java.util.Iterator;
 import java.util.Map;
 
 public abstract class ItunesConnectTask extends Task {
   @Override
   protected Map getGuides() throws FileNotFoundException {
     Map guides = super.getGuides();
     Iterator guidesIterator = guides.entrySet().iterator();
     while (guidesIterator.hasNext()) {
       Map.Entry guideEntry = (Map.Entry) guidesIterator.next();
       Map guide = (Map) guideEntry.getValue();
       Map ios = (Map) guide.get("ios");
       if (ios == null) {
         guidesIterator.remove();
       }
     }
     return guides;
   }
 
   protected Integer getAppleIdOfGuide(Map guide) {
     Map ios = (Map) guide.get("ios");
     return (Integer) ios.get("apple_id");
   }
 
   protected MainPage gotoItunesConnect() {
     driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
       SigninPage signinPage = new SigninPage(driver);
       signinPage.signin(getProperty("itunes.username"), getProperty("itunes.password"));
     }
     try {
       WebElement continueButton = driver.findElement(By.cssSelector("img.customActionButton"));
       continueButton.click();
     } catch (NoSuchElementException e) {
     }
     return new MainPage(driver);
   }
 
   protected AppSummaryPage gotoAppSummary(Integer appleId) {
     ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
     SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
     return searchResultPage.clickFirstResult();
   }
 
   protected class VersionMissingException extends Throwable {
     public VersionMissingException(String s) {
       super(s);
     }
   }
 
   protected class MostRecentVersionRejectedException extends Throwable {
   }
 }
