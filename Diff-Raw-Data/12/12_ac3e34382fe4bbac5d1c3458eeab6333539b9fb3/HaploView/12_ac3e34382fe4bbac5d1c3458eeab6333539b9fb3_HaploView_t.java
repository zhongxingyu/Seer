 package edu.mit.wi.haploview;
 
 
 import edu.mit.wi.pedfile.PedFileException;
 import edu.mit.wi.haploview.association.*;
 import edu.mit.wi.haploview.tagger.TaggerConfigPanel;
 import edu.mit.wi.haploview.tagger.TaggerResultsPanel;
 
 import javax.help.*;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import java.net.URL;
 
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 
 public class HaploView extends JFrame implements ActionListener, Constants{
 
     boolean DEBUG = false;
 
     JMenuItem readMarkerItem, analysisItem, blocksItem, gbrowseItem, spacingItem, gbEditItem;
     String exportItems[] = {
         EXPORT_TEXT, EXPORT_PNG, EXPORT_OPTIONS
     };
     JMenuItem exportMenuItems[];
     JMenu keyMenu, displayMenu, analysisMenu;
     JMenuItem clearBlocksItem;
 
     String viewItems[] = {
         VIEW_DPRIME, VIEW_HAPLOTYPES, VIEW_CHECK_PANEL, VIEW_TAGGER, VIEW_ASSOC
     };
     JRadioButtonMenuItem viewMenuItems[];
     String zoomItems[] = {
         "Zoomed", "Medium", "Unzoomed"
     };
     JRadioButtonMenuItem zoomMenuItems[];
     String colorItems[] = {
         "Standard (D' / LOD)", "R-squared", "D' / LOD (alt)", "Confidence bounds", "4 Gamete", "GOLD heatmap"
     };
     JRadioButtonMenuItem colorMenuItems[];
     JRadioButtonMenuItem blockMenuItems[];
     String blockItems[] = {"Confidence intervals (Gabriel et al)",
                            "Four Gamete Rule",
                            "Solid spine of LD",
                            "Custom"};
     String printValueItems[] = {
             "D'","R-squared","None"
     };
     JRadioButtonMenuItem printValueMenuItems[];
 
 
     HaploData theData;
 
     private int currentBlockDef = BLOX_GABRIEL;
     private javax.swing.Timer timer;
 
     static HaploView window;
     public static JFileChooser fc;
     private JScrollPane hapScroller;
     HaploviewTabbedPane tabs;
 
     DPrimeDisplay dPrimeDisplay;
     HaplotypeDisplay hapDisplay;
     CheckDataPanel checkPanel;
     CustomAssocPanel custAssocPanel;
     TDTPanel tdtPanel;
     HaploAssocPanel hapAssocPanel;
     PermutationTestPanel permutationPanel;
     TaggerResultsPanel taggerResultsPanel;
     private TaggerConfigPanel taggerConfigPanel;
 
     JProgressBar haploProgress;
     boolean isMaxSet = false;
     JPanel progressPanel = new JPanel();
     LayoutManager defaultLayout = new GridBagLayout();
 
     public HaploView(){
         try{
             fc = new JFileChooser(System.getProperty("user.dir"));
         }catch(NullPointerException n){
             try{
                 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                 fc = new JFileChooser(System.getProperty("user.dir"));
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             }catch(Exception e){
                 JOptionPane.showMessageDialog(this,
                         e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
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
 
         gbrowseItem = new JMenuItem(DOWNLOAD_GBROWSE);
         gbrowseItem.addActionListener(this);
         gbrowseItem.setEnabled(false);
         fileMenu.add(gbrowseItem);
 
         fileMenu.addSeparator();
 
         exportMenuItems = new JMenuItem[exportItems.length];
         for (int i = 0; i < exportItems.length; i++) {
             exportMenuItems[i] = new JMenuItem(exportItems[i]);
             exportMenuItems[i].addActionListener(this);
             exportMenuItems[i].setEnabled(false);
             fileMenu.add(exportMenuItems[i]);
         }
 
         //update menu item
         fileMenu.addSeparator();
         menuItem = new JMenuItem("Check for update");
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
 
         fileMenu.addSeparator();
         menuItem = new JMenuItem("Quit");
         setAccelerator(menuItem, 'Q', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         fileMenu.setMnemonic(KeyEvent.VK_F);
 
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
         colorMenuItems[Options.getLDColorScheme()].setSelected(true);
         displayMenu.add(colorMenu);
 
         ButtonGroup pg = new ButtonGroup();
         JMenu printValueMenu = new JMenu("Show LD values");
         printValueMenu.setMnemonic(KeyEvent.VK_V);
         printValueMenuItems = new JRadioButtonMenuItem[printValueItems.length];
         for (int i = 0; i< printValueItems.length; i++){
             printValueMenuItems[i] = new JRadioButtonMenuItem(printValueItems[i],i==0);
             printValueMenuItems[i].addActionListener(this);
             printValueMenuItems[i].setActionCommand("printvalue" + i);
             printValueMenu.add(printValueMenuItems[i]);
             pg.add(printValueMenuItems[i]);
         }
         printValueMenuItems[Options.getPrintWhat()].setSelected(true);
         displayMenu.add(printValueMenu);
 
         spacingItem = new JMenuItem("LD Display Spacing");
         spacingItem.setMnemonic(KeyEvent.VK_S);
         spacingItem.addActionListener(this);
         spacingItem.setEnabled(false);
         displayMenu.add(spacingItem);
 
         //gbrowse options editor
         gbEditItem = new JMenuItem(GBROWSE_OPTS);
         gbEditItem.setMnemonic(KeyEvent.VK_H);
         gbEditItem.addActionListener(this);
         gbEditItem.setEnabled(false);
         displayMenu.add(gbEditItem);
 
         //show block tag pooper?
         JCheckBoxMenuItem pooper = new JCheckBoxMenuItem("Show tags in blocks");
         pooper.addActionListener(this);
         displayMenu.add(pooper);
 
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
 
         JMenu helpMenu = new JMenu("Help");
         helpMenu.setMnemonic(KeyEvent.VK_H);
         menuBar.add(helpMenu);
 
         JMenuItem helpContentsItem;
         helpContentsItem = new JMenuItem("Help Contents");
         HelpSet hs;
         HelpBroker hb;
         String helpHS = "jhelpset.hs";
         try {
             URL hsURL = HelpSet.findHelpSet(HaploView.class.getClassLoader(), helpHS);
             hs = new HelpSet(null, hsURL);
         } catch (Exception ee) {
             System.out.println( "HelpSet " + ee.getMessage());
             System.out.println("HelpSet "+ helpHS +" not found");
             return;
         }
         hb = hs.createHelpBroker();
         helpContentsItem.addActionListener(new CSH.DisplayHelpFromSource(hb));
         helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
         helpMenu.add(helpContentsItem);
 
         menuItem = new JMenuItem("About Haploview");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
 
 
         //color key
         keyMenu = new JMenu("Key");
         menuBar.add(Box.createHorizontalGlue());
         menuBar.add(keyMenu);
 
 
 
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
         }else if (command.equals(DOWNLOAD_GBROWSE)){
             GBrowseDialog gbd = new GBrowseDialog(this, "Connect to HapMap Info Server");
             gbd.pack();
             gbd.setVisible(true);
         }else if (command.equals(GBROWSE_OPTS)){
             GBrowseOptionDialog gbod = new GBrowseOptionDialog(this, "HapMap Info Track Options");
             gbod.pack();
             gbod.setVisible(true);
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
 
             //zooming clauses
         }else if (command.startsWith("zoom")){
             dPrimeDisplay.zoom(Integer.valueOf(command.substring(4)).intValue());
 
             //coloring clauses
         }else if (command.startsWith("color")){
             Options.setLDColorScheme(Integer.valueOf(command.substring(5)).intValue());
             dPrimeDisplay.colorDPrime();
             changeKey();
 
             //which LD value to print.
         }else if (command.startsWith("printvalue")){
             Options.setPrintWhat(Integer.valueOf(command.substring(10)).intValue());
             dPrimeDisplay.colorDPrime();
 
             //exporting clauses
         }else if (command.equals(EXPORT_PNG)){
             export(tabs.getSelectedPrimary(), PNG_MODE, 0, Chromosome.getUnfilteredSize());
         }else if (command.equals(EXPORT_TEXT)){
             export(tabs.getSelectedPrimary(), TXT_MODE, 0, Chromosome.getUnfilteredSize());
         }else if (command.equals(EXPORT_OPTIONS)){
             ExportDialog exDialog = new ExportDialog(this);
             exDialog.pack();
             exDialog.setVisible(true);
         }else if (command.equals("Show tags in blocks")){
             Options.setShowBlockTags(((JCheckBoxMenuItem)e.getSource()).getState());
             hapDisplay.repaint();
         }else if (command.equals("LD Display Spacing")){
             ProportionalSpacingDialog spaceDialog = new ProportionalSpacingDialog(this, "Adjust LD Spacing");
             spaceDialog.pack();
             spaceDialog.setVisible(true);
         }else if (command.equals("About Haploview")){
             JOptionPane.showMessageDialog(this,
                     ABOUT_STRING,
                     "About Haploview",
                     JOptionPane.INFORMATION_MESSAGE);
         } else if(command.equals("Check for update")) {
             final SwingWorker worker = new SwingWorker(){
                 UpdateChecker uc;
                 String unableToConnect;
                 public Object construct() {
                     uc = new UpdateChecker();
                     try {
                         uc.checkForUpdate();
                     } catch(IOException ioe) {
                         unableToConnect = ioe.getMessage();
                     }
                     return null;
                 }
                 public void finished() {
                     window.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                     if(uc != null) {
                         if(unableToConnect != null) {
                             JOptionPane.showMessageDialog(window,
                                     "An error occured while checking for update.\n " + unableToConnect ,
                                     "Update Check",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                         else if(uc.isNewVersionAvailable()) {
                             UpdateDisplayDialog udp = new UpdateDisplayDialog(window,"Update Check",uc);
                             udp.pack();
                             udp.setVisible(true);
                         } else {
                             JOptionPane.showMessageDialog(window,
                                     "Your version of Haploview is up to date.",
                                     "Update Check",
                                     JOptionPane.INFORMATION_MESSAGE);
                         }
                     }
 
                 }
             };
             setCursor(new Cursor(Cursor.WAIT_CURSOR));
             worker.start();
         }else if (command.equals("Quit")){
             quit();
         } else {
             for (int i = 0; i < tabs.getTabCount(); i++) {
                 if (command.equals(tabs.getTitleAt(i))){
                     tabs.setSelectedIndex(i);
                     break;
                 }
             }
         }
     }
 
     private void changeKey() {
         int scheme = Options.getLDColorScheme();
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
         } else if (scheme == GAB_SCHEME){
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
         }else if (scheme == GOLD_SCHEME){
             JMenuItem keyItem = new JMenuItem("High D'");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.red);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
             keyItem.setPreferredSize(size);
             keyItem.setBackground(new Color(255,255,0));
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
             keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.green);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
             keyItem.setPreferredSize(size);
             keyItem.setBackground(new Color(0,255,255));
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Low D'");
             keyItem.setForeground(Color.white);
             keyItem.setBackground(new Color(0,0,127));
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
         if (dPrimeDisplay != null && tabs.getTitleAt(tabs.getSelectedIndex()).equals(VIEW_DPRIME)){
             dPrimeDisplay.repaint();
         }
     }
 
     void readGenotypes(String[] inputOptions, int type){
         //input is a 2 element array with
         //inputOptions[0] = ped file
         //inputOptions[1] = info file (null if none)
         //inputOptions[2] = custom association test list file (null if none)
         //type is either 3 or 4 for ped and hapmap files respectively
         final File inFile = new File(inputOptions[0]);
         final AssociationTestSet customAssocSet;
 
         try {
             if (inputOptions[2] != null && inputOptions[1] == null){
                 throw new HaploViewException("A marker information file is required if a tests file is specified.");
             }
 
             if (inputOptions[1] == null && Options.getAssocTest() != ASSOC_NONE){
                 throw new HaploViewException("A marker information file is required for association tests.");
             }
 
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             if (inFile.length() < 1){
                 throw new HaploViewException("Genotype file is empty or nonexistent: " + inFile.getName());
             }
 
             if (type == HAPS_FILE){
                 //these are not available for non ped files
                 viewMenuItems[VIEW_CHECK_NUM].setEnabled(false);
                 viewMenuItems[VIEW_ASSOC_NUM].setEnabled(false);
                 Options.setAssocTest(ASSOC_NONE);
             }
             theData = new HaploData();
 
             if (type == HAPS_FILE){
                 theData.prepareHapsInput(new File(inputOptions[0]));
             }else{
                 theData.linkageToChrom(inFile, type);
             }
 
             if(theData.getPedFile().isBogusParents()) {
                 JOptionPane.showMessageDialog(this,
                         "One or more individuals in the file reference non-existent parents.\nThese references have been ignored.",
                         "File Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
             if(theData.getPedFile().isHaploidHets()) {
                 JOptionPane.showMessageDialog(this,
                        "At least one male in the file is heterozygous.\nThese genotypes have been ignored.",
                         "File Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
 
             //deal with marker information
             theData.infoKnown = false;
             File markerFile;
             if (inputOptions[1] == null){
                 markerFile = null;
             }else{
                 markerFile = new File(inputOptions[1]);
             }
 
             //turn on/off gbrowse menu
             if (Options.isGBrowseShown()){
                 gbEditItem.setEnabled(true);
             }else{
                 gbEditItem.setEnabled(false);
             }
 
             if (type == HAPS_FILE){
                 readMarkers(markerFile, null);
                 HashSet emptyHashSet = new HashSet();
 
                 //initialize realIndex
                 Chromosome.doFilter(Chromosome.getUnfilteredSize());
                 customAssocSet = null;
                 theData.getPedFile().setWhiteList(emptyHashSet);
                 checkPanel = new CheckDataPanel(this);
             }else{
                 readMarkers(markerFile, theData.getPedFile().getHMInfo());
                 //we read the file in first, so we can whitelist all the markers in the custom test set
                 HashSet whiteListedCustomMarkers = new HashSet();
                 if (inputOptions[2] != null){
                     customAssocSet = new AssociationTestSet(inputOptions[2]);
                     whiteListedCustomMarkers = customAssocSet.getWhitelist();
                 }else{
                     customAssocSet = null;
                 }
                 theData.getPedFile().setWhiteList(whiteListedCustomMarkers);
 
                 checkPanel = new CheckDataPanel(this);
                 checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
                 //set up the indexing to take into account skipped markers.
                 Chromosome.doFilter(checkPanel.getMarkerResults());
             }
 
 
             //let's start the math
             final SwingWorker worker = new SwingWorker(){
                 public Object construct(){
                     Container contents = getContentPane();
                     contents.removeAll();
                     contents.repaint();
                     defaultLayout = contents.getLayout();
                     contents.setLayout(new GridBagLayout());
                     haploProgress = new JProgressBar(0,2);
                     haploProgress.setValue(0);
                     haploProgress.setStringPainted(true);
                     haploProgress.setForeground(new Color(40,40,255));
                     haploProgress.setPreferredSize(new Dimension(250,20));
                     progressPanel.setLayout(new BoxLayout(progressPanel,BoxLayout.Y_AXIS));
                     JLabel progressLabel = new JLabel("Loading data...");
                     progressPanel.add(progressLabel);
                     progressLabel.setAlignmentX(CENTER_ALIGNMENT);
                     progressPanel.add(haploProgress);
                     contents.add(progressPanel);
                     progressPanel.revalidate();
 
                     for (int i = 0; i < viewMenuItems.length; i++){
                         viewMenuItems[i].setEnabled(false);
                     }
                     dPrimeDisplay=null;
 
                     changeKey();
                     theData.generateDPrimeTable();
                     theData.guessBlocks(BLOX_GABRIEL);
                     //theData.guessBlocks(BLOX_NONE);  //for debugging, doesn't call blocks at first
 
                     blockMenuItems[0].setSelected(true);
                     zoomMenuItems[0].setSelected(true);
                     theData.blocksChanged = false;
                     contents = getContentPane();
                     contents.removeAll();
 
                     tabs = new HaploviewTabbedPane();
                     tabs.addChangeListener(new TabChangeListener());
 
                     //first, draw the D' picture
                     dPrimeDisplay = new DPrimeDisplay(window);
                     JScrollPane dPrimeScroller = new JScrollPane(dPrimeDisplay);
                     dPrimeScroller.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                     dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
                     dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
                     HaploviewTab ldTab = new HaploviewTab(dPrimeDisplay);
                     ldTab.add(dPrimeScroller);
                     tabs.addTab(VIEW_DPRIME, ldTab);
                     viewMenuItems[VIEW_D_NUM].setEnabled(true);
 
                     //compute and show haps on next tab
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
                     HaploviewTab hapsTab = new HaploviewTab(hapDisplay);
                     hapsTab.add(hapScroller);
                     hapsTab.add(hdc);
                     tabs.addTab(VIEW_HAPLOTYPES, hapsTab);
                     viewMenuItems[VIEW_HAP_NUM].setEnabled(true);
                     displayMenu.setEnabled(true);
                     analysisMenu.setEnabled(true);
 
                     //check data panel
                     HaploviewTab checkTab = new HaploviewTab(checkPanel);
                     checkTab.add(checkPanel);
                     CheckDataController cdc = new CheckDataController(checkPanel);
                     checkTab.add(cdc);
 
                     tabs.addTab(VIEW_CHECK_PANEL, checkTab);
                     viewMenuItems[VIEW_CHECK_NUM].setEnabled(true);
                     tabs.setSelectedComponent(checkTab);
 
 
                     //only show tagger if we have a .info file
                     if (theData.infoKnown){
                         //tagger display
                         taggerConfigPanel = new TaggerConfigPanel(theData);
                         HaploviewTabbedPane tagTabs = new HaploviewTabbedPane();
                         tagTabs.add("Configuration",taggerConfigPanel);
 
                         taggerResultsPanel = new TaggerResultsPanel();
                         taggerConfigPanel.addActionListener(taggerResultsPanel);
                         tagTabs.addTab("Results",taggerResultsPanel);
 
                         HaploviewTab taggerTab = new HaploviewTab(tagTabs);
                         taggerTab.add(tagTabs);
                         tabs.addTab(VIEW_TAGGER,taggerTab);
                         viewMenuItems[VIEW_TAGGER_NUM].setEnabled(true);
                     }
 
                     //Association panel
                     if(Options.getAssocTest() != ASSOC_NONE) {
                         HaploviewTabbedPane metaAssoc = new HaploviewTabbedPane();
                         try{
                             tdtPanel = new TDTPanel(new AssociationTestSet(theData.getPedFile(), null,null, Chromosome.getAllMarkers()));
                         } catch(PedFileException e) {
                             JOptionPane.showMessageDialog(window,
                                     e.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
 
 
                         metaAssoc.add("Single Marker", tdtPanel);
 
                         if (!Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                             hapAssocPanel = new HaploAssocPanel(new AssociationTestSet(theData.getHaplotypes(), null));
                         }else{
                             hapAssocPanel = new HaploAssocPanel(new AssociationTestSet(null,null));
                         }
                         metaAssoc.add("Haplotypes", hapAssocPanel);
 
                         //custom association tests
                         custAssocPanel = null;
                         if(customAssocSet != null) {
                             try {
                                 customAssocSet.runFileTests(theData, tdtPanel.getTestSet().getMarkerAssociationResults());
                                 custAssocPanel = new CustomAssocPanel(customAssocSet);
                                 metaAssoc.addTab("Custom",custAssocPanel);
                                 metaAssoc.setSelectedComponent(custAssocPanel);
                             } catch (HaploViewException e) {
                                 JOptionPane.showMessageDialog(window,
                                     e.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                             }
                         }
 
                         AssociationTestSet custPermSet = null;
                         if (custAssocPanel != null){
                             custPermSet = custAssocPanel.getTestSet();
                         }
                         AssociationTestSet permSet = new AssociationTestSet();
                         permSet.cat(tdtPanel.getTestSet());
                         permSet.cat(hapAssocPanel.getTestSet());
 
                         permutationPanel = new PermutationTestPanel(
                                 new PermutationTestSet(0,theData.getPedFile(),custPermSet, permSet));
                         metaAssoc.add(permutationPanel,"Permutation Tests");
 
                         HaploviewTab associationTab = new HaploviewTab(metaAssoc);
                         associationTab.add(metaAssoc);
                         tabs.addTab(VIEW_ASSOC, associationTab);
                         viewMenuItems[VIEW_ASSOC_NUM].setEnabled(true);
 
                     }
 
 
                     contents.remove(progressPanel);
                     contents.setLayout(defaultLayout);
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
                     if (isMaxSet == true){
                         haploProgress.setValue(theData.dPrimeCount);
                     }
                     if (theData.finished){
                         timer.stop();
                         for (int i = 0; i < blockMenuItems.length; i++){
                             blockMenuItems[i].setEnabled(true);
                         }
                         clearBlocksItem.setEnabled(true);
                         readMarkerItem.setEnabled(true);
                         blocksItem.setEnabled(true);
                         exportMenuItems[2].setEnabled(true);
                         progressPanel.removeAll();
                         isMaxSet = false;
                         theData.dPrimeCount = 0;
                         theData.dPrimeTotalCount = -1;
 
                         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                     if (theData.dPrimeTotalCount != -1 && isMaxSet == false){
                         haploProgress.setMaximum(theData.dPrimeTotalCount);
                         isMaxSet = true;
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
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }catch(PedFileException pfe){
             JOptionPane.showMessageDialog(this,
                     pfe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
                 gbrowseItem.setEnabled(true);
                 spacingItem.setEnabled(true);
             }else{
                 analysisItem.setEnabled(false);
                 gbrowseItem.setEnabled(false);
                 spacingItem.setEnabled(false);
             }
             if (checkPanel != null){
                 //this is triggered when loading markers after already loading genotypes
                 //it is dumb and sucks, but at least it works. bah.
                 checkPanel = new CheckDataPanel(this);
                 HaploviewTab checkTab = ((HaploviewTab)tabs.getComponentAt(VIEW_CHECK_NUM));
                 checkTab.removeAll();
 
                 JPanel metaCheckPanel = new JPanel();
                 metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
                 metaCheckPanel.add(checkPanel);
                 CheckDataController cdc = new CheckDataController(checkPanel);
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
             if (tabs.getTitleAt(tabs.getSelectedIndex()).equals(VIEW_HAPLOTYPES)){
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
         if (tabs != null){
             tabs.removeAll();
             dPrimeDisplay = null;
             hapDisplay = null;
             tdtPanel = null;
             checkPanel = null;
         }
     }
 
     class TabChangeListener implements ChangeListener{
         public void stateChanged(ChangeEvent e) {
             if (tabs.getSelectedIndex() != -1){
                 window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                 String title = tabs.getTitleAt(tabs.getSelectedIndex());
                 if (title.equals(VIEW_DPRIME) || title.equals(VIEW_HAPLOTYPES)){
                     exportMenuItems[0].setEnabled(true);
                     exportMenuItems[1].setEnabled(true);
                 }else if (title.equals(VIEW_ASSOC) || title.equals(VIEW_CHECK_PANEL) || title.equals(VIEW_TAGGER)){
                     exportMenuItems[0].setEnabled(true);
                     exportMenuItems[1].setEnabled(false);
                 }else{
                     exportMenuItems[0].setEnabled(false);
                     exportMenuItems[1].setEnabled(false);
                 }
 
                 //if we've adjusted the haps display thresh we need to change the haps ass panel
                 if (title.equals(VIEW_ASSOC)){
                     JTabbedPane metaAssoc = ((JTabbedPane)((HaploviewTab)tabs.getSelectedComponent()).getComponent(0));
                     //this is the haps ass tab inside the assoc super-tab
                     HaploAssocPanel htp = (HaploAssocPanel) metaAssoc.getComponent(1);
                     if (htp.initialHaplotypeDisplayThreshold != Options.getHaplotypeDisplayThreshold() &&
                             !Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                         htp.makeTable(new AssociationTestSet(theData.getHaplotypes(), null));
                         permutationPanel.setBlocksChanged();
                         AssociationTestSet custSet = null;
                         if (custAssocPanel != null){
                             custSet = custAssocPanel.getTestSet();
                         }
                         AssociationTestSet permSet = new AssociationTestSet();
                         permSet.cat(tdtPanel.getTestSet());
                         permSet.cat(hapAssocPanel.getTestSet());
                         permutationPanel.setTestSet(
                                 new PermutationTestSet(0,theData.getPedFile(),custSet,permSet));
                     }
                 }
 
                 if (title.equals(VIEW_DPRIME)){
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
 
                     Chromosome.doFilter(checkPanel.getMarkerResults());
 
                     //after editing the filtered marker list, needs to be prodded into
                     //resizing correctly
                     dPrimeDisplay.computePreferredSize();
                     dPrimeDisplay.colorDPrime();
                     dPrimeDisplay.revalidate();
 
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
 
                     if (taggerConfigPanel != null){
                         taggerConfigPanel.refreshTable();
                     }
 
                     if(permutationPanel != null) {
                         permutationPanel.setBlocksChanged();
                         AssociationTestSet custSet = null;
                         if (custAssocPanel != null){
                             custSet = custAssocPanel.getTestSet();
                         }
                         AssociationTestSet permSet = new AssociationTestSet();
                         permSet.cat(tdtPanel.getTestSet());
                         permSet.cat(hapAssocPanel.getTestSet());
                         permutationPanel.setTestSet(
                                 new PermutationTestSet(0,theData.getPedFile(),custSet,permSet));
                     }
 
                     checkPanel.changed=false;
                 }
 
                 if (hapDisplay != null && theData.blocksChanged){
                     try{
                         hapDisplay.getHaps();
                         if(Options.getAssocTest() != ASSOC_NONE &&
                                 !Chromosome.getDataChrom().equalsIgnoreCase("chrx")) {
                             //this is the haps ass tab inside the assoc super-tab
                             hapAssocPanel.makeTable(new AssociationTestSet(theData.getHaplotypes(), null));
 
                             permutationPanel.setBlocksChanged();
                             AssociationTestSet custSet = null;
                             if (custAssocPanel != null){
                                 custSet = custAssocPanel.getTestSet();
                             }
                             AssociationTestSet permSet = new AssociationTestSet();
                             permSet.cat(tdtPanel.getTestSet());
                             permSet.cat(hapAssocPanel.getTestSet());
                             permutationPanel.setTestSet(
                                     new PermutationTestSet(0,theData.getPedFile(),custSet,permSet));
                         }
                     }catch(HaploViewException hv){
                         JOptionPane.showMessageDialog(window,
                                 hv.getMessage(),
                                 "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     hapScroller.setViewportView(hapDisplay);
 
                     theData.blocksChanged = false;
                 }
                 if (theData.finished){
                     setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 }
             }
         }
     }
 
     void export(Component c, int format, int start, int stop){
         if (c == null) return;
 
         fc.setSelectedFile(new File(""));
         if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
             File outfile = fc.getSelectedFile();
 
             if (format == PNG_MODE || format == COMPRESSED_PNG_MODE){
                 BufferedImage image = null;
                 if (c.equals(dPrimeDisplay)){
                     try {
                         if (format == PNG_MODE){
                             image = dPrimeDisplay.export(start, stop, false);
                         }else{
                             image = dPrimeDisplay.export(start, stop, true);
                         }
                     } catch(HaploViewException hve) {
                         JOptionPane.showMessageDialog(this,
                                 hve.getMessage(),
                                 "Export Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }else if (c.equals(hapDisplay)){
                     image = hapDisplay.export();
                 }else{
                     image = new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
                 }
 
                 if (image != null){
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
                 }
             } else if (format == TXT_MODE){
                 try{
                     if (c.equals(dPrimeDisplay)){
                         theData.saveDprimeToText(outfile, TABLE_TYPE, start, stop);
                     }else if (c.equals(hapDisplay)){
                         theData.saveHapsToText(hapDisplay.filteredHaplos,hapDisplay.multidprimeArray, outfile);
                     }else if (c.equals(checkPanel)){
                         theData.getPedFile().saveCheckDataToText(outfile);
                     }else if (c.equals(tdtPanel)){
                         tdtPanel.getTestSet().saveSNPsToText(outfile);
                     }else if (c.equals(hapAssocPanel)){
                         hapAssocPanel.getTestSet().saveHapsToText(outfile);
                     }else if (c.equals(permutationPanel)){
                         permutationPanel.export(outfile);
                     }else if (c.equals(custAssocPanel)){
                         custAssocPanel.getTestSet().saveResultsToText(outfile);
                     }else if (c.equals(taggerConfigPanel) || c.equals(taggerResultsPanel)){
                         taggerConfigPanel.export(outfile);
                     }
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             ioe.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }catch(HaploViewException he){
                     JOptionPane.showMessageDialog(this,
                             he.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 
     public static void main(String[] args) {
         //this parses the command line arguments. if nogui mode is specified,
         //then haploText will execute whatever the user specified
         HaploText argParser = new HaploText(args);
 
         //if nogui is specified, then HaploText has already executed everything, and let Main() return
         //otherwise, we want to actually load and run the gui
         if(!argParser.isNogui()) {
             try {
                 UIManager.put("EditorPane.selectionBackground",Color.lightGray);
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(window,
                         e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
             window  =  new HaploView();
 
             //setup view object
             window.setTitle(TITLE_STRING);
             window.setSize(800,600);
 
             final SwingWorker worker = new SwingWorker(){
                 UpdateChecker uc;
                 public Object construct() {
                     uc = new UpdateChecker();
                     try {
                         uc.checkForUpdate();
                     } catch(IOException ioe) {
                         //this means we couldnt connect but we want it to die quietly
                     }
                     return null;
                 }
                 public void finished() {
                     if(uc != null) {
                         if(uc.isNewVersionAvailable()) {
                             //theres an update available so lets pop some crap up
                             final JLayeredPane jlp = window.getLayeredPane();
 
                             final JPanel udp = new JPanel();
                             udp.setLayout(new BoxLayout(udp, BoxLayout.Y_AXIS));
                             double version = uc.getNewVersion();
                             Font detailsFont = new Font("Default", Font.PLAIN, 14);
                             JLabel announceLabel = new JLabel("A newer version of Haploview (" +version+") is available.");
                             announceLabel.setFont(detailsFont);
                             JLabel detailsLabel = new JLabel("See \"Check for update\" in the file menu for details.");
                             detailsLabel.setFont(detailsFont);
                             udp.add(announceLabel);
                             udp.add(detailsLabel);
 
                             udp.setBorder(BorderFactory.createRaisedBevelBorder());
                             int width = udp.getPreferredSize().width;
                             int height = udp.getPreferredSize().height;
                             int borderwidth = udp.getBorder().getBorderInsets(udp).right;
                             int borderheight = udp.getBorder().getBorderInsets(udp).bottom;
                             udp.setBounds(jlp.getWidth()-width-borderwidth, jlp.getHeight()-height-borderheight,
                                    udp.getPreferredSize().width, udp.getPreferredSize().height);
                             udp.setOpaque(true);
 
                             jlp.add(udp, JLayeredPane.POPUP_LAYER);
 
                             java.util.Timer updateTimer = new java.util.Timer();
                             //show this update message for 6.5 seconds
                             updateTimer.schedule(new TimerTask() {
                                 public void run() {
                                     jlp.remove(udp);
                                     jlp.repaint();
                                 }
                             },6000);
 
                         }
                     }
                 }
             };
 
 
 
             //center the window on the screen
             Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
             window.setLocation((screen.width - window.getWidth()) / 2,
                     (screen.height - window.getHeight()) / 2);
 
             window.setVisible(true);
             worker.start();
 
 
             //parse command line stuff for input files or prompt data dialog
             String[] inputArray = new String[3];
             if (argParser.getHapsFileName() != null){
                 inputArray[0] = argParser.getHapsFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 inputArray[2] = null;
                 window.readGenotypes(inputArray, HAPS_FILE);
             }else if (argParser.getPedFileName() != null){
                 inputArray[0] = argParser.getPedFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 inputArray[2] = null;
                 window.readGenotypes(inputArray, PED_FILE);
             }else if (argParser.getHapmapFileName() != null){
                 inputArray[0] = argParser.getHapmapFileName();
                 inputArray[1] = null;
                 inputArray[2] = null;
                 window.readGenotypes(inputArray, HMP_FILE);
             }else{
                 ReadDataDialog readDialog = new ReadDataDialog("Welcome to HaploView", window);
                 readDialog.pack();
                 readDialog.setVisible(true);
             }
         }
     }
 
     class HaploviewTabbedPane extends JTabbedPane{
 
         public Component getSelectedPrimary(){
             //find selected component recursively in case there are tabs of tabs of tabs...
             //return the primary component of the HaploviewTab, as opposed to the tab itself
             Component c = ((HaploviewTab)getSelectedComponent()).getPrimary();
             if (c instanceof HaploviewTabbedPane){
                 return ((HaploviewTabbedPane)c).getSelectedPrimary();
             }else{
                 return c;
             }
         }
 
     }
 }
 
 
 
 
