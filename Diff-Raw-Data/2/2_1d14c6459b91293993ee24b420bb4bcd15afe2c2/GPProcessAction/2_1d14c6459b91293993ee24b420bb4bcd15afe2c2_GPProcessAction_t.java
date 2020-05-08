 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.service.task.GPTask;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.struts.form.GpProcessForm;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 import org.apache.struts.util.LabelValueBean;
 import org.genepattern.client.GPClient;
 import org.genepattern.util.StringUtils;
 import org.genepattern.visualizer.RunVisualizerConstants;
 import org.genepattern.webservice.AnalysisWebServiceProxy;
 import org.genepattern.webservice.JobResult;
 import org.genepattern.webservice.Parameter;
 import org.genepattern.webservice.TaskIntegratorProxy;
 import org.genepattern.webservice.WebServiceException;
 
 
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
 
 public class GPProcessAction extends DispatchAction {
     private static Logger logger = Logger.getLogger(GPProcessAction.class);
     private static String HMV = "Heat Map Viewer";
     private static String HC = "Hierarchical Clustering";
     private static String KNN = "K-Nearest Neighbors";
     private static String CMS = "Comparative Marker Selection";
     
     private static String HEAT_MAP_VIEW = "HeatMapViewer";
     private static String HC_PIPELINE = "HC.pipeline";
     private static String KNN_PIPELINE = "KNN.pipeline";
     private static String CMS_PIPELINE = "CMS.pipeline";
     
     private static String GP_SERVER = "gov.nih.nci.caintegrator.gp.server";
     
     public ActionForward setup(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
     	GpProcessForm gpForm = (GpProcessForm) form;
     	
     	processSetUp(gpForm, request);
 
         return mapping.findForward("success");
     }
     
     public ActionForward startApplet(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
     	logger.info("Entering startApplet method.......");
 
     	GpProcessForm gpForm = (GpProcessForm)form;
         String processName = gpForm.getProcessName();
         if (processName.equalsIgnoreCase(HC_PIPELINE)){
         	return runHCPipeline(mapping, form, request, response);
         }
         else if (processName.equalsIgnoreCase(KNN_PIPELINE)){
         	return runKNNPipeline(mapping, form, request, response);
         }
         else if (processName.equalsIgnoreCase(CMS_PIPELINE)){
         	return runCMSPipeline(mapping, form, request, response);
         }
 
 		String jobNumber = gpForm.getJobId(); 
 
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		GPTask gpTask = getGPTask(tempGpTaskList, jobNumber);
 		String gpserverURL = System.getProperty(GP_SERVER);
 		setFileInformation(request, gpserverURL,
 				"gov.nih.nci.caintegrator.gpvisualizer.heatmapviewer.gp_lsid",
 				"gov.nih.nci.caintegrator.gpvisualizer.heatmapviewer.commandLine");
 
 		String fileName = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".gct";
 
 		request.setAttribute(RunVisualizerConstants.DOWNLOAD_FILES, fileName);
 		
         request.setAttribute("name", HEAT_MAP_VIEW);
         request.setAttribute(RunVisualizerConstants.PARAM_NAMES, "dataset");
 
 		gpForm.setJobList(getGPTaskList(tempGpTaskList));
 
 		gpForm.setProcessList(getVisualizers());
 
         return mapping.findForward("appletViewer");
     }
     private ActionForward runHCPipeline(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
     	logger.info("Entering runHCPipeline.......");
 
     	GpProcessForm gpForm = (GpProcessForm)form;
 
 		String jobNumber = gpForm.getJobId(); 
 		GPTask gpTask = getGPTask(request, jobNumber);
 
 		String gpserverURL = System.getProperty(GP_SERVER);
 		Parameter[] par = new Parameter[1];
 		
 		String fileName = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".gct";
 
 		par[0] = new Parameter("VariationFiltering1.input.filename", fileName);
 
 		runGenePattern(request, HC_PIPELINE, par, gpTask);
         return mapping.findForward("viewJob");
     }
     private ActionForward runKNNPipeline(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
     	logger.info("Entering runKNNPipeline.......");
 
     	GpProcessForm gpForm = (GpProcessForm)form;
 
 		String jobNumber = gpForm.getJobId(); 
 
 		GPTask gpTask = getGPTask(request, jobNumber);
 
 		String gpserverURL = System.getProperty(GP_SERVER);
 		
 		Parameter[] par = new Parameter[2];
 		
 		String fileName1 = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".gct";
 		String fileName2 = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".cls";
 
 		par[0] = new Parameter("VariationFiltering1.input.filename", fileName1);
 		par[1] = new Parameter("SplitDatasetTrainTest2.cls.file", fileName2);
 
 		runGenePattern(request, KNN_PIPELINE, par, gpTask);
         return mapping.findForward("viewJob");
     }
     private ActionForward runCMSPipeline(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
     	logger.info("Entering runCMSPipeline.......");
 
     	GpProcessForm gpForm = (GpProcessForm)form;
 
 		String jobNumber = gpForm.getJobId(); 
 
 		GPTask gpTask = getGPTask(request, jobNumber);
 
 		String gpserverURL = System.getProperty(GP_SERVER);
 		
 		Parameter[] par = new Parameter[2];
 		
 		String fileName1 = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".gct";
 		String fileName2 = gpserverURL + "gp/jobResults/" + jobNumber + "/" + gpTask.getResultName() + ".cls";
 
 		par[0] = new Parameter("VariationFiltering1.input.filename", fileName1);
 		par[1] = new Parameter("ComparativeMarkerSelection2.cls.filename", fileName2);
 
 		runGenePattern(request, CMS_PIPELINE, par, gpTask);
 				
         return mapping.findForward("viewJob");
     }
     public ActionForward hcApplet(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
 
     	GpProcessForm gpForm = (GpProcessForm)form;
     	processSetUp(gpForm, request);
 		String jobNumber =  gpForm.getJobId(); 
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		GPTask gpTask = getGPTask(tempGpTaskList, jobNumber);
 		String gpserverURL = System.getProperty(GP_SERVER);
 		
 		setFileInformation(request, gpserverURL,
 				"gov.nih.nci.caintegrator.gpvisualizer.hcpipeline.gp_lsid",
 				"gov.nih.nci.caintegrator.gpvisualizer.hcpipeline.commandLine");
 
 		int newJobNumber = findChildJobNumber(request, 
 				Integer.parseInt(jobNumber), 
 				System.getProperty("gov.nih.nci.caintegrator.gpvisualizer.hcpipeline.child_lsid"));
 		
 		if (newJobNumber == -1)
 			return mapping.findForward("appletViewer");
 		
 		String fileName = gpserverURL + "gp/jobResults/" + newJobNumber + "/" +  gpTask.getResultName(); 
 
 		request.setAttribute("cdtFile", fileName + ".mad.cdt");
 		request.setAttribute("gtrFile", fileName + ".mad.gtr");
 		request.setAttribute("atrFile", fileName + ".mad.atr");
 
 		request.setAttribute(RunVisualizerConstants.DOWNLOAD_FILES, "cdt.file,gtr.file,atr.file");
 
         request.setAttribute("name", "HierarchicalClusteringViewer");
         request.setAttribute("gp_paramNames", "cdt.file,gtr.file,atr.file");
 
         return mapping.findForward("appletViewer");
     }
     public ActionForward knnApplet(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
 
     	GpProcessForm gpForm = (GpProcessForm)form;
     	processSetUp(gpForm, request);
 		String jobNumber = gpForm.getJobId(); 
 
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		GPTask gpTask = getGPTask(tempGpTaskList, jobNumber);
 
 		String gpserverURL = System.getProperty(GP_SERVER);
 		setFileInformation(request, gpserverURL,
 				"gov.nih.nci.caintegrator.gpvisualizer.predictionResultsViewer.gp_lsid",
 				"gov.nih.nci.caintegrator.gpvisualizer.predictionResultsViewer.commandLine");
 
 		int newJobNumber = findChildJobNumber(request, 
 				Integer.parseInt(jobNumber), 
 				System.getProperty("gov.nih.nci.caintegrator.gpvisualizer.predictionResultsViewer.child_lsid"));
 
 		if (newJobNumber == -1)
 			return mapping.findForward("appletViewer");
 		
		String fileName = gpserverURL + "gp/jobResults/" + newJobNumber + "/" + gpTask.getResultName() + ".mad.test.0.pred.odf";
 		logger.info("datafile name = " + fileName);
 		request.setAttribute("predictionResultsfilename", fileName);
 
 		request.setAttribute(RunVisualizerConstants.DOWNLOAD_FILES, "prediction.results.filename");
 
         request.setAttribute("name", "PredictionResultsViewer");
         request.setAttribute("gp_paramNames", "prediction.results.filename");
 
         return mapping.findForward("appletViewer");
     }
     public ActionForward cmsApplet(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
     	GpProcessForm gpForm = (GpProcessForm)form;
     	processSetUp(gpForm, request);
 		String jobNumber = gpForm.getJobId(); 
 
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		GPTask gpTask = getGPTask(tempGpTaskList, jobNumber);
 		
 		String gpserverURL = System.getProperty(GP_SERVER);
 		setFileInformation(request, gpserverURL,
 				"gov.nih.nci.caintegrator.gpvisualizer.comparativeMarkerSelectionViewer.gp_lsid",
 				"gov.nih.nci.caintegrator.gpvisualizer.comparativeMarkerSelectionViewer.commandLine");
 
 		int newJobNumber = findChildJobNumber(request, 
 				Integer.parseInt(jobNumber), 
 				System.getProperty("gov.nih.nci.caintegrator.gpvisualizer.comparativeMarkerSelectionViewer.one.child_lsid"));
 		if (newJobNumber == -1)
 			return mapping.findForward("appletViewer");
 		
 		String fileName1 = gpserverURL + "gp/jobResults/" + newJobNumber + "/" + gpTask.getResultName() + ".mad.gct";
 		
 		newJobNumber = findChildJobNumber(request, 
 				Integer.parseInt(jobNumber),
 				System.getProperty("gov.nih.nci.caintegrator.gpvisualizer.comparativeMarkerSelectionViewer.two.child_lsid"));
 
 		if (newJobNumber == -1)
 			return mapping.findForward("appletViewer");
 		
 		String fileName2 = gpserverURL + "gp/jobResults/" + newJobNumber + "/" + gpTask.getResultName() + ".mad.comp.marker.odf";
 		
 		request.setAttribute("comparativeMarkerSelectionDatasetFilename", fileName1);
 		request.setAttribute("comparativeMarkerSelectionFilename", fileName2);
 
 		request.setAttribute(RunVisualizerConstants.DOWNLOAD_FILES, "comparative.marker.selection.filename,dataset.filename");
 
         request.setAttribute("name", "ComparativeMarkerSelectionViewer");
         request.setAttribute("gp_paramNames", "comparative.marker.selection.filename,dataset.filename");
 
         return mapping.findForward("appletViewer");
     }
 	private String appendString(String[] str, String token){
         StringBuffer sb = new StringBuffer();
         int i = 1; //str.length;
         for (String string : str){
         	sb.append(string);
         	if (i++ != str.length){
         		sb.append(token);
         	}
         }
         return sb.toString();
 	}
 	
 	private List<String> getGPTaskList(Collection collection){
 		List<String> jobList = new ArrayList<String>();
 		if (collection != null && !collection.isEmpty()){
 			for(Iterator i = collection.iterator();i.hasNext();)	{
 			
 				GPTask task = (GPTask) i.next();
 				if (task.getStatus().equals(FindingStatus.Completed) && task.getTaskModule() == null)
 					jobList.add(task.getJobId());
 			}
 		}
 		return jobList;
 	}
 	
 	private GPTask getGPTask(Collection tempGpTaskList, String jobNumber){
 		GPTask gpTask = null;
 		if (tempGpTaskList != null && !tempGpTaskList.isEmpty()){
 			for(Iterator i = tempGpTaskList.iterator();i.hasNext();)	{
 				GPTask task = (GPTask) i.next();
 				if (task.getJobId().equals(jobNumber))
 					gpTask = task;
 			}
 		}
 		return gpTask;
 	}
 	private GPTask getGPTask(HttpServletRequest request, String jobNumber){
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		GPTask gpTask = null;
 		if (tempGpTaskList != null && !tempGpTaskList.isEmpty()){
 			for(Iterator i = tempGpTaskList.iterator();i.hasNext();)	{
 				GPTask task = (GPTask) i.next();
 				if (task.getJobId().equals(jobNumber))
 					gpTask = task;
 			}
 		}
 		return gpTask;
 	}
 	
 	private List<LabelValueBean> getVisualizers(){
 		List<LabelValueBean> processList = new ArrayList<LabelValueBean>();
 		processList.add(new LabelValueBean(HMV, HEAT_MAP_VIEW));
 		processList.add(new LabelValueBean(HC, HC_PIPELINE));
 		processList.add(new LabelValueBean(KNN, KNN_PIPELINE));
 		processList.add(new LabelValueBean(CMS, CMS_PIPELINE));
 		return processList;
 	}
 	private void runGenePattern(HttpServletRequest request,
 			String moduleName, Parameter[] par, GPTask gpTask) throws Exception{
 		GPClient gpClient = (GPClient)request.getSession().getAttribute("genePatternServer");
 
 		int nowait = gpClient.runAnalysisNoWait(moduleName, par);
 
 		String tid = String.valueOf(nowait);
 		request.setAttribute("jobId", tid);
 		request.setAttribute("gpStatus", "running");
 		GPTask task = new GPTask(tid, gpTask.getResultName(), FindingStatus.Running);
 		task.setTaskModule(moduleName);
         if (moduleName.equalsIgnoreCase(HC_PIPELINE)){
         	task.setTaskModuleDisplayName(HC);
         }
         else if (moduleName.equalsIgnoreCase(KNN_PIPELINE)){
         	task.setTaskModuleDisplayName(KNN);
         }
         else if (moduleName.equalsIgnoreCase(CMS_PIPELINE)){
         	task.setTaskModuleDisplayName(CMS);
         }
 
 		ApplicationFactory.getPresentationTierCache().addNonPersistableToSessionCache(request.getSession().getId(), "latestGpTask",(Serializable)task); 
 	}
 	private void setFileInformation(HttpServletRequest request, String gpserverURL,
 			String  gp_lsid, String gp_commandLine) throws Exception{
 		String gpUserId = (String)request.getSession().getAttribute("gpUserId");
 
 		String password = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.password");
 		
 	    TaskIntegratorProxy taskIntegratorProxy = new TaskIntegratorProxy(gpserverURL, gpUserId, password, false);
 	    String lsid = System.getProperty(gp_lsid);
         String[] theFiles = taskIntegratorProxy.getSupportFileNames(lsid);
         long[] dateLongs =
         	taskIntegratorProxy.getLastModificationTimes(lsid, theFiles);
         String[] dateStrings = new String[dateLongs.length];
         int index = 0;
         for (long dateLong : dateLongs){
         	dateStrings[index++] = Long.toString(dateLong);
         }
         request.setAttribute(RunVisualizerConstants.SUPPORT_FILE_DATES, appendString(dateStrings, ","));
         request.setAttribute(RunVisualizerConstants.SUPPORT_FILE_NAMES, appendString(theFiles, ","));
         String commandline = System.getProperty(gp_commandLine);
         commandline = StringUtils.htmlEncode(commandline);
         request.setAttribute(RunVisualizerConstants.COMMAND_LINE, commandline);
         request.setAttribute("gp_lsid", lsid);
         String gpHomeURL =  (String)request.getSession().getAttribute("ticketString");
 		int ppp = gpHomeURL.indexOf("?");
 		String ticketString = gpHomeURL.substring(ppp);
 		request.setAttribute("ticketString", ticketString);
 		
 		String supportFileURL = System.getProperty("gov.nih.nci.caintegrator.gpvisualizer.supportFileURL");
 		supportFileURL = gpserverURL + "gp/" + supportFileURL;
         request.setAttribute("supportFileURL", supportFileURL);
         
         request.setAttribute("goApplet", "goApplet");
 	}
     private void processSetUp(GpProcessForm gpForm,
             HttpServletRequest request)
     throws Exception {
     	
 		RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 
     	GPTask gpTask = (GPTask)_cacheManager.getNonPersistableObjectFromSessionCache(
     			request.getSession().getId(), "latestGpTask");
     	if (gpTask != null){
     		request.setAttribute("jobId", gpTask.getJobId());
     		request.setAttribute("resultName", gpTask.getResultName());
 			request.setAttribute("jobIdSelect", gpTask.getJobId() + "_jobId");
 			request.setAttribute("processSelect", gpTask.getJobId() + "_process");
 			request.setAttribute("submitButton", gpTask.getJobId() + "_submit");
 			request.setAttribute("gpStatus", gpTask.getStatus().toString());
 			if (gpTask.getTaskModule() != null){
 				request.setAttribute("taskModule", gpTask.getTaskModule());
 			}
     	}
 		
 		Collection tempGpTaskList = _cacheManager.getAllSessionGPTasks(request.getSession().getId());
 		gpForm.setJobList(getGPTaskList(tempGpTaskList));
 		gpForm.setProcessList(getVisualizers());
     }
     private int findChildJobNumber(HttpServletRequest request, int parentJobNumber, String child_lsid){
     	AnalysisWebServiceProxy analysisProxy = (AnalysisWebServiceProxy)request.getSession().getAttribute("AnalysisWebServiceProxy");
     	if (analysisProxy == null){
 			return -1;
 		}
     	GPClient gpClient = (GPClient)request.getSession().getAttribute("genePatternServer");
     	try {
     		int[] children = analysisProxy.getChildren(parentJobNumber);
     		JobResult result = null;
     		for (int job : children){
     			result = gpClient.createJobResult(job);
     			if (result.getLSID().equalsIgnoreCase(child_lsid)){
     				return result.getJobNumber();
     			}
     		}
     	} catch (WebServiceException we){
     		logger.error("Failed to obtain child job number: " + we.getMessage());
     	}
 		return -1;
     }
 }
 
