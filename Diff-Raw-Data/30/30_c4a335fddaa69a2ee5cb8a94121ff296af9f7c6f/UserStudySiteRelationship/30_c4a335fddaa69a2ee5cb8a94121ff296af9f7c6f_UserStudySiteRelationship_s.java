 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class UserStudySiteRelationship {
     private PscUser user;
     private StudySite studySite;
 
     private UserRelationshipTools tools;
     private Configuration configuration;
 
     public UserStudySiteRelationship(PscUser user, StudySite studySite, Configuration configuration) {
         this.tools = new UserRelationshipTools(user, studySite.getStudy());
         this.user = user;
         this.studySite = studySite;
         this.configuration = configuration;
     }
 
     /**
      * Returns true if the user may approve amendments for this site and there
      * are any amendments to approve.
      * @return
      */
     public boolean getCanApproveAmendments() {
         return !getStudySite().getUnapprovedAmendments().isEmpty() &&
             hasMatchingRole(STUDY_QA_MANAGER);
     }
 
     /**
      * Returns true if the user may assign subjects and the study site is in
      * a state to accept them.
      */
     public boolean getCanAssignSubjects() {
         return !getStudySite().getAmendmentApprovals().isEmpty() &&
             hasMatchingRole(STUDY_SUBJECT_CALENDAR_MANAGER) && isEnabledToAssignSubjects();
     }
 
     public Boolean isEnabledToAssignSubjects() {
         return configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT);
     }
 
     /**
      * Returns true if the user is allowed to create subjects for the
      * associated site.
      */
     public boolean getCanCreateSubjects() {
 
         return hasMatchingRole(SUBJECT_MANAGER);
     }
 
     /**
      * Returns true if the user can create and modify users for the site.
      */
     public boolean getCanAdministerUsers() {
         return hasMatchingRole(USER_ADMINISTRATOR);
     }
 
     /**
      * Returns true if the user can grant access to this study site for particular
      * subject calendar managers.
      */
     public boolean getCanAdministerTeam() {
         return hasMatchingRole(STUDY_TEAM_ADMINISTRATOR);
     }
 
     /**
      * Returns true if the user is allowed to see subject information for the
      * study site and there are any subjects associated with it.
      */
     public boolean getCanSeeSubjectInformation() {
         return !getStudySite().getStudySubjectAssignments().isEmpty()
             && hasAtLeastOneMatchingRole(PscRoleUse.SUBJECT_MANAGEMENT.roles());
     }
 
     /**
      * Returns true if the user is allowed to take subject off from the study for the
      * study site and there are any subjects associated with it.
      */
 
     public boolean getCanTakeSubjectOffStudy() {
         return !getStudySite().getStudySubjectAssignments().isEmpty()
             && hasMatchingRole(STUDY_SUBJECT_CALENDAR_MANAGER);
     }
 
     /**
      * Returns true if the user can see this study site in any capacity.
      */
     public boolean isVisible() {
         return hasAtLeastOneMatchingRole(PscRoleUse.SITE_PARTICIPATION.roles())
             || tools.isManagingAsOneOf(PscRole.STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
     }
 
     public Collection<UserStudySubjectAssignmentRelationship> getVisibleAssignments() {
         return relate(getStudySite().getStudySubjectAssignments());
     }
 
     private Collection<UserStudySubjectAssignmentRelationship> relate(
         Collection<StudySubjectAssignment> assignments
     ) {
         List<UserStudySubjectAssignmentRelationship> result =
             new ArrayList<UserStudySubjectAssignmentRelationship>(assignments.size());
         if (getCanSeeSubjectInformation()) {
             for (StudySubjectAssignment assignment : assignments) {
                 result.add(new UserStudySubjectAssignmentRelationship(getUser(), assignment));
             }
         }
         Collections.sort(result);
         return result;
     }
 
     ////// ACCESSORS
 
     public PscUser getUser() {
         return user;
     }
 
     public StudySite getStudySite() {
         return studySite;
     }
 
     /////// HELPERS
 
     private boolean hasMatchingRole(PscRole role) {
         return hasAtLeastOneMatchingRole(role);
     }
 
     private boolean hasAtLeastOneMatchingRole(PscRole... roles) {
         for (PscRole role : roles) {
             if (tools.hasMatchingScope(role, getStudySite().getSite())
                 && tools.hasMatchingStudyScope(role)) {
                 return true;
             }
         }
         return false;
     }
 
     ////// OBJECT METHODS
 
     @Override
     public String toString() {
         return new StringBuilder(getClass().getSimpleName()).
             append("[user=").append(getUser()).
             append("; studySite=").append(getStudySite()).
             append(']').toString();
     }
 }
