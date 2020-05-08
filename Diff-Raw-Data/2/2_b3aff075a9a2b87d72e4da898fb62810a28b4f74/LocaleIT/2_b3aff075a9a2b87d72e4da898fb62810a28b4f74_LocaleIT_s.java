 package com.gmail.at.zhuikov.aleksandr.it;
 
 import static junit.framework.Assert.assertEquals;
 import static org.openqa.selenium.support.PageFactory.initElements;
 
 import org.junit.Test;
 
 import com.gmail.at.zhuikov.aleksandr.it.page.LoginPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.OrdersPage;
 
 public class LocaleIT extends AbstractWebDriverTest {
 
 	@Test
 	public void loginWithRussian() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
 		assertEquals("Login", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "Russian");
 		OrdersPage page = initElements(driver, OrdersPage.class);
 		assertEquals("Добавить новый заказ", page.getAddNewOrderLinkText());
 	}
 	
 	@Test
 	public void loginWithEnglish() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
 		assertEquals("Login", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "English");
 		OrdersPage page = initElements(driver, OrdersPage.class);
 		assertEquals("Add new order", page.getAddNewOrderLinkText());
 	}
 	
 	@Test
 	public void loginWithEstonian() throws Exception {
 		LoginPage loginPage = initElements(driver, LoginPage.class);
 		assertEquals("Login", loginPage.getLoginButtonText());
 		loginPage.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN", "Estonian");
 		OrdersPage page = initElements(driver, OrdersPage.class);
		assertEquals("Добавить новый заказ", page.getAddNewOrderLinkText());
 	}
 }
