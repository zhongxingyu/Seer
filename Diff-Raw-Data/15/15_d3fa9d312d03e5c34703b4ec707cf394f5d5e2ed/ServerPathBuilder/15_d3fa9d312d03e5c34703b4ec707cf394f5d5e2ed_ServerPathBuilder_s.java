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
 
 /**
  * An implementation of the {@link BackendPathBuilder} that returns the {@link ServletContext#getRealPath(String)}.
  */
 public class ServerPathBuilder implements BackendPathBuilder {
 
 	@Override
 	public String getBackendPath(String urlPath, Context context, ServletContext servletContext) {
		String storagePath = servletContext.getRealPath(urlPath);
		if(urlPath.endsWith(Constants.defaultSeparator) && !storagePath.endsWith(Constants.defaultSeparator))
 			storagePath += Constants.defaultSeparator;
 		return storagePath;
 	}
 
 }
