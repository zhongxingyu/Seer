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
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Handle <script> tags.
  */
 public class JavascriptTag implements JFlexParserTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JavascriptTag.class.getName());
 
 	/**
 	 * Parse a Mediawiki HTML link of the form "<script>...</script>".
 	 */
 	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
 		if (logger.isFinerEnabled()) logger.finer("javascript: " + raw + " (" + lexer.yystate() + ")");
 		if (StringUtils.isBlank(raw)) {
 			// no link to display
 			return raw;
 		}
 		return this.parseScriptTag(lexer.getParserInput(), raw, lexer.getMode());
 	}
 
 	/**
 	 *
 	 */
 	private String parseScriptTag(ParserInput parserInput, String raw, int mode) throws ParserException {
 		// get open <script> tag
 		int pos = raw.indexOf(">");
 		String openTag = raw.substring(0, pos + 1);
 		// get closing </script> tag
 		raw = raw.substring(pos + 1);
 		pos = raw.lastIndexOf("<");
 		String closeTag = raw.substring(pos);
 		raw = raw.substring(0, pos);
 		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
			if (mode >= JFlexParser.MODE_POSTPROCESS) {
				// if Javascript is disabled but a script tag is present during the
				// postprocessor parsing then it's highly likely someone is attempting
				// an XSS attack.
				logger.warning("Potential XSS attack detected from user " + parserInput.getUserDisplay() + ": " + raw);
			}
 			return StringEscapeUtils.escapeHtml(openTag) + JFlexParserUtil.parseFragment(parserInput, raw, mode) + StringEscapeUtils.escapeHtml(closeTag);
 		}
 		JFlexTagItem tag = new JFlexTagItem("script", openTag);
 		tag.getTagContent().append(raw);
 		return tag.toHtml();
 	}
 }
