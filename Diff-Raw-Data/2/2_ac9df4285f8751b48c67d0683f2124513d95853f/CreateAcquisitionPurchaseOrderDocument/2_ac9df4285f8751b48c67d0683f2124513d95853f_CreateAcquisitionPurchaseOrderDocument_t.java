 /*
  * @(#)CreateAcquisitionPurchaseOrderDocument.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import module.finance.domain.SupplierContact;
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import net.sf.jasperreports.engine.JRException;
 import pt.ist.bennu.core._development.PropertiesManager;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.bennu.core.domain.util.Address;
 import pt.ist.bennu.core.util.BundleUtil;
 import pt.ist.bennu.core.util.ReportUtils;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 
 /**
  * 
  * @author Pedro Santos
  * @author Paulo Abrantes
  * @author Luis Cruz
  * 
  */
 public class CreateAcquisitionPurchaseOrderDocument extends
         WorkflowActivity<RegularAcquisitionProcess, CreateAcquisitionPurchaseOrderDocumentInformation> {
 
     private static final String EXTENSION_PDF = "pdf";
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
         return isUserProcessOwner(process, user) && process.getAcquisitionProcessState().isAuthorized()
                 && ExpenditureTrackingSystem.isAcquisitionCentralGroupMember(user) && process.getRequest().hasSelectedSupplier()
                 && process.isCommitted() && process.isReverifiedAfterCommitment();
     }
 
     @Override
     protected void process(CreateAcquisitionPurchaseOrderDocumentInformation activityInformation) {
         RegularAcquisitionProcess process = activityInformation.getProcess();
         String requestID = process.getAcquisitionRequestDocumentID();
         byte[] file =
                 createPurchaseOrderDocument(process.getAcquisitionRequest(), requestID, activityInformation.getSupplierContact());
         new PurchaseOrderDocument(process, file, requestID + "." + EXTENSION_PDF, requestID);
     }
 
     @Override
     public ActivityInformation<RegularAcquisitionProcess> getActivityInformation(RegularAcquisitionProcess process) {
         return new CreateAcquisitionPurchaseOrderDocumentInformation(process, this);
     }
 
     @Override
     public String getLocalizedName() {
         return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
         return "resources/AcquisitionResources";
     }
 
     @Override
     public boolean isConfirmationNeeded(RegularAcquisitionProcess process) {
         return !process.getFiles(PurchaseOrderDocument.class).isEmpty();
     }
 
     protected byte[] createPurchaseOrderDocument(final AcquisitionRequest acquisitionRequest, final String requestID,
             final SupplierContact supplierContact) {
         final Map<String, Object> paramMap = new HashMap<String, Object>();
         paramMap.put("acquisitionRequest", acquisitionRequest);
         paramMap.put("supplierContact", supplierContact);
         paramMap.put("requestID", requestID);
         paramMap.put("responsibleName", getLoggedPerson().getExpenditurePerson().getName());
         DeliveryLocalList deliveryLocalList = new DeliveryLocalList();
         List<AcquisitionRequestItemBean> acquisitionRequestItemBeans = new ArrayList<AcquisitionRequestItemBean>();
         createBeansLists(acquisitionRequest, deliveryLocalList, acquisitionRequestItemBeans);
         paramMap.put("deliveryLocals", deliveryLocalList);
         paramMap.put("institutionSocialSecurityNumber",
                 PropertiesManager.getProperty(VirtualHost.getVirtualHostForThread().getHostname() + ".ssn"));
         paramMap.put("cae", PropertiesManager.getProperty(VirtualHost.getVirtualHostForThread().getHostname() + ".cae"));
         paramMap.put("logoFilename", "Logo_" + VirtualHost.getVirtualHostForThread().getHostname() + ".png");
         paramMap.put("commitmentNumbers", acquisitionRequest.getCommitmentNumbers());
 
         final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources/AcquisitionResources");
         try {
             final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
             final String documentName =
                    virtualHost.getExpenditureTrackingSystem().getRequireCommitmentNumber() ? "/reports/acquisitionRequestDocument.jasper" : "/reports/acquisitionRequestPurchaseOrder.jasper";
             byte[] byteArray =
                     ReportUtils.exportToPdfFileAsByteArray(documentName, paramMap, resourceBundle, acquisitionRequestItemBeans);
             return byteArray;
         } catch (JRException e) {
             e.printStackTrace();
             throw new DomainException("acquisitionRequestDocument.message.exception.failedCreation");
         }
 
     }
 
     private void createBeansLists(AcquisitionRequest acquisitionRequest, DeliveryLocalList deliveryLocalList,
             List<AcquisitionRequestItemBean> acquisitionRequestItemBeans) {
         for (AcquisitionRequestItem acquisitionRequestItem : acquisitionRequest.getOrderedRequestItemsSet()) {
             DeliveryLocal deliveryLocal =
                     deliveryLocalList.getDeliveryLocal(acquisitionRequestItem.getRecipient(),
                             acquisitionRequestItem.getRecipientPhone(), acquisitionRequestItem.getRecipientEmail(),
                             acquisitionRequestItem.getAddress());
             acquisitionRequestItemBeans.add(new AcquisitionRequestItemBean(deliveryLocal.getIdentification(),
                     acquisitionRequestItem));
         }
     }
 
     public static class AcquisitionRequestItemBean {
         private String identification;
         private AcquisitionRequestItem acquisitionRequestItem;
 
         public AcquisitionRequestItemBean(String identification, AcquisitionRequestItem acquisitionRequestItem) {
             setIdentification(identification);
             setAcquisitionRequestItem(acquisitionRequestItem);
         }
 
         public String getIdentification() {
             return identification;
         }
 
         public void setIdentification(String identification) {
             this.identification = identification;
         }
 
         public void setAcquisitionRequestItem(AcquisitionRequestItem acquisitionRequestItem) {
             this.acquisitionRequestItem = acquisitionRequestItem;
         }
 
         public AcquisitionRequestItem getAcquisitionRequestItem() {
             return acquisitionRequestItem;
         }
 
     }
 
     public static class DeliveryLocalList extends ArrayList<DeliveryLocal> {
         private char id = 'A';
 
         public DeliveryLocal getDeliveryLocal(String personName, String phone, String email, Address address) {
             for (DeliveryLocal deliveryLocal : this) {
                 if (deliveryLocal.getPersonName().equals(personName) && deliveryLocal.getPhone().equals(phone)
                         && deliveryLocal.getEmail().equals(email) && deliveryLocal.getAddress().equals(address)) {
                     return deliveryLocal;
                 }
             }
 
             DeliveryLocal newDelDeliveryLocal = new DeliveryLocal("" + id, personName, phone, email, address);
             id++;
             add(newDelDeliveryLocal);
             return newDelDeliveryLocal;
         }
     }
 
     public static class DeliveryLocal {
         private String identification;
         private String personName;
         private String phone;
         private String email;
         private Address address;
 
         public DeliveryLocal(String identification, String personName, String phone, String email, Address address) {
             setIdentification(identification);
             setPersonName(personName);
             setPhone(phone);
             setEmail(email);
             setAddress(address);
         }
 
         public String getIdentification() {
             return identification;
         }
 
         public void setIdentification(String identification) {
             this.identification = identification;
         }
 
         public String getPersonName() {
             return personName;
         }
 
         public void setPersonName(String personName) {
             this.personName = personName;
         }
 
         public String getPhone() {
             return phone;
         }
 
         public void setPhone(String phone) {
             this.phone = phone;
         }
 
         public String getEmail() {
             return email;
         }
 
         public void setEmail(String email) {
             this.email = email;
         }
 
         public Address getAddress() {
             return address;
         }
 
         public void setAddress(Address address) {
             this.address = address;
         }
 
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
         return false;
     }
 
 }
