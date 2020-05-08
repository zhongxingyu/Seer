 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util;
 
 import org.apache.commons.lang.SystemUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LibGdaCommon {
 	
 	private static final Logger logger = LoggerFactory.getLogger(LibGdaCommon.class);
 	
 	private static boolean LIBRARY_AVAILABLE = false;
 	
 	static {
 		final String libname = "gda_common";
 		try {
 			if (SystemUtils.IS_OS_LINUX) {
 				System.loadLibrary(libname);
 				LIBRARY_AVAILABLE = true;
 			}
 		} catch (Throwable e) {
 			logger.warn("Couldn't load " + libname + " library", e);
 		}
 	}
 	
 	public static String getFullNameOfUser(String username) {
 		if (LIBRARY_AVAILABLE) {
 			return _getFullNameOfUser(username);
 		}
		return null;
 	}
 	
 	private static native String _getFullNameOfUser(String username);
 	
 }
