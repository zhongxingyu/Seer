 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009,2010,2011 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 
 package org.etudes.component.app.melete;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.CourseModuleService;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.MeleteExportService;
 import org.etudes.api.app.melete.ModuleDateBeanService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.ModuleShdatesService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.ViewModBeanService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.hibernate.HibernateException;
 
 public class ModuleServiceImpl implements ModuleService, Serializable
 {
 	public final static int CC_CODE = 3;
 
 	public final static int Copyright_CODE = 1;
 	public final static int FU_CODE = 4;
 	// constants
 	public final static int NO_CODE = 0;
 	public final static int PD_CODE = 2;
 	/** Dependency: The logging service. */
 	private Log logger = LogFactory.getLog(ModuleServiceImpl.class);
 	private ModuleDateBean mdBean = null;
 	private MeleteCHService meleteCHService;
 
 	private Module module = null;
 
 	private ModuleDB moduledb;
 
 	private SectionService sectionService;
 	private List<?> viewModuleBeans = null;
 	/** Dependency: The melete import export service. */
 	protected MeleteExportService meleteExportService;
 
 	public ModuleServiceImpl()
 	{
 		if (moduledb == null) moduledb = getModuledb();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void applyBaseDateTx(String course_id, int days_diff)
 	{
 		moduledb.applyBaseDateTx(course_id, days_diff);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 */
 	public void archiveModules(List<ModuleDateBeanService> selModBeans, List<ModuleDateBeanService> moduleDateBeans, String courseId)
 	throws Exception
 	{
 
 		try
 		{
 			moduledb.archiveModules(selModBeans, moduleDateBeans, courseId);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 			throw new MeleteException("archive_fail");
 		}
 		catch (Exception ex)
 		{
 			throw new MeleteException("archive_fail");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void bringOneLevelUp(ModuleObjService module, List<? extends SectionBeanService> secBeans) throws MeleteException
 	{
 		moduledb.bringOneLevelUp((Module) module, secBeans);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean checkCalendar(String courseId)
 	{
 		return moduledb.checkCalendar(courseId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean checkEditAccess(String user_id, String course_id)
 	{
 		return moduledb.checkEditAccess(user_id, course_id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int cleanUpDeletedModules(String userId) throws Exception
 	{
 		int noOfDeleted = moduledb.cleanUpDeletedModules(userId);
 		return noOfDeleted;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void copyModule(ModuleObjService module, String courseId, String userId) throws MeleteException
 	{
 		moduledb.copyModule((Module) module, courseId, userId);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void createSubSection(ModuleObjService module, List<? extends SectionBeanService> secBeans) throws MeleteException
 	{
 		moduledb.createSubSection((Module) module, secBeans);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deleteModules(List<? extends ModuleObjService> delModules, String courseId, String userId) throws Exception
 	{
 		List<? extends ModuleObjService> allModules = new ArrayList<Module>(0);
 
 		moduledb.deleteCalendar(delModules, courseId);
 		try
 		{
 			allModules = moduledb.getActivenArchiveModules(courseId);
 			moduledb.deleteModules(delModules, allModules, courseId, userId);
 		}
 		catch (Exception ex)
 		{
 			throw new MeleteException("delete_module_fail");
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @return
 	 */
 	public List<CourseModuleService> getArchiveModules(String course_id)
 	{
 		List<CourseModuleService> archModules = null;
 		try
 		{
 			archModules = moduledb.getArchivedModules(course_id);
 		}
 		catch (Exception ex)
 		{
 			logger.debug("ManageModulesBusiness --get Archive Modules failed");
 		}
 		return archModules;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public CourseModuleService getCourseModule(int moduleId, String courseId) throws Exception
 	{
 		CourseModule cMod = null;
 		try
 		{
 			cMod = moduledb.getCourseModule(moduleId, courseId);
 		}
 		catch (Exception ex)
 		{
 			logger.debug("ManageModulesBusiness --get Archive Modules failed");
 		}
 		return cMod;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getCourseModuleSize(String courseId)
 	{
 		return moduledb.getCourseModuleSize(courseId);
 	}
 
 	/**
 	 * @return meleteChService
 	 */
 	public MeleteCHService getMeleteCHService()
 	{
 		return meleteCHService;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Date getMinStartDate(String course_id)
 	{
 		return moduledb.getMinStartDate(course_id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ModuleObjService getModule(int moduleId)
 	{
 		try
 		{
 			module = moduledb.getModule(moduleId);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return module;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ModuleDateBeanService getModuleDateBean(String userId, String courseId, int moduleId)
 	{
 		if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
 		try
 		{
 			mdBean = moduledb.getModuleDateBean(userId, courseId, moduleId);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return mdBean;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ModuleDateBeanService getModuleDateBeanBySeq(String userId, String courseId, int seqNo)
 	{
 		if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 
 		try
 		{
 			mdBean = moduledb.getModuleDateBeanBySeq(userId, courseId, seqNo);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return mdBean;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ModuleDateBeanService> getModuleDateBeans(String userId, String courseId)
 	{
 		if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 		List<ModuleDateBeanService> moduleDateBeans = null;
 		try
 		{
 			moduleDateBeans = moduledb.getShownModulesAndDatesForInstructor(userId, courseId);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return moduleDateBeans;
 	}
 
 	/**
 	 * @return moduledb Returns the moduledb.
 	 */
 	public ModuleDB getModuledb()
 	{
 		return moduledb;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ModuleObjService> getModules(String courseId)
 	{
 		List<ModuleObjService> modules = null;
 		try
 		{
 			modules = moduledb.getModules(courseId);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return modules;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getNextSeqNo(String userId, String courseId, int currSeqNo)
 	{
 		int nextseq = 0;
 		nextseq = moduledb.getNextSeqNo(userId, courseId, currSeqNo);
 		return nextseq;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Map<String, Integer> getNumberOfModulesCompletedByUserId(String course_id)
 	{
 		return moduledb.getNumberOfModulesCompletedByUserId(course_id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getNumberOfSectionsReadFromModule(String user_id, int module_id)
 	{
 		return moduledb.getNumberOfSectionsReadFromModule(user_id, module_id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getPrevSeqNo(String userId, String courseId, int currSeqNo)
 	{
 		int prevseq = 0;
 		prevseq = moduledb.getPrevSeqNo(userId, courseId, currSeqNo);
 		return prevseq;
 	}
 
 	/**
 	 * @return sectionService
 	 */
 	public SectionService getSectionService()
 	{
 		return sectionService;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public org.w3c.dom.Document getSubSectionW3CDOM(String sectionsSeqXML)
 	{
 		SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 		org.w3c.dom.Document subSectionW3CDOM = ssuImpl.getSubSectionW3CDOM(sectionsSeqXML);
 		return subSectionW3CDOM;
 
 	}
 
 	/**
 	 * @return list of viewModuleBeans
 	 */
 	public List getViewModuleBeans()
 	{
 		return this.viewModuleBeans;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ViewModBeanService> getViewModules(String userId, String courseId, boolean fromCourseMap, boolean filtered)
 	{
 		if (moduledb == null) moduledb = ModuleDB.getModuleDB();
 		List<ViewModBeanService> viewModuleBeans = null;
 		try
 		{
 			viewModuleBeans = moduledb.getViewModulesAndDates(userId, courseId, fromCourseMap, filtered);
 		}
 		catch (HibernateException e)
 		{
 			// e.printStackTrace();
 			logger.debug(e.toString());
 		}
 		return viewModuleBeans;
 	}
 
 	public void init()
 	{
 		logger.info(this + ".init()");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void insertProperties(ModuleObjService module, ModuleShdatesService moduleshdates, String userId, String courseId) throws Exception
 	{
 
 		// module object and moduleshdates are provided by ui pages
 
 		Module module1 = (Module) module;
 		ModuleShdates moduleshdates1 = (ModuleShdates) moduleshdates;
 
 		// insert new module
 		moduledb.addModule(module1, moduleshdates1, userId, courseId);
 
 		if (moduleshdates1.getAddtoSchedule() != null)
 		{
 			if (moduleshdates1.getAddtoSchedule().booleanValue() == true)
 			{
 				moduledb.updateCalendar(module1, moduleshdates1, courseId);
 			}
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isModuleCompleted(String user_id, int module_id)
 	{
 		return moduledb.isModuleCompleted(user_id, module_id);
 	}
 
 	/**
 	 * Checks if the module is open at a given time
 	 * 
 	 * @param openDate
 	 *        Start date
 	 * @param closeDate
 	 *        End date
 	 * @return invalid if bad dates or not able to determine the status. open if module is open and active closed if module is closed later if module is not opened yet
 	 */
 	public String isSectionModuleOpen(Date startDate, Date endDate)
 	{
 		String access = "invalid";
 		Date currentDate = new java.util.Date();
 
 		// check invalid
 		if (startDate != null && endDate != null && startDate.compareTo(endDate) >= 0)
 		{
 			return access;
 		}
 
 		// check for open, close, not yet
 		if ((startDate == null || startDate.before(currentDate)) && (endDate == null || endDate.after(currentDate)))
 		{
 			access = "open";
 		}
 		else
 		{
 			if (startDate != null && startDate.after(currentDate))
 			{
 				access = "later";
 			}
 
 			if (endDate != null && endDate.before(currentDate))
 			{
 				access = "closed";
 			}
 		}
 		return access;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void moveSections(String courseId, List<? extends SectionBeanService> sectionBeans, ModuleObjService selectedModule)
 			throws MeleteException
 	{
 		try
 		{
 			for (ListIterator<?> i = sectionBeans.listIterator(); i.hasNext();)
 			{
 				SectionBean moveSectionBean = (SectionBean) i.next();
 				if (moveSectionBean.getSection().getModuleId() != selectedModule.getModuleId().intValue())
 					moduledb.moveSection(courseId, moveSectionBean.getSection(), (Module) selectedModule);
 			}
 		}
 		catch (Exception ex)
 		{
 			throw new MeleteException("move_section_fail");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String printModule(ModuleObjService module) throws MeleteException
 	{
 		try
 		{
 
 			return moduledb.prepareModuleSectionsForPrint((Module) module);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw new MeleteException("print_module_fail");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void restoreModules(List modules, String courseId) throws Exception
 	{
 
 		try
 		{
 			moduledb.restoreModules(modules, courseId);
 		}
 		catch (Exception ex)
 		{
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("ManageModulesBusiness --restore Modules failed");
 				ex.printStackTrace();
 			}
 			throw new MeleteException(ex.toString());
 		}
 	}
 
 	/**
 	 * @param meleteCHService
 	 *        The meleteChService to set
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setModule(ModuleObjService mod)
 	{
 		module = (Module) mod;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setModuleDateBean(ModuleDateBeanService mdBean)
 	{
 		this.mdBean = (ModuleDateBean) mdBean;
 	}
 
 	/**
 	 * @param moduledb
 	 *        The moduledb to set.
 	 */
 	public void setModuledb(ModuleDB moduledb)
 	{
 		this.moduledb = moduledb;
 	}
 
 	/**
 	 * @param sectionService
 	 *        The sectionService to set
 	 */
 	public void setSectionService(SectionService sectionService)
 	{
 		this.sectionService = sectionService;
 	}
 
 	/**
 	 * @param viewModuleBeans
 	 *        The list of viewModuleBeans to set
 	 */
 	public void setViewModuleBeans(List viewModuleBeans)
 	{
 		this.viewModuleBeans = viewModuleBeans;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void sortModule(ModuleObjService module, String course_id, String Direction) throws MeleteException
 	{
 		moduledb.sortModuleItem((Module) module, course_id, Direction);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void sortSectionItem(ModuleObjService module, String section_id, String Direction) throws MeleteException
 	{
 		moduledb.sortSectionItem((Module) module, section_id, Direction);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
	public void updateModuleDates(ModuleShdatesService modShdates, String courseId) throws Exception
 	{
 		moduledb.updateModuleShdates((ModuleShdates) modShdates);
		try
		{
			if (modShdates.getAddtoSchedule() != null)
			{
				moduledb.updateCalendar((Module) modShdates.getModule(), (ModuleShdates)modShdates, courseId);
			}
		}
		catch (Exception ex)
		{
			logger.debug("Exception thrown while updating calendar tool tables");
		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void updateProperties(List<? extends ModuleDateBeanService> moduleDateBeans, String courseId) throws MeleteException
 	{
 		try
 		{
 			moduledb.updateModuleDateBeans(moduleDateBeans);
 		}
 		catch (Exception ex)
 		{
 			logger.debug("multiple user exception in module business");
 			throw new MeleteException("edit_module_multiple_users");
 		}
 		for (ListIterator<?> i = moduleDateBeans.listIterator(); i.hasNext();)
 		{
 			ModuleDateBean mdbean = (ModuleDateBean) i.next();
 			try
 			{
 				if (((ModuleShdates) mdbean.getModuleShdate()).getAddtoSchedule() != null)
 				{
 					moduledb.updateCalendar((Module) mdbean.getModule(), (ModuleShdates) mdbean.getModuleShdate(), courseId);
 				}
 			}
 			catch (Exception ex)
 			{
 				logger.debug("Exception thrown while updating calendar tool tables");
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean updateSeqXml(String courseId) throws Exception
 	{
 		int modId;
 		List<ModuleObjService> modList = getModules(courseId);
 		// Iterate through each course, get all modules for the course
 		if (modList != null)
 		{
 			if (logger.isDebugEnabled()) logger.debug("Number of modules is " + modList.size());
 			List<Section> secList;
 			Module mod = null;
 			for (ListIterator<ModuleObjService> i = modList.listIterator(); i.hasNext();)
 			{
 				mod = (Module) i.next();
 				modId = mod.getModuleId().intValue();
 				secList = null;
 				try
 				{
 					// Get sections for each module;
 					secList = moduledb.getSections(modId);
 				}
 				catch (Exception ex)
 				{
 					logger.debug("ModuleServiceImpl updateSeqXml - get sections failed");
 					throw ex;
 				}
 
 				// Get the meleteDocs content info for each section
 				if (secList != null)
 				{
 					if (logger.isDebugEnabled()) logger.debug("Number of sections is " + secList.size());
 					SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 					for (ListIterator<Section> j = secList.listIterator(); j.hasNext();)
 					{
 						Section sec = (Section) j.next();
 						ssuImpl.addSection(sec.getSectionId().toString());
 					}// End for seclist loop
 					String seqXml = ssuImpl.storeSubSections();
 
 					mod.setSeqXml(seqXml);
 					moduledb.updateModule(mod);
 				}// End seclist != null
 			}// End modlist for loop
 		}// End modlist != null
 		return true;
 	}
 
 }
