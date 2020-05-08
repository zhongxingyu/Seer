 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executer.
  * 
  * Universal Task Executer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executer is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executer. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.subversion.utils;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import net.lmxm.ute.beans.FileReference;
 import net.lmxm.ute.enums.SubversionDepth;
 import net.lmxm.ute.enums.SubversionRevision;
 import net.lmxm.ute.event.StatusChangeHelper;
 import net.lmxm.ute.exceptions.ConfigurationException;
 import net.lmxm.ute.resources.types.ExceptionResourceType;
 import net.lmxm.ute.resources.types.StatusChangeMessageResourceType;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.io.ISVNReporterBaton;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 
 /**
  * The Class SubversionUtils.
  */
 public final class SubversionRepositoryUtils extends AbstractSubversionUtils {
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(SubversionRepositoryUtils.class);
 
 	/**
 	 * Instantiates a new subversion repository utils.
 	 * 
 	 * @param username the username
 	 * @param password the password
 	 * @param statusChangeHelper the status change helper
 	 */
 	public SubversionRepositoryUtils(final String username, final String password,
 			final StatusChangeHelper statusChangeHelper) {
 		super(username, password, statusChangeHelper);
 	}
 
 	/**
 	 * Convert subversion depth to svn depth.
 	 * 
 	 * @param depth the depth
 	 * @return the sVN depth
 	 */
 	private SVNDepth convertSubversionDepthToSvnDepth(final SubversionDepth depth) {
 		final String prefix = "convertSubversionDepthToSvnDepth() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SVNDepth svnDepth;
 
 		if (depth == SubversionDepth.EMPTY) {
 			svnDepth = SVNDepth.EMPTY;
 		}
 		else if (depth == SubversionDepth.FILES) {
 			svnDepth = SVNDepth.FILES;
 		}
 		else if (depth == SubversionDepth.IMMEDIATES) {
 			svnDepth = SVNDepth.IMMEDIATES;
 		}
 		else if (depth == SubversionDepth.INFINITY) {
 			svnDepth = SVNDepth.INFINITY;
 		}
 		else {
 			LOGGER.error("{} : Unsupported Subversion depth \"{}\"", prefix, depth);
 			throw new ConfigurationException(ExceptionResourceType.UNSUPPORTED_SUBVERSION_DEPTH, depth);
 		}
 
 		LOGGER.debug("{} returning {}", prefix, svnDepth);
 
 		return svnDepth;
 	}
 
 	/**
 	 * Export files.
 	 * 
 	 * @param urlString the url
 	 * @param destinationPath the path
 	 * @param files the files
 	 * @param depth the depth
 	 * @param revision the revision
 	 * @param revisionDate the revision date
 	 * @param revisionNumber the revision number
 	 */
 	public void exportFiles(final String urlString, final String destinationPath, final List<FileReference> files,
 			final SubversionDepth depth, final SubversionRevision revision, final Date revisionDate,
 			final Long revisionNumber) {
 		final String prefix = "exportFiles() :";
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("{} entered", prefix);
 			LOGGER.debug("{} urlString={}", prefix, urlString);
 			LOGGER.debug("{} destinationPath={}", prefix, destinationPath);
 			LOGGER.debug("{} files={}", prefix, files);
 		}
 
 		checkNotNull(urlString, "URL must not be null");
 		checkNotNull(destinationPath, "Destination path must not be null");
 
 		try {
 			getStatusChangeHelper().important(this, StatusChangeMessageResourceType.SUBVERSION_EXPORT_STARTED);
 
 			getStatusChangeHelper().info(this, StatusChangeMessageResourceType.SUBVERSION_EXPORT_FILE, urlString,
 					destinationPath);
 
 			final SVNURL url = SVNURL.parseURIEncoded(urlString);
 			final File exportDirectory = new File(destinationPath);
 
 			if (exportDirectory.exists()) {
 				LOGGER.debug("{} export directory already exists", prefix);
 			}
 			else {
 				if (exportDirectory.mkdirs()) {
 					LOGGER.debug("{} successfully created export directories", prefix);
 				}
 				else {
 					LOGGER.error("{} unable to create export directories", prefix);
 
 					throw new RuntimeException("Unable to create export directories"); // TODO Use appropriate exception
 				}
 			}
 
 			final SVNRepository repository = SVNRepositoryFactory.create(url);
 
 			repository.setAuthenticationManager(getAuthenticationManager());
 
 			final SVNNodeKind nodeKind = repository.checkPath("", -1);
 
 			if (nodeKind == SVNNodeKind.NONE) {
 				LOGGER.error("{} No entry at URL {}", prefix, url);
 
 				final SVNErrorMessage err = SVNErrorMessage
 						.create(SVNErrorCode.UNKNOWN, "No entry at URL ''{0}''", url);
 				throw new SVNException(err);
 			}
 			else if (nodeKind == SVNNodeKind.FILE) {
 				LOGGER.error("{} Entry at URL {} is a file while directory was expected", prefix, url);
 
 				final SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN,
 						"Entry at URL ''{0}'' is a file while directory was expected", url);
 				throw new SVNException(err);
 			}
 
 			final long revisionToExport = resolveRevision(repository, revision, revisionDate, revisionNumber);
 
 			if (CollectionUtils.isEmpty(files)) {
 				LOGGER.debug("{} files list is empty, exporting entire directory", prefix);
 
 				final ISVNReporterBaton reporterBaton = new ExportReporterBaton(revisionToExport,
 						getStatusChangeHelper());
 				final SubversionExportEditor exportEditor = new SubversionExportEditor(exportDirectory,
 						getStatusChangeHelper());
 				final SVNDepth svnDepth = convertSubversionDepthToSvnDepth(depth);
 
				repository.update(revisionToExport, null, svnDepth, true, reporterBaton, exportEditor);
 			}
 			else {
 				LOGGER.debug("{} files list contains entries, exporting {} individual files", prefix, files.size());
 
 				final File directory = new File(destinationPath);
 
 				for (final FileReference fileReference : files) {
 					final String fileName = fileReference.getName();
 					final String targetName = fileReference.getTargetName();
 					final String targetFileName = StringUtils.isBlank(targetName) ? fileName : targetName;
 
 					LOGGER.debug("{} exporting file {}", prefix, fileName);
 
 					FileOutputStream contents = null;
 
 					try {
 						final File destinationFile = new File(directory, targetFileName);
 
 						if (destinationFile.exists()) {
 							LOGGER.debug("{} destination file {} exists, deleting", prefix, targetFileName);
 						}
 
 						contents = new FileOutputStream(destinationFile);
 						repository.getFile(fileName, revisionToExport, null, contents);
 						contents.close();
 
 						if (StringUtils.isBlank(targetName)) {
 							getStatusChangeHelper().info(this,
 									StatusChangeMessageResourceType.SUBVERSION_EXPORT_FILE_ADDED, fileName);
 						}
 						else {
 							getStatusChangeHelper().info(this,
 									StatusChangeMessageResourceType.SUBVERSION_EXPORT_FILE_ADDED_AS, fileName,
 									targetName);
 						}
 					}
 					catch (final FileNotFoundException e) {
 						LOGGER.error("FileNotFoundException caught exporting a file", e);
 
 						throw new RuntimeException(e);
 					}
 					catch (final IOException e) {
 						LOGGER.error("IOException caught exporting a file", e);
 
 						throw new RuntimeException(e);
 					}
 					finally {
 						if (contents != null) {
 							try {
 								contents.close();
 							}
 							catch (final Exception e) {
 								LOGGER.error("Exception caught closing the destination file", e);
 							}
 						}
 					}
 				}
 			}
 
 			LOGGER.debug("{} finished exporting files", prefix);
 
 			getStatusChangeHelper().important(this, StatusChangeMessageResourceType.SUBVERSION_EXPORT_FINISHED,
 					revisionToExport);
 		}
 		catch (final SVNAuthenticationException e) {
 			LOGGER.error("SVNAuthenticationException caught exporting a file", e);
 			getStatusChangeHelper().error(this, StatusChangeMessageResourceType.SUBVERSION_AUTHENTICATION_FAILED);
 			throw new RuntimeException(e); // TODO Use appropriate exception
 		}
 		catch (final SVNException e) {
 			LOGGER.error("SVNException caught exporting a file", e);
 			getStatusChangeHelper().error(this, StatusChangeMessageResourceType.SUBVERSION_EXPORT_ERROR);
 			throw new RuntimeException(e); // TODO Use appropriate exception
 		}
 	}
 
 	/**
 	 * Resolve revision.
 	 * 
 	 * @param repository the repository
 	 * @param revision the revision
 	 * @param revisionDate the revision date
 	 * @param revisionNumber the revision number
 	 * @return the long
 	 * @throws SVNException the sVN exception
 	 */
 	private long resolveRevision(final SVNRepository repository, final SubversionRevision revision,
 			final Date revisionDate, final Long revisionNumber) throws SVNException {
 		final String prefix = "resolveRevision() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final long revisionToExport;
 
 		if (revision == SubversionRevision.DATE) {
 			revisionToExport = repository.getDatedRevision(revisionDate);
 		}
 		else if (revision == SubversionRevision.HEAD) {
 			revisionToExport = repository.getLatestRevision();
 		}
 		else if (revision == SubversionRevision.NUMBERED) {
 			revisionToExport = revisionNumber;
 		}
 		else {
 			LOGGER.error("{} unsupported revision type {}", prefix, revision);
 			throw new RuntimeException("Unsupported revision type"); // TODO
 		}
 
 		LOGGER.debug("{} returning {}", prefix, revisionToExport);
 
 		return revisionToExport;
 	}
 }
