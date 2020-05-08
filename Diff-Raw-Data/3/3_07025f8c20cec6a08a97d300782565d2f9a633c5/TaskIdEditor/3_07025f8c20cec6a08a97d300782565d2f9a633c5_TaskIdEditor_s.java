 package com.versionone.taskview.views;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 
 import com.versionone.common.sdk.Task;
 import com.versionone.taskview.Activator;
 
 /**
  * Provide edit support for the Task ID Column
  * 
  * @author jerry
  */
 public class TaskIdEditor extends EditingSupport {
 
 	/**
 	 * This is the cell editor used to edit the Task ID cell.  It was created 
 	 * based on the TextCellEditor.  The objective here is to only have 
 	 * support for Copy, and nothing else.
 	 * 
 	 * @author jerry
 	 *
 	 */
 	class TaskIdCellEditor extends CellEditor {
 
 		protected Text text;
 		
 	    public TaskIdCellEditor(Composite parent, int style) {
 	        super(parent, style);
 	    }
 		
 	    public TaskIdCellEditor(Composite parent) {
 	        this(parent, SWT.SINGLE);
 	    }
 	    
 		@Override
 		protected Control createControl(Composite parent) {
 	        text = new Text(parent, getStyle());
 	        text.setFont(parent.getFont());
 	        text.setBackground(parent.getBackground());
 			text.setEditable(false);
 			text.setMenu(createMenu());
 	        return text;
 		}
 
 		@Override
 		protected Object doGetValue() {
 			return text.getText();
 		}
 
 		@Override
 		protected void doSetFocus() {
 			if(null != text) {
 				text.selectAll();
 				text.setFocus();
 			}
 		}
 
 		@Override
 		protected void doSetValue(Object value) {
 			Assert.isTrue(value != null && (value instanceof String));
 			text.setText((String)value);
 		}
 		
 		private Menu createMenu() {
 			Menu rc = new Menu(text);
 			MenuItem copyItem = new MenuItem(rc, SWT.PUSH);
 			copyItem.setText("Copy");
 			copyItem.addSelectionListener(new SelectionListener() {
 
				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					text.copy();
 				}
 
				@Override
 				public void widgetSelected(SelectionEvent e) {
 					text.copy();
 				}
 			});
 			return rc;
 		}
 	}
 	
 	TaskIdCellEditor _editor = null;
 
 	/**
 	 * Create  
 	 * @param viewer - table viewer
 	 */
 	public TaskIdEditor(TableViewer viewer) {
 		super(viewer);
 		_editor = new TaskIdCellEditor(viewer.getTable());
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		return true;
 	}
 
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		return _editor;
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		try {
 			return ((Task)element).getID();
 		} catch (Exception e) {
 			Activator.logError(e);
 		}
 		return "*** ERROR ***";
 	}
 
 	@Override
 	protected void setValue(Object element, Object value) {
 		// DO NOT modify the value
 	}
 
 }
