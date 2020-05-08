 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.crawler.fs.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.nio.channels.FileChannel;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.charset.ACharsetDetectorInputStream;
 import org.paxle.core.charset.ICharsetDetector;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.core.mimetype.IMimeTypeDetector;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.crawler.CrawlerContext;
 import org.paxle.crawler.CrawlerTools;
 import org.paxle.crawler.ICrawlerContext;
 import org.paxle.crawler.CrawlerTools.DirlistEntry;
 import org.paxle.crawler.fs.IFsCrawler;
 
 /**
  * @scr.component
  * @scr.service interface="org.paxle.crawler.ISubCrawler"
  * @scr.property name="Protocol" value="file"
  */
 public class FsCrawler implements IFsCrawler {
 	
 	private final Log logger = LogFactory.getLog(FsCrawler.class);
 	
 	public ICrawlerDocument request(URI location) {
 		
 		final ICrawlerContext ctx = CrawlerContext.getCurrentContext();
 		if (ctx == null) throw new IllegalStateException("Cannot access CrawlerContext from " + Thread.currentThread().getName());
 				
 		ICrawlerDocument cdoc = null;
 		try {
 			// creating an empty crawler-document
 			cdoc = ctx.createDocument();
 			
 			final ICommandProfile cmdProfile = ctx.getCommandProfile();
 			boolean omitHidden = true;
 			boolean inclParent = false;
 			int readMode = VAL_READ_MODE_STD;
 			if (cmdProfile != null) {
 				Serializable val;
 				if ((val = cmdProfile.getProperty(PROP_VALIDATE_NOT_HIDDEN)) != null)
 					omitHidden = ((Boolean)val).booleanValue();
 				if ((val = cmdProfile.getProperty(PROP_READ_MODE)) != null)
 					readMode = ((Integer)val).intValue();
 				if ((val = cmdProfile.getProperty(PROP_INCLUDE_PARENT_DIR)) != null)
 					inclParent = ((Boolean)val).booleanValue();
 			}
 			
 			ICrawlerDocument.Status status = ICrawlerDocument.Status.OK;
 			String err = null;
 			final File file = new File(location);
 			
 			if (!file.exists()) {
 				err = "File not found";
 				status = ICrawlerDocument.Status.NOT_FOUND;
 			} else if (!file.canRead()) {
 				err = "Read permission denied";
 				status = ICrawlerDocument.Status.UNKNOWN_FAILURE;/*		java 1.6
 			} else if (file.isDirectory() && !file.canExecute()) {
 				err = "Permission to enter directory denied";
 				status = ICrawlerDocument.Status.UNKNOWN_FAILURE;*/
 			} else if (omitHidden && file.isHidden()) {
 				err = "Hidden";
 				status = ICrawlerDocument.Status.UNKNOWN_FAILURE;
 			}
 			
 			cdoc.setStatus(status);
 			if (err != null) {
 				logger.warn(String.format("Error crawling %s: %s", location, err));
 				cdoc.setStatusText(err);
 				return cdoc;
 			}
 			
 			cdoc.setCrawlerDate(new Date());
 			cdoc.setLastModDate(new Date(file.lastModified()));
 			cdoc.setLocation(location);
 			
 			if (file.isDirectory()) {
 				final File[] list = file.listFiles();
 				final Iterator<DirlistEntry> dirlistIt = new DirlistIterator(list, omitHidden);
				try {
 					CrawlerTools.saveListing(cdoc, dirlistIt, inclParent, list.length > 0);
				} catch (IOException e) {
					final String msg = String.format("Error saving dir-listing for '%s': %s", location, e.getMessage());			
					logger.error(msg, e);
					cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
					return cdoc;
				}
 			} else {
 				final File contentFile = generateContentFile(readMode, file, cdoc);
 				cdoc.setContent(contentFile);
 			}
 		
		} catch (Exception e) {
			final String msg = String.format("Unexpected %s while crawling '%s'",
					e.getClass().getName(),
					location);			
 			logger.error(msg, e);
 			if (cdoc != null) {
 				cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
 			}
 		}		
 		
 		return cdoc;
 	}
 	
 	/**
 	 * A wrapper class around a {@link File} which implements the methods necessary for
 	 * the dirlist-generation.
 	 */
 	private static class DirlistEntryImpl implements DirlistEntry {
 		
 		File file;
 		
 		public URI getFileURI() { return null; }
 		public String getFileName() { return file.getName(); }
 		public long getLastModified() { return file.lastModified(); }
 		public long getSize() { return file.length(); }
 	};
 	
 	private static class DirlistIterator implements Iterator<DirlistEntry> {
 		
 		private final DirlistEntryImpl entry = new DirlistEntryImpl();
 		private final boolean omitHidden;
 		private final File[] list;
 		
 		private File next = null;
 		private int idx = -1;
 		
 		public DirlistIterator(final File[] list, final boolean omitHidden) {
 			this.list = list; 
 			this.omitHidden = omitHidden;
 			next = next0();
 		}
 		
 		private File next0() {
 			while (idx + 1 < list.length) {
 				idx++;
 				// check whether we are allowed to crawl this file
 				if (omitHidden && list[idx].isHidden())
 					continue;
 				return list[idx];
 			}
 			return null;
 		}
 		
 		public boolean hasNext() {
 			return next != null;
 		}
 		
 		public DirlistEntry next() {
 			entry.file = next;
 			next = next0();
 			return entry;
 		}
 		
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private File generateContentFile(final int readMode, final File file, final ICrawlerDocument cdoc) {
 		final File content;
 		
 		switch (readMode) {
 			case VAL_READ_MODE_DIRECT:
 				// TODO: prevent content from being deleted
 				content = file;
 				break;
 			
 			case VAL_READ_MODE_STD: {
 				logger.info(String.format("Copying '%s' using a standard copy mechanism", file));
 				FileInputStream fis = null;
 				try {
 					fis = new FileInputStream(file);
 					CrawlerTools.saveInto(cdoc, fis);
 					content = cdoc.getContent();
 				} catch (IOException e) {
 					logger.error(String.format("Error saving '%s': %s", cdoc.getLocation(), e.getMessage()), e);
 					cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
 					return null;
 				} finally {
 					if (fis != null) try { fis.close(); } catch (IOException e) {/* ignore */}
 				}
 			} break;
 			
 			case VAL_READ_MODE_CHANNELED:
 				content = copyChanneled(file, cdoc, false);
 				break;
 			case VAL_READ_MODE_CHANNELED_FSYNC:
 				content = copyChanneled(file, cdoc, true);
 				break;
 			
 			default:
 				throw new RuntimeException("switch statement does not cover read-mode: " + readMode);
 		}
 		
 		return content;
 	}
 	
 	private File copyChanneled(final File file, final ICrawlerDocument cdoc, final boolean useFsync) {
 		logger.info(String.format("Copying '%s' using the copy mechanism of the OS%s",
 				file,
 				(useFsync) ? " with fsync" : ""));
 		
 		final ITempFileManager tfm = CrawlerContext.getCurrentContext().getTempFileManager();
 		if (tfm == null) {
 			cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,
 					"Cannot access ITempFileMananger from " + Thread.currentThread().getName());
 			return null;
 		}
 		
 		FileInputStream fis = null;
 		FileOutputStream fos = null;
 		File out = null;
 		try {
 			out = tfm.createTempFile();
 			fis = new FileInputStream(file);
 			fos = new FileOutputStream(out);
 			
 			final FileChannel in_fc = fis.getChannel();
 			final FileChannel out_fc = fos.getChannel();
 			
 			long txed = 0L;
 			while (txed < in_fc.size())
 				txed += in_fc.transferTo(txed, in_fc.size() - txed, out_fc);
 			
 			if (useFsync)
 				out_fc.force(false);
 			out_fc.close();
 			
 			try {
 				detectFormats(cdoc, fis);
 			} catch (IOException ee) {
 				logger.warn(String.format("Error detecting format of '%s': %s", cdoc.getLocation(), ee.getMessage()));
 			}
 		} catch (IOException e) {
 			logger.error(String.format("Error copying '%s' to '%s': %s", cdoc.getLocation(), out, e.getMessage()), e);
 			cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
 		} finally {
 			if (fis != null) try { fis.close(); } catch (IOException e) {/* ignore */}
 			if (fos != null) try { fos.close(); } catch (IOException e) {/* ignore */}
 		}
 		return out;
 	}
 	
 	private void detectFormats(final ICrawlerDocument cdoc, InputStream is) throws IOException {
 		final ICrawlerContext ctx = CrawlerContext.getCurrentContext();
 		final ICharsetDetector chardet = ctx.getCharsetDetector();
 		final IMimeTypeDetector mimedet = ctx.getMimeTypeDetector();
 		if (chardet == null && mimedet == null)
 			return;
 		
 		ACharsetDetectorInputStream acis = null;
 		if (chardet != null)
 			is = acis = chardet.createInputStream(is);
 		
 		String mimeType = null;
 		String charset = null;
 		
 		int bufsize = 10240;					// needs to be big enough for the mime-type detector to detect it in one pass
 		if (cdoc.getSize() < bufsize)
 			bufsize = (int)cdoc.getSize();
 		final byte[] buf = new byte[bufsize];
 		
 		int read = 0;
 		boolean mimeTypeTested = false;
 		while ((read = is.read(buf)) != -1) {
 			if (mimedet != null && !mimeTypeTested) {
 				byte[] test_buf = buf;
 				if (read < bufsize) {
 					test_buf = new byte[read];
 					System.arraycopy(buf, 0, test_buf, 0, read);
 				}
 				
 				try {
 					mimeType = mimedet.getMimeType(test_buf, "FS-Crawler");
 				} catch (Exception e) {
 					logger.warn(String.format("Error detecting mime-type of '%s': %s", cdoc.getLocation(), e.getMessage()));
 				}
 				
 				mimeTypeTested = true;
 			}
 			
 			if (charset == null && chardet != null && acis.charsetDetected())
 				charset = acis.getCharset();
 			
 			if ((mimedet == null || mimeType != null) && (chardet == null || charset != null))
 				break;
 		}
 		cdoc.setCharset(charset);
 		cdoc.setMimeType(mimeType);
 	}
 }
