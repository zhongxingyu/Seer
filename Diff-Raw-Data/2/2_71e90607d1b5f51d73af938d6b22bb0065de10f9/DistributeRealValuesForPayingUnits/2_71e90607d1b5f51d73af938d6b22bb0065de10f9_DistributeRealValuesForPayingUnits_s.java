 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AbstractDistributeRealValuesForPayingUnits;
 
 public class DistributeRealValuesForPayingUnits extends AbstractDistributeRealValuesForPayingUnits<RegularAcquisitionProcess> {
 
     @Override
     protected boolean isAccessible(RegularAcquisitionProcess process) {
 	return userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(RegularAcquisitionProcess process) {
	return  isCurrentUserProcessOwner(process) && process.getAcquisitionProcessState().isInvoiceReceived() && !process.getAcquisitionRequest().hasAtLeastOneConfirmation();
     }
 
 }
