 package com.pace.base.project.excel.elements;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.poi.ss.usermodel.Workbook;
 
import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.project.ExcelPaceProjectConstants;
 import com.pace.base.project.ExcelProjectDataErrorException;
 import com.pace.base.project.PaceProjectReadException;
 import com.pace.base.project.PaceProjectWriteException;
import com.pace.base.project.ProjectDataError;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.excel.IExcelDynamicReferenceElementItem;
 import com.pace.base.project.excel.PafExcelInput;
 import com.pace.base.project.excel.PafExcelRow;
 import com.pace.base.project.excel.PafExcelValueObject;
 import com.pace.base.project.utils.PafExcelUtil;
 import com.pace.base.ui.PrintStyle;
 import com.pace.base.utility.GUIDUtil;
 
 public class PrintStylesExcelElementItem<T extends Map<String, PrintStyle>> extends PafExcelElementItem<T> 
 		implements IExcelDynamicReferenceElementItem {
 
 	public PrintStylesExcelElementItem(Workbook workbook) {
 		super(workbook);
 	}
 
 	public PrintStylesExcelElementItem(Workbook workbook,
 			boolean isCellReferencingEnabled) {
 		super(workbook, isCellReferencingEnabled);
 	}
 
 	@Override
 	protected void createHeaderListMapEntries() {
 		getHeaderListMap().put(getSheetName(), Arrays.asList("GUID", "Name","Default Style?",
 				"Portait", "Landscape",  "Adjust To", "% Normal Size", "Fit To", "Page(s) Wide", "Page(s) Tall", "Paper Size", "First Page Number",//Page Tab
 				"Header Margin","Top Margin","Left Margin","Right Margin","Bottom Margin","Footer Margin","Center Horizontally","Center Vertically",//Margins Tab
 				"Header", "Footer","Different odd and even pages","Different first page","Scale with document","Align with page margins",//Header/Footer Tab
 				"Entire View","User Selection", "User Selected Print Area", "Rows to repeat at top","Columns to repeat at left","Gridlines","Black and white","Draft quality","Row and column headings","Comments","Cell errors as","Down, then Over", "Over, then Down")); //Sheet Tab
 	}
 	
 	@Override
 	public Map<String, String> getDynamicReferenceMap() {
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected T readExcelSheet() throws PaceProjectReadException,
 			PafException {
 		// TODO Auto-generated method stub
 		PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), getSheetName(), getHeaderListMap().get(getSheetName()).size())
 		.headerListMap(getHeaderListMap())
 		.excludeHeaderRows(true)
 		.excludeEmptyRows(true)
 		.sheetRequired(true)
 		.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 		.build();
 
 		List<PafExcelRow> excelRowList = PafExcelUtil.readExcelSheet(input);
 	
 		Map<String, PrintStyle> printStylesMap = new TreeMap<String, PrintStyle>();
 				
 		PrintStyle printStyle = null;
 		
 		for ( PafExcelRow row : excelRowList ) {
 		
 			printStyle = new PrintStyle();
 			
 			for ( Integer rowIndex : row.getRowItemOrderedIndexes()) {
 			
 				List<PafExcelValueObject> rowItemList = row.getRowItem(rowIndex);
 				
 				PafExcelValueObject firstValueObject = rowItemList.get(0);
 			
 				try {
 				
 					switch (rowIndex) {
 					
 						//"GUID"
 						case 0:											
 							printStyle.setGUID(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 					
 						//"Name" (Print Style Name)
 						case 1:											
 							printStyle.setName(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//"Default Print Style?" 
 						case 2:											
 							printStyle.setDefaultStyle(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//"Portrait"
 						case 3:
 							printStyle.setPortrait(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Landscape"
 						case 4:
 							printStyle.setLandscape(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
							printStyle.setFitTo(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
							if( ( printStyle.getPortrait() && printStyle.getLandscape() ) || ( ! printStyle.getPortrait() && ! printStyle.getLandscape() ) ) {
								addProjectDataErrorToList(ProjectDataError.createRequiredProjectDataError(
										getProjectElementId(), firstValueObject));
							}
 							break;
 							
 						//"Adjust To"
 						case 5:
 							printStyle.setAdjustTo(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"% Normal Size"
 						case 6:
 							printStyle.setPercentNormalSize(PafExcelUtil.getInteger(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"FitTo"
 						case 7:
 							printStyle.setFitTo(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
							if( ( printStyle.getAdjustTo() && printStyle.getFitTo() ) || ( ! printStyle.getAdjustTo() && ! printStyle.getFitTo() ) ) {
								addProjectDataErrorToList(ProjectDataError.createRequiredProjectDataError(
										getProjectElementId(), firstValueObject));
							}
 							break;
 							
 						//"Page(s) Wide"
 						case 8:
 							printStyle.setPageWide(PafExcelUtil.getInteger(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Page(s) Tall"
 						case 9:
 							printStyle.setPageTall(PafExcelUtil.getInteger(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Paper Size"
 						case 10:
 							printStyle.setPaperSize(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 
 						//"First Page Number",
 						case 11:
 							printStyle.setFirstPageNumber(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Header"
 						case 12:
 							printStyle.setHeader(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Top"
 						case 13:
 							printStyle.setTop(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Left"
 						case 14:
 							printStyle.setLeft(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Right"
 						case 15:
 							printStyle.setRight(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Bottom"
 						case 16:
 							printStyle.setBottom(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Footer"
 						case 17:
 							printStyle.setFooter(new Float(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true)));
 							break;
 							
 						//"Center Horizontally"
 						case 18:
 							printStyle.setCenterHorizontally(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Center Vertically"
 						case 19:
 							printStyle.setCenterVertically(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Header Text"
 						case 20:
 							printStyle.setHeaderText(PafExcelUtil.getString(getProjectElementId(), firstValueObject));
 							break;
 							
 						//"Footer Text"
 						case 21:
 							printStyle.setFooterText(PafExcelUtil.getString(getProjectElementId(), firstValueObject));
 							break;
 						
 						//"Different odd and even pages"
 						case 22:
 							printStyle.setDiffOddAndEvenPages(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Different first page"
 						case 23:
 							printStyle.setDiffFirstPage(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Scale with document"
 						case 24:
 							printStyle.setScaleWithDocument(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Align with page margins"
 						case 25:
 							printStyle.setAlignWithPageMargin(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Entire Sheet":
 						case 26:
 							printStyle.setEntireView(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"User Selection?":
 						case 27:
 							printStyle.setUserSelection(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
							if( ( printStyle.getEntireView() && printStyle.getUserSelection() ) || ( ! printStyle.getEntireView() && ! printStyle.getUserSelection()) ) {
								addProjectDataErrorToList(ProjectDataError.createRequiredProjectDataError(
										getProjectElementId(), firstValueObject));
							}
 							break;
 							
 						//"User Selected Print area":
 						case 28:
 							printStyle.setUserSelectionText(PafExcelUtil.getString(getProjectElementId(), firstValueObject));
 							break;
 							
 						//"Rows to repeat at top"	
 						case 29:
 							printStyle.setRowsToRepeatAtTop(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//"Columns to repeat at left"
 						case 30:
 							printStyle.setColsToRepeatAtLeft(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//"Gridlines"
 						case 31:
 							printStyle.setGridlines(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//"Black and white"
 						case 32:
 							printStyle.setBlackAndWhite(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Draft quality"
 						case 33:
 							printStyle.setDraftQuality(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Row and column headings"
 						case 34:
 							printStyle.setRowAndColHeadings(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Comments"
 						case 35:
 							printStyle.setComment(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Cell errors as",
 						case 36:
 							printStyle.setCellErrorsAs(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 							
 						//"Down, then Over"
 						case 37:
 							printStyle.setDownThenOver(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
 							break;
 			
 						//"Over, then Down"
 						case 38:
 							printStyle.setOverThenDown(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject, true));
							if( ( printStyle.getDownThenOver() && printStyle.getOverThenDown()) || ( ! printStyle.getDownThenOver() && ! printStyle.getOverThenDown() ) ) {
								addProjectDataErrorToList(ProjectDataError.createRequiredProjectDataError(
										getProjectElementId(), firstValueObject));
							}
 							break;
 							
 						default:
 							break;
 					}
 				} catch (ExcelProjectDataErrorException epdee) {
 					addProjectDataErrorToList(epdee.getProjectDataError());
 				}
 			}
			if( printStyle.getName() != null ) {
 				printStylesMap.put(GUIDUtil.getGUID(), printStyle);
			}
 		}
 		return (T) printStylesMap;
 	}
 
 	@Override
 	protected void writeExcelSheet(T t) throws PaceProjectWriteException,
 			PafException {
 		// TODO Auto-generated method stub
 		PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), getSheetName(), getHeaderListMap().get(getSheetName()).size())
 		.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 		.build();
 	
 	
 		Map<String, PrintStyle> printStylesMap = t;
 		
 		List<PafExcelRow> excelRowList = new ArrayList<PafExcelRow>();
 	
 		//create and add header to list
 		excelRowList.add(PafExcelUtil.createHeaderRow(getHeaderListMap().get(getSheetName())));
 		
 		if ( printStylesMap != null ) {
 			
 			for ( String key : printStylesMap.keySet() ) {
 				
 				PrintStyle printStyle = printStylesMap.get(key);
 				
 				PafExcelRow excelRow = new PafExcelRow();
 				
 				//GUID
 				excelRow.addRowItem(0, PafExcelValueObject.createFromString(printStyle.getGUID()));
 	
 				//name
 				excelRow.addRowItem(1, PafExcelValueObject.createFromString(printStyle.getName()));
 				
 				//Default Print Style?
 				excelRow.addRowItem(2, PafExcelValueObject.createFromBoolean(printStyle.getDefaultStyle()));
 				
 				//Portrait?
 				excelRow.addRowItem(3, PafExcelValueObject.createFromBoolean(printStyle.getPortrait()));
 								
 				//Landscape?
 				excelRow.addRowItem(4, PafExcelValueObject.createFromBoolean(printStyle.getLandscape()));
 				
 				//Adjust To
 				excelRow.addRowItem(5, PafExcelValueObject.createFromBoolean(printStyle.getAdjustTo()));
 				
 				//"% Normal Size"
 				excelRow.addRowItem(6, PafExcelValueObject.createFromInteger(printStyle.getPercentNormalSize()));
 								
 				//Fit To
 				excelRow.addRowItem(7, PafExcelValueObject.createFromBoolean(printStyle.getFitTo()));
 				
 				//"page Wide"
 				excelRow.addRowItem(8, PafExcelValueObject.createFromInteger(printStyle.getPageWide()));
 	
 				//"Page Tall"
 				excelRow.addRowItem(9, PafExcelValueObject.createFromInteger(printStyle.getPageTall()));
 				
 				//"Paper Size"
 				excelRow.addRowItem(10, PafExcelValueObject.createFromString(printStyle.getPaperSize()));
 			
 				//"First Page Number",
 				excelRow.addRowItem(11, PafExcelValueObject.createFromString(printStyle.getFirstPageNumber()));
 			
 				//"Header"
 				excelRow.addRowItem(12, PafExcelValueObject.createFromString(printStyle.getHeader().toString()));
 			
 				//"Top"
 				excelRow.addRowItem(13, PafExcelValueObject.createFromString(printStyle.getTop().toString()));
 			
 				//"Left"
 				excelRow.addRowItem(14, PafExcelValueObject.createFromString(printStyle.getLeft().toString()));
 			
 				//"Right"
 				excelRow.addRowItem(15, PafExcelValueObject.createFromString(printStyle.getRight().toString()));
 			
 				//"Bottom"
 				excelRow.addRowItem(16, PafExcelValueObject.createFromString(printStyle.getBottom().toString()));
 			
 				//"Footer"
 				excelRow.addRowItem(17, PafExcelValueObject.createFromString(printStyle.getFooter().toString()));
 			
 				//"Center Horizontally"
 				excelRow.addRowItem(18, PafExcelValueObject.createFromBoolean(printStyle.getCenterHorizontally()));
 			
 				//"Center Vertically"
 				excelRow.addRowItem(19, PafExcelValueObject.createFromBoolean(printStyle.getCenterVertically()));
 			
 				//"Header Text"
 				excelRow.addRowItem(20, PafExcelValueObject.createFromString(printStyle.getHeaderText()));
 			
 				//"Footer Text"
 				excelRow.addRowItem(21, PafExcelValueObject.createFromString(printStyle.getFooterText()));
 			
 				//"Different odd and even pages"
 				excelRow.addRowItem(22, PafExcelValueObject.createFromBoolean(printStyle.getDiffOddAndEvenPages()));
 			
 				//"Different first page"
 				excelRow.addRowItem(23, PafExcelValueObject.createFromBoolean(printStyle.getDiffFirstPage()));
 			
 				//"Scale with document"
 				excelRow.addRowItem(24, PafExcelValueObject.createFromBoolean(printStyle.getScaleWithDocument()));
 			
 				//"Align with page margins"
 				excelRow.addRowItem(25, PafExcelValueObject.createFromBoolean(printStyle.getAlignWithPageMargin()));
 			
 				//"Entire Sheet"
 				excelRow.addRowItem(26, PafExcelValueObject.createFromBoolean(printStyle.getEntireView()));
 			
 				//"User Selection?"
 				excelRow.addRowItem(27, PafExcelValueObject.createFromBoolean(printStyle.getUserSelection()));
 			
 				//"User Selected Print area"
 				excelRow.addRowItem(28, PafExcelValueObject.createFromString(printStyle.getUserSelectionText()));
 			
 				//"Rows to repeat at top"
 				excelRow.addRowItem(29, PafExcelValueObject.createFromString(printStyle.getRowsToRepeatAtTop()));
 			
 				//"Columns to repeat at left"
 				excelRow.addRowItem(30, PafExcelValueObject.createFromString(printStyle.getColsToRepeatAtLeft()));
 			
 				//"Gridlines"
 				excelRow.addRowItem(31, PafExcelValueObject.createFromBoolean(printStyle.getGridlines()));
 			
 				//"Black and white"
 				excelRow.addRowItem(32, PafExcelValueObject.createFromBoolean(printStyle.getBlackAndWhite()));
 			
 				//"Draft quality"
 				excelRow.addRowItem(33, PafExcelValueObject.createFromBoolean(printStyle.getDraftQuality()));
 			
 				//"Row and column headings"
 				excelRow.addRowItem(34, PafExcelValueObject.createFromBoolean(printStyle.getRowAndColHeadings()));
 			
 				//"Comments"
 				excelRow.addRowItem(35, PafExcelValueObject.createFromString(printStyle.getComment()));
 			
 				//"Cell errors as"
 				excelRow.addRowItem(36, PafExcelValueObject.createFromString(printStyle.getCellErrorsAs()));
 			
 				//"Down, then Over"
 				excelRow.addRowItem(37, PafExcelValueObject.createFromBoolean(printStyle.getDownThenOver()));
 			
 				//"Over, then Down"
 				excelRow.addRowItem(38, PafExcelValueObject.createFromBoolean(printStyle.getOverThenDown()));
 			
 				excelRowList.add(excelRow);			
 			}
 		}
 					
 		PafExcelUtil.writeExcelSheet(input, excelRowList);
 	}
 
 	@Override
 	public ProjectElementId getProjectElementId() {
 		// TODO Auto-generated method stub
 		return ProjectElementId.PrintStyles;
 	}
 
 }
