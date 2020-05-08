 package net.sf.jmoney.entrytable;
 
 public class RowSelectionTracker<R extends RowControl> {
 
 	private R currentRowControl = null;
 	
 	public R getSelectedRow() {
 		return currentRowControl;
 	}
 
 	/**
 	 * Sets the focus to the given row and column.
 	 * 
	 * @param row
	 * @param column
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
 			}
 			
 			currentRowControl = row;
 			
			row.arrive();  // Causes the selection colors etc.  Focus is already set.
 		}
 		return true;
 	}
 
 }
