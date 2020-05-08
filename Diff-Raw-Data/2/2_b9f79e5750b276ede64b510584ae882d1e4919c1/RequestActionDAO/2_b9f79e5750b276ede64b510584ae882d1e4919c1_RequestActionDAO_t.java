 package fr.cg95.cvq.dao.request.hibernate;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestAdminAction;
 import fr.cg95.cvq.business.request.RequestAdminAction.Type;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestActionDAO;
 
 /**
  * Implementation of the {@link IRequestActionDAO} interface.
  * 
  * @author jsb@zenexity.fr
  */
 public class RequestActionDAO extends GenericDAO implements IRequestActionDAO {
 
     @Override
     public boolean hasAction(final Long requestId, final RequestActionType type) {
        return !Long.valueOf(0).equals(HibernateUtil.getSession()
             .createQuery("select count(*) from RequestAction where request_id = :requestId and type = :type")
                 .setLong("requestId", requestId).setString("type", type.toString()).uniqueResult());
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RequestAdminAction> getAdminActions() {
         return HibernateUtil.getSession().createCriteria(RequestAdminAction.class)
             .addOrder(Order.desc("date")).list();
     }
 
     @Override
     public boolean hasArchivesMigrationAction() {
         return HibernateUtil.getSession().createCriteria(RequestAdminAction.class)
             .add(Restrictions.eq("type", Type.ARCHIVES_MIGRATED)).uniqueResult() != null;
     }
 }
