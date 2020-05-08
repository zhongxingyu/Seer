 package representation;
 
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 // items have ids and tags
 public class Item {
     
     public int label; // 0 for random, 1 for favorite
     public int id;
     public ArrayList<String> tags;
     public ArrayList<Integer> tagsID;
     public double similarityScore;
     public double preferenceScore;
     public HashMap<Item, Integer> closestItems;
    public HashMap<Item, Double> similarityDict;
     
     
     public Item() {
         label = -1;
         id = 0;
         tags = new ArrayList<String>();
         tagsID = new ArrayList<Integer>();
         similarityScore = 0;
        similarityDict = new HashMap<Item, Double>();
         preferenceScore = 0;
        closestItems = new HashMap<Item, Integer>(6000);
     }
     
     public Item(String data) {
         this();
         this.process(data);
     }
     
     public void process(String data) {
         // break up categories
         StringTokenizer colonTokenizer = new StringTokenizer(data, ":");
         
         /* This is buggy, won't use while loops for now
         int counter = 1;
         while (colonTokenizer.hasMoreTokens()) {
             // first token is label
             if (counter == 1) {
                 String labelString = colonTokenizer.nextToken().trim();
                 label = Integer.valueOf(labelString);
             }
         
             // second token is id
             if (counter == 2) {
                 String idString = colonTokenizer.nextToken();
                 id = Integer.valueOf(idString.trim());
             }
         
             // third token is tags
             if (counter == 3) {
                 String tagsString = colonTokenizer.nextToken().trim();
                 //System.out.println(tagsString);
                 StringTokenizer commaTokenizer = new StringTokenizer(tagsString, ",");
                 while (commaTokenizer.hasMoreTokens()) {
                     String tag = commaTokenizer.nextToken().trim();
                     this.tags.add(tag);
                 }
             }
             if (counter > 3) {
                 System.out.println("SOMETHING WENT WRONG, SHOULD ONLY HAVE 3 TOKENS WHAT");
                 break;
             }
             counter = counter + 1;
         }
         */
         
         // first token is the label
         label = Integer.valueOf(colonTokenizer.nextToken().trim());
         
         // second token is the id
         id = Integer.valueOf(colonTokenizer.nextToken().trim());
 		
         // tokenize the tags
         if (colonTokenizer.hasMoreTokens()) {
             String tagsString = colonTokenizer.nextToken().trim();
             //System.out.println(tagsString);
             StringTokenizer commaTokenizer = new StringTokenizer(tagsString, ",");
             while (commaTokenizer.hasMoreTokens()) {
                 // split out underscores
                 String unprocessed = commaTokenizer.nextToken().toLowerCase();
                 String[] noUnderScores = unprocessed.split("_");
                 String tag = "";
                 for (int i=0; i<noUnderScores.length; i++) {
                     tag = tag + noUnderScores[i] + " ";
                 }
                 // other processing
                 tag = tag.trim().toLowerCase();
                 this.tags.add(tag);
             }
         }
     }
     
     public String toString() {
         String s = label + "," + id; // + tags.toString() + ": " + tagsID.toString();
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
     
     public int getLabel() {
         return label;
     }
 }
