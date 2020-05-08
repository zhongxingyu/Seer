 package dk.statsbiblioteket.broadcasttranscoder.ws;
 
 import dk.statsbiblioteket.broadcasttranscoder.btaws.BtaResponse;
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.BroadcastTranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.processors.*;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
 import dk.statsbiblioteket.broadcasttranscoder.ws.config.ConfigurationLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  */
 @Path("/bta")
 public class BroadcastTranscoderService {
 
     public static Logger logger = LoggerFactory.getLogger(BroadcastTranscoderService.class);
 
    private static Set<String> runningTranscodes = new HashSet<String>();
 
     @Context
     ServletConfig config;
     @Context
     ServletContext context;
 
     @GET @Path("/btaDVDTranscode")
     @Produces(MediaType.APPLICATION_XML)
     public BtaResponse startDigitvTranscoding(
             @QueryParam("programpid") String programPid,
             @QueryParam("title") String title,
             @QueryParam("channel") String channel,
             @QueryParam("date") long startTime,
             @QueryParam("additional_start_offset") long additionalStartOffset,
             @QueryParam("additional_end_offset") long additionalEndOffset,
             @QueryParam("filename_prefix") String filenamePrefix,
             @DefaultValue("true") @QueryParam("burn_subtitles") String burnSubtitles)
             throws ProcessorException {
         final String programDescription = " " + title + " " + programPid + " " + channel + " " + startTime;
         logger.info("Received request for" + programDescription);
         checkContext();
         TranscodeRequest request = new TranscodeRequest();
         SingleTranscodingContext<BroadcastTranscodingRecord> transcodingContext = (SingleTranscodingContext<BroadcastTranscodingRecord>) context.getAttribute("transcodingContext");
         request.setObjectPid(programPid);
         request.setOutputBasename(filenamePrefix+"_"+startTime+"_"+additionalStartOffset+"_"+additionalEndOffset);
         transcodingContext.setDigitvStartOffset(additionalStartOffset);
         transcodingContext.setDigitvEndOffset(additionalEndOffset);
         return getBtaResponse(request, transcodingContext, programDescription);
     }
 
     private BtaResponse getBtaResponse(final TranscodeRequest request, final SingleTranscodingContext<BroadcastTranscodingRecord> transcodingContext, String programDescription) {
         BtaResponse response = new BtaResponse();
         response.setFilename(request.getOutputBasename());
         boolean isTranscoding = FileUtils.hasTemporarMediaOutputFile(request, transcodingContext);
         boolean isComplete = FileUtils.hasFinalMediaOutputFile(request, transcodingContext);
         if (isTranscoding) {
             response.setStatus("STARTED");
             File temporaryMediaOutputFile = FileUtils.findTemporaryMediaOutputFile(request, transcodingContext);
             response.setFilename(temporaryMediaOutputFile.getAbsolutePath());
             response.setFilelengthBytes(temporaryMediaOutputFile.length());
             logger.debug("Already started" + programDescription);
             return response;
         } else if (isComplete) {
             response.setStatus("DONE");
             File finalMediaOutputFile = FileUtils.findFinalMediaOutputFile(request, transcodingContext);
             response.setFilename(finalMediaOutputFile.getAbsolutePath());
             response.setFilelengthBytes(finalMediaOutputFile.length());
             logger.debug("Already complete" + programDescription);
             return response;
         } else {
             logger.debug("Starting new transcoding for" + programDescription);
             response.setStatus("STARTING");
            if (!runningTranscodes.contains(request.getOutputBasename())) {
                runningTranscodes.add(request.getOutputBasename());
                 new Thread() {
                     @Override
                     public void run() {
                         Object lockObject = null;
                         try {
                             lockObject = ConfigurationLoader.getThePool().borrowObject();
                             performTranscoding(request, transcodingContext);
                         } catch (Exception e) {    //Fault barrier for transcoding
                             logger.error("Error in processing " + request.getObjectPid(), e);
                         } finally {
                             try {
                                 ConfigurationLoader.getThePool().returnObject(lockObject);
                             } catch (Exception e) {
                                 logger.error("Error returning lock object", e);
                             }
                            runningTranscodes.remove(request.getOutputBasename());
                         }
                     }
                 }.start();
             }
             return response;
         }
     }
 
     private static void performTranscoding(TranscodeRequest request, SingleTranscodingContext<BroadcastTranscodingRecord> transcodingContext) throws ProcessorException {
         ProcessorChainElement pbcorer = new PbcoreMetadataExtractorProcessor();
         ProcessorChainElement programFetcher = new ProgramMetadataFetcherProcessor();
         ProcessorChainElement filedataFetcher    = new FileMetadataFetcherProcessor();
         ProcessorChainElement sanitiser = new SanitiseBroadcastMetadataProcessor();
         ProcessorChainElement sorter = new BroadcastMetadataSorterProcessor();
         ProcessorChainElement fileFinderFetcher = new FilefinderFetcherProcessor();
         ProcessorChainElement identifier = new FilePropertiesIdentifierProcessor();
         ProcessorChainElement clipper = new ClipFinderProcessor();
         ProcessorChainElement coverage = new CoverageAnalyserProcessor();
         ProcessorChainElement fixer = new StructureFixerProcessor();
         ProcessorChainElement concatenator = new ClipConcatenatorProcessor();
         ProcessorChainElement firstChain = ProcessorChainElement.makeChain(
                 pbcorer,
                 programFetcher,
                 filedataFetcher,
                 sanitiser,
                 sorter,
                 fileFinderFetcher,
                 identifier,
                 clipper,
                 coverage,
                 fixer,
                 concatenator);
         firstChain.processIteratively(request, transcodingContext);
         ProcessorChainElement secondChain;
         ProcessorChainElement pider = new PidAndAsepctRatioExtractorProcessor();
         ProcessorChainElement waver = new WavTranscoderProcessor();
         ProcessorChainElement multistreamer = new MultistreamVideoTranscoderProcessor();
         ProcessorChainElement unistreamvideoer = new UnistreamVideoTranscoderProcessor();
         ProcessorChainElement unistreamaudioer = new UnistreamAudioTranscoderProcessor();
         ProcessorChainElement renamer = new FinalMediaFileRenamerProcessor();
           switch (request.getFileFormat()) {
             case MULTI_PROGRAM_MUX:
                 if (transcodingContext.getVideoOutputSuffix().equals("mpeg")) {
                     logger.debug("Generating DVD video. No previews or snapshots for " + request.getObjectPid());
                     secondChain = ProcessorChainElement.makeChain(pider,
                             multistreamer,
                             renamer
                     );
                 } else {
                     secondChain = ProcessorChainElement.makeChain(pider,
                             multistreamer,
                             renamer);
                 }
                 break;
             case SINGLE_PROGRAM_VIDEO_TS:
                 if (transcodingContext.getVideoOutputSuffix().equals("mpeg")) {
                     logger.debug("Generating DVD video. No previews or snapshots for " + request.getObjectPid());
                     secondChain = ProcessorChainElement.makeChain(pider,
                             unistreamvideoer,
                             renamer
                     );
                 } else {
                     secondChain = ProcessorChainElement.makeChain(pider,
                             unistreamvideoer,
                             renamer);
                 }
                 break;
             case SINGLE_PROGRAM_AUDIO_TS:
                 secondChain = ProcessorChainElement.makeChain(pider,
                         unistreamaudioer,
                         renamer);
                 break;
             case MPEG_PS:
                 if (transcodingContext.getVideoOutputSuffix().equals("mpeg")) {
                     logger.debug("Generating DVD video. No previews or snapshots for " + request.getObjectPid());
                     secondChain = ProcessorChainElement.makeChain(pider,
                             unistreamvideoer,
                             renamer
                     );
                 } else {
                     secondChain = ProcessorChainElement.makeChain(pider,
                             unistreamvideoer,
                             renamer);
                 }
                 break;
             case AUDIO_WAV:
                 final String message = "Cannot process wav files at present. Exiting for " + request.getObjectPid();
                 logger.info(message);
                 throw new ProcessorException(message);
                // request.setRejected(true);
                // return;
             // secondChain = ProcessorChainElement.makeChain(waver,
             //         renamer,
             //         zeroChecker,
             //         previewer);
             default:
                 return;
         }
         secondChain.processIteratively(request, transcodingContext);
     }
 
 
     /**
      *
      * @param programPid
      * @param title
      * @param channel
      * @param startTime
      * @param additionalStartOffset
      * @param additionalEndOffset
      * @param filenamePrefix
      * @param sendEmailParam Ignored. Exists for backwards compatibility.
      * @param alternative  Ignored. Exists for backwards compatibility.
      * @return
      */
     @GET @Path("/digitv_transcode")
     @Produces(MediaType.APPLICATION_XML)
     public BtaResponse startDigitvTranscoding(
             @QueryParam("programpid") String programPid,
             @QueryParam("title") String title,
             @QueryParam("channel") String channel,
             @QueryParam("date") long startTime,
             @QueryParam("additional_start_offset") long additionalStartOffset,
             @QueryParam("additional_end_offset") long additionalEndOffset,
             @QueryParam("filename_prefix") String filenamePrefix,
             @DefaultValue("false") @QueryParam("send_email") String sendEmailParam,
             @DefaultValue("false") @QueryParam("alternative") String alternative ) throws ProcessorException {
         return startDigitvTranscoding(programPid, title, channel, startTime,
                 additionalStartOffset, additionalEndOffset, filenamePrefix, "true");
     }
 
 
     public void checkContext() throws ProcessorException {
         Object o = context.getAttribute("transcodingContext");
         if (! (o instanceof SingleTranscodingContext)) {
             throw new ProcessorException("Web context not initialised with SingleTranscodingContext");
         }
     }
 
 }
