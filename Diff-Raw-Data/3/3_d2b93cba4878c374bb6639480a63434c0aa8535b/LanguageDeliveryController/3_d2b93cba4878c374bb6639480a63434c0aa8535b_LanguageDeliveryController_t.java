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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.Language;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.Repository;
 import org.infoglue.cms.entities.management.RepositoryLanguage;
 import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.util.CacheController;
 
 
 public class LanguageDeliveryController extends BaseDeliveryController
 {
 
 	/**
 	 * Private constructor to enforce factory-use
 	 */
 	
 	private LanguageDeliveryController()
 	{
 	}
 	
 	/**
 	 * Factory method
 	 */
 	
 	public static LanguageDeliveryController getLanguageDeliveryController()
 	{
 		return new LanguageDeliveryController();
 	}
 	
 	
 	/**
 	 * This method return a LanguageVO
 	 */
 	
 	public LanguageVO getLanguageVO(Database db, Integer languageId) throws SystemException, Exception
 	{
		if(languageId == null || languageId.intValue() == 0)
			return null;
			
 		String key = "" + languageId;
 		getLogger().info("key:" + key);
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
 		if(languageVO != null)
 		{
 			getLogger().info("There was an cached languageVO:" + languageVO);
 		}
 		else
 		{
 			Language language = (Language)getObjectWithId(LanguageImpl.class, languageId, db);
 				
 			if(language != null)
 				languageVO = language.getValueObject();
             
 			CacheController.cacheObject("languageCache", key, languageVO);				
 		}
 				
 		return languageVO;
 	}
 
 	/**
 	 * This method returns all languages for a certain repository.
 	 * 
 	 * @param repositoryId
 	 * @return
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 
 	public List getAvailableLanguagesForRepository(Database db, Integer repositoryId) throws SystemException, Exception
     {
 		String key = "" + repositoryId + "_allLanguages";
 		getLogger().info("key:" + key);
 		List list = (List)CacheController.getCachedObject("languageCache", key);
 		if(list != null)
 		{
 			getLogger().info("There was an cached list:" + list);
 		}
 		else
 		{
 		    list = new ArrayList();
 		    
 	        Repository repository = (Repository) getObjectWithId(RepositoryImpl.class, repositoryId, db);
 	        if (repository != null) 
 	        {
 	            for (Iterator i = repository.getRepositoryLanguages().iterator();i.hasNext();) 
 	            {
 	                RepositoryLanguage repositoryLanguage = (RepositoryLanguage) i.next();
 	                Language language = repositoryLanguage.getLanguage();
 	                if (language != null)
 	                    list.add(language.getValueObject());
 	            }
 	        }
         
 	        if(list.size() > 0)
 	            CacheController.cacheObject("languageCache", key, list);				
 		}
 	        
         return list;
     } 
 	
 	/**
 	 * This method returns the languages assigned to a respository. 
 	 */
 	
 	public List getAvailableLanguages(Database db, Integer siteNodeId) throws SystemException, Exception
 	{ 
 		getLogger().info("getAvailableLanguages for " + siteNodeId + " start.");
 
 		List languageVOList = new ArrayList();
 
         getLogger().info("siteNodeId:" + siteNodeId);
 
         SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 			
 		if(siteNode != null)
 		{
 			Repository repository = siteNode.getRepository();
      		if(repository != null)
 			{
      		    getLogger().info("repository:" + repository.getName());
 
      		    Collection repositoryLanguages = repository.getRepositoryLanguages();
      		    getLogger().info("repositoryLanguages:" + repositoryLanguages.size());
      			Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
      			while(repositoryLanguagesIterator.hasNext())
      			{
      				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguagesIterator.next();
      				Language language = repositoryLanguage.getLanguage();
      				if(language != null)
      				{
      					getLogger().info("Adding " + language.getName() + " to the list of available languages");
          				languageVOList.add(language.getValueObject());
      				}
      			}
 			}
 		}
 
 		getLogger().info("getAvailableLanguages for " + siteNodeId + " end.");
 
         return languageVOList;	
 	}
 
 
 	/**
 	 * This method returns the master language. 
 	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
 	 */
 	
 	public LanguageVO getMasterLanguage(Database db, String repositoryName) throws SystemException, Exception
 	{ 
         Language language = null;
 
      	OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.name = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
 		oql.bind(repositoryName);
 		
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if (results.hasMore()) 
         {
         	language = (Language)results.next();
         }
             
         return (language == null) ? null : language.getValueObject();	
 	}
 	
 
 	/**
 	 * This method returns the master language. 
 	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
 	 */
 	
 	public LanguageVO getMasterLanguageForRepository(Database db, Integer repositoryId) throws SystemException, Exception
 	{ 
 		String languageKey = "" + repositoryId;
 		getLogger().info("languageKey in getMasterLanguageForRepository:" + languageKey);
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
 		if(languageVO != null)
 		{
 			getLogger().info("There was an cached master language:" + languageVO.getName());
 		}
 		else
 		{
 			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
 			oql.bind(repositoryId);
 			
 			QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
 			{
 				Language language = (Language)results.next();
 				languageVO = language.getValueObject();
 			}
 			
 			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
 		}
 
 		return languageVO;	
 	}
 
 	/**
 	 * This method returns the master language. 
 	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
 	 */
 	
 	public LanguageVO getMasterLanguageForRepository(Integer repositoryId, Database db) throws SystemException, Exception
 	{ 
 		LanguageVO languageVO = null;
 
 		String languageKey = "" + repositoryId;
 		getLogger().info("languageKey in getMasterLanguageForRepository:" + languageKey);
 		languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
 		if(languageVO != null)
 		{
 			getLogger().info("There was an cached master language:" + languageVO.getName());
 		}
 		else
 		{
 			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
 			oql.bind(repositoryId);
 			
 			QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
 			{
 				Language language = (Language)results.next();
 				languageVO = language.getValueObject();
 			}
 			
 			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
 		}
 
 		return languageVO;	
 	}
 
 	
 	/**
 	 * This method returns the master language. 
 	 * todo - add attribute on repositoryLanguage to be able to sort them... and then fetch the first
 	 */
 	
 	public LanguageVO getMasterLanguageForSiteNode(Database db, Integer siteNodeId) throws SystemException, Exception
 	{ 
 	    String languageKey = "siteNodeId_" + siteNodeId;
 		getLogger().info("languageKey in getMasterLanguageForSiteNode:" + languageKey);
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("masterLanguageCache", languageKey);
 		if(languageVO != null)
 		{
 		    getLogger().info("There was an cached master language:" + languageVO.getName());
 		}
 		else
 		{
 			SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 			Integer repositoryId = siteNode.getRepository().getRepositoryId();
          	
 			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.repositoryLanguages.repository.repositoryId = $1 ORDER BY l.repositoryLanguages.sortOrder, l.languageId");
 			oql.bind(repositoryId);
 			
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
 				Language language = (Language)results.next();
 				languageVO = language.getValueObject();
             }
 			
 			CacheController.cacheObject("masterLanguageCache", languageKey, languageVO);
 		}
 		
         return languageVO;	
 	}
 	
 
 	/**
 	 * This method returns language with the languageCode sent in. 
 	 */
 	
 	public Locale getLocaleWithId(Database db, Integer languageId)
 	{
 		String key = "" + languageId;
 		getLogger().info("key:" + key);
 		Locale locale = (Locale)CacheController.getCachedObject("localeCache", key);
 		if(locale != null)
 		{
 			getLogger().info("There was an cached locale:" + locale);
 		}
 		else
 		{
 			locale = Locale.getDefault();
 			
 			if (languageId != null)
 			{
 				try 
 				{
 					LanguageVO languageVO = getLanguageVO(db, languageId);
 					locale = new Locale(languageVO.getLanguageCode());
 				} 
 				catch (Exception e) 
 				{
 					getLogger().error("An error occurred in getLocaleWithId: getting locale with languageid:" + languageId + "," + e, e);
 				}	
 			}
 			
 			CacheController.cacheObject("localeCache", key, locale);				
 		}
 		
 		return locale; 
 	}
 
 	/**
 	 * This method returns language with the languageCode sent in. 
 	 */
 	
 	public Locale getLocaleWithCode(String languageCode)
 	{
 		String key = "" + languageCode;
 		getLogger().info("key:" + key);
 		Locale locale = (Locale)CacheController.getCachedObject("localeCache", key);
 		if(locale != null)
 		{
 			getLogger().info("There was an cached locale:" + locale);
 		}
 		else
 		{
 			locale = Locale.getDefault();
 			
 			if (languageCode != null)
 			{
 				try 
 				{
 					locale = new Locale(languageCode);
 				} 
 				catch (Exception e) 
 				{
 					getLogger().error("An error occurred in getLocaleWithCode: getting locale with languageCode:" + languageCode + "," + e, e);
 				}	
 			}
 			
 			CacheController.cacheObject("localeCache", key, locale);				
 		}
 		
 		return locale; 
 	}
 
 
 	/**
 	 * This method returns language with the languageCode sent in. 
 	 */
 	
 	public LanguageVO getLanguageWithCode(Database db, String languageCode) throws SystemException, Exception
 	{ 
 		String key = "" + languageCode;
 		getLogger().info("key:" + key);
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("languageCache", key);
 		if(languageVO != null)
 		{
 			getLogger().info("There was an cached languageVO:" + languageVO);
 		}
 		else
 		{
 			Language language = null;
 	
 			OQLQuery oql = db.getOQLQuery( "SELECT l FROM org.infoglue.cms.entities.management.impl.simple.LanguageImpl l WHERE l.languageCode = $1");
 			oql.bind(languageCode);
 			
         	QueryResults results = oql.execute(Database.ReadOnly);
 			
 			if (results.hasMore()) 
             {
             	language = (Language)results.next();
 				languageVO = language.getValueObject();
 	        }
             
 			CacheController.cacheObject("languageCache", key, languageVO);
 		}
 		
         return languageVO;	
 	}
 
 
 	/**
 	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
 	 */
 	
 	public LanguageVO getLanguageIfRepositorySupportsIt(Database db, String languageCodes, Integer siteNodeId) throws SystemException, Exception
 	{
 		if (languageCodes == null) return null;
 		int index = Integer.MAX_VALUE;
 		int currentIndex = 0;
 		getLogger().info("Coming in with languageCodes:" + languageCodes);
 		
         Language language = null;
 
     	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 		Repository repository = siteNode.getRepository();
 		if(repository != null)
 		{
 			Collection languages = repository.getRepositoryLanguages();
 			Iterator languageIterator = languages.iterator();
 			while(languageIterator.hasNext())
 			{
 				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
 				Language currentLanguage = repositoryLanguage.getLanguage();
 				getLogger().info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
 				currentIndex = languageCodes.toLowerCase().indexOf(currentLanguage.getLanguageCode().toLowerCase());
 				if( currentIndex > -1 && currentIndex < index)
 				{
 					index = currentIndex;
 					getLogger().info("Found the language in the list of supported languages for this site: " + currentLanguage.getName() + " - priority:" + index);
 					language = currentLanguage;
 					if (index==0) break; // Continue and try to find a better candidate unless index is 0 (first prio)
 				}
 			}
 		}
 
 		return (language == null) ? null : language.getValueObject();	
 	}
 
 	/**
 	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
 	 */
 	
 	public LanguageVO getLanguageIfSiteNodeSupportsIt(Database db, String languageCodes, Integer siteNodeId, InfoGluePrincipal principal) throws SystemException, Exception
 	{
 	    if (languageCodes == null) 
 	    	return null;
 		
 		String key = "" + siteNodeId + "_" + languageCodes;		
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("siteNodeLanguageCache", key);
 		if(languageVO != null)
 			return languageVO;
 		
 	    int index = Integer.MAX_VALUE;
 		int currentIndex = 0;
 		getLogger().info("Coming in with languageCodes:" + languageCodes);
 		
         Language language = null;
 
     	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 		    	
     	Repository repository = siteNode.getRepository();
 		if(repository != null)
 		{
 			Collection languages = repository.getRepositoryLanguages();
 			Iterator languageIterator = languages.iterator();
 			while(languageIterator.hasNext())
 			{
 				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
 				Language currentLanguage = repositoryLanguage.getLanguage();
 				getLogger().info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
 				
 				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
 				
 				currentIndex = languageCodes.toLowerCase().indexOf(currentLanguage.getLanguageCode().toLowerCase());
 				if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()) && currentIndex > -1 && currentIndex < index)
 				{
 					index = currentIndex;
 					getLogger().info("Found the language in the list of supported languages for this site: " + currentLanguage.getName() + " - priority:" + index);
 
 					DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
 			    	ContentVO contentVO = ndc.getBoundContent(db, principal, siteNodeId, currentLanguage.getId(), false, BasicTemplateController.META_INFO_BINDING_NAME, deliveryContext);		
 					if(contentVO != null)
 					{
 				    	ContentVersionVO contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(db, siteNodeId, contentVO.getId(), currentLanguage.getId(), false, deliveryContext, principal);
 				    	if(contentVersionVO != null)
 				    	{
 							language = currentLanguage;
 							getLogger().info("Language now: " + language.getName());
 				    	}
 				    }
 					
 					if (index==0) break; // Continue and try to find a better candidate unless index is 0 (first prio)
 				}
 			}
 		}
 
 		if(language != null)
 			CacheController.cacheObject("siteNodeLanguageCache", key, language.getValueObject());
 
 		getLogger().info("Returning language: " + language);
 		
 		return (language == null) ? null : language.getValueObject();	
 	}
 
  
 	/**
 	 * This method returns language with the languageCode sent in if it is allowed/supported in the current repository. 
 	 */
 	
 	public LanguageVO getLanguageIfSiteNodeSupportsIt(Database db, Integer languageId, Integer siteNodeId) throws SystemException, Exception
 	{
 		if (languageId == null) 
 		    return null;
 
 		String key = "" + siteNodeId + "_" + languageId;		
 		LanguageVO languageVO = (LanguageVO)CacheController.getCachedObject("siteNodeLanguageCache", key);
 		if(languageVO != null)
 			return languageVO;
 		
 		NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, new Integer(-1));
 
 		getLogger().info("Coming in with languageId:" + languageId);
 		
         Language language = null;
 
     	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 
 		if(!getIsValidLanguage(db, ndc, siteNode, languageId))
 		    return null;		
     	
     	Repository repository = siteNode.getRepository();
 		if(repository != null)
 		{
 			Collection languages = repository.getRepositoryLanguages();
 	    	
 			Iterator languageIterator = languages.iterator();
 			while(languageIterator.hasNext())
 			{
 				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
 				Language currentLanguage = repositoryLanguage.getLanguage();
 				getLogger().info("CurrentLanguage:" + currentLanguage.getId());
 				if(currentLanguage.getId().intValue() == languageId.intValue())
 				{
 				    getLogger().info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
 					if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
 					{
 					    language = currentLanguage;
 					    break;
 					}
 				    /*
 				    DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
 			    	ContentVO contentVO = ndc.getBoundContent(db, principal, siteNodeId, currentLanguage.getId(), false, BasicTemplateController.META_INFO_BINDING_NAME, deliveryContext);		
 					if(contentVO != null)
 					{
 				    	ContentVersionVO contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(db, siteNodeId, contentVO.getId(), currentLanguage.getId(), false, deliveryContext, principal);
 				    	if(contentVersionVO != null)
 				    	{
 				    	    System.out.println("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
 							language = currentLanguage;
 							break;
 				    	}
 				    }
 				    */
 				}
 			}
 		}
 		
 		if(language != null)
 			CacheController.cacheObject("siteNodeLanguageCache", key, language.getValueObject());
 
 		getLogger().info("Returning language: " + language);
 
 		return (language == null) ? null : language.getValueObject();	
 	}
 
 	/**
 	 * This method returns all languages available for a site node. 
 	 */
 	
 	public List getLanguagesForSiteNode(Database db, Integer siteNodeId, InfoGluePrincipal principal) throws SystemException, Exception
 	{
 		String key = "" + siteNodeId;		
 		List languageVOList = (List)CacheController.getCachedObject("siteNodeLanguageCache", key);
 		if(languageVOList != null)
 			return languageVOList;
 		
 		getLogger().info("Coming in with siteNodeId:" + siteNodeId);
 		
         languageVOList = new ArrayList();
 
     	SiteNode siteNode = (SiteNode)getObjectWithId(SiteNodeImpl.class, siteNodeId, db);
 		    	
     	Repository repository = siteNode.getRepository();
 		if(repository != null)
 		{
 			Collection languages = repository.getRepositoryLanguages();
 			Iterator languageIterator = languages.iterator();
 			while(languageIterator.hasNext())
 			{
 				RepositoryLanguage repositoryLanguage = (RepositoryLanguage)languageIterator.next();
 				Language currentLanguage = repositoryLanguage.getLanguage();
 				getLogger().info("CurrentLanguageCode:" + currentLanguage.getLanguageCode());
 				
 				NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, currentLanguage.getId(), new Integer(-1));
 				
 				if(getIsValidLanguage(db, ndc, siteNode, currentLanguage.getId()))
 				{
 					getLogger().info("Found the language in the list of supported languages for this site: " + currentLanguage.getName());
 					languageVOList.add(currentLanguage.getValueObject());
 				}
 			}
 		}
 
 		if(languageVOList != null)
 			CacheController.cacheObject("siteNodeLanguageCache", key, languageVOList);
 
 		getLogger().info("Returning languageVOList: " + languageVOList.size());
 		
 		return languageVOList;	
 	}
 
 	
 	public boolean getIsValidLanguage(Database db, NodeDeliveryController ndc, SiteNode siteNode, Integer languageId) throws Exception
 	{
 	    boolean isValidLanguage = true;
 	    
 	    SiteNodeVersion siteNodeVersion = ndc.getActiveSiteNodeVersion(siteNode.getId(), db);
 	    Integer disabledLanguagesSiteNodeVersionId = ndc.getDisabledLanguagesSiteNodeVersionId(db, siteNode.getId());
 	    getLogger().info("disabledLanguagesSiteNodeVersionId:" + disabledLanguagesSiteNodeVersionId);
 	    
 	    if(disabledLanguagesSiteNodeVersionId != null)
 	    {
 	        SiteNodeVersion disabledLanguagesSiteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(disabledLanguagesSiteNodeVersionId, db);
 	        
 	        String disabledLanguagesString = CmsPropertyHandler.getPropertySetValue("siteNode_" + disabledLanguagesSiteNodeVersion.getValueObject().getSiteNodeId() + "_disabledLanguages");
 		    getLogger().info("disabledLanguagesString:" + disabledLanguagesString);
 		    
 		    if(disabledLanguagesString != null && !disabledLanguagesString.equalsIgnoreCase(""))
 		    {
 		        String[] disabledLanguagesStringArray = disabledLanguagesString.split(",");
 		        for(int i=0; i<disabledLanguagesStringArray.length; i++)
 		        {
 		            getLogger().info("languageId.intValue():" + languageId.intValue());
 		            getLogger().info("disabledLanguagesStringArray:" + disabledLanguagesStringArray);
 				    if(languageId.intValue() == new Integer(disabledLanguagesStringArray[i]).intValue())
 		            {
 		                isValidLanguage = false;
 			            getLogger().info("isValidLanguage:" + isValidLanguage);
 		                break;
 		            }
 		        }
 		    }
 		    
 		}
 	    getLogger().info("languageId:" + languageId + " was valid:" + isValidLanguage);
 		
 		return isValidLanguage;
 	}
 
 	
 	
 }
