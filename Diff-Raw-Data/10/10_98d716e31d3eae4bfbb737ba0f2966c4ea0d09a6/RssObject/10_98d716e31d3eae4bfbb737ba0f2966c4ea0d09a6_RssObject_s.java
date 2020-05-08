 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package rssscout;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author JC Denton
  */
 public class RssObject {
 
     String url = "";
     String chans = "";
     String color = "";
     String searchTerms = "";
     ArrayList<String> searchTermsList = new ArrayList();
 
     public RssObject(String configLine) {
 
 
         //configLine is: http://kalamazoo.craigslist.org/tag/index.rss; #cl-scout,#rss-scout; BLUE; 360, broken, broken 360
         processLine(configLine);
         processSearchTerms(searchTerms);
         printSearchList();
 
 
 
 
     }
 
     private void printSearchList() {
         for (int i = 0; i < searchTermsList.size(); i++) {
             System.out.println(searchTermsList.get(i));
         }
     }
 
     private  void processSearchTerms(String sTerms) {
         Scanner scanner = new Scanner(sTerms);
         scanner.useDelimiter(",");
         while (scanner.hasNext()) {
             searchTermsList.add(scanner.next());
         }
     }
 
     private void processLine(String aLine) {
         //use a second Scanner to parse the content of each line 
 
         Scanner scanner = new Scanner(aLine);
         scanner.useDelimiter(";");
         try{
         if (scanner.hasNext()) {
             url = scanner.next();
             chans = scanner.next();
             color = scanner.next();
             searchTerms = scanner.next();
             System.out.println("Name: " + url + " chans: " + chans + " color: " + color + " searchTerms: " + searchTerms);
             System.out.println();
             //no need to call scanner.close(), since the source is a String
         }
         }catch(Exception e)
         {
         System.out.println("Please check rssLinks.txt, proper config is: rss url;channel(s);color;search term(s)");
          System.exit(0);
         }
     }
 }
