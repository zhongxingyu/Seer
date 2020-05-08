 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.sample.wicket.view;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.createNiceMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.wicket.spring.test.ApplicationContextMock;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.DirectFieldAccessor;
 
 import com.globant.katari.hibernate.coreuser.domain.Role;
 import com.globant.katari.sample.testsupport.SecurityTestUtils;
 import com.globant.katari.user.application.DeleteUserCommand;
 import com.globant.katari.user.application.Password;
 import com.globant.katari.user.application.Profile;
 import com.globant.katari.user.application.SaveUserCommand;
 import com.globant.katari.user.application.UserFilterCommand;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.filter.Paging;
 
 import com.globant.katari.tools.KatariWicketTester;
 
 public class UserListPageTest {
 
   private KatariWicketTester tester;
 
   private DeleteUserCommand deleteUser;
 
   @Before
   public void setUp() {
     User user = new User("admin", "admin@none");
     // Change the id, otherwise the delete button will always be disabled.
     new DirectFieldAccessor(user).setPropertyValue("id", 1000);
     SecurityTestUtils.setContextUser(user);
 
     List<User> firstPage = new LinkedList<User>();
     firstPage.add(new User("name1", "email1"));
     firstPage.add(new User("name2", "email2"));
     firstPage.add(new User("name3", "email3"));
     firstPage.add(new User("name4", "email4"));
     firstPage.add(new User("name5", "email5"));
 
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
     expect(profile.getName()).andReturn("name1");
     expect(profile.getEmail()).andReturn("email1");
     expect(profile.getRoles()).andReturn(new ArrayList<Role>());
     expectLastCall().anyTimes();
     replay(profile);
 
     Password password = createMock(Password.class);
     expect(password.getNewPassword()).andReturn("none");
     expect(password.getConfirmedPassword()).andReturn("none");
     replay(password);
 
     List<Role> theRoles = new ArrayList<Role>();
     theRoles.add(new Role("test"));
     theRoles.add(new Role("test2"));
 
     SaveUserCommand saveUser = createMock(SaveUserCommand.class);
     saveUser.setUserId(0);
     saveUser.init();
     expect(saveUser.getProfile()).andReturn(profile);
     expectLastCall().anyTimes();
     expect(saveUser.getPassword()).andReturn(password);
     expectLastCall().anyTimes();
     expect(saveUser.execute()).andReturn(null);
     expect(saveUser.getRoles()).andReturn(theRoles).anyTimes();
     replay(saveUser);
 
     ApplicationContextMock applicationContext = new ApplicationContextMock();
    applicationContext.putBean("userFilterCommand", emptyFilter);
     applicationContext.putBean("deleteUserCommand", deleteUser);
     applicationContext.putBean("saveUserCommand", saveUser);
 
     tester = new KatariWicketTester(applicationContext);
     tester.startPage(new UserListPage());
   }
 
   @Test
   public void testUserList() {
     // Looks like the data view starts counting from 1.
     tester.assertLabel("users:1:name", "name1");
     tester.assertLabel("users:1:email", "email1");
     tester.assertLabel("users:5:name", "name5");
     tester.assertLabel("users:5:email", "email5");
   }
 
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
 }
 
