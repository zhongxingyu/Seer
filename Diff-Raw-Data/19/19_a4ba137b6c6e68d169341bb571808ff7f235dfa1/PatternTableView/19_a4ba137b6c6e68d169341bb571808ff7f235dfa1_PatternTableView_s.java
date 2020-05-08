 package org.geworkbench.util.sequences;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 
 import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
 import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
 import org.geworkbench.util.patterns.PatternOperations;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: This view is used to display patterns in a table format.</p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @author not attributable
  * @version $Id$
  */
 
 public class PatternTableView extends JPanel {
 	private static final long serialVersionUID = 1451645981046343519L;
 	
 	//For property changes
     static final public String ROWSELECTION = "rowselection";
 
     private PatternTableModel model = null;
     
     BorderLayout borderLayout1 = new BorderLayout();
     private JScrollPane jScrollPane = new JScrollPane();
     //popup menu
     private JPopupMenu patternMenu = new JPopupMenu();
     private JMenuItem maskPatternItem = new JMenuItem();
     private JMenuItem unmaskAllPatternItem = new JMenuItem();
     private JMenuItem jSavePatternsItem = new JMenuItem();
     private JMenuItem jSavePatternsWInfoItem = new JMenuItem();
     private JMenuItem jSaveSelectedPatternsWInfoItem = new JMenuItem();
     private boolean saveAllPatterns;
 
     private static class JPTable extends JTable {
 		private static final long serialVersionUID = -8375106946681259570L;
 
 		protected void paintComponent(Graphics g) {
             setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             super.paintComponent(g);
             setCursor(Cursor.getDefaultCursor());
         }
     }
 
 
     private static class RegxFilter extends javax.swing.filechooser.FileFilter {
 
         public String getDescription() {
             return "Regex patterns only (*.regx)";
         }
 
         public boolean accept(File f) {
             if (f != null && (f.isDirectory() || f.getName().endsWith(".regx"))) {
                 return true;
             }
             return false;
         }
     }
 
 
     private JPTable patternTable = new JPTable();
 
 	private DSSequenceSet<? extends DSSequence> sequenceSet;
 
     public PatternTableView(PatternTableModel model, DSSequenceSet<? extends DSSequence> sequenceSet) {
         this.model = model;
         this.sequenceSet = sequenceSet;
         try {
             jbInit();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void jbInit() throws Exception {
         this.setLayout(borderLayout1);
         patternTable.setBackground(Color.pink);
         patternTable.setEnabled(true);
         patternTable.setModel(model);
         add(jScrollPane, BorderLayout.CENTER);
         jScrollPane.getViewport().add(patternTable, null);
         addMouseListener();
         initPopupMenuItem();
         addMouseListenerToHeaderInTable();
     }
 
     private void addPopupMenuItem() {
         patternMenu.add(maskPatternItem);
         patternMenu.add(unmaskAllPatternItem);
         patternMenu.add(jSavePatternsItem);
         patternMenu.addSeparator();
         patternMenu.add(jSaveSelectedPatternsWInfoItem);
         patternMenu.addSeparator();
         patternMenu.add(jSavePatternsWInfoItem);
     }
 
     private void initPopupMenuItem() {
 
         maskPatternItem.setText("Mask Pattern");
         maskPatternItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 maskPatternItem_actionPerformed();
             }
         });
 
         unmaskAllPatternItem.setText("Unmask all Patterns");
         unmaskAllPatternItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 unmaskAllPatternItem_actionPerformed();
             }
         });
 
         jSavePatternsItem.setText("Save Patterns (Regex Only)");
         jSavePatternsItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 savePatternsItem_actionPerformed(e);
             }
         });
 
         jSaveSelectedPatternsWInfoItem.setText("Save Selected Patterns");
         jSaveSelectedPatternsWInfoItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 saveAllPatterns = false;
                 savePatternAndInfoItem_actionPerformed(e);
             }
         });
 
         jSavePatternsWInfoItem.setText("Save All Patterns");
         jSavePatternsWInfoItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 saveAllPatterns = true;
                 savePatternAndInfoItem_actionPerformed(e);
             }
         });
 
         addPopupMenuItem();
     }
 
     static private List<DSMatchedPattern<DSSequence, CSSeqRegistration>> getSelectedPatterns(JPTable patternTable) {
 		List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patternMatches = new ArrayList<DSMatchedPattern<DSSequence, CSSeqRegistration>>();
 		if (patternTable != null) {
 			PatternTableModel model = (PatternTableModel) (patternTable)
 					.getModel();
 			int[] rows = patternTable.getSelectedRows();
 			for (int i = 0; i < rows.length; i++) {
 				DSMatchedPattern<DSSequence, CSSeqRegistration> pattern = model
 						.getPattern(rows[i]);
 				patternMatches.add(pattern);
 			}
 		}
     	return patternMatches;
     }
     
     private void addMouseListener() {
         patternTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
             	List<DSMatchedPattern<DSSequence, CSSeqRegistration>> selected = 
             			getSelectedPatterns(patternTable);
                 firePropertyChange(ROWSELECTION, null, selected);
             }
         });
 
         patternTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(MouseEvent e) {
                if (e.isMetaDown()) {
                     patternMenu.show(patternTable, e.getX(), e.getY());
                 }
             }
         });
     }
 
     public void savePatternsItem_actionPerformed(ActionEvent e) {
         int[] rows = patternTable.getSelectedRows();
 
         if (rows.length == 0) {
             showErrorMessage("No patterns selected for saving");
             return;
         }
 
         JFileChooser chooser = new JFileChooser(org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().getDefPath());
         //  PatternFileFormat format = new PatternFileFormat();
         //  FileFilter filter = format.getFileFilter();
         RegxFilter filter = new RegxFilter(); // .regx extension recommended for
         // save pattern
         chooser.setFileFilter(filter);
         chooser.setFileFilter(filter);
         int returnVal = chooser.showSaveDialog(this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             try {
                 File selected = chooser.getSelectedFile().getAbsoluteFile();
                 String filename = selected.getName();
 
                 if (!filename.endsWith(".regx")) {
                     selected = new File(selected.getAbsolutePath() + ".regx");
                 }
 
                 if (selected.exists()) {
 					int o = JOptionPane.showConfirmDialog(null,
 					"The file already exists. Do you wish to overwrite it?", 
 					"Replace the existing file?",
 					JOptionPane.YES_NO_OPTION);
 					if (o != JOptionPane.YES_OPTION) {
 						return;
 					}
 				}
                 
                 BufferedWriter writer = new BufferedWriter(new FileWriter(selected));
                 for (int i = 0; i < rows.length; i++) {
                     org.geworkbench.util.patterns.CSMatchedSeqPattern pattern = (org.geworkbench.util.patterns.CSMatchedSeqPattern) model.getPattern(i);
                     writer.write("[" + i + "]\t");
                     writer.write(pattern.getASCII());
                     writer.newLine();
                 }
                 writer.flush();
                 writer.close();
             } catch (IOException ex) {
                 showErrorMessage("While saving the pattens an exception occured (IOException)." + "Patterns may not have been saved.");
                 ex.printStackTrace();
             }
         }
     }
 
     /**
      * Loads patterns from a file into the view. The patterns are parsed with
      * the corresponding database file.
      *
      * @param e
      */
 
 
     private void savePatternAndInfoItem_actionPerformed(ActionEvent e) {
         if (!saveAllPatterns && patternTable.getSelectedRowCount() == 0) {
             showErrorMessage("No patterns selected for saving");
             return;
         }
 
         int answer = JOptionPane.showConfirmDialog(null, "The saving operation may take a while downloading the patterns.\n" + "While the saving is in progress please do not run other discoveries\n" + "on this file.\n" + "Continue with the operation?", "Save Patterns", JOptionPane.YES_NO_OPTION);
         if (answer == JOptionPane.NO_OPTION) {
             return;
         }
 
         final JFileChooser chooser = new JFileChooser(org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().getDefPath());
         PatFilter filter = new PatFilter();
         chooser.setFileFilter(filter);
         final int returnVal = chooser.showSaveDialog(this);
         Runnable patternSaver = new Runnable() {
             public void run() {
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     //disable menu item and save
                     jSavePatternsWInfoItem.setEnabled(false);
                     updateFileProperty(chooser.getSelectedFile().getAbsolutePath());
                     PatternResult patternDB = (saveAllPatterns) ? getPatternDB() : getPatternDB(patternTable.getSelectedRows());
 
                     saveAllPatterns = false;
 
                     if (patternDB == null) {
                         return;
                     }
                     File file = chooser.getSelectedFile();
                     if (!file.getName().endsWith(".pat")) {
                         file = new File(file.getAbsolutePath() + ".pat");
                     }
 
                     if (file.exists()) {
     					int o = JOptionPane.showConfirmDialog(null,
     					"The file already exists. Do you wish to overwrite it?", 
     					"Replace existing file?",
     					JOptionPane.YES_NO_OPTION);
     					if (o != JOptionPane.YES_OPTION) {
     						return;
     					}
     				}
                     
                     patternDB.write(file);
                     jSavePatternsWInfoItem.setEnabled(true);
                     JOptionPane.showMessageDialog(null, "Save operation completed.", "Save Patterns", JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         };
         Thread t = new Thread(patternSaver);
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
     }
 
     private PatternResult getPatternDB() {
         DSSequenceSet<? extends DSSequence> db = sequenceSet;
         PatternResult patternDB = new PatternResult(db.getFile(), null);
 
         for (int i = 0; i < model.getRowCount(); i++) {
             DSMatchedSeqPattern pattern = model.getPattern(i);
             PatternOperations.fill(pattern, db);
             patternDB.add(pattern);
         }
         return patternDB;
     }
 
     private PatternResult getPatternDB(int[] rows) {
         DSSequenceSet<? extends DSSequence> db = sequenceSet;
         PatternResult patternDB = new PatternResult(db.getFile(), null);
 
         for (int i = 0; i < rows.length; i++) {
             DSMatchedSeqPattern pattern = model.getPattern(rows[i]);
             PatternOperations.fill(pattern, db);
             patternDB.add(pattern);
         }
 
         return patternDB;
     }
     
     /**
      * Pops an Error Panel.
      *
      * @param message
      */
     private void showErrorMessage(String message) {
         JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
 
     }
 
     private void updateFileProperty(String path) {
         //update the property file
         org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().setDefPath(path);
     }
 
     private void addMouseListenerToHeaderInTable() {
         MouseAdapter listMouseListener = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                if (e.isMetaDown()) {
                     patternMenu.show(patternTable, e.getX(), e.getY());
                 } else {
                     TableColumnModel columnModel = patternTable.getColumnModel();
                     int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                     int column = patternTable.convertColumnIndexToModel(viewColumn);
                     if (e.getClickCount() == 1 && column != -1) {
                         if (column == PatternTableModel.PTMSeqNo) {
                             sort(SEQNO);
                         } else if (column == PatternTableModel.PTMSupport) {
                             sort(IDNO);
                         } else if (column == PatternTableModel.PTMTokNo) {
                             sort(TOKNO);
                         } else if (column == PatternTableModel.PTMZScore) {
                             sort(PVALUE);
                         }
                     }
                 }
             }
         };
         JTableHeader th = patternTable.getTableHeader();
         th.addMouseListener(listMouseListener);
     }
 
     private final static int IDNO = 1;
     private final static int SEQNO = 2;
     private final static int TOKNO = 3;
     private final static int PVALUE = 4;
 
     private void sort(int field) {
         model.sort(field);
     }
 
     private void maskPatternItem_actionPerformed() {
         int[] selectedRow = patternTable.getSelectedRows();
         model.mask(selectedRow, true);
     }
 
     private void unmaskAllPatternItem_actionPerformed() {
         model.mask(null, true);
     }
 }
