 package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities;
 
 import java.util.List;
 
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitItemBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 
 public class GenericAssignPayingUnitToItem<T extends PaymentProcess> extends AbstractActivity<T> {
     @Override
     protected boolean isAccessible(T process) {
 	final Person loggedPerson = getLoggedPerson();
 	return loggedPerson != null && loggedPerson == process.getRequestor() || userHasRole(RoleType.ACQUISITION_CENTRAL);
     }
 
     @Override
     protected boolean isAvailable(T process) {
	return (isCurrentUserProcessOwner(process) && process.isInGenesis())
 		|| ((process instanceof SimplifiedProcedureProcess)
 			&& ((SimplifiedProcedureProcess) process).getProcessClassification() == ProcessClassification.CT75000
 			&& userHasRole(RoleType.ACQUISITION_CENTRAL) && process.isAuthorized());
     }
 
     @Override
     protected void process(T process, Object... objects) {
 	RequestItem item = (RequestItem) objects[0];
 	List<UnitItemBean> beans = (List<UnitItemBean>) objects[1];
 
 	for (; !item.getUnitItems().isEmpty(); item.getUnitItems().get(0).delete())
 	    ;
 
 	for (UnitItemBean bean : beans) {
 	    if (bean.getAssigned()) {
 		item.createUnitItem(bean.getUnit(), bean.getShareValue());
 	    }
 	}
     }
 }
