 import java.util.*;
 import java.io.*;
 
 public class indexSearch {
   //private static ArrayList<Integer> IDs = new ArrayList<Integer>();
   //private static ArrayList<Double> Scores = new ArrayList<Double>();
   
   public static void indSearch() {
     try {
       FileWriter fwrite = new FileWriter("indexedanswers.txt");
       BufferedWriter writer = new BufferedWriter(fwrite);
     
     
       for (int i=0; i<Record.numQuery(); i++) {
         timer.startIndTimer();
         int id = Integer.parseInt(Record.getQuery(i));
         ArrayList<Entry> allEntries = new ArrayList<Entry>();
         
         Entry entry = SongDatabase.getEntry(id);
         ArrayList<Entry> aentry = new ArrayList<Entry>();
         
         for (int j=0; j<entry.user.size(); j++) {
           aentry = SongDatabase.getEntry(entry.user.get(j));
          System.out.println(aentry.get(j).songID);
           for (int k=0; k<aentry.size(); k++) {
            boolean contain = false;
            if (aentry.get(k).songID != id) {
              contain = false;
              for (int m=0; m<allEntries.size(); m++) {
                for (int n=0; n<aentry.size(); n++) {
                  if (allEntries.get(m).songID == aentry.get(n).songID) {
                    contain = true;
                  }
                }
              }
              if (!contain)
                allEntries.add(aentry.get(k));
            }
           }
         }
         
         for (int j=0; j<allEntries.size(); j++) {
           double score = -1.0;
          //System.out.println(allEntries.get(j).songID);
           score = linSearch.compareEntry(entry, allEntries.get(j));
          System.out.println(score);
           linSearch.IDs.add(allEntries.get(j).songID);
           linSearch.Scores.add(new Double(score));
         }
         
         linSearch.getBest(id, writer);
         timer.stopIndTimer();
       } //end query for
       writer.close();
     } catch (Exception e) {
       e.getMessage();
     }
   }
 }
