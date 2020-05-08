 package com.sk.frontend.web.controller;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 import java.util.Calendar;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.sk.domain.CreditCardPaymentMethod;
 import com.sk.domain.CreditCardType;
 import com.sk.domain.Shopper;
 import com.sk.domain.ShoppingCart;
 import com.sk.service.OrderService;
 import com.sk.service.ShopperService;
 import com.sk.service.encryption.EncryptionService;
 import com.sk.util.builder.CreditCardPaymentMethodBuilder;
 import com.sk.util.builder.ShopperBuilder;
 import com.sk.util.builder.ShoppingCartBuilder;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PaymentControllerTest {
 
 	private PaymentController controller;
 
 	@Mock private OrderService orderService;
 	@Mock private BindingResult bindingResult;
 	@Mock private ShopperService shopperService;
 	@Mock private EncryptionService encryptionService;
 
 	@Before
 	public void before() {
 		controller = new PaymentController();
 		controller.setOrderService(orderService);
 		controller.setShopperService(shopperService);
 		controller.setEncryptionService(encryptionService);
 	}
 
 	@Test
 	public void shouldCreateModelForPayment() {
 
 		ModelAndView mav = controller.getPaymentMAV(null);
 		assertThat(mav.getModelMap().containsKey("payment"), equalTo(true));
 		assertThat(mav.getModelMap().containsKey("years"), equalTo(true));
 		assertThat(mav.getModelMap().containsKey("months"), equalTo(true));
 		assertThat(mav.getModelMap().containsKey("creditCardTypes"), equalTo(true));
 
 	}
 	
 	@Test
 	public void shouldSetCardNoAndCVCIfExists(){
 		Shopper shopper = new ShopperBuilder().name("Default Shopper").cardNo("ABCDABCD").cvc("ZXC").build();
 		when(shopperService.getStubShopper()).thenReturn(shopper);
 		when(encryptionService.decrypt("ABCDABCD")).thenReturn("12341234");
 		when(encryptionService.decrypt("ZXC")).thenReturn("003");
 		
 		ModelAndView mav = controller.show();
 		CreditCardPaymentMethod payment = (CreditCardPaymentMethod)mav.getModelMap().get("payment");
 
 		assertThat(payment.getOwner(), equalTo("Default Shopper"));
 		assertThat(payment.getCardNumber(), equalTo("12341234"));
 		assertThat(payment.getCvc(), equalTo("003"));
 		assertFalse((Boolean)mav.getModelMap().get("showSaveCheck"));
 	}
 	
 	@Test
 	public void shouldNotCardInfoSetIfNotExists(){
 		Shopper shopper = new ShopperBuilder().name("Default Shopper").build();
 		when(shopperService.getStubShopper()).thenReturn(shopper);
 		
 		ModelAndView mav = controller.show();
 		CreditCardPaymentMethod payment = (CreditCardPaymentMethod)mav.getModelMap().get("payment");
 		
 		assertNull(payment.getOwner());
 		assertNull(payment.getCardNumber());
 		assertNull(payment.getCvc());
 		assertTrue((Boolean)mav.getModelMap().get("showSaveCheck"));
 	}
 	
 	@Test
 	public void shouldCreateCreditCardTypes() {
 
 		Map<String, String> creditCardTypes = controller.getCreditCardTypes();
 		assertThat(creditCardTypes.keySet().contains(CreditCardType.VISA.name()), equalTo(true));
 		assertThat(creditCardTypes.keySet().contains(CreditCardType.MASTERCARD.name()), equalTo(true));
 		assertThat(creditCardTypes.keySet().size(), equalTo(2));
 
 	}
 
 	@Test
 	public void shouldCreateYears() {
 
 		TreeMap<String, String> years = (TreeMap<String, String>) controller.getYears();
 		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
 
 		assertThat(years.keySet().size(), equalTo(12));
 		assertThat(years.firstKey(), equalTo("" + currentYear));
 		assertThat(years.lastKey(), equalTo("" + (currentYear + 11)));
 
 	}
 
 	@Test
 	public void shouldCreateMonths() {
 
 		TreeMap<String, String> manths = (TreeMap<String, String>) controller.getMonths();
 
 		assertThat(manths.keySet().size(), equalTo(12));
 		assertThat(manths.firstKey(), equalTo("01"));
 		assertThat(manths.lastKey(), equalTo("12"));
 
 	}
 
 	@Test
 	public void shouldNotCallOrderServiceIfCreditCardIsNotValid() {
 		when(bindingResult.hasErrors()).thenReturn(true);
 		controller.submit(null, bindingResult, null);
 		verifyZeroInteractions(orderService);
 		verify(bindingResult).hasErrors();
 	}
 
 	@Test
 	public void shoulCallOrderServiceIfCardIsValid() {
 		when(bindingResult.hasErrors()).thenReturn(false);
 
 		CreditCardPaymentMethod cardPaymentMethod = new CreditCardPaymentMethodBuilder().build();
 		ShoppingCart shoppingCart = new ShoppingCartBuilder().build();
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setAttribute("cart", shoppingCart);
 
 		ModelAndView mav = controller.submit(cardPaymentMethod, bindingResult, request);
 
 		verify(orderService).createOrder(shoppingCart, cardPaymentMethod);
 		verify(bindingResult).hasErrors();
 		assertThat(mav.getViewName(), equalTo("confirm"));
 	}
 
 	@Test
 	public void shouldSaveCreditCardInfoIfSaveChecked(){
 		
 		CreditCardPaymentMethod cardPaymentMethod = new CreditCardPaymentMethodBuilder().build();
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("saveCardInfo", "1");
 		when(bindingResult.hasErrors()).thenReturn(false);
 		
 		Shopper shopper = new ShopperBuilder().name("Default Shopper").build();
 		when(shopperService.getStubShopper()).thenReturn(shopper);
 		
 		controller.submit(cardPaymentMethod, bindingResult, request);
 		
 		verify(shopperService).encryptAndsaveCardInfo(shopper, cardPaymentMethod.getCardNumber(), cardPaymentMethod.getCvc());
 	}
 }
