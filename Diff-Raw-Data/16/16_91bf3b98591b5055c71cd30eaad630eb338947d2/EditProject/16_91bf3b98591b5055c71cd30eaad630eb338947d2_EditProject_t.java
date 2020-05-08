 package org.strasa.web.maintenance.view.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.strasa.middleware.manager.ProgramManagerImpl;
 import org.strasa.middleware.manager.ProjectManagerImpl;
 import org.strasa.middleware.manager.StudyManagerImpl;
 import org.strasa.middleware.manager.StudyTypeManagerImpl;
 import org.strasa.middleware.manager.UserManagerImpl;
 import org.strasa.middleware.model.Program;
 import org.strasa.middleware.model.Project;
 import org.strasa.middleware.model.Study;
 import org.strasa.middleware.model.StudyType;
 import org.strasa.middleware.model.User;
 import org.strasa.web.uploadstudy.view.model.AddProject;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.ContextParam;
 import org.zkoss.bind.annotation.ContextType;
 import org.zkoss.bind.annotation.ExecutionArgParam;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.Init;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Tabpanel;
 import org.zkoss.zul.Tabpanels;
 import org.zkoss.zul.Window;
 
 
 public class EditProject {
 	ProjectManagerImpl projectMan;
 	Integer userId=1;
 	List<Project> projectList;
 	private StudyManagerImpl studyMan;
 	public List<Project> getProjectList() {
 		return projectList;
 	}
 	public void setProjectList(List<Project> projectList) {
 		this.projectList = projectList;
 	}
 	@Init
 	public void init(){
 		
 		studyMan = new StudyManagerImpl();
 		projectMan = new ProjectManagerImpl();
 		//		projectList = new ArrayList<Project>();
 
 		projectList = projectMan.getProjectByUserId(userId);
 	}
 	
 	@NotifyChange("projectList")
 	@Command("deleteProject")
	public void deleteStudy(@BindingParam("projectId") Integer projectId){
 		
 		if(studyMan.getStudyByProgramId(projectId).isEmpty()){
 			projectMan.deleteProjectById(projectId);
 			Messagebox.show("Changes saved.");
 		}
 		else  Messagebox.show("Cannot delete a project with studies.", "Error", Messagebox.OK, Messagebox.ERROR); 
 //		populateEditStudyList();
 	}
 	
 	@NotifyChange("*")
 	@Command("addProject")
 	public void addProject(@ContextParam(ContextType.COMPONENT) Component component) {
 
 		Window win = (Window) component.getFellow("editProjectWindow");
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		
 		params.put("oldVar", null);
 		params.put("programID",1); //yah better change this! Make Dynamic
 		params.put("parent", win);
 
 		Window popup = (Window) Executions.getCurrent().createComponents(
 				 AddProject.ZUL_PATH, win, params);
 
 		popup.doModal();
 		
 	}
 	
 	@NotifyChange("projectList")
 	@Command("refreshProjectList")
 	public void refreshProjectList(@BindingParam("selected") Project selected) {
 		projectList.clear();
 		projectList.addAll(projectMan.getProjectByUserId(userId));
 	}
 }
