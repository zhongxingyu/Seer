 package Util2415;
 
 import java.util.Hashtable;
 
 /**
  * Code to read from CSV file and reupdate constants
  *
  * @author SamCrane
  */
 public abstract class CSVReader {
 
    //Version 1.3 does not support generics :( :( :(
    //Also, they use a sort of pidgin Hashtable which works slightly differently
     // http://docs.oracle.com/javame/config/cldc/ref-impl/cldc1.1/jsr139/index.html
     
    private static Hashtable<String, Double> resources = new Hashtable<String, Double>();
 
    public static double getValue(String s) {
        return resources.get(s);
     }
 
     public void getFromFile(String s) {
         try {
             Scanner sc = new Scanner(s);
 
             while (sc.hasNext()) {
                resources.put(sc.next(), Double.parseDouble(sc.next()));
             }
             sc.close();
         } catch (Exception ioe) {
             ioe.printStackTrace();
             System.exit(-1);
         }
     }
 }
