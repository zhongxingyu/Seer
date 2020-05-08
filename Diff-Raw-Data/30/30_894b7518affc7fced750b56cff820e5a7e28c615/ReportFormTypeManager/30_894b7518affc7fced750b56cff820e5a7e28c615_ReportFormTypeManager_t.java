 package org.huamuzhen.oa.biz;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.domain.dao.ReportFormTypeDAO;
 import org.huamuzhen.oa.domain.entity.ReportFormType;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ReportFormTypeManager extends BaseManager<ReportFormType, String> {
 
 	@Resource
	private ReportFormTypeDAO reportFormTypeDAO;
	
	@Resource
 	public void setDao(ReportFormTypeDAO dao) {
 		super.setDao(dao);
 	}
 
	public void saveReportFormType(String id, String name, String[] requiredOrgUnitIds) {
		if(null == id){
			this.createNew(name,requiredOrgUnitIds);
		}else{
			this.updateExisting(id,name,requiredOrgUnitIds);
		}
		
	}
	
	private void createNew(String name, String[] requiredOrgUnitIds){
		
	}
	
	private void updateExisting(String id, String name, String[] requiredOrgUnitIds){
 		
 	}
 }
