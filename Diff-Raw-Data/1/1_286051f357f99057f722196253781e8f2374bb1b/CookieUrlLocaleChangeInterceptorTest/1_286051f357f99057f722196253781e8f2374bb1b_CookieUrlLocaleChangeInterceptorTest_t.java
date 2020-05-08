 package com.github.lrkwz.web.servlet.handler.i18n;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Locale;
 
 import javax.servlet.http.Cookie;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
 public class CookieUrlLocaleChangeInterceptorTest extends
 		UrlLocaleChangeInterceptorTest {
 
 	@Test
 	public void cookieBasedTest() throws Exception {
 		MockHttpServletRequest request = new MockHttpServletRequest("GET",
 				"/somecontroller");
 
 		Locale locale = Locale.ITALY;
 		Cookie cookies = new Cookie(localeResolver.getCookieName(),
 				locale.toString());
 		request.setCookies(cookies);
 
 		doGet(request, new MockHttpServletResponse());
 
 		assertTrue(RequestContextUtils.getLocale(request) + "!=" + locale,
 				RequestContextUtils.getLocale(request).equals(locale));
 	}
 }
