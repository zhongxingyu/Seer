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
 import org.strasa.web.uploadstudy.view.model.AddProgram;
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
 import org.zkoss.zk.ui.UiException;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Tabpanel;
 import org.zkoss.zul.Window;
 
 
 public class EditProgram {
 	ProgramManagerImpl programMan;
 	StudyManagerImpl studyMan;
 
 	List<Program> programList;
 	
 	Integer userId=1;
 	public List<Program> getProgramList() {
 		return programList;
 	}
 	public void setProgramList(List<Program> programList) {
 		this.programList = programList;
 	}
 	@Init
 	public void init(@ContextParam(ContextType.VIEW) Component view){
 		programMan = new ProgramManagerImpl();
 		studyMan = new StudyManagerImpl();
 		//		programList = new ArrayList<program>();
 
 		programList = programMan.getProgramByUserId(userId);
 	}
 
 
 	@NotifyChange("programList")
 	@Command("deleteProgram")
	public void deleteStudy(@BindingParam("programId") Integer programId){
 		
 		if(studyMan.getStudyByProgramId(programId).isEmpty()){
 			programMan.deleteProgramById(programId);
 			Messagebox.show("Changes saved.");
 		}
 		else  Messagebox.show("Cannot delete a program with studies.", "Error", Messagebox.OK, Messagebox.ERROR); 
 //		populateEditStudyList();
 	}
 	
 	@NotifyChange("*")
 	@Command("addProgram")
 	public void addProgram(@ContextParam(ContextType.COMPONENT) Component component) {
 		Window win = (Window) component.getFellow("editProgramWindow");
 		Map<String, Object> params = new HashMap<String, Object>();
 
 		params.put("oldVar", null);
 
 		Window popup = (Window) Executions.createComponents(
 				 AddProgram.ZUL_PATH, win, params);
 
 		popup.doModal();
 		setProgramList(programMan.getProgramByUserId(userId));
 	}
 	
 	@NotifyChange("programList")
 	@Command("refreshProgramList")
 	public void refreshProgramList(@BindingParam("selected") Program selected) {
 		programList.clear();
 		programList.addAll(programMan.getProgramByUserId(userId));
 	}
 }
