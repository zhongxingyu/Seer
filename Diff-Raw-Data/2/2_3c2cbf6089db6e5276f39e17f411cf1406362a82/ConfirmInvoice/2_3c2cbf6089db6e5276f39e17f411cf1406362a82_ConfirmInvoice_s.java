 package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;
 
 import java.util.Set;
 
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.util.BundleUtil;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem._development.ExternalIntegration;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.UnitItem;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public class ConfirmInvoice extends WorkflowActivity<RegularAcquisitionProcess, ActivityInformation<RegularAcquisitionProcess>> {
 
     @Override
     public boolean isActive(RegularAcquisitionProcess process, User user) {
 	Person person = user.getExpenditurePerson();
 	return isUserProcessOwner(process, user)
 		&& person != null
 		&& process.isActive()
 		&& !process.isInvoiceReceived()
 		&& !process.getUnconfirmedInvoices(person).isEmpty()
 		&& process.isResponsibleForUnit(person)
		&& (!ExpenditureTrackingSystem.isInvoiceAllowedToStartAcquisitionProcess()
 			|| process.isPendingInvoiceConfirmation());
     }
 
     @Override
     protected void process(ActivityInformation<RegularAcquisitionProcess> activityInformation) {
 	final RegularAcquisitionProcess process = activityInformation.getProcess();
 	process.confirmInvoiceBy(UserView.getCurrentUser().getExpenditurePerson());
 
 	if (ExternalIntegration.isActive()) {
 	    process.createFundAllocationRequest(true);
 	}
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
     public boolean isConfirmationNeeded(RegularAcquisitionProcess process) {
 	User currentUser = UserView.getCurrentUser();
 	Set<AcquisitionInvoice> unconfirmedInvoices = process.getUnconfirmedInvoices(currentUser.getExpenditurePerson());
 	for (AcquisitionInvoice unconfirmedInvoice : unconfirmedInvoices) {
 	    if (!StringUtils.isEmpty(unconfirmedInvoice.getConfirmationReport())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public String getLocalizedConfirmationMessage(RegularAcquisitionProcess process) {
 	StringBuilder builder = new StringBuilder();
 	User currentUser = UserView.getCurrentUser();
 	Set<AcquisitionInvoice> unconfirmedInvoices = process.getUnconfirmedInvoices(currentUser.getExpenditurePerson());
 	final int invoiceCount = unconfirmedInvoices.size();
 	int column = 1;
 	if (invoiceCount > 1) {
 	    builder.append("<table>");
 	}
 	for (AcquisitionInvoice unconfirmedInvoice : unconfirmedInvoices) {
 	    if (invoiceCount > 1) {
 		if (column == 1) {
 		    builder.append("<tr>");
 		}
 		builder.append("<td>");
 	    }
 	    builder.append(BundleUtil.getFormattedStringFromResourceBundle(getUsedBundle(), "activity.confirmation."
 		    + getClass().getName(), unconfirmedInvoice.getInvoiceNumber(), unconfirmedInvoice.getConfirmationReport()));
 	    if (invoiceCount > 1) {
 		builder.append("</td>");
 		if (column == 2) {
 		    builder.append("</tr>");
 		    column = 0;
 		}
 	    }
 
 	    column++;
 	}
 	if (invoiceCount > 1) {
 	    builder.append("</table>");
 	}
 
 	return builder.toString();
     }
 
     @Override
     public boolean isUserAwarenessNeeded(final RegularAcquisitionProcess process, final User user) {
 	final Person person = user.getExpenditurePerson();
 	if (person.hasAnyValidAuthorization()) {
 	    for (final RequestItem requestItem : process.getRequest().getRequestItemsSet()) {
 		for (final UnitItem unitItem : requestItem.getUnitItemsSet()) {
 		    final Unit unit = unitItem.getUnit();
 		    for (final PaymentProcessInvoice invoice : requestItem.getInvoicesFilesSet()) {
 			if (!unitItem.getConfirmedInvoices().contains(invoice) && unit.isDirectResponsible(person)) {
 			    return true;
 			}
 		    }
 		}
 	    }
 	}
 	return false;
     }
 
 }
