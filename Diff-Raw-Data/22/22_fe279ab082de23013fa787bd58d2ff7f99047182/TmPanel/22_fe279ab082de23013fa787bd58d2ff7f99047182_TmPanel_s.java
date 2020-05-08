 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.applications.olifant;
 
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import net.sf.okapi.common.observer.IObservable;
 import net.sf.okapi.common.observer.IObserver;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.lib.tmdb.DbUtil;
 import net.sf.okapi.lib.tmdb.ITm;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 class TmPanel extends Composite implements IObserver {
 
 	private final static int KEYCOLUMNWIDTH = 90;
 
 	private final static int SAVE_FLAG = 0x01;
 	private final static int SAVE_SOURCE = 0x02;
 	private final static int SAVE_TARGET = 0x04;
 	//private final static int SAVE_THIRD = 0x08;
 
 	private CTabItem tabItem;
 	private final SashForm sashMain;
 	private final EditorPanel editPanel;
 	private final Table table;
 	private final LogPanel logPanel;
 	private ITm tm;
 	private int currentEntry;
 	private boolean needSave = false;
 	private boolean wasModified = false;
 	private StatusBar statusBar;
 	private Thread workerThread;
 	private MainForm mainForm;
 	private int srcCol; // Column in the table that holds the source text, use -1 for none, 0-based, 1=SegKey+Flag
 	private int trgCol; // Column in the table that holds the target text, use -1 for none, 0-based, 1=SegKey+Flag
 	private TMOptions opt;
 
 	public TmPanel (MainForm mainForm,
 		Composite parent,
 		int flags,
 		ITm tm,
 		StatusBar statusBar)
 	{
 		super(parent, flags);
 		this.mainForm = mainForm;
 		this.tm = tm;
 		this.statusBar = statusBar;
 		
 		opt = new TMOptions();
 		
 		srcCol = -1;
 		trgCol = -1;
 		
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		setLayout(layout);
 		setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		// Create the two main parts of the UI
 		sashMain = new SashForm(this, SWT.VERTICAL);
 		sashMain.setLayout(new GridLayout(1, false));
 		sashMain.setLayoutData(new GridData(GridData.FILL_BOTH));
 		sashMain.setSashWidth(4);
 		
 		// Edit panels
 		editPanel = new EditorPanel(sashMain, SWT.VERTICAL);
 		editPanel.clear();
 
 		// Table
 		table = new Table(sashMain, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK | SWT.V_SCROLL);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		table.addControlListener(new ControlAdapter() {
 		    public void controlResized(ControlEvent e) {
 		    	int count = table.getColumnCount()-1; // Exclude Key column
 		    	if ( count < 1 ) return;
 		    	try {
 		    		table.setRedraw(false);
 		    		Rectangle rect = table.getClientArea();
 		    		int keyColWidth = table.getColumn(0).getWidth();
 		    		int part = (int)((rect.width-keyColWidth) / count);
 		    		int remainder = (int)((rect.width-keyColWidth) % count);
 		    		for ( int i=1; i<table.getColumnCount(); i++ ) {
 		    			table.getColumn(i).setWidth(part);
 		    		}
 		    		table.getColumn(1).setWidth(table.getColumn(1).getWidth()+remainder);
 		    	}
 		    	finally {
 		    		table.setRedraw(true);
 		    	}
 		    }
 		});
 		
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if ( event.detail == SWT.CHECK ) {
 					// Do not force the selection: table.setSelection((TableItem)event.item);
 					TableItem ti = (TableItem)event.item;
 					ti.setData((Integer)ti.getData() | SAVE_FLAG); // Entry has been changed
 					needSave = true;
 				}
 				else {
 					saveEntry();
 					updateCurrentEntry();
 				}
             }
 		});
 
 		table.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if ( e.character == ' ' ) { // Changes the flag with the space-bar
 					TableItem si = table.getItem(table.getSelectionIndex());
 					for ( TableItem ti : table.getSelection() ) {
 						if ( ti == si ) continue; // Skip focused item because it will get set by SelectionAdapter()
 						ti.setChecked(!ti.getChecked());
 						ti.setData((Integer)ti.getData() | SAVE_FLAG); // Entry has been changed
 						needSave = true;
 					}
 				}
 				else { 
 					checkPage(e.keyCode, e.stateMask);
 				}
 			}
 		});
 		
 		// Create the first column (always present)
 		TableColumn col = new TableColumn(table, SWT.NONE);
 		col.setText("Flag/SegKey");
 		col.setWidth(KEYCOLUMNWIDTH);
 		
 		logPanel = new LogPanel(sashMain, 0);
 		
 		sashMain.setWeights(new int[]{3, 7, 2});
 	}
 
 	ITm getTm () {
 		return tm;
 	}
 	
 	void setTabItem (CTabItem tabItem) {
 		this.tabItem = tabItem;
 	}
 	
 	CTabItem getTabItem () {
 		return tabItem;
 	}
 	
 	LogPanel getLog () {
 		return logPanel;
 	}
 	
 	boolean canClose () {
 		try {
 			if ( hasRunningThread() ) {
 				return false;
 			}
 			saveEntryAndModificationsIfNeeded();
 			return true;
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getMessage(), null);
 			return false;
 		}
 	}
 
 	void saveEntryAndModificationsIfNeeded () {
 		saveEntry();
 		saveModificationsIfNeeded();
 	}
 
 	boolean hasRunningThread () {
 		return (( workerThread != null ) && workerThread.isAlive() );
 	}
 
 	void setTmOptions (TMOptions opt) {
 		this.opt = opt;
 	}
 	
 	void editColumns () {
 		try {
 			saveEntryAndModificationsIfNeeded();
 			ArrayList<String> prevList = opt.getVisibleFields();
 			ColumnsForm dlg = new ColumnsForm(getShell(), tm, prevList);
 			
 			Object[] res = dlg.showDialog();
 			if (( res[0] == null ) && ( !(Boolean)res[1] )) {
 				return; // Nothing to do
 			}
 			if ( res[0] == null ) {
 				opt.setVisibleFields(prevList);
 			}
 			else {
 				@SuppressWarnings("unchecked")
 				ArrayList<String> newList = (ArrayList<String>)res[0];
 				opt.setVisibleFields(newList);
 			}
 
 			//TODO Set the columns with the source and target
 			srcCol = -1;
 			trgCol = -1;
 			int n = 1;
 			for ( String fn : opt.getVisibleFields() ) {
 				if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
 					if ( srcCol == -1 ) srcCol = n;
 					else if ( trgCol == -1 ) trgCol = n;
 				}
 				n++;
 			}
 			updateVisibleFields();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), "Error editing columns document.\n"+e.getMessage(), null);
 		}
 	}
 
 	void editLocales () {
 		try {
 			saveEntryAndModificationsIfNeeded();
 			LocalesForm dlg = new LocalesForm(getShell(), tm);
 			if ( !dlg.showDialog() ) {
 				// No change was made, we can skip the re-drawing
 				return;
 			}
 
 			// Ensure columns of delete locales are removed from the display
 			java.util.List<String> available = tm.getAvailableFields();
 			// Update the list of visible fields
 			// by removing any fields not available anymore
 			ArrayList<String> visible = opt.getVisibleFields();
 			visible.retainAll(available);
 			opt.setVisibleFields(visible);
 			
 			//TODO Set the columns with the source and target
 			srcCol = -1;
 			trgCol = -1;
 			int n = 1;
 			for ( String fn : opt.getVisibleFields() ) {
 				if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
 					if ( srcCol == -1 ) srcCol = n;
 					else if ( trgCol == -1 ) trgCol = n;
 				}
 				n++;
 			}
 			updateVisibleFields();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), "Error editing columns document.\n"+e.getMessage(), null);
 		}
 	}
 
 	void resetTmDisplay () {
 		srcCol = -1;
 		trgCol = -1;
 		// By default: all and only text fields are visible
 		opt.setVisibleFields(new ArrayList<String>());
 		
 		int n = 1; // SEGKEY and FLAG are there by default
 		for ( String fn : tm.getAvailableFields() ) {
 			if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
 				opt.getVisibleFields().add(fn);
 				if ( srcCol == -1 ) srcCol = n;
 				else if ( trgCol == -1 ) trgCol = n;
 				n++;
 			}
 		}
 
 		tm.setPageSize(opt.getPageSize());
 		// Update the visible fields
 		updateVisibleFields();
 	}
 	
 	private void updateVisibleFields () {
 		try {
 			table.setRedraw(false);
 			// Indicate to the TM back-end which fields the UI wants
 			tm.setRecordFields(opt.getVisibleFields());
 			// Remove all variable columns
 			int n;
 			while ( (n = table.getColumnCount()) > 1 ) {
 				table.getColumns()[n-1].dispose();
 			}
 			// Add the new ones
 			for ( String fn : opt.getVisibleFields() ) {
 				TableColumn col = new TableColumn(table, SWT.NONE);
 				col.setText(fn);
 				col.setWidth(150);
 			}
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), "Error updating columns.\n"+e.getMessage(), null);
 		}
 		finally {
 			table.setRedraw(true);
 		}
 		fillTable(0, 0);
 	}
 	
 	@Override
 	protected void finalize () {
 		dispose();
 	}
 
 	@Override
 	public void dispose () {
 		tm = null;
 		super.dispose();
 	}
 
 	// To call before currentEntry is updated to the upcoming value
 	void checkPage (int keyCode,
 		int stateMask)
 	{
 		int direction = -1;
 		int selection = 0;
 		int n = table.getSelectionIndex();
 
 		switch ( keyCode ) {
 		case SWT.ARROW_DOWN:
 			if ( n == table.getItemCount()-1 ) {
 				direction = 1;
 			}
 			break;
 		case SWT.PAGE_DOWN:
 			if ( (stateMask & SWT.CTRL) != 0 ) {
 				// Ctrl+PageDown goes to the next page
 				direction = 1;
 			}
 			else if ( n == table.getItemCount()-1 ) {
 				// PageDown goes to the next page only if 
 				// the current selection is the last row of the current page 
 				direction = 1;
 			}
 			break;
 		case SWT.ARROW_UP:
 			if ( n == 0 ) {
 				direction = 2;
 				selection = -1;
 			}
 			break;
 		case SWT.PAGE_UP:
 			if ( (stateMask & SWT.CTRL) != 0 ) {
 				// Ctrl+PageUp goes to the previous page
 				direction = 2;
 				selection = -1;
 			}
 			else if ( n == 0 ) {
 				// PageUp goes to the previous page only if 
 				// the current selection is the first row of the current page 
 				direction = 2;
 				selection = -1;
 			}
 			break;
 		case SWT.HOME:
 			if ( n == 0 ) {
 				direction = 0;
 			}
 			break;
 		case SWT.END:
 			if ( n == table.getItemCount()-1 ) {
 				direction = 3;
 				selection = -1;
 			}
 			break;
 		}
 
 		if ( direction > -1 ) {
 			saveEntry();
 			fillTable(direction, selection);
 		}
 	}
 	
 	void updateCurrentEntry () {
 		try {
 			int n = table.getSelectionIndex();
 			if ( n == -1 ) {
 				editPanel.setFields(null, null);
 			}
 			else {
 				TableItem ti = table.getItem(n);
 				editPanel.setFields(
 					srcCol==-1 ? null : ti.getText(srcCol),
 					trgCol==-1 ? null : ti.getText(trgCol));
 			}
 			currentEntry = n;
 			statusBar.setCounter(n, table.getItemCount());
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), "Error while updating entry.\n"+e.getMessage(), null);
 		}
 	}
 	
 	void saveEntry () {
 		if ( currentEntry < 0 ) return;
 		// Else: save the entry
 		TableItem ti = table.getItem(currentEntry);
 		if ( editPanel.isSourceModified() && ( srcCol != -1 )) {
 			ti.setText(srcCol, editPanel.getSourceText());
 			ti.setData((Integer)ti.getData() | SAVE_SOURCE);
 			needSave = true;
 		}
 		if ( editPanel.isTargetModified() && ( trgCol != -1 )) {
 			ti.setText(trgCol, editPanel.getTargetText());
 			ti.setData((Integer)ti.getData() | SAVE_TARGET);
 			needSave = true;
 		}
 	}
 	
 	boolean wasModified () {
 		return wasModified;
 	}
 	
 	/**
 	 * Saves the modifications in the current page into the back-end.
 	 */
 	private void saveModificationsIfNeeded () {
 		if ( !needSave ) {
 			return; // Nothing need to be saved
 		}
 		wasModified = true; // Indicates that the TM was changed in this session
 		
 		LinkedHashMap<String, Object> tuFields = new LinkedHashMap<String, Object>();
 		LinkedHashMap<String, Object> segFields = new LinkedHashMap<String, Object>();
 
 		for ( int i=0; i<table.getItemCount(); i++ ) {
 			TableItem ti = table.getItem(i);
 			int signal = (Integer)ti.getData();
 			if ( signal != 0 ) {
 				long segKey = Long.valueOf(ti.getText(0));
 				
 				if ( (signal & SAVE_FLAG) == SAVE_FLAG ) {
 					segFields.put(DbUtil.FLAG_NAME, ti.getChecked());
 				}
 				if ( (signal & SAVE_SOURCE) == SAVE_SOURCE ) {
 					String fn = table.getColumns()[srcCol].getText();
 					segFields.put(fn, ti.getText(srcCol));
 				}
 				if ( (signal & SAVE_TARGET) == SAVE_TARGET ) {
 					String fn = table.getColumns()[trgCol].getText();
 					segFields.put(fn, ti.getText(trgCol));
 				}
 				tm.updateRecord(segKey, tuFields, segFields);
 			}
 		}
 		needSave = false;
 	}
 	
 	/**
 	 * Fills the table with a new page
 	 * @param direction 0=from the top, 1=next, 2=previous, 3=last
 	 * @param selection 0=top, -1=last
 	 */
 	void fillTable (int direction,
 		int selection)
 	{
 		try {
 			saveModificationsIfNeeded();
 			ResultSet rs = null;
 			switch ( direction ) {
 			case 0:
 				rs = tm.getFirstPage();
 				break;
 			case 1:
 				rs = tm.getNextPage();
 				break;
 			case 2:
 				rs = tm.getPreviousPage();
 				break;
 			case 3:
 				rs = tm.getLastPage();
 				break;
 			}
 			if ( rs == null ) {
 				// No move of the page, leave things as they are
 				return;
 			}
 			
 			table.removeAll();
 			currentEntry = -1;
 			
 			while ( rs.next() ) {
 				TableItem item = new TableItem(table, SWT.NONE);
 				item.setText(0, String.format("%d", rs.getLong(ITm.SEGKEY_FIELD)));
 				item.setChecked(rs.getBoolean(ITm.FLAG_FIELD));
 				item.setData(0); // Modified flag
 				for ( int i=0; i<opt.getVisibleFields().size(); i++ ) {
 					// +2 because the result set has always seg-key and flag (and 1-based index)
 					item.setText(i+1, rs.getString(i+3)==null ? "" : rs.getString(i+3));
 				}
 			}
 			if ( table.getItemCount() > 0 ) {
 				if ( selection == -1 ) table.setSelection(table.getItemCount()-1);
 				else table.setSelection(0);
 				updateCurrentEntry();
 				statusBar.setPage(tm.getCurrentPage(), tm.getPageCount());
 			}
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), "Error while filling the table.\n"+e.getMessage(), null);
 		}
 	}
 	
 	void toggleExtra () {
 		editPanel.toggleExtra();
 	}
 	
 	void toggleLog () {
 		if ( sashMain.getWeights()[2] > 0 ) {
 			sashMain.setWeights(new int[]{3, 7, 0});
 		}
 		else {
 			sashMain.setWeights(new int[]{3, 7, 2});
 		}
 	}
 	
 	void showLog () {
 		// Ensure the Log panel is visible
 		if ( sashMain.getWeights()[2] <= 0 ) {
 			toggleLog();
 		}
 	}
 
 	EditorPanel getEditorPanel () {
 		return editPanel;
 	}
 	
 	void startThread (Thread workerThread) {
 		this.workerThread = workerThread;
 		workerThread.start();
 	}
 	
 	@Override
 	public void update (IObservable source,
 		Object arg)
 	{
 		if ( mainForm.getCurrentTmPanel() == this ) {
 			mainForm.updateCommands();
 		}
 		resetTmDisplay();
 	}
 
 }
