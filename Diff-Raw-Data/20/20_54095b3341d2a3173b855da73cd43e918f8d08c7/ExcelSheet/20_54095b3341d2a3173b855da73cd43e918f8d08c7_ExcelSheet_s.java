 package excel;
 
 import java.util.Iterator;
 
 import org.apache.poi.ss.formula.functions.Column;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 
 /**
  * @author KarelPetranek
  * Represents one sheet in Excel workbook
  */
 public class ExcelSheet {
 	private Sheet sheet;
 	
 	/**
 	 * Creates a sheet from the given POI sheet
 	 * @param sheet The POI sheet
 	 */
 	public ExcelSheet(Sheet sheet)  {
 		this.sheet = sheet;
 	}
 	
 	/**
 	 * String value of the cell at the given coordinates. If the cell is not of string type,
 	 * a conversion is performed automatically. Never returns null.
 	 * @param x Column
 	 * @param y Row
 	 * @return String representation of the given cell
 	 */
 	public String stringAt(int x, int y)  {
 		return cellAt(x, y).asString();
 	}
 	
 	/**
 	 * Returns a cell at the given coordinates
 	 * @param x Column
 	 * @param y Row
 	 * @return Cell at the given coordinates
 	 */
 	public ExcelCell cellAt(int x, int y)  {
 		if (sheet == null)
 			return new ExcelCell();
 		
 		Row row = sheet.getRow(y);
 		if (row == null)
 			return new ExcelCell();
 		return new ExcelCell(row.getCell(x));
 	}
 	
 	/**
 	 * Returns row at the given index. The row contains only elements between the first and last non-blank cells.
 	 * @param index Index of the row
 	 * @return Row at the given index
 	 */
 	public ExcelRow getRow(int index)  {
 		ExcelRow res = new ExcelRow(index);
 		if (sheet == null)
 			return res;
 		
 		Row row = sheet.getRow(index);
 		if (row == null)
 			return res;
 		
 		int lastNonEmptyCell = -1;
 		lastNonEmptyCell = fillRawRow(row, res);
 		
		res.removeRange(lastNonEmptyCell, res.size());
 		
 		return res;
 	}
 	
 	/**
 	 * Returns row at the given index. The row contains all elements (including empty cells)
 	 * @param index Index of the row
 	 * @return {@link Row} at the given index
 	 */
 	public ExcelRow getRowWithEmptyCells(int index) {
 		ExcelRow res = new ExcelRow(index);
 		if (sheet == null)
 			return res;
 		
 		Row row = sheet.getRow(index);
 		if (row == null)
 			return res;
 		
 		fillRawRow(row, res);
 		
 		return res;
 	}
 	
 	/**
 	 * Private method only
 	 * Fills {@link ExcelRow} instance with raw data from excel {@link Row}
 	 * @param row {@link Row} instance
 	 * @param res {@link ExcelRow} instance
 	 * @return last non-empty cell index
 	 */
 	private int fillRawRow(Row row, ExcelRow res) {
 		int lastNonEmptyCell = -1, i = 0;
 		Iterator<Cell> iter = row.cellIterator();
 		while (iter.hasNext())  {
 			Cell c = iter.next();
 			ExcelCell ec = new ExcelCell(c);
 			if (!ec.isEmpty())
 				lastNonEmptyCell = i;
 			res.add(ec);
 			i++;
 		}
 		return lastNonEmptyCell;
 	}
 	
 	/**
 	 * Creates row in sheet with selected index and create appropriate number of cells in it
 	 * @param index row index
 	 * @param minCapacity minimal amount of cells in row (zero based column index) 
 	 * @param leftOffset number of cells from left which will be ignored
 	 * @return {@link ExcelRow} instance
 	 */
 	public ExcelRow createRow(int index, int minCapacity, int leftOffset) {
 		if(sheet == null) 
 			return new ExcelRow(index);
 		Row row = sheet.createRow(index);
 		for(int i = 0; i < minCapacity; i++) row.createCell(i + leftOffset);
 		return getRowWithEmptyCells(index);
 	}
 	
 	/**
 	 * Returns column at the given index. The column contains only elements between the first and last non-blank cells.
 	 * @param index Column index
 	 * @return Column at the given index
 	 */
 	public ExcelColumn getColumn(int index)  {
 		ExcelColumn res = new ExcelColumn(index);
 		if (sheet == null)
 			return res;
 		
 		int lastNonEmptyCell = -1;
 		lastNonEmptyCell = fillRawColumn(index, res);
 		
 		res.removeRange(lastNonEmptyCell + 1, res.size());
 		
 		return res;		
 	}
 	
 	/**
 	 * Returns column at the given index. The column contains only elements  (including empty cells)
 	 * @param index Column index
 	 * @return Column at the given index
 	 */
 	public ExcelColumn getColumnWithEmptyCells(int index) {
 		ExcelColumn res = new ExcelColumn(index);
 		if (sheet == null)
 			return res;
 		
 		fillRawColumn(index, res);
 		
 		return res;
 	}
 	
 	/**
 	 * Private method only
 	 * Fills {@link ExcelColumn} instance with raw data from excel in selected column (index)
 	 * @param row column index
 	 * @param res {@link ExcelRow} instance
 	 * @return last non-empty cell index
 	 */
 	private int fillRawColumn(int index, ExcelColumn res) {
 		int lastNonEmptyCell = -1;
 		for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++)  {
 			ExcelCell c = cellAt(index, i);
 			res.add(c);
 			if (!c.isEmpty())
 				lastNonEmptyCell = i;
 		}
 		return lastNonEmptyCell;
 	}
 	
 	/**
 	 * Returns column at the given index. The column contains only elements between the first and last non-blank cells with first offset-time elements skipped.
 	 * @param index {@link Column} index
 	 * @param offset number of skipped elements from top
 	 * @return Column at the given index
 	 */
 	public ExcelColumn getColumn(int index, int offset) {
 		ExcelColumn column = getColumn(index);
 		column.removeRange(0, offset);
 		return column;
 	}
 	
 	/**
 	 * Returns column at the given index. The column contains only elements between the first and last non-blank cells with first offset-time elements skipped.
 	 * @param index {@link Column} index
 	 * @param offset number of skipped elements from top
 	 * @return Column at the given index
 	 */
 	public ExcelColumn getColumnWithEmptyCells(int index, int offset) {
 		ExcelColumn column = getColumnWithEmptyCells(index);
 		column.removeRange(0, offset);
 		return column;
 	}
 	
 	/**
 	 * Gets sheet name
 	 * @return Sheet name
 	 */
 	public String getName()  {
 		return sheet == null ? "" : sheet.getSheetName();
 	}
 	
 	/**
 	 * Returns true if the sheet actually exists in the workbook. Use this to check that the sheet
 	 * returned by various find methods in ExcelWorkbook was found.
 	 * @return True if the sheet is valid
 	 */
 	public boolean isValid()  {
 		return sheet != null;
 	}
 	
 	/**
 	 * Returns true if this sheet is hidden or invalid
 	 * @return True if this sheet is hidden or invalid
 	 */
 	public boolean isHidden()  {
 		if (sheet == null)
 			return true;
 		return sheet.getWorkbook().isSheetHidden(sheet.getWorkbook().getSheetIndex(sheet));
 	}
 	
 	@Override
 	public String toString()  {
 		return getName();
 	}
 }
