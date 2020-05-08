 package com.mjeanroy.springhub.security;
 
 import org.fest.assertions.api.Assertions;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.web.method.HandlerMethod;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SecurityInterceptorTest {
 
 	@Test
	public void testSecurityInterceptor_shouldThrow403() throws Exception {
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
 		HandlerMethod handler = Mockito.mock(HandlerMethod.class);
 
 		Security securityAnnotation = Mockito.mock(Security.class);
 		Mockito.when(handler.getMethodAnnotation(Security.class)).thenReturn(securityAnnotation);
 
 		SecurityInterceptor interceptor = new SecurityInterceptor();
 		interceptor.setSalt("fnt");
 		interceptor.setSecret("ag1yxzwlmryjhp%o");
 		interceptor.setCookieName("SESSIONID");
 
 		boolean result = interceptor.preHandle(request, response, handler);
 
 		Assertions.assertThat(result).isFalse();
		Mockito.verify(response).setStatus(403);
 	}
 
 	@Test
 	public void testSecurityInterceptor_shouldRedirect() throws Exception {
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
 		HandlerMethod handler = Mockito.mock(HandlerMethod.class);
 
 		Security securityAnnotation = Mockito.mock(Security.class);
 		Mockito.when(securityAnnotation.redirectTo()).thenReturn("/login");
 		Mockito.when(handler.getMethodAnnotation(Security.class)).thenReturn(securityAnnotation);
 
 		SecurityInterceptor interceptor = new SecurityInterceptor();
 		interceptor.setSalt("fnt");
 		interceptor.setSecret("ag1yxzwlmryjhp%o");
 		interceptor.setCookieName("SESSIONID");
 
 		boolean result = interceptor.preHandle(request, response, handler);
 
 		Assertions.assertThat(result).isFalse();
 		Mockito.verify(response).sendRedirect("/login");
 	}
 
 	@Test
 	public void testSecurityInterceptor_shouldAuthenticate() throws Exception {
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
 		HandlerMethod handler = Mockito.mock(HandlerMethod.class);
 
 		Security securityAnnotation = Mockito.mock(Security.class);
 		Mockito.when(securityAnnotation.redirectTo()).thenReturn("/login");
 		Mockito.when(handler.getMethodAnnotation(Security.class)).thenReturn(securityAnnotation);
 
 		Cookie cookie1 = new Cookie("SESSIONID", "ZvnSZ7HTSao9e%2FmyIKPDcw%3D%3D");
 		Cookie cookie2 = new Cookie("bar", "ZvnSZ7HTSao9e%2FmyIKPDcw");
 		Cookie[] cookies = {cookie1, cookie2};
 		Mockito.when(request.getCookies()).thenReturn(cookies);
 
 		SecurityInterceptor interceptor = new SecurityInterceptor();
 		interceptor.setSalt("fnt");
 		interceptor.setSecret("ag1yxzwlmryjhp%o");
 		interceptor.setCookieName("SESSIONID");
 
 		boolean result = interceptor.preHandle(request, response, handler);
 
 		Assertions.assertThat(result).isTrue();
 		Mockito.verify(response, Mockito.never()).sendRedirect(Mockito.anyString());
 		Mockito.verify(response, Mockito.never()).setStatus(Mockito.anyInt());
 	}
 
 	@Test
 	public void testSecurityInterceptor_noSecurity() throws Exception {
 		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
 		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
 		HandlerMethod handler = Mockito.mock(HandlerMethod.class);
 
 		Mockito.when(handler.getMethodAnnotation(Security.class)).thenReturn(null);
 
 		SecurityInterceptor interceptor = new SecurityInterceptor();
 		interceptor.setSalt("fnt");
 		interceptor.setSecret("ag1yxzwlmryjhp%o");
 		interceptor.setCookieName("SESSIONID");
 
 		boolean result = interceptor.preHandle(request, response, handler);
 
 		Assertions.assertThat(result).isTrue();
 		Mockito.verify(response, Mockito.never()).sendRedirect(Mockito.anyString());
 		Mockito.verify(response, Mockito.never()).setStatus(Mockito.anyInt());
 	}
 }
