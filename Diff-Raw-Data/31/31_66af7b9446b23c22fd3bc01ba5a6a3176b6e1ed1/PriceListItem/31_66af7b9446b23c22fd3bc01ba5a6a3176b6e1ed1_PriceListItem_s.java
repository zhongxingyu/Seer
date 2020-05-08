 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import static shoptool.Util.*;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class PriceListItem implements PriceListItemInterface {
     private final static String seperator = " ";
     private final static char ID_SEPERATOR = ':';
     private final static String ID = "ID";
     private final static String NAME = "NAME";
     private final static String PRICE = "PRICE";
     private final static String PRICE_BUY = "PRICE_BUY";
     private final static String PRICE_SELL = "PRICE_SELL";
     private final static String NORMAL_STOCK = "NORMAL";
     private final static String MAX_STOCK = "MAX";
     private final static String STOCK_UPDATE_TIME = "TIME";
     private final static String DEFAULT_NAME = "";
     private final static int DEFAULT_MAXSTOCK = 0;
     private final static int DEFAULT_NORMALSTOCK = 0;
     private final static int DEFAULT_STOCK_UPDATE_TIME = 0;
     private String id; //minecraft block id
     private String name; //minecraft block name
    //prices in relation to base value 10
     private int priceSell;
     private int priceBuy;
     private int normalStock; //amount of items the store tries to have
     private int maxStock; //the maximum amount of items the store can keep
     private int stockUpdateTime; //time in seconds the current stock is changed by one item towards 'normalStock'
 
     /**
      * Copy constructor.
      * @param src 
      */
     public PriceListItem(PriceListItem src) {
         id = src.id;
         name = src.name;
         priceSell = src.priceSell;
         priceBuy = src.priceBuy;
         normalStock = src.normalStock;
         maxStock = src.maxStock;
         stockUpdateTime = src.stockUpdateTime;
     }
     
     /**
      * Parse an 'PriceListItem' from a String (line of a pricelist)
      * @param line 
      */
     public PriceListItem(String line) throws Exception {
         setSeperator(seperator);
         setId(extract(line, ID));
         if (!reviseID()) //fatal: no correct ID
         {
             throw new Exception();
         }
         
         setName(extract(line, NAME));
         
         int price = extractInt(line, PRICE);
         if (price == -1) {
             setPriceBuy(extractInt(line, PRICE_BUY));
             setPriceSell(extractInt(line, PRICE_SELL));
         } else {
             setPriceBuy(price);
             setPriceSell(price);
         }
 
         setNormalStock(extractInt(line, NORMAL_STOCK));
         setMaxStock(extractInt(line, MAX_STOCK));
         setStockUpdateTime(extractInt(line, STOCK_UPDATE_TIME));
     }
 
     /**
      * Check this item for correctness and set default values for
      * incorrect fields.
      * Only if building a correct item failed, 'false' is returned.
      * @return 
      */
     public boolean revise() {
         //note: id must be correct already
         if (name == null) {
             revWarn(NAME, toStr());
             setName(DEFAULT_NAME);
         }
         //leave priceBuy/priceSell: '-1' means not available
         if (normalStock == -1) {
             revWarn(NORMAL_STOCK, toStr());
             setNormalStock(DEFAULT_NORMALSTOCK);
         }
         if (maxStock == -1) {
             revWarn(MAX_STOCK, toStr());
             setMaxStock(DEFAULT_MAXSTOCK);
         }
         if (stockUpdateTime == -1) {
             revWarn(STOCK_UPDATE_TIME, toStr());
             setStockUpdateTime(DEFAULT_STOCK_UPDATE_TIME);
         }
         return true;
     }
 
     /**
      * Check if 'id' is in one of the two formats
      * 1) ID
      * 2) ID:VALUE
      * where "ID" and "VALUE" are correct Integers without leading zeros.
      * If possible, 'id' is converted to this format and 'true' is returned.
      * Otherwise 'false' is returned.
      * @return 
      */
     private boolean reviseID() {
         if (id == null) {
             return false;
         }
         int i;
         try { //normal number (format 1)?
             i = Integer.parseInt(id);
             //ok: write correct format 1) back
             id = Integer.toString(i);
             return true;
         } catch (NumberFormatException e1) {
             int val;
             try { //ID:VALUE (format 2)?
                 i = Integer.parseInt(id.substring(0, id.indexOf(ID_SEPERATOR)));
                 val = Integer.parseInt(id.substring(id.indexOf(ID_SEPERATOR) + 1, id.length()));
                 //ok: write correct format 2) back
                 id = Integer.toString(i) + ID_SEPERATOR + Integer.toString(val);
                 return true;
             } catch (NumberFormatException e2) {
                 return false;
             }
         }
     }
     
     /**
      * Factor the prices.
      * Buy is always ceiled, sell floored.
      * @param factorBuy
      * @param factorSell 
      */
     public void changePrices(double factorBuy, double factorSell) {
         priceBuy = (int) Math.ceil(priceBuy * factorBuy);
         priceSell = (int) Math.floor(priceSell * factorSell);
     }
 
     public String toStr() {
         setSeperator(seperator);
         StringBuilder sb = new StringBuilder();
         sb.append(svalToStr(ID, id));
         sb.append(svalToStr(NAME, name));
         sb.append(ivalToStr(PRICE_BUY, priceBuy));
         sb.append(ivalToStr(PRICE_SELL, priceSell));
         sb.append(ivalToStr(NORMAL_STOCK, normalStock));
         sb.append(ivalToStr(MAX_STOCK, maxStock));
         sb.append(ivalToStr(STOCK_UPDATE_TIME, stockUpdateTime));
         sb.delete(sb.length() - seperator.length(), sb.length()); //remove last seperator
         return sb.toString();
     }
 
     @Override
     public String id() {
         return id;
     }
 
     @Override
     public String name() {
         return name;
     }
 
     @Override
     public int priceSell() {
         return priceSell;
     }
 
     @Override
     public int priceBuy() {
         return priceBuy;
     }
 
     @Override
     public int normalStock() {
         return normalStock;
     }
 
     @Override
     public int maxStock() {
         return maxStock;
     }
 
     @Override
     public int stockUpdateTime() {
         return stockUpdateTime;
     }
 
     @Override
     public boolean isBuy() {
        return priceBuy != -1;
     }
 
     @Override
     public boolean isSell() {
        return priceSell != -1;
     }
 
     private void setId(String id) {
         this.id = id;
     }
 
     public final void setMaxStock(int maxStock) {
         this.maxStock = maxStock;
     }
 
     private void setName(String name) {
         this.name = name;
     }
 
     public final void setNormalStock(int normalStock) {
         this.normalStock = normalStock;
     }
 
     private void setPriceBuy(int priceBuy) {
         this.priceBuy = priceBuy;
     }
 
     private void setPriceSell(int priceSell) {
         this.priceSell = priceSell;
     }
 
     public final void setStockUpdateTime(int stockUpdateTime) {
         this.stockUpdateTime = stockUpdateTime;
     }
 }
