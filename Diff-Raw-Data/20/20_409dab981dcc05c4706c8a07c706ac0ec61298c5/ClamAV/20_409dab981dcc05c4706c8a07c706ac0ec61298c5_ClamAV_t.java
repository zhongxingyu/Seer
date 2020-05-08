 package cirrus.server;
 
 import cirrus.server.AntiVirus;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
import java.util.Scanner;

 public class ClamAV implements AntiVirus
 {
    private static final String clamCmd = "clamdscan";
 
     private Runtime rt;
 
     public ClamAV()
     {
         rt = Runtime.getRuntime();
     }
 
     public boolean scan(String filename)
     {
         boolean infected = false;
         try
         {
             String cmd = clamCmd + " " + filename;
            Process p = rt.exec(cmd);
             BufferedReader buff = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line = buff.readLine();
            Scanner l = new Scanner(line);
            l.next();
            if (!l.next().equals("OK"))
                infected = true;
             while (line != null)
             {
                 System.err.println(line);
                 line = buff.readLine();
             }
         } catch (Exception e)
         {
             System.out.println("Error!");
         }
         return infected;
     }
 }
