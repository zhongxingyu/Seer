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
 
 package org.infoglue.deliver.taglib.common;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.jsp.JspException;
 
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.deliver.taglib.TemplateControllerTag;
 
 public class IncludeTag extends TemplateControllerTag 
 {
 	private static final long serialVersionUID = 4050206323348354355L;
 	
 	private Integer contentId;
 	private String relationAttributeName;
 	private String contentName;
 	private String template;
 	private boolean useAttributeLanguageFallback = true;
 	private boolean useSubContext = false;
 	
     public IncludeTag()
     {
         super();
     }
 
 	public int doEndTag() throws JspException
     {
 		try
         {
 		    if(contentId == null)
 		    {
 			    Integer componentContentId = this.getController().getComponentLogic().getInfoGlueComponent().getContentId();
 			    
 			    List relatedContents = this.getController().getRelatedContents(componentContentId, relationAttributeName, useAttributeLanguageFallback);
 
 			    Iterator i = relatedContents.iterator();
 			    while(i.hasNext())
 			    {
 			        ContentVO contentVO = (ContentVO)i.next();
 			        if(contentVO.getName().equalsIgnoreCase(contentName))
 	                {
 			            contentId = contentVO.getId();
 			            break;
 	                }
 			    }
 
 			    template = this.getController().getContentAttributeUsingLanguageFallback(contentId, "Template", true);
 		    }
 		    else
 		    {
 		        template = this.getController().getContentAttributeUsingLanguageFallback(contentId, "Template", true);
 		    }
 		    
		    String result = this.getController().renderString(template, contentId, this.useSubContext);
 		    produceResult(result);
         } 
 		catch (Exception e)
         {
             e.printStackTrace();
 		    produceResult("");
         }
 		
 		this.contentId = null;
 		this.relationAttributeName = null;
 		this.contentName = null;
 		this.template = null;
 		this.useAttributeLanguageFallback = true;
 		this.useSubContext = false;
 		
         return EVAL_PAGE;
     }
 
     public void setTemplate(String template) throws JspException
     {
         this.contentId = null;
         this.template = evaluateString("includeTag", "template", template);
     }
     
     public void setContentId(String contentId) throws JspException
     {
         this.contentId = evaluateInteger("includeTag", "contentId", contentId);
     }
     
     public void setRelationAttributeName(String relationAttributeName) throws JspException
     {
         this.contentId = null;
         this.relationAttributeName = evaluateString("includeTag", "relationAttributeName", relationAttributeName);
     }
 
     public void setContentName(String contentName) throws JspException
     {
         this.contentId = null;
         this.contentName = evaluateString("includeTag", "contentName", contentName);
     }
 
     public void setUseAttributeLanguageFallback(boolean useAttributeLanguageFallback) throws JspException
     {
         this.useAttributeLanguageFallback = useAttributeLanguageFallback;
     }
 
     public void setUseSubContext(boolean useSubContext) throws JspException
     {
         this.useSubContext = useSubContext;
     }
 }
