 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 
 public class EditAcquisitionRequestItemRealValues extends GenericAcquisitionProcessActivity {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	return userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
	return super.isAvailable(process) && process.getAcquisitionProcessState().isInvoiceReceived()
		&& !process.getAcquisitionRequest().hasAtLeastOneConfirmation();
     }
 
     @Override
     protected void process(RegularAcquisitionProcess process, Object... objects) {
 	AcquisitionRequestItemBean acquisitionRequestItemBean = (AcquisitionRequestItemBean) objects[0];
 	acquisitionRequestItemBean.getItem().editRealValues(acquisitionRequestItemBean);
     }
 
 }
