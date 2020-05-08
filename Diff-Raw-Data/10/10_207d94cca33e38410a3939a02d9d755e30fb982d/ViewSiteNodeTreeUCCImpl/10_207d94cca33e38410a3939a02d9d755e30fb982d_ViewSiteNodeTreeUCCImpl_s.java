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
 
 package org.infoglue.cms.controllers.usecases.structuretool.impl.simple;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.controllers.kernel.impl.simple.BaseUCCController;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCC;
 import org.infoglue.cms.entities.management.Repository;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 public class ViewSiteNodeTreeUCCImpl extends BaseUCCController implements ViewSiteNodeTreeUCC
 {
 	/**
 	 * This method fetches the root siteNode for a particular repository.
 	 * If there is no such siteNode we create one as all repositories need one to work.
 	 */
 	        
    	public SiteNodeVO getRootSiteNode(Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws ConstraintException, SystemException
    	{
         Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         SiteNodeVO siteNodeVO = null;
 
         beginTransaction(db);
 
         try
         {
             getLogger().info("Fetching the root siteNode for the repository " + repositoryId);
 			OQLQuery oql = db.getOQLQuery( "SELECT c FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl c WHERE is_undefined(c.parentSiteNode) AND c.repository.repositoryId = $1");
 			oql.bind(repositoryId);
 			
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
 			    siteNodeVO = ((SiteNode)results.next()).getValueObject();
             }
             else
             {
 				//None found - we create it and give it the name of the repository.
 				getLogger().info("Found no rootSiteNode so we create a new....");
 				SiteNodeVO rootSiteNodeVO = new SiteNodeVO();
 				Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
 				rootSiteNodeVO.setName(repository.getName());
 				rootSiteNodeVO.setIsBranch(new Boolean(true));
 				SiteNode siteNode = SiteNodeController.getController().create(db, null, null, infoGluePrincipal, repositoryId, rootSiteNodeVO);
 				//siteNodeVO = SiteNodeController.getController().create(null, null, infoGluePrincipal, repositoryId, siteNodeVO);
 				//siteNodeVO = SiteNodeControllerProxy.getSiteNodeControllerProxy().acCreate(infoGluePrincipal, null, null, repositoryId, rootSiteNodeVO);
 				siteNodeVO = siteNode.getValueObject();
 				SiteNodeVO newSiteNodeVO = siteNodeVO;
 				
             	//Also creates an initial meta info for the sitenode.
 				SiteNodeController.getController().createSiteNodeMetaInfoContent(db, siteNode, repositoryId, infoGluePrincipal, null);
 			}
            	getLogger().info("Did we find anything?");
             
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
 
         return siteNodeVO;
    	}
     
     public SiteNodeVO getSiteNode(Integer siteNodeId) throws ConstraintException, SystemException
     {
     	return SiteNodeController.getSiteNodeVOWithId(siteNodeId);
     	/*
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         SiteNode siteNode = null;
 
         beginTransaction(db);
 
         try
         {
             siteNode = SiteNodeController.getSiteNodeVOWithId(siteNodeId);
         
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
 
         return siteNode.getValueObject();
 		*/
     }        
 
 
     public List getSiteNodeChildren(Integer parentSiteNodeId) throws ConstraintException, SystemException
     {
 		Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         List childrenVOList = null;
 
         beginTransaction(db);
 
         try
         {
             SiteNode siteNode = SiteNodeController.getSiteNodeWithId(parentSiteNodeId, db, true);
 	        Collection children = siteNode.getChildSiteNodes();
         	childrenVOList = SiteNodeController.toVOList(children);
         	
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
         
         return childrenVOList;
     } 
 }
         
