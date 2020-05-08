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
 package org.jamwiki.taglib;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 
 /**
  * This class provides capability to format an edit comment, parsing out any
  * section name that is present and converting it into a link to the section.
  */
 public class EditCommentTag extends TagSupport {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(EditCommentTag.class.getName());
	private static final Pattern SECTION_NAME_PATTERN = Pattern.compile("(/\\*(.+?)\\*/)(.*)");
 	private static final String CSS_SECTION_COMMENT = "section-link";
 	private String comment = null;
 	private String topic = null;
 
 	/**
 	 * Generate the tag HTML output.
 	 */
 	public int doEndTag() throws JspException {
 		try {
 			String result = this.parseComment();
 			this.pageContext.getOut().print(result);
 		} catch (Exception e) {
 			logger.severe("Failure while building edit comment for comment " + this.comment, e);
 			throw new JspException(e);
 		}
 		return EVAL_PAGE;
 	}
 
 	/**
 	 * Return the full (un-parsed) edit comment.
 	 */
 	public String getComment() {
 		return this.comment;
 	}
 
 	/**
 	 * Set the full (un-parsed) edit comment.
 	 */
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	/**
 	 * Return the topic name
 	 */
 	public String getTopic() {
 		return this.topic;
 	}
 
 	/**
 	 * Set the topic name.
 	 */
 	public void setTopic(String topic) {
 		this.topic = topic;
 	}
 
 	/**
 	 * Process the edit comment and return a parsed output string.
 	 */
 	private String parseComment() throws Exception {
 		if (StringUtils.isBlank(this.getComment())) {
 			return this.getComment();
 		}
 		Matcher matcher = SECTION_NAME_PATTERN.matcher(this.getComment().trim());
 		if (!matcher.matches()) {
 			return StringEscapeUtils.escapeXml(this.getComment());
 		}
 		String sectionName = matcher.group(2);
 		if (StringUtils.isBlank(sectionName)) {
 			return StringEscapeUtils.escapeXml(this.getComment());
 		}
 		sectionName = sectionName.trim();
 		String additionalComment = matcher.group(3);
 		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
 		WikiLink wikiLink = LinkUtil.parseWikiLink(this.topic + "#" + sectionName);
 		String result = "";
 		result += "<span class=\"" + CSS_SECTION_COMMENT + "\">";
 		result += LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki, wikiLink, "&rarr;", null, null, false);
 		result += StringEscapeUtils.escapeXml(sectionName) + (!StringUtils.isBlank(additionalComment) ? " -" : "");
 		result += "</span>";
 		if (!StringUtils.isBlank(additionalComment)) {
 			result += " " + StringEscapeUtils.escapeXml(additionalComment);
 		}
 		return result;
 	}
 }
