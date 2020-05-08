  /*
  *  Ara - Capture Species and Specimen Data
  *
  * Copyright © 2009  INBio (Instituto Nacional de Biodiversidad).
  * Heredia, Costa Rica.
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
 
 package org.inbio.ara.inventory;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Calendar;
 import com.sun.webui.jsf.component.DropDown;
 import com.sun.webui.jsf.component.Label;
 import com.sun.webui.jsf.component.TextArea;
 import com.sun.webui.jsf.component.TextField;
 import com.sun.webui.jsf.model.Option;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlCommandButton;
 import javax.faces.context.FacesContext;
 import org.inbio.ara.AraSessionBean;
 import org.inbio.ara.util.ValidatorBean;
 import org.inbio.ara.SessionManager;
 import org.inbio.ara.dto.agent.CollectionDTO;
 import org.inbio.ara.dto.gis.SiteDTO;
 import org.inbio.ara.dto.inventory.IdentificationDTO;
 import org.inbio.ara.dto.inventory.PersonDTO;
 import org.inbio.ara.dto.inventory.ProjectDTO;
 import org.inbio.ara.dto.inventory.SelectionListDTO;
 import org.inbio.ara.dto.inventory.SelectionListEntity;
 import org.inbio.ara.dto.inventory.SpecimenDTO;
 import org.inbio.ara.util.AddRemoveList;
 import org.inbio.ara.util.BundleHelper;
 import org.inbio.ara.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  *
  * @version EditGathering.java
  * @version Created on 24/08/2009, 11:17:56 AM
  * @author esmata
  */
 
 public class EditGathering extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
 
     // </editor-fold>
 
     //Binding de los componenetes graficos
     private DropDown ddLocalities = new DropDown();
     private DropDown ddResponsible = new DropDown();
     private DropDown ddExposition = new DropDown();
     private Calendar initial_date = new com.sun.webui.jsf.component.Calendar();
     private Calendar final_date = new com.sun.webui.jsf.component.Calendar();
     private TextField txGradient = new TextField();
     private TextField txElevationMin = new TextField();
     private TextField txElevationMax = new TextField();
     private TextField txDepthMin = new TextField();
     private TextField txDepthMax = new TextField();
     private TextArea txaSurrounding = new TextArea();
     private TextArea txaSiteDescription = new TextArea();
     private HtmlCommandButton gatheDetailButton = new HtmlCommandButton();
     private HtmlCommandButton espeGenerateButton = new HtmlCommandButton();
     private Label lbTitle = new Label();
 
     //En esta variable se setearan los datos del drop down de localidades
     private SingleSelectOptionsList localitiesData = new SingleSelectOptionsList();
     //En esta variable se setearan los datos del drop down de responsables
     private SingleSelectOptionsList responsibleData = new SingleSelectOptionsList();
     //En esta variable se setearan los datos del drop down de expocision
     private SingleSelectOptionsList expositionData = new SingleSelectOptionsList();
 
 	//Contexto utilizado para obtener el current locale
 	private FacesContext context;
 	private Locale myLocale;
 
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public EditGathering() {
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
     @Override
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
             log("EditGathering Initialization Failure", e);
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
     @Override
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
 
         /* --------------------------------------- Carga de datos ------------ */
         this.getExpositionData().setOptions(setExpositionDropDownData
                 (SelectionListEntity.EXPOSITION.getId()));
         //Para drop down de responsables
         this.getResponsibleData().setOptions(SetResponsibleDropDownData());
         //Para drop down de sites
         this.getLocalitiesData().setOptions(SetSitesDropDownData());
 
         this.loadAddRemoveData();
 
         //Metodo se ejecuta solamente la primer ves para cargar datos
         if(this.getinventory$GatheringSessionBean().isFirstTime()){            
             this.loadAddRemoveSelectedData();
             this.loadDates(); //Load the initial and final date
             this.loadTextFields(); //Cargar los textos de los TextFields de edicion
             this.getinventory$GatheringSessionBean().setFirstTime(false);
         }
 
         // Setear el titulo de la pantalla
         Long id = this.getinventory$GatheringSessionBean().getCurrentGatheringDTO().
                 getGatheringObservationId();
         lbTitle.setText(BundleHelper.getDefaultBundleValue
                 ("editing_gathering", this.getMyLocale())+"  "+id);
         
         /* Filtrado de funcionalidad, dependiendo del protocolo de recoleccion
          * especificamente el atributo "Usa detalles de recoleccion" */
         boolean useDetail = this.getinventory$GatheringSessionBean().isUseDetail();
         if(useDetail==true){
             this.getGatheDetailButton().setRendered(true);
             this.getEspeGenerateButton().setRendered(false);
         }
         if(useDetail==false){
             this.getGatheDetailButton().setRendered(false);
             this.getEspeGenerateButton().setRendered(true);
         }
         /* ------------------------------------------------------------------- */
         
     }
 
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called (regardless of whether
      * or not this was the page that was actually rendered).  Customize this
      * method to release resources acquired in the <code>init()</code>,
      * <code>preprocess()</code>, or <code>prerender()</code> methods (or
      * acquired during execution of an event handler).</p>
      */
     @Override
     public void destroy() {
     }
 
     /**
      * Metodo encargado de cargar los datos de los distintos add remove de la
      * ventana de nueva recoleccion, carga los disponibles
      */
     private void loadAddRemoveData(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         //Cargar datos del add remove de colecciones (Disponibles)
         if (gsb.getArCollectionsEdit().getAvailableOptions() == null ||
                 gsb.getArCollectionsEdit().getAvailableOptions().length == 0) {
 
             List<CollectionDTO> collectionsList = gsb.SetCollectionDropDownData();
             List<Option> list = new ArrayList<Option>();
             for (CollectionDTO coll : collectionsList) {
                 list.add(new Option(coll.getCollectionId(), coll.getCollectionName()));
             }
             gsb.getArCollectionsEdit().setAvailableOptions(list.toArray(new Option[list.size()]));
         }
         //Cargar los datos del add remove de colectores (Disponibles)
         if (gsb.getArCollectorsEdit().getAvailableOptions() == null ||
                 gsb.getArCollectorsEdit().getAvailableOptions().length == 0) {
 
             List<PersonDTO> collectorsList = gsb.SetColectorsDropDownData();
             List<Option> list = new ArrayList<Option>();
             for (PersonDTO per : collectorsList) {
                 list.add(new Option(per.getPersonKey(), per.getNaturalLongName()));
             }
             gsb.getArCollectorsEdit().setAvailableOptions(list.toArray(new Option[list.size()]));
         }
         //Cargar los datos del add remove de proyectos (Disponibles)
         if (gsb.getArProjectsEdit().getAvailableOptions() == null ||
                 gsb.getArProjectsEdit().getAvailableOptions().length == 0) {
 
             List<ProjectDTO> proList = gsb.SetProjectsDropDownData();
             List<Option> list = new ArrayList<Option>();
             for (ProjectDTO pro : proList) {
                 list.add(new Option(pro.getProjectId(), pro.getDescription()));
             }
             gsb.getArProjectsEdit().setAvailableOptions(list.toArray(new Option[list.size()]));
         }
         //Setea los labels del componente add remove
         gsb.getArCollectionsEdit().setLbTitle(BundleHelper.getDefaultBundleValue("associated_collections", this.getMyLocale()));
         gsb.getArCollectionsEdit().setLbAvailable(BundleHelper.getDefaultBundleValue("available", this.getMyLocale()));
         gsb.getArCollectionsEdit().setLbSelected(BundleHelper.getDefaultBundleValue("selected", this.getMyLocale()));
 
         gsb.getArCollectorsEdit().setLbTitle(BundleHelper.getDefaultBundleValue("collectors_list", this.getMyLocale()));
         gsb.getArCollectorsEdit().setLbAvailable(BundleHelper.getDefaultBundleValue("available", this.getMyLocale()));
         gsb.getArCollectorsEdit().setLbSelected(BundleHelper.getDefaultBundleValue("selected", this.getMyLocale()));
 
         gsb.getArProjectsEdit().setLbTitle(BundleHelper.getDefaultBundleValue("projects_list", this.getMyLocale()));
         gsb.getArProjectsEdit().setLbAvailable(BundleHelper.getDefaultBundleValue("available", this.getMyLocale()));
         gsb.getArProjectsEdit().setLbSelected(BundleHelper.getDefaultBundleValue("selected", this.getMyLocale()));
     }
 
     /**
      * Metodo encargado de cargar los datos de los distintos add remove de la
      * ventana de nueva recoleccion, carga los seleccionados segun el
      * currenGatheringObservationDTO para edicion
      */
     private void loadAddRemoveSelectedData(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         //Cargar datos del add remove de colecciones (Seleccionados)
         List<CollectionDTO> collectionsList = gsb.getCurrentGatheringDTO().getCollectionsList();
         List<Long> list = new ArrayList<Long>();
         for (CollectionDTO coll : collectionsList) {
               list.add(coll.getCollectionId());
          }
         gsb.getArCollectionsEdit().setSelectedOptions(list.toArray(new Long[list.size()]));
 
 		//Cargar los datos del add remove de colectores (Seleccionados)
         List<PersonDTO> collectorsList = gsb.getCurrentGatheringDTO().getColectorsList();
         List<Long> listP = new ArrayList<Long>();
         for (PersonDTO per : collectorsList) {
             listP.add(per.getPersonKey());
          }
         gsb.getArCollectorsEdit().setSelectedOptions(listP.toArray(new Long[listP.size()]));
 
 		//Cargar los datos del add remove de proyectos (Seleccionados)
         List<ProjectDTO> proList = gsb.getCurrentGatheringDTO().getProjectsList();
         List<Long> listProy = new ArrayList<Long>();
         for (ProjectDTO pro : proList) {
             listProy.add(pro.getProjectId());
         }
         gsb.getArProjectsEdit().setSelectedOptions(listProy.toArray(new Long[listProy.size()]));
     }
 
     /**
      * Metodo encargado de cargar los textos para los textFields de edicion
      */
     private void loadTextFields(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         Long gradient = gsb.getCurrentGatheringDTO().getGradient();
         Double depthMax = gsb.getCurrentGatheringDTO().getMaximumDepth();
         Double depthMin = gsb.getCurrentGatheringDTO().getMinimumDepth();
         Double eleMax = gsb.getCurrentGatheringDTO().getMaximumElevation();
         Double eleMin = gsb.getCurrentGatheringDTO().getMinimumElevation();
         if(gradient!=null)
             this.getTxGradient().setText(String.valueOf(gradient));
         if(depthMax!=null)
             this.getTxDepthMax().setText(String.valueOf(depthMax));
         if(depthMin!=null)
             this.getTxDepthMin().setText(String.valueOf(depthMin));
         if(eleMax!=null)
                 this.getTxElevationMax().setText(String.valueOf(eleMax));
         if(eleMin!=null)
             this.getTxElevationMin().setText(String.valueOf(eleMin));
     }
 
     /**
      * Metodo que obtiene la fecha inicial y final del currentDTO y los carga al
      * los componentes graficos de fechas
      */
     private void loadDates(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         java.util.Calendar ini = gsb.getCurrentGatheringDTO().getInitialDateTime();
         java.util.Calendar fin = gsb.getCurrentGatheringDTO().getFinalDateTime();
         if(ini!=null)
             this.getInitial_date().setSelectedDate(ini.getTime());
         if(fin!=null)
             this.getFinal_date().setSelectedDate(fin.getTime());
     }
 
     /**
      * Metodo para obtener los datos a mostrar en los drop downs de la
      * ventana de new que pertenecen a listas de seleccion
      * @param selectionListEntityId que es el id del enum de listas de seleccion
      * @return
      */
     private Option[] setExpositionDropDownData(Long selectionListEntityId){
         Long collection = this.getAraSessionBean().getGlobalCollectionId();
         List<SelectionListDTO> DTOList = this.getinventory$GatheringSessionBean().
                 setExpositionDropDownData(selectionListEntityId,collection);
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(SelectionListDTO slDTO : DTOList){
             option = new Option(slDTO.getValueId(), slDTO.getValueName().trim());
             allOptions.add(option);
         }
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     /**
      * Obtener los datos del drop down de responsables
      */
     public Option[] SetResponsibleDropDownData(){
         List<PersonDTO> instDTOList = this.getinventory$GatheringSessionBean().
                 SetResponsibleDropDownData();
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(PersonDTO perDTO : instDTOList){
             option = new Option(perDTO.getPersonKey(), perDTO.getNaturalLongName().trim());
             allOptions.add(option);
         }
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
    /**
      * Obtener los datos del drop down de sitios
      */
     public Option[] SetSitesDropDownData(){
         List<SiteDTO> instDTOList = this.getinventory$GatheringSessionBean().
                 SetSitesDropDownData();
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(SiteDTO sDTO : instDTOList){
             option = new Option(sDTO.getSiteId(), sDTO.getDescription().trim());
             allOptions.add(option);
         }
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected AraSessionBean getAraSessionBean() {
         return (AraSessionBean) getBean("AraSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected GatheringSessionBean getinventory$GatheringSessionBean() {
         return (GatheringSessionBean) getBean("inventory$GatheringSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected GatheringDetailSessionBean getinventory$GatheringDetailSessionBean() {
         return (GatheringDetailSessionBean) getBean("inventory$GatheringDetailSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected SpecimenSessionBean getinventory$SpecimenSessionBean() {
         return (SpecimenSessionBean) getBean("inventory$SpecimenSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected ValidatorBean getutil$ValidatorBean() {
         return (ValidatorBean) getBean("util$ValidatorBean");
     }
 
     /**
      * To get the sessionBean for specimen generation
      * @return
      */
     protected SpecimenGenerationSessionBean getinventory$SpecimenGenerationSessionBean() {
         return (SpecimenGenerationSessionBean) getBean("inventory$SpecimenGenerationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected SessionManager getSessionManager() {
         return (SessionManager) getBean("SessionManager");
     }
 
     /**
      * @return the myLocale
      */
     public Locale getMyLocale() {
 		return this.getAraSessionBean().getCurrentLocale();
     }
 
     /**
      * @param myLocale the myLocale to set
      */
     public void setMyLocale(Locale myLocale) {
         this.myLocale = myLocale;
     }
 
     /**
      * @return the localitiesData
      */
     public SingleSelectOptionsList getLocalitiesData() {
         return localitiesData;
     }
 
     /**
      * @param localitiesData the localitiesData to set
      */
     public void setLocalitiesData(SingleSelectOptionsList localitiesData) {
         this.localitiesData = localitiesData;
     }
 
     /**
      * @return the responsibleData
      */
     public SingleSelectOptionsList getResponsibleData() {
         return responsibleData;
     }
 
     /**
      * @param responsibleData the responsibleData to set
      */
     public void setResponsibleData(SingleSelectOptionsList responsibleData) {
         this.responsibleData = responsibleData;
     }
 
     /**
      * @return the expositionData
      */
     public SingleSelectOptionsList getExpositionData() {
         return expositionData;
     }
 
     /**
      * @param expositionData the expositionData to set
      */
     public void setExpositionData(SingleSelectOptionsList expositionData) {
         this.expositionData = expositionData;
     }
 
     /**
      * @return the ddLocalities
      */
     public DropDown getDdLocalities() {
         return ddLocalities;
     }
 
     /**
      * @param ddLocalities the ddLocalities to set
      */
     public void setDdLocalities(DropDown ddLocalities) {
         this.ddLocalities = ddLocalities;
     }
 
     /**
      * @return the ddResponsible
      */
     public DropDown getDdResponsible() {
         return ddResponsible;
     }
 
     /**
      * @param ddResponsible the ddResponsible to set
      */
     public void setDdResponsible(DropDown ddResponsible) {
         this.ddResponsible = ddResponsible;
     }
 
     /**
      * @return the ddExposition
      */
     public DropDown getDdExposition() {
         return ddExposition;
     }
 
     /**
      * @param ddExposition the ddExposition to set
      */
     public void setDdExposition(DropDown ddExposition) {
         this.ddExposition = ddExposition;
     }
 
     /**
      * @return the initial_date
      */
     public Calendar getInitial_date() {
         return initial_date;
     }
 
     /**
      * @param initial_date the initial_date to set
      */
     public void setInitial_date(Calendar initial_date) {
         this.initial_date = initial_date;
     }
 
     /**
      * @return the final_date
      */
     public Calendar getFinal_date() {
         return final_date;
     }
 
     /**
      * @param final_date the final_date to set
      */
     public void setFinal_date(Calendar final_date) {
         this.final_date = final_date;
     }
 
     /**
      * @return the txGradient
      */
     public TextField getTxGradient() {
         return txGradient;
     }
 
     /**
      * @param txGradient the txGradient to set
      */
     public void setTxGradient(TextField txGradient) {
         this.txGradient = txGradient;
     }
 
     /**
      * @return the txElevationMin
      */
     public TextField getTxElevationMin() {
         return txElevationMin;
     }
 
     /**
      * @param txElevationMin the txElevationMin to set
      */
     public void setTxElevationMin(TextField txElevationMin) {
         this.txElevationMin = txElevationMin;
     }
 
     /**
      * @return the txElevationMax
      */
     public TextField getTxElevationMax() {
         return txElevationMax;
     }
 
     /**
      * @param txElevationMax the txElevationMax to set
      */
     public void setTxElevationMax(TextField txElevationMax) {
         this.txElevationMax = txElevationMax;
     }
 
     /**
      * @return the txDepthMin
      */
     public TextField getTxDepthMin() {
         return txDepthMin;
     }
 
     /**
      * @param txDepthMin the txDepthMin to set
      */
     public void setTxDepthMin(TextField txDepthMin) {
         this.txDepthMin = txDepthMin;
     }
 
     /**
      * @return the txDepthMax
      */
     public TextField getTxDepthMax() {
         return txDepthMax;
     }
 
     /**
      * @param txDepthMax the txDepthMax to set
      */
     public void setTxDepthMax(TextField txDepthMax) {
         this.txDepthMax = txDepthMax;
     }
 
     /**
      * @return the txaSurrounding
      */
     public TextArea getTxaSurrounding() {
         return txaSurrounding;
     }
 
     /**
      * @param txaSurrounding the txaSurrounding to set
      */
     public void setTxaSurrounding(TextArea txaSurrounding) {
         this.txaSurrounding = txaSurrounding;
     }
 
     /**
      * @return the txaSiteDescription
      */
     public TextArea getTxaSiteDescription() {
         return txaSiteDescription;
     }
 
     /**
      * @param txaSiteDescription the txaSiteDescription to set
      */
     public void setTxaSiteDescription(TextArea txaSiteDescription) {
         this.txaSiteDescription = txaSiteDescription;
     }
 
     /**
      * Metodo ejecutado por el boton de actualizar informacion
      * @return
      */
     public String btnUpdateGathering_action() {
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         //Variables para maximos y minimos
         String eleMax = (String) this.getTxElevationMax().getText();
         String eleMin = (String) this.getTxElevationMin().getText();
         String deMax = (String) this.getTxDepthMax().getText();
         String deMin = (String) this.getTxDepthMin().getText();
         Double elevationMax = null, elevationMin = null, depthMax = null, depthMin = null;
         if (eleMax != null) {
             elevationMax = Double.valueOf(eleMax);
         }
         if (eleMin != null) {
             elevationMin = Double.valueOf(eleMin);
         }
         if (deMax != null) {
             depthMax = Double.valueOf(deMax);
         }
         if (deMin != null) {
             depthMin = Double.valueOf(deMin);
         }
         //Validar maximos y minimos
         if (elevationMax != null && elevationMin != null) {
             if (elevationMax < elevationMin) {
                 MessageBean.setErrorMessageFromBundle("error_max_min", this.getMyLocale());
                 return null;
             }
         }
         if (depthMax != null && depthMin != null) {
             if (depthMax < depthMin) {
                 MessageBean.setErrorMessageFromBundle("error_max_min", this.getMyLocale());
                 return null;
             }
         }
         //Actualizar en el currentDTO los datos que NO estan directamente "bindeados" con el jsp
         GregorianCalendar iniCal = new GregorianCalendar();
         GregorianCalendar finCal = new GregorianCalendar();
         Date iniDate = this.getInitial_date().getSelectedDate();
         Date finDate = this.getFinal_date().getSelectedDate();
         if (iniDate != null) {
             iniCal.setTime(iniDate);
             gsb.getCurrentGatheringDTO().setInitialDateTime(iniCal);
         }
         if (finDate != null) {
             finCal.setTime(finDate);
             gsb.getCurrentGatheringDTO().setFinalDateTime(finCal);
         }
         String gra = (String) this.getTxGradient().getText();
         if (gra != null) {
             gsb.getCurrentGatheringDTO().setGradient(Long.valueOf(gra));
         }
         gsb.getCurrentGatheringDTO().setMaximumElevation(elevationMax);
         gsb.getCurrentGatheringDTO().setMinimumElevation(elevationMin);
         gsb.getCurrentGatheringDTO().setMaximumDepth(depthMax);
         gsb.getCurrentGatheringDTO().setMinimumDepth(depthMin);
 
         gsb.getCurrentGatheringDTO().setColectorsList(colectorsAsDTO());
         gsb.getCurrentGatheringDTO().setProjectsList(projectsAsDTO());
         gsb.getCurrentGatheringDTO().setCollectionsList(collectionsAsDTO());
 
         try{
             //Llamar metodo que persiste el DTO (update)
             this.getinventory$GatheringSessionBean().getInventoryFacade().
                     updateGathering(gsb.getCurrentGatheringDTO());
         }
         catch(Exception e){
             MessageBean.setErrorMessageFromBundle("error", this.getMyLocale());
             return null;
         }
 
         //Actualizar el data provider del paginador
         this.getinventory$GatheringSessionBean().getPagination().refreshList();
 
         //Avisar al usuario que la operacion fue exitosa
         MessageBean.setSuccessMessageFromBundle("update_success", this.getMyLocale());
         
         return null;
     }
 
     /**
      * Metodo que toma los valores seleccionados en el add remove de colectores
      * y los transforma en una lista de PersonDTO
      */
     private List<PersonDTO> colectorsAsDTO(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         List<PersonDTO> result = new ArrayList();
         Long[] opList = gsb.getArCollectorsEdit().getSelectedOptions();
 
 		int arrayLength = 0;
 
 		if(opList != null){
 			arrayLength = opList.length;
 			for(int i = 0; i < arrayLength; i++){
 				PersonDTO aux = new PersonDTO();
 				aux.setPersonKey(opList[i]);
 				result.add(aux);
 			}
 		}
 
         return result;
     }
 
     /**
      * Metodo que toma los valores seleccionados en el add remove de projectos
      * y los transforma en una lista de PersonDTO
      */
     private List<ProjectDTO> projectsAsDTO(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         List<ProjectDTO> result = new ArrayList();
         Long[] opList = gsb.getArProjectsEdit().getSelectedOptions();
 		int arrayLength = 0;
 
 		if(opList != null){
 			arrayLength = opList.length;
 			for(int i = 0; i < arrayLength; i++){
 				ProjectDTO aux = new ProjectDTO();
 				aux.setProjectId(opList[i]);
 				result.add(aux);
 			}
 
 		}
         return result;
     }
 
     /**
      * Metodo que toma los valores seleccionados en el add remove de colecciones asociadas
      * y los transforma en una lista de PersonDTO
      */
     private List<CollectionDTO> collectionsAsDTO(){
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         List<CollectionDTO> result = new ArrayList();
         Long[] opList = gsb.getArCollectionsEdit().getSelectedOptions();
 
 		int arrayLength = 0;
 
 		if(opList != null){
 			arrayLength = opList.length;
 			for(int i = 0; i < arrayLength; i++){
 				CollectionDTO aux = new CollectionDTO();
 				aux.setCollectionId(opList[i]);
 				result.add(aux);
 			}
 
 		}
         return result;
     }
 
     /**
      * Metodo ejecutado por el boton de generar especimenes asociados a dicha recoleccion
      * @return
      */
     public String btnGenerateSpecimens_action() {
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
         SpecimenGenerationSessionBean sgsb = this.getinventory$SpecimenGenerationSessionBean();
         
         //Reestablecer valores iniciales de la pantalla de generacion
         sgsb.setSpecimenDTO(new SpecimenDTO());
         sgsb.setIdentificationDTO(new IdentificationDTO());
         sgsb.setLifeFormList(new ArrayList<Long>());
         sgsb.setSpecimenQuantity(new Long(0));
 
         //Setear el valor de gatheringObservationId
         sgsb.setGatheringObservationId(gsb.getCurrentGatheringDTO().getGatheringObservationId());
         sgsb.setGatheringDetailObservationId(null);
 
         //Limpiar los addRemove
         sgsb.setArIdentifierList(new AddRemoveList());
         sgsb.setArLifeFormList(new AddRemoveList());
         sgsb.setArTaxonList(new AddRemoveList());
 
         /*Indicar a la pantalla de edit que cargue 1 sola ves los datos
         seleccionados de los AddRemove*/
         this.getinventory$GatheringSessionBean().setFirstTime(true);
 
        /* Dejar limpios los add remove de la pantalla de edición, esto con el fin de que
         * si el usuario decide volver a la edición desde los detalles de recolección y/o
         * desde la generación de especímenes los datos de los add remove no se repitan */
        gsb.setArCollectionsEdit(new AddRemoveList());
        gsb.setArCollectorsEdit(new AddRemoveList());
        gsb.setArProjectsEdit(new AddRemoveList());

         return "generate";
     }
 
     /**
      * Metodo ejecutado por el boton de detalles de recolección
      * @return
      */
     public String btnGatheringDetail_action() {
         GatheringSessionBean gsb = this.getinventory$GatheringSessionBean();
 
         //Pasar el dto de recoleccion
         this.getinventory$GatheringDetailSessionBean().setCurrentGathering(gsb.getCurrentGatheringDTO());
 
         //Ponerlo en null para que sea nuevamente instanciado en el prerender de listDetail
         this.getinventory$GatheringDetailSessionBean().setPagination(null);
 
         /*Indicar a la pantalla de edit que cargue 1 sola ves los datos
         seleccionados de los AddRemove*/
         this.getinventory$GatheringSessionBean().setFirstTime(true);
 
        /* Dejar limpios los add remove de la pantalla de edición, esto con el fin de que
         * si el usuario decide volver a la edición desde los detalles de recolección y/o
         * desde la generación de especímenes los datos de los add remove no se repitan */
        gsb.setArCollectionsEdit(new AddRemoveList());
        gsb.setArCollectorsEdit(new AddRemoveList());
        gsb.setArProjectsEdit(new AddRemoveList());

         return "detail";
     }
 
     /**
      * @return the gatheDetailButton
      */
     public HtmlCommandButton getGatheDetailButton() {
         return gatheDetailButton;
     }
 
     /**
      * @param gatheDetailButton the gatheDetailButton to set
      */
     public void setGatheDetailButton(HtmlCommandButton gatheDetailButton) {
         this.gatheDetailButton = gatheDetailButton;
     }
 
     /**
      * @return the espeGenerateButton
      */
     public HtmlCommandButton getEspeGenerateButton() {
         return espeGenerateButton;
     }
 
     /**
      * @param espeGenerateButton the espeGenerateButton to set
      */
     public void setEspeGenerateButton(HtmlCommandButton espeGenerateButton) {
         this.espeGenerateButton = espeGenerateButton;
     }
 
     /**
      * @return the lbTitle
      */
     public Label getLbTitle() {
         return lbTitle;
     }
 
     /**
      * @param lbTitle the lbTitle to set
      */
     public void setLbTitle(Label lbTitle) {
         this.lbTitle = lbTitle;
     }
 }
 
