 package edu.mit.wi.haploview;
 
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Vector;
 
 public class HaploView extends JFrame implements ActionListener{
 
     boolean DEBUG = false;
     long maxCompDist;
 
     //some constants etc.
     static final String MARKER_DATA_EXT = ".info";
 
     static final String READ_GENOTYPES = "Open genotype data";
     static final String READ_MARKERS = "Load marker data";
     JMenuItem readMarkerItem;
 
     static final String EXPORT_TEXT = "Export data to text";
     static final String EXPORT_PNG = "Export data to PNG";
     static final String EXPORT_PS = "Export data to postscript";
     static final String EXPORT_PRINT = "Print";
     String exportItems[] = {
         EXPORT_TEXT, EXPORT_PNG, EXPORT_PS, EXPORT_PRINT
     };
     JMenuItem exportMenuItems[];
 
     static final String DEFINE_BLOCKS = "Define blocks";
     static final String CLEAR_BLOCKS = "Clear all blocks";
     JMenuItem clearBlocksItem;
     JMenuItem defineBlocksItem;
 
     static final String QUIT = "Quit";
 
     static final String VIEW_DPRIME = "D Prime Plot";
     static final String VIEW_HAPLOTYPES = "Haplotypes";
     static final String VIEW_GENOTYPES = "Genotype Data";
     static final String VIEW_MARKERS = "Marker Data";
     static final String VIEW_CHECK_PANEL = "Check Markers";
     static final String VIEW_TDT = "TDT";
 
     static final int VIEW_D_NUM = 0;
     static final int VIEW_HAP_NUM = 1;
     static final int VIEW_TDT_NUM = 2;
     static final int VIEW_CHECK_NUM = 3;
 
     String viewItems[] = {
         VIEW_DPRIME, VIEW_HAPLOTYPES, VIEW_TDT, VIEW_CHECK_PANEL
     };
     JRadioButtonMenuItem viewMenuItems[];
 
     //static final String DISPLAY_OPTIONS = "Display Options";
     //JMenuItem displayOptionsItem;
 
     //start filechooser in current directory
     final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
 
     HaploData theData;
     //JFrame checkWindow;
     private CheckDataPanel checkPanel;
     private TDTPanel tdtPanel;
     private boolean doTDT = false;
     //private String hapInputFileName;
     //private BlockDisplay theBlocks;
     //private boolean infoKnown = false;
     private int currentBlockDef;
     private javax.swing.Timer timer;
 
     static HaploView window;
     DPrimeDisplay dPrimeDisplay;
     private JScrollPane hapScroller;
     private HaplotypeDisplay hapDisplay;
     private JTabbedPane tabs;
     private String[] inputOptions;
 
     //COMMAND LINE ARGUMENTS
     private HaploText argParser;
 
     public HaploView(){
 
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
 
         menuItem = new JMenuItem(QUIT);
         setAccelerator(menuItem, 'Q', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         /// display menu
 
         JMenu displayMenu = new JMenu("Display");
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
 
         //analysis menu
         JMenu analysisMenu = new JMenu("Analysis");
         menuBar.add(analysisMenu);
         defineBlocksItem = new JMenuItem(DEFINE_BLOCKS);
         setAccelerator(defineBlocksItem, 'B', false);
         defineBlocksItem.addActionListener(this);
         defineBlocksItem.setEnabled(false);
         analysisMenu.add(defineBlocksItem);
         clearBlocksItem = new JMenuItem(CLEAR_BLOCKS);
         setAccelerator(clearBlocksItem, 'C', false);
         clearBlocksItem.addActionListener(this);
         clearBlocksItem.setEnabled(false);
         analysisMenu.add(clearBlocksItem);
 
         // maybe
         //displayMenu.addSeparator();
         //displayOptionsItem = new JMenuItem(DISPLAY_OPTIONS);
         //setAccelerator(displayOptionsItem, 'D', false);
 
         /** NEEDS FIXING
          helpMenu = new JMenu("Help");
          menuBar.add(Box.createHorizontalGlue());
          menuBar.add(helpMenu);
 
          menuItem = new JMenuItem("Tutorial");
          menuItem.addActionListener(this);
          helpMenu.add(menuItem);
          **/
 
         /*
         clearBlocksMenuItem.addActionListener(this);
         clearBlocksMenuItem.setEnabled(false);
         toolMenu.add(clearBlocksMenuItem);
         */
 
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e){
                 quit();
             }
         });
 
     }
 
     public void argHandler(String[] args) {
       this.argParser = new HaploText(args);
     }
 
 
     // function workaround for overdesigned, underthought swing api -fry
     void setAccelerator(JMenuItem menuItem, char what, boolean shift) {
         menuItem.setAccelerator(KeyStroke.getKeyStroke(what, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | (shift ? ActionEvent.SHIFT_MASK : 0)));
     }
 
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command == READ_GENOTYPES){
             ReadDataDialog readDialog = new ReadDataDialog("Open new data", this);
             readDialog.pack();
             readDialog.setVisible(true);
         } else if (command == READ_MARKERS){
             fc.setSelectedFile(null);
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 readMarkers(fc.getSelectedFile());
             }
         }else if (command == CLEAR_BLOCKS){
             theData.guessBlocks(3);
             dPrimeDisplay.refresh();
             try{
                 hapDisplay.getHaps();
             }catch(HaploViewException hve){
                 JOptionPane.showMessageDialog(this,
                         hve.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
             hapScroller.setViewportView(hapDisplay);
         }else if (command == DEFINE_BLOCKS){
             try {
                 defineBlocks();
             }catch(HaploViewException hve) {
                 JOptionPane.showMessageDialog(this,
                         hve.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }else if (command == "Tutorial"){
             showHelp();
         } else if (command == QUIT){
             quit();
         } else {
             for (int i = 0; i < viewItems.length; i++) {
                 if (command == viewItems[i]) tabs.setSelectedIndex(i);
             }
         }
     }
 
 
     void quit(){
         //any handling that might need to take place here
         System.exit(0);
     }
 
 
     void readPedGenotypes(String[] f){
         //input is a 3 element array with
         //inputOptions[0] = ped file
         //inputOptions[1] = info file (null if none)
         //inputOptions[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)
 
         inputOptions = f;
         File pedFile = new File(inputOptions[0]);
         //pop open checkdata window
         //checkWindow = new JFrame();
         try {
             if (pedFile.length() < 1){
                 throw new HaploViewException("Pedfile is empty or nonexistent: " + pedFile.getName());
             }
 
             checkPanel = new CheckDataPanel(pedFile);
             checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
             theData = new HaploData();
             JTable table = checkPanel.getTable();
             boolean[] markerResultArray = new boolean[table.getRowCount()];
             for (int i = 0; i < table.getRowCount(); i++){
                 markerResultArray[i] = ((Boolean)table.getValueAt(i,7)).booleanValue();
             }
 
             theData.linkageToChrom(markerResultArray,checkPanel.getPedFile());
             this.doTDT = true;
             processData();
         }catch(IOException ioexec) {
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
     void readPhasedGenotypes(String[] f){
         //input is a 3 element array with
         //inputOptions[0] = haps file
         //inputOptions[1] = info file (null if none)
         //inputOptions[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)
 
         //these are not available for non ped files
         viewMenuItems[VIEW_CHECK_NUM].setEnabled(false);
         viewMenuItems[VIEW_TDT_NUM].setEnabled(false);
         checkPanel = null;
         doTDT = false;
 
 
         inputOptions = f;
         theData = new HaploData();
         try{
             theData.prepareHapsInput(new File(inputOptions[0]));
             processData();
         }catch(IOException ioexec) {
             JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch(HaploViewException ex){
             JOptionPane.showMessageDialog(this,
                     ex.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
     void processData() {
         maxCompDist = Long.parseLong(inputOptions[2])*1000;
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
         final SwingWorker worker = new SwingWorker(){
             public Object construct(){
                 dPrimeDisplay=null;
                 theData.infoKnown = false;
                 if (!(inputOptions[1].equals(""))){
                     readMarkers(new File(inputOptions[1]));
                 }
                 theData.generateDPrimeTable(maxCompDist);
                 theData.guessBlocks(0);
                 //drawPicture(theData);
                 //void drawPicture(HaploData theData) {
                 Container contents = getContentPane();
                 contents.removeAll();
 
                 int currentTab = 0;
                 /*if (!(tabs == null)){
                 currentTab = tabs.getSelectedIndex();
                 } */
 
                 tabs = new JTabbedPane();
                 tabs.addChangeListener(new TabChangeListener());
 
                 //first, draw the D' picture
                 JPanel panel = new JPanel();
                 panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                 dPrimeDisplay = new DPrimeDisplay(theData);
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
                 panel.add(hapScroller);
                 panel.add(hdc);
                 tabs.addTab(viewItems[VIEW_HAP_NUM], panel);
                 viewMenuItems[VIEW_HAP_NUM].setEnabled(true);
 
                 //TDT panel
                 if(doTDT) {
                     tdtPanel = new TDTPanel(theData.chromosomes);
                     tabs.addTab(viewItems[VIEW_TDT_NUM], tdtPanel);
                     viewMenuItems[VIEW_TDT_NUM].setEnabled(true);
                 }
 
                 //check data panel
                 if (checkPanel != null){
                     tabs.addTab(viewItems[VIEW_CHECK_NUM], checkPanel);
                     viewMenuItems[VIEW_CHECK_NUM].setEnabled(true);
                     currentTab=VIEW_CHECK_NUM;
                 }
 
 
 
                 tabs.setSelectedIndex(currentTab);
                 contents.add(tabs);
 
                 //next add a little spacer
                 //ontents.add(Box.createRigidArea(new Dimension(0,5)));
 
                 //and then add the block display
                 //theBlocks = new BlockDisplay(theData.markerInfo, theData.blocks, dPrimeDisplay, infoKnown);
                 //contents.setBackground(Color.black);
 
                 //put the block display in a scroll pane in case the data set is very large.
                 //JScrollPane blockScroller = new JScrollPane(theBlocks,
                 //						    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                 //					    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                 //blockScroller.getHorizontalScrollBar().setUnitIncrement(60);
                 //blockScroller.setMinimumSize(new Dimension(800, 100));
                 //contents.add(blockScroller);
                 repaint();
                 setVisible(true);
                 //}
 
                 theData.finished = true;
                 return "";
             }
         };
 
         timer = new javax.swing.Timer(50, new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 if (theData.finished){
                     timer.stop();
                     defineBlocksItem.setEnabled(true);
                     clearBlocksItem.setEnabled(true);
                     readMarkerItem.setEnabled(true);
                     setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 }
             }
         });
 
         worker.start();
         timer.start();
     }
 
 
     void readMarkers(File inputFile){
         try {
             theData.prepareMarkerInput(inputFile,maxCompDist);
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
         }/*catch (RuntimeException rtexec){
             JOptionPane.showMessageDialog(this,
                     "An error has occured. It is probably related to file format:\n"+rtexec.toString(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }  */
         if (dPrimeDisplay != null){
             dPrimeDisplay.refresh();
         }
     }
 
 
     class TabChangeListener implements ChangeListener{
         public void stateChanged(ChangeEvent e) {
             viewMenuItems[tabs.getSelectedIndex()].setSelected(true);
             if (checkPanel != null && checkPanel.changed){
                 window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 JTable table = checkPanel.getTable();
                 boolean[] markerResults = new boolean[table.getRowCount()];
                 for (int i = 0; i < table.getRowCount(); i++){
                     markerResults[i] = ((Boolean)table.getValueAt(i,7)).booleanValue();
                 }
                 int count = 0;
                 for (int i = 0; i < Chromosome.getSize(); i++){
                     if (markerResults[i]){
                         count++;
                     }
                 }
                 Chromosome.realIndex = new int[count];
                 int k = 0;
                 for (int i =0; i < Chromosome.getSize(); i++){
                     if (markerResults[i]){
                         Chromosome.realIndex[k] = i;
                         k++;
                     }
                 }
                 theData.filteredDPrimeTable = theData.getFilteredTable();
                 theData.guessBlocks(currentBlockDef);
 
                 //hack-y way to refresh the image
                 dPrimeDisplay.setVisible(false);
                 dPrimeDisplay.setVisible(true);
 
                 hapDisplay.theData = theData;
                 try{
                     hapDisplay.getHaps();
                 }catch(HaploViewException hv){
                     JOptionPane.showMessageDialog(window,
                             hv.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
                 tdtPanel.refreshTable();
                 //System.out.println(tabs.getComponentAt(VIEW_TDT_NUM));
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 checkPanel.changed=false;
             }
         }
     }
 
     //TODO: investigate export options
     /**void doExportDPrime(){
      fc.setSelectedFile(null);
      int returnVal = fc.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION){
      try {
      DrawingMethods dm = new DrawingMethods();
      Dimension theSize = dm.dPrimeGetPreferredSize(theData.dPrimeTable.length, infoKnown);
      BufferedImage image = new BufferedImage((int)theSize.getWidth(), (int)theSize.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      dm.dPrimeDraw(theData.dPrimeTable, infoKnown, theData.markerInfo, image.getGraphics());
      dm.saveImage(image, fc.getSelectedFile().getPath());
      } catch (IOException ioexec){
      JOptionPane.showMessageDialog(this,
      ioexec.getMessage(),
      "File Error",
      JOptionPane.ERROR_MESSAGE);
      }
      }
      }**/
 
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
 
 
     void saveDprimeToText(){
         fc.setSelectedFile(null);
         try{
             fc.setSelectedFile(null);
             int returnVal = fc.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 theData.saveDprimeToText(theData.filteredDPrimeTable, fc.getSelectedFile(), theData.infoKnown);
             }
         }catch (IOException ioexec){
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
 
     void defineBlocks() throws HaploViewException{
         String[] methodStrings = {"95% of informative pairwise comparisons show strong LD via confidence intervals (SFS)",
                                   "Four Gamete Rule",
                                   "Solid block of strong LD via D prime (MJD)"};
         JComboBox methodList = new JComboBox(methodStrings);
         JOptionPane.showMessageDialog(this,
                 methodList,
                 "Select a block-finding algorithm",
                 JOptionPane.QUESTION_MESSAGE);
         theData.guessBlocks(methodList.getSelectedIndex());
         hapDisplay.getHaps();
         hapScroller.setViewportView(hapDisplay);
         dPrimeDisplay.refresh();
     }
 
 
     public static void main(String[] args) {
         boolean nogui = false;
         //HaploView window;
         for(int i = 0;i<args.length;i++) {
             if(args[i].equals("-n") || args[i].equals("-h")) {
                 nogui = true;
             }
         }
         if(nogui) {
             HaploText textOnly = new HaploText(args);
         }
         else {
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) { }
 
             window =  new HaploView();
             window.argHandler(args);
 
             //setup view object
             window.setTitle("HaploView beta");
             window.setSize(800,600);
 
             //center the window on the screen
             Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
             window.setLocation((screen.width - window.getWidth()) / 2,
                     (screen.height - window.getHeight()) / 2);
 
             window.setVisible(true);
             ReadDataDialog readDialog = new ReadDataDialog("Welcome to HaploView", window);
             readDialog.pack();
             readDialog.setVisible(true);
 
         }
     }
 }
 
