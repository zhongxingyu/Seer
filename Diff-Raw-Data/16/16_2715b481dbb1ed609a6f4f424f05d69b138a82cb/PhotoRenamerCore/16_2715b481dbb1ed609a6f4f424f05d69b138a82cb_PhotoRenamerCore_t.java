 package org.zephir.photorenamer.core;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zephir.photorenamer.dao.JpegDAO;
 import org.zephir.util.RETokenizer;
 import org.zephir.util.exception.CustomException;
 
 public class PhotoRenamerCore {
 	private static Logger log = LoggerFactory.getLogger(PhotoRenamerCore.class);
 	private File folderToProcess;
 	private String pattern;
 	private String suffix;
 	private long deltaInSeconds;
 	private boolean renameExtraFiles = false;
 	private boolean rotateImages = false;
 
 	public PhotoRenamerCore() { }
 
 	public void processFolder() throws CustomException {
 		if (!getFolderToProcess().exists()) {
 			log.error("processFolder() KO: folder must exist ('" + folderToProcess.getAbsolutePath() + "')");
 			throw new CustomException("processFolder() KO: folder must exist ('" + folderToProcess.getAbsolutePath() + "')");
 		}
 		final String folderStr = "Folder to process: '"+getFolderToProcess().getAbsolutePath()+"'";
 		log.info(StringUtils.repeat("_", folderStr.length()));
 		log.info(folderStr);
 		log.info("Delta in seconds: "+getDeltaInSeconds()+"s");
 		log.info("Pattern: '"+getPattern()+"'");
 		log.info("Suffix: '"+getSuffix()+"'");
 		log.info("Rename extra files: "+renameExtraFiles());
 		log.info(StringUtils.repeat("", folderStr.length()));
 
 		int nbPhotosRenamed = 0;
 		int nbPhotosRotated = 0;
 		int nbVideosRenamed = 0;
 		for (File f : getFolderToProcess().listFiles()) {
 			if (!f.exists()) {
 				log.debug("processFolder() file deleted: '" + f.getAbsolutePath() + "'");
 			} else {
 				if (f.isFile()) {
 					//file
 					if (isFileExtensionInList(f, PhotoRenamerConstants.FILES_WITH_METADATA_EXTENSION_LIST)) {
 						try {
 							File newFile = renamePhoto(f);
 							nbPhotosRenamed++;
 							if (rotateImages && isFileExtensionInList(f, PhotoRenamerConstants.FILES_ROTABLE)) {
 								boolean isRotated = rotatePhoto(newFile);
 								if (isRotated) {
 									nbPhotosRotated++;
 								}
 							}
 						} catch (CustomException e) {
 							log.error("Error during renaming of the photo '"+f.getAbsoluteFile()+"': " + e, e);
 						}
 					} else if (isFileExtensionInList(f, PhotoRenamerConstants.FILES_TO_RENAME_EXTENSION_LIST)) {
 						if (renameExtraFiles) {
 							try {
 								renameVideo(f);
 								nbVideosRenamed++;
 							} catch (CustomException e) {
 								log.error("Error during renaming of the video '"+f.getAbsoluteFile()+"': " + e);
 							}
 						} else {
 							log.debug("Video skipped: '" + f.getAbsolutePath() + "'");
 						}
 					} else {
 						log.debug("File isn't a JPEG image or video: '" + f.getAbsolutePath() + "'");
 					}
 				}
 			}
 		}
 		log.debug(nbPhotosRenamed + " photo(s) ("+nbPhotosRotated+" rotated) and " + nbVideosRenamed + " video(s) renamed");
 		log.debug("");
 	}
 	
 	private boolean isFileExtensionInList(final File f, final List<String> extensionList) {
 		boolean lRes = false;
 		final String lowerCaseName = f.getName().toLowerCase();
 		for (final String extension: extensionList) {
 			if (lowerCaseName.endsWith(extension)) {
 				lRes = true;
 				break;
 			}
 		}
 		return lRes;
 	}
 	
 	private boolean rotatePhoto(final File f) throws CustomException {
 		try {
 			return JpegDAO.rotateImage(f);
 		} catch (CustomException e) {
 			throw e;
 		} catch (Exception e) {
 			log.error("rotatePhoto(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 			throw new CustomException("rotatePhoto(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 		}
 	}
 
 	private File renamePhoto(final File f) throws CustomException {
 		try {
 			//get file original date
 			Date originalDate = JpegDAO.getDateTimeOriginal(f);
			File newFile = f;
			if (originalDate != null) {
				newFile = renameFile(f, originalDate);
			} else{
				log.warn("Can't rename photo '"+f.getAbsoluteFile()+"': No EXIF data.");
			}
			return newFile;
 		} catch (CustomException e) {
 			throw e;
 		} catch (Exception e) {
 			log.error("renamePhoto(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 			throw new CustomException("renamePhoto(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 		}
 	}
 
 	private void renameVideo(final File f) throws CustomException {
 		try {
 			//get file original date
 			Date lastModifiedDate = new Date(f.lastModified());
 
 			renameFile(f, lastModifiedDate);
 		} catch (CustomException e) {
 			throw e;
 		} catch (Exception e) {
 			log.error("renameVideo(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 			throw new CustomException("renameVideo(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 		}
 	}
 
 	private File renameFile(final File f, final Date originalDate) throws CustomException {
 		try {
 			String oldFilename = f.getAbsolutePath();
 
 			//apply delta in seconds
 			long newTime = originalDate.getTime() + getDeltaInSeconds() * 1000;
 			Date newDate = new Date(newTime);
 
 			//get new filename for the photo
 			SimpleDateFormat sdf = new SimpleDateFormat(getPattern());
 			String newDateStr = sdf.format(newDate);
 			String newFilename = f.getAbsoluteFile().getParent() + File.separatorChar + newDateStr;
 
 			String oldFilenameWithoutExtension = f.getName().substring(0, f.getName().lastIndexOf("."));
 			String extension = f.getName().substring(f.getName().lastIndexOf(".")).toLowerCase();
 			String suffixStr =  getSuffix().replaceAll(PhotoRenamerConstants.SUFFIX_ORIGINAL_FILENAME_TOKEN, oldFilenameWithoutExtension) + extension;
 
 			int suffixIteration = 0;
 			File newFile = new File(newFilename + suffixStr);
 			while(newFile.exists()) {
 				if (newFile.getAbsolutePath().equals(oldFilename)) { break; }
 				suffixIteration++;
 				newFile = new File(newFilename + "_" + suffixIteration + suffixStr);
 			}
 			
 			//rename file
 			f.renameTo(newFile);
 			log.debug("'"+f.getAbsolutePath()+"' ---> '"+newFile.getName()+"'");
 
 			return newFile;
 		} catch (Exception e) {
 			log.error("renameFile(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 			throw new CustomException("renameFile(f='"+f.getAbsolutePath()+"') KO: " + e, e);
 		}
 	}
 
 	public File getFolderToProcess() {
 		if (folderToProcess == null) {
 			folderToProcess = new File(PhotoRenamerConstants.USER_DIR);
 		}
 		return folderToProcess;
 	}
 
 	public void setFolderToProcess(final String folderNameToProcess) {
 		if (folderNameToProcess == null) {
 			folderToProcess = null;
 		} else {
 			folderToProcess = new File(folderNameToProcess);
 		}
 	}
 
 	public String getPattern() {
 		if (pattern == null) {
 			pattern = PhotoRenamerConstants.DEFAULT_PATTERN;
 		}
 		return pattern;
 	}
 
 	public void setPattern(final String pattern) {
 		this.pattern = pattern;
 	}
 
 	public long getDeltaInSeconds() {
 		return deltaInSeconds;
 	}
 
 	public void setDeltaInSeconds(final long deltaInSeconds) {
 		this.deltaInSeconds = deltaInSeconds;
 	}
 
 	public void setDelta(String delta) throws CustomException {
 		if (delta == null || delta.length() == 0) {
 			delta = "0s";
 		}
 		long deltaInSecond = 0;
 		Iterator<String> tokenizer = new RETokenizer(delta, "[a-z]", true);
 		for (; tokenizer.hasNext(); ) {
 		    String valueStr = tokenizer.next();
 		    String unit = tokenizer.next().toLowerCase();
 		    int value = Integer.parseInt(valueStr);
 		    if (unit.equals("s")) {
 		    	deltaInSecond += value;
 			} else if (unit.equals("m")) {
 				deltaInSecond += value * 60;
 			} else if (unit.equals("h")) {
 				deltaInSecond += value * 60 * 60;
 			} else if (unit.equals("d")) {
 				deltaInSecond += value * 60 * 60 * 24;
 			} else {
 				log.debug("setDelta("+delta+") KO: unit '"+unit+"' not recognized (unit possible: s, m, h, d)");
 				throw new CustomException("setDelta("+delta+") KO: unit '"+unit+"' not recognized (unit possible: s, m, h, d)");
 			}
 		}
 		setDeltaInSeconds(deltaInSecond);
 	}
 
 	public String getSuffix() {
 		if (suffix == null) {
 			suffix = "";
 		}
 		return suffix;
 	}
 
 	public void setSuffix(final String suffix) {
 		this.suffix = suffix;
 	}
 
 	public boolean renameExtraFiles() {
 		return renameExtraFiles;
 	}
 
 	public void setRenameExtraFiles(final boolean renameExtraFiles) {
 		this.renameExtraFiles = renameExtraFiles;
 	}
 
 	public void setRotateImages(final boolean rotateImages) {
 		this.rotateImages = rotateImages;
 	}
 }
