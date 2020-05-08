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
 
 import org.apache.poi.xwpf.converter.xwpf.common.ConversionUtil;
 import org.apache.poi.xwpf.converter.xwpf.common.ElementType;
 import org.apache.poi.xwpf.converter.xwpf.common.HTMLConstants;
 import org.apache.poi.xwpf.usermodel.XWPFDocument;
 import org.apache.poi.xwpf.usermodel.XWPFTableCell;
 import org.apache.poi.xwpf.usermodel.XWPFTableRow;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
 
 /**
  * This class encapsulates a table cell parsing element.
  * 
  * @author Anton
  * 
  */
 public class TableCellParsingElement extends AbstractParsingElement {
 
 	private XWPFTableRow docxTableRow;
 	private XWPFTableCell docxTableCell;
 	private TableRowParsingElement tableRowParsingElement;
 	private int rowSpanCellNumber;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param tableRowParsingElement
 	 *            table row parsing element
 	 * @param document
 	 *            document
 	 */
 	public TableCellParsingElement(
 			TableRowParsingElement tableRowParsingElement, XWPFDocument document) {
 		super(ElementType.TABLE_CELL, false, document);
 		super.setMayContainParagraph(true);
 		this.tableRowParsingElement = tableRowParsingElement;
 		this.docxTableRow = tableRowParsingElement.getDocxTableRow();
 
 		this.addRowSpanCellIfNeeded();
 
 		this.docxTableCell = this.createNewCell();
 		// System.out.println("Created new CELL");
 
 	}
 
 	/**
 	 * This method creates new cell.
 	 * 
 	 * @return new cell
 	 */
 	private XWPFTableCell createNewCell() {
 		XWPFTableCell cell = this.docxTableRow.createCell();
 		CTTc cttc = cell.getCTTc();
 		CTTcPr cTTcPr = this.getCTTcPr(cttc);
 		this.createVisibleBorder(cTTcPr, HTMLConstants.COLOR_GREY);
 		CTTblWidth cTTblWidth = this.getCTTblWidth(cTTcPr);
 		cTTblWidth.setW(new BigInteger("0"));
 		cTTblWidth.setType(STTblWidth.AUTO);
 		this.addNewGridSpanColumn();
 		return cell;
 	}
 
 	/**
 	 * This method adds row span cell if needed.
 	 */
 	private void addRowSpanCellIfNeeded() {
 		int rowNum = this.docxTableRow.getTable().getNumberOfRows();
 		int cellNum = this.docxTableRow.getTableCells().size();
 		if (this.tableRowParsingElement.getTableParsingElement()
 				.containsRowCellAtPosition(rowNum, cellNum)) {
 
 			// System.out.println("Creating new rowspan cell at positions:"
 			// + this.docxTableRow.getTable().getNumberOfRows() + "; "
 			// + this.docxTableRow.getTableCells().size());
 			XWPFTableCell rowSpanCell = this.createNewCell();
 
 			TableCellParsingElement firstRowSpanCell = this.tableRowParsingElement
 					.getTableParsingElement().getFirstRowSpanCell(rowNum,
 							cellNum);
 
 			CTTcPr pr = this.getCTTcPr(rowSpanCell.getCTTc());
 
 			if (firstRowSpanCell.getDocxTableCell().getCTTc().getTcPr() != null
 					&& firstRowSpanCell.getDocxTableCell().getCTTc().getTcPr()
 							.getTcW() != null) {
 				pr.setTcW(firstRowSpanCell.getDocxTableCell().getCTTc()
 						.getTcPr().getTcW());
 
 			}
 
 			if (firstRowSpanCell.getDocxTableCell().getCTTc().getTcPr() != null
 					&& firstRowSpanCell.getDocxTableCell().getCTTc().getTcPr()
 							.getGridSpan() != null) {
 				pr.setGridSpan(firstRowSpanCell.getDocxTableCell().getCTTc()
 						.getTcPr().getGridSpan());
 			}
 
 			CTVMerge merge = pr.getVMerge();
 			if (merge == null) {
 				merge = pr.addNewVMerge();
 			}
 
 			merge.setVal(STMerge.CONTINUE);
 
 			this.createVisibleBorder(pr, HTMLConstants.COLOR_GREY);
 
 		}
 	}
 
 	/**
 	 * This method creates new or returns existing CTTcPr.
 	 * 
 	 * @param cttc
 	 *            CTTc
 	 * @return new or existing CTTcPr
 	 */
 	private CTTcPr getCTTcPr(CTTc cttc) {
 		CTTcPr pr = cttc.getTcPr();
 		if (pr == null) {
 			pr = cttc.addNewTcPr();
 		}
 		return pr;
 	}
 
 	/**
 	 * This method adds new grid span column.
 	 */
 	private void addNewGridSpanColumn() {
 
 		int rowNum = this.docxTableRow.getTable().getNumberOfRows();
 		if (rowNum == 1 && this.docxTableRow.getTable().getCTTbl() != null
 				&& this.docxTableRow.getTable().getCTTbl().getTblGrid() == null) {
 			CTTblGrid grid = this.docxTableRow.getTable().getCTTbl()
 					.addNewTblGrid();
 			grid.addNewGridCol();
 		}
 
 	}
 
 	/**
 	 * This class creates visible border. The default color is grey.
 	 * 
 	 * @param pr
 	 *            CTTcPr
 	 * @param color
 	 *            HTML color to use
 	 */
 	private void createVisibleBorder(CTTcPr pr, String color) {
 		CTTcBorders borders = pr.addNewTcBorders();
 		CTBorder border = borders.addNewBottom();
 		border.setVal(STBorder.THICK);
 		border.setColor(color);
 		CTBorder border1 = borders.addNewLeft();
 		border1.setVal(STBorder.THICK);
 		border1.setColor(color);
 		CTBorder border2 = borders.addNewRight();
 		border2.setVal(STBorder.THICK);
 		border2.setColor(color);
 		CTBorder border3 = borders.addNewTop();
 		border3.setVal(STBorder.THICK);
 		border3.setColor(color);
 	}
 
 	/**
 	 * @return the docxTableRow
 	 */
 	public XWPFTableRow getDocxTableRow() {
 		return docxTableRow;
 	}
 
 	/**
 	 * @param docxTableRow
 	 *            the docxTableRow to set
 	 */
 	public void setDocxTableRow(XWPFTableRow docxTableRow) {
 		this.docxTableRow = docxTableRow;
 	}
 
 	/**
 	 * @return the docxTableCell
 	 */
 	public XWPFTableCell getDocxTableCell() {
 		return docxTableCell;
 	}
 
 	/**
 	 * @param docxTableCell
 	 *            the docxTableCell to set
 	 */
 	public void setDocxTableCell(XWPFTableCell docxTableCell) {
 		this.docxTableCell = docxTableCell;
 	}
 
 	/**
 	 * This method sets height
 	 * 
 	 * @param height
 	 *            height
 	 * @param usePercentage
 	 *            if true, use percentage instead of pixels
 	 */
 	public void setHeight(double height, boolean usePercentage) {
 		if (usePercentage) {
 			CTPageSz pageSize = this.getDocument().getDocument().getBody()
 					.getSectPr().getPgSz();
 			BigInteger documentHeight = pageSize.getH();
			int tableRowHeight = (int) ( (documentHeight.intValue() / 100) * height);
 			this.docxTableRow.setHeight(tableRowHeight);
 
 		} else {
			this.docxTableRow.setHeight((int)height);
 		}
 	}
 
 	/**
 	 * This method sets width
 	 * 
 	 * @param width
 	 *            width
 	 * @param usePercentage
 	 *            if true, use percentage instead of pixels
 	 */
 	public void setWidth(double width, boolean usePercentage) {
 
 		CTTcPr cTTcPr = this.getCTTcPr(this.docxTableCell.getCTTc());
 		CTTblWidth cTTblWidth = this.getCTTblWidth(cTTcPr);
 		cTTblWidth.setType(STTblWidth.DXA);
 
 		if (usePercentage) {
 			CTPageSz pageSize = this.getDocument().getDocument().getBody()
 					.getSectPr().getPgSz();
 			BigInteger documentWidth = pageSize.getW();
			int tableWidth = (int) ( (documentWidth.intValue() / 100) * width) ;
 			cTTblWidth.setW(BigInteger.valueOf(tableWidth));
 
 		} else {
 			cTTblWidth.setW(BigInteger.valueOf(ConversionUtil
 					.convertTableCellPixelsToWidthUnits(width)));
 		}
 	}
 
 	/**
 	 * This method adds new or returns existing CTTblWidth.
 	 * 
 	 * @param cTTcPr
 	 *            CTTcPr
 	 * @return new or xisting CTTblWidth
 	 */
 	private CTTblWidth getCTTblWidth(CTTcPr cTTcPr) {
 		CTTblWidth cTTblWidth = cTTcPr.getTcW();
 		if (cTTblWidth == null) {
 			cTTblWidth = cTTcPr.addNewTcW();
 		}
 		return cTTblWidth;
 	}
 
 	/**
 	 * This method sets column span.
 	 * 
 	 * @param columnSpan
 	 *            column span
 	 */
 	public void setColumnSpan(int columnSpan) {
 		CTTc cttc = this.docxTableCell.getCTTc();
 		CTTcPr cTTcPr = getCTTcPr(cttc);
 
 		CTDecimalNumber gridSpan = cTTcPr.getGridSpan();
 
 		if (gridSpan == null) {
 			gridSpan = cTTcPr.addNewGridSpan();
 		}
 		gridSpan.setVal(BigInteger.valueOf(columnSpan));
 
 	}
 
 	/**
 	 * This method sets row span.
 	 * 
 	 * @param rowSpan
 	 *            row span
 	 */
 	public void setRowSpan(int rowSpan) {
 
 		// System.out.println("Setting rowSpan="+rowSpan);
 		if (rowSpan > 1) {
 			int currentRow = this.docxTableRow.getTable().getNumberOfRows();
 			this.rowSpanCellNumber = this.docxTableRow.getTableCells().size() - 1;
 			for (int i = 0; i < rowSpan; i++) {
 				this.tableRowParsingElement.getTableParsingElement()
 						.addRowSpanCell((currentRow + i), this);
 			}
 			CTTc cttc = this.docxTableCell.getCTTc();
 			CTTcPr cTTcPr = getCTTcPr(cttc);
 
 			CTVMerge merge = cTTcPr.getVMerge();
 			if (merge == null) {
 				merge = cTTcPr.addNewVMerge();
 			}
 
 			merge.setVal(STMerge.RESTART);
 
 		}
 	}
 
 	/**
 	 * @return the tableRowParsingElement
 	 */
 	public TableRowParsingElement getTableRowParsingElement() {
 		return tableRowParsingElement;
 	}
 
 	/**
 	 * @param tableRowParsingElement
 	 *            the tableRowParsingElement to set
 	 */
 	public void setTableRowParsingElement(
 			TableRowParsingElement tableRowParsingElement) {
 		this.tableRowParsingElement = tableRowParsingElement;
 	}
 
 	/**
 	 * @return the rowSpanCellNumber
 	 */
 	public int getRowSpanCellNumber() {
 		return rowSpanCellNumber;
 	}
 
 	/**
 	 * @param rowSpanCellNumber
 	 *            the rowSpanCellNumber to set
 	 */
 	public void setRowSpanCellNumber(int rowSpanCellNumber) {
 		this.rowSpanCellNumber = rowSpanCellNumber;
 	}
 
 }
