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
 
 import org.infoglue.deliver.taglib.component.ComponentLogicTag;
 
 public class ChildContentsTag extends ComponentLogicTag 
 {
 	private static final long serialVersionUID = 4050206323348354355L;
 
 	private Integer contentId;
 	private String propertyName;
 	private boolean useInheritance 	= true;
 	private boolean searchRecursive = false;
 	private String sortAttribute 	= "id";
 	private String sortOrder		= "asc";
     private boolean includeFolders 	= false;
 	
     public ChildContentsTag()
     {
         super();
     }
 
 	public int doEndTag() throws JspException
     {
 	    if(this.contentId != null)
 	        setResultAttribute(this.getController().getChildContents(this.contentId, this.searchRecursive, this.sortAttribute, this.sortOrder, this.includeFolders));
         else if(this.propertyName != null)
             setResultAttribute(getComponentLogic().getChildContents(this.propertyName, this.useInheritance, this.searchRecursive, this.sortAttribute, this.sortOrder, this.includeFolders));
         else
             throw new JspException("You must state either propertyName or siteNodeId");
 	    
 	    return EVAL_PAGE;
     }
 
	public void setPropertyName(String propertyName) throws JspException
 	{
         this.propertyName = evaluateString("childContents", "propertyName", propertyName);
 	}
 	
     public void setContentId(String contentId) throws JspException
     {
         this.contentId = evaluateInteger("childContents", "contentId", contentId);
     }
     
     public void setIncludeFolders(boolean includeFolders)
     {
         this.includeFolders = includeFolders;
     }
     
     public void setSearchRecursive(boolean searchRecursive)
     {
         this.searchRecursive = searchRecursive;
     }
     
     public void setSortAttribute(String sortAttribute)
     {
         this.sortAttribute = sortAttribute;
     }
     
     public void setSortOrder(String sortOrder)
     {
         this.sortOrder = sortOrder;
     }
     
     public void setUseInheritance(boolean useInheritance)
     {
         this.useInheritance = useInheritance;
     }
 }
