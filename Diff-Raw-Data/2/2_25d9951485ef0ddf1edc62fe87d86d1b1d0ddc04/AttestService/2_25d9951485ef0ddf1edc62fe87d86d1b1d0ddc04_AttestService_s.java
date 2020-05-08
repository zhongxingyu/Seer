 /*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package com.intel.openAttestation.AttestationService.resource;
 
 import gov.niarl.hisAppraiser.hibernate.domain.AuditLog;
 
 import org.apache.log4j.Logger;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.intel.openAttestation.AttestationService.util.AttestUtil;
 import com.intel.openAttestation.AttestationService.bean.Host;
 import com.intel.openAttestation.AttestationService.bean.PCRValue;
 import com.intel.openAttestation.AttestationService.bean.RespSyncBean;
 import com.intel.openAttestation.AttestationService.bean.AnalysisDetails;
 import com.intel.openAttestation.AttestationService.util.ResultConverter;
 import com.intel.openAttestation.AttestationService.util.ResultConverter.AttestResult;
 import com.intel.openAttestation.AttestationService.hibernate.dao.AttestDao;
 
 import gov.niarl.hisAppraiser.hibernate.domain.AttestRequest;
 
 public class AttestService {
 	
 	public static Logger logger = Logger.getLogger("AttestService");
 	/**
 	 * generate a hashMap of pcrs for a given auditlog. The hashMap key is pcr's number and value is pcr's value.  
 	 * @param auditlog of interest
 	 * @return contain key-values of pcrs like {<'1','11111111111'>,<'2','111111111111111111111'>,...}   
 	 */
 	public static HashMap<Integer,String> generatePcrsByAuditId(AuditLog auditlog){
 		HashMap<Integer,String> pcrs = new HashMap<Integer, String>();
 		pcrs.put(0, auditlog.getPcr0());
 		pcrs.put(1, auditlog.getPcr1());
 		pcrs.put(2, auditlog.getPcr2());
 		pcrs.put(3, auditlog.getPcr3());
 		pcrs.put(4, auditlog.getPcr4());
 		pcrs.put(5, auditlog.getPcr5());
 		pcrs.put(6, auditlog.getPcr6());
 		pcrs.put(7, auditlog.getPcr7());
 		pcrs.put(8, auditlog.getPcr8());
 		pcrs.put(9, auditlog.getPcr9());
 		pcrs.put(10, auditlog.getPcr10());
 		pcrs.put(11, auditlog.getPcr11());
 		pcrs.put(12, auditlog.getPcr12());
 		pcrs.put(13, auditlog.getPcr13());
 		pcrs.put(14, auditlog.getPcr14());
 		pcrs.put(15, auditlog.getPcr15());
 		pcrs.put(16, auditlog.getPcr16());
 		pcrs.put(17, auditlog.getPcr17());
 		pcrs.put(18, auditlog.getPcr18());
 		pcrs.put(19, auditlog.getPcr19());
 		pcrs.put(20, auditlog.getPcr20());
 		pcrs.put(21, auditlog.getPcr21());
 		pcrs.put(22, auditlog.getPcr22());
 		pcrs.put(23, auditlog.getPcr23());
 		return pcrs;
 	}
 	
 	/**
 	 * decide whether all hosts has attested for a given requestId. 
 	 * @param requestId of interest
 	 * @return true if all has attested, else false
 	 */
 	public static boolean isAllAttested(String requestId){
 		AttestDao attestationDao = new AttestDao();
 		List<AttestRequest> attestRequests = attestationDao.getRequestsByRequestId(requestId);
 		for (AttestRequest attestRequest : attestRequests){
 			if (attestRequest.getResult() == null)
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * get synchronous result for a given requestId. Just get value from DB.
 	 * @param requestId
 	 * @return
 	 */
 	public static RespSyncBean getRespSyncResult(String requestId) {
 		AttestDao dao = new AttestDao();
 		RespSyncBean resp = new RespSyncBean();
 		List<Host> hosts = new ArrayList<Host> ();
 		
 		for (AttestRequest attest: dao.getRequestsByRequestId(requestId)){
 			Integer result = attest.getResult()==null ? ResultConverter.getIntFromResult(AttestResult.PENDING) : attest.getResult();
 			Host host = new Host();
 			List<PCRValue> pcr_values = new ArrayList<PCRValue>();
 			HashMap<Integer,String> pcrs = new HashMap<Integer, String>();
 			if (ResultConverter.getResultFromInt(result) == AttestResult.TRUSTED || ResultConverter.getResultFromInt(result) == AttestResult.UN_TRUSTED){
 				if (attest.getPCRMask()!=null){
 					AuditLog auditlog = dao.getAuditLogById(attest.getAuditLog().getId());
 					if (auditlog != null){
 						pcrs = generatePcrsByAuditId(auditlog);
 						for (Integer i : AttestUtil.generatePcrSelectedPositions(attest.getPCRMask())){
 							pcr_values.add(new PCRValue(i,pcrs.get(i)));
 						}
 						host.setPcr_values(pcr_values);
 					}
 				}
 			}
 			host.setHost_name(attest.getHostName());
 			host.setTrust_lvl(ResultConverter.getStringFromInt(result));
			host.setVtime(new Date());
 
 			if (attest.getAuditLog() != null) {
 				AttestUtil.loadProp();
 				host.setUrl("http://" + AttestUtil.getPortalAddress() + "/OAT/report.php?id=" + attest.getAuditLog().getId());
 			}
 			host.setReport_is_valid(false);
 
 			if (attest.getAuditLog() != null) {
 				AuditLog auditlog = dao.getAuditLogById(attest.getAuditLog().getId());
 				if (auditlog.getValidationErrors() == null) {
 					host.setReport_is_valid(true);
 				}
 			}
 
 			String[] analysisList = {"VALIDATE_PCR", "COMPARE_REPORT"};
 			if (attest.getAnalysisRequest() != null)
 				analysisList = attest.getAnalysisRequest().split(";");
 
 			String analysisResults = attest.getAnalysisResults();
 			List<AnalysisDetails> detailsList = null;
 
 			if (analysisResults != null && !analysisResults.equals("")) {
 				detailsList = new ArrayList<AnalysisDetails>();
 				int analysisCounter = 0;
 				while (analysisResults.length() > 0) {
 					String[] analysisElements = analysisResults.split("\\|", 5);
 					AnalysisDetails detail = new AnalysisDetails();
 
 					int outputLength = Integer.parseInt(analysisElements[3]);
 					int tmpLength = 0;
 					for (int i = 0; i < 4; i++)
 						tmpLength += analysisElements[i].length() + 1;
 
 					detail.setName(analysisList[analysisCounter].split(",")[0]);
 					detail.setResult(analysisElements[1]);
 					detail.setStatus(analysisElements[2]);
 					detail.setOutput(analysisResults.substring(tmpLength, tmpLength + outputLength));
 					detailsList.add(detail);
 
 					analysisResults = analysisResults.substring(tmpLength + outputLength + 1);
 					analysisCounter++;
 				}
 			}
 
 			host.setAnalysis_details(detailsList);
 			hosts.add(host);
 		}
 		resp.setHosts(hosts);
 		return resp;
 	}
 	
 	/**
 	 * add requests to DB and return requestId.
 	 * @param reqAttestation
 	 * @param Xauthuser
 	 * @param isSync
 	 * @return requestId 
 	 */
 
 
 //	public static String addRequests(ReqAttestationBean reqAttestation,
 //			String requestHost, boolean isSync) {
 //		AttestDao dao = new AttestDao();
 //		int hostNum = Integer.parseInt(String.valueOf(reqAttestation.getCount()));
 //		AttestRequest[] attestRequests = new AttestRequest[hostNum];
 //		String requestId;
 //		if (isSync)
 //			requestId = AttestUtil.generateRequestId("PollHostsRequestId");
 //		else
 //			requestId = AttestUtil.generateRequestId("PostHostsRequestId");
 //		Date requestTime = new Date();
 //		for(int i=0; i<hostNum; i++){
 //			attestRequests[i] = new AttestRequest();
 //			attestRequests[i].setRequestId(requestId);
 //			attestRequests[i].setHostName(reqAttestation.getHosts().get(i));
 //			attestRequests[i].setRequestTime(requestTime);
 //			if (reqAttestation.getTimeThreshold() ==null)
 //				attestRequests[i].setNextAction(ActionConverter.getIntFromAction(Action.SEND_REPORT));
 //			else
 //				attestRequests[i].setNextAction(ActionConverter.getIntFromAction(Action.DO_NOTHING));
 //			attestRequests[i].setIsConsumedByPollingWS(false);
 //			attestRequests[i].setMachineCert(dao.getMachineCert(reqAttestation.getHosts().get(i)));
 //			attestRequests[i].setRequestHost(requestHost);
 //			attestRequests[i].setCount(reqAttestation.getCount());
 //			attestRequests[i].setPCRMask(reqAttestation.getPCRmask());
 //			attestRequests[i].setIsSync(isSync);
 //		}
 //		for(AttestRequest req: attestRequests)
 //			dao.saveRequest(req);
 //		return requestId;
 //	}
 
 	/**
 	 * get requests by specific requestId
 	 * @param requestId
 	 * @return
 	 */
 	public static List<AttestRequest> getRequestsByReqId(String requestId) {
 		AttestDao dao = new AttestDao();
 		return dao.getRequestsByRequestId(requestId);
 	}
 
 	/**
 	 * get newest request by id.
 	 * @param id
 	 * @return
 	 */
 	public static AttestRequest loadRequest(Long id) {
 		AttestDao dao = new AttestDao();
         return dao.getRequestById(id);
 	}
 
 	/**
 	 * authentication
 	 * @param authblob
 	 * @return
 	 */
 	public static boolean ISV_Autherntication_module() {
 		return true;
 	}
 }
