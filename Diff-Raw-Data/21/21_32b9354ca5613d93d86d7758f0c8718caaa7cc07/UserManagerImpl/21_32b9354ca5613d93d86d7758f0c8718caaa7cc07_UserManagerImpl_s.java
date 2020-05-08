 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.radar.service.impl;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.patientview.model.Patient;
 import org.patientview.radar.dao.JoinRequestDao;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.exception.JoinCreationException;
 import org.patientview.radar.exception.PatientLinkException;
 import org.patientview.radar.exception.RegisterException;
 import org.patientview.radar.exception.UserCreationException;
 import org.patientview.radar.exception.UserMappingException;
 import org.patientview.radar.exception.UserRoleException;
 import org.patientview.radar.model.JoinRequest;
 import org.patientview.radar.model.exception.DaoException;
 import org.patientview.radar.model.exception.DecryptionException;
 import org.patientview.radar.model.exception.EmailAddressNotFoundException;
 import org.patientview.radar.model.exception.InvalidSecurityQuestionAnswer;
 import org.patientview.radar.model.exception.RegistrationException;
 import org.patientview.radar.model.exception.UserEmailAlreadyExists;
 import org.patientview.radar.model.filter.PatientUserFilter;
 import org.patientview.radar.model.filter.ProfessionalUserFilter;
 import org.patientview.radar.model.user.AdminUser;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.model.user.ProfessionalUser;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.EmailManager;
 import org.patientview.radar.service.PatientManager;
 import org.patientview.radar.service.UserManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.authentication.ProviderManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.util.StringUtils;
 
 import java.util.Date;
 import java.util.List;
 
 public class UserManagerImpl implements UserManager, UserDetailsService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerImpl.class);
 
 
     private static final String PATIENT_GROUP = "PATIENT"; // Radar patient mapping
     private static final String PATIENT_VIEW_GROUP = "patient"; // Patient view mapping
 
     private EmailManager emailManager;
     private ProviderManager authenticationManager;
 
     private UserDao userDao;
     private JoinRequestDao joinRequestDao;
     private PatientManager patientManager;
 
 
     public AdminUser getAdminUser(String email) {
         return userDao.getAdminUser(email);
     }
 
     public AdminUser getAdminUserWithUsername(String username) {
         return userDao.getAdminUserWithUsername(username);
     }
 
     public PatientUser getPatientUser(Long id) {
         return userDao.getPatientUser(id);
     }
 
     public PatientUser getPatientUser(String email) {
         return userDao.getPatientUser(email);
     }
 
     public PatientUser getPatientUserWithUsername(String username) {
         return userDao.getPatientUserWithUsername(username);
     }
 
     public PatientUser getPatientUser(String email, Date dateOfBirth) {
         PatientUser user = userDao.getPatientUser(email);
         if (user != null) {
             return user.getDateOfBirth().equals(dateOfBirth) ? user : null;
         }
         return null;
     }
 
     public PatientUser getPatientUserWithUsername(String username, Date dateOfBirth) {
         PatientUser user = userDao.getPatientUserWithUsername(username);
         if (user != null && user.getDateOfBirth() != null) {
             return user.getDateOfBirth().equals(dateOfBirth) ? user : null;
         }
         return null;
     }
 
     public List<PatientUser> getPatientUsers() {
         return getPatientUsers(new PatientUserFilter(), -1, -1);
     }
 
     public List<PatientUser> getPatientUsers(PatientUserFilter filter) {
         return getPatientUsers(filter, -1, -1);
     }
 
     public List<PatientUser> getPatientUsers(PatientUserFilter filter, int page, int numberPerPage) {
         return userDao.getPatientUsers(filter, page, numberPerPage);
     }
 
     public void savePatientUser(PatientUser patientUser) throws Exception {
 
         userDao.savePatientUser(patientUser);
     }
 
     public void deletePatientUser(PatientUser patientUser) throws Exception {
         userDao.deletePatientUser(patientUser);
     }
 
 
     /**
      * Create a PV user for this patient.
      * Create Radar user access for this patient
      * Create PV join request for this patient
      * @param patient patient that is being added in Radar
      * @return the combined patient - user object
      * @throws UserCreationException
      * @throws UserMappingException
      * @throws UserRoleException
      * @throws PatientLinkException
      * @throws JoinCreationException
      */
     private PatientUser createUserInPatientViewAndRadar(Patient patient) throws UserCreationException,
             UserMappingException, UserRoleException, PatientLinkException, JoinCreationException {
 
         //-------- Patient View Tables ------//
         // Create the user record
         PatientUser patientUser = createPatientViewUser(patient);
 
         // Create the patient mapping in patient view so patient view knows the user is a patient
         userDao.createRoleInPatientView(patientUser.getId(), PATIENT_VIEW_GROUP);
 
         // Switch from patient view to Radar
         patientUser.setUserId(patientUser.getId());
 
         //---------- Radar Tables -----------//
         patientUser = createRadarUser(patientUser, patient);
         userDao.saveUserMapping(patientUser);
 
         // We've created a new user so we need to create a join request
         createJoinRequest(patient);
 
 
         return patientUser;
 
     }
 
     /**
      * Ensure that this newly added Radar patient/patientUser has the correct permissions.
      * This means, a usermapping to the selected renal unit on the demographic panel,
      * and a usermapping for the disease group they have been added to
      * @param patient the patient record for the patient added
      * @param patientUser the patientUser for the patient added (yes this is confusing)
      */
     private void createPermissionsForNewPatientUser(Patient patient, PatientUser patientUser)
             throws UserMappingException {
         createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), getUnitCode(patient));
         // Map the Disease Group
         if (patient.getDiseaseGroup() != null) {
             createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), patient.getDiseaseGroup()
                     .getId());
         }
         // Map the Patient Group
         createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), PATIENT_GROUP);
     }
 
     private PatientUser createRadarUser(PatientUser patientUser, Patient patient) throws UserCreationException {
 
         // Invalidate the id which relates to patient view
         patientUser.setId(0L);
         // now fill in the radar patient stuff
         // and invalidate the id and this will create a record in tbl_patient_users
         patientUser.setRadarNumber(patient.getRadarNo());
         patientUser.setDateOfBirth(patient.getDob());
         userDao.savePatientUser(patientUser);
 
         return patientUser;
     }
 
     /**
      * Entry point to update or add a patient from the Demographics screen.
      *
      * This is responsible for:
      *  - creating the patient record and linking if necessary.
      *  - create user records and user mappings in radar and patientview
      *  - send a join request for new patients
      *
      * @param patient
      * @return
      * @throws RegisterException
      * @throws Exception
      */
     public PatientUser addPatientUserOrUpdatePatient(Patient patient) throws Exception {
 
         try {
             final boolean isNewPatient = !patient.hasValidId();
 
             patientManager.save(patient);
 
             if (isNewPatient) {
                 PatientUser patientUser;
 
                 if (!userExistsInPatientView(patient.getNhsno())) {
                     patientUser = createUserInPatientViewAndRadar(patient);
                 } else {
                     patientUser = userDao.getPatientViewUser(patient.getNhsno());
                 }
 
                 createPermissionsForNewPatientUser(patient, patientUser);
             }
 
         }  catch (UserCreationException  uce) {
             throw new RegisterException("Could not create user", uce);
         }  catch (UserMappingException ume) {
             throw new RegisterException("Could not create user mappings", ume);
         }  catch (UserRoleException ure) {
             throw new RegisterException("Could not create role", ure);
         }  catch (JoinCreationException jce) {
             throw new RegisterException("User created but could not create join request", jce);
         }  catch (PatientLinkException ple) {
             throw new RegisterException("Could not create role", ple);
         }
 
         return null;
     }
 
     public void createUserMappingInPatientView(String username, String nhsNo, String unitCode)
             throws UserMappingException {
         try {
             if (!userDao.userExistsInPatientView(nhsNo, unitCode)) {
                 userDao.createUserMappingInPatientView(username, nhsNo, unitCode);
             }
         } catch (Exception e) {
             LOGGER.error("Error mapping user {}, {}", username, e.getMessage());
             throw new UserMappingException("Error creating mapping", e);
         }
 
     }
 
     private void createJoinRequest(Patient patient) throws JoinCreationException {
 
         try {
             // Now create a join request for the new user
             JoinRequest joinRequest = new JoinRequest();
             joinRequest.setNhsNo(patient.getNhsno());
             joinRequest.setDateOfBirth(patient.getDob());
             joinRequest.setEmail(patient.getEmailAddress());
             joinRequest.setFirstName(patient.getForename());
             joinRequest.setLastName(patient.getSurname());
             String unitCode = patient.getUnitcode();
 
             if (!StringUtils.hasText(unitCode) && patient.getRenalUnit() != null) {
                 unitCode = patient.getRenalUnit().getUnitCode();
             }
 
             if (!StringUtils.hasText(unitCode) && patient.getDiseaseGroup() != null) {
                 unitCode = patient.getDiseaseGroup().getId();
             }
 
             joinRequest.setUnitcode(unitCode);
             joinRequest.setDateOfRequest(new Date());
 
             joinRequestDao.saveJoinRequest(joinRequest);
         } catch (Exception e) {
             LOGGER.error("Error creating join request", e);
             throw new JoinCreationException("Error creating join request", e);
         }
 
     }
 
 
     private PatientUser createPatientViewUser(Patient patient) throws UserCreationException {
 
         PatientUser patientUser = null;
 
         try {
 
             patientUser = userDao.getPatientViewUser(patient.getNhsno());
 
             // Not registered on the system so create a username for them and a mapping to the patients unit
             if (patientUser == null) {
 
                 patientUser = new PatientUser();
 
                 patientUser.setUsername(generateUsername(patient));
                 patientUser.setName(patient.getForename() + " " + patient.getSurname());
                 patientUser.setPassword(User.getPasswordHash(generateRandomPassword()));
                 patientUser.setEmail(patient.getEmailAddress());
 
                 patientUser = (PatientUser) userDao.createUser(patientUser);
 
             }
         } catch (Exception e) {
             LOGGER.error("Error creating user");
             throw new UserCreationException("Error creating user in database", e);
         }
 
 
         return patientUser;
     }
 
     public void registerProfessional(ProfessionalUser professionalUser) throws UserEmailAlreadyExists,
             InvalidSecurityQuestionAnswer, RegistrationException {
         User user = userDao.getProfessionalUser(professionalUser.getEmail());
         if (user != null) {
             throw new UserEmailAlreadyExists("Email address already exists");
         }
 
         if (!professionalUser.getSecurityQuestion().equals(professionalUser.getSecurityQuestionAnsw())) {
             throw new InvalidSecurityQuestionAnswer("Security question answer is incorrect");
         }
 
         try {
             saveProfessionalUser(professionalUser);
         } catch (Exception e) {
             LOGGER.error("Could not register professional", e);
             throw new RegistrationException("Could not register professional", e);
         }
         emailManager.sendProfessionalRegistrationAdminNotificationEmail(professionalUser);
     }
 
     public ProfessionalUser getProfessionalUser(Long id) {
         return userDao.getProfessionalUser(id);
     }
 
     public ProfessionalUser getProfessionalUser(String email) {
         return userDao.getProfessionalUser(email);
     }
 
     public ProfessionalUser getProfessionalUserWithUsername(String username) {
         return userDao.getProfessionalUserWithUsername(username);
     }
 
     public User getSuperUserWithUsername(String username) {
         return userDao.getSuperUserWithUsername(username);
     }
 
     public void saveProfessionalUser(ProfessionalUser professionalUser) throws Exception {
         // if its a new user generate a password
         if (!professionalUser.hasValidId()) {
             String password = generateRandomPassword();
             professionalUser.setPassword(ProfessionalUser.getPasswordHash(password));
             professionalUser.setUsername(professionalUser.getEmail());
         }
 
         userDao.saveProfessionalUser(professionalUser);
     }
 
     public void deleteProfessionalUser(ProfessionalUser professionalUser) throws Exception {
         userDao.deleteProfessionalUser(professionalUser);
     }
 
     public List<ProfessionalUser> getProfessionalUsers() {
         return getProfessionalUsers(new ProfessionalUserFilter(), -1, -1);
     }
 
     public List<ProfessionalUser> getProfessionalUsers(ProfessionalUserFilter filter) {
         return getProfessionalUsers(filter, -1, -1);
     }
 
     public List<ProfessionalUser> getProfessionalUsers(ProfessionalUserFilter filter, int page, int numberPerPage) {
         return userDao.getProfessionalUsers(filter, page, numberPerPage);
     }
 
     public boolean authenticateProfessionalUser(String username, String password) throws AuthenticationException {
         ProfessionalUser professionalUser = userDao.getProfessionalUserByUsername(username);
         if (professionalUser != null) {
             try {
                 Authentication authentication = authenticationManager.
                         authenticate(new UsernamePasswordAuthenticationToken(username, password));
                 return authentication.isAuthenticated();
             } catch (AuthenticationException e) {
                 LOGGER.warn("Authentication failed for user {} and password {}", username, e.getMessage());
                 throw e;
             }
         }
         return false;
     }
 
     public void changeUserPassword(String username, String password) throws DecryptionException, DaoException {
         ProfessionalUser professionalUser = getProfessionalUser(username);
 
         try {
             userDao.saveProfessionalUser(professionalUser);
         } catch (Exception e) {
             LOGGER.error("could not save professional user", e);
             throw new DaoException("Could not save professional user");
         }
     }
 
     public boolean userExistsInPatientView(String nhsno) {
         return userDao.userExistsInPatientView(nhsno);
     }
 
     public void sendForgottenPasswordToPatient(String username) throws EmailAddressNotFoundException,
             DecryptionException {
         // In theory this could just go in the email manager but we need to query for user first
         PatientUser patientUser = userDao.getPatientUser(username);
         if (patientUser != null) {
             try {
                 String password = generateRandomPassword();
                 patientUser.setPassword(ProfessionalUser.getPasswordHash(password));
 
                 userDao.savePatientUser(patientUser);
 
                 emailManager.sendForgottenPassword(patientUser, password);
             } catch (Exception e) {
                 LOGGER.error("Could not decrypt password for forgotten password email for {}", username, e);
                 throw new DecryptionException("Could not decrypt password for forgotten password email", e);
             }
         } else {
             LOGGER.error("Could not find user with email {}", username);
             throw new EmailAddressNotFoundException("Email Address not found");
         }
     }
 
     public void sendForgottenPasswordToProfessional(String username) throws EmailAddressNotFoundException,
             DecryptionException {
         // In theory this could just go in the email manager but we need to query for user first
         ProfessionalUser professionalUser = userDao.getProfessionalUser(username);
         if (professionalUser != null) {
             try {
                 String password = generateRandomPassword();
                 professionalUser.setPassword(ProfessionalUser.getPasswordHash(password));
 
                 userDao.saveProfessionalUser(professionalUser);
 
                 emailManager.sendForgottenPassword(professionalUser, password);
             } catch (Exception e) {
                 LOGGER.error("Could not decrypt");
                 throw new DecryptionException("Could not decrypt", e);
             }
         } else {
             LOGGER.error("Could not find user with email {}", username);
             throw new EmailAddressNotFoundException("Email Address not found");
         }
     }
 
     private String generateRandomPassword() {
         // I love you Apache commons
         return RandomStringUtils.randomAlphanumeric(8);
     }
 
     public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DataAccessException {
         // Pull the user from the DAO
         User user = userDao.getProfessionalUser(email);
         if (user != null) {
             return user;
         }
         throw new UsernameNotFoundException("User not found with email address " + email);
     }
 
     public User getExternallyCreatedUser(String nshNo) {
         return userDao.getPatientViewUser(nshNo);
     }
 
     public void setEmailManager(EmailManager emailManager) {
         this.emailManager = emailManager;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public void setAuthenticationManager(ProviderManager authenticationManager) {
         this.authenticationManager = authenticationManager;
     }
 
     public String generateUsername(Patient patient) {
 
         //Strip non alpha numeric characters
         String username = patient.getForename().replaceAll("\\P{Alnum}", "") + "."
                 + patient.getSurname().replaceAll("\\P{Alnum}", "");
 
         username = username.toLowerCase();
 
        int i = 1;
        while (userDao.usernameExistsInPatientView(username + i) ||
                userDao.getPatientUserWithUsername(username + i) != null) {
            ++i;
         }
        return username + i;
     }
 
 
     private String getUnitCode(Patient patient) {
         String unitCode = null;
         if (patient.getRenalUnit() != null) {
             unitCode = patient.getRenalUnit().getUnitCode();
         }  else {
             unitCode = patient.getUnitcode();
         }
 
         return unitCode;
     }
 
     public List<String> getPatientRadarMappings(String nhsNo) {
         return userDao.getPatientRadarMappings(nhsNo);
     }
 
     public void setJoinRequestDao(JoinRequestDao joinRequestDao) {
         this.joinRequestDao = joinRequestDao;
     }
 
     public void setPatientManager(PatientManager patientManager) {
         this.patientManager = patientManager;
     }
 
 }
 
