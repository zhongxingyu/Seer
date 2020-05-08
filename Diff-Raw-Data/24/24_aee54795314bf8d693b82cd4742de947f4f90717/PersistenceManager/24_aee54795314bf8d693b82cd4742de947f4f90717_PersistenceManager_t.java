 package jpaoletti.jpm.hibernate.core;
 
 import jpaoletti.jpm.core.PMContext;
 import jpaoletti.jpm.core.PMException;
 import jpaoletti.jpm.core.PresentationManager;
import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.hibernate.HibernateUtil;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
 
 public class PersistenceManager implements jpaoletti.jpm.core.PersistenceManager<Session> {
 
     private Session session;
 
     @Override
     public void commit(PMContext ctx, Object transaction) throws Exception {
        try {
            ((Transaction) transaction).commit();
        } catch (ConstraintViolationException e) {
            throw new PMException(MessageFactory.error("constraint.violation.exception", ctx.getEntity().getTitle(), ctx.getOperation().getTitle()));
        }
     }
 
     @Override
     public void finish(PMContext ctx) {
         try {
             getConnection().close();
         } catch (Exception e) {
         }
     }
 
     @Override
     public void init(Session o) throws Exception {
         try {
             this.session = o;
         } catch (Exception e) {
             PresentationManager.getPm().error(e);
             throw new PMException(e);
         }
     }
 
     @Override
     public void rollback(PMContext ctx, Object transaction) throws Exception {
         ((Transaction) transaction).rollback();
         getConnection().close();
         init(getConnection());
         //TODO Need some checks
     }
 
     @Override
     public Object startTransaction(PMContext ctx) throws Exception {
         return getConnection().beginTransaction();
     }
 
     @Override
     public Session getConnection() {
         return session;
     }
 
     @Override
     public Session newConnection() {
         return HibernateUtil.getSession();
     }
 }
