 package com.mpower.ws;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.ReportSegmentationResult;
 import com.mpower.domain.ReportSegmentationType;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.ReportSegmentationResultsService;
 import com.mpower.service.ReportSegmentationResultsServiceImpl;
 import com.mpower.service.ReportSegmentationTypeService;
 import com.mpower.service.ReportWizardService;
 import com.mpower.util.SessionHelper;
 
 import com.mpower.ws.axis.ExecuteSegmentationByIdRequest;
 import com.mpower.ws.axis.ExecuteSegmentationByIdResponse;
 import com.mpower.ws.axis.ExecuteSegmentationByNameRequest;
 import com.mpower.ws.axis.ExecuteSegmentationByNameResponse;
 import com.mpower.ws.axis.GetSegmentationByIdRequest;
 import com.mpower.ws.axis.GetSegmentationByIdResponse;
 import com.mpower.ws.axis.GetSegmentationByNameRequest;
 import com.mpower.ws.axis.GetSegmentationByNameResponse;
 import com.mpower.ws.axis.GetSegmentationListByTypeRequest;
 import com.mpower.ws.axis.GetSegmentationListByTypeResponse;
 import com.mpower.ws.axis.GetSegmentationListRequest;
 import com.mpower.ws.axis.GetSegmentationListResponse;
 import com.mpower.ws.axis.ObjectFactory;
 import com.mpower.ws.axis.Segmentation;
 import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
 
 @Endpoint
 public class TheGuruWebService {
 	
     private static final Log logger = LogFactory.getLog(TheGuruWebService.class);
 
     ReportWizardService reportWizard;
     ReportSegmentationResultsServiceImpl reportSegmentationResults;
     ReportSegmentationTypeService reportSegmentationType;
     
     @PayloadRoot(localPart = "GetSegmentationListByTypeRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
     public GetSegmentationListByTypeResponse getSegmentationListByType(GetSegmentationListByTypeRequest request) {
     	ObjectFactory of = new ObjectFactory();
     	GetSegmentationListByTypeResponse response = of.createGetSegmentationListByTypeResponse();
 
     	List<ReportWizard> segmentations = reportWizard.findAllSegmentations();
     	
     	Iterator<ReportWizard> it = segmentations.iterator();
     	
     	while (it.hasNext()) {
     		ReportWizard wiz = it.next();
 
     		ReportSegmentationType segType = reportSegmentationType.find(wiz.getId());
     		if (segType != null && segType.getSegmentationType().equals(request.getType())) {
     			Segmentation seg = of.createSegmentation();
     	
     			seg.setId(wiz.getId());
     			seg.setName(wiz.getReportName());
     			seg.setDescription(wiz.getReportComment());
     			seg.setType(segType.getSegmentationType());
     		
     			response.getSegmentation().add(seg);
     		}
     	}
     	
     	return response;
     }
     
     
     @PayloadRoot(localPart = "GetSegmentationListRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
     public GetSegmentationListResponse getSegmentationList(GetSegmentationListRequest request) {
     	ObjectFactory of = new ObjectFactory();
     	
 
     	List<ReportWizard> segmentations = reportWizard.findAllSegmentations();
     	
     	Iterator<ReportWizard> it = segmentations.iterator();
     	GetSegmentationListResponse response = of.createGetSegmentationListResponse();
     	
     	while (it.hasNext()) {
     		ReportWizard wiz = it.next();
 
     		Segmentation seg = of.createSegmentation();
     	
     		seg.setId(wiz.getId());
     		seg.setName(wiz.getReportName());
     		seg.setDescription(wiz.getReportComment());
     		seg.setExecutionCount(wiz.getResultCount());
     		seg.setExecutionUser(wiz.getLastRunByUserName());
             GregorianCalendar cal = new GregorianCalendar();
 
             Date lastRunDate = wiz.getLastRunDateTime(); 
             if (lastRunDate != null) {
             	cal.setTime(lastRunDate);
 
             	XMLGregorianCalendar xmlDate = new XMLGregorianCalendarImpl();
             	xmlDate.setSecond(cal.get(GregorianCalendar.SECOND));
             	xmlDate.setMinute(cal.get(GregorianCalendar.MINUTE));
             	xmlDate.setHour(cal.get(GregorianCalendar.HOUR_OF_DAY));
             	xmlDate.setDay(cal.get(GregorianCalendar.DAY_OF_MONTH));
             	xmlDate.setMonth(cal.get(GregorianCalendar.MONTH));
             	xmlDate.setYear(cal.get(GregorianCalendar.YEAR));
             	seg.setExecutionDate(xmlDate);
             }
     		
     		ReportSegmentationType segType = reportSegmentationType.find(wiz.getId());
     		if (segType == null) seg.setType("Unknown");
     		else seg.setType(segType.getSegmentationType());
     		
     		response.getSegmentation().add(seg);
     	}
 
     	
         return response;
     }
 
         @PayloadRoot(localPart = "ExecuteSegmentationByNameRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
         public ExecuteSegmentationByNameResponse executeSegmentationByName(ExecuteSegmentationByNameRequest request) throws Exception {
            	ObjectFactory of = new ObjectFactory();
         	
            	
         	List<ReportWizard> segmentations = reportWizard.findAllSegmentations();
         	
         	Iterator<ReportWizard> it = segmentations.iterator();
         	ExecuteSegmentationByNameResponse response = of.createExecuteSegmentationByNameResponse();
         	
         	while (it.hasNext()) {
         		ReportWizard wiz = it.next();
 
         		if (request.getName().equalsIgnoreCase(wiz.getReportName())) {
 
         			List<ReportSegmentationResult> results;
         			reportSegmentationResults.getJasperServerService().setUserName(SessionHelper.getGuruSessionData().getUsername());
         			reportSegmentationResults.getJasperServerService().setPassword(SessionHelper.getGuruSessionData().getPassword());
 
         			reportSegmentationResults.executeSegmentation(wiz.getId());
         			results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
         			
         			Iterator<ReportSegmentationResult> it2 = results.iterator();
         			while (it2.hasNext()) {
         				ReportSegmentationResult result = it2.next();
         				response.getConstituentid().add(result.getEntityId());
         			}
         			break;
         		}
         	}
         	
             return response;
         }
 
         @PayloadRoot(localPart = "GetSegmentationByNameRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
         public GetSegmentationByNameResponse getSegmentationByName(GetSegmentationByNameRequest request) throws Exception {
            	ObjectFactory of = new ObjectFactory();
         	
            	
         	List<ReportWizard> segmentations = reportWizard.findAllSegmentations();
         	
         	Iterator<ReportWizard> it = segmentations.iterator();
         	GetSegmentationByNameResponse response = of.createGetSegmentationByNameResponse();
         	
         	while (it.hasNext()) {
         		ReportWizard wiz = it.next();
 
         		if (request.getName().equalsIgnoreCase(wiz.getReportName())) {
 
         			List<ReportSegmentationResult> results;
         			reportSegmentationResults.getJasperServerService().setUserName(SessionHelper.getGuruSessionData().getUsername());
         			reportSegmentationResults.getJasperServerService().setPassword(SessionHelper.getGuruSessionData().getPassword());
 
         			results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
         			
         			Iterator<ReportSegmentationResult> it2 = results.iterator();
         			while (it2.hasNext()) {
         				ReportSegmentationResult result = it2.next();
         				response.getConstituentid().add(result.getEntityId());
         			}
         			break;
         		}
         	}
         	
             return response;
         }
         
         @PayloadRoot(localPart = "ExecuteSegmentationByIdRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
         public ExecuteSegmentationByIdResponse executeSegmentationById(ExecuteSegmentationByIdRequest request) throws Exception {
            	ObjectFactory of = new ObjectFactory();
         	
            	
            	
            	
         	ReportWizard wiz = reportWizard.Find(request.getId());
         	
         	ExecuteSegmentationByIdResponse response = of.createExecuteSegmentationByIdResponse();
         	
 
 
         	List<ReportSegmentationResult> results;
         	reportSegmentationResults.getJasperServerService().setUserName(SessionHelper.getGuruSessionData().getUsername());
         	reportSegmentationResults.getJasperServerService().setPassword(SessionHelper.getGuruSessionData().getPassword());
         	
         	reportSegmentationResults.executeSegmentation(wiz.getId());
         	
         	results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
         			
         	Iterator<ReportSegmentationResult> it2 = results.iterator();
         	while (it2.hasNext()) {
         		ReportSegmentationResult result = it2.next();
         		response.getConstituentid().add(result.getEntityId());
 
         	}
 
         	
             return response;
         }
 
         
         @PayloadRoot(localPart = "GetSegmentationByIdRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
         public GetSegmentationByIdResponse getSegmentationById(GetSegmentationByIdRequest request) throws Exception {
            	ObjectFactory of = new ObjectFactory();
         	
            	
            	
        	GetSegmentationByIdResponse response = of.createGetSegmentationByIdResponse();           	
         	ReportWizard wiz = reportWizard.Find(request.getId());
         	
        	if (wiz.getExecuteSegmentation() == false) {
        		//
        		// we should throw an error that the segmentation has not been executed...
        		return response;
        	}

         	
 
 
         	List<ReportSegmentationResult> results;
         	reportSegmentationResults.getJasperServerService().setUserName(SessionHelper.getGuruSessionData().getUsername());
         	reportSegmentationResults.getJasperServerService().setPassword(SessionHelper.getGuruSessionData().getPassword());
         	
         	results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
         			
         	Iterator<ReportSegmentationResult> it2 = results.iterator();
         	while (it2.hasNext()) {
         		ReportSegmentationResult result = it2.next();
         		response.getConstituentid().add(result.getEntityId());
 
         	}
 
         	
             return response;
         }
 
 		public ReportWizardService getReportWizard() {
 			return reportWizard;
 		}
 
 		public void setReportWizard(ReportWizardService reportWizard) {
 			this.reportWizard = reportWizard;
 		}
 
 		public ReportSegmentationResultsServiceImpl getReportSegmentationResults() {
 			return reportSegmentationResults;
 		}
 
 		public void setReportSegmentationResults(
 				ReportSegmentationResultsServiceImpl reportSegmentationResults) {
 			this.reportSegmentationResults = reportSegmentationResults;
 		}
 
 		public ReportSegmentationTypeService getReportSegmentationType() {
 			return reportSegmentationType;
 		}
 
 		public void setReportSegmentationType(
 				ReportSegmentationTypeService reportSegmentationType) {
 			this.reportSegmentationType = reportSegmentationType;
 		}
     
 }
