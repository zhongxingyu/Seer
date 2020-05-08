 package collector.desktop.view.browser;
 
 import java.io.InputStream;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.GuiController;
 import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.model.album.AlbumItem;
 import collector.desktop.model.album.AlbumItemResultSet;
 import collector.desktop.model.album.AlbumItemStore;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 
 public class Utilities {
 	private final static Logger LOGGER = LoggerFactory.getLogger(Utilities.class);
 	
 	/** The anchor to which a jump is performed as soon as the page is fully loaded. 
 	 * This field is used via the set and get methods by the browser progress listener */
 	private static String futureJumpAnchor = null;
 	/** The background color of the application widgets in html hex */
 	private static String backgroundColorOfWidgetInHex = null;
 	/** The default system font */
 	private static String defaultSystemFont = null;
 	
 	static String getAnchorForAlbumItemId(long albumItemId) {
 		return "albumId" + albumItemId;
 	}
 
 	/** To avoid rebuilding the exact same HTML output when returning from the picture viewer 
 	 * to the album view, the last generated HTML output is saved  */
 	static String lastPageAsHtml;
 
 	static void performBrowserQueryAndShow(Browser browser, String sqlQuery) {				
 		try {
 			AlbumItemStore.reinitializeStore(DatabaseOperations.executeSQLQuery(sqlQuery));
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An error occured while reinitializing the album item store using the following SQL query (" + sqlQuery + ")", ex);
 		}
 		showAlbum(browser);
 	}
 
 	static void performLastQuery(Browser browser) {
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
 		Utilities.futureJumpAnchor = futureJumpAnchor;
 	}
 
 	/** The browser progress listener uses this method to jump to a previously
 	 * provided anchor, as soon as the HTML document is completely loaded */
 	static String getFutureJumpAnchor() {
 		return Utilities.futureJumpAnchor;
 	}
 
 	/** Attention: This method must only be used to jump to an anchor (id) if the currently shown
 	 * HTML document is fully loaded. (Due to asynchronous creation) If it is necessary 
 	 * to jump to a specific item after creating a new HTML document, the setFutureJumpAnchor 
 	 * must be used. By using this method the browser progress listener will execute the
 	 * jump as soon as the document is completely loaded */
 	static void jumpToAnchor(String anchor) {
 		ApplicationUI.getAlbumItemBrowser().execute("window.location.hash=\"" + anchor + "\"");
 	}
 
 	static void goBackToLastPage() {
 		if (lastPageAsHtml != null) {
 			ApplicationUI.getAlbumItemBrowser().setText(lastPageAsHtml);
 		}
 	}
 
 	static void showAlbum(Browser browser) {
 		if (GuiController.getGuiState().isViewDetailed()) {
 			DetailedViewCreator.showDetailedAlbum(browser);
 		} else {
 			GalleryViewCreator.showOverviewAlbum(browser);
 		}
 	}
 	
 	static void loadHtmlPage(Browser browser, InputStream fileInputStream) {
 		browser.setText(FileSystemAccessWrapper.readInputStreamIntoString(fileInputStream));
 	}
 	
 	static void loadHtmlPage(Browser browser, String htmlString) {
 		browser.setText(htmlString);
 	}
 	
 	static void loadHelpPage() {
 		loadHtmlPage(
 				ApplicationUI.getAlbumItemBrowser(),
 				ApplicationUI.class.getClassLoader().getResourceAsStream("htmlfiles/help.html"));
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
 
 	static String getDefaultSystemFont() {
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
 	
 	static void addAdditionalAlbumItems() {
 		if (GuiController.getGuiState().isViewDetailed()) {
 			if (!AlbumItemStore.isStopIndexAtEnd()) {
 				StringBuilder rows = new StringBuilder();
 
 				AlbumItemStore.increaseStopIndex();
 				for (AlbumItem albumItem : (AlbumItemStore.getAlbumItemsInRange(
 						AlbumItemStore.getPreviousStopIndex() + 1, AlbumItemStore.getStopIndex())))
 				{
 					rows.append(ItemCreator.getAlbumItemTableRowHtml(albumItem));
 				}
 
 
 				String javascript = "var table = document.getElementById('albumItems'); " +
 									"var tbody = table.tBodies[0]; " +
 									"var temp = tbody.ownerDocument.createElement('div'); " +
 									"temp.innerHTML = '<table>' + tbody.innerHTML + '" + rows + "</table>'; " +
 									"tbody.parentNode.replaceChild(temp.firstChild.firstChild, tbody); ";
 
 				
 				// In JavaScript (as in Java), a backslash will escape the following character. 
 				// However, Windows uses the backslash also as the file separator character. For this reason 
 				// we have to escape possible backslashes by using the backslash character itself
 				ApplicationUI.getAlbumItemBrowser().execute(javascript.replace("\\", "\\\\"));
 			}
 		} else {
 			if (!AlbumItemStore.isStopIndexAtEnd()) {
 				StringBuilder divs = new StringBuilder();
 
 				AlbumItemStore.increaseStopIndex();
 				for (AlbumItem albumItem : (AlbumItemStore.getAlbumItemsInRange(
 						AlbumItemStore.getPreviousStopIndex() + 1, AlbumItemStore.getStopIndex())))
 				{					
 					divs.append(ItemCreator.getAlbumItemDivContainerHtml(albumItem));
 				}
 
 				String javascript = "var div = document.getElementById('albumItems');" +
 									"div.innerHTML = div.innerHTML + '" + divs + "';";
 
 				// In JavaScript (as in Java), a backslash will escape the following character. 
 				// However, Windows uses the backslash also as the file separator character. For this reason 
 				// we have to escape possible backslashes by using the backslash character itself
 				ApplicationUI.getAlbumItemBrowser().execute(javascript.replace("\\", "\\\\"));
 			}
 		}
 	}
 
 	static void setLastPageAsHtml(String lastShownContentAsHtml) {
 		Utilities.lastPageAsHtml = lastShownContentAsHtml;
 	}
	
	/**
	 * Escapes backslashed in a filepath for java(script)
	 * @param filePath A filepath that contains backslashes as separators which is an escape sequence in javascript
	 * @return The escaped string of the filepath.
	 */
	public static String escapeBackslashesInFilePath(String filePath) {
		return filePath.replaceAll("\\\\", "\\\\\\\\");	
	}
 }
