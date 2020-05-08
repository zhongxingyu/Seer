 package com.tzapps.tzpalette.data;
 
 public enum PaletteDataType
 {
     ColorToRGB("RGB"),
     ColorToHSV("HSV"),
     ColorToHSL("HSL");
 
     private String name;
     
     PaletteDataType(String name)
     {
         this.name = name;
     }
     
     public String getName()
     {
         return this.name;
     }
     
     public static PaletteDataType fromString(String name)
     {
         if (name != null)
         {
             for (PaletteDataType dataType : PaletteDataType.values())
             {
                 if (name.equalsIgnoreCase(dataType.name))
                     return dataType;
             }
         }
         
         return null;
     }
}

