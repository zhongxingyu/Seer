 package java.lang;
 
 public class Object{
     public Object() { }
     
     public String toString() {
         return "Some random object";
     }
 
     public static int test(){
         char [] cs = new char[3];
         cs[0] = 'l';
        cs[1] = 'o';
         cs[2] = 'l';
         String s1 = new String(cs);
         String s2 = new String(cs);
         String s3 = s1 + s2;
         int i = 0;
         
         char [] cs2 = new char[6];
         cs2[0] = 'l';
        cs2[1] = 'o';
         cs2[2] = 'l';
         cs2[3] = 'l';
        cs2[4] = 'o';
         cs2[5] = 'l';
         
         String s4 = new String(cs2);
         
         //using binary bits so it's easy to see what is wrong.
         if(!s3.equals((Object)s4)) i = 1;
         
         cs2 = new char[4];
         cs2[0] = 'l';
        cs2[1] = 'o';
         cs2[2] = 'l';
         cs2[3] = '7';
         
        s4 = new String(cs2);

         s3 = new String(cs) + 7;
         if(!s3.equals((Object)s4)) i = i + 2;
         
         cs2 = new char[5];
        cs2[0] = '7';
         cs2[1] = 'r';
         cs2[2] = 'o';
         cs2[3] = 'f';
         cs2[4] = 'l';
         s4 = new String(cs2);
         
         s3 = 7 + "rofl";
         if(!s3.equals((Object)s4)) i = i + 4;
         
         cs2 = new char[8];
         cs2[0] = 'l';
         cs2[1] = 'o';
         cs2[2] = 'l';
         cs2[3] = '7';
         cs2[4] = 'r';
         cs2[5] = 'o';
         cs2[6] = 'f';
         cs2[7] = 'l';
         s4 = new String(cs2);
         
         s3 = "lol" + 7 + "rofl";
         if(!s3.equals((Object)"lol7rofl")) i = i + 8;
         
         return i;
     }
 }
