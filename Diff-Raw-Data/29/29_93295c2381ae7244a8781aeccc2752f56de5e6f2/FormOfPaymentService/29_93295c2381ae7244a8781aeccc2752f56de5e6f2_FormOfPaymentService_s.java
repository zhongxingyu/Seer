 package com.cse769.EJB.Service;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
import com.cse769.EJB.Entity.EventCategory;
 import com.cse769.EJB.Entity.FormOfPayment;
 
 @Stateless
 public class FormOfPaymentService {
 	
 		@PersistenceContext(unitName="examples-769-EJB")
 		EntityManager em;
 		
 		public void createFormOfPayment() {
 			FormOfPayment formOfPayment = new FormOfPayment();
 			//TODO: 
 			em.persist(formOfPayment);
 		}
 		
 		public FormOfPayment getFormOfPaymentById(Long id) {
 			return em.find(FormOfPayment.class, id);
 		}
 		
 		public void removeFormOfPayment(Long id) {
 			FormOfPayment fop = em.find(FormOfPayment.class, id);
 			em.remove(fop);
 		}
 		
 		public void updateFormOfPayment(FormOfPayment fop) {
			FormOfPayment newFop = new FormOfPayment();
 			newFop.setAddress(fop.getAddress());
 			newFop.setCardNumber(fop.getCardNumber());
 			newFop.setCity(fop.getCity());
 			newFop.setExpiration(fop.getExpiration());
 			newFop.setFullName(fop.getFullName());
 			newFop.setLastFour(fop.getLastFour());
 			newFop.setPhoneNumber(fop.getPhoneNumber());
 			newFop.setState(fop.getPhoneNumber());
 			newFop.setTransactions(fop.getTransactions());
 		}
 
 	}
