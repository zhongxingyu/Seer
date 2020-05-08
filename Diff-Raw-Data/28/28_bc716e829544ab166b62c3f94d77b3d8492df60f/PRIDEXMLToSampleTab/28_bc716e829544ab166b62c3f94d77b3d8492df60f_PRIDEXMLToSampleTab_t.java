 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class PRIDEXMLToSampleTab {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     //TODO make required
     //TODO make multiple
     @Option(name = "-i", aliases={"--input"}, usage = "input filename")
     private String inputFilename;
 
     //TODO make required
     @Option(name = "-o", aliases={"--output"}, usage = "output filename")
     private String outputFilename;
 
     @Option(name = "-p", aliases={"--projects"}, usage = "projects filename")
     private String projectsFilename;
 
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
         
     public PRIDEXMLToSampleTab() {
     }
     
 
     public SampleData convert(Set<File> infiles) throws DocumentException, FileNotFoundException {
         
         SampleData st = new SampleData();
         
         for (File infile : infiles){
             Element expcollection = XMLUtils.getDocument(infile).getRootElement();
             Element exp = XMLUtils.getChildByName(expcollection, "Experiment");
             Element additional = XMLUtils.getChildByName(exp, "additional");
             Element description = XMLUtils.getChildByName(exp, "description");
             Element admin = XMLUtils.getChildByName(description, "admin");
             Element sampledescription = XMLUtils.getChildByName(admin, "sampleDescription");
             String accession = XMLUtils.getChildByName(exp, "ExperimentAccession").getTextTrim();
             
             if (st.msi.submissionTitle == null)
                 st.msi.submissionTitle = XMLUtils.getChildByName(exp, "Title").getTextTrim();
             //PRIDE dont have submission description
             //actually maybe it does as a CVparam...
             st.msi.submissionReferenceLayer = false;
             
             
             for (Element contact : XMLUtils.getChildrenByName(admin, "contact")){
                 String name = XMLUtils.getChildByName(exp, "name").getTextTrim();
                 String institution = XMLUtils.getChildByName(exp, "institution").getTextTrim();
                 if (!st.msi.organizationName.contains(institution))
                     st.msi.organizationName.add(institution);
                 String[] splitnames = PRIDEutils.splitName(name);
                 //TODO check does not already exist?
                 st.msi.personFirstName.add(splitnames[0]);
                 st.msi.personInitials.add(splitnames[1]);
                 st.msi.personLastName.add(splitnames[2]);
                 st.msi.personEmail.add("");
                 st.msi.personRole.add("Submitter");
             }
             
             for (Element reference : XMLUtils.getChildrenByName(exp, "Reference")){
                 for (Element referenceadditional : XMLUtils.getChildrenByName(reference, "additional")){
                     for (Element referenceaddpar : XMLUtils.getChildrenByName(referenceadditional, "cvParam")){
                         if (referenceaddpar.attributeValue("cvLabel").trim().equals("PubMed")){
                             //some PubMed IDs have full URLs, strip them
                             String pubmedid = referenceaddpar.attributeValue("accession").trim();
                             pubmedid.replace("http://www.ncbi.nlm.nih.gov/pubmed/", "");
                             //check to avoid duplicates
                             if (!st.msi.publicationPubMedID.contains(pubmedid)){
                                 st.msi.publicationPubMedID.add(pubmedid);
                                 st.msi.publicationDOI.add("");
                             }
                         }
                     }
                 }
             }
             
             SampleNode sample = new SampleNode();
             sample.setNodeName("GPR-"+accession);
             
             DatabaseAttribute dbattr = new DatabaseAttribute("PRIDE", accession, "http://www.ebi.ac.uk/pride/showExperiment.do?value="+accession);
             sample.addAttribute(dbattr);
 
             for (Element cvparam : XMLUtils.getChildrenByName(sampledescription, "cvParam")){
                 String name = cvparam.attributeValue("name").trim();
                String value = cvparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                } else {
                    value = value.trim();
                 }
                 //TODO  use special attribute classes where appropriate
                 CharacteristicAttribute attr = new CharacteristicAttribute(name, value);
                 if (cvparam.attributeValue("cvLabel") != null && cvparam.attributeValue("accession") != null){
                     attr.termSourceREF = cvparam.attributeValue("cvLabel").trim();
                     //TODO make sure that this term source is then added to msi section
                     attr.termSourceID = cvparam.attributeValue("accession").trim();
                 }
                 sample.addAttribute(attr);
             }
             
             for (Element cvparam : XMLUtils.getChildrenByName(additional, "cvParam")){
                 String name = cvparam.attributeValue("name").trim();
                String value = cvparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                } else {
                    value = value.trim();
                 }
                 CharacteristicAttribute attr = new CharacteristicAttribute(name, value);
                 if (cvparam.attributeValue("cvLabel") != null && cvparam.attributeValue("accession") != null){
                     attr.termSourceREF = cvparam.attributeValue("cvLabel").trim();
                     //TODO make sure that this term source is then added to msi section
                     attr.termSourceID = cvparam.attributeValue("accession").trim();
                 }
                 sample.addAttribute(attr);
             }
             
             for (Element userparam : XMLUtils.getChildrenByName(additional, "userParam")){
                 String name = userparam.attributeValue("name").trim();
                String value = userparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                } else {
                    value = value.trim();
                 }
                 CommentAttribute attr = new CommentAttribute(name, value);
                 sample.addAttribute(attr);
             }
             
             try {
                 st.scd.addNode(sample);
             } catch (ParseException e) {
                 log.error("Unable to add node "+sample);
                 e.printStackTrace();
                 continue;
             }
             
             
             
         }
         //these can only be calculated after all other steps
         
         //submission id is the minimum sample id
         List<String> submitids = new ArrayList<String>();
         for (SCDNode in : st.scd.getAllNodes()){
             submitids.add(in.getNodeName());
         }
         Collections.sort(submitids);
         st.msi.submissionIdentifier = submitids.get(0) ;
         
         log.info("Finished convert()");
         return st;
     }
 
     public void convert(Set<File> infiles, Writer writer) throws IOException, DocumentException  {
         log.debug("recieved infiles, preparing to convert");
         SampleData st = convert(infiles);
 
         log.info("SampleTab converted, preparing to write");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         sampletabwriter.write(st);
         log.info("SampleTab written");
         sampletabwriter.close();
 
     }
 
     public void convert(Set<File> infiles, File sampletabFile) throws IOException, DocumentException {
 
         // create parent directories, if they dont exist
         sampletabFile = sampletabFile.getAbsoluteFile();
         if (sampletabFile.isDirectory()) {
             sampletabFile = new File(sampletabFile, "sampletab.pre.txt");
         }
         if (!sampletabFile.getParentFile().exists()) {
             sampletabFile.getParentFile().mkdirs();
         }
         FileWriter writer = null;
         try {
             writer = new FileWriter(sampletabFile); 
             convert(infiles, writer);
         } finally {
             try {
                 if (writer != null ){
                     writer.close();
                 }
             } catch (IOException e) {
                 //do nothing
             }
         }
     }
 
     public void convert(Set<File> infiles, String outfilename) throws IOException, DocumentException  {
 
         convert(infiles, new File(outfilename));
     }
 
 
     public static void main(String[] args) {
         new PRIDEXMLToSampleTab().doMain(args);
     }
 
     public void doMain(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments.
             parser.parseArgument(args);
             // TODO check for extra arguments?
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
         
         //TODO convert to using globs
         
         HashSet<File> prideFiles = new HashSet<File>();
         
         if (projectsFilename != null){
             //if a project filename was given, then we find the project that the provided input filename is part of
             
             
             //read the given input filename to determine accession
             Element expcollection;
             try {
                 expcollection = XMLUtils.getDocument(new File(inputFilename)).getRootElement();
             } catch (FileNotFoundException e) {
                 System.out.println("Unable to read "+inputFilename);
                 System.exit(1);
                 return;
             } catch (DocumentException e) {
                 System.out.println("Unable to parse "+inputFilename);
                 System.exit(1);
                 return;
             }
             Element exp = XMLUtils.getChildByName(expcollection, "Experiment");
             String accession = "GPR-"+XMLUtils.getChildByName(exp, "ExperimentAccession").getTextTrim();
 
             //read all the projects
             Map<String, Set<String>> projects;
             try {
                 projects = PRIDEutils.loadProjects(new File(projectsFilename));
             } catch (IOException e) {
                 System.out.println("Unable to read projects file "+projectsFilename);
                 e.printStackTrace();
                 System.exit(1);
                 return;
             }
             //find the project
             String projectname = null;
             for (String name: projects.keySet()){
                 if (projects.get(name).contains(accession)){
                     projectname = name;
                 }
             }
             if (projectname != null){
                 //now add all the files that are similar to the input filename but with the other accessions
                 Set<String> accessions = projects.get(projectname);
                 if (accession.equals(Collections.min(accessions))){
                     for (String subaccession : accessions){
                         prideFiles.add(new File(inputFilename.replace(accession, subaccession)));
                     }
                 } else {
                     System.out.println("Accession is not minimum in project, terminating");
                     System.exit(0);
                     return;
                 }
             } else {
                 System.out.println("Unable to find project of "+accession);
                 System.exit(1);
                 return;
             }
             
         } else {
             prideFiles.add(new File(inputFilename));
         }
         
         try {
             convert(prideFiles, outputFilename);
         } catch (IOException e) {
             System.out.println("Error converting " + prideFiles + " to " + outputFilename);
             e.printStackTrace();
             System.exit(1);
             return;
         } catch (DocumentException e) {
             System.out.println("Error converting " + prideFiles + " to " + outputFilename);
             e.printStackTrace();
             System.exit(1);
             return;
         }
     }
 }
