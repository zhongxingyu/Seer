 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.jasperreports.engine.JRException;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.ProjectFinancer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.SearchAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.UnitItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.DomainObjectBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationExpirationDateBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.ProcessStateBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.VariantBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean.CreateItemSchemaType;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.expenditureTrackingSystem.domain.processes.ProcessComment;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.ProcessAction;
 import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionProcess")
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
 	@Forward(name = "view.active.processes", path = "/acquisitions/viewActiveProcesses.jsp"),
 	@Forward(name = "view.my.processes", path = "/acquisitions/viewMyProcesses.jsp"),
 	@Forward(name = "select.unit.to.add", path = "/acquisitions/selectPayingUnitToAdd.jsp"),
 	@Forward(name = "remove.paying.units", path = "/acquisitions/removePayingUnits.jsp"),
 	@Forward(name = "edit.request.item", path = "/acquisitions/editRequestItem.jsp"),
 	@Forward(name = "edit.request.item.real.values", path = "/acquisitions/editRequestItemRealValues.jsp"),
 	@Forward(name = "assign.unit.item", path = "/acquisitions/assignUnitItem.jsp"),
 	@Forward(name = "edit.real.shares.values", path = "/acquisitions/editRealSharesValues.jsp"),
 	@Forward(name = "edit.supplier", path = "/acquisitions/editSupplierAddress.jsp"),
 	@Forward(name = "execute.payment", path = "/acquisitions/executePayment.jsp"), 
 	@Forward(name = "view.comments", path = "/acquisitions/viewComments.jsp")
 })
 public class SimplifiedProcedureProcessAction extends ProcessAction {
 
     private static final Context CONTEXT = new Context("acquisitions");
 
     @Override
     protected Context getContextModule(final HttpServletRequest request) {
 	return CONTEXT;
     }
 
     public static class AcquisitionProposalDocumentForm extends FileUploadBean {
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
     protected GenericProcess getProcess(final HttpServletRequest request) {
 	return getProcess(request, "acquisitionProcessOid");
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
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final SimplifiedProcedureProcess acquisitionProcess) {
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("view.acquisition.process");
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeDeleteAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	genericActivityExecution(request, "DeleteAcquisitionProcess");
 	return showPendingProcesses(mapping, form, request, response);
     }
 
     public ActionForward searchAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SearchAcquisitionProcess searchAcquisitionProcess = getRenderedObject();
 	if (searchAcquisitionProcess == null) {
 	    searchAcquisitionProcess = new SearchAcquisitionProcess();
 	}
 	request.setAttribute("searchAcquisitionProcess", searchAcquisitionProcess);
 	return mapping.findForward("search.acquisition.process");
     }
 
     public ActionForward executeAddAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final AcquisitionProposalDocumentForm acquisitionProposalDocumentForm = new AcquisitionProposalDocumentForm();
 	request.setAttribute("acquisitionProposalDocumentForm", acquisitionProposalDocumentForm);
 	return mapping.findForward("add.acquisition.proposal.document");
     }
 
     public ActionForward addAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final AcquisitionProposalDocumentForm acquisitionProposalDocumentForm = getRenderedObject();
 	final String filename = acquisitionProposalDocumentForm.getFilename();
 	final byte[] bytes = consumeInputStream(acquisitionProposalDocumentForm);
 	acquisitionProcess.getActivityByName("AddAcquisitionProposalDocument").execute(acquisitionProcess, filename, bytes);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward downloadAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 	final AcquisitionProposalDocument acquisitionProposalDocument = getDomainObject(request, "acquisitionProposalDocumentOid");
 	return download(response, acquisitionProposalDocument);
     }
 
     public ActionForward downloadAcquisitionRequestDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 	final AcquisitionRequestDocument acquisitionRequestDocument = getDomainObject(request, "acquisitionRequestDocumentOid");
 	return download(response, acquisitionRequestDocument);
     }
 
     public ActionForward executeCreateAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	return executeCreateAcquisitionRequestItem(mapping, request, acquisitionProcess);
     }
 
     private ActionForward executeCreateAcquisitionRequestItem(final ActionMapping mapping, final HttpServletRequest request,
 	    SimplifiedProcedureProcess acquisitionProcess) {
 	request.setAttribute("bean", new AcquisitionRequestItemBean(acquisitionProcess.getAcquisitionRequest()));
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("create.acquisition.request.item");
     }
 
     public ActionForward createNewAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject();
 
 	SimplifiedProcedureProcess acquisitionProcess = (SimplifiedProcedureProcess) requestItemBean.getAcquisitionRequest()
 		.getAcquisitionProcess();
 	AbstractActivity<AcquisitionProcess> activity = acquisitionProcess.getActivityByName("CreateAcquisitionRequestItem");
 	try {
 	    activity.execute(acquisitionProcess, requestItemBean);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle(), e.getArgs());
 	    return executeCreateAcquisitionRequestItem(mapping, request, acquisitionProcess);
 	}
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForApproval");
     }
 
     public ActionForward executeApproveAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	User user = UserView.getUser();
 	return executeActivityAndViewProcess(mapping, form, request, response, "ApproveAcquisitionProcess", user.getPerson());
     }
 
     public ActionForward executeSubmitForFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	User user = UserView.getUser();
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForFundAllocation", user.getPerson());
     }
 
     public ActionForward executeProjectFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	for (Financer financer : acquisitionProcess.getProjectFinancersWithFundsAllocated()) {
 	    fundAllocationBeans.add(new FundAllocationBean(financer));
 	}
 	request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	return mapping.findForward("allocate.project.funds");
     }
 
     public ActionForward allocateProjectFunds(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "ProjectFundAllocation", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	for (Financer financer : acquisitionProcess.getFinancersWithFundsAllocated()) {
 	    fundAllocationBeans.add(new FundAllocationBean(financer));
 	}
 	request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	return mapping.findForward("allocate.funds");
     }
 
     public ActionForward allocateFunds(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "FundAllocation", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final FundAllocationExpirationDateBean fundAllocationExpirationDateBean = new FundAllocationExpirationDateBean();
 	request.setAttribute("fundAllocationExpirationDateBean", fundAllocationExpirationDateBean);
 	return mapping.findForward("allocate.funds.to.service.provider");
     }
 
     public ActionForward allocateFundsToServiceProvider(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final FundAllocationExpirationDateBean fundAllocationExpirationDateBean = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "FundAllocationExpirationDate", fundAllocationExpirationDateBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeCreateAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("uploadFile", new FileUploadBean());
 	return mapping.findForward("prepare.create.acquisition.request");
     }
 
     public ActionForward showPendingProcesses(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	List<AcquisitionProcess> processes = new ArrayList<AcquisitionProcess>();
 
 	for (SimplifiedProcedureProcess process : GenericProcess.getAllProcesses(SimplifiedProcedureProcess.class)) {
 	    if (process.isPersonAbleToExecuteActivities()) {
 		processes.add((AcquisitionProcess) process);
 	    }
 	}
 	request.setAttribute("activeProcesses", processes);
 
 	return mapping.findForward("view.active.processes");
     }
 
     public ActionForward showMyProcesses(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	Person person = getLoggedPerson();
 	request.setAttribute("processes", person.getAcquisitionProcesses());
 
 	return mapping.findForward("view.my.processes");
     }
 
     public ActionForward createAcquisitionRequestDocument(ActionMapping mapping, ActionForm actionForm,
 	    HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 
 	AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 
 	AbstractActivity<AcquisitionProcess> createAquisitionRequest = acquisitionProcess
 		.getActivityByName("CreateAcquisitionRequest");
 	createAquisitionRequest.execute(acquisitionProcess);
 	AcquisitionRequestDocument acquisitionRequestDocument = acquisitionRequest.getAcquisitionRequestDocument();
 
 	download(response, acquisitionRequestDocument);
 
 	return null;
     }
 
     public ActionForward addAcquisitionRequestDocument(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
 	    HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	FileUploadBean fileUploadBean = getRenderedObject("acquisitionRequestDocument");
 	RenderUtils.invalidateViewState();
 
 	AbstractActivity<AcquisitionProcess> createAquisitionRequest = acquisitionProcess
 		.getActivityByName("CreateAcquisitionRequest");
 	byte[] content = consumeInputStream(fileUploadBean);
 	createAquisitionRequest.execute(acquisitionProcess, content, fileUploadBean.getFilename());
 
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
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
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
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
	return processInvoiceData(mapping, request, "CreateAcquisitionRequest.ReceiveInvoice");
     }
 
     public ActionForward updateInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return processInvoiceData(mapping, request, "FixInvoice");
     }
 
     private ActionForward processInvoiceData(final ActionMapping mapping, final HttpServletRequest request, String activity) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final ReceiveInvoiceForm receiveInvoiceForm = getRenderedObject();
 	final byte[] bytes = consumeInputStream(receiveInvoiceForm);
 	AbstractActivity<AcquisitionProcess> receiveInvoice = acquisitionProcess.getActivityByName(activity);
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
 	AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
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
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	for (Financer financer : acquisitionProcess.getFinancersWithFundsAllocated()) {
 	    final ProjectFinancer projectFinancer = (ProjectFinancer) financer;
 	    FundAllocationBean fundAllocationBean = new FundAllocationBean(projectFinancer);
 	    fundAllocationBean.setFundAllocationId(projectFinancer.getProjectFundAllocationId());
 	    fundAllocationBean.setEffectiveFundAllocationId(projectFinancer.getProjectFundAllocationId());
 	    fundAllocationBeans.add(fundAllocationBean);
 	}
 	request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 
 	return mapping.findForward("allocate.effective.project.funds");
     }
 
     public ActionForward allocateProjectFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "AllocateProjectFundsPermanently", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeAllocateFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
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
     }
 
     public ActionForward allocateFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(acquisitionProcess, "AllocateFundsPermanently", fundAllocationBeans);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeUnApproveAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnApproveAcquisitionProcess");
     }
 
     public ActionForward executeAddPayingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("domainObjectBean", new DomainObjectBean<Unit>());
 	return mapping.findForward("select.unit.to.add");
     }
 
     public ActionForward addPayingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DomainObjectBean<Unit> bean = getRenderedObject("unitToAdd");
 	List<Unit> units = new ArrayList<Unit>();
 	units.add(bean.getDomainObject());
 	return executeActivityAndViewProcess(mapping, form, request, response, "AddPayingUnit", units);
     }
 
     public ActionForward executeRemovePayingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("payingUnits", acquisitionProcess.getPayingUnits());
 	return mapping.findForward("remove.paying.units");
     }
 
     public ActionForward removePayingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final Unit payingUnit = getDomainObject(request, "unitOID");
 	List<Unit> units = new ArrayList<Unit>();
 	units.add(payingUnit);
 	try {
 	    genericActivityExecution(request, "RemovePayingUnit", units);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	}
 	return executeRemovePayingUnit(mapping, form, request, response);
     }
 
     public ActionForward executeRejectAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("stateBean", new ProcessStateBean());
 	return mapping.findForward("reject.acquisition.process");
     }
 
     public ActionForward rejectAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final ProcessStateBean stateBean = getRenderedObject();
 	try {
 	    genericActivityExecution(request, "RejectAcquisitionProcess", stateBean.getJustification());
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	}
 	return viewAcquisitionProcess(mapping, request, (SimplifiedProcedureProcess) getDomainObject(request,
 		"acquisitionProcessOid"));
     }
 
     public ActionForward executeDeleteAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	SimplifiedProcedureProcess acquisitionProcess = (SimplifiedProcedureProcess) item.getAcquisitionRequest()
 		.getAcquisitionProcess();
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	genericActivityExecution(acquisitionProcess, "DeleteAcquisitionRequestItem", item);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeEditAcquisitionRequestItemPostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionRequestItemBean itemBean = getRenderedObject("acquisitionRequestItem");
 	request.setAttribute("itemBean", itemBean);
 	RenderUtils.invalidateViewState("acquisitionRequestItem");
 	return mapping.findForward("edit.request.item");
     }
 
     public ActionForward executeEditAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem acquisitionRequestItem = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionRequestItemBean itemBean = new AcquisitionRequestItemBean(acquisitionRequestItem);
 	request.setAttribute("itemBean", itemBean);
 	return mapping.findForward("edit.request.item");
     }
 
     public ActionForward executeAcquisitionRequestItemEdition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject("acquisitionRequestItem");
 	try {
 	    genericActivityExecution(requestItemBean.getAcquisitionRequest().getAcquisitionProcess(),
 		    "EditAcquisitionRequestItem", requestItemBean);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle(), e.getArgs());
 	    return executeEditAcquisitionRequestItem(mapping, form, request, response);
 	}
 	return viewAcquisitionProcess(mapping, request, (SimplifiedProcedureProcess) requestItemBean.getAcquisitionRequest()
 		.getAcquisitionProcess());
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
 	request.setAttribute("beans", beans);
 
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
 
     public ActionForward executeAssignPayingUnitToItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	List<UnitItemBean> beans = new ArrayList<UnitItemBean>();
 	for (Unit unit : acquisitionProcess.getPayingUnits()) {
 	    beans.add(new UnitItemBean(unit, item));
 	}
 	request.setAttribute("acquisitionRequestItem", item);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("unitItemBeans", beans);
 
 	return mapping.findForward("assign.unit.item");
     }
 
     public ActionForward executeAssignPayingUnitToItemCreation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final SimplifiedProcedureProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 
 	List<UnitItemBean> beans = getRenderedObject("unitItemBeans");
 	try {
 	    genericActivityExecution(acquisitionProcess, "AssignPayingUnitToItem", item, beans);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	    return executeAssignPayingUnitToItem(mapping, form, request, response);
 	}
 
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward calculateShareValuePostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 
 	List<UnitItemBean> beans = getRenderedObject("unitItemBeans");
 	int assigned = 0;
 	for (UnitItemBean bean : beans) {
 	    if (bean.getAssigned()) {
 		assigned++;
 	    }
 	}
 	if (assigned != 0) {
 	    Money[] shareValues;
 	    shareValues = item.getTotalItemValueWithAdditionalCostsAndVat().allocate(assigned);
 
 	    int i = 0;
 	    for (UnitItemBean bean : beans) {
 		if (bean.getAssigned()) {
 		    bean.setShareValue(shareValues[i++]);
 		} else {
 		    bean.setShareValue(null);
 		}
 	    }
 	}
 	request.setAttribute("acquisitionRequestItem", item);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("unitItemBeans", beans);
 
 	RenderUtils.invalidateViewState();
 	return mapping.findForward("assign.unit.item");
     }
 
     public ActionForward executeActivityAndViewProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response, final String activityName) {
 	genericActivityExecution(request, activityName);
 	return viewAcquisitionProcess(mapping, form, request, response);
     }
 
     public ActionForward executeActivityAndViewProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response, final String activityName, Object... args) {
 
 	genericActivityExecution(request, activityName, args);
 	return viewAcquisitionProcess(mapping, form, request, response);
     }
 
     public ActionForward createItemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	AcquisitionRequestItemBean acquisitionRequestItemBean = getRenderedObject("acquisitionRequestItem");
 	RenderUtils.invalidateViewState();
 	acquisitionRequestItemBean.setRecipient(null);
 	acquisitionRequestItemBean.setAddress(null);
 
 	if (acquisitionRequestItemBean.getItem() != null
 		&& acquisitionRequestItemBean.getCreateItemSchemaType().equals(CreateItemSchemaType.EXISTING_DELIVERY_INFO)) {
 	    acquisitionRequestItemBean.setDeliveryInfo(acquisitionRequestItemBean.getAcquisitionRequest().getRequester()
 		    .getDeliveryInfoByRecipientAndAddress(acquisitionRequestItemBean.getItem().getRecipient(),
 			    acquisitionRequestItemBean.getItem().getAddress()));
 	} else {
 	    acquisitionRequestItemBean.setDeliveryInfo(null);
 	}
 
 	request.setAttribute("bean", acquisitionRequestItemBean);
 	request.setAttribute("acquisitionProcess", acquisitionRequestItemBean.getAcquisitionRequest().getAcquisitionProcess());
 	return mapping.findForward("create.acquisition.request.item");
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
 
     public ActionForward executeRemoveFundsPermanentlyAllocated(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveFundsPermanentlyAllocated");
     }
 
     public ActionForward executeRemoveFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemoveFundAllocationExpirationDate");
     }
 
     public ActionForward executeCancelAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "CancelAcquisitionRequest");
     }
 
     public ActionForward executeCancelRemoveFundAllocationExpirationDate(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "CancelRemoveFundAllocationExpirationDate");
     }
 
     public ActionForward editSupplierAddress(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 
 	return mapping.findForward("edit.supplier");
     }
 
     public ActionForward executeSendAcquisitionRequestToSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SendAcquisitionRequestToSupplier");
     }
 
     public ActionForward executeSubmitForConfirmInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForConfirmInvoice");
     }
 
     
     public ActionForward viewComments(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	
 	AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	
 	Set<ProcessComment> comments = new TreeSet<ProcessComment>  (ProcessComment.COMPARATOR);
 	comments.addAll(acquisitionProcess.getComments());
 	
 	request.setAttribute("comments", comments);
 	request.setAttribute("bean", new VariantBean());
 	return mapping.findForward("view.comments");
     }
     
     public ActionForward addComment(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	String comment = getRenderedObject("comment");
 	AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	acquisitionProcess.createComment(getLoggedPerson(), comment);
 
 	RenderUtils.invalidateViewState("comment");
 	return viewComments(mapping, form, request, response);
     }
 }
