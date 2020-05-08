 package tests.appiumTests.ios;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 
 import runner.DeviceConfig;
 import runner.Devices;
 import tests.page.CallPage;
 import tests.page.LoginPage;
 import tests.page.SettingsPage;
 import tests.page.android.CallPageAndroid;
 import tests.page.android.LoginPageAndroid;
 import tests.page.exceptions.XmlParametersException;
 import tests.page.ios.CallPageIos;
 import tests.page.ios.LoginPageIos;
import tests.page.ios.SettingsPageIos;
 import utils.ApplicationStorage;
 
 import com.ios.AppiumDriver;
 import com.mobile.driver.nativedriver.NativeDriver;
 import com.mobile.driver.page.PageFactory;
 import com.mobile.driver.wait.Sleeper;
 
 import driver.IosDriverWrapper;
 
 public class NonAutorizationBaseTest {
 	protected static final String DELIMETER = ":";
 
 	private static final String HOST = DeviceConfig.getHost();
 
 	private static final String PORT = DeviceConfig.getPort();
 	
 	private static final String DEVICE = DeviceConfig.getDevice();
 	
 	protected static final String USER_NAME = ApplicationStorage.getDefaultUsername();//"sip:skustov2@sipnet.ru";
 	
 	protected static final String USER_PASSWORD = ApplicationStorage.getDefaultPassword();//"zzzzzz";
 	
 	protected static final String INCORRECT_USER_NAME = "7812001245@211.195.68.250";
 	
 	protected static final String INCORRECT_PASSWORD = "70mNZcEy05G123";
 
 	protected NativeDriver driver;
 
 	protected LoginPage main;
 	
 	protected CallPage call;
 	
 	protected SettingsPage settings;
 	
 	
 	@BeforeClass(description = "Init and check page")
 	public void initPages() throws Exception {
 		
 		switch (Devices.valueOf(DEVICE)) {
 		case IPHONE:
 			driver = IosDriverWrapper.getIphone(HOST, PORT);
 			main = PageFactory.initElements(driver, LoginPageIos.class);
 			call = PageFactory.initElements(driver, CallPageIos.class);
			settings = PageFactory.initElements(driver, SettingsPageIos.class);
 			break;
 		case ANDROID:
 			driver = IosDriverWrapper.getAndroid(HOST, PORT);
 			main = PageFactory.initElements(driver, LoginPageAndroid.class);
 			call = PageFactory.initElements(driver, CallPageAndroid.class);
 			break;
 		default:
 			throw new XmlParametersException("Invalid device");
 		}
 
 	}
 	
 	@AfterClass
 	public void tearDown() throws Exception {
 		AppiumDriver.class.cast(driver).quit();
 	}
 
 }
