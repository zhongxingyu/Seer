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
 
    @PersistenceContext(unitName = "PersistenceUnit")
    private EntityManager em;
 
     @Override
     public List<OrchestraRegistration> getMessages() {
        TypedQuery<OrchestraRegistration> query = em.createQuery("select m from OrchestraRegistration m", OrchestraRegistration.class);
         return query.getResultList();
     }
 
     @Override
     public void createRegistration(OrchestraRegistration registration) {
        em.merge(registration);
     }
 }
