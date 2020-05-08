 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
  * and the Lesotho Land Administration Authority (LAA). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
  *       endorse or promote products derived from this software without specific prior
  * 	  written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.clients.beans.cadastre;
 
 import java.math.BigDecimal;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import org.jdesktop.observablecollections.ObservableList;
 import org.sola.clients.beans.address.AddressBean;
 import org.sola.clients.beans.controls.SolaList;
 import org.sola.clients.beans.converters.TypeConverters;
 import org.sola.clients.beans.validation.Localized;
 import org.sola.common.messaging.ClientMessage;
 import org.sola.services.boundary.wsclients.WSManager;
 import org.sola.webservices.transferobjects.cadastre.CadastreObjectTO;
 import org.sola.webservices.transferobjects.EntityAction;
 
 /** 
  * Contains properties and methods to manage <b>Cadastre</b> object of the 
  * domain model. Could be populated from the {@link CadastreObjectTO} object.
  */
 public class CadastreObjectBean extends CadastreObjectSummaryBean {
 
     public static final String SUBPLOT_TYPE = "subplot";
     public static final String GEOM_POLYGON_PROPERTY = "geomPolygon";
     public static final String SELECTED_PROPERTY = "selected";
     public static final String PENDING_STATUS = "pending";
     public static final String ADDRESS_LIST_PROPERTY = "addressList";
     public static final String SELECTED_ADDRESS_PROPERTY = "selectedAddress";
     public static final String OFFICIAL_AREA_SIZE_PROPERTY = "officialAreaSize";
     public static final String CALCULATED_AREA_SIZE_PROPERTY = "calculatedArea";
     
     private byte[] geomPolygon;
     private transient boolean selected;
     
     private SolaList<SpatialValueAreaBean> spatialValueAreaList;
     private SolaList<AddressBean> addressList;
     private transient AddressBean selectedAddress;
     
     public CadastreObjectBean() {
         super();
         addressList = new SolaList<AddressBean>();
         spatialValueAreaList = new SolaList<SpatialValueAreaBean>();
     }
 
     public byte[] getGeomPolygon() {
         return geomPolygon;
     }
 
     public void setGeomPolygon(byte[] geomPolygon) { //NOSONAR
         byte[] old = this.geomPolygon;
         this.geomPolygon = geomPolygon; //NOSONAR
         propertySupport.firePropertyChange(GEOM_POLYGON_PROPERTY, old, this.geomPolygon);
     }
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         boolean oldValue = this.selected;
         this.selected = selected;
         propertySupport.firePropertyChange(SELECTED_PROPERTY, oldValue, this.selected);
     }
     
     /** Looks for officialArea code in the list of areas. */
     @NotNull(message=ClientMessage.CHECK_NOTNULL_AREA, payload=Localized.class)
     public BigDecimal getOfficialAreaSize(){
         return getArea(SpatialValueAreaBean.CODE_OFFICIAL_AREA);
     }
 
     /** Sets officialArea code. */
     public void setOfficialAreaSize(BigDecimal area){
         setArea(area, SpatialValueAreaBean.CODE_OFFICIAL_AREA, OFFICIAL_AREA_SIZE_PROPERTY);
     }
     
     /** Returns calculated area based on feature geometry. </code> */
     public BigDecimal getCalculatedArea(){
         return getArea(SpatialValueAreaBean.CODE_CALCULATED_AREA);
     }
     
     /** Synonym method for <code>setOfficialAreaSize(BigDecimal area)</code> */
     public void setCalculatedArea(BigDecimal area){
         setArea(area, SpatialValueAreaBean.CODE_CALCULATED_AREA, CALCULATED_AREA_SIZE_PROPERTY);
     }
     
     private BigDecimal getArea(String areaCode){
         if(getSpatialValueAreaFiletredList()==null || getSpatialValueAreaFiletredList().size() < 1){
             return null;
         }
         for(SpatialValueAreaBean areaBean : getSpatialValueAreaFiletredList()){
             if(areaBean.getTypeCode()!=null && areaBean.getTypeCode().equals(areaCode)){
                 return areaBean.getSize();
             }
         }
         return null;
     }
     
     private void setArea(BigDecimal area, String areaCode, String evtName){
         for(SpatialValueAreaBean areaBean : getSpatialValueAreaFiletredList()){
             if(areaBean.getTypeCode()!=null && areaBean.getTypeCode().equals(areaCode)){
                 // Delete area if provided value is null
                 if(area == null){
                     areaBean.setEntityAction(EntityAction.DELETE);
                 } else {
                     areaBean.setSize(area);
                 }
                 propertySupport.firePropertyChange(evtName, null, area);
                 return;
             }
         }
         
         // Official area not found, add new if provided area not null
         if(area!=null){
             SpatialValueAreaBean areaBean = new SpatialValueAreaBean();
             areaBean.setSize(area);
             areaBean.setTypeCode(areaCode);
             areaBean.setSpatialUnitId(this.getId());
             getSpatialValueAreaList().addAsNew(areaBean);
         }
         propertySupport.firePropertyChange(evtName, null, area);
     }
     
     @Valid
     public ObservableList<AddressBean> getAddressFilteredList() {
         return addressList.getFilteredList();
     }
     
     public SolaList<AddressBean> getAddressList() {
         return addressList;
     }
 
     public void setAddressList(SolaList<AddressBean> addressList) {
         this.addressList = addressList;
        propertySupport.firePropertyChange(ADDRESS_LIST_PROPERTY, null, this.addressList);
     }
 
     /** Returns merged string of addresses. */
     public String getAddressString(){
         String address = "";
         if(getAddressFilteredList()!=null){
             for (AddressBean addressBean : getAddressFilteredList()){
                 if(addressBean.getDescription()!=null && !addressBean.getDescription().isEmpty()){
                     if(address.isEmpty()){
                         address = addressBean.getDescription();
                     } else {
                         address = address + "; " + addressBean.getDescription();
                     }
                 }
             }
         }
         return address;
     }
     
     public AddressBean getSelectedAddress() {
         return selectedAddress;
     }
 
     public void setSelectedAddress(AddressBean selectedAddress) {
         AddressBean oldValue = this.selectedAddress;
         this.selectedAddress = selectedAddress;
         propertySupport.firePropertyChange(SELECTED_ADDRESS_PROPERTY, oldValue, this.selectedAddress);
     }
 
     public SolaList<SpatialValueAreaBean> getSpatialValueAreaList() {
         return spatialValueAreaList;
     }
 
     @Valid
     public ObservableList<SpatialValueAreaBean> getSpatialValueAreaFiletredList() {
         return spatialValueAreaList.getFilteredList();
     }
     
     public void setSpatialValueAreaList(SolaList<SpatialValueAreaBean> spatialValueAreaList) {
         this.spatialValueAreaList = spatialValueAreaList;
     }
     
     /** Adds new cadastre object address. */
     public void addAddress(AddressBean address){
         if(address!=null){
             getAddressList().addAsNew(address);
         }
     }
     
     /** Removes selected address. */
     public void removeSelectedAddress(){
         if(selectedAddress!=null){
             if(selectedAddress.isNew()){
                 getAddressList().remove(selectedAddress);
             } else {
                 getAddressList().safeRemove(selectedAddress, EntityAction.DELETE);
             }
         }
     }
     
     /** Updates selected address. */
     public void updateSelectedAddress(AddressBean address){
         if(selectedAddress!=null && address!=null){
             selectedAddress.setDescription(address.getDescription());
         }
     }
     
     /** Returns {@link CadastreObjectBean} by ID*/
     public static CadastreObjectBean getCadastreObject(String id){
         if(id==null){
             return null;
         }
         return TypeConverters.TransferObjectToBean(
                 WSManager.getInstance().getCadastreService().getCadastreObject(id),
                 CadastreObjectBean.class, null);
     }
 }
