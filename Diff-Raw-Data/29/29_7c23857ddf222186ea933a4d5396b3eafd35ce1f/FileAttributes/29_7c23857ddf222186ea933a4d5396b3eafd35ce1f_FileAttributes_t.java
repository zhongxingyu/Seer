 /*
 blizzy's Backup - Easy to use personal file backup application
 Copyright (C) 2011 Maik Schreiber
 
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
 package de.blizzy.backup;
 
 import java.io.IOException;
 import java.nio.file.FileSystemException;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.attribute.DosFileAttributeView;
 import java.nio.file.attribute.DosFileAttributes;
 import java.nio.file.attribute.FileTime;
 import java.nio.file.attribute.PosixFileAttributeView;
 import java.nio.file.attribute.PosixFileAttributes;
 
 public class FileAttributes {
 	private static enum OS {
 		DOS, POSIX;
 	}
 	
 	private static OS os;
 	
 	private Path path;
 	private DosFileAttributeView dosView;
 	private DosFileAttributes dosAttrs;
 	private PosixFileAttributeView posixView;
 	private PosixFileAttributes posixAttrs;
 
 	private FileAttributes(Path path, DosFileAttributeView dosView, DosFileAttributes dosAttrs,
 			PosixFileAttributeView posixView, PosixFileAttributes posixAttrs) {
 
 		if ((posixView != null) && (posixAttrs != null)) {
 			dosView = null;
 			dosAttrs = null;
 		} else {
 			posixView = null;
 			posixAttrs = null;
 		}
 
 		this.path = path;
 		this.dosView = dosView;
 		this.dosAttrs = dosAttrs;
 		this.posixView = posixView;
 		this.posixAttrs = posixAttrs;
 	}
 	
 	public static FileAttributes get(Path path) throws IOException {
 		DosFileAttributeView dosView = null;
 		DosFileAttributes dosAttrs = null;
 		if ((os == OS.DOS) || (os == null)) {
 			dosView = Files.getFileAttributeView(
 					path, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
 			try {
 				dosAttrs = Files.readAttributes(path, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
 			} catch (UnsupportedOperationException e) {
 				// ignore
 			} catch (FileSystemException e) {
 				// ignore
 			}
 		}
 		
 		PosixFileAttributeView posixView = null;
 		PosixFileAttributes posixAttrs = null;
 		if ((os == OS.POSIX) || (os == null)) {
 			posixView = Files.getFileAttributeView(
 					path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
 			try {
 				posixAttrs = Files.readAttributes(path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
 			} catch (UnsupportedOperationException e) {
 				// ignore
 			} catch (FileSystemException e) {
 				// ignore
 			}
 		}
 		
 		if (os == null) {
 			os = ((posixView != null) && (posixAttrs != null)) ? OS.POSIX : OS.DOS;
 		}
 
 		return new FileAttributes(path, dosView, dosAttrs, posixView, posixAttrs);
 	}
 	
 	public boolean isHidden() {
		if (dosAttrs != null) {
			return dosAttrs.isHidden();
		}
		if (posixAttrs != null) {
			return path.getFileName().startsWith("."); //$NON-NLS-1$
		}
		return false;
 	}
 	
 	public void setHidden(boolean hidden) throws IOException {
 		if (dosView != null) {
 			dosView.setHidden(hidden);
 		}
 	}
 	
 	public FileTime getCreationTime() {
		if (dosAttrs != null) {
			return dosAttrs.creationTime();
		}
		if (posixAttrs != null) {
			return posixAttrs.creationTime();
		}
		return null;
 	}
 	
 	public FileTime getModificationTime() {
		if (dosAttrs != null) {
			return dosAttrs.lastModifiedTime();
		}
		if (posixAttrs != null) {
			return posixAttrs.lastModifiedTime();
		}
		return null;
 	}
 	
 	public void setTimes(FileTime creationTime, FileTime modificationTime) throws IOException {
 		if (dosView != null) {
 			dosView.setTimes(modificationTime, null, creationTime);
		} else if (posixView != null) {
 			posixView.setTimes(modificationTime, null, creationTime);
 		}
 	}
 }
