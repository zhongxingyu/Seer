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
 
 package org.inbio.ara.transaction;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Button;
 import com.sun.webui.jsf.component.Calendar;
 import com.sun.webui.jsf.component.Tab;
 import com.sun.webui.jsf.component.TabSet;
 import com.sun.webui.jsf.model.Option;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import javax.faces.FacesException;
 import org.inbio.ara.AraSessionBean;
 import org.inbio.ara.dto.agent.InstitutionDTO;
 import org.inbio.ara.dto.inventory.PersonDTO;
 import org.inbio.ara.dto.inventory.SelectionListDTO;
 import org.inbio.ara.dto.inventory.SelectionListEntity;
 import org.inbio.ara.dto.transaction.TransactionDTO;
 import org.inbio.ara.util.BundleHelper;
 import org.inbio.ara.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  *
  * @version NewTransaction.java
  * @version Created on Feb 18, 2010, 4:53:29 PM
  * @author echinchilla
  */
 
 public class NewTransaction extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
 
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Variables "bindeadas" con interfaz">
     private Calendar clTransactionDate = new Calendar();
     private Calendar clExpirationDate = new Calendar();
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="OptionsList's">
     /**
      * En esta variable se setearan los datos del drop down de sender person.
      *
      * NOTA: personData se está usando tanto para senderPerson como para
      * receiverPerson. Esto, probablemete, deba cambiar.
      */
     private SingleSelectOptionsList institutionData = new SingleSelectOptionsList();
     private SingleSelectOptionsList senderPersonData = new SingleSelectOptionsList();
     private SingleSelectOptionsList receiverPersonData = new SingleSelectOptionsList();
     private SingleSelectOptionsList transactionTypeData = new SingleSelectOptionsList();
     // </editor-fold>
 
     private Button btnAddSpecimens = new Button();
     
     private TabSet tabSet = new TabSet();
     private Tab tabAddTransactedSpecimens = new Tab();
 
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public NewTransaction() {
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
             log("NewTransaction Initialization Failure", e);
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
 
         //<<<<<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>>>>>\\
         java.util.Calendar currentDate = java.util.Calendar.getInstance();
         this.clTransactionDate.setSelectedDate(currentDate.getTime());
         //<<<<<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>>>>>\\
 
         //Para el DropDown/selectionList
         this.getTransactionTypeData().setOptions(getSelectionListDropDownData
                 (SelectionListEntity.TRANSACTION_TYPE.getId()));
 
         this.getInstitutionData().setOptions(setInstitutionDropDownData());
         this.getSenderPersonData().setOptions(setSenderPersonDropDownData());
         this.getReceiverPersonData().setOptions(setReceiverPersonDropDownData());
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
      * <p>Return a reference to the scoped data bean.</p>
      *
      * @return reference to the scoped data bean
      */
     protected TransactionSessionBean getTransactionSessionBean() {
         return (TransactionSessionBean) getBean("transaction$TransactionSessionBean");
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
      * @return the clTransactionDate
      */
     public Calendar getClTransactionDate() {
         return clTransactionDate;
     }
 
     /**
      * @param clTransactionDate the clTransactionDate to set
      */
     public void setClTransactionDate(Calendar clTransactionDate) {
         this.clTransactionDate = clTransactionDate;
     }
 
     /**
      * @return the clExpirationDate
      */
     public Calendar getClExpirationDate() {
         return clExpirationDate;
     }
 
     /**
      * @param clExpirationDate the clExpirationDate to set
      */
     public void setClExpirationDate(Calendar clExpirationDate) {
         this.clExpirationDate = clExpirationDate;
     }
 
     /**
      * @return the transactionTypeData
      */
     public SingleSelectOptionsList getTransactionTypeData() {
         return transactionTypeData;
     }
 
     /**
      * @param transactionTypeData the transactionTypeData to set
      */
     public void setTransactionTypeData(SingleSelectOptionsList transactionTypeData) {
         this.transactionTypeData = transactionTypeData;
     }
 
     /**
      * @return the myLocales
      */
     public Locale getMyLocale() {
         return this.getAraSessionBean().getCurrentLocale();
     }
 
      /**
      * Metodo ejecutado por el boton de crear nueva persona
      * @return
      */
     public String btnSaveTransaction_action() {
 
         TransactionDTO transactionDTO = getTransactionSessionBean().getCurrentTransaction();
 
         GregorianCalendar transactionDateGC = new GregorianCalendar();
         GregorianCalendar expirationDateGC = new GregorianCalendar();
         Date transactionDateD = this.getClTransactionDate().getSelectedDate();
         Date expirationDateD = this.getClExpirationDate().getSelectedDate();
 
         if (transactionDateD != null) {
             transactionDateGC.setTime(transactionDateD);
             transactionDTO.setTransactionDate(transactionDateGC);
         }
         if (expirationDateD != null) {
             expirationDateGC.setTime(expirationDateD);
             transactionDTO.setExpirationDate(expirationDateGC);
         }
         else {
             transactionDTO.setExpirationDate(null);
         }
         
         if (transactionDateD != null && expirationDateD != null) {
             if (transactionDateD.getTime() > expirationDateD.getTime()) {
                 MessageBean.setErrorMessageFromBundle("error_transaction_date_expiration_date", this.getMyLocale());
                 return null;
             }
         }
 
         transactionDTO.setUserName(getAraSessionBean().getGlobalUserName());
         transactionDTO.setCollectionId(getAraSessionBean().getGlobalCollectionId());
 
         try{
             //Mandar a persistir
             if (transactionDateD == null) {
                 MessageBean.setErrorMessageFromBundle("error_transaction_date", this.getMyLocale());
                 return null;
             }
             else if(transactionDTO.getSenderPersonId() == null) {
                 MessageBean.setErrorMessageFromBundle("error_sender_person", this.getMyLocale());
                 return null;
             }
             else if(transactionDTO.getTransactionTypeId() == null) {
                 MessageBean.setErrorMessageFromBundle("error_transaction_type", this.getMyLocale());
                 return null;
             }
             this.getTransactionSessionBean().setCurrentTransaction(transactionDTO);
             TransactionDTO aux = this.getTransactionSessionBean().saveTransaction(transactionDTO);
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ID = " + aux.getTransactionId().toString());
             this.getTransactionSessionBean().setCurrentTransaction(aux);
             //Setear el currentDTO con el DTO recien persistido
             
         }
         catch(Exception e){
            MessageBean.setErrorMessageFromBundle("error", this.getMyLocale());
             return null;
         }
 
         //Refrescar la lista de transacciones
         this.getTransactionSessionBean().getPagination().addItem();
         this.getTransactionSessionBean().getPagination().refreshList();
 
         //Notificar al usuario
         MessageBean.setSuccessMessageFromBundle("create_transaction_succces", this.getMyLocale());
 
 
         this.getTransactionSessionBean().setTransactionJustCreated(true);
         return "editNewTransaction";
     }
 
     /**
      * Metodo para obtener los datos a mostrar en los drop downs de la
      * ventana de generacion que pertenecen a listas de seleccion
      * @param selectionListEntityId que es el id del enum de listas de seleccion
      * @return
      */
     public Option[] getSelectionListDropDownData(Long selectionListEntityId) {
 
         List<SelectionListDTO> DTOList = this.getTransactionSessionBean().
                 getInventoryFacade().
                 getAllSelectionListElementsByCollection
                 (selectionListEntityId, getAraSessionBean().getGlobalCollectionId());
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null, " -- " + BundleHelper.getDefaultBundleValue
                 ("drop_down_default", getMyLocale()) + " --");
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
      * @return the institutionData1
      */
     public SingleSelectOptionsList getInstitutionData() {
         return institutionData;
     }
 
     /**
      * @param institutionData1 the institutionData1 to set
      */
     public void setInstitutionData(SingleSelectOptionsList institutionData) {
         this.institutionData = institutionData;
     }
 
     /**
      * @return the senderPersonData1
      */
     public SingleSelectOptionsList getSenderPersonData() {
         return senderPersonData;
     }
 
     /**
      * @param senderPersonData1 the senderPersonData1 to set
      */
     public void setSenderPersonData(SingleSelectOptionsList senderPersonData) {
         this.senderPersonData = senderPersonData;
     }
 
     /**
      * @return the receiverPersonData1
      */
     public SingleSelectOptionsList getReceiverPersonData() {
         return receiverPersonData;
     }
 
     /**
      * @param receiverPersonData1 the receiverPersonData1 to set
      */
     public void setReceiverPersonData(SingleSelectOptionsList receiverPersonData) {
         this.receiverPersonData = receiverPersonData;
     }
 
     public Option[] setInstitutionDropDownData(){
         List<InstitutionDTO> institutionDTOList = this.getTransactionSessionBean().
                 setInstitutionDropDownData();
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(InstitutionDTO institutionDTO : institutionDTOList){
             option = new Option(institutionDTO.getInstitutionId(), institutionDTO.getInstitutionCode().trim());
             allOptions.add(option);
         }
         option = new Option(new Long(-1)," -- "+BundleHelper.getDefaultBundleValue
                 ("no_institution_associated",getMyLocale())+" --");
         allOptions.add(option);
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     public Option[] setSenderPersonDropDownData(){
         TransactionSessionBean tsb = this.getTransactionSessionBean();
         List<PersonDTO> personDTOList =
                 tsb.setSenderPersonDropDownData(tsb.getCurrentTransaction().getSenderInstitutionId());
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(PersonDTO personDTO : personDTOList){
             option = new Option(personDTO.getPersonKey(), personDTO.getNaturalLongName().trim());
             allOptions.add(option);
         }
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     public Option[] setReceiverPersonDropDownData(){
         TransactionSessionBean tsb = this.getTransactionSessionBean();
         List<PersonDTO> personDTOList =
                 tsb.setReceiverPersonDropDownData(tsb.getCurrentTransaction().getReceiverInstitutionId());
         ArrayList<Option> allOptions = new ArrayList<Option>();
         Option[] allOptionsInArray;
         Option option;
         //Crear opcion titulo
         option = new Option(null," -- "+BundleHelper.getDefaultBundleValue
                 ("drop_down_default",getMyLocale())+" --");
         allOptions.add(option);
         //Crear todas las opciones del drop down
         for(PersonDTO personDTO : personDTOList){
             option = new Option(personDTO.getPersonKey(), personDTO.getNaturalLongName().trim());
             allOptions.add(option);
         }
         //Sets the elements in the SingleSelectedOptionList Object
         allOptionsInArray = new Option[allOptions.size()];
         return allOptions.toArray(allOptionsInArray);
     }
 
     private void setSenderPersonListOptions(List<PersonDTO> personList) {
 
          List<Option> list = new ArrayList<Option>();
 
         for (PersonDTO personDTO : personList) {
                 list.add(new Option(personDTO.getPersonKey(), personDTO.getNaturalLongName()));
         }
 
         this.getSenderPersonData().setOptions(list.toArray(new Option[list.size()]));
     }
 
     public String updateSenderPersonListAction() {
 
 	TransactionSessionBean tsb =
         this.getTransactionSessionBean();
 
 	List<PersonDTO> personList =
               tsb.getTransactionFacade().getPersonsByInstitutionId(tsb.getCurrentTransaction().
               getSenderInstitutionId());
         this.setSenderPersonListOptions(personList);
 
         return null;
     }
 
     private void setReceiverPersonListOptions(List<PersonDTO> personList) {
 
         List<Option> list = new ArrayList<Option>();
 
         for (PersonDTO personDTO : personList) {
                 list.add(new Option(personDTO.getPersonKey(), personDTO.getNaturalLongName()));
         }
 
         this.getSenderPersonData().setOptions(list.toArray(new Option[list.size()]));
     }
 
     public String updateReceiverPersonListAction() {
 
 	TransactionSessionBean tsb =
         this.getTransactionSessionBean();
 
 	List<PersonDTO> personList =
               tsb.getTransactionFacade().getPersonsByInstitutionId(tsb.getCurrentTransaction().
               getReceiverInstitutionId());
         this.setReceiverPersonListOptions(personList);
 
         return null;
     }
 
     /**
      * @return the btnAddSpecimens
      */
     public // </editor-fold>
     Button getBtnAddSpecimens() {
         return btnAddSpecimens;
     }
 
     /**
      * @param btnAddSpecimens the btnAddSpecimens to set
      */
     public void setBtnAddSpecimens(Button btnAddSpecimens) {
         this.btnAddSpecimens = btnAddSpecimens;
     }
 
     /**
      * @return the tabContainer
      */
     public TabSet getTabSet() {
         return tabSet;
     }
 
     /**
      * @param tabContainer the tabContainer to set
      */
     public void setTabSet(TabSet tabSet) {
         this.tabSet = tabSet;
     }
 
     /**
      * @return the tabAddTransactedSpecimens
      */
     public Tab getTabAddTransactedSpecimens() {
         return tabAddTransactedSpecimens;
     }
 
     /**
      * @param tabAddTransactedSpecimens the tabAddTransactedSpecimens to set
      */
     public void setTabAddTransactedSpecimens(Tab tabAddTransactedSpecimens) {
         this.tabAddTransactedSpecimens = tabAddTransactedSpecimens;
     }
     
 }
 
