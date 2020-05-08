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
 
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class parses signature tags of the form <code>~~~</code>,
  * <code>~~~~</code> and <code>~~~~~</code>.
  */
 public class WikiSignatureTag {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiSignatureTag.class.getName());
 
 	/**
 	 *
 	 */
	private String buildWikiSignature(ParserInput parserInput, ParserOutput parserOutput, int mode, boolean includeUser,
			boolean includeDate) {
 		try {
 			String signature = "";
 			if (includeUser) {
 				signature = this.retrieveUserSignature(parserInput);
 				// parse signature as link in order to store link metadata
 				WikiLinkTag wikiLinkTag = new WikiLinkTag();
 				wikiLinkTag.parse(parserInput, parserOutput, mode, signature);
 				if (mode != JFlexParser.MODE_MINIMAL) {
 					signature = JFlexParserUtil.parseFragment(parserInput, signature, mode);
 				}
 			}
 			if (includeUser && includeDate) {
 				signature += " ";
 			}
 			if (includeDate) {
 				SimpleDateFormat format = new SimpleDateFormat();
 				format.applyPattern(Environment.getValue(Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN));
 				signature += format.format(new java.util.Date());
 			}
 			return signature;
 		} catch (Exception e) {
 			logger.severe("Failure while building wiki signature", e);
 			// FIXME - return empty or a failure indicator?
 			return "";
 		}
 	}
 
 	/**
 	 * Parse a Mediawiki signature of the form "~~~~" and return the resulting
 	 * HTML output.
 	 */
 	public String parse(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) {
 		if (raw.equals("~~~")) {
 			return this.buildWikiSignature(parserInput, parserOutput, mode, true, false);
 		}
 		if (raw.equals("~~~~")) {
 			return this.buildWikiSignature(parserInput, parserOutput, mode, true, true);
 		}
 		if (raw.equals("~~~~~")) {
 			return this.buildWikiSignature(parserInput, parserOutput, mode, false, true);
 		}
 		return raw;
 	}

 	/**
 	 *
 	 */
 	private String retrieveUserSignature(ParserInput parserInput) {
 		WikiUser user = parserInput.getWikiUser();
 		if (user != null && !StringUtils.isBlank(user.getSignature())) {
 			return user.getSignature();
 		}
 		String login = parserInput.getUserIpAddress();
 		String email = parserInput.getUserIpAddress();
 		String displayName = parserInput.getUserIpAddress();
 		String userId = "-1";
 		if (user != null && !StringUtils.isBlank(user.getUsername())) {
 			login = user.getUsername();
 			displayName = (!StringUtils.isBlank(user.getDisplayName())) ? user.getDisplayName() : login;
 			email = user.getEmail();
 			userId = Integer.toString(user.getUserId());
 		}
		if (login == null || displayName == null) {
 			logger.info("Signature tagged parsed without user information available, returning empty");
 			return "";
 		}
 		MessageFormat formatter = new MessageFormat(Environment.getValue(Environment.PROP_PARSER_SIGNATURE_USER_PATTERN));
 		Object params[] = new Object[7];
 		params[0] = NamespaceHandler.NAMESPACE_USER + NamespaceHandler.NAMESPACE_SEPARATOR + login;
 		// FIXME - hard coding
 		params[1] = NamespaceHandler.NAMESPACE_SPECIAL + NamespaceHandler.NAMESPACE_SEPARATOR + "Contributions?contributor=" + login;
 		params[2] = NamespaceHandler.NAMESPACE_USER_COMMENTS + NamespaceHandler.NAMESPACE_SEPARATOR + login;
 		params[3] = login;
 		params[4] = displayName;
		params[5] = email!=null ? email : "";
 		params[6] = userId;
 		return formatter.format(params);
 	}
 }
