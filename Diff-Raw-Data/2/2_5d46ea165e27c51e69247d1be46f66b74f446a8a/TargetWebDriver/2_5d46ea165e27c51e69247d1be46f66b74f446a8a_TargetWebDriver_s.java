 package com.dynacrongroup.webtest.driver;
 
 import com.dynacrongroup.webtest.conf.SauceLabsCredentials;
 import com.dynacrongroup.webtest.conf.WebtestConfigFactory;
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Throwables;
 import com.typesafe.config.Config;
 import org.openqa.selenium.Capabilities;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Constructor;
 import java.util.UUID;
 
 /**
  * User: yurodivuie
  * Date: 6/2/13
  * Time: 4:26 PM
  */
 public class TargetWebDriver {
 
     /**
      * Unique identifier for this job run. Global value for entire suite
      * execution (i.e. corresponds to a single complete mvn clean verify)
      */
     protected static final String UNIQUE_ID = String.valueOf(UUID.randomUUID());
 
     @VisibleForTesting
     protected static final String WEBDRIVER_BROWSER_PATH = "webdriver.browser";
 
     @VisibleForTesting
     protected static final String WEBDRIVER_TYPE_PATH = "webdriver.type";
 
     @VisibleForTesting
     protected static final String WEBDRIVER_VERSION_PATH = "webdriver.version";
 
     @VisibleForTesting
     protected static final String WEBDRIVER_PLATFORM_PATH = "webdriver.platform";
 
     @VisibleForTesting
     protected static final String WEBDRIVER_CAPABILITIES_PATH = "webdriver.capabilities";
 
     @VisibleForTesting
     protected static final String SAUCELABS_TIMEOUT_PATH = "saucelabs.timeout";
 
     private static final Logger LOG = LoggerFactory.getLogger(TargetWebDriver.class);
 
     private final Class testClass;
 
     private final Browser browser;
 
     private final Type type;
 
     private final String version;
 
     private final Platform platform;
 
     private final DesiredCapabilities capabilities;
 
     public TargetWebDriver(Class testClass, Browser browser, Type type, String version, Platform platform, DesiredCapabilities capabilities) {
         this.testClass = testClass;
         this.browser = browser;
         this.type = type;
         this.version = version;
         this.platform = platform;
         this.capabilities = capabilities;
     }
 
     public TargetWebDriver(Class testClass) {
         Config config = WebtestConfigFactory.getConfig(testClass);
 
         this.testClass = testClass;
         this.browser = getBrowserFrom(config);
         this.type = getTypeFrom(config);
 
         if (isLocal()) {
             this.version = "";
             this.platform = Platform.getCurrent();
         } else {
             this.version = getVersionFrom(config);
             this.platform = getPlatformFrom(config);
         }
 
         this.capabilities = getCapabilitiesFrom(config);
     }
 
     public Class getTestClass() {
         return testClass;
     }
 
     public Browser getBrowser() {
         return browser;
     }
 
     public Type getType() {
         return type;
     }
 
     public String getVersion() {
         return version;
     }
 
     public Platform getPlatform() {
         return platform;
     }
 
     public DesiredCapabilities getCapabilities() {
         return capabilities;
     }
 
     public Boolean isLocal() {
         return type.equals(Type.LOCAL);
     }
 
     public Boolean isRemote() {
         return type.equals(Type.REMOTE);
     }
 
     public WebDriver build() {
         WebDriver driver = null;
         if (isLocal()) {
             driver = buildLocal();
         }
         else {
             driver = buildRemote();
         }
         return driver;
     }
 
     private WebDriver buildLocal() {
         WebDriver driver = null;
         try {
             LOG.debug("building {} for {}", browser.getDriverClass(), getTestClass());
             Constructor constructor = browser.getDriverClass().getDeclaredConstructor(Capabilities.class);
             driver = (WebDriver) constructor.newInstance(getCapabilities());
         } catch (Exception ex) {
             Throwables.propagate(ex);
         }
         return driver;
     }
 
     private WebDriver buildRemote() {
         LOG.debug("building remote {} for {}", browser.toString(), getTestClass());
         return new RemoteWebDriver(SauceLabsCredentials.getConnectionLocation(), getCapabilities());
     }
 
     private Browser getBrowserFrom(Config config) {
         return Browser.fromJson(config.getString(WEBDRIVER_BROWSER_PATH));
     }
 
     private Type getTypeFrom(Config config) {
         return Type.fromJson(config.getString(WEBDRIVER_TYPE_PATH));
     }
 
     private String getVersionFrom(Config config) {
         return config.getString(WEBDRIVER_VERSION_PATH);
     }
 
     private Platform getPlatformFrom(Config config) {
         return Platform.valueOf(config.getString(WEBDRIVER_PLATFORM_PATH).toUpperCase());
     }
 
     private DesiredCapabilities getCapabilitiesFrom(Config config) {
         DesiredCapabilities newCapabilities = buildDefaultCapabilities(config);
         if (config.hasPath(WEBDRIVER_CAPABILITIES_PATH)) {
            newCapabilities = new DesiredCapabilities(config.getObject(WEBDRIVER_CAPABILITIES_PATH).unwrapped());
         }
         return newCapabilities;
     }
 
     private DesiredCapabilities buildDefaultCapabilities(Config config) {
         DesiredCapabilities defaultCapabilities = new DesiredCapabilities();
         defaultCapabilities.setJavascriptEnabled(true);
         if (isRemote()) {
             defaultCapabilities.setBrowserName(getBrowser().name().toLowerCase());
             defaultCapabilities.setVersion(getVersion());
             defaultCapabilities.setPlatform(getPlatform());
             defaultCapabilities.setCapability("name", getTestClass().getSimpleName());
             defaultCapabilities.setCapability("build", UNIQUE_ID);
             defaultCapabilities.setCapability("command-timeout", config.getNumber(SAUCELABS_TIMEOUT_PATH).toString());    //default is 300 - may need to revisit.
         }
         return defaultCapabilities;
     }
 }
