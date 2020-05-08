 template T {
     enum AnyOldEnumName {
 		FOO, BAR, KAKE, RISKOKER;
     }
 }
 
 package P  {
     inst T;
 
     class V {
         public static void main(String args[]) {
             /* dette fungerer ikke fordi jeg spesifikt har ekskludert metoden
                som får det til å fungere, fordi det skapte trøbbel. TODO,
                se jastadd-tilleggsfilen "EnumFixup"  */
            AnyOldEnumName y = java.lang.Enum.valueOf( AnyOldEnumName.class, "KAKE" );
         }
     }
 
 }
     
