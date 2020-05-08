 package module.workingCapital.domain;
 
 import module.finance.domain.Supplier;
 import myorg.domain.User;
 import myorg.domain.util.Money;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 
 public class WorkingCapitalAcquisition extends WorkingCapitalAcquisition_Base {
 
     public WorkingCapitalAcquisition() {
 	super();
 	setWorkingCapitalSystem(WorkingCapitalSystem.getInstance());
     }
 
     public WorkingCapitalAcquisition(final WorkingCapital workingCapital, final String documentNumber, final Supplier supplier,
 	    final String description, final AcquisitionClassification acquisitionClassification, final Money valueWithoutVat,
 	    final Money money, final byte[] invoiceContent, String displayName, String fileName) {
 	this();
 	setWorkingCapital(workingCapital);
 	edit(documentNumber, supplier, description, acquisitionClassification, valueWithoutVat);
 	final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = new WorkingCapitalAcquisitionTransaction(this, money);
 	if (invoiceContent != null) {
 	    WorkingCapitalInvoiceFile workingCapitalInvoiceFile = new WorkingCapitalInvoiceFile(displayName, fileName,
 		    invoiceContent, workingCapitalAcquisitionTransaction);
 	    workingCapital.getWorkingCapitalProcess().addFiles(workingCapitalInvoiceFile);
 	}
     }
 
     public void edit(String documentNumber, Supplier supplier, String description,
 	    AcquisitionClassification acquisitionClassification, Money valueWithoutVat) {
 	setDocumentNumber(documentNumber);
 	setAcquisitionClassification(acquisitionClassification);
 	setSupplier(supplier);
 	setDescription(description);
 	setValueWithoutVat(valueWithoutVat);
     }
 
     public void edit(String documentNumber, Supplier supplier, String description,
 	    AcquisitionClassification acquisitionClassification, Money valueWithoutVat, Money value) {
 	edit(documentNumber, supplier, description, acquisitionClassification, valueWithoutVat);
 	final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = getWorkingCapitalAcquisitionTransaction();
 	workingCapitalAcquisitionTransaction.resetValue(value);
     }
 
     public void edit(String documentNumber, Supplier supplier, String description,
 	    AcquisitionClassification acquisitionClassification, Money valueWithoutVat, Money value, byte[] invoiceContent,
 	    String displayName, String fileName) {
 	edit(documentNumber, supplier, description, acquisitionClassification, valueWithoutVat, value);
 	getWorkingCapitalAcquisitionTransaction().getInvoice().delete();
 
 	WorkingCapitalInvoiceFile workingCapitalInvoiceFile = new WorkingCapitalInvoiceFile(displayName, fileName,
 		invoiceContent, getWorkingCapitalAcquisitionTransaction());
 	getWorkingCapital().getWorkingCapitalProcess().addFiles(workingCapitalInvoiceFile);
 
     }
 
     public void approve(final User user) {
 	setApproved(new DateTime());
 	final Money valueForAuthorization = Money.ZERO;
 	final WorkingCapital workingCapital = getWorkingCapital();
 	final Authorization authorization = workingCapital.findUnitResponsible(user.getPerson(), valueForAuthorization);
 	setApprover(authorization);
     }
 
     public void reject(final User user) {
 	setRejectedApproval(new DateTime());
 	final Money valueForAuthorization = Money.ZERO;
 	final WorkingCapital workingCapital = getWorkingCapital();
 	final Authorization authorization = workingCapital.findUnitResponsible(user.getPerson(), valueForAuthorization);
 	setApprover(authorization);
     }
 
     public void unApprove() {
 	setApproved(null);
 	removeApprover();
     }
 
     public void verify(User user) {
 	setVerified(new DateTime());
 	setVerifier(user.getPerson());
     }
 
     public void rejectVerify(final User user) {
 	setNotVerified(new DateTime());
 	setVerifier(user.getPerson());
     }
 
     public void unVerify() {
 	setVerified(null);
 	removeVerifier();
     }
 
     public boolean isCanceledOrRejected() {
	return (getIsCanceled() != null && getIsCanceled().booleanValue()) || getRejectedApproval() != null; // || getNotVerified() != null;
     }
 
     public void cancel() {
 	setIsCanceled(Boolean.TRUE);
     }
 
     public Money getValue() {
 	return getWorkingCapitalAcquisitionTransaction().getValue();
     }
 
     @Override
     public Money getValueAllocatedToSupplier() {
 	return isCanceledOrRejected() ? Money.ZERO : getValue();
     }
 
     @Override
     public Money getValueAllocatedToSupplierForLimit() {
 	return isCanceledOrRejected() ? Money.ZERO : getValueWithoutVat();
     }
 
 }
