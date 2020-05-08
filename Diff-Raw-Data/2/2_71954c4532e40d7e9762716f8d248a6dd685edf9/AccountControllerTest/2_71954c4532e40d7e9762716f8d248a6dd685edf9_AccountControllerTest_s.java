 package com.morgajel.spoe.controller;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.servlet.ModelAndView;
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.model.Role;
 import com.morgajel.spoe.model.Snippet;
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.RoleService;
 import com.morgajel.spoe.service.SnippetService;
 import com.morgajel.spoe.web.EditAccountForm;
 import com.morgajel.spoe.web.RegistrationForm;
 import com.morgajel.spoe.web.SetPasswordForm;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests the Account Controller.
  * @author jmorgan
  *
  */
 public class AccountControllerTest {
     private AccountController accountController;
     private Account mockAccount;
     private Snippet mockSnippet;
     private SecurityContext mockContext;
     private Role mockRole;
     private AccountService mockAccountService;
     private EditAccountForm mockEditAccountForm;
     private RegistrationForm mockRegistrationForm;
     private RoleService mockRoleService;
     private MailSender mockMailSender;
     private SetPasswordForm mockPassForm;
     private SimpleMailMessage mockTemplateMessage;
     private VelocityEngine mockVelocityEngine;
     private static final String username = "morgo2";
     private static final String firstname = "Jesse";
     private static final String lastname = "Morgan";
     private static final String email = "morgo2@example.com";
     private static final String passfield = "255edd2793e5286d4441ea6bfba734b59e915864";
     private static final String password = "MatchedLuggage12345";
     //private final String tempHash="df9dd14cbdb3b00f8a54b66f489241e8aeb903ff";
     private static final String checksum = "279d8d8a18b94782ef606fbbadd6c011b1692ad0"; //morgo2+temphash+0
 
     /**
      * Create the intial mockups and classes that are used with each run.
      * @throws Exception generic exception
      */
     @Before
     public void setUp() throws Exception {
         mockAccountService = mock(AccountService.class);
         mockRoleService = mock(RoleService.class);
         mockAccount = mock(Account.class);
         mockContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
         mockRole = mock(Role.class);
         mockSnippet = mock(Snippet.class);
         mockMailSender = mock(MailSender.class);
         mockPassForm = mock(SetPasswordForm.class);
         mockRegistrationForm = mock(RegistrationForm.class);
         mockEditAccountForm = mock(EditAccountForm.class);
         mockTemplateMessage = mock(SimpleMailMessage.class);
         mockVelocityEngine = mock(VelocityEngine.class);
         List<Account> accountList = new ArrayList();
         accountList.add(mockAccount);
         List<Snippet> snippetList = new ArrayList();
         snippetList.add(mockSnippet);
         accountController = new AccountController();
         accountController.setAccountService(mockAccountService);
         accountController.setRoleService(mockRoleService);
         accountController.setMailSender(mockMailSender);
         accountController.setTemplateMessage(mockTemplateMessage);
         accountController.setVelocityEngine(mockVelocityEngine);
     }
     /**
      * Tears down all mockups and classes between each test.
      * @throws Exception generic exception
      */
     @After
     public void tearDown() throws Exception {
         mockAccountService = null;
         mockAccount = null;
         mockRole = null;
         mockMailSender = null;
         mockPassForm = null;
         mockEditAccountForm = null;
         mockTemplateMessage = null;
         mockVelocityEngine = null;
         mockContext = null;
         accountController = null;
     }
     /**
      * Test ActivateAccount's Best case scenario.
      */
     @Test
     public void testActivateAccountBestcase() {
 
         // Test Bestcase scenario success
         when(mockAccount.getPassword()).thenReturn(passfield);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccount.getEnabled()).thenReturn(false);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockAccountService.loadByUsernameAndChecksum(username, checksum)).thenReturn(mockAccount);
         ModelAndView results = accountController.activateAccount(username, checksum, new SetPasswordForm());
         assertEquals("account/activationSuccess", results.getViewName());
     }
     /**
      * Test ActivateAccount when there is no matching account.
      */
     @Test
     public void testActivateAccountNoMatch() {
 
         // non-existent account failure
         when(mockAccountService.loadByUsername(username)).thenReturn(null);
         when(mockAccountService.loadByUsernameAndChecksum(username, checksum)).thenReturn(null);
         ModelAndView results = accountController.activateAccount(username, checksum, new SetPasswordForm());
         assertEquals("account/activationFailure", results.getViewName());
         //TODO assertEquals("I'm sorry, that account was not found.",results.someting());
     }
     /**
      * Test AciveAccount mismatched checksum.
      */
     @Test
     public void testActivateAccountChecksumMismatch() {
         //Test checksum mismatch
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         when(mockAccount.getPassword()).thenReturn("some wrong passfield");
         when(mockAccount.getEnabled()).thenReturn(false);
         ModelAndView results = accountController.activateAccount(username, checksum, new SetPasswordForm());
         assertEquals("account/activationFailure", results.getViewName());
     }
     /**
      * Test AcivateAccount when account is already Activated.
      */
     @Test
     public void testActivateAccountAlreadyActivated() {
         //Test already enabled failure
         when(mockAccountService.loadByUsernameAndChecksum(username, checksum)).thenReturn(mockAccount);
         when(mockAccount.getEnabled()).thenReturn(true);
         ModelAndView results = accountController.activateAccount(username, checksum, new SetPasswordForm());
         assertEquals("account/activationFailure", results.getViewName());
 
     }
     /**
      * Test ActivateAccount when something throws an Exception.
      */
     @Test
     public void testActivateAccountThrowException() {
         //Test throw exception caught
         stub(mockAccountService.loadByUsernameAndChecksum(username, checksum)).toThrow(new IndexOutOfBoundsException());
         ModelAndView results = accountController.activateAccount(username, checksum, new SetPasswordForm());
         assertEquals("account/activationFailure", results.getViewName());
         assertEquals("<!--java.lang.IndexOutOfBoundsException-->", results.getModel().get("message"));
     }
     /**
      * Test the Registration Mail Sender to ensure the gears spin.
      */
     @Test
     public void testSendRegEmail() {
         when(mockAccount.getPassword()).thenReturn(passfield);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccount.getEnabled()).thenReturn(false);
 
         accountController.sendRegEmail(mockAccount, "http://example.com/sometesturl");
         verify(mockMailSender).send((SimpleMailMessage) anyObject());
 
     }
 
     /**
      * Test the Set and Get Template Messages.
      */
     @Test
     public void testSetAndGetTemplateMessage() {
         SimpleMailMessage simpleMailMessage = mock(SimpleMailMessage.class);
         accountController.setTemplateMessage(simpleMailMessage);
         //using deep stubs, boooo
         assertEquals(accountController.getTemplateMessage(), simpleMailMessage);
     }
     /**
      * Test the Set and Get Mail Sender.
      */
     @Test
     public void testSetAndGetMailSender() {
         MailSender mailSender = mock(MailSender.class);
         accountController.setMailSender(mailSender);
         //using deep stubs, boooo
         assertEquals(accountController.getMailSender(), mailSender);
     }
     /**
      * Test the Set and get for the velocity engine.
      */
     @Test
     public void testSetAndGetVelocityEngine() {
         VelocityEngine velocityEngine = mock(VelocityEngine.class);
         accountController.setVelocityEngine(velocityEngine);
         //using deep stubs, boooo
         assertEquals(accountController.getVelocityEngine(), velocityEngine);
     }
     /**
      * Test the results of the Default View.
      */
     @Test
     public void testDefaultView() {
         SecurityContextHolder.setContext(mockContext);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         ModelAndView mav = accountController.defaultView();
         assertEquals("account/view", mav.getViewName());
         assertEquals("show the default view for " + username, mav.getModel().get("message"));
     }
     /**
      * Test to make sure the Registration form is displayed.
      */
     @Test
     public void testGetRegistrationForm() {
         ModelAndView mav = accountController.getRegistrationForm(mockRegistrationForm);
         assertEquals(mav.getViewName(), "account/registrationForm");
     }
     /**
      * Text CreateAccount to make sure it works.
      */
     @Test
     public void testCreateAccountSuccess() {
 
         when(mockRegistrationForm.getUsername()).thenReturn(username);
         when(mockRegistrationForm.getFirstname()).thenReturn(firstname);
         when(mockRegistrationForm.getLastname()).thenReturn(lastname);
         when(mockRegistrationForm.getEmail()).thenReturn(email);
         when(mockRegistrationForm.getConfirmEmail()).thenReturn(email);
 
         when(mockRoleService.loadByName("ROLE_REVIEWER")).thenReturn(mockRole);
         ModelAndView result = accountController.createAccount(mockRegistrationForm, null);
         Account account = (Account) result.getModel().get("account");
 
         verify(mockAccountService).addAccount((Account) anyObject());
         verify(mockRoleService).loadByName("ROLE_REVIEWER");
 
         verify(mockAccountService).saveAccount((Account) anyObject());
        assertEquals(accountController.getActivationUrl() + username + "/" + account.activationChecksum(),
                     result.getModel().get("url"));
         assertEquals("account/registrationSuccess", result.getViewName());
     }
     /**
      * Test CreateAccount with a bad email address.
      */
     @Test
     public void testCreateAccountBadEmail() {
         when(mockRegistrationForm.getUsername()).thenReturn(username);
         when(mockRegistrationForm.getFirstname()).thenReturn(firstname);
         when(mockRegistrationForm.getLastname()).thenReturn(lastname);
         when(mockRegistrationForm.getEmail()).thenReturn(email);
         when(mockRegistrationForm.getConfirmEmail()).thenReturn("wrong@Email.com");
 
         ModelAndView result = accountController.createAccount(mockRegistrationForm, null);
         assertEquals("account/registrationForm", result.getViewName());
         assertEquals("Sorry, your Email addresses didn't match.", result.getModel().get("message"));
     }
     /**
      * Test CreateAccount when an Exception is thrown.
      */
     @Test
     public void testCreateAccountException() {
         stub(mockAccount.getUsername()).toThrow(new IndexOutOfBoundsException());
         ModelAndView result = accountController.createAccount(mockRegistrationForm, null);
         assertEquals("account/registrationForm", result.getViewName());
         assertEquals("There was an issue creating your account."
                 + "Please contact the administrator for assistance.", result.getModel().get("message"));
     }
     /**
      * Test to make sure SetPassword works.
      */
     @Test
     public void testSetPasswordSuccess() {
         when(mockPassForm.getPassword()).thenReturn(passfield);
         when(mockPassForm.getConfirmPassword()).thenReturn(passfield);
         when(mockPassForm.getUsername()).thenReturn(username);
         when(mockPassForm.getChecksum()).thenReturn(checksum);
         when(mockAccountService.loadByUsernameAndChecksum(username, checksum)).thenReturn(mockAccount);
 
         ModelAndView result = accountController.setPassword(mockPassForm);
 
         verify(mockAccount).setEnabled(true);
         verify(mockAccount).setHashedPassword(passfield);
         assertEquals("redirect:/", result.getViewName());
     }
     /**
      * Test SetPassword when failing.
      */
     @Test
     public void testSetPasswordFail() {
         when(mockPassForm.getPassword()).thenReturn(passfield);
         when(mockPassForm.getConfirmPassword()).thenReturn("mismatching passfield");
 
         ModelAndView result = accountController.setPassword(mockPassForm);
 
         assertEquals("account/activationSuccess", result.getViewName());
         assertEquals("Your passwords did not match, try again.", result.getModel().get("message"));
         assertEquals(mockPassForm, result.getModel().get("passform"));
     }
     /**
      * Test GetContextAccount to pull a user's account from the Security Context.
      */
     @Test
     public void testGetContextAccount() {
         SecurityContextHolder.setContext(mockContext);
         when(mockContext.getAuthentication().getName()).thenReturn(username);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
         Account account = accountController.getContextAccount();
         assertEquals(mockAccount, account);
     }
     /**
      * Test displaying the EditAccountForm to make sure it returns properly.
      */
     @Test
     public void testEditAccountForm() {
         when(mockEditAccountForm.getFirstname()).thenReturn(firstname);
         when(mockEditAccountForm.getLastname()).thenReturn(lastname);
         when(mockEditAccountForm.getPassword()).thenReturn(password);
         when(mockEditAccountForm.getConfirmPassword()).thenReturn(password);
         when(mockEditAccountForm.getEmail()).thenReturn(email);
         when(mockEditAccountForm.getConfirmEmail()).thenReturn(email);
 
         ModelAndView results = accountController.editAccountForm(mockEditAccountForm);
         assertEquals(mockEditAccountForm, results.getModel().get("eaForm"));
         assertEquals("account/editAccountForm", results.getViewName());
     }
     /**
      * Text submitting an Account Edit Form.
      * TODO make account testing more robust.
      */
     @Test
     public void testEditAccountFormSubmit() {
         when(mockEditAccountForm.getFirstname()).thenReturn(firstname);
         when(mockEditAccountForm.getLastname()).thenReturn(lastname);
         when(mockEditAccountForm.getPassword()).thenReturn(password);
         when(mockEditAccountForm.getConfirmPassword()).thenReturn(password);
         when(mockEditAccountForm.getEmail()).thenReturn(email);
         when(mockEditAccountForm.getConfirmEmail()).thenReturn(email);
         ModelAndView results = accountController.saveEditAccountForm(mockEditAccountForm);
 
         assertEquals(mockEditAccountForm, results.getModel().get("eaForm"));
         assertEquals("your form has been submitted, but this is currently unimplemented...", results.getModel().get("message"));
         assertEquals("account/editAccountForm", results.getViewName());
     }
     /**
      * Test DisplayUser finding a user.
      */
     @Test
     public void testDisplayUserSuccess() {
         when(mockAccount.getPassword()).thenReturn(passfield);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccount.getFirstname()).thenReturn(firstname);
         when(mockAccount.getLastname()).thenReturn(lastname);
         when(mockAccount.getEmail()).thenReturn(email);
         when(mockAccountService.loadByUsername(username)).thenReturn(mockAccount);
 
         ModelAndView mav = accountController.displayUser(username);
 
         assertEquals(username, mav.getModel().get("message"));
         assertEquals("account/viewUser", mav.getViewName());
         assertEquals(mockAccount, mav.getModel().get("account"));
     }
     /**
      * Test DisplayUser when a user is not found.
      */
     @Test
     public void testDisplayUserNotFound() {
         when(mockAccount.getPassword()).thenReturn(passfield);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccount.getFirstname()).thenReturn(firstname);
         when(mockAccount.getLastname()).thenReturn(lastname);
         when(mockAccount.getEmail()).thenReturn(email);
         when(mockAccountService.loadByUsername(username)).thenReturn(null);
 
         ModelAndView mav = accountController.displayUser(username);
 
         assertEquals("I'm sorry, " + username + " was not found.", mav.getModel().get("message"));
         assertEquals("account/viewUser", mav.getViewName());
     }
     /**
      * Test DisplayUser when an Exception is thrown.
      */
     @Test
     public void testDisplayUserException() {
         when(mockAccount.getPassword()).thenReturn(passfield);
         when(mockAccount.getUsername()).thenReturn(username);
         when(mockAccount.getFirstname()).thenReturn(firstname);
         when(mockAccount.getLastname()).thenReturn(lastname);
         when(mockAccount.getEmail()).thenReturn(email);
         stub(mockAccountService.loadByUsername(username)).toThrow(new IndexOutOfBoundsException());
 
         ModelAndView mav = accountController.displayUser(username);
 
         assertEquals("Something failed while trying to display " + username, mav.getModel().get("message"));
         assertEquals("account/activationFailure", mav.getViewName());
     }
 }
