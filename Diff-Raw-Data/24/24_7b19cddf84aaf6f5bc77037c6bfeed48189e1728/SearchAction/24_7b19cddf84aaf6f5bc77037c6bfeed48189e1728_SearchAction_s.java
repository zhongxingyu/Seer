 /**
  * 
  */
 package gov.nih.nci.caxchange.ctom.viewer.actions;
 
 import gov.nih.nci.caxchange.ctom.viewer.constants.DisplayConstants;
 import gov.nih.nci.caxchange.ctom.viewer.constants.ForwardConstants;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LabActivitiesSearchForm;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LoginForm;
 import gov.nih.nci.caxchange.ctom.viewer.util.ObjectFactory;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.LabActivityResult;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.LabActivityResultComparator;
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.SearchResult;
 import gov.nih.nci.labhub.domain.CD;
 import gov.nih.nci.labhub.domain.II;
 import gov.nih.nci.labhub.domain.LaboratoryResult;
 import gov.nih.nci.labhub.domain.LaboratoryTest;
 import gov.nih.nci.labhub.domain.Specimen;
 import gov.nih.nci.labhub.domain.SpecimenCollection;
 import gov.nih.nci.labhub.domain.Study;
 import gov.nih.nci.labhub.domain.StudySite;
 import gov.nih.nci.labhub.domain.SubjectAssignment;
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 import gov.nih.nci.system.query.cql.CQLAssociation;
 import gov.nih.nci.system.query.cql.CQLAttribute;
 import gov.nih.nci.system.query.cql.CQLGroup;
 import gov.nih.nci.system.query.cql.CQLLogicalOperator;
 import gov.nih.nci.system.query.cql.CQLObject;
 import gov.nih.nci.system.query.cql.CQLPredicate;
 import gov.nih.nci.system.query.cql.CQLQuery;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
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
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * This class performs the search action. The search action calls the searchObjects method with
  * the user entered search criteria and queries the database.
  * If the search returns a non null resultset; it forwards the user to search results page and
  * displays the results.
  * @author asharma
  * 
  */
 public class SearchAction extends Action
 {
 	private static final Logger logDB = Logger.getLogger(SearchAction.class);
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
         //gets the session object from HttpRequest 
 		HttpSession session = request.getSession();
 		
         //search form
 		LabActivitiesSearchForm lForm = (LabActivitiesSearchForm) form;
        
 		UserInfoHelper.setUserInfo(((LoginForm) session
 				.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(),
 				session.getId());
 		try
 		{
 			SearchResult searchResult = new SearchResult();
 			
 			//search based on the given search criteria
 			searchResult = searchObjects(mapping, lForm, request, errors, messages);
 			
 		    //if the search returned nothing/null; display message  	
 			if (searchResult.getSearchResultObjects() == null
 					|| searchResult.getSearchResultObjects().isEmpty())
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						DisplayConstants.ERROR_ID,
 						"The search criteria returned zero results"));
 				saveErrors(request, errors);
 				if (logDB.isDebugEnabled())
 					logDB
 							.debug(session.getId()
 									+ "|"
 									+ ((LoginForm) session
 											.getAttribute(DisplayConstants.LOGIN_OBJECT))
 											.getLoginId() + "|"
 									+ lForm.getFormName()
 									+ "|search|Failure|No Records found for "
 									+ lForm.getFormName() + " object|"
 									+ form.toString() + "|");
 				return (mapping.findForward(ForwardConstants.SEARCH_FAILURE));
 			}
 			
 			//if search result is not null; forward to searchresults page
 			if (searchResult.getSearchResultMessage() != null
 					&& !(searchResult.getSearchResultMessage().trim()
 							.equalsIgnoreCase("")))
 			{
 				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 						DisplayConstants.MESSAGE_ID, searchResult
 								.getSearchResultMessage()));
 				saveMessages(request, messages);
 			}
 			
 			session.setAttribute(DisplayConstants.SEARCH_RESULT, searchResult);
 		}  
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					DisplayConstants.ERROR_ID, cse.getMessage()));
 			saveErrors(request, errors);
 			if (logDB.isDebugEnabled())
 				logDB.debug(session.getId()
 						+ "|"
 						+ ((LoginForm) session
 								.getAttribute(DisplayConstants.LOGIN_OBJECT))
 								.getLoginId() + "|" + lForm.getFormName()
 						+ "|search|Failure|Error Searching the "
 						+ lForm.getFormName() + " object|" + form.toString()
 						+ "|" + cse.getMessage());
 		}
 		catch (Exception e)
 		{
 			logDB.error(e.getMessage());
 		}
 		
 		//if search result is not null; forward to searchresults page
 		session.setAttribute(DisplayConstants.CURRENT_FORM, lForm);
 		if (logDB.isDebugEnabled())
 			logDB.debug(session.getId()
 					+ "|"
 					+ ((LoginForm) session
 							.getAttribute(DisplayConstants.LOGIN_OBJECT))
 							.getLoginId() + "|" + lForm.getFormName()
 					+ "|search|Success|Success in searching "
 					+ lForm.getFormName() + " object|" + form.toString() + "|");
 		
 		return (mapping.findForward(ForwardConstants.SEARCH_SUCCESS));
 	}
 
 	/**
 	 * SearchObjects queries the database with the user entered search criteria.
 	 * returns the search results
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param errors
 	 * @param messages
 	 * @return searchResult
 	 * @throws Exception
 	 */
 	private SearchResult searchObjects(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			ActionErrors errors, ActionMessages messages) throws Exception
 	{
 		HashMap map = new HashMap();
 		List allLarList = new ArrayList();
 		
 		//search form 
 		LabActivitiesSearchForm lForm = (LabActivitiesSearchForm) form;
 		try
 		{
 			ApplicationService appService = ApplicationServiceProvider.getApplicationService();
 
 			// Create the query to get SubjectAssignment object
 			CQLQuery query = new CQLQuery();
 			CQLObject target = new CQLObject();
 			target.setName("gov.nih.nci.labhub.domain.SubjectAssignment");
 			
 			// Set the subject Identifier on the association to II
 			CQLAssociation subjectAssignmentAssociation1 = new CQLAssociation();
 			subjectAssignmentAssociation1.setName("gov.nih.nci.labhub.domain.II");
 			subjectAssignmentAssociation1.setTargetRoleName("studySubjectIdentifier");
 			subjectAssignmentAssociation1.setAttribute(new CQLAttribute("extension",
 					CQLPredicate.EQUAL_TO, lForm.getPatientId().trim()));
 			
 			// Now get to StudySite
 			CQLAssociation subjectAssignmentAssociation2 = new CQLAssociation();
 			subjectAssignmentAssociation2.setName("gov.nih.nci.labhub.domain.StudySite");
 			subjectAssignmentAssociation2.setTargetRoleName("studySite");
 
 			// Now get to Study
 			CQLAssociation studySiteAssociation1 = new CQLAssociation();
 			studySiteAssociation1.setName("gov.nih.nci.labhub.domain.Study");
 			studySiteAssociation1.setTargetRoleName("study");
 			subjectAssignmentAssociation2.setAssociation(studySiteAssociation1);
 			
 			// Now set the study identifier on the association to II
 			CQLAssociation studyAssociation1 = new CQLAssociation();
 			studyAssociation1.setName("gov.nih.nci.labhub.domain.II");
 			studyAssociation1.setTargetRoleName("studyIdentifier");
 			studyAssociation1.setAttribute(new CQLAttribute("extension",
 					CQLPredicate.EQUAL_TO, lForm.getStudyId().trim()));
 			studySiteAssociation1.setAssociation(studyAssociation1);
 
 			// Now get the SpecimenCollection
 			CQLAssociation subjectAssignmentAssociation3 = new CQLAssociation();
 			subjectAssignmentAssociation3.setName("gov.nih.nci.labhub.domain.SpecimenCollection");
 			subjectAssignmentAssociation3.setTargetRoleName("activityCollection");
 
 			// Set the date conditions
 			CQLGroup group = new CQLGroup();
 			group.addAttribute(new CQLAttribute("actualStartDateTime",
 					CQLPredicate.GREATER_THAN, lForm.getBeginDate()));
 			group.addAttribute(new CQLAttribute("actualStartDateTime",
 					CQLPredicate.LESS_THAN, lForm.getEndDate()));
 
 			group.setLogicOperator(CQLLogicalOperator.AND);
 			subjectAssignmentAssociation3.setGroup(group);
 
 			// Then put it all together
 			CQLGroup finalgroup = new CQLGroup();
 			finalgroup.addAssociation(subjectAssignmentAssociation1);
 			finalgroup.addAssociation(subjectAssignmentAssociation2);
 			finalgroup.addAssociation(subjectAssignmentAssociation3);
 			finalgroup.setLogicOperator(CQLLogicalOperator.AND);
 			target.setGroup(finalgroup);
 
 			query.setTarget(target);
 			List resultList = appService.query(query);
 			
 			//Connection connection = null;
 			//Session session = null;
 
 			//session = ORMConnection.openSession("gov.nih.nci.labhub.domain.SubjectAssignment");
 			//connection = session.connection();
 			List larlist = null;
 
 			int i = 0;
 			for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();)
 			{
 				SubjectAssignment sa = (SubjectAssignment) resultsIterator.next();
 
				//sa.getStudySite().getStudy().getIdentifier();

 				LabActivityResult labActivityResult = null;
 
 				larlist = printRecord(sa, lForm.getBeginDate(), lForm.getEndDate());
 
 				for (int j = 0; j < larlist.size(); j++)
 				{
 					i++;
 					labActivityResult = (LabActivityResult) larlist.get(j);
 					labActivityResult.setAdverseEventReported("0");
 					/*
 					ResultSet rs = connection
 							.createStatement()
 							.executeQuery(
 									"select ADVERSE_EVENT_INDICATOR, UPDATE_DATE from LV_ADVERSE_EVENTS where CLINICAL_RESULT_ID = "
 											+ labActivityResult
 													.getLabResultId());
 					if (rs.next()) {
 						labActivityResult.setAdverseEventReported(rs
 								.getString(1));
 						labActivityResult
 								.setAdverseEventReportedDate(new SimpleDateFormat(
 										"MM-dd-yyyy").format(rs.getDate(2)));
 					} else {
 						labActivityResult.setAdverseEventReported("0");
 					}
 					// labActivityResult.setRecordId(Integer.toString(i));
 
 					// map.put(Integer.toString(i), labActivityResult);
 					 */
 					allLarList.add(labActivityResult);
 				}
 			}
 			if (allLarList != null && allLarList.size() > 0)
 			{
 				LabActivityResultComparator comp = (LabActivityResultComparator) ObjectFactory
 						.getObject("labActivityResultComparator");
 				Collections.sort(allLarList, comp);
 				int k = 0;
 				for (int j = 0; j < allLarList.size(); j++)
 				{
 					k++;
 					LabActivityResult labActivityResult = null;
 					labActivityResult = (LabActivityResult) allLarList
 							.get(j);
 					labActivityResult.setRecordId(Integer.toString(k));
 					map.put(Integer.toString(k), labActivityResult);
 				}
 			}
 		}
 		catch (Exception ex)
 		{
			ex.printStackTrace();
 		}
 		
 		SearchResult searchResult = new SearchResult();
 		searchResult.setSearchResultObjects(allLarList);
 		request.getSession().setAttribute("RESULT_SET", map);
 		request.getSession().setAttribute("SEARCH_RESULT", searchResult);
 			
 		return searchResult;
 	}
 
 	/**
 	 * @param sa
 	 * @param beginDate2
 	 * @param endDate2
 	 * @return
 	 * @throws ParseException
 	 */
 	private ArrayList printRecord(SubjectAssignment sa, String beginDate2, String endDate2)
 		throws ParseException
 	{
 		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 
 		ArrayList list = new ArrayList();
 
 		Date beginActDate = null;
 		Date endActDate = null;
 
 		beginActDate = formatter.parse(beginDate2);
 		endActDate = formatter.parse(endDate2);
 
 		String patientId = null;
 		String studyId = null;
 		String date = null;
 		Date actualDate = null;
 		String labTestId = null;
 		String textResult = null;
 		Float numericResult = null;
 		String unitOfMeasure = null;
		Double lowRange;
		Double highRange;
 		Collection<II> identifiers = null;
 
 		if (sa == null)
 			return null;
 
 		if (sa != null)
 		{
 			LabActivityResult labActivityResult = new LabActivityResult();
 
 			labActivityResult.setSubjectAssignment(sa);
 			
 			// Set the patient ID
 			identifiers = sa.getStudySubjectIdentifier();
 			patientId = retrieveIdentifier(identifiers);
 			labActivityResult.setPatientId(patientId);
 
 			// Set the study ID
 			StudySite site = sa.getStudySite();
 			if (site != null)
 			{
 				Study study = site.getStudy();
 				identifiers = study.getStudyIdentifier();
 				studyId = retrieveIdentifier(identifiers);
 				labActivityResult.setStudyId(studyId);
 			}
 			
 			// Now set the lab information
 			Collection activityCollection = sa.getActivityCollection();
 			if (activityCollection != null && activityCollection.size() > 0)
 			{
 				for (Iterator activityIterator = activityCollection.iterator(); activityIterator.hasNext();)
 				{
 					SpecimenCollection activity = (SpecimenCollection) activityIterator.next();
 
 					actualDate = activity.getActualStartDateTime();
 
 					if (actualDate.before(beginActDate) || actualDate.after(endActDate))
 						continue;
 
 					date = convertToString(activity.getActualStartDateTime());
 					labActivityResult.setDate(date);
 					labActivityResult.setActualDate(actualDate);
 					Collection specimenCollection = activity.getSpecimenCollection();
 					
 					if (specimenCollection != null && specimenCollection.size() > 0)
 					{
 						for (Iterator specimenIterator = specimenCollection
 								.iterator(); specimenIterator.hasNext();)
 						{
 							Specimen specimen = (Specimen) specimenIterator.next();
 							Collection labTestCollection = specimen.getLaboratoryTestCollection();
 
 							if (labTestCollection != null && labTestCollection.size() > 0)
 							{
 								for (Iterator labTestIterator = labTestCollection
 										.iterator(); labTestIterator.hasNext();)
 								{
 									LaboratoryTest labTest = (LaboratoryTest) labTestIterator.next();
 									if (labTest != null)
 									{
 										CD labTestIde = labTest.getLaboratoryTestId();
 										if (labTestIde != null)
 										{
 											labTestId = labTestIde.getCode();
 											labActivityResult.setLabTestId(labTestId);
 										}
 										LaboratoryResult labResult = labTest.getLaboratoryResult();
 
 										if (labResult != null)
 										{
 											numericResult = labResult.getNumericResult();
 											labActivityResult.setLabResultId(labResult.getId().toString());
 
 											if (numericResult != null)
 												labActivityResult.setNumericResult(numericResult.toString());
 											else
 												labActivityResult.setNumericResult("");
 
 											textResult = labResult.getTextResult();
 											labActivityResult.setTextResult(textResult);
 											
											/*
 											CD units = labResult.getUnits();
 											if (units != null)
 											{
 												unitOfMeasure = units.getCode();
 												labActivityResult.setUnitOfMeasure(unitOfMeasure);
											}*/
 
											lowRange = labResult.getReferenceRangeLow().doubleValue();
 											labActivityResult.setLowRange(String.valueOf(lowRange));
											highRange = labResult.getReferenceRangeHigh().doubleValue();
 
 											if (numericResult != null)
 											{
 												if (numericResult < lowRange || numericResult > highRange)
 												{
 													labActivityResult.setIsAdverseEvent(DisplayConstants.YES);
 													System.out.println(DisplayConstants.YES);
 												}
 												else
 												{
 													labActivityResult.setIsAdverseEvent(DisplayConstants.NO);
 												}
 											}
 											else
 											{
 												labActivityResult.setIsAdverseEvent(DisplayConstants.NO);
 											}
 
 											labActivityResult.setHighRange(String.valueOf(highRange));
 											labActivityResult.setLabResult(labResult);
 											list.add(labActivityResult);
 											labActivityResult = new LabActivityResult();
 											
 											// Re set the values
 											labActivityResult.setSubjectAssignment(sa);
 											labActivityResult.setPatientId(patientId);
 											labActivityResult.setStudyId(studyId);
 											labActivityResult.setDate(date);
 											labActivityResult.setActualDate(actualDate);
 
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return list;
 	}
 	
 	private String retrieveIdentifier(Collection<II> identifiers)
 	{
 		String identifier = null;
 		
 		Iterator<II> idIterator = identifiers.iterator();
 		identifier = idIterator.next().getExtension();
 		
 		return identifier;
 	}
 
 	private static String convertToString(java.util.Date date) {
 		if (date == null)
 			return "-";
 		SimpleDateFormat formatter = new SimpleDateFormat();
 		String str = formatter.format(date);
 		return str;
 	}
 }
