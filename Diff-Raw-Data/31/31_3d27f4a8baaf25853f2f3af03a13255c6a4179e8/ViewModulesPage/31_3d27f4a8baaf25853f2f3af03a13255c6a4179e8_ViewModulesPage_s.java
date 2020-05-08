 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2011 Etudes, Inc.
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
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.faces.component.UICommand;
 import javax.faces.component.UIData;
 import javax.faces.component.UIParameter;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.ViewModBeanService;
 import org.etudes.api.app.melete.ViewSecBeanService;
 import org.etudes.component.app.melete.CourseModule;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 
 public class ViewModulesPage implements Serializable
 {
 
 	public ViewModBeanService viewMbean;
 	private Boolean autonumber;
 	private String emptyString = "";
 	/** identifier field */
 	private int moduleId;
 	private int moduleSeqNo;
 	private ModuleService moduleService;
 	private int nextSeqNo;
 	private String nullString = null;
 	private ViewModBeanService prevMbean;
 	private int prevSectionSize;
 	private int prevSeqNo;
 	private Boolean printable;
 	private SectionService sectionService;
 	private int sectionSize;
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ViewModulesPage.class);
 
 	protected ThreadLocalManager threadLocalManager = org.sakaiproject.thread_local.cover.ThreadLocalManager.getInstance();
 	// This needs to be set later using Utils.getBinding
 	String courseId;
 
 	String userId;
 
 	public ViewModulesPage()
 	{
 		courseId = null;
 		userId = null;
 	}
 
 	/**
 	 * @return empty string
 	 */
 	public String getEmptyString()
 	{
 		return emptyString;
 	}
 
 	/**
 	 * Get module date bean, previous sequence number, next sequence number and previous section size
 	 * 
 	 * @return ViewMbean module date bean
 	 */
 	public ViewModBeanService getViewMbean()
 	{
 		// refresh the bean
 		String moduleIdfromOutside = (String) threadLocalManager.get("MELETE_MODULE_ID");
 		if (moduleIdfromOutside != null && new Integer(moduleIdfromOutside).intValue() != this.moduleId)
 		{
 			setViewMbean(null);
 			setPrevMbean(null);
 			setModuleId(new Integer(moduleIdfromOutside).intValue());
 			setModuleSeqNo(0);
 			setPrintable(null);
 			setAutonumber(null);
 		}
 
 		if (this.viewMbean == null)
 		{
 			try
 			{
 				FacesContext ctx = FacesContext.getCurrentInstance();
 				ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 
 				MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(ctx);
 				String courseId = mPage.getCurrentSiteId();
 				String userId = mPage.getCurrentUser().getId();
 
 				if (moduleIdfromOutside != null)
 				{
 					logger.debug("reading module id in view page from thread local :" + moduleIdfromOutside);
 					this.moduleId = new Integer(moduleIdfromOutside).intValue();
 					this.viewMbean = (ViewModBeanService) getModuleService().getViewModBean(userId, courseId, this.moduleId);
 					mPage.populateMeleteSession();
 					String backToOutside = (String) threadLocalManager.get("MELETE_NAVIGATE_BACK");
 					mPage.setNavigateCM(backToOutside);
 					
 					threadLocalManager.set("MELETE_MODULE_ID", null);
 					threadLocalManager.set("MELETE_NAVIGATE_BACK", null);
 				}
 				else
 				{
 					// normal processing if thread local has no value - rashmi
 					if (this.moduleId > 0)
 					{
 						this.viewMbean = (ViewModBeanService) getModuleService().getViewModBean(userId, courseId, this.moduleId);
 					}
 					else
 					{
 						this.viewMbean = (ViewModBeanService) getModuleService().getViewModBeanBySeq(userId, courseId, this.moduleSeqNo);
 					}
 				}
 				if (this.viewMbean != null)
 				{
 					this.moduleId = this.viewMbean.getModuleId();
 					this.moduleSeqNo = this.viewMbean.getSeqNo();
 					this.prevSeqNo = getModuleService().getPrevSeqNo(userId, courseId, this.moduleSeqNo);
 					this.nextSeqNo = getModuleService().getNextSeqNo(userId, courseId, this.moduleSeqNo);
 				}
 				this.prevSectionSize = 0;
 				if ((this.prevSeqNo > 0) && (this.prevSeqNo != this.moduleSeqNo))
 				{
 					this.prevMbean = (ViewModBeanService) getModuleService().getViewModBeanBySeq(userId, courseId, prevSeqNo);
 					if (this.prevMbean != null)
 					{
 						if (this.prevMbean.getVsBeans() != null)
 						{
 							this.prevSectionSize = this.prevMbean.getVsBeans().size();
 						}
 					}
 				}
 
 			}
 			catch (Exception e)
 			{
 				logger.debug(e.toString());
 				e.printStackTrace();
 			}
 		}
 		return this.viewMbean;
 	}
 
 	/**
 	 * @return module id
 	 */
 	public int getModuleId()
 	{
 		return this.moduleId;
 	}
 
 	/**
 	 * @return module seq no
 	 */
 	public int getModuleSeqNo()
 	{
 		return this.moduleSeqNo;
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * @return next seq no
 	 */
 	public int getNextSeqNo()
 	{
 		return this.nextSeqNo;
 	}
 
 	/**
 	 * @return null value for string
 	 */
 	public String getNullString()
 	{
 		return nullString;
 	}
 
 	/**
 	 * @return previous module date bean
 	 */
 	public ViewModBeanService getPrevMbean()
 	{
 		if (this.prevMbean == null)
 		{
 			getViewMbean();
 		}
 		return this.prevMbean;
 	}
 
 	/**
 	 * @return prev section size
 	 */
 	public int getPrevSectionSize()
 	{
 		return this.prevSectionSize;
 	}
 
 	/**
 	 * @return prev seq no
 	 */
 	public int getPrevSeqNo()
 	{
 		return this.prevSeqNo;
 	}
 
 	public void setSectionService(SectionService sectionService)
 	{
 		this.sectionService = sectionService;
 	}
 
 	/**
 	 * Get section size
 	 * 
 	 * @return Section size or 0, if there are no sections
 	 */
 	public int getSectionSize()
 	{
 		this.sectionSize = 0;
 		if (this.viewMbean == null) getViewMbean();
 
 		if (this.viewMbean != null && this.viewMbean.getVsBeans() != null)
 		{
 			this.sectionSize = this.viewMbean.getVsBeans().size();
 		}
 		return this.sectionSize;
 	}
 
 	/**
 	 * Go to next section, set all the values such as section id, module id, section and module
 	 * 
 	 * 
 	 */
 	public void goNextSection(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		
 		ViewSecBeanService secBean = (ViewSecBeanService) this.viewMbean.getVsBeans().get(0);
 		SectionObjService sec = sectionService.getSection(secBean.getSectionId());
 		
 		/*ValueBinding binding = Util.getBinding("#{viewSectionsPage}");
 		ViewSectionsPage vsPage = (ViewSectionsPage) binding.getValue(ctx);
 		vsPage.setSectionId(secBean.getSectionId());
 		vsPage.setModuleId(sec.getModuleId());
 		vsPage.setModuleSeqNo(sec.getModule().getCoursemodule().getSeqNo());
 		vsPage.setSection(null);
 		vsPage.setSection(sec);
 		// added by rashmi on 6/14/05
 		vsPage.setModule(null);
 		vsPage.setAutonumber(null);*/
 		
 		try
 		{
 			ctx.getExternalContext().redirect("view_section.jsf?moduleId="+sec.getModuleId()+"&sectionId="+secBean.getSectionId()+"&moduleSeqNo="+sec.getModule().getCoursemodule().getSeqNo());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * Go to previous or next module
 	 * 
 	 * 
 	 */
 	public void goPrevNext(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		this.moduleSeqNo = new Integer(((String) ctx.getExternalContext().getRequestParameterMap().get("modseqno"))).intValue();
 		this.viewMbean = null;
 		this.moduleId = 0;
 		try
 		{
 			ctx.getExternalContext().redirect("view_module.jsf?modSeqNo="+this.moduleSeqNo);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * Go to previous section, set all the values such as section id, module id, section and module
 	 * 
 	 * 
 	 */
 	public void goPrevSection(ActionEvent evt)
 	{
 		String retVal;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		/*ValueBinding binding = Util.getBinding("#{viewSectionsPage}");
 
 		ViewSectionsPage vsPage = (ViewSectionsPage) binding.getValue(ctx);*/
 		
 		if (this.prevMbean != null)
 		{
 
 			ViewSecBeanService secBean = (ViewSecBeanService) this.prevMbean.getVsBeans().get(this.prevMbean.getVsBeans().size() - 1);
 			SectionObjService sec = sectionService.getSection(secBean.getSectionId());
 			if (sec != null)
 			{
 				/*vsPage.setSectionId(secBean.getSectionId());
 				vsPage.setModuleId(sec.getModuleId());
 				vsPage.setModuleSeqNo(sec.getModule().getCoursemodule().getSeqNo());
 				vsPage.setSection(null);
 				vsPage.setSection(sec);
 				// added by rashmi on 6/14/05
 				vsPage.setModule(null);
 				vsPage.setAutonumber(null);*/
				retVal = "view_section.jsf?moduleId=" + sec.getModuleId() + "&sectionId=" + secBean.getSectionId() + "&moduleSeqNo="
						+ sec.getModule().getCoursemodule().getSeqNo();
 			}
 			else
 			{
 				this.viewMbean = null;
 				retVal = "view_module.jsf?modId=" + this.moduleId;
 			}
 		}
 		else
 		{
 			retVal = "view_module.jsf?modId=" + this.moduleId;
 		}
 
 		try
 		{
 			ctx.getExternalContext().redirect(retVal);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * Go to previous whats next, set all the values such as prev sec id, prev mod id, module and next seq no
 	 * 
 	 * 
 	 */
 	public void goPrevWhatsNext(ActionEvent evt)
 	{
 		int prevSecId = 0, prevModId = 0;
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		/*ValueBinding binding = Util.getBinding("#{viewNextStepsPage}");
 		ViewNextStepsPage vnPage = (ViewNextStepsPage) binding.getValue(context);*/
 		if (this.prevMbean == null) getViewMbean();
 		if (this.prevMbean != null)
 		{
 			if (this.prevMbean.getVsBeans() != null)
 			{
 				prevSecId = ((ViewSecBeanService) this.prevMbean.getVsBeans().get(this.prevMbean.getVsBeans().size() - 1)).getSectionId();
 			}
 			else
 			{
 				prevSecId = 0;
 			}
 			//vnPage.setPrevSecId(prevSecId);
 			prevModId = this.prevMbean.getModuleId();
 			//vnPage.setPrevModId(prevModId);
 			//vnPage.setModule(null);
 			// vnPage.setModule(this.prevMdbean.getModule());
 		}
 		else
 		{
 			//vnPage.setPrevSecId(0);
 		}
 
 		//vnPage.setNextSeqNo(this.moduleSeqNo);
 
 		try
 		{
 			context.getExternalContext().redirect("view_whats_next.jsf?nextSeqNo="+this.moduleSeqNo+"&prevSecId="+prevSecId+"&prevModId="+prevModId);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 
 	}
 
 	/**
 	 * Go to table of contents
 	 * 
 	 * @return list_modules_student or list_modules_inst page
 	 */
 	public String goTOC()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		mPage.setNavigateCM(null);
 		binding = Util.getBinding("#{listModulesPage}");
 		ListModulesPage listPage = (ListModulesPage) binding.getValue(context);
 		listPage.setViewModuleBeans(null);
 		listPage.setAutonumberMaterial(null);
 		return listPage.listViewAction();
 	}
 
 	/**
 	 * Go to whats next, set all the values such as prev sec id, prev mod id, module and next seq no
 	 * 
 	 * 
 	 */
 	public void goWhatsNext(ActionEvent evt)
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		/*ValueBinding binding = Util.getBinding("#{viewNextStepsPage}");
 		ViewNextStepsPage vnPage = (ViewNextStepsPage) binding.getValue(context);
 		vnPage.setPrevSecId(0);
 		vnPage.setPrevModId(this.moduleId);
 
 		vnPage.setNextSeqNo(this.nextSeqNo);
 		vnPage.setModule(null);*/
 
 		// if (this.mdbean != null) vnPage.setModule(this.mdbean.getModule());
 
 		try
 		{
 			context.getExternalContext().redirect("view_whats_next.jsf?nextSeqNo="+this.nextSeqNo+"&prevSecId=0&prevModId="+this.moduleId);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 
 	}
 
 	/**
 	 * Fetch autonumbering preference for course
 	 * 
 	 * @return true if autonumbering is allowed, false if not
 	 */
 	public boolean isAutonumber()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			if (autonumber == null)
 			{
 				ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				if (courseId == null) getCourseId();
 				autonumber = new Boolean(preferencePage.isMaterialAutonumber(courseId));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			autonumber = false;
 		}
 		return autonumber.booleanValue();
 	}
 
 	/**
 	 * Fetch printable preference for course
 	 * 
 	 * @return true if printing is allowed, false if not
 	 */
 	public boolean isPrintable()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			if (printable == null)
 			{
 				ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				if (courseId == null) getCourseId();
 				printable = new Boolean(preferencePage.isMaterialPrintable(courseId));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			printable = false;
 		}
 		return printable.booleanValue();
 	}
 
 	/**
 	 * @param autonumber
 	 *        true if autonumbering is allowed, false if not
 	 */
 	public void setAutonumber(Boolean autonumber)
 	{
 		this.autonumber = autonumber;
 	}
 
 	/**
 	 * @param ViewMbean
 	 *        module date bean
 	 */
 	public void setViewMbean(ViewModBeanService viewMbean)
 	{
 		this.viewMbean = viewMbean;
 	}
 
 	/**
 	 * @param moduleId
 	 *        the module id
 	 */
 	public void setModuleId(int moduleId)
 	{
 		this.moduleId = moduleId;
 	}
 
 	/**
 	 * @param moduleSeqNo
 	 *        module seq no
 	 */
 	public void setModuleSeqNo(int moduleSeqNo)
 	{
 		this.moduleSeqNo = moduleSeqNo;
 	}
 	
 	public void setPrevSeqNo(int prevSeqNo)
 	{
 		this.prevSeqNo = prevSeqNo;
 	}
 	
 	public void setNextSeqNo(int nextSeqNo)
 	{
 		this.nextSeqNo = nextSeqNo;
 	}
 	
 	public void setPrevSectionSize(int prevSectionSize)
 	{
 		this.prevSectionSize = prevSectionSize;
 	}
 
 	/**
 	 * @param moduleService
 	 *        The moduleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService)
 	{
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @param prevMdbean
 	 *        previous module date bean
 	 */
 	public void setPrevMbean(ViewModBeanService prevMbean)
 	{
 		this.prevMbean = prevMbean;
 	}
 
 	/**
 	 * @param printable
 	 *        true if printing is allowed, false if not
 	 */
 	public void setPrintable(Boolean printable)
 	{
 		this.printable = printable;
 	}
 
 	/**
 	 * Populate view module page, setting module id, module sequence number
 	 * 
 	 * @param evt
 	 *        Action event
 	 */
 	public void viewModule(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UICommand cmdLink = (UICommand) evt.getComponent();
 		List cList = cmdLink.getChildren();
 		UIParameter param = new UIParameter();
 		for (int i = 0; i < cList.size(); i++)
 		{
 			Object obj = cList.get(i);
 			if (obj instanceof UIParameter)
 			{
 				param = (UIParameter) cList.get(i);
 			}
 		}
 
 		/*ValueBinding binding = Util.getBinding("#{viewModulesPage}");
 		ViewModulesPage vmPage = (ViewModulesPage) binding.getValue(ctx);*/
 		int modId = ((Integer)param.getValue()).intValue();
 		/*vmPage.setModuleId(modId);
 		vmPage.setViewMbean(null);
 		vmPage.setPrintable(null);
 		vmPage.setAutonumber(null);*/
 		try
 		{
 			ModuleService modServ = getModuleService();
 			CourseModule cMod = (CourseModule) modServ.getCourseModule(modId, getCourseId());
 			int modSeqNo = cMod.getSeqNo();
 			//vmPage.setModuleSeqNo(modSeqNo);
 			try
 			{
 				ctx.getExternalContext().redirect("view_module.jsf?modId=" + modId + "&modSeqNo=" + modSeqNo);
 			}
 			catch (Exception e)
 			{
 				return;
 			}
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 		//vmPage.getViewMbean();
 		
 	}
 
 	/**
 	 * Set up view section page with the right values for section id, module id, module sequene number and section
 	 * 
 	 * 
 	 */
 	public void viewSection(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = null;
 		table = (UIData) root.findComponent("viewmoduleform").findComponent("tablesec");
 		/*ValueBinding binding = Util.getBinding("#{viewSectionsPage}");
 
 		ViewSectionsPage vsPage = (ViewSectionsPage) binding.getValue(ctx);*/
 
 		ViewSecBeanService secBean = (ViewSecBeanService) table.getRowData();
 		SectionObjService sec = sectionService.getSection(secBean.getSectionId());
 		/*vsPage.resetValues();
 		vsPage.setSectionId(secBean.getSectionId());
 		vsPage.setModuleId(sec.getModuleId());
 		vsPage.setModuleSeqNo(sec.getModule().getCoursemodule().getSeqNo());
 		vsPage.setSection(null);
 		vsPage.setSection(sec);
 		// added by rashmi on 6/14/05
 		vsPage.setModule(null);
 		// vsPage.setAutonumber(this.autonumber);*/
 
 		String retVal = "view_section";
 		try
 		{
 			ctx.getExternalContext().redirect("view_section.jsf?moduleId="+sec.getModuleId()+"&sectionId="+secBean.getSectionId()+"&moduleSeqNo="+sec.getModule().getCoursemodule().getSeqNo());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * @return course id
 	 */
 	private String getCourseId()
 	{
 		if (courseId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			courseId = mPage.getCurrentSiteId();
 		}
 		return courseId;
 	}
 
 	/**
 	 * @return user id
 	 */
 	private String getUserId()
 	{
 		if (userId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			userId = mPage.getCurrentUser().getId();
 		}
 		return userId;
 	}
 
 }
