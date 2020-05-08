 /*
  * @(#)AcquisitionFundAllocationDiaryAndTransactionReportRequest.java
  *
  * Copyright 2012 Instituto Superior Tecnico
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
 
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Set;
 
 import module.finance.domain.Supplier;
 import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.bennu.core.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixframework.DomainObject;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public class AcquisitionFundAllocationDiaryAndTransactionReportRequest extends
         AcquisitionFundAllocationDiaryAndTransactionReportRequest_Base {
 
     public AcquisitionFundAllocationDiaryAndTransactionReportRequest(final UnitItem unitItem, final String processId,
             final String payingUnitNumber, final String payingAccountingUnit, final String diaryNumber,
             final String transactionNumber) {
         super();
         setUnitItem(unitItem);
         setProcessId(processId);
         setPayingUnitNumber(payingUnitNumber);
         setPayingAccountingUnit(payingAccountingUnit);
         setDiaryNumber(diaryNumber);
         setTransactionNumber(transactionNumber);
     }
 
     @Override
     public String getQueryString() {
         final UnitItem unitItem = getUnitItem();
         final Unit unit = unitItem.getUnit();
         final RequestItem item = unitItem.getItem();
         final CPVReference cpvReference = item.getCPVReference();
         final RequestWithPayment request = item.getRequest();
         final Supplier supplier = getSupplier(request);
         final PaymentProcess process = request.getProcess();
 
         final Money shareValue = unitItem.getShareValue();
 
         final BigDecimal d = new BigDecimal(1).add(unitItem.getVatValue().divide(new BigDecimal(100)));
         final Money shareWithoutVat = shareValue.divideAndRound(d);
         final Money shareVat = shareValue.subtract(shareWithoutVat);
 
         Object[] insertArgs =
                 new Object[] {
                         "INTERACT_ID",
                         Long.valueOf(getInteractionId()),
                         "PROCESS_ID",
                         getProcessId(),
                         "ITEM_ID",
                         unitItem.getExternalId(),
                         "PROJ_ID",
                         getProjectId(unit),
                         "PROJ_MEMBER",
                         getSubProjectId(unit),
                         "SUPPLIER_ID",
                         supplier == null ? null : supplier.getGiafKey(),
                         "SUPPLIER_DOC_TYPE",
                         request instanceof AcquisitionRequest ? (supplier == null ? null : (hasProposal(request) ? "Proposta" : "Factura")) : (supplier == null ? "Reembolso" : "Factura"),
                         "SUPPLIER_DOC_ID", supplier == null ? null : limitStringSize(getProposalNumber(request), 24), "CPV_ID",
                         cpvReference.getCode(), "CPV_DESCRIPTION", cpvReference.getDescription(), "MOV_DESCRIPTION",
                        sanitize(limitStringSize(Integer.toString(item.getUnitItems().size())) + " - " + item.getDescription(), 4000),
                         "MOV_PCT_IVA", unitItem.getVatValue(), "MOV_VALUE", shareValue, "MOV_VALUE_IVA", shareVat,
 //,		    "CALLBACK_URL", getCallbackUrl()
                         "PROCESS_URL", getProcessUrl(process), "GIAF_DIARIO", getDiaryNumber(), "GIAF_NUM_REG",
                         getTransactionNumber(), };
         return insertQuery(insertArgs);
     }
 
     private String sanitize(final String s) {
 	return s.replace('\n', ' ');
     }
 
     public String getProjectId(final Unit unit) {
         if (unit instanceof Project) {
             final Project project = (Project) unit;
             return project.getProjectCode();
         } else if (unit instanceof SubProject) {
             final SubProject subProject = (SubProject) unit;
             final Project project = (Project) subProject.getParentUnit();
             return project.getProjectCode();
         }
         return null;
     }
 
     public String getSubProjectId(final Unit unit) {
         if (unit instanceof SubProject) {
             final SubProject subProject = (SubProject) unit;
             final Project project = (Project) subProject.getParentUnit();
             final String projectName = project.getName();
             final String description = subProject.getName().substring(projectName.length() + 3);
             final int i = description.indexOf(" - ");
             return description.substring(0, i);
         }
         return null;
     }
 
     @Override
     public void processResultSet(final ResultSet resultSet) throws SQLException {
         registerOnExternalSystem();
         setExternalAccountingIntegrationSystemFromPendingResult(null);
     }
 
     @Override
     protected String getTableName() {
         return "CONTABILIZACAO";
     }
 
     private Supplier getSupplier(final RequestWithPayment request) {
         if (request instanceof AcquisitionRequest) {
             final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
             return acquisitionRequest.getSupplier();
         }
         return null;
     }
 
     private boolean hasProposal(final RequestWithPayment request) {
         if (request instanceof AcquisitionRequest) {
             final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
             final AcquisitionProcess process = acquisitionRequest.getProcess();
             final AcquisitionProposalDocument acquisitionProposalDocument = process.getAcquisitionProposalDocument();
             if (acquisitionProposalDocument != null) {
                 return true;
             }
         }
         return false;
     }
 
     private String getProposalNumber(final RequestWithPayment request) {
         if (request instanceof AcquisitionRequest) {
             final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
             final AcquisitionProcess process = acquisitionRequest.getProcess();
             final AcquisitionProposalDocument acquisitionProposalDocument = process.getAcquisitionProposalDocument();
             if (acquisitionProposalDocument != null) {
                 return acquisitionProposalDocument.getProposalId();
             }
             final Set<PaymentProcessInvoice> invoices = request.getInvoices();
             if (!invoices.isEmpty()) {
                 return invoices.iterator().next().getInvoiceNumber();
             }
         }
         return null;
     }
 
     protected String getProcessUrl(final DomainObject process) {
         final StringBuilder result = new StringBuilder();
         result.append("https://");
         result.append(VirtualHost.getVirtualHostForThread().getHostname());
         result.append("/ForwardToProcess/");
         result.append(process.getExternalId());
         return result.toString();
     }
 
     @Override
     public void handle(final SQLException e) {
         if (e.getMessage().indexOf("unique") >= 0) {
             registerOnExternalSystem();
             setExternalAccountingIntegrationSystemFromPendingResult(null);
         } else {
             super.handle(e);
         }
     }
 
 }
