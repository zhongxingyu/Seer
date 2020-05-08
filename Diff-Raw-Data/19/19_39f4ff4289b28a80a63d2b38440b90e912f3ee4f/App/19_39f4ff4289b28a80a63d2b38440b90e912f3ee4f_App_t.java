 package com.pcwerk.seck;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 
 import com.pcwerk.seck.crawler.Crawler;
 import com.pcwerk.seck.extractor.ExtractorFactory;
 import com.pcwerk.seck.extractor.MediaExtractor;
 import com.pcwerk.seck.indexer.Indexer;
 import com.pcwerk.seck.store.WebDocument;
 import com.pcwerk.seck.store.DatabaseSQL;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 public class App {
   
   protected ArrayList<String> commandStack = new ArrayList<String>();
   protected HashMap<String,String> params = new HashMap<String,String>();
   
   
   public App() {
     // some default value
     params.put("hbase-host", "localhost");
   }
   
   public static void main(String[] argv) { 
     App app = new App();
     app.parseArgs(argv);
     app.showParams();
     app.run();
   }
 
   private void showParams() {
     System.out.println("[i] commands:");
     for (String command : commandStack) {
       System.out.println("[i]   " + command);
     }
     
     System.out.println("[i] parameters:");
     for (String key : params.keySet() ) {
       System.out.println("[i]   " + key + " => " + params.get(key));
     }
   }
 
   private void usage() {
     System.out.println("usage: App <command> <required parameters> [options]");
     System.out.println("command(s):");
     System.out.println("crawl                perform crawling action");
     System.out.println("crawlinfo            display crawled information");
     System.out.println("index                perform indexing action");
     System.out.println("indexinfo            display index information");
     System.out.println("hbasetest            run hbase connection test");
     System.out.println("cmdlinesearch        run command line search");
     System.out.println("options:");
     System.out.println("  --path=path        specify the search path");
     System.out.println("  --search-key=key   specify the search keyword");
     System.out.println("  --root-url=URL     specify the crawling at root url");
     System.out.println("  --hbase-host=host  specifi the hostname (default=localhost");
     System.out.println("  --tc=#             specify the thread count (default=1)");
     System.out.println("  --depth=#          specify crawling depth (default = 1");
     System.out.println("  --file=FILENAME    specify a datafile to save (if not specified, use to stdout)");
   }
   
   private void run() {
     for (String command : commandStack) {
       System.out.println("[i] execute: " + command);
       if (command.equals("crawl")) {
         if (! params.containsKey("root-url")) {
           usage();
           System.exit(0);
         }
         crawl();
       } else if (command.equals("index")) {
           index();
       } else if (command.equals("index-info")) {
           indexInfo();
       }
       else if (command.equals("crawl-info")) {
         crawlInfo();
       } else if (command.equals("extract")) {
           extractor();
        } else if (command.equals("hbasetest")) {
         hbaseTest();
       } else if (command.equals("cmdlinesearch")) {
         commandLineSearch();
       } else {
         System.out.println("[e]   '" + command + "' => unknown command");
       }
     }
   }
   
   private void extractor(){
       
       try{ 
           DatabaseSQL db = new DatabaseSQL(params.get( "file" ));
           List<String> urls = db.getMediaLinks();
           
           for (String url: urls){
         	  System.out.println("Extracting url: " + url);
               MediaExtractor me = new MediaExtractor(url);
               db.saveMedia(url, me.getMap());
           }            
           
           
           db.closeDb();
       }
       catch( SqlJetException e ){
           e.printStackTrace();
       }
   }
   
   private void commandLineSearch() {
     new CmdLineSearch().run(params.get("path"), params.get("search-key"));
   }
 
   private void hbaseTest() {
     try {
       new HBaseTest(params.get("hbase-host")).run();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   private void crawl() {
 	    System.out.println("[i]   crawling starts");
 	    
 	    String file = params.get("file");
 	    String rootUrl = params.get("root-url");
 	    int depth = Integer.parseInt(params.get("depth"));
 	    int tc = Integer.parseInt(params.get("tc"));
 	    
 	    Crawler crawler = new Crawler(file, rootUrl, depth, tc);
 	    
 	    try {
 			crawler.crawl();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Crawling failed");
 		}
 	    
 	    System.out.println("[i]   crawling ends");
 	  }
 
 	  private void crawlInfo() {
 	    System.out.println("[i]   display information on the crawled data");
 	    
 	    Crawler.crawlInfo(params.get("file"));
 	  }
   
 //	    private void extractor(){
 //	        ExtractorFactory efactory = new ExtractorFactory();
 //	        
 //	        String sourceURL = "someurl"; 
 //
 //	        efactory.extraction( sourceURL, params.get("file") );       
 //	        
 //	    }
 
 	    private void index(){
 	        System.out.println("[i]   indexing starts");
	        Indexer indexer = new Indexer("../../indexing");
 
 	        System.out.println("\n Indexing... \n");
 
 	        List<WebDocument> pages = new ArrayList<WebDocument>();
 	        try{ 
 	            DatabaseSQL db = new DatabaseSQL(params.get( "file" ));
 	            db.readDb(pages);
 	            db.closeDb();
 	        }
 	        catch( SqlJetException e ){
 	            e.printStackTrace();
 	        }
 
 	        indexer.indexWriter( pages );
 	        indexer.closeIndex();
 
 	        System.out.println("[i]   indexing ends");
 	    }
 
 	    private void indexInfo(){
 	        System.out.println("[i]   Display Index Table");
	        Indexer indexer = new Indexer("../../indexing");
 	        indexer.indexReader();
 	        System.out.println("[i]   End of Display");
 	    }
 
   private void parseArgs(String[] argv) {
     StringBuffer sb = new StringBuffer();
 
     final String shortopts = "-:hu:c:f:d:H:p:k;";
     final LongOpt[] longopts = 
     { 
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("path", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
         new LongOpt("search-key", LongOpt.REQUIRED_ARGUMENT, null, 'k'),
         new LongOpt("hbase-host", LongOpt.REQUIRED_ARGUMENT, null, 'H'),
         new LongOpt("root-url", LongOpt.REQUIRED_ARGUMENT, sb, 'u'),
         new LongOpt("tc", LongOpt.REQUIRED_ARGUMENT, sb, 'c'),
         new LongOpt("depth", LongOpt.REQUIRED_ARGUMENT, sb, 'd'),
         new LongOpt("file", LongOpt.REQUIRED_ARGUMENT, sb, 'f')
     };
 
     Getopt g = new Getopt("App", argv, shortopts, longopts);
     g.setOpterr(false);
 
     int c = 0;
 
     while ((c = g.getopt()) != -1) {
       String key = longopts[g.getLongind()].getName();
       String val = g.getOptarg();
 
       switch (c) {
       case 0:
         params.put(key, val);
         break;
       case 1:
         commandStack.add(val);
         break;
       case 2:
         // weirdness occurs!
         break;
       case 'h':
         usage();
         System.exit(0);
         break;
       case 'H':
         params.put("hbase-host", g.getOptarg());
         break;
       case 'p':
         params.put("path", g.getOptarg());
         break;
       case 'k':
         params.put("search-key", g.getOptarg());
         break;
       case 'u':
         params.put("root-url", g.getOptarg());
         break;
       case 'c':
         params.put("tc", g.getOptarg());
         break;
       case 'd':
         params.put("depth", g.getOptarg());
         break;
       case 'f':
         params.put("file", g.getOptarg());
         break;
       case ':':
         System.out.println("You need an argument for option " + (char) g.getOptopt());
         break;
       case '?':
         System.out.println("The option '" + (char) g.getOptopt() + "' is not valid");
         break;
       default:
         System.out.println("getopt() returned " + c);
         break;
       }
     }
     
     /*
     for (int i = g.getOptind(); i < argv.length; i++) {
       commandStack.add(argv[i]);
     }
     */
   }
 }
