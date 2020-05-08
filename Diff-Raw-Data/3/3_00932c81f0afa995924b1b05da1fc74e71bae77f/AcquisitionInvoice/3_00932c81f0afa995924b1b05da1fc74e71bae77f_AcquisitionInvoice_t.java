 /*
  * @(#)AcquisitionInvoice.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import module.workflow.domain.ProcessFileValidationException;
 import module.workflow.domain.WorkflowProcess;
 import module.workflow.util.FileUploadBeanResolver;
 import module.workflow.util.WorkflowFileUploadBean;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.util.BundleUtil;
 import myorg.util.ClassNameBundle;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.fileBeans.InvoiceFileBean;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.fileBeans.InvoiceFileBean.RequestItemHolder;
 
 @ClassNameBundle(bundle = "resources/AcquisitionResources")
 /**
  * 
  * @author Luis Cruz
  * @author Paulo Abrantes
  * 
  */
 public class AcquisitionInvoice extends AcquisitionInvoice_Base {
 
     static {
 	FileUploadBeanResolver.registerBeanForProcessFile(AcquisitionInvoice.class, InvoiceFileBean.class);
     }
 
     public AcquisitionInvoice(String displayName, String filename, byte[] content) {
 	super();
 	init(displayName, filename, content);
     }
 
     @Override
     public void delete() {
 	throw new UnsupportedOperationException();
     }
 
     @Override
     public void fillInNonDefaultFields(WorkflowFileUploadBean bean) {
 	super.fillInNonDefaultFields(bean);
 
 	InvoiceFileBean fileBean = (InvoiceFileBean) bean;
 	AcquisitionRequest request = fileBean.getRequest();
 	request.validateInvoiceNumber(fileBean.getInvoiceNumber());
 
 	setInvoiceNumber(fileBean.getInvoiceNumber());
 	setInvoiceDate(fileBean.getInvoiceDate());
 
 	setInvoiceDate(fileBean.getInvoiceDate());
 	StringBuilder builder = new StringBuilder("<ul>");
 	for (RequestItemHolder itemHolder : fileBean.getItems()) {
 	    if (itemHolder.isAccountable()) {
 		addRequestItems(itemHolder.getItem());
 		builder.append("<li>");
 		builder.append(itemHolder.getDescription());
 		builder.append(" - ");
 		builder.append(BundleUtil.getFormattedStringFromResourceBundle("resources/AcquisitionResources",
 			"acquisitionRequestItem.label.quantity"));
 		builder.append(":");
 		builder.append(itemHolder.getAmount());
 		builder.append("</li>");
 	    }
 	}
 	builder.append("</ul>");
 	setConfirmationReport(builder.toString());
     }
 
     @Override
     public void validateUpload(WorkflowProcess workflowProcess) {
 	RegularAcquisitionProcess process = (RegularAcquisitionProcess) workflowProcess;
 
 	if (process.isAcquisitionProcessed() && ExpenditureTrackingSystem.isAcquisitionCentralGroupMember(UserView.getCurrentUser())) {
 	    return;
 	}
 
 	if (ExpenditureTrackingSystem.isInvoiceAllowedToStartAcquisitionProcess()) {
 	    if (process.isInGenesis() && process.getRequestor() == UserView.getCurrentUser().getExpenditurePerson()) {
 		return;
 	    }
 	    throw new ProcessFileValidationException("resources/AcquisitionResources", "error.acquisitionInvoice.upload.invalid.or.in.construction");
 	}
 
 	throw new ProcessFileValidationException("resources/AcquisitionResources", "error.acquisitionInvoice.upload.invalid");
     }
 
     @Override
     public void postProcess(final WorkflowFileUploadBean bean) {
 	final InvoiceFileBean fileBean = (InvoiceFileBean) bean;
 	final AcquisitionRequest request = fileBean.getRequest();
 	final AcquisitionProcess process = request.getProcess();
 	if (!ExpenditureTrackingSystem.isInvoiceAllowedToStartAcquisitionProcess()
 		|| !process.isInGenesis()) {
 	    if (!fileBean.getHasMoreInvoices()) {
 		((RegularAcquisitionProcess) request.getProcess()).invoiceReceived();
 	    }
 	    request.processReceivedInvoice();
 	}
     }
 
     @Override
     public boolean isPossibleToArchieve() {
 	RegularAcquisitionProcess process = (RegularAcquisitionProcess) getProcess();
 	return (process.isAcquisitionProcessed() || process.isInvoiceReceived())
 		&& ExpenditureTrackingSystem.isAcquisitionCentralGroupMember(UserView.getCurrentUser());
     }
 
     @Override
     public String getDisplayName() {
 	return getFilename();
     }
 
     @Override
     public void processRemoval() {
 	for (; !getFinancers().isEmpty(); getFinancers().get(0).removeAllocatedInvoices(this))
 	    ;
	getProjectFinancers().clear();
 	for (; !getRequestItems().isEmpty(); getRequestItems().get(0).removeInvoicesFiles(this))
 	    ;
 	for (; !getUnitItems().isEmpty(); getUnitItems().get(0).removeConfirmedInvoices(this))
 	    ;
     }
 
 }
