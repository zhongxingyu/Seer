 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.cannedapps.cachat.app.presenters.ChatPresenter;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 import fakes.TestingMessageView;
 
 @RunWith(RobolectricTestRunner.class)
 public class MessagePresenterTest {
 
   @Test
   public void testFirst() {
     TestingMessageView view = new TestingMessageView();
     ChatPresenter presenter = new ChatPresenter(view);
     
     presenter.addMessage("hello");  // << This is the method we want to test
     
    assertThat(view.lastMessage, equalTo("hello\n"));
   }
 }
