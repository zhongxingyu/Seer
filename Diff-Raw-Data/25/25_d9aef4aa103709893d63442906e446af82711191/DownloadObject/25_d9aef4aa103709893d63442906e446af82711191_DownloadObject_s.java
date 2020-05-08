 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery.Gallery3;
 
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 
 public class DownloadObject implements GalleryDownloadObject {
 
     private final String mSourceLink;
     private final String mRootLink;
     private final int mFileSize;
 
     public DownloadObject(String rootLink, String sourceLink, int fileSize) {
 	mSourceLink = sourceLink;
 	mRootLink = rootLink;
 	mFileSize = fileSize;
     }
 
     public String getRootLink() {
 	return mRootLink;
     }
 
     public String getUniqueId() {
 	return mSourceLink;
     }
 
     public int getFileSize() {
 	return mFileSize;
     }
 
     @Override
     public String toString() {
 	return mSourceLink;
     }
 }
