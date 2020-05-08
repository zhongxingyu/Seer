 package org.dianexus.triceps;
 
 import java.io.File;
import java.util.Hashtable;
 
 /*public*/ final class SourceInfo implements VersionIF {
 	/*public*/ static final int SOURCE_OK = 0;
 	/*public*/ static final int SOURCE_DOES_NOT_EXIST = 1;
 	/*public*/ static final int SOURCE_IS_NOT_A_FILE = 2;
 	/*public*/ static final int SOURCE_IS_NOT_READABLE = 3;
 	/*public*/ static final int SOURCE_IS_NULL = 4;
 	/*public*/ static final int SOURCE_READ_ERROR = 5;
 	/*public*/ static final String[] STATUS_MSG = { "OK", "does not exist", "not a file", "not readable", "null filename", "read error" };
 
 	private String source = null;
 	private long lastModified = 0L;
 	private long fileLength = 0L;
 	private int status = SOURCE_IS_NULL;
 	
	private static final Hashtable sources = new Hashtable();
 	
 	private SourceInfo(String src) { 
 		source = src;
 		getInfo();
 	}
 	
	/*public*/ static synchronized SourceInfo getInstance(String src) {
 		SourceInfo si = new SourceInfo(src);
 		Object o = sources.get(src);
 		
 		if (o instanceof SourceInfo) {
 			SourceInfo old = (SourceInfo) o;
 			if (si.equals(o)) {
 				return old;	// so can more readily tell whether a file has changed.
 			}
 			else {
 				sources.put(src,si);
 				return si;
 			}
 		}
 		else {
			sources.put(src,si); 
 			return si;
 		}
 	}
 	
 	
 	private void getInfo() {
 		try {
 			if (source != null && source.trim().length() > 0) {
 				File file = new File(source);
 				if (!file.exists()) {
 					setStatus(SOURCE_DOES_NOT_EXIST);
 				}
 				else if (!file.isFile()) {
 					setStatus(SOURCE_IS_NOT_A_FILE);
 				}
 				else if (!file.canRead()) {
 					setStatus(SOURCE_IS_NOT_READABLE);
 				}
 				else {
 					lastModified = file.lastModified();
 					fileLength = file.length();
 					setStatus(SOURCE_OK);
 				}
 			}
 			else {
 				setStatus(SOURCE_IS_NULL);
 			}
 		}
 		catch (Exception e) {
 			setStatus(SOURCE_READ_ERROR);
 		}
 	}
 	
 	/*public*/ boolean isReadable() { return (status == SOURCE_OK); }
 	/*public*/ int getError() { return status; }
 	/*public*/ String getSource() { return source; }
 	/*public*/ long getLastModified() { return lastModified; }
 	/*public*/ long getLength() { return fileLength; }
 	
 	/*public*/ boolean equals(SourceInfo o) {
 		if (o == null || this.status != SOURCE_OK)
 			return false;
 		
 		return (source.equals(o.getSource()) &&
 			lastModified == o.getLastModified() &&
 			fileLength == o.getLength() &&
 			status == o.getError());
 	}
 	
 	private void setStatus(int st) {
 		status = st;
 		
 		if (DEBUG) {
 			if (status != SOURCE_OK) {
 				Logger.writeln("##SourceInfo.setStatus(" + source + ")->" + STATUS_MSG[status]);
 			}
 		}
 	}
 }
 		
