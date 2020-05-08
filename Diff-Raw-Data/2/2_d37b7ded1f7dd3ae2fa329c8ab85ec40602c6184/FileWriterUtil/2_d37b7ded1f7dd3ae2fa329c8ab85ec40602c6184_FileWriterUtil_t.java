 // FileWriterUtil.java
 
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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import com.webobjects.appserver.WOComponent;
 import com.webobjects.appserver.WOContext;
 import com.webobjects.foundation.NSData;
 import com.webobjects.foundation.NSForwardException;
 
 public class FileWriterUtil implements java.io.Closeable {
 
 	protected File base;
 	protected boolean zip;
 	public boolean overwrite = true;
 	protected ZipOutputStream zipout;
 	public WOContext ctx;
 	protected Object curDir;
 	
 	public FileWriterUtil (String filename, boolean isZip, boolean overwrite)
 				throws java.io.IOException{
 		this(new File(filename), isZip, overwrite);
 	}
 	public FileWriterUtil (File file, boolean isZip, boolean overwrite)
 				throws java.io.IOException{
 		super();
 		base = file;
 		zip = isZip;
 		if(zip) {
 			File dir = file.getParentFile();
 			if(file.exists()) {
 				if(overwrite) {
 					file.delete();
 				} else {
 					String suffix = file.getName();
 					int dot = suffix.lastIndexOf('.');
 					StringBuilder buf = new StringBuilder(suffix);
 					if(dot <= 0) {
 						dot = suffix.length();
 						buf.append('.').append('.');
 						suffix = null;
 					} else {
 						suffix = suffix.substring(dot);
 					}
 					int idx = 1;
 					while(file.exists()) {
 						buf.delete(dot +1, buf.length());
 						buf.append(idx++);
 						if(suffix != null) buf.append(suffix);
 						file = new File(dir,buf.toString());
 					}
 					base.renameTo(file);
 				}
 			}
 			StringBuilder name = new StringBuilder(28);
 			name.append('_').append(base.getName());
 			base = new File(dir,name.toString());
 			zipout = new ZipOutputStream(new FileOutputStream(base));
 		} else if(!file.exists()) {
 			file.mkdirs();
 		} else if (!file.isDirectory()) {
 			throw new IllegalArgumentException("Directory is required for non-zip out");
 		}
 	}
 	
 	public File getBase() {
 		return base;
 	}
 	
 	protected void finalize() throws Throwable {
 		close();
 		super.finalize();
 	}
 	
 	public void close() {
 		try {
 			if(zipout != null) {
 				zipout.close();
 				base.renameTo(new File(base.getParentFile(),base.getName().substring(1)));
 			}
 			zipout = null;
 			base = null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new NSForwardException(e);
 		}
 	}
 	
     public void writeFile(String filename, WOComponent page)  {
 		NSData content = (page == null)? null : page.generateResponse().content();
 		writeData(filename, content);
     }
     public void writeData(String filename, NSData content)  {
     	try {
         	OutputStream stream = null;
 			if(zip) {
 				stream = zipout;
 				if(curDir != null)
 					filename = (String)curDir + filename;
 				if(content == null && !(filename.charAt(filename.length() -1) != '/'))
 					filename = filename + '/';
 				zipout.putNextEntry(new ZipEntry(filename));
 			} else {
 				File folder = (curDir == null)? base : (File)curDir;
 				File file = new File(folder,filename);
 				if(content == null)
 					file.mkdirs();
 				else {
 					if(file.exists()) {
 						if(overwrite)
 							file.delete();
 						else
 							return;
 					}
 					stream = new FileOutputStream(file);
 				}
 			}
 			if(content != null) {
 				content.writeToStream(stream);
 				if(!zip)
 					stream.close();
 			}
 		} catch (Exception e) {
 			throw new NSForwardException(e);
 		}
     }
     
     public void enterDir(String dirName, boolean root) {
     	if(dirName == null) {
     		curDir = null;
     		return;
     	}
     	if(dirName.charAt(0) == '/') {
     		dirName = dirName.substring(1);
     		root = true;
     	}
     	if(root)
     		curDir = null;
     	if(zip) {
     		if(dirName.charAt(dirName.length() -1) != '/')
     			dirName = dirName + '/';
     		curDir = (root)?dirName: (String)curDir + dirName;
     		try {
 				zipout.putNextEntry(new ZipEntry((String)curDir));
 			} catch (IOException e) {
 				throw new NSForwardException(e);
 			}
     	} else {
    		File folder = (root || curDir == null)?base:(File)curDir;
     		folder = new File(folder,dirName);
     		if(!folder.exists())
     			folder.mkdirs();
     		curDir = folder;
     	}
     }
     
     public Object leaveDir() {
     	if(curDir == null)
     		return null;
     	if(zip) {
     		String path = (String)curDir;
     		int idx = path.lastIndexOf('/', path.length() -2);
     		if(idx > 0)
     			curDir = path.substring(0, idx +1);
     		else
     			curDir = null;
     	} else {
 			File folder = (File)curDir;
 			curDir = folder.getParentFile();
 			if(base.equals(curDir))
 				curDir = null;
 		}
     	return curDir;
     }
     
     public File currDir() {
     	if(zip)
     		return null;
     	if(curDir == null)
     		return base;
     	return (File)curDir;
     }
     
     public static class Zipper implements Runnable {
     	protected File folder;
     	protected File target;
     	protected ZipOutputStream zip;
     	protected StringBuilder path;
     	
     	public Zipper(File source, File target) {
     		folder = source;
     		this.target = target;
     	}
 
 		public void run() {
 			try {
 				File tmp = new File(target.getParentFile(),target.getName() + ".tmp");
 				zip = new ZipOutputStream(new FileOutputStream(tmp));
 				if(target.exists())
 					target.delete();
 				path = new StringBuilder();
 				recurse(folder);
 				zip.close();
 				tmp.renameTo(target);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		protected void recurse(File file) throws IOException {
 			int length = path.length();
 			String name = file.getName();
 			if(file.isDirectory())
 				name = name + '/';
 			path.append(name);
 			ZipEntry entry = new ZipEntry(path.toString());
 			zip.putNextEntry(entry);
 			if(file.isDirectory()) {
 				File[] subs = file.listFiles();
 				for (int i = 0; i < subs.length; i++) {
 					recurse(subs[i]);
 				}
 			} else {
 				entry.setSize(file.length());
 				byte[] buf = new byte[4096];
 				FileInputStream in = new FileInputStream(file);
 				while (in.available() > 0) {
 					int len = in.read(buf);
 					zip.write(buf, 0, len);
 				}
 				in.close();
 			}
 			path.delete(length, path.length());
 		}
     }
 }
