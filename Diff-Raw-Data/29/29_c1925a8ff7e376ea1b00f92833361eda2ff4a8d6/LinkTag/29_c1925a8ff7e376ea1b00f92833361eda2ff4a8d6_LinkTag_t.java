 package org.jmwiki.tags;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 import org.apache.log4j.Logger;
 import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
 import org.jmwiki.servlets.JMController;
 import org.jmwiki.utils.JSPUtils;
 import org.jmwiki.utils.Utilities;
 
 /**
  *
  */
 public class LinkTag extends TagSupport {
 
 	private static Logger logger = Logger.getLogger(LinkTag.class);
 	private String value = null;
 
 	/**
 	 *
 	 */
 	public int doEndTag() throws JspException {
 		try {
			value = (String)ExpressionUtil.evalNotNull("link", "value", value, Object.class, this, pageContext);
 			HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
 			String url = null;
 			String virtualWiki = retrieveVirtualWiki(request);
 			try {
 				url = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, value);
 				this.pageContext.getOut().print(url);
 			} catch (Exception e) {
 				logger.error("Failure while building url " + url + " with value " + value);
 				throw new JspException(e);
 			}
 			return EVAL_PAGE;
 		} finally {
 			// FIXME - var & value not getting reset, so explicitly call release
 			release();
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static String retrieveVirtualWiki(HttpServletRequest request) throws JspException {
 		String virtualWiki = request.getParameter(JMController.PARAMETER_VIRTUAL_WIKI);
 		if (virtualWiki == null) {
 			virtualWiki = (String)request.getAttribute(JMController.PARAMETER_VIRTUAL_WIKI);
 		}
 		if (virtualWiki == null) {
 			logger.error("No virtual wiki found for context path: " + request.getContextPath());
 			throw new JspException("No virtual wiki value found");
 		}
 		return virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public String getValue() {
 		return this.value;
 	}
 
 	/**
 	 *
 	 */
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	/**
 	 *
 	 */
     public void release() {
 		super.release();
 		this.value = null;
     }
 }
