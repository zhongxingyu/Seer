 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.web.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import eionet.meta.dao.domain.Attribute;
 import eionet.meta.dao.domain.DataSet;
 import eionet.meta.service.IDataService;
 import eionet.meta.service.ServiceException;
 import eionet.meta.service.data.DataElementsFilter;
 import eionet.meta.service.data.DataElementsResult;
 import eionet.util.SecurityUtil;
 
 /**
  * Data elements search page controller.
  *
  * @author Juhan Voolaid
  */
 @UrlBinding("/searchelements/{$event}")
 public class SearchDataElementsActionBean extends AbstractActionBean {
 
     /** Select option values. */
     private List<String> regStatuses;
 
     /** Select option values. */
     private List<DataSet> dataSets;
 
     /** Search filter. */
     private DataElementsFilter filter;
 
     /** Search result object. */
     private DataElementsResult result;
 
     /** Filtering attributes that are available to add. */
     private List<Attribute> addableAttributes;
 
     /** Added filtering attributes */
     private List<Attribute> addedAttributes;
 
     /** Id of the attribute to add. */
     private int addAttr;
 
     /** Id of the attribute to delete. */
     private int delAttr;
 
     /** Data service. */
     @SpringBean
     private IDataService dataService;
 
     /**
      * Default form view action.
      *
      * @return
      * @throws ServiceException
      */
     @DefaultHandler
     public Resolution view() throws ServiceException {
         if (addAttr != 0) {
             return addAttribute();
         }
 
         if (delAttr != 0) {
             return deleteAttribute();
         }
 
         filter = new DataElementsFilter();
 
         regStatuses = new ArrayList<String>();
         regStatuses.add("Released");
         regStatuses.add("Recorded");
         regStatuses.add("Qualified");
         regStatuses.add("Candidate");
         regStatuses.add("Incomplete");
 
         dataSets = dataService.getDataSets();
 
         filter.getAttributes().add(dataService.getAttributeByName("Name"));
         filter.getAttributes().add(dataService.getAttributeByName("Definition"));
         filter.getAttributes().add(dataService.getAttributeByName("Keyword"));
 
         addableAttributes = dataService.getDataElementAttributes();
 
         return new ForwardResolution("/pages/dataElementSearch.jsp");
     }
 
     /**
      * Search action.
      *
      * @return
      * @throws ServiceException
      */
     public Resolution search() throws ServiceException {
         result = dataService.searchDataElements(filter);
         return new ForwardResolution("/pages/dataElementsResult.jsp");
     }
 
     /**
      * Add attribute action.
      *
      * @return
      * @throws ServiceException
      */
     private Resolution addAttribute() throws ServiceException {
         regStatuses = new ArrayList<String>();
         regStatuses.add("Released");
         regStatuses.add("Recorded");
         regStatuses.add("Qualified");
         regStatuses.add("Candidate");
         regStatuses.add("Incomplete");
 
         dataSets = dataService.getDataSets();
         addableAttributes = dataService.getDataElementAttributes();
 
         addSelectedAttribute();
         filterAddableAttributes();
 
         return new ForwardResolution("/pages/dataElementSearch.jsp");
     }
 
     /**
      * Remove attribute action.
      *
      * @return
      * @throws ServiceException
      */
     private Resolution deleteAttribute() throws ServiceException {
         regStatuses = new ArrayList<String>();
         regStatuses.add("Released");
         regStatuses.add("Recorded");
         regStatuses.add("Qualified");
         regStatuses.add("Candidate");
         regStatuses.add("Incomplete");
 
         dataSets = dataService.getDataSets();
         addableAttributes = dataService.getDataElementAttributes();
 
         deleteSelectedAttribute();
         filterAddableAttributes();
 
         return new ForwardResolution("/pages/dataElementSearch.jsp");
     }
 
     /**
      * Deletes the attribute from addedAttributes collection.
      */
     private void deleteSelectedAttribute() {
         for (Attribute a : addedAttributes) {
             if (delAttr == a.getId()) {
                 addedAttributes.remove(a);
                 break;
             }
         }
         delAttr = 0;
     }
 
     /**
      * Adds the attribute to addedAttributes collection.
      */
     private void addSelectedAttribute() {
         if (addedAttributes == null) {
             addedAttributes = new ArrayList<Attribute>();
         }
         for (Attribute a : addableAttributes) {
             if (addAttr == a.getId()) {
                 addedAttributes.add(a);
                 break;
             }
         }
         addAttr = 0;
     }
 
     /**
      * Filters out attributes that are already included in the form.
      *
      * @throws ServiceException
      */
     private void filterAddableAttributes() throws ServiceException {
         // Remove already included attributes
         for (Attribute a : filter.getAttributes()) {
             if (addableAttributes.contains(a)) {
                 addableAttributes.remove(a);
             }
         }
 
         // Remove the added attributes
         if (addedAttributes != null) {
             for (Attribute a : addedAttributes) {
                 if (addableAttributes.contains(a)) {
                     addableAttributes.remove(a);
                 }
             }
         }
     }
 
     /**
      * Returns true, when user has permission to add common element.
      *
      * @return
      */
     public boolean isPermissionToAdd() {
         try {
             return getUser() != null && SecurityUtil.hasPerm(getUser().getUserName(), "/elements", "i");
         } catch (Exception e) {
             LOGGER.error("Failed to get user permission: " + e.getMessage(), e);
             return false;
         }
     }
 
     /**
      * @return the filter
      */
     public DataElementsFilter getFilter() {
         return filter;
     }
 
     /**
      * @param filter
      *            the filter to set
      */
     public void setFilter(DataElementsFilter filter) {
         this.filter = filter;
     }
 
     /**
      * @return the regStatuses
      */
     public List<String> getRegStatuses() {
         return regStatuses;
     }
 
     /**
      * @return the dataSets
      */
     public List<DataSet> getDataSets() {
         return dataSets;
     }
 
     /**
      * @return the result
      */
     public DataElementsResult getResult() {
         return result;
     }
 
     /**
      * @return the addAttr
      */
     public int getAddAttr() {
         return addAttr;
     }
 
     /**
      * @param addAttr
      *            the addAttr to set
      */
     public void setAddAttr(int addAttr) {
         this.addAttr = addAttr;
     }
 
     /**
      * @return the addableAttributes
      */
     public List<Attribute> getAddableAttributes() {
         return addableAttributes;
     }
 
     /**
      * @return the addedAttributes
      */
     public List<Attribute> getAddedAttributes() {
         return addedAttributes;
     }
 
     /**
      * @param addedAttributes
      *            the addedAttributes to set
      */
     public void setAddedAttributes(List<Attribute> addedAttributes) {
         this.addedAttributes = addedAttributes;
     }
 
     /**
      * @return the delAttr
      */
     public int getDelAttr() {
         return delAttr;
     }
 
     /**
      * @param delAttr
      *            the delAttr to set
      */
     public void setDelAttr(int delAttr) {
         this.delAttr = delAttr;
     }
 
 }
