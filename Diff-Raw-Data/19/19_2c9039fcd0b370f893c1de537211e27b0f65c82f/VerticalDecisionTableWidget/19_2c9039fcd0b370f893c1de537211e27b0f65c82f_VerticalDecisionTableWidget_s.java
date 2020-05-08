 package org.drools.guvnor.decisiontable.client.widget;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.drools.guvnor.decisiontable.client.widget.cells.CellValueFactory;
 import org.drools.guvnor.decisiontable.client.widget.cells.VerticalDecisionTableCellFactory;
 import org.drools.ide.common.client.modeldriven.dt.ActionCol;
 import org.drools.ide.common.client.modeldriven.dt.AttributeCol;
 import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
 import org.drools.ide.common.client.modeldriven.dt.DTColumnConfig;
 import org.drools.ide.common.client.modeldriven.dt.GuidedDecisionTable;
 import org.drools.ide.common.client.modeldriven.dt.MetadataCol;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.TableCellElement;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Vertical implementation of a Decision Table where rules are represented as
  * rows and the definition (meta, conditions and actions are represented as
  * columns.
  * 
  * @author manstis
  * 
  */
 public class VerticalDecisionTableWidget extends DecisionTableWidget {
 
     public VerticalDecisionTableWidget() {
 	this.manager = new VerticalSelectionManager();
 	this.cellFactory = new VerticalDecisionTableCellFactory(this);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getMainPanel()
      */
     @Override
     protected Panel getMainPanel() {
 	if (this.mainPanel == null) {
 	    this.mainPanel = new HorizontalPanel();
 	}
 	return this.mainPanel;
     }
 
     @Override
     protected Panel getBodyPanel() {
 	if (this.bodyPanel == null) {
 	    this.bodyPanel = new VerticalPanel();
 	}
 	return this.bodyPanel;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getHeaderWidget()
      */
     @Override
     protected DecisionTableHeaderWidget getHeaderWidget() {
 	if (this.headerWidget == null) {
 	    this.headerWidget = new VerticalDecisionTableHeaderWidget(this);
 	}
 	return this.headerWidget;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getSidebarWidget()
      */
     @Override
     protected DecisionTableSidebarWidget getSidebarWidget() {
 	if (this.sidebarWidget == null) {
 	    this.sidebarWidget = new VerticalDecisionTableSidebarWidget(this);
 	}
 	return this.sidebarWidget;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.drools.guvnor.decisiontable.client.widget.DecisionTableWidget#
      * getScrollHandler()
      */
     @Override
     protected ScrollHandler getScrollHandler() {
 	return new ScrollHandler() {
 
 	    @Override
 	    public void onScroll(ScrollEvent event) {
 		headerWidget.setScrollPosition(scrollPanel
 			.getHorizontalScrollPosition());
 		sidebarWidget
 			.setScrollPosition(scrollPanel.getScrollPosition());
 	    }
 	};
     }
 
     /**
      * Set the Decision Table's data. This removes all existing columns from the
      * Decision Table and re-creates them based upon the provided data.
      * 
      * @param data
      */
     @Override
     public void setData(GuidedDecisionTable model) {
 
 	this.model = model;
 
 	final int dataSize = model.getData().length;
 
 	if (dataSize > 0) {
 
 	    // Clear existing columns
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		table.removeColumn(iCol);
 	    }
 	    columns.clear();
 
 	    // Initialise CellTable's Metadata columns
 	    int iCol = 0;
 	    for (DTColumnConfig col : model.getMetadataCols()) {
 		DynamicEditColumn column = new DynamicEditColumn(col,
 			new DecisionTableProxyCell(this.manager,
 				this.cellFactory), iCol);
 		table.addColumn(column);
 		columns.add(iCol, column);
 		iCol++;
 	    }
 
 	    // Initialise CellTable's Attribute columns
 	    for (DTColumnConfig col : model.getAttributeCols()) {
 		DynamicEditColumn column = new DynamicEditColumn(col,
 			new DecisionTableProxyCell(this.manager,
 				this.cellFactory), iCol);
 		table.addColumn(column);
 		columns.add(iCol, column);
 		iCol++;
 	    }
 
 	    // Initialise CellTable's Condition columns
 	    for (DTColumnConfig col : model.getConditionCols()) {
 		DynamicEditColumn column = new DynamicEditColumn(col,
 			new DecisionTableProxyCell(this.manager,
 				this.cellFactory), iCol);
 		table.addColumn(column);
 		columns.add(iCol, column);
 		iCol++;
 	    }
 
 	    // Initialise CellTable's Action columns
 	    for (DTColumnConfig col : model.getActionCols()) {
 		DynamicEditColumn column = new DynamicEditColumn(col,
 			new DecisionTableProxyCell(this.manager,
 				this.cellFactory), iCol);
 		table.addColumn(column);
 		columns.add(iCol, column);
 		iCol++;
 	    }
 
 	    // Setup data
 	    this.data = new DynamicData();
 	    for (int iRow = 0; iRow < dataSize; iRow++) {
 		String[] row = model.getData()[iRow];
 		ArrayList<CellValue> cellRow = new ArrayList<CellValue>();
 		for (iCol = 0; iCol < row.length; iCol++) {
 		    DTColumnConfig column = columns.get(iCol).getModelColumn();
 		    CellValue cv = CellValueFactory.getInstance()
 			    .makeCellValue(column, iRow, iCol);
 		    cv.setValue(row[iCol]);
 		    cellRow.add(cv);
 		}
 		this.data.add(cellRow);
 	    }
 	}
 	table.setPageSize(dataSize);
 	table.setRowCount(dataSize);
 	table.setRowData(0, data);
 
 	// Calls to CellTable.setRowData() causes the rows to revert to their
 	// default heights
 	manager.assertRowHeights();
 
 	// Draw the header
 	headerWidget.redraw();
 
     }
 
     /**
      * This is where the "action" takes place! Some of this is probably common
      * to a Horizontal Decision Table too, however until I start to write one
      * it's left in here!
      * 
      * @author manstis
      * 
      */
     private class VerticalSelectionManager implements SelectionManager {
 
 	private boolean isMerged = false;
 
 	// Selections store the actual grid data selected (irrespective of
 	// merged cells). So a merged cell spanning 2 rows is stored as 2
 	// selections. Selections are ordered by row number so we can
 	// iterate top to bottom.
 	private TreeSet<Coordinate> selections = new TreeSet<Coordinate>(
 		new Comparator<Coordinate>() {
 
 		    @Override
 		    public int compare(Coordinate o1, Coordinate o2) {
 			return o1.getRow() - o2.getRow();
 		    }
 
 		});
 
 	/**
 	 * Select a single cell. If the cell is merged the selection is extended
 	 * to include all merged cells.
 	 * 
 	 * @param start
 	 *            The physical coordinate of the cell
 	 */
 	@Override
 	public void startSelecting(Coordinate start) {
 	    clearSelection();
 	    CellValue startCell = data.get(start);
 	    extendSelection(startCell.getCoordinate());
 	}
 
 	/**
 	 * Return a cell based upon a coordinate. Rows and Columns are
 	 * zero-based and conceptually extend downwards and rightwards.
 	 * 
 	 * 
 	 * @param c
 	 *            The physical coordinate of the cell
 	 */
 	@Override
 	public CellValue getPhysicalCell(Coordinate c) {
 	    return data.get(c);
 	}
 
 	/**
 	 * Clear and selection.
 	 */
 	@Override
 	public void clearSelection() {
 	    selections.clear();
 	}
 
 	/**
 	 * Update all selected cells with the applicable value. When a value of
 	 * a cell is changed it can force additional merging of cells.
 	 * 
 	 * @param value
 	 *            The value with which to update selected cells
 	 */
 	@Override
 	public void update(Object value) {
 	    for (Coordinate c : this.selections) {
 		data.set(c, value);
 	    }
 	    // Redraw and re-apply our visual trickery
 	    // TODO Only a partial redraw is necessary
 	    removeModelMerging();
 	    table.redraw();
 	    assertModelMerging();
 	    applyMergingToTable();
 	}
 
 	/**
 	 * Add a new column to the right of the table
 	 * 
 	 * @param title
 	 *            A title for the column
 	 */
 	@Override
 	public void addColumn(DTColumnConfig modelColumn) {
 	    int index = 0;
 	    if (modelColumn instanceof MetadataCol) {
 		index = findMetadataColumnIndex();
 	    } else if (modelColumn instanceof AttributeCol) {
 		index = findAttributeColumnIndex();
 	    } else if (modelColumn instanceof ConditionCol) {
 		index = findConditionColumnIndex();
 	    } else if (modelColumn instanceof ActionCol) {
 		index = findActionColumnIndex();
 	    }
 	    insertColumnBefore(modelColumn, index);
 	}
 
 	// Find the right-most index for a Metadata column
 	private int findMetadataColumnIndex() {
 	    int index = 0;
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		DTColumnConfig column = columns.get(iCol).getModelColumn();
 		if (column instanceof MetadataCol) {
 		    index = iCol;
 		}
 	    }
 	    return index + 1;
 	}
 
 	// Find the right-most index for a Attribute column
 	private int findAttributeColumnIndex() {
 	    int index = 0;
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		DTColumnConfig column = columns.get(iCol).getModelColumn();
 		if (column instanceof MetadataCol) {
 		    index = iCol;
 		} else if (column instanceof AttributeCol) {
 		    index = iCol;
 		}
 	    }
 	    return index + 1;
 	}
 
 	// Find the right-most index for a Condition column
 	private int findConditionColumnIndex() {
 	    int index = 0;
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		DTColumnConfig column = columns.get(iCol).getModelColumn();
 		if (column instanceof MetadataCol) {
 		    index = iCol;
 		} else if (column instanceof AttributeCol) {
 		    index = iCol;
 		} else if (column instanceof ConditionCol) {
 		    index = iCol;
 		}
 	    }
 	    return index + 1;
 	}
 
 	// Find the right-most index for an Action column
 	private int findActionColumnIndex() {
 	    int index = columns.size();
 	    return index;
 	}
 
 	/**
 	 * Insert a new column at the specified index.
 	 * 
 	 * @param title
 	 *            A title for the column
 	 * 
 	 * @param index
 	 *            The (zero-based) index of the column
 	 */
 	@Override
 	public void insertColumnBefore(DTColumnConfig modelColumn, int index) {
 
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		CellValue cell = CellValueFactory.getInstance().makeCellValue(
 			modelColumn, iRow, index);
 		data.get(iRow).add(index, cell);
 	    }
 	    assertColumnCoordinates(index);
 	    // CellTable doesn't support inserting arbitrary columns so we
 	    // need to delete and reinsert all columns. We also need to
 	    // re-index the model column from which the cell will retrieve
 	    // it's value
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		table.removeColumn(0);
 	    }
 	    DynamicEditColumn column = new DynamicEditColumn(modelColumn,
 		    new DecisionTableProxyCell(this, cellFactory), index);
 	    columns.add(index, column);
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		DynamicEditColumn col = columns.get(iCol);
 		col.setColumnIndex(iCol);
 		table.addColumn(col);
 	    }
 
 	    // Changing the data causes a redraw so we need to re-apply our
 	    // visual trickery
 	    assertModelMerging();
 	    table.setRowCount(data.size());
 	    table.setPageSize(data.size());
 	    table.setRowData(0, data);
 	    applyMergingToTable();
 
 	    // Header needs to be narrowed when the vertical scrollbar appears
 	    headerWidget.setWidth(scrollPanel.getElement().getClientWidth()
 		    + "px");
 	    headerWidget.redraw();
 	}
 
 	/**
 	 * Add a new row to the bottom of the table
 	 */
 	@Override
 	public void addRow() {
 	    insertRowBefore(data.size());
 	}
 
 	/**
 	 * Insert a new row at the specified index.
 	 * 
 	 * @param index
 	 *            The (zero-based) index of the row
 	 */
 	@Override
 	public void insertRowBefore(int index) {
 	    if (index < 0) {
 		throw new IllegalArgumentException(
 			"Row number cannot be less than zero.");
 	    }
 	    if (index > data.size()) {
 		throw new IllegalArgumentException(
 			"Row number cannot be greater than the number of declared rows.");
 	    }
 
 	    List<CellValue> row = new ArrayList<CellValue>();
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		DTColumnConfig column = columns.get(iCol).getModelColumn();
 		CellValue data = CellValueFactory.getInstance().makeCellValue(
 			column, index, iCol);
 		row.add(data);
 	    }
 	    data.add(index, row);
 	    assertRowCoordinates(index);
 
 	    // Changing the data causes a redraw so we need to re-apply our
 	    // visual trickery
 	    assertModelMerging();
 	    table.setRowCount(data.size());
 	    table.setPageSize(data.size());
 	    table.setRowData(0, data);
 	    applyMergingToTable();
 
 	    // Header needs to be narrowed when the vertical scrollbar appears
 	    headerWidget.setWidth(scrollPanel.getElement().getClientWidth()
 		    + "px");
 	    headerWidget.redraw();
 	}
 
 	/**
 	 * This ensures the height of rows remains correct following merge
 	 * operations. Thinking about it this probably doesn't make sense for a
 	 * Horizontal Decision Table but I'll refactor it when I get to write
 	 * that one!
 	 */
 	@Override
 	public void assertRowHeights() {
 	    final int MAX_ROW = data.size();
 	    for (int iRow = 0; iRow < MAX_ROW; iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 		    CellValue cell = data.get(iRow).get(iCol);
 		    if (cell.getRowSpan() != 0) {
 			TableCellElement tce = table
 				.getRowElement(
 					cell.getHtmlCoordinate().getRow())
 				.getCells()
 				.getItem(cell.getHtmlCoordinate().getCol());
			int height = cell.getRowSpan() * resource.cellTableStyle().rowHeight();
 			tce.getStyle().setHeight(height, Unit.PX);
 		    }
 		}
 	    }
 	}
 
 	/**
 	 * Toggle the state of Decision Table merging.
 	 * 
 	 * @return The state of merging after completing this call
 	 */
 	@Override
 	public boolean toggleMerging() {
 	    if (!isMerged) {
 		isMerged = true;
 		assertModelMerging();
 		applyMergingToTable();
 	    } else {
 		isMerged = false;
 		removeModelMerging();
 		table.redraw();
 		assertRowHeights();
 	    }
 	    return isMerged;
 	}
 
 	// Ensure Coordinates are the extents of merged cell
 	private void extendSelection(Coordinate coordinate) {
 	    CellValue startCell = data.get(coordinate);
 	    CellValue endCell = startCell;
 	    while (startCell.getRowSpan() == 0) {
 		startCell = data.get(startCell.getCoordinate().getRow() - 1)
 			.get(startCell.getCoordinate().getCol());
 	    }
 
 	    if (startCell.getRowSpan() > 1) {
 		endCell = data.get(
 			endCell.getCoordinate().getRow() + endCell.getRowSpan()
 				- 1).get(endCell.getCoordinate().getCol());
 	    }
 	    selectRange(startCell, endCell);
 	}
 
 	// Select a range of cells between the two coordinates.
 	private void selectRange(CellValue startCell, CellValue endCell) {
 	    int col = startCell.getCoordinate().getCol();
 	    for (int iRow = startCell.getCoordinate().getRow(); iRow <= endCell
 		    .getCoordinate().getRow(); iRow++) {
 		CellValue cell = data.get(iRow).get(col);
 		selections.add(cell.getCoordinate());
 	    }
 	}
 
 	// Ensure cells in rows have correct physical coordinates
 	private void assertRowCoordinates(int index) {
 	    for (int iRow = index; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 		    CellValue cell = data.get(iRow).get(iCol);
 		    cell.setCoordinate(new Coordinate(iRow, iCol));
 		}
 	    }
 	}
 
 	// Ensure cells in columns have correct physical coordinates
 	private void assertColumnCoordinates(int index) {
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = index; iCol < row.size(); iCol++) {
 		    CellValue cell = data.get(iRow).get(iCol);
 		    cell.setCoordinate(new Coordinate(iRow, iCol));
 		}
 	    }
 	}
 
 	// Here lays a can of worms! Each cell in the Decision Table
 	// has three coordinates: (1) The physical coordinate, (2) The
 	// coordinate relating to the HTML table element and (3) The
 	// coordinate mapping a HTML table element back to the physical
 	// coordinate. For example a cell could have the (1) physical
 	// coordinate (0,0) which equates to (2) HTML element (0,1) in
 	// which case the cell at physical coordinate (0,1) would
 	// have a (3) mapping back to (0,0).
 	@Override
 	public void assertModelIndexes() {
 
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		int colCount = 0;
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 
 		    CellValue indexCell = row.get(iCol);
 
 		    int newRow = 0;
 		    int newCol = 0;
 		    if (indexCell.getRowSpan() != 0) {
 			newRow = iRow;
 			newCol = colCount++;
 
 			CellValue cell = data.get(newRow).get(newCol);
 			cell.setPhysicalCoordinate(new Coordinate(iRow, iCol));
 
 		    } else {
 			List<CellValue> priorRow = data.get(iRow - 1);
 			CellValue priorCell = priorRow.get(iCol);
 			Coordinate priorHtmlCoordinate = priorCell
 				.getHtmlCoordinate();
 			newRow = priorHtmlCoordinate.getRow();
 			newCol = priorHtmlCoordinate.getCol();
 		    }
 		    indexCell.setHtmlCoordinate(new Coordinate(newRow, newCol));
 
 		}
 	    }
 	}
 
 	// Ensure merging is reflected in the model
 	@Override
 	public void assertModelMerging() {
 	    final int MAX_ROW = data.size() - 1;
 
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		for (int iRow = 0; iRow < MAX_ROW; iRow++) {
 		    int rowSpan = 1;
 		    CellValue cell1 = data.get(iRow).get(iCol);
 		    CellValue cell2 = data.get(iRow + rowSpan).get(iCol);
 
 		    // Don't merge empty cells
 		    if (isMerged && !cell1.isEmpty()) {
 			while (cell1.getValue().equals(cell2.getValue())
 				&& iRow + rowSpan < MAX_ROW) {
 			    cell2.setRowSpan(0);
 			    rowSpan++;
 			    cell2 = data.get(iRow + rowSpan).get(iCol);
 			}
 			if (cell1.getValue().equals(cell2.getValue())) {
 			    cell2.setRowSpan(0);
 			    rowSpan++;
 			}
 		    }
 		    cell1.setRowSpan(rowSpan);
 		    iRow = iRow + rowSpan - 1;
 		}
 	    }
 
 	    // Set indexes after merging has been corrected
 	    // TODO Could this be incorporated into here?
 	    assertModelIndexes();
 
 	}
 
 	// Remove merging from model
 	@Override
 	public void removeModelMerging() {
 	    for (int iCol = 0; iCol < columns.size(); iCol++) {
 		for (int iRow = 0; iRow < data.size(); iRow++) {
 		    CellValue cell = data.get(iRow).get(iCol);
 		    Coordinate c = new Coordinate(iRow, iCol);
 		    cell.setCoordinate(c);
 		    cell.setHtmlCoordinate(c);
 		    cell.setPhysicalCoordinate(c);
 		    cell.setRowSpan(1);
 
 		}
 	    }
 
 	    // Set indexes after merging has been corrected
 	    // TODO Could this be incorporated into here?
 	    assertModelIndexes();
 	}
 
 	// Apply merging to UI
 	@Override
 	public void applyMergingToTable() {
 
 	    if (isMerged) {
 		final int MAX_ROW = data.size();
 
 		for (int iCol = columns.size() - 1; iCol >= 0; iCol--) {
 		    for (int iRow = 0; iRow < MAX_ROW; iRow++) {
 			CellValue cell1 = data.get(iRow).get(iCol);
 
 			if (cell1.getRowSpan() == 0) {
 			    table.getRowElement(iRow).deleteCell(iCol);
 			} else if (cell1.getRowSpan() > 1) {
 			    table.getRowElement(
 				    cell1.getHtmlCoordinate().getRow())
 				    .getCells()
 				    .getItem(cell1.getHtmlCoordinate().getCol())
 				    .setRowSpan(cell1.getRowSpan());
 			}
 		    }
 		}
 	    }
 	    assertRowHeights();
 	}
 
 	@Override
 	public void sort() {
 	    final DynamicEditColumn[] sortOrderList = new DynamicEditColumn[columns
 		    .size()];
 	    int index = 0;
 	    for (DynamicEditColumn column : columns) {
 		int sortIndex = column.getSortIndex();
 		if (sortIndex != -1) {
 		    sortOrderList[sortIndex] = column;
 		    index++;
 		}
 	    }
 	    final int sortedColumnCount = index;
 
 	    List<List<CellValue>> displayedItems = table.getDisplayedItems();
 	    Collections.sort(displayedItems, new Comparator<List<CellValue>>() {
 		public int compare(List<CellValue> leftRow,
 			List<CellValue> rightRow) {
 		    int comparison = 0;
 		    for (int index = 0; index < sortedColumnCount; index++) {
 			DynamicEditColumn sortableHeader = sortOrderList[index];
 			Comparable leftColumnValue = leftRow.get(sortableHeader
 				.getColumnIndex());
 			Comparable rightColumnValue = rightRow
 				.get(sortableHeader.getColumnIndex());
 			comparison = (leftColumnValue == rightColumnValue) ? 0
 				: (leftColumnValue == null) ? -1
 					: (rightColumnValue == null) ? 1
 						: leftColumnValue
 							.compareTo(rightColumnValue);
 			if (comparison != 0) {
 			    switch (sortableHeader.getSortDirection()) {
 			    case ASCENDING:
 				break;
 			    case DESCENDING:
 				comparison = -comparison;
 				break;
 			    default:
 				throw new IllegalStateException(
 					"Sorting can only be enabled for ASCENDING or"
 						+ " DESCENDING, not sortDirection ("
 						+ sortableHeader
 							.getSortDirection()
 						+ ") .");
 			    }
 			    return comparison;
 			}
 		    }
 		    return comparison;
 		}
 	    });
 
 	    data.clear();
 	    data.addAll(displayedItems);
 
 	    removeModelMerging();
 	    table.setRowCount(data.size());
 	    table.setPageSize(data.size());
 	    table.setRowData(0, data);
 	    assertModelMerging();
 	    applyMergingToTable();
 
 	}
 
 	// ************** DEBUG
 
 	public void dumpIndexes() {
 	    System.out.println("coordinates");
 	    System.out.println("-----------");
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 		    CellValue cell = row.get(iCol);
 
 		    Coordinate c = cell.getCoordinate();
 		    int rowSpan = cell.getRowSpan();
 
 		    System.out.print(c.toString());
 		    System.out.print("-S" + rowSpan + " ");
 		}
 		System.out.print("\n");
 	    }
 
 	    System.out.println();
 	    System.out.println("htmlToDataMap");
 	    System.out.println("-------------");
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 		    CellValue cell = row.get(iCol);
 
 		    Coordinate c = cell.getPhysicalCoordinate();
 		    int rowSpan = cell.getRowSpan();
 
 		    System.out.print(c.toString());
 		    System.out.print("-S" + rowSpan + " ");
 		}
 		System.out.print("\n");
 	    }
 
 	    System.out.println();
 	    System.out.println("dataToHtmlMap");
 	    System.out.println("-------------");
 	    for (int iRow = 0; iRow < data.size(); iRow++) {
 		List<CellValue> row = data.get(iRow);
 		for (int iCol = 0; iCol < row.size(); iCol++) {
 		    CellValue cell = row.get(iCol);
 
 		    Coordinate c = cell.getHtmlCoordinate();
 		    int rowSpan = cell.getRowSpan();
 
 		    System.out.print(c.toString());
 		    System.out.print("-S" + rowSpan + " ");
 		}
 		System.out.print("\n");
 	    }
 
 	}
 
 	public void dumpSelections(String title) {
 	    System.out.println(title);
 	    System.out.println();
 	    for (Coordinate c : selections) {
 		CellValue cell = data.get(c.getRow()).get(c.getCol());
 		System.out.println(cell.getCoordinate().toString());
 	    }
 	    System.out.println();
 	}
 
     }
 
 }
