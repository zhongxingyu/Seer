 package org.amanzi.splash.ui;
 
 /*
  * "The Java Developer's Guide to Eclipse"
  *   by D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
  * 
  * (C) Copyright International Business Machines Corporation, 2003, 2004. 
  * All Rights Reserved.
  * 
  * Code or samples provided herein are provided without warranty of any kind.
  */
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import javax.swing.CellEditor;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.swing.ColumnHeaderRenderer;
 import org.amanzi.splash.swing.RowHeaderRenderer;
 import org.amanzi.splash.swing.SplashTable;
 import org.amanzi.splash.swing.SplashTableModel;
 import org.amanzi.splash.ui.wizards.ExportScriptWizard;
 import org.amanzi.splash.utilities.ActionUtil;
 import org.amanzi.splash.utilities.Util;
 import org.eclipse.albireo.core.SwingControl;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.ui.rubyeditor.EditorUtility;
 
 import com.eteks.openjeks.format.CellFormat;
 import com.eteks.openjeks.format.CellFormatPanel;
 
 /**
  * Defines a sample "mini-spreadsheet" editor that demonstrates how to create an
  * editor whose input is based on either resources from the workspace (IDE) or
  * directly from the file system (RCP).
  */
 public abstract class AbstractSplashEditor extends EditorPart implements TableModelListener {
 	static final String STD_HEADINGS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	private int defaultAlignment;
 	private boolean isDirty = false;
 	private boolean enabled;
 	private TableViewer tableViewer;
 	CellFormatPanel cellFormatPanel = null;
 	CellFormat cellFormat = null;
 	SplashTableModel tableModel; 
 
 	private ResourceBundle resourceBundle = ResourceBundle.getBundle ("com.eteks.openjeks.resources.openjeks");
 	private String selectCellValue = "";
 	SplashTable table;
 	private Thread thread;
 	private SwingControl swingControl; 
 	private int ROWS_EDGE_MARGIN = 5;
 	private int COLUMNS_EDGE_MARGIN = 5;
 
 	/**
 	 * Class constructor
 	 */
 	public AbstractSplashEditor() {
 		table = new SplashTable();
 		table.getModel().addTableModelListener(this);
 	}
 
 	public SplashTable getTable(){
 		return table;
 	}
 
 	/**
 	 * Create a new valid <code>IEditorInput</code> for this concrete
 	 * implementation (e.g., by opening a "new dialog" or launching a 
 	 * creation wizard).
 	 */
 	public abstract IEditorInput createNewInput(String message)
 	throws CoreException;
 
 	/**
 	 * 
 	 * @param table
 	 */
 	private void launchCellFormatPanel(SplashTable table)
 	{
 		int firstRow, firstColumn, lastRow, lastColumn;
 		firstRow = table.getSelectedRow();
 		firstColumn = table.getSelectedColumn();
 		lastRow = firstRow + table.getSelectedRowCount() - 1;
 		lastColumn = firstColumn + table.getSelectedColumnCount() - 1;
 		//cellFormat = getTable().tableFormat.getFormatAt(firstRow, firstColumn , lastRow, lastColumn);
 		
 		
 		try {
 		      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		    } catch(Exception e) {
 		      System.out.println("Error setting Java LAF: " + e);
 		    }
 
 		
 		cellFormat = ((Cell)table.getValueAt(table.getSelectedRow(), table.getSelectedColumn())).getCellFormat();
 		cellFormatPanel = new CellFormatPanel(cellFormat);
 		if (JOptionPane.showConfirmDialog(null,
 				cellFormatPanel,
 				resourceBundle.getString ("FORMAT_PANEL_TITLE"),
 				JOptionPane.OK_CANCEL_OPTION ,
 				JOptionPane.PLAIN_MESSAGE) == 0)
 		{
 			cellFormat = cellFormatPanel.getCellFormat();
 			//getTable().tableFormat.setFormatAt(cellFormat, firstRow, firstColumn , lastRow, lastColumn);
 			//table.getValueAt(table.getsele, arg1)
 			for (int i=firstRow;i<=lastRow;i++){
 				for (int j=firstColumn;j<=lastColumn;j++){
 					((Cell)table.getValueAt(i, j)).setCellFormat(cellFormat);
 				}
 			}
 			setIsDirty(true);
 		}
 		cellFormatPanel = null;
 		table.repaint();
 	}
 
 	/**
 	 * 
 	 */
 	public void tableChanged(TableModelEvent e) {
 		int row = e.getFirstRow();
 		int column = e.getColumn();
 		TableModel model = (TableModel)e.getSource();
 		if (!selectCellValue.equals(model.getValueAt(row, column))) setIsDirty(true);
 	}
 
 
 	/**
 	 * Updates value of Cell from referenced script
 	 * 
 	 * @param cell Cell to export
 	 * @author Lagutko_N
 	 */
 
 	private void updateCell(final Cell cell) {
 		//if Cell has no reference script than it cannot be update
 		if (!cell.hasReference()) {
 			//TODO: add error message
 			return;
 		}
 
 		tableModel.updateCellFromScript(cell);
 	}
 
 	/**
 	 * Opens referenced script in editor
 	 * 
 	 * @param cell cell to export
 	 * @author Lagutko_N
 	 */
 
 	private void openCell(final Cell cell) {
 		final Display display = swingControl.getDisplay();
 
 		//if Cell has no references to script than it cannot be open
 		if (!cell.hasReference()) {
 			//TODO: add error message
 			return;
 		}
 
 		//run open action
 		ActionUtil.getInstance(display).runTask(new Runnable() {
 			public void run() {
 				//find file by URI
 				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(cell.getScriptURI());
 				if (files.length == 1) {
 					try {
 						//open file in editor
 						EditorUtility.openInEditor(files[0]);
 					}
 					catch (RubyModelException e) {
 						//TODO: handle excpetion
 					}
 					catch (PartInitException e) {
 						//TODO: handle excpetion
 					}
 				}
 				else {
 					//TODO: handle this situation
 				}
 			}
 		});
 	}
 
 	/**
 	 * Method that exports cell to script 
 	 *
 	 * @param Cell cell to export
 	 * @author Lagutko_N
 	 */
 
 	private void exportCell(final Cell cell) {		
 		//get Cell and Display
 		final Display display = swingControl.getDisplay();
 
 		//run ExportScriptWizard
 		ActionUtil.getInstance(display).runTask( new Runnable() {
 			public void run() {
 				WizardDialog dialog = new WizardDialog(display.getActiveShell(), new ExportScriptWizard(cell));
 				dialog.open();
 			}
 		});
 	}
 
 	/**
 	 * handle event for cancelling cell editing
 	 */
 	private void cancelCellEditing() {
 		CellEditor ce = table.getCellEditor();
 		if (ce != null) {
 			ce.cancelCellEditing();
 		}
 	}
 
 
 
 	/**
 	 * show column menu pop up 
 	 * @param e
 	 */
 	private void maybeShowColumnPopup(MouseEvent e){
 		//Lagutko, 16.06.2009, isPopupTrigger doesn't work correctly for Windows systems
 		if (/*e.isPopupTrigger() &&*/ table.isEnabled()) {
 			Point p = new Point(e.getX(), e.getY());
 			int col = table.columnAtPoint(p);
 			int row = table.rowAtPoint(p);
 
 //			translate table index to model index
 			int mcol = table.getColumn(
 					table.getColumnName(col)).getModelIndex();
 
 			if (row >= 0 && row < table.getRowCount()) {
 				cancelCellEditing();
 
 //				create popup menu...
 				JPopupMenu contextMenu = createColumnContextMenu(row,
 						mcol);
 
 //				... and show it
 				if (contextMenu != null
 						&& contextMenu.getComponentCount() > 0) {
 					contextMenu.show(table, p.x, p.y);
 				}
 			}
 		}
 	}
 
 	/**
 	 * show row menu pop up
 	 * @param e
 	 */
 	private void maybeShowRowPopup(MouseEvent e){
 		//Lagutko, 16.06.2009, isPopupTrigger doesn't work correctly for Windows systems
 		if (/*e.isPopupTrigger() &&*/ table.isEnabled()) {
 			Point p = new Point(e.getX(), e.getY());
 			int col = table.columnAtPoint(p);
 			int row = table.rowAtPoint(p);
 
 //			translate table index to model index
 			int mcol = table.getColumn(
 					table.getColumnName(col)).getModelIndex();
 
 			if (row >= 0 && row < table.getRowCount()) {
 				cancelCellEditing();
 
 //				create popup menu...
 				JPopupMenu contextMenu = createRowContextMenu(row,
 						mcol);
 
 //				... and show it
 				if (contextMenu != null
 						&& contextMenu.getComponentCount() > 0) {
 					contextMenu.show(table, p.x, p.y);
 				}
 			}
 		}
 	}
 
 	/**
 	 * show cell menu pop up
 	 * @param e
 	 */
 	private void maybeShowPopup(MouseEvent e) {
 		//Lagutko, 16.06.2009, isPopupTrigger doesn't work correctly for Windows systems
 		if (/*e.isPopupTrigger() &&*/ table.isEnabled()) {
 			Point p = new Point(e.getX(), e.getY());
 			int col = table.columnAtPoint(p);
 			int row = table.rowAtPoint(p);
 
 //			translate table index to model index
 			int mcol = table.getColumn(
 					table.getColumnName(col)).getModelIndex();
 
 			if (row >= 0 && row < table.getRowCount()) {
 				cancelCellEditing();
 
 //				create popup menu...
 				JPopupMenu contextMenu = createContextMenu(row,	mcol);
 
 //				... and show it
 				if (contextMenu != null
 						&& contextMenu.getComponentCount() > 0) {
 					contextMenu.show(table, p.x, p.y);
 				}
 			}
 		}
 	}
 	// Returns the preferred height of a row.
     // The result is equal to the tallest cell in the row.
     public int getPreferredRowHeight(JTable table, int rowIndex, int margin) {
         // Get the current default height for all rows
         int height = table.getRowHeight();
     
         // Determine highest cell in the row
         for (int c=0; c<table.getColumnCount(); c++) {
             TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
             Component comp = table.prepareRenderer(renderer, rowIndex, c);
             int h = comp.getPreferredSize().height + 2*margin;
             height = Math.max(height, h);
         }
         return height;
     }
     
    
 
 	/**
 	 * create Swing table
 	 * @param parent
 	 */
 	private void createTable(final Composite parent) {
 		swingControl = new SwingControl(parent, SWT.NONE) {
 			{
 				setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 				setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
 			}
 			protected JComponent createSwingComponent() {
 
 				JScrollPane scrollPane = new JScrollPane(table);
 
 				table.setOpaque(true);
 				table.setBackground(getAWTHierarchyRoot().getBackground());
 
 				scrollPane.setBorder(new EmptyBorder(1,1,1,1));
 
 				scrollPane.setRowHeaderView(getTable().rowHeader);
 				
 				
 				table.setRowHeight(table.getDefaultRowHeight());
 				
 				//table.getColumnModel().getColumn(2).setCellRenderer(new BackgroundColumnCellRenderer(new java.awt.Color(255,255,204)));
 				
 				for (int i=0;i<table.getColumnCount();i++){
 					TableColumn col = table.getColumnModel().getColumn(i);
 				    col.setPreferredWidth(table.getDefaultColumnWidth());
 				    
 				    col.setHeaderRenderer(new ColumnHeaderRenderer(table.getDefaultColumnWidth(), 20));
 				}
 				
 				// Handle the listener
 				ListSelectionModel selectionModel = table.getSelectionModel();
 				selectionModel.addListSelectionListener(new ListSelectionListener(){
 
 					//@Override
 					public void valueChanged(ListSelectionEvent e) {
 						// TODO Auto-generated method stub
 						
 						updateTableHeaderHighlights(table.getSelectedRow(), table.getSelectedColumn());
 						
 //						for (int i=0;i<table.getSelectedRows().length;i++){
 //							for (int j=0;j<table.getSelectedColumns().length;j++){
 //								updateTableHeaderHighlights(i,j);								
 //							}
 //						}
 					}
 					
 				});
 
 				
 				// set selection mode for contiguous  intervals
 				MouseListener ml = new HeaderMouseAdapter();
 					
 				// we don't allow reordering
 				table.getTableHeader().setReorderingAllowed(false);
 				table.getTableHeader().addMouseListener(ml);
 				table.rowHeader.addMouseListener(ml);
 				table.addKeyListener(new KeyListener(){
 
 					public void keyPressed(KeyEvent e) {
 						
 					}
 
 					@SuppressWarnings("static-access")
 					public void keyReleased(KeyEvent e) {
 						int row = table.getSelectedRow();
 						int column = table.getSelectedColumn();
 						Util.logn("e.getKeyCode() = " + e.getKeyCode());
 						//Util.log("e.VK_ENTER = " + e.VK_ENTER);
 						if (e.getKeyCode() == e.VK_ENTER) /*ENTER*/
 						{
 							
 							int rdiff = table.getRowCount() - row;
 							int cdiff = table.getColumnCount() - column;
 
 							
 							if (rdiff < COLUMNS_EDGE_MARGIN)
 							{
 								getTable().insertRows(table.getRowCount()-1, COLUMNS_EDGE_MARGIN-rdiff);
 								setIsDirty(true);
 							}
 
 							if (cdiff < ROWS_EDGE_MARGIN)
 							{
 								getTable().insertColumns(table.getColumnCount()-1,ROWS_EDGE_MARGIN-cdiff );
 								setIsDirty(true);
 							}						
 						}
 						
 						
 						if (e.isControlDown() && e.getKeyCode() == 67){
 							table.copyCell(row, column);
 							
 						}else if (e.isControlDown() && e.getKeyCode() == 88){
 							table.cutCell(row, column);
 						}
 						else if (e.isControlDown() && e.getKeyCode() == 86){
 							table.pasteCell(row, column);
 						}else if (e.isControlDown() && e.getKeyCode() == 66){
 							Util.logn("user pressed CTRL+B");
 							Cell cell = (Cell)table.getValueAt(row, column);
 							CellFormat cf = cell.getCellFormat();
 							
 							Integer fs = cf.getFontStyle().intValue();
 							
 							switch (fs){
 							case Font.PLAIN:
 								fs = Font.BOLD;
 								break;
 							case Font.BOLD:
 								fs = Font.PLAIN;
 								break;
 							case Font.ITALIC:
 								fs = Font.BOLD+Font.ITALIC;
 								break;
 							case Font.BOLD+Font.ITALIC:
 								fs = Font.ITALIC;
 								break;
 							}
 							
 							Util.logn("fs: " + fs);
 							cf.setFontStyle(fs);
 							table.tableFormat.setFormatAt(cf, row, column, row, column);
 							cell.setCellFormat(cf);
 							table.repaint();
 							setIsDirty(true);
 						}else if (e.isControlDown() && e.getKeyCode() == 73){
 							Util.logn("user pressed CTRL+i");
 							Cell cell = (Cell)table.getValueAt(row, column);
 							CellFormat cf = cell.getCellFormat();
 							
 							Integer fs = cf.getFontStyle().intValue();
 							Util.logn("CTRL+B has been pressed ");
 							switch (fs){
 							case Font.PLAIN:
 								fs = Font.ITALIC;
 								break;
 							case Font.BOLD:
 								fs = Font.BOLD+Font.ITALIC;
 								break;
 							case Font.ITALIC:
 								fs = Font.PLAIN;
 								break;
 							case Font.BOLD+Font.ITALIC:
 								fs = Font.BOLD;
 								break;
 							}
 							
 							Util.logn("fs: " + fs);
 							cf.setFontStyle(fs);
 							table.tableFormat.setFormatAt(cf, row, column, row, column);
 							cell.setCellFormat(cf);
 							table.repaint();
 							setIsDirty(true);
 						}else if (e.isControlDown() && e.getKeyCode() == 85){
 							Util.logn("user pressed CTRL+U");
 							Cell cell = (Cell)table.getValueAt(row, column);
 							
 							String old_value = (String) cell.getValue();
 							String new_value = "";
 							if (old_value.contains("<HTML><U>") && old_value.contains("</U></HTML>")){
 								new_value = old_value.replace("<HTML><U>", "");
 								new_value = new_value.replace("</U></HTML>", "");
 							}else{
 								new_value = "<HTML><U>" + old_value + "</U></HTML>";
 							}
 							
 							cell.setValue(new_value);
 							table.setValueAt(cell, row, column);
 							setIsDirty(true);
 						}
 
 					}
 
 					public void keyTyped(KeyEvent e) {
 						// TODO Auto-generated method stub
 
 					}
 
 				});
 				table.addMouseListener(new MouseListener(){
 
 					public void mouseClicked(MouseEvent e) {
 
 
 					}
 
 					public void mouseEntered(MouseEvent e) {
 
 
 					}
 
 					public void mouseExited(MouseEvent e) {
 
 
 					}
 
 					public void mousePressed(MouseEvent e) {
 
 						int column = table.columnAtPoint(e.getPoint());
 						int row = table.rowAtPoint(e.getPoint());
 						if(e.getButton()==3)
 						{
 							table.setColumnSelectionInterval(column, column);
 							table.setRowSelectionInterval(row, row);
 
 							maybeShowPopup(e);
 							//launchCellFormatPanel(table);
 						}else{
 							Util.logn("Cell clicked !!!");
 							
 							
 							//table.repaint();
 						}
 					}
 
 					public void mouseReleased(MouseEvent e) {
 
 
 					}
 
 				});
 
 				return scrollPane;
 			}
 
 			public Composite getLayoutAncestor() {
 				return parent;
 			}
 
 		};
 	}
 	
 	private void updateTableHeaderHighlights(int row, int column){
 		if (prev_selected_column != -1){
 			TableColumn tc = table.getColumnModel().getColumn(prev_selected_column);
 		    ((ColumnHeaderRenderer)tc.getHeaderRenderer()).selected = false;
 		}
 		
 		TableColumn tc = table.getColumnModel().getColumn(column);
 	    ((ColumnHeaderRenderer)tc.getHeaderRenderer()).selected = true;
 	    
 	    //table.rowHeader
 	    //table.rowHeader.repaint();
 	    
 		
 		prev_selected_column = column;
 		
 		((RowHeaderRenderer)table.rowHeader.getCellRenderer(row, 0)).row = row;
 		//((RowHeaderRenderer)table.rowHeader.getCellRenderer(row, 0)).column = column;
 		
 		table.getTableHeader().resizeAndRepaint();
 	    table.rowHeader.repaint();
 	}
 	
 	int prev_selected_column = -1;
 	int prev_selected_row = -1;
 
 	/**
 	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
 	 */
 	public void createPartControl(final Composite parent) {
 		createTable(parent);
 		table.setModel(tableModel);
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#doSave(IProgressMonitor)
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		try {
 			if (validateEditorInput(getEditorInput()) != null) {
 				if (getEditorInput().exists())
 					saveContents();
 				else
 					doSaveAs(MessageFormat.format(
 							"The original input ''{0}'' has been deleted.",
 							new Object[] { getEditorInput().getName() }));
 			} else {
 				doSaveAs();
 			}
 		} catch (CoreException e) {
 			monitor.setCanceled(true);
 			MessageDialog.openError(null, "Unable to Save Changes", e
 					.getLocalizedMessage());
 			return;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#isDirty()
 	 */
 	public boolean isDirty() {
 		return isDirty;
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
 	 */
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 
 
 
 	/**
 	 * Subclasses should persist the mini-spreadsheet in their
 	 * implementation-specific manner (in our case, directly in
 	 * file system or the workspace).
 	 */
 	public abstract boolean saveContents() throws CoreException;
 
 
 
 	/**
 	 * Respond to the Outline view's request to update the selection.
 	 * 
 	 * @see org.amanzi.splash.ui.outline.JRSSContentOutlinePage 
 	 */
 	public void selectionChanged(ISelection selection) {
 		tableViewer.setSelection(selection);
 	}
 
 
 
 	/**
 	 * Set the mini-spreadsheet's contents using the given <code>IEditorInput</code>
 	 * (subclasses can assume it has previously been validated
 	 * by <code>validateEditorInput</code>.
 	 */
 	public abstract void setContents(IEditorInput editorInput)
 	throws CoreException;
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		//table.setFocus();
 	}
 
 	/**
 	 * Persist the mini-spreadsheet as a workspace resource, allowing 
 	 * for the <b>Restore from Local History</b> options.
 	 */
 
 	/**
 	 * Flag the mini-spreadsheet as dirty,
 	 * enable the <b>Save</b> options, an update the editor's modification
 	 * indicator (*).
 	 */
	protected void setIsDirty(final boolean is_dirty) {
		Runnable r = new Runnable() {
			public void run() {
				isDirty = is_dirty;
				firePropertyChange(PROP_DIRTY);
 
			}
		};
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
		
			}
 
 	/**
 	 * Verify the editor input is valid (subclasses may transform it to another
 	 * implementator of <code>IEditorInput</code>), or null if the
 	 * input is unacceptable.
 	 */
 	public abstract IEditorInput validateEditorInput(IEditorInput editorInput);
 
 
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#doSaveAs()
 	 */
 	public void doSaveAs() {
 		doSaveAs("Save As");
 	}
 
 	private void doSaveAs(String message) {
 		try {
 			IEditorInput input = createNewInput(message);
 			if (input != null) {
 				setInput(input);
 				saveContents();
 				setPartName(input.getName());
 
 				firePropertyChange(PROP_INPUT);
 			}
 		} catch (CoreException e) {
 			MessageDialog.openError(null, "Unable to Save Changes", e
 					.getLocalizedMessage());
 			return;
 		}
 	}
 
 	/**
 	 * Return the column alignment.
 	 */
 	public int getDefaultAlignment() {
 		return defaultAlignment;
 	}
 
 
 
 
 
 
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
 	 */
 	public void init(IEditorSite site, IEditorInput editorInput)
 	throws PartInitException {
 
 		if (!editorInput.exists())
 			throw new PartInitException(editorInput.getName()
 					+ "does not exist.");
 
 		IEditorInput ei = validateEditorInput(editorInput);
 
 		// This message includes class names to help
 		// the programmer / reader; production code would instead
 		// log an error and provide a helpful, friendly message.
 		if (ei == null)
 			throw new PartInitException(MessageFormat.format("Invalid input.\n\n({0} is not a valid input for {1})",
 					new String[] {editorInput.getClass().getName(),
 					this.getClass().getName()
 			}));
 
 		try {
 
 			Util.logn("ei: " + ei.toString());
 			setInput(ei);
 			setContents(ei);
 			setSite(site);
 			setPartName(editorInput.getName());
 		} catch (CoreException e) {
 			throw new PartInitException(e.getMessage());
 		}
 	}
 
 
 	/**
 	 * create context menu for columns
 	 * @param rowIndex
 	 * @param columnIndex
 	 * @return
 	 */
 	private JPopupMenu createColumnContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = new JPopupMenu();
 
 		JMenuItem columnInsertMenu = new JMenuItem();
 		columnInsertMenu.setText("Insert Column");
 		columnInsertMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().insertColumn(columnIndex);
 				table.setColumnSelectionInterval(columnIndex, columnIndex);
 				table.setRowSelectionInterval(0, table.getRowCount() - 1);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(columnInsertMenu);
 
 		JMenuItem deleteColumnMenu = new JMenuItem();
 		deleteColumnMenu.setText("Delete Column");
 		deleteColumnMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().deleteColumn(columnIndex);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(deleteColumnMenu);
 
 		JMenuItem moveColumnLeftMenu = new JMenuItem();
 		moveColumnLeftMenu.setText("Move Column Left");
 		moveColumnLeftMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().moveColumnLeft(columnIndex);
 
 				table.setColumnSelectionInterval(columnIndex-1, columnIndex-1);
 				setIsDirty(true);
 			}
 
 
 		});
 		contextMenu.add(moveColumnLeftMenu);
 
 		JMenuItem moveColumnRightMenu = new JMenuItem();
 		moveColumnRightMenu.setText("Move Column Right");
 		moveColumnRightMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().moveColumnRight(columnIndex);
 				table.setColumnSelectionInterval(columnIndex+1, columnIndex+1);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(moveColumnRightMenu);
 
 
 		JMenuItem columnFormattingMenu = new JMenuItem();
 		columnFormattingMenu.setText("Column Formatting");
 		columnFormattingMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				launchCellFormatPanel(table);
 			}
 		});
 		contextMenu.add(columnFormattingMenu);
 
 		return contextMenu;
 	}
 	/**
 	 * create context menu for rows
 	 * @param rowIndex
 	 * @param columnIndex
 	 * @return
 	 */
 	private JPopupMenu createRowContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = new JPopupMenu();
 
 		JMenuItem rowInsertMenu = new JMenuItem();
 		rowInsertMenu.setText("Insert Row");
 		rowInsertMenu.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				int col = table.getSelectedColumn();
 				getTable().insertRow(rowIndex);
 				table.setColumnSelectionInterval(col, col);
 				table.setRowSelectionInterval(0, rowIndex);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(rowInsertMenu);
 
 		JMenuItem deleteRowMenu = new JMenuItem();
 		deleteRowMenu.setText("Delete Row");
 		deleteRowMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().deleteRow(rowIndex);
 				setIsDirty(true);
 			}
 		}); 
 		contextMenu.add(deleteRowMenu);
 
 		JMenuItem moveRowUpMenu = new JMenuItem();
 		moveRowUpMenu.setText("Move Row Up");
 		moveRowUpMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().moveRowUp(rowIndex);
 				table.setRowSelectionInterval(rowIndex-1, rowIndex-1);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(moveRowUpMenu);
 
 		JMenuItem moveRowDownMenu = new JMenuItem();
 		moveRowDownMenu.setText("Move Row Down");
 		moveRowDownMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTable().moveRowDown(rowIndex);
 				table.setRowSelectionInterval(rowIndex+1, rowIndex+1);
 				setIsDirty(true);
 			}
 		});
 		contextMenu.add(moveRowDownMenu);
 
 		JMenuItem rowFormattingMenu = new JMenuItem();
 		rowFormattingMenu.setText("Row Formatting");
 		rowFormattingMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				launchCellFormatPanel(table);
 			}
 		});
 		contextMenu.add(rowFormattingMenu);
 
 		return contextMenu;
 	}
 
 	/**
 	 * 
 	 * @param rowIndex
 	 * @param columnIndex
 	 * @return
 	 */
 	private JPopupMenu createContextMenu(final int rowIndex,
 			final int columnIndex) {
 		JPopupMenu contextMenu = new JPopupMenu();
 		
 		final Cell cell = (Cell)table.getValueAt(rowIndex, columnIndex);
 
 		JMenuItem cellCopyMenu = new JMenuItem();
 		cellCopyMenu.setText("Copy \t\t Ctrl+C");
 		cellCopyMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				table.copyCell(rowIndex,columnIndex);
 			}
 		});
 		contextMenu.add(cellCopyMenu);
 		
 		JMenuItem cellCutMenu = new JMenuItem();
 		cellCutMenu.setText("Cut \t\t Ctrl+X");
 		cellCutMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				table.cutCell(rowIndex,columnIndex);
 			}
 		});
 		contextMenu.add(cellCutMenu);
 		
 		JMenuItem cellPasteMenu = new JMenuItem();
 		cellPasteMenu.setText("Paste \t\t Ctrl+V");
 		cellPasteMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				table.pasteCell(rowIndex,columnIndex);
 			}
 		});
 		contextMenu.add(cellPasteMenu);
 		
 		JMenuItem cellFormattingMenu = new JMenuItem();
 		cellFormattingMenu.setText("Cell Formatting");
 		cellFormattingMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				launchCellFormatPanel(table);
 			}
 		});
 		contextMenu.add(cellFormattingMenu);
 		
 		//Lagutko, 16.06.2009, new menu items for integration with RDT
 		
 		JSeparator separator = new JSeparator();
 		contextMenu.add(separator);
 		
 		if (!cell.hasReference()) {
 			JMenuItem exportCellMenu = new JMenuItem();
 			exportCellMenu.setText("Export Cell");
 			exportCellMenu.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					exportCell(cell);
 				}
 			});
 			contextMenu.add(exportCellMenu);
 		}
 		else {		
 			JMenuItem openCellMenu = new JMenuItem();
 			openCellMenu.setText("Open Cell");
 			openCellMenu.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					openCell(cell);
 				}
 			});
 			contextMenu.add(openCellMenu);
 		
 			JMenuItem updateCellMenu = new JMenuItem();
 			updateCellMenu.setText("Update Cell");
 			updateCellMenu.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					updateCell(cell);
 				}
 			});
 			contextMenu.add(updateCellMenu);
 		}
 		
 		contextMenu.add(createRunMenu());
 		
 		return contextMenu;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 
 
 	private class HeaderMouseAdapter extends MouseAdapter
 	{
 
 		public void mousePressed(MouseEvent e) {
 
 			if(e.getButton()==3)
 			{
 				int col = -1;
 				int row = -1;
 				if (e.getSource() instanceof JTableHeader)
 				{
 					col = table.columnAtPoint(e.getPoint());
 				}
 				if (e.getSource() instanceof JTable)
 				{
 					row = table.rowAtPoint(e.getPoint());
 				}
 				if (col >= 0)
 				{
 					maybeShowColumnPopup(e);
 				}
 				else if (row >= 0)
 				{
 					maybeShowRowPopup(e);
 				}
 
 				int rowCount = table.getRowCount();
 				int colCount = table.getColumnCount();
 
 				table.setRowSelectionInterval(0, rowCount - 1);
 
 				if (col >= 0) // select column
 				{
 					table.setColumnSelectionInterval(col, col);
 					table.setRowSelectionInterval(0, rowCount - 1);
 				}
 				else if (row >= 0) // select row
 				{
 					table.setColumnSelectionInterval(0, colCount - 1);
 					table.setRowSelectionInterval(row, row);
 				}
 				else // select all
 				{
 					table.setColumnSelectionInterval(0, colCount - 1);
 					table.setRowSelectionInterval(0, rowCount - 1);
 				}
 			}
 		}
 
 		public void mouseClicked(MouseEvent e)
 		{
 
 			int col = -1;
 			int row = -1;
 			if (e.getSource() instanceof JTableHeader)
 			{
 				col = table.columnAtPoint(e.getPoint());
 			}
 			if (e.getSource() instanceof JTable)
 			{
 				row = table.rowAtPoint(e.getPoint());
 			}
 
 			int rowCount = table.getRowCount();
 			int colCount = table.getColumnCount();
 
 			table.setRowSelectionInterval(0, rowCount - 1);
 
 			if (col >= 0) // select column
 			{
 				table.setColumnSelectionInterval(col, col);
 				table.setRowSelectionInterval(0, rowCount - 1);
 			}
 			else if (row >= 0) // select row
 			{
 				table.setColumnSelectionInterval(0, colCount - 1);
 				table.setRowSelectionInterval(row, row);
 			}
 			else // select all
 			{
 				table.setColumnSelectionInterval(0, colCount - 1);
 				table.setRowSelectionInterval(0, rowCount - 1);
 			}
 
 		}
 	}
 
 
 
 	private class SelectionAdapter implements ListSelectionListener
 	{
 		public void valueChanged(ListSelectionEvent e)
 		{
 //			int c = listenerList.getListenerCount(SpreadsheetSelectionListener.class);
 //			if (c > 0)
 //			{
 //			//fireSelectionChanged();
 //			}
 		}
 	}
 
 
 
 	public CellFormat getCellFormat() {
 		return cellFormat;
 	}
 
 	public void setCellFormat(CellFormat cellFormat) {
 		this.cellFormat = cellFormat;
 	}
 	
 	/**
 	 * Creates a menu for Launching
 	 * 
 	 * @return created menu
 	 * @author Lagutko_N
 	 */
 	
 	private JMenu createRunMenu() {
 		JMenu runMenu = new JMenu("Run as...");
 		
 		try {		
 			ILaunchConfiguration[] launchConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
 		
 			for (final ILaunchConfiguration configuration : launchConfigurations) {
 				JMenuItem menuItem = new JMenuItem(configuration.getName());
 			
 				menuItem.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {						
 						launch(configuration);
 					}
 				});
 				
 				runMenu.add(menuItem);
 			}		
 			
 			if (launchConfigurations.length > 0) {
 				runMenu.add(new JSeparator());
 			}
 		}
 		catch (CoreException e) {
 			DebugPlugin.log(e);
 		}		
 		JMenuItem openRunDialog = new JMenuItem("Open run dialog...");
 		openRunDialog.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openLaunchDialog();
 			}
 		});
 		
 		runMenu.add(openRunDialog);
 		
 		return runMenu;
 	}
 	
 	/**
 	 * Launches chosen configuration
 	 * 
 	 * @param configuration launch configuration
 	 * @author Lagutko_N
 	 */
 	
 	private void launch(final ILaunchConfiguration configuration) {
 		//get Cell and Display
 		final Display display = swingControl.getDisplay();
 
 		//run ExportScriptWizard
 		ActionUtil.getInstance(display).runTask( new Runnable() {
 			public void run() {
 				DebugUITools.launch(configuration, "run");
 			}
 		});
 	}
 	
 	/**
 	 * Opens 'Open run dialog...' dialog
 	 * 
 	 * @author Lagutko_N
 	 */
 	
 	private void openLaunchDialog() {
 		//get Cell and Display
 		final Display display = swingControl.getDisplay();
 
 		//run ExportScriptWizard
 		ActionUtil.getInstance(display).runTask( new Runnable() {
 			public void run() {
 				Shell shell = display.getActiveShell();
 				StructuredSelection selection = StructuredSelection.EMPTY;
 				DebugUITools.openLaunchConfigurationDialogOnGroup(shell, selection, "org.eclipse.debug.ui.launchGroup.run");
 			}
 		});
 	}
 }
 
