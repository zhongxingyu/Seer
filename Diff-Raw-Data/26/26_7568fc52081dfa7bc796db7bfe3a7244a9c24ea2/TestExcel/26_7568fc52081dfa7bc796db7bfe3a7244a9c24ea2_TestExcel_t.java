 package excel;
 
 import jxl.Cell;
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Evgen
  * Date: 16.03.13
  * Time: 22:12
  * To change this template use File | Settings | File Templates.
  */
 public class TestExcel {
     public static void main(String[] args) throws BiffException, IOException {
         Workbook workbook = Workbook.getWorkbook(new File("d:\\Other\\pechat\\price1.xls"));
         Sheet sheet = workbook.getSheet(0);
         int startRow = 3;
         int finishRow = 3560;
         int columnSku = 0;
         int columnCategory = 1;
         int columnManufacturer = 2;
         int columnName = 4;
         int columnPrice = 5;
         int columnWarranty = 6;
         int columnDescription = 11;
 
 //        for (int i = startRow; i<finishRow; i++){
 //            Cell[] column = sheet.getRow(i);
 //            if (isCategoryRow(column)){
 //                System.out.println("New category: "+ column[1].getContents());
 //            }
 //            else {
 //
 //                    Product product = new Product();
 //                    product.categoryName = column[columnCategory].getContents();
 //                    product.manufacturer = column[columnManufacturer].getContents();
 //                    product.productName = column[columnName].getContents();
 //                    product.productPrice = column[columnPrice].getContents();
 //                    product.warranty = column[columnWarranty].getContents();
 //                    product.productSku = column[columnSku].getContents();
 //                    product.shortDescription = column[columnDescription].getContents();
 //                    System.out.println(product);
 //            }
 //        }
         Cell[] column = sheet.getRow(2493);
         System.out.println(column[4].getContents());
 
     }
 
     private static boolean isCategoryRow(Cell[] column){
         return column[2].getContents() == column[3].getContents();
     }
 
 }
