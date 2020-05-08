 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.presentationTier.WorkflowLayoutContext;
 import module.workflow.presentationTier.actions.ProcessManagement;
 import myorg.presentationTier.Context;
 import myorg.presentationTier.actions.ContextBaseAction;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AbstractFundAllocationActivityInformation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionRequestItemActivityInformation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionRequestItemActivityInformation.CreateItemSchemaType;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 
 @Mapping(path = "/expenditureProcesses")
 public class ExpenditureProcessesAction extends ContextBaseAction {
 
     public ActionForward addAllocationFundGeneric(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	AbstractFundAllocationActivityInformation<PaymentProcess> activityInformation = getRenderedObject("activityBean");
 	final PaymentProcess process = activityInformation.getProcess();
 	request.setAttribute("process", process);
 	request.setAttribute("information", activityInformation);
 
 	List<FundAllocationBean> fundAllocationBeans = activityInformation.getBeans();
 	Integer index = Integer.valueOf(request.getParameter("index"));
 
 	Financer financer = getDomainObject(request, "financerOID");
 	FundAllocationBean fundAllocationBean = new FundAllocationBean(financer);
 	fundAllocationBean.setFundAllocationId(null);
 	fundAllocationBean.setEffectiveFundAllocationId(null);
 	fundAllocationBean.setAllowedToAddNewFund(false);
 
 	fundAllocationBeans.add(index + 1, fundAllocationBean);
 	RenderUtils.invalidateViewState();
 	return ProcessManagement.performActivityPostback(activityInformation, request);
     }
 
     public ActionForward removeAllocationFundGeneric(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AbstractFundAllocationActivityInformation<PaymentProcess> activityInformation = getRenderedObject("activityBean");
 	final PaymentProcess process = activityInformation.getProcess();
 	request.setAttribute("process", process);
 	request.setAttribute("information", activityInformation);
 
 	List<FundAllocationBean> fundAllocationBeans = activityInformation.getBeans();
 	int index = Integer.valueOf(request.getParameter("index")).intValue();
 
 	fundAllocationBeans.remove(index);
 	RenderUtils.invalidateViewState();
 	return ProcessManagement.performActivityPostback(activityInformation, request);
     }
 
     public ActionForward itemPostBack(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	CreateAcquisitionRequestItemActivityInformation activityInformation = getRenderedObject("activityBean");
 	WorkflowProcess process = AbstractDomainObject.fromExternalId(request.getParameter("processId"));
 	RenderUtils.invalidateViewState();
 	activityInformation.setRecipient(null);
 	activityInformation.setAddress(null);
 
 	if (activityInformation.getCreateItemSchemaType().equals(CreateItemSchemaType.EXISTING_DELIVERY_INFO)) {
 	    activityInformation.setDeliveryInfo(activityInformation.getAcquisitionRequest().getRequester()
 		    .getDeliveryInfoByRecipientAndAddress(activityInformation.getRecipient(), activityInformation.getAddress()));
 	} else {
 	    activityInformation.setDeliveryInfo(null);
 	}
 
 	request.setAttribute("information", activityInformation);
 	request.setAttribute("process", process);
 	return ProcessManagement.performActivityPostback(activityInformation, request);
     }
 
    public ActionForward itemInvalidInfo(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {
	CreateAcquisitionRequestItemActivityInformation activityInformation = getRenderedObject("activityBean");
	WorkflowProcess process = AbstractDomainObject.fromExternalId(request.getParameter("processId"));

	request.setAttribute("information", activityInformation);
	request.setAttribute("process", process);

	return ProcessManagement.performActivityPostback(activityInformation, request);
    }

     @Override
     public Context createContext(String contextPathString, HttpServletRequest request) {
 	WorkflowProcess process = getProcess(request);
 	WorkflowLayoutContext layout = process.getLayout();
 	layout.setElements(contextPathString);
 	return layout;
     }
 
     protected <T extends WorkflowProcess> T getProcess(HttpServletRequest request) {
 	return (T) getDomainObject(request, "processId");
     }
 }
