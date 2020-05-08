 package zeontologynewsextractor;
 
 import extractor.AnnotationsGesture;
 import utils.RSSGetter;
 import extractor.InseeSparqlRequest;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import utils.*;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class ZeOntologyNewsExtractor {
 
     /**
      * @param args the command line arguments 
      */
     public static void main(String[] args) throws MalformedURLException  {
         ArrayList<Item>  items = new ArrayList<Item>();
         String [] fluxLinks = {
                          //"resources/examples/europe1.rss"
                          "http://news.google.fr/news?pz=1&cf=all&ned=fr&hl=fr&topic=n&output=rss&num=1"
                        // ,"http://europe1.fr.feedsportal.com/c/32376/f/546041/index.rss"
                        // ,"http://actualite.portail.free.fr/france/rss.xml"
                        // ,"http://www.tv5.org/TV5Site/rss/actualites.php?rub=3"
         };
         
         System.out.println("========= Starting rss downloading==================");
         items.addAll(RSSGetter.getFlux(fluxLinks));
         
         System.out.println("========= Cleaning flow with already analysed items==================");
         RDFDatabase.verifyItemsList(items);
         
         System.out.println("========= Starting annotations adding==================");
         AnnotationsGesture.addAnnotations(items);
         
         System.out.println("========= Printing result items==================");
         for(int i=0; i< items.size();i++)System.out.println("\n"+items.get(i));
         
         System.out.println("========= Saving to MYSQL==================");
         MysqlDatabase.createItemFromItems(items);
         
         System.out.println("========= saving to rdf database==================");
         RDFDatabase.addItems(items);
 
         System.out.println("========= saving to 4Store database==================");
         FourStoreDatabase.addItems(items);
         System.out.println("========= writing the rdf output file==================");
         RDFDatabase.writeRDFFile();
         
         System.out.println("========= closing database ==================");
         MysqlDatabase.close();
         
         System.out.println("Done");
     }
 }
