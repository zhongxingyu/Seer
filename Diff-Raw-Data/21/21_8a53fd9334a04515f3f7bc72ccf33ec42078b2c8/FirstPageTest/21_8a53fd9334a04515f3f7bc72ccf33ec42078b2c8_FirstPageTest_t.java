 package be.cegeka.rsvz.ui;
 
 import org.glassfish.embeddable.GlassFish;
 import org.glassfish.embeddable.GlassFishProperties;
 import org.glassfish.embeddable.GlassFishRuntime;
 import org.glassfish.embeddable.archive.ScatteredArchive;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
import java.io.IOException;
 
 public class FirstPageTest {
     private static final Logger LOG = LoggerFactory.getLogger(FirstPageTest.class);
     private GlassFish glassfish;
 
     @Before
     public void startServer() throws Exception {
         GlassFishProperties gfProps = new GlassFishProperties();
         gfProps.setPort("http-listener", 9999);
         glassfish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
         glassfish.start();
        System.out.println(getLocalPath());
        File webRoot = new File(getLocalPath() + "/src/main/webapp");
        File classes = new File(getLocalPath() + "/target/classes");
         ScatteredArchive archive = new ScatteredArchive("web", ScatteredArchive.Type.WAR, webRoot);
         archive.addClassPath(classes);
         glassfish.getDeployer().deploy(archive.toURI());
 
     }
 
     @Test
     public void languageLabelShouldBeLanguage() {
         WebDriver driver = new HtmlUnitDriver();
         driver.get("http://localhost:9999/web/index.xhtml");
         LOG.debug("current url: {}", driver.getCurrentUrl());
         System.out.println("current url: " + driver.getCurrentUrl());
         LOG.debug("current title: {}", driver.getTitle());
         WebElement element = driver.findElement(By.id("languageLabel"));
         Assert.assertEquals("Language", element.getText());
     }
 
     @After
     public void closeServer() throws Exception {
         glassfish.stop();
         glassfish.dispose();
     }

    private String getLocalPath() throws IOException {
        String canonicalPath = new File(".").getCanonicalPath();
        if (canonicalPath.indexOf("richfaces") > 0) {
            return canonicalPath;
        } else {
            return canonicalPath + "/richfaces";
        }
    }

 }
