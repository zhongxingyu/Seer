 /*
  * ViewDataConsole.java
  *
  * Created on October 4, 2007, 3:19 PM
  */
 package edu.wustl.cab2b.client.ui.controls.sheet;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JViewport;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.RowSorterEvent;
 import javax.swing.event.RowSorterListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 /**
  *
  * @author  jasbir_sachdeva
  */
 class ViewDataConsole extends javax.swing.JPanel implements PropertyChangeListener {
 
     /** The Tables data Model, that pickes data from both Read-Only and Extensible Data Models. */
     private CompositeTableModel compositeDataModel;
 
     //    /**  */
     //    private Map<TableColumn, ColumnFilterModel> colExtraStateMap;
     /** Collection of Hidden and Visible Table COlumns.
      * Columns that are deleted are kept in removedTCols. */
     private Set<TableColumn> activeTableCols = new HashSet<TableColumn>(200);
 
     /** References to Removed Column are kept here (in sets), for undo.*/
     private List<List<TableColumn>> removedTCols = new ArrayList<List<TableColumn>>();
 
     /** Each new Column is given name as: "New Col-1", "New Col-2", and so on. */
     private int columnAppendCount = 0;
 
     /***/
     public static final String EXT_COLUMNS_NAME_PREFIX = "N.Col-";
 
     /** To be used by Table while laying out Columns...*/
     int defaultColWidth = Toolkit.getDefaultToolkit().getScreenSize().width / 20;
 
     /** Events name, fired when some button is pressed. */
     public static final String BUTTON_SELECT_ALL = "BUTTON_SELECT_ALL";
 
     public static final String BUTTON_SELECTION_RESET = "BUTTON_SELECTION_RESET";
 
     TableRowSorter tblRowSorter = new TableRowSorter();
 
     /**     Keeps references to Anyone who wishes to listens to column Selections. */
     ArrayList<ListSelectionListener> colSelectionListeners = new ArrayList<ListSelectionListener>();
 
     /***/
     ArrayList<TableModelListener> tblModelListener = new ArrayList<TableModelListener>();
 
     ArrayList<RowSorterListener> tblRowSorterListener = new ArrayList<RowSorterListener>();
 
     /**     This action listener is informed if user wants to export selection  .*/
     private ActionListener actLsnExportCells;
 
     private ActionListener actLsnShowPropDialogs;
 
     //    private JComponent tblFixedHeader;
     //  Actions...
     ShowDetailsAction actShowDetails = new ShowDetailsAction();
 
     AddUserColAction actAddCol = new AddUserColAction();
 
     CutAction actCut = new CutAction();
 
     CopyAction actCopy = new CopyAction();
 
     ResetAllAction actResetAll = new ResetAllAction();
 
     SelectAllAction actSelectAll = new SelectAllAction();
 
     RemoveAllSelectedExtColumnsAction actRemoveCols = new RemoveAllSelectedExtColumnsAction();
 
     UndeleteColumnsAction actUndoColDel = new UndeleteColumnsAction();
 
     PasteFromClipAction actPaste = new PasteFromClipAction();
 
     ExportSelectionAction actExportSelecion = new ExportSelectionAction();
 
     ShowCustomizationConsoleAction actShowProps = new ShowCustomizationConsoleAction();
 
     ClearSelectionAction actClearSelection = new ClearSelectionAction();
 
     /** Table Filter instance associated to data-table   */
     private RowFilter rfilterTbl;
 
     private TableRowSorter rsortTable;
 
     private boolean isCutInProgress = false;
 
     private boolean isSelectionFromRowHeader = false;
 
     /*List of addtional Toolbar actions...*/
     List<JButton> butToolbarAdditionalAL = new ArrayList<JButton>();
 
     //
     //
     //    TableModel tmROData;
     /** Creates new form ViewDataConsole */
     ViewDataConsole() {
         initComponents();
         addTableColSelectionListener(new ColSelectionsListener());
         addTableRowSelectionListener(new RowSelectionsListener());
 
         setupAcions();
         setupRowHeader();
         //        setupToolBar();
         Common.setButtonsLooks(this);
     }
 
     public void setupToolBarFromContextMenu() {
         //  Make Tool bar replica of context menu...
         for (int idx = 0; idx < popTblContextMnu.getComponentCount(); idx++) {
             Component comp = popTblContextMnu.getComponent(idx);
             if (comp instanceof JSeparator) {
                 tbarMain.addSeparator(new Dimension(11, 0));
             } else if (comp instanceof JMenuItem) {
                 JMenuItem mnuItem = (JMenuItem) comp;
                 Action act = mnuItem.getAction();
                 if (null != act) {
                     tbarMain.add(act);
                 }
             }
         }
     }
 
     Object getValueAt(int row, int column) {
         return tblData.getValueAt(row, column);
     }
 
     void setupRowHeader() {
         RowHeaderTableModel tmRowHeader = new RowHeaderTableModel();
         tblRowHeader.setModel(tmRowHeader);
         tblRowHeader.getColumnModel().getColumn(0).setMaxWidth(20);
         int totalWidth = tblRowHeader.getColumnModel().getColumnMargin() * tblRowHeader.getColumnCount()
                 + tblRowHeader.getColumnModel().getTotalColumnWidth();
         tblRowHeader.setPreferredScrollableViewportSize(new Dimension(totalWidth, 0));
 
         //  explicity instaling viewport for JTable..
         JViewport vpRowHeader = new JViewport();
         vpRowHeader.setView(tblRowHeader);
         spaData.setRowHeader(vpRowHeader);
 
         //Installing  Column Width Manager...
 
         //  Setup scrollpane corners...
         JTableHeader headTable = tblRowHeader.getTableHeader();
         spaData.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, headTable);
         spaData.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, lblReset);
         spaData.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, lblSelectAll);
         headTable.addMouseListener(new RowHeaderHeadEventHandler());
 
         // Connect row selection models...
         tblRowHeader.setSelectionModel(tblData.getSelectionModel());
         spaData.getRowHeader().addChangeListener(new HeaderAndDataScrolSync());
         //        tblRowHeader.
     }
 
     void addUserColumn(SheetColumn sCol) {
         //  Provide provision in underlying model to accomodate for new column...
         compositeDataModel.addNewColumn(sCol);
         //  actually inform table to render new column now...
         tblData.addColumn(sCol);
     }
 
     void extendSelectionsToAllColumns() {
         int endCol = tblData.getColumnModel().getColumnCount();
         tblData.getColumnModel().getSelectionModel().setSelectionInterval(0, endCol - 1);
     }
 
     void setTableFilter(RowFilter tableFilter) {
         rfilterTbl = tableFilter;
     }
 
     void reapplyFilter() {
         //  Reapply row sorter, automatically filter will be reapplied...
 
         rsortTable.allRowsChanged();
         tblRowHeader.revalidate();
         tblRowHeader.repaint();
     }
 
     private void setupAcions() {
         //        butRemoveCol.setAction(actRemoveCols);
         //        miRemoveCols.setAction(actRemoveCols);
         //
         //        butUndoColDelete.setAction(actUndoColDel);
         //        miUndoRemoveCol.setAction(actUndoColDel);
 
         miShowDetails.setAction(actShowDetails);
         miAddCol.setAction(actAddCol);
         miCut.setAction(actCut);
         miCopy.setAction(actCopy);
         miReset.setAction(actResetAll);
         miSelectAll.setAction(actSelectAll);
         butPaste.setAction(actPaste);
         miPaste.setAction(actPaste);
 
         actExportSelecion.addAssociatedTable(tblData);
         butExportSelection.setAction(actExportSelecion);
         miExportCells.setAction(actExportSelecion);
         miProperties.setAction(actShowProps);
 
         butErazeContents.setAction(actClearSelection);
         miClearSelection.setAction(actClearSelection);
     }
 
     public void setupToolBar() {
         //  Make Tool bar replica of context menu...
         tbarMain.addSeparator(new Dimension(11, 0));
         tbarMain.add(actShowDetails).setText(actShowDetails.getValue(Action.NAME).toString());
         tbarMain.addSeparator(new Dimension(11, 0));
 
         //        Action actCopyTBar = new CopyAction();
         //        tbarMain.add( actCopyTBar).setText( "Hi");
         //        actCopyTBar.putValue( "hideActionText", false);
         tbarMain.add(actCopy);
         tbarMain.add(actPaste);
 
         tbarMain.addSeparator(new Dimension(11, 0));
         tbarMain.add(actAddCol).setText(actAddCol.getValue(Action.NAME).toString());
         tbarMain.add(actShowProps).setText(actShowProps.getValue(Action.NAME).toString());
         tbarMain.add(actSelectAll).setText(actSelectAll.getValue(Action.NAME).toString());
         tbarMain.add(actExportSelecion).setText(actExportSelecion.getValue(Action.NAME).toString());
         tbarMain.add(actClearSelection).setText(actClearSelection.getValue(Action.NAME).toString());
         tbarMain.addSeparator(new Dimension(11, 0));
         tbarMain.add(actResetAll);
     }
 
     /** Reset to defaults, as if these are being used for first time.
      */
     private void setupTblData() {
         //  Clear table with all columns..
         Enumeration<TableColumn> enTC = tblData.getColumnModel().getColumns();
         while (enTC.hasMoreElements()) {
             TableColumn tcRemoved = enTC.nextElement();
             tblData.getColumnModel().removeColumn(tcRemoved);
         }
         //  Notify all mutables columns are removed...
         //        firePropertyChange( COLUMNS_REMOVED_ALL, false, true);
 
         //  Apply Selection Schema...
         tblData.getSelectionModel().clearSelection();
         tblData.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         ListSelectionModel lsm = new DefaultListSelectionModel();
         lsm.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
         //  set up listeners...
         for (ListSelectionListener colSL : colSelectionListeners) {
             lsm.addListSelectionListener(colSL);
         }
         tblData.getColumnModel().setSelectionModel(lsm);
 
         //  set default rederers: we do NOT want chk boxes for boolean type columns...
         tblData.setDefaultRenderer(Boolean.class, null);
 
         tblData.revalidate();
     }
 
     //    void applyTableSorter( RowFilter rf){
     //        tblData.setRowSorter(tblRowSorter);
     //    }
 
     //
     //    Listeners...
     //
     public void propertyChange(PropertyChangeEvent evt) {
 
     }
 
     //
     //    Getters & Setters...
     //
     /**             Scenario C.2-B:  Model settings and Relay of Table Model Events.     */
     public void setReadOnlyDataModel(TableModel tmROData, ArrayList<SheetColumn> colSheetAL) {
         //  Chk Non-null Data Model
         if (null == tmROData) {
             throw new IllegalArgumentException(getClass().getName()
                     + ".setReadOnlyDataModel(...) does NOT excepts null Data Model.");
         }
 
         //setup table column width preferences...
         setupCompositeDataModel(tmROData, colSheetAL);
 
         //        //  Set up lister for the sorter...
         //        for (RowSorterListener rsl : tblRowSorterListener) {
         //            tblData.getRowSorter().removeRowSorterListener(rsl);
         //        }
         //        for (RowSorterListener rsl : tblRowSorterListener) {
         //            tblData.getRowSorter().addRowSorterListener(rsl);
         //        }
 
         tblData.getRowSorter().addRowSorterListener(new RowHeaderRefresher());
 
         //  Boolean should NOT be rendered as check boxes...
         tblData.setDefaultRenderer(Boolean.class, null);
         tblData.invalidate();
         tblRowHeader.invalidate();
     }
 
     public TableModel getReadOnlyDataModel() {
         return compositeDataModel.getPrimaryModel();
     }
 
     public TableModel getCompositeDataModel() {
         return compositeDataModel;
     }
 
     //    public TableColumnModel getTableColumnModel() {
     //        return tblData.getColumnModel();
     //    }
     /** @returns refrence to Jtable that is used to show contents.  */
     JTable getDataViewTable() {
         return tblData;
     }
 
     JSheetViewDataModel getViewTableModel() {
         return new JSheetViewDataModel(tblData, compositeDataModel);
     }
 
     ListSelectionModel getSelectionModel() {
         return tblData.getSelectionModel();
     }
 
     int[] getSelectedColumns() {
         return tblData.getSelectedColumns();
     }
 
     int getSelectedColumn() {
         return tblData.getSelectedColumn();
     }
 
     int getSelectedRow() {
         return tblData.getSelectedRow();
     }
 
     int[] getSelectedRows() {
         return tblData.getSelectedRows();
     }
 
     void setAdditionalToolbarActions(List<Action> actions) {
         //  remove all old actions...
         for (JComponent comp : butToolbarAdditionalAL) {
             tbarMain.remove(comp);
         }
 
         //  Append all new actions...
         butToolbarAdditionalAL.clear();
         for (Action act : actions) {
             JButton butInstalled = tbarMain.add(act);
             Common.setButtonsLooks(butInstalled);
             butToolbarAdditionalAL.add(butInstalled);
 
         }
 
         tbarMain.revalidate();
         tbarMain.repaint();
     }
 
     void setAdditionalContectMenuActions(List<Action> actions) {
 
     }
 
     //    /**
     //     *  Setups the Fixed columns to shown on the left hand side of the Data Table.
     //     *  Note they do not scroll with the selection...
     //     */
     //    public void setLeftColumns(JTable tblRowHeader, JComponent tblFixedHeader) {
     //        this.tblRowHeader = tblRowHeader;
     //        this.tblFixedHeader = tblFixedHeader;
     //    }
     /**     @return TRUE, if all the column in selection are extendable, i.e. editable and removable.
      */
     boolean isOnlyExtColumnInSelection() {
         int sCols[] = tblData.getSelectedColumns();
         if (sCols.length <= 0) {
             return false;
         }
 
         for (int viewColIdx : sCols) {
             int modelColIdx = tblData.convertColumnIndexToModel(viewColIdx);
             if (modelColIdx < compositeDataModel.roDataModel.getColumnCount()) //  At least one column, from Read-Only model is selected...
             {
                 return false;
             }
         }
         //  no-read-only column found...
         return true;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         tblRowHeader = new javax.swing.JTable();
         butRemoveExtendedCols = new javax.swing.JButton();
         popTblContextMnu = new javax.swing.JPopupMenu();
         miShowDetails = new javax.swing.JMenuItem();
         miAddCol = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         miCopy = new javax.swing.JMenuItem();
         miPaste = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JSeparator();
         miClearSelection = new javax.swing.JMenuItem();
         miReset = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         miSelectAll = new javax.swing.JMenuItem();
         miExportCells = new javax.swing.JMenuItem();
         miProperties = new javax.swing.JMenuItem();
         miExportRows = new javax.swing.JMenuItem();
         jPanel3 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         butSelectAll = new javax.swing.JButton();
         butCopy = new javax.swing.JButton();
         butPaste = new javax.swing.JButton();
         butExportSelection = new javax.swing.JButton();
         butErazeContents = new javax.swing.JButton();
         jPanel2 = new javax.swing.JPanel();
         addCol = new javax.swing.JButton();
         butRemoveCol = new javax.swing.JButton();
         butUndoColDelete = new javax.swing.JButton();
         popExtra = new javax.swing.JPopupMenu();
         miRemoveCols = new javax.swing.JMenuItem();
         miUndoRemoveCol = new javax.swing.JMenuItem();
         miCut = new javax.swing.JMenuItem();
         lblReset = new javax.swing.JLabel();
         pnlEmpty = new javax.swing.JPanel();
         lblSelectAll = new javax.swing.JLabel();
         jSeparator4 = new javax.swing.JToolBar.Separator();
         tbarMain = new javax.swing.JToolBar();
         butShowDetailsTB = new javax.swing.JButton();
         jSeparator5 = new javax.swing.JToolBar.Separator();
         butCopyTB = new javax.swing.JButton();
         butPasteTB = new javax.swing.JButton();
         jSeparator6 = new javax.swing.JToolBar.Separator();
         butAddUserColTB = new javax.swing.JButton();
         butPropertiesTB = new javax.swing.JButton();
         butSelectAllTB = new javax.swing.JButton();
         butExportSelectionTB = new javax.swing.JButton();
         butClearSelectionTB = new javax.swing.JButton();
         jSeparator7 = new javax.swing.JToolBar.Separator();
         butResetTB = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         spaData = new javax.swing.JScrollPane();
         tblData = new javax.swing.JTable();
 
         tblRowHeader.setModel(new javax.swing.table.DefaultTableModel(
                 new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } },
                 new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
         tblRowHeader.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblRowHeaderMouseClicked(evt);
             }
 
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 tblRowHeaderMousePressed(evt);
             }
         });
 
         butRemoveExtendedCols.setText("Remove All");
         butRemoveExtendedCols.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 butRemoveExtendedColsActionPerformed(evt);
             }
         });
 
         miShowDetails.setMnemonic('D');
         miShowDetails.setText("Show Row Details"); // NOI18N
         miShowDetails.setEnabled(false);
         popTblContextMnu.add(miShowDetails);
 
         miAddCol.setMnemonic('A');
         miAddCol.setText("Add Column");
         popTblContextMnu.add(miAddCol);
         popTblContextMnu.add(jSeparator1);
 
         miCopy.setMnemonic('C');
         miCopy.setText("Copy");
         popTblContextMnu.add(miCopy);
 
         miPaste.setMnemonic('P');
         miPaste.setText("Paste");
         popTblContextMnu.add(miPaste);
         popTblContextMnu.add(jSeparator3);
 
         miClearSelection.setMnemonic('e');
         miClearSelection.setText("Clear Selection");
         popTblContextMnu.add(miClearSelection);
 
         miReset.setMnemonic('R');
         miReset.setText("Reset");
         popTblContextMnu.add(miReset);
         popTblContextMnu.add(jSeparator2);
 
         miSelectAll.setMnemonic('l');
         miSelectAll.setText("Select All");
         popTblContextMnu.add(miSelectAll);
 
         miExportCells.setMnemonic('x');
         miExportCells.setText("Export Selection");
         popTblContextMnu.add(miExportCells);
 
         miProperties.setMnemonic('p');
         miProperties.setText("Properties"); // NOI18N
         popTblContextMnu.add(miProperties);
 
         miExportRows.setText("Export Rows ...");
 
         jPanel3.setLayout(new java.awt.GridBagLayout());
 
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(
                                                                        new javax.swing.border.LineBorder(
                                                                                new java.awt.Color(51, 0, 51), 1,
                                                                                true),
                                                                        "Selection",
                                                                        javax.swing.border.TitledBorder.CENTER,
                                                                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                                                                        new java.awt.Font("Tahoma", 1, 16)));
 
         butSelectAll.setText("Select All");
         butSelectAll.setVisible(false);
         butSelectAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 butSelectAllActionPerformed(evt);
             }
         });
         jPanel4.add(butSelectAll);
 
         butCopy.setText("Copy");
         butCopy.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 butCopyActionPerformed(evt);
             }
         });
         jPanel4.add(butCopy);
 
         butPaste.setText("Paste");
         jPanel4.add(butPaste);
 
         butExportSelection.setText("Export");
         jPanel4.add(butExportSelection);
 
         butErazeContents.setText("Clear Selection");
         butErazeContents.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 butErazeContentsActionPerformed(evt);
             }
         });
         jPanel4.add(butErazeContents);
 
         jPanel3.add(jPanel4, new java.awt.GridBagConstraints());
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                                                                        new javax.swing.border.LineBorder(
                                                                                new java.awt.Color(51, 0, 51), 1,
                                                                                true),
                                                                        " For New Columns Only ",
                                                                        javax.swing.border.TitledBorder.CENTER,
                                                                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                                                                        new java.awt.Font("Tahoma", 1, 16)));
 
         addCol.setText("Add Col");
         addCol.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addColActionPerformed(evt);
             }
         });
         jPanel2.add(addCol);
 
         butRemoveCol.setText("Remove Col");
         butRemoveCol.setEnabled(false);
         jPanel2.add(butRemoveCol);
 
         butUndoColDelete.setText("Undo Remove-Col");
         butUndoColDelete.setEnabled(false);
         jPanel2.add(butUndoColDelete);
 
         jPanel3.add(jPanel2, new java.awt.GridBagConstraints());
 
         miRemoveCols.setText("Remove Columns");
         miRemoveCols.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 miRemoveColsActionPerformed(evt);
             }
         });
         popExtra.add(miRemoveCols);
 
         miUndoRemoveCol.setText("Undo Remove Column");
         popExtra.add(miUndoRemoveCol);
 
         miCut.setMnemonic('u');
         miCut.setText("Cut"); // NOI18N
         popExtra.add(miCut);
 
         lblReset.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
 
         lblSelectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
         lblSelectAll.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 lblSelectAllMouseClicked(evt);
             }
         });
 
         setComponentPopupMenu(popTblContextMnu);
         setLayout(new java.awt.BorderLayout());
 
         tbarMain.setMargin(new java.awt.Insets(0, 11, 0, 0));
 
         butShowDetailsTB.setAction(actShowDetails);
         butShowDetailsTB.setFont(butShowDetailsTB.getFont().deriveFont(
                                                                        butShowDetailsTB.getFont().getStyle()
                                                                                & ~java.awt.Font.BOLD));
         butShowDetailsTB.setFocusable(false);
         butShowDetailsTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butShowDetailsTB);
         tbarMain.add(jSeparator5);
 
         butCopyTB.setAction(actCopy);
         butCopyTB.setFont(butCopyTB.getFont().deriveFont(butCopyTB.getFont().getStyle() & ~java.awt.Font.BOLD));
         butCopyTB.setMnemonic('c');
         butCopyTB.setToolTipText("Copy Selection to Clipboard");
         butCopyTB.setFocusable(false);
         butCopyTB.setHideActionText(true);
         butCopyTB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         tbarMain.add(butCopyTB);
 
         butPasteTB.setAction(actPaste);
         butPasteTB.setFont(butPasteTB.getFont().deriveFont(butPasteTB.getFont().getStyle() & ~java.awt.Font.BOLD));
         butPasteTB.setToolTipText("Paste from clipboard");
         butPasteTB.setHideActionText(true);
         butPasteTB.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         tbarMain.add(butPasteTB);
         tbarMain.add(jSeparator6);
 
         butAddUserColTB.setAction(actAddCol);
         butAddUserColTB.setFont(butAddUserColTB.getFont().deriveFont(
                                                                      butAddUserColTB.getFont().getStyle()
                                                                              & ~java.awt.Font.BOLD));
         butAddUserColTB.setToolTipText("New Column where you may edit contents");
         butAddUserColTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butAddUserColTB);
 
         butPropertiesTB.setAction(actShowProps);
         butPropertiesTB.setFont(butPropertiesTB.getFont().deriveFont(
                                                                      butPropertiesTB.getFont().getStyle()
                                                                              & ~java.awt.Font.BOLD));
         butPropertiesTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butPropertiesTB);
 
         butSelectAllTB.setAction(actSelectAll);
         butSelectAllTB.setFont(butSelectAllTB.getFont().deriveFont(
                                                                    butSelectAllTB.getFont().getStyle()
                                                                            & ~java.awt.Font.BOLD));
         butSelectAllTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butSelectAllTB);
 
         butExportSelectionTB.setAction(actExportSelecion);
         butExportSelectionTB.setFont(butExportSelectionTB.getFont().deriveFont(
                                                                                butExportSelectionTB.getFont().getStyle()
                                                                                        & ~java.awt.Font.BOLD));
         butExportSelectionTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butExportSelectionTB);
 
         butClearSelectionTB.setAction(actClearSelection);
         butClearSelectionTB.setFont(butClearSelectionTB.getFont().deriveFont(
                                                                              butClearSelectionTB.getFont().getStyle()
                                                                                      & ~java.awt.Font.BOLD));
         butClearSelectionTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butClearSelectionTB);
         tbarMain.add(jSeparator7);
 
         butResetTB.setAction(actResetAll);
         butResetTB.setFont(butResetTB.getFont().deriveFont(butResetTB.getFont().getStyle() & ~java.awt.Font.BOLD));
         butResetTB.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         tbarMain.add(butResetTB);
 
         add(tbarMain, java.awt.BorderLayout.NORTH);
 
         jPanel1.setInheritsPopupMenu(true);
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         spaData.setAutoscrolls(true);
         spaData.setInheritsPopupMenu(true);
         spaData.addAncestorListener(new javax.swing.event.AncestorListener() {
             public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
             }
 
             public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                 spaDataAncestorAdded(evt);
             }
 
             public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
             }
         });
 
         tblData.setAutoCreateColumnsFromModel(false);
         tblData.setAutoCreateRowSorter(true);
         tblData.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tblData.setCellSelectionEnabled(true);
         tblData.setInheritsPopupMenu(true);
         tblData.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tblDataMouseClicked(evt);
             }
 
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 tblDataMousePressed(evt);
             }
         });
         spaData.setViewportView(tblData);
 
         jPanel1.add(spaData, java.awt.BorderLayout.CENTER);
 
         add(jPanel1, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void miRemoveColsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRemoveColsActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_miRemoveColsActionPerformed
 
     private void addColActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColActionPerformed
         // TODO add your handling code here:
 
     }//GEN-LAST:event_addColActionPerformed
 
     private void butCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCopyActionPerformed
         // TODO add your handling code here:
         copySelectionIntoClipboard();
     }//GEN-LAST:event_butCopyActionPerformed
 
     private void butSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSelectAllActionPerformed
         // TODO add your handling code here:
 
     }//GEN-LAST:event_butSelectAllActionPerformed
 
     private void spaDataAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_spaDataAncestorAdded
         // TODO add your handling code here:
 
     }//GEN-LAST:event_spaDataAncestorAdded
 
     private void butRemoveExtendedColsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRemoveExtendedColsActionPerformed
         // TODO add your handling code here:
 
     }//GEN-LAST:event_butRemoveExtendedColsActionPerformed
 
     private void butErazeContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butErazeContentsActionPerformed
         // TODO add your handling code here:
 
     }//GEN-LAST:event_butErazeContentsActionPerformed
 
     private void tblRowHeaderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRowHeaderMousePressed
         // TODO add your handling code here:
         isSelectionFromRowHeader = true;
         extendSelectionsToAllColumns();
     }//GEN-LAST:event_tblRowHeaderMousePressed
 
     private void tblDataMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDataMousePressed
         // TODO add your handling code here:
         isSelectionFromRowHeader = false;
 
     }//GEN-LAST:event_tblDataMousePressed
 
     private void tblDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDataMouseClicked
         int rowModel = tblData.convertRowIndexToModel(tblData.getSelectedRow());
         // TODO add your handling code here:
         if (evt.getClickCount() == 2) {
             firePropertyChange(Common.EVENT_DATA_ROW_DOUBLE_CLICKED, -1, rowModel);
         } else {
             firePropertyChange(Common.EVENT_DATA_SINGLE_CLICKED, -1, rowModel);
         }
 
     }//GEN-LAST:event_tblDataMouseClicked
 
     private void tblRowHeaderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRowHeaderMouseClicked
         // TODO add your handling code here:
 
         if (evt.getClickCount() == 2) {
             int rowModel = tblData.convertRowIndexToModel(tblData.getSelectedRow());
             firePropertyChange(Common.EVENT_HEADER_ROW_DOUBLE_CLICKED, -1, rowModel);
         }
 
     }//GEN-LAST:event_tblRowHeaderMouseClicked
 
     private void lblSelectAllMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSelectAllMouseClicked
         // TODO add your handling code here:
         doSelectAll();
     }//GEN-LAST:event_lblSelectAllMouseClicked
 
     //
     //  BEHAVIOUR
     //
     void requestResetAll() {
         firePropertyChange(Common.REQUEST_RESET_ALL, 0, 1);
     }
 
     void doSelectAll() {
         //  Select All Rows in the view...
         extendSelectionsToAllColumns();
        tblData.getSelectionModel().setSelectionInterval(0, tblData.getRowCount()-1);
     }
 
     /**
      *  Setups Table Model for the data-Table, from scratch.
      */
     void setupCompositeDataModel(TableModel tmROData, ArrayList<SheetColumn> colSheetAL) {
         compositeDataModel = new CompositeTableModel(tmROData);
 
         //  Disable Row & Column Selection Listeners ...
         for (ListSelectionListener colSL : colSelectionListeners) {
             tblData.getColumnModel().getSelectionModel().removeListSelectionListener(colSL);
         }
 
         // Remove any current columns
         TableColumnModel cm = tblData.getColumnModel();
         while (cm.getColumnCount() > 0) {
             cm.removeColumn(cm.getColumn(0));
         }
 
         tblData.setModel(compositeDataModel);
         for (SheetColumn aColumn : colSheetAL) {
             tblData.addColumn(aColumn);
         }
         rsortTable = new TableRowSorter(compositeDataModel);
         rsortTable.setRowFilter(rfilterTbl);
         tblData.setRowSorter(rsortTable);
 
         //  Attach old Selection listeners again to the Table...
         for (ListSelectionListener colSL : colSelectionListeners) {
             tblData.getColumnModel().getSelectionModel().addListSelectionListener(colSL);
         }
 
         //  reset Default Table Column Width...
         for (int i = 0; i < tblData.getColumnCount(); i++) {
             tblData.getColumnModel().getColumn(i).setMinWidth(defaultColWidth);
         }
     }
 
     public void addTableRowSelectionListener(ListSelectionListener l) {
         tblData.getSelectionModel().addListSelectionListener(l);
     }
 
     public void removeTableRowSelectionListener(ListSelectionListener l) {
         tblData.getSelectionModel().removeListSelectionListener(l);
     }
 
     public void addTableColSelectionListener(ListSelectionListener l) {
         tblData.getColumnModel().getSelectionModel().removeListSelectionListener(l);
         tblData.getColumnModel().getSelectionModel().addListSelectionListener(l);
         colSelectionListeners.add(l);
     }
 
     public void removeTableColSelectionListener(ListSelectionListener l) {
         tblData.getColumnModel().getSelectionModel().removeListSelectionListener(l);
         colSelectionListeners.remove(l);
     }
 
     public void addTableModelListener(TableModelListener tml) {
         //        tblModelListener .add( tml);
         tblData.getModel().addTableModelListener(tml);
     }
 
     public void addTableRowsorterListener(RowSorterListener rsl) {
         tblRowSorterListener.add(rsl);
     }
 
     void setColumnVisibility(SheetColumn colSheet) {
         if (!(colSheet.isVisible() ^ isColumnExists(colSheet))) //  No change required...
         {
             return;
         }
         //  Apply change...
         if (colSheet.isVisible()) {
             tblData.getColumnModel().addColumn(colSheet);
         } else {
             tblData.getColumnModel().removeColumn(colSheet);
         }
     }
 
     boolean isColumnExists(TableColumn colTable) {
         Enumeration cols = tblData.getColumnModel().getColumns();
         while (cols.hasMoreElements()) {
             if (cols.nextElement() == colTable) {
                 return true;
             }
         }
 
         //  Not found...
         return false;
     }
 
     /**
      *  Author: Unknown  (But NOT Jassi)
      */
     public void pasteFromClipboard() {
         Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
 
         String clipData;
         try {
             clipData = (String) clipBoard.getData(DataFlavor.stringFlavor);
         } catch (Exception ex) {
             ex.printStackTrace();
             return;
         }
 
         if (null == clipData || clipData.length() == 0) // nothing to use...
         {
             return;
         }
 
         int targetCol = tblData.getSelectedColumn();
         int targetRow = tblData.getSelectedRow();
 
         if (targetRow == -1 || targetCol == -1) // we do NOT know where to paste...
         {
             return;
         }
 
         String[] rowsData = clipData.split("\n");
         if (rowsData == null || rowsData.length == 0) //  each row data should be terminated by <EOLN>
         {
             return;
         }
 
         //  Import each Row...
         for (int vRow = 0; vRow < rowsData.length; vRow++) {
             if (vRow + targetRow >= tblData.getRowCount()) //  ignore rest of the cells, as we have nowhere to paste these cells, as they pass table boundary...
             {
                 break;
             }
 
             //  Import all the cells, separated by tab...
             String[] cellsVal = rowsData[vRow].split("\t");
             for (int vCol = 0; vCol < cellsVal.length; vCol++) {
                 if (vCol + targetCol >= tblData.getColumnCount()) //  ignore rest of the cells, as we have nowhere to paste these cells, as they pass table boundary...
                 {
                     break;
                 }
                 int mRow = tblData.convertRowIndexToModel(vRow + targetRow);
                 int mCol = tblData.convertColumnIndexToModel(vCol + targetCol);
                 compositeDataModel.setValueAt(cellsVal[vCol], mRow, mCol);
             }
         }
     }
 
     void doFireShowDetailsRequest() {
         if (tblData.getSelectedRow() != -1) {
             //  only if there is a valid selection...
             int rowModel = tblData.convertRowIndexToModel(tblData.getSelectedRow());
             firePropertyChange(Common.REQUESTED_SHOW_ROW_DETAILS, -1, rowModel);
         }
     }
 
     void doFireAddUserColRequest() {
         firePropertyChange(Common.USER_COLUMN_ADDITION_REQUESTED, 0, 1);
     }
 
     /**     Clears Selected cells in the table.     */
     void doClearSelection() {
         doClearSelection(tblData.getSelectedRows(), tblData.getSelectedColumns());
     }
 
     /**     Clear contents of Cells, whose location is provided in parameters   */
     void doClearSelection(int[] rowsIdx, int[] colsIdx) {
         for (int rIdx = 0; rIdx < rowsIdx.length; rIdx++) {
             for (int cIdx = 0; cIdx < colsIdx.length; cIdx++) {
                 tblData.setValueAt("", rowsIdx[rIdx], colsIdx[cIdx]);
             }
         }
     }
 
     /** Selected Contents of table are copied into Clipboard. */
     public void copySelectionIntoClipboard() {
         StringBuffer selText = getTblSelectionDataWithTabs();
         if (null == selText || selText.length() == 0) //  Nothing to Select..
         {
             return;
         }
 
         StringSelection sSel = new StringSelection(selText.toString());
         Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
         clipBoard.setContents(sSel, sSel);
     }
 
     /**     ALL Selected rows and columns data would be picked and returned.
      * Columns will be separated with tabs.    */
     public StringBuffer getTblSelectionDataWithTabs() {
         return getTblSelectionData(false, false);
     }
 
     /**     ALL Selected rows and columns data would be picked and returned.
      * Columns will be separated with comma.    */
     public StringBuffer getTblSelectionDataWithCommas() {
         return getTblSelectionData(true, true);
     }
 
     /**     ALL columns for rows "selRows" would be picked, irrespective of the visiblility.
      *              Rows are in table viewcoordinated. */
     public StringBuffer getTblRowDataWithTabs(int[] selRows) {
         return getTblRowData(selRows, false, true);
     }
 
     /**     Returned data from those cells that are part of rows which are both Marked and Visible.
      * Data from only will be returned. ALL columns for rows in ''rowMarkedInModel" would be picked, subjected
      * to the condition that these are also visible.
      * rowMarkedInModel are in Table's Model coordinated.
      */
     public StringBuffer getTblRowDataWithComma(ArrayList<Boolean> rowMarkedInModel) {
         ArrayList<Integer> selIRows = new ArrayList<Integer>();
 
         //  Scan overall visible rows, and pick rows that are marked for export...
         for (int viewRowIdx = 0; viewRowIdx < tblData.getRowCount(); viewRowIdx++) {
             int modelRowIndex = tblData.convertRowIndexToModel(viewRowIdx);
             if (rowMarkedInModel.get(modelRowIndex)) // this row is marked for export...
             {
                 selIRows.add(viewRowIdx);
             }
         }
 
         //  Convert to array...
         int[] selRows = new int[selIRows.size()];
         for (int idx = 0; idx < selIRows.size(); idx++) {
             selRows[idx] = selIRows.get(idx);
         }
 
         //  These are the actual rows (both Marked and Visible - data from only these should be returned...
         return getTblRowData(selRows, true, true);
     }
 
     /**     ALL columns for rows "selRows" would be picked, irrespective of the visiblility.
      *              Rows are in table viewcoordinated.
      */
     public StringBuffer getTblRowData(int[] selRows, boolean forceEscapeComma, boolean isCommaSepatrated) {
 
         //  Make a selection that selects all the columns...
         int colCount = tblData.getColumnCount();
         int[] colsIdx = new int[colCount];
         for (int i = 0; i < colCount; i++) {
             colsIdx[i] = i;
         }
         return getTblSelectionData(selRows, colsIdx, forceEscapeComma, isCommaSepatrated);
     }
 
     /**     ALL Selected rows and columns data would be picked and returned.
      * forceEscapeComma: true => Encapsulate all cell values within double quotes.
      * false => Encapsulate those cell values within double quotes, that contains comma
      *
      * isCommaSepatrated: true => Separate all cell values with Comma
      * false => Separate all cell values with Tabs         */
     public StringBuffer getTblSelectionData(boolean forceEscapeComma, boolean isCommaSepatrated) {
         StringBuffer sBuff = new StringBuffer();
         int[] rowsIdx = tblData.getSelectedRows();
         int[] colsIdx = tblData.getSelectedColumns();
         return getTblSelectionData(rowsIdx, colsIdx, forceEscapeComma, isCommaSepatrated);
     }
 
     /**     ALL specified rows and columns data would be picked and returned.
      * rows and columns are specified with rowsIdx[] and colsIdx[] arrays.
      * Both values are in Table view coordinates.
      *
      * forceEscapeComma: true => Encapsulate all cell values within double quotes.
      * false => Encapsulate those cell values within double quotes, that contains comma
      *
      * isCommaSepatrated: true => Separate all cell values with Comma
      * false => Separate all cell values with Tabs         */
     public StringBuffer getTblSelectionData(int[] rowsIdx, int[] colsIdx, boolean forceEscapeComma,
                                             boolean isCommaSepatrated) {
         StringBuffer sBuff = new StringBuffer();
 
         if (rowsIdx.length > 0) {
             for (int rIdx = 0; rIdx < rowsIdx.length; rIdx++) {
                 for (int cIdx = 0; cIdx < colsIdx.length; cIdx++) {
                     Object cellValue = tblData.getValueAt(rowsIdx[rIdx], colsIdx[cIdx]);
 
                     if (cIdx > 0 && cIdx < colsIdx.length) {
                         if (isCommaSepatrated) {
                             sBuff.append(',');
                         } else {
                             sBuff.append("\t");
                         }
                     }
 
                     if (null == cellValue) {
                         sBuff.append("");
                     } else {
                         // If special character in the column name put it into double quotes
                         String strVal = cellValue.toString();
                         sBuff.append(forceEscapeComma || (strVal.indexOf(',') >= 0) ? '"' + strVal + '"' : strVal);
                     }
                 }
                 sBuff.append("\n");
             }
         }
         return sBuff;
     }
 
     //
     //    DEBUG Help...
     //
     void printSelection() {
         int[] scols = tblData.getSelectedColumns();
         int[] srows = tblData.getSelectedRows();
 
         for (int idx : srows) {
 
         }
         for (int idx : scols) {
 
         }
 
     }
 
     void printColList() {
         int idx = 0;
         TableColumnModel colModel = tblData.getColumnModel();
         Enumeration<TableColumn> eTC = colModel.getColumns();
         while (eTC.hasMoreElements()) {
             TableColumn tc = eTC.nextElement();
 
         }
     }
 
     //
     //      EVENT Mgmt
     //
     /**     Given a view-row-index of the data table, it returned the actual row number,
      * the one that was in effect when there was no sort/filter.   */
     int convertRowIndexToModel(int viewRowIndex) {
         return tblData.convertRowIndexToModel(viewRowIndex);
     }
 
     /**     Given a view-column-index of the data table, it returned the actual column number,
      * the one that was in effect when there was no shiting of columns.   */
     int convertColumnIndexToModel(int viewColIndex) {
         return tblData.convertColumnIndexToModel(viewColIndex);
     }
 
     /** Returned underlying table row count...  */
     int getRowCount() {
         return tblData.getRowCount();
     }
 
     void addExportCellsActionlistener(ActionListener exportCellsActionListener) {
         this.actLsnExportCells = exportCellsActionListener;
     }
 
     void addShowPropertyDialogActionListener(ActionListener actLsnShowPropDialogs) {
         this.actLsnShowPropDialogs = actLsnShowPropDialogs;
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addCol;
 
     private javax.swing.JButton butAddUserColTB;
 
     private javax.swing.JButton butClearSelectionTB;
 
     private javax.swing.JButton butCopy;
 
     private javax.swing.JButton butCopyTB;
 
     private javax.swing.JButton butErazeContents;
 
     private javax.swing.JButton butExportSelection;
 
     private javax.swing.JButton butExportSelectionTB;
 
     private javax.swing.JButton butPaste;
 
     private javax.swing.JButton butPasteTB;
 
     private javax.swing.JButton butPropertiesTB;
 
     private javax.swing.JButton butRemoveCol;
 
     private javax.swing.JButton butRemoveExtendedCols;
 
     private javax.swing.JButton butResetTB;
 
     private javax.swing.JButton butSelectAll;
 
     private javax.swing.JButton butSelectAllTB;
 
     private javax.swing.JButton butShowDetailsTB;
 
     private javax.swing.JButton butUndoColDelete;
 
     private javax.swing.JPanel jPanel1;
 
     private javax.swing.JPanel jPanel2;
 
     private javax.swing.JPanel jPanel3;
 
     private javax.swing.JPanel jPanel4;
 
     private javax.swing.JSeparator jSeparator1;
 
     private javax.swing.JSeparator jSeparator2;
 
     private javax.swing.JSeparator jSeparator3;
 
     private javax.swing.JToolBar.Separator jSeparator4;
 
     private javax.swing.JToolBar.Separator jSeparator5;
 
     private javax.swing.JToolBar.Separator jSeparator6;
 
     private javax.swing.JToolBar.Separator jSeparator7;
 
     private javax.swing.JLabel lblReset;
 
     private javax.swing.JLabel lblSelectAll;
 
     private javax.swing.JMenuItem miAddCol;
 
     private javax.swing.JMenuItem miClearSelection;
 
     private javax.swing.JMenuItem miCopy;
 
     private javax.swing.JMenuItem miCut;
 
     private javax.swing.JMenuItem miExportCells;
 
     private javax.swing.JMenuItem miExportRows;
 
     private javax.swing.JMenuItem miPaste;
 
     private javax.swing.JMenuItem miProperties;
 
     private javax.swing.JMenuItem miRemoveCols;
 
     private javax.swing.JMenuItem miReset;
 
     private javax.swing.JMenuItem miSelectAll;
 
     private javax.swing.JMenuItem miShowDetails;
 
     private javax.swing.JMenuItem miUndoRemoveCol;
 
     private javax.swing.JPanel pnlEmpty;
 
     private javax.swing.JPopupMenu popExtra;
 
     private javax.swing.JPopupMenu popTblContextMnu;
 
     private javax.swing.JScrollPane spaData;
 
     private javax.swing.JToolBar tbarMain;
 
     private javax.swing.JTable tblData;
 
     private javax.swing.JTable tblRowHeader;
 
     // End of variables declaration//GEN-END:variables
 
     /** Returns an ImageIcon, or null if the path was invalid. */
     protected ImageIcon createImageIcon(String pathImage) {
 
         pathImage = "/" + this.getClass().getPackage().getName().replace('.', '/') + '/' + pathImage;
         //        pathImage = '/'+this.getClass().getPackage().getName().replace('.', '/') + '/'+pathImage;
         java.net.URL imgURL = getClass().getResource(pathImage);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("[ViewDataConsole]: Unable to locate Toolbar icon images: " + pathImage);
             return null;
         }
     }
 
     //
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     //                                               HELPER  CLASS    
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     //    
     /// Actions...
     class RemoveAllSelectedExtColumnsAction extends AbstractAction {
 
         RemoveAllSelectedExtColumnsAction() {
             super("Remove Columns");
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     /** onClicking show details of the selected row. */
     class ShowDetailsAction extends AbstractAction {
 
         ShowDetailsAction() {
             super("Show Details", createImageIcon("images/show_details.png"));
             //            super("Show Details", createImageIcon( "images/show_details.png"));
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             doFireShowDetailsRequest();
         }
     }
 
     /** Add user column, which are editable. */
     class AddUserColAction extends AbstractAction {
 
         AddUserColAction() {
             super("Add Column", createImageIcon("images/add_user_col.png"));
         }
 
         public void actionPerformed(ActionEvent e) {
             doFireAddUserColRequest();
         }
     }
 
     /** Add user column, which are editable. */
     class CutAction extends AbstractAction {
 
         CutAction() {
             super("Cut");
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             isCutInProgress = true;
             copySelectionIntoClipboard();
         }
     }
 
     /** Add user column, which are editable. */
     class CopyAction extends AbstractAction {
 
         CopyAction() {
             super("Copy", createImageIcon("images/copy.png"));
         }
 
         public void actionPerformed(ActionEvent e) {
             copySelectionIntoClipboard();
         }
     }
 
     /** Revert to original state, no FIlter, no sort, no column shifting, nothing... . */
     class ResetAllAction extends AbstractAction {
 
         ResetAllAction() {
             super("Reset", createImageIcon("images/reset.png"));
         }
 
         public void actionPerformed(ActionEvent e) {
             requestResetAll();
         }
     }
 
     /** Sellect all cells. */
     class SelectAllAction extends AbstractAction {
 
         SelectAllAction() {
             super("Select All", createImageIcon("images/sel_all.png"));
         }
 
         public void actionPerformed(ActionEvent e) {
             doSelectAll();
         }
     }
 
     /**     Undo deleted columns */
     class UndeleteColumnsAction extends AbstractAction {
 
         UndeleteColumnsAction() {
             super("Undo Remove-Col");
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     /**     Paste from Clipboard.       */
     class PasteFromClipAction extends AbstractAction {
 
         PasteFromClipAction() {
             super("Paste", createImageIcon("images/paste.png"));
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             isCutInProgress = false;
             pasteFromClipboard();
         }
     }
 
     /**     Paste from Clipboard.       */
     class ClearSelectionAction extends AbstractAction {
 
         ClearSelectionAction() {
             super("Clear ", createImageIcon("images/clear_sel.png"));
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             doClearSelection();
         }
     }
 
     /**     Export Selection to File.       */
     class ExportSelectionAction extends AbstractAction implements ListSelectionListener {
 
         /**  Listen to following table for selection events, and keep enabled status synchronised to it.  */
         JTable tblAssociated;
 
         ExportSelectionAction() {
             super("Export ", createImageIcon("images/exp_sel.png"));
             setEnabled(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             actLsnExportCells.actionPerformed(e);
         }
 
         /** This instance will attach itself to the Table, for listening table selections   */
         private void addAssociatedTable(JTable tbl) {
             tblAssociated = tbl;
             tbl.getSelectionModel().addListSelectionListener(this);
             tbl.getColumnModel().getSelectionModel().addListSelectionListener(this);
         }
 
         public void valueChanged(ListSelectionEvent e) {
             setEnabled(tblAssociated.getSelectedRowCount() + tblAssociated.getSelectedColumnCount() > 0);
         }
     }
 
     /**     Show Properties Dialog.       */
     class ShowCustomizationConsoleAction extends AbstractAction {
 
         ShowCustomizationConsoleAction() {
             super("Properties", createImageIcon("images/prop.png"));
             setEnabled(true);
         }
 
         public void actionPerformed(ActionEvent e) {
             actLsnShowPropDialogs.actionPerformed(e);
             //            firePropertyChange(Common.REQUEST_SHOW_CUSTOMIZATION_CONSOLE, 0, 1);
         }
     }
 
     /**     Listen to Ciolumnm Selections, so that dependent  button could be enabled/disabled.     */
     class ColSelectionsListener implements ListSelectionListener {
 
         public void valueChanged(ListSelectionEvent e) {
             //             
             if (!e.getValueIsAdjusting()) {
                 boolean onlyExtColumnSelected = isOnlyExtColumnInSelection();
                 actRemoveCols.setEnabled(onlyExtColumnSelected);
                 actPaste.setEnabled(onlyExtColumnSelected);
                 actClearSelection.setEnabled(onlyExtColumnSelected);
             }
         }
     }
 
     /**     Listen to Ciolumnm Selections, so that dependent  button could be enabled/disabled.     */
     class RowSelectionsListener implements ListSelectionListener {
 
         public void valueChanged(ListSelectionEvent e) {
 
             if (!e.getValueIsAdjusting()) {
                 actShowDetails.setEnabled(tblData.getSelectedRow() >= 0);
                 firePropertyChange(JSheet.EVENT_DATA_SINGLE_CLICKED, -1, e.getLastIndex());
             }
         }
     }
 
     class CompositeTableModel extends AbstractTableModel implements TableModelListener {
         //
         //         Enclosed State and Workers......
         //
         /** The Read Only component of the Table Data is stored here. */
         private TableModel roDataModel;
 
         /** The New Columns of the Table Data are stored and maintained here. */
         private DefaultTableModel extDataModel;
 
         //  Fixed state, once read-only models is connected to this...
         int roModelRowCount = -1;
 
         int roModelColCount = -1;
 
         //
         //         Constructor...
         //
         /**
          *  It is asumed that Read Only Model does NOT change.
          *  Therefore,  changes performed in read-only model will not be automatically be shown in the JTable.
          */
         private CompositeTableModel(TableModel roDataModel) {
             setPrimaryDataModel(roDataModel);
 
             if (null == extDataModel) //  the extentions to the Table are written to this model,
             //  so that the Read-Only Model remains intact...
             {
                 extDataModel = new DefaultTableModel(roDataModel.getRowCount(), 0);
             }
 
             connectModels();
         }
 
         //
         //         Property Accessors...
         //
         /** Getter method for underlying Read-Only Table Model */
         TableModel getPrimaryModel() {
             return roDataModel;
         }
 
         /** Setter method for underlying Read-Only Table Model */
         private void setPrimaryDataModel(TableModel roDataModel) {
             if (null == roDataModel) {
                 throw new IllegalStateException("Primary data Model NOT set. "
                         + "setupCompositeDataModel() should be called after "
                         + "setPrimaryDataModel(). NOTE: roDataModel=" + roDataModel);
             }
 
             this.roDataModel = roDataModel;
             roModelColCount = roDataModel.getColumnCount();
             roModelRowCount = roDataModel.getRowCount();
         }
 
         /** Getter for Underlying Extended table Model */
         TableModel getExtendedTableModel() {
             return extDataModel;
         }
 
         //
         //         Behaviour management...
         //
         private void clearExtendedDataModel() {
             extDataModel.getDataVector().clear();
             extDataModel.setColumnCount(0);
             extDataModel.setRowCount(0);
         }
 
         private void connectModels() {
             extDataModel.addTableModelListener(CompositeTableModel.this);
         }
 
         private void disConnectModels() {
             extDataModel.removeTableModelListener(CompositeTableModel.this);
         }
 
         //
         //         Listeners management...
         //
         public void tableChanged(TableModelEvent oe) {
             //  Performing Co-ordinates Transformation (Ext-Model space to Composite-Model space)...
             int fr = oe.getFirstRow();
             int lr = oe.getLastRow();
             int col = oe.getColumn() + roDataModel.getColumnCount();
             TableModelEvent ne = new TableModelEvent((TableModel) oe.getSource(), fr, lr, col, oe.getType());
             // Relay to Parent...
             fireTableChanged(ne);
         }
 
         //
         //         Table Model's Abstract Method now Implemented...
         //
         public int getRowCount() {
             assert roDataModel.getRowCount() == roModelRowCount;
             return roDataModel.getRowCount();
         }
 
         public int getColumnCount() {
             assert roDataModel.getColumnCount() == roModelColCount;
             return roDataModel.getColumnCount() + extDataModel.getColumnCount();
         }
 
         @Override
         public String getColumnName(int columnIndex) {
             assert roDataModel.getColumnCount() == roModelColCount;
             if (columnIndex < roDataModel.getColumnCount()) {
                 //  ReadOnly Model has this information...
                 return roDataModel.getColumnName(columnIndex);
             }
 
             //  column number index in Extensible model is required, to get Column Name...
             int idxCol2 = columnIndex - roDataModel.getColumnCount();
             return extDataModel.getColumnName(idxCol2);
         }
 
         @Override
         public Class<?> getColumnClass(int columnIndex) {
             if (columnIndex < roDataModel.getColumnCount()) {
                 //  ReadOnly Model has this information...
                 return roDataModel.getColumnClass(columnIndex);
             }
 
             //  Assume all to be strings...
             return String.class;
         }
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             if (columnIndex < roDataModel.getColumnCount()) //  ReadOnly Model has this information...
             {
                 return false;
             }
 
             //  column number index in Extensible model is required, to get Column Name...
             int idxCol2 = columnIndex - roDataModel.getColumnCount();
             return extDataModel.isCellEditable(rowIndex, idxCol2);
         }
 
         public Object getValueAt(int rowIndex, int columnIndex) {
             assert roDataModel.getColumnCount() == roModelColCount;
             if (columnIndex < roDataModel.getColumnCount()) //  ReadOnly Model has this information...
             {
                 return roDataModel.getValueAt(rowIndex, columnIndex);
             }
 
             //  column number index in Extensible model is required, to get Column Name...
             int idxCol2 = columnIndex - roDataModel.getColumnCount();
             return extDataModel.getValueAt(rowIndex, idxCol2);
         }
 
         @Override
         public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
             if (columnIndex < roDataModel.getColumnCount()) //  ReadOnly Model has this information...
             {
                 throw new IllegalStateException("These table cells are read-Only.");
             }
 
             //  column number index in Extensible model is required, to get Column Name...
             int idxCol2 = columnIndex - roDataModel.getColumnCount();
             extDataModel.setValueAt(aValue, rowIndex, idxCol2);
         }
 
         /**
          *  @returns: The newly created tableColumn. It NOT attached to the table yet.
          */
         private TableColumn createNewColumn() {
             //  Create a new Column, set its header value and return the reference...
             String colName = EXT_COLUMNS_NAME_PREFIX + (++columnAppendCount);
             TableColumn newTC = new TableColumn(getColumnCount());
             newTC.setHeaderValue(colName);
             newTC.setPreferredWidth(defaultColWidth);
             newTC.setMinWidth(defaultColWidth);
             return newTC;
         }
 
         /**     Now add a new column to the underlying storage.
          * it is still NOT safe to announce its presence, so momemtorarily disable listeners     */
         private void addNewColumn(TableColumn tc) {
             //  We do NOT intermediate changes to be sent to JTable...
             disConnectModels();
             //  Add column now...
             tc.setModelIndex(getColumnCount());
             extDataModel.addColumn(tc.getHeaderValue());
             //  revert back...
             connectModels();
         }
 
         /**
          *  @returns Array of all the columns that belongs to Extended Column Model
          */
         private int[] getViewIdxForAllExtColumns() {
             return new int[0];
         }
     }
 
     /**     MOdel that is responsible for rendering Row header for the Data View Table  */
     class RowHeaderTableModel extends AbstractTableModel {
 
         public int getRowCount() {
             return tblData.getRowCount();
         }
 
         public int getColumnCount() {
             return 1;
         }
 
         public Object getValueAt(int rowIndex, int columnIndex) {
             return tblData.convertRowIndexToModel(rowIndex) + 1;
         }
 
         @Override
         public String getColumnName(int columnIndex) {
             return "#";
         }
     }
 
     class HeaderAndDataScrolSync implements ChangeListener {
 
         public void stateChanged(ChangeEvent e) {
             Point pViewPos = spaData.getRowHeader().getViewPosition();
             spaData.getViewport().setViewPosition(pViewPos);
         }
     }
 
     /** Row header cell renderer that shows selection of rows and row numbers   */
     class FixedTblSelHangerRenderer extends DefaultTableCellRenderer {
 
         FixedTblSelHangerRenderer() {
             super();
             //            int rh = tblRowHeader.getRowHeight();
             //            //            int cw = tblRowHeader.get;
             ////            mg.setSize( getWidth(), rh);
             //            mg.setSize(14, rh - 1);
             //            setLayout(new BorderLayout());
             //            add(mg, BorderLayout.CENTER);
             //            //            pnlFixedTblHangerRenderer.setSize( 10, rh);
             //            revalidate();
         }
 
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
 
             //            setText(value.toString());
 
             //            mg.setSelected(isSelected && selectionRowMode);
             //            if (isSelected) {
             //                setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
             //            } else {
             //                setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
             //            }
 
             invalidate();
             //            return this;
             return super.getTableCellRendererComponent(table, value, isSelected && isSelectionFromRowHeader,
                                                        hasFocus, row, column);
         }
     }
 
     class RowHeaderHeadEventHandler extends MouseAdapter {
 
         @Override
         public void mouseClicked(MouseEvent e) {
             tblData.getRowSorter().setSortKeys(null);
         }
     }
 
     class RowHeaderRefresher implements RowSorterListener {
 
         public void sorterChanged(RowSorterEvent e) {
             tblRowHeader.repaint();
         }
     }
 
     /**     Test Run: Stand alone mode...   */
     public static void main(String[] args) {
         SheetTestFrame.main(args);
     }
 
     //
     //     Grave-Yard...
     //
 }
