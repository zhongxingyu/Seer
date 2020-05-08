 package org.patientview.radar.web.pages.patient.srns;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.IAjaxCallDecorator;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.patientview.model.Patient;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.model.DiagnosisCode;
 import org.patientview.radar.model.generic.AddPatientModel;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.DiagnosisManager;
 import org.patientview.radar.service.PatientManager;
 import org.patientview.radar.web.RadarApplication;
 import org.patientview.radar.web.behaviours.RadarBehaviourFactory;
 import org.patientview.radar.web.models.PageNumberModel;
 import org.patientview.radar.web.models.RadarModelFactory;
 import org.patientview.radar.web.pages.BasePage;
 import org.patientview.radar.web.panels.DemographicsPanel;
 import org.patientview.radar.web.panels.DiagnosisPanel;
 import org.patientview.radar.web.panels.FirstVisitPanel;
 import org.patientview.radar.web.panels.FollowUpPanel;
 import org.patientview.radar.web.panels.HospitalisationPanel;
 import org.patientview.radar.web.panels.PathologyPanel;
 import org.patientview.radar.web.panels.RelapsePanel;
 import org.patientview.radar.web.visitors.PatientFormVisitor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @AuthorizeInstantiation({User.ROLE_PROFESSIONAL, User.ROLE_SUPER_USER})
 public class SrnsPatientPage extends BasePage implements PatientCallBack {
 
     protected static final String PARAM_ID = "id";
 
     @SpringBean
     private DiagnosisManager diagnosisManager;
 
     @SpringBean
     private PatientManager patientManager;
 
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
 
     private List<Component> componentsToUpdate = new ArrayList<Component>();
 
     private IModel<Patient> patientModel = new Model<Patient>();
 
 
     public SrnsPatientPage(){
         patientModel.setObject(new Patient());
     }
 
 
     public SrnsPatientPage(PageParameters pageParameters) {
         super();
 
         // this constructor is used when a patient exists
         patientModel.setObject(patientManager.getPatientByRadarNumber(pageParameters.get("id").toLong()));
         demographicsPanel = new DemographicsPanel("demographicsPanel", patientModel, this) ;
         radarNumberModel.setObject(patientModel.getObject().getRadarNo());
         init();
     }
 
     public SrnsPatientPage(Patient patient) {
         patientModel.setObject(patient);
         demographicsPanel = new DemographicsPanel("demographicsPanel", patientModel, this) ;
         patientModel.setObject(patient);
         init();
 
     }
 
     public void updateModel(Long radarNumber) {
         this.radarNumberModel.setObject(radarNumber);
     }
 
      public void init() {
 
         diagnosisPanel = new DiagnosisPanel("diagnosisPanel", radarNumberModel);
         firstVisitPanel = new FirstVisitPanel("firstVisitPanel", radarNumberModel);
         followUpPanel = new FollowUpPanel("followUpPanel", radarNumberModel);
         pathologyPanel = new PathologyPanel("pathologyPanel", radarNumberModel);
         relapsePanel = new RelapsePanel("relapsePanel", radarNumberModel);
         hospitalisationPanel = new HospitalisationPanel("hospitalisationPanel", radarNumberModel);
 
         componentsToUpdate.add(diagnosisPanel);
 
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
 
         // edit form behaviour
         visitChildren(new PatientFormVisitor());
 
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
 //            if (radarNumberModel.getObject() != null) {
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
 
             target.add(pageNumber);
             //            }
 
         }
 
         @Override
         protected IAjaxCallDecorator getAjaxCallDecorator() {
             return RadarBehaviourFactory.getWarningOnFormExitCallDecorator();
         }
     }
 
     public static PageParameters getParameters(Patient patient) {
        return new PageParameters().set(PARAM_ID, patient.getRadarNo());
     }
 
     public static PageParameters getParameters(AddPatientModel patientModel) {
         PageParameters pageParameters = new PageParameters();
         if (patientModel.getDiseaseGroup().getId().equals(DiseaseGroup.SRNS_DISEASE_GROUP_ID)) {
             pageParameters.set("diagnosis", DiagnosisCode.SRNS_ID);
         } else if (patientModel.getDiseaseGroup().getId().equals(DiseaseGroup.MPGN_DISEASEGROUP_ID)) {
             pageParameters.set("diagnosis", DiagnosisCode.MPGN_ID);
         }
 
         pageParameters.set("renalUnitId", patientModel.getCentre().getId());
         pageParameters.set("diseaseGroupId", patientModel.getDiseaseGroup().getId());
         pageParameters.set("idType", patientModel.getNhsNumberType().toString());
         pageParameters.set("idVal", patientModel.getPatientId());
         return pageParameters;
     }
 
     public IModel<Long> getRadarNumberModel() {
         return radarNumberModel;
     }
 }
