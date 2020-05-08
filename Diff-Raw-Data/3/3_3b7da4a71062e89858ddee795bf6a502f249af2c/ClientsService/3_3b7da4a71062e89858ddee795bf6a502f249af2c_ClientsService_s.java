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
 package census.business;
 
 import census.business.api.BusinessException;
 import census.business.api.SecurityException;
 import census.business.api.ValidationException;
 import census.business.api.Validator;
 import census.business.dto.ClientDTO;
 import census.persistence.Client;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import javax.persistence.NoResultException;
 import org.joda.time.DateMidnight;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class ClientsService extends BusinessService {
 
     /**
      * Gets a template client for registration. A template client is an instance
      * of Client with some properties set to their default values.
      *
      * @throws IllegalStateException if the session is not active
      * @return a template client
      */
     public ClientDTO getTemplateClient() {
         assertOpenSessionExists();
 
         ClientDTO client = new ClientDTO();
 
         client.setId(getNextId());
         client.setAttendancesBalance(new Integer(0).shortValue());
         client.setExpirationDate(new DateMidnight());
         client.setMoneyBalance(BigDecimal.ZERO.setScale(2));
         client.setRegistrationDate(new DateMidnight());
 
         return client;
     }
 
     /**
      * Registers a new client.
      *
      * <ul>
      *
      * <li> Full name, card and note properties are always required to be
      * non-null.
      *
      * <li> If useSecuredProperties is true, attendancesBalance, registrationDate,
      * expirationDate properties are required to be non-null.
      *
      * <li> If useSecuredProperties is true, the permissions level has to be
      * <code>PL_ALL</code>.
      *
      * </ul>
      *
      * @param client the client's new information
      * @param useSecuredProperties if true, the special fields are used.
      * @return the new client's ID
      * @throws NullPointerException if either client or useSecuredProperties is
      * null, or any of the required properties is null
      * @throws ValidationException if any of the required properties is invalid
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws IllegalStateException if the transaction or the session is not active
      */
     public Short registerClient(ClientDTO client, Boolean useSecuredProperties) throws BusinessException, ValidationException, SecurityException {
         assertTransactionActive();
         assertOpenSessionExists();
         
         if (client == null) {
             throw new NullPointerException("The client is null."); //NOI18N
         }
 
         if (useSecuredProperties == null) {
             throw new NullPointerException("The useSecuredPropeties is null."); //NOI18N
         }
 
         Client newClient = new Client();
 
         /*
          * Validation
          */
         getFullNameValidator().validate(client.getFullName());
         newClient.setFullName(client.getFullName());
 
         getCardValidator().validate(client.getCard());
         newClient.setCard(client.getCard());
 
         getNoteValidator().validate(client.getNote());
         newClient.setNote(client.getNote());
 
         if (!sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)
                 && useSecuredProperties) {
             throw new SecurityException(bundle.getString("AccessToSecuredPropertiesDenied"));
         }
 
         if (useSecuredProperties) {
             if(client.getAttendancesBalance() == null) {
                 throw new NullPointerException("The client.getAttendancesBalance() is null."); //NOI18N
             }
             newClient.setAttendancesBalance(client.getAttendancesBalance());
             if(client.getMoneyBalance() == null) {
                 throw new NullPointerException("The client.getMoneyBalance() is null."); //NOI18N
             }
             newClient.setMoneyBalance(BigDecimal.ZERO);
             if(client.getRegistrationDate() == null) {
                 throw new NullPointerException("The client.getRegistrationDate() is null."); //NOI18N
             }
             newClient.setRegistrationDate(new Date());
             if(client.getExpirationDate() == null) {
                 throw new NullPointerException("The client.getExpirationDate() is null."); //NOI18N
             }
             newClient.setExpirationDate(client.getExpirationDate().toDate());
         } else {
             newClient.setAttendancesBalance(new Integer(0).shortValue());
             newClient.setMoneyBalance(BigDecimal.ZERO);
             newClient.setRegistrationDate(new Date());
             newClient.setExpirationDate(client.getRegistrationDate().toDate());
         }
         
         newClient.setAttendances(null);
         newClient.setFinancialAcitivities(null);
         newClient.setId(getNextId());
 
         // TODO: note changes
         entityManager.persist(newClient);
         entityManager.flush();
         
         return newClient.getId();
     }
 
     /**
      * Gets the client's by its ID.
      *
      * @param clientId the client's ID.
      * @return the client
      * @throws IllegalStateException if the session is not active
      * @throws NullPointerException if the clientId is null
      * @throws ValidationException if the client's ID is invalid
      */
     public ClientDTO getById(Short clientId) throws ValidationException {
         assertOpenSessionExists();
         
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         Client client = entityManager.find(Client.class, clientId);
         
         if(client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
         
         ClientDTO clientDTO = new ClientDTO();
         clientDTO.setId(clientId);
         clientDTO.setFullName(client.getFullName());
         clientDTO.setAttendancesBalance(client.getAttendancesBalance());
         clientDTO.setMoneyBalance(client.getMoneyBalance());
         clientDTO.setRegistrationDate(new DateMidnight(client.getRegistrationDate()));
         clientDTO.setExpirationDate(new DateMidnight(client.getExpirationDate()));
         clientDTO.setNote(client.getNote());
         clientDTO.setCard(client.getCard());
         
         return clientDTO;
     }
 
     /**
      * Finds the client by its card.
      *
      * @param card the client's card
      * @return the client's ID, or null, if the card is not assigned to any Client.
      * @throws IllegalStateException if the session is not active
      * @throws NullPointerException if the card is null
      */
     public Short findByCard(Integer card){
         assertOpenSessionExists();
         
         if (card == null) {
             throw new NullPointerException("The card is null."); //NOI18N
         }
 
         try {
             Client client = (Client) entityManager.createNamedQuery("Client.findByCard") //NOI18N
                     .setParameter("card", card) //NOI18N
                     .setMaxResults(1)
                     .getSingleResult();
             return client.getId();
         } catch (NoResultException ex) {
             return null;
         }
     }
 
     /**
      * Finds all clients whose full names match the requirement. 
      * 
      * <ul>
      * 
      * <il> If exact match is required, a full name matches, if it's 
      * exactly the same as the full name provided. 
      * 
      * <il> If exact match is not required, a full name matches, if it
      * contains the full name provided. 
      * 
      * <il> Both type of matches are case-insensitive.
      * 
      * </ul>
      *
      * @param fullName the full name
      * @param exactMatch whether an exact match is required
      * @return the list of the clients whose full name matches
      * @throws IllegalStateException if the session is not active
      * @throws NullPointerException if either fullName or exactMatch is null
      */
     public List<ClientDTO> findByFullName(String fullName, Boolean exactMatch) throws IllegalArgumentException {
         assertOpenSessionExists();
         
         if (fullName == null) {
             throw new IllegalArgumentException("The fullName is null."); //NOI18N
         }
 
         if (exactMatch == null) {
             throw new IllegalArgumentException("The exactMatch is null."); //NOI18N
         }
 
         List<Client> clients;
 
         if (exactMatch == true) {
             clients = entityManager.createNamedQuery("Client.findByFullNameExact") //NOI18N
                     .setParameter("fullName", fullName) //NOI18N
                     .getResultList(); 
         } else {
             clients = entityManager.createNamedQuery("Client.findByFullNameNotExact") //NOI18N
                     .setParameter("fullName", fullName) //NOI18N
                     .getResultList(); //NOI18N
         }
         
         List<ClientDTO> result = new LinkedList<ClientDTO>();
 
         for (Client client : clients) {
             ClientDTO clientDTO = new ClientDTO();
             clientDTO.setId(client.getId());
             clientDTO.setFullName(client.getFullName());
             clientDTO.setAttendancesBalance(client.getAttendancesBalance());
             clientDTO.setMoneyBalance(client.getMoneyBalance());
             clientDTO.setRegistrationDate(new DateMidnight(client.getRegistrationDate()));
             clientDTO.setExpirationDate(new DateMidnight(client.getExpirationDate()));
             clientDTO.setNote(client.getNote());
             clientDTO.setCard(client.getCard());
             result.add(clientDTO);
         }
 
         return result;
     }
 
     /**
      * Updates the information about the client.
      *
      * <ul>
      *
      * <il> Full name, card and note properties are always required to be
      * non-null.
      *
      * <li> If useSecuredProperties is true, attendancesBalance, registrationDate,
      * expirationDate properties are required to be non-null.
      *
      * <li>If useSecuredProperties is true, the permissions level has to be
      * <code>PL_ALL</code>.
      *
      * </ul>
      *
      * @param client the Client's ID.
      * @param useSecuredProperties if true, the special fields are used.
      * @throws NullPointerException if either of the arguments or required
      * properties is null
      * @throws SecurityException if current security rules restrict this operation
      * @throws ValidationException if any of the required properties is invalid
      * @throws IllegalStateException if the transaction or the session is not active
      */
     public void updateClient(ClientDTO client, Boolean useSecuredProperties) throws SecurityException, ValidationException {
         assertTransactionActive();
         assertOpenSessionExists();
 
         if (client == null) {
             throw new NullPointerException("The client is null."); //NOI18N
         }
 
         if (client.getId() == null) {
             throw new NullPointerException("The client's ID is null."); //NOI18N
         }
         
         if(useSecuredProperties == null) {
             throw new NullPointerException("The useSecuredProperties is null."); //NOI18N
         }
 
         Client originalClient = entityManager.find(Client.class, client.getId());
 
         if (originalClient == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
         
         if(useSecuredProperties && !sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("AccessToSecuredPropetiesDenied"));
         }
 
         if (!originalClient.getFullName().equals(client.getFullName())) {
             getFullNameValidator().validate(client.getFullName());
             originalClient.setFullName(client.getFullName());
         }
 
         /*
          * Validates the card, only if it was changed. Otherwise, the validator
          * will throw ValidationException for the card is already assigned to
          * this client.
          */
        if (!originalClient.getCard().equals(client.getCard())) {
             getCardValidator().validate(client.getCard());
             originalClient.setCard(client.getCard());
         }
 
         /*
          * It's faster just to revalidate the note and merge it.
          */
         getNoteValidator().validate(client.getNote());
         originalClient.setNote(client.getNote());
         
         if(client.getAttendancesBalance() == null) {
             throw new NullPointerException("The client.getAttendancesBalance() is null"); //NOI18N
         }
         originalClient.setAttendancesBalance(client.getAttendancesBalance());
         
         if(client.getMoneyBalance() == null) {
             throw new NullPointerException("The client.getMoneyBalance() is null."); //NOI18N
         }
         
         /*
          * Normalizes the scale, and throws an exception, if the scale is 
          * to big.
          */
         if (client.getMoneyBalance().scale() > 2) {
             throw new ValidationException(bundle.getString("Message.MoneyBalanceCanHasTwoDigitsAfterDecimalPointMax"));
         }
         client.setMoneyBalance(client.getMoneyBalance().setScale(2));
         
         if (client.getMoneyBalance().precision() > 5) {
             throw new ValidationException(bundle.getString("LimitReached"));
         }
         originalClient.setMoneyBalance(client.getMoneyBalance());
         
         if(originalClient.getRegistrationDate() == null) {
             throw new NullPointerException("The client.getRegistrationDate() is null."); //NOI18N
         }
         originalClient.setRegistrationDate(client.getRegistrationDate().toDate());
         
         if(originalClient.getExpirationDate() == null) {
             throw new NullPointerException("The client.getExpirationDate() is null."); //NOI18N
         }
         originalClient.setExpirationDate(client.getExpirationDate().toDate());
         
         entityManager.flush();
     }
 
     /**
      * Returns whether the client has a debt.
      * 
      * @param clientId the client's ID
      * @return true, if the client's money balance is not negative.
      * @throws NullPointerException if the clientId is null
      * @throws ValidationException if the client's ID is invalid 
      * @throws IllegalStateException if the session is not active
      */
     public Boolean hasDebt(Short clientId) throws ValidationException {
         assertOpenSessionExists();
         
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         Client client = entityManager.find(Client.class, clientId);
         if (client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
 
         return client.getMoneyBalance().compareTo(BigDecimal.ZERO) < 0;
     }
 
     /**
      * Gets the ID of the next client to be registered.
      * 
      * @throws IllegalStateException if the session is not active
      * @return the next client's ID
      */
     public Short getNextId() {
         assertOpenSessionExists();
         
         try {
             return new Integer(1 + (Short) entityManager
                     .createNamedQuery("Client.findAllIdsOrderByIdDesc") //NOI18N
                     .setMaxResults(1)
                     .getSingleResult())
                     .shortValue();
         } catch (NoResultException ex) {
             return 1;
         }
     }
 
     private Validator getFullNameValidator() {
         return new Validator<String>() {
 
             @Override
             public void validate(String value) throws ValidationException {
                 if (value == null) {
                     throw new NullPointerException("The full name is null."); //NOI18N
                 }
                 if (value.trim().isEmpty()) {
                     throw new ValidationException(bundle.getString("ClientFullNameEmpty"));
                 }
             }
         };
     }
 
     private Validator getCardValidator() {
         return new Validator<Integer>() {
 
             @Override
             public void validate(Integer value) throws ValidationException {
                 if (value != null && (value > 99999999 || value < 10000000)) {
                     throw new ValidationException(bundle.getString("ClientCardInvalid")); //NOI18N
                 }
 
                 // If a card is to be assigned,
                 // let's make sure no one else uses it.
                 if (value != null) {
                     List<Client> clients = entityManager
                             .createNamedQuery("Client.findByCard") //NOI18N
                             .setParameter("card", value) //NOI18N
                             .getResultList(); 
 
                     if (!clients.isEmpty()) {
                         throw new ValidationException(java.text.MessageFormat.format(bundle.getString("ClientCardAlreadyInUse"), new Object[] {clients.get(0).getFullName(), clients.get(0).getId()}));
                     }
                 }
             }
         };
     }
 
     private Validator getNoteValidator() {
         return new Validator<String>() {
 
             @Override
             public void validate(String value) throws ValidationException, IllegalArgumentException {
                 if (value == null) {
                     throw new NullPointerException("The note is null."); //NOI18N
                 }
             }
         };
     }
      
     /**
      * Singleton instance.
      */
     private static ClientsService instance;
 
 
     /**
      * Gets an instance of this class.
      * 
      * @return an instance of this class 
      */
     public static ClientsService getInstance() {
         if (instance == null) {
             instance = new ClientsService();
         }
         return instance;
     }
 }
