 package net.XerorBattler.SignEnchanter;
 
 public enum SEEnchantList {
    DURABILITY ("Durability");
 
     private final String name;
 
     SEEnchantList(String name) {
         this.name = name;
     }
     
     public String getName()
     {
         return this.name;
     }
     
   static public String getMember(String name) {
        SEEnchantList[] enchants = SEEnchantList.values();
        for (SEEnchantList enchant : enchants)
           if (enchant.name.equalsIgnoreCase(name))
               return enchant.name();
       return null;
    }
 }
