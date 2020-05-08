 package org.jtalks.poulpe.model.dao.hibernate;
 
 import java.util.List;
 import org.jtalks.common.model.dao.hibernate.AbstractHibernateParentRepository;
 import org.jtalks.poulpe.model.dao.RankDao;
 import org.jtalks.poulpe.model.entity.Rank;
 
 /**
  * Implementation of dao for {@link Rank}. The most of methods inherited from
  * superclass.
  * @author Pavel Vervenko
  */
 public class RankHibernateDao extends AbstractHibernateParentRepository<Rank> implements RankDao {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final List<Rank> getAll() {
         return getSession().createQuery("from Rank").list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRankNameExists(String name) {
         return ((Number) getSession().createQuery(
                 "select count(*) from Rank r where r.rankName = ?")
                 .setString(0, name)
                 .uniqueResult()).intValue() != 0;
     }
 }
