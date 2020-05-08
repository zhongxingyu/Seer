 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 package de.thischwa.c5c.requestcycle.impl;
 
 import javax.servlet.ServletContext;
 
 import de.thischwa.c5c.Constants;
 import de.thischwa.c5c.requestcycle.Context;
 import de.thischwa.c5c.requestcycle.BackendPathBuilder;
import de.thischwa.c5c.util.StringUtils;
 
 /**
  * An implementation of the {@link BackendPathBuilder} that returns the {@link ServletContext#getRealPath(String)}.
 * But it will be respected, if the servlet is running in a context other than the root context.
  */
 public class ServerPathBuilder implements BackendPathBuilder {
 
 	@Override
 	public String getBackendPath(String urlPath, Context context, ServletContext servletContext) {
		String requestedPath = urlPath;
		
		// check if the servlet runs in not in the root-context and adjust the url-path
		String ctxPath = servletContext.getContextPath();
		if(!StringUtils.isNullOrEmpty(ctxPath) && requestedPath.startsWith(ctxPath))
			requestedPath = requestedPath.substring(ctxPath.length(), requestedPath.length());
		
		String storagePath = servletContext.getRealPath(requestedPath);
		if(requestedPath.endsWith(Constants.defaultSeparator) && !storagePath.endsWith(Constants.defaultSeparator))
 			storagePath += Constants.defaultSeparator;
 		return storagePath;
 	}
 
 }
