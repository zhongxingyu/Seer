 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The Netarchive Suite - Software to harvest and preserve websites
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
  *   USA
  */
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;
 
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
 
 public class AspectRatioDetectorProcessor extends ProcessorChainElement {
 
     private static Logger log = Logger.getLogger(AspectRatioDetectorProcessor.class);
 
     private static final long clipSize = 100000000L;
 
     private String darPatternS = ".*DAR\\s([0-9]*):([0-9]*).*";
     private Pattern darPattern = Pattern.compile(darPatternS, Pattern.DOTALL);
 
     /**
      * Detects and sets the display aspect ratio for the program. If there is a single
      * clip then it looks in the middle of the clip. If there are multiple clips then
      * it looks at the beginning of the second clip.
      * Pre-condition: non-empty clip data in the request.
      * Post-condition: request.getDisplayAspectRatio returns a non-null result.
      *
      * The command line to extract the aspect ratio is like
      * dd if=mux2.ts bs=1000 count=100000|  vlc - --intf dummy --novideo --noaudio --program 2005
      * --sout='#std{access=file,mux=ts,dst=-}' | ffmpeg -i - -y  -f mpeg2video /dev/null
      *
      * @param request
      * @param config
      * @throws ProcessorException
      */
     @Override
     protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         Long blocksize = 1000L;
         Long blockcount = clipSize/blocksize;
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
         String command = null;
         switch(request.getClipType()) {
             case MUX:
                 command = "dd if=" + filename + " "
                         + "bs=" + blocksize + " "
                         + "count=" + blockcount + " "
                         + "skip=" + offset/blocksize + " "
                         + "|vlc - " +  " --program=" + program + " "
                         + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                         + "--sout '#std{access=file,mux=ts,dst=-}' "
                         + "|ffmpeg -i - ";
                 break;
             case MPEG1:
                 command = "dd if=" + filename + " "
                         + "bs=" + blocksize + " "
                         + "count=" + blockcount + " "
                         + "skip=" + offset/blocksize + " "
                         + "| ffmpeg -i -";
                 break;
             case MPEG2:
                 command = "dd if=" + filename + " "
                         + "bs=" + blocksize + " "
                         + "count=" + blockcount + " "
                         + "skip=" + offset/blocksize + " "
                         + "| ffmpeg -i - ";
                 break;
             case WAV:
                 return;
                 //command = "true";
         }
 
         log.info("Executing '" + command + "'");
         try {
             ExternalJobRunner runner = null;
             try {
                runner = new ExternalJobRunner(10000L, new String[]{"bash", "-c", command});
             } catch (ExternalProcessTimedOutException e) {
                 throw new ProcessorException(e);
             }
             log.debug("Command '" + command + "' returned with output '" + runner.getError());
             log.info("Command '" + command + "' returned with exit code '" + runner.getExitValue() + "'");
             Matcher m = darPattern.matcher(runner.getError());
             if (m.matches()) {
                 String top = m.group(1);
                 String bottom = m.group(2);
                 log.debug("Matched DAR '" + top + ":" + bottom);
                 final double displayAspectRatio = Double.parseDouble(top) / Double.parseDouble(bottom);
                 log.info("Detected aspect ratio '" + displayAspectRatio + "' for '" + request.getPid() + "'");
                 request.setDisplayAspectRatio(displayAspectRatio);
                 request.setDisplayAspectRatioString(top + ":" + bottom);
             }
         } catch (IOException e) {
             throw new ProcessorException(e);
         } catch (InterruptedException e) {
             throw new ProcessorException(e);
         }
 
     }
 }
