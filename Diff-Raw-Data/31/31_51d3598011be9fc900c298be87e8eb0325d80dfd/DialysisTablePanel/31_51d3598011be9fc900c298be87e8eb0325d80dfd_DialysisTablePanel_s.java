 package com.solidstategroup.radar.web.panels.tables;
 
 import com.solidstategroup.radar.model.Treatment;
 import com.solidstategroup.radar.model.exception.InvalidModelException;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.TreatmentManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.RadarSecuredSession;
 import com.solidstategroup.radar.web.behaviours.RadarBehaviourFactory;
 import com.solidstategroup.radar.web.components.RadarDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDropdownChoice;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
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
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class DialysisTablePanel extends Panel {
     @SpringBean
     private TreatmentManager treatmentManager;
 
     public DialysisTablePanel(String id, final IModel<Long> radarNumberModel) {
         super(id);
 
         final IModel dialysisListModel = new AbstractReadOnlyModel<List>() {
             @Override
             public List getObject() {
                 if (radarNumberModel.getObject() != null) {
                     return treatmentManager.getTreatmentsByRadarNumber(radarNumberModel.getObject());
                 }
                 return Collections.emptyList();
             }
         };
 
         final WebMarkupContainer dialysisContainer = new WebMarkupContainer("dialysisContainer");
         add(dialysisContainer);
 
         final List<Component> addDialysisFormComponentsToUpdate = new ArrayList<Component>();
         final List<Component> editDialysisFormComponentsToUpdate = new ArrayList<Component>();
 
         final IModel editDialysisModel = new Model<Treatment>();
 
         // Edit dialysis container
         final MarkupContainer editDialysisContainer = new WebMarkupContainer("editDialysisContainer") {
             @Override
             public boolean isVisible() {
                 return editDialysisModel.getObject() != null;
             }
         };
         editDialysisContainer.setOutputMarkupPlaceholderTag(true);
         editDialysisContainer.setOutputMarkupPlaceholderTag(true);
         add(editDialysisContainer);
 
         // Dialysis
         ListView<Treatment> dialysisListView = new ListView<Treatment>("dialysis", dialysisListModel) {
             @Override
             protected void populateItem(final ListItem<Treatment> item) {
                 item.setModel(new CompoundPropertyModel<Treatment>(item.getModelObject()));
                 item.add(new Label("treatmentModality.description"));
                 item.add(DateLabel.forDatePattern("startDate", RadarApplication.DATE_PATTERN));
                 item.add(DateLabel.forDatePattern("endDate", RadarApplication.DATE_PATTERN));
                 AjaxLink ajaxDeleteLink = new AjaxLink("deleteLink") {
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         Treatment treatment = item.getModelObject();
                         treatmentManager.deleteTreatment(treatment);
                         target.add(addDialysisFormComponentsToUpdate.toArray(new Component[
                                 addDialysisFormComponentsToUpdate.size()]));
                         target.add(dialysisContainer);
 
                     }
                 };
                 item.add(ajaxDeleteLink);
                 ajaxDeleteLink.add(RadarBehaviourFactory.getDeleteConfirmationBehaviour());
                 AjaxLink ajaxEditLink = new AjaxLink("editLink") {
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         editDialysisModel.setObject(item.getModelObject());
                         target.add(editDialysisContainer);
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
             }
         };
         dialysisContainer.setOutputMarkupId(true);
         dialysisContainer.setOutputMarkupPlaceholderTag(true);
         dialysisContainer.add(dialysisListView);
 
         DialysisForm editDialysisForm =
                 new DialysisForm("editDialysisForm", new CompoundPropertyModel<Treatment>(editDialysisModel),
                         editDialysisFormComponentsToUpdate);
         editDialysisForm.add(new AjaxSubmitLink("save") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.add(editDialysisContainer);
                 target.add(dialysisContainer);
                 try {
                     treatmentManager.saveTreatment((Treatment) form.getModelObject());
                 } catch (InvalidModelException e) {
                     for (String error : e.getErrors()) {
                         error(error);
                     }
                     return;
                 }
                 form.getModel().setObject(null);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(editDialysisFormComponentsToUpdate.toArray(new Component[
                         editDialysisFormComponentsToUpdate.size()]));
             }
         });
         editDialysisForm.add(new AjaxLink("cancel") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 editDialysisModel.setObject(null);
                 target.add(editDialysisContainer);
             }
         });
         editDialysisContainer.add(editDialysisForm);
 
 
         // Add dialysis form
         DialysisForm addDialysisForm =
                 new DialysisForm("addDialysisForm", new CompoundPropertyModel<Treatment>(new Treatment()),
                         addDialysisFormComponentsToUpdate);
         addDialysisForm.add(new AjaxSubmitLink("save") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 target.add(addDialysisFormComponentsToUpdate.toArray(new Component[
                         addDialysisFormComponentsToUpdate.size()]));
                 target.add(dialysisContainer);
                 Treatment treatment = (Treatment) form.getModelObject();
                 treatment.setRadarNumber(radarNumberModel.getObject());
                 try {
                     treatmentManager.saveTreatment(treatment);
                 } catch (InvalidModelException e) {
                     for (String error : e.getErrors()) {
                         error(error);
                     }
                     return;
                 }
 
                 form.getModel().setObject(new Treatment());
                 dialysisContainer.setVisible(true);
 
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(addDialysisFormComponentsToUpdate
                         .toArray(new Component[addDialysisFormComponentsToUpdate.size()]));
             }
         });
         add(addDialysisForm);
     }
 
     private final class DialysisForm extends Form<Treatment> {
         private RadarDateTextField endDate;
         @SpringBean
         private TreatmentManager treatmentManager;
 
         private DialysisForm(String id, IModel<Treatment> treatmentIModel, List<Component> componentsToUpdate) {
             super(id, treatmentIModel);
 
             RadarRequiredDropdownChoice treatmentModality = new RadarRequiredDropdownChoice("treatmentModality",
                     treatmentManager.getTreatmentModalities(),new ChoiceRenderer("description", "id"), this,
                     componentsToUpdate);
             add(treatmentModality);
 
             RadarRequiredDateTextField startDate = new RadarRequiredDateTextField("startDate", this, componentsToUpdate);
             add(startDate);
             endDate = new RadarDateTextField("endDate", this, componentsToUpdate);
             add(endDate);
 
             treatmentModality.setOutputMarkupId(true);
             treatmentModality.setOutputMarkupPlaceholderTag(true);
 
             startDate.setOutputMarkupPlaceholderTag(true);
             startDate.setOutputMarkupPlaceholderTag(true);
 
             endDate.setOutputMarkupPlaceholderTag(true);
             endDate.setOutputMarkupPlaceholderTag(true);
 
             componentsToUpdate.add(treatmentModality);
             componentsToUpdate.add(startDate);
             componentsToUpdate.add(endDate);
 
             FeedbackPanel dialysisFeedback = new FeedbackPanel("dialysisFeedback",
                     new IFeedbackMessageFilter() {
                         public boolean accept(FeedbackMessage feedbackMessage) {
                             return TreatmentManager.ERROR_MESSAGES.contains(feedbackMessage.getMessage());
                         }
                     });
 
             add(dialysisFeedback);
             dialysisFeedback.setOutputMarkupPlaceholderTag(true);
             componentsToUpdate.add(dialysisFeedback);
         }
 
         @Override
         protected void onValidateModelObjects() {
             super.onValidateModelObjects();
             Treatment treatment = getModelObject();
             Date start = treatment.getStartDate();
             Date end = treatment.getEndDate();
             if (start != null && end != null && start.compareTo(end) != -1) {
                endDate.error("End date cannot be less than start date");
             }
         }
     }
 
 }
