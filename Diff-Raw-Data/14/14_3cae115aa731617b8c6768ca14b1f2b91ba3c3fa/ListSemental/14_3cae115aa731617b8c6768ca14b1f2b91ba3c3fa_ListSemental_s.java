 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.inbio.ara.germplasm;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Calendar;
 import com.sun.webui.jsf.model.Option;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import javax.faces.FacesException;
 import javax.faces.component.html.HtmlCommandButton;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.component.html.HtmlInputText;
 import javax.faces.component.html.HtmlPanelGrid;
 import org.inbio.ara.AraSessionBean;
 import org.inbio.ara.admin.SelectionListSessionBean;
 import org.inbio.ara.admin.PersonSessionBean;
 import org.inbio.ara.dto.germplasm.BreedDTO;
 import org.inbio.ara.dto.germplasm.SementalDTO;
 import org.inbio.ara.dto.gis.SiteDTO;
 import org.inbio.ara.dto.inventory.SelectionListDTO;
 import org.inbio.ara.dto.inventory.SelectionListEntity;
 import org.inbio.ara.inventory.GatheringSessionBean;
 import org.inbio.ara.util.BundleHelper;
 import org.inbio.ara.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  *
  * @version ListSemental.java
  * @version Created on 08/04/2010, 09:41:03 AM
  * @author dasolano
  */
 
 public class ListSemental extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
 
     // </editor-fold>
 
     private String quantityTotal = new String();
 
     private HtmlDataTable dataTableSemental = new HtmlDataTable();
 
 
     private HtmlPanelGrid alertMessage = new HtmlPanelGrid();
     private HtmlPanelGrid mainPanel = new HtmlPanelGrid();
 
 
     /*Componentes para las busquedas*/
     private HtmlInputText txSimpleSearch = new HtmlInputText(); //Input text de busqueda simple
     private HtmlPanelGrid gridpAdvancedSearch = new HtmlPanelGrid();
     private HtmlCommandButton btnSimpleSearch = new HtmlCommandButton(); //Boton busqueda simple
     private HtmlCommandButton btnAdvSearch = new HtmlCommandButton(); //Boton busqueda avanzada
 
     //componentes para busuqedas avanzadas
     private Calendar birthDate = new Calendar();
     private SingleSelectOptionsList breeds = new SingleSelectOptionsList();
     private SingleSelectOptionsList sites = new SingleSelectOptionsList();
     private SingleSelectOptionsList conditions = new SingleSelectOptionsList();
 
     //private SingleSelectOptionsList responsablePerson = new SingleSelectOptionsList();
     //private SingleSelectOptionsList germinationMethod = new SingleSelectOptionsList();
     
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public ListSemental() {
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
             log("ListSemental Initialization Failure", e);
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
 
         //Preguntar si la bandera de busqueda avanzada esta prendida
         if(getgermplasm$SementalSessionBean().isAdvancedSearch()){
 
             breeds.setOptions(SetBreedDropDownData());
             sites.setOptions(SetSitesDropDownData());
             conditions.setOptions(getSelectionListDropDownData(SelectionListEntity.CONDITION.getId()));
             this.getGridpAdvancedSearch().setRendered(true);//Muestra el panel de busqueda avanzada
         }
         //Inicializar el dataprovider si la paginacion es nula y no es filtrado por busquedas
         else if (getgermplasm$SementalSessionBean().getPagination()==null) {
 
             getgermplasm$SementalSessionBean().initDataProvider();
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
     @Override
     public void destroy() {
     }
 
     /**
      * Obtener los datos del drop down de Instituciones
      */
     public Option[] SetBreedDropDownData(){
         List<BreedDTO> instDTOList = this.getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().getAllBreeds();
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(BreedDTO instDTO : instDTOList){
             option = new Option(instDTO.getBreedId(), instDTO.getName().trim());
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
         List<SiteDTO> instDTOList = this.getinventory$GatheringSessionBean().SetSitesDropDownData();
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue("drop_down_default",getMyLocale())+" --");
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
      * Metodo para obtener los datos a mostrar en los drop downs de la
      * ventana de generacion que pertenecen a listas de seleccion
      * @param selectionListEntityId que es el id del enum de listas de seleccion
      * @return
      */
     public Option[] getSelectionListDropDownData(Long selectionListEntityId) {
 
         //getAllSelectionListElementsByCollection
         List<SelectionListDTO> DTOList = this.getgermplasm$AccessionSessionBean().
                 getInventoryFacadeRemote().
                 getAllSelectionListElementsByCollection(selectionListEntityId, getAraSessionBean().getGlobalCollectionId());
         /*List<SelectionListDTO> DTOList = this.getPassportSessionBean().
         getGermplasmFacadeRemote().getElementsForSelectionList(selectionListEntityId);*/
 
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null, " -- " + BundleHelper.getDefaultBundleValue("drop_down_default", getMyLocale()) + " --");
         allOptions.add(option);
 
         //Crear todas las opciones del drop down
         for (SelectionListDTO slDTO : DTOList) {
             option = new Option(slDTO.getValueId(), slDTO.getValueName());
             allOptions.add(option);
         }
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     /**
      * Redirect to NewSemental.jsp
      * @return
      */
     public String btn_new_action()
     {
         getgermplasm$SementalSessionBean().resetValues();
         return "new";
     }
 
     /**
      * Choose the selected semental, and redirect to EditSemental.jsp
      * @return
      */
     public String btn_edit_action()
     {
         int n = this.getDataTableSemental().getRowCount();
         ArrayList<SementalDTO> selected = new ArrayList();
         for (int i = 0; i < n; i++) { //Obtener elementos seleccionados
             this.getDataTableSemental().setRowIndex(i);
             SementalDTO aux = (SementalDTO) this.
                     getDataTableSemental().getRowData();
             if (aux.isSelected()) {
                 selected.add(aux);
             }
         }
         if(selected == null || selected.size() == 0){
             //En caso de que no se seleccione ningun elemento
             MessageBean.setErrorMessageFromBundle("not_selected", this.getMyLocale());
             return null;
         }
         else if(selected.size() == 1){ //En caso de que solo se seleccione un elemento
 
             getgermplasm$SementalSessionBean().setSementalDTO(selected.get(0));
 
             //Llamada al jsp encargado de la edicion de accessiones
             return "edit";
 
         }
         else{ //En caso de que sea seleccion multiple
             MessageBean.setErrorMessageFromBundle("not_yet", this.getMyLocale());
             return null;
         }
     }
 
     /**
      * Choose the selected Semental, search if it has associated semen_gatherings and
      * then ask if you want to delete it.
      * @return
      */
     public String btn_delete_action()
     {
         int n = this.getDataTableSemental().getRowCount();
         ArrayList<SementalDTO> selected = new ArrayList();
         for (int i = 0; i < n; i++) { //Obtener elementos seleccionados
             this.getDataTableSemental().setRowIndex(i);
             SementalDTO aux = (SementalDTO) this.
                     getDataTableSemental().getRowData();
             if (aux.isSelected()) {
                 selected.add(aux);
             }
         }
         if(selected == null || selected.size() == 0)
         {
             //En caso de que no se seleccione ningun elemento
             MessageBean.setErrorMessageFromBundle("not_selected", this.getMyLocale());
             return null;
         }
         else if(selected.size() == 1)
         { //En caso de que solo se seleccione un elemento
 
             //si tiene hijos o asociacions despliega el mensaje de alerta
             if(getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().
                     haveSemenGathering(
                     selected.get(0).getSementalId()))
             {
                 this.getAlertMessage().setRendered(true);
                 this.getMainPanel().setRendered(false);
                 getgermplasm$SementalSessionBean().setDeleteSemental(
                         selected.get(0).getSementalId());
             }
             else
             {
                 //delete the accession
                 getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().
                         deleteSemental(selected.get(0).getSementalId());
                 //refresh the list
                 getgermplasm$SementalSessionBean().getPagination().deleteItem();
                 getgermplasm$SementalSessionBean().getPagination().refreshList();
                 getgermplasm$SemenGatheringSessionBean().setPagination(null);
                 MessageBean.setSuccessMessageFromBundle("delete_semental_success", this.getMyLocale());
             }
 
             return null;
         }
         else{ //En caso de que sea seleccion multiple
             MessageBean.setErrorMessageFromBundle("not_yet", this.getMyLocale());
             return null;
         }
     }
 
     /**
      * Confirm the delete semental action
      * @return
      */
     public String btn_confirm_delete_action()
     {
         //delete the accession
         getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().
                 deleteSemental(
                 getgermplasm$SementalSessionBean().getDeleteSemental());
         //refresh the list
         getgermplasm$SementalSessionBean().getPagination().deleteItem();
         getgermplasm$SementalSessionBean().getPagination().refreshList();
         getgermplasm$SemenGatheringSessionBean().setPagination(null);
 
         //show and hidde panels
         this.getMainPanel().setRendered(true);
         this.getAlertMessage().setRendered(false);
         MessageBean.setSuccessMessageFromBundle("delete_semental_success", this.getMyLocale());
 
         return null;
     }
 
     /**
      * cancel the delete action
      * @return
      */
     public String btn_cancel_delete_action()
     {
         //show and hidde panels
         this.getMainPanel().setRendered(true);
         this.getAlertMessage().setRendered(false);
         return null;
     }
 
     /**
      * Performed the simple search for sementals
      * @return
      */
     public String btnSimpleSearch_action()
     {
         String userInput = "";
         if(this.getTxSimpleSearch().getValue()!= null)
             userInput = this.getTxSimpleSearch().getValue().toString();
         userInput = userInput.trim();
         System.out.println(1);
         if(userInput.length()==0){
             //Se desabilitan las banderas de busqueda simple y avanzada
             this.getgermplasm$SementalSessionBean().setQueryModeSimple(false);
             this.getgermplasm$SementalSessionBean().setQueryMode(false);
             //Finalmente se setea el data provider del paginador con los datos por default
             this.getgermplasm$SementalSessionBean().getPagination().setTotalResults
                     (getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().
                     countAllSemental().intValue());
         }
         else{
             //Setear el string para consulta simple del SessionBean
             this.getgermplasm$SementalSessionBean().setConsultaSimple(userInput);
             //Indicarle al SessionBean que el paginador debe "trabajar" en modo busqueda simple
             this.getgermplasm$SementalSessionBean().setQueryModeSimple(true);
             //Desabilitar la bandera de busqueda avanzada
             this.getgermplasm$SementalSessionBean().setQueryMode(false);
             //Finalmente se inicializa el data provider del paginador con los resultados de la consulta
             this.getgermplasm$SementalSessionBean().getPagination().setTotalResults
                     (getgermplasm$SementalSessionBean().getGermplasmFacadeRemote().
                     countSementalSimpleSearch(
                     userInput).intValue());
         }
         //set the first result of the query
         this.getgermplasm$SementalSessionBean().getPagination().firstResults();
         return null;
     }
 
     /**
      * Display the advance search options
      * @return
      */
     public String btnAdvSearch_action()
     {
         boolean advanced = getgermplasm$SementalSessionBean().isAdvancedSearch();
         if(advanced==false){ //Mostrar panel de busqueda avanzada
             getgermplasm$SementalSessionBean().setAdvancedSearch(true);
             //Deshabilitar busqueda simple
             this.getTxSimpleSearch().setRendered(false);
             this.getBtnSimpleSearch().setRendered(false);
             //Cambia el text del boton de busqueda avanzada
             this.getBtnAdvSearch().setValue(BundleHelper.getDefaultBundleValue("advanced_search_specimen_back",getMyLocale()));
             return null;
         }
         else if(advanced==true){
             this.getgermplasm$SementalSessionBean().setAdvancedSearch(false);
             //Ocultar el panel
             this.gridpAdvancedSearch.setRendered(false);
             //Habilitar busqueda simple
             this.getTxSimpleSearch().setRendered(true);
             this.getBtnSimpleSearch().setRendered(true);
             //Cambia el text del boton de busqueda avanzada
             this.getBtnAdvSearch().setValue(BundleHelper.getDefaultBundleValue("advanced_search",getMyLocale()));
 
             //Reestablecer los valores por defecto de los textfields
             getgermplasm$SementalSessionBean().setQuerySementalDTO(new SementalDTO());
         }
         return null;
     }
 
     /**
      * Performed the advance search action
      * @return
      */
     public String btnAdvSearchSemental_action()
     {
         GregorianCalendar birth = new GregorianCalendar();
         Date bdate = this.getBirthDate().getSelectedDate();
         if(bdate!=null){
             birth.setTime(bdate);
             getgermplasm$SementalSessionBean().getQuerySementalDTO().setBirthDate(birth);
         }
 
 
         //Indicarle al SessionBean que el paginador debe "trabajar" en modo busqueda avanzada
         this.getgermplasm$SementalSessionBean().setQueryMode(true);
         //Desabilitar la bandera de busqueda simple
         this.getgermplasm$SementalSessionBean().setQueryModeSimple(false);
         //Finalmente se inicializa el data provider del paginador con los resultados de la consulta
         this.getgermplasm$SementalSessionBean().getPagination().setTotalResults(
                 this.getgermplasm$SementalSessionBean().
                 getGermplasmFacadeRemote().
                 countSementalAdvancedSearch(
                 getgermplasm$SementalSessionBean().
                 getQuerySementalDTO()).intValue());
 
         this.getgermplasm$SementalSessionBean().getPagination().firstResults();
         this.getgermplasm$SementalSessionBean().getPagination().refreshList();
 
         this.getTxSimpleSearch().setValue("");
 
         return null;
     }
 
     
     /**
      * @return the myLocale
      */
     public Locale getMyLocale() {
         return this.getAraSessionBean().getCurrentLocale();
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
     protected AccessionSessionBean getgermplasm$AccessionSessionBean() {
         return (AccessionSessionBean) getBean("germplasm$AccessionSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected SelectionListSessionBean getadmin$SelectionListSessionBean() {
         return (SelectionListSessionBean) getBean("admin$SelectionListSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected PersonSessionBean getadmin$PersonSessionBean() {
         return (PersonSessionBean) getBean("admin$PersonSessionBean");
     }
 
     protected SementalSessionBean getgermplasm$SementalSessionBean() {
         return (SementalSessionBean) getBean("germplasm$SementalSessionBean");
     }
 
     protected SemenGatheringSessionBean getgermplasm$SemenGatheringSessionBean() {
         return (SemenGatheringSessionBean) getBean("germplasm$SemenGatheringSessionBean");
     }
 
 
     /**
      * @return the quantityTotal
      */
     public String getQuantityTotal() {
         quantityTotal = this.getgermplasm$SementalSessionBean().getQuantityTotal();
         return quantityTotal;
     }
 
     /**
      * @param quantityTotal the quantityTotal to set
      */
     public void setQuantityTotal(String quantityTotal) {
         this.quantityTotal = quantityTotal;
     }
 
     /**
      * @return the dataTableSemental
      */
     public HtmlDataTable getDataTableSemental() {
         return dataTableSemental;
     }
 
     /**
      * @param dataTableSemental the dataTableSemental to set
      */
     public void setDataTableSemental(HtmlDataTable dataTableSemental) {
         this.dataTableSemental = dataTableSemental;
     }
 
     /**
      * @return the gridpAdvancedSearch
      */
     public HtmlPanelGrid getGridpAdvancedSearch() {
         return gridpAdvancedSearch;
     }
 
     /**
      * @param gridpAdvancedSearch the gridpAdvancedSearch to set
      */
     public void setGridpAdvancedSearch(HtmlPanelGrid gridpAdvancedSearch) {
         this.gridpAdvancedSearch = gridpAdvancedSearch;
     }
 
     /**
      * @return the alertMessage
      */
     public HtmlPanelGrid getAlertMessage() {
         return alertMessage;
     }
 
     /**
      * @param alertMessage the alertMessage to set
      */
     public void setAlertMessage(HtmlPanelGrid alertMessage) {
         this.alertMessage = alertMessage;
     }
 
     /**
      * @return the mainPanel
      */
     public HtmlPanelGrid getMainPanel() {
         return mainPanel;
     }
 
     /**
      * @param mainPanel the mainPanel to set
      */
     public void setMainPanel(HtmlPanelGrid mainPanel) {
         this.mainPanel = mainPanel;
     }
 
     /**
      * @return the txSimpleSearch
      */
     public HtmlInputText getTxSimpleSearch() {
         return txSimpleSearch;
     }
 
     /**
      * @param txSimpleSearch the txSimpleSearch to set
      */
     public void setTxSimpleSearch(HtmlInputText txSimpleSearch) {
         this.txSimpleSearch = txSimpleSearch;
     }
 
     /**
      * @return the btnSimpleSearch
      */
     public HtmlCommandButton getBtnSimpleSearch() {
         return btnSimpleSearch;
     }
 
     /**
      * @param btnSimpleSearch the btnSimpleSearch to set
      */
     public void setBtnSimpleSearch(HtmlCommandButton btnSimpleSearch) {
         this.btnSimpleSearch = btnSimpleSearch;
     }
 
     /**
      * @return the btnAdvSearch
      */
     public HtmlCommandButton getBtnAdvSearch() {
         return btnAdvSearch;
     }
 
     /**
      * @param btnAdvSearch the btnAdvSearch to set
      */
     public void setBtnAdvSearch(HtmlCommandButton btnAdvSearch) {
         this.btnAdvSearch = btnAdvSearch;
     }
 
     /**
      * @return the birthDate
      */
     public //Boton busqueda avanzada
     //componentes para busuqedas avanzadas
     Calendar getBirthDate() {
         return birthDate;
     }
 
     /**
      * @param birthDate the birthDate to set
      */
     public void setBirthDate(Calendar birthDate) {
         this.birthDate = birthDate;
     }
 
     /**
      * @return the breeds
      */
     public SingleSelectOptionsList getBreeds() {
         return breeds;
     }
 
     /**
      * @param breeds the breeds to set
      */
     public void setBreeds(SingleSelectOptionsList breeds) {
         this.breeds = breeds;
     }
 
     /**
      * @return the sites
      */
     public SingleSelectOptionsList getSites() {
         return sites;
     }
 
     /**
      * @param sites the sites to set
      */
     public void setSites(SingleSelectOptionsList sites) {
         this.sites = sites;
     }
 
     /**
      * @return the conditions
      */
     public SingleSelectOptionsList getConditions() {
         return conditions;
     }
 
     /**
      * @param conditions the conditions to set
      */
     public void setConditions(SingleSelectOptionsList conditions) {
         this.conditions = conditions;
     }
     
 }
 
