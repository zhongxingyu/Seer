 package org.huamuzhen.oa.biz;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.domain.dao.ReportFormDAO;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class ReportFormManager extends BaseManager<ReportForm, String> {
 	
 	@Resource
 	private ReportFormDAO reportFormDAO;
 
 	@Resource
 	public void setDao(ReportFormDAO dao) {
 		super.setDao(dao);
 	}
 	
 	@Transactional
 	public ReportForm saveReportForm(String id, String title,
 			String formId, String landUser, String originalLandUser,
 			String landLocation, String landArea, String landUse,
 			String originalLandUse, String matter, String matterDetail,
 			String policyBasis, String comment) {
 		if (null == id) {
 			return createNew(title, formId, landUser, originalLandUser,landLocation, landArea, landUse, originalLandUse, matter, matterDetail, policyBasis, comment);
 		} else {
 			return updateExisting(id, title, formId, landUser, originalLandUser,landLocation, landArea, landUse, originalLandUse, matter, matterDetail, policyBasis, comment);
 		}
 	}
 
 	private ReportForm updateExisting(String id, String title,
 			String formId, String landUser, String originalLandUser,
 			String landLocation, String landArea, String landUse,
 			String originalLandUse, String matter, String matterDetail,
 			String policyBasis, String comment) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private ReportForm createNew(String title, String formId,
 			String landUser, String originalLandUser, String landLocation,
 			String landArea, String landUse, String originalLandUse,
 			String matter, String matterDetail, String policyBasis,
 			String comment) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
     public String generateFormId(String previousFormId){
 		
 		String currentSuffix= previousFormId.substring(13);
 		Integer nextSuffix = Integer.parseInt(currentSuffix) + 1;
 		if(nextSuffix < 10){
 			return "0" + nextSuffix;
 		}else{
 			return nextSuffix.toString();
 		}
 	}
 	
 	public String generateFormId(){
 		
 		String dateOfToday = new SimpleDateFormat("yyyyMMdd")
 				.format(new Date());
		String fuzzyQueryCondition = dateOfToday + "%-00";
System.out.println("fuzzyQueryCondition: " + fuzzyQueryCondition);
 		List<String> newCreatedFormIdListOfToday = this.reportFormDAO
				.findNewCreatedFormIdListOfToday(fuzzyQueryCondition);
 		if (newCreatedFormIdListOfToday.size() == 0) {
 			return dateOfToday + "0000-00";
 		}
 		String latestFormIdOfToday = newCreatedFormIdListOfToday.get(0);
 
 		Integer count = Integer.parseInt(latestFormIdOfToday.substring(8, 12)) + 1;
 		if (count < 10) {
 			return dateOfToday + "000" + count + "-00";
 		} else if (count < 100) {
 			return dateOfToday + "00" + count + "-00";
 		} else if (count < 1000) {
 			return dateOfToday + "0" + count + "-00";
 		} else {
 			return dateOfToday + count.toString() + "-00";
 		}
 
 	}
 	
 }
