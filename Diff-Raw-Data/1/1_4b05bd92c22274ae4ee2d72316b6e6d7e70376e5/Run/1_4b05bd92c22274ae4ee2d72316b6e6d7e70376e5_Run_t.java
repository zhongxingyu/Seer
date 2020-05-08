 /**
  * Commandline example showing:
  * 1. How to discover an XBRL instance document
  * 2. How to travers the presentation networks for that instance
  * 3. How to create an XBRL Inline document
  */
 package org.xbrlapi.examples.render;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.Concept;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.Instance;
 import org.xbrlapi.Item;
 import org.xbrlapi.LabelResource;
 import org.xbrlapi.RoleType;
 import org.xbrlapi.aspects.Aspect;
 import org.xbrlapi.aspects.AspectModel;
 import org.xbrlapi.aspects.AspectValue;
 import org.xbrlapi.aspects.NonDimensionalAspectModel;
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.data.XBRLStore;
 import org.xbrlapi.data.bdbxml.StoreImpl;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.loader.LoaderImpl;
 import org.xbrlapi.loader.discoverer.DiscoveryManager;
 import org.xbrlapi.networks.Network;
 import org.xbrlapi.networks.Networks;
 import org.xbrlapi.networks.Relationship;
 import org.xbrlapi.sax.EntityResolverImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.xlink.XLinkProcessor;
 import org.xbrlapi.xlink.XLinkProcessorImpl;
 import org.xbrlapi.xlink.handler.XBRLCustomLinkRecogniserImpl;
 import org.xbrlapi.xlink.handler.XBRLXLinkHandlerImpl;
 import org.xml.sax.EntityResolver;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 /**
  * @author Steve Yang (steve2yang@yahoo.com) (YangSt1)
  * @author Geoff Shuetrim (geoff@galexy.net)
  */
 public class Run {
 
     private static XBRLStore store = null;
 
     protected static Logger logger = Logger.getLogger(Run.class);
 
     private static double startTime;
 
     private static ArrayList<String> labels = new ArrayList<String>();
     private static Map<String,List<Item>> itemMap = new HashMap<String,List<Item>>();
     
     private static AspectModel aspectModel;
     private static Instance instance;
     private static ArrayList<Concept> concepts = new ArrayList<Concept>();
     private static int maxLevel = 1;
 
     public static void main(String[] args) {
 
         try {
 
             // Process command line arguments
             List<String> inputs = new Vector<String>();
             HashMap<String, String> arguments = new HashMap<String, String>();
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
 
                     } else if (args[i].equals("-output")) {
                         i++;
                         arguments.put("output", args[i]);
 
                     } else if (args[i].equals("-template")) {
                         i++;
                         arguments.put("template", args[i]);
 
                     } else if (args[i].equals("-target")) {
                         i++;
                         arguments.put("target", args[i]);
 
                     } else
                         badUsage("Unknown argument: " + args[i]);
 
                 } else {
                     inputs.add(args[i]);
                 }
                 i++;
             }
 
             if (!arguments.containsKey("database"))
                 badUsage("You need to specify the database directory.");
 
             if (!arguments.containsKey("container"))
                 badUsage("You need to specify the database container name.");
 
             if (!arguments.containsKey("cache"))
                 badUsage("You need to specify the root of the document cache.");
 
             if (!arguments.containsKey("template"))
                 badUsage("You need to specify a Freemarker template.");
 
             if (!arguments.containsKey("output"))
                 badUsage("You need to specify an output file.");
 
             if (!arguments.containsKey("target"))
                 badUsage("You need to specify a target XBRL instance.");
 
             // Make sure that the taxonomy cache exists
             try {
                 File cache = new File(arguments.get("cache"));
                 if (!cache.exists())
                     badUsage("The document cache directory does not exist. "
                             + cache.toString());
             } catch (Exception e) {
                 badUsage("There are problems with the cache location: "
                         + arguments.get("cache"));
             }
 
             startTime = System.currentTimeMillis();
 
             // Set up the data store to load the data
             store = createStore(arguments.get("database"), arguments
                     .get("container"));
 
             // Set up the data loader (does the parsing and data discovery)
             Loader loader = createLoader(store, arguments.get("cache"));
 
             // Queue up document URLs for discovery.
             URL targetURL = null;
             for (String url : inputs) {
                 try {
                     loader.stashURL(new URL(url));
                 } catch (MalformedURLException e) {
                     badUsage("Malformed URL for: " + url);
                 }
             }
             try {
                 targetURL = new URL(arguments.get("target"));
                 loader.stashURL(targetURL);
             } catch (MalformedURLException e) {
                 badUsage("Malformed URL for target XBRL instance.");
             }
             reportTime("Setting URLs");
 
             // Load the data required to render the XBRL instance.
             DiscoveryManager discoverer = new DiscoveryManager(loader);
             Thread discoveryThread = new Thread(discoverer);
             discoveryThread.start();
             while (discoveryThread.isAlive()) {
                 Thread.sleep(2000);
                 logger.info("Currently loading " + loader.getDocumentURL()
                         + ".");
                 logger.info(loader.getDocumentsStillToAnalyse().size()
                         + " documents still to load.");
             }
             reportTime("Loading data");
 
             // Get the Freemarker template ready to use.
             if (!arguments.containsKey("template"))
                 throw new XBRLException(
                         "The Freemarker template must be specified.");
             File templateFile = new File(arguments.get("template"));
             if (!templateFile.exists())
                 throw new XBRLException(
                         "The freemarker template does not exist.");
             Configuration freemarker = new Configuration();
             freemarker.setDirectoryForTemplateLoading(templateFile
                     .getParentFile());
             freemarker.setObjectWrapper(new DefaultObjectWrapper());
             Template template = freemarker.getTemplate(templateFile.getName());
 
             Run.reportTime("Configuring Freemarker");
 
             // Create the Freemarker data-model that will populate the template
             Map<String, Object> model = new HashMap<String, Object>();
 
             // Get the root fragment of the target XBRL instance
             FragmentList<Instance> instances = store.getFragmentsFromDocument(targetURL, "Instance");
             if (instances.getLength() > 1)
                 throw new XBRLException("The target instance is not a single XBRL instance.");
             if (instances.getLength() == 0)
                 throw new XBRLException("The target document is not an XBRL instance.");
             instance = instances.get(0);
             reportTime("Getting the instance");
 
             SimpleDateFormat fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
             model.put("store", store);
             model.put("iso4217",org.xbrlapi.utilities.Constants.ISO4217);
             model.put("xbrli",org.xbrlapi.utilities.Constants.XBRL21Namespace);
             model.put("now", fmt.format(new Date()));
             model.put("linkbaseReferences", instance.getLinkbaseRefs());
             model.put("schemaReferences", instance.getSchemaRefs());
             model.put("contexts", instance.getContexts());
             model.put("units", instance.getUnits());
             reportTime("Adding report resources to the data model");
             
             FragmentList<Item> items = instance.getItems();
             for (Item item: items) {
                 String key= item.getNamespaceURI() + item.getLocalname();
                 if (itemMap.containsKey(key)) {
                     itemMap.get(key).add(item);
                 } else {
                     List<Item> newlist = new Vector<Item>();
                     newlist.add(item);
                     itemMap.put(key,newlist);
                 }
             }
             reportTime("Mapping items by concept");
             
             // Iterate the presentation networks in the DTS
             List<Map<String, Object>> tables = new Vector<Map<String, Object>>();
             model.put("tables", tables);
             for (String linkrole : store.getLinkRoles(Constants.PresentationArcRole).keySet()) {
                 HashMap<String, Object> table = new HashMap<String, Object>();
                 tables.add(table);
 
                 labels = new ArrayList<String>();
                 concepts = new ArrayList<Concept>();
                 maxLevel = 1;
                 
                 String roleTitle = linkrole;
                 FragmentList<RoleType> roleDeclarations = store
                         .getRoleTypes(linkrole);
                 if (roleDeclarations.getLength() > 0)
                     roleTitle = roleDeclarations.get(0).getDefinition();
                 table.put("title", roleTitle);
 
                 // Configure the aspect model (useful for sorting facts by their aspects)
                 aspectModel = new NonDimensionalAspectModel();
                 aspectModel.arrangeAspect(Aspect.PERIOD,"column");
 
                 FragmentList<Concept> roots = store.<Concept>getNetworkRoots(
                         Constants.XBRL21LinkNamespace, "presentationLink",
                         linkrole, Constants.XBRL21LinkNamespace,
                         "presentationArc", Constants.PresentationArcRole);
 
                 maxLevel = 1;
                 for (Concept root : roots) {
                     parsePresentation(
                             "", 
                             null, 
                             root, 
                             new Float(0.0).floatValue(), 
                             linkrole,
                             Constants.StandardLabelRole);
                 }
                 
                 // Get the sorted list of period aspect values
                 List<List<AspectValue>> aspectCombinations = aspectModel.getAspectValueCombinationsForDimension("column");
                 List<AspectValue> periods = new Vector<AspectValue>();
                 for (int j=0; j<aspectCombinations.size(); j++) {
                     periods.add(aspectCombinations.get(j).get(0));
                 }
                 
                 // Map from concept name (resolved QName) to the concept label.
                 table.put("aspectModel", aspectModel);
                 table.put("concepts", concepts);
                 table.put("labels", labels);
                 table.put("periods",periods);
                 table.put("maxLevel", maxLevel);
                 reportTime("Processing  " + roleTitle);
                 
                 break;
                 
             }
 
             // TODO Extend the template to render the footnote information.
             FragmentList<ExtendedLink> footnoteLinks = instance
                     .getFootnoteLinks();
             model.put("footnotes", footnoteLinks);
             reportTime("Processing footnotes");
 
             // Process the template and data model to produce the rendered report.
             Writer out = new OutputStreamWriter(new FileOutputStream(arguments.get("output")));
             template.process(model, out);
             out.flush();
             reportTime("Processing the template");
 
             // Clean up the data store and exit
             cleanup(store);
             reportTime("Database cleanup");
             System.exit(0);
 
         } catch (Exception e) {
             try {
                 cleanup(store);
                 reportTime("Database cleanup");
             } catch (Exception neverThrown) {
                 ;
             }
             e.printStackTrace();
             badUsage(e.getMessage());
         }
 
     }
 
     private static void parsePresentation(
             String indent,
             Concept parent, 
             Concept concept, 
             float order,
             String linkrole,
             String labelRole
             ) throws Exception {
 
         concepts.add(concept);
 
         String conceptKey = concept.getTargetNamespaceURI() + concept.getName();
         if (itemMap.containsKey(conceptKey)) {
             List<Item> items = itemMap.get(conceptKey);
             if (! items.isEmpty()) {
                 for (Item item: items) {
                     aspectModel.addFact(item);
                 }
             }
         }
         
         maxLevel = Math.max(indent.length(), maxLevel);
 
         // Get the standard concept label
         //logger.info(conceptKey + " " + labelRole);
         FragmentList<LabelResource> labelResources = concept.getLabelsWithLanguageAndRole("en-US",labelRole);
         String label;
         if (labelResources.getLength() > 0) {
             label = labelResources.get(0).getStringValue().trim();
         } else {
             label = concept.getName();
         }
         label = indent + label;
         labels.add(label);
         logger.info(label);
 
         Networks networks = concept.getNetworksFromWithRoleAndArcrole(linkrole, Constants.PresentationArcRole);
 
         if (networks.getSize() > 0) {
 
             Network network = networks.getNetwork(Constants.PresentationArcRole,linkrole);
             List<Relationship> relationships = network.getActiveRelationshipsFrom(concept.getFragmentIndex());
 
             for (Relationship relationship: relationships) {
                 
                 labelRole = Constants.StandardLabelRole;
                 if (relationship.getArc().hasAttribute("preferredLabel"))
                     labelRole = relationship.getArc().getAttribute("preferredLabel");
                 
                 parsePresentation(
                         indent + " ", 
                         concept, 
                         (Concept) relationship.getTarget(), 
                         new Float(relationship.getOrder()).floatValue(), 
                         linkrole,
                         labelRole);
             }
         }
     }
 
 
 
 
 
     /**
      * Convenience method to report durations required to perform various tasks.
      * 
      * @param task
      *            The task being reported on.
      */
     protected static void reportTime(String task) {
         // Report on the time taken to do the rendering.
         String duration = (new Double(
                 (System.currentTimeMillis() - startTime) / 1000)).toString();
         logger.info(task + " took " + duration + " second(s).");
        startTime = System.currentTimeMillis();
     }
 
     /**
      * Report incorrect usage of the command line, along with instructions on
      * correct usage.
      * 
      * @param message
      *            The error message describing why the command line usage
      *            failed.
      */
     private static void badUsage(String message) {
         if (!"".equals(message)) {
             System.err.println(message);
         }
 
         System.err.println("Command line usage: java org.xbrlapi.examples.render.Run <OPTIONS> <ADDITIONAL URLS>");
         System.err.println("Mandatory arguments: ");
         System.err.println(" -database VALUE     directory containing the Oracle Berkeley XML database");
         System.err.println(" -container VALUE    name of the data container");
         System.err.println(" -cache VALUE        directory that is the root of the document cache");
         System.err.println(" -output VALUE       the name (and path) of the output file");
         System.err.println(" -template VALUE     path to the Freemarker template for the rendering.");
         System.err.println(" -target VALUE       The URL of the target XBRL instance to render.");
         System.err.println(" The optional additional URLs allow control over the DTS supporting the rendering of the XBRL instance.");
 
         if ("".equals(message)) {
             System.exit(0);
         } else {
             System.exit(1);
         }
     }
 
     /**
      * Create an Oracle Berkeley XML database store and return it cast to an
      * XBRL data store to expose XBRL store enhancements.
      * 
      * @param database
      *            The location to use for the new store.
      * @param container
      *            The name to use for the XML container.
      * @return the new store.
      * @throws XBRLException
      *             if the store cannot be initialised.
      */
     private static XBRLStore createStore(String database, String container)
             throws XBRLException {
         Store store = new StoreImpl(database, container);
         return (XBRLStore) store;
     }
 
     /**
      * @param store
      *            The store to use for the loader.
      * @param cache
      *            The root directory of the document cache.
      * @return the loader to use for loading the instance and its DTS
      * @throws XBRLException
      *             if the loader cannot be initialised.
      */
     private static Loader createLoader(Store store, String cache)
             throws XBRLException {
         XBRLXLinkHandlerImpl xlinkHandler = new XBRLXLinkHandlerImpl();
         XBRLCustomLinkRecogniserImpl clr = new XBRLCustomLinkRecogniserImpl();
         XLinkProcessor xlinkProcessor = new XLinkProcessorImpl(xlinkHandler,
                 clr);
 
         File cacheFile = new File(cache);
 
         // Rivet errors in the SEC XBRL data require these URL remappings to
         // prevent discovery process from breaking.
         HashMap<String, String> map = new HashMap<String, String>();
         map
                 .put(
                         "http://www.xbrl.org/2003/linkbase/xbrl-instance-2003-12-31.xsd",
                         "http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd");
         map
                 .put(
                         "http://www.xbrl.org/2003/instance/xbrl-instance-2003-12-31.xsd",
                         "http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd");
         map
                 .put(
                         "http://www.xbrl.org/2003/linkbase/xbrl-linkbase-2003-12-31.xsd",
                         "http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd");
         map
                 .put(
                         "http://www.xbrl.org/2003/instance/xbrl-linkbase-2003-12-31.xsd",
                         "http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd");
         map.put("http://www.xbrl.org/2003/instance/xl-2003-12-31.xsd",
                 "http://www.xbrl.org/2003/xl-2003-12-31.xsd");
         map.put("http://www.xbrl.org/2003/linkbase/xl-2003-12-31.xsd",
                 "http://www.xbrl.org/2003/xl-2003-12-31.xsd");
         map.put("http://www.xbrl.org/2003/instance/xlink-2003-12-31.xsd",
                 "http://www.xbrl.org/2003/xlink-2003-12-31.xsd");
         map.put("http://www.xbrl.org/2003/linkbase/xlink-2003-12-31.xsd",
                 "http://www.xbrl.org/2003/xlink-2003-12-31.xsd");
 
         EntityResolver entityResolver = new EntityResolverImpl(cacheFile, map);
 
         Loader myLoader = new LoaderImpl(store, xlinkProcessor);
         myLoader.setCache(new CacheImpl(cacheFile));
         myLoader.setEntityResolver(entityResolver);
         xlinkHandler.setLoader(myLoader);
         return myLoader;
     }
 
     /**
      * Helper method to clean up and shut down the data store.
      * 
      * @param store
      *            The store for the XBRL data.
      * @throws XBRLException
      *             if the store cannot be closed.
      */
     private static void cleanup(Store store) throws XBRLException {
         store.close();
     }
 
 }
