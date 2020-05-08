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
 package org.infoglue.deliver.taglib.structure;
 
 import javax.servlet.jsp.JspException;
 
 import org.infoglue.deliver.taglib.TemplateControllerTag;
 import org.infoglue.deliver.taglib.component.ComponentLogicTag;
 
public class PageUrlTag extends ComponentLogicTag {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4050485595074016051L;
 	
 	private String propertyName;
 	private boolean useInheritance = true;
 
 	private Integer siteNodeId;
 	private Integer languageId;
 	private Integer contentId = new Integer(-1);
 
 	private String extraParameters;
 	
 	public PageUrlTag() 
 	{
 		super();
 	}
 
     public int doEndTag() throws JspException
     {
         produceResult(getPageUrl());
         return EVAL_PAGE;
     }
 
 	private String getPageUrl() throws JspException
 	{
 	    if(this.languageId == null)
 	        this.languageId = getController().getLanguageId();
 	    
 	    if(this.propertyName != null)
 	        return getComponentLogic().getPageUrl(propertyName, contentId, languageId, useInheritance);
 	    else
 	        return getController().getPageUrl(siteNodeId, languageId, contentId);
 	}
 
 	public void setSiteNodeId(final String siteNodeId) throws JspException
     {
         this.siteNodeId = evaluateInteger("pageUrl", "siteNodeId", siteNodeId);
     }
 
     public void setLanguageId(final String languageId) throws JspException
     {
         this.languageId = evaluateInteger("pageUrl", "languageId", languageId);
     }
 
     public void setContentId(final String contentId) throws JspException
     {
         this.contentId = evaluateInteger("pageUrl", "contentId", contentId);
     }
     
     public void setPropertyName(String propertyName)
     {
         this.propertyName = propertyName;
     }
     
     public void setUseInheritance(boolean useInheritance)
     {
         this.useInheritance = useInheritance;
     }
     
     public void setExtraParameters(String extraParameters)
     {
         this.extraParameters = extraParameters;
     }
 }
