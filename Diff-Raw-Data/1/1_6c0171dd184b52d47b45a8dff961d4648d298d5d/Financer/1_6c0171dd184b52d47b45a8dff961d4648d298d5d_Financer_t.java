 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.ResourceBundle;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public class Financer extends Financer_Base {
 
     protected Financer() {
 	super();
	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     public Financer(final AcquisitionRequest acquisitionRequest, final CostCenter costCenter) {
 	this();
 	if (acquisitionRequest == null || costCenter == null) {
 	    throw new DomainException("error.financer.wrong.initial.arguments");
 	}
 	if (acquisitionRequest.hasPayingUnit(costCenter)) {
 	    throw new DomainException("error.financer.acquisition.request.already.has.paying.unit");
 	}
 
 	setFundedRequest(acquisitionRequest);
 	setUnit(costCenter);
     }
 
     public void delete() {
 	if (checkIfCanDelete()) {
 	    removeExpenditureTrackingSystem();
 	    removeFundedRequest();
 	    removeUnit();
 	    Transaction.deleteObject(this);
 	}
     }
 
     private boolean checkIfCanDelete() {
 	if (hasAnyUnitItems()) {
 	    throw new DomainException("acquisitionProcess.message.exception.cannotRemovePayingUnit.alreadyAssignedToItems");
 	}
 	return true;
     }
 
     public Money getAmountAllocated() {
 	Money amount = Money.ZERO;
 	for (UnitItem unitItem : getUnitItems()) {
 	    if (unitItem.getRealShareValue() != null) {
 		amount = amount.add(unitItem.getRealShareValue());
 	    } else if (unitItem.getShareValue() != null) {
 		amount = amount.add(unitItem.getShareValue());
 	    }
 	}
 	return amount;
     }
 
     public Money getRealShareValue() {
 	Money amount = Money.ZERO;
 	for (UnitItem unitItem : getUnitItemsSet()) {
 	    amount = amount.add(unitItem.getRealShareValue());
 	}
 	return amount;
     }
 
     public Money getShareValue() {
 	Money amount = Money.ZERO;
 	for (UnitItem unitItem : getUnitItemsSet()) {
 	    amount = amount.add(unitItem.getShareValue());
 	}
 	return amount;
     }
 
     public boolean isRealUnitShareValueLessThanUnitShareValue() {
 	return getRealShareValue().isLessThanOrEqual(getShareValue());
     }
 
     public boolean isAccountingEmployee(final Person person) {
 	final Unit unit = getUnit();
 	return unit.isAccountingEmployee(person);
     }
 
     protected String getAllocationIds(final String id, final String key) {
 	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.AcquisitionResources", Language.getLocale());
 	final StringBuilder stringBuilder = new StringBuilder();
 	stringBuilder.append('[');
 	stringBuilder.append(resourceBundle.getObject(key));
 	stringBuilder.append(' ');
 	stringBuilder.append(id == null || id.isEmpty() ? "-" : id);
 	stringBuilder.append(" ]");
 	return stringBuilder.toString();
     }
 
     public String getFundAllocationIds() {
 	return getAllocationIds(getFundAllocationId(), "financer.label.allocation.id.prefix.giaf");
     }
 
     public String getEffectiveFundAllocationIds() {
 	return getAllocationIds(getEffectiveFundAllocationId(), "financer.label.allocation.id.prefix.giaf");
     }
 
     public boolean isProjectAccountingEmployee(final Person person) {
 	final Unit unit = getUnit();
 	return unit.isProjectAccountingEmployee(person);
     }
 
     public boolean hasAllocatedFundsForAllProject() {
 	return true;
     }
 
     public boolean hasAllocatedFundsPermanentlyForAllProjectFinancers() {
 	return true;
     }
 
 }
