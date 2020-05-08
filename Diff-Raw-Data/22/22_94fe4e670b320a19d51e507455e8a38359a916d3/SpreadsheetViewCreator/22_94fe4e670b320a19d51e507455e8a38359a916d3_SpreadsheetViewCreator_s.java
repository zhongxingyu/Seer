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
 
 import org.eclipse.swt.browser.Browser;
 import org.sammelbox.controller.GuiController;
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.model.album.AlbumItem;
 import org.sammelbox.model.album.AlbumItemStore;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.UIConstants;
 
 public final class SpreadsheetViewCreator {	
 	private SpreadsheetViewCreator() {
 	}
 	
	public static void showDetailedAlbum(Browser browser) {
 		// Exit if no album is selected
 		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
 			return;
 		}
 		
 		// Builders for efficient html creation
 		StringBuilder htmlSpreadsheet = new StringBuilder();
 		StringBuilder htmlSpreadsheetHeader = new StringBuilder();
 		StringBuilder htmlSpreadsheetData = new StringBuilder();
 		StringBuilder htmlSpreadsheetRow = new StringBuilder();
 		StringBuilder htmlSpreadsheetFooter = new StringBuilder();
 
 		// Create the header of the spreadsheet
		if(AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex()).size() > 0) {
			SpreadsheetItemCreator.createSpreadsheetHeader(htmlSpreadsheetHeader, AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex()).get(0));
 		}
 		
 		// Add all available album items
 		boolean hasEvenCountingInList = false;
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
 			htmlSpreadsheetRow.delete(0, htmlSpreadsheetRow.length());
 			SpreadsheetItemCreator.createNextDataRow(albumItem, htmlSpreadsheetData, htmlSpreadsheetRow, hasEvenCountingInList);
 			hasEvenCountingInList = !hasEvenCountingInList;
 		}
 
 		// Create the footer of the spreadsheet
		if(AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex()).size() > 0) {
			SpreadsheetItemCreator.createSpreadsheetFooter(htmlSpreadsheetFooter, AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex()).get(0));
 		}
 		
 		// If no album items have been found
 		if (htmlSpreadsheetData.length() == 0) {
 			htmlSpreadsheet.append(
 	          "<tr><td><div>" + 
 	            "<h3>" + 
 	              Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, GuiController.getGuiState().getSelectedAlbum()) + 
 	            "</h3>" + 
 	            "<p>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + "</p>" +
 	          "</div></td></tr>"); 
 		} else {
 			htmlSpreadsheet.append("<table id=\"spreadsheetTable\">");
 			htmlSpreadsheet.append(htmlSpreadsheetHeader);
 			htmlSpreadsheet.append(htmlSpreadsheetData);
 			htmlSpreadsheet.append(htmlSpreadsheetFooter);
 			htmlSpreadsheet.append("</table>");
 			htmlSpreadsheet.append("</br>");
 			htmlSpreadsheet.append("<button type=\"button\">" + Translator.toBeTranslated("Änderungen Sichern") + "</button> ");
 			htmlSpreadsheet.append("</br></br>");
 			htmlSpreadsheet.append("<div id=\"showModify\" class=\"smallLabel dirty\">To be modified <span id=\"modifyCount\">0</span></div> ");
 			htmlSpreadsheet.append("<div id=\"showAdd\" class=\"smallLabel new\">To be added <span id=\"addCount\">0</span></div> ");
 			htmlSpreadsheet.append("<div id=\"showDelete\" class=\"smallLabel delete\">To be deleted <span id=\"deleteCount\">0</span></div> ");
 		}
 		
 		// Build header using album name. Include view name if appropriated
 		String collectionHeader = GuiController.getGuiState().getSelectedAlbum();
 		if (GuiController.getGuiState().isViewSelected()) {
 			collectionHeader += " - " + GuiController.getGuiState().getSelectedSavedSearch();
 		}
 		
 		// Create final page html
 		String finalPageAsHtml = 
 				"<!DOCTYPE HTML>" +
 				"<html>" +
 				  "<head>" +
 				    "<title>sammelbox.org</title>" +
 				    "<meta " + UIConstants.META_PARAMS + ">" + 
 				    "<link rel=\"stylesheet\" href=\"" + UIConstants.STYLE_CSS_SPREADSHEET + "\" />" +
 				    "<script src=\"" + UIConstants.SPREADSHEETSCRIPTS_JS + "\"></script>" +
 				  "</head>" +
 				  "<body>" +
 				    "<h2>" + collectionHeader + "</h2>" +
 				  	htmlSpreadsheet +
 				  "</body>" +
 				"</html>";
 				
 		browser.setText(finalPageAsHtml);		
 		BrowserUtils.setLastPageAsHtml(finalPageAsHtml);		
 	}
 }
