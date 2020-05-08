 package org.patientview.radar.web.pages.patient.hnf1b;
 
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
import org.patientview.radar.model.Demographics;
import org.patientview.radar.model.generic.AddPatientModel;
import org.patientview.radar.model.user.User;
import org.patientview.radar.service.DemographicsManager;
import org.patientview.radar.web.behaviours.RadarBehaviourFactory;
import org.patientview.radar.web.pages.BasePage;
import org.patientview.radar.web.panels.NonAlportGeneticsPanel;
import org.patientview.radar.web.panels.generic.GenericDemographicsPanel;
import org.patientview.radar.web.panels.generic.MedicalResultsPanel;
import org.patientview.radar.web.panels.hnf1b.HNF1BMiscPanel;
import org.patientview.radar.web.visitors.PatientFormVisitor;
 
 @AuthorizeInstantiation({User.ROLE_PROFESSIONAL, User.ROLE_SUPER_USER})
 public class HNF1BPatientPage extends BasePage {
 
     public enum Tab {
         // Used for storing the current tab
         DEMOGRAPHICS(1),
         GENETICS(2),
         PROTEINURIA(3),
         HNF1BMisc(4),
         MEDICAL_RESULTS(5);
 
         private int pageNumber;
 
         Tab(int pageNumber) {
             this.pageNumber = pageNumber;
         }
 
         public int getPageNumber() {
             return pageNumber;
         }
     }
 
     protected static final String PARAM_ID = "id";
 
     @SpringBean
     private DemographicsManager demographicsManager;
 
     private Demographics demographics;
     private MarkupContainer linksContainer;
 
     // The panels we are using
     private GenericDemographicsPanel genericDemographicsPanel;
    private NonAlportGeneticsPanel geneticsPanel;
     private HNF1BMiscPanel hnf1BMiscPanel;
     private MedicalResultsPanel medicalResultsPanel;
 
     private Tab currentTab = Tab.DEMOGRAPHICS;
 
     public HNF1BPatientPage(AddPatientModel patientModel) {
         // set the nhs id or chi id based on model
         demographics = new Demographics();
         demographics.setDiseaseGroup(patientModel.getDiseaseGroup());
         demographics.setRenalUnit(patientModel.getCentre());
         demographics.setNhsNumber(patientModel.getPatientId());
         demographics.setNhsNumberType(patientModel.getNhsNumberType());
 
         init(demographics);
     }
 
     public HNF1BPatientPage(PageParameters pageParameters) {
         // this constructor is used when a patient exists
         demographics = demographicsManager.getDemographicsByRadarNumber(pageParameters.get("id").toLong());
         init(demographics);
     }
 
     public void init(Demographics demographics) {
         // init all the panels
         genericDemographicsPanel = new GenericDemographicsPanel("demographicsPanel", demographics) {
             @Override
             public boolean isVisible() {
                 return currentTab.equals(Tab.DEMOGRAPHICS);
             }
         };
         add(genericDemographicsPanel);
 
        geneticsPanel = new NonAlportGeneticsPanel("geneticsPanel", demographics) {
             @Override
             public boolean isVisible() {
                 return currentTab.equals(Tab.GENETICS);
             }
         };
         add(geneticsPanel);
 
         hnf1BMiscPanel = new HNF1BMiscPanel("hnf1BMiscPanel", demographics) {
             @Override
             public boolean isVisible() {
                 return currentTab.equals(Tab.HNF1BMisc);
             }
         };
         add(hnf1BMiscPanel);
 
         medicalResultsPanel = new MedicalResultsPanel("medicalResultsPanel", demographics) {
             @Override
             public boolean isVisible() {
                 return currentTab.equals(Tab.MEDICAL_RESULTS);
             }
         };
 
         medicalResultsPanel.setOutputMarkupPlaceholderTag(true);
 
         add(medicalResultsPanel);
 
         // Add a container for the links to update the highlighted tab
         linksContainer = new WebMarkupContainer("linksContainer");
         linksContainer.setOutputMarkupId(true);
         add(linksContainer);
 
         // Add the links to switch tab
         linksContainer.add(new TabAjaxLink("demographicsLink", Tab.DEMOGRAPHICS));
         linksContainer.add(new TabAjaxLink("geneticsLink", Tab.GENETICS));
         linksContainer.add(new TabAjaxLink("hnf1BMiscLink", Tab.HNF1BMisc));
         linksContainer.add(new TabAjaxLink("medicalResultsLink", Tab.MEDICAL_RESULTS));
 
         IModel<Integer> pageNumberModel = new Model<Integer>();
         pageNumberModel.setObject(Tab.DEMOGRAPHICS.getPageNumber());
 
         Label pageNumber = new Label("pageNumber", pageNumberModel);
         pageNumber.setOutputMarkupPlaceholderTag(true);
         add(pageNumber);
 
         visitChildren(new PatientFormVisitor());
         add(RadarBehaviourFactory.getWarningOnPatientPageExitBehaviour());
     }
 
     public static PageParameters getPageParameters(Demographics demographics) {
         return new PageParameters().set(PARAM_ID, demographics.getId());
     }
 
     public Tab getCurrentTab() {
         return currentTab;
     }
 
     private class TabAjaxLink extends AjaxLink {
         private Tab tab;
 
         public TabAjaxLink(String id, Tab tab) {
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
             if (demographics != null && demographics.hasValidId()) {
                 currentTab = tab;
                 // Add the links container to update hover class
                 target.add(linksContainer);
 
                 // add each panel to the response
                 target.add(genericDemographicsPanel, geneticsPanel, hnf1BMiscPanel, medicalResultsPanel);
 
                 Component pageNumber = getPage().get("pageNumber");
                 IModel pageNumberModel = pageNumber.getDefaultModel();
                 pageNumberModel.setObject(HNF1BPatientPage.this.currentTab.getPageNumber());
                 target.add(pageNumber);
             }
         }
 
         @Override
         protected IAjaxCallDecorator getAjaxCallDecorator() {
             return RadarBehaviourFactory.getWarningOnFormExitCallDecorator();
         }
     }
 }
