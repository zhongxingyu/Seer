 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Set;
 
 import module.finance.domain.Supplier;
 import myorg.domain.VirtualHost;
 import myorg.domain.util.Money;
import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixframework.DomainObject;
 
 public class AcquisitionFundAllocationDiaryAndTransactionReportRequest extends AcquisitionFundAllocationDiaryAndTransactionReportRequest_Base {
     
     public AcquisitionFundAllocationDiaryAndTransactionReportRequest(final UnitItem unitItem, 
 	    final String processId, final String payingUnitNumber, final String payingAccountingUnit,
 	    final String diaryNumber, final String transactionNumber) {
         super();
         setUnitItem(unitItem);
 	setProcessId(processId);
 	setPayingUnitNumber(payingUnitNumber);
 	setPayingAccountingUnit(payingAccountingUnit);
 	setDiaryNumber(diaryNumber);
 	setTransactionNumber(transactionNumber);
     }
 
     @Override
     public String getQueryString() {
 	final UnitItem unitItem = getUnitItem();
 	final Unit unit = unitItem.getUnit();
 	final RequestItem item = unitItem.getItem();
 	final CPVReference cpvReference = item.getCPVReference();
 	final RequestWithPayment request = item.getRequest();
 	final Supplier supplier = getSupplier(request);
 	final PaymentProcess process = request.getProcess();
 
 	final Money shareValue = unitItem.getShareValue();
 
 	final BigDecimal d = new BigDecimal(1).add(unitItem.getVatValue().divide(new BigDecimal(100)));
 	final Money shareWithoutVat = shareValue.divideAndRound(d);
 	final Money shareVat = shareValue.subtract(shareWithoutVat);
 
 	System.out.println(getProcessId());
 	Object[] insertArgs = new Object[] {
 		"INTERACT_ID", Long.valueOf(getInteractionId()),
 		"PROCESS_ID", getProcessId(),
 		"ITEM_ID", unitItem.getExternalId(),
 		"PROJ_ID", getProjectId(unit),
 		"PROJ_MEMBER", getSubProjectId(unit),
 		"SUPPLIER_ID", supplier == null ? null : supplier.getGiafKey(),
 		"SUPPLIER_DOC_TYPE", request instanceof AcquisitionRequest ?
 			(supplier == null ? null : (hasProposal(request) ? "Proposta" : "Factura")) :
 			    (supplier == null ? "Reembolso" : "Factura"),
 		"SUPPLIER_DOC_ID", supplier == null ? null : limitStringSize(getProposalNumber(request), 24),
 		"CPV_ID", cpvReference.getCode(),
 		"CPV_DESCRIPTION", cpvReference.getDescription(),
 		"MOV_DESCRIPTION", limitStringSize(Integer.toString(item.getUnitItemsCount()) + " - " + item.getDescription(), 4000),
 		"MOV_PCT_IVA", unitItem.getVatValue(),
 		"MOV_VALUE", shareValue,
 		"MOV_VALUE_IVA", shareVat,
 //,		    "CALLBACK_URL", getCallbackUrl()
 		"PROCESS_URL", getProcessUrl(process),
 		"GIAF_DIARIO", getDiaryNumber(),
 		"GIAF_NUM_REG", getTransactionNumber(),
 	    };
 	final String q = insertQuery(insertArgs);
 	System.out.println(q);
 	return q;
     }
 
     public String getProjectId(final Unit unit) {
 	if (unit instanceof Project) {
 	    final Project project = (Project) unit;
 	    return project.getProjectCode();
 	} else if (unit instanceof SubProject) {
 	    final SubProject subProject = (SubProject) unit;
 	    final Project project = (Project) subProject.getParentUnit();
 	    return project.getProjectCode();
 	}
 	return null;
     }
 
     public String getSubProjectId(final Unit unit) {
 	if (unit instanceof SubProject) {
 	    final SubProject subProject = (SubProject) unit;
 	    final Project project = (Project) subProject.getParentUnit();
 	    final String projectName = project.getName();
 	    final String description = subProject.getName().substring(projectName.length() + 3);
 	    final int i = description.indexOf(" - ");
 	    return description.substring(0, i);
 	}
 	return null;
     }
 
     @Override
     public void processResultSet(final ResultSet resultSet) throws SQLException {
 	registerOnExternalSystem();
     }
 
     @Override
     protected String getTableName() {
 	return "CONTABILIZACAO";
     }
 
     private Supplier getSupplier(final RequestWithPayment request) {
 	if (request instanceof AcquisitionRequest) {
 	    final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
 	    return acquisitionRequest.getSupplier();
 	}
 	return null;
     }
 
     private boolean hasProposal(final RequestWithPayment request) {
 	if (request instanceof AcquisitionRequest) {
 	    final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
 	    final AcquisitionProcess process = acquisitionRequest.getProcess();
 	    final AcquisitionProposalDocument acquisitionProposalDocument = process.getAcquisitionProposalDocument();
 	    if (acquisitionProposalDocument != null) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private String getProposalNumber(final RequestWithPayment request) {
 	if (request instanceof AcquisitionRequest) {
 	    final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
 	    final AcquisitionProcess process = acquisitionRequest.getProcess();
 	    final AcquisitionProposalDocument acquisitionProposalDocument = process.getAcquisitionProposalDocument();
 	    if (acquisitionProposalDocument != null) {
 		return acquisitionProposalDocument.getProposalId();
 	    }
 	    final Set<PaymentProcessInvoice> invoices = request.getInvoices();
 	    if (!invoices.isEmpty()) {
 		return invoices.iterator().next().getInvoiceNumber();
 	    }
 	}
 	return null;
     }
 
     protected String getProcessUrl(final DomainObject process) {
 	final StringBuilder result = new StringBuilder();
 	result.append("https://");
 	result.append(VirtualHost.getVirtualHostForThread().getHostname());
 	result.append("/ForwardToProcess/");
 	result.append(process.getExternalId());
 	return result.toString();
     }
 
 }
