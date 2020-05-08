 /**
  * Commandline example showing:
  * 1. How to load an XBRL instance document
  * 2. How to analyse the presentation networks for that instance
  */
 package org.xbrlapi.data.bdbxml.examples.load;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xbrlapi.Concept;
 import org.xbrlapi.Context;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.FootnoteResource;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.Instance;
 import org.xbrlapi.Item;
 import org.xbrlapi.Resource;
 import org.xbrlapi.Unit;
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.data.bdbxml.StoreImpl;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.loader.LoaderImpl;
 import org.xbrlapi.sax.EntityResolver;
 import org.xbrlapi.sax.EntityResolverImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.xlink.XLinkProcessor;
 import org.xbrlapi.xlink.XLinkProcessorImpl;
 import org.xbrlapi.xlink.handler.XBRLCustomLinkRecogniserImpl;
 import org.xbrlapi.xlink.handler.XBRLXLinkHandlerImpl;
 
 /**
  *  This example loads a set of documents, including one or more XBRL instances,
  *  based on the list of URLs provided on the command line.  The documents
  *  are loaded into a Oracle Berkeley XML Database (but this can easily be modified
  *  by using a different data store) and then the information in the instances
  *  that have been loaded is analysed and reported on, demonstrating how
  *  the XBRLAPI can be used to access information in instances.
  * @author Geoff Shuetrim (geoff@galexy.net)
  */
 public class Load {
 
     private static Store store = null;
 
     /**
      * @param args The array of commandline arguments.
      */
     public static void main(String[] args) {
 
         try {
             
             // Process command line arguments
             List<URI> inputs = new LinkedList<URI>();
             HashMap<String,String> arguments = new HashMap<String,String>();
             int i = 0;
             if (i >= args.length)
                 badUsage("No map of taxonomies has been specified");
             while (true) {
                 if (i == args.length)
                     break;
                 else if (args[i].charAt(0) == '-') {
                     if (args[i].equals("-database")) {
                         i++;
                         arguments.put("database", args[i]);
                     } else if (args[i].equals("-container")) {
                         i++;
                         arguments.put("container", args[i]);
                     } else if (args[i].equals("-cache")) {
                         i++;
                         arguments.put("cache", args[i]);
                         
                     } else if (args[i].equals("-dbCacheSize")) {
                         i++;
                         arguments.put("dbCacheSize", args[i]);
                         
                     } else
                         badUsage("Unknown argument: " + args[i]);
                 } else {
                     try {
                         inputs.add(new URI(args[i]));
                     } catch (URISyntaxException e) {
                         badUsage("Malformed discovery starting point URI: " + args[i]);
                     }
                 }
                 i++;
             }
             
             if (! arguments.containsKey("database")) badUsage("You need to specify the database directory.");
             if (! arguments.containsKey("container")) badUsage("You need to specify the database container name.");
             if (! arguments.containsKey("cache")) badUsage("You need to specify the root of the document cache.");
             if (inputs.size() < 1) badUsage("You need to specify at least one starting point for discovery.");
             
             // Make sure that the taxonomy cache exists
             try {
                 File cache = new File(arguments.get("cache"));
                 if (!cache.exists()) badUsage("The document cache directory does not exist. " + cache.toString());
             } catch (Exception e) {
                 badUsage("There are problems with the cache location: " + arguments.get("cache"));
             }
             
             // Set up the cache size if that has been specified.
             
             
             // Set up the data store to load the data
             store = createStore(arguments.get("database"),arguments.get("container"), arguments.get("dbCacheSize"));
 
             // Set up the data loader (does the parsing and data discovery)
             Loader loader = createLoader(store,arguments.get("cache"));
             
             // Load the instance data
             loader.discover(inputs);
             
             // Clean up the data store and exit
             cleanup(store);
 
         } catch (Exception e) {
             badUsage(e.getMessage());
         }
         
     }
     
     /**
      * Report the information about a concept in the presentation heirarchy
      * @param indent The indent to use for reporting the fragment
      * @param fragment The fragment to report
      * @param linkRole The linkrole of the network to use
      * @throws XBRLExceptions
      */
     private static void reportNode(String indent, Fragment fragment, URI linkRole) throws XBRLException {
         Concept concept = (Concept) fragment;
         System.out.println(indent + concept.getTargetNamespace() + ":" + concept.getName());
         List<Fragment> children = store.getTargets(concept.getIndex(),linkRole,Constants.PresentationArcrole);
         if (children.size() > 0) {
             for (Fragment child: children) {
                 reportNode(indent + " ", child,linkRole);
             }  
         }
     }
     
     private static void reportInstance(Instance instance) throws XBRLException {
         List<Item> items = instance.getChildItems();
         System.out.println("Top level items in the instance.");
         for (Item item: items) {
             System.out.println(item.getLocalname() + " " + item.getContextId());
         }
 
         List<Context> contexts = instance.getContexts();
         System.out.println("Contexts in the instance.");
         for (Context context: contexts) {
             System.out.println("Context ID " + context.getId());
         }
     
         List<Unit> units = instance.getUnits();
         System.out.println("Units in the instance.");
         for (Unit unit: units) {
             System.out.println("Unit ID " + unit.getId());
         }
         
         List<ExtendedLink> links = instance.getFootnoteLinks();
         System.out.println("Footnote links in the instance.");
         for (ExtendedLink link: links) {            
             List<Resource> resources = link.getResources();
             for (Resource resource: resources) {
                 FootnoteResource fnr = (FootnoteResource) resource;
                 System.out.println("Footnote resource: " + fnr.getDataRootElement().getTextContent());
             }
         }
         
     }
     
     /**
      * Report incorrect usage of the command line, with a list of the arguments
      * @param message The error message describing why the command line usage failed.
      */
     private static void badUsage(String message) {
         if (!"".equals(message)) {
             System.err.println(message);
         }
         
         System.err.println("Command line usage: java org.xbrlapi.bdbxml.examples.load.Load -database VALUE -container VALUE -cache VALUE URI1 URI2 ...");
         System.err.println("Mandatory arguments: ");
         System.err.println(" -database VALUE   directory containing the Oracle BDB XML database");
         System.err.println(" -container VALUE  name of the data container");
         System.err.println(" -cache VALUE      directory that is the root of the document cache");
         System.err.println("Optional arguments: ");
         System.err.println(" -dbCacheSize VALUE    Size of the cache (in MB) to use for the Oracle BDB XML database");        
         System.err.println("EG: To load the XBRL instance schema.");
        System.err.println("java -Xmx1g -Dlog4j.configuration=/home/geoff/log4j.xml org.xbrlapi.bdbxml.examples.load.Load -dbCacheSize 250 -database /home/geoff/container -container test -cache /home/geoff/cache http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd");        
     }
     
     /**
      * Create an Oracle Berkeley XML database store and return it
      * cast to an XBRL data store to expose XBRL store enhancements.
      * @param database The location to use for the new store.
      * @param container The name to use for the XML container.
      * @param cacheSize The string representation of the number of MB of database
      * cache to use.
      * @return the new store.
      * @throws XBRLException if the store cannot be initialised.
      */
     private static Store createStore(String database, String container, String cacheSize) throws XBRLException {
         if (cacheSize == null)return new StoreImpl(database,container);
         return new StoreImpl(database,container, (new Integer(cacheSize)).intValue());
     }
     
     
     /**
      * @param store The store to use for the loader.
      * @param cache The root directory of the document cache.
      * @return the loader to use for loading the instance and its DTS
      * @throws XBRLException if the loader cannot be initialised.
      */
     private static Loader createLoader(Store store, String cache) throws XBRLException {
         XBRLXLinkHandlerImpl xlinkHandler = new XBRLXLinkHandlerImpl();
         XBRLCustomLinkRecogniserImpl clr = new XBRLCustomLinkRecogniserImpl(); 
         XLinkProcessor xlinkProcessor = new XLinkProcessorImpl(xlinkHandler ,clr);
         
         File cacheFile = new File(cache);
         
         // Rivet errors in the SEC XBRL data require these URI remappings to prevent discovery process from breaking.
         HashMap<URI,URI> map = new HashMap<URI,URI>();
         try {
             map.put(new URI("http://www.xbrl.org/2003/linkbase/xbrl-instance-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/instance/xbrl-instance-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/linkbase/xbrl-linkbase-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/instance/xbrl-linkbase-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/instance/xl-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xl-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/linkbase/xl-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xl-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/instance/xlink-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xlink-2003-12-31.xsd"));
             map.put(new URI("http://www.xbrl.org/2003/linkbase/xlink-2003-12-31.xsd"),new URI("http://www.xbrl.org/2003/xlink-2003-12-31.xsd"));
         } catch (URISyntaxException e) {
             throw new XBRLException("URI syntax exception",e);
         }
         EntityResolver entityResolver = new EntityResolverImpl(cacheFile,map);      
         
         Loader myLoader = new LoaderImpl(store,xlinkProcessor);
         myLoader.setCache(new CacheImpl(cacheFile));
         myLoader.setEntityResolver(entityResolver);
         xlinkHandler.setLoader(myLoader);
         return myLoader;
     }
     
     /**
      * Helper method to clean up and shut down the data store.
      * @param store the store for the XBRL data.
      * @throws XBRLException if the store cannot be closed. 
      */
     private static void cleanup(Store store) throws XBRLException {
         store.close();
     }    
 
 }
