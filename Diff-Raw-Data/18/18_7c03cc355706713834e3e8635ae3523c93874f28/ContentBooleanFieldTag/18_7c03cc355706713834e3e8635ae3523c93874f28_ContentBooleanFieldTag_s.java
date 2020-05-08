 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.workflow.taglib;
 
 import java.text.MessageFormat;
 
 import javax.servlet.jsp.JspException;
 
 /**
  * 
  */
 public abstract class ContentBooleanFieldTag extends ContentInputTag {
 	private static final String FIELD_UNCHECKED = "<input id=\"{0}\" name=\"{1}\" type=\"{2}\" value=\"{3}\"/>";
	private static final String FIELD_CHECKED   = "<input id=\"{0}\" name=\"{1}\" type=\"{2}\" value=\"{3}\" checked=\"true\"/>";
 
 	private String value;
 	private String fieldType;
 	
 	
 	/**
 	 * 
 	 */
 	public ContentBooleanFieldTag(String fieldType) {
 		super();
 		this.fieldType = fieldType;
 	}
 
 	/**
 	 * 
 	 */
 	public int doEndTag() throws JspException {
 		write(createBooleanFieldHTML());
 		return EVAL_PAGE;
 	}
 
 
 	/**
 	 * 
 	 */
 	private String createBooleanFieldHTML() {
 		String value = (this.value == null) ? "" : this.value;
 		if(getContentValue() != null && value != null && value.equals(getContentValue()))
 			return MessageFormat.format(FIELD_CHECKED, new Object[] { getIdAttr(), getName(), fieldType, value });
 		else
 			return MessageFormat.format(FIELD_UNCHECKED, new Object[] { getIdAttr(), getName(), fieldType, value });
 	}
 
 	/**
 	 * 
 	 */
 	public void setValue(String value) {
 		this.value = value;
 	}
 }
