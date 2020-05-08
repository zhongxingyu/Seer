 import java.io.*;
  
 class AccessFile {
    private String location;
 
     public AccessFile(String input) {
         location = input;
     }
     
     public void show() {
         String thisLine;
         try {
             InputStream is = getClass().getResourceAsStream(location);
             BufferedReader br = new BufferedReader
                 (new InputStreamReader(is));
             while ((thisLine = br.readLine()) != null) {
                 System.out.println(thisLine);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
