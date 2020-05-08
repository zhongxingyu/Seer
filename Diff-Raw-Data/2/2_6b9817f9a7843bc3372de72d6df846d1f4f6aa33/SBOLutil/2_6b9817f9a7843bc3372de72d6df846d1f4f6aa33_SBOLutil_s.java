 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sbolstandard.libSBOLj;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.Iterator;
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
 
 /**
  * SBOL utils provide read and write methods for interacting with interfaces outside of libSBOLj.
  *
  * The utils include methods for writing SBOL RDF and JSON. Since, the primary
  * goal of SBOL is data/ information exchange/ sharing on the web we are using RDF.
  * SBOL utils also include the methods for exchanging data with other common
  * data formats, such as GenBank flat files using BioJava.
  * @author mgaldzic
  * @since 0.2, 03/2/2011
  */
 public class SBOLutil {
 
     /**
      * Reads the common GenBank flat file so the records in it can be iterated over.
      *
      * GenBank flat format files can have multiple sequence records. Threfore, 
      * fromGenBankFile uses the org.biojavax.bio.seq.RichSequenceIterator to hold
      * the data from a GenBank file so it can be stepped through.
      * #fromRichSequenceIter(org.biojavax.bio.seq.RichSequenceIterator) will do
      * that.
      *
      * @param filename The file path for a GenBank file (eg "test\\test_files\\BFa_8.15.gb")
      * @return Data from the input file as a RichSequenceIterator a BioJava iterator for annotated sequences.
      *         If it cannot find a file it prints a warning, and returns an empty iterator.
      * @throws BioException BioJava threw up, TODO: understand what BioJava exceptions are.
      * @see #fromRichSequenceIter(org.biojavax.bio.seq.RichSequenceIterator)
      */
     public static RichSequenceIterator fromGenBankFile(String filename) throws BioException {
 
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
 
     /**
      * Steps through a RichSequenceIterator and builds up an SBOL Library.
      *
      * The 1 or many GenBank style records, stored as a BioJava object
      * RichSequenceIterator are mapped to a SBOL Library object which can contain
      * many DNA components. The libSBOLj.Library can then be serialized as RDF or
      * Json.
      *
      * @param rsi RichSequenceIterator created by BioJava (eg from GenBank file)
      * @return Library of DNA Components and SequenceFeatures from the input
      * @throws BioException BioJava threw up, TODO: understand what BioJava exceptions are.
      */
     public static Library fromRichSequenceIter(RichSequenceIterator rsi) throws BioException {
         SbolService s = new SbolService();
         
         Library lib = s.createLibrary("BioFabLib_1", "BIOAFAB Pilot Project",
                 "Pilot Project Designs, see http://biofab.org/data");
         while (rsi.hasNext()) {
             RichSequence rs = rsi.nextRichSequence();
             System.out.println("readGB file of: " + rs.getName());
            s.addDnaComponentToLibrary(SbolService.readRichSequence(rs), lib);
         }
         return lib;
     }
 
     /**
      * Maps the BioJava RichSequence object to DnaComponent its DNA Sequence, Annotations, and Features.
      *
      * An individual GenBank record is translated into the SBOL model, by mapping.
      * The GenBank record is mapped to DnaComponent. The Features location information
      * is mapped to SequenceAnnotations of that DnaComponent. The Feature Notes
      * are mapped to SequenceFeatures. Then the DnaComponent gets linked to its
      * annotations and the features.
      * @param rs a RichSequence containing DNA sequence described by features.
      * @return DnaComponent with the attached SequenceAnnotations and SequenceFeatures
      */
     public static DnaComponent readRichSequence(RichSequence rs) {
         SbolService s = new SbolService();
         //The main GenBank Record can be found by the following
         DnaComponent comp = s.createDnaComponent(rs.getName(),
                 rs.getName(), rs.getDescription(), false, "other_DNA",
                 s.createDnaSequence(rs.seqString()));
 
         //Now iterate through the features (all)
         FeatureHolder fh = rs.filter(FeatureFilter.all);
         //System.out.println("Features");
         //DnaComponent compAnotFeat = null;
         for (Iterator<Feature> i = fh.features(); i.hasNext();) {
             RichFeature rf = (RichFeature) i.next();
 
             //Get the location of the feature
             Integer rfStart = rf.getLocation().getMin();
             Integer rfStop = rf.getLocation().getMax();
             String rfStrand = Character.toString(rf.getStrand().getToken());
             SequenceAnnotation anot = s.createSequenceAnnotationForDnaComponent(rfStart,
                     rfStop, rfStrand, comp);
 
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
             // should add return void?
             SequenceAnnotation anotFeat = s.addSequenceFeatureToSequenceAnnotation(feat, anot);
             //compAnotFeat = s.addSequenceAnnotationToDnaComponent(anotFeat, comp);
 
         }
         return comp;
     }
 
 
 
     /**
      * Customizes the Json writer to leave out fields annotated with @SkipInJson.
      *
      * This is needed for the MyExclusionStrategy class
      * TODO: Does this need to be public?
      *
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target({ElementType.FIELD})
     public @interface SkipInJson {
         // Field tag only annotation
     }
 
     /**
      * Writes a Json serialization of a Library.
      *
      * All SBOL information that is found in a Library is written into Json form.
      * Uses the com.google.gson library which walks the SBOL data graph from Library
      * to all its children and outputs a String with all the information inside.
      *
      * @param input an SBOL Library to be written out
      * @return String containing the Json serialization
      */
     public static String toJson(Library input) {
 
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
         Gson gson = new GsonBuilder().setExclusionStrategies(new MyExclusionStrategy(SkipInJson.class)).create();
 
         String aJsonString = gson.toJson(input);
         return aJsonString;
     }
 
     /**
      * Writes a RDF serialization of a Library.
      *
      * All SBOL information that is found in a Library is written into RDF form.
      * Walks the SBOL data graph from Library to all its children and outputs
      * a String with all the RDF information inside.
      *
      * @param input an SBOL Library to be written out
      * @return String containing the RDF serialization
      */
     public static String toRDF(Library input) {
         //make RDF
         SbolService s = new SbolService();
         s.insertLibrary(input);
         String rdfString = s.getAllAsRDF();
         return rdfString;
      
     }
     public static SbolService fromRDF(String rdfString) {
         SbolService s = new SbolService(rdfString);
         return s;
     }
 
 }
