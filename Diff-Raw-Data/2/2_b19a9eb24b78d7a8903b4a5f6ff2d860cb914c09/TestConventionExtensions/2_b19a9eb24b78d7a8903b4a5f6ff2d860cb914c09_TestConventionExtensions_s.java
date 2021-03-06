 package it.com.atlassian.labs.speakeasy;
 
 import com.atlassian.pageobjects.TestedProduct;
 import com.atlassian.pageobjects.page.HomePage;
 import com.atlassian.pageobjects.page.LoginPage;
 import com.atlassian.plugin.test.PluginJarBuilder;
 import com.atlassian.webdriver.pageobjects.WebDriverTester;
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  *
  */
 public class TestConventionExtensions
 {
     private static TestedProduct<?> product = OwnerOfTestedProduct.INSTANCE;
 
     @Before
     public void login()
     {
         product.visit(LoginPage.class).loginAsSysAdmin(HomePage.class);
     }
 
     @After
     public void logout()
     {
         ((WebDriverTester)product.getTester()).getDriver().manage().deleteAllCookies();
     }
 
     @Test
     public void testBasicConventionPlugin() throws IOException, URISyntaxException
     {
          File jar = new PluginJarBuilder("ConventionZip")
                 .addFormattedResource("atlassian-extension.json",
                         "{'key'         : 'test-convention',",
                         " 'version'      : '1'",
                         "}")
                 .addResource("js/", "")
                 .addResource("js/test/", "")
                 .addFile("js/test/foo.js", new File(getClass().getResource("/archetype/main.js").toURI()))
                 .addResource("css/", "")
                 .addFile("css/test-convention.css", new File(getClass().getResource("/archetype/main.css").toURI()))
                 .addResource("images/", "")
                 .addFile("images/projectavatar.png", new File(getClass().getResource("/archetype/projectavatar.png").toURI()))
                 .addResource("ui/", "")
                 .addFile("ui/web-items.json", new File(getClass().getResource("/archetype/web-items.json").toURI()))
                 .buildWithNoManifest();
         File zip = new File(jar.getPath() + ".zip");
         FileUtils.moveFile(jar, zip);
 
         product.visit(SpeakeasyUserPage.class)
                 .uploadPlugin(zip)
                 .enablePlugin("test-convention");
 
         SpeakeasyUserPage page = product.visit(SpeakeasyUserPage.class);
         HiBanner banner = product.getPageBinder().bind(HiBanner.class);
         assertTrue(banner.isFooVisible());
         assertTrue(banner.isFooImageLoaded());
         assertTrue(banner.isYahooLinkAvailable());
         assertFalse(banner.isBarVisible());
 
        assertEquals(asList("css/test-convention.css", "images/projectavatar.png", "js/test/", "js/test/foo.js", "ui/web-items.json", "atlassian-extension.json"),
                 page.openEditDialog("test-convention").getFileNames());
 
         page.uninstallPlugin("test-convention");
     }
 
 }
