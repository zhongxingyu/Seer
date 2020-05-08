 package java.lang;
 
 public class Object{
     public Object() { }
     public static int test(){
         int i = 10;
         int k = 20;
         char c = (char)1;
         i = i + c;
         short s = (short)1;
         i = i + s;
         int z = i + k;
        return z + k + i + c + s;
     }
 }
