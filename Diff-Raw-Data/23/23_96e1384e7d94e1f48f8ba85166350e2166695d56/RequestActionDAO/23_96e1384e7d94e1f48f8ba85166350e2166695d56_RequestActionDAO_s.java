 package fr.cg95.cvq.dao.request.hibernate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Hibernate;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestActionDAO;
 
 /**
  * Implementation of the {@link IRequestActionDAO} interface.
  * 
  * @author bor@zenexity.fr
  */
 public class RequestActionDAO extends GenericDAO implements IRequestActionDAO {
 
    /**
     * TODO : it can happen that a request has two entries for a given resulting
     * state
     */
     public RequestAction findByRequestIdAndResultingState(final Long requestId,
             final RequestState requestState) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestAction as requestAction");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         sb.append(" where request_id = ? ");
         objectList.add(requestId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and resultingState = ? ");
         objectList.add(requestState.toString());
         typeList.add(Hibernate.STRING);
         
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
        return (RequestAction) HibernateUtil.getSession()
             .createQuery(sb.toString())
            .setParameters(objectTab, typeTab).uniqueResult();
     }
 
     public boolean hasAction(final Long requestId, final RequestActionType type) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestAction as requestAction");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         sb.append(" where request_id = ? ");
         objectList.add(requestId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and type = ? ");
         objectList.add(type.toString());
         typeList.add(Hibernate.STRING);
         
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         List resultList = HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
 
         if (resultList == null || resultList.size() == 0)
             return false;
         else
             return true;
     }
 
     public List<RequestAction> listByRequest(final Long requestId) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestAction as requestAction");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         sb.append(" where request_id = ? ");
         objectList.add(requestId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" order by date asc");
         
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
     }
     
     public RequestAction findLastAction(final Long requestId,
         final RequestActionType type) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestAction as requestAction");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         sb.append(" where request_id = ? ");
         objectList.add(requestId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and type = ? ");
         objectList.add(type.toString());
         typeList.add(Hibernate.STRING);
         
         sb.append(" order by date desc");
         
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
 
         List<RequestAction> matchingActions = (List<RequestAction>)HibernateUtil.getSession().createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
         if (!matchingActions.isEmpty()) return matchingActions.get(0);
         return null;
     }
 }
