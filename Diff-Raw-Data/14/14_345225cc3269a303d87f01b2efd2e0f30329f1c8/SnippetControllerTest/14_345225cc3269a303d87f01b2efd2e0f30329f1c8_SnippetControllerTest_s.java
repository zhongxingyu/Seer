 package com.morgajel.spoe.controller;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mail.MailSender;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.servlet.ModelAndView;
 import static org.mockito.Mockito.*;
 
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.model.Snippet;
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.SnippetService;
 import com.morgajel.spoe.web.EditSnippetForm;
 
 
 
 public class SnippetControllerTest {
     private AccountService mockAccountService;
     private SnippetService mockSnippetService;
     private SecurityContext mockContext;
     private EditSnippetForm mockEditSnippetForm;
     private Account mockAccount;
     private Snippet mockSnippet;
     private MailSender mockMailSender;
     private SnippetController snippetController;
     private String username = "bobdole";
     private String content = "Four score and seven years ago.";
     private String title = "Abe's Speech";
     private Long snippetId = 123123L;
     private Long accountId = 456456L;
     @Before
     public void setUp() throws Exception {
         mockAccountService = mock(AccountService.class);
         mockSnippetService = mock(SnippetService.class);
         mockMailSender = mock(MailSender.class);
         mockAccount = mock(Account.class);
         mockSnippet = mock(Snippet.class);
         mockEditSnippetForm = mock(EditSnippetForm.class);
         mockContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
 
         snippetController = new SnippetController();
         snippetController.setAccountService(mockAccountService);
         snippetController.setSnippetService(mockSnippetService);
         snippetController.setMailSender(mockMailSender);
         SecurityContextHolder.setContext(mockContext);
     }
 
     @After
     public void tearDown() throws Exception {
         mockAccountService = null;
         mockSnippetService = null;
         mockMailSender = null;
         snippetController = null;
         mockAccount = null;
         mockSnippet = null;
         mockEditSnippetForm = null;
         mockContext = null;
     }
 
     @Test
     public void testGetAndSetMailSender() {
         snippetController.setMailSender(mockMailSender);
         assertEquals(mockMailSender, snippetController.getMailSender());
     }
 
     @Test
     public void testGetAndSetAccountService() {
         snippetController.setAccountService(mockAccountService);
         assertEquals(mockAccountService, snippetController.getAccountService());
     }
 
     @Test
     public void testGetContextAccount() {
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         Account account = snippetController.getContextAccount();
         assertEquals(mockAccount, account);
     }
 
     @Test
     public void testSaveSnippetNewSnippet() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(null);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccount.getAccountId()).thenReturn(accountId);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
 
         ModelAndView mav = snippetController.saveSnippet(mockEditSnippetForm);
 
         verify(mockSnippetService, times(1)).saveSnippet((Snippet) anyObject());
         assertEquals("Snippet saved.", mav.getModel().get("message"));
         assertEquals("snippet/editSnippet", mav.getViewName());
         assertEquals(mockEditSnippetForm, mav.getModel().get("editSnippetForm"));
     }
 
     @Test
     public void testSaveSnippetExistingSnippet() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccount.getAccountId()).thenReturn(accountId);
         when(mockSnippet.getAuthor()).thenReturn(mockAccount);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
 
         ModelAndView mav = snippetController.saveSnippet(mockEditSnippetForm);
 
         verify(mockSnippetService, times(1)).saveSnippet((Snippet) anyObject());
         assertEquals("Snippet saved.", mav.getModel().get("message"));
         assertEquals("snippet/editSnippet", mav.getViewName());
         assertEquals(mockEditSnippetForm, mav.getModel().get("editSnippetForm"));
     }
 
     @Test
     public void testSaveSnippetNotYourSnippet() {
         Account otherUser = mock(Account.class);
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccount.getAccountId()).thenReturn(accountId);
         when(mockSnippet.getAuthor()).thenReturn(otherUser);
         when(otherUser.getUsername()).thenReturn("otheruser");
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
 
         ModelAndView mav = snippetController.saveSnippet(mockEditSnippetForm);
 
         verify(mockSnippetService, times(0)).saveSnippet((Snippet) anyObject());
         assertEquals("I'm sorry, you're not the author of this snippet.", mav.getModel().get("message"));
         assertEquals("snippet/snippetFailure", mav.getViewName());
     }
 
     @Test
     public void testSaveSnippetNotFound() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccount.getAccountId()).thenReturn(accountId);
         when(mockSnippet.getAuthor()).thenReturn(mockAccount);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(null);
 
         ModelAndView mav = snippetController.saveSnippet(mockEditSnippetForm);
 
         verify(mockSnippetService, times(0)).saveSnippet((Snippet) anyObject());
         assertEquals("I'm sorry, that snippet wasn't found.", mav.getModel().get("message"));
         assertEquals("snippet/snippetFailure", mav.getViewName());
     }
     @Test
     public void testSaveSnippetException() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         stub(mockContext.getAuthentication()).toThrow(new IndexOutOfBoundsException());
         when(mockAccount.getAccountId()).thenReturn(accountId);
         when(mockSnippet.getAuthor()).thenReturn(mockAccount);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(null);
 
         ModelAndView mav = snippetController.saveSnippet(mockEditSnippetForm);
 
         verify(mockSnippetService, times(0)).saveSnippet((Snippet) anyObject());
         assertEquals("something failed.", mav.getModel().get("message"));
         assertEquals("snippet/snippetFailure", mav.getViewName());
     }
 
     @Test
     public void testMySnippets() {
         //TODO how does this test bad jsp loops and such
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         ModelAndView mav = snippetController.showMySnippets();
         assertEquals(mockAccount, mav.getModel().get("account"));
         assertEquals("snippet/mySnippets", mav.getViewName());
     }
 
     @Test
     public void testMySnippetsNoAccount() {
         //TODO how does this test bad jsp loops and such
         when(mockAccountService.loadByUsername(username)).thenReturn(null);
         ModelAndView mav = snippetController.showMySnippets();
         assertEquals("Odd, I couldn't find your account.", mav.getModel().get("message"));
         assertEquals("snippet/snippetFailure", mav.getViewName());
     }
 
     @Test
     public void testMySnippetsException() {
         //TODO how does this test bad jsp loops and such
         stub(mockContext.getAuthentication()).toThrow(new IndexOutOfBoundsException());
         ModelAndView mav = snippetController.showMySnippets();
         assertEquals("Something failed while trying to display user snippets.", mav.getModel().get("message"));
         assertEquals("snippet/snippetFailure", mav.getViewName());
     }
 
     @Test
     public void testEditSnippet() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
         when(mockSnippet.getAuthor()).thenReturn(mockAccount);
         when(mockAccount.getUsername()).thenReturn(username);
 
         ModelAndView mav = snippetController.editSnippet(snippetId);
 
         assertEquals("snippet/editSnippet", mav.getViewName());
         assertNull(mav.getModel().get("message"));
     }
 
     @Test
     public void testEditSnippetNotAuthor() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
         Account notAccount = mock(Account.class);
         when(mockSnippet.getAuthor()).thenReturn(notAccount);
         when(notAccount.getUsername()).thenReturn("bobbo");
 
         ModelAndView mav = snippetController.editSnippet(snippetId);
 
         verify(mockEditSnippetForm, times(0)).importSnippet((Snippet) anyObject());
         assertEquals("snippet/viewSnippet", mav.getViewName());
         assertEquals("I'm sorry, Only the author can edit a snippet.", mav.getModel().get("message"));
     }
 
     @Test
     public void testEditSnippetNotFound() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippetService.loadById(snippetId)).thenReturn(null);
         Account notAccount = mock(Account.class);
         when(mockSnippet.getAuthor()).thenReturn(notAccount);
         when(notAccount.getUsername()).thenReturn("bobbo");
 
         ModelAndView mav = snippetController.editSnippet(snippetId);
 
         verify(mockEditSnippetForm, times(0)).importSnippet((Snippet) anyObject());
         assertEquals("snippet/snippetFailure", mav.getViewName());
         assertEquals("I'm sorry, 123123 was not found.", mav.getModel().get("message"));
     }
 
     @Test
     public void testEditSnippetException() {
         when(mockEditSnippetForm.getContent()).thenReturn(content);
         when(mockEditSnippetForm.getTitle()).thenReturn(title);
         when(mockEditSnippetForm.getSnippetId()).thenReturn(snippetId);
         stub(mockContext.getAuthentication()).toThrow(new IndexOutOfBoundsException());
 
         ModelAndView mav = snippetController.editSnippet(snippetId);
 
         verify(mockEditSnippetForm, times(0)).importSnippet((Snippet) anyObject());
         assertEquals("snippet/snippetFailure", mav.getViewName());
         assertEquals("Something failed while trying to display 123123.", mav.getModel().get("message"));
     }
     @Test
     public void testGetAndSetSnippetService() {
         snippetController.setSnippetService(mockSnippetService);
         assertEquals(mockSnippetService, snippetController.getSnippetService());
     }
 
     @Test
     public void testDisplaySnippet() {
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
         ModelAndView mav = snippetController.displaySnippet(snippetId);
 
         assertEquals("snippet/viewSnippet", mav.getViewName());
         assertEquals(mockSnippet, mav.getModel().get("snippet"));
        assertEquals("<!-- nothing important-->", mav.getModel().get("message"));
         assertNull(mav.getModel().get("editlink"));
     }
 
     @Test
     public void testDisplaySnippetIsOwner() {
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockSnippetService.loadById(snippetId)).thenReturn(mockSnippet);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockSnippet.getAuthor()).thenReturn(mockAccount);
         when(mockSnippet.getSnippetId()).thenReturn(snippetId);
         when(mockAccount.getUsername()).thenReturn(username);
         ModelAndView mav = snippetController.displaySnippet(snippetId);
 
         assertEquals("snippet/viewSnippet", mav.getViewName());
         assertEquals(mockSnippet, mav.getModel().get("snippet"));
        assertEquals("<!-- nothing important-->", mav.getModel().get("message"));
         assertEquals("<div style='float:right;'><a href='/snippet/edit/" + snippetId + "'>[edit]</a></div>", mav.getModel().get("editlink"));
     }
     @Test
     public void testDisplaySnippetNotFound() {
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockSnippetService.loadById(snippetId)).thenReturn(null);
 
         ModelAndView mav = snippetController.displaySnippet(snippetId);
 
        assertEquals("snippet/viewSnippet", mav.getViewName());
         assertEquals("I'm sorry, " + snippetId + " was not found.", mav.getModel().get("message"));
     }
     @Test
     public void testDisplaySnippetException() {
         stub(mockContext.getAuthentication()).toThrow(new IndexOutOfBoundsException());
 
         ModelAndView mav = snippetController.displaySnippet(snippetId);
 
         assertEquals("snippet/snippetFailure", mav.getViewName());
         assertEquals("Something failed while trying to display " + snippetId + ".", mav.getModel().get("message"));
         assertNull(mav.getModel().get("editlink"));
     }
 
     @Test
     public void testCreateSnippetForm() {
         ModelAndView mav = snippetController.createSnippetForm();
         assertEquals("snippet/editSnippet", mav.getViewName());
         assertEquals(EditSnippetForm.class, mav.getModel().get("editSnippetForm").getClass());
     }
 
     @Test
     public void testDefaultView() {
         ModelAndView mav = snippetController.defaultView();
         assertEquals("snippet/view", mav.getViewName());
         assertEquals("show the default view for snippets", mav.getModel().get("message"));
     }
 
 }
