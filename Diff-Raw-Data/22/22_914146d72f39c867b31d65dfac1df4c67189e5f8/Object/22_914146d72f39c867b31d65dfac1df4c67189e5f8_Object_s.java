 package java.lang;
 
 public class Object{
     public Object() { }
     
     public String toString() {
         return "Some random object";
     }
 
     public static int test(){
         String s1 = "lol";
         String s2 = "lol";
         String s3 = "nope";
         int i = 0;
         
         //using binary bits so it's easy to see what is wrong.
         if(!s1.equals((Object)s2)) i = 1;
         
         if(s1.equals((Object)s3)) i = i + 2;
         
         return i;
     }
 }
