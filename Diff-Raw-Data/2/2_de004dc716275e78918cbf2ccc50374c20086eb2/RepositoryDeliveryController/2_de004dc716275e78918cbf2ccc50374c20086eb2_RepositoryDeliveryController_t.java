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
 
 package org.infoglue.deliver.controllers.kernel.impl.simple;
 
 import org.infoglue.cms.entities.management.*;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.util.*;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.deliver.applications.databeans.NullObject;
 import org.infoglue.deliver.util.CacheController;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 public class RepositoryDeliveryController extends BaseDeliveryController
 {
 
 	/**
 	 * Private constructor to enforce factory-use
 	 */
 	
 	private RepositoryDeliveryController()
 	{
 	}
 	
 	/**
 	 * Factory method
 	 */
 	
 	public static RepositoryDeliveryController getRepositoryDeliveryController()
 	{
 		return new RepositoryDeliveryController();
 	}
 	
 
 	/**
 	 * This method returns the master repository.
 	 */
 	
 	public RepositoryVO getMasterRepository(Database db) throws SystemException, Exception
 	{
 		RepositoryVO repositoryVO = (RepositoryVO)CacheController.getCachedObject("masterRepository", "masterRepository");
 		if(repositoryVO != null)
 			return repositoryVO;
 		
      	OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");
 		
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
         {
         	Repository repository = (Repository)results.next();
         	repositoryVO = repository.getValueObject();
         }
 
 		if(repositoryVO != null)
 			CacheController.cacheObject("masterRepository", "masterRepository", repositoryVO);
 		
         return repositoryVO;	
 	}
 	
 
 	public List getRepositoriesFromServerName(Database db, String serverName, String portNumber, String repositoryName) throws SystemException, Exception
     {
 	    List repositories = new ArrayList();
 	    
         OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r WHERE is_defined(r.dnsName)");
         QueryResults results = oql.execute(Database.ReadOnly);
         while (results.hasMore()) 
         {
             Repository repository = (Repository) results.next();
             getLogger().info("repository:" + repository.getDnsName());
             String[] dnsNames = splitStrings(repository.getDnsName());
             getLogger().info("dnsNames:" + dnsNames);
             for (int i=0;i<dnsNames.length;i++) 
             {
             	getLogger().info("dnsNames[i]:" + dnsNames[i]);
                 String dnsName = dnsNames[i];
             	int protocolIndex = dnsName.indexOf("://");
                 if(protocolIndex > -1)
                     dnsName = dnsName.substring(protocolIndex + 3);
                 
                 getLogger().info("Matching only server name - removed protocol if there:" + dnsName);
                 
            	if((dnsName.indexOf(":") == -1 && dnsName.indexOf(serverName) == 0) || dnsName.indexOf(serverName + ":" + portNumber) == 0)
                 {
             	    if(repositoryName != null && repositoryName.length() > 0)
             	    {
             	        getLogger().info("Has to check repositoryName also:" + repositoryName);
                         if(repository.getValueObject().getName().equalsIgnoreCase(repositoryName))
             	            repositories.add(repository.getValueObject());
             	    }
             	    else
             	    {
             	        repositories.add(repository.getValueObject());
             	    }
             	}
             }
         }
         
         return repositories;
     }
  	
     private String[] splitStrings(String str)
     {
         List list = new ArrayList();
         StringTokenizer st = new StringTokenizer(str, ",");
         while (st.hasMoreTokens()) 
         {
             String token = st.nextToken().trim();
             list.add(token);
         }
         
         return (String[]) list.toArray(new String[0]);
     } 
 	
 	/**
 	 * This method returns all the repositories.
 	 */
 	
 	public List getRepositoryVOList(Database db) throws SystemException, Exception
 	{
 		List repositoryVOList = new ArrayList();
 		
 		OQLQuery oql = db.getOQLQuery( "SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RepositoryImpl r ORDER BY r.repositoryId");
 		
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
 		{
 			Repository repository = (Repository)results.next();
 			RepositoryVO repositoryVO = repository.getValueObject();
 			repositoryVOList.add(repositoryVO);
 		}
 
 		return repositoryVOList;	
 	}
 
 	/**
 	 * This method fetches a property for a repository.
 	 */
 	
 	public String getPropertyValue(Integer repositoryId, String propertyName) 
 	{
 		String key = "parentRepository_" + repositoryId + "_" + propertyName;
 	    getLogger().info("key:" + key);
 	    Object object = CacheController.getCachedObject("parentRepository", key);
 		
 	    if(object instanceof NullObject)
 		{
 			getLogger().info("There was an cached property but it was null:" + object);
 			return null;
 		}
 		else if(object != null)
 		{
 			getLogger().info("There was an cached property:" + object);
 			return (String)object;
 		}
 		
 		String propertyValue = null;
 		
         Map args = new HashMap();
 	    args.put("globalKey", "infoglue");
 	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
 	    
 	    propertyValue = ps.getString("repository_" + repositoryId + "_" + propertyName);
 	    getLogger().info("propertyValue:" + propertyValue);
 	    if(propertyValue != null)
 	        CacheController.cacheObject("parentRepository", key, propertyValue);
 	    else
 	        CacheController.cacheObject("parentRepository", key, new NullObject());
 	        
 		return propertyValue;
 	}
 
 }
