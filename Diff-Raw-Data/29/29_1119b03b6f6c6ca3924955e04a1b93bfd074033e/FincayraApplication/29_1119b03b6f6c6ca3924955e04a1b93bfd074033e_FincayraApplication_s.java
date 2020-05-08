 package org.innobuilt.fincayra;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileSystemManager;
 import org.apache.commons.vfs.VFS;
 import org.apache.commons.vfs.impl.DefaultFileMonitor;
 import org.innobuilt.fincayra.mail.MailManager;
 import org.innobuilt.fincayra.persistence.PersistenceManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 /*   Copyright 2010 Jesse Piascik
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 public class FincayraApplication {
 	private static Logger LOGGER = LoggerFactory.getLogger(FincayraApplication.class);
 	private String rootDir = null;
 	private String pageDir = null;
 	private String jsDir = null;
 	private String bundleDir = null;
 	private MergeEngine mergeEngine = new MergeEngine();
 	private MailManager mailManager = null;
	private PersistenceManager persistenceManager = null;
 	private String url = "http://localhost:8080/fincayra/";
 	private String secureUrl;
 	private String name;
 	private boolean reloadRootScope = false;
 	private List<String> hiddenPaths = new ArrayList<String>();
 	private List<String> exposedPaths = new ArrayList<String>();
 	
 	public void watch(String fileName) {
 		if (this.getReloadRootScope()) {
 	
 			LOGGER.info("Adding file to root scope watch: {}", fileName); 
 			try {
 				FileSystemManager fsManager = VFS.getManager();
 				 FileObject listendir = fsManager.resolveFile(fileName);
 				 
 	
 				 DefaultFileMonitor fm = new DefaultFileMonitor(new FincayraFileListener());
 				 fm.setRecursive(true);
 				 fm.addFile(listendir);
 				 fm.start();
 			} catch (FileSystemException e) {
 				LOGGER.error("Unable to watch directory: {}", fileName);
 			}
 		}
 	}
 
 	public String getPageDir() {
 		return pageDir;
 	}
 
 	public void setPageDir(String pageDir) {
 		this.pageDir = pageDir;
 		this.hidePath(pageDir);
 	}
 
 	//Spring stuff
 	private static FincayraApplication application = null;
 
 	
 	public static final FincayraApplication get() {
 		if (application == null) {
 			application = new FincayraApplication(); 
 		}
 		
 		return application;
 	}
 
 	public void setMailManager(MailManager mailManager) {
 		this.mailManager = mailManager;
 		hidePath(mailManager.getTemplateDir());
 	}
 
 
 	public MailManager getMailManager() {
 		return mailManager;
 	}
 
 	public void setMergeEngine(MergeEngine mergeEngine) {
 		this.mergeEngine = mergeEngine;
 	}
 
 
 	public MergeEngine getMergeEngine() {
 		return mergeEngine;
 	}
 
 
 	public void setRootDir(String rootDir) {
 		LOGGER.info("rootDir=" + rootDir);
 		this.rootDir = rootDir;
 	}
 
 
 	public String getRootDir() {
 		return rootDir;
 	}
 
 
 	public void setJsDir(String jsDir) {
 		this.jsDir = jsDir;
 		hidePath(jsDir);
 	}
 
 
 	public String getJsDir() {
 		return jsDir;
 	}
 
 
 	public void setBundleDir(String bundleDir) {
 		this.bundleDir = bundleDir;
 		hidePath(bundleDir);
 	}
 
 
 	public String getBundleDir() {
 		return bundleDir;
 	}
 
 	public void setPersistenceManager(PersistenceManager persistenceManager) {
 		this.persistenceManager = persistenceManager;
 	}
 
 	public PersistenceManager getPersistenceManager() {
 		return persistenceManager;
 	}
 	
 	public void hidePath(String path) {
 		if(!hiddenPaths.contains(path))hiddenPaths.add(path);
 	}
 	
 	public List<String> getHiddenPaths() {
 		return hiddenPaths;
 	}
 
 	public void exposePath(String path) {
 		if(!exposedPaths.contains(path))exposedPaths.add(path);
 	}
 	
 	public List<String> getExposedPaths() {
 		return exposedPaths;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void setSecureUrl(String secureUrl) {
 		this.secureUrl = secureUrl;
 	}
 
 	public String getSecureUrl() {
 		return secureUrl;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setReloadRootScope(boolean reloadRootScope) {
 		this.reloadRootScope = reloadRootScope;
 	}
 
 	public boolean getReloadRootScope() {
 		return reloadRootScope;
 	}
 
 	
 
 }
