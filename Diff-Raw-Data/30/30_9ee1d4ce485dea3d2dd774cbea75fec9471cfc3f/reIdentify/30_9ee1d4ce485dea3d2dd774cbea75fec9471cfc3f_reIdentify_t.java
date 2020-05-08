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
  * reIdentify.java
  *
  * Created on June 24, 2008, 8:16 PM
  * Copyright roaguilar
  */
 package org.inbio.ara.web.identification;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.AddRemove;
 import com.sun.webui.jsf.component.Body;
 import com.sun.webui.jsf.component.Button;
 import com.sun.webui.jsf.component.Calendar;
 import com.sun.webui.jsf.component.Checkbox;
 import com.sun.webui.jsf.component.DropDown;
 import com.sun.webui.jsf.component.Form;
 import com.sun.webui.jsf.component.Head;
 import com.sun.webui.jsf.component.Html;
 import com.sun.webui.jsf.component.Hyperlink;
 import com.sun.webui.jsf.component.Label;
 import com.sun.webui.jsf.component.Link;
 import com.sun.webui.jsf.component.Listbox;
 import com.sun.webui.jsf.component.Page;
 import com.sun.webui.jsf.component.PanelLayout;
 import com.sun.webui.jsf.component.RadioButtonGroup;
 import com.sun.webui.jsf.component.StaticText;
 import com.sun.webui.jsf.component.Tab;
 import com.sun.webui.jsf.component.TabSet;
 import com.sun.webui.jsf.component.Table;
 import com.sun.webui.jsf.component.TableColumn;
 import com.sun.webui.jsf.component.TableRowGroup;
 import com.sun.webui.jsf.component.TextField;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlMessages;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.LongConverter;
 import javax.faces.event.ValueChangeEvent;
 import org.inbio.ara.facade.specimen.LifeStageSexSimple;
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
 import org.inbio.ara.web.references.ReferenceSessionBean;
 import org.inbio.ara.web.site.SiteSessionBean;
 import org.inbio.ara.web.species.SpeciesSessionBean;
 import org.inbio.ara.web.specimen.SpecimenSessionBean;
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
 public class reIdentify extends AbstractPageBean {
 
 	
 	public static final String ADD_ELEMENT_FAILED = BundleHelper.getDefaultBundleValue("add_element_failed");
 	public static final String CHOOSE_SEX = BundleHelper.getDefaultBundleValue("choose_sex");
 	public static final String CHOOSE_STAGE = BundleHelper.getDefaultBundleValue("choose_stage");
 	public static final String WITHOUT_GATHERING_DETAILS = BundleHelper.getDefaultBundleValue("without_gathering_details");
 	public static final String EMPTY_IDENTIFICATION_DATE = BundleHelper.getDefaultBundleValue("empty_identification_date");
 	public static final String EMPTY_SPECIMEN_NUMBER = BundleHelper.getDefaultBundleValue("empty_specimen_number");
 	public static final String EMPTY_STATUS_IDENTIFICATION = BundleHelper.getDefaultBundleValue("empty_status_identification");
 	public static final String EMTPY_TAXON_LIST = BundleHelper.getDefaultBundleValue("emtpy_taxon_list");
 	public static final String INVALID_SEARCH_OPTION = BundleHelper.getDefaultBundleValue("invalid_search_option");
 	public static final String SINONYM_IDENTIFICATIONS_NOT_ALLOWED = BundleHelper.getDefaultBundleValue("sinonym_identifications_not_allowed");
 	public static final String THE_COLLECTION = BundleHelper.getDefaultBundleValue("the_collection");
 	private static final String MANY_IDENTIFICATIONS_NOT_ALLOWED = BundleHelper.getDefaultBundleValue("many_identifications_not_allowed");
 	private static final String DUPLICATED_DATA = BundleHelper.getDefaultBundleValue("duplicated_data");
 	private static final String SPECIMEN_ACCOUNT_REQUIRED = BundleHelper.getDefaultBundleValue("specimen_account_required");
 	private static final String STAGE_AMOUNT_NOT_NUMERIC = BundleHelper.getDefaultBundleValue("stage_amount_not_numeric");
 	private static final String STAGE_AMOUNT_OUT_OF_RANGE = BundleHelper.getDefaultBundleValue("stage_amount_out_of_range");
 	private int __placeholder;
 
 	/**
 	 * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
 	 * This method is automatically generated, so any user-specified code inserted
 	 * here is subject to being replaced.</p>
 	 */
 	private void _init() throws Exception {
 		radioButtonGroup1DefaultOptions.setOptions(new com.sun.webui.jsf.model.Option[]{new com.sun.webui.jsf.model.Option("searchByTaxon", "Tax\u00f3n"), new com.sun.webui.jsf.model.Option("searchByGathering", "Recolecci\u00f3n"), new com.sun.webui.jsf.model.Option("searchByGatheringDetail", "Detalle de recolecci\u00f3n")});
        t_stadiumSex1.setWidth("384");
        t_stadiumSex1.setStyle("height: 42px; left: 312px; top: 48px; position: absolute");
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
 
 	private Label label1 = new Label();
 
 	public Label getLabel1() {
 		return label1;
 	}
 
 	public void setLabel1(Label l) {
 		this.label1 = l;
 	}
 
 	private HtmlMessages messageList1 = new HtmlMessages();
 
 	public HtmlMessages getMessageList1() {
 		return messageList1;
 	}
 
 	public void setMessageList1(HtmlMessages hm) {
 		this.messageList1 = hm;
 	}
 
 	private TextField txt_specimenId = new TextField();
 
 	public TextField getTxt_specimenId() {
 		return txt_specimenId;
 	}
 
 	public void setTxt_specimenId(TextField tf) {
 		this.txt_specimenId = tf;
 	}
 
 	private Listbox lst_specimen = new Listbox();
 
 	public Listbox getLst_specimen() {
 		return lst_specimen;
 	}
 
 	public void setLst_specimen(Listbox l) {
 		this.lst_specimen = l;
 	}
 
 	private TabSet tab_Identification = new TabSet();
 
 	public TabSet getTab_Identification() {
 		return tab_Identification;
 	}
 
 	public void setTab_Identification(TabSet ts) {
 		this.tab_Identification = ts;
 	}
 
 	private Tab tab_reIdentify = new Tab();
 
 	public Tab getTab_reIdentify() {
 		return tab_reIdentify;
 	}
 
 	public void setTab_reIdentify(Tab t) {
 		this.tab_reIdentify = t;
 	}
 
 	private Tab tab_actualInformation = new Tab();
 
 	public Tab getTab_actualInformation() {
 		return tab_actualInformation;
 	}
 
 	public void setTab_actualInformation(Tab t) {
 		this.tab_actualInformation = t;
 	}
 
 	private Tab tab_lifeStageSex = new Tab();
 
 	public Tab getTab_lifeStageSex() {
 		return tab_lifeStageSex;
 	}
 
 	public void setTab_lifeStageSex(Tab t) {
 		this.tab_lifeStageSex = t;
 	}
 
 	private PanelLayout layoutPanel1 = new PanelLayout();
 
 	public PanelLayout getLayoutPanel1() {
 		return layoutPanel1;
 	}
 
 	public void setLayoutPanel1(PanelLayout pl) {
 		this.layoutPanel1 = pl;
 	}
 
 	private Tab tab_identification = new Tab();
 
 	public Tab getTab_identification() {
 		return tab_identification;
 	}
 
 	public void setTab_identification(Tab t) {
 		this.tab_identification = t;
 	}
 
 	private PanelLayout layoutPanel2 = new PanelLayout();
 
 	public PanelLayout getLayoutPanel2() {
 		return layoutPanel2;
 	}
 
 	public void setLayoutPanel2(PanelLayout pl) {
 		this.layoutPanel2 = pl;
 	}
 
 	private Tab tab_identifier = new Tab();
 
 	public Tab getTab_identifier() {
 		return tab_identifier;
 	}
 
 	public void setTab_identifier(Tab t) {
 		this.tab_identifier = t;
 	}
 
 	private PanelLayout layoutPanel3 = new PanelLayout();
 
 	public PanelLayout getLayoutPanel3() {
 		return layoutPanel3;
 	}
 
 	public void setLayoutPanel3(PanelLayout pl) {
 		this.layoutPanel3 = pl;
 	}
 
 	private Table t_stadiumSex1 = new Table();
 
 	public Table getT_stadiumSex1() {
 		return t_stadiumSex1;
 	}
 
 	public void setT_stadiumSex1(Table t) {
 		this.t_stadiumSex1 = t;
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
 
 	private Button btn_removeStadiumSex1 = new Button();
 
 	public Button getBtn_removeStadiumSex1() {
 		return btn_removeStadiumSex1;
 	}
 
 	public void setBtn_removeStadiumSex1(Button b) {
 		this.btn_removeStadiumSex1 = b;
 	}
 
 	private DropDown dd_stadium1 = new DropDown();
 
 	public DropDown getDd_stadium1() {
 		return dd_stadium1;
 	}
 
 	public void setDd_stadium1(DropDown dd) {
 		this.dd_stadium1 = dd;
 	}
 
 	private DropDown dd_sex1 = new DropDown();
 
 	public DropDown getDd_sex1() {
 		return dd_sex1;
 	}
 
 	public void setDd_sex1(DropDown dd) {
 		this.dd_sex1 = dd;
 	}
 
 	private Label label2 = new Label();
 
 	public Label getLabel2() {
 		return label2;
 	}
 
 	public void setLabel2(Label l) {
 		this.label2 = l;
 	}
 
 	private Label label3 = new Label();
 
 	public Label getLabel3() {
 		return label3;
 	}
 
 	public void setLabel3(Label l) {
 		this.label3 = l;
 	}
 
 	private Label label4 = new Label();
 
 	public Label getLabel4() {
 		return label4;
 	}
 
 	public void setLabel4(Label l) {
 		this.label4 = l;
 	}
 
 	private Button btn_addStadiumSex1 = new Button();
 
 	public Button getBtn_addStadiumSex1() {
 		return btn_addStadiumSex1;
 	}
 
 	public void setBtn_addStadiumSex1(Button b) {
 		this.btn_addStadiumSex1 = b;
 	}
 
 	private TextField txt_ssQuantity1 = new TextField();
 
 	public TextField getTxt_ssQuantity1() {
 		return txt_ssQuantity1;
 	}
 
 	public void setTxt_ssQuantity1(TextField tf) {
 		this.txt_ssQuantity1 = tf;
 	}
 
 	private DropDown dd_identificationType1 = new DropDown();
 
 	public DropDown getDd_identificationType1() {
 		return dd_identificationType1;
 	}
 
 	public void setDd_identificationType1(DropDown dd) {
 		this.dd_identificationType1 = dd;
 	}
 
 	private DropDown dd_taxonomicalCategory1 = new DropDown();
 
 	public DropDown getDd_taxonomicalCategory1() {
 		return dd_taxonomicalCategory1;
 	}
 
 	public void setDd_taxonomicalCategory1(DropDown dd) {
 		this.dd_taxonomicalCategory1 = dd;
 	}
 
 	private Label label5 = new Label();
 
 	public Label getLabel5() {
 		return label5;
 	}
 
 	public void setLabel5(Label l) {
 		this.label5 = l;
 	}
 
 	private DropDown dd_validator1 = new DropDown();
 
 	public DropDown getDd_validator1() {
 		return dd_validator1;
 	}
 
 	public void setDd_validator1(DropDown dd) {
 		this.dd_validator1 = dd;
 	}
 
 	private Label label6 = new Label();
 
 	public Label getLabel6() {
 		return label6;
 	}
 
 	public void setLabel6(Label l) {
 		this.label6 = l;
 	}
 
 	private Label label7 = new Label();
 
 	public Label getLabel7() {
 		return label7;
 	}
 
 	public void setLabel7(Label l) {
 		this.label7 = l;
 	}
 	private Calendar cal_identificationDate1 = new Calendar();
 
 	public Calendar getCal_identificationDate1() {
 		return cal_identificationDate1;
 	}
 
 	public void setCal_identificationDate1(Calendar c) {
 		this.cal_identificationDate1 = c;
 	}
 	private Label label8 = new Label();
 
 	public Label getLabel8() {
 		return label8;
 	}
 
 	public void setLabel8(Label l) {
 		this.label8 = l;
 	}
 	private DropDown dd_identificationStatus1 = new DropDown();
 
 	public DropDown getDd_identificationStatus1() {
 		return dd_identificationStatus1;
 	}
 
 	public void setDd_identificationStatus1(DropDown dd) {
 		this.dd_identificationStatus1 = dd;
 	}
 	private AddRemove ad_taxonList1 = new AddRemove();
 
 	public AddRemove getAd_taxonList1() {
 		return ad_taxonList1;
 	}
 
 	public void setAd_taxonList1(AddRemove ar) {
 		this.ad_taxonList1 = ar;
 	}
 	private Label label9 = new Label();
 
 	public Label getLabel9() {
 		return label9;
 	}
 
 	public void setLabel9(Label l) {
 		this.label9 = l;
 	}
 	private Label label10 = new Label();
 
 	public Label getLabel10() {
 		return label10;
 	}
 
 	public void setLabel10(Label l) {
 		this.label10 = l;
 	}
 	private Label label11 = new Label();
 
 	public Label getLabel11() {
 		return label11;
 	}
 
 	public void setLabel11(Label l) {
 		this.label11 = l;
 	}
 	private DropDown dd_taxonomicalRange1 = new DropDown();
 
 	public DropDown getDd_taxonomicalRange1() {
 		return dd_taxonomicalRange1;
 	}
 
 	public void setDd_taxonomicalRange1(DropDown dd) {
 		this.dd_taxonomicalRange1 = dd;
 	}
 	private AddRemove ar_identifier1 = new AddRemove();
 
 	public AddRemove getAr_identifier1() {
 		return ar_identifier1;
 	}
 
 	public void setAr_identifier1(AddRemove ar) {
 		this.ar_identifier1 = ar;
 	}
 	private Label label12 = new Label();
 
 	public Label getLabel12() {
 		return label12;
 	}
 
 	public void setLabel12(Label l) {
 		this.label12 = l;
 	}
 	private Button btn_taxonomySearch = new Button();
 
 	public Button getBtn_taxonomySearch() {
 		return btn_taxonomySearch;
 	}
 
 	public void setBtn_taxonomySearch(Button b) {
 		this.btn_taxonomySearch = b;
 	}
 	private Label label13 = new Label();
 
 	public Label getLabel13() {
 		return label13;
 	}
 
 	public void setLabel13(Label l) {
 		this.label13 = l;
 	}
 	private RadioButtonGroup radioButtonGroup1 = new RadioButtonGroup();
 
 	public RadioButtonGroup getRadioButtonGroup1() {
 		return radioButtonGroup1;
 	}
 
 	public void setRadioButtonGroup1(RadioButtonGroup rbg) {
 		this.radioButtonGroup1 = rbg;
 	}
 	private SingleSelectOptionsList radioButtonGroup1DefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getRadioButtonGroup1DefaultOptions() {
 		return radioButtonGroup1DefaultOptions;
 	}
 
 	public void setRadioButtonGroup1DefaultOptions(SingleSelectOptionsList ssol) {
 		this.radioButtonGroup1DefaultOptions = ssol;
 	}
 	private Label label14 = new Label();
 
 	public Label getLabel14() {
 		return label14;
 	}
 
 	public void setLabel14(Label l) {
 		this.label14 = l;
 	}
 	private StaticText st_specimenId = new StaticText();
 
 	public StaticText getSt_specimenId() {
 		return st_specimenId;
 	}
 
 	public void setSt_specimenId(StaticText st) {
 		this.st_specimenId = st;
 	}
 	private Table table_actualIdentification1 = new Table();
 
 	public Table getTable_actualIdentification1() {
 		return table_actualIdentification1;
 	}
 
 	public void setTable_actualIdentification1(Table t) {
 		this.table_actualIdentification1 = t;
 	}
 	private TableRowGroup tableRowGroup2 = new TableRowGroup();
 
 	public TableRowGroup getTableRowGroup2() {
 		return tableRowGroup2;
 	}
 
 	public void setTableRowGroup2(TableRowGroup trg) {
 		this.tableRowGroup2 = trg;
 	}
 	private TableColumn tableColumn6 = new TableColumn();
 
 	public TableColumn getTableColumn6() {
 		return tableColumn6;
 	}
 
 	public void setTableColumn6(TableColumn tc) {
 		this.tableColumn6 = tc;
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
 	private StaticText staticText6 = new StaticText();
 
 	public StaticText getStaticText6() {
 		return staticText6;
 	}
 
 	public void setStaticText6(StaticText st) {
 		this.staticText6 = st;
 	}
 	private TableColumn tableColumn8 = new TableColumn();
 
 	public TableColumn getTableColumn8() {
 		return tableColumn8;
 	}
 
 	public void setTableColumn8(TableColumn tc) {
 		this.tableColumn8 = tc;
 	}
 	private StaticText staticText7 = new StaticText();
 
 	public StaticText getStaticText7() {
 		return staticText7;
 	}
 
 	public void setStaticText7(StaticText st) {
 		this.staticText7 = st;
 	}
 	private TableColumn tableColumn9 = new TableColumn();
 
 	public TableColumn getTableColumn9() {
 		return tableColumn9;
 	}
 
 	public void setTableColumn9(TableColumn tc) {
 		this.tableColumn9 = tc;
 	}
 	private StaticText staticText8 = new StaticText();
 
 	public StaticText getStaticText8() {
 		return staticText8;
 	}
 
 	public void setStaticText8(StaticText st) {
 		this.staticText8 = st;
 	}
 	private TableColumn tableColumn10 = new TableColumn();
 
 	public TableColumn getTableColumn10() {
 		return tableColumn10;
 	}
 
 	public void setTableColumn10(TableColumn tc) {
 		this.tableColumn10 = tc;
 	}
 	private StaticText staticText9 = new StaticText();
 
 	public StaticText getStaticText9() {
 		return staticText9;
 	}
 
 	public void setStaticText9(StaticText st) {
 		this.staticText9 = st;
 	}
 	private TableColumn tableColumn11 = new TableColumn();
 
 	public TableColumn getTableColumn11() {
 		return tableColumn11;
 	}
 
 	public void setTableColumn11(TableColumn tc) {
 		this.tableColumn11 = tc;
 	}
 	private StaticText staticText10 = new StaticText();
 
 	public StaticText getStaticText10() {
 		return staticText10;
 	}
 
 	public void setStaticText10(StaticText st) {
 		this.staticText10 = st;
 	}
 	private StaticText st_gatheringObservation = new StaticText();
 
 	public StaticText getSt_gatheringObservation() {
 		return st_gatheringObservation;
 	}
 
 	public void setSt_gatheringObservation(StaticText st) {
 		this.st_gatheringObservation = st;
 	}
 	private Label label15 = new Label();
 
 	public Label getLabel15() {
 		return label15;
 	}
 
 	public void setLabel15(Label l) {
 		this.label15 = l;
 	}
 	private Table t_stadiumSex2 = new Table();
 
 	public Table getT_stadiumSex2() {
 		return t_stadiumSex2;
 	}
 
 	public void setT_stadiumSex2(Table t) {
 		this.t_stadiumSex2 = t;
 	}
 	private TableRowGroup tableRowGroup3 = new TableRowGroup();
 
 	public TableRowGroup getTableRowGroup3() {
 		return tableRowGroup3;
 	}
 
 	public void setTableRowGroup3(TableRowGroup trg) {
 		this.tableRowGroup3 = trg;
 	}
 	private TableColumn tableColumn12 = new TableColumn();
 
 	public TableColumn getTableColumn12() {
 		return tableColumn12;
 	}
 
 	public void setTableColumn12(TableColumn tc) {
 		this.tableColumn12 = tc;
 	}
 	private StaticText staticText11 = new StaticText();
 
 	public StaticText getStaticText11() {
 		return staticText11;
 	}
 
 	public void setStaticText11(StaticText st) {
 		this.staticText11 = st;
 	}
 	private TableColumn tableColumn13 = new TableColumn();
 
 	public TableColumn getTableColumn13() {
 		return tableColumn13;
 	}
 
 	public void setTableColumn13(TableColumn tc) {
 		this.tableColumn13 = tc;
 	}
 	private StaticText staticText12 = new StaticText();
 
 	public StaticText getStaticText12() {
 		return staticText12;
 	}
 
 	public void setStaticText12(StaticText st) {
 		this.staticText12 = st;
 	}
 	private TableColumn tableColumn14 = new TableColumn();
 
 	public TableColumn getTableColumn14() {
 		return tableColumn14;
 	}
 
 	public void setTableColumn14(TableColumn tc) {
 		this.tableColumn14 = tc;
 	}
 	private StaticText staticText13 = new StaticText();
 
 	public StaticText getStaticText13() {
 		return staticText13;
 	}
 
 	public void setStaticText13(StaticText st) {
 		this.staticText13 = st;
 	}
 	private Checkbox chck_dataEntryError = new Checkbox();
 
 	public Checkbox getChck_dataEntryError() {
 		return chck_dataEntryError;
 	}
 
 	public void setChck_dataEntryError(Checkbox c) {
 		this.chck_dataEntryError = c;
 	}
 	private Button btn_reIdentify = new Button();
 
 	public Button getBtn_reIdentify() {
 		return btn_reIdentify;
 	}
 
 	public void setBtn_reIdentify(Button b) {
 		this.btn_reIdentify = b;
 	}
 	private Button btn_cancel = new Button();
 
 	public Button getBtn_cancel() {
 		return btn_cancel;
 	}
 
 	public void setBtn_cancel(Button b) {
 		this.btn_cancel = b;
 	}
 	private Button btn_add = new Button();
 
 	public Button getBtn_add() {
 		return btn_add;
 	}
 
 	public void setBtn_add(Button b) {
 		this.btn_add = b;
 	}
 	private LongConverter longConverter1 = new LongConverter();
 
 	public LongConverter getLongConverter1() {
 		return longConverter1;
 	}
 
 	public void setLongConverter1(LongConverter lc) {
 		this.longConverter1 = lc;
 	}
 	private Button btn_remove = new Button();
 
 	public Button getBtn_remove() {
 		return btn_remove;
 	}
 
 	public void setBtn_remove(Button b) {
 		this.btn_remove = b;
 	}
 	private Button btn_clean = new Button();
 
 	public Button getBtn_clean() {
 		return btn_clean;
 	}
 
 	public void setBtn_clean(Button b) {
 		this.btn_clean = b;
 	}
 	private Label label16 = new Label();
 
 	public Label getLabel16() {
 		return label16;
 	}
 
 	public void setLabel16(Label l) {
 		this.label16 = l;
 	}
 	private StaticText st_specimenCategory = new StaticText();
 
 	public StaticText getSt_specimenCategory() {
 		return st_specimenCategory;
 	}
 
 	public void setSt_specimenCategory(StaticText st) {
 		this.st_specimenCategory = st;
 	}
 	private Label label17 = new Label();
 
 	public Label getLabel17() {
 		return label17;
 	}
 
 	public void setLabel17(Label l) {
 		this.label17 = l;
 	}
 	private Label label18 = new Label();
 
 	public Label getLabel18() {
 		return label18;
 	}
 
 	public void setLabel18(Label l) {
 		this.label18 = l;
 	}
 	private DropDown dd_typeSpecimenType = new DropDown();
 
 	public DropDown getDd_typeSpecimenType() {
 		return dd_typeSpecimenType;
 	}
 
 	public void setDd_typeSpecimenType(DropDown dd) {
 		this.dd_typeSpecimenType = dd;
 	}
 	private DropDown dd_institution = new DropDown();
 
 	public DropDown getDd_institution() {
 		return dd_institution;
 	}
 
 	public void setDd_institution(DropDown dd) {
 		this.dd_institution = dd;
 	}
 	private Hyperlink spanishLink1 = new Hyperlink();
 
 	public Hyperlink getSpanishLink1() {
 		return spanishLink1;
 	}
 
 	public void setSpanishLink1(Hyperlink h) {
 		this.spanishLink1 = h;
 	}
 	private Hyperlink englishLink1 = new Hyperlink();
 
 	public Hyperlink getEnglishLink1() {
 		return englishLink1;
 	}
 
 	public void setEnglishLink1(Hyperlink h) {
 		this.englishLink1 = h;
 	}
 	// </editor-fold>
 	/**
 	 * <p>Construct a new Page bean instance.</p>
 	 */
 	public reIdentify() {
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
 			log("Module reIdentify Initialization Failure", e);
 			throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
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
 		getidentification$IdentificationSessionBean().populateOptions();
 		this.lst_specimen.setLabel(getidentification$IdentificationSessionBean().getSpecimenCount());
 		if (this.getidentification$IdentificationSessionBean().getSelectedSpecimenId() != null) {
 			setSelectedSpecimenInfo();
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
 	protected GatheringDetailSessionBean getgathering$GatheringDetailSessionBean() {
 		return (GatheringDetailSessionBean) getBean("gathering$GatheringDetailSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected MessageBean getutil$MessageBean() {
 		return (MessageBean) getBean("util$MessageBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected ReferenceSessionBean getreferences$ReferenceSessionBean() {
 		return (ReferenceSessionBean) getBean("references$ReferenceSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SessionBean1 getSessionBean1() {
 		return (SessionBean1) getBean("SessionBean1");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected GatheringSessionBeanV2 getgathering$GatheringSessionBeanV2() {
 		return (GatheringSessionBeanV2) getBean("gathering$GatheringSessionBeanV2");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected ProfileSessionBean getadmin$profile$ProfileSessionBean() {
 		return (ProfileSessionBean) getBean("admin$profile$ProfileSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected AraApplicationBean getAraApplicationBean() {
 		return (AraApplicationBean) getBean("AraApplicationBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected RequestBean1 getRequestBean1() {
 		return (RequestBean1) getBean("RequestBean1");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SessionManager getSessionManager() {
 		return (SessionManager) getBean("SessionManager");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SpecimenGenerationSessionBean getgathering$SpecimenGenerationSessionBean() {
 		return (SpecimenGenerationSessionBean) getBean("gathering$SpecimenGenerationSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected AudienceSessionBean getaudience$AudienceSessionBean() {
 		return (AudienceSessionBean) getBean("audience$AudienceSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected InstitutionSessionBean getadmin$institution$InstitutionSessionBean() {
 		return (InstitutionSessionBean) getBean("admin$institution$InstitutionSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected ApplicationBean1 getApplicationBean1() {
 		return (ApplicationBean1) getBean("ApplicationBean1");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected UserSessionBean getuser$UserSessionBean() {
 		return (UserSessionBean) getBean("user$UserSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected AraRequestBean getAraRequestBean() {
 		return (AraRequestBean) getBean("AraRequestBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected GroupSessionBean getgroup$GroupSessionBean() {
 		return (GroupSessionBean) getBean("group$GroupSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected PersonSessionBean getadmin$person$PersonSessionBean() {
 		return (PersonSessionBean) getBean("admin$person$PersonSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SpeciesSessionBean getspecies$SpeciesSessionBean() {
 		return (SpeciesSessionBean) getBean("species$SpeciesSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SelectionListBean getutil$SelectionListBean() {
 		return (SelectionListBean) getBean("util$SelectionListBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected org.inbio.ara.web.identification.IdentificationSessionBean getidentification$IdentificationSessionBean() {
 		return (org.inbio.ara.web.identification.IdentificationSessionBean) getBean("identification$IdentificationSessionBean");
 	}
 
 	private boolean validate() {
 		if (this.cal_identificationDate1.getSelectedDate() == null) {
 			this.getutil$MessageBean().addErrorMessage(EMPTY_IDENTIFICATION_DATE);
 			return false;
 		}
 
 		if (this.getidentification$IdentificationSessionBean().getSelectedIdentificationStatus().equals(-1L)) {
 			this.getutil$MessageBean().addErrorMessage(EMPTY_STATUS_IDENTIFICATION);
 			return false;
 		}
 
 		if (this.getidentification$IdentificationSessionBean().getSelectedTaxon() == null) {
 			this.getutil$MessageBean().addErrorMessage(EMTPY_TAXON_LIST);
 			return false;
 		}
 
 		if (this.getidentification$IdentificationSessionBean().getSelectedTaxon().length == 0) {
 			this.getutil$MessageBean().addErrorMessage(EMTPY_TAXON_LIST);
 			return false;
 		}
 
 		if (!this.getSessionManager().canIdentifyWithSynonyms()) {
 			if (this.getidentification$IdentificationSessionBean().getSelectedTaxonCategory().equals(3L)) {
 				this.getutil$MessageBean().addErrorMessage(SINONYM_IDENTIFICATIONS_NOT_ALLOWED);
 				return false;
 			}
 		}
 
 		// Validaciones para la categor�a de especimen "Observaci�n"
 		if (getidentification$IdentificationSessionBean().getCurrentSpecimenCategory().equals(1L)) {
 			// No se permite identificar con varios taxones un mismo especimen.
 			if (this.getidentification$IdentificationSessionBean().getSelectedTaxon() != null) {
 				if (this.getidentification$IdentificationSessionBean().getSelectedTaxon().length > 1) {
 					this.getutil$MessageBean().addErrorMessage(MANY_IDENTIFICATIONS_NOT_ALLOWED);
 					return false;
 				}
 			}
 		}
 
 		// Validaciones para la categor�a de especimen "Individual"
 		if (getidentification$IdentificationSessionBean().getCurrentSpecimenCategory().equals(2L)) {
 			// No se permite identificar con varios taxones un mismo especimen.
 			if (this.getidentification$IdentificationSessionBean().getSelectedTaxon() != null) {
 				if (this.getidentification$IdentificationSessionBean().getSelectedTaxon().length > 1) {
 					this.getutil$MessageBean().addErrorMessage(MANY_IDENTIFICATIONS_NOT_ALLOWED);
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public String btn_reIdentify_action() {
 		if (validate()) {
 			if (this.chck_dataEntryError.isChecked()) {
 				this.getidentification$IdentificationSessionBean().setDataEntryError(this.getChck_dataEntryError().getSelectedValue().toString());
 			}
 			this.getidentification$IdentificationSessionBean().reIdentify();
 			this.getidentification$IdentificationSessionBean().cleanParameters();
 			this.chck_dataEntryError.setSelected("n");
 		}
 		return null;
 	}
 
 	public String btn_removeStadiumSex1_action() {
 		LifeStageSexSimple object = (LifeStageSexSimple) this.getidentification$IdentificationSessionBean().getLifeStageSexSimpleDataProvider().getObject(this.tableRowGroup1.getRowKey());
 		this.getidentification$IdentificationSessionBean().setSelectedLifeStage(object.getLifeStage().getId());
 		this.getidentification$IdentificationSessionBean().setSelectedSex(object.getSex().getId());
 		this.txt_ssQuantity1.setValue(object.getQuantity());
 		this.getidentification$IdentificationSessionBean().getLifeStageSexSimpleDataProvider().removeElement(this.tableRowGroup1.getRowKey());
 		return null;
 	}
 
 	public String btn_addStadiumSex1_action() {
 		if (this.canAddLifeStageSex()) {
 			LifeStageSexSimple object = new LifeStageSexSimple();
 			object.setQuantity(Long.parseLong(txt_ssQuantity1.getValue().toString()));
 			object.setLifeStage(this.getutil$SelectionListBean().getLifeStageById(this.getidentification$IdentificationSessionBean().getSelectedLifeStage()));
 			object.setSex(this.getutil$SelectionListBean().getSexById(this.getidentification$IdentificationSessionBean().getSelectedSex()));
 			if (this.getidentification$IdentificationSessionBean().getLifeStageSexSimpleDataProvider().addElement(object) == false) {
 				this.getutil$MessageBean().addErrorMessage(DUPLICATED_DATA);
 			}
 		}
 		return null;
 	}
 
 	private boolean canAddLifeStageSex() {
 		/*
 		if (this.getgathering$SpecimenGenerationSessionBean().getSelectedSpecimenType().equals(-1L)) {
 		this.getutil$MessageBean().addErrorMessage("Seleccion antes un tipo de espec�men.");
 		return false;
 		}
 		 */
 
        //Validar si no hay ningun especimen seleccionado (current specimen)
        if(this.getidentification$IdentificationSessionBean().getCurrentSpecimenCategory() == null){
            MessageBean.setErrorMessageFromBundle("error_reidentify_lifestage_sex_table");
            return false;
        }

 		if ((this.getidentification$IdentificationSessionBean().getLifeStageSexSimpleDataProvider().getList().size() == 1) & (this.getidentification$IdentificationSessionBean().getCurrentSpecimenCategory().equals(2L))) {
			MessageBean.addErrorMessage(ADD_ELEMENT_FAILED);
 			return false;
 		}
 
 		if (this.getidentification$IdentificationSessionBean().getSelectedLifeStage() == -1L) {
			MessageBean.addErrorMessage(CHOOSE_STAGE);
 			return false;
 		}
 		if (this.getidentification$IdentificationSessionBean().getSelectedSex() == -1L) {
			MessageBean.addErrorMessage(CHOOSE_SEX);
 			return false;
 		}
 
 		if (this.txt_ssQuantity1.getValue() == null) {
			MessageBean.addErrorMessage(SPECIMEN_ACCOUNT_REQUIRED);
 			return false;
 		} else {
 			// Validaci�n de tipo de dato
 			//String value = (String)txt_ssQuantity1.getValue();
 			if (txt_ssQuantity1.getValue() != null) {
 				String value = txt_ssQuantity1.getValue().toString();
 				try {
 					Integer ssQuantity = Integer.parseInt(value);
 					if ((ssQuantity < 1) | (ssQuantity > 10000)) {
						MessageBean.addErrorMessage(STAGE_AMOUNT_OUT_OF_RANGE);
 						return false;
 					}
 				} catch (NumberFormatException ex) {
					MessageBean.addErrorMessage(STAGE_AMOUNT_NOT_NUMERIC);
 					return false;
 				}
 			} else {
				MessageBean.addErrorMessage(STAGE_AMOUNT_NOT_NUMERIC);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void dd_stadium1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_sex1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void txt_ssQuantity1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void cal_identificationDate1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_validator1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_identificationStatus1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_identificationType1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_taxonomicalRange1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public void dd_taxonomicalCategory1_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SiteSessionBean getsite$SiteSessionBean() {
 		return (SiteSessionBean) getBean("site$SiteSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean getnomenclaturalgroup$NomenclaturalGroupSessionBean() {
 		return (org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean) getBean("nomenclaturalgroup$NomenclaturalGroupSessionBean");
 	}
 
 	public void txt_specimenId_processValueChange(ValueChangeEvent event) {
 		// TODO: Replace with your code
 	}
 
 	public String btn_add_action() {
 		if (this.txt_specimenId.getValue() != null) {
 			if (this.getidentification$IdentificationSessionBean().addSpecimenId(this.txt_specimenId.getValue().toString())) {
 				this.st_specimenId.setValue(this.txt_specimenId.getValue());
 				this.st_gatheringObservation.setValue(this.getidentification$IdentificationSessionBean().getGatheringObservationSummary());
 			}
 			this.txt_specimenId.setValue("");
 		} else {
 			this.getutil$MessageBean().addErrorMessage(EMPTY_SPECIMEN_NUMBER);
 		}
 		return null;
 	}
 
 	public void lst_specimen_processValueChange(ValueChangeEvent event) {
 	}
 
 	private void setSelectedSpecimenInfo() {
 		if (this.getidentification$IdentificationSessionBean().getSelectedSpecimenId() > 0L) {
 			this.st_specimenId.setValue(this.getidentification$IdentificationSessionBean().getSelectedSpecimenId());
 			this.st_gatheringObservation.setValue(this.getidentification$IdentificationSessionBean().getGatheringObservationSummary());
 			this.getidentification$IdentificationSessionBean().setSpecimenActualInfo(this.getidentification$IdentificationSessionBean().getSelectedSpecimenId());
 		} else {
 			this.st_specimenId.setValue("");
 			this.st_gatheringObservation.setValue("");
 		}
 	}
 
 	public String btn_remove_action() {
 		this.getidentification$IdentificationSessionBean().removeSelectedSpecimenId();
 		return null;
 	}
 
 	public String btn_clean_action() {
 		this.getidentification$IdentificationSessionBean().cleanSpecimenList();
 		return null;
 	}
 
 	public String btn_cancel_action() {
 		this.getidentification$IdentificationSessionBean().cleanParameters();
 		return "cancelReIdentify";
 	}
 
 	public String btn_taxonomySearch_action() {
 		String value = this.radioButtonGroup1.getSelected().toString();
 		this.getidentification$IdentificationSearchSessionBean().cleanParameters();
 		if (value.equals("searchByTaxon")) {
 			this.getidentification$IdentificationSearchSessionBean().setTaxonCategoryOption(this.getutil$SelectionListBean().getTaxonCategory());
 			this.getidentification$IdentificationSearchSessionBean().setTaxonomicalRangeOption(this.getutil$SelectionListBean().getTaxonomicalRange());
 		}
 		if (value.equals("searchByGatheringDetail")) {
 			if (!this.getSessionManager().useGatheringDetail()) {
 				this.getutil$MessageBean().addErrorMessage(INVALID_SEARCH_OPTION);
 				this.getutil$MessageBean().addErrorMessage(THE_COLLECTION + getSessionManager().getCollection().getName() + WITHOUT_GATHERING_DETAILS);
 				return null;
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected IdentificationSearchSessionBean getidentification$IdentificationSearchSessionBean() {
 		return (IdentificationSearchSessionBean) getBean("identification$IdentificationSearchSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
 		return (SpecimenSessionBean) getBean("specimen$SpecimenSessionBean");
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
 }
 
