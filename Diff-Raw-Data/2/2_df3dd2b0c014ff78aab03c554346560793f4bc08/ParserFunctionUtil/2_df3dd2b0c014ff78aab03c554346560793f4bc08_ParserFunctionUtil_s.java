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
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Vector;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Process parser functions.  See http://www.mediawiki.org/wiki/Help:Magic_words#Parser_functions.
  */
 public class ParserFunctionUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ParserFunctionUtil.class.getName());
 	private static final String PARSER_FUNCTION_ANCHOR_ENCODE = "anchorencode:";
 	private static final String PARSER_FUNCTION_FILE_PATH = "filepath:";
 	private static final String PARSER_FUNCTION_FULL_URL = "fullurl:";
 	private static final String PARSER_FUNCTION_LOCAL_URL = "localurl:";
 	private static final String PARSER_FUNCTION_URL_ENCODE = "urlencode:";
 	private static Vector PARSER_FUNCTIONS = new Vector();
 
 	static {
 		// parser functions
 		PARSER_FUNCTIONS.add(PARSER_FUNCTION_ANCHOR_ENCODE);
 		PARSER_FUNCTIONS.add(PARSER_FUNCTION_FILE_PATH);
 		PARSER_FUNCTIONS.add(PARSER_FUNCTION_FULL_URL);
 		PARSER_FUNCTIONS.add(PARSER_FUNCTION_LOCAL_URL);
 		PARSER_FUNCTIONS.add(PARSER_FUNCTION_URL_ENCODE);
 	}
 
 	/**
 	 * Determine if a template name corresponds to a parser function requiring
 	 * special handling.  See http://meta.wikimedia.org/wiki/Help:Magic_words
 	 * for a list of Mediawiki parser functions.  If the template name is a parser
 	 * function then return the parser function name and argument.
 	 */
 	protected static String[] parseParserFunctionInfo(String name) {
 		int pos = name.indexOf(":");
 		if (pos == -1 || (pos + 2) > name.length()) {
 			return null;
 		}
 		String parserFunction = name.substring(0, pos + 1).trim();
		String parserFunctionArguments = name.substring(pos + 2).trim();
 		if (!PARSER_FUNCTIONS.contains(parserFunction) || StringUtils.isBlank(parserFunctionArguments)) {
 			return null;
 		}
 		return new String[]{parserFunction, parserFunctionArguments};
 	}
 
 	/**
 	 * Process a parser function, returning the value corresponding to the parser
 	 * function result.  See http://meta.wikimedia.org/wiki/Help:Magic_words for a
 	 * list of Mediawiki parser functions.
 	 */
 	protected static String processParserFunction(ParserInput parserInput, String parserFunction, String parserFunctionArguments) throws Exception {
 		if (parserFunction.equals(PARSER_FUNCTION_ANCHOR_ENCODE)) {
 			return Utilities.encodeAndEscapeTopicName(parserFunctionArguments);
 		}
 		if (parserFunction.equals(PARSER_FUNCTION_FILE_PATH)) {
 			// pre-pend the image namespace to the file name
 			String filename = NamespaceHandler.NAMESPACE_IMAGE + NamespaceHandler.NAMESPACE_SEPARATOR + parserFunctionArguments;
 			String result = LinkUtil.buildImageFileUrl(parserInput.getContext(), parserInput.getVirtualWiki(), filename);
 			if (result == null) {
 				return "";
 			}
 			// add nowiki tags so that the next round of parsing does not convert to an HTML link
 			return "<nowiki>" + LinkUtil.normalize(Environment.getValue(Environment.PROP_FILE_SERVER_URL) + result) + "</nowiki>";
 		}
 		if (parserFunction.equals(PARSER_FUNCTION_FULL_URL)) {
 			String result = LinkUtil.buildTopicUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserFunctionArguments, false);
 			return LinkUtil.normalize(Environment.getValue(Environment.PROP_SERVER_URL) + result);
 		}
 		if (parserFunction.equals(PARSER_FUNCTION_LOCAL_URL)) {
 			return LinkUtil.buildTopicUrl(parserInput.getContext(), parserInput.getVirtualWiki(), parserFunctionArguments, false);
 		}
 		if (parserFunction.equals(PARSER_FUNCTION_URL_ENCODE)) {
 			try {
 				return URLEncoder.encode(parserFunctionArguments, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				// this should never happen
 				throw new IllegalStateException("Unsupporting encoding UTF-8");
 			}
 		}
 		return null;
 	}
 }
