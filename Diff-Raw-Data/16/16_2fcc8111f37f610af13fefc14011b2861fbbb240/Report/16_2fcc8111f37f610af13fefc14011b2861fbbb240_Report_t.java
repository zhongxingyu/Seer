 /*
  * Copyright 2013 ENERKO Informatik GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.enerko.reports2.engine;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.formula.IStabilityClassifier;
 import org.apache.poi.ss.formula.udf.UDFFinder;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.FormulaEvaluator;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 /**
  * Implements a report on the basis of Apache HSSF.<br>
  * If any numerical, date or datetime based cell leads to a {@link ParseException} or
  * {@link NumberFormatException} the whole report is canceled.<br>
  * At the end of the report all functions are evaluated. The report will be created
  * nevertheless if evaluation fails (Excel will then evaluate the formulas).
  * @author Michael J. Simons, 2013-06-18
  */
 public class Report {
 	public final static Map<String, SimpleDateFormat> DATE_FORMATS_SQL;
 	public final static Map<String, String> DATE_FORMATS_EXCEL;
 	public final static Map<Integer, String> IMPORTABLE_CELL_TYPES;
 	public final static DateFormat DATEFORMAT_OUT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN);
 	
 	static {
 		final HashMap<String, SimpleDateFormat> dateFormatsSqlTmp = new HashMap<String, SimpleDateFormat>();
 		dateFormatsSqlTmp.put("date", ConcreteArgument.dateFormat);
 		dateFormatsSqlTmp.put("datetime", ConcreteArgument.dateTimeFormat);
 		DATE_FORMATS_SQL = Collections.unmodifiableMap(dateFormatsSqlTmp);
 		
 		final HashMap<String, String> dateFormatsExcel = new HashMap<String, String>();
 		dateFormatsExcel.put("date",    "dd/mm/yyyy");
 		dateFormatsExcel.put("datetime", "dd/mm/yyyy HH:mm");			
 		DATE_FORMATS_EXCEL = Collections.unmodifiableMap(dateFormatsExcel);
 		
 		final HashMap<Integer, String> importableCellTypes = new HashMap<Integer, String>();
 		importableCellTypes.put(new Integer(Cell.CELL_TYPE_STRING)  , "string");
 		importableCellTypes.put(new Integer(Cell.CELL_TYPE_NUMERIC) , "number");
 		importableCellTypes.put(new Integer(Cell.CELL_TYPE_FORMULA) , "number");	
 		IMPORTABLE_CELL_TYPES = Collections.unmodifiableMap(importableCellTypes);
 	}
 	
 	private final Workbook workbook;
	/** 
	 * There is a maximum number of 4000 cell styles to HSSF. As each number gets formatted, this isn't much
	 * so formatted cell styles are cached
	 */
	private final Map<String, CellStyle> formatCache = new HashMap<String, CellStyle>();
 	
 	Report(final ReportSource reportSource, UDFFinder customFunctions) {
 		this(reportSource, customFunctions, null);
 	}
 	
 	Report(final ReportSource reportSource, UDFFinder customFunctions, final InputStream template) {
 		if(template == null)
 			this.workbook = new HSSFWorkbook();
 		else
 			try {
 				this.workbook = new HSSFWorkbook(new BufferedInputStream(template));
 			} catch(IOException e) {
 				throw new RuntimeException("Could not load template for report!");
 			}
 	
 		if(customFunctions != null)
 			this.workbook.addToolPack(customFunctions);
 		
 		String previousSheetName = null;
 		Sheet sheet = null;
 		// Iterator over all celldefinitions
 		// this doesn't compile inside Oracle Database VM. You need to import the compiled classes
 		// or use reportSource.iterator() directly
 		for(CellDefinition cellDefinition : reportSource) {
 			// Create and cache the current sheet.			
 			if(previousSheetName == null || !previousSheetName.equals(cellDefinition.sheetname)) {
 				previousSheetName = cellDefinition.sheetname;
 				sheet = getSheet(workbook, cellDefinition.sheetname);					
 			}
 			
 			// create, fill and add cell
 			this.addCell(workbook, sheet, cellDefinition);
 		}
 		
 		// Evaluate all formulas
 		try {
 			final FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
 			formulaEvaluator.clearAllCachedResultValues();
 			formulaEvaluator.evaluateAll();
 		} catch(Exception e) {
 		}
 	}
 	
 	Report(final InputStream workbook, UDFFinder customFunctions) {
 		try {
 			this.workbook = new HSSFWorkbook(new BufferedInputStream(workbook));
 		} catch(IOException e) {
 			throw new RuntimeException("Could not load template for report!");
 		}
 		if(customFunctions != null)
 			this.workbook.addToolPack(customFunctions);
 	}
 	
 	/**
 	 * Writes the report into the given {@link OutputStream}, flushes and closes the stream.
 	 * @param out
 	 * @throws IOException
 	 */
 	public void write(final OutputStream out) throws IOException {
 		this.workbook.write(out);
 		out.flush();
 		out.close();
 	}
 	
 	public List<CellDefinition> evaluateWorkbook() {
 		final List<CellDefinition> rv = new ArrayList<CellDefinition>();
 		
 		boolean reevaluate = false;
 		if(workbook instanceof HSSFWorkbook) {
 			try {
 				workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
 			} catch(Exception e) {
 				reevaluate = true;
 			}
 		}
 		
 		final FormulaEvaluator formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook, IStabilityClassifier.TOTALLY_IMMUTABLE);
 		formulaEvaluator.clearAllCachedResultValues();
 						
 		for(int i=0; i<workbook.getNumberOfSheets(); ++i) {			
 			final Sheet sheet = workbook.getSheetAt(i);			
 			for(Row row : sheet) {				
 				for(Cell cell : row) {				
 					if(reevaluate && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
 						try {							
 							formulaEvaluator.evaluateFormulaCell(cell);
 						} catch(Exception e) {
 							ReportEngine.logger.log(Level.WARNING, String.format("Could not evaluate formula '%s' in cell %s on sheet '%s': %s", cell.getCellFormula(),  CellReferenceHelper.getCellReference(cell.getColumnIndex(), row.getRowNum()), sheet.getSheetName(), e.getMessage()));
 						}		
 					}
 										
 					final CellDefinition cellDefinition = IMPORTABLE_CELL_TYPES.containsKey(new Integer(cell.getCellType())) ? new CellDefinition(sheet.getSheetName(), cell) : null;
 					if(cellDefinition != null)
 						rv.add(cellDefinition);
 				}
 			}			
 		}
 		
 		return rv;
 	}
 	
 	/**
 	 * Create a new {@link Sheet} if the sheet with the given name doesn't exist,
 	 * otherwise returns the existing sheet.
 	 * @param workbook
 	 * @param name
 	 * @return Existing or newly created sheet
 	 */
 	private Sheet getSheet(final Workbook workbook, final String name) {
 		Sheet sheet = workbook.getSheet(name);
 		if(sheet == null)			
 			sheet = workbook.createSheet(name);
 		return sheet;
 	}
 	
 	private void addCell(final Workbook workbook, final Sheet sheet, final CellDefinition cellDefinition) {		
 		final String type = cellDefinition.getType();
 		
 		final int columnNum = cellDefinition.column, rowNum = cellDefinition.row;
 		
 		Row row = sheet.getRow(rowNum);
 		if(row == null)
 			row = sheet.createRow(rowNum);
 		
 		Cell cell = row.getCell(columnNum);
 		// If the cell already exists and is no blank cell
 		// it will be used including all formating
 		if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
 			cell = fill(workbook, cell, type, cellDefinition.value, false);			
 		}
 		// Otherwise a new cell will be created, the datatype set and 
 		// optionally a format will be created
 		else {
 			cell = fill(workbook, row.createCell(columnNum), type, cellDefinition.value, true);
 			
 			final Sheet referenceSheet;
 			if(cellDefinition.getReferenceCell() != null && (referenceSheet = workbook.getSheet(cellDefinition.getReferenceCell().sheetname)) != null) {				
 				final Row referenceRow = referenceSheet.getRow(cellDefinition.getReferenceCell().row);
 				final Cell referenceCell = referenceRow == null ? null : referenceRow.getCell(cellDefinition.getReferenceCell().column);				
 				if(referenceCell != null && referenceCell.getCellStyle() != null)
 					cell.setCellStyle(referenceCell.getCellStyle());				
 			}
 		}		
 	}
 	
 	private CellStyle getFormat(final Workbook workbook, final Cell cell, String type, String value) {		
 		String format = (String) DATE_FORMATS_EXCEL.get(type.toLowerCase());		
 		// Type is number
 		if(format == null) {
 			final String[] hlp  = value.split("@@");
 			format = hlp.length > 1 ? hlp[1] : "0.00####";
 		}
		CellStyle cellStyle = formatCache.get(format);
		if(cellStyle == null) {
			cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(workbook.createDataFormat().getFormat(format));
			formatCache.put(format, cellStyle);
		}
 		
 		return cellStyle;		
 	}
 	
 	private SimpleDateFormat getDateFormatSql(String type) {
 		return ((SimpleDateFormat)DATE_FORMATS_SQL.get(type.toLowerCase()));
 	}
 	
 	private Cell fill(final Workbook workbook, Cell tmp, String type, String value, boolean setType) {
 		if(type.equalsIgnoreCase("string")) {
 			if(setType)
 				tmp.setCellType(Cell.CELL_TYPE_STRING);
 			tmp.setCellValue(value);						
 		} else if(type.equalsIgnoreCase("number")) {
 			if(setType) {
 				tmp.setCellType(Cell.CELL_TYPE_NUMERIC);
 				tmp.setCellStyle(getFormat(workbook, tmp, type, value));
 			}
 			try {
 				tmp.setCellValue(Double.parseDouble(value.split("@@")[0]));
 			} catch(NumberFormatException e) {
 				throw new RuntimeException(String.format("Could not parse value \"%s\" for numeric cell %dx%d!", value, tmp.getColumnIndex(), tmp.getRowIndex()));
 			}
 		} else if(type.equalsIgnoreCase("date") || type.equalsIgnoreCase("datetime")) {
 			if(setType) {
 				tmp.setCellType(Cell.CELL_TYPE_NUMERIC);
 				tmp.setCellStyle(getFormat(workbook, tmp, type, value));
 			}
 			try {
 				tmp.setCellValue(getDateFormatSql(type).parse(value));
 			} catch(ParseException e) {
 				throw new RuntimeException(String.format("Could not parse value \"%s\" for date/datetime cell %dx%d!", value, tmp.getColumnIndex(), tmp.getRowIndex()));
 			}
 		} else if(type.equalsIgnoreCase("formula")) {
 			if(setType)
 				tmp.setCellType(Cell.CELL_TYPE_FORMULA);
 			tmp.setCellFormula(value);			
 		} else
 			throw new RuntimeException("Invalid type " + type);
 		return tmp;
 	}
 }
