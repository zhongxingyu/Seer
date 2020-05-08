 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.nwu.bioinformatics.commons.CollectionUtils;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
 import edu.northwestern.bioinformatics.studycalendar.domain.WithBigId;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 
 /**
  * @author Rhett Sutphin
  */
 public abstract class WithBigIdDao<T extends DomainObject & WithBigId> extends StudyCalendarDao<T> {
     public T getByBigId(String bigId) {
        Study example = new Study();
        example.setBigId(bigId);
        return (T) CollectionUtils.firstElement(getHibernateTemplate().findByExample(example));
     }
 
     public T getByBigId(T template) {
         return getByBigId(template.getBigId());
     }
 }
