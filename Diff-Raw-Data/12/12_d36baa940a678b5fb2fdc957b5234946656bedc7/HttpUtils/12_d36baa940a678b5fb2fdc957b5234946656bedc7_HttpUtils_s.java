 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.utils;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import net.lmxm.ute.beans.FileReference;
 import net.lmxm.ute.beans.locations.HttpLocation;
 import net.lmxm.ute.beans.sources.HttpSource;
 import net.lmxm.ute.listeners.StatusChangeEvent;
 import net.lmxm.ute.listeners.StatusChangeEventType;
 import net.lmxm.ute.listeners.StatusChangeListener;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 
 /**
  * The Class HttpUtils.
  */
 public class HttpUtils {
 
 	/** The Constant INSTANCE. */
 	private static final HttpUtils INSTANCE = new HttpUtils();
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
 
 	/**
 	 * Gets the full url.
 	 * 
 	 * @param source the source
 	 * @return the full url
 	 */
 	public static String getFullUrl(final HttpSource source) {
 		final String prefix = "execute() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		Preconditions.checkNotNull(source, "Source may not be null");
 
 		final HttpLocation location = source.getLocation();
 		Preconditions.checkNotNull(location, "Source location may not be null");
 
 		final String url = StringUtils.trimToNull(location.getUrl());
 		Preconditions.checkNotNull(url, "Source location url may not be blank");
 
 		final String relativePath = source.getRelativePath();
 		// Relative path may be null, StringUtils.join() will treat null values as empty strings
 
 		final String fullUrl = PathUtils.buildFullPath(url, relativePath);
 
 		LOGGER.debug("{} returning {}", prefix, fullUrl);
 
 		return fullUrl;
 	}
 
 	/**
 	 * Gets the single instance of HttpUtils.
 	 * 
 	 * @return single instance of HttpUtils
 	 */
 	public static HttpUtils getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Instantiates a new http utils.
 	 */
 	private HttpUtils() {
 		super();
 	}
 
 	/**
 	 * Download file.
 	 * 
 	 * @param sourceUrl the source url
 	 * @param destinationFilePath the destination file path
 	 * @param statusChangeListener the status change listener
 	 */
 	private void downloadFile(final String sourceUrl, final String destinationFilePath,
 			final StatusChangeListener statusChangeListener) {
 		final String prefix = "execute() :";
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("{} entered", prefix);
 			LOGGER.debug("{} sourceUrl={}", prefix, sourceUrl);
 			LOGGER.debug("{} destinationFilePath={}", prefix, destinationFilePath);
 		}
 
 		try {
 			final DefaultHttpClient httpClient = new DefaultHttpClient();
 			final HttpGet httpGet = new HttpGet(sourceUrl);
 			final HttpResponse response = httpClient.execute(httpGet);
 
 			final HttpEntity entity = response.getEntity();
 
 			if (entity == null) {
 				statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.ERROR,
 						"No response received from the remote server. The file may not exist."));
 
 				throw new RuntimeException(); // TODO Use appropriate exception
 			}
 			else {
 				final InputStream inputStream = entity.getContent();
 				final OutputStream out = new FileOutputStream(destinationFilePath);
 				final byte buffer[] = new byte[1024];
 				int length;
 
 				while ((length = inputStream.read(buffer)) > 0) {
 					out.write(buffer, 0, length);
 				}
 
 				out.close();
 
 				statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.INFO,
 						"Successfully download " + sourceUrl));
 			}
 		}
 		catch (final ClientProtocolException e) {
 			LOGGER.debug("ClientProtocolException caught", e);
 
 			statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.ERROR,
 					"Error occurred downloading " + sourceUrl));
 
 			throw new RuntimeException(); // TODO Use appropriate exception
 		}
 		catch (final IOException e) {
 			LOGGER.debug("IOException caught", e);
 
 			statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.ERROR,
 					"Error occurred downloading " + sourceUrl + " to " + destinationFilePath));
 
 			throw new RuntimeException(); // TODO Use appropriate exception
 		}
 		catch (final Exception e) {
 			LOGGER.debug("Exception caught", e);
 
 			statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.ERROR,
 					"Error occurred downloading " + sourceUrl + " to " + destinationFilePath));
 
 			throw new RuntimeException(); // TODO Use appropriate exception
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Download files.
 	 * 
 	 * @param url the url
 	 * @param destinationPath the destination path
 	 * @param files the files
 	 * @param statusChangeListener the status change listener
 	 */
 	public void downloadFiles(final String url, final String destinationPath, final List<FileReference> files,
 			final StatusChangeListener statusChangeListener) {
 		final String prefix = "downloadFiles() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		Preconditions.checkArgument(StringUtils.isNotBlank(url), "URL may not be blank or null");
 		Preconditions.checkArgument(StringUtils.isNotBlank(destinationPath),
 				"Destination path may not be blank or null");
 		Preconditions.checkNotNull(statusChangeListener, "Status change listener may not be null");
 
 		FileSystemUtils.getInstance().createDirectory(destinationPath);
 
 		if (files == null || files.size() == 0) {
 			LOGGER.error("{} downloadFiles", prefix);
 
 			statusChangeListener.statusChange(new StatusChangeEvent(this, StatusChangeEventType.FATAL,
 					"List of files is empty"));
 
 			throw new IllegalArgumentException("List of files is empty"); // TODO Use appropriate exception
 		}
 
 		// Download each file
 		for (final FileReference fileReference : files) {
 			LOGGER.debug("{} downloading file {}", prefix, fileReference.getName());
 
 			final String sourceUrl = PathUtils.buildFullPath(url, fileReference.getName());
 			final String name = fileReference.getName();
 			final String targetName = fileReference.getTargetName();
 			final String targetFileName = StringUtils.isBlank(targetName) ? name : targetName;
 			final String destinationFilePath = PathUtils.buildFullPath(destinationPath, targetFileName);
 
 			downloadFile(sourceUrl, destinationFilePath, statusChangeListener);
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 }
