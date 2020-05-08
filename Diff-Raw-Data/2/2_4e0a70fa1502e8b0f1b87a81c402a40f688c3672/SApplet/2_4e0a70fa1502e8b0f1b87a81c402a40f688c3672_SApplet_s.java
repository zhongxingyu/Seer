 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.screen.control.swing;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.KeyboardFocusManager;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.swing.BoxLayout;
 
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBParams;
 import org.jbundle.base.model.ScreenConstants;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.screen.control.swing.util.ScreenInfo;
 import org.jbundle.base.screen.model.AppletScreen;
 import org.jbundle.base.screen.model.BasePanel;
 import org.jbundle.base.screen.model.BaseScreen;
 import org.jbundle.base.screen.model.FrameScreen;
 import org.jbundle.base.screen.view.swing.VAppletScreen;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.Environment;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.model.App;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.util.Util;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.screen.BaseApplet;
 import org.jbundle.thin.base.screen.comp.JTiledImage;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.ThinMenuConstants;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * SApplet Class.
  */
 public class SApplet extends BaseApplet
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
      * The ScreenField for this control.
      */
     protected AppletScreen m_screenField = null;
     /**
      * The pane to add components to.
      */
     protected Container m_paneBottom = null;
     /**
      * Background: Location of background image.
      */
     public static final String DEFAULT_HELP_URL = "docs/help/user/basic/index.xml"; 
 
     /**
      * SApplet Class Constructor
      */
     public SApplet()
     {
         super();
     }
     /**
      * SApplet Class Constructor
      */
     public SApplet(String[] args)
     {
         this();
         this.init(args);
     }
     /**
      * Called when the applet is loaded.
      */
     public void init()
     {
         super.init();
     }
     /**
      * SApplet Class Constructor
      */
     public void init(String[] args)
     {
         if (this.getApplication() == null)
         {   // Once only
             BaseApplet applet = null;
             if (!gbStandAlone)
                 applet = (BaseApplet)this.getApplet();
             Map<String,Object> properties = null;
             if (args != null)
             {
                 properties = new Hashtable<String,Object>();
                 Util.parseArgs(properties, args);
             }
             m_application = new MainApplication(null, properties, applet);
         }
         super.init(args);
         // Note: This is the default focus traversal policy... VFrameScreen also has to set its own focus traversal policy.
         KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalPolicy(new MyFocusTraversalPolicy());
     }
     /**
      * The start() method is called when the page containing the applet
      * first appears on the screen. The AppletWizard's initial implementation
      * of this method starts execution of the applet's thread.
      */
     public void start()
     {
         super.start();
         if (m_screenField == null)
         {   // This is the code that runs if this is an applet
             m_screenField = new AppletScreen();
             m_screenField.setTask(this);
             m_screenField.setScreenFieldView(m_screenField.setupScreenFieldView(true));
             // NOTE: When an Applet Screen passes no parent, the SApplet peer is not created, set it!
             m_screenField.init(null, null, null, ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
            this.addRecordOwner(m_screenField);
             
             this.setupLookAndFeel(null);
             Container container = this.getBottomPane();
             while (container != null)
             {
             	if (container.getParent() == null)
             		container.setFocusTraversalPolicy(new MyFocusTraversalPolicy());	// Set the traversal policy for an applet
          		container = container.getParent();
             }
         }
         
         BaseScreen.makeScreenFromParams(this, null, m_screenField, ScreenConstants.DONT_PUSH_TO_BROSWER, null);
     }
     /**
      *
      */
     public void setupLookAndFeel(PropertyOwner propertyOwner)
     {
         if (m_screenField == null)
             return;
         // Now that the physical screen is set up, make sure the screeninfo has access to the screen default properties
         ScreenInfo screenInfo = ((VAppletScreen)m_screenField.getScreenFieldView()).getScreenInfo();
         if (propertyOwner == null)
             propertyOwner = m_screenField.retrieveUserProperties(Params.SCREEN);
         screenInfo.setScreenProperties(propertyOwner, null);
         
         super.setupLookAndFeel(propertyOwner);
     }
     /**
      * Change the screen properties to these properties.
      * @param propertyOwner The properties to change to.
      */
     public void setScreenProperties(PropertyOwner propertyOwner, Map<String,Object> properties)
     {
         VAppletScreen vAppletScreen = (VAppletScreen)this.getScreenField().getScreenFieldView();
         vAppletScreen.setScreenProperties(propertyOwner, properties);
     }
     /**
      * The stop() method is called when the page containing the applet is
      * no longer on the screen. The AppletWizard's initial implementation of
      * this method stops execution of the applet's thread.
      */
     public void stopTask()
     {
         super.stopTask();
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
      * Place additional applet clean up code here.  destroy() is called when
      * when your applet is terminating and being unloaded.
      */
     public void destroy()
     {
         super.destroy();
     }
     /**
      * Free/Destroy the control.
      */
     public void free()
     {
         if (m_screenField != null) if (m_screenField.getScreenFieldView().getControl() == this)
         {
             m_screenField.getScreenFieldView().setControl(null);    // keep ~ScreenField from deleteing me!
             m_screenField.free();
             m_screenField = null;
         }
         super.free();
         ClassServiceUtility.getClassService().shutdownService(this);	// Careful of circular calls
     }
     /**
      * This application is done, stop the application.
      */
     public void quit()
     {
         Environment env = null;
         if (this.getApplication() != null)
         	env = ((BaseApplication)this.getApplication()).getEnvironment();
         if (env != null)
         	env.freeIfDone();
 //        if (gbStandAlone)
 //        	System.exit(0); // If standalone (don't call if applet in browser)
     }
     /**
      * Call free for all the Freeable sub-components of the target container.
      * In thick, all the components should have been freed, so don't try again!
      * This is an empty shell.
      * Note: This method is EXACTLY the same as the freeSubComponents method in JBasePanel.
      * @param container The parent component to look through for freeable components.
      */
     public void freeSubComponents(Container container)
     {
     }
     /**
      * STANDALONE APPLICATION SUPPORT
      *    The main() method acts as the applet's entry point when it is run
      * as a standalone application. It is ignored if the applet is run from
      * within an HTML page.
      */
     public static void main(String args[])
     {
         BaseApplet.main(args);  // call super.main();
         // Create Toplevel Window to contain applet SApplet
         // Create the form, passing it the recordset
         FrameScreen frameScreen = new FrameScreen(null, null, null, ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
 
         // The following code starts the applet running within the frame window.
         // It also calls GetParameters() to retrieve parameter values from the
         // command line, and sets m_fStandAlone to true to prevent init() from
         // trying to get them from the HTML page.
         AppletScreen appletScreen = new AppletScreen(null, frameScreen, null, ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
 
         SApplet applet = (SApplet)appletScreen.getScreenFieldView().getControl();
         applet.init();
         applet.start();
     }
     /**
      * Set the AppletScreen.
      */
     public void setScreenField(AppletScreen screenField)
     {
         m_screenField = screenField;
     }
     /**
      * Set the AppletScreen.
      */
     public AppletScreen getScreenField()
     {
         return m_screenField;
     }
     /**
      * PARAMETER SUPPORT
      *      The getParameterInfo() method returns an array of strings describing
      * the parameters understood by this applet.
      *
      * booking Parameter Information:
      *  { "Name", "Type", "Description" },
      */
     public String[][] getParameterInfo()
     {
         String[][] info =
         {
             { Params.REMOTE_HOST, "String", "URL/IP of the RMI/App Server" },
             { DBParams.RECORD, "String", "Name of the Record" },
             { DBParams.SCREEN, "String", "Name of the Screen" },
             { DBParams.USER_NAME, "String", "Name or ID of the User" },
         };
         return info;        
     }
     /**
      * Add any applet sub-panel(s) now.
      */
     public boolean addSubPanels(Container parent)
     {
         parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
         m_paneBottom = parent;
         return true;
     }
     /**
      * Get the pane to add components to.
      */
     public Container getBottomPane()
     {
         return m_paneBottom;
     }
     /**
      * Set the background image's color.
      */
     public void setBackgroundColor(Color colorBackground)
     {
         super.setBackgroundColor(colorBackground);
         JTiledImage tiledImage = this.getTiledImage(this);
         if (tiledImage != null)
             tiledImage.setBackground(colorBackground);
     }
     /**
      * Go thought the entire tree to find the tiled image to change it's color.
      */
     public JTiledImage getTiledImage(Container container)
     {
         JTiledImage tiledImage = null;
         int iCount = container.getComponentCount();
         for (int i = 0; i < iCount; i++)
         {
             Component component = container.getComponent(i);
             if (component instanceof JTiledImage)
                 tiledImage = (JTiledImage)component;
             else if (component instanceof Container)
                 tiledImage = this.getTiledImage((Container)component);
             if (tiledImage != null)
                 return tiledImage;
         }
         return null;
     }
     /**
      * This is a utility method to show an HTML page.
      * @param iOptions Display options
      * @return True if the document was displayed correctly.
      */
     public boolean showTheDocument(String strURL, int iOptions)
     {
         String strTarget = strURL;
         if (strTarget == null)
             strTarget = SApplet.DEFAULT_HELP_URL;   // Default help
         if (this.getApplication() != null)  // Always
             return this.getApplication().showTheDocument(strURL, this, iOptions);
         return false;
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
         if (m_screenField == null)
         {
             FrameScreen frameScreen = new FrameScreen(null, null, null, ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
             
             m_screenField = new AppletScreen();
             m_screenField.setTask(this);
             m_screenField.setScreenFieldView(m_screenField.setupScreenFieldView(true));
             // NOTE: When an Applet Screen passes no parent, the SApplet peer is not created, set it!
             m_screenField.init(null, frameScreen, null, ScreenConstants.DONT_DISPLAY_FIELD_DESC, null);
             
             this.init(null);
             this.addRecordOwner(m_screenField);
 
             BaseScreen.makeScreenFromParams(this, null, m_screenField, 0, null);
         }
     }
     /**
      * This is a special method that runs some code when this screen is opened as a task.
      */
     public void run()
     {
         if (m_screenField != null)
             m_screenField.run();
     }
     /**
      * Fix the weird screen resize problem.
      * Someone changes my size to these weird dimensions. When they do, I have to resize to the right size again.
      * This is a HACK!
      */
     public void setBounds(int x, int y, int width, int height)
     {
         super.setBounds(x, y, width, height);
         if ((x == 0) && (y == 0) && (width == 22) && (height == 30))
             if (m_screenField != null)
                 if (!m_bInResizeLoop)
         {
             m_bInResizeLoop = true; // No endless loop
             m_screenField.resizeToContent(m_screenField.getTitle());
         }
         m_bInResizeLoop = false;
     }
     private boolean m_bInResizeLoop = false;
     /**
      * The browser back button was pressed (Javascript called me).
      * @param command The command that was popped from the browser history.
      */
 	public void doJavaBrowserBack(String command)
 	{
 		if (command != null)
 			if (command.startsWith("#"))
 				command = command.substring(1);
 		BasePanel screen = this.getScreenField();
 		String javaCommand = screen.popHistory(2, false);		// Current command
 		Utility.getLogger().info("Browser back: java command=" + javaCommand + "  browser command=" + command);
 		if (javaCommand == null)
 			javaCommand = command;
 		if ((javaCommand == null) || (javaCommand.length() == 0))
 			javaCommand = this.getInitialCommand(false);
 		screen.handleCommand(this.cleanCommand(javaCommand), screen, ScreenConstants.DONT_PUSH_TO_BROSWER | ScreenConstants.USE_SAME_WINDOW);
 	}
 	/**
 	 * Get the original screen params.
 	 * @return
 	 */
 	public String getInitialCommand(boolean bIncludeAppletCommands)
 	{
 		
 		String strCommand = DBConstants.BLANK;
         if (this.getProperty(Params.SCREEN) != null)
         	strCommand = Utility.addURLParam(strCommand, Params.SCREEN, this.getProperty(Params.SCREEN));
         else if (this.getProperty(DBParams.RECORD) != null)
         {
             Utility.addURLParam(strCommand, DBParams.RECORD, this.getProperty(DBParams.RECORD));
             if (this.getProperty(DBParams.COMMAND) != null)
             	strCommand = Utility.addURLParam(strCommand, DBParams.COMMAND, this.getProperty(DBParams.COMMAND));
         }
         if (strCommand.length() == 0)
         {
             if (this.getProperty(ThinMenuConstants.FORM.toLowerCase()) != null)
             	strCommand = Utility.addURLParam(strCommand, ThinMenuConstants.FORM.toLowerCase(), this.getProperty(ThinMenuConstants.FORM.toLowerCase()));
         }
         if (strCommand.length() == 0)
         	strCommand = super.getInitialCommand(bIncludeAppletCommands);
 		return strCommand;
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
 		BasePanel screen = this.getScreenField();
 		Utility.getLogger().info("Browser forward: browser command=" + command);
 		screen.handleCommand(this.cleanCommand(command), screen, ScreenConstants.DONT_PUSH_TO_BROSWER | ScreenConstants.USE_SAME_WINDOW);
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
 		BasePanel screen = this.getScreenField();
 		Utility.getLogger().info("Browser hash change: java browser command=" + command);
 		screen.handleCommand(this.cleanCommand(command), screen, ScreenConstants.DONT_PUSH_TO_BROSWER | ScreenConstants.USE_SAME_WINDOW);
 	}
     /**
      * Do some applet-wide action.
      * For example, submit or reset. Pass this action down to all the JBaseScreens.
      * Remember to override this method to send the actual data!
      * @param iOptions Extra command options
      */
     public boolean doAction(String strAction, int iOptions)
     {
 		BasePanel screen = this.getScreenField();
 		return screen.handleCommand(strAction, screen, iOptions);
     }
 }
