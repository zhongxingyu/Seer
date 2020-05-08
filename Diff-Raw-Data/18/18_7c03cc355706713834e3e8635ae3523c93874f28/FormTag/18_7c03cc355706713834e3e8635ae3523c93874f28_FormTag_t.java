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
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 
 /**
  * 
  */
 public class FormTag extends BodyTagSupport {
 	private static final long serialVersionUID = 3256441425909724981L;
 
 	private static final String RETURN_ADDRESS_PARAMETER = "returnAddress";
 
	private static final String FORM_START = "<form name=\"form\" id=\"form\" method=\"get\" action=\"{0}\">";
	private static final String HIDDEN     = "<div><input id=\"{0}\" name=\"{0}\" type=\"hidden\" value=\"{1}\"/></div>";
 	private static final String FORM_END   = "</form>";
 
 	/**
 	 * 
 	 */
 	public FormTag() {
 		super();
 	}
 
 	/**
 	 * 
 	 */
 	public int doStartTag() throws JspException {
 		final String returnAddress  = getReturnAddress();
 		final String actionID       = WorkflowHelper.getActionID(pageContext.getRequest());
 		final String workflowID     = WorkflowHelper.getWorkflowID(pageContext.getRequest());
 
 		final String actionIDName   = WorkflowHelper.ACTION_ID_PARAMETER;
 		final String workflowIDName = WorkflowHelper.WORKFLOW_ID_PARAMETER;
 
 		write(MessageFormat.format(FORM_START, new Object[] { returnAddress }));
 		write(MessageFormat.format(HIDDEN, new Object[] { actionIDName, actionID }));
 		write(MessageFormat.format(HIDDEN, new Object[] { workflowIDName,  workflowID }));
 		return EVAL_BODY_INCLUDE;
 	}
 
 	/**
 	 * 
 	 */
 	public int doEndTag() throws JspException {
 		write(FORM_END);
 		return EVAL_PAGE;
 	}
 
 	/**
 	 * 
 	 */
 	private void write(String text) throws JspException {
 		try {
 			pageContext.getOut().write(text);
 		} catch(IOException e) {
 			e.printStackTrace();
 			throw new JspTagException("IO error: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private String getReturnAddress() {
 		return pageContext.getRequest().getParameter(RETURN_ADDRESS_PARAMETER);
 	}
 }
