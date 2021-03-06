 /******************************************************************************
  * EvaluationAssignConfirmProducer.java - created by kahuja@vt.edu on Oct 05, 2006
  * 
  * Copyright (c) 2007 Virginia Polytechnic Institute and State University
  * Licensed under the Educational Community License version 1.0
  * 
  * A copy of the Educational Community License has been included in this 
  * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
  * 
  * Contributors:
  * Rui Feng (fengr@vt.edu)
  * Kapil Ahuja (kahuja@vt.edu)
  *****************************************************************************/
 
 package org.sakaiproject.evaluation.tool.producers;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
 import org.sakaiproject.evaluation.logic.EvalExternalLogic;
 import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
 import org.sakaiproject.evaluation.logic.model.EvalGroup;
 import org.sakaiproject.evaluation.logic.utils.EvalUtils;
 import org.sakaiproject.evaluation.model.constant.EvalConstants;
 import org.sakaiproject.evaluation.tool.EvaluationBean;
 
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Confirmation of assign an evaluation to courses
  * 
  * @author Kapil Ahuja (kahuja@vt.edu)
  * @author Rui Feng (fengr@vt.edu)
  * @author Aaron Zeckoski (aaronz@vt.edu)
  */
 public class EvaluationAssignConfirmProducer implements ViewComponentProducer, NavigationCaseReporter {
 
 	public static final String VIEW_ID = "evaluation_assign_confirm";
 	public String getViewID() {
 		return VIEW_ID;
 	}
 
 	private EvalExternalLogic externalLogic;
 	public void setExternalLogic(EvalExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 
 	private EvalAssignsLogic assignsLogic;
 	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
 		this.assignsLogic = assignsLogic;
 	}
 
 	private EvaluationBean evaluationBean;
 	public void setEvaluationBean(EvaluationBean evaluationBean) {
 		this.evaluationBean = evaluationBean;
 	}
 
 	private Locale locale;
 	public void setLocale(Locale locale) {
 		this.locale = locale;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
 	 */
 	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
 		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
 
 		UIMessage.make(tofill, "page-title", "evaluationassignconfirm.page.title");
 
 		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),
 				new SimpleViewParameters(SummaryProducer.VIEW_ID));
 
 		UIMessage.make(tofill, "create-evaluation-title", "starteval.page.title");
 		UIMessage.make(tofill, "assign-evaluation-title", "assigneval.page.title");
		UIMessage.make(tofill, "assign-evaluation-title-confirmation", "assigneval.page.confirmation.title");
 		UIMessage.make(tofill, "eval-assign-info", "evaluationassignconfirm.eval.assign.info", new Object[] {evaluationBean.eval.getTitle()});
 		// getting the data this way is crap
 		Date startDate = evaluationBean.eval.getStartDate();
 		if (startDate == null) { startDate = evaluationBean.startDate; }
 		if (startDate == null) { startDate = new Date(); } // default to avoid crashing, hate this -AZ
 		UIMessage.make(tofill, "eval-assign-instructions", "evaluationassignconfirm.eval.assign.instructions",
 				new Object[] {df.format(startDate)});
 
 		Long evaluationId = evaluationBean.eval.getId();
 
 		UIMessage.make(tofill, "courses-selected-header", "evaluationassignconfirm.courses.selected.header");
 
 		UIMessage.make(tofill, "title-header", "evaluationassignconfirm.title.header");
 		UIMessage.make(tofill, "enrollment-header", "evaluationassignconfirm.enrollment.header");
 
 		String[] selectedIds = evaluationBean.selectedSakaiSiteIds;
 		int[] enrollment = evaluationBean.enrollment;
 
 		Map allIdTitleMap = new HashMap();
 		List evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
 		for (int i = 0; i < evalGroups.size(); i++) {
 			EvalGroup c = (EvalGroup) evalGroups.get(i);
 			allIdTitleMap.put(c.evalGroupId, c.title);
 		}
 
 		for (int i = 0; i < selectedIds.length; ++i) {
 			String evalGroupId = selectedIds[i];
 			UIBranchContainer siteRow = UIBranchContainer.make(tofill, "sites:", evalGroupId);
 			UIOutput.make(siteRow, "siteTitle", (String) allIdTitleMap.get(evalGroupId));
 			if (evaluationId != null) {
 				// only add in this link if the evaluation exists
 				Long assignGroupId = assignsLogic.getAssignGroupId(evaluationId, evalGroupId);
 				if (assignGroupId != null) {
 					UILink.make(siteRow, "direct-eval-group-link", UIMessage.make("evaluationassignconfirm.direct.link"), 
 							externalLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroupId.toString()));
 				}
 			}
 			UIOutput.make(siteRow, "enrollment", enrollment[i] + "");
 		}
 
 		// show submit buttons for first time evaluation creation && Queued Evaluation case
 		if (evaluationId == null) {
 			//first time evaluation creation
 			showButtonsForm(tofill);
 		} else {
 			// check if evaluation is queued; Closed, started evaluation can not have assign groups changed
 			if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(EvalUtils.getEvaluationState( evaluationBean.eval ) ) ) {
 				showButtonsForm(tofill);
 			}
 		}
 	}
 
 	/**
 	 * @param tofill
 	 */
 	private void showButtonsForm(UIContainer tofill) {
 		UIBranchContainer showButtons = UIBranchContainer.make(tofill, "showButtons:");
 		UIForm evalAssignForm = UIForm.make(showButtons, "evalAssignForm");
 		UICommand.make(evalAssignForm, "doneAssignment", 
 				UIMessage.make("evaluationassignconfirm.done.button"), "#{evaluationBean.doneAssignmentAction}");
 		UICommand.make(evalAssignForm, "changeAssignedCourse", 
 				UIMessage.make("evaluationassignconfirm.changes.assigned.courses.button"),
 				"#{evaluationBean.changeAssignedCourseAction}");
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
 	 */
 	public List reportNavigationCases() {
 		List i = new ArrayList();
 		i.add(new NavigationCase(ControlEvaluationsProducer.VIEW_ID, new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
 		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
 		return i;
 	}
 
 }
