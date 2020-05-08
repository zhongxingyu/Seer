 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 // items have ids and tags
 public class Item {
     
     public int id;
     public ArrayList<String> tags;
     public ArrayList<Integer> tagsID;
     public double similarityScore;
     public double preferenceScore;
     public HashMap<Item, Integer> closestItems;
     
     public Item() {
         id = 0;
         tags = new ArrayList<String>();
         tagsID = new ArrayList<Integer>();
         similarityScore = 0;
         preferenceScore = 0;
         closestItems = new HashMap<Item, Integer>();
     }
     
     public Item(String data) {
         this();
         this.process(data);
     }
     
     public void process(String data) {
         // break up categories
         StringTokenizer colonTokenizer = new StringTokenizer(data, ":");
         
        // first token is id
        id = Integer.valueOf(colonTokenizer.nextToken());
        
         // tokenize the tags
         if (colonTokenizer.hasMoreTokens()) {
             String tagsString = colonTokenizer.nextToken();
             //System.out.println(tagsString);
             StringTokenizer commaTokenizer = new StringTokenizer(tagsString, ",");
             while (commaTokenizer.hasMoreTokens()) {
             
                 String tag = commaTokenizer.nextToken();
                 this.tags.add(tag);
         
             }
         }
     }
     
     public String toString() {
         String s = id + ": " + tags.toString() + ": " + tagsID.toString();
         return s;
     }
     
     // ====== GETTERS AND SETTERS ======
     
     public double getSimilarityScore() {
         return similarityScore;
     }
 
     public ArrayList<String> getTags() {
         return tags;
     }
     
     public void setTags(ArrayList<String> tags) {
         this.tags = tags;
     }
     
     public ArrayList<Integer> getTagsID() {
         return tagsID;
     }
     
     public void setTagsID(HashMap<String, Integer> allTagsDict) {
         ArrayList<Integer> temp = new ArrayList<Integer>();
         for (String tag : tags) {
             temp.add(allTagsDict.get(tag));
         }
         tagsID = temp;
     }
     
     public void setTagsID(ArrayList<Integer> tagsID) {
         this.tagsID = tagsID;
     }
     
     public void setID(int id) {
         this.id = id;
     }
     
     public HashMap<Item, Integer> getClosestItems() {
         return closestItems;
     }
 }
