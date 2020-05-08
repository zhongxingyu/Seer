 package org.riviti.EnterpriseTest.Backend;
 
 import org.riviti.EnterpriseTest.Common.OrchestraRegistration;
 import org.riviti.EnterpriseTest.Common.OrchestraRegistrationHandler;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 @Stateless
 public class OrchestraRegistrationHandlerBean implements OrchestraRegistrationHandler {
 
    private EntityManager entityManager;
 
     @Override
     public List<OrchestraRegistration> getMessages() {
        TypedQuery<OrchestraRegistration> query = entityManager.createQuery("select m from OrchestraRegistration m", OrchestraRegistration.class);
         return query.getResultList();
     }
 
     @Override
     public void createRegistration(OrchestraRegistration registration) {
        entityManager.merge(registration);
    }

    @PersistenceContext(unitName = "PersistenceUnit")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
     }
 }
