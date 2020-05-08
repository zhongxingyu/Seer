 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class Reader
 {
     public static PCList read()
     {
         PCList toReturn = new PCList();
 
         BufferedReader inStream = new BufferedReader(new InputStreamReader(System.in));
         try {
             String temp;
             while ((temp = inStream.readLine()) != null)
             {
                 toReturn.add(new PCList(temp));
             }
         }
         catch(IOException e) {
             e.printStackTrace();
         }
 
         return toReturn;
     }
 
     public static PCList read(PCList listOfChars)
     {
        return null;
     }
 }
