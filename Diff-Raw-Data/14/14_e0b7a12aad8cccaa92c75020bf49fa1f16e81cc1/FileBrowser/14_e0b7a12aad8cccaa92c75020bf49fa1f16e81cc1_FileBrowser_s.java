 /*
  * Copyright 2012  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
  * 
  * This file is part of ahgdc.
  * 
  * ahgdc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ahgdc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ahgdc.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package android.hgd;
 
 import java.io.File;
 
 public class FileBrowser
 {
 	private String currentPath = "/";
 	
 	public static final int NO_ACTION = 0;
 	public static final int VALID_TO_UPLOAD = 1;
 	public static final int DIRECTORY = 2;
 	
 	public FileBrowser() {}
 	
 	/**
 	 * Return a list of all the files and directories in the current directory (currentPath).
 	 * Only the names are added to the string array, not the full path.
 	 * Prepend a parent directory (..) to the top of the list.
 	 * 
 	 * @return The contents of the current directory.
 	 */
 	public String[] getFilelist() {
 		File[] files = (new File(currentPath)).listFiles();
 		String[] res = new String[(files.length+1)];
 		res[0] = "..";
 		
 		for (int i = 1; i <= files.length; i++) {
 			res[i] = files[i-1].getName();
 		}
 		
 		return res;
 	}
 	
 	public void resetPath() {
 		currentPath = "/";
 	}
 	
 	public String getPath() {
 		return currentPath;
 	}
 	
 	public boolean isValidToUpload(File f) {
 		return (f.canRead() && f.exists() && f.isFile());
 	}
 	
 	public int update(String itemClicked) {
 		if (itemClicked.equals("..")) {
 			String parent = (new File(currentPath)).getParent();
 			if (parent == null) {
 				return NO_ACTION;
 			}
 			if (new File(parent) != null) {
 				currentPath = parent;
 				return DIRECTORY;
 			}
 		}
		if ((new File(currentPath + "/" + itemClicked)).isDirectory()) {
 			currentPath = currentPath + itemClicked + "/";
 			return DIRECTORY;
 		}
		if (isValidToUpload(new File(currentPath + "/" + itemClicked))) {
 			return VALID_TO_UPLOAD;
 		}
 		return NO_ACTION;
 	}
 
 }
