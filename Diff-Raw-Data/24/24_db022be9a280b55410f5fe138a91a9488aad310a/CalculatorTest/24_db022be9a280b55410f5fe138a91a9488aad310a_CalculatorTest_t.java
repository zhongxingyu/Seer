 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2012, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.examples.richrates.ui;
 
 import java.net.URL;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.drone.api.annotation.Drone;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.arquillian.testng.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @since 4.2.0
  */
 public class CalculatorTest extends Arquillian {
 
     @Drone
     private WebDriver driver;
     @ArquillianResource
     private URL deploymentURL;
 
     public static final String WEBAPP_SRC = "src/main/webapp";
 
     // TODO include manifest.mf with org.slf4j dependency
     @Deployment(testable = false)
     public static WebArchive createTestArchive() {
         MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom(
             "pom.xml");
 
         WebArchive war = ShrinkWrap.create(WebArchive.class, "richrates.war");
 
         war.addPackage("org.richfaces.examples.richrates");
         war.addPackage("org.richfaces.examples.richrates.annotation");
         war.addPackages(true, "org.slf4j");
 
         war.merge(ShrinkWrap.create(ExplodedImporter.class, "tmp1.war").importDirectory("src/main/webapp")
             .as(WebArchive.class));
         war.merge(ShrinkWrap.create(ExplodedImporter.class, "tmp2.war").importDirectory("src/main/resources")
             .as(WebArchive.class), "WEB-INF/classes");
 
         war.addAsLibraries(resolver.artifact("org.richfaces.ui:richfaces-components-ui").resolveAsFiles());
         war.addAsLibraries(resolver.artifact("org.richfaces.core:richfaces-core-impl").resolveAsFiles());
         war.addAsLibraries(resolver.artifact("org.jdom:jdom").resolveAsFiles());
         war.addAsLibraries(resolver.artifact("joda-time:joda-time").resolveAsFiles());
 
         return war;
     }
 
     public ExpectedCondition<WebElement> visibilityOfElementLocated(final By locator) {
         return new ExpectedCondition<WebElement>() {
             public WebElement apply(WebDriver driver) {
                 WebElement toReturn = driver.findElement(locator);
                 if (toReturn.isDisplayed()) {
                     return toReturn;
                 }
                 return null;
             }
         };
     }
 
     public ExpectedCondition<WebElement> notEmptyText(final By locator) {
         return new ExpectedCondition<WebElement>() {
             public WebElement apply(WebDriver driver) {
                 WebElement toReturn = driver.findElement(locator);
                 if (!toReturn.getText().equals("")) {
                     return toReturn;
                 }
                 return null;
             }
         };
     }
 
     @Test
    public void testCalculate() {
         driver.get(deploymentURL.toString());
 
         WebDriverWait wait = new WebDriverWait(driver, 5000);
 
        WebElement amountInput = driver.findElement(By.id("calculator:amount")); 
        amountInput.clear();
        amountInput.sendKeys("33");
        amountInput.submit();
        
         wait.until(notEmptyText(By.cssSelector("div.result")));
 
         String result = driver.findElement(By.cssSelector("div.result")).getText();
 
        Assert.assertTrue(result.matches("33.000 EUR = \\d+\\.\\d{3} USD"),
            "Result should start match expression \"33.000 EUR = \\d+\\.\\d{3} USD\".");
        Assert.assertNotEquals(result, "33.000 EUR = 0.000 USD", "Exchange rate for USD should not be 0.");
     }
 }
