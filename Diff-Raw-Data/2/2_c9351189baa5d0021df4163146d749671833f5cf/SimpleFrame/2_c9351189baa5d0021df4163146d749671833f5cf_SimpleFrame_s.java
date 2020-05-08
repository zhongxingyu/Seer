 /*
  * Signal Visualization Tools for Make Sense Platform
  * Copyright (C) 2012 Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package org.makesense.sigvis;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.KeyStroke;
 import javax.swing.SwingWorker;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.mina.util.ConcurrentHashSet;
 import org.makesense.sigvis.DataCache2.ValueType;
 import org.makesense.sigvis.panels.AmbientCloud;
 import org.makesense.sigvis.panels.BarChart;
 import org.makesense.sigvis.panels.DisplayPanel;
 import org.makesense.sigvis.panels.HeatStripes;
 import org.makesense.sigvis.panels.IntersectionLineMap;
 import org.makesense.sigvis.panels.LineChart;
 import org.makesense.sigvis.panels.RssiStDvLineChart;
 import org.makesense.sigvis.panels.SignalLineMap;
 import org.makesense.sigvis.panels.SignalToDistanceMap;
 import org.makesense.sigvis.panels.VoronoiHeatMap;
 import org.makesense.sigvis.panels.VoronoiRSSIQualityMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SimpleFrame extends JFrame implements ActionListener,
     ChangeListener, DataCache2Listener, WindowListener {
   private static final Logger log = LoggerFactory.getLogger(SimpleFrame.class);
 
   protected int desiredFps = 10;
 
   private static final File FILE_NONE = new File("");
 
   protected FilteringDataCache cache;
 
   protected ConnectionHandler connHandler;
 
   protected BufferedImage receiverIcon = null;
 
   protected BufferedImage transmitterIcon = null;
 
   protected final String initialTitle;
   protected String titlePrefix = "";
   protected String panelTitle = "";
 
   protected int displayedHistory = 60000;
 
   protected long staleDataAge = 10000l;
 
   protected JSlider timeOffsetSlider;
 
   protected int currentTimeOffset = 0;
 
   private final ConnectionOptionsPanel connectionPanel = new ConnectionOptionsPanel();
 
   private static final Set<SimpleFrame> allFrames = new ConcurrentHashSet<SimpleFrame>();
 
   public SimpleFrame(String initialTitle, FilteringDataCache cache) {
     super();
     allFrames.add(this);
     this.initialTitle = initialTitle;
 
     this.cache = cache;
     this.connHandler = this.cache.getHandler();
     this.configureTitlePrefix(initialTitle);
     this.cache.addListener(this);
 
     this.receiverIcon = ImageResources.IMG_RECEIVER;
     this.transmitterIcon = ImageResources.IMG_TRANSMITTER;
 
     this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
     FileFilter filter = new CacheFileFilter();
     this.fileChooser.addChoosableFileFilter(filter);
     this.fileChooser.setFileFilter(filter);
 
   }
 
   protected void configureTitlePrefix(String suffix) {
     this.titlePrefix = (this.cache.isClone ? ("["
         + SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT,
             SimpleDateFormat.MEDIUM).format(this.cache.getCreationTs()) + "] ")
         : "")
         + (suffix == null ? "" : suffix);
   }
 
   protected void setTitle() {
     this.setTitle(this.titlePrefix + " " + this.panelTitle);
   }
 
   public void configureDisplay() {
 
     this.setTitle();
     this.addWindowListener(this);
     this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     this.setPreferredSize(new Dimension(800, 600));
     this.setLayout(new BorderLayout());
     int maxCacheSeconds = (int) (this.cache.getMaxCacheAge() / 1000);
     this.timeOffsetSlider = new JSlider(0, maxCacheSeconds, maxCacheSeconds);
     this.timeOffsetSlider.setMajorTickSpacing(maxCacheSeconds / 10);
     this.timeOffsetSlider.setMinorTickSpacing(maxCacheSeconds / 20);
     this.timeOffsetSlider.setPaintTicks(true);
     Dictionary<Integer, JLabel> sliderLabels = new Hashtable<Integer, JLabel>();
 
     for (int i = 0; i <= maxCacheSeconds; i += maxCacheSeconds / 10) {
       int minutes = (maxCacheSeconds - i) / 60;
       sliderLabels.put(Integer.valueOf(i), new JLabel(Integer.toString(minutes)
           + " min."));
     }
 
     this.timeOffsetSlider.setLabelTable(sliderLabels);
     this.timeOffsetSlider.setPaintLabels(true);
     this.timeOffsetSlider.addChangeListener(this);
     this.timeOffsetSlider.setBorder(new TitledBorder("Minutes into the Past"));
 
     this.add(this.timeOffsetSlider, BorderLayout.SOUTH);
 
     this.buildMenu();
 
     this.setJMenuBar(this.menu);
     
     // Default to ambient variance chart
     AmbientCloud newChart = new AmbientCloud(this.cache);
     this.configureGfx(newChart);
     this.mainPanel = newChart;
     this.displayPanel = newChart;
     this.displayPanel.setMinValue(-100f);
     this.displayPanel.setMaxValue(-40f);
     this.displayPanel.setTimeOffset(this.currentTimeOffset);
    this.displayPanel.setMaxAge(this.displayedHistory);
     
     this.titleChartType = "Ambient Variance";
     this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
     this.setTitle();
     this.add(newChart, BorderLayout.CENTER);
     this.validate();
 
     this.pack();
 
     this.setVisible(true);
 
     this.startUpdates();
   }
 
   protected Timer updateTimer = new Timer();
 
   protected TimerTask updateTask = null;
 
   protected String currentDeviceId = null;
 
   protected boolean isTransmitter = false;
 
   private String titleChartType = "";
   private String titleDeviceName = "";
 
   /*
    * mainPanel and displayPanel should point to the same object, but are
    * convenience pointers for accessing it as a JPanel class or a DisplayPanel
    * interface.
    */
   protected JComponent mainPanel = null;
 
   protected DisplayPanel displayPanel = null;
 
   protected JMenuBar menu = new JMenuBar();
 
   protected JMenu fileMenu = new JMenu("File");
 
   protected JMenu windowMenu = new JMenu("Windows");
 
   protected JMenuItem windowNew = new JMenuItem("New Window");
 
   protected JMenuItem openConnection = new JMenuItem("Connect...");
 
   protected JMenuItem closeWindow = new JMenuItem("Close Window");
 
   protected JMenuItem quitApp = new JMenuItem("Quit " + SignalVisualizer.TITLE);
 
   protected JMenu graphicsMenu = new JMenu("Graphics");
 
   protected JMenu visualizationMenu = new JMenu("Visualizations");
 
   protected JMenu dataMenu = new JMenu("Cache");
 
   protected JMenu helpMenu = new JMenu("Help");
 
   protected JMenuItem helpAbout = new JMenuItem("About...");
 
   protected JMenuItem clearCache = new JMenuItem("Clear Cache");
 
   protected JMenuItem cloneCache = new JMenuItem("Clone Cache");
 
   protected JMenuItem statsCache = new JMenuItem("Cache Info...");
 
   protected JMenuItem saveCache = new JMenuItem("Save Cache...");
 
   protected JMenuItem loadCache = new JMenuItem("Open Cache File...");
 
   protected JRadioButtonMenuItem visualizeRssiBars = new JRadioButtonMenuItem(
       "RSSI Bar");
   protected JRadioButtonMenuItem visualizeVarianceBars = new JRadioButtonMenuItem(
       "Var. Bar");
 
   protected JRadioButtonMenuItem visualizeRssiVoronoi = new JRadioButtonMenuItem(
       "RSSI. Voronoi");
 
   protected JRadioButtonMenuItem visualizeVarianceVoronoi = new JRadioButtonMenuItem(
       "Var. Voronoi");
 
   protected JRadioButtonMenuItem visualizeRssiLines = new JRadioButtonMenuItem(
       "RSSI Lines");
 
   protected JRadioButtonMenuItem visualizeVarianceLines = new JRadioButtonMenuItem(
       "Var. Lines");
 
   protected JRadioButtonMenuItem visualizeRssiStDvLines = new JRadioButtonMenuItem(
       "RSSI+Var. Lines");
 
   protected JRadioButtonMenuItem visualizeSignalRings = new JRadioButtonMenuItem(
       "RSSI Rings");
 
   protected JRadioButtonMenuItem visualizeRssiStripes = new JRadioButtonMenuItem(
       "RSSI Stripes");
 
   protected JRadioButtonMenuItem visualizeVarianceStripes = new JRadioButtonMenuItem(
       "Var Stripes");
 
   protected JRadioButtonMenuItem visualizeRssiLineMap = new JRadioButtonMenuItem(
       "RSSI Line Map");
 
   protected JRadioButtonMenuItem visualizeVarLineMap = new JRadioButtonMenuItem(
       "Var. Line Map");
 
   protected JRadioButtonMenuItem visualizeRssiIntersect = new JRadioButtonMenuItem(
       "RSSI Intersect. Map");
   protected JRadioButtonMenuItem visualizeVarIntersect = new JRadioButtonMenuItem(
       "Var. Intersect. Map");
 
   protected JRadioButtonMenuItem visualizeMaxRssiMap = new JRadioButtonMenuItem(
       "Max RSSI Map");
   protected JRadioButtonMenuItem visualizeMaxVarianceMap = new JRadioButtonMenuItem(
       "Max Var. Map");
   
   protected JRadioButtonMenuItem visualizeAmbient = new JRadioButtonMenuItem("Ambient Variance");
 
   protected JMenuItem gfxAntiAlias = new JCheckBoxMenuItem("Anti-Alias");
   protected JMenuItem gfxTransparent = new JCheckBoxMenuItem("Transparency");
 
   protected JMenuItem refresh1hz = new JRadioButtonMenuItem("1 Hz");
   protected JMenuItem refresh5hz = new JRadioButtonMenuItem("5 Hz");
   protected JMenuItem refresh10hz = new JRadioButtonMenuItem("10 Hz", true);
   protected JMenuItem refresh15hz = new JRadioButtonMenuItem("15 Hz");
   protected JMenuItem refresh20hz = new JRadioButtonMenuItem("20 Hz");
   protected JMenuItem refresh30hz = new JRadioButtonMenuItem("30 Hz");
 
   protected ButtonGroup refreshGroup = new ButtonGroup();
 
   protected JMenu receiversMenu = new JMenu("Receivers");
 
   protected ConcurrentHashMap<JRadioButtonMenuItem, String> receiverMenuItems = new ConcurrentHashMap<JRadioButtonMenuItem, String>();
 
   protected JMenu transmittersMenu = new JMenu("Transmitters");
 
   protected JMenu transmittersFiduciaryMenu = new JMenu("Fiduciary");
 
   protected JMenu transmittersDynamicMenu = new JMenu("Dynamic");
 
   protected ConcurrentHashMap<JRadioButtonMenuItem, String> transmitterFiduciaryItems = new ConcurrentHashMap<JRadioButtonMenuItem, String>();
 
   protected ConcurrentHashMap<JRadioButtonMenuItem, String> transmitterDynamicItems = new ConcurrentHashMap<JRadioButtonMenuItem, String>();
 
   protected JMenu deviceMenu = new JMenu("Device");
 
   protected JMenu sourceReceiversMenu = new JMenu("Receivers");
 
   protected ConcurrentHashMap<JCheckBoxMenuItem, String> sourceReceiverMenuItems = new ConcurrentHashMap<JCheckBoxMenuItem, String>();
 
   protected JMenu sourceTransmittersMenu = new JMenu("Transmitters");
 
   protected ConcurrentHashMap<JCheckBoxMenuItem, String> sourceTransmitterMenuItems = new ConcurrentHashMap<JCheckBoxMenuItem, String>();
 
   protected JMenuItem sourceReceiverSelectAll = new JMenuItem("Select All");
 
   protected JMenuItem sourceReceiverClearAll = new JMenuItem("Clear All");
 
   protected JMenuItem sourceTransmitterSelectAll = new JMenuItem("Select All");
 
   protected JMenuItem sourceTransmitterClearAll = new JMenuItem("Clear All");
 
   protected JMenu sourcesMenu = new JMenu("Sources");
 
   protected ButtonGroup selectedDeviceGroup = new ButtonGroup();
 
   protected ButtonGroup visualizationGroup = new ButtonGroup();
 
   protected static volatile int numWindows = 0;
 
   protected JFileChooser fileChooser = new JFileChooser();
 
   protected void buildMenu() {
 
     int acceleratorMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
     this.windowNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
         acceleratorMask));
     this.saveCache.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
         acceleratorMask));
     this.loadCache.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
         acceleratorMask));
     this.closeWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
         acceleratorMask));
     this.quitApp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
         acceleratorMask));
 
     this.fileMenu.add(this.windowNew);
     this.fileMenu.add(this.openConnection);
     this.fileMenu.add(this.loadCache);
     this.fileMenu.add(this.saveCache);
 
     this.fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
     this.fileMenu.add(this.closeWindow);
     this.fileMenu.add(this.quitApp);
 
     this.windowNew.addActionListener(this);
     this.openConnection.addActionListener(this);
     this.saveCache.addActionListener(this);
     this.loadCache.addActionListener(this);
     this.closeWindow.addActionListener(this);
     this.quitApp.addActionListener(this);
 
     this.menu.add(this.fileMenu);
 
     // this.menu.add(this.windowMenu);
 
     this.menu.add(this.deviceMenu);
 
     this.deviceMenu.add(this.transmittersMenu);
     this.deviceMenu.add(this.receiversMenu);
 
     this.transmittersMenu.add(this.transmittersFiduciaryMenu);
     this.transmittersMenu.add(this.transmittersDynamicMenu);
 
     this.menu.add(this.sourcesMenu);
     this.sourcesMenu.add(this.sourceTransmittersMenu);
     this.sourcesMenu.add(this.sourceReceiversMenu);
 
     this.sourceTransmittersMenu.add(this.sourceTransmitterSelectAll);
     this.sourceTransmittersMenu.add(this.sourceTransmitterClearAll);
     this.sourceTransmittersMenu.add(new JSeparator(JSeparator.HORIZONTAL));
 
     this.sourceReceiversMenu.add(this.sourceReceiverSelectAll);
     this.sourceReceiversMenu.add(this.sourceReceiverClearAll);
     this.sourceReceiversMenu.add(new JSeparator(JSeparator.HORIZONTAL));
 
     this.sourceTransmitterSelectAll.addActionListener(this);
     this.sourceTransmitterClearAll.addActionListener(this);
 
     this.sourceReceiverSelectAll.addActionListener(this);
     this.sourceReceiverClearAll.addActionListener(this);
 
     this.buildMenuTransmitters();
 
     this.buildMenuReceivers();
 
     this.refreshGroup.add(this.refresh1hz);
     this.refreshGroup.add(this.refresh5hz);
     this.refreshGroup.add(this.refresh10hz);
     this.refreshGroup.add(this.refresh15hz);
     this.refreshGroup.add(this.refresh20hz);
     this.refreshGroup.add(this.refresh30hz);
 
     this.graphicsMenu.add(this.gfxAntiAlias);
     this.graphicsMenu.add(this.gfxTransparent);
     this.graphicsMenu.add(new JSeparator(JSeparator.HORIZONTAL));
 
     this.graphicsMenu.add(this.refresh1hz);
     this.graphicsMenu.add(this.refresh5hz);
     this.graphicsMenu.add(this.refresh10hz);
     this.graphicsMenu.add(this.refresh15hz);
     this.graphicsMenu.add(this.refresh20hz);
     this.graphicsMenu.add(this.refresh30hz);
 
     this.gfxAntiAlias.addActionListener(this);
     this.gfxTransparent.addActionListener(this);
 
     this.refresh1hz.addActionListener(this);
     this.refresh5hz.addActionListener(this);
     this.refresh10hz.addActionListener(this);
     this.refresh15hz.addActionListener(this);
     this.refresh20hz.addActionListener(this);
     this.refresh30hz.addActionListener(this);
 
     this.menu.add(graphicsMenu);
 
     this.visualizationMenu.add(this.visualizeRssiBars);
     this.visualizationMenu.add(this.visualizeVarianceBars);
     this.visualizationMenu.add(this.visualizeRssiVoronoi);
     this.visualizationMenu.add(this.visualizeVarianceVoronoi);
     this.visualizationMenu.add(this.visualizeRssiLines);
     this.visualizationMenu.add(this.visualizeVarianceLines);
     this.visualizationMenu.add(this.visualizeSignalRings);
     this.visualizationMenu.add(this.visualizeRssiStripes);
     this.visualizationMenu.add(this.visualizeVarianceStripes);
     this.visualizationMenu.add(this.visualizeRssiLineMap);
     this.visualizationMenu.add(this.visualizeVarLineMap);
 
     // TODO: Pretty sure this isn't a useful plot, but no sense in removing it
     // entirely.
     // this.visualizationMenu.add(this.visualizeRssiIntersect);
     this.visualizationMenu.add(this.visualizeVarIntersect);
     this.visualizationMenu.add(this.visualizeMaxRssiMap);
     this.visualizationMenu.add(this.visualizeMaxVarianceMap);
     this.visualizationMenu.add(this.visualizeRssiStDvLines);
     this.visualizationMenu.add(this.visualizeAmbient);
 
     this.visualizeRssiBars.addActionListener(this);
     this.visualizeVarianceBars.addActionListener(this);
     this.visualizeRssiVoronoi.addActionListener(this);
     this.visualizeVarianceVoronoi.addActionListener(this);
     this.visualizeRssiLines.addActionListener(this);
     this.visualizeVarianceLines.addActionListener(this);
     this.visualizeSignalRings.addActionListener(this);
     this.visualizeRssiStripes.addActionListener(this);
     this.visualizeVarianceStripes.addActionListener(this);
     this.visualizeRssiLineMap.addActionListener(this);
     this.visualizeVarLineMap.addActionListener(this);
     this.visualizeRssiIntersect.addActionListener(this);
     this.visualizeVarIntersect.addActionListener(this);
     this.visualizeMaxRssiMap.addActionListener(this);
     this.visualizeMaxVarianceMap.addActionListener(this);
     this.visualizeRssiStDvLines.addActionListener(this);
     this.visualizeAmbient.addActionListener(this);
     
 
     this.visualizeRssiBars.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
         acceleratorMask));
     this.visualizeVarianceBars.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_1, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeRssiVoronoi.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_2, acceleratorMask));
     this.visualizeVarianceVoronoi.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_2, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeRssiLines.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_3, acceleratorMask));
     this.visualizeVarianceLines.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_3, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeSignalRings.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_4, acceleratorMask));
     this.visualizeRssiStripes.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_5, acceleratorMask));
     this.visualizeVarianceStripes.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_5, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeRssiLineMap.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_6, acceleratorMask));
     this.visualizeVarLineMap.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_6, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     // this.visualizeRssiIntersect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7,
     // acceleratorMask));
     this.visualizeVarIntersect.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_7, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeMaxRssiMap.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_8, acceleratorMask));
     this.visualizeMaxVarianceMap.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_8, acceleratorMask | InputEvent.SHIFT_DOWN_MASK));
     this.visualizeRssiStDvLines.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_9, acceleratorMask));
     this.visualizeAmbient.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0,acceleratorMask));
 
     this.visualizationGroup.add(this.visualizeRssiBars);
     this.visualizationGroup.add(this.visualizeVarianceBars);
     this.visualizationGroup.add(this.visualizeRssiVoronoi);
     this.visualizationGroup.add(this.visualizeVarianceVoronoi);
     this.visualizationGroup.add(this.visualizeRssiLines);
     this.visualizationGroup.add(this.visualizeVarianceLines);
     this.visualizationGroup.add(this.visualizeSignalRings);
     this.visualizationGroup.add(this.visualizeRssiStripes);
     this.visualizationGroup.add(this.visualizeVarianceStripes);
     this.visualizationGroup.add(this.visualizeRssiLineMap);
     this.visualizationGroup.add(this.visualizeVarLineMap);
     this.visualizationGroup.add(this.visualizeRssiIntersect);
     this.visualizationGroup.add(this.visualizeVarIntersect);
     this.visualizationGroup.add(this.visualizeMaxRssiMap);
     this.visualizationGroup.add(this.visualizeMaxVarianceMap);
     this.visualizationGroup.add(this.visualizeRssiStDvLines);
     this.visualizationGroup.add(this.visualizeAmbient);
     
     this.visualizeAmbient.setSelected(true);
 
     this.menu.add(this.visualizationMenu);
 
     this.clearCache.addActionListener(this);
     this.cloneCache.addActionListener(this);
     this.statsCache.addActionListener(this);
 
     this.dataMenu.add(this.statsCache);
     this.dataMenu.add(this.cloneCache);
     this.dataMenu.add(new JSeparator(JSeparator.HORIZONTAL));
     // this.dataMenu.add(this.saveCache);
     // this.dataMenu.add(this.loadCache);
     // this.dataMenu.add(new JSeparator(JSeparator.HORIZONTAL));
     this.dataMenu.add(this.clearCache);
 
     this.menu.add(this.dataMenu);
 
     this.helpMenu.add(this.helpAbout);
     this.helpAbout.addActionListener(this);
     this.menu.add(this.helpMenu);
 
   }
 
   protected void buildMenuReceivers() {
     for (JMenuItem item : this.receiverMenuItems.keySet()) {
       this.selectedDeviceGroup.remove(item);
     }
 
     this.receiverMenuItems.clear();
     this.receiversMenu.removeAll();
 
     // this.receiversMenu.removeAll();
     this.sourceReceiverMenuItems.clear();
     this.sourceReceiversMenu.removeAll();
     this.sourceReceiversMenu.add(this.sourceReceiverSelectAll);
     this.sourceReceiversMenu.add(this.sourceReceiverClearAll);
 
     List<String> receivers = this.cache.getReceiverIds();
     Collections.sort(receivers);
     for (String receiver : receivers) {
       JRadioButtonMenuItem newSelectedItem = new JRadioButtonMenuItem(
           receiver.toString());
       JCheckBoxMenuItem newCheckedItem = new JCheckBoxMenuItem(
           receiver.toString());
       newCheckedItem.setSelected(true);
       if (this.receiverMenuItems.put(newSelectedItem, receiver) == null) {
         newSelectedItem.addActionListener(this);
         this.selectedDeviceGroup.add(newSelectedItem);
         this.receiversMenu.add(newSelectedItem);
       }
       if (this.sourceReceiverMenuItems.put(newCheckedItem, receiver) == null) {
         newCheckedItem.addActionListener(this);
         this.sourceReceiversMenu.add(newCheckedItem);
       }
     }
   }
 
   protected void buildMenuTransmitters() {
     // TODO: Finish me
     for (JMenuItem item : this.transmitterFiduciaryItems.keySet()) {
       this.selectedDeviceGroup.remove(item);
     }
     this.transmitterFiduciaryItems.clear();
     this.transmittersFiduciaryMenu.removeAll();
 
     this.sourceTransmitterMenuItems.clear();
     this.sourceTransmittersMenu.removeAll();
     this.sourceTransmittersMenu.add(this.sourceTransmitterSelectAll);
     this.sourceTransmittersMenu.add(this.sourceTransmitterClearAll);
 
     // Fiduciary transmitters
     List<String> transmitters = this.cache.getFiduciaryTransmitterIds();
     Collections.sort(transmitters);
     for (String transmitter : transmitters) {
       JRadioButtonMenuItem newSelectedItem = new JRadioButtonMenuItem(
           transmitter.toString());
       JCheckBoxMenuItem newCheckedItem = new JCheckBoxMenuItem(
           transmitter.toString());
       newCheckedItem.setSelected(true);
       if (this.transmitterFiduciaryItems.put(newSelectedItem, transmitter) == null) {
         newSelectedItem.addActionListener(this);
         this.selectedDeviceGroup.add(newSelectedItem);
         this.transmittersFiduciaryMenu.add(newSelectedItem);
       }
       if (this.sourceTransmitterMenuItems.put(newCheckedItem, transmitter) == null) {
         newCheckedItem.addActionListener(this);
         this.sourceTransmittersMenu.add(newCheckedItem);
       }
     }
 
     for (JMenuItem item : this.transmitterDynamicItems.keySet()) {
       this.selectedDeviceGroup.remove(item);
     }
     this.transmitterDynamicItems.clear();
     this.transmittersDynamicMenu.removeAll();
 
     // Dynamic transmitters
     transmitters = this.cache.getDynamicTransmitterIds();
     for (String transmitter : transmitters) {
       JRadioButtonMenuItem newSelectedItem = new JRadioButtonMenuItem(
           transmitter.toString());
       JCheckBoxMenuItem newCheckedItem = new JCheckBoxMenuItem(
           transmitter.toString());
       newCheckedItem.setSelected(true);
       if (this.transmitterDynamicItems.put(newSelectedItem, transmitter) == null) {
         newSelectedItem.addActionListener(this);
         this.selectedDeviceGroup.add(newSelectedItem);
         this.transmittersDynamicMenu.add(newSelectedItem);
       }
       if (this.sourceTransmitterMenuItems.put(newCheckedItem, transmitter) == null) {
         newCheckedItem.addActionListener(this);
         this.sourceTransmittersMenu.add(newCheckedItem);
       }
 
     }
   }
 
   public void actionPerformed(ActionEvent e) {
     if (e.getSource() == this.gfxAntiAlias && this.displayPanel != null) {
 
       this.displayPanel.setAntiAlias(this.gfxAntiAlias.isSelected());
     } else if (e.getSource() == this.gfxTransparent
         && this.displayPanel != null) {
       this.displayPanel.setTransparency(this.gfxTransparent.isSelected());
     }
 
     else if (e.getSource() == this.refresh1hz) {
       this.setDesiredFps(1);
     } else if (e.getSource() == this.refresh5hz) {
       this.setDesiredFps(5);
     } else if (e.getSource() == this.refresh10hz) {
       this.setDesiredFps(10);
     } else if (e.getSource() == this.refresh15hz) {
       this.setDesiredFps(15);
     } else if (e.getSource() == this.refresh20hz) {
       this.setDesiredFps(20);
     } else if (e.getSource() == this.refresh30hz) {
       this.setDesiredFps(30);
     } else if (e.getSource() == this.windowNew) {
       this.openNewWindow();
     } else if (e.getSource() == this.visualizeRssiBars) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       BarChart newChart = new BarChart(DataCache2.ValueType.RSSI, this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       newChart.setDisplayedId(this.currentDeviceId);
       newChart.setDeviceIsTransmitter(this.isTransmitter);
       this.titleChartType = "RSSI Bar Chart";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
     } else if (e.getSource() == this.visualizeVarianceBars) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       BarChart newChart = new BarChart(DataCache2.ValueType.VARIANCE,
           this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(0f);
       this.displayPanel.setMaxValue(50f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       newChart.setDisplayedId(this.currentDeviceId);
       newChart.setDeviceIsTransmitter(this.isTransmitter);
       this.titleChartType = "Variance Bar Chart";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
     } else if (e.getSource() == this.visualizeRssiVoronoi) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       VoronoiHeatMap heatMap = new VoronoiHeatMap(ValueType.RSSI, this.cache);
       this.configureGfx(heatMap);
       this.mainPanel = heatMap;
       this.displayPanel = heatMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       heatMap.setDisplayedId(this.currentDeviceId);
       heatMap.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "RSSI Voronoi Map";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(heatMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeVarianceVoronoi) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       VoronoiHeatMap heatMap = new VoronoiHeatMap(ValueType.VARIANCE,
           this.cache);
       this.configureGfx(heatMap);
       this.mainPanel = heatMap;
       this.displayPanel = heatMap;
       this.displayPanel.setMinValue(0f);
       this.displayPanel.setMaxValue(50f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       heatMap.setDisplayedId(this.currentDeviceId);
       heatMap.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Variance Voronoi Map";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(heatMap, BorderLayout.CENTER);
       this.validate();
     } else if (e.getSource() == this.visualizeRssiLines) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       LineChart newChart = new LineChart(ValueType.RSSI, this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setSelfAdjustMax(false);
       this.displayPanel.setSelfAdjustMin(false);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(true);
 
       this.titleChartType = "RSSI Lines";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
     } else if (e.getSource() == this.visualizeVarianceLines) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       LineChart newChart = new LineChart(ValueType.VARIANCE, this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(0f);
       this.displayPanel.setMaxValue(50f);
       this.displayPanel.setSelfAdjustMax(true);
       this.displayPanel.setSelfAdjustMin(false);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(true);
       this.titleChartType = "Variance Lines";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeSignalRings) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       SignalToDistanceMap newMap = new SignalToDistanceMap(this.cache);
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setDisplayLegend(true);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "RSSI Rings";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeRssiStripes) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       HeatStripes newMap = new HeatStripes(ValueType.RSSI, this.cache);
       newMap.setThresholdValue(-100f);
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "RSSI Stripes";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeVarianceStripes) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       HeatStripes newMap = new HeatStripes(ValueType.VARIANCE, this.cache);
       newMap.setThresholdValue(1f);
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(0f);
       this.displayPanel.setMaxValue(20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Variance Stripes";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeRssiLineMap) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       SignalLineMap newMap = new SignalLineMap(ValueType.RSSI, this.cache);
 
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(0f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "RSSI Lines";
       this.panelTitle =(this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeVarLineMap) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       SignalLineMap newMap = new SignalLineMap(ValueType.VARIANCE, this.cache);
 
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(1f);
       this.displayPanel.setMaxValue(20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Variance Lines";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeRssiIntersect) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       IntersectionLineMap newMap = new IntersectionLineMap(ValueType.RSSI,
           this.cache);
 
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(0f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "RSSI Intersect.";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeVarIntersect) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       IntersectionLineMap newMap = new IntersectionLineMap(ValueType.VARIANCE,
           this.cache);
 
       this.configureGfx(newMap);
       this.mainPanel = newMap;
       this.displayPanel = newMap;
       this.displayPanel.setMinValue(1f);
       this.displayPanel.setMaxValue(20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setDisplayLegend(false);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Variance Intersect.";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeMaxRssiMap) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       VoronoiRSSIQualityMap heatMap = new VoronoiRSSIQualityMap(ValueType.RSSI,
           this.cache);
       this.configureGfx(heatMap);
       this.mainPanel = heatMap;
       this.displayPanel = heatMap;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       heatMap.setDisplayedId(this.currentDeviceId);
       heatMap.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Max RSSI Map";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(heatMap, BorderLayout.CENTER);
       this.validate();
 
     } else if (e.getSource() == this.visualizeMaxVarianceMap) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       VoronoiRSSIQualityMap heatMap = new VoronoiRSSIQualityMap(
           ValueType.VARIANCE, this.cache);
       this.configureGfx(heatMap);
       this.mainPanel = heatMap;
       this.displayPanel = heatMap;
       this.displayPanel.setMinValue(0f);
       this.displayPanel.setMaxValue(50f);
       this.displayPanel.setMaxAge(this.staleDataAge);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       heatMap.setDisplayedId(this.currentDeviceId);
       heatMap.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setDeviceIcon(this.isTransmitter ? this.transmitterIcon
           : this.receiverIcon);
       this.titleChartType = "Max Variance Map";
       this.panelTitle =(this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(heatMap, BorderLayout.CENTER);
       this.validate();
     } else if (e.getSource() == this.visualizeRssiStDvLines) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       RssiStDvLineChart newChart = new RssiStDvLineChart(this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-20f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(true);
       this.titleChartType = "RSSI + StdDev Lines";
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
 
     }else if (e.getSource() == this.visualizeAmbient) {
       if (this.mainPanel != null) {
         this.remove(this.mainPanel);
       }
       AmbientCloud newChart = new AmbientCloud(this.cache);
       this.configureGfx(newChart);
       this.mainPanel = newChart;
       this.displayPanel = newChart;
       this.displayPanel.setMinValue(-100f);
       this.displayPanel.setMaxValue(-40f);
       this.displayPanel.setTimeOffset(this.currentTimeOffset);
       this.displayPanel.setDisplayedId(this.currentDeviceId);
       this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
       this.displayPanel.setMaxAge(this.displayedHistory);
       this.displayPanel.setDisplayLegend(true);
       this.titleChartType = "Ambient Variance";
       this.panelTitle = (this.cache.getRegionUri() == null ? "" : this.cache.getRegionUri()) + this.titleChartType;
       this.setTitle();
       this.add(newChart, BorderLayout.CENTER);
       this.validate();
 
     }
 
     else if (e.getSource() instanceof JRadioButtonMenuItem) {
       boolean found = false;
       for (JRadioButtonMenuItem menuItem : this.receiverMenuItems.keySet()) {
         if (e.getSource() == menuItem) {
           String receiverId = this.receiverMenuItems.get(menuItem);
           this.currentDeviceId = receiverId;
           this.titleDeviceName = receiverId.toString();
           this.isTransmitter = false;
           if (this.displayPanel != null) {
             this.displayPanel.setDisplayedId(receiverId);
             this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
             this.displayPanel.setDeviceIcon(this.receiverIcon);
           }
           found = true;
           break;
         }
 
       }
       if (!found) {
         for (JRadioButtonMenuItem menuItem : this.transmitterFiduciaryItems
             .keySet()) {
           if (e.getSource() == menuItem) {
             String transmitterId = this.transmitterFiduciaryItems.get(menuItem);
             this.currentDeviceId = transmitterId;
             this.titleDeviceName = transmitterId.toString();
             this.isTransmitter = true;
             if (this.displayPanel != null) {
               this.displayPanel.setDisplayedId(transmitterId);
               this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
               this.displayPanel.setDeviceIcon(this.transmitterIcon);
             }
             found = true;
             break;
           }
 
         }
       }
       if (!found) {
         for (JRadioButtonMenuItem menuItem : this.transmitterDynamicItems
             .keySet()) {
           if (e.getSource() == menuItem) {
             String transmitterId = this.transmitterDynamicItems.get(menuItem);
             this.currentDeviceId = transmitterId;
             this.titleDeviceName = transmitterId.toString();
             this.isTransmitter = true;
             if (this.displayPanel != null) {
               this.displayPanel.setDisplayedId(transmitterId);
               this.displayPanel.setDeviceIsTransmitter(this.isTransmitter);
               this.displayPanel.setDeviceIcon(this.transmitterIcon);
             }
             found = true;
             break;
           }
         }
       }
       this.panelTitle = (this.isTransmitter ? "Transmitter " : "Receiver ")
           + this.titleDeviceName + " - " + this.titleChartType;
       this.setTitle();
       this.validate();
     } else if (e.getSource() instanceof JCheckBoxMenuItem) {
       boolean found = false;
       for (JCheckBoxMenuItem menuItem : this.sourceReceiverMenuItems.keySet()) {
         if (e.getSource() == menuItem) {
           found = true;
           String deviceId = this.sourceReceiverMenuItems.get(menuItem);
           if (menuItem.isSelected()) {
             if (deviceId != null) {
               this.cache.addAllowedDevice(deviceId);
             } else {
               log.warn("No HashableByteArray available for {}", menuItem);
             }
           } else {
             if (deviceId != null) {
               this.cache.removeAllowedDevice(deviceId);
             } else {
               log.warn("No HashableByteArray available for {}", menuItem);
             }
           }
           break;
         }
       }
       if (!found) {
         for (JCheckBoxMenuItem menuItem : this.sourceTransmitterMenuItems
             .keySet()) {
           if (e.getSource() == menuItem) {
             found = true;
             String deviceId = this.sourceTransmitterMenuItems.get(menuItem);
             if (menuItem.isSelected()) {
               if (deviceId != null) {
                 this.cache.addAllowedDevice(deviceId);
               } else {
                 log.warn("No HashableByteArray available for {}", menuItem);
               }
             } else {
               if (deviceId != null) {
                 this.cache.removeAllowedDevice(deviceId);
               } else {
                 log.warn("No HashableByteArray available for {}", menuItem);
               }
             }
             break;
           }
         }
       }
     }
 
     else if (e.getSource() == this.clearCache) {
       int response = JOptionPane
           .showConfirmDialog(
               this,
               "This will clear all data from the cache.\nAre you sure you want to clear the cache?",
               "Confirm Cache Clear", JOptionPane.OK_CANCEL_OPTION);
       if (response == JOptionPane.OK_OPTION) {
         this.cache.clearCachedData();
       }
     } else if (e.getSource() == this.cloneCache) {
       this.cloneNewWindow();
     } else if (e.getSource() == this.statsCache) {
       this.cache.showStatsPane(this);
     } else if (e.getSource() == this.saveCache) {
       this.saveCache();
     } else if (e.getSource() == this.loadCache) {
       this.loadCache();
     } else if (e.getSource() == this.openConnection) {
       this.openConnection();
     } else if (e.getSource() == this.sourceReceiverClearAll) {
       for (JCheckBoxMenuItem item : this.sourceReceiverMenuItems.keySet()) {
         item.setSelected(false);
         this.cache.removeAllowedDevice(this.sourceReceiverMenuItems.get(item));
       }
 
     } else if (e.getSource() == this.sourceReceiverSelectAll) {
       for (JCheckBoxMenuItem item : this.sourceReceiverMenuItems.keySet()) {
         item.setSelected(true);
         this.cache.addAllowedDevice(this.sourceReceiverMenuItems.get(item));
       }
 
     } else if (e.getSource() == this.sourceTransmitterClearAll) {
       for (JCheckBoxMenuItem item : this.sourceTransmitterMenuItems.keySet()) {
         item.setSelected(false);
         this.cache.removeAllowedDevice(this.sourceTransmitterMenuItems
             .get(item));
       }
     } else if (e.getSource() == this.sourceTransmitterSelectAll) {
       for (JCheckBoxMenuItem item : this.sourceTransmitterMenuItems.keySet()) {
         item.setSelected(true);
         this.cache.addAllowedDevice(this.sourceTransmitterMenuItems.get(item));
       }
     } else if (e.getSource() == this.helpAbout) {
       JEditorPane htmlPane = new JEditorPane("text/html",
           SignalVisualizer.ABOUT_HTML);
       htmlPane.setEditable(false);
       JOptionPane.showMessageDialog(this, htmlPane, "About "
           + SignalVisualizer.TITLE, JOptionPane.INFORMATION_MESSAGE);
     } else if (e.getSource() == this.closeWindow) {
       this.close();
     } else if (e.getSource() == this.quitApp) {
       this.quit();
     }
   }
 
   private void close() {
     this.cache.removeListener(this);
     this.setVisible(false);
     this.dispose();
   }
 
   private void quit() {
     if (this.isVisible()) {
       int response = JOptionPane.showConfirmDialog(this,
           "Are you sure you want to quit?", "Exit "+SignalVisualizer.TITLE, JOptionPane.OK_CANCEL_OPTION);
       if (response != JOptionPane.OK_OPTION) {
         return;
       }
     }
 
     this.cache.shutdown();
     this.connHandler.disconnectAsClient();
 
     for (Iterator<SimpleFrame> iter = allFrames.iterator(); iter.hasNext();) {
       SimpleFrame frame = iter.next();
       frame.close();
     }
 
     System.exit(0);
   }
 
   protected void openConnection() {
 
     String host = this.connHandler.getClientHost(), region = this.connHandler
         .getRegion();
     int port = this.connHandler.getClientPort();
     if (host != null) {
       this.connectionPanel.setWorldModelHost(host);
     }
     if (port > 0) {
       this.connectionPanel.setWorldModelPort(Integer.toString(port));
     }
     if (region != null) {
       this.connectionPanel.setRegion(region);
     }
     boolean badInput = false;
     do {
       if (badInput) {
         JOptionPane
             .showMessageDialog(
                 this,
                 "Unable to connect to world model.\nPlease verify the connection settings.",
                 "Connection Failed", JOptionPane.WARNING_MESSAGE);
         badInput = false;
       }
 
       int response = JOptionPane.showConfirmDialog(this, this.connectionPanel,
           "World Model Details", JOptionPane.OK_CANCEL_OPTION,
           JOptionPane.INFORMATION_MESSAGE);
       if (response != JOptionPane.OK_OPTION) {
         return;
       }
       host = this.connectionPanel.getWorldModelHost();
       if (host == null) {
         badInput = true;
         continue;
       }
       host = host.trim();
       if (host.length() == 0) {
         badInput = true;
         continue;
       }
       try {
         port = Integer.parseInt(this.connectionPanel.getWorldModelPort());
       } catch (NumberFormatException nfe) {
         badInput = true;
         continue;
       }
       region = this.connectionPanel.getRegion();
       if (region == null) {
         badInput = true;
         continue;
       }
       region = region.trim();
       if (region.length() == 0) {
         badInput = true;
         continue;
       }
       try {
         this.connHandler.disconnectAsClient();
       } catch (Exception e) {
         log.error("Exception while disconnecting from world model.", e);
       }
       this.connHandler.setClientConnection(host, port);
       this.connHandler.setRegion(region);
       this.cache.clearAll();
       badInput = !this.connHandler.connectAsClient();
     } while (badInput || host == null || region == null || port <= 0);
 
     this.connHandler.startup();
   }
 
   protected void closeConnection() {
 
     this.cache.disableStreaming();
   }
 
   protected void saveCache() {
     long date = this.cache.isClone() ? this.cache.getCreationTs() : System
         .currentTimeMillis();
     String dateName = (this.cache.getRegionUri() == null ? "" : this.cache
         .getRegionUri() + " ")
         + SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,
             SimpleDateFormat.MEDIUM).format(new Date(date))
         + CacheFileFilter.EXTENSION;
     File dateFile = new File(dateName);
     this.fileChooser.setSelectedFile(dateFile);
     int returnVal = this.fileChooser.showSaveDialog(this);
 
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       File saveFile = this.fileChooser.getSelectedFile();
 
       if (this.fileChooser.getFileFilter() instanceof CacheFileFilter) {
         String fileName = saveFile.getPath();
         if (fileName.indexOf(CacheFileFilter.EXTENSION) == -1) {
           saveFile = new File(fileName + CacheFileFilter.EXTENSION);
         }
       }
       boolean approve = true;
       if (saveFile.exists()) {
         approve = (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
             "\"" + saveFile.getName() + "\" already exists.\nOverwrite?"));
       }
       if (approve) {
         this.cache.saveToFile(saveFile);
       }
     }
   }
 
   protected void loadCache() {
 
     this.fileChooser.setSelectedFile(FILE_NONE);
     int returnVal = this.fileChooser.showOpenDialog(this);
 
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       File loadFile = this.fileChooser.getSelectedFile();
       if (!loadFile.exists() || !loadFile.canRead()) {
         JOptionPane.showMessageDialog(this,
             "Cannot find file \"" + loadFile.getPath() + "\".",
             "Unable to find file.", JOptionPane.ERROR_MESSAGE);
         return;
       }
       // Since we're loading a cache from disk, no more live updates
       // Removing/readding the listener causes a WM disconnect
       // If this was the only/last frame listening to the incoming data.
       // FIXME: This is a hack.
       this.cache.removeListener(this);
 
       if (!this.cache.isClone() && this.cache.getNumListeners() > 0) {
         this.cache = this.cache.clone();
         if (this.displayPanel != null) {
           this.displayPanel.setCache(this.cache);
         }
       }
 
       this.cache.addListener(this);
 
       this.cache.restoreFromFile(loadFile);
 
     }
   }
 
   public void fileLoaded(final String filename) {
     this.configureTitlePrefix(filename);
     this.setTitle();
   }
 
   protected void configureGfx(final DisplayPanel panel) {
     if (panel.supportsAntiAlias()) {
       this.gfxAntiAlias.setEnabled(true);
       panel.setAntiAlias(this.gfxAntiAlias.isSelected());
     } else {
 
       this.gfxAntiAlias.setEnabled(false);
     }
     if (panel.supportsTransparency()) {
       this.gfxTransparent.setEnabled(true);
       panel.setTransparency(this.gfxTransparent.isSelected());
     } else {
       this.gfxTransparent.setEnabled(false);
     }
   }
 
   protected void openNewWindow() {
     SimpleFrame newFrame = new SimpleFrame(this.initialTitle, this.cache);
     newFrame.configureDisplay();
   }
 
   protected void cloneNewWindow() {
     SimpleFrame newFrame = new SimpleFrame(this.initialTitle,
         this.cache.clone());
     newFrame.configureDisplay();
   }
 
   @Override
   public void windowActivated(WindowEvent e) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void windowClosed(WindowEvent e) {
     --SimpleFrame.numWindows;
     this.allFrames.remove(this);
     this.cache.removeListener(this);
     if (this.cache.getNumListeners() == 0) {
       this.cache.shutdown();
       this.cache.clearAll();
     }
 
     if (SimpleFrame.numWindows <= 0) {
       log.warn("Forcing exit.");
       this.quit();
     }
   }
 
   @Override
   public void windowClosing(WindowEvent e) {
   }
 
   @Override
   public void windowDeactivated(WindowEvent e) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void windowDeiconified(WindowEvent e) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void windowIconified(WindowEvent e) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void windowOpened(WindowEvent e) {
     ++SimpleFrame.numWindows;
   }
 
   public int getDesiredFps() {
     return desiredFps;
   }
 
   public void setDesiredFps(int desiredFps) {
     this.desiredFps = desiredFps;
     if (this.displayPanel != null) {
       this.displayPanel.setMinFps((float) Math.ceil(this.desiredFps * 0.8f));
     }
 
     if (this.updateTask != null) {
       this.updateTask.cancel();
       this.updateTask = new TimerTask() {
 
         @Override
         public void run() {
           if (SimpleFrame.this.mainPanel != null) {
             SimpleFrame.this.mainPanel.repaint(20);
           }
         }
       };
       this.updateTimer.schedule(this.updateTask, 1000 / this.desiredFps,
           1000 / this.desiredFps);
     }
   }
 
   @Override
   public void receiverAdded(String receiverId) {
     if (receiverId == null) {
       this.buildMenuReceivers();
       return;
     }
     JRadioButtonMenuItem newReceiverItem = new JRadioButtonMenuItem(
         receiverId.toString());
     JCheckBoxMenuItem newCheckedItem = new JCheckBoxMenuItem(
         receiverId.toString());
     newCheckedItem.setSelected(true);
     this.cache.addAllowedDevice(receiverId);
     if (this.receiverMenuItems.put(newReceiverItem, receiverId) == null) {
       newReceiverItem.addActionListener(this);
       this.selectedDeviceGroup.add(newReceiverItem);
       this.receiversMenu.add(newReceiverItem);
     }
     if (this.sourceReceiverMenuItems.put(newCheckedItem, receiverId) == null) {
       newCheckedItem.addActionListener(this);
       this.sourceReceiversMenu.add(newCheckedItem);
     }
 
   }
 
   @Override
   public void transmitterAdded(String transmitterId, final boolean isFiduciary) {
     if (transmitterId == null) {
 
       this.buildMenuTransmitters();
       return;
     }
     JRadioButtonMenuItem newTransmitterItem = new JRadioButtonMenuItem(
         transmitterId.toString());
     JCheckBoxMenuItem newCheckedItem = new JCheckBoxMenuItem(
         transmitterId.toString());
     this.cache.addAllowedDevice(transmitterId);
     newCheckedItem.setSelected(true);
     if (isFiduciary) {
       if (this.transmitterFiduciaryItems.put(newTransmitterItem, transmitterId) == null) {
         this.selectedDeviceGroup.add(newTransmitterItem);
         this.transmittersFiduciaryMenu.add(newTransmitterItem);
         newTransmitterItem.addActionListener(this);
       }
     } else {
       if (this.transmitterDynamicItems.put(newTransmitterItem, transmitterId) == null) {
         this.selectedDeviceGroup.add(newTransmitterItem);
         this.transmittersDynamicMenu.add(newTransmitterItem);
         newTransmitterItem.addActionListener(this);
       }
 
     }
 
     if (this.sourceTransmitterMenuItems.put(newCheckedItem, transmitterId) == null) {
       newCheckedItem.addActionListener(this);
       this.sourceTransmittersMenu.add(newCheckedItem);
     }
     this.buildMenuTransmitters();
   }
 
   public void startUpdates() {
     this.updateTask = new TimerTask() {
 
       @Override
       public void run() {
         if (SimpleFrame.this.mainPanel != null) {
           SimpleFrame.this.mainPanel.repaint(20);
         }
       }
     };
     this.updateTimer.schedule(this.updateTask, 1000 / this.desiredFps,
         1000 / this.desiredFps);
   }
 
   @Override
   public void stateChanged(ChangeEvent arg0) {
 
     this.currentTimeOffset = (this.timeOffsetSlider.getMaximum() - this.timeOffsetSlider
         .getValue()) * 1000;
     if (this.displayPanel != null) {
       this.displayPanel.setTimeOffset(currentTimeOffset);
     }
   }
 
 }
