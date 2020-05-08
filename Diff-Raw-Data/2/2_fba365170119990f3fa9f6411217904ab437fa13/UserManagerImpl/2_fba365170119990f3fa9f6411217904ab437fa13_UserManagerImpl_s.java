 package org.patientview.radar.service.impl;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.patientview.model.Patient;
 import org.patientview.radar.dao.DemographicsDao;
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
 import org.patientview.radar.service.PatientLinkManager;
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
     private static final String PATIENT_GROUP = "PATIENT";
     private static final String PATIENT_VIEW_GROUP = "patient";
 
     private EmailManager emailManager;
     private ProviderManager authenticationManager;
 
     private UserDao userDao;
     private JoinRequestDao joinRequestDao;
     private DemographicsDao demographicsDao;
     private PatientLinkManager patientLinkManager;
 
 
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
         if (user != null) {
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
 
     private PatientUser registerPatientUser(Patient patient) throws UserCreationException, UserMappingException,
             UserRoleException, PatientLinkException, JoinCreationException {
 
         PatientUser patientUser = null;
         boolean generateJoinRequest = false;
 
         // If the patient is new then we save the patient record otherwise we have to link it
         if (!patient.hasValidId()) {
             generateJoinRequest = true;
             demographicsDao.saveDemographics(patient);
         } else {
             patientLinkManager.linkPatientRecord(patient);
         }
 
         // Create the user record
         patientUser = createUser(patient);
 
         // Create the patient mapping in patient view so patient view knows the user is a patient
        userDao.createRoleInPatientView(patientUser.getUserId(), PATIENT_VIEW_GROUP);
 
         createPatientMappings(patient, patientUser);
 
         if (generateJoinRequest) {
             createJoinRequest(patient);
         }
 
         return patientUser;
 
     }
 
 
     public PatientUser savePatientUser(Patient patient) throws RegisterException, Exception {
 
         try {
 
             // Check of the patient needs registering first otherwise just save the patient record
             if (patient.isEditableDemographics() || !userExistsInPatientView(patient.getNhsno())) {
                 return registerPatientUser(patient);
             }  else {
                 demographicsDao.saveDemographics(patient);
                 return null;
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
 
 
     private void createPatientMappings(Patient patient, PatientUser patientUser) throws UserMappingException {
         // Create Radar Mapping
       //  userDao.saveUserMapping(patientUser);
         // Map the Renal Unit
         createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), getUnitCode(patient));
         // Map the Disease Group
         createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), patient.getDiseaseGroup()
                 .getId());
         // Map the Patient Group
         createUserMappingInPatientView(patientUser.getUsername(), patient.getNhsno(), PATIENT_GROUP);
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
 
     private void validatePatient(Patient patient) {
 
         // Check we have a valid radar number, email address and date of birth
         if (patient == null || patient.getId() < 1) {
             throw new IllegalArgumentException("Invalid demographics supplied to savePatientUser");
         }
 
         if (patient.getDob() == null) {
             throw new IllegalArgumentException("Missing required parameter to savePatientUser: " +
                     "demographics.getDateOfBirth()");
         }
 
         if (patient.getNhsno() == null) {
             throw new IllegalArgumentException("Missing required parameter to savePatientUser: " +
                     "demographics.getNhsNumber()");
         }
 
     }
 
     private PatientUser createUser(Patient patient) throws UserCreationException {
 
         PatientUser patientUser = null;
 
         try {
 
             patientUser = userDao.getPatientViewUser(patient.getNhsno());
 
             // Not registered on the system so create a username for them and a mapping to the patients unit
             if (patientUser == null) {
 
                 patientUser = new PatientUser();
 
                 patientUser.setUsername(generateUsername(patient));
                 patientUser.setName(patient.getForename() + " " + patient.getSurname());
                 patientUser.setPassword(generateRandomPassword());
                 patientUser.setEmail(patient.getEmailAddress());
 
                 patientUser = (PatientUser) userDao.createUser(patientUser);
 
                 // now fill in the radar patient stuff
                 // and invalidate the id and this will create a record in tbl_patient_users
                 patientUser.setRadarNumber(patient.getId());
                 patientUser.setDateOfBirth(patient.getDob());
                 //patientUser.setId(0L);
                 userDao.savePatientUser(patientUser);
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
         ProfessionalUser professionalUser = userDao.getProfessionalUser(username);
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
 
     public List<String> getUnitCodes(User user) {
         return userDao.getUnitCodes(user);
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
         while (userDao.usernameExistsInPatientView(username + i) &&
                 userDao.getPatientUserWithUsername(username + 1) == null) {
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
 
     public void setDemographicsDao(DemographicsDao demographicsDao) {
         this.demographicsDao = demographicsDao;
     }
 
     public void setPatientLinkManager(PatientLinkManager patientLinkManager) {
         this.patientLinkManager = patientLinkManager;
     }
 }
 
