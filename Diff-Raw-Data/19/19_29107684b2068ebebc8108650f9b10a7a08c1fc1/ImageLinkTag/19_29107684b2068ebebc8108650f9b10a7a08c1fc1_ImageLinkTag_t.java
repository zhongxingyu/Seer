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
 import java.util.EnumSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.ImageBorderEnum;
 import org.jamwiki.utils.ImageHorizontalAlignmentEnum;
 import org.jamwiki.utils.ImageVerticalAlignmentEnum;
 import org.jamwiki.utils.ImageMetadata;
 import org.jamwiki.utils.ImageUtil;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class parses image links of the form <code>[[Image|frame|100px|Caption]]</code>.
  */
 public class ImageLinkTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ImageLinkTag.class.getName());
 	// look for image size info in image tags
 	private static Pattern IMAGE_SIZE_PATTERN = Pattern.compile("([0-9]+)[ ]*px", Pattern.CASE_INSENSITIVE);
 	// FIXME - make configurable
 	private static final int DEFAULT_THUMBNAIL_SIZE = 220;
 
 	/**
 	 * Parse a Mediawiki link of the form "[[topic|text]]" and return the
 	 * resulting HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		if (lexer.getMode() <= JFlexParser.MODE_PREPROCESS) {
 			// parse as a link if not doing a full parse
 			return lexer.parse(JFlexLexer.TAG_TYPE_WIKI_LINK, raw);
 		}
 		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(lexer.getParserInput(), raw);
 		if (StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
 			// no destination or section
 			return raw;
 		}
 		if (wikiLink.getColon() || !wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 			// parse as a link
 			return lexer.parse(JFlexLexer.TAG_TYPE_WIKI_LINK, raw);
 		}
 		try {
			String result = this.parseImageLink(lexer.getParserInput(), lexer.getMode(), wikiLink);
			// depending on image alignment/border the image may be contained within a div.  If that's the
			// case, close any open paragraph tags prior to pushing the image onto the stack
			if (result.startsWith("<div") && lexer.peekTag().getTagType().equals("p")) {
				lexer.popTag("p");
				StringBuilder tagContent = lexer.peekTag().getTagContent();
				String trimmedTagContent = tagContent.toString().trim();
				if (tagContent.length() != trimmedTagContent.length()) {
					// trim trailing whitespace
					tagContent.replace(0, tagContent.length() - 1, trimmedTagContent);
				}
				tagContent.append(result);
				lexer.pushTag("p", null);
				return "";
			} else {
				// otherwise just return the image HTML
				return result;
			}
 		} catch (DataAccessException e) {
 			logger.severe("Failure while parsing link " + raw, e);
 			return "";
 		} catch (ParserException e) {
 			logger.severe("Failure while parsing link " + raw, e);
 			return "";
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String parseImageLink(ParserInput parserInput, int mode, WikiLink wikiLink) throws DataAccessException, ParserException {
 		String context = parserInput.getContext();
 		String virtualWiki = parserInput.getVirtualWiki();
 		ImageMetadata imageMetadata = parseImageParams(wikiLink.getText());
 		if ((imageMetadata.getBorder() == ImageBorderEnum.THUMB || imageMetadata.getBorder() == ImageBorderEnum.FRAMELESS)&& imageMetadata.getMaxDimension() <= 0) {
 			imageMetadata.setMaxDimension(DEFAULT_THUMBNAIL_SIZE);
 		}
 		if (!StringUtils.isBlank(imageMetadata.getCaption())) {
 			imageMetadata.setCaption(JFlexParserUtil.parseFragment(parserInput, imageMetadata.getCaption(), mode));
 		}
 		// do not escape html for caption since parser does it above
 		try {
 			return ImageUtil.buildImageLinkHtml(context, virtualWiki, wikiLink.getDestination(), imageMetadata, null, false);
 		} catch (IOException e) {
 			throw new ParserException("I/O Failure while parsing image link", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private ImageMetadata parseImageParams(String paramText) {
 		ImageMetadata imageMetadata = new ImageMetadata();
 		if (StringUtils.isBlank(paramText)) {
 			return imageMetadata;
 		}
 		String[] tokens = paramText.split("\\|");
 		tokenLoop: for (int i = 0; i < tokens.length; i++) {
 			String token = tokens[i];
 			if (StringUtils.isBlank(token)) {
 				continue;
 			}
 			token = token.trim();
 			for (ImageBorderEnum border : EnumSet.allOf(ImageBorderEnum.class)) {
 				if (border.toString().equalsIgnoreCase(token)) {
 					imageMetadata.setBorder(border);
 					continue tokenLoop;
 				}
 			}
 			for (ImageHorizontalAlignmentEnum horizontalAlignment : EnumSet.allOf(ImageHorizontalAlignmentEnum.class)) {
 				if (horizontalAlignment.toString().equalsIgnoreCase(token)) {
 					imageMetadata.setHorizontalAlignment(horizontalAlignment);
 					continue tokenLoop;
 				}
 			}
 			for (ImageVerticalAlignmentEnum verticalAlignment : EnumSet.allOf(ImageVerticalAlignmentEnum.class)) {
 				if (verticalAlignment.toString().equalsIgnoreCase(token)) {
 					imageMetadata.setVerticalAlignment(verticalAlignment);
 					continue tokenLoop;
 				}
 			}
 			// if none of the above tokens matched then check for size or caption
 			Matcher m = IMAGE_SIZE_PATTERN.matcher(token);
 			if (m.find()) {
 				imageMetadata.setMaxDimension(Integer.valueOf(m.group(1)));
 			} else {
 				// FIXME - this is a hack.  images may contain piped links, so if
 				// there was previous caption info append the new info.
 				if (StringUtils.isBlank(imageMetadata.getCaption())) {
 					imageMetadata.setCaption(token);
 				} else {
 					imageMetadata.setCaption(imageMetadata.getCaption() + "|" + token);
 				}
 			}
 		}
 		return imageMetadata;
 	}
 }
