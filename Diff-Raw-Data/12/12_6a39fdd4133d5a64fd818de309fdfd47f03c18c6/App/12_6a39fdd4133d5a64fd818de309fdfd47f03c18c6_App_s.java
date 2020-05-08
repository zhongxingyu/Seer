 package edu.rpi.tw.impav;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.prefs.BackingStoreException;
 
 import javax.swing.JFrame;
 
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.store.LockObtainFailedException;
 
 import twitter4j.TwitterException;
 
 /**
  * This application is now called skitter.
  * 
  */
 public class App  {
     
     public static String fileOrUri;
     private Thread thread;
     public static boolean fullscreen = false;
     
    public App(String urlo) throws Exception {
         
 //    	//test query for scalability upto full NCI Thesaurus 
 //    	String queryText = 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
 //				"PREFIX bfo: <http://www.ifomis.org/bfo/1.1#Entity>" +
 //				"PREFIX obo: <http://purl.obolibrary.org/obo/>" +
 //				"PREFIX skos:  <http://www.w3.org/2004/02/skos/core#>" +
 //				"CONSTRUCT {"+
 //				"?s skos:prefLabel ?termName."+
 //				"?s a skos:Concept." +
 //				"}WHERE{" +
 //				"GRAPH <http://bioportal.bioontology.org/ontologies/NCIT>{" +
 //				"?s rdfs:label ?termName." +
 //				"}" +
 //				"}";
 
     	
 //		-- Flu Ontology -------
 //		String queryText = 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
 //				"PREFIX bfo: <http://www.ifomis.org/bfo/1.1#Entity>" +
 //				"PREFIX obo: <http://purl.obolibrary.org/obo/>" +
 //				"PREFIX skos:  <http://www.w3.org/2004/02/skos/core#>" +
 //				"CONSTRUCT {"+
 //				"?s skos:prefLabel ?symptomName."+
 //				"?s a skos:Concept." +
 //				"}WHERE{" +
 //				"GRAPH <http://bioportal.bioontology.org/ontologies/FLU>{" +
 //				"?s ?p <http://purl.obolibrary.org/obo/OGMS_0000020>." +
 //				"?s <http://bioportal.bioontology.org/metadata/def/prefLabel> ?symptomName. "+
 //				"}}";
 
 //      -- Emotion Ontology ------ *to extract feelings
     	String queryText = 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
 				"PREFIX bfo: <http://www.ifomis.org/bfo/1.1#Entity>" +
 				"PREFIX obo: <http://purl.obolibrary.org/obo/>" +
 				"PREFIX skos:  <http://www.w3.org/2004/02/skos/core#>" +
 				"CONSTRUCT {"+
 				"?s skos:prefLabel ?emotionName."+
 				"?s1 skos:prefLabel ?synonym."+
 				"?s a skos:Concept." +
 				"?s1 a skos:Concept." +
 				"}WHERE{" +
 				"GRAPH <http://bioportal.bioontology.org/ontologies/MFOEM>{" +
 				"?s rdfs:subClassOf obo:MFOEM_000001." +
 				"?s rdfs:label ?emotionName. "+
 				"?s1 rdfs:subClassOf ?s."+
 				"?s1 rdfs:label ?synonym."+
 				"}}";
 
     	
     	String sparqlService = "http://sparql.bioontology.org/sparql";
 		String apikey = "b2aa80e5-8af9-4cc8-9226-55547c5faa65";
 
 		String httpQueryString = String.format("query=%s&apikey=%s", 
 			     URLEncoder.encode(queryText, "UTF-8"), 
 			     apikey);
 		
 		String url = sparqlService + "?" + httpQueryString;
 
     	TweetQueue queue = new TweetQueue(url);
         queue.start(); 
         GraphBuilder builder = new GraphBuilder(queue, this);
         thread = new Thread(builder);
         thread.start();
     }
 
     public static void main(String[] args) {
             try {
                App app = new App(args[0]);
             } catch (Exception e) {
                 e.printStackTrace();
             }
     }
 }
