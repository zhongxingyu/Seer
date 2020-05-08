 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 
 public class ConfirmInvoice extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && process.isResponsibleForUnit(loggedPerson);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
 	final Person loggedPerson = getLoggedPerson();
	return super.isAvailable(process) && loggedPerson != null && !process.getUnconfirmedInvoices(loggedPerson).isEmpty();
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	Person person = (Person) objects[0];
 	process.confirmInvoiceBy(person);
     }
 
 }
