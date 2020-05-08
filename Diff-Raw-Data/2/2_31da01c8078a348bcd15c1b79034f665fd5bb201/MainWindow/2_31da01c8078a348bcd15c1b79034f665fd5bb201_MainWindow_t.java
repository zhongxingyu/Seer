 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import javax.swing.JFileChooser;
 import javax.swing.text.Document;
 import javax.swing.undo.*;
 import controller.Controller;
 import java.awt.Image;
 import java.awt.Point;
 import java.util.ArrayList;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JTextField;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.filechooser.FileFilter;
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
     String SavenLoad;
     
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
     @SuppressWarnings("LeakingThisInConstructor")
     public MainWindow() {
         initComponents();
         //setExtendedState(JFrame.MAXIMIZED_BOTH);
         setIconImage(icon);
         // se utiliza para los botones de cargado y guardado ---
         deliver.Deliver.setDestination(this);
         // ---
         eventArea.setEditable(false);
         eventArea.setForeground(Gray);
         vars = new ArrayList<String>();
         SavenLoad = "";
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
                 //ex.printStackTrace();
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
         jSplitPane1 = new javax.swing.JSplitPane();
         jScrollPane2 = new javax.swing.JScrollPane();
         syntaxArea = new javax.swing.JTextArea();
         jScrollPane1 = new javax.swing.JScrollPane();
         eventArea = new javax.swing.JTextArea();
         LoadEvent = new javax.swing.JButton();
         LoadSyntax = new javax.swing.JButton();
         SaveSyntax = new javax.swing.JButton();
         CerrarSintax = new javax.swing.JButton();
         NVariable = new javax.swing.JButton();
         NOperation = new javax.swing.JButton();
         NDataBase = new javax.swing.JButton();
         NTable = new javax.swing.JButton();
         deshacer = new javax.swing.JButton();
         editar = new javax.swing.JToggleButton();
         rehacer = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         DireccionSintax = new javax.swing.JLabel();
         DireccionEvent = new javax.swing.JLabel();
         AlcatelLogo = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setTitle("KiwiSyntaxManager");
         setFocusCycleRoot(false);
         setLocationByPlatform(true);
         setMinimumSize(new java.awt.Dimension(922, 494));
         setPreferredSize(new java.awt.Dimension(922, 494));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         jSplitPane1.setResizeWeight(0.5);
         jSplitPane1.setPreferredSize(new java.awt.Dimension(339, 407));
 
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
 
         CerrarSintax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/cross.png"))); // NOI18N
         CerrarSintax.setToolTipText("Sintax close");
         CerrarSintax.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CerrarSintaxActionPerformed(evt);
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
 
         jPanel1.setPreferredSize(new java.awt.Dimension(470, 20));
 
         DireccionSintax.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         DireccionSintax.setText("<Empty>");
         DireccionSintax.setPreferredSize(new java.awt.Dimension(34, 20));
 
         DireccionEvent.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         DireccionEvent.setText("<Empty>");
         DireccionEvent.setToolTipText("");
         DireccionEvent.setPreferredSize(new java.awt.Dimension(34, 20));
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addComponent(DireccionEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(DireccionSintax, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(DireccionSintax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(DireccionEvent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         AlcatelLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         AlcatelLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons2/Alcatel-LucentMini.png"))); // NOI18N
 
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
                 .addComponent(CerrarSintax, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                 .addComponent(deshacer, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rehacer, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 348, Short.MAX_VALUE)
                 .addComponent(AlcatelLogo))
             .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(LoadEvent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(SaveSyntax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(NVariable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(NOperation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(NDataBase, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(NTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(editar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(deshacer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(rehacer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(LoadSyntax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(AlcatelLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addComponent(CerrarSintax))
                 .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 898, Short.MAX_VALUE)
                 .addGap(0, 0, 0)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         deshacer.getAccessibleContext().setAccessibleDescription("");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void LoadEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadEventActionPerformed
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setMultiSelectionEnabled(false);
         fileChooser = setFilters(fileChooser, true, "text without format (*.txt)", "txt");
         if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
             Object[] what= new Object[1];
             what[0] = fileChooser.getSelectedFile().toString();
             Controller.controller(Controller.readInputEvent, what);
             DireccionEvent.setText((String) what[0]);
             eventArea.setCaretPosition(0);
         }
     }//GEN-LAST:event_LoadEventActionPerformed
 	
     private void LoadSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadSyntaxActionPerformed
         if (Cerrar()) {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setMultiSelectionEnabled(false);
             fileChooser = setFilters(fileChooser, true, "text without format (*.txt)", "txt");
             fileChooser = setFilters(fileChooser, false, "sintax file (*.stx)", "stx");
             if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                 syntaxArea.setText("");
                 Object[] what= new Object[1];
                 what[0] = fileChooser.getSelectedFile().toString();
                 Controller.controller(Controller.readInputSintax, what);
                 DireccionSintax.setText((String) what[0]);
             }
         }
     }//GEN-LAST:event_LoadSyntaxActionPerformed
 
     private void NVariableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NVariableActionPerformed
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
             JOptionPane.showMessageDialog(this, "Can't undo more", 
                     "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);           
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
             JOptionPane.showMessageDialog(this, "Can't redo more", 
                     "ERROR MESSAGE", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_redoActionPerformed
   
     private void NOperationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NOperationActionPerformed
         JDialog no = new NewOperations(this);
         no.setVisible(true);
     }//GEN-LAST:event_NOperationActionPerformed
     
     private void SaveSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveSyntaxActionPerformed
         logicSaveFile();
     }//GEN-LAST:event_SaveSyntaxActionPerformed
 
     private void NTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NTableActionPerformed
         JDialog jd = new NewTable(this);
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
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         if (Cerrar()) {System.exit(0);}
     }//GEN-LAST:event_formWindowClosing
 
     private void CerrarSintaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CerrarSintaxActionPerformed
         if (Cerrar()) {
             syntaxArea.setText("");
             DireccionSintax.setText("<Empty>");
         }
     }//GEN-LAST:event_CerrarSintaxActionPerformed
       
     private boolean Cerrar() { 
         boolean gc = true;
         if (!syntaxArea.getText().equalsIgnoreCase("")) {
             int res = JOptionPane.showConfirmDialog(this, "Save sintax file?", 
                     "WARNING", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
             if (res == JOptionPane.YES_OPTION) {
                 logicSaveFile();
             } else if (res == JOptionPane.CANCEL_OPTION) {
                 gc = false;
             }
         }
         return gc;
     }
     
     private JTextField oldColName;
     private JTextField newColName;
     private JTextField tf;
     private JFrame f;
 
     //<editor-fold defaultstate="collapsed" desc=" Make's Room ">
     private String preMakeRoom(javax.swing.JTextArea Area) {
         String bs = "";
         int act = Area.getCaretPosition();
         try {
             String ant = Area.getText(act - 1, 1);
             if (!ant.equalsIgnoreCase("\n")) {
                 bs = "\n";
             }
         } catch (BadLocationException ex) {}
         return bs;
     }
 
     private String postMakeRoom(javax.swing.JTextArea Area) {
         String bs = "";
         int act = Area.getCaretPosition();
         try {
             String sig = Area.getText(act, 1);
             if (sig.equalsIgnoreCase("\n")) {
                 Area.setCaretPosition(Area.getCaretPosition() + 1);
             } else {
                 bs = "\n";
             }
         } catch (Exception ex) {
             bs = "\n";
         }
         Area.setCaretPosition(act);
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
         Area.insert(preMakeRoom(Area) + str + postMakeRoom(Area), 
                 Area.getCaretPosition());
     }
     //</editor-fold>
     
     /*
      * CONTENIDO DEL BOTON DE FAILUREMANAGER
      * JDialog jd = new FailureManager(this, true, this.vars);
      * jd.setLocationByPlatform(true);
      * jd.setLocationRelativeTo(this);
      * jd.setVisible(true);
     */
 
     public void addVars(String vars) {
         this.vars.add(vars);
     }
     
     //mantener el nombre del cargado para el guardado
     
     private void saveFile(JFileChooser jfc, String extension) {
         Object[] what = new Object[2];
         what[0] = syntaxArea.getText();
         what[1] = jfc.getSelectedFile().toString() + extension;
         Controller.controller(Controller.writeOutput, what);   
         DireccionSintax.setText((String) what[1]);
         //syntaxArea.setText("");        
     }
     
     // <editor-fold defaultstate="collapsed" desc=" Logica e interfaz de guardado ">
     private void logicSaveFile() {
         JFileChooser jfc = new JFileChooser();
         jfc = setFilters(jfc, true, "sintax file (*.stx)", "stx");
         jfc.setMultiSelectionEnabled(false);
         jfc.setVisible(true);
         boolean repeat = true;
         int Guardar;
         do {
             Guardar = jfc.showSaveDialog(this);
             if(Guardar == JFileChooser.APPROVE_OPTION){
                 if (jfc.getSelectedFile().exists()) {
                     int res = JOptionPane.showConfirmDialog(this, "Are you sure to overwrite the file?",
                            "WARNING", JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                     if (res == JOptionPane.YES_OPTION) {
                         repeat = false;
                         saveFile(jfc, "");
                     } else {repeat = true;}
                 } else {
                     repeat = false;
                     jfc.setSelectedFile(new File(getValidName(jfc.getSelectedFile().toString(),
                             getExtension(jfc.getFileFilter()))));
                     saveFile(jfc, "." + getExtension(jfc.getFileFilter()));
                 }
             }
         } while (repeat && !(Guardar == JFileChooser.CANCEL_OPTION));
     }
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" metodo de Ayuda de guardado y cargado ">
     private String getValidName(String name, String extension) {
         String cleanName = name;
         String[] splits = name.split("\\.");
         if (splits.length != 0) {
             if (splits[splits.length-1].equalsIgnoreCase(extension)) {
                 cleanName = "";
                 for(int i = 0; i < splits.length-2; i++) {
                     cleanName += splits[i] + ".";
                 }
                 cleanName += splits[splits.length-2];
             }
         }
         return cleanName;
     }
     
     private String getExtension(FileFilter filter) {
         int ind = filter.toString().indexOf("extensions=[") + 12;
         int fin = filter.toString().length()-2;
         return filter.toString().substring(ind, fin);
     }
     
     private JFileChooser setFilters(JFileChooser jfc, boolean predefined, String comment, String extension) {
         if (predefined || 
                 !jfc.getFileFilter().getDescription().equalsIgnoreCase("Todos los Archivos")) {
             jfc.setAcceptAllFileFilterUsed(false);
         }
         FileNameExtensionFilter newFilter = new FileNameExtensionFilter(comment, extension);
         jfc.addChoosableFileFilter(newFilter);
         if (predefined) {jfc.setFileFilter(newFilter);}
         return jfc;
     }
     // </editor-fold>
     
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
     private javax.swing.JLabel AlcatelLogo;
     private javax.swing.JButton CerrarSintax;
     private javax.swing.JLabel DireccionEvent;
     private javax.swing.JLabel DireccionSintax;
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
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JButton rehacer;
     private javax.swing.JTextArea syntaxArea;
     // End of variables declaration//GEN-END:variables
 }
