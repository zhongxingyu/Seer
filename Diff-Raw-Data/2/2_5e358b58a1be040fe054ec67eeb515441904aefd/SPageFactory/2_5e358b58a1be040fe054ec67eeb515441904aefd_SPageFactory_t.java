 /*
 This file is part of the Sulfur project by Ivan De Marino (http://ivandemarino.me).
 
 Copyright (c) 2013, Ivan De Marino (http://ivandemarino.me)
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package sulfur.factories;
 
 import sulfur.SPage;
 import sulfur.factories.exceptions.*;
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import org.apache.log4j.Logger;
 import org.openqa.selenium.WebDriver;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Ivan De Marino
  *
  * #factory
  * #singleton
  *
  * TODO
  */
 public class SPageFactory {
     /** Logger */
     private static final Logger LOG = Logger.getLogger(SPageFactory.class);
 
     /** MANDATORY System Property to instruct Sulfur where to look for the Config file */
     public static final String SYSPROP_CONFIG_FILE_PATH = "sulfur.config";
 
     private final SConfig mConfig;
     private final SPageConfigFactory mPageConfigFactory;
 
     private static SPageFactory singleton = null;
 
     private SPageFactory() {
         // Read configuration file location
         String configFilePath = System.getProperty(SYSPROP_CONFIG_FILE_PATH);
         if (null == configFilePath) {
             throw new SConfigNotProvidedException();
         }
 
         // Parse configuration file
         Gson gson = new Gson();
         try {
             FileReader configFileReader = new FileReader(configFilePath);
             mConfig = gson.fromJson(configFileReader, SConfig.class);
 
             // Logging
             LOG.debug("FOUND Sulfur Config file: " + configFilePath);
             mConfig.logDebug(LOG);
         } catch (FileNotFoundException fnfe) {
             LOG.error("INVALID Config (not found)");
             throw new SInvalidConfigException(configFilePath);
         } catch (JsonSyntaxException jse) {
             LOG.error("INVALID Config (malformed)");
             throw new SInvalidConfigException(configFilePath, jse);
         }
 
         // Fetch a SPageConfigFactory
         mPageConfigFactory = SPageConfigFactory.getInstance();
 
         LOG.debug("Available Pages: " + getAvailablePageConfigs());
     }
 
     /**
      * Factory Method
      *
      * @return The SPageFactory
      */
     public synchronized static SPageFactory getInstance() {
         if (null == singleton) {
             singleton = new SPageFactory();
         }
 
         return singleton;
     }
 
     /**
      * Utility method to get rid of the SPageFactory Singleton Instance.
      * NOTE: Make sure you know what you are doing when using this.
      */
     public synchronized static void clearInstance() {
         singleton = null;
     }
 
     /**
      * Creates a SPage.
      * It validates the input parameters, checking if the requested driver exists, if the page exists and the
      * mandatory path and query parameters are all provided.
      *
      * NOTE: The returned SPage hasn't loaded yet, so the User can still operate on it before the initial HTTP GET.
      *
      * @param driverName Possible values are listed in @see SWebDriverFactory
      * @param pageName Name of the SPage we want to open. It must be part of the given SPageConfig(s)
      * @param pathParams Map of parameters that will be set in the SPage URL Path (@see SPageConfig)
      * @param queryParams Map of parameters that will be set in the SPage URL Query (@see SPageConfig)
      * @return A "ready to open" SPage object
      */
     public SPage createPage(String driverName,
                            String pageName,
                            Map<String, String> pathParams,
                            Map<String, String> queryParams) {
 
         // Validate Driver Name
         if (!getConfig().getDrivers().contains(driverName)) {
             throw new SUnavailableDriverException(driverName);
         }
         // Validate SPage Name
         if (!getAvailablePageConfigs().contains(pageName)) {
             throw new SUnavailablePageException(pageName);
         }
 
         // Fetch required SPageConfig
         SPageConfig pageConfig = mPageConfigFactory.getPageConfig(pageName);
 
         // Compose URL Path & Query to the SPage
         String urlPath = pageConfig.composeUrlPath(pathParams);
         String urlQuery = pageConfig.composeUrlQuery(queryParams);
 
         // Create the requested driver
         WebDriver driver = SWebDriverFactory.createDriver(driverName);
 
         // Create the destination URL
         String initialUrl;
         try {
             initialUrl = new URL(mConfig.getProtocol(), mConfig.getHost(), mConfig.getPort(), urlPath + "?" + urlQuery).toString();
         } catch (MalformedURLException mue) {
             LOG.fatal(String.format("FAILED to compose the URL to the Page '%s'", pageName), mue);
             throw new SFailedToCreatePageException(mue);
         }
 
         // Create and return the new SPage
         try {
             return new SPage(driver, initialUrl, pageConfig.getComponentClassnames());
         } catch (Exception e) {
             // In case something goes wrong when creating the SPage, it's important we "quit()" the driver.
             // We don't want Browser instances hanging around
            LOG.fatal(String.format("EXCEPTION thrown while trying to create Page '%s'", pageName), e);
             driver.quit();
             throw e;
         }
     }
 
     public SPage createClonePage(SPage pageToClone) {
         return new SPage(pageToClone);
     }
 
     public SPage createNextPage(SPage currentPage, String pageName) {
         // Fetch required SPageConfig
         SPageConfig pageConfig = mPageConfigFactory.getPageConfig(pageName);
         // Build a new page using the same driver as "currentPage"
         return new SPage(currentPage.getDriver(), pageConfig.getComponentClassnames());
     }
 
     public Set<String> getAvailablePageConfigs() {
         return mPageConfigFactory.getPageConfigs().keySet();
     }
 
     /**
      * @return The Sulfur Configuration currently used by the SPageFactory
      */
     public SConfig getConfig() {
         return mConfig;
     }
 }
