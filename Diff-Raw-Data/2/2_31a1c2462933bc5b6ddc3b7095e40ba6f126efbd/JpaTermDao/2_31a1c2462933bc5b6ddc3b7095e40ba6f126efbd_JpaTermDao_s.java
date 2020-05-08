 package com.alwold.classwatch.dao;
 
 import com.alwold.classwatch.model.School;
 import com.alwold.classwatch.model.Term;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.NonUniqueResultException;
 import org.springframework.orm.jpa.support.JpaDaoSupport;
 
 /**
  *
  * @author alwold
  */
 public class JpaTermDao extends JpaDaoSupport implements TermDao {
 
 	public List<Term> getTerms(Long schoolId) {
		return getJpaTemplate().find("from Term t where t.pk.school.id = ? and t.startDate <= ? and t.endDate >= ?", schoolId, new Date(), new Date());
 	}
 	
 	public Term getTerm(Long schoolId, String termCode) {
 		School school = getJpaTemplate().find(School.class, schoolId);
 		List terms = getJpaTemplate().find("from Term where code = ? and school = ?", termCode, school);
 		if (terms.isEmpty()) {
 			return null;
 		} else if (terms.size() > 1) {
 			throw new NonUniqueResultException();
 		} else {
 			return (Term) terms.get(0);
 		}
 	}
 	
 	
 }
