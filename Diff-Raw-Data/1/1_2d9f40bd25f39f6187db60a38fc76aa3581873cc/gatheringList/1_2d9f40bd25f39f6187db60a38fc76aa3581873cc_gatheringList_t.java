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
 /*
  * gatheringList.java
  *
  * Created on May 10, 2008, 11:25 AM
  * Copyright roaguilar
  */
 package org.inbio.ara.web.gathering;
 
 import com.sun.data.provider.RowKey;
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Body;
 import com.sun.webui.jsf.component.Button;
 import com.sun.webui.jsf.component.Form;
 import com.sun.webui.jsf.component.Head;
 import com.sun.webui.jsf.component.Html;
 import com.sun.webui.jsf.component.Hyperlink;
 import com.sun.webui.jsf.component.ImageComponent;
 import com.sun.webui.jsf.component.Label;
 import com.sun.webui.jsf.component.Link;
 import com.sun.webui.jsf.component.Page;
 import com.sun.webui.jsf.component.StaticText;
 import com.sun.webui.jsf.component.Table;
 import com.sun.webui.jsf.component.TableColumn;
 import com.sun.webui.jsf.component.TableRowGroup;
 import javax.faces.FacesException;
 import javax.faces.component.UIColumn;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.component.html.HtmlMessages;
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
 import org.inbio.ara.web.group.GroupSessionBean;
 import org.inbio.ara.web.identification.IdentificationSearchSessionBean;
 import org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean;
 import org.inbio.ara.web.references.ReferenceSessionBean;
 import org.inbio.ara.web.species.SpeciesSessionBean;
 import org.inbio.ara.web.specimen.SpecimenSessionBean;
 import org.inbio.ara.web.user.UserSessionBean;
 import org.inbio.ara.web.util.MessageBean;
 import org.inbio.ara.web.util.SelectionListBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  */
 public class gatheringList extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
     private int __placeholder;
     
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
 		gatheringTable.setStyle("left: 0px; top: 216px; position: absolute; width: 100%");
     }
     
     private Page page1 = new Page();
     
     public Page getPage1() {
         return page1;
     }
     
     public void setPage1(Page p) {
         this.page1 = p;
     }
     
     private Html html1 = new Html();
     
     public Html getHtml1() {
         return html1;
     }
     
     public void setHtml1(Html h) {
         this.html1 = h;
     }
     
     private Head head1 = new Head();
     
     public Head getHead1() {
         return head1;
     }
     
     public void setHead1(Head h) {
         this.head1 = h;
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
 
     private HtmlMessages messageList1 = new HtmlMessages();
 
     public HtmlMessages getMessageList1() {
         return messageList1;
     }
 
     public void setMessageList1(HtmlMessages hm) {
         this.messageList1 = hm;
     }
 
     private Label label1 = new Label();
 
     public Label getLabel1() {
         return label1;
     }
 
     public void setLabel1(Label l) {
         this.label1 = l;
     }
 
     private Table gatheringTable = new Table();
 
     public Table getGatheringTable() {
         return gatheringTable;
     }
 
     public void setGatheringTable(Table t) {
         this.gatheringTable = t;
     }
 
     private TableRowGroup tableRowGroup1 = new TableRowGroup();
 
     public TableRowGroup getTableRowGroup1() {
         return tableRowGroup1;
     }
 
     public void setTableRowGroup1(TableRowGroup trg) {
         this.tableRowGroup1 = trg;
     }
 
     private TableColumn tableColumn1 = new TableColumn();
 
     public TableColumn getTableColumn1() {
         return tableColumn1;
     }
 
     public void setTableColumn1(TableColumn tc) {
         this.tableColumn1 = tc;
     }
 
     private StaticText staticText1 = new StaticText();
 
     public StaticText getStaticText1() {
         return staticText1;
     }
 
     public void setStaticText1(StaticText st) {
         this.staticText1 = st;
     }
 /*
     private Button btn_search = new Button();
 
     public Button getBtn_search() {
         return btn_search;
     }
 
     public void setBtn_search(Button b) {
         this.btn_search = b;
     }
 */
     private TableColumn tableColumn2 = new TableColumn();
 
     public TableColumn getTableColumn2() {
         return tableColumn2;
     }
 
     public void setTableColumn2(TableColumn tc) {
         this.tableColumn2 = tc;
     }
 
     private StaticText staticText2 = new StaticText();
 
     public StaticText getStaticText2() {
         return staticText2;
     }
 
     public void setStaticText2(StaticText st) {
         this.staticText2 = st;
     }
 
     private TableColumn tableColumn3 = new TableColumn();
 
     public TableColumn getTableColumn3() {
         return tableColumn3;
     }
 
     public void setTableColumn3(TableColumn tc) {
         this.tableColumn3 = tc;
     }
 
     private StaticText staticText3 = new StaticText();
 
     public StaticText getStaticText3() {
         return staticText3;
     }
 
     public void setStaticText3(StaticText st) {
         this.staticText3 = st;
     }
 
     private TableColumn tableColumn4 = new TableColumn();
 
     public TableColumn getTableColumn4() {
         return tableColumn4;
     }
 
     public void setTableColumn4(TableColumn tc) {
         this.tableColumn4 = tc;
     }
 
     private StaticText staticText4 = new StaticText();
 
     public StaticText getStaticText4() {
         return staticText4;
     }
 
     public void setStaticText4(StaticText st) {
         this.staticText4 = st;
     }
 
     private TableColumn tableColumn5 = new TableColumn();
 
     public TableColumn getTableColumn5() {
         return tableColumn5;
     }
 
     public void setTableColumn5(TableColumn tc) {
         this.tableColumn5 = tc;
     }
 
     private StaticText staticText5 = new StaticText();
 
     public StaticText getStaticText5() {
         return staticText5;
     }
 
     public void setStaticText5(StaticText st) {
         this.staticText5 = st;
     }
 
     private TableColumn tableColumn7 = new TableColumn();
 
     public TableColumn getTableColumn7() {
         return tableColumn7;
     }
 
     public void setTableColumn7(TableColumn tc) {
         this.tableColumn7 = tc;
     }
 
     private StaticText staticText7 = new StaticText();
 
     public StaticText getStaticText7() {
         return staticText7;
     }
 
     public void setStaticText7(StaticText st) {
         this.staticText7 = st;
     }
 
     private TableColumn tableColumn6 = new TableColumn();
 
     public TableColumn getTableColumn6() {
         return tableColumn6;
     }
 
     public void setTableColumn6(TableColumn tc) {
         this.tableColumn6 = tc;
     }
 
     private Button btn_edit = new Button();
 
     public Button getBtn_edit() {
         return btn_edit;
     }
 
     public void setBtn_edit(Button b) {
         this.btn_edit = b;
     }
 
     private Button btn_remove = new Button();
 
     public Button getBtn_remove() {
         return btn_remove;
     }
 
     public void setBtn_remove(Button b) {
         this.btn_remove = b;
     }
 
     private Button btn_new1 = new Button();
 
     public Button getBtn_new1() {
         return btn_new1;
     }
 
     public void setBtn_new1(Button b) {
         this.btn_new1 = b;
     }
     
     // </editor-fold>
     
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public gatheringList() {
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
             log("gatheringList Initialization Failure", e);
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
     public void prerender() {
         if(this.getGatheringSessionBean().getPagination() == null){
             this.getGatheringSessionBean().initDataProvider();
         }
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
     protected AudienceSessionBean getaudience$AudienceSessionBean() {
         return (AudienceSessionBean)getBean("audience$AudienceSessionBean");
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
     protected SessionManager getSessionManager() {
         return (SessionManager)getBean("SessionManager");
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
     protected UserSessionBean getuser$UserSessionBean() {
         return (UserSessionBean)getBean("user$UserSessionBean");
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
     protected PersonSessionBean getadmin$person$PersonSessionBean() {
         return (PersonSessionBean)getBean("admin$person$PersonSessionBean");
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
     protected AraApplicationBean getAraApplicationBean() {
         return (AraApplicationBean)getBean("AraApplicationBean");
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
     protected SessionBean1 getSessionBean1() {
         return (SessionBean1)getBean("SessionBean1");
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
     protected ProfileSessionBean getadmin$profile$ProfileSessionBean() {
         return (ProfileSessionBean)getBean("admin$profile$ProfileSessionBean");
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
     protected ReferenceSessionBean getreferences$ReferenceSessionBean() {
         return (ReferenceSessionBean)getBean("references$ReferenceSessionBean");
     }
 
     public String hlink_exit1_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public String hlink_showWelcome_action() {
         return "cancel";
     }
 
     public String btn_edit_action() {
         /*
         RowKey rowKey = this.tableRowGroup1.getRowKey();
         this.getgathering$GatheringSessionBeanV2().setEditMode(false);
         this.getgathering$GatheringSessionBeanV2().setSelectedGathering(rowKey);
         this.getgathering$GatheringSessionBeanV2().populateList();
         return "editGathering";
          */
         if (this.getSessionManager().canModify()!=null) {
             RowKey rowKey = this.tableRowGroup1.getRowKey();
             this.getGatheringSessionBean().setEditMode(false);
             this.getGatheringSessionBean().setSelectedGathering(rowKey);
             this.getGatheringSessionBean().populateList();
             return this.getSessionManager().canModify();
         } else {
             this.getutil$MessageBean().addCantModifyMessage();
             return null;
         }
     }
 
     public String btn_new_action() {
         if (this.getSessionManager().canAdd() != null) {
             return this.getSessionManager().canAdd();
         } else {
             this.getutil$MessageBean().addCantAddMessage();
             return null;
         }
     }    
 
     public String btn_search_action() {        
         return "search";
     }
 
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GatheringSessionBeanV2 getGatheringSessionBean() {
         return (GatheringSessionBeanV2)getBean("gathering$GatheringSessionBeanV2");
     }
 
     public String btn_remove_action() {
         /*
         RowKey rowKey = this.tableRowGroup1.getRowKey();
         this.getgathering$GatheringSessionBeanV2().setSelectedGathering(rowKey);
         this.getgathering$GatheringSessionBeanV2().delete();
         return null;
          */
         if (this.getSessionManager().canDelete() != null) {
             RowKey rowKey = this.tableRowGroup1.getRowKey();
             this.getGatheringSessionBean().setSelectedGathering(rowKey);
             this.getGatheringSessionBean().delete();
             return null;
         } else {
             this.getutil$MessageBean().addCantDeleteMessage();
             return null;
         }
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
     protected GatheringDetailSessionBean getgathering$GatheringDetailSessionBean() {
         return (GatheringDetailSessionBean)getBean("gathering$GatheringDetailSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.gathering.SpecimenGenerationSessionBean getgathering$SpecimenGenerationSessionBean() {
         return (org.inbio.ara.web.gathering.SpecimenGenerationSessionBean)getBean("gathering$SpecimenGenerationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.identification.IdentificationSessionBean getidentification$IdentificationSessionBean() {
         return (org.inbio.ara.web.identification.IdentificationSessionBean)getBean("identification$IdentificationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.site.SiteSessionBean getsite$SiteSessionBean() {
         return (org.inbio.ara.web.site.SiteSessionBean)getBean("site$SiteSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected NomenclaturalGroupSessionBean getnomenclaturalgroup$NomenclaturalGroupSessionBean() {
         return (NomenclaturalGroupSessionBean)getBean("nomenclaturalgroup$NomenclaturalGroupSessionBean");
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
     protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
         return (SpecimenSessionBean)getBean("specimen$SpecimenSessionBean");
     }
 }
 
