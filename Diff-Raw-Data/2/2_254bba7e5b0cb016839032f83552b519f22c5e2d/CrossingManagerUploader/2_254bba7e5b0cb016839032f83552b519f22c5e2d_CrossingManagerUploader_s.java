 package org.generationcp.breeding.manager.crossingmanager.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.generationcp.breeding.manager.crossingmanager.CrossingManagerImportFileComponent;
 import org.generationcp.breeding.manager.pojos.ImportedCondition;
 import org.generationcp.breeding.manager.pojos.ImportedConstant;
 import org.generationcp.breeding.manager.pojos.ImportedFactor;
 import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
 import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
 import org.generationcp.breeding.manager.pojos.ImportedVariate;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.GermplasmListManager;
 import org.generationcp.middleware.pojos.GermplasmList;
 import org.generationcp.middleware.pojos.GermplasmListData;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.vaadin.data.Property.ConversionException;
 import com.vaadin.data.Property.ReadOnlyException;
 import com.vaadin.ui.Upload.Receiver;
 import com.vaadin.ui.Upload.SucceededEvent;
 import com.vaadin.ui.Upload.SucceededListener;
 import com.vaadin.ui.Window.Notification;
 
 public class CrossingManagerUploader implements Receiver, SucceededListener {
 	
 	private static final long serialVersionUID = 1L;
 	private CrossingManagerImportFileComponent source;
 	
 	public File file;
 
 	private String tempFileName;
 	
 	private Integer currentSheet;
 	private Integer currentRow;
 	private Integer currentColumn;
 	
 	private String originalFilename;
 	private String study;
 	private String title;
 	private String pmKey;
 	private String objective;
 	private Date startDate;
 	private Date endDate;
 	private String studyType;
 	
 	private InputStream inp;
 	private Workbook wb;
 	
 	private ImportedGermplasmCrosses importedGermplasmCrosses;
 	
 	private Boolean fileIsValid;
 
 	private GermplasmListManager germplasmListManager;
 	
 	private GermplasmList maleGermplasmList;
 	private GermplasmList femaleGermplasmList;	
 	
 	public CrossingManagerUploader(CrossingManagerImportFileComponent crossingManagerImportFileComponent, GermplasmListManager germplasmListManager) {
 		this.source = crossingManagerImportFileComponent;
 		this.germplasmListManager = germplasmListManager;
 	}
 	
 	public OutputStream receiveUpload(String filename, String mimeType) { 
 		tempFileName = source.getAccordion().getApplication().getContext().getBaseDirectory().getAbsolutePath()+"/WEB-INF/uploads/imported_nurserytemplate.xls";
 		FileOutputStream fos = null;
         try {
         	file = new File(tempFileName);
             fos = new FileOutputStream(file);
             
             originalFilename = filename;            
         } catch (final java.io.FileNotFoundException e) {
             System.out.println("FileNotFoundException on receiveUpload(): "+e.getMessage());
             return null;
         }
         return fos; // Return the output stream to write to
     }
 
     public void uploadSucceeded(SucceededEvent event) {
     	System.out.println("DEBUG | "+tempFileName);
     	System.out.println("DEBUG | Upload succeeded!");
     	
     	currentSheet = 0;
     	currentRow = 0;
     	currentColumn = 0;
         
     	fileIsValid = true;
     	
 		try {
 			inp = new FileInputStream(tempFileName);
 			wb = new HSSFWorkbook(inp);
 			
         	readSheet1();
         	readSheet2();
 
         	if(fileIsValid==false){
         		importedGermplasmCrosses = null;
         	}
         	
         	if(importedGermplasmCrosses==null || importedGermplasmCrosses.getImportedGermplasmCrosses().size()==0){
         		source.selectManuallyMakeCrosses();
         	} else {
         		source.selectAlreadyDefinedCrossesInNurseryTemplateFile();
         	}
         	
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found");
 		} catch (IOException e) {
 			showInvalidFileTypeError();
 		} catch (ReadOnlyException e) {
 			showInvalidFileTypeError();
 		} catch (ConversionException e) {
 			showInvalidFileTypeError();
 		} catch (OfficeXmlFileException e){
 			showInvalidFileTypeError();
 		}
     }
 
 
     private void readSheet1(){
     	readNurseryTemplateFileInfo();
     	readConditions();
     	readFactors();
     	readConstants();
     	readVariates();
     }
     
     private void readSheet2(){
     	currentSheet = 1;
     	currentRow = 0;
     	currentColumn = 0;
     	    	
     	ImportedGermplasmCross importedGermplasmCross;
     	Boolean germplasmListDataAreValid = true;
     
 		if(fileIsValid){
 			currentRow++;
     	
 			while(!rowIsEmpty()){
 				System.out.println("");
 				importedGermplasmCross = new ImportedGermplasmCross();
 				for(int col=0;col<importedGermplasmCrosses.getImportedFactors().size();col++){
 					if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("CROSS")){
 						importedGermplasmCross.setCross(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
 						System.out.println("DEBUG | CROSS:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("FEMALE ENTRY ID")){
 						importedGermplasmCross.setFemaleEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
 						System.out.println("DEBUG | FEMALE ENTRY ID:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("MALE ENTRY ID")){
 						importedGermplasmCross.setMaleEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
 						System.out.println("DEBUG | MALE ENTRY ID:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("FGID")){
 						importedGermplasmCross.setFemaleGId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
 						System.out.println("DEBUG | FEMALE GID:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("MGID")){
 						importedGermplasmCross.setMaleGId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
 						System.out.println("DEBUG | MALE GID:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("CROSSING DATE")){
 						try {
 							importedGermplasmCross.setCrossingDate(new SimpleDateFormat("yyyymmdd").parse(getCellStringValue(currentSheet, currentRow, col, true)));
 						} catch (ParseException e) {
 							System.out.println("ERROR | Unable to parse date - " + getCellStringValue(currentSheet, currentRow, col));
 						}
 						System.out.println("DEBUG | CROSSING DATE:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("SEEDS HARVESTED")){
 						importedGermplasmCross.setSeedsHarvested(getCellStringValue(currentSheet, currentRow, col, true));
 						System.out.println("DEBUG | SEEDS HARVESTED:"+getCellStringValue(currentSheet, currentRow, col));
 					} else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("NOTES")){
 						importedGermplasmCross.setNotes(getCellStringValue(currentSheet, currentRow, col, true));
 						System.out.println("DEBUG | NOTES:"+getCellStringValue(currentSheet, currentRow, col));
 					} else {
 						System.out.println("DEBUG | Unhandled Column - "+importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase()+":"+getCellStringValue(currentSheet, currentRow, col));
 					}
 				}
 				importedGermplasmCrosses.addImportedGermplasmCross(importedGermplasmCross);
 				
 				//Check if male entryID and GID are valid
 				Boolean maleFound = true; //set true for cases wherein there is no list id
 				if(maleGermplasmList!=null){
 					maleFound = false;
 					for(int gd=0;gd<maleGermplasmList.getListData().size();gd++){
 						GermplasmListData germplasmListData = maleGermplasmList.getListData().get(gd);
 					    if(germplasmListData.getEntryId().equals(importedGermplasmCross.getMaleEntryId())
 					    		&& germplasmListData.getGid().equals(importedGermplasmCross.getMaleGId())){ 
 					    	maleFound = true;
 						}
 					}
 				}
 				
 				//Check if female entryID and GID are valid
 				Boolean femaleFound = true; //set true for cases wherein there is no list id
				if(maleGermplasmList!=null){
 					femaleFound = false;				
 					for(int gd=0;gd<maleGermplasmList.getListData().size();gd++){
 						GermplasmListData germplasmListData = femaleGermplasmList.getListData().get(gd);
 					    if(germplasmListData.getEntryId().equals(importedGermplasmCross.getFemaleEntryId())
 					    		&& germplasmListData.getGid().equals(importedGermplasmCross.getFemaleGId())){
 					    	femaleFound = true;
 					    }
 					}
 				}
 				
 				if(maleFound==false || femaleFound==false)
 					germplasmListDataAreValid = false;				
 				
 				currentRow++;
 			}
     	}
 		if(germplasmListDataAreValid==false){
 			showInvalidFileError("Invalid germplasm list data on sheet 2.");
 		}
     }
 
     private void readNurseryTemplateFileInfo(){
     	try {
     		study = getCellStringValue(0,0,1,true);
         	title = getCellStringValue(0,1,1,true);
         	pmKey = getCellStringValue(0,2,1,true);
         	objective = getCellStringValue(0,3,1,true);
 			startDate = new SimpleDateFormat("yyyymmdd").parse(getCellStringValue(0,4,1,true));
 			endDate = new SimpleDateFormat("yyyymmdd").parse(getCellStringValue(0,5,1,true));
         	studyType = getCellStringValue(0,6,1,true);
 			
 			importedGermplasmCrosses = new ImportedGermplasmCrosses(originalFilename, study, title, pmKey, objective, startDate, endDate, studyType); 
 			
 			source.updateFilenameLabelValue(originalFilename);
 			
 	    	System.out.println("DEBUG | Original Filename:" + originalFilename);
 	    	System.out.println("DEBUG | Study:" + study);
 	    	System.out.println("DEBUG | Title:" + title);
 	    	System.out.println("DEBUG | PMKey:" + pmKey);
 	    	System.out.println("DEBUG | Objective:" + objective);
 	    	System.out.println("DEBUG | Start Date:" + startDate.toString());
 	    	System.out.println("DEBUG | End Date:" + endDate.toString());
 	    	System.out.println("DEBUG | Study Type:" + studyType);
 	    	
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
     	
     	//Prepare for next set of data
     	while(!rowIsEmpty()){
     		currentRow++;
     	}
     	
     }
     
     private void readConditions(){
     
     	Boolean femaleListIDPresent = false;
     	Boolean maleListIDPresent = false;
     	
     	currentRow++; //Skip row from file info
     	
     	//Check if headers are correct
     	if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONDITION") 
     		|| !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
     		|| !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
     		|| !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
     		|| !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
     		|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
     		|| !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
     		|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")){
     		showInvalidFileError("Incorrect headers for conditions.");
     		System.out.println("DEBUG | Invalid file on readConditions header");
     		System.out.println(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase());
     	}
     	//If file is still valid (after checking headers), proceed
     	if(fileIsValid){
     		ImportedCondition importedCondition;
     		currentRow++; 
     		while(!rowIsEmpty()){
     			importedCondition = new ImportedCondition(getCellStringValue(currentSheet,currentRow,0,true)
     				,getCellStringValue(currentSheet,currentRow,1,true)
     				,getCellStringValue(currentSheet,currentRow,2,true)
     				,getCellStringValue(currentSheet,currentRow,3,true)
     				,getCellStringValue(currentSheet,currentRow,4,true)
     				,getCellStringValue(currentSheet,currentRow,5,true)
     				,getCellStringValue(currentSheet,currentRow,6,true)
     				,getCellStringValue(currentSheet,currentRow,7,true));
     			importedGermplasmCrosses.addImportedCondition(importedCondition);
     	
     	    	//If male list ID is preset, set flag
     			if(importedCondition.getCondition().toUpperCase().equals("MALE LIST ID")){
     				maleListIDPresent = true;
     				
     				if(importedCondition.getValue()!=null && importedCondition.getValue()!=""){
 	    				try {
 	   						maleGermplasmList = germplasmListManager.getGermplasmListById(Integer.valueOf(importedCondition.getValue()));
 						} catch (NumberFormatException e) {
 							e.printStackTrace();
 						} catch (MiddlewareQueryException e) {
 							e.printStackTrace();
 						}
     				}
     			}
     			//If female list ID is preset, set flag
     			else if(importedCondition.getCondition().toUpperCase().equals("FEMALE LIST ID")){
     				femaleListIDPresent = true;
     				if(importedCondition.getValue()!=null && importedCondition.getValue()!=""){
 	    				try {
 							femaleGermplasmList = germplasmListManager.getGermplasmListById(Integer.valueOf(importedCondition.getValue()));
 						} catch (NumberFormatException e) {
 							showInvalidFileError("System Error");
 							e.printStackTrace();
 						} catch (MiddlewareQueryException e) {
 							showInvalidFileError("System Error");
 							e.printStackTrace();
 						}
     				}
     			}    			
     			
     			System.out.println("");
     			System.out.println("DEBUG | Condition:"+getCellStringValue(currentSheet,currentRow,0));
     			System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
     			System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
     			System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
     			System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
     			System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
     			System.out.println("DEBUG | Value:"+getCellStringValue(currentSheet,currentRow,6));
     			System.out.println("DEBUG | Label:"+getCellStringValue(currentSheet,currentRow,7));
     			
     			currentRow++;
     		}
     	}
     	
     	//Check if male & female list ID's are present
     	if(femaleListIDPresent==false || maleListIDPresent==false){
     		showInvalidFileError("Male and Female List ID's should be present under the conditions.");
     	}
     	
     	currentRow++;
     }
 
     private void readFactors(){
 
     	if(rowIsEmpty())
     		currentRow++;
     	
     	//Check if headers are correct
     	if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
         	|| !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
         	|| !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
         	|| !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
         	|| !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
         	|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
         	|| !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("NESTED IN")
         	|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
         	showInvalidFileError("Incorrect headers for factors.");
         	System.out.println("DEBUG | Invalid file on readFactors header");
         }
     	//If file is still valid (after checking headers), proceed
     	if(fileIsValid){
     		ImportedFactor importedFactor;    		
     		currentRow++; //skip header
     		while(!rowIsEmpty()){
     			importedFactor = new ImportedFactor(getCellStringValue(currentSheet,currentRow,0,true)
     				,getCellStringValue(currentSheet,currentRow,1,true)
     				,getCellStringValue(currentSheet,currentRow,2,true)
     				,getCellStringValue(currentSheet,currentRow,3,true)
     				,getCellStringValue(currentSheet,currentRow,4,true)
     				,getCellStringValue(currentSheet,currentRow,5,true)
     				,getCellStringValue(currentSheet,currentRow,6,true)
     				,getCellStringValue(currentSheet,currentRow,7,true));
     			
    				importedGermplasmCrosses.addImportedFactor(importedFactor);
     			
     			System.out.println("");
     			System.out.println("DEBUG | Factor:"+getCellStringValue(currentSheet,currentRow,0));
     			System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
     			System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
     			System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
     			System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
     			System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
     			System.out.println("DEBUG | Nested In:"+getCellStringValue(currentSheet,currentRow,6));
     			System.out.println("DEBUG | Label:"+getCellStringValue(currentSheet,currentRow,7));
     			
     			currentRow++;
     		}
     	}
     	currentRow++;
     }
     
     private void readConstants(){
     	//Check if headers are correct
     	if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONSTANT") 
         	|| !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
         	|| !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
         	|| !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
         	|| !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
         	|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
         	|| !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
         	|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("SAMPLE LEVEL")) {
         	showInvalidFileError("Incorrect headers for constants.");
         	System.out.println("DEBUG | Invalid file on readConstants header");
         }
     	//If file is still valid (after checking headers), proceed
     	if(fileIsValid){
     		ImportedConstant importedConstant;
     		currentRow++; //skip header
     		while(!rowIsEmpty()){
     			importedConstant = new ImportedConstant(getCellStringValue(currentSheet,currentRow,0,true)
     				,getCellStringValue(currentSheet,currentRow,1,true)
     				,getCellStringValue(currentSheet,currentRow,2,true)
     				,getCellStringValue(currentSheet,currentRow,3,true)
     				,getCellStringValue(currentSheet,currentRow,4,true)
     				,getCellStringValue(currentSheet,currentRow,5,true)
     				,getCellStringValue(currentSheet,currentRow,6,true)
     				,getCellStringValue(currentSheet,currentRow,7,true));
     			importedGermplasmCrosses.addImportedConstant(importedConstant);
     			
     			System.out.println("");
     			System.out.println("DEBUG | Constant:"+getCellStringValue(currentSheet,currentRow,0));
     			System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
     			System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
     			System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
     			System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
     			System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
     			System.out.println("DEBUG | Value:"+getCellStringValue(currentSheet,currentRow,6));
     			System.out.println("DEBUG | Sample Level:"+getCellStringValue(currentSheet,currentRow,7));
 
     			
     			currentRow++;
     		}
     	}
     	currentRow++;
     }
     
     private void readVariates(){
     	//Check if headers are correct
     	if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
         	|| !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
         	|| !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
         	|| !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
         	|| !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
         	|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
     		|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("SAMPLE LEVEL")) {
         	showInvalidFileError("Incorrect headers for variates.");
         	System.out.println("DEBUG | Invalid file on readVariates header");
         }
     	//If file is still valid (after checking headers), proceed
     	if(fileIsValid){
     		ImportedVariate importedVariate;
     		currentRow++; //skip header
     		while(!rowIsEmpty()){
     			importedVariate = new ImportedVariate(getCellStringValue(currentSheet,currentRow,0,true)
     				,getCellStringValue(currentSheet,currentRow,1,true)
     				,getCellStringValue(currentSheet,currentRow,2,true)
     				,getCellStringValue(currentSheet,currentRow,3,true)
     				,getCellStringValue(currentSheet,currentRow,4,true)
     				,getCellStringValue(currentSheet,currentRow,5,true)
     				,getCellStringValue(currentSheet,currentRow,7,true));
     			importedGermplasmCrosses.addImportedVariate(importedVariate);
 
     			System.out.println("");
     			System.out.println("DEBUG | Variate:"+getCellStringValue(currentSheet,currentRow,0));
     			System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
     			System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
     			System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
     			System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
     			System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
     			System.out.println("DEBUG | Sample Level:"+getCellStringValue(currentSheet,currentRow,7));
     			
     			currentRow++;
     		}
     	}
     	currentRow++;
     }
 
     
     
     private Boolean rowIsEmpty(){
     	return rowIsEmpty(currentRow);
     }
     
     private Boolean rowIsEmpty(Integer row){
     	return rowIsEmpty(currentSheet, row);
     }
 
     private Boolean rowIsEmpty(Integer sheet, Integer row){
         for(int col=0;col<8;col++){
         	if(getCellStringValue(sheet, row, col)!="" && getCellStringValue(sheet, row, col)!=null)
         		return false;
         }
         return true;    	
     }    
     
     private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber){
     	return getCellStringValue(sheetNumber, rowNumber, columnNumber, false);
     }
     	
     private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber, Boolean followThisPosition){
     	if(followThisPosition){
     		currentSheet = sheetNumber;
     		currentRow = rowNumber;
     		currentColumn = columnNumber;
     	}
     
     	try {
     		Sheet sheet = wb.getSheetAt(sheetNumber);
     		Row row = sheet.getRow(rowNumber);
     		Cell cell = row.getCell(columnNumber);
     		return cell.getStringCellValue();
     	} catch(IllegalStateException e) {
     		Sheet sheet = wb.getSheetAt(sheetNumber);
     		Row row = sheet.getRow(rowNumber);
     		Cell cell = row.getCell(columnNumber);
     		return String.valueOf(Integer.valueOf((int) cell.getNumericCellValue()));
     	} catch(NullPointerException e) {
     		return "";
     	}
     }
     
     private void showInvalidFileError(String message){
     	if(fileIsValid){
     	    MessageNotifier.showError(source.getWindow(), "", message);
     		//source.getAccordion().getApplication().getMainWindow().showNotification(message, Notification.TYPE_ERROR_MESSAGE);
     		fileIsValid = false;
     	}
     }
     
     private void showInvalidFileTypeError(){
     	if(fileIsValid){
     	    MessageNotifier.showError(source.getWindow(), "", "Invalid Import File Type, you need to upload an XLS file");
     		//source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File Type, you need to upload an XLS file", Notification.TYPE_ERROR_MESSAGE);
     		fileIsValid = false;
     	}
     }    
     
     public ImportedGermplasmCrosses getImportedGermplasmCrosses(){
     	return importedGermplasmCrosses;
     }
     
 };
