 /**
  * 
  */
 package com.morgajel.spoe.controller;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.stub;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Locale;
 
import org.hibernate.Session;
import org.hibernate.SessionFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.MessageSource;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.SnippetService;
 
 /**
  * @author jmorgan
  *
  */
 public class SearchControllerTest {
 
     private AccountService mockAccountService;
     private SnippetService mockSnippetService;
     private SecurityContext mockContext;
     private Account mockAccount;
     private MessageSource mockMessageSource;
    private SessionFactory mockSessionFactory;
    private Session mockSession;
     private static final String USERNAME = "bobdole";
    private static final Locale LOCALE = Locale.getDefault();

     private SearchController searchController;
     @Before
     public void setUp() throws Exception {
         mockAccountService = mock(AccountService.class);
         mockSnippetService = mock(SnippetService.class);
         mockAccount = mock(Account.class);
        mockSession = mock(Session.class);
         mockContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
         mockMessageSource = mock(MessageSource.class);
        mockSessionFactory = mock(SessionFactory.class);
         searchController = new SearchController();
         searchController.setAccountService(mockAccountService);
         SecurityContextHolder.setContext(mockContext);
         searchController.setMessageSource(mockMessageSource);
        searchController.setSessionFactory(mockSessionFactory);
     }
 
     @After
     public void tearDown() throws Exception {
         searchController = null;
     }
 
     @Test
     public void testQuickSearch() {
         String searchQuery = "fun search";

         ModelAndView mav = searchController.quickSearch(searchQuery);

         assertEquals("search/results", mav.getViewName());
        //Note that this is poorly written and fails.
        //FIXME this also sucks and is unreliable
     }
 
     @Test
     public void testQuickSearchFail() {
         String searchQuery = "fun search";
         stub(mockMessageSource.getMessage(eq("search.results"), (Object[]) any(), eq(LOCALE))).toThrow(new IndexOutOfBoundsException());
         ModelAndView mav = searchController.quickSearch(searchQuery);
         assertEquals("search/results", mav.getViewName());
         verify(mockMessageSource).getMessage(eq("search.searchfailed"), (Object[]) any(), eq(LOCALE));
        //FIXME this also sucks and is unreliable
     }
 
     @Test
     public void testGetAndSetAccountService() {
         searchController.setAccountService(mockAccountService);
         assertEquals(mockAccountService, searchController.getAccountService());
     }
     @Test
     public void testGetAndSetSnippetService() {
         searchController.setSnippetService(mockSnippetService);
         assertEquals(mockSnippetService, searchController.getSnippetService());
     }

     @Test
     public void testGetContextAccount() {
         when(mockContext.getAuthentication().getName()).thenReturn(USERNAME);
         when(mockAccountService.loadByUsername(USERNAME)).thenReturn(mockAccount);
         Account account = searchController.getContextAccount();
         assertEquals(mockAccount, account);
     }
 
 }
