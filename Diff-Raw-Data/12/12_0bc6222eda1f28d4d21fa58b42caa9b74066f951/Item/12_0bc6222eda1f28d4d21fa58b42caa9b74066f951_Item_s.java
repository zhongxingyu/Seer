 package com.programania.gildedrose;
 
 public class Item {
   public String name;
   public int sellIn;
   public int quality;
 
   public Item(String name, int sellIn, int quality) {
     this.name = name;
     this.sellIn = sellIn;
     this.quality = quality;
   }
 
   public String getName() {
     return name;
   }
 
   public int getSellIn() {
     return sellIn;
   }
 
   public int getQuality() {
     return quality;
   }
 
   public void setZeroQuality() {
     quality = 0;
   }
 
   public void increaseQuality() {
     if (quality < 50)
       quality++;
   }
 
   public void decreaseQuality() {
    if (quality < 50)
       quality--;
   }
 
   public void decreaseSellIn() {
     sellIn--;
   }
 
   public boolean is(String someName) {
     return name == someName;
   }
 }
