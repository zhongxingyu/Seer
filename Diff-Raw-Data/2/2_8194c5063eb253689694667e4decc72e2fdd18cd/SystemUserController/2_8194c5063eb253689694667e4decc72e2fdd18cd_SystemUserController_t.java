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
 
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.Group;
 import org.infoglue.cms.entities.management.Role;
 import org.infoglue.cms.entities.management.SystemUser;
 import org.infoglue.cms.entities.management.SystemUserVO;
 import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.util.PasswordGenerator;
 import org.infoglue.cms.util.mail.MailServiceFactory;
 
 /**
  * SystemUserController.java
  * Created on 2002-aug-28 
  * @author Stefan Sik, ss@frovi.com 
  * 
  * This class is a helper class for the use case handle SystemUsers
  * 
  */
 public class SystemUserController extends BaseController
 {
     private final static Logger logger = Logger.getLogger(SystemUserController.class.getName());
 
 	/**
 	 * Factory method
 	 */
 
 	public static SystemUserController getController()
 	{
 		return new SystemUserController();
 	}
 	
 	/*
     public static SystemUser getSystemUserWithId(Integer systemUserId, Database db) throws SystemException, Bug
     {
 		return (SystemUser) getObjectWithId(SystemUserImpl.class, systemUserId, db);
     }
     
     public SystemUserVO getSystemUserVOWithId(Integer systemUserId) throws SystemException, Bug
     {
 		return (SystemUserVO) getVOWithId(SystemUserImpl.class, systemUserId);
     }
 	*/
 
 	public SystemUserVO getSystemUserVOWithName(String name)  throws SystemException, Bug
 	{
 		SystemUserVO systemUserVO = null;
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			systemUserVO = getReadOnlySystemUserWithName(name, db).getValueObject();
 
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			logger.info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return systemUserVO;		
 	}	
 	
 	
 	
 	/**
 	 * 	Get the SystemUser with the userName
 	 */
 	 
 	public SystemUser getReadOnlySystemUserWithName(String userName, Database db)  throws SystemException, Bug
 	{
 		SystemUser systemUser = null;
         OQLQuery	oql;
         try
         {										
         	oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
         	oql.bind(userName);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	systemUser = (SystemUser)results.next();
             	logger.info("found one:" + systemUser.getFirstName());
             }
         }
         catch(Exception e)
         {
             throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);    
         }    
 
 		return systemUser;		
 	}
 
 
 	/**
 	 * 	Get the SystemUser with the userName
 	 */
 	 
 	public SystemUser getSystemUserWithName(String userName, Database db)  throws SystemException, Bug
 	{
 		SystemUser systemUser = null;
         OQLQuery oql;
         try
         {					
         	oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1");
         	oql.bind(userName);
         	
         	QueryResults results = oql.execute();
     		this.logger.info("Fetching entity in read/write mode" + userName);
 
 			if (results.hasMore()) 
             {
             	systemUser = (SystemUser)results.next();
             	logger.info("found one:" + systemUser.getFirstName());
             }
         }
         catch(Exception e)
         {
             throw new SystemException("An error occurred when we tried to fetch " + userName + " Reason:" + e.getMessage(), e);    
         }    
 
 		return systemUser;		
 	}
 
 
 
 	public SystemUserVO getSystemUserVO(String userName, String password)  throws SystemException, Bug
 	{
 		SystemUserVO systemUserVO = null;
 
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			systemUserVO = getSystemUserVO(db, userName, password);
 
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			logger.info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return systemUserVO;		
 	}	
 
 
 	public SystemUserVO getSystemUserVO(Database db, String userName, String password)  throws SystemException, Exception
 	{
 		SystemUserVO systemUserVO = null;
 
 		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1 AND u.password = $2");
 		oql.bind(userName);
 		oql.bind(password);
     	
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
 		{
 			SystemUser systemUser = (SystemUser)results.next();
 			systemUserVO = systemUser.getValueObject();
 			logger.info("found one:" + systemUserVO.getFirstName());
 		}
 
 		results.close();
 		oql.close();
 
 		return systemUserVO;		
 	}	
     
 	public SystemUser getSystemUser(Database db, String userName, String password)  throws SystemException, Exception
 	{
 		SystemUser systemUser = null;
 		
 		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u WHERE u.userName = $1 AND u.password = $2");
 		oql.bind(userName);
 		oql.bind(password);
     	
 		QueryResults results = oql.execute();
 		this.logger.info("Fetching entity in read/write mode" + userName);
 
 		if (results.hasMore()) 
 		{
 			systemUser = (SystemUser)results.next();
 			logger.info("found one:" + systemUser.getFirstName());
 		}
 
 		results.close();
 		oql.close();
 
 		return systemUser;		
 	}	
 	
     public List getSystemUserVOList() throws SystemException, Bug
     {
         return getAllVOObjects(SystemUserImpl.class, "userName");
     }
 
     public List getSystemUserVOList(Database db) throws SystemException, Bug
     {
         return getAllVOObjects(SystemUserImpl.class, "userName", db);
     }
 
     public List getSystemUserList(Database db) throws SystemException, Bug
     {
         return getAllObjects(SystemUserImpl.class, "userName", db);
     }
     
 	public List getFilteredSystemUserVOList(String firstName, String lastName, String userName, String email, String[] roleNames) throws SystemException, Bug
 	{
 		List filteredList = new ArrayList();
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 								
 			filteredList = getFilteredSystemUserList(firstName, lastName, userName, email, roleNames, db);
 			
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			logger.info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException("An error occurred so we should not complete the transaction:" + e, e);
 		}
 		
 		return toVOList(filteredList);
 	}
 
 	public List getFilteredSystemUserList(String firstName, String lastName, String userName, String email, String[] roleNames, Database db) throws SystemException, Bug, Exception
 	{
 		List filteredList = new ArrayList();
 		
		OQLQuery oql = db.getOQLQuery( "SELECT u FROM org.infoglue.cms.entities.management.impl.simple.SystemUserImpl u ORDER BY u.userName");
     	
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while (results.hasMore()) 
 		{
 			SystemUser extranetUser = (SystemUser)results.next();
 			boolean include = true;
 			
 			if(firstName != null && !firstName.equals("") && extranetUser.getFirstName().toLowerCase().indexOf(firstName.toLowerCase()) == -1)
 				include = false;
 			
 			if(lastName != null && !lastName.equals("") && extranetUser.getLastName().toLowerCase().indexOf(lastName.toLowerCase()) == -1)
 				include = false;
 			
 			if(userName != null && !userName.equals("") && extranetUser.getUserName().toLowerCase().indexOf(userName.toLowerCase()) == -1)
 				include = false;
 			
 			if(email != null && !email.equals("") && extranetUser.getEmail().toLowerCase().indexOf(email.toLowerCase()) == -1)
 				include = false;
 			
 			boolean hasRoles = true;
 			if(roleNames != null && roleNames.length > 0)
 			{	
 				for(int i=0; i < roleNames.length; i++)
 				{
 					String roleName = roleNames[i];
 					if(roleName != null && !roleName.equals(""))
 					{	
 						Collection roles = extranetUser.getRoles();
 						Iterator rolesIterator = roles.iterator();
 						boolean hasRole = false;
 						while(rolesIterator.hasNext())
 						{
 							Role role = (Role)rolesIterator.next();
 							if(role.getRoleName().equalsIgnoreCase(roleName))
 							{
 								hasRole = true;
 								break;
 							}
 						}
 						
 						if(!hasRole)
 						{
 							hasRoles = false;
 							break;
 						}
 					}
 				}					
 			}
 			
 			if(include && hasRoles)
 				filteredList.add(extranetUser);
 		}
 		
 		results.close();
 		oql.close();
 
 		return filteredList;
 	}
 
 	/*
 	 * CREATE
 	 * 
 	 */
     public SystemUserVO create(SystemUserVO systemUserVO) throws ConstraintException, SystemException
     {
     	/*
     	try
     	{
 	    	MessageDigest md = MessageDigest.getInstance("MD5");
 	
 		    md.update(systemUserVO.getPassword().getBytes("utf-8"));
 		    String digestedPassword = new String(md.digest());
 		    systemUserVO.setPassword(digestedPassword);
     	}
     	catch(Exception e)
     	{
     		e.printStackTrace();
     	}
     	*/
     	
     	SystemUser systemUser = new SystemUserImpl();
         systemUser.setValueObject(systemUserVO);
         systemUser = (SystemUser) createEntity(systemUser);
         return systemUser.getValueObject();
     }     
 
 	/*
 	 * CREATE
 	 * 
 	 */
     public SystemUser create(SystemUserVO systemUserVO, Database db) throws ConstraintException, SystemException, Exception
     {
     	/*
     	try
     	{
 	    	MessageDigest md = MessageDigest.getInstance("MD5");
 	
 		    md.update(systemUserVO.getPassword().getBytes("utf-8"));
 		    String digestedPassword = new String(md.digest());
 		    systemUserVO.setPassword(digestedPassword);
     	}
     	catch(Exception e)
     	{
     		e.printStackTrace();
     	}
     	*/
 
     	SystemUser systemUser = new SystemUserImpl();
         systemUser.setValueObject(systemUserVO);
         systemUser = (SystemUser) createEntity(systemUser, db);
         return systemUser;
     }     
 
 	/*
 	 * DELETE
 	 * 
 	 */
 	 
     public void delete(String userName) throws ConstraintException, SystemException
     {
 		Database db = CastorDatabaseService.getDatabase();
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		SystemUser systemUser = null;
 
 		beginTransaction(db);
 
 		try
 		{
 			//add validation here if needed
 			
 		    delete(userName, db);
 			
 			//If any of the validations or setMethods reported an error, we throw them up now before create.
 			ceb.throwIfNotEmpty();
             
 			commitTransaction(db);
 		}
 		catch(ConstraintException ce)
 		{
 			logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
 			rollbackTransaction(db);
 			throw ce;
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not completes the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 
     }        
 
     /*
 	 * DELETE
 	 * 
 	 */
 	 
     public void delete(String userName, Database db) throws ConstraintException, SystemException, Exception
     {
 		SystemUser systemUser = getSystemUserWithName(userName, db);
 
 		Collection roles = systemUser.getRoles();
 		Iterator rolesIterator = roles.iterator();
 		while(rolesIterator.hasNext())
 		{
 			Role role = (Role)rolesIterator.next();
 			role.getSystemUsers().remove(systemUser);
 		}
 		
 		Collection groups = systemUser.getGroups();
 		Iterator groupsIterator = groups.iterator();
 		while(groupsIterator.hasNext())
 		{
 			Group group = (Group)groupsIterator.next();
 			group.getSystemUsers().remove(systemUser);
 		}
 		
 		db.remove(systemUser);
     }        
 
 
     public SystemUserVO update(SystemUserVO systemUserVO) throws ConstraintException, SystemException
     {
     	return (SystemUserVO) updateEntity(SystemUserImpl.class, (BaseEntityVO) systemUserVO);
     }        
 
 
     public SystemUserVO update(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         SystemUser systemUser = null;
 
         beginTransaction(db);
 
         try
         {
             systemUser = update(systemUserVO, roleNames, groupNames, db);
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             logger.warn("An error occurred so we should not completes the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return systemUser.getValueObject();
     }        
 
     public SystemUser update(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames, Database db) throws ConstraintException, SystemException
     {
         SystemUser systemUser = systemUser = getSystemUserWithName(systemUserVO.getUserName(), db);
 		systemUser.getRoles().clear();
 		
 		if(roleNames != null)
 		{
 			for (int i=0; i < roleNames.length; i++)
             {
             	Role role = RoleController.getController().getRoleWithName(roleNames[i], db);
             	systemUser.getRoles().add(role);
 				role.getSystemUsers().add(systemUser);
             }
 		}
 		
 		systemUser.getGroups().clear();
 		
 		if(groupNames != null)
 		{
 			for (int i=0; i < groupNames.length; i++)
             {
 			    Group group = GroupController.getController().getGroupWithName(groupNames[i], db);
             	systemUser.getGroups().add(group);
             	group.getSystemUsers().add(systemUser);
             }
 		}
 		
 		systemUserVO.setPassword(systemUser.getPassword());
 		systemUser.setValueObject(systemUserVO);
 
         return systemUser;
     }     
     
     public void updatePassword(String userName) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 
         try
         {
             updatePassword(userName, db);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     }        
 
     public void updatePassword(String userName, Database db) throws ConstraintException, SystemException
     {
         SystemUser systemUser = getSystemUserWithName(userName, db);
         
         String newPassword = PasswordGenerator.generate();
         
         systemUser.setPassword(newPassword);
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("CMS notification: You or an administrator have requested a new password for your account (" + userName + "). \n");
 		sb.append("\n");
 		sb.append("The new password is '" + newPassword + "'.\n");
 		sb.append("\n");
 		sb.append("Please notify the administrator if this does not work. \n");
 		sb.append("\n");
 		sb.append("-----------------------------------------------------------------------\n");
 		sb.append("This email was automatically generated and the sender is the CMS-system. \n");
 		sb.append("Do not reply to this email. \n");
 		
 		try
 		{
 			String systemEmailSender = CmsPropertyHandler.getSystemEmailSender();
 			if(systemEmailSender == null || systemEmailSender.equalsIgnoreCase(""))
 				systemEmailSender = "InfoGlueCMS@" + CmsPropertyHandler.getMailSmtpHost();
 	
 			MailServiceFactory.getService().send(systemEmailSender, systemUser.getEmail(), null, "InfoGlue Information - Password changed!!", sb.toString());
 		}
 		catch(Exception e)
 		{
 			logger.error("The notification was not sent. Reason:" + e.getMessage(), e);
 		}
     }        
 
     public void updatePassword(String userName, String oldPassword, String newPassword) throws ConstraintException, SystemException
     {
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 
         try
         {
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
 
             updatePassword(userName, oldPassword, newPassword, db);
             
             commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     }    
     
     public void updatePassword(String userName, String oldPassword, String newPassword, Database db) throws ConstraintException, SystemException, Exception
     {
         if(newPassword == null)
             throw new ConstraintException("SystemUser.newPassword", "301");
 
         SystemUser systemUser = getSystemUser(db, userName, oldPassword);
         if(systemUser == null)
             throw new ConstraintException("SystemUser.oldPassword", "310");
             
         systemUser.setPassword(newPassword);		
     }        
 
   
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new SystemUserVO();
 	}
 
 }
  
