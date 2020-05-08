 package tk.amberide;
 
 import tk.amberide.ide.data.io.LoggedOutputStream;
 import tk.amberide.ide.os.OS;
 import tk.amberide.ide.data.res.xml.XMLResourceManager;
 import tk.amberide.ide.data.Workspace;
 import tk.amberide.ide.data.state.IStateManager;
 import tk.amberide.ide.data.state.LazyState;
 import tk.amberide.ide.data.state.Scope;
 import tk.amberide.ide.data.state.node.IState;
 import tk.amberide.ide.data.state.node.SimpleState;
 import tk.amberide.ide.data.state.xml.XMLStateManager;
 import tk.amberide.ide.gui.AmberUIManager;
 import tk.amberide.ide.gui.FileViewerPanel;
 import tk.amberide.ide.gui.editor.map.MapEditorPanel;
 import tk.amberide.ide.gui.editor.text.ScriptEditorPanel;
 import tk.amberide.ide.gui.misc.ErrorHandler;
 import tk.amberide.ide.gui.misc.FileSystemIcon;
 import tk.amberide.ide.gui.misc.TipOfTheDay;
 import tk.amberide.ide.gui.viewer.AudioViewerPanel;
 import tk.amberide.ide.gui.viewer.ImageViewerPanel;
 import tk.amberide.ide.swing.Dialogs;
 import tk.amberide.ide.swing.UIUtil;
 import tk.amberide.ide.tool.ToolDefinition;
 import java.awt.EventQueue;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 /**
  * @author Tudor
  */
 public class Amber {
 
     private static IDE main;
     private static XMLResourceManager resources;
     private static IStateManager states = new XMLStateManager();
     private static Workspace workspace;
     private static File root = new File(System.getProperty("user.home") + File.separatorChar + ".amber");
 
     static {
         if (!root.exists()) {
             root.mkdirs();
         }
     }
 
     /**
      * Main method: entry point to Amber. Should not be called directly.
      *
      * @param args arguments to hint options to Amber
      */
     public static void main(final String[] args) throws FileNotFoundException {
         try {
             OS.loadNativeLibraries();
             Storage.init();
             //setupLogging();
             UIUtil.makeNative();
             AmberUIManager.setup();
             try {
                 ErrorHandler.init();
             } catch (Exception e) {
             }
             ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
 
             Runtime.getRuntime().addShutdownHook(new Thread() {
                 @Override
                 public void run() {
                     System.out.println("Shutting down...");
                     try {
                         if (states != null) {
                             states.emitStates();
                         }
                         if (resources != null) {
                             resources.emitResources();
                         }
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             });
             if (args.length == 1) {
                 initializeProject(new File(args[0]));
             }
             states.registerMacro("${GLOBAL.DIR}", root.getAbsolutePath());
             loadStates(Scope.GLOBAL);
 
             setupFileIcons();
             setupFileViewers();
 
             Settings.load();
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     main = new IDE();
                     try {
                         restoreLastWorkspace();
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                     main.setVisible(true);
                     showTipOfTheDay();
                     states.registerStateOwner(Amber.class);
                 }
             });
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     @LazyState(scope = Scope.GLOBAL, name = "LastProjectDirectory")
     protected static String saveLastProject() {
         return workspace != null ? workspace.getRootDirectory().getAbsolutePath() : null;
     }
 
     private static void setupFileViewers() {
         FileViewerPanel.setDefaultPanel(ScriptEditorPanel.class);
         FileViewerPanel.registerPanel(MapEditorPanel.class, "m");
         FileViewerPanel.registerPanel(ImageViewerPanel.class, "jpg", "jpeg", "png", "gif", "bmp");
         FileViewerPanel.registerPanel(AudioViewerPanel.class, "wav", "midi", "mid", "aiff", "ogg");
     }
 
     private static void setupFileIcons() {
         FileSystemIcon.setIcon("m", new ImageIcon(ClassLoader.getSystemResource("icon/General.Map.png")));
         FileSystemIcon.setIcon("rb", new ImageIcon(ClassLoader.getSystemResource("icon/General.Script.png")));
     }
 
     private static void restoreLastWorkspace() throws Exception {
         IState lastProject = states.getState(Scope.GLOBAL, "LastProjectDirectory");
         if (workspace == null && lastProject != null && lastProject.get() != null) {
             File root = new File((String) lastProject.get());
             System.out.println("Loaded project " + root);
             if (root.exists()) {
                 openWorkspace(root);
             }
         }
     }
 
     private static void openWorkspace(File root) throws Exception {
         workspace = new Workspace(root);
         resources = new XMLResourceManager(workspace);
         resources.loadResources();
         root.mkdirs();
         states.registerMacro("${PROJECT.DIR}", workspace.getDataDirectory().getAbsolutePath());
         states.clearStates(Scope.PROJECT);
         states.loadStates(Scope.PROJECT);
         main.loadProject(workspace);
         main.setTitle(String.format("Amber IDE (%s)", root.getAbsolutePath()));
         Storage.recentProjects.put(workspace.getRootDirectory().getAbsolutePath(), new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        main.validate();
     }
 
     private static void setupLogging() throws IOException {
         File log = new File("amber.log");
         if (!log.exists()) {
             log.createNewFile();
         }
         LoggedOutputStream logger = new LoggedOutputStream(log);
         System.setOut(logger);
         System.setErr(logger);
     }
 
     private static void loadStates(int scope) {
         try {
             states.loadStates(scope);
         } catch (Exception e) {
             e.printStackTrace();
             Dialogs.errorDialog()
                     .setTitle("Failed to load previous state")
                     .setMessage("Something went wrong while restoring project state: " + e)
                     .show();
         }
     }
 
     private static void showTipOfTheDay() {
         IState tips = states.getState(Scope.GLOBAL, "ShowTipOfTheDay");
         if (tips == null) {
             tips = new SimpleState("ShowTipOfTheDay", true);
             states.addState(Scope.GLOBAL, tips);
         }
         if ((Boolean) tips.get()) {
             ((SimpleState) tips).set(TipOfTheDay.showTipOfTheDay());
         }
     }
 
     /**
      * Initializes a new Amber project in the specified root folder, and opens
      * it in the UI.
      *
      * @param root the directory that the project should be created in
      */
     public static void initializeProject(File root) {
         if (workspace != null) {
             if (Dialogs.confirmDialog()
                     .setTitle("Opening project...")
                     .setMessage("The project can either be opened in this window, or in a new window.\n"
                     + "Where would you like to open the project?")
                     .setOptionType(JOptionPane.YES_NO_CANCEL_OPTION)
                     .setMessageType(JOptionPane.QUESTION_MESSAGE)
                     .setOptions("This window", "New window", "Cancel").show() == JOptionPane.NO_OPTION) {
                 try {
                     OS.newInstance(Amber.class, root.getAbsolutePath());
                     return;
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
         try {
             openWorkspace(root);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * Fetches the current project ResourceManager
      *
      * @return the resource manager currently in use
      */
     public static XMLResourceManager getResourceManager() {
         return resources;
     }
 
     /**
      * Fetches the Workspace of the current project
      *
      * @return the Workspace object
      */
     public static Workspace getWorkspace() {
         return workspace;
     }
 
     /**
      * Opens a file in the editor panel. Type lookup is done by file extension.
      *
      * @param file the file to be opened
      */
     public static void openFileTab(File file) {
         main.openFile(file);
     }
 
     public static void openToolTab(ToolDefinition tool) {
         main.addToolTab(tool);
     }
 
     /**
      * Gets the main JFrame which hosts the current Amber IDE process
      *
      * @return the IDE JFrame
      */
     public static IDE getUI() {
         return main;
     }
 
     public static IStateManager getStateManager() {
         return states;
     }
 }
