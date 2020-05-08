 package org.geworkbench.engine.config;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.swing.JOptionPane;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.engine.ccm.ComponentConfigurationManager;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.config.rules.GeawConfigRule;
 import org.geworkbench.engine.config.rules.PluginRule;
 import org.geworkbench.util.SplashBitmap;
 
 import com.jgoodies.looks.plastic.PlasticLookAndFeel;
 import com.jgoodies.looks.plastic.theme.SkyBlue;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust, Inc.</p>
  *
  * @author First Genetic Trust, Inc.
  * @version 1.0
  */
 
 /**
  * The starting point for an application. Parses the application configuration
  * file.
  */
 public class UILauncher {
 	private static Log log = LogFactory.getLog(UILauncher.class);
 	
     public static String getComponentsDirectory() {
     	return componentsDir;
     }
 
 	/**
      * The name of the string in the <code>application.properties</code> file
      * that contains the location of the application configuration file.
      */
     private final static String CONFIG_FILE_NAME = "component.configuration.file";
 
     private static SplashBitmap splash = new org.geworkbench.util.SplashBitmap(SplashBitmap.class.getResource("splashscreen.png"));
     
     private static final String LOOK_AND_FEEL_FLAG = "-lookandfeel";
 
     private static final String DEFAULT_COMPONENTS_DIR = "components";
     private static final String COMPONENTS_DIR_PROPERTY = "components.dir";
     private static String componentsDir = null;
 
     /**
      * Configure the rules for translating the application configuration file.
      */
     private static Digester createDigester() {
         Digester digester = new Digester(new org.apache.xerces.parsers.SAXParser());
 
         digester.setUseContextClassLoader(true);
         // Opening tag <geaw-config>
         digester.addRule("geaw-config", new GeawConfigRule("org.geworkbench.engine.config.rules.GeawConfigObject"));
         // Creates the top-level GUI window
         digester.addObjectCreate("geaw-config/gui-window", "org.geworkbench.engine.config.rules.GUIWindowObject");
         digester.addCallMethod("geaw-config/gui-window", "createGUI", 1);
         digester.addCallParam("geaw-config/gui-window", 0, "class");
         // Instantiates a plugin and adds it in the PluginResgistry
         digester.addRule("geaw-config/plugin", new PluginRule("org.geworkbench.engine.config.rules.PluginObject"));
         // Registers a plugin with an extension point
         digester.addCallMethod("geaw-config/plugin/extension-point", "addExtensionPoint", 1);
         digester.addCallParam("geaw-config/plugin/extension-point", 0, "name");
         // Registers a visual plugin with the top-level application GUI.
         digester.addCallMethod("geaw-config/plugin/gui-area", "addGUIComponent", 1);
         digester.addCallParam("geaw-config/plugin/gui-area", 0, "name");
         // Associates the plugin's module methods to plugin modules
         digester.addCallMethod("geaw-config/plugin/use-module", "addModule", 2);
         digester.addCallParam("geaw-config/plugin/use-module", 0, "name");
         digester.addCallParam("geaw-config/plugin/use-module", 1, "id");
         // Turn subscription object on and off
         digester.addCallMethod("geaw-config/plugin/subscription", "handleSubscription", 2);
         digester.addCallParam("geaw-config/plugin/subscription", 0, "type");
         digester.addCallParam("geaw-config/plugin/subscription", 1, "enabled");
         // Sets up a coupled listener relationship involving 2 plugins.
         digester.addCallMethod("geaw-config/plugin/coupled-event", "registerCoupledListener", 2);
         digester.addCallParam("geaw-config/plugin/coupled-event", 0, "event");
         digester.addCallParam("geaw-config/plugin/coupled-event", 1, "source");
         
         return digester;
     }
 
     /**
      * Reads application properties from a file called
      * <bold>application.properties</bold>
      */
     private static void initProperties() {
         InputStream reader = null;
         try {
             reader = Class.forName(UILauncher.class.getName()).getResourceAsStream("/application.properties");
             System.getProperties().load(reader);
             if (System.getSecurityManager() == null) {
                 System.setSecurityManager(new SecurityManager());
             }
             reader.close();
         } catch (ClassNotFoundException cnfe) {
             cnfe.printStackTrace();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
         //Set system-wide ToolTip properties
         ToolTipManager.sharedInstance().setInitialDelay(100);
     }
 
     private static void exitOnErrorMessage(String message) {
         JOptionPane.showMessageDialog(null, message, "Application Startup Error", JOptionPane.ERROR_MESSAGE);
         System.exit(1);
     }
 
     /**
      * The application start point.
      *
      * @param args
      */
     public static void main(String[] args) {
         String configFileArg = null;
         String lookAndFeelArg = null;
 
         for (int i = 0; i < args.length; i++) {
             if (LOOK_AND_FEEL_FLAG.equals(args[i])) {
                 if (args.length == (i + 1)) {
                     exitOnErrorMessage("No look & feel parameter specified.");
                 } else {
                     i++;
                     lookAndFeelArg = args[i];
                 }
             } else {
                 configFileArg = args[i];
             }
         }
         try {
             if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") == -1) {
                 // If we're not on windows, then use native look and feel no matter what
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } else {
 
                 if (lookAndFeelArg != null) {
                     if ("native".equals(lookAndFeelArg)) {
                         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                     } else if ("plastic".equals(lookAndFeelArg)) {
                         PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
                         UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
                     } else {
                         UIManager.setLookAndFeel(lookAndFeelArg);
                     }
                 } else {
                     // Default to plastic.
                     PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
                     UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
                 }
             }
         } 
         catch (Exception e) {
             log.error(e,e);
         }
 
         splash.hideOnClick();
         splash.addAutoProgressBarIndeterminate();
         splash.setProgressBarString("loading...");
         splash.showSplash();
 
         // Read the properties file
         initProperties();
 
         componentsDir = System.getProperty(COMPONENTS_DIR_PROPERTY);
 		if (componentsDir == null) {
 			componentsDir = DEFAULT_COMPONENTS_DIR;
 		}
         
         Digester digester = createDigester();
 
         org.geworkbench.util.Debug.debugStatus = false; // debugging toggle
         // Locate and open the application configuration file.
         String configFileName = null;
         if (configFileArg != null) {
             configFileName = configFileArg;
         } else {
             configFileName = System.getProperty(CONFIG_FILE_NAME);
             if (configFileName == null)
                 exitOnErrorMessage("Invalid or absent configuration file.");
         }
 
         try {
             InputStream is = Class.forName(UILauncher.class.getName()).getResourceAsStream("/" + configFileName);
             if (is == null) {
                 exitOnErrorMessage("Invalid or absent configuration file.");
             }
             digester.parse(is);
             is.close();
         } catch (Exception e) {
             log.error(e,e);
             exitOnErrorMessage("Exception in parsing the configuration file.");
         }
 
         /* Load Components */
         ComponentConfigurationManager ccm = ComponentConfigurationManager.getInstance(); 
         ccm.loadAllComponentFolders();
         ccm.loadSelectedComponents();
         
         PluginRegistry.debugPrint();
 
         splash.hideSplash();
         GUIFramework guiWindow = GeawConfigObject.getGuiWindow();
         guiWindow.setVisible(true);
     }
 
 	public static void setProgressBarString(String name) {
 		splash.setProgressBarString(name);
 	}
 
 }
 
