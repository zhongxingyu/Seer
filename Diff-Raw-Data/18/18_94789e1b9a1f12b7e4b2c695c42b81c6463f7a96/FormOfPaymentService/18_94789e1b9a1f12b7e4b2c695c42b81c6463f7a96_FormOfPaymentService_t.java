 package com.cse769.EJB.Service;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
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
 
 	}
