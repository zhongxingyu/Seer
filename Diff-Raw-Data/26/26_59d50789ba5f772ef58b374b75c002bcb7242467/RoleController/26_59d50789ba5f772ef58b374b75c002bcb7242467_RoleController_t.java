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
 import org.infoglue.cms.entities.management.Role;
 import org.infoglue.cms.entities.management.RoleVO;
 import org.infoglue.cms.entities.management.SystemUser;
 import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 
 import org.exolab.castor.jdo.Database;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * RoleHelper.java
  * Created on 2002-aug-28 
  * @author Stefan Sik, ss@frovi.com 
  * 
  * This class is a helper class for the use case handle roles
  */
 public class RoleController extends BaseController
 {
 
 	/**
 	 * Factory method
 	 */
 
 	public static RoleController getController()
 	{
 		return new RoleController();
 	}
 	
     public Role getRoleWithId(Integer roleId, Database db) throws SystemException, Bug
     {
 		return (Role) getObjectWithId(RoleImpl.class, roleId, db);
     }
 
 	public Role getRoleWithName(String roleName, Database db) throws SystemException, Bug
 	{
 		return (Role)getObjectWithId(RoleImpl.class, roleName, db);
 	}
     
     /*
     public static List getRoleVOList(Database db) throws SystemException, Bug
     {
         return getAllVOObjects(RoleImpl.class, db);
     }
 	*/
 	
     public RoleVO getRoleVOWithId(Integer roleId) throws SystemException, Bug
     {
 		return (RoleVO) getVOWithId(RoleImpl.class, roleId);
     }
 
 	public RoleVO getRoleVOWithId(String roleName) throws SystemException, Bug
 	{
 		return (RoleVO) getVOWithId(RoleImpl.class, roleName);
 	}
 
	public RoleVO getRoleVOWithId(String roleName, Database db) throws SystemException, Bug
	{
		return (RoleVO) getVOWithId(RoleImpl.class, roleName, db);
	}

     // Simple, without db
 	/*
     public static Role getRoleWithId(Integer roleId) throws SystemException, Bug
     {
 		return (Role) getObjectWithId(RoleImpl.class, roleId);
     }
     */
     
     public List getRoleVOList() throws SystemException, Bug
     {
         return getAllVOObjects(RoleImpl.class, "roleName");
     }
 
     public List getRoleVOList(Database db) throws SystemException, Bug
     {
         return getAllVOObjects(RoleImpl.class, "roleName", db);
     }
 
     public RoleVO create(RoleVO roleVO) throws ConstraintException, SystemException
     {
         Role role = new RoleImpl();
         role.setValueObject(roleVO);
         role = (Role) createEntity(role);
         return role.getValueObject();
     }     
 
     public void delete(RoleVO roleVO) throws ConstraintException, SystemException
     {
     	deleteEntity(RoleImpl.class, roleVO.getRoleName());
     }        
 
 	public void delete(String roleName) throws ConstraintException, SystemException
 	{
 		deleteEntity(RoleImpl.class, roleName);
 	}        
 
 	// Get list of users accosiated with this role
 	public List getRoleSystemUserVOList(String userName, Database db)  throws SystemException, Bug
 	{
 		Collection systemUsers = null;
 		List systemUsersVO = new ArrayList();
 		Role role = null;
 		
 		try 
 		{
 			role = getRoleWithName(userName, db);
 			systemUsers = role.getSystemUsers();		
 			
 			Iterator it = systemUsers.iterator();
 			while (it.hasNext())
 			{
 				SystemUser systemUser = (SystemUser) it.next();
 				systemUsersVO.add(systemUser.getValueObject());
 			}
 		}
 		catch( Exception e)		
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
 		}
 		
 		return systemUsersVO;		
 	}
 
 	public List getRoleSystemUserVOList(String roleName)  throws SystemException, Bug
 	{
 		List systemUsersVO = null;
 		Database db = CastorDatabaseService.getDatabase();
 		try
 		{
 			beginTransaction(db);
 			
 			systemUsersVO = getRoleSystemUserVOList(roleName, db);
 			
 			commitTransaction(db);
 		}
 		catch ( Exception e )
 		{
 			rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
 		}		
 		return systemUsersVO;
 	}
 
     public RoleVO update(RoleVO roleVO) throws ConstraintException, SystemException
     {
     	return (RoleVO) updateEntity(RoleImpl.class, (BaseEntityVO) roleVO);
     }        
 
 
     public RoleVO update(RoleVO roleVO, String[] systemUsers) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         Role role = null;
 
         beginTransaction(db);
 
         try
         {
             //add validation here if needed
 			role = getRoleWithName(roleVO.getRoleName(), db);
 			role.getSystemUsers().clear();
 			
    			if(systemUsers != null)
 			{
 				for (int i=0; i < systemUsers.length; i++)
 	            {
 	        		SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(systemUsers[i], db);
 	        		
 	            	role.getSystemUsers().add(systemUser);
 					systemUser.getRoles().add(role);
 	            }
 			}
            	
             role.setValueObject(roleVO);
 
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
 
         return role.getValueObject();
     }        
 
 	
 	/**
 	 * This method gets a list of Roles for a particular systemUser.
 	 * @param systemUserId
 	 * @return
 	 * @throws SystemException
 	 * @throws Bug
 	 */
 	
 	public List getRoleVOList(String userName)  throws SystemException, Bug
 	{
 		List roleVOList = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 		try
 		{
 			beginTransaction(db);
 			
 			SystemUser systemUser = SystemUserController.getController().getSystemUserWithName(userName, db);
 			roleVOList = toVOList(systemUser.getRoles());
 			
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch a list of users in this role. Reason:" + e.getMessage(), e);			
 		}		
 		
 		return roleVOList;
 	}
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new RoleVO();
 	}
 
 }
  
