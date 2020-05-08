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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SearchController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.util.CmsLogger;
 
 import webwork.action.Action;
 
 import java.util.List;
 
 
 /**
  * Action class for usecase SearchContentAction. Was better before but due to wanted support for multiple 
  * databases and lack of time I had to cut down on functionality - sorry Magnus. 
  *
  * @author Magnus Gvenal
  * @author Mattias Bogeblad
  */
 
 public class SearchContentAction extends InfoGlueAbstractAction 
 {
 
 	private List contentVersionVOList;
 	private Integer repositoryId;
 	private String searchString;
 	private String name;
 	private Integer languageId;
 	private Integer contentTypeDefinitionId;
 	private Integer caseSensitive;
 	private Integer inverseSearch;
 	private Integer stateId;
	private boolean advancedEnabled = false;
 	
 	private int maxRows = 0;
 	
 	//This is for advanced search
 	private List principals 			= null;
 	private List availableLanguages 	= null;
 	private List contentTypeDefinitions = null;
 	
 	//This is for replace
 	private String replaceString		= null;
 	//private String[] contentVersionId  	= null;
 	
 	public void setSearchString(String s)
 	{
	    this.searchString = s;
		//this.searchString = s.replaceAll("'","");
 	}
 	
 	public String getSearchString()
 	{
 		return this.searchString;	
 	}
 	
 	public void setMaxRows(int r)
 	{
 		this.maxRows = r;	
 	}
 	
 	public int getMaxRows()
 	{
 		if(maxRows == 0)
 		    maxRows=100;
 		
 		return this.maxRows;	
 	}
 
 	public List getContentVersionVOList()
 	{
 		return this.contentVersionVOList;		
 	}
 	
 	public String doExecute() throws Exception 
 	{
 	    CmsLogger.logInfo("Executing doExecute on SearchContentAction..");
 		int maxRows = 100;
 		try
 		{
 			maxRows = Integer.parseInt(CmsPropertyHandler.getProperty("maxRows"));
 		}
 		catch(Exception e)
 		{
 		}
 		/*
 		System.out.println("this.repositoryId:" + this.repositoryId);
 		System.out.println("this.getSearchString():" + this.getSearchString());
 		System.out.println("maxRows:" + maxRows);
 		System.out.println("name:" + name);
 		System.out.println("languageId:" + languageId);
 		System.out.println("contentTypeDefinitionId:" + contentTypeDefinitionId);
 		System.out.println("caseSensitive:" + caseSensitive);
 		System.out.println("stateId:" + stateId);
 		*/
 		contentVersionVOList = SearchController.getContentVersions(this.repositoryId, this.getSearchString(), maxRows, name, languageId, contentTypeDefinitionId, caseSensitive, stateId);
 	    
 	    this.principals = UserControllerProxy.getController().getAllUsers();
 	    this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 	    this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 
 		CmsLogger.logInfo("Finished executing doExecute on SearchContentAction..");
         return "success";
 	}
 	
 	/**
 	 * This method returns the advanced search interface to the user.
 	 */
 
 	public String doInput() throws Exception 
 	{
 	    this.principals = UserControllerProxy.getController().getAllUsers();
 	    this.availableLanguages = LanguageController.getController().getLanguageVOList(this.repositoryId);
 	    this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
 	    
 	    return Action.INPUT;
 	}
 	
 	
 	public ContentVO getContentVO(Integer contentId)
 	{
 		ContentVO contentVO = null;
 		
 		try
 		{
 			if(contentId != null)
 			{
 				contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred when we tried to get the content for this version:" + e.getMessage(), e);
 		}
 		
 		return contentVO;
 	}
 
 	public LanguageVO getLanguageVO(Integer languageId)
 	{
 		LanguageVO languageVO = null;
 		
 		try
 		{
 			if(languageId != null)
 			{
 				languageVO = LanguageController.getController().getLanguageVOWithId(languageId);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred when we tried to get the language for this version:" + e.getMessage(), e);
 		}
 		
 		return languageVO;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return repositoryId;
 	}
 
 	public void setRepositoryId(Integer integer)
 	{
 		repositoryId = integer;
 	}
 
     public List getAvailableLanguages()
     {
         return availableLanguages;
     }
     
     public List getContentTypeDefinitions()
     {
         return contentTypeDefinitions;
     }
     
     public List getPrincipals()
     {
         return principals;
     }
  
    /* 
     public String[] getContentVersionId()
     {
         return contentVersionId;
     }
     
     public void setContentVersionIds(String[] contentVersionId)
     {
         this.contentVersionId = contentVersionId;
     }
     */
     public String getReplaceString()
     {
         return replaceString;
     }
     
     public void setReplaceString(String replaceString)
     {
         this.replaceString = replaceString;
     }
     
     public Integer getCaseSensitive()
     {
         return caseSensitive;
     }
     
     public void setCaseSensitive(Integer caseSensitive)
     {
         this.caseSensitive = caseSensitive;
     }
     
     public Integer getContentTypeDefinitionId()
     {
         return contentTypeDefinitionId;
     }
     
     public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
     {
         this.contentTypeDefinitionId = contentTypeDefinitionId;
     }
     
     public Integer getInverseSearch()
     {
         return inverseSearch;
     }
     
     public void setInverseSearch(Integer inverseSearch)
     {
         this.inverseSearch = inverseSearch;
     }
     
     public Integer getLanguageId()
     {
         return languageId;
     }
     
     public void setLanguageId(Integer languageId)
     {
         this.languageId = languageId;
     }
     
     public String getName()
     {
         return name;
     }
     
     public void setName(String name)
     {
         this.name = name;
     }
     
     public Integer getStateId()
     {
         return stateId;
     }
     
     public void setStateId(Integer stateId)
     {
         this.stateId = stateId;
     }
    
    public boolean isAdvancedEnabled()
    {
        return advancedEnabled;
    }
    
    public void setAdvancedEnabled(boolean advancedEnabled)
    {
        this.advancedEnabled = advancedEnabled;
    }
 }
