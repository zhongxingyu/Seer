 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.InfrastructureContext;
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.util.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 9/26/12
  * Time: 3:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MultistreamVideoTranscoderProcessor extends ProcessorChainElement {
 
 
     public static int getHeight(TranscodeRequest request, SingleTranscodingContext context) {
         return context.getVideoHeight();
     }
 
     public static int getWidth(TranscodeRequest request, SingleTranscodingContext context) {
         Double aspectRatio = request.getDisplayAspectRatio();
         if (aspectRatio != null) {
             long width = 2*Math.round(aspectRatio*getHeight(request, context)/2);
             //if (width%2 == 1) width += 1;
             return (int) width;
         } else {
             return 320;
         }
     }
 
     private static Logger logger = LoggerFactory.getLogger(MultistreamVideoTranscoderProcessor.class);
 
 
     protected void processThis(TranscodeRequest request, SingleTranscodingContext context) throws ProcessorException {
         int programNumber = 0;
         if (request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX)) {
             Integer programNumberObject = request.getClips().get(0).getProgramId();
             if (programNumberObject == null) {
                 throw new ProcessorException("Cannot transcode multi-program transport stream because no program number specified: " + request.getObjectPid());
             } else {
                 programNumber = programNumberObject;
             }
         }
         String processSubstitutionFileList = request.getClipperCommand();
         File outputDir = FileUtils.getTemporaryMediaOutputDir(request, context);
         outputDir.mkdirs();
 
         //The odd logic here is that we prefer to use custom-PMT if we have all three pids, but because of a bug in vlc we
         //may need to use a custom PMT to get DR1 (program 101) to work even if we don't have a pid for dvbsub. So:
         boolean useCustomPMT = (request.getDvbsubPid() != null && !request.getAudioPids().isEmpty() && request.getVideoPid() != null && request.getVideoFcc() != null && request.getAudioFcc() != null );
         useCustomPMT = useCustomPMT |  (!request.getAudioPids().isEmpty() && request.getVideoPid() != null && request.getVideoFcc() != null && request.getAudioFcc() != null && programNumber==101);
         useCustomPMT = true;
         String clipperCommand = null;
         if (request.getFileFormat().equals(FileFormatEnum.SINGLE_PROGRAM_AUDIO_TS)) {
             clipperCommand = findAudioClipperCommand(request, context, processSubstitutionFileList);
         } else {
             clipperCommand = findVideoClipperCommand(request, context, processSubstitutionFileList, programNumber, useCustomPMT);
         }
         try {
             long programLength = MetadataUtils.findProgramLengthMillis(request);
             long timeout = (long) (programLength/context.getTranscodingTimeoutDivisor());
             logger.debug("Setting transcoding timeout for '" + request.getObjectPid() + "' to " + timeout + "ms" );
             request.setTranscoderCommand(clipperCommand);
             ExternalJobRunner.runClipperCommand(timeout, clipperCommand);
         } catch (ExternalProcessTimedOutException e) {
             File outputFile =  FileUtils.getTemporaryMediaOutputFile(request, context);
             logger.warn("Deleting '" + outputFile.getAbsolutePath() + "'");
             outputFile.delete();
             throw new ProcessorException("Process timed out for "+request.getObjectPid(),e);
         }
     }
 
     private String findAudioClipperCommand(TranscodeRequest request, SingleTranscodingContext context, String processSubstitutionFileList) {
         return "cat " + processSubstitutionFileList + "| "
                + "ffmpeg -i - -acodec libmp3lame -ar 44100 -ac 2 -ab "
                 + context.getAudioBitrate() + "000 -y " + FileUtils.getTemporaryMediaOutputDir(request, context);
     }
 
     private String findVideoClipperCommand(TranscodeRequest request, SingleTranscodingContext context, String processSubstitutionFileList, int programNumber, boolean useCustomPMT) {
         String clipperCommand;
         if (!useCustomPMT) {
             String programString = "";
             String programSelectString = "";
             if (request.getFileFormat().equals(FileFormatEnum.MULTI_PROGRAM_MUX)) {
                 programString = " --program=" + programNumber;
                 programSelectString = ",select=\"program=\" + programNumber + \"\"";
             }
             clipperCommand = "cat " + processSubstitutionFileList + " | vlc - " + programString + " --quiet --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                     + "--sout-all --sout '#duplicate{dst=\"transcode{senc=dvbsub}"
                     + ":transcode{vcodec=h264,vb=" + context.getVideoBitrate() + ",venc=x264{" + context.getX264VlcParams() + "},soverlay,deinterlace,audio-sync,"
                     + ",width=" + getWidth(request, context)
                     + ",height=" + getHeight(request, context) +",threads=0}"
                     + ":std{access=file,mux=ts,dst=-}\""
                     + programSelectString + "' | "
                     + "ffmpeg -i -  -async 2 -vcodec copy -ac 2 -acodec libmp3lame -ar 44100 -ab " + context.getAudioBitrate() + "000 -y -f flv " + FileUtils.getTemporaryMediaOutputFile(request, context);
         /*
         For yousee ts files, change this to something like
         ffmpeg -i  ~/scratch/BTA-unittest/ANIMAL_20121008_120000_20121008_130000.mux -vcodec libx264 -preset superfast -profile:v High -level 3.0 -ab 96000 -vb 400000 -acodec libmp3lame -async 2 -ac 2 -ar 44100 -s 512x288  -f flv temp.flv
          */
 
         } else {
             String programSelector = " --program=1010 --sout-all --ts-extra-pmt=1010:1010=" + request.getVideoPid() + ":video=" + request.getVideoFcc()
                     + "," + request.getMinimumAudioPid() + ":audio=" + request.getAudioFcc();
             if (request.getDvbsubPid() != null) {
                 programSelector += "," + request.getDvbsubPid() + ":spu=dvbs";
             }
             logger.debug("Using Custom PMT for '" + request.getObjectPid() + "': " + programSelector);
             clipperCommand = "cat " + processSubstitutionFileList + " |  vlc - " + programSelector + " --quiet --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                     + "--sout-all --sout '#transcode{vcodec=x264,vb=" + context.getVideoBitrate() + ",venc=x264{" + context.getX264VlcParams() + "}" +
                     ",soverlay,deinterlace,audio-sync,"
                     + ",width=" + getWidth(request, context)
                     + ",height=" + getHeight(request, context) +",threads=0}"
                     + ":std{access=file,mux=ts,dst=-}' |" +
                     "ffmpeg -i -  -async 2 -vcodec copy -acodec libmp3lame -ac 2 -ar 44100 -ab " + context.getAudioBitrate()
                     + "000 -y -f flv " + FileUtils.getTemporaryMediaOutputFile(request, context);
         }
         return clipperCommand;
     }
 
 
 }
