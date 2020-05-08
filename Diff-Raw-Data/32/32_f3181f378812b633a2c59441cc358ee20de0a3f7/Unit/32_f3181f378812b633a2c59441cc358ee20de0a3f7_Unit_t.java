 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import module.organization.domain.Accountability;
 import module.organization.domain.Party;
 import module.organization.domain.PartyType;
 import module.organization.domain.UnitBean;
 import module.organizationIst.domain.IstAccountabilityType;
 import module.organizationIst.domain.IstPartyType;
 import module.workflow.util.ProcessEvaluator;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.AuthorizationLog;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.luceneIndexing.IndexableField;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.IndexDocument;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Indexable;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Searchable;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class Unit extends Unit_Base implements Indexable, Searchable {
 
     public static enum UnitIndexFields implements IndexableField {
 
 	NAME_INDEX("name"), NUMBER_INDEX("number");
 
 	private String fieldName;
 
 	private UnitIndexFields(String fieldName) {
 	    this.fieldName = fieldName;
 	}
 
 	@Override
 	public String getFieldName() {
 	    return fieldName;
 	}
 
     }
 
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
 	setDefaultRegeimIsCCP(Boolean.TRUE);
     }
 
     public Unit(final Unit parentUnit, final String name) {
 	this();
 	final String acronym = StringUtils.abbreviate(name, 5);
 	createRealUnit(this, parentUnit, IstPartyType.UNIT, acronym, name);
 
 	// TODO : After this object is refactored to retrieve the name and
 	// parent from the real unit,
 	// the following two lines may be deleted.
 	setName(name);
 	setParentUnit(parentUnit);
     }
 
     public void setName(final String name) {
 	getUnit().setPartyName(new MultiLanguageString(name));
     }
 
     public String getName() {
 	return getUnit().getPartyName().getContent();
     }
 
     protected static void createRealUnit(final Unit expenditureUnit, final Unit parentExpenditureUnit,
 	    final IstPartyType istPartyType, final String acronym, final String name) {
 	final UnitBean unitBean = new UnitBean();
 	unitBean.setParent(parentExpenditureUnit.getUnit());
 	unitBean.setAccountabilityType(IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType());
 	unitBean.setAcronym(acronym);
 	unitBean.setBegin(new LocalDate());
 	unitBean.setEnd(null);
 	unitBean.setName(new MultiLanguageString(name));
 	unitBean.setPartyType(PartyType.readBy(istPartyType.getType()));
 	final module.organization.domain.Unit createdUnit = unitBean.createUnit();
 	expenditureUnit.setUnit(createdUnit);
     }
 
     public static final Unit createRealUnit(final Unit parentExpenditureUnit, final IstPartyType istPartyType,
 	    final String acronym, final String name) {
 	final UnitBean unitBean = new UnitBean();
 	unitBean.setParent(parentExpenditureUnit.getUnit());
 	unitBean.setAccountabilityType(IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType());
 	unitBean.setAcronym(acronym);
 	unitBean.setBegin(new LocalDate());
 	unitBean.setEnd(null);
 	unitBean.setName(new MultiLanguageString(name));
 	unitBean.setPartyType(PartyType.readBy(istPartyType.getType()));
 	final module.organization.domain.Unit createdUnit = unitBean.createUnit();
 	return createdUnit.getExpenditureUnit();
     }
 
     public void setParentUnit(final Unit parentUnit) {
 	if (parentUnit == null) {
 	    setExpenditureTrackingSystemFromTopLevelUnit(ExpenditureTrackingSystem.getInstance());
 	    getUnit().closeAllParentAccountabilitiesByType(IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType());
 	} else {
 	    parentUnit.getUnit().addChild(getUnit(), IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType(),
 		    new LocalDate(), null);
 	}
     }
 
     private void deleteUnit() {
 	final module.organization.domain.Unit unit = getUnit();
 	removeUnit();
 	unit.delete();
     }
 
     @Service
     public static Unit createNewUnit(final CreateUnitBean createUnitBean) {
 	if (createUnitBean.getCostCenter() != null) {
 	    return new CostCenter(createUnitBean.getParentUnit(), createUnitBean.getName(), createUnitBean.getCostCenter());
 	}
 	if (createUnitBean.getProjectCode() != null) {
 	    final Unit unit = createRealUnit(createUnitBean.getParentUnit(), IstPartyType.PROJECT, createUnitBean
 		    .getProjectCode(), createUnitBean.getName());
 	    final Project project = (Project) unit;
 	    project.setName(createUnitBean.getName());
 	    project.setProjectCode(createUnitBean.getProjectCode());
 	    project.setParentUnit(createUnitBean.getParentUnit());
 	    return project;
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
 	if (getUnit().hasAnyParentAccountabilities()) {
 	    throw new DomainException("error.cannot.delete.units.which.have.subUnits");
 	}
 
 	removeExpenditureTrackingSystemFromTopLevelUnit();
 	deleteUnit();
 	removeExpenditureTrackingSystem();
 	deleteDomainObject();
     }
 
     public void findAcquisitionProcessesPendingAuthorization(final Set<AcquisitionProcess> result, final boolean recurseSubUnits) {
 	findAcquisitionProcessesPendingAuthorization(getUnit(), result, recurseSubUnits);
     }
 
     public static void findAcquisitionProcessesPendingAuthorization(final Party party, final Set<AcquisitionProcess> result,
 	    final boolean recurseSubUnits) {
 	if (recurseSubUnits) {
 	    for (final Accountability accountability : party.getChildAccountabilitiesSet()) {
 		final Party child = accountability.getChild();
 		if (child.isUnit()) {
 		    final module.organization.domain.Unit childUnit = (module.organization.domain.Unit) child;
 		    if (childUnit.hasExpenditureUnit()) {
 			final Unit expenditureUnit = childUnit.getExpenditureUnit();
 			if (expenditureUnit instanceof CostCenter || expenditureUnit instanceof Project) {
 			    expenditureUnit.findAcquisitionProcessesPendingAuthorization(result, recurseSubUnits);
 			}
 		    }
 		    findAcquisitionProcessesPendingAuthorization(child, result, recurseSubUnits);
 		}
 	    }
 	}
     }
 
     public static Unit findUnitByCostCenter(final String costCenter) {
 	final Party party = Party.findPartyByPartyTypeAndAcronymForAccountabilityTypeLink((Set) MyOrg.getInstance()
 		.getTopUnitsSet(), IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType(), PartyType
 		.readBy(IstPartyType.COST_CENTER.getType()), "CC. " + costCenter);
 	return party == null || !party.isUnit() ? null : ((module.organization.domain.Unit) party).getExpenditureUnit();
     }
 
     public boolean isCurrentUserResponsibleForUnit() {
 	return isResponsible(getUnit(), Person.getLoggedPerson());
     }
 
     public boolean isResponsible(Person person) {
 	return isResponsible(getUnit(), person);
     }
 
     public static boolean isResponsible(final Party party, final Person person) {
 	if (party.isUnit()) {
 
 	    if (!person.hasAnyAuthorizations()) {
 		return false;
 	    }
 	    final module.organization.domain.Unit unit = (module.organization.domain.Unit) party;
 	    LocalDate today = new LocalDate();
 	    if (unit.hasExpenditureUnit()) {
 		for (Authorization authorization : unit.getExpenditureUnit().getAuthorizationsSet()) {
 		    if (authorization.isValidFor(today) && authorization.getPerson() == person) {
 			return true;
 		    }
 		}
 	    }
 
 	    for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		    final Party parent = accountability.getParent();
 		    if (isResponsible(accountability.getParent(), person)) {
 			return true;
 		    }
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isResponsible(final Person person, final Money amount) {
 	for (Authorization authorization : person.getAuthorizationsSet()) {
 	    if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(amount)
 		    && isSubUnit(authorization.getUnit())) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public boolean isDirectResponsible(final Person person) {
 	return isDirectResponsible(person, Money.ZERO);
     }
 
     public boolean isDirectResponsible(final Person person, final Money amount) {
 	boolean hasSomeOtherValidAuthorization = false;
 	for (final Authorization authorization : getAuthorizationsSet()) {
 	    if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(amount)) {
 		if (authorization.getPerson() == person) {
 		    return true;
 		}
 		hasSomeOtherValidAuthorization = true;
 	    }
 	}
 	if (hasSomeOtherValidAuthorization) {
 	    return false;
 	}
 	final Unit parentUnit = getParentUnit();
 	return parentUnit != null && parentUnit.isDirectResponsible(person, amount);
     }
 
     public Authorization findClosestAuthorization(final Person person, final Money money) {
 	return findClosestAuthorization(getUnit(), person, money);
     }
 
     public static Authorization findClosestAuthorization(final Party party, final Person person, final Money money) {
 	if (party.isUnit()) {
 	    final module.organization.domain.Unit unit = (module.organization.domain.Unit) party;
 	    if (unit.hasExpenditureUnit()) {
 		for (final Authorization authorization : unit.getExpenditureUnit().getAuthorizationsSet()) {
 		    if (authorization.getPerson() == person && authorization.isValid()) {
 			if (authorization.getMaxAmount().isGreaterThanOrEqual(money)) {
 			    return authorization;
 			}
 		    }
 		}
 	    }
 
 	    for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		    final Party parent = accountability.getParent();
 		    final Authorization authorization = findClosestAuthorization(accountability.getParent(), person, money);
 		    if (authorization != null) {
 			return authorization;
 		    }
 		}
 	    }
 	}
 	return null;
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
 	return isSubUnit(getUnit(), unit);
     }
 
     public static boolean isSubUnit(final Party party, final Unit unit) {
 	if (party == null || unit == null || !party.isUnit()) {
 	    return false;
 	}
 	final module.organization.domain.Unit parentUnit = (module.organization.domain.Unit) party;
 	if (unit == parentUnit.getExpenditureUnit()) {
 	    return true;
 	}
 	for (final Accountability accountability : parentUnit.getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party parent = accountability.getParent();
 		if (isSubUnit(parent, unit)) {
 		    return true;
 		}
 	    }
 	}
 	return false;
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
 	for (final Accountability accountability : getUnit().getChildAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party child = accountability.getChild();
 		if (child.isUnit()) {
 		    final module.organization.domain.Unit unit = (module.organization.domain.Unit) child;
 		    if (unit.hasExpenditureUnit() && unit.getExpenditureUnit().hasResponsiblesInUnit()) {
 			return true;
 		    }
 		}
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
 
    public boolean hasAuthorizationsFor(final Person person, final Money money) {
	for (final Authorization authorization : getAuthorizations()) {
 	    if (authorization.getPerson() == person
 		    && (money == null || authorization.getMaxAmount().isGreaterThanOrEqual(money))) {
 		return true;
 	    }
 	}
 	return false;
     }
 
    public boolean hasActiveAuthorizationsFor(final Person person, final Money money) {
	for (final Authorization authorization : getAuthorizations()) {
	    if (authorization.isValid() && authorization.getPerson() == person
		    && (money == null || authorization.getMaxAmount().isGreaterThanOrEqual(money))) {
		return true;
	    }
	}
	return false;
    }

    public boolean hasAnyAuthorizationForAmount(final Money money) {
	for (final Authorization authorization : getAuthorizations()) {
 	    if (authorization.getMaxAmount().isGreaterThanOrEqual(money)) {
 		return true;
 	    }
 	}
 	return false;
     }
 
    public boolean hasAnyActiveAuthorizationForAmount(final Money money) {
	for (final Authorization authorization : getAuthorizations()) {
	    if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(money)) {
		return true;
	    }
	}
	return false;
    }

     public boolean isMostDirectAuthorization(Person person, Money money) {
 	return !hasAnyAuthorizations() ? hasParentUnit() && getParentUnit().isMostDirectAuthorization(person, money)
		: hasAnyActiveAuthorizationForAmount(money) ? hasActiveAuthorizationsFor(person, money) : hasParentUnit()
 			&& getParentUnit().isMostDirectAuthorization(person, money);
     }
 
     public Unit getParentUnit() {
 	return getParentUnit(getUnit());
     }
 
     private static Unit getParentUnit(final module.organization.domain.Unit unit) {
 	for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party parent = accountability.getParent();
 		if (parent.isUnit()) {
 		    final module.organization.domain.Unit parentUnit = (module.organization.domain.Unit) parent;
 		    if (parentUnit.hasExpenditureUnit()) {
 			return parentUnit.getExpenditureUnit();
 		    }
 		    final Unit result = getParentUnit(parentUnit);
 		    if (result != null) {
 			return result;
 		    }
 		}
 	    }
 	}
 	return null;
     }
 
     public boolean hasParentUnit() {
 	return hasParentUnit(getUnit());
     }
 
     private static boolean hasParentUnit(final module.organization.domain.Unit unit) {
 	for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
 	    if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		final Party parent = accountability.getParent();
 		if (parent.isUnit()) {
 		    final module.organization.domain.Unit parentUnit = (module.organization.domain.Unit) parent;
 		    if (parentUnit.hasExpenditureUnit()) {
 			return true;
 		    }
 		    final boolean result = hasParentUnit(parentUnit);
 		    if (result) {
 			return true;
 		    }
 		}
 	    }
 	}
 	return false;
     }
 
     public boolean isTreasuryMember(Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit(getUnit(), person);
 	return accountingUnit != null && accountingUnit.getTreasuryMembersSet().contains(person);
     }
 
     public static AccountingUnit getAccountingUnit(final Party party, final Person person) {
 	if (party.isUnit()) {
 	    final module.organization.domain.Unit unit = (module.organization.domain.Unit) party;
 	    if (unit.hasExpenditureUnit()) {
 		final Unit expenditureUnit = unit.getExpenditureUnit();
 		if (expenditureUnit.hasAccountingUnit()) {
 		    return expenditureUnit.getAccountingUnit();
 		}
 	    }
 	    for (final Accountability accountability : party.getParentAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		    final AccountingUnit accountingUnit = getAccountingUnit(accountability.getParent(), person);
 		    if (accountingUnit != null) {
 			return accountingUnit;
 		    }
 		}
 	    }
 	}
 	return null;
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
 	addAllSubUnits(result, getUnit());
     }
 
     private static void addAllSubUnits(final List<Unit> result, final Party party) {
 	if (party.isUnit()) {
 	    final module.organization.domain.Unit unit = (module.organization.domain.Unit) party;
 	    if (unit.hasExpenditureUnit()) {
 		result.add(unit.getExpenditureUnit());
 	    }
 
 	    for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
 		if (accountability.getAccountabilityType() == IstAccountabilityType.ORGANIZATIONAL.readAccountabilityType()) {
 		    final Party child = accountability.getChild();
 		    addAllSubUnits(result, child);
 		}
 	    }
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
 
     public SortedSet<Authorization> getSortedAuthorizationsSet() {
 	final SortedSet<Authorization> authorizations = new TreeSet<Authorization>(Authorization.COMPARATOR_BY_NAME_AND_DATE);
 	authorizations.addAll(getAuthorizationsSet());
 	return authorizations;
     }
 
     public Set<Unit> getSubUnitsSet() {
 	final Set<Unit> result = new HashSet<Unit>();
 	getSubUnitsSet(result, getUnit());
 	return result;
     }
 
     public static void getSubUnitsSet(final Set<Unit> result, final Party party) {
 	for (final Accountability accountability : party.getChildAccountabilitiesSet()) {
 	    final Party child = accountability.getChild();
 	    if (child.isUnit()) {
 		final module.organization.domain.Unit unit = (module.organization.domain.Unit) child;
 		if (unit.hasExpenditureUnit()) {
 		    result.add(unit.getExpenditureUnit());
 		} else {
 		    getSubUnitsSet(result, unit);
 		}
 	    }
 	}
     }
 
     public boolean hasAnySubUnits() {
 	return hasAnySubUnits(getUnit());
     }
 
     private static boolean hasAnySubUnits(final module.organization.domain.Unit unit) {
 	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
 	    final Party child = accountability.getChild();
 	    if (child.isUnit()) {
 		final module.organization.domain.Unit childUnit = (module.organization.domain.Unit) child;
 		if (childUnit.hasExpenditureUnit()) {
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     @Override
     @Service
     public void addObservers(Person observer) {
 	super.addObservers(observer);
     }
 
     @Override
     @Service
     public void removeObservers(Person observer) {
 	super.removeObservers(observer);
     }
 
     @Service
     public void toggleDefaultRegeim() {
 	final Boolean currentValue = getDefaultRegeimIsCCP();
 	final boolean newValue = currentValue == null ? Boolean.TRUE : !currentValue.booleanValue();
 	setDefaultRegeimIsCCP(Boolean.valueOf(newValue));
     }
 
     public static class UnitProcessEvaluator extends ProcessEvaluator<GenericProcess> {
 
 	private final Set<Unit> processed = new HashSet<Unit>();
 
 	public UnitProcessEvaluator(ProcessEvaluator<GenericProcess> genericProcessEvaluator) {
 	    next = genericProcessEvaluator;
 	}
 
     }
 
     public void evaluateAllProcesses(final ProcessEvaluator<GenericProcess> unitProcessEvaluator, final PaymentProcessYear year) {
 	// if (!unitProcessEvaluator.processed.contains(this)) {
 	// unitProcessEvaluator.processed.add(this);
 	for (final Financer financer : getFinancedItems()) {
 	    final PaymentProcess process = financer.getFundedRequest().getProcess();
 	    if (year == null || process.getPaymentProcessYear() == year) {
 		unitProcessEvaluator.evaluate(process);
 	    }
 	}
 	evaluateAllProcesses(unitProcessEvaluator, year, getUnit());
 
 	// for (final Unit unit : getSubUnitsSet()) {
 	// unit.evaluateAllProcesses(unitProcessEvaluator, year);
 	// }
 	// }
     }
 
     private static void evaluateAllProcesses(final ProcessEvaluator<GenericProcess> unitProcessEvaluator,
 	    final PaymentProcessYear year, final module.organization.domain.Unit unit) {
 	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
 	    final Party child = accountability.getChild();
 	    if (child.isUnit()) {
 		final module.organization.domain.Unit childUnit = (module.organization.domain.Unit) child;
 		if (childUnit.hasExpenditureUnit()) {
 		    childUnit.getExpenditureUnit().evaluateAllProcesses(unitProcessEvaluator, year);
 		} else {
 		    evaluateAllProcesses(unitProcessEvaluator, year, childUnit);
 		}
 	    }
 	}
     }
 
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument document = new IndexDocument(this);
 	document.indexField(UnitIndexFields.NAME_INDEX, StringNormalizer.normalize(getName()));
 	return document;
     }
 
     @Override
     public Set<Indexable> getObjectsToIndex() {
 	Set<Indexable> set = new HashSet<Indexable>();
 	set.add(this);
 	return set;
     }
 
     public boolean isAccountingResponsible(final Person person) {
 	final AccountingUnit accountingUnit = getAccountingUnit();
 	return accountingUnit != null && person != null && accountingUnit.hasResponsiblePeople(person);
     }
 
     public boolean isAccountManager(final Person accountManager) {
 	return getAccountManager() == accountManager || (hasParentUnit() && getParentUnit().isAccountManager(accountManager));
     }
 
     public boolean hasSomeAccountManager() {
 	return hasAccountManager() || (hasParentUnit() && getParentUnit().hasSomeAccountManager());
     }
 
     public boolean isUnitObserver(final User user) {
 	final Person person = user.getExpenditurePerson();
 	return getObserversSet().contains(person);
     }
 
     @Override
     public boolean isConnectedToCurrentHost() {
 	return getExpenditureTrackingSystem() == ExpenditureTrackingSystem.getInstance();
     }
 
 }
