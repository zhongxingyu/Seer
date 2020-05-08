 package edu.northwestern.bioinformatics.studycalendar.dao.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 import gov.nih.nci.cabig.ctms.dao.MutableDomainObjectDao;
 
 /**
  * @author Nataliya Shurupova
  */
 public class AmendmentDao extends StudyCalendarMutableDomainObjectDao<Amendment> {
     @Override
     public Class<Amendment> domainClass() {
         return Amendment.class;
     }
 
     public List<Amendment> getAll() {
         return getHibernateTemplate().find("from Amendment");
     }

    @Deprecated
    public Amendment getByStudyId(Integer studyId) {
        throw new UnsupportedOperationException("Deprecated");
    }
 }
