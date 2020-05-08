 import models.User;
 import org.junit.Test;
 
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Josh
  * Date: 5/19/12
  * Time: 3:25 PM
  * To change this template use File | Settings | File Templates.
  */
 public class UserTest {
     @Test
     public void createAndRetrieveUser() {
         running(fakeApplication(), new Runnable() {
             @Override
             public void run() {
                 new User("bob@gmail.com", "secret", "Bob").save();
                User bob = User.find.all().get(0);
                 assertThat(bob).isNotNull();
                 assertThat("Bob").isEqualTo(bob.fullName);
                 assertThat("secret").isEqualTo(bob.password);
                 assertThat("bob@gmail.com").isEqualTo(bob.email);
             }
         });
     }
 }
