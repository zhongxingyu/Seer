 package org.jsystem.webdriver_so.generators;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.commons.lang3.StringUtils;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.chrome.ChromeOptions;
 
 public class ChromeWebDriverGenerator implements WebDriverGenerator {
 
 	@Override
 	public WebDriver getWebDriver(WebDriverConfiguration config) throws FileNotFoundException {
 
 		WebDriver driver = null;
 		String chromeDriverPath;
 		ChromeOptions options = new ChromeOptions();
 		List<String> switches = new ArrayList<String>();
 
 		chromeDriverPath = config.getExecutable();
 
 		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
 
		if (!StringUtils.isEmpty(config.getProfile())) {
 			switches.add("--user-data-dir=" + config.getProfile());
 		}
 
		if (!StringUtils.isEmpty(config.getExtension())) {
 			switches.add("--load-extension=" + config.getExtension());
 		}
 
 		if (config.isWindowMaximize() == true) {
 			switches.add("--start-maximized");
 		}
 
 		if (config.isIgnoreCertificateErrors() == true) {
 			switches.add("--ignore-certificate-errors");
 		}
 
 		if (config.getExtraFlags() != null) {
 			String[] flags = config.getExtraFlags().split(",");
 			for (String flag : flags) {
 				if (flag.startsWith("--") == false) {
 					flag = "--" + flag;
 				}
 				switches.add(flag);
 			}
 		}
 
 		if (switches.size() > 0) {
 			options.addArguments(switches);
 			driver = new ChromeDriver(options);
 		} else {
 			driver = new ChromeDriver();
 		}
 		return driver;
 	}
 
 }
