 
 public class Proxy
 {
 
     private static int engaged = 0;
 
     private static native void newobj_(Object thread, Object o);
     public static void newobj(Object o)
     {
         if( engaged != 0 )
         {
             newobj_(Thread.currentThread(), o);
         }
     }
 
     private static native void newarr_(Object thread, Object o);
     public static void newarr(Object o)
     {
         if( engaged != 0 )
         {
             newarr_(Thread.currentThread(), o);
         }
     }
 
    private static native void method_entry_(Object thread, Object o);
    public static void method_entry(Object o)
     {
         if( engaged != 0 )
         {
            method_entry_(Thread.currentThread(), o);
         }
     }
 
    private static native void method_exit_(Object thread, Object o);
    public static void method_exit(Object o)
     {
         if( engaged != 0 )
         {
            method_exit_(Thread.currentThread(), o);
         }
     }
 }
