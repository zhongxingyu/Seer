 package com.nbcedu.function.schoolmaster2.action;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.xwork.StringUtils;
 
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
import com.nbcedu.function.schoolmaster2.data.model.TSm2Progress;
import com.nbcedu.function.schoolmaster2.data.model.TSm2Step;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.StepVo;
 
 /**
  * 校长
  * @author xuechong
  */
 @SuppressWarnings("serial")
 public class MasterSubjectAction extends BaseAction{
 	
 	private String moduleId;
 	private TSm2Subject subject;
	private TSm2Step step;
	private List<TSm2Progress> proList = new ArrayList<TSm2Progress>();
 	
 	private SM2MasterSubBiz subBiz;
 	
 	public String list(){
 		if(StringUtils.isNotBlank(moduleId)){
 			if(Utils.getDefaultMasterUids().contains(this.getUserId())){
 				this.pm = this.subBiz.findByModule(moduleId); 
 			}else{
 				this.pm = this.subBiz.findByMaster(moduleId, this.getUserId());
 			}
 			return moduleId + "List";
 		}else{
 			return "404";
 		}
 	}
 	
 	/**
 	 * 查看详细
 	 * @return
 	 * @author xuechong
 	 */
 	public String detail(){
 		this.subject = this.subBiz.findById(this.id);
 		List<StepVo> steps = this.subBiz.findAllSteps(this.id);
 		this.getRequestMap().put("steps", steps);
 		return "detail";
 	}
 	
	public String showStep(){
		return "stepDetail";
	}
 	
 	
 	///////////////////////
 	////getters&setters////
 	///////////////////////
 	public String getModuleId() {
 		return moduleId;
 	}
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 	public void setSubBiz(SM2MasterSubBiz subBiz) {
 		this.subBiz = subBiz;
 	}
 	public TSm2Subject getSubject() {
 		return subject;
 	}
 	public void setSubject(TSm2Subject subject) {
 		this.subject = subject;
 	}
 	
 }
