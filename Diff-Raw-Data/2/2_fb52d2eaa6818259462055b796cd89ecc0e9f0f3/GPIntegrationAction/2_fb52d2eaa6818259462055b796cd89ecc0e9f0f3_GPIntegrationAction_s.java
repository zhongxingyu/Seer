 package gov.nih.nci.rembrandt.web.struts.action;
 
 import gov.nih.nci.caintegrator.analysis.messaging.IdGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.application.analysis.gp.GenePatternPublicUserPool;
 import gov.nih.nci.caintegrator.application.lists.ListItem;
 
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.security.EncryptionUtil;
 import gov.nih.nci.caintegrator.security.PublicUserPool;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 import gov.nih.nci.caintegrator.util.idmapping.IdMapper;
 import gov.nih.nci.caintegrator.service.task.GPTask;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.struts.form.GpIntegrationForm;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 
 
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.io.Serializable;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 import org.genepattern.client.GPServer;
 import org.genepattern.webservice.Parameter;
 
 public class GPIntegrationAction extends DispatchAction {
 	
 	  private IdMapper idMappingManager;
 	  
 	
 	private static Logger logger = Logger.getLogger(GPIntegrationAction.class);
     private Collection<GeneIdentifierDE> geneIdentifierDECollection;
 
     private String gpPoolString = ":GP30:RBT";
     public ActionForward setup(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
     throws Exception {
     	GpIntegrationForm gpForm = (GpIntegrationForm) form;
         /*setup the defined Disease query names and the list of samples selected from a Resultset*/
         GroupRetriever groupRetriever = new GroupRetriever();
         gpForm.setExistingGroupsList(groupRetriever.getClinicalGroupsCollection(request.getSession()));         
     
         return mapping.findForward("success");
     }
     
     /**
      * Method submittal
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
 	public ActionForward submit(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
     	
     	    List<String> idStringList = new ArrayList<String>();
     	    List<List<String>> allStringList = new ArrayList<List<String>>();
             List<String> fileNameList = new ArrayList<String>();
             List<String> reportIdStringList = new ArrayList<String>();
 
          
          
     	   GpIntegrationForm gpForm = (GpIntegrationForm) form;
     	   String sessionId = request.getSession().getId();
            HttpSession session = request.getSession();
            
        	   String[] patientGroups = gpForm.getSelectedGroups();
        	   
        	   //create a sampleGroup array with the length of selected  patient groups, it needs to be >2
        	   // SampleGroup objects will be used to store patient group names and specimen ids
        	   SampleGroup[] sampleGroup = new SampleGroup [patientGroups.length];            
        	   
        	   
        	   
     	
            UserListBeanHelper helper = new UserListBeanHelper(request.getSession().getId());
            Set<String> patientIdset = new HashSet<String>();
            
          	   for(int j=0; j<patientGroups.length;j++) {
         	   Set<String> idSet = null;        	   
         	   
         	   String[] uiDropdownString =patientGroups[j].split("#");
         	   
                String myClassName = uiDropdownString[0];           
                String myValueName = uiDropdownString[1];              
                sampleGroup[j] = new SampleGroup();  
                
                // add the selected pt group names such "GBM", "MIXED" to the sampleGroup object
                sampleGroup[j].setGroupName(myValueName);
                patientIdset.clear();
                
                List<ListItem> listItemts = helper.getUserList(myValueName).getListItems();            	
                
                for (Iterator i = listItemts.iterator(); i.hasNext(); ) {
    				ListItem item = (ListItem)i.next();
    				String id = item.getName();
    				patientIdset.add(id);
    				
    			  }               
             
             	
            
            if(patientIdset != null && patientIdset.size()>0) {
         	   
         	   // need to convert pt dids to the specimen ids
         		List<String> specimenNames = LookupManager.getSpecimenNames(patientIdset);        	
         		if(specimenNames != null){
         			   for (Iterator i = specimenNames.iterator(); i.hasNext(); ) {
         				   String sampleid  = (String)i.next();     
         				   // add speicmen ids to the samplegroup with the corresponding selected pt group
         				   sampleGroup[j].add(sampleid);	        	   			   
         		   			
         		          }// end of for
                     }// end of if
         		
         		idStringList.add(getIdsAsDelimitedString(sampleGroup[j], "\t"));
         		
               }	
         		
            
            }
           
           
         
            allStringList.add(idStringList);
    		   fileNameList.add("labIdsFile");
    	
 			reportIdStringList.add("reporter=NONE");
 			allStringList.add(reportIdStringList);
 			fileNameList.add("reproterIdsFile");
 			logger.info("Have no gene or report list...");
 	
    		  
    		  
    		
    		  
    	
 		String platformName = gpForm.getArrayPlatform();
 	
 	
 		//Now let's write them to files
 		List<String> filePathList = new ArrayList<String>();
 		writeGPFile(filePathList, allStringList, fileNameList);
 		
 		
 		
 	
 		//Now get the R-binary file name:
 		
 		String r_fileName = null;
 		String a_fileName = null;
 		
 	
        if(platformName != null && platformName.equalsIgnoreCase(ArrayPlatformType.AFFY_OLIGO_PLATFORM.toString())) {
        
 			r_fileName = System.getProperty("gov.nih.nci.rembrandt.affy_data_matrix");
 			a_fileName = System.getProperty("gov.nih.nci.rembrandt.affy_data_annotation");		
 		 
 	 }
 	
 		
 //		*** RUN TASK ON THE GP SERVER
 		String tid = "209";
 		String gpModule =  System.getProperty("gov.nih.nci.caintegrator.gp.modulename");						
 		
 		
 		String gpserverURL = System.getProperty("gov.nih.nci.caintegrator.gp.server")!=null ? 
 				(String)System.getProperty("gov.nih.nci.caintegrator.gp.server") : "localhost:8080"; //default to localhost
 				try {
 					//*	
 					
 				 	   UserCredentials credentials = (UserCredentials)request.getSession().getAttribute(RembrandtConstants.USER_CREDENTIALS);
 		        			       
 				
 						String rembrandtUser = null;
 						
 						String analysisResultName = gpForm.getAnalysisResultName();
 
 						
 						if(credentials!= null) {
 							rembrandtUser= credentials.getUserName();
 						}
 							     
 							String publicUser = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.name");
 							String password = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.password");
 							
 						if(rembrandtUser==null)	{
 							rembrandtUser=publicUser;
 						}
 						
 							//Check to see the user is already created otherwise create one.
 						GPServer gpServer = null;
 						if (rembrandtUser.equals(publicUser)){
 							String gpUser = (String)session.getAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME);
 							if (gpUser == null){
 								PublicUserPool pool = GenePatternPublicUserPool.getInstance();
 								gpUser = pool.borrowPublicUser();
 								session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME, gpUser);
 								session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_POOL, pool);
 							}
 							rembrandtUser = gpUser;
 						}
 						
 						String encryptKey = System.getProperty("gov.nih.nci.caintegrator.gp.desencrypter.key");
 						String urlString = EncryptionUtil.encrypt(rembrandtUser+ gpPoolString, encryptKey);
 						urlString = URLEncoder.encode(urlString, "UTF-8");
 						String ticketString = gpserverURL+"gp?ticket="+ urlString;
 						
 						logger.info(ticketString);
 						URL url;
 			            try {
 			            	url = new java.net.URL(ticketString);
 			            	URLConnection conn = url.openConnection();
 			            	final int size = conn.getContentLength();
 			            	logger.info(Integer.toString(size));
 
 			            } catch (Exception e) {
 			            	logger.error(e.getMessage());
 			            }
 			            
 			            
 						gpServer = new GPServer(gpserverURL, rembrandtUser, password);
 						int size = filePathList.size();
 						Parameter[] par = new Parameter[filePathList.size() + 3 + 3];
 						int currpos= 1;
 						for (int i = 0; i < filePathList.size(); i++){
 							par[i] = new Parameter("input.filename" + currpos++, filePathList.get(i));
 						}
 						par[--currpos] = new Parameter("project.name", "rembrandt");
 
 						//r_fileName = "'/usr/local/genepattern/resources/DataMatrix_ISPY_306cDNA_17May07.Rda'";
 						par[++currpos] = new Parameter("array.filename", r_fileName);
 						par[++currpos] = new Parameter("annotation.filename", a_fileName);
 							
 						par[++currpos] = new Parameter("analysis.name", analysisResultName);
 
 						//always just 2
 						par[++currpos] = new Parameter("output.cls.file",analysisResultName+".cls");
 						par[++currpos] = new Parameter("output.gct.file",analysisResultName+".gct");
 						
 						//JobResult preprocess = gpServer.runAnalysis(gpModule, par);
 						int nowait = gpServer.runAnalysisNoWait(gpModule, par);
 
 						tid = String.valueOf(nowait);
 						//LSID = urn:lsid:8080.root.localhost:genepatternmodules:20:2.1.7
 						request.setAttribute("jobId", tid);
 						request.setAttribute("gpStatus", "running");
 						session.setAttribute("genePatternServer", gpServer);
						//request.setAttribute("genePatternURL", ticketString);
 						request.getSession().setAttribute("gptid", tid);
 						request.getSession().setAttribute("gpUserId", rembrandtUser);
 						request.getSession().setAttribute("ticketString", ticketString);
 						GPTask gpTask = new GPTask(tid, analysisResultName, FindingStatus.Running);
 						RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 						_cacheManager.addNonPersistableToSessionCache(request.getSession().getId(), "latestGpTask",(Serializable) gpTask); 
 						
 					} catch (Exception e) {
 						StringWriter sw = new StringWriter();
 						PrintWriter pw = new PrintWriter(sw);
 						e.printStackTrace(pw);
 						logger.error(sw.toString());
 						logger.error(gpModule + " failed...." + e.getMessage());
 						throw new Exception(e.getMessage());
 					}
 					return mapping.findForward("viewJob");
 
 					
    		
 
     } 
     
    
 		
 	private String getIdsAsDelimitedString(String listName, Set<String> idSet,	String token){
 				StringBuffer sb = null;
 				
 				sb = new StringBuffer(replaceSpace(listName) + "=");
 				int size = idSet.size();
 				int count = 0;
 				for (String id : idSet) {
 					sb.append(id);
 					if (++count < size) {
 						sb.append(token);
 					}
 				}
 				if (sb.length() == 0)
 					return "";
 				return sb.toString();
 		    }
 		    
     
 	 private String getIdsAsDelimitedString(IdGroup idGroup, String token){
 			StringBuffer sb = new StringBuffer(replaceSpace(idGroup.getGroupName()) + "=");
 			for (Iterator i = idGroup.iterator(); i.hasNext(); ) {
 			  //sb.append(DOUBLE_QUOTE+(String)i.next()+DOUBLE_QUOTE);
 			  sb.append((String)i.next());
 			  if (i.hasNext()) {
 			    sb.append(token);
 			  }
 			}
 			if (sb.length() == 0)
 				return "";
 			return sb.toString();
 	    }
 	 
     private String replaceSpace(String text){
     	return text.replaceAll(" ", "_");
     }
 
     private void writeGPFile(List<String> filePathList, 
     		List<List<String>> allIdStringList,
     		List<String> fileNameList)throws IOException{
     	int count = 0; 
     	String fileName = null;
     	String fileExtension = ".txt";
 		for (List<String> list : allIdStringList){
 			if (!list.isEmpty()){
 				fileName = fileNameList.get(count);	
 				// this is used to view the file locally
 				//File idFile =File.createTempFile(fileName, fileExtension, new File("C:\\temp\\rembrandt"));
 				File idFile =File.createTempFile(fileName, fileExtension);
 				FileWriter idFw = new FileWriter(idFile);
 				for (String ids : list){
 					idFw.write(ids);
 					idFw.write("\n");
 				}
 				idFw.close();
 				filePathList.add(idFile.getAbsolutePath());
 			}
 			else
 				filePathList.add("");
 			count++;
 		}
     }
 
 
     
 
 }
