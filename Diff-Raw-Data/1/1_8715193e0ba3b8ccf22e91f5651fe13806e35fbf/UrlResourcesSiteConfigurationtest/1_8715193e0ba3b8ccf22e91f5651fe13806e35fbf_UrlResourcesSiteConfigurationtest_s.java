 // Copyright (2006-2007) Schibsted Søk AS
 /*
  * FileResourcesSearchTabsCreatorTest.java
  *
  * Created on 22 January 2006, 16:05
  */
 
 package no.schibstedsok.searchportal.mode.config;
 
 
 import java.util.Properties;
 import no.schibstedsok.searchportal.site.SiteTestCase;
 import no.schibstedsok.searchportal.site.config.SiteConfiguration;
 import no.schibstedsok.searchportal.site.Site;
 import org.testng.annotations.Test;
 import static org.testng.AssertJUnit.*;
 
 
 /**
  * Tests using SearchTabsCreator against URL-based configuration files.
  * Only to be run when an application server is up and running.
  * 
  * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
  * @version $Id: UrlResourcesSiteConfigurationtest.java 3359 2006-08-03 08:13:22Z mickw $
  */
 public final class UrlResourcesSiteConfigurationtest extends SiteTestCase {
 
     private static final String FAIL_CONFIG_NOT_RUNNING =
             "\n\n"
             + "Unable to obtain configuration resources from search-front-config. \n"
             + "Please start this service before trying to build/deploy search-front-html."
             + "\n";
 
     public UrlResourcesSiteConfigurationtest(final String testName) {
         super(testName);
     }	     
     
     /**
      * Test of valueOf method, of class no.schibstedsok.searchportal.configuration.SiteConfiguration.
      */
     @Test
     public void testDefaultSite() {
 
         final Site site = Site.DEFAULT;
 
         final SiteConfiguration result = SiteConfiguration.valueOf(site);
         assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
     }
 
     /**
      * Test of getProperties method, of class no.schibstedsok.searchportal.configuration.SiteConfiguration.
      */
     @Test
     public void testDefaultSiteGetProperties() {
 
         final SiteConfiguration instance = SiteConfiguration.valueOf(Site.DEFAULT);
 
         final Properties result = instance.getProperties();
         assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
     }
 
 }
