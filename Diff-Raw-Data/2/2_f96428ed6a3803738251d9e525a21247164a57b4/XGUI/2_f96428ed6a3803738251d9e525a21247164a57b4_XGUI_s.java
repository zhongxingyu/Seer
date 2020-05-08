 /**
  * The XGUI class contains methods needed for graphical user interface on
  * computer platform.
  */
 package services;
 
 import javax.swing.*;
 import wordlists.*;
 import java.util.LinkedList;
 import utils.Word;
 import wordlists.*;
 
 public class XGUI extends javax.swing.JFrame implements IService {
 
     private static Object dictObject;
     public wordlists.DWAMemory[] wordList;
 
     /** Constructors */
     public XGUI(wordlists.DWAMemory[] theDict) {
       //initComponents();
       this.wordList = theDict;
       //this.setVisible(true);
       }
       
     public XGUI() {
       //initComponents();
       }
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      */
     @SuppressWarnings("unchecked")
 
     private void initComponents() {
 
         jSearchField = new javax.swing.JTextField();
         jSearchButton = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jWordDef = new javax.swing.JTextArea();
         jDictBox = new javax.swing.JComboBox();
         jMenuBar1 = new javax.swing.JMenuBar();
         jFileMenu = new javax.swing.JMenu();
         jHelpMenu = new javax.swing.JMenu();
         jExitMenuItem = new javax.swing.JMenuItem();
 
         
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Universal Dictionary");
 
 
         jSearchField.setText("");
 
         jExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent event) {
               stop();
               }
 
         });
 
         jSearchButton.setText("Search");
         jSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchInDict();
             }
         });
 
         jFileMenu.setText("File");
         jMenuBar1.add(jFileMenu);
 
         jHelpMenu.setText("Help");
         jMenuBar1.add(jHelpMenu);
 
         jExitMenuItem.setText("Exit");
         jFileMenu.add(jExitMenuItem);
 
         setJMenuBar(jMenuBar1);
 
         jWordDef.setColumns(20);
         jWordDef.setRows(5);
        jWordDef.setText("bn\n");
         jScrollPane1.setViewportView(jWordDef);
 
         jDictBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Test", "Bla" }));
         jDictBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 //jDictBoxActionPerformed(evt);
             }
         });
         
         jSearchField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
               searchInDict();
               }
         });
 
         jSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
 //                jPrintedText.setText(jSearchField.getText());
             }
         });	
 
 
 
         /** 
          * JFrom Layout Scheme
          */
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jDictBox, 0, 169, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jSearchButton)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jSearchButton)
                     .addComponent(jDictBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
         setVisible(true);
         System.out.println("Bounds: " + this.getBounds());
     }
 
     public void run() {
       initComponents();
       }
 
     public void stop() {
       System.out.println("System is now exiting. Bye :)");
       System.exit(0);
       }
     
     private void searchInDict() {
       try {
         matchFound = wordList[0].search(jSearchField.getText(), 4);
         
         String definitions = "";
         for(int temp = 0; temp < matchFound.size(); temp++) {
           definitions += matchFound.get(temp).getWord() + " - "
                       + matchFound.get(temp).getDescription() + "\n";
           }
         jWordDef.setText(definitions);
       } catch (Exception e) {
         // TODO: Write an exceptions handler
         };
       };
 
     // Variables declaration
     private LinkedList<Word> matchFound;
     private javax.swing.JMenu jFileMenu;
     private javax.swing.JMenu jHelpMenu;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JButton jSearchButton;
     private javax.swing.JTextField jSearchField;
     private javax.swing.JLabel jPrintedText;
     private javax.swing.JMenuItem jExitMenuItem;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextArea jWordDef;
     private javax.swing.JComboBox jDictBox;
 
     // End of variables declaration
 }
