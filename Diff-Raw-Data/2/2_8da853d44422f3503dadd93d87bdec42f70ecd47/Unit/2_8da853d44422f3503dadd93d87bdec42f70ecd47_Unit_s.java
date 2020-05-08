 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.util.Comparator;
 import java.util.Set;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 
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
 	for (final AcquisitionRequest acquisitionRequest : ExpenditureTrackingSystem.getInstance().getAcquisitionRequestsSet()) {
 	    if (acquisitionRequest.getAcquisitionProcess().getRequestingUnit() == this) {
		result.add(acquisitionRequest.getValueAllocated());
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
 		return unit;
 	    }
 	}
 	return null;
     }
 
     public boolean isResponsible(Person person) {
 	for (Authorization authorization : person.getAuthorizations()) {
 	    if (authorization.getUnit() == this) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     public String getPresentationName() {
 	return getName();
     }
 
 }
