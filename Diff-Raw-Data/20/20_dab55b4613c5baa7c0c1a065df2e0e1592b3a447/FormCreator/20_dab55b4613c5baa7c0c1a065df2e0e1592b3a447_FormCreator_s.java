 package phyml.gui.control;
 
 import com.google.common.eventbus.Subscribe;
 import grisu.jcommons.processes.ExternalCommand;
 import grisu.jcommons.utils.swing.LogPanel;
 import org.apache.commons.lang.StringUtils;
 import phyml.gui.view.ExternalCommandLogPanel;
 import phyml.gui.view.SubmitPanel;
 
 import javax.swing.*;
 
 /**
  * This class assembles the Form according to the node list.
  * <p/>
  * Later on we can make it easier to configure (for example with a text file), but for now configuration
  * is done programatically.
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 4/10/13
  * Time: 3:38 PM
  */
 public class FormCreator {
 
     public static final int COLLAPSIBLE_LAYOUT = 3;
     private final NodeController controller;
     private JTabbedPane tabbedPane;
     private LogPanel logPanel;
     private final int layout;
     private SubmitPanel submitPanel;
     private final boolean displayDebug;
 
     public FormCreator(NodeController nc) {
         this(nc, COLLAPSIBLE_LAYOUT, false);
     }
 
     public FormCreator(NodeController nc, int layout, boolean debug) {
         this.controller = nc;
         this.layout = layout;
         this.displayDebug = debug;
 
         NodeController.eventBus.register(this);
 
         nc.initializeNodes();
 
     }
 
     public static void main(String[] args) {
 
         if (args.length < 1 || args.length > 2) {
             System.err.println("Wrong number of arguments. Only name of controller class (and, optionally, 'debug' string) is allowed.");
             System.exit(1);
         }
 
         String controllerClass = args[0];
         boolean debug = false;
         if ( args.length == 2 ) {
             if (StringUtils.equals("debug", args[1])) {
                 debug = true;
             }
         }
 
         NodeController controller = null;
 
         ClassLoader classLoader = FormCreator.class.getClassLoader();
         try {
             Class aClass;
             try {
                 aClass = classLoader.loadClass(controllerClass);
             } catch (ClassNotFoundException e) {
                 aClass = classLoader.loadClass("phyml.gui.control." + controllerClass);
             }
 
             controller = (NodeController) aClass.newInstance();
 
         } catch (Exception e) {
             System.err.println("Error creating controller class: " + e.getLocalizedMessage());
             System.exit(2);
         }
 
         FormCreator fc = new FormCreator(controller, FormCreator.COLLAPSIBLE_LAYOUT, debug);
 
         fc.display();
 
     }
 
 
 
     public void display() {
         try {
             SwingUtilities.invokeAndWait(new Thread() {
                 public void run() {
                     final JFrame frame = new JFrame("InputForm");
                     frame.setSize(800, 400);
 
                     Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                         @Override
                         public void uncaughtException(Thread t, Throwable e) {
 
                             JOptionPane.showMessageDialog(frame,
                                     e.getLocalizedMessage(),     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
 
                    addPane(controller.getTitle(), getSubmitPanel());
 
                     frame.setContentPane(getTabbedPane());
                     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                     //        frame.pack();
                     frame.setVisible(true);
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private void addPane(String title, JComponent panel) {
         getTabbedPane().add(title, panel);
     }
 
     public LogPanel getLogPanel() {
         if ( logPanel == null ) {
             logPanel = new LogPanel();
         }
         return logPanel;
     }
 
     public JTabbedPane getTabbedPane() {
         if ( tabbedPane == null ) {
             tabbedPane = new JTabbedPane();
         }
         return tabbedPane;
     }
 
     public SubmitPanel getSubmitPanel() {
         if ( submitPanel == null ) {
             submitPanel = new SubmitPanel(controller, layout, displayDebug);
         }
         return submitPanel;
     }
 
     @Subscribe
     public void newPhyMLRun(ExternalCommand ec) {
         ExternalCommandLogPanel panel = new ExternalCommandLogPanel(ec);
         addPane("Execution log", panel);
         getTabbedPane().setSelectedComponent(panel);
     }
 
     @Subscribe
     public void phyMLRunPanelCanBeRemoved(final ExternalCommandLogPanel eclp) {
         SwingUtilities.invokeLater(new Thread() {
             public void run() {
                 removePane(eclp);
                getTabbedPane().setSelectedComponent(getSubmitPanel());
             }
         });
 
     }
 
     public void removePane(JComponent c) {
         getTabbedPane().remove(c);
     }
 }
