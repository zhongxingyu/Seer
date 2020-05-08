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
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class LinkParamTag extends BodyTagSupport {
 
 	private static WikiLogger logger = WikiLogger.getLogger(LinkParamTag.class.getName());
 	private String value = null;
 	private String key = null;
 
 	/**
 	 *
 	 */
 	public int doEndTag() throws JspException {
 		String tagValue = null;
 		LinkTag parent = (LinkTag)this.getParent();
 		if (parent == null) {
 			throw new JspException("linkParam tag not nested within a link tag");
 		}
 		try {
			if (StringUtils.hasText(this.value)) {
 				tagValue = ExpressionUtil.evalNotNull("linkParam", "value", this.value, Object.class, this, pageContext).toString();
 			} else {
 				tagValue = this.getBodyContent().getString();
 			}
 			parent.addQueryParam(this.key, tagValue);
 		} catch (JspException e) {
 			logger.severe("Failure in link tag for " + this.value, e);
 			throw e;
 		}
 		return SKIP_BODY;
 	}
 
 	/**
 	 *
 	 */
 	public String getKey() {
 		return this.key;
 	}
 
 	/**
 	 *
 	 */
 	public void setKey(String key) {
 		this.key = key;
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
 }
