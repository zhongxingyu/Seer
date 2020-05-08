 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import business.Output;
 import java.awt.Color;
 import java.awt.Event;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileReader;
 import java.util.Scanner;
 import javax.swing.JFileChooser;
 import javax.swing.text.Document;
 import javax.swing.undo.*;
 import java.lang.Object;
 import controller.Controller;
 import deliver.Deliver;
 import java.awt.BorderLayout;
 import java.awt.Image;
 import java.awt.event.ActionListener;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.BadLocationException;
 import javax.swing.undo.CannotUndoException;
 
 /**
  *
  * @author Kiwi
  */
 public class MainWindow extends javax.swing.JFrame {
 
     //Colores para letras
     public static final Color Red = new Color(255, 0, 0);
     public static final Color Black = new Color(0, 0, 0);
     public static final Color Gray = new Color(109, 109, 109);
 
     private Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons2/icon-kiwi.png"));
 
     public ArrayList<String> vars;
     
     //<editor-fold defaultstate="collapsed" desc=" Undo & Redo part1 ">
     private Document editorPaneDocument;
     protected UndoHandler undoHandler = new UndoHandler();
     protected UndoManager undoManager = new UndoManager();
     private UndoAction undoAction = null;
     private RedoAction redoAction = null;
     //</editor-fold>
 
     /**
      * Creates new form NewJFrame
      */
     public MainWindow() {
         initComponents();
         setExtendedState(JFrame.MAXIMIZED_BOTH);
         setIconImage(icon);
         vars = new ArrayList<String>();
         editorPaneDocument = syntaxArea.getDocument();
         editorPaneDocument.addUndoableEditListener(undoHandler);
 
         KeyStroke undoKeystroke = KeyStroke.getKeyStroke("control Z");
         KeyStroke redoKeystroke = KeyStroke.getKeyStroke("control Y");
 
         undoAction = new UndoAction();
         syntaxArea.getInputMap().put(undoKeystroke, "undoKeystroke");
         syntaxArea.getActionMap().put("undoKeystroke", undoAction);
 
         redoAction = new RedoAction();
         syntaxArea.getInputMap().put(redoKeystroke, "redoKeystroke");
         syntaxArea.getActionMap().put("redoKeystroke", redoAction);
 
         // Edit menu
         JMenu editMenu = new JMenu("Edit");
         JMenuItem undoMenuItem = new JMenuItem(undoAction);
         JMenuItem redoMenuItem = new JMenuItem(redoAction);
         editMenu.add(undoMenuItem);
         editMenu.add(redoMenuItem);
         Deliver.setDestination(this);
 
 
     }
 
     public void setSyntaxText(String text) {
         //syntaxArea.append(text);
         insertText(syntaxArea, text);
     }
     
     public void setEventText(String text) {
         insertText(eventArea, text);
     }
 
     //<editor-fold defaultstate="collapsed" desc=" Undo & Redo part2">
     class UndoHandler implements UndoableEditListener {
 
         /**
          * Messaged when the Document has created an edit, the edit is added to
          * <code>undoManager</code>, an instance of UndoManager.
          */
         @Override
         public void undoableEditHappened(UndoableEditEvent e) {
             undoManager.addEdit(e.getEdit());
             undoAction.update();
             redoAction.update();
         }
     }
 
     class UndoAction extends AbstractAction {
 
         public UndoAction() {
             super("Undo");
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             try {
                 undoManager.undo();
             } catch (CannotUndoException ex) {
                 // TODO deal with this
                 //ex.printStackTrace();
             }
             update();
             redoAction.update();
         }
 
         protected void update() {
             if (undoManager.canUndo()) {
                 setEnabled(true);
                 putValue(Action.NAME, undoManager.getUndoPresentationName());
             } else {
                 setEnabled(false);
                 putValue(Action.NAME, "Undo");
             }
         }
     }
 
     class RedoAction extends AbstractAction {
 
         public RedoAction() {
             super("Redo");
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             try {
                 undoManager.redo();
             } catch (CannotRedoException ex) {
                 // TODO deal with this
                 ex.printStackTrace();
             }
             update();
             undoAction.update();
         }
 
         protected void update() {
             if (undoManager.canRedo()) {
                 setEnabled(true);
                 putValue(Action.NAME, undoManager.getRedoPresentationName());
             } else {
                 setEnabled(false);
                 putValue(Action.NAME, "Redo");
             }
         }
     }
     //</editor-fold>
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jFileChooser1 = new javax.swing.JFileChooser();
         jFrame1 = new javax.swing.JFrame();
         jLabel1 = new javax.swing.JLabel();
         jSplitPane1 = new javax.swing.JSplitPane();
         jScrollPane2 = new javax.swing.JScrollPane();
         syntaxArea = new javax.swing.JTextArea();
         jScrollPane1 = new javax.swing.JScrollPane();
         eventArea = new javax.swing.JTextArea();
         LoadEvent = new javax.swing.JButton();
         LoadSyntax = new javax.swing.JButton();
         SaveSyntax = new javax.swing.JButton();
         NVariable = new javax.swing.JButton();
         NOperation = new javax.swing.JButton();
         NDataBase = new javax.swing.JButton();
         NTable = new javax.swing.JButton();
         deshacer = new javax.swing.JButton();
         editar = new javax.swing.JToggleButton();
         rehacer = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         FailureManager = new javax.swing.JButton();
 
         jFrame1.setBounds(new java.awt.Rectangle(0, 0, 225, 206));
         jFrame1.setLocationByPlatform(true);
 
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setText("<html><img src=\"http://farm4.staticflickr.com/3227/3115937621_650616f2b0.jpg\" width=210 height=180></html>");
 
         javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
         jFrame1.getContentPane().setLayout(jFrame1Layout);
         jFrame1Layout.setHorizontalGroup(
             jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jFrame1Layout.createSequentialGroup()
                 .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 15, Short.MAX_VALUE))
         );
         jFrame1Layout.setVerticalGroup(
             jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jFrame1Layout.createSequentialGroup()
                 .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(38, Short.MAX_VALUE))
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("KiwiSyntaxManager");
         setFocusCycleRoot(false);
         setLocationByPlatform(true);
 
         jSplitPane1.setResizeWeight(0.5);
 
         jScrollPane2.setToolTipText("");
 
         syntaxArea.setColumns(20);
         syntaxArea.setRows(5);
         jScrollPane2.setViewportView(syntaxArea);
 
         jSplitPane1.setRightComponent(jScrollPane2);
 
         eventArea.setColumns(20);
         eventArea.setRows(5);
         jScrollPane1.setViewportView(eventArea);
 
         jSplitPane1.setLeftComponent(jScrollPane1);
 
         LoadEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/page_white_lightning.png"))); // NOI18N
         LoadEvent.setToolTipText("Load Event");
         LoadEvent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 LoadEventActionPerformed(evt);
             }
         });
 
         LoadSyntax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/page_white_put.png"))); // NOI18N
         LoadSyntax.setToolTipText("Load Syntax");
         LoadSyntax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 LoadSyntaxActionPerformed(evt);
             }
         });
 
         SaveSyntax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/disk.png"))); // NOI18N
         SaveSyntax.setToolTipText("Save");
         SaveSyntax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 SaveSyntaxActionPerformed(evt);
             }
         });
 
         NVariable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/tag_blue_add.png"))); // NOI18N
         NVariable.setToolTipText("New Variable");
         NVariable.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 NVariableActionPerformed(evt);
             }
         });
 
         NOperation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/cog_add.png"))); // NOI18N
         NOperation.setToolTipText("New Operation");
         NOperation.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 NOperationActionPerformed(evt);
             }
         });
 
         NDataBase.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/database_add.png"))); // NOI18N
         NDataBase.setToolTipText("New DataBase");
         NDataBase.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 NDataBaseActionPerformed(evt);
             }
         });
 
         NTable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/table_add.png"))); // NOI18N
         NTable.setToolTipText("New Table");
         NTable.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 NTableActionPerformed(evt);
             }
         });
 
         deshacer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/arrow_undo.png"))); // NOI18N
         deshacer.setToolTipText("Undo");
         deshacer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 undoActionPerformed(evt);
             }
         });
 
         editar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/page_white_edit.png"))); // NOI18N
         editar.setToolTipText("Edit Code");
         editar.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/page_white_delete.png"))); // NOI18N
         editar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editarActionPerformed(evt);
             }
         });
 
         rehacer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/arrow_redo.png"))); // NOI18N
         rehacer.setToolTipText("Redo");
         rehacer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 redoActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 20, Short.MAX_VALUE)
         );
 
         FailureManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/bug_edit.png"))); // NOI18N
         FailureManager.setToolTipText("Failure manager");
        FailureManager.setEnabled(false);
         FailureManager.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 FailureManagerActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(LoadEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(LoadSyntax, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(SaveSyntax, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(NVariable, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(NOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(NDataBase, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(NTable, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(editar, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(FailureManager, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(deshacer, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rehacer, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 906, Short.MAX_VALUE)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(LoadEvent)
                     .addComponent(SaveSyntax)
                     .addComponent(NVariable)
                     .addComponent(NOperation)
                     .addComponent(NDataBase)
                     .addComponent(NTable)
                     .addComponent(editar)
                     .addComponent(deshacer)
                     .addComponent(rehacer)
                     .addComponent(LoadSyntax)
                     .addComponent(FailureManager))
                 .addGap(1, 1, 1)
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         deshacer.getAccessibleContext().setAccessibleDescription("");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void LoadEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadEventActionPerformed
         // TODO add your handling code here:
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt format", "txt");
         fileChooser.setAcceptAllFileFilterUsed(false);
         fileChooser.setFileFilter(filter);
         int seleccion = fileChooser.showOpenDialog(this);
         if (seleccion == JFileChooser.APPROVE_OPTION) {
             String storeAllString = "";
             File fichero = fileChooser.getSelectedFile();
             // Aquí debemos abrir y leer el fichero.
             Object[] what;
             what = new Object[1];
             what[0] = fichero.toString();
             try {
                 Controller.controller(Controller.readInput, what);
             } catch (Exception e) {
             }
             eventArea.setCaretPosition(0);
         }
     }//GEN-LAST:event_LoadEventActionPerformed
 	
 	private void LoadSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadSyntaxActionPerformed
         // TODO add your handling code here:
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt and .stx format", "txt", "stx");
         fileChooser.setAcceptAllFileFilterUsed(false);
         fileChooser.setFileFilter(filter);
         int seleccion = fileChooser.showOpenDialog(this);
         fileChooser.setMultiSelectionEnabled(false);
         if (seleccion == JFileChooser.APPROVE_OPTION) {
             String storeAllString = "";
             File fichero = fileChooser.getSelectedFile();
             // Aquí debemos abrir y leer el fichero.
             try {
                 FileReader readTextFile = new FileReader(fichero.toString());
                 Scanner fileReaderScan = new Scanner(readTextFile);
                 while (fileReaderScan.hasNextLine()) {
                     String temp = fileReaderScan.nextLine() + "\n";
                     storeAllString = storeAllString + temp;
                 }
             } catch (Exception e) {
             }
             syntaxArea.setText(storeAllString);
             syntaxArea.setCaretPosition(0);
         }
     }//GEN-LAST:event_LoadSyntaxActionPerformed
 
     private void NVariableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NVariableActionPerformed
         // TODO add your handling code here:
         JDialog jd = new NewVariable(this);
         jd.setLocationByPlatform(true);
         jd.setModal(true);
         jd.setLocationRelativeTo(this);
         jd.setVisible(true);
     }//GEN-LAST:event_NVariableActionPerformed
 
     private void undoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoActionPerformed
         try {
             undoManager.undo();
         } catch (CannotUndoException cre) {
             
             JOptionPane op = new JOptionPane();
             op.showMessageDialog(this, "Can't undo more", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
             
         }
 
     }//GEN-LAST:event_undoActionPerformed
 
     private void editarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarActionPerformed
         // TODO add your handling code here:
         if (syntaxArea.isEditable()) {
             syntaxArea.setEditable(false);
             syntaxArea.setForeground(Gray);
         } else {
             syntaxArea.setEditable(true);
             syntaxArea.setForeground(Black);
         }
     }//GEN-LAST:event_editarActionPerformed
 
     private void redoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoActionPerformed
         try {
             undoManager.redo();
         } catch (CannotRedoException cre) {
             
             JOptionPane op = new JOptionPane();
             op.showMessageDialog(this, "Can't redo more", "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
             
         }
     }//GEN-LAST:event_redoActionPerformed
   
     private void NOperationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NOperationActionPerformed
         JDialog no = new NewOperations(this);
         no.setVisible(true);
     }//GEN-LAST:event_NOperationActionPerformed
     
     private void SaveSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveSyntaxActionPerformed
         
         JFileChooser jfc = new JFileChooser();
         jfc.setMultiSelectionEnabled(false);
         jfc.setVisible(true);
         
         if(JFileChooser.APPROVE_OPTION == jfc.showSaveDialog(this)){
            Object[] what = new Object[2];
            what[0] = syntaxArea.getText();
            what[1] = jfc.getSelectedFile();
            Controller.controller(Controller.writeOutput, what);
         }
         
         /*
         f = new JFrame("Write a file name");
         f.setLayout(new BorderLayout());
         
         f.setVisible(true);
         
         this.tf = new JTextField("syntax");
         JButton confirmButton = new JButton("Ok");
         JButton defaultButton = new JButton("Default: syntax");
         JButton cancelButton = new JButton("Cancel");
         
         f.add(tf, BorderLayout.NORTH);
         f.add(confirmButton, BorderLayout.WEST);
         f.add(defaultButton, BorderLayout.CENTER);
         f.add(cancelButton, BorderLayout.SOUTH);
         
         f.pack();
         f.setLocation(this.getLocation().x + this.getWidth()/2 - f.getWidth()/2, this.getLocation().y + this.getHeight()/2 - f.getHeight()/2);
         
         
         confirmButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 business.Output.publishOutput(syntaxArea.getText(), tf.getText());
                 f.dispose();
             }
         });
         
         defaultButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 business.Output.publishOutput(syntaxArea.getText(), "");
                 f.dispose();
             }
         });
         
         cancelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 f.dispose();
             }
         });
         */
     }//GEN-LAST:event_SaveSyntaxActionPerformed
 
     private void NTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NTableActionPerformed
         JDialog jd = new NewTable();
         jd.setLocationByPlatform(true);
         jd.setLocationRelativeTo(this);
         jd.setModal(true);
         jd.setVisible(true);
         
         
     }//GEN-LAST:event_NTableActionPerformed
 
     private void NDataBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NDataBaseActionPerformed
         JDialog jd = new NewBBDD(this, true);
         jd.setLocationByPlatform(true);
         jd.setLocationRelativeTo(this);
         jd.setVisible(true);
     }//GEN-LAST:event_NDataBaseActionPerformed
 
     private void FailureManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FailureManagerActionPerformed
         JDialog jd = new FailureManager(this.vars);
         jd.setLocationByPlatform(true);
         jd.setLocationRelativeTo(this);
         jd.setVisible(true);
     }//GEN-LAST:event_FailureManagerActionPerformed
     
     private JTextField oldColName;
     private JTextField newColName;
     private JTextField tf;
     private JFrame f;
 
     //<editor-fold defaultstate="collapsed" desc=" Make's Room ">
     private String preMakeRoom(javax.swing.JTextArea Area) {
         String bs = "";
         int act = Area.getCaretPosition();
         try {
             String sig = Area.getText(act - 1, 1);
             if (!sig.equalsIgnoreCase("\n")) {
                 //Area.insert("\n", Area.getCaretPosition());
                 bs = "\n";
             }
         } catch (BadLocationException ex) {
         }
         return bs;
     }
 
     private String postMakeRoom(javax.swing.JTextArea Area) {
         String bs = "";
         int act = Area.getCaretPosition();
         Area.setCaretPosition(Area.getDocument().getLength());
         int fin = Area.getCaretPosition();
         Area.setCaretPosition(act);
         try {
             String sig = Area.getText(act, 1);
             if (act != fin && sig.equalsIgnoreCase("\n")) {
                 Area.setCaretPosition(Area.getCaretPosition() + 1);
             } else {
                 //Area.insert("\n", Area.getCaretPosition());
                 bs = "\n";
             }
         } catch (BadLocationException ex) {
             //Area.insert("\n", Area.getCaretPosition());
             bs = "\n";
         }
         return bs;
     }
 
     /**
      * Inserta el parametro String en el parametor JTextArea haciendo un "hueco"
      * en el texto, si fuera necesario
      *
      * @param Area JTextArea
      * @param str String
      */
     private void insertText(javax.swing.JTextArea Area, String str) {
         Area.insert(preMakeRoom(Area) + str
                 + postMakeRoom(Area), Area.getCaretPosition());
     }
     //</editor-fold>
 
     public void addVars(String vars) {
         this.vars.add(vars);
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 
                 new MainWindow().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton FailureManager;
     private javax.swing.JButton LoadEvent;
     private javax.swing.JButton LoadSyntax;
     private javax.swing.JButton NDataBase;
     private javax.swing.JButton NOperation;
     private javax.swing.JButton NTable;
     private javax.swing.JButton NVariable;
     private javax.swing.JButton SaveSyntax;
     private javax.swing.JButton deshacer;
     private javax.swing.JToggleButton editar;
     private javax.swing.JTextArea eventArea;
     private javax.swing.JFileChooser jFileChooser1;
     private javax.swing.JFrame jFrame1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JButton rehacer;
     private javax.swing.JTextArea syntaxArea;
     // End of variables declaration//GEN-END:variables
 }
