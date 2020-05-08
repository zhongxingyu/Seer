 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 public class PaymentProcessInvoice extends PaymentProcessInvoice_Base {
 
     public PaymentProcessInvoice() {
 	super();
     }
 
     @Override
     public void delete() {
 	getUnitItems().clear();
 	getRequestItems().clear();
 	getProjectFinancers().clear();
 	getFinancers().clear();
	super.delete();
     }
 }
