 /*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package gov.niarl.hisAppraiser.hibernate.util;
 import gov.niarl.hisAppraiser.hibernate.dao.AnalysisTypesDao;
 import gov.niarl.hisAppraiser.hibernate.dao.AttestDao;
 import gov.niarl.hisAppraiser.hibernate.dao.OSDao;
 import gov.niarl.hisAppraiser.hibernate.domain.AnalysisTypes;
 import gov.niarl.hisAppraiser.hibernate.domain.AttestRequest;
 import gov.niarl.hisAppraiser.hibernate.domain.AuditLog;
 import gov.niarl.hisAppraiser.hibernate.domain.PCRManifest;
 import gov.niarl.hisAppraiser.hibernate.domain.PcrWhiteList;
 import gov.niarl.hisAppraiser.hibernate.util.ResultConverter.AttestResult;
 import gov.niarl.hisAppraiser.util.HisUtil;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ws.rs.core.GenericEntity;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class AttestService {
 	
 	/**
 	 * Reads the list of analyses requested within the attestation
 	 * request received and executes them.
 	 * @param attestRequest The attestation request to be fulfilled
 	 * @return The attestation request received, updated with analyses
 	 * result.
 	 */
 	public static AttestRequest doAnalyses(AttestRequest attestRequest, String machineName) {
 		String analysisReqString = "VALIDATE_PCR;COMPARE_REPORT";
 		if (attestRequest.getAnalysisRequest() != null) {
 			analysisReqString = attestRequest.getAnalysisRequest();
 		}
 
 		String[] analysisReqList = analysisReqString.split(";");
 		for (String analysis : analysisReqList) {
 			if (analysis.length() == 0 && analysisReqList.length == 1) {
 				break;
 			} else if (analysis.equals("VALIDATE_PCR")) {
 				attestRequest = validatePCRReport(attestRequest, machineName);
 			} else if (analysis.equals("COMPARE_REPORT")) {
 				attestRequest = evaluateCompareReport(attestRequest);
 			} else {
 				attestRequest = analysisLauncher(attestRequest, analysis);
 			}
 		}
 		return attestRequest;
 	}
 
 	/**
 	 * Executes the received analysis, writing thier result in the
 	 * received AttestRequest
 	 * @param attestRequest The AttestRequest to be updated
 	 * @param analysis The analysis to be executed
 	 * @return The updated AttestRequest
 	 */
 	public static AttestRequest analysisLauncher(AttestRequest attestRequest, String analysis) {
 		OSDao osDao = new OSDao();
 		boolean analysisResult = false;
 		String analysisStatus = "ANALYSIS_COMPLETED";
 		String analysisOutput = "";
 		String analysisName = analysis.split(",")[0].trim();
 		String analysisParameters = "";
 		String os_name = osDao.findHostOS(attestRequest.getHostName());
 		AnalysisTypesDao ATDao = new AnalysisTypesDao();
 		AnalysisTypes analysisType = ATDao.getAnalysisTypeByName(analysisName);
 
 		if (analysisType == null) {
 			attestRequest = updateAnalysisResult(attestRequest, "NULL", false, "ANALYSIS_NOT_FOUND", "");
 			attestRequest.setResult(ResultConverter.getIntFromResult(AttestResult.UN_TRUSTED));
 			return attestRequest;
 		}
 
 		try {
 			if (os_name == null)
 				throw new Exception ("Error occurred retrieving OS name");
 
 			if (analysis.indexOf(',') != -1) {
 				analysisParameters = analysis.substring(analysis.indexOf(',') + 1);
 			}
 			
 			int intParamPcrMask = -1;
 			for (String parameter : analysisParameters.split(",")) {
 				if (!parameter.startsWith("pcrs="))
 					continue;
 
 				intParamPcrMask = 0;
 
 				for (String pcr : parameter.substring("pcrs=".length()).split("\\|")) {
 					if (pcr.trim().equals(""))
 						continue;
 					try {
						int intPcr = Integer.parseInt(pcr.trim());
						if (intPcr < 0 || intPcr > 23)
							throw new IllegalArgumentException("Wrong syntax: requested PCRs not in the allowed range [0-23]");
						intParamPcrMask |= (0x00800000 >> intPcr);
 					} catch (Exception e) {
 						throw new IllegalArgumentException("Wrong syntax: requested PCRs are not integers");
 					}
 				}
 				break;
 			}
 
 			byte[] requiredPcrMask = HisUtil.unHexString(analysisType.getRequiredPcrMask());
 			int intRequiredPcrMask = (requiredPcrMask[2] & 0xFF) | ((requiredPcrMask[1] & 0xFF) << 8) | ((requiredPcrMask[0] & 0xFF) << 16);
 
 			byte[] pcrIMLMask = HisUtil.unHexString(new AttestDao().getPcrIMLMask(attestRequest.getHostName()));
 			int intPcrIMLMask = (pcrIMLMask[2] & 0xFF) | ((pcrIMLMask[1] & 0xFF) << 8) | ((pcrIMLMask[0] & 0xFF) << 16);
 
 			/*
 			 * If the request does not contain the "pcrs" parameter
 			 * the entire list of PCRs from requiredPcrMask is assumed
 			 * to be requested by the analysis.
 			 */
 			if (intParamPcrMask == -1)
 				intParamPcrMask = intRequiredPcrMask;
 			else if ((intParamPcrMask | intRequiredPcrMask) != intRequiredPcrMask)
 				throw new UnsupportedOperationException("PCRs specified as parameter are not in the set of PCRs required by the analysis");
 			if ((intParamPcrMask | intPcrIMLMask) != intPcrIMLMask)
 				throw new UnsupportedOperationException("The host does not provide the complete logs (IML) for requested PCRs");
 
 			AttestUtil.loadProp();
 			String path = System.getenv("PATH");
 			String[] env_var = { "PATH=" + path, "ANALYSIS=" + analysisType.getName() + "," + analysisParameters, "OS=" + os_name,
 			                     "URL=" + AttestUtil.getDownloadIRWebServiceUrl(), "IR=" + attestRequest.getAuditLog().getId()};
 			Runtime r = Runtime.getRuntime();
 			String script_string = analysisType.getURL();
 			Process p = r.exec(script_string, env_var);
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			BufferedReader brerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 			int exitCode = p.waitFor();
 
 			String line = "";
 			analysisOutput = "";
 
 			while ((line = br.readLine()) != null) {
 				analysisOutput += line + "\n";
 			}
 			analysisOutput = analysisOutput.substring(0, Math.min(analysisOutput.length(), 80));
 
 			analysisOutput = analysisOutput.trim();
 			if (exitCode == 0) {
 				analysisResult = true;
 			} else if (exitCode != 1) {
 				analysisStatus = "SCRIPT_ERROR";
 
 				System.out.println("error: External analysis tool returned error code " + exitCode);
 				System.out.println("\tAnalysis tool output: " + analysisOutput.replace("\n", " \\n "));
 			}
 		} catch (Exception e) {
 			if (e instanceof IOException)
 				System.out.println("IO error executing analysis: " + e.getMessage());
 			else
 				System.out.println("Error executing analysis: " + e.getMessage());
 
 			analysisResult = false;
 			analysisStatus = "OAT_ERROR";
 			analysisOutput = "";
 
 			if (e instanceof UnsupportedOperationException || e instanceof IllegalArgumentException)
 				analysisOutput = e.getMessage();
 		}
 
 		if (!analysisResult)
 			attestRequest.setResult(ResultConverter.getIntFromResult(AttestResult.UN_TRUSTED));
 
 		attestRequest = updateAnalysisResult(attestRequest, "DB:" + analysisType.getId().toString(),
 		                                     analysisResult, analysisStatus, analysisOutput);
 		return attestRequest;
 	}
 	
 	/** 
 	 * Verifies if any errors occurred during the report comparison.
 	 * It updates the fields Result and analysisResults of the recevied
 	 * attestRequest and returns the updated object.
 	 * @param attestRequest The attestation request to be analysed
 	 * @return The updated attestRequest
 	 */
 	public static AttestRequest evaluateCompareReport(AttestRequest attestRequest) {
 		boolean analysisResult = false;
 		String analysisOutput = "";
 		AuditLog auditLog = attestRequest.getAuditLog();
 		if (auditLog != null && attestRequest.getIsConsumedByPollingWS()) {
 			if (auditLog.getReportCompareErrors() != null) {
 				analysisOutput = auditLog.getReportCompareErrors().trim();
 			} else {
 				analysisResult = true;
 			}
 		} else {
 			analysisOutput = "Analysis requested for a not valid report";
 		}
 
 		if (!analysisResult) {
 			attestRequest.setResult(ResultConverter.getIntFromResult(AttestResult.UN_TRUSTED));
 		}
 		attestRequest = updateAnalysisResult(attestRequest, "COMPARE_REPORT", analysisResult, "ANALYSIS_COMPLETED", analysisOutput);
 		return attestRequest;
 	}
 
 	/**
 	 * validate PCR value of a request. Here is 4 cases, that is timeout, unknown, trusted and untrusted.
 	 * case1 (timeout): attest's time is greater than default timeout of attesting from OpenAttestation.properties. In generally, it is usually set as 60 seconds;
 	 * case2 (unknown): machine is not enrolled in attest server.  Just check whether active machineCert is existed.
 	 * case3 (trusted): all hosts has attested and their pcrs has matched with PCRManifest table;
 	 * case4 (untrusted): all hosts has attested, but their pcrs cannot match with PCRManifest table.  
 	 * @param attestRequest of intending to validate. 
 	 * @return 
 	 */
 	public static AttestRequest validatePCRReport(AttestRequest attestRequest,String machineNameInput){
 		String analysisOutput = "";
 		attestRequest.getAuditLog();
 		List<PcrWhiteList> whiteList = new ArrayList<PcrWhiteList>();
 		AttestDao dao = new AttestDao();
 		boolean flag = true;
 		whiteList = dao.getPcrValue(machineNameInput);
 		
 		System.out.println(attestRequest.getId() +":" +attestRequest.getAuditLog().getId());
 		
 		 if(attestRequest.getAuditLog()!= null && attestRequest.getIsConsumedByPollingWS()){
 			 
 			AuditLog auditLog = attestRequest.getAuditLog();
 			HashMap<Integer,String> pcrs = new HashMap<Integer, String>();
 			pcrs = generatePcrsByAuditId(auditLog);
 			
 			if (whiteList!=null && whiteList.size() != 0){
 				for(int i=0; i<whiteList.size(); i++){
 					int pcrNumber = Integer.valueOf(whiteList.get(i).getPcrName()).intValue();
 						if(!whiteList.get(i).getPcrDigest().equalsIgnoreCase(pcrs.get(pcrNumber))){
 							analysisOutput += "PCR #" + whiteList.get(i).getPcrName() + " mismatch. ";
 							flag = false;
 						}
 				}
 			} else {
 				analysisOutput += "No PCR in white list.";
 				flag = false;
 			}
 			
 			if (!flag){
 				attestRequest.setResult(ResultConverter.getIntFromResult(AttestResult.UN_TRUSTED));
 			}
 			attestRequest = updateAnalysisResult(attestRequest, "VALIDATE_PCR", flag, "ANALYSIS_COMPLETED", analysisOutput.trim());
 		}
 		 return attestRequest;
 	}
 
 	/**
 	 * Receives the current content of field analysisResult of table
 	 * AttestRequest and updates it with received information
 	 * about the analysis: name, result, status and output.
 	 * @param prevAnalysisResult Previous content of field analysisResult
 	 * @param analysis The analysis to be added to analysisResult
 	 * @param result Result of the analysis to be added to analysisResult
 	 * @param status Status of the analysis to be added to analysisResult
 	 * @param output Output of the analysis to be added to analysisResult
 	 * @return The updated content of field analysisResult
 	 */
 	public static AttestRequest updateAnalysisResult(AttestRequest attestRequest, String analysis, boolean result, String status, String output) {
 		String analysisResult = (attestRequest.getAnalysisResults() == null) ? "" : attestRequest.getAnalysisResults();
 		analysisResult += analysis + "|" + result + "|" + status + "|" + output.length() + "|" + output + ";";
 		attestRequest.setAnalysisResults(analysisResult);
 
 		return attestRequest;
 	}
 	
 	/*
 	 * compare request's PCR with PCRManifest in DB. 
 	 * @Param needs to compare request. 
 	 *        At first convert request string from AttestRequest to List<Manifest>.
 	 *        A request may contain several pcrs, so needs parse a List of PCRmanifest.  
 	 * @Return null string if PCR not exists, else not null string 
 	 * with different pcrs' number and separating them with '|' like this '2|20'     
 	 */
 	public static String compareManifestPCR(List<PCRManifest> manifestPcrs){
 		GenericEntity<List<PCRManifest>> entity = new GenericEntity<List<PCRManifest>>(manifestPcrs) {}; 
 		AttestUtil.loadProp();
 		//manifestPcrs.
 		WebResource resource = AttestUtil.getClient(AttestUtil.getManifestWebServicesUrl());
 	    ClientResponse res = resource.path("/Validate").type("application/json").
   	              accept("application/json").post(ClientResponse.class,entity);
 	    return res.getEntity(String.class);
 	}
 
 	
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
 
 }
