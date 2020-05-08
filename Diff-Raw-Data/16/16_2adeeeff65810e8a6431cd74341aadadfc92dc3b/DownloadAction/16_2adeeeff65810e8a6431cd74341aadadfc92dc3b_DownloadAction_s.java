 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caarray.domain.file.FileType;
 import gov.nih.nci.caarray.services.ServerConnectionException;
 import gov.nih.nci.caintegrator.application.download.caarray.CaArrayFileDownloadManager;
 import gov.nih.nci.caintegrator.application.lists.ListItem;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.application.zip.FileNameGenerator;
 import gov.nih.nci.caintegrator.dto.critieria.Constants;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.rembrandt.download.caarray.RembrandtCaArrayFileDownloadManager;
 import gov.nih.nci.rembrandt.dto.lookup.DownloadFileLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.struts.form.DownloadForm;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.login.LoginException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 import org.apache.struts.util.LabelValueBean;
 
 public class DownloadAction extends DispatchAction {
 	private static Logger logger = Logger.getLogger(RefineQueryAction.class);
 	private CaArrayFileDownloadManager rbtCaArrayFileDownloadManager;
 	public ActionForward setup(
 			ActionMapping mapping,
 			ActionForm form,
 			HttpServletRequest request,
 			HttpServletResponse response)
 			throws Exception {
 		
 		//prepopulate the fields for BRB downloads
 		List fileList = LookupManager.getDownloadBRBFileList();
 		if (fileList == null || fileList.isEmpty())	{
 			request.setAttribute("downloadFileList", new ArrayList());
 			return mapping.findForward("success");
 		}
 		List<DownloadFileLookup> downloadFileList = new ArrayList<DownloadFileLookup>();
 		
 		Collection<InstitutionDE> collection = InsitutionAccessHelper.getInsititutionCollection(request.getSession());
 
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
 		request.getSession().setAttribute("downloadFileList", downloadFileList);
 		
 		//prepopulate the fields for caArray
 		GroupRetriever groupRetriever = new GroupRetriever();
 		List<LabelValueBean> al = new ArrayList<LabelValueBean>();
 		al.addAll(groupRetriever.getClinicalGroupsCollectionNoPath(request.getSession()));
 		//specifically remove only these values, not to effect the groupRetriever
 //		LabelValueBean tmp = new LabelValueBean("UNKNOWN", "UNKNOWN");
 //		al.remove(tmp);
 //		tmp = new LabelValueBean("ALL", "ALL");
 //		al.remove(tmp);
 //		tmp = new LabelValueBean("NON_TUMOR", "NON_TUMOR");
 //		al.remove(tmp);
 		request.getSession().setAttribute("sampleGroupsList", al);
 		
 		return  mapping.findForward("success");
 	}
 
 	public ActionForward caarray(
 			ActionMapping mapping,
 			ActionForm form,
 			HttpServletRequest request,
 			HttpServletResponse response)
 			throws Exception {
 
 		try {
 			rbtCaArrayFileDownloadManager = RembrandtCaArrayFileDownloadManager.getInstance();
		} catch (MalformedURLException e) {
 	        logger.error(new IllegalStateException("caArray URL error" ));
 			logger.error(e.getMessage());
 			logger.error(e);
		} catch (LoginException e) {
	        logger.error(new IllegalStateException("caArray username/pwd error" ));
			logger.error(e.getMessage());
			logger.error(e);
		} catch (ServerConnectionException e) {
	        logger.error(new IllegalStateException("caArray server connecrtion error" ));
			logger.error(e.getMessage());
			logger.error(e);
		}
 		//Necessary for cache clean up
		request.getSession().setAttribute(CaArrayFileDownloadManager.ZIP_FILE_PATH,RembrandtCaArrayFileDownloadManager.getInstance().getOutputZipDirectory());
 		DownloadForm dlForm = (DownloadForm)form;
 		String groupNameList = dlForm.getGroupNameCompare();
 
 		UserListBeanHelper helper = new UserListBeanHelper(request.getSession().getId());
 		Set<String> patientIdset = new HashSet<String>();
   
 		List<ListItem> listItemts = helper.getUserList(groupNameList).getListItems();            	
 		List<String> specimenNames = null;
 		for (ListItem item : listItemts) {
 			patientIdset.add(item.getName());
 		}               
         //Get InsitutionAccess
         Collection<InstitutionDE> accessInstitutions = InsitutionAccessHelper.getInsititutionCollection(request.getSession());
 		if(patientIdset != null && patientIdset.size()>0) {  
 			// need to convert pt dids to the specimen ids
 			specimenNames = LookupManager.getSpecimenNames(patientIdset, accessInstitutions);     
 		}// end of if
 		String tempName = groupNameList.toLowerCase();
 		
 		tempName = FileNameGenerator.generateUniqueFileName(tempName,dlForm.getFileType(),dlForm.getArrayPlatform());
 		String taskId = tempName ;
 		FileType type = null;
 		String experimentName = null;
 		if (dlForm.getFileType().equals("CEL"))
 			type = FileType.AFFYMETRIX_CEL;
 		else if (dlForm.getFileType().equals("CHP"))
 			type = FileType.AFFYMETRIX_CHP;
 
 		if (dlForm.getArrayPlatform().equals(Constants.AFFY_OLIGO_PLATFORM)){
 			experimentName = System.getProperty(RembrandtCaArrayFileDownloadManager.GE_EXPERIMENT_NAME);
 		}
 		else if (dlForm.getArrayPlatform().equals(Constants.AFFY_100K_SNP_ARRAY)){
 			experimentName = System.getProperty(RembrandtCaArrayFileDownloadManager.CN_EXPERIMENT_NAME);
 		}
 		experimentName = experimentName.trim();
 		rbtCaArrayFileDownloadManager.executeDownloadStrategy(
 				request.getSession().getId(), 
 				taskId,
 				tempName + ".zip", 
 				specimenNames, 
 				type,
 				experimentName);
 
 		return  mapping.findForward("success");
 	}
 	public ActionForward download(
 			ActionMapping mapping,
 			ActionForm form,
 			HttpServletRequest request,
 			HttpServletResponse response)
 			throws Exception {
 		String dir = System.getProperty(RembrandtCaArrayFileDownloadManager.OUTPUT_ZIP_DIR);
 		String fileName = request.getParameter("file");
 		String filePath = dir + "/"+ fileName;
 		final ServletOutputStream out = response.getOutputStream(); 
 		response.setContentType("application/octet-stream");
 		
 		response.setHeader( "Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
 		File file = null;
 		try {
 			file = new File(filePath);
 			response.setContentLength( (int)file.length() );
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
 }
