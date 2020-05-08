 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.caps;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.caps.AbstractTableCAPs;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.table.Cell;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.ITableAdapter;
 import org.eclipse.jubula.rc.swt.driver.DragAndDropHelperSwt;
 import org.eclipse.jubula.rc.swt.uiadapter.TableAdapter;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Widget;
 /**
  * Toolkit specific commands for the <code>Table</code>
  *
  * @author BREDEX GmbH
  */
 public class TableCAPs extends AbstractTableCAPs {
     
 
     /**
      *  Gets the real table component
      * @return the table
      */
     private Table getTable() {
         return (Table)getComponent().getRealComponent();
     }
     
     /**
      * @return The event thread queuer.
      */
     public IEventThreadQueuer getEventThreadQueuer() {
         return getRobotFactory().getEventThreadQueuer();
     }
     
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         final String[] componentTextArray;
         Item[] itemArray = getTable().getColumns();
         componentTextArray = getTextArrayFromItemArray(itemArray);         
         return componentTextArray;
     }
     
     /**
      * {@inheritDoc}
      */
     protected Object setEditorToReplaceMode(Object editor, boolean replace) {
         if (replace) {
             getRobot().clickAtCurrentPosition(editor, 3, 
                     InputConstants.MOUSE_BUTTON_LEFT);
         } else {
            getRobot().clickAtCurrentPosition(editor, 1, 
                     InputConstants.MOUSE_BUTTON_LEFT);
         }
         return editor;
     }
 
     /**
      * {@inheritDoc}
      */
     protected Object activateEditor(Cell cell, Rectangle rectangle) {
         TableAdapter table = (TableAdapter) getComponent();
         return table.activateEditor(cell);
     }
 
     /**
      * {@inheritDoc}
      */
     protected Rectangle scrollCellToVisible(final int row, final int col)
         throws StepExecutionException {
         final Table table = getTable();
         getEventThreadQueuer().invokeAndWait("scrollCellToVisible", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         if (table.getColumnCount() > 0 || col > 0) {
                             table.showColumn(table.getColumn(col));
                         }
                         table.showItem(table.getItem(row));
                         return null;
                     }
                 });
 
         final Rectangle cellBoundsRelativeToParent = getCellBounds(row, col);
             
         getEventThreadQueuer().invokeAndWait("getCellBoundsRelativeToParent", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     org.eclipse.swt.graphics.Point cellOriginRelativeToParent = 
                         table.getDisplay().map(
                                 table, table.getParent(), 
                                 new org.eclipse.swt.graphics.Point(
                                         cellBoundsRelativeToParent.x, 
                                         cellBoundsRelativeToParent.y));
                     cellBoundsRelativeToParent.x = 
                         cellOriginRelativeToParent.x;
                     cellBoundsRelativeToParent.y = 
                         cellOriginRelativeToParent.y;
                     return null;
                 }
             });
 
         Control parent = (Control)getEventThreadQueuer().invokeAndWait("getParent", //$NON-NLS-1$
                 new IRunnable() {
                 public Object run() {
                     table.getParent();
                     return null;
                 }
             });
 
             
         getRobot().scrollToVisible(
                 parent, cellBoundsRelativeToParent);
         
         return getVisibleBounds(getCellBounds(row, col));
     }
 
     /**
      * {@inheritDoc}
      */
     protected int getExtendSelectionModifier() {
         return SWT.MOD1;
     }
 
     /**
      * {@inheritDoc}
      */
     protected Cell getCellAtMousePosition() throws StepExecutionException {
         
         final Table table = getTable();
         final Point awtMousePos = getRobot().getCurrentMousePosition();
         Cell returnvalue = (Cell) getEventThreadQueuer().invokeAndWait(
                 "getCellAtMousePosition",  //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         Cell cell = null;
                         final int itemCount = table.getItemCount();
                         for (int rowCount = table.getTopIndex(); 
                                 rowCount < itemCount; rowCount++) {
                             if (cell != null) {
                                 break;
                             }
                             final int columnCount = table.getColumnCount();
                             if (columnCount > 0) {
                                 for (int col = 0; col < columnCount; col++) {
                                     final Rectangle itemBounds = getCellBounds(
                                             rowCount, col);
                                     final org.eclipse.swt.graphics.Point 
                                         absItemBounds = table
                                             .toDisplay(itemBounds.x,
                                                     itemBounds.y);
                                     final Rectangle absRect = new Rectangle(
                                             absItemBounds.x, absItemBounds.y,
                                             itemBounds.width,
                                             itemBounds.height);
                                     if (absRect.contains(awtMousePos)) {
                                         cell = new Cell(rowCount, col);
                                         break;
                                     }
                                 }
                             } else {
                                 final Rectangle itemBounds = getCellBounds(
                                         rowCount, 0);
                                 final org.eclipse.swt.graphics.Point 
                                     absItemBounds = table
                                         .toDisplay(itemBounds.x, itemBounds.y);
                                 final Rectangle absRect = new Rectangle(
                                         absItemBounds.x, absItemBounds.y,
                                         itemBounds.width, itemBounds.height);
                                 if (absRect.contains(awtMousePos)) {
                                     cell = new Cell(rowCount, 0);
                                 }
                             }
                         }
                         if (cell == null) {
                             throw new StepExecutionException(
                                     "No cell under mouse position found!", //$NON-NLS-1$
                                     EventFactory
                                             .createActionError(
                                                     TestErrorEvent.NOT_FOUND));
                         }
                         return cell;
                     }
                 });
         return returnvalue;
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean isMouseOnHeader() {
         final Table table = getTable();
         final ITableAdapter adapter = (ITableAdapter)getComponent();
         Boolean isVisible;
         isVisible = (Boolean)getEventThreadQueuer().invokeAndWait(
                 "isMouseOnHeader", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return new Boolean(table.getHeaderVisible());
                     }
                 });
         
         if (!(isVisible.booleanValue())) {
             return false;
         }
         
         Boolean isOnHeader = new Boolean(false);
         isOnHeader = (Boolean)getEventThreadQueuer().invokeAndWait(
                 "isMouseOnHeader", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         final Point awtMousePos = getRobot()
                             .getCurrentMousePosition();
                         org.eclipse.swt.graphics.Point mousePos =
                             new org.eclipse.swt.graphics.Point(
                                 awtMousePos.x, awtMousePos.y);
 
                         for (int j = 0; j < table.getColumnCount(); j++) {
                             final Rectangle constraints = 
                                     adapter.getHeaderBounds(j);
                             
                             org.eclipse.swt.graphics.Rectangle bounds = 
                                     SwtUtils.getWidgetBounds(
                                     table);
                             
                             if (constraints != null) {
                                 // Use SWT's mapping function, if possible, as it is more
                                 // multi-platform than simply adding the x and y values.
                                 org.eclipse.swt.graphics.Point
                                 convertedLocation = getConvertedLocation(
                                         constraints);
                                 bounds.x = convertedLocation.x;
                                 bounds.y = convertedLocation.y;
                                 
                                 bounds.height = constraints.height;
                                 bounds.width = constraints.width;
                             }
 
                             if (bounds.contains(mousePos)) {
                                 return new Boolean(true);
                             }
                         }      
                         return new Boolean(false);
                     }
                 });                  
         
         return isOnHeader.booleanValue();
     }
     
     /**
      * Returns an array of representation strings that corresponds to the given
      * array of items or null if the given array is null;
      * @param itemArray the item array whose item texts have to be read
      * @return array of item texts corresponding to the given item array
      */
     protected final String[] getTextArrayFromItemArray(Item[] itemArray) {
         final String[] itemTextArray;
         if (itemArray == null) {
             itemTextArray = null;
         } else {
             itemTextArray = new String[itemArray.length];
             for (int i = 0; i < itemArray.length; i++) {
                 Item item = itemArray[i];
                 if (item == null) {
                     itemTextArray[i] = null;
                 } else {
                     itemTextArray[i] = SwtUtils.removeMnemonics(item.getText());
                 }
             }
         }
         
         return itemTextArray;
     }
     
     /**
      * Computes the visible cellBounds inside the visible bounds of the table.<br>
      * The result is the intersection of the visible bounds of the table and the 
      * bounds of the cell.
      * @param cellBounds the bounds of the cell to click in. These bounds must
      *                  be relative to the table's location.
      * @return the visible cell bounds, relative to the table's location.
      */
     private Rectangle getVisibleBounds(Rectangle cellBounds) {
         org.eclipse.swt.graphics.Rectangle r = 
             (org.eclipse.swt.graphics.Rectangle)
             getEventThreadQueuer().invokeAndWait("getVisibleCellBounds: " + cellBounds,  //$NON-NLS-1$
                     new IRunnable() {
 
                     public Object run() {
                         return getTable().getClientArea();
                     }
                 });
         
         Rectangle visibleTableBounds = new Rectangle(
             r.x, r.y, r.width, r.height);
         Rectangle visibleCellBounds = 
             visibleTableBounds.intersection(cellBounds);
         return visibleCellBounds;
     }
     
     /**
      * @param constraints Rectangle
      * @return converted Location of table
      */
     private org.eclipse.swt.graphics.Point getConvertedLocation(
             final Rectangle constraints) {
         org.eclipse.swt.graphics.Point convertedLocation =
             (org.eclipse.swt.graphics.Point)getEventThreadQueuer()
                 .invokeAndWait("toDisplay", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return getTable().toDisplay(
                                 constraints.x, constraints.y);
                     }
                 });
         return convertedLocation;
     }
         
     /**
      * 
      * @param row   The row of the cell
      * @param col   The column of the cell
      * @return The bounding rectangle for the cell, relative to the table's 
      *         location.
      */
     private Rectangle getCellBounds(final int row, final int col) {
         final Table table = getTable();
         Rectangle cellBounds = (Rectangle)getEventThreadQueuer().invokeAndWait(
                 "evaluateCellBounds", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(row, col);
                         TableItem ti = table.getItem(row);
                         int column = (table.getColumnCount() > 0 || col > 0) 
                             ? col : 0;
                         org.eclipse.swt.graphics.Rectangle r = 
                                 ti.getBounds(column);
                         String text = ti.getText(column);
                         Image image = ti.getImage(column);
                         if (text != null && text.length() != 0) {
                             GC gc = new GC(table);
                             int charWidth = 0; 
                             try {
                                 FontMetrics fm = gc.getFontMetrics();
                                 charWidth = fm.getAverageCharWidth();
                             } finally {
                                 gc.dispose();
                             }
                             r.width = text.length() * charWidth;
                             if (image != null) {
                                 r.width += image.getBounds().width;
                             }
                         } else if (image != null) {
                             r.width = image.getBounds().width;
                         }
                         if (column > 0) {
                             TableColumn tc = table.getColumn(column);
                             int alignment = tc.getAlignment();
                             if (alignment == SWT.CENTER) {
                                 r.x += ((double)tc.getWidth() / 2) 
                                         - ((double)r.width / 2);
                             }
                             if (alignment == SWT.RIGHT) {
                                 r.x += tc.getWidth() - r.width;
                             }
                         }
                         
                         return new Rectangle(r.x, r.y, r.width, r.height);
                     }
                 });
         return cellBounds;
     }
     /**
      * {@inheritDoc}
      */
     protected Object getSpecificRectangle(Rectangle rectangle) {
         return new org.eclipse.swt.graphics.Rectangle(rectangle.x, rectangle.y,
                 rectangle.width, rectangle.height);
     }
     
     /**
      * {@inheritDoc}
      */
     public void gdClickDirect(int count, int button, int xPos, String xUnits, 
         int yPos, String yUnits) throws StepExecutionException {
         
         int correctedYPos = correctYPos(yPos, yUnits);
         super.gdClickDirect(count, button, xPos, xUnits, correctedYPos, yUnits);
     }
     
     /**
      * Corrects the given Y position based on the height of the table's header.
      * This ensures, for example, that test steps don't try to click within the
      * table header (where we receive no confirmation events).
      * 
      * @param pos The Y position to correct.
      * @param units The units used for the Y position.
      * @return The corrected Y position.
      */
     private int correctYPos(int pos, String units) {
         int correctedPos = pos;
         int headerHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                 "getHeaderHeight", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         return new Integer(
                             ((Table)getComponent().getRealComponent())
                             .getHeaderHeight());
                     }
             
                 })).intValue();
 
         if (POS_UNIT_PIXEL.equalsIgnoreCase(units)) {
             // Pixel units
             correctedPos += headerHeight;
         } else {
             // Percentage units
             int totalHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                     "getWidgetBounds", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             return new Integer(
                                 SwtUtils.getWidgetBounds(
                                     (Widget) getComponent().
                                         getRealComponent()).height);
                         }
             
                     })).intValue();
             long targetHeight = totalHeight - headerHeight;
             long targetPos = Math.round((double)targetHeight * (double)pos
                 / 100.0);
             targetPos += headerHeight;
             double heightPercentage = 
                 (double)targetPos / (double)totalHeight * 100.0;
             correctedPos = (int)Math.round(heightPercentage);
             if (correctedPos > 100) { // rounding error
                 correctedPos = 100;
             }
         }
         return correctedPos;
     }
     
     /**
      * Drags the cell of the Table.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the 
      * cell can be defined.
      * 
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param row The row of the cell.
      * @param rowOperator the row header operator
      * @param col The column of the cell.
      * @param colOperator the column header operator
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdDragCell(final int mouseButton, final String modifier, 
             final String row, String rowOperator, final String col,
             final String colOperator, final int xPos, final String xUnits,
             final int yPos, final String yUnits) 
         throws StepExecutionException {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectCell(row, rowOperator, col, colOperator, 0, xPos,
                 xUnits, yPos, yUnits, 
                 CompSystemConstants.EXTEND_SELECTION_NO, 1);
     }
     
     /**
      * Drops on the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the 
      * cell can be defined.
      * 
      * @param row The row of the cell.
      * @param rowOperator The row operator
      * @param col The column of the cell.
      * @param colOperator The column operator
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdDropCell(final String row, final String rowOperator,
             final String col, final String colOperator, final int xPos, 
             final String xUnits, final int yPos, final String yUnits,
             int delayBeforeDrop) throws StepExecutionException {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCell", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     CAPUtil.shakeMouse();
 
                     // drop
                     gdSelectCell(row, rowOperator, col, colOperator, 0, xPos,
                             xUnits, yPos, yUnits, 
                             CompSystemConstants.EXTEND_SELECTION_NO, 1);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drags this row.
      * 
      * @param mouseButton the mouse button
      * @param modifier the modifier
      * @param col the column
      * @param colOperator the column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragRowByValue(int mouseButton, String modifier, String col,
             String colOperator, final String value, final String regexOp,
             final String searchType) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectRowByValue(col, colOperator, value, regexOp, 1, 
                 CompSystemConstants.EXTEND_SELECTION_NO,
                 searchType, 1);
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drops on this row.
      * 
      * @param col the column
      * @param colOperator the column operator
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropRowByValue(final String col, final String colOperator,
             final String value, final String regexOp, final String searchType, 
             int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         
         try {
             getEventThreadQueuer().invokeAndWait("gdDropRowByValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     CAPUtil.shakeMouse();
 
                     // drop
                     gdSelectRowByValue(col, colOperator, value, regexOp,
                             CompSystemConstants.EXTEND_SELECTION_NO, 
                             searchType, 
                             ClickOptions.create().setClickCount(0));
                     return null;
                 }            
             });
             
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drags the cell.
      * 
      * @param mouseButton the mouse button
      * @param modifier the modifiers
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragCellByColValue(int mouseButton, String modifier,
             String row, String rowOperator, final String value,
             final String regex, final String searchType) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdSelectCellByColValue(row, rowOperator, value, regex, 
                 CompSystemConstants.EXTEND_SELECTION_NO,
                 searchType, ClickOptions.create().setClickCount(0));
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drops on the cell.
      * 
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropCellByColValue(final String row, final String rowOperator,
             final String value, final String regex, final String searchType,
             int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
 
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCellByColValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     CAPUtil.shakeMouse();
 
                     // drop
                     gdSelectCellByColValue(row, rowOperator, value, regex,
                             CompSystemConstants.EXTEND_SELECTION_NO, 
                             searchType, 
                             ClickOptions.create().setClickCount(0));
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
         
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
 }
