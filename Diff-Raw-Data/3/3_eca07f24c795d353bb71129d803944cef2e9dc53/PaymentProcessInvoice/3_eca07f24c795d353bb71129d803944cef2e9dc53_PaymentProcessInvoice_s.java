 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 public class PaymentProcessInvoice extends PaymentProcessInvoice_Base {
 
     public PaymentProcessInvoice() {
 	super();
     }
 
     @Override
     public void delete() {
	super.delete();
 	getUnitItems().clear();
 	getRequestItems().clear();
 	getProjectFinancers().clear();
 	getFinancers().clear();
     }
 }
