 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 package org.etudes.tool.melete;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.*;
 import org.etudes.api.app.melete.*;
 
 import javax.faces.application.Application;
 import javax.faces.component.html.*;
 import javax.faces.component.*;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.io.*;
 
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 
 //import com.sun.faces.util.Util;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionService;
 //import org.sakaiproject.jsf.ToolBean;
 
 import org.etudes.api.app.melete.MeleteCHService;
 
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.util.ResourceLoader;
 
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.etudes.simpleti.SakaiSimpleLTI;
 import org.imsglobal.simplelti.SimpleLTIUtil;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 /*
  * Mallika - 8/15/06 - Adding code to get content from resources
  * Mallika - 8/16/06 - Adding code to prev next
  * Mallika - 2/8/07 - Adding code to consolidate view section
  * Mallika - 2/27/07 - Adding fix for bug report
  * Mallika - 4/25/07 - Adding prev next
  * Rashmi - 5/10/07 - removing code from constructor
  * Mallika - 5/23/07 - adding getContent method
  * Mallika - 5/31/07 - using meleteCHServiceImpl
  **/
 public class ViewSectionsPage implements Serializable/*,ToolBean */{
 
 
 	  /** identifier field */
 	  private int moduleId;
 	  private int sectionId;
       private int moduleSeqNo;
       private int prevSecId;
       private int nextSecId;
       private int nextSeqNo;
 
       public SectionObjService section;
       public ModuleObjService module;
       private boolean instRole;
 
       private String typeEditor;
       private String typeLink;
       private String typeUpload;
       private String sectionContentType;
       String courseId;
       String userId;
 
       private ModuleService moduleService;
       private SectionService sectionService;
 
   	  private MeleteCHService meleteCHService;
 
 	  private String sectionDisplaySequence;
 
       /** Dependency:  The logging service. */
       protected Log logger = LogFactory.getLog(ViewSectionsPage.class);
 
       private String  nullString = null;
       private String emptyString = "";
       public HtmlPanelGroup secpgroup;
       private org.w3c.dom.Document subSectionW3CDom;
       private String linkName;
       private Boolean autonumber;
 
       // added to reduce queries
       private String contentLinkUrl;
 
 	  public ViewSectionsPage(){
 	  	courseId = null;
 	  	userId = null;
 	  	contentLinkUrl = null;
 	  }
 
 	  //Code to test
 
       public String getTypeEditor(){
 	  	return "typeEditor";
 	  }
 
 	  public String getTypeLink(){
 	  	return "typeLink";
 	  }
 
 	  public String getTypeLTI(){
 	  	return "typeLTI";
 	  }
 
 	  public String getTypeUpload(){
 	  	return "typeUpload";
 	  }
 
 	  public String  getNullString() {
 	  	return nullString;
 	  }
 
 	  public String  getEmptyString() {
 	  	return emptyString;
 	  }
 
 	  public void resetValues()
 	  {
 		  contentLinkUrl =null;
 		  autonumber = null;
 	  }
 
 	private ContentResource getContentResource()
 	{
 		if (this.section == null) return null;
 		SectionResourceService secRes = this.section.getSectionResource();
 		if ( secRes == null ) return null;
 		if ( secRes.getResource() == null ) return null;
 		String resourceId = secRes.getResource().getResourceId();
 		if ( resourceId == null ) return null;
 		try
 		{
 			ContentResource resource = getMeleteCHService().getResource(resourceId);
 			return resource;
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 		return null;
 	}
 
 	  /*
 	   * Added by rashmi to fix bug#282 - 3/10/05
 	   * show link and uploaded file in the same window and same frame.	   *
 	   */
 	  public String getContent()
 	  {
 		ContentResource resource = getContentResource();
 		if ( resource == null ) return "";
 		String str = null;
 		try
 		{
 			byte[] rsrcArray = resource.getContent();
 			str = new String(rsrcArray);
 		}
 		catch(Exception e)
 		{
 			logger.debug(e.toString());
 			return "";
 		}
 		return str;
 
 /* Moved to getContentResource() -- Chuck 12/08/2008 -- Delete after QA
 
 		SectionResourceService secRes = null;
 		if (this.section != null)
 		{
 		  secRes = this.section.getSectionResource();
 		}
 		String resourceId = null;
 		if (secRes != null)
 		{
 		if (secRes.getResource() != null)
 		{
 	    resourceId = secRes.getResource().getResourceId();
 	    ContentResource resource = null;
     	String url = "#";
     	String record = "";
     	if (resourceId != null)
     	{
     	      try
               {
               resource = getMeleteCHService().getResource(resourceId);
                 if (resource!= null)
                 {
             	   byte[] rsrcArray = resource.getContent();
             	   record = new String(rsrcArray);
                 }
               }
               catch (Exception e)
               {
               record = "";
         	  e.printStackTrace();
               }
     	}
     	 return record;
 		}
 		}
 		return "";
 */
 	  }
 
 	public String getContentLTI()
 	{
 		ContentResource resource = getContentResource();
 		if ( resource == null )
 		{
 			return "";
 		}
 		String str = null;
 		try
 		{
 			byte[] rsrcArray = resource.getContent();
 			if ( rsrcArray == null )
 			{
 				if (logger.isDebugEnabled())
 					logger.debug("Resource has no content"+resource.getId());
 				return "";
                         }
 			str = new String(rsrcArray);
 		}
 		catch(Exception e)
 		{
 			logger.debug(e.toString());
 			return "";
 		}
 
 		// Check to see if we are doing a POST...
 		if ( SimpleLTIUtil.isPostLaunch(str) )
 		{
 			return getContentPost(str);
 		}
 
 		String context = ToolManager.getCurrentPlacement().getContext();
 
 		// TODO: Deal with POST!!! Return an iFrame
 		Properties props = SakaiSimpleLTI.doLaunch(str, context, resource.getId());
 
 		// The resource *insisted* on a POST
 		if ( SimpleLTIUtil.isPostLaunch(props) )
 		{
 			return getContentPost(str);
 		}
 
 		String htmltext = props.getProperty("htmltext");
 		if ( htmltext != null )
 		{
 			return htmltext;
 		}
 
 		// htmltext not returned from launch
 		if (logger.isDebugEnabled())
 			logger.debug("Unable to get htmltext for "+resource.getId());
 		return "";
 	  }
 
 	private String getContentPost(String str)
 	{
 		String frameHeight = SimpleLTIUtil.getFrameHeight(str);
 		String htmltext = "<iframe id=\"iframeLTIPost\" src=\"" +
 			getContentLink() +
 			"\" style=\"visibility:visible\" scrolling= \"auto\" width=\"100%\" " +
 			" marginwidth=\"0\" marginheight=\"0\" " +
 			"height=\"" + frameHeight + "\"  border=\"0\" frameborder= \"0\"></iframe>";
 		return htmltext;
 	}
 
 	  public String getContentLink()
 	  {
 		String url = null;
 		if (this.section == null) return null;
 		if(contentLinkUrl == null)
 		{
 		SectionResourceService secRes = this.section.getSectionResource();
 		String resourceId = null;
 		if (secRes != null && (secRes.getResource() != null))
 			{
 				resourceId = secRes.getResource().getResourceId();
 			}
 	    ContentResource resource = null;
 
 	    if (resourceId != null)
     	{
     	    try
     	    {
               resource = getMeleteCHService().getResource(resourceId);
               setLinkName(resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
              
               url = getMeleteCHService().getResourceUrl(resourceId);
               if (logger.isDebugEnabled()) logger.debug("Resource url is "+url);
       	      contentLinkUrl = url;
               }
               catch (Exception e)
               {
               url = null;
               contentLinkUrl = null;
         	  logger.debug(e.toString());
               }
     	}
 		}
 		logger.debug("content link value send is" + contentLinkUrl);
     	 return contentLinkUrl;
 	  }
 
 	  public String getSectionContentType()
 	  {
 		if(this.section != null && this.section.getContentType() != null)
 		  sectionContentType = this.section.getContentType();
 		else sectionContentType = "notype";
 
 		return sectionContentType;
 	  }
 
 
 	  public String getLinkName()
 	  {
 		  return linkName;
 	  }
 
 	  public void setLinkName(String linkName)
 	  {
 		  this.linkName = linkName;
 	  }
   /**
 	   * @return Returns the ModuleService.
 	   */
 	  public ModuleService getModuleService() {
 		return moduleService;
 	  }
 
 	 /**
 	  * @param moduleService The moduleService to set.
 	  */
 	  public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	  }
 
   /**
 	   * @return Returns the SectionService.
 	   */
 	  public SectionService getSectionService() {
 		return sectionService;
 	  }
 
 	 /**
 	  * @param sectionService The sectionService to set.
 	  */
 	  public void setSectionService(SectionService sectionService) {
 		this.sectionService = sectionService;
 	  }
 
 
 
 
 	  public int getSectionId() {
         return this.sectionId;
     }
 
     public void setSectionId(int sectionId) {
         this.sectionId = sectionId;
         logSectionReadEvent(sectionId);
     }
 
     public int getModuleSeqNo() {
         return this.moduleSeqNo;
     }
 
     public void setModuleSeqNo(int moduleSeqNo) {
         this.moduleSeqNo = moduleSeqNo;
     }
 
     public int getModuleId() {
     	return moduleId;
     }
 
     public void setModuleId(int moduleId) {
     	this.moduleId = moduleId;
     }
 
     public int getPrevSecId() {
     	if (this.module == null) getModule();
     	return prevSecId;
     }
 
 
     public int getNextSecId() {
     	if (this.module == null) getModule();
     	return nextSecId;
     }
 
     public int getNextSeqNo() {
     	return nextSeqNo;
     }
 
     public boolean getInstRole()
     {
     	FacesContext context = FacesContext.getCurrentInstance();
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 		if ((String)sessionMap.get("role") !=null)
 	  		this.instRole = ((String)sessionMap.get("role")).equals("INSTRUCTOR");
 	  	else this.instRole = false;
 		return instRole;
     }
 
     public void setInstRole(boolean instRole)
     {
     	this.instRole = instRole;
     }
 
     public ModuleObjService getModule()
     {
     	Element secElement;
     	Node prevNode,nextNode;
    	String courseId = null;
     	FacesContext ctx = FacesContext.getCurrentInstance();
     	logger.debug("get Module at viewsection java param value" + ctx.getExternalContext().getRequestParameterMap().get("vs_id"));
     	String directvs_id = (String)ctx.getExternalContext().getRequestParameterMap().get("vs_id");
     	if(directvs_id != null) 
     	{
     		int d_vs_id=new Integer(directvs_id).intValue();
     		if(d_vs_id != this.sectionId)
     		{
     			this.sectionId=d_vs_id;
     			this.moduleId = getSectionService().getSection(sectionId).getModuleId();
     			this.module = null;
     			this.section= null;
     		}
     		String direct_cid = (String)ctx.getExternalContext().getRequestParameterMap().get("c_id");
         	if(direct_cid != null) courseId = direct_cid;        		
     	}
     	
     	if (this.module == null)
     	{    		
     	try {
     		if(courseId == null) courseId = getCourseId();	
   	  	  this.module = (ModuleObjService) getModuleService().getModule(this.moduleId);
   	  	  this.nextSeqNo = getModuleService().getNextSeqNo(courseId, this.moduleSeqNo, getInstRole());
   	  	  this.subSectionW3CDom = getModuleService().getSubSectionW3CDOM(this.module.getSeqXml());
   	  	  secElement = subSectionW3CDom.getElementById(String.valueOf(this.sectionId));
   	  	  prevNode = getPreviousNode(secElement);
 		  if (prevNode != null)
 		  {
 		    this.prevSecId = Integer.parseInt(prevNode.getAttributes().getNamedItem("id").getNodeValue());
 		  }
 		  else
 		  {
 			this.prevSecId = 0;
 		  }
 		  nextNode = getNextNode(secElement);
 		  if (nextNode != null)
 		  {
 		    this.nextSecId = Integer.parseInt(nextNode.getAttributes().getNamedItem("id").getNodeValue());
 		  }
 		  else
 		  {
 			this.nextSecId = 0;
 		  }
 
   	  	}
   	  	catch (Exception e)
           {
   	  		logger.debug(e.toString());
           }
     	}
     	return this.module;
     }
 
     private Node getPreviousNode(Element secElement)
     {
 
     	if (secElement.getPreviousSibling() != null)
     	{
     	    if (secElement.getPreviousSibling().hasChildNodes() == false)
     	    {
     		  return secElement.getPreviousSibling();
     	    }
     	    else
     	    {
     	    	return getInnerLastChild((Element)secElement.getPreviousSibling());
     	    }
     	}
     	else
     	{
     		if (secElement.getParentNode() != null)
     		{
 
     	      if (secElement.getParentNode().getNodeName().equals("module"))
     	      {
     	    	  return null;
     	      }
     	      else
     	      {
     		    return secElement.getParentNode();
     	      }
     		}
     		else
     		{
     		  return null;
     		}
     	}
     }
     private Node getInnerLastChild(Element secElement)
     {
     	if (secElement.getLastChild().hasChildNodes() == false)
     	{
     		return secElement.getLastChild();
     	}
     	else
     	{
     		return getInnerLastChild((Element)secElement.getLastChild());
     	}
     }
     private Node getNextNode(Element secElement)
     {
     	if (secElement.hasChildNodes())
     	{
     		return secElement.getFirstChild();
     	}
     	else
     	{
     	  if (secElement.getNextSibling() != null)
     	  {
     		return secElement.getNextSibling();
     	  }
     	  else
     	  {
     		if (secElement.getParentNode() != null)
     		{
     		  if (secElement.getParentNode().getNodeName().equals("module"))
       	      {
     			  return null;
       	      }
       	      else
       	      {
     		    return getParentsNextSibling(secElement);
       	      }
     		}
     		else
     		{
     		  return null;
     		}
     	  }
     	}
     }
     private Node getParentsNextSibling(Element secElement)
     {
     	if (secElement.getParentNode().getNodeName().equals("module"))
 	    {
     		return null;
 	    }
     	if (secElement.getParentNode().getNextSibling() == null)
     	{
     		return getParentsNextSibling((Element)secElement.getParentNode());
     	}
     	else
     	{
     		if (secElement != null)
     		{
     			if (secElement.getParentNode() != null)
     			{
     				return secElement.getParentNode().getNextSibling();
     			}
     		}
 
     	}
     	return null;
     }
 
     public void setModule(ModuleObjService module){
       this.module = module;
     }
 
     public SectionObjService getSection()
     {
     	 try
 		{
 			if (this.section == null)
 			{
 				this.section = (SectionObjService) getSectionService().getSection(this.sectionId);
 				this.sectionDisplaySequence=getSectionService().getSectionDisplaySequence(this.section);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 
     	return this.section;
     }
 
     public void setSectionDisplaySequence(String sectionDisplaySequence){
 	          this.sectionDisplaySequence = sectionDisplaySequence;
 		      }
 
         public String getSectionDisplaySequence(){
 
                       if (this.sectionDisplaySequence == null) {
 			      this.section = (SectionObjService) getSectionService().getSection(this.sectionId);
 			      this.sectionDisplaySequence=getSectionService().getSectionDisplaySequence(this.section);
 			};
 		      return this.sectionDisplaySequence;
 		          }
     public void setSection(SectionObjService section){
       this.section = section;
       this.sectionDisplaySequence=getSectionService().getSectionDisplaySequence(this.section);
     }
 
 
 
 // add by rashmi for navigation on view sections page
 
 public String goTOC()
 {
 	resetValues();
 	FacesContext context = FacesContext.getCurrentInstance();
 	ValueBinding binding = Util.getBinding("#{listModulesPage}");
 	ListModulesPage listPage = (ListModulesPage)
         binding.getValue(context);
 	listPage.setViewModuleBeans(null);
 	listPage.setAutonumberMaterial(null);
 	if (getInstRole()) return "list_modules_inst";
 	else return "list_modules_student";
 }
 
 public String goPrevNext()
 {
 	resetValues();
 	FacesContext context = FacesContext.getCurrentInstance();
 	this.section = null;
 	//this.module = null;
 	String moduleIdStr = (String)context.getExternalContext().getRequestParameterMap().get("modid");
 	String sectionIdStr = (String)context.getExternalContext().getRequestParameterMap().get("secid");
 	if (moduleIdStr != null)
 	{
 		if (moduleIdStr.trim().length() > 0)
 		{
 		  setModuleId(new Integer(moduleIdStr).intValue());
 		}
 	}
 	if (sectionIdStr != null)
 	{
 		if (sectionIdStr.trim().length() > 0)
 		{
 	      setSectionId(new Integer(sectionIdStr).intValue());
 		}
 	}
 	this.module = null;
 	return "view_section";
 }
 
 public String goWhatsNext()
 {
 	resetValues();
 	FacesContext context = FacesContext.getCurrentInstance();
 	int currSeqNo = new Integer(((String)context.getExternalContext().getRequestParameterMap().get("modseqno"))).intValue();
 
 	ValueBinding binding =
         Util.getBinding("#{viewNextStepsPage}");
       ViewNextStepsPage vnPage = (ViewNextStepsPage)
         binding.getValue(context);
 	vnPage.setPrevSecId(this.sectionId);
 	vnPage.setPrevModId(this.moduleId);
 	vnPage.setNextSeqNo(this.nextSeqNo);
 	vnPage.setModuleSeqNo(this.moduleSeqNo);
 
     vnPage.setModule(this.module);
 
 	return "view_whats_next";
 }
 
 public String goPrevModule()
 {
 	resetValues();
 	FacesContext context = FacesContext.getCurrentInstance();
 	this.section = null;
 	//this.module = null;
 	String moduleIdStr = (String)context.getExternalContext().getRequestParameterMap().get("modid");
 	if (moduleIdStr != null)
 	{
 		if (moduleIdStr.trim().length() > 0)
 		{
 		  this.moduleId = new Integer(moduleIdStr).intValue();
 		}
 	}
 	this.module = null;
 	ValueBinding binding =
         Util.getBinding("#{viewModulesPage}");
       ViewModulesPage vmPage = (ViewModulesPage)
         binding.getValue(context);
     vmPage.setModuleId(this.moduleId);
     vmPage.setPrintable(null);
     vmPage.setAutonumber(null);
   	vmPage.setMdbean(null);
   	vmPage.setPrevMdbean(null);
     vmPage.setModuleSeqNo(this.moduleSeqNo);
 
     return "view_module";
 }
 
 public String goNextModule()
 {
 	resetValues();
 	FacesContext context = FacesContext.getCurrentInstance();
 	this.section = null;
 	//this.module = null;
 	String modSeqNoStr = (String)context.getExternalContext().getRequestParameterMap().get("modseqno");
 	if ((modSeqNoStr == null)||(modSeqNoStr.length() == 0)) modSeqNoStr = "0";
 	int nextSeqNo = getModuleService().getNextSeqNo(getCourseId(),new Integer(modSeqNoStr).intValue(),getInstRole());
 	//ModuleDateBean nextMdBean = (ModuleDateBean) getModuleService().getModuleDateBeanBySeq(getUserId(),getCourseId(),nextSeqNo);
 	this.module = null;
 	ValueBinding binding =
         Util.getBinding("#{viewModulesPage}");
       ViewModulesPage vmPage = (ViewModulesPage)
         binding.getValue(context);
 
   /*  if (nextMdBean != null)
     {
     	vmPage.setModuleId(nextMdBean.getModuleId());
     }*/
   	vmPage.setMdbean(null);
   	vmPage.setPrevMdbean(null);
   	vmPage.setModuleId(0);
     vmPage.setModuleSeqNo(this.nextSeqNo);
     vmPage.setPrintable(null);
     vmPage.setAutonumber(null);
     
     return "view_module";
 }
 
 public String gotoAddBookmark()
 {
 	return "add_bookmark";
 }
 
 public String gotoMyBookmarks()
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	ValueBinding binding =
         Util.getBinding("#{bookmarkPage}");
     BookmarkPage bmPage = (BookmarkPage)binding.getValue(context);
     bmPage.resetValues();
 	return "list_bookmarks";
 }
 
 
 /*
  * section breadcrumps in format module title >> section title
  */
 public HtmlPanelGroup getSecpgroup() {
 	  return null;
     }
 
 public void setSecpgroup(HtmlPanelGroup secpgroup)
 {
 	FacesContext context = FacesContext.getCurrentInstance();
 	 Application app = context.getApplication();
 
 	 List list = secpgroup.getChildren();
 	 list.clear();
 
 	 //1. add module as commandlink and it takes to view module page
 	 Class[] param = new Class[1];
 	 HtmlCommandLink modLink = new HtmlCommandLink();
      param[0] = new ActionEvent(modLink).getClass();
      modLink.setId("modSeclink");
      modLink.setActionListener(app.createMethodBinding("#{viewModulesPage.viewModule}", param));
      modLink.setAction(app.createMethodBinding("#{viewModulesPage.redirectToViewModule}", null));
     //1a . add outputtext to display module title
      HtmlOutputText outModule = new HtmlOutputText();
      outModule.setId("modtext");
      if(this.module == null) getModule();
      if (this.module != null)
      {
        outModule.setValue(this.module.getTitle());
      }
      //1b. param to set module id
      UIParameter modidParam = new UIParameter();
      modidParam.setName("modid");
      if (this.module != null)
      {
        modidParam.setValue(this.module.getModuleId());
      }
      modLink.getChildren().add(outModule);
      modLink.getChildren().add(modidParam);
      list.add(modLink);
 
      //2. add >>
      HtmlOutputText seperatorText = new HtmlOutputText();
      seperatorText.setId("sep1");
      seperatorText.setTitle(" "+(char)187+" ");
      seperatorText.setValue(" "+(char)187+" ");
      list.add(seperatorText);
 
 	 // note: when subsections are in place then find all parents of subsection
 	 // and in a while or for loop create commandlink with action/action listener as viewSection
 
 	 //3. add current section title
 	 HtmlOutputText currSectionText = new HtmlOutputText();
 	 currSectionText.setId("currsectext");
      if(this.section == null) getSection();
      if (this.section != null)
      {
        currSectionText.setValue(this.section.getTitle());
      }
 	 list.add(currSectionText);
 
 	 this.secpgroup = secpgroup;
 }
 
 private String getCourseId()
 {
 	if (courseId == null)
 	{
 	FacesContext context = FacesContext.getCurrentInstance();
   	Map sessionMap = context.getExternalContext().getSessionMap();
 	courseId = (String)sessionMap.get("courseId");
 	}
 	return courseId;
 }
 private String getUserId()
 {
 	if (userId == null)
 	{
 	FacesContext context = FacesContext.getCurrentInstance();
   	Map sessionMap = context.getExternalContext().getSessionMap();
 	userId = (String)sessionMap.get("userId");
 	}
 	return userId;
 }
 
 /**
  * @return Returns the meleteCHService.
  */
 public MeleteCHService getMeleteCHService() {
 	return meleteCHService;
 }
 /**
  * @param meleteCHService The meleteCHService to set.
  */
 public void setMeleteCHService(MeleteCHService meleteCHService) {
 	this.meleteCHService = meleteCHService;
 }
     public boolean isAutonumber()
     {
      FacesContext ctx = FacesContext.getCurrentInstance();
      try{
          if(autonumber == null)
          {
           ValueBinding binding = Util.getBinding("#{authorPreferences}");
           AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(ctx);
           if (courseId == null) getCourseId();
           autonumber = new Boolean(preferencePage.isMaterialAutonumber(courseId));
          }
      }
       catch(Exception e){e.printStackTrace();
       autonumber=false;}
      return autonumber.booleanValue();
     }
 
     public void setAutonumber(Boolean autonumber)
     {
      this.autonumber = autonumber;
     }
 
     private void logSectionReadEvent(int sectionId) {
     	// Log melete.section.read event, if not logged yet on this user session
 		String readEvent = "melete.section.read";
 		String reference = readEvent + "-" + String.valueOf(sectionId);
 		String sessionValue = (String) SessionManager.getCurrentSession().getAttribute(reference);
 		if (sessionValue == null || sessionValue.equals("")) {
 			SessionManager.getCurrentSession().setAttribute(reference, "true");
 			EventTrackingService.post(EventTrackingService.newEvent(readEvent, ToolManager.getCurrentPlacement().getContext(), true));
 		}
     }
 }
 
