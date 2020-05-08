 /*L
  * Copyright (c) 2006 SAIC, SAIC-F.
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/rembrandt/LICENSE.txt for details.
  */
 
 package gov.nih.nci.rembrandt.web.struts2.action;
 
 import gov.nih.nci.caarray.domain.file.FileType;
 import gov.nih.nci.caarray.services.ServerConnectionException;
 import gov.nih.nci.caintegrator.application.download.caarray.CaArrayFileDownloadManager;
 import gov.nih.nci.caintegrator.application.download.caarray.CaArrayFileDownloadManagerInterface;
 import gov.nih.nci.caintegrator.application.lists.ListItem;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.application.zip.FileNameGenerator;
 import gov.nih.nci.caintegrator.dto.critieria.Constants;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.rembrandt.download.caarray.RembrandtCaArrayFileDownloadManager;
 import gov.nih.nci.rembrandt.dto.lookup.DownloadFileLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.struts2.form.DownloadForm;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Future;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.util.LabelValueBean;
 import org.apache.struts2.interceptor.ServletRequestAware;
 import org.apache.struts2.interceptor.ServletResponseAware;
 import org.apache.struts2.interceptor.SessionAware;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class DownloadAction extends ActionSupport 
 	implements SessionAware, ServletRequestAware, ServletResponseAware {
 	private static Logger logger = Logger.getLogger(RefineQueryAction.class);
 	private CaArrayFileDownloadManagerInterface rbtCaArrayFileDownloadManagerInterface;
 	
 	Map<String, Object> sessionMap;
 	HttpServletRequest servletRequest;
 	HttpServletResponse servletResponse;
 	
 	DownloadForm downloadForm;
 	String file;
 	
 	//List<DownloadFileLookup> fileList;
 	List<DownloadFileLookup> downloadFileList;// = new ArrayList<DownloadFileLookup>();
 	List<gov.nih.nci.rembrandt.web.bean.LabelValueBean> sampleGroupsList;
 	List<LabelValueBean> arrayPlatformList;
 	List<String> fileTypeList;
 	List<String> brbFormatList;
 	
 	protected void setLookups() {
 		GroupRetriever groupRetriever = new GroupRetriever();
 		this.sampleGroupsList = new ArrayList<gov.nih.nci.rembrandt.web.bean.LabelValueBean>();
		List<gov.nih.nci.rembrandt.web.bean.LabelValueBean> cgCollections = groupRetriever.getClinicalGroupsCollectionNoPath(this.servletRequest.getSession());
 		sampleGroupsList.addAll(groupRetriever.getClinicalGroupsCollectionNoPath(this.servletRequest.getSession()));
 		
 		this.arrayPlatformList = new ArrayList<LabelValueBean>();
 		this.arrayPlatformList.add(new LabelValueBean("Oligo (Affymetrix U133 Plus 2.0)", Constants.AFFY_OLIGO_PLATFORM));
 		this.arrayPlatformList.add(new LabelValueBean("Affymetrix 100K SNP Array", Constants.AFFY_100K_SNP_ARRAY));
 		 
 		this.fileTypeList = new ArrayList<String>();
 		this.fileTypeList.add("CEL");
 		this.fileTypeList.add("CHP");
 		
 		this.brbFormatList = new ArrayList<String>();
 		this.brbFormatList.add("BRB Format");
 	}
 
 	public String setup() 
 			throws Exception {
 		
 		this.sessionMap.put("currentPage", "0");
 		//prepopulate the fields for BRB downloads
 		List<DownloadFileLookup> fileList = LookupManager.getDownloadFileList(null);
 		
 		if (fileList == null || fileList.isEmpty())	{
			this.servletRequest.setAttribute("downloadFileList", new ArrayList<DownloadFileLookup>());
 			return "success";
 		}
 		
		downloadFileList = new ArrayList<DownloadFileLookup>();
		
 		Collection<InstitutionDE> collection = InsitutionAccessHelper.getInsititutionCollection(this.servletRequest.getSession());
 
 		for (int i = 0; i < fileList.size(); i++){
 			DownloadFileLookup lookup = (DownloadFileLookup)fileList.get(i);
 			if (lookup.getAccessCode().equals(new Long(8))){
 				downloadFileList.add(lookup);
 				continue;
 			}
 			for (Iterator it = collection.iterator(); it.hasNext();){
 				InstitutionDE de = (InstitutionDE)it.next();
 			
 				if (lookup.getAccessCode().equals((Long)de.getValue())){
 					downloadFileList.add(lookup);
 				}
 			}
 		}
 		
 		this.sessionMap.put("downloadFileList", downloadFileList);
 		
 		setLookups();
 
 		return  "success";
 	}
 
 	public String caarray()
 			throws Exception {
         //if (!isTokenValid(request)) {
 		//	return mapping.findForward("failure");
 		//}
 		setLookups();
 		this.downloadFileList = (List<DownloadFileLookup>)this.sessionMap.get("downloadFileList");
 		
 
 		try {
 			rbtCaArrayFileDownloadManagerInterface = ApplicationContext.getCaArrayFileDownloadManagerInterface();
 		} catch (Exception e) {
 	        logger.error(new IllegalStateException("caArray URL error" ));
 			logger.error(e.getMessage());
 			logger.error(e);
 		} 
 		//Necessary for cache clean up
 		this.sessionMap.put(CaArrayFileDownloadManagerInterface.ZIP_FILE_PATH,
 				rbtCaArrayFileDownloadManagerInterface.getOutputZipDirectory());
 		
 		DownloadForm dlForm = (DownloadForm)this.downloadForm;
 		String groupNameList = dlForm.getGroupNameCompare();
 
 		UserListBeanHelper helper = new UserListBeanHelper(this.servletRequest.getSession().getId());
 		Set<String> patientIdset = new HashSet<String>();
   
 		List<ListItem> listItemts = helper.getUserList(groupNameList).getListItems();            	
 		List<String> specimenNames = null;
 		for (ListItem item : listItemts) {
 			patientIdset.add(item.getName());
 		}               
         //Get InsitutionAccess
         Collection<InstitutionDE> accessInstitutions = InsitutionAccessHelper.getInsititutionCollection(this.servletRequest.getSession());
 		if(patientIdset != null && patientIdset.size()>0) {  
 			// need to convert pt dids to the specimen ids
 			specimenNames = LookupManager.getSpecimenNames(patientIdset, accessInstitutions);     
 		}// end of if
 		
 		//Non tumor sample hack
 		List<String> removeFromList = new ArrayList<String>();
 		List<String> addToList = new ArrayList<String>();		
 		for(String specimenName : specimenNames){
 				if(specimenName.equals("NT1_T")){
 					addToList.add("Normal_1");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT2_T")){
 					addToList.add("Normal_2");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT3_T")){
 					addToList.add("Normal_3");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT4_T")){
 					addToList.add("Normal_4");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT5_T")){
 					addToList.add("Normal_5");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT6_T")){
 					addToList.add("Normal_6");
 					removeFromList.add(specimenName);
 				}
 				if(specimenName.equals("NT7_T")){
 					addToList.add("Normal_7");
 					removeFromList.add(specimenName);
 				}				
 			}
 		specimenNames.removeAll(removeFromList);
 		specimenNames.addAll(addToList);
 		//
 		String tempName = groupNameList.toLowerCase();
 		
 		tempName = FileNameGenerator.generateUniqueFileName(tempName,dlForm.getFileType(),dlForm.getArrayPlatform());
 		String taskId = tempName ;
 		FileType type = null;
 		String experimentName = null;
 		if (dlForm.getFileType().equals("CEL"))
 		{	
 //			type = FileType.AFFYMETRIX_CEL;
 			type = new FileType();
 			type.setName("AFFYMETRIX_CEL");
 		}
 		else if (dlForm.getFileType().equals("CHP"))
 		{
 //			type = FileType.AFFYMETRIX_CHP;
 			type = new FileType();
 			type.setName("AFFYMETRIX_CHP");
 		}
 
 		if (dlForm.getArrayPlatform().equals(Constants.AFFY_OLIGO_PLATFORM)){
 			experimentName = System.getProperty(RembrandtCaArrayFileDownloadManager.GE_EXPERIMENT_NAME);
 		}
 		else if (dlForm.getArrayPlatform().equals(Constants.AFFY_100K_SNP_ARRAY)){
 			experimentName = System.getProperty(RembrandtCaArrayFileDownloadManager.CN_EXPERIMENT_NAME);
 		}
 		experimentName = experimentName.trim();
 		Future<?> future = rbtCaArrayFileDownloadManagerInterface.executeDownloadStrategy(
 				this.servletRequest.getSession().getId(), 
 				taskId,
 				tempName + ".zip", 
 				specimenNames, 
 				type,
 				experimentName);
 
         Map<String,Future<?>> futureTaskMap = (Map<String,Future<?>>) this.servletRequest.getSession().getAttribute("FutureTaskMap");
         if(futureTaskMap == null){
        	 futureTaskMap = new HashMap<String,Future<?>>();
         }
         futureTaskMap.put(taskId,future);
         
         this.sessionMap.put("FutureTaskMap",futureTaskMap);
         //resetToken(request);
         
        // saveToken(request);
 		return  "success";
 	}
 	public String download()
 			throws Exception {
 		String dir = System.getProperty(RembrandtCaArrayFileDownloadManager.OUTPUT_ZIP_DIR);
 		String fileName = this.servletRequest.getParameter("file");
 		String filePath = dir + "/"+ fileName;
 		
 		final ServletOutputStream out = servletResponse.getOutputStream(); 
 		servletResponse.setContentType("application/octet-stream");
 		
 		servletResponse.setHeader( "Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
 		File file = null;
 		try {
 			file = new File(filePath);
 			servletResponse.setContentLength( (int)file.length() );
 			BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
 			byte[] buf = new byte[4 * 1024]; // 4K buffer
 			int bytesRead;
 			while ((bytesRead = is.read(buf)) != -1) {
 				out.write(buf, 0, bytesRead);
 			}
 			is.close();
 			out.close();
 		}catch (IOException ioe){
 			logger.error("IO exception in sending file " +  file.toString() + ioe.getMessage());
 		}
 
 		return  null;
 	}
 
 	@Override
 	public void setServletRequest(HttpServletRequest arg0) {
 		servletRequest = arg0;
 		
 	}
 
 	@Override
 	public void setSession(Map<String, Object> arg0) {
 		sessionMap = arg0;
 		
 	}
 
 	@Override
 	public void setServletResponse(HttpServletResponse arg0) {
 		servletResponse = arg0;
 		
 	}
 
 	public DownloadForm getDownloadForm() {
 		return downloadForm;
 	}
 
 	public void setDownloadForm(DownloadForm downloadForm) {
 		this.downloadForm = downloadForm;
 	}
 
 	public List<DownloadFileLookup> getDownloadFileList() {
 		return downloadFileList;
 	}
 
 	public void setDownloadFileList(List<DownloadFileLookup> downloadFileList) {
 		this.downloadFileList = downloadFileList;
 	}
 
 //	public List<DownloadFileLookup> getFileList() {
 //		return fileList;
 //	}
 //
 //	public void setFileList(List<DownloadFileLookup> fileList) {
 //		this.fileList = fileList;
 //	}
 
 	public List<gov.nih.nci.rembrandt.web.bean.LabelValueBean> getSampleGroupsList() {
 		return sampleGroupsList;
 	}
 
 	public void setSampleGroupsList(
 			List<gov.nih.nci.rembrandt.web.bean.LabelValueBean> sampleGroupsList) {
 		this.sampleGroupsList = sampleGroupsList;
 	}
 
 	public List<LabelValueBean> getArrayPlatformList() {
 		return arrayPlatformList;
 	}
 
 	public void setArrayPlatformList(List<LabelValueBean> arrayPlatformList) {
 		this.arrayPlatformList = arrayPlatformList;
 	}
 
 	public List<String> getFileTypeList() {
 		return fileTypeList;
 	}
 
 	public void setFileTypeList(List<String> fileTypeList) {
 		this.fileTypeList = fileTypeList;
 	}
 
 	public List<String> getBrbFormatList() {
 		return brbFormatList;
 	}
 
 	public void setBrbFormatList(List<String> brbFormatList) {
 		this.brbFormatList = brbFormatList;
 	}
 
 	public String getFile() {
 		return file;
 	}
 
 	public void setFile(String file) {
 		this.file = file;
 	}
 	
 	
 }
