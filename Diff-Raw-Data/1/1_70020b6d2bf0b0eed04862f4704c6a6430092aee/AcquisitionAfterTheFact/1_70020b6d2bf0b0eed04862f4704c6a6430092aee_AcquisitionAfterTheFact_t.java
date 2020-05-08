 package pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact;
 
 import myorg.domain.util.ByteArray;
 import myorg.domain.util.Money;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.dto.AfterTheFactAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 
 public class AcquisitionAfterTheFact extends AcquisitionAfterTheFact_Base {
 
     public AcquisitionAfterTheFact(final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess) {
 	super();
 	setAfterTheFactAcquisitionProcess(afterTheFactAcquisitionProcess);
     }
 
     public void edit(final AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean) {
 	setAfterTheFactAcquisitionType(afterTheFactAcquisitionProcessBean.getAfterTheFactAcquisitionType());
 	setValue(afterTheFactAcquisitionProcessBean.getValue());
 	setVatValue(afterTheFactAcquisitionProcessBean.getVatValue());
 	setSupplier(afterTheFactAcquisitionProcessBean.getSupplier());
 	setDescription(afterTheFactAcquisitionProcessBean.getDescription());
 	setDeletedState(Boolean.FALSE);
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
 	    if (getValue() != null && !supplier.isFundAllocationAllowed(getValue())) {
 		throw new DomainException("acquisitionProcess.message.exception.SupplierDoesNotAlloweAmount");
 	    }
 	    super.setSupplier(supplier);
 	}
     }
 
     @Override
     public void setValue(final Money value) {
	super.setValue(Money.ZERO);
 	if (getSupplier() != null && !getSupplier().isFundAllocationAllowed(Money.ZERO)) {
 	    throw new DomainException("acquisitionProcess.message.exception.SupplierDoesNotAlloweAmount");
 	}
 	super.setValue(value);
     }
 
     public boolean isAppiableForYear(final int year) {
 	final LocalDate localDate = getInvoice().getInvoiceDate();
 	return localDate != null && localDate.getYear() == year;
     }
 
     @Override
     public AfterTheFactInvoice receiveInvoice(String filename, byte[] bytes, String invoiceNumber, LocalDate invoiceDate) {
 	final AfterTheFactInvoice invoice = hasInvoice() ? getInvoice() : new AfterTheFactInvoice(this);
 	invoice.setFilename(filename);
 	invoice.setContent(new ByteArray(bytes));
 	invoice.setInvoiceNumber(invoiceNumber);
 	invoice.setInvoiceDate(invoiceDate);
 	setInvoice(invoice);
 
 	return invoice;
     }
 
     public String getInvoiceNumber() {
 	Invoice invoice = getInvoice();
 	return invoice != null ? invoice.getInvoiceNumber() : null;
     }
 
     public LocalDate getInvoiceDate() {
 	Invoice invoice = getInvoice();
 	return invoice != null ? invoice.getInvoiceDate() : null;
     }
 
 }
