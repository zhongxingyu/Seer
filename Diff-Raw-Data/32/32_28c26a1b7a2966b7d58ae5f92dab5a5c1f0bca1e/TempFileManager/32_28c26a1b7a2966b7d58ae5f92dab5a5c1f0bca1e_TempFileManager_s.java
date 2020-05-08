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
 package org.paxle.core.io.temp.impl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.StatusVariable;
 import org.paxle.core.io.temp.ITempDir;
 import org.paxle.core.io.temp.ITempFileManager;
 
 public class TempFileManager implements ITempFileManager, Monitorable {
 	private final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/TempFileManager");
 	
 	/* =========================================================
 	 * OSGi Monitorable CONSTANTS
 	 * ========================================================= */		
 	public static final String MONITOR_PID = "org.paxle.tempFileManager";
 	static final String MONITOR_FILES_USED = "files.used";
 	static final String MONITOR_FILES_TOTAL = "files.total";
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	private AtomicInteger totalCount = new AtomicInteger(0);
 	
 	/**
 	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
 	 */
 	@SuppressWarnings("serial")
 	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
 			add(MONITOR_FILES_USED);
 			add(MONITOR_FILES_TOTAL);
 	}};	
 	
 	private final Hashtable<String,ITempDir> classMap = new Hashtable<String,ITempDir>();
 	
 	/**
 	 * A map containing all temp-files managed by this temp-file-manager
 	 */
 	private final Hashtable<File,ITempDir> fileMap = new Hashtable<File,ITempDir>();
 	
 	private final ITempDir defaultDir;
	private final boolean deleteOnExit;
 	
	public TempFileManager(ITempDir defaultDir, final boolean deleteOnExit) {
 		this.defaultDir = defaultDir;
		this.deleteOnExit = deleteOnExit;
	}
	
	public TempFileManager(final boolean deleteOnExit) {
		this(new DefaultTempDir(), deleteOnExit);
 	}
 	
 	public TempFileManager() {
		this(true);
 	}
 	
 	public void removeTempDirFor(String... classNames) {
 		for (final String className : classNames)
 			this.classMap.remove(className);
 	}
 	
 	public void setTempDirFor(ITempDir dir, String... classNames) {
 		for (final String className : classNames)
 			this.classMap.put(className, dir);
 	}
 	
 	public File createTempFile() throws IOException {
 		final String className = new Exception().getStackTrace()[1].getClassName();		// FIXME: slow!
 		ITempDir dir = this.classMap.get(className);
 		if (dir == null) dir = defaultDir;
 		
 		// creating a new temp file
 		final File ret = dir.createTempFile(className, ".tmp");
 		
 		// remember the created temp file for later cleanup
 		this.fileMap.put(ret, dir);				
 		this.totalCount.incrementAndGet();
 		
		if (deleteOnExit) {
			ret.deleteOnExit();
		}
		
 		return ret;
 	}
 	
 	public boolean isKnown(File file) {
 		if (file == null) return false;
 		return this.fileMap.containsKey(file);
 	}
 	
 	public void releaseTempFile(File file) throws FileNotFoundException, IOException {
 		if (!this.isKnown(file)) return;
 		
 		// getting the temp-dir where the temp-file is stored in
 		final ITempDir dir = this.fileMap.remove(file);
 		
 		// testing if the file was already deleted
 		if (!file.exists()) {
 			this.logger.debug(String.format(
 					"Unable to release tempfile '%s'. File does not exist.",
 					file
 			));
 			return;
 		}
 		
 		// trying to delete the temp-file
 		boolean success = ((dir == null) ? defaultDir : dir).releaseTempFile(file);		
 		if (!success) {
 			this.logger.warn(String.format(
 					"Unable to release tempfile '%s'.",
 					file
 			));
 		}
 	}
 
 	public String getDescription(String id) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(id)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + id);
 		}		
 		
 		return this.rb.getString(id);
 	}
 
 	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(id)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + id);
 		}
 		
 		int val = 0;
 		int type = StatusVariable.CM_GAUGE;
 		
 		if (id.equals(MONITOR_FILES_TOTAL)) {
 			val = this.totalCount.get();
 			type = StatusVariable.CM_CC;
 		} else if (id.equals(MONITOR_FILES_USED)) {
 			val = this.fileMap.size();
 		}
 		
 		return new StatusVariable(id, type, val);
 	}
 
 	public String[] getStatusVariableNames() {
 		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
 	}
 
 	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
 		return false;
 	}
 
 	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
 		if (id.equals(MONITOR_FILES_TOTAL)) {
 			this.totalCount.set(0);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Method just used for testing
 	 * @return
 	 */
 	Map<File,ITempDir> getFileMap() {
 		return Collections.unmodifiableMap(fileMap);
 	}
 }
