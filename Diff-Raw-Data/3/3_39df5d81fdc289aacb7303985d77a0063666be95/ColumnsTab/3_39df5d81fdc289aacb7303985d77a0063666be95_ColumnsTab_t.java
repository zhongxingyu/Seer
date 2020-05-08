 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.table.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
 import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
 import net.sf.okapi.common.ui.abstracteditor.TableAdapter;
 import net.sf.okapi.filters.table.base.Parameters;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * 
  * 
  * @version 0.1, 20.06.2009
  */
 
 public class ColumnsTab extends Composite implements IDialogPage {
 	
 	private double[] columnPoints = {1.6, 2, 2, 2, 3, 1, 1};
 	
 	private Table table;
 	private TableColumn tblclmnColumn;
 	private TableColumn tblclmnType;
 	private TableColumn tblclmnSource;
 	private TableColumn tblclmnSuffix;
 	private TableColumn tblclmnLanguage;
 	private TableColumn tblclmnStart;
 	private TableColumn tblclmnEnd;
 	private Group extr;
 	private Group gnum;
 	private Group colDefs;
 	private Button defs;
 	private Button all;
 	private Button fix;
 	private Button names;
 	private Button vals;
 	private Spinner num;
 	private Button btnAdd;
 	private Button btnRemove;
 	private Button btnModify;
 	private Composite buttons;
 	private Label label_1;
 	private TableAdapter adapter;	
 
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public ColumnsTab(final Composite parent, int style) {
 		
 		super(parent, style);
 		setLayout(new GridLayout(2, false));
 		
 		extr = new Group(this, SWT.NONE);
 		extr.setLayout(new GridLayout(1, false));
 		extr.setData("name", "extr");
 		extr.setText("Extraction mode");
 		extr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 		
 		all = new Button(extr, SWT.RADIO);
 		all.setData("name", "all");
 		all.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		all.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		all.setText("Extract from all columns (create separate text units)");
 		
 		defs = new Button(extr, SWT.RADIO);
 		defs.setData("name", "defs");
 		defs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		defs.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		defs.setText("Extract by column definitions");
 		
 		gnum = new Group(this, SWT.NONE);
 		gnum.setLayout(new GridLayout(4, false));
 		gnum.setData("name", "gnum");
 		gnum.setText("Number of columns");
 		gnum.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 		
 		vals = new Button(gnum, SWT.RADIO);
 		vals.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 		vals.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		vals.setText("Defined by values (may vary in different rows)");
 		
 		names = new Button(gnum, SWT.RADIO);
 		names.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 		names.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		names.setText("Defined by column names");
 		
 		fix = new Button(gnum, SWT.RADIO);
 		fix.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		fix.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		fix.setText("Fixed number of columns");
 		new Label(gnum, SWT.NONE);
 		new Label(gnum, SWT.NONE);
 		
 		num = new Spinner(gnum, SWT.BORDER);
 		num.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		num.setMinimum(1);
 		
 		colDefs = new Group(this, SWT.NONE);
 		colDefs.setData("name", "colDefs");
 		colDefs.setText("Column definitions");
 		colDefs.setLayout(new GridLayout(2, false));
 		colDefs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		
 		table = new Table(colDefs, SWT.BORDER | SWT.FULL_SELECTION);
 		
 		table.addMouseListener(new MouseAdapter() {
 			public void mouseDoubleClick(MouseEvent e) {
 								
 				addModifyRow(table.getItem(new Point(e.x, e.y)));
 			}
 		});
 		
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				interop(e.widget);
 			}
 		});
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		
 		tblclmnColumn = new TableColumn(table, SWT.RIGHT);
 		tblclmnColumn.setWidth(72);
 		tblclmnColumn.setText("Column #");
 		
 		tblclmnType = new TableColumn(table, SWT.NONE);
 		tblclmnType.setWidth(93);
 		tblclmnType.setText("Type");
 		
 		tblclmnSource = new TableColumn(table, SWT.RIGHT);
 		tblclmnSource.setWidth(95);
 		tblclmnSource.setText("Source column");
 		
 		tblclmnLanguage = new TableColumn(table, SWT.NONE);
 		tblclmnLanguage.setWidth(84);
 		tblclmnLanguage.setText("Language");
 		
 		tblclmnSuffix = new TableColumn(table, SWT.NONE);
 		tblclmnSuffix.setWidth(116);
 		tblclmnSuffix.setText("ID suffix");
 		
 		tblclmnStart = new TableColumn(table, SWT.RIGHT);
 		tblclmnStart.setWidth(47);
 		tblclmnStart.setText("Start");
 		
 		tblclmnEnd = new TableColumn(table, SWT.RIGHT);
 		tblclmnEnd.setWidth(47);
 		tblclmnEnd.setText("End");
 		
 		adapter = new TableAdapter(table);
 		adapter.setRelColumnWidths(columnPoints);
 		
 		buttons = new Composite(colDefs, SWT.NONE);
 		buttons.setData("name", "buttons");
 		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		buttons.setLayout(new GridLayout(1, false));
 		
 		btnAdd = new Button(buttons, SWT.NONE);
 		
 		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnAdd.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 							
 				addModifyRow(null);
 			}
 		});
 		btnAdd.setText("Add...");
 		
 		btnModify = new Button(buttons, SWT.NONE);
 		btnModify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnModify.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				addModifyRow(table.getItem(table.getSelectionIndex()));
 			}
 		});
 		btnModify.setText("Modify...");
 		
 		btnRemove = new Button(buttons, SWT.NONE);
 		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnRemove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				adapter.removeSelected();
 				interop(e.widget);
 			}
 		});
 		btnRemove.setText("Remove");
 		
 		label_1 = new Label(buttons, SWT.NONE);
 		label_1.setText("                          ");
 		new Label(colDefs, SWT.NONE);
 		new Label(colDefs, SWT.NONE);
 		new Label(colDefs, SWT.NONE);
 	}
 
 	protected void addModifyRow(TableItem item) {
 		
 		if (item == null) { // Add new item			
 			adapter.unselect();
 			
 			Object res = SWTUtil.inputQuery(AddModifyColumnDefPage.class, getShell(), "Add column definition", 
 					new String[] {Util.intToStr(SWTUtil.getColumnMaxValue(table, 0) + 1), "Source", "", "", "", "0", "0"}, 
 					null); 
 			
 			if (res != null)				
 				adapter.addModifyRow((String []) res, 1, TableAdapter.DUPLICATE_REPLACE);
 			else
 				adapter.restoreSelection();
 		}
 		else {
 		
 			Object res = SWTUtil.inputQuery(AddModifyColumnDefPage.class, getShell(), "Modify column definition", 
 					SWTUtil.getText(item),
 					null);
 			
 			if (res != null)							
 				adapter.addModifyRow(item, (String []) res, 1, TableAdapter.DUPLICATE_REPLACE);
 		}
 		
 		adapter.sort(1, true);
 		interop(table);  // Selection changes
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents sub-classing of SWT components
 	}
 
 	public boolean canClose(boolean isOK) {
 		
 		return true;
 	}
 
 	public void interop(Widget speaker) {
 		
 		SWTUtil.setAllEnabled(colDefs, defs.getSelection());
 		SWTUtil.setAllEnabled(buttons, defs.getSelection());
 		num.setEnabled(fix.getSelection());
 		
 		btnModify.setEnabled(buttons.getEnabled() && table.getItemCount() > 0 && table.getSelectionIndex() != -1);
 		btnRemove.setEnabled(btnModify.getEnabled());			
 	}
 
 	public boolean load(Object data) {
 		
 		// Common part
 		
 		if (data instanceof net.sf.okapi.filters.table.base.Parameters) {
 			
 			net.sf.okapi.filters.table.base.Parameters params =
 				(net.sf.okapi.filters.table.base.Parameters) data;
 		
 			num.setSelection(params.numColumns);
 			
 			//------------------
 			SWTUtil.unselectAll(gnum);
 			
 			if (params.detectColumnsMode == Parameters.DETECT_COLUMNS_FIXED_NUMBER)
 				fix.setSelection(true);
 			
 			else if (params.detectColumnsMode == Parameters.DETECT_COLUMNS_COL_NAMES)
 				names.setSelection(true);
 			
 			else
 				vals.setSelection(true);
 			
 			//------------------
 			SWTUtil.unselectAll(extr);
 			
 			if (params.sendColumnsMode == Parameters.SEND_COLUMNS_LISTED) {
 				
 				defs.setSelection(true);
 				all.setSelection(false);
 			}
 			
 			else if (params.sendColumnsMode == Parameters.SEND_COLUMNS_ALL) {
 				
 				defs.setSelection(false);
 				all.setSelection(true);
 			}
 			
 			else {
 				
 				defs.setSelection(false);
 				all.setSelection(false);
 			}
 		
 			// -----------------
 			
 			//adapter.clear();
 			
 			adapter.addRows(params.sourceColumns, 1);
 			adapter.addRows(params.targetColumns, 1);
 			adapter.addRows(params.targetSourceRefs, 1);
 			adapter.addRows(params.sourceIdColumns, 1);
 			adapter.addRows(params.sourceIdSourceRefs, 1);
 			adapter.addRows(params.commentColumns, 1);
 			adapter.addRows(params.commentSourceRefs, 1);
 			
 			if (params.recordIdColumn > 0)
 				adapter.addRow(params.recordIdColumn, 1);
 			
 			adapter.sort(1, true);
 						
 			List<String> sourceColumns = ListUtil.stringAsList(params.sourceColumns);	
 			List<String> sourceIdSuffixes = ListUtil.stringAsList(params.sourceIdSuffixes);
 			List<String> targetColumns = ListUtil.stringAsList(params.targetColumns);
 			List<String> targetLanguages = ListUtil.stringAsList(params.targetLanguages);
 			List<String> targetSourceRefs = ListUtil.stringAsList(params.targetSourceRefs);			
 			List<String> sourceIdColumns = ListUtil.stringAsList(params.sourceIdColumns);
 			List<String> sourceIdSourceRefs = ListUtil.stringAsList(params.sourceIdSourceRefs);
 			List<String> commentColumns = ListUtil.stringAsList(params.commentColumns);
 			List<String> commentSourceRefs = ListUtil.stringAsList(params.commentSourceRefs);
 					
 			// Types
 			for (int i = 0; i < sourceColumns.size(); i++)
 				adapter.setValue(adapter.findValue(sourceColumns.get(i), 1), 2, AddModifyColumnDefPage.TYPE_SOURCE);
 			
 			for (int i = 0; i < targetColumns.size(); i++)
 				adapter.setValue(adapter.findValue(targetColumns.get(i), 1), 2, AddModifyColumnDefPage.TYPE_TARGET);
 			
 			for (int i = 0; i < sourceIdColumns.size(); i++)
 				adapter.setValue(adapter.findValue(sourceIdColumns.get(i), 1), 2, AddModifyColumnDefPage.TYPE_SOURCE_ID); 
 			
 			for (int i = 0; i < commentColumns.size(); i++)
 				adapter.setValue(adapter.findValue(commentColumns.get(i), 1), 2, AddModifyColumnDefPage.TYPE_COMMENT);
 			
 			adapter.setValue(adapter.findValue(Util.intToStr(params.recordIdColumn), 1), 2, AddModifyColumnDefPage.TYPE_RECORD_ID);
 			
 			// Refs
 			for (int i = 0; i < sourceIdSuffixes.size(); i++)
 				adapter.setValue(adapter.findValue(sourceColumns.get(i), 1), 5, sourceIdSuffixes.get(i));
 			
 			for (int i = 0; i < commentSourceRefs.size(); i++)
 				adapter.setValue(adapter.findValue(commentColumns.get(i), 1), 3, commentSourceRefs.get(i));
 			
 			for (int i = 0; i < sourceIdSourceRefs.size(); i++)
 				adapter.setValue(adapter.findValue(sourceIdColumns.get(i), 1), 3, sourceIdSourceRefs.get(i));
 			
 			for (int i = 0; i < targetSourceRefs.size(); i++)
 				adapter.setValue(adapter.findValue(targetColumns.get(i), 1), 3, targetSourceRefs.get(i));
 			
 			for (int i = 0; i < targetLanguages.size(); i++)
 				adapter.setValue(adapter.findValue(targetColumns.get(i), 1), 4, targetLanguages.get(i));
 		}
 
 		if (data instanceof net.sf.okapi.filters.table.fwc.Parameters) {
 			
 			net.sf.okapi.filters.table.fwc.Parameters params =
 				(net.sf.okapi.filters.table.fwc.Parameters) data;
 						
 			List<String> columnStartPositions = ListUtil.stringAsList(params.columnStartPositions);	
 			List<String> columnEndPositions = ListUtil.stringAsList(params.columnEndPositions);
 
 			for (int i = 0; i < Math.min(columnStartPositions.size(), columnEndPositions.size()); i++) {
 				
 				adapter.setValue(i + 1, 6, columnStartPositions.get(i)); // the table rows are already created here  
 				adapter.setValue(i + 1, 7, columnEndPositions.get(i));
 			}
 		}
 		
 		return true;
 	}
 
 	public boolean save(Object data) {
 
 // Common part
 		
 		if (data instanceof net.sf.okapi.filters.table.base.Parameters) {
 			
 			net.sf.okapi.filters.table.base.Parameters params =
 				(net.sf.okapi.filters.table.base.Parameters) data;
 		
 			params.numColumns = num.getSelection();
 		
 			// -----------------
 			if (fix.getSelection())
 				params.detectColumnsMode = Parameters.DETECT_COLUMNS_FIXED_NUMBER;
 			
 			else if (names.getSelection())
 				params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
			
			else 
				params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
 		
 			// -----------------
 						
 			if (all.getEnabled() && !defs.getSelection())
 				params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
 			
 			else if ((defs.getSelection() && defs.getEnabled()))
 				params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
 						
 			// -----------------
 			List<String> sourceColumns = new ArrayList<String>();	
 			List<String> sourceIdSuffixes = new ArrayList<String>();
 			List<String> targetColumns = new ArrayList<String>();
 			List<String> targetLanguages = new ArrayList<String>();
 			List<String> targetSourceRefs = new ArrayList<String>();			
 			List<String> sourceIdColumns = new ArrayList<String>();
 			List<String> sourceIdSourceRefs = new ArrayList<String>();
 			List<String> commentColumns = new ArrayList<String>();
 			List<String> commentSourceRefs = new ArrayList<String>();
 			
 			for (int i = 1; i <= adapter.getNumRows(); i++) {
 			
 				if (AddModifyColumnDefPage.TYPE_SOURCE.equalsIgnoreCase(adapter.getValue(i, 2))) {
 					
 					sourceColumns.add(adapter.getValue(i, 1));
 					sourceIdSuffixes.add(adapter.getValue(i, 5));
 				}
 					
 				else if (AddModifyColumnDefPage.TYPE_SOURCE_ID.equalsIgnoreCase(adapter.getValue(i, 2))) {
 					
 					sourceIdColumns.add(adapter.getValue(i, 1));
 					sourceIdSourceRefs.add(adapter.getValue(i, 3));
 				}
 				
 				else if (AddModifyColumnDefPage.TYPE_TARGET.equalsIgnoreCase(adapter.getValue(i, 2))) {
 				
 					targetColumns.add(adapter.getValue(i, 1));
 					targetSourceRefs.add(adapter.getValue(i, 3));
 					targetLanguages.add(adapter.getValue(i, 4));
 				}
 				
 				else if (AddModifyColumnDefPage.TYPE_COMMENT.equalsIgnoreCase(adapter.getValue(i, 2))) {
 					
 					commentColumns.add(adapter.getValue(i, 1));
 					commentSourceRefs.add(adapter.getValue(i, 3));
 				}
 					
 				else if (AddModifyColumnDefPage.TYPE_RECORD_ID.equalsIgnoreCase(adapter.getValue(i, 2)))
 					params.recordIdColumn = Util.strToInt(adapter.getValue(i, 1), 0);					
 			}
 			
 			params.sourceColumns = ListUtil.listAsString(sourceColumns);
 			params.sourceIdSuffixes = ListUtil.listAsString(sourceIdSuffixes);
 			params.targetColumns = ListUtil.listAsString(targetColumns);
 			params.targetLanguages = ListUtil.listAsString(targetLanguages);
 			params.targetSourceRefs = ListUtil.listAsString(targetSourceRefs);
 			params.sourceIdColumns = ListUtil.listAsString(sourceIdColumns);
 			params.sourceIdSourceRefs = ListUtil.listAsString(sourceIdSourceRefs);
 			params.commentColumns = ListUtil.listAsString(commentColumns);
 			params.commentSourceRefs = ListUtil.listAsString(commentSourceRefs);
 		}
 		
 		if (data instanceof net.sf.okapi.filters.table.fwc.Parameters) {
 			
 			net.sf.okapi.filters.table.fwc.Parameters params =
 				(net.sf.okapi.filters.table.fwc.Parameters) data;
 		
 			List<String> columnStartPositions = new ArrayList<String>();
 			List<String> columnEndPositions = new ArrayList<String>();
 			
 			for (int i = 1; i <= adapter.getNumRows(); i++) {
 				
 				columnStartPositions.add(adapter.getValue(i, 6));
 				columnEndPositions.add(adapter.getValue(i, 7));
 			}
 			
 			params.columnStartPositions = ListUtil.listAsString(columnStartPositions);
 			params.columnEndPositions = ListUtil.listAsString(columnEndPositions);			
 		}
 		
 		return true;
 	}
 }
