 package uk.ac.ebi.fgpt.sampletab.other;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.AbstractDriver;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 
 public class TCGA extends AbstractDriver {
 
     @Argument(required=true, index=0, metaVar="OUTPUT", usage = "output filename")
     private String outputFilename;
 
     @Option(name = "--clinical", usage = "input clinical filename")
     protected String inputClinicalFilename = null;
 
     @Option(name = "--biospecimen",  usage = "input biospecimen filename")
     protected String inputBioSpecimenFilename = null;
     
     @Option(name = "--abbreviation")
     protected String abbreviation = null;
     
     private Logger log = LoggerFactory.getLogger(getClass());
     private final SampleData st;
 
     TermSource ncbitaxonomy = new TermSource("NCBI Taxonomy", "http://www.ncbi.nlm.nih.gov/taxonomy", null);
     
     
     public static void main(String[] args) {
         new TCGA().doMain(args);
     }
 
     private TCGA() {
         st = new SampleData();
     }
     
     private void addAttribute(SampleNode sample, String key, String value) {
         key = key.trim();
         value = value.trim();
         
         if (value.contains("|")) {
             for (String subvalue : value.split("\\|")) {
                 addAttribute(sample, key, subvalue);
             }
         } else {
             if (value.equals("[Not Available]") 
                     || value.equals("[Not Applicable]")
                     || value.equals("null")
                     || value.endsWith("Not Tested")) {
                 value = "";
             }
             key = key.replace("_", " ");
             
             if (key != null && key.length() > 0
                     && value != null && value.length() > 0) {
                 if (key == "gender"){
                     sample.addAttribute(new SexAttribute(value));
                 } else {
                     sample.addAttribute(new CommentAttribute(key, value));
                 }
             }
         }
     }
     
     private void doClinical() {
         if (inputClinicalFilename == null) {
             return;
         }
         File inputFile = new File(inputClinicalFilename);
         if (!inputFile.exists() || !inputFile.isFile()) {
             log.error("Unable to read "+inputFile);
             return;
         }
         
         CSVReader reader = null;
         int linecount;
         String [] nextLine;
         String[] headers = null;
         try {
             reader = new CSVReader(new FileReader(inputFile), "\t".charAt(0));
             linecount = 0;
             while ((nextLine = reader.readNext()) != null) {
                 linecount += 1;
                 if (linecount % 10000 == 0){
                     log.info("processing line "+linecount);
                 }
                                 
                 if (headers == null || linecount == 0) {
                     headers = nextLine;
                 } else {
                     Map<String, String> line = new HashMap<String, String>();
                     for (int i = 0; i < nextLine.length; i++) {
                         line.put(headers[i], nextLine[i]);
                     }
                     SampleNode sample;
                     sample = st.scd.getNode(line.get("bcr_patient_barcode"), SampleNode.class);
                     if (sample == null) {
                         sample = new SampleNode(line.get("bcr_patient_barcode"));
                         st.scd.addNode(sample);
                     }
                     //mark the sample as human
                     sample.addAttribute(new OrganismAttribute("Homo sapiens", st.msi.getOrAddTermSource(ncbitaxonomy), 9606));
                     
                     //add all the other columns
                     for (int i = 1; i < headers.length && i < nextLine.length; i++){
                         String key = headers[i];
                         String value = nextLine[i];
                         addAttribute(sample, key, value);
                     }
                 }
             }
             reader.close();
         } catch (FileNotFoundException e) {
             log.error("Error processing "+inputFile, e);
         } catch (IOException e) {
             log.error("Error processing "+inputFile, e);
         } catch (ParseException e) {
             log.error("Error building Sampledata", e);
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (IOException e) {
                 //do nothing
             }
         }
     }
     
     private void setSampleType(SampleNode sample, String code){
         //these are hard coded by tcga
         //see the url below for an up-to-date list
         //https://tcga-data.nci.nih.gov/datareports/codeTablesReport.htm?codeTable=sample%20type
         Integer codeID = new Integer(code);
         if (codeID == 1) { sample.addAttribute(new CommentAttribute("sample type", "primary solid tumor"));
         } else if (codeID == 2) { sample.addAttribute(new CommentAttribute("sample type", "recurrent solid tumor"));
         } else if (codeID == 3) { sample.addAttribute(new CommentAttribute("sample type", "primary blood derived cancer - peripheral blood"));
         } else if (codeID == 4) { sample.addAttribute(new CommentAttribute("sample type", "recurrent blod derived cancer - bone marrow"));
         } else if (codeID == 5) { sample.addAttribute(new CommentAttribute("sample type", "additional - new primary"));
         } else if (codeID == 6) { sample.addAttribute(new CommentAttribute("sample type", "metastatic"));
         } else if (codeID == 7) { sample.addAttribute(new CommentAttribute("sample type", "additional metastatic"));
         } else if (codeID == 8) { sample.addAttribute(new CommentAttribute("sample type", "human tumor original cells"));
         } else if (codeID == 9) { sample.addAttribute(new CommentAttribute("sample type", "primary blood derived cancer - bone marrow"));
         } else if (codeID == 10) { sample.addAttribute(new CommentAttribute("sample type", "blood derived normal"));
         } else if (codeID == 11) { sample.addAttribute(new CommentAttribute("sample type", "solid tissue normal"));
         } else if (codeID == 12) { sample.addAttribute(new CommentAttribute("sample type", "buccal cell normal "));
         } else if (codeID == 13) { sample.addAttribute(new CommentAttribute("sample type", "EBV immortalized normal"));
         } else if (codeID == 14) { sample.addAttribute(new CommentAttribute("sample type", "bone marrow normal"));
         } else if (codeID == 20) { sample.addAttribute(new CommentAttribute("sample type", "control analyte"));
         } else if (codeID == 40) { sample.addAttribute(new CommentAttribute("sample type", "recurrent blood derived cancer - peripheral blood"));
         } else if (codeID == 50) { sample.addAttribute(new CommentAttribute("sample type", "cell lines"));
         } else if (codeID == 60) { sample.addAttribute(new CommentAttribute("sample type", "primary xenograft tissue"));
         } else if (codeID == 61) { sample.addAttribute(new CommentAttribute("sample type", "cell line derived xenograft tissue"));
         } else {
             log.warn("unrecognized sample type "+code);
         }
         
     }
     
     private void doBioSpecimen() {
         if (inputBioSpecimenFilename == null) {
             return;
         }
         File inputFile = new File(inputBioSpecimenFilename);
         if (!inputFile.exists() || !inputFile.isFile()) {
             log.error("Unable to read "+inputFile);
             return;
         }
         
         CSVReader reader = null;
         int linecount;
         String [] nextLine;
         String[] headers = null;
         try {
             reader = new CSVReader(new FileReader(inputFile), "\t".charAt(0));
             linecount = 0;
             while ((nextLine = reader.readNext()) != null) {
                 linecount += 1;
                 if (linecount % 10000 == 0){
                     log.info("processing line "+linecount);
                 }
                                 
                 if (headers == null || linecount == 0) {
                     headers = nextLine;
                 } else {
                     
                     String sampleName = nextLine[0];
                     SampleNode sample = st.scd.getNode(sampleName, SampleNode.class);
                     if (sample == null) {
                         sample = new SampleNode(sampleName);
                         st.scd.addNode(sample);
                     }
                     //see if we can find a parent node
                     String[] nameParts = sampleName.split("-");
                     String parentName = nameParts[0];
                     for (int i = 1; i < nameParts.length-1; i++) {
                         parentName = parentName+"-"+nameParts[i];
                     }
                     SampleNode parent = st.scd.getNode(parentName, SampleNode.class);
                     if (parent != null) {
                         parent.addChildNode(sample);
                         sample.addParentNode(parent);
                     } else {
                         //no parent, so mark the sample as human
                         sample.addAttribute(new OrganismAttribute("Homo sapiens", st.msi.getOrAddTermSource(ncbitaxonomy), 9606));
                     }
                     
                     //add all the other columns
                     for (int i = 1; i < headers.length; i++){
                         String key = headers[i];
                         String value = nextLine[i];
                         if (key.equals("sample_type_id")){
                             setSampleType(sample, value);
                         } else {
                             addAttribute(sample, key, value);
                         }
                     }
                 }
             }
             reader.close();
         } catch (FileNotFoundException e) {
             log.error("Error processing "+inputFile, e);
         } catch (IOException e) {
             log.error("Error processing "+inputFile, e);
         } catch (ParseException e) {
             log.error("Error building Sampledata", e);
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (IOException e) {
                 //do nothing
             }
         }
         
     }
     
     protected void doMain(String[] args) {
         super.doMain(args);
         
         if (abbreviation != null) {
             //no per-sample links, only per project
            st.msi.databases.add(new Database("The Cancer Genome Atlas", abbreviation, "https://tcga-data.nci.nih.gov/tcga/tcgaCancerDetails.jsp?diseaseType="+abbreviation));
         }
         
         //mark as reference layer
         st.msi.submissionReferenceLayer = true;
         
         st.msi.submissionIdentifier = "GCG-"+abbreviation;
         
         doClinical();
         
         doBioSpecimen();
 
         if (st.scd.getAllNodes().size() == 0) {
             log.error("No nodes created, aborting");
         }
         
         //write output
         SampleTabWriter writer = null ; 
         File outputFile = new File(outputFilename, SampleTabUtils.getSubmissionDirPath(st.msi.submissionIdentifier));
         outputFile.mkdirs();
         outputFile = new File(outputFile, "sampletab.pre.txt");
         log.info("writing "+outputFile);
         try {
             writer = new SampleTabWriter(new BufferedWriter(new FileWriter(outputFile)));
             writer.write(st);
             writer.close();
         } catch (IOException e) {
             log.error("Error writing to "+outputFile, e);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     //do nothing
                 }
             }
         }
     }
 }
