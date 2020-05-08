 /*
  	org.manalith.ircbot.common/FileReadWriter.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.manalith.ircbot.common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 public class FileReadWriter {
 	private File f;
 	private String filename;
 	private String filestream;
 
 	private FileInputStream fis;
 	private InputStreamReader isr;
 	private FileOutputStream fos;
 	private OutputStreamWriter osw;
 
 	public FileReadWriter() throws FileNotFoundException {
 		f = null;
 		filename = "";
 		filestream = "";
 
 		fis = null;
 		isr = null;
 		fos = null;
 		osw = null;
 
 		this.openFile();
 	}
 
 	public FileReadWriter(String newFilename) {
 		this.setFilename(newFilename);
 		f = null;
 		filestream = "";
 
 		fis = null;
 		isr = null;
 		fos = null;
 		osw = null;
 
 		this.openFile();
 	}
 
 	public void setFilename(String newFilename) {
 		this.filename = newFilename;
 	}
 
 	public String getFilename() {
 		return this.filename;
 	}
 
 	public void setFileStream(String newFileStream) {
 		this.filestream = newFileStream;
 	}
 
 	public String getFileStream() {
 		return this.filestream;
 	}
 
 	protected File allocateFileObject() {
		
		String dir = "";
		
		if ( this.getFilename().contains(Character.toString(File.separatorChar)) )
		{
			int len = this.getFilename().split("\\" + Character.toString(File.separatorChar)).length - 1;
			
			for ( int i = 0 ; i < len; i++ )
			{
				if ( i != 0 ) dir += "/";
				dir += this.getFilename().split("\\/")[i];
			}
		}
			
		File t = new File(dir);
		if ( !t.exists() ) t.mkdirs();
		
 		return new File(this.filename);
 	}
 
 	protected boolean exists() {
 		return f.exists();
 	}
 
 	protected void openFile() {
 		f = this.allocateFileObject();
 	}
 
 	protected void createFile() throws FileNotFoundException, IOException {
 		f.createNewFile();
 		this.allocateStreamWriter();
 	}
 
 	protected void allocateStreamReader() throws FileNotFoundException {
 		fis = new FileInputStream(f);
 		isr = new InputStreamReader(fis);
 	}
 
 	protected void allocateStreamWriter() throws FileNotFoundException {
 		fos = new FileOutputStream(f);
 		osw = new OutputStreamWriter(fos);
 	}
 
 	public InputStreamReader getStreamReaderResource() {
 		return this.isr;
 	}
 
 	public OutputStreamWriter getStreamWriterResource() {
 		return this.osw;
 	}
 }
