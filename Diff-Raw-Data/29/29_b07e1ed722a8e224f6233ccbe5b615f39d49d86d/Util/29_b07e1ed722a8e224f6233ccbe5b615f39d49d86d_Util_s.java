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
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Util {
 
     private static Logger log = Logger.getLogger(Util.class);
 
     /**
      * Pattern for extracting a uuid of a doms object from url-decoded
      * permanent url.
      */
     public static final String UUID_STRING = ".*uuid:(.*)";
     public static final Pattern UUID_PATTERN = Pattern.compile(UUID_STRING);
 
     private Util(){}
 
 
     public static String getDemuxFilename(TranscodeRequest request) {
         return request.getPid()+"_first.ts";
     }
 
     /**
      * Gets the uuid from a shard url
      * @param shardUrl
      * @return
      * @throws UnsupportedEncodingException
      */
     public static String getUuid(String shardUrl) throws UnsupportedEncodingException {
         String urlS = URLDecoder.decode(shardUrl, "UTF-8");
         Matcher m = UUID_PATTERN.matcher(urlS);
         if (m.matches()) {
              return m.group(1);
         } else return null;
     }
 
     public static String getFinalFilename(TranscodeRequest request) {
         return request.getPid() + ".mp4";
     }
 
     public static File getTempDir(ServletConfig config) {
         return new File(getInitParameter(config, Constants.TEMP_DIR_INIT_PARAM));
     }
 
     public static File getFinalDir(ServletConfig config) {
         return new File(Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM));
     }
 
     public static File getDemuxFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getDemuxFilename(request));
     }
 
     public static File getIntialFinalFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getFinalFilename(request));
     }
 
     public static File getFinalFinalFile(TranscodeRequest request, ServletConfig config) {
         return new File(getFinalDir(config), getFinalFilename(request));
     }
 
     public static File getFlashFile(TranscodeRequest request, ServletConfig config) {
         return new File(getFinalDir(config), request.getPid() + ".flv");
     }
 
     public static File getLockFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getLockFileName(request));
     }
 
     public static String getLockFileName(TranscodeRequest request) {
         return request.getKeyForLockFilename() + ".lck";
     }
 
     public static ServiceTypeEnum getServiceTypeFromLockFile(File lockFile) {
         String type = lockFile.getName().split("\\.")[1];
         return ServiceTypeEnum.valueOf(type);
     }
 
     public static String getPidFromLockFile(File lockFile) {
         return lockFile.getName().split("\\.")[0];
     }
 
     public static TranscodeRequest getRequestFromLockFile(String lockFilename) {
     	//String lockFilename = "8a90f338-566a-0410-bc61-9ec4cb1a6b14_-400_300_Filename_prefix#DIGITV_BROADCAST_EXTRACTION.lck";
     	String domsProgramPidPattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
     	String userAdditionalStartEndOffsetPattern = "[-]?[0-9]*";
     	String serviceTypePattern = "DIGITV_BROADCAST_EXTRACTION";
 		String filenamePrefixPattern = "[^#]*";
 		Pattern p = Pattern.compile("("+domsProgramPidPattern + ")_"
 				+ "(" + userAdditionalStartEndOffsetPattern + ")_"
 				+ "(" + userAdditionalStartEndOffsetPattern + ")_"
 				+ "(" + filenamePrefixPattern + ")\\."
 				+ "(" + serviceTypePattern + ").lck");
 		Matcher m = p.matcher(lockFilename);
 		if (!m.matches()) {
 			log.error("Could not match lock filename: " + lockFilename);
 			throw new RuntimeException("Could not match lock filename.");
 		}
 		String domsProgramPid = m.group(1);
 		long userAdditionalStartOffset = Long.valueOf(m.group(2));
 		long userAdditionalEndOffset = Long.valueOf(m.group(3));
 		String filenamePrefix = m.group(4);
 		String serviceType = m.group(5); // unused
     	TranscodeRequest request = new TranscodeRequest(domsProgramPid, userAdditionalStartOffset, userAdditionalEndOffset, filenamePrefix);
     	//lockFilename.equals(request.getKeyForLockFilename()+".lck");
     	return request;
 	}
 
 
 	public static File[] getAllLockFiles(ServletConfig config) {
         FilenameFilter filter = new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".lck");
             }
         };
         return (getTempDir(config)).listFiles(filter);
     }
 
     public static void unlockRequest(TranscodeRequest request) {
         synchronized (request) {
             if (request.getThePool() != null && request.getLockObject() != null) {
                 try {
                     log.info("Unlocking request '" + request.getKey() + "' from '" + request.getLockObject() + "'");
                     request.getThePool().returnObject(request.getLockObject());
                     request.setLockObject(null);
                     request.setThePool(null);
                 } catch (Exception e) {
                     log.error(e);
                 }
             }
         }
     }
 
     public static String getStreamId(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         File outputFile = OutputFileUtil.getExistingMediaOutputFile(request, config);
         String filename = getRelativePath(OutputFileUtil.getBaseOutputDir(request, config) , outputFile);
         if (filename.endsWith(".mp4")) {
             return "mp4:" + filename;
         } else if (filename.endsWith(".mp3")) {
             return "mp3:" + filename;
         } else if (filename.endsWith(".flv")) {
             return "flv:" + filename;
         } else return null;      
     }
 
     public static String getRelativePath(File parent, File child) throws ProcessorException {
         try {
             return getRelativeFile(child, parent);
         } catch (IOException e) {
             throw new ProcessorException(e);
         }
     }
     /**
      * Returns the path of one File relative to another.
      *
      * @param target the target directory
      * @param base the base directory
      * @return target's path relative to the base directory
      * @throws IOException if an error occurs while resolving the files' canonical names
      */
      public static String getRelativeFile(File target, File base) throws IOException
      {
        String[] baseComponents = base.getCanonicalPath().split(Pattern.quote(File.separator));
        String[] targetComponents = target.getCanonicalPath().split(Pattern.quote(File.separator));
 
        // skip common components
        int index = 0;
        for (; index < targetComponents.length && index < baseComponents.length; ++index)
        {
          if (!targetComponents[index].equals(baseComponents[index]))
          break;
        }
 
        StringBuilder result = new StringBuilder();
        if (index != baseComponents.length)
        {
          // backtrack to base directory
          for (int i = index; i < baseComponents.length; ++i)
            result.append(".." + File.separator);
        }
        for (; index < targetComponents.length; ++index)
          result.append(targetComponents[index] + File.separator);
        if (!target.getPath().endsWith("/") && !target.getPath().endsWith("\\"))
        {
          // remove final path separator
          result.delete(result.length() - File.separator.length(), result.length());
        }
        return result.toString();
      }
 
 
 
 
     public static int getQueuePosition(TranscodeRequest request, ServletConfig config) {
         return ProcessorChainThreadPool.getInstance(config).getPosition(request);
     }
 
 
     public static String getInitParameter(ServletConfig config, String paramName) {
     	String result;
         if (config.getServletContext() != null && config.getServletContext().getInitParameter(paramName) != null) {
             result = config.getServletContext().getInitParameter(paramName);
         } else {
             result = config.getInitParameter(paramName);
         }
         if (result == null) {
         	log.warn("Parameter " + paramName + " was not defined!");
         }
         return result;
     }
 
     public static String getAudioBitrate(ServletConfig config) {
         return getInitParameter(config, Constants.AUDIO_BITRATE);
     }
 
     public static String getVideoBitrate(ServletConfig config) {
         return getInitParameter(config, Constants.VIDEO_BITRATE);
     }
 
     public static String getPrimarySnapshotSuffix(ServletConfig config) {
         return getInitParameter(config, Constants.SNAPSHOT_PRIMARY_FORMAT);
     }
 
     public static long getTranscodingTimeout(ServletConfig config, TranscodeRequest request) {
         return Math.round(Double.parseDouble(getInitParameter(config, Constants.TRANSCODING_TIMEOUT_FACTOR))*request.getTotalLengthSeconds()*1000L)
                + Long.parseLong(getInitParameter(config, Constants.TRANSCODING_TIMEOUT_CONSTANT));
     }
 
 }
