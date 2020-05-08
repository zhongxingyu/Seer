 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.workflow.presentationTier.actions.ProcessManagement;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateRefundProcessBean;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionRefundProcess")
 public class RefundProcessAction extends PaymentProcessAction {
 
     public ActionForward prepareCreateRefundProcessUnderCCP(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CreateRefundProcessBean bean = getRenderedObject();
 	if (bean == null) {
 	    bean = new CreateRefundProcessBean(getLoggedPerson(), true);
 	}
 	request.setAttribute("bean", bean);
 	return forward(request, "/acquisitions/refund/createRefundRequest.jsp");
     }
 
     public ActionForward prepareCreateRefundProcessUnderRCIST(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CreateRefundProcessBean bean = getRenderedObject();
 	if (bean == null) {
 	    bean = new CreateRefundProcessBean(getLoggedPerson(), false);
 	}
 	request.setAttribute("bean", bean);
 	return forward(request, "/acquisitions/refund/createRefundRequest.jsp");
     }
 
     public ActionForward createRefundProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {

 	CreateRefundProcessBean bean = getRenderedObject("createRefundProcess");
	RefundProcess process = RefundProcess.createNewRefundProcess(bean);
	return ProcessManagement.forwardToProcess(process);

     }
 
     public ActionForward createRefundProcessPostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {

 	CreateRefundProcessBean bean = getRenderedObject("createRefundProcess");
 	request.setAttribute("bean", bean);
 	RenderUtils.invalidateViewState("createRefundProcess");
 	return forward(request, "/acquisitions/refund/createRefundRequest.jsp");
     }
 
 }
