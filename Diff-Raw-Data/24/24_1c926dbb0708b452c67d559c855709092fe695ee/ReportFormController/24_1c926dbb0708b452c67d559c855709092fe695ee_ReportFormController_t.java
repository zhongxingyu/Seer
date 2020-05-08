 package org.huamuzhen.oa.server.controller;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.huamuzhen.oa.biz.FeedbackManager;
 import org.huamuzhen.oa.biz.KeyValuePairManager;
 import org.huamuzhen.oa.biz.ReportFormManager;
 import org.huamuzhen.oa.biz.ReportFormMatterManager;
 import org.huamuzhen.oa.biz.ReportFormTypeManager;
 import org.huamuzhen.oa.biz.UserManager;
 import org.huamuzhen.oa.domain.entity.Feedback;
 import org.huamuzhen.oa.domain.entity.OrgUnit;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.huamuzhen.oa.domain.entity.ReportFormMatter;
 import org.huamuzhen.oa.domain.entity.ReportFormType;
 import org.huamuzhen.oa.domain.entity.User;
 import org.huamuzhen.oa.domain.enumeration.Privilege;
 import org.huamuzhen.oa.domain.enumeration.ReportFormStatus;
 import org.huamuzhen.oa.domain.enumeration.UrgentLevel;
 import org.huamuzhen.oa.server.util.FeedbackComparator;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping("/reportForm")
 public class ReportFormController {
 	
 	@Resource
 	private ReportFormManager reportFormManager;
 	
 	@Resource
 	private ReportFormTypeManager reportFormTypeManager;
 	
 	@Resource
 	private FeedbackManager feedbackManager;
 	
 	@Resource
 	private UserManager userManager;
 	
 	@Resource
 	private KeyValuePairManager config;
 	
 	@Resource
 	private ReportFormMatterManager reportFormMatterManager;
 	
 	private static String OPERATE = "operate";
 	private static String STATUS = "status";
 	
 	@RequestMapping(value = { "", "/" })
 	public String index(HttpServletRequest request){
 		
 		return "reportForm";
 	}
 	
 	@RequestMapping("/addReportForm")
 	public ModelAndView addReportForm(HttpSession session){
 		ModelAndView mav = baseOperateReportFormMAV(session);
 		mav.addObject(OPERATE, "add");
 		return mav;
 	}
 	
 	
 	@RequestMapping(value="/editUnsendReportForm/{id}",method=RequestMethod.POST)
 	public ModelAndView editUnsendReportForm(@PathVariable String id,HttpSession session){
 		ReportForm selectedReportForm =reportFormManager.findOne(id);
 		ModelAndView mav = baseOperateReportFormMAV(session);
 		mav.addObject("selectedReportForm", selectedReportForm);
 		mav.addObject(OPERATE, "edit");
 		return mav;
 	}
 	
 	@RequestMapping(value="/reCreateReportForm/{id}",method=RequestMethod.POST)
 	public ModelAndView reCreateReportForm(@PathVariable String id,HttpSession session){
 		ReportForm selectedReportForm =reportFormManager.findOne(id);
 		ModelAndView mav = baseOperateReportFormMAV(session);
 		mav.addObject("selectedReportForm", selectedReportForm);
 		mav.addObject(OPERATE, "reCreate");
 		return mav;
 	}
 	
 	private ModelAndView baseOperateReportFormMAV(HttpSession session){
 		ModelAndView mav = new ModelAndView("operateReportForm");
 		User currentUser = (User)session.getAttribute("currentUser");
 		mav.addObject("supportedReportFormTypeList", currentUser.getSupportedReportFormTypes());
 		List<ReportFormMatter> reportFormMatterList = reportFormMatterManager.findAll();
 		mav.addObject("reportFormMatterList", reportFormMatterList);
 		return mav;
 	}
 	
 	@RequestMapping(value="/add",method=RequestMethod.POST)
 	public ModelAndView add(HttpServletRequest request){
 		String reportFormTypeId = request.getParameter("reportFormTypeId");
 		
 		String title = request.getParameter("title");
 		if(title.trim().equals("")){
 			title = null;
 		}
 		
 		String landUser = request.getParameter("landUser");
 		String originalLandUser = request.getParameter("originalLandUser");
 		String landLocation = request.getParameter("landLocation");
 		String landArea = request.getParameter("landArea");
 		String landAreaMeasure = request.getParameter("landAreaMeasure");
 		String landUse = request.getParameter("landUse");
 		String originalLandUse = request.getParameter("originalLandUse");
 		String matter = request.getParameter("matter");
 		String matterDetail = request.getParameter("matterDetail");
 		String policyBasis = request.getParameter("policyBasis");
 		String comment = request.getParameter("comment");
 		String responsiblePerson = request.getParameter("responsiblePerson");
 		String auditor = request.getParameter("auditor");
 		String tabulator = request.getParameter("tabulator");
 		User currentUser = (User)request.getSession().getAttribute("currentUser");
 		
 	    ReportForm newReportForm = reportFormManager.createNew(title,reportFormTypeId,
 				landUser, originalLandUser, landLocation, new BigDecimal(landArea),landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson,auditor,tabulator, currentUser.getId());
 	    
 		ModelAndView mav = new ModelAndView("viewReportForm");
 		mav.addObject("reportForm", newReportForm);
 		return mav;
 	}
 	
 	@RequestMapping(value="/edit",method=RequestMethod.POST)
 	public String edit(HttpServletRequest request){
 		String id = request.getParameter("id");
 		String reportFormTypeId = request.getParameter("reportFormTypeId");
 		String title = request.getParameter("title");
 		String formId = request.getParameter("formId");
 		String landUser = request.getParameter("landUser");
 		String originalLandUser = request.getParameter("originalLandUser");
 		String landLocation = request.getParameter("landLocation");
 		String landArea = request.getParameter("landArea");
 		String landAreaMeasure = request.getParameter("landAreaMeasure");
 		String landUse = request.getParameter("landUse");
 		String originalLandUse = request.getParameter("originalLandUse");
 		String matter = request.getParameter("matter");
 		String matterDetail = request.getParameter("matterDetail");
 		String policyBasis = request.getParameter("policyBasis");
 		String comment = request.getParameter("comment");
 		String responsiblePerson = request.getParameter("responsiblePerson");
 		String auditor = request.getParameter("auditor");
 		String tabulator = request.getParameter("tabulator");
 		reportFormManager.updateExisting(id, reportFormTypeId, title, formId,
 				landUser, originalLandUser, landLocation, new BigDecimal(landArea),landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson,auditor,tabulator);
 		
 		
 		return "redirect:/reportForm/list/notSendReportForm";
 	}
 	
 	@RequestMapping(value="/reCreate",method=RequestMethod.POST)
 	public ModelAndView reCreate(HttpServletRequest request){
 		
 		String oldId = request.getParameter("id");
 		String reportFormTypeId = request.getParameter("reportFormTypeId");
 		String title = request.getParameter("title");
 		String oldFormId = request.getParameter("formId");
 		String landUser = request.getParameter("landUser");
 		String originalLandUser = request.getParameter("originalLandUser");
 		String landLocation = request.getParameter("landLocation");
 		String landArea = request.getParameter("landArea");
 		String landAreaMeasure = request.getParameter("landAreaMeasure");
 		String landUse = request.getParameter("landUse");
 		String originalLandUse = request.getParameter("originalLandUse");
 		String matter = request.getParameter("matter");
 		String matterDetail = request.getParameter("matterDetail");
 		String policyBasis = request.getParameter("policyBasis");
 		String comment = request.getParameter("comment");
 		String responsiblePerson = request.getParameter("responsiblePerson");
 		String auditor = request.getParameter("auditor");
 		String tabulator = request.getParameter("tabulator");
 		User currentUser = (User)request.getSession().getAttribute("currentUser");
 		ReportForm newReportForm = reportFormManager.reCreateReportForm(oldId, reportFormTypeId, title, oldFormId,
 				landUser, originalLandUser, landLocation, new BigDecimal(landArea),landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson,auditor,tabulator,currentUser.getId() );
 		
 		ModelAndView mav = new ModelAndView("viewReportForm");
 		mav.addObject("reportForm", newReportForm);
 		return mav;
 
 	}
 	
 	@RequestMapping(value="/delete/{id}",method=RequestMethod.POST)
 	public String delete(@PathVariable String id){
		reportFormManager.setReportFormAsDeleted(id);
 		return "redirect:/reportForm/list/notSendReportForm";
 		
 	}
 	
 	@RequestMapping(value="/sendToOrgUnits/{id}", method=RequestMethod.POST)
 	public String sendToOrgUnits(@PathVariable String id, HttpSession session){
 		User currentUser = (User)session.getAttribute("currentUser");
 		reportFormManager.sendToOrgUnits(id, currentUser.getId(),Integer.parseInt(config.getParamsMap().get("deadlineDuration")));
 		return "redirect:/reportForm/list/notSendReportForm";
 		
 	}
 	
 	@RequestMapping(value="/sendToLeader1/{id}", method=RequestMethod.POST)
 	public String sendToLeader1(@PathVariable String id, HttpServletRequest request){
 		String leader1Id = request.getParameter("leader1Id");
 		if(leader1Id.equals("")){
 			leader1Id = null;
 		}
 		User currentUser = (User)request.getSession().getAttribute("currentUser");
 		reportFormManager.sendToLeader1(id, leader1Id,currentUser.getId());	
 		return "redirect:/reportForm/list/gotReplyFromUnitsReportForm";
 	}
 	
 	@RequestMapping(value="/sendBackToReporter/{id}", method=RequestMethod.POST)
 	public String sendBackToReporter(@PathVariable String id,  HttpSession session){
 		User currentUser = (User)session.getAttribute("currentUser");
 		reportFormManager.sendBackToReporter(id,currentUser.getId());	
 		return "redirect:/reportForm/list/rejectedByLeader2ReportForm";
 	}
 	
 	@RequestMapping(value="/list/{reportFormStatusLink}")
 	public ModelAndView list(@PathVariable String reportFormStatusLink, HttpSession session){
 		ModelAndView mav = new ModelAndView("listResponseReportForm");
 		User currentUser = (User)session.getAttribute("currentUser");
 		List<ReportForm> reportFormList = null;
 		if (reportFormStatusLink.equals("sentToLeader1ReportForm")
 				|| reportFormStatusLink.equals("sentToLeader2ReportForm")) {
 			reportFormList = reportFormManager.findReportFormByStatusAndCurrentReceiverId(reportFormStatusLink,currentUser.getId());
 			
 		}else if(reportFormStatusLink.equals("sentToOrgUnitsReportForm")){
 			// need to be optimized, use one HQL instead of looping
 			List<ReportForm> allReportFormList = reportFormManager.findReportFormByStatus(reportFormStatusLink);
 			reportFormList = new ArrayList<ReportForm>();
 			List<ReportForm> responsedReportFormList = new ArrayList<ReportForm>();
 			for(ReportForm reportForm : allReportFormList){
 				if(currentUser.getOrgUnit()!=null){
 					for(OrgUnit orgUnit:reportForm.getReportFormType().getRequiredOrgUnits()){
 						if(currentUser.getOrgUnit().getName().equals(orgUnit.getName())){
 							if(!feedbackManager.checkIfOrgUnitFeedbackIsAlreadyExists(orgUnit, reportForm.getId())){
 								reportFormList.add(reportForm);
 								break;
 							}else{
 								responsedReportFormList.add(reportForm);
 								break;
 							}
 						}
 					}
 				}
 			}
 			mav.addObject("responsedReportFormList", responsedReportFormList);
 			reportFormList = addUrgentInfo(reportFormList);
 		}else if(reportFormStatusLink.equals("passedReportForm")){
 			if(currentUser.getPrivilege() == Privilege.OFFICE){
 				reportFormList = reportFormManager.findReportFormByStatus(reportFormStatusLink);
 			}else if(currentUser.getPrivilege() == Privilege.NORMAL){
 				reportFormList = reportFormManager.findReportFormByStatusAndCreatorId(reportFormStatusLink, currentUser.getId());
 			}else if(currentUser.getPrivilege() == Privilege.LEADER1){
 				reportFormList = reportFormManager.findReportFormByStatusAndCurrentReceiverId(reportFormStatusLink, currentUser.getId());
 			}
 		}else if(reportFormStatusLink.equals("notSendReportForm")){
 			reportFormList = reportFormManager.findReportFormByStatusAndCreatorId(reportFormStatusLink, currentUser.getId());
 		}else if(reportFormStatusLink.equals("gotReplyFromUnitsReportForm")){
 			reportFormList = reportFormManager.findReportFormByStatusAndCreatorId(reportFormStatusLink, currentUser.getId());
 		}else if(reportFormStatusLink.equals("rejectedByLeader1ReportForm")){
 			reportFormList = reportFormManager.findReportFormByStatusAndCurrentReceiverId(reportFormStatusLink, currentUser.getId());
 		}else if(reportFormStatusLink.equals("rejectedByLeader2ReportForm")){
 			reportFormList = reportFormManager.findReportFormByStatusAndCurrentReceiverId(reportFormStatusLink, currentUser.getId());
 		}else if(reportFormStatusLink.equals("sentToOfficeReportForm")){
 			if(currentUser.getPrivilege() == Privilege.OFFICE){
 				reportFormList = reportFormManager.findReportFormByStatus(reportFormStatusLink);
 			}else if(currentUser.getPrivilege() == Privilege.LEADER1){
 				reportFormList = reportFormManager.findReportFormByStatusAndCurrentReceiverId(reportFormStatusLink, currentUser.getId());
 			}
 			reportFormList = addUrgentInfo(reportFormList);
 		}else{ 
 			reportFormList = reportFormManager.findReportFormByStatus(reportFormStatusLink);
 		}
 		mav.addObject("reportFormList", reportFormList);
 		mav.addObject("reportFormStatusLink", reportFormStatusLink);
 		
 		return mav;
 	}
 	
 	private List<ReportForm> addUrgentInfo(final List<ReportForm> reportFormList) {
 		long now = System.currentTimeMillis();
 		for(ReportForm reportForm : reportFormList){
 			if(null != reportForm.getDeadlineTime()){
 				if(reportForm.getDeadlineTime().getTime() < now){
 					reportForm.setUrgentLevel(UrgentLevel.EXCEED);
 				}else if(reportForm.getDeadlineTime().getTime() < now + 86400000){
 					reportForm.setUrgentLevel(UrgentLevel.URGENT);
 				}
 			}
 		}
 		return reportFormList;
 	}
 
 	@RequestMapping(value="responseReportForm/{id}",method=RequestMethod.POST)
 	public ModelAndView responseReportForm(@PathVariable String id, HttpServletRequest request){
 		ModelAndView mav = baseViewReportFormMAV(id);
 		ReportForm selectedReportForm = reportFormManager.findOne(id);
 		mav.addObject("selectedReportForm", selectedReportForm);
 		if(selectedReportForm.getStatus() == ReportFormStatus.SENT_TO_ORG_UNITS){
 			User currentUser = (User)request.getSession().getAttribute("currentUser");
 			if(null == currentUser.getOrgUnit()){
 				 ModelAndView errorMav = new ModelAndView("error");
 				 errorMav.addObject("errorMessage", "用户不属于任何部门");
 				 return errorMav;
 			}
 			ReportFormType reportFormType = reportFormTypeManager.findOne(selectedReportForm.getReportFormType().getId());
 			Set<OrgUnit> requiredOrgUnits = reportFormType.getRequiredOrgUnits();
 			
 			OrgUnit qualifiedOrgUnit = null;
 			for(OrgUnit orgUnit : requiredOrgUnits){
 				if(currentUser.getOrgUnit().getName().equals(orgUnit.getName())){					
 					 qualifiedOrgUnit = orgUnit;
 					 break;
 				}
 			}
 			if(feedbackManager.checkIfOrgUnitFeedbackIsAlreadyExists(qualifiedOrgUnit, id)){
 				 mav.addObject("feedbackExistsFlag", new Object());
 			 }
 			mav.addObject("qualifiedOrgUnit", qualifiedOrgUnit);
 		}else if(selectedReportForm.getStatus() == ReportFormStatus.SENT_TO_LEADER1){
 			mav.addObject("leader2List", userManager.findUserByPrivilege(Privilege.LEADER2));
 		}
 		mav.addObject("responseType", selectedReportForm.getStatus());
 		mav.addObject(STATUS, "response");
 		return mav;
 	}	
 	
 	
 	@RequestMapping(value="/printReportForm/{id}",method=RequestMethod.POST)
 	public ModelAndView printReportForm(@PathVariable String id){
 		ReportForm printedReportForm =reportFormManager.findOne(id);
 		ModelAndView mav = new ModelAndView("printReportForm");
 		mav.addObject("printedReportForm", printedReportForm);
 		
 		List<Feedback> feedbackList = feedbackManager.findFeedbackByReportFormId(id);
 		List<Feedback> feedbackFromOrgUnits = new ArrayList<Feedback>();
 		// empty feedback
 		Feedback feedbackFromLeader1 = new Feedback();
 		Feedback feedbackFromLeader2 = new Feedback();
 		Feedback feedbackFromOffice = new Feedback();
 		for(Feedback feedback : feedbackList){
 			if(feedback.getOwner().equals(Privilege.LEADER1.toString())){
 				feedbackFromLeader1 = feedback;
 			}else if(feedback.getOwner().equals(Privilege.LEADER2.toString())){
 				feedbackFromLeader2 = feedback;
 			}else if(feedback.getOwner().equals(Privilege.OFFICE.toString())){
 				feedbackFromOffice = feedback;
 			}else{
 				feedbackFromOrgUnits.add(feedback);
 			}
 		}
 		Collections.sort(feedbackFromOrgUnits,new FeedbackComparator());
 
 		mav.addObject("feedbackFromOrgUnits", feedbackFromOrgUnits);
 		mav.addObject("feedbackFromLeader1", feedbackFromLeader1);
 		mav.addObject("feedbackFromLeader2", feedbackFromLeader2);
 		mav.addObject("feedbackFromOffice", feedbackFromOffice);
 		
 		return mav;
 	}
 	
 	@RequestMapping(value="/view/{id}",method=RequestMethod.POST)
 	public ModelAndView view(@PathVariable String id){
 		
 		return baseViewReportFormMAV(id);
 	}
 	
 	@RequestMapping(value="/reviewReportForm/{id}",method=RequestMethod.POST)
 	public ModelAndView reviewReportForm(@PathVariable String id){
 		ModelAndView mav = baseViewReportFormMAV(id);
 		mav.addObject(STATUS, "review");
 		mav.addObject("leader1List", userManager.findUserByPrivilege(Privilege.LEADER1));
 		return mav;
 	}
 	
 	private ModelAndView baseViewReportFormMAV(String id){
 		ModelAndView mav = new ModelAndView("viewReportForm");
 		ReportForm selectedReportForm = reportFormManager.findOne(id);
 		mav.addObject("reportForm", selectedReportForm);
 		List<Feedback> feedbackList = feedbackManager.findFeedbackByReportFormId(id);
 		mav.addObject("feedbackList",feedbackList);
 		return mav;
 	}
 
 }
