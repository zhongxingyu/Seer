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
 
 package org.infoglue.deliver.taglib.content;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 
 import org.infoglue.deliver.taglib.component.ComponentLogicTag;
 
 /**
  * This is an attempt to make an TagLib for attempts to get a AssetUrl:s from a content referenced by a component
  * in a JSP.
  * 
  * <%@ taglib uri="infoglue" prefix="infoglue" %>
  * 
  * <infoglue:component.AssetUrl propertyName="Logotype" assetKey="logotype"/>
  *
  * @author Mattias Bogeblad
  */
 
 public class AssetUrlTag extends ComponentLogicTag
 {
 	private static final long serialVersionUID = 3546080250652931383L;
 
 	private Integer contentId;
 	private String propertyName;
     private String assetKey;
     private boolean useInheritance = true;
     
     public AssetUrlTag()
     {
         super();
     }
     
     public int doEndTag() throws JspException
     {
         try
         {
             if(contentId != null)
             {
 	            if(assetKey != null)
	                write(getController().getAssetUrl(contentId, assetKey));
 	            else
	                write(getController().getAssetUrl(contentId));    
             }
             else if(propertyName != null)
             {
 	            if(assetKey != null)
	                write(getComponentLogic().getAssetUrl(propertyName, assetKey, useInheritance));
 	            else
	                write(getComponentLogic().getAssetUrl(propertyName, useInheritance));                    
             }
             else
             {
                 throw new JspException("You must supply either contentId or propertyName");
             }
         }
         catch(Exception e)
         {
             throw new JspTagException("ComponentLogic.getAssetUrl Error: " + e.getMessage());
         }
         
         return EVAL_PAGE;
     }
 
     public void setAssetKey(String assetKey)
     {
         this.assetKey = assetKey;
     }
 
     public void setPropertyName(String propertyName)
     {
         this.propertyName = propertyName;
     }
     
     public void setUseInheritance(boolean useInheritance)
     {
         this.useInheritance = useInheritance;
     }
     
    public void setContentId(Integer contentId)
     {
        this.contentId = contentId;
     }
 }
