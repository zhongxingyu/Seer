 package br.com.dextra.expertus.environment;
 
 import java.util.HashMap;
 import java.util.Map;
 
import org.apache.commons.lang3.StringUtils;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import com.saucelabs.saucerest.SauceREST;
 
 /**
  * This is an abstract class that represents a test environment at SauceLabs.
  *
  * @author leandro.guimaraes
  *
  */
 public abstract class SauceEnvironment extends Environment {
 
 	protected static final String BROWSER_VERSION_CAPABILITY = "version";
 
 	protected static final String PLATFORM_CAPABILITY = "platform";
 
 	protected String platform;
 
 	protected String browserVersion;
 
 	protected String sauceUsername;
 
 	protected String sauceKey;
 
 	@Override
 	public String getSessionTestId() {
 		return ((RemoteWebDriver) this.driver).getSessionId().toString();
 	}
 
 	protected boolean isAllPropertiesOk() {
 		return !(StringUtils.isEmpty(this.platform) || StringUtils.isEmpty(this.browser)
 				|| StringUtils.isEmpty(this.browserVersion) || StringUtils.isEmpty(this.sauceUsername) || StringUtils
 					.isEmpty(this.sauceKey));
 	}
 
 	@Override
 	public void setSessionName(String sessionName) {
 		SauceREST sauceRest = new SauceREST(this.sauceUsername, this.sauceKey);
 
 		Map<String, Object> mapInfo = new HashMap<String, Object>();
 		mapInfo.put("name", sessionName);
 
		sauceRest.updateJobInfo(this.getSessionTestId(), mapInfo);
 	}
 
 }
