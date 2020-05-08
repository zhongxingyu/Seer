 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 
 public class ReceiveInvoice extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(AcquisitionProcess process) {
	return userHasRole(RoleType.ACCOUNTABILITY);
     }
 
     @Override
     protected boolean isAvailable(AcquisitionProcess process) {
 	return process.isAcquisitionProcessed(); 
     }
 
     @Override
     protected void process(AcquisitionProcess process, Object... objects) {
 	
 	final AcquisitionRequest acquisitionRequest = process.getAcquisitionRequest();
 
 	String filename = (String) objects[0];
 	byte[] bytes = (byte[]) objects[1];
 	String invoiceNumber = (String) objects[2];
 	DateTime invoiceDate = (DateTime) objects[3];
 	
 	acquisitionRequest.receiveInvoice(filename, bytes, invoiceNumber, invoiceDate);
 	new AcquisitionProcessState(process, AcquisitionProcessStateType.INVOICE_RECEIVED);
     }
 
 }
