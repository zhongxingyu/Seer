 package gov.nih.nci.rembrandt.web.inbox;
 
 import gov.nih.nci.caintegrator.application.cache.BusinessTierCache;
 import gov.nih.nci.caintegrator.application.download.DownloadStatus;
 import gov.nih.nci.caintegrator.application.download.DownloadTask;
 import gov.nih.nci.caintegrator.application.download.caarray.CaArrayFileDownloadManagerInterface;
 import gov.nih.nci.caintegrator.application.zip.ZipItem;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.caintegrator.service.task.Task;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.download.caarray.RembrandtCaArrayFileDownloadManager;
 import gov.nih.nci.rembrandt.dto.query.CompoundQuery;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import uk.ltd.getahead.dwr.ExecutionContext;
 
 
 
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
 
 public class QueryInbox {
 	
 	private HttpSession session;
 	private BusinessTierCache btc;
 	private RembrandtPresentationTierCache ptc;
 	private CaArrayFileDownloadManagerInterface rbtCaArrayFileDownloadManagerInterface;
 	private String zipFileUrl = System.getProperty(RembrandtCaArrayFileDownloadManager.ZIP_FILE_URL);
 	private final String TOKEN = "#$#"; 
 	
 	public QueryInbox()	{
 		//get some common stuff
 		session = ExecutionContext.get().getSession(false);
 		btc = ApplicationFactory.getBusinessTierCache();
 		ptc = ApplicationFactory.getPresentationTierCache();
 		//ServletContext servletContext = session.getServletContext();
 		
 	}
 	
 	public QueryInbox(HttpSession session)	{
 		this.session = session;
 		btc = ApplicationFactory.getBusinessTierCache();
 		ptc = ApplicationFactory.getPresentationTierCache();
 	}
 	
 	
 	public String checkSingleTaskResult(String sid, String tid)	{
 		//check the status of a single task
 		String currentStatus = "";
 		
 		TaskResult t = ptc.getTaskResult(sid, tid);
        //System.out.println("Reading to cache Id: "+ t.getTask().getId()+" Status: "+t.getTask().getStatus());
 
 		switch(t.getTask().getStatus())	{
 			case Completed:
 				currentStatus = "Completed";
 			break;
 			case Running:
 				currentStatus = "Running";
 			break;
 			case Error:
 				currentStatus = "Error";
 			break;
 			case Email:
 				currentStatus = "Email";
 			break;
 			case Retrieving:
 				currentStatus = "Retrieving";
 			break;
 			default:
 				currentStatus = "Running";
 			break;
 		}
 		
 		return currentStatus;
 	}
 	
 	public String checkSingle(String sid, String tid)	{
 		//check the status of a single task
 		String currentStatus = "";
 		
 		Finding f = (Finding) btc.getObjectFromSessionCache(sid, tid);
 		
 		switch(f.getStatus())	{
 			case Completed:
 				currentStatus = "Completed";
 			break;
 			case Running:
 				currentStatus = "Running";
 			break;
 			case Error:
 				currentStatus = "Error";
 			break;
 			case Email:
 				currentStatus = "Email";
 			break;
 			case Retrieving:
 				currentStatus = "Retrieving";
 			break;
 			default:
 				currentStatus = "Running";
 			break;
 		}
 		
 		return currentStatus;
 	}
 	
 	public Map checkAllStatus(String sid)	{
 		Map currentStatuses = new HashMap();
 		
 		Collection<Finding> findings = btc.getAllSessionFindings(sid);
 		for(Finding f: findings){
 			String tmp = new String();
 			if(f.getTaskId()!=null && f.getTaskId().equals("GeneExpressionPlot"))	{
 				continue; //dont want this one...its for internal use only
 			}
 			tmp = this.checkSingle(sid, f.getTaskId());
 			
 			Map fdata = new HashMap();
 			fdata.put("time", String.valueOf(f.getElapsedTime()/1000));
 			fdata.put("status", tmp);
 			if(f.getStatus()!=null && f.getStatus().getComment()!=null)	{
 				fdata.put("comments", StringEscapeUtils.escapeJavaScript(f.getStatus().getComment()));
 			}
 			currentStatuses.put(f.getTaskId(), fdata);
 		}
 		
 		return currentStatuses;
 	}
 	
 	public Map checkAllTaskResultsStatus(String sid)	{
 		Map currentStatuses = new HashMap();
 		String numberOfSeconds = System.getProperty("rembrandt.numberOfSecondsToWaitBeforeEmail");
 		int numberOfSecondsToWaitBeforeEmail = 45;
 		if(numberOfSeconds != null){
 					try {
 						numberOfSecondsToWaitBeforeEmail = Integer.parseInt(numberOfSeconds.trim());
 					} catch (NumberFormatException e) {
 						numberOfSecondsToWaitBeforeEmail = 45;
 					}
 
 		}
 		Collection<TaskResult> tasks = ptc.getAllSessionTaskResults(sid);
 		for(TaskResult taskResult: tasks){
 			Task t = taskResult.getTask();
 			String tmp = new String();
 			tmp = this.checkSingleTaskResult(sid, t.getId());
 			
 			Map fdata = new HashMap();
 			fdata.put("cache_id", sid);
			fdata.put("task_id", t.getId());
 			fdata.put("time", String.valueOf(t.getElapsedTimeInSec()));
	        System.out.println("Cache Id: "+ sid+ "Task Id: "+ t.getId()+" Status: "+tmp+" Time: "+t.getElapsedTimeInSec());

 			if(t.getStatus()!= null && t.getStatus().equals(FindingStatus.Running) && t.getElapsedTimeInSec()> numberOfSecondsToWaitBeforeEmail){
 				fdata.put("email_timeout", "true");
 			}else{
 				fdata.put("email_timeout", "false");
 			}
 			fdata.put("status", tmp);
			if(!tmp.equals("Completed")){
 				fdata.put("emailIcon", " ");
 			}
 			if(t.getStatus()!=null && t.getStatus().getComment()!=null)	{
 				fdata.put("comments", StringEscapeUtils.escapeJavaScript(t.getStatus().getComment()));
 			}
 			currentStatuses.put(t.getId(), fdata);
	        

 		}
		System.out.println(currentStatuses);
 		return currentStatuses;
 	}
 	
 	public String checkAllDownloadStatus()	{
 
 		try {
 			rbtCaArrayFileDownloadManagerInterface = ApplicationContext.getCaArrayFileDownloadManagerInterface();
 			JSONArray dlArray = new JSONArray();
 			JSONObject dlObject = new JSONObject();
 			if(rbtCaArrayFileDownloadManagerInterface == null){
 				dlObject.put("name", "caArray server unavaiable");
 				dlObject.put("status", DownloadStatus.Error);
 				dlObject.put("url", "");		
 				dlObject.put("size", "");
 			}
 			else{
 				rbtCaArrayFileDownloadManagerInterface.setBusinessCacheManager(ApplicationFactory.getBusinessTierCache());
 				Collection<DownloadTask> downloads = rbtCaArrayFileDownloadManagerInterface.getAllSessionDownloads(session.getId());
 				
 	
 				//////// TESTing
 	//			dlObject.put("name", "My first download");
 	//			dlObject.put("status", "downloading");
 	//			dlArray.add(dlObject);
 	//			dlObject = new JSONObject();
 	//			dlObject.put("name", "My second download");
 	//			dlObject.put("status", "zipping");
 	//			dlArray.add(dlObject);
 				////// END TESTING
 				
 				for(DownloadTask dl : downloads){
 					if(dl.getListOfZipFileLists() == null){
 						dlObject = new JSONObject();
 						dlObject.put("name", dl.getZipFileName());
 						dlObject.put("status", dl.getDownloadStatus().toString());
 						if(dl.getZipFileName() != null)
 							dlObject.put("url", zipFileUrl +  dl.getZipFileName() );
 						else
 							dlObject.put("url", "");		
 						
 						;
 						if(dl.getZipFileSize()!= null)
 							dlObject.put("size", FileUtils.byteCountToDisplaySize(dl.getZipFileSize()));
 						else
 							dlObject.put("size", "");	
 						
 						dlArray.add(dlObject);
 					}else {
 						List<ZipItem> zipItems = dl.getListOfZipFileLists();
 						for(ZipItem zi : zipItems){
 							dlObject = new JSONObject();
 							dlObject.put("name", zi.getFileName());
 							dlObject.put("status", dl.getDownloadStatus().toString());
 							if(zi.getFileName() != null)
 								dlObject.put("url", zipFileUrl +  zi.getFileName() );
 							else
 								dlObject.put("url", "");		
 							
 							;
 							if(zi.getFileSize()!= null)
 								dlObject.put("size", FileUtils.byteCountToDisplaySize(zi.getFileSize()));
 							else
 								dlObject.put("size", "");	
 							
 							dlArray.add(dlObject);
 						}
 					}
 				}
 			}
 			return dlArray.toString();
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "error";
 		}
 	}
 	
 	public String checkStatus()	{
 		//simulate that the query is still running, assuming we have only 1 query for testing
 
 		Random r = new Random();
 		int randInt = Math.abs(r.nextInt()) % 11;
 		if(randInt % 2 == 0)
 			return "false";
 		else
 			return "true";
 	}
 	
 	public String deleteFinding(String key)	{
 		String success = "fail";
 		try	{
 		
 			success = "pass";
 		}
 		catch(Exception e){}
 		return success;
 	}
 
 	public Map mapTest(String testKey)	{
 		Map myMap = new HashMap();
 		myMap.put("firstKey", testKey);
 		myMap.put("secondKey", testKey+"_1");
 		return myMap;
 	}
 	
 	public String getQueryName()	{
 		String st = "nothing";
 		
 		try	{
 			st = String.valueOf(ptc.getSessionQueryBag(session.getId()).getQueries().size());
 		}
 		catch(Exception e){
 			st = "no worky";
 		}
 		
 		return st;	
 	}
 	public String getQueryDetailFromCompoundQuery(String queryName){
 		String result = "";
 		String compoundQueryName = "";
 		int index = queryName.lastIndexOf(TOKEN);
 		if (index <= 0)
 			return result;
 		else {
 			compoundQueryName = queryName.substring(0, index).trim();
 			queryName = queryName.substring(index+ TOKEN.length());
 		}
 		CompoundQuery cq = (CompoundQuery)ptc.getSessionQueryBag(session.getId()).getCompoundQuery(compoundQueryName);
 		if (cq != null){
 			result = cq.getQueryDetails(queryName);
 		}
 
 		return result;
 	}
 }
