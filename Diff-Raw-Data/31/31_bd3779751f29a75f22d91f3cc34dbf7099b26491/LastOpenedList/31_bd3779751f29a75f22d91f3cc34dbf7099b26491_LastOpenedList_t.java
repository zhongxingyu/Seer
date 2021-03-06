 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
  *
  *  This file is modified by Dimitry Polivaev in 2008.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.main.application;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.freeplane.core.controller.Controller;
 import org.freeplane.core.frame.IMapViewChangeListener;
 import org.freeplane.core.frame.IMapViewManager;
 import org.freeplane.core.modecontroller.IMapChangeListener;
 import org.freeplane.core.modecontroller.MapChangeEvent;
 import org.freeplane.core.modecontroller.ModeController;
 import org.freeplane.core.model.MapModel;
 import org.freeplane.core.model.NodeModel;
 import org.freeplane.core.resources.ResourceController;
 import org.freeplane.core.ui.AFreeplaneAction;
 import org.freeplane.core.ui.MenuBuilder;
 import org.freeplane.core.ui.UIBuilder;
 import org.freeplane.core.ui.components.FreeplaneMenuBar;
 import org.freeplane.core.ui.components.UITools;
 import org.freeplane.core.url.UrlManager;
 import org.freeplane.core.util.Compat;
 import org.freeplane.core.util.LogTool;
 import org.freeplane.features.mindmapmode.MModeController;
 import org.freeplane.n3.nanoxml.XMLParseException;
 
 /**
  * This class manages a list of the maps that were opened last. It aims to
  * provide persistence for the last recent maps. Maps should be shown in the
  * format:"mode\:key",ie."Mindmap\:/home/joerg/freeplane.mm"
  */
 class LastOpenedList implements IMapViewChangeListener, IMapChangeListener {
	private static final String LAST_OPENED_LIST_LENGTH = "last_opened_list_length";
	private static final String OPENED_NOW = "openedNow";
	private static final String LAST_OPENED = "lastOpened";
 	public static final String LOAD_LAST_MAP = "load_last_map";
 	public static final String LOAD_LAST_MAPS = "load_last_maps";
	private static final String SEPARATOR = File.pathSeparator + File.pathSeparator;
 	private final Controller controller;
 	final private List<String> currenlyOpenedList = new LinkedList<String>();
 	/**
 	 * Contains Restore strings.
 	 */
 	final private List<String> lastOpenedList = new LinkedList<String>();
 	/**
 	 * Contains Restore string => map name (map.toString()).
 	 */
 	final private Map<String, String> mRestorableToMapName = new HashMap<String, String>();
 
 	LastOpenedList(final Controller controller) {
 		this.controller = controller;
		restoreList(LAST_OPENED, lastOpenedList);
 	}
 
 	public void afterViewChange(final Component oldView, final Component newView) {
 		if (newView == null) {
 			updateMenus();
 			return;
 		}
 		final IMapViewManager mapViewManager = controller.getMapViewManager();
 		final ModeController modeController = mapViewManager.getModeController(newView);
 		final MapModel map = mapViewManager.getModel(newView);
 		final String restoreString = getRestoreable(modeController, map);
 		updateList(map, restoreString);
 	}
 
 	public void afterViewClose(final Component oldView) {
 		final String restoreable = getRestoreable(oldView);
 		if (restoreable == null) {
 			return;
 		}
 		currenlyOpenedList.remove(restoreable);
 	}
 
 	public void afterViewCreated(final Component mapView) {
 		final String restoreable = getRestoreable(mapView);
 		if (restoreable == null) {
 			return;
 		}
 		currenlyOpenedList.add(restoreable);
 	}
 
 	public void beforeViewChange(final Component oldView, final Component newView) {
 	}
 
 	private int getMaxMenuEntries() {
		return ResourceController.getResourceController().getIntProperty(LAST_OPENED_LIST_LENGTH, 25);
 	}
 
 	private String getRestorable(final File file) {
 		if (file == null) {
 			return null;
 		}
 		return "MindMap:" + file.getAbsolutePath();
 	}
 
 	private String getRestoreable(final Component mapView) {
 		final IMapViewManager mapViewManager = controller.getMapViewManager();
 		final ModeController modeController = mapViewManager.getModeController(mapView);
 		final MapModel map = mapViewManager.getModel(mapView);
 		final String restoreString = getRestoreable(modeController, map);
 		return restoreString;
 	}
 
 	public String getRestoreable(final ModeController modeController, final MapModel map) {
 		if (map == null) {
 			return null;
 		}
 		if (!modeController.getModeName().equals(MModeController.MODENAME)) {
 			return null;
 		}
 		final File file = map.getFile();
 		return getRestorable(file);
 	}
 
 	private String getStringRep(final List<String> list) {
 		final StringBuilder strBldr = new StringBuilder();
 		for (final String s : list) {
 			strBldr.append(s + SEPARATOR);
 		}
 		return strBldr.toString();
 	}
 
 	public void mapChanged(final MapChangeEvent event) {
 		if (!event.getProperty().equals(UrlManager.MAP_URL)) {
 			return;
 		}
 		final URL before = (URL) event.getOldValue();
 		if (before != null) {
 			final String fileBefore = before.getFile();
 			if (fileBefore != null) {
 				final String restorable = getRestorable(new File(fileBefore));
 				currenlyOpenedList.remove(restorable);
 			}
 		}
 		final URL after = (URL) event.getNewValue();
 		if (after != null) {
 			final String fileAfter = after.getFile();
 			if (fileAfter != null) {
 				final String restorable = getRestorable(new File(fileAfter));
 				currenlyOpenedList.add(restorable);
 				updateList(event.getMap(), restorable);
 			}
 		}
 	}
 
 	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
 	}
 
 	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
 	}
 
 	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
 	                        final NodeModel child, final int newIndex) {
 	}
 
 	public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
 	}
 
 	public void open(final String restoreable) throws FileNotFoundException, XMLParseException, MalformedURLException,
 	        IOException, URISyntaxException {
 		final boolean changedToMapView = tryToChangeToMapView(restoreable);
 		if ((restoreable != null) && !(changedToMapView)) {
			final StringTokenizer token = new StringTokenizer(restoreable, SEPARATOR);
 			if (token.hasMoreTokens()) {
 				final String mode = token.nextToken();
 				if (controller.selectMode(mode)) {
 					final String fileName = token.nextToken("").substring(1);
 					controller.getModeController().getMapController().newMap(Compat.fileToUrl(new File(fileName)));
 				}
 			}
 		}
 	}
 
 	public void openMapsOnStart() {
 		final boolean loadLastMap = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAP);
 		final String lastMap;
 		if (loadLastMap && !lastOpenedList.isEmpty()) {
 			lastMap = lastOpenedList.get(0);
 		}
 		else {
 			lastMap = null;
 		}
 		final boolean loadLastMaps = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAPS);
 		if (loadLastMaps) {
 			final List<String> startList = new LinkedList<String>();
			restoreList(OPENED_NOW, startList);
 			safeOpen(startList);
 			if (!lastOpenedList.isEmpty()) {
 				tryToChangeToMapView(lastMap);
 			}
 			return;
 		}
 		if (loadLastMap && !lastOpenedList.isEmpty()) {
 			safeOpen(lastMap);
 		}
 	}
 
 	private void remove(final String restoreable) {
 		lastOpenedList.remove(restoreable);
 		updateMenus();
 	}
 
 	private void restoreList(final String key, final List<String> list) {
 		final String restored = ResourceController.getResourceController().getProperty(key, null);
 		if (restored != null) {
 			list.addAll(Arrays.asList(restored.split(SEPARATOR)));
 		}
 	}
 
 	void safeOpen(final List<String> maps) {
 		for (final String restoreable : maps) {
 			safeOpen(restoreable);
 		}
 	}
 
 	public void safeOpen(final String restoreable) {
 		try {
 			open(restoreable);
 		}
 		catch (final Exception ex) {
 			remove(restoreable);
 			UITools.errorMessage("An error occured on opening the file: " + restoreable + ".");
 			LogTool.warn(ex);
 		}
 	}
 
 	public void saveProperties() {
		ResourceController.getResourceController().setProperty(LAST_OPENED, getStringRep(lastOpenedList));
		ResourceController.getResourceController().setProperty(OPENED_NOW, getStringRep(currenlyOpenedList));
 	}
 
 	private boolean tryToChangeToMapView(final String restoreable) {
 		return controller.getMapViewManager().tryToChangeToMapView(mRestorableToMapName.get(restoreable));
 	}
 
 	private void updateList(final MapModel map, final String restoreString) {
 		if (restoreString != null) {
 			if (lastOpenedList.contains(restoreString)) {
 				lastOpenedList.remove(restoreString);
 			}
 			lastOpenedList.add(0, restoreString);
 			mRestorableToMapName.put(restoreString, map.getTitle());
 		}
 		updateMenus();
 	}
 
 	private void updateMenus() {
 		final ModeController modeController = controller.getModeController();
 		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
 		menuBuilder.removeChildElements(FreeplaneMenuBar.FILE_MENU + "/last");
 		int i = 0;
 		int maxEntries = getMaxMenuEntries();
 		for (final String key : lastOpenedList) {
 			if (i == 0
 			        && (!modeController.getModeName().equals(MModeController.MODENAME) || controller.getMap() == null || controller
 			            .getMap().getURL() == null)) {
 				i++;
 				maxEntries++;
 			}
 			if (i == maxEntries) {
 				break;
 			}
 			final AFreeplaneAction lastOpenedActionListener = new OpenLastOpenedAction(i++, key, controller, this);
 			menuBuilder.addAction(FreeplaneMenuBar.FILE_MENU + "/last", lastOpenedActionListener, UIBuilder.AS_CHILD);
 		}
 	}
 }
