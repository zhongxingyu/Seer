 package module.finance.domain;
 
 import myorg.domain.util.Money;
 
 public class Supplier extends Supplier_Base {
 
     public static Money SUPPLIER_LIMIT = new Money("75000");
 
     public static Money SOFT_SUPPLIER_LIMIT = new Money("60000");
 
     public Supplier() {
	super();
	setFinanceSystem(FinanceSystem.getInstance());
	setSupplierLimit(SOFT_SUPPLIER_LIMIT);
     }
 
     public void delete() {
	if (checkIfCanBeDeleted()) {
 	    removeFinanceSystem();
 	    deleteDomainObject();
 	}
     }
 
    protected boolean checkIfCanBeDeleted() {
	return !hasAnyProvisions();
    }

     public String getPresentationName() {
 	return getFiscalIdentificationCode() + " - " + getName();
     }
 
     @Override
     public void setSupplierLimit(final Money supplierLimit) {
 	final Money newLimit = supplierLimit.isGreaterThanOrEqual(SUPPLIER_LIMIT) ? SUPPLIER_LIMIT : supplierLimit;
 	super.setSupplierLimit(newLimit);
     }
 
     public Money getAllocated() {
 	Money result = Money.ZERO;
 	for (final Provision provision : getProvisionsSet()) {
 	    if (provision.isInAllocationPeriod()) {
 		result = result.add(provision.getValueAllocatedToSupplier());
 	    }
 	}
 	return result;
     }
 
     public Money getAllocatedForLimit() {
 	Money result = Money.ZERO;
 	for (final Provision provision : getProvisionsSet()) {
 	    if (provision.isInAllocationPeriod()) {
 		result = result.add(provision.getValueAllocatedToSupplierForLimit());
 	    }
 	}
 	return result;
     }
 
     public boolean isFundAllocationAllowed(final Money value) {
 	final Money allocated = getAllocated();
 	final Money totalValue = allocated.add(value);
 	return totalValue.isLessThanOrEqual(SUPPLIER_LIMIT) && totalValue.isLessThan(getSupplierLimit());
     }
 
 }
