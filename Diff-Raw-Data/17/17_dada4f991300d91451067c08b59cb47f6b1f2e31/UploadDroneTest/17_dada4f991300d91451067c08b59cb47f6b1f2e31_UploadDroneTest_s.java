 /* MyMAM - Open Source Digital Media Asset Management.
  * http://www.mymam.net
  *
  * Copyright 2013, MyMAM contributors as indicated by the @author tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.mymam.server.test;
 
 import net.mymam.entity.Access;
 import net.mymam.fileprocessor.exceptions.RestCallFailedException;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.drone.api.annotation.Drone;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.concurrent.TimeUnit;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author fstab
  */
 @RunWith(Arquillian.class)
 public class UploadDroneTest {
 
 
     @Deployment(testable = false)
     public static Archive<?> createDeployment() {
         WebArchive war = Deployments.createFullDeployment();
         // uncomment the following line in order to see a list of deployed files on STDOUT.
         // System.out.println(war.toString(true));
         // uncomment the following line int order to write a copy of the deployment to a file.
         // war.as(ZipExporter.class).exportTo(new File("/tmp/testdeployment.war"), true);
         return war;
     }
 
     @Drone
     WebDriver driver;
 
     @ArquillianResource
     URL deploymentUrl;
 
     private static void login(WebDriver driver) {
         driver.findElement(By.id("navbar:login")).click();
         driver.findElement(By.id("loginform:username")).sendKeys("admin");
         driver.findElement(By.id("loginform:password")).sendKeys("admin");
         driver.findElement(By.id("loginform:submit")).click();
     }
 
     private static void upload(WebDriver driver, Path path) {
         assertTrue(path.toFile().exists());
         driver.findElement(By.id("navbar:nav-upload-link")).click();
         WebElement fileInputBtn = driver.findElement(By.cssSelector("#fileupload input[type='file']"));
         // <input type="file"> must be visible, otherwise Selenium cannot interact with it.
         // Make opacity > 0 and transform = none in order to mark it as visible.
         ((JavascriptExecutor)driver).executeScript("arguments[0].style.opacity = 1; arguments[0].style.transform = 'none';", fileInputBtn);
         fileInputBtn.sendKeys(path.toAbsolutePath().toString());
         // Submit upload form by clicking on the submit button (default form submit is disabled in mymam-upload.js)
         driver.findElement(By.cssSelector("#fileupload input.submit-button")).click();
     }
 
    private static void fillMetadataForm(WebDriver driver) {
         driver.findElement(By.id("navbar:nav-dashboard-link")).click();
         driver.findElement(By.id("newly-imported-videos:data-table:0:edit-button")).click();
        driver.findElement(By.id("metadata:title")).sendKeys("test title");
         new Select(driver.findElement(By.id("metadata:access"))).selectByValue(Access.PUBLIC.toString());
         driver.findElement(By.id("metadata:done")).click();
     }
 
     private Path makeDummyMp4() throws IOException {
         Path mp4File = Files.createTempFile("test", ".mp4");
         try ( OutputStream stream = Files.newOutputStream(mp4File) ) {
             stream.write("This is not really mp4, never mind.".getBytes());
             return mp4File;
         }
     }
 
     /**
     * Anonymous access to upload.xhtml should be redirected
     * to login.xhtml by the {@link net.mymam.security.SecurityFilter}.
      */
     @Test
     public void testUpload() throws RestCallFailedException, IOException {
         driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
         driver.get(deploymentUrl + "index.xhtml");
         login(driver);
         upload(driver, makeDummyMp4());
         driver.findElement(By.id("navbar:nav-dashboard-link")).click();
         assertTrue(driver.findElement(By.id("content")).getText().contains("1 new file is currently being imported."));
         FileProcessorSimulator.runImport(new URL(deploymentUrl, "rest"));
        fillMetadataForm(driver);
         driver.findElement(By.id("navbar:nav-home-link")).click();
        // TODO: Assert that video is available on start page.
     }
 }
