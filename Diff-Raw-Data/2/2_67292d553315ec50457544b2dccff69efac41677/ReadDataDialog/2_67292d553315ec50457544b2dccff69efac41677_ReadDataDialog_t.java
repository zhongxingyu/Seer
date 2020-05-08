 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import java.awt.event.*;
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 
 public class ReadDataDialog extends JDialog
         implements ActionListener, DocumentListener, Constants {
 
     static final String MARKER_DATA_EXT = ".info";
     static final String MAP_FILE_EXT = ".map";
     static final String BIM_FILE_EXT = ".bim";
     static final String BROWSE_GENO = "browse for geno files";
     static final String BROWSE_HAPS = "browse for haps files";
     static final String BROWSE_HMP = "browse for hapmap files";
     static final String BROWSE_PHASE = "browse for PHASE files";
     static final String BROWSE_SAMPLE = "browse for sample files";
     static final String BROWSE_LEGEND = "browse for legend files";
     static final String BROWSE_INFO = "browse for info files";
     static final String BROWSE_ASSOC = "browse for association test files";
     static final String BROWSE_WGA = "browse for PLINK files";
     static final String BROWSE_MAP = "browse for PLINK map files";
 
     private int fileType;
     private JTextField pedFileField, pedInfoField, hapsFileField, hapsInfoField, hmpFileField,
             phaseFileField, phaseSampleField, phaseLegendField, plinkFileField, plinkMapField, testFileField;
     private JCheckBox doAssociation, doGB, phaseDoGB, downloadDoGB, xChrom, gZip, embeddedMap, plinkChrom, selectColumns, nonSNP;
     private JRadioButton trioButton, ccButton, standardTDT, parenTDT;
     private JButton browseAssocButton, browsePlinkMapButton;
     private NumberTextField maxComparisonDistField, missingCutoffField, chromStartField, chromEndField;
     private JLabel testFileLabel, mapLabel;
     private JComboBox chromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox loadChromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox plinkChromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox popChooser = new JComboBox(POP_NAMES);
     private JComboBox phaseChooser = new JComboBox(RELEASE_NAMES);
     private String chromChoice, popChoice, phaseChoice, embed, selectCols;
     private boolean isDownloaded = false;
 
     JTabbedPane dataFormatPane = new JTabbedPane(JTabbedPane.LEFT);
 
     public ReadDataDialog(String title, HaploView h){
         super(h, title);
 
         //Ped Panel...
         pedFileField = new JTextField("",20);
         JButton browsePedFileButton = new JButton("Browse");
         browsePedFileButton.setActionCommand(BROWSE_GENO);
         browsePedFileButton.addActionListener(this);
         pedInfoField = new JTextField("",20);
         pedInfoField.getDocument().addDocumentListener(this);
         JButton browsePedInfoButton = new JButton("Browse");
         browsePedInfoButton.setActionCommand(BROWSE_INFO);
         browsePedInfoButton.addActionListener(this);
         JPanel assocPanel = new JPanel();
         doAssociation = new JCheckBox("Do association test");
         doAssociation.setSelected(false);
         doAssociation.setEnabled(false);
         doAssociation.setActionCommand("association");
         doAssociation.addActionListener(this);
         xChrom = new JCheckBox("X Chromosome");
         xChrom.setSelected(false);
         xChrom.setActionCommand("xChrom");
         xChrom.addActionListener(this);
         assocPanel.add(xChrom);
         assocPanel.add(doAssociation);
         JPanel tdtOptsPanel = new JPanel();
         trioButton = new JRadioButton("Family trio data", true);
         trioButton.setEnabled(false);
         trioButton.setActionCommand("tdt");
         trioButton.addActionListener(this);
         ccButton = new JRadioButton("Case/Control data");
         ccButton.setEnabled(false);
         ccButton.setActionCommand("ccButton");
         ccButton.addActionListener(this);
         ButtonGroup group = new ButtonGroup();
         group.add(trioButton);
         group.add(ccButton);
         tdtOptsPanel.add(trioButton);
         tdtOptsPanel.add(ccButton);
         JPanel tdtTypePanel = new JPanel();
         standardTDT = new JRadioButton("Standard TDT", true);
         standardTDT.setEnabled(false);
         parenTDT = new JRadioButton("ParenTDT", true);
         parenTDT.setEnabled(false);
         ButtonGroup tdtGroup = new ButtonGroup();
         tdtGroup.add(standardTDT);
         tdtGroup.add(parenTDT);
         tdtTypePanel.add(standardTDT);
         tdtTypePanel.add(parenTDT);
         testFileField = new JTextField("",20);
         testFileField.setEnabled(false);
         testFileField.setBackground(this.getBackground());
         browseAssocButton = new JButton("Browse");
         browseAssocButton.setActionCommand(BROWSE_ASSOC);
         browseAssocButton.addActionListener(this);
         browseAssocButton.setEnabled(false);
         testFileLabel = new JLabel("Test list file (optional):");
         testFileLabel.setEnabled(false);
 
         //Haps Panel..
         hapsFileField = new JTextField("",20);
         JButton browseHapsFileButton = new JButton("Browse");
         browseHapsFileButton.setActionCommand(BROWSE_HAPS);
         browseHapsFileButton.addActionListener(this);
         hapsInfoField = new JTextField("",20);
         JButton browseHapsInfoButton = new JButton("Browse");
         browseHapsInfoButton.setActionCommand(BROWSE_INFO);
         browseHapsInfoButton.addActionListener(this);
 
         //HMP panel...
         hmpFileField = new JTextField("",20);
         JButton browseHmpButton = new JButton("Browse");
         browseHmpButton.setActionCommand(BROWSE_HMP);
         browseHmpButton.addActionListener(this);
         doGB = new JCheckBox("Download and show HapMap info track (requires internet connection)");
         doGB.setSelected(false);
 
         //PHASE panel...
         phaseFileField = new JTextField("",20);
         JButton browsePhaseButton = new JButton("Browse");
         browsePhaseButton.setActionCommand(BROWSE_PHASE);
         browsePhaseButton.addActionListener(this);
         phaseSampleField = new JTextField("",20);
         JButton browseSampleButton = new JButton("Browse");
         browseSampleButton.setActionCommand(BROWSE_SAMPLE);
         browseSampleButton.addActionListener(this);
         phaseLegendField = new JTextField("",20);
         JButton browseLegendButton = new JButton("Browse");
         browseLegendButton.setActionCommand(BROWSE_LEGEND);
         browseLegendButton.addActionListener(this);
         JPanel phaseGzipPanel = new JPanel();
         gZip = new JCheckBox("Files are GZIP compressed", false);
         gZip.setEnabled(true);
         phaseGzipPanel.add(gZip);
         JPanel phaseChromPanel = new JPanel();
         phaseChromPanel.add(new JLabel("Chromosome (Required for Info Track):"));
         loadChromChooser.setSelectedIndex(-1);
         phaseChromPanel.add(loadChromChooser);
         JPanel phaseBrowsePanel = new JPanel();
         phaseDoGB = new JCheckBox("Download and show HapMap info track (requires internet connection)");
         phaseDoGB.setSelected(false);
         phaseBrowsePanel.add(phaseDoGB);
 
          //Download Panel...
         JPanel downloadChooserPanel = new JPanel();
         JLabel downloadLabel = new JLabel("*Phased HapMap downloads require an active internet connection");
        downloadChooserPanel.add(new JLabel("Release:"));
         downloadChooserPanel.add(phaseChooser);
         phaseChooser.setSelectedIndex(1);
         downloadChooserPanel.add(new JLabel("Chromosome:"));
         chromChooser.setSelectedIndex(-1);
         downloadChooserPanel.add(chromChooser);
         downloadChooserPanel.add(new JLabel("Analysis Panel:"));
         downloadChooserPanel.add(popChooser);
         JPanel downloadPositionPanel = new JPanel();
         chromStartField = new NumberTextField("",6,false,false);
         chromStartField.setEnabled(true);
         chromEndField = new NumberTextField("",6,false,false);
         chromEndField.setEnabled(true);
         downloadPositionPanel.add(new JLabel("Start kb:"));
         downloadPositionPanel.add(chromStartField);
         downloadPositionPanel.add(new JLabel("End kb:"));
         downloadPositionPanel.add(chromEndField);
         JPanel downloadBrowsePanel = new JPanel();
         downloadDoGB = new JCheckBox("Show HapMap info track");
         downloadDoGB.setSelected(true);
         downloadBrowsePanel.add(downloadDoGB);
 
         //Plink Panel...
         plinkFileField = new JTextField("",20);
         JButton browsePlinkFileButton = new JButton("Browse");
         browsePlinkFileButton.setActionCommand(BROWSE_WGA);
         browsePlinkFileButton.addActionListener(this);
         mapLabel = new JLabel("Map File:");
         plinkMapField = new JTextField("",20);
         browsePlinkMapButton = new JButton("Browse");
         browsePlinkMapButton.setActionCommand(BROWSE_MAP);
         browsePlinkMapButton.addActionListener(this);
         JPanel inputPanel = new JPanel();
         embeddedMap = new JCheckBox("Integrated Map Info");
         embeddedMap.addActionListener(this);
         embeddedMap.setSelected(false);
         inputPanel.add(embeddedMap);
         nonSNP = new JCheckBox("Non-SNP");
         nonSNP.addActionListener(this);
         nonSNP.setSelected(false);
         inputPanel.add(nonSNP);
         JPanel plinkChromPanel = new JPanel();
         plinkChrom = new JCheckBox("Only load results from Chromosome");
         plinkChrom.addActionListener(this);
         plinkChromPanel.add(plinkChrom);
         plinkChromChooser.setSelectedIndex(-1);
         plinkChromPanel.add(plinkChromChooser);
         plinkChromChooser.setEnabled(false);
         selectColumns = new JCheckBox("Select Columns");
         plinkChromPanel.add(selectColumns);
 
         JPanel pedTab = new JPanel(new GridBagLayout());
         pedTab.setPreferredSize(new Dimension(375,200));
         JPanel hapsTab = new JPanel(new GridBagLayout());
         JPanel hmpTab = new JPanel(new GridBagLayout());
         JPanel phaseTab = new JPanel(new GridBagLayout());
         JPanel downloadTab = new JPanel(new GridBagLayout());
         JPanel plinkTab = new JPanel(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(5,5,5,5);
         pedTab.add(new JLabel("Data File:"),c);
         hapsTab.add(new JLabel("Data File:"),c);
         phaseTab.add(new JLabel("Phase File:"),c);
         plinkTab.add(new JLabel("Results File:"),c);
         c.weightx = 1;
         hmpTab.add(new JLabel("Data File:"),c);
         c.anchor = GridBagConstraints.CENTER;
         c.weightx = 0;
         downloadTab.add(downloadChooserPanel,c);
         c.gridx = 1;
         pedTab.add(pedFileField,c);
         hapsTab.add(hapsFileField,c);
         hmpTab.add(hmpFileField,c);
         phaseTab.add(phaseFileField,c);
         plinkTab.add(plinkFileField,c);
         c.gridx = 2;
         c.insets = new Insets(0,10,0,0);
         pedTab.add(browsePedFileButton,c);
         hapsTab.add(browseHapsFileButton,c);
         plinkTab.add(browsePlinkFileButton,c);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browsePhaseButton,c);
         c.weightx = 1;
         hmpTab.add(browseHmpButton,c);
         c.weightx = 0;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(5,5,5,5);
         c.gridy = 1;
         c.gridx = 0;
         downloadTab.add(downloadPositionPanel,c);
         c.gridy = 2;
         downloadTab.add(downloadBrowsePanel,c);
         c.gridy = 3;
         downloadTab.add(downloadLabel,c);
         c.anchor = GridBagConstraints.EAST;
         c.gridx = 0;
         c.gridy = 1;
         c.insets = new Insets(5,5,5,5);
         pedTab.add(new JLabel("Locus Information File:"),c);
         hapsTab.add(new JLabel("Locus Information File:"),c);
         phaseTab.add(new JLabel("Sample File:"),c);
         plinkTab.add(mapLabel,c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 1;
         c.insets = new Insets(0,0,0,0);
         pedTab.add(pedInfoField,c);
         hapsTab.add(hapsInfoField,c);
         phaseTab.add(phaseSampleField,c);
         plinkTab.add(plinkMapField,c);
         c.gridx = 2;
         c.insets = new Insets(0,10,0,0);
         pedTab.add(browsePedInfoButton,c);
         hapsTab.add(browseHapsInfoButton,c);
         plinkTab.add(browsePlinkMapButton,c);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browseSampleButton,c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 0;
         c.gridy = 2;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5,5,5,5);
         phaseTab.add(new JLabel("Legend File:"),c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 1;
         phaseTab.add(phaseLegendField,c);
         c.gridx = 2;
         c.insets = new Insets(0,10,0,0);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browseLegendButton,c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 0;
         c.gridwidth = 3;
         c.insets = new Insets(0,0,0,0);
         pedTab.add(assocPanel,c);
         hmpTab.add(doGB,c);
         plinkTab.add(inputPanel,c);
         c.gridy = 3;
         pedTab.add(tdtOptsPanel,c);
         phaseTab.add(phaseGzipPanel,c);
         plinkTab.add(plinkChromPanel,c);
         c.gridy = 4;
         pedTab.add(tdtTypePanel,c);
         phaseTab.add(phaseChromPanel,c);
         c.gridy = 5;
         phaseTab.add(phaseBrowsePanel,c);
         c.anchor = GridBagConstraints.EAST;
         c.gridwidth = 1;
         c.insets = new Insets(0,0,0,5);
         pedTab.add(testFileLabel,c);
         c.gridx = 1;
         c.insets = new Insets(0,0,0,0);
         pedTab.add(testFileField,c);
         c.gridx = 2;
         c.insets = new Insets(0,10,0,0);
         pedTab.add(browseAssocButton,c);
 
         dataFormatPane.setFont(new Font("Default",Font.BOLD,12));
         dataFormatPane.addTab("Linkage Format",pedTab);
         dataFormatPane.addTab("Haps Format",hapsTab);
         dataFormatPane.addTab("HapMap Format",hmpTab);
         dataFormatPane.addTab("PHASE Format",phaseTab);
         dataFormatPane.addTab("HapMap Download",downloadTab);
         dataFormatPane.addTab("PLINK Format",plinkTab);
 
         JButton okButton = new JButton("OK");
         okButton.addActionListener(this);
         this.getRootPane().setDefaultButton(okButton);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         JButton proxyButton = new JButton("Proxy Settings");
         proxyButton.addActionListener(this);
         JPanel choicePanel = new JPanel();
         choicePanel.add(okButton);
         choicePanel.add(cancelButton);
         JPanel proxyPanel = new JPanel();
         proxyPanel.add(proxyButton);
 
 
         JPanel contents = new JPanel();
         contents.setLayout(new GridBagLayout());
         GridBagConstraints a = new GridBagConstraints();
         a.gridwidth = 3;
         a.gridx = 0;
         a.gridy = 0;
         contents.add(dataFormatPane, a);
 
         JPanel compDistPanel = new JPanel();
         compDistPanel.add(new JLabel("Ignore pairwise comparisons of markers >"));
         maxComparisonDistField = new NumberTextField(String.valueOf(Options.getMaxDistance()/1000),6, false,false);
         compDistPanel.add(maxComparisonDistField);
         compDistPanel.add(new JLabel("kb apart."));
         a.gridy = 1;
         contents.add(compDistPanel,a);
 
         JPanel missingCutoffPanel = new JPanel();
         missingCutoffField = new NumberTextField(String.valueOf(Options.getMissingThreshold()*100),3, false,false);
         missingCutoffPanel.add(new JLabel("Exclude individuals with >"));
         missingCutoffPanel.add(missingCutoffField);
         missingCutoffPanel.add(new JLabel("% missing genotypes."));
         a.gridy = 2;
         contents.add(missingCutoffPanel,a);
         a.gridy = 3;
         contents.add(choicePanel,a);
         a.anchor = GridBagConstraints.EAST;
         a.gridx = 2;
         contents.add(proxyPanel,a);
 
         if (h.getPhasedSelection() != null){
             if (((String)h.getPhasedSelection().get(0)).startsWith("16")){
                 phaseChooser.setSelectedIndex(0);
             }
 
             if (((String)h.getPhasedSelection().get(1)).equals("X")){
                 chromChooser.setSelectedIndex(22);
             }else if (((String)h.getPhasedSelection().get(1)).equals("Y")){
                 chromChooser.setSelectedIndex(23);
             }else{
                 chromChooser.setSelectedIndex(Integer.parseInt((String)h.getPhasedSelection().get(1))-1);
             }
 
             if (((String)h.getPhasedSelection().get(2)).equals("YRI")){
                 popChooser.setSelectedIndex(1);
             }else if (((String)h.getPhasedSelection().get(2)).equals("CHB+JPT")){
                 popChooser.setSelectedIndex(2);
             }
 
             chromStartField.setText((String)h.getPhasedSelection().get(3));
             chromEndField.setText((String)h.getPhasedSelection().get(4));
         }
 
         //contents.setPreferredSize(new Dimension(700,700));
         this.setContentPane(contents);
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
         this.setResizable(false);
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals(BROWSE_GENO)){
             browse(GENO_FILE);
         }else if (command.equals(BROWSE_HAPS)){
             browse(HAPS_FILE);
         }else if (command.equals(BROWSE_HMP)){
             browse(HMP_FILE);
         }else if (command.equals(BROWSE_PHASE)){
             browse(PHASED_FILE);
         }else if (command.equals(BROWSE_SAMPLE)){
             browse(SAMPLE_FILE);
         }else if (command.equals(BROWSE_LEGEND)){
             browse(LEGEND_FILE);
         }else if (command.equals(BROWSE_INFO)){
             browse(INFO_FILE);
         }else if (command.equals(BROWSE_ASSOC)){
             browse(ASSOC_FILE);
         }else if (command.equals(BROWSE_WGA)){
             browse(PLINK_FILE);
         }else if (command.equals(BROWSE_MAP)){
             browse(MAP_FILE);
         }
         else if (command.equals("OK")){
 
             //workaround for dumb Swing can't requestFocus until shown bug
             //this one seems to throw a harmless exception in certain versions of the linux JRE
             try{
                 SwingUtilities.invokeLater( new Runnable(){
                     public void run()
                     {
                         pedFileField.requestFocus();
                     }});
             }catch (RuntimeException re){
             }
             int currTab = dataFormatPane.getSelectedIndex();
             if (currTab == 0){
                 fileType = PED_FILE;
             }else if (currTab == 1){
                 fileType = HAPS_FILE;
             }else if (currTab == 2){
                 fileType = HMP_FILE;
             }else if (currTab == 3){
                 fileType = PHASED_FILE;
             }else if (currTab == 4){
                 fileType = PHASEDHMPDL_FILE;
             }else if (currTab == 5){
                 fileType = PLINK_FILE;
             }
             HaploView caller = (HaploView)this.getParent();
             if(missingCutoffField.getText().equals("")) {
                 Options.setMissingThreshold(1);
             } else {
                 double missingThreshold = (double)(Integer.parseInt(missingCutoffField.getText())) / 100;
                 if(missingThreshold > 1) {
                     JOptionPane.showMessageDialog(caller,
                             "Missing cutoff must be between 0 and 100",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 Options.setMissingThreshold(missingThreshold);
             }
 
             if (doAssociation.isSelected() && fileType == PED_FILE){
                 if (trioButton.isSelected()){
                     Options.setAssocTest(ASSOC_TRIO);
                     if(standardTDT.isSelected()){
                         Options.setTdtType(TDT_STD);
                     }else if(parenTDT.isSelected()) {
                         Options.setTdtType(TDT_PAREN);
                     }
                 } else {
                     Options.setAssocTest(ASSOC_CC);
                 }
             }else{
                 Options.setAssocTest(ASSOC_NONE);
             }
 
             if (xChrom.isSelected() && fileType == PED_FILE){
                 Chromosome.setDataChrom("chrx");
             }else {
                 Chromosome.setDataChrom("none");
             }
 
 
             if (doGB.isSelected() && fileType == HMP_FILE){
                 Options.setShowGBrowse(true);
             }else{
                 Options.setShowGBrowse(false);
             }
             Options.setgBrowseLeft(0);
             Options.setgBrowseRight(0);
 
             if (maxComparisonDistField.getText().equals("")){
                 Options.setMaxDistance(0);
             }else{
                 Options.setMaxDistance(Integer.parseInt(maxComparisonDistField.getText()));
             }
 
             if (fileType == PHASED_FILE){
                 if (gZip.isSelected()){
                     Options.setGzip(true);
                 }else{
                     Options.setGzip(false);
                 }
                 isDownloaded = false;
                 if (phaseDoGB.isSelected()){
                     Options.setShowGBrowse(true);
                     if (loadChromChooser.getSelectedIndex() == -1){
                         JOptionPane.showMessageDialog(caller,
                                 "HapMap Info Track download requires a chromosome.",
                                 "Invalid value",
                                 JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                 }else{
                     Options.setShowGBrowse(false);
                 }
                 if (loadChromChooser.getSelectedIndex() == -1){
                     chromChoice = "";
                 }else{
                     chromChoice = (String)loadChromChooser.getSelectedItem();
                 }
             }
             if (fileType == PHASEDHMPDL_FILE){
                 isDownloaded = true;
 
                 if (downloadDoGB.isSelected()){
                     Options.setShowGBrowse(true);
                 }else{
                     Options.setShowGBrowse(false);
                 }
                 if (chromChooser.getSelectedIndex() == -1){
                     JOptionPane.showMessageDialog(caller,
                             "Please select a chromosome.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (chromStartField.getText().equals("")){
                     JOptionPane.showMessageDialog(caller,
                             "Please enter a starting value.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (chromEndField.getText().equals("")){
                     JOptionPane.showMessageDialog(caller,
                             "Please enter an ending value.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (Integer.parseInt(chromStartField.getText()) >= Integer.parseInt(chromEndField.getText())){
                     JOptionPane.showMessageDialog(caller,
                             "End position must be larger then start position.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 chromChoice = (String)chromChooser.getSelectedItem();
                 popChoice = (String)popChooser.getSelectedItem();
                 phaseChoice = (String)phaseChooser.getSelectedItem();
 
             }
 
             if (fileType == PLINK_FILE){
                 if (embeddedMap.isSelected()){
                     embed = "E";
                 }
                 Options.setSNPBased(!nonSNP.isSelected());
                 if (plinkChrom.isSelected()){
                     if (plinkChromChooser.getSelectedIndex() == -1){
                         JOptionPane.showMessageDialog(caller,
                                 "Please select a chromosome to load.",
                                 "Invalid value",
                                 JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     chromChoice = new Integer((plinkChromChooser.getSelectedIndex()+1)).toString();
                 }else{
                     chromChoice = null;
                 }
                 if (selectColumns.isSelected()){
                     selectCols = "Y";
                 }
             }
 
             String[] returnStrings;
             if (fileType == HAPS_FILE){
                 returnStrings = new String[]{hapsFileField.getText(), hapsInfoField.getText(),null};
                 if (returnStrings[1].equals("")) returnStrings[1] = null;
             }else if (fileType == HMP_FILE){
                 returnStrings = new String[]{hmpFileField.getText(),null,null};
             }else if (fileType == PHASED_FILE ){
                 returnStrings = new String[]{phaseFileField.getText(), phaseSampleField.getText(), phaseLegendField.getText(), chromChoice};
             }else if (fileType == PHASEDHMPDL_FILE){
                 returnStrings = new String[]{"Chr" + chromChoice + ":" + popChoice + ":" + chromStartField.getText() + ".." +
                         chromEndField.getText(), popChoice, chromStartField.getText(), chromEndField.getText(), chromChoice, phaseChoice, "txt"};
             }else if (fileType == PLINK_FILE){
                 returnStrings = new String[]{plinkFileField.getText(), plinkMapField.getText(),null,embed,null,chromChoice,selectCols};
             }
             else{
                 returnStrings = new String[]{pedFileField.getText(), pedInfoField.getText(), testFileField.getText()};
                 if (returnStrings[1].equals("")) returnStrings[1] = null;
                 if (returnStrings[2].equals("") || !doAssociation.isSelected()) returnStrings[2] = null;
             }
 
 
             //if a dataset was previously loaded during this session, discard the display panes for it.
             caller.clearDisplays();
             this.dispose();
             if (fileType != PLINK_FILE){
                 caller.readGenotypes(returnStrings, fileType, isDownloaded);
             }else{
                 caller.readWGA(returnStrings);
             }
         }else if (command.equals("Cancel")){
             this.dispose();
         }else if (command.equals("association")){
             switchAssoc(doAssociation.isSelected());
         }else if(command.equals("tdt")){
             standardTDT.setEnabled(true);
             if (!xChrom.isSelected()){
                 parenTDT.setEnabled(true);
             }
         }else if(command.equals("ccButton")){
             standardTDT.setEnabled(false);
             parenTDT.setEnabled(false);
         }else if (command.equals("xChrom")){
             if (xChrom.isSelected()){
                 parenTDT.setEnabled(false);
                 standardTDT.setSelected(true);
             }else if (standardTDT.isEnabled()){
                 parenTDT.setEnabled(true);
             }
         }else if (command.equals("Integrated Map Info")){
             if (embeddedMap.isSelected()){
                 embeddedMap.setSelected(true);
                 mapLabel.setEnabled(false);
                 plinkMapField.setEnabled(false);
                 browsePlinkMapButton.setEnabled(false);
             }else{
                 embeddedMap.setSelected(false);
                 mapLabel.setEnabled(true);
                 plinkMapField.setEnabled(true);
                 browsePlinkMapButton.setEnabled(true);
             }
 
         }else if (command.equals("Non-SNP")){
             if (nonSNP.isSelected()){
                 nonSNP.setSelected(true);
                 mapLabel.setEnabled(false);
                 plinkMapField.setEnabled(false);
                 plinkMapField.setText("");
                 browsePlinkMapButton.setEnabled(false);
                 plinkChrom.setSelected(false);
                 plinkChrom.setEnabled(false);
                 plinkChromChooser.setSelectedIndex(-1);
                 plinkChromChooser.setEnabled(false);
                 embeddedMap.setSelected(false);
                 embeddedMap.setEnabled(false);
             }else{
                 nonSNP.setSelected(false);
                 mapLabel.setEnabled(true);
                 plinkMapField.setEnabled(true);
                 browsePlinkMapButton.setEnabled(true);
                 plinkChrom.setEnabled(true);
                 embeddedMap.setEnabled(true);
             }
         }else if (command.equals("Only load results from Chromosome")){
             if (plinkChrom.isSelected()){
                 plinkChrom.setSelected(true);
                 plinkChromChooser.setEnabled(true);
             }else{
                 plinkChrom.setSelected(false);
                 plinkChromChooser.setEnabled(false);
             }
         }else if (command.equals("Proxy Settings")){
             ProxyDialog pd = new ProxyDialog(this,"Proxy Settings");
             pd.pack();
             pd.setVisible(true);
         }
     }
 
     void browse(int browseType){
         String name;
         String markerInfoName = "";
         String mapFileName = "";
         HaploView.fc.setSelectedFile(new File(""));
         int returned = HaploView.fc.showOpenDialog(this);
         if (returned != JFileChooser.APPROVE_OPTION) return;
         File file = HaploView.fc.getSelectedFile();
 
         if (browseType == GENO_FILE){
             name = file.getName();
             pedFileField.setText(file.getParent()+File.separator+name);
 
             if(pedInfoField.getText().equals("")){
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name,".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens()-1;
                 for (int i = 0; i < numPieces; i++){
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for info file for original file sample.ped
                 //either sample.ped.info or sample.info
                 File maybeMarkers1 = new File(file.getParent(), name + MARKER_DATA_EXT);
                 File maybeMarkers2 = new File(file.getParent(), baseName + MARKER_DATA_EXT);
                 if (maybeMarkers1.exists()){
                     markerInfoName = maybeMarkers1.getName();
                 }else if (maybeMarkers2.exists()){
                     markerInfoName = maybeMarkers2.getName();
                 }else{
                     return;
                 }
                 pedInfoField.setText(file.getParent()+File.separator+markerInfoName);
             }
         }else if (browseType == HAPS_FILE){
             name = file.getName();
             hapsFileField.setText(file.getParent()+File.separator+name);
 
             if(hapsInfoField.getText().equals("")){
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name,".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens()-1;
                 for (int i = 0; i < numPieces; i++){
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for info file for original file sample.haps
                 //either sample.haps.info or sample.info
                 File maybeMarkers1 = new File(file.getParent(), name + MARKER_DATA_EXT);
                 File maybeMarkers2 = new File(file.getParent(), baseName + MARKER_DATA_EXT);
                 if (maybeMarkers1.exists()){
                     markerInfoName = maybeMarkers1.getName();
                 }else if (maybeMarkers2.exists()){
                     markerInfoName = maybeMarkers2.getName();
                 }else{
                     return;
                 }
                 hapsInfoField.setText(file.getParent()+File.separator+markerInfoName);
             }
         }else if (browseType == HMP_FILE){
             name = file.getName();
             hmpFileField.setText(file.getParent()+File.separator+name);
         }else if (browseType == PHASED_FILE){
             name = file.getName();
             phaseFileField.setText(file.getParent()+File.separator+name);
         }else if (browseType == SAMPLE_FILE){
             name = file.getName();
             phaseSampleField.setText(file.getParent()+File.separator+name);
         }else if (browseType == LEGEND_FILE){
             name = file.getName();
             phaseLegendField.setText(file.getParent()+File.separator+name);
         }else if (browseType==INFO_FILE){
             markerInfoName = file.getName();
             if (dataFormatPane.getSelectedIndex() == 1){
                 hapsInfoField.setText(file.getParent()+File.separator+markerInfoName);
             }else{
                 pedInfoField.setText(file.getParent()+File.separator+markerInfoName);
             }
         }else if (browseType == ASSOC_FILE){
             testFileField.setText(file.getParent() + File.separator + file.getName());
         }else if (browseType == PLINK_FILE){
             name = file.getName();
             plinkFileField.setText(file.getParent()+File.separator+name);
 
             if(plinkMapField.getText().equals("") && !nonSNP.isSelected()){
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name,".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens()-1;
                 for (int i = 0; i < numPieces; i++){
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for map file for original file
                 //either .map or .bim extensions
                 File maybeMap1 = new File(file.getParent(), name + MAP_FILE_EXT);
                 File maybeMap2 = new File(file.getParent(), baseName + MAP_FILE_EXT);
                 File maybeMap3 = new File(file.getParent(), name + BIM_FILE_EXT);
                 File maybeMap4 = new File(file.getParent(), baseName + BIM_FILE_EXT);
                 if (maybeMap1.exists()){
                     mapFileName = maybeMap1.getName();
                 }else if (maybeMap2.exists()){
                     mapFileName = maybeMap2.getName();
                 }else if (maybeMap3.exists()){
                     mapFileName = maybeMap3.getName();
                 }else if (maybeMap4.exists()){
                     mapFileName = maybeMap4.getName();
                 }else{
                     return;
                 }
                 plinkMapField.setText(file.getParent()+File.separator+mapFileName);
             }
         }else if (browseType == MAP_FILE){
             name = file.getName();
             plinkMapField.setText(file.getParent()+File.separator+name);
         }
     }
 
     public void insertUpdate(DocumentEvent e) {
         checkInfo(e);
     }
 
     public void removeUpdate(DocumentEvent e) {
         checkInfo(e);
     }
 
     public void changedUpdate(DocumentEvent e) {
         //not fired by plain text components
     }
 
     private void switchAssoc(boolean b){
         if(b){
             doAssociation.setEnabled(true);
             trioButton.setEnabled(true);
             ccButton.setEnabled(true);
             browseAssocButton.setEnabled(true);
             testFileField.setEnabled(true);
             testFileField.setBackground(Color.white);
             testFileLabel.setEnabled(true);
             if (trioButton.isSelected()){
                 standardTDT.setEnabled(true);
                 if (!xChrom.isSelected()){
                     parenTDT.setEnabled(true);
                 }
             }
         }else{
             doAssociation.setSelected(false);
             trioButton.setEnabled(false);
             ccButton.setEnabled(false);
             browseAssocButton.setEnabled(false);
             testFileField.setEnabled(false);
             testFileField.setBackground(this.getBackground());
             testFileLabel.setEnabled(false);
             standardTDT.setEnabled(false);
             parenTDT.setEnabled(false);
         }
     }
 
     private void checkInfo(DocumentEvent e){
         //the text in the info field has changed. if it is empty, disable assoc testing
         if (pedInfoField.getText().equals("")){
             switchAssoc(false);
             doAssociation.setEnabled(false);
         }else{
             doAssociation.setEnabled(true);
         }
     }
 }
 
