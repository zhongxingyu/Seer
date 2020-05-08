 package com.pace.base.project.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import com.pace.base.PafException;
 import com.pace.base.project.ExcelPaceProjectConstants;
 import com.pace.base.project.ExcelProjectDataErrorException;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.excel.PafExcelInput;
 import com.pace.base.project.excel.PafExcelRow;
 import com.pace.base.project.excel.PafExcelValueObject;
 import com.pace.base.project.excel.PafExcelValueObjectType;
 
 public class PafExcelUtilTest extends TestCase {
 
 	private static Logger logger = Logger.getLogger(PafExcelUtilTest.class);
 	
 	String emptyWorkbookName = "./test_files/EmptyWorkbook.xlsx";
 	
 	String sampleWorkbookName = "./test_files/Sample.xlsx";
 	
 	String projectWorkbookName = "./test_files/project-template.xlsx";
 	
 	String blankWorkbookWithMacros = "./test_files/blank.xlsm";
 	
 	private static final String EXCEL_PROJECT_DATA_ERROR_EXCEPTION_SHOULD_HAVE_BEEN_THROWN = "ExcelProjectDataErrorException should have been thrown";
 	List<PafExcelValueObject> nullExcelValueList = null;
 	
 	public PafExcelUtilTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		
 		super.setUp();
 		
 		nullExcelValueList = new ArrayList<PafExcelValueObject>();
 		
 		for (int i = 0; i < 5; i++) {
 			
 			nullExcelValueList.add(PafExcelValueObject.createFromString(null));
 			
 		}
 		
 	}
 
 	protected void tearDown() throws Exception {
 		
 		super.tearDown();
 		
 		nullExcelValueList = null;
 	}
 
 	public void testReadExcelSheet() {
 
 		PafExcelInput input = new PafExcelInput.Builder(emptyWorkbookName, "Sheet1", 1).build();
 		
 		try {
 			
 logger.info("Full workbook name: [" + input.getFullWorkbookName().toString() + "]");			
 			List<PafExcelRow> pafExcelRows = PafExcelUtil.readExcelSheet(input);
 	logger.info("emptyWorkbookName:[" + emptyWorkbookName.toString() + "]");		
 			assertNotNull(pafExcelRows);
 			assertEquals(1, pafExcelRows.size());
 	logger.info("should not get here");		
 			input = new PafExcelInput.Builder(emptyWorkbookName, "Sheet1", 10).build();
 			
 			pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertNotNull(pafExcelRows);
 			assertEquals(1, pafExcelRows.size());
 			
 			input = new PafExcelInput.Builder(emptyWorkbookName, "Sheet1", 1).excludeEmptyRows(true).build();
 									
 			pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertNotNull(pafExcelRows);
 			assertEquals(0, pafExcelRows.size());
 			
 			input = new PafExcelInput.Builder(sampleWorkbookName, "Sheet1", 6).excludeEmptyRows(true).build();
 			
 			pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertNotNull(pafExcelRows);
 			assertEquals(3, pafExcelRows.size());			
 			
 			for (PafExcelRow row : pafExcelRows) {
 
 				int ndx = 0;
 
 				assertNotNull(row.getPafExcelValueObjectListMap());
 
 				for (Integer index : row.getRowItemOrderedIndexes()) {
 
 					List<PafExcelValueObject> rowItemList = row.getRowItem(index);
 					
 					assertNotNull(rowItemList);
 					
 					assertEquals(1, rowItemList.size());
 					
 					PafExcelValueObject rowItem = rowItemList.get(0);
 					
 					switch (ndx++) {
 
 					case 0:
 						
 						if ( rowItem.getString() == null ) {
 							
 							assertTrue(rowItem.isType(PafExcelValueObjectType.Blank));
 							assertNull(rowItem.getString());
 							assertEquals("", rowItem.getValueAsString());
 							
 						} else {
 						
 							assertTrue(rowItem.isType(PafExcelValueObjectType.String));
 							assertEquals("Hello", rowItem.getString());
 							assertEquals("Hello", rowItem.getValueAsString());
 							
 						}
 						
 						
 
 						break;
 					case 1:
 						assertTrue(rowItem.isType(PafExcelValueObjectType.Numeric));
 						assertEquals(new Integer(123), rowItem.getInteger());
 						assertEquals(new Double(123), rowItem.getDouble());
 						break;
 
 					case 2:
 						assertTrue(rowItem.isType(PafExcelValueObjectType.Numeric));
 						assertEquals(Integer.valueOf(123), rowItem.getInteger());
 						assertEquals(Double.valueOf(123.22), rowItem.getDouble());
 						break;
 
 					case 3:
 						assertTrue(rowItem.isType(PafExcelValueObjectType.Boolean));
 						assertNotNull(rowItem.getBoolean());
 						assertTrue(rowItem.getBoolean());
 
 						break;
 
 					case 4:
 
 						assertTrue(rowItem.isType(PafExcelValueObjectType.String));
 						assertEquals("  ", rowItem.getString());
 						assertEquals("  ", rowItem.getValueAsString());
 
 						break;
 
 					case 5:
 
 						assertTrue(rowItem.isType(PafExcelValueObjectType.Blank));
 
 						break;
 
 					}
 				}
 				
 			}
 									
 			input = new PafExcelInput.Builder(sampleWorkbookName, "Sheet1", 6).build();
 
 			pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertNotNull(pafExcelRows);
 			assertEquals(5, pafExcelRows.size());
 
 			Map<String, List<String>> headerListMap = new HashMap<String, List<String>>();
 			
 			headerListMap.put(ProjectElementId.DynamicMembers.toString(), Arrays.asList("dimension", "member"));
 			
 			input = new PafExcelInput.Builder(projectWorkbookName, ProjectElementId.DynamicMembers.toString(), 2)
 											.headerListMap(headerListMap)
 											.multiDataRow(true)
 											.excludeHeaderRows(true)
 											.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 											.build();
 
 			pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertEquals(1, pafExcelRows.size());
 
 			
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 	
 	public void testGetString() {
 
 		PafExcelInput input = new PafExcelInput.Builder(sampleWorkbookName, "Sheet1", 1).build();
 		
 		try {
 			
 			List<PafExcelRow> pafExcelRows = PafExcelUtil.readExcelSheet(input);
 			
 			assertNotNull(pafExcelRows);
 			assertEquals(5, pafExcelRows.size());
 			
 			PafExcelRow row = pafExcelRows.get(0);
 			
 			
 			List<PafExcelValueObject> valueObjectList = row.getRowItem(0);
 			
 			assertNotNull(valueObjectList);
 			
 			PafExcelValueObject valueObject = valueObjectList.get(0);
 			
 			if ( valueObject.isBlank()) {
 				
 				assertNull(valueObject.getString());
 				
 			} else {
 				
 				assertEquals("Hello", valueObject.getString());
 			}
 			
 			
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}		
 		
 	}
 
 	public void testGetWorkbookSheetNames() {
 
 		try {
 			
 			List<String> workbookNames = PafExcelUtil.getWorkbookSheetNames(projectWorkbookName);
 			
 			assertNotNull(workbookNames);
			assertEquals(35, workbookNames.size());
 			
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 	
 	public void testCreateExcelReferenceMap() {
 		
 		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
 		
 		String key = ProjectElementId.NumericFormats.toString();
 		
 		headerMap.put(key, Arrays.asList("name"));
 		
 		PafExcelInput input = new PafExcelInput.Builder(projectWorkbookName, key, 1)
 				.headerListMap(headerMap)
 				.excludeHeaderRows(true)
 				.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 				.build();
 
 		
 		try {
 			
 			Map<String, String> map = PafExcelUtil.createCellReferenceMap(input);
 			
 			assertNotNull(map);
 			assertEquals(14, map.size());
 			
 			for (String mapKey : map.keySet()) {
 				
 				assertTrue(map.get(mapKey).contains(key));
 				
 				
 			}
 			
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 
 	public void testConvertBlanksToNullInList() {
 		
 		assertNotNull(PafExcelUtil.convertBlanksToNullInList(null));
 		assertEquals(0, PafExcelUtil.convertBlanksToNullInList(null).size());
 		
 		PafExcelValueObject str1 = PafExcelValueObject.createFromString("str1");
 		PafExcelValueObject blank1 = PafExcelValueObject.createBlank();
 		PafExcelValueObject str2 = PafExcelValueObject.createFromString("str2");
 		PafExcelValueObject blank2 = PafExcelValueObject.createBlank();
 		
 		List<PafExcelValueObject> initialList = Arrays.asList(str1, blank1, str2, blank2);
 		
 		List<PafExcelValueObject> updatedList = PafExcelUtil.convertBlanksToNullInList(initialList);
 		
 		assertEquals(initialList.size(), updatedList.size());
 		
 		for (int i = 0; i < initialList.size(); i++) {
 			
 			PafExcelValueObject initialValueObject = initialList.get(i);
 			PafExcelValueObject updatedValueObject = updatedList.get(i);
 			
 			if ( initialValueObject.isString() ) {
 				
 				assertTrue(updatedValueObject.isString());
 				
 			} else if ( initialValueObject.isBlank() ) { 
 				
 				assertNull(updatedValueObject);
 				
 			} else {
 				
 				fail("Should not make it here");
 				
 			}
 			
 		}
 		
 	}
 	
 	public void testIsHeader() {
 		
 		assertFalse(PafExcelUtil.isHeaderRow(null, null));
 		
 		Workbook wb = new XSSFWorkbook();
 		
 		PafExcelInput input = new PafExcelInput.Builder(wb, "TestSheet", 1).build();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, null));
 		
 		PafExcelRow row = new PafExcelRow();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(null, row));
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		row.setHeader(true);
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, null));
 				
 		Map<String, List<String>> headerListMap = new HashMap<String, List<String>>();
 		
 		List<String> headerList1 = new ArrayList<String>();
 
 		String strValue = "one";
 		String strValue2 = "two";
 		String strValue3 = "three";
 		
 		headerList1.add(strValue);
 		headerList1.add(strValue2);
 		headerList1.add(strValue3);
 		
 		headerListMap.put("HEADER1", headerList1);
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 1)
 			.headerListMap(headerListMap)
 			.build();		
 		
 		Map<Integer, List<PafExcelValueObject>> rowDataMap = new HashMap<Integer, List<PafExcelValueObject>>();
 		
 		rowDataMap.put(0, Arrays.asList(PafExcelValueObject.createFromString(strValue)));
 		rowDataMap.put(1, Arrays.asList(PafExcelValueObject.createFromString(strValue2)));
 		rowDataMap.put(2, Arrays.asList(PafExcelValueObject.createFromString(strValue3)));
 		
 		row.setPafExcelValueObjectListMap(rowDataMap);
 	
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 2)
 		.headerListMap(headerListMap)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 4)
 		.headerListMap(headerListMap)
 		.build();
 					
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(0)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(1)
 		.build();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(2)
 		.build();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(3)
 		.build();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(4)
 		.build();
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		rowDataMap.clear();
 		
 		row.setPafExcelValueObjectListMap(rowDataMap);
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 1)
 		.headerListMap(headerListMap)
 		//.startDataReadColumnIndex(0)
 		.build();		
 		
 		assertFalse(PafExcelUtil.isHeaderRow(input, row));
 		
 		rowDataMap.put(0, Arrays.asList(PafExcelValueObject.createFromString(strValue)));
 		//rowDataMap.put(1, Arrays.asList(PafExcelValueObject.createFromString(strValue2)));
 		//rowDataMap.put(2, Arrays.asList(PafExcelValueObject.createFromString(strValue3)));
 
 		row.setPafExcelValueObjectListMap(rowDataMap);
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 1)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(0)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		rowDataMap.clear();
 		
 		//rowDataMap.put(0, Arrays.asList(PafExcelValueObject.createFromString(strValue)));
 		rowDataMap.put(1, Arrays.asList(PafExcelValueObject.createFromString(strValue2)));
 		//rowDataMap.put(2, Arrays.asList(PafExcelValueObject.createFromString(strValue3)));
 
 		row.setPafExcelValueObjectListMap(rowDataMap);
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 2)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(1)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 		rowDataMap.clear();
 		
 		//rowDataMap.put(0, Arrays.asList(PafExcelValueObject.createFromString(strValue)));
 		//rowDataMap.put(1, Arrays.asList(PafExcelValueObject.createFromString(strValue2)));
 		rowDataMap.put(2, Arrays.asList(PafExcelValueObject.createFromString(strValue3)));
 
 		row.setPafExcelValueObjectListMap(rowDataMap);
 		
 		input = new PafExcelInput.Builder(wb, "TestSheet", 3)
 		.headerListMap(headerListMap)
 		.startDataReadColumnIndex(2)
 		.build();
 		
 		assertTrue(PafExcelUtil.isHeaderRow(input, row));
 		
 	}
 
 	/*public void testGetStringProjectElementIdPafExcelValueObjectBoolean() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetStringProjectElementIdPafExcelValueObjectListOfString() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetStringProjectElementIdPafExcelValueObjectBooleanListOfString() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetIntegerProjectElementIdPafExcelValueObject() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetIntegerProjectElementIdPafExcelValueObjectBoolean() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetBooleanProjectElementIdPafExcelValueObject() {
 		fail("Not yet implemented");
 	}
 
 	public void testGetBooleanProjectElementIdPafExcelValueObjectBoolean() {
 		fail("Not yet implemented");
 	}
 
 
 	public void testGetStringProjectElementIdPafExcelValueObject() {
 		fail("Not yet implemented");
 	}
 */
 
 	public void testGetStringArProjectElementIdListOfPafExcelValueObject() {
 						
 		try {
 			
 			String[] nullAr = PafExcelUtil.getStringAr(null, nullExcelValueList);
 			
 			assertNotNull(nullAr);
 			assertEquals(5, nullAr.length);
 			
 			for (String nullStr : nullAr) {
 				
 				assertNull(nullStr);
 				
 			}
 			
 		} catch (ExcelProjectDataErrorException e) {
 			
 			fail(e.getMessage());
 			
 		}
 		
 	}
 
 	public void testGetStringArProjectElementIdListOfPafExcelValueObjectBoolean() {
 
 		try {
 			
 			String[] nullAr = PafExcelUtil.getStringAr(null, nullExcelValueList, false);
 			
 			assertNotNull(nullAr);
 			assertEquals(5, nullAr.length);
 			
 			for (String nullStr : nullAr) {
 				
 				assertNull(nullStr);
 				
 			}
 									
 		} catch (ExcelProjectDataErrorException e) {
 			
 			fail(e.getMessage());
 			
 		}
 		
 		try {
 			
 			PafExcelUtil.getStringAr(null, nullExcelValueList, true);
 			
 			fail(EXCEL_PROJECT_DATA_ERROR_EXCEPTION_SHOULD_HAVE_BEEN_THROWN);
 									
 		} catch (ExcelProjectDataErrorException e) {
 			
 			assertNotNull(e.getProjectDataError());
 			
 		}
 		
 		nullExcelValueList.add(2, PafExcelValueObject.createFromString("stringValue"));
 		nullExcelValueList.add(3, PafExcelValueObject.createFromFormula("formulaValue"));
 		nullExcelValueList.add(4, PafExcelValueObject.createBlank());
 		
 		try {
 			
 			String[] nullAr = PafExcelUtil.getStringAr(null, nullExcelValueList, false);
 			
 			assertNotNull(nullAr);
 			assertEquals(8, nullAr.length);
 			
 			for (int i = 0; i < nullAr.length; i++ ) {
 				
 				switch(i) {
 				
 				case 0:
 				case 1:
 				case 4:
 				case 5:
 				case 6:
 				case 7:
 				case 8:
 					
 					assertNull(nullAr[i]);
 					break;
 				case 2:
 				case 3:
 									
 					assertNotNull(nullAr[i]);
 					break;
 				
 				}
 				
 			}
 									
 		} catch (ExcelProjectDataErrorException e) {
 			
 			fail(e.getMessage());
 			
 		}
 		
 	}
 
 	public void testGetStringArFromDelimValueObjectProjectElementIdPafExcelValueObject() {
 		
 		try {
 			
 			String[] strAr = PafExcelUtil.getStringArFromDelimValueObject(null, null);
 		
 			assertNull(strAr);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		String strValue = "Str1";
 		
 		PafExcelValueObject valueObject = PafExcelValueObject.createFromString(strValue);
 		
 		try {
 			
 			String[] strAr = PafExcelUtil.getStringArFromDelimValueObject(null, valueObject);
 		
 			assertNotNull(strAr);
 			assertEquals(1, strAr.length);
 			assertEquals(strValue, strAr[0]);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		valueObject = PafExcelValueObject.createFromString("Str1 | Str2|Str3");
 		
 		try {
 			
 			String[] strAr = PafExcelUtil.getStringArFromDelimValueObject(null, valueObject);
 		
 			assertNotNull(strAr);
 			assertEquals(3, strAr.length);
 			assertEquals("Str1", strAr[0]);
 			assertEquals("Str2", strAr[1]);
 			assertEquals("Str3", strAr[2]);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		
 	}
 
 	public void testGetStringArFromDelimValueObjectProjectElementIdPafExcelValueObjectBoolean() {
 
 		try {
 			
 			PafExcelUtil.getStringArFromDelimValueObject(null, null, true);
 		
 			fail(EXCEL_PROJECT_DATA_ERROR_EXCEPTION_SHOULD_HAVE_BEEN_THROWN);
 						
 		} catch (ExcelProjectDataErrorException e) {
 			
 		}
 		
 		String strValue = "Str1";
 		
 		PafExcelValueObject valueObject = PafExcelValueObject.createFromString(strValue);
 		
 		try {
 			
 			String[] strAr = PafExcelUtil.getStringArFromDelimValueObject(null, valueObject, true);
 		
 			assertNotNull(strAr);
 			assertEquals(1, strAr.length);
 			assertEquals(strValue, strAr[0]);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		valueObject = PafExcelValueObject.createFromString("Str1 | Str2|Str3");
 		
 		try {
 			
 			String[] strAr = PafExcelUtil.getStringArFromDelimValueObject(null, valueObject, true);
 		
 			assertNotNull(strAr);
 			assertEquals(3, strAr.length);
 			assertEquals("Str1", strAr[0]);
 			assertEquals("Str2", strAr[1]);
 			assertEquals("Str3", strAr[2]);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 
 	public void testGetHexNumber() {
 		
 		
 		ProjectElementId projectElementId = ProjectElementId.ApplicationDef;
 		
 		try {
 			
 			String hexNumber = PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString(null));
 			
 			assertNull(hexNumber);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			
 			String hexNumber = PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString(""));
 			assertNull(hexNumber);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			
 			PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString("0"));
 			
 			fail("Should have thrown exception.");
 						
 		} catch (ExcelProjectDataErrorException e) {
 			
 		}
 		
 		try {
 			
 			PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString("0000000"));
 			
 			fail("Should have thrown exception.");
 						
 		} catch (ExcelProjectDataErrorException e) {
 			
 		}
 		
 		try {
 			
 			String hexNumber = PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString("00aaff"));
 			assertNotNull(hexNumber);
 			assertEquals("00AAFF", hexNumber);
 			
 		} catch (ExcelProjectDataErrorException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			
 			PafExcelUtil.getHexNumber(projectElementId, PafExcelValueObject.createFromString("abcdeh"));
 			
 			fail("Should have thrown exception.");
 						
 		} catch (ExcelProjectDataErrorException e) {
 			
 		}
 		
 		
 	}
 
 	public void testGetDelimValueObjectFromStringAr() {
 
 		PafExcelValueObject valueObject  = PafExcelUtil.getDelimValueObjectFromStringAr(null);
 		
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isBlank());	
 								
 		valueObject  = PafExcelUtil.getDelimValueObjectFromStringAr(new String[] {});
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isBlank());		
 		
 		String one = "one";
 		valueObject  = PafExcelUtil.getDelimValueObjectFromStringAr(new String[] {one});
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isString());
 		
 		String two = "two";
 		valueObject  = PafExcelUtil.getDelimValueObjectFromStringAr(new String[] {one, two});
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isString());
 		assertEquals(one + " | " + two, valueObject.getString());
 		
 		//assertTrue(valueObject.isType(PafExcelValueObjectType.Formula));
 		//assertEquals(one + " & \"|\" & " + two, valueObject.getFormula());
 		
 		
 	}
 	
 	public void testGetDelimValueObjectFromList() {
 		
 		PafExcelValueObject valueObject  = PafExcelUtil.getDelimValueObjectFromList(null);
 		
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.Blank));
 						
 		valueObject  = PafExcelUtil.getDelimValueObjectFromList(new ArrayList<PafExcelValueObject>());
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.Blank));		
 		
 		String one = "one";
 		PafExcelValueObject oneValueObject = PafExcelValueObject.createFromString(one);
 		
 		valueObject  = PafExcelUtil.getDelimValueObjectFromList(Arrays.asList(oneValueObject));
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.String));
 		assertEquals(one, valueObject.getString());
 		
 		String two = "two";
 		PafExcelValueObject twoValueObject = PafExcelValueObject.createFromString(two);
 				
 		valueObject  = PafExcelUtil.getDelimValueObjectFromList(Arrays.asList(oneValueObject, twoValueObject));
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.String));
 		assertEquals(one + " | " + two, valueObject.getString());
 		
 		String formula = "CustomMenus!$A$39";
 		
 		PafExcelValueObject threeValueObject = PafExcelValueObject.createFromFormula(formula);
 		
 		valueObject  = PafExcelUtil.getDelimValueObjectFromList(Arrays.asList(oneValueObject, twoValueObject, threeValueObject));
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.Formula));
 		assertEquals("\"" + one + "\" & \" | \" & \"" + two + "\" & \" | \" & " + formula , valueObject.getFormula());
 		
 		valueObject  = PafExcelUtil.getDelimValueObjectFromList(Arrays.asList(threeValueObject, twoValueObject, oneValueObject));
 		assertNotNull(valueObject);
 		assertTrue(valueObject.isType(PafExcelValueObjectType.Formula));
 		assertEquals(formula + " & \" | \" & \"" + two + "\" & \" | \" & \"" + one + "\"", valueObject.getFormula());
 		
 	}
 	
 	/*public void testWorkbookWithEnabledMacros() {
 		
 		
 		String blankWorkbookWithMacrosCopy = blankWorkbookWithMacros.replaceAll("blank", "blankCopy");
 		
 		File blankWorkbookWithMacrosFile = new File(blankWorkbookWithMacros);
 		File blankWorkbookWithMacrosCopyFile = new File(blankWorkbookWithMacrosCopy);
 		
 		if ( blankWorkbookWithMacrosCopyFile.exists()) {
 			
 			blankWorkbookWithMacrosCopyFile.delete();
 			
 		}
 		
 		
 		assertFalse(blankWorkbookWithMacrosCopyFile.exists());		
 		
 		try {
 			
 			FileUtils.copy(blankWorkbookWithMacrosFile, blankWorkbookWithMacrosCopyFile);
 			
 		} catch (IOException e) {
 			
 			fail(e.getMessage());
 		}	
 		
 		assertTrue(blankWorkbookWithMacrosCopyFile.exists());		
 		
 		try {
 			ExcelPaceProject eppIn = new ExcelPaceProject(projectWorkbookName);
 			
 			assertNotNull(eppIn);
 			
 			ExcelPaceProject eppOut = new ExcelPaceProject(blankWorkbookWithMacrosCopy, new HashSet<ProjectElementId>(Arrays.asList(ProjectElementId.ApplicationDef)));
 			
 			assertNotNull(eppOut);
 			
 			eppOut.setApplicationDefinitions(eppIn.getApplicationDefinitions());
 			
 			eppOut.save(ProjectElementId.ApplicationDef);	
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 	}*/
 	
 	
 }
