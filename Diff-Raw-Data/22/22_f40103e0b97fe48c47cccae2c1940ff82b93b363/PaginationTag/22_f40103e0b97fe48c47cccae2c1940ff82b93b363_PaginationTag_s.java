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
 package org.jamwiki.tags;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 
 /**
  *
  */
 public class PaginationTag extends BodyTagSupport {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(PaginationTag.class.getName());
 
 	private String rootUrl = null;
 	private String total = null;
 
 	/**
 	 *
 	 */
 	public int doEndTag() throws JspException {
 		String baseUrl = null;
 		int count = 0;
 		try {
 			baseUrl = ExpressionUtil.evalNotNull("pagination", "rootUrl", this.rootUrl, Object.class, this, pageContext).toString();
 			count = new Integer(ExpressionUtil.evalNotNull("pagination", "total", this.total, Object.class, this, pageContext).toString()).intValue();
 			this.pageContext.getOut().print(pagination(baseUrl, count));
 		} catch (Exception e) {
 			logger.severe("Failure while building pagination object", e);
 			throw new JspException(e);
 		}
 		return EVAL_PAGE;
 	}
 
 	/**
 	 *
 	 */
 	private StringBuffer buildOption(int num, Pagination pagination, String baseUrl) {
 		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 		StringBuffer output = new StringBuffer();
 		try {
 			if (num == pagination.getNumResults()) {
 				output.append(num);
 				return output;
 			}
 			output.append("<a href=\"");
 			String virtualWiki = JAMWikiServlet.getVirtualWikiFromRequest(request);
 			WikiLink wikiLink = LinkUtil.parseWikiLink(baseUrl);
 			String query = LinkUtil.appendQueryParam(wikiLink.getQuery(), "num", new Integer(num).toString());
			query += "&amp;offset=" + pagination.getOffset();
 			wikiLink.setQuery(query);
 			output.append(LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWiki, wikiLink));
 			output.append("\">");
 			output.append(num);
 			output.append("</a>");
 		} catch (Exception e) {
 			logger.warning("Failure while building pagination element", e);
 		}
 		return output;
 	}
 
 	/**
 	 *
 	 */
 	public String getRootUrl() {
 		return this.rootUrl;
 	}
 
 	/**
 	 *
 	 */
 	public String getTotal() {
 		return this.total;
 	}
 
 	/**
 	 *
 	 */
 	private StringBuffer nextPage(Pagination pagination, String baseUrl, int count, boolean previous) {
 		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 		StringBuffer output = new StringBuffer();
 		try {
 			Object[] objects = new Object[1];
 			objects[0] = new Integer(pagination.getNumResults());
 			if (pagination.getOffset() == 0 && previous) {
 				output.append(Utilities.formatMessage("common.pagination.previous", request.getLocale(), objects));
 				return output;
 			}
 			if (pagination.getNumResults() != count && !previous) {
 				output.append(Utilities.formatMessage("common.pagination.next", request.getLocale(), objects));
 				return output;
 			}
 			output.append("<a href=\"");
 			String virtualWiki = JAMWikiServlet.getVirtualWikiFromRequest(request);
 			WikiLink wikiLink = LinkUtil.parseWikiLink(baseUrl);
 			int offset = pagination.getOffset() + pagination.getNumResults();
 			if (previous) {
 				offset = pagination.getOffset() - pagination.getNumResults();
 				if (offset < 0) offset = 0;
 			}
 			String query = LinkUtil.appendQueryParam(wikiLink.getQuery(), "num", new Integer(pagination.getNumResults()).toString());
 			query += "&amp;offset=" + offset;
 			wikiLink.setQuery(query);
 			output.append(LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWiki, wikiLink));
 			output.append("\">");
 			if (previous) {
 				output.append(Utilities.formatMessage("common.pagination.previous", request.getLocale(), objects));
 			} else {
 				output.append(Utilities.formatMessage("common.pagination.next", request.getLocale(), objects));
 			}
 			output.append("</a>");
 		} catch (Exception e) {
 			logger.warning("Failure while building pagination element", e);
 		}
 		return output;
 	}
 
 	/**
 	 *
 	 */
 	private StringBuffer numResults(Pagination pagination, String baseUrl) {
 		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 		StringBuffer output = new StringBuffer();
 		output.append(Utilities.formatMessage("common.pagination.results", request.getLocale())).append(":&#160;");
 		output.append(buildOption(10, pagination, baseUrl));
 		output.append("&#160;|&#160;");
 		output.append(buildOption(25, pagination, baseUrl));
 		output.append("&#160;|&#160;");
 		output.append(buildOption(50, pagination, baseUrl));
 		output.append("&#160;|&#160;");
 		output.append(buildOption(100, pagination, baseUrl));
 		output.append("&#160;|&#160;");
 		output.append(buildOption(250, pagination, baseUrl));
 		output.append("&#160;|&#160;");
 		output.append(buildOption(500, pagination, baseUrl));
 		return output;
 	}
 
 	/**
 	 *
 	 */
 	private String pagination(String baseUrl, int count) {
 		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 		Pagination pagination = JAMWikiServlet.buildPagination(request, null);
 		StringBuffer output = new StringBuffer();
 		output.append(this.nextPage(pagination, baseUrl, count, true));
 		output.append("&#160;|&#160;");
 		output.append(this.nextPage(pagination, baseUrl, count, false));
 		output.append("&#160;&#160;(");
 		output.append(this.numResults(pagination, baseUrl));
 		output.append(")");
 		return output.toString();
 	}
 
 	/**
 	 *
 	 */
 	public void setRootUrl(String rootUrl) {
 		this.rootUrl = rootUrl;
 	}
 
 	/**
 	 *
 	 */
 	public void setTotal(String total) {
 		this.total = total;
 	}
 }
