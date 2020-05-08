 package ch.hslu.appe.fs1301.business;
 
 import static org.easymock.EasyMock.expect;
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.powermock.api.easymock.PowerMock;
 
 import ch.hslu.appe.fs1301.business.shared.AccessDeniedException;
 import ch.hslu.appe.fs1301.business.shared.Ticket;
 import ch.hslu.appe.fs1301.business.shared.UserRole;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOZentrallagerBestellung;
 import ch.hslu.appe.fs1301.data.shared.iStockRepository;
 import ch.hslu.appe.fs1301.data.shared.iTransaction;
 import ch.hslu.appe.fs1301.data.shared.entity.Produkt;
 import ch.hslu.appe.fs1301.data.shared.entity.ZentrallagerBestellung;
 import ch.hslu.appe.stock.StockException;
 
 public class StockAPITest {
 	private iInternalStockAPI fTestee;
 	private iStockRepository fStockRepositoryMock;
 	private iInternalSessionAPI fSessionMock;
 	private iTransaction fTransactionMock;
 	
 	@Before
 	public void setUp() {	
 		fStockRepositoryMock = PowerMock.createMock(iStockRepository.class);
 		fSessionMock = PowerMock.createMock(iInternalSessionAPI.class);
 		fTransactionMock = PowerMock.createMock(iTransaction.class);
 		fTestee = new StockAPI(fStockRepositoryMock, fTransactionMock, fSessionMock);		
 	}
 	
 	@After
 	public void cleanUp() {
 		PowerMock.niceReplayAndVerify();
 	}
 	
 	@Test(expected = AccessDeniedException.class)
 	public void needsAdmin_OnGetOpenOrders() throws AccessDeniedException {
 		final int ExpectedRole = UserRole.ADMIN;
 		
 		//Check against role
 		expect(fSessionMock.hasRole(ExpectedRole)).andReturn(false);
 		PowerMock.replayAll();
 		
 		fTestee.getOpenOrders();
 	}
 	
 	@Test
 	public void returnsListAsDTO_OnGetOpenOrder() throws AccessDeniedException{
 		setupCheckRoleIrrelevant();
 		
 		List<ZentrallagerBestellung> jpaList = createOrdersById(1, 2);
 		expect(fStockRepositoryMock.getAll()).andReturn(jpaList);
 		PowerMock.replayAll();
 		
 		List<DTOZentrallagerBestellung> resultList = fTestee.getOpenOrders();
 		
 		
 		for(int i = 0; i < jpaList.size(); i++) {
 			assertThat(resultList.get(i).getId()).isSameAs(jpaList.get(i).getId());
 		}
 	}
 	
 	@Test(expected = AccessDeniedException.class)
 	public void needsAdmin_OnConfirmOrderReceivedFromStock() throws AccessDeniedException {
 		final int ExpectedRole = UserRole.ADMIN;
 		
 		//Check against role
 		expect(fSessionMock.hasRole(ExpectedRole)).andReturn(false);
 		PowerMock.replayAll();
 		
 		fTestee.confirmOrderReceivedFromStock(1);
 	}
 	
 	@Test
 	public void forwardsConfirmOrderReceivedFromStock() throws AccessDeniedException {
 		final int ExpectedId = 56;
 		setupCheckRoleIrrelevant();
 		fTransactionMock.beginTransaction();
 		fStockRepositoryMock.confirmOrderReceivedFromStock(ExpectedId);
 		fTransactionMock.commitTransaction();
 		PowerMock.replayAll();
 		
 		fTestee.confirmOrderReceivedFromStock(ExpectedId);
 	}
 	
 	@Test
 	public void rollBacksTransaction_OnConfirmOrderReceivedFromStock_WhenAnExceptionOccurs() throws AccessDeniedException {
 		final int ExpectedId = 56;
 		setupCheckRoleIrrelevant();
 		fTransactionMock.beginTransaction();
 		fStockRepositoryMock.confirmOrderReceivedFromStock(ExpectedId);
 		EasyMock.expectLastCall().andThrow(new RuntimeException());
 		fTransactionMock.rollbackTransaction();
 		PowerMock.replayAll();
 		
 		fTestee.confirmOrderReceivedFromStock(ExpectedId);
 	}
 	
 	@Test
 	public void reserveItem() throws StockException {
 		Produkt produktMock = PowerMock.createMock(Produkt.class);
 		expect(produktMock.getId()).andReturn(10);
 		fStockRepositoryMock.persistObject(EasyMock.anyObject(ZentrallagerBestellung.class));
 		EasyMock.expectLastCall();
 		
 		PowerMock.replayAll();
 		
		fTestee.reserveItem(produktMock, 20);
 	}
 	
 	@Test
	public void reserveItem_AndFinalizeOrder() throws StockException {
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		
 		Produkt produktMock = PowerMock.createMock(Produkt.class);
 		Produkt produktMock2 = PowerMock.createMock(Produkt.class);
		expect(produktMock.getId()).andReturn(50);
		expect(produktMock2.getId()).andReturn(30);
 		fStockRepositoryMock.persistObject(EasyMock.anyObject(ZentrallagerBestellung.class));
 		EasyMock.expectLastCall().times(2);
 		
 		PowerMock.replayAll();
 		
		tickets.add(fTestee.reserveItem(produktMock, 20));
		tickets.add(fTestee.reserveItem(produktMock2, 30));
 		fTestee.finalizeOrder(tickets);
 	}
 	
 	@Test(expected = StockException.class)
 	public void reserveItem_AndFinalizeOrder_ThrowStockException() throws StockException {
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		tickets.add(new Ticket(124124l, new Date()));
 		
 		PowerMock.replayAll();
 		
 		fTestee.finalizeOrder(tickets);
 	}
 	
 	@Test(expected = StockException.class)
 	public void reserveItem_AndThrowStockException() throws StockException {
 		Produkt produktMock = PowerMock.createMock(Produkt.class);
		expect(produktMock.getId()).andReturn(10);
 		
 		PowerMock.replayAll();
 		
		fTestee.reserveItem(produktMock, 1000);
 	}
 	
 	@Test
 	public void cancelTicket() throws StockException  {
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		tickets.add(new Ticket(5125124l, new Date()));
 		
 		PowerMock.replayAll();
 		
 		fTestee.cancelReservedTickets(tickets);
 	}
 	
 	private void setupCheckRoleIrrelevant() {
 		expect(fSessionMock.hasRole(EasyMock.anyInt())).andReturn(true);
 	}
 	
 	private List<ZentrallagerBestellung> createOrdersById(int... ids){
 		Produkt product = new Produkt();
 		product.setId(19);
 		List<ZentrallagerBestellung> list = new ArrayList<ZentrallagerBestellung>(ids.length);
 		for(int id : ids) {
 			ZentrallagerBestellung order = new ZentrallagerBestellung();
 			order.setId(id);
 			order.setProdukt(product);
 			list.add(order);
 		}
 		return list;		
 	}
 }
