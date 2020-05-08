 package com.gmail.at.zhuikov.aleksandr.servlet.controllers;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.springframework.http.HttpStatus.CREATED;
 import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeAvailable;
 import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeValue;
 import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.http.ResponseEntity;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.ModelAndViewDefiningException;
 
 import com.gmail.at.zhuikov.aleksandr.root.domain.Order;
 import com.gmail.at.zhuikov.aleksandr.root.repository.OrderRepository;
 
 public class CreateOrderControllerTest {
 
 	@Mock 
 	private OrderRepository orderRepository;
 	private MockHttpServletRequest request = new MockHttpServletRequest();
 	
 	@InjectMocks 
 	private CreateOrderController controller = new CreateOrderController();
 	
 	@Before
 	public void injectMocks() {
 		initMocks(this);
 	}
 	
 	@Test
 	public void createForm() {
 		assertEquals("addOrder", controller.createForm());
 	}
 	
 	@Test
 	public void prepareOrder() {
 		Order order = controller.prepareOrder("x");
 		assertEquals("x", order.getCustomer());
 	}
 	
 	@Test
 	public void create() throws ModelAndViewDefiningException {
 		Order order = new Order("x");
 		String view = controller.create(order);
 		verify(orderRepository).save(order);
 		assertEquals("redirect:/orders", view);
 	}
 	
 	@Test
 	public void createFromBody() {
 		Order order = new Order("x");
 		when(orderRepository.save(order)).then(new Answer<Order>() {
 
 			@Override
 			public Order answer(InvocationOnMock invocation) throws Throwable {
 				Order param = (Order) invocation.getArguments()[0];
 				ReflectionTestUtils.setField(param, "id", 123L);
 				return param;
 			}
 		});
 		
 		String view = controller.createFromBody(order);
 		verify(orderRepository).save(order);
 		assertEquals("redirect:/orders/123", view);
 	}
 	
 	@Test
 	public void createFromBodyAndReturnLocation() {
 		Order order = new Order("x");
 		when(orderRepository.save(order)).then(new Answer<Order>() {
 
 			@Override
 			public Order answer(InvocationOnMock invocation) throws Throwable {
 				Order param = (Order) invocation.getArguments()[0];
 				ReflectionTestUtils.setField(param, "id", 123L);
 				return param;
 			}
 		});
 		
 		ResponseEntity<Void> response = controller.createFromBodyAndReturnLocation(order, request);
 		verify(orderRepository).save(order);
 		assertEquals(CREATED, response.getStatusCode());
 		assertEquals("http://localhost/orders/123", response.getHeaders().getLocation().toString());
 	}
 	
 	@Test
 	public void handleInvalidOrderAfterBindException() {
 		Order order = new Order("x");
 		ModelAndView mav = controller.handleInvalidOrder(new BindException(order, "order"));
 		assertViewName(mav, "addOrder");
 		assertModelAttributeValue(mav, "order", order);
 		assertModelAttributeAvailable(mav, "org.springframework.validation.BindingResult.order");
 	}
 	
 	@Test
 	public void handleInvalidOrderAfterMethodArgumentNotValidException() {
 		Order order = new Order("x");
 		MethodArgumentNotValidException e = new MethodArgumentNotValidException(
 				null,
 				new BindException(order, "order"));
 		ModelAndView mav = controller.handleInvalidOrder(e);
 		assertViewName(mav, "addOrder");
 		assertModelAttributeValue(mav, "order", order);
 		assertModelAttributeAvailable(mav, "org.springframework.validation.BindingResult.order");
 	}
 }
