 package com.cse769.EJB.Service;
 
import java.util.List;

 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
import com.cse769.EJB.Entity.Event;
import com.cse769.EJB.Entity.EventCategory;
 import com.cse769.EJB.Entity.FormOfPayment;
 
 public class FormOfPaymentService {
 	
	@Stateless
	public class FormOfPaymentService {
		

 		@PersistenceContext(unitName="examples-769-EJB")
 		EntityManager em;
 		
 		public void createFormOfPayment() {
			FormOfPayment formOfPayment;
 			//TODO: 
 			em.persist(formOfPayment);
 		}
 		
 		public FormOfPayment getFormOfPaymentById(Long id) {
 			return em.find(FormOfPayment.class, id);
 		}
 
 	}

}
