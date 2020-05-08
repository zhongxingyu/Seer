 import java.util.*;
 import java.io.*;
 
 public class indexSearch {
   public static void indSearch() {
     int bid = 0;
     int sid = 0;
     int tid = 0;
     double bids = 9999;
     double sids = 9999;
     double tids = 9999;
     
     for (int i=0; i<Record.numQueries(); i++) {
       timer.startIndTimer();
       int id = Integer.parseInt(Record.getQuery(i));
      List<Entry> allEntries = new ArrayList<Entry>();
       
       Entry entry = SongDatabase.getEntry(id);
       List<Entry> aentry = new ArrayList<Entry>();
       
       for (int j=0; j<entry.user.size(); j++) {
         aentry = SongDatabase.getEntry(entry.user.get(j));
         for (int k=0; k<aentry.size(); k++) {
          if (!allEntries.contains(aentry.get(k)) && aentry.get(k).id != id)
            allEntries.add(aentry.get(k).id);
         }
       }
       
       for (int j=0; j<IDs.size(); j++) {
         double score = -1.0;
        score = linSearch.compareEntry(entry, allEntries.get(j)));
 
         int itemp, itemp1;
         double dtemp, dtemp1;
 
         if (score < bids) {
           dtemp = bids;
           itemp = bid;
           bids = score;
           bid = j;
           if (dtemp < sids) {
             dtemp1 = dtemp;
             itemp1 = itemp;
             sids = dtemp;
             sid = itemp;
             if (dtemp1 < tids) {
               tids = dtemp1;
               tid = itemp1;
             }
           }
         }
         
       }
       System.out.println("The top similarities to " + id + " are " + bid + " " + sid + " " + tid);
       try {
         FileWriter fwrite = new FileWriter("indexedanswers.txt");
         BufferedWriter writer = new BufferedWriter(fwrite);
         writer.write(id + " " + bid + " " + sid + " " + tid);
       } catch (Exception e) {
         e.getMessage();
       }
       
     } //end query for
     timer.stopIndTimer();
   }
 }
