 package ch.hslu.appe.fs1301.business;
 
 import java.util.List;
 
 import com.google.inject.Inject;
 
 import ch.hslu.appe.fs1301.business.shared.AccessDeniedException;
 import ch.hslu.appe.fs1301.business.shared.UserRole;
 import ch.hslu.appe.fs1301.business.shared.iStockAPI;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOConverter;
 import ch.hslu.appe.fs1301.business.shared.dto.DTOZentrallagerBestellung;
 import ch.hslu.appe.fs1301.data.shared.iStockRepository;
 import ch.hslu.appe.fs1301.data.shared.iTransaction;
 
 public class StockAPI extends BaseAPI implements iStockAPI {
 
 	private iStockRepository fStockRepository;
 
 	@Inject
 	protected StockAPI(iStockRepository stockRepository, iTransaction transaction, iInternalSessionAPI sessionAPI) {
 		super(transaction, sessionAPI);
 		fStockRepository = stockRepository;
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
 }
