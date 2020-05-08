 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.security;
 
 import de.cismet.tools.gui.StaticSwingTools;
 import java.awt.Component;
 import java.net.URL;
import java.util.ResourceBundle;
 import java.util.prefs.Preferences;
 import javax.swing.JFrame;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
 import org.jdesktop.swingx.JXLoginPane;
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.auth.DefaultUserNameStore;
 import org.jdesktop.swingx.auth.LoginService;
 
 /**
  *
  * @author spuhl
  */
 public abstract class PasswordDialog extends LoginService {
 
     protected DefaultUserNameStore usernames;
     private Preferences appPrefs = null;
     private UsernamePasswordCredentials creds;
     protected Component parent;
     private JFrame parentFrame;
     protected  boolean isAuthenticationDone = false;
     private boolean isAuthenticationCanceled = false;
     protected URL url;
     private Object dummy = new Object();
     private String username = null;
     private String title;
     private String prefTitle;    
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PasswordDialog.class);
 
     public String getUserName() {
         return creds.getUserName();
     }
     
     public PasswordDialog(URL url) {
         super();
         if(log.isDebugEnabled())
             log.debug("Creating new PaswordDialog Instance for URL: " + url.toString());  //NOI18N
         this.url = url;        
     }
 
     public PasswordDialog(URL url, Component parentComponent) {
         this(url);
         if (parentComponent != null) {
             this.parent = (StaticSwingTools.getParentFrame(parentComponent));
             if (this.parent == null) {
                 this.parent = (StaticSwingTools.getFirstParentFrame(parentComponent));
             }
         }
 
 
     }
 
     public void setUsernamePassword(UsernamePasswordCredentials creds) {
         this.creds = creds;
     }
 
     public UsernamePasswordCredentials getCredentials()
             throws CredentialsNotAvailableException {
         if(log.isDebugEnabled())
             log.debug("Credentials requested for :" + url.toString() + " alias: " + title);  //NOI18N
         usernames = new DefaultUserNameStore();
         appPrefs = Preferences.userNodeForPackage(this.getClass());
         usernames.setPreferences(appPrefs.node("loginURLHash" + Integer.toString(url.toString().hashCode())));  //NOI18N
 
         if (creds != null) {            
             return creds;
         }
 
         synchronized (dummy) {            
             if (creds != null) {
                 return creds;
             }
             isAuthenticationCanceled = false;            
             requestUsernamePassword();
 
             return creds;
 
         }
     }
 
     private void requestUsernamePassword() throws CredentialsNotAvailableException {
         if(log.isDebugEnabled())
             log.debug("requestUsernamePassword"); //NOI18N
         JXLoginPane login = new JXLoginPane(this, null, usernames);
 
         String[] names = usernames.getUserNames();
         if (names.length != 0) {
             username = names[names.length - 1];
         }
 
         login.setUserName(username);
         title = WebAccessManager.getInstance().getServerAliasProperty(url.toString());
         if (title != null) {
             login.setMessage(org.openide.util.NbBundle.getMessage(PasswordDialog.class, "PasswordDialog.requestUsernamePassword().login.message")  //NOI18N
                     + " \"" + title + "\" ");  //NOI18N
         } else {
             title = url.toString();
             if (title.startsWith("http://") && title.length() > 21) {
                 title = title.substring(7, 21) + "...";  //NOI18N
             } else if (title.length() > 14) {
                 title = title.substring(0, 14) + "...";  //NOI18N
             }
 
             login.setMessage(org.openide.util.NbBundle.getMessage(PasswordDialog.class, "PasswordDialog.requestUsernamePassword().login.message")  //NOI18N
                     + "\n" + " \"" + title + "\" "); //NOI18N
         }
 
         if(log.isDebugEnabled())
             log.debug("parentFrame in GUICredentialprovider:" + parent);  //NOI18N
         JXLoginPane.JXLoginDialog dialog = new JXLoginPane.JXLoginDialog((JFrame) parent, login);
 
         try {
             ((JXPanel) ((JXPanel) login.getComponent(1)).getComponent(1)).getComponent(3).requestFocus();
         } catch (Exception skip) {
         }
         login.setVisible(true);
         dialog.setAlwaysOnTop(true);
         dialog.setVisible(true);
         
 
         if (!isAuthenticationDone) {
             isAuthenticationCanceled = true;
             throw new CredentialsNotAvailableException();
         }
     }
 
     public abstract boolean authenticate(String name, char[] password, String server) throws Exception;
 
     public boolean isAuthenticationCanceled() {
         return isAuthenticationCanceled;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
     
 }
