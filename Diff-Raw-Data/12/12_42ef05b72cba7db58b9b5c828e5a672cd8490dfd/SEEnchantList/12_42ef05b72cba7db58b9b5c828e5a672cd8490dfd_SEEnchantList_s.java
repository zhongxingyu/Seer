 package net.XerorBattler.SignEnchanter;
 
 public enum SEEnchantList {
    Durability ("DURABILITY");
 
     private final String name;
 
     SEEnchantList(String name) {
         this.name = name;
     }
     
     public String getName()
     {
         return this.name;
     }
     
   static public boolean isMember(String name) {
        SEEnchantList[] enchants = SEEnchantList.values();
        for (SEEnchantList enchant : enchants)
           if (enchant.name.equals(name))
               return true;
       return false;
    }
 }
