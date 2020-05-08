 /**
  * Models a bike shop with basic buy and sell functions.
  *
  * @author Markus Iser, Martin Thoma
  * @version 1.0
  */
 class BikeShop {
     /** By how much should the size of the array be changed? */
     private static final int RESIZE_ARRAY_BY = 5;
 
     /** How many bikes should the shop initialy be able to store? */
     private static final int INITIAL_SHOP_SIZE = 10;
 
     /** Represents the bike warehouse, that can hold bikes. */
     private Bike[] warehouse;
 
     /** The number of bikes that are currently at the warehouse. */
     private int nrOfBikes;
 
     /**
      * Initialize a bikeshop with a capacity of INITIAL_SHOP_SIZE
      * bikes, but no stored bikes.
      */
     public BikeShop() {
         warehouse = new Bike[INITIAL_SHOP_SIZE];
         nrOfBikes = 0;
     }
 
     /**
      * Add a new bike to the warehouse.
      * @param bike The new bike you want to add.
      */
     public void buy(final Bike bike) {
         warehouse[nrOfBikes] = bike;
         nrOfBikes++;
         if (nrOfBikes == warehouse.length) {
             Bike[] newStock = new Bike[warehouse.length + RESIZE_ARRAY_BY];
             System.arraycopy(warehouse, 0, newStock, 0, warehouse.length);
             this.warehouse = newStock;
         }
     }
 
     /**
      * Removes the bike at position bikePlace from the warehouse.
      * @param bikePlace The position of the bike in the warehouse.
      */
    public void sell(final int bikePlace) {
         if (bikePlace < nrOfBikes) {
             nrOfBikes--;
             warehouse[bikePlace] = warehouse[nrOfBikes];
         }
         if (nrOfBikes < warehouse.length - RESIZE_ARRAY_BY) {
             Bike[] newStock = new Bike[warehouse.length - RESIZE_ARRAY_BY];
             System.arraycopy(warehouse, 0, newStock, 0, newStock.length);
             this.warehouse = newStock;
         }
     }
 }
 
