 package dupa;
 
 import dupa.utils.DuplicatesList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
 
 /**
  *
  * @author Alexander Glumoff
  */
 public class Dupa {
 
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
     if (args.length > 0) {
       DupFinder finder = new DupFinder();
       finder.addPath(args[0]);
       finder.find();
       //formatOutput(dupList);
     }
   }
 
   public static void formatOutput(DuplicatesList dupList) {
 //    Set set = dupList.getHashMap().entrySet();
 //    Iterator it = set.iterator();
 //    int dupNum = 0;
 //    System.out.println("<duplicates>");
 //    while (it.hasNext()) {
 //      dupNum++;
 //      Map.Entry<String, ArrayList<DupFile>> me = (Map.Entry<String, ArrayList<DupFile>>) it.next();
 //      Iterator it2 = me.getValue().iterator();
 //      System.out.println(dupNum + ". md5: " + me.getKey());
 //      while (it2.hasNext()) {
 //        File f = (File) it2.next();
 //        System.out.println("\t" + f.getAbsolutePath() + " (size: " + f.length() + ")");
 //        //f.setMd5sum(me.getKey());
 //        //list.add(f);
 //      }
 //      System.out.printf("<duplicate id=\"%d\" md5=\"%s\">\n", dupNum, me.getKey());
 //      while (it2.hasNext()) {
 //        DupFile f = (DupFile) it2.next();
 //        //System.out.printf("<file path=\"%s\" size=\"%d\"/>\n", f.getAbsolutePath(), f.length());
 //      }
 //      System.out.println("</duplicate>");
 //    }
 //    System.out.println("</duplicates>");
   }
 }
