 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.FundAllocationExpirationDate.FundAllocationNotAllowedException;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.ChangeFinancerAccountingUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationExpirationDateBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.SetRefundeeBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.VariantBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter.ChecksumPredicate;
 import pt.ist.fenixWebFramework.servlets.json.JsonObject;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.ist.fenixWebFramework.util.DomainReference;
 
 @Mapping(path = "/acquisitionSimplifiedProcedureProcess")
 public class SimplifiedProcedureProcessAction extends RegularAcquisitionProcessAction {
 
     static {
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
 		return !(httpServletRequest.getRequestURI().endsWith("/acquisitionSimplifiedProcedureProcess.do")
 			&& httpServletRequest.getQueryString() != null && httpServletRequest.getQueryString().contains(
 			"method=checkSupplierLimit"));
 	    }
 	});
     }
 
     @Override
     protected String getSelectUnitToAddForwardUrl() {
 	return "/acquisitions/commons/selectPayingUnitToAdd.jsp";
     }
 
     @Override
     protected String getRemovePayingUnitsForwardUrl() {
 	return "/acquisitions/commons/removePayingUnits.jsp";
     }
 
     @Override
     protected String getAssignUnitItemForwardUrl() {
 	return "/acquisitions/commons/assignUnitItem.jsp";
     }
 
     @Override
     protected String getEditRealShareValuesForwardUrl() {
 	return "/acquisitions/commons/assignUnitItemRealValues.jsp";
     }
 
     public static class AcquisitionProposalDocumentForm extends FileUploadBean {
 	private String proposalID;
 
 	public void setProposalID(String proposalID) {
 	    this.proposalID = proposalID;
 	}
 
 	public String getProposalID() {
 	    return proposalID;
 	}
     }
 
     public static class ReceiveInvoiceForm extends FileUploadBean {
 	private String invoiceNumber;
 	private LocalDate invoiceDate;
 	private List<DomainReference<AcquisitionRequestItem>> items;
 	private DomainReference<AcquisitionRequest> request;
 	private Boolean hasMoreInvoices = Boolean.FALSE;
 
 	public Boolean getHasMoreInvoices() {
 	    return hasMoreInvoices;
 	}
 
 	public void setHasMoreInvoices(Boolean hasMoreInvoices) {
 	    this.hasMoreInvoices = hasMoreInvoices;
 	}
 
 	public ReceiveInvoiceForm() {
 
 	}
 
 	public ReceiveInvoiceForm(AcquisitionRequest request) {
 	    setRequest(request);
 	    setItems(new ArrayList<AcquisitionRequestItem>(request.getAcquisitionRequestItemsSet()));
 	}
 
 	public String getInvoiceNumber() {
 	    return invoiceNumber;
 	}
 
 	public void setInvoiceNumber(String invoiceNumber) {
 	    this.invoiceNumber = invoiceNumber;
 	}
 
 	public LocalDate getInvoiceDate() {
 	    return invoiceDate;
 	}
 
 	public void setInvoiceDate(LocalDate invoiceDate) {
 	    this.invoiceDate = invoiceDate;
 	}
 
 	public void setItems(List<AcquisitionRequestItem> items) {
 	    this.items = new ArrayList<DomainReference<AcquisitionRequestItem>>();
 	    for (AcquisitionRequestItem item : items) {
 		this.items.add(new DomainReference<AcquisitionRequestItem>(item));
 	    }
 	}
 
 	public List<AcquisitionRequestItem> getItems() {
 	    List<AcquisitionRequestItem> items = new ArrayList<AcquisitionRequestItem>();
 	    for (DomainReference<AcquisitionRequestItem> item : this.items) {
 		items.add(item.getObject());
 	    }
 	    return items;
 	}
 
 	public void setRequest(AcquisitionRequest request) {
 	    this.request = new DomainReference<AcquisitionRequest>(request);
 	}
 
 	public AcquisitionRequest getRequest() {
 	    return this.request.getObject();
 	}
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected SimplifiedProcedureProcess getProcess(final HttpServletRequest request) {
 	SimplifiedProcedureProcess process = getDomainObject(request, "acquisitionProcessOid");
 	if (process == null) {
 	    process = (SimplifiedProcedureProcess) super.getProcess(request);
 	}
 	return process;
     }
 
     public ActionForward prepareCreateAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateAcquisitionProcessBean acquisitionProcessBean = new CreateAcquisitionProcessBean();
 	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
 	return forward(request, "/acquisitions/createAcquisitionProcess.jsp");
     }
 
     public ActionForward createNewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	CreateAcquisitionProcessBean createAcquisitionProcessBean = getRenderedObject();
 	final Person person = getLoggedPerson();
 	createAcquisitionProcessBean.setRequester(person);
 	final SimplifiedProcedureProcess acquisitionProcess = SimplifiedProcedureProcess
 		.createNewAcquisitionProcess(createAcquisitionProcessBean);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeAddAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final AcquisitionProposalDocumentForm acquisitionProposalDocumentForm = new AcquisitionProposalDocumentForm();
 	request.setAttribute("acquisitionProposalDocumentForm", acquisitionProposalDocumentForm);
 	return forward(request, "/acquisitions/addAcquisitionProposalDocument.jsp");
     }
 
     public ActionForward executeChangeAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeAddAcquisitionProposalDocument(mapping, form, request, response);
     }
 
     public ActionForward addAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final AcquisitionProposalDocumentForm acquisitionProposalDocumentForm = getRenderedObject();
 	final String filename = acquisitionProposalDocumentForm.getFilename();
 	final byte[] bytes = consumeInputStream(acquisitionProposalDocumentForm);
 	final String proposalID = acquisitionProposalDocumentForm.getProposalID();
 	final AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 	final AcquisitionProposalDocument acquisitionProposalDocument = acquisitionRequest.getAcquisitionProposalDocument();
 	final String activity = acquisitionProposalDocument == null ? "AddAcquisitionProposalDocument"
 		: "ChangeAcquisitionProposalDocument";
 	acquisitionProcess.getActivityByName(activity).execute(acquisitionProcess, filename, bytes, proposalID);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward downloadAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 	final AcquisitionProposalDocument acquisitionProposalDocument = getDomainObject(request, "acquisitionProposalDocumentOid");
 	return download(response, acquisitionProposalDocument);
     }
 
     public ActionForward downloadAcquisitionPurchaseOrderDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 	final PurchaseOrderDocument acquisitionRequestDocument = getDomainObject(request, "purchaseOrderDocumentOid");
 	return download(response, acquisitionRequestDocument);
     }
 
     public ActionForward executeSubmitForFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getLoggedPerson();
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForFundAllocation", person);
     }
 
     public ActionForward executeFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final FundAllocationExpirationDateBean fundAllocationExpirationDateBean = new FundAllocationExpirationDateBean();
 	try {
 	    genericActivityExecution(acquisitionProcess, "FundAllocationExpirationDate", fundAllocationExpirationDateBean);
 	} catch (final FundAllocationNotAllowedException ex) {
 	    addErrorMessage(ex.getMessage(), getBundle(), ex.getArgs());
 	}
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward allocateFundsToServiceProvider(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final FundAllocationExpirationDateBean fundAllocationExpirationDateBean = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "FundAllocationExpirationDate", fundAllocationExpirationDateBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeCreateAcquisitionPurchaseOrderDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return forward(request, "/acquisitions/createAcquisitionRequest.jsp");
     }
 
     public ActionForward createAcquisitionPurchaseOrderDocument(ActionMapping mapping, ActionForm actionForm,
 	    HttpServletRequest request, HttpServletResponse response) throws IOException {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 
 	AbstractActivity<AcquisitionProcess> createAquisitionRequest = acquisitionProcess
 		.getActivityByName("CreateAcquisitionPurchaseOrderDocument");
 	createAquisitionRequest.execute(acquisitionProcess);
 
 	return executeCreateAcquisitionPurchaseOrderDocument(mapping, actionForm, request, response);
     }
 
     public ActionForward executeReceiveInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	request.setAttribute("invoiceActivity", "saveInvoice");
 	return executeInvoiceActivity(mapping, request);
     }
 
     public ActionForward executeFixInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	request.setAttribute("invoiceActivity", "updateInvoice");
 	return executeInvoiceActivity(mapping, request);
     }
 
     private ActionForward executeInvoiceActivity(final ActionMapping mapping, final HttpServletRequest request) {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	ReceiveInvoiceForm receiveInvoiceForm = getRenderedObject();
 	if (receiveInvoiceForm == null) {
 	    final AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 	    receiveInvoiceForm = new ReceiveInvoiceForm(acquisitionRequest);
 	    // TODO: FIX INVOICE
 	    // if (acquisitionRequest.hasInvoice()) {
 	    // final Invoice invoice = acquisitionRequest.getInvoice();
 	    // receiveInvoiceForm.setInvoiceNumber(invoice.getInvoiceNumber());
 	    // receiveInvoiceForm.setInvoiceDate(invoice.getInvoiceDate());
 	    // }
 	}
 	request.setAttribute("receiveInvoiceForm", receiveInvoiceForm);
 	return forward(request, "/acquisitions/receiveInvoice.jsp");
     }
 
     public ActionForward saveInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return processInvoiceData(mapping, request, "ReceiveInvoice");
     }
 
     public ActionForward updateInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return processInvoiceData(mapping, request, "FixInvoice");
     }
 
     private ActionForward processInvoiceData(final ActionMapping mapping, final HttpServletRequest request, String activity) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final ReceiveInvoiceForm receiveInvoiceForm = getRenderedObject();
 	final byte[] bytes = consumeInputStream(receiveInvoiceForm);
 	AbstractActivity<RegularAcquisitionProcess> receiveInvoice = acquisitionProcess.getActivityByName(activity);
 	try {
 	    receiveInvoice.execute(acquisitionProcess, receiveInvoiceForm.getFilename(), bytes, receiveInvoiceForm
 		    .getInvoiceNumber(), receiveInvoiceForm.getInvoiceDate(), receiveInvoiceForm.getItems(), receiveInvoiceForm
 		    .getHasMoreInvoices());
 	} catch (DomainException ex) {
 	    addErrorMessage(ex.getMessage(), getBundle(), ex.getArgs());
 	}
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward downloadInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 	final Invoice invoice = getDomainObject(request, "invoiceOid");
 	return download(response, invoice);
     }
 
     public ActionForward executeConfirmInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getLoggedPerson();
 	return executeActivityAndViewProcess(mapping, form, request, response, "ConfirmInvoice", person);
     }
 
     public ActionForward executePayAcquisition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	VariantBean bean = new VariantBean();
 
 	request.setAttribute("bean", bean);
 	request.setAttribute("process", acquisitionProcess);
 	return forward(request, "/acquisitions/executePayment.jsp");
     }
 
     public ActionForward executePayAcquisitionAction(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	String paymentReference = getRenderedObject("reference");
 	return executeActivityAndViewProcess(mapping, form, request, response, "PayAcquisition", paymentReference);
     }
 
     public ActionForward executeUnApprove(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnApprove");
     }
 
     public ActionForward executeEditAcquisitionRequestItemRealValues(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem acquisitionRequestItem = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionRequestItemBean itemBean = new AcquisitionRequestItemBean(acquisitionRequestItem);
 	request.setAttribute("itemBean", itemBean);
 	return forward(request, "/acquisitions/editRequestItemRealValues.jsp");
     }
 
     public ActionForward executeAcquisitionRequestItemRealValuesEdition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject("acquisitionRequestItem");
 	genericActivityExecution(requestItemBean.getAcquisitionRequest().getAcquisitionProcess(),
 		"EditAcquisitionRequestItemRealValues", requestItemBean);
 	return viewAcquisitionProcess(mapping, request, (SimplifiedProcedureProcess) requestItemBean.getAcquisitionRequest()
 		.getAcquisitionProcess());
     }
 
     public ActionForward executeUnSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnSubmitForApproval");
     }
 
     public ActionForward executeRemoveFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveFundAllocationExpirationDate");
     }
 
     public ActionForward executeCancelRemoveFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "CancelRemoveFundAllocationExpirationDate");
     }
 
     public ActionForward editSupplierAddress(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 
 	return forward(request, "/acquisitions/editSupplierAddress.jsp");
     }
 
     public ActionForward executeSendPurchaseOrderToSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SendPurchaseOrderToSupplier");
     }
 
     public ActionForward executeSkipPurchaseOrderDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SkipPurchaseOrderDocument");
     }
 
     public ActionForward executeSubmitForConfirmInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForConfirmInvoice");
     }
 
     public ActionForward executeRevertInvoiceSubmission(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RevertInvoiceSubmission");
     }
 
     public ActionForward executeChangeFinancersAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getLoggedPerson();
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	Set<Financer> financersWithFundsAllocated = acquisitionProcess.getAcquisitionRequest()
 		.getAccountingUnitFinancerWithNoFundsAllocated(person);
 	Set<ChangeFinancerAccountingUnitBean> financersBean = new HashSet<ChangeFinancerAccountingUnitBean>(
 		financersWithFundsAllocated.size());
 	for (Financer financer : financersWithFundsAllocated) {
 	    financersBean.add(new ChangeFinancerAccountingUnitBean(financer, financer.getAccountingUnit()));
 	}
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("financersBean", financersBean);
 	return forward(request, "/acquisitions/changeFinancersAccountingUnit.jsp");
     }
 
     public ActionForward changeFinancersAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	Collection<ChangeFinancerAccountingUnitBean> financersBean = getRenderedObject();
 	SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "ChangeFinancersAccountingUnit", financersBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     @Override
     protected Class<? extends AcquisitionProcess> getProcessClass() {
 	return SimplifiedProcedureProcess.class;
     }
 
     public ActionForward executeSetRefundee(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 	final SetRefundeeBean setRefundeeBean = new SetRefundeeBean(acquisitionProcess);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("setRefundeeBean", setRefundeeBean);
 	return forward(request, "/acquisitions/setRefundee.jsp");
     }
 
     public ActionForward setRefundee(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SetRefundeeBean setRefundeeBean = getRenderedObject();
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 	final Person refundee = acquisitionRequest.getRefundee();
 	final String activity = refundee == null ? "SetRefundee" : "ChangeRefundee";
 	genericActivityExecution(acquisitionProcess, activity, setRefundeeBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeChangeRefundee(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeSetRefundee(mapping, form, request, response);
     }
 
     public ActionForward executeUnsetRefundee(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final SetRefundeeBean setRefundeeBean = new SetRefundeeBean(acquisitionProcess);
 	setRefundeeBean.setRefundee(null);
 	genericActivityExecution(acquisitionProcess, "UnsetRefundee", setRefundeeBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRemoveFundAllocationExpirationDateForResponsible(final ActionMapping mapping,
 	    final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "RemoveFundAllocationExpirationDateForResponsible");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeUnAuthorizeAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "UnAuthorizeAcquisitionProcess");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRevertSkipPurchaseOrderDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "RevertSkipPurchaseOrderDocument");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeCancelInvoiceConfirmation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	User user = UserView.getCurrentUser();
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "CancelInvoiceConfirmation", user.getExpenditurePerson());
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRevertProcessNotConfirmmingFundAllocationExpirationDate(final ActionMapping mapping,
 	    final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "RevertProcessNotConfirmmingFundAllocationExpirationDate");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRemoveCancelProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "RemoveCancelProcess");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRevertToInvoiceConfirmation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "RevertToInvoiceConfirmation");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeUnlockInvoiceReceiving(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "UnlockInvoiceReceiving");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeLockInvoiceReceiving(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	genericActivityExecution(acquisitionProcess, "LockInvoiceReceiving");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeRemoveInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("process", acquisitionProcess);
 	return forward(request, "/acquisitions/removeAcquisitionInvoice.jsp");
     }
 
     public ActionForward removeInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final AcquisitionInvoice invoice = getDomainObject(request, "invoiceOid");
 	genericActivityExecution(acquisitionProcess, "RemoveInvoice", invoice);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     private static final String SUPPLIER_LIMIT_OK = "SOK";
     private static final String SUPPLIER_LIMIT_NOT_OK = "SNOK";
 
     public ActionForward checkSupplierLimit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 
 	if (getLoggedPerson() == null) {
 	    return null;
 	}
 
 	Supplier supplier = getDomainObject(request, "supplierOid");
 	Money softLimit = supplier.getSoftTotalAllocated();
 	Money supplierLimit = supplier.getSupplierLimit();
 
 	JsonObject reply = new JsonObject();
 
 	reply.addAttribute("status", softLimit.isGreaterThanOrEqual(supplierLimit) ? SUPPLIER_LIMIT_NOT_OK : SUPPLIER_LIMIT_OK);
 	reply.addAttribute("softLimit", softLimit.toFormatStringWithoutCurrency());
 	reply.addAttribute("supplierLimit", supplierLimit.toFormatStringWithoutCurrency());
 
 	writeJsonReply(response, reply);
 
 	return null;
     }
 }
