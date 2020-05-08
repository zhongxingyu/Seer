 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.SearchRefundProcesses;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateRefundProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.EditRefundInvoiceBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.RefundInvoiceBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.RefundItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.VariantBean;
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionRefundProcess")
 @Forwards( { @Forward(name = "create.refund.process", path = "/acquisitions/refund/createRefundRequest.jsp"),
 	@Forward(name = "view.refund.process", path = "/acquisitions/refund/viewRefundRequest.jsp"),
 	@Forward(name = "search.refund.process", path = "/acquisitions/refund/searchRefundRequest.jsp"),
 	@Forward(name = "create.refund.item", path = "/acquisitions/refund/createRefundItem.jsp"),
 	@Forward(name = "edit.refund.item", path = "/acquisitions/refund/editRefundItem.jsp"),
 	@Forward(name = "view.comments", path = "/acquisitions/viewComments.jsp"),
 	@Forward(name = "generic.upload", path = "/acquisitions/genericUpload.jsp"),
 	@Forward(name = "select.unit.to.add", path = "/acquisitions/commons/selectPayingUnitToAdd.jsp"),
 	@Forward(name = "assign.unit.item", path = "/acquisitions/commons/assignUnitItem.jsp"),
 	@Forward(name = "edit.real.shares.values", path = "/acquisitions/commons/assignUnitItemRealValues.jsp"),
 	@Forward(name = "remove.paying.units", path = "/acquisitions/commons/removePayingUnits.jsp"),
 	@Forward(name = "allocate.project.funds", path = "/acquisitions/commons/allocateProjectFunds.jsp"),
 	@Forward(name = "allocate.funds", path = "/acquisitions/commons/allocateFunds.jsp"),
 	@Forward(name = "add.refund.invoice", path = "/acquisitions/refund/addRefundInvoice.jsp"),
 	@Forward(name = "remove.refund.invoice", path = "/acquisitions/refund/removeRefundInvoice.jsp"),
 	@Forward(name = "edit.refund.invoice", path = "/acquisitions/refund/editRefundInvoice.jsp"),
 	@Forward(name = "allocate.effective.project.funds", path = "/acquisitions/commons/allocateEffectiveProjectFunds.jsp"),
 	@Forward(name = "allocate.effective.funds", path = "/acquisitions/commons/allocateEffectiveFunds.jsp"),
 	@Forward(name = "execute.payment", path = "/acquisitions/refund/executePayment.jsp") })
 public class RefundProcessAction extends PaymentProcessAction {
 
     private static final Context CONTEXT = new Context("acquisitions");
 
     @Override
     protected Context getContextModule(final HttpServletRequest request) {
 	return CONTEXT;
     }
 
     public ActionForward viewRefundProcess(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	RefundProcess process = getProcess(request);
 	return viewRefundProcess(mapping, request, process);
     }
 
     public ActionForward viewRefundProcess(final ActionMapping mapping, final HttpServletRequest request, RefundProcess process) {
 
 	request.setAttribute("refundProcess", process);
 	return mapping.findForward("view.refund.process");
     }
 
     public ActionForward prepareCreateRefundProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CreateRefundProcessBean bean = new CreateRefundProcessBean(getLoggedPerson());
 	request.setAttribute("bean", bean);
 	return mapping.findForward("create.refund.process");
     }
 
     public ActionForward createRefundProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CreateRefundProcessBean bean = getRenderedObject("createRefundProcess");
 	RefundProcess process = RefundProcess.createNewRefundProcess(bean);
 	return viewRefundProcess(mapping, request, process);
 
     }
 
     public ActionForward createRefundProcessPostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CreateRefundProcessBean bean = getRenderedObject("createRefundProcess");
 	request.setAttribute("bean", bean);
 	RenderUtils.invalidateViewState("createRefundProcess");
 	return mapping.findForward("create.refund.process");
 
     }
 
     public ActionForward showPendingProcesses(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SearchRefundProcesses searchRefundProcess = new SearchRefundProcesses();
 	searchRefundProcess.setHasAvailableAndAccessibleActivityForUser(Boolean.TRUE);
 	return searchRefundProcess(mapping, request, searchRefundProcess);
     }
 
     public ActionForward showMyProcesses(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SearchRefundProcesses searchRefundProcess = new SearchRefundProcesses();
 	searchRefundProcess.setRequestingPerson(getLoggedPerson());
 	return searchRefundProcess(mapping, request, searchRefundProcess);
     }
 
     public ActionForward searchRefundProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SearchRefundProcesses searchRefundProcess = getRenderedObject();
 	if (searchRefundProcess == null) {
 	    searchRefundProcess = new SearchRefundProcesses();
 	}
 	return searchRefundProcess(mapping, request, searchRefundProcess);
     }
 
     public ActionForward searchRefundProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final SearchRefundProcesses searchRefundProcess) {
 	request.setAttribute("searchRefundProcess", searchRefundProcess);
 	return mapping.findForward("search.refund.process");
     }
 
     public ActionForward executeCreateRefundItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RefundItemBean bean = new RefundItemBean();
 	request.setAttribute("bean", bean);
 	request.setAttribute("refundProcess", getProcess(request));
 
 	return mapping.findForward("create.refund.item");
 
     }
 
     public ActionForward actualCreationRefundItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	RefundItemBean bean = getRenderedObject("refundItemBean");
 	RefundProcess process = getProcess(request);
 	genericActivityExecution(process, "CreateRefundItem", bean);
 
 	return viewRefundProcess(mapping, request, process);
     }
 
     public ActionForward executeEditRefundItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RefundItem item = getDomainObject(request, "refundItemOid");
 	RefundProcess process = getProcess(request);
 	RefundItemBean bean = new RefundItemBean(item);
 	request.setAttribute("refundProcess", process);
 	request.setAttribute("refundItem", item);
 	request.setAttribute("bean", bean);
 	return mapping.findForward("edit.refund.item");
     }
 
     public ActionForward actualEditRefundItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RefundItemBean bean = getRenderedObject("refundItemBean");
 	RefundItem item = getDomainObject(request, "refundItemOid");
 	RefundProcess process = getProcess(request);
 	genericActivityExecution(request, "EditRefundItem", item, bean);
 	return viewRefundProcess(mapping, request, process);
     }
 
     public ActionForward executeDeleteRefundItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RefundItem item = getDomainObject(request, "refundItemOid");
 	RefundProcess process = getProcess(request);
 	genericActivityExecution(process, "DeleteRefundItem", item);
 	return viewRefundProcess(mapping, request, process);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected RefundProcess getProcess(HttpServletRequest request) {
 	RefundProcess process = getDomainObject(request, "refundProcessOid");
 	if (process == null) {
 	    process = getDomainObject(request, "processOid");
 	}
 	return process;
     }
 
     @Override
     public ActionForward viewProcess(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) {
 	return viewRefundProcess(mapping, request, getProcess(request));
     }
 
     public ActionForward executeSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForApproval");
     }
 
     public ActionForward executeUnSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnSubmitForApproval");
     }
 
     @Override
     protected RefundItem getRequestItem(HttpServletRequest request) {
 	RefundItem item = getDomainObject(request, "refundItemOid");
 	return item != null ? item : (RefundItem) super.getRequestItem(request);
     }
 
     public ActionForward executeApprove(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "Approve");
     }
 
     public ActionForward executeUnApprove(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnApprove");
     }
 

     public ActionForward executeUnSubmitForFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "UnSubmitForFundAllocation");
     }
 
     public ActionForward executeCreateRefundInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	RefundItem item = getRequestItem(request);
 	RefundInvoiceBean bean = new RefundInvoiceBean();
 	bean.setItem(item);
 
 	request.setAttribute("bean", bean);
 	return mapping.findForward("add.refund.invoice");
     }
 
     public ActionForward createRefundInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	RefundProcess process = getProcess(request);
 	RefundInvoiceBean bean = getRenderedObject("bean");
 
 	if (bean.getInputStream() == null) {
 	    addMessage("refundItem.message.info.mustAddInvoiceFile", getBundle());
 	    request.setAttribute("bean", bean);
 	    RenderUtils.invalidateViewState("bean");
 	    return mapping.findForward("add.refund.invoice");
 	}
	
 	byte[] fileBytes = consumeInputStream(bean);
 	try {
 	    genericActivityExecution(process, "CreateRefundInvoice", bean, fileBytes);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	    request.setAttribute("bean", bean);
 	    RenderUtils.invalidateViewState("bean");
 	    return mapping.findForward("add.refund.invoice");
 	}
 
 	return viewProcess(mapping, form, request, response);
     }
 
     public ActionForward executeSubmitForInvoiceConfirmation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForInvoiceConfirmation");
     }
 
     public ActionForward executeConfirmInvoices(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "ConfirmInvoices");
     }
 
     public ActionForward downloadInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 	RefundInvoice invoice = getDomainObject(request, "invoiceOID");
 	download(response, invoice.getFile());
 	return null;
     }
 
     public ActionForward executeRemoveRefundInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RequestItem item = getRequestItem(request);
 	request.setAttribute("item", item);
 	return mapping.findForward("remove.refund.invoice");
     }
 
     public ActionForward removeRefundInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	RefundInvoice invoice = getDomainObject(request, "invoiceOid");
 	RefundProcess process = getProcess(request);
 	genericActivityExecution(process, "RemoveRefundInvoice", invoice);
 	return executeRemoveRefundInvoice(mapping, form, request, response);
     }
 
     public ActionForward executeEditRefundInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RefundItem item = getRequestItem(request);
 	request.setAttribute("item", item);
 
 	List<EditRefundInvoiceBean> beans = new ArrayList<EditRefundInvoiceBean>();
 	for (RefundInvoice invoice : item.getInvoices()) {
 	    beans.add(new EditRefundInvoiceBean(invoice));
 	}
 	request.setAttribute("invoices", beans);
 	return mapping.findForward("edit.refund.invoice");
     }
 
     public ActionForward editRefundInvoice(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	List<EditRefundInvoiceBean> beans = getRenderedObject("invoiceBeans");
 	RefundProcess process = getProcess(request);
 	try {
 	    genericActivityExecution(process, "EditRefundInvoice", beans);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
	    return executeEditRefundInvoice(mapping, form, request, response);
 	}
 	return viewProcess(mapping, form, request, response);
     }
 
     public ActionForward executeRefundPerson(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final RefundProcess refundProcess = getProcess(request);
 	VariantBean bean = new VariantBean();
 	request.setAttribute("bean", bean);
 	request.setAttribute("process", refundProcess);
 	return mapping.findForward("execute.payment");
     }
 
     public ActionForward executeRefundPersonAction(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	String paymentReference = getRenderedObject("reference");
 	return executeActivityAndViewProcess(mapping, form, request, response, "RefundPerson", paymentReference);
     }
 }
