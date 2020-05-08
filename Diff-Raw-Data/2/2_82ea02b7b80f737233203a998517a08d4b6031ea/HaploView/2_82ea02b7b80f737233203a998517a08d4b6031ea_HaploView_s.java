 package edu.mit.wi.haploview;
 
 
 import edu.mit.wi.pedfile.PedFileException;
 import edu.mit.wi.pedfile.CheckData;
 import edu.mit.wi.haploview.TreeTable.*;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.io.*;
 import java.util.Vector;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 
 public class HaploView extends JFrame implements ActionListener, Constants{
 
     boolean DEBUG = false;
 
     JMenuItem readMarkerItem, analysisItem, blocksItem;
     String exportItems[] = {
         EXPORT_TEXT, EXPORT_PNG, EXPORT_OPTIONS
     };
     JMenuItem exportMenuItems[];
     JMenu keyMenu, displayMenu, analysisMenu;
     JMenuItem clearBlocksItem;
 
     String viewItems[] = {
         VIEW_DPRIME, VIEW_HAPLOTYPES, VIEW_CHECK_PANEL, VIEW_TDT
     };
     JRadioButtonMenuItem viewMenuItems[];
     String zoomItems[] = {
         "Zoomed", "Medium", "Unzoomed"
     };
     JRadioButtonMenuItem zoomMenuItems[];
     String colorItems[] = {
         "Standard", "R-squared", "D' / LOD (alt)", "Confidence bounds", "4 Gamete"
     };
     JRadioButtonMenuItem colorMenuItems[];
     JRadioButtonMenuItem blockMenuItems[];
     String blockItems[] = {"Confidence intervals (Gabriel et al)",
                            "Four Gamete Rule",
                            "Solid spine of LD",
                            "Custom"};
 
     HaploData theData;
     CheckDataPanel checkPanel;
 
     private int currentBlockDef = BLOX_GABRIEL;
     private TDTPanel tdtPanel;
     int currentScheme = STD_SCHEME;
     private javax.swing.Timer timer;
     //long maxCompDist;
 
     static HaploView window;
     JFileChooser fc;
     DPrimeDisplay dPrimeDisplay;
     private JScrollPane hapScroller;
     private HaplotypeDisplay hapDisplay;
     JTabbedPane tabs;
     CheckDataController cdc;
 
 
     public HaploView(){
         //set defaults
         Options.setMissingThreshold(1.0);
         Options.setSpacingThreshold(0.0);
         Options.setAssocTest(ASSOC_NONE);
         Options.setHaplotypeDisplayThreshold(1);
         Options.setMaxDistance(500);
 
         try{
             fc = new JFileChooser(System.getProperty("user.dir"));
         }catch(NullPointerException n){
             try{
                 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                 fc = new JFileChooser(System.getProperty("user.dir"));
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             }catch(Exception e){
             }
         }
 
         //menu setup
         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         JMenuItem menuItem;
 
         //file menu
         JMenu fileMenu = new JMenu("File");
         menuBar.add(fileMenu);
 
         menuItem = new JMenuItem(READ_GENOTYPES);
         setAccelerator(menuItem, 'O', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         /*
         viewGenotypesItem = new JMenuItem(VIEW_GENOTYPES);
         viewGenotypesItem.addActionListener(this);
         //viewGenotypesItem.setEnabled(false);
         fileMenu.add(viewGenotypesItem);
         */
 
         readMarkerItem = new JMenuItem(READ_MARKERS);
         setAccelerator(readMarkerItem, 'I', false);
         readMarkerItem.addActionListener(this);
         readMarkerItem.setEnabled(false);
         fileMenu.add(readMarkerItem);
 
         analysisItem = new JMenuItem(READ_ANALYSIS_TRACK);
         setAccelerator(analysisItem, 'A', false);
         analysisItem.addActionListener(this);
         analysisItem.setEnabled(false);
         fileMenu.add(analysisItem);
 
         blocksItem = new JMenuItem(READ_BLOCKS_FILE);
         setAccelerator(blocksItem, 'B', false);
         blocksItem.addActionListener(this);
         blocksItem.setEnabled(false);
         fileMenu.add(blocksItem);
 
         /*
         viewMarkerItem = new JMenuItem(VIEW_MARKERS);
         viewMarkerItem.addActionListener(this);
         //viewMarkerItem.setEnabled(false);
         fileMenu.add(viewMarkerItem);
         */
 
         fileMenu.addSeparator();
 
         exportMenuItems = new JMenuItem[exportItems.length];
         for (int i = 0; i < exportItems.length; i++) {
             exportMenuItems[i] = new JMenuItem(exportItems[i]);
             exportMenuItems[i].addActionListener(this);
             exportMenuItems[i].setEnabled(false);
             fileMenu.add(exportMenuItems[i]);
         }
 
         fileMenu.addSeparator();
         fileMenu.setMnemonic(KeyEvent.VK_F);
 
         menuItem = new JMenuItem("Quit");
         setAccelerator(menuItem, 'Q', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         /// display menu
         displayMenu = new JMenu("Display");
         displayMenu.setMnemonic(KeyEvent.VK_D);
         menuBar.add(displayMenu);
 
         ButtonGroup group = new ButtonGroup();
         viewMenuItems = new JRadioButtonMenuItem[viewItems.length];
         for (int i = 0; i < viewItems.length; i++) {
             viewMenuItems[i] = new JRadioButtonMenuItem(viewItems[i], i == 0);
             viewMenuItems[i].addActionListener(this);
 
             KeyStroke ks = KeyStroke.getKeyStroke('1' + i, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
             viewMenuItems[i].setAccelerator(ks);
 
             displayMenu.add(viewMenuItems[i]);
             viewMenuItems[i].setEnabled(false);
             group.add(viewMenuItems[i]);
         }
         displayMenu.addSeparator();
         //a submenu
         ButtonGroup zg = new ButtonGroup();
         JMenu zoomMenu = new JMenu("LD zoom");
         zoomMenu.setMnemonic(KeyEvent.VK_Z);
         zoomMenuItems = new JRadioButtonMenuItem[zoomItems.length];
         for (int i = 0; i < zoomItems.length; i++){
             zoomMenuItems[i] = new JRadioButtonMenuItem(zoomItems[i], i==0);
             zoomMenuItems[i].addActionListener(this);
             zoomMenuItems[i].setActionCommand("zoom" + i);
             zoomMenu.add(zoomMenuItems[i]);
             zg.add(zoomMenuItems[i]);
         }
         displayMenu.add(zoomMenu);
         //another submenu
         ButtonGroup cg = new ButtonGroup();
         JMenu colorMenu = new JMenu("LD color scheme");
         colorMenu.setMnemonic(KeyEvent.VK_C);
         colorMenuItems = new JRadioButtonMenuItem[colorItems.length];
         for (int i = 0; i< colorItems.length; i++){
             colorMenuItems[i] = new JRadioButtonMenuItem(colorItems[i],i==0);
             colorMenuItems[i].addActionListener(this);
             colorMenuItems[i].setActionCommand("color" + i);
             colorMenu.add(colorMenuItems[i]);
             cg.add(colorMenuItems[i]);
         }
         displayMenu.add(colorMenu);
         JMenuItem spacingItem = new JMenuItem("LD Display Spacing");
         spacingItem.setMnemonic(KeyEvent.VK_S);
         spacingItem.addActionListener(this);
         displayMenu.add(spacingItem);
         displayMenu.setEnabled(false);
 
         //analysis menu
         analysisMenu = new JMenu("Analysis");
         analysisMenu.setMnemonic(KeyEvent.VK_A);
         menuBar.add(analysisMenu);
         //a submenu
         ButtonGroup bg = new ButtonGroup();
         JMenu blockMenu = new JMenu("Define Blocks");
         blockMenu.setMnemonic(KeyEvent.VK_B);
         blockMenuItems = new JRadioButtonMenuItem[blockItems.length];
         for (int i = 0; i < blockItems.length; i++){
             blockMenuItems[i] = new JRadioButtonMenuItem(blockItems[i], i==0);
             blockMenuItems[i].addActionListener(this);
             blockMenuItems[i].setActionCommand("block" + i);
             blockMenuItems[i].setEnabled(false);
             blockMenu.add(blockMenuItems[i]);
             bg.add(blockMenuItems[i]);
         }
         analysisMenu.add(blockMenu);
         clearBlocksItem = new JMenuItem(CLEAR_BLOCKS);
         setAccelerator(clearBlocksItem, 'C', false);
         clearBlocksItem.addActionListener(this);
         clearBlocksItem.setEnabled(false);
         analysisMenu.add(clearBlocksItem);
         JMenuItem customizeBlocksItem = new JMenuItem(CUST_BLOCKS);
         customizeBlocksItem.addActionListener(this);
         analysisMenu.add(customizeBlocksItem);
         analysisMenu.setEnabled(false);
 
         //color key
         keyMenu = new JMenu("Key");
         menuBar.add(Box.createHorizontalGlue());
         menuBar.add(keyMenu);
 
         /** NEEDS FIXING
          helpMenu = new JMenu("Help");
          menuBar.add(Box.createHorizontalGlue());
          menuBar.add(helpMenu);
 
          menuItem = new JMenuItem("Tutorial");
          menuItem.addActionListener(this);
          helpMenu.add(menuItem);
          **/
         /*
         Configuration.readConfigFile();
         if(Configuration.isCheckForUpdate()) {
             Object[] options = {"Yes",
                                 "Not now",
                                 "Never ask again"};
             int n = JOptionPane.showOptionDialog(this,
                     "Would you like to check if a new version "
                     + "of haploview is available?",
                     "Check for update",
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     options,
                     options[1]);
 
             if(n == JOptionPane.YES_OPTION) {
                 UpdateChecker uc = new UpdateChecker();
                 if(uc.checkForUpdate()) {
                     JOptionPane.showMessageDialog(this,
                             "A new version of Haploview is available!\n Visit http://www.broad.mit.edu/mpg/haploview/ to download the new version\n (current version: " + Constants.VERSION
                             + "  newest version: " + uc.getNewVersion() + ")" ,
                             "Update Available",
                             JOptionPane.INFORMATION_MESSAGE);
                 }
             }
             else if(n == JOptionPane.CANCEL_OPTION) {
                 Configuration.setCheckForUpdate(false);
                 Configuration.writeConfigFile();
             }
         }           */
 
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e){
                 quit();
             }
         });
 
 
 
     }
 
 
     // function workaround for overdesigned, underthought swing api -fry
     void setAccelerator(JMenuItem menuItem, char what, boolean shift) {
         menuItem.setAccelerator(KeyStroke.getKeyStroke(what, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | (shift ? ActionEvent.SHIFT_MASK : 0)));
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals(READ_GENOTYPES)){
             ReadDataDialog readDialog = new ReadDataDialog("Open new data", this);
             readDialog.pack();
             readDialog.setVisible(true);
         } else if (command.equals(READ_MARKERS)){
             //JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 readMarkers(fc.getSelectedFile(),null);
             }
         }else if (command.equals(READ_ANALYSIS_TRACK)){
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 readAnalysisFile(fc.getSelectedFile());
             }
         }else if (command.equals(READ_BLOCKS_FILE)){
             fc.setSelectedFile(new File(""));
             if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                 readBlocksFile(fc.getSelectedFile());
             }
         }else if (command.equals(CUST_BLOCKS)){
             TweakBlockDefsDialog tweakDialog = new TweakBlockDefsDialog("Customize Blocks", this);
             tweakDialog.pack();
             tweakDialog.setVisible(true);
         }else if (command.equals(CLEAR_BLOCKS)){
             changeBlocks(BLOX_NONE);
 
             //blockdef clauses
         }else if (command.startsWith("block")){
             int method = Integer.valueOf(command.substring(5)).intValue();
 
             changeBlocks(method);
             /*for (int i = 1; i < colorMenuItems.length; i++){
             if (method+1 == i){
             colorMenuItems[i].setEnabled(true);
             }else{
             colorMenuItems[i].setEnabled(false);
             }
             }
             colorMenuItems[0].setSelected(true);*/
 
             //zooming clauses
         }else if (command.startsWith("zoom")){
             dPrimeDisplay.zoom(Integer.valueOf(command.substring(4)).intValue());
 
             //coloring clauses
         }else if (command.startsWith("color")){
             currentScheme = Integer.valueOf(command.substring(5)).intValue()+1;
             dPrimeDisplay.colorDPrime(currentScheme);
             changeKey(currentScheme);
             //exporting clauses
         }else if (command.equals(EXPORT_PNG)){
             export(tabs.getSelectedIndex(), PNG_MODE, 0, Chromosome.getSize());
         }else if (command.equals(EXPORT_TEXT)){
             export(tabs.getSelectedIndex(), TXT_MODE, 0, Chromosome.getSize());
         }else if (command.equals(EXPORT_OPTIONS)){
             ExportDialog exDialog = new ExportDialog(this);
             exDialog.pack();
             exDialog.setVisible(true);
         }else if (command.equals("Select All")){
             checkPanel.selectAll();
         }else if (command.equals("Rescore Markers")){
             String cut = cdc.hwcut.getText();
             if (cut.equals("")){
                 cut = "0";
             }
             CheckData.hwCut = Double.parseDouble(cut);
 
             cut = cdc.genocut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.failedGenoCut = Integer.parseInt(cut);
 
             cut = cdc.mendcut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.numMendErrCut = Integer.parseInt(cut);
 
             cut = cdc.mafcut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.mafCut = Double.parseDouble(cut);
 
             checkPanel.redoRatings();
         }else if (command.equals("LD Display Spacing")){
             ProportionalSpacingDialog spaceDialog = new ProportionalSpacingDialog(this, "Adjust LD Spacing");
             spaceDialog.pack();
             spaceDialog.setVisible(true);
         }else if (command.equals("Tutorial")){
             showHelp();
         } else if (command.equals("Quit")){
             quit();
         } else {
             for (int i = 0; i < viewItems.length; i++) {
                 if (command.equals(viewItems[i])) tabs.setSelectedIndex(i);
             }
         }
     }
 
 
 
     private void changeKey(int scheme) {
         keyMenu.removeAll();
         if (scheme == WMF_SCHEME){
             JMenuItem keyItem = new JMenuItem("High D' / High LOD");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             for (int i = 1; i < 5; i++){
                 double gr = i * (255.0 / 6.0);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color((int)gr, (int)gr, (int)gr));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("Low D' / Low LOD");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
 
             keyItem = new JMenuItem("High D' / High LOD");
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             for (int i = 1; i < 5; i++){
                 double r = i * (255.0 / 6.0);
                 double gb = i * (200.0 / 6.0);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color((int)r, (int)gb, (int)gb));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("High D' / Low LOD");
             keyItem.setBackground(new Color(255, 200, 200));
             keyMenu.add(keyItem);
 	} else if (scheme == RSQ_SCHEME){
             JMenuItem keyItem = new JMenuItem("High R-squared");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.darkGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.gray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.lightGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Low R-squared");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
 	} else if (scheme == STD_SCHEME){
             JMenuItem keyItem = new JMenuItem("High D'");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.red);
             keyMenu.add(keyItem);
             for (int i = 1; i < 4; i++){
                 double blgr = (255-32)*2*(0.5*i/3);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color(255,(int)blgr, (int)blgr));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("Low D'");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("High D' / Low LOD");
             keyItem.setBackground(new Color(192, 192, 240));
             keyMenu.add(keyItem);
         } else if (scheme == SFS_SCHEME){
             JMenuItem keyItem = new JMenuItem("Strong Linkage");
             keyItem.setBackground(Color.darkGray);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Uninformative");
             keyItem.setBackground(Color.lightGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Recombination");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
         } else if (scheme == GAM_SCHEME){
             JMenuItem keyItem = new JMenuItem("< 4 Gametes");
             keyItem.setBackground(Color.darkGray);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("4 Gametes");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
         }
     }
 
     void quit(){
         //any handling that might need to take place here
         Configuration.writeConfigFile();
         System.exit(0);
     }
 
     void readAnalysisFile(File inFile){
         try{
             theData.readAnalysisTrack(inFile);
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioe){
             JOptionPane.showMessageDialog(this,
                     ioe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         dPrimeDisplay.computePreferredSize();
         if (dPrimeDisplay != null && tabs.getSelectedIndex() == VIEW_D_NUM){
             dPrimeDisplay.repaint();
         }
     }
 
     void readGenotypes(String[] inputOptions, int type){
         //input is a 2 element array with
         //inputOptions[0] = ped file
         //inputOptions[1] = info file (null if none)
         //type is either 3 or 4 for ped and hapmap files respectively
 
         final File inFile = new File(inputOptions[0]);
 
         try {
             if (inFile.length() < 1){
                 throw new HaploViewException("Genotype file is empty or nonexistent: " + inFile.getName());
             }
 
             if (type == HAPS){
                 //these are not available for non ped files
                 viewMenuItems[VIEW_CHECK_NUM].setEnabled(false);
                 viewMenuItems[VIEW_TDT_NUM].setEnabled(false);
                 Options.setAssocTest(ASSOC_NONE);
             }
             theData = new HaploData();
 
             if (type == HAPS){
                 theData.prepareHapsInput(new File(inputOptions[0]));
             }else{
                 theData.linkageToChrom(inFile, type);
             }
 
             //deal with marker information
             theData.infoKnown = false;
             File markerFile;
             if (inputOptions[1] == null){
                 markerFile = null;
             }else{
                 markerFile = new File(inputOptions[1]);
             }
 
             checkPanel = null;
             if (type == HAPS){
                 readMarkers(markerFile, null);
             }else{
                 readMarkers(markerFile, theData.getPedFile().getHMInfo());
                 checkPanel = new CheckDataPanel(theData, true);
                 checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
             }
 
 
             //let's start the math
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             final SwingWorker worker = new SwingWorker(){
                 public Object construct(){
                     dPrimeDisplay=null;
 
                     changeKey(1);
                     theData.generateDPrimeTable();
                     theData.guessBlocks(BLOX_GABRIEL);
                     //theData.guessBlocks(BLOX_NONE);  //for debugging, doesn't call blocks at first
                     colorMenuItems[0].setSelected(true);
                     blockMenuItems[0].setSelected(true);
                     zoomMenuItems[0].setSelected(true);
                     theData.blocksChanged = false;
                     Container contents = getContentPane();
                     contents.removeAll();
 
                     int currentTab = VIEW_D_NUM;
                     /*if (!(tabs == null)){
                     currentTab = tabs.getSelectedIndex();
                     } */
 
                     tabs = new JTabbedPane();
                     tabs.addChangeListener(new TabChangeListener());
 
                     //first, draw the D' picture
                     JPanel panel = new JPanel();
                     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                     dPrimeDisplay = new DPrimeDisplay(window);
                     JScrollPane dPrimeScroller = new JScrollPane(dPrimeDisplay);
                     dPrimeScroller.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                     dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
                     dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
                     panel.add(dPrimeScroller);
                     tabs.addTab(viewItems[VIEW_D_NUM], panel);
                     viewMenuItems[VIEW_D_NUM].setEnabled(true);
 
                     //compute and show haps on next tab
                     panel = new JPanel();
                     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                     try {
                         hapDisplay = new HaplotypeDisplay(theData);
                     } catch(HaploViewException e) {
                         JOptionPane.showMessageDialog(window,
                                 e.getMessage(),
                                 "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     HaplotypeDisplayController hdc =
                             new HaplotypeDisplayController(hapDisplay);
                     hapScroller = new JScrollPane(hapDisplay);
                     hapScroller.getVerticalScrollBar().setUnitIncrement(60);
                     hapScroller.getHorizontalScrollBar().setUnitIncrement(60);
                     panel.add(hapScroller);
                     panel.add(hdc);
                     tabs.addTab(viewItems[VIEW_HAP_NUM], panel);
                     viewMenuItems[VIEW_HAP_NUM].setEnabled(true);
                     displayMenu.setEnabled(true);
                     analysisMenu.setEnabled(true);
 
                     //LOD panel
                     /*panel = new JPanel();
                     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                     LODDisplay ld = new LODDisplay(theData);
                     JScrollPane lodScroller = new JScrollPane(ld);
                     panel.add(lodScroller);
                     tabs.addTab(viewItems[VIEW_LOD_NUM], panel);
                     viewMenuItems[VIEW_LOD_NUM].setEnabled(true);*/
 
                     //int optionalTabCount = 1;
 
 
 
                     //check data panel
                     if (checkPanel != null){
                         //optionalTabCount++;
                         //VIEW_CHECK_NUM = optionalTabCount;
                         //viewItems[VIEW_CHECK_NUM] = VIEW_CHECK_PANEL;
                         JPanel metaCheckPanel = new JPanel();
                         metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
                         JLabel countsLabel = new JLabel("Using " + theData.numSingletons + " singletons and "
                                 + theData.numTrios + " trios from "
                                 + theData.numPeds + " families.");
                         if (theData.numTrios + theData.numSingletons == 0){
                             countsLabel.setForeground(Color.red);
                         }
                         countsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                         metaCheckPanel.add(countsLabel);
                         metaCheckPanel.add(checkPanel);
                         cdc = new CheckDataController(window);
                         metaCheckPanel.add(cdc);
 
                         tabs.addTab(viewItems[VIEW_CHECK_NUM], metaCheckPanel);
                         viewMenuItems[VIEW_CHECK_NUM].setEnabled(true);
                         currentTab=VIEW_CHECK_NUM;
                     }
 
                     //TDT panel
                     if(Options.getAssocTest() != ASSOC_NONE) {
                         //optionalTabCount++;
                         //VIEW_TDT_NUM = optionalTabCount;
                         //viewItems[VIEW_TDT_NUM] = VIEW_TDT;
                         JTabbedPane metaAssoc = new JTabbedPane();
                         try{
                             tdtPanel = new TDTPanel(theData.getPedFile());
                         } catch(PedFileException e) {
                             JOptionPane.showMessageDialog(window,
                                     e.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                         metaAssoc.add("Single Marker", tdtPanel);
 
                         HaploAssocPanel htp = new HaploAssocPanel(theData.getHaplotypes());
                         metaAssoc.add("Haplotypes", htp);
 
                         tabs.addTab("Association Results", metaAssoc);
                         viewMenuItems[VIEW_TDT_NUM].setEnabled(true);
                     }
 
                     tabs.setSelectedIndex(currentTab);
                     contents.add(tabs);
 
                     repaint();
                     setVisible(true);
 
                     theData.finished = true;
                     setTitle(TITLE_STRING + " -- " + inFile.getName());
                     return null;
                 }
             };
 
             timer = new javax.swing.Timer(50, new ActionListener(){
                 public void actionPerformed(ActionEvent evt){
                     if (theData.finished){
                         timer.stop();
                         for (int i = 0; i < blockMenuItems.length; i++){
                             blockMenuItems[i].setEnabled(true);
                         }
                         clearBlocksItem.setEnabled(true);
                         readMarkerItem.setEnabled(true);
                         blocksItem.setEnabled(true);
                         exportMenuItems[2].setEnabled(true);
 
                         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                 }
             });
 
             worker.start();
             timer.start();
         }catch(IOException ioexec) {
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch(PedFileException pfe){
             JOptionPane.showMessageDialog(this,
                     pfe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     void readBlocksFile(File file) {
        try{
            Vector cust = theData.readBlocks(file);
            theData.guessBlocks(BLOX_CUSTOM, cust);
            changeBlocks(BLOX_CUSTOM);
        }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioe){
             JOptionPane.showMessageDialog(this,
                     ioe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     void readMarkers(File inputFile, String[][] hminfo){
         try {
             theData.prepareMarkerInput(inputFile, hminfo);
             if (theData.infoKnown){
                 analysisItem.setEnabled(true);
             }else{
                 analysisItem.setEnabled(false);
             }
             if (checkPanel != null){
                 //this is triggered when loading markers after already loading genotypes
                 //it is dumb and sucks, but at least it works. bah.
                 checkPanel = new CheckDataPanel(theData, true);
                 Container checkTab = (Container)tabs.getComponentAt(VIEW_CHECK_NUM);
                 checkTab.removeAll();
 
                 JPanel metaCheckPanel = new JPanel();
                 metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
                 JLabel countsLabel = new JLabel("Using " + theData.numSingletons + " singletons and "
                         + theData.numTrios + " trios from "
                         + theData.numPeds + " families.");
                 if (theData.numTrios + theData.numSingletons == 0){
                     countsLabel.setForeground(Color.red);
                 }
                 countsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 metaCheckPanel.add(countsLabel);
                 metaCheckPanel.add(checkPanel);
                 cdc = new CheckDataController(window);
                 metaCheckPanel.add(cdc);
 
                 checkTab.add(metaCheckPanel);
                 repaint();
             }
             if (tdtPanel != null){
                 tdtPanel.refreshNames();
             }
             if (dPrimeDisplay != null){
                 dPrimeDisplay.computePreferredSize();
             }
         }catch (HaploViewException e){
             JOptionPane.showMessageDialog(this,
                     e.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioexec){
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (PedFileException pfe){
         }
     }
 
     public int getCurrentBlockDef() {
         return currentBlockDef;
     }
 
     public void changeBlocks(int method){
         if (method == BLOX_NONE || method == BLOX_CUSTOM){
             blockMenuItems[BLOX_CUSTOM].setSelected(true);
         }
         if (method != BLOX_CUSTOM){
             theData.guessBlocks(method);
         }
 
         dPrimeDisplay.repaint();
         currentBlockDef = method;
 
         try{
             if (tabs.getSelectedIndex() == VIEW_HAP_NUM){
                 hapDisplay.getHaps();
             }
         }catch(HaploViewException hve) {
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         hapScroller.setViewportView(hapDisplay);
     }
 
     public void clearDisplays() {
         if (dPrimeDisplay != null){
             dPrimeDisplay.setVisible(false);
             dPrimeDisplay = null;
         }
         if (hapDisplay != null){
             hapDisplay.setVisible(false);
             hapDisplay = null;
         }
         if (tdtPanel != null){
             tdtPanel.setVisible(false);
             tdtPanel = null;
         }
     }
 
     class TabChangeListener implements ChangeListener{
         public void stateChanged(ChangeEvent e) {
             int tabNum = tabs.getSelectedIndex();
             if (tabNum == VIEW_D_NUM || tabNum == VIEW_HAP_NUM){
                 exportMenuItems[0].setEnabled(true);
                 exportMenuItems[1].setEnabled(true);
             }else if (tabNum == VIEW_TDT_NUM || tabNum == VIEW_CHECK_NUM){
                 exportMenuItems[0].setEnabled(true);
                 exportMenuItems[1].setEnabled(false);
             }else{
                 exportMenuItems[0].setEnabled(false);
                 exportMenuItems[1].setEnabled(false);
             }
 
             //if we've adjusted the haps display thresh we need to change the haps ass panel
             if (tabNum == VIEW_TDT_NUM){
                 JTabbedPane metaAssoc= (JTabbedPane)tabs.getComponentAt(tabNum);
                 //this is the haps ass tab inside the assoc super-tab
                 HaploAssocPanel htp = (HaploAssocPanel) metaAssoc.getComponent(1);
                 if (htp.initialHaplotypeDisplayThreshold != Options.getHaplotypeDisplayThreshold()){
                     htp.makeTable(theData.getHaplotypes());
                 }
             }
 
             if (tabNum == VIEW_D_NUM){
                 keyMenu.setEnabled(true);
             }else{
                 keyMenu.setEnabled(false);
             }
 
             viewMenuItems[tabs.getSelectedIndex()].setSelected(true);
 
             if (checkPanel != null && checkPanel.changed){
                 //first store up the current blocks
                 Vector currentBlocks = new Vector();
                 for (int blocks = 0; blocks < theData.blocks.size(); blocks++){
                     int thisBlock[] = (int[]) theData.blocks.elementAt(blocks);
                     int thisBlockReal[] = new int[thisBlock.length];
                     for (int marker = 0; marker < thisBlock.length; marker++){
                         thisBlockReal[marker] = Chromosome.realIndex[thisBlock[marker]];
                     }
                     currentBlocks.add(thisBlockReal);
                 }
 
                 window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 JTable table = checkPanel.getTable();
                 boolean[] markerResults = new boolean[table.getRowCount()];
                 for (int i = 0; i < table.getRowCount(); i++){
                     markerResults[i] = ((Boolean)table.getValueAt(i,CheckDataPanel.STATUS_COL)).booleanValue();
                 }
 
                 Chromosome.doFilter(markerResults);
 
 
                 //after editing the filtered marker list, needs to be prodded into
                 //resizing correctly
                 dPrimeDisplay.computePreferredSize();
                 dPrimeDisplay.colorDPrime(currentScheme);
 
                 hapDisplay.theData = theData;
 
                 if (currentBlockDef != BLOX_CUSTOM){
                     changeBlocks(currentBlockDef);
                 }else{
                     //adjust the blocks
                     Vector theBlocks = new Vector();
                     for (int x = 0; x < currentBlocks.size(); x++){
                         Vector goodies = new Vector();
                         int currentBlock[] = (int[])currentBlocks.elementAt(x);
                         for (int marker = 0; marker < currentBlock.length; marker++){
                             for (int y = 0; y < Chromosome.realIndex.length; y++){
                                 //we only keep markers from the input that are "good" from checkdata
                                 //we also realign the input file to the current "good" subset since input is
                                 //indexed of all possible markers in the dataset
                                 if (Chromosome.realIndex[y] == currentBlock[marker]){
                                     goodies.add(new Integer(y));
                                 }
                             }
                         }
                         int thisBlock[] = new int[goodies.size()];
                         for (int marker = 0; marker < thisBlock.length; marker++){
                             thisBlock[marker] = ((Integer)goodies.elementAt(marker)).intValue();
                         }
                         if (thisBlock.length > 1){
                             theBlocks.add(thisBlock);
                         }
                     }
                     theData.guessBlocks(BLOX_CUSTOM, theBlocks);
                 }
 
                 if (tdtPanel != null){
                     tdtPanel.refreshTable();
                 }
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 checkPanel.changed=false;
             }
 
             if (hapDisplay != null && theData.blocksChanged){
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try{
                     hapDisplay.getHaps();
                     if(Options.getAssocTest() != ASSOC_NONE) {
                         JTabbedPane metaAssoc= (JTabbedPane)tabs.getComponentAt(VIEW_TDT_NUM);
                         //this is the haps ass tab inside the assoc super-tab
                         HaploAssocPanel hasp = (HaploAssocPanel)metaAssoc.getComponent(1);
                         hasp.makeTable(theData.getHaplotypes());
                     }
                 }catch(HaploViewException hv){
                     JOptionPane.showMessageDialog(window,
                             hv.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
                 hapScroller.setViewportView(hapDisplay);
 
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 theData.blocksChanged = false;
             }
         }
     }
 
 
     void export(int tabNum, int format, int start, int stop){
         fc.setSelectedFile(new File(""));
         if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
             File outfile = fc.getSelectedFile();
             if (format == PNG_MODE || format == COMPRESSED_PNG_MODE){
                 BufferedImage image;
                 if (tabNum == VIEW_D_NUM){
                     if (format == PNG_MODE){
                         image = dPrimeDisplay.export(start, stop, false);
                     }else{
                         image = dPrimeDisplay.export(start, stop, true);
                     }
                 }else if (tabNum == VIEW_HAP_NUM){
                     image = hapDisplay.export();
                 }else{
                     image = new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
                 }
 
                 try{
                     String filename = outfile.getPath();
                     if (! (filename.endsWith(".png") || filename.endsWith(".PNG"))){
                         filename += ".png";
                     }
                     Jimi.putImage("image/png", image, filename);
                 }catch(JimiException je){
                     JOptionPane.showMessageDialog(this,
                             je.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             } else if (format == TXT_MODE){
                 try{
                     if (tabNum == VIEW_D_NUM){
                         theData.saveDprimeToText(outfile, TABLE_TYPE, start, stop);
                     }else if (tabNum == VIEW_HAP_NUM){
                         theData.saveHapsToText(hapDisplay.filteredHaplos,hapDisplay.multidprimeArray, outfile);
                     }else if (tabNum == VIEW_CHECK_NUM){
                         checkPanel.printTable(outfile);
                     }else if (tabNum == VIEW_TDT_NUM){
                         JTable table = tdtPanel.getTable();
                         JTreeTable jtt = ((HaploAssocPanel)((JTabbedPane)tabs.getComponent(tabNum)).
                                 getComponent(1)).jtt;
                         FileWriter assocWriter = new FileWriter(outfile);
                         int numCols = table.getColumnCount();
                         StringBuffer header = new StringBuffer("Single Marker Association\n");
                         for (int i = 0; i < numCols; i++){
                             header.append(table.getColumnName(i)).append("\t");
                         }
                         header.append("\n");
                         assocWriter.write(header.toString());
                         for (int i = 0; i < table.getRowCount(); i++){
                             StringBuffer sb = new StringBuffer();
                             for (int j = 0; j < numCols; j++){
                                 sb.append(table.getValueAt(i,j)).append("\t");
                             }
                             sb.append("\n");
                             assocWriter.write(sb.toString());
                         }
 
                         //now we write the haplotype association
                         numCols = jtt.getColumnCount();
                         header = new StringBuffer("\nHaplotype Association\n\t");
                         for (int i = 0; i < numCols; i++){
                             header.append(jtt.getColumnName(i)).append("\t");
                         }
                         header.append("\n");
                         assocWriter.write(header.toString());
                         HaplotypeAssociationModel ham = (HaplotypeAssociationModel) jtt.getTree().getModel();
                         HaplotypeAssociationNode root = (HaplotypeAssociationNode) ham.getRoot();
                         for(int i=0;i<ham.getChildCount(root);i++) {
                             HaplotypeAssociationNode curBlock = (HaplotypeAssociationNode) ham.getChild(root,i);
                             assocWriter.write(curBlock.getName() + "\n");
                             StringBuffer sb = new StringBuffer();
                             for(int j=0;j<ham.getChildCount(curBlock);j++){
                                 HaplotypeAssociationNode curHap = (HaplotypeAssociationNode) ham.getChild(curBlock,i);
                                 sb.append("\t").append(curHap.getName()).append("\t");
                                 sb.append(curHap.getFreq()).append("\t");
                                 sb.append(curHap.getCounts()).append("\t");
                                 sb.append(curHap.getChiSq()).append("\t");
                                 sb.append(curHap.getPVal()).append("\n");
                             }
                             assocWriter.write(sb.toString());
                         }
                         assocWriter.close();
                     }
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             ioe.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 
     void showHelp(){
 
         //Help Text:
         String helpText = new String();
         try{
             File helpFile = new File(System.getProperty("java.class.path") + File.separator + "haplohelp.txt");
             BufferedReader inHelp = new BufferedReader(new FileReader(helpFile));
             helpText = inHelp.readLine();
             String currentLine = new String();
             while ((currentLine = inHelp.readLine()) != null){
                 helpText += ("\n" + currentLine);
             }
             inHelp.close();
         }catch (IOException ioexec){
             helpText = "Help file not found.\n";
         }
 
         JFrame helpFrame = new JFrame("HaploView Help");
         JTextArea helpTextArea = new JTextArea();
         JScrollPane helpDisplayPanel = new JScrollPane(helpTextArea);
         helpDisplayPanel.setBackground(Color.white);
         helpTextArea.setText(helpText);
         helpDisplayPanel.setOpaque(true);
         helpDisplayPanel.setPreferredSize(new Dimension(450,500));
         helpFrame.setContentPane(helpDisplayPanel);
         helpFrame.pack();
         helpFrame.setVisible(true);
     }
 
     public static void main(String[] args) {
         boolean nogui = false;
         //HaploView window;
         for(int i = 0;i<args.length;i++) {
            if(args[i].equals("-n") || args[i].equals("-h") || args[i].equals("-help")) {
                 nogui = true;
             }
         }
         if(nogui) {
             HaploText textOnly = new HaploText(args);
         } else {
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) { }
             //System.setProperty("swing.disableFileChooserSpeedFix", "true");
 
             window  =  new HaploView();
 
             //setup view object
             window.setTitle(TITLE_STRING);
             window.setSize(800,600);
 
             //center the window on the screen
             Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
             window.setLocation((screen.width - window.getWidth()) / 2,
                     (screen.height - window.getHeight()) / 2);
 
             window.setVisible(true);
 
             //parse command line stuff for input files or prompt data dialog
             HaploText argParser = new HaploText(args);
             String[] inputArray = new String[2];
             if (argParser.getHapsFileName() != null){
                 inputArray[0] = argParser.getHapsFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 window.readGenotypes(inputArray, HAPS);
             }else if (argParser.getPedFileName() != null){
                 inputArray[0] = argParser.getPedFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 window.readGenotypes(inputArray, PED);
             }else if (argParser.getHapmapFileName() != null){
                 inputArray[0] = argParser.getHapmapFileName();
                 inputArray[1] = "";
                 window.readGenotypes(inputArray, HMP);
             }else{
                 ReadDataDialog readDialog = new ReadDataDialog("Welcome to HaploView", window);
                 readDialog.pack();
                 readDialog.setVisible(true);
             }
         }
     }
 
 
 }
 
