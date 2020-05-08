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
 package dk.statsbiblioteket.doms.radiotv.extractor;
 
 public class Constants {
     private Constants(){}
 
     public static final String TEMP_DIR_INIT_PARAM = "dk.statsbiblioteket.doms.radiotv.extractor.tempdir";
     public static final String FINAL_DIR_INIT_PARAM = "dk.statsbiblioteket.doms.radiotv.extractor.finaldir";
     public static final String ERROR_DIR = "dk.statsbiblioteket.doms.radiotv.extractor.errordir";
     public static final String FILE_LOCATOR_URL = "dk.statsbiblioteket.doms.radiotv.extractor.fileLocatorUrl";
     public static final String DEMUXER_ALGORITHM = "dk.statsbiblioteket.doms.radiotv.extractor.demuxerAlgorithm";
     public static final String HANDBRAKE_PARAMETERS = "dk.statsbiblioteket.doms.radiotv.extractor.handbrakeParameters";
     public static final String X264_PARAMETERS = "dk.statsbiblioteket.doms.radiotv.extractor.x264Parameters";
     public static final String VIDEO_BITRATE = "dk.statsbiblioteket.doms.radiotv.extractor.videoBitrate";
     public static final String AUDIO_BITRATE = "dk.statsbiblioteket.doms.radiotv.extractor.audioBitrate";
     public static final String WOWZA_URL = "dk.statsbiblioteket.doms.radiotv.extractor.wowzaUrl";
     public static final String MAX_ACTIVE_PROCESSING = "dk.statsbiblioteket.doms.radiotv.extractor.maxActive";
     public static final String RELEASE_AFTER_DEMUX = "dk.statsbiblioteket.doms.radiotv.extractor.releaseAfterDemux";
     public static final String SOX_TRANSCODE_PARAM = "dk.statsbiblioteket.doms.radiotv.extractor.soxWavParam";
     
     
     public static final String PICTURE_HEIGHT = "dk.statsbiblioteket.doms.radiotv.extractor.pictureHeight";
     public static final String FFMPEG_PARAMS = "dk.statsbiblioteket.doms.radiotv.extractor.ffmpegParameters";
     public static final String X264_PRESET = "dk.statsbiblioteket.doms.radiotv.extractor.x264Preset";
     public static final String X264_PRESET_VLC = "dk.statsbiblioteket.doms.radiotv.extractor.x264PresetVlc";
 
     /**
      *  Constants relating to preview clips
      */
     public static final String PREVIEW_LENGTH = "dk.statsbiblioteket.doms.radiotv.extractor.previewLength";
     public static final String PREVIEW_DIRECTORY = "dk.statsbiblioteket.doms.radiotv.extractor.previewDirectory";
     public static final String PREVIEW_SERVICE_URL = "dk.statsbiblioteket.doms.radiotv.extractor.wowzaPreviewUrl";
     //public static final String PREVIEW_THUMBNAIL_DIRECTORY = "dk.statsbiblioteket.doms.radiotv.extractor.previewThumbnailDirectory";
 
     /**
      * Constants relating to snapshots
      */
     public static final String SNAPSHOT_NUMBER = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotNumber";
     public static final String SNAPSHOT_VIDEO_LENGTH = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotLength";
     public static final String SNAPSHOT_DIRECTORY = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotDirectory";
     public static final String SNAPSHOT_PRIMARY_FORMAT = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotPrimaryFormat";
     public static final String SNAPSHOT_FINAL_FORMAT = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotFinalFormat";
     public static final String SNAPSHOT_SCALE_SIZE = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotScaleSize";
     public static final String SNAPSHOT_QUALITY = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotQuality";
     public static final String SNAPSHOT_SERVICE_URL = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotWebserver";
     //TODO additional parameters for final scaling
 
     public static final String PREVIEW_TIMEOUT_FACTOR = "dk.statsbiblioteket.doms.radiotv.extractor.previewTimeoutFactor";
     public static final String TRANSCODING_TIMEOUT_FACTOR = "dk.statsbiblioteket.doms.radiotv.extractor.transcodingTimeoutFactor";
     public static final String SNAPSHOT_TIMEOUT_FACTOR = "dk.statsbiblioteket.doms.radiotv.extractor.snapshotTimeoutFactor";
     public static final String OUTPUT_BUFFER_THREADWAIT_TIMEOUT = "dk.statsbiblioteket.doms.radiotv.extractor.threadWait";
 
     /**
      * Constants relating to offsets
      */
     public static final String START_OFFSET_DIGITV = "dk.statsbiblioteket.doms.radiotv.extractor.startOffsetDigitv";
     public static final String END_OFFSET_DIGITV = "dk.statsbiblioteket.doms.radiotv.extractor.endOffsetDigitv";
     public static final String START_OFFSET_BART = "dk.statsbiblioteket.doms.radiotv.extractor.startOffsetBart";
     public static final String END_OFFSET_BART = "dk.statsbiblioteket.doms.radiotv.extractor.endOffsetBart";
     public static final String START_OFFSET_RADIO = "dk.statsbiblioteket.doms.radiotv.extractor.startOffsetRadio";
     public static final String END_OFFSET_RADIO = "dk.statsbiblioteket.doms.radiotv.extractor.endOffsetRadio";
 
     /**
      *  Constants relating to DOMS
      */
     public static final String DOMS_ENDPOINT = "dk.statsbiblioteket.doms.radiotv.extractor.domsEndpoint";
     public static final String DOMS_USERNAME = "dk.statsbiblioteket.doms.radiotv.extractor.domsUsername";
     public static final String DOMS_PASSWORD = "dk.statsbiblioteket.doms.radiotv.extractor.domsPassword";
     //public static final String PREVIEW_THUMBNAIL_DIRECTORY = "dk.statsbiblioteket.doms.radiotv.extractor.previewThumbnailDirectory";
 
     /**
      * Constants relating to shard analysis
      *
      */
     public static final String ANALYSIS_DIRECTORY = "dk.statsbiblioteket.doms.radiotv.extractor.analysisDirectory";
     public static final String MAXIMUM_HOLE_SIZE_SECONDS = "dk.statsbiblioteket.doms.radiotv.extractor.maxHoleSize";
 
     /**
      * Constants relating to DigiTV transcoding
      */
     public static final String DIGITV_FINAL_DIR_INIT_PARAM = "dk.statsbiblioteket.doms.radiotv.extractor.digitvFinaldir";
 	public static final String DIGITV_WORK_DIR_INIT_PARAM = "dk.statsbiblioteket.doms.radiotv.extractor.digitvWorkdir";
 	public static final String DIGITV_USER_EMAIL = "dk.statsbiblioteket.doms.radiotv.extractor.digitvEmail";
     public static final String DIGITV_VIDEO_BITRATE = "dk.statsbiblioteket.doms.radiotv.extractor.digitv.videoBitrate";
     public static final String DIGITV_AUDIO_BITRATE = "dk.statsbiblioteket.doms.radiotv.extractor.digitv.audioBitrate";
 
 
 }
