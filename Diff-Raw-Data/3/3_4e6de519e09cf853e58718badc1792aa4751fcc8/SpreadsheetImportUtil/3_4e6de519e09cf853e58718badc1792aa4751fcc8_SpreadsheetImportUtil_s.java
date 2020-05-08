 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.spreadsheetimport;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.sql.ResultSet;
 import java.sql.SQLSyntaxErrorException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.DateUtil;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.springframework.util.StringUtils;
 import org.springframework.web.multipart.MultipartFile;
 
 /**
  *
  */
 public class SpreadsheetImportUtil {
 	
 	/** Logger for this class and subclasses */
 	protected static final Log log = LogFactory.getLog(SpreadsheetImportUtil.class);
 	
 	/**
 	 * Resolve template dependencies: 1. Generate pre-specified values which are necessary for
 	 * template to be imported. 2. Create import indices which describe the order in which columns
 	 * must be imported. 3. Generated dependencies between columns being imported and other columns
 	 * which be must imported first.
 	 * 
 	 * @param template
 	 * @throws Exception
 	 */
 	public static void resolveTemplateDependencies(SpreadsheetImportTemplate template) throws Exception {
 		
 		Set<SpreadsheetImportTemplatePrespecifiedValue> prespecifiedValues = new TreeSet<SpreadsheetImportTemplatePrespecifiedValue>();
 		
 		Map<String, Set<UniqueImport>> mapTnToUi = template.getMapOfColumnTablesToUniqueImportSet();
 		Map<UniqueImport, Set<SpreadsheetImportTemplateColumn>> mapUiToCs = template.getMapOfUniqueImportToColumnSet();
 		
 		List<String> tableNamesSortedByImportIdx = new ArrayList<String>();
 		
 //		// special treatment: when there's a reference to person_id, but 
 //		//  1) the current table is not encounter and 
 //		//  2) there's no column of table person to be added
 //		// then we should still add a person implicitly. This person record will use all default values
 //		boolean hasToAddPerson = false;
 //		for (UniqueImport key : mapUiToCs.keySet()) {
 //			String tableName = key.getTableName();			
 //			if (!("encounter".equals(tableName) || mapTnToUi.keySet().contains("person"))) {
 //				hasToAddPerson = true;
 //				break;
 //			}
 //		}
 //		if (hasToAddPerson) {
 //			UniqueImport ui = new UniqueImport("person", new Integer(-1));
 //			mapTnToUi.put("person", new TreeSet<UniqueImport>());
 //			mapUiToCs.put(ui, new TreeSet<SpreadsheetImportTemplateColumn>());
 //		}
 				
 		// Find requirements
 		for (UniqueImport key : mapUiToCs.keySet()) {
 			String tableName = key.getTableName();
 			
 			Map<String, String> mapIkTnToCn = DatabaseBackend.getMapOfImportedKeyTableNameToColumnNamesForTable(tableName);
 			
 			// encounter_id is optional, so it won't be part of mapIkTnToCn
 			// if we need to create new encounter for this row, then force it to be here
 			if (template.isEncounter() && "obs".equals(tableName))
 				mapIkTnToCn.put("encounter", "encounter_id");
 
 			// we need special treatment for provider_id of Encounter
 			// provider_id is of type person, but the meaning is different. During import, reference to person is considered patient,
 			// but for provider_id of Encounter, it refers to a health practitioner
 			if ("encounter".equals(tableName)) {
 //				mapIkTnToCn.put("person", "provider_id");
 				mapIkTnToCn.put("location", "location_id");
 			}				
 			
 			// Ignore users tableName 
 			mapIkTnToCn.remove("users");
 			
 			for (String necessaryTableName : mapIkTnToCn.keySet()) {
 
 				String necessaryColumnName = mapIkTnToCn.get(necessaryTableName);
 
 				// TODO: I believe patient and person are only tables with this relationship, if not, then this
 				// needs to be generalized
 				if (necessaryTableName.equals("patient") &&
 					!mapTnToUi.containsKey("patient") &&
 					mapTnToUi.containsKey("person")) {
 					necessaryTableName = "person";
 				}
 				
 				if (mapTnToUi.containsKey(necessaryTableName) && !("encounter".equals(tableName) && ("provider_id".equals(necessaryColumnName)))) {
 					
 					// Not already imported? Add
 					if (!tableNamesSortedByImportIdx.contains(necessaryTableName)) {
 						tableNamesSortedByImportIdx.add(necessaryTableName);
 					}
 					
 					// Add column dependencies
 					// TODO: really _table_ dependencies - for simplicity only use _first_ column
 					// of each unique import
 					Set<SpreadsheetImportTemplateColumn> columnsImportFirst = new TreeSet<SpreadsheetImportTemplateColumn>();
 					for (UniqueImport uniqueImport : mapTnToUi.get(necessaryTableName)) {
 						// TODO: hacky cast
 						columnsImportFirst.add(((TreeSet<SpreadsheetImportTemplateColumn>)mapUiToCs.get(uniqueImport)).first());
 					}
 					for (SpreadsheetImportTemplateColumn columnImportNext : mapUiToCs.get(key)) {
 						for (SpreadsheetImportTemplateColumn columnImportFirst : columnsImportFirst) {
 							SpreadsheetImportTemplateColumnColumn cc = new SpreadsheetImportTemplateColumnColumn();
 							cc.setColumnImportFirst(columnImportFirst);
 							cc.setColumnImportNext(columnImportNext);
 							cc.setColumnName(necessaryColumnName);
 							columnImportNext.getColumnColumnsImportBefore().add(cc);
 						}
 					}
 					
 				} else {
 					
 					// Add pre-specified value
 					SpreadsheetImportTemplatePrespecifiedValue v = new SpreadsheetImportTemplatePrespecifiedValue();
 					v.setTemplate(template);
 					v.setTableDotColumn(necessaryTableName + "." + necessaryTableName + "_id");
 					for (SpreadsheetImportTemplateColumn column : mapUiToCs.get(key)) {
 						SpreadsheetImportTemplateColumnPrespecifiedValue cpv = new SpreadsheetImportTemplateColumnPrespecifiedValue();
 						cpv.setColumn(column);
 						cpv.setPrespecifiedValue(v);
 						
 						
 //						System.out.println("SpreadsheetImportUtils: " + v.getTableDotColumn() + " ==> " + v.getValue());
 						
 						cpv.setColumnName(necessaryColumnName);						
 						v.getColumnPrespecifiedValues().add(cpv);
 					}
 					prespecifiedValues.add(v);
 				}
 			}
 			
 			// Add this tableName if not already added
 			if (!tableNamesSortedByImportIdx.contains(tableName)) {
 				tableNamesSortedByImportIdx.add(tableName);
 			}
 		}
 		
 		// Add all pre-specified values		
 		template.getPrespecifiedValues().addAll(prespecifiedValues);
 		
 		// Set column import indices based on tableNameSortedByImportIdx
 		int importIdx = 0;
 		for (String tableName : tableNamesSortedByImportIdx) {
 			for (UniqueImport uniqueImport : mapTnToUi.get(tableName)) {
 				for (SpreadsheetImportTemplateColumn column : mapUiToCs.get(uniqueImport)) {
 					column.setImportIdx(importIdx);
 					importIdx++;
 				}
 			}
 		}
 	}
 	
 	private static String toString(List<String> list) {
 		String result = "";
 		for (int i = 0; i < list.size(); i++) {
 			if (list.size() == 2 && i == 1) {
 				result += " and ";
 			} else if (list.size() > 2 && i == list.size() - 1) {
 				result += ", and ";
 			} else if (i != 0) {
 				result += ", ";
 			}
 			result += list.get(i);
 		}
 		return result;
 	}
 	
 	public static File importTemplate(SpreadsheetImportTemplate template, MultipartFile file, String sheetName,
 	                                     List<String> messages, boolean rollbackTransaction) throws Exception {
 
 		if (file.isEmpty()) {
 			messages.add("file must not be empty");
 			return null;
 		}
 		
 		// Open file
 		Workbook wb = WorkbookFactory.create(file.getInputStream());
 		Sheet sheet;
 		if (!StringUtils.hasText(sheetName)) {
 			sheet = wb.getSheetAt(0);
 		} else {
 			sheet = wb.getSheet(sheetName);
 		}
 		
 		// Header row
 		Row firstRow = sheet.getRow(0);
 		if (firstRow == null) {
 			messages.add("Spreadsheet header row must not be null");
 			return null;
 		}
 		
 		List<String> columnNames = new Vector<String>();
 		for (Cell cell : firstRow) {
 			columnNames.add(cell.getStringCellValue());
 		}
 		if (log.isDebugEnabled()) {
 			log.debug("Column names: " + columnNames.toString());
 		}
 		
 		// Required column names
 		List<String> columnNamesOnlyInTemplate = new Vector<String>();
 		columnNamesOnlyInTemplate.addAll(template.getColumnNamesAsList());
 		columnNamesOnlyInTemplate.removeAll(columnNames);
 		if (columnNamesOnlyInTemplate.isEmpty() == false) {
 			messages.add("required column names not present: " + toString(columnNamesOnlyInTemplate));
 			return null;
 		}
 		
 		// Extra column names?
 		List<String> columnNamesOnlyInSheet = new Vector<String>();
 		columnNamesOnlyInSheet.addAll(columnNames);
 		columnNamesOnlyInSheet.removeAll(template.getColumnNamesAsList());
 		if (columnNamesOnlyInSheet.isEmpty() == false) {
 			messages.add("Extra column names present, these will not be processed: " + toString(columnNamesOnlyInSheet));
 		}
 		
 		// Process rows
 		boolean skipThisRow = true;
 		for (Row row : sheet) {
 			if (skipThisRow == true) {
 				skipThisRow = false;
 			} else {
 				boolean rowHasData = false;
 				Map<UniqueImport, Set<SpreadsheetImportTemplateColumn>> rowData = template
 				        .getMapOfUniqueImportToColumnSetSortedByImportIdx();
 				
 				for (UniqueImport uniqueImport : rowData.keySet()) {
 					Set<SpreadsheetImportTemplateColumn> columnSet = rowData.get(uniqueImport);
 					for (SpreadsheetImportTemplateColumn column : columnSet) {
 												
 						int idx = columnNames.indexOf(column.getName());
 						Cell cell = row.getCell(idx);
 						
 						Object value = null;
 						// check for empty cell (new Encounter)
 						if (cell == null) {
 							rowHasData = true;
 							column.setValue("");
 							continue;
 						}
 						
 						switch (cell.getCellType()) {
 							case Cell.CELL_TYPE_BOOLEAN:
 								value = new Boolean(cell.getBooleanCellValue());
 								break;
 							case Cell.CELL_TYPE_ERROR:
 								value = new Byte(cell.getErrorCellValue());
 								break;
 							case Cell.CELL_TYPE_FORMULA:
 							case Cell.CELL_TYPE_NUMERIC:
 								if (DateUtil.isCellDateFormatted(cell)) {									
 									java.util.Date date = cell.getDateCellValue();
 									java.sql.Date sqlDate = new java.sql.Date(date.getTime());
 									value = "'" + sqlDate.toString() + "'";
 								} else {
 									value = cell.getNumericCellValue();
 								}
 								break;
 							case Cell.CELL_TYPE_STRING:
 								// Escape for SQL
 								value = "'" + cell.getRichStringCellValue() + "'";
 								break;
 						}
 						if (value != null) {
 							rowHasData = true;
 							column.setValue(value);
						}
 					}
 				}
 				
 				for (UniqueImport uniqueImport : rowData.keySet()) {
 					Set<SpreadsheetImportTemplateColumn> columnSet = rowData.get(uniqueImport);
 					boolean isFirst = true;
 					for (SpreadsheetImportTemplateColumn column : columnSet) {
 
 						if (isFirst) {
 							// Should be same for all columns in unique import
 //							System.out.println("SpreadsheetImportUtil.importTemplate: column.getColumnPrespecifiedValues(): " + column.getColumnPrespecifiedValues().size());
 							if (column.getColumnPrespecifiedValues().size() > 0) {
 								Set<SpreadsheetImportTemplateColumnPrespecifiedValue> columnPrespecifiedValueSet = column.getColumnPrespecifiedValues();
 								for (SpreadsheetImportTemplateColumnPrespecifiedValue columnPrespecifiedValue : columnPrespecifiedValueSet) {
 //									System.out.println(columnPrespecifiedValue.getPrespecifiedValue().getValue());
 								}
 							}
 						}
 					}
 				}
 				
 				
 				
 				if (rowHasData) {
 					Exception exception = null;
 					try {
 						DatabaseBackend.validateData(rowData);
 						String encounterId = DatabaseBackend.importData(rowData, rollbackTransaction);
 						if (encounterId != null) {
 							for (UniqueImport uniqueImport : rowData.keySet()) {
 								Set<SpreadsheetImportTemplateColumn> columnSet = rowData.get(uniqueImport);
 								for (SpreadsheetImportTemplateColumn column : columnSet) {
 									if ("encounter".equals(column.getTableName())) {						
 										int idx = columnNames.indexOf(column.getName());
 										Cell cell = row.getCell(idx);
 										if (cell == null)											
 											cell = row.createCell(idx);
 										cell.setCellValue(encounterId);
 									}
 								}
 							}
 						}
 					} catch (SpreadsheetImportTemplateValidationException e) {
 						messages.add("Validation failed: " + e.getMessage());
 						return null;
 					} catch (SpreadsheetImportDuplicateValueException e) {
 						messages.add("found duplicate value for column " + e.getColumn().getName() + " with value " + e.getColumn().getValue());
 						return null;
 					} catch (SpreadsheetImportSQLSyntaxException e) {
 						messages.add("SQL syntax error: \"" + e.getSqlErrorMessage() + "\".<br/>Attempted SQL Statement: \"" + e.getSqlStatement() + "\"");
 						return null;
 					} catch (Exception e) {
 						exception = e;
 					}
 					if (exception != null) {
 						throw exception;
 					}
 				}
 			}
 		}
 		
 		// write back Excel file to a temp location
 		File returnFile = File.createTempFile("sim", ".xls");
 		FileOutputStream fos = new FileOutputStream(returnFile);
 		wb.write(fos);
 		fos.close();
 		
 		return returnFile;
 	}
 }
