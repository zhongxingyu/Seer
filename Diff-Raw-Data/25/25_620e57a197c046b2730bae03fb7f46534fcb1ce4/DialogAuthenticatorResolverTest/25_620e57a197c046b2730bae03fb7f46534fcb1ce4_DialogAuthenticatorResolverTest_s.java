 package tfdhs.core.ui;
 
 import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
 import java.net.Authenticator;
 import java.net.PasswordAuthentication;
 
import javax.swing.JComponent;
 import javax.swing.JDialog;
import javax.swing.JLabel;
 import javax.swing.JOptionPane;
import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import org.junit.Before;
 import org.junit.Test;
import static org.mockito.Mockito.*;
import tfdhs.api.AuthenticatorResolver;
import static org.junit.Assert.*;
import static tfdhs.core.ui.HttpClientWindowTest.getChildNamed;
 
 /**
  * Display a dialog where the user can enter username/password.
  * 
  * @author frode
  * 
  */
 public class DialogAuthenticatorResolverTest {
 
     private DialogAuthenticatorResolver resolver;
 
     @Before
     public void setUp() {
 	resolver = new DialogAuthenticatorResolver(null);
     }
 
     @Test
     public void assertUsernameField() {
 
 	Component comp = getChildNamed(resolver.getDialog(), "usernameField");
 
 	assertTrue("Missing username field in dialog",
 		comp instanceof JTextField);
     }
 
     @Test
     public void assertPasswordField() {
 
 	Component comp = getChildNamed(resolver.getDialog(), "passwordField");
 
 	assertTrue("Missing password field in dialog",
 		comp instanceof JPasswordField);
     }
 
     @Test
     public void testAuthenticateDialogShown() {
 	final Authenticator mockAuthenticator = mock(Authenticator.class);
 	final JDialog mockDialog = mock(JDialog.class);
 	resolver = new DialogAuthenticatorResolver(null) {
 	    protected JDialog getDialog() {
 		return mockDialog;
 	    }
 	};
 
 	resolver.authenticate(mockAuthenticator);
 
 	verify(mockDialog).setVisible(true);
 	verify(mockDialog).dispose();
 
     }
 
     @Test
     public void testAuthenticateDialogOkClicked() {
 	final Authenticator mockAuthenticator = mock(Authenticator.class);
 	final JDialog mockDialog = mock(JDialog.class);
 	resolver = new DialogAuthenticatorResolver(null) {
 	    protected JDialog getDialog() {
 		return mockDialog;
 	    }
 
 	    protected boolean userClickedOk() {
 		return true;
 	    }
 	};
 
 	resolver.setUsername("username");
 	resolver.setPassword("password");
 
 	PasswordAuthentication auth = resolver.authenticate(mockAuthenticator);
 
 	assertNotNull("PasswordAuthentication was null", auth);
 	assertEquals("Username", "username", auth.getUserName());
 	assertEquals("Password", "password", new String(auth.getPassword()));
 
     }
 
     @Test
     public void testAuthenticateDialogAborted() {
 	final Authenticator mockAuthenticator = mock(Authenticator.class);
 	final JDialog mockDialog = mock(JDialog.class);
 	resolver = new DialogAuthenticatorResolver(null) {
 	    protected JDialog getDialog() {
 		return mockDialog;
 	    }
 
 	    protected boolean userClickedOk() {
 		return false;
 	    }
 	};
 
 	resolver.setUsername("username");
 	resolver.setPassword("password");
 
 	PasswordAuthentication auth = resolver.authenticate(mockAuthenticator);
 
 	assertNull("PasswordAuthentication was not null", auth);
 
     }
 
     @Test
     public void testUserClickedOk() {
 
 	final JOptionPane mockPane = mock(JOptionPane.class);
 	resolver = new DialogAuthenticatorResolver(null) {
 
 	    protected JOptionPane getInputPane() {
 		return mockPane;
 	    }
 	};
 
 	when(mockPane.getValue()).thenReturn(JOptionPane.UNINITIALIZED_VALUE);
 	assertFalse("was ok", resolver.userClickedOk());
 
 	when(mockPane.getValue()).thenReturn(JOptionPane.NO_OPTION);
 	assertFalse("was ok", resolver.userClickedOk());
 
 	when(mockPane.getValue()).thenReturn(JOptionPane.YES_OPTION);
 	assertTrue("Was not ok", resolver.userClickedOk());
 
     }
 
 }
