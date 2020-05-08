 package com.scorehero.pathing.fullband;
 import java.util.TreeMap;
 import java.util.TreeMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Collection;
 import com.sleepycat.bind.tuple.IntegerBinding;
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.OperationStatus;
 import com.sleepycat.je.StatsConfig;
 
 public class StandardScoredBeat extends ScoredBeat {
     protected TreeMap< Integer, Integer> scoredBandStateMap;
 
     public StandardScoredBeat() {
         super();
         this.scoredBandStateMap = new TreeMap< Integer, Integer >( );
     }
 
     public void addScore(BandState bandState, int score) {
         Integer key = new Integer(bandState.serializedData());
         Integer oldScoreObj = this.scoredBandStateMap.get(key);
         int oldScore = (null == oldScoreObj) ? 0 : oldScoreObj.intValue();
 
         if ((score > oldScore) || (0 == score)) {
             this.scoredBandStateMap.put(key, new Integer(score));
             assert(this.scoredBandStateMap.size() > 0);
         }
     }
 
     public int getScore(BandState bandState) {
 
         Integer scoredBandState = scoredBandStateMap.get(new Integer(bandState.serializedData()));
         if (null == scoredBandState) {
             System.out.println("ruh-roh! couldn't find score!");
             System.out.println(bandState);
             // ruh-roh! This should never happen.
         }
         
         return scoredBandState.intValue();
     }
 
     public void flush(String title, int beatNumber) throws Exception {
         Database database = BDBScoredBeat.getDB(title, beatNumber, false);
         DatabaseEntry key = new DatabaseEntry();
         DatabaseEntry value = new DatabaseEntry();
         Set< Map.Entry< Integer, Integer > > entrySet = this.scoredBandStateMap.entrySet();
         System.out.println("done building entrySet");
         for (Map.Entry< Integer, Integer > entry : entrySet) {
             IntegerBinding.intToEntry(entry.getKey().intValue(), key);
             IntegerBinding.intToEntry(entry.getValue().intValue(), value);
             database.put(null, key, value);
         }
         Environment env = database.getEnvironment();
         /*
         StatsConfig config = new StatsConfig();
         config.setClear(true);
         System.out.println(env.getStats(config));
         */
         database.close();
         env.close();
     }
 
     public void close() {
     }
 
 }
