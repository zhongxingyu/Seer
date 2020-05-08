 package wingset;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.plaf.css.Utils;
 import org.wings.border.SLineBorder;
 import org.wings.header.StyleSheetHeader;
 import org.wings.session.SessionManager;
 
 import javax.swing.tree.*;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import java.io.*;
 import java.util.*;
 import java.awt.*;
 
 /**
  * The root of the WingSet demo application.
  *
  * @author holger engels
  */
 public class WingSet2
 {
     /**
      * If true then use {@link wingset.StatisticsTimerTask} to log statistics on a regular basis to a logging file.
      * (Typically a file named wings-statisticsxxxlog placed in jakarta-tomcat/temp directory)
      */
     private static final boolean LOG_STATISTICS_TO_FILE = true;
 
     private final static Log log = LogFactory.getLog(WingSet2.class);
 
     static {
         if (LOG_STATISTICS_TO_FILE) {
             StatisticsTimerTask.startStatisticsLogging(60);
         }
     }
 
     /**
      * The root frame of the WingSet application.
      */
     private final SFrame frame;
 
     private final SPanel panel;
     private final STree tree;
     private final SPanel header;
     private final SPanel content;
     private WingsImage wingsImage;
     SCardLayout cards = new SCardLayout();
 
     /**
      * Constructor of the wingS application.
      * <p/>
      * <p>This class is referenced in the <code>web.xml</code> as root entry point for the wingS application.
      * For every new client an new {@link org.wings.session.Session} is created which constructs a new instance of this class.
      */
     public WingSet2() {
         wingsImage = new WingsImage();
 
         tree = new STree(new PanesTreeModel());
         tree.setName("examples");
         tree.addTreeSelectionListener(new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                 if (node == null)
                     show(wingsImage);
                 else {
                     Object userObject = node.getUserObject();
                     if (userObject instanceof WingSetPane) {
                         WingSetPane wingSetPane = (WingSetPane)userObject;
                         wingSetPane.initializePanel();
                         show(wingSetPane);
                     }
                     else
                         show(wingsImage);
                 }
             }
         });
         tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         tree.setVerticalAlignment(SConstants.TOP_ALIGN);
 
         SScrollPane scrollPane = new SScrollPane(tree);
         scrollPane.setMode(SScrollPane.MODE_COMPLETE);
         scrollPane.setPreferredSize(new SDimension("220px", "100%"));
         scrollPane.setVerticalAlignment(SConstants.TOP_ALIGN);
         SLineBorder border = new SLineBorder(new Color(190, 190, 190), 0);
         border.setThickness(1, SConstants.RIGHT);
         scrollPane.setBorder(border);
 
         header = createHeader();
 
         content = new SPanel(cards);
         content.setPreferredSize(SDimension.FULLAREA);
         content.add(wingsImage);
 
         panel = new SPanel(new SBorderLayout());
         panel.setPreferredSize(SDimension.FULLAREA);
         panel.add(header, SBorderLayout.NORTH);
         panel.add(scrollPane, SBorderLayout.WEST);
         panel.add(content, SBorderLayout.CENTER);
 
         frame = new SFrame("WingSet Demo");
         frame.getContentPane().add(panel, SBorderLayout.CENTER);
         frame.getContentPane().setPreferredSize(SDimension.FULLAREA);
         if (!Utils.isMSIE(frame)) {
             frame.getContentPane().setAttribute("position", "absolute");
             frame.getContentPane().setAttribute("height", "100%");
             frame.getContentPane().setAttribute("width", "100%");
         }
         frame.setAttribute("position", "absolute");
         frame.setAttribute("height", "100%");
         frame.setAttribute("width", "100%");
 
         frame.addHeader(new StyleSheetHeader("../css/wingset2.css"));
 
         frame.show();
     }
 
     private void show(SComponent component) {
         if (component.getParent() != content) {
             content.add(component);
         }
         cards.show(component);
     }
 
     private SPanel createHeader() {
         SPanel header = new SPanel();
         header.setPreferredSize(SDimension.FULLWIDTH);
        header.setStyle("header");
 
         try {
             header.setLayout(new STemplateLayout(SessionManager.getSession().
                     getServletContext().getRealPath("/templates/WingSetHeader.thtml")));
         } catch (java.io.IOException ex) {
             log.error("Could not find template file!", ex);
         }
 
         return header;
     }
 
     static class PanesTreeModel
         extends DefaultTreeModel
     {
         public PanesTreeModel() {
             super(buildNodes());
         }
 
         private static TreeNode buildNodes() {
             DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demo");
             DefaultMutableTreeNode wings = new DefaultMutableTreeNode("wingS");
             root.add(wings);
             DefaultMutableTreeNode wingx = new DefaultMutableTreeNode("wingX");
             root.add(wingx);
 
             String dirName = SessionManager.getSession().getServletContext().getRealPath("/WEB-INF/classes/wingset");
             String includeTests = (String)SessionManager.getSession().getProperty("wingset.include.tests");
             String includeExperiments = (String)SessionManager.getSession().getProperty("wingset.include.experiments");
 
             File dir = new File(dirName);
 
             String[] wingsClassFileNames = dir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith("Example.class") && !name.startsWith("X");
                 }
             });
             Arrays.sort(wingsClassFileNames);
             buildNodes(wings, wingsClassFileNames);
 
             String[] wingxClassFileNames = dir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith("Example.class") && name.startsWith("X");
                 }
             });
             Arrays.sort(wingxClassFileNames);
             buildNodes(wingx, wingxClassFileNames);
 
             if ("TRUE".equalsIgnoreCase(includeTests)) {
                 DefaultMutableTreeNode tests = new DefaultMutableTreeNode("Tests");
                 root.add(tests);
 
                 String[] testClassFileNames = dir.list(new FilenameFilter() {
                     public boolean accept(File dir, String name) {
                         return name.endsWith("Test.class");
                     }
                 });
                 Arrays.sort(testClassFileNames);
                 buildNodes(tests, testClassFileNames);
             }
 
             if ("TRUE".equalsIgnoreCase(includeExperiments)) {
                 DefaultMutableTreeNode experiments = new DefaultMutableTreeNode("Experiments");
                 root.add(experiments);
 
                 String[] experimentClassFileNames = dir.list(new FilenameFilter() {
                     public boolean accept(File dir, String name) {
                         return name.endsWith("Experiment.class");
                     }
                 });
                 Arrays.sort(experimentClassFileNames);
                 buildNodes(experiments, experimentClassFileNames);
             }
 
             return root;
         }
 
         private static void buildNodes(DefaultMutableTreeNode node, String[] classFileNames) {
             for (int i = 0; i < classFileNames.length; i++) {
                 String classFileName = classFileNames[i];
                 String className = "wingset." + classFileName.substring(0, classFileName.length() - ".class".length());
                 try {
                     Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                     WingSetPane example = (WingSetPane)clazz.newInstance();
                     DefaultMutableTreeNode child = new DefaultMutableTreeNode(example);
                     node.add(child);
                 }
                 catch (Throwable e) {
                     System.err.println("Could not load plugin: " + className);
                     e.printStackTrace();
                 }
             }
         }
     }
 }
