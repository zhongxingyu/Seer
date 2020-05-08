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
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.SearchAcquisitionProcess;
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
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.fenixWebFramework.renderers.components.state.IViewState;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionProcess")
 @Forwards( { @Forward(name = "search.acquisition.process", path = "/acquisitions/searchAcquisitionProcess.jsp"),
 	@Forward(name = "view.fund.allocations", path = "/acquisitions/viewFundAllocations.jsp") })
 public class RegularAcquisitionProcessAction extends PaymentProcessAction {
 
     private static final Context CONTEXT = new Context("acquisitions");
 
     @Override
     protected Context getContextModule(final HttpServletRequest request) {
 	return CONTEXT;
     }
 
     @Override
     public ActionForward viewProcess(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) {
 	return viewAcquisitionProcess(mapping, request, getProcess(request));
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final AcquisitionProcess acquisitionProcess) {
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	return mapping.findForward("view.acquisition.process");
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final RegularAcquisitionProcess acquisitionProcess = getProcess(request);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward executeCancelAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("acquisitionProcess", acquisitionProcess);
 	request.setAttribute("confirmCancelAcquisitionProcess", Boolean.TRUE);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward cancelAcquisitionRequest(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	return executeActivityAndViewProcess(mapping, form, request, response, "CancelAcquisitionRequest");
     }
 
     public ActionForward executeDeleteAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final RegularAcquisitionProcess acquisitionProcess = getProcess(request);
 	request.setAttribute("confirmDeleteAcquisitionProcess", Boolean.TRUE);
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward deleteAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	genericActivityExecution(request, "DeleteAcquisitionProcess");
 	return showPendingProcesses(mapping, form, request, response);
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
 	return mapping.findForward("create.acquisition.request.item");
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

	return mapping.findForward(forward);
    }

    public ActionForward createItemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {
	return itemPostBack(mapping, request, "create.acquisition.request.item");
    }

    public ActionForward editItemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {
	return itemPostBack(mapping, request, "edit.request.item");
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
 	return viewAcquisitionProcess(mapping, request, acquisitionProcess);
     }
 
     public ActionForward showPendingProcesses(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final SearchAcquisitionProcess searchAcquisitionProcess = new SearchAcquisitionProcess(getProcessClass());
 	User user = UserView.getUser();
 	if (user != null && user.getPerson().hasAnyAuthorizations()) {
 	    searchAcquisitionProcess.setResponsibleUnitSetOnly(Boolean.TRUE);
 	}
 	searchAcquisitionProcess.setHasAvailableAndAccessibleActivityForUser(Boolean.TRUE);
 	return searchAcquisitionProcess(mapping, request, searchAcquisitionProcess);
     }
 
     public ActionForward showMyProcesses(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final SearchAcquisitionProcess searchAcquisitionProcess = new SearchAcquisitionProcess(getProcessClass());
 	searchAcquisitionProcess.setRequester(getLoggedPerson());
 	return searchAcquisitionProcess(mapping, request, searchAcquisitionProcess);
     }
 
     public ActionForward searchAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SearchAcquisitionProcess searchAcquisitionProcess = getRenderedObject();
 	if (searchAcquisitionProcess == null) {
 	    searchAcquisitionProcess = new SearchAcquisitionProcess(getProcessClass());
 	    User user = UserView.getUser();
 	    if (user != null && user.getPerson().hasAnyAuthorizations()) {
 		searchAcquisitionProcess.setResponsibleUnitSetOnly(Boolean.TRUE);
 	    }
 	}
 	return searchAcquisitionProcess(mapping, request, searchAcquisitionProcess);
     }
 
     public ActionForward searchAcquisitionProcess(final ActionMapping mapping, final HttpServletRequest request,
 	    final SearchAcquisitionProcess searchAcquisitionProcess) {
 	request.setAttribute("searchAcquisitionProcess", searchAcquisitionProcess);
 	return mapping.findForward("search.acquisition.process");
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
 	return viewAcquisitionProcess(mapping, request, requestItemBean.getAcquisitionRequest().getAcquisitionProcess());
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
 	return viewAcquisitionProcess(mapping, request, (AcquisitionProcess) getProcess(request));
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
 
 	return viewAcquisitionProcess(mapping, request, process);
     }
 
     public ActionForward executeUnsetSkipSupplierFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AcquisitionProcess process = getProcess(request);
 	try {
 	    genericActivityExecution(process, "UnsetSkipSupplierFundAllocation", new Object[] {});
 	} catch (DomainException e) {
 	    addMessage(e.getLocalizedMessage(), getBundle(), new String[] {});
 	}
 
 	return viewAcquisitionProcess(mapping, request, process);
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
 
 	return mapping.findForward("view.fund.allocations");
     }
 
     @Override
     protected AcquisitionRequestItem getRequestItem(HttpServletRequest request) {
 	AcquisitionRequestItem item = getDomainObject(request, "acquisitionRequestItemOid");
 	return item != null ? item : (AcquisitionRequestItem) super.getRequestItem(request);
     }
 }
