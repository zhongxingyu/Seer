 package collector.desktop.view.browser;
 
 import java.util.List;
 
 import org.eclipse.swt.browser.Browser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.model.album.AlbumItem;
 import collector.desktop.model.album.AlbumItemPicture;
 import collector.desktop.model.album.AlbumItemStore;
 import collector.desktop.model.album.FieldType;
 import collector.desktop.model.album.ItemField;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 import collector.desktop.view.UIConstants;
 
 public class GalleryViewCreator {
 	private final static Logger LOGGER = LoggerFactory.getLogger(GalleryViewCreator.class);
 	
 	static void showOverviewAlbum(Browser browser) {
 		StringBuilder galleryItemHtmlBuilder = new StringBuilder();
 		
 		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems(AlbumItemStore.getStopIndex())) {
 			String picturePath = "";
 			long id = 0;
 
 			for (ItemField fieldItem : albumItem.getFields()) {				
 				if (fieldItem.getType().equals(FieldType.UUID)) {
 					// schema or content version UUID --> ignore
 				} else if (fieldItem.getType().equals(FieldType.ID)) {
 					if (!fieldItem.getName().equals("typeinfo")) {
 						// do not show, but store id
 						id = fieldItem.getValue();
 					}
 				}
 			}		
 
 			List<AlbumItemPicture> pictures = null;
 			try {
 				pictures = DatabaseOperations.getAlbumItemPictures(ApplicationUI.getSelectedAlbum(), id);
 			} catch (DatabaseWrapperOperationException ex) {
 				LOGGER.error("An error occured while retrieving the pictures associated with the album item #'" + 
 					id + "' from the album '" + ApplicationUI.getSelectedAlbum() + "'", ex);
 			}
 			
 			if (pictures == null || pictures.isEmpty()) {
 				picturePath = FileSystemAccessWrapper.PLACEHOLDERIMAGE;
 			} else {
 				picturePath = pictures.get(0).getThumbnailPicturePath();
 			}
 			
 			galleryItemHtmlBuilder.append("<div id=\"imageId" + id + "\" " +
 					                      "     class=\"pictureContainer\" " +
 					                      "     onMouseOver=\"parent.location.href=&quot;show:///details=" + id + "&quot;\" " +
 					                      "		onClick=\"parent.location.href=&quot;show:///detailsComposite=" + id + "&quot;\">");
 			galleryItemHtmlBuilder.append("  <div class=\"innerPictureContainer\">");
 			galleryItemHtmlBuilder.append("    <img src=\"" + picturePath + "\">");
 			galleryItemHtmlBuilder.append("  </div>");
 			galleryItemHtmlBuilder.append("</div>");
 		}
 
 		String finalPageAsHtml = "<!DOCTYPE HTML>" +
 								 "  <html>" +
 								 "    <head>" +
 								 "      <title>sammelbox.org</title>" +
 								 "      <meta " + UIConstants.META_PARAMS + ">" + 
 								 "      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
 								 "      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
 								 "    </head>" +
 								 "    <body style=\"background-color:#ffffff;font-family:" +  Utilities.getDefaultSystemFont() + "\">" +
								        galleryItemHtmlBuilder.toString() +
 								 "    </body>";
 				
 		browser.setText(finalPageAsHtml);
 		Utilities.setLastPageAsHtml(finalPageAsHtml);
 	}
 }
