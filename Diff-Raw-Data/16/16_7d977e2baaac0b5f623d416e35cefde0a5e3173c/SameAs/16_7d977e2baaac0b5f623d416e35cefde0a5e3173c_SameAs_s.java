 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
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
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SameAsAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.CachedParser;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 public class SameAs {
     
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
     
     public static void main(String[] args) {
         new SameAs().doMain(args);
 
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
         
     public SampleData convert(SampleData st) throws IOException {
         for (SampleNode s : st.scd.getNodes(SampleNode.class)) {
             //itterate over a copy so we can add attributes as we go
             for (SCDNodeAttribute a: new ArrayList<SCDNodeAttribute>(s.getAttributes())){
                 //Do AE=AE equivalence
                 if (a.getAttributeType().equals("comment[Source EXP]")){
                     File sourceFile = getFile("GA"+a.getAttributeValue());
                     
                     SampleData sd = null;
                     log.debug("reading "+sourceFile);
                     try {
                         sd = CachedParser.get(sourceFile);
                     } catch (ParseException e) {
                         log.error("Unable to read "+sourceFile);
                         e.printStackTrace();
                         continue;
                     }
                     
                     if (sd != null) {
                         //try to get a sample with the same name as sample of interest
                         String sampleName = s.getNodeName();
                         sampleName = sampleName.replace(a.getAttributeValue()+":", "");
                         log.debug("looking for sample "+sampleName);
                         
                         SampleNode t = sd.scd.getNode(sampleName, SampleNode.class);
                         if (t != null) {
                             s.addAttribute(new SameAsAttribute(t.getSampleAccession()));
                         }
                     }
                 }
                 //Do AE=SRA equivalence
                 if (a.getAttributeType().equals("comment[ENA_SAMPLE]")){
                     //TODO finish this after SRA=SRA is solved
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
        log.info("created MAGETABParser<SampleData>, beginning parse");
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
