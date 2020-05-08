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
 package org.inbio.ara.web.gathering;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.Calendar;
 import com.sun.webui.jsf.component.PageAlert;
 import com.sun.webui.jsf.component.TextField;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import javax.faces.FacesException;
 import org.inbio.ara.facade.util.SearchManagerRemote;
 import org.inbio.ara.persistence.gathering.GatheringObservation;
 import org.inbio.ara.web.StringUtils;
 import org.inbio.ara.web.util.BundleHelper;
 
 public class SearchGathering extends AbstractPageBean {
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
 
     private TextField txt_identification = new TextField();
 
     public TextField getTxt_identification() {
         return txt_identification;
     }
 
     public void setTxt_identification(TextField tf) {
         this.txt_identification = tf;
     }
     private TextField txt_locality = new TextField();
 
     public TextField getTxt_locality() {
         return txt_locality;
     }
 
     public void setTxt_locality(TextField tf) {
         this.txt_locality = tf;
     }
     private Calendar cal_init_date = new Calendar();
 
     public Calendar getCal_init_date() {
         return cal_init_date;
     }
 
     public void setCal_init_date(Calendar c) {
         this.cal_init_date = c;
     }
     private Calendar cal_final_date = new Calendar();
 
     public Calendar getCal_final_date() {
         return cal_final_date;
     }
 
     public void setCal_final_date(Calendar c) {
         this.cal_final_date = c;
     }
     private TextField txt_resposible = new TextField();
 
     public TextField getTxt_resposible() {
         return txt_resposible;
     }
 
     public void setTxt_resposible(TextField tf) {
         this.txt_resposible = tf;
     }
     private TextField txt_collection = new TextField();
 
     public TextField getTxt_collection() {
         return txt_collection;
     }
 
     public void setTxt_collection(TextField tf) {
         this.txt_collection = tf;
     }
     private PageAlert searchAlert = new PageAlert();
 
     public PageAlert getSearchAlert() {
         return searchAlert;
     }
 
     public void setSearchAlert(PageAlert pa) {
         this.searchAlert = pa;
     }
 
     // </editor-fold>
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public SearchGathering() {
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
             log("SearchGathering Initialization Failure", e);
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
 
     public String searchButton_action() {
         if (!isValidInput()) {
             return null;
         }
         Hashtable searchCriteria = new Hashtable();
 
         if (txt_identification.getText() != null) {
             searchCriteria.put("table.id = ",
                         txt_identification.getText().toString());
         }
         if (txt_locality.getText() != null) {
             searchCriteria.put("lower(table.site.description) like ",
                 "'%" + txt_locality.getText().toString().toLowerCase() + "%'");
         }
         if (cal_init_date.getText() != null) {
             searchCriteria.put("table.initialDate = ",
             "'"+to_char_date(cal_init_date.getSelectedDate(), "yyyy-MM-dd")+"'");
         }
         if (cal_final_date.getText() != null) {
             searchCriteria.put("table.finalDate = ",
             "'"+to_char_date(cal_final_date.getSelectedDate(), "yyyy-MM-dd")+"'");
         }
         if (txt_resposible.getText() != null) {
             String resp = txt_resposible.getText().toString().toLowerCase();
             searchCriteria.put("lower(table.responsiblePerson.firstName) like ",
                "'%" + resp + "%'" +
                " or lower(table.responsiblePerson.lastName) like " +
                "'%" + resp + "%'" +
                " or lower(table.responsiblePerson.secondLastName) like " +
                "'%" + resp + "%'");
         }
         if (txt_collection.getText() != null) {
             searchCriteria.put("lower(table.collection.name) like ",
                "'%" + txt_collection.getText().toString().toLowerCase() + "%'");
         }
 
         GatheringSessionBeanV2 gsb = getGatheringSessionBean();
         SearchManagerRemote smr = gsb.getSearchManager();
         gsb.setSearchCriteria(searchCriteria);
 
         if(gsb.getPagination() != null)
            gsb.getPagination().firstResults();
 
         Long resultSet = smr.countResult(GatheringObservation.class, searchCriteria);
         if (resultSet != null || resultSet != 0) {
             getGatheringSessionBean().setFiltered(true);
             return "gath_obs_list";
         }
         return null;
 
         /*
         List resultSet = smr.makeQuery(GatheringObservation.class, searchCriteria);
         
         if (resultSet != null) {
             getGatheringSessionBean().setFiltered(true);
             getGatheringSessionBean().getGatheringDataProvider().clearObjectList();
             getGatheringSessionBean().getGatheringDataProvider().setList(resultSet);
             return "gath_obs_list"; //back to the list
         }
         
         return null;*/
     }
 
     private boolean isValidInput() {
         boolean isValid = true;
         String message = "";
         if (txt_identification.getText() != null) {
             String id = txt_identification.getText().toString();
             if (!StringUtils.isNumeric(id)) {
                 message += BundleHelper.getDefaultBundleValue("id_error");
                 isValid = false;
             }
         }
         if (txt_locality.getText() != null) {
             String locality = txt_locality.getText().toString();
             if (!locality.matches("[0-9a-zA-ZáéíóúÁÉÍÓÚñÑ -]*")) {
                 message += BundleHelper.getDefaultBundleValue("locality_error");
                 isValid = false;
             }
         }
         if (cal_init_date.getText() != null) {
             Date d = new Date();
             d = cal_init_date.getSelectedDate();
         }
         if (cal_final_date.getText() != null) {
             Date d = new Date();
             d = cal_final_date.getSelectedDate();
         }
         if (txt_resposible.getText() != null) {
             String responsible = txt_resposible.getText().toString();
             if (!responsible.matches("[0-9a-zA-ZáéíóúÁÉÍÓÚñÑ. -]*")) {
                 message += BundleHelper.getDefaultBundleValue("responsible_error");
                 isValid = false;
             }
         }
         if (txt_collection.getText() != null) {
             String collection = txt_collection.getText().toString();
             if (!collection.matches("[0-9a-zA-ZáéíóúÁÉÍÓÚñÑ -]*")) {
                 message += BundleHelper.getDefaultBundleValue("collection_error");
                 isValid = false;
             }
         }
 
         if (!isValid) {
             searchAlert.setRendered(true);
             searchAlert.setType("error");
             searchAlert.setSummary(BundleHelper.getDefaultBundleValue("validation_error"));
             searchAlert.setDetail(message);
         }
         return isValid;
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GatheringSessionBeanV2 getGatheringSessionBean() {
         return (GatheringSessionBeanV2)getBean("gathering$GatheringSessionBeanV2");
     }
 
     private String to_char_date(Date date, String format) {
         if(date != null){
             SimpleDateFormat df = new SimpleDateFormat(format);
             return df.format(date);
         }
         else return "";
     }
 
 
 }
 
