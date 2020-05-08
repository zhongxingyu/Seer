 package com.gmail.at.zhuikov.aleksandr.it;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.openqa.selenium.support.PageFactory.initElements;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.gmail.at.zhuikov.aleksandr.it.page.AddItemPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.AddOrderPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.EditOrderPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.LoginPage;
 import com.gmail.at.zhuikov.aleksandr.it.page.OrdersPage;
 
 public class WebDriverIT extends AbstractWebDriverTest {
 
	private String customerName = getUniqueCharString();
 	
 	private AddOrderPage addOrderPage;
 	private OrdersPage ordersPage;
 	private EditOrderPage editOrderPage;
 	private AddItemPage addItemPage;
 
 	public WebDriverIT() {
 		super("");
 	}
 
 	@Before 
 	public void initFirstPage() {
 		
 		initElements(driver, LoginPage.class)
 			.loginWithMyOpenId("spring-reference-admin", "5ybQ58oN");
 		
 		ordersPage = initElements(driver, OrdersPage.class);
 	}
 	
 	@Test
 	public void userCreatesOrder() {
 		addOrderPage = ordersPage.clickAddNewOrder();
 
 		addOrderPageShowsErrorIfCustomerFieldIsEmpty();
 		addOrderPageShowsErrorIfEmailIsIllegal();
 		addOrderPageReturnsToOrdersListIfAllFieldsOk();
 		
 		clickingNewlyCreatedOrderOpensIt();
 		editOrderPageShowsErrorIfEmailIsIllegal();
 		editOrderPageReturnsToOrdersListIfAllFieldsOk();
 		
 		clickingUpdatedOrderOpensIt();
 		clickingAddItemLinkOpensAddItemPage();
 		addItemPageShowsErrorIfProductFieldIsEmpty();
 		addItemPageShowsErrorIfPriceIsNegative();
 		addItemPageShowsErrorIfQuantityIsLessThanOne();
 		addItemPageReturnsToEditOrderIfAllFieldsAreOk();
 		addItemPageShowsErrorIfOrderAlreadyHasItemWithSameProductAndPrice();
 		addItemPageAddsAnotherItemAndReturnsToEditOrder();
 		
 		editOrderPageReturnsToOrdersListIfDeleteClicked();
 	}
 	
 	@Test
 	public void pagesAppearWhenMoreThanTwentyOrders() {
 		
 		for (int i = 0; i < 21; i++) {
 			ordersPage = ordersPage
 				.clickAddNewOrder()
 				.setCustomer(customerName)
 				.setEmail("mail@example.com")
 				.clickSaveButton();
 		}
 		
 		assertTrue(ordersPage.hasPages());
 	}
 	
 	private void addItemPageShowsErrorIfOrderAlreadyHasItemWithSameProductAndPrice() {
 		addItemPage = editOrderPage
 				.clickAddItemLink()
 				.setProduct("test product")
 				.setPrice("2555.12")
 				.setQuantity("344234")
 				.clickSaveButtonAndExpectErrors();
 		
 		assertTrue(addItemPage.hasOrderErrors());
 	}
 	
 	private void addItemPageAddsAnotherItemAndReturnsToEditOrder() {
 		editOrderPage = addItemPage
 				.setProduct("test product2")
 				.setPrice("2555.12")
 				.setQuantity("344234")
 				.clickSaveButton();
 		
 		assertTrue(editOrderPage.hasItem("test product2 344234 x"));
 	}
 	
 	private void editOrderPageReturnsToOrdersListIfDeleteClicked() {
 		ordersPage = editOrderPage.clickDeleteButton();
 		assertFalse(ordersPage.hasOrder(customerName));
 	}
 
 	private void addItemPageReturnsToEditOrderIfAllFieldsAreOk() {
 		
 		editOrderPage = addItemPage
 			.setProduct("test product")
 			.setPrice("2555.12")
 			.setQuantity("344234")
 			.clickSaveButton();
 		
 		assertTrue(editOrderPage.hasItem("test product 344234 x"));
 	}
 
 	private void addItemPageShowsErrorIfQuantityIsLessThanOne() {
 		addItemPage = addItemPage
 				.setQuantity("0")
 				.clickSaveButtonAndExpectErrors();
 		assertTrue(addItemPage.hasQuantityErrors());
 	}
 
 	private void addItemPageShowsErrorIfPriceIsNegative() {
 		addItemPage = addItemPage
 				.setPrice("-1")
 				.clickSaveButtonAndExpectErrors();
 		assertTrue(addItemPage.hasPriceErrors());
 	}
 
 	private void addItemPageShowsErrorIfProductFieldIsEmpty() {
 		addItemPage = addItemPage
 				.setProduct("  ")
 				.clickSaveButtonAndExpectErrors();
 		assertTrue(addItemPage.hasProductErrors());
 	}
 
 	private void clickingAddItemLinkOpensAddItemPage() {
 		addItemPage = editOrderPage.clickAddItemLink();
 	}
 
 	private void addOrderPageShowsErrorIfCustomerFieldIsEmpty() {
 		addOrderPage = addOrderPage
 				.setCustomer("  ")
 				.clickSaveButtonAndExpectErrors();
 		assertTrue(addOrderPage.hasCustomerErrors());
 	}
 	
 	private void addOrderPageShowsErrorIfEmailIsIllegal() {
 		addOrderPage = addOrderPage
 				.setEmail("wrong mail")
 				.clickSaveButtonAndExpectErrors();
 		assertTrue(addOrderPage.hasEmailErrors());
 	}
 	
 	private void addOrderPageReturnsToOrdersListIfAllFieldsOk() {
 		ordersPage = addOrderPage
 				.setCustomer(customerName)
 				.setEmail("mail@example.com")
 				.clickSaveButton();
 	}
 	
 	private void clickingNewlyCreatedOrderOpensIt() {
 		editOrderPage = ordersPage.clickOrder(customerName);
 		
 		assertEquals(customerName, editOrderPage.getCustomer());
 		assertEquals("mail@example.com", editOrderPage.getEmail());
 	}
 	
 	private void clickingUpdatedOrderOpensIt() {
 		editOrderPage = ordersPage.clickOrder(customerName);
 		
 		assertEquals(customerName, editOrderPage.getCustomer());
 		assertEquals("changed@example.com", editOrderPage.getEmail());
 	}
 	
 	private void editOrderPageShowsErrorIfEmailIsIllegal() {
 		editOrderPage = editOrderPage
 				.setEmail("wrong mail")
 				.clickSaveButtonAndExpectErrors();
 		
 		assertTrue(editOrderPage.hasEmailErrors());
 	}
 	
 	private void editOrderPageReturnsToOrdersListIfAllFieldsOk() {
 		ordersPage = editOrderPage
 				.setEmail("changed@example.com")
 				.clickSaveButton();
 	}
 }
