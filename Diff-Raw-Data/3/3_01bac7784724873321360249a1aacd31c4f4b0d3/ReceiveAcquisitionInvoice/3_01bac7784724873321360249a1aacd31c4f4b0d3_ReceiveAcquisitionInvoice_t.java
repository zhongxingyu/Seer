 package pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.activities;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.fenixWebFramework.security.UserView;
 
 public class ReceiveAcquisitionInvoice extends AbstractActivity<AfterTheFactAcquisitionProcess> {
 
     @Override
     protected boolean isAccessible(final AfterTheFactAcquisitionProcess process) {
 	final User user = UserView.getUser();
 	final Person person = user == null ? null : user.getPerson();
	return person != null && (person.hasRoleType(RoleType.ACQUISITION_CENTRAL)
		|| person.hasRoleType(RoleType.ACQUISITION_CENTRAL_MANAGER));
     }
 
     @Override
     protected boolean isAvailable(final AfterTheFactAcquisitionProcess process) {
 	return true;
     }
 
     @Override
     protected void process(AfterTheFactAcquisitionProcess process, Object... objects) {
 	final AcquisitionAfterTheFact acquisitionAfterTheFact = process.getAcquisitionAfterTheFact();
 
 	final String filename = (String) objects[0];
 	final byte[] bytes = (byte[]) objects[1];
 	final String invoiceNumber = (String) objects[2];
 	final LocalDate invoiceDate = (LocalDate) objects[3];
 
 	acquisitionAfterTheFact.receiveInvoice(filename, bytes, invoiceNumber, invoiceDate);
     }
 
 }
