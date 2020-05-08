 package com.sk.frontend.web.controller;
 
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
 import com.sk.domain.ShoppingCart;
 import com.sk.service.OrderService;
 import com.sk.util.builder.CreditCardPaymentMethodBuilder;
 import com.sk.util.builder.ShoppingCartBuilder;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PaymentControllerTest {
 
 	private PaymentController controller;
 
 	@Mock
 	private OrderService orderService;
 
 	@Mock
 	private BindingResult bindingResult;
 
 	@Before
 	public void before() {
 		controller = new PaymentController();
 		controller.setOrderService(orderService);
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
 }
