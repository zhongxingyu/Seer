 package net.worldoftomorrow.nala.ni.CustomItems;
 
 public enum CustomType {
     FURNACE("furnace", "oven"),
     WORKBENCH("crafting_table", "craftingtable", "workbench");
 
     public final String[] names;
 
     private CustomType(String... names) {
         this.names = names;
     }
 
     public String[] getNames() {
        return names.clone();
     }
 }
