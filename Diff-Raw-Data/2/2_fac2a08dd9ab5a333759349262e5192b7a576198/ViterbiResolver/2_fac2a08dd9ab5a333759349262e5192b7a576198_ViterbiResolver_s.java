 package com.personalityextractor.entity.resolver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import com.personalityextractor.data.source.Wikiminer;
 import com.personalityextractor.entity.Entity;
 import com.personalityextractor.entity.WikipediaEntity;
 
 
 import cs224n.util.CounterMap;
 
 public class ViterbiResolver extends BaseEntityResolver {
 
     public ViterbiResolver() {
     }
 
     /*
      * get wikimimer compare() scores between all entities
      */
     private CounterMap<String, String> populateCompareScores(
             List<String> twEntities,
             HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities) {
         CounterMap<String, String> probabilites = new CounterMap<String, String>();
         for(int i=0;i<tweetEntityTowikiEntities.get(twEntities.get(1)).size();i++){
             probabilites.setCount("-1", tweetEntityTowikiEntities.get(twEntities.get(1)).get(i).getWikiminerID(), 0.0000001);
             probabilites.setCount(tweetEntityTowikiEntities.get(twEntities.get(1)).get(i).getWikiminerID(), "-1", 0.0000001);
 
         }
         for (int i = 1; i < twEntities.size(); i++) {
             String twEntity = twEntities.get(i);
             ArrayList<WikipediaEntity> wikiEntities = tweetEntityTowikiEntities
                     .get(twEntity);
 
             for (int j = 0; j < wikiEntities.size(); j++) {
                 // iterate over ALL wikiEntities and get compare score for
                 // wikiEntities[i]
                 for (int k = i + 1; k < twEntities.size(); k++) {
                     ArrayList<WikipediaEntity> wEntities = tweetEntityTowikiEntities
                             .get(twEntities.get(k));
                     for (WikipediaEntity wEntity : wEntities) {
                         if (wEntity.getText().equalsIgnoreCase("void_node") || wEntity.getText().equalsIgnoreCase("start_node") ||wEntity.getText().equalsIgnoreCase("end_node")) {
                             probabilites.setCount(wikiEntities.get(j)
                                     .getWikiminerID(),
                                     wEntity.getWikiminerID(), 0.0000001);
                             probabilites.setCount(wEntity.getWikiminerID(),
                                     wikiEntities.get(j).getWikiminerID(),
                                     0.0000001);
                             continue;
                         }
                         probabilites.setCount(wikiEntities.get(j)
                                 .getWikiminerID(), wEntity.getWikiminerID(),
                                 Wikiminer.compare(wikiEntities.get(j)
                                         .getWikiminerID(), wEntity
                                         .getWikiminerID()));
                         probabilites.setCount(wEntity.getWikiminerID(),
                                 wikiEntities.get(j).getWikiminerID(), Wikiminer
                                         .compare(wikiEntities.get(j)
                                                 .getWikiminerID(), wEntity
                                                 .getWikiminerID()));
 
                     }
                 }
 
             }
         }
         return probabilites;
 
     }
 
     private HashMap<String, String> buildwikiIDToTweetEntityMap(
             HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities) {
         // assuming that wikipedia ids are unique
         HashMap<String, String> wikiIDToTweetEntity = new HashMap<String, String>();
         Object[] objArray = tweetEntityTowikiEntities.keySet().toArray();
         List<String> twEntities = Arrays.asList(Arrays.copyOf(objArray,
                 objArray.length, String[].class));
         for (int i = 0; i < twEntities.size(); i++) {
             ArrayList<WikipediaEntity> wikiEntities = tweetEntityTowikiEntities
                     .get(twEntities.get(i));
             for (WikipediaEntity we : wikiEntities) {
                 wikiIDToTweetEntity.put(we.getWikiminerID(), twEntities.get(i));
             }
         }
         return wikiIDToTweetEntity;
     }
 
     private HashMap<String, ArrayList<WikipediaEntity>> getWikiSenses(List<String> entities) {
        
         HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities = new HashMap<String, ArrayList<WikipediaEntity>>();
         //start node
         ArrayList<WikipediaEntity> start = new ArrayList<WikipediaEntity>();
         start.add(new WikipediaEntity("start_node", "-1", "0.0000001"));
         tweetEntityTowikiEntities.put("start_node", start);
        
         //end node
         ArrayList<WikipediaEntity> end = new ArrayList<WikipediaEntity>();
         end.add(new WikipediaEntity("end_node", "-2", "0.0000001"));
         tweetEntityTowikiEntities.put("end_node", end);
        
        
         for (String entity : entities) {
             List<WikipediaEntity> wikiEntities = new ArrayList<WikipediaEntity>();
             String xml = Wikiminer.getXML(entity, false);
             if (xml == null)
                 continue;
             ArrayList<String[]> weentities = Wikiminer.getWikipediaSenses(xml,
                     true);
             if (weentities.size() == 0)
                 continue;
             ArrayList<WikipediaEntity> ids = new ArrayList<WikipediaEntity>();
             for (String[] arr : weentities) {
                 WikipediaEntity we = new WikipediaEntity(arr[0], arr[1], arr[2]);
                 ids.add(we);
                 wikiEntities.add(we);
             }
             // adding a void entity
             WikipediaEntity we = new WikipediaEntity("void_node", "0", "0.0000001");
             ids.add(we);
             tweetEntityTowikiEntities.put(entity, ids);
         }
 
         return tweetEntityTowikiEntities;
     }
 
     private static List<String> swap(List<String> l, int p) {
         int x = p;
         int y = p + 1;
         if (p == l.size() - 1) {
             y = 0;
         }
 
         List<String> sList = new ArrayList<String>();
         for (int i = 0; i < l.size(); i++) {
             if (i == x) {
                 sList.add(l.get(y));
             } else if (i == y) {
                 sList.add(l.get(x));
             } else {
                 sList.add(l.get(i));
             }
         }
 
         return sList;
     }
 
     public List<WikipediaEntity> resolve(List<String> entities) {
        ArrayList<Entity> entityList = new ArrayList<Entity>();
         double bestProbability = (-1)*Integer.MAX_VALUE;
         String bestPath = "";
         List<String> bestSequence=null;
 
         // find potential wiki entities for each entity
         HashMap<String, ArrayList<WikipediaEntity>> tweetEntityTowikiEntities = getWikiSenses(entities);
 
         // remove entities which have no wikipedia senses
         Object[] objArray = tweetEntityTowikiEntities.keySet().toArray();
         List<String> twEntities = Arrays.asList(Arrays.copyOf(objArray,
                 objArray.length, String[].class));
         for (int i = 0; i < entities.size(); i++) {
             if (!twEntities.contains(entities.get(i))) {
                 entities.remove(i);
                 i--;
             }
         }
 
         twEntities = entities;
 
         // try all permutations of entities
         for (int x = 0; x < entities.size(); x++) {
 ////        for (int x = 0; x < 1; x++) {
             for (int z = 0; z < entities.size() - 1; z++) {
 ////            for (int z = 0; z < 1; z++)
                 twEntities = swap(twEntities, z);
 
                 //add start and end nodes
                 twEntities.add(0, "start_node");
                 twEntities.add(twEntities.size(), "end_node");
 
                 // pre-calculate all compare scores between wikipedia entities.
                 CounterMap<String, String> probabilites = populateCompareScores(twEntities,
                                 tweetEntityTowikiEntities);
 
                 // declare the dp matrix and initialize it for the first state
                 HashMap<String, String[]> prev_BestPaths = new HashMap<String, String[]>();
                 ArrayList<WikipediaEntity> first_entities = tweetEntityTowikiEntities.get(twEntities.get(0));
 
                 for (WikipediaEntity we : first_entities) {
                     prev_BestPaths.put(we.getWikiminerID(), new String[] {
                             we.getCommonness(), we.getWikiminerID(), we.getCommonness() });
                 }
 
                 // System.out.println("Start viterbi");
                 // not worrying about the order of entities for now
                 for (int i = 1; i < twEntities.size(); i++) {
                     if(i==twEntities.size()-2){
                         System.out.println("");
                     }
 
                     for (String key : prev_BestPaths.keySet()) {
                         // System.out.println(Arrays.asList(prev_BestPaths.get(key)));
                     }
 
                     HashMap<String, String[]> next_BestPaths = new HashMap<String, String[]>();
                     ArrayList<WikipediaEntity> next_WikiSenses = tweetEntityTowikiEntities.get(twEntities.get(i));
 
                     for (int j = 0; j < next_WikiSenses.size(); j++) {
                         double total = 0;
                         String maxpath = "";
                         double maxprob = (-1) * Integer.MAX_VALUE;
 
                         double prob = 1;
                         String v_path = "";
                         double v_prob = 1;
                         ArrayList<WikipediaEntity> previous_WikiSenses = tweetEntityTowikiEntities.get(twEntities.get(i - 1));
 
                         for (int k = 0; k < previous_WikiSenses.size(); k++) {
                             String[] objs = prev_BestPaths
                                     .get(previous_WikiSenses.get(k)
                                             .getWikiminerID());
                             prob = Double.parseDouble(objs[0]);
                             v_path = (String) objs[1];
                             v_prob = Double.parseDouble(objs[2]);
                             double count = probabilites
                                     .getCount(previous_WikiSenses.get(k)
                                             .getWikiminerID(), next_WikiSenses
                                             .get(j).getWikiminerID());
                             // System.out.println("Comparing "
                             // + previous_WikiSenses.get(k).getWikiminerID()
                             // + ", "
                             // + next_WikiSenses.get(j).getWikiminerID()
                             // + " : " + count);
 
                             double compareScore;
                             if (count == 0.0) {
                                 compareScore = 0.0;
                             } else {
                                 compareScore = Math.log(count);
                             }
                            
                             prob += (compareScore + Double.valueOf(previous_WikiSenses.get(k).getCommonness()));
                             v_prob += (compareScore + Double.valueOf(previous_WikiSenses.get(k).getCommonness()));
 
                             total += Math.exp(prob);
                             double check = Math.log(total);
                             if (v_prob > maxprob) {
                                 maxprob = v_prob;
                                 maxpath = v_path
                                         + ","
                                         + next_WikiSenses.get(j)
                                                 .getWikiminerID();
                             }
                         }
                         next_BestPaths.put(next_WikiSenses.get(j)
                                 .getWikiminerID(),
                                 new String[] { String.valueOf(Math.log(total)),
                                         maxpath, String.valueOf(maxprob) });
                     }
                     prev_BestPaths = next_BestPaths;
                 }
 
                 for (String key : prev_BestPaths.keySet()) {
                     // System.out.println("Entity: " + key);
                     // System.out.println(Arrays.asList(prev_BestPaths.get(key)));
                 }
 
                 double total = 0;
                 String maxpath = "";
                 double maxprob = (-1) * Integer.MAX_VALUE;
 
                 double prob = 1;
                 String v_path = "";
                 double v_prob = 1;
 
                 for (String s : prev_BestPaths.keySet()) {
                     String[] info = prev_BestPaths.get(s);
                     prob = Double.parseDouble(info[0]);
                     v_path = info[1];
                     v_prob = Double.parseDouble(info[2]);
                     total += Math.exp(prob);
                     if (v_prob > maxprob) {
                         maxpath = v_path;
                         maxprob = v_prob;
                     }
                 }
                 if(maxprob > bestProbability){
                     bestPath = maxpath;
                     bestProbability = maxprob;
                     bestSequence = new ArrayList<String>(twEntities);
                 }
                
                 System.out.println("Entities : " + twEntities);
                 System.out.println("MaxPath: " + maxpath + "\tMaxProb: "
                         + maxprob + "\n");
 
                                
                 twEntities.remove(0);
                 twEntities.remove(twEntities.size()-1);
             }
         }
 
         System.out.println("BestPath: " + bestPath + "\tBestProb: "
                 + bestProbability + "\n");
 
         String[] ids = bestPath.split(",");
         for(int l=0; l<ids.length; l++) {
             entityList.add(new WikipediaEntity(bestSequence.get(l), ids[l]));
         }
 
         return entityList;
     }
 
     public static void main(String args[]) {
         ViterbiResolver vr = new ViterbiResolver();
         ArrayList<String> entities = new ArrayList<String>();
         entities.add("apple");
         entities.add("iphone");
         entities.add("jobs");
 
         vr.resolve(entities);
 
     }
 
 }
