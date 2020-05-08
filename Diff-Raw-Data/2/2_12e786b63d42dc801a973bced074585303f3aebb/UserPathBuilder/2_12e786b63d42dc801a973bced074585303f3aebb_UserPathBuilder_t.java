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
 package de.thischwa.c5c.requestcycle;
 
 import javax.servlet.ServletContext;
 
 import de.thischwa.c5c.Connector;
 
 
 /**
  * Interface for building the path to <i>userfiles</i>. <br/> 
  * <br/>
  * <b>Hint:</b> You are free to implement this interface they way you need it. The return value 
  * can be global (regardless of a request) or on a per-request basis.  
  */
 public interface UserPathBuilder {
 
 	/**
 	 * Returns the server-side <i>userfiles</i> absolute path. The provided implementation of {@link Connector}
 	 * will use this value to resolve the server-side location of resources. <br/>
 	 * <br/> 
	 * <b>Hint:</b> E.g. {@link Context} and/or {@link ServletContext} can be used to to implement a
 	 * filesystem storage for each user.  
 	 * 
 	 * @param urlPath
 	 * @param context
 	 * @param servletContext
 	 * @return the constructed server-side path
 	 */
 	public String getServerPath(String urlPath, Context context, ServletContext servletContext);
 	
 }
