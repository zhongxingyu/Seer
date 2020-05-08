 
 /**
  * A customer.
  * 
  * @author Fergal Hainey, Janie Sinclair, Chiedu Agborh, Yowana Kuvoka
  * @version 2010.03.02
  */
 
 import java.util.Random;
 import java.util.ArrayList;
 
 public class Customer
 {
     private static int nextId = 1;
     private int id;
     private int arrivedShopFloor;
     private int departShopFloor;
     private int arrivedFrontCheckOut;
     private int departCheckOut;
     private SuperSim superSim;
     private final int numberOfItems;
     private ArrayList<Item> shoppingBasket;
 
     public Customer(SuperSim superSim)
     {
         makeId();
         this.superSim = superSim;
         arrivedShopFloor = this.superSim.getIterationsSoFar();
         numberOfItems = calcNumberOfItems();
         departShopFloor = numberOfItems * superSim.getShopFloorConstant() + arrivedShopFloor;
         shoppingBasket = new ArrayList<Item>();
         fillBasket();
     }
 
     private int calcNumberOfItems()
     {
         int itemsMean = superSim.getItemsMean();
         int itemsStandardDeviation = superSim.getItemsStandardDeviation();
         double standardLowerLimit =
             ((double) (superSim.getItemsLowerLimit() - itemsMean))
             /
             ((double) itemsStandardDeviation);
         double standardUpperLimit =
             ((double) (superSim.getItemsUpperLimit() - itemsMean))
             /
             ((double) itemsStandardDeviation);
         double gaussian;
         Random rand = superSim.getRand();
         do {
             gaussian = rand.nextGaussian();
         }
        while (gaussian <= standardLowerLimit && gaussian >= standardUpperLimit);
        return (int) Math.round((gaussian * (double) itemsStandardDeviation) + (double) itemsMean);
     }
     //Janie, fill arraylist of items with items randomly selected from StockList
     private void fillBasket()
     {
         for(int i = 0; i < numberOfItems ; i++)
         {
             String[] barcodes = superSim.getStockListBarcodes();
             Random z = superSim.getRand();
             Item y = superSim.getStockList().get(barcodes[z.nextInt(barcodes.length)]);
             shoppingBasket.add(y);
         }
     }
     
     // set the customers unique number and increment the static id counter thing
     private void makeId()
     {
         id = nextId;
         nextId++;
     }
     
     // Accessors
     public int getArrivedShopFloor()
     {
         return arrivedShopFloor;
     }
     
     public int getNumberOfItems()
     {
         return numberOfItems;
     }
     
     public int getDepartShopFloor()
     {
         return departShopFloor;
     }
     
     public int getArrivedFrontCheckOut()
     {
         return arrivedFrontCheckOut;
     }
     
     public int getDepartCheckOut()
     {
         return departCheckOut;
     }
     
     public ArrayList<Item> getItems()
     {
         return shoppingBasket;
     }
     
     public int getId()
     {
         return id;
     }
     
     // Mutators
     public void process()
     {
         arrivedFrontCheckOut = superSim.getIterationsSoFar();
         departCheckOut = arrivedFrontCheckOut + numberOfItems * superSim.getCheckOutConstant();
     }
 }
