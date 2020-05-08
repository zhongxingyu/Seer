 import java.io.*;
 import java.util.*;
 
 public class ExtraInfoParser {
   private DirreqStatsFileHandler dsfh;
   private SortedSet<String> countries;
   private SortedMap<String, String> directories;
   public ExtraInfoParser(DirreqStatsFileHandler dsfh,
       SortedSet<String> countries,
       SortedMap<String, String> directories) {
     this.dsfh = dsfh;
     this.countries = countries;
     this.directories = directories;
   }
   public void parse(String dir, BufferedReader br) throws IOException {
     String line = null, date = null, v3ips = null;
    boolean skip = false;
     while ((line = br.readLine()) != null) {
       if (line.startsWith("dirreq-stats-end ")) {
         date = line.split(" ")[1];
        // trusted had very strange dirreq-v3-shares here...
        skip = dir.equals("8522EB98C91496E80EC238E732594D1509158E77")
            && (date.equals("2009-09-10") || date.equals("2009-09-11"));
       } else if (line.startsWith("dirreq-v3-reqs ")
           && line.length() > "dirreq-v3-reqs ".length()) {
         v3ips = line.split(" ")[1];
       } else if (line.startsWith("dirreq-v3-share ")
          && v3ips != null && !skip) {
         Map<String, String> obs = new HashMap<String, String>();
         String[] parts = v3ips.split(",");
         for (String p : parts) {
           for (String c : this.countries) {
             if (p.startsWith(c)) {
               obs.put(c, p.substring(3));
             }
           }
         }
         String share = line.substring("dirreq-v3-share ".length(),
             line.length() - 1);
         dsfh.addObs(this.directories.get(dir), date, obs, share);
       }
     }
   }
 }
 
