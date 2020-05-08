 package gov.nih.nci.caintegrator.application.report;
 
 import gov.nih.nci.caintegrator.application.bean.LossOfExpressionIHCFindingReportBean;
 import gov.nih.nci.caintegrator.application.bean.LossOfExpressionIHCFindingReportBean;
 import gov.nih.nci.caintegrator.application.util.PatientComparator;
 import gov.nih.nci.caintegrator.application.util.TimepointStringComparator;
 import gov.nih.nci.caintegrator.domain.finding.protein.ihc.bean.LossOfExpressionIHCFinding;
 import gov.nih.nci.caintegrator.domain.finding.protein.ihc.bean.LossOfExpressionIHCFinding;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.caintegrator.studyQueryService.dto.ihc.LossOfExpressionIHCFindingCriteria;
 import gov.nih.nci.caintegrator.studyQueryService.dto.ihc.LossOfExpressionIHCFindingCriteria;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 /**
  * @author rossok
  * Nov 30,2006
  * 
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class LossOfExpressionIHCReport{
 
 	/**
 	 * 
 	 */
 	public LossOfExpressionIHCReport() {}
 
 	
 	public static Document getReportXML(Finding finding, Map filterMapParams) {
 
 		Document document = DocumentHelper.createDocument();
 
 			Element report = document.addElement( "Report" );
 			Element cell = null;
 			Element data = null;
 			Element dataRow = null;
 			//ADD BASIC ATTRIBUTED
 	        report.addAttribute("reportType", "IHC Loss Of Expression");
 	        report.addAttribute("groupBy", "none");
 	        String queryName = finding.getTaskId();	       
 	        report.addAttribute("queryName", queryName);
 	        report.addAttribute("sessionId", "the session id");	        
 		    
 			StringBuffer sb = new StringBuffer();
 		     
 			/* *************************************************************************************************
              * 
              * PROCESS QUERY DETAILS
              * 
              * *************************************************************************************************             * 
 			 */
             LossOfExpressionIHCFindingCriteria criteria = (LossOfExpressionIHCFindingCriteria)finding.getQueryDTO();
             ArrayList<String> queryDetails = new ArrayList();
             if(criteria!=null){
                 String tmp = ""; 
                 ArrayList<String> tmpCollection = new ArrayList<String>();
                 tmp = criteria.getQueryName()!=null ? criteria.getQueryName() : "";
                 queryDetails.add("Query Name: " + tmp);
                 tmp = "";
                 if(criteria.getProteinBiomarkerCrit()!=null && 
                         criteria.getProteinBiomarkerCrit().getProteinNameCollection()!=null && 
                         !criteria.getProteinBiomarkerCrit().getProteinNameCollection().isEmpty()){
                     Set<String> biomarkers = criteria.getProteinBiomarkerCrit().getProteinNameCollection();
                     tmpCollection = new ArrayList<String>(biomarkers);
                     for(String bm : tmpCollection){
                             if(bm != tmpCollection.get(0)){
                                 tmp += ", " + bm;
                             }
                             else{
                                 tmp += bm;
                             }
                     }
                     
                 }
                 queryDetails.add("Biomarker(s): " + tmp);
                 tmpCollection.clear();
                 tmp = "";
                 if(criteria.getSpecimenCriteria()!=null && 
                         criteria.getSpecimenCriteria().getTimeCourseCollection()!=null && 
                         !criteria.getSpecimenCriteria().getTimeCourseCollection().isEmpty()){
                     Set<String> timepoints = criteria.getSpecimenCriteria().getTimeCourseCollection();                    
                     tmpCollection = new ArrayList<String>(timepoints);
                     TimepointStringComparator ts = new TimepointStringComparator();
                     Collections.sort(tmpCollection, ts);
                     for(String tc : tmpCollection){
                         if(tc != tmpCollection.get(0)){
                             tmp += ", " + tc;
                         }
                         else{
                             tmp += tc;
                         }
                     }
                 }
                 queryDetails.add("Timepoint(s): " + tmp);
                 tmpCollection.clear();
                 tmp = "";                  
                 if(criteria.getBenignSumOperator()!=null && criteria.getBenignSum()!=null){
                     tmp = criteria.getBenignSumOperator() + " " + criteria.getBenignSum().toString();
                 }
                 queryDetails.add("Benign Sum: " + tmp);
                 tmp = "";
                 if(criteria.getInvasiveSumOperator()!=null && criteria.getInvasiveSum()!=null){
                     tmp = criteria.getInvasiveSumOperator() + " " + criteria.getInvasiveSum().toString();
                 }
                 queryDetails.add("Invasive Sum: " + tmp);
                 tmp = "";            
                 if(criteria.getResultCodeCollection()!=null && !criteria.getResultCodeCollection().isEmpty()){
                     Set<String> resultCodes = (Set<String>) criteria.getResultCodeCollection();                    
                     tmpCollection = new ArrayList<String>(resultCodes);
                     for(String rc : tmpCollection){
                         if(rc != tmpCollection.get(0)){
                             tmp += ", " + rc;
                         }
                         else{
                             tmp += rc;
                         }
                     }
                    
                 }
                 queryDetails.add("Loss Result: " + tmp); 
                 tmpCollection.clear();
                 tmp = "";
             }
 			
             String qd = "";
             for(String q : queryDetails){
                 qd += q + " ||| ";
             }
             
             /* **************************************************************************************************************
              * 
              * BUILD REPORT AS XML
              * 
              * **************************************************************************************************************
              */
             
             //GET FINDINGS, CAST TO APPROPRIATE FINDINGS AND ADD TO ARRAYLIST (RESULTS)
             ArrayList dFindings = new ArrayList(finding.getDomainFindings());
             ArrayList<LossOfExpressionIHCFinding> domainFindings = new ArrayList<LossOfExpressionIHCFinding>(dFindings);
 			ArrayList<LossOfExpressionIHCFindingReportBean> results = new ArrayList<LossOfExpressionIHCFindingReportBean>();
             for(LossOfExpressionIHCFinding loef : domainFindings)  {
                 LossOfExpressionIHCFindingReportBean reportBean = new LossOfExpressionIHCFindingReportBean(loef);
                 results.add(reportBean);
             }
             
             
 			if(!results.isEmpty())	{
                 
                 //SORT THE ARRAYLIST(RESULTS) BY PATIENT DID
                 PatientComparator p = new PatientComparator();
                 Collections.sort(results, p);
                 
                 //CREATE A HASHMAP SORTED BY PATIENT DID AS THE KEY AND THE ARRAYLIST OF REPORTBEANS AS THE VALUE                
                  
                 Map<String,ArrayList<LossOfExpressionIHCFindingReportBean>> reportBeanMap = new HashMap<String,ArrayList<LossOfExpressionIHCFindingReportBean>>();
                 
                 for(int i =0; i<results.size();i++){
                    boolean found = false;
                    Set<String> keys = reportBeanMap.keySet();
                    if(!keys.isEmpty()){
                        for(String k:keys){
                            String s = results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName();
                            if(s.equals(k)){
                                found = true;
                                reportBeanMap.get(k).add(results.get(i));
                                break;
                            }
                        }
                        if(!found){
                            reportBeanMap.put(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName(),new ArrayList<LossOfExpressionIHCFindingReportBean>());
                            reportBeanMap.get(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName()).add(results.get(i));
                        }
                     }
                    else{
                         reportBeanMap.put(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName(),new ArrayList<LossOfExpressionIHCFindingReportBean>());
                         reportBeanMap.get(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName()).add(results.get(i));
                     }
                 }
                  
                 //IF THE USER SELECTED TIMEPOINTS FOR WHICH THAT PATIENT DID DID NOT HAVE DATA, CREATE NULL BEANS SO AS TO RENDER A READABLE REPORT
                 Set<String> b = reportBeanMap.keySet();
                         for(String g: b){
                             while(reportBeanMap.get(g).size()<(reportBeanMap.get(g).get(0).getTimepointHeaders(criteria).size())){
                                 reportBeanMap.get(g).add(new LossOfExpressionIHCFindingReportBean(new LossOfExpressionIHCFinding()));
                             }
                         }
 				
                 //ADD QUERY DETAILS      
                 Element details = report.addElement("Query_details");                
 				cell = details.addElement("Data");
 				cell.addText(qd);
 				cell = null;
                 
                 //ADD HEADERS THAT ARE TIMEPOINT-INDEPENDENT (PATIENT AND BIOMARKER)
                 Element headerRow = report.addElement("Row").addAttribute("name", "headerRow");
                 ArrayList<String> ntpHeaders = results.get(0).getNonTimepointHeaders();
                 for(String ntpHeader : ntpHeaders){
                     cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "ntpHeader");
                     data = cell.addElement("Data").addAttribute("type", "header").addText(ntpHeader);
                     data = null;
                     cell = null;    
                 }
 				
 				//ADD HEADERS THAT ARE TIMEPOINT DEPENDENT
                 ArrayList<String> headers = results.get(0).getHeaders();                
                 for(String header : headers){                    
     		        cell = headerRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "header").addAttribute("group", "header");
     			        data = cell.addElement("Data").addAttribute("type", "header").addText(header);
     			        data = null;
     			        cell = null;		        
                 }
                 
 		        
                 //ADD TIMEPOINT SUBHEADERS
                 ArrayList<String> tpHeaders = results.get(0).getTimepointHeaders(criteria);
                 TimepointStringComparator ts = new TimepointStringComparator();
                 Collections.sort(tpHeaders,ts);   
                 Element tpHeaderRow = report.addElement("Row").addAttribute("name", "tpHeaderRow");
                // for(String tpHeader : tpHeaders){  
                 for(int i=0; i<tpHeaders.size();i++){                    
                     cell = tpHeaderRow.addElement("Cell").addAttribute("type", "header").addAttribute("class", "firstTP").addAttribute("group", "tpHeader");                        
                    data = cell.addElement("Data").addAttribute("type", "header").addText(tpHeaders.get(i));                    
                     data = null;
                     cell = null;
                 }
                 
                   Set<String> keys = reportBeanMap.keySet();      
 		    	// ADD DATA ROWS		   
                    for(String key : keys) {       
                        
                                        dataRow = report.addElement("Row").addAttribute("name", "dataRow");                         
                                        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "sample").addAttribute("group", "data");
                                            data = cell.addElement("Data").addAttribute("type", "selectable").addText(reportBeanMap.get(key).get(0).getPatientDID());
                                            data = null;
                                        cell = null;                                               
                                        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                           data = cell.addElement("Data").addAttribute("type", "header").addText(reportBeanMap.get(key).get(0).getBiomarkerName());
                                            data = null;
                                        cell = null;
                                        
                                         //GRAB EACH REPORT BEAN IN EACH ARRAYLIST AND MATCH UP TO THE APPROPRIATE TIMEPOINT AS A MAP WITH THE TIMEPOINT AS KEY AND REPORTBEAN THE VALUE
                                         ArrayList<LossOfExpressionIHCFindingReportBean> myList = reportBeanMap.get(key);
                                         Map<String,LossOfExpressionIHCFindingReportBean> myMap = new HashMap<String,LossOfExpressionIHCFindingReportBean>();
                                         ArrayList<LossOfExpressionIHCFindingReportBean> mySortedMap = new ArrayList<LossOfExpressionIHCFindingReportBean>();
                                         
                                         for(LossOfExpressionIHCFindingReportBean ggg : myList){
                                             for(int i=0; i<tpHeaders.size();i++){
                                                 if(ggg.getTimepoint().equalsIgnoreCase(tpHeaders.get(i))){
                                                     myMap.put(tpHeaders.get(i),ggg);
                                                     break;
                                                 }
                                                 else if(ggg.getTimepoint().equals("--")){
                                                     if(!myMap.containsKey(tpHeaders.get(i))){
                                                         myMap.put(tpHeaders.get(i),ggg);
                                                         break;
                                                     }                                                    
                                                 }
                                             }
                                         }
                                         
                                         //SORT MAP BY TIMEPOINT SO THAT THE REPORT BEAN DATA CAN EASILY BE DISPLAYED UNDER THE APPROPRIATE TIEMPOINT
                                         for(int i=0; i<tpHeaders.size();i++){
                                             for(String k : myMap.keySet()){
                                                 if(k.equalsIgnoreCase(tpHeaders.get(i))){
                                                     mySortedMap.add(myMap.get(k));
                                                 }
                                             }
                                         }
                                             
                                             
                                             
                                         //ITERATE OVER THE MAP FOR EACH DATA FIELD WITH ITS CORRESPONDING TIMEPOINT AND BUILD DATA ROWS
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)	{         		        	      			        
                         			        cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getBenignPresentValue());                                        
                         			        data = null;
                         			        cell = null;
                                         }
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getInvasiveSum());
                                             data = null;
                                             cell = null;
                         			       
                                         }
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getBenignSum());
                                             data = null;
                                             cell = null;
                                            
                                         }
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getInvasiveBenignDiff());
                                             data = null;
                                             cell = null;
                                            
                                         }
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getComments());
                                             data = null;
                                             cell = null;
                                            
                                         }
                                         for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                             cell = dataRow.addElement("Cell").addAttribute("type", "data").addAttribute("class", "data").addAttribute("group", "data");
                                             data = cell.addElement("Data").addAttribute("type", "header").addText(reportBean.getLossResult());
                                             data = null;
                                             cell = null;
                                            
                                         }
                        
                    }                   
 			}
             
 			else {
 				//TODO: handle this error
 				sb.append("<br><Br>Loss of Expression is empty<br>");
 			}
 		    
 		    return document;
 	}
     
     public static HSSFWorkbook getReportExcel(Finding finding, HashMap map) {
         HSSFWorkbook wb = new HSSFWorkbook();
         HSSFSheet sheet = wb.createSheet(finding.getTaskId());
         
         ArrayList dFindings = new ArrayList(finding.getDomainFindings());
         ArrayList<LossOfExpressionIHCFinding> domainFindings = new ArrayList<LossOfExpressionIHCFinding>(dFindings);
         ArrayList<LossOfExpressionIHCFindingReportBean> results = new ArrayList<LossOfExpressionIHCFindingReportBean>();
         for(LossOfExpressionIHCFinding loef : domainFindings)  {
             LossOfExpressionIHCFindingReportBean reportBean = new LossOfExpressionIHCFindingReportBean(loef);
             results.add(reportBean);
         }
         LossOfExpressionIHCFindingCriteria criteria = (LossOfExpressionIHCFindingCriteria)finding.getQueryDTO();
         
         if(!results.isEmpty())  {
             
             //SORT THE ARRAYLIST(RESULTS) BY PATIENT DID
             PatientComparator p = new PatientComparator();
             Collections.sort(results, p);
             
             //CREATE A HASHMAP SORTED BY PATIENT DID AS THE KEY AND THE ARRAYLIST OF REPORTBEANS AS THE VALUE                
              
             Map<String,ArrayList<LossOfExpressionIHCFindingReportBean>> reportBeanMap = new HashMap<String,ArrayList<LossOfExpressionIHCFindingReportBean>>();
             
             for(int i =0; i<results.size();i++){
                 if(i==0){
                     reportBeanMap.put(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName(),new ArrayList<LossOfExpressionIHCFindingReportBean>());
                     reportBeanMap.get(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName()).add(results.get(i));                        
                 }
                 else if(!results.get(i).getPatientDID().equalsIgnoreCase(results.get(i-1).getPatientDID())){ 
                     reportBeanMap.put(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName(),new ArrayList<LossOfExpressionIHCFindingReportBean>());
                     reportBeanMap.get(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName()).add(results.get(i));
                 }
                 else if(results.get(i).getPatientDID().equalsIgnoreCase(results.get(i-1).getPatientDID()) && !results.get(i).getBiomarkerName().equalsIgnoreCase(results.get(i-1).getBiomarkerName())){ 
                     reportBeanMap.put(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName(),new ArrayList<LossOfExpressionIHCFindingReportBean>());
                     reportBeanMap.get(results.get(i).getPatientDID()+"_"+results.get(i).getBiomarkerName()).add(results.get(i));
                 }
                 else{
                     reportBeanMap.get(results.get(i-1).getPatientDID()+"_"+results.get(i-1).getBiomarkerName()).add(results.get(i));                        
                 }
             }
              
             //IF THE USER SELECTED TIMEPOINTS FOR WHICH THAT PATIENT DID DID NOT HAVE DATA, CREATE NULL BEANS SO AS TO RENDER A READABLE REPORT
             Set<String> b = reportBeanMap.keySet();
                     for(String g: b){
                         while(reportBeanMap.get(g).size()<(reportBeanMap.get(g).get(0).getTimepointHeaders(criteria).size())){
                             reportBeanMap.get(g).add(new LossOfExpressionIHCFindingReportBean(new LossOfExpressionIHCFinding()));
                         }
                     }
                     
             ArrayList<String> ntpHeaders = results.get(0).getNonTimepointHeaders();
             HSSFRow row = sheet.createRow((short) 0);
             
             
             //ADD HEADERS THAT ARE TIMEPOINT DEPENDENT
             ArrayList<String> headers = results.get(0).getHeaders(); 
             ArrayList<String> tpHeaders = results.get(0).getTimepointHeaders(criteria);
             TimepointStringComparator ts = new TimepointStringComparator();
             Collections.sort(tpHeaders,ts);
             ArrayList<String> combinedHeaders = new ArrayList<String>(); 
             for(int i=0; i<headers.size();i++){
                 for(int j=0; j<tpHeaders.size();j++){
                     combinedHeaders.add(headers.get(i)+tpHeaders.get(j));
                 }
             }
             
             ntpHeaders.addAll(combinedHeaders);
             
             for(int i = 0; i<ntpHeaders.size();i++){
                 HSSFCell cell = row.createCell((short) i);
                 cell.setCellValue(ntpHeaders.get(i));                   
             }
             
             row = null;
             HSSFCell dataCell = null;
             Set<String> keysSet = reportBeanMap.keySet(); 
             ArrayList<String> keys = new ArrayList<String>(keysSet);
             // ADD DATA ROWS           
                for(int i=0;i<keys.size();i++) {       
                                    sheet.createFreezePane( 0, 1, 0, 1 );
                                    row = sheet.createRow((short) i + 1); 
                                    dataCell = row.createCell((short) 0);
                                    dataCell.setCellValue(reportBeanMap.get(keys.get(i)).get(0).getPatientDID());
                                    
                                    dataCell = row.createCell((short) 1);
                                    dataCell.setCellValue(reportBeanMap.get(keys.get(i)).get(0).getBiomarkerName());
                
                                     //GRAB EACH REPORT BEAN IN EACH ARRAYLIST AND MATCH UP TO THE APPROPRIATE TIMEPOINT AS A MAP WITH THE TIMEPOINT AS KEY AND REPORTBEAN THE VALUE
                                     ArrayList<LossOfExpressionIHCFindingReportBean> myList = reportBeanMap.get(keys.get(i));
                                     Map<String,LossOfExpressionIHCFindingReportBean> myMap = new HashMap<String,LossOfExpressionIHCFindingReportBean>();
                                     ArrayList<LossOfExpressionIHCFindingReportBean> mySortedMap = new ArrayList<LossOfExpressionIHCFindingReportBean>();
                                     
                                     for(LossOfExpressionIHCFindingReportBean ggg : myList){
                                         for(int j=0; j<tpHeaders.size();j++){
                                             if(ggg.getTimepoint().equalsIgnoreCase(tpHeaders.get(j))){
                                                 myMap.put(tpHeaders.get(j),ggg);
                                                 break;
                                             }
                                             else if(ggg.getTimepoint().equals("--")){
                                                 if(!myMap.containsKey(tpHeaders.get(j))){
                                                     myMap.put(tpHeaders.get(j),ggg);
                                                     break;
                                                 }                                                    
                                             }
                                         }
                                     }
                                     
                                     //SORT MAP BY TIMEPOINT SO THAT THE REPORT BEAN DATA CAN EASILY BE DISPLAYED UNDER THE APPROPRIATE TIEMPOINT
                                     for(int t=0; t<tpHeaders.size();t++){
                                         for(String k : myMap.keySet()){
                                             if(k.equalsIgnoreCase(tpHeaders.get(t))){
                                                 mySortedMap.add(myMap.get(k));
                                             }
                                         }
                                     }
                                     
                                         
                                         
                                    int counter = 2;
                                     //ITERATE OVER THE MAP FOR EACH DATA FIELD WITH ITS CORRESPONDING TIMEPOINT AND BUILD DATA ROWS
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap) {                                                   
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getBenignPresentValue());                                        
                                     }
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         if(!reportBean.getInvasiveSum().equals("--")){
                                         dataCell.setCellValue(Short.parseShort(reportBean.getInvasiveSum())); 
                                         }else{
                                         dataCell.setCellValue(reportBean.getInvasiveSum()); 
                                         }
                                     }
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         if(!reportBean.getBenignSum().equals("--")){
                                         dataCell.setCellValue(Short.parseShort(reportBean.getBenignSum())); 
                                         }else{
                                         dataCell.setCellValue(reportBean.getBenignSum());     
                                         }
                                     }
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         if(!reportBean.getInvasiveBenignDiff().equals("--")){
                                         dataCell.setCellValue(Short.parseShort(reportBean.getInvasiveBenignDiff()));
                                         }else{
                                         dataCell.setCellValue(reportBean.getInvasiveBenignDiff());   
                                         }
                                     }
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getComments()); 
                                     }
                                     for(LossOfExpressionIHCFindingReportBean reportBean : mySortedMap)  {
                                         dataCell = row.createCell((short) counter++);
                                         dataCell.setCellValue(reportBean.getLossResult()); 
                                     }
                
                    
                }  
         
         }
         
         else {
             //TODO: handle this error
            
         }
         return wb;
     }
 
 }
 
