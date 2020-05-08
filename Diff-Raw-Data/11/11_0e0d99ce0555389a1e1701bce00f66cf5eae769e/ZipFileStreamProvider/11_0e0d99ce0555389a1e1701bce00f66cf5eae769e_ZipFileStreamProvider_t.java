 /*
  * Copyright 2011-2013 Jeroen Meetsma - IJsberg
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.io;
 
 import java.io.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  */
 public class ZipFileStreamProvider implements FileStreamProvider {
 
 	private ZipOutputStream out;
 	private BufferedOutputStream bufferedOut;
 
 	public ZipFileStreamProvider(String fileName) {
 		try {
 			File f = new File(fileName);
 			new File(f.getParent()).mkdirs();
 			out = new ZipOutputStream(new FileOutputStream(f));
 			bufferedOut = new BufferedOutputStream(out);
 		} catch (IOException e) {
 			throw new RuntimeException("unable to save to " + fileName, e);
 		}
 	}
 
 	@Override
 	public PrintStream createPrintStream(String fileName) {
        String cleanPath = FileSupport.convertToUnixStylePath(fileName);
        if(fileName.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
		return new PrintStream(createOutputStream(cleanPath));
 	}
 
 	public void closeCurrentStream() {
 		try {
 			bufferedOut.flush();
 			out.closeEntry();
 		} catch (IOException e) {
 			throw new RuntimeException("unable to close zipfile entry", e);
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			closeCurrentStream();
 			bufferedOut.close();
 			out.close();
 		} catch (IOException e) {
 			throw new RuntimeException("unable to close zipfile", e);
 		}
 	}
 
 	@Override
 	public OutputStream createOutputStream(String fileName) {
 		try {
 			ZipEntry e = new ZipEntry(FileSupport.convertToUnixStylePath(fileName));
 			out.putNextEntry(e);
 			return bufferedOut;
 		} catch (IOException e) {
 			throw new RuntimeException("unable to save to " + fileName, e);
 		}
 	}
 }
