 package collector.desktop.view.browser;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.i18n.DictKeys;
 import collector.desktop.controller.i18n.Translator;
 import collector.desktop.model.album.AlbumItemPicture;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 import collector.desktop.view.UIConstants;
 import collector.desktop.view.composites.StatusBarComposite;
 
 public class PictureViewCreator {
 	private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewCreator.class);
 	
 	static void showPicture(long albumItemId) {
 		StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));
 
 		StringBuilder picturePage = new StringBuilder();
 		try {
 			List<AlbumItemPicture> pictures = DatabaseOperations.getAlbumItemPictures(ApplicationUI.getSelectedAlbum(), albumItemId);
 			
 			String originalPathToPicture = "";
 			if (pictures.size() >= 1) {
 				originalPathToPicture = pictures.get(0).getOriginalPicturePath();
 			}
 	
 			StringBuilder smallPictures = new StringBuilder();
 			if (pictures.size() >= 2) {
 				int counter = 1;
 	
 				for (AlbumItemPicture picture : pictures) {
					String escapedPicturePath = Utilities.escapeBackslashesInFilePath(picture.getOriginalPicturePath());
 					smallPictures.append(
							"<a onMouseover='change(\"bigimg\", \"" + escapedPicturePath + "\")'>" + 
 									"  <img border=\"1\" " +
 									"       onMouseOver='this.style.cursor=\"pointer\"' " +
 									"       id=\"smallimage" + counter + "\" " +
 									"		style=\"width:120px; margin-top:10px;\"" +
 									"       src=\"" + picture.getThumbnailPicturePath() + "\">" +
 									"</a>" +
 							"</br>");
 
 					counter++;
 				}
 	
 				smallPictures.append("<br>" +
 						             "<form>" +
 						             "  <input type='button' " +
 						             "         onclick=\"parent.location.href='show:///lastPage'\" " +
 						             "         value='Go Back'>" +
 						             "</form>");
 			}
 	
 			picturePage.append(
 					"<html>" +
 	                "  <head>" +
 					"      <meta " + UIConstants.META_PARAMS + ">" + 
 					"      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
 					"      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
 			        "  </head>" +
 			        "  <body>" +
 			        "    <table>" +
 			        "      <tr>" +
 			        "        <td align=\"center\" valign=\"top\">" +
 			                   smallPictures.toString() +		
 			        "        </td>" +
 			        "        <td align=\"left\" valign=\"top\">" +
 			        "          <img style=\"max-width: 100%; max-height: 100%;\" " +
 			        "               id=\"bigimg\" src=\"" + originalPathToPicture + "\" " +
 			        "               onMouseOver=\"changeCursorToHand('bigimg')\" " +
 			        "               onclick=\"parent.location.href='show:///lastPage'\">" +
 			        "        </td>" +
 			        "      </tr>" +
 			        "    </table>" +
 			        "  </body>" +
 			        "</html>");
 	
 			ApplicationUI.getAlbumItemBrowser().setText(picturePage.toString());
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
 					ApplicationUI.getSelectedAlbum(), ex);
 		}
 	}
 }
