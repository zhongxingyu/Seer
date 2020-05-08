 /* Copyright 2013 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package org.kiji.wibidota;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.TreeMap;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  * A class that represents a job to output all possible 4 person teams, and their success rate with
  * all possible fifth picks.
  */
 public class FifthPick extends Configured implements Tool {
   private static final Logger LOG = LoggerFactory.getLogger(FifthPick.class);
 
   /** Flag on the player_slot field that indicates they were on the Dire team. */
   private static final int DIRE_MASK = 128;
 
   /**
    * Some useful counters to keep track of while processing match history.
    */
   static enum FifthPickCounters {
     MALFORMED_MATCH_LINES,
     NON_MATCHMAKING_MATCHES,
     INVALID_MODE_GAMES, // Currently game_mode seems to always be 0, making this useless.
     ABANDONED_GAMES,
     COOP_BOT_GAMES,
     BOT_GAMES, // Game is 'real' but has bots.
     TOURNAMENT_GAMES,
   }
 
   /** A comparable which combines a hero id and a result */
   public static class FifthResultWritable implements Writable {
     public int mHeroId;
     public int mResult; // 0 for loss, 1 for win.
 
     private FifthResultWritable() { }
 
     public FifthResultWritable(int heroId, int result) {
       mHeroId = heroId;
       mResult = result;
     }
 
     public void write(DataOutput out) throws IOException {
       out.writeInt(mHeroId);
       out.writeInt(mResult);
     }
 
     public void readFields(DataInput in) throws IOException {
       mHeroId = in.readInt();
       mResult = in.readInt();
     }
 
     public static FifthResultWritable read(DataInput in) throws IOException {
       FifthResultWritable w = new FifthResultWritable();
       w.readFields(in);
       return w;
     }
   }
 
   /*
    * A mapper that reads over matches from the input files and outputs the result for each
    * possible 4 man team and the result of the fifth.
    */
   public static class Map extends Mapper<LongWritable, Text, Text, FifthResultWritable> {
 
     private final static JsonParser PARSER = new JsonParser();
     public void map(LongWritable key, Text value, Context context)
         throws IOException, InterruptedException {
       try{
         int[] radiant_heroes = new int[5];
         int[] dire_heroes = new int[5];
         int radiantOutcome;
         int direOutcome;
         JsonObject matchTree = PARSER.parse(value.toString()).getAsJsonObject();
         int lobby_type = matchTree.get("lobby_type").getAsInt();
         // This check ensure that we only consider public matchmaking games.
         if (4 == lobby_type) {
           context.getCounter(FifthPickCounters.COOP_BOT_GAMES).increment(1);
           // Bot games don't count.
           return;
         } else if (2 == lobby_type) {
           // Keep track of tournament games
           context.getCounter(FifthPickCounters.TOURNAMENT_GAMES).increment(1);
         }
         if (matchTree.get("radiant_win").getAsBoolean()) {
           radiantOutcome = 1;
           direOutcome = 0;
         } else {
           radiantOutcome = 0;
           direOutcome = 1;
         }
 
         // Go through the player list and output the appropriate result information.
         for (JsonElement playerElem : matchTree.get("players").getAsJsonArray()) {
           JsonObject player = playerElem.getAsJsonObject();
           // Check for abandoned and bot games.
           if (!player.has("leaver_status")) {
             // Indicates a bot.
             context.getCounter(FifthPickCounters.BOT_GAMES).increment(1);
           } else if (2 == player.get("leaver_status").getAsInt()) {
             // Player abandoned the game. Record it.
             context.getCounter(FifthPickCounters.ABANDONED_GAMES).increment(1);
           }
           // This is where we actually write out victory or defeat.
           int hero_id = player.get("hero_id").getAsInt();
           int player_slot = player.get("player_slot").getAsInt();
           if (player_slot < DIRE_MASK) {
             // Radiant player.
             radiant_heroes[player_slot] = hero_id;
           } else {
             // Dire player
             dire_heroes[player_slot - DIRE_MASK] = hero_id;
           }
         }
         // Sort the arrays .
         Arrays.sort(radiant_heroes);
         Arrays.sort(dire_heroes);
        
         // Now go through the 5 slots and construct keys and outputs.
         for (int skip = 0; skip < 5; skip++) {
             StringBuilder radiant_key_builder = new StringBuilder();
             StringBuilder dire_key_builder = new StringBuilder();
             for (int j = 0; j < 5; j++) {
                 if (j == skip) {
                     continue;
                 }
                 radiant_key_builder.append(Integer.toString(radiant_heroes[j]));
                 dire_key_builder.append(Integer.toString(dire_heroes[j]));
                if (!((j == 5) || ((j == 4) && (skip == 5)))) {
                     radiant_key_builder.append('.');
                     dire_key_builder.append('.');
                 }
             }
             context.write(new Text(radiant_key_builder.toString()),
                     new FifthResultWritable(radiant_heroes[skip], radiantOutcome));
             context.write(new Text(dire_key_builder.toString()),
                     new FifthResultWritable(dire_heroes[skip], direOutcome));
         }
       } catch (IllegalStateException e) {
         // Indicates malformed JSON.
         context.getCounter(FifthPickCounters.MALFORMED_MATCH_LINES).increment(1);
       }
     }
   }
 
   /**
    * A simple reducer that simply outputs the non-anonymous player accounts with their play count.
    */
   public static class Reduce
       extends Reducer<Text, FifthResultWritable, Text, Text> {
     private final static Gson GSON = new GsonBuilder().create();
 
     public void reduce(Text key, Iterable<FifthResultWritable> values, Context context) 
         throws IOException, InterruptedException {
       TreeMap<String, Integer> totalGames = new TreeMap<String, Integer>();
       TreeMap<String, Integer> victories = new TreeMap<String, Integer>();
       for (FifthResultWritable result : values) {
         String heroKey = Integer.toString(result.mHeroId);
         if (!totalGames.containsKey(heroKey)) {
           totalGames.put(heroKey, 0);
         }
         if (!victories.containsKey(heroKey)) {
           victories.put(heroKey, 0);
         }
         totalGames.put(heroKey, totalGames.get(heroKey) + 1);
         if (0 != result.mResult) {
           victories.put(heroKey, victories.get(heroKey) + 1);
         }
       }
       TreeMap<String, TreeMap<String, Integer>> OutputMap =
           new TreeMap<String, TreeMap<String, Integer>>();
       OutputMap.put("totals", totalGames);
       OutputMap.put("victories", victories);
       context.write(key, new Text(GSON.toJson(OutputMap)));
     }
   }
 
   public static void main(String args[]) throws Exception {
     Configuration conf = new Configuration();
     int res = ToolRunner.run(conf, new FifthPick(), args);
     System.exit(res);
   }
 
   public final int run(final String[] args) throws Exception {
     Job job = new Job(super.getConf(), "fifth pick");
     job.setOutputKeyClass(Text.class);
     job.setOutputValueClass(Text.class);
     
     job.setMapperClass(Map.class);
     job.setReducerClass(Reduce.class);
     job.setMapOutputKeyClass(Text.class);
     job.setMapOutputValueClass(FifthResultWritable.class);
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);
 
     FileInputFormat.addInputPath(job, new Path(args[0]));
     FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
     if (job.waitForCompletion(true)) {
       return 0;
     } else {
       return -1;
     }
   }
 }
 
