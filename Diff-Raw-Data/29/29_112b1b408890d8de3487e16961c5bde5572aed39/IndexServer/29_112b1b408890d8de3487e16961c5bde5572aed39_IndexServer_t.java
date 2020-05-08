 package edu.umich.eecs485.pa6;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Iterator;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import edu.umich.eecs485.pa6.utils.QueryHit;
 import edu.umich.eecs485.pa6.utils.GenericIndexServer;
 
 public class IndexServer extends GenericIndexServer {
     
     
    static HashMap<String, List<DocItem>> map;
    
   static HashMap<String, Double> pr_map;
   
   static HashMap<String, Double> df_map;
   
   static long doc_length;
    
    File pr_file;
     
   public IndexServer(int port, File fname, File pr_filename) throws IOException {
     super(port, fname);
     pr_file = pr_filename;
   }
 
   public void initServer(File fname) {
     // Do something!
     System.err.println("Init server with fname " + fname);
     
     // here we will load the serialized map object back into mem.
   
     map = new HashMap<String, List<DocItem>>();
     
     try {
         BufferedReader read = new BufferedReader(new FileReader(fname));
         
         String line;
         ArrayList<DocItem> listDocItem;
         String[] key_value;
         String word;
         String value;
         
         String[] df_list;
         String df;
         String [] list;
         String docid;
         String tfidf;
         DocItem item;
         
         long i = 0;
         
         while( (line = read.readLine()) != null ){
             key_value = line.split("\\s+", 2);
             word = key_value[0];
             value = key_value[1];
             
             df_list = value.split("\\s+", 2);
             df = df_list[0];
             list = df_list[1].split("\\s+");
             listDocItem = new ArrayList<DocItem>();
             
             for (int j=0; j<list.length; j++) {
                 docid = list[j].split(":")[0];
                 tfidf = list[j].split(":")[1];
                 item = new DocItem(docid, tfidf);
                 listDocItem.add(item);
             }
             
            df_map.put(word, new Double(df));
             map.put(word, listDocItem);
             System.out.println(i++);
         }
         
     } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     
     try {
         BufferedReader read = new BufferedReader(new FileReader(pr_file));
         
         String line;
         String docid;
         Double pr;
         
         while( (line = read.readLine()) != null ){
             docid = line.split("\\s+")[0];
             pr = new Double(line.split("\\s+")[1]);
             pr_map.put(docid, pr);
         }
         
     } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     
     System.out.println("done");
   }
   
   public List<QueryHit> processQuery(String query) {
       System.out.println("Processing query '" + query + "'");
       ArrayList<QueryHit> result = new ArrayList<QueryHit>();
       
       // Split query String into words
       String [] words = query.toLowerCase().split("\\s*[^0-9a-zA-Z']+\\s*"); 
       String word;
       int totalWords = words.length;
       
       HashSet<DocItem> union = new HashSet<DocItem>(); 
       
       for (int i=0; i<totalWords; i++) {
           word = words[i];
           
           if (map.get(word) != null) {
               union.addAll(map.get(word));
           }
       }
       
       for (DocItem item: union) {
           result.add( new QueryHit(item.getIdentifier(), calScore(words, item)) );
       }
       
       // this will sort doc item in descending order
       Collections.sort( result, new DocItemComparator());
       
       return result;
   }
   
   public static double calScore(String[] words, DocItem item) {
       // TODO
       double score = Math.random() * 10;
       return score;
   }
   
   public static class DocItemComparator implements Comparator<QueryHit>{
       // this will sort doc item in descending order
       
       @Override
       public int compare(QueryHit o1, QueryHit o2) {
           if (o1.getScore() > o2.getScore() ) {
               return -1;
           } else if (o1.getScore() == o2.getScore() ) {
               return 0;
           } else {
               return 1;
           }
       }
   }
   
   public static void main(String argv[]) throws IOException {
     if (argv.length < 2) {
       System.err.println("Usage: IndexServer <portnum> <inverted-index-filename> <pr-filename>");
       return;
     }
 
     // Parse args
     int i = 0;
     int portnum = -1;
     try {
       portnum = Integer.parseInt(argv[i++]);
     } catch (NumberFormatException nfe) {
       System.err.println("Cannot parse port number: " + argv[i-1]);
       return;
     }
     File fname = new File(argv[i++]).getCanonicalFile();
     File pr_filename = new File(argv[i++]).getCanonicalFile();
 
     // Run server.  Note that because server.serve() creates a new
     // thread, the process will not terminate even though serve() returns.
     IndexServer server = new IndexServer(portnum, fname, pr_filename);
     server.serve();
   }
 }
