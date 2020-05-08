 package org.freshwaterlife.fishlink.metadatacreator;
 
 import java.util.Calendar;
 import java.util.Date;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.DataValidation;
 import org.apache.poi.ss.usermodel.DataValidationConstraint;
 import org.apache.poi.ss.usermodel.DataValidationHelper;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddressList;
 import org.freshwaterlife.fishlink.POI_Utils;
 
 /**
  *
  * @author Christian
  */
 public class CYAB_Sheet {
 
     private Sheet poiSheet;
 
     private CellStyle dateStyle;
 
     private CellStyle dateAndTimeStyle;
 
     CYAB_Sheet(Sheet sheet) {
         poiSheet = sheet;
         Workbook wb = sheet.getWorkbook();
         CreationHelper createHelper = wb.getCreationHelper();
         dateAndTimeStyle = wb.createCellStyle();
         dateAndTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
         dateStyle = wb.createCellStyle();
         dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
     }
 
     /**
      * 
      * @param row Excel based
      * @return
      */
     private Row getRow(int row){
         Row poiRow = poiSheet.getRow(row -1);
         if (poiRow == null){
             poiRow = poiSheet.createRow(row - 1);
         }
         return poiRow;
     }
 
     private Cell getCell(String column, int row){
        Row poiRow = getRow(row);
         int columnNumber = POI_Utils.alphaToIndex(column);
         Cell cell = poiRow.getCell(columnNumber);
         if (cell == null){
            cell = poiRow.createCell(columnNumber);
         }
         return cell;
     }
 
     private Cell getCellZeroBased(int column, int row){
         Row poiRow = poiSheet.getRow(row);
         if (poiRow == null){
             poiRow = poiSheet.createRow(row);
         }
         Cell cell = poiRow.getCell(column);
         if (cell == null){
            cell = poiRow.createCell(column);
         }
         return cell;
     }
 
     /**
      * Sets the value of the cell to the integer.
      * 
      * Creates the row and or cell if required.
      * 
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      */
     void setValue (String column, int row, int value){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
     }
 
     /**
      * Sets the value of the cell to the double.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      */
     void setValue (String column, int row, double value){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
     }
 
     /**
      * Sets the value of the cell to the String.
      *
      * Creates the row and or cell if required.
      *
      * @param row Zero based
      * @param column zero based
      * @param value Integer value
      */
     public void setValueZeroBased (int column, int row, String value){
         Cell cell = getCellZeroBased(column, row);
         cell.setCellValue(value);
     }
 
     /**
      * Sets the value of the cell to the String.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      */
     public void setValue (String column, int row, String value){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
     }
 
     /**
      * Sets the value of the cell to the boolean.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      */
     public void setValue (String column, int row, boolean value){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
     }
 
     /**
      * Sets the value of the cell to the Date.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      */
     public void setValue (String column, int row, Date value){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(value);
         int hours = calendar.get(Calendar.HOUR_OF_DAY);
         int minutes = calendar.get(Calendar.MINUTE);
         if (hours == 0 && minutes == 0){
             //TODO: check for seconds
             cell.setCellStyle(dateStyle);
             CellStyle test = cell.getCellStyle();
         } else {
             //TODO: Check if just a time
             cell.setCellStyle(dateAndTimeStyle);
         }
     }
 
     /**
      * Sets the value of the cell to the Date.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      * @param value Integer value
      * @param format String format of the datacell;
      */
     public void setValue (String column, int row, Date value, String format){
         Cell cell = getCell(column, row);
         cell.setCellValue(value);
         Workbook wb = poiSheet.getWorkbook();
         CreationHelper createHelper = wb.getCreationHelper();
         CellStyle cellStyle = wb.createCellStyle();
         cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
         cell.setCellStyle(cellStyle);
     }
 
     /**
      * Sets the background of the cell to Aqua.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      */
     public void setForegroundAqua (String column, int row){
         Cell cell = getCell(column, row);
         // Aqua background
         CellStyle style = poiSheet.getWorkbook().createCellStyle();
         style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
         style.setFillPattern(CellStyle.SOLID_FOREGROUND);
         Font font = poiSheet.getWorkbook().createFont();
         font.setBoldweight(font.BOLDWEIGHT_BOLD);
         style.setFont(font);
         cell.setCellStyle(style);
     }
 
     void addValidation (String replaceColum, String column, int row, String rule, String popupTitle, String popupMessage,
            int errorStyle, String errorTitle, String errorMessage) {
         rule = rule.replaceAll("\\$"+replaceColum, "\\$" + column);
         addValidation (column, row, rule, popupTitle, popupMessage, errorStyle, errorTitle, errorMessage);
     }
     
     private void addValidation (String column, int row, String rule, String popupTitle, String popupMessage,
             int errorStyle, String errorTitle, String errorMessage) {
         int columnNumber = POI_Utils.alphaToIndex(column);
         CellRangeAddressList addressList = new CellRangeAddressList(row - 1, row -1, columnNumber,  columnNumber);
         DataValidationHelper dataValidationHelper = poiSheet.getDataValidationHelper();
         DataValidationConstraint constraint;
         if (rule.isEmpty()){
             if (popupTitle.isEmpty() && popupMessage.isEmpty()){
                 return; //No rule or message so ignore.
             }
             //Create an any rule. Numberic method is only one that support Validation Type.
             constraint = dataValidationHelper.createNumericConstraint(DataValidationConstraint.ValidationType.ANY, 
                     DataValidationConstraint.OperatorType.IGNORED, null, null); 
         } else if (rule.startsWith("{")){
             rule = rule.replace("{", "");
             rule = rule.replace("}", "");
             String[] values = rule.split(",");
             constraint = dataValidationHelper.createExplicitListConstraint(values);
         } else if (rule.equals("BLANK")){
             constraint = dataValidationHelper.createTextLengthConstraint(
                     DataValidationConstraint.OperatorType.LESS_THAN, "1", "1");
         } else {
             constraint = dataValidationHelper.createFormulaListConstraint(rule);
         }
         DataValidation dataValidation = dataValidationHelper.createValidation(constraint, addressList);
         dataValidation.setSuppressDropDownArrow(false);
         if (!popupTitle.isEmpty()){
             dataValidation.createPromptBox(popupTitle, popupMessage);
             dataValidation.setShowPromptBox(true);
         }
         dataValidation.setErrorStyle(errorStyle);
         if (!errorTitle.isEmpty()){
             dataValidation.createErrorBox(errorTitle, errorMessage);
         }
         poiSheet.addValidationData(dataValidation);
     }
 
     /**
      * Sets the background of the cell to Aqua.
      *
      * Creates the row and or cell if required.
      *
      * @param row Using Excel counting
      * @param column Using Excel names
      */
     public void createFreezePane (String column, int row){
         int columnNumber = POI_Utils.alphaToIndex(column);
         poiSheet.createFreezePane(columnNumber, row, columnNumber, row);
     }
 
     /* Pass through methods */
     
     void autoSizeColumn(int column) {
         poiSheet.autoSizeColumn(column);
     }
 
 
 }
