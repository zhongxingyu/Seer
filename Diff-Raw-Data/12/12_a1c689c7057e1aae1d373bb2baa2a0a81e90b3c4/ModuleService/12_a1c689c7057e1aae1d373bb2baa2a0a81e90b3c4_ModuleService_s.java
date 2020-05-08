 /**********************************************************************************
 *
 * $Header: /usr/src/sakai/melete-2.4/melete-api/src/java/org/sakaiproject/api/app/melete/ModuleService.java,v 1.20 2007/11/07 00:54:16 mallikat Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
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
 
 package org.sakaiproject.api.app.melete;
 
 import java.util.*;
 
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 
 /* Mallika - 3/22/05 - Added methods for moduledatebeanservice
  * Mallika - 3/28/05 - Catching exception in updateProperties
  * Mallika - 4/18/05 - Added method to delete module
  * Rashmi - 4/21-22 add methods for sort modules
  * Rashmi - 07/07/07 - removed season and yr from method signature of insert properties
  * Mallika - 8/1/06 - adding code to delete modules
  * Rashmi - 1/3/07 - remove license methods
  * Mallika - 11/6/07 - adding methods to get prev and next seq no
  */
 
 public interface ModuleService{
 
 	public static final int MIGRATE_IN_PROCESS=0;
 	public static final int MIGRATE_FAILED=1;
 	public static final int MIGRATE_COMPLETE=2;
 	public static final int MIGRATE_INCOMPLETE=3;
 
 
 	public void insertProperties(ModuleObjService module, ModuleShdatesService moduleshdates,String userId, String courseId) throws Exception;
 
 	public List getModuleDateBeans(String userId, String courseId);
 
 	public void setModuleDateBeans(List moduleDateBeansList);
 
 	public ModuleDateBeanService getModuleDateBean(String userId, String courseId,  int moduleId);
 
 	public ModuleDateBeanService getModuleDateBeanBySeq(String userId, String courseId,  int seqNo);
 
 	public void setModuleDateBean(ModuleDateBeanService mdBean);
 
 	public List getModules(String courseId);
 
 	public void setModules(List modules) ;
 
 	public void updateProperties(List moduleDateBeans)  throws Exception;
 
 	public void archiveModules(List moduleDateBeans, String courseId);
 
 	public ModuleObjService getModule(int moduleId);
 
 	public void setModule(ModuleObjService mod);
 
 	public List getArchiveModules(String course_id);
 
 	public void restoreModules(List modules) throws Exception;
 
 	public CourseModuleService getCourseModule(int moduleId,  String courseId) throws Exception;
 
	public void deleteModules(List moduleDateBeans, String courseId, String userId);
 
 	public int getMaxSeqNo(String courseId);
 	public int getNextSeqNo(String courseId, int currSeqNo);
 	public int getPrevSeqNo(String courseId, int currSeqNo);
 
 	public org.w3c.dom.Document getSubSectionW3CDOM(String sectionsSeqXML);
 
 	public int getMigrateStatus() throws Exception;
     public int migrateMeleteDocs(String meleteDocsDir) throws Exception;
 	public boolean updateSeqXml(String courseId) throws Exception;
 	public void checkInstallation() throws Exception;
 
 
 	public void createSubSection(ModuleObjService module, List secBeans) throws MeleteException;
 
 	public void bringOneLevelUp(ModuleObjService module, List secBeans) throws MeleteException;
 
 	public void sortModule(ModuleObjService module,String course_id,String Direction) throws MeleteException;
 
 	public void sortSectionItem(ModuleObjService module,String section_id,String Direction) throws MeleteException;
 
 	public void copyModule(ModuleObjService module,String courseId,String userId) throws MeleteException;
 
 	public void moveSections(List sectionBeans,ModuleObjService selectedModule) throws MeleteException;
 
 	public String printModule(ModuleObjService module) throws MeleteException;
 	
 	public int cleanUpDeletedModules() throws Exception;
 }
