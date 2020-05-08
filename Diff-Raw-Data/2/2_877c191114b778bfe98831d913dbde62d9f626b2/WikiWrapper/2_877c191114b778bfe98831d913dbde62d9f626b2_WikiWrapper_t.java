 package modules.HTMLparsing;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Jeremy, Manushi, Albert
  */
 public class WikiWrapper 
 {
     public static void main(String[] args)
     {
         WikiWrapper wrapper = new WikiWrapper();
         ArrayList<String> words = new ArrayList();
         HashMap<String,Integer> containedWords;
         
         /* example implementation of wiki wrapper */
         String pageTitle = "computer science";
         System.out.println("Does wikipedia have a \"" + pageTitle +"\" page?: " + wrapper.hasWikiPage(pageTitle));
         words.add("hash table");
         words.add("data structures");
         words.add("algorithms");
         containedWords = wrapper.containWordsGivenPage(words, pageTitle);
         for(int i = 0; i < words.size(); i ++)
         {
             System.out.println("Does the " + pageTitle + " wikipedia page contain: \"" + words.get(i) + "\"?  :" + containedWords.containsKey(words.get(i)));
         }
         
         
         /* example 2 */
         System.out.println("");
         pageTitle = "biology";
         words = new ArrayList();
         words.add("organisms");
         words.add("cell theory");
         words.add("Anatomy");
         words.add("scientific model");
         words.add("natural selections");
         words.add("natural selection");
         System.out.println("Does wikipedia have a \"" + pageTitle +"\" page?: " + wrapper.hasWikiPage(pageTitle));
         containedWords = wrapper.containWordsGivenPage(words, pageTitle);
         for(int i = 0; i < words.size(); i ++)
         {
             System.out.println("Does the " + pageTitle + " wikipedia page contain: \"" + words.get(i) + "\"?  :" + containedWords.containsKey(words.get(i)));
         }
         
         /* example 3 */
         System.out.println("");
         pageTitle = "bike wars";
         System.out.println("Does wikipedia have a \"" + pageTitle +"\" page?: " + wrapper.hasWikiPage(pageTitle));
     }
     public WikiWrapper()
     {
        
     }
     /* returns true for only exact string matches (case insensitive) */
     public boolean hasWikiPage(String search)
     {
         String search_formatted = search.replace(' ', '+');
         HashMap<String,Integer> wikiHits =  new HashMap();
         try { 
             URL wikipedia = new URL("http://en.wikipedia.org/w/api.php?action=opensearch&search=" + search_formatted); 
             BufferedReader in = new BufferedReader(new InputStreamReader(wikipedia.openStream())); 
             String inputLine; 
             while ((inputLine = in.readLine()) != null) { 
                 String delim1 = "[,]";
                 String delim2 = "[\"]";
                 String[] tokens = inputLine.split(delim1);
                 for(int i = 1; i < tokens.length; i++)
                 {
                     if(tokens[i].equals("[]]"))
                         return false;
                     
                     tokens[i] = tokens[i].replace('[', ' ');
                     tokens[i] = tokens[i].replace(']', ' '); 
                     //System.out.println(i + ": "+ tokens[i] + "");
                     String[] token2 = tokens[i].split(delim2);
                     //System.out.println("|"+token2[1].toLowerCase()+"|");
                    if(token2.length < 2)
                        continue;
                     wikiHits.put(token2[1].toLowerCase(), 1);
                 }
                 // print line from URL
                 //System.out.println(inputLine); 
             } 
             in.close(); 
 
         } catch (MalformedURLException me) { 
             System.out.println(me); 
 
         } catch (IOException ioe) { 
             System.out.println(ioe); 
         }
         //System.out.println("|"+search.toLowerCase()+"|");
         if(wikiHits.containsKey(search.toLowerCase()))
             return true;
         else
             return false;
     }
     
     /* pass in an array list of words to check in wiki page, if a specific page does
      * not exist it will return null. It will return a hashMap of words found in the page
      */
     public HashMap<String,Integer> containWordsGivenPage(ArrayList<String> words, String pageTitle)
     {
         if(words == null)
             return null;
         HashMap<String, Integer> contains = new HashMap();
         
         /* ensure that pageTitle exists */
         if(!hasWikiPage(pageTitle))
         {
             System.err.println("Page title does not exist");
             return null;
         }
         
         int wordsFound = 0;
         String pageTitle_Formatted = pageTitle.replace(' ', '_');
         try { 
             URL wikipedia = new URL("http://en.wikipedia.org/wiki/" + pageTitle_Formatted); 
             BufferedReader in = new BufferedReader(new InputStreamReader(wikipedia.openStream())); 
             String inputLine; 
             while ((inputLine = in.readLine()) != null) {       
                 if(wordsFound == words.size())
                     break;
                 for(int i = 0; i < words.size(); i++)
                 {
                     if(contains.containsKey(words.get(i)))
                         continue;
                     
                     if(inputLine.toLowerCase().contains(words.get(i).toLowerCase()))
                     {
                         contains.put(words.get(i), 1);
                         //System.out.println("contains: " + words.get(i));
                         wordsFound++;
                     }
                 }
                 //System.out.println(inputLine); 
             } 
             in.close(); 
 
         } catch (MalformedURLException me) { 
             System.out.println(me); 
 
         } catch (IOException ioe) { 
             System.out.println(ioe); 
         }
         return contains;
     }
     public boolean AsubB(String moduleA, String moduleB) 
     {
         ArrayList<String> words = new ArrayList();
         HashMap<String,Integer> containedWords;
         words.add(moduleA);
         containedWords = containWordsGivenPage(words, moduleB);
         if(containedWords == null || containedWords.isEmpty())
             return false;
         else
             return true;
     }
 }
