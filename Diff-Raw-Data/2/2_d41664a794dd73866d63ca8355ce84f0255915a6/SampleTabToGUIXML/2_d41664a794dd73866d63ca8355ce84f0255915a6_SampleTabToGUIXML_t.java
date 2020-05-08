 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.Serializer.Property;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 
 @SuppressWarnings("restriction")
 public class SampleTabToGUIXML {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Argument(required=true, index=0, metaVar="OUTPUT", usage = "output filename")
     private String outputFilename;
 
     @Argument(required=true, index=1, metaVar="INPUT", usage = "input filename or globs")
     private List<String> inputFilenames;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public static void main(String[] args) {
         new SampleTabToGUIXML().doMain(args);
     }
     
     public static String stripNonValidXMLCharacters(String in) {
         //from http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
 
         if (in == null){ 
             return null;
         }
         
         StringBuffer out = new StringBuffer(); // Used to hold the output.
         char current; // Used to reference the current character.
         
         for (int i = 0; i < in.length(); i++) {
             current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
             if ((current == 0x9) ||
                 (current == 0xA) ||
                 (current == 0xD) ||
                 ((current >= 0x20) && (current <= 0xD7FF)) ||
                 ((current >= 0xE000) && (current <= 0xFFFD)) ||
                 ((current >= 0x10000) && (current <= 0x10FFFF))){
                 out.append(current);
             }
         }
         return out.toString();
     } 
 
     private void writeAttribute(XMLStreamWriter xmlWriter, String cls, String classDefined, String dataType, String value) throws XMLStreamException{
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", cls);
         xmlWriter.writeAttribute("classDefined", classDefined);
         xmlWriter.writeAttribute("dataType", dataType);
         if (value != null){
             xmlWriter.writeStartElement("value");
             
             value = stripNonValidXMLCharacters(value);
             xmlWriter.writeCharacters(value);
             xmlWriter.writeEndElement(); //value
         }
         xmlWriter.writeEndElement(); //attribute
     }
 
     public void doMain(String[] args) {
 
         CmdLineParser cmdParser = new CmdLineParser(this);
         try {
             // parse the arguments.
             cmdParser.parseArgument(args);
             // TODO check for extra arguments?
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             cmdParser.printSingleLineUsage(System.err);
             System.err.println();
             cmdParser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
 
         log.debug("Looking for input files");
         List<File> inputFiles = new ArrayList<File>();
         for (String inputFilename : inputFilenames){
             inputFiles.addAll(FileUtils.getMatchesGlob(inputFilename));
         }
         log.info("Found " + inputFiles.size() + " input files");
         //TODO no duplicates
         Collections.sort(inputFiles);
 
         XMLOutputFactory output = XMLOutputFactory.newInstance();
         
         
         FileWriter outputWriter = null;
         try {
             //outputWriter = new FileWriter(outputFilename);
             //XMLStreamWriter xmlWriter = output.createXMLStreamWriter(outputWriter);
             
             Processor processor = new Processor(false);
             Serializer serializer = processor.newSerializer(new File(outputFilename));
             serializer.setOutputProperty(Property.INDENT, "yes");
             XMLStreamWriter xmlWriter = serializer.getXMLStreamWriter();
             
             xmlWriter.writeStartDocument();
             xmlWriter.writeStartElement("Biosamples");
             
             for (File inputFile : inputFiles){
                 log.info("File "+inputFile);
                 SampleData sd = null;
                 SampleTabSaferParser stParser = new SampleTabSaferParser();
                 try {
                     sd = stParser.parse(inputFile);
                 } catch (ParseException e) {
                     log.error("Unable to parse file "+inputFile);
                     e.printStackTrace();
                 }
                 
                 if (sd != null){
                 
                     //if release date is in the future, dont output
                     if (sd.msi.submissionReleaseDate.after(new Date())){
                         log.info("Future release, skipping");
                         continue;
                     }
                     
                     //NB. this assumes all samples are in a group. This is a condition of the .toload.txt files
                     //but not of sampletab.txt files.
                     
                     for (GroupNode g : sd.scd.getNodes(GroupNode.class)){
                         log.debug("Group "+g.getNodeName());
                         
                         xmlWriter.writeStartElement("SampleGroup");
                         xmlWriter.writeAttribute("id", g.getGroupAccession());
 
                         writeAttribute(xmlWriter, "Submission Title", "true", "STRING", sd.msi.submissionTitle);
                         writeAttribute(xmlWriter, "Submission Identifier", "true", "STRING", sd.msi.submissionIdentifier);
                         writeAttribute(xmlWriter, "Submission Description", "true", "STRING", sd.msi.submissionDescription);
                         writeAttribute(xmlWriter, "Submission Version", "true", "STRING", sd.msi.submissionVersion);
                         writeAttribute(xmlWriter, "Submission Reference Layer", "true", "BOOLEAN", sd.msi.submissionReferenceLayer.toString());
                         writeAttribute(xmlWriter, "Submission Release Date", "true", "STRING", sd.msi.getSubmissionReleaseDateAsString());
                         writeAttribute(xmlWriter, "Submission Modification Date", "true", "STRING", sd.msi.getSubmissionUpdateDateAsString());
                         writeAttribute(xmlWriter, "Name", "true", "BOOLEAN", g.getNodeName());
                         writeAttribute(xmlWriter, "Group Accession", "true", "BOOLEAN", g.getGroupAccession());
                         //group description?
                         
                         //organizations   
                         if (sd.msi.organizations.size() > 0){
                             xmlWriter.writeStartElement("attribute");
                             xmlWriter.writeAttribute("class", "Organizations");
                             xmlWriter.writeAttribute("classDefined", "true");
                             xmlWriter.writeAttribute("dataType", "OBJECT");
                             xmlWriter.writeStartElement("value");                     
                             for (Integer i = 0; i < sd.msi.organizations.size(); i++){
                                 Organization o = sd.msi.organizations.get(i);
                                 if (o != null){
                                     xmlWriter.writeStartElement("object");
                                     xmlWriter.writeAttribute("id", "Org"+i.toString());
                                     xmlWriter.writeAttribute("class", "Organization");
                                     xmlWriter.writeAttribute("classDefined", "true");
         
                                     writeAttribute(xmlWriter, "Organization Name", "true", "STRING", o.getName());
                                     writeAttribute(xmlWriter, "Organization Address", "true", "STRING", o.getAddress());
                                     writeAttribute(xmlWriter, "Organization URI", "true", "STRING", o.getURI());
                                     writeAttribute(xmlWriter, "Organization Email", "true", "STRING", o.getEmail());
                                     writeAttribute(xmlWriter, "Organization Role", "true", "STRING", o.getRole());
                                     
                                     xmlWriter.writeEndElement(); //object
                                 }
                             }
                             xmlWriter.writeEndElement(); //value
                             xmlWriter.writeEndElement(); //organizations
                         }
                         
                         //persons
                         if (sd.msi.persons.size() > 0){
                             xmlWriter.writeStartElement("attribute");
                             xmlWriter.writeAttribute("class", "Persons");
                             xmlWriter.writeAttribute("classDefined", "true");
                             xmlWriter.writeAttribute("dataType", "OBJECT");
                             xmlWriter.writeStartElement("value");                     
                             for (Integer i = 0; i < sd.msi.persons.size(); i++){
                                 Person p = sd.msi.persons.get(i);
                                 if (p != null){
                                     xmlWriter.writeStartElement("object");
                                     xmlWriter.writeAttribute("id", "Per"+i.toString());
                                     xmlWriter.writeAttribute("class", "Person");
                                     xmlWriter.writeAttribute("classDefined", "true");
         
                                     writeAttribute(xmlWriter, "Person Last Name", "true", "STRING", p.getLastName());
                                     writeAttribute(xmlWriter, "Person Initials", "true", "STRING", p.getInitials());
                                     writeAttribute(xmlWriter, "Person First Name", "true", "STRING", p.getFirstName());
                                     writeAttribute(xmlWriter, "Person Email", "true", "STRING", p.getEmail());
                                     writeAttribute(xmlWriter, "Person Role", "true", "STRING", p.getRole());
                                     
                                     xmlWriter.writeEndElement(); //object
                                 }
                             }
                             xmlWriter.writeEndElement(); //value
                             xmlWriter.writeEndElement(); //persons
                         }
                         
                         //publications
                         if (sd.msi.publications.size() > 0){
                             xmlWriter.writeStartElement("attribute");
                             xmlWriter.writeAttribute("class", "Publications");
                             xmlWriter.writeAttribute("classDefined", "true");
                             xmlWriter.writeAttribute("dataType", "OBJECT");
                             xmlWriter.writeStartElement("value");                     
                             for (Integer i = 0; i < sd.msi.publications.size(); i++){
                                 Publication p = sd.msi.publications.get(i);
                                 if (p != null){
                                     xmlWriter.writeStartElement("object");
                                     xmlWriter.writeAttribute("id", "Pub"+i.toString());
                                     xmlWriter.writeAttribute("class", "Publication");
                                     xmlWriter.writeAttribute("classDefined", "true");
         
                                     writeAttribute(xmlWriter, "Publication PubMed ID", "true", "STRING", p.getPubMedID());
                                     writeAttribute(xmlWriter, "Publication DOI", "true", "STRING", p.getDOI());
                                     
                                     xmlWriter.writeEndElement(); //object
                                 }
                             }
                             xmlWriter.writeEndElement(); //value
                             xmlWriter.writeEndElement(); //publications
                         }
                         
                         //term sources
                         if (sd.msi.termSources.size() > 0){
                             xmlWriter.writeStartElement("attribute");
                             xmlWriter.writeAttribute("class", "Term Sources");
                             xmlWriter.writeAttribute("classDefined", "true");
                             xmlWriter.writeAttribute("dataType", "OBJECT");
                             xmlWriter.writeStartElement("value");                     
                             for (Integer i = 0; i < sd.msi.termSources.size(); i++){
                                 TermSource t = sd.msi.termSources.get(i);
                                if (t != null && t.getName() != null){
                                     xmlWriter.writeStartElement("object");
                                     xmlWriter.writeAttribute("id", t.getName());
                                     xmlWriter.writeAttribute("class", "Term Source");
                                     xmlWriter.writeAttribute("classDefined", "true");
         
                                     writeAttribute(xmlWriter, "Term Source Name", "true", "STRING", t.getName());
                                     writeAttribute(xmlWriter, "Term Source URI", "true", "STRING", t.getURI());
                                     writeAttribute(xmlWriter, "Term Source Version", "true", "STRING", t.getVersion());
                                     
                                     xmlWriter.writeEndElement(); //object
                                 }
                             }
                             xmlWriter.writeEndElement(); //value
                             xmlWriter.writeEndElement(); //term sources
                         }
                         
                         //database entries
                         if (sd.msi.databases.size() > 0){
                             xmlWriter.writeStartElement("attribute");
                             xmlWriter.writeAttribute("class", "Databases");
                             xmlWriter.writeAttribute("classDefined", "true");
                             xmlWriter.writeAttribute("dataType", "OBJECT");
                             xmlWriter.writeStartElement("value");                     
                             for (Integer i = 0; i < sd.msi.databases.size(); i++){
                                 Database d = sd.msi.databases.get(i);
                                 if (d != null){
                                     xmlWriter.writeStartElement("object");
                                     xmlWriter.writeAttribute("id", "Dat"+i.toString());
                                     xmlWriter.writeAttribute("class", "Database");
                                     xmlWriter.writeAttribute("classDefined", "true");
         
                                     writeAttribute(xmlWriter, "Database Name", "true", "STRING", d.getName());
                                     writeAttribute(xmlWriter, "Database ID", "true", "STRING", d.getID());
                                     writeAttribute(xmlWriter, "Database URI", "true", "STRING", d.getURI());
                                     
                                     xmlWriter.writeEndElement(); //object
                                 }
                             }
                             xmlWriter.writeEndElement(); //value
                             xmlWriter.writeEndElement(); //databaases
                         }
                         
                         Set<String> attributeTypes = new HashSet<String>();
                         
                         for (Node s : g.getParentNodes()){
                             log.debug("Node "+s.getNodeName());
                             //these should all be samples, but have to check anyway...
                             if (SampleNode.class.isInstance(s)){
                                 SampleNode sample = (SampleNode) s;
                                 xmlWriter.writeStartElement("Sample");
                                 xmlWriter.writeAttribute("groupId", g.getGroupAccession());
                                 xmlWriter.writeAttribute("id", sample.getSampleAccession());
 
                                 writeAttribute(xmlWriter, "Name", "false", "STRING", sample.getNodeName());
                                 attributeTypes.add("Name");
                                 
                                 writeAttribute(xmlWriter, "Sample Accession", "false", "STRING", sample.getSampleAccession());
                                 attributeTypes.add("Sample Accession");
                                 
                                 writeAttribute(xmlWriter, "Sample Description", "false", "STRING", sample.getSampleDescription());
                                 attributeTypes.add("Sample Description");
                                 
                                 for (SCDNodeAttribute a : sample.getAttributes()){
                                     if (a.getAttributeValue() != null && a.getAttributeValue().length()>0){
                                         writeAttribute(xmlWriter, a.getAttributeType(), "false", "STRING", a.getAttributeValue());
                                         attributeTypes.add(a.getAttributeType());
                                         log.debug("Attribute "+a.getAttributeType()+" "+a.getAttributeValue());
                                     }
                                 }
                                 
                                 //implicit derived from
                                 for (Node p : s.getParentNodes()){
                                     //these should all be samples, but have to check anyway...
                                     if (SampleNode.class.isInstance(p)){
                                         SampleNode parent = (SampleNode) p;
                                         writeAttribute(xmlWriter, "derived from", "false", "STRING", parent.getSampleAccession());
                                         attributeTypes.add("derived from");
                                     }
                                 }
                                 
                                 xmlWriter.writeEndElement(); //Sample
                             }
                         }
                         
                         //write out precomputed attribute summary
                         xmlWriter.writeCharacters("\n");
                         xmlWriter.writeStartElement("SampleAttributes");
                         for (String attributeType : attributeTypes){
                             writeAttribute(xmlWriter, attributeType, "false", "STRING", null);
                         }
                         xmlWriter.writeEndElement(); //SampleAttributes
                         
                         xmlWriter.writeEndElement(); //SampleGroup
                     }
                 }
             }
             xmlWriter.writeEndDocument();
             
             xmlWriter.close();
         } catch (XMLStreamException e) {
             e.printStackTrace();
         } catch (SaxonApiException e) {
             e.printStackTrace();
         } finally {
             if (outputWriter != null){
                 try{
                     outputWriter.close();
                 } catch (IOException e) {
                     //do nothing
                 }
             }
         }
         
     }
 }
