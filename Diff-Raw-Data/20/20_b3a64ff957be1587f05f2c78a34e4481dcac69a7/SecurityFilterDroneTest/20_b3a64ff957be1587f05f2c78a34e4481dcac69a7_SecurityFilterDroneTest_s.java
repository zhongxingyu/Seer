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
 
 import com.thoughtworks.selenium.DefaultSelenium;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.drone.api.annotation.Drone;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.net.URL;
 
 import static junit.framework.Assert.*;
 
 /**
  * First example of a Selenium test.
  *
  * @author fstab
  */
 @RunWith(Arquillian.class)
 public class SecurityFilterDroneTest {
 
 
     @Deployment(testable = false)
     public static Archive<?> createDeployment() {
         WebArchive war = Deployments.createFullDeployment();
         System.out.println(war.toString(true));
         return war;
     }
 
     @Drone
    DefaultSelenium browser;
 
     @ArquillianResource
     URL deploymentUrl;
 
     /**
      * Anonymous access to upload.xhtml should be redirected
      * to login.xhtml by the {@link net.mymam.security.SecurityFilter}.
      */
     @Test
     public void testUploadPageRequiresLogin() {
        browser.open(deploymentUrl + "index.xhtml");
        browser.click("navbar:nav-upload-link");
        assertTrue(browser.isElementPresent("loginform"));
     }
 }
