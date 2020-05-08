 package com.wesabe.servlet.tests;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.wesabe.servlet.BadRequestException;
 import com.wesabe.servlet.SafeRequest;
 
 @RunWith(Enclosed.class)
 public class SafeRequestTest {
 	private static <E> List<E> enumerationToList(Enumeration<E> enumeration) {
 		List<E> items = Lists.newLinkedList();
 		while (enumeration.hasMoreElements()) {
 			items.add(enumeration.nextElement());
 		}
 		return items;
 	}
 	
 	private static abstract class Context {
 		protected SafeRequest request;
 		protected HttpServletRequest servletRequest;
 		
 		public void setup() throws Exception {
 			this.servletRequest = mock(HttpServletRequest.class);
 			this.request = new SafeRequest(servletRequest);
 		}
 	}
 	
 	public static class Getting_The_Method extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itNormalizesTheMethodName() throws Exception {
 			when(servletRequest.getMethod()).thenReturn("get");
 			
 			assertEquals("GET", request.getMethod());
 			
 			verify(servletRequest).getMethod();
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionIfTheMethodIsInvalid() throws Exception {
 			when(servletRequest.getMethod()).thenReturn("poop");
 			
 			try {
 				request.getMethod();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 	
 	public static class Getting_The_Scheme extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itNormalizesTheScheme() throws Exception {
 			when(servletRequest.getScheme()).thenReturn("http");
 			
 			assertEquals("http", request.getScheme());
 			
 			verify(servletRequest).getScheme();
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionIfTheSchemeIsInvalid() throws Exception {
 			when(servletRequest.getScheme()).thenReturn("poop");
 			
 			try {
 				request.getScheme();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 	
 	public static class Getting_The_Server_Port extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itNormalizesTheServerPort() throws Exception {
 			when(servletRequest.getServerPort()).thenReturn(80);
 			
 			assertEquals(80, request.getServerPort());
 			
 			verify(servletRequest).getServerPort();
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionIfTheSchemeIsInvalid() throws Exception {
 			when(servletRequest.getServerPort()).thenReturn(1112228888);
 			
 			try {
 				request.getServerPort();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 	
 	public static class Getting_A_Date_Header extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itReturnsAnyParsedDateHeader() throws Exception {
 			when(servletRequest.getDateHeader("Last-Modified")).thenReturn(80L);
 			
 			assertEquals(80, request.getDateHeader("Last-Modified"));
 			
 			verify(servletRequest).getDateHeader("Last-Modified");
 		}
 		
 		@Test
 		public void itWrapsAFailedParseInABadRequestException() throws Exception {
 			when(servletRequest.getDateHeader("Last-Modified")).thenThrow(new IllegalArgumentException("no"));
 			
 			try {
 				request.getDateHeader("Last-Modified");
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 				assertTrue(e.getCause() instanceof IllegalArgumentException);
 			}
 		}
 	}
 	
 	public static class Getting_An_Int_Header extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itReturnsAnyParsedIntHeader() throws Exception {
 			when(servletRequest.getIntHeader("Age")).thenReturn(200);
 			
 			assertEquals(200, request.getIntHeader("Age"));
 			
 			verify(servletRequest).getIntHeader("Age");
 		}
 		
 		@Test
 		public void itWrapsAFailedParseInABadRequestException() throws Exception {
 			when(servletRequest.getIntHeader("Age")).thenThrow(new IllegalArgumentException("no"));
 			
 			try {
 				request.getIntHeader("Age");
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 				assertTrue(e.getCause() instanceof IllegalArgumentException);
 			}
 		}
 	}
 	
 	public static class Getting_The_Server_Name extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itNormalizesTheServerName() throws Exception {
 			when(servletRequest.getServerName()).thenReturn("example.com");
 			
 			assertEquals("example.com", request.getServerName());
 			
 			verify(servletRequest).getServerName();
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionIfTheSchemeIsInvalid() throws Exception {
 			when(servletRequest.getServerName()).thenReturn("blah\0.com");
 			
 			try {
 				request.getServerName();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 	
 	public static class Getting_The_Request_Dispatcher extends Context {
 		private RequestDispatcher dispatcher;
 		
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 			
 			this.dispatcher = mock(RequestDispatcher.class);
 		}
 		
 		@Test
 		public void itPassesThroughIfPathStartsWithWebInf() throws Exception {
 			when(servletRequest.getRequestDispatcher("WEB-INF/thing")).thenReturn(dispatcher);
 			
 			assertEquals(dispatcher, request.getRequestDispatcher("WEB-INF/thing"));
 			
 			verify(servletRequest).getRequestDispatcher("WEB-INF/thing");
 		}
 		
 		@Test
 		public void itReturnsNullIfPathDoesNotStartWithWebInf() throws Exception {
 			when(servletRequest.getRequestDispatcher(anyString())).thenReturn(dispatcher);
 			
 			assertNull(request.getRequestDispatcher("../WEB-INF/thing"));
 			
 			verify(servletRequest, never()).getRequestDispatcher(anyString());
 		}
 	}
 	
 	public static class Getting_A_List_Of_Header_Names extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itEnumeratesValidHeaders() throws Exception {
 			when(servletRequest.getHeaderNames()).thenReturn(Collections.enumeration(ImmutableList.of("Accept", "User-Agent")));
 			
 			assertEquals(ImmutableList.of("Accept", "User-Agent"), enumerationToList(request.getHeaderNames()));
 			
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionOnMalformedHeaders() throws Exception {
 			when(servletRequest.getHeaderNames()).thenReturn(Collections.enumeration(ImmutableList.of("Accept", "Age\0DEATH")));
 			
 			try {
 				request.getHeaderNames();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 	
 	public static class Getting_A_Header_Value extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 			
 			when(servletRequest.getHeader("Accept")).thenReturn("application/json");
 			when(servletRequest.getHeader("User-Agent")).thenReturn("MAL\0\0ICE");
 		}
 		
 		@Test
 		public void itPassesValidHeadersStraightThrough() throws Exception {
 			assertEquals("application/json", request.getHeader("Accept"));
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionOnInvalidValues() throws Exception {
 			try {
 				request.getHeader("User-Agent");
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 		
 		@Test
 		public void itThrowsAnIllegalArgumentExceptionWhenAskedForTheValueOfAMalformedHeader() throws Exception {
 			try {
 				request.getHeader("User-Agent\0");
 				fail("should have thrown an IllegalArgumentException, but didn't");
 			} catch (IllegalArgumentException e) {
 				assertTrue(true);
 			}
 		}
 	}
 	
 	public static class Getting_A_List_Of_Header_Values extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itEnumeratesValidHeaderValues() throws Exception {
 			when(servletRequest.getHeaders("Accept")).thenReturn(Collections.enumeration(ImmutableList.of("application/json", "application/xml")));
 			
 			assertEquals(ImmutableList.of("application/json", "application/xml"), enumerationToList(request.getHeaders("Accept")));
 			
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionOnMalformedHeaderValues() throws Exception {
 			when(servletRequest.getHeaders("Accept")).thenReturn(Collections.enumeration(ImmutableList.of("application/json", "Age\0DEATH")));
 			
 			try {
 				request.getHeaders("Accept");
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 		
 		@Test
 		public void itThrowsAnIllegalArgumentExceptionWhenAskedForTheValuesOfAMalformedHeader() throws Exception {
 			try {
 				request.getHeaders("User-Agent\0");
 				fail("should have thrown an IllegalArgumentException, but didn't");
 			} catch (IllegalArgumentException e) {
 				assertTrue(true);
 			}
 		}
 	}
 	
	public static class Getting_A_List_Of_Cookies extends Context {
 		@Before
 		@Override
 		public void setup() throws Exception {
 			super.setup();
 		}
 		
 		@Test
 		public void itReturnsAnEmptyArrayIsCookiesAreNull() throws Exception {
 			when(servletRequest.getCookies()).thenReturn(null);
 			
 			assertArrayEquals(new Cookie[0], request.getCookies());
 		}
 		
 		@Test
 		public void itReturnsAnArrayOfValidCookies() throws Exception {
 			when(servletRequest.getCookies()).thenReturn(new Cookie[] { new Cookie("sessionid", "blorp") });
 			
 			assertEquals("sessionid", request.getCookies()[0].getName());
 			assertEquals("blorp", request.getCookies()[0].getValue());
 		}
 		
 		@Test
 		public void itThrowsABadRequestExceptionWithInvalidCookies() throws Exception {
 			final Cookie badCookie = mock(Cookie.class);
 			when(badCookie.getName()).thenReturn("\0\0");
 			when(servletRequest.getCookies()).thenReturn(new Cookie[] { badCookie });
 			
 			try {
 				request.getCookies();
 				fail("should have thrown a BadRequestException, but didn't");
 			} catch (BadRequestException e) {
 				assertSame(servletRequest, e.getBadRequest());
 			}
 		}
 	}
 }
