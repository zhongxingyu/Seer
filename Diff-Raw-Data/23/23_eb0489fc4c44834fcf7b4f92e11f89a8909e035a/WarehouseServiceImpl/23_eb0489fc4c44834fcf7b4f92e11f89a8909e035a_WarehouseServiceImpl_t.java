 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package aic2010.services;
 
 import aic2010.TestDataManager;
 import aic2010.exception.UnknownProductException;
 import aic2010.model.Product;
 import aic2010.model.ProductEntry;
 import aic2010.model.WarehouseAnswer;
import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 /**
  *
  * @author rudolf
  */
 
 @WebService(endpointInterface = "aic2010.services.WarehouseService",
             serviceName = "WarehouseService",
             targetNamespace="http://infosys.tuwien.ac.at/aic10/ass1/dto/warehouse",
             portName="WarehousePT")
 public class WarehouseServiceImpl extends SupplierServiceImpl implements WarehouseService{
 
     private Map<String, ProductEntry> products;
    private Map<String, Product> actual_products;
 
     public WarehouseServiceImpl(){
             products = new HashMap<String, ProductEntry>();
            actual_products = new HashMap<String, Product>();
             addProducts();
     }
 
     private void addProducts(){
         System.out.println("Called addProducts...");
         Product product1 = TestDataManager.getProduct(false, false);
         
         ProductEntry entry1 = new ProductEntry();
         entry1.setDeliveryTime(3);
         entry1.setAvailableAmount(3);
 
         Product product2 = TestDataManager.getProduct2();
         ProductEntry entry2 = new ProductEntry();
         entry2.setDeliveryTime(2);
         entry2.setAvailableAmount(1);
 
         products.put(product1.getId(), entry1);
         products.put(product2.getId(), entry2);
        actual_products.put(product1.getId(), product1);
        actual_products.put(product2.getId(), product2);
     }
 
     @Override
     public WarehouseAnswer check_availability(Product product, int amount)
     throws UnknownProductException{
         if (product != null)
             System.out.println("Called check_availability with product id: " + product.getId());
         else
             System.out.println("Called check_availability with NULL product");
         if(products.containsKey(product.getId())){
 
             ProductEntry entry = products.get(product.getId());
 
             WarehouseAnswer answer = new WarehouseAnswer();
             answer.setDeliveryTime(entry.getDeliveryTime());
 
             if(amount<=entry.getAvailableAmount())
                 answer.setIsAvailable(true);
             else
                 answer.setIsAvailable(false);
 
             return answer;
         }
         else{
             throw new UnknownProductException("Could not find product", product.getName());
         }
     }

    @Override
    public BigDecimal order(Product product,
                        Integer amount)
    throws UnknownProductException{
        if(actual_products.containsKey(product.getId())){
            Product actual_product = actual_products.get(product.getId());
            BigDecimal overallAmount = actual_product.getSingleUnitPrice().multiply(new BigDecimal(amount));
            return overallAmount;
        }
        else{
            throw new UnknownProductException("Could not find product", product.getId());
        }
    }
 }
