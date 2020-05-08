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
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.content.api.GroupAwareEdit;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.sdata.tool.CHSHandler;
 import org.sakaiproject.sdata.tool.SDataAccessException;
 import org.sakaiproject.sdata.tool.api.Handler;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.api.SDataFunction;
 import org.sakaiproject.time.api.TimeService;
 
 /**
  * Base SDataFunction for ContentHosting
  * 
  * @author ieb
  */
 public abstract class CHSSDataFunction implements SDataFunction
 {
 
 	private static final Log log = LogFactory.getLog(CHSSDataFunction.class);
 
 	protected ContentHostingService contentHostingService;
 
 	protected TimeService timeService;
 
 	public CHSSDataFunction()
 	{
 
 		contentHostingService = Kernel.contentHostingService();
 		timeService = Kernel.timeService();
 
 	}
 
 	/**
 	 * Get a ContentEntity relevant to the request
 	 * 
 	 * @param h
 	 *        THe Handler, which is expected to be a CHSHandler
 	 * @param repositoryPath
 	 *        the path into the repository
 	 * @return the ContentEntity specified, may return null if handler was not a
 	 *         CHSHandler
 	 * @throws SDataAccessException
 	 * @throws PermissionException
 	 */
 	protected ContentEntity getEntity(Handler h, String repositoryPath)
 			throws SDataAccessException, PermissionException
 	{
 		if (h instanceof CHSHandler)
 		{
 			CHSHandler chsHandler = (CHSHandler) h;
 			return chsHandler.getEntity(repositoryPath);
 		}
 		log.warn("A CHSDataFunction should only be invoked by a CHSHandler, handler was "
 				+ h);
 		return null;
 	}
 
 	/**
 	 * Get a GroupAwareEdit object based on the target
 	 * 
 	 * @param handler
 	 *        the Handler, expected to be a CHSHandler
 	 * @param target
 	 *        the target object that might already be suitable, if so it will be
 	 *        used.
 	 * @param repositoryPath
 	 *        The path into the repository where the content entity is located
 	 * @return The edit object, however may return null if one can't be found.
 	 * @throws SDataException
 	 *         thrown if there is a problem, the status code will indicate the
 	 *         type of problem.
 	 */
 	protected GroupAwareEdit editEntity(Handler handler, Object target,
 			String repositoryPath) throws SDataException
 	{
 		
 		if (target instanceof GroupAwareEdit && ((GroupAwareEdit) target).isActiveEdit())
 		{
 			return (GroupAwareEdit) target;
 		}
 		try
 		{
 			if (target instanceof ContentCollection)
 			{
 				ContentCollection cc = (ContentCollection) target;
 				return contentHostingService.editCollection(cc.getId());
 			}
 			if (target instanceof ContentResource)
 			{
 				ContentResource cc = (ContentResource) target;
 				return contentHostingService.editResource(cc.getId());
 			}
 
 			// so we have no idea what this entity is, try all the available
 			// methods.
 
 			GroupAwareEdit ce = null;
 			try
 			{
 				ce = contentHostingService.editResource(repositoryPath);
 			}
 			catch (IdUnusedException e)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Resource Not Found " + repositoryPath + " " + e);
 				}
 			}
 			catch (TypeException e)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Resource Not Found " + repositoryPath + " " + e);
 				}
 			}
 			catch ( PermissionException e ) 
 			{
 				// this can happen if the id points to a content entity and doesnt include a tailing /
 				if (log.isDebugEnabled())
 				{
 					log.debug("Resource Not Found " + repositoryPath + " " + e);
 				}	
 			}
 			if (ce == null)
 			{
 				if (!repositoryPath.endsWith("/"))
 				{
 					repositoryPath = repositoryPath + "/";
 				}
 				try
 				{
 					ce = contentHostingService.editCollection(repositoryPath);
 				}
 				catch (IdUnusedException e)
 				{
 					if (log.isDebugEnabled())
 					{
 						log.debug("Collection Not Found " + repositoryPath + " " + e);
 					}
 				}
 				catch (TypeException e)
 				{
 					if (log.isDebugEnabled())
 					{
 						log.debug("Collection Not Found " + repositoryPath + " " + e);
 					}
 				}
 			}
 			// check that we can see it, just in case
 			if (ce != null)
 			{
 				String lock = ContentHostingService.AUTH_RESOURCE_HIDDEN;
 				boolean canSeeHidden = Kernel.securityService().unlock(lock,
 						ce.getReference());
 				if (!canSeeHidden && !ce.isAvailable())
 				{
 					log.info(" Hidden Entity "+repositoryPath);
 					throw new SDataAccessException(HttpServletResponse.SC_FORBIDDEN,
 							"Permission denied on item");
 				}
 
 			} else {
 				log.info(" Null Entity "+ce.getId()+" "+repositoryPath);
 				
 			}
 
 			return ce;
 
 		}
 		catch (IdUnusedException e)
 		{
 			logException("Entity Not found ", e);
 			throw new SDataException(HttpServletResponse.SC_NOT_FOUND,
 					"Item does not exist");
 		}
 		catch (TypeException e)
 		{
 			logException("Type missmatch ", e);
 			throw new SDataException(HttpServletResponse.SC_NOT_FOUND,
 					"Item does not exist");
 		}
 		catch (PermissionException e)
 		{
 			logException("Permissions Deined trying to access an entity ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
 					"Permission Denied");
 		}
 		catch (InUseException e)
 		{
 			logException("Id Already in use ", e);
 			throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
 					"Permission Denied");
 		}
 
 	}
 
 	/**
 	 * @param string
 	 * @param e
 	 */
 	protected void logException(String string, Exception e)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.warn("Type missmatch ", e);
 		}
 		else
 		{
 			log.warn("Type missmatch " + e.getMessage());
 			log.warn("Type missmatch ", e);
 		}
 	}
 
 	/**
 	 * @param edit
 	 * @throws SDataException
 	 */
	protected void commitEntity(GroupAwareEdit edit) throws SDataException
 	{
 		if (edit instanceof ContentCollectionEdit)
 		{
 			contentHostingService.commitCollection((ContentCollectionEdit) edit);
 		}
 		else if (edit instanceof ContentResourceEdit)
 		{
 			try
 			{
				contentHostingService.commitResource((ContentResourceEdit) edit);
 			}
 			catch (OverQuotaException e)
 			{
 				log.warn("Over Quota Exception on commit " + e.getMessage());
 				throw new SDataException(HttpServletResponse.SC_PAYMENT_REQUIRED,
 						"Over quota");
 			}
 			catch (ServerOverloadException e)
 			{
 				log.warn("Server Overload on commit " + e.getMessage());
 				throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"Cant set hidden status");
 			}
 
 		}
 		else
 		{
 			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Cant set hidden status, unknown entity type");
 
 		}
 	}
 	
 	protected void cancelEntity(GroupAwareEdit edit) throws SDataException {
 		if (edit instanceof ContentCollectionEdit)
 		{
 			contentHostingService.cancelCollection((ContentCollectionEdit) edit);
 		}
 		else if (edit instanceof ContentResourceEdit)
 		{
 				contentHostingService.cancelResource((ContentResourceEdit) edit);
 		}
 		else
 		{
 			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Cant set hidden status, unknown entity type");
 
 		}
 	}
 
 
 	
 	public void destroy() 
 	{
 	}
 
 
 }
