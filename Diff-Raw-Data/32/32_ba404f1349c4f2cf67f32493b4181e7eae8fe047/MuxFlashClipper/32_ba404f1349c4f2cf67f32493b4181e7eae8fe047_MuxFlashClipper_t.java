 /* File:        $Id$
  * Revision:    $Revision$
  * Author:      $Author$
  * Date:        $Date$
  *
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
 
 import javax.servlet.ServletConfig;
 import java.io.File;
 
 public class MuxFlashClipper extends ProcessorChainElement {
 
     private static final String algorithm = "vlc";
 
     @Override
     protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
              this.seamlessClip(request, config);
      }
 
     private void seamlessClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         Long blocksize = 1880L;
         Long offsetBytes = 0L;
         Long totalLengthBytes = 0L;
         String fileList = "";
         String processSubstitutionFileList = "";
         final int clipSize = request.getClips().size();
         int programNumber = 0;
         for (int iclip = 0; iclip < clipSize; iclip++ ) {
             TranscodeRequest.FileClip clip = request.getClips().get(iclip);
             final long fileLength = new File(clip.getFilepath()).length();  //This line has the side-effect of automounting the media file
             Long clipLength = clip.getClipLength();
             fileList += " " + clip.getFilepath() + " ";
             if (iclip == 0) {
                 programNumber = clip.getProgramId();
                 offsetBytes = clip.getStartOffsetBytes();
                 if (offsetBytes == null) offsetBytes = 0L;
                 if (clipLength != null && clipSize == 1) {
                     totalLengthBytes = clipLength;   //Program contained within file
                      processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs="+blocksize + " skip=" + offsetBytes/blocksize
                             + " count=" + totalLengthBytes/blocksize + ") " ;
                 } else {         //Otherwise always go to end of file
                     totalLengthBytes = fileLength - offsetBytes;
                     processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs="+blocksize + " skip=" + offsetBytes/blocksize + ") " ;
                 }
 
             } else if (iclip == clipSize - 1 && clipSize != 1) {   //last clip in multiclip program
                 if (clipLength != null) {
                     totalLengthBytes += clip.getClipLength();
                 } else {
                     totalLengthBytes += fileLength;
                 }
                 processSubstitutionFileList +=" <(dd if=" + clip.getFilepath() + " bs=" + blocksize + " count=" + clip.getClipLength()/blocksize + ") ";
             } else {   //A file in the middle of a program so take the whole file
                 totalLengthBytes += fileLength;
                 processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs=" + blocksize + ") ";
             }
         }
         boolean pidSubtitles = request.getDvbsubPid() != null && !request.getAudioPids().isEmpty() && request.getVideoPid() != null;
 
 
         String clipperCommand;
         if (algorithm.equals("full_vlc")) {
             clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                     + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                     + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                     + "--sout-all --sout '#duplicate{dst=\"transcode{senc=dvbsub}"
                     + ":transcode{acodec=mp3,vcodec=h264,vb=" + Util.getVideoBitrate(config) + ",venc=x264{profile=baseline,preset=superfast},soverlay,deinterlace,audio-sync,"
                     + "ab=" + Util.getAudioBitrate(config)
                     + ",width=" + FlashTranscoderProcessor.getWidth(request, config)
                     + ",height=" + FlashTranscoderProcessor.getHeight(request, config) +",samplerate=44100,threads=0}"
                     + ":std{access=file,mux=ffmpeg{mux=flv}"
                     + ",dst='" + OutputFileUtil.getFlashVideoOutputFile(request, config).getAbsolutePath() +"'}\""
                     + ",select=\"program=" + programNumber + "\"' ";
         }
         else {
             if (!pidSubtitles) {
                 clipperCommand = "cat " + processSubstitutionFileList + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                     + "--sout-all --sout '#duplicate{dst=\"transcode{senc=dvbsub}"
                     + ":transcode{vcodec=h264,vb=" + Util.getVideoBitrate(config) + ",venc=x264{" + Util.getInitParameter(config, Constants.X264_PRESET_VLC) + "},soverlay,deinterlace,audio-sync,"
                     + ",width=" + FlashTranscoderProcessor.getWidth(request, config)
                     + ",height=" + FlashTranscoderProcessor.getHeight(request, config) +",threads=0}"
                     + ":std{access=file,mux=ts,dst=-}\""
                     + ",select=\"program=" + programNumber + "\"' | "
                     + "ffmpeg -i -  -async 2 -vcodec copy -ac 2 -acodec libmp3lame -ar 44100 -ab " + Util.getAudioBitrate(config) + " -f flv " + OutputFileUtil.getFlashVideoOutputFile(request, config) ;
 
             } else {
                  clipperCommand = "cat " + processSubstitutionFileList + " |  vlc - --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                         + "--sout-all --sout '#duplicate{dst=\""
                         + "transcode{vcodec=x264,vb=" + Util.getVideoBitrate(config) + ",venc=x264{" + Util.getInitParameter(config, Constants.X264_PRESET_VLC) + "},soverlay,deinterlace,audio-sync,"
                         + ",width=" + FlashTranscoderProcessor.getWidth(request, config)
                         + ",height=" + FlashTranscoderProcessor.getHeight(request, config) +",threads=0}"
                         + ":std{access=file,mux=ts,dst=-}\""
                         + ",select=\"es=" + request.getVideoPid() + ",es=" + request.getMinimumAudioPid() + ",es="+request.getDvbsubPid() + "\"}' |" +
                         "ffmpeg -i -  -async 2 -vcodec copy -acodec libmp3lame -ac 2 -ar 44100 -ab " + Util.getAudioBitrate(config)
                         + "000 -f flv " + OutputFileUtil.getFlashVideoOutputFile(request, config);
             }
         }
        symlinkToRootDir(config, OutputFileUtil.getFlashVideoOutputFile(request, config));
         ExternalJobRunner.runClipperCommand(clipperCommand);
     }
 
    public static void symlinkToRootDir(ServletConfig config, File file) throws ProcessorException {
        File rootdir = new File(Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM));
        ExternalJobRunner.runClipperCommand("ln -s " + file.getAbsolutePath() + " " + rootdir.getAbsolutePath());
    }

 }
