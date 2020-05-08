 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.sample.wicket.view;
 
 import static org.easymock.EasyMock.anyInt;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.createNiceMock;
 import static org.easymock.classextension.EasyMock.replay;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.IMarkupResourceStreamProvider;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.spring.test.ApplicationContextMock;
 import org.apache.wicket.util.resource.IResourceStream;
 import org.apache.wicket.util.resource.StringResourceStream;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.globant.katari.hibernate.coreuser.domain.Role;
 import com.globant.katari.user.application.DeleteUserCommand;
 import com.globant.katari.user.application.Password;
 import com.globant.katari.user.application.Profile;
 import com.globant.katari.user.application.SaveUserCommand;
 import com.globant.katari.user.application.UserFilterCommand;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.filter.Paging;
 import com.globant.katari.tools.KatariWicketTester;
 
 public class UserPageTest {
 
   private DeleteUserCommand deleteUser;
 
   private ApplicationContextMock applicationContext;
 
   @Before
   public void setUp() {
 
     List<User> firstPage = new LinkedList<User>();
 
     List<Role> theRoles = new ArrayList<Role>();
     theRoles.add(new Role("test"));
     theRoles.add(new Role("test2"));
 
     UserFilterCommand emptyFilter = createNiceMock(UserFilterCommand.class);
     expect(emptyFilter.getPaging()).andReturn(new Paging());
     expectLastCall().anyTimes();
 
     expect(emptyFilter.execute()).andReturn(firstPage);
     expectLastCall().anyTimes();
     replay(emptyFilter);
 
     deleteUser = createMock(DeleteUserCommand.class);
     deleteUser.setUserId(0);
     expect(deleteUser.execute()).andReturn(null);
     replay(deleteUser);
 
     Profile profile = createMock(Profile.class);
     expect(profile.getName()).andReturn("name");
     expectLastCall().anyTimes();
     expect(profile.getEmail()).andReturn("email");
     expectLastCall().anyTimes();
     expect(profile.getRoles()).andReturn(new ArrayList<Role>());
     expectLastCall().anyTimes();
     replay(profile);
 
     Password password = createNiceMock(Password.class);
     expect(password.getNewPassword()).andReturn("pass");
     expectLastCall().anyTimes();
     expect(password.getConfirmedPassword()).andReturn("pass");
     expectLastCall().anyTimes();
     replay(password);
 
     SaveUserCommand saveNewUser = createMock(SaveUserCommand.class);
     saveNewUser.setUserId(anyInt());
     expectLastCall().anyTimes();
     saveNewUser.init();
     expectLastCall().anyTimes();
     expect(saveNewUser.getProfile()).andReturn(profile);
     expectLastCall().anyTimes();
     expect(saveNewUser.getPassword()).andReturn(password);
     expectLastCall().anyTimes();
     expect(saveNewUser.execute()).andReturn(null);
     expect(saveNewUser.getRoles()).andReturn(theRoles).anyTimes();
     replay(saveNewUser);
 
     applicationContext = new ApplicationContextMock();
    applicationContext.putBean("filterCommand", emptyFilter);
     applicationContext.putBean("deleteUserCommand", deleteUser);
     applicationContext.putBean("saveUserCommand", saveNewUser);
   }
 
   @Test
   public void testNewUser() {
     KatariWicketTester tester;
     tester = new KatariWicketTester(applicationContext);
     tester.startPage(new UserPage());
     tester.assertLabel("head-title-label", "New user");
     tester.assertModelValue("userForm:profile.name", "name");
     tester.assertModelValue("userForm:profile.email", "email");
     tester.assertModelValue("userForm:passwords:password.newPassword", "pass");
     tester.assertModelValue("userForm:passwords:password.confirmedPassword",
         "pass");
   }
 
   @Test
   public void testSaveUser() {
     KatariWicketTester tester;
     tester = new KatariWicketTester(applicationContext);
     tester.startPage(new UserPage(new PageParameters("id=1")));
     tester.assertLabel("head-title-label", "Edit user");
 
     tester.newFormTester("userForm").submit();
     /* Submiting the form with:
      *
      * tester.submitForm("userForm");
      *
      * does not work, looks like the form is not initialized correctly from the
      * data provided by the command.
      */
     // FormTester form = tester.newFormTester("userForm"); form.submit();
 
     tester.assertRenderedPage(UserListPage.class);
   }
     /*
   @Test
   public void testUserDelete() {
     // deletes a user. Does nothing in the page, but executes the delete
     // command.
     tester.clickLink("users:1:delete");
     tester.assertRenderedPage(UserListPage.class);
     verify(deleteUser);
   }
 
   @Test
   public void testUserEdit() {
     tester.clickLink("users:1:edit");
     tester.assertRenderedPage(UserPage.class);
   }
   */
 
   /* Page that contains a single link to a user page. Used for testing
    * UserPage.Link.
    */
   public static class PageWithLink extends WebPage implements
       IMarkupResourceStreamProvider {
 
     public IResourceStream getMarkupResourceStream(
         final MarkupContainer container, final Class<?> containerClass) {
       return new StringResourceStream(
           "<html><span wicket:id='edit'></span></html>");
     }
     public PageWithLink(final User user) {
       if (user == null) {
         add(new UserPage.Link("edit"));
       } else {
         add(new UserPage.Link("edit", user));
       }
     }
   }
 
   @Test
   public void testLink_noUser() {
     KatariWicketTester tester = new KatariWicketTester(applicationContext);
     tester.startPage(new PageWithLink(null));
     tester.clickLink("edit");
     tester.assertRenderedPage(UserPage.class);
   }
 
   @Test
   public void testLink_user() {
     KatariWicketTester tester = new KatariWicketTester(applicationContext);
     tester.startPage(new PageWithLink(new User("name", "password")));
     tester.clickLink("edit");
     tester.assertRenderedPage(UserPage.class);
     tester.assertModelValue("userForm:profile.name", "name");
   }
 }
 
