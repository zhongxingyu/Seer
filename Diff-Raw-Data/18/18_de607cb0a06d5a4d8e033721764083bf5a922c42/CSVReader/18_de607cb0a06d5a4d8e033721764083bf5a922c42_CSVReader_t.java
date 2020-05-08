 package Util2415;
 
 import java.util.Hashtable;
import java.lang.Double;
 
 /**
  * Code to read from CSV file and reupdate constants
  *
  * @author SamCrane
  */
 public abstract class CSVReader {
 
    // They use a sort of pidgin Hashtable which works slightly differently
     // http://docs.oracle.com/javame/config/cldc/ref-impl/cldc1.1/jsr139/index.html
     
    private static Hashtable resources = new Hashtable();
 
    public static Double getValue(String s) {
        //Getting errors on this line
        // "inconvertible types: required: double. Found: Object"
        return (Double)resources.get(s); 
     }
 
     public void getFromFile(String s) {
         try {
             Scanner sc = new Scanner(s);
 
             while (sc.hasNext()) {
                resources.put(sc.next(), new Double (Double.parseDouble(sc.next())));
             }
             sc.close();
         } catch (Exception ioe) {
             ioe.printStackTrace();
             System.exit(-1);
         }
     }
 }
