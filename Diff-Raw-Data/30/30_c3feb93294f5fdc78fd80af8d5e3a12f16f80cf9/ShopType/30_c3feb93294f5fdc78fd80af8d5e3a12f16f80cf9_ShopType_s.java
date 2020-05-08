 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 
 package shoptool;
 
 import java.util.Iterator;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class ShopType implements ShopTypeInterface {
     private String name;
     private PriceList pricelist;
     private double interestBuy;
     private double interestSell;
     
     public void setPricelist(PriceList p) {
         pricelist = p;
     }
     
     /**
      * Recalculate prices in 'pricelist' according to interests
      */
     public void recalcPrices() {
         Iterator<PriceListItem> it = pricelist.iterator();
         while (it.hasNext()) {
             PriceListItem item = it.next();
             item.setPriceBuy( (int) Math.ceil(item.priceBuy() + item.priceBuy()*interestBuy) );
            item.setPriceSell( (int) Math.floor(item.priceSell() + item.priceSell()*interestSell) );
         }
     }
     
     public void addItem(PriceListItem item) {
         pricelist.addItem(item);
     }
     
     @Override
     public String name() {
         return name;
     }
     
     @Override
     public PriceList pricelist() {
         return pricelist;
     }
 
     public void setInterestBuy(double interestBuy) {
         this.interestBuy = interestBuy;
     }
 
     public void setInterestSell(double interestSell) {
         this.interestSell = interestSell;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     
 }
