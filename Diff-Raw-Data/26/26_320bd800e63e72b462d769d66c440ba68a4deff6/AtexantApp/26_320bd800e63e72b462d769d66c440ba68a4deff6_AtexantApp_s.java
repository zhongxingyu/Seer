 package ru.tsu.inf.atexant;
 
 import ru.tsu.inf.atexant.storages.*;
 import ru.tsu.inf.atexant.dump.*;
 import ru.tsu.inf.atexant.nlp.*;
 import ru.tsu.inf.atexant.search.*;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.*;
 import ru.tsu.inf.atexant.nlp.sentences.SentenceSemanticSimilarityMeasurer;
 import ru.tsu.inf.atexant.nlp.sentences.SentenceTreeWithWordTokensBuilder;
 import ru.tsu.inf.atexant.nlp.sentences.SentenceTree;
 
 
 public class AtexantApp
 {
     
     public static Properties localProps;
     
     private static class WikipediaPageSaver extends WikipediaPageHandler {
         private WikipediaPageStorage storage = null;
         
         public WikipediaPageSaver(WikipediaPageStorage a) {
             storage = a;
         }
         
         @Override
         public void handle(WikipediaPage page) {
 
             //redirects preprocessing
             if (page.isRedirect) {
                 int baseNameStarts = page.rawText.indexOf("[[");
                 int baseNameEnds = page.rawText.indexOf("]]");
 
                 if (baseNameStarts != -1 && baseNameEnds != -1) {
                     String baseName = page.rawText.substring(baseNameStarts+2, baseNameEnds);
                     page.redirectPageTitle = baseName;
                 }
             }
 
             try {
                 storage.savePage(page);
             } catch (WikipediaPageStorageException e) {
                 AtexantApp.log("page "+ page.id.toString() +" was not saved successfully probably because it's already in storage");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         } 
     }
     
     public static void main(String[] args) throws Exception
     {
         String first = "I like red apples";
         String second = "He adores green oranges";
         
         SentenceSemanticSimilarityMeasurer sm = new SentenceSemanticSimilarityMeasurer();
         
         sm.getSimilarityOfSentences(first, second);
         
          localProps = new Properties();
         
         try {
             localProps.load(new FileInputStream("local.properties"));
         } catch(IOException e) {          
             System.out.println("File local.properties should be created. See in _local.properties");
             e.printStackTrace();
             System.exit(1);
         }
         
         if (args.length < 1) {
             return;
         }
         
         final AtexantApp app = new AtexantApp();
         
         if (args[0].equalsIgnoreCase("processFile")) {
             if (args.length < 3) {
                 return;
             }
             
             WikipediaPageHandler handler = null;
             
             String handlerCmd = args[2];
             
             if (handlerCmd.equalsIgnoreCase("saveAllPagesInDb")) {
                 handler = new WikipediaPageSaver(app.getStorage());
             } else if(handlerCmd.equalsIgnoreCase("wordMentions")) {
                 handler = new WordsMentionsPersisterWikipediaPageHandler(new FileSystemWordsMentionsStorage());
             } else {
                 System.out.println("wrong wikipedia page handler (3 parameter)");
                 return;
             }
             
             long offset = 0;
             
             if (args.length > 3) {
                 offset = Long.parseLong(args[3]);
             }
             
             app.proccessXmlFile(args[1], handler, offset);
             return;
         }
         
         if (args[0].equalsIgnoreCase("processRedirects")) {
             app.processRedirectsInDb();
             return;
         }
         
         if (args[0].equalsIgnoreCase("debug")) {
 	    app.wikiDebug(args[1]);
 	    return;
 	}
 
         return;
     }
     
     public WikipediaPageStorage getStorage() throws Exception {
         return new MysqlWikipediaPageStorage();
     }
     
     public void processRedirectsInDb() throws Exception {
         WikipediaPageStorage st = getStorage();
         for (WikipediaPage p : st.getAll()) {
             if (!p.isRedirect) {
                 continue;
             }
             
             try {
                 WikipediaPage redirect = st.findByTitle(p.redirectPageTitle);
                 p.redirectPageId = redirect.id;
             } catch (WikipediaPageStorageException e) {
                 log("redirected page was not found for " + p.id.toString());
                 continue;
             } catch (Exception e) {
                 int a = 0;
                 e.printStackTrace();
             }
             
             try {
                 st.savePage(p);
             } catch (WikipediaPageStorageException e) {
                 log("redirecting page" + p.id.toString() + " was not saved successfully");
                 continue;
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     public void proccessXmlFile(String filename, WikipediaPageHandler handler, long offset) throws Exception {
         WikipediaParser.parse(filename, handler, offset);
     }
     
     public static void log(String message) {
         System.out.println(message);
     }
     
     private void wikiDebug(String fileName) throws Exception
     {
 	WikipediaParser.parse(fileName, new WikipediaPageHandler(){
 		private int count = 0;
 
 		public void handle(WikipediaPage page)
 		{
 		    System.out.println("" + page);
 		    count++;
 		    if (count > 100)
 			System.exit(0);
 		}
 	    });
     }
 }
