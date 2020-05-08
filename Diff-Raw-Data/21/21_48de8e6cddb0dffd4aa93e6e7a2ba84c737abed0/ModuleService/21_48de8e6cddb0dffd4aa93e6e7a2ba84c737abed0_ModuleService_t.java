 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
 
 package org.etudes.api.app.melete;
 
 import java.util.*;
 import org.etudes.api.app.melete.exception.MeleteException;
 
 public interface ModuleService
 {
 	/**
 	 * Apply base date to all module start and end dates
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @param days_diff
 	 *        Time difference in days
 	 */
 	public void applyBaseDateTx(String course_id, int days_diff);
 
 	/**
 	 * Archive the modules. Adjust the remaining module sequence accordingly.
 	 * 
 	 * @param selModBeans
 	 *        List of to be archived ModuleDateBean objects
 	 * @param moduleDateBeans
 	 *        List of at the time active ModuleDateBean objects
 	 * @param courseId
 	 *        The Course Id
 	 * @throws Exception
 	 *         "archive_fail" MeleteException
 	 */
 	public void archiveModules(List<ModuleDateBeanService> selModBeans, List<ModuleDateBeanService> moduleDateBeans, String courseId) throws Exception;
 
 	/**
 	 * Move subsections a level up.
 	 * 
 	 * @param module
 	 *        ModuleObjService Object
 	 * @param secBeans
 	 *        List of SectionBeans
 	 * @throws MeleteException
 	 *         "indent_left_fail" MeleteException
 	 */
 	public void bringOneLevelUp(ModuleObjService module, List<? extends SectionBeanService> secBeans) throws MeleteException;
 
 	/**
 	 * Checks if calendar tool is included in the site
 	 * 
 	 * @param courseId
 	 *        The Site Id
 	 * 
 	 * @return true if site has calendar tool
 	 */
 	public boolean checkCalendar(String courseId);
 
 	/**
 	 * Check if user has edit access.
 	 * 
 	 * @param user_id
 	 *        The user Id
 	 * @param course_id
 	 *        The course Id
 	 * @return
 	 */
 	public boolean checkEditAccess(String user_id, String course_id);
 
 	/**
 	 * Part of melete admin tool. Cleans database and removes the deleted modules.
 	 * 
 	 * @param userId
 	 *        The user Id
 	 * @return
 	 * @throws "cleanup_module_fail" MeleteException
 	 */
 	public int cleanUpDeletedModules(String userId) throws Exception;
 
 	/**
 	 * Creates a duplicate copy. The copied Module title and its sections title are appended with (Copied date)
 	 * 
 	 * @param module
 	 *        ModuleObjService object
 	 * @param courseId
 	 *        The course id
 	 * @param userId
 	 *        The user Id
 	 * @throws "copy_fail" MeleteException
 	 */
 	public void copyModule(ModuleObjService module, String courseId, String userId) throws MeleteException;
 
 	/**
 	 * Right indent sections to next level of subsections.
 	 * 
 	 * @param module
 	 *        ModuleObjService object
 	 * @param secBeans
 	 *        List of sectionBeans
 	 * @throws MeleteException
 	 *         "indent_right_fail" MeleteException
 	 */
 	public void createSubSection(ModuleObjService module, List<? extends SectionBeanService> secBeans) throws MeleteException;
 
 	/**
 	 * 
 	 * @param delModules
 	 *        List of ModuleObjService objects
 	 * @param courseId
 	 *        The course Id
 	 * @param userId
 	 *        The user Id
 	 * @throws Exception
 	 *         "delete_module_fail" MeleteException
 	 */
 	public void deleteModules(List<? extends ModuleObjService> delModules, String courseId, String userId) throws Exception;
 
 	/**
 	 * Get archived modules of the course.
 	 * 
 	 * @param course_id
 	 *        The Site Id
 	 * @return List of archived CourseModule Objects
 	 */
 	public List<CourseModuleService> getArchiveModules(String course_id);
 
 	/**
 	 * Gets the corresponding courseModule object
 	 * 
 	 * @param moduleId
 	 *        The Module Id
 	 * @param courseId
 	 *        The Course Id
 	 * @return CourseModuleService object
 	 * @throws Exception
 	 */
 	public CourseModuleService getCourseModule(int moduleId, String courseId) throws Exception;
 
 	/**
 	 * Get the number of active modules in a site.
 	 * 
 	 * @param courseId
 	 *        The course Id
 	 * @return
 	 */
 	public int getCourseModuleSize(String courseId);
 
 	/**
 	 * Gets the earliest start date(if defined) of all the modules. If start date exists for the modules gets the earliest start 
 	 * date of all modules else returns null
 	 * 
 	 * @param course_id
 	 *        Course id
 	 * @return 
 	 */
 	public Date getMinStartDate(String course_id);
 
 	/**
 	 * Gets the Module.
 	 * 
 	 * @param moduleId
 	 *        The Module Id
 	 * @return
 	 */
 	public ModuleObjService getModule(int moduleId);
 
 	/**
 	 * Creates the ModuleDateBean wrapper class and sets the section display level and row classes etc
 	 * 
 	 * @param userId
 	 *        The user Id
 	 * @param courseId
 	 *        The course Id
 	 * @param moduleId
 	 *        The module Id
 	 * @return
 	 */
 	public ModuleDateBeanService getModuleDateBean(String userId, String courseId, int moduleId);
 
 	/**
 	 * Creates the ModuleDateBean wrapper class and sets the section display level and row classes etc, fetches by seq number
 	 * 
 	 * @param userId
 	 *        The user id
 	 * @param courseId
 	 *        The course id
 	 * @param seqNo
 	 *        Sequence number
 	 * @return
 	 */
 	public ModuleDateBeanService getModuleDateBeanBySeq(String userId, String courseId, int seqNo);
 
 	/**
 	 * Gets list of modules
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @return List of ModuleObjService objects
 	 */
 	public List<ModuleDateBeanService> getModuleDateBeans(String userId, String courseId);
 
 	/**
 	 * Gets the list of active Modules of a site.
 	 * 
 	 * @param courseId
 	 *        The course Id
 	 * @return List of ModuleObjService Objects
 	 */
 	public List<ModuleObjService> getModules(String courseId);
 
 	/**
 	 * Get next sequence number. Method invoked for authors and students Checks against special access and denied modules before returning sequence number
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param currSeqNo
 	 *        Current sequence number
 	 * @return Next sequence number
 	 */
 	public int getNextSeqNo(String userId, String courseId, int currSeqNo);
 
 	/**
 	 * Get the number of modules completely read by the users of the site
 	 * 
 	 * @param course_id
 	 *        The Course Id
 	 * @return Map<user Id, count of completely read modules>
 	 */
 	public Map<String, Integer> getNumberOfModulesCompletedByUserId(String course_id);
 
 	/**
 	 * Get the number of sections read by a user from a module
 	 * 
 	 * @param user_id
 	 *        The user Id
 	 * @param module_id
 	 *        The module Id
 	 * @return
 	 */
 	public int getNumberOfSectionsReadFromModule(String user_id, int module_id);
 
 	/**
 	 * Get previous sequence number. Method invoked for authors and students Checks against special access and denied modules before returning sequence number
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param currSeqNo
 	 *        Current sequence number
 	 * @return Previous sequence number
 	 */
 	public int getPrevSeqNo(String userId, String courseId, int currSeqNo);
 
 	/**
 	 * Generates the DOM structure for the sequence XML
 	 * 
 	 * @param sectionsSeqXML
 	 *        Sequence XML
 	 * @return
 	 */
 	public org.w3c.dom.Document getSubSectionW3CDOM(String sectionsSeqXML);
 
 	/**
 	 * Get list of modules with view status set
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param courseId
 	 *        Course id
 	 * @param fromCourseMap
 	 *        true if method is invoked from coursemap, false otherwise.If not invoked from course map, some queries may be skipped
 	 * @param filtered
 	 *        If false, return all modules including invalid ones. If true, do not return invalid modules.
 	 * @return List of modules
 	 */
 	public List<ViewModBeanService> getViewModules(String userId, String courseId, boolean fromCourseMap, boolean filtered);
 
 	/**
 	 * Adds Module to the Database.
 	 * 
 	 * @param module
 	 *        ModuleObjService object
 	 * @param moduleshdates
 	 *        ModuleShdatesService Object
 	 * @param userId
 	 *        The user Id
 	 * @param courseId
 	 *        The Site Id
 	 * @throws Exception
 	 *         "add_module_fail" MeleteException
 	 */
 	public void insertProperties(ModuleObjService module, ModuleShdatesService moduleshdates, String userId, String courseId) throws Exception;
 
 	/**
 	 * Checks if the module is completely read by the user
 	 * 
 	 * @param user_id
 	 *        The user Id
 	 * @param module_id
 	 *        The module Id
 	 * @return true if read completely
 	 */
 	public boolean isModuleCompleted(String user_id, int module_id);
 
 	/**
 	 * Checks if a module is open or closed at a given time
 	 * 
 	 * @param startDate
 	 * @param endDate
 	 * @return invalid if not able to determine the status. open if module is open and active. closed if module is closed. later if module is not opened yet.
 	 */
 	public String isSectionModuleOpen(Date startDate, Date endDate);
 
 	/**
 	 * Moves the list of sections to the module. Updates the old and selected module sequence XML. For composed sections, moves the file to selected module's content collection.
 	 * 
 	 * @param courseId
 	 *        The Site Id
 	 * @param sectionBeans
 	 *        List of Sections to be moved
 	 * @param selectedModule
 	 *        The selected ModuleObjService Object
 	 * @throws MeleteException
 	 *         "move_section_fail" MeleteException
 	 */
 	public void moveSections(String courseId, List<? extends SectionBeanService> sectionBeans, ModuleObjService selectedModule)
 			throws MeleteException;
 
 	/**
 	 * Gets the module and its sections contents in a printable fashion.
 	 * 
 	 * @param module
 	 *        ModuleObjService Object
 	 * @return
 	 * @throws "print_module_fail" MeleteException
 	 */
 	public String printModule(ModuleObjService module) throws MeleteException;
 
 	/**
 	 * Restores a list of archived modules. Restored modules have open dates and are sequenced at the bottom of the list.
 	 * 
 	 * @param modules
 	 *        List of CourseModule objects
 	 * @param courseId
 	 *        The courseId
 	 * @throws Exception
 	 *         MeleteException
 	 */
 	public void restoreModules(List<? extends CourseModuleService> modules, String courseId) throws Exception;
 
 	/**
 	 * Note: No longer in use
 	 * 
 	 * @param mod
 	 *        ModuleObjService object
 	 */
 	public void setModule(ModuleObjService mod);
 
 	/**
 	 * Note: no longer in Use
 	 * 
 	 * @param mdBean
 	 */
 	public void setModuleDateBean(ModuleDateBeanService mdBean);
 
 	/**
 	 * Sorts the module in the specified direction (One up/down, all up/all down).
 	 * 
 	 * @param module
 	 *        ModuleObjService object
 	 * @param course_id
 	 *        The course id
 	 * @param Direction
 	 *        direction to move(allUp, up, down, allDown)
 	 * @throws "sort_fail" MeleteException
 	 */
 	public void sortModule(ModuleObjService module, String course_id, String Direction) throws MeleteException;
 
 	/**
 	 * Sorts the section in the specified direction (One up/down, all up/all down). Updates the module sequence XMl accordingly.
 	 * 
 	 * @param module
 	 *        ModuleObjService object
 	 * @param section_id
 	 *        The section id
 	 * @param Direction
 	 *        direction to move the section (allUp, up, down, allDown)
 	 * @throws "sort_fail" MeleteException
 	 */
 	public void sortSectionItem(ModuleObjService module, String section_id, String Direction) throws MeleteException;
 
 	/**
 	 * Update module's dates from Coursemap
 	 * 
 	 * @param modShdates
 	 *        ModuleShdatesService Object
 	 * @throws Exception
 	 *         "edit_module_multiple_users" MeleteException and Hibernate Exception
 	 */
	public void updateModuleDates(ModuleShdatesService modShdates, String courseId) throws Exception;
 
 	/**
 	 * Update Module and ModuleShdates. Updates calendar tool.
 	 * 
 	 * @param moduleDateBeans
 	 *        List of moduleDateBeans objects
 	 * @param courseId
 	 *        The course Id
 	 * @throws Exception
 	 *         edit_module_multiple_users MeleteException
 	 */
 	public void updateProperties(List<? extends ModuleDateBeanService>  moduleDateBeans, String courseId) throws Exception;
 
 	/**
 	 * Creates sequence XML for all modules of the site.
 	 * 
 	 * @param courseId
 	 *        Course Id
 	 * @return
 	 * @throws Exception
 	 *         Hibernate Exception, MeleteException
 	 */
 	public boolean updateSeqXml(String courseId) throws Exception;
 }
