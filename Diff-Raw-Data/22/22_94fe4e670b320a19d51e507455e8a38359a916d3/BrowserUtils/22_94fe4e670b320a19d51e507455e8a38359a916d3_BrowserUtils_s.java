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
 
 package org.sammelbox.view.browser;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Display;
 import org.sammelbox.controller.GuiController;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 import org.sammelbox.model.album.AlbumItemResultSet;
 import org.sammelbox.model.album.AlbumItemStore;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.SammelView;
 import org.sammelbox.view.UIConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class BrowserUtils {
 	public static final String PROJECT_WEBSITE = "http://www.sammelbox.org";
 	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserUtils.class);
 		
 	/** The anchor to which a jump is performed as soon as the page is fully loaded. 
 	 * This field is used via the set and get methods by the browser progress listener */
 	private static String futureJumpAnchor = UIConstants.NO_ANCHOR_DEFINED;
 	/** The background color of the application widgets in html hex */
 	private static String backgroundColorOfWidgetInHex = null;
 	/** The default system font */
 	private static String defaultSystemFont = null;
 	
 	private BrowserUtils() {
 	}
 	
 	static String getAnchorForAlbumItemId(long albumItemId) {
 		return "albumId" + albumItemId;
 	}
 
 	/** To avoid rebuilding the exact same HTML output when returning from the picture viewer 
 	 * to the album view, the last generated HTML output is saved  */
 	private static String lastPageAsHtml;
 
 	static void performBrowserQueryAndShow(Browser browser, String sqlQuery) {				
 		try {
 			AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(sqlQuery));
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An error occured while reinitializing the album item store using the following SQL query (" + sqlQuery + ")", ex);
 		}
 		showAlbum(browser);
 	}
 
 	static void showResultSet(Browser browser, AlbumItemResultSet albumItemResultSet) {
 		try {
 			AlbumItemStore.reinitializeStore(albumItemResultSet);
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("Could not reinitialize album item store", ex);
 		}
 		showAlbum(browser);
 	}
 
 	/** Use this method to set a "future-jump-anchor" to a specific item
 	 * This id will be used to jump to an item after the content
 	 * of the HTML document changed and is completely loaded */
 	static void setFutureJumpAnchor(String futureJumpAnchor) {
 		BrowserUtils.futureJumpAnchor = futureJumpAnchor;
 	}
 
 	/** The browser progress listener uses this method to jump to a previously
 	 * provided anchor, as soon as the HTML document is completely loaded */
 	static String getFutureJumpAnchor() {
 		return BrowserUtils.futureJumpAnchor;
 	}
 
 	/** Attention: This method must only be used to jump to an anchor (id) if the currently shown
 	 * HTML document is fully loaded. (Due to asynchronous creation) If it is necessary 
 	 * to jump to a specific item after creating a new HTML document, the setFutureJumpAnchor 
 	 * must be used. By using this method the browser progress listener will execute the
 	 * jump as soon as the document is completely loaded */
 	static void jumpToAnchor(String anchor) {
 		if (!anchor.equals(UIConstants.NO_ANCHOR_DEFINED)) {
 			String javaScriptScrollToSnippet = "document.getElementById(\"" + anchor + "\").scrollIntoView(true)";
 			ApplicationUI.getAlbumItemBrowser().execute(javaScriptScrollToSnippet);
 		}
 	}
 
 	static void returnFromImageViewer() {
 		if (lastPageAsHtml != null) {
 			ApplicationUI.getAlbumItemBrowser().setText(lastPageAsHtml);
 		}
 	}
 
 	static void showAlbum(Browser browser) {
 		SammelView currentView = GuiController.getGuiState().getSammelView();
 		
 		if (SammelView.DETAILED_VIEW.equals(currentView)) {
 			DetailedViewCreator.showDetailedAlbum(browser);
 		} else if (SammelView.GALLERY_VIEW.equals(currentView)) {
 			GalleryViewCreator.showOverviewAlbum(browser);
 		} else if (SammelView.SPREADSHEET_VIEW.equals(currentView)) {
			// TODO link spreadsheet view here
 		}
 	}
 	
 	static void loadHtmlPage(Browser browser, InputStream fileInputStream) {
 		browser.setText(FileSystemAccessWrapper.readInputStreamIntoString(fileInputStream));
 	}
 	
 	static Map<String, String> getInitializedContentMap() {
 		Map<String, String> contentMap = new HashMap<>();
 		contentMap.put("EFFECT_JS", FileSystemLocations.getEffectsJS());
 		contentMap.put("STYLE_CSS", FileSystemLocations.getStyleCSS());
 		
 		return contentMap;
 	}
 	
 	static void fillAndLoadTemplate(Browser browser, String htmlTemplateFilename, Map<String, String> templateContent) {
 		String htmlString = FileSystemAccessWrapper.readInputStreamIntoString(
 			ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("templates/" + htmlTemplateFilename));
 		
 		for (Map.Entry<String, String> mapEntry : templateContent.entrySet()) {
 			htmlString = htmlString.replace("<!--" + mapEntry.getKey() + "-->", mapEntry.getValue());
 		}
 		
 		browser.setText(htmlString);
 	}
 	
 	static String getBackgroundColorOfWidgetInHex() {
 		if (backgroundColorOfWidgetInHex == null) { 
 			backgroundColorOfWidgetInHex = 
 				Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRed()))
 			  + Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getGreen()))
 			  + Integer.toHexString((Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getBlue()));
 		}
 		
 		return backgroundColorOfWidgetInHex;
 	}
 
 	public static String getDefaultSystemFont() {
 		if (defaultSystemFont == null) {
 			defaultSystemFont = Display.getCurrent().getSystemFont().getFontData()[0].getName();
 		}
 		
 		return defaultSystemFont;
 	}
 	
 	static String escapeHtmlString(String htmlString) {
 		if (htmlString == null) {
 			return "";
 		}		
 		
 		String escapedString = htmlString;
 		
 		escapedString = escapedString.replace("&", "&amp;");
 		escapedString = escapedString.replace("<", "&lt;");
 		escapedString = escapedString.replace(">", "&gt;");
 
 		return escapedString;
 	}
 
 	static void setLastPageAsHtml(String lastShownContentAsHtml) {
 		BrowserUtils.lastPageAsHtml = lastShownContentAsHtml;
 	}
 	
 	public static String getLastPageAsHtml() {
 		return lastPageAsHtml;
 	}
 	
 	/**
 	 * Escapes backslashes in a file path to make it ready to use as a string parameter for a javascript method.
 	 * @param filePath A file path that contains backslashes as separators which is an escape sequence in javascript and
 	 * therefore not suitable to use as method parameter.
 	 * @return The escaped file path string.
 	 */
 	public static String escapeBackslashesInFilePath(String filePath) {
 		return filePath.replaceAll("\\\\", "\\\\\\\\");	
 	}
 }
