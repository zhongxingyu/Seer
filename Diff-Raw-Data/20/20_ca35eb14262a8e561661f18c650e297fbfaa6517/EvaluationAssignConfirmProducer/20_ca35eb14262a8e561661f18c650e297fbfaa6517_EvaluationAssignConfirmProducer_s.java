 /**
  * $Id$
  * $URL$
  * EvaluationCreateProducer.java - evaluation - Oct 05, 2006 11:32:44 AM - kahuja
  **************************************************************************
  * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
  * Licensed under the Educational Community License version 1.0
  * 
  * A copy of the Educational Community License has been included in this 
  * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
  *
  * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
  */
 
 package org.sakaiproject.evaluation.tool.producers;
 
 import java.text.DateFormat;
 import java.util.Locale;
 import java.util.Set;
 
 import org.sakaiproject.evaluation.constant.EvalConstants;
 import org.sakaiproject.evaluation.logic.EvalEvaluationService;
 import org.sakaiproject.evaluation.logic.EvalSettings;
 import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
 import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
 import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
 import org.sakaiproject.evaluation.logic.model.EvalGroup;
 import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
 import org.sakaiproject.evaluation.model.EvalEvaluation;
 import org.sakaiproject.evaluation.tool.SetupEvalBean;
 import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
 import org.sakaiproject.evaluation.utils.EvalUtils;
 
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.ActionResultInterceptor;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 /**
  * Show the currently assigned courses or confirm the assignment and create the evaluation
  * 
  * @author Aaron Zeckoski (aaronz@vt.edu)
  */
 public class EvaluationAssignConfirmProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {
 
    public static final String VIEW_ID = "evaluation_assign_confirm";
    public String getViewID() {
       return VIEW_ID;
    }
 
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
       this.externalLogic = externalLogic;
    }
 
    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
       this.settings = settings;
    }
 
    private ExternalHierarchyLogic hierLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
       hierLogic = logic;
    }
 
    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
       this.evaluationService = evaluationService;
    }
 
    private Locale locale;
    public void setLocale(Locale locale) {
       this.locale = locale;
    }
 
 
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
 
       EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
       if (evalViewParams.evaluationId == null) {
          throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
       }
 
       String actionBean = "setupEvalBean.";
       /**
        * This is the evaluation we are working with on this page,
        * this should ONLY be read from, do not change any of these fields
        */
       EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
       Long evaluationId = evalViewParams.evaluationId;
 
       DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
 
       // local variables used in the render logic
       String currentUserId = externalLogic.getCurrentUserId();
       boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
 
       /*
        * top links here
        */
       UIInternalLink.make(tofill, "summary-link", 
             UIMessage.make("summary.page.title"), 
             new SimpleViewParameters(SummaryProducer.VIEW_ID));
 
       if (userAdmin) {
          UIInternalLink.make(tofill, "administrate-link", 
                UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));
          UIInternalLink.make(tofill, "control-scales-link",
                UIMessage.make("controlscales.page.title"),
                new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
       }
 
       UIInternalLink.make(tofill, "control-evaluations-link",
             UIMessage.make("controlevaluations.page.title"),
             new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
 
 
       // normal page content
 
       UIMessage.make(tofill, "eval-assign-info", "evaluationassignconfirm.eval.assign.info", 
             new Object[] {evaluation.getTitle()});
 
       UIMessage.make(tofill, "eval-assign-instructions", "evaluationassignconfirm.eval.assign.instructions",
             new Object[] {df.format(evaluation.getStartDate())});
 
       // show the selected groups
       String[] selectedGroupIDs = evalViewParams.selectedGroupIDs; //SetupEvalBean.makeArrayFromBooleanMap(evalViewParams.selectedGroupIDsMap);
       if (selectedGroupIDs != null 
             && selectedGroupIDs.length > 0) {
          for (int i = 0; i < selectedGroupIDs.length; ++i) {
             String evalGroupId = selectedGroupIDs[i];
             EvalGroup group = externalLogic.makeEvalGroupObject(evalGroupId);
 
             UIBranchContainer groupRow = UIBranchContainer.make(tofill, "groups:", evalGroupId);
             UIOutput.make(groupRow, "groupTitle", group.title);
             if (evaluationId != null) {
                // only add in this link if the evaluation exists
                Long assignGroupId = evaluationService.getAssignGroupId(evaluationId, evalGroupId);
                if (assignGroupId != null) {
                   UILink.make(groupRow, "direct-eval-group-link", UIMessage.make("evaluationassignconfirm.direct.link"), 
                         externalLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroupId.toString()));
                }
             }
             Set<String> s = externalLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
             UIOutput.make(groupRow, "enrollment", s.size() + "");
          }
       } else {
          UIMessage.make(tofill, "no-courses-selected", "evaluationassignconfirm.no_nodes_selected");
       }
 
       // show the selected hierarchy nodes
       Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
       if (showHierarchy) {
          UIOutput.make(tofill, "nodes-selected-table");
          String[] selectedNodeIDs = evalViewParams.selectedHierarchyNodeIDs; //SetupEvalBean.makeArrayFromBooleanMap(evalViewParams.selectedHierarchyNodeIDsMap);
          if (selectedNodeIDs != null && selectedNodeIDs.length > 0) {
             for (int i = 0; i < selectedNodeIDs.length; i++ ) {
                EvalHierarchyNode node = hierLogic.getNodeById(selectedNodeIDs[i]);
 
                UIBranchContainer nodeRow = UIBranchContainer.make(tofill, "node-row:");
                UIOutput.make(nodeRow, "node-title", node.title);
                UIOutput.make(nodeRow, "node-abbr", node.description);
             }
          } else {
             UIMessage.make(tofill, "no-nodes-selected", "evaluationassignconfirm.no_nodes_selected");
          }
       }
 
       // show submit buttons for first time evaluation creation && not active yet Evaluation case
       String evalState = EvalUtils.getEvaluationState( evaluation, false );
       if ( EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, false) ) {
          // first time evaluation creation or still in queue
          UIBranchContainer showButtons = UIBranchContainer.make(tofill, "showButtons:");
          UIForm evalAssignForm = UIForm.make(showButtons, "evalAssignForm");
          UICommand.make(evalAssignForm, "doneAssignment", UIMessage.make("evaluationassignconfirm.done.button"), 
                actionBean + "completeConfirmAction");
          UIMessage.make(evalAssignForm, "cancel-button", "evaluationassignconfirm.changes.assigned.courses.button");
          // bind in the selected nodes and groups
          //evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedGroupIDs", 
          //      evalViewParams.selectedGroupIDs) );
          //evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedHierarchyNodeIDs", 
          //      evalViewParams.selectedHierarchyNodeIDs) );
          evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedGroupIDsWithPucArray", 
                evalViewParams.selectedGroupIDs) );
          evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedHierarchyNodeIDsWithPucArray", 
                evalViewParams.selectedHierarchyNodeIDs) );
          evalAssignForm.parameters.add( new UIELBinding(actionBean + "evaluationId",evaluationId));
         //evalAssignForm.parameters.add( new UIELBinding(actionBean + ))
          // also bind the evaluation id
          evalAssignForm.parameters.add( new UIELBinding(actionBean + "evaluationId", evaluationId) );
       }
    }
 
 
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
       // handles the navigation cases and passing along data from view to view
       EvalViewParameters evp = (EvalViewParameters) incoming;
       Long evalId = evp.evaluationId;
       if ("evalSettings".equals(actionReturn)) {
          result.resultingView = new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalId);
       } else if ("evalAssign".equals(actionReturn)) {
          result.resultingView = new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evalId);
       } else if ("evalConfirm".equals(actionReturn)) {
          result.resultingView = new EvalViewParameters(EvaluationAssignConfirmProducer.VIEW_ID, evalId);
       } else if ("controlEvals".equals(actionReturn)) {
          result.resultingView = new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID);
       }
    }
 
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
       return new EvalViewParameters();
    }
 
 }
