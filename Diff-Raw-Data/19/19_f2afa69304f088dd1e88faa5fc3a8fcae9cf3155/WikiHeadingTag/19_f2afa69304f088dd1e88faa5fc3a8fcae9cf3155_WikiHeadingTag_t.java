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
 
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.TableOfContents;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * This class parses wiki headings of the form <code>==heading content==</code>.
  */
 public class WikiHeadingTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());
 
 	/**
 	 *
 	 */
 	private String buildSectionEditLink(ParserInput parserInput, int section) {
 		if (!parserInput.getAllowSectionEdit()) {
 			return "";
 		}
 		if (parserInput.getLocale() == null) {
 			logger.info("Unable to build section edit links for " + parserInput.getTopicName() + " - locale is empty");
 			return "";
 		}
 		// FIXME - template inclusion causes section edits to break, so disable for now
 		String inclusion = (String)parserInput.getTempParams().get(TemplateTag.TEMPLATE_INCLUSION);
 		boolean disallowInclusion = (inclusion != null && inclusion.equals("true"));
 		if (disallowInclusion) {
 			return "";
 		}
		String output = "<span class=\"editsection\">[";
 		String url = "";
 		try {
 			url = LinkUtil.buildEditLinkUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserInput.getTopicName(), null, section);
 		} catch (Exception e) {
 			logger.severe("Failure while building link for topic " + parserInput.getVirtualWiki() + " / " + parserInput.getTopicName(), e);
 		}
 		output += "<a href=\"" + url + "\">";
 		output += Utilities.formatMessage("common.sectionedit", parserInput.getLocale());
		output += "</a>]</span>";
 		return output;
 	}
 
 	/**
 	 * Parse a Mediawiki heading of the form "==heading==" and return the
 	 * resulting HTML output.
 	 */
 	public String parse(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) {
 		try {
 			int level = 0;
 			if (raw.startsWith("=====") && raw.endsWith("=====")) {
 				level = 5;
 			} else if (raw.startsWith("====") && raw.endsWith("====")) {
 				level = 4;
 			} else if (raw.startsWith("===") && raw.endsWith("===")) {
 				level = 3;
 			} else if (raw.startsWith("==") && raw.endsWith("==")) {
 				level = 2;
 			} else if (raw.startsWith("=") && raw.endsWith("=")) {
 				level = 1;
 			} else {
 				return raw;
 			}
 			String tagText = raw.substring(level, raw.length() - level).trim();
 			ParserInput tmpParserInput = new ParserInput(parserInput);
 			String tocText = JFlexParserUtil.parseFragment(tmpParserInput, tagText, JFlexParser.MODE_PROCESS);
 			tocText = Utilities.stripMarkup(tocText);
 			String tagName = parserInput.getTableOfContents().checkForUniqueName(tocText);
 			// re-convert any &uuml; or other (converted by the parser) entities back
 			tagName = StringEscapeUtils.unescapeHtml(tagName);
 			if (mode <= JFlexParser.MODE_SLICE) {
 				parserOutput.setSectionName(tagName);
 				return raw;
 			}
 			String output = this.updateToc(parserInput, tagName, tocText, level);
 			int nextSection = parserInput.getTableOfContents().size();
 			output += "<a name=\"" + Utilities.encodeAndEscapeTopicName(tagName) + "\"></a>";
 			output += "<h" + level + ">";
			output += this.buildSectionEditLink(parserInput, nextSection);
			output += "<span>" + JFlexParserUtil.parseFragment(parserInput, tagText, mode) + "</span>";
 			output += "</h" + level + ">";
 			return output.toString();
 		} catch (Throwable t) {
 			logger.info("Unable to parse " + raw, t);
 			return raw;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String updateToc(ParserInput parserInput, String name, String text, int level) {
 		String output = "";
 		if (parserInput.getTableOfContents().getStatus() == TableOfContents.STATUS_TOC_UNINITIALIZED) {
 			output += "__TOC__";
 		}
 		parserInput.getTableOfContents().addEntry(name, text, level);
 		return output;
 	}
 }
