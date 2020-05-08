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
           for (int k=0; k<aentry.size(); k++) {
            if (!allEntries.contains(aentry.get(k)) && aentry.get(k).songID != id)
            allEntries.add(aentry.get(k));
           }
         }
         
         for (int j=0; j<allEntries.size(); j++) {
           double score = -1.0;
           score = linSearch.compareEntry(entry, allEntries.get(j));
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
