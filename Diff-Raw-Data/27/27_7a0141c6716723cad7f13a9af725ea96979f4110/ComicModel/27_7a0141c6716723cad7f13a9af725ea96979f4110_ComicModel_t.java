 // Jimic, a minimalistic eComic viewer.
 // Copyright (C) 2006 Thomas Aglassinger
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 package net.sf.jimic;
 
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class ComicModel {
 
 	private String comicName;
 
 	private int loadStatus;
 
 	private Hashtable zipEntryMap;
 
 	private String[] zipEntryNames;
 
 	private ZipFile zipFile;
 
 	public ComicModel() {
 		super();
 		zipEntryMap = new Hashtable();
 	}
 
 	public String getComicName() {
 		return comicName;
 	}
 
 	public String[] getImageNames() {
 		return zipEntryNames;
 	}
 
 	public int getImageCount() {
 		return zipEntryMap.size();
 	}
 
 	public Image getImage(int imageIndex) throws IOException,
 			InterruptedException {
 		if (imageIndex < 0) {
 			throw new IllegalArgumentException(
 					"imageIndex must be at least 0 but is " + imageIndex);
 		} else if (imageIndex >= getImageCount()) {
 			throw new IllegalArgumentException("imageIndex must be at most "
 					+ (getImageCount() - 1) + " but is " + imageIndex);
 		}
 		return getImage(zipEntryNames[imageIndex]);
 	}
 
 	public void openZipFile(File archiveFile) throws IOException {
 		Vector names = new Vector();
 
 		closeZipFile();
 		zipFile = new ZipFile(archiveFile);
 
 		try {
 			FilenameFilter imageFilter = new ImageFilenameFilter();
 			Enumeration zipEntries = zipFile.entries();
 
 			while (zipEntries.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) zipEntries.nextElement();
 				String name = entry.getName();
 
 				if (imageFilter.accept(archiveFile, name)) {
 					names.addElement(name);
 					zipEntryMap.put(name, entry);
 				}
 			}
 			if (zipEntryMap.size() == 0) {
 				throw new IllegalArgumentException(
 						"comic must contain images using a supported format");
 			}
 		} catch (Exception error) {
 			closeZipFile();
 			IOException error2 = new IOException("cannot read file: "
 					+ archiveFile);
 			error2.initCause(error);
 			throw error2;
 		}
 
 		zipEntryNames = new String[names.size()];
 		zipEntryNames = toArray(names);
 		sort(zipEntryNames);
 		comicName = archiveFile.getName();
 	}
 
 	private String[] toArray(Vector items) {
 		int itemCount = items.size();
 		String[] result = new String[itemCount];
 		for (int i = 0; i < itemCount; i += 1) {
 			result[i] = (String) items.elementAt(i);
 		}
 		return result;
 	}
 
 	private void sort(String[] a) {
 		// TODO: Replace bubblesort by something faster.
 		int n = a.length;
 		for (int i = 0; i < n - 1; i++) {
 			for (int j = 0; j < n - 1 - i; j++)
 				if (a[j + 1].compareTo(a[j]) < 0) {
 					String tmp = a[j];
 					a[j] = a[j + 1];
 					a[j + 1] = tmp;
 				}
 		}
 	}
 
 	public void closeZipFile() throws IOException {
 		if (zipFile != null) {
 			try {
 				zipFile.close();
 			} finally {
 				zipFile = null;
 				zipEntryMap.clear();
 			}
 		}
 	}
 
	public Image getImage(String imageNameInZip) throws IOException, InterruptedException {
 		Image result;
 
		ZipEntry entryToRead = (ZipEntry) zipEntryMap.get(imageNameInZip);
		if (imageNameInZip == null) {
			throw new IllegalArgumentException("name must be zip file: " + imageNameInZip);
 		}
 		long entrySize = entryToRead.getSize();
 		if (entrySize < 0) {
			throw new IOException("cannot obtain size of entry: " + imageNameInZip);
 		} else if (entrySize > Integer.MAX_VALUE) {
			throw new IOException("size of entry " + imageNameInZip + " must be at most "
 					+ Integer.MAX_VALUE + " but is " + entrySize);
 		}
 		int bytesToRead = (int) entrySize;
 		byte[] imageData = new byte[bytesToRead];
 		InputStream imageStream = new BufferedInputStream(zipFile
 				.getInputStream(entryToRead));
 
 		try {
 			int indexInFile = 0;
 			int bytesRead;
 
 			do {
 				bytesRead = imageStream.read(imageData, indexInFile,
 						imageData.length - indexInFile);
 				if (bytesRead > 0) {
 					indexInFile += bytesRead;
 				}
 			} while (bytesRead > 0);
 			if (indexInFile != bytesToRead) {
				throw new IOException("number of bytes read for entry " + imageNameInZip
 						+ " must be " + bytesToRead + " but is " + bytesRead);
 			}
 		} finally {
 			imageStream.close();
 		}
		result = readImage(imageNameInZip, imageData);
 		return result;
 	}
 
 	/**
 	 * Read image from <code>imageFile</code> in any format supported by
 	 * <code>Toolkit.getImage()</code>.
 	 * 
 	 * @see Toolkit#getImage(java.lang.String)
 	 */
	public Image readImage(String name, byte[] imageData) throws InterruptedException,
 			IOException {
 		Image imageToLoad = Toolkit.getDefaultToolkit().createImage(imageData);
 		Component component = new Component() {
 			// Do nothing.
 		};
 		MediaTracker tracker = new MediaTracker(component);
 		int id = 1;
 
 		tracker.addImage(imageToLoad, id);
 		tracker.waitForID(id, 0);
 		loadStatus = tracker.statusID(id, false);
 		tracker.removeImage(imageToLoad, id);
 		if (loadStatus == MediaTracker.ABORTED) {
 			throw new InterruptedException("image loading cancelled");
 		} else if (loadStatus == MediaTracker.ERRORED) {
			throw new IOException("cannot read or parse image: " + name);
 		}
 		return imageToLoad;
 	}
 
 }
