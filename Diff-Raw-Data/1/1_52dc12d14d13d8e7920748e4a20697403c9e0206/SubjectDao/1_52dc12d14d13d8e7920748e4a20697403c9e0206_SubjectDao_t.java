 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.nwu.bioinformatics.commons.CollectionUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 import java.util.List;
 
@Transactional(readOnly = true)
 public class SubjectDao extends StudyCalendarMutableDomainObjectDao<Subject> implements DeletableDomainObjectDao<Subject> {
     @Override
     public Class<Subject> domainClass() {
         return Subject.class;
     }
 
     /**
      * Finds all the subjects
      *
      * @return      a list of all the subjects
      */
     public List<Subject> getAll() {
         return getHibernateTemplate().find("from Subject p order by p.lastName, p.firstName");
     }
 
     /**
      * Finds the subject assignment for the given subject, study, and site
      *
      * @param  subject the subject to search with for the assignment
      * @param  study the study to search with for the assignment
      * @param  site the site to search with for the assignment
      * @return      the subject assignment for the given subject, study, and site
      */
     public StudySubjectAssignment getAssignment(final Subject subject, final Study study, final Site site) {
         return (StudySubjectAssignment) CollectionUtils.firstElement(getHibernateTemplate().find(
                 "from StudySubjectAssignment a where a.subject = ? and a.studySite.site = ? and a.studySite.study = ?",
                 new Object[]{subject, site, study}));
     }
 
     /**
      * Finds the subject for the given mrn (person id)
      *
      * @param  mrn the mrn (person id) to search for the subject with
      * @return      the subject that correspnds to the given mrn
      */
     @SuppressWarnings("unchecked")
     public Subject findSubjectByPersonId(final String mrn) {
         List<Subject> results = getHibernateTemplate().find("from Subject s left join fetch s.assignments where s.personId= ?", mrn);
         if (!results.isEmpty()) {
             Subject subject = results.get(0);
             return subject;
         }
         String message = "No subject exist with the given mrn :" + mrn;
         logger.info(message);
 
         return null;
     }
 
     /**
      * Finds all the subjects for the given first name, last name, and birth date.
      *
      * @param  firstName the first name to search for the subject with
      * @param  lastName the lastName to search for the subject with
      * @param  dateOfBirth the birth date to search for the subject with
      * @return      finds the subject for the given first name, last name and date of birth
      */
     @SuppressWarnings("unchecked")
     public List<Subject> findSubjectByFirstNameLastNameAndDoB(final String firstName, final String lastName, Date dateOfBirth) {
         List<Subject> results = getHibernateTemplate().find("from Subject s left join fetch s.assignments where s.firstName= ? and s.lastName= ? and s.dateOfBirth= ?", new Object[] {firstName, lastName, dateOfBirth});
         if (!results.isEmpty()) {
             return results;
         }
         String message = "No subject exist with the given firstName : " + firstName + " , lastName : " + lastName + " ,dateOfBirth : " + dateOfBirth;
         logger.info(message);
 
         return null;
     }
 
     @Transactional(readOnly = false)
     public void delete(Subject subject) {
         getHibernateTemplate().delete(subject);
     }
 }
