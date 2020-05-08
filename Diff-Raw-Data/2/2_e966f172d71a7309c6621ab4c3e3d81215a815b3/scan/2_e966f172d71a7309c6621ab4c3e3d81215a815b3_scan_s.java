 package mosky;
 import java.util.*;
 
 public class scan {
 
     static public String str(String prompt, String pattern, String errmsg)
         throws IllegalStateException
     {
         while(true)
         {
             System.out.print(prompt + " ");
             Scanner scanner = new Scanner(System.in);
             String str = null;
 
             try {
                 if(pattern == null)
                     str = scanner.next();
                 else
                     str = scanner.next(pattern);
             }
             catch(NoSuchElementException e) {
                 // pass
             }
 
             if(str == null)
             {
                 if(errmsg != null)
                     System.out.println(errmsg);
             }
             else
                 return str;
         }
     }
 
     static public String str(String prompt, String pattern)
         throws IllegalStateException
     {
         return str(prompt, pattern, null);
            }
 
     static public String str(String prompt)
         throws IllegalStateException
     {
         return str(prompt, null, null);
     }
 }
