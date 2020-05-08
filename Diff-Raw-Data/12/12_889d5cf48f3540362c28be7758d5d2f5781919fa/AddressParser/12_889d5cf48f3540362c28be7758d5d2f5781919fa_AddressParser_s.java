 package dk.itu.kf04.g4tw.model;
 
 import dk.itu.kf04.g4tw.util.DynamicArray;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class AddressParser {
 
     /**
      * Named roads, all road segments with the same name is grouped
      */
     private static HashMap<String, DynamicArray<Road>> namedRoads = null;
 
     private static String sal = "(s|S)(a|A)(l|L)";
     private static String bs = "[a-zæøåüöéA-ZÆØÅÜÖÉ]";
 
     static String[] patterns = new String[] {
             /* Street Name */				"((?<=\\s|^)(?!"+sal+")"+bs+"{3,}\\s?)+",
             /* House Number */				"(?<=\\W|"+bs+"|^)\\d{1,3}(?!\\d|\\.)",
             /* House Letter */				"(?<=\\W\\d{1,3}\\s?)\\s?"+bs+"{1}(\\b?|$)",
             /* Floor */						"(?<=(\\d|"+bs+"|\\W)\\s?)(\\d{1,2}\\s?("+sal+"|\\.\\s?"+sal+")(\\s[fFhHmMtTvV\\.]{2,3})?)",
             /* Postal Code */				"\\d{4}\\s?",
             /* City */						"(?<=\\s|^)(?!"+sal+")("+bs+"{3,}\\s?)+\\s"+bs+"+"};
 
     /**
      *
      * @param address The adress to search for
      * @return return an DynamicArray of Road with all the hits
      */
     public static DynamicArray<Road> getRoad(String address)
     {
         /**
          * index 0 = street name
          * index 1 = street number
          * index 2 = house letter
          * index 3 = house floor
          * index 4 = postal code
          * index 5 = city
          */
         String[] addressInfo = parseAddress(address);
 
         DynamicArray<Road> hits = namedRoads.get(addressInfo[0]);
         if(hits == null)
             hits = new DynamicArray<Road>();
 
         if(!addressInfo[2].equals("") && hits.length() > 1) {
             hits = sortByLetter(hits, addressInfo[2]);
         }
 
         if(!addressInfo[1].equals("")  && hits.length() > 1) {
             hits = sortByNumber(hits, Integer.parseInt(addressInfo[1]));
         }
 
         return hits;
     }
 
     private static DynamicArray<Road> sortByLetter(DynamicArray<Road> arr, String letter)
     {
         DynamicArray<Road> returnArr = new DynamicArray<Road>();
 
         for(int i = 0; i < arr.length(); i++){
             Road r = arr.get(i);
             if(r.getStartLetter() != null) {
                 if(r.getEndLetter() != null) {
                     if(letter.compareTo(r.getStartLetter()) >= 0 && letter.compareTo(r.getEndLetter()) <= 0) {
                         returnArr.add(r);
                     }
                 } else if(letter.compareTo(r.getStartLetter()) >= 0) {
                     returnArr.add(r);
                 }
             } else if(r.getEndLetter() != null && letter.compareTo(r.getEndLetter()) <= 0) {
                 returnArr.add(r);
             }
         }
         return returnArr;
     }
 
     private static DynamicArray<Road> sortByNumber(DynamicArray<Road> arr, int num)
     {
         DynamicArray<Road> returnArr = new DynamicArray<Road>();
 
         for(int i = 0; i < arr.length(); i++){
             Road r = arr.get(i);
             if(r.getStartNumber() > 0) {
                 if(r.getEndNumber() > 0) {
                     if(num >= r.getStartNumber() && num <= r.getEndNumber())
                         returnArr.add(arr.get(i));
                 } else if(num >= r.getStartNumber())
                     returnArr.add(arr.get(i));
             } else if(r.getEndNumber() > 0 && num <= r.getEndNumber())
                 returnArr.add(arr.get(i));
         }
         return returnArr;
     }
 
     private static String[] parseAddress(String s) throws IllegalArgumentException {
         String[] address = new String[6];
         Arrays.fill(address, "");
 
         s = s.replaceAll(",", " ");
         s = s.replaceAll("#", " ");
 
         for(int i = 0; i < patterns.length; i++)
         {
             Pattern p = Pattern.compile(patterns[i]);
             Matcher m = p.matcher(s);
             if(m.find())
             {
                 address[i] = m.group().trim();
                 if(i == 0) s = s.replaceAll(m.group(), " ");
             }
         }
 
         if(address[0].equals("")) {
             throw new IllegalArgumentException();
         }
 
         return address;
     }
 
     public static void setNamedRoads(HashMap<String, DynamicArray<Road>> map)
     {
         namedRoads = map;
         System.out.println("Got the map!");
 
         /*Road r1 = null;
         Road r2 = null;*/
 
        //System.out.println(getRoad("Strøget 65 A").length());
         //System.out.println(getRoad("Bispevej 37 R").length());
 
         /*
         DynamicArray<Road> da1 = getRoad("Bispevej 37 R");
         DynamicArray<Road> da2 = getRoad("Bispevej 37 B");
 
         if(da1.length() == 1)
             r1 = da1.get(0);
 
         if(da2.length() == 1)
             r2 = da2.get(0);
 
         DijkstraEdge[] path = DijkstraSP.shortestPath(r1, r2);
         System.out.println("Got the path. Start printing!");
 
         int prev = r2.getId();
         do
         {
             System.out.println(path[prev] + "-->");
             prev = path[prev].getId();
         } while(path[prev] != null);*/
     }
 }
