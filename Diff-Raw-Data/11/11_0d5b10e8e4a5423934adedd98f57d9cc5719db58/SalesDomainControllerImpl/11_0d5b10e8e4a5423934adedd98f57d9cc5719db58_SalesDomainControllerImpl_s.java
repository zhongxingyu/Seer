 package ee.ut.math.tvt.salessystem.domain.controller.impl;
 
 import java.text.ParseException;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import ee.ut.math.tvt.salessystem.domain.controller.ConfirmationStatusEvent;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.HistoryItem;
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.domain.exception.VerificationFailedException;
 import ee.ut.math.tvt.salessystem.ui.model.StockTableModel;
 import ee.ut.math.tvt.salessystem.ui.tabs.ConfirmationTab;
 import ee.ut.math.tvt.salessystem.util.HibernateUtil;
 
 /**
  * Implementation of the sales domain controller.
  */
 public class SalesDomainControllerImpl implements SalesDomainController {	
     protected Vector<ConfirmationStatusEvent> _listeners;    
     ConfirmationTab confirmationPopup;
     private Session session = HibernateUtil.currentSession();
     
     private static final Logger log = Logger.getLogger(StockTableModel.class);
     
     public void addConfirmationStatusListener(ConfirmationStatusEvent listener) {
 		if (_listeners == null) {
 			_listeners = new Vector<ConfirmationStatusEvent>();
 		}
 			
 		_listeners.addElement(listener);
 	}
 	
 	protected void confirmTransaction(HistoryItem sale, boolean success) {
 		if(success) {
 			session.beginTransaction();
 			
 			//Insert the new sale
 			session.save(sale);
 			
 			//Insert the solditems for this sale and decrease quantity for stockitems
 			Set<SoldItem> items = sale.getSoldItems();
 			for(SoldItem item : items) {
 				item.setHistoryItem(sale);
 				StockItem stockItem = item.getStockItem();
 				stockItem.decreaseQuantity(item.getQuantity());
 				
				try {
					session.save(stockItem);
				} catch (Exception e) {
					log.warn("Save failed, merging instead");
					session.merge(stockItem);
				}
				
 				session.save(item);
 			}
 			
 			session.getTransaction().commit();
 		}
 		
 		if (_listeners != null) {
 			for(ConfirmationStatusEvent e : _listeners) {
 				e.SaleConfirmed(success);
 			}
 		}
 	}
     
     public SalesDomainControllerImpl() {
     	confirmationPopup = new ConfirmationTab();
     }
 	
 	public void submitCurrentPurchase(List<SoldItem> goods) throws VerificationFailedException {
 		double sum = 0;
 		for(SoldItem item : goods) {
 			sum += item.getSum();
 		}
 		
 		final HistoryItem sale = new HistoryItem(goods);
 		
 		confirmationPopup.setCost(sum);
 		confirmationPopup.addConfirmationStatusListener(new ConfirmationStatusEvent() {
 			public void SaleConfirmed(boolean success) {
 				confirmTransaction(sale, success);
 			}
 		});
 		
 		try {
 			confirmationPopup.draw().setVisible(true);
 		} catch (ParseException e) {}
 	}
 
 	public void cancelCurrentPurchase() throws VerificationFailedException {				
 		// XXX - Cancel current purchase
 	}
 
 	public void startNewPurchase() throws VerificationFailedException {
 		// XXX - Start new purchase
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<StockItem> loadWarehouseState() {
 		System.out.println("StockItem returned "+session.createQuery("from StockItem").list().size() + " rows.");
 		return session.createQuery("from StockItem").list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<HistoryItem> getSales() {
 		System.out.println("HistoryItem returned "+session.createQuery("from HistoryItem").list().size() + " rows.");
 		return session.createQuery("from HistoryItem").list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<SoldItem> getSoldItems() {
 		System.out.println("SoldItem returned "+session.createQuery("from SoldItem").list().size() + " rows.");
 		return session.createQuery("from SoldItem").list();
 	}
 	
 	public void endSession() {
 		//HibernateUtil.closeSession();
 	}
 
 	@Override
 	public void addStock(StockItem Product) {
 		session.beginTransaction();
 		
		session.saveOrUpdate(Product);
 		
 		session.getTransaction().commit();
 	}
 }
