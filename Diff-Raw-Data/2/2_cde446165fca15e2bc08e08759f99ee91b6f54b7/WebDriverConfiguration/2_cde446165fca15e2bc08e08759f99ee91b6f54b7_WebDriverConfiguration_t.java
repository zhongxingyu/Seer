 package com.olo.initiator;
 
 import java.io.File;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.android.AndroidDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxBinary;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.iphone.IPhoneDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.testng.ITestResult;
 
 import com.olo.util.OSUtil;
 import com.opera.core.systems.OperaDriver;
 
 public class WebDriverConfiguration {
 	
 	protected DesiredCapabilities getInternetExplorerCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.internetExplorer();
 		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getFirefoxCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.firefox();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getChromeCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.chrome();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getOperaCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.opera();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getAndroidCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.android();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getIphoneCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.iphone();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getIpadCapabilities(){
 		DesiredCapabilities capabilities = null;
 		capabilities = DesiredCapabilities.ipad();
 		return capabilities;
 	}
 	
 	protected DesiredCapabilities getCapabilities(String browser) throws Exception{
 		DesiredCapabilities capabilities = null;
 		if(browser.equals("Firefox")){
 			capabilities = getFirefoxCapabilities();
 		}else if(browser.equals("Explorer")){
 			capabilities = getInternetExplorerCapabilities();
 		}else if(browser.equals("Chrome")){
 			capabilities = getChromeCapabilities();
 		}else if(browser.equals("Opera")){
 			capabilities = getOperaCapabilities();
 		}else if(browser.equals("Android")){
 			capabilities = getAndroidCapabilities();
 		}else if(browser.equals("Iphone")){
 			capabilities = getIphoneCapabilities();
 		}else if(browser.equals("Ipad")){
 			capabilities = getIpadCapabilities();
 		}else{
 			throw new Exception("Un Supported Browser");
 		}
 		return capabilities;
 	}
 	
 	protected WebDriver getInternetExplorerDriver(DesiredCapabilities capabilities){
 		return new InternetExplorerDriver(capabilities);
 	}
 	
 	protected WebDriver getFirefoxDriver(DesiredCapabilities capabilities){
 		FirefoxBinary binary = new FirefoxBinary();
 		FirefoxProfile profile = new FirefoxProfile();
 		profile.setAcceptUntrustedCertificates(true);
 		return new FirefoxDriver(binary,profile,capabilities);
 	}
 	
 	protected WebDriver getChromeDriver(DesiredCapabilities capabilities){
 		return new ChromeDriver(capabilities);
 	}
 	
 	protected WebDriver getOperaDriver(DesiredCapabilities capabilities){
 		return new OperaDriver(capabilities);
 	}
 	
 	protected WebDriver getAndroidDriver(DesiredCapabilities capabilities){
 		return new AndroidDriver(capabilities);
 	}
 	
 	protected WebDriver getIphoneDriver(DesiredCapabilities capabilities) throws Exception{
 		return new IPhoneDriver(capabilities);
 	}
 	
 	protected WebDriver getRemoteWebDriverDriver(String hubURL, DesiredCapabilities capabilities) throws Exception{
 		return new RemoteWebDriver(new URL(hubURL),capabilities);
 	}
 	
 	protected WebDriver getDriver(String browser, DesiredCapabilities capabilities) throws Exception{
 		if(browser.equals("Firefox")){
 			return getFirefoxDriver(capabilities);
 		}else if(browser.equals("Explorer")){
			System.setProperty("webdriver.ie.driver", System.getProperty("user.dir")+"/drivers/win"+OSUtil.getJavaBitVersion()+"/IEDriverServer.exe");
 			return getInternetExplorerDriver(capabilities);
 		}else if(browser.equals("Chrome")){
 			String driverFolder=null;
 			String javaBitVersion = OSUtil.getJavaBitVersion();
 			if(Platform.getCurrent().is(Platform.WINDOWS)){
 				driverFolder="win"+javaBitVersion;
 			}else if(Platform.getCurrent().is(Platform.MAC)){
 				driverFolder="mac"+javaBitVersion;
 			}else{
 				driverFolder="linux"+javaBitVersion;
 			}
 			System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"/drivers/"+driverFolder+"/chromedriver.exe");
 			return getChromeDriver(capabilities);
 		}else if(browser.equals("Opera")){
 			return getOperaDriver(capabilities);
 		}else if(browser.equals("Android")){
 			return getAndroidDriver(capabilities);
 		}else if(browser.equals("Iphone") || browser.equals("Ipad")){
 			return getIphoneDriver(capabilities);
 		}else{
 			throw new Exception("Unsupported Browser");
 		}
 	}
 	
 	protected void takeScreenShotForTest(ITestResult result,WebDriver driver) throws Exception{
 		String screenShotFileName=System.currentTimeMillis()+".png";
 		String screenShotPath=result.getTestContext().getOutputDirectory()+"/"+"screenshots"+"/"+screenShotFileName;
 		File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
 		FileUtils.copyFile(srcFile, new File(screenShotPath));
 		result.setAttribute("screenshot", screenShotFileName);
 	}
 	
 	protected void setWaitForPageToLoadInSec(WebDriver driver,long sec){
 		driver.manage().timeouts().pageLoadTimeout(sec, TimeUnit.SECONDS);
 	}
 	
 	protected void setImplicitWait(WebDriver driver,long sec){
 		driver.manage().timeouts().implicitlyWait(sec, TimeUnit.SECONDS);
 	}
 	
 	protected void windowMaximizeAndWindowFocus(WebDriver driver){
 		driver.manage().window().maximize();
 		driver.switchTo().window(driver.getWindowHandle());
 	}
 	
 	protected void openUrlAndDeleteCookies(WebDriver driver,String url){
 		driver.get(url);
 		driver.manage().deleteAllCookies();
 	}
 	
 }
