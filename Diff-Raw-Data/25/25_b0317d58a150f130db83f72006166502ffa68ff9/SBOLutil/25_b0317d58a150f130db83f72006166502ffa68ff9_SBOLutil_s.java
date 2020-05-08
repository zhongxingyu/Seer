 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sbolstandard.libSBOLj;
 
 import com.clarkparsia.empire.annotation.InvalidRdfException;
 import com.clarkparsia.empire.annotation.RdfGenerator;
 import com.clarkparsia.openrdf.ExtGraph;
 import com.clarkparsia.openrdf.ExtRepository;
 import com.clarkparsia.openrdf.OpenRdfIO;
 import com.clarkparsia.openrdf.OpenRdfUtil;
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.NoSuchElementException;
 import java.util.logging.Logger;
 import java.util.Iterator;
 import java.util.logging.Level;
 import org.biojava.bio.BioException;
 import org.biojava.bio.seq.Feature;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojavax.Note;
 import org.biojavax.RichAnnotation;
 import org.biojavax.SimpleNamespace;
 import org.biojavax.bio.seq.RichFeature;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.rdfxml.RDFXMLWriter;
 import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
 
 /**
  *
  * @author mgaldzic
  */
 public class SBOLutil {
 
     public RichSequenceIterator fromGenBankFile(String filename) throws BioException {
 
         BufferedReader br = null;
         SimpleNamespace ns = null;
         String fileString = filename;
         RichSequence rs_1 = null;
         try {
             br = new BufferedReader(new FileReader(fileString));
         } catch (FileNotFoundException fnfe) {
             System.out.println("FileNotFoundException: " + fnfe);
         }
         // try {
         ns = new SimpleNamespace("bioJavaNS");
         //Make a biojava.RichSequenceObject
         RichSequenceIterator rsi = RichSequence.IOTools.readGenbankDNA(br, ns);
 
         /**  } catch (Exception be) {
         System.exit(-1);
         }
          */
         return rsi;
     }
 
     public Library fromRichSequenceIter(RichSequenceIterator rsi) throws BioException {
         SbolService s = new SbolService();
         Library lib = new Library();
         lib.setId("BioFabLib_1");
         while (rsi.hasNext()) {
             RichSequence rs = rsi.nextRichSequence();
             System.out.println("readGB file of: " + rs.getName());
             s.addDnaComponentToLibrary(readRichSequence(rs), lib);
         }
         return lib;
     }
 
     public DnaComponent readRichSequence(RichSequence rs) {
         SbolService s = new SbolService();
         //The main GenBank Record can be found by the following
         DnaComponent comp = s.createDnaComponent(rs.getName(),
                 rs.getName(), rs.getDescription(), false, "type",
                 s.createDnaSequence(rs.seqString()));
 
         //Now iterate through the features (all)
         FeatureHolder fh = rs.filter(FeatureFilter.all);
         //System.out.println("Features");
         DnaComponent compAnotFeat = null;
         for (Iterator<Feature> i = fh.features(); i.hasNext();) {
             RichFeature rf = (RichFeature) i.next();
 
             //Get the location of the feature
             Integer rfStart = rf.getLocation().getMin();
             Integer rfStop = rf.getLocation().getMax();
             String rfStrand = Character.toString(rf.getStrand().getToken());
             SequenceAnnotation anot = s.createSequenceAnnotation(rfStart,
                     rfStop, rfStrand);
 
             //Get the Rich Annotation of the Rich Feature
             RichAnnotation ra = (RichAnnotation) rf.getAnnotation();
 
             String label = "";
             //Iterate through the notes in the Rich Annotation
             for (Iterator<Note> it = ra.getNoteSet().iterator(); it.hasNext();) {
                 Note n = it.next();
                 String key = n.getTerm().getName();
                 String value = n.getValue();
                 //int rank = n.getRank();
                 // print the qualifier out in key=value (rank) format
                 //System.out.println(key+"="+value+" ("+rank+")");
                 if (key.equals("label") || key.equals("gene")) {
                     label = value;
                 } else {
                     label = "misc";
                 }
             }
             SequenceFeature feat = s.createSequenceFeature(label, label, label, rf.getType());
             SequenceAnnotation anotFeat = s.addSequenceFeatureToSequenceAnnotation(feat, anot);
             compAnotFeat = s.addSequenceAnnotationToDnaComponent(anotFeat, comp);
 
         }
         return compAnotFeat;
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target({ElementType.FIELD})
     public @interface SkipInJson {
         // Field tag only annotation
     }
 
     public String toJson(Library input) {
 
         // converting to JSON
         //add this type to skip: SupportsRdfId
 
         class MyExclusionStrategy implements ExclusionStrategy {
 
             private final Class<?> typeToSkip;
 
             private MyExclusionStrategy(Class<?> typeToSkip) {
                 this.typeToSkip = typeToSkip;
             }
 
             public boolean shouldSkipClass(Class<?> clazz) {
                 return (clazz == typeToSkip);
             }
 
             public boolean shouldSkipField(FieldAttributes f) {
                 return f.getAnnotation(SkipInJson.class) != null;
             }
         }
         Gson gson = new GsonBuilder().setExclusionStrategies(new MyExclusionStrategy(String.class)).create();
 
         String aJsonString = gson.toJson(input);
         return aJsonString;
     }
 
     public String toRDF(Library input) {
         ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();
         ExtGraph aGraph;
         String rdfString = null;
         //make RDF
         try {
             aGraph = RdfGenerator.asRdf(input);
             for (Iterator<DnaComponent> dci = input.getComponents().iterator(); dci.hasNext();) {
                 DnaComponent aDc = dci.next();
                 aGraph.add(RdfGenerator.asRdf(aDc));
                 for (Iterator<SequenceAnnotation> sai = aDc.getAnnotations().iterator(); sai.hasNext();) {
                     SequenceAnnotation aSa = sai.next();
                     System.out.println("sai id:" + aSa.getId());
                     aGraph.add(RdfGenerator.asRdf(aSa));
                 }
             }
             //rdfString = "t";//aGraph.toString();
             try {
                 aRepo.addGraph(aGraph);
                 RepositoryConnection con = aRepo.getConnection();

                RDFXMLPrettyWriter rdfWriter = new RDFXMLPrettyWriter(System.out);
                 try {
                     try {
                         con.prepareGraphQuery(QueryLanguage.SERQL, "CONSTRUCT * FROM {x} p {y}").evaluate(rdfWriter);
                         //TurtleWriter turtleWriter = new TurtleWriter(System.out);
                         //TurtleWriter turtleWriter = new TurtleWriter(System.out);
                     } catch (QueryEvaluationException ex) {
                         Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (RDFHandlerException ex) {
                         Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (MalformedQueryException ex) {
                     Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } catch (RepositoryException ex) {
                 Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, "addGraph", ex);
             }
             OpenRdfIO.writeGraph(aGraph, new OutputStreamWriter(new FileOutputStream("data\\output.ttl")), RDFFormat.TURTLE);
             OpenRdfIO.writeGraph(aGraph, new OutputStreamWriter(new FileOutputStream("data\\output.rdf")), RDFFormat.RDFXML);
 
         } catch (IOException ex) {
             Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, "toRDF IO Ex", ex);
         } catch (InvalidRdfException ex) {
             Logger.getLogger(SBOLutil.class.getName()).log(Level.SEVERE, "toRDF Invalid RDF", ex);
         }
         return rdfString;
         /*
 
         ExtRepository theRepo = OpenRdfUtil.createInMemoryRepo();
         ExtGraph theGraph;
 
         try {
         theGraph = OpenRdfIO.readGraph(new InputStreamReader(new FileInputStream("data\\input.rdf")), RDFFormat.RDFXML);
         try {
         theRepo.addGraph(theGraph);
         } catch (RepositoryException ex) {
         Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
         } catch (RDFParseException ex) {
         Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }*/
     }
 }
