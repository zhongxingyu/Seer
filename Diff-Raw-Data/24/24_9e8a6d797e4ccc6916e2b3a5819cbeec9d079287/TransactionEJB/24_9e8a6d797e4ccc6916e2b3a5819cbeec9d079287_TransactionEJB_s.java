 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.transaction.businesslogic;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import org.sola.common.DateUtility;
 import org.sola.common.SOLAException;
 import org.sola.common.messaging.ServiceMessage;
 import org.sola.services.common.EntityAction;
 import org.sola.services.common.br.ValidationResult;
 import org.sola.services.common.ejbs.AbstractEJB;
 import org.sola.services.common.faults.SOLAValidationException;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.ejb.cadastre.businesslogic.CadastreEJBLocal;
 import org.sola.services.ejb.cadastre.repository.entities.CadastreObjectStatusChanger;
 import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;
 import org.sola.services.ejb.system.repository.entities.BrValidation;
 import org.sola.services.ejb.transaction.repository.entities.*;
 
 /**
  * EJB to manage data in the transaction schema. Also supports the performing actions against
  * transaction data.
  */
 @Stateless
 @EJB(name = "java:global/SOLA/TransactionEJBLocal", beanInterface = TransactionEJBLocal.class)
 public class TransactionEJB extends AbstractEJB implements TransactionEJBLocal {
 
     @EJB
     SystemEJBLocal systemEJB;
     @EJB
     CadastreEJBLocal cadastreEJB;
 
     /**
      * Sets the entity package for the EJB to TransactionBasic.class.getPackage().getName(). This is
      * used to restrict the save and retrieval of Code Entities.
      *
      * @see AbstractEJB#getCodeEntity(java.lang.Class, java.lang.String, java.lang.String)
      * AbstractEJB.getCodeEntity
      * @see AbstractEJB#getCodeEntityList(java.lang.Class, java.lang.String)
      * AbstractEJB.getCodeEntityList
      * @see
      * AbstractEJB#saveCodeEntity(org.sola.services.common.repository.entities.AbstractCodeEntity)
      * AbstractEJB.saveCodeEntity
      */
     @Override
     protected void postConstruct() {
         setEntityPackage(TransactionBasic.class.getPackage().getName());
     }
 
     /**
      * Can be used to retrieve or create a transaction of the specified type for a service.
      *
      * @param <T> Generic type of the transaction class. Must extend {@linkplain TransactionBasic}.
      * @param serviceId The identifier of the service
      * @param createIfNotFound Flag it indicate whether the transaction should be created if it does
      * not already exist
      * @param transactionClass The class indicating the specific transaction required.
      */
     @Override
     public <T extends TransactionBasic> T getTransactionByServiceId(
             String serviceId,
             boolean createIfNotFound,
             Class<T> transactionClass) {
         T transaction = null;
         if (serviceId != null) {
             Map<String, Object> params = new HashMap<String, Object>();
             params.put(
                     CommonSqlProvider.PARAM_WHERE_PART,
                     TransactionBasic.QUERY_WHERE_BYFROMSERVICEID);
             params.put("serviceId", serviceId);
             transaction = getRepository().getEntity(transactionClass, params);
         }
         if (createIfNotFound && transaction == null) {
             transaction = this.createTransaction(serviceId, transactionClass);
         }
         return transaction;
     }
 
     /**
      * Uses generics to create transaction of the type indicated by the transaction class.
      *
      * @param <T> Generic type of the transaction class. Must extend {@linkplain TransactionBasic}.
      * @param serviceId The identifier of the service the transaction must be associated with.
      * @param transactionClass The class indicating the specific transaction required.
      * @throws SOLAException If the transaction cannot be created.
      */
     @Override
     public <T extends TransactionBasic> T createTransaction(
             String serviceId, Class<T> transactionClass) {
         T transaction = null;
         try {
             transaction = transactionClass.newInstance();
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     new Object[]{"Transaction cannot be created.", ex});
         }
         transaction.getId();
         transaction.setFromServiceId(serviceId);
         return getRepository().saveEntity(transaction);
     }
 
     /**
      * Retrieves a transaction by id.
      *
      * @param <T> Generic type of the transaction class. Must extend {@linkplain TransactionBasic}.
      * @param id The transaction identifier.
      * @param transactionClass The class indicating the specific transaction required.
      */
     @Override
     public <T extends TransactionBasic> T getTransactionById(String id, Class<T> transactionClass) {
         return getRepository().getEntity(transactionClass, id);
     }
 
     /**
      * Sets the status of the transaction to the value specified.
      *
      * @param serviceId The identifier of the service the transaction is associated with.
      * @param statusCode The status to assign the transaction
      * @return
      * <code>true</code> if the status of the transaction is successfully changed,
      * <code>false</code> otherwise.
      * @see #getTransactionByServiceId(java.lang.String, boolean, java.lang.Class)
      * getTransactionByServiceId
      */
     @Override
     public boolean changeTransactionStatusFromService(
             String serviceId, String statusCode) {
         TransactionStatusChanger transaction =
                 this.getTransactionByServiceId(serviceId, false, TransactionStatusChanger.class);
         if (transaction == null) {
             return false;
         }
 
         transaction.setStatusCode(statusCode);
         getRepository().saveEntity(transaction);
         return true;
     }
 
     /**
      * Validates the transaction and updates the status of the transaction to approved if
      * applicable.
      *
      * @param requestType The type of service associated with the transaction
      * @param serviceId The identifier of the service associated with the transaction
      * @param languageCode The language code to use to localize any validation messages
      * @param validationOnly Flag to indicate if only validations should be executed
      * @return The validation messages returned from the business rules.
      * @see #changeStatusOfTransactionObjectsOnApproval(java.lang.String, java.lang.String)
      * changeStatusOfTransactionObjectsOnApproval
      */
     @Override
     public List<ValidationResult> approveTransaction(
             String requestType, String serviceId, String languageCode, boolean validationOnly) {
 
         TransactionStatusChanger transaction = this.getTransactionByServiceId(
                 serviceId, false, TransactionStatusChanger.class);
         List<ValidationResult> validationResultList = new ArrayList<ValidationResult>();
         if (transaction == null) {
             return validationResultList;
         }
 
         validationResultList.addAll(this.validateTransaction(
                 transaction.getId(), requestType,
                 languageCode, RegistrationStatusType.STATUS_CURRENT));
 
         validationOnly = validationOnly || !systemEJB.validationSucceeded(validationResultList);
         if (!validationOnly) {
             this.changeStatusOfTransactionObjectsOnApproval(requestType, transaction.getId());
             transaction.setStatusCode(TransactionStatusType.APPROVED);
             transaction.setApprovalDatetime(DateUtility.now());
             getRepository().saveEntity(transaction);
         }
         return validationResultList;
     }
 
     /**
     * Updates the status of any cadastre objects associated with the transaction.
      *
      * @param requestType The type of service associated to the transaction
      * @param transactionId The transaction identifier
      * @see
      * org.sola.services.ejb.cadastre.businesslogic.CadastreEJB#ChangeStatusOfCadastreObjects(java.lang.String,
      * java.lang.String, java.lang.String) CadastreEJB.ChangeStatusOfCadastreObjects
      * @see
      * org.sola.services.ejb.cadastre.businesslogic.CadastreEJB#approveCadastreRedefinition(java.lang.String)
      * CadastreEJB.approveCadastreRedefinition
      */
     private void changeStatusOfTransactionObjectsOnApproval(
             String requestType, String transactionId) {
         if (requestType.equals(TransactionType.CADASTRE_CHANGE)
                 || requestType.equals(TransactionType.NEW_APARTMENT)
                 || requestType.equals(TransactionType.NEW_DIGITAL_PROPERTY)
                 || requestType.equals(TransactionType.NEW_DIGITAL_TITLE)
                 || requestType.equals(TransactionType.NEW_FREEHOLD)
                 || requestType.equals(TransactionType.NEW_STATE)) {
             cadastreEJB.ChangeStatusOfCadastreObjects(
                     transactionId,
                     CadastreObjectStatusChanger.QUERY_WHERE_SEARCHBYTRANSACTION_PENDING,
                     RegistrationStatusType.STATUS_CURRENT);
         }
        if (requestType.equals(TransactionType.CADASTRE_CHANGE)) {
            cadastreEJB.ChangeStatusOfCadastreObjects(
                    transactionId,
                    CadastreObjectStatusChanger.QUERY_WHERE_SEARCHBYTRANSACTION_TARGET,
                    RegistrationStatusType.STATUS_HISTORIC);
        }
         if (requestType.equals(TransactionType.REDEFINE_CADASTRE)) {
             cadastreEJB.approveCadastreRedefinition(transactionId);
         }
     }
 
     /**
      * Deletes the transaction associated with the specified service id.
      *
      * @param serviceId Identifier of the service
      * @return
      * <code>true</code> if the transaction is deleted.
      */
     @Override
     public boolean rejectTransaction(String serviceId) {
         TransactionBasic transaction = this.getTransactionByServiceId(serviceId, false, TransactionBasic.class);
         if (transaction == null) {
             return false;
         }
 
         transaction.setEntityAction(EntityAction.DELETE);
         getRepository().saveEntity(transaction);
         return true;
     }
 
     /**
      * Retrieves all transaction.reg_status_type code values.
      *
      * @param languageCode The language code to use for localization of display values.
      */
     @Override
     public List<RegistrationStatusType> getRegistrationStatusTypes(String languageCode) {
         return getRepository().getCodeList(RegistrationStatusType.class, languageCode);
     }
 
     /**
      * Saves the transaction. Also validates the transaction.
      *
      * @param <T> Generic type of the transaction class. Must extend {@linkplain TransactionBasic}.
      * @param transaction The transaction to save
      * @param requestType The type of the service associated with the transaction.
      * @param languageCode The language code to use for localization of display values.
      * @return The list of validation messages.
      * @throws SOLAValidationException If the validations fail
      * @see #validateTransaction(java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String) validateTransaction
      * @see org.sola.services.ejb.system.businesslogic.SystemEJB#validationSucceeded(java.util.List)
      * SystemEJB.validationSucceeded
      */
     @Override
     public <T extends TransactionBasic> List<ValidationResult> saveTransaction(
             T transaction, String requestType, String languageCode) {
 
         //Saves the transaction
         transaction = this.saveEntity(transaction);
 
         //It runs the validation
         List<ValidationResult> validationResultList = validateTransaction(
                 transaction.getId(), requestType,
                 languageCode, RegistrationStatusType.STATUS_PENDING);
 
         if (!systemEJB.validationSucceeded(validationResultList)) {
             //If the validation fails the whole transaction is rolledback.
             throw new SOLAValidationException(validationResultList);
         }
         return validationResultList;
     }
 
     /**
      * Validates the transaction if it is a cadastre change or redefine cadastre service.
      *
      * @param transactionId Identifier of the transaction to validate.
      * @param requestType The type of service associated with the transaction.
      * @param languageCode The language code to use for localizing the validation messages.
      * @param momentCode Indicates the subset of validation rules to apply for the transaction.
      * @return The list of validation messages
      * @see
      * org.sola.services.ejb.system.businesslogic.SystemEJB#getBrForValidatingTransaction(java.lang.String,
      * java.lang.String, java.lang.String) SystemEJB.getBrForValidatingTransaction
      * @see
      * org.sola.services.ejb.system.businesslogic.SystemEJB#checkRulesGetValidation(java.util.List,
      * java.lang.String, java.util.HashMap) SystemEJB.checkRulesGetValidation
      */
     private List<ValidationResult> validateTransaction(
             String transactionId, String requestType, String languageCode, String momentCode) {
 
         List<BrValidation> brValidationList = null;
         if (requestType.equals(TransactionType.CADASTRE_CHANGE)
                 || requestType.equals(TransactionType.REDEFINE_CADASTRE)) {
             brValidationList = this.systemEJB.getBrForValidatingTransaction(
                     "cadastre_object", momentCode, requestType);
         }
 
         HashMap<String, Serializable> params = new HashMap<String, Serializable>();
 
         //The business rules fired, are supposed to get only one parameter and that is
         // the id of the transaction
         params.put("id", transactionId);
 
         //Run the validation
         List<ValidationResult> validationResultList =
                 this.systemEJB.checkRulesGetValidation(brValidationList, languageCode, params);
 
         //If there has to be extra validation depending in the kind of transaction
         // has to happen here and added to the validationResultList.
 
         return validationResultList;
     }
 }
