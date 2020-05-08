 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
  
 package com.organic.maynard.outliner;
 
 import java.util.*;
 import javax.swing.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 public class PlatformCompatibility {
 
 	// Constants
 	public static final String LINE_END_MAC = "\r";
 	public static final String LINE_END_WIN = "\r\n";
 	public static final String LINE_END_UNIX = "\n";
 	public static final String LINE_END_DEFAULT = System.getProperty("line.separator");
 
 	public static final String PLATFORM_MAC = "Macintosh";
 	public static final String PLATFORM_WIN = "Windows";
 	public static final String PLATFORM_UNIX = "Unix";
 	
 	public static final String[] PLATFORM_IDENTIFIERS = {PLATFORM_MAC, PLATFORM_WIN, PLATFORM_UNIX};	
 
 	public static ArrayList JAVA_VERSION_ARRAY = new ArrayList();
 	
 	static {
 		String javaVersion = System.getProperty("java.version");
 
		StringTokenizer tokenizer = new StringTokenizer(javaVersion,"._");
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
			try {
				Integer value = new Integer(token);
				JAVA_VERSION_ARRAY.add(value);
			} catch (NumberFormatException e) {
				System.out.println("Java Version not a number: " + javaVersion);
			}
 		}
 	}
 
 	// Constructors
 	public PlatformCompatibility() {}
 
 
 	// Static Methods
 	public static String getScrollBarUIClassName() {
 		// Code switches on the platform and returns the appropriate
 		// platform specific classname or else the default.
 		if (isWindows()) {
 			return "com.organic.maynard.outliner.MetalScrollBarUI";
 		} else {
 			return "com.organic.maynard.outliner.BasicScrollBarUI";
 		}
 	}
 	
 	public static boolean areFilenamesEquivalent(String filename1, String filename2) {
 		if (isWindows()) {
 			// Windows is not case sensitive so we need to ignore case.
 			return filename1.equalsIgnoreCase(filename2); // Should be expanded to handle the ~ thing.
 		} else {
 			return filename1.equals(filename2);
 		}
 	}
 	
 	
 	// Utility Methods
 	public static boolean isWindows() {
 		String osName = System.getProperty("os.name");
 		if (osName.toLowerCase().startsWith("win")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean isMac() { // [deric] 31Sep2001  
 		String osName = System.getProperty("os.name");
 		if (osName.toLowerCase().startsWith("mac")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean isJava1_3_1() {
 		String javaVersion = System.getProperty("java.version");
 		if (javaVersion.startsWith("1.3.1")) {
 			return true;
 		} else {
 			return false;
 		}	
 	}
 
 	public static boolean isAtLeastJavaVersion(int major, int minor, int release) {
 		if (JAVA_VERSION_ARRAY.get(0) != null && major != -1) {
 			int currentMajor = ((Integer) JAVA_VERSION_ARRAY.get(0)).intValue();
 			
 			if (major > currentMajor) {
 				return false;
 			}
 		}
 
 		if (JAVA_VERSION_ARRAY.get(1) != null && minor != -1) {
 			int currentMinor = ((Integer) JAVA_VERSION_ARRAY.get(1)).intValue();
 
 			if (minor > currentMinor) {
 				return false;
 			}
 		}
 
 		if (JAVA_VERSION_ARRAY.get(2) != null && release != -1) {
 			int currentRelease = ((Integer) JAVA_VERSION_ARRAY.get(2)).intValue();
 
 			if (release > currentRelease) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	// Line Ending and Platform conversions
 	public static String platformToLineEnding(String platform) {
 		if (platform.equals(PLATFORM_MAC)) {
 			return LINE_END_MAC;
 		} else if (platform.equals(PLATFORM_WIN)) {
 			return LINE_END_WIN;
 		} else if (platform.equals(PLATFORM_UNIX)) {
 			return LINE_END_UNIX;
 		} else {
 			return LINE_END_DEFAULT;
 		}
 	}
 
 	public static String lineEndingToPlatform(String line_ending) {
 		if (line_ending.equals(LINE_END_MAC)) {
 			return PLATFORM_MAC;
 		} else if (line_ending.equals(LINE_END_WIN)) {
 			return PLATFORM_WIN;
 		} else if (line_ending.equals(LINE_END_UNIX)) {
 			return PLATFORM_UNIX;
 		} else {
 			System.out.println("Unknown line ending: " + line_ending);
 			return "UNKNOWN";
 		}
 	}
 }
