 package com.nbcedu.function.schoolmaster2.action;
 
 
 import java.util.List;
 
 import com.nbcedu.function.schoolmaster2.biz.SM2SubjectBiz;
 import com.nbcedu.function.schoolmaster2.biz.Sm2TypeBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Type;
 
 @SuppressWarnings("serial")
 public class SubjectAction extends BaseAction{
 	
 	private String moduleId;
 	
 	private TSm2Subject subject = new TSm2Subject(); 
 	
 	private SM2SubjectBiz sm2SubjectBiz;
 	private Sm2TypeBiz sm2TypeBiz;
 	
 	public String toAdd(){
		List<TSm2Type> types = this.sm2TypeBiz.findByModUseId(moduleId, this.getUserId(),0);
 		this.getRequest().setAttribute("types", types);
 		types.isEmpty();
 		return "subjectAdd";
 	}
 	
 	public String find(){
 //		判断角色 如果是主管则查询所有自己的，否则只查看主管指定执行者可看
 		if(1==1){
 			pm = this.sm2SubjectBiz.findByCreaterId(this.getUserId(),moduleId);
 		}else{
 			pm = this.sm2SubjectBiz.findByExceuteUserId(this.getUserId(),moduleId);
 		}
 		
 		return "list";
 	}
 	
 	public String remove(){
 		return "refreshTeacherList";
 	}
 	
 	public String modify(){
 		return "refreshTeacherList";
 	}
 	
 	/////////////////////////
 	/////getters&setters/////
 	/////////////////////////
 
 	public TSm2Subject getSubject() {
 		return subject;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 
 	public void setSubject(TSm2Subject subject) {
 		this.subject = subject;
 	}
 
 	public void setSm2SubjectBiz(SM2SubjectBiz sm2SubjectBiz) {
 		this.sm2SubjectBiz = sm2SubjectBiz;
 	}
 
 	public void setSm2TypeBiz(Sm2TypeBiz sm2TypeBiz) {
 		this.sm2TypeBiz = sm2TypeBiz;
 	}
 	
 }
  
