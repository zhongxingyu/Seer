 package edu.lmu.cs.headmaster.ws.dao;
 
 import java.util.List;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import edu.lmu.cs.headmaster.ws.dao.util.QueryBuilder;
 import edu.lmu.cs.headmaster.ws.domain.Grant;
 
 public class GrantDaoHibernateImpl extends HibernateDaoSupport implements GrantDao {
     
     @Override
     public Grant getGrantById(Long id) {
         return getHibernateTemplate().get(Grant.class, id);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<Grant> getGrants(String query, Boolean awarded,
             Boolean presented, int skip, int max) {
         return createGrantQuery(query, awarded, presented)
                 .build(getSession())
                 .setFirstResult(skip)
                 .setMaxResults(max)
                 .list();
     }
 
     @Override
     public Grant createGrant(Grant grant) {
         getHibernateTemplate().save(grant);
         return grant;
     }
 
     @Override
     public void createOrUpdateGrants(Grant grant) {
         getHibernateTemplate().saveOrUpdate(grant);
     }
 
     /**
      * Returns a base HQL query object (no pagination) for the given parameters
     * for students.
      */
     private QueryBuilder createGrantQuery(String query, Boolean awarded,
             Boolean presented) {
         // The desired return order is id.
         QueryBuilder builder = new QueryBuilder(
             "select g from Grant g",
             "order by id"
         );
         
         if (query != null) {
             builder.clause("lower(g.facultyMentor) like lower(:query) or lower(g.title) like lower(:query)", query + "%");
         }
 
         if (awarded != null) {
             builder.clause("g.awarded = :awarded", awarded + "%");
         }
 
         if (presented != null) {
             builder.clause("g.presented = :presented", presented + "%");
         }
 
         // All done.
         return builder;
     }
 
 }
