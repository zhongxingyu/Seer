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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.InterWikiHandler;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class parses wiki links of the form <code>[[Topic to Link To|Link Text]]</code>.
  */
 public class WikiLinkTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiLinkTag.class.getName());
 	// look for image size info in image tags
 	private static Pattern IMAGE_SIZE_PATTERN = Pattern.compile("([0-9]+)[ ]*px", Pattern.CASE_INSENSITIVE);
 	// FIXME - make configurable
 	private static final int DEFAULT_THUMBNAIL_SIZE = 180;
 
 	/**
 	 *
 	 */
 	private String buildInternalLinkUrl(ParserInput parserInput, int mode, String raw) throws ParserException {
 		String context = parserInput.getContext();
 		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(parserInput, raw);
 		if (wikiLink == null) {
 			// invalid link
 			return raw;
 		}
 		if (StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
 			// invalid topic
 			return raw;
 		}
 		try {
 			if (!wikiLink.getColon() && wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 				// parse as an image
 				return this.parseImageLink(parserInput, mode, wikiLink);
 			}
 			if (!StringUtils.isBlank(wikiLink.getInterWiki())) {
 				// inter-wiki link
 				return LinkUtil.interWiki(wikiLink);
 			}
 			String virtualWiki = parserInput.getVirtualWiki();
 			if (wikiLink.getVirtualWiki() != null) {
 				// link to another virtual wiki
 				virtualWiki = wikiLink.getVirtualWiki().getName();
 			}
 			if (StringUtils.isBlank(wikiLink.getText()) && !StringUtils.isBlank(wikiLink.getDestination())) {
 				wikiLink.setText(wikiLink.getDestination());
 				if (!StringUtils.isBlank(wikiLink.getSection())) {
 					wikiLink.setText(wikiLink.getText() + "#" + Utilities.decodeAndEscapeTopicName(wikiLink.getSection(), true));
 				}
 			} else if (StringUtils.isBlank(wikiLink.getText()) && !StringUtils.isBlank(wikiLink.getSection())) {
 				wikiLink.setText(Utilities.decodeAndEscapeTopicName("#" + wikiLink.getSection(), true));
 			} else {
 				wikiLink.setText(JFlexParserUtil.parseFragment(parserInput, wikiLink.getText(), mode));
 			}
 			if (StringUtils.equals(wikiLink.getDestination(), parserInput.getTopicName()) && StringUtils.equals(virtualWiki, parserInput.getVirtualWiki())) {
 				// same page, bold the text and return
 				return "<b>" + (StringUtils.isBlank(wikiLink.getText()) ? wikiLink.getDestination() : wikiLink.getText()) + "</b>";
 			}
 			// do not escape text html - already done by parser
 			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, wikiLink, wikiLink.getText(), null, null, false);
 		} catch (DataAccessException e) {
 			logger.severe("Failure while parsing link " + raw, e);
 			return "";
 		} catch (ParserException e) {
 			logger.severe("Failure while parsing link " + raw, e);
 			return "";
 		}
 	}
 
 	/**
 	 * Parse a Mediawiki link of the form "[[topic|text]]" and return the
 	 * resulting HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		raw = this.processLinkMetadata(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw);
 		if (lexer.getMode() <= JFlexParser.MODE_PREPROCESS) {
 			// do not parse to HTML when in preprocess mode
 			return raw;
 		}
 		return this.processLinkContent(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw);
 	}
 
 	/**
 	 *
 	 */
 	private String parseImageLink(ParserInput parserInput, int mode, WikiLink wikiLink) throws DataAccessException, ParserException {
 		String context = parserInput.getContext();
 		String virtualWiki = parserInput.getVirtualWiki();
 		boolean thumb = false;
 		boolean frame = false;
 		String caption = null;
 		String align = null;
 		int maxDimension = -1;
 		if (!StringUtils.isBlank(wikiLink.getText())) {
 			String[] tokens = wikiLink.getText().split("\\|");
 			for (int i = 0; i < tokens.length; i++) {
 				String token = tokens[i];
 				if (StringUtils.isBlank(token)) {
 					continue;
 				}
 				if (token.equalsIgnoreCase("noframe")) {
 					frame = false;
 				} else if (token.equalsIgnoreCase("frame")) {
 					frame = true;
 				} else if (token.equalsIgnoreCase("thumb")) {
 					thumb = true;
 				} else if (token.equalsIgnoreCase("right")) {
 					align = "right";
 				} else if (token.equalsIgnoreCase("left")) {
 					align = "left";
 				} else if (token.equalsIgnoreCase("center")) {
 					align = "center";
 				} else {
 					Matcher m = IMAGE_SIZE_PATTERN.matcher(token);
 					if (m.find()) {
 						maxDimension = Integer.valueOf(m.group(1));
 					} else {
 						// FIXME - this is a hack.  images may contain piped links, so if
 						// there was previous caption info append the new info.
 						if (StringUtils.isBlank(caption)) {
 							caption = token;
 						} else {
 							caption += "|" + token;
 						}
 					}
 				}
 			}
 			if (thumb && maxDimension <= 0) {
 				maxDimension = DEFAULT_THUMBNAIL_SIZE;
 			}
 			caption = JFlexParserUtil.parseFragment(parserInput, caption, mode);
 		}
 		// do not escape html for caption since parser does it above
 		try {
 			return LinkUtil.buildImageLinkHtml(context, virtualWiki, wikiLink.getDestination(), frame, thumb, align, caption, maxDimension, false, null, false);
 		} catch (IOException e) {
 			throw new ParserException("I/O Failure while parsing image link", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String processLinkContent(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws ParserException {
 		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(parserInput, raw);
 		if (StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
 			// no destination or section
 			return raw;
 		}
 		return this.buildInternalLinkUrl(parserInput, mode, raw);
 	}
 
 	/**
 	 *
 	 */
 	private String processLinkMetadata(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws ParserException {
 		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(parserInput, raw);
 		if (StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
 			return raw;
 		}
 		String result = raw;
 		if (!wikiLink.getColon() && wikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)) {
 			String sortKey = wikiLink.getText();
 			if (!StringUtils.isBlank(sortKey)) {
 				sortKey = JFlexParserUtil.parseFragment(parserInput, sortKey, JFlexParser.MODE_PREPROCESS);
 			}
 			parserOutput.addCategory(wikiLink.getDestination(), sortKey);
 			if (mode > JFlexParser.MODE_MINIMAL) {
 				// keep the category around in minimal parsing mode, otherwise suppress it from the output
 				result = "";
 			}
 		}
 		if (!StringUtils.isBlank(wikiLink.getDestination())) {
 			parserOutput.addLink(wikiLink.getDestination());
 		}
 		return result;
 	}
 }
