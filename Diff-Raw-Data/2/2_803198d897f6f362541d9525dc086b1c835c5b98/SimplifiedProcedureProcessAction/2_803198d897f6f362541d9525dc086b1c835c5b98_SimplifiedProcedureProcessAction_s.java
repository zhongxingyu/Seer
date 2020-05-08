 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.UnitItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.ChangeFinancerAccountingUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationExpirationDateBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.SetRefundeeBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.VariantBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionSimplifiedProcedureProcess")
 @Forwards( { @Forward(name = "edit.request.acquisition", path = "/acquisitions/editAcquisitionRequest.jsp"),
 	@Forward(name = "create.acquisition.process", path = "/acquisitions/createAcquisitionProcess.jsp"),
 	@Forward(name = "view.acquisition.process", path = "/acquisitions/viewAcquisitionProcess.jsp"),
 	@Forward(name = "search.acquisition.process", path = "/acquisitions/searchAcquisitionProcess.jsp"),
 	@Forward(name = "add.acquisition.proposal.document", path = "/acquisitions/addAcquisitionProposalDocument.jsp"),
 	@Forward(name = "create.acquisition.request.item", path = "/acquisitions/createAcquisitionRequestItem.jsp"),
 	@Forward(name = "reject.acquisition.process", path = "/acquisitions/rejectAcquisitionProcess.jsp"),
 	@Forward(name = "allocate.project.funds", path = "/acquisitions/allocateProjectFunds.jsp"),
 	@Forward(name = "allocate.funds", path = "/acquisitions/allocateFunds.jsp"),
 	@Forward(name = "allocate.effective.project.funds", path = "/acquisitions/allocateEffectiveProjectFunds.jsp"),
 	@Forward(name = "allocate.effective.funds", path = "/acquisitions/allocateEffectiveFunds.jsp"),
 	@Forward(name = "allocate.funds.to.service.provider", path = "/acquisitions/allocateFundsToServiceProvider.jsp"),
 	@Forward(name = "prepare.create.acquisition.request", path = "/acquisitions/createAcquisitionRequest.jsp"),
 	@Forward(name = "receive.invoice", path = "/acquisitions/receiveInvoice.jsp"),
 	@Forward(name = "select.unit.to.add", path = "/acquisitions/selectPayingUnitToAdd.jsp"),
 	@Forward(name = "remove.paying.units", path = "/acquisitions/removePayingUnits.jsp"),
 	@Forward(name = "edit.request.item", path = "/acquisitions/editRequestItem.jsp"),
 	@Forward(name = "edit.request.item.real.values", path = "/acquisitions/editRequestItemRealValues.jsp"),
 	@Forward(name = "assign.unit.item", path = "/acquisitions/assignUnitItem.jsp"),
 	@Forward(name = "edit.real.shares.values", path = "/acquisitions/editRealSharesValues.jsp"),
 	@Forward(name = "edit.supplier", path = "/acquisitions/editSupplierAddress.jsp"),
 	@Forward(name = "execute.payment", path = "/acquisitions/executePayment.jsp"),
 	@Forward(name = "change.financers.accounting.units", path = "/acquisitions/changeFinancersAccountingUnit.jsp"),
 	@Forward(name = "view.comments", path = "/acquisitions/viewComments.jsp"),
 	@Forward(name = "generic.upload", path = "/acquisitions/genericUpload.jsp"),
 	@Forward(name = "set.refundee", path = "/acquisitions/setRefundee.jsp") })
 public class SimplifiedProcedureProcessAction extends RegularAcquisitionProcessAction {
 
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
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected SimplifiedProcedureProcess getProcess(final HttpServletRequest request) {
 	return (SimplifiedProcedureProcess) getProcess(request, "acquisitionProcessOid");
     }
 
     public ActionForward prepareCreateAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateAcquisitionProcessBean acquisitionProcessBean = new CreateAcquisitionProcessBean();
 	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
 	return mapping.findForward("create.acquisition.process");
     }
 
     public ActionForward createNewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	CreateAcquisitionProcessBean createAcquisitionProcessBean = getRenderedObject();
 	User user = UserView.getUser();
 	createAcquisitionProcessBean.setRequester(user != null ? user.getPerson() : null);
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
 	return mapping.findForward("add.acquisition.proposal.document");
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
 	final String activity = acquisitionProposalDocument == null ? "AddAcquisitionProposalDocument" : "ChangeAcquisitionProposalDocument";
 	acquisitionProcess.getActivityByName(activity).execute(acquisitionProcess, filename, bytes,
 		proposalID);
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
 	User user = UserView.getUser();
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForFundAllocation", user.getPerson());
     }
 
     public ActionForward executeProjectFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final User user = UserView.getUser();
 	if (acquisitionProcess.getCurrentOwner() == null ||
 		(user != null && acquisitionProcess.getCurrentOwner() == user.getPerson())) {
 	    if (acquisitionProcess.getCurrentOwner() == null) { 
 		acquisitionProcess.takeProcess();
 	    }
 	    request.setAttribute("acquisitionProcess", acquisitionProcess);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : acquisitionProcess.getProjectFinancersWithFundsAllocated(user.getPerson())) {
 		fundAllocationBeans.add(new FundAllocationBean(financer));
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	    return mapping.findForward("allocate.project.funds");
 	} else {
 	    return viewAcquisitionProcess(mapping, request, acquisitionProcess);
 	}
     }
 
     public ActionForward allocateProjectFunds(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "ProjectFundAllocation", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final User user = UserView.getUser();
 	if (acquisitionProcess.getCurrentOwner() == null ||
 		(user != null && acquisitionProcess.getCurrentOwner() == user.getPerson())) {
 	    if (acquisitionProcess.getCurrentOwner() == null) { 
 		acquisitionProcess.takeProcess();
 	    }
 	    request.setAttribute("acquisitionProcess", acquisitionProcess);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : acquisitionProcess.getFinancersWithFundsAllocated(user.getPerson())) {
 		fundAllocationBeans.add(new FundAllocationBean(financer));
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	    return mapping.findForward("allocate.funds");
 	} else {
 	    return viewAcquisitionProcess(mapping, request, acquisitionProcess);
 	}
     }
 
     public ActionForward allocateFunds(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "FundAllocation", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final FundAllocationExpirationDateBean fundAllocationExpirationDateBean = new FundAllocationExpirationDateBean();
 	request.setAttribute("fundAllocationExpirationDateBean", fundAllocationExpirationDateBean);
 	return mapping.findForward("allocate.funds.to.service.provider");
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
 	return mapping.findForward("prepare.create.acquisition.request");
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
 	    receiveInvoiceForm = new ReceiveInvoiceForm();
 	    final AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 	    if (acquisitionRequest.hasInvoice()) {
 		final Invoice invoice = acquisitionRequest.getInvoice();
 		receiveInvoiceForm.setInvoiceNumber(invoice.getInvoiceNumber());
 		receiveInvoiceForm.setInvoiceDate(invoice.getInvoiceDate());
 	    }
 	}
 	request.setAttribute("receiveInvoiceForm", receiveInvoiceForm);
 	return mapping.findForward("receive.invoice");
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
 	receiveInvoice.execute(acquisitionProcess, receiveInvoiceForm.getFilename(), bytes,
 		receiveInvoiceForm.getInvoiceNumber(), receiveInvoiceForm.getInvoiceDate());
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward downloadInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 	final Invoice invoice = getDomainObject(request, "invoiceOid");
 	return download(response, invoice);
     }
 
     public ActionForward executeConfirmInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	User user = UserView.getUser();
 	return executeActivityAndViewProcess(mapping, form, request, response, "ConfirmInvoice", user.getPerson());
     }
 
     public ActionForward executePayAcquisition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	VariantBean bean = new VariantBean();
 
 	request.setAttribute("bean", bean);
 	request.setAttribute("process", acquisitionProcess);
 	return mapping.findForward("execute.payment");
     }
 
     public ActionForward executePayAcquisitionAction(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	String paymentReference = getRenderedObject("reference");
 	return executeActivityAndViewProcess(mapping, form, request, response, "PayAcquisition", paymentReference);
     }
 
     public ActionForward executeAllocateProjectFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final User user = UserView.getUser();
 	if (acquisitionProcess.getCurrentOwner() == null ||
 		(user != null && acquisitionProcess.getCurrentOwner() == user.getPerson())) {
 	    if (acquisitionProcess.getCurrentOwner() == null) { 
 		acquisitionProcess.takeProcess();
 	    }
 	    request.setAttribute("acquisitionProcess", acquisitionProcess);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : acquisitionProcess.getFinancersWithFundsAllocated()) {
 		if (financer.isProjectFinancer()) {
 		    final ProjectFinancer projectFinancer = (ProjectFinancer) financer;
 		    FundAllocationBean fundAllocationBean = new FundAllocationBean(projectFinancer);
 		    fundAllocationBean.setFundAllocationId(projectFinancer.getProjectFundAllocationId());
 		    fundAllocationBean.setEffectiveFundAllocationId(projectFinancer.getProjectFundAllocationId());
 		    fundAllocationBeans.add(fundAllocationBean);
 		}
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);	    
 	    return mapping.findForward("allocate.effective.project.funds");
 	} else {
 	    return viewAcquisitionProcess(mapping, request, acquisitionProcess);
 	}
     }
 
     public ActionForward allocateProjectFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "AllocateProjectFundsPermanently", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward addAllocationFundForProject(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	return addAllocationFundGeneric(mapping, request, "financerFundAllocationId", "allocate.effective.project.funds");
     }
 
     public ActionForward removeAllocationFundForProject(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	return removeAllocationFundGeneric(mapping, request, "financerFundAllocationId", "allocate.effective.project.funds");
     }
 
     public ActionForward executeAllocateFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final User user = UserView.getUser();
 	if (acquisitionProcess.getCurrentOwner() == null ||
 		(user != null && acquisitionProcess.getCurrentOwner() == user.getPerson())) {
 	    if (acquisitionProcess.getCurrentOwner() == null) { 
 		acquisitionProcess.takeProcess();
 	    }
 	    request.setAttribute("acquisitionProcess", acquisitionProcess);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : acquisitionProcess.getFinancersWithFundsAllocated()) {
 		FundAllocationBean fundAllocationBean = new FundAllocationBean(financer);
 		fundAllocationBean.setFundAllocationId(financer.getFundAllocationId());
 		fundAllocationBean.setEffectiveFundAllocationId(financer.getFundAllocationId());
 		fundAllocationBeans.add(fundAllocationBean);
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 
 	    return mapping.findForward("allocate.effective.funds");
 	} else {
 	    return viewAcquisitionProcess(mapping, request, acquisitionProcess);
 	}
     }
 
     private ActionForward addAllocationFundGeneric(final ActionMapping mapping, final HttpServletRequest request,
 	    String viewStateID, String forward) {
 
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	List<FundAllocationBean> fundAllocationBeans = getRenderedObject(viewStateID);
 	Integer index = Integer.valueOf(request.getParameter("index"));
 
 	Financer financer = getDomainObject(request, "financerOID");
 	FundAllocationBean fundAllocationBean = new FundAllocationBean(financer);
 	fundAllocationBean.setFundAllocationId(null);
 	fundAllocationBean.setEffectiveFundAllocationId(null);
 	fundAllocationBean.setAllowedToAddNewFund(false);
 
 	fundAllocationBeans.add(index + 1, fundAllocationBean);
 	request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	RenderUtils.invalidateViewState();
 	return mapping.findForward(forward);
     }
 
     private ActionForward removeAllocationFundGeneric(final ActionMapping mapping, final HttpServletRequest request,
 	    String viewStateID, String forward) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	List<FundAllocationBean> fundAllocationBeans = getRenderedObject(viewStateID);
 	int index = Integer.valueOf(request.getParameter("index")).intValue();
 
 	fundAllocationBeans.remove(index);
 	request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	RenderUtils.invalidateViewState();
 	return mapping.findForward(forward);
     }
 
     public ActionForward addAllocationFund(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	return addAllocationFundGeneric(mapping, request, "financerFundAllocationId", "allocate.effective.funds");
     }
 
     public ActionForward removeAllocationFund(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	return removeAllocationFundGeneric(mapping, request, "financerFundAllocationId", "allocate.effective.funds");
     }
 
     public ActionForward allocateFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	try {
 	    genericActivityExecution(acquisitionProcess, "AllocateFundsPermanently", fundAllocationBeans);
 	} catch (DomainException e) {
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	    request.setAttribute("acquisitionProcess", acquisitionProcess);
 	    addMessage(e.getMessage(), getBundle());
 	    return mapping.findForward("allocate.effective.funds");
 	}
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeUnApproveAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnApproveAcquisitionProcess");
     }
 
     public ActionForward executeEditAcquisitionRequestItemRealValues(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem acquisitionRequestItem = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionRequestItemBean itemBean = new AcquisitionRequestItemBean(acquisitionRequestItem);
 	request.setAttribute("itemBean", itemBean);
 	return mapping.findForward("edit.request.item.real.values");
     }
 
     public ActionForward executeAcquisitionRequestItemRealValuesEdition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject("acquisitionRequestItem");
 	genericActivityExecution(requestItemBean.getAcquisitionRequest().getAcquisitionProcess(),
 		"EditAcquisitionRequestItemRealValues", requestItemBean);
 	return viewAcquisitionProcess(mapping, request, (SimplifiedProcedureProcess) requestItemBean.getAcquisitionRequest()
 		.getAcquisitionProcess());
     }
 
     public ActionForward executeDistributeRealValuesForPayingUnits(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	List<UnitItemBean> beans = new ArrayList<UnitItemBean>();
 
 	for (UnitItem unitItem : item.getUnitItems()) {
 	    beans.add(new UnitItemBean(unitItem));
 	}
 	request.setAttribute("item", item);
 	request.setAttribute("unitItemBeans", beans);
 
 	return mapping.findForward("edit.real.shares.values");
     }
 
     public ActionForward executeDistributeRealValuesForPayingUnitsEdition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
	List<UnitItemBean> beans = getRenderedObject("beans");
 
 	try {
 	    return executeActivityAndViewProcess(mapping, form, request, response, "DistributeRealValuesForPayingUnits", beans,
 		    item);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	    request.setAttribute("item", item);
 	    request.setAttribute("beans", beans);
 	    return mapping.findForward("edit.real.shares.values");
 	}
     }
 
     @Override
     protected String getBundle() {
 	return "ACQUISITION_RESOURCES";
     }
 
     public ActionForward executeUnSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnSubmitForApproval");
     }
 
     public ActionForward executeRemoveFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveFundAllocation");
     }
 
     public ActionForward executeRemoveProjectFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveProjectFundAllocation");
     }
 
     public ActionForward executeRemoveFundsPermanentlyAllocated(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveFundsPermanentlyAllocated");
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
 
 	return mapping.findForward("edit.supplier");
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
 	User user = UserView.getUser();
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	Set<Financer> financersWithFundsAllocated = acquisitionProcess.getAcquisitionRequest()
 		.getAccountingUnitFinancerWithNoFundsAllocated(user.getPerson());
 	Set<ChangeFinancerAccountingUnitBean> financersBean = new HashSet<ChangeFinancerAccountingUnitBean>(
 		financersWithFundsAllocated.size());
 	for (Financer financer : financersWithFundsAllocated) {
 	    financersBean.add(new ChangeFinancerAccountingUnitBean(financer, financer.getAccountingUnit()));
 	}
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("financersBean", financersBean);
 	return mapping.findForward("change.financers.accounting.units");
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
 
     public ActionForward executeSetRefundee(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getProcess(request);
 	final SetRefundeeBean setRefundeeBean = new SetRefundeeBean(acquisitionProcess);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("setRefundeeBean", setRefundeeBean);
 	return mapping.findForward("set.refundee");
     }
 
     public ActionForward setRefundee(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
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
 
 }
