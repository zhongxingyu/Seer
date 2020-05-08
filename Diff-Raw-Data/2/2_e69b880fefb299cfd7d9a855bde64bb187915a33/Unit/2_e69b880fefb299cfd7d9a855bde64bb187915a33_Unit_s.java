 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Acquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.AuthorizationLog;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public class Unit extends Unit_Base {
 
     public static final Comparator<Unit> COMPARATOR_BY_PRESENTATION_NAME = new Comparator<Unit>() {
 
 	public int compare(final Unit unit1, Unit unit2) {
 	    return unit1.getPresentationName().compareTo(unit2.getPresentationName());
 	}
 
     };
 
     public Unit() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	final ExpenditureTrackingSystem expenditureTrackingSystem = ExpenditureTrackingSystem.getInstance();
 	setExpenditureTrackingSystem(expenditureTrackingSystem);
     }
 
     public Unit(final Unit parentUnit, final String name) {
 	this();
 	setName(name);
 	setParentUnit(parentUnit);
     }
 
     @Override
     public void setParentUnit(final Unit parentUnit) {
 	if (parentUnit == null) {
 	    setExpenditureTrackingSystemFromTopLevelUnit(ExpenditureTrackingSystem.getInstance());
 	}
 	super.setParentUnit(parentUnit);
     }
 
     @Service
     public static Unit createNewUnit(final CreateUnitBean createUnitBean) {
 	if (createUnitBean.getCostCenter() != null) {
 	    return new CostCenter(createUnitBean.getParentUnit(), createUnitBean.getName(), createUnitBean.getCostCenter());
 	}
 	if (createUnitBean.getProjectCode() != null) {
 	    return new Project(createUnitBean.getParentUnit(), createUnitBean.getName(), createUnitBean.getProjectCode());
 	}
 	return new Unit(createUnitBean.getParentUnit(), createUnitBean.getName());
     }
 
     @Service
     public void delete() {
 	if (!getAuthorizationsSet().isEmpty()) {
 	    throw new DomainException("error.cannot.delete.units.which.have.or.had.authorizations");
 	}
 	if (hasAnyFinancedItems()) {
 	    throw new DomainException("error.cannot.delete.units.which.have.or.had.financedItems");
 	}
 
 	for (final Unit unit : getSubUnitsSet()) {
 	    unit.delete();
 	}
 	removeExpenditureTrackingSystemFromTopLevelUnit();
 	removeParentUnit();
 	removeExpenditureTrackingSystem();
 	Transaction.deleteObject(this);
     }
 
     public void findAcquisitionProcessesPendingAuthorization(final Set<AcquisitionProcess> result, final boolean recurseSubUnits) {
 	if (recurseSubUnits) {
 	    for (final Unit unit : getSubUnitsSet()) {
 		unit.findAcquisitionProcessesPendingAuthorization(result, recurseSubUnits);
 	    }
 	}
     }
 
     public Money getTotalAllocated() {
 	Money result = Money.ZERO;
 	for (final Acquisition acquisition : ExpenditureTrackingSystem.getInstance().getAcquisitionsSet()) {
 	    if (acquisition instanceof AcquisitionRequest) {
 		final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) acquisition;
 		AcquisitionProcess acquisitionProcess = acquisitionRequest.getAcquisitionProcess();
 		if (acquisitionProcess.isAllocatedToUnit(this)) {
 		    result = result.add(acquisitionRequest.getAcquisitionProcess().getAmountAllocatedToUnit(this));
 		}
 	    }
 	}
 	for (final Unit unit : getSubUnitsSet()) {
 	    result = result.add(unit.getTotalAllocated());
 	}
 	return result;
     }
 
     public static Unit findUnitByCostCenter(final String costCenter) {
 	for (final Unit unit : ExpenditureTrackingSystem.getInstance().getTopLevelUnitsSet()) {
 	    final Unit result = unit.findByCostCenter(costCenter);
 	    if (result != null) {
 		return result;
 	    }
 	}
 	return null;
     }
 
     protected Unit findByCostCenter(final String costCenter) {
 	for (final Unit unit : getSubUnitsSet()) {
 	    final Unit result = unit.findByCostCenter(costCenter);
 	    if (result != null) {
 		return result;
 	    }
 	}
 	return null;
     }
 
     public boolean isResponsible(Person person) {
 	for (Authorization authorization : getAuthorizationsSet()) {
	    if (authorization.isValid() && isSubUnit(authorization.getUnit())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isResponsible(final Person person, final Money amount) {
 	for (Authorization authorization : person.getAuthorizationsSet()) {
 	    if (authorization.isValid() && authorization.getMaxAmount().isGreaterThan(amount)
 		    && isSubUnit(authorization.getUnit())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public Authorization findClosestAuthorization(final Person person, final Money money) {
 	for (final Authorization authorization : getAuthorizationsSet()) {
 	    if (authorization.getPerson() == person && authorization.isValid()) {
 		if (authorization.getMaxAmount().isGreaterThanOrEqual(money)) {
 		    return authorization;
 		}
 	    }
 	}
 	return hasParentUnit() ? getParentUnit().findClosestAuthorization(person, money) : null;
     }
 
     public String getPresentationName() {
 	return getName();
     }
 
     public String getType() {
 	return ResourceBundle.getBundle("resources/ExpenditureOrganizationResources", Language.getLocale()).getString(
 		"label." + getClass().getSimpleName());
     }
 
     public String getShortIdentifier() {
 	return "";
     }
 
     public boolean isSubUnit(final Unit unit) {
 	return unit != null && (this == unit || isSubUnitOfParent(unit));
     }
 
     protected boolean isSubUnitOfParent(final Unit unit) {
 	return hasParentUnit() && getParentUnit().isSubUnit(unit);
     }
 
     public boolean isAccountingEmployee(final Person person) {
 	final Unit parentUnit = getParentUnit();
 	return parentUnit != null && parentUnit.isAccountingEmployee(person);
     }
 
     public Financer finance(final RequestWithPayment acquisitionRequest) {
 	throw new Error("Units with no accounting cannot finance any acquisitions: " + getExternalId());
     }
 
     public boolean isProjectAccountingEmployee(Person person) {
 	final Unit parentUnit = getParentUnit();
 	return parentUnit != null && parentUnit.isProjectAccountingEmployee(person);
     }
 
     public CostCenter getCostCenterUnit() {
 	return getParentUnit() != null ? getParentUnit().getCostCenterUnit() : null;
     }
 
     public boolean hasResponsibleInSubUnits() {
 	for (Unit unit : getSubUnits()) {
 	    if (unit.hasResponsiblesInUnit()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasResponsiblesInUnit() {
 	return getAuthorizationsCount() > 0;
     }
 
     public boolean hasAuthorizationsFor(Person person) {
 	return hasAuthorizationsFor(person, null);
     }
 
     public boolean hasAuthorizationsFor(Person person, Money money) {
 	for (Authorization authorization : getAuthorizations()) {
 	    if (authorization.getPerson() == person
 		    && (money == null || authorization.getMaxAmount().isGreaterThanOrEqual(money))) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean hasAnyAuthorizationForAmount(Money money) {
 	for (Authorization authorization : getAuthorizations()) {
 	    if (authorization.getMaxAmount().isGreaterThanOrEqual(money)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isMostDirectAuthorization(Person person, Money money) {
 	return !hasAnyAuthorizations() ? hasParentUnit() && getParentUnit().isMostDirectAuthorization(person, money)
 		: hasAnyAuthorizationForAmount(money) ? hasAuthorizationsFor(person, money) : hasParentUnit()
 			&& getParentUnit().isMostDirectAuthorization(person, money);
     }
 
     public boolean isTreasuryMember(Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit();
 	if (accountingUnit == null) {
 	    final Unit parentUnit = getParentUnit();
 	    return parentUnit != null && parentUnit.isTreasuryMember(person);
 	}
 	return accountingUnit.getTreasuryMembersSet().contains(person);
     }
 
     public Set<PaymentProcess> getProcesses(PaymentProcessYear year) {
 	Set<PaymentProcess> processes = new HashSet<PaymentProcess>();
 	for (Financer financer : getFinancedItems()) {
 	    PaymentProcess process = financer.getFundedRequest().getProcess();
 	    if (year == null || process.getPaymentProcessYear() == year) {
 		processes.add(process);
 	    }
 	}
 	return processes;
     }
 
     public List<Unit> getAllSubUnits() {
 	List<Unit> result = new ArrayList<Unit>();
 	addAllSubUnits(result);
 	return result;
     }
 
     protected void addAllSubUnits(final List<Unit> result) {
 	for (final Unit unit : getSubUnits()) {
 	    result.add(unit);
 	    unit.addAllSubUnits(result);
 	}
     }
 
     @Override
     @Service
     public void removeUnit() {
         super.removeUnit();
     }
 
     @Override
     @Service
     public void setUnit(final module.organization.domain.Unit unit) {
         super.setUnit(unit);
     }
 
     public Set<AuthorizationLog> getSortedAuthorizationLogsSet() {
 	final Set<AuthorizationLog> authorizationLogs = new TreeSet<AuthorizationLog>(AuthorizationLog.COMPARATOR_BY_WHEN);
 	authorizationLogs.addAll(getAuthorizationLogsSet());
 	return authorizationLogs;
     }
 
 }
