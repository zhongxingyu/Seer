 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessState;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestDocument;
 
 public class CreateAcquisitionRequest extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(AcquisitionProcess process) {
 	return userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(AcquisitionProcess process) {
	return process.isProcessInState(AcquisitionProcessStateType.FUNDS_ALLOCATED_TO_SERVICE_PROVIDER);
     }
 
     @Override
     protected void process(AcquisitionProcess process, Object... objects) {
 	new AcquisitionRequestDocument(process.getAcquisitionRequest());
 	new AcquisitionProcessState(process, AcquisitionProcessStateType.ACQUISITION_PROCESSED);
     }
 
 }
