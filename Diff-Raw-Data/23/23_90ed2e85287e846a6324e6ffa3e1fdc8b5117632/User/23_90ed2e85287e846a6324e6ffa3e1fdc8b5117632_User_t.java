 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 // Users are a list of listings
 public class User {
     
     public ArrayList<Listing> listings;
     public HashMap<String, Integer> tagDict;
     public HashMap<Integer, Integer> idDict;
     
     public User() {
         listings = new ArrayList<Listing>();
         tagDict = new HashMap<String, Integer>();
         idDict = new HashMap<Integer, Integer>();
     }
     
     public User(String filePath) {
         this();
         this.process(filePath);
     }
     
     // method takes the name of a text file formated as ListingID:tag,tag,tag...
     public void process(String filePath) {
         try {
             BufferedReader reader = new BufferedReader(new FileReader(filePath));    
             // read in every line
             String thisLine;
             while( (thisLine = reader.readLine()) != null) {
                 // make a new listing
                 Listing tempListing = new Listing(thisLine);
                 
                 // put this listing in the arrayList if it has not been parsed yet
                 if (!idDict.containsKey(tempListing.id)) {
                     idDict.put(tempListing.id, tempListing.id);
                     listings.add(tempListing);  
                 
                     // get tag counts from the listing
                     for (String tag : tempListing.getTags()) {
                         int count = 1;
                         if (tagDict.containsKey(tag)) {
                             count = count + tagDict.get(tag);
                         }
                         tagDict.put(tag, count);
                     }
                 }
                 else {
                     System.out.println("Repeat listing: " + tempListing.id);
                 }
             }
             reader.close();
         }
         catch (IOException e){
             
         }
     }
     
     public String toString() {
         return tagDict.toString();
     }
     
     // parse through data, record 
     public static void main(String args[]) {
        User me = new User("take3.txt");
         System.out.println("User: " + me);
         System.out.println("Num Listings: " + me.listings.size());
     }
 }
