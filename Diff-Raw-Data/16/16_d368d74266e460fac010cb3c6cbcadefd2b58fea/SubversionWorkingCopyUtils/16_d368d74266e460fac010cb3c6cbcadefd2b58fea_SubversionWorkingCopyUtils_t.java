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
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 
 import net.lmxm.ute.event.StatusChangeHelper;
 import net.lmxm.ute.resources.types.StatusChangeMessageResourceType;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 /**
  * The Class SubversionWorkingCopyUtils.
  */
 public final class SubversionWorkingCopyUtils extends AbstractSubversionUtils {
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(SubversionWorkingCopyUtils.class);
 
 	/**
 	 * Checks if is working copy.
 	 * 
 	 * @param path the path
 	 * 
 	 * @return true, if is working copy
 	 */
 	private static boolean isWorkingCopy(final String path) {
 		final String prefix = "isWorkingCopy() :";
 
 		LOGGER.debug("{} entered, path={}", prefix, path);
 
 		boolean isWorkingCopy;
 
 		try {
 			isWorkingCopy = SVNWCUtil.isWorkingCopyRoot(new File(path));
 		}
 		catch (final SVNException e) {
 			LOGGER.debug(
 					"{} SVNException caught calling SVNWCUtil.isWorkingCopyRoot(), assuming that the path is not a working copy",
 					prefix, path);
 
 			isWorkingCopy = false;
 		}
 
 		LOGGER.debug("{} returning {}", prefix, isWorkingCopy);
 
 		return isWorkingCopy;
 	}
 
 	/**
 	 * Instantiates a new subversion working copy utils.
 	 * 
 	 * @param username the username
 	 * @param password the password
 	 * @param statusChangeHelper the status change helper
 	 */
 	public SubversionWorkingCopyUtils(final String username, final String password,
 			final StatusChangeHelper statusChangeHelper) {
 		super(username, password, statusChangeHelper);
 	}
 
 	/**
 	 * Update working copy.
 	 * 
 	 * @param path the path
 	 */
 	public void updateWorkingCopy(final String path) {
 		final String prefix = "updateWorkingCopy() :";
 
 		LOGGER.debug("{} entered, path={}", prefix, path);
 
 		final String pathTrimmed = StringUtils.trimToNull(path);
 
 		checkNotNull(pathTrimmed, "Path must not be blank");
 		checkState(isWorkingCopy(pathTrimmed), "Path must be a working copy root");
 
 		try {
 			LOGGER.debug("{} start updating working copy", prefix);
 
 			getStatusChangeHelper().important(this, StatusChangeMessageResourceType.SUBVERSION_UPDATE_STARTED,
 					pathTrimmed);
 
 			final DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
 			final SVNClientManager clientManager = SVNClientManager.newInstance(options);
 			clientManager.setAuthenticationManager(getAuthenticationManager());
 
 			final SVNUpdateClient updateClient = clientManager.getUpdateClient();
 			updateClient.setEventHandler(new EventHandler(getStatusChangeHelper()));
 			updateClient.doUpdate(new File(pathTrimmed), SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
 
 			getStatusChangeHelper().important(this, StatusChangeMessageResourceType.SUBVERSION_UPDATE_FINISHED,
 					pathTrimmed);
 
 			LOGGER.debug("{} finished updating working copy", prefix);
 		}
 		catch (final SVNAuthenticationException e) {
 			LOGGER.error("SVNAuthenticationException caught exporting a file", e);
			getStatusChangeHelper().error(this, StatusChangeMessageResourceType.SUBVERSION_AUTHENTICATION_FAILED);
 			throw new RuntimeException(e); // TODO Use appropriate exception
 		}
 		catch (final SVNException e) {
 			LOGGER.error("SVNException caught while updating working copy", e);
 			getStatusChangeHelper().error(this, StatusChangeMessageResourceType.SUBVERSION_UPDATE_ERROR, pathTrimmed);
 			throw new RuntimeException(e); // TODO Use appropriate exception
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 }
