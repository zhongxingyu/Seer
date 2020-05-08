 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2011 Etudes, Inc.
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
 package org.etudes.component.app.melete;
 
 import java.io.Serializable;
 import java.util.List;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.etudes.api.app.melete.*;
 
 public class ModuleDateBean implements Serializable, ModuleDateBeanService
 {
 
 	private List<SectionBeanService> sectionBeans;
 
 	protected CourseModule cmod;
 
 	protected boolean dateFlag;
 
 	/** nullable persistent field */
 	protected Module module;
 
 	/** identifier field */
 	protected int moduleId;
 
 	/** nullable persistent field */
 	protected ModuleShdates moduleShdate;
 
 	protected String rowClasses;
 
 	protected boolean saFlag;
 
 	protected boolean selected;
 
 	protected String truncTitle;
 
	protected boolean visibleFlag;

 	/** default constructor */
 	public ModuleDateBean()
 	{
 	}
 
 	/** full constructor */
 	public ModuleDateBean(int moduleId, Module module, ModuleShdates moduleShdate, CourseModule cmod, List<SectionBeanService> sectionBeans)
 	{
 		this.moduleId = moduleId;
 		this.module = module;
 		this.moduleShdate = moduleShdate;
 		this.cmod = cmod;
 		this.sectionBeans = sectionBeans;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public org.etudes.api.app.melete.CourseModuleService getCmod()
 	{
 		return this.cmod;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public org.etudes.api.app.melete.ModuleObjService getModule()
 	{
 		return this.module;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getModuleId()
 	{
 		return this.moduleId;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public org.etudes.api.app.melete.ModuleShdatesService getModuleShdate()
 	{
 		return this.moduleShdate;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getRowClasses()
 	{
 		return this.rowClasses;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<SectionBeanService> getSectionBeans()
 	{
 		return this.sectionBeans;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getTruncTitle()
 	{
 		return truncTitle;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isDateFlag()
 	{
 		return dateFlag;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isSaFlag()
 	{
 		return saFlag;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isSelected()
 	{
 		return selected;
 	}
 
	/**
	 * {@inheritDoc}
	 */
	public boolean isVisibleFlag()
	{
		return visibleFlag;
	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setCmod(org.etudes.api.app.melete.CourseModuleService cmod)
 	{
 		this.cmod = (CourseModule) cmod;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setDateFlag(boolean dateFlag)
 	{
 		this.dateFlag = dateFlag;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setModule(org.etudes.api.app.melete.ModuleObjService module)
 	{
 		this.module = (Module) module;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setModuleId(int moduleId)
 	{
 		this.moduleId = moduleId;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setModuleShdate(org.etudes.api.app.melete.ModuleShdatesService moduleShdate)
 	{
 		this.moduleShdate = (ModuleShdates) moduleShdate;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setRowClasses(String rowClasses)
 	{
 		this.rowClasses = rowClasses;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setSaFlag(boolean saFlag)
 	{
 		this.saFlag = saFlag;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setSectionBeans(List<SectionBeanService> sectionBeans)
 	{
 		this.sectionBeans = sectionBeans;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setSelected(boolean selected)
 	{
 		this.selected = selected;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setTruncTitle(String truncTitle)
 	{
 		this.truncTitle = truncTitle;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
	public void setVisibleFlag(boolean visibleFlag)
	{
		this.visibleFlag = visibleFlag;
	}

	/**
	 * {@inheritDoc}
	 */
 	public String toString()
 	{
 		return new ToStringBuilder(this).append("moduleId", getModuleId()).toString();
 	}
 
 }
