 package com.sk.frontend.web.controller;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import javax.servlet.http.Cookie;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.sk.domain.Product;
 import com.sk.domain.ShoppingCart;
 import com.sk.frontend.web.interceptor.ShoppingCartInterceptor;
 import com.sk.service.CacheService;
 import com.sk.service.ProductService;
 import com.sk.util.builder.ProductBuilder;
 import com.sk.util.builder.ShoppingCartBuilder;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class CartControllerTest {
 
 	private CartController cartController;
 	private MockHttpServletRequest request = new MockHttpServletRequest();
 	@Mock private ProductService productService;
 	@Mock private CacheService cacheService;
 	
 	
 	@Before
 	public void init(){
 		cartController = new CartController(productService, cacheService);
 		cartController.setAppRoot("appRoot");
 	}
 	
 	@Test
 	public void shouldAddProductToCart() {
 		
 		String productUrl = "product-url";
 		Product product = new ProductBuilder().build();
 		ShoppingCart cart = new ShoppingCartBuilder().build();
 		
 		request.setAttribute("cart", cart);
 		request.setCookies(new Cookie("cart", "1234"));
 		when(productService.findByUrl(productUrl)).thenReturn(product);
 		
 		cartController.addToCart(productUrl, request);
 		
 		assertThat(cart.getItems().size(), equalTo(1));
 		assertThat(cart.getItems().iterator().next().getProduct(), equalTo(product));
 	}
 	
 	@Test
 	public void shouldRedirectToShowCartPage(){
 		String productUrl = "product-url";
 		Product product = new ProductBuilder().build();
 		ShoppingCart cart = new ShoppingCartBuilder().build();
 		
 		request.setAttribute("cart", cart);
 		request.setCookies(new Cookie("cart", "1234"));
 		when(productService.findByUrl(productUrl)).thenReturn(product);
 		
 		ModelAndView mav = cartController.addToCart(productUrl, request);
 		RedirectView rView = (RedirectView)mav.getView();
 		
 		assertThat(rView.getUrl(), equalTo("appRoot/cart/show"));
 	}
 	
 	@Test
 	public void shouldAddCartToCacheEveryBuy(){
 		
 		String productUrl = "product-url";
 		ShoppingCart cart = new ShoppingCartBuilder().build();
 		
 		request.setAttribute("cart", cart);
 		request.setCookies(new Cookie("cart", "1234"));
 		
 		cartController.addToCart(productUrl, request);
 		
 		verify(cacheService).put("1234", cart, ShoppingCartInterceptor.SEVENDAYS);
 	}
	
	@Test
	public void shouldDeleteProductInShoppingCart() {
		String productUrl = "product-url";
		
		Product product = new ProductBuilder().build();
		ShoppingCart cart = new ShoppingCartBuilder().build();
		cart.addProduct(product);
		
		when(productService.findByUrl(productUrl)).thenReturn(product);
		request.setAttribute("cart", cart);
		request.setCookies(new Cookie("cart", "1234"));
		
		cartController.deleteFromCart(productUrl, request);
		
		
		assertThat(cart.getItems().size(), equalTo(0));
	}

	@Test
	public void shouldAddCartToCacheEveryDelete(){
		String productUrl = "product-url";
		ShoppingCart cart = new ShoppingCartBuilder().build();
		
		request.setAttribute("cart", cart);
		request.setCookies(new Cookie("cart", "1234"));
		
		cartController.deleteFromCart(productUrl, request);
		
		verify(cacheService).put("1234", cart, ShoppingCartInterceptor.SEVENDAYS);
	}
 
 }
