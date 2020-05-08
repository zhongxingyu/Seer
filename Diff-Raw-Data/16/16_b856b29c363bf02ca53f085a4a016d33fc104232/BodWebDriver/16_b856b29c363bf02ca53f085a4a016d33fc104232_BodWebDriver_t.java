 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.support;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.not;
 
 import java.io.File;
 import java.util.concurrent.TimeUnit;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.pages.physicalresourcegroup.ListPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.physicalresourcegroup.NewPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.virtualresourcegroup.ListVirtualResourceGroupPage;
 import nl.surfnet.bod.pages.virtualresourcegroup.NewVirtualResourceGroupPage;
 
 import org.hamcrest.Matcher;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import com.google.common.io.Files;
 
 public class BodWebDriver {
 
   private static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url",
       "http://localhost:8080/bod"));
 
   private FirefoxDriver driver;
 
   public synchronized void initializeOnce() {
     if (driver == null) {
       this.driver = new FirefoxDriver();
       this.driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
       Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
           if (driver != null) {
             driver.quit();
           }
         }
       });
     }
   }
 
   public void takeScreenshot(File screenshot) throws Exception {
     if (driver != null) {
       File temp = driver.getScreenshotAs(OutputType.FILE);
       Files.copy(temp, screenshot);
     }
   }
 
   public void createNewPhysicalGroup(String name) throws Exception {
     NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.sendName(name);
     page.sendInstitution("Utrecht");
 
     page.save();
   }
 
   /**
    * Shortcut, to login the given user. Heavily depends on the
    * {@link ShibbolethImitatorInterceptor} being active!
    */
   public void performLogin(String userName) {
     driver.get(URL_UNDER_TEST + "?user-name=" + userName + "&name-id=urn:collab:person:surfguest.nl:" + userName);
   }

   private static String withEndingSlash(String path) {
     return path.endsWith("/") ? path : path + "/";
   }
 
   public void deletePhysicalGroup(PhysicalResourceGroup group) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.deleteByName(group.getName());
   }
 
   public void verifyGroupWasCreated(String name) {
     assertListTable(containsString(name));
   }
 
   public void verifyGroupWasDeleted(PhysicalResourceGroup group) {
     assertListTable(not(containsString(group.getName())));
   }
 
   private void assertListTable(Matcher<String> tableMatcher) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
     String row = page.getTable();
 
     assertThat(row, tableMatcher);
   }
 
   public NewVirtualResourceGroupPage createNewVirtualResourceGroup(String name) throws Exception {
     NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.sendSurfConnextGroupName(name);
     page.save();

     return page;
   }
 
   public void deleteVirtualResourceGroup(VirtualResourceGroup vrg) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.deleteByName(vrg.getSurfConnextGroupName());
   }
 
   public void verifyVirtualResourceGroupWasCreated(String name) {
     assertVirtualResourceGroupListTable(containsString(name));
   }
 
   public void verifyVirtualResourceGroupWasDeleted(VirtualResourceGroup vrg) {
     assertVirtualResourceGroupListTable(not(containsString(vrg.getSurfConnextGroupName())));
   }

   public void verifyVirtualResourceGroupSurfConnextGroupNameHasError(){

   }
 
   private void assertVirtualResourceGroupListTable(Matcher<String> tableMatcher) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
     String row = page.getTable();
 
     assertThat(row, tableMatcher);
   }

 }
