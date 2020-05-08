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
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.requestcycle.impl;
 
 import javax.servlet.http.HttpServletRequest;
 
 import de.thischwa.c5c.UserObjectProxy;
 import de.thischwa.c5c.requestcycle.FilemanagerCapability;
 
 /**
 * The default {@link FilemanagerCapability} implementation. All capabilities
 * which set in the property <code>connector.capabilities</code> will be set for each files. 
  */
 public class DefaultCapability implements FilemanagerCapability {
 
 	@Override
 	public CAPABILITY[] getCapabilities(HttpServletRequest req, final String urlPath) {
 		return UserObjectProxy.getDefaultC5FileCapabilities();
 	}
 
 }
