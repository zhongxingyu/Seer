 package excel;
 
 import jxl.read.biff.BiffException;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Evgen
  * Date: 12.04.13
  * Time: 14:01
  * To change this template use File | Settings | File Templates.
  */
 public class KpiServiceProductProvider extends AbstractProductProvider {
 
 
     private static KpiServiceProductProvider kpiServiceProductProvider = new KpiServiceProductProvider();
 
     {
         try {
             settings.setWorkbook("d:\\Other\\pechat\\price1.xls");
         } catch (BiffException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         settings.setSheet();
 
         settings.setHeaderRow(2490);
 
         settings.setColumnProductName(3);
         settings.setColumnProductSku(2);
         settings.setColumnProductPrice(4);
         settings.setColumnWarranty(8);
 
         currentRow =  settings.getHeaderRow();
     }
 
     private KpiServiceProductProvider(){
     }
 
     public static KpiServiceProductProvider getInstance(){
         return kpiServiceProductProvider;
     }
 
     @Override
     protected String getCategoryNameOfRow(int row) {
         return getRow(row)[2].getContents();
     }
 
     @Override
     protected boolean isCategory(int row) {
         if (getRow(row)[2].getContents().equals(getRow(row)[3].getContents()))
             return true;
 
         return false;
     }
 
     @Override
     protected boolean isLastProduct() {
         if  (getNextProductRow().length == 1)
             return true;
 
         return false;
     }
 
     @Override
     public Product nextProduct() {
         Product product = new Product();
         product.categoryName = currentCategory;
         product.manufacturer = getCurrentRow()[settings.getColumnManufacturer()].getContents();
         product.productName = getCurrentRow()[settings.getColumnProductName()].getContents();
         product.productPrice = getCurrentRow()[settings.getColumnProductPrice()].getContents();
         product.warranty = getCurrentRow()[settings.getColumnWarranty()].getContents();
         product.productSku = getCurrentRow()[settings.getColumnProductSku()].getContents();
         product.shortDescription = getCurrentRow()[settings.getColumnShortDescription()].getContents();
         return product;
     }
 }
