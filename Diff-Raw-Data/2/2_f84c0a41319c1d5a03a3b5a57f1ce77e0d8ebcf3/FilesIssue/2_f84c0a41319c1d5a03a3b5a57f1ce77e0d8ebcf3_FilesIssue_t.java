 package com.isaacjg.darklight.issues;
 
 import java.io.File;
 
 import com.ijg.darklight.sdk.core.Issue;
 
 /*
  * FilesIssue - An Issue for Darklight Nova Core
  * Copyright  2013 Isaac Grant
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * FilesIssue is an Issue for Darklight Nova Core that checks if
  * a file, directory, or set of files and/or directories have been
  * deleted
  * 
  * @author Isaac Grant
  */
 
 public class FilesIssue extends Issue {
 
 	private File[] files;
 	
 	public FilesIssue() {
 		super("File Issue", "[type] \"[name]\" has been deleted");
 	}
 
 	public void setFile(File file) {
 		files = new File[] { file };
 		if (file.isDirectory()) {
 			setDescription(getDescription().replace("[type]", "Folder").replace("[name]", file.getName()));
		} else {
			setDescription(getDescription().replace("[type]", "File").replace("[name]", file.getName()));
 		}
 	}
 	
 	public void setFiles(File[] files, String fileGroupName) {
 		this.files = files;
 		setDescription(fileGroupName + " files have been deleted");
 	}
 	
 	@Override
 	public boolean isFixed() {
 		for (File file : files) {
 			if (file.exists()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
