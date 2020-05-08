 package pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact;
 
 import java.math.BigDecimal;
 
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class AcquisitionAfterTheFact extends AcquisitionAfterTheFact_Base {
 
     public AcquisitionAfterTheFact(final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess) {
 	super();
 	setAfterTheFactAcquisitionProcess(afterTheFactAcquisitionProcess);
     }
 
     public void edit(AfterTheFactAcquisitionType type, Money value, BigDecimal vatValue, Supplier supplier, String description) {
 	setDeletedState(Boolean.FALSE);
 	setAfterTheFactAcquisitionType(type);
 	setValue(value);
 	setVatValue(vatValue);
 	setSupplier(supplier);
 	setDescription(description);
     }
 
     public void delete() {
 	setDeletedState(Boolean.TRUE);
     }
 
     public String getAcquisitionProcessId() {
 	return getAfterTheFactAcquisitionProcess().getAcquisitionProcessId();
     }
 
     @Override
     public void setSupplier(final Supplier supplier) {
 	if (supplier != getSupplier()) {
 	    super.setSupplier(supplier);
 	    setValue(getValue());
 	}
     }
 
     @Override
     public void setValue(final Money value) {
 	super.setValue(Money.ZERO);
 	if (getSupplier() != null && !getSupplier().isFundAllocationAllowed(Money.ZERO)) {
	    throw new DomainException("acquisitionProcess.message.exception.SupplierDoesNotAlloweAmount", DomainException
		    .getResourceFor("resources/AcquisitionResources"));
 	}
 	super.setValue(value);
     }
 
     public boolean isAppiableForYear(final int year) {
 	final LocalDate localDate = getAfterTheFactAcquisitionProcess().getInvoice().getInvoiceDate();
 	return localDate != null && localDate.getYear() == year;
     }
 
     public AfterTheFactInvoice receiveInvoice(String filename, byte[] bytes, String invoiceNumber, LocalDate invoiceDate) {
 	return getAfterTheFactAcquisitionProcess().receiveInvoice(filename, bytes, invoiceNumber, invoiceDate);
     }
 
     public String getInvoiceNumber() {
 	return getAfterTheFactAcquisitionProcess().getInvoiceNumber();
     }
 
     public LocalDate getInvoiceDate() {
 	return getAfterTheFactAcquisitionProcess().getInvoiceDate();
     }
 }
