 package net.minecraft.server;
 
 import forge.Configuration;
 
 public class BerriesConfig
 {
     public final Configuration config;
     public final int cropid_vine;
     public final int cropid_blackberry;
     public final int cropid_raspberry;
     public final int cropid_strawberry;
     public final int cropid_blueberry;
     public final int cropid_huckleberry;
     public final ItemStack item_blackberry;
     public final ItemStack item_raspberry;
     public final ItemStack item_strawberry;
     public final ItemStack item_blueberry;
     public final ItemStack item_huckleberry;
 
     public BerriesConfig(Configuration var1)
     {
         this.config = var1;
 
         try
         {
             var1.load();
         }
         catch (RuntimeException var3)
         {
             var3.printStackTrace();
         }
 
         this.cropid_vine = this.getInt("cropid.vine", 60);
         this.cropid_blackberry = this.getInt("cropid.blackberry", 61);
         this.cropid_raspberry = this.getInt("cropid.raspberry", 62);
         this.cropid_strawberry = this.getInt("cropid.strawberry", 63);
         this.cropid_blueberry = this.getInt("cropid.blueberry", 64);
         this.cropid_huckleberry = this.getInt("cropid.huckleberry", 65);
         this.item_blackberry = this.getItem("item.blackberry", 20000, 5);
         this.item_raspberry = this.getItem("item.raspberry", 20000, 8);
         this.item_strawberry = this.getItem("item.strawberry", 0, 0);
         this.item_blueberry = this.getItem("item.blueberry", 20000, 7);
         this.item_huckleberry = this.getItem("item.huckleberry", 20000, 6);
         var1.save();
     }
 
     public int getInt(String var1, int var2)
     {
         return Integer.parseInt(this.config.getOrCreateIntProperty(var1, "general", var2).value);
     }
 
     public ItemStack getItem(String var1, int var2, int var3)
     {
         int var4 = this.getInt(var1, var2);
 
         if (var4 == 0)
         {
             return null;
         }
         else
         {
            int var5 = this.getInt(var1 + ".damage", var3);
             return new ItemStack(var4, 1, var5);
         }
     }
 
     public void registerCrops()
     {
         if (this.cropid_vine != 0)
         {
             new VineCrop(this.cropid_vine);
         }
 
         if (this.cropid_blackberry != 0 && this.item_blackberry != null)
         {
             new BlackberryCrop(this.cropid_blackberry, this.item_blackberry);
         }
 
         if (this.cropid_raspberry != 0 && this.item_raspberry != null)
         {
             new RaspberryCrop(this.cropid_raspberry, this.item_raspberry);
         }
 
         if (this.cropid_strawberry != 0 && this.item_strawberry != null)
         {
             new StrawberryCrop(this.cropid_strawberry, this.item_strawberry);
         }
 
         if (this.cropid_blueberry != 0 && this.item_blueberry != null)
         {
             new BlueberryCrop(this.cropid_blueberry, this.item_blueberry);
         }
 
         if (this.cropid_huckleberry != 0 && this.item_huckleberry != null)
         {
             new HuckleberryCrop(this.cropid_huckleberry, this.item_huckleberry);
         }
     }
 }
