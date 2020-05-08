 package ch.hslu.appe.fs1301.business;
 
 import static org.easymock.EasyMock.expect;
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.powermock.api.easymock.PowerMock;
 
 import ch.hslu.appe.fs1301.business.shared.AccessDeniedException;
 import ch.hslu.appe.fs1301.business.shared.UserRole;
 import ch.hslu.appe.fs1301.business.shared.iStockAPI;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOZentrallagerBestellung;
 import ch.hslu.appe.fs1301.data.shared.iStockRepository;
 import ch.hslu.appe.fs1301.data.shared.iTransaction;
 import ch.hslu.appe.fs1301.data.shared.entity.Produkt;
 import ch.hslu.appe.fs1301.data.shared.entity.ZentrallagerBestellung;
 
 public class StockAPITest {
 	private iStockAPI fTestee;
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
 		
 		fStockRepositoryMock.confirmOrderReceivedFromStock(ExpectedId);
 		PowerMock.replayAll();
 		
 		fTestee.confirmOrderReceivedFromStock(ExpectedId);
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
