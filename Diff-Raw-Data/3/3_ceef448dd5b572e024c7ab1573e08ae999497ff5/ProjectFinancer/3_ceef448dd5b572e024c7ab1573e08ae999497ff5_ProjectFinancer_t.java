 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.utl.ist.fenix.tools.util.Strings;
 
 public class ProjectFinancer extends ProjectFinancer_Base {
 
     protected ProjectFinancer() {
 	super();
     }
 
     protected ProjectFinancer(final RequestWithPayment acquisitionRequest, final Unit unit) {
 	this();
 	if (acquisitionRequest == null || unit == null) {
 	    throw new DomainException("error.financer.wrong.initial.arguments");
 	}
 	if (acquisitionRequest.hasPayingUnit(unit)) {
 	    throw new DomainException("error.financer.acquisition.request.already.has.paying.unit");
 	}
 
 	setFundedRequest(acquisitionRequest);
 	setUnit(unit);
 	setAccountingUnit(unit.getAccountingUnit());
     }
 
     public ProjectFinancer(final RequestWithPayment acquisitionRequest, final Project project) {
 	this(acquisitionRequest, (Unit) project);
     }
 
     public ProjectFinancer(final RequestWithPayment acquisitionRequest, final SubProject subProject) {
 	this(acquisitionRequest, (Unit) subProject);
     }
 
     @Override
     public String getFundAllocationIds() {
 	final String financerString = super.getFundAllocationIds();
 	return financerString + " " + getAllocationIds(getProjectFundAllocationId(), "financer.label.allocation.id.prefix.mgp");
     }
 
     @Override
     public String getEffectiveFundAllocationIds() {
 	final StringBuilder financerString = new StringBuilder(super.getEffectiveFundAllocationIds());
 	Strings strings = getEffectiveProjectFundAllocationId();
 	if (strings != null && !strings.isEmpty()) {
 	    for (String allocationId : strings.getUnmodifiableList()) {
 		financerString.append(getAllocationIds(allocationId, "financer.label.allocation.id.prefix.mgp"));
 		financerString.append(' ');
 	    }
 	}
 	return financerString.toString();
     }
 
     @Override
     public boolean hasAllocatedFundsForAllProject() {
 	return (getProjectFundAllocationId() != null && !getProjectFundAllocationId().isEmpty())
 		|| (getAmountAllocated().equals(Money.ZERO) && hasAnyOtherFinancerIsAllocated());
     }
 
     private boolean hasAnyOtherFinancerIsAllocated() {
 	for (Financer financer : getFundedRequest().getFinancers()) {
 	    if (financer != this && financer.isProjectFinancer()) {
 		ProjectFinancer projectFinancer = (ProjectFinancer) financer;
 		if ((projectFinancer.getProjectFundAllocationId() != null && !projectFinancer.getProjectFundAllocationId()
 			.isEmpty())) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     @Override
     public boolean hasAllocatedFundsPermanentlyForAllProjectFinancers() {
 	List<PaymentProcessInvoice> allocatedInvoicesInProject = getAllocatedInvoicesInProject();
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!allocatedInvoicesInProject.containsAll(unitItem.getConfirmedInvoices())) {
 		return false;
 	    }
 	}
 	return getEffectiveProjectFundAllocationId() != null && !getEffectiveProjectFundAllocationId().isEmpty();
     }
 
     @Override
     public boolean hasAllocatedFundsPermanentlyForAnyProjectFinancers() {
 	List<PaymentProcessInvoice> allocatedInvoicesInProject = getAllocatedInvoicesInProject();
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!allocatedInvoicesInProject.isEmpty() && allocatedInvoicesInProject.containsAll(unitItem.getConfirmedInvoices())) {
 		return true;
 	    }
 	}
 	return getEffectiveProjectFundAllocationId() != null && !getEffectiveProjectFundAllocationId().isEmpty();
     }
 
     @Override
     public boolean isProjectFinancer() {
 	return true;
     }
 
     public void addEffectiveProjectFundAllocationId(String effectiveProjectFundAllocationId) {
 	if (StringUtils.isEmpty(effectiveProjectFundAllocationId)) {
 	    throw new DomainException("acquisitionProcess.message.exception.effectiveFundAllocationCannotBeNull");
 	}
 	Strings strings = getEffectiveProjectFundAllocationId();
 	if (strings == null) {
 	    strings = new Strings(effectiveProjectFundAllocationId);
 	}
 	if (!strings.contains(effectiveProjectFundAllocationId)) {
 	    strings = new Strings(strings, effectiveProjectFundAllocationId);
 	}
 	setEffectiveProjectFundAllocationId(strings);
 
 	allocateInvoicesInProject();
     }
 
     private void allocateInvoicesInProject() {
 	getAllocatedInvoicesInProject().clear();
 	Set<PaymentProcessInvoice> invoices = new HashSet<PaymentProcessInvoice>();
 	for (UnitItem unitItem : getUnitItems()) {
 	    invoices.addAll(unitItem.getConfirmedInvoices());
 	}
 	getAllocatedInvoicesInProject().addAll(invoices);
     }
 
     @Override
     public boolean isAccountingEmployee(Person person) {
 	return getUnit().isAccountingEmployee(person);
     }
 
     @Override
     public boolean isAccountingEmployeeForOnePossibleUnit(Person person) {
 	return false;
     }
 
     @Override
     public boolean hasFundAllocationId() {
 	return super.hasFundAllocationId() || getProjectFundAllocationId() != null;
     }
 
     public Set<AccountingUnit> getAccountingUnits() {
 	Set<AccountingUnit> res = new HashSet<AccountingUnit>();
 	res.add(getUnit().getAccountingUnit());
 	final AccountingUnit accountingUnit = AccountingUnit.readAccountingUnitByUnitName("10");
 	if (accountingUnit != null) {
 	    res.add(accountingUnit);
 	}
 	return res;
     }
 
     @Override
     public boolean isProjectAccountingEmployeeForOnePossibleUnit(final Person person) {
 	for (final AccountingUnit accountingUnit : getAccountingUnits()) {
 	    if (accountingUnit.hasPeople(person)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Override
     public boolean isProjectAccountingEmployee(Person person) {
	final AccountingUnit accountingUnit = getAccountingUnit();
	return accountingUnit == null ? false : accountingUnit.hasProjectAccountants(person);
     }
 
     @Override
     public boolean isFundAllocationPresent() {
 	return getProjectFundAllocationId() != null || super.isFundAllocationPresent();
     }
 
     @Override
     public boolean isEffectiveFundAllocationPresent() {
 	return getEffectiveProjectFundAllocationId() != null || super.isEffectiveFundAllocationPresent();
     }
 
     public void resetEffectiveFundAllocation() {
 	setEffectiveProjectFundAllocationId(null);
 	getAllocatedInvoicesInProject().clear();
     }
 
     @Override
     public boolean hasAllInvoicesAllocatedInProject() {
 	List<PaymentProcessInvoice> allocatedInvoices = getAllocatedInvoicesInProject();
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (!allocatedInvoices.containsAll(unitItem.getConfirmedInvoices())) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     @Override
     public void createFundAllocationRequest(final boolean isFinalFundAllocation) {
 	for (final UnitItem unitItem : getUnitItemsSet()) {
 	    final RequestWithPayment fundedRequest = getFundedRequest();
 	    final PaymentProcess process = fundedRequest.getProcess();
 	    final Unit unit = getUnit();
 	    final AccountingUnit accountingUnit = unit.getAccountingUnit();
 
 	    new ProjectAcquisitionFundAllocationRequest(unitItem, process.getProcessNumber(), process, unit.getUnitNumber(),
 		    accountingUnit.getName(), getAmountAllocated(), Boolean.valueOf(isFinalFundAllocation));
 	}
     }
 
     public String getProjectFundAllocationIdsForAllUnitItems() {
 	final StringBuilder result = new StringBuilder();
 	for (final UnitItem unitItem : getUnitItemsSet()) {
 	    final String projectFundAllocationId = unitItem.getProjectFundAllocationId();
 	    if (projectFundAllocationId == null) {
 		return null;
 	    }
 	    result.append(", ");
 	    result.append(projectFundAllocationId);
 	}
 	return result.toString();
     }
 
 }
