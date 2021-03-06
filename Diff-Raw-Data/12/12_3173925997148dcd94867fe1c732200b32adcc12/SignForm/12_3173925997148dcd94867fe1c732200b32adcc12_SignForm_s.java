 /* 
  *  Process Aware Web Application Platform 
  * 
  *  Copyright (C) 2011-2013 Inherit S AB 
  * 
  *  This program is free software: you can redistribute it and/or modify 
  *  it under the terms of the GNU Affero General Public License as published by 
  *  the Free Software Foundation, either version 3 of the License, or 
  *  (at your option) any later version. 
  * 
  *  This program is distributed in the hope that it will be useful, 
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
  *  GNU Affero General Public License for more details. 
  * 
  *  You should have received a copy of the GNU Affero General Public License 
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  * 
  *  e-mail: info _at_ inherit.se 
  *  mail: Inherit S AB, Långsjövägen 8, SE-131 33 NACKA, SWEDEN 
  *  phone: +46 8 641 64 14 
  */ 
  
 package org.inheritsource.portal.components.mycases;
 
 
 import org.hippoecm.hst.core.component.HstComponentException;
 import org.hippoecm.hst.core.component.HstRequest;
 import org.hippoecm.hst.core.component.HstResponse;
 import org.inheritsource.service.common.domain.ActivityInstanceItem;
 import org.inheritsource.service.common.domain.ActivityInstanceLogItem;
 import org.inheritsource.service.common.domain.DocBoxFormData;
 import org.inheritsource.service.common.domain.ProcessInstanceDetails;
 import org.inheritsource.service.common.domain.StartLogItem;
 import org.inheritsource.service.common.domain.TimelineItem;
 import org.inheritsource.service.rest.client.InheritServiceClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SignForm extends Form  {
  
 	public static final Logger log = LoggerFactory.getLogger(SignForm.class);
 
 	public static final String START = "START";
 	@Override
     public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {
 		super.doBeforeRender(request, response);
 		
 		String formDocId = getPublicRequestParameter(request, "formDocId"); 
 		String signActivityName = getPublicRequestParameter(request, "signActivityName"); 
		 
 		// find data to sign
 		// Alt 1. There is a formDocId request parameteter => sign pdf generated by formDocId
         //        signActivityName is ignored.
 		// Alt 2. There is a processActivityFormInstanceId or taskUuid
 		//     2a. signActivityName, Sign data from signActivityName activity in the processInstance
 		//         identified by activity instance identified by processActivityFormInstanceId or taskUuid
 		//     2b. no signActivityName or signActivityName cannot match activity or signActivityName=START
 		//         => sign process instance start form 
 		//         
 		
 		String startFormDocId = null;
 		
 		InheritServiceClient isc = new InheritServiceClient();
 
 		if (formDocId == null || formDocId.trim().length() == 0 ) {
 			// alternative 2 (no formDocId)
 			ProcessInstanceDetails piDetails = null;
 			ActivityInstanceItem activity = (ActivityInstanceItem)request.getAttribute("activity");
 			if (activity != null && activity.getActivityInstanceUuid()!=null) {
 				piDetails = isc.getProcessInstanceDetailByActivityInstanceUuid(activity.getActivityInstanceUuid());
 				
 				// sign process instance start form is the default behaviour
 				
 				for (TimelineItem item : piDetails.getTimeline().getItems()) {
 					
 					if (item instanceof ActivityInstanceLogItem) {
 						ActivityInstanceLogItem logItem = (ActivityInstanceLogItem)item;
						if (logItem.getActivityName().equals(signActivityName)) {
 							// signActivityName match => Alternative 2a
 						}
 					}
 					
 					if (item instanceof StartLogItem) {
 						// keep startFormDocId in memory in case of Alternative 2b
 						StartLogItem startItem = (StartLogItem)item;
 						startFormDocId = startItem.getFormDocId();
 					}
 				}
 			}
 		}
 		
 		if (formDocId == null || formDocId.trim().length() == 0 ) {
 			// Alternative 2b
 			formDocId = startFormDocId;
 		}
 
 		
 		// TODO fråga DocBox efter pdf & checksum
 		// look up pdf/a from docbox and checksum TODO hårkodat nu...
 		DocBoxFormData docBoxFormData = isc.getDocBoxFormData(formDocId);
 		
		String pdfUrl = "http://" + request.getServerName() +  "/docbox/doc/ref/" + docBoxFormData.getDocboxRef();
 		String docNo = docBoxFormData.getDocNo();
 		String pdfChecksum = docBoxFormData.getCheckSum();
 		String docboxRef = docBoxFormData.getDocboxRef();
 		
 		StringBuffer responseUrl = request.getRequestURL();
 		responseUrl.append("/confirm?docboxRef=");
 		responseUrl.append(docboxRef);
 		responseUrl.append("&docNo=");
 		responseUrl.append(docNo);
 		
 		String signText = "Härmed undertecknar jag dokumentet " + pdfUrl + " med dokumentnummer " + docNo + " och kontrollsumman " + pdfChecksum;
 		
 		request.setAttribute("pdfUrl", pdfUrl);
 		request.setAttribute("docNo", docNo);
 		request.setAttribute("pdfChecksum", pdfChecksum);
 		request.setAttribute("signText", signText);
 		request.setAttribute("responseUrl", responseUrl.toString());
 
 	}
 	
 }
