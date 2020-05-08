 /* Ara - capture species and specimen data
  * 
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */ 
 package org.inbio.ara.web.specimen;
 
 import com.sun.data.provider.RowKey;
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Body;
 import com.sun.webui.jsf.component.Button;
 import com.sun.webui.jsf.component.Checkbox;
 import com.sun.webui.jsf.component.Form;
 import com.sun.webui.jsf.component.Hyperlink;
 import com.sun.webui.jsf.component.ImageComponent;
 import com.sun.webui.jsf.component.Label;
 import com.sun.webui.jsf.component.Link;
 import com.sun.webui.jsf.component.Page;
 import com.sun.webui.jsf.component.Table;
 import com.sun.webui.jsf.component.TableColumn;
 import com.sun.webui.jsf.component.TableRowGroup;
 import com.sun.webui.jsf.model.DefaultTableDataProvider;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlMessages;
 import javax.faces.context.FacesContext;
 import org.inbio.ara.web.ApplicationBean1;
 import org.inbio.ara.web.AraApplicationBean;
 import org.inbio.ara.web.AraRequestBean;
 import org.inbio.ara.web.RequestBean1;
 import org.inbio.ara.web.SessionBean1;
 import org.inbio.ara.web.SessionManager;
 import org.inbio.ara.web.admin.institution.InstitutionSessionBean;
 import org.inbio.ara.web.admin.person.PersonSessionBean;
 import org.inbio.ara.web.admin.profile.ProfileSessionBean;
 import org.inbio.ara.web.audience.AudienceSessionBean;
 import org.inbio.ara.web.gathering.GatheringDetailSessionBean;
 import org.inbio.ara.web.gathering.GatheringSessionBeanV2;
 import org.inbio.ara.web.gathering.SpecimenGenerationSessionBean;
 import org.inbio.ara.web.group.GroupSessionBean;
 import org.inbio.ara.web.identification.IdentificationSearchSessionBean;
 import org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean;
 import org.inbio.ara.web.references.ReferenceSessionBean;
 import org.inbio.ara.web.site.SiteSessionBean;
 import org.inbio.ara.web.species.SpeciesSessionBean;
 import org.inbio.ara.web.user.UserSessionBean;
 import org.inbio.ara.web.util.BundleHelper;
 import org.inbio.ara.web.util.MessageBean;
 import org.inbio.ara.web.util.SelectionListBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  */
 public class SpecimenList extends AbstractPageBean {
 	
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
     private int __placeholder;
     
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     
     private Page page1 = new Page();
     
     public Page getPage1() {
         return page1;
     }
     
     public void setPage1(Page p) {
         this.page1 = p;
     }
     
     private Link link1 = new Link();
     
     public Link getLink1() {
         return link1;
     }
     
     public void setLink1(Link l) {
         this.link1 = l;
     }
     
     private Body body1 = new Body();
     
     public Body getBody1() {
         return body1;
     }
     
     public void setBody1(Body b) {
         this.body1 = b;
     }
     
     private Form form1 = new Form();
     
     public Form getForm1() {
         return form1;
     }
     
     public void setForm1(Form f) {
         this.form1 = f;
     }
 
     private ImageComponent image1 = new ImageComponent();
 
     public ImageComponent getImage1() {
         return image1;
     }
 
     public void setImage1(ImageComponent ic) {
         this.image1 = ic;
     }
 
     private Label label1 = new Label();
 
     public Label getLabel1() {
         return label1;
     }
 
     public void setLabel1(Label l) {
         this.label1 = l;
     }
 
     private Table specimenTable = new Table();
 
     public Table getSpecimenTable() {
         return specimenTable;
     }
 
     public void setSpecimenTable(Table t) {
         this.specimenTable = t;
     }
 
     private TableRowGroup tableRowGroup1 = new TableRowGroup();
 
     public TableRowGroup getTableRowGroup1() {
         return tableRowGroup1;
     }
 
     public void setTableRowGroup1(TableRowGroup trg) {
         this.tableRowGroup1 = trg;
     }
 
     private DefaultTableDataProvider defaultTableDataProvider = new DefaultTableDataProvider();
 
     public DefaultTableDataProvider getDefaultTableDataProvider() {
         return defaultTableDataProvider;
     }
 
     public void setDefaultTableDataProvider(DefaultTableDataProvider dtdp) {
         this.defaultTableDataProvider = dtdp;
     }
 
     private Button btn_edit = new Button();
 
     public Button getBtn_edit() {
         return btn_edit;
     }
 
     public void setBtn_edit(Button b) {
         this.btn_edit = b;
     }
 
     private HtmlMessages messageList1 = new HtmlMessages();
 
     public HtmlMessages getMessageList1() {
         return messageList1;
     }
 
     public void setMessageList1(HtmlMessages hm) {
         this.messageList1 = hm;
     }
 
     private Hyperlink hlink_showWelcome1 = new Hyperlink();
 
     public Hyperlink getHlink_showWelcome1() {
         return hlink_showWelcome1;
     }
 
     public void setHlink_showWelcome1(Hyperlink h) {
         this.hlink_showWelcome1 = h;
     }
     
     // </editor-fold>
     
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public SpecimenList() {
     }
     
     /**
      * <p>Callback method that is called whenever a page is navigated to,
      * either directly via a URL, or indirectly via page navigation.
      * Customize this method to acquire resources that will be needed
      * for event handlers and lifecycle methods, whether or not this
      * page is performing post back processing.</p>
      *
      * <p>Note that, if the current request is a postback, the property
      * values of the components do <strong>not</strong> represent any
      * values submitted with this request.  Instead, they represent the
      * property values that were saved for this view when it was rendered.</p>
      */
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("Module specimenList Initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
     }
     
     /**
      * <p>Callback method that is called after the component tree has been
      * restored, but before any event processing takes place.  This method
      * will <strong>only</strong> be called on a postback request that
      * is processing a form submit.  Customize this method to allocate
      * resources that will be required in your event handlers.</p>
      */
     public void preprocess() {
     }
     
     /**
      * <p>Callback method that is called just before rendering takes place.
      * This method will <strong>only</strong> be called for the page that
      * will actually be rendered (and not, for example, on a page that
      * handled a postback and then navigated to a different page).  Customize
      * this method to allocate resources that will be required for rendering
      * this page.</p>
      */
     @Override
     public void prerender() {
        if(this.getspecimen$SpecimenSessionBean().getPaginationInventory() == null)
             this.getspecimen$SpecimenSessionBean().initDataProvider();
     }
     
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called (regardless of whether
      * or not this was the page that was actually rendered).  Customize this
      * method to release resources acquired in the <code>init()</code>,
      * <code>preprocess()</code>, or <code>prerender()</code> methods (or
      * acquired during execution of an event handler).</p>
      */
     public void destroy() {
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
         return (SpecimenSessionBean)getBean("specimen$SpecimenSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GatheringDetailSessionBean getgathering$GatheringDetailSessionBean() {
         return (GatheringDetailSessionBean)getBean("gathering$GatheringDetailSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SpeciesSessionBean getspecies$SpeciesSessionBean() {
         return (SpeciesSessionBean)getBean("species$SpeciesSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected AraRequestBean getAraRequestBean() {
         return (AraRequestBean)getBean("AraRequestBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected AudienceSessionBean getaudience$AudienceSessionBean() {
         return (AudienceSessionBean)getBean("audience$AudienceSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected MessageBean getutil$MessageBean() {
         return (MessageBean)getBean("util$MessageBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SessionManager getSessionManager() {
         return (SessionManager)getBean("SessionManager");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected ProfileSessionBean getadmin$profile$ProfileSessionBean() {
         return (ProfileSessionBean)getBean("admin$profile$ProfileSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected RequestBean1 getRequestBean1() {
         return (RequestBean1)getBean("RequestBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected InstitutionSessionBean getadmin$institution$InstitutionSessionBean() {
         return (InstitutionSessionBean)getBean("admin$institution$InstitutionSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SessionBean1 getSessionBean1() {
         return (SessionBean1)getBean("SessionBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GroupSessionBean getgroup$GroupSessionBean() {
         return (GroupSessionBean)getBean("group$GroupSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected PersonSessionBean getadmin$person$PersonSessionBean() {
         return (PersonSessionBean)getBean("admin$person$PersonSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected AraApplicationBean getAraApplicationBean() {
         return (AraApplicationBean)getBean("AraApplicationBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected ReferenceSessionBean getreferences$ReferenceSessionBean() {
         return (ReferenceSessionBean)getBean("references$ReferenceSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected ApplicationBean1 getApplicationBean1() {
         return (ApplicationBean1)getBean("ApplicationBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SelectionListBean getutil$SelectionListBean() {
         return (SelectionListBean)getBean("util$SelectionListBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SpecimenGenerationSessionBean getgathering$SpecimenGenerationSessionBean() {
         return (SpecimenGenerationSessionBean)getBean("gathering$SpecimenGenerationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GatheringSessionBeanV2 getgathering$GatheringSessionBeanV2() {
         return (GatheringSessionBeanV2)getBean("gathering$GatheringSessionBeanV2");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected UserSessionBean getuser$UserSessionBean() {
         return (UserSessionBean)getBean("user$UserSessionBean");
     }
 
     public String btn_edit_action() {
         RowKey[] selectedRowKeys = getTableRowGroup1().getSelectedRowKeys();
         if (selectedRowKeys.length == 1) {
             this.getspecimen$SpecimenSessionBean().setCurrentSpecimen(selectedRowKeys[0]);
             this.getspecimen$SpecimenSessionBean().setInMultipleEdition(false);
             return "edit";
         } else if(selectedRowKeys.length == 0) {
             info(BundleHelper.getDefaultBundleValue("not_selected"));
             return null; // Not Yet Implemented
         } else {  // Multiple edition
             //this.getspecimen$SpecimenSessionBean().setSelectedSpecimens(selectedRowKeys);
             //this.getspecimen$SpecimenSessionBean().setInMultipleEdition(true);
             info(BundleHelper.getDefaultBundleValue("not_yet"));
             return null; // Not Yet Implemented
         }
     }
     
     public String btn_search_action() {        
         return "search_specimen";
     }
     
     public String btn_reload_action() {
         getspecimen$SpecimenSessionBean().setIsFiltered(false);
        getspecimen$SpecimenSessionBean().getPaginationInventory().getDataProvider().clearObjectList();
        //getspecimen$SpecimenSessionBean().getSpecimenDataProvider().clearObjectList();
        //getspecimen$SpecimenSessionBean().getSpecimenDataProvider().refreshDataList();
         return null;
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SiteSessionBean getsite$SiteSessionBean() {
         return (SiteSessionBean)getBean("site$SiteSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected IdentificationSearchSessionBean getidentification$IdentificationSearchSessionBean() {
         return (IdentificationSearchSessionBean)getBean("identification$IdentificationSearchSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected NomenclaturalGroupSessionBean getnomenclaturalgroup$NomenclaturalGroupSessionBean() {
         return (NomenclaturalGroupSessionBean)getBean("nomenclaturalgroup$NomenclaturalGroupSessionBean");
     }
 
     public String englishLink_action() {
         FacesContext context = FacesContext.getCurrentInstance();
         context.getViewRoot().setLocale(Locale.ENGLISH);
         return null;
     }
 
     public String spanishLink_action() {
         FacesContext context = FacesContext.getCurrentInstance();
         Locale l = new Locale("ES");
         context.getViewRoot().setLocale(l);
         return null;
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.identification.IdentificationSessionBean getidentification$IdentificationSessionBean() {
         return (org.inbio.ara.web.identification.IdentificationSessionBean)getBean("identification$IdentificationSessionBean");
     }
 
     public String hlink_exit1_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public String hlink_showWelcome_action() {
         return "cancel";
     }
 }
 
