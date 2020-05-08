 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.view;
 
 import java.io.File;
 
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 
 public final class UIConstants {
 	
 	private UIConstants() {
 		// not needed
 	}
 	
 	// ------------------ GUI Constants ------------------
 	public static final int SCROLL_SPEED_MULTIPLICATOR = 3;
 	
 	public static final int RIGHT_PANEL_LARGE_WIDTH = 320;
 	public static final int RIGHT_PANEL_MEDIUM_WIDTH = 225;
 	public static final int RIGHT_PANEL_SMALL_WIDTH = 150;
 	public static final int RIGHT_PANEL_NO_WIDTH = 0;
 
 	/** The minimum width of the shell in pixels. The shell can never have a smaller width than this. */
 	public static final int MIN_SHELL_WIDTH = 1110;
 	/** The minimum height of the shell in pixels. The shell can never have a smaller height than this. */
 	public static final int MIN_SHELL_HEIGHT = 700;
 	
 	// ------------------ Browser Anchors ------------------
 	public static final String NO_ANCHOR_DEFINED = "";
 	
 	// ------------------ Browser Constants ------------------
 	public static final String STYLE_CSS = "file://" + FileSystemLocations.getStyleCSS();
 	public static final String EFFECTS_JS = "file://" + FileSystemLocations.getAppDataDir() + File.separatorChar + "effects.js";
	public static final String META_PARAMS = "http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" charset=\"utf-8\"";
 	
 	// ------------------ Browser Listener Constants ------------------
 	public static final String SHOW_UPDATE_ENTRY_COMPOSITE = "show:///updateComposite=";
 	public static final String DELETE_ENTRY = "show:///deleteComposite=";
 	public static final String SHOW_BIG_PICTURE = "show:///bigPicture=";
 	public static final String SHOW_LAST_PAGE = "show:///lastPage";
 	public static final String SHOW_DETAILS = "show:///details=";
 	public static final String ADD_ADDITIONAL_ALBUM_ITEMS = "show:///addAdditionalAlbumItems";
 	public static final String SHOW_DETAILS_VIEW_OF_ALBUM = "show:///showDetails=";
 	public static final String BROWSER_RESIZED = "show:///browserResized";
 	public static final String SHOW_URL = "show:///url=";
 }
