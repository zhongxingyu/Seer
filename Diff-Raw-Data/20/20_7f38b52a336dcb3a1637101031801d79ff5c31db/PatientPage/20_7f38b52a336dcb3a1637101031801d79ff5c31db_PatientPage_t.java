 package com.solidstategroup.radar.web.pages;
 
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.behaviours.RadarBehaviourFactory;
 import com.solidstategroup.radar.web.components.RadarDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.models.PageNumberModel;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import com.solidstategroup.radar.web.panels.DemographicsPanel;
 import com.solidstategroup.radar.web.panels.DiagnosisPanel;
 import com.solidstategroup.radar.web.panels.FirstVisitPanel;
 import com.solidstategroup.radar.web.panels.FollowUpPanel;
 import com.solidstategroup.radar.web.panels.HospitalisationPanel;
 import com.solidstategroup.radar.web.panels.PathologyPanel;
 import com.solidstategroup.radar.web.panels.RelapsePanel;
 import com.solidstategroup.radar.web.validators.RadarDateValidator;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.IAjaxCallDecorator;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.string.StringValue;
 import org.apache.wicket.util.visit.IVisit;
 import org.apache.wicket.util.visit.IVisitor;
 
 @AuthorizeInstantiation({User.ROLE_PROFESSIONAL, User.ROLE_SUPER_USER})
 public class PatientPage extends BasePage {
 
     protected static final String PARAM_ID = "id";
     @SpringBean
     private DiagnosisManager diagnosisManager;
     @SpringBean
     private DemographicsManager demographicsManager;
 
     public enum CurrentTab {
         // Used for storing the current tab
         DEMOGRAPHICS(RadarApplication.DEMOGRAPHICS_PAGE_NO),
         DIAGNOSIS(RadarApplication.DIAGNOSIS_PAGE_NO),
         FIRST_VISIT(RadarApplication.CLINICAL_FIRST_VISIT_PAGE_NO),
         FOLLOW_UP(RadarApplication.CLINICAL_FOLLOW_UP_PAGE_NO),
         PATHOLOGY(RadarApplication.PATHOLOGY_PAGE_NO),
         RELAPSE(RadarApplication.RELAPSE_PAGE_NO),
         HOSPITALISATION(RadarApplication.HOSPITALISATION_PAGE_NO);
 
         private int pageNumber;
 
         CurrentTab(int pageNumber) {
             this.pageNumber = pageNumber;
         }
 
         public int getPageNumber() {
             return pageNumber;
         }
     }
 
     private IModel<Long> radarNumberModel = new Model<Long>();
 
     private DemographicsPanel demographicsPanel;
     private DiagnosisPanel diagnosisPanel;
     private FirstVisitPanel firstVisitPanel;
     private FollowUpPanel followUpPanel;
     private PathologyPanel pathologyPanel;
     private RelapsePanel relapsePanel;
     private HospitalisationPanel hospitalisationPanel;
 
     private MarkupContainer linksContainer;
 
     private CurrentTab currentTab = CurrentTab.DEMOGRAPHICS;
 
     public PatientPage(PageParameters parameters) {
         super();
 
         // Get radar number from parameters - we might not have one for new patients
         StringValue idValue = parameters.get(PARAM_ID);
         if (!idValue.isEmpty()) {
             radarNumberModel.setObject(idValue.toLongObject());
         }
 
         // Construct panels for each of the tabs
         demographicsPanel = new DemographicsPanel("demographicsPanel", radarNumberModel);
         diagnosisPanel = new DiagnosisPanel("diagnosisPanel", radarNumberModel);
         firstVisitPanel = new FirstVisitPanel("firstVisitPanel", radarNumberModel);
         followUpPanel = new FollowUpPanel("followUpPanel", radarNumberModel);
         pathologyPanel = new PathologyPanel("pathologyPanel", radarNumberModel);
         relapsePanel = new RelapsePanel("relapsePanel", radarNumberModel);
         hospitalisationPanel = new HospitalisationPanel("hospitalisationPanel", radarNumberModel);
 
         // Add them all to the page
         add(demographicsPanel, diagnosisPanel, firstVisitPanel, followUpPanel, pathologyPanel, relapsePanel,
                 hospitalisationPanel);
 
         // Add a container for the links to update the highlighted tab
         linksContainer = new WebMarkupContainer("linksContainer");
         linksContainer.setOutputMarkupId(true);
 
         // Add the links to switch tab
         TabAjaxLink demographicsLink = new TabAjaxLink("demographicsLink", CurrentTab.DEMOGRAPHICS);
         linksContainer.add(demographicsLink);
         linksContainer.add(new TabAjaxLink("diagnosisLink", CurrentTab.DIAGNOSIS));
         linksContainer.add(new TabAjaxLink("firstVisitLink", CurrentTab.FIRST_VISIT));
         linksContainer.add(new TabAjaxLink("followUpLink", CurrentTab.FOLLOW_UP));
         linksContainer.add(new TabAjaxLink("pathologyLink", CurrentTab.PATHOLOGY));
         linksContainer.add(new TabAjaxLink("relapseLink", CurrentTab.RELAPSE));
         linksContainer.add(new TabAjaxLink("hospitalisationLink", CurrentTab.HOSPITALISATION));
         add(linksContainer);
 
         IModel pageNumberModel = RadarModelFactory.getPageNumberModel(RadarApplication.DEMOGRAPHICS_PAGE_NO,
                 radarNumberModel, diagnosisManager);
 
         Label pageNumber = new Label("pageNumber", pageNumberModel);
         pageNumber.setOutputMarkupPlaceholderTag(true);
         add(pageNumber);
 
 
         visitChildren(new IVisitor<Component, Object>() {
             public void component(Component component, IVisit<Object> objectIVisit) {
                 //add onkeyup event to date to santise input - tried attaching behaviour in the componenet class itself
                 // but did not work
                 if (component instanceof RadarDateTextField || component instanceof RadarRequiredDateTextField) {
                     component.add(new AttributeModifier("onkeyup", "radarUtility.sanitiseDateInput(this);"));
                 }
 
                 // add validator to date components - adding it inside the component constructor does not work
                 if (component instanceof RadarDateTextField || component instanceof RadarRequiredDateTextField) {
                     component.add(new RadarDateValidator());
                 }
 
                 //if form component - mark form as dirty onchange
                 if (component instanceof FormComponent || component instanceof Radio) {
                     // ignore the subform components
                     String[] ignoreParents = {"immunosuppression", "plasmapheresispanel", "dialysiscontainer",
                            "transplantscontainer", "rejectDataContainer", "editTransplantContainer", "addTransplantForm"};

                    // ignore recored switchers
                    String[] ignoreIds = {"switcher"};

                     boolean ignoreComponent = false;
                    for (String ignore : ignoreParents) {
                        if (component.getPath().toLowerCase().contains(ignore.toLowerCase())) {
                            ignoreComponent = true;
                            break;
                        }
                    }
                    for (String ignore : ignoreIds) {
                        if (component.getId().toLowerCase().contains(ignore.toLowerCase())) {
                             ignoreComponent = true;
                             break;
                         }
                     }
                    if (!ignoreComponent) {
                         component.add(new AttributeAppender("onchange", RadarApplication.FORM_IS_DIRTY_TRUE_SCRIPT));
                     }
                 }
 
             }
 
         });
 
         add(RadarBehaviourFactory.getWarningOnPatientPageExitBehaviour());
     }
 
     public CurrentTab getCurrentTab() {
         return currentTab;
     }
 
     private class TabAjaxLink extends AjaxLink {
         private CurrentTab tab;
 
         public TabAjaxLink(String id, CurrentTab tab) {
             super(id);
             this.tab = tab;
 
             // Decorate span with class="hovered" if we're active tab
             MarkupContainer span = new WebMarkupContainer("span");
             span.add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
                 @Override
                 public String getObject() {
                     return currentTab.equals(TabAjaxLink.this.tab) ? "hovered" : "";
                 }
             }));
             add(span);
         }
 
         @Override
         public void onClick(AjaxRequestTarget target) {
             if (radarNumberModel.getObject() != null) {
                 currentTab = tab;
                 // Add the links container to update hover class
                 target.add(linksContainer);
                 target.add(demographicsPanel, diagnosisPanel, firstVisitPanel, followUpPanel, pathologyPanel,
                         relapsePanel, hospitalisationPanel);
 
                 Component pageNumber = getPage().get("pageNumber");
                 PageNumberModel pageNumberModel = (PageNumberModel) pageNumber.getDefaultModel();
 
                 // if a tab has sub tabs then get the current selected page number of the sub tab
                 if (currentTab.equals(CurrentTab.FIRST_VISIT)) {
                     pageNumberModel.setPageNumber(firstVisitPanel.getCurrentTab().getPageNumber());
                 } else if (currentTab.equals(CurrentTab.FOLLOW_UP)) {
                     pageNumberModel.setPageNumber(followUpPanel.getCurrentTab().getPageNumber());
                 } else {
                     pageNumberModel.setPageNumber(currentTab.getPageNumber());
                 }
 
                 if (currentTab.equals(CurrentTab.FOLLOW_UP)) {
                     getPage().get("followUpPanel:clinicalPicturePanel:clinicalPicturePanel:clinicalPictureContainer").
                             setVisible(false);
                 }
 
                 target.add(pageNumber);
             }
 
         }
 
         @Override
         protected IAjaxCallDecorator getAjaxCallDecorator() {
             return RadarBehaviourFactory.getWarningOnFormExitCallDecorator();
         }
     }
 
     public static PageParameters getParameters(Demographics demographics) {
         return new PageParameters().set(PARAM_ID, demographics.getId());
     }
 
     public IModel<Long> getRadarNumberModel() {
         return radarNumberModel;
     }
 }
