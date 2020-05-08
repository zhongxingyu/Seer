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
  * EditSpecimen.java
  *
  * Created on 14 de mayo de 2008, 12:20 PM
  * Copyright herson
  */
 package org.inbio.ara.web.specimen;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.DropDown;
 import com.sun.webui.jsf.component.Hyperlink;
 import com.sun.webui.jsf.component.RadioButtonGroup;
 import com.sun.webui.jsf.component.Table;
 import com.sun.webui.jsf.component.TableRowGroup;
 import com.sun.webui.jsf.component.TextField;
 import com.sun.webui.jsf.model.DefaultTableDataProvider;
 import com.sun.webui.jsf.model.Option;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.LongConverter;
 import javax.faces.validator.LongRangeValidator;
 import org.inbio.ara.dto.LifeStageSexDTO;
 import org.inbio.ara.persistence.gathering.GatheringObservationMethod;
 import org.inbio.ara.persistence.specimen.ExtractionType;
 import org.inbio.ara.persistence.specimen.Origin;
 import org.inbio.ara.persistence.specimen.PreservationMedium;
 import org.inbio.ara.persistence.specimen.Specimen;
 import org.inbio.ara.persistence.specimen.SpecimenCategory;
 import org.inbio.ara.persistence.specimen.SpecimenType;
 import org.inbio.ara.persistence.specimen.StorageType;
 import org.inbio.ara.persistence.specimen.Substrate;
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
 import org.inbio.ara.web.identification.IdentificationSessionBean;
 import org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean;
 import org.inbio.ara.web.references.ReferenceSessionBean;
 import org.inbio.ara.web.site.SiteSessionBean;
 import org.inbio.ara.web.species.SpeciesSessionBean;
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
 public class EditSpecimen extends AbstractPageBean {
 
     private static final String CHOOSE_STAGE_KEY = "choose_stage";
     private static final String ADD_STAGE_FAILED_KEY = "add_stage_failed";
     private static final String CHOOSE_SEX_KEY = "choose_sex";
     private static final String CHOOSE_SPECIMEN_KEY = "choose_specimen";
     private static final String DUPLICATED_DATA_KEY = "duplicated_data";
     private static final String SPECIMEN_ACCOUNT_REQUIRED_KEY = "specimen_account_required";
     private static final String STAGE_AMOUNT_NOT_NUMERIC_KEY = "stage_amount_not_numeric";
     private static final String STAGE_AMOUNT_OUT_OF_RANGE_KEY = "stage_amount_out_of_range";
     private static final String SUCCESS_KEY = "creation_success";
 	// <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 	private int __placeholder;
 	//private static final String bundle = "org/inbio/ara/web/resources/global";
 
 	/**
 	 * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
 	 * This method is automatically generated, so any user-specified code inserted
 	 * here is subject to being replaced.</p>
 	 */
 	private void _init() throws Exception {
 	}
 
 	private TextField gathSeqInput = new TextField();
 
 	public TextField getGathSeqInput() {
 		return gathSeqInput;
 	}
 
 	public void setGathSeqInput(TextField tf) {
 		this.gathSeqInput = tf;
 	}
 	private TextField barCodeInput = new TextField();
 
 	public TextField getBarCodeInput() {
 		return barCodeInput;
 	}
 
 	public void setBarCodeInput(TextField tf) {
 		this.barCodeInput = tf;
 	}
 	private DropDown extractionDD = new DropDown();
 
 	public DropDown getExtractionDD() {
 		return extractionDD;
 	}
 
 	public void setExtractionDD(DropDown dd) {
 		this.extractionDD = dd;
 	}
 	private SingleSelectOptionsList extractionDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getExtractionDDDefaultOptions() {
 		return extractionDDDefaultOptions;
 	}
 
 	public void setExtractionDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.extractionDDDefaultOptions = ssol;
 	}
 	private DropDown preservationDD = new DropDown();
 
 	public DropDown getPreservationDD() {
 		return preservationDD;
 	}
 
 	public void setPreservationDD(DropDown dd) {
 		this.preservationDD = dd;
 	}
 	private SingleSelectOptionsList preservationDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getPreservationDDDefaultOptions() {
 		return preservationDDDefaultOptions;
 	}
 
 	public void setPreservationDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.preservationDDDefaultOptions = ssol;
 	}
 	private DropDown originDD = new DropDown();
 
 	public DropDown getOriginDD() {
 		return originDD;
 	}
 
 	public void setOriginDD(DropDown dd) {
 		this.originDD = dd;
 	}
 	private SingleSelectOptionsList originDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getOriginDDDefaultOptions() {
 		return originDDDefaultOptions;
 	}
 
 	public void setOriginDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.originDDDefaultOptions = ssol;
 	}
 	private DropDown substrateDD = new DropDown();
 
 	public DropDown getSubstrateDD() {
 		return substrateDD;
 	}
 
 	public void setSubstrateDD(DropDown dd) {
 		this.substrateDD = dd;
 	}
 	private SingleSelectOptionsList substrateDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getSubstrateDDDefaultOptions() {
 		return substrateDDDefaultOptions;
 	}
 
 	public void setSubstrateDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.substrateDDDefaultOptions = ssol;
 	}
 
 	private DropDown storageDD = new DropDown();
 
 	public DropDown getStorageDD() {
 		return storageDD;
 	}
 
 	public void setStorageDD(DropDown dd) {
 		this.storageDD = dd;
 	}
 	private SingleSelectOptionsList storageDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getStorageDDDefaultOptions() {
 		return storageDDDefaultOptions;
 	}
 
 	public void setStorageDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.storageDDDefaultOptions = ssol;
 	}
 
 
 	private SingleSelectOptionsList discardedRadioButtonDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getDiscardedRadioButtonDefaultOptions() {
 		return discardedRadioButtonDefaultOptions;
 	}
 
 	public void setDiscardedRadioButtonDefaultOptions(SingleSelectOptionsList ssol) {
 		this.discardedRadioButtonDefaultOptions = ssol;
 	}
 	private TextField numWholeInput = new TextField();
 
 	public TextField getNumWholeInput() {
 		return numWholeInput;
 	}
 
 	public void setNumWholeInput(TextField tf) {
 		this.numWholeInput = tf;
 	}
 
 	private TextField numFragmentsInput = new TextField();
 
 	public TextField getNumFragmentsInput() {
 		return numFragmentsInput;
 	}
 
 	public void setNumFragmentsInput(TextField tf) {
 		this.numFragmentsInput = tf;
 	}
 	private LongConverter sexDDConverter = new LongConverter();
 
 	public LongConverter getSexDDConverter() {
 		return sexDDConverter;
 	}
 
 	public void setSexDDConverter(LongConverter lc) {
 		this.sexDDConverter = lc;
 	}
 	private DropDown categoryDD = new DropDown();
 
 	public DropDown getCategoryDD() {
 		return categoryDD;
 	}
 
 	public void setCategoryDD(DropDown dd) {
 		this.categoryDD = dd;
 	}
 	private SingleSelectOptionsList categoryDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getCategoryDDDefaultOptions() {
 		return categoryDDDefaultOptions;
 	}
 
 	public void setCategoryDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.categoryDDDefaultOptions = ssol;
 	}
 	private DropDown collectionDD = new DropDown();
 
 	public DropDown getCollectionDD() {
 		return collectionDD;
 	}
 
 	public void setCollectionDD(DropDown dd) {
 		this.collectionDD = dd;
 	}
 	private SingleSelectOptionsList collectionDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getCollectionDDDefaultOptions() {
 		return collectionDDDefaultOptions;
 	}
 
 	public void setCollectionDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.collectionDDDefaultOptions = ssol;
 	}
 
 	private LongRangeValidator longRangeValidator1 = new LongRangeValidator();
 
 	public LongRangeValidator getLongRangeValidator1() {
 		return longRangeValidator1;
 	}
 
 	public void setLongRangeValidator1(LongRangeValidator lrv) {
 		this.longRangeValidator1 = lrv;
 	}
 	private LongRangeValidator longRangeValidator2 = new LongRangeValidator();
 
 	public LongRangeValidator getLongRangeValidator2() {
 		return longRangeValidator2;
 	}
 
 	public void setLongRangeValidator2(LongRangeValidator lrv) {
 		this.longRangeValidator2 = lrv;
 	}
 	private RadioButtonGroup discardedRadioButton = new RadioButtonGroup();
 
 	public RadioButtonGroup getDiscardedRadioButton() {
 		return discardedRadioButton;
 	}
 
 	public void setDiscardedRadioButton(RadioButtonGroup rbg) {
 		this.discardedRadioButton = rbg;
 	}
 	private LongRangeValidator longRangeValidator3 = new LongRangeValidator();
 
 	public LongRangeValidator getLongRangeValidator3() {
 		return longRangeValidator3;
 	}
 
 	public void setLongRangeValidator3(LongRangeValidator lrv) {
 		this.longRangeValidator3 = lrv;
 	}
 	private LongRangeValidator longRangeValidator4 = new LongRangeValidator();
 
 	public LongRangeValidator getLongRangeValidator4() {
 		return longRangeValidator4;
 	}
 
 	public void setLongRangeValidator4(LongRangeValidator lrv) {
 		this.longRangeValidator4 = lrv;
 	}
 
 	private DropDown gathObsMethodDD = new DropDown();
 
 	public DropDown getGathObsMethodDD() {
 		return gathObsMethodDD;
 	}
 
 	public void setGathObsMethodDD(DropDown dd) {
 		this.gathObsMethodDD = dd;
 	}
 	private SingleSelectOptionsList gathObsMethodDDDefaultOptions = new SingleSelectOptionsList();
 
 	public SingleSelectOptionsList getGathObsMethodDDDefaultOptions() {
 		return gathObsMethodDDDefaultOptions;
 	}
 
 	public void setGathObsMethodDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.gathObsMethodDDDefaultOptions = ssol;
 	}
 	private Hyperlink englishLink1 = new Hyperlink();
 
 	public Hyperlink getEnglishLink1() {
 		return englishLink1;
 	}
 
 	public void setEnglishLink1(Hyperlink h) {
 		this.englishLink1 = h;
 	}
 	private Hyperlink spanishLink1 = new Hyperlink();
 
 	public Hyperlink getSpanishLink1() {
 		return spanishLink1;
 	}
 
 	public void setSpanishLink1(Hyperlink h) {
 		this.spanishLink1 = h;
 	}
 
 	private Table table1 = new Table();
 
 	public Table getTable1() {
 		return table1;
 	}
 
 	public void setTable1(Table t) {
 		this.table1 = t;
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
 
 
 	// </editor-fold>
 	/**
 	 * <p>Construct a new Page bean instance.</p>
 	 */
 	public EditSpecimen() {
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
 			log("Module newSpecimen Initialization Failure", e);
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
 		Specimen s;
 		if (this.getspecimen$SpecimenSessionBean().isInMultipleEdition()) {
 			s = this.mergeSpecimens();
 		} else {
 			s = this.getspecimen$SpecimenSessionBean().getCurrentSpecimen();
 		}
 
 		/**********************************************************************/
 		/******************ESTO SE DEBE BORRAR*********************************/
 		this.getSessionManager().setCollection(s.getCollection());
 		/**********************************************************************/
 		/**********************************************************************/
 		Locale l = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 
 		discardedRadioButtonDefaultOptions.setOptions(new Option[]{
 					new Option("0", MessageBean.getMessageFromBundle("no")),
 					new Option("1", MessageBean.getMessageFromBundle("yes"))
 				});
 		categoryDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(SpecimenCategory.class, l));
 		collectionDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getCollection());
 		extractionDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(ExtractionType.class, l));
 		gathObsMethodDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(GatheringObservationMethod.class, l));
 		originDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(Origin.class, l));
 		preservationDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(PreservationMedium.class, l));
 		substrateDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(Substrate.class, l));
 		storageDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(StorageType.class, l));
 
 		this.gathSeqInput.setText(s.getGatheringObservation().getId());
 
 		this.barCodeInput.setText(s.getId());
 
 		//this.categoryDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(SpecimenCategory.class));
 		this.categoryDD.setSelected((s.getSpecimenCategory() != null) ? s.getSpecimenCategory().getId() : 0L);
 
 		//this.collectionDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getCollection());
 		this.collectionDD.setSelected((s.getCollection() != null) ? s.getCollection().getId() : 0L);
 
 		//this.extractionDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(ExtractionType.class));
 		this.extractionDD.setSelected((s.getExtractionType() != null) ? s.getExtractionType().getId() : 0L);
 
 		this.gathObsMethodDD.setSelected((s.getGatheringObservationMethod() != null) ? s.getGatheringObservationMethod().getId() : 0L);
 
 
 		//this.originDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(Origin.class));
 		this.originDD.setSelected((s.getOrigin() != null) ? s.getOrigin().getId() : 0L);
 
 		//this.preservationDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(PreservationMedium.class));
 		this.preservationDD.setSelected((s.getPreservationMedium() != null) ? s.getPreservationMedium().getId() : 0L);
 
 		//this.substrateDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(Substrate.class));
 		this.substrateDD.setSelected((s.getSubstrate() != null) ? s.getSubstrate().getId() : 0L);
 
 		//this.storageDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(StorageType.class));
 		this.storageDD.setSelected((s.getStorageType() != null) ? s.getStorageType().getId() : 0L);
 
 		discardedRadioButtonDefaultOptions.setSelectedValue(s.getDiscarded());
 		this.numWholeInput.setText(s.getNumberWhole());
 		this.numFragmentsInput.setText(s.getNumberFragments());
 
         this.getEditSpecimenSessionBean().populateOptions();
         //this.getEdit$EditSpecimenSessionBean().populateOptions();
 
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
 	protected GatheringSessionBeanV2 getgathering$GatheringSessionBeanV2() {
 		return (GatheringSessionBeanV2) getBean("gathering$GatheringSessionBeanV2");
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
 	protected PersonSessionBean getadmin$person$PersonSessionBean() {
 		return (PersonSessionBean) getBean("admin$person$PersonSessionBean");
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
 	protected SessionManager getSessionManager() {
 		return (SessionManager) getBean("SessionManager");
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
 	protected ProfileSessionBean getadmin$profile$ProfileSessionBean() {
 		return (ProfileSessionBean) getBean("admin$profile$ProfileSessionBean");
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
 	protected GroupSessionBean getgroup$GroupSessionBean() {
 		return (GroupSessionBean) getBean("group$GroupSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected MessageBean getMessageBean() {
 		return (MessageBean) getBean("util$MessageBean");
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
 	protected AraApplicationBean getAraApplicationBean() {
 		return (AraApplicationBean) getBean("AraApplicationBean");
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
 	protected SpeciesSessionBean getspecies$SpeciesSessionBean() {
 		return (SpeciesSessionBean) getBean("species$SpeciesSessionBean");
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
 	protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
 		return (SpecimenSessionBean) getBean("specimen$SpecimenSessionBean");
 	}
 
 	protected EditSpecimenSessionBean getEditSpecimenSessionBean() {
 		return (EditSpecimenSessionBean) getBean("specimen$EditSpecimenSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected RequestBean1 getRequestBean1() {
 		return (RequestBean1) getBean("RequestBean1");
 	}
 
 	/**
 	 * Edita la informacion basica de un especimen
 	 */
 	public String btn_save_action() {
 		if (getspecimen$SpecimenSessionBean().isInMultipleEdition()) {
 			List<Specimen> listSpecimen = getspecimen$SpecimenSessionBean().getSpecimenList();
 			for (Specimen elem : listSpecimen) {
 				getspecimen$SpecimenSessionBean().setCurrentSpecimen(elem);
 				updateInformation();
 			}
 			getspecimen$SpecimenSessionBean().setInMultipleEdition(false);
 		} else {
 			updateInformation();
 		}
 		return null;
 	}
 
 	/**
 	 * Updates information about one specimen. Can be called in a loop to make
 	 * multiple edition.
 	 * This method works on the currentSpecimen saved in the SpecimenSessionBean
 	 */
 	private void updateInformation() {
 		Specimen specimen = getspecimen$SpecimenSessionBean().getCurrentSpecimen();
 		specimen.setLastModificationBy(this.getSessionManager().getUser().getUserName());
 
 		//specimen.setId(Long.parseLong((String) this.getBarCodeInput().getText()));
 		if (this.getGathSeqInput().getText() != null && !this.getGathSeqInput().getText().equals("")) {
 			Long gatheringObservationId = (Long) getGathSeqInput().getValue();
 			specimen.setGatheringObservation(this.getspecimen$SpecimenSessionBean().
 					getGatheringBean().find(gatheringObservationId));
 		}
 
 		if (((Long) categoryDD.getSelected()) >= 0L) {
 			SpecimenCategory category = this.getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findSpecimenCategory((Long) categoryDD.getSelected());
 			specimen.setSpecimenCategory(category);
 		}
 
 		//if(((Long) collectionDD.getSelected()) >= 0L) {
 		//    Collection collection = this.getspecimen$SpecimenSessionBean().
 		//            getCollectionBean().find((Long) collectionDD.getSelected());
 		//    specimen.setCollection(collection);
 		//}
 		if (((Long) extractionDD.getSelected()) >= 0L) {
 			ExtractionType extraction = this.getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findExtractionType((Long) extractionDD.getSelected());
 			specimen.setExtractionType(extraction);
 		}
 		if (((Long) gathObsMethodDD.getSelected()) >= 0L) {
 			GatheringObservationMethod gom = this.getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findGOMethod((Long) gathObsMethodDD.getSelected());
 			specimen.setGatheringObservationMethod(gom);
 		}
 		if (((Long) this.getEditSpecimenSessionBean().getTypeDD().getSelected()) >= 0L) {
 			SpecimenType type = this.getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findSpecimenType((Long) this.getEditSpecimenSessionBean().getTypeDD().getSelected());
 			specimen.setSpecimenType(type);
 		}
 		if (((Long) originDD.getSelected()) >= 0L) {
 			Origin origin = this.getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findOrigin((Long) originDD.getSelected());
 			specimen.setOrigin(origin);
 		}
 		if (((Long) preservationDD.getSelected()) >= 0L) {
 			PreservationMedium preservation = getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findPreservationMedium((Long) preservationDD.getSelected());
 			specimen.setPreservationMedium(preservation);
 		}
 		if (((Long) substrateDD.getSelected()) >= 0L) {
 			Substrate substrate = getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findSubstrate((Long) substrateDD.getSelected());
 			specimen.setSubstrate(substrate);
 		}
 
 		if (((Long) storageDD.getSelected()) >= 0L) {
 			StorageType storageType = getspecimen$SpecimenSessionBean().
 					getSpecimenBean().findStorageType((Long) storageDD.getSelected());
 			specimen.setStorageType(storageType);
 		}
 
 		specimen.setDiscarded(discardedRadioButton.getSelected().toString());
 
 		if (getNumWholeInput().getText() != null) {
 			specimen.setNumberWhole(Long.parseLong(getNumWholeInput().
 					getText().toString()));
 		} else {
 			specimen.setNumberWhole(null);
 		}
 
 		if (getNumFragmentsInput().getText() != null) {
 			specimen.setNumberFragments(Long.parseLong(getNumFragmentsInput().
 					getText().toString()));
 		} else {
 			specimen.setNumberFragments(null);
 		}
 
 		try {
 			getspecimen$SpecimenSessionBean().getSpecimenBean().update(specimen);
             //FIXME
             //getspecimen$SpecimenSessionBean().getSpecimenBean().saveSpecimenLifeStageSexList(getEdit$EditSpecimenSessionBean().getLifeStageSexDataProvider().getList());
             getspecimen$SpecimenSessionBean().getSpecimenBean().updateSpecimenLifeStageSexList(specimen.getId(),getEditSpecimenSessionBean().getLifeStageSexDTODataProvier().getList());
 
 			info(MessageBean.getMessageFromBundle("update_succesful"));
 		} catch (Exception e) {
 			error(MessageBean.getMessageFromBundle("update_failure"));
 		}
 
 		//CommitChanges no recarga la lista con los nuevos valores como deberia
 		//para volver a la lista de resultados.
 		//En su lugar se cambia la bandera de filtrado y se recarga la lista
 		//completa de nuevo.
 		//getspecimen$SpecimenSessionBean().getSpecimenDataProvider().commitChanges();
 		getspecimen$SpecimenSessionBean().setIsFiltered(false);
 		updateSessionBean();
 	}
 
 	/**
 	 * Updates information about current specimen(s) in edition in order to
 	 * permit to make 2 or more editions (clicks on the "update" button)
 	 */
 	private void updateSessionBean() {
 		if (getspecimen$SpecimenSessionBean().isInMultipleEdition()) {
 			ArrayList<Specimen> lista = getspecimen$SpecimenSessionBean().getSpecimenList();
 			getspecimen$SpecimenSessionBean().getSpecimenList().clear();
 			for (Specimen oldSpecimen : lista) {
 				Specimen updatedSpecimen = getspecimen$SpecimenSessionBean().
 						getSpecimenBean().find(oldSpecimen.getId());
 				getspecimen$SpecimenSessionBean().getSpecimenList().add(updatedSpecimen);
 			}
 		} else {
 			System.out.println("actualizado current specimen");
 			Specimen oldSpecimen = getspecimen$SpecimenSessionBean().
 					getCurrentSpecimen();
 			Specimen updatedSpecimen = getspecimen$SpecimenSessionBean().
 					getSpecimenBean().find(oldSpecimen.getId());
 			getspecimen$SpecimenSessionBean().setCurrentSpecimen(updatedSpecimen);
 		}
 	}
 
 	public String btn_cancel_action() {
 		getspecimen$SpecimenSessionBean().setInMultipleEdition(false);
 		getspecimen$SpecimenSessionBean().setSpecimenList(null);
 		//Navigation rule to SpecimenList
 		return "specimen_list";
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
 	protected SpecimenGenerationSessionBean getgathering$SpecimenGenerationSessionBean() {
 		return (SpecimenGenerationSessionBean) getBean("gathering$SpecimenGenerationSessionBean");
 	}
 
 	/**
 	 * Takes specimenList from SpecimenSessionBean and merge them to a single
 	 * Specimen object with the common charateristics of all the Specimens
 	 * objects passed through the function.
 	 * Whenever the SpecimenSessionBean.multipleEdition is true, the GUI shows
 	 * the information shared by all of them and keeps deselected but editable
 	 * the non-shared information.
 	 *
 	 * @return Specimen the information shared by all the specimens.
 	 */
 	private Specimen mergeSpecimens() {
 		Specimen s = new Specimen();
 		boolean firstIteration = true;
 		List<Specimen> listSpecimen = getspecimen$SpecimenSessionBean().getSpecimenList();
 		for (Specimen elem : listSpecimen) {
 			System.out.println("NOT YET IMPLEMENTED");
 			if (firstIteration) {
 				s = elem;
 			}
 			firstIteration = false;
 		}
 		return s;
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
 	protected IdentificationSearchSessionBean getidentification$IdentificationSearchSessionBean() {
 		return (IdentificationSearchSessionBean) getBean("identification$IdentificationSearchSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected NomenclaturalGroupSessionBean getnomenclaturalgroup$NomenclaturalGroupSessionBean() {
 		return (NomenclaturalGroupSessionBean) getBean("nomenclaturalgroup$NomenclaturalGroupSessionBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected IdentificationSessionBean getidentification$IdentificationSessionBean() {
 		return (IdentificationSessionBean) getBean("identification$IdentificationSessionBean");
 	}
 
 	public String btn_addStadiumSex1_action() {
 
 		if (this.canAddLifeStageSex()) {
            this.getEditSpecimenSessionBean().saveActualLifeStageSexDTO();
            MessageBean.setSuccessMessageFromBundle(SUCCESS_KEY);
 		}
 		return null;
 	}
 	private TextField txt_ssQuantity = new TextField();
 
 	public TextField getTxt_ssQuantity() {
 		return txt_ssQuantity;
 	}
 
 	public void setTxt_ssQuantity(TextField tf) {
 		this.txt_ssQuantity = tf;
 	}
 
 	private boolean canAddLifeStageSex() {
 
 
 		if (this.getEditSpecimenSessionBean().getTypeDD().getValue().equals(-1L)) {
 			MessageBean.setErrorMessageFromBundle(CHOOSE_SPECIMEN_KEY);
 			return false;
 		}
 
 
         if ((this.getEditSpecimenSessionBean().getLifeStageSexDTODataProvier().getList().size() == 1) & (!this.getEditSpecimenSessionBean().getTypeDD().getValue().equals(2L))) {
             MessageBean.setErrorMessageFromBundle(ADD_STAGE_FAILED_KEY);
 			return false;
 		}
 
         if (this.getEditSpecimenSessionBean().getActualLifeStageSexDTO().getLifeStageKey() == null){
             MessageBean.setErrorMessageFromBundle(CHOOSE_STAGE_KEY);
 			return false;
         }
 
         if (this.getEditSpecimenSessionBean().getActualLifeStageSexDTO().getSexKey() == null){
             MessageBean.setErrorMessageFromBundle(CHOOSE_SEX_KEY);
 			return false;
         }
 
          if (this.getEditSpecimenSessionBean().getActualLifeStageSexDTO().getQuantity() == null){
             MessageBean.setErrorMessageFromBundle(SPECIMEN_ACCOUNT_REQUIRED_KEY);
 			return false;
          } else {
 			// Validaci�n de tipo de dato
 			String value = this.getEditSpecimenSessionBean().getActualLifeStageSexDTO().getQuantity();
 			try {
 				Integer ssQuantity = Integer.parseInt(value);
 				if ((ssQuantity < 0) | (ssQuantity > 10000)) {
 					MessageBean.setErrorMessageFromBundle(STAGE_AMOUNT_OUT_OF_RANGE_KEY);
 					return false;
 				}
 			} catch (NumberFormatException ex) {
 				MessageBean.setErrorMessageFromBundle(STAGE_AMOUNT_NOT_NUMERIC_KEY);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void btn_removeStadiumSex1_action() {
 
 		//LifeStageSexSimple object = (LifeStageSexSimple) this.getEdit$EditSpecimenSessionBean().getLifeStageSexDataProvider().getObject(this.tableRowGroup1.getRowKey());
         LifeStageSexDTO lssDTO = (LifeStageSexDTO) this.getEditSpecimenSessionBean().getLifeStageSexDTODataProvier().getObject(this.tableRowGroup1.getRowKey());
         this.getEditSpecimenSessionBean().setActualLifeStageSexDTO(lssDTO);
         this.getEditSpecimenSessionBean().deleteActualLifeStageSexDTO();
 	}
 }
