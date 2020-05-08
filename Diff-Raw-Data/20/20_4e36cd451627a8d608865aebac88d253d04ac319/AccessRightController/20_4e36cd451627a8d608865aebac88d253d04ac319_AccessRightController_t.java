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
 import org.infoglue.cms.entities.management.AccessRight;
 import org.infoglue.cms.entities.management.AccessRightVO;
 import org.infoglue.cms.entities.management.AccessVO;
 import org.infoglue.cms.entities.management.InterceptionPoint;
 import org.infoglue.cms.entities.management.InterceptionPointVO;
 import org.infoglue.cms.entities.management.impl.simple.AccessImpl;
 import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.security.InfoGlueRole;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.deliver.util.CacheController;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * This class is a helper class for the use case handle Accesss
  *
  * @author Mattias Bogeblad
  */
 
 public class AccessRightController extends BaseController
 {
 	
 	/**
 	 * Factory method
 	 */
 
 	public static AccessRightController getController()
 	{
 		return new AccessRightController();
 	}
 	
 	public AccessRight getAccessRightWithId(Integer accessRightId, Database db) throws SystemException, Bug
 	{
 		return (AccessRight) getObjectWithId(AccessRightImpl.class, accessRightId, db);
 	}
     
 	public AccessRightVO getAccessRightVOWithId(Integer accessRightId) throws SystemException, Bug
 	{
 		return (AccessRightVO) getVOWithId(AccessRightImpl.class, accessRightId);
 	}
   
 	public List getAccessRightVOList() throws SystemException, Bug
 	{
 		return getAllVOObjects(AccessRightImpl.class, "accessRightId");
 	}
 
 	public List getAccessRightVOList(Integer interceptionPointId, String parameters, String roleName) throws SystemException, Bug
 	{
 		List accessRightVOList = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 			
 			InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(interceptionPointId);
 			if(interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue())
 				accessRightVOList = toVOList(getAccessRightList(interceptionPointId, parameters, roleName, db));
 			else
 				accessRightVOList = toVOList(getAccessRightList(interceptionPointId, roleName, db));
 
 			CmsLogger.logInfo("accessRightVOList:" + accessRightVOList.size());
 			
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			CmsLogger.logInfo("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return accessRightVOList;	
 	}
 
 
 	public List getAccessRightList(String interceptionPointName, String parameters, String roleName, Database db) throws SystemException, Bug
 	{
 		List accessRightList = getAccessRightList(InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName).getId(), parameters, roleName, db);
 		
 		return accessRightList;		
 	}
 
 	public List getAccessRightList(Integer interceptionPointId, String parameters, String roleName, Database db) throws SystemException, Bug
 	{
 		List accessRightList = new ArrayList();
 		
 		try
 		{
 		    CmsLogger.logInfo("getAccessRightList(Integer interceptionPointId, String parameters, String roleName, Database db)");
 			CmsLogger.logInfo("interceptionPointId:" + interceptionPointId);
 			CmsLogger.logInfo("parameters:" + parameters);
 			CmsLogger.logInfo("roleName:" + roleName);
 			OQLQuery oql = null;
 			
 			if(parameters == null || parameters.length() == 0)
 			{
 				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2) AND f.roleName = $3");
 				oql.bind(interceptionPointId);
 				oql.bind(parameters);
 				oql.bind(roleName);
 			}
 			else
 			{
 		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2 AND f.roleName = $3");
 				oql.bind(interceptionPointId);
 				oql.bind(parameters);
 				oql.bind(roleName);
 			}
 			
 			QueryResults results = oql.execute();
 			
 			while (results.hasMore()) 
 			{
 				AccessRight accessRight = (AccessRight)results.next();
 				CmsLogger.logInfo("accessRight:" + accessRight.getAccessRightId());
 				accessRightList.add(accessRight);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of Access rights. Reason:" + e.getMessage(), e);    
 		}
 		
 		return accessRightList;		
 	}
 	
 	public List getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)  throws SystemException, Bug
 	{
 		List accessRightList = new ArrayList();
 		
 		try
 		{
 		    CmsLogger.logInfo("getAccessRightListForEntity(Integer interceptionPointId, String parameters, Database db)");
 			CmsLogger.logInfo("interceptionPointId:" + interceptionPointId);
 			CmsLogger.logInfo("parameters:" + parameters);
 			
 			OQLQuery oql = null;
 			
 			if(parameters == null || parameters.length() == 0)
 			{
 				oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND (is_undefined(f.parameters) OR f.parameters = $2)");
 				oql.bind(interceptionPointId);
 				oql.bind(parameters);
 			}
 			else
 			{
 		    	oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.parameters = $2");
 				oql.bind(interceptionPointId);
 				oql.bind(parameters);
 			}
 						
 			QueryResults results = oql.execute();
 			while (results.hasMore()) 
 			{
 				AccessRight accessRight = (AccessRight)results.next();
 				CmsLogger.logInfo("accessRight:" + accessRight.getAccessRightId());
 				accessRightList.add(accessRight);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
 		}
 		
 		return accessRightList;		
 	}
 	
 	
 	public List getAccessRightList(Integer interceptionPointId, Database db)  throws SystemException, Bug
 	{
 		List accessRightList = new ArrayList();
 		
 		try
 		{
 			CmsLogger.logInfo("getAccessRightList(Integer interceptionPointId, Database db)");
 			CmsLogger.logInfo("interceptionPointId: " + interceptionPointId);
 			
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1");
 			oql.bind(interceptionPointId);
 			
 			QueryResults results = oql.execute();
 			while (results.hasMore()) 
 			{
 				AccessRight accessRight = (AccessRight)results.next();
 				CmsLogger.logInfo("accessRight:" + accessRight.getAccessRightId());
 				accessRightList.add(accessRight);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
 		}
 		
 		return accessRightList;		
 	}
 
 	public List getAccessRightList(String roleName, Database db)  throws SystemException, Bug
 	{
 		List accessRightList = new ArrayList();
 		
 		try
 		{
 			CmsLogger.logInfo("getAccessRightList(String roleName, Database db)");
 			CmsLogger.logInfo("roleName: " + roleName);
 			
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.roleName = $1");
 			oql.bind(roleName);
 			
 			QueryResults results = oql.execute();
 			while (results.hasMore()) 
 			{
 				AccessRight accessRight = (AccessRight)results.next();
 				CmsLogger.logInfo("accessRight:" + accessRight.getAccessRightId());
 				accessRightList.add(accessRight);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
 		}
 		
 		return accessRightList;		
 	}
 	
 
 	public List getAccessRightList(Integer interceptionPointId, String roleName, Database db)  throws SystemException, Bug
 	{
 		List accessRightList = new ArrayList();
 		
 		try
 		{
 		    CmsLogger.logInfo("getAccessRightList(Integer interceptionPointId, String roleName, Database db)");
 			CmsLogger.logInfo("interceptionPointId: " + interceptionPointId);
 			CmsLogger.logInfo("roleName: " + roleName);
 			
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint = $1 AND f.roleName = $2");
 			oql.bind(interceptionPointId);
 			oql.bind(roleName);
 			
 			QueryResults results = oql.execute();
 			while (results.hasMore()) 
 			{
 				AccessRight accessRight = (AccessRight)results.next();
 				CmsLogger.logInfo("accessRight:" + accessRight.getAccessRightId());
 				accessRightList.add(accessRight);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch a list of Function. Reason:" + e.getMessage(), e);    
 		}
 		
 		return accessRightList;		
 	}
 	
 	/**
 	 * This method creates an access note.
 	 * 
 	 * @param accessRightVO
 	 * @param db
 	 * @return
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 	
 	public AccessRightVO create(AccessRightVO accessRightVO, InterceptionPoint interceptionPoint, Database db) throws SystemException, Exception
 	{
 		AccessRight accessRight = new AccessRightImpl();
 		accessRight.setValueObject(accessRightVO);
 		
 		accessRight.setInterceptionPoint(interceptionPoint);
 		
 		db.create(accessRight);
 					
 		return accessRight.getValueObject();
 	}     
 
 	
 	public AccessVO update(AccessVO AccessVO) throws ConstraintException, SystemException
 	{
 		return (AccessVO) updateEntity(AccessImpl.class, AccessVO);
 	}        
 
 	
 	public void update(/*String category, *//*Integer interceptionPointId,*/ String parameters, HttpServletRequest request) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		//CmsLogger.logInfo("category:" + category);
 		CmsLogger.logInfo("parameters:" + parameters);
 		
 		try 
 		{
 			beginTransaction(db);
 
 			int roleIndex = 0;
 			String roleName = request.getParameter(roleIndex + "_roleName");
 			while(roleName != null)
 			{
 				CmsLogger.logInfo("roleName:" + roleName);
 				
 				int interceptionPointIndex = 0;
 				String interceptionPointIdString = request.getParameter(roleName + "_" + interceptionPointIndex + "_InterceptionPointId");
 				while(interceptionPointIdString != null)
 				{
 					CmsLogger.logInfo("interceptionPointIdString:" + interceptionPointIdString);
 					
 					delete(new Integer(interceptionPointIdString), parameters, roleName, db);
 
 					String hasAccess = request.getParameter(roleName + "_" + interceptionPointIdString + "_hasAccess");
 					
 					if(hasAccess != null)
 					{
 						AccessRightVO accessRightVO = new AccessRightVO();
 						accessRightVO.setRoleName(roleName);
 						accessRightVO.setParameters(parameters);
 
 						InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithId(new Integer(interceptionPointIdString), db);
 						CmsLogger.logInfo("Creating access for roleName:" + roleName + ":" + parameters + "_" + interceptionPoint.getName());
 						
 						create(accessRightVO, interceptionPoint, db);
 					}
 					
 					interceptionPointIndex++;
 					interceptionPointIdString = request.getParameter(roleName + "_" + interceptionPointIndex + "_InterceptionPointId");
 				}
 								
 				roleIndex++;
 				roleName = request.getParameter(roleIndex + "_roleName");
 			}
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			CmsLogger.logInfo("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 	}			
 	
 	
 	/**
 	 * This method deletes all occurrencies of AccessRight which has the interceptionPointId.
 	 * 
 	 * @param roleName
 	 * @throws ConstraintException
 	 * @throws SystemException
 	 */
 
 	public void delete(String roleName) throws SystemException, Exception
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		CmsLogger.logInfo("roleName:" + roleName);
 		
 		try 
 		{
 			beginTransaction(db);
 
 			List accessRightList = getAccessRightList(roleName, db);
 			Iterator i = accessRightList.iterator();
 			while(i.hasNext())
 			{
 				AccessRight accessRight = (AccessRight)i.next();
 				db.remove(accessRight);
 			}
 			
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			CmsLogger.logInfo("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 	}        
 	
 	/**
 	 * This method deletes all occurrencies of AccessRight which has the interceptionPointId.
 	 * 
 	 * @param roleName
 	 * @throws ConstraintException
 	 * @throws SystemException
 	 */
 
 	public void delete(Integer interceptionPointId, String parameters, String roleName, Database db) throws SystemException, Exception
 	{
 		List accessRightList = getAccessRightList(interceptionPointId, parameters, roleName, db);
 		Iterator i = accessRightList.iterator();
 		while(i.hasNext())
 		{
 			AccessRight accessRight = (AccessRight)i.next();
 			db.remove(accessRight);
 		}
 		
 	}        
 
 	
 	
 	/**
 	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
 	 */
 	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String parameters) throws SystemException
 	{
 	    if(infoGluePrincipal == null)
 	        return false;
 	        
 		if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
 			return true;
 			
 		boolean isPrincipalAuthorized = false;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 			
 			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName, parameters);
 		
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			CmsLogger.logInfo("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 					
 		return isPrincipalAuthorized;
 	}
 	
 	
 	/**
 	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
 	 */
 	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters) throws SystemException
 	{		
 	    CmsLogger.logInfo("getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName, String extraParameters)");
 		CmsLogger.logInfo("infoGluePrincipal: " + infoGluePrincipal.getName());
 		CmsLogger.logInfo("interceptionPointName: " + interceptionPointName);
 		CmsLogger.logInfo("extraParameters: " + extraParameters);
 		
 		if(infoGluePrincipal != null && infoGluePrincipal.getIsAdministrator())
 			return true;
 		
 		String key = "cachedAccessRightsVOList";
 		CmsLogger.logInfo("key:" + key);
 		List cachedAccessRightsVOList = (List)CacheController.getCachedObject("authorizationCache", key);
 		if(cachedAccessRightsVOList == null)
 		{
 		    cachedAccessRightsVOList = this.getAccessRightVOList();
 		    CacheController.cacheObject("authorizationCache", key, cachedAccessRightsVOList);
 		}
 		
 		boolean isPrincipalAuthorized = false;
 		
 		Collection roles = infoGluePrincipal.getRoles();
 		CmsLogger.logInfo("roles:" + roles.size());
 			
 		Iterator i = roles.iterator();
 		outer:while(i.hasNext())
 		{
 			InfoGlueRole role = (InfoGlueRole)i.next();
			//CmsLogger.logInfo("role:" + role.getName());
 			
 			Iterator cachedAccessRightsVOListIterator = cachedAccessRightsVOList.iterator();
 			while(cachedAccessRightsVOListIterator.hasNext()) 
 			{
 			    AccessRightVO candidateAccessRightVO = (AccessRightVO)cachedAccessRightsVOListIterator.next();
				//CmsLogger.logInfo("candidateAccessRight:" + candidateAccessRightVO.getRoleName() + ":" + candidateAccessRightVO.getInterceptionPointName() + ":" + candidateAccessRightVO.getParameters());
 				if(candidateAccessRightVO.getRoleName().equalsIgnoreCase(role.getName()) && candidateAccessRightVO.getInterceptionPointName().equalsIgnoreCase(interceptionPointName) && candidateAccessRightVO.getParameters().equalsIgnoreCase(extraParameters))
 				{
 				    isPrincipalAuthorized = true;
 					CmsLogger.logInfo("isPrincipalAuthorized:" + isPrincipalAuthorized);
 				    break outer;
 				}
 			}
 		}
 		
 		return isPrincipalAuthorized;
 	}
 		
 	/**
 	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
 	 */
 	public boolean getIsPrincipalAuthorized(InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws SystemException
 	{
 		if(infoGluePrincipal.getIsAdministrator())
 			return true;
 			
 		String key = "" + infoGluePrincipal.getName() + "_" + interceptionPointName;
 		CmsLogger.logInfo("key:" + key);
 		Boolean cachedIsPrincipalAuthorized = (Boolean)CacheController.getCachedObject("authorizationCache", key);
 		if(cachedIsPrincipalAuthorized != null)
 		{
 			CmsLogger.logInfo("There was an cached authorization:" + cachedIsPrincipalAuthorized);
 			return cachedIsPrincipalAuthorized.booleanValue();
 		}
 		
 		boolean isPrincipalAuthorized = false;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 			
 			isPrincipalAuthorized = getIsPrincipalAuthorized(db, infoGluePrincipal, interceptionPointName);
 			
 			CacheController.cacheObject("authorizationCache", key, new Boolean(isPrincipalAuthorized));
 			
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			CmsLogger.logInfo("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 					
 		return isPrincipalAuthorized;
 	}
 
 	/**
 	 * This method checks if a role has access to an entity. It takes name and id of the entity. 
 	 */
 	
 	public boolean getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName) throws SystemException
 	{		
 	    CmsLogger.logInfo("getIsPrincipalAuthorized(Database db, InfoGluePrincipal infoGluePrincipal, String interceptionPointName)");
 	    CmsLogger.logInfo("infoGluePrincipal:" + infoGluePrincipal);
 		CmsLogger.logInfo("interceptionPointName:" + interceptionPointName);
 		
 		if(infoGluePrincipal.getIsAdministrator())
 			return true;
 
 		String key = "cachedAccessRightsVOList";
 		CmsLogger.logInfo("key:" + key);
 		List cachedAccessRightsVOList = (List)CacheController.getCachedObject("authorizationCache", key);
 		if(cachedAccessRightsVOList == null)
 		{
 		    cachedAccessRightsVOList = this.getAccessRightVOList();
 		    CacheController.cacheObject("authorizationCache", key, cachedAccessRightsVOList);
 		}
 
 		boolean isPrincipalAuthorized = false;
 		
 		Collection roles = infoGluePrincipal.getRoles();
 			
 		Iterator i = roles.iterator();
 		outer:while(i.hasNext())
 		{
 			InfoGlueRole role = (InfoGlueRole)i.next();
 			CmsLogger.logInfo("role:" + role.getName());
 			Iterator cachedAccessRightsVOListIterator = cachedAccessRightsVOList.iterator();
 			while(cachedAccessRightsVOListIterator.hasNext()) 
 			{
 			    AccessRightVO candidateAccessRightVO = (AccessRightVO)cachedAccessRightsVOListIterator.next();
 				//CmsLogger.logInfo("candidateAccessRightVO role:" + candidateAccessRightVO.getRoleName());
 			    if(candidateAccessRightVO.getRoleName().equalsIgnoreCase(role.getName()) && candidateAccessRightVO.getInterceptionPointName().equalsIgnoreCase(interceptionPointName))
 				{
 				    isPrincipalAuthorized = true;
 				    break outer;
 				}
 			}
 
 			/*
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.AccessRightImpl f WHERE f.interceptionPoint.name = $1 AND f.roleName = $2");
 			oql.bind(interceptionPointName);
 			oql.bind(role.getName());
 
 			QueryResults results = oql.execute();
 			CmsLogger.logInfo("Anything:" + results.hasMore());
 			if (results.hasMore()) 
 			{
 				isPrincipalAuthorized = true;
 				break;
 			}
 			*/
 		}
 				
 		return isPrincipalAuthorized;
 	}
 	
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new AccessVO();
 	}
 
 }
  
