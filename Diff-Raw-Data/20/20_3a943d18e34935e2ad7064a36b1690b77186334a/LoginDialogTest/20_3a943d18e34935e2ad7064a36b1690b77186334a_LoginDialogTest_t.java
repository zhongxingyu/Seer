 package sonique.bango;
 
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 import sonique.bango.app.SupermanApp;
 import sonique.bango.driver.panel.LoginDialog;
 import sonique.bango.driver.panel.MessageBox;
import sonique.bango.matcher.IsDisplayed;
import sonique.testsupport.matchers.AppendableAllOf;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.isEmptyString;
import static sonique.bango.matcher.AMessageOf.aMessageOf;
import static sonique.bango.matcher.ATitleOf.aTitleOf;
 import static sonique.bango.matcher.IsDisabled.isDisabled;
 import static sonique.bango.matcher.IsEnabled.isEnabled;
 
 public class LoginDialogTest extends OncePerSuiteBangoTest {
 
     private LoginDialog loginDialog;
     private SupermanApp supermanApp;
 
     @Before
     public void setUp() throws Exception {
         supermanApp = bangoTestEnvironment.borrowSupermanApp();
         loginDialog = supermanApp.loginDialog();
         loginDialog.username().clear();
         loginDialog.password().clear();
     }
 
     @After
     public void tearDown() throws Exception {
         bangoTestEnvironment.releaseSupermanApp(supermanApp);
     }
 
     @Test
     public void loginButtonIsDisabledWhenNoFieldsPopulated() throws Exception {
         assertThat(loginDialog.loginButton(), isDisabled());
     }
 
     @Test
     public void loginButtonIsDisabledWhenOnlyUsernameFieldIsPopulated() throws Exception {
         loginDialog.username().enter("a.a");
         loginDialog.password().clear();
 
         assertThat(loginDialog.loginButton(), isDisabled());
     }
 
     @Test
     public void loginButtonIsEnabledWhenBothFieldsArePopulated() throws Exception {
         loginDialog.username().enter("a.a");
         loginDialog.password().enter("asds");
 
         assertThat(loginDialog.loginButton(), isEnabled());
     }
 
     @Test
     public void resetButtonEmptiesTheFieldsAndDisablesTheLoginButton() throws Exception {
         loginDialog.username().enter("a.a");
         loginDialog.password().enter("dsfsdfs");
 
         loginDialog.resetButton().click();
 
         assertThat(loginDialog.username().value(), isEmptyString());
         assertThat(loginDialog.password().value(), isEmptyString());
         assertThat(loginDialog.loginButton(), isDisabled());
     }
 
     @Test
     public void showsErrorWhenIncorrectDetailsAreEntered() throws Exception {
         loginDialog.username().enter("wilbur");
         loginDialog.password().enter("junk");
 
         loginDialog.loginButton().click();
 
         MessageBox notificationWindow = supermanApp.messageBox();
        assertThat(notificationWindow, isDisplayed().with(aTitleOf("Error")).and(aMessageOf("Bad Credentials")));
         notificationWindow.clickOk();
     }

    private AppendableAllOf<MessageBox> isDisplayed() {
        return IsDisplayed.isDisplayed();
    }
 }
