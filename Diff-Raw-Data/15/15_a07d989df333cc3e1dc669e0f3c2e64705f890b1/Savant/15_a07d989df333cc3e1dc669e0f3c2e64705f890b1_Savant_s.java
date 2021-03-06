 /*
  *    Copyright 2009-2010 University of Toronto
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package savant.view.swing;
 
 import info.clearthought.layout.TableLayout;
 import org.java.plugin.ObjectFactory;
 import org.java.plugin.PluginManager;
 import org.java.plugin.registry.Extension;
 import org.java.plugin.registry.ExtensionPoint;
 import org.java.plugin.registry.PluginDescriptor;
 import org.java.plugin.standard.StandardPluginLocation;
 import org.noos.xing.mydoggy.*;
 import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
 import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
 import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;
 import savant.analysis.BatchAnalysisForm;
 import savant.controller.BookmarkController;
 import savant.controller.FrameController;
 import savant.controller.RangeController;
 import savant.controller.ViewTrackController;
 import savant.controller.event.range.RangeChangedEvent;
 import savant.controller.event.range.RangeChangedListener;
 import savant.controller.event.rangeselection.RangeSelectionChangedEvent;
 import savant.controller.event.rangeselection.RangeSelectionChangedListener;
 import savant.format.DataFormatForm;
 import savant.model.Genome;
 import savant.plugin.AuxData;
 import savant.util.MiscUtils;
 import savant.util.Range;
 import savant.view.swing.util.ScreenShot;
 import savant.view.swing.sequence.SequenceViewTrack;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import savant.view.swing.PluginDialog;
 
 /**
  * Main application Window (Frame).
  * 
  * @author mfiume
  */
 public class Savant extends javax.swing.JFrame implements ComponentListener, RangeSelectionChangedListener, RangeChangedListener {
 
     private JPanel masterPlaceholderPanel;
     private JPanel framePlaceholderPanel;
     private JPanel frameContentPanel;
     private JPanel menuPanel;
 
     private static ToolWindowManager masterToolWindowManager;
     private static ToolWindowManager frameToolWindowManager;
 
     private static void addTrackFromFile(String selectedFileName) {
 
         // Some types of track actually create more than one track per frame, e.g. BAM
         
         List<ViewTrack> tracks = ViewTrack.create(selectedFileName);
 
         if (tracks != null  && tracks.size() > 0) {
             Frame frame = null;
             JPanel panel = createDockedFramePanel(MiscUtils.getFilenameFromPath(selectedFileName));
             if (!tracks.isEmpty()) {
                 frame = new Frame(panel, tracks);
             }
             FrameController.getInstance().addFrame(frame, panel);
         }
     }
 
     /** == [[ DOCKING ]] ==
      *  Components (such as frames, the Task Pane, etc.)
      *  can be docked to regions of the UI
      */
     private void initDocking() {
 
         masterPlaceholderPanel = new JPanel();
         masterPlaceholderPanel.setLayout(new TableLayout(new double[][]{{0, -1, 0}, {0, -1, 0}}));
 
         this.panel_main.setLayout(new BorderLayout());
         this.panel_main.add(masterPlaceholderPanel,BorderLayout.CENTER);
 
 
         initToolWindowManager();
     }
 
     protected void initToolWindowManager() {
         // Create a new instance of MyDoggyToolWindowManager passing the frame.
         MyDoggyToolWindowManager myDoggyToolWindowManager1 = new MyDoggyToolWindowManager();
         this.masterToolWindowManager = myDoggyToolWindowManager1;
 
         MyDoggyToolWindowManager myDoggyToolWindowManager2 = new MyDoggyToolWindowManager();
         this.frameToolWindowManager = myDoggyToolWindowManager2;
 
         initContentManager();
 
         // Add myDoggyToolWindowManager to the frame. MyDoggyToolWindowManager is an extension of a JPanel
         masterPlaceholderPanel.add(myDoggyToolWindowManager1, "1,1,");
         framePlaceholderPanel.add(myDoggyToolWindowManager2, "1,1,");
     }
 
     protected void initContentManager() {
         framePlaceholderPanel = new JPanel();
         framePlaceholderPanel.setBackground(Color.red);
         framePlaceholderPanel.setLayout(new TableLayout(new double[][]{{0, -1, 0}, {0, -1, 0}}));
 
         frameContentPanel = new JPanel();
         frameContentPanel.setBackground(BrowserDefaults.colorBrowseBackground);
 
         ContentManager contentManager1 = masterToolWindowManager.getContentManager();
         contentManager1.addContent("MasterKey",
                                   "Master",
                                   null,           // An icon
                                   framePlaceholderPanel);
 
         ContentManager contentManager2 = frameToolWindowManager.getContentManager();
         contentManager2.addContent("FrameKey",
                                   "Frames",
                                   null,           // An icon
                                   frameContentPanel);
 
         setupMasterContentManagerUI();
         setupFrameContentManagerUI();
      }
 
     protected void setupMasterContentManagerUI() {
         ContentManager contentManager = masterToolWindowManager.getContentManager();
         MultiSplitContentManagerUI contentManagerUI = new MyDoggyMultiSplitContentManagerUI();
         contentManager.setContentManagerUI(contentManagerUI);
 
         contentManagerUI.setShowAlwaysTab(false);
         contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.BOTTOM);
         contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener() {
             public boolean contentUIRemoving(ContentManagerUIEvent event) {
                 return true;
                 //return JOptionPane.showConfirmDialog(frame, "Are you sure?") == JOptionPane.OK_OPTION;
             }
 
             public void contentUIDetached(ContentManagerUIEvent event) {
                 //JOptionPane.showMessageDialog(frame, "Hello World!!!");
             }
         });
 
         TabbedContentUI contentUI = contentManagerUI.getContentUI(masterToolWindowManager.getContentManager().getContent(0));
 
         contentUI.setMinimizable(false);
         contentUI.setCloseable(false);
         contentUI.setDetachable(false);
         contentUI.setTransparentMode(false);
         contentUI.setTransparentRatio(0.7f);
         contentUI.setTransparentDelay(1000);
     }
 
     protected void setupFrameContentManagerUI() {
         
         ContentManager contentManager = frameToolWindowManager.getContentManager();
         MultiSplitContentManagerUI contentManagerUI = new MyDoggyMultiSplitContentManagerUI();
         contentManager.setContentManagerUI(contentManagerUI);
 
         contentManagerUI.setShowAlwaysTab(false);
         contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.BOTTOM);
         contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener() {
             public boolean contentUIRemoving(ContentManagerUIEvent event) {
                 return true;
                 //return JOptionPane.showConfirmDialog(frame, "Are you sure?") == JOptionPane.OK_OPTION;
             }
 
             public void contentUIDetached(ContentManagerUIEvent event) {
                 //JOptionPane.showMessageDialog(frame, "Hello World!!!");
             }
         });
 
         TabbedContentUI contentUI = contentManagerUI.getContentUI(frameToolWindowManager.getContentManager().getContent(0));
 
         contentUI.setMinimizable(false);
         contentUI.setCloseable(false);
         contentUI.setDetachable(false);
         contentUI.setTransparentMode(false);
         contentUI.setTransparentRatio(0.7f);
         contentUI.setTransparentDelay(1000);
     }
 
 
 
     static enum ViewMode {
         BROWSE, FORMAT
     };
     /** Minimum and maximum dimensions of the browser form */
     static int minimumFormWidth = 500;
     static int minimumFormHeight = 500;
 
     /** The loaded genome */
     private Genome loadedGenome;
     /** The log */
     private static JTextArea log;
 
     /**
      * Range Controls
      */
     /** Controls (buttons, text fields etc.) for chosing current viewable range */
     private List<JComponent> rangeControls;
     /** From and To textboxes */
     private JTextField textboxFrom, textboxTo;
     /** Click and drag control for range selection */
     private RangeSelectionPanel rangeSelector;
 
     /** Information & Analysis Tabbed Pane (for plugin use) */
     private JTabbedPane auxTabbedPane;
 
     private PluginManager pluginManager;
 
 
     /**
      * Info
      */
     private static DataSheet currentRangeDataSheet;
     private static BookmarkSheet favoriteSheet;
 
     private RangeController rangeController = RangeController.getInstance();
     private BookmarkController favoriteController = BookmarkController.getInstance();
 
     private static Savant instance = null;
 
     public static Savant getInstance() {
         if (instance == null) instance = new Savant();
         return instance;
     }
     
     /** Creates new form Savant */
     private Savant() {
         try {
             // Set System L&F
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             // handle exception
         }
 
         addComponentListener(this);
 
         initComponents();
         init();
 
         loadPlugins();
         initPlugins();
     }
 
 
     private void loadPlugins() {
 
         pluginManager = ObjectFactory.newInstance().createManager();
 
         File pluginsDir = new File("plugins");
         File[] plugins = pluginsDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().endsWith(".jar");
             }
         });
 
         try {
             PluginManager.PluginLocation[] locations = new PluginManager.PluginLocation[plugins.length];
 
             for (int i=0; i<plugins.length; i++) {
                 locations[i] = StandardPluginLocation.create(plugins[i]);
             }
 
             pluginManager.publishPlugins(locations);
         }
         catch (Exception e) {
             throw new RuntimeException(e); // TODO: fix this and handle properly
         }
     }
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         panel_top = new javax.swing.JPanel();
         panelExtendedRight = new javax.swing.JPanel();
         panelExtendedMiddle = new javax.swing.JPanel();
         panel_main = new javax.swing.JPanel();
         panel_bottom = new javax.swing.JPanel();
         menuBar_top = new javax.swing.JMenuBar();
         menu_file = new javax.swing.JMenu();
         menu_load = new javax.swing.JMenu();
         menuitem_genome = new javax.swing.JMenuItem();
         menuitem_track = new javax.swing.JMenuItem();
         menuItemFormat = new javax.swing.JMenuItem();
         submenu_download = new javax.swing.JMenu();
         menuitem_preformatted = new javax.swing.JMenuItem();
         menuitem_ucsc = new javax.swing.JMenuItem();
         menuitem_thousandgenomes = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JSeparator();
         menuitem_screen = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JSeparator();
         menuitem_exit = new javax.swing.JMenuItem();
         menu_edit = new javax.swing.JMenu();
         menuitem_undo = new javax.swing.JMenuItem();
         jMenuItem5 = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         menuItemAddToFaves = new javax.swing.JMenuItem();
         menu_view = new javax.swing.JMenu();
         menuItem_viewRangeControls = new javax.swing.JCheckBoxMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         menuItemPanLeft = new javax.swing.JMenuItem();
         menuItemPanRight = new javax.swing.JMenuItem();
         menuItemZoomIn = new javax.swing.JMenuItem();
         menuItemZoomOut = new javax.swing.JMenuItem();
         menu_plugins = new javax.swing.JMenu();
         menuitem_pluginmanager = new javax.swing.JMenuItem();
         menu_help = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setBackground(new java.awt.Color(204, 204, 204));
 
         panel_top.setMaximumSize(new java.awt.Dimension(1000, 50));
         panel_top.setMinimumSize(new java.awt.Dimension(0, 0));
         panel_top.setPreferredSize(new java.awt.Dimension(0, 50));
         panel_top.setLayout(new java.awt.BorderLayout());
 
         panelExtendedRight.setBackground(new java.awt.Color(153, 255, 0));
         panelExtendedRight.setMinimumSize(new java.awt.Dimension(10, 100));
         panelExtendedRight.setOpaque(false);
         panelExtendedRight.setPreferredSize(new java.awt.Dimension(10, 100));
 
         javax.swing.GroupLayout panelExtendedRightLayout = new javax.swing.GroupLayout(panelExtendedRight);
         panelExtendedRight.setLayout(panelExtendedRightLayout);
         panelExtendedRightLayout.setHorizontalGroup(
             panelExtendedRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 10, Short.MAX_VALUE)
         );
         panelExtendedRightLayout.setVerticalGroup(
             panelExtendedRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 50, Short.MAX_VALUE)
         );
 
         panel_top.add(panelExtendedRight, java.awt.BorderLayout.LINE_END);
 
         panelExtendedMiddle.setBackground(new java.awt.Color(204, 204, 204));
         panelExtendedMiddle.setMinimumSize(new java.awt.Dimension(0, 0));
         panelExtendedMiddle.setOpaque(false);
 
         javax.swing.GroupLayout panelExtendedMiddleLayout = new javax.swing.GroupLayout(panelExtendedMiddle);
         panelExtendedMiddle.setLayout(panelExtendedMiddleLayout);
         panelExtendedMiddleLayout.setHorizontalGroup(
             panelExtendedMiddleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 990, Short.MAX_VALUE)
         );
         panelExtendedMiddleLayout.setVerticalGroup(
             panelExtendedMiddleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 50, Short.MAX_VALUE)
         );
 
         panel_top.add(panelExtendedMiddle, java.awt.BorderLayout.CENTER);
 
         panel_main.setBackground(new java.awt.Color(153, 153, 153));
         panel_main.setMinimumSize(new java.awt.Dimension(500, 500));
         panel_main.setPreferredSize(new java.awt.Dimension(500, 300));
 
         javax.swing.GroupLayout panel_mainLayout = new javax.swing.GroupLayout(panel_main);
         panel_main.setLayout(panel_mainLayout);
         panel_mainLayout.setHorizontalGroup(
             panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 1000, Short.MAX_VALUE)
         );
         panel_mainLayout.setVerticalGroup(
             panel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 500, Short.MAX_VALUE)
         );
 
         panel_bottom.setMinimumSize(new java.awt.Dimension(520, 5));
         panel_bottom.setPreferredSize(new java.awt.Dimension(100, 5));
 
         javax.swing.GroupLayout panel_bottomLayout = new javax.swing.GroupLayout(panel_bottom);
         panel_bottom.setLayout(panel_bottomLayout);
         panel_bottomLayout.setHorizontalGroup(
             panel_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 1000, Short.MAX_VALUE)
         );
         panel_bottomLayout.setVerticalGroup(
             panel_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 10, Short.MAX_VALUE)
         );
 
         menu_file.setText("File");
 
         menu_load.setText("Load...");
 
         menuitem_genome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
         menuitem_genome.setText("Genome");
         menuitem_genome.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_genomeActionPerformed(evt);
             }
         });
         menu_load.add(menuitem_genome);
 
         menuitem_track.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
         menuitem_track.setText("Track");
         menuitem_track.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_trackActionPerformed(evt);
             }
         });
         menu_load.add(menuitem_track);
 
         menu_file.add(menu_load);
 
         menuItemFormat.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
         menuItemFormat.setText("Format");
         menuItemFormat.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemFormatActionPerformed(evt);
             }
         });
         menu_file.add(menuItemFormat);
 
         if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
         submenu_download.setText("Download");
 
             menuitem_preformatted.setText("Preformatted");
             menuitem_preformatted.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     menuitem_preformattedActionPerformed(evt);
                 }
             });
             submenu_download.add(menuitem_preformatted);
 
             menuitem_ucsc.setText("UCSC");
             menuitem_ucsc.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     menuitem_ucscActionPerformed(evt);
                 }
             });
             submenu_download.add(menuitem_ucsc);
 
             menuitem_thousandgenomes.setText("1000 Genomes");
             menuitem_thousandgenomes.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     menuitem_thousandgenomesActionPerformed(evt);
                 }
             });
             submenu_download.add(menuitem_thousandgenomes);
 
             menu_file.add(submenu_download);
             menu_file.add(jSeparator3);
         }
 
         menuitem_screen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         menuitem_screen.setText("Screenshot");
         menuitem_screen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_screenActionPerformed(evt);
             }
         });
         menu_file.add(menuitem_screen);
         menu_file.add(jSeparator4);
 
         menuitem_exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
         menuitem_exit.setText("Exit");
         menuitem_exit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_exitActionPerformed(evt);
             }
         });
         menu_file.add(menuitem_exit);
 
         menuBar_top.add(menu_file);
 
         menu_edit.setText("Edit");
 
         menuitem_undo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
         menuitem_undo.setText("Undo Range Change");
         menuitem_undo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_undoActionPerformed(evt);
             }
         });
         menu_edit.add(menuitem_undo);
 
         jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem5.setText("Redo Range Change");
         jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem5ActionPerformed(evt);
             }
         });
         menu_edit.add(jMenuItem5);
         menu_edit.add(jSeparator2);
 
         menuItemAddToFaves.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
         menuItemAddToFaves.setText("Bookmark");
         menuItemAddToFaves.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemAddToFavesActionPerformed(evt);
             }
         });
         menu_edit.add(menuItemAddToFaves);
 
         menuBar_top.add(menu_edit);
 
         menu_view.setText("View");
 
         menuItem_viewRangeControls.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         menuItem_viewRangeControls.setSelected(true);
         menuItem_viewRangeControls.setText("Range Control Menu");
         menuItem_viewRangeControls.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItem_viewRangeControlsMousePressed(evt);
             }
         });
         menu_view.add(menuItem_viewRangeControls);
         menu_view.add(jSeparator1);
 
         menuItemPanLeft.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.SHIFT_MASK));
         menuItemPanLeft.setText("Pan Left");
         menuItemPanLeft.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemPanLeftActionPerformed(evt);
             }
         });
         menu_view.add(menuItemPanLeft);
 
         menuItemPanRight.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.SHIFT_MASK));
         menuItemPanRight.setText("Pan Right");
         menuItemPanRight.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemPanRightActionPerformed(evt);
             }
         });
         menu_view.add(menuItemPanRight);
 
         menuItemZoomIn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.SHIFT_MASK));
         menuItemZoomIn.setText("Zoom In");
         menuItemZoomIn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemZoomInActionPerformed(evt);
             }
         });
         menu_view.add(menuItemZoomIn);
 
         menuItemZoomOut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.SHIFT_MASK));
         menuItemZoomOut.setText("Zoom Out");
         menuItemZoomOut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuItemZoomOutActionPerformed(evt);
             }
         });
         menu_view.add(menuItemZoomOut);
 
         menuBar_top.add(menu_view);
 
         menu_plugins.setText("Plugins");
 
         menuitem_pluginmanager.setText("Plugin Manager");
         menuitem_pluginmanager.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuitem_pluginmanagerActionPerformed(evt);
             }
         });
         menu_plugins.add(menuitem_pluginmanager);
 
         menuBar_top.add(menu_plugins);
 
         menu_help.setText("Help");
 
         if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
             jMenuItem1.setText("Website");
             jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     jMenuItem1ActionPerformed(evt);
                 }
             });
             menu_help.add(jMenuItem1);
         }
 
         menuBar_top.add(menu_help);
 
         setJMenuBar(menuBar_top);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(panel_bottom, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
             .addComponent(panel_top, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
             .addComponent(panel_main, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(panel_top, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(panel_main, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(panel_bottom, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Shift the currentViewableRange all the way to the right
      * @param evt The mouse event which triggers the function
      */
     /**
      * Shift the currentViewableRange to the right
      * @param evt The mouse event which triggers the function
      */
     /**
      * Shift the currentViewableRange to the left
      * @param evt The mouse event which triggers the function
      */
     /**
      * Shift the currentViewableRange all the way to the left
      * @param evt The mouse event which triggers the function
      */
     private void menuItem_viewRangeControlsMousePressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItem_viewRangeControlsMousePressed
         this.panel_top.setVisible(!this.panel_top.isVisible());
     }//GEN-LAST:event_menuItem_viewRangeControlsMousePressed
 
     private void menuItemZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemZoomInActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.zoomIn();
     }//GEN-LAST:event_menuItemZoomInActionPerformed
 
     private void menuItemZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemZoomOutActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.zoomOut();
     }//GEN-LAST:event_menuItemZoomOutActionPerformed
 
     private void menuItemPanLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemPanLeftActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.shiftRangeLeft();
     }//GEN-LAST:event_menuItemPanLeftActionPerformed
 
     private void menuItemPanRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemPanRightActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.shiftRangeRight();
     }//GEN-LAST:event_menuItemPanRightActionPerformed
 
     private void menuitem_undoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_undoActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.undoRangeChange();
     }//GEN-LAST:event_menuitem_undoActionPerformed
 
     private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
         RangeController rc = RangeController.getInstance();
         rc.redoRangeChange();
     }//GEN-LAST:event_jMenuItem5ActionPerformed
 
     private void menuItemAddToFavesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAddToFavesActionPerformed
         BookmarkController fc = BookmarkController.getInstance();
         fc.addCurrentRangeToBookmarks();
     }//GEN-LAST:event_menuItemAddToFavesActionPerformed
 
     private void menuItemFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemFormatActionPerformed
         Savant.log("Showing format form...");
         DataFormatForm ff = new DataFormatForm();
     }//GEN-LAST:event_menuItemFormatActionPerformed
 
     private void menuitem_exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_exitActionPerformed
         System.exit(0);
     }//GEN-LAST:event_menuitem_exitActionPerformed
 
     private void menuitem_genomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_genomeActionPerformed
         this.showOpenGenomeDialog();
     }//GEN-LAST:event_menuitem_genomeActionPerformed
 
     private void menuitem_trackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_trackActionPerformed
         this.showOpenTracksDialog();
     }//GEN-LAST:event_menuitem_trackActionPerformed
 
     private void menuitem_screenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_screenActionPerformed
         ScreenShot.takeAndSave();
     }//GEN-LAST:event_menuitem_screenActionPerformed
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         try {
             java.awt.Desktop.getDesktop().browse(java.net.URI.create(BrowserDefaults.url));
         } catch (IOException ex) {
             Logger.getLogger(Savant.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void menuitem_preformattedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_preformattedActionPerformed
         try {
             java.awt.Desktop.getDesktop().browse(java.net.URI.create(BrowserDefaults.url_preformatteddata));
         } catch (IOException ex) {
             Logger.getLogger(Savant.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_menuitem_preformattedActionPerformed
 
     private void menuitem_ucscActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_ucscActionPerformed
         try {
             java.awt.Desktop.getDesktop().browse(java.net.URI.create(BrowserDefaults.url_ucsctablebrowser));
         } catch (IOException ex) {
             Logger.getLogger(Savant.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_menuitem_ucscActionPerformed
 
     private void menuitem_thousandgenomesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_thousandgenomesActionPerformed
         try {
             java.awt.Desktop.getDesktop().browse(java.net.URI.create(BrowserDefaults.url_thousandgenomes));
         } catch (IOException ex) {
             Logger.getLogger(Savant.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_menuitem_thousandgenomesActionPerformed
 
     private void menuitem_pluginmanagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitem_pluginmanagerActionPerformed
         PluginDialog pd = new PluginDialog();
         pd.setVisible(true);
     }//GEN-LAST:event_menuitem_pluginmanagerActionPerformed
 
     /**
      * Starts an instance of the Savant Browser
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 Savant.getInstance().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem5;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JMenuBar menuBar_top;
     private javax.swing.JMenuItem menuItemAddToFaves;
     private javax.swing.JMenuItem menuItemFormat;
     private javax.swing.JMenuItem menuItemPanLeft;
     private javax.swing.JMenuItem menuItemPanRight;
     private javax.swing.JMenuItem menuItemZoomIn;
     private javax.swing.JMenuItem menuItemZoomOut;
     private javax.swing.JCheckBoxMenuItem menuItem_viewRangeControls;
     private javax.swing.JMenu menu_edit;
     private javax.swing.JMenu menu_file;
     private javax.swing.JMenu menu_help;
     private javax.swing.JMenu menu_load;
     private javax.swing.JMenu menu_plugins;
     private javax.swing.JMenu menu_view;
     private javax.swing.JMenuItem menuitem_exit;
     private javax.swing.JMenuItem menuitem_genome;
     private javax.swing.JMenuItem menuitem_pluginmanager;
     private javax.swing.JMenuItem menuitem_preformatted;
     private javax.swing.JMenuItem menuitem_screen;
     private javax.swing.JMenuItem menuitem_thousandgenomes;
     private javax.swing.JMenuItem menuitem_track;
     private javax.swing.JMenuItem menuitem_ucsc;
     private javax.swing.JMenuItem menuitem_undo;
     private javax.swing.JPanel panelExtendedMiddle;
     private javax.swing.JPanel panelExtendedRight;
     private javax.swing.JPanel panel_bottom;
     private javax.swing.JPanel panel_main;
     private javax.swing.JPanel panel_top;
     private javax.swing.JMenu submenu_download;
     // End of variables declaration//GEN-END:variables
 
         /**
      * Initialize the Browser
      */
     void init() {
 
         rangeController.addRangeChangedListener(this);
 
         initGUIFrame();
         initMenu();
         initDocking();
         initPanelsAndDocking();
     }
 
     private void initPanelsAndDocking() {
         initDocking();
         initAuxilliaryPanels();
     }
 
     private void initAuxilliaryPanels() {
         initAuxPanel1();
         initAuxPanel2();
     }
 
     private PluginManager getPluginManager() {
         return pluginManager;
     }
 
     private void initPlugins() {
         try{
             // hide the plugin manager menu item
             menu_plugins.setVisible(false);
 
             // init the AuxData plugins
             PluginDescriptor core = pluginManager.getRegistry().getPluginDescriptor("savant.core");
             ExtensionPoint point = pluginManager.getRegistry().getExtensionPoint(core.getId(), "AuxData");
 
             for (Iterator it = point.getConnectedExtensions().iterator(); it.hasNext();) {
                 Extension ext = (Extension) it.next();
                 PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
                 pluginManager.activatePlugin(descr.getId());
                 ClassLoader classLoader = pluginManager.getPluginClassLoader(descr);
                 Class pluginCls = classLoader.loadClass(ext.getParameter("class").valueAsString());
                 AuxData auxData = (AuxData) pluginCls.newInstance();
                auxData.init(getAuxTabbedPane());
             }
 

//            Iterator it = getPluginManager().getRegistry().getPluginDescriptors().iterator();
//            while (it.hasNext()) {
//                PluginDescriptor p = (PluginDescriptor) it.next();
//                if (p.getExtensionPoint("AuxData") != null) {
//
//                    AuxData auxData = (AuxData) getPluginManager().getPlugin(p.getId());
//                    auxData.init(getAuxTabbedPane());
//                }
//            }
         }
         catch (Exception e) {
             e.printStackTrace(); // TODO: handle properly
         }
 
     }
     private void initAuxPanel1() {
 
         JPanel auxPanel = this.createDockedPanel("Information & Analysis", ToolWindowAnchor.BOTTOM);
 
         auxTabbedPane = new JTabbedPane();
 
         //initDataTab(auxTabbedPane);
         initBatchAnalyzeTab(auxTabbedPane);
         initLogTab(auxTabbedPane);
 
         auxTabbedPane.setSelectedIndex(0);
 
         auxPanel.setLayout(new BorderLayout());
         auxPanel.add(auxTabbedPane, BorderLayout.CENTER);
     }
 
     private void initAuxPanel2() {
 
         JPanel p = this.createDockedPanel("Annotation");
 
         JTabbedPane jtp = new JTabbedPane();
         initBookmarksTab(jtp);
 
         p.setLayout(new BorderLayout());
         p.add(jtp, BorderLayout.CENTER);
     }
 
     /**
      * Provide access to the tabbed pane in the bottom auxilliary panel
      * @return the auxilliary tabbed pane
      */
     public JTabbedPane getAuxTabbedPane() {
          return auxTabbedPane;
      }
     /**
      * Set up frame
      */
     void initGUIFrame() {
         this.setTitle("Savant Genome Browser");
         this.setName("Savant Genome Browser");
         this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
     }
 
     private void initMenu() {
         initBrowseMenu();
     }
 
     /**
      * Set up frame
      */
     void initFrame() {
         this.setTitle("Savant Genome Browser");
         this.setName("Savant Genome Browser");
         this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
         this.panel_main.setBackground(Color.lightGray);
     }
 
     /**
      * == [ RESIZE FORM ] ==
      *  Java does not enforce (proactively) a minimum
      *  form size. Here, we resize the form to a minimum
      *  size if the user has resized it to something
      *  smaller.
      */
     /**
      * Resize the form to the minimum size if the
      * user has resized it to something smaller.
      * @param e The resize event
      */
     public void componentResized(ComponentEvent e) {
         int width = getWidth();
         int height = getHeight();
         //we check if either the width
         //or the height are below minimum
         boolean resize = false;
         if (width < minimumFormWidth) {
             resize = true;
             width = minimumFormWidth;
         }
         if (height < minimumFormHeight) {
             resize = true;
             height = minimumFormHeight;
         }
         if (resize) {
             setSize(width, height);
         }
     }
 
     public void componentMoved(ComponentEvent e) {
     }
 
     public void componentShown(ComponentEvent e) {
     }
 
     public void componentHidden(ComponentEvent e) {
     }
 
     private void addEtchedBorder(JPanel panel) {
         Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
         panel.setBorder(loweredetched);
     }
 
     private void initBrowseMenu() {
 
         this.menuPanel = new JPanel();
         this.panelExtendedMiddle.setLayout(new BorderLayout());
         this.panelExtendedMiddle.add(menuPanel);
         JPanel p = this.menuPanel;
 
         p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
 
 
         Dimension buttonDimension = new Dimension(45,23);
 
         p.add(getRigidPadding());
         JButton genomeButton = addButton(p, "Genome");
         genomeButton.setToolTipText("Load a genome");
 
         // .createRoundedBalloonTip(Component attachedComponent, Alignment alignment, Color borderColor, Color fillColor, int borderWidth, int horizontalOffset, int verticalOffset, int arcWidth, int arcHeight, boolean useCloseButton)
 
         genomeButton.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 showOpenGenomeDialog();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         JButton trackButton = addButton(p, "  Track  ");
         trackButton.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 showOpenTracksDialog();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         p.add(Box.createGlue());
 
         p.add(getRigidPadding());
 
         rangeSelector = new RangeSelectionPanel();
         rangeSelector.setPreferredSize(new Dimension(10000, 23));
         rangeSelector.setMaximumSize(new Dimension(10000, 23));
         this.addEtchedBorder(rangeSelector);
         rangeController.addRangeChangedListener(rangeSelector);
         rangeSelector.addRangeChangedListener(this);
         //mt.setVisible(true);
         //mt.setBackground(Color.red);
         p.add(rangeSelector);
 
         p.add(getRigidPadding());
         JButton zoomIn = addButton(p, "+");
         zoomIn.setPreferredSize(buttonDimension);
         zoomIn.setMinimumSize(buttonDimension);
         zoomIn.setMaximumSize(buttonDimension);
         zoomIn.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.zoomIn();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         JButton zoomOut = addButton(p, "-");
         zoomOut.setPreferredSize(buttonDimension);
         zoomOut.setMinimumSize(buttonDimension);
         zoomOut.setMaximumSize(buttonDimension);
         zoomOut.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.zoomOut();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         p.add(getRigidPadding());
 
         JButton shiftFarLeft = addButton(p, "|<");
 
         shiftFarLeft.setPreferredSize(buttonDimension);
         shiftFarLeft.setMinimumSize(buttonDimension);
         shiftFarLeft.setMaximumSize(buttonDimension);
 
         shiftFarLeft.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.shiftRangeFarLeft();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         JButton shiftLeft = addButton(p, "<");
         shiftLeft.setPreferredSize(buttonDimension);
         shiftLeft.setMinimumSize(buttonDimension);
         shiftLeft.setMaximumSize(buttonDimension);
         shiftLeft.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.shiftRangeLeft();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         JButton shiftRight = addButton(p, ">");
         shiftRight.setPreferredSize(buttonDimension);
         shiftRight.setMinimumSize(buttonDimension);
         shiftRight.setMaximumSize(buttonDimension);
         shiftRight.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.shiftRangeRight();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         JButton shiftFarRight = addButton(p, ">|");
         shiftFarRight.setPreferredSize(buttonDimension);
         shiftFarRight.setMinimumSize(buttonDimension);
         shiftFarRight.setMaximumSize(buttonDimension);
         shiftFarRight.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 rangeController.shiftRangeFarRight();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
 
         p.add(getRigidPadding());
         int tfwidth = 100;
         int tfheight = 22;
         textboxFrom = addTextField(p, "from");
         textboxFrom.setHorizontalAlignment(JTextField.CENTER);
         textboxFrom.setPreferredSize(new Dimension(tfwidth, tfheight));
         textboxFrom.setMaximumSize(new Dimension(tfwidth, tfheight));
         textboxFrom.setMinimumSize(new Dimension(tfwidth, tfheight));
 
         textboxFrom.addKeyListener(new java.awt.event.KeyAdapter() {
 
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 RangeTextBoxKeypressed(evt);
             }
         });
 
         textboxTo = addTextField(p, "to");
         textboxTo.setHorizontalAlignment(JTextField.CENTER);
         textboxTo.setPreferredSize(new Dimension(tfwidth, tfheight));
         textboxTo.setMaximumSize(new Dimension(tfwidth, tfheight));
         textboxTo.setMinimumSize(new Dimension(tfwidth, tfheight));
 
         textboxTo.addKeyListener(new java.awt.event.KeyAdapter() {
 
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 RangeTextBoxKeypressed(evt);
             }
         });
 
         rangeControls = new ArrayList<JComponent>();
         rangeControls.add(textboxFrom);
         rangeControls.add(textboxTo);
         rangeControls.add(shiftFarLeft);
         rangeControls.add(shiftFarRight);
         rangeControls.add(shiftLeft);
         rangeControls.add(shiftRight);
         rangeControls.add(zoomIn);
         rangeControls.add(zoomOut);
         rangeControls.add(rangeSelector);
 
         hideRangeControls();
     }
 
 
     private JTextField addTextField(JPanel p, String msg) {
         JTextField f = new JTextField(msg);
         p.add(f);
         return f;
     }
 
 
     private void RangeTextBoxKeypressed(java.awt.event.KeyEvent evt) {
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             this.setRangeFromTextBoxes();
         }
     }
 
     private void hideRangeControls() {
         changeVisibility(rangeControls, false);
     }
 
     private void showRangeControls() {
         changeVisibility(rangeControls, true);
     }
 
     private static void changeVisibility(List<JComponent> components, boolean isVisible) {
         for (JComponent j : components) {
             j.setVisible(isVisible);
         }
     }
 
     private void initBookmarksTab(JTabbedPane jtp) {
         JPanel tablePanel = createTabPanel(jtp, "Bookmarks");
         favoriteSheet = new BookmarkSheet(this, tablePanel);
         favoriteController.addFavoritesChangedListener(favoriteSheet);
     }
 
     private void initDataTab(JTabbedPane jtp) {
         JPanel tablePanel = createTabPanel(jtp, "Table View");
         currentRangeDataSheet = new DataSheet(this, tablePanel);
         rangeController.addRangeChangedListener(currentRangeDataSheet);
         ViewTrackController.getInstance().addTracksChangedListener(currentRangeDataSheet);
     }
 
     private void initBatchAnalyzeTab(JTabbedPane jtp) {
 
         JPanel tablePanel = createTabPanel(jtp, "Batch Run");
 
         JButton addBatchAnalysisButton = new JButton("Add Batch Analysis");
         addBatchAnalysisButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 BatchAnalysisForm baf = new BatchAnalysisForm();
                 baf.setVisible(true);
                 baf.setAlwaysOnTop(true);
             }
 
         });
 
         tablePanel.add(addBatchAnalysisButton);
 
     }
 
     private void initLogTab(JTabbedPane jtp) {
         JPanel pan = createTabPanel(jtp, "Log");
         this.initLog(pan);
     }
 
     private JPanel createTabPanel(JTabbedPane jtp, String name) {
         JPanel pan = new JPanel();
         pan.setLayout(new BorderLayout());
         pan.setBackground(BrowserDefaults.colorTabBackground);
         jtp.addTab(name, pan);
         return pan;
     }
 
     private Component getRigidPadding() {
         return Box.createRigidArea(new Dimension(BrowserDefaults.padding, BrowserDefaults.padding));
     }
 
     private JButton addButton(JPanel p, String label) {
         JButton b = new JButton(label);
         p.add(b);
         return b;
     }
 
     /**
      * [[ DIALOGS ]]
      *  Dialogs are forms which prompt the user
      *  to perform some action (e.g. open / save a file)
      */
     /**
      * Open Genome Dialog
      *  Prompt the user to open a genome file.
      *  Expects a Binary Fasta file (created using the
      *  data formatter included in the distribution)
      */
     void showOpenGenomeDialog() {
 
         // create a frame and place the dialog in it
         JFrame jf = new JFrame();
         FileDialog fd = new FileDialog(jf, "Load Genome", FileDialog.LOAD);
         /*
         fd.setFilenameFilter(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(DataFormatter.defaultExtension);
             }
         });
          */
         fd.setVisible(true);
         jf.setAlwaysOnTop(true);
 
         // get the path (null if none selected)
         String selectedFileName = fd.getFile();
 
         // set the genome
         if (selectedFileName != null) {
             selectedFileName = fd.getDirectory() + selectedFileName;
             setGenome(selectedFileName);
         }
     }
 
     /**
      * Open Tracks Dialog
      *  Prompt the user to open track file(s).
      *  Expects a Binary formatted file (created using the
      *  data formatter included in the distribution)
      */
     void showOpenTracksDialog() {
 
         if (!this.isGenomeLoaded()) {
             JOptionPane.showMessageDialog(this, "Load a genome first.");
             return;
         }
         // create a frame and place the dialog in it
         JFrame jf = new JFrame();
         FileDialog fd = new FileDialog(jf, "Open Tracks", FileDialog.LOAD);
         fd.setVisible(true);
         jf.setAlwaysOnTop(true);
 
         // get the path (null if none selected)
         String selectedFileName = fd.getFile();
 
         // set the track
         if (selectedFileName != null) {
             selectedFileName = fd.getDirectory() + selectedFileName;
             try {
                 addTrackFromFile(selectedFileName);
             } catch (Exception e) {
                 promptUserToFormatFile(selectedFileName);
             }
         }
     }
 
     private JPanel createDockedPanel(String name) {
         return createDockedPanel(name, ToolWindowAnchor.RIGHT);
     }
 
     private JPanel createDockedPanel(String name, ToolWindowAnchor where) {
         JPanel p = new JPanel();
         p.setLayout(new BorderLayout());
         masterToolWindowManager.registerToolWindow(name,    // Id
                                              "",            // Title
                                              null,          // Icon
                                              p,             // Component
                                              where);       // Anchor
         ToolWindow tw = masterToolWindowManager.getToolWindow(name);
         tw.setIndex(-1);
         SlidingTypeDescriptor slidingTypeDescriptor = (SlidingTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.SLIDING);
         slidingTypeDescriptor.setEnabled(false);
         tw.setAvailable(true);
         tw.setActive(false);
         return p;
     }
 
     private static JPanel createDockedFramePanel(String name) {
 
         System.out.println("CREATING DOCKED PANEL: " + name);
 
         JPanel p = new JPanel();
         p.setLayout(new BorderLayout());
         frameToolWindowManager.registerToolWindow(name,                 // Id
                                              "",                        // Title
                                              null,                      // Icon
                                              p,                         // Component
                                              ToolWindowAnchor.TOP);     // Anchor
         ToolWindow tw = frameToolWindowManager.getToolWindow(name);
         tw.setIndex(-1);
         SlidingTypeDescriptor slidingTypeDescriptor = (SlidingTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.SLIDING);
         slidingTypeDescriptor.setEnabled(false);
         //tw.setAvailable(true);
 
         AggregationPosition ap = AggregationPosition.BOTTOM;
         if (FrameController.getInstance().getFrames().size() == 0) {
             ap = AggregationPosition.TOP;
         }
 
         tw.aggregate(ap);
         tw.setActive(true);
 
         return p;
     }
 
     /** [[EVENTS]]**/
         public void rangeSelectionChangeReceived(RangeSelectionChangedEvent event) {
         rangeController.setRange(event.range());
     }
 
     public void rangeChangeReceived(RangeChangedEvent event) {
         // adjust descriptions
         setRangeDescription(event.range());
 
         // adjust range controls
         setRangeSelectorFromRange();
 
         // draw all frames
         FrameController fc = FrameController.getInstance();
         fc.drawFrames();
     }
 
     /** [[ GETTERS AND SETTERS ]] */
 
 
     //public List<TrackDocument> getTrackDocuments() { return this.frames; }
     //public DockPanel getDockPanel() { return this.DOCKPANEL; }
     /**
      * Get the loaded genome.
      * @return The loaded genome
      */
     public Genome getGenome() {
         return this.loadedGenome;
     }
 
     /**
      * Set the genome at specified path.
      * @param filename The name of the genome file containing file to be set
      */
     private void setGenome(String filename) {
         try {
             Genome g = new Genome(filename);
             setGenome(filename, g);
         } catch (FileNotFoundException ex) {
         } catch (Exception ex) {
             promptUserToFormatFile(filename);
         /*
          } catch (Exception e) {
             JFrame frame = new JFrame();
             e.printStackTrace();
             JOptionPane.showMessageDialog(frame, "Error: " + e.getLocalizedMessage());
             frame.setVisible(true);
          * 
          */
         }
     }
 
     public static void promptUserToFormatFile(String fileName) {
         String message = "This file does not appear to be formatted. Format now?";
         String title = "Unrecognized file";
         // display the JOptionPane showConfirmDialog
         int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
         if (reply == JOptionPane.YES_OPTION)
         {
            DataFormatForm dff = new DataFormatForm(fileName);
 
            if (dff.didSuccessfullyFormat()) {
                String outfilepath = dff.getOutputFilePath();
                addTrackFromFile(outfilepath);
            }
         }
     }
 
     /**
      * Set the genome.
      * @param genome The genome to set
      */
     private void setGenome(String filename, Genome genome) {
 
         boolean someGenomeSetAlready = isGenomeLoaded();
 
         showRangeControls();
 
         this.loadedGenome = genome;
         rangeController.setMaxRange(new Range(1, genome.getLength()));
 
         rangeSelector.setMaximum(genome.getLength());
 
         if (genome.isSequenceSet()) {
             try {
                 JPanel panel = createDockedFramePanel(MiscUtils.getFilenameFromPath(filename));
                 List<ViewTrack> tracks = new ArrayList<ViewTrack>();
                 tracks.add(new SequenceViewTrack(genome.getName(), genome));
                 Frame frame = new Frame(panel, tracks);
                 FrameController.getInstance().addFrame(frame, panel);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(Savant.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         if (!someGenomeSetAlready) {
             rangeController.setRange(1, Math.min(1000,genome.getLength()));
         }
 
         this.showRangeControls();
     }
 
     /**
      * Set range description.
      *  - Change the from and to textboxes.
      *  - TODO: set the range length somewhere
      * @param range
      */
     void setRangeDescription(Range range) {
         textboxFrom.setText(MiscUtils.intToString(range.getFrom()));
         textboxTo.setText(MiscUtils.intToString(range.getTo()));
         //label_rangeLength.setText(MiscUtils.intToString(getRange().getLength()));
     }
 
     /**
      * Set the current range from the rangeSelector.
      */
     void setRangeFromRangeSelection() {
         int minRange = rangeSelector.getLowerPosition();
         int maxRange = rangeSelector.getUpperPosition();
 
         rangeController.setRange(minRange, maxRange);
     }
 
     /**
      * Set the current range from the Zoom track bar.
      */
     void setRangeFromTextBoxes() {
 
         int from = MiscUtils.stringToInt(MiscUtils.removeChar(textboxFrom.getText(), ','));
         int to = MiscUtils.stringToInt(MiscUtils.removeChar(textboxTo.getText(), ','));
 
         if (from <= 0) {
             JOptionPane.showMessageDialog(this, "Invalid start value.");
             textboxFrom.requestFocus();
             return;
         }
 
         if (to <= 0) {
             JOptionPane.showMessageDialog(this, "Invalid end value.");
             textboxTo.requestFocus();
             return;
         }
 
         if (from > to) {
             //MessageBox.Show("INVALID RANGE");
             JOptionPane.showMessageDialog(this, "Invalid range.");
             textboxTo.requestFocus();
             return;
         }
 
         rangeController.setRange(from, to);
     }
 
     /**
      * Set the range selection upper and lower values
      * from the current range.
      */
     void setRangeSelectorFromRange() {
         rangeSelector.setRange(rangeController.getRangeStart(), rangeController.getRangeEnd());
     }
 
 
     /** == [[ DOCKING ]] ==
      *  Components (such as frames, the Task Pane, etc.)
      *  can be docked to regions of the UI
      */
 
      /**
       * Start the log
      */
     private void initLog(JPanel pan) {
         if (log != null) {
             JOptionPane.showMessageDialog(this, "Log already started");
             return;
         }
 
         log = new JTextArea();
         log.setFont(new Font(BrowserDefaults.fontName, Font.PLAIN, 18));
         JScrollPane jsp = new JScrollPane(log);
         pan.add(jsp);
 
         log(log, "LOG STARTED");
     }
 
     /**
      * Stop the log
      */
     private void killLog() {
         log = null;
     }
 
     /**
      * Update the log
      */
     private void updateLog() {
         if (log == null) {
             return;
         }
         log(log, "Range change to: " + rangeController.getRange());
         // TODO: add track information
     }
 
     /**
      * Get a string formatted for the log (with a new line).
      * @param s The message to format
      * @return A string based on the given message to be logged
      */
     private static String logMessage(String s) {
         return "[" + MiscUtils.now() + "]\t" + s + "\n";
     }
 
     /**
      * Get a string formatted for the log (without a new line).
      * @param s The message to format
      * @return A string based on the given message to be logged
      */
     private static String logMessageN(String s) {
         return "[" + MiscUtils.now() + "]\t" + s;
     }
 
     /**
      * log a message on the given text area
      * @param rtb The text area on which to post the message
      * @param msg The message to post
      */
     public static void log(JTextArea rtb, String msg) {
         rtb.append(logMessage(msg));
         rtb.setCaretPosition(rtb.getText().length());
     }
 
     /**
      * log a message on the default log text area
      * @param msg The message to post
      */
     public static void log(String msg) {
         log.append(logMessage(msg));
         log.setCaretPosition(log.getText().length());
     }
 
     /**
      * Get whether or not a genome has been loaded.
      * @return True iff a genome has been loaded
      */
     public boolean isGenomeLoaded() {
         return getGenome() != null;
     }
 }
 
