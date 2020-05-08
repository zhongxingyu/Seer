 import java.util.Random;
 
 /**
  * The test class populates the warehouse program with
  * some sample data.
  */
 public class Test  {
     StockControl stockControl;
     Random rand;
     public Test(int productQuant, int saleQuant)  {
         stockControl = new StockControl();
         rand = new Random();
         for (int i = 0; i < productQuant; i++)  {
             addStock(i);
         }
         
         for (int i = 0; i < saleQuant; i++)  {
             addSale();
         }
         stockControl.sortLists();
         stockControl.printProducts();
         stockControl.printSales();
         
         stockControl.processSales();
         stockControl.printProducts();
     }
     
     public boolean addStock(int quant)  {
         
         int stock = rand.nextInt(200);
         int reorderLevel = (rand.nextInt(10)+1)*10;
         int orderLevel = -1;
         while (orderLevel < stock)  {
             orderLevel = reorderLevel + (rand.nextInt(11)*20);
         }
         // System.out.println("product"+quant+"    "+stock+"    "+reorderLevel+"    "+orderLevel);
         return stockControl.newProduct(("product" + quant), stock, reorderLevel, orderLevel);
     }
     
     /**
      * This method adds a new sale to the sales list controlled within
      * the StockControl class.  The product to be sold is chosen at 
      * random from the list of products managed by StockControl. The quantity
      * of the sale is randomly chosen, limited by the amount of product in stock
      * (stock).
      * 
      */
     public void addSale()  {
         stockControl.shuffleLists();
         Product product = stockControl.getStockedProduct();
         if (product == null)  {
             return;
         }
         stockControl.newSale(product, (rand.nextInt(product.getStock()+1)+1));
         stockControl.processSales();
     }
     
 }
