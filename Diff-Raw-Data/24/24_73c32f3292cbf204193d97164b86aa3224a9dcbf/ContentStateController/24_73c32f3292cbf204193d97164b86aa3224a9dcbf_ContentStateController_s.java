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
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import org.infoglue.cms.entities.kernel.*;
 import org.infoglue.cms.entities.management.AccessRight;
 import org.infoglue.cms.entities.management.AccessRightVO;
 import org.infoglue.cms.entities.management.InterceptionPoint;
 import org.infoglue.cms.entities.content.*;
 import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
 import org.infoglue.cms.entities.workflow.*;
 
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.PersistenceException;
 
 public class ContentStateController extends BaseController 
 {
     private final static Logger logger = Logger.getLogger(ContentStateController.class.getName());
 
 	public static final ContentCategoryController contentCategoryController = ContentCategoryController.getController();
 	
 	public static final int OVERIDE_WORKING = 1;
 	public static final int LEAVE_WORKING   = 2;
 	
 	/**
 	 * This method handles versioning and state-control of content.
 	 * Se inline documentation for further explainations.
 	 */
 	
     public static ContentVersion changeState(Integer oldContentVersionId, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Integer contentId, List resultingEvents) throws ConstraintException, SystemException
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 		
 		ContentVersion newContentVersion = null; 
 		
         beginTransaction(db);
 		try
 		{
 			newContentVersion = changeState(oldContentVersionId, stateId, versionComment, infoGluePrincipal, contentId, db, resultingEvents);
         	commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }    	    	
     	
     	return newContentVersion;
     }        
 
 
 	/**
 	 * This method handles versioning and state-control of content.
 	 * Se inline documentation for further explainations.
 	 */
 	
 	public static ContentVersion changeState(Integer oldContentVersionId, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Integer contentId, Database db, List resultingEvents) throws SystemException
 	{
 		ContentVersion newContentVersion = null;
 
 		try
 		{
 			ContentVersion oldContentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(oldContentVersionId, db);
 
 			if (contentId == null)
 				contentId = new Integer(oldContentVersion.getOwningContent().getContentId().intValue());
 
 			//Here we create a new version if it was a state-change back to working, it's a copy of the publish-version
 			if (stateId.intValue() == ContentVersionVO.WORKING_STATE.intValue())
 			{
 				logger.info("About to create a new working version");
 
 				ContentVersionVO newContentVersionVO = new ContentVersionVO();
 				newContentVersionVO.setStateId(stateId);
 				newContentVersionVO.setVersionComment("New working version");
 				newContentVersionVO.setModifiedDateTime(new Date());
 				newContentVersionVO.setVersionModifier(infoGluePrincipal.getName());
 				newContentVersionVO.setVersionValue(oldContentVersion.getVersionValue());
 				newContentVersion = ContentVersionController.getContentVersionController().create(contentId, oldContentVersion.getLanguage().getLanguageId(), newContentVersionVO, oldContentVersion.getContentVersionId(), db);
 				
 				//ContentVersionController.getContentVersionController().copyDigitalAssets(oldContentVersion, newContentVersion, db);
 				copyAccessRights(oldContentVersion, newContentVersion, db);
 				copyContentCategories(oldContentVersion, newContentVersion, db);
 			}
 
 			//If the user changes the state to publish we create a copy and set that copy to publish.
 			if (stateId.intValue() == ContentVersionVO.PUBLISH_STATE.intValue())
 			{
 				logger.info("About to copy the working copy to a publish-one");
 
 				//First we update the old working-version so it gets a comment
 				oldContentVersion.setVersionComment(versionComment);
 
 				//Now we create a new version which is basically just a copy of the working-version
 				ContentVersionVO newContentVersionVO = new ContentVersionVO();
 				newContentVersionVO.setStateId(stateId);
 				newContentVersionVO.setVersionComment(versionComment);
 				newContentVersionVO.setModifiedDateTime(new Date());
 				newContentVersionVO.setVersionModifier(infoGluePrincipal.getName());
 				newContentVersionVO.setVersionValue(oldContentVersion.getVersionValue());
 				newContentVersion = ContentVersionController.getContentVersionController().create(contentId, oldContentVersion.getLanguage().getLanguageId(), newContentVersionVO, oldContentVersion.getContentVersionId(), db);
 				
 				//ContentVersionController.getContentVersionController().copyDigitalAssets(oldContentVersion, newContentVersion, db);
 				copyAccessRights(oldContentVersion, newContentVersion, db);
 				copyContentCategories(oldContentVersion, newContentVersion, db);
 
 				//Creating the event that will notify the editor...
 				EventVO eventVO = new EventVO();
 				eventVO.setDescription(newContentVersion.getVersionComment());
 				eventVO.setEntityClass(ContentVersion.class.getName());
 				eventVO.setEntityId(new Integer(newContentVersion.getId().intValue()));
 				eventVO.setName(newContentVersion.getOwningContent().getName());
 				eventVO.setTypeId(EventVO.PUBLISH);
 				eventVO = EventController.create(eventVO, newContentVersion.getOwningContent().getRepository().getId(), infoGluePrincipal, db);
 				resultingEvents.add(eventVO);
 			}
 
 			//If the user in the publish-app publishes a publish-version we change state to published.
 			if (stateId.intValue() == ContentVersionVO.PUBLISHED_STATE.intValue())
 			{
 				logger.info("About to publish an existing version");
 				oldContentVersion.setStateId(stateId);
 				oldContentVersion.setIsActive(new Boolean(true));
 				newContentVersion = oldContentVersion;
 			}
 
 		}
 		catch (Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
 
 		return newContentVersion;
 	}
 
 
 	/**
 	 * This method assigns the same access rights as the old content-version has.
 	 */
 	
 	private static void copyAccessRights(ContentVersion originalContentVersion, ContentVersion newContentVersion, Database db) throws ConstraintException, SystemException, Exception
 	{
 		List interceptionPointList = InterceptionPointController.getController().getInterceptionPointList("ContentVersion", db);
 		logger.info("interceptionPointList:" + interceptionPointList.size());
 		Iterator interceptionPointListIterator = interceptionPointList.iterator();
 		while(interceptionPointListIterator.hasNext())
 		{
 			InterceptionPoint interceptionPoint = (InterceptionPoint)interceptionPointListIterator.next();
 			List accessRightList = AccessRightController.getController().getAccessRightListForEntity(interceptionPoint.getId(), originalContentVersion.getId().toString(), db);
 			logger.info("accessRightList:" + accessRightList.size());
 			Iterator accessRightListIterator = accessRightList.iterator();
 			while(accessRightListIterator.hasNext())
 			{
 				AccessRight accessRight = (AccessRight)accessRightListIterator.next();
 				logger.info("accessRight:" + accessRight.getId());
 				
 				AccessRightVO copiedAccessRight = accessRight.getValueObject().createCopy(); //.getValueObject();
 				copiedAccessRight.setParameters(newContentVersion.getId().toString());
 				AccessRightController.getController().create(copiedAccessRight, interceptionPoint, db);
 			}
 		}
 	}	
 
 	/**
 	 * Makes copies of the ContentCategories for the old ContentVersion so the new ContentVersion
 	 * still has references to them.
 	 *
 	 * @param originalContentVersion
 	 * @param newContentVersion
 	 * @param db The Database to use
 	 * @throws SystemException If an error happens
 	 */
 	private static void copyContentCategories(ContentVersion originalContentVersion, ContentVersion newContentVersion, Database db) throws SystemException, PersistenceException
 	{
		List orignals = contentCategoryController.findByContentVersion(originalContentVersion.getId());
 		for (Iterator iter = orignals.iterator(); iter.hasNext();)
 		{
 			ContentCategoryVO vo = (ContentCategoryVO)iter.next();
 			vo.setContentVersionId(newContentVersion.getId());
 			contentCategoryController.createWithDatabase(vo, db);
 		}
 	}
 
 	/**
 	 * This method should never be called.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return null;
 	}
 
 }
  
