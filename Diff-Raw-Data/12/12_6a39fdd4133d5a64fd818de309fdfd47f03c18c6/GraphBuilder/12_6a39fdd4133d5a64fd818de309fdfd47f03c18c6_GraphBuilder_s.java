 
 package edu.rpi.tw.impav;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 
 import javax.swing.SwingUtilities;
 
 import org.apache.jena.fuseki.http.UpdateRemote;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.update.UpdateFactory;
 import com.hp.hpl.jena.update.UpdateRequest;
 
 public class GraphBuilder implements Runnable {
 
     private TweetQueue queue;
     
     private int count = 0;
 
     private App app;
     
     private boolean run = true;
     
     public synchronized void pleaseStop() {
         run = false;
     }
     
     public GraphBuilder(TweetQueue queue, App app) {
         this.queue = queue;
         this.app = app;
     }
 
     @Override
     public void run() {	
         while (true) {
             synchronized (this) {
                 if (!run) return;
             }
             List<Tweet> tweets = new LinkedList<Tweet>();
             BlockingQueue<Tweet> q = queue.getTweets();
                         
             // First attempt to get a single tweet, 
             // blocking until there is at least one.
             try {
                 Tweet t = q.take();
                 tweets.add(t);
             } catch (InterruptedException e) {
             }
             // Once we've established that there's at least one,
             // drain the rest to the list for processing.
             q.drainTo(tweets);
             
             // Build up the graph nodes.
             synchronized(app) {
                 processTweets(tweets);
             }
             
         }
     }
 
     private void processTweets(List<Tweet> tweets) {
         for (Tweet t : tweets) {
             System.out.println("label:" + t.labels.toString());
             System.out.println("tweet:" + t.text);
             System.out.println("time:" + t.created);
             System.out.println("location: " + t.location + "\n");           
 //            System.out.println("origianl: " + t.originalText + "\n");
             
             UpdateRequest request = UpdateFactory.create();
             try {
                 
                 for(String label:t.labels){
                 	
                 	//To Jim: NOT working with following CODE (do not UPDATE to Endpoint) : with prov:value and rdfa:seeAlso
                 	//Also, GRAPH doesn't work with me. Can you fix it if you can?
                 	
                     String query = "prefix dc: <http://purl.org/dc/terms/> \n"+
                         "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
                         "prefix prov: <http://www.w3.org/ns/prov#>  \n"+
                         "prefix ogc: <http://www.opengis.net/rdf#>  \n"+
                         "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>  \n"+
                         "prefix foaf: <http://xmlns.com/foaf/0.1/>  \n"+
                         "prefix owl: <http://www.w3.org/2002/07/owl#>  \n"+
                         "prefix xsd: <http://www.w3.org/2001/XMLSchema#>  \n"+
                         "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
                         "INSERT DATA { GRAPH <http://purl.org/twc/skitter/testgraph> {\n"+
                         "  <http://purl.org/twc/skitter/"+ t.creator.getScreenName() + "/status/" + t.id +"> prov:value '''" + t.text + "'''; \n"+
                         "      rdfs:seeAlso <http://twitter.com/" + t.creator.getScreenName() + "/status/" + t.id + ">; \n"+
                         "      prov:wasAttributedTo <http://twitter.com/" + t.creator.getScreenName() + ">; \n"+
                         "      dc:date \"" + t.getCreated() + "\"^^xsd:dateTime . \n";
                     for (Individual concept : t.termVector) {
                         query += "  <http://purl.org/twc/skitter/"+ t.creator.getScreenName() + "/status/" + t.id +"> dc:subject <"+concept.getURI()+">. \n";
                     }
                     if(t.location.compareTo("null") < 0){
                         query = query + "  <http://purl.org/twc/skitter/"+ t.creator.getScreenName() + "/status/" + t.id +"/location> a geo:Point; \n"+
                             "     " + t.location + "\n"+
                             "  <http://purl.org/twc/skitter/"+ t.creator.getScreenName() + "/status/" + t.id +"> prov:atLocation <http://purl.org/twc/skitter/"+ t.creator.getScreenName() + "/status/" + t.id +">.\n";
                         //request.add("prefix dc: <http://purl.org/dc/terms/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix prov: <http://www.w3.org/ns/prov#> prefix ogc: <http://www.opengis.net/rdf#> prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> prefix foaf: <http://xmlns.com/foaf/0.1/> prefix owl: <http://www.w3.org/2002/07/owl#> prefix xsd: <http://www.w3.org/2001/XMLSchema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> INSERT DATA{ <http://purl.org/twc/skitter/tweet/"+ count +"> dc:subject <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + label + ">; prov:value \"" + t.text + "\"; rdfs:seeAlso <http://twitter.com/" + t.creator + "/status/" + t.id + ">; dc:date \"" + t.created + "\"^^xsd:dateTime . <http://purl.org/twc/skitter/tweet/" + count + "/location> a geo:Point;"+ t.location + "<http://purl.org/twc/skitter/tweet/" + count + "> prov:location <http://purl.org/twc/skitter/tweet/" + count + "/location>.}" );
 
                     }else{
                         //request.add("prefix dc: <http://purl.org/dc/terms/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix prov: <http://www.w3.org/ns/prov#> prefix ogc: <http://www.opengis.net/rdf#> prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> prefix foaf: <http://xmlns.com/foaf/0.1/> prefix owl: <http://www.w3.org/2002/07/owl#> prefix xsd: <http://www.w3.org/2001/XMLSchema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> INSERT DATA{ <http://purl.org/twc/skitter/tweet/"+ count +"> dc:subject <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + label + ">; prov:value \"" + t.text + "\"; rdfs:seeAlso <http://twitter.com/" + t.creator + "/status/" + t.id + "> ; dc:date \"" + t.created + "\"^^xsd:dateTime .}");
                     }
                     query = query + "} }";
                     System.out.println(query);
 
                     request.add(query);
                     
                     // But working with following -- without prov:value & rdfa:seeAlso 
 //                    if(t.location.compareTo("null") < 0){
 //                        request.add("prefix dc: <http://purl.org/dc/terms/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix prov: <http://www.w3.org/ns/prov#> prefix ogc: <http://www.opengis.net/rdf#> prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> prefix foaf: <http://xmlns.com/foaf/0.1/> prefix owl: <http://www.w3.org/2002/07/owl#> prefix xsd: <http://www.w3.org/2001/XMLSchema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> INSERT DATA{<http://purl.org/twc/skitter/tweet/"+ count +"> dc:subject <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + label + "> ;	dc:date \"" + t.created + "\"^^xsd:dateTime . <http://purl.org/twc/skitter/tweet/" + count + "/location> a geo:Point;"+ t.location + "<http://purl.org/twc/skitter/tweet/" + count + "> prov:location <http://purl.org/twc/skitter/tweet/" + count + "/location>.}" );
 //
 //                    }else{
 //                        request.add("prefix dc: <http://purl.org/dc/terms/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix prov: <http://www.w3.org/ns/prov#> prefix ogc: <http://www.opengis.net/rdf#> prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> prefix foaf: <http://xmlns.com/foaf/0.1/> prefix owl: <http://www.w3.org/2002/07/owl#> prefix xsd: <http://www.w3.org/2001/XMLSchema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> INSERT DATA{<http://purl.org/twc/skitter/tweet/"+ count +"> dc:subject <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + label + "> ;	dc:date \"" + t.created + "\"^^xsd:dateTime .}");
 //                    }
 
                 }        
                 count++;
                UpdateRemote.execute(request, "http://localhost:3030/db/update"); //http://doppio.med.yale.edu:3030
             	
             } catch (Exception e){
                 System.out.println(e);
             }
         }
     }
 
 }
