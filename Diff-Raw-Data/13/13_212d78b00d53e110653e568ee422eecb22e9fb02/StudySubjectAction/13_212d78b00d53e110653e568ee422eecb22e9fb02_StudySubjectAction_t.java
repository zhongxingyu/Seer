 /**
  * 
  */
 package gov.nih.nci.caxchange.ctom.viewer.actions;
 
 import java.util.Iterator;
 import java.util.List;
 
 import gov.nih.nci.caxchange.ctom.viewer.constants.DisplayConstants;
 import gov.nih.nci.caxchange.ctom.viewer.constants.ForwardConstants;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LabActivitiesSearchForm;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LoginForm;
 import gov.nih.nci.labhub.domain.II;
 import gov.nih.nci.labhub.domain.SubjectAssignment;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 import gov.nih.nci.system.query.cql.CQLAssociation;
 import gov.nih.nci.system.query.cql.CQLAttribute;
 import gov.nih.nci.system.query.cql.CQLGroup;
 import gov.nih.nci.system.query.cql.CQLLogicalOperator;
 import gov.nih.nci.system.query.cql.CQLObject;
 import gov.nih.nci.system.query.cql.CQLPredicate;
 import gov.nih.nci.system.query.cql.CQLQuery;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * @author asharma
  *
  */
 public class StudySubjectAction extends Action
 {
 
 	private static final Logger log = Logger.getLogger(StudySubjectAction.class);
 	
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception 
 	{
 		HttpSession session = request.getSession();
 		LabActivitiesSearchForm baseDBForm = new LabActivitiesSearchForm();
 		ActionErrors errors = new ActionErrors();		
 		if((request.getParameter("studySubjectGridId"))!=null){
 			session.setAttribute(DisplayConstants.HOT_LINK,"true"); 
 			session.setAttribute("studySubjectGridId", (String)request.getParameter("studySubjectGridId"));
 			session.setAttribute(DisplayConstants.CURRENT_ACTION, DisplayConstants.SEARCH);
 			session.setAttribute(DisplayConstants.CURRENT_FORM, baseDBForm);
 			session.setAttribute(DisplayConstants.CURRENT_TABLE_ID,DisplayConstants.HOME_ID);
 			LabActivitiesSearchForm labFm=(LabActivitiesSearchForm)session.getAttribute("CURRENT_FORM");
 			try {
 				log.info("Get Patient info");
 				getPatientIdRoot((String)session.getAttribute("studySubjectGridId"),labFm);
 				
 			} catch (Exception e) {
 				log.error("Error retreiving patient information",e);
 			}
 			if(labFm.getStudyId()==null)
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						DisplayConstants.ERROR_ID,
 						"No Patient Information associated with that study"));
 				saveErrors(request, errors);
				log.debug("No Patient Information associated with that study");
				
 				return (mapping.findForward(ForwardConstants.SEARCH_FAILURE));
 			}
 			session.setAttribute("CURRENT_FORM", labFm);
 		  	String operation= "execute";
 			return (mapping.findForward(ForwardConstants.LOGIN_SUCCESS_HOTLINK));
 			}
 		
 		
 		// clear the junk in the session here
 		 
 		if (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null && request.getParameter("studySubjectGridId")==null ) 
 		{
 			session.removeAttribute(DisplayConstants.CURRENT_ACTION);
 			session.removeAttribute(DisplayConstants.CURRENT_FORM);
 			session.removeAttribute(DisplayConstants.SEARCH_RESULT);
 			session.setAttribute(DisplayConstants.HOT_LINK,"false");
 			session.setAttribute(DisplayConstants.CURRENT_TABLE_ID,DisplayConstants.HOME_ID);
 		if (log.isDebugEnabled())
 				log.debug("||||Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 
 		if (log.isDebugEnabled())
 			log.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|Home|Redirect|Success|Already Logged In and Forwarding to the Home Page||");
 		
 		return mapping.findForward(ForwardConstants.HOME_PAGE);
 	}
 	/**
 	 * 
 	 * Select i.extension from identifier i where i.protocol_id in (
 	 * select ss.protocol_id from study_site ss, study_participant_assignment spa, identifier ii where ((ii.root='50113a1a-4c3e-4418-8c37-7383cba1aaf8' and ii.study_participant_assignmnt_id=spa.id 
 	 * and spa.study_site_id=ss.id))) UNION
 	 * select i.extension from identifier i where i.root='50113a1a-4c3e-4418-8c37-7383cba1aaf8' and i.study_participant_assignmnt_id is not null
 	 * @param studyGridId
 	 * @param labFm
 	 * @throws Exception
 	 */
 	private void getPatientIdRoot(String studyGridId,LabActivitiesSearchForm labFm) throws Exception
 	{
 		
 		try
 		{
 			ApplicationService appService = ApplicationServiceProvider.getApplicationService();
 			
 			// Create the query to get SubjectAssignment object
 			CQLQuery query = new CQLQuery();
 			CQLObject target = new CQLObject();
 			target.setName("gov.nih.nci.labhub.domain.SubjectAssignment");
 			//target.setAttribute(new CQLAttribute("studySubjectIdentifier",
 					//CQLPredicate.IS_NOT_NULL,""));
 			
 			// Set the subject Identifier on the association to II
 			CQLAssociation subjectAssignmentAssociation1 = new CQLAssociation();
 			subjectAssignmentAssociation1.setName("gov.nih.nci.labhub.domain.II");
 			subjectAssignmentAssociation1.setTargetRoleName("studySubjectIdentifier");
 			subjectAssignmentAssociation1.setAttribute(new CQLAttribute("root",
 					CQLPredicate.EQUAL_TO, studyGridId.trim()));
 			
 			// Now get to StudySite
 			CQLAssociation subjectAssignmentAssociation2 = new CQLAssociation();
 			subjectAssignmentAssociation2.setName("gov.nih.nci.labhub.domain.StudySite");
 			subjectAssignmentAssociation2.setTargetRoleName("studySite");
 
 			// Now get to Study
 			CQLAssociation studySiteAssociation1 = new CQLAssociation();
 			studySiteAssociation1.setName("gov.nih.nci.labhub.domain.Study");
 			studySiteAssociation1.setTargetRoleName("study");
 			subjectAssignmentAssociation2.setAssociation(studySiteAssociation1);
 			
 			
 			CQLGroup finalgroup = new CQLGroup();
 			finalgroup.addAssociation(subjectAssignmentAssociation1);
 			finalgroup.addAssociation(subjectAssignmentAssociation2);
 			finalgroup.setLogicOperator(CQLLogicalOperator.AND);
 			target.setGroup(finalgroup);
 			query.setTarget(target);
 			
 			List resultList = appService.query(query);
 			for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();)
 			{
 				SubjectAssignment ii = (SubjectAssignment) resultsIterator.next();
 				for(Iterator i = ii.getStudySubjectIdentifier().iterator();i.hasNext();)
 				{	
 					II ident = (II)i.next();
 					labFm.setPatientId(ident.getExtension());
 					//System.out.println(ident.getExtension());
 					//System.out.println("Study gridid"+studyGridId);
 				}
 				for(Iterator i = ii.getStudySite().getStudy().getStudyIdentifier().iterator();i.hasNext();)
 				{	
 					II ident = (II)i.next();
 					labFm.setStudyId(ident.getExtension());
 					//System.out.println(ident.getExtension());
 				}
 			}		
 		}
 		catch(Exception e){}
 		}	
 }
 
 
