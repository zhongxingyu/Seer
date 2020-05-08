 /*
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package org.apache.poi.xwpf.converter.xwpf.bo;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.util.Units;
 import org.apache.poi.xwpf.converter.xwpf.common.ConversionUtil;
 import org.apache.poi.xwpf.converter.xwpf.common.ElementType;
 import org.apache.poi.xwpf.converter.xwpf.common.HTMLConstants;
 import org.apache.poi.xwpf.usermodel.XWPFDocument;
 import org.apache.poi.xwpf.usermodel.XWPFTable;
 import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
 import org.apache.poi.xwpf.usermodel.XWPFTableCell;
 import org.apache.poi.xwpf.usermodel.XWPFTableRow;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
 
 /**
  * This class encapsulates a table parsing element.
  * 
  * @author Anton
  * 
  */
 public class TableParsingElement extends AbstractParsingElement {
 
 	private XWPFTable docxTable;
 	private Map<Integer, ArrayList<TableCellParsingElement>> rowSpanCells = Collections
 			.synchronizedMap(new HashMap<Integer, ArrayList<TableCellParsingElement>>());
 
 	/**
 	 * Constructor
 	 * 
 	 * @param topLevelElement
 	 *            indicates if top level element or sub-element
 	 * @param document
 	 *            document
 	 */
 	public TableParsingElement(boolean topLevelElement, XWPFDocument document) {
 		super(ElementType.TABLE, topLevelElement, document);
 		docxTable = document.createTable();
 
 		// Remove default rows
 		for (int i = 0; i <= docxTable.getNumberOfRows(); i++) {
 			docxTable.removeRow(i);
 		}
 	}
 
 	/**
 	 * @return the docxTable
 	 */
 	public XWPFTable getDocxTable() {
 		return docxTable;
 	}
 
 	/**
 	 * @param docxTable
 	 *            the docxTable to set
 	 */
 	public void setDocxTable(XWPFTable docxTable) {
 		this.docxTable = docxTable;
 	}
 
 	/**
 	 * This method sets border on the table
 	 * 
 	 * @param visible
 	 *            visible if true
 	 * @param thick
 	 *            if true, thick
 	 * @param size
 	 *            size
 	 * @param space
 	 *            space
 	 */
 	public void setBorder(boolean visible, boolean thick, int size, int space) {
 		XWPFBorderType borderToUse = XWPFBorderType.SINGLE;
 		String color = HTMLConstants.COLOR_WHITE;
 		if (thick) {
 			borderToUse = XWPFBorderType.THICK;
 		}
 		if (visible) {
 			color = HTMLConstants.COLOR_GREY;
 		}
 		// System.out.println("setBorder. visible=" + visible + "; thick=" +
 		// thick
 		// + "; size=" + size + "; color=" + color);
 		docxTable.setInsideHBorder(borderToUse, Units.toEMU(size),
 				Units.toEMU(space), color);
 		docxTable.setInsideVBorder(borderToUse, Units.toEMU(size),
 				Units.toEMU(space), color);
 	}
 
 	/**
 	 * Sets cell margins
 	 * 
 	 * @param top
 	 *            top margin
 	 * @param left
 	 *            left margin
 	 * @param bottom
 	 *            bottom margin
 	 * @param right
 	 *            right margin
 	 */
 	public void setCellMargins(int top, int left, int bottom, int right) {
 		docxTable.setCellMargins(Units.toEMU(top), Units.toEMU(left),
 				Units.toEMU(bottom), Units.toEMU(right));
 		// System.out.println("setCellMargins. top=" + top + "; left=" + left
 		// + "; bottom" + bottom + "; right=" + right);
 	}
 
 	/**
 	 * This method sets width.
 	 * 
 	 * @param width
 	 *            table width
 	 * @param usePercentage
 	 *            if true, use percentage instead of pixels
 	 */
 	public void setWidth(double width, boolean usePercentage) {
 
 		if (usePercentage) {
 			BigInteger documentWidth = this.getDocumentWidth();
 			int tableWidth = (int) ((documentWidth.intValue() / 100) * width);
 			docxTable.setWidth(tableWidth);
 
 		} else {
			// docxTable.setWidth(Units.toEMU(width));
			docxTable.setWidth((int) ConversionUtil
					.convertPixelsTo20thPoints(width));
 		}
		// System.out.println(" Set table width to: "+docxTable.getWidth());
 	}
 
 	/**
 	 * This method adds row span cell information to the rowspan collection.
 	 * 
 	 * @param rowNum
 	 *            row number
 	 * @param startingCell
 	 *            first cell in the row span
 	 */
 	public void addRowSpanCell(int rowNum, TableCellParsingElement startingCell) {
 		Integer key = new Integer(rowNum);
 		ArrayList<TableCellParsingElement> values = this.rowSpanCells.get(key);
 
 		if (values == null) {
 			values = new ArrayList<TableCellParsingElement>();
 		}
 
 		if (!values.contains(startingCell)) {
 			values.add(startingCell);
 		}
 		this.rowSpanCells.put(key, values);
 
 		// System.out.println("Added new row span cell to the map at: " + key
 		// + " with values: " + values.toArray());
 	}
 
 	/**
 	 * This method checks if row span collection contains a cell in a particular
 	 * position.
 	 * 
 	 * @param rowNum
 	 *            row number
 	 * @param cellNum
 	 *            cell number
 	 * @return if true, contains a cell in a particular position
 	 */
 	public boolean containsRowCellAtPosition(int rowNum, int cellNum) {
 		boolean result = false;
 
 		Integer key = new Integer(rowNum);
 
 		ArrayList<TableCellParsingElement> values = this.rowSpanCells.get(key);
 
 		if (values != null) {
 
 			for (TableCellParsingElement cell : values) {
 				if (cell.getRowSpanCellNumber() == cellNum) {
 					result = true;
 					break;
 				}
 			}
 		}
 
 		// System.out.println("Checking if has rowspan at rowNum: " + rowNum
 		// + " with cellNum: " + cellNum + "; result = " + result);
 
 		return result;
 	}
 
 	/**
 	 * This method returns first row span cell so it could be used as a template
 	 * for next cells.
 	 * 
 	 * @param rowNum
 	 *            row number
 	 * @param cellNum
 	 *            cell number
 	 * @return first row span cell
 	 */
 	public TableCellParsingElement getFirstRowSpanCell(int rowNum, int cellNum) {
 		TableCellParsingElement result = null;
 
 		Integer key = new Integer(rowNum);
 
 		ArrayList<TableCellParsingElement> values = this.rowSpanCells.get(key);
 
 		for (TableCellParsingElement cell : values) {
 			if (cell.getRowSpanCellNumber() == cellNum) {
 				result = cell;
 				break;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @return the rowSpanCells
 	 */
 	public Map<Integer, ArrayList<TableCellParsingElement>> getRowSpanCells() {
 		return rowSpanCells;
 	}
 
 	/**
 	 * @param rowSpanCells
 	 *            the rowSpanCells to set
 	 */
 	public void setRowSpanCells(
 			Map<Integer, ArrayList<TableCellParsingElement>> rowSpanCells) {
 		this.rowSpanCells = rowSpanCells;
 	}
 
 	/**
 	 * This method finalizes table's meta data when the table is fully
 	 * populated. The method is only needed for future PDF conversion because
 	 * PDF converter expects this data. Setting this meta data variables does
 	 * not have any impact on generated POI word document.
 	 */
 	public void populateMetaDataUponCompletion() {
 
 		CTTbl cTTbl = docxTable.getCTTbl();
 		List<CTTblGridCol> cols = null;
 		CTTblGrid grid = null;
 
 		if (cTTbl != null) {
 
 			this.getCTTblPr(cTTbl);
 
 			grid = this.getCTTblGrid(cTTbl);
 			cols = grid.getGridColList();
 
 		}
 
 		for (XWPFTableRow row : this.docxTable.getRows()) {
 
 			int diff = row.getTableCells().size() - cols.size();
 
 			for (int i = 0; i < diff; i++) {
 				grid.addNewGridCol();
 			}
 
 			int i = 0;
 
 			for (XWPFTableCell cell : row.getTableCells()) {
 
 				CTTblGridCol gridColumn = cols.get(i);
 
 				if (cell.getCTTc().getTcPr().getTcW().getW().intValue() == 0) {
 					BigInteger columnWidth = this.getDocumentWidth().divide(
 							BigInteger.valueOf(row.getTableCells().size()));
 					cell.getCTTc().getTcPr().getTcW().setW(columnWidth);
 					cell.getCTTc().getTcPr().getTcW().setType(STTblWidth.DXA);
 					gridColumn.setW(columnWidth);
 
 				}
 				i++;
 			}
		}

		List<CTTblGridCol> tableCols = grid.getGridColList();
		for (CTTblGridCol col : tableCols) {
			// System.out.println("tblGridCol.getW()=" + col.getW());
			if (col.getW() == null) {
				BigInteger columnWidth = this.getDocumentWidth().divide(
						BigInteger.valueOf(tableCols.size()));
				col.setW(columnWidth);

			}
		}

 	}
 
 	/**
 	 * This method returns existing or creates new CTTblGrid.
 	 * 
 	 * @param cTTbl
 	 *            CTTbl
 	 * @return CTTblGrid
 	 */
 	private CTTblGrid getCTTblGrid(CTTbl cTTbl) {
 		CTTblGrid grid = cTTbl.getTblGrid();
 
 		if (grid == null) {
 			grid = cTTbl.addNewTblGrid();
 		}
 		return grid;
 	}
 
 	/**
 	 * This method returns existing or adds new CTTblPr
 	 * 
 	 * @param cTTbl
 	 *            CTTblPr
 	 * @return CTTblPr
 	 */
 	private CTTblPr getCTTblPr(CTTbl cTTbl) {
 		CTTblPr cTTblpr = cTTbl.getTblPr();
 		if (cTTblpr == null) {
 			cTTblpr = docxTable.getCTTbl().addNewTblPr();
 		}
 		return cTTblpr;
 	}
 
 	/**
 	 * This method returns document width.
 	 * 
 	 * @return document width
 	 */
 	private BigInteger getDocumentWidth() {
 		CTPageSz pageSize = this.getDocument().getDocument().getBody()
 				.getSectPr().getPgSz();
 		BigInteger documentWidth = pageSize.getW();
 		return documentWidth;
 	}
 
 }
