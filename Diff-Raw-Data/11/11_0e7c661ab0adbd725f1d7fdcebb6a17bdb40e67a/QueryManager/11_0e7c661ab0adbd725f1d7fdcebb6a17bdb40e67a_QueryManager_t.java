 package org.huamuzhen.oa.biz;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.domain.dao.ReportFormDAO;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class QueryManager extends BaseManager<ReportForm, String> {
 	
 	@Resource
 	private ReportFormDAO reportFormDAO;
 
 	@Resource
 	public void setDao(ReportFormDAO dao) {
 		super.setDao(dao);
 	}
 	
 	@Transactional
 	public List<ReportForm> queryForm(String formId, String landUser, String landerLocation){
 		StringBuffer formIdBuffer = new StringBuffer();
 		formIdBuffer.append("%").append(formId).append("%");		
 		StringBuffer landUserBuffer = new StringBuffer();
 		landUserBuffer.append("%").append(landUser).append("%");		
 		StringBuffer landerLocationBuffer = new StringBuffer();
 		landerLocationBuffer.append("%").append(landerLocation).append("%");
 		
		List<ReportForm> queryResult = this.reportFormDAO.queryReportFromByKeyword(formIdBuffer.toString(), landUserBuffer.toString(), landerLocationBuffer.toString());
 		return  queryResult;		
 		
 	}
 	
 }
