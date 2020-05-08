 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-10-7
  * Time: 21:18
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
 
 import com.luntsys.luntbuild.db.Project;
 import com.luntsys.luntbuild.db.Role;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.ValidationException;
 import com.luntsys.luntbuild.web.components.SecuritySupportComponent;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.engine.IPageLoader;
 import org.apache.tapestry.event.PageDetachListener;
 import org.apache.tapestry.event.PageEvent;
 import org.apache.tapestry.spec.IComponentSpecification;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * This component responsible for editing basic information about a user, such as
  * user name, etc..
  *
  * @author robin shine
  */
 public abstract class UserEditor extends SecuritySupportComponent implements PageDetachListener {
 	private User user;
     private boolean canViewProject;
     private boolean canBuildProject;
     private boolean canAdminProject;
 
 	/**
 	 * save the edit results
 	 * @param cycle
 	 */
 	public void save(IRequestCycle cycle){
 		getUsersTab().ensureCurrentTab();
 		try {
 			getUser().validate();
 			if (getUser().getId() == 0 && Luntbuild.getDao().isUserNameUsed(getUser())) {
 				setErrorMsg("User with this name already exists!");
 				return;
 			}
 			if (!isSiteAdmin()) {
 				getUser().setCanCreateProject(Luntbuild.getDao().loadUser(getUserId()).isCanCreateProject());
 			}
 			Luntbuild.getDao().saveUser(getUser());
 			setUserId(getUser().getId());
 			UsersTab usersTab = (UsersTab) getContainer();
 			UserViewer userViewer = (UserViewer) getContainer().getComponent("userViewerComponent");
 			userViewer.setUserId(getUser().getId());
 			usersTab.setAction("viewUser");
 			SecurityHelper.refreshUserCache();
             List projects = Luntbuild.getDao().loadProjects();
             for (Iterator iter = projects.iterator(); iter.hasNext();) {
                 Project project = (Project) iter.next();
                Luntbuild.getDao().loadProject(project.getId()); // Need to do that to get user role mapping
                 
                 if (this.canAdminProject) {
                     List adminUsers = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_ADMIN);
                     adminUsers.add(getUser());
                     project.putMappedRolesUserList(adminUsers, Role.LUNTBUILD_PRJ_ADMIN);
                 }
                 if (this.canBuildProject) {
                     List buildUsers = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_BUILDER);
                     buildUsers.add(getUser());
                     project.putMappedRolesUserList(buildUsers, Role.LUNTBUILD_PRJ_BUILDER);
                 }
                 if (this.canViewProject) {
                     List viewUsers = project.getMappedRolesUserList(Role.LUNTBUILD_PRJ_VIEWER);
                     viewUsers.add(getUser());
                     project.putMappedRolesUserList(viewUsers, Role.LUNTBUILD_PRJ_VIEWER);
                 }
                 if (this.canAdminProject || this.canBuildProject || this.canViewProject)
                     Luntbuild.getDao().saveProject(project);
             }
 		} catch (ValidationException e) {
 			setErrorMsg(e.getMessage());
 		}
 	}
 
 	/**
 	 * @return users tab
 	 */
 	public UsersTab getUsersTab() {
 		return (UsersTab) getContainer();
 	}
 
 	/**
 	 * @param cycle
 	 */
 	public void cancel(IRequestCycle cycle){
 		getUsersTab().ensureCurrentTab();
 		UsersTab usersTab = (UsersTab) getContainer();
 		if (getUserId() == 0)  // creating new user
 			usersTab.setAction(null);
 		else
 			usersTab.setAction("viewUser");
 	}
 
 	/**
 	 * @param cycle
 	 */
 	public void edit(IRequestCycle cycle) {
 		getUsersTab().ensureCurrentTab();
 		// does nothing
 	}
 
 	/**
 	 * @param errorMsg
 	 */
 	public abstract void setErrorMsg(String errorMsg);
 
 	/**
 	 * @return css index
 	 */
 	public abstract int getCssIndex();
 
 	/**
 	 * @return css class
 	 */
 	public String getPropertyNameCssClass() {
 		if (getCssIndex() % 2 == 0)
 			return "propertyEditorName propertyEditorName2";
 		else
 			return "propertyEditorName propertyEditorName1";
 	}
 
 	/**
 	 * @return css value
 	 */
 	public String getPropertyValueCssClass() {
 		if (getCssIndex() % 2 == 0)
 			return "propertyEditorValue propertyEditorValue2";
 		else
 			return "propertyEditorValue propertyEditorValue1";
 	}
 
 	/**
 	 * @return css tail
 	 */
 	public String getTailCssClass() {
 		if (getCssIndex() % 2 ==0)
 			return "propertyEditorTail propertyEditorTail2";
 		else
 			return "propertyEditorTail propertyEditorTail1";
 	}
 
 	/**
 	 * @return notifiers
 	 */
 	public List getNotifierInstances() {
 		return Luntbuild.getNotifierInstances(Luntbuild.notifiers);
 	}
 
 	/**
 	 * @return user id
 	 */
 	public abstract long getUserId();
 
 	/**
 	 * @param userId
 	 */
 	public abstract void setUserId(long userId);
 
 	public void pageDetached(PageEvent event) {
 		this.user = null;
 	}
 
 	public void finishLoad(IRequestCycle iRequestCycle, IPageLoader iPageLoader, IComponentSpecification iComponentSpecification) {
 		super.finishLoad(iRequestCycle, iPageLoader, iComponentSpecification);
 		this.user = null;
 	}
 
 	/**
 	 * @return user
 	 */
 	public User getUser() {
 		if (this.user == null) {
 			if (getUserId() == 0) // creating new user
 				this.user = new User();
 			else
 				this.user = Luntbuild.getDao().loadUser(getUserId());
 		}
 		return this.user;
 	}
 
     /**
      * @return can view project
      */
     public boolean isCanViewProject() {
         return this.canViewProject;
     }
 
     /**
      * @param canViewProject
      */
     public void setCanViewProject(boolean canViewProject) {
         this.canViewProject = canViewProject;
     }
 
     /**
      * @return can build project
      */
     public boolean isCanBuildProject() {
         return this.canBuildProject;
     }
 
     /**
      * @param canBuildProject
      */
     public void setCanBuildProject(boolean canBuildProject) {
         this.canBuildProject = canBuildProject;
     }
 
     /**
      * @return can admin project
      */
     public boolean isCanAdminProject() {
         return this.canAdminProject;
     }
 
     /**
      * @param canAdminProject
      */
     public void setCanAdminProject(boolean canAdminProject) {
         this.canAdminProject = canAdminProject;
     }
 
 }
