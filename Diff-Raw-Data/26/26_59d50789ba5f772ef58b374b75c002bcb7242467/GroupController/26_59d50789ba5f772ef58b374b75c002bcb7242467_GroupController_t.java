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
 
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.Group;
 import org.infoglue.cms.entities.management.GroupVO;
 import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RoleVO;
 import org.infoglue.cms.entities.management.SystemUser;
 import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.deliver.util.CacheController;
 
 
 import org.exolab.castor.jdo.Database;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * GroupHelper.java
  * Created on 2002-aug-28 
  * @author Stefan Sik, ss@frovi.com 
  * 
  * This class is a helper class for the use case handle groups
  */
 public class GroupController extends BaseController
 {
 
 	/**
 	 * Factory method
 	 */
 
 	public static GroupController getController()
 	{
 		return new GroupController();
 	}
 	
     public Group getGroupWithId(Integer groupId, Database db) throws SystemException, Bug
     {
 		return (Group) getObjectWithId(GroupImpl.class, groupId, db);
     }
 
 	public Group getGroupWithName(String groupName, Database db) throws SystemException, Bug
 	{
 		return (Group)getObjectWithId(GroupImpl.class, groupName, db);
 	}
     
     /*
     public static List getGroupVOList(Database db) throws SystemException, Bug
     {
         return getAllVOObjects(GroupImpl.class, db);
     }
 	*/
 	
     public GroupVO getGroupVOWithId(Integer groupId) throws SystemException, Bug
     {
 		return (GroupVO) getVOWithId(GroupImpl.class, groupId);
     }
 
 	public GroupVO getGroupVOWithId(String groupName) throws SystemException, Bug
 	{
 		return (GroupVO) getVOWithId(GroupImpl.class, groupName);
 	}
 
	public GroupVO getGroupVOWithId(String groupName, Database db) throws SystemException, Bug
	{
		return (GroupVO) getVOWithId(GroupImpl.class, groupName, db);
	}

     // Simple, without db
 	/*
     public static Group getGroupWithId(Integer groupId) throws SystemException, Bug
     {
 		return (Group) getObjectWithId(GroupImpl.class, groupId);
     }
     */
     
     public List getGroupVOList() throws SystemException, Bug
     {
         return getAllVOObjects(GroupImpl.class, "groupName");
     }
 
     public List getGroupVOList(Database db) throws SystemException, Bug
     {
 		String cacheKey = "allGroupVO";
 		getLogger().info("cacheKey in getGroupVOList:" + cacheKey);
 		List groupVOList = (List)CacheController.getCachedObject("groupVOListCache", cacheKey);
 		if(groupVOList != null)
 		{
 			getLogger().info("There was an cached list of GroupVO:" + groupVOList.size());
 		}
 		else
 		{
 		    groupVOList = getAllVOObjects(GroupImpl.class, "groupName", db);
 			if(groupVOList != null)
 			    CacheController.cacheObject("groupVOListCache", cacheKey, groupVOList);
 		}
 		
 		return groupVOList;
 	}
 
     public GroupVO create(GroupVO groupVO) throws ConstraintException, SystemException
     {
         Group group = new GroupImpl();
         group.setValueObject(groupVO);
         group = (Group) createEntity(group);
         return group.getValueObject();
     }     
 
     public void delete(GroupVO groupVO) throws ConstraintException, SystemException
     {
     	deleteEntity(GroupImpl.class, groupVO.getGroupName());
     }        
 
 	public void delete(String groupName) throws ConstraintException, SystemException
 	{
 		deleteEntity(GroupImpl.class, groupName);
 	}        
 
 	// Get list of users accosiated with this group
 	public List getGroupSystemUserVOList(String userName, Database db)  throws SystemException, Bug
 	{
 		Collection systemUsers = null;
 		List systemUsersVO = new ArrayList();
 		Group group = null;
 		
 		try 
 		{
 			group = getGroupWithName(userName, db);
 			systemUsers = group.getSystemUsers();		
 			
 			Iterator it = systemUsers.iterator();
 			while (it.hasNext())
 			{
 				SystemUser systemUser = (SystemUser) it.next();
 				systemUsersVO.add(systemUser.getValueObject());
 			}
 		}
 		catch( Exception e)		
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
 		}
 		
 		return systemUsersVO;		
 	}
 
 	public List getGroupSystemUserVOList(String groupName)  throws SystemException, Bug
 	{
 		List systemUsersVO = null;
 		Database db = CastorDatabaseService.getDatabase();
 		try
 		{
 			beginTransaction(db);
 			
 			systemUsersVO = getGroupSystemUserVOList(groupName, db);
 			
 			commitTransaction(db);
 		}
 		catch ( Exception e )
 		{
 			rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
 		}		
 		return systemUsersVO;
 	}
 
     public GroupVO update(GroupVO groupVO) throws ConstraintException, SystemException
     {
     	return (GroupVO) updateEntity(GroupImpl.class, (BaseEntityVO) groupVO);
     }        
 
 
     public GroupVO update(GroupVO groupVO, String[] systemUsers) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         Group group = null;
 
         beginTransaction(db);
 
         try
         {
             //add validation here if needed
 			group = getGroupWithName(groupVO.getGroupName(), db);
 			group.getSystemUsers().clear();
 			
    			if(systemUsers != null)
 			{
 				for (int i=0; i < systemUsers.length; i++)
 	            {
 	        		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(systemUsers[i], db);
 	        		
 	            	group.getSystemUsers().add(systemUser);
 					systemUser.getGroups().add(group);
 	            }
 			}
            	
             group.setValueObject(groupVO);
 
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             getLogger().warn("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return group.getValueObject();
     }        
 
 	
 	/**
 	 * This method gets a list of Groups for a particular systemUser.
 	 * @param systemUserId
 	 * @return
 	 * @throws SystemException
 	 * @throws Bug
 	 */
 	
 	public List getGroupVOList(String userName)  throws SystemException, Bug
 	{
 		List groupVOList = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 		try
 		{
 			beginTransaction(db);
 			
 			SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
 			groupVOList = toVOList(systemUser.getGroups());
 			
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this group. Reason:" + e.getMessage(), e);			
 		}		
 		
 		return groupVOList;
 	}
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new GroupVO();
 	}
 
 }
  
