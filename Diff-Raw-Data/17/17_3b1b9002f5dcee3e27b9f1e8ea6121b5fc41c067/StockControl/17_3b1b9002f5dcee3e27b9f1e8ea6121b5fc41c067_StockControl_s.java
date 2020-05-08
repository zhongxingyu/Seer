 import java.util.ArrayList;
 import java.util.Collections;
 /**
  * The stock control class
  */
 public class StockControl  {
     private ArrayList<Product> products;
     private ArrayList<Sale> sales;
 
     public StockControl()  {
         products = new ArrayList();
         sales = new ArrayList();
 
     }
 
     public boolean newProduct(String name, int stock, int reorderLevel, int orderLevel)  {
         return products.add(new Product(name, stock, reorderLevel, orderLevel));
     }
 
     public boolean newSale(Product product, int quant)  {
         return sales.add(new Sale(product, quant));
     }
     
     public Product getProduct(int index)  {
         return products.get(index);
     }
     
     public int getProductsLength()  {
         return products.size();
     }
     
     /**
      * Returns the first product in the list with a stock greater than 0.
      */
     public Product getStockedProduct()  {
         for (Product product : products)  {
             if (product.getStock() > 0)  {
                 return product;
             }
         }
         return null;
     }
         
    
     public void processSales()  {
         for (Product product : products)  {
             for (Sale sale : sales)  {
                if (sale.getProductName() == product.getName())  {
                     product.removeStock(sale.getQuant());
                 }
             }
         }
             
     }
     
     public void printProducts()  {
         System.out.println("PRODUCTS");
         for (Product product : products)  {
             System.out.println("    " + product);
         }
         System.out.println();
     }
     
     public void printSales()  {
         System.out.println("SALES");
         for (Sale sale : sales)  {
             System.out.println("    " + sale);
         }
         System.out.println();
     }
     
     public void sortLists()  {
         Collections.sort(sales);
         Collections.sort(products);
     }
     
     public void shuffleLists()  {
         Collections.shuffle(sales);
         Collections.shuffle(products);
     }
 
   
 }
 
