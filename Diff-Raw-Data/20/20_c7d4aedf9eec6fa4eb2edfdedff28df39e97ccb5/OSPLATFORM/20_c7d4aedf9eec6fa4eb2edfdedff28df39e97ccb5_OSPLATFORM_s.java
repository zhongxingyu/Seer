 /*******************************************************************************
  * Copyright (c) 2010 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Jacques Bouthillier - Initial Implementation to define the Operating system 
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.report.internal.util;
 
 import java.io.File;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.reviews.r4e.core.utils.sys.OSPLATFORM.OSTYPE;
 
 
 public final class OSPLATFORM {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	// Separator among files
 	private static final String	FILE_PATH_SEPARATOR					= File.pathSeparator;
 
 	private static final char	FILE_PATH_SEPARATOR_CHAR			= File.pathSeparatorChar;
 
 	// Separator between file elements (directory and file names)
 	private static final String	FILE_ELEMENT_SEPARATOR				= File.separator;
 
 	private static final char	FILE_ELEMENT_SEPARATOR_CHAR			= File.separatorChar;
 
 	public static final String		FFILE_ELEMENT_SEPARATOR_PORTABLE		= "/";
 
 	private static final boolean	IS_FILE_ELEMENT_SEPARATOR_PORTABLE	= FFILE_ELEMENT_SEPARATOR_PORTABLE
 																			.equals(FILE_ELEMENT_SEPARATOR);
 
 	// ------------------------------------------------------------------------
 	// Variables
 	// ------------------------------------------------------------------------
 	public static final String		FNAME								= System.getProperty("os.name");
 
 	public static final OSTYPE		FTYPE								= OSTYPE.resolveOSNameToOSType(FNAME);
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 	private OSPLATFORM() {
	};
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	public enum OSTYPE {
 		WINDOWS {
 			protected OSTYPE match(String name) {
 				OSTYPE type = null;
 				// String OSREGEX = new String("(.*(?i)Windows.*)");
 				// if (name.matches(OSREGEX)) {
 				// type = WINDOWS;
 				// }
 				if (Platform.getOS().matches(Platform.OS_WIN32)) {
 					type = WINDOWS;
 				}
 				return type;
 			}
 		}, //
 		LINUX {
 			protected OSTYPE match(String name) {
 				OSTYPE type = null;
 				// String OSREGEX = new String("(.*(?i)Linux.*)");
 				// if (name.matches(OSREGEX)) {
 				// type = LINUX;
 				// }
 				if (Platform.getOS().matches(Platform.OS_LINUX)) {
 					type = LINUX;
 				}
 				return type;
 			}
 		}, // 
 		SOLARIS {
 			protected OSTYPE match(String name) {
 				OSTYPE type = null;
 				// String OSREGEX = new String("(.*(?i)Solaris.*)");
 				// String OSREGEX = new String("(.*(?i)SunOS.*)");
 				// if (name.matches(OSREGEX)) {
 				// type = SOLARIS;
 				// }
 				if (Platform.getOS().matches(Platform.OS_SOLARIS)) {
 					type = SOLARIS;
 				}
 				return type;
 			}
 		},
 		MAC {
 			@Override
 			protected OSTYPE match(String name) {
 				OSTYPE type = null;
 
 				if (Platform.getOS().matches(Platform.OS_MACOSX)) {
 					type = MAC;
 				}
 				return type;
 			}
 		};
 		
 		protected abstract OSTYPE match(String name);
 
 		// Internal Helper
 		protected static OSTYPE resolveOSNameToOSType(String aName) {
 			// System.out.println("***** os.name: " +
 			// System.getProperty("os.name"));
 			// System.out.println("Platform OS: " +
 			// Platform.getOS() +
 			// "\t arch: " +
 			// Platform.getOSArch() +
 			// "\t Workstation: " +
 			// Platform.getWS());
 			OSTYPE type = null;
 			for (OSTYPE t : OSTYPE.values()) {
 				type = t.match(aName);
 				if (type != null)
 					break;
 			}
 			return type;
 		}
 
 		//
 		// Publish methods
 		//
 
 		public boolean isWindowsOS() {
 			return this == WINDOWS;
 		}
 
 		public boolean isLinuxOS() {
 			return this == LINUX;
 		}
 
 		public boolean isSolarisOS() {
 			return this == SOLARIS;
 		}
 		
 		public boolean isMacOS() {
 			return this == MAC;
 		}
	}; // enum
 
 
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		for (Entry<Object, Object> pair : System.getProperties().entrySet()) {
 			System.out.println(pair.getKey() + " [" + pair.getValue() + "]");
 		}
 
 		//
 
 		System.out.println("main() OS Name [" + OSPLATFORM.FNAME + "]");
 		System.out.println("main() OS Type [" + OSPLATFORM.FTYPE + "]");
 
 		System.out.println("main() Windows/Linux/Solaris "
  + OSPLATFORM.FTYPE.isWindowsOS() + "/"
 				+ OSPLATFORM.FTYPE.isLinuxOS() + "/" + OSPLATFORM.FTYPE.isSolarisOS());
 
 		//
 
 		System.out.println("main() File Path separator ["
 				+ OSPLATFORM.FILE_PATH_SEPARATOR + "] char ["
 				+ OSPLATFORM.FILE_PATH_SEPARATOR_CHAR + "]");
 		System.out.println("main() File Element separator ["
 				+ OSPLATFORM.FILE_ELEMENT_SEPARATOR + "] char ["
 				+ OSPLATFORM.FILE_ELEMENT_SEPARATOR_CHAR + "] Portable "
 				+ OSPLATFORM.IS_FILE_ELEMENT_SEPARATOR_PORTABLE);
 
 	}
 
 }
