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
 
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class parses wiki links of the form <code>[[Topic to Link To|Link Text]]</code>.
  */
 public class WikiLinkTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiLinkTag.class.getName());
 	// temporary parameter passed to indicate that the fragment being parsed is a link caption
 	protected static final String LINK_CAPTION = "link-caption";
 
 	/**
 	 * Parse a Mediawiki link of the form "[[topic|text]]" and return the
 	 * resulting HTML output.
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		boolean containsNestedLinks = (args.length > 0 && StringUtils.equals(args[0].toString(), "nested"));
 		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(lexer.getParserInput(), raw);
 		if (StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
 			// no destination or section
 			return raw;
 		}
 		if (containsNestedLinks) {
 			// if there is a nested link it must be an image, otherwise the syntax is invalid.
 			if (wikiLink.getColon() || !wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 				int start = raw.indexOf("[[");
 				int end = raw.lastIndexOf("]]");
 				String content = raw.substring(start + "[[".length(), end);
 				return "[[" + JFlexParserUtil.parseFragment(lexer.getParserInput(), content, lexer.getMode()) + "]]";
 			}
 		}
 		raw = this.processLinkMetadata(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw, wikiLink);
 		if (lexer.getMode() <= JFlexParser.MODE_PREPROCESS) {
 			// do not parse to HTML when in preprocess mode
 			return raw;
 		}
 		if (!wikiLink.getColon() && wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
 			// parse as an image
 			return lexer.parse(JFlexLexer.TAG_TYPE_IMAGE_LINK, raw);
 		}
 		try {
 			if (!StringUtils.isBlank(wikiLink.getInterWiki())) {
 				// inter-wiki link
 				return LinkUtil.interWiki(wikiLink);
 			}
 			String virtualWiki = lexer.getParserInput().getVirtualWiki();
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
 				// pass a parameter via the parserInput to prevent nested links from being generated
 				lexer.getParserInput().getTempParams().put(LINK_CAPTION, true);
 				wikiLink.setText(JFlexParserUtil.parseFragment(lexer.getParserInput(), wikiLink.getText(), lexer.getMode()));
 				lexer.getParserInput().getTempParams().remove(LINK_CAPTION);
 			}
 			if (StringUtils.equals(wikiLink.getDestination(), lexer.getParserInput().getTopicName()) && StringUtils.equals(virtualWiki, lexer.getParserInput().getVirtualWiki()) && StringUtils.isBlank(wikiLink.getSection())) {
 				// same page, bold the text and return
 				return "<b>" + (StringUtils.isBlank(wikiLink.getText()) ? wikiLink.getDestination() : wikiLink.getText()) + "</b>";
 			}
 			// do not escape text html - already done by parser
 			return LinkUtil.buildInternalLinkHtml(lexer.getParserInput().getContext(), virtualWiki, wikiLink, wikiLink.getText(), null, null, false);
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
 	private String processLinkMetadata(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw, WikiLink wikiLink) throws ParserException {
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
