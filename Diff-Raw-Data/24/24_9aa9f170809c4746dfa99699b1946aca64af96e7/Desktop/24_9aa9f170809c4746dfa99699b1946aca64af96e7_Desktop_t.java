 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.desktop.backend.impl.jdic;
 
import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdesktop.jdic.desktop.DesktopException;
 import org.jdesktop.jdic.desktop.internal.BrowserService;
 import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
 import org.jdesktop.jdic.desktop.internal.ServiceManager;
 import org.jdesktop.jdic.desktop.internal.impl.DesktopConstants;
 import org.jdesktop.jdic.desktop.internal.impl.WinAPIWrapper;
 import org.jdesktop.jdic.desktop.internal.impl.WinUtility;
 import org.paxle.desktop.backend.desktop.IDesktop;
 
 public class Desktop implements IDesktop {
 	
 	private final Log logger = LogFactory.getLog(Desktop.class);
 	
 	public boolean browse(String url) throws MalformedURLException {
 		return browse(new URL(url));
 	}
 	
 	public boolean browse(URL url) {
 		try {
 			return browseImpl(url);
 		} catch (DesktopException e) {
 			if (logger.isDebugEnabled()) {
 				logger.error("Backend error starting browser", e);
 			} else {
 				logger.error(e.getMessage());
 			}
 			return false;
 		} catch (LinkageError e) {
 			logger.error("Linkage error starting browser: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	private boolean browseImpl(URL url) throws DesktopException {
 		if (System.getProperty("paxle.desktop.jdic.reflectBrowse", "false").equals("false")) {
 			org.jdesktop.jdic.desktop.Desktop.browse(url);
 			return true;
 		} else {		
 			final BrowserService browserService = (BrowserService)ServiceManager.getService(ServiceManager.BROWSER_SERVICE);
 			
 			try {
 				if (browserService.getClass().getName().equals("org.jdesktop.jdic.desktop.internal.impl.WinBrowserService")) {
 					boolean findOpenNew = false;
 					//First check if we could find command for verb opennew
 					String verbCmd = WinUtility.getVerbCommand(url, DesktopConstants.VERB_OPENNEW);
 					if (verbCmd != null) {
 						findOpenNew = true;
 					} else {
 						//If no opennew verb command find, then check open verb command
 						verbCmd = WinUtility.getVerbCommand(url, DesktopConstants.VERB_OPEN);
 					}
 	
 					if (verbCmd != null) {
 						
 						boolean result;
 						
 						try {
 							if (findOpenNew) {
 								//If there is opennew verb command, use this one
 								result = winShellExecute(url.toString(), DesktopConstants.VERB_OPENNEW);
 							} else {
 								//else use open verb command
 								result = winShellExecute(url.toString(), DesktopConstants.VERB_OPEN);
 							}
 						} catch (Exception e) {
 							final LaunchFailedException ex = new LaunchFailedException("Reflection error: " + e.getMessage());
 							ex.initCause(e);
 							throw ex;
 						}
 						
 						if (!result) {
 							throw new LaunchFailedException("Failed to launch the default browser");
 						}
 					} else {
 						throw new LaunchFailedException("No default browser associated with this URL");
 					}
 					
 					return false;
 				} else {
 					browserService.show(url);
 					return true;
 				}
 			} catch (LaunchFailedException e) {
 				final DesktopException ex = new DesktopException("Failed launching default browser");
 				ex.initCause(e);
 				throw ex;
 			}
 		}
 	}
 	
 	private boolean winShellExecute(final String filePath, final String verb) throws Exception {
		final Class<?> clz = WinAPIWrapper.class;
		Method stringToByteArray = null, shellExecute = null;
		for (final Method m : clz.getDeclaredMethods()) {
			if (m.getName().equals("stringToByteArray")) {
				stringToByteArray = m;
				continue;
			} else if (m.getName().equals("shellExecute")) {
				shellExecute = m;
				continue;
			}
		}
		stringToByteArray.setAccessible(true);
		shellExecute.setAccessible(true);
		
		byte[] filePathBytes = (byte[])stringToByteArray.invoke(null, filePath);
		byte[] verbBytes = (byte[])stringToByteArray.invoke(null, verb);
		final Integer result = (Integer)shellExecute.invoke(null, filePathBytes, verbBytes);
		logger.debug("ShellExecute(NULL, \"" + verb + "\", \"" + filePath + "\", NULL, NULL, SW_SHOWNORMAL); returned " + result);
 		return (result != null && result.intValue() > 32);
 	}
 }
