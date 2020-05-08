 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package test;
 
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author Coevtmw
  */
 @Stateless
 public class InstrumentFacade implements InstrumentFacadeLocal, InstrumentFacadeRemote {
     @PersistenceContext
     private EntityManager em;
 
     public void create(Instrument instrument) {
         em.persist(instrument);
     }
 
     public void edit(Instrument instrument) {
         em.merge(instrument);
     }
 
     public void remove(Instrument instrument) {
         em.remove(em.merge(instrument));
     }
 
     public Instrument find(Object id) {
         return em.find(test.Instrument.class, id);
     }
 
     public List<Instrument> findAll() {
         return em.createQuery("select object(o) from Instrument as o").getResultList();
     }
    
    public Item findItem(Integer id) {
        return em.find(test.Item.class, id);
    }
 
 }
