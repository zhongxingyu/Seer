 /**
  * Copyright Notice.  Copyright 2008  Scenpro, Inc (caBIG Participant). caXchange
  * was created with NCI funding and is part of the caBIG initiative. 
  * The software subject to this notice and license includes both human readable source code form and 
  * machine readable, binary, object code form (the caBIG Software).
  * This caBIG Software License (the License) is between caBIG Participant and You.  
  * You (or Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity.  Control for purposes of this 
  * definition means (i) the direct or indirect power to cause the direction or management of such entity, 
  * whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity.  
  * License.  Provided that You agree to the conditions described below, caBIG Participant grants 
  * You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and 
  * royalty-free right and license in its rights in the caBIG Software, including any copyright or patent rights therein, to 
  * (i) use, install, disclose, access, operate, execute, reproduce, copy, modify, translate, market, publicly display, 
  * publicly perform, and prepare derivative works of the caBIG Software in any manner and for any purpose, and to have 
  * or permit others to do so; (ii) make, have made, use, practice, sell, and offer for sale, import, and/or otherwise 
  * dispose of caBIG Software (or portions thereof); (iii) distribute and have distributed to and by third parties the 
  * caBIG Software and any modifications and derivative works thereof; and (iv) sublicense the foregoing rights 
  * set out in (i), (ii) and (iii) to third parties, including the right to license such rights to further third parties.  
  * For sake of clarity, and not by way of limitation, caBIG Participant shall have no right of accounting or right of payment
  *  from You or Your sublicensees for the rights granted under this License.  This License is granted at no charge to You.  
  *  Your downloading, copying, modifying, displaying, distributing or use of caBIG Software constitutes acceptance of all 
  *  of the terms and conditions of this Agreement.  If you do not agree to such terms and conditions, you have no right to 
  *  download, copy, modify, display, distribute or use the caBIG Software.  
  * 1.	Your redistributions of the source code for the caBIG Software must retain the above copyright notice, 
  * 		this list of conditions and the disclaimer and limitation of liability of Article 6 below.  
  * 		Your redistributions in object code form must reproduce the above copyright notice, this list of conditions and 
  * 		the disclaimer of Article 6 in the documentation and/or other materials provided with the distribution, if any.
  * 2.	Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: 
  * 		This product includes software developed by Scenpro, Inc.  
  * 		If You do not include such end-user documentation, You shall include this acknowledgment in the caBIG Software itself, 
  * 		wherever such third-party acknowledgments normally appear.
  * 3.	You may not use the names  Scenpro, Inc, 
  * 		The National Cancer Institute, NCI, Cancer Bioinformatics Grid or caBIG to endorse or promote products 
  * 		derived from this caBIG Software.  This License does not authorize You to use any trademarks, service marks, trade names,
  * 		logos or product names of either caBIG Participant, NCI or caBIG, except as required to comply with the terms of this 
  * 		License.
  * 4.	For sake of clarity, and not by way of limitation, You may incorporate this caBIG Software into Your proprietary 
  * 		programs and into any third party proprietary programs.  However, if You incorporate the caBIG Software into third party 
  * 		proprietary programs, You agree that You are solely responsible for obtaining any permission from such third parties 
  * 		required to incorporate the caBIG Software into such third party proprietary programs and for informing Your sublicensees, 
  * 		including without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
  * 		before incorporating the caBIG Software into such third party proprietary software programs.  In the event that You fail to 
  * 		obtain such permissions, You agree to indemnify caBIG Participant for any claims against caBIG Participant by such third 
  * 		parties, except to the extent prohibited by law, resulting from Your failure to obtain such permissions.
  * 5.	For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
  * 		to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses of 
  * 		modifications of the caBIG Software, or any derivative works of the caBIG Software as a whole, provided Your use, reproduction, 
  * 		and distribution of the Work otherwise complies with the conditions stated in this License.
  * 6.	THIS caBIG SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES (INCLUDING, BUT NOT LIMITED TO, 
  * 		THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  
  * 		IN NO EVENT SHALL THE Scenpro, Inc OR ITS AFFILIATES 
  * 		BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * 		PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
  * 		ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
  * 		OUT OF THE USE OF THIS caBIG SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  **/
 package gov.nih.nci.caxchange.ctom.viewer.actions;
 
 import gov.nih.nci.cabig.ccts.domain.Documentation;
 import gov.nih.nci.cabig.ccts.domain.LabResult;
 import gov.nih.nci.cabig.ccts.domain.Participant;
 import gov.nih.nci.cabig.ccts.domain.PerformedActivity;
 import gov.nih.nci.cabig.ccts.domain.PerformedStudy;
 import gov.nih.nci.cabig.ccts.domain.StudySubject;
 import gov.nih.nci.cabig.ccts.domain.LoadLabsRequest;
 
 import gov.nih.nci.cagrid.caxchange.client.CaXchangeRequestProcessorClient;
 import gov.nih.nci.cagrid.caxchange.context.client.CaXchangeResponseServiceClient;
 import gov.nih.nci.cagrid.caxchange.context.stubs.types.CaXchangeResponseServiceReference;
 //import gov.nih.nci.cagrid.common.Utils;
 
 import gov.nih.nci.caxchange.Credentials;
 import gov.nih.nci.caxchange.Message;
 import gov.nih.nci.caxchange.MessagePayload;
 import gov.nih.nci.caxchange.MessageTypes;
 import gov.nih.nci.caxchange.Metadata;
 import gov.nih.nci.caxchange.Request;
 import gov.nih.nci.caxchange.Response;
 import gov.nih.nci.caxchange.ResponseMessage;
 import gov.nih.nci.caxchange.Statuses;
 import gov.nih.nci.caxchange.TargetResponseMessage;
 
 import gov.nih.nci.caxchange.ctom.viewer.beans.LabViewerStatus;
 import gov.nih.nci.caxchange.ctom.viewer.beans.util.HibernateUtil;
 import gov.nih.nci.caxchange.ctom.viewer.constants.DisplayConstants;
 import gov.nih.nci.caxchange.ctom.viewer.constants.ForwardConstants;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LabActivitiesSearchResultForm;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LoginForm;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.LabActivityResult;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.SearchResult;
 
 import gov.nih.nci.labhub.domain.II;
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 //import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.namespace.QName;
 
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.types.URI;
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.hibernate.Session;
 
 /**
  * This class performs the Load to caAERS action. It loads the selected form data to caAERS.
  * It checks if valid login information is in session; if not it redirects the user to login page.
  * 
  * @author asharma
  *
  */
 public class LoadTocaAERSAction extends Action
 {
 	private static final Logger logDB = Logger.getLogger(LoadTocaAERSAction.class);
 	private static final String CONFIG_FILE = "/loadLabURLS.properties";
 	
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		HttpSession session = request.getSession();
 		LabActivitiesSearchResultForm lForm = (LabActivitiesSearchResultForm) form;
 		
 		//if the session is new or the login object is null; redirects the user to login page  
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null))
 		{
 			logDB.error("No Session or User Object Forwarding to the Login Page");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		String username = ((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId();
 		UserInfoHelper.setUserInfo(username, session.getId());		
 		int numOfLabs =0;
 		
 		try
 		{  //calls the loadTocaAERS method
 			numOfLabs = loadTocaAERS(request, lForm, username);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, numOfLabs+" Message(s) Submitted to caAERS Successfully"));
 			updateLabResult(request);
 			updateLabResultForUI(request);
 			saveMessages( request, messages );
 		}
 		catch (Exception cse)
 		{
 			String msg = cse.getMessage();
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Error in Submitting Messages to caAERS: " + msg));
 			saveErrors( request,errors );
 			logDB.error("Error sending labs to caAERS", cse);
 		}
 		session.setAttribute(DisplayConstants.CURRENT_FORM, lForm);
 		//if the login is valid and the selected form data is successfully loaded to caAERS; 
 		//it returns to the search results page and displays the load successful message	
 		return (mapping.findForward(ForwardConstants.LOAD_TO_caAERS_EVENT_SUCCESS));
 	}
 	
 	/**
 	 * Collects the selected form data and calls the EvenManager sendLabActivitiesmethod to
 	 * load the data to CTMS
 	 * @param request
 	 * @param form
 	 * @param username
 	 * @return numOfLabs number of labs laoded
 	 * @throws Exception
 	 */
 	private int loadTocaAERS(HttpServletRequest request,ActionForm form, String username) throws Exception
 	{
 		HttpSession session = request.getSession();
 	    LabActivitiesSearchResultForm lForm = (LabActivitiesSearchResultForm)form;
 		HashMap map = (HashMap) request.getSession().getAttribute("RESULT_SET");
 		HashMap<String,LabActivityResult> labResultsMap = new HashMap<String,LabActivityResult>();
 		HashMap<String,String> labResultIds = new HashMap<String,String>();
 		String[] test = lForm.getRecordIds();
 		int count =0;
 		int numOfLabs=0;
 		// Create the list of results to send
 		if(test!=null)
 		{
 			count = test.length;
 			for(int i=0;i<count;i++)
 			{
 				if(map.get(test[i]) != null){
 					LabActivityResult lar = (LabActivityResult)map.get(test[i]);
 					labResultsMap.put(test[i],lar);
 					labResultIds.put(test[i],lar.getLabResultId());
 				}
 			}
 		}
 		 Properties props = new Properties();
 		 //Get the file input stream
 		 try
 		 {
 			 InputStream stream = getClass().getResourceAsStream(CONFIG_FILE);
 			 props.load(stream);
 		 } 
 		 catch (FileNotFoundException e1) 
 		 {
 		     logDB.error("The config file not found: " + CONFIG_FILE);
 		 } 
 		 catch (IOException e1) 
 		 {
 			 logDB.error("Error reading the config file: " + CONFIG_FILE);
 		 }
 		 
 		// Then create the request
 		String url = (String)props.getProperty("url");
 		CaXchangeRequestProcessorClient client = new CaXchangeRequestProcessorClient(url);
 		
 		LoadLabsRequest labRequest = new LoadLabsRequest();
 		
 		// Then for each lab selected set the lab information
 		LabResult labResults[]= new LabResult[labResultsMap.size()];
 		int i = 0;
 		
 		for(String key: labResultsMap.keySet()){
 			LabActivityResult lab = labResultsMap.get(key);
 			// Populate the study information
 			Documentation documentation = new Documentation();
 			PerformedStudy performedStudy = new PerformedStudy();
 			
 			String studyId = lab.getStudyId();
 			if (studyId != null)
 			{
 				// Set the study identifier on the document
 				gov.nih.nci.cabig.ccts.domain.II ii = new gov.nih.nci.cabig.ccts.domain.II();
 				ii.setExtension(studyId);
 				ii.setAssigningAuthorityName("CTODS");
 				ii.setRoot("caAERS");
 				gov.nih.nci.cabig.ccts.domain.II[] iis = new gov.nih.nci.cabig.ccts.domain.II[1];
 				iis[0] = ii;
 				documentation.setII(iis);
 			}
 			Documentation[] docs = new Documentation[1];
 			docs[0] = documentation;
 			performedStudy.setDocumentation(docs);
 			
 			// Then set the participant and study subject assignment identifiers
 			Participant participant= new Participant();
 			StudySubject studySubject= new StudySubject();
 			
 			Collection<II> studySubjectIds = lab.getSubjectAssignment().getStudySubjectIdentifier();
 			if (studySubjectIds != null && studySubjectIds.size() > 0)
 			{
 				Iterator<II> idIterator = studySubjectIds.iterator();
 				II ssII = idIterator.next();
 				gov.nih.nci.cabig.ccts.domain.II ii = new gov.nih.nci.cabig.ccts.domain.II();
 				ii.setAssigningAuthorityName("CTODS");
 				ii.setRoot("caAERS");
 				ii.setExtension(ssII.getExtension());
 				gov.nih.nci.cabig.ccts.domain.II[] iis = new gov.nih.nci.cabig.ccts.domain.II[1];
 				iis[0] = ii;
 				participant.setII(iis);
 				gov.nih.nci.cabig.ccts.domain.II ii2 = new gov.nih.nci.cabig.ccts.domain.II();
 				ii2.setAssigningAuthorityName("CTODS");
 				ii2.setRoot("caAERS");
 				ii2.setExtension(ssII.getExtension());
 				gov.nih.nci.cabig.ccts.domain.II[] iis2 = new gov.nih.nci.cabig.ccts.domain.II[1];
 				iis2[0] = ii2;
 				studySubject.setII(iis2);
 			}
 			studySubject.setParticipant(participant);
 			studySubject.setPerformedStudy(performedStudy);
 			
 			// Set the activity name
 			PerformedActivity performedActivity= new PerformedActivity();
 			String testName = lab.getLabTestId();
 			performedActivity.setName(testName);
 		    PerformedActivity[] performedActivitys = new PerformedActivity[1];
 			performedActivitys[0] = performedActivity;
 			studySubject.setPerformedActivity(performedActivitys);
 			
 			// Then set the lab result
 			LabResult labResult = new LabResult();
 			labResult.setStudySubject(studySubject);
 			
 			// Set the reported date
 			java.util.Date labDate = lab.getActualDate();
 			if (labDate != null)
 			{
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(labDate);
 				labResult.setReportedDateTime(cal);
 			}
 			
 			// Set the lab result details
 			String numResult = lab.getNumericResult();
 			if ((numResult != null) && (!numResult.equals("")))
 				labResult.setNumericResult(Float.parseFloat(numResult));
 			String txtResult = lab.getTextResult();
 			if ((txtResult != null) && (!txtResult.equals("")))
 				labResult.setTextResult(txtResult);
 			String labUom = lab.getUnitOfMeasure();
 			if (labUom != null)
 				labResult.setNumericUnit(labUom);
 			String lowRange = lab.getLowRange();
 			if (lowRange != null)
 				labResult.setReferenceRangeLow(Float.parseFloat(lowRange));
 			String highRange = lab.getHighRange();
 			if (highRange != null)
 				labResult.setReferenceRangeHigh(Float.parseFloat(highRange));
 			
 			labResults[i] = labResult;
 			i++;
 		}
 		labRequest.setLabResult(labResults);
 		numOfLabs = labResults.length; 
 		//PrintWriter writer = new PrintWriter("caAERSmessage.xml");
 		QName lab = new QName("http://integration/caaers.nci.nih.gov/services","LoadLabsRequest");
 		//Utils.serializeObject(labRequest, lab, writer);
         
 		// Create the caxchange message
 		Message requestMessage = new Message();
 		Metadata metadata = new Metadata();
 	    metadata.setExternalIdentifier("CTODS");
 	    Credentials creds = new Credentials();
 	    creds.setUserName(username);
 	    String credentialEpr = (String)request.getSession().getAttribute("CAGRID_SSO_DELEGATION_SERVICE_EPR");
 	    logDB.info("The credential EPR: "+ credentialEpr);
 	    if (credentialEpr != null)
 	    	creds.setDelegatedCredentialReference(credentialEpr);
 	    metadata.setCredentials(creds);
 	    metadata.setMessageType(MessageTypes.LAB_BASED_AE);
 	    requestMessage.setMetadata(metadata);
 	    Request caxchangeRequest = new Request();
 	    requestMessage.setRequest(caxchangeRequest);
         MessagePayload messagePayload = new MessagePayload();
         URI uri = new URI();
         uri.setPath("gme://ccts.cabig/1.0/gov.nih.nci.cabig.ccts.domain");
         messagePayload.setXmlSchemaDefinition(uri);
         MessageElement messageElement = new MessageElement(lab, labRequest);
         messagePayload.set_any(new MessageElement[]{messageElement});
         requestMessage.getRequest().setBusinessMessagePayload(messagePayload);
 		
         CaXchangeResponseServiceReference crsr = client.processRequestAsynchronously(requestMessage);
         CaXchangeResponseServiceClient responseService = new CaXchangeResponseServiceClient(crsr.getEndpointReference());
         
         boolean gotResponse=false;
         int responseCount = 0;
         ResponseMessage responseMessage = null;
         while(!gotResponse)
         {
 	        try
 	        {
 	        	responseMessage = responseService.getResponse();
 	            gotResponse = true;
 	        }
 	        catch (Exception e)
 	        {
 	        	logDB.info("No response from caxchange", e);
 	        	responseCount++;
 	        	if (responseCount > 60)
 	        	{
 	        		logDB.error("Never got a response from caxchange hub");
 	        		throw new Exception("No response from hub");
 	        	}
 	        	Thread.sleep(1000);
 	        }
         }
         if (responseMessage != null)
         {
         	Response resp = responseMessage.getResponse();
         	logDB.info("caXchange response was " + resp.getResponseStatus().toString());
         	if (resp.getResponseStatus().equals(Statuses.SUCCESS))
         	{   
            		if(resp.getCaXchangeError()!=null){
         		String message = resp.getCaXchangeError().getErrorDescription();
         		logDB.info("Received a success from caxchange hub: " + message);
            		}
            		if(resp.getTargetResponse()!=null)
            			{
            		     for(TargetResponseMessage msg: resp.getTargetResponse()){
            		    	MessageElement[] messagePay = msg.getTargetBusinessMessage().get_any();
            		    	for(MessageElement mEle: messagePay)
            		    	{
           		    		String mEleValue =mEle.getAttributeValue("Acknowlegdement");
            		    		if(mEleValue.equalsIgnoreCase("Processed"))
            		    		{
            		    			logDB.info("Response from Target Service was " + mEleValue);	
            		    		}
            		    		else
            		    		{
            		        		logDB.error("Received response from Target Service: " + mEleValue);
            		        		throw new Exception(mEleValue);
            		    		}
            		    	}
            		    	           		    	
            		     }
            			}
         	}
         	else if (resp.getResponseStatus().equals(Statuses.FAILURE))
         	  {   String message="";
            		if(resp.getCaXchangeError()!=null){
         		message = resp.getCaXchangeError().getErrorDescription();
         		logDB.error("Received a failure from caxchange hub: " + message);
            		}else{
            			if(resp.getTargetResponse()!=null)
            			{
            		     for(TargetResponseMessage msg: resp.getTargetResponse()){
            		    	 message = msg.getTargetMessageStatus().getValue() +":" +msg.getTargetError().getErrorDescription();
            		     }
            			}
            		}
         		throw new Exception(message);
         	}
         }
 	    lForm.setRecordId("");
 		lForm.setRecordId(null);
 		session.setAttribute("LabResultIDs", labResultIds);
 		return numOfLabs;
 	}	
 	
 	/**
 	 * updateLabResult updates the database with information about the
 	 * labs results that were sent to caAERS.
 	 * @param request
 	 */
 	private void updateLabResult(HttpServletRequest request)
 	{
 		Session session=null;
 		Date date = new Date();
 		try{
 	    session =  HibernateUtil.getSessionFactory().getCurrentSession();
 	    if(session!=null)
 	    {
 	    	HashMap<String,String> labResultIds = (HashMap<String,String>)request.getSession().getAttribute("LabResultIDs");
 	    	if(labResultIds!= null)
 	    	{
 	    	 for(String key: labResultIds.keySet())
 	    	 {
 	    		int labResutId = Integer.parseInt(labResultIds.get(key));
 	    		LabViewerStatus lvs = new LabViewerStatus();
 	    		lvs.setAdverseEventIndicator(true);
 	    		lvs.setAdverseEventSentDate(date);
 	    		lvs.setClinicalResultId(labResutId);
 	    		session.beginTransaction();
 	    		session.save(lvs);
 	    	 }
 		  session.getTransaction().commit();
 	     }
 	   } 	
 		}catch (Exception se){
 			logDB.error("Error updating Lab Result: ",se);
 			if (session.getTransaction() != null) {
 		    	session.getTransaction().rollback();
 		    	}
 		      
 		} 		     
 	}
 	
 	/**
 	 * Update the LabResults for UI display
 	 * @param request
 	 */
 	private void updateLabResultForUI(HttpServletRequest request){
 		SearchResult searchResult= (SearchResult)request.getSession().getAttribute("SEARCH_RESULT");
 		List search = searchResult.getSearchResultObjects();
 		HashMap<String,String> labResultIds = (HashMap<String,String>)request.getSession().getAttribute("LabResultIDs");
 		if(labResultIds!= null)
     	{
 		 for(String key: labResultIds.keySet()){
 			int index = (Integer.parseInt(key))-1;
 			LabActivityResult lar = (LabActivityResult)search.get(index);
 			lar.setAdverseEventReported(true);
 			lar.setAdverseEventReportedDate(new java.util.Date().toString());
 		 }
   		 request.getSession().setAttribute("SEARCH_RESULT", searchResult);
        }	
 	}
 }
