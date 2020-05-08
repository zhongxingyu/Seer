 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 
 public class EditAcquisitionRequestItemRealValues extends
 	WorkflowActivity<RegularAcquisitionProcess, EditAcquisitionRequestItemRealValuesActivityInformation> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	return isUserProcessOwner(process, user) && user.getExpenditurePerson().hasRoleType(RoleType.ACQUISITION_CENTRAL)
 		&& process.getAcquisitionProcessState().isInvoiceReceived();
     }
 
     @Override
     protected void process(EditAcquisitionRequestItemRealValuesActivityInformation activityInformation) {
 	activityInformation.getItem().editRealValues(activityInformation.getRealQuantity(),
 		activityInformation.getRealUnitValue(), activityInformation.getShipment(), activityInformation.getRealVatValue());
     }
 
     @Override
     public ActivityInformation<RegularAcquisitionProcess> getActivityInformation(RegularAcquisitionProcess process) {
 	return new EditAcquisitionRequestItemRealValuesActivityInformation(process, this);
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "label." + getClass().getName());
     }
 
     @Override
     public String getUsedBundle() {
 	return "resources/AcquisitionResources";
     }
 
     @Override
     public boolean isDefaultInputInterfaceUsed() {
 	return false;
     }
 }
