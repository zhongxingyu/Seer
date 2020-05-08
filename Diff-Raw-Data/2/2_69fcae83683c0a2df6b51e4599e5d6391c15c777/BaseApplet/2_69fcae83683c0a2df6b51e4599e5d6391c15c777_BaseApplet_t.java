 package org.jbundle.thin.base.screen;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.SystemColor;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JApplet;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.OverlayLayout;
 import javax.swing.SwingUtilities;
 
 import org.jbundle.model.App;
 import org.jbundle.model.BaseAppletReference;
 import org.jbundle.model.Freeable;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.RecordOwnerParent;
 import org.jbundle.model.Task;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.FieldTable;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.db.ThinPhysicalDatabase;
 import org.jbundle.thin.base.db.ThinPhysicalDatabaseParent;
 import org.jbundle.thin.base.db.ThinPhysicalTable;
 import org.jbundle.thin.base.db.mem.base.PhysicalDatabaseParent;
 import org.jbundle.thin.base.remote.RemoteSession;
 import org.jbundle.thin.base.remote.RemoteTable;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.screen.comp.ChangePasswordDialog;
 import org.jbundle.thin.base.screen.comp.JStatusbar;
 import org.jbundle.thin.base.screen.comp.JTiledImage;
 import org.jbundle.thin.base.screen.comp.LoginDialog;
 import org.jbundle.thin.base.screen.landf.ScreenDialog;
 import org.jbundle.thin.base.screen.landf.ScreenUtil;
 import org.jbundle.thin.base.screen.util.html.JHelpPane;
 import org.jbundle.thin.base.screen.util.html.JHtmlEditor;
 import org.jbundle.thin.base.thread.SyncPage;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.RecordOwnerCollection;
 import org.jbundle.thin.base.util.ThinMenuConstants;
 import org.jbundle.thin.base.util.Util;
 import org.jbundle.thin.base.util.base64.Base64;
 
 /**
  * The base class for all Applets and stand alone (applet) tasks.
  * For stand-alone tasks this object is the top-level screen.
  * Generally, if you override this class you should not provide too
  * much functionality (maybe a main method and addSubPanels method)
  * as usually the applet stays the same and the screen changes.
  * <p/>There are several parameters that applet looks for:
  * <pre>
  * screen=.package.Screenclass
  * background=image/backgroundimage
  * backgroundcolor=#FFFFCC
  * </pre>
  */
 public class BaseApplet extends JApplet
     implements Task, SyncPage, BaseAppletReference
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
      * The parent application that this applet/screen belongs to.
      */
     protected Application m_application = null;
     /**
      * Standalone?
      */
     protected static boolean gbStandAlone = false;
     /**
      * The image to tile for the background (passed in as background=xxx).
      */
     protected ImageIcon m_imageBackground = null;
     /**
      * The screen background color (passed in a backgroundcolor=color).
      */
     protected Color m_colorBackground = null;
     /**
      * Status bar (for standalone apps).
      */
     protected JStatusbar m_statusbar = null;
     /**
      * Help pane.
      */
     protected JHtmlEditor m_helpEditor = null;
     /**
      * Last Error message.
      */
     protected String m_strLastError = Constants.BLANK;
     /**
      * Last display message.
      */
     protected String m_strCurrentStatus = null;
     /**
      * Last display level.
      */
     protected int m_iCurrentWarningLevel = Constants.INFORMATION;
     /**
      * The last error code assigned (if the error code matches thin, then m_strLastError is the error.
      */
     protected static int m_iLastErrorCode = -2;
     /**
      * Strictly for passing standalone args.
      */
     private static String[] m_args = null;
     /**
      * The params that are local to this Task.
      */
     protected Map<String,Object> m_properties = null;
     /**
      * If you don't want a status bar, set this to false in init.
      */
     protected boolean m_bAddStatusbar = true;
     /**
      *  History of sub-screens contained in this Applet Screen (for the 'back' command).
      */
     protected Vector<String> m_vHistory = null;
     /**
      * The browser manager for browser environments.
      */
     protected BrowserManager m_browserManager = null;
     /**
      * Right above the first JScreen.
      */
     protected Container m_parent = null;
     /**
      * Children record owners.
      */
     protected RecordOwnerCollection m_recordOwnerCollection = null;
 
     /**
      * Default constructor.
      */
     public BaseApplet()
     {
         super();
         if (Application.getRootApplet() == null)
         	Application.setRootApplet(this);   // The one and only
     }
     /**
      * BaseApplet Class Constructor.
      * This is rarely used, except to make a new window.
      * @param args Arguments in standalone pass-in format.
      */
     public BaseApplet(String[] args)
     {
         this();
         this.init(args);
     }
     /**
      * Initializes the applet.
      * @param args Arguments in standalone pass-in format.
      */
     public void init(String[] args)
     {
         BaseApplet applet = null;
         if (!gbStandAlone)
             applet = this;
         if (m_application == null)
         {
             Map<String,Object> properties = null;
             if (args != null)
             {
                 properties = new Hashtable<String,Object>();
                 Util.parseArgs(properties, args);
             }
             m_application = new ThinApplication(null, properties, applet);      // Start the session for this user
         }
         m_application.addTask(this, null);                    // Add this session to the list
         
         m_recordOwnerCollection = new RecordOwnerCollection(this);
 
         try   {	// Add browser connection if running as an applet
             Class.forName("netscape.javascript.JSObject"); // Test if this exists
             Map<String,Object> mapInitialCommand = new HashMap<String,Object>();
             String strInitialCommand = this.getInitialCommand(true);
             if ((strInitialCommand != null) && (strInitialCommand.length() > 0))
             	Util.parseArgs(mapInitialCommand, strInitialCommand);
             BrowserManager bm = new BrowserManager(this, mapInitialCommand);	// This will throw an exception if there is a problem
             this.setBrowserManager(bm);
         } catch (Exception ex)  { // Ignore if no browser stuff
         }
         if (m_application.getProperty("hash") != null)
         {	// Special case - html hash params get added (and override) initial params
             Map<String,Object> properties = this.getProperties();
             if (properties == null)
             	properties = new Hashtable<String,Object>();
         	Util.parseArgs(properties, m_application.getProperty("hash"));
         	this.setProperties(properties);
         }
         m_parent = this.addBackground(this);
         this.addSubPanels(m_parent);    // Add any sub-panels now
 
         this.setupLookAndFeel(null);
         
         if (this.getBrowserManager() != null)
         	if ((this.getClass().getName().indexOf(".thin.") != -1) || (this.getClass().getName().indexOf(".Thin") != -1))
         		this.repaint();		// HACK - On thin if this is in an applet window, the paint sequence displays the overlays backward
     }
     /**
      * APPLET INFO SUPPORT:
      *      The getAppletInfo() method returns a string describing the applet's
      * author, copyright date, or miscellaneous information.
      * @return The applet info.
      */
     public String getAppletInfo()
     {
         return "Name: BaseApplet\r\n" +
                "Author: Don Corley\r\n" +
                "Version 1.0.0";
     }
     /**
      * Free all the resources belonging to this applet.
      * If all applet screens are closed, shut down the applet.
      */
     public void free()
     {
         if (BaseApplet.getSharedInstance() != null)
         	if (BaseApplet.getSharedInstance().getApplet() == null)
         		if (this.getParent() != null)
         			this.getParent().remove(this);      // Remove from frame
         if (this.getHelpView() != null)
         	this.getHelpView().free();
         this.freeSubComponents(this); // Free all the sub-screens.
     	if (m_recordOwnerCollection != null)
     		m_recordOwnerCollection.free();
     	m_recordOwnerCollection = null;
         boolean bEmptyTaskList = true;
         if (m_application != null)
        	bEmptyTaskList = m_application.removeTask(this);  // Remove this session from the list
         if (bEmptyTaskList)
             this.quit();
         if (Application.getRootApplet() == this)
         	Application.setRootApplet(null);
         if (BaseApplet.getSharedInstance() == this)
         {
         	if (m_application.getTaskList() != null)
         	{
 	            for (Task task : m_application.getTaskList().keySet())
 	            {
 	                if (task instanceof BaseApplet)
 	                	Application.setRootApplet((BaseApplet)task);     // New main applet
 	            }
         	}
         }
         m_application = null;
     }
     /**
      * This application is done, stop the application.
      */
     public void quit()
     {
         m_application.free();   // No more frames -> Quit java!
         //if (gbStandAlone)
         //	System.exit(0);
     }
     /**
      * For Stand-alone initialization.
      * In the overriding app must do one of the following:
      * <pre>
      *  BaseApplet.main(args);  // call this
      *  SApplet applet = new SApplet();     // Create your applet object
      *  applet.init();
      *  applet.start();                         // This calls init(args)
      *      (or)
      *  BaseApplet.main(args);  // call this
      *  SApplet applet = new SApplet(args);     // Create your applet object - this calls init(args)
      *      (or)
      *  SApplet applet = new SApplet();
      *  BaseApplet.main(args);  // call this
      *  </pre>
      * @param args Arguments in standalone pass-in format.
      */
     public static void main(String[] args)
     {
         gbStandAlone = true;
         m_args = args;      // For a standalone app, you must save the args to parse them later.
         if (Application.getRootApplet() != null)
         {
         	Application.getRootApplet().init();
         	Application.getRootApplet().start();
         }
     }
     /**
      * Add any applet sub-panel(s) now.
      * Usually, you override this, although for a simple screen, just pass a screen=class param.
      * @param parent The parent to add the new screen to.
      * @return true if success
      */
     public boolean addSubPanels(Container parent)
     {
         if (parent == null)
             parent = m_parent;
         String strScreen = this.getProperty(Params.SCREEN);
         JBasePanel baseScreen = (JBasePanel)Util.makeObjectFromClassName(strScreen);
         if (baseScreen != null)
         {
             FieldList record = null;
             baseScreen.init(this, record);  // test
             return this.changeSubScreen(parent, baseScreen, null);   // You must manually push the history command
         }
         return false;	// Nothing happened
     }
     /**
      * This is just for convenience. A simple way to change or set the screen for this applet.
      * The old screen is removed and the new screen is added.
      * This method is also used to switch a sub-screen to a new sub-screen.
      * @param parent The (optional) parent to add this screen to (BaseApplet keeps track of the parent).
      * @param baseScreen The new top screen to add.
      * @param strCommandToPush Optional command to push on the stack to re-display this screen.
      */
     public boolean checkSecurity(JBasePanel baseScreen)
     {
         if ((this.getApplication() == null) || (baseScreen == null))
             return true;    // Never
         int iErrorCode = baseScreen.checkSecurity(this.getApplication());
         
         if (iErrorCode == Constants.READ_ACCESS)
         {
             int iLevel = Constants.LOGIN_USER;
             try {
                 iLevel = Integer.parseInt(this.getProperty(Params.SECURITY_LEVEL)); // This is my current login level
             } catch (NumberFormatException ex) {
             }
             if (iLevel == Constants.LOGIN_USER)
             	iErrorCode = Constants.AUTHENTICATION_REQUIRED;	// For now, thin security is manual (No read access control)
             else if (iLevel == Constants.LOGIN_AUTHENTICATED)
             	iErrorCode = Constants.ACCESS_DENIED;	// If they only are allowed read access, don't let them access this screen (for now)
         }
         	
         if (iErrorCode == Constants.NORMAL_RETURN)
             return true;    // Success
         
         if (iErrorCode == Constants.ACCESS_DENIED)
         {
             String strMessage = this.getApplication().getSecurityErrorText(iErrorCode);
             JOptionPane.showConfirmDialog(this, strMessage, strMessage, JOptionPane.OK_CANCEL_OPTION);
             return false;
         }
         String strDisplay = null;
         if ((iErrorCode == Constants.LOGIN_REQUIRED) || (iErrorCode == Constants.AUTHENTICATION_REQUIRED))
         {
                 iErrorCode = onLogonDialog();
                 if (iErrorCode == JOptionPane.CANCEL_OPTION)
                     return false;    // User clicked the cancel button
                 if (iErrorCode == Constants.NORMAL_RETURN)
                     return true;    // Success
                 strDisplay = this.getLastError(iErrorCode);
         }
         else
             strDisplay = this.getLastError(iErrorCode);
 
         // Display the error message and return!
         JOptionPane.showConfirmDialog(this, strDisplay, "Error", JOptionPane.OK_CANCEL_OPTION);
         return false;   // Not successful
     }
     /**
      * Display the logon dialog and login.
      * @return
      */
     public int onLogonDialog()
     {
         String strDisplay = "Login required";
         strDisplay = this.getTask().getString(strDisplay);
         for (int i = 1; i < 3; i++)
         {
             String strUserName = this.getProperty(Params.USER_NAME);
             Frame frame = ScreenUtil.getFrame(this);
             LoginDialog dialog = new LoginDialog(frame, true, strDisplay, strUserName);
             if (frame != null)
                 ScreenUtil.centerDialogInFrame(dialog, frame);
             
             dialog.setVisible(true);
             int iOption = dialog.getReturnStatus();
             if (iOption == LoginDialog.NEW_USER_OPTION)
             	iOption = this.createNewUser(dialog);
             if (iOption != JOptionPane.OK_OPTION)
                 return iOption;   // Canceled dialog = You don't get in.
             strUserName = dialog.getUserName();
             String strPassword = dialog.getPassword();
             try {
                 byte[] bytes = strPassword.getBytes(Base64.DEFAULT_ENCODING);
                 bytes = Base64.encodeSHA(bytes);
                 char[] chars = Base64.encode(bytes);
                 strPassword = new String(chars);
             } catch (NoSuchAlgorithmException ex) {
                 ex.printStackTrace();
             } catch (UnsupportedEncodingException ex) {
                 ex.printStackTrace();
             }
             String strDomain = this.getProperty(Params.DOMAIN);
             int iSuccess = this.getApplication().login(this, strUserName, strPassword, strDomain);
             if (iSuccess == Constants.NORMAL_RETURN)
             {   // If they want to save the user... save it in a muffin.
                 if (this.getApplication().getMuffinManager() != null)
                     if (this.getApplication().getMuffinManager().isServiceAvailable())
                 {
                     if (dialog.getSaveState() == true)
                         this.getApplication().getMuffinManager().setMuffin(Params.USER_ID, this.getApplication().getProperty(Params.USER_ID));    // Set muffin to current user.
                     else
                         this.getApplication().getMuffinManager().setMuffin(Params.USER_ID, null);    // Set muffin to current user.
                 }
                 return iSuccess;
             }
             // Otherwise, continue looping (3 times)
         }
         return this.setLastError(strDisplay);
     }
     /**
      * Create a new user.
      * @param dialog
      * @return
      */
     public int createNewUser(LoginDialog dialog)
     {
         String strUserName = dialog.getUserName();
         String strPassword = dialog.getPassword();
         try {
             byte[] bytes = strPassword.getBytes(Base64.DEFAULT_ENCODING);
             bytes = Base64.encodeSHA(bytes);
             char[] chars = Base64.encode(bytes);
             strPassword = new String(chars);
         } catch (NoSuchAlgorithmException ex) {
             ex.printStackTrace();
         } catch (UnsupportedEncodingException ex) {
             ex.printStackTrace();
         }
         String strDomain = this.getProperty(Params.DOMAIN);
         int iSuccess = this.getApplication().createNewUser(this, strUserName, strPassword, strDomain);
         if (iSuccess == Constants.NORMAL_RETURN)
         	;
     	return JOptionPane.OK_OPTION;
     }
     /**
      * Display the change password dialog and change the password.
      * @return
      */
     public int onChangePassword()
     {
         String strDisplay = "Login required";
         strDisplay = this.getTask().getString(strDisplay);
         for (int i = 1; i < 3; i++)
         {
             String strUserName = this.getProperty(Params.USER_NAME);
             Frame frame = ScreenUtil.getFrame(this);
             ChangePasswordDialog dialog = new ChangePasswordDialog(frame, true, strDisplay, strUserName);
             ScreenUtil.centerDialogInFrame(dialog, frame);
             
             dialog.setVisible(true);
             int iOption = dialog.getReturnStatus();
             if (iOption != JOptionPane.OK_OPTION)
                 return iOption;   // Canceled dialog = You don't get in.
             strUserName = dialog.getUserName();
             String strPassword = dialog.getCurrentPassword();
             String strNewPassword = dialog.getNewPassword();
             try {
                 byte[] bytes = strPassword.getBytes(Base64.DEFAULT_ENCODING);
                 bytes = Base64.encodeSHA(bytes);
                 char[] chars = Base64.encode(bytes);
                 strPassword = new String(chars);
                 
                 bytes = strNewPassword.getBytes(Base64.DEFAULT_ENCODING);
                 bytes = Base64.encodeSHA(bytes);
                 chars = Base64.encode(bytes);
                 strNewPassword = new String(chars);
             } catch (NoSuchAlgorithmException ex) {
                 ex.printStackTrace();
             } catch (UnsupportedEncodingException ex) {
                 ex.printStackTrace();
             }
             
             int errorCode = this.getApplication().createNewUser(this, strUserName, strPassword, strNewPassword);
             if (errorCode == Constants.NORMAL_RETURN)
                 return errorCode;
         }
         return this.setLastError(strDisplay);
     }
     /**
      * 
      * @param parent
      * @param baseScreen
      * @param strCommandToPush
      * @return
      */
     public boolean changeSubScreen(Container parent, JBasePanel baseScreen, String strCommandToPush)
     {
         if ((parent == null) || (parent == this))
             parent = m_parent;
         boolean bScreenChange = false;
         if (!this.checkSecurity(baseScreen))
         {
             baseScreen.free();
             return false;
         }
         this.freeSubComponents(parent);
         if (parent.getComponentCount() > 0)
         { // Remove all the old components
             parent.removeAll();
             bScreenChange = true;
         }
         JComponent screen = baseScreen;
         if (!(parent.getLayout() instanceof BoxLayout))
             parent.setLayout(new BorderLayout());   // If default layout, use box layout!
 
         screen = baseScreen.setupNewScreen(baseScreen);
 
         screen.setMinimumSize(new Dimension(100, 100));
         screen.setAlignmentX(LEFT_ALIGNMENT);
         screen.setAlignmentY(TOP_ALIGNMENT);
         parent.add(screen);
         
         if (bScreenChange)
         {
             this.invalidate();
             this.validate();
             this.repaint();
             baseScreen.resetFocus();
         }
         if (parent == m_parent)
         {
             if (strCommandToPush == null)
                 strCommandToPush = baseScreen.getScreenCommand();
             if (strCommandToPush != null)
                 this.pushHistory(strCommandToPush, false);
         }
         
         return true;    // Success
     }
     /**
      * Call free for all the Freeable sub-components of the target container.
      * Note: This method is EXACTLY the same as the freeSubComponents method in JBasePanel.
      * @param container The parent component to look through for freeable components.
      */
     public void freeSubComponents(Container container)
     {
         for (int i = 0; i < container.getComponentCount(); i++)
         {
             Component component = container.getComponent(i);
             if (component instanceof Freeable)
                 ((Freeable)component).free();
             else if (component instanceof Container)
                 this.freeSubComponents((Container)component);
         }
     }
     /**
      * Initializes the applet.  You never need to call this directly; it is
      * called automatically by the system once the applet is created.
      */
     public void init()
     {
         super.init();
     }
     /**
      * Called to start the applet.  You never need to call this directly; it
      * is called when the applet's document is visited.
      */
     public void start()
     {
         super.start();
         this.init(m_args);
     }
     /**
      * Called to stop the applet.  The browser calls this when the applet's document is
      * no longer on the screen.  It is guaranteed to be called before destroy()
      * is called.  You never need to call this method directly
      */
     public void stop()
     {
         gbStandAlone = false;	// Just being careful (make sure this exit(0) is not called.
         this.free();
         super.stop();
     }
     /**
      * Called to stop the applet.  This is called when the applet's document is
      * no longer on the screen.  It is guaranteed to be called before destroy()
      * is called.  You never need to call this method directly
      */
     public void stopTask()
     {
         this.free();
     }
     /**
      * Is this task currently involved in computations?
      * @return True if the task is currently active.
      */
     public boolean isRunning()
     {
     	return false;
     }
     /**
      * Cleans up whatever resources are being held.  If the applet is active
      * it is stopped.
      */
     public void destroy()
     {
         super.destroy();
     }
     /**
      * Get this image.
      * @param filename The filename of this image (if no path, assumes images/buttons; if not ext assumes .gif).
      * @return The image.
      */
     public ImageIcon loadImageIcon(String filename)
     {
         return this.loadImageIcon(filename, null);
     }
     /**
      * Get this image.
      * @param filename The filename of this image (if no path, assumes images/buttons; if not ext assumes .gif).
      * @param description The image description.
      * @return The image.
      */
     public ImageIcon loadImageIcon(String filename, String description)
     {
         filename = this.getImageFilename(filename);
         URL url = null;
         if (this.getApplication() != null)
             url = this.getApplication().getResourceURL(filename, this);
         if (url == null)
         { // Not a resource, try reading it from the disk.
             try   {
                 return new ImageIcon(filename, description);
             } catch (Exception ex)  { // File not found - try a fully qualified URL.
                 ex.printStackTrace();
                 return null;
             }
         }
         return new ImageIcon(url, description);
     }
     /**
      * Get this image, then redisplay sField when you're done.
      */
     public String getImageFilename(String strFilename)
     {
         return this.getImageFilename(strFilename, "buttons");
     }
     /**
      * Get this image's full filename.
      * @param filename The filename of this image (if no path, assumes images/buttons; if not ext assumes .gif).
      * @param strSubDirectory The sub-directory.
      * @return The full (relative) filename for this image.
      */
     public String getImageFilename(String strFilename, String strSubDirectory)
     {
         if ((strFilename == null) || (strFilename.length() == 0))
             return null;    // A null will tell a JButton not to load an image
         if (strFilename.indexOf('.') == -1)
             strFilename += ".gif";
         strFilename = Util.getFullFilename(strFilename, strSubDirectory, Constants.IMAGE_LOCATION, true);
         return strFilename;
     }
     /**
      * Get the initial applet or the initial application instance.
      * @return The initial applet instance.
      */
     public static BaseApplet getSharedInstance()
     {
         return (BaseApplet)Application.getRootApplet();
     }
     /**
      * If this is an applet, return the instance.
      * If Standalone, return a null.
      * @return The applet if this was started as an applet.
      */
     public BaseAppletReference getApplet()
     {
         if (gbStandAlone)
             return null;        // Not an applet
         return (BaseApplet)Application.getRootApplet();
     }
     /**
      * (Optionally) set up the background for this screen.
      * Gets the background and color from the getBackgroundXXX methods.
      * @param panelTop The panel to add a background behind.
      * @return The container with this panel and the background.
      */
     public Container addBackground(Container panelTop)
     {
         Color colorBackground = this.getBackgroundColor();
         String strBackgroundImageName = this.getBackgroundImageName();
         if (strBackgroundImageName != null)
             m_imageBackground = this.loadImageIcon(strBackgroundImageName, "background");
         if (panelTop instanceof JApplet)
             panelTop = ((JApplet)panelTop).getContentPane();
         Container panelContainer = null;
         if ((this.getBackgroundImage() == null) && (colorBackground == null))
             panelContainer = panelTop;
         else
         {
                 // 2nd level an overlay panel
             panelTop.setLayout(new OverlayLayout(panelTop));
                 // 3rd level - a panel to hold all the display panels
             panelContainer = new JPanel();
           ((JPanel)panelContainer).setOpaque(false);
             panelTop.add(panelContainer);
 			panelContainer.setLayout(new BoxLayout(panelContainer, BoxLayout.Y_AXIS));
                 // 3rd level - Background for other components
             JTiledImage background = new JTiledImage(m_imageBackground, colorBackground); // Tiled Background
             panelTop.add(background);
         }
         // Add a status bar and a help pane (note: this also fixes the applet alignment problems
         JPanel panelMain = new JPanel();
         panelMain.setOpaque(false);
         panelContainer.setLayout(new BorderLayout());
         panelContainer.add(panelMain, BorderLayout.CENTER);
         if (this.getApplet() == null)
         	if (m_bAddStatusbar)
         		panelContainer.add(m_statusbar = new JStatusbar(null), BorderLayout.SOUTH);
         JHelpPane helpPane = new JHelpPane(null, null);
         panelContainer.add(helpPane, BorderLayout.EAST);
         this.setHelpView(helpPane.getHelpView().getHtmlEditor());
         helpPane.setVisible(false);
         if (this.getProperty("displayInitialHelp") != null)
         	this.getHelpView().getHelpPane().setVisible(true);	// Hack - this is a weird place for this, but it works
 
         panelContainer = panelMain;
         
         return panelContainer;
     }
     /**
      * Get the help pane.
      * @return the help pane.
      */
     public JHtmlEditor getHelpView()
     {
     	return m_helpEditor;
     }
     /**
      * Set the help pane.
      * @return the help pane.
      */
     public void setHelpView(JHtmlEditor helpEditor)
     {
     	if (m_helpEditor != null)
     		m_helpEditor.setCallingApplet(null);
     	m_helpEditor = helpEditor;
     	if (m_helpEditor != null)
     		m_helpEditor.setCallingApplet(this);
     }
     /**
      * Get the Status bar (for standalone apps).
      * @return The status bar.
      */
     public JStatusbar getStatusbar()
     {
         return m_statusbar;
     }
     /**
      * Get the background image.
      * @return The background image for this applet.
      */
     public ImageIcon getBackgroundImage()
     {
         return m_imageBackground;
     }
     /**
      * Get the background image's name.
      * From the background parameter name.
      * @return The background image name (or null if none).
      */
     public String getBackgroundImageName()
     {
         return this.getProperty(Params.BACKGROUND);     // Resource file
     }
     /**
      * Set the background image's color.
      * @param colorBackground The color to set the background.
      */
     public void setBackgroundColor(Color colorBackground)
     {
         m_colorBackground = colorBackground;
         if (m_parent != null)
         {
             JTiledImage panel = (JTiledImage)JBasePanel.getSubScreen(this, JTiledImage.class);
             if (panel != null)
                 panel.setBackground(colorBackground);
         }
     }
     /**
      * Get the background color.
      * From the backgroundcolor param.
      * (ie., backgroundcolor=red  or backgroundcolor=#FFFFCC).
      * @return The background color
      */
     public Color getBackgroundColor()
     {
         if (m_colorBackground == null)
         {
             String strBackgroundColor = this.getProperty(Params.BACKGROUNDCOLOR);   // Resource file
             if ((strBackgroundColor != null) && (strBackgroundColor.length() > 0))
                 m_colorBackground = BaseApplet.nameToColor(strBackgroundColor);
         }
         if (m_colorBackground == null)
             m_colorBackground = SystemColor.window;     // Default color
         return m_colorBackground;
     }
     /**
      * Convert this color string to the Color object.
      * @param strColor Name of the color (ie., gray or 0xFFFFCF or #FFFFCF).
      * @return The java color object (or null).
      */
     public static Color nameToColor(String strColor)
     {
         final Object[][] obj = {
             {"black", Color.black},
             {"blue", Color.blue},
             {"cyan", Color.cyan},
             {"darkGray", Color.darkGray},
             {"gray", Color.gray},
             {"green", Color.green},
             {"lightGray", Color.lightGray},
             {"magenta", Color.magenta},
             {"orange", Color.orange},
             {"pink", Color.pink},
             {"red", Color.red},
             {"white", Color.white},
             {"yellow", Color.yellow}
         };      // Not static, because this method is usually not called.
         for (int i = 0; i < obj.length; i++)
         {
             if (strColor.equalsIgnoreCase((String)obj[i][0]))
                 return (Color)obj[i][1];
         }
         try {
             return Color.decode(strColor);
         } catch (NumberFormatException ex)  {
         }
         return null;
     }
     /**
      * Do some applet-wide action.
      * For example, submit or reset. Pass this action down to all the JBaseScreens.
      * Remember to override this method to send the actual data!
      * @param iOptions Extra command options
      */
     public boolean doAction(String strAction, int iOptions)
     {
         if (Constants.BACK.equalsIgnoreCase(strAction))
         {
             String strPrevAction = this.popHistory(1, false);   // Current screen
             strAction = this.popHistory(1, false);   // Last screen
             if (strAction != null)		// I don't back up into the browser history if the user hit the java back button.
             {	// If that wasn't the first screen, redo pop and update the browser this time
             	this.pushHistory(strAction, false);
             	strAction = this.popHistory(1, true);
             }
             if (strAction != null)
             {
             	iOptions = iOptions | Constants.DONT_PUSH_TO_BROSWER;	// Don't push the back command
                 if (!Constants.BACK.equalsIgnoreCase(strAction)) // Never (prevent endless recursion)
                     return this.doAction(strAction, iOptions);
             }
             else if (strPrevAction != null)
                 this.pushHistory(strPrevAction, false);    // If top of stack, leave it alone.
         }
         if (Constants.HELP.equalsIgnoreCase(strAction))
         {
             String strPrevAction = this.popHistory();   // Current screen
             this.pushHistory(strPrevAction, ((iOptions & Constants.DONT_PUSH_TO_BROSWER) == Constants.PUSH_TO_BROSWER));    // If top of stack, leave it alone.
             if ((strPrevAction != null)
                 && (strPrevAction.length() > 0)
                 && (strPrevAction.charAt(0) != '?'))
                     strPrevAction = "?" + strPrevAction;
             String strURL = Util.addURLParam(strPrevAction, Params.HELP, Constants.BLANK);
             iOptions = this.getHelpPageOptions(iOptions);
             return this.getApplication().showTheDocument(strURL, this, iOptions);
         }
         if (strAction != null)
             if (strAction.indexOf('=') != -1)
         {
             Map<String,Object> properties = new Hashtable<String,Object>();
             Util.parseArgs(properties, strAction);
             String strApplet = (String)properties.get(Params.APPLET);
             if (strApplet != null)
                 if ((strApplet.length() == 0)
                     || (strAction.indexOf("BaseApplet") != -1))
             {
                 this.setProperties(properties);
                 if (!this.addSubPanels(null))
                 	return false;	// If error, return false
                 this.popHistory(1, false);      // Pop the default command, the action command is better.
                 this.pushHistory(strAction, ((iOptions & Constants.DONT_PUSH_TO_BROSWER) == Constants.PUSH_TO_BROSWER));    // This is the command to get to this screen (for the history).
                 return true;	// Command handled
             }
         }
         return false;
     }
     /**
      * Get the display preference for the help window.
      * @return
      */
     public int getHelpPageOptions(int iOptions)
     {
     	String strPreference = this.getProperty(ThinMenuConstants.USER_HELP_DISPLAY);
     	if ((strPreference == null) || (strPreference.length() == 0))
     		strPreference = this.getProperty(ThinMenuConstants.HELP_DISPLAY);
     	if (this.getHelpView() == null)
     		if (ThinMenuConstants.HELP_PANE.equalsIgnoreCase(strPreference))
     			strPreference = null;
     	if ((strPreference == null) || (strPreference.length() == 0))
 		{
     		//if (this.getAppletScreen() != null)
     		//	strPreference = MenuConstants.HELP_WEB;
     		//else if ((applet.getHelpView() != null) && (applet.getHelpView().getBaseApplet() == applet))
     			strPreference = ThinMenuConstants.HELP_PANE;
     		//else
     		//	strPreference = MenuConstants.HELP_WINDOW;
 		}
 		if (ThinMenuConstants.HELP_WEB.equalsIgnoreCase(strPreference))
 			iOptions = ThinMenuConstants.HELP_WEB_OPTION;
 		else if (ThinMenuConstants.HELP_PANE.equalsIgnoreCase(strPreference))
 			iOptions = ThinMenuConstants.HELP_PANE_OPTION;
 		else if (ThinMenuConstants.HELP_WINDOW.equalsIgnoreCase(strPreference))
 			iOptions = ThinMenuConstants.HELP_WINDOW_OPTION;
 		else
 			iOptions = ThinMenuConstants.HELP_PANE_OPTION;	// Default
     	return iOptions;
     }
     /**
      * Do some applet-wide action.
      * Here are how actions are handled:
      * When a BasePanel receives a command it calls it's doAction method. If the doAction
      * doesn't handle the action, it is passed to the parent's doAction method, until it
      * hits the (this) applet method. This applet method tries to pass the command to all
      * sub-BasPanels until one does the action.
      * For example, submit or reset. Pass this action down to all the JBaseScreens.
      * Do not override this method, override the handleAction method in the JBasePanel.
      * @param strAction The command to pass to all the sub-JBasePanels.
      * @param target The parent to start the sub-search from (non-inclusive).
      * @param source The command source
      * @param iOptions Extra command options
      * @return true If handled.
      */
     public static boolean handleAction(String strAction, Container target, Component source, int iOptions)
     {
         // First see if I can handle this action
         if (target instanceof JBasePanel)
         {   // This is a panel, handle the action.
             if (((JBasePanel)target).doAction(strAction, iOptions))
                 return true;
         }
         else if (target instanceof BaseApplet)
         {   // This is a applet, handle the action.
             if (((BaseApplet)target).doAction(strAction, iOptions))
                 return true;
         }
         // Next, see if my children can handle this action
         for (int iIndex = 0; iIndex < target.getComponentCount(); iIndex++)
         {
             Component component = target.getComponent(iIndex);
             if (component != source)
             {
                 if (component instanceof JBasePanel)
                 {   // This is a panel, handle the action.
                         if (((JBasePanel)component).handleAction(strAction, target, iOptions))  // and make sure this method is not called again for this object
                             return true;
                 }
                 else if (component instanceof Container)
                 {   // Call this for all the sub-panels for this parent
                     if (BaseApplet.handleAction(strAction, (Container)component, target, iOptions))   // Continue down thru the tree
                         return true;
                 }
             }
         }
         // Last, see if my parents can handle this action
         Container parent = target.getParent();
         if (parent != source)
         {
             if (parent instanceof JBasePanel)
             {
                 return ((JBasePanel)parent).handleAction(strAction, target, iOptions); // Continue up the chain.
             }
             else if (parent != null)
             {
                 return BaseApplet.handleAction(strAction, parent, target, iOptions);  // and make sure this method is not called again for this object
             }
         }
         return false;
     }
     /**
      * Get the application session that goes with this task (applet).
      * @return The application parent.
      */
     @Override
     public Application getApplication()
     {
         if (m_application != null)
             return m_application;
         if (this.getApplet() != null)
             if (this.getApplet() != this)
                 return ((BaseApplet)this.getApplet()).getApplication();
         return null;
     }
     /**
      * This is task's parent application.
      * @param application This task's application.
      */
     public void setApplication(App application)
     {
         m_application = (Application)application;
     }
     /**
      * Lookup and return the object server.
      * This is just for convience, the server is in the Application.
      * @return The server object.
      */
     public Object getRemoteTask()
     {
         return this.getApplication().getRemoteTask(this);
     }
     /**
      * Link this record to the remote Ejb Table.
      * Used ONLY for thin tables.
      * @param database (optional) remote database that this table is in.
      * @param record The fieldlist to create a remote peer for.
      * @return The new fieldtable for this fieldlist.
      */
     public FieldTable linkNewRemoteTable(FieldList record)
     {
         return this.linkNewRemoteTable(record, false);
     }
     /**
      * Link this record to the remote Ejb Table.
      * This method, gets the remote database and makes a remote table, the creates a
      * RemoteFieldTable for this record and (optionally) wraps a CacheRemoteFieldTable.
      * Used ONLY for thin tables.
      * @param database (optional) remote database that this table is in.
      * @param record The fieldlist to create a remote peer for.
      * @param bUseCache Add a CacheRemoteFieldTable to this table?
      * @return The new fieldtable for this fieldlist.
      */
     public FieldTable linkNewRemoteTable(FieldList record, boolean bUseCache)
     {
         FieldTable table = record.getTable();
         if (table == null)
         {
             RemoteTable remoteTable = null;
             try   {
                 synchronized (this.getRemoteTask())
                 {   // In case this is called from another task
                     RemoteTask server = (RemoteTask)this.getRemoteTask();
                     Map<String, Object> dbProperties = this.getApplication().getProperties();
                     remoteTable = server.makeRemoteTable(record.getRemoteClassName(), null, null, dbProperties);
                 }
             } catch (RemoteException ex)    {
                 ex.printStackTrace();
             } catch (Exception ex)  {
                 ex.printStackTrace();
             }
             if (bUseCache)
                 remoteTable = new org.jbundle.thin.base.db.client.CachedRemoteTable(remoteTable);
             table = new org.jbundle.thin.base.db.client.RemoteFieldTable(record, remoteTable, this.getRemoteTask());
         }
         return table;
     }
     /**
      * Link this record to the remote Ejb Table in this remote session.
      * Note: If you don't pass a record, this method will create one for you of the main
      * record of this session.
      * @param session Optional parent or database session.
      * @param strTableSessionClass Optional class for the table session. Null defaults to TableSessionObject.
      * @param record Thin record or null if you want the main record is this session.
      * @param bUseCache Use cache table
      * @return The new fieldtable for this fieldlist.
      */
     public FieldTable linkRemoteSessionTable(RemoteSession session, FieldList record, boolean bUseCache)
     {
         FieldTable table = null;
         if (record != null)
         {
             if (Constants.TABLE == (record.getDatabaseType() & Constants.TABLE_TYPE_MASK))
             {   // Special case - table - NOTE: I am VERY careful not to directly reference the table classes... I don't want them loaded unless they are necessary!
                 Application app = this.getApplication();
                 ThinPhysicalDatabaseParent dbParent = ((ThinApplication)app).getPDatabaseParent(mapDBParentProperties, true);
                 ThinPhysicalDatabase pDatabase = dbParent.getPDatabase(record.getDatabaseName(), ThinPhysicalDatabase.NET_TYPE, true);    // Net database
                 ThinPhysicalTable pTable = pDatabase.getPTable(record, true, true);
                 pTable.addPTableOwner((PhysicalDatabaseParent)dbParent);   // NO NO NO - Don't coerce 
             }
             table = record.getTable();
         }
         if (table == null)
         {
             RemoteTable remoteTable = null;
             try   {
                 synchronized (this.getRemoteTask())
                 {   // In case this is called from another task
                     if ((record == null)
                         && (session instanceof RemoteTable))
                     {   // Special case - no record supplied for a RemoteTable, build the record.
                         remoteTable = (RemoteTable)session;
                         record = remoteTable.makeFieldList(null);
                     }
                     else
                     {
                         remoteTable = session.getRemoteTable(record.getTableNames(false));
                     }
                 }
             } catch (RemoteException ex)    {
                 ex.printStackTrace();
             } catch (Exception ex)  {
                 ex.printStackTrace();
             }
             if (bUseCache)
                 remoteTable = new org.jbundle.thin.base.db.client.CachedRemoteTable(remoteTable);
             table = new org.jbundle.thin.base.db.client.RemoteFieldTable(record, remoteTable, this.getRemoteTask());
         }
         return table;
     }
     public static Map<String,Object> mapDBParentProperties = new Hashtable<String,Object>();
     static
     {
         mapDBParentProperties.put(PhysicalDatabaseParent.TIME, "-1");   // Default time
         mapDBParentProperties.put(PhysicalDatabaseParent.DBCLASS, Constants.ROOT_PACKAGE + "thin.base.db.mem.net.NDatabase");   // Hybrid (net) database.        
     }
     /**
      * Create this session with this class name at the remote server.
      * @param parentSessionObject The (optional) parent session.
      * @param strSessionClass The class name of the remote session to create.
      * @return The new remote session (or null if not found).
      */
     public RemoteSession makeRemoteSession(RemoteSession parentSessionObject, String strSessionClass)
     {
         RemoteTask server = (RemoteTask)this.getRemoteTask();
         try   {
             synchronized (server)
             {   // In case this is called from another task
                 if (parentSessionObject == null)
                     return (RemoteSession)server.makeRemoteSession(strSessionClass);
                 else
                     return (RemoteSession)parentSessionObject.makeRemoteSession(strSessionClass);
             }
         } catch (RemoteException ex)    {
             ex.printStackTrace();
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
         return null;
     }
     /**
      * This is a special method that runs some code when this screen is opened as a task.
      */
     public void run()
     {   // Add this code to run the job in the overriding class
         if (this.getParent() == null)
             new JBaseFrame("Applet Window", this);  // Must have a window first
     }
     /**
      * If this task object was created from a class name, call init(xxx) for the task.
      * You may want to put logic in here that checks to make sure this object was not already inited.
      * Typically, you init a Task object and pass it to the job scheduler. The job scheduler
      * will check to see if this task is owned by an application... if not, initTask() is called.
      */
     public void initTask(App application, Map<String, Object> properties)
     {
         if (m_application != null)
             return;     // No, already inited!
         m_application = (Application)application;
         if (properties != null)
         {
             if (m_properties != null)
                 m_properties.putAll(properties);
             else
                 m_properties = properties;
         }
         this.init(null);
     }
     /**
      * Set the (optional) browser manager.
      * @param browserManager The browser manager.
      */
     public void setBrowserManager(BrowserManager browserManager)
     {
         m_browserManager = browserManager;
     }
     /**
      * Get the muffin manager.
      * @return The muffin manager.
      */
     public BrowserManager getBrowserManager()
     {
         return m_browserManager;
     }
     /**
      * Push this command onto the history stack.
      * @param strHistory The history command to push onto the stack.
      */
     public void pushHistory(String strHistory)
     {
         this.pushHistory(strHistory, true);
     }
     /**
      * Push this command onto the history stack.
      * @param strHistory The history command to push onto the stack.
      */
     public void pushHistory(String strHistory, boolean bPushToBrowser)
     {
         if (m_vHistory == null)
             m_vHistory = new Vector<String>();
         m_vHistory.addElement(strHistory);
         String strHelpURL = Util.fixDisplayURL(strHistory, true, true, true, this);
     	this.getApplication().showTheDocument(strHelpURL, this, ThinMenuConstants.HELP_WINDOW_CHANGE);
         if (bPushToBrowser)
 			if (this.getBrowserManager() != null)
 				this.getBrowserManager().pushBrowserHistory(strHistory, this.getStatusText(Constants.INFORMATION));    	// Let browser know about the new screen
     }
     /**
      * Pop a command off the history stack.
      * @return The command at the top of the history stack or null if empty.
      */
     public String popHistory()
     {
         return this.popHistory(1, true);
     }
     /**
      * Pop this command off the history stack.
      * NOTE: Do not use this method in most cases, use the method in BaseApplet.
      * @return The history command on top of the stack.
      */
     public String popHistory(int quanityToPop, boolean bPushToBrowser)
     {
         String strHistory = null;
         for (int i = 0; i < quanityToPop; i++)
         {
         	strHistory = null;
 	        if (m_vHistory != null) if (m_vHistory.size() > 0)
 	            strHistory = (String)m_vHistory.remove(m_vHistory.size() - 1);
         }
         if (bPushToBrowser)
 			if (this.getBrowserManager() != null)
 				this.getBrowserManager().popBrowserHistory(quanityToPop, strHistory != null, this.getStatusText(Constants.INFORMATION));    	// Let browser know about the new screen
         return strHistory;
     }
     /**
      * The browser back button was pressed (Javascript called me).
      * @param command The command that was popped from the browser history.
      */
 	public void doJavaBrowserBack(String command)
 	{
 		if (command != null)
 			if (command.startsWith("#"))
 				command = command.substring(1);
 		String javaCommand = this.popHistory(2, false);		// Current command
 		Util.getLogger().info("Browser back: java command=" + javaCommand + "  browser command=" + command);
 		if (javaCommand == null)
 			javaCommand = command;
 		if ((javaCommand == null) || (javaCommand.length() == 0))
 			javaCommand = this.getInitialCommand(false);
 		Util.getLogger().info("=target command=" + this.cleanCommand(javaCommand));
 		BaseApplet.handleAction(this.cleanCommand(command), this, this, Constants.DONT_PUSH_TO_BROSWER);
 		//x		this.doAction(this.cleanCommand(javaCommand), Constants.DONT_PUSH_TO_BROSWER);
 	}
 	/**
 	 * Get the original screen params.
 	 * @param bIncludeAppletCommands Include the initial applet commands?
 	 * @return
 	 */
 	public String getInitialCommand(boolean bIncludeAppletCommands)
 	{
 		String strCommand = Constants.BLANK;
 		if (bIncludeAppletCommands)
 		{
 	        if (this.getProperty(Params.APPLET) != null)
 	        	strCommand = Util.addURLParam(strCommand, Params.APPLET, this.getProperty(Params.APPLET));
 	        else if (this.getProperty("code") != null)
 	        	strCommand = Util.addURLParam(strCommand, Params.APPLET, this.getProperty("code"));
 	        else
 	        	strCommand = Util.addURLParam(strCommand, Params.APPLET, this.getClass().getName());
 	        if (this.getProperty("jnlpjars") != null)
 	        	strCommand = Util.addURLParam(strCommand, "jnlpjars", this.getProperty("jnlpjars"));
 	        if (this.getProperty("jnlpextensions") != null)
 	        	strCommand = Util.addURLParam(strCommand, "jnlpextensions", this.getProperty("jnlpextensions"));
 	        if (this.getProperty(ScreenUtil.BACKGROUND_COLOR) != null)
 	        	strCommand = Util.addURLParam(strCommand, ScreenUtil.BACKGROUND_COLOR, this.getProperty(ScreenUtil.BACKGROUND_COLOR));
 	        if (this.getProperty(Params.BACKGROUND) != null)
 	        	strCommand = Util.addURLParam(strCommand, Params.BACKGROUND, this.getProperty(Params.BACKGROUND));
 	        if (this.getProperty("webStart") != null)
 	        	strCommand = Util.addURLParam(strCommand, "webStart", this.getProperty("webStart"));
 	        if (this.getProperty(Params.USER_ID) != null)
 	        	strCommand = Util.addURLParam(strCommand, Params.USER_ID, this.getProperty(Params.USER_ID));
 	        if (this.getProperty(Params.USER_NAME) != null)
 	        	strCommand = Util.addURLParam(strCommand, Params.USER_NAME, this.getProperty(Params.USER_NAME));
 		}
         if (this.getProperty(Params.SCREEN) != null)
         	strCommand = Util.addURLParam(strCommand, Params.SCREEN, this.getProperty(Params.SCREEN));
         if (strCommand.length() == 0)
         {
             String strMenu = this.getProperty(Params.MENU);
             if (strMenu == null)
             	strMenu = Constants.BLANK;
         	strCommand = Util.addURLParam(strCommand, Params.MENU, strMenu);
         }
 		return strCommand;
 	}
 	/**
 	 * Clean the javascript command for java use.
 	 */
 	public String cleanCommand(String command)
 	{
 		if (command == null)
 			return command;
 		Map<String,Object> properties = new HashMap<String,Object>();
 		Util.parseArgs(properties, command);
 		properties.remove(Params.APPLET);
 		properties.remove("code");
 		properties.remove("jnlpjars");
 		properties.remove("jnlpextensions");
 		properties.remove(ScreenUtil.BACKGROUND_COLOR);
 		properties.remove(Params.BACKGROUND);
 		command = Constants.BLANK;
 		for (String key : properties.keySet())
 		{
 			if (properties.get(key) != null)
 				command = Util.addURLParam(command, key, properties.get(key).toString());
 		}
 		return command;
 	}
     /**
      * The browser back button was pressed (Javascript called me).
      * @param command The command that was popped from the browser history.
      */
 	public void doJavaBrowserForward(String command)
 	{
 		if (command != null)
 			if (command.startsWith("#"))
 				command = command.substring(1);
 		Util.getLogger().info("Browser forward: java browser command=" + command + " = target: " + this.cleanCommand(command));
 		BaseApplet.handleAction(this.cleanCommand(command), this, this, Constants.DONT_PUSH_TO_BROSWER);
 		//x		this.doAction(this.cleanCommand(command), Constants.DONT_PUSH_TO_BROSWER);
 	}
     /**
      * The browser hash value changed (Javascript called me).
      * @param command The command that was popped from the browser history.
      */
 	public void doJavaBrowserHashChange(String command)
 	{
 		if (command != null)
 			if (command.startsWith("#"))
 				command = command.substring(1);
 		Util.getLogger().info("Browser hash change: java browser command=" + command + " = target: " + this.cleanCommand(command));
 		BaseApplet.handleAction(this.cleanCommand(command), this, this, Constants.DONT_PUSH_TO_BROSWER);
 //x		this.doAction(this.cleanCommand(command), Constants.DONT_PUSH_TO_BROSWER);
 	}
     /**
      * Get the properties.
      * @return The properties object.
      */
     public Map<String, Object> getProperties()
     {
         return m_properties;
     }
     /**
      * Set the properties.
      * @param strProperties The properties to set.
      */
     public void setProperties(Map<String, Object> properties)
     {
         m_properties = properties;
     }
     /**
      * Get this property.
      * @param strProperty The property key to find.
      * @return The property (of null if not found).
      */
     public String getProperty(String strProperty)
     {
         String strValue = null;
         if (m_properties != null)
         {   // Try local properties
             if (m_properties.get(strProperty) != null)
                 strValue = m_properties.get(strProperty).toString();
             if ((strValue != null) && (strValue.length() > 0))
                 return strValue;
         }
         if (this.getApplication() != null)
             strValue = this.getApplication().getProperty(strProperty);  // Try app
         return strValue;
     }
     /**
      * Set this property.
      * @param strProperty The property key.
      * @param strValue The property value.
      */
     public void setProperty(String strProperty, String strValue)
     {
         if (m_properties == null)
             m_properties = new Hashtable<String,Object>();
         if (strValue != null)
             m_properties.put(strProperty, strValue);
         else
             m_properties.remove(strProperty);
     }
     /**
      * Retrieve/Create a user properties record with this lookup key.
      * @param strPropertyCode The key I'm looking up.
      * @return The UserProperties for this registration key.
      */
     public PropertyOwner retrieveUserProperties(String strRegistrationKey)
     {
         if (this.getApplication() != null)
             return this.getApplication().retrieveUserProperties(strRegistrationKey);        // Override this for functionality
         return null;
     }
     /**
      * Display this status message in the status box or at the bottom of the browser.
      * @param strStatus The message to display in the status box.
      */
     public void setStatusText(String strStatus)
     {
         this.setStatusText(strStatus, Constants.INFORMATION);
     }
     /**
      * Display this status message in the status box or at the bottom of the browser.
      * @param strStatus The message to display in the status box.
      * @param iWarningLevel The warning level to display (see JOptionPane).
      * INFORMATION_MESSAGE/WARNING_MESSAGE.
      */
     public void setStatusText(String strStatus, int iWarningLevel)
     {
         if (strStatus == null)
             strStatus = Constants.BLANK;
         m_strCurrentStatus = strStatus;
         m_iCurrentWarningLevel = iWarningLevel;
         if (this.getApplet() != null)
             ((BaseApplet)this.getApplet()).showStatus(strStatus);
         else
         {
             if (iWarningLevel != Constants.ERROR)
             {   // Warning or information
                 ImageIcon icon = null;
                 if (iWarningLevel == Constants.INFORMATION)
                     icon = this.loadImageIcon(ThinMenuConstants.INFORMATION);
                 else if (iWarningLevel == Constants.WARNING)
                     icon = this.loadImageIcon(ThinMenuConstants.WARNING);
                 else if (iWarningLevel == Constants.WAIT)
                     icon = this.loadImageIcon(ThinMenuConstants.WAIT);
                 else if (iWarningLevel == Constants.ERROR)
                     icon = this.loadImageIcon(ThinMenuConstants.ERROR);
                 if (this.getStatusbar() != null)
                     this.getStatusbar().showStatus(strStatus, icon, iWarningLevel);
             }
             else
             {   // Full blown error message (show dialog)
                 Container frame = BaseApplet.getSharedInstance();
                 while ((frame = frame.getParent()) != null)
                 {
                     if (frame instanceof JFrame)
                     {
                         JOptionPane.showMessageDialog((JFrame)frame, strStatus, "Error: ", JOptionPane.WARNING_MESSAGE);
                         break;
                     }
                 }
             }
         }
     }
     /**
      * Get the last status message if it is at this level or above.
      * Typically you do this to see if the current message you want to display can
      * be displayed on top of the message that is there already.
      * Calling this method will clear the last status text.
      * @param iWarningLevel The maximum warning level to retrieve.
      * @return The current message if at this level or above, otherwise return null.
      */
     public String getStatusText(int iWarningLevel)
     {
         String strStatus = m_strCurrentStatus;
         if (m_iCurrentWarningLevel < iWarningLevel)
             strStatus = null;
         return strStatus;
     }
     /**
      * Display the status text.
      * @param strMessage The message to display.
      */
     public Cursor setStatus(int iStatus, Component comp, Cursor cursor)
     {
         Cursor oldCursor = null;
         if (comp != null)
         	if (SwingUtilities.isEventDispatchThread())	// Just being careful
         {
             oldCursor = comp.getCursor();
             if (cursor == null)
                 cursor = Cursor.getPredefinedCursor(iStatus);
             comp.setCursor(cursor);
         }
         if (m_statusbar != null)
             m_statusbar.setStatus(iStatus);
         return oldCursor;
     }
     /*
      * Get the last error code.
      * This call clears the last error code.
      * @param iErrorCode Pass the error code of the last error code or 0 to get the last error.
      * @return The last error string if it matches this code (else null).
      */
     public String getLastError(int iErrorCode)
     {
         if ((m_strLastError == null) || ((iErrorCode != 0) && (iErrorCode != m_iLastErrorCode)))
             return Constants.BLANK;
         String string = m_strLastError;
         m_strLastError = null;
         return string;
     }
     /**
      * Set the last (next) error code to display.
      * @param strLastError The error to set.
      * @return The made-up error code that matches this message.
      */
     public int setLastError(String strLastError)
     {
         m_strLastError = strLastError;
         if (m_iLastErrorCode > -2)
             m_iLastErrorCode = -2;
         return --m_iLastErrorCode;
     }
     /**
      * Get the task for this record owner parent.
      * If this is a RecordOwner, return the parent task. If this is a Task, return this.
      * For an applet, return this.
      * @return Record owner's environment, or null to use the default enviroment.
      */
     public Task getTask()
     {
         return this;
     }
     /**
      * Convert this key to a localized string.
      * In thin, this just calls the getString method in application,
      * in thick, a local resource can be saved.
      * @param strKey The key to lookup in the resource file.
      * @return The localized key.
      */
     public String getString(String strKey)
     {
         if (this.getApplication() != null)
             return this.getApplication().getString(strKey);
         return strKey;
     }
     /**
      *  Throw up a dialog box to show "about" info.
      * @return true.
      */
     public boolean onAbout()
     {
         Application application = this.getApplication();
         application.getResources(null, true);   // Set the resource bundle to default
         String strTitle = this.getString(ThinMenuConstants.ABOUT);
         String strMessage = this.getString("Copyright");
         JOptionPane.showMessageDialog(ScreenUtil.getFrame(this), strMessage, strTitle, JOptionPane.INFORMATION_MESSAGE);
         return true;
     }
     /**
      * Throw up a dialog box to change the font.
      * Note: This is only used in the thin implementation, thick uses this method name in VAppletScreen.
      * @return True if successful.
      */
     public boolean onSetFont()
     {
         Map<String,Object> properties = null;
         RemoteTask task = (RemoteTask)this.getApplication().getRemoteTask(null, null, false);
         try {
             if (task != null)
                 properties = (Map)task.doRemoteAction(Params.RETRIEVE_USER_PROPERTIES, properties);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         Frame frame = ScreenUtil.getFrame(this);
         ScreenDialog dialog = new ScreenDialog((Frame)frame, properties);
         ScreenUtil.centerDialogInFrame(dialog, (Frame)frame);
         
         dialog.setVisible(true);
         if (dialog.getReturnStatus() == JOptionPane.OK_OPTION)
         {
             properties = dialog.getProperties();
 
             // Now change any other screens to match.
             Application app = this.getApplication();
             for (Task taskScreen : app.getTaskList().keySet())
             {
                 if (taskScreen instanceof BaseApplet)
                     ((BaseApplet)taskScreen).setScreenProperties(null, properties);                
             }
 
             try {
                 if (task != null)
                     task.doRemoteAction(Params.SAVE_USER_PROPERTIES, properties);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         return true;    // Command handled
     }
     /**
      * Change the screen properties to these properties.
      * @param propertyOwner The properties to change to.
      */
     public void setScreenProperties(PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         Frame frame = ScreenUtil.getFrame(this);
         ScreenUtil.updateLookAndFeel((Frame)frame, propertyOwner, properties);
         Color colorBackgroundNew = ScreenUtil.getColor(ScreenUtil.BACKGROUND_COLOR, propertyOwner, properties);
         if (colorBackgroundNew != null)
             this.setBackgroundColor(colorBackgroundNew);
     }
     /**
      * Get the screen properties and set up the look and feel.
      * @param propertyOwner The screen properties (if null, I will look them up).
      */
     public void setupLookAndFeel(PropertyOwner propertyOwner)
     {
         Map<String,Object> properties = null;
         if (propertyOwner == null)
             propertyOwner = this.retrieveUserProperties(Params.SCREEN);
         if (propertyOwner == null)
         {   // Thin only
             RemoteTask task = (RemoteTask)this.getApplication().getRemoteTask(null, null, false);
             if (task != null)
             {
                 try {
                     properties = (Map)task.doRemoteAction(Params.RETRIEVE_USER_PROPERTIES, properties);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
         String backgroundName = null;
         if (propertyOwner != null)
             if (propertyOwner.getProperty(Params.BACKGROUNDCOLOR) != null)
                 backgroundName = propertyOwner.getProperty(Params.BACKGROUNDCOLOR);
         if (backgroundName == null)
             if (properties != null)
                 backgroundName = (String)properties.get(Params.BACKGROUNDCOLOR);
         if (backgroundName != null)
             this.setBackgroundColor(BaseApplet.nameToColor(backgroundName));
         Container top = this;
         while (top.getParent() != null)
         {
             top = top.getParent();
         }
         ScreenUtil.updateLookAndFeel(top, propertyOwner, properties);
     }
     /**
      * A utility method to get an Input stream from a filename or URL string.
      * @param strFilename The filename or url to open as an Input Stream.
      * @return The imput stream (or null if there was an error).
      */
     public InputStream getInputStream(String strFilename)
     {
         InputStream streamIn = null;
         if ((strFilename != null) && (strFilename.length() > 0))
         {
             try   {
                 URL url = null;
                 if (strFilename.indexOf(':') == -1)
                     if (this.getApplication() != null)
                         url = this.getApplication().getResourceURL(strFilename, this);
                 if (url != null)
                     streamIn = url.openStream();
             } catch (Exception ex)  {
                 streamIn = null;
             }
         }
         if (streamIn == null)
             streamIn = Util.getInputStream(strFilename, this.getApplication());
         return streamIn;
     }
     /**
      * Can this task be the main task?
      * @return true If it can.
      */
     public boolean isMainTaskCandidate()
     {
         return true;    // All applet screens are candidates for the main task.
     }
     /**
      * Get the default lock strategy to use for this type of table.
      * @return The lock strategy.
      */
     public int getDefaultLockType(int iDatabaseType)
     {
         if ((iDatabaseType & Constants.REMOTE) != 0)
             return Constants.OPEN_LAST_MOD_LOCK_TYPE;
         if ((iDatabaseType & Constants.LOCAL) != 0)
             return Constants.OPEN_NO_LOCK_TYPE;
         return Constants.OPEN_NO_LOCK_TYPE;
     }
     /**
      * This attribute indicates whether the method
      * paint(Graphics) has been called at least once since the
      * construction of this window.<br>
      * This attribute is used to notify method splash(Image)
      * that the window has been drawn at least once
      * by the AWT event dispatcher thread.<br>
      * This attribute acts like a latch. Once set to true,
      * it will never be changed back to false again.
      *
      * @see #paint
      * @see #splash
      */
     private boolean paintCalled = true;
     /**
      * Paints the image on the window.
      */
     public void paint(Graphics g) {
         super.paint(g);
         
         // Notify the page loader that the window
         // has been painted.
         // Note: To improve performance we do not enter
         // the synchronized block unless we have to.
         if (! paintCalled) {
             paintCalled = true;
             synchronized (this) { notifyAll(); }
         }
     }
     public boolean isPaintCalled()
     {
         return paintCalled;
     }
     
     public void setPaintCalled(boolean bPaintCalled)
     {
         paintCalled = bPaintCalled;
     }
     /**
      * Add this record owner to my list.
      * @param recordOwner The recordowner to add
      */
     public boolean addRecordOwner(RecordOwnerParent recordOwner)
     {
     	return m_recordOwnerCollection.addRecordOwner(recordOwner);
     }
     /**
      * Remove this record owner to my list.
      * @param recordOwner The recordowner to remove.
      */
     public boolean removeRecordOwner(RecordOwnerParent recordOwner)
     {
     	return m_recordOwnerCollection.removeRecordOwner(recordOwner);
     }
 }
