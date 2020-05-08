 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.editor.coredata.table;
 
 import java.util.Vector;
 
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableColumn;
 
 import dsagenesis.core.model.sql.AbstractSQLTableModel;
 import dsagenesis.editor.coredata.CoreEditorFrame;
 import dsagenesis.editor.coredata.table.cell.BasicCellRenderer;
 import dsagenesis.editor.coredata.table.cell.CheckBoxCellRenderer;
 import dsagenesis.editor.coredata.table.cell.CommitButtonCell;
 import dsagenesis.editor.coredata.table.cell.IntegerCellRenderer;
 
 /**
  * CoreEditorTable
  * 
  * JTable implementation that is filled with AbstractSQLTabelModel's.
  */
 public class CoreEditorTable 
 		extends JTable 
 		implements TableModelListener
 {
 	// ============================================================================
 	//  Constants
 	// ============================================================================
 		
 	private static final long serialVersionUID = 1L;
 
 	
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 		
 	private AbstractSQLTableModel sqlTable;
 	
 	private CoreEditorFrame jframe;
 	
 	private CommitButtonCell btnCommit;
 	
 	// ============================================================================
 	//  Constructors
 	// ============================================================================
 			
 	/**
 	 * Constructor.
 	 * 
 	 * @param sqlTable
 	 */
 	public CoreEditorTable(CoreEditorFrame frame, AbstractSQLTableModel sqlTable)
 	{
 		super();
 		
 		this.sqlTable = sqlTable;
 		this.jframe = frame;
 		setup();
 	}
 	
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 	
 	/**
 	 * setup
 	 * 
 	 * the defaults.
 	 */
 	private void setup()
 	{
 		// this is better for larger tables
 		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
 		this.setName(sqlTable.getDBTableName());
 				
 		this.setAutoCreateRowSorter(true);
 		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		this.getTableHeader().setReorderingAllowed(false);
 		this.setDefaultRenderer(String.class, new BasicCellRenderer());
 		this.setDefaultRenderer(Boolean.class, new CheckBoxCellRenderer());
 		this.setDefaultRenderer(Integer.class, new IntegerCellRenderer());
 		this.setDefaultRenderer(Object.class, new BasicCellRenderer());
 		
 		this.setRowHeight(22);
 	}
 	
 	/**
 	 * containsUncommitedData
 	 * 
 	 * @return
 	 */
 	public boolean containsUncommitedData()
 	{
 		if( btnCommit == null )
 			return false;
 		
 		if( btnCommit.getChangedRows().contains(true) )
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * loadData
 	 * 
 	 * updates the whole table for the DB
 	 */
 	public void loadData()
 	{
 		if( sqlTable == null )
 			return;
 		
		Vector<Vector<Object>> data = sqlTable.queryListAsVector();
		Vector<String> labels = sqlTable.getColumnLabels();
		Vector<Class<?>> classes = sqlTable.getTableColumnClasses();
 		
 		//if editable fill up cells for commit button
 		if( sqlTable.isEditable() )
 		{
 			for( int i=0; i < data.size(); i++ )
 				data.elementAt(i).addElement(null);
 			
 			labels.addElement("");
 			classes.addElement(Object.class);
 		}
 		
 		CoreEditorTableModel cetm = new CoreEditorTableModel(
 				data, 
 				labels,
 				classes,
 				this
 			);
 		cetm.setReadOnly(!sqlTable.isEditable());
 		this.setModel(cetm);
 		
 		this.sqlTable.setupJTableColumnModels(jframe, this);
 		
 		if( sqlTable.isEditable() )
 		{
 			// setup commit button and add listener
 			TableColumn currColumn = this.getColumnModel().getColumn(this.getColumnCount()-1);
 			currColumn.setMaxWidth(25);
 			currColumn.setMinWidth(25);
 			this.btnCommit = new CommitButtonCell(this);
 			this.btnCommit.initRows(data.size());
 			
 			currColumn.setCellEditor(this.btnCommit);
 			currColumn.setCellRenderer(this.btnCommit);
 			
 			// TODO add global commit
 			/*
 			currColumn = this.getTableHeader().getColumnModel().getColumn(this.getColumnCount()-1);
 			currColumn.setCellRenderer( );
 			*/
 			
 			// add listener
 			this.getModel().addTableModelListener(this);
 			this.getModel().addTableModelListener(this.jframe);
 		}
 	}
 	
 	/**
 	 * commitRow
 	 * 
 	 * commit changes of the row.
 	 * 
 	 * @param row
 	 */
 	public void commitRow(int row)
 	{
 System.out.println("CoreEditorTable commit "+row);
 		
 		// TODO
 
 		
 	}
 
 	/**
 	 * overridden for handling the commit button updates
 	 */
 	@Override
 	public void tableChanged(TableModelEvent e)
 	{
 		// default handling
 		super.tableChanged(e);
 		
 		// now we check if the commit button needs to be enabled
 		int row = e.getFirstRow();
 		int column = e.getColumn();
         
 		// fail saves
 		// that the button is not enabled unnecessarily
 		if( row < 0 || column < 0 )
 			return;
 		
 		if( e.getType() != TableModelEvent.UPDATE )
 			return;
 		
 		if( this.getCellRenderer(row, column) != null
         		&&  this.getCellRenderer(row, column) instanceof CommitButtonCell )
         	return;
         
         if( this.getCellEditor() != null
         		&&  this.getCellEditor() instanceof CommitButtonCell )
         	return;
         
         // now something changed enable button
         this.btnCommit.setEnabled(row, true);
         
 		// to update the icon
 		this.repaint();
 	}
 }
