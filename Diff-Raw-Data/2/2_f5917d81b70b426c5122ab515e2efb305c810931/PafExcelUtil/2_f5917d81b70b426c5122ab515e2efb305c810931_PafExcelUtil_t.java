 /**
  * 
  */
 package com.pace.base.project.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.FormulaEvaluator;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.project.ExcelPaceProjectConstants;
 import com.pace.base.project.ExcelProjectDataErrorException;
 import com.pace.base.project.ProjectDataError;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.excel.PafExcelInput;
 import com.pace.base.project.excel.PafExcelRow;
 import com.pace.base.project.excel.PafExcelValueObject;
 import com.pace.base.utility.CollectionsUtil;
 import com.pace.base.utility.StringUtils;
 
 /**
  * @author jmilliron
  *
  */
 public class PafExcelUtil {
 
 	private static final Logger logger = Logger.getLogger(PafExcelUtil.class);
 		
 	/**
 	 * 
 	 * A user can read from an existing workbook via the input, or pass have this method create a new
 	 * workbook reference and read from that.  The main objective of this method is to read in a sheet from
 	 * an existing workbook and to convert the data from that sheet into a model pace can use.
 	 * 
 	 * If input doesn't have a column limit, a default of 100 columns will be read.
 	 * 
 	 * @param input 	input meta data used by method to read in sheet
 	 * @return			a list of paf row's.
 	 * @throws PafException
 	 */
 	public static List<PafExcelRow> readExcelSheet(PafExcelInput input) throws PafException {
 		
 		validateReadInput(input);
 				
 		Workbook wb = null;
 		
 		if ( input.getWorkbook() != null ) {
 			
 			wb = input.getWorkbook();
 			
 		} else {
 			
 			wb = readWorkbook(input.getFullWorkbookName());
 			
 		}
 		
 		List<PafExcelRow> pafRowList = new ArrayList<PafExcelRow>();
 if (wb ==null) logger.info("Workbook(WB) is null");				
 		if ( wb != null ) {
 logger.info("Workbook(WB) exists");		
 			Sheet sheet = wb.getSheet(input.getSheetId());	
 if (sheet ==null) logger.info("Sheet is null");							
 			if ( sheet != null ) {
 logger.info("Sheet is not null");			
 				int sheetLastRowNumber = sheet.getLastRowNum();	
 				
 				//if row limit is less than max row number on sheet, reset last row number to row limit.
 				if ( input.getRowLimit() > 0 && input.getRowLimit() < sheetLastRowNumber ) {
 					
 					sheetLastRowNumber = input.getRowLimit();
 					
 					//subtract one to aline with 0 based indexes
 					sheetLastRowNumber--;
 					
 				}
 								
 				int startColumnIndex = 0;
 				
 				if ( input.getStartDataReadColumnIndex() != null && input.getStartDataReadColumnIndex() < input.getColumnLimit()) {
 					
 					startColumnIndex = input.getStartDataReadColumnIndex();
 									
 				}
 				
 				PafExcelRow pafRow = null;
 				
 				OUTTER_LOOP:
 				for (int i = 0; i <= sheetLastRowNumber; i++) {
 											
 					Row row = sheet.getRow(i);
 					
 					//if entire row is blank
 					if ( row == null ) {
 					
 						//by default, include empty row
 						if ( ! input.isExcludeEmptyRows() ) {										
 logger.info("Adding PafExcelRow");
 							pafRowList.add(new PafExcelRow());
 													
 						}	
 						
 						continue;
 						
 					} 	
 					
 					int endColumnIndex = input.getColumnLimit();
 								
 					//Add pace row items to pace row
 					for (int t = startColumnIndex; t < endColumnIndex; t++) {
 						
 						Cell cell = row.getCell(t);
 														
 						//if cell is null, have row create blank cell
 						if ( cell == null ) {
 							
 							logger.debug("Null Blank: " + i + ", " + t);
 		
 							//since cell is null, have row create cell
 							cell = row.createCell(t, Cell.CELL_TYPE_BLANK);
 																	
 						} 
 							
 						PafExcelValueObject pafExcelValueObject = PafExcelValueObject.createFromCell(wb, cell);				
 						
 						//if 1st column item
 						if ( t == startColumnIndex ) {
 							
 							//if user flagged end of sheet data with a string and string matches here, stop reading data
 							if ( pafExcelValueObject.isString() && input.getEndOfSheetIdnt() != null &&
 									pafExcelValueObject.getString().equalsIgnoreCase(input.getEndOfSheetIdnt())) {
 								
 								break OUTTER_LOOP;
 								
 							}
 							
 							//if not multi data row or it is but 1st item isn't a blank, create a new row
 							if ( ! input.isMultiDataRow() || (input.isMultiDataRow() && ! pafExcelValueObject.isBlank()) ) {
 																	
 								pafRow = new PafExcelRow();
 								
 							}
 							
 						}
 						
 						pafRow.addRowItem(t, pafExcelValueObject);					
 						
 					}
 					
 					//if the list already contains the row, remove.
 					if (pafRowList.contains(pafRow) && pafRowList.indexOf(pafRow) == (pafRowList.size() - 1)) {
 						
 						pafRowList.remove(pafRow);
 														
 					}
 											
 					pafRowList.add(pafRow);
 					
 					//header row
 					if ( isHeaderRow(input, pafRow) ) {
 						
 						//if exclude header, then remove from list of pace rows
 						if ( input.isExcludeHeaderRows() ) {
 							
 							pafRowList.remove(pafRow);
 							sheetLastRowNumber++;
 							
 						//else set the header attribute
 						} else {
 						
 							pafRow.setHeader(true);
 							
 						}				
 							
 					//blank row
 					} else if ( isBlankRow(input, pafRow)) {
 						
 						if ( input.isExcludeEmptyRows() ) {
 							
 							pafRowList.remove(pafRow);
 							sheetLastRowNumber++;
 							
 						} else {
 							
 							pafRow.setBlank(true);
 						}
 						
 					//data row
 					} else {
 						
 						//exclude data rows?  if yes, remove
 						if ( input.isExcludeDataRows()) {
 							
 							pafRowList.remove(pafRow);
 							
 						}
 						
 					}
 					
 				}
 			}
 		
 		}		
 		
 		return pafRowList;
 		
 	}
 	
 	private static void validateReadInput(PafExcelInput input) throws PafException {
 
 		if ( input == null ) {
 			
 			String error = "Paf Excel Input cannot be null"; 
 			
 			logger.error(error);
 			
 			throw new NullPointerException(error);
 			
 		} else if ( input.getFullWorkbookName() == null && input.getWorkbook() == null ) {
 						
 			String error = "Paf excel input has not been configured property.";
 			
 			logger.error(error);
 			
 			throw new NullPointerException(error);
 			
 		}
 		
 		Workbook wb = null;
 		
 		if ( input.getWorkbook() != null ) {
 			
 			wb = input.getWorkbook();
 			
 		} else {
 			
 			wb = readWorkbook(input.getFullWorkbookName());
 			
 		}
 			
 		if ( wb == null ) {
 			
 			String error = "Workbook " + input.getFullWorkbookName() + " could not be read."; 
 			
 			logger.error(error);
 			
 			throw new PafException(error, PafErrSeverity.Error);
 			
 		} else {
 			
 			Sheet sheet = wb.getSheet(input.getSheetId());
 			
 			if ( sheet == null && input.isSheetRequired() ) {
 			
 				String error = "Sheet " + input.getSheetId() + " does not exist.";
 				
 				logger.error(error);
 				
 				throw new PafException(error, PafErrSeverity.Error);
 				
 			}	
 			
 		}
 		
 	}
 
 	public static List<String> getWorkbookSheetNames(Workbook workbook) throws PafException {
 		
 		List<String> sheetNameList = new ArrayList<String>();
 		
 		if ( workbook != null ) {
 			
 			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
 				
 				sheetNameList.add(workbook.getSheetName(i));
 				
 			}
 			
 		}
 		
 		if ( logger.isDebugEnabled() ) {
 			
 			logger.debug("Sheets: " + sheetNameList);
 			
 		}
 		
 		return sheetNameList;
 	
 		
 	}
 	
 	public static List<String> getWorkbookSheetNames(String workbookLocation) throws PafException {
 		
 		Workbook wb = readWorkbook(workbookLocation);
 		
 		return getWorkbookSheetNames(wb);
 			
 	}
 	
 	private static boolean isBlankRow(PafExcelInput input, PafExcelRow pafRow) {
 		
 		if ( pafRow != null ) {
 			
 			for (Integer index : pafRow.getPafExcelValueObjectListMap().keySet()) {
 				
 				for (PafExcelValueObject valueObject :  pafRow.getPafExcelValueObjectListMap().get(index) ) {
 					
 					if ( ! valueObject.isBlank() ) {
 						
 						return false;
 						
 					}
 					
 				}
 				
 				
 			}
 			
 		} 
 			
 		return true;		
 		
 	}
 
 	protected static boolean isHeaderRow(PafExcelInput input, PafExcelRow pafRow) {
 		
 		boolean isHeader = false;
 		
 		logger.debug("Checking if row is header row.");
 		
 		if ( input != null && pafRow != null ) {
 			
 			int startIndex = 0;
 			
 			if ( input.getStartDataReadColumnIndex() != null && input.getStartDataReadColumnIndex() > 0 ) {
 				
 				startIndex = input.getStartDataReadColumnIndex();
 				
 			}
 			
 			int endIndex = input.getColumnLimit();
 			
 			//continue if start index is less than end index
 			if ( startIndex < endIndex ) {
 								
 				List<Integer> rowColumnIndexList = pafRow.getRowItemOrderedIndexes();
 				
 				//loop through header list and see if the row matches any of the header list
 				OUTTER_LOOP:
 				for (String headerKey : input.getHeaderListMap().keySet()) {
 				
 					List<String> headerList = input.getHeaderListMap().get(headerKey); 
 					
 					//see if header row or not
 					if ( headerList != null && headerList.size() > 0 ) {
 												
 						try {
 							
 							headerList = headerList.subList(startIndex, endIndex);
 						
 						} catch (IndexOutOfBoundsException e) {
 							
 							continue;
 							
 						}
 						
 						List<Integer> updateRowColumnIndexList = new ArrayList<Integer>();
 						
 						try {
 							
 							updateRowColumnIndexList.addAll(rowColumnIndexList.subList(0, endIndex-startIndex));
 							
 						} catch (IndexOutOfBoundsException e) {
 							
 							continue;
 							
 						}
 						
 						//if header list size does not equal the number of row item indexes, continue to next header;
 						if ( updateRowColumnIndexList.size() != headerList.size()) {
 																		
 							continue;
 							
 						}
 						
 						isHeader = true;
 						
 						//loop through all pace row items
 						for (Integer rowItemIndex : updateRowColumnIndexList ) {							
 							
 							List<PafExcelValueObject> paceRowItemList = pafRow.getRowItem(rowItemIndex);
 														
 							logger.debug("\tProcessing Row Item List (element 0): " + paceRowItemList.get(0).getValueAsString());
 							
 							int headerIndex = updateRowColumnIndexList.indexOf(rowItemIndex);
 										
 							//items to compare
 							String headerItem = headerList.get(headerIndex);
 							String paceRowItem = paceRowItemList.get(0).getValueAsString();
 							
 							//if pace row item is a header ignore field, set as blank
 							if ( headerItem.equals(ExcelPaceProjectConstants.HEADER_IGNORE_IDENT)) {
 								
 								paceRowItem = ExcelPaceProjectConstants.HEADER_IGNORE_IDENT;
 								
 							}
 							
 							//if string returns something and the header list does not match
 							if ( paceRowItemList.size() > 1 || (paceRowItemList.size() == 1 && ! headerItem.equalsIgnoreCase(paceRowItem))) {
 																				
 								isHeader = false;
 								
 								continue OUTTER_LOOP;
 							}
 							
 						}
 						
 						//if header, break look and don't check next header
 						if ( isHeader ) {
 							
 							pafRow.setHeaderIdent(headerKey);
 							
 							break OUTTER_LOOP;
 							
 						}
 						
 					}
 				}
 			}	
 		}
 		logger.debug("Row is header: " + isHeader);
 		
 		return isHeader;	
 	}
 	
 	
 	public static void writeExcelSheet(PafExcelInput input, List<PafExcelRow> paceRowList) throws PafException {
 		
 		if ( input == null ) {
 			
 			throw new IllegalArgumentException("Pace Excel Input cannot be null");
 			
 		} else if ( input.getFullWorkbookName() == null && input.getWorkbook() == null ) {
 							
 			throw new IllegalArgumentException("A Full workbook name and a workbook haven't not been provided.  One is required.");
 			
 		}
 		
 		Workbook wb = null;
 		
 		if ( input.getWorkbook() != null ) {
 			
 			wb = input.getWorkbook();
 			
 		} else {
 			
 			wb = readWorkbook(input.getFullWorkbookName());
 			
 		}
 	
 		if ( wb == null ) {
 			
 			throw new PafException("Couldn't get a reference to the workbook " + input.getFullWorkbookName() , PafErrSeverity.Error);
 			
 		}
 		
 		Sheet sheet = wb.getSheet(input.getSheetId());
 		
 		if ( sheet == null ) {
 			
 			sheet = wb.createSheet(input.getSheetId());
 			
 		} else {
 			
 			sheet = clearSheetValues(sheet);
 			
 		}
 						
 		if ( paceRowList != null ) {
 		
 			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
 			
 			int rowNdx = 0;
 			
 			int maxNumberOfColumns = 0;
 			
 			CellStyle boldCellStyle = null;
 			
 			boolean isFirstHeader = true;
 			
 			for (PafExcelRow paceRow : paceRowList ) {
 																			
 				if ( paceRow != null ) {
 					
 					//if row is header and need to exclude, continue to next row
 					if ( paceRow.isHeader() && input.isExcludeHeaderRows())  {
 						continue;
 					}
 			
 					if ( paceRow.numberOfColumns() > maxNumberOfColumns) {
 						
 						maxNumberOfColumns = paceRow.numberOfColumns();
 						
 					}
 					
 					for (int i = 0; i < paceRow.numberOfCompressedRows(); i++ ) {
 				
 						Row row = sheet.createRow(rowNdx++);
 						
 						if ( isFirstHeader && paceRow.isHeader() ) {
 							
 							//freeze 1st header row
 							sheet.createFreezePane(0, 1);
 							
 							isFirstHeader = false;
 						}
 						
 						for ( Integer index : paceRow.getRowItemOrderedIndexes() ) {
 											
 							List<PafExcelValueObject> rowItemList = paceRow.getPafExcelValueObjectListMap().get(index);
 							
 							//Cell cell = row.createCell(rowItemNdx++);;
 							Cell cell = row.createCell(index);
 							
 							if ( i < rowItemList.size()) {
 								
 								PafExcelValueObject rowItem = rowItemList.get(i);
 								
 								if ( rowItem == null ) {
 									
 									cell.setCellType(Cell.CELL_TYPE_BLANK);
 									
 								} else {
 									
 									//if header, bold item
 									if ( paceRow.isHeader() || rowItem.isBoldItem() ) {
 										
 										if ( boldCellStyle == null ) {
 											boldCellStyle = getBoldCellStyle(wb);
 											
 											//turn word wrap on											
 											boldCellStyle.setWrapText(true);
 										}									
 										
 										cell.setCellStyle(boldCellStyle);
 										
 									}
 									
 									switch ( rowItem.getType() ) {
 									
 									case Blank:
 										
 										cell.setCellType(Cell.CELL_TYPE_BLANK);
 										
 										break;
 										
 									case Boolean:
 																														
 										if ( rowItem.getBoolean() != null ) {
 																																
 											Boolean boolValue = rowItem.getBoolean();
 											
 											if ( input.isExcludeDefaultValuesOnWrite()) {
 												
 												//if default, clear value
 												if ( boolValue.equals(Boolean.FALSE)) {
 													
 													boolValue = null;
 													
 												}
 												
 											}
 											
 											//true or false
 											if ( boolValue != null ) {
 												
 												cell.setCellType(Cell.CELL_TYPE_BOOLEAN);	
 												cell.setCellValue(boolValue);
 												
 											}
 											
 										}
 										
 										break;
 										
 									case Double:
 																														
 										if ( rowItem.getDouble() != null ) {
 										
 											Double doubleVal = rowItem.getDouble();
 											
 											if ( doubleVal != null && input.isExcludeDefaultValuesOnWrite()) {
 												
 												if ( doubleVal == 0 ) {
 													
 													doubleVal = null;
 													
 												}
 												
 											}
 											
 											if ( doubleVal != null ) {
 											
 												cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 												
 												cell.setCellValue(doubleVal);
 												
 											}
 											
 											
 										}
 										
 										break;
 										
 									case Integer:
 																														
 										if ( rowItem.getInteger() != null ) {
 											
 											Integer intValue = rowItem.getInteger();
 											
 											if ( intValue != null && input.isExcludeDefaultValuesOnWrite()) {
 												
 												if ( intValue == 0 ) {
 													
 													intValue = null;
 													
 												}
 												
 											}
 											
 											if ( intValue != null ) {
 												
 												cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 												
 												cell.setCellValue(intValue);
 												
 											}
 											
 										}
 										
 										break;
 										
 									case Formula:
 										
 										cell.setCellType(Cell.CELL_TYPE_FORMULA);
 									
 										String formula = rowItem.getFormula();
 
 										String formulas[] = formula.split(" & \" \\| \" & ");
 										if( formulas.length >= 1 ) { //there is only 1 formula
 											formula = formulas[0];
 											String tokens[] = formula.split("!");
 											if( tokens.length > 1 ) {
 												String sheetName = tokens[0];
 												if( ! sheetName.matches("[a-zA-Z0123456789]*") )  {
 													formula = "'" + sheetName + "'!" + tokens[1];
 												}
 											}
 										}
 										if ( formulas.length > 1 ) { // more than 1 formula concatenated
 											String tmp = "";
 											for( int j=1;j<formulas.length;j++ ) {
 												tmp = formulas[j];
 												String tokens[] = formulas[j].split("!");
 												if( tokens.length > 1 ) {
 													String sheetName = tokens[0];
 													if( ! sheetName.matches("[a-zA-Z0123456789]*") )  {
 														tmp = "'" + sheetName + "'!" + tokens[1];
 													}
													formula += " & \" | \" & " + tmp; //append to the previous one
 												}
 											}
 										}
 										cell.setCellFormula(formula);											
 										
 										evaluator.evaluateFormulaCell(cell);
 									
 										break;
 										
 									case String:
 										
 										cell.setCellType(Cell.CELL_TYPE_STRING);
 										
 										cell.setCellValue(rowItem.getString());
 										
 										break;
 									
 									}
 									
 								}
 								
 								
 								
 							} else {
 								
 								cell.setCellType(Cell.CELL_TYPE_BLANK);
 								
 							}
 												
 						}
 					}					
 					
 				}
 												
 			}	
 			
 			//add end of sheet ident
 			if ( input.getEndOfSheetIdnt() != null ) {
 				
 				int lastSheetRowNumber = sheet.getLastRowNum() + 1;
 				
 				Row lastRow = sheet.createRow(lastSheetRowNumber);
 				
 				Cell eosCell = lastRow.createCell(0, Cell.CELL_TYPE_STRING);
 				
 				eosCell.setCellValue(input.getEndOfSheetIdnt());				
 				
 				//set bold style
 				eosCell.setCellStyle(getBoldCellStyle(wb));
 				
 			}
 			
 			//auto size columns?
 			if ( input.isAutoSizeColumns() ) {
 			
 				for (int i = 0; i <= maxNumberOfColumns; i++) {
 					
 					sheet.autoSizeColumn((short) i);
 					
 				}
 			
 			}
 			
 			
 			
 		}	
 		
 		logger.info("\tSaving sheet: " + input.getSheetId() );
 		
 		wb.setActiveSheet(wb.getSheetIndex(sheet.getSheetName()));
 						
 		//if write to filesystem, close workbook
 		if ( input.isAutoWriteToFileSystem() ) {
 		
 			writeWorkbook(wb, input.getFullWorkbookName());
 			
 		}
 	    
 	    
 		
 	}
 	
 	private static CellStyle getBoldCellStyle(Workbook workbook) {
 		
 		CellStyle cellStyle = null;
 		
 		if ( workbook != null ) {		
 							
 			cellStyle = workbook.createCellStyle();
 			
 			Font font = workbook.createFont();
 			
 			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
 			
 			cellStyle.setFont(font);
 			
 						
 		}	
 		
 		return cellStyle;	
 		
 	}
 	
 
 	private static Sheet clearSheetValues(Sheet sheet) {
 
 		if ( sheet != null ) {
 			
 			Workbook wb = sheet.getWorkbook();
 			
 			String sheetName = sheet.getSheetName();
 									
 			int sheetNdx = wb.getSheetIndex(sheetName);
 			
 			wb.removeSheetAt(sheetNdx);
 								
 			sheet = wb.createSheet(sheetName);
 								
 			wb.setSheetOrder(sheetName, sheetNdx);	
 									
 		}
 		
 		return sheet;		
 		
 	}
 
 	public static String createExcelReference(String sheetId, String address) {
 
 		String excelReference = null;
 		
 		if ( sheetId != null && address != null ) {
 			
 			if ( sheetId.contains(" ") ) {
 				
 				sheetId = "'" + sheetId.trim() + "'";
 				
 			}
 			
 			excelReference = sheetId + "!" + address; 
 			
 		}
 		
 		return excelReference;
 				
 	}
 	
 	/**
 	 * 
 	 * Replaces all blanks with null
 	 *
 	 * @param list list of paf excel value objects
 	 * @return a list of excel value objects will all blank's converted into null items in list
 	 */
 	public static List<PafExcelValueObject> convertBlanksToNullInList(List<PafExcelValueObject> list) {
 		
 		List<PafExcelValueObject> updatedList = new ArrayList<PafExcelValueObject>();
 		
 		if ( list != null ) {
 		
 			for (PafExcelValueObject listValueObject : list ) {
 				
 				if ( listValueObject.isBlank() ) {
 					
 					updatedList.add(null);
 					
 				} else {
 					
 					updatedList.add(listValueObject);
 					
 				}
 				
 			}
 			
 		}
 		
 		return updatedList;
 		
 	}
 	
 	public static String getString(ProjectElementId projectElementId, PafExcelValueObject valueObject, boolean isRequired) throws ExcelProjectDataErrorException {
 		
 		return getString(projectElementId, valueObject, isRequired, null);
 		
 	}
 	
 	public static String getString(ProjectElementId projectElementId, PafExcelValueObject valueObject, List<String> validValueList) throws ExcelProjectDataErrorException {
 		
 		return getString(projectElementId, valueObject, false, validValueList);
 		
 	}
 	
 	public static String getString(ProjectElementId projectElementId, PafExcelValueObject valueObject, boolean isRequired, List<String> validValueList) throws ExcelProjectDataErrorException {
 	
 		String str = null;				
 
 		//if null or blank
 		if ( valueObject == null || valueObject.isBlank() ) {
 			
 			if ( isRequired ) {
 			
 				throw new ExcelProjectDataErrorException(ProjectDataError.createRequiredProjectDataError(projectElementId, valueObject));
 				
 			}
 			
 		} else if ( valueObject.isString() || valueObject.isFormula() || valueObject.isNumeric() ) {
 						
 			if ( validValueList == null ) {
 				
 				str = valueObject.getValueAsString();
 				
 			} else if (validValueList != null && CollectionsUtil.containsIgnoreCase(validValueList, valueObject.getValueAsString()))  {
 				
 				for (String validValue : validValueList ) {
 					
 					if ( valueObject.getValueAsString().equalsIgnoreCase(validValue)) {
 						
 						str = validValue;
 						break;
 						
 					}
 					
 				}
 				
 			} else {
 				
 				throw new ExcelProjectDataErrorException(ProjectDataError.createInvalidValueProjectDataError(projectElementId, valueObject, validValueList));
 				
 			}
 			
 		} else {
 				
 			throw new ExcelProjectDataErrorException(ProjectDataError.createInvalidValueProjectDataError(projectElementId, valueObject, validValueList));
 			
 		}
 			
 	
 		
 		return str;
 		
 	}
 	
 		
 	public static Integer getInteger(ProjectElementId projectElementId, PafExcelValueObject valueObject) throws ExcelProjectDataErrorException {
 		
 		return getInteger(projectElementId, valueObject, false);
 		
 	}
 	
 	public static Integer getInteger(ProjectElementId projectElementId, PafExcelValueObject valueObject, boolean isRequired) throws ExcelProjectDataErrorException {
 		
 		Integer intValue = null;
 		
 		//if not blank
 		if ( valueObject != null && ! valueObject.isBlank() ) {
 			
 			//if numeric, get int value
 			if ( valueObject.isNumeric() ) {
 				
 				intValue = valueObject.getInteger();
 				
 			//throw invalid exception
 			} else {
 				
 				throw new ExcelProjectDataErrorException(ProjectDataError.createInvalidNumberProjectDataError(projectElementId, valueObject));
 			}
 			
 		//if blank
 		} else {
 			
 			//if required and blank
 			if ( isRequired ) {
 				
 				throw new ExcelProjectDataErrorException(ProjectDataError.createRequiredProjectDataError(projectElementId, valueObject));
 				
 			}
 			
 		}
 		
 		return intValue;
 		
 	}
 	
 	public static Boolean getBoolean(ProjectElementId projectElementId, PafExcelValueObject valueObject) throws ExcelProjectDataErrorException {
 		
 		return getBoolean(projectElementId, valueObject, false);
 		
 	}
 	
 	public static Boolean getBoolean(ProjectElementId projectElementId, PafExcelValueObject valueObject, boolean returnDefaultIfNull) throws ExcelProjectDataErrorException {
 		
 		Boolean boolValue = null;
 		
 		if ( ! valueObject.isBlank() ) {
 			
 			if ( valueObject.isBoolean() ) {
 				
 				boolValue = valueObject.getBoolean();
 				
 			} else {
 				
 				throw new ExcelProjectDataErrorException(ProjectDataError.createBooleanProjectDataError(projectElementId, valueObject));
 			}
 			
 		}	
 		
 		if ( returnDefaultIfNull && boolValue == null) {
 			
 			boolValue = Boolean.FALSE;
 			
 		}
 		
 		return boolValue;
 		
 	}
 	
 	public static String[] getStringAr(ProjectElementId projectElementId, List<PafExcelValueObject> valueObjectList) throws ExcelProjectDataErrorException {
 	
 		return getStringAr(projectElementId, valueObjectList, false);
 		
 	}
 	
 	public static String[] getStringAr(ProjectElementId projectElementId, List<PafExcelValueObject> valueObjectList, boolean isRequired ) throws ExcelProjectDataErrorException {
 	
 		String[] stringAr = null;	
 		
 		List<String> stringList = new ArrayList<String>();
 		
 		if ( valueObjectList != null ) {
 		
 			for (PafExcelValueObject excelValueObject : valueObjectList ) {
 				
 				stringList.add(getString(projectElementId, excelValueObject));
 				
 			}
 			
 		}	
 		
 		//if list has elements, convert into a string array
 		if ( stringList.size() > 0 ) {
 					
 			stringAr = stringList.toArray(new String[0]);
 			
 		}
 		
 		//if required
 		if ( isRequired ) {
 			
 			//if required and null or array converted to list with nulls pruned and size of 0, then throw project data error.
 			if ( stringAr == null || CollectionsUtil.arrayToListPruneNulls(stringAr).size() == 0 ) {
 
 				//throw new exception using 1st excel value object in list, should be blank
 				throw new ExcelProjectDataErrorException(ProjectDataError.createRequiredProjectDataError(projectElementId, valueObjectList.get(0)));
 				
 			}
 			
 		}
 		
 		return stringAr;
 		
 	}
 	
 	public static String[] getStringArFromDelimValueObject(ProjectElementId projectElementId, PafExcelValueObject valueObject) throws ExcelProjectDataErrorException {
 		
 		return getStringArFromDelimValueObject(projectElementId, valueObject, false);
 		
 	}
 	
 	public static String[] getStringArFromDelimValueObject(ProjectElementId projectElementId, PafExcelValueObject valueObject, boolean isRequired) throws ExcelProjectDataErrorException {
 		
 		String[] strAr = null;		
 			
 		//if null or blank
 		if ( valueObject == null || valueObject.isBlank() ) {
 			
 			//if required, throw error if null or blank
 			if ( isRequired ) {
 				
 				throw new ExcelProjectDataErrorException(ProjectDataError.createRequiredProjectDataError(projectElementId, valueObject));
 				
 			}
 			
 		} else if ( valueObject.isString() || valueObject.isFormula() || valueObject.isNumeric() ) {
 						
 				List<String> listFromString = StringUtils.stringToList(PafExcelUtil.getString(projectElementId, valueObject), ExcelPaceProjectConstants.EXCEL_STRING_FIELD_DELIM);
 				
 				if ( listFromString.size() > 0 ) {
 					
 					strAr = listFromString.toArray(new String[0]);
 					
 				}
 				
 		} 
 		
 		return strAr;
 		
 	}
 	
 	public static String getString(ProjectElementId projectElementId, PafExcelValueObject valueObject)  throws ExcelProjectDataErrorException {
 		
 		return getString(projectElementId, valueObject, false, null);
 		
 	}
 	
 
 	
 	
 	public static String getHexNumber(ProjectElementId projectElementId, PafExcelValueObject valueObject) throws ExcelProjectDataErrorException {
 		
 		if ( valueObject != null ) {
 			
 			if ( ! valueObject.isBlank() ) {
 				
 				String hexNumber = null;
 				
 				if ( valueObject.isNumeric()) {
 					
 					hexNumber = valueObject.getInteger().toString();
 					
 				} else {
 					
 					hexNumber = valueObject.getValueAsString();
 					
 				}
 	
 				//hex values are length of 6
 				if ( hexNumber.length() != 6 ) {
 					
 					throw new ExcelProjectDataErrorException(ProjectDataError.createInvalidValueProjectDataError(projectElementId, valueObject));
 					
 				} else {
 				
 					Pattern p = Pattern.compile(ExcelPaceProjectConstants.HEX_FONT_PATTERN);
 					
 					Matcher m = p.matcher(hexNumber);
 					
 					if ( m.matches() ) {
 						
 						return hexNumber.toUpperCase();
 						
 					} else {
 						
 						throw new ExcelProjectDataErrorException(ProjectDataError.createInvalidValueProjectDataError(projectElementId, valueObject));
 						
 					}								
 					
 				}							
 				
 			} 
 		}
 		
 		
 		return null;
 	}
 	
 	public static Workbook readWorkbook(String workbookLocation) throws PafException {
 				
 		if ( workbookLocation == null ) {
 			
 			throw new IllegalArgumentException("Workbook location can not be null.");
 			
 		}
 		
 		Workbook wb = null;
 		
 		File fullFileWorkbookLocation = new File(workbookLocation);
 		
 		if ( fullFileWorkbookLocation.isFile() ) {
 		
 			InputStream inp = null;	
 				
 			try {
 				
 				inp = new FileInputStream(workbookLocation);
 				
 				wb = WorkbookFactory.create(inp);
 				
 			} catch (FileNotFoundException e) {
 				
 				throw new PafException(e.getMessage(), PafErrSeverity.Error);
 				
 			} catch (InvalidFormatException e) {
 				
 				throw new PafException(e.getMessage(), PafErrSeverity.Error);
 				
 			} catch (IOException e) {
 				
 				throw new PafException(e.getMessage(), PafErrSeverity.Error);
 				
 			}
 			
 		} else {
 			
 			wb = new XSSFWorkbook();
 			
 		}
 		
 		if ( wb == null ) {
 			
 			logger.error("Couldn't get a refernece to workbook " + workbookLocation);			
 			throw new PafException("Workbook reference " + workbookLocation + " is null.", PafErrSeverity.Error);
 		}		
 		
 		return wb;
 		
 	}
 	
 	public static void writeWorkbook(Workbook wb, String fullWorkbookName) throws PafException {
 		
 		// Write the output to a file
 	    FileOutputStream fileOut = null;
 	        
 		try {
 			
 			fileOut = new FileOutputStream(fullWorkbookName);
 								
 			wb.write(fileOut);
 			
 		} catch (FileNotFoundException e) {
 			
 			logger.error(e.getMessage());
 			
 			throw new PafException(e.getMessage(), PafErrSeverity.Fatal);
 			
 		} catch (IOException e) {
 
 			logger.error(e.getMessage());
 			
 			throw new PafException(e.getMessage(), PafErrSeverity.Error);
 			
 		} finally {
 		
 			if ( fileOut != null ) {
 				
 				try {
 					fileOut.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * Creats a cell reference map, key is actual value, value is sheet/cell reference
 	 * 
 	 * @param input
 	 * @return
 	 * @throws PafException
 	 */
 	public static Map<String, String> createCellReferenceMap ( PafExcelInput input )  throws PafException  {
 		
 		validateReadInput(input);
 		
 		Map<String, String> cellReferenceMap = new HashMap<String, String>();
 			
 		List<PafExcelRow> excelRowList = readExcelSheet(input);
 		
 		int startColumnIndex = 0;
 		
 		if ( input.getStartDataReadColumnIndex() != null ) {
 			
 			startColumnIndex = input.getStartDataReadColumnIndex();
 			
 		}
 		
 		if ( excelRowList != null ) {
 		
 			for ( PafExcelRow excelRow : excelRowList ) {
 			
 				if ( excelRow.getRowItemOrderedIndexes().contains(startColumnIndex)) {
 					
 					List<PafExcelValueObject> valueObjectList = excelRow.getRowItem(startColumnIndex);
 					
 					if ( valueObjectList != null && valueObjectList.size() > 0 ) {
 						
 						//get 1st element
 						PafExcelValueObject valueObject = valueObjectList.get(0);
 						
 						if ( valueObject.getCellAddress() != null ) {
 							
 							cellReferenceMap.put(valueObject.getString(), createExcelReference(input.getSheetId(), valueObject.getCellAddress(true)));
 							
 						}
 						
 					}
 					
 				}
 				
 				
 			}			
 			
 		}		
 		
 		return cellReferenceMap;
 	
 	}
 	
 	public static PafExcelRow createHeaderRow(List<String> headerList) {
 		
 		PafExcelRow row = new PafExcelRow();
 		
 		if ( headerList != null) {
 		
 			row.setHeader(true);
 			
 			int headerNdx = 0;
 			
 			for ( String header : headerList ) {
 				
 				row.addRowItem(headerNdx++, PafExcelValueObject.createFromString(header));
 				
 			}
 			
 		}	
 		
 		return row;
 		
 	}
 	
 	public static void orderSheets(String fullWorkbookName, List<ProjectElementId> sheetOrderList) throws PafException {
 				
 		try {
 			
 			Workbook wb = readWorkbook(fullWorkbookName);
 			
 			if ( wb != null ) {
 				
 				Map<ProjectElementId, Integer> sheetOrderMap = new HashMap<ProjectElementId, Integer>();
 				
 				for ( ProjectElementId projectElementId : sheetOrderList ) {
 					
 					int sheetIndex = wb.getSheetIndex(projectElementId.toString());
 					
 					logger.info(projectElementId.toString() + " : sheet index = " + sheetIndex);
 									
 					
 				}
 				
 			}
 			
 			
 			
 		} catch (PafException e) {
 			throw e;
 		}
 				
 	}
 	
 	public static PafExcelValueObject getDelimValueObjectFromStringAr(String[] stringAr) {
 		
 		PafExcelValueObject valueObject = null;
 		
 		if ( stringAr != null && stringAr.length > 0 ) {
 				
 			List<PafExcelValueObject> list = new ArrayList<PafExcelValueObject>();
 			
 			for ( String string : stringAr ) {
 				
 				list.add(PafExcelValueObject.createFromString(string));
 				
 			}
 			
 			valueObject = PafExcelUtil.getDelimValueObjectFromList(list);
 			
 		} else {
 			
 			valueObject = PafExcelValueObject.createBlank();
 			
 		}
 		
 		return valueObject;
 		
 	} 
 	
 	/**
 	 * 
 	 * Converts a list of PafExcelValueObjects into a single PafExcelValueObject that is delimited
 	 * by |'s between the values.  If all items are string, then the PafExcelValueObject will be a 
 	 * string with the |'s between the string values, but if formula, the entire list is parsed
 	 * into formula segements like this: =RuleSet_ContribPct!$A$2 & "|" & "StoreSales"
 	 *
 	 * @param list list of paf excel value objects
 	 * @return
 	 */
 	public static PafExcelValueObject getDelimValueObjectFromList(List<PafExcelValueObject> list) {
 		
 		PafExcelValueObject valueObject = null;
 		
 		if ( list != null && list.size() > 0 ) {
 				
 			boolean allStringValueObjects = true;
 			
 			for (PafExcelValueObject stringValueObject : list ) {
 				
 				if ( ! stringValueObject.isString() ) {
 					
 					allStringValueObjects = false;
 					break;
 				} 
 				
 			}
 			
 			StringBuilder sb = new StringBuilder();
 			
 			int strNdx = 0;
 			
 			for (PafExcelValueObject buildValueObject : list ) {
 			
 				if ( allStringValueObjects ) {
 					
 					sb.append(buildValueObject.getString());	
 					
 				} else {
 					
 					if ( buildValueObject.isFormula()) {
 					
 						sb.append(buildValueObject.getFormula());
 						
 					} else {
 					
 						sb.append("\"" + buildValueObject.getString() + "\"");
 						
 					}
 					
 				}
 								
 				if ( ++strNdx != list.size() ) {
 					
 					if ( allStringValueObjects ) {
 					
 						sb.append(" " + ExcelPaceProjectConstants.EXCEL_STRING_FIELD_DELIM + " ");
 						
 					} else {
 					
 						sb.append(" & \" " + ExcelPaceProjectConstants.EXCEL_STRING_FIELD_DELIM + " \" & ");
 					}
 				}
 				
 			}
 			
 			if ( allStringValueObjects ) {
 				
 				valueObject = PafExcelValueObject.createFromString(sb.toString());
 				
 			} else {
 			
 				valueObject = PafExcelValueObject.createFromFormula(sb.toString());
 				
 			}
 									
 		} else {
 		
 			valueObject = PafExcelValueObject.createBlank();
 			
 		}		
 		
 		return valueObject;
 		
 	}
 
 	/**
 	 * 
 	 * Deletes a sheet from a workbook
 	 *
 	 * @param workbook workbook to delete sheet from
 	 * @param sheetName name of sheet to delete
 	 */
 	public static void deleteSheet(Workbook workbook, String sheetName) {
 
 		if ( workbook != null && sheetName != null ) {
 		
 			int sheetIndex = workbook.getSheetIndex(sheetName);
 			
 			if ( sheetIndex >= 0 ) {
 	
 				logger.info("Deleting sheet: " + sheetName);
 				
 				workbook.removeSheetAt(sheetIndex);
 				
 			}
 		
 		}
 		
 	} 
 	
 }
