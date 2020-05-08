 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.util.ExternalJobRunner;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileFormatEnum;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 9/26/12
  * Time: 1:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PidAndAsepctRatioExtractorProcessor extends ProcessorChainElement {
 
     private static Logger logger = LoggerFactory.getLogger(PidAndAsepctRatioExtractorProcessor.class);
 
     public PidAndAsepctRatioExtractorProcessor() {
     }
 
     public PidAndAsepctRatioExtractorProcessor(ProcessorChainElement childElement) {
         super(childElement);
     }
 
     @Override
     protected void processThis(TranscodeRequest request, SingleTranscodingContext context) throws ProcessorException {
         Long blocksize = 1880L;
         Long blockcount = context.getAnalysisClipLength()/blocksize;
         String filename = null;
         Integer program;
         Long offset = null;
         if (request.getClips().size() == 1) {
             TranscodeRequest.FileClip clip = request.getClips().get(0);
             program = clip.getProgramId();
             filename = clip.getFilepath();
             offset = (new File(filename)).length()/2L;
         } else {
             TranscodeRequest.FileClip clip = request.getClips().get(1);
             offset=0L;
             program = clip.getProgramId();
             filename = clip.getFilepath();
         }
         String command = "dd if=" + filename + " "
                 + "bs=" + blocksize + " "
                 + "count=" + blockcount + " "
                 + "skip=" + offset/blocksize + " "
                 + "|ffmpeg -i - ";
 
 
         logger.info("Executing '" + command + "'");
         ExternalJobRunner runner;
         try {
             runner = new ExternalJobRunner(new String[]{"bash", "-c", command});
             logger.debug("Command '" + command + "' returned with output '" + runner.getError());
         } catch (Exception e) {
             throw new ProcessorException("Failed to run command "+command,e);
         }
         String[] commandOutput = runner.getError().split("\\n");
         if (request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX)) {
             findPidsMultiMux(commandOutput, request, context);
         }  else {
             findPidsSingleMuxOrMpeg(commandOutput, request, context);
         }
         if (!request.getFileFormat().equals(FileFormatEnum.MPEG_PS)) {
             validateFoundData(request, context);
         }
     }
 
     private void validateFoundData(TranscodeRequest request, SingleTranscodingContext context) throws ProcessorException {
        if (request.getAudioPids().isEmpty() && request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX)) {
             throw new ProcessorException("No audio stream discovered for " + request.getObjectPid());
        } else if (request.getAudioPids().isEmpty()) {
            logger.warn("No audio stream discovered for " + request.getObjectPid() + " - proceeding with caution.");
         }
         if (request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX) || request.getFileFormat().equals(FileFormatEnum.SINGLE_PROGRAM_VIDEO_TS)) {
             if (request.getVideoPid() == null) {
                 throw new ProcessorException("No video stream discovered for " + request.getObjectPid());
             }
         }
     }
 
     private void findPidsSingleMuxOrMpeg(String[] commandOutput, TranscodeRequest request, SingleTranscodingContext context) {
         boolean foundProgram = false;
         for (String line: commandOutput) {
             findPidInLine(line, request, context);
         }
     }
 
     private void findPidsMultiMux(String[] commandOutput, TranscodeRequest request, SingleTranscodingContext context) {
         boolean foundProgram = false;
         Pattern thisProgramPattern = Pattern.compile(".*Program\\s"+request.getClips().get(0).getProgramId()+".*");
         Pattern programPattern = Pattern.compile(".*Program.*");
 
         for (String line: commandOutput) {
             logger.debug("Checking line '" + line + "'");
             if (foundProgram && programPattern.matcher(line).matches()) {
                 logger.debug("Found next program section, returning");
                 return;
             }
             if (thisProgramPattern.matcher(line).matches()) {
                 logger.debug("Found requested program");
                 foundProgram = true;
             }
             if (foundProgram) {
                 findPidInLine(line, request, context);
             }
 
         }
     }
 
     private void findPidInLine(String line, TranscodeRequest request, SingleTranscodingContext context) {
         Pattern dvbsubPattern = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*dvb.*sub.*");
         Pattern videoPattern = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*Video.*DAR\\s(([0-9]*):([0-9]*)).*");
         Pattern audioPattern1 = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*Audio.*");
         Pattern audioPattern2 = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*0x0011.*");
         Matcher dvbsubMatcher = dvbsubPattern.matcher(line);
         if (dvbsubMatcher.matches()){
             request.setDvbsubPid(dvbsubMatcher.group(1));
             logger.info("Setting pid for dvbsub '" + dvbsubMatcher.group(1) + "'");
         }
         Matcher videoMatcher = videoPattern.matcher(line);
         if (videoMatcher.matches()) {
             request.setVideoPid(videoMatcher.group(1));
             logger.info("Setting pid for video '" + videoMatcher.group(1) + "'");
             if (line.contains("mpeg2video")) {
                 request.setVideoFcc("mpgv");
             } else if (line.contains("h264")) {
                 request.setVideoFcc("h264");
             }
             logger.debug("Identified video fourcc for " + request.getObjectPid() + ": " + request.getVideoFcc());
             request.setDisplayAspectRatioString(videoMatcher.group(2));
             logger.debug("Identified aspect ratio '" + request.getDisplayAspectRatioString() + "'");
         }
         Matcher audioMatcher = audioPattern1.matcher(line);
        if (audioMatcher.matches()) {
             request.addAudioPid(audioMatcher.group(1));
             logger.info("Setting pid for audio '" + audioMatcher.group(1) + "'");
             if (line.contains("aac_latm")) {
                 request.setAudioFcc("mp4a");
             } else if (line.contains("mp2")) {
                 request.setAudioFcc("mpga");
            } else if (line.contains("ac3")) {
                request.setAudioFcc("a52");
             }
             logger.debug("Identified audio fourcc for " + request.getObjectPid() + ": " + request.getAudioFcc());
         }
         audioMatcher = audioPattern2.matcher(line);
         if (audioMatcher.matches() && !line.contains("5.1")) {
             request.addAudioPid(audioMatcher.group(1));
             logger.info("Setting pid for audio '" + audioMatcher.group(1) + "'");
             request.setAudioFcc("mp4a");
             logger.debug("Identified audio fourcc for " + request.getObjectPid() + ": " + request.getAudioFcc());
         }
         Matcher darMatcher = videoPattern.matcher(line);
         if (darMatcher.matches()) {
             String top = darMatcher.group(3);
             String bottom = darMatcher.group(4);
             logger.debug("Matched DAR '" + top + ":" + bottom);
             final double displayAspectRatio = Double.parseDouble(top) / Double.parseDouble(bottom);
             logger.info("Detected aspect ratio '" + displayAspectRatio + "' for '" + request.getObjectPid() + "'");
             request.setDisplayAspectRatio(displayAspectRatio);
             request.setDisplayAspectRatioString(top + ":" + bottom);
         }
     }
 
 }
