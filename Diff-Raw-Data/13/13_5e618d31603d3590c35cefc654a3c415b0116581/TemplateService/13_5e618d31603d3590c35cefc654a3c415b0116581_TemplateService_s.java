 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import static java.util.Arrays.asList;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
 import edu.nwu.bioinformatics.commons.StringUtils;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.util.ObjectSetUtil;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.annotation.Propagation;
 
 import java.util.*;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 @Transactional
 public class TemplateService {
     public static final String SUBJECT_COORDINATOR_ACCESS_ROLE = "SUBJECT_COORDINATOR";
     public static final String SUBJECT_COORDINATOR_GROUP = "SUBJECT_COORDINATOR";
     private StudyCalendarAuthorizationManager authorizationManager;
     private StudyDao studyDao;
     private SiteDao siteDao;
     private StudySiteDao studySiteDao;
     private DeltaDao deltaDao;
     private UserRoleDao userRoleDao;
 
     public static final String USER_IS_NULL = "User is null";
     public static final String SITE_IS_NULL = "Site is null";
     public static final String SITES_LIST_IS_NULL = "Sites List is null";
     public static final String STUDY_IS_NULL = "Study is null";
     public static final String LIST_IS_NULL = "List parameter is null";
     public static final String STUDIES_LIST_IS_NULL = "StudiesList is null";
     public static final String STRING_IS_NULL = "String parameter is null";
 
     public void assignTemplateToSites(Study studyTemplate, List<Site> sites) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (sites == null) {
             throw new IllegalArgumentException(SITES_LIST_IS_NULL);
         }
         for (Site site : sites) {
             StudySite ss = new StudySite();
             ss.setStudy(studyTemplate);
             ss.setSite(site);
             studySiteDao.save(ss);
         }
     }
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User
             assignTemplateToSubjectCoordinator( Study study,
                                                     Site site,
                                                     edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception {
         if (study == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (user == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
 
         UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
         if (!userRole.getStudySites().contains(findStudySite(study, site))) {
             userRole.addStudySite(findStudySite(study, site));
             userRoleDao.save(userRole);
 
             assignMultipleTemplates(asList(study), site, user.getCsmUserId().toString());
         }
 
         return user;
     }
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User
             removeAssignedTemplateFromSubjectCoordinator(Study study,
                                                              Site site,
                                                              edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception {
         if (study == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (user == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
         UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
         if (userRole.getStudySites().contains(findStudySite(study, site))) {
             userRole.removeStudySite(findStudySite(study, site));
             userRoleDao.save(userRole);
 
             removeMultipleTemplates(asList(study), site, user.getCsmUserId().toString());
         }
 
         return user;
     }
     
     public void removeTemplateFromSites(Study studyTemplate, List<Site> sites) {
         List<StudySite> studySites = studyTemplate.getStudySites();
         List<StudySite> toRemove = new LinkedList<StudySite>();
         List<Site> cannotRemove = new LinkedList<Site>();
         for (Site site : sites) {
             for (StudySite studySite : studySites) {
                 if (studySite.getSite().equals(site)) {
                     if (studySite.isUsed()) {
                         cannotRemove.add(studySite.getSite());
                     } else {
                         try {
                             authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(studySite));
                         } catch (RuntimeException e) {
                             throw e;
                         } catch (Exception e) {
                             throw new StudyCalendarSystemException(e);
                         }
                         toRemove.add(studySite);
                     }
                 }
             }
         }
         for (StudySite studySite : toRemove) {
             Site siteAssoc = studySite.getSite();
             siteAssoc.getStudySites().remove(studySite);
             siteDao.save(siteAssoc);
             Study studyAssoc = studySite.getStudy();
             studyAssoc.getStudySites().remove(studySite);
             studyDao.save(studyAssoc);
         }
         if (cannotRemove.size() > 0) {
             StringBuilder msg = new StringBuilder("Cannot remove ")
                 .append(StringUtils.pluralize(cannotRemove.size(), "site"))
                 .append(" (");
             for (Iterator<Site> it = cannotRemove.iterator(); it.hasNext();) {
                 Site site = it.next();
                 msg.append(site.getName());
                 if (it.hasNext()) msg.append(", ");
             }
             msg.append(") from study ").append(studyTemplate.getName())
                 .append(" because there are subject(s) assigned");
             throw new StudyCalendarValidationException(msg.toString());
         }
     }
     
     public void assignMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
         if (studyTemplates == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (userId == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         List<String> assignedUserIds = new ArrayList<String>();
 
         assignedUserIds.add(userId);
 
         for (Study template : studyTemplates) {
             List<StudySite> studySites = template.getStudySites();
             for (StudySite studySite : studySites) {
                 if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                     String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                     authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, SUBJECT_COORDINATOR_ACCESS_ROLE);
                 }
             }
         }
     }
 
     public Map getSiteLists(Study studyTemplate) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         Map<String, List> siteLists = new HashMap<String, List>();
         List<Site> availableSites = new ArrayList<Site>();
         List<Site> assignedSites = new ArrayList<Site>();
         List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
         for (ProtectionGroup site : allSitePGs) {
             availableSites.add(DomainObjectTools.loadFromExternalObjectId(site.getProtectionGroupName(), siteDao));
         }
         for (StudySite ss : studyTemplate.getStudySites()) {
             assignedSites.add(ss.getSite());
         }
         availableSites = (List) ObjectSetUtil.minus(availableSites, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);
 
         return siteLists;
     }
     
     @SuppressWarnings({ "unchecked" })
     public Map<String, List<Study>> getTemplatesLists(Site site, User subjectCdUser) throws Exception {
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (subjectCdUser == null) {
             throw new IllegalArgumentException(USER_IS_NULL);
         }
         Map<String, List<Study>> templatesMap = new HashMap<String, List<Study>>();
         List<Study> assignedTemplates = new ArrayList<Study>();
         List<Study> allTemplates = new ArrayList<Study>();
 
         List<StudySite> studySites = site.getStudySites();
         for (StudySite studySite : studySites) {
             allTemplates.add(studySite.getStudy());
             if (authorizationManager.isUserPGAssigned(DomainObjectTools.createExternalObjectId(studySite), subjectCdUser.getUserId().toString())) {
                 assignedTemplates.add(studySite.getStudy());
             }
         }
 
         List<Study> availableTemplates = (List<Study>) ObjectSetUtil.minus(allTemplates, assignedTemplates);
         templatesMap.put(StudyCalendarAuthorizationManager.ASSIGNED_PES, assignedTemplates);
         templatesMap.put(StudyCalendarAuthorizationManager.AVAILABLE_PES, availableTemplates);
         return templatesMap;
     }
     
     public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
         if(siteName == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         return authorizationManager.getPGByName(siteName);
     }
 
     /**
      * Returns a copy of the given list of studies containing only those which should be
      * visible to the given user role.
      *
      * @param studies
      * @return
      * @throws Exception
      */
     public List<Study> filterForVisibility(List<Study> studies, UserRole role) throws Exception {
         if (studies == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         if (role == null) {
             return Collections.emptyList();
         }
 
         List<Study> filtered = new ArrayList<Study>(studies);
         for (Iterator<Study> it = filtered.iterator(); it.hasNext();) {
             Study study = it.next();
             if (!authorizationManager.isTemplateVisible(role, study)) it.remove();
         }
         return filtered;
     }
 
     public void removeMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
         if (studyTemplates == null) {
             throw new IllegalArgumentException(STUDIES_LIST_IS_NULL);
         }
         if (site == null) {
             throw new IllegalArgumentException(SITE_IS_NULL);
         }
         if (userId == null) {
             throw new IllegalArgumentException(STRING_IS_NULL);
         }
         List<String> userIds = new ArrayList<String>();
 
         userIds.add(userId);
 
         for (Study template : studyTemplates) {
             List<StudySite> studySites = template.getStudySites();
             for (StudySite studySite : studySites) {
 
                 if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                     String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                     ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
                     authorizationManager.removeProtectionGroupUsers(userIds, studySitePG);
                 }
             }
         }
     }
     
     @Transactional(propagation = Propagation.SUPPORTS)
     public <P extends PlanTreeNode<?>> P findParent(PlanTreeNode<P> node) {
         if (node.getParent() != null) {
             return node.getParent();                                                                                                    
         } else {
             Delta<P> delta = deltaDao.findDeltaWhereAdded(node);
             if (delta != null) {
                 return delta.getNode();
             }
 
             delta = deltaDao.findDeltaWhereRemoved(node);
             if (delta != null) {
                 return delta.getNode();
             }
 
             throw new StudyCalendarSystemException("Could not locate delta where %s was added or where it was removed", node);
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public <T extends PlanTreeNode<?>> T findAncestor(PlanTreeNode<?> node, Class<T> klass) {
         boolean moreSpecific = DomainObjectTools.isMoreSpecific(node.getClass(), klass);
         boolean parentable = PlanTreeNode.class.isAssignableFrom(node.parentClass());
         if (moreSpecific && parentable) {
             PlanTreeNode<?> parent = findParent((PlanTreeNode<? extends PlanTreeNode<?>>) node);
             if (klass.isAssignableFrom(parent.getClass())) {
                 return (T) parent;
             } else {
                 return findAncestor(parent, klass);
             }
         } else {
             throw new StudyCalendarSystemException("%s is not a descendant of %s",
                 node.getClass().getSimpleName(), klass.getSimpleName());
         }
     }
 
     // this is PlanTreeNode instead of PlanTreeNode<?> due to a javac bug
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     public Study findStudy(PlanTreeNode node) {
         if (node instanceof PlannedCalendar) return ((PlannedCalendar) node).getStudy();
         else return findAncestor(node, PlannedCalendar.class).getStudy();
     }
 
     /**
      * Finds the node in the given study which matches the type and id of parameter node.
      */
     public PlanTreeNode<?> findEquivalentChild(Study study, PlanTreeNode<?> node) {
         return findEquivalentChild(study.getPlannedCalendar(), node);
     }
 
     public PlanTreeNode<?> findEquivalentChild(PlanTreeNode<?> tree, PlanTreeNode<?> parameterNode) {
         if (isEquivalent(tree, parameterNode)) return tree;
         if (tree instanceof PlanTreeInnerNode) {
             for (PlanTreeNode<?> child : ((PlanTreeInnerNode<?, PlanTreeNode<?>, ?>) tree).getChildren()) {
                 PlanTreeNode<?> match = findEquivalentChild(child, parameterNode);
                 if (match != null) return match;
             }
         }
         return null;
     }
 
     public boolean isEquivalent(PlanTreeNode<?> node, PlanTreeNode<?> toMatch) {
         return (toMatch == node) ||
             (sameClassIgnoringProxies(toMatch, node) && toMatch.getId().equals(node.getId()));
     }
 
     // This is not a general solution, but it will work for all PlanTreeNode subclasses
     private boolean sameClassIgnoringProxies(PlanTreeNode<?> toMatch, PlanTreeNode<?> node) {
         return toMatch.getClass().isAssignableFrom(node.getClass())
             || node.getClass().isAssignableFrom(toMatch.getClass());
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
     
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
     public void setStudySiteDao(StudySiteDao studySiteDao) {
         this.studySiteDao = studySiteDao;
     }
 
     public void setDeltaDao(DeltaDao deltaDao) {
         this.deltaDao = deltaDao;
     }
 
     @Required
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
         this.authorizationManager = authorizationManager;
     }
 
     public void setUserRoleDao(UserRoleDao userRoleDao) {
         this.userRoleDao = userRoleDao;
     }
 }
