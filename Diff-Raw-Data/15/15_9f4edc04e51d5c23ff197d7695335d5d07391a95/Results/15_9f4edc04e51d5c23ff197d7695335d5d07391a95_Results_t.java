 package sim;
 
 import java.util.Arrays;
 import desmoj.core.simulator.TimeSpan;
 
 /**
  * A singleton collecting Biathlon results.
  */
 public class Results {
     private static Results instance = null;
 
     private Logger logger;
 
     /**
      * Results array indexed by the internal competitor ID.
      */
     private Result[] results;
 
     /**
      * A simple class representing results of a biathlete.
      */
     private static class Result implements Comparable {
         String name = "John Doe";
         int misses = 0;
         long finishTime = 0;
 
         public int compareTo(Object that) throws java.lang.ClassCastException {
             // Castin' like there's no tomorrow.
 
             Result t = (Result) that;
             return (int) (this.finishTime - t.finishTime);
         }
     }
 
 
     private Results() {
         results = new Result[Biathlon.NUM_COMPETITORS];
 
         for(int i = 0; i < Biathlon.NUM_COMPETITORS; i++)
           results[i] = new Result();
     }
 
     /**
      * Returns the single instance of this class.
      */
     public static Results getInstance() {
         if(instance == null) {
             instance = new Results();
         }
 
         return instance;
     }
 
     /**
      * Registers a competitor for result collecting.
      * @param id The internal ID of a competitor.
      * @param name The name of the competitor.
      */
     public void registerCompetitor(int id, String name) {
         results[id].name = name;
     }
 
     /**
      * Registers and accumulates misses of a competitor.
      * @param id Internal ID of a competitor.
      * @param misses The number of misses in a single shooting session.
      */
     public void registerMisses(int id, int misses) {
         results[id].misses += misses;
     }
 
     /**
      * Registers the finish time of a competitor.
      * @param id Internal competitor ID.
      */
     public void registerFinish(int id) {
         results[id].finishTime = Biathlon.getInstance().presentTime().getTimeTruncated();
     }
 
     /**
      * Dumps the sorted (by <code>finishTime</code>) results into a file.
      * @param filename The log file storing the results.
      */
     public void dumpResults(String filename) {
         if(logger == null) logger = new Logger(filename);
 
         Arrays.sort(results);
         long firstTime = results[0].finishTime;
 
         for(Result r : results) {
             long delta = r.finishTime - firstTime;
 
             long min = delta / 60;
             long sec = delta % 60;
 
             // Do some pretty formatting...
             String d;
 
            if(min == 0 && sec == 0) d = "---";
             else if(min == 0)        d = String.format("+%d", sec);
             else                     d = String.format("+%d:%02d", min, sec);
 
             logger.log(r.finishTime,
                        String.format("%s \t %d misses \t %s", d, r.misses, r.name));
         }
     }
 }
