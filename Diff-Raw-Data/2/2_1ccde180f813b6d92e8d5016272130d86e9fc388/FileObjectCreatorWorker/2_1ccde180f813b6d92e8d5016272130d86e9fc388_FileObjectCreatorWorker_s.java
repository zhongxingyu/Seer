 package dk.statsbiblioteket.doms.transformers.fileobjectcreator;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.transformers.common.DomsWebserviceFactory;
 import dk.statsbiblioteket.doms.transformers.common.FFProbeLocationDomsConfig;
 import dk.statsbiblioteket.doms.transformers.fileobjectcreator.MuxFileChannelCalculator;
 import jsr166y.ForkJoinTask;
 import jsr166y.RecursiveAction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class FileObjectCreatorWorker extends RecursiveAction {
     private static Logger log = LoggerFactory.getLogger(FileObjectCreatorWorker.class);
 
     private MuxFileChannelCalculator muxFileChannelCalculator;
     private FFProbeLocationDomsConfig config;
     private ResultWriter resultWriter;
     private String baseUrl;
     private List<String> data;
 
     private static boolean shutdown = false;
 
     public FileObjectCreatorWorker(FFProbeLocationDomsConfig config,
                                    ResultWriter resultWriter,
                                    String baseUrl,
                                    List<String> data,
                                    MuxFileChannelCalculator muxFileChannelCalculator) {
         this.config = config;
         this.resultWriter = resultWriter;
         this.baseUrl = baseUrl;
         this.data = data;
         this.muxFileChannelCalculator = muxFileChannelCalculator;
     }
 
     @Override
     protected void compute() {
         if (data.size() == 1) {
             try {
                 DomsObject domsObject = DomsFileParser.parse(config, baseUrl, data.get(0), muxFileChannelCalculator);
                 if (domsObject != null) {
                     doWork(domsObject);
                 } else {
                     resultWriter.logIgnored(data.get(0));
                 }
             } catch (ParseException e) {
                 log.info("Error while parsing: " + data.get(0));
             } catch (FileIgnoredException e) {
                 log.info("Ignored file: " + e.getFilename());
             }
         } else if (permissionToRun()) {
             int center = data.size()/2;
             ForkJoinTask<Void> workerA = new FileObjectCreatorWorker(config, resultWriter, baseUrl, data.subList(0, center),
                         muxFileChannelCalculator);
             ForkJoinTask<Void> workerB = new FileObjectCreatorWorker(config, resultWriter, baseUrl, data.subList(center, data.size()),
                         muxFileChannelCalculator);
             invokeAll(workerA, workerB);
         }
     }
 
     public void doWork(DomsObject domsObject) {
         String comment = "Batch-created by " + this.getClass().getName();
 
         if (!permissionToRun()) {
             return;
         }
 
         String uuid = null;
         if (domsObject != null && domsObject.isValid()) {
             String output = domsObject.formatAsInput();
 
             try {
                 CentralWebservice webservice = newWebservice();
                 uuid = webservice.getFileObjectWithURL(domsObject.getPermanentUrl());
                 if (uuid == null) {
                     String formatUri = null;
                     try {
                         File ffProbeFile = domsObject.getFFProbeFile();
                         formatUri = SimpleFFProbeParser.getFormatURIFromFile(ffProbeFile);
                         log.info("Got formatURI from \"" + ffProbeFile + "\": " + formatUri);
                     } catch (Exception e) {
                         resultWriter.logBadFFProbeData(domsObject);
                         formatUri = domsObject.guessFormatUri();
                         if (formatUri != null) {
                             log.warn("Couldn't get formatURI from ffProbeFile, this should probably be investigated. "
                                     + "Based on the filename the following formatURI will be used instead: " + formatUri);
                         } else {
                             String errorMsg = "Failed getting a formatURI for " + domsObject;
                             log.error(errorMsg, e);
                             resultWriter.logFailure(errorMsg);
                             /* Possibly problematic early return. Due to the shear number of ways this can fail,
                                the entire function should probably be refactured instead.*/
                             return;
                         }
                     }
 
                     uuid = webservice.newObject (
                             "doms:Template_RadioTVFile",
                             new ArrayList<String>(),
                             comment
                     );
 
                     webservice.addFileFromPermanentURL(
                             uuid,
                             domsObject.getFileName(),
                             null,
                             domsObject.getPermanentUrl(),
                             formatUri,
                             comment
                     );
 
                     LinkedList<String> pidsToMarkAsPublished = new LinkedList<String>();
                     pidsToMarkAsPublished.add(uuid);
 
                     webservice.markPublishedObject(pidsToMarkAsPublished, "Finalizing batch-creation of object.");
 
                     resultWriter.logSuccess(output + " (" + uuid + ")");
                     log.info("Created (" + uuid + "): " + output);
                     resultWriter.logNewUuid(uuid);
                 } else {
                     log.info("Already exists (" + uuid + "): " + output);
                     resultWriter.logExisting(uuid);
                 }
 
             } catch (InvalidCredentialsException e) {
                 resultWriter.logFailure(output);
                 log.error("Authentication-related error. Requesting shutdown..", e);
                 requestShutdown();
             } catch (InvalidResourceException e) {
                 resultWriter.logFailure(output);
                 if (uuid == null) {
                     log.error("Inconsistent data, this really shouldn't happen: " +
                             "The most likely reason for this is that the template, \"doms:Template_RadioTVFile\", does not exist." +
                             "Requesting shutdown..");
                 } else {
                     log.error("Inconsistent data, this really shouldn't happen: " +
                             "The most likely reason for this is that the object with uuid = \"" + uuid + "\" cannot be found." +
                             "Requesting shutdown..");
                 }
                 requestShutdown();
             } catch (MethodFailedException e) {
                 resultWriter.logFailure(output);
                 log.warn("Ingest of the following object failed: " + domsObject + "(uuid=\"" + uuid + "\")", e);
             } catch (Exception e) {
                 resultWriter.logFailure(uuid);
                 if (uuid != null) {
                     log.error("Failure getting ffprobe data for " + uuid, e);
                     resultWriter.logBadFFProbeData(domsObject);
                 } else {
                     log.error(e.getMessage(), e);
                 }
             }
         }
     }
 
     public static void requestShutdown() {
         log.info("Shutdown requested.");
         shutdown = true;
     }
 
     private boolean permissionToRun() {
         return !shutdown;
     }
 
     private CentralWebservice newWebservice() {
         try {
             return new DomsWebserviceFactory(config).getWebservice();
         } catch (RuntimeException e) {
             System.err.println("Error communication with DOMS. Config: " + config);
             requestShutdown();
         }
         return null;
     }
 }
