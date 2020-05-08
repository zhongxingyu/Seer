 package org.mongkie.ui.enrichment.go;
 
 import org.mongkie.enrichment.go.EnrichedGoId;
 import javax.swing.event.ChangeEvent;
 import javax.swing.table.TableModel;
 import java.awt.BorderLayout;
 import java.io.File;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.JXTreeTable;
 import org.jdesktop.swingx.treetable.TreeTableModel;
 import org.mongkie.enrichment.EnrichmentResultUIProvider;
 import org.mongkie.enrichment.spi.EnrichedResultUI;
 import org.mongkie.enrichment.spi.EnrichedTerm;
 import org.mongkie.enrichment.spi.EnrichmentResultUISupport;
 import org.mongkie.gobean.EnrichedResult;
 import org.mongkie.ui.enrichment.go.util.UIUtilities;
 import org.mongkie.ui.enrichment.go.util.TableCellDoubleRenderer;
 import org.mongkie.ui.enrichment.go.util.TableUtilities;
 import org.openide.util.Lookup;
 
 public class EnrichedResultView extends JPanel implements EnrichedResultUI, EnrichmentResultUISupport<EnrichedGoId> {
 
     private EnrichedResult analysisResult;
     private JTabbedPane tabbedPane;
     private JXTable tableView;
     private JXTreeTable treeTableView;
     private static final int MAX_TERMS_FOR_TREE_EXPANSION = 10;
     //
     private static final String TABLE_VIEW = "Table view";
     private static final String TREETABLE_VIEW = "TreeTable view";
     private ReportPanel reportPanel;
     private InformationPanel informationPanel;
 
     public EnrichedResultView(EnrichedResult result) {
         this.analysisResult = result;
         setViewPanels();
         setBorder(null);
         reportPanel = new ReportPanel(result);
         informationPanel = new InformationPanel();
     }
 
     private void setViewPanels() {
         EnrichedTreeTableViewModel model = new EnrichedTreeTableViewModel(analysisResult, analysisResult.getSelectedGoIds());
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
         tableView = getTableView(model);
         treeTableView = getTreeTableView(model);
         addToTabbedPane(tabbedPane, TABLE_VIEW, tableView);
         addToTabbedPane(tabbedPane, TREETABLE_VIEW, treeTableView);
         if (analysisResult.getSelectedGoIds().size() <= MAX_TERMS_FOR_TREE_EXPANSION) {
             treeTableView.expandAll();
         }
         tabbedPane.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(ChangeEvent e) {
                 Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().setLookupContents(getSelectedTerms());
             }
         });
         this.setLayout(new BorderLayout());
         this.add(tabbedPane);
     }
 
     private JXTable getTableView(TableModel model) {
         JXTable table = new JXTable(model);
         table.setColumnControlVisible(true);
         //table.setHighlighters(HighlighterFactory.createAlternateStriping(2));
         table.setHighlighters(new HeatHighlighter(EnrichedTreeTableViewModel.ADJUSTED_P_COLUMN, analysisResult));
         TableSelectionListener listener = new TableSelectionListener(table);
         table.getSelectionModel().addListSelectionListener(listener);
         table.setComponentPopupMenu(null);
 //        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
         table.setDefaultRenderer(Double.class, new TableCellDoubleRenderer());
         table.setInheritsPopupMenu(true);
 //        JPopupMenu popupmenu = new JPopupMenu();
 //        popupmenu.add(new ExportTableAction());
 //        table.setComponentPopupMenu(popupmenu);
         return table;
     }
 
     private JXTreeTable getTreeTableView(TreeTableModel model) {
         JXTreeTable treeTable = new JXTreeTable(model);
         treeTable.setRootVisible(false);
         treeTable.setColumnControlVisible(true);
         treeTable.setHighlighters(new HeatHighlighter(EnrichedTreeTableViewModel.ADJUSTED_P_COLUMN, analysisResult));
         TreeTableSelectionListener listener = new TreeTableSelectionListener(treeTable);
         treeTable.addTreeSelectionListener(listener);
         treeTable.setDefaultRenderer(Double.class, new TableCellDoubleRenderer());
         treeTable.setInheritsPopupMenu(true);
 //        JPopupMenu popupmenu = new JPopupMenu();
 //        popupmenu.add(new ExportTreeTableAction());
 //        treeTable.setComponentPopupMenu(popupmenu);
         return treeTable;
     }
 
     private void addToTabbedPane(JTabbedPane tabbedPane, String title, JComponent component) {
         JScrollPane scrollPane = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
         scrollPane.setInheritsPopupMenu(true);
 //        JPanel panel = new JPanel(new BorderLayout());
 //        panel.add(scrollPane, BorderLayout.CENTER);
 //        tabbedPane.add(title, panel);
         tabbedPane.add(title, scrollPane);
     }
 
     public void exportTable() {
         File saveFile = UIUtilities.getSaveFile("go result files", "Export Table as ...", "tsv", "Tab Separated values (.tsv)");
         if (saveFile == null) {
             return;
         }
         UIUtilities.writeToFile(saveFile, TableUtilities.getCopyContents(tableView));
     }
 
     public void exportTreeTable() {
         File saveFile = UIUtilities.getSaveFile("go result files", "Export TreeTable as ...", "tsv", "Tab Separated values (.tsv)");
         if (saveFile == null) {
             return;
         }
         UIUtilities.writeToFile(saveFile, TableUtilities.getCopyContents(treeTableView));
     }
 
     private JXTable getSelectedTable() {
         return (JXTable) ((JScrollPane) tabbedPane.getSelectedComponent()).getViewport().getView();
     }
 
     @Override
     public EnrichedTerm[] getSelectedTerms() {
         JXTable table = getSelectedTable();
         int[] selectedRows = table.getSelectedRows();
         EnrichedGoId[] terms = new EnrichedGoId[selectedRows.length];
         for (int i = 0; i < selectedRows.length; i++) {
             terms[i] = (EnrichedGoId) table.getModel().getValueAt(table.convertRowIndexToModel(selectedRows[i]), 0);
         }
         return terms;
     }
 
     @Override
     public EnrichedTerm[] getAllTerms() {
         JXTable table = getSelectedTable();
         EnrichedGoId[] terms = new EnrichedGoId[table.getRowCount()];
         for (int i = 0; i < terms.length; i++) {
             terms[i] = (EnrichedGoId) table.getModel().getValueAt(table.convertRowIndexToModel(i), 0);
         }
         return terms;
     }
 
     @Override
     public void selectAll() {
         JXTable table = getSelectedTable();
         table.setRowSelectionInterval(0, table.getRowCount() - 1);
     }
 
     @Override
     public void selectNone() {
         getSelectedTable().clearSelection();
     }
 
     @Override
     public JPanel getReportPanel() {
         return reportPanel;
     }
 
     @Override
     public JPanel getInformationPanel(EnrichedGoId term) {
         informationPanel.setTerm(term);
         return informationPanel;
     }
 
     @Override
     public JPanel getPanel() {
         return this;
     }
 
     @Override
     public void destroy() {
         removeAll();
     }
 
     private class TableSelectionListener implements ListSelectionListener {
 
         private JXTable table;
 
         TableSelectionListener(JXTable table) {
             this.table = table;
         }
 
         @Override
         public void valueChanged(ListSelectionEvent e) {
             if (e.getValueIsAdjusting()) {
                 return;
             }
             int[] selectedRows = table.getSelectedRows();
             EnrichedGoId[] terms = new EnrichedGoId[selectedRows.length];
             for (int i = 0; i < selectedRows.length; i++) {
                 terms[i] = (EnrichedGoId) table.getModel().getValueAt(table.convertRowIndexToModel(selectedRows[i]), 0);
             }
             Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().setLookupContents(terms);
         }
     }
 
     private class TreeTableSelectionListener implements TreeSelectionListener {
 
         private JXTreeTable treetable;
 
         TreeTableSelectionListener(JXTreeTable treetable) {
             this.treetable = treetable;
         }
 
         @Override
         public void valueChanged(TreeSelectionEvent e) {
             int[] selectedRows = treetable.getSelectedRows();
             EnrichedGoId[] terms = new EnrichedGoId[selectedRows.length];
             for (int i = 0; i < selectedRows.length; i++) {
                 terms[i] = (EnrichedGoId) treetable.getModel().getValueAt(treetable.convertRowIndexToModel(selectedRows[i]), 0);
             }
             Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().setLookupContents(terms);
         }
     }
 }
