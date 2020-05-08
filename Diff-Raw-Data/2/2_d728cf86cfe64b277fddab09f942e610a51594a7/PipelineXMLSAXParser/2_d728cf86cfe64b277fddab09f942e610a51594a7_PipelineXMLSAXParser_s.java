 package uk.ac.ebi.fgpt.conan.core.pipeline;
 
 import org.xml.sax.*;
 import org.xml.sax.helpers.DefaultHandler;
 import uk.ac.ebi.fgpt.conan.core.process.DisplayNameProcessDecorator;
 import uk.ac.ebi.fgpt.conan.dao.ConanProcessDAO;
 import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
 import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
 import uk.ac.ebi.fgpt.conan.model.ConanProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanUser;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.SchemaFactory;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.ServiceConfigurationError;
 
 /**
  * A parser for conan pipelines.xml files that uses a SAX XML parser to read the pipeline XML. This parser will validate
  * pipelines.xml documents against their schema by loading the XSD from the resource location 'conan/pipelines.xsd'.
  * Failure to validate the pipelines.xml document against the schema will result in an exception that will prevent
  * pipelines from being loaded from the document.
  *
  * @author Tony Burdett
  * @date 16-Oct-2010
  */
 public class PipelineXMLSAXParser extends AbstractPipelineXMLParser {
     private final SAXParserFactory factory;
 
     public PipelineXMLSAXParser(ConanUserDAO userDAO, ConanProcessDAO processDAO) {
         super(userDAO, processDAO);
         this.factory = SAXParserFactory.newInstance();
     }
 
     @Override
     public Collection<ConanPipeline> parsePipelineXML(URL pipelineXMLResource) throws IOException {
         getLog().debug("Parsing pipeline XML from " + pipelineXMLResource);
         Collection<ConanPipeline> conanPipelines = new ArrayList<ConanPipeline>();
 
         try {
             getLog().debug("Creating XMLReader from " + pipelineXMLResource);
 
             XMLReader reader;
             synchronized (factory) {
                 factory.setValidating(false);
                 factory.setNamespaceAware(false);
                 reader = factory.newSAXParser().getXMLReader();
             }
             reader.setContentHandler(new PipelineXMLContentHandler(conanPipelines));
             reader.setErrorHandler(new PipelineXMLErrorHandler());
             reader.parse(new InputSource(pipelineXMLResource.openStream()));
         }
         catch (SAXException e) {
             throw new IOException("Unable to read from " + pipelineXMLResource, e);
         }
         catch (ParserConfigurationException e) {
             throw new IOException("Unable to configure SAX parser", e);
         }
 
         return conanPipelines;
     }
 
     @Override
     public Collection<ConanPipeline> parseAndValidatePipelineXML(URL pipelineXMLResource) throws IOException {
         getLog().debug("Parsing and validating pipeline XML from " + pipelineXMLResource);
         Collection<ConanPipeline> conanPipelines = new ArrayList<ConanPipeline>();
 
         try {
             getLog().debug("Creating XMLReader from " + pipelineXMLResource);
 
             XMLReader reader;
             synchronized (factory) {
                 // set factory to non-validating (no DOCTYPE declaration) but namespace aware
                 factory.setValidating(false);
                 factory.setNamespaceAware(true);
 
                // manually configure schema to use contacts.xsd resource
                 URL pipelinesXSDResource = getClass().getClassLoader().getResource(PIPELINES_SCHEMA_LOCATION);
                 SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                 factory.setSchema(schemaFactory.newSchema(new StreamSource(pipelinesXSDResource.openStream())));
 
                 reader = factory.newSAXParser().getXMLReader();
             }
             reader.setContentHandler(new PipelineXMLContentHandler(conanPipelines));
             reader.setErrorHandler(new PipelineXMLErrorHandler());
             reader.parse(new InputSource(pipelineXMLResource.openStream()));
         }
         catch (SAXException e) {
             throw new IOException("Unable to read from " + pipelineXMLResource, e);
         }
         catch (ParserConfigurationException e) {
             throw new IOException("Unable to configure SAX parser", e);
         }
 
         return conanPipelines;
     }
 
     private class PipelineXMLContentHandler extends DefaultHandler {
         private Collection<ConanPipeline> conanPipelines;
         private DefaultConanPipeline currentPipeline;
         private List<ConanProcess> currentProcesses;
         private ConanProcess currentProcess;
 
         private PipelineXMLContentHandler(Collection<ConanPipeline> conanPipelines) {
             this.conanPipelines = conanPipelines;
         }
 
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attributes)
                 throws SAXException {
             if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PIPELINE_ELEMENT)) {
                 currentPipeline = readPipeline(attributes);
             }
             else if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PROCESSES_ELEMENT)) {
                 currentProcesses = readProcesses();
             }
             else if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PROCESS_ELEMENT)) {
                 currentProcess = readProcess(attributes);
             }
         }
 
         @Override
         public void endElement(String uri, String localName, String qName) throws SAXException {
             if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PIPELINE_ELEMENT)) {
                 if (currentPipeline != null) {
                     getLog().debug("Loaded pipeline '" + currentPipeline.getName() + "'");
                     conanPipelines.add(currentPipeline);
                 }
             }
             else if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PROCESSES_ELEMENT)) {
                 if (currentPipeline != null) {
                     currentPipeline.setProcesses(currentProcesses);
                 }
                 else {
                     getLog().warn(
                             "Read a set of processes, but the pipeline to which they should be added is not valid.  " +
                                     "These processes will not be available");
                 }
             }
             else if (uri.equals(PIPELINES_SCHEMA_NAMESPACE) && localName.equals(PROCESS_ELEMENT)) {
                 getLog().debug("Loaded process '" + currentProcess.getName() + "'");
                 if (currentProcesses != null) {
                     currentProcesses.add(currentProcess);
                 }
                 else {
                     getLog().warn("Read process '" + currentProcess + "' but it could not be added to a valid " +
                                           "collection of processes.  This process will not be available.");
                 }
 
             }
         }
 
         private DefaultConanPipeline readPipeline(Attributes attributes) {
             for (int i = 0; i < attributes.getLength(); i++) {
                 getLog().trace("Next attribute: " +
                                        attributes.getQName(i) + " = " +
                                        attributes.getValue(i));
             }
 
             // read out pipeline metadata
             String name = attributes.getValue(PIPELINE_NAME_ATTRIBUTE);
             String usernameStr = attributes.getValue(PIPELINE_CREATOR_ATTRIBUTE);
             String isPrivateStr = attributes.getValue(PIPELINE_PRIVATE_ATTRIBUTE);
             String isDaemonizedStr = attributes.getValue(PIPELINE_DAEMONIZED_ATTRIBUTE);
 
             // lookup user by username
             Collection<ConanUser> conanUsers = getUserDAO().getUserByUserName(usernameStr);
             if (conanUsers.isEmpty()) {
                 getLog().error("An unknown user '" + usernameStr + "' was named as the creator of " +
                                        "the pipeline '" + name + "'.  This pipeline will not be loaded.");
                 return null;
             }
             else {
                 if (conanUsers.size() > 1) {
                     getLog().error(
                             "The username '" + usernameStr + "' is ambiguous, there are " + conanUsers.size() + " " +
                                     "users with this name.  The first user from the database will be marked as the creator of " +
                                     "this pipeline, but database consistency should be checked");
                 }
 
                 ConanUser conanUser = conanUsers.iterator().next();
                 boolean isPrivate = Boolean.parseBoolean(isPrivateStr);
                 boolean isDaemonized = Boolean.parseBoolean(isDaemonizedStr);
 
                 // create pipeline
                 DefaultConanPipeline conanPipeline = new DefaultConanPipeline(name, conanUser, isPrivate, isDaemonized);
 
                 getLog().trace("Starting parsing pipeline '" + conanPipeline.getName() + "'");
                 return conanPipeline;
             }
         }
 
         private List<ConanProcess> readProcesses() {
             return new ArrayList<ConanProcess>();
         }
 
         private ConanProcess readProcess(Attributes attributes) {
             for (int i = 0; i < attributes.getLength(); i++) {
                 getLog().trace("Next attribute: " +
                                        attributes.getQName(i) + " = " +
                                        attributes.getValue(i));
             }
 
             String processName = attributes.getValue(PROCESS_NAME_ATTRIBUTE);
             String processDisplayName = attributes.getValue(PROCESS_DISPLAYNAME_ATTRIBUTE);
 
             // retrieve process
             ConanProcess p = getProcessDAO().getProcess(processName);
             if (p != null) {
                 getLog().trace("Starting parsing process '" + p.getName() + "'");
                 if (processDisplayName != null) {
                     getLog().trace("Decorating '" + p.getName() + "' with display name '" + processDisplayName + "'");
                     return new DisplayNameProcessDecorator(p, processDisplayName);
                 }
                 else {
                     return p;
                 }
             }
             else {
                 String msg = "pipelines.xml references a process (" + processName + ") that was not loaded";
                 getLog().error(msg);
                 throw new ServiceConfigurationError(msg);
             }
         }
     }
 
     private class PipelineXMLErrorHandler implements ErrorHandler {
         public void warning(SAXParseException exception) throws SAXException {
             getLog().warn("XML parse warning at line " + exception.getLineNumber() + ", " +
                                   "column " + exception.getColumnNumber() + ": " + exception.getMessage());
         }
 
         public void error(SAXParseException exception) throws SAXException {
             getLog().error("XML parse error at line " + exception.getLineNumber() + ", " +
                                    "column " + exception.getColumnNumber() + ": " + exception.getMessage());
             throw exception;
         }
 
         public void fatalError(SAXParseException exception) throws SAXException {
             getLog().error("XML parsing fatal error at line " + exception.getLineNumber() + ", " +
                                    "column " + exception.getColumnNumber() + ": " + exception.getMessage());
             throw exception;
         }
     }
 }
