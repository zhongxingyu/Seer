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
 
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.kernel.*;
 import org.infoglue.cms.entities.management.AvailableServiceBinding;
 import org.infoglue.cms.entities.management.Language;
 import org.infoglue.cms.entities.management.RegistryVO;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.entities.structure.ServiceBinding;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
 
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Collections;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 
 public class SiteNodeVersionController extends BaseController 
 {
 	private final RegistryController registryController = RegistryController.getController();
 
 	/**
 	 * Factory method
 	 */
 
 	public static SiteNodeVersionController getController()
 	{
 		return new SiteNodeVersionController();
 	}
 	
     public SiteNodeVersionVO getSiteNodeVersionVOWithId(Integer siteNodeVersionId) throws SystemException, Bug
     {
 		return (SiteNodeVersionVO) getVOWithId(SiteNodeVersionImpl.class, siteNodeVersionId);
     }
    	
 	public SiteNodeVersion getSiteNodeVersionWithId(Integer siteNodeVersionId, Database db) throws SystemException, Bug
     {
 		return (SiteNodeVersion) getObjectWithId(SiteNodeVersionImpl.class, siteNodeVersionId, db);
     }
 
     public static SiteNodeVersion getSiteNodeVersionWithIdAsReadOnly(Integer siteNodeVersionId, Database db) throws SystemException, Bug
     {
 		return (SiteNodeVersion) getObjectWithIdAsReadOnly(SiteNodeVersionImpl.class, siteNodeVersionId, db);
     }
 
     public List getSiteNodeVersionVOList() throws SystemException, Bug
     {
         return getAllVOObjects(SiteNodeVersionImpl.class, "siteNodeVersionId");
     }
 
     public static void delete(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
     {
     	deleteEntity(SiteNodeVersionImpl.class, siteNodeVersionVO.getSiteNodeVersionId());
     }        
 
     /**
      * This method removes the siteNodeVersion and also all associated bindings.
      * @param siteNodeVersion
      * @param db
      * @throws ConstraintException
      * @throws SystemException
      */
     public void delete(SiteNodeVersion siteNodeVersion, Database db) throws ConstraintException, SystemException
     {
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		try
 		{
 			SiteNode siteNode = siteNodeVersion.getOwningSiteNode();
 		    Collection serviceBindings = siteNodeVersion.getServiceBindings();
 		    Iterator serviceBindingsIterator = serviceBindings.iterator();
 		    while(serviceBindingsIterator.hasNext())
 		    {
 		        ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
 		        serviceBindingsIterator.remove();
 		        db.remove(serviceBinding);
 		    }
 		    
 			siteNode.getSiteNodeVersions().remove(siteNodeVersion);
 			db.remove(siteNodeVersion);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
     }        
 	
 
 	/**
 	 * This method creates an initial siteNodeVersion for the siteNode sent in and within the transaction sent in.
 	 */
 	
 	public static SiteNodeVersion createInitialSiteNodeVersion(Database db, SiteNode siteNode, InfoGluePrincipal infoGluePrincipal) throws SystemException, Bug
 	{
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		SiteNodeVersion siteNodeVersion = null;
 
 		try
 		{
 			//SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db);
             
 			siteNodeVersion = new SiteNodeVersionImpl();
 			siteNodeVersion.setIsCheckedOut(new Boolean(false));
 			siteNodeVersion.setModifiedDateTime(new Date());
 			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
 			siteNodeVersion.setStateId(new Integer(0));
 			siteNodeVersion.setVersionComment("Initial version");
 			siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
 			siteNodeVersion.setVersionNumber(new Integer(1));
         	
 			db.create((SiteNodeVersion)siteNodeVersion);
 			
 			List siteNodeVersions = new ArrayList();
 			siteNodeVersions.add(siteNodeVersion);
 			siteNode.setSiteNodeVersions(siteNodeVersions);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
     	
 		return siteNodeVersion;		
 	}
 
 	/**
 	 * This method creates a new siteNodeVersion for the siteNode sent in.
 	 */
 	
 	public static SiteNodeVersion create(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO) throws SystemException, Bug
 	{
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	SiteNodeVersion siteNodeVersion = null;
 
         beginTransaction(db);
 
         try
         {
         	SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db);
             
         	siteNodeVersion = new SiteNodeVersionImpl();
         	siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
         	siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
         	siteNodeVersion.setValueObject(siteNodeVersionVO);
         	
         	//Remove later and use a lookup....
         	siteNodeVersion.setVersionNumber(new Integer(1));
         	
         	siteNodeVersion = (SiteNodeVersion)createEntity(siteNodeVersion, db);
             //commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             //rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return siteNodeVersion;		
 	}
 
 
 	/**
 	 * This method creates a new siteNodeVersion for the siteNode sent in.
 	 */
 	
 	public static SiteNodeVersion create(Integer siteNodeId, InfoGluePrincipal infoGluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws SystemException, Bug, Exception
 	{
     	SiteNodeVersion siteNodeVersion = null;
 
     	SiteNode siteNode = SiteNodeController.getSiteNodeWithId(siteNodeId, db);
         
     	siteNodeVersion = new SiteNodeVersionImpl();
     	siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
     	siteNodeVersion.setVersionModifier(infoGluePrincipal.getName());
     	siteNodeVersion.setValueObject(siteNodeVersionVO);
     	//Remove later and use a lookup....
     	siteNodeVersion.setVersionNumber(new Integer(1));
     	
     	db.create(siteNodeVersion);
     	
 		return siteNodeVersion;		
 	}
 
 
 	public SiteNodeVersionVO getLatestActiveSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	SiteNodeVersionVO siteNodeVersionVO = null;
 
         beginTransaction(db);
 
         try
         {
             SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(db, siteNodeId);
             if(siteNodeVersion != null)
                 siteNodeVersionVO = siteNodeVersion.getValueObject();
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return siteNodeVersionVO;
     }
 
 	public SiteNodeVersion getLatestActiveSiteNodeVersion(Database db, Integer siteNodeId) throws SystemException, Bug, Exception
     {
 	    SiteNodeVersion siteNodeVersion = null;
 	    
 	    OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.isActive = $2 ORDER BY cv.siteNodeVersionId desc");
 		oql.bind(siteNodeId);
 		oql.bind(new Boolean(true));
 		
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
 	    {
 	    	siteNodeVersion = (SiteNodeVersion)results.next();
         }
 
 		return siteNodeVersion;
     }
 
 	public SiteNodeVersionVO getLatestSiteNodeVersionVO(Integer siteNodeId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	SiteNodeVersionVO siteNodeVersionVO = null;
 
         beginTransaction(db);
 
         try
         {
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
         	oql.bind(siteNodeId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
             	CmsLogger.logInfo("found one:" + siteNodeVersion.getValueObject());
             	siteNodeVersionVO = siteNodeVersion.getValueObject();
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return siteNodeVersionVO;
     }
     
 	/**
 	 * This is a method used to get the latest site node version of a sitenode within a given transaction.
 	 */
 
 	public SiteNodeVersion getLatestSiteNodeVersion(Database db, Integer siteNodeId) throws SystemException, Bug
 	{
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
 		SiteNodeVersion siteNodeVersion = null;
 
 		try
 		{
 			OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 ORDER BY cv.siteNodeVersionId desc");
 			oql.bind(siteNodeId);
         	
 			QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
 			{
 				siteNodeVersion = (SiteNodeVersion)results.next();
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
     	
 		return siteNodeVersion;
 	}
 
     
     public SiteNodeVersionVO updateStateId(Integer siteNodeVersionId, Integer stateId, String versionComment, InfoGluePrincipal infoGluePrincipal, Integer siteNodeId) throws ConstraintException, SystemException
     {
     	SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
     	SiteNodeVersionVO returnVO = null;
     	
     	//Here we just updates the state if it's a publish-state-change.
     	if(stateId.intValue() == 2)
     	{    		
 	    	siteNodeVersionVO.setStateId(stateId);
 	    	siteNodeVersionVO.setVersionComment(versionComment);
 	    	returnVO = (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, siteNodeVersionVO);
     	}
     	    	
     	//Here we create a new version if it was a state-change back to working
     	if(stateId.intValue() == 0)
     	{
 			siteNodeVersionVO.setStateId(stateId);
 			siteNodeVersionVO.setVersionComment("");
 			create(siteNodeId, infoGluePrincipal, siteNodeVersionVO);
 			returnVO = getLatestSiteNodeVersionVO(siteNodeId);
     	}
     	
     	return returnVO;
     }        
 
 
 	public static void deleteVersionsForSiteNodeWithId(Integer siteNodeId) throws ConstraintException, SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 		List siteNodeVersions = new ArrayList();
         try
         {
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1");
         	oql.bind(siteNodeId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			while (results.hasMore()) 
             {
             	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
 				siteNodeVersions.add(siteNodeVersion.getValueObject());
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
 		Iterator i = siteNodeVersions.iterator();
 		while(i.hasNext())
 		{
 			SiteNodeVersionVO siteNodeVersionVO = (SiteNodeVersionVO)i.next();
 			delete(siteNodeVersionVO);
 		}    	
     }
 
 	/** 
 	 * This methods deletes all versions for the siteNode sent in
 	 */
 
 	public static void deleteVersionsForSiteNode(SiteNode siteNode, Database db) throws ConstraintException, SystemException, Bug, Exception
     {
        	Collection siteNodeVersions = Collections.synchronizedCollection(siteNode.getSiteNodeVersions());
        	Iterator siteNodeVersionIterator = siteNodeVersions.iterator();
 			
 		while (siteNodeVersionIterator.hasNext()) 
         {
         	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionIterator.next();
 			Collection serviceBindings = Collections.synchronizedCollection(siteNodeVersion.getServiceBindings());
 			Iterator serviceBindingIterator = serviceBindings.iterator();
 			while(serviceBindingIterator.hasNext())
 			{
 				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
 				serviceBindingIterator.remove();
 				db.remove(serviceBinding);
 			}
 	    	
 			CmsLogger.logInfo("Deleting siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
         	siteNodeVersionIterator.remove();
 			db.remove(siteNodeVersion);
         }		    	
     }
 
 
    	/**
 	 * This method returns a list with AvailableServiceBidningVO-objects which are available for the
 	 * siteNodeTypeDefinition sent in
 	 */
 	
 	public static List getServiceBindningVOList(Integer siteNodeVersionId) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         List serviceBindningVOList = null;
 
         beginTransaction(db);
 
         try
         {
 			SiteNodeVersion siteNodeVersion = getSiteNodeVersionWithIdAsReadOnly(siteNodeVersionId, db);
             Collection serviceBindningList = siteNodeVersion.getServiceBindings();
         	serviceBindningVOList = toVOList(serviceBindningList);
         	
             //If any of the validations or setMethods reported an error, we throw them up now before create.
             ceb.throwIfNotEmpty();
 			commitTransaction(db);
         }
         catch(ConstraintException ce)
         {
             CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + ce, ce);
             rollbackTransaction(db);
             throw ce;
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not complete the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
         return serviceBindningVOList;
 	}
 	
 	
 	public static SiteNodeVersion getLatestPublishedSiteNodeVersion(Integer siteNodeId) throws SystemException, Bug, Exception
     {
         SiteNodeVersion siteNodeVersion = null;
         
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
         try
         {        
 	        siteNodeVersion = getLatestPublishedSiteNodeVersion(siteNodeId, db);
 			
             commitTransaction(db);            
         }
         catch(Exception e)
         {
         	CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
             
 		return siteNodeVersion;
     }
 
 	
 	
 	public static SiteNodeVersion getLatestPublishedSiteNodeVersion(Integer siteNodeId, Database db) throws SystemException, Bug, Exception
     {
         SiteNodeVersion siteNodeVersion = null;
         
         OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1  AND cv.stateId = $2 AND cv.isActive = $3 ORDER BY cv.siteNodeVersionId desc");
     	oql.bind(siteNodeId);
     	oql.bind(SiteNodeVersionVO.PUBLISHED_STATE);
     	oql.bind(true);
     	
     	QueryResults results = oql.execute();
 		
 		if (results.hasMore()) 
         {
         	siteNodeVersion = (SiteNodeVersion)results.next();
         }
             
 		return siteNodeVersion;
     }
 
 
 	/**
 	 * This method returns the version previous to the one sent in.
 	 */
 	
 	public static SiteNodeVersionVO getPreviousSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	SiteNodeVersionVO siteNodeVersionVO = null;
 
         beginTransaction(db);
 
         try
         {           
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.siteNodeVersionId < $2 ORDER BY cv.siteNodeVersionId desc");
         	oql.bind(siteNodeId);
         	oql.bind(siteNodeVersionId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
             	CmsLogger.logInfo("found one:" + siteNodeVersion.getValueObject());
             	siteNodeVersionVO = siteNodeVersion.getValueObject();
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return siteNodeVersionVO;
     }
 
 
 	/**
 	 * This method returns the version previous to the one sent in.
 	 */
 	
 	public static SiteNodeVersionVO getPreviousActiveSiteNodeVersionVO(Integer siteNodeId, Integer siteNodeVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
     	SiteNodeVersionVO siteNodeVersionVO = null;
 
         beginTransaction(db);
 
         try
         {           
             OQLQuery oql = db.getOQLQuery( "SELECT cv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl cv WHERE cv.owningSiteNode.siteNodeId = $1 AND cv.isActive = $2 AND cv.siteNodeVersionId < $3 ORDER BY cv.siteNodeVersionId desc");
         	oql.bind(siteNodeId);
         	oql.bind(new Boolean(true));
         	oql.bind(siteNodeVersionId);
         	
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
             	CmsLogger.logInfo("found one:" + siteNodeVersion.getValueObject());
             	siteNodeVersionVO = siteNodeVersion.getValueObject();
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return siteNodeVersionVO;
     }
 
 	 
 	/**
 	 * Recursive methods to get all siteNodeVersions of a given state
 	 * under the specified parent siteNode including the given siteNode.
 	 */ 
 	
 	public List getSiteNodeVersionVOWithParentRecursive(Integer siteNodeId, Integer stateId) throws ConstraintException, SystemException
 	{
 		return getSiteNodeVersionVOWithParentRecursive(siteNodeId, stateId, new ArrayList());
 	}
 	
 	private List getSiteNodeVersionVOWithParentRecursive(Integer siteNodeId, Integer stateId, List resultList) throws ConstraintException, SystemException
 	{
 		SiteNodeVersionVO siteNodeVersionVO = getLatestSiteNodeVersionVO(siteNodeId);
 		if(siteNodeVersionVO.getStateId().intValue() == stateId.intValue())
 			resultList.add(siteNodeVersionVO);
 		
 		// Get the children of this sitenode and do the recursion
 		List childSiteNodeList = SiteNodeController.getController().getSiteNodeChildren(siteNodeId);
 		Iterator childSiteNodeListIterator = childSiteNodeList.iterator();
 		while(childSiteNodeListIterator.hasNext())
 		{
 			SiteNodeVO siteNodeVO = (SiteNodeVO)childSiteNodeListIterator.next();
 			getSiteNodeVersionVOWithParentRecursive(siteNodeVO.getId(), stateId, resultList);
 		}
 	
 		return resultList;
 	}
 
 	/**
 	 * Recursive methods to get all siteNodeVersions of a given state
 	 * under the specified parent siteNode including the given siteNode.
 	 */ 
 	
 	public List getPublishedSiteNodeVersionVOWithParentRecursive(Integer siteNodeId) throws ConstraintException, SystemException
 	{
 	    List publishedSiteNodeVersionVOList = new ArrayList();
 	    
 	    Database db = CastorDatabaseService.getDatabase();
 
 	    beginTransaction(db);
 
         try
         {
             SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
             List publishedSiteNodeVersions = new ArrayList();
             getPublishedSiteNodeVersionWithParentRecursive(siteNode, publishedSiteNodeVersions, db);
             publishedSiteNodeVersionVOList = toVOList(publishedSiteNodeVersions);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
         return publishedSiteNodeVersionVOList;
 	}
 	
 	private List getPublishedSiteNodeVersionWithParentRecursive(SiteNode siteNode, List resultList, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    SiteNodeVersion siteNodeVersion = getLatestPublishedSiteNodeVersion(siteNode.getId(), db);
 		if(siteNodeVersion != null)
 			resultList.add(siteNodeVersion);
 		
 		// Get the children of this sitenode and do the recursion
 		Collection childSiteNodeList = siteNode.getChildSiteNodes();
 		Iterator childSiteNodeListIterator = childSiteNodeList.iterator();
 		while(childSiteNodeListIterator.hasNext())
 		{
 			SiteNode childSiteNode = (SiteNode)childSiteNodeListIterator.next();
 			getPublishedSiteNodeVersionWithParentRecursive(childSiteNode, resultList, db);
 		}
 		
 		return resultList;
 	}
 
 	
 	/**
 	 * Recursive methods to get all contentVersions of a given state under the specified parent content.
 	 */ 
 	
     public void getSiteNodeAndAffectedItemsRecursive(Integer siteNodeId, Integer stateId, List siteNodeVersionVOList, List contenteVersionVOList) throws ConstraintException, SystemException
 	{
         Database db = CastorDatabaseService.getDatabase();
 
 	    beginTransaction(db);
 
         try
         {
             SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, db);
 
             getSiteNodeAndAffectedItemsRecursive(siteNode, stateId, new ArrayList(), new ArrayList(), db, siteNodeVersionVOList, contenteVersionVOList);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 	}
 	
 	private void getSiteNodeAndAffectedItemsRecursive(SiteNode siteNode, Integer stateId, List checkedSiteNodes, List checkedContents, Database db, List siteNodeVersionVOList, List contentVersionVOList) throws ConstraintException, SystemException, Exception
 	{
 	    checkedSiteNodes.add(siteNode.getId());
         
 		// Get the versions of this siteNode.
 		//SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersionIfInState(siteNode, stateId, db);
 		SiteNodeVersion siteNodeVersion = getLatestActiveSiteNodeVersion(db, siteNode.getId());
 		if(siteNodeVersion != null && siteNodeVersion.getStateId().intValue() == stateId.intValue())
 		{
 		    siteNodeVersionVOList.add(siteNodeVersion.getValueObject());
 		    
 	        List relatedEntities = RegistryController.getController().getMatchingRegistryVOListForReferencingEntity(SiteNodeVersion.class.getName(), siteNodeVersion.getId().toString(), db);
 	        Iterator relatedEntitiesIterator = relatedEntities.iterator();
 	        
 	        while(relatedEntitiesIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)relatedEntitiesIterator.next();
 	            if(registryVO.getEntityName().equals(SiteNode.class.getName()) && !checkedSiteNodes.contains(new Integer(registryVO.getEntityId())))
 	            {
 	                SiteNode relatedSiteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(registryVO.getEntityId()), db);
 	                //SiteNodeVersion relatedSiteNodeVersion = getLatestActiveSiteNodeVersionIfInState(relatedSiteNode, stateId, db);
 	                SiteNodeVersion relatedSiteNodeVersion = getLatestActiveSiteNodeVersion(db, new Integer(registryVO.getEntityId()));
 	        		if(relatedSiteNodeVersion != null && siteNodeVersion.getStateId().intValue() == stateId.intValue() && siteNode.getRepository().getId().intValue() == relatedSiteNodeVersion.getOwningSiteNode().getRepository().getId().intValue())
 	                {
 	                    siteNodeVersionVOList.add(relatedSiteNodeVersion.getValueObject());
 	                }
 	                
 	    		    checkedSiteNodes.add(new Integer(registryVO.getEntityId()));
 	            }
 	            else if(registryVO.getEntityName().equals(Content.class.getName()) && !checkedContents.contains(new Integer(registryVO.getEntityId())))
 	            {
 	                Content relatedContent = ContentController.getContentController().getContentWithId(new Integer(registryVO.getEntityId()), db);
 	                List relatedContentVersions = ContentVersionController.getContentVersionController().getLatestActiveContentVersionIfInState(relatedContent, stateId, db);
 	                
 	                Iterator relatedContentVersionsIterator = relatedContentVersions.iterator();
 	                while(relatedContentVersionsIterator.hasNext())
 	                {
 	                    ContentVersion relatedContentVersion = (ContentVersion)relatedContentVersionsIterator.next();
 		                if(relatedContentVersion != null && siteNode.getRepository().getId().intValue() == relatedContentVersion.getOwningContent().getRepository().getId().intValue())
 		                {
 		                    contentVersionVOList.add(relatedContentVersion.getValueObject());
 		                }
 	                }
 	                
 	    		    checkedContents.add(new Integer(registryVO.getEntityId()));
 	            }
 	        }	    
 
 		}
 		
 		// Get the children of this siteNode and do the recursion
 		Collection childSiteNodeList = siteNode.getChildSiteNodes();
 		Iterator cit = childSiteNodeList.iterator();
 		while (cit.hasNext())
 		{
 			SiteNode childSiteNode = (SiteNode) cit.next();
 			getSiteNodeAndAffectedItemsRecursive(childSiteNode, stateId, checkedSiteNodes, checkedContents, db, siteNodeVersionVOList, contentVersionVOList);
 		}
    
 	}
 
 	/**
 	 * This method returns the latest sitenodeVersion there is for the given siteNode.
 	 */
 	
 	public SiteNodeVersion getLatestActiveSiteNodeVersionIfInState(SiteNode siteNode, Integer stateId, Database db) throws SystemException, Exception
 	{
 		SiteNodeVersion siteNodeVersion = null;
 		
 		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
 
 		SiteNodeVersion latestSiteNodeVersion = null;
 		
 		Iterator versionIterator = siteNodeVersions.iterator();
 		while(versionIterator.hasNext())
 		{
 		    SiteNodeVersion siteNodeVersionCandidate = (SiteNodeVersion)versionIterator.next();	
 			
 			if(latestSiteNodeVersion == null || (latestSiteNodeVersion.getId().intValue() < siteNodeVersionCandidate.getId().intValue() && siteNodeVersionCandidate.getIsActive().booleanValue()))
 			    latestSiteNodeVersion = siteNodeVersionCandidate;
 			
 			if(siteNodeVersionCandidate.getIsActive().booleanValue() && siteNodeVersionCandidate.getStateId().intValue() == stateId.intValue())
 			{
 				if(siteNodeVersionCandidate.getOwningSiteNode().getSiteNodeId().intValue() == siteNode.getId().intValue())
 				{
 					if(siteNodeVersion == null || siteNodeVersion.getSiteNodeVersionId().intValue() < siteNodeVersionCandidate.getId().intValue())
 					{
 						siteNodeVersion = siteNodeVersionCandidate;
 					}
 				}
 			}
 		}
 
 		if(siteNodeVersion != latestSiteNodeVersion)
 		    siteNodeVersion = null;
 		    
 		return siteNodeVersion;
 	}
 
 	
 	   /**
      * This method gets the meta info for the siteNodeVersion.
      * @param db
      * @throws ConstraintException
      * @throws SystemException
      * @throws Exception
      */
     public List getMetaInfoContentVersionVOList(Integer siteNodeVersionId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
     {
         List contentVersionVOList = new ArrayList();
         
         Database db = CastorDatabaseService.getDatabase();
 
 	    beginTransaction(db);
 
         try
         {
             SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
             List contentVersions = getMetaInfoContentVersions(db, siteNodeVersion, infoGluePrincipal);
             contentVersionVOList = toVOList(contentVersions);
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
             CmsLogger.logSevere("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
         
 		return contentVersionVOList;
     }
 
     /**
      * This method gets the meta info for the siteNodeVersion.
      * @param db
      * @throws ConstraintException
      * @throws SystemException
      * @throws Exception
      */
     private List getMetaInfoContentVersions(Database db, SiteNodeVersion siteNodeVersion, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException, Exception
     {
         List contentVersions = new ArrayList();
         
         List languages = LanguageController.getController().getLanguageList(siteNodeVersion.getOwningSiteNode().getRepository().getId(), db);
 		Language masterLanguage = LanguageController.getController().getMasterLanguage(db, siteNodeVersion.getOwningSiteNode().getRepository().getId());
 		
 		Integer metaInfoAvailableServiceBindingId = null;
 		Integer serviceBindingId = null;
 		AvailableServiceBinding availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName("Meta information", db, true);
 		if(availableServiceBinding != null)
 			metaInfoAvailableServiceBindingId = availableServiceBinding.getAvailableServiceBindingId();
 		
 		Collection serviceBindings = siteNodeVersion.getServiceBindings();
 		Iterator serviceBindingIterator = serviceBindings.iterator();
 		while(serviceBindingIterator.hasNext())
 		{
 			ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
 			if(serviceBinding.getAvailableServiceBinding().getId().intValue() == metaInfoAvailableServiceBindingId.intValue())
 			{
 				serviceBindingId = serviceBinding.getId();
 				break;
 			}
 		}
 
 		if(serviceBindingId != null)
 		{
 			List boundContents = ContentController.getBoundContents(serviceBindingId); 
 			if(boundContents.size() > 0)
 			{
 				ContentVO contentVO = (ContentVO)boundContents.get(0);
 				
 				Iterator languageIterator = languages.iterator();
 				while(languageIterator.hasNext())
 				{
 					Language language = (Language)languageIterator.next();
 					ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentVO.getId(), language.getId(), db);
 					
 					if(contentVersion != null)
 					    contentVersions.add(contentVersion);
 				}
 			}
 		}
 		
 		return contentVersions;
     }
 
 	
 	/**
 	 * Updates the SiteNodeVersion.
 	 */
 	
 	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
 	{
     	registryController.updateSiteNodeVersion(siteNodeVersionVO);
 
 		return (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, (BaseEntityVO)siteNodeVersionVO);
 	}  
 	
 	/**
 	 * Updates the SiteNodeVersion within a transaction.
 	 */
 	
	public SiteNodeVersionVO update(SiteNodeVersionVO siteNodeVersionVO, Database db) throws ConstraintException, SystemException, Exception
 	{
    	registryController.updateSiteNodeVersion(getSiteNodeVersionWithId(siteNodeVersionVO.getId(), db), db);
 
 		return (SiteNodeVersionVO) updateEntity(SiteNodeVersionImpl.class, (BaseEntityVO)siteNodeVersionVO, db);
 	}    
 	
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new SiteNodeVersionVO();
 	}
 
 
 }
  
