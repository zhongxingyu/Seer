 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
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
 import pt.ist.expenditureTrackingSystem.presentationTier.Context;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.ProcessAction;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions.SimplifiedProcedureProcessAction.ReceiveInvoiceForm;
 import pt.ist.fenixWebFramework.struts.annotations.Forward;
 import pt.ist.fenixWebFramework.struts.annotations.Forwards;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/acquisitionAfterTheFactAcquisitionProcess")
 @Forwards( {
 	@Forward(name = "create.afterTheFact.acquisition.process", path = "/acquisitions/createAfterTheFactAcquisitionProcess.jsp"),
 	@Forward(name = "view.afterTheFact.acquisition.process", path = "/acquisitions/viewAfterTheFactAcquisitionProcess.jsp"),
 	@Forward(name = "edit.afterTheFact.acquisition.process", path = "/acquisitions/editAfterTheFactAcquisitionProcess.jsp"),
 	@Forward(name = "receive.acquisition.invoice", path = "/acquisitions/receiveAcquisitionInvoice.jsp"),
	@Forward(name = "show.pending.processes", path = "/search.do?method=search"),
 	@Forward(name = "import.afterTheFact.acquisitions", path = "/acquisitions/importAfterTheFactAcquisitions.jsp"),
 	@Forward(name = "view.import.afterTheFact.acquisitions.result", path = "/acquisitions/viewImportAfterTheFactAcquisitionsResult.jsp"),
 	@Forward(name = "list.afterTheFact.imports", path = "/acquisitions/listImportAfterTheFactAcquisitionsResult.jsp") })
 public class AfterTheFactAcquisitionProcessAction extends ProcessAction {
 
     private static final Context CONTEXT = new Context("acquisitions");
 
     @Override
     protected Context getContextModule(final HttpServletRequest request) {
 	return CONTEXT;
     }
 
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
 	return mapping.findForward("create.afterTheFact.acquisition.process");
     }
 
     public ActionForward createNewAfterTheFactAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = getRenderedObject();
 	final AfterTheFactAcquisitionProcess afterTheFactAcquisitionProcess = AfterTheFactAcquisitionProcess
 		.createNewAfterTheFactAcquisitionProcess(afterTheFactAcquisitionProcessBean);
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
 	return mapping.findForward("view.afterTheFact.acquisition.process");
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
 	return mapping.findForward("edit.afterTheFact.acquisition.process");
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
 	return mapping.findForward("receive.acquisition.invoice");
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
 	return mapping.findForward("show.pending.processes");
     }
 
     public ActionForward prepareImport(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AfterTheFactAcquisitionsImportBean afterTheFactAcquisitionsImportBean = new AfterTheFactAcquisitionsImportBean();
 	request.setAttribute("afterTheFactAcquisitionsImportBean", afterTheFactAcquisitionsImportBean);
 	return mapping.findForward("import.afterTheFact.acquisitions");
     }
 
     public ActionForward processImport(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AfterTheFactAcquisitionsImportBean afterTheFactAcquisitionsImportBean = getRenderedObject();
 	final byte[] contents = consumeInputStream(afterTheFactAcquisitionsImportBean);
 	afterTheFactAcquisitionsImportBean.setFileContents(contents);
 	afterTheFactAcquisitionsImportBean.setCreateData(false);
 	afterTheFactAcquisitionsImportBean.importAcquisitions();
 	request.setAttribute("afterTheFactAcquisitionsImportBean", afterTheFactAcquisitionsImportBean);
 	return mapping.findForward("view.import.afterTheFact.acquisitions.result");
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
 	return mapping.findForward("view.import.afterTheFact.acquisitions.result");
     }
 
     public ActionForward listImports(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	List<ImportFile> files = File.getFiles(ImportFile.class);
 	request.setAttribute("files", files);
 	return mapping.findForward("list.afterTheFact.imports");
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
 
 }
