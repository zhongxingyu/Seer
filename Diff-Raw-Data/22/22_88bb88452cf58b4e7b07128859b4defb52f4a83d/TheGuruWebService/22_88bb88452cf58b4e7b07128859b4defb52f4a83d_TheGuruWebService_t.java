 package com.mpower.ws;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.ReportSegmentationResult;
 import com.mpower.domain.ReportSegmentationType;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.JasperServerService;
 import com.mpower.service.ReportSegmentationResultsService;
 import com.mpower.service.ReportSegmentationResultsServiceImpl;
 import com.mpower.service.ReportSegmentationTypeService;
 import com.mpower.service.ReportWizardService;
 
 
 import com.mpower.ws.axis.ExecuteSegmentationByIdRequest;
 import com.mpower.ws.axis.ExecuteSegmentationByIdResponse;
 import com.mpower.ws.axis.ExecuteSegmentationByNameRequest;
 import com.mpower.ws.axis.ExecuteSegmentationByNameResponse;
 import com.mpower.ws.axis.GetSegmentationByIdRequest;
 import com.mpower.ws.axis.GetSegmentationByIdResponse;
 import com.mpower.ws.axis.GetSegmentationByNameRequest;
 import com.mpower.ws.axis.GetSegmentationByNameResponse;
 import com.mpower.ws.axis.GetSegmentationCountByTypeRequest;
 import com.mpower.ws.axis.GetSegmentationCountByTypeResponse;
 import com.mpower.ws.axis.GetSegmentationListByTypeRequest;
 import com.mpower.ws.axis.GetSegmentationListByTypeResponse;
 import com.mpower.ws.axis.GetSegmentationListRequest;
 import com.mpower.ws.axis.GetSegmentationListResponse;
 import com.mpower.ws.axis.ObjectFactory;
 import com.mpower.ws.axis.Segmentation;
 import com.orangeleap.common.security.CasUtil;
 import com.orangeleap.common.security.OrangeLeapAuthentication;
 import com.orangeleap.common.security.OrangeLeapUsernamePasswordLocal;
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
 
 		int resultCount = 0;
 		int startIndex = 0;
 		String sortField = "";
 		String sortOrder = "";
 		if (request.getResultCount() != null && request.getResultCount() > 0)
 			resultCount = request.getResultCount();
 		if (request.getStartIndex() != null && request.getStartIndex() > 0)
 			startIndex = request.getStartIndex();
 		if (request.getSortField() != null && request.getSortField().length() > 0)
 			sortField = request.getSortField();
 		if (request.getSortDirection() != null && request.getSortDirection().length() > 0)
 			sortOrder = request.getSortDirection();
 
 		List<ReportWizard> segmentations = reportWizard.findSegmentationsBySegmentationTypeName(request.getType(),
 						startIndex, resultCount, sortField, sortOrder);
 
 		Iterator<ReportWizard> it = segmentations.iterator();
 
 		while (it.hasNext()) {
 			ReportWizard wiz = it.next();
 			Segmentation seg = createAndPopulateSegmentation(of, wiz);
 			response.getSegmentation().add(seg);
 		}
 
 		return response;
 	}
 
     @PayloadRoot(localPart = "GetSegmentationCountByTypeRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
     public GetSegmentationCountByTypeResponse getSegmentationCountByType(GetSegmentationCountByTypeRequest request) {
     	ObjectFactory of = new ObjectFactory();
     	GetSegmentationCountByTypeResponse response = of.createGetSegmentationCountByTypeResponse();
     	response.setCount(reportWizard.getSegmentationCountBySegmentationTypeName(request.getType()));
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
     		Segmentation seg = createAndPopulateSegmentation(of, wiz);
     		response.getSegmentation().add(seg);
     	}
 
 
         return response;
     }
 
 	private Segmentation createAndPopulateSegmentation(ObjectFactory of,
 			ReportWizard wiz) {
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
 
			XMLGregorianCalendar xmlDate;
			try {
				xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
				seg.setExecutionDate(xmlDate);
			} catch (DatatypeConfigurationException e) {
				logger.error(e.getMessage());
			}
 		}
 
 		ReportSegmentationType segType = reportSegmentationType.find(wiz.getId());
 		if (segType == null) seg.setType("Unknown");
 		else seg.setType(segType.getSegmentationType());
 		return seg;
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
         			Map<String, Object> info = (Map<String, Object>)SecurityContextHolder.getContext().getAuthentication().getDetails();
 
         			OrangeLeapAuthentication auth = new OrangeLeapAuthentication();
 
         			JasperServerService jss = reportSegmentationResults.getJasperServerService();
         			jss.setUserName(auth.getUserName());
         			jss.setPassword(auth.getPassword());
 
         			reportSegmentationResults.executeSegmentation(wiz.getId());
         			results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
 
         			Iterator<ReportSegmentationResult> it2 = results.iterator();
         			while (it2.hasNext()) {
         				ReportSegmentationResult result = it2.next();
         				response.getEntityid().add(result.getEntityId());
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
 
 
            			Map<String, Object> info = (Map<String, Object>)SecurityContextHolder.getContext().getAuthentication().getDetails();
 
         			OrangeLeapAuthentication auth = new OrangeLeapAuthentication();
 
         			JasperServerService jss = reportSegmentationResults.getJasperServerService();
         			jss.setUserName(auth.getUserName());
         			jss.setPassword(auth.getPassword());
 
         			results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
 
         			Iterator<ReportSegmentationResult> it2 = results.iterator();
         			while (it2.hasNext()) {
         				ReportSegmentationResult result = it2.next();
         				response.getEntityid().add(result.getEntityId());
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
 
         	Map<String, Object> info = (Map<String, Object>)SecurityContextHolder.getContext().getAuthentication().getDetails();
 
 			OrangeLeapAuthentication auth = new OrangeLeapAuthentication();
 
 			JasperServerService jss = reportSegmentationResults.getJasperServerService();
 			jss.setUserName(auth.getUserName());
 			jss.setPassword(auth.getPassword());
 
         	reportSegmentationResults.executeSegmentation(wiz.getId());
 
         	results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
 
         	Iterator<ReportSegmentationResult> it2 = results.iterator();
         	while (it2.hasNext()) {
         		ReportSegmentationResult result = it2.next();
         		response.getEntityid().add(result.getEntityId());
 
         	}
 
 
             return response;
         }
 
 
         @PayloadRoot(localPart = "GetSegmentationByIdRequest", namespace = "http://www.orangeleap.com/theguru/services/1.0")
         public GetSegmentationByIdResponse getSegmentationById(GetSegmentationByIdRequest request) throws Exception {
            	ObjectFactory of = new ObjectFactory();
 
 
 
         	GetSegmentationByIdResponse response = of.createGetSegmentationByIdResponse();
         	ReportWizard wiz = reportWizard.Find(request.getId());
 
         	if (wiz.getResultCount() == 0) {
         		//
         		// we should throw an error that the segmentation has not been executed...
         		return response;
         	}
 
 
 
 
         	List<ReportSegmentationResult> results;
 			Map<String, Object> info = (Map<String, Object>)SecurityContextHolder.getContext().getAuthentication().getDetails();
 
 			String site = (String) info.get(OrangeLeapUsernamePasswordLocal.SITE);
 
 			OrangeLeapAuthentication auth = new OrangeLeapAuthentication();
 
 			JasperServerService jss = reportSegmentationResults.getJasperServerService();
 			jss.setUserName(auth.getUserName());
 			jss.setPassword(auth.getPassword());
 
         	results = reportSegmentationResults.readReportSegmentationResultsByReportId(wiz.getId());
 
         	Iterator<ReportSegmentationResult> it2 = results.iterator();
         	while (it2.hasNext()) {
         		ReportSegmentationResult result = it2.next();
         		response.getEntityid().add(result.getEntityId());
 
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
