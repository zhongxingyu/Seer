 // PlistReader.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.util.Enumeration;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import com.webobjects.appserver.WOApplication;
 import com.webobjects.foundation.*;
 
 public class PlistReader extends SettingsReader implements Cloneable {
 	
 	public static final String LinkKey = "thisIsPlistLink";
 	
 	protected NSDictionary pref;
 	protected File inputFile;
 	protected String encoding;
 	protected StringBuffer innerKeyPath;
 
 	protected NSDictionary rootDict;
 
 	protected int state = -1;
 	/* -1 uninitialized
 	 *  0 unchanged
 	 *  1 modified
 	 *  2 combined with other */
 
 	public int state() {
 		return state;
 	}
 	
 	public PlistReader() {
 		String inputFilePath = System.getProperty("PlistReader.filePath",null);
 		if(inputFilePath == null) {
 			System.out.println("Using default settings");
 			inputFilePath = WOApplication.application().resourceManager().
 				pathURLForResourceNamed("defaultSettings.plist", null, null).getFile();
 			if(inputFilePath == null)
 				throw new IllegalStateException(
 						"Required environment variable \"PlistReader.filePath\"");
 		}
 		inputFilePath = Various.convertFilePath(inputFilePath);
 		inputFile = new File(inputFilePath);
 		encoding = System.getProperty("PlistReader.encoding","utf8");
 	 	innerKeyPath = new StringBuffer(System.getProperty("PlistReader.innerKeyPath",""));
 	 	refresh();
 	}
 	
 	public static PlistReader readerForFile(String inputFilePath, String encoding) {
 		PlistReader result = new PlistReader(new File(inputFilePath),encoding,null);
 		result.refresh();
 		return result;
 	}
 	
 	protected PlistReader(File file, String encoding, CharSequence innerKeyPath) {
 		inputFile = file;
 		this.encoding = encoding;
 		this.innerKeyPath = (innerKeyPath==null)?new StringBuffer():
 			new StringBuffer(innerKeyPath);
 		//refresh();
 	}
 	
 	public PlistReader(NSDictionary dict) {
 		innerKeyPath = new StringBuffer();
 		rootDict = dict;
 		pref = rootDict;
 		state = 3;
 	}
 
 	public void refresh() {
 		if(inputFile == null){
 			System.err.println("No input filePath specified for reading preferences");
 			return;
 		}
 		if(!inputFile.canRead()) {
 			System.err.println("Specified file does not exisi or can't be read");
 			return;
 		}
 		try {
 			rootDict = (NSDictionary)readPlist(new FileInputStream(inputFile), encoding);
 		} catch (FileNotFoundException e) {
 			throw new NSForwardException(e,"Could not read plist at specified path: " 
 					+ inputFile.getAbsolutePath());
 		}
 		if(rootDict == null)
 			throw new NullPointerException("Could not read plist at specified path: " 
 					+ inputFile);
 		System.out.println("Settings are read from " + inputFile.getAbsolutePath());
 		if(innerKeyPath != null && innerKeyPath.length() > 0) {
 			pref = (NSDictionary)rootDict.valueForKeyPath(innerKeyPath.toString());
 			System.out.println("with inner keyPath " + innerKeyPath.toString());
 		} else
 			pref = rootDict;
 		state = 0;
 	}
 	
 	public static Object readPlist(String filename, String framework, String encoding) {
 		InputStream is = WOApplication.application().resourceManager().
 					inputStreamForResourceNamed(filename, framework, null);
 		return readPlist(is, encoding);
 	}
 	
 	public static Object readPlist(InputStream stream, String encoding) {
 		if(encoding == null)
 			encoding = System.getProperty("PlistReader.encoding","utf8");
 		try {
 			if(stream == null)
 				return null;
 			NSData data = new NSData(stream,stream.available());
 			return NSPropertyListSerialization.propertyListFromData(data,encoding);
 		} catch (Exception e) {
 			System.err.println("Error reading plist " + stream
 					+ " using encoding " + encoding);
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 	
 	public static Object readPlist(String filePath, String encoding) {
 		filePath = Various.convertFilePath(filePath);
 		try {
 			FileInputStream fis = new FileInputStream(filePath);
 			return readPlist(fis, encoding);
		} catch (java.io.FileNotFoundException e) {
			return null;
		} catch (Exception ioex) {
 			System.err.println("Error reading plist " + filePath + 
 					" using encoding " + encoding);
 			ioex.printStackTrace(System.err);
 			return null;
 		}
 	}
 	
 	protected static boolean dictIslink(Object dict) {
 		if(!(dict instanceof NSDictionary))
 			return false;
 		Object isLink = ((NSDictionary)dict).valueForKey(LinkKey);
 		if(isLink == null)
 			return false;
 		if(isLink instanceof String)
 			return NSPropertyListSerialization.booleanForString((String)isLink);
 		else if(isLink instanceof Boolean)
 			return ((Boolean)isLink).booleanValue();
 		return false;
 	}
 	
 	protected File resolveFile(String filePath) {
 		File nextFile = null;
 		if(filePath.charAt(0) == '.') {
 			int pos =1;
 			nextFile = inputFile.getParentFile();
 			while(filePath.charAt(pos) == '.') {
 				nextFile = nextFile.getParentFile();
 				pos++;
 			}
 			filePath = filePath.substring(pos+1);
 			while(filePath.startsWith("../")) {
 				nextFile = nextFile.getParentFile();
 				filePath = filePath.substring(3);
 			}
 			filePath = filePath.replace('/', File.separatorChar);
 			nextFile = new File(nextFile,filePath);
 		} else {
 			nextFile = new File(Various.convertFilePath(filePath));
 		}
 		return nextFile;
 	}
 	
 	protected Object resolveLink(NSDictionary linkDict, PlistReader updateReader) {
 		if(dictIslink(linkDict)) {
 			NSDictionary newRootDict = null;
 			String keyPath = (String)linkDict.valueForKey("innerKeyPath");
 			if(updateReader != null) {
 				updateReader.innerKeyPath = (keyPath==null)?new StringBuffer():
 					new StringBuffer(keyPath);
 			}
 			String filePath = (String)linkDict.valueForKey("inputFilePath");
 			if(filePath != null) {
 				String newEncoding = (String)linkDict.valueForKey("encoding");
 				if(newEncoding == null)
 					newEncoding = encoding;
 				File nextFile = resolveFile(filePath);
 				try {
 					newRootDict = (NSDictionary)readPlist(new FileInputStream(nextFile)
 							, newEncoding);
 				} catch (FileNotFoundException e) {
 					throw new NSForwardException(e);
 				}
 				if(updateReader != null) {
 					updateReader.rootDict = newRootDict;
 					updateReader.encoding = newEncoding;
 					updateReader.inputFile = nextFile;
 					updateReader.pref = newRootDict;
 					updateReader.state = 0;
 				}
 			} else {
 				newRootDict = rootDict;
 			}
 			if(keyPath != null) {
 				Object result = newRootDict.valueForKeyPath(keyPath);
 				if(updateReader != null) {
 					updateReader.pref = (result instanceof NSDictionary)?
 							(NSDictionary)result:null;
 				}
 				if(result instanceof NSDictionary)
 					newRootDict = (NSDictionary)result;
 				else
 					return result;
 			}
 			return cloneDictionary(newRootDict,true);
 		} else {
 			return linkDict;
 		}
 	}
 	
 	protected static StringBuffer appendKey(StringBuffer buf, CharSequence key) {
 		if(buf.length() > 0)
 			buf.append('.');
 		return buf.append(key);
 	}
 	
 	protected NSMutableDictionary changeState(String[] path, int i) {
 		state = 1;
 		NSMutableDictionary dict = cloneDictionary(pref, true);
 		pref = dict; 
 		for (int j = 0; j < i; j++) {
 			dict = (NSMutableDictionary)dict.valueForKey(path[j]);
 		}
 		return dict;
 	}
 	
 	public static NSMutableDictionary cloneDictionary (NSDictionary dict, boolean recurse) {
 		if(dict == null) return null;
 		NSMutableDictionary result = new NSMutableDictionary();
 		Enumeration enu = dict.keyEnumerator();
 		while (enu.hasMoreElements()) {
 			Object key = enu.nextElement();
 			Object obj = dict.objectForKey(key);
 			if(obj instanceof NSDictionary) {
 				if(recurse)
 				result.setObjectForKey(cloneDictionary((NSDictionary)obj, recurse), key);
 			} else if (obj instanceof NSArray) {
 				if(recurse)
 					result.setObjectForKey(cloneArray((NSArray)obj, recurse), key);
 			} else {
 				result.setObjectForKey(obj, key);
 			}
 		}
 		return result;
 	}
 	
 	public static NSMutableArray cloneArray(NSArray array, boolean recurse) {
 		if(array ==null) return null;
 		NSMutableArray result = new NSMutableArray();
 		Enumeration enu = array.objectEnumerator();
 		while (enu.hasMoreElements()) {
 			Object obj = enu.nextElement();
 			if(obj instanceof NSDictionary) {
 				if(recurse)
 					result.addObject(cloneDictionary((NSDictionary)obj,recurse));
 			} else if (obj instanceof NSArray) {
 				if(recurse)
 					result.addObject(cloneArray((NSArray)obj,recurse));
 			} else {
 				result.addObject(obj);
 			}
 		}
 		return result;
 	}
 	
 	public NSDictionary shallowDictionary() {
 		return cloneDictionary(pref, false);
 	}
 
 	public NSDictionary fullDictionary() {
 		return cloneDictionary(pref, true);
 	}
 
 	public Enumeration keyEnumerator() {
 		return pref.keyEnumerator();
 	}
 
 	public PlistReader subreaderForPath(String path, boolean createIfNeeded) {
 		PlistReader result = (PlistReader)traverseKeyPath(path, createIfNeeded, true);
 			/*new PlistReader(inputFile,encoding,innerKeyPath);
 		result.rootDict = rootDict;
 		Object target = traverseKeyPath(path, createIfNeeded, result);
 		if(!(target instanceof NSDictionary)) {
 			return null;
 		}*/
 		return result;
 	}
 	
 	public void save() throws Exception {
 		super.save();
 		// TODO implement save method
 	}
 
 //	protected NSMutableSet toSave;
 	public void takeValueForKeyPath(Object value, String keyPath) {
 		int lastDot = keyPath.lastIndexOf('.');
 		if(lastDot < 0) {
 			takeValueForKey(value, keyPath);
 		} else {
 			if(state < 1) {
 				pref = cloneDictionary(pref, true);
 				state = 1;
 			}
 			NSMutableDictionary end = (NSMutableDictionary)traverseKeyPath(
 					keyPath.substring(0,lastDot), true, false);
 			end.takeValueForKey(value, keyPath.substring(lastDot + 1));
 			/*
 			SettingsReader next = subreaderForPath(keyPath.substring(0, lastDot), true);
 			next.takeValueForKey(value, keyPath.substring(lastDot + 1));
 			if(next instanceof PlistReader) {
 				File nextFile = ((PlistReader)next).inputFile;
 				if(!(inputFile == nextFile)) {
 					if(toSave == null) {
 						toSave = new NSMutableSet(nextFile);
 					} else {
 						toSave.addObject(nextFile);
 					}
 
 				}
 			} else {
 				if(toSave == null) {
 					toSave = new NSMutableSet(next);
 				} else {
 					toSave.addObject(next);
 				}
 			} */
 		}
 	}
 
 	public Object valueForKeyPath(String keyPath) {
 		Object result = traverseKeyPath(keyPath, false, false);
 		if(result instanceof NSDictionary)
 			return new PlistReader((NSDictionary)result);
 		return result;
 		/*
 		Object result = pref.valueForKeyPath(keyPath);
 		if(result == null || result instanceof NSDictionary) {
 			PlistReader next = new PlistReader(inputFile,encoding,innerKeyPath);
 			next.rootDict = rootDict;
 			if(result == null) {
 				result = traverseKeyPath(keyPath, false, next);
 			} else {
 				next.pref = (NSDictionary)result;
 				appendKey(next.innerKeyPath, keyPath);
 				if(dictIslink(result)) {
 					result = resolveLink((NSDictionary)result, next);
 					if(state < 2) {
 						pref = cloneDictionary(pref, true);
 						state = 2;
 					}
 					pref.takeValueForKeyPath(result, keyPath);
 				}
 			}
 			if(result instanceof NSDictionary) {
 				return next;
 			}
 		}
 		return result;*/
 	}
 
 	public void takeValueForKey(Object value, String key) {
 		if(state < 1)
 			state = 1;
 		if(value instanceof SettingsReader)
 			value = ((SettingsReader)value).fullDictionary();
 		pref.takeValueForKey(value, key);
 	}
 
 	public Object valueForKey(String key) {
 		Object result =  pref.valueForKey(key);
 		if (result instanceof NSDictionary) {
 			return new PlistReader((NSDictionary)result);
 			/*
 			PlistReader next = new PlistReader(inputFilePath,encoding,innerKeyPath);
 			appendKey(next.innerKeyPath, key);
 			next.rootDict = rootDict;
 			next.pref = (NSDictionary)result;
 			result = resolveLink((NSDictionary)result, next);
 			if (result instanceof NSDictionary)
 				return next;*/
 		}
 		return result;
 	}
 
 	public void mergeValueToKeyPath(Object value, String keyPath) {
 		if(value == null)
 			return;
 		if("/".equals(keyPath) || ".".equals(keyPath))
 			keyPath = null;
 		if(keyPath == null && !(value instanceof NSDictionary))
 			throw new IllegalArgumentException(
 					"Can't override whole dictionary with single value");
 		if(state < 1)
 			pref = cloneDictionary(pref, true);
 		if(state < 2)
 			state = 2;
 		boolean shouldMerge = (value instanceof NSDictionary);
 		if(shouldMerge) {
 			value = resolveLink((NSDictionary)value,null);
 			shouldMerge = (value instanceof NSDictionary);
 		}
 		if(shouldMerge) {
 			NSMutableDictionary dict = (NSMutableDictionary)((keyPath==null)?pref:
 				traverseKeyPath(keyPath, true, false));
 			mergeDict(dict, (NSDictionary)value);
 		} else {
 			int dot = keyPath.lastIndexOf('.');
 			if(dot > 0) {
 				String path = keyPath.substring(0,dot);
 				String key = keyPath.substring(dot +1);
 				NSMutableDictionary dict = (NSMutableDictionary)traverseKeyPath(
 						path, true, false);
 				dict.takeValueForKey(value, key);
 			} else {
 				pref.takeValueForKey(value, keyPath);
 			}
 		}
 	}
 	
 	protected void mergeDict(NSMutableDictionary target, NSDictionary source) {
 		Enumeration enu = source.keyEnumerator();
 		while (enu.hasMoreElements()) {
 			String key = (String) enu.nextElement();
 			Object tVal = target.valueForKey(key);
 			Object sVal = source.valueForKey(key);
 			if(sVal instanceof NSDictionary) {
 				sVal = resolveLink((NSDictionary)sVal,null);
 			} else if (tVal instanceof NSDictionary) {
 				String filePath = Various.convertFilePath(sVal.toString());
 				Object plist = readPlist(filePath, encoding);
 				if(plist != null)
 					sVal = plist;
 			}
 			if(sVal instanceof NSDictionary) {
 				if(tVal instanceof NSMutableDictionary) 
 					mergeDict((NSMutableDictionary)tVal,(NSDictionary)sVal);
 				else
 					target.takeValueForKey(cloneDictionary((NSDictionary)sVal, true), key);
 			} else {
 				target.takeValueForKey(sVal,key);
 			}
 		}
 	}
 	
 	public PlistReader clone() {
 		PlistReader result = new PlistReader(inputFile, encoding, innerKeyPath);
 		result.state = this.state;
 		result.pref = cloneDictionary(pref, true);
 		result.rootDict = rootDict;
 //		result.toSave = toSave.mutableClone();
 		return result;
 	}
 	
 	public Object traverseKeyPath(String keyPath, boolean create, boolean reader) {
 		NSDictionary dict = pref;
 		String[] path = keyPath.split("\\.");
 		PlistReader resultReader = null;
 		for (int i = 0; i < path.length; i++) {
 			Object next = dict.valueForKey(path[i]);
 			if(resultReader != null)
 				appendKey(resultReader.innerKeyPath, path[i]);
 			if(next == null) {
 				if(!create)
 					return null;
 				next = new NSMutableDictionary();
 				dict.takeValueForKey(next, path[i]);
 			} else if(next instanceof NSDictionary) {
 				if(dictIslink(next)) {
 					if(reader && resultReader == null) {
 						resultReader = clone();
 					}
 					next = (NSMutableDictionary)resolveLink((NSDictionary)next, resultReader);
 					if(state < 1)
 						dict = changeState(path, i);
 					if(state < 2)
 						state = 2;
 					dict.takeValueForKey(next, path[i]);
 				}
 			} else if(reader || create || path.length -i > 1) {
 				String link = next.toString();
 				if(link.indexOf('/') > 0) {
 					if(reader) {
 						resultReader = new PlistReader(resolveFile(link),encoding,null);
 						resultReader.refresh();
 						next = resultReader.pref;
 					} else {
 						try {
 							next = readPlist(new FileInputStream(resolveFile(link)), encoding);
 						} catch (FileNotFoundException e) {
 							throw new NSForwardException(e);
 						}
 					}
 				} else {
 					if(reader) {
 						resultReader = (PlistReader)subreaderForPath(link, create);
 						next = resultReader.pref;
 					} else {
 						next = traverseKeyPath(link,create,false);
 					}
 				}
 				if(!(next instanceof NSMutableDictionary)) {
 					throw new IllegalStateException("PlistReader could not traverse key path '"
 							+ keyPath + "' at key '" + path[i] + "'. In a file " 
 							+ inputFile.getAbsolutePath());
 				}
 				if(state < 1)
 					dict = changeState(path, i);
 				if(state < 2)
 					state = 2;
 				dict.takeValueForKey(next, path[i]);
 			} else {
 				return next;
 			}
 			dict = (NSDictionary)next;
 		} // numerate path[]
 		if(reader) {
 			if(resultReader == null) {
 				resultReader = clone();
 				resultReader.pref = dict;
 				appendKey(resultReader.innerKeyPath, keyPath);
 			}
 			return resultReader;
 		}
 		return dict;
 	}
 	
 }
