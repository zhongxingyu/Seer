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
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.io.IOException;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 
 public class FlashTranscoderProcessor extends ProcessorChainElement {
 
     private static Logger log = Logger.getLogger(FlashTranscoderProcessor.class);
 
 
     @Override
     protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         switch (request.getClipType()) {
             case MUX:
                 (new MuxFlashClipper()).processThis(request, config);
                 break;
             case MPEG1:
                 (new MpegTranscoderProcessor()).processThis(request, config);
                 break;
             case MPEG2:
                 (new MpegTranscoderProcessor()).processThis(request, config);
                 break;
             case WAV:
                 (new WavTranscoderProcessor()).processThis(request, config);
         }
         Util.unlockRequest(request);
     }
 
     public static String getFfmpegCommandLine(TranscodeRequest request, ServletConfig config) {
         String line = "ffmpeg -i - " + config.getInitParameter(Constants.FFMPEG_PARAMS)
                 + " -b " + Util.getInitParameter(config, Constants.VIDEO_BITRATE) + "000"
                 + " -ab " + Util.getInitParameter(config, Constants.AUDIO_BITRATE) + "000"
                 + " " + getFfmpegAspectRatio(request, config)           
                 + " " + " -vpre "  + config.getInitParameter(Constants.X264_PRESET)
                + " -threads 0 " + OutputFileUtil.getFlashVideoOutputFile(request, config);
         return line;
     }
 
 
     protected static String getFfmpegAspectRatio(TranscodeRequest request, ServletConfig config) {
         Double aspectRatio = request.getDisplayAspectRatio();
         String ffmpegResolution;
         Long height = Long.parseLong(Util.getInitParameter(config, Constants.PICTURE_HEIGHT));
         if (aspectRatio != null) {
             long width = Math.round(aspectRatio*height);
             if (width%2 == 1) width += 1;
             ffmpegResolution = " -s " + width + "x" + height;
         } else {
             ffmpegResolution = " -s 320x240";
         }
         return ffmpegResolution;
     }
 
     public static int getHeight(TranscodeRequest request, ServletConfig config) {
         Long height = Long.parseLong(Util.getInitParameter(config, Constants.PICTURE_HEIGHT));
         return height.intValue();
    }
 
      public static int getWidth(TranscodeRequest request, ServletConfig config) {
        Double aspectRatio = request.getDisplayAspectRatio();
         Long height = Long.parseLong(Util.getInitParameter(config, Constants.PICTURE_HEIGHT));
         if (aspectRatio != null) {
             long width = Math.round(aspectRatio*height);
             if (width%2 == 1) width += 1;
             return (int) width;
         } else {
             return 320;
         }
    }
 
 }
