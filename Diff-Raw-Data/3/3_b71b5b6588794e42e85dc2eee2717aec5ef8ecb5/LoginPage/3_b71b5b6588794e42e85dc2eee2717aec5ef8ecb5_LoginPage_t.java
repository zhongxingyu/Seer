 package tests.adSDK.page;
 
 import org.apache.log4j.Logger;
 import org.openqa.selenium.TimeoutException;
 
 import tests.adSDK.page.exceptions.AdSDKLoadException;
 import tests.adSDK.page.exceptions.AdSDKToolbarException;
 
 import com.annotation.FindBy;
 import com.element.UIView;
 import com.mobile.driver.nativedriver.NativeDriver;
 import com.mobile.driver.page.PageFactory;
 import com.mobile.driver.wait.Sleeper;
 
 public class LoginPage extends BasePage{
 	
 	private static final Logger LOGGER = Logger.getLogger(LoginPage.class);
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/textfield[1]")
 	private UIView loginTextfield;
 	
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/secure[1]")
 	private UIView passwordTextfield;
 	
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/slider[1]")
 	private UIView savePasswordSlider;
 	
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[1]")
 	private UIView loginButton;
 	
 	@FindBy(locator = "//window[2]/toolbar[1]/button[1]")
 	private UIView doneButton;
 	
 	public LoginPage(NativeDriver driver) {
 		this.driver = driver;
 	}
 	
 	public CallScreen simpleLogin(String login, String password) {
		loginTextfield.touch();
 		loginTextfield.type(login);
		passwordTextfield.touch();
 		passwordTextfield.type(password);
 		savePasswordSlider.touch();
 		loginButton.touch();
 		return  PageFactory.initElements(driver, CallScreen.class);
 	}
 	
 	@Override
 	public void checkPage() {
 		loginTextfield.waitForElement(WAIT_FOR_ELEMENT_TIMEOUT);
 		
 	}
 
 }
