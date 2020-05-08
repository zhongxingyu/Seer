 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.gui.utils;
 
 import java.awt.Dimension;
 import java.awt.Point;
 
 import javax.swing.JFrame;
 
 import net.lmxm.ute.gui.GuiContants;
 import net.lmxm.ute.utils.AbstractPreferences;
 
 /**
  * Retrieves and persists user settings stored using java Preferences.
  */
 public final class UserPreferences extends AbstractPreferences {
 
 	/** The Constant JOBS_SPLIT_PANE_DIVIDER_LOCATION. */
 	private static final String JOBS_SPLIT_PANE_DIVIDER_LOCATION = "jobsSplitPaneDividerLocation";
 
 	/** The Constant LAST_FILE_EDITED_PATH. */
 	private static final String LAST_FILE_EDITED_PATH = "lastFileEditedPath";
 
 	/** The Constant MAIN_SPLIT_PANE_DIVIDER_LOCATION. */
 	private static final String MAIN_SPLIT_PANE_DIVIDER_LOCATION = "mainSplitPaneDividerLocation";
 
 	/** The Constant WINDOW_LOCATION. */
 	private static final String WINDOW_LOCATION = "windowLocation";
 
 	/** The Constant WINDOW_SIZE. */
 	private static final String WINDOW_SIZE = "windowSize";
 
 	/** The Constant WINDOW_STATE. */
 	private static final String WINDOW_STATE = "windowState";
 
 	/**
 	 * Instantiates a new user preferences.
 	 */
 	public UserPreferences() {
 		super(UserPreferences.class);
 	}
 
 	/**
 	 * Gets the jobs split pane divider location.
 	 * 
 	 * @return the jobs split pane divider location
 	 */
 	public int getJobsSplitPaneDividerLocation() {
 		return getInt(JOBS_SPLIT_PANE_DIVIDER_LOCATION, GuiContants.DEFAULT_JOBS_SPLIT_PANE_DIVIDER_LOCATION);
 	}
 
 	/**
 	 * Retrieves "last edited file path" setting from Preferences.
 	 * 
 	 * @return Last file edited path
 	 */
 	public String getLastFileEditedPath() {
 		return getString(LAST_FILE_EDITED_PATH);
 	}
 
 	/**
 	 * Gets the main split pane divider location.
 	 * 
 	 * @return the main split pane divider location
 	 */
 	public int getMainSplitPaneDividerLocation() {
 		return getInt(MAIN_SPLIT_PANE_DIVIDER_LOCATION, GuiContants.DEFAULT_MAIN_SPLIT_PANE_DIVIDER_LOCATION);
 	}
 
 	/**
 	 * Gets the window location.
 	 * 
 	 * @return the window location
 	 */
 	public Point getWindowLocation() {
 		return getPoint(WINDOW_LOCATION);
 	}
 
 	/**
 	 * Gets the window size.
 	 * 
 	 * @return the window size
 	 */
 	public Dimension getWindowSize() {
 		final Dimension dimension = getDimension(WINDOW_SIZE);
 
		if (dimension.getHeight() == 0 || dimension.getWidth() == 0) {
 			return new Dimension(GuiContants.DEFAULT_WINDOW_WIDTH, GuiContants.DEFAULT_WINDOW_HEIGHT);
 		}
 		else {
 			return dimension;
 		}
 	}
 
 	/**
 	 * Gets the window state.
 	 * 
 	 * @return the window state
 	 */
 	public int getWindowState() {
 		return getInt(WINDOW_STATE, JFrame.MAXIMIZED_BOTH);
 	}
 
 	/**
 	 * Removes the last file edited path.
 	 */
 	public void removeLastFileEditedPath() {
 		removePreference(LAST_FILE_EDITED_PATH);
 	}
 
 	/**
 	 * Sets the jobs split pane divider location.
 	 * 
 	 * @param dividerLocation the new jobs split pane divider location
 	 */
 	public void setJobsSplitPaneDividerLocation(final int dividerLocation) {
 		setInt(JOBS_SPLIT_PANE_DIVIDER_LOCATION, dividerLocation);
 	}
 
 	/**
 	 * Persists "open last edited file" setting to Preferences.
 	 * 
 	 * @param path the new last file edited path
 	 */
 	public void setLastFileEditedPath(final String path) {
 		setString(LAST_FILE_EDITED_PATH, path);
 	}
 
 	/**
 	 * Sets the main split pane divider location.
 	 * 
 	 * @param dividerLocation the new main split pane divider location
 	 */
 	public void setMainSplitPaneDividerLocation(final int dividerLocation) {
 		setInt(MAIN_SPLIT_PANE_DIVIDER_LOCATION, dividerLocation);
 	}
 
 	/**
 	 * Sets the window location.
 	 * 
 	 * @param location the new window location
 	 */
 	public void setWindowLocation(final Point location) {
 		setPoint(WINDOW_LOCATION, location);
 	}
 
 	/**
 	 * Sets the window size.
 	 * 
 	 * @param windowSize the new window size
 	 */
 	public void setWindowSize(final Dimension windowSize) {
 		setDimension(WINDOW_SIZE, windowSize);
 	}
 
 	/**
 	 * Sets the window state.
 	 * 
 	 * @param windowState the new window state
 	 */
 	public void setWindowState(final int windowState) {
 		setInt(WINDOW_STATE, windowState);
 	}
 }
