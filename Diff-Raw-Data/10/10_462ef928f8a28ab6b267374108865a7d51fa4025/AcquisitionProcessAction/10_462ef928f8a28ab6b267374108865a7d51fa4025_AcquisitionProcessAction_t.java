 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.jasperreports.engine.JRException;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProposalDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.SearchAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.DomainObjectBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.ProcessAction;
 import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
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
 	@Forward(name = "edit.acquisition.request.item", path = "/acquisitions/editAcquisitionRequestItem.jsp"),
 	@Forward(name = "allocate.funds", path = "/acquisitions/allocateFunds.jsp"),
 	@Forward(name = "allocate.funds.to.service.provider", path = "/acquisitions/allocateFundsToServiceProvider.jsp"),
 	@Forward(name = "prepare.create.acquisition.request", path = "/acquisitions/createAcquisitionRequest.jsp"),
 	@Forward(name = "receive.invoice", path = "/acquisitions/receiveInvoice.jsp"),
 	@Forward(name = "view.active.processes", path = "/acquisitions/viewActiveProcesses.jsp"),
 	@Forward(name = "select.unit.to.add", path = "/acquisitions/selectUnitToAdd.jsp"),
 	@Forward(name = "remove.paying.units", path = "/acquisitions/removePayingUnits.jsp"),
 	@Forward(name = "delete.request.item", path = "/acquisitions/deleteRequestItems.jsp"), 
 	@Forward(name = "edit.request.items", path = "/acquisitions/editRequestItems.jsp"),
 	@Forward(name = "edit.request.item", path = "/acquisitions/editRequestItem.jsp")
 })
 public class AcquisitionProcessAction extends ProcessAction {
 
     private static final Context CONTEXT = new Context("acquisitions");
 
     @Override
     protected Context getContextModule() {
 	return CONTEXT;
     }
 
     public static class AcquisitionProposalDocumentForm extends FileUploadBean {
     }
 
     public static class ReceiveInvoiceForm extends FileUploadBean {
 	private String invoiceNumber;
 	private DateTime invoiceDate;
 
 	public String getInvoiceNumber() {
 	    return invoiceNumber;
 	}
 
 	public void setInvoiceNumber(String invoiceNumber) {
 	    this.invoiceNumber = invoiceNumber;
 	}
 
 	public DateTime getInvoiceDate() {
 	    return invoiceDate;
 	}
 
 	public void setInvoiceDate(DateTime invoiceDate) {
 	    this.invoiceDate = invoiceDate;
 	}
     }
 
     public ActionForward prepareCreateAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateAcquisitionProcessBean acquisitionProcessBean = new CreateAcquisitionProcessBean();
 	User user = UserView.getUser();
 	if (user != null && user.getPerson() != null) {
 	    acquisitionProcessBean.setRecipient(user.getPerson().getName());
 	}
 	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
 	return mapping.findForward("create.acquisition.process");
     }
 
     public ActionForward createNewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	CreateAcquisitionProcessBean createAcquisitionProcessBean = getRenderedObject();
 	User user = UserView.getUser();
 	createAcquisitionProcessBean.setRequester(user != null ? user.getPerson() : null);
 	final AcquisitionProcess acquisitionProcess = AcquisitionProcess
 		.createNewAcquisitionProcess(createAcquisitionProcessBean);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward editAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	return editAcquisitionRequest(mapping, request, acquisitionProcess);
     }
 
     protected ActionForward editAcquisitionRequest(final ActionMapping mapping, final HttpServletRequest request,
 	    final AcquisitionProcess acquisitionProcess) {
 	final AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 	request.setAttribute("acquisitionRequest", acquisitionRequest);
 	return mapping.findForward("edit.request.acquisition");
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final AcquisitionProcess acquisitionProcess) {
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("view.acquisition.process");
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
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
 	    final User user = UserView.getUser();
 	    if (user != null) {
 		searchAcquisitionProcess.setRequester(user.getUsername());
 	    }
 	    searchAcquisitionProcess.setAcquisitionProcessState(AcquisitionProcessStateType.IN_GENESIS);
 	}
 	request.setAttribute("searchAcquisitionProcess", searchAcquisitionProcess);
 	return mapping.findForward("search.acquisition.process");
     }
 
     public ActionForward executeAddAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final AcquisitionProposalDocumentForm acquisitionProposalDocumentForm = new AcquisitionProposalDocumentForm();
 	request.setAttribute("acquisitionProposalDocumentForm", acquisitionProposalDocumentForm);
 	return mapping.findForward("add.acquisition.proposal.document");
     }
 
     public ActionForward addAcquisitionProposalDocument(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
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
 	AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("bean", new AcquisitionRequestItemBean(acquisitionProcess.getAcquisitionRequest()));
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("create.acquisition.request.item");
     }
 
     public ActionForward createNewAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject();
 
 	AcquisitionProcess acquisitionProcess = requestItemBean.getAcquisitionRequest().getAcquisitionProcess();
 	AbstractActivity<AcquisitionProcess> activity = acquisitionProcess.getActivityByName("CreateAcquisitionRequestItem");
 	activity.execute(acquisitionProcess, requestItemBean);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     protected ActionForward editAcquisitionRequestItem(final ActionMapping mapping, final HttpServletRequest request,
 	    final AcquisitionRequestItem acquisitionRequestItem) {
 	final AcquisitionProcess acquisitionProcess = acquisitionRequestItem.getAcquisitionRequest().getAcquisitionProcess();
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("acquisitionRequestItem", acquisitionRequestItem);
 	return mapping.findForward("edit.acquisition.request.item");
     }
 
     public ActionForward executeSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForApproval");
     }
 
     public ActionForward executeApproveAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "ApproveAcquisitionProcess");
     }
 
     public ActionForward executeFundAllocation(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("allocate.funds");
     }
 
     public ActionForward allocateFundsToServiceProvider(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("allocate.funds.to.service.provider");
     }
 
     public ActionForward executeCreateAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("prepare.create.acquisition.request");
     }
 
     public ActionForward showPendingProcesses(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	List<AcquisitionProcess> processes = new ArrayList<AcquisitionProcess>();
 
 	for (AcquisitionProcess process : GenericProcess.getAllProcesses(AcquisitionProcess.class)) {
 	    if (process.isPersonAbleToExecuteActivities()) {
 		processes.add((AcquisitionProcess) process);
 	    }
 	}
 	request.setAttribute("activeProcesses", processes);
 
 	return mapping.findForward("view.active.processes");
     }
 
     public ActionForward createAcquisitionRequestDocument(ActionMapping mapping, ActionForm actionForm,
 	    HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 
 	AcquisitionRequest acquisitionRequest = acquisitionProcess.getAcquisitionRequest();
 
 	AcquisitionRequestDocument acquisitionRequestDocument = acquisitionRequest != null ? acquisitionRequest
 		.getAcquisitionRequestDocument() : null;
 
 	if (acquisitionRequestDocument == null) {
 	    AbstractActivity<AcquisitionProcess> createAquisitionRequest = acquisitionProcess
 		    .getActivityByName("CreateAcquisitionRequest");
 	    createAquisitionRequest.execute(acquisitionProcess);
 	    acquisitionRequestDocument = acquisitionRequest.getAcquisitionRequestDocument();
 	}
 
 	download(response, acquisitionRequestDocument);
 
 	ActionForward findForward = mapping.findForward("prepare.create.acquisition.request");
 	findForward.setRedirect(true);
 	return findForward;
     }
 
     public ActionForward executeReceiveInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
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
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	final ReceiveInvoiceForm receiveInvoiceForm = getRenderedObject();
 	final byte[] bytes = consumeInputStream(receiveInvoiceForm);
 	AbstractActivity<AcquisitionProcess> receiveInvoice = acquisitionProcess.getActivityByName("ReceiveInvoice");
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
 	return executeActivityAndViewProcess(mapping, form, request, response, "ConfirmInvoice");
     }
 
     public ActionForward executePayAcquisition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "PayAcquisition");
     }
 
     public ActionForward executeAllocateFundsPermanently(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "AllocateFundsPermanently");
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
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("payingUnits", acquisitionProcess.getPayingUnits());
 	return mapping.findForward("remove.paying.units");
     }
 
     public ActionForward removePayingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final Unit payingUnit = getDomainObject(request, "unitOID");
 	List<Unit> units = new ArrayList<Unit>();
 	units.add(payingUnit);
 	return executeActivityAndViewProcess(mapping, form, request, response, "RemovePayingUnit", units);
     }
 
     public ActionForward executeRejectAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "RejectAcquisitionProcess");
     }
 
     public ActionForward executeDeleteAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("delete.request.item");
     }
 
     public ActionForward deleteAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionProcess acquisitionProcess = item.getAcquisitionRequest().getAcquisitionProcess();
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	genericActivityExecution(acquisitionProcess, "DeleteAcquisitionRequestItem", item);
 	return acquisitionProcess.getAcquisitionRequest().getAcquisitionRequestItemsCount() > 0 ? mapping
 		.findForward("delete.request.item") : viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeEditAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionProcess acquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("edit.request.items");
     }
 
     public ActionForward editAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem acquisitionRequestItem = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionRequestItemBean itemBean = new AcquisitionRequestItemBean(acquisitionRequestItem);
 	request.setAttribute("itemBean", itemBean);
 	return mapping.findForward("edit.request.item");
     }
 
     public ActionForward executeAcquisitionRequestItemEdition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject("itemBean");
 	genericActivityExecution(requestItemBean.getAcquisitionRequest().getAcquisitionProcess(), "EditAcquisitionRequestItem",
 		requestItemBean.getItem(), requestItemBean.getDescription(), requestItemBean.getQuantity(), requestItemBean
 			.getUnitValue(), requestItemBean.getProposalReference(), requestItemBean.getSalesCode());
 	return executeEditAcquisitionRequestItem(mapping, form, request, response);
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
 
 }
