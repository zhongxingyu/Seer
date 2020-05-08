 package com.solidstategroup.radar.web.panels.subtabs;
 
 import com.solidstategroup.radar.model.ImmunosuppressionTreatment;
 import com.solidstategroup.radar.model.exception.InvalidModelException;
 import com.solidstategroup.radar.model.sequenced.Therapy;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.service.ImmunosuppressionManager;
 import com.solidstategroup.radar.service.PlasmapheresisManager;
 import com.solidstategroup.radar.service.TherapyManager;
 import com.solidstategroup.radar.service.TreatmentManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.RadarSecuredSession;
 import com.solidstategroup.radar.web.behaviours.RadarBehaviourFactory;
 import com.solidstategroup.radar.web.components.RadarComponentFactory;
 import com.solidstategroup.radar.web.components.RadarDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDropdownChoice;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import com.solidstategroup.radar.web.panels.PlasmaPheresisPanel;
 import com.solidstategroup.radar.web.panels.firstvisit.YesNoRadioGroupPanel;
 import com.solidstategroup.radar.web.panels.tables.DialysisTablePanel;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
 import org.apache.wicket.datetime.markup.html.basic.DateLabel;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.RangeValidator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class TreatmentPanel extends Panel {
     @SpringBean
     private TherapyManager therapyManager;
     @SpringBean
     private DiagnosisManager diagnosisManager;
     @SpringBean
     private ImmunosuppressionManager immunosuppressionManager;
     @SpringBean
     private PlasmapheresisManager plasmapheresisManager;
 
     public static final long CYCLOPHOSPHAMIDE_ID = 8;
 
     private IModel<ImmunosuppressionTreatment> editImmunosuppressionTreatmentIModel;
 
     public TreatmentPanel(String id, final IModel<Long> radarNumberModel, boolean firstVisit,
                           IModel<Therapy> followingVisitTherapyModel, List<Component> followingVisitComponentsToUpdate) {
         super(id);
 
         // Immunosuppression including Monoclonals
 
         final IModel immunosuppressionTreatmentListModel = new AbstractReadOnlyModel<List>() {
             @Override
             public List getObject() {
 
                 if (radarNumberModel.getObject() != null) {
                     return immunosuppressionManager.getImmunosuppressionTreatmentByRadarNumber(
                             radarNumberModel.getObject());
                 }
                 return Collections.emptyList();
 
             }
         };
 
         editImmunosuppressionTreatmentIModel = new Model<ImmunosuppressionTreatment>();
 
         final List<Component> addImmunoSuppressComponentsToUpdate = new ArrayList<Component>();
         final List<Component> editImmunoSuppressComponentsToUpdate = new ArrayList<Component>();
 
         final WebMarkupContainer immunosuppressionTreatmentsContainer =
                 new WebMarkupContainer("immunosuppressionTreatmentsContainer");
 
         // For showing edit from ajax call
         final MarkupContainer editContainer = new WebMarkupContainer("editContainer") {
             @Override
             public boolean isVisible() {
                 return editImmunosuppressionTreatmentIModel.getObject() != null;
             }
         };
         editContainer.setOutputMarkupId(true);
         editContainer.setOutputMarkupPlaceholderTag(true);
 
         ListView<ImmunosuppressionTreatment> immunosuppressionTreatmentListView =
                 new ListView<ImmunosuppressionTreatment>("immunosuppressionTreatments",
                         immunosuppressionTreatmentListModel) {
                     @Override
                     protected void populateItem(final ListItem<ImmunosuppressionTreatment> item) {
                         item.setModel(new CompoundPropertyModel<ImmunosuppressionTreatment>(item.getModelObject()));
                         item.add(DateLabel.forDatePattern("startDate", RadarApplication.DATE_PATTERN));
                         item.add(DateLabel.forDatePattern("endDate", RadarApplication.DATE_PATTERN));
                         item.add(new Label("immunosuppression.description"));
                         item.add(new Label("cyclophosphamideTotalDose") {
                             @Override
                             public boolean isVisible() {
                                 return item.getModelObject().getImmunosuppression().getId().
                                         equals(CYCLOPHOSPHAMIDE_ID);
                             }
                         });
                         AjaxLink ajaxDeleteLink = new AjaxLink("deleteLink") {
                             @Override
                             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                                 immunosuppressionManager.deleteImmunosuppressionTreatment(item.getModelObject());
                                 ajaxRequestTarget.add(addImmunoSuppressComponentsToUpdate.toArray(
                                         new Component[addImmunoSuppressComponentsToUpdate.size()]));
                                 ajaxRequestTarget.add(immunosuppressionTreatmentsContainer);
                             }
                         };
                         item.add(ajaxDeleteLink);
                         ajaxDeleteLink.add(RadarBehaviourFactory.getDeleteConfirmationBehaviour());
                         AjaxLink ajaxEditLink = new AjaxLink("editLink") {
                             @Override
                             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                                 editImmunosuppressionTreatmentIModel.setObject(item.getModelObject());
                                 ajaxRequestTarget.add(editContainer);
                             }
                         };
                         item.add(ajaxEditLink);
 
                         AuthenticatedWebSession session = RadarSecuredSession.get();
                         if (session.isSignedIn()) {
                             if (session.getRoles().hasRole(User.ROLE_PATIENT)) {
                                 ajaxDeleteLink.setVisible(false);
                                 ajaxEditLink.setVisible(false);
                             }
                         }
 
                         immunosuppressionTreatmentsContainer.setVisible(true);
                     }
                 };
 
         immunosuppressionTreatmentsContainer.add(immunosuppressionTreatmentListView);
         add(immunosuppressionTreatmentsContainer);
 
         immunosuppressionTreatmentsContainer.setOutputMarkupId(true);
         immunosuppressionTreatmentsContainer.setOutputMarkupPlaceholderTag(true);
 
         // Construct the form
         ImmunosuppressionTreatmentForm editImmunosuppressionForm =
                 new ImmunosuppressionTreatmentForm("editImmunosuppressionForm",
                         new CompoundPropertyModel<ImmunosuppressionTreatment>(editImmunosuppressionTreatmentIModel),
                         editImmunoSuppressComponentsToUpdate);
 
         editImmunosuppressionForm.add(new AjaxSubmitLink("save") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 target.add(editContainer);
                 target.add(immunosuppressionTreatmentsContainer);
                 try {
                     immunosuppressionManager.saveImmunosuppressionTreatment((ImmunosuppressionTreatment)
                             form.getModelObject());
                 } catch (InvalidModelException e) {
                     for (String error : e.getErrors()) {
                         error(error);
                     }
                     return;
                 }
                 editImmunosuppressionTreatmentIModel.setObject(null);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(editImmunoSuppressComponentsToUpdate.toArray(
                         new Component[editImmunoSuppressComponentsToUpdate.size()]));
             }
         });
         editImmunosuppressionForm.add(new AjaxLink("cancel") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 editImmunosuppressionTreatmentIModel.setObject(null);
                 target.add(editContainer);
             }
         });
         editContainer.add(editImmunosuppressionForm);
         add(editContainer);
 
         // Construct the add form
         ImmunosuppressionTreatmentForm addImmunosuppressionForm =
                 new ImmunosuppressionTreatmentForm("addImmunosuppressionForm",
                         new CompoundPropertyModel<ImmunosuppressionTreatment>(new ImmunosuppressionTreatment()),
                         addImmunoSuppressComponentsToUpdate);
 
 
         addImmunosuppressionForm.add(new AjaxSubmitLink("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 target.add(immunosuppressionTreatmentsContainer);
                 target.add(addImmunoSuppressComponentsToUpdate.toArray(
                         new Component[addImmunoSuppressComponentsToUpdate.size()]));
                 ImmunosuppressionTreatment immunosuppressionTreatment = (ImmunosuppressionTreatment)
                         form.getModelObject();
                 immunosuppressionTreatment.setRadarNumber(radarNumberModel.getObject());
                 try {
                     immunosuppressionManager.saveImmunosuppressionTreatment(immunosuppressionTreatment);
                 } catch (InvalidModelException e) {
                     for (String error : e.getErrors()) {
                         error(error);
                     }
                     return;
                 }
                 form.getModel().setObject(new ImmunosuppressionTreatment());
                 immunosuppressionTreatmentsContainer.setVisible(true);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(addImmunoSuppressComponentsToUpdate.toArray(
                         new Component[addImmunoSuppressComponentsToUpdate.size()]));
             }
         });
         add(addImmunosuppressionForm);
 
 
         // Drugs
         final List<Component> therapyFormComponentsToUpdate = new ArrayList<Component>();
 
         final CompoundPropertyModel<Therapy> firstVisitTherapyFormModel = new CompoundPropertyModel
                 <Therapy>(new LoadableDetachableModel<Therapy>() {
             @Override
             public Therapy load() {
                 Therapy therapyModelObject = null;
 
                 if (radarNumberModel.getObject() != null) {
                     therapyModelObject = therapyManager.getFirstTherapyByRadarNumber(radarNumberModel.getObject());
                 }
 
                 if (therapyModelObject == null) {
                     therapyModelObject = new Therapy();
                     therapyModelObject.setSequenceNumber(1);
                 }
                 return therapyModelObject;
             }
         });
 
         CompoundPropertyModel<Therapy> therapyFormModel;
         if (firstVisit) {
             therapyFormModel = firstVisitTherapyFormModel;
         } else {
             therapyFormModel = new CompoundPropertyModel<Therapy>(followingVisitTherapyModel);
         }
 
         final Form<Therapy> therapyForm = new Form<Therapy>("therapyForm", therapyFormModel) {
             @Override
             protected void onSubmit() {
                 Therapy therapy = getModelObject();
                 therapy.setRadarNumber(radarNumberModel.getObject());
                 therapyManager.saveTherapy(therapy);
             }
         };
 
         final IModel<Boolean> isSrnsModel = RadarModelFactory.getIsSrnsModel(radarNumberModel, diagnosisManager);
         IModel firstColumnLabelModel = new LoadableDetachableModel() {
             @Override
             protected Object load() {
                 return isSrnsModel.getObject() ? "Prior to Referral" : "Drugs in the 4 weeks after Biopsy";
             }
         };
         Label successLabel = RadarComponentFactory.getSuccessMessageLabel("successMessage", therapyForm,
                 therapyFormComponentsToUpdate);
 
         Label errorLabel = RadarComponentFactory.getErrorMessageLabel("errorMessage", therapyForm,
                 therapyFormComponentsToUpdate);
 
         RadarRequiredDateTextField treatmentRecordDate = new RadarRequiredDateTextField("treatmentRecordDate",
                 therapyForm, therapyFormComponentsToUpdate);
 
         therapyForm.add(treatmentRecordDate);
 
 
         Label firstColumnLabel = new Label("firstColumnLabel", firstColumnLabelModel);
         therapyForm.add(firstColumnLabel);
 
         WebMarkupContainer currentContainer = new WebMarkupContainer("currentContainer") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         therapyForm.add(currentContainer);
 
         WebMarkupContainer nsaidContainerParent = new WebMarkupContainer("nsaidContainerParent") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         YesNoRadioGroupPanel nsaidContainer = new YesNoRadioGroupPanel("nsaidContainer", true,
                 (CompoundPropertyModel) therapyFormModel,
                 "nsaid");
         nsaidContainerParent.add(nsaidContainer);
         therapyForm.add(nsaidContainerParent);
 
         nsaidContainerParent.add(new YesNoRadioGroupPanel("nsaidPriorContainer", true,
                 (CompoundPropertyModel) therapyFormModel, "nsaidPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
 
         WebMarkupContainer diureticContainerParent = new WebMarkupContainer("diureticContainerParent") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         YesNoRadioGroupPanel diureticContainer = new YesNoRadioGroupPanel("diureticContainer", true,
                 (CompoundPropertyModel) therapyFormModel,
                 "diuretic");
 
         diureticContainerParent.add(diureticContainer);
         therapyForm.add(diureticContainerParent);
 
         diureticContainerParent.add(new YesNoRadioGroupPanel("diureticPriorContainer",
                 true, (CompoundPropertyModel) therapyFormModel, "diureticPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
 
         boolean antihypertensiveToggleInit = (Boolean.FALSE.equals(therapyForm.getModelObject().getAntihypertensive())
                 && Boolean.FALSE.equals(therapyForm.getModelObject().getAntihypertensivePrior())) ||
                 (therapyForm.getModelObject().getAntihypertensive() == null &&
                         therapyForm.getModelObject().getAntihypertensivePrior() == null)
                 ? false : true;
         final IModel<Boolean> antihypertensiveToggleModel = new Model<Boolean>(antihypertensiveToggleInit);
 
         AjaxFormChoiceComponentUpdatingBehavior antihypertensiveToggleBehaviour = new AjaxFormChoiceComponentUpdatingBehavior() {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 antihypertensiveToggleModel.setObject(therapyForm.getModelObject().getAntihypertensive());
 
                 target.add(therapyFormComponentsToUpdate.toArray(new Component[therapyFormComponentsToUpdate.size()]));
             }
         };
 
         AjaxFormChoiceComponentUpdatingBehavior antihypertensiveToggleBehaviour2 = new AjaxFormChoiceComponentUpdatingBehavior() {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 antihypertensiveToggleModel.setObject(therapyForm.getModelObject().getAntihypertensivePrior());
 
                 target.add(therapyFormComponentsToUpdate.toArray(new Component[therapyFormComponentsToUpdate.size()]));
             }
         };
 
         YesNoRadioGroupPanel antihypertensiveContainer = new YesNoRadioGroupPanel("antihypertensiveContainer", true,
                 therapyFormModel,
                 "antihypertensive", antihypertensiveToggleBehaviour);
         therapyForm.add(antihypertensiveContainer);
 
 
         therapyForm.add(new YesNoRadioGroupPanel("antihypertensivePriorContainer", true, therapyFormModel,
                 "antihypertensivePrior", antihypertensiveToggleBehaviour2) {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
 
         WebMarkupContainer aceInhibitorContainer = new WebMarkupContainer("aceInhibitorContainer") {
             @Override
             public boolean isVisible() {
                 return antihypertensiveToggleModel.getObject();
             }
         };
 
         therapyFormComponentsToUpdate.add(aceInhibitorContainer);
         aceInhibitorContainer.setOutputMarkupId(true);
         aceInhibitorContainer.setOutputMarkupPlaceholderTag(true);
 
         YesNoRadioGroupPanel aceInhibitorRadioGroup = new YesNoRadioGroupPanel("aceInhibitorRadioGroup",
                 true, therapyFormModel, "aceInhibitor");
         aceInhibitorContainer.add(aceInhibitorRadioGroup);
         YesNoRadioGroupPanel aceInhibitorPriorRadioGroup = new YesNoRadioGroupPanel("aceInhibitorPriorRadioGroup",
                 true, therapyFormModel,
                 "aceInhibitorPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         aceInhibitorContainer.add(aceInhibitorPriorRadioGroup);
         therapyForm.add(aceInhibitorContainer);
 
 
         WebMarkupContainer arb1AntagonistContainer = new WebMarkupContainer("arb1AntagonistContainer") {
             @Override
             public boolean isVisible() {
                 return antihypertensiveToggleModel.getObject();
             }
         };
 
         therapyFormComponentsToUpdate.add(arb1AntagonistContainer);
         arb1AntagonistContainer.setOutputMarkupId(true);
         arb1AntagonistContainer.setOutputMarkupPlaceholderTag(true);
 
         arb1AntagonistContainer.add(new YesNoRadioGroupPanel("arb1AntagonistRadioGroup", true, therapyFormModel, "arb1Antagonist"));
         arb1AntagonistContainer.add(new YesNoRadioGroupPanel("arb1AntagonistPriorRadioGroup", true, therapyFormModel,
                 "arb1AntagonistPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
         therapyForm.add(arb1AntagonistContainer);
 
         WebMarkupContainer calciumChannelBlockerContainer = new WebMarkupContainer("calciumChannelBlockerContainer") {
             @Override
             public boolean isVisible() {
                 return antihypertensiveToggleModel.getObject();
             }
         };
         therapyFormComponentsToUpdate.add(calciumChannelBlockerContainer);
         calciumChannelBlockerContainer.setOutputMarkupId(true);
         calciumChannelBlockerContainer.setOutputMarkupPlaceholderTag(true);
 
         calciumChannelBlockerContainer.add(new YesNoRadioGroupPanel("calciumChannelBlockerRadioGroup", true, therapyFormModel,
                 "calciumChannelBlocker"));
         calciumChannelBlockerContainer.add(new YesNoRadioGroupPanel("calciumChannelBlockerPriorRadioGroup", true, therapyFormModel,
                 "calciumChannelBlockerPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
         therapyForm.add(calciumChannelBlockerContainer);
 
         WebMarkupContainer betaBlockerContainer = new WebMarkupContainer("betaBlockerContainer") {
             @Override
             public boolean isVisible() {
                 return antihypertensiveToggleModel.getObject();
             }
         };
         therapyFormComponentsToUpdate.add(betaBlockerContainer);
         betaBlockerContainer.setOutputMarkupId(true);
         betaBlockerContainer.setOutputMarkupPlaceholderTag(true);
 
         betaBlockerContainer.add(new YesNoRadioGroupPanel("betaBlockerRadioGroup", true, therapyFormModel, "betaBlocker"));
         betaBlockerContainer.add(new YesNoRadioGroupPanel("betaBlockerPriorRadioGroup", true, therapyFormModel,
                 "betaBlockerPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
         therapyForm.add(betaBlockerContainer);
 
         WebMarkupContainer otherAntihypertensiveContainer = new WebMarkupContainer("otherAntihypertensiveContainer") {
             @Override
             public boolean isVisible() {
                 return antihypertensiveToggleModel.getObject();
             }
         };
         therapyFormComponentsToUpdate.add(otherAntihypertensiveContainer);
         otherAntihypertensiveContainer.setOutputMarkupId(true);
         otherAntihypertensiveContainer.setOutputMarkupPlaceholderTag(true);
 
         otherAntihypertensiveContainer.add(new YesNoRadioGroupPanel("otherAntihypertensiveRadioGroup", true, therapyFormModel,
                 "otherAntihypertensive"));
         otherAntihypertensiveContainer.add(new YesNoRadioGroupPanel("otherAntihypertensivePriorRadioGroup", true, therapyFormModel,
                 "otherAntihypertensivePrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
         therapyForm.add(otherAntihypertensiveContainer);//@current
 
 
         therapyForm.add(new YesNoRadioGroupPanel("insulinContainer", true, therapyFormModel, "insulin"));
         therapyForm.add(new YesNoRadioGroupPanel("insulinPriorContainer", true, therapyFormModel, "insulinPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
 
         WebMarkupContainer lipidLoweringAgentContainerParent =
                 new WebMarkupContainer("lipidLoweringAgentContainerParent") {
                     @Override
                     public boolean isVisible() {
                         return isSrnsModel.getObject();
                     }
                 };
         lipidLoweringAgentContainerParent.add(new YesNoRadioGroupPanel("lipidLoweringAgentContainer", true, therapyFormModel,
                 "lipidLoweringAgent"));
         lipidLoweringAgentContainerParent.add(new YesNoRadioGroupPanel("lipidLoweringAgentPriorContainer", true, therapyFormModel,
                 "lipidLoweringAgentPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
         therapyForm.add(lipidLoweringAgentContainerParent);
 
         therapyForm.add(new YesNoRadioGroupPanel("epoContainer", true, therapyFormModel, "epo"));
         therapyForm.add(new YesNoRadioGroupPanel("epoPriorContainer", true, therapyFormModel, "epoPrior") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         });
 
         therapyForm.add(new TextField("other1"));
 
         WebMarkupContainer other1PriorContainer = new WebMarkupContainer("other1PriorContainer") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         other1PriorContainer.add(new TextField("other1Prior"));
         therapyForm.add(other1PriorContainer);
 
         therapyForm.add(new TextField("other2"));
 
         WebMarkupContainer other2PriorContainer = new WebMarkupContainer("other2PriorContainer") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         other2PriorContainer.add(new TextField("other2Prior"));
         therapyForm.add(other2PriorContainer);
 
         therapyForm.add(new TextField("other3"));
 
         WebMarkupContainer other3PriorContainer = new WebMarkupContainer("other3PriorContainer") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         other3PriorContainer.add(new TextField("other3Prior"));
         therapyForm.add(other3PriorContainer);
 
         therapyForm.add(new TextField("other4"));
 
         WebMarkupContainer other4PriorContainer = new WebMarkupContainer("other4PriorContainer") {
             @Override
             public boolean isVisible() {
                 return isSrnsModel.getObject();
             }
         };
         other4PriorContainer.add(new TextField("other4Prior"));
         therapyForm.add(other4PriorContainer);
 
 
         AjaxSubmitLink save = new AjaxSubmitLink("save", therapyForm) {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.add(therapyFormComponentsToUpdate.toArray(new Component[therapyFormComponentsToUpdate.size()]));
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(therapyFormComponentsToUpdate.toArray(new Component[therapyFormComponentsToUpdate.size()]));
             }
         };
 
         therapyForm.add(save);
         add(therapyForm);
 
         // Plasmapheresis
         PlasmaPheresisPanel plasmaPheresisPanel = new PlasmaPheresisPanel("plasmapheresisPanel", radarNumberModel);
         add(plasmaPheresisPanel);
 
         DialysisTablePanel dialysisTablePanel = new DialysisTablePanel("dialysisContainer", radarNumberModel);
         dialysisTablePanel.setVisible(firstVisit);
         add(dialysisTablePanel);
 
         if (!firstVisit) {
             for (Component component : followingVisitComponentsToUpdate) {
                 therapyFormComponentsToUpdate.add(component);
             }
         }
 
     }
 
     private final class ImmunosuppressionTreatmentForm extends Form<ImmunosuppressionTreatment> {
         private RadarDateTextField endDate;
         @SpringBean
         private ImmunosuppressionManager immunosuppressionManager;
 
         private ImmunosuppressionTreatmentForm(String id, IModel<ImmunosuppressionTreatment> model, final List<Component> componentsToUpdate) {
             super(id, model);
             RadarRequiredDateTextField startDate = new RadarRequiredDateTextField("startDate", this, componentsToUpdate);
             add(startDate);
 
             RadarRequiredDropdownChoice immunoSuppression = new RadarRequiredDropdownChoice("immunosuppression",
                     immunosuppressionManager.getImmunosuppressions(), new ChoiceRenderer("description", "id"),
                     this, componentsToUpdate);
 
             final Label totalDoseLabel = new Label("totalDoseLabel", "Total dose of course in g") {
                 @Override
                 public boolean isVisible() {
                     if (ImmunosuppressionTreatmentForm.this.getModelObject() != null) {
                         return ImmunosuppressionTreatmentForm.this.getModelObject().getImmunosuppression() != null ?
                                 ImmunosuppressionTreatmentForm.this.getModelObject().getImmunosuppression().getId().
                                         equals(CYCLOPHOSPHAMIDE_ID) : false;
                     }
 
                     return false;
                 }
             };
             add(totalDoseLabel);
 
             final TextField cyclophosphamideTotalDose = new RadarTextFieldWithValidation("cyclophosphamideTotalDose",
                    new RangeValidator<Double>(0.01, 9.99), true, this, componentsToUpdate) {
                 @Override
                 public boolean isVisible() {
                     if (ImmunosuppressionTreatmentForm.this.getModelObject() != null) {
                         return ImmunosuppressionTreatmentForm.this.getModelObject().getImmunosuppression() != null ?
                                 ImmunosuppressionTreatmentForm.this.getModelObject().getImmunosuppression().getId().
                                         equals(CYCLOPHOSPHAMIDE_ID) :
                                 false;
                     }
 
                     return false;
                 }
             };
             add(cyclophosphamideTotalDose);
 
             totalDoseLabel.setOutputMarkupId(true);
             totalDoseLabel.setOutputMarkupPlaceholderTag(true);
             cyclophosphamideTotalDose.setOutputMarkupId(true);
             cyclophosphamideTotalDose.setOutputMarkupPlaceholderTag(true);
 
             immunoSuppression.add(new AjaxFormComponentUpdatingBehavior("onChange") {
                 @Override
                 protected void onUpdate(AjaxRequestTarget target) {
                     target.add(totalDoseLabel);
                     target.add(cyclophosphamideTotalDose);
                 }
             });
 
             add(immunoSuppression);
 
             endDate = new RadarDateTextField("endDate", this, componentsToUpdate);
             add(endDate);
 
             startDate.setOutputMarkupId(true);
             startDate.setOutputMarkupPlaceholderTag(true);
             endDate.setOutputMarkupPlaceholderTag(true);
             endDate.setOutputMarkupId(true);
             immunoSuppression.setOutputMarkupId(true);
             immunoSuppression.setOutputMarkupPlaceholderTag(true);
             cyclophosphamideTotalDose.setOutputMarkupId(true);
             cyclophosphamideTotalDose.setOutputMarkupPlaceholderTag(true);
 
             componentsToUpdate.add(startDate);
             componentsToUpdate.add(endDate);
             componentsToUpdate.add(immunoSuppression);
             componentsToUpdate.add(cyclophosphamideTotalDose);
             componentsToUpdate.add(totalDoseLabel);
 
             FeedbackPanel treatmentFeedback = new FeedbackPanel("immunosupressionFeedback",
                     new IFeedbackMessageFilter() {
                         public boolean accept(FeedbackMessage feedbackMessage) {
                             if (TreatmentManager.ERROR_MESSAGES.contains(feedbackMessage.getMessage())) {
                                 return true;
                             }
                             return false;
                         }
                     });
 
             add(treatmentFeedback);
             treatmentFeedback.setOutputMarkupPlaceholderTag(true);
             componentsToUpdate.add(treatmentFeedback);
         }
 
         @Override
         protected void onValidateModelObjects() {
             super.onValidateModelObjects();
             ImmunosuppressionTreatment treatment = getModelObject();
             Date start = treatment.getStartDate();
             Date end = treatment.getEndDate();
             if (start != null && end != null && start.compareTo(end) != -1) {
                 endDate.error("End date cannot be less than start date");
             }
         }
     }
 }
