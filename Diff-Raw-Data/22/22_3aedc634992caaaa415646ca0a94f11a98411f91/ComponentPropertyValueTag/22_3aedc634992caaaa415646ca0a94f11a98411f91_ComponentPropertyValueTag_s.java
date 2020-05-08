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
 
 import org.infoglue.deliver.taglib.component.ComponentLogicTag;
 
 public class ComponentPropertyValueTag extends ComponentLogicTag 
 {
 	private static final long serialVersionUID = 4050206323348354355L;
 
 	private Integer siteNodeId;
 	private String propertyName;
 	private boolean useLanguageFallback = true;
 	private boolean useInheritance = true;
 	
 	
     public ComponentPropertyValueTag()
     {
         super();
     }
 
 	public int doEndTag() throws JspException
     {
 	    try
 	    {
 	        if(siteNodeId == null)
 	        {
 		        String propertyValue = getComponentLogic().getPropertyValue(propertyName, useLanguageFallback, useInheritance);
 		        setResultAttribute(propertyValue);
 	        }
 	        else
 	        {
 		        String propertyValue = getComponentLogic().getPropertyValue(siteNodeId, propertyName, useLanguageFallback, useInheritance);
 		        setResultAttribute(propertyValue);
 	        }
 	    }
 	    catch(Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	    
 		return EVAL_PAGE;
     }
 
     public void setSiteNodeId(String siteNodeId) throws JspException
     {
         this.siteNodeId = evaluateInteger("componentPropertyValue", "siteNodeId", siteNodeId);
     }
 
    public void setPropertyName(String name) throws JspException
 	{
		this.propertyName = evaluateString("componentPropertyValue", "propertyName", name);
 	}
 	
 	public void setUseInheritance(boolean useInheritance)
     {
         this.useInheritance = useInheritance;
     }
 	
 }
