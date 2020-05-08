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
 
 import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
 
 import javax.servlet.ServletConfig;
 import java.util.List;
 import java.io.File;
 
 public class MpegTranscoderProcessor extends ProcessorChainElement {
 
     @Override
     protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         mpegClip(request, config);
     }
 
 
     private void mpegClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         long blocksize = 1880L;
         final int clipSize = request.getClips().size();
         String command;
         if (clipSize > 1) {
             command = getMultiClipCommand(request, config);
         } else {
             TranscodeRequest.FileClip clip = request.getClips().get(0);
             String start = "";
             if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) start = "skip=" + clip.getStartOffsetBytes()/blocksize;
             String length = "";
             if (clip.getClipLength() != null && clip.getClipLength() != 0L) length = "count=" + clip.getClipLength()/blocksize;
             command = "dd if=" + clip.getFilepath() + " bs=" + blocksize + " " + start + " " + length + "| "
                     + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);
         }
        ExternalJobRunner.runClipperCommand(command);
     }
 
     private String getMultiClipCommand(TranscodeRequest request, ServletConfig config) {
         String files = "";
         long start = 0L;
         long length = 0L;
         long blocksize = 1880L;
         List<TranscodeRequest.FileClip> clips = request.getClips();
         for (int i=0; i<clips.size(); i++) {
             TranscodeRequest.FileClip clip = clips.get(i);
             files += " " + clip.getFilepath() + " ";
             if (clip.getClipLength() != null) {
                 length += clip.getClipLength()/blocksize;
             } else if (clip.getStartOffsetBytes() != null) {
                 length += ((new File(clip.getFilepath())).length() - clip.getStartOffsetBytes())/blocksize;
             } else {
                 length += (new File(clip.getFilepath())).length()/blocksize;
             }
             if (i == 0 && clip.getStartOffsetBytes() != null) {
                 start = clip.getStartOffsetBytes()/blocksize;
             }
         }
         String command = "cat " + files
                 + " |dd bs=" + blocksize + " skip=" + start + " count=" + length + "| "
                 + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);
         return command;
     }
 
 
 }
