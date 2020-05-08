 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class Tester {
     
     // Instance variables
     public User user;
     public RandomItems randomItems;
 
     // This assigns each tag value a unique integer
     public HashMap<String, Integer> allTagsDict;
 
     public HashMap<Item, Double> itemsSimilarityScore;
     public int n; // number of nearest neighbors
     
     // The found preferences
     public ArrayList<Item> preferences;
     
     public Tester() {
         user = new User();
         randomItems = new RandomItems();
 
         allTagsDict = new HashMap<String, Integer>();
         
         itemsSimilarityScore = new HashMap<Item, Double>();
         n = 20;
     }
     
     public Tester(User user, RandomItems randomItems) {
         this();
         
         this.user = user;
         this.randomItems = randomItems;
         
         // prepare dictionary, update all items appropriately
         this.allTagsDict = this.prepareAllTagsDict();
         for (Item item : user.items) {
             item.setTagsID(allTagsDict);
         }
         for (Item item : randomItems.items) {
             item.setTagsID(allTagsDict);
         }
         
         this.findPreferences();
     }
     
     public HashMap<String,Integer> prepareAllTagsDict() {
         HashMap<String, Integer> temp = new HashMap<String, Integer>();
         
         for (String key : this.user.tagDict.keySet()) {
             temp.put(key, (temp.size() + 1));
         }
         
         for (String key : this.randomItems.tagDict.keySet()) {
             if (!temp.containsKey(key)) {
                 temp.put(key, (temp.size() + 1));
             }
         }
         
         return temp;
     }
     
     // takes a user's item, and compares it to random item's, then updates the user's item appropriately
     public void rankRandomItemsToComparedToUser(Item userItem, ArrayList<Item> randomItems) {
         // compare randomItems to this item
         for (Item random : randomItems) {
             random.similarityScore = Helper.similarity(userItem.getTagsID(), random.getTagsID());
         }
         
         // TDL: maybe make this its own class??
         // sort randomItems by similarityScore from greatest to least
         // I think this gives an xlint warning for some reason
         Comparator<Item> similarityComparator = new Comparator<Item>() {
             // compares by similarity score
             public int compare(Item a, Item b) {
                 if (a.similarityScore < b.similarityScore) {
                     return 1;
                 } 
                 else if (a.similarityScore > b.similarityScore) {
                     return -1;
                 } 
                 return 0;   
             }
         };
         
         // now randomItems are in sorted order
         Collections.sort(randomItems, similarityComparator);
         
         // updated userItem's closestItems hashmap
         HashMap<Item, Integer> similarityDict = userItem.getClosestItems();
         for (int i=0; i<randomItems.size(); i++) {
             userItem.closestItems.put(randomItems.get(i), (i+1));
         }
     }
     
     public void rankUserItemsComparedToRandom(Item randomItem, ArrayList<Item> userItems) {
         for (Item userItem : userItems) {
             if (userItem.closestItems.containsKey(randomItem)) {
                 randomItem.closestItems.put(userItem, userItem.closestItems.get(randomItem));
             }
             else {
                 System.out.println("UH OH COULD NOT FIND CLOSEST ITEMS");
             }
         }
     }
     
     // returns a summation of the absolute value of the correlation for a random item
     public void computePreferenceScore(Item randomItem) {
         double temp = 0;
         for (Item userItem : randomItem.closestItems.keySet()) {
             if (randomItem.closestItems.get(userItem) <= n) {
                 temp = temp + Math.abs(userItem.getSimilarityScore());
             }
         }
         randomItem.preferenceScore = temp;
     }
     
     // algorithm:
     // for each item belonging to random user,
     // find the top 20 most correlated vectors;
     // for each item in randomitem, calculate the score
     // returns a sorted array of items
     public void findPreferences() {
         // for each user's item, find closest random items
         for (Item userItem : user.getItems()) {
             rankRandomItemsToComparedToUser(userItem, randomItems.getItems());
         }
         
         // for each random item, calculate preference score
         for (Item randomItem : randomItems.getItems()) {
             rankUserItemsComparedToRandom(randomItem, user.getItems());
             computePreferenceScore(randomItem);
         }
         
         // sort randomItems by preference from greatest to least
         // I think this gives an xlint warning for some reason
         // TDL: MAKE THIS ITS OWN CLASS???
         Comparator<Item> preferenceComparator = new Comparator<Item>() {
             // compares by similarity score
             public int compare(Item a, Item b) {
                 if (a.preferenceScore < b.preferenceScore) {
                     return 1;
                 } 
                 else if (a.preferenceScore > b.preferenceScore) {
                     return -1;
                 } 
                 return 0;   
             }
         };
         
         // sort the randomItems by randomitems's preference score
         Collections.sort(randomItems.items, preferenceComparator);
         preferences = (ArrayList<Item>)randomItems.items.clone();
         
     }
     
     public void printPreferences() {
         for (Item sortedItem : preferences) {
             System.out.println(sortedItem);
         }
     }
     
     public static void main(String[] args) {
        User testUser = new User("data/MadeUpTrainingData.txt");
        RandomItems testRandomItems = new RandomItems("data/MadeUpTestingData.txt");
        
         Tester test = new Tester(testUser, testRandomItems);
        System.out.println("Dict: " + test.allTagsDict.toString());
         test.printPreferences();
         
     }
 
 }
