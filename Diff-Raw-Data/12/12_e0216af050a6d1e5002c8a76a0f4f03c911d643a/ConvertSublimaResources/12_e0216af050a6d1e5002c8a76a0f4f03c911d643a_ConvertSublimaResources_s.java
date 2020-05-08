 package com.computas.sublima.app.adhoc;
 // for alt som er ressurser, erstatt URIen med URLen.
 
 import com.computas.sublima.query.service.SettingsService;
 
 
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
 import com.hp.hpl.jena.reasoner.rulesys.Rule;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.util.PrintUtil;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class ConvertSublimaResources {
 
   private static Logger logger = Logger.getLogger(ConvertSublimaResources.class);
 
   public static void main(String[] args) {
     String argument = "";
     String inputfilename = null;
     String inputformat = "";
     String outputfilename = null;
     String outputformat = "";
     String replaceResourceWith = null;
     String usageString = "usage: ConvertSublimaResource [-r urlOrUri] -input filename format -output filename format ";
 
     for (int i = 0; i < args.length; i++) {
       argument = args[i];
 
       if (argument.equals("-input")) {
         if (i < args.length + 1) {
           inputfilename = args[i + 1];
           inputformat = args[i + 2];
         } else {
           System.err.println("-input requires a filename and format");
           System.err.println(usageString);
           return;
         }
       }
 
       if (argument.equals("-output")) {
         if (i < args.length + 1) {
           outputfilename = args[i + 1];
           outputformat = args[i + 2];
         } else {
           System.err.println("-output requires a filename and format");
           System.err.println(usageString);
           return;
         }
       }
 
 
       if (argument.equals("-r")) {
         if (i < args.length + 1) {
           if (args[i + 1].toLowerCase().equals("url")) {
             replaceResourceWith = "url";
           } else if (args[i + 1].toLowerCase().equals("uri")) {
             replaceResourceWith = "uri";
           } else {
             System.err.println("-r requires either \"url\" or \"uri\"");
             System.err.println(usageString);
           }
         }
       }
     }
 
     if (outputfilename == null || inputfilename == null) {
       System.err.println(usageString);
       return;
     }
 
 
     try {
       convert(inputfilename, inputformat, outputfilename, outputformat, replaceResourceWith);
     } catch (FileNotFoundException fe) {
       System.err.println(fe.toString());
       System.err.println(usageString);
       return;
     } catch (IOException fe) {
       System.err.println(fe.toString());
       System.err.println(usageString);
       return;
     }
     return;
   }
 
   /**
    * Converts a model from resources represented as URLs to resources represented as URIs and visa versa. Unlike convert() this handles the model in memory.
    */
   public static Model convertModel(Model model, String replaceResourceWith) throws IOException {
     if (replaceResourceWith != null) {
 
       Property p = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
       Resource o = model.getResource("http://xmlns.computas.com/sublima#Resource");
       StmtIterator iter = model.listStatements(null, p, o);
 
       List list = iter.toList();
       for (int i = 0; i < list.size(); i++) {
 
         Statement stmt = (Statement) list.get(i); // get next statement
         Resource subject = stmt.getSubject();     // get the subject
         Property predicate = stmt.getPredicate();   // get the predicate
         RDFNode object = stmt.getObject();      // get the object
         System.out.println(subject.toString());
 
         String rep = null;
         if (replaceResourceWith.equals("url")) {
 
           // rename all statements about this resource to its sub:url
           Property urlProp = model.getProperty("http://xmlns.computas.com/sublima#url");
           NodeIterator nIter = model.listObjectsOfProperty(subject, urlProp);
 
           try {
             rep = nIter.nextNode().toString();
           } catch (NoSuchElementException e) {
             logger.warn("ConvertSublimaResources.convert() --> Resource " + subject.toString() + " has no property sublima:url");
           }
           renameSubject(subject, rep);
         } else {
 
           // rename all statements about this resource to its dct:identifier
           Property identifierProp = model.getProperty("http://purl.org/dc/terms/identifier");
           NodeIterator nIter = model.listObjectsOfProperty(subject, identifierProp);
           try {
             rep = nIter.nextNode().toString();
           } catch (NoSuchElementException e) {
             logger.warn("ConvertSublimaResources.convert() --> Resource " + subject.toString() + " has no property dct:identifier");
           }
           renameSubject(subject, rep);
         }
 
         System.out.println("Renamed " + subject.toString() + " to " + rep);
       }
     }
 
     // Add skos inverse triples
     Model outModel = addByRules(model);
     return outModel;
 
   }
 
 
   /**
    * Converts from resources represented as URLs to resources represented as URIs and visa versa.
    */
   public static void convert(String inputFileName, String inFormat, String outputFileName, String outFormat, String replaceResourceWith) throws FileNotFoundException, java.io.IOException {
 
     //System.out.println(inputFileName + " " + inFormat + " " + outputFileName + " " + outFormat + " " + replaceResourceWith);
 
     // create an empty model
     Model model = ModelFactory.createDefaultModel();
 
     // use the FileManager to find the input file
     InputStream in = FileManager.get().open(inputFileName);
     if (in == null) {
       throw new FileNotFoundException("File: " + inputFileName + " not found");
     }
 
     // read the RDF file
     RDFReader rdr = model.getReader(inFormat);
     File f = new File(inputFileName);
     String base = f.toString(); //"file:///" + f.getCanonicalPath().replace('\\', '/');
     rdr.read(model, in, base);
 
     if (replaceResourceWith != null) {
 
       Property p = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
       Resource o = model.getResource("http://xmlns.computas.com/sublima#Resource");
       StmtIterator iter = model.listStatements(null, p, o);
 
       List list = iter.toList();
       for (int i = 0; i < list.size(); i++) {
 
         Statement stmt = (Statement) list.get(i); // get next statement
         Resource subject = stmt.getSubject();     // get the subject
         Property predicate = stmt.getPredicate();   // get the predicate
         RDFNode object = stmt.getObject();      // get the object
         System.out.println(subject.toString());
 
         String rep = null;
         if (replaceResourceWith.equals("url")) {
 
           // rename all statements about this resource to its sub:url
           Property urlProp = model.getProperty("http://xmlns.computas.com/sublima#url");
           NodeIterator nIter = model.listObjectsOfProperty(subject, urlProp);
 
           try {
             rep = nIter.nextNode().toString();
           } catch (NoSuchElementException e) {
             logger.warn("ConvertSublimaResources.convert() --> Resource " + subject.toString() + " has no property sublima:url");
           }
           renameSubject(subject, rep);
         } else {
 
           // rename all statements about this resource to its dct:identifier
           Property identifierProp = model.getProperty("http://purl.org/dc/terms/identifier");
           NodeIterator nIter = model.listObjectsOfProperty(subject, identifierProp);
           try {
             rep = nIter.nextNode().toString();
           } catch (NoSuchElementException e) {
             logger.warn("ConvertSublimaResources.convert() --> Resource " + subject.toString() + " has no property dct:identifier");
           }
           renameSubject(subject, rep);
         }
 
         System.out.println("Renamed " + subject.toString() + " to " + rep);
       }
     }
 
     // Add skos inverse triples etc. Rules are specified in the property file.
     Model outModel = addByRules(model);
 
     // write the RDF file
     OutputStream out = new FileOutputStream(outputFileName);
     outModel.write(out, outFormat, base);
 
     outModel.close();
     return;
   }
 
 
   /**
    * Similar to ResourceUtils.renameResource, but only works on subjects of a statement.
    */
   private static Resource renameSubject(Resource old, String uri) {
     Model m = old.getModel();
     List stmts = new ArrayList();
 
     // list the statements that mention old as a subject
     for (Iterator i = old.listProperties(); i.hasNext(); stmts.add(i.next())) ;
 
     // create a new resource to replace old
     Resource res = (uri == null) ? m.createResource() : m.createResource(uri);
 
     // now move the statements to refer to res instead of old
     for (Iterator i = stmts.iterator(); i.hasNext();) {
       Statement s = (Statement) i.next();
 
       s.remove();
 
       Resource subj = s.getSubject().equals(old) ? res : s.getSubject();
       RDFNode obj = s.getObject();
 
       m.add(subj, s.getPredicate(), obj);
     }
 
     return res;
   }
 
 
   /**
    * Add skos:broader and skos:narrower where appropriate using rules
    */
   private static Model addByRules(Model model) {
 
     String skosNs = "http://www.w3.org/2004/02/skos/core#";
     String dctNs = "http://purl.org/dc/terms/";
     String subNs = "http://xmlns.computas.com/sublima#";
     String rdfNs = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     String wdrNs = "http://www.w3.org/2007/05/powder#";
 
     PrintUtil.registerPrefix("skos", skosNs);
     PrintUtil.registerPrefix("dct", dctNs);
     PrintUtil.registerPrefix("sub", subNs);
     PrintUtil.registerPrefix("rdf", rdfNs);
     PrintUtil.registerPrefix("wdr", wdrNs);
 
    String rules = SettingsService.getProperty("sublima.import.rules");
    logger.info("Applying rules " + rules);
    /*String rules = "[inverseRule1: (?Y skos:broader ?X) -> (?X skos:narrower ?Y)]" +
             "[inverseRule2: (?X skos:narrower ?Y) -> (?Y skos:broader ?X)]" +
            "[statusRule: (?X a sub:Resource) -> (?X wdr:DescribedBy <http://sblima.computas.com/status/godkjent_av_administrator>)];*/
 
     Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
     InfModel inf = ModelFactory.createInfModel(reasoner, model);
 
     return inf;
   }
 
 
 }  
