 package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
 import dk.statsbiblioteket.util.Streams;
 import dk.statsbiblioteket.util.Strings;
 import dk.statsbiblioteket.util.console.ProcessRunner;
 import dk.statsbiblioteket.util.xml.DOM;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 /** The jpylyzer metadata content checker checker */
 public class JpylyzerValidatorEventHandler
         implements TreeEventHandler {
 
     //Name of content file in jp2 virtual folder
     public static final String CONTENTS = "/contents";
     //Folder where the batch lives. Used because jpylyzer must work on the absolute path to the file
     private final String scratchFolder;
     /** Logger */
     private final Logger log = LoggerFactory.getLogger(JpylyzerValidatorEventHandler.class);
     /** The result collector results are collected in. */
     private final ResultCollector resultCollector;
     //The validator
     private AttributeValidator attributeValidator;
     // Double flags indicating if we are in a virtual jp2 folder
     private boolean isInDataFile;
     private String datafile;
     //If true, we will not expect to find a jpylyzer output file and will run jpylyzer instead
     private boolean atNinestars = false;
     //Path to the jpylyzer executable
     private String jpylyzerPath;
 
 
     /**
      * Construct a new JpylyzerValidatorEventHandler.
      *
      * @param batchFolder       the folder where the batches live
      * @param resultCollector     the result collector
      * @param controlPoliciesPath path to the control policies. If null, use default control policies
      *
      * @throws FileNotFoundException if the control policies point to file that is not found
      */
     public JpylyzerValidatorEventHandler(String batchFolder,
                                          ResultCollector resultCollector,
                                          String controlPoliciesPath)
             throws
             FileNotFoundException {
         this.scratchFolder = batchFolder;
         this.resultCollector = resultCollector;
 
 
         Document controlPoliciesDocument;
         if (controlPoliciesPath != null) {
             controlPoliciesDocument = DOM.streamToDOM(new FileInputStream(controlPoliciesPath));
         } else {
             controlPoliciesDocument = DOM.streamToDOM(Thread.currentThread().getContextClassLoader()
                                                             .getResourceAsStream("defaultControlPolicies.xml"));
         }
         if (this.jpylyzerPath == null) {
            this.jpylyzerPath = "src/main/extras/jpylyzer-1.10.1/jpylyzer.py";
         }
         attributeValidator = new JP2AttributeValidatorFactory(controlPoliciesDocument).createValidator();
 
     }
 
     /**
      * Extended constructor for the validator. This should be used if we want the validator to be able to
      * execute jpylyzer.
      *
      * @param batchFolder       the folder where the batches live
      * @param resultCollector     the result collector
      * @param controlPoliciesPath path to the control policies. If null, use default control policies
      * @param jpylyzerPath        path to the jpylyzer executable
      * @param atNinestars         if true, we will not look for a jpylyzer metadata file, and will instead execute
      *                            jpylyzer
      *
      * @throws FileNotFoundException if the control policies point to file that is not found
      */
     public JpylyzerValidatorEventHandler(String batchFolder,
                                          ResultCollector resultCollector,
                                          String controlPoliciesPath,
                                          String jpylyzerPath,
                                          boolean atNinestars)
             throws
             FileNotFoundException {
 
         this(batchFolder, resultCollector, controlPoliciesPath);
         this.jpylyzerPath = jpylyzerPath;
         this.atNinestars = atNinestars;
     }
 
     /**
      * Node begins. Sets the isInDataFile flag if this node is a data file node
      * @param event the node begins event
      */
     @Override
     public void handleNodeBegin(NodeBeginsParsingEvent event) {
         if (event instanceof DataFileNodeBeginsParsingEvent) {
             isInDataFile = true;
             datafile = event.getName();
         }
     }
 
     /**
      * Node ends. Clears the isInDataFile flag if this node is a data file node.
      * @param event the node ends event
      */
     @Override
     public void handleNodeEnd(NodeEndParsingEvent event) {
         if (event instanceof DataFileNodeEndsParsingEvent) {
             isInDataFile = false;
             datafile = null;
         }
     }
 
     /**
      * Attribute event.
      * If we are in a data file node, and the event is for a file called "/contents" and the flag "atNinestars" is set
      * we will run jpylyzer and validate the output.
      * If we are in a data file node, and the event is for a file called "*.jpylyzer.xml", validate the contents
      * @param event the attribute event
      */
     @Override
     public void handleAttribute(AttributeParsingEvent event) {
         try {
             if (isInDataFile) {
 
                 if (event.getName().endsWith(CONTENTS)) {
                     log.debug("Encountered event {}", event.getName());
                     if (atNinestars) {
                         File filePath = new File(scratchFolder, datafile);
                         InputStream jpylizerOutput = jpylize(filePath);
 
                         attributeValidator.validate(datafile, getByteArrayOutputStream(jpylizerOutput), resultCollector);
                     }
                 } else {
                     if (event.getName().endsWith("jpylizer.xml")) {
                         attributeValidator.validate(datafile, getByteArrayOutputStream(event.getData()), resultCollector);
                     }
                 }
             }
         } catch (IOException e) {
             resultCollector.addFailure(event.getName(),"jp2file",getComponent(),e.getMessage(),Strings.getStackTrace(e));
         }
     }
 
     private byte[] getByteArrayOutputStream(InputStream jpylizerOutput)
             throws
             IOException {
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         Streams.pipe(jpylizerOutput, byteStream);
         return byteStream.toByteArray();
     }
 
     /**
      * Get the component name
      */
     private String getComponent() {
         return "jpylyzerprofile" + getClass().getPackage().getImplementationVersion();
     }
 
     @Override
     public void handleFinish() {
         //Anything to do here?
     }
 
     /**
      * run jpylyzer on the given file and return the xml report as an inputstream.
      * @param dataPath the path to the jp2 file
      * @return the jpylyzer xml report
      * @throws RuntimeException if the execution of jpylyzer failed in some fashion (not invalid file, if the program
      * returned non-zero returncode)
      */
     private InputStream jpylize(File dataPath)
             throws
             IOException {
 
         log.info("Running jpylyzer on file {}", dataPath);
         ProcessRunner runner = new ProcessRunner(jpylyzerPath, dataPath.getAbsolutePath());
         runner.setOutputCollectionByteSize(Integer.MAX_VALUE);
 
         //this call is blocking
         runner.run();
 
         //we could probably do something more clever with returning the output while the command is still running.
         if (runner.getReturnCode() == 0) {
             return runner.getProcessOutput();
         } else {
             throw new IOException(
                     "failed to run jpylyzer, returncode:" + runner.getReturnCode() + ", stdOut:" + runner
                             .getProcessOutputAsString() + " stdErr:" + runner.getProcessErrorAsString());
         }
     }
 
 
 }
