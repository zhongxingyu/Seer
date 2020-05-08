 package org.moo.android.filebrowser;
 
 /*
  * Copyright (c) 2009, Bahtiar `kalkin-` Gadimov
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, 
  * are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this list of 
  *   conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice, this list 
  *   of conditions and the following disclaimer in the documentation and/or other materials 
  *   provided with the distribution.
  * 
  * - Neither the name of the Moo Productions nor the names of its contributors may be used 
  *   to endorse or promote products derived from this software without specific prior written 
  *   permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * This class returns all dirs and files from a directory filtered, and sorted
  * aphabeticaly
  * 
  * @author Bahtiar `kalkin-` Gadimov
  * 
  */
 public class DirectoryManager {
 
	public ArrayList<File> getDirectoryListing(File directory, String[] mFilters) {
 
 		File[] dirs = directory.listFiles(new DirFilter());
 		File[] files;
 		if (mFilters != null)
 			files = directory.listFiles(new FileFilter(mFilters));
 		else
 			files = directory.listFiles(new FileFilter());
 
 		int dirEntries = 0;
 		if (directory.getParentFile() != null)
 			dirEntries = 1;
 
 		if (dirs == null && files == null)
 			return new ArrayList<File>(1);
 		if (dirs != null) {
 			Arrays.sort(dirs);
 			dirEntries += dirs.length;
 		}
 
 		if (files != null) {
 			Arrays.sort(files);
 			dirEntries += files.length;
 		}
 		ArrayList<File> result = new ArrayList<File>(dirEntries);
 
 		if (directory.getParentFile() != null)
 			result.add(directory.getParentFile());
 
 		for (File file : dirs) {
 			result.add(file);
 		}
 		for (File file : files) {
 			result.add(file);
 		}
 		return result;
 	}
 }
