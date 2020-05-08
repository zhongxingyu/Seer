 package edu.wustl.cab2b.client.ui.mainframe;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.geom.Point2D;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.MissingResourceException;
 import java.util.Scanner;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 
 import org.jdesktop.swingx.JXFrame;
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXStatusBar;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.wustl.cab2b.client.cache.ClientSideCache;
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.CustomizableBorder;
 import edu.wustl.cab2b.client.ui.experiment.ExperimentPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.BusinessInterface;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeHandler;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentHome;
 import edu.wustl.cab2b.common.locator.Locator;
 import edu.wustl.cab2b.common.locator.LocatorException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Main frame of the caB2B application.
  * 
  * @author chetan_bh
  */
 public class MainFrame extends JXFrame {
 
     private static final long serialVersionUID = 1234567890L;
 
     /** Resource bundle name for getting error codes and its description. */
     public static String errorCodesFileName = "errorcodes";
 
     /** Resource bundle name for getting externalized strings. */
     public static String applicationResourcesFileName = "Cab2bApplicationResources";
 
     /** Resource bundle name for getting jndi properties. */
     public static String jndiResourceFileName = "jndi";
 
     public static String cab2bLogoName = "b2b_logo_image.gif";
 
     public static URL progressBarURL;
 
     /**
      * Everything related GUI's containers and its components size is relative
      * to this size.
      */
     public static Dimension mainframeScreenDimesion = Toolkit.getDefaultToolkit().getScreenSize();
 
     public static SearchDataWelcomePanel searchDataWelcomePanel = null;
 
     public static ExperimentPanel openExperimentWelcomePanel = null;
 
     public static NewWelcomePanel newWelcomePanel = null;
 
     private static JProgressBar progressBar;
 
     private static JLabel progressBarLabel;
 
     /**
      * Global navigation panel which is at the top of the MainFrame.
      */
     GlobalNavigationPanel globalNavigationPanel;
 
     JXPanel leftHandSidePanel;
 
     JXPanel homePanel;
 
     JXPanel searchPanel;
 
     JXPanel experimentPanel;
 
     MainFrameStackedBoxPanel myStackedBoxPanel;
 
     JSplitPane splitPane;
 
     // fields used by the status bar
     public static enum Status {
         READY, BUSY
     };
 
     private static Cab2bLabel status;
 
     private static Cab2bLabel statusMessage;
 
     private JXStatusBar statusBar;
 
     public static JXPanel mainPanel;
 
     public MainFrame() {
         this("");
     }
 
     public MainFrame(String title) {
         this(title, true);
     }
 
     public MainFrame(String title, boolean exitOnClose) {
         super(title, exitOnClose);
         statusBar = WindowUtilities.getStatusBar(this);
         initGUI();
     }
 
     /**
      * Initialize GUI for main frame.
      */
     private void initGUI() {
         setExtendedState(JXFrame.MAXIMIZED_BOTH);
         URL url = this.getClass().getClassLoader().getResource("b2b_logo_image.gif");
         Image im = Toolkit.getDefaultToolkit().getImage(url);
         this.setIconImage(im);
         this.setLayout(new BorderLayout());
 
         globalNavigationPanel = new GlobalNavigationPanel(this, this);
         this.add(globalNavigationPanel, BorderLayout.NORTH);
 
         // set new welcome panel
         newWelcomePanel = new NewWelcomePanel(this);
         homePanel = newWelcomePanel;
         homePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
 
         myStackedBoxPanel = new MainFrameStackedBoxPanel(this);
         myStackedBoxPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
 
         splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, myStackedBoxPanel, homePanel);
         splitPane.setBorder(null);
         splitPane.setDividerSize(4);
         Dimension dim = CommonUtils.getRelativeDimension(mainframeScreenDimesion, 0.25f, 0.0f);
         splitPane.setDividerLocation(242);
         splitPane.setOneTouchExpandable(false);
 
         mainPanel = new Cab2bPanel(new RiverLayout());
         mainPanel.add("hfill vfill", splitPane);
         mainPanel.setBorder(new CustomizableBorder(new Insets(10, 0, 6, 10), true, true));
         mainPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
 
         this.add(mainPanel, BorderLayout.CENTER);
 
         statusBar = WindowUtilities.getStatusBar(this);
         JXStatusBar.Constraint c1 = new JXStatusBar.Constraint();
         c1.setFixedWidth(100);
         status = new Cab2bLabel("Ready");
         statusMessage = new Cab2bLabel();
         statusBar.add(status, c1); // Fixed width of 100 with no inserts
         statusBar.add(new JSeparator(JSeparator.VERTICAL));
         statusBar.add(statusMessage);
         this.add(statusBar, BorderLayout.SOUTH);
 
     }
 
     /** Method to set experiment home panel */
     public void setOpenExperimentWelcomePanel() {
         CustomSwingWorker swingWorker = new CustomSwingWorker(MainFrame.mainPanel) {
             @Override
             protected void doNonUILogic() throws RuntimeException {
                 openExperimentWelcomePanel = new ExperimentPanel("My Experiments");
                 GradientPaint gp = new GradientPaint(new Point2D.Double(.05d, 0), Color.WHITE, new Point2D.Double(
                         .95d, 0), Color.WHITE);
                 openExperimentWelcomePanel.setTitlePainter(new BasicGradientPainter(gp));
                 openExperimentWelcomePanel.setTitleForeground(Color.BLUE);
             }
 
             @Override
             protected void doUIUpdateLogic() throws RuntimeException {
                 mainPanel.removeAll();
                 MainFrame.this.remove(mainPanel);
                 MainFrame.this.add(openExperimentWelcomePanel);
             }
         };
         swingWorker.start();
     }
 
     /** Method to set data search home panel */
     public void setSearchDataWelcomePanel() {
 
         CustomSwingWorker swingWorker = new CustomSwingWorker(this) {
             @Override
             protected void doNonUILogic() throws RuntimeException {
                 if (searchDataWelcomePanel == null) {
                     searchDataWelcomePanel = new SearchDataWelcomePanel(MainFrame.this);
                 }
                 homePanel = searchDataWelcomePanel;
             }
 
             @Override
             protected void doUIUpdateLogic() throws RuntimeException {
                 setWelcomePanel();
             }
         };
         swingWorker.start();
     }
 
     /** Method to set home panel */
     public void setHomeWelcomePanel() {
 
         CustomSwingWorker swingWorker = new CustomSwingWorker(this) {
             @Override
             protected void doNonUILogic() throws RuntimeException {
                 if (newWelcomePanel == null) {
                     newWelcomePanel = new NewWelcomePanel(MainFrame.this);
                 }
                 homePanel = newWelcomePanel;
             }
 
             @Override
             protected void doUIUpdateLogic() throws RuntimeException {
                 setWelcomePanel();
             }
         };
         swingWorker.start();
 
     }
 
     /** Method to set Welcome panel */
     public void setWelcomePanel() {
         // homePanel.setBorder(new CustomizableBorder(new Insets(1, 1, 1, 1),
        // true, true));
         splitPane.setRightComponent(homePanel);
         splitPane.setOneTouchExpandable(true);
         //mension dim = CommonUtils.getRelativeDimension(mainframeScreenDimesion, 0.25f, 0.0f);
         splitPane.setDividerLocation(242);
         splitPane.setOneTouchExpandable(false);
        mainPanel.removeAll();
         mainPanel.add("hfill vfill", splitPane);
         // mainPanel.setBorder(new CustomizableBorder(new Insets(10, 0, 6, 10),
         // true, true));
 
        if (openExperimentWelcomePanel != null && openExperimentWelcomePanel.isVisible() == true) {
            Logger.out.debug("Removing openExperimentWelcomePanel");
            openExperimentWelcomePanel.removeAll();
            this.remove(openExperimentWelcomePanel);
            this.validate();
        }

         this.add(mainPanel);
     }
 
     public void setDataForMySearchQueriesPanel(Vector data) {
         myStackedBoxPanel.setDataForMySearchQueriesPanel(data);
     }
 
     public void setDataForPopularSearchCategoriesPanel(Vector data) {
         myStackedBoxPanel.setDataForPopularSearchCategoriesPanel(data);
     }
 
     public void setDataForMyExperimentsPanel(Vector data) {
         myStackedBoxPanel.setDataForMyExperimentsPanel(data);
     }
 
     protected static void initializeResources() {
         /* Initialze logger. */
 
         /* Initialize error codes resource bundhe. */
         try {
             ErrorCodeHandler.initBundle(errorCodesFileName);
         } catch (MissingResourceException mre) {
             CheckedException checkedException = new CheckedException(mre.getMessage(), mre,
                     ErrorCodeConstants.IO_0002);
             CommonUtils.handleException(checkedException, null, true, true, false, true);
         }
         /* Initialize application resources bundle. */
         try {
             ApplicationProperties.initBundle(applicationResourcesFileName);
         } catch (MissingResourceException mre) {
             CheckedException checkedException = new CheckedException(mre.getMessage(), mre,
                     ErrorCodeConstants.IO_0002);
             CommonUtils.handleException(checkedException, null, true, true, false, true);
         }
     }
 
     public Vector getExperiments() {
         Vector dataVector = new Vector();
 
         // EJB code start
         BusinessInterface bus = null;
         try {
             bus = Locator.getInstance().locate(edu.wustl.cab2b.common.ejb.EjbNamesConstants.EXPERIMENT,
                                                ExperimentHome.class);
         } catch (LocatorException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         }
 
         ExperimentBusinessInterface expBus = (ExperimentBusinessInterface) bus;
         try {
             dataVector = expBus.getExperimentHierarchy();
         } catch (RemoteException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         } catch (ClassNotFoundException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         } catch (DAOException e1) {
             CommonUtils.handleException(e1, this, true, true, false, false);
         }
 
         return dataVector;
     }
 
     /**
      * set the status bar message
      * 
      * @param message
      *            the message
      */
     public static void setStatusMessage(String message) {
         MainFrame.statusMessage.setText(message);
     }
 
     /**
      * set the status in the status bar
      * 
      * @param status
      *            the status
      */
     public static void setStatus(Status status) {
         if (status == Status.BUSY) {
             MainFrame.status.setText("Busy");
         } else if (status == Status.READY) {
             MainFrame.status.setText("Ready");
         }
 
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Logger.configure("log4j.properties");
 
         JFrame progressBarFrame = new JFrame("caB2B client launcher....");
         int imageX = 442;
         int imageY = 251;
         int progressbarY = 14;
         int labelY = 20;
         progressBar = new JProgressBar();
         progressBar.setIndeterminate(true);
         progressBar.setPreferredSize(new Dimension(imageX, progressbarY));
         progressBar.setMinimum(0);
         progressBar.setMaximum(100);
         progressBar.setValue(0);
         progressBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
         progressBarLabel = new JLabel(" Launching caB2B client....");
         progressBarLabel.setForeground(Color.WHITE);
         progressBarLabel.setPreferredSize(new Dimension(imageX, labelY));
         String fontFamily = progressBarLabel.getFont().getFamily();
         progressBarLabel.setFont(new Font(fontFamily, Font.PLAIN, 14));
 
         /* Initialize all Resources. */
         initializeResources();
         String mainFrameTitle = ApplicationProperties.getValue("cab2b.main.frame.title");
 
         MainFrame mainFrame = new MainFrame(mainFrameTitle);
 
         ClassLoader loader = mainFrame.getClass().getClassLoader();
         Image backgroundImage = new ImageIcon(loader.getResource("progress_bar.gif")).getImage();
         BackgroundImagePanel imagePanel = new BackgroundImagePanel(backgroundImage);
         imagePanel.setPreferredSize(new Dimension(imageX, imageY));
         imagePanel.add(progressBarLabel, BorderLayout.SOUTH);
 
         int height = imageY + progressbarY;
         progressBarFrame.setSize(imageX, height);
         progressBarFrame.getContentPane().add(imagePanel, BorderLayout.CENTER);
         progressBarFrame.getContentPane().add(progressBar, BorderLayout.SOUTH);
         progressBarFrame.setLocation((mainframeScreenDimesion.width - imageX) / 2,
                                      (mainframeScreenDimesion.height - height) / 2);
         progressBarFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(cab2bLogoName));
         progressBarFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         progressBarFrame.setAlwaysOnTop(true);
         progressBarFrame.setUndecorated(true);
         progressBarFrame.setVisible(true);
         Toolkit.getDefaultToolkit().setDynamicLayout(true);
         ClientSideCache.getInstance(); // initializing the cache at startup
 
         Vector<String> myRecentExperiments = new Vector<String>();
         myRecentExperiments.add("Breast Cancer Microarrays (Hu133 Plus 2.0)");
         myRecentExperiments.add("Breast Cancer Microarrays (MOE430 Plus 2.0)");
         myRecentExperiments.add("Acute Myelogenous Leukemia Microarrays");
         mainFrame.setDataForMyExperimentsPanel(myRecentExperiments);
 
         progressBar.setValue(90);
 
         Vector<String> mySearchQueries = new Vector<String>();
         mySearchQueries.add("Prostate Cancer Microarray Data");
         mySearchQueries.add("Glioblastoma Microarray Data");
         mainFrame.setDataForMySearchQueriesPanel(mySearchQueries);
 
         Vector<String> popularSearchCategories = new Vector<String>();
         popularSearchCategories.add("Gene Annotation");
         popularSearchCategories.add("Microarray Annotation");
         popularSearchCategories.add("Tissue Biospecimens");
         popularSearchCategories.add("Molecular Biospecimens");
         mainFrame.setDataForPopularSearchCategoriesPanel(popularSearchCategories);
 
         progressBar.setValue(100);
         progressBarFrame.setVisible(false);
         progressBarFrame.removeAll();
         progressBarFrame = null;
         mainFrame.setVisible(true);
     }
 
     /**
      * @return Returns the progressBarLabel.
      */
     public static JLabel getProgressBarLabel() {
         return progressBarLabel;
     }
 
     /**
      * @return Returns the progressBar.
      */
     public static JProgressBar getProgressBar() {
         return progressBar;
     }
 }
