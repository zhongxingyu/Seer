 package net.sf.jmoney.entrytable;
 
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

 public class RowSelectionTracker<R extends RowControl> {
 
 	private R currentRowControl = null;
 	
	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			/*
			 * The current row control has been disposed. When that happens
			 * there is no row selected.
			 */
			if (e.widget != currentRowControl) {
				throw new RuntimeException("Something is wrong.");
			}
			currentRowControl = null;
		}
		
	};
	
 	public R getSelectedRow() {
 		return currentRowControl;
 	}
 
 	/**
 	 * Sets the focus to the given row and column.
 	 * 
 	 * @param row the row to be the newly selected row, or null if no row is
 	 * 				to be selected
 	 * @param column the column to be the selected row, or null if no column
 	 * 				is to get the selection.  NOTE: this is not yet implemented
 	 * @return true if the new row selection could be made, false if there
 	 * 		are issues with a previously selected row that prevent the change
 	 * 		in selection from being made
 	 */
 	public boolean setSelection(R row,	CellBlock column) {
 		if (row != currentRowControl) {
 			if (currentRowControl != null) {
 				if (!currentRowControl.canDepart()) {
 					return false;
 				}
				
				currentRowControl.removeDisposeListener(disposeListener);
 			}
 			
 			currentRowControl = row;
 			
 			if (row != null) {
 				row.arrive();  // Causes the selection colors etc.  Focus is already set.
				
				row.addDisposeListener(disposeListener);
 			}
 		}
 		return true;
 	}
 
 }
