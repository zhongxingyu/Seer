 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU Lesser General Public License Version 3.0 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl-3.0.html
  * 
  *  - Mozilla Public License Version 2.0 or later (the "MPL")
  *    http://www.mozilla.org/MPL/2.0/
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.impl;
 
 import java.awt.Dimension;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.io.filefilter.FileFileFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.thischwa.c5c.Connector;
 import de.thischwa.c5c.FilemanagerAction;
 import de.thischwa.c5c.UserObjectProxy;
 import de.thischwa.c5c.exception.C5CException;
 import de.thischwa.c5c.exception.FilemanagerException;
 import de.thischwa.c5c.exception.FilemanagerException.Key;
 import de.thischwa.c5c.requestcycle.response.FileProperties;
 import de.thischwa.c5c.requestcycle.response.ResponseFactory;
 import de.thischwa.c5c.requestcycle.response.mode.Delete;
 import de.thischwa.c5c.requestcycle.response.mode.DownloadInfo;
 import de.thischwa.c5c.requestcycle.response.mode.Rename;
 import de.thischwa.c5c.requestcycle.response.mode.UploadFile;
 import de.thischwa.c5c.util.StringUtils;
 import de.thischwa.jii.IDimensionProvider;
 import de.thischwa.jii.core.SimpleImageInfoWrapper;
 import de.thischwa.jii.exception.ReadException;
 
 /**
  * The default implementation of the {@link Connector} interface. 
  * It's a real local filesystem backend connector. The file access is translated as-is
  * to the local filesystem. A servlet context is respected, if it exists.
  */
 public class LocalConnector implements Connector {
 	
 	private static Logger logger = LoggerFactory.getLogger(LocalConnector.class);
 	
 	public void init() {
 		logger.info("*** {} sucessful initialized.", this.getClass().getName());
 	}
 	
 	@Override
 	public List<FileProperties> getFolder(String urlPath, boolean needSize, boolean showThumbnailsInGrid) throws C5CException {
 		File folder = buildAndCheckFolder(urlPath);
 		return constructFromDirRequest(urlPath, folder, needSize, showThumbnailsInGrid);
 	}
 	
 	@Override
 	public FileProperties getInfo(String urlPath, boolean needSize, boolean showThumbnailsInGrid) throws C5CException {
 		File file = buildRealFile(urlPath);
 		if(!file.exists()) {
 			logger.error("Requested file not exits: {}", file.getAbsolutePath());
 			throw new FilemanagerException(FilemanagerAction.INFO, FilemanagerException.Key.FileNotExists, urlPath);
 		}
 		return constructFileInfo(file, needSize, showThumbnailsInGrid);
 	}
 	
 	@Override
 	public Rename rename(String oldPath, String sanitizedName) throws C5CException {
 		File src = buildRealFile(oldPath);
 		if(!src.exists()) {
 			logger.error("Source file not found: {}", src.getAbsolutePath());
 			FilemanagerException.Key key = (src.isDirectory()) ? FilemanagerException.Key.DirectoryNotExist : FilemanagerException.Key.FileNotExists;
 			throw new FilemanagerException(FilemanagerAction.RENAME, key, oldPath);
 		}
 		
 		boolean isDirectory = src.isDirectory();
 		File dest = new File(src.getParentFile(), sanitizedName);
 		if(dest.exists()) {
 			logger.warn("Destination file already exists: {}", dest.getAbsolutePath());
 			FilemanagerException.Key key = (dest.isDirectory()) ? FilemanagerException.Key.DirectoryAlreadyExists : FilemanagerException.Key.FileAlreadyExists;
 			throw new FilemanagerException(FilemanagerAction.RENAME, key, sanitizedName);
 		}
 		
 		boolean success = false;
 		try {
 			success = src.renameTo(dest);
 		} catch (SecurityException e) {
 			logger.warn(String.format("Error while renaming [%s] to [%s]", src.getAbsolutePath(), dest.getAbsolutePath()), e);
 		}
 		if(!success) {
 			FilemanagerException.Key key = (src.isDirectory()) ? FilemanagerException.Key.ErrorRenamingDirectory : FilemanagerException.Key.ErrorRenamingFile;
 			throw new FilemanagerException(FilemanagerAction.RENAME, key, oldPath, sanitizedName);
 		}
 		return ResponseFactory.buildRename(oldPath, sanitizedName, isDirectory);
 	}
 	
 	@Override
 	public void createFolder(String urlDirectory, String sanitizedFolderName) throws C5CException {
 		File parentFolder = buildAndCheckFolder(urlDirectory);
 		File newFolder = new File(parentFolder, sanitizedFolderName);
 		if(newFolder.exists()) {
 			logger.warn("Destination file already exists: {}", newFolder.getAbsolutePath());
 			throw new FilemanagerException(FilemanagerAction.CREATEFOLDER, Key.DirectoryAlreadyExists, sanitizedFolderName);
 		}
 
 		boolean success = false;
 		try {
 			success = newFolder.mkdirs();
 		} catch (SecurityException e) {
 			logger.warn(String.format("Error while creating folder [%s]", newFolder.getAbsolutePath()), e);
 		}
 		if(!success) {
 			throw new FilemanagerException(FilemanagerAction.RENAME, FilemanagerException.Key.UnableToCreateDirectory, sanitizedFolderName);
 		}
 	}
 	
 	/**
 	 * Builds the and check folder.
 	 *
 	 * @param urlPath the url path
 	 * @return the file
 	 * @throws FilemanagerException the known exception
 	 */
 	private File buildAndCheckFolder(String urlPath) throws FilemanagerException {
 		File parentFolder = buildRealFile(urlPath);
 		if(!parentFolder.exists()) {
 			logger.error("Source file not found: {}", parentFolder.getAbsolutePath());
 			FilemanagerException.Key key = (parentFolder.isDirectory()) ? FilemanagerException.Key.DirectoryNotExist : FilemanagerException.Key.FileNotExists;
 			throw new FilemanagerException(FilemanagerAction.CREATEFOLDER, key, urlPath);
 		}		
 		return parentFolder;
 	}
 
 	/**
 	 * Builds the real file.
 	 *
 	 * @param urlPath the url path
 	 * @return the file
 	 */
 	private File buildRealFile(String urlPath) {
 		String path = UserObjectProxy.getUserPath(urlPath);
 		return new File(path);
 	}
 	
 	@Override
 	public Delete delete(String urlPath) throws C5CException {
 		File file = buildRealFile(urlPath);
 		if(!file.exists()) {
 			logger.error("Requested file not exits: {}", file.getAbsolutePath());
 			FilemanagerException.Key key = (file.isDirectory()) ? FilemanagerException.Key.DirectoryNotExist : FilemanagerException.Key.FileNotExists;
 			throw new FilemanagerException(FilemanagerAction.DELETE, key, urlPath);
 		}
 		boolean success = false;
 		boolean isDir = file.isDirectory();
 		if(isDir) {
 			try {
 				FileUtils.deleteDirectory(file);
 				success = true;
 			} catch (IOException e) {
 			}
 		} else {
 			success = FileUtils.deleteQuietly(file);
 		}
 		if(!success) 
 			throw new FilemanagerException(FilemanagerAction.DELETE, FilemanagerException.Key.InvalidDirectoryOrFile, urlPath);
 		return ResponseFactory.buildDelete(urlPath, isDir);
 	}
 
 	/**
 	 * Construct file info.
 	 * 
 	 * @param file the file
 	 * @param needSize the need size
 	 * @param showThumbnailsInGrid the show thumbnails in grid
 	 * @param isDirRequest the is dir request
 	 *
 	 * @return the file info
 	 * @throws C5CException the connector exception
 	 */
 	private FileProperties constructFileInfo(File file, boolean needSize, boolean showThumbnailsInGrid) throws C5CException {
 		try {
 			FileProperties fileProperties = ResponseFactory.buildFileProperties(file.getName(), file.isDirectory(), file.length(), new Date(file.lastModified()));
 			// 'needsize' isn't implemented in the filemanager yet, so the dimension is set if we have an image.
 			String ext = FilenameUtils.getExtension(file.getPath());
			Set<String> allowedImageExtensions = UserObjectProxy.getFilemanagerConfig().getImages().getExtensions();
 			if(!StringUtils.isNullOrEmptyOrBlank(ext) && allowedImageExtensions.contains(ext)) {
 				IDimensionProvider dp = new SimpleImageInfoWrapper();
 				dp.set(file);
 				Dimension dim = dp.getDimension();
 				fileProperties.setSize(dim);
 			}
 			return fileProperties;
 		} catch (SecurityException e) {
 			throw new C5CException(String.format("Error while analysing %s: %s", file.getPath(), e.getMessage()));
 		} catch (ReadException e) {
 			throw new C5CException(String.format("Error while getting the dimension of the image %s: %s", file.getPath(), e.getMessage()));			
 		} catch (FileNotFoundException e) {
 			throw new C5CException(String.format("File not found: %s", file.getPath()));
 		} 
 	}
 
 	/**
 	 * Construct from dir request.
 	 * @param urlPath the url path
 	 * @param dir the dir
 	 * @param needSize the need size
 	 * @param showThumbnailsInGrid the show thumbnails in grid
 	 * @return the folder info
 	 * @throws C5CException the connector exception
 	 */
 	private List<FileProperties> constructFromDirRequest(String urlPath, File dir, boolean needSize, boolean showThumbnailsInGrid) throws C5CException {
 		List<FileProperties> props = new ArrayList<>();
 		// add dirs
 		File[] fileList = dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
 		for (File file : fileList) {
 			FileProperties fp = new FileProperties(file.getName(), new Date(file.lastModified()));
 			fp.setDir(true);
 			props.add(fp);
 		}
 
 		// add files
 		fileList = dir.listFiles((FileFilter) FileFileFilter.FILE);
 		for (File file : fileList) {
 			props.add(constructFileInfo(file, needSize, showThumbnailsInGrid));
 		}
 		return props;
 	}
 	
 	@Override
 	public UploadFile upload(String urlDirectory, String sanitizedName, InputStream in, Integer maxFileSize) throws C5CException {
 		File parentFolder = buildAndCheckFolder(urlDirectory);
 		File fileToSave = new File(parentFolder, sanitizedName);
 		if(fileToSave.exists())
 			throw new FilemanagerException(FilemanagerException.Key.FileAlreadyExists, urlDirectory);
 		try {
 			Long size = IOUtils.copyLarge(in, new FileOutputStream(fileToSave));
 			if(maxFileSize != null && size > maxFileSize.longValue() * 1024 * 1024)
 				throw new FilemanagerException(FilemanagerAction.UPLOAD, FilemanagerException.Key.UploadFilesSmallerThan, maxFileSize.toString());
 			UploadFile uf = ResponseFactory.buildUploadFile(urlDirectory, sanitizedName, size);
 			return uf;
 		} catch (IOException e) {
 			throw new FilemanagerException(FilemanagerAction.UPLOAD, FilemanagerException.Key.InvalidFileUpload, urlDirectory);
 		}
 	}
 	
 	@Override
 	public DownloadInfo download(String urlPath) throws C5CException {
 		File file = buildRealFile(urlPath);
 		try {
 			InputStream in = new BufferedInputStream(new FileInputStream(file));
 			return ResponseFactory.buildDownloadInfo(in, file.length());
 		} catch (FileNotFoundException e) {
 			logger.error("Requested file not exits: {}", file.getAbsolutePath());
 			throw new FilemanagerException(FilemanagerAction.DOWNLOAD, FilemanagerException.Key.FileNotExists, urlPath);
 		}
 	}
 }
