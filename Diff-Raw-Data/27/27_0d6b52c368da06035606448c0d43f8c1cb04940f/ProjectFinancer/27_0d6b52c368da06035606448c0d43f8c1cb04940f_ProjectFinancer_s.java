 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import myorg.domain.util.Money;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
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
 	    for (String allocationId : strings) {
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
 	    setEffectiveProjectFundAllocationId(strings);
 	} else {
 	    strings.add(effectiveProjectFundAllocationId);
 	    setEffectiveProjectFundAllocationId(strings);
 	}
 
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
 	res.add(AccountingUnit.readAccountingUnitByUnitName("10"));
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
 }
