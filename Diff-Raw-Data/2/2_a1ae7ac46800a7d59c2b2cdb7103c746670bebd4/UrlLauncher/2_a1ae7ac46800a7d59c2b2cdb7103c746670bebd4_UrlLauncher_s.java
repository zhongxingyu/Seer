 /*
  * JYald
  * 
  * Copyright (C) 2011 Oguz Kartal
  * 
  * This file is part of JYald
  * 
  * JYald is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JYald is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JYald.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.jyald.util;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 
 public class UrlLauncher {
 	
 	private static Process run(String...args) {
 		
 		try {
 			return Runtime.getRuntime().exec(args);
 		}
 		catch (IOException e) {}
 		
 		return null;
 	}
 	
 	private static void goDefault(String url) {
 		run("open",url);
 	}
 	
 	private static void goForMacOsX(String url) {
 		try {
 			Class<?> fileMgrClass = Class.forName("com.apple.eio.FileManager");
 			Method openMeth = fileMgrClass.getDeclaredMethod("openURL", 
 					new Class[]{String.class});
 			
 			openMeth.invoke(null, new Object[]  {url});
 			
 		} catch (Exception e) {
 			goDefault(url);
 		}	
 	}
 	
 	private static void goForLinux(String url) {
 		String[] browsers = {
			"firefox", "chromium", "mozilla",
 			"netscape", "konqueror", "opera"
 		};
 		
 		String sysBrowser = null;
 		
 		for (int i=0;i<browsers.length;i++) {
 			try {
 				if (run("which",browsers[i]).waitFor()==0) {
 					sysBrowser = browsers[i];
 					break;
 				}
 			} catch (Exception e) {}
 		}
 		
 		if (sysBrowser == null)
 			run("./xdg-open",url);
 		else
 			run(sysBrowser,url);
 	}
 	
 	private static void goForWindows(String url) {
 		run("cmd.exe","/C","start",url);
 	}
 	
 	public static void go(String url) {
 		String os = System.getProperty("os.name").toLowerCase();
 		
 		if (os.startsWith("win"))
 			goForWindows(url);
 		else if (os.startsWith("mac os x"))
 			goForMacOsX(url);
 		else if (os.startsWith("linux"))
 			goForLinux(url);
 		else
 			goDefault(url);
 	}
 }
