 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.business;
 
 import java.text.MessageFormat;
 import org.key2gym.business.api.BusinessException;
 import org.key2gym.business.api.SecurityException;
 import org.key2gym.business.api.ValidationException;
 import org.key2gym.business.dto.FreezeDTO;
 import org.key2gym.persistence.Administrator;
 import org.key2gym.persistence.Client;
 import org.key2gym.persistence.ClientFreeze;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import org.joda.time.DateMidnight;
 import org.omg.CORBA.INTERNAL;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class FreezesService extends BusinessService {
 
     protected FreezesService() {
     }
 
     /**
      * Records a freeze for the client.
      *
      * <ul>
      *
      * <li> The current permissions level has to be at least
      * <code>PL_EXTENDED</code>. </li> <li> The client can not be expired. </li>
      * <li> The number of days can not exceed 10. </li> <li> There can be at
      * most 1 freeze per month. </li>
      *
      * </ul>
      *
      * @param clientId the client's ID
      * @throws IllegalStateException if the transaction or the session is not
      * active
      * @throws NullPointerException if any of the arguments is null
      * @throws ValidationException if the client's ID is invalid
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws SecurityException if current security rules restrict this
      * operation
      */
     public void addFreeze(Short clientId, Short days, String note) throws ValidationException, BusinessException, org.key2gym.business.api.SecurityException {
         assertOpenSessionExists();
         assertTransactionActive();
 
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         if (days == null) {
             throw new NullPointerException("The days is null."); //NOI18N
         }
 
         if (note == null) {
             throw new NullPointerException("The note is null."); //NOI18N
         }
 
         if (sessionService.getPermissionsLevel() > SessionsService.PL_EXTENDED) {
             throw new SecurityException(bundle.getString("Security.Operation.Denied"));
         }
 
         Client client = entityManager.find(Client.class, clientId);
 
         if (client == null) {
             throw new ValidationException(bundle.getString("Invalid.Client.ID"));
         }
 
         if (client.getExpirationDate().compareTo(new Date()) < 0) {
             throw new BusinessException(bundle.getString("BusinessRule.Client.SubscriptionExpired"));
         }
 
         if (days < 1 || days > 10) {
             String message = MessageFormat.format(
                     bundle.getString("BusinessRule.Freeze.Days.HasToBeWithinRange.withRangeBeginAndRangeEnd"),
                    1, 10
             );
             throw new ValidationException(message);
         }
 
         if (note.trim().isEmpty()) {
             throw new ValidationException(bundle.getString("Invalid.Freeze.Note.CanNotBeEmpty"));
         }
 
         DateMidnight today = new DateMidnight();
         List<ClientFreeze> freezes = entityManager
                 .createNamedQuery("ClientFreeze.findByClientAndDateIssuedRange") //NOI18N
                 .setParameter("client", client)
                 .setParameter("rangeBegin", today.minusMonths(1).toDate()) //NOI18N
                 .setParameter("rangeEnd", today.toDate()) //NOI18N
                 .getResultList();
 
         if (!freezes.isEmpty()) {
             throw new BusinessException(bundle.getString("BusinessRule.Freeze.ClientHasAlreadyBeenFrozenLastMonth"));
         }
 
         // Rolls the expiration date
         client.setExpirationDate(new DateMidnight(client.getExpirationDate()).plusDays(days).toDate());
 
         ClientFreeze clientFreeze = new ClientFreeze();
         clientFreeze.setDateIssued(new Date());
         clientFreeze.setDays(days);
         clientFreeze.setClient(client);
         clientFreeze.setAdministrator(entityManager.find(Administrator.class, sessionService.getTopmostAdministratorId()));
         clientFreeze.setNote(note);
 
         entityManager.persist(clientFreeze);
         entityManager.flush();
     }
 
     /**
      * Finds all freezes for the client.
      *
      * @param clientId the client's ID
      * @throws IllegalStateException if the session is not active
      * @throws NullPointerException if the client's ID is null
      * @throws ValidationException if the client's ID is invalid
      * @return the list of all freezes for the client
      */
     public List<FreezeDTO> findFreezesForClient(Short clientId) throws ValidationException {
         assertOpenSessionExists();
 
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         Client client = entityManager.find(Client.class, clientId);
 
         if (client == null) {
             throw new ValidationException(bundle.getString("Invalid.Client.ID"));
         }
 
         List<ClientFreeze> freezes = entityManager.createNamedQuery("ClientFreeze.findByClient").setParameter("client", client).getResultList(); //NOI18N
         List<FreezeDTO> result = new LinkedList<>();
 
         for (ClientFreeze freeze : freezes) {
             result.add(wrapFreeze(freeze));
         }
 
         return result;
     }
 
     /**
      * Finds freezes having date issued within the range.
      * 
      * <ul>
      * 
      * <li> The permissions level has to be PL_ALL </li>
      * <li> The beginning date has be before or equal to ending date. </li>
      * 
      * </ul>
      * 
      * @param begin the beginning date
      * @param end the ending date
      * @return the list of freezes
      * @throws IllegalStateException if no session is open
      * @throws NullPointerException if any of the arguments is null
      * @throws SecurityException if current security rules restrict this operation
      * @throws BusinessException if any of the arguments is invalid
      */
     public List<FreezeDTO> findByDateIssuedRange(DateMidnight begin, DateMidnight end) throws SecurityException, ValidationException {
         assertOpenSessionExists();
         
         if(!sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("Security.Access.Denied"));
         }
         
         if(begin == null) {
             throw new NullPointerException("The begin is null."); //NOI18N
         }
         
         if(end == null) {
             throw new NullPointerException("The end is null."); //NOI18N
         }
         
         if(begin.isAfter(end)) {
             throw new ValidationException(bundle.getString("Invalid.DateRange.BeginningAfterEnding"));
         }
         
         List<ClientFreeze> freezes = entityManager
                 .createNamedQuery("ClientFreeze.findByDateIssuedRange") //NOI18N
                 .setParameter("rangeBegin", begin.toDate()) //NOI18N
                 .setParameter("rangeEnd", end.toDate()) //NOI18N
                 .getResultList();
         List<FreezeDTO> result = new LinkedList<>();
         
         for(ClientFreeze freeze : freezes) {
             result.add(wrapFreeze(freeze));
         }
         
         return result;
     }
     
     /**
      * Finds all freezes.
      * 
      * <ul>
      * 
      * <li> The permissions level has to be PL_ALL. </li>
      * 
      * </ul>
      * 
      * @throws IllegalStateException if no session is open
      * @throws SecurityException if current security rules restrict this operation
      * @return the list of all freezes 
      */
     public List<FreezeDTO> findAll() {
         assertOpenSessionExists();
         
         List<ClientFreeze> freezes = entityManager
                 .createNamedQuery("ClientFreeze.findAll") //NOI18N
                 .getResultList();
         List<FreezeDTO> result = new LinkedList<>();
         
         for(ClientFreeze freeze : freezes) {
             result.add(wrapFreeze(freeze));
         }
         
         return result;
     }
     
     /**
      * Removes the freeze by its ID.
      * 
      * <ul>
      * 
      * <li> The permissions level has to be PL_ALL </li>
      * <li> The freeze has to be active, which is the expiration date can
      * not be in the past. </li>
      * 
      * </ul>
      * 
      * @param id the freeze's ID
      * @throws IllegalStateException if no session is open
      * @throws SecurityException if current security rules restrict this operation
      * @throws NullPointerException if the freeze's ID is null
      * @throws ValidationException if the freeze's ID is invalid
      * @throws BusinessException if current business rules restrict this operation
      */
     public void remove(Short id) throws SecurityException, ValidationException, BusinessException {
         assertOpenSessionExists();
         
         if(!sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("Security.Operation.Denied"));
         }
         
         assertTransactionActive();
         
         if(id == null) {
             throw new NullPointerException("The id is null."); //NOI18N
         }
         
         ClientFreeze clientFreeze = entityManager.find(ClientFreeze.class, id);
         
         if(clientFreeze == null) {
             throw new ValidationException(bundle.getString("Invalid.Freeze.ID"));
         }
         
   
         if(new DateMidnight(clientFreeze.getDateIssued()).plusDays(clientFreeze.getDays()).isBeforeNow()) {
             throw new BusinessException(bundle.getString("BusinessRule.Freeze.AlreadyExpired"));
         }
         
         Client client = clientFreeze.getClient();
         
         client.setExpirationDate(new DateMidnight(client.getExpirationDate()).minusDays(clientFreeze.getDays()).toDate());
         
         // TODO: note change
         entityManager.remove(clientFreeze);
         entityManager.flush();
     }
     
 
     private FreezeDTO wrapFreeze(ClientFreeze freeze) {
         FreezeDTO freezeDTO = new FreezeDTO();
 
         freezeDTO.setId(freeze.getId());
         freezeDTO.setAdministratorFullName(freeze.getAdministrator().getFullName());
         freezeDTO.setAdministratorId(freeze.getAdministrator().getId());
         freezeDTO.setClientFullName(freeze.getClient().getFullName());
         freezeDTO.setClientId(freeze.getClient().getId());
         freezeDTO.setDateIssued(new DateMidnight(freeze.getDateIssued()));
         freezeDTO.setDays(freeze.getDays());
         freezeDTO.setNote(freeze.getNote());
         
         return freezeDTO;
     }
     
     /**
      * Singleton instance.
      */
     private static FreezesService instance;
 
     /**
      * Gets an instance of this class.
      * 
      * @return an instance of this class 
      */
     public static FreezesService getInstance() {
         if (instance == null) {
             instance = new FreezesService();
         }
         return instance;
     }
 }
