 package uk.ac.ebi.fgpt.sampletab.ncbi;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.AbstractInfileDriver;
 import uk.ac.ebi.fgpt.sampletab.Normalizer;
 import uk.ac.ebi.fgpt.sampletab.utils.ENAUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class NCBISampleTabCombiner extends AbstractInfileDriver {
 
     @Option(name = "-o", usage = "output filename")
     private String outputFilename;
     
     @Option(name = "-p", usage = "project filename")
     private String projectFilename = "ncbi_projects.txt";
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     private final ConcurrentMap<String, Set<File>> groupings = new ConcurrentHashMap<String, Set<File>>();
 
     class GroupIDsTask implements Runnable {
         private final File inFile;
         private Logger log = LoggerFactory.getLogger(getClass());
 
         GroupIDsTask(File inFile) {
             this.inFile = inFile;
         }
 
         public Collection<String> getGroupIds(Document xml) throws DocumentException, IOException {
             Collection<String> groupids = new ArrayList<String>();
             Element root = xml.getRootElement();
             Element ids = XMLUtils.getChildByName(root, "Ids");
             Element attributes = XMLUtils.getChildByName(root, "Attributes");
             for (Element id : XMLUtils.getChildrenByName(ids, "Id")) {
                 String dbname = id.attributeValue("db");
                 String sampleid = id.getText();
                 if (dbname == null) {
                     //id with no db
                     log.info("id with no db "+sampleid);
                 } else if (dbname.equals("SRA")) {
                     // group by sra study
                     log.debug("Getting studies of SRA sample " + sampleid);
                     Collection<String> studyids = ENAUtils.getStudiesForSample(sampleid);
                     if (studyids == null || studyids.size() == 0) {
                         //did not find any studies directly
                         //try indirect via submission
                         Collection<String> submissionIds = ENAUtils.getSubmissionsForSample(sampleid);
                         for (String submissionId : submissionIds) {
                             studyids.addAll(ENAUtils.getStudiesForSubmission(submissionId));
                         }
                         
                         if (studyids.size() == 0) {
                             //if there are still no study ids, use submission ids
                             studyids.addAll(submissionIds);
                         }
                     }
                     if (studyids != null && studyids.size() > 0 ) {
                         groupids.addAll(studyids);
                     }
                 } else if (dbname.equals("dbGaP")) {
                     // group by dbGaP project
                     for (Element attribute : XMLUtils.getChildrenByName(attributes, "Attribute")) {
                         if (attribute.attributeValue("attribute_name").equals("gap_accession")) {
                             groupids.add(attribute.getText());
                         }
                     }
                 } else if (dbname.equals("EST") || dbname.equals("GSS")) { 
                     //dont' use these if possible
                 } else if (dbname.equals("ATCC")) { 
                     groupids.add("ATCC");
                 } else if (dbname.equals("Coriell")) { 
                     groupids.add("Coriell");
                 } else if (dbname.equals("HapMap")) { 
                     groupids.add("HapMap");
                 } else if (dbname.equals("DSMZ")) { 
                     groupids.add("DSMZ");
                 } else {
                     // could group by others, but it is messy
                     //just throw them in a big group for the moment - 95% is either SRA or dbGaP
                     if (!groupids.contains("NCBI_BioSamples")){
                         groupids.add("NCBI_BioSamples");
                     }
                 }
             }
             if (groupids.size() > 1){
                 groupids.remove("NCBI_BioSamples");
             }
             return groupids;
         }
         
         public void run() {
 
             Document xml = null;
             try {
                 xml = XMLUtils.getDocument(inFile);
             } catch (FileNotFoundException e) {
                 log.error("Unable to find "+inFile, e);
                 return;
             } catch (DocumentException e) {
                 log.error("Unable to read "+inFile, e);
                 return;
             }
             
             Collection<String> groupids = null;
             try {
                 groupids = getGroupIds(xml);
             } catch (DocumentException e) {
                 log.error("unable to get groupIds of "+inFile, e);
                 return;
             } catch (FileNotFoundException e) {
                 log.error("unable to get groupIds of "+inFile, e);
                 return;
             } catch (IOException e) {
                 log.error("unable to get groupIds of "+inFile, e);
                 return;
             }
 
 //            if (groupids.size() > 1) {
 //                log.warn("Multiple groups for " + inFile);
 //            }
             
             if (groupids.size() == 0) {
                 log.error("No groups for " + inFile);
             }
 
             for (String groupid : groupids) {
                 
                 Set<File> group = groupings.get(groupid);
                 if (group == null) {
                     final Set<File> value = Collections.synchronizedSet(new HashSet<File>());
                     group = groupings.putIfAbsent(groupid, value);
                     if (group == null) {
                         group = value;
                     }
                 }
                 
                 group.add(inFile);
             }
         }
     }
 
     public SampleData combine(Collection<SampleData> indata) {
 
         SampleData sampleout = new SampleData();
         for (SampleData sampledata : indata) {
             
             sampleout.msi.persons.addAll(sampledata.msi.persons);
             sampleout.msi.organizations.addAll(sampledata.msi.organizations);
             sampleout.msi.termSources.addAll(sampledata.msi.termSources);
             sampleout.msi.databases.addAll(sampledata.msi.databases);
             sampleout.msi.publications.addAll(sampledata.msi.publications);
 
             //if (sampleout.msi.submissionDescription == null || sampleout.msi.submissionDescription.trim().equals(""))
             //    sampleout.msi.submissionDescription = sampledata.msi.submissionDescription;
             //if (sampleout.msi.submissionTitle == null || sampleout.msi.submissionTitle.trim().equals(""))
             //    sampleout.msi.submissionTitle = sampledata.msi.submissionTitle;
 
             if (sampledata.msi.submissionReleaseDate != null) {
                 if (sampleout.msi.submissionReleaseDate == null) {
                     sampleout.msi.submissionReleaseDate = sampledata.msi.submissionReleaseDate;
                 } else {
                     // use the most recent of the two dates
                     Date datadate = sampledata.msi.submissionReleaseDate;
                     Date outdate = sampleout.msi.submissionReleaseDate;
                     if (datadate != null && outdate != null && datadate.after(outdate)) {
                         sampleout.msi.submissionReleaseDate = sampledata.msi.submissionReleaseDate;
 
                     }
                 }
             }
             if (sampledata.msi.submissionUpdateDate != null) {
                 if (sampleout.msi.submissionUpdateDate == null) {
                     sampleout.msi.submissionUpdateDate = sampledata.msi.submissionUpdateDate;
                 } else {
                     // use the most recent of the two dates
                     Date datadate = sampledata.msi.submissionUpdateDate;
                     Date outdate = sampleout.msi.submissionUpdateDate;
                     if (datadate != null && outdate != null && datadate.after(outdate)) {
                         sampleout.msi.submissionUpdateDate = sampledata.msi.submissionUpdateDate;
 
                     }
                 }
             }
 
             // add nodes from here to parent
             for (SCDNode node : sampledata.scd.getRootNodes()) {
                 try {
                     sampleout.scd.addNode(node);
                 } catch (uk.ac.ebi.arrayexpress2.magetab.exception.ParseException e4) {
                     log.warn("Unable to add node " + node.getNodeName(), e4);
                     continue;
                 }
             }
         }
         sampleout.msi.submissionTitle = SampleTabUtils.generateSubmissionTitle(sampleout);
         return sampleout;
     }
     
     class OutputTask implements Runnable {
         private final List<File> files;
         private final NCBIBiosampleRunnable converter;
 
         public OutputTask(List<File> files) {
             this.files = files;
             this.converter = new NCBIBiosampleRunnable(null, null);
         }
 
         public void run() {
             List<SampleData> sampledatas = new ArrayList<SampleData>();
             for (File xmlFile : files) {
                 log.debug("converting " + xmlFile);
                 try {
                     sampledatas.add(this.converter.convert(xmlFile));
                 } catch (ParseException e2) {
                     log.warn("Unable to convert " + xmlFile, e2);
                     continue;
                 } catch (uk.ac.ebi.arrayexpress2.magetab.exception.ParseException e2) {
                     log.warn("Unable to convert " + xmlFile, e2);
                     continue;
                 } catch (DocumentException e2) {
                     log.warn("Unable to convert " + xmlFile, e2);
                     continue;
                 } catch (FileNotFoundException e2) {
                     log.warn("Unable to convert " + xmlFile, e2);
                     continue;
                 }
             }
             SampleData sampleout = combine(sampledatas);
             
             String identifier = null;
             int used_i = 0;
             for(int i = 0 ; i < sampledatas.size(); i++){
                 SampleData sd = sampledatas.get(i);
                 if (identifier == null){
                     identifier = sd.msi.submissionIdentifier;
                     used_i = i;
                 } else {
                     Pattern p = Pattern.compile("GNC-([0-9]*)");
                     Matcher m1 = p.matcher(identifier);
                     Matcher m2 = p.matcher(sd.msi.submissionIdentifier);
                     if (!m1.matches() || !m2.matches()){
                         if (!m1.matches()){
                             log.warn("Unable to find numbers in "+identifier);
                         }
                         if (!m2.matches()){
                             log.warn("Unable to find numbers in "+sd.msi.submissionIdentifier);
                         }
                     } else {
                         Integer i1 = new Integer(m1.group(1));
                         Integer i2 = new Integer(m2.group(1));
                         if (i1 < i2){
                             identifier = sd.msi.submissionIdentifier;
                             used_i = i;
                         }
                     }
                 }
             }
             
             sampleout.msi.submissionIdentifier = identifier;
             sampleout.msi.submissionTitle = SampleTabUtils.generateSubmissionTitle(sampleout);
 
             // more sanity checks
             if (sampleout.scd.getRootNodes().size() != this.files.size()) {
                 log.warn("unequal group size " + sampleout.msi.submissionIdentifier);
             }
 
             Normalizer norm = new Normalizer();
             norm.normalize(sampleout);
             
             File outFile = files.get(used_i);
             outFile = new File(outFile.getParentFile(), outputFilename);
             
             SampleTabWriter writer;
             try {
                 writer = new SampleTabWriter(new BufferedWriter(new FileWriter(outFile)));
                 writer.write(sampleout);
                 writer.close();
                log.error("Wrote to " + outFile);
             } catch (IOException e) {
                 log.error("Unable to write " + outFile, e);
                 return;
             }
 
         }
     }
     
     @Override 
     public void postProcess(){
 
         // at this point, subs is a mapping from the project name to a set of BioSample accessions
         // output them to a file
         File projout = new File(projectFilename);
         BufferedWriter projoutwrite = null;
         try {
             projoutwrite = new BufferedWriter(new FileWriter(projout));
             synchronized (groupings) {
                 // sort them to put them in a sensible order
                 List<String> projects = new ArrayList<String>(groupings.keySet());
                 Collections.sort(projects);
                 for (String project : projects) {
 
                     projoutwrite.write(project);
                     projoutwrite.write("\t");
                     List<File> files = new ArrayList<File>(groupings.get(project));
                     Collections.sort(files);
                     for (File file : files) {
                         projoutwrite.write(file.toString());
                         projoutwrite.write("\t");
                     }
 
                     projoutwrite.write("\n");
                 }
             }
         } catch (IOException e) {
             log.error("Unable to write to " + projout, e);
             System.exit(1);
         } finally {
             if (projoutwrite != null) {
                 try {
                     projoutwrite.close();
                 } catch (IOException e) {
                     // failed within a fail so give up
                     log.error("Unable to close file writer " + projout);
 
                 }
             }
         }
         
         //TODO trigger all the output processes
         ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
         
         List<String> projects = new ArrayList<String>(groupings.keySet());
         for (String project : projects) {
             List<File> files = new ArrayList<File>(groupings.get(project));
             Runnable t = new OutputTask(files);
             if (threaded) {
                 pool.execute(t);
             } else {
                 t.run();
             }
         }
         
         // run the pool and then close it afterwards
         // must synchronize on the pool object
         synchronized (pool) {
             pool.shutdown();
             try {
                 // allow 24h to execute. Rather too much, but meh
                 pool.awaitTermination(1, TimeUnit.DAYS);
             } catch (InterruptedException e) {
                 log.error("Interupted awaiting thread pool termination", e);
             }
         }
         
     }
 
     public static void main(String[] args) throws IOException {
         new NCBISampleTabCombiner().doMain(args);
     }
 
     @Override
     protected Runnable getNewTask(File inputFile) {
         return new GroupIDsTask(inputFile);
     }
 
 }
