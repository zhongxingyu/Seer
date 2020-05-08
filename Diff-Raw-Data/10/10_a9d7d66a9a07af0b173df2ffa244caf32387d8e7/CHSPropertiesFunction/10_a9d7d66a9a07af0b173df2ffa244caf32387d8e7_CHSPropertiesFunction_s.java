 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool.functions;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.api.GroupAwareEdit;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.sdata.tool.model.CHSNodeMap;
 import org.sakaiproject.sdata.tool.api.Handler;
 import org.sakaiproject.sdata.tool.api.ResourceDefinition;
 import org.sakaiproject.sdata.tool.api.SDataException;
 
 /**
  * <p>
  * Set properties on the content entity.
  * </p>
  * <p>
  * Properties are specified in 4 request arrays associated in order. name,
  * value, action. These arrays must all be specified and the same length.
  * </p>
  * <p>
  * <b>item (optional)</b>: A list of item names, if this parameter is present
  * on a ContentCollection, the item specifies the target item as a child of the 
  * ContentCollection. A blank string identifies the ContentCollection itself.
  * </p>
  * <p>
  * <b>name</b>: A list of request parameters of name <b>name</b> that specify
  * the name of each property in the value and action parameters.
  * </p>
  * <p>
  * <b>value</b>: A list of request parameters of name <b>value</b> that
  * specify the value of each property named in the name parameter.
  * </p>
  * <p>
  * <b>action</b>: A list of request parameters of name <b>acrtion</b> that
  * specifies what should be done with each name value pair. Action can be <b>a</a>
  * for add, <b>d</b> for remove or <b>r</b> for replace.
  * </p>
  * <ul>
  * <li> <b>add</b>: To add a property or to create a new property. </li>
  * <li> <b>remove</b>: To remove the property. </li>
  * <li> <b>replace</b>: To replace the property with the value specified, this
  * will be a single value property to start with, but if later (including in the
  * same request) it is converted into a list. </li>
  * </ul>
  * 
  * @author ieb
  */
 public class CHSPropertiesFunction extends CHSSDataFunction
 {
 
 	public static final String ADD = "a";
 
 	public static final String REMOVE = "d";
 
 	public static final String REPLACE = "r";
 
 	public static final String NAME = "name";
 
 	public static final String VALUE = "value";
 
 	public static final String ACTION = "action";
 
 	private static final Log log = LogFactory.getLog(CHSPropertiesFunction.class);
 
 	private static final String ITEM = "item";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.SDataFunction#call(org.sakaiproject.sdata.tool.api.Handler,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
 	 *      org.sakaiproject.sdata.tool.api.ResourceDefinition)
 	 */
 	public void call(Handler handler, HttpServletRequest request,
 			HttpServletResponse response, Object target, ResourceDefinition rp)
 			throws SDataException
 	{
 		SDataFunctionUtil.checkMethod(request.getMethod(), "POST");
 		
 		GroupAwareEdit baseEdit = editEntity(handler, target, rp.getRepositoryPath());
 		ResourcePropertiesEdit baseProperties = baseEdit.getPropertiesEdit();
 
 		String[] items = request.getParameterValues(ITEM);
 		String[] names = request.getParameterValues(NAME);
 		String[] values = request.getParameterValues(VALUE);
 		String[] actions = request.getParameterValues(ACTION);
 
 		if (names == null || values == null || actions == null
 				|| names.length != values.length || names.length != actions.length)
 		{
 			throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
 					"Request must contain the same number of name, value, and action parameters ");
 		}
 		
 		GroupAwareEdit[] edit = new GroupAwareEdit[names.length];
 		boolean committed = false;
 		try {
 			ResourcePropertiesEdit[] properties = new ResourcePropertiesEdit[names.length];
 			for ( int i = 0; i < names.length; i++ ) {
 				edit[i] = baseEdit;
 				properties[i] = baseProperties;
 			}
 			if ( items != null ) {
 				String repositoryPath = rp.getRepositoryPath();
 				if (!repositoryPath.endsWith("/"))
 				{
 					repositoryPath = repositoryPath + "/";
 				}
 				for ( int i = 0; i < items.length; i++ ) {
 					if ( items[i].length() > 0 ) {
 						String p = repositoryPath+items[i];
						edit[i] = editEntity(handler, null, p);
 						properties[i] = edit[i].getPropertiesEdit();
 					}
 				}
 			}
 	
 			for (int i = 0; i < names.length; i++)
 			{
 				
 				if ( log.isDebugEnabled() ) {
 					log.debug("Property  ref=["+edit[i].getId()+"] prop=["+names[i]+"] value=["+values[i]+"] action=["+actions[i]+"]");
 				}
 				if (ADD.equals(actions[i]))
 				{
 					List<?> p = properties[i].getPropertyList(names[i]);
 					if (p == null || p.size() == 0)
 					{
 						properties[i].addProperty(names[i], values[i]);
 					}
 					else if (p.size() == 1)
 					{
 						String value = properties[i].getProperty(names[i]);
 						properties[i].removeProperty(names[i]);
 						properties[i].addPropertyToList(names[i], value);
 						properties[i].addPropertyToList(names[i], values[i]);
 					}
 					else
 					{
 						properties[i].addPropertyToList(names[i], values[i]);
 					}
 				}
 				else if (REMOVE.equals(actions[i]))
 				{
 					properties[i].removeProperty(names[i]);
 				}
 				else if (REPLACE.equals(actions[i]))
 				{
 					properties[i].removeProperty(names[i]);
 					properties[i].addProperty(names[i], values[i]);
 				}
 	
 			}
 			Map<GroupAwareEdit , GroupAwareEdit> committedEdit = new HashMap<GroupAwareEdit, GroupAwareEdit>();
 			for ( int i = 0; i < edit.length; i++) {
 				log.info("Comitting "+edit[i].getId());
 				if (!committedEdit.containsKey(edit[i]) ) {
 					commitEntity(edit[i]);
 					committedEdit.put(edit[i], edit[i]);
 				}
 			}
 			committed = true;
 		}finally {
 			if ( !committed )
 			{
 				for ( int i = 0; i < edit.length; i++) 
 				{
 					cancelEntity(edit[i]);
 				}				
 			}
 		}
 
 		CHSNodeMap nm = new CHSNodeMap((ContentEntity) baseEdit, rp.getDepth(), rp);
 		try
 		{
 			handler.sendMap(request, response, nm);
 		}
 		catch (IOException e)
 		{
 			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"IO Error " + e.getMessage());
 		}
 
 	}
 
 
 
 }
