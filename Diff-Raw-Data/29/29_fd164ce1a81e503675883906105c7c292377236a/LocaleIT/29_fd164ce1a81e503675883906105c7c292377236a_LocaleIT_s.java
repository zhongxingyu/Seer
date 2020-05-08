 package com.gmail.at.zhuikov.aleksandr.it;
 
 import static junit.framework.Assert.assertEquals;
 import static org.openqa.selenium.support.PageFactory.initElements;
 
 import org.junit.Test;
 
 import com.gmail.at.zhuikov.aleksandr.it.page.LoginPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.OrdersPage;
 
 public class LocaleIT extends AbstractWebDriverTest {
 
//	@Override
//	protected WebDriver createDriver() {
//		FirefoxProfile profile = new FirefoxProfile();
//		profile.setPreference("intl.accept_languages", "ru");
//        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
//        capabillities.setCapability("version", "5");
//        capabillities.setCapability("platform", Platform.XP);
//        capabillities.setCapability("name", getClass().getSimpleName());
//        capabillities.setCapability("capture-html", true);
//        capabillities.setCapability("firefox_profile", profile);
//
//        try {
//			return new RemoteWebDriver(
//			   new URL("http://cloudbees_reference:91d5e3b0-cbd6-4e23-acc9-ce14dc000565@ondemand.saucelabs.com:80/wd/hub"),
//			   capabillities);
//		} catch (MalformedURLException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
 	@Test
 	public void loginWithRussian() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
		assertEquals("Войти", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "Russian");
 		OrdersPage page = initElements(driver, OrdersPage.class);
 		assertEquals("Добавить новый заказ", page.getAddNewOrderLinkText());
 	}
 	
 	@Test
 	public void loginWithEnglish() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
		assertEquals("Войти", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "English");
 		OrdersPage page = initElements(driver, OrdersPage.class);
 		assertEquals("Add new order", page.getAddNewOrderLinkText());
 	}
 	
 	@Test
 	public void loginWithEstonian() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
		assertEquals("Войти", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "Estonian");
 		OrdersPage page = initElements(driver, OrdersPage.class);
 		assertEquals("Добавить новый заказ", page.getAddNewOrderLinkText());
 	}
 }
