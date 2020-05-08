 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.DateIntervalBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.ProcessStateBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.AcquisitionRequestItemBean.CreateItemSchemaType;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.fenixWebFramework.renderers.components.state.IViewState;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionProcess")
 public class RegularAcquisitionProcessAction extends PaymentProcessAction {
 
     @Override
     protected String getSelectUnitToAddForwardUrl() {
 	throw new Error("not.implemented");
     }
 
     @Override
     protected String getRemovePayingUnitsForwardUrl() {
 	throw new Error("not.implemented");
     }
 
     @Override
     protected String getAssignUnitItemForwardUrl() {
 	throw new Error("not.implemented");
     }
 
     @Override
     protected String getEditRealShareValuesForwardUrl() {
 	throw new Error("not.implemented");
     }
 
     @Override
     public ActionForward viewProcess(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) {
 	return viewAcquisitionProcess(mapping, request, getProcess(request));
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final AcquisitionProcess acquisitionProcess) {
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return forward(request, "/acquisitions/viewAcquisitionProcess.jsp");
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final RegularAcquisitionProcess acquisitionProcess = getProcess(request);
 	return redirectToProcessPage(request, acquisitionProcess);
     }
 
     public ActionForward executeCancelAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("confirmCancelAcquisitionProcess", Boolean.TRUE);
	return redirectToProcessPage(request, acquisitionProcess);
     }
 
     public ActionForward cancelAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "CancelAcquisitionRequest");
     }
 
     public ActionForward executeCreateAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	RegularAcquisitionProcess acquisitionProcess = getProcess(request);
 	return executeCreateAcquisitionRequestItem(mapping, request, acquisitionProcess);
     }
 
     private ActionForward executeCreateAcquisitionRequestItem(final ActionMapping mapping, final HttpServletRequest request,
 	    RegularAcquisitionProcess acquisitionProcess) {
 	request.setAttribute("itemBean", new AcquisitionRequestItemBean(acquisitionProcess.getAcquisitionRequest()));
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return forward(request, "/acquisitions/createAcquisitionRequestItem.jsp");
     }
 
     private ActionForward itemPostBack(final ActionMapping mapping, final HttpServletRequest request, String forward) {
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
 
 	request.setAttribute("itemBean", acquisitionRequestItemBean);
 	request.setAttribute("acquisitionProcess", acquisitionRequestItemBean.getAcquisitionRequest().getAcquisitionProcess());
 
 	return forward(request, forward);
     }
 
     public ActionForward createItemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return itemPostBack(mapping, request, "/acquisitions/createAcquisitionRequestItem.jsp");
     }
 
     public ActionForward editItemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	return itemPostBack(mapping, request, "/acquisitions/editRequestItem.jsp");
     }
 
     public ActionForward createNewAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AcquisitionRequestItemBean requestItemBean = getRenderedObject();
 
 	RegularAcquisitionProcess acquisitionProcess = (RegularAcquisitionProcess) requestItemBean.getAcquisitionRequest()
 		.getAcquisitionProcess();
 	AbstractActivity<RegularAcquisitionProcess> activity = acquisitionProcess
 		.getActivityByName("CreateAcquisitionRequestItem");
 	try {
 	    activity.execute(acquisitionProcess, requestItemBean);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle(), e.getArgs());
 	    return executeCreateAcquisitionRequestItem(mapping, request, acquisitionProcess);
 	}
 	return redirectToProcessPage(request, acquisitionProcess);
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
 
     public ActionForward executeDeleteAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionProcess acquisitionProcess = item.getAcquisitionRequest().getAcquisitionProcess();
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	genericActivityExecution(acquisitionProcess, "DeleteAcquisitionRequestItem", item);
 	return viewAcquisitionProcess(mapping, form, request, response);
     }
 
     public ActionForward executeEditAcquisitionRequestItemPostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionRequestItemBean itemBean = getRenderedObject("acquisitionRequestItem");
 	request.setAttribute("itemBean", itemBean);
 	RenderUtils.invalidateViewState("acquisitionRequestItem");
 	return forward(request, "/acquisitions/editRequestItem.jsp");
     }
 
     public ActionForward executeEditAcquisitionRequestItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final AcquisitionRequestItem acquisitionRequestItem = getDomainObject(request, "acquisitionRequestItemOid");
 	AcquisitionRequestItemBean itemBean = new AcquisitionRequestItemBean(acquisitionRequestItem);
 	request.setAttribute("itemBean", itemBean);
 	return forward(request, "/acquisitions/editRequestItem.jsp");
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
 	return viewAcquisitionProcess(mapping, form, request, response);
     }
 
     public ActionForward executeSubmitForApproval(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "SubmitForApproval");
     }
 
     public ActionForward executeRejectAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final RegularAcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("stateBean", new ProcessStateBean());
 	return forward(request, "/acquisitions/rejectAcquisitionProcess.jsp");
     }
 
     public ActionForward rejectAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final ProcessStateBean stateBean = getRenderedObject();
 	try {
 	    genericActivityExecution(request, "RejectAcquisitionProcess", stateBean.getJustification());
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	}
 	return viewAcquisitionProcess(mapping, form, request, response);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected RegularAcquisitionProcess getProcess(HttpServletRequest request) {
 	return getDomainObject(request, "processOid");
     }
 
     protected Class<? extends AcquisitionProcess> getProcessClass() {
 	return RegularAcquisitionProcess.class;
     }
 
     public ActionForward executeSetSkipSupplierFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionProcess process = getProcess(request);
 	genericActivityExecution(process, "SetSkipSupplierFundAllocation", new Object[] {});
 
 	return redirectToProcessPage(request, process);
     }
 
     public ActionForward executeRemovePermanentProjectFunds(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionProcess process = getProcess(request);
 	genericActivityExecution(process, "RemovePermanentProjectFunds", new Object[] {});
 
 	return redirectToProcessPage(request, process);
     }
 
     public ActionForward executeUnsetSkipSupplierFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionProcess process = getProcess(request);
 	try {
 	    genericActivityExecution(process, "UnsetSkipSupplierFundAllocation", new Object[] {});
 	} catch (DomainException e) {
 	    addMessage(e.getLocalizedMessage(), getBundle(), new String[] {});
 	}
 
 	return redirectToProcessPage(request, process);
     }
 
     public ActionForward checkFundAllocations(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	IViewState viewState = RenderUtils.getViewState("dateSelection");
 
 	DateIntervalBean bean = viewState == null ? new DateIntervalBean() : (DateIntervalBean) viewState.getMetaObject()
 		.getObject();
 
 	if (viewState == null) {
 	    LocalDate today = new LocalDate();
 	    bean.setBegin(today);
 	    bean.setEnd(today);
 	}
 
 	DateTime begin = bean.getBegin().toDateTimeAtStartOfDay();
 	DateTime end = bean.getEnd().plusDays(1).toDateTimeAtStartOfDay();
 
 	List<AcquisitionProcess> processes = new ArrayList<AcquisitionProcess>();
 
 	for (AcquisitionProcess process : GenericProcess.getAllProcesses(AcquisitionProcess.class)) {
 	    if (!process.getExecutionLogs(begin, end, FundAllocation.class, ProjectFundAllocation.class,
 		    AllocateFundsPermanently.class, AllocateProjectFundsPermanently.class).isEmpty()) {
 		processes.add(process);
 	    }
 	}
 	RenderUtils.invalidateViewState();
 	request.setAttribute("processes", processes);
 	request.setAttribute("bean", bean);
 
 	return forward(request, "/acquisitions/viewFundAllocations.jsp");
     }
 
     @Override
     protected AcquisitionRequestItem getRequestItem(HttpServletRequest request) {
 	AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	return item != null ? item : (AcquisitionRequestItem) super.getRequestItem(request);
     }
 
     protected ActionForward redirectToProcessPage(HttpServletRequest request, AcquisitionProcess process) {
 	ActionForward forward = new ActionForward();
 	forward.setRedirect(true);
 	String realPath = "/acquisition" + process.getClass().getSimpleName() + ".do?method=viewProcess&processOid="
 		+ process.getOID();
 	forward.setPath(realPath + "&" + GenericChecksumRewriter.CHECKSUM_ATTRIBUTE_NAME + "="
 		+ GenericChecksumRewriter.calculateChecksum(request.getContextPath() + realPath));
 	return forward;
     }
 }
