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
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvTranscoderMuxClipper;
 
 import org.apache.commons.collections.map.CaseInsensitiveMap;
 import org.apache.log4j.Logger;
 import org.omg.PortableInterceptor.DISCARDING;
 
 import javax.servlet.ServletConfig;
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Date;
 
 /**
  * Manages final output files where files like 8hjiee-.... are placed
  * in underdirectories like 8/h/j/i/8jiee-....
  */
 public class OutputFileUtil {
 
 	private static Logger log = Logger.getLogger(Util.class);
 	private final static int depth = 4;
 
 	/**
 	 * Get the specific directory for the putput of this request. This lies 4
 	 * directories down under the base output directory.
 	 * @param request
 	 * @param config
 	 * @return
 	 */
 	public static File getOutputDir(TranscodeRequest request, ServletConfig config) {
 		File rootDir = getBaseOutputDir(request, config);
 		log.trace("Output directory is relative to '" + rootDir + "'");
 		String relativePath = "";
 		String pid = request.getPid();
 		for (int pos = 0; pos < depth; pos++) {
 			relativePath += pid.charAt(pos) + "/";
 		}
 		log.trace("Relative path is '" + relativePath + "'");
 		return new File(rootDir, relativePath);
 	}
 
 	/**
 	 * Get the root output directory for this type of request.
 	 * @param request
 	 * @param config
 	 * @return
 	 */
 	public static File getBaseOutputDir(TranscodeRequest request, ServletConfig config) {
 		ServiceTypeEnum serviceType = request.getServiceType();
 		return new File(getBaseOutputDir(serviceType, config));
 	}
 
 	/**
 	 * Get the root output directory for this service type.
	 * @param request
 	 * @param config
 	 * @return
 	 */
 	public static String getBaseOutputDir(ServiceTypeEnum serviceType, ServletConfig config) {
		String rootDir = "";
 		switch (serviceType) {
 		case BROADCAST_EXTRACTION:
 			rootDir = Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM);
 			break;
 		case DIGITV_BROADCAST_EXTRACTION:
 			rootDir = Util.getInitParameter(config, Constants.DIGITV_WORK_DIR_INIT_PARAM);
 			break;
 		case PREVIEW_GENERATION:
 			rootDir = Util.getInitParameter(config, Constants.PREVIEW_DIRECTORY);
 			break;
 		case THUMBNAIL_GENERATION:
 			rootDir = Util.getInitParameter(config, Constants.SNAPSHOT_DIRECTORY);
 			break;
 		case PREVIEW_THUMBNAIL_GENERATION:
 			rootDir = Util.getInitParameter(config, Constants.SNAPSHOT_DIRECTORY);
 			break;
 		case SHARD_ANALYSIS:
 			rootDir = Util.getInitParameter(config, Constants.ANALYSIS_DIRECTORY);
			break;
		}
		return rootDir;
 	}
 
 	/**
 	 * Determine whether any output files from this request have been created.
 	 * @param request
 	 * @param config
 	 * @return
 	 * @throws ProcessorException if unable to access files 
 	 */
 	public static boolean hasOutputFile(TranscodeRequest request, ServletConfig config) throws ProcessorException {
 		switch(request.getServiceType()) {
 		case BROADCAST_EXTRACTION:
 			return hasExtractedBroadcast(request, config);
 		case DIGITV_BROADCAST_EXTRACTION:
 			return isWorkingOnDigitvBroadcastExtraction(request, config) || hasFinsishedDigitvBroadcastExtraction(request, config);
 		case PREVIEW_GENERATION:
 			return hasPreview(request, config);
 		case THUMBNAIL_GENERATION:
 			return hasSnapshot(request, config);
 		case PREVIEW_THUMBNAIL_GENERATION:
 			return hasPreview(request, config);
 		}
 		log.error("Unexpected state for " + request);
 				return false;
 	}
 
 	/**
 	 * Return the final output directory for this request. Create it if necessary.
 	 * @param request
 	 * @param config
 	 * @return
 	 */
 	public static File getAndCreateOutputDir(TranscodeRequest request, ServletConfig config) {
 		File result = getOutputDir(request, config);
 		result.mkdirs();
 		return result;
 	}
 
 	public static File getFlashVideoOutputFile(TranscodeRequest request, ServletConfig config) {
 		return new File(getOutputDir(request, config), request.getPid() + ".flv");
 	}
 
 	public static File getFlashVideoPreviewOutputFile(TranscodeRequest request, ServletConfig config) {
 		return new File(getOutputDir(request, config), request.getPid() + ".preview.flv");
 	}
 
 	public static File getMP3AudioOutputFile(TranscodeRequest request, ServletConfig config) {
 		return new File(getOutputDir(request, config), request.getPid() + ".mp3");
 	}
 
 	public static File getMP3AudioPreviewOutputFile(TranscodeRequest request, ServletConfig config) {
 		return new File(getOutputDir(request, config), request.getPid() + ".preview.mp3");
 	}
 
 	public static boolean hasFinsishedDigitvBroadcastExtraction(TranscodeRequest request, ServletConfig config) throws ProcessorException {
 		File rootDirFinal = new File(Util.getInitParameter(config, Constants.DIGITV_FINAL_DIR_INIT_PARAM));
 		final String filenameWithoutExt = getDigitvProgramFilenameWithoutExtension(request);
 		return lookForFileInFolder(rootDirFinal, filenameWithoutExt);
 	}
 
 	public static boolean isWorkingOnDigitvBroadcastExtraction(TranscodeRequest request, ServletConfig config) throws ProcessorException {
 		File workDir = new File(Util.getInitParameter(config, Constants.DIGITV_WORK_DIR_INIT_PARAM));
 		final String filenameWithoutExt = getDigitvProgramFilenameWithoutExtension(request);
 		return lookForFileInFolder(workDir, filenameWithoutExt);
 	}
 
 	public static boolean lookForFileInFolder(File fileLocation, final String filenameWithoutExt) throws ProcessorException {
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(filenameWithoutExt);
 			}
 		};
 		log.debug("Looking for file " + filenameWithoutExt + " in: " + fileLocation.getAbsolutePath());
 		int i = 0;
 		while ((fileLocation.listFiles(filter) == null) && (i < 5)) {
 			try {
 				log.warn("Unable to access : " + fileLocation.getAbsolutePath() + ". Retrying " + i + ". " + new Date(System.currentTimeMillis()));
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				log.info("Got interrupted. Ignoring interruption. Cause: " + e.getMessage());
 				// Do nothing...
 			}
 			i++;
 		}
 		if (fileLocation.listFiles(filter) == null) {
 			log.error("Unable to access directory: " + fileLocation);
 			throw new ProcessorException("Unable to access directories at storage.");
 		}
 		boolean hasFileInFinishedDir = fileLocation.listFiles(filter).length > 0;
 		return hasFileInFinishedDir;
 	}
 
 	public static File getDigitvWorkOutputFile(TranscodeRequest request, ServletConfig config) {
 		File rootDir = getBaseOutputDir(request, config);
 		log.trace("Output directory is: '" + rootDir + "'");
 		String filename = getDigitvProgramFilename(request);
 		File result = new File(rootDir, filename);
 		log.trace("Resulting filename: " + result.getAbsoluteFile());
 		return result;
 	}
 
 	public static File getDigitvDoneFile(TranscodeRequest request, ServletConfig config) {
 		String rootDir = Util.getInitParameter(config, Constants.DIGITV_FINAL_DIR_INIT_PARAM);
 		log.trace("Output directory is: '" + rootDir + "'");
 		String filename = getDigitvProgramFilename(request);
 		File result = new File(rootDir, filename);
 		log.trace("Resulting filename: " + result.getAbsoluteFile());
 		return result;
 	}
 
 	public static File getExistingMediaOutputFile(TranscodeRequest request, ServletConfig config) {
 		switch(request.getServiceType()) {
 		case BROADCAST_EXTRACTION:
 			return getBroadcastFile(request, config);
 		case DIGITV_BROADCAST_EXTRACTION:
 			return getDigitvBroadcastFile(request, config);
 		case PREVIEW_GENERATION:
 			return getPreviewFile(request, config);
 		}
 		throw new RuntimeException("Didn't return a media output file for request '" + request.getPid() + "'");
 	}
 
 	private static File getBroadcastFile(TranscodeRequest request, ServletConfig config) {
 		final String uuid = request.getPid();
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(uuid+".") && !pathname.getName().contains("preview");
 			}
 		};
 		File outputDir = getOutputDir(request, config);
 		log.trace("Looking for output in directory '" + outputDir + "'");
 		return outputDir.listFiles(filter)[0];
 	}
 
 	private static File getDigitvBroadcastFile(TranscodeRequest request, ServletConfig config) {
 		final String filenameWithoutExtension = getDigitvProgramFilenameWithoutExtension(request);
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(filenameWithoutExtension);
 			}
 		};
 		File outputDir = getBaseOutputDir(request, config);
 		log.trace("Looking for output in directory '" + outputDir + "'");
 		return outputDir.listFiles(filter)[0];
 	}
 
 	private static File getPreviewFile(TranscodeRequest request, ServletConfig config) {
 		final String uuid = request.getPid();
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(uuid+".") && pathname.getName().contains("preview");
 			}
 		};
 		File outputDir = getOutputDir(request, config);
 		return outputDir.listFiles(filter)[0];
 	}
 
 	private static boolean hasExtractedBroadcast(TranscodeRequest request, ServletConfig config) {
 		final String uuid = request.getPid();
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(uuid+".") && !pathname.getName().contains("preview");
 			}
 		};
 		File outputDir = getOutputDir(request, config);
 		return outputDir.listFiles(filter).length > 0;
 	}
 
 	private static boolean hasPreview(TranscodeRequest request, ServletConfig config) {
 		final String uuid = request.getPid();
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(uuid+".") && pathname.getName().contains("preview");
 			}
 		};
 		File outputDir = getOutputDir(request, config);
 		if (outputDir == null) {
 			log.warn("Returned null output directory for request '" + request.getPid() + "'");
 			return false;
 		}
 		final File[] files = outputDir.listFiles(filter);
 		if (files != null) {
 			return files.length > 0;
 		} else {
 			log.warn("Null file list. Is '" + outputDir.getAbsolutePath() + "' a directory?");
 			return false;
 		}
 	}
 
 	private static boolean hasSnapshot(TranscodeRequest request,final ServletConfig config) {
 		final String uuid = request.getPid();
 		final FileFilter filter = new FileFilter(){
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith(uuid+".")
 						&& !pathname.getName().contains("preview") 
 						&& pathname.getName().endsWith(Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
 			}
 		};
 		File outputDir = getOutputDir(request, config);
 		final File[] files = outputDir.listFiles(filter);
 		if (files != null && files.length > 0) {
 			log.debug("Found existing output file '" + files[0].getAbsolutePath() + "'");
 		}
 		return files.length > 0;
 	}
 
 	public static String[] getAllSnapshotFilenames(final ServletConfig config, final TranscodeRequest request) throws ProcessorException {
 		FileFilter fileFilter = new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				boolean matchesPid = pathname.getName().contains(request.getPid());
 				boolean matchesFormat = pathname.getName().endsWith(Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
 				return matchesFormat && matchesPid;
 			}
 		};
 		File[] allFiles = getOutputDir(request, config).listFiles(fileFilter);
 		String[] filenames = new String[allFiles.length];
 		for (int i=0; i<allFiles.length; i++) {
 			filenames[i] = Util.getRelativePath(OutputFileUtil.getBaseOutputDir(request, config), allFiles[i]);
 		}
 		return filenames;
 	}
 
 	public static File[] getAllSnapshotFiles(final ServletConfig config, final TranscodeRequest request) throws ProcessorException {
 		FileFilter fileFilter = new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				boolean matchesPid = pathname.getName().contains(request.getPid());
 				boolean matchesFormat = pathname.getName().endsWith(Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
 				return matchesFormat && matchesPid;
 			}
 		};
 		return getOutputDir(request, config).listFiles(fileFilter);       
 	}
 
 
 	public static String getSnapshotBasename(ServletConfig config, TranscodeRequest request, String label, String count) {
 		return request.getPid() + "." + label + "." + count;
 	}
 
 
 	public static File getFullPrimarySnapshotFile(ServletConfig config, TranscodeRequest request, String label, String count) {
 		return new File(getAndCreateOutputDir(request, config), getSnapshotBasename(config, request, label, count) + "." + Util.getPrimarySnapshotSuffix(config));
 	}
 
 	public static File getFullFinalSnapshotFile(ServletConfig config, TranscodeRequest request, String label, String count) {
 		return new File(getAndCreateOutputDir(request, config), getSnapshotBasename(config, request, label, count) + "." + Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
 	}
 
 	public static String getDigitvProgramFilename(TranscodeRequest request) {
 		String filename = getDigitvProgramFilenameWithoutExtension(request);
 		String fullFilename = filename;
 		String extension = "";
 		if ((request.getClips() != null) && (request.getClipType()!= null)) {
 			switch (request.getClipType()) {
 			case MUX:
 				extension = "mpeg";
 				break;
 			case MPEG1:
 				extension = "mpeg";
 				break;
 			case MPEG2:
 				extension = "mpeg";
 				break;
 			case WAV:
 				extension = "mp3";
 				break;
 			default:
 				extension = "unknown";
 				break;
 			}
 			fullFilename = filename + "." + extension;
 		}
 		log.debug("Filename: " + fullFilename);
 		return fullFilename;
 	}
 
 	public static String getDigitvProgramFilenameWithoutExtension(TranscodeRequest request) {
 		String filename = request.getFilenamePrefix()
 				+ "_" + request.getUserAdditionalStartOffset() + "_" + request.getUserAdditionalEndOffset() + "_digitv";
 		return filename;
 	}
 
 }
