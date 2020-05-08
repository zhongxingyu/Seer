 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 
 package shoptool;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * 
  * @author Felix Wiemuth
  */
 public class PriceList implements PriceListInterface {
    LinkedList<PriceListItem> items = new LinkedList<PriceListItem>();
     
     @Override
     public Iterator<PriceListItem> iterator() {
         return items.iterator();
     }
 
     void addItem(PriceListItem item) {
         items.add(item);
     }
     
     /**
      * Get the price lists for shops //TODO wrap in Type "ShopType"
      * @return 
      */
     
     
 }
