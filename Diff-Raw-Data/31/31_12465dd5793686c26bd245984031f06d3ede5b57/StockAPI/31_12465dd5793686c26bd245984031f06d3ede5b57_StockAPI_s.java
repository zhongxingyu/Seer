 package ch.hslu.appe.fs1301.business;
 
 import java.util.Date;
 import java.util.List;
 
 import ch.hslu.appe.fs1301.business.shared.AccessDeniedException;
 import ch.hslu.appe.fs1301.business.shared.Ticket;
 import ch.hslu.appe.fs1301.business.shared.UserRole;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOConverter;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOZentrallagerBestellung;
 import ch.hslu.appe.fs1301.data.shared.iStockRepository;
 import ch.hslu.appe.fs1301.data.shared.iTransaction;
 import ch.hslu.appe.fs1301.data.shared.entity.Produkt;
 import ch.hslu.appe.fs1301.data.shared.entity.ZentrallagerBestellung;
 import ch.hslu.appe.stock.Stock;
 import ch.hslu.appe.stock.StockException;
 import ch.hslu.appe.stock.StockFactory;
 
 import com.google.inject.Inject;
 
 public class StockAPI extends BaseAPI implements iInternalStockAPI {
 
 	private iStockRepository fStockRepository;
 	private Stock fStock;
 
 	@Inject
 	protected StockAPI(iStockRepository stockRepository, iTransaction transaction, iInternalSessionAPI sessionAPI) {
 		super(transaction, sessionAPI);
 		fStockRepository = stockRepository;
		fStock = StockFactory.getStock();	
 	}
 
 	@Override
 	public List<DTOZentrallagerBestellung> getOpenOrders() throws AccessDeniedException {
 		checkRole(UserRole.ADMIN);
 		
 		return DTOConverter.convertZentrallagerBestellung(fStockRepository.getAll());
 	}
 
 	@Override
 	public void confirmOrderReceivedFromStock(int id) throws AccessDeniedException {
 		checkRole(UserRole.ADMIN);
 		
 		fTransaction.beginTransaction();
 		try {
 			fStockRepository.confirmOrderReceivedFromStock(id);
 		} catch (Exception exception) {
 			fTransaction.rollbackTransaction();
 			return;
 		}
 		fTransaction.commitTransaction();
 	}
 
 	@Override
 	public Date finalizeOrder(List<Ticket> tickets) throws StockException {
 		
 		Date latestDate = new Date();
 		
 		for (Ticket ticket : tickets) {
 			if (fStock.orderItem(ticket.getTicket()) == 0) {
 				throw new StockException("Invalid Ticket");
 			}
 			latestDate = latestDate.before(ticket.getDeliveryDate()) ? ticket.getDeliveryDate() : latestDate;
 		}
 		
 		return latestDate;
 	}
 	
 	public Ticket reserveItem(Produkt produkt, int anzahl) throws StockException {
 		String produktID = generateArticelIDforStock(produkt.getId());
 		long ticket = fStock.reserveItem(produktID, anzahl);
 		if (ticket == -1) throw new StockException("Not Enough Items in Stock");
 		Date deliveryDate = fStock.getItemDeliveryDate(produktID);
 		
 		ZentrallagerBestellung stockOrder = new ZentrallagerBestellung();
 		stockOrder.setProdukt(produkt);
 		stockOrder.setAnzahl(anzahl);
 		stockOrder.setLiefertermin(deliveryDate);
 		
 		fStockRepository.persistObject(stockOrder);
 		
 		return new Ticket(ticket, deliveryDate);
 	}
 	
 	private String generateArticelIDforStock(int productID)
 	{
 		return String.format("%06d", productID);
 	}
 
 	@Override
 	public void cancelReservedTickets(List<Ticket> tickets) throws StockException {
 		for (Ticket ticket : tickets) {
 			fStock.freeItem(ticket.getTicket());
 		}
 	}
 }
