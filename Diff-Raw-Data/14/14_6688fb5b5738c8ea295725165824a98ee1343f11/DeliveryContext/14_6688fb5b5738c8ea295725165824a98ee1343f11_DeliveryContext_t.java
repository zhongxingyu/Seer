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
 
 package org.infoglue.deliver.applications.databeans;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.infoglue.cms.applications.common.Session;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 
 /**
  * @author Mattias Bogeblad
  *
  * This class is used to store the context of a page and get and set information that is central to it.
  * TODO - write more
  */
 
 public class DeliveryContext implements UsageListener
 {
 	public static final String META_INFO_BINDING_NAME 					= "Meta information";
 	public static final String TEMPLATE_ATTRIBUTE_NAME   				= "Template";
 	public static final String TITLE_ATTRIBUTE_NAME     		 		= "Title";
 	public static final String NAV_TITLE_ATTRIBUTE_NAME 		 		= "NavigationTitle";
 	/*
 	protected static final String DISABLE_PAGE_CACHE_ATTRIBUTE_NAME		= "DisablePageCache";
 	protected static final String PAGE_CONTENT_TYPE_ATTRIBUTE_NAME		= "ContentType";
 	protected static final String ENABLE_PAGE_PROTECTION_ATTRIBUTE_NAME = "ProtectPage";
 	protected static final String DISABLE_EDIT_ON_SIGHT_ATTRIBUTE_NAME	= "DisableEditOnSight";
 	*/
 	
 	public static final boolean USE_LANGUAGE_FALLBACK        	= true;
 	public static final boolean DO_NOT_USE_LANGUAGE_FALLBACK 	= false;
 	public static final boolean USE_INHERITANCE 				= true;
 	public static final boolean DO_NOT_USE_INHERITANCE 			= false;
 	
 	//These are the standard parameters which uniquely defines which page to show.
 	private Integer siteNodeId = null;
 	private Integer contentId  = null; 
 	private Integer languageId = null;
 	
 	//This sets the content type
 	private String contentType = null;
 	
 	//Lets one disable caching of this page if needed for some requests.
 	private boolean disablePageCache = false;
 
 	//This decides if to show a minimalistic version of the page structure - not render all compoents etc.
 	private boolean showSimple = false;
 
 	//This parameter are set if you want to access a certain repository startpage
 	private String repositoryName = null;
 
 	private String pageKey = null;
 	private String pagePath = null;
 	
 	private HttpServletResponse httpServletResponse = null;
 	private HttpServletRequest httpServletRequest = null;
 	private Session session = null;
 	private InfoGlueAbstractAction infoglueAbstractAction = null;
 	
 	//This section has control over what contents and sitenodes are used where so the pagecache can be selectively updated.
 	private List usageListeners = new ArrayList();
 	
 	private Set usedContents = new HashSet();
 	private Set usedContentVersions = new HashSet();
 	private Set usedSiteNodes = new HashSet();
 	private Set usedSiteNodeVersions = new HashSet();
 	
	private Integer pageMetaInfoContentVersionId = null;
	
 	//private InfoGluePrincipal infoGluePrincipal = null;
 	
 	//This variable sets if all urls generated should contain the server name etc.
 	private boolean useFullUrl = false;
 	
 	//The variable sets if url generation should skip niceUris
 	private boolean disableNiceUri = false;
 
 	//The variable sets if the response string should be trimmed to avoid problems with xml-responses etc.
 	private boolean trimResponse = false;
 
 	//The variable sets if the full page should be rendered once more after all components have been rendered.
 	private boolean evaluateFullPage = true;
 
 	public static DeliveryContext getDeliveryContext()
 	{
 		return new DeliveryContext();
 	}
 	
 	private DeliveryContext()
 	{
 	}
 	/*
 	public static DeliveryContext getDeliveryContext(InfoGluePrincipal infoGluePrincipal)
 	{
 		return new DeliveryContext(infoGluePrincipal);
 	}
 	
 	private DeliveryContext(InfoGluePrincipal infoGluePrincipal)
 	{
 		this.infoGluePrincipal = infoGluePrincipal;
 	}
 	*/
 	
 	public java.lang.Integer getSiteNodeId()
 	{
 		return this.siteNodeId;
 	}
         
 	public void setSiteNodeId(Integer siteNodeId)
 	{
 		this.siteNodeId = siteNodeId;
 	}
 
 	public Integer getContentId()
 	{
 		return this.contentId;
 	}
         
 	public void setContentId(Integer contentId)
 	{
 		this.contentId = contentId;
 	}
 
 	public Integer getLanguageId()
 	{
 		return this.languageId;
 	}
         
 	public void setLanguageId(Integer languageId)
 	{
 		this.languageId = languageId;   
 	}
 
 	public String getRepositoryName()
 	{
 		return this.repositoryName;
 	}
         
 	public void setRepositoryName(String repositoryName)
 	{
 		this.repositoryName = repositoryName;
 	}
 
 	public String getPageKey()
 	{
 		return this.pageKey;
 	}
 
 	public String getPagePath()
 	{
 		return this.pagePath;
 	}
 
 	public void setPageKey(String pageKey)
 	{
 		this.pageKey = pageKey;
 	}
 
 	public void setPagePath(String pagePath)
 	{
 		this.pagePath = pagePath;
 	}
 	
 	/*
 	public InfoGluePrincipal getPrincipal()
 	{
 		return this.infoGluePrincipal;
 	}
 	*/
 	
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append("siteNodeId:" + this.siteNodeId);
 		sb.append("languageId:" + this.languageId);
 		sb.append("contentId:" + this.contentId);
 		//sb.append("InfoGluePrincipal:" + this.infoGluePrincipal);
 		
 		return sb.toString();
 	}
 
 	public Session getSession()
 	{
 		return this.session;
 	}
 
 	public void setSession(Session session)
 	{
 		this.session = session;
 	}
 
 	public InfoGlueAbstractAction getInfoGlueAbstractAction()
 	{
 		return infoglueAbstractAction;
 	}
 
 	public void setInfoGlueAbstractAction(InfoGlueAbstractAction action)
 	{
 	    infoglueAbstractAction = action;
 	}
 
 
 	public HttpServletRequest getHttpServletRequest() 
 	{
 		return httpServletRequest;
 	}
 	
 	public void setHttpServletRequest(HttpServletRequest httpServletRequest) 
 	{
 		this.httpServletRequest = httpServletRequest;
 	}
 	
 	public HttpServletResponse getHttpServletResponse() 
 	{
 		return httpServletResponse;
 	}
 	
 	public void setHttpServletResponse(HttpServletResponse httpServletResponse) 
 	{
 		this.httpServletResponse = httpServletResponse;
 	}
 
     public void addUsedContent(String usedContent)
     {
         this.usedContents.add(usedContent);
 
         Iterator iterator = this.getUsageListeners().iterator();
         while(iterator.hasNext())
         {
             UsageListener usageListener = (UsageListener)iterator.next();
             usageListener.addUsedContent(usedContent);
         }
     }
 
     public void addUsedSiteNode(String usedSiteNode)
     {
         this.usedSiteNodes.add(usedSiteNode);
         
         Iterator iterator = this.getUsageListeners().iterator();
         while(iterator.hasNext())
         {
             UsageListener usageListener = (UsageListener)iterator.next();
             usageListener.addUsedSiteNode(usedSiteNode);
         }
     }
 
     public void addUsedContentVersion(String usedContentVersion)
     {
         this.usedContentVersions.add(usedContentVersion);
         
         Iterator iterator = this.getUsageListeners().iterator();
         while(iterator.hasNext())
         {
             UsageListener usageListener = (UsageListener)iterator.next();
             usageListener.addUsedContentVersion(usedContentVersion);
         }
     }
 
     public void addUsedSiteNodeVersion(String usedSiteNodeVersion)
     {
         this.usedSiteNodeVersions.add(usedSiteNodeVersion);
         
         Iterator iterator = this.getUsageListeners().iterator();
         while(iterator.hasNext())
         {
             UsageListener usageListener = (UsageListener)iterator.next();
             usageListener.addUsedSiteNodeVersion(usedSiteNodeVersion);
         }
     }
 
     public String[] getAllUsedEntities()
     {
         List list = new ArrayList();
         list.addAll(this.usedContents);
         list.addAll(this.usedContentVersions);
         list.addAll(this.usedSiteNodes);
         list.addAll(this.usedSiteNodeVersions);
         Object[] array = list.toArray();
         String[] groups = new String[array.length];
         for(int i=0; i<array.length; i++)
             groups[i] = array[i].toString();
         
         return groups;
     }
     
     public List getUsageListeners()
     {
         return usageListeners;
     }
 
     public boolean getShowSimple()
     {
         return showSimple;
     }
     
     public void setShowSimple(boolean showSimple)
     {
         this.showSimple = showSimple;
     }
     
     public String getContentType()
     {
         return contentType;
     }
     
     public void setContentType(String contentType)
     {
         this.contentType = contentType;
     }
     
     public boolean getDisablePageCache()
     {
         return disablePageCache;
     }
     
     public void setDisablePageCache(boolean disablePageCache)
     {
         this.disablePageCache = disablePageCache;
     }
     
     public boolean getUseFullUrl()
     {
         return useFullUrl;
     }
     
     public void setUseFullUrl(boolean useFullUrl)
     {
         this.useFullUrl = useFullUrl;
     }
 
     public boolean getDisableNiceUri()
     {
         return this.disableNiceUri;
     }
     
     public void setDisableNiceUri(boolean disableNiceUri)
     {
         this.disableNiceUri = disableNiceUri;
     }
 
     public boolean getTrimResponse()
     {
         return this.trimResponse;
     }
 
     public void setTrimResponse(boolean trimResponse) 
 	{
 		this.trimResponse = trimResponse;
 	}
 
 	public boolean getEvaluateFullPage() 
 	{
 		return evaluateFullPage;
 	}
 
 	public void setEvaluateFullPage(boolean evaluateFullPage) 
 	{
 		this.evaluateFullPage = evaluateFullPage;
 	}

	public Integer getPageMetaInfoContentVersionId() 
	{
		return pageMetaInfoContentVersionId;
	}

	public void setPageMetaInfoContentVersionId(Integer pageMetaInfoContentVersionId) 
	{
		if(this.pageMetaInfoContentVersionId == null)
			this.pageMetaInfoContentVersionId = pageMetaInfoContentVersionId;
	}
 }
