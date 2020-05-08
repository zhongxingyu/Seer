 package java.lang;
 
 public class Object{
     public Object() { }
     public static int test(){
         int [] is = new int[10];
         int sum = 0;
        for(int i = 0; i < is.length; i = i + 1) sum = sum + is[i];
         return sum;
     }
 }
