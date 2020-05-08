 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2007 Nigel Westbury <westbury@users.sf.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.entrytable;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Slider;
 
 public class VirtualRowTable<T extends EntryData> extends Composite {
 
 	/** the number of rows in the underlying model */
 	int rowCount;
 
 	/**
 	 * the 0-based index of the object in the underlying model that is currently
 	 * the top row in the visible area
 	 */ 
 	int topVisibleRow = 0;
 
 	/**
 	 * the set of row objects for all the rows that are either currently visible or
 	 * is the selected row (control for the selected row is maintained even when scrolled
 	 * out of view because it may contain unsaved data or may have data that cannot be saved
 	 * because it is not valid) 
 	 */
 	Map<EntryData, BaseEntryRowControl> rows = new HashMap<EntryData, BaseEntryRowControl>();
 
 	/**
 	 * the list of row objects for all the rows that used to be visible and may be re-used,
 	 * or may be released if no longer visible.  The <code>scrollToGivenFix</code> method will
 	 * move the row controls to this field, then fetch the row controls it needs for the new
 	 * scroll position (which moves the row control back from this field to the rows field),
 	 * then releases any rows left in this field.
 	 */
 	Map<EntryData, BaseEntryRowControl> previousRows = new HashMap<EntryData, BaseEntryRowControl>();
 	
 	/**
 	 * the currently selected row, as a 0-based index into the underlying rows,
 	 * or -1 if no row is selected. To get the selected row as an index into the
 	 * <code>rows</code> list, you must subtract currentVisibleTopRow from
 	 * this value.
 	 */
 	int currentRow = -1;
 
 	/**
 	 * The user is allowed to scroll the selected row off the client area.  There may
 	 * be uncommitted changes in this row.  These changes are not committed until the user
 	 * attempts to select another row.  Therefore we must keep the row control even though
 	 * it is not visible.
 	 * <P> 
 	 *  This field always represents the same row as <code>currentRow</code>. 
 	 */
 	RowSelectionTracker rowTracker;
 	
 	IContentProvider<T> contentProvider;
 
 	IRowProvider<T> rowProvider;
 
 	private Header<T> header;
 	
 	private Composite contentPane;
 	
 	/**
 	 * Size of the contentPane, cached for performance reasons only
 	 */
 	private Point clientAreaSize;
 
 	/**
 	 * Must be non-null.
 	 */
 	private Slider vSlider;
 
 	/**
 	 * The position of the slider.  This content pane and the slider both
 	 * update each other.  For example, if page down is pressed in this
 	 * content pane then the slider must be updated, but if the slider is
 	 * dragged then this content pane must be updated.
 	 * 
 	 * This class listens to the slider for changes.  However, if this content pane
 	 * updates the slider then we don't want the listener to process the change.
 	 * We can avoid this from happened if this class always sets the new position
 	 * in <code>sliderPosition</code> before changing the slider and if this class's
 	 * listener checks the value against <code>sliderPosition</code> before processing. 
 	 */
 	private int sliderPosition = 0;
 
 	protected FocusCellTracker focusCellTracker = new FocusCellTracker();
 	
 	/**
 	 * This composite creates a two by two grid.  The header in the
 	 * top left, the table of rows in the bottom left, the vertical
 	 * scroll bar in the bottom right, and a blank cell in the top
 	 * right.  If no vertical scroll bar is required then its size
 	 * is set to zero so the header and rows take up the full
 	 * width.  This layout ensures that the header is always the same
 	 * width as the rows, which is good if the columns are going to
 	 * line up with the header. 
 	 * 
 	 * @param parent
 	 * @param rootBlock
 	 * @param contentProvider
 	 * @param rowTracker 
 	 */
 	// TODO: tidy up EntriesTable parameter.  Perhaps we need to remove EntriesTable altogether?
 	public VirtualRowTable(Composite parent, Block<? super T, ?> rootBlock, EntriesTable entriesTable, IContentProvider<T> contentProvider, IRowProvider<T> rowProvider, RowSelectionTracker rowTracker) {
 		super(parent, SWT.NONE);
 		this.contentProvider = contentProvider;
 		this.rowProvider = rowProvider;
 		this.rowTracker = rowTracker;
 		
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		setLayout(layout);
 	
 		header = new Header<T>(this, SWT.NONE, rootBlock);
 		Composite blankPane = new Composite(this, SWT.NONE);
 		contentPane = createContentPane(this);
 		vSlider = new Slider(this, SWT.VERTICAL);
 		
 		header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		blankPane.setLayoutData(new GridData(0, 0));
 		contentPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		vSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
 
 		vSlider.addSelectionListener(sliderSelectionListener);
 
 		rowCount = contentProvider.getRowCount();
 		sliderPosition = 0;
 	}
 
 	/**
 	 * Refreshes the content of the rows.  The set of rows is
 	 * assumed unchanged.
 	 */
 	public void refreshContentOfAllRows() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Similar to <code>refreshContentOfAllRows</code>
 	 * but updates the balances only.
 	 */
 	public void refreshBalancesOfAllRows() {
 		for (EntryData entryData: rows.keySet()) {
 			rows.get(entryData).refreshBalance();
 		}
 		// TODO Auto-generated method stub
 		
 	}
 
 	// TODO: Verify first parameter needed.
 	// Can we clean this up?
 	public void setCurrentRow(T committedEntryData, T uncommittedEntryData) {
 		currentRow = contentProvider.indexOf(committedEntryData);
 		
 		header.setInput(uncommittedEntryData);
 	}	
 
 	/**
 	 * Deletes the given row.
 	 * 
 	 * The row must be deleted from the underlying content.  This
 	 * method is not responsible for doing that.  This method does
 	 * update the display, decrement the row count.
 `	 * 
 	 * If the row being deleted is the selected row then any uncommitted
 	 * changes are discarded without warning (it is assumed that the
 	 * caller gave sufficient warning to the user).
 	 * 
 	 * @param data
 	 */
 	public void deleteRow(int index) {
 		if (index == currentRow) {
 			currentRow = -1;
 		}
 		
 		// Three cases
 //		if (index < topVisibleRow) {
 //			topVisibleRow--;
 //		} else if (index >= topVisibleRow + rows.size()) {
 //			// nothing to do in this case
 //		} else {
 //			EntryData entryData
 //			BaseEntryRowControl removedRow = rows.remove(index - topVisibleRow);
 //			rowProvider.releaseRow(removedRow);
 //		}
 		
 		rowCount--;
		adjustVerticalScrollBar();
 		
 		// Refresh the display.
 		scrollToSliderPosition();
 
 		refreshBalancesOfAllRows();
 	}
 
 	/**
 	 * Inserts the given row.
	 * <P> 
 	 * The row must have been inserted into the underlying content.  This
 	 * method is not responsible for doing that.  This method does
	 * update the display, increment the row count and adjusting the scroll
	 * bar.
	 * <P>
 	 * This method does not affect the current selection.  It is possible that 
 	 * a row is inserted by another view/editor while a row is being edited.
 	 * In such a case, the editing of the row is not affected.
 	 * 
	 * @param index the index into the content of this new row
 	 */
 	public void insertRow(int index) {
 		rowCount++;
		adjustVerticalScrollBar();
 		
 		// Refresh the display.
 		scrollToSliderPosition();
 		
 		refreshBalancesOfAllRows();
 	}
 
 	/**
 	 * Sets the focus to the given column and row.
 	 *  
 	 * @param x
 	 * @param y
 	 */
 	public void setSelection(int x, int y) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * 
 	 * @return the selected row, or -1 if no row is selected
 	 */
 	public int getSelection() {
 		return currentRow;
 	}
 
 	private Composite createContentPane(Composite parent) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 
 		// This composite has not yet been sized.  Initialize the cached size to zeroes.
 		clientAreaSize = new Point(0, 0);
 
 		composite.addControlListener(new ControlAdapter() {
 		    @Override	
 			public void controlResized(ControlEvent e) {
 				Point newSize = composite.getSize();
 
 				if (newSize.x != clientAreaSize.x) {
 					// Width has changed.  Update the sizes of the row controls.
 					for (BaseEntryRowControl rowControl: rows.values()) {
 						int rowHeight = rowControl.computeSize(newSize.x, SWT.DEFAULT).y;
 						rowControl.setSize(newSize.x, rowHeight);
 					}
 				}
 
 				clientAreaSize = newSize;
 
 				/*
 				 * Refresh. This method refreshes the display according to the
 				 * current slider position. If the rows don't change height then
 				 * this method is not necessary. However, changing the width
 				 * could potentially change the preferred height of each row.
 				 */
 				scrollToSliderPosition();
 				
 				/*
 				 * Adjust the vertical scroller (make it invisible if all the rows
 				 * fit in the client area, or change the thumb bar height)
 				 */
 				adjustVerticalScrollBar();
 			}
 		});
 		
 		// EXPERIMENTAL:
 		composite.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				/*
 				 * FEATURE IN SWT: When SWT needs to resolve a mnemonic (accelerator)
 				 * character, it recursively calls the traverse event down all
 				 * controls in the containership hierarchy.  If e.doit is false,
 				 * no control has yet matched the mnemonic, and we don't have to
 				 * do anything since we don't do mnemonic matching and no mnemonic
 				 * has matched.
 				 */
 				if (e.doit) {
 //					parent.keyTraversed(TableRow.this, e);
 				}
 			}
 		});
 
 		return composite;
 	}
 
 	/*
 	 * Adjust the vertical scroller (make it invisible if all the rows
 	 * fit in the client area, or change the thumb bar height)
 	 */
 	protected void adjustVerticalScrollBar() {
 		/*
 		 * Calculate the average height of all the visible (or partially
 		 * visible) rows.
 		 */
 		int totalHeight = 0;
 		for (Control rowControl: rows.values()) {
 			totalHeight += rowControl.getSize().y;
 		}
 		double averageHeight = ((double)totalHeight) / rows.size();
 		
 		double heightAllRows = rowCount * averageHeight;
 		
 		if (heightAllRows <= clientAreaSize.y) {
 			vSlider.setVisible(false);
 		} else {
 			vSlider.setThumb((int)(vSlider.getMaximum() * clientAreaSize.y / heightAllRows));
 			vSlider.setVisible(true);
 		}
 	}
 
 	/**
 	 * Scroll the table so that the given row in fully visible. The table is
 	 * scrolled by the least amount possible, which means:
 	 * <ol>
 	 * <li>The row is already fully visible - do not scroll</li>
 	 * <li>The row is partially or fully off the top - scroll so the row is
 	 * aligned with the top of the visible area</li>
 	 * <li>The row is partially or fully off the bottom - scroll so the row is
 	 * aligned with the bottom of the visible area</li>
 	 * </ol>
 	 * 
 	 * If the given row is bigger than the visible area then it will not be
 	 * possible to fully show that row. In such case, the table is scrolled to
 	 * show the top of the row if there are no visible rows above the selected row,
 	 * otherwise to show the bottom of the row.
 	 * 
 	 * @param index
 	 *            of the row to show, where 0 is the index of the first row
 	 *            in the underlying list of rows
 	 */
 	void scrollToShowRow(int rowIndex) {
 		if (rowIndex <= topVisibleRow) {
 			setTopRow(rowIndex);
 		} else if (rowIndex >= topVisibleRow+rows.size() - 1) {
 			setBottomRow(rowIndex);
 		}
 	}
 
 	/**
 	 * Scroll the view to the given fix and update the scroll-bar
 	 * to reflect the new position of the visible area.
 	 * 
 	 * @param anchorRowNumber
 	 * @param anchorRowPosition
 	 */
 	void scrollToGivenFix(int anchorRowNumber, int anchorRowPosition) {
 		scrollViewToGivenFix(anchorRowNumber, anchorRowPosition);
 
 		// Having updated the view, we must move the scroll bar to match.
 
 		/*
 		 * This code is all about calculating the position of the vertical
 		 * scroll bar that matches the visible content of the visible area.
 		 * This is a little bit complex, but I think this code is correct.
 		 */
 
 //		System.out.println("-----------------");
 		for (int rowIndex = topVisibleRow; rowIndex < topVisibleRow + rows.size(); rowIndex++) {
 			Rectangle bounds = getRowControl(rowIndex).getBounds();
 
 			// so p = (r * height(r) - top(r)) / (n * height(r) - cah)
 
 			double p = ((double)(rowIndex * bounds.height - bounds.y)) / (rowCount * bounds.height - clientAreaSize.y);
 
 //			double start = ((double)row) / rowCount;
 //			double end = ((double)(row + 1)) / rowCount;
 //			System.out.println("p = " + p + ", start = " + start + ", end = " + end + ", height = " + bounds.height);
 			if (p >= ((double)rowIndex) / rowCount && p <= ((double)(rowIndex + 1)) / rowCount) {
 				double maximum = vSlider.getMaximum() - vSlider.getThumb();
 				sliderPosition = (int)(p * maximum);
 				vSlider.setSelection(sliderPosition);
 				return;
 			}
 		}
 		System.out.println("no match!!!"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This method scrolls the table. It updates the list of TableRow
 	 * objects and sets the positions appropriately.
 	 * 
 	 * A row and its position is specified. This method will position rows
 	 * above and below in order to fill the visible area.
 	 * 
 	 * Normally the row must be specified as a valid index into the list of
 	 * underlying row objects. That is, it must be not less than zero and
 	 * less than the number of rows in the underlying list. However, there
 	 * is one exception. This method does allow the index to be equal to the
 	 * number of rows (representing a row after the last row) if the
 	 * position of that row is at or below the bottom of the visible area.
 	 * 
 	 * This method does not handle adjustment of the focus or anything like
 	 * that. That is up to the caller.
 	 * 
 	 * @param anchorRowIndex
 	 *            the index of a row to be displayed, where the index is a
 	 *            0-based index into the underlying model.
 	 * @param anchorRowPosition
 	 *            the position at which the top of the given row is to be
 	 *            positioned, relative to the top of the visible client area
 	 */
 	private void scrollViewToGivenFix(int anchorRowIndex, int anchorRowPosition) {
 
 		/*
 		 * Save the previous set of visible rows.  In a small scroll, a lot of the rows
 		 * that were previously visible will remain visible, so we keep these controls.
 		 */
 		previousRows = rows;
 		rows = new HashMap<EntryData, BaseEntryRowControl>();
 		
 		/*
 		 * The <code>rows</code> field contains a list of consecutive rows
 		 * and <code>topVisibleRow</code> contains the absolute index
 		 * of the first row in the list.  These fields must be updated for
 		 * the new scroll position.
 		 */
 
 		/*
 		 * We add the following rows before we add the prior rows. The reason
 		 * for this is as follows. If there are not enough prior rows then we
 		 * align the first row with the top of the client area and if there are
 		 * not enough following rows then we align the last row with the bottom
 		 * of the client area. However, if there are not enough rows to fill the
 		 * client area then we want want the rows to be aligned with the top of
 		 * the client area with a blank area at the bottom. By going upwards
 		 * last, we ensure the top alignment overrules the bottom alignment.
 		 */
 		
 		/*
 		 * Add following rows until we reach the bottom of the client area.  If we
 		 * run out of rows in the underlying model then re-align the bottom row
 		 * with the bottom. 
 		 */
 		int rowPosition = anchorRowPosition;
 		int rowIndex = anchorRowIndex;
 		while (rowPosition < clientAreaSize.y) {
 			if (rowIndex == rowCount) {
 				// We have run out of rows.  Re-align to bottom.
 				anchorRowPosition += (clientAreaSize.y - rowPosition);
 				break;
 			}
 
 			Control rowControl = getRowControl(rowIndex);
 			int rowHeight = rowControl.getSize().y;
 			
 			rowIndex++;
 			rowPosition += rowHeight;
 		}
 
 		/*
 		 * Add prior rows until we reach the top of the client area.  If we
 		 * run out of rows in the underlying model then we must scroll to
 		 * the top.
 		 */
 		rowPosition = anchorRowPosition;
 		rowIndex = anchorRowIndex;
 		while (rowPosition > 0) {
 			if (rowIndex == 0) {
 				// We have run out of rows.  Re-align to top.
 				rowPosition = 0;
 				break;
 			}
 
 			rowIndex--;
 			Control rowControl = getRowControl(rowIndex);
 			int rowHeight = rowControl.getSize().y;
 
 			rowPosition -= rowHeight;
 		}
 
 		topVisibleRow = rowIndex; 
 		int newTopRowOffset = rowPosition;
 
 		/*
 		 * We have to move the controls front-to-back if we're scrolling
 		 * forwards and back-to-front if we're scrolling backwards to avoid ugly
 		 * screen refresh artifacts.
 		 * 
 		 * However, the problem is that 'scrolling forwards' and 'scrolling
 		 * backwards' are not well-defined. For example, what happens if a row
 		 * in the middle of the visible area is being deleted. The rows below
 		 * scroll up a bit to fill the gap. But suppose the rows below are small
 		 * and there are not enough to fill the gap. The rows above would then
 		 * have to move down to fill the gap. An unlikely situation, but this
 		 * demonstrates the difficulty in determining in a well defined way
 		 * whether we are scrolling up or down.
 		 * 
 		 * The solution is that we make two passes through the controls. First
 		 * iterate through the row controls moving only the controls that are
 		 * being moved upwards, then iterate in reverse moving only the controls
 		 * that are being moved downwards.
 		 */
 		int topPosition = newTopRowOffset;
 		rowIndex = topVisibleRow;
 		
 		while (topPosition < clientAreaSize.y && rowIndex < rowCount) {
 			BaseEntryRowControl rowControl = getRowControl(rowIndex);
 			int rowHeight = rowControl.getSize().y;
 			if (rowControl.getBounds().y >= topPosition) {
 				rowControl.setBounds(0, topPosition, clientAreaSize.x, rowHeight);
 			}
 			topPosition += rowHeight;
 			rowIndex++;
 		}
 
 		while (topPosition > 0) {
 			rowIndex--;
 			BaseEntryRowControl rowControl = getRowControl(rowIndex);
 			int rowHeight = rowControl.getSize().y;
 			topPosition -= rowHeight;
 			if (rowControl.getBounds().y < topPosition) {
 				rowControl.setBounds(0, topPosition, clientAreaSize.x, rowHeight);
 			}
 		}
 
 		/*
 		 * We must keep the selected row, even if it is not visible.
 		 * 
 		 * Note that we want to make the selected control invisible but we do
 		 * not set the visible property to false. This is because all rows in
 		 * the rows map are assumed to be visible and the visible property is
 		 * not set on when this row becomes visible. Also, we must be sure not
 		 * to mess with the height, because this is not re-calculated each time,
 		 * so we move it off just before the top of the visible area. This
 		 * ensures it remains not visible even if the client area is re-sized.
 		 */
 		BaseEntryRowControl selectedRow = (BaseEntryRowControl)rowTracker.getSelectedRow();
 		if (selectedRow != null) {
 			EntryData selectedEntryData = selectedRow.committedEntryData;
 			if (previousRows.containsKey(selectedEntryData)) {
 				rows.put(selectedEntryData, selectedRow);
 				previousRows.remove(selectedEntryData);
 
 				int rowHeight = selectedRow.getSize().y;
 				selectedRow.setLocation(0, -rowHeight);
 			}
 		}
 		
 		/*
 		 * Remove any previous rows that are now unused.
 		 * 
 		 * It is important to clear the map of previousRows because otherwise code
 		 * outside this method that attempts to get a row control may end up with a
 		 * row control that has already been released.
 		 */
 		for (BaseEntryRowControl<T, ?> rowControl: previousRows.values()) {
 			rowProvider.releaseRow(rowControl);
 		}
 		previousRows.clear();
 	}
 
 	void setTopRow(int topRow) {
 		scrollToGivenFix(topRow, 0);
 
 	}
 
 	private void setBottomRow(int bottomRow) {
 		scrollToGivenFix(bottomRow + 1, clientAreaSize.y);
 	}
 
 	/**
 	 * Returns the TableRow object for the given row, creating a row
 	 * if it does not exist.
 	 * 
 	 * This method lays out the rows given the current width of the
 	 * client area.  Callers can rely on the size of the row control
 	 * being set correctly.
 	 * 
 	 * @param rowIndex the 0-based index of the required row, based
 	 * 			on the rows in the underlying model
 	 * @return
 	 */
 	private BaseEntryRowControl getRowControl(int rowIndex) {
 		T entryData = contentProvider.getElement(rowIndex);
 
 		BaseEntryRowControl rowControl = rows.get(entryData);
 		if (rowControl == null) {
 			rowControl = previousRows.remove(entryData);
 			if (rowControl == null) {
 				// we must create a new row object
 				rowControl = rowProvider.getNewRow(contentPane, entryData);
 				int rowHeight = rowControl.computeSize(clientAreaSize.x, SWT.DEFAULT).y;
 				rowControl.setSize(clientAreaSize.x, rowHeight);
 			}
 
 			rows.put(entryData, rowControl);
 		}
 		
 		return rowControl;
 	}
 
 	/**
 	 * Page up will scroll the table so that the row above the first
 	 * fully visible row becomes the last row, with the bottom of the row
 	 * aligned with the bottom of the visible area.  If there is no fully
 	 * visible row but there are two partially visible rows then the table
 	 * will be scrolled so that the bottom of the top row is aligned with
 	 * the bottom of the visible area.  If there is only one row visible and
 	 * it is only partially visible then the table is scrolled by the visible height.
 	 */
 	public void doPageUp() {
 		if (currentRow == -1) {
 			return;
 		}
 
 		if (currentRow > 0) {
 			BaseEntryRowControl selectedRow = getSelectedRow();
 
 			if (!selectedRow.canDepart()) {
 				return;
 			}
 
 			int currentColumn = selectedRow.getCurrentColumn();
 
 			/*
 			 * Get previous row until we reach a row that, if positioned at the
 			 * top of the visible area, puts the bottom of the previous current
 			 * row at the bottom or below the bottom of the visible area.
 			 */
 			int totalHeight = 0;
 			do {
 				int rowHeight = getRowControl(currentRow).getBounds().height;
 				totalHeight += rowHeight;
 				if (totalHeight >= clientAreaSize.y) {
 					break;
 				}
 				currentRow--;
 			} while (currentRow > 0);
 
 			scrollToShowRow(currentRow);
 			getRowControl(currentRow).arrive(currentColumn);
 		}
 	}
 
 	public void doPageDown() {
 		if (currentRow == -1) {
 			return;
 		}
 
 		if (currentRow < rowCount - 1) {
 			BaseEntryRowControl selectedRow = getSelectedRow();
 
 			if (!selectedRow.canDepart()) {
 				return;
 			}
 
 			int currentColumn = selectedRow.getCurrentColumn();
 
 			/*
 			 * Get previous row until we reach a row that, if positioned at the
 			 * bottom of the visible area, puts the top of the previous current
 			 * row at the top or above the top of the visible area.
 			 */
 			int totalHeight = 0;
 			do {
 				int rowHeight = getRowControl(currentRow).getBounds().height;
 				totalHeight += rowHeight;
 				if (totalHeight >= clientAreaSize.y) {
 					break;
 				}
 				currentRow++;
 			} while (currentRow < rowCount - 1);
 
 			scrollToShowRow(currentRow);
 			getRowControl(currentRow).arrive(currentColumn);
 		}
 	}
 
 	public void doRowUp() {
 		if (currentRow == -1) {
 			return;
 		}
 
 		if (currentRow > 0) {
 			BaseEntryRowControl selectedRow = getSelectedRow();
 
 			if (!selectedRow.canDepart()) {
 				return;
 			}
 
 			int currentColumn = selectedRow.getCurrentColumn();
 
 			currentRow--;
 			scrollToShowRow(currentRow);
 			getRowControl(currentRow).arrive(currentColumn);
 		}
 	}
 
 	public void doRowDown() {
 		if (currentRow == -1) {
 			return;
 		}
 
 		if (currentRow < rowCount - 1) {
 			BaseEntryRowControl selectedRow = getSelectedRow();
 
 			if (!selectedRow.canDepart()) {
 				return;
 			}
 
 			int currentColumn = selectedRow.getCurrentColumn();
 
 			currentRow++;
 			scrollToShowRow(currentRow);
 			getRowControl(currentRow).arrive(currentColumn);
 		}
 	}
 
 	private BaseEntryRowControl getSelectedRow() {
 		if (currentRow == -1) {
 			return null;
 		} else {
 			EntryData entryData = contentProvider.getElement(currentRow);
 			return rows.get(entryData);
 		}
 	}
 
 	/**
 	 * @param portion
 	 */
 	private void scrollToSliderPosition() {
 		double maximum = vSlider.getMaximum() - vSlider.getThumb();
 		double portion = sliderPosition / maximum;
 
 		/*
 		 * find the 'anchor' row.  All other visible rows are positioned upwards
 		 * and downwards from this control.
 		 */
 		double rowDouble = portion * rowCount;
 		int anchorRowNumber = Double.valueOf(Math.floor(rowDouble)).intValue();
 		double rowRemainder = rowDouble - anchorRowNumber;
 
 		if (anchorRowNumber == rowCount) {
 			scrollViewToGivenFix(anchorRowNumber, clientAreaSize.y);
 		} else {
 			BaseEntryRowControl anchorRowControl = getRowControl(anchorRowNumber);
 			int anchorRowHeight = anchorRowControl.getSize().y;
 			int anchorRowPosition = (int)(portion * clientAreaSize.y - anchorRowHeight * rowRemainder);
 			anchorRowControl.setSize(clientAreaSize.x, anchorRowHeight);
 			scrollViewToGivenFix(anchorRowNumber, anchorRowPosition);
 		}
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
 //	public boolean setSelection(BaseEntryRowControl row,
 //			CellBlock column) {
 //		BaseEntryRowControl currentRowControl = getSelectedRow();
 //		if (row != currentRowControl) {
 //			if (currentRowControl != null) {
 //				if (!currentRowControl.canDepart()) {
 //					return false;
 //				}
 //			}
 //			
 //			row.arrive();  // Causes the selection colors etc.  Focus is already set.
 //			currentRow = rows.indexOf(row) + topVisibleRow; 
 //		}
 //		return true;
 //	}
 
 	/**
 	 * The SelectionListener for the table's vertical slider control.
 	 * 
 	 * Note that the selection never changes when the table is scrolled using
 	 * the scroll bar.  The UI would be rather confusing otherwise, because
 	 * then dragging the scroll bar would either cause a dialog to pop up asking if
 	 * the changes in the current selection should be committed or would cause
 	 * the changes to be committed without warning.  The usual convention with
 	 * tables is to allow the selection to be scrolled off the screen, so that
 	 * is what we do.
 	 */
 	private SelectionListener sliderSelectionListener = new SelectionListener() {
 		public void widgetSelected(SelectionEvent e) {
 			switch (e.detail) {
 			case SWT.ARROW_DOWN:
 			{
 				/*
 				 * Scroll down so that the next row below the top visible row
 				 * becomes the top visible row.
 				 */
 				scrollToGivenFix(topVisibleRow + 1, 0);
 			}
 			break;
 
 			case SWT.ARROW_UP:
 			{
 				/*
 				 * Scroll up so that the next row above the top visible row
 				 * becomes the top visible row.
 				 */
 				if (topVisibleRow > 0) {
 					scrollToGivenFix(topVisibleRow - 1, 0);
 				}
 			}
 			break;
 
 			case SWT.PAGE_DOWN:
 			{
 				/*
 				 * Page down so that the lowest visible row (or
 				 * partially visible row) becomes the top row.
 				 * 
 				 *  However, if the lowest visible row is also the top
 				 *  row (i.e. the row is so high that it fills the visible
 				 *  area) then scroll up by 90% of the height of the visible
 				 *  area.
 				 */
 				int bottomRow = topVisibleRow + rows.size() - 1;
 				if (rows.size() == 1) {
 					Control rowControl = rows.get(contentProvider.getElement(bottomRow)); 
 					scrollToGivenFix(bottomRow, rowControl.getBounds().y - clientAreaSize.y * 90 / 100);
 				} else {
 					scrollToGivenFix(bottomRow, 0);
 				}
 			}
 			break;
 
 			case SWT.PAGE_UP:
 			{
 				/*
 				 * Page up so that the first visible row (or
 				 * partially visible row) becomes the bottom row,
 				 * aligned with the bottom of the visible area.
 				 * 
 				 *  However, if the first visible row is also the bottom
 				 *  row (i.e. the row is so high that it fills the visible
 				 *  area) then scroll down by 90% of the height of the visible
 				 *  area.
 				 */
 				if (rows.size() == 1) {
 					Control rowControl = rows.get(contentProvider.getElement(topVisibleRow)); 
 					scrollToGivenFix(topVisibleRow, rowControl.getBounds().y + clientAreaSize.y * 90 / 100);
 				} else {
 					scrollToGivenFix(topVisibleRow+1, clientAreaSize.y);
 				}
 			}
 			break;
 
 			case SWT.NONE:
 			case SWT.DRAG:
 			default:
 				// Assume scroll bar dragged.
 			{
 				/*
 				 * The JavaDoc is incorrect.  It states that the selection can take
 				 * any value from 'minimum' to 'maximum'.  In fact the largest value
 				 * it can take is maximum - thumb (it has this value when the slider
 				 * is scrolled completely to the bottom).
 				 * 
 				 * We cannot rely on the thumb size because it changes.  The thumb size
 				 * will be appropriate for the last position but may not be correct
 				 * for the new position.  We therefore calculate a proportion between
 				 * 0 and 1.
 				 */
 				int selection = vSlider.getSelection();
 				if (selection == sliderPosition) {
 					return;
 				}
 				sliderPosition = selection;
 
 				scrollToSliderPosition();
 			}
 			}
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	};
 
 	/**
 	 * This method is called when the content changes.
 	 * Specific changes to the content such as a row insert
 	 * or delete can be more efficiently refreshed using the
 	 * deleteRow and insertRow methods.
 	 * 
 	 * This method is called when content is sorted or filtered.
 	 * 
 	 */
 	// TODO: Are there more efficent methods following JFace conventions
 	// to do this?
 	public void refreshContent() {
 		rowCount = contentProvider.getRowCount();
 		// TODO: ensure selected row remains selected and visible
 		// (if a sort.  Now if a filter then it may not remain visible).
 		setTopRow(0);
 	}
 
 	/**
 	 * This method is called when an attempt to leave the selected row fails.
 	 * We want to be sure that the selected row becomes visible because
 	 * the user needs to correct the errors in the row.
 	 */
 	public void scrollToShowRow(BaseEntryRowControl<T, ?> rowControl) {
 		int rowIndex = contentProvider.indexOf(rowControl.getContent());
 		scrollToShowRow(rowIndex);
 	}
 
 	/**
 	 * This method is called whenever a row in this table ceases to be a selected row,
 	 * regardless of whether the row is currently visible or not.
 	 */
 	public void rowDeselected(BaseEntryRowControl<T, ?> rowControl) {
 		/*
 		 * If the row is not visible then we can release it.  However,
 		 * there is no easy way of knowing whether it is visible. (We know
 		 * it will be in the rows map because the selected row is always
 		 * in that map whether visible or not).
 		 */
 	}
 
 	/**
 	 * This method will return a row control for a given EntryData
 	 * object.
 	 * 
 	 * It may be that the row is not visible.  In that case the view
 	 * is scrolled to make it visible.  The row is then selected.
 	 * 
 	 * This method is currently used only by 'duplicate transaction'
 	 * action and is called only on the new entry row.  We may therefore
 	 * want to review this method at some later time.
 	 *  
 	 * @param newEntryRow
 	 * @return
 	 */
 	public BaseEntryRowControl getRowControl(T entryData) {
 		// Crappy code...
 		scrollToShowRow(contentProvider.indexOf(entryData));
 		BaseEntryRowControl rowControl = rows.get(entryData);
 		rowControl.getChildren()[0].setFocus();
 		return rowControl;
 	}
 
 	/**
 	 * This method should be called when the height of a row may have changed.
 	 * <P>
 	 * The changed row will most likely also be the selected row. However, it is
 	 * possible that it is not. We keep the top of the selected row at the same
 	 * position, moving all rows below it up or down. However, if the table is
 	 * scrolled to the bottom or near the bottom and the row height is being
 	 * reduced then this may result in a blank space at the bottom of the table.
 	 * In that case we re-adjust the rows so the table is fully scrolled to the
 	 * bottom (which would result in the top of the changed row being moved
 	 * down).
 	 * 
 	 * @param rowControl
 	 */
 	public void refreshSize(BaseEntryRowControl<T, ?> rowControl) {
 		int rowTop = rowControl.getLocation().y;
 		
 		int rowHeight = rowControl.computeSize(clientAreaSize.x, SWT.DEFAULT).y;
 		rowControl.setSize(clientAreaSize.x, rowHeight);
 
 		int rowIndex = contentProvider.indexOf(rowControl.getContent());
 		scrollToGivenFix(rowIndex, rowTop);
 	}
 }
 
