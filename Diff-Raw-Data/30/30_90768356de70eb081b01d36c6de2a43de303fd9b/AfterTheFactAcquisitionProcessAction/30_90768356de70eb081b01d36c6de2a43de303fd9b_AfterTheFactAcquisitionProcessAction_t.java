 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import myorg.domain.exceptions.DomainException;

 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.File;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Invoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.ImportFile;
 import pt.ist.expenditureTrackingSystem.domain.dto.AfterTheFactAcquisitionProcessBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.AfterTheFactAcquisitionsImportBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.AfterTheFactAcquisitionsImportBean.ImportError;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.ProcessAction;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions.SimplifiedProcedureProcessAction.ReceiveInvoiceForm;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionAfterTheFactAcquisitionProcess")
 public class AfterTheFactAcquisitionProcessAction extends ProcessAction {
 
     @Override
     protected GenericProcess getProcess(HttpServletRequest request) {
 	return getProcess(request, "afterTheFactAcquisitionProcessOid");
     }
 
     @Override
     public ActionForward viewProcess(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 	    HttpServletResponse response) {
 	GenericProcess process = getProcess(request);
 	if (process == null) {
 	    process = getDomainObject(request, "processOid");
 	}
 	return viewAfterTheFactAcquisitionProcess(mapping, request, (AfterTheFactAcquisitionProcess) process);
     }
 
     public ActionForward prepareCreateAfterTheFactAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = new AfterTheFactAcquisitionProcessBean();
 	request.setAttribute("afterTheFactAcquisitionProcessBean", afterTheFactAcquisitionProcessBean);
 	return forward(request, "/acquisitions/createAfterTheFactAcquisitionProcess.jsp");
     }
 
     public ActionForward createNewAfterTheFactAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = getRenderedObject();
	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess;
	try {
	    afterTheFactAcquisitionProcess = AfterTheFactAcquisitionProcess
		    .createNewAfterTheFactAcquisitionProcess(afterTheFactAcquisitionProcessBean);
	} catch (pt.ist.expenditureTrackingSystem.domain.DomainException e) {
	    addMessage(e.getMessage(), getBundle(), e.getArgs());
	    return prepareCreateAfterTheFactAcquisitionProcess(mapping, form, request, response);
	}
 	return viewAfterTheFactAcquisitionProcess(mapping, request, afterTheFactAcquisitionProcess);
     }
 
     public ActionForward viewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = getDomainObject(request, "acquisitionProcessOid");
 	return viewAfterTheFactAcquisitionProcess(mapping, request, afterTheFactAcquisitionProcess);
     }
 
     private ActionForward viewAfterTheFactAcquisitionProcess(ActionMapping mapping, HttpServletRequest request,
 	    AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess) {
 	request.setAttribute("afterTheFactAcquisitionProcess", afterTheFactAcquisitionProcess);
 	return forward(request, "/acquisitions/viewAfterTheFactAcquisitionProcess.jsp");
     }
 
     public ActionForward executeEditAfterTheFactAcquisition(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = getRenderedObject();
 	if (afterTheFactAcquisitionProcessBean == null) {
 	    final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = getDomainObject(request,
 		    "afterTheFactAcquisitionProcessOid");
 	    afterTheFactAcquisitionProcessBean = new AfterTheFactAcquisitionProcessBean(afterTheFactAcquisitionProcess);
 	}
 	request.setAttribute("afterTheFactAcquisitionProcessBean", afterTheFactAcquisitionProcessBean);
 	return forward(request, "/acquisitions/editAfterTheFactAcquisitionProcess.jsp");
     }
 
     public ActionForward editAfterTheFactAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = getRenderedObject();
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = afterTheFactAcquisitionProcessBean
 		.getAfterTheFactAcquisitionProcess();
 	final AbstractActivity<GenericProcess> activity = afterTheFactAcquisitionProcess
 		.getActivityByName("EditAfterTheFactAcquisition");
 	activity.execute(afterTheFactAcquisitionProcess, afterTheFactAcquisitionProcessBean);
 	return viewAfterTheFactAcquisitionProcess(mapping, request, afterTheFactAcquisitionProcess);
     }
 
     public ActionForward executeReceiveAcquisitionInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = getDomainObject(request,
 		"afterTheFactAcquisitionProcessOid");
 	request.setAttribute("afterTheFactAcquisitionProcess", afterTheFactAcquisitionProcess);
	final ReceiveInvoiceForm receiveInvoiceForm = new ReceiveInvoiceForm();
 
 	final AcquisitionAfterTheFact acquisitionAfterTheFact = afterTheFactAcquisitionProcess.getAcquisitionAfterTheFact();
 	if (acquisitionAfterTheFact.hasInvoice()) {
 	    final Invoice invoice = acquisitionAfterTheFact.getInvoice();
 	    receiveInvoiceForm.setInvoiceDate(invoice.getInvoiceDate());
 	    receiveInvoiceForm.setInvoiceNumber(invoice.getInvoiceNumber());
 	}
 
 	request.setAttribute("receiveInvoiceForm", receiveInvoiceForm);
 	return forward(request, "/acquisitions/receiveAcquisitionInvoice.jsp");
     }
 
     public ActionForward receiveAcquisitionInvoice(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final ReceiveInvoiceForm receiveInvoiceForm = getRenderedObject();
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = getDomainObject(request,
 		"afterTheFactAcquisitionProcessOid");
 	final AbstractActivity<GenericProcess> activity = afterTheFactAcquisitionProcess
 		.getActivityByName("ReceiveAcquisitionInvoice");
 	final byte[] bytes = consumeInputStream(receiveInvoiceForm);
 	activity.execute(afterTheFactAcquisitionProcess, receiveInvoiceForm.getFilename(), bytes, receiveInvoiceForm
 		.getInvoiceNumber(), receiveInvoiceForm.getInvoiceDate());
 	return viewAfterTheFactAcquisitionProcess(mapping, request, afterTheFactAcquisitionProcess);
     }
 
     public ActionForward executeDeleteAfterTheFactAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = getDomainObject(request,
 		"afterTheFactAcquisitionProcessOid");
 	final AbstractActivity<GenericProcess> activity = afterTheFactAcquisitionProcess
 		.getActivityByName("DeleteAfterTheFactAcquisitionProcess");
 	activity.execute(afterTheFactAcquisitionProcess);
 	return forward(request, "/search.do?method=search");
     }
 
     public ActionForward prepareImport(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AfterTheFactAcquisitionsImportBean afterTheFactAcquisitionsImportBean = new AfterTheFactAcquisitionsImportBean();
 	request.setAttribute("afterTheFactAcquisitionsImportBean", afterTheFactAcquisitionsImportBean);
 	return forward(request, "/acquisitions/importAfterTheFactAcquisitions.jsp");
     }
 
     public ActionForward processImport(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AfterTheFactAcquisitionsImportBean afterTheFactAcquisitionsImportBean = getRenderedObject();
 	final byte[] contents = consumeInputStream(afterTheFactAcquisitionsImportBean);
 	afterTheFactAcquisitionsImportBean.setFileContents(contents);
 	afterTheFactAcquisitionsImportBean.setCreateData(false);
 	afterTheFactAcquisitionsImportBean.importAcquisitions();
 	request.setAttribute("afterTheFactAcquisitionsImportBean", afterTheFactAcquisitionsImportBean);
 	return forward(request, "/acquisitions/viewImportAfterTheFactAcquisitionsResult.jsp");
     }
 
     public ActionForward importAcquisitions(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AfterTheFactAcquisitionsImportBean afterTheFactAcquisitionsImportBean = getRenderedObject();
 	try {
 	    afterTheFactAcquisitionsImportBean.reset();
 	    afterTheFactAcquisitionsImportBean.setCreateData(true);
 	    afterTheFactAcquisitionsImportBean.importAcquisitions();
 	} catch (ImportError ex) {
 	    // just show the page...
 	}
 	request.setAttribute("afterTheFactAcquisitionsImportBean", afterTheFactAcquisitionsImportBean);
 	return forward(request, "/acquisitions/viewImportAfterTheFactAcquisitionsResult.jsp");
     }
 
     public ActionForward listImports(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	List<ImportFile> files = File.getFiles(ImportFile.class);
 	request.setAttribute("files", files);
 	return forward(request, "/acquisitions/listImportAfterTheFactAcquisitionsResult.jsp");
     }
 
     public ActionForward downloadImportFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 
 	ImportFile file = getDomainObject(request, "fileOID");
 	return download(response, file);
     }
 
     public ActionForward cancelImportFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ImportFile file = getDomainObject(request, "fileOID");
 	file.cancel();
 
 	return listImports(mapping, form, request, response);
     }
 
     public ActionForward enableImportFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	ImportFile file = getDomainObject(request, "fileOID");
 	file.reenable();
 
 	return listImports(mapping, form, request, response);
     }
 
    @Override
    protected String getBundle() {
	return "ACQUISITION_RESOURCES";
    }
 }
