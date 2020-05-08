 package collector.desktop.view.browser;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.controller.i18n.DictKeys;
 import collector.desktop.controller.i18n.Translator;
 import collector.desktop.controller.settings.ApplicationSettingsManager;
 import collector.desktop.model.album.AlbumItem;
 import collector.desktop.model.album.AlbumItemPicture;
 import collector.desktop.model.album.FieldType;
 import collector.desktop.model.album.ItemField;
 import collector.desktop.model.album.OptionType;
 import collector.desktop.model.album.StarRating;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseConstants;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 
 public class ItemCreator {
 	private final static Logger LOGGER = LoggerFactory.getLogger(ItemCreator.class);
 	
 	static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
 		return getAlbumItemTableRowHtml(albumItem, true);
 	}
 	
 	static String getAlbumItemTableRowHtml(AlbumItem albumItem, boolean showUpdateAndRemoveButtons) {
 		StringBuilder htmlDataColumnContent = new StringBuilder();
 		StringBuilder htmlPictureColumnContent = new StringBuilder();
 		StringBuilder albumItemTableRowHtml = new StringBuilder();
 		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, showUpdateAndRemoveButtons);
 
 		return albumItemTableRowHtml.toString();
 	}
 
 	static String getAlbumItemDivContainerHtml(AlbumItem albumItem) {
 		StringBuilder htmlBuilder = new StringBuilder();
 		addAlbumItemDivContainer(albumItem, htmlBuilder);
 
 		return htmlBuilder.toString();
 	}
 
 	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
 		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
 	}
 	
 	private static String getThumbnailForFirstPicture(AlbumItem albumItem) {
 		return ((albumItem.getFirstPicture() != null) ? albumItem.getFirstPicture().getThumbnailPicturePath() : FileSystemAccessWrapper.PLACEHOLDERIMAGE);
 	}
 	
 	static void addAlbumItemDivContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
 		htmlBuilder.append(
 				"<div id=\"imageId" + albumItem.getItemID() + "\" " +
 				"    class=\"pictureContainer\" " +
 				"    onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" " +
 				"    onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">" +
                 "  <div class=\"innerPictureContainer\">" +
 		        "    <img src=\"" + getThumbnailForFirstPicture(albumItem) + "\">" +
                 "  </div>" +
                 "</div>");
 	}
 	
 	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, 
 			StringBuilder albumItemTableRowHtml, boolean showUpdateAndRemoveButtons) {
 		
 		// the id of the current album item
 		long id = -1;	
 		
 		for (ItemField fieldItem : albumItem.getFields()) {			
 			if (fieldItem.getType().equals(FieldType.UUID)) {
 				// schema or content version UUID --> ignore 
 			} else if (fieldItem.getType().equals(FieldType.ID)) {
 				if (fieldItem.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
 					// do not show, but store id
 					id = fieldItem.getValue();
 				}
 			} else if (fieldItem.getType().equals(FieldType.Option)) {
 				if (fieldItem.getValue() == OptionType.YES) {
 					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_YES)));
 				} else if (fieldItem.getValue() == OptionType.NO) {
 					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_NO)));
 				} else if (fieldItem.getValue() == OptionType.UNKNOWN) {
 					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_UNKNOWN)));
 				}
 			} else if (fieldItem.getType().equals(FieldType.Date)) {
 				java.sql.Date sqlDate = fieldItem.getValue();
 				java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
 
 				SimpleDateFormat dateFormater = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, ApplicationSettingsManager.getUserDefinedLocale());
 				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), dateFormater.format(utilDate)));
 			} else  if (fieldItem.getType().equals(FieldType.Text)) {
 				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Utilities.escapeHtmlString((String) fieldItem.getValue())));
 			} else if (fieldItem.getType().equals(FieldType.Integer)) {
 				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Integer) fieldItem.getValue()).toString()));
 			} else if (fieldItem.getType().equals(FieldType.Decimal)) {
 				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Double) fieldItem.getValue()).toString()));
 			} else if (fieldItem.getType().equals(FieldType.StarRating)) {
 				htmlDataColumnContent.append(getFieldNameAndStars(fieldItem.getName(), (StarRating) fieldItem.getValue()));
 			} else if (fieldItem.getType().equals(FieldType.URL)) {
 				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((String) fieldItem.getValue())));
 			}
 		}
 				
 		try {
 			List<AlbumItemPicture> pictures = albumItem.getPictures();
 			if (DatabaseOperations.isPictureAlbum(ApplicationUI.getSelectedAlbum()) || !pictures.isEmpty()) {
 				
 				htmlPictureColumnContent.append(
 						"<table border=0>" +
 						"  <tr>" +
 						"    <td align=center width=200 height=200>" +
 								getMainPictureHtml(id, pictures) +
 						"    </td>" +
 						"  </tr>" +
 						"  <tr>" +
 						"    <td>" +
 						"      <div style=\"max-width:200px;\">" +
 								getAlternativePicturesHtml(id, pictures) +
 						"      </div>" + 
 						"    </td>" +
 						"  </tr>" + 
 						"</table>");
 			}
 		} catch (DatabaseWrapperOperationException ex) {
 			LOGGER.error("An issue regarding the album item picture occured", ex);
 		}
 
 		if (showUpdateAndRemoveButtons) {
 			htmlDataColumnContent.append(getUpdateRemoveButtonsHtml(id));
 		}	
 
 		
 		// set the picture column with only if there is also a picture
 		String widthParameter = "";
 		if (htmlPictureColumnContent.length() != 0) {
 			widthParameter = "width=200px";
 		}
 		
 		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\">" +
 				                     "  <td " + widthParameter + ">" + htmlPictureColumnContent + "</td>" +
 				                     "    <td bgcolor=" + Utilities.getBackgroundColorOfWidgetInHex() + ">" + 
 				                     "       <div style=\"margin:20px\">" + htmlDataColumnContent + "</div>" + 
 				                     "    </td>" +
 				                     "  </tr>" +
 				                     "  <tr>" +
 				                     "    <td height=\"20\" colspan=\"2\">" +
 				                     "  </td>" +
 				                     "</tr>");		
 	}
 	
 	private static String getUpdateRemoveButtonsHtml(long id) {
 		return "<form style=\"margin-top:10px\">" +
 		       "  <input type=\"button\" " +
 			   "         onclick=parent.location.href=\"show:///updateComposite=" + id + "\" " +
 			   "         value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">" +
 		       "  <input type=\"button\" " +
 			   "    onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" " +
 		       "    value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">" +
 		       "</form>";
 	}
 	
 	private static String getFieldNameAndValueLine(String fieldName, String value) {
 		return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span> : " + value + "<br>"; 
 	}
 	
 	private static String getFieldNameAndStars(String fieldName, StarRating rating) {
 		if (rating.equals(StarRating.OneStar)) {
 			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.ONE_STAR_IMAGE + "><br>";
 		} else if (rating.equals(StarRating.TwoStars)) {
 			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.TWO_STARS_IMAGE + "><br>";
 		} else if (rating.equals(StarRating.ThreeStars)) {
 			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.THREE_STARS_IMAGE + "><br>";
 		} else if (rating.equals(StarRating.FourStars)) {
 			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.FOUR_STARS_IMAGE + "><br>";
 		} else if (rating.equals(StarRating.FiveStars)) {
 			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.FIVE_STARS_IMAGE + "><br>";
 		}
 		
 		return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemAccessWrapper.ZERO_STARS_IMAGE + "><br>";
 	}
 	
 	private static String getAlternativePicturesHtml(long id, List<AlbumItemPicture> pictures) {
 		StringBuilder htmlBuilder = new StringBuilder();
 		
 		if (pictures.size() > 1) {
 			for(AlbumItemPicture picture : pictures) {
 				htmlBuilder.append(
 					"<div align=center style=\"display:inline; min-width:40px; width:auto; width:40px\">" +
 					"  <a onClick=showBigPicture(\"imageId" + id + "\") " +
					"     onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + picture.getThumbnailPicturePath() + "&quot;)\">" +
				    "    <img onMouseOver=this.style.cursor=\"pointer\" style=\"max-width:40px; max-height:40px;\" src=\"" + picture.getThumbnailPicturePath() + "\">" +
 				    "  </a>" +
 				    "</div>");
 			}
 		}
 		
 		return htmlBuilder.toString();
 	}
 
 	private static String getMainPictureHtml(long id, List<AlbumItemPicture> albumItemPictures) {
 		// Initialize with placeholder
 		String mainPictureHtml = "<img id=\"imageId" + id + "\" " +
 				 				 "     style=\"max-width:195px; max-height:195px;\"" +
 				 				 "     src=\"" + FileSystemAccessWrapper.PLACEHOLDERIMAGE + "\">";
 		
 		// Use primary image if available
 		if (!albumItemPictures.isEmpty()) {
 			mainPictureHtml = "<img id=\"imageId" + id + "\" " +
 							  "     style=\"max-width:195px; " +
 							  "     max-height:195px;\" " +
 							  "     src=\"" + albumItemPictures.get(0).getThumbnailPicturePath() + "\" " +
 							  "     onMouseOver=changeCursorToHand(\"imageId" + id + "\") " +
 							  "     onClick=showBigPicture(\"imageId" + id + "\")>";
 		}
 		
 		return mainPictureHtml;
 	}
 }
