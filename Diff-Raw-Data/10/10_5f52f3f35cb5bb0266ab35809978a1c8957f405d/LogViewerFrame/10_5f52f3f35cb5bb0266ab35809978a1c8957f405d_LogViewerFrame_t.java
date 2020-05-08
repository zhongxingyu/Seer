 /**
  * Open Wonderland
  *
  * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * The Open Wonderland Foundation designates this particular file as
  * subject to the "Classpath" exception as provided by the Open Wonderland
  * Foundation in the License file that accompanied this code.
  */
 
 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.client.jme;
 
 import com.jme.renderer.jogl.JOGLContextCapabilities;
 import com.jme.system.DisplaySystem;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.TreeMap;
 import java.util.concurrent.Semaphore;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.ParagraphView;
 import javax.swing.text.Position;
 import javax.swing.text.StyledEditorKit;
 import javax.swing.text.View;
 import javax.swing.text.ViewFactory;
 import org.jdesktop.mtgame.RenderManager;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
 
 /**
  * A log viewer.
  * @author Jonathan Kaplan <kaplanj@dev.java.net>
  */
 public class LogViewerFrame extends javax.swing.JFrame {
     private static final Logger logger = 
             Logger.getLogger(LogViewerFrame.class.getName());
 
     private static ResourceBundle BUNDLE =
                 ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/Bundle");
 
     /** log levels */
     private static final Level[] LOG_LEVELS = new Level[] {
         Level.SEVERE, Level.WARNING, Level.INFO,
         Level.FINE, Level.FINER, Level.FINEST,
         Level.ALL, Level.OFF
     };
 
     /** the log handler */
     private LogViewerHandler handler;
 
     /** the queue of work to process */
     private List<LogRecord> workQueue = new LinkedList<LogRecord>();
 
     /** the list of currently displayed records */
     private final List<LogEntry> entries = new LinkedList<LogEntry>();
     
     /** the maximum number of entries */
     private int maxEntries = 1000;
 
     /** whether the viewer is visible on startup */
     private boolean visibleOnStartup = false;
 
     /**
      * Get the global log viewer frame singleton
      * @return the LogViewerFrame singleton
      */
     public static LogViewerFrame getInstance() {
         return SingletonHolder.INSTANCE;
     }
 
     /** Creates new form LogViewerFrame */
     protected LogViewerFrame() {
         initComponents();
 
         logPane.setEditorKit(new NoWrapEditorKit());
         errorText.addCaretListener(new CaretListener() {
             public void caretUpdate(CaretEvent ce) {
                 String sel = errorText.getSelectedText();
                 errorCopyButton.setEnabled(sel != null);
             }
         });
 
         // make sure the logPane starts scrolled to the bottom
         ((ManualScrollEditorPane) logPane).scrollToEnd();
         
         levelCB.setModel(new DefaultComboBoxModel(LOG_LEVELS));
 
         // reload the preferred log levels
         LoggerTableModel levelModel = new LoggerTableModel();
         levelModel.restore();
         levelTable.setModel(levelModel);
 
         // set up the table
         JComboBox tableLevelCB = new JComboBox();
         tableLevelCB.setModel(new DefaultComboBoxModel(LOG_LEVELS));
         levelTable.getColumnModel().getColumn(1).setCellEditor(
                 new DefaultCellEditor(tableLevelCB));
         levelTable.getColumnModel().getColumn(0).setPreferredWidth(85);
         levelTable.getColumnModel().getColumn(1).setPreferredWidth(15);
         levelTable.getSelectionModel().addListSelectionListener(
                 new ListSelectionListener() 
         {
             public void valueChanged(ListSelectionEvent lse) {
                 if (levelTable.getSelectedRow() != -1) {
                     levelTableMinus.setEnabled(true);
                 } else {
                     levelTableMinus.setEnabled(false);
                 }
             }   
         });
         
         // restore default values
         restore();
     }
 
     private void restore() {
         Preferences prefs = Preferences.userNodeForPackage(LogViewerFrame.class);
 
         maxEntries = prefs.getInt("maxEntries", maxEntries);
 
         Logger root = LogManager.getLogManager().getLogger("");
         Level levelVal = Level.parse(prefs.get("rootLevel", root.getLevel().getName()));
         root.setLevel(levelVal);
 
         visibleOnStartup = prefs.getBoolean("visibleOnStartup", visibleOnStartup);
     }
     
     public LogViewerHandler getHandler() {
         return handler;
     }
 
     public void setHandler(LogViewerHandler handler) {
         this.handler = handler;
     }
 
     public int getMaxEntries() {
         return maxEntries;
     }
 
     public void setMaxEntries(int maxEntries) {
         if (maxEntries <= 0) {
             throw new IllegalArgumentException("Maximum entries must be " +
                                                "more than 0");
         }
 
         this.maxEntries = maxEntries;
 
         // save a preference
         Preferences prefs = Preferences.userNodeForPackage(LogViewerFrame.class);
         prefs.putInt("maxEntries", maxEntries);
 
         // processing records now will correctly remove any records over
         // the new limit
         processRecords();
     }
 
     public Level getRootLogLevel() {
         return Logger.getLogger("").getLevel();
     }
 
     public void setRootLogLevel(Level rootLevel) {
         Logger.getLogger("").setLevel(rootLevel);
 
         // save a preference
         Preferences prefs = Preferences.userNodeForPackage(LogViewerFrame.class);
         prefs.put("rootLevel", rootLevel.getName());
     }
 
     public boolean isVisibleOnStartup() {
         return visibleOnStartup;
     }
 
     public void setVisibleOnStartup(boolean visibleOnStartup) {
         this.visibleOnStartup = visibleOnStartup;
 
         // save a preference
         Preferences prefs = Preferences.userNodeForPackage(LogViewerFrame.class);
         prefs.putBoolean("visibleOnStartup", visibleOnStartup);
     }
 
     /**
      * Called by the handler to add a new record to the log. This method queues
      * the record and schedules the actual update to happen on the AWT
      * event thread.
      * @param record the record to process
      */
     protected synchronized void addRecord(LogRecord record) {
         // OWL issue #160: queue up multiple events to improve performance
 
         // if the queue is empty, then we need to schedule a task
         // to clear the list. If the list is not empty, it means a task
         // has been scheduled but has not yet run. In that case, we can
         // just add our elements to the list and they will be processed
         // by the eventual task
         boolean schedule = workQueue.isEmpty();
 
         // add our record
         workQueue.add(record);
 
         // schedule a task if necessary
         if (schedule) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     processRecords();
                 }
             });
         }
     }
 
     /**
      * Called on the AWT event thread to process all outstanding records
      */
     protected void processRecords() {
         // OWL issue #160: handle multiple events to improve performance
 
         // first, get the set of records to process, making sure we have the
         // lock to prevent additions
         Collection<LogRecord> records;
         synchronized (this) {
             // copy the records into a new collection
             records = new ArrayList<LogRecord>(workQueue);
 
             // clear the work queue. This ensures that the next time an element
             // is added, a new task will be scheduled
             workQueue.clear();
         }
 
         // process each record
         StringBuilder str = new StringBuilder();
         for (LogRecord record : records) {
             // format the record and add it to the strig builder
             int length = format(record, str);
 
             // add it to the list of records
             LogEntry entry = new LogEntry();
             entry.record = record;
             entry.length = length;
             entries.add(entries.size(), entry);
         }
 
         // if we are now over the maximum number of entries, remove as many
         // as we need
         int removeLen = 0;
         while (entries.size() > getMaxEntries()) {
             LogEntry entry = entries.remove(0);
             removeLen += entry.length;
         }
 
         // update the scrolling information for the panel
         ManualScrollEditorPane mspe = (ManualScrollEditorPane) logPane;
         Position pos = mspe.preModify();
         boolean atEnd = mspe.atEnd();
 
         // add the new text to the end of the current document
         try {
             final Document doc = logPane.getDocument();
 
             // if there is text to remove, remove it first
             if (removeLen > 0) {
                 doc.remove(0, removeLen);
             }
 
             Position end = doc.getEndPosition();
             doc.insertString(end.getOffset() - 1, str.toString(), null);
         } catch (BadLocationException ble) {
             // should never happen
             logger.log(Level.WARNING, "Bad location", ble);
             return;
         }
 
         // now that everything is updated, have the panel scroll to the
         // right place
         mspe.postModify(pos, atEnd);
     }
 
     /**
      * Format the given record, and add it to the given string builder. Return
      * the length of text added to the builder.
      */
     protected int format(LogRecord record, StringBuilder builder) {
         int startLen = builder.length();
         
         builder.append(record.getLevel());
         builder.append(" ");
         builder.append(DateFormat.getTimeInstance().format(new Date(record.getMillis())));
         builder.append(" ");
         builder.append(record.getSourceClassName());
         builder.append(" ");
         builder.append(record.getSourceMethodName());
         builder.append("\n");
 
         if (record.getMessage() != null) {
             builder.append(record.getMessage());
             builder.append("\n");
         }
 
         if (record.getThrown() != null) {
             builder.append(formatThrowable(record.getThrown()));
         }
 
         // return the difference in length from when we started
         return builder.length() - startLen;
     }
 
     protected String formatThrowable(Throwable t) {
         StringWriter out = new StringWriter();
         t.printStackTrace(new PrintWriter(out));
         return out.toString();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         configDialog = new javax.swing.JDialog();
         jLabel1 = new javax.swing.JLabel();
         backlogTF = new javax.swing.JTextField();
         configCancelButton = new javax.swing.JButton();
         okButton = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         levelCB = new javax.swing.JComboBox();
         jLabel3 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         levelTable = new javax.swing.JTable();
         levelTablePlus = new javax.swing.JButton();
         levelTableMinus = new javax.swing.JButton();
         jLabel4 = new javax.swing.JLabel();
         startupCB = new javax.swing.JCheckBox();
         errorReportDialog = new javax.swing.JDialog();
         errorScrollPane = new javax.swing.JScrollPane();
         errorText = new javax.swing.JTextArea();
         errorCloseButton = new javax.swing.JButton();
         errorCopyButton = new javax.swing.JButton();
         errorSelectButton = new javax.swing.JButton();
         logScrollPane = new javax.swing.JScrollPane();
         logPane = new ManualScrollEditorPane();
         logPane.setText("");
         configureButton = new javax.swing.JButton();
         closeButton = new javax.swing.JButton();
         errorReportButton = new javax.swing.JButton();
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/Bundle"); // NOI18N
         configDialog.setTitle(bundle.getString("LogViewerFrame.configDialog.title")); // NOI18N
 
         jLabel1.setText(bundle.getString("LogViewerFrame.jLabel1.text")); // NOI18N
 
         configCancelButton.setText(bundle.getString("LogViewerFrame.configCancelButton.text")); // NOI18N
         configCancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configCancelButtonActionPerformed(evt);
             }
         });
 
         okButton.setText(bundle.getString("LogViewerFrame.okButton.text")); // NOI18N
         okButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 okButtonActionPerformed(evt);
             }
         });
 
         jLabel2.setText(bundle.getString("LogViewerFrame.jLabel2.text")); // NOI18N
 
         levelCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NOT SET" }));
 
         jLabel3.setText(bundle.getString("LogViewerFrame.jLabel3.text")); // NOI18N
 
         levelTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null}
             },
             new String [] {
                 "Logger", "Level"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Object.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         levelTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane1.setViewportView(levelTable);
 
         levelTablePlus.setText(bundle.getString("LogViewerFrame.levelTablePlus.text")); // NOI18N
         levelTablePlus.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 levelTablePlusActionPerformed(evt);
             }
         });
 
         levelTableMinus.setText(bundle.getString("LogViewerFrame.levelTableMinus.text")); // NOI18N
         levelTableMinus.setEnabled(false);
         levelTableMinus.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 levelTableMinusActionPerformed(evt);
             }
         });
 
         jLabel4.setText(bundle.getString("LogViewerFrame.jLabel4.text")); // NOI18N
 
         org.jdesktop.layout.GroupLayout configDialogLayout = new org.jdesktop.layout.GroupLayout(configDialog.getContentPane());
         configDialog.getContentPane().setLayout(configDialogLayout);
         configDialogLayout.setHorizontalGroup(
             configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(configDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(configDialogLayout.createSequentialGroup()
                         .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(configDialogLayout.createSequentialGroup()
                                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(configDialogLayout.createSequentialGroup()
                                         .add(jLabel1)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(backlogTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                     .add(configDialogLayout.createSequentialGroup()
                                         .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(jLabel2)
                                             .add(jLabel4))
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                         .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(levelCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                             .add(startupCB)))
                                     .add(jLabel3))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE))
                             .add(configDialogLayout.createSequentialGroup()
                                 .add(okButton)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)))
                         .add(configCancelButton))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, configDialogLayout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(levelTableMinus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(levelTablePlus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap())
         );
         configDialogLayout.setVerticalGroup(
             configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(configDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(backlogTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(levelCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(6, 6, 6)
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jLabel4)
                     .add(startupCB))
                 .add(18, 18, 18)
                 .add(jLabel3)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(configDialogLayout.createSequentialGroup()
                         .add(levelTablePlus)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(levelTableMinus))
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(configDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(configCancelButton)
                     .add(okButton)))
         );
 
         errorReportDialog.setTitle(bundle.getString("LogViewerFrame.errorReportDialog.title")); // NOI18N
 
         errorText.setColumns(20);
         errorText.setEditable(false);
         errorText.setRows(5);
         errorText.setWrapStyleWord(true);
         errorScrollPane.setViewportView(errorText);
 
         errorCloseButton.setText(bundle.getString("LogViewerFrame.errorCloseButton.text")); // NOI18N
         errorCloseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 errorCloseButtonActionPerformed(evt);
             }
         });
 
         errorCopyButton.setText(bundle.getString("LogViewerFrame.errorCopyButton.text")); // NOI18N
         errorCopyButton.setEnabled(false);
         errorCopyButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 errorCopyButtonActionPerformed(evt);
             }
         });
 
         errorSelectButton.setText(bundle.getString("LogViewerFrame.errorSelectButton.text")); // NOI18N
         errorSelectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 errorSelectButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout errorReportDialogLayout = new org.jdesktop.layout.GroupLayout(errorReportDialog.getContentPane());
         errorReportDialog.getContentPane().setLayout(errorReportDialogLayout);
         errorReportDialogLayout.setHorizontalGroup(
             errorReportDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, errorReportDialogLayout.createSequentialGroup()
                 .add(305, 305, 305)
                 .add(errorSelectButton)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(errorCopyButton)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(errorCloseButton))
             .add(errorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
         );
         errorReportDialogLayout.setVerticalGroup(
             errorReportDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, errorReportDialogLayout.createSequentialGroup()
                 .add(errorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(errorReportDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(errorCloseButton)
                     .add(errorCopyButton)
                     .add(errorSelectButton)))
         );
 
         setTitle(bundle.getString("LogViewerFrame.title")); // NOI18N
 
         logPane.setEditable(false);
         logScrollPane.setViewportView(logPane);
 
         configureButton.setText(bundle.getString("LogViewerFrame.configureButton.text")); // NOI18N
         configureButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configureButtonActionPerformed(evt);
             }
         });
 
         closeButton.setText(bundle.getString("LogViewerFrame.closeButton.text")); // NOI18N
         closeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeButtonActionPerformed(evt);
             }
         });
 
         errorReportButton.setText(bundle.getString("LogViewerFrame.errorReportButton.text")); // NOI18N
         errorReportButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 errorReportButtonActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(logScrollPane)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(335, Short.MAX_VALUE)
                 .add(errorReportButton)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(configureButton)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(closeButton))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(logScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                 .add(4, 4, 4)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(closeButton)
                     .add(configureButton)
                     .add(errorReportButton)))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
         setVisible(false);
     }//GEN-LAST:event_closeButtonActionPerformed
 
     private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
         backlogTF.setText(String.valueOf(getMaxEntries()));
         
         // get the root logger level
         Logger root = LogManager.getLogManager().getLogger("");
         levelCB.setSelectedItem(root.getLevel());
 
         startupCB.setSelected(visibleOnStartup);
 
         // populate the table with any loggers with non-default levels
         LoggerTableModel levelModel = (LoggerTableModel) levelTable.getModel();
         levelModel.reload();
         
         configDialog.pack();
         configDialog.setVisible(true);
         configDialog.toFront();
     }//GEN-LAST:event_configureButtonActionPerformed
 
     private void configCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configCancelButtonActionPerformed
         configDialog.setVisible(false);
     }//GEN-LAST:event_configCancelButtonActionPerformed
 
     private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
         // set values for maximum entry count
         setMaxEntries(Integer.parseInt(backlogTF.getText()));
 
         // set the root logger level
         setRootLogLevel((Level) levelCB.getSelectedItem());
 
         // save the visible on startup preference
         setVisibleOnStartup(startupCB.isSelected());
 
         // set levels for any specified loggers
         LoggerTableModel levelModel = (LoggerTableModel) levelTable.getModel();
         levelModel.save();
 
         configDialog.setVisible(false);
     }//GEN-LAST:event_okButtonActionPerformed
 
     private void errorReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorReportButtonActionPerformed
         errorText.setText(generateErrorReport());
 
         // scroll to the top
         errorText.setCaretPosition(0);
 
         errorReportDialog.pack();
         errorReportDialog.setVisible(true);
         errorReportDialog.toFront();
     }//GEN-LAST:event_errorReportButtonActionPerformed
 
     private void errorCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorCloseButtonActionPerformed
         errorReportDialog.setVisible(false);
     }//GEN-LAST:event_errorCloseButtonActionPerformed
 
     private void errorSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorSelectButtonActionPerformed
         Position end = errorText.getDocument().getEndPosition();
 
         errorText.getCaret().setDot(0);
         errorText.getCaret().moveDot(end.getOffset() - 1);
         errorText.getCaret().setSelectionVisible(true);
     }//GEN-LAST:event_errorSelectButtonActionPerformed
 
     private void errorCopyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorCopyButtonActionPerformed
         StringSelection sel = new StringSelection(errorText.getSelectedText());
         getToolkit().getSystemClipboard().setContents(sel, sel);
 
         // clear the selection
         int end = errorText.getCaretPosition();
         errorText.getCaret().setDot(end - 1);
     }//GEN-LAST:event_errorCopyButtonActionPerformed
 
     private void levelTablePlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelTablePlusActionPerformed
         DefaultTableModel levelModel = (DefaultTableModel) levelTable.getModel();
         levelModel.addRow(new Object[] { "", null });
     }//GEN-LAST:event_levelTablePlusActionPerformed
 
     private void levelTableMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelTableMinusActionPerformed
         DefaultTableModel levelModel = (DefaultTableModel) levelTable.getModel();
         levelModel.removeRow(levelTable.getSelectedRow());
     }//GEN-LAST:event_levelTableMinusActionPerformed
 
     protected String generateErrorReport() {
         final StringBuilder out = new StringBuilder();
         out.append("Error report generated " +
                    DateFormat.getDateTimeInstance().format(new Date()) + "\n");
         out.append("\n");
 
         // java version, etc
         out.append("-------- System Information --------\n");
         out.append("Java version: " + System.getProperty("java.version") + "\n");
         out.append("Java vendor:" + System.getProperty("java.vendor") + "\n");
         out.append("OS:" + System.getProperty("os.name") + "\n");
         out.append("OS version: " + System.getProperty("os.version") + "\n");
         out.append("OS architecture: " + System.getProperty("os.arch") + "\n");
         out.append("\n");
         out.append("Max memory: " + Runtime.getRuntime().maxMemory() + "\n");
         out.append("Total memory: " + Runtime.getRuntime().totalMemory() + "\n");
         out.append("Free memory: " + Runtime.getRuntime().freeMemory() + "\n");
         out.append("\n");
 
         // graphics
         out.append("-------- Graphics Information --------\n");
 
         // grab display information from the renderer
         final Semaphore gs = new Semaphore(0);        
         SceneWorker.addWorker(new WorkCommit() {
             public void commit() {
                 try {
                     DisplaySystem ds = DisplaySystem.getDisplaySystem("JOGL");
                     out.append("Display adapter:  " + ds.getAdapter() + "\n");
                     out.append("Display vendor:   " + ds.getDisplayVendor() + "\n");
                     out.append("Driver version:   " + ds.getDriverVersion() + "\n");
                     out.append("Display renderer: " + ds.getDisplayRenderer() + "\n");
                     out.append("API Version:      " + ds.getDisplayAPIVersion() + "\n");
                     out.append("\n\n");
                 } finally {
                     gs.release();
                 } 
             }
         });
         
         // wait for the renderer to run the worker
         try {
             gs.acquire();
         } catch (InterruptedException ie) {
             // ignore
         }
         
         RenderManager rm = WorldManager.getDefaultWorldManager().getRenderManager();
         JOGLContextCapabilities cap = rm.getContextCaps();
         out.append("GL_ARB_fragment_program..." + cap.GL_ARB_fragment_program + "\n");
         out.append("GL_ARB_fragment_shader..." + cap.GL_ARB_fragment_shader + "\n");
         out.append("GL_ARB_shader_objects..." + cap.GL_ARB_shader_objects + "\n");
         out.append("GL_ARB_texture_non_power_of_two..." + cap.GL_ARB_texture_non_power_of_two + "\n");
         out.append("GL_ARB_vertex_buffer_object..." + cap.GL_ARB_vertex_buffer_object + "\n");
         out.append("GL_ARB_vertex_program..." + cap.GL_ARB_vertex_program + "\n");
         out.append("GL_ARB_vertex_shader..." + cap.GL_ARB_vertex_shader + "\n");
         out.append("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB + "\n");
         out.append("GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB..." + cap.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB + "\n");
         out.append("GL_MAX_TEXTURE_COORDS_ARB..." + cap.GL_MAX_TEXTURE_COORDS_ARB + "\n");
         out.append("GL_MAX_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_TEXTURE_IMAGE_UNITS_ARB + "\n");
         out.append("GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT..." + cap.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT + "\n");
         out.append("GL_MAX_TEXTURE_UNITS..." + cap.GL_MAX_TEXTURE_UNITS + "\n");
         out.append("GL_MAX_VARYING_FLOATS_ARB..." + cap.GL_MAX_VARYING_FLOATS_ARB + "\n");
         out.append("GL_MAX_VERTEX_ATTRIBS_ARB..." + cap.GL_MAX_VERTEX_ATTRIBS_ARB + "\n");
         out.append("GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB..." + cap.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB + "\n");
         out.append("GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB..." + cap.GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB + "\n");
         out.append("GL_SGIS_generate_mipmap..." + cap.GL_SGIS_generate_mipmap + "\n");
         out.append("GL_SHADING_LANGUAGE_VERSION_ARB..." + cap.GL_SHADING_LANGUAGE_VERSION_ARB + "\n");
         out.append("GL_VERSION_1_2..." + cap.GL_VERSION_1_2 + "\n");
         out.append("GL_VERSION_2_0..." + cap.GL_VERSION_2_0 + "\n");
         out.append("GL_VERSION_2_1..." + cap.GL_VERSION_2_1 + "\n");
         out.append("GL_VERSION_3_0..." + cap.GL_VERSION_3_0 + "\n");
         out.append("\n");
 
         // error log
         out.append("-------- Error Log --------\n");
         for (LogEntry entry : entries) {
             format(entry.record, out);
         }
         out.append("\n");
 
         // thread dump
         out.append("-------- Threads --------\n");
         for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
             out.append(e.getKey().getName() + " " + e.getKey().getState() + "\n");
             for (StackTraceElement ste : e.getValue()) {
                 out.append("    " + ste.getClassName());
                 out.append("." + ste.getMethodName());
                 if (ste.isNativeMethod()) {
                     out.append("(native)");
                 } else {
                     out.append("(" + ste.getFileName() + ":");
                     out.append(ste.getLineNumber() + ")");
                 }
                 out.append("\n");
 
             }
             out.append("\n");
 
         }
         out.append("\n");
 
         return out.toString();
     }
 
     private class LogEntry {
         LogRecord record;
         int length;
     }
 
     private class LoggerTableModel extends DefaultTableModel {
 
         private Map<String, Level> origLevels;
         private final List<Logger> createdLoggers;
 
         public LoggerTableModel() {
             super(new Object[]{
                         BUNDLE.getString("Logger"),
                         BUNDLE.getString("Level")}, 0);
 
             createdLoggers = new ArrayList<Logger>();
         }
 
         public void reload() {
             // clear the model
             setRowCount(0);
 
             origLevels = new TreeMap<String, Level>();
 
             // collect the logger names and levels, and sort them by their name
             LogManager logManager = LogManager.getLogManager();
             Enumeration<String> loggerNames = logManager.getLoggerNames();
             while (loggerNames.hasMoreElements()) {
                 String loggerName = loggerNames.nextElement();
                 if (loggerName.length() == 0) {
                     // skip loggers with empty names
                     continue;
                 }
 
                 // OWF issue #129: make sure the logger is not null
                 // by using Logger.getLogger(), which will create a logger
                 // if necessary
                 Level level = Logger.getLogger(loggerName).getLevel();
                 if (level == null) {
                     // skip loggers with undefined levels
                     continue;
                 }
                 origLevels.put(loggerName, level);
             }
 
             // populate the model
             for (Map.Entry<String, Level> e : origLevels.entrySet()) {
                 addRow(new Object[]{e.getKey(), e.getValue()});
             }
         }
 
         public void save() {
             Preferences prefs = Preferences.userNodeForPackage(LogViewerFrame.class);
             prefs = prefs.node("loggerTable");
 
             try {
                 prefs.clear();
             } catch (BackingStoreException ex) {
                 logger.log(Level.WARNING, "Error clearing preferences", ex);
             }
 
             for (int i = 0; i < getRowCount(); i++) {
                 String loggerName = (String) getValueAt(i, 0);
                 Level level = (Level) getValueAt(i, 1);
 
                 if (loggerName != null && level != null) {
                     Logger.getLogger(loggerName).setLevel(level);
                     prefs.put(loggerName, level.getName());
                     origLevels.remove(loggerName);
                 }
             }
 
             // any values left in origLevels need to be reset to the
             // default
             for (String loggerName : origLevels.keySet()) {
                 Logger removeLogger = Logger.getLogger(loggerName);
                 removeLogger.setLevel(null);
 
                 // remove the logger from our list of saved loggers
                 createdLoggers.remove(removeLogger);
             }
         }
 
         public void restore() {
             Preferences prefs = Preferences.userNodeForPackage(LoggerTableModel.class);
             prefs = prefs.node("loggerTable");
 
             try {
                 for (String loggerName : prefs.keys()) {
                     Level level = Level.parse(prefs.get(loggerName, "WARNING"));
                     Logger createLogger = Logger.getLogger(loggerName);
                     createLogger.setLevel(level);
 
                     // OWF issue #130: hold a strong reference to the created
                     // logger, to make sure it isn't garbage collected
                     createdLoggers.add(createLogger);
                 }
             } catch (BackingStoreException ex) {
                 logger.log(Level.WARNING, "Error restoringing preferences", ex);
             }
         }
     }
 
     private static class SingletonHolder {
         private static LogViewerFrame INSTANCE = new LogViewerFrame();
     }
 
     private class NoWrapEditorKit extends StyledEditorKit {
         @Override
         public ViewFactory getViewFactory() {
             final ViewFactory sf = super.getViewFactory();
             return new ViewFactory() {
                 public View create(Element element) {
                     if (element.getName().equals(AbstractDocument.ParagraphElementName)) {
                         return new ParagraphView(element) {
                             @Override
                             public void layout(int width, int height) {
                                try {
                                    super.layout(Short.MAX_VALUE, height);
                                } catch (Throwable t) {
                                    // this method sometimes throws an error
                                    // which prevents the component from
                                    // initializing. Just ignore any errors.
                                    logger.log(Level.WARNING, "Error in layout", 
                                               t);
                                }
                             }
 
                             @Override
                             public float getMinimumSpan(int axis) {
                                 return super.getPreferredSpan(axis);
                             }
                         };
                     }
 
                     return sf.create(element);
                 }
             };
         }
     }
 
     /**
      * JEditorPane implementation that scrolls if it is at the bottom, but
      * otherwise stays in the same relative position.
      */
     private class ManualScrollEditorPane extends JEditorPane {
         public ManualScrollEditorPane() {
             addMouseMotionListener(new MouseMotionAdapter() {
                 @Override
                 public void mouseDragged(MouseEvent e) {
                     Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                     forceScroll(r);
                 }
             });
         }
 
         @Override
         public void scrollRectToVisible(Rectangle r) {
             // ignore any scroll requests from the system
             return;
         }
 
         /**
          * Call before making any changes to the content of the panel to
          * return the currently visible position.
          * @return the upper left hand corner of the currently visible
          * portion of the document
          */
         public Position preModify() {
             // get the currently visible portion of the screen
             Rectangle visible = getVisibleRect();
 
             // find the position in the document corresponding to the upper
             // left hand corner of the visible area
             Point upperLeft = new Point((int) visible.getMinX(),
                                         (int) visible.getMinY());
 
             try {
                 return getDocument().createPosition(viewToModel(upperLeft));
             } catch (BadLocationException ble) {
                 logger.log(Level.WARNING, "Bad loction", ble);
                 return null;
             }
         }
 
         /**
          * Return whether or not the end of the document is in view.
          * @return true if the end of the document is in view, or false
          * if not.
          */
         public boolean atEnd() {
             // determine if we are at the end by creating a rectangle containing
             // the last line, and figuring out if it is visible
             try {
                 int endOffset = getDocument().getEndPosition().getOffset() - 1;
                 Rectangle lastLine = modelToView(endOffset);
 
                 // extend the last line to be the whole width of the panel
                 // to guarantee it will intersect
                 lastLine = new Rectangle(0, (int) lastLine.getMinY(),
                                          getWidth(), (int) lastLine.getHeight());
 
                 return getVisibleRect().intersects(lastLine);
             } catch (BadLocationException ble) {
                 logger.log(Level.WARNING, "Bad location", ble);
                 return false;
             }
         }
 
         /**
          * Call after the changes to the panel have been made.
          * @param pos the position that was returned from preModify()
          * @param atEnd true if we were previously at the end of the document
          */
         public void postModify(Position pos, boolean atEnd) {
             int offset;
 
             if (atEnd) {
                 // if we were previously at the end, stay there
                 offset = getDocument().getEndPosition().getOffset() - 1;
             } else {
                 offset = pos.getOffset();
             }
 
             try {
                 super.scrollRectToVisible(modelToView(offset));
             } catch (BadLocationException ble) {
                 logger.log(Level.WARNING, "Bad location", ble);
             }
         }
 
         /**
          * Force the scroll to the end
          */
         public void scrollToEnd() {
             int offset = getDocument().getEndPosition().getOffset() - 1;
             try {
                 super.scrollRectToVisible(modelToView(offset));
             } catch (BadLocationException ble) {
                 logger.log(Level.WARNING, "Bad location", ble);
             }
         }
 
         private void forceScroll(Rectangle r) {
             super.scrollRectToVisible(r);
         }
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextField backlogTF;
     private javax.swing.JButton closeButton;
     private javax.swing.JButton configCancelButton;
     private javax.swing.JDialog configDialog;
     private javax.swing.JButton configureButton;
     private javax.swing.JButton errorCloseButton;
     private javax.swing.JButton errorCopyButton;
     private javax.swing.JButton errorReportButton;
     private javax.swing.JDialog errorReportDialog;
     private javax.swing.JScrollPane errorScrollPane;
     private javax.swing.JButton errorSelectButton;
     private javax.swing.JTextArea errorText;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JComboBox levelCB;
     private javax.swing.JTable levelTable;
     private javax.swing.JButton levelTableMinus;
     private javax.swing.JButton levelTablePlus;
     private javax.swing.JEditorPane logPane;
     private javax.swing.JScrollPane logScrollPane;
     private javax.swing.JButton okButton;
     private javax.swing.JCheckBox startupCB;
     // End of variables declaration//GEN-END:variables
 
 }
