 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.nwu.bioinformatics.commons.CollectionUtils;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
 import edu.northwestern.bioinformatics.studycalendar.domain.WithBigId;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 
 /**
  * @author Rhett Sutphin
  */
 public abstract class WithBigIdDao<T extends DomainObject & WithBigId> extends StudyCalendarDao<T> {
    @SuppressWarnings("unchecked")
     public T getByBigId(String bigId) {
        return (T) CollectionUtils.firstElement(
            getHibernateTemplate().find("from " + domainClass().getName() + " where bigId = ?", bigId));
     }
 
     public T getByBigId(T template) {
         return getByBigId(template.getBigId());
     }
 }
