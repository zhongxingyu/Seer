 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * PaginationTag.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * Dec 01, 2012		chalam						Created
  * *************************************************************
  */
 package org.sourcetree.interview.tags;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sourcetree.interview.dto.ListProp;
 
 /**
  * Pagination Support tag
  * 
  * @author chalam
  */
 public class PaginationTag extends TagSupport
 {
 	private static final long serialVersionUID = 1L;
 	private static final Log LOG = LogFactory.getLog(PaginationTag.class);
 
 	private ListProp listProp;
 
 	private String searchValue;
 
 	private String urlPrefix;
 
 	/**
 	 * @param listProp
 	 *            the listProp to set
 	 */
 	public void setListProp(ListProp listProp)
 	{
 		this.listProp = listProp;
 	}
 
 	/**
 	 * @param searchValue
 	 *            the searchValue to set
 	 */
 	public void setSearchValue(String searchValue)
 	{
 		this.searchValue = searchValue;
 	}
 
 	/**
 	 * @param urlPrefix
 	 *            the urlPrefix to set
 	 */
 	public void setUrlPrefix(String urlPrefix)
 	{
 		this.urlPrefix = urlPrefix;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int doStartTag() throws JspException
 	{
 		if (listProp != null)
 		{
 			StringBuilder sb = new StringBuilder("<div class='pagination'>");
 			sb.append(generatePagingInfo(listProp));
 			sb.append("</div>");
 
 			try
 			{
 				pageContext.getOut().write(sb.toString());
 			}
 			catch (IOException e)
 			{
 				LOG.error("", e);
 			}
 		}
 
 		return SKIP_BODY;
 	}
 
 	private StringBuilder generatePagingInfo(ListProp listProp)
 	{
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("<span class='pagination_total'>Total Records :");
 		sb.append(listProp.getTotalRecords());
 		sb.append(" </span>");
 
 		sb.append("<span style='display:inline-block'>");
 		if (listProp.getPage() > 1)
 		{
 			sb.append("<a class='pagerPrev pagination_total' href='");
 			sb.append(urlPrefix);
 
 			if (!urlPrefix.endsWith("/"))
 			{
 				sb.append("/");
 			}
 
 			sb.append(listProp.getPage() - 1);
 			if (!StringUtils.isBlank(searchValue))
 			{
 				sb.append("?searchValue=");
 				sb.append(searchValue);
 			}
 
 			sb.append("'>");
 			sb.append("<< PREV");
 			sb.append("</a>&nbsp;");
 		}
 
 		if (listProp.getTotalPages() > 1
				&& listProp.getTotalPages() >= listProp.getPage())
 		{
 			sb.append("&nbsp;<a class='pager pagination_page' href='");
 			sb.append(urlPrefix);
 
 			if (!urlPrefix.endsWith("/"))
 			{
 				sb.append("/");
 			}
 
 			sb.append(listProp.getPage() + 1);
 
 			if (!StringUtils.isBlank(searchValue))
 			{
 				sb.append("?searchValue=");
 				sb.append(searchValue);
 			}
 
 			sb.append("'>");
 			sb.append("NEXT >>");
 			sb.append("</a>");
 		}
 
 		sb.append("</span>");
 		sb.append("<span class='pagination_page'> Page:");
 		sb.append(listProp.getPage());
 		sb.append(" of ");
 		sb.append(listProp.getTotalPages());
 		sb.append(" </span>");
 
 		return sb;
 	}
 }
