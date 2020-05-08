 import java.util.StringTokenizer;
 import java.util.ArrayList;
 
 // listings have ids and tags
 public class Listing {
     
     public int id;
     public ArrayList<String> tags;
     
     public Listing() {
         id = 0;
         tags = new ArrayList<String>();
     }
     
     public Listing(String data) {
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
            System.out.println(tagsString);
            StringTokenizer commaTokenizer = new StringTokenizer(tagsString, ",");
            while (commaTokenizer.hasMoreTokens()) {
             
                String tag = commaTokenizer.nextToken();
                this.tags.add(tag);
         
            }
         }
     }
     
     public String toString() {
         String s = id + ": " + tags.toString();
         return s;
     }
     
     public ArrayList<String> getTags() {
         return tags;
     }
 }
