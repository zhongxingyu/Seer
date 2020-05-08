 package com.tzapps.tzpalette.ui;
 
 public enum PaletteItemOption
 {
     View("View"),
     Rename("Rename"),
     Delete("Delete");
     
     private String name;    
     
     private PaletteItemOption(String name)
     {
         this.name = name;
     }
     
     public String getName()
     {
         return this.name;
     }
     
     public static PaletteItemOption fromString(String name)
     {
         if (name != null)
         {
             for (PaletteItemOption option : PaletteItemOption.values())
             {
                 if (name.equalsIgnoreCase(option.name))
                     return option;
             }
         }
         
         return null;
     }
}
