 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.coderesolution.reflection.java.plugins.codelocators;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.wazaabi.coderesolution.reflection.java.plugins.Activator;
 import org.eclipse.wazaabi.coderesolution.reflection.java.plugins.codedescriptors.PluginCodeDescriptor;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeDescriptor;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeLocator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 
 public class PlatformPluginCodeLocator extends AbstractCodeLocator {
 
 	static private final String URI_PREFIX = "platform:/plugin/"; //$NON-NLS-1$ 
 	static private final String LANGUAGE = "java"; //$NON-NLS-1$
 	static private final String LANGUAGE_PART = "?language="; //$NON-NLS-1$
 	static private final int URI_PREFIX_LENGTH = URI_PREFIX.length();
 	static private final int LANGUAGE_PART_LENGTH = LANGUAGE_PART.length();
 
 	@Override
 	public AbstractCodeDescriptor resolveCodeDescriptor(String uri) {
 		String path = uri.substring(URI_PREFIX_LENGTH);
 		int idx = path.indexOf("/"); //$NON-NLS-1$
 		if (idx != -1) {
 			String bundleName = path.substring(0, idx);
 			if (!"".equals(bundleName)) { //$NON-NLS-1$
 				path = path.substring(idx + 1);
 				idx = path.lastIndexOf("?"); //$NON-NLS-1$
 				if (idx != -1)
 					path = path.substring(0, idx);
 				if (!"".equals(path)) //$NON-NLS-1$
 					return new PluginCodeDescriptor(bundleName, path);
 			}
 		}
 		return null;
 	}
 
 	public InputStream getResourceInputStream(String uri) throws IOException {
 		if (Activator.getDefault() != null) {
 			final String str = uri.substring(URI_PREFIX_LENGTH);
 			String bundleName = str.substring(0, str.indexOf("/")); //$NON-NLS-1$
 			if ("".equals(bundleName)) //$NON-NLS-1$
 				return null;
 			final String path = str.substring(bundleName.length());
 			Bundle bundle = Activator.getDefault().getBundleForName(bundleName);
 			if (bundle != null) {
 				URL url = bundle.getResource(path);
 				if (url != null) {
 					return url.openStream();
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isCodeLocatorFor(String uri) {
 		if (uri != null && uri.startsWith(URI_PREFIX)) {
 			int idx = uri.lastIndexOf(LANGUAGE_PART);
 			if (idx == -1)
 				return true; // java is the default language (when nothing is
 								// specified)
 			String language = uri.substring(idx + LANGUAGE_PART_LENGTH);
 			if (LANGUAGE.equals(language)) //$NON-NLS-1$
 				return true;
 		}
 		return false;
 
 	}
 
 	public String getFullPath(String prefix, String relativePath, Object context) {
 		if (prefix == null || "".equals(prefix) && relativePath != null //$NON-NLS-1$
				&& relativePath.startsWith(URI_PREFIX))
 			return relativePath;
 		if (URI_PREFIX.equals(prefix) && context != null) {
 			Bundle bundle = FrameworkUtil.getBundle(context.getClass());
 			if (bundle != null)
 				return URI_PREFIX + bundle.getSymbolicName() + '/'
 						+ relativePath;
 		} else if (prefix != null && prefix.startsWith(URI_PREFIX)) {
 			if (prefix.endsWith("/"))
 				return prefix + relativePath;
 			else
 				return prefix + '/' + relativePath;
 		}
 		return null;
 	}
 }
