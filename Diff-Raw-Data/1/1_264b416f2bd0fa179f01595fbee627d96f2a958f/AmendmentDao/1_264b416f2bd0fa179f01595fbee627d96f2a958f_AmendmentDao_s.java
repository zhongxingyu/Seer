 package edu.northwestern.bioinformatics.studycalendar.dao.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Hibernate;
 import org.hibernate.type.Type;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.criterion.Criterion;
 import org.springframework.orm.hibernate3.HibernateCallback;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 /**
  * @author Nataliya Shurupova
  * @author Rhett Sutphin
  */
 public class AmendmentDao extends StudyCalendarMutableDomainObjectDao<Amendment> implements DeletableDomainObjectDao<Amendment> {
     @Override
     public Class<Amendment> domainClass() {
         return Amendment.class;
     }
 
     @SuppressWarnings({ "unchecked" })
     public List<Amendment> getAll() {
         return getHibernateTemplate().find("from Amendment");
     }
 
     @SuppressWarnings({ "unchecked" })
     public Amendment getByNaturalKey(String key) {
         final Amendment.Key keyParts = Amendment.decomposeNaturalKey(key);
         List<Amendment> results = getHibernateTemplate().executeFind(new HibernateCallback() {
             public Object doInHibernate(Session session) throws HibernateException {
                 Criteria crit = session.createCriteria(Amendment.class)
                         .add(Restrictions.disjunction()
                             .add(Restrictions.like("date", keyParts.getDate()))
                             .add(Restrictions.eq("date", keyParts.getDate()))
                         );
                 if (keyParts.getName() != null) crit.add( Restrictions.eq("name", keyParts.getName()) );
                 crit.addOrder(Order.asc("name"));
                 return crit.list();
             }
         });
         if (results.size() == 0) {
             return null;
         } else if (results.size() > 1) {
             // if there's one with this date and no name, it matches
             for (Amendment result : results) if (result.getName() == null) return result;
 
             List<String> resultKeys = new ArrayList<String>(results.size());
             for (Amendment result : results) resultKeys.add(result.getNaturalKey());
             throw new StudyCalendarValidationException(
                 "More than one amendment could match %s: %s.  Please be more specific.",
                 key, StringUtils.join(resultKeys.iterator(), ", "));
         } else {
             return results.get(0);
         }
     }
 
     public void delete(Amendment amendment) {
         getHibernateTemplate().delete(amendment);
     }
 }
