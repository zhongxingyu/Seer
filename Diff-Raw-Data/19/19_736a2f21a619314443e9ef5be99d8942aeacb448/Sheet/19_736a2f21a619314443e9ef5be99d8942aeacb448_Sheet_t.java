 package org.jbei.ice.client.bulkupload.sheet;
 
 import java.util.ArrayList;
 
 import org.jbei.ice.client.bulkupload.SheetPresenter;
 import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
 import org.jbei.ice.client.bulkupload.widget.CellWidget;
 import org.jbei.ice.shared.EntryAddType;
 import org.jbei.ice.shared.dto.BulkUploadInfo;
 import org.jbei.ice.shared.dto.EntryInfo;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLTable.Cell;
 import com.google.gwt.user.client.ui.HasAlignment;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class Sheet extends Composite implements SheetPresenter.View {
 
     protected final FlexTable layout;
     protected FlexTable sheetTable; // table used to represent the spreadsheet
     protected int row; // current row in the spreadsheet
 
     private int currentRow;
     private int currentIndex;
 
     private int inputRow;     // row of last cell that was switch to input
     private int inputIndex;   // index of last cell that was switched to input
 
     protected final FlexTable colIndex;
     protected final ScrollPanel sheetTableFocusPanelWrapper;
     protected final ScrollPanel colIndexWrapper;
     protected final FlexTable header;
     protected final ScrollPanel headerWrapper;
     private CellWidget replaced;
 
     private int headerCol;
 
     protected final SheetPresenter presenter;
     private SheetCell newCellSelection;
     private boolean cellHasFocus;  // whether input widget for a cell has focus
 
     public final static int ROW_COUNT = 40;
 
     public Sheet(EntryAddType type) {
         this(type, null);
     }
 
     public Sheet(EntryAddType type, BulkUploadInfo info) {
 
         headerCol = 0;
 
         layout = new FlexTable();
         layout.setCellPadding(0);
         layout.setCellSpacing(0);
         initWidget(layout);
 
         header = new FlexTable();
         header.setCellPadding(0);
         header.setCellSpacing(0);
         header.setWidth("100%");
         header.addStyleName("sheet_header_table");
 
         sheetTable = new FlexTable();
         sheetTable.setCellPadding(0);
         sheetTable.setCellSpacing(0);
         sheetTable.setStyleName("sheet_table");
         sheetTable.setWidth("100%");
 
         // then wrap it in a scroll panel that expands to fill area given by browser
         sheetTableFocusPanelWrapper = new ScrollPanel(sheetTable);
         sheetTableFocusPanelWrapper.setWidth((Window.getClientWidth() - 40) + "px");
         sheetTableFocusPanelWrapper.setHeight((Window.getClientHeight() - 340 - 30) + "px");
 
         colIndex = new FlexTable();
         colIndex.setCellPadding(0);
         colIndex.setCellSpacing(0);
         colIndex.setStyleName("sheet_col_index");
         colIndexWrapper = new ScrollPanel(colIndex);
         colIndexWrapper.setHeight((Window.getClientHeight() - 340 - 30 - 15) + "px");
 
         addPanelHandlers();
         addWindowResizeHandler();
 
         currentRow = inputRow = -1;
         currentIndex = inputIndex = -1;
 
         sheetTable.addDoubleClickHandler(new CellDoubleClick());
         sheetTable.addClickHandler(new CellClick());
 
         // init
         headerWrapper = new ScrollPanel(header);
         headerWrapper.setWidth((Window.getClientWidth() - 15) + "px");
 
         addScrollHandlers();
 
         // presenter
         presenter = new SheetPresenter(this, type, info);
         init();
     }
 
     public void setCurrentInfo(BulkUploadInfo info) {
         presenter.setCurrentInfo(info);
     }
 
     // experimental
     public void decreaseWidthBy(int amount) {
         sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() - amount) + "px");
         headerWrapper.setWidth((headerWrapper.getOffsetWidth() - amount) + "px");
     }
 
     public void increaseWidthBy(int amount) {
         sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() + amount) + "px");
         headerWrapper.setWidth((headerWrapper.getOffsetWidth() + amount) + "px");
     }
 
     private void addWindowResizeHandler() {
         Window.addResizeHandler(new ResizeHandler() {
 
             private int previousWidth = Window.getClientWidth();
 
             @Override
             public void onResize(ResizeEvent event) {
                 // 970 is anticipated width of page window (menu?). "proper" way to do this is detect if
                 // window has horizontal scroll bars
                 if (Window.getClientWidth() < 970)
                     return;
 
                 int delta = event.getWidth() - previousWidth;
                 previousWidth = event.getWidth();
                 sheetTableFocusPanelWrapper.setWidth((sheetTableFocusPanelWrapper.getOffsetWidth() + delta) + "px");
                 headerWrapper.setWidth((headerWrapper.getOffsetWidth() + delta) + "px");
 
                 int wrapperHeight = (event.getHeight() - 340 - 30);
                 if (wrapperHeight >= 0)
                     sheetTableFocusPanelWrapper.setHeight(wrapperHeight + "px");
 
                 int rowIndexHeight = (event.getHeight() - 340 - 30 - 15);
                 if (rowIndexHeight >= 0)
                     colIndexWrapper.setHeight(rowIndexHeight + "px");
             }
         });
     }
 
     private void addScrollHandlers() {
         sheetTableFocusPanelWrapper.addScrollHandler(new ScrollHandler() {
 
             @Override
             public void onScroll(ScrollEvent event) {
                 headerWrapper.setHorizontalScrollPosition(sheetTableFocusPanelWrapper.getHorizontalScrollPosition());
                 colIndexWrapper.setVerticalScrollPosition(sheetTableFocusPanelWrapper.getVerticalScrollPosition());
 
                 if (row >= 1000)
                     return;
 
                 int vScrollPosition = sheetTableFocusPanelWrapper.getVerticalScrollPosition();
                 int maxVscrollPosition = sheetTableFocusPanelWrapper.getMaximumVerticalScrollPosition();
 
                 if (vScrollPosition >= maxVscrollPosition - 100) {
                     presenter.addRow(row);
                     // index col
                     HTML indexCell = new HTML((row + 1) + "");
                     colIndex.setWidget(row, 0, indexCell);
                     indexCell.setStyleName("index_cell");
                     colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");
                     row += 1;
                 }
 
             }
         });
     }
 
     protected void init() {
         DOM.setStyleAttribute(headerWrapper.getElement(), "overflowY", "hidden");
         DOM.setStyleAttribute(headerWrapper.getElement(), "overflowX", "hidden");
 
         DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowY", "hidden");
         DOM.setStyleAttribute(colIndexWrapper.getElement(), "overflowX", "hidden");
 
         // get header
         layout.setWidget(0, 0, headerWrapper);
         layout.getFlexCellFormatter().setColSpan(0, 0, 2);
         layout.setWidget(1, 0, colIndexWrapper);
         layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
         layout.setWidget(1, 1, sheetTableFocusPanelWrapper);
         layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);
 
         createHeaderCells();
 
         // add rows
         int count = ROW_COUNT;
         row = 0;
 
         while (count > 0) {
 
             presenter.addRow(row);
             // index col
             HTML indexCell = new HTML((row + 1) + "");
             colIndex.setWidget(row, 0, indexCell);
             indexCell.setStyleName("index_cell");
             colIndex.getFlexCellFormatter().setStyleName(row, 0, "index_td_cell");
 
             count -= 1;
             row += 1;
         }
     }
 
     private void addPanelHandlers() {
         sheetTable.addDomHandler(new KeyDownHandler() {
 
             @Override
             public void onKeyDown(KeyDownEvent event) {
 
                 if (event.isUpArrow()) {
                     dealWithUpArrowPress();

                 } else if (event.isDownArrow()) {
                     dealWithDownArrowPress();
                     event.preventDefault();
                 } else if (event.isRightArrow()) {
                    if (cellHasFocus)
                        return;
                     selectCell(currentRow, currentIndex + 1);
                     event.preventDefault();
                 } else if (event.isLeftArrow()) {
                    if (cellHasFocus)
                        return;
                     selectCell(currentRow, currentIndex - 1);
                     event.preventDefault();
                 } else {
                     int code = event.getNativeKeyCode();
 
                     if (KeyCodes.KEY_TAB == code || KeyCodes.KEY_ENTER == code) {
 
                         if (currentIndex == presenter.getFieldSize() - 1) {
                             selectCell(currentRow + 1, 0);
                         } else {
                             selectCell(currentRow, currentIndex + 1);
                         }
                         event.preventDefault();
                         return;
                     }
 
                     if (KeyCodes.KEY_SHIFT == code)
                         return;
 
                     switchToInput();
                 }
             }
         }, KeyDownEvent.getType());
     }
 
     protected Widget createHeaderCells() {
         addLeadHeader();
 
         SheetHeader.createHeaders(presenter.getTypeHeaders().getHeaders(), headerCol, row, header);
         headerCol += presenter.getTypeHeaders().getHeaderSize();
         addTailHeader();
 
         row += 1;
         return header;
     }
 
     // header that covers the span of the row index
     private void addLeadHeader() {
         HTML cell = new HTML("&nbsp;");
         cell.setStyleName("leader_cell_column_header");
         header.setWidget(row, headerCol, cell);
         header.getFlexCellFormatter().setStyleName(row, headerCol, "leader_cell_column_header_td");
         headerCol += 1;
     }
 
     // tail header 
     private void addTailHeader() {
         HTML cell = new HTML("&nbsp;");
         cell.setStyleName("tail_cell_column_header");
         header.setWidget(row, headerCol, cell);
         header.getFlexCellFormatter().setStyleName(row, headerCol, "tail_cell_column_header_td");
         headerCol += 1;
     }
 
     @Override
     public void clear() {
         if (!Window.confirm("This will clear all data. Continue?"))
             return;
 
         for (int i = 0; i < sheetTable.getRowCount(); i += 1) {
             if (isEmptyRow(i))
                 continue;
 
             int j = 0;
             for (CellColumnHeader header : presenter.getTypeHeaders().getHeaders()) {
                 HasText widget = (HasText) sheetTable.getWidget(i, j);
                 widget.setText("");
                 ((Widget) widget).setStyleName("cell");
                 header.getCell().reset();
                 j += 1;
             }
         }
     }
 
     public ArrayList<EntryInfo> getCellData(String ownerEmail, String owner) {
         return presenter.getCellEntryList(ownerEmail, owner);
     }
 
     @Override
     public int getSheetRowCount() {
         return sheetTable.getRowCount();
     }
 
     public void highlightHeaders(int row, int col) {
         SheetHeader.highlightHeader(col, header);
 
         int count = colIndex.getRowCount();
         for (int i = 0; i < count; i += 1) {
             if (i == row)
                 colIndex.getFlexCellFormatter().setStyleName(i, 0, "index_td_selected_cell");
             else
                 colIndex.getFlexCellFormatter().setStyleName(i, 0, "index_td_cell");
         }
     }
 
     // currently goes through each row and cell and checks to cell value
     public boolean isEmptyRow(int row) {
 
         for (CellColumnHeader header : presenter.getTypeHeaders().getHeaders()) {
             if (header.getCell().getDataForRow(row) != null)
                 return false;
         }
 
         return true;
     }
 
     public boolean validate() {
         return presenter.validateCells();
     }
 
     @Override
     public void clearErrorCell(int row, int col) {
         Widget widget = sheetTable.getWidget(row, col);
         if (widget != null && widget instanceof CellWidget) {
             ((CellWidget) widget).clearError();
         }
     }
 
     @Override
     public void setErrorCell(int row, int col, String errMsg) {
 
         Widget widget = sheetTable.getWidget(row, col);
         if (widget != null && widget instanceof CellWidget) {
             ((CellWidget) widget).showError(errMsg);
         }
     }
 
     @Override
     public void scrollElementToView(int row, int col) {
         Widget widget = sheetTable.getWidget(row, col);
         if (widget == null)
             return;
         widget.getElement().scrollIntoView();
     }
 
     /**
      * Replaces the cell with an input widget that is determined by the type of header
      */
     private void switchToInput() {
         if (cellHasFocus)
             return;
 
         inputIndex = currentIndex;
         inputRow = currentRow;
 
         Widget widget = sheetTable.getWidget(currentRow, currentIndex);
         int tabIndex = -1;
         if (widget instanceof CellWidget) {
             replaced = (CellWidget) widget;
             tabIndex = replaced.getTabIndex();
         }
 
         newCellSelection = presenter.setCellInputFocus(currentRow, currentIndex);
         if (newCellSelection == null)
             return;
 
 
         sheetTable.setWidget(currentRow, currentIndex, newCellSelection.getWidget(currentRow, true, tabIndex));
         // all cell to set focus to whatever their input mechanism is.
         // e.g. if an input box, allow focus on that box
         newCellSelection.setFocus(currentRow);
         cellHasFocus = true;
     }
 
     private void dealWithUpArrowPress() {
         // exit for up arrow press in auto complete box
         CellColumnHeader currentHeader = presenter.getTypeHeaders().getHeaderForIndex(currentIndex);
         if (currentHeader.getCell().hasMultiSuggestions() && cellHasFocus)
             return;
 
         selectCell(currentRow - 1, currentIndex);
     }
 
     private void dealWithDownArrowPress() {
         // exit for down arrow press in auto complete box
         CellColumnHeader currentHeader = presenter.getTypeHeaders().getHeaderForIndex(currentIndex);
         if (currentHeader.getCell().hasMultiSuggestions() && cellHasFocus)
             return;
 
         selectCell(currentRow + 1, currentIndex);
     }
 
     /**
      * cell selection via click or arrow press
      *
      * @param newRow user selected row
      * @param newCol user selected column
      */
     private void selectCell(int newRow, int newCol) {
         if (currentIndex == newCol && currentRow == newRow)
             return;
 
         highlightHeaders(newRow, newCol);
 
         // handle previous selection
         SheetCell prevSelection = newCellSelection;
 
         if (prevSelection != null) {
 
             if (cellHasFocus && replaced != null) {
                 // switch from input
 
                 String data = prevSelection.setDataForRow(currentRow);
                 if (data == null)
                     replaced.setValue("");
                 else
                     replaced.setValue(data);
 
                 sheetTable.setWidget(inputRow, inputIndex, replaced);
 
                 // reset
                 cellHasFocus = false;
                 replaced = null;
                 inputRow = inputIndex = -1;
             }
 
             if (currentIndex != -1 && currentRow != -1) {
                 if (prevSelection.handlesSelection()) {
                     //cell has its own widget on select. this needs to be combined at some point to they all have
                     // their own
                     int tabIndex = (currentRow * presenter.getFieldSize()) + currentIndex + 1;
                     Widget widget = prevSelection.getWidget(currentRow, false, tabIndex);
                     sheetTable.setWidget(currentRow, currentIndex, widget);
                 } else {
 
                     Widget cellWidget = sheetTable.getWidget(currentRow, currentIndex);
                     if (cellWidget instanceof CellWidget)
                         ((CellWidget) cellWidget).setFocus(false);
                 }
             }
         }
 
         // handle current selection
         newCellSelection = presenter.getTypeHeaders().getHeaderForIndex(newCol).getCell();
         Widget widget;
 
         // now deal with current selection
         if (newCellSelection.handlesSelection()) {
             widget = sheetTable.getWidget(newRow, newCol);
             int tabIndex = -1;
 
 //            int tabIndex = (currentRow *  presenter.getFieldSize() ) + currentIndex + 1;
             if (widget instanceof CellWidget) {
                 tabIndex = ((CellWidget) widget).getTabIndex();
             }
             widget = newCellSelection.getWidget(newRow, true, tabIndex);
             sheetTable.setWidget(newRow, newCol, widget);
         } else {
             widget = sheetTable.getWidget(newRow, newCol);
             if (widget instanceof CellWidget) {
                 ((CellWidget) widget).setFocus(true);
             }
 
 
 //            if (cellWidget instanceof Label) {
 //                Label label = (Label) cellWidget;
 //
 //                HTMLPanel panel;
 //                if (label.getText().isEmpty()) {
 //                    panel = new HTMLPanel(
 //                            "<div class=\"cell cell_selected\"><div style=\"position: relative; width: 5px; height:"
 //                                    + "5px; background-color: #0082C0; top: "
 //                                    + "12px; right: -122px; border: 3px solid white; cursor:
 // crosshair\"></div></div>");
 //                }
 //                else {
 //                    panel = new HTMLPanel(
 //                            "<div class=\"cell cell_selected\">"
 //                                    + label.getText()
 //                                    + "<div style=\"position: relative; width: 5px; height: 5px; background-color: "
 //                                    + "#0082C0; top: -2px; right: -124px; border: 3px solid white; cursor:
 // crosshair\"></div></div>");
 //                }
 //                sheetTable.setWidget(newRow, newCol, panel);
 //            }
         }
         widget.getElement().scrollIntoView();
         // update current
         currentRow = newRow;
         currentIndex = newCol;
     }
 
     @Override
     public void setCellWidgetForCurrentRow(String value, int row, int col, int size) {
         sheetTable.setWidget(row, col, new CellWidget(value, row, col, size));
     }
 
     //
     // inner classes
     //
 
     protected class CellClick implements ClickHandler {
 
         @Override
         public void onClick(ClickEvent event) {
             Cell cell = sheetTable.getCellForEvent(event);
             if (cell == null)
                 return;
 
             selectCell(cell.getRowIndex(), cell.getCellIndex());
         }
     }
 
     protected class CellDoubleClick implements DoubleClickHandler {
 
         @Override
         public void onDoubleClick(DoubleClickEvent event) {
             switchToInput();
         }
     }
 }
