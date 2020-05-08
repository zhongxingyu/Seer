 package jauk;
 
 public final class Version
     extends Object
 {
     public final static String Name = "jauk";
     public final static int Major   =  0;
     public final static int Minor   =  5;
    public final static int Build   =  2;
 
 
     public final static String Number = String.valueOf(Major)+'.'+String.valueOf(Minor);
 
     public final static String Full = Name+'-'+Number;
 
     private Version(){
         super();
     }
 }
