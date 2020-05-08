 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
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
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DerivedFromAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.CachedParser;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 public class DerivedFrom {
     
     @Option(name = "--sampletab-path", usage = "SampleTab path")
     private String stPath = ".";
     
     @Option(name = "--output", usage = "output filename")
     private String outputFilename;
 
     @Argument
     private List<String> arguments = new ArrayList<String>();
 
     @Option(name = "-h", usage = "display help")
     private boolean help;
     
     // logging
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public List<String> coriellSubmissionIDs = null;
     public List<String> coriellSampleIDs = new ArrayList<String>();
     public List<String> coriellSampleAccessions = new ArrayList<String>();
     
     private Map<String, SampleNode> sampleAccessiontoNode = new HashMap<String, SampleNode>();
     
     
     public DerivedFrom(){
         log.info("Creating new DerivedFrom instance...");
     }
     
     public File getFile(String submissionID){
         File stPathFile = new File(stPath);
         
         File subdir = null;
         if (submissionID.startsWith("GCR-")){
             subdir = new File(stPathFile, "coriell"); 
         }
         else if (submissionID.startsWith("GEN-")){
             subdir = new File(stPathFile, "sra"); 
         }
         else if (submissionID.startsWith("GEN")){
             subdir = new File(stPathFile, "encode"); 
         }
         else if (submissionID.startsWith("G1K")){
             subdir = new File(stPathFile, "g1k"); 
         }
         else if (submissionID.startsWith("GMS-")){
             subdir = new File(stPathFile, "imsr"); 
         }
         else if (submissionID.startsWith("GVA-")){
             subdir = new File(stPathFile, "dgva"); 
         }
         else if (submissionID.startsWith("GAE-")){
             subdir = new File(stPathFile, "ae"); 
         }
         else if (submissionID.startsWith("GPR-")){
             subdir = new File(stPathFile, "pride"); 
         }
         
         File subsubdir = new File(subdir, submissionID);
         File sampletab = new File(subsubdir, "sampletab.txt");
         
         return sampletab;
     }
     
     public void setup() {
         if (coriellSubmissionIDs == null){
             log.info("Running setup()...");
             coriellSubmissionIDs = new ArrayList<String>();
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
             
 
             for (String coriellID : coriellSubmissionIDs){
                 File coriellFile = getFile(coriellID);
                 SampleData sd = null;
                 try {
                     sd = CachedParser.get(coriellFile);
                 } catch (ParseException e) {
                     log.error("Unable to read "+coriellFile);
                     e.printStackTrace();
                     continue;
                 }
                 
                 for (SampleNode s : sd.scd.getNodes(SampleNode.class)) {
                     if (coriellSampleIDs.contains(s.getNodeName())){
                         log.warn("Duplicate coriell IDs "+s.getNodeName());
                     } else {
                         coriellSampleIDs.add(s.getNodeName());
                         coriellSampleAccessions.add(s.getSampleAccession());
                         sampleAccessiontoNode.put(s.getSampleAccession(), s);
                     }
                 }
             }
         }
     }
     
     public SampleData convert(SampleData st) throws IOException {
         setup();
         for (SampleNode sample : st.scd.getNodes(SampleNode.class)){
             Set<String> hits = new HashSet<String>();
             
             if (coriellSampleIDs.contains(sample.getNodeName())){
                 int i = coriellSampleIDs.indexOf(sample.getNodeName());
                 hits.add(coriellSampleAccessions.get(i));
             }
             for (SCDNodeAttribute attribute: sample.getAttributes()){
                 if (coriellSampleIDs.contains(attribute.getAttributeValue())){
                     int i = coriellSampleIDs.indexOf(attribute.getAttributeValue());
                     if (!coriellSampleAccessions.get(i).equals(sample.getSampleAccession())){
                         hits.add(coriellSampleAccessions.get(i));
                     }
                 }
             }
             //now we have some mappings between this sample and coriell data
             //apply them back to this sampletab
             if (hits.size() > 1){
                 //if there are more than one hit, see if we can collapse them down
                 
                 //firstly, get the sample nodes that correspond to the hits
                 List<SampleNode> hitSampleNodes = new ArrayList<SampleNode>(hits.size());
                 for(String hitAccession : hits) {
                     SampleNode coriellSample = sampleAccessiontoNode.get(hitAccession);
                     hitSampleNodes.add(coriellSample);
                 }
                 //now we have all the hit sample nodes, see if any of them are derived from each other
                 for(SampleNode coriellSample : hitSampleNodes){
                     for (Node p : coriellSample.getParentNodes()){
                         if (SampleNode.class.isInstance(p)){
                             SampleNode parent = (SampleNode) p;
                             if (hits.contains(parent.getSampleAccession())){
                                 hits.remove(parent.getSampleAccession());
                             }
                         }
                     }
                 }
             }
             //now we should have removed multi-parent derived from in favor of the most child-like
             //can actually add attributes now
             for(String hit : hits){
                 //check its not a self-hit
                 if (!hit.equals(sample.getSampleAccession())){
                     sample.addAttribute(new DerivedFromAttribute(hit));
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
         SampleTabParser<SampleData> stparser = new SampleTabParser<SampleData>();
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
 
 
 
     
     public static void main(String[] args) {
         new DerivedFrom().doMain(args);
 
     }
     public void doMain(String[] args){
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
 
         System.out.println("Starting...");
 
         for (String inputFilename : arguments) {
             log.trace("inputFilename: "+inputFilename);
             for (File inputFile : FileUtils.getMatchesGlob(inputFilename)) {
                 log.trace("inputFile: "+inputFile);
 
                 File outputFile = new File(inputFile.getParentFile(), outputFilename);
                 if (!outputFile.exists() 
                         || outputFile.lastModified() < inputFile.lastModified()) {
                 
                     try {
                         convert(inputFile, outputFile);
                     } catch (ParseException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 
 }
