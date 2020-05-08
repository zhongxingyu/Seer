 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser.jflex;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.ImageBorderEnum;
 import org.jamwiki.utils.ImageHorizontalAlignmentEnum;
 import org.jamwiki.utils.ImageMetadata;
 import org.jamwiki.utils.ImageUtil;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Handle image galleries of the form <gallery>...</gallery>.
  */
 public class GalleryTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(GalleryTag.class.getName());
 	private static final int DEFAULT_IMAGES_PER_ROW = 4;
 	private static final int DEFAULT_THUMBNAIL_MAX_DIMENSION = 120;
 
 	/**
 	 * Given a list of image links to display in the gallery, generate the
 	 * gallery HTML.
 	 */
 	private String generateGalleryHtml(ParserInput parserInput, List<WikiLink> imageLinks) {
 		if (imageLinks.isEmpty()) {
 			// empty gallery tag
 			return "";
 		}
 		int width = DEFAULT_THUMBNAIL_MAX_DIMENSION;
		int perRow = DEFAULT_IMAGES_PER_ROW;
 		String virtualWiki;
 		ImageMetadata imageMetadata = this.initializeImageMetadata();
 		int count = 0;
 		StringBuilder result = new StringBuilder("<table class=\"gallery\" cellspacing=\"0\" cellpadding=\"0\">\n<tr>\n");
 		for (WikiLink wikiLink : imageLinks) {
 			count++;
			if (count != 1 && count % perRow == 1) {
 				// new row
 				result.append("</tr>\n<tr>\n");
 			}
			result.append("<td>\n<div style=\"width:" + (width + 35) + "px;\" class=\"gallerybox\">\n");
 			virtualWiki = (wikiLink.getVirtualWiki() == null) ? parserInput.getVirtualWiki() : wikiLink.getVirtualWiki().getName();
 			imageMetadata.setAlt((StringUtils.isBlank(wikiLink.getText())) ? wikiLink.getArticle() : "");
 			try {
 				result.append(ImageUtil.buildImageLinkHtml(parserInput.getContext(), virtualWiki, wikiLink.getDestination(), imageMetadata, null, true)).append("\n");
 			} catch (DataAccessException e) {
 				// this should only happen if there is a database failure
 				logger.error("Data access exception while parsing gallery tag", e);
 			} catch (IOException e) {
 				logger.error("I/O exception while parsing gallery tag", e);
 			}
 			if (!StringUtils.isBlank(wikiLink.getText())) {
 				result.append("<div class=\"gallerytext\">\n<p>").append(wikiLink.getText()).append("</p>\n</div>\n");
 			}
			result.append("</div>\n</td>\n");
		}
		// add any blank columns that are necessary to fill out the last row
		if ((count % perRow) != 0) {
			for (int i = (perRow - (count % perRow)); i > 0; i--) {
				result.append("<td>&#160;</td>\n");
			}
 		}
 		result.append("</tr>\n</table>");
 		return result.toString();
 	}
 
 	/**
 	 * Process the contents of the gallery tag into a list of wiki link objects
 	 * for the images in the gallery.  This method also updates the topic metadata,
 	 * including any "link to" records, in the ParserOutput object.
 	 */
 	private List<WikiLink> generateImageLinks(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws ParserException {
 		// get the tag contents as a list of wiki syntax for image thumbnails
 		String content = JFlexParserUtil.tagContent(raw);
 		List<WikiLink> imageLinks = new ArrayList<WikiLink>();
 		if (!StringUtils.isBlank(content)) {
 			String[] lines = content.split("\n");
 			String imageLinkText;
 			WikiLink wikiLink;
 			for (String line : lines) {
 				imageLinkText = "[[" + line.trim() + "]]";
 				try {
 					wikiLink = JFlexParserUtil.parseWikiLink(parserInput, parserOutput, imageLinkText);
 				} catch (ParserException e) {
 					// failure while parsing, the user may have entered invalid text
 					logger.info("Invalid gallery entry " + line);
 					continue;
 				}
 				if (!wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 					// not an image
 					continue;
 				}
 				// store image link as parser output metadata
 				parserOutput.addLink(wikiLink.getDestination());
 				// parse the caption
 				if (!StringUtils.isBlank(wikiLink.getText())) {
 					wikiLink.setText(JFlexParserUtil.parseFragment(parserInput, parserOutput, wikiLink.getText(), mode));
 				}
 				imageLinks.add(wikiLink);
 			}
 		}
 		return imageLinks;
 	}
 
 	/**
 	 *
 	 */
 	private ImageMetadata initializeImageMetadata() {
 		ImageMetadata imageMetadata = new ImageMetadata();
 		imageMetadata.setMaxHeight(DEFAULT_THUMBNAIL_MAX_DIMENSION);
 		imageMetadata.setMaxWidth(DEFAULT_THUMBNAIL_MAX_DIMENSION);
 		imageMetadata.setBorder(ImageBorderEnum.THUMB);
 		imageMetadata.setHorizontalAlignment(ImageHorizontalAlignmentEnum.CENTER);
 		return imageMetadata;
 	}
 
 	/**
 	 * Parse a gallery tag of the form <gallery>...</gallery> and return the
 	 * resulting HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		// get the tag contents as a list of wiki syntax for image thumbnails.  this will also
 		// generate metadata for the links.
 		List<WikiLink> imageLinks = this.generateImageLinks(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw);
 		// if pre-processor mode then there is nothing more to do
 		if (lexer.getMode() <= JFlexParser.MODE_PREPROCESS) {
 			return raw;
 		}
 		// generate the gallery HTML
 		return this.generateGalleryHtml(lexer.getParserInput(), imageLinks);
 	}
 }
