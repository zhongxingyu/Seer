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
 
 package org.infoglue.deliver.applications.databeans;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.deliver.util.CacheController;
 
 /**
  * 
  */
 
 public class ComponentProperty
 {
     public final static Logger logger = Logger.getLogger(ComponentProperty.class.getName());
     
 	public static final String BINDING 		= "binding";
 	public static final String TEXTFIELD 	= "textfield";
 	public static final String TEXTAREA 	= "textarea";
 	public static final String SELECTFIELD 	= "select";
 	
 	private Integer id;
 	private String name;
	private String description = "";
 	private String type;
 	private String[] allowedContentTypeNamesArray = null;
 	private Integer componentId;
 	private String entityClass;
 	private Integer entityId;
 	private String value;
 	private boolean isMultipleBinding = false;
 	private String visualizingAction = null;
 	private String createAction = null;
 
 	private List options = new ArrayList();
 	
 	private List contentBindings = new ArrayList();
 	private List siteNodeBindings = new ArrayList();
 		
 		
 	public Integer getComponentId()
 	{
 		return componentId;
 	}
 
 	public List getContentBindings()
 	{
 		return contentBindings;
 	}
 
 	public String getEntityClass()
 	{
 		return entityClass;
 	}
 
 	public Integer getEntityId()
 	{
 		return entityId;
 	}
 
 	public Integer getId()
 	{
 		return id;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public List getSiteNodeBindings()
 	{
 		return siteNodeBindings;
 	}
 
 	public String getValue()
 	{
 		return value;
 	}
 
 	public void setComponentId(Integer integer)
 	{
 		componentId = integer;
 	}
 
 	public void setContentBindings(List list)
 	{
 		contentBindings = list;
 	}
 
 	public void setEntityClass(String string)
 	{
 		entityClass = string;
 	}
 
 	public void setEntityId(Integer integer)
 	{
 		entityId = integer;
 	}
 
 	public void setId(Integer integer)
 	{
 		id = integer;
 	}
 
 	public void setName(String string)
 	{
 		name = string;
 	}
 
     public String getDescription()
     {
    	if(description == null)
    		return "";
    	else
    		return description;
     }
     
     public void setDescription(String description)
     {
   		this.description = description;
     }
 
 	public void setSiteNodeBindings(List list)
 	{
 		siteNodeBindings = list;
 	}
 
 	public void setValue(String string)
 	{
 		value = string;
 	}
 
 	public String getType()
 	{
 		return type;
 	}
 
 	public void setType(String string)
 	{
 		type = string;
 	}
 
 	public boolean getIsMultipleBinding()
 	{
 		return this.isMultipleBinding;
 	}
 
 	public void setIsMultipleBinding(boolean isMultipleBinding)
 	{
 		this.isMultipleBinding = isMultipleBinding;
 	}
 
 	public String getVisualizingAction()
 	{
 		return visualizingAction;
 	}
 
 	public void setVisualizingAction(String visualizingAction)
 	{
 		this.visualizingAction = visualizingAction;
 	}
 
 	public String getCreateAction()
 	{
 		return this.createAction;
 	}
 
 	public void setCreateAction(String createAction)
 	{
 		this.createAction = createAction;
 	}
 
     public String[] getAllowedContentTypeNamesArray()
     {
         return allowedContentTypeNamesArray;
     }
     
     public void setAllowedContentTypeNamesArray(String[] allowedContentTypeNamesArray)
     {
         this.allowedContentTypeNamesArray = allowedContentTypeNamesArray;
     }
 
     public String getAllowedContentTypeNamesAsUrlEncodedString() throws Exception
     {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < allowedContentTypeNamesArray.length; i++)
         {
             if (i > 0)
             {
                 sb.append("&");
             }
 
             sb.append("allowedContentTypeNames=" + URLEncoder.encode(allowedContentTypeNamesArray[i], "UTF-8"));
         }
 
         return sb.toString();
     }
 
     public String getAllowedContentTypeIdAsUrlEncodedString(Database db) throws Exception
     {
         StringBuffer sb = new StringBuffer();
         String allowedContentTypeName = null;
         for (int i = 0; i < allowedContentTypeNamesArray.length; i++)
         {
             if (i > 0)
             {
                 sb.append("&");
             }
 
             allowedContentTypeName = allowedContentTypeNamesArray[i];
             ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController()
                     .getContentTypeDefinitionVOWithName(allowedContentTypeName, db);
             if ( contentTypeDefinitionVO != null )
             {
                 sb.append("allowedContentTypeIds=" + contentTypeDefinitionVO.getId());
             }
             else
             {
                 logger.error("Cant find the ContentTypeDefinition for: " + allowedContentTypeName );
             }
         }
         return sb.toString();
     }
 
     public List getOptions()
     {
         return options;
     }
 
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("id=").append(id)
 			.append(" name=").append(name)
 			.append(" type=").append(type)
 			.append(" componentId=").append(componentId)
 			.append(" entityClass=").append(entityClass)
 			.append(" entityId=").append(entityId)
 			.append(" value=").append(value)
 			.append(" isMultipleBinding=").append(isMultipleBinding)
 			.append(" visualizingAction=").append(visualizingAction)
 			.append(" createAction=").append(createAction)
 			.append(" allowedContentTypeNames=").append(allowedContentTypeNamesArray)
 			.append(" contentBindings.size=").append(contentBindings.size())
 			.append(" siteNodeBindings.size=").append(siteNodeBindings.size())
 			.append(" categoryBindings.size=").append("not implemented");
 		return sb.toString();
 	}
 	
 }
