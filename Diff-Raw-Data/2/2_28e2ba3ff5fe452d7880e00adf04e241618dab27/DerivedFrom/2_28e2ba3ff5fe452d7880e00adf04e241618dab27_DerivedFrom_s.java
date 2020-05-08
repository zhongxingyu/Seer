 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DerivedFromAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.CachedParser;
 import uk.ac.ebi.fgpt.sampletab.utils.FileGlobIterable;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 public class DerivedFrom {
         
     // logging
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public List<String> coriellSubmissionIDs = Collections.synchronizedList(new ArrayList<String>());
     public List<String> coriellSampleIDs = Collections.synchronizedList(new ArrayList<String>());
     public List<String> coriellSampleAccessions = Collections.synchronizedList(new ArrayList<String>());
     
     private Map<String, SampleNode> sampleAccessiontoNode = new HashMap<String, SampleNode>();
     
     public DerivedFrom(File rootDir){
         log.info("Creating new DerivedFrom instance...");
         
         //hard-coded list of coriell submission identifiers
         coriellSubmissionIDs.add("GCR-ada");
         coriellSubmissionIDs.add("GCR-autism");
         coriellSubmissionIDs.add("GCR-cohort");
         coriellSubmissionIDs.add("GCR-leiomyosarcoma");
         coriellSubmissionIDs.add("GCR-nhgri");
         coriellSubmissionIDs.add("GCR-nia");
         coriellSubmissionIDs.add("GCR-niaid");
         coriellSubmissionIDs.add("GCR-ninds");
         coriellSubmissionIDs.add("GCR-nigms");
         coriellSubmissionIDs.add("GCR-primate");
         coriellSubmissionIDs.add("GCR-winstar");
         coriellSubmissionIDs.add("GCR-yerkes");
 
         for (String coriellID : coriellSubmissionIDs) { 
             File coriellFile = new File(SampleTabUtils.getSubmissionDirFile(coriellID), "sampletab.txt");
            coriellFile = new File(rootDir, coriellFile.getAbsolutePath());
             SampleData sd = null;
             try {
                 sd = CachedParser.get(coriellFile);
             } catch (ParseException e) {
                 log.error("Unable to read "+coriellFile, e);
                 continue;
             }
             
             for (SampleNode s : sd.scd.getNodes(SampleNode.class)) {
                 if (coriellSampleIDs.contains(s.getNodeName())) {
                     log.warn("Duplicate coriell IDs "+s.getNodeName());
                 } else {
                     coriellSampleIDs.add(s.getNodeName());
                     coriellSampleAccessions.add(s.getSampleAccession());
                     sampleAccessiontoNode.put(s.getSampleAccession(), s);
                 }
             }
         }
     }
     
     private Set<String> getHits(String query) {
         Set<String> hits = new HashSet<String>();
         if (coriellSampleIDs.contains(query)) {
             int i = coriellSampleIDs.indexOf(query);
             hits.add(coriellSampleAccessions.get(i));
         }
         return hits;
     }
     
     public SampleData convert(SampleData st) throws IOException {
         //process each sample
         for (SampleNode sample : st.scd.getNodes(SampleNode.class)) {
             Set<String> hits = new HashSet<String>();
             
             hits.addAll(getHits(sample.getNodeName()));
             hits.addAll(getHits("source "+sample.getNodeName()));
             //check if this is a synonym or individual
             for (SCDNodeAttribute a : sample.getAttributes()) {
                 boolean isComment = false;
                 synchronized (CommentAttribute.class) {
                     isComment = CommentAttribute.class.isInstance(a);
                 }
                 boolean isCharacteristic = false;
                 synchronized (CharacteristicAttribute.class) {
                     isCharacteristic = CharacteristicAttribute.class.isInstance(a);
                 }
                 if (isComment) {
                     CommentAttribute coa = (CommentAttribute) a;
                     if (coa.type.toLowerCase().equals("synonym")) {
                         hits.addAll(getHits(coa.getAttributeValue()));
                     }
                 } else if (isCharacteristic) {
                     CharacteristicAttribute cha = (CharacteristicAttribute) a;
                     if (cha.type.toLowerCase().equals("dna-id")) {
                         hits.addAll(getHits(cha.getAttributeValue()));
                     } else if (cha.type.toLowerCase().equals("individual")) {
                         hits.addAll(getHits(cha.getAttributeValue()));
                     } else if (cha.type.toLowerCase().equals("hapmap sample id")) {
                         hits.addAll(getHits(cha.getAttributeValue()));
                     } else if (cha.type.toLowerCase().equals("sample id")) {
                         hits.addAll(getHits(cha.getAttributeValue()));
                     } else if (cha.type.toLowerCase().equals("cell line")) {
                         hits.addAll(getHits(cha.getAttributeValue()));
                     }
                 }
             }
             
             
             //now we have some mappings between this sample and coriell data
             //apply them back to this sampletab
             if (hits.size() > 1) {
                 //if there are more than one hit, see if we can collapse them down
                 
                 //firstly, get the sample nodes that correspond to the hits
                 List<SampleNode> hitSampleNodes = new ArrayList<SampleNode>(hits.size());
                 for (String hitAccession : hits) {
                     SampleNode coriellSample = sampleAccessiontoNode.get(hitAccession);
                     hitSampleNodes.add(coriellSample);
                 }
                 //now we have all the hit sample nodes, see if any of them are derived from each other
                 for (SampleNode coriellSample : hitSampleNodes) {
                     for (Node p : coriellSample.getParentNodes()) {
                         if (SampleNode.class.isInstance(p)) {
                             SampleNode parent = (SampleNode) p;
                             if (hits.contains(parent.getSampleAccession())) {
                                 hits.remove(parent.getSampleAccession());
                             }
                         }
                     }
                 }
             }
             //now we should have removed multi-parent derived from in favor of the most child-like
 
             //check its not a self-hit
             for (Iterator<String> it = hits.iterator(); it.hasNext(); ) {
                 String hit = it.next();
                 if (hit.equals(sample.getSampleAccession())){
                     it.remove();
                 }
             }
             //can actually add attributes now
             //only add one derived from per sample at the moment
             if (hits.size() == 1) {
                 for (String hit : hits) {
                     sample.addAttribute(new DerivedFromAttribute(hit));
                 }
             } else if (hits.size() == 0) {
                 //do nothing
             } else  if (hits.size() > 1) {
                 for(String hit : hits) {
                     log.warn("Multiple derived from detected "+sample.getSampleAccession()+" <- "+hit);
                 }
             }
         }
         
         return st;
     }
 
     public void convert(SampleData sampleIn, Writer writer) throws IOException {
         log.info("recieved sampletab, preparing to convert");
         SampleData sampleOut = convert(sampleIn);
         log.info("sampletab converted, preparing to output");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         log.info("created SampleTabWriter");
         sampletabwriter.write(sampleOut);
         sampletabwriter.close();
 
     }
 
     public void convert(File sampletabFile, Writer writer) throws ParseException, IOException {
         log.info("preparing to load SampleData");
         SampleTabSaferParser stparser = new SampleTabSaferParser();
         log.info("created SampleTabParser<SampleData>, beginning parse");
         SampleData st = stparser.parse(sampletabFile);
         convert(st, writer);
     }
 
     public void convert(File inputFile, String outputFilename) throws ParseException, IOException  {
         convert(inputFile, new File(outputFilename));
     }
 
     public void convert(File inputFile, File outputFile) throws ParseException, IOException {
         convert(inputFile, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, Writer writer) throws ParseException, IOException {
         convert(new File(inputFilename), writer);
     }
 
     public void convert(String inputFilename, File outputFile) throws ParseException, IOException{
         convert(inputFilename, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, String outputFilename) throws ParseException, IOException {
         convert(inputFilename, new File(outputFilename));
     }
 }
