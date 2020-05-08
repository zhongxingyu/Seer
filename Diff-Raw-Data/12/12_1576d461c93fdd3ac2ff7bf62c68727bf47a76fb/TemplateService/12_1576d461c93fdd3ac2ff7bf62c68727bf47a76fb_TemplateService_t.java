 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
 import edu.northwestern.bioinformatics.studycalendar.web.StudyListController;
 import edu.nwu.bioinformatics.commons.StringUtils;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.util.ObjectSetUtil;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 import java.util.*;
 import static java.util.Arrays.asList;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 // TODO: None of these methods should throw plain java.lang.Exception
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
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     private DaoFinder daoFinder;
 
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
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User assignTemplateToSubjectCoordinator(
         Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
     ) throws Exception {
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
 
     public edu.northwestern.bioinformatics.studycalendar.domain.User removeAssignedTemplateFromSubjectCoordinator(
         Study study, Site site, edu.northwestern.bioinformatics.studycalendar.domain.User user
     ) throws Exception {
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
         StudySite studySite = study.getStudySite(site);
         if (user.hasAssignment(studySite)) {
             throw new StudyCalendarValidationException("%s is still responsible for one or more subjects on %s at %s.  Please reassign those subjects before removing %s from that study and site.",
                 user.getName(), study.getAssignedIdentifier(), site.getName(), user.getName());
         }
         if (userRole.getStudySites().contains(studySite)) {
             userRole.removeStudySite(studySite);
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
 
     @SuppressWarnings({ "unchecked" })
     public Map<String, List<Site>> getSiteLists(Study studyTemplate) throws Exception {
         if (studyTemplate == null) {
             throw new IllegalArgumentException(STUDY_IS_NULL);
         }
         Map<String, List<Site>> siteLists = new HashMap<String, List<Site>>();
         List<Site> availableSites = new ArrayList<Site>();
         List<Site> assignedSites = new ArrayList<Site>();
         List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
         for (ProtectionGroup sitePG : allSitePGs) {
             String pgName = sitePG.getProtectionGroupName();
             Site site = DomainObjectTools.loadFromExternalObjectId(pgName, siteDao);
             if (site == null) throw new StudyCalendarSystemException("%s does not map to a PSC site", pgName);
             availableSites.add(site);
         }
         for (StudySite ss : studyTemplate.getStudySites()) {
             assignedSites.add(ss.getSite());
         }
         availableSites = (List<Site>) ObjectSetUtil.minus(availableSites, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
         siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);
 
         return siteLists;
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
 
     public <T extends PlanTreeNode<?>> Collection<T> findChildren(PlanTreeInnerNode node, Class<T> childClass) {
         List<T> children = new LinkedList<T>();
         findChildren(node, childClass, children);
         return children;
     }
 
     private <T extends PlanTreeNode<?>> void findChildren(PlanTreeInnerNode node, Class<T> childClass, Collection<T> target) {
         if (childClass.isAssignableFrom(node.childClass())) {
             target.addAll(node.getChildren());
         } else {
             for (Object o : node.getChildren()) {
                 if (o instanceof PlanTreeInnerNode) {
                     findChildren((PlanTreeInnerNode) o, childClass, target);
                 }
             }
         }
     }
 
     // this is PlanTreeNode instead of PlanTreeNode<?> due to a javac bug
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     @Transactional(propagation = Propagation.SUPPORTS)
     public Study findStudy(PlanTreeNode node) {
         if (node instanceof PlannedCalendar) return ((PlannedCalendar) node).getStudy();
         else return findAncestor(node, PlannedCalendar.class).getStudy();
     }
 
     /**
      * Finds the node in the given study which matches the type and id of parameter node.
      */
     @Transactional(propagation = Propagation.SUPPORTS)
     public PlanTreeNode<?> findEquivalentChild(Study study, PlanTreeNode<?> node) {
         return findEquivalentChild(study.getPlannedCalendar(), node);
     }
 
     @Transactional(propagation = Propagation.SUPPORTS)
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
 
     @Transactional(propagation = Propagation.SUPPORTS)
     public boolean isEquivalent(PlanTreeNode<?> node, PlanTreeNode<?> toMatch) {
         return (toMatch == node) ||
             (sameClassIgnoringProxies(toMatch, node) && identifiersMatch(toMatch, node));
     }
 
     // This is not a general solution, but it will work for all PlanTreeNode subclasses
     private boolean sameClassIgnoringProxies(PlanTreeNode<?> toMatch, PlanTreeNode<?> node) {
         return toMatch.getClass().isAssignableFrom(node.getClass())
             || node.getClass().isAssignableFrom(toMatch.getClass());
     }
 
     private boolean identifiersMatch(PlanTreeNode<?> toMatch, PlanTreeNode<?> node) {
         boolean idMatch, gridIdMatch;
         idMatch = gridIdMatch = false;
 
         if (toMatch.getId() != null && node.getId() != null) {
             idMatch = toMatch.getId().equals(node.getId());
         }
         if (toMatch.getGridId() != null && node.getGridId() != null) {
             gridIdMatch = toMatch.getGridId().equals(node.getGridId());
         }
         return (idMatch || gridIdMatch);
     }
 
     // XXX TODO: it is inappropriate to have a reference to the web layer in the service layer
     public List<StudyListController.ReleasedTemplate> getPendingTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception{
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = filterForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, filterForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
         List<Study> subjectAssignableStudies = filterForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
             devableStudies,
             filterForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
             subjectAssignableStudies
         );
 
         List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new StudyListController.ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
 
         List<StudyListController.ReleasedTemplate> pendingTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
 
         for (StudyListController.ReleasedTemplate releasedTemplate: releasedTemplates) {
             Study releasedTemplateStudy = releasedTemplate.getStudy();
             List<Site> sites = releasedTemplateStudy.getSites();
             if (sites.size()==0 ) {
                 if (!pendingTemplates.contains(releasedTemplate)) {
                      pendingTemplates.add(releasedTemplate);
                 }
             }
             for (Site site : sites) {
                 if (!isStudyAssignedToAnySite(releasedTemplateStudy) ||
                     !isStudyApprovedBySite(site, releasedTemplateStudy) ||
                     !isSubjectCoordinatorAssignedToStudy(releasedTemplateStudy)) {
                     if (!pendingTemplates.contains(releasedTemplate)) {
                      pendingTemplates.add(releasedTemplate);
                     }
                 }
             }
         }
        Collections.sort(pendingTemplates, new AlphabeticallyOrderedComparator());
         return pendingTemplates;
     }
 
    private static class AlphabeticallyOrderedComparator implements Comparator<StudyListController.ReleasedTemplate> {
        public static final Comparator<? super StudyListController.ReleasedTemplate> INSTANCE = new AlphabeticallyOrderedComparator();
        public int compare(StudyListController.ReleasedTemplate rt1, StudyListController.ReleasedTemplate rt2) {
             return rt1.getDisplayName().compareTo(rt2.getDisplayName());
        }
    }

     // XXX TODO: it is inappropriate to have a reference to the web layer in the service layer
     public List<StudyListController.ReleasedTemplate> getReleasedAndAssignedTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception{
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = filterForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, filterForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<Study> subjectAssignableStudies = filterForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
             devableStudies,
             filterForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
             subjectAssignableStudies
         );
 
         List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new StudyListController.ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
 
         List<StudyListController.ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
 
         for (StudyListController.ReleasedTemplate releasedTemplate: releasedTemplates) {
             Study releasedTemplateStudy = releasedTemplate.getStudy();
             List<Site> sites = releasedTemplateStudy.getSites();
             for (Site site: sites) {
                 if (isStudyAssignedToAnySite(releasedTemplateStudy) &&
                     isStudyApprovedBySite(site, releasedTemplateStudy) &&
                     isSubjectCoordinatorAssignedToStudy(releasedTemplateStudy)){
                     if (!releasedAndAssignedTemplates.contains(releasedTemplate)) {
                         releasedAndAssignedTemplates.add(releasedTemplate);
                     }
                 }
             }
         }
 
         log.info("releasedAndAssignedTemplate " + releasedAndAssignedTemplates);
         return releasedAndAssignedTemplates;
     }
 
     // XXX TODO: it is inappropriate to have a reference to the web layer in the service layer
     public List<StudyListController.ReleasedTemplate> getReleasedTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception{
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = filterForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, filterForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<Study> subjectAssignableStudies = filterForVisibility(studies, user.getUserRole(SUBJECT_COORDINATOR));
 
         List<Study> visibleStudies = union(
             devableStudies,
             filterForVisibility(studies, user.getUserRole(SITE_COORDINATOR)),
             subjectAssignableStudies
         );
 
 
         List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
         for (Study visibleStudy : visibleStudies) {
             if (visibleStudy.isReleased()) {
                 releasedTemplates.add(new StudyListController.ReleasedTemplate(visibleStudy, subjectAssignableStudies.contains(visibleStudy)));
             }
         }
         log.info("releasedTemplates " + releasedTemplates);
         return releasedTemplates;
     }
 
     // XXX TODO: it is inappropriate to have a reference to the web layer in the service layer
     public List<StudyListController.DevelopmentTemplate> getInDevelopmentTemplates(List<Study> studies, edu.northwestern.bioinformatics.studycalendar.domain.User user) throws Exception{
         log.debug("{} studies found total", studies.size());
         List<Study> devableStudies = filterForVisibility(studies, user.getUserRole(STUDY_COORDINATOR));
         devableStudies = union(devableStudies, filterForVisibility(studies, user.getUserRole(STUDY_ADMIN)));
 
         List<StudyListController.DevelopmentTemplate> inDevelopmentTemplates = new ArrayList<StudyListController.DevelopmentTemplate>();
         for (Study devableStudy : devableStudies) {
             if (devableStudy.isInDevelopment()) {
                 inDevelopmentTemplates.add(new StudyListController.DevelopmentTemplate(devableStudy));
             }
         }
         log.info("inDevelopmentTemplates " + inDevelopmentTemplates);
         return inDevelopmentTemplates;
     }
 
 
     private List<Study> union(List<Study>... lists) {
         Set<Study> union = new LinkedHashSet<Study>();
         for (List<Study> list : lists) {
             union.addAll(list);
         }
         return new ArrayList<Study>(union);
     }
 
     private boolean isStudyAssignedToAnySite(Study study) {
         return !study.getStudySites().isEmpty();
     }
 
     private boolean isStudyApprovedBySite(Site site, Study study) {
         return site.getStudySite(study).getUnapprovedAmendments().isEmpty();
     }
 
 
     private boolean isSubjectCoordinatorAssignedToStudy(Study study) {
         for (StudySite studySite : study.getStudySites()) {
             List<UserRole> userRoles = studySite.getUserRoles();
             for(UserRole userRole : userRoles) {
                 if (userRole.getRole().equals(Role.SUBJECT_COORDINATOR)){
                     return true;
                 }
             }
         }
         return false;
     }
 
     protected <T extends PlanTreeNode<?>> void delete(Collection<T> collection) {
         for (T t : collection) delete(t);
     }
 
     @SuppressWarnings({ "unchecked" })
     public <T extends PlanTreeNode> void delete(T object) {
         DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(object.getClass());
         if (!(dao instanceof DeletableDomainObjectDao)) {
             throw new StudyCalendarSystemException(
                 "DAO for %s (%s) does not implement the deletable interface",
                 object.getClass().getSimpleName(), dao.getClass().getName()
                 );
         }
         DeletableDomainObjectDao<T> deleter = (DeletableDomainObjectDao) dao;
         if (object instanceof PlanTreeInnerNode) {
             PlanTreeInnerNode innerNode = (PlanTreeInnerNode) object;
             delete(innerNode.getChildren());
             innerNode.getChildren().clear();
         }
         deleter.delete(object);
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
 
     @Required
     public void setUserRoleDao(UserRoleDao userRoleDao) {
         this.userRoleDao = userRoleDao;
     }
 
     @Required
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 }
