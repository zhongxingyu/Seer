 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU Lesser General Public License Version 3.0 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl-3.0.html
  * 
  *  - Mozilla Public License Version 2.0 or later (the "MPL")
  *    http://www.mozilla.org/MPL/2.0/
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.requestcycle.impl;
 
 import javax.servlet.ServletContext;
 
 import de.thischwa.c5c.requestcycle.Context;
 import de.thischwa.c5c.requestcycle.UserPathBuilder;
 
 /**
 * Implementation of the {@link UserPathBuilder} that returns the {@link ServletContext#getRealPath(String)}.
  */
 public class ServerPathBuilder implements UserPathBuilder {
 
 	@Override
 	public String getServerPath(String path, Context context, ServletContext servletContext) {
 		return servletContext.getRealPath(path);
 	}
 
 }
