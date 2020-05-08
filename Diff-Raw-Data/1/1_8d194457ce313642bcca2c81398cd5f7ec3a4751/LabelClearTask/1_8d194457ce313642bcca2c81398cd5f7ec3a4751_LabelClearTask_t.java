 package com.ericsson.teamresource.tasks;
 
 import java.util.TimerTask;
 
 import com.ericsson.teamresource.services.ProjectTypeManagement;
 import com.ericsson.teamresource.services.RoleTypeManagement;
 import com.ericsson.teamresource.services.TeamManagement;
 import com.ericsson.teamresource.services.UnitManagement;
 import com.ericsson.teamresource.services.WorkTypeManagement;
 import com.ericsson.teamresource.util.MyApplicationContextUtil;
 
 public class LabelClearTask extends TimerTask {
     public LabelClearTask() {
         super();
     }
 
     @Override
     public void run() {
         ((ProjectTypeManagement) MyApplicationContextUtil.getContext().getBean("projectTypeService")).clearUnusedProjectType();
         ((RoleTypeManagement) MyApplicationContextUtil.getContext().getBean("roleTypeService")).clearUnusedRoleType();
         ((TeamManagement) MyApplicationContextUtil.getContext().getBean("teamService")).clearUnusedTeam();
         ((UnitManagement) MyApplicationContextUtil.getContext().getBean("unitService")).clearUnusedUnit();
         ((WorkTypeManagement) MyApplicationContextUtil.getContext().getBean("workTypeService")).clearUnusedWorkType();
     }
 }
