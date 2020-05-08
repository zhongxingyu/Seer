 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 //import javax.swing.event.*;
 import javax.swing.text.*;
 //import java.beans.*;
 
 
 public class ReadDataDialog extends JDialog implements ActionListener {
 
     static final String HAPMAP_DATA = "Browse HapMap data from DCC";
     static final String RAW_DATA = "Load raw genotypes";
     static final String PHASED_DATA = "Load phased haplotypes";
     static final String MARKER_DATA_EXT = ".info";
     static final String BROWSE_GENO = "browse for geno files";
     static final String BROWSE_INFO = "browse for info files";    
     static final int GENO = 0;
     static final int INFO = 1;
     static final int HAPS = 2;
     static final int PED = 3;
 
     HaploView caller;
     int fileType;
     JTextField genoFileField, infoFileField;
     NumberTextField maxComparisonDistField;
     JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
 
 
     public ReadDataDialog(String title, HaploView h){
         caller = h;
         setTitle(title);
 
         JPanel contents = new JPanel();
         JButton hapmapButton = new JButton(HAPMAP_DATA);
         hapmapButton.addActionListener(this);
         //hapmap isn't ready yet
         hapmapButton.setEnabled(false);
         JButton rawdataButton = new JButton(RAW_DATA);
         rawdataButton.addActionListener(this);
         JButton phaseddataButton = new JButton(PHASED_DATA);
         phaseddataButton.addActionListener(this);
 
         contents.add(Box.createRigidArea(new Dimension(10,10)));
         contents.add(rawdataButton);
         contents.add(Box.createRigidArea(new Dimension(10,10)));
         contents.add(phaseddataButton);
         contents.add(Box.createRigidArea(new Dimension(10,10)));
         contents.add(hapmapButton);
         contents.add(Box.createRigidArea(new Dimension(10,10)));
 
         contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
         this.setContentPane(contents);
         this.setLocation(caller.getX() + 100,
                 caller.getY() + 100);
         //this.setLocation((getParent().getWidth() - this.getWidth()) / 2,
         //		   (getParent().getHeight() - this.getHeight()) / 2);
         this.setModal(true);
     }
 
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command==RAW_DATA){
             load(PED);
         }else if (command == PHASED_DATA){
             load(HAPS);
         }else if (command == BROWSE_GENO){
             browse(GENO);
         }else if (command == BROWSE_INFO){
             browse(INFO);
         }else if (command == HAPMAP_DATA){
             //hapmap
         }else if (command == "OK"){
             String[] returnStrings = {genoFileField.getText(), infoFileField.getText(), maxComparisonDistField.getText()};
             if (fileType == HAPS){
                 caller.readPhasedGenotypes(returnStrings);
             }else if (fileType == PED){
                 caller.readPedGenotypes(returnStrings);
             }
             if (caller.dPrimeDisplay != null){
                 caller.dPrimeDisplay.setVisible(false);
             }
             this.dispose();
         }else if (command == "Cancel"){
             this.dispose();
         }
     }
 
 
     void browse(int browseType){
         String name;
         String markerInfoName = "";
         fc.setSelectedFile(null);
         int returned = fc.showOpenDialog(this);
         if (returned != JFileChooser.APPROVE_OPTION) return;
         File file = fc.getSelectedFile();
 
         if (browseType == GENO){
             name = file.getName();
             genoFileField.setText(file.getParent()+File.separator+name);
 
             if(infoFileField.getText().equals("")){
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name,".");
                 String baseName = st.nextToken();
                 for (int i = 0; i < st.countTokens()-1; i++){
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
                 infoFileField.setText(file.getParent()+File.separator+markerInfoName);
             }
 
         }else if (browseType==INFO){
             markerInfoName = file.getName();
             infoFileField.setText(file.getParent()+File.separator+markerInfoName);
         }
     }
 
     void load(int ft){
         fileType = ft;
         JPanel contents = new JPanel();
         contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
 
         JPanel filePanel = new JPanel();
         filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
         JPanel topFilePanel = new JPanel();
         JPanel botFilePanel = new JPanel();
         genoFileField = new JTextField("",20);

        //workaround for stupid java problem where focus can't be granted until window is displayed
        SwingUtilities.invokeLater( new Runnable(){
            public void run()
            {
                genoFileField.requestFocusInWindow();
            }
        });

         infoFileField = new JTextField("",20);
         JButton browseGenoButton = new JButton("Browse");
         browseGenoButton.setActionCommand(BROWSE_GENO);
         browseGenoButton.addActionListener(this);
         JButton browseInfoButton = new JButton("Browse");
         browseInfoButton.setActionCommand(BROWSE_INFO);
         browseInfoButton.addActionListener(this);
         topFilePanel.add(new JLabel("Genotype file: "));
         topFilePanel.add(genoFileField);
         topFilePanel.add(browseGenoButton);
         botFilePanel.add(new JLabel("Locus information file: "));
         botFilePanel.add(infoFileField);
         botFilePanel.add(browseInfoButton);
         filePanel.add(topFilePanel);
         filePanel.add(botFilePanel);
         filePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         contents.add(filePanel);
 
         JPanel prefsPanel = new JPanel();
         maxComparisonDistField = new NumberTextField("200",4);
         prefsPanel.add(new JLabel("Ignore pairwise comparisons of markers >"));
         prefsPanel.add(maxComparisonDistField);
         prefsPanel.add(new JLabel("kb apart."));
         contents.add(prefsPanel);
 
         JPanel choicePanel = new JPanel();
         JButton okButton = new JButton("OK");
        this.getRootPane().setDefaultButton(okButton);
         okButton.addActionListener(this);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         choicePanel.add(okButton);
         choicePanel.add(cancelButton);
         contents.add(choicePanel);
 
         this.setContentPane(contents);
         this.pack();
     }
 
 
     class NumberTextField extends JTextField {
 
         public NumberTextField(String str, int size){
             super(str, size);
         }
 
         protected Document createDefaultModel(){
             return new NTFDocument(this);
         }
 
         protected class NTFDocument extends PlainDocument {
             NumberTextField ntf;
 
             public NTFDocument(NumberTextField ntf){
                 super();
                 this.ntf = ntf;
             }
 
             public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                 int length = ntf.getText().length();
                 char[] source = str.toCharArray();
                 String to_insert = "";
                 for (int i=0; i<source.length; i++){
                     if (length+i > 3){
                         Toolkit.getDefaultToolkit().beep();
                         super.insertString(offs, to_insert, a);
                         return;
                     }
                     if (Character.isDigit(source[i])) to_insert+=source[i];
                     else Toolkit.getDefaultToolkit().beep();
                 }
 
                 super.insertString(offs, to_insert, a);
             }
         }
     }
 
 
 }
 
