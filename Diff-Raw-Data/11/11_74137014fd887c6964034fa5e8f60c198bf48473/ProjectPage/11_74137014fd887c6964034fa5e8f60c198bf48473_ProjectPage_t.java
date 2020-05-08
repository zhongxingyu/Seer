 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-4-30
  * Time: 8:36:29
  *
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  
  */
 package com.luntsys.luntbuild.web;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.engine.IPageLoader;
 import org.apache.tapestry.event.PageDetachListener;
 import org.apache.tapestry.event.PageEvent;
 import org.apache.tapestry.spec.IComponentSpecification;
 
 import com.luntsys.luntbuild.db.Project;
 import com.luntsys.luntbuild.db.Role;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.db.VcsLogin;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.web.components.tabcontrol.TabControl;
 
 /**
  * this page is used to show aspects of a specific project.
  *
  * @author robin shine
  */
 public abstract class ProjectPage extends HierarchyPage implements PageDetachListener {
 	public static final long SERVICE_PARAMETER_SCHEDULES = 1;
 	private Project project;
 
 	public void pageDetached(PageEvent event) {
 		project = null;
 	}
 
 	public void finishLoad(IRequestCycle iRequestCycle, IPageLoader iPageLoader, IComponentSpecification iComponentSpecification) {
 		super.finishLoad(iRequestCycle, iPageLoader, iComponentSpecification);
 		project = null;
 	}
 
 	public String getPageDataDescription() {
 		if (project == null || project.getId() == 0)
 			return "project - creating...";
 		else {
 			return "project - " + getProject().getName();
 		}
 	}
 
 	public PageInfo[] getHierarchyPageInfos() {
 		PageInfo[] pageInfos = new PageInfo[1];
 		pageInfos[0] = new PageInfo();
 		pageInfos[0].setPageName("Home");
 
 		// data description and id are non-sense for home page, we are just make some
 		// initialization of them
 		pageInfos[0].setPageDataDesciption("Home");
 		pageInfos[0].setPageDataId(Home.SERVICE_PARAMETER_HOME);
 
 		return pageInfos;
 	}
 
 	public void activateExternalPage(Object[] parameters, IRequestCycle cycle) {
 		setProjectId(((Long) parameters[0]).longValue());
 		gotoBasicTab(cycle);
 		if (parameters.length >= 2) {
 			long actionId = ((Long) parameters[1]).longValue();
 			if (actionId == SERVICE_PARAMETER_SCHEDULES) {
 				TabControl tabs = (TabControl) getComponent("tabs");
 				tabs.setSelectedTabId("schedules");
 			}
 		}
 	}
 
 	public void setProject(Project project) {
 		this.project = project;
 	}
 
 	public abstract void setProjectIdToCopy(long projectIdToCopy);
 
 	public abstract long getProjectIdToCopy();
 
 	public abstract void setProjectId(long projectId);
 
 	public abstract long getProjectId();
 
 	public Project getProject() {
 		if (project == null) {
 			if (getProjectId() == 0) {
 				if (getProjectIdToCopy() == 0)
 					project = new Project();
 				else {
 					project = Luntbuild.getDao().loadProject(getProjectIdToCopy()).createNewByCopy();
 				}
 				if (!SecurityHelper.isSiteAdmin()) {
                     User loginUser = Luntbuild.getDao().loadUser(SecurityHelper.getPrincipalAsString());
 					List adminUsers = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_ADMIN);
 					if (!adminUsers.contains(loginUser))
 						adminUsers.add(loginUser);
 					project.putMappedRolesUserList(adminUsers, Role.LUNTBUILD_PRJ_ADMIN);
 				}
 			} else {
 				project = Luntbuild.getDao().loadProjectInternal(getProjectId());
 			}
 		}
 		return project;
 	}
 
 	public void saveProject() {
 		Luntbuild.getDao().saveProject(getProject());
 		if (getProjectId() == 0) {
 			Iterator it = getProject().getVcsLogins().iterator();
 			if (getProjectIdToCopy() == 0) {
				while (it.hasNext()) {
					VcsLogin vcsLogin = (VcsLogin) it.next();
					vcsLogin.setId(0);
					Luntbuild.getDao().saveVcsLogin(vcsLogin);
				}
 				it = getProject().getSchedules().iterator();
 				while (it.hasNext()) {
 					Schedule schedule = (Schedule) it.next();
 					schedule.setId(0);
 					Luntbuild.getDao().saveSchedule(schedule);
 				}
 			}
 		}
 		setProjectId(getProject().getId());
         Luntbuild.setProjectCreator(getProjectId(), SecurityHelper.getPrincipalAsString());
         
 		SecurityHelper.refreshUserCache();
 	}
 
 	public void gotoBasicTab(IRequestCycle cycle) {
 		TabControl tabs = (TabControl) getComponent("tabs");
 		tabs.setSelectedTabId("basic");
 		ProjectBasicTab basicTab = (ProjectBasicTab) getComponent("basic");
 		basicTab.tabSelected();
 	}
 
 	public int getRefreshInterval(){
 		TabControl tabs = (TabControl) getComponent("tabs");
 		if (tabs.getSelectedTabId().equals("schedules")) {
 			return ((SchedulesTab) tabs.getSelectedTab()).getRefreshInterval();
 		} else
 			return 0;
 	}
 }
