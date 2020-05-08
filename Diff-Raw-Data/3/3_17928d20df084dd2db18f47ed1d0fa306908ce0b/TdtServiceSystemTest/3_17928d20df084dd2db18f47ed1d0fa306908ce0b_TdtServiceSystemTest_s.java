 package ru.alepar.tdt.gwt.server;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
 import org.junit.Rule;
 import org.junit.Test;
 import ru.alepar.tdt.backend.dao.core.DaoSession;
 import ru.alepar.tdt.backend.dao.core.DaoSessionFactoryImpl;
 import ru.alepar.tdt.backend.model.UserAccount;
 import ru.alepar.tdt.backend.model.UserLogin;
 import ru.alepar.tdt.gwt.client.action.user.AddUser;
 import ru.alepar.tdt.testsupport.rules.LocalGae;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 /**
  * User: looser
  * Date: Jul 15, 2010
  */
 public class TdtServiceSystemTest {
     @Rule
     public LocalGae localGae = new LocalGae(
             new LocalUserServiceTestConfig(),
             new LocalDatastoreServiceTestConfig()
     );
 
     public static final UserLogin LOGIN = new UserLogin("user_login");
 
     @Test
     public void commandsAreConvertedAndResultIsStoredInTheDatabase() {
         User user = UserServiceFactory.getUserService().getCurrentUser();
         assertThat(user.getUserId(), equalTo(LocalGae.MY_USER_ID));
 
         new TdtServiceImpl().execute(new AddUser(LOGIN));
 
         assertUserExistsInDb(LocalGae.MY_USER_ID, LOGIN);
     }
 
     private void assertUserExistsInDb(String userId, UserLogin login) {
         DaoSession session = DaoSessionFactoryImpl.sessionInstance();
         session.open();
         try {
             UserAccount userAccount = session.userAccount().find(userId);
             assertThat(userAccount.getLogin(), equalTo(login));
         } finally {
             session.close();
         }
     }
 
 
 }
