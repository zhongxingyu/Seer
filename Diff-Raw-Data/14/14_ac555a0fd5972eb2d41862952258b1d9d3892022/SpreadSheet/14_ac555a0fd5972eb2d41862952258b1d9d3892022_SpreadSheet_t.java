 package com.springapp.mvc.utils;
 
 import com.springapp.mvc.domain.Employee;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.ss.usermodel.Cell;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static java.lang.Double.parseDouble;
 
 public class SpreadSheet {
     private final HSSFSheet sheet;
 
     public SpreadSheet(HSSFSheet sheet) {
         this.sheet = sheet;
     }
     public String getCellValue(int row, int column) {
         HSSFRow sheetRow = sheet.getRow(row);
         HSSFCell cell = sheetRow.getCell(column);
 
        if ((cell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
             return String.valueOf(cell.getNumericCellValue());
         } else if(cell.getCellType()==Cell.CELL_TYPE_BOOLEAN) {
             return String.valueOf(cell.getBooleanCellValue());
 
        }else if(cell.getCellType()==Cell.CELL_TYPE_FORMULA){
            return String.valueOf(cell.getDateCellValue());
         }
         else {
             return cell.getStringCellValue();
         }
     }
 
     public Double getCellValueAsDouble(int row, int column) {
         String cellValueAsString = getCellValue(row, column);
         return cellValueAsString.trim().length() == 0 ? null : parseDouble(cellValueAsString);
     }
 
     public Long getCellValueAsLong(int row, int column) {
         String cellValue = getCellValue(row, column);
        return cellValue.trim().length() == 0? null : Long.parseLong(cellValue.split("\\.")[0]);
     }
 
     public List<Employee> buildEmployee() {
         ArrayList<Employee> employees = new ArrayList<Employee>();
         int rowNum = sheet.getPhysicalNumberOfRows();
         if(rowNum<2){
             System.out.println("no information to build object");
             return employees;
         }
         for(int i=1;i<rowNum;i++){
             employees.add(createEmployee(i));
         }
         return employees;
 
     }
 
     private Employee createEmployee(int row) {
         Employee employee = new Employee();
         employee.setId(getCellValueAsLong(row, 0));
         employee.setAccount(getCellValue(row, 1));
         employee.setName(getCellValue(row,2));
         employee.setRate(getCellValueAsDouble(row,3));
        employee.setTimeToJoin(DateUtils.createDateWithFormat(getCellValue(row,4),"dd/MM/yyyy"));
         employee.setTotalWorkYear(getCellValueAsDouble(row,5));
         employee.setTimeInTW(getCellValueAsDouble(row,6));
         employee.setGraduate(Boolean.parseBoolean(getCellValue(row,7)));
         employee.setOnceOut(Boolean.parseBoolean(getCellValue(row,8)));
         employee.setTimeOnThisAccount(getCellValueAsDouble(row, 9));
         return employee;
     }
 }
