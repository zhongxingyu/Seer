 package org.motechproject.whp.reports.export.query.service;
 
 import bad.robot.excel.valuetypes.Coordinate;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import java.io.IOException;
 
public class ExcelTest {
 
     protected final ExcelExporter excelExporter = new ExcelExporter();
 
     protected org.apache.poi.ss.usermodel.Cell getCellForCoordinate(Coordinate coordinate, Workbook workbook) throws IOException {
         org.apache.poi.ss.usermodel.Row row = getRowForCoordinate(coordinate, workbook);
         return row.getCell(coordinate.getColumn().value());
     }
 
     protected org.apache.poi.ss.usermodel.Row getRowForCoordinate(Coordinate coordinate, Workbook workbook) throws IOException {
         Sheet sheet = workbook.getSheetAt(coordinate.getSheet().value());
         org.apache.poi.ss.usermodel.Row row = sheet.getRow(coordinate.getRow().value());
         if (row == null)
             throw new IllegalStateException("expected to find a row");
         return row;
     }
 
 }
