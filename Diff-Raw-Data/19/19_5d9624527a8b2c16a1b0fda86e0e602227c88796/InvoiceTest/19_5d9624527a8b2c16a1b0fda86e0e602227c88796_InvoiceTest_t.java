 package org.agito.bpmo.test;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Date;
 
 import org.agito.bpmo.sample.invoice.Invoice;
 import org.agito.bpmo.sample.invoice.InvoiceAccess;
 import org.agito.bpmo.sample.invoice.InvoiceLifecycle;
 import org.agito.bpmo.sample.invoice.InvoiceProcessActivity;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import de.agito.cps.core.bpmo.IBPMO;
 import de.agito.cps.core.bpmo.PrincipalType;
 import de.agito.cps.core.exception.ValidationException;
 import de.agito.cps.core.process.spi.eventing.ProcessAgentEventType;
 import de.agito.cps.core.test.BPMOTestRule;
 import de.agito.cps.core.test.annotations.BPMOTestUserId;
 import de.agito.cps.core.utils.ConvertUtils;
 import de.agito.cps.core.utils.ResourceReader;
 
 public class InvoiceTest {
 
 	@Rule
 	public BPMOTestRule bpmoRule = BPMOTestRule.init();
 
 	@Rule
 	public ExpectedException expectedException = ExpectedException.none();
 
 	@BPMOTestUserId("bob")
 	@Test
 	public void testInvoiceStartPositive() {
 
 		// create bpmo
 		IBPMO bpmo = bpmoRule.getRuntimeService().createBPMO(Invoice.$BPMO, InvoiceLifecycle.NEW, "001");
 		InvoiceAccess invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getInvoicingParty().setValue("ACME");
 		invoiceAccess.getInvoiceNumber().setValue("10343456");
 		invoiceAccess.getInvoiceDate().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getInvoiceReceived().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getTermOfPayment().setValue(new BigDecimal(30));
 		invoiceAccess.getOrderNumber().setValue("90387595");
 		invoiceAccess.getTaxPositions().getRows().get(0).getNetAmount().setValue(new BigDecimal("100.98"));
 		try {
 			invoiceAccess.getInvoiceAttachment().setValue("Invoice ACME Inc.pdf", "application/pdf",
 					ResourceReader.getResourceAsBinary(BPMOTestRule.class.getClassLoader(), "Invoice ACME Inc.pdf"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// start process
 		bpmo.startProcess();
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.processInstanceId(bpmo.getBPMOHeader().getProcessInstanceId())
 				.eventType(ProcessAgentEventType.PROCESS_START).singleResult());
 
 	}
 
 	@BPMOTestUserId("bob")
 	@Test
 	public void testInvoiceStartNeagtive() {
 
 		// create bpmo
 		IBPMO bpmo = bpmoRule.getRuntimeService().createBPMO(Invoice.$BPMO, InvoiceLifecycle.NEW, "001");
 		InvoiceAccess invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getInvoicingParty().setValue("ACME");
 		invoiceAccess.getInvoiceNumber().setValue("10343456");
 		invoiceAccess.getInvoiceDate().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getInvoiceReceived().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getTermOfPayment().setValue(new BigDecimal(30));
 		invoiceAccess.getOrderNumber().setValue("90387595");
 		invoiceAccess.getTaxPositions().getRows().get(0).getNetAmount().setValue(new BigDecimal("100.98"));
 
 		// expect ValidationException upon startProcess()
 		expectedException.expect(ValidationException.class);
 
 		// start process
 		bpmo.startProcess();
 	}
 
 	@BPMOTestUserId("bob")
 	@Test
 	public void testInvoiceFinish() {
 
 		// create bpmo
 		IBPMO bpmo = bpmoRule.getRuntimeService().createBPMO(Invoice.$BPMO, InvoiceLifecycle.NEW, "001");
 		InvoiceAccess invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getInvoicingParty().setValue("ACME");
 		invoiceAccess.getInvoiceNumber().setValue("10343456");
 		invoiceAccess.getInvoiceDate().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getInvoiceReceived().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getTermOfPayment().setValue(new BigDecimal(30));
 		invoiceAccess.getOrderNumber().setValue("90387595");
 		invoiceAccess.getTaxPositions().getRows().get(0).getNetAmount().setValue(new BigDecimal("100.98"));
 		try {
 			invoiceAccess.getInvoiceAttachment().setValue("Invoice ACME Inc.pdf", "application/pdf",
 					ResourceReader.getResourceAsBinary(BPMOTestRule.class.getClassLoader(), "Invoice ACME Inc.pdf"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// start process
 		bpmo.startProcess();
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.processInstanceId(bpmo.getBPMOHeader().getProcessInstanceId())
 				.eventType(ProcessAgentEventType.PROCESS_START).singleResult());
 
 		// wait for async jobs
 		bpmoRule.waitForActivitiJobExecutorToProcessAllJobs(30000l, 200l);
 
 		// checkout and complete Approval task
 		bpmo = bpmoRule.getRuntimeService().readBPMO(bpmo.getBPMOHeader().getBPMOUuid(), true,
 				InvoiceProcessActivity.Approval);
 		bpmo.claimTaskInstance(InvoiceProcessActivity.Approval);
 		bpmo.completeTaskInstance("approved", null);
 
 		// wait for async jobs
 		bpmoRule.waitForActivitiJobExecutorToProcessAllJobs(30000l, 200l);
 
 		// assert process end
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.bpmoUuid(bpmo.getBPMOHeader().getBPMOUuid()).eventType(ProcessAgentEventType.PROCESS_END)
 				.singleResult());

 		Assert.assertNotNull(bpmoRule.getRuntimeService().createBusinessLogQuery()
 				.bpmoUuid(bpmo.getBPMOHeader().getBPMOUuid()).singleResult());
 
 	}
 
 	@BPMOTestUserId("bob")
 	@Test
 	public void testInvoiceFinishIncludeOrderCheck() {
 
 		// create bpmo
 		IBPMO bpmo = bpmoRule.getRuntimeService().createBPMO(Invoice.$BPMO, InvoiceLifecycle.NEW, "001");
 		InvoiceAccess invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getInvoicingParty().setValue("ACME");
 		invoiceAccess.getInvoiceNumber().setValue("10343456");
 		invoiceAccess.getInvoiceDate().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getInvoiceReceived().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getTermOfPayment().setValue(new BigDecimal(30));
 		invoiceAccess.getTaxPositions().getRows().get(0).getNetAmount().setValue(new BigDecimal("100.98"));
 		try {
 			invoiceAccess.getInvoiceAttachment().setValue("Invoice ACME Inc.pdf", "application/pdf",
 					ResourceReader.getResourceAsBinary(BPMOTestRule.class.getClassLoader(), "Invoice ACME Inc.pdf"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// start process
 		bpmo.startProcess();
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.processInstanceId(bpmo.getBPMOHeader().getProcessInstanceId())
 				.eventType(ProcessAgentEventType.PROCESS_START).singleResult());
 
 		// wait for async jobs
 		bpmoRule.waitForActivitiJobExecutorToProcessAllJobs(30000l, 200l);
 
 		// checkout and complete CheckOrder task
 		bpmo = bpmoRule.getRuntimeService().readBPMO(bpmo.getBPMOHeader().getBPMOUuid(), true,
 				InvoiceProcessActivity.CheckOrder);
 		bpmo.claimTaskInstance(InvoiceProcessActivity.CheckOrder);
 		invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getOrderNumber().setValue("90387595");
 		invoiceAccess.getOrderProfitcenter().setValue("100012345");
 		invoiceAccess.getOrderCostCenter().setValue("3453464");
 		invoiceAccess.getOrderChecked().setValue(true);
 		invoiceAccess.getShipmentChecked().setValue(true);
 		invoiceAccess.getApprover().setValue("alice", PrincipalType.USER);
 		bpmo.completeTaskInstance("approved", null);
 
 		// checkout and complete Approval task
 		bpmoRule.switchUser("alice");
 		bpmo.claimTaskInstance(InvoiceProcessActivity.Approval);
 		bpmo.completeTaskInstance("approved", null);
 
 		// wait for async jobs
 		bpmoRule.waitForActivitiJobExecutorToProcessAllJobs(30000l, 200l);
 
 		// assert process end
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.bpmoUuid(bpmo.getBPMOHeader().getBPMOUuid()).eventType(ProcessAgentEventType.PROCESS_END)
 				.singleResult());
 
 	}
 
 	@BPMOTestUserId("bob")
 	@Test
 	public void testInvoiceDisapproveAndCancel() {
 
 		// create bpmo
 		IBPMO bpmo = bpmoRule.getRuntimeService().createBPMO(Invoice.$BPMO, InvoiceLifecycle.NEW, "001");
 		InvoiceAccess invoiceAccess = new InvoiceAccess(bpmo.getBPMOData());
 		invoiceAccess.getInvoicingParty().setValue("ACME");
 		invoiceAccess.getInvoiceNumber().setValue("10343456");
 		invoiceAccess.getInvoiceDate().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getInvoiceReceived().setValue(ConvertUtils.getXMLGregCalFromDate(new Date()));
 		invoiceAccess.getTermOfPayment().setValue(new BigDecimal(30));
 		invoiceAccess.getOrderNumber().setValue("90387595");
 		invoiceAccess.getTaxPositions().getRows().get(0).getNetAmount().setValue(new BigDecimal("100.98"));
 		try {
 			invoiceAccess.getInvoiceAttachment().setValue("Invoice ACME Inc.pdf", "application/pdf",
 					ResourceReader.getResourceAsBinary(BPMOTestRule.class.getClassLoader(), "Invoice ACME Inc.pdf"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// start process
 		bpmo.startProcess();
 		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
 				.processInstanceId(bpmo.getBPMOHeader().getProcessInstanceId())
 				.eventType(ProcessAgentEventType.PROCESS_START).singleResult());
 
 		// wait for async jobs
 		bpmoRule.waitForActivitiJobExecutorToProcessAllJobs(30000l, 200l);
 
 		// checkout and complete Approval task
 		bpmo = bpmoRule.getRuntimeService().readBPMO(bpmo.getBPMOHeader().getBPMOUuid(), true,
 				InvoiceProcessActivity.Approval);
 		bpmo.claimTaskInstance(InvoiceProcessActivity.Approval);
 		bpmo.completeTaskInstance("disapproved", "not approved");
 
		// checkout and complete CheckOrder task
		bpmo.claimTaskInstance(InvoiceProcessActivity.CheckOrder);
		bpmo.completeTaskInstance("cancel", "was wrong");

		// assert process end
		Assert.assertNotNull(bpmoRule.getRuntimeService().createProcessHistoryQuery()
				.bpmoUuid(bpmo.getBPMOHeader().getBPMOUuid()).eventType(ProcessAgentEventType.PROCESS_CANCEL)
				.singleResult());
 
 	}
 }
