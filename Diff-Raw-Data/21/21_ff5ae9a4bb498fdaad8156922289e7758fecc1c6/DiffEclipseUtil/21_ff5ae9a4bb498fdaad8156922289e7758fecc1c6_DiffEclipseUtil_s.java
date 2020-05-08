 /*******************************************************************************
  * Copyright (c) 2011 Robert Wloch and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Robert Wloch - initial API and implementation
  *******************************************************************************/
 package de.rowlo.diffeclipse.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import de.rowlo.diffeclipse.event.DiffUtilFileListEvent;
 import de.rowlo.diffeclipse.event.DiffUtilFileListListener;
 import de.rowlo.diffeclipse.event.FolderScannedEvent;
 import de.rowlo.diffeclipse.event.FolderScannerListener;
 import de.rowlo.diffeclipse.event.TextLocationEclipseHandler;
 
 /**
  * 
  * @author Robert Wloch (robert@rowlo.de)
  */
 public final class DiffEclipseUtil implements FolderScannerListener {
 
 	private final Map<String, Set<String>> plainEclipseFiles = Collections
 			.synchronizedMap(new HashMap<String, Set<String>>());
 	private final Map<String, Set<String>> extendedEclipseFiles = Collections
 			.synchronizedMap(new HashMap<String, Set<String>>());
 	private String extendedEclipseLocation = null;
 	private String plainEclipseLocation = null;
 	private TextLocationEclipseHandler plainEclipseHandler;
 	private TextLocationEclipseHandler extendedEclipseHandler;
 	private final List<DiffUtilFileListListener> diffUtilFileListListeners = new ArrayList<DiffUtilFileListListener>();
 
 	public final static DiffEclipseUtil INSTANCE = new DiffEclipseUtil();
 
 	private DiffEclipseUtil() {
 	}
 
 	public void addDiffUtilFileListListener(DiffUtilFileListListener listener) {
 		if (listener != null && !diffUtilFileListListeners.contains(listener)) {
 			diffUtilFileListListeners.add(listener);
 		}
 	}
 
 	public void removeDiffUtilFileListListener(DiffUtilFileListListener listener) {
 		if (listener != null && diffUtilFileListListeners.contains(listener)) {
 			diffUtilFileListListeners.remove(listener);
 		}
 	}
 
 	protected void notifyDiffUtilFileListListeners(final String location) {
 		List<DiffUtilFileListListener> listeners = new ArrayList<DiffUtilFileListListener>(diffUtilFileListListeners);
 		for (DiffUtilFileListListener listener : listeners) {
 			DiffUtilFileListEvent event = new DiffUtilFileListEvent(location);
 			listener.onFileListChange(event);
 		}
 	}
 
 	/**
 	 * Set all the files contained in a location.
 	 * 
 	 * @param location
 	 *            a path in a file system
 	 * @param files
 	 *            collection of path strings without the location prefix, e.g.
 	 *            the full path should be location + files[n]
 	 */
 	protected void setPlainEclipseFiles(String location, Set<String> files) {
 		plainEclipseFiles.clear();
 		if (location != null && files != null) {
 			plainEclipseFiles.put(location, files);
 			plainEclipseLocation = location;
 			notifyDiffUtilFileListListeners(plainEclipseLocation);
 		}
 	}
 
 	/**
 	 * Set all the files contained in a location.
 	 * 
 	 * @param location
 	 *            a path in a file system
 	 * @param files
 	 *            collection of path strings without the location prefix, e.g.
 	 *            the full path should be location + files[n]
 	 */
 	protected void setExtendedEclipseFiles(String location, Set<String> files) {
 		extendedEclipseFiles.clear();
 		if (location != null && files != null) {
 			extendedEclipseFiles.put(location, files);
 			extendedEclipseLocation = location;
 			notifyDiffUtilFileListListeners(extendedEclipseLocation);
 		}
 	}
 
 	/**
 	 * Checks, if filePath is in the list of known files for the plain Eclipse
 	 * location. If filePath starts with the plain or extended Eclipse' location
 	 * then that will be trimmed first.
 	 * 
 	 * @param filePath
 	 * @return true, if filePath is part of plain Eclipse' files
 	 * 
 	 * @see #setPlainEclipseFiles(String, Set)
 	 */
 	public boolean isPlainEclipseFile(String filePath) {
 		if (filePath == null) {
 			return false;
 		}
 		if (plainEclipseLocation != null && filePath.startsWith(plainEclipseLocation)) {
 			filePath = filePath.substring(plainEclipseLocation.length());
 		}
 		if (extendedEclipseLocation != null && filePath.startsWith(extendedEclipseLocation)) {
 			filePath = filePath.substring(extendedEclipseLocation.length());
 		}
 		Set<String> files = plainEclipseFiles.get(plainEclipseLocation);
		return files.contains(filePath);
 	}
 
 	public boolean isPlainEclipseLocation(String location) {
 		return (plainEclipseLocation != null && plainEclipseLocation.equals(location));
 	}
 
 	public String getPlainEclipseLocation() {
 		return plainEclipseLocation;
 	}
 
 	/**
 	 * Checks, if filePath is in the list of known files for the extended
 	 * Eclipse location. If filePath starts with the plain or extended Eclipse'
 	 * location then that will be trimmed first.
 	 * 
 	 * @param filePath
 	 * @return true, if filePath is part of extended Eclipse' files
 	 * 
 	 * @see #setExtendedEclipseFiles(String, Set)
 	 */
 	public boolean isExtendedEclipseFile(String filePath) {
 		if (filePath == null) {
 			return false;
 		}
 		if (plainEclipseLocation != null && filePath.startsWith(plainEclipseLocation)) {
 			filePath = filePath.substring(plainEclipseLocation.length());
 		}
 		if (extendedEclipseLocation != null && filePath.startsWith(extendedEclipseLocation)) {
 			filePath = filePath.substring(extendedEclipseLocation.length());
 		}
 		Set<String> files = extendedEclipseFiles.get(extendedEclipseLocation);
 		return files.contains(filePath);
 	}
 
 	public boolean isExtendedEclipseLocation(String location) {
 		return (extendedEclipseLocation != null && extendedEclipseLocation.equals(location));
 	}
 
 	public String getExtendedEclipseLocation() {
 		return extendedEclipseLocation;
 	}
 
 	/**
 	 * Sets the {@link TextLocationEclipseHandler} that scans the plain Eclipse
 	 * location.
 	 * 
 	 * @param eclipseHandler
 	 */
 	public void usePlainEclipseHandler(TextLocationEclipseHandler eclipseHandler) {
 		if (this.plainEclipseHandler == null) {
 			this.plainEclipseHandler = eclipseHandler;
 			eclipseHandler.addFolderScannerListener(this);
 		}
 	}
 
 	/**
 	 * Sets the {@link TextLocationEclipseHandler} that scans the extended
 	 * Eclipse location.
 	 * 
 	 * @param eclipseHandler
 	 */
 	public void useExtendedEclipseHandler(TextLocationEclipseHandler eclipseHandler) {
 		if (this.extendedEclipseHandler == null) {
 			this.extendedEclipseHandler = eclipseHandler;
 			eclipseHandler.addFolderScannerListener(this);
 		}
 	}
 
 	@Override
 	public void onFolderScanned(FolderScannedEvent event) {
 		if (event != null) {
 			TextLocationEclipseHandler eclipseHandler = event.getEclipseHandler();
 			Set<String> files = event.getFiles();
 			String eclipseLocation = event.getFolderLocation();
 			if (eclipseHandler != null && files != null) {
 				if (eclipseHandler.equals(this.plainEclipseHandler)) {
 					setPlainEclipseFiles(eclipseLocation, files);
 				} else if (eclipseHandler.equals(this.extendedEclipseHandler)) {
 					setExtendedEclipseFiles(eclipseLocation, files);
 				}
 			}
 		}
 	}
 }
