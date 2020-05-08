 package org.generationcp.ibpworkbench.util;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.generationcp.commons.util.PoiUtil;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.StudyDataManager;
 import org.generationcp.middleware.manager.api.TraitDataManager;
 import org.generationcp.middleware.pojos.CharacterDataElement;
 import org.generationcp.middleware.pojos.CharacterLevelElement;
 import org.generationcp.middleware.pojos.DatasetCondition;
 import org.generationcp.middleware.pojos.Factor;
 import org.generationcp.middleware.pojos.NumericDataElement;
 import org.generationcp.middleware.pojos.NumericLevelElement;
 import org.generationcp.middleware.pojos.Scale;
 import org.generationcp.middleware.pojos.Study;
 import org.generationcp.middleware.pojos.Trait;
 import org.generationcp.middleware.pojos.TraitMethod;
 import org.generationcp.middleware.pojos.Variate;
 
 
 public class DatasetExporter {
 
     private static final int conditionListHeaderRowIndex = 8;
     
     private StudyDataManager studyDataManager;
     private TraitDataManager traitDataManager;
     private Integer studyId;
     private Integer representationId;
     
     public DatasetExporter(StudyDataManager studyDataManager, TraitDataManager traitDataManager, Integer studyId, Integer representationId) {
         this.studyDataManager = studyDataManager;
         this.traitDataManager = traitDataManager;
         this.studyId = studyId;
         this.representationId = representationId;
     }
     
     public FileOutputStream exportToFieldBookExcel(String filename) throws DatasetExporterException {
         //create workbook
         Workbook workbook = new HSSFWorkbook();
         CellStyle cellStyle = workbook.createCellStyle();
         CellStyle cellStyleForObservationSheet = workbook.createCellStyle();
         
         //create two sheets, one for description and nother for measurements
         Sheet descriptionSheet = workbook.createSheet("Description");
         Sheet observationSheet = workbook.createSheet("Observation");
         
         //this map is for mapping the columns names of the dataset to their column index in the excel sheet
         Map<String, Integer> columnsMap = new HashMap<String, Integer>(); 
         int observationSheetColumnIndex = 0;
         
         //write the details on the first sheet - description
         //get the study first
         Study study = null;
         
         try {
             study = this.studyDataManager.getStudyByID(this.studyId);
         } catch (MiddlewareQueryException ex) {
             throw new DatasetExporterException("Error with getting Study with id: " + this.studyId, ex);
         }
         
         if(study != null) {
             //get the needed study details
             String name = study.getName();
             String title = study.getTitle();
             Integer pmkey = study.getProjectKey();
             String objective = study.getObjective();
             Integer startDate = study.getStartDate();
             Integer endDate = study.getEndDate();
             String type = study.getType();
             
             //add to the sheet
             Row row0 = descriptionSheet.createRow(0);
             row0.createCell(0).setCellValue("STUDY");
             row0.createCell(1).setCellValue(name);
             
             Row row1 = descriptionSheet.createRow(1);
             row1.createCell(0).setCellValue("TITLE");
             row1.createCell(1).setCellValue(title);
             
             Row row2 = descriptionSheet.createRow(2);
             row2.createCell(0).setCellValue("PMKEY");
             Cell pmKeyCell = PoiUtil.createCell(cellStyle, row2, (short) 1, CellStyle.ALIGN_LEFT, CellStyle.ALIGN_JUSTIFY);
             pmKeyCell.setCellValue(pmkey);
             
             Row row3 = descriptionSheet.createRow(3);
             row3.createCell(0).setCellValue("OBJECTIVE");
             row3.createCell(1).setCellValue(objective);
             
             Row row4 = descriptionSheet.createRow(4);
             row4.createCell(0).setCellValue("START DATE");
             Cell startDateCell = PoiUtil.createCell(cellStyle, row4, (short) 1, CellStyle.ALIGN_LEFT, CellStyle.ALIGN_JUSTIFY);
             startDateCell.setCellValue(startDate);
             
             Row row5 = descriptionSheet.createRow(5);
             row5.createCell(0).setCellValue("END DATE");
             Cell endDateCell = PoiUtil.createCell(cellStyle, row5, (short) 1, CellStyle.ALIGN_LEFT, CellStyle.ALIGN_JUSTIFY);
             endDateCell.setCellValue(endDate);
             
             Row row6 = descriptionSheet.createRow(6);
             row6.createCell(0).setCellValue("STUDY TYPE");
             row6.createCell(1).setCellValue(type);
             
             //merge cells for the study details
             for(int ctr = 0; ctr < 7; ctr++) {
                 descriptionSheet.addMergedRegion(new CellRangeAddress(ctr, ctr, 1, 7));
             }
             
             //empty row
             Row row7 = descriptionSheet.createRow(7);
             
             //row with headings for condition list
             Row conditionHeaderRow = descriptionSheet.createRow(this.conditionListHeaderRowIndex);
             conditionHeaderRow.createCell(0).setCellValue("CONDITION");
             conditionHeaderRow.createCell(1).setCellValue("DESCRIPTION");
             conditionHeaderRow.createCell(2).setCellValue("PROPERTY");
             conditionHeaderRow.createCell(3).setCellValue("SCALE");
             conditionHeaderRow.createCell(4).setCellValue("METHOD");
             conditionHeaderRow.createCell(5).setCellValue("DATA TYPE");
             conditionHeaderRow.createCell(6).setCellValue("VALUE");
             conditionHeaderRow.createCell(7).setCellValue("LABEL");
             
             //get the conditions and their details
             List<DatasetCondition> conditions = new ArrayList<DatasetCondition>();
             try {
                 conditions.addAll(this.studyDataManager.getConditionsByRepresentationId(this.representationId));
             } catch(Exception ex) {
                 throw new DatasetExporterException("Error with getting conditions of study - " + name 
                         + ", representation - " + this.representationId, ex);
             }
             
             int conditionRowIndex = this.conditionListHeaderRowIndex + 1;
             for(DatasetCondition condition : conditions) {
                 String traitScaleMethodInfo[] = getTraitScaleMethodInfo(condition.getTraitId(), condition.getScaleId(), condition.getMethodId());
                 
                 String conditionName = condition.getName();
                 if(conditionName != null) {
                     conditionName = conditionName.trim();
                 }
                 String conditionType = condition.getType();
                 
                 String conditionLabel = "";
                 try {
                     conditionLabel = this.studyDataManager.getMainLabelOfFactorByFactorId(condition.getFactorId());
                 } catch (MiddlewareQueryException ex) {
                     conditionLabel = "";
                 }
                 
                 Row conditionRow = descriptionSheet.createRow(conditionRowIndex);
                 conditionRow.createCell(0).setCellValue(conditionName);
                 conditionRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                 conditionRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                 conditionRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                 conditionRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                 conditionRow.createCell(5).setCellValue(conditionType);
                 if(conditionType.equals("N")) {
                     Double thevalue = (Double) condition.getValue();
                     conditionRow.createCell(6).setCellValue(thevalue);
                 } else {
                     conditionRow.createCell(6).setCellValue(condition.getValue().toString());
                 }
                 conditionRow.createCell(7).setCellValue(conditionLabel);
                 
                 //add entry to columns mapping
                 //we set the value to -1 to signify that this should not be a column in the observation sheet
                 if(!conditionName.equals("STUDY")) {
                     columnsMap.put(conditionName, Integer.valueOf(-1));
                 }
                 
                 conditionRowIndex++;
             }
             
             //empty row
             Row emptyRowBeforeFactors = descriptionSheet.createRow(conditionRowIndex);
             
             //row with headings for factor list
             int factorRowHeaderIndex = conditionRowIndex + 1;
             Row factorHeaderRow = descriptionSheet.createRow(factorRowHeaderIndex);
             factorHeaderRow.createCell(0).setCellValue("FACTOR");
             factorHeaderRow.createCell(1).setCellValue("DESCRIPTION");
             factorHeaderRow.createCell(2).setCellValue("PROPERTY");
             factorHeaderRow.createCell(3).setCellValue("SCALE");
             factorHeaderRow.createCell(4).setCellValue("METHOD");
             factorHeaderRow.createCell(5).setCellValue("DATA TYPE");
             factorHeaderRow.createCell(6).setCellValue("");
             factorHeaderRow.createCell(7).setCellValue("LABEL");
             
             //get the factors and their details
             List<Factor> factors = new ArrayList<Factor>();
             try {
                 factors.addAll(this.studyDataManager.getFactorsByRepresentationId(this.representationId));
             } catch(Exception ex) {
                 throw new DatasetExporterException("Error with getting factors of study - " + name 
                         + ", representation - " + this.representationId, ex);
             }
             
             int factorRowIndex = factorRowHeaderIndex + 1;
             for(Factor factor : factors) {
                 String dataType = factor.getDataType();
                 String factorName = factor.getName();
                 if(factorName != null) {
                     factorName = factorName.trim();
                 }
                 
                 //check if factor is already written as a condition
                 Integer temp = columnsMap.get(factorName);
                 if(temp == null && !factorName.equals("STUDY")) {
                     String traitScaleMethodInfo[] = getTraitScaleMethodInfo(factor.getTraitId(), factor.getScaleId(), factor.getMethodId());
                     
                     String factorLabel = "";
                     try {
                         factorLabel = this.studyDataManager.getMainLabelOfFactorByFactorId(factor.getFactorId());
                     } catch (MiddlewareQueryException ex) {
                         factorLabel = "";
                     }
                     
                     Row factorRow = descriptionSheet.createRow(factorRowIndex);
                     factorRow.createCell(0).setCellValue(factorName);
                     factorRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                     factorRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                     factorRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                     factorRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                     factorRow.createCell(5).setCellValue(dataType);
                     factorRow.createCell(6).setCellValue("");
                     factorRow.createCell(7).setCellValue(factorLabel);
                     
                     //add entry to columns mapping
                     columnsMap.put(factorName, Integer.valueOf(observationSheetColumnIndex));
                     observationSheetColumnIndex++;
                         
                     factorRowIndex++;
                 }
             }
             
             //empty row
             Row emptyRowBeforeVariate = descriptionSheet.createRow(factorRowIndex);
             
             //row with headings for variate list
             int variateHeaderRowIndex = factorRowIndex + 1;
             Row variateHeaderRow = descriptionSheet.createRow(variateHeaderRowIndex);
             variateHeaderRow.createCell(0).setCellValue("VARIATE");
             variateHeaderRow.createCell(1).setCellValue("DESCRIPTION");
             variateHeaderRow.createCell(2).setCellValue("PROPERTY");
             variateHeaderRow.createCell(3).setCellValue("SCALE");
             variateHeaderRow.createCell(4).setCellValue("METHOD");
             variateHeaderRow.createCell(5).setCellValue("DATA TYPE");
             
             //get the variates and their details
             List<Variate> variates = new ArrayList<Variate>();
             try {
                 variates.addAll(this.studyDataManager.getVariatesByRepresentationId(this.representationId));
             }
             catch(Exception ex) {
                 throw new DatasetExporterException("Error with getting variates of study - " + name 
                         + ", representation - " + this.representationId, ex);
             }
             
             int variateRowIndex = variateHeaderRowIndex + 1;
             for(Variate variate : variates) {
                 String dataType = variate.getDataType();
                 String variateName = variate.getName();
                 if(variateName != null) {
                     variateName = variateName.trim();
                 }
                 
                 String traitScaleMethodInfo[] = getTraitScaleMethodInfo(variate.getTraitId(), variate.getScaleId(), variate.getMethodId());
                 
                 Row variateRow = descriptionSheet.createRow(variateRowIndex);
                 variateRow.createCell(0).setCellValue(variateName);
                 variateRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                 variateRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                 variateRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                 variateRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                 variateRow.createCell(5).setCellValue(dataType);
                 
                 //add entry to columns mapping
                 columnsMap.put(variateName, Integer.valueOf(observationSheetColumnIndex));
                 observationSheetColumnIndex++;
                 
                 variateRowIndex++;
             }
             
             //populate the measurements sheet
             //establish the columns of the dataset first
             Row datasetHeaderRow = observationSheet.createRow(0);
             for(String columnName : columnsMap.keySet()) {
                 short columnIndex = columnsMap.get(columnName).shortValue();
                 if(columnIndex >= 0) {
                     Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, datasetHeaderRow, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                     cell.setCellValue(columnName);
                 }
             }
             
             //then work with the data
             //do it by 50 rows at a time
             int pageSize = 50;
             long totalNumberOfRows = 0;
             int sheetRowIndex = 1;
             
             try {
                 totalNumberOfRows = this.studyDataManager.countOunitIDsByRepresentationId(this.representationId);
             } catch(Exception ex) {
                 throw new DatasetExporterException("Error with getting count of ounit ids for study - " + name 
                         + ", representation - " + this.representationId, ex); 
             }
             
             for(int start = 0; start < totalNumberOfRows; start = start + pageSize) {
                 List<Integer> ounitIds = new ArrayList<Integer>();
                 try {
                     //first get the ounit ids, these are the ids of the rows in the dataset
                     ounitIds.addAll(this.studyDataManager.getOunitIDsByRepresentationId(this.representationId, start, pageSize));
                 } catch(Exception ex) {
                     throw new DatasetExporterException("Error with getting ounit ids of study - " + name 
                             + ", representation - " + this.representationId, ex); 
                 }
                 
                 if(!ounitIds.isEmpty()) {
                     //map each ounit id into a row in the observation sheet
                     Map<Integer, Row> rowMap = new HashMap<Integer, Row>();
                     for(Integer ounitId : ounitIds) {
                         Row row = observationSheet.createRow(sheetRowIndex);
                         sheetRowIndex++;
                         rowMap.put(ounitId, row);
                     }
                     
                     //then get the data for each of the observation units (ounits)
                     List<CharacterLevelElement> charLevels = new ArrayList<CharacterLevelElement>();
                     try {
                         charLevels.addAll(this.studyDataManager.getCharacterLevelValuesByOunitIdList(ounitIds));
                     } catch(Exception ex) {
                         throw new DatasetExporterException("Error with getting character level values of study - " + name 
                                 + ", representation - " + this.representationId, ex);
                     }
                     
                     for(CharacterLevelElement elem : charLevels) {
                         String factorName = elem.getFactorName();
                         if(factorName != null) {
                             factorName = factorName.trim();
                         }
                         if(!factorName.equals("STUDY")) {
                             Row row = rowMap.get(elem.getOunitId());
                             if(row != null) {
                                 short columnIndex = columnsMap.get(factorName).shortValue();
                                 if(columnIndex >= 0) {
                                     Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                     String value = elem.getValue();
                                     if(value != null) {
                                         value = value.trim();
                                     }
                                     cell.setCellValue(value);
                                 }
                             }
                         }
                     }
                     
                     List<NumericLevelElement> numericLevels = new ArrayList<NumericLevelElement>();
                     try {
                         numericLevels.addAll(this.studyDataManager.getNumericLevelValuesByOunitIdList(ounitIds));
                     } catch(Exception ex) {
                         throw new DatasetExporterException("Error with getting numeric level values of study - " + name 
                                 + ", representation - " + this.representationId, ex);
                     }
                     
                     for(NumericLevelElement elem : numericLevels) {
                         String factorName = elem.getFactorName();
                         if(factorName != null) {
                             factorName = factorName.trim();
                         }
                         if(!factorName.equals("STUDY")) {
                             Row row = rowMap.get(elem.getOunitId());
                             if(row != null) {
                                 short columnIndex = columnsMap.get(factorName).shortValue();
                                 if(columnIndex >= 0) {
                                     Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                    cell.setCellValue(elem.getValue());
                                 }
                             }
                         }
                     }
                     
                     List<CharacterDataElement> charDatas = new ArrayList<CharacterDataElement>();
                     try {
                         charDatas.addAll(this.studyDataManager.getCharacterDataValuesByOunitIdList(ounitIds));
                     } catch(Exception ex) {
                         throw new DatasetExporterException("Error with getting character data values of study - " + name 
                                 + ", representation - " + this.representationId, ex);
                     }
                     
                     for(CharacterDataElement elem : charDatas) {
                         Row row = rowMap.get(elem.getOunitId());
                         if(row != null) {
                             String variateName = elem.getVariateName();
                             if(variateName != null) {
                                 variateName = variateName.trim();
                             }
                             short columnIndex = columnsMap.get(variateName).shortValue();
                             Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                             String value = elem.getValue();
                             if(value != null) {
                                 value = value.trim();
                             }
                             cell.setCellValue(value);
                         }
                     }
                     
                     List<NumericDataElement> numericDatas = new ArrayList<NumericDataElement>();
                     try {
                         numericDatas.addAll(this.studyDataManager.getNumericDataValuesByOunitIdList(ounitIds));
                     } catch(Exception ex) {
                         throw new DatasetExporterException("Error with getting numeric data values of study - " + name 
                                 + ", representation - " + this.representationId, ex);
                     }
                     
                     for(NumericDataElement elem : numericDatas) {
                         Row row = rowMap.get(elem.getOunitId());
                         if(row != null) {
                             String variateName = elem.getVariateName();
                             if(variateName != null) {
                                 variateName = variateName.trim();
                             }
                             short columnIndex = columnsMap.get(variateName).shortValue();
                             Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                            cell.setCellValue(elem.getValue());
                         }
                     }
                 }
             }
             
         }
         
         //adjust column widths of description sheet to fit contents
         for(int ctr = 0; ctr < 8; ctr++) {
             if(ctr != 1) {
                 descriptionSheet.autoSizeColumn(ctr);
             }
         }
         
         //adjust column widths of observation sheet to fit contents
         for(int ctr = 0; ctr < observationSheetColumnIndex; ctr++) {
             observationSheet.autoSizeColumn(ctr);
         }
         
         try {
             //write the excel file
             FileOutputStream fileOutputStream = new FileOutputStream(filename);
             workbook.write(fileOutputStream);
             fileOutputStream.close();
             return fileOutputStream;
         } catch(Exception ex) {
             throw new DatasetExporterException("Error with writing to: " + filename, ex);
         }
     }
     
     private String[] getTraitScaleMethodInfo(Integer traitId, Integer scaleId, Integer methodId) throws DatasetExporterException {
 String toreturn[] = new String[4];
         
         try {
             Trait trait = this.traitDataManager.getTraitById(traitId);
             Scale scale = this.traitDataManager.getScaleByID(scaleId);
             TraitMethod method = this.traitDataManager.getTraitMethodById(methodId);
             
             if(trait != null){
                 toreturn[0] = trait.getDescripton();
                 toreturn[1] = trait.getName();
             } else {
                 toreturn[0] = "Not specified";
                 toreturn[1] = "Not specified";
             }
             
             if(scale != null){
                 toreturn[2] = scale.getName();
             } else {
                 toreturn[2] = "Not specified";
             }
             
             if(method != null){
                 toreturn[3] = method.getName();
             } else {
                 toreturn[3] = "Not specified";
             }
         }
         catch(Exception ex) {
             throw new DatasetExporterException("Error with getting trait, scale, and method information for " +
                         "trait id = " + traitId +
                         " scale id = " + scaleId + 
                         " method id = " + methodId, ex);
         }
         
         return toreturn;
     }
     
 }
