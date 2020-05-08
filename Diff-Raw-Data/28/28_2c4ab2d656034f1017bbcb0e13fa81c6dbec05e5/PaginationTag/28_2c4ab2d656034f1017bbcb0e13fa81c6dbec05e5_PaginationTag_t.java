 package com.xone.service.app.tags;
 
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.xone.model.hibernate.support.Pagination;
 
 public class PaginationTag extends TagSupport {
 
 	private static final long serialVersionUID = 2037407896755085349L;
 	
	protected static final int I_NUM = 4;
	
 	protected Object pagination;
 	protected String href;
 	protected String name;
 
 	public Object getPagination() {
 		return pagination;
 	}
 
 	public void setPagination(Object pagination) {
 		this.pagination = pagination;
 	}
 
 	public String getHref() {
 		if (null == href) {
 			return "";
 		}
 		return href;
 	}
 
 	public void setHref(String href) {
 		this.href = href;
 	}
 
 	public String getName() {
 		if (StringUtils.isBlank(name)) {
 			return "pagination";
 		}
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	protected String page(Pagination p, int offset) {
 		String tag = "?";
 		if (getHref().indexOf("?") > -1) {
 			tag = "&";
 		}
 		StringBuffer params = new StringBuffer();
 		params.append(getHref());
 		params.append(tag);
 		params.append(getName());
 		params.append(".pageNo=");
 		params.append(offset);
 		params.append("&");
 		params.append(getName());
 		params.append(".pageSize=");
 		params.append(p.getPageSize());
 		return params.toString();
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected String params() {
 		Map<String, String []> map = (Map<String, String []>)this.pageContext.getRequest().getParameterMap();
 		StringBuffer params = new StringBuffer();
 		if (null != map && !map.isEmpty()) {
 			for (Map.Entry<String, String []> m : map.entrySet()) {
 				String key = m.getKey();
 				if (key.equals(getName() + ".pageNo") || key.equals(getName() + ".pageSize")) {
 					continue;
 				}
 				for (String v : m.getValue()) {
 					if (StringUtils.isBlank(v)) {
 						continue;
 					}
 					params.append("&");
 					params.append(key);
 					params.append("=");
 					params.append(v);
 				}
 			}
 		}
 		return params.toString();
 	}
 
 	@Override
 	public int doStartTag() throws JspException {
 		try {
 			JspWriter out = this.pageContext.getOut();
 			if (getPagination() == null || (getPagination() instanceof String) || !(getPagination() instanceof Pagination)) {
 				return SKIP_BODY;
 			}
 			Pagination p = (Pagination)getPagination();
 			int totalPage = p.getTotalPage();
 			if (0 == totalPage) {
 				return SKIP_BODY;
 			}
 			String params = params();
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("<div class=\"pagination pagination-centered\">");
 			buffer.append("<ul>");
 			buffer.append("<li><a href=\"#");
 			buffer.append("\">");
 			buffer.append("共");
 			buffer.append(totalPage);
 			buffer.append("页&nbsp;/&nbsp;");
 			buffer.append(p.getTotalCount());
 			buffer.append("条记录&nbsp;/&nbsp;第");
 			buffer.append(p.getPageNo());
 			buffer.append("页</a></li>");
 			buffer.append("<li><a href=\"");
 			buffer.append(page(p, p.getPrePage()));
 			buffer.append(params);
 			buffer.append("\">上一页</a></li>");
 			int pageNo = p.getPageNo();
			int start = (pageNo > I_NUM) ? (pageNo - I_NUM) : 1;
			for (int i = start; i <= totalPage; i ++) {
 				buffer.append("<li><a href=\"");
 				buffer.append(page(p, i));
 				buffer.append(params);
 				buffer.append("\">");
 				buffer.append(i);
 				buffer.append("</a></li>");
				if (i > start + 2 * I_NUM) {
 					buffer.append("<li><a href=\"#");
 					buffer.append("\">...</a></li>");
 					break;
 				}
 			}
 			buffer.append("<li><a href=\"");
 			buffer.append(page(p, p.getNextPage()));
 			buffer.append(params);
 			buffer.append("\">下一页</a></li>");
 			buffer.append("</ul>");
 			buffer.append("</div>");
 			out.println(buffer.toString());
 		} catch (Exception e) {
 			throw new JspException(e.getMessage());
 		}
 		return SKIP_BODY;
 	}
 
 	@Override
 	public int doEndTag() throws JspException {
 		return EVAL_PAGE;
 	}
 
 	@Override
 	public void release() {
 		super.release();
 		this.setPagination(null);
 		this.setHref(null);
 	}
 
 }
