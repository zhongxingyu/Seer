 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import org.hibernate.Hibernate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 @Transactional
 public class UserService implements Serializable {
     private UserDao userDao;
     private UserProvisioningManager userProvisioningManager;
     public static final String STUDY_CALENDAR_APPLICATION_ID = "2";
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public User saveUser(User user, String password, final String emailAddress) {
         if (user == null)
             return null;
 
         if (user.getCsmUserId() == null) {
             try {
                 gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user, password, emailAddress);
                 user.setCsmUserId(csmUser.getUserId());
                 if (csmUser.getUserId() == null) {
                     throw new StudyCalendarSystemException("CSM user did not get an ID on create");
                 }
             } catch (CSTransactionException e) {
                 throw new StudyCalendarSystemException("CSM user creation failed", e);
             }
         } else {
             try {
                 gov.nih.nci.security.authorization.domainobjects.User csmUser = userProvisioningManager.getUserById(user.getCsmUserId().toString());
                 csmUser.setPassword(password);
                 userProvisioningManager.modifyUser(csmUser);
             } catch (CSObjectNotFoundException e) {
                 throw new StudyCalendarSystemException(
                         "%s references CSM user with id %d but CSM reports no such user exists",
                         user.getName(), user.getCsmUserId(), e);
             } catch (CSTransactionException e) {
                 throw new StudyCalendarSystemException("CSM user update failed", e);
             }
         }
 
         userDao.save(user);
         return user;
     }
 
     private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user, String password, final String emailAddress) throws CSTransactionException {
         gov.nih.nci.security.authorization.domainobjects.User csmUser =
                 new gov.nih.nci.security.authorization.domainobjects.User();
         csmUser.setLoginName(user.getName());
         csmUser.setPassword(password);
         // These attributes can't be null. Oracle treats an empty string as NULL.
         csmUser.setFirstName(".");
         csmUser.setLastName(".");
         csmUser.setEmailId(emailAddress);
         userProvisioningManager.createUser(csmUser);
         return csmUser;
     }
 
     public List<User> getSubjectCoordinatorsForSites(List<Site> sites) {
         List<User> users = userDao.getAllSubjectCoordinators();
         List<User> associatedUsers = new ArrayList<User>();
         for (User user : users) {
             for (Site site : sites) {
                 UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
                 if (userRole != null && userRole.getSites().contains(site)) {
                     associatedUsers.add(user);
                     break;
                 }
             }
         }
         return associatedUsers;
     }
 
     public List<User> getSiteCoordinatorsAssignableUsers(User siteCoordinator) {
         List<Site> sites = new ArrayList<Site>();
         List<User> assignableUsers = new ArrayList<User>();
         if (siteCoordinator != null) {
             UserRole userRole = siteCoordinator.getUserRole(Role.SITE_COORDINATOR);
 
             sites.addAll(userRole.getSites());
 
             assignableUsers = getSubjectCoordinatorsForSites(sites);
             Collections.sort(assignableUsers, new NamedComparator());
         }
 
         return assignableUsers;
     }
 
     public String getEmailAddresssForUser(final User user) {
         gov.nih.nci.security.authorization.domainobjects.User csmUser = null;
         try {
             csmUser = userProvisioningManager.getUserById(user.getCsmUserId().toString());
         } catch (CSObjectNotFoundException e) {
             log.error("No csm user found for given csm user id:" + user.getCsmUserId());
         }
         return csmUser != null ? csmUser.getEmailId() : null;
     }
 
     /**
      * Returns the user, fully initialized.
      * @param username
      */
     public User getUserByName(String username) {
         User user = userDao.getByName(username);
         if (user != null) {
             Hibernate.initialize(user);
             for (UserRole role : user.getUserRoles()) {
                 Hibernate.initialize(role.getSites());
                 Hibernate.initialize(role.getStudySites());
             }
         }
         return user;
     }
 
     ////// CONFIGURATION
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     @Required
     public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
         this.userProvisioningManager = userProvisioningManager;
     }
 
 
 }
