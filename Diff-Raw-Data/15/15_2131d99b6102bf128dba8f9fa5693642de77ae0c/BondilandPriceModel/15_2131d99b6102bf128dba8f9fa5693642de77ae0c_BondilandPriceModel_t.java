 package net.deepbondi.minecraft.market;
 import org.bukkit.Material;
 
 // A very simple model based on supply only.  The marginal cost of an item
 // is determined by a power law function of the supply, adjusted to fix
 // `basePricePerItem` as the price of a single item when the stock is at
 // `referenceStockLevel' items, and `maxPricePerItem' as the price 
 // when there is only one item left.
 // 
 // Note that `maxPricePerItem` is only a very rough approximation due to
 // the use of an integral in place of a summation in `marketValue`.  This
 // is an intentional compromise.
 public class BondilandPriceModel implements PriceModel {
     double basePricePerItem = 0.2;
     int referenceStockLevel = 1000;
     double maxPricePerItem = 30;
     
     // Compute the total value in the market for commodity with the given
     // stack size and stock level.
         // The proper computation would be the sum over the marginal value
         // function from stock levels 0 through qty-1.
         // This is an approximation (the integral of the same function from 0 to qty)
     private double marketValue(int stackSize, long qty) {
         double p = basePricePerItem;
         double y0 = maxPricePerItem;
         double r = referenceStockLevel;
         
         double alpha = StrictMath.log(y0/p) / StrictMath.log(r) - 1.0;
         return y0 / ((-alpha) * StrictMath.pow((double)qty, alpha));
     }
     
     private double basePrice(Commodity item, long qty, CommoditiesMarket market) {
         Material itemMaterial = Material.getMaterial(item.getItemId());

        // Cookies are always 1.0
        if (itemMaterial == Material.COOKIE) return StrictMath.abs(qty);

         int stackSize = itemMaterial.getMaxStackSize();
         long stockLevel = item.getInStock();
         
         return StrictMath.abs(marketValue(stackSize, stockLevel) 
                             - marketValue(stackSize, stockLevel + qty));
     }
     
    private double buyerTax  = 0.05;
    private double sellerTax = 0.05;
     
     public double checkBuyPrice(Commodity item, long qty, CommoditiesMarket market) {
         return (1.0 + buyerTax) * basePrice(item, -qty, market);
     }
     public double checkSellPrice(Commodity item, long qty, CommoditiesMarket market) {
         return basePrice(item, qty, market) / (1.0 + sellerTax);
     }
 }
 
