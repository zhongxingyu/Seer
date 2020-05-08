 package ee.ut.math.tvt.salessystem.ui.model;
 
 import org.apache.log4j.Logger;
 
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 
 /**
  * Main model. Holds all the other models.
  */
 public class SalesSystemModel {
     
     private static final Logger log = Logger.getLogger(SalesSystemModel.class);
 
     // Warehouse model
     private StockTableModel warehouseTableModel;
     
     // Current shopping cart model
     private PurchaseInfoTableModel currentPurchaseTableModel;
     
     private PurchaseHistoryTableModel historyTableModel;
 
     private final SalesDomainController domainController;
 
     /**
      * Construct application model.
      * @param domainController Sales domain controller.
      */
     public SalesSystemModel(SalesDomainController domainController) {
         this.domainController = domainController;
         
         warehouseTableModel = new StockTableModel();
         currentPurchaseTableModel = new PurchaseInfoTableModel();
         historyTableModel = new PurchaseHistoryTableModel();
 
         // populate stock model with data from the warehouse
        updateWarehouseTableModel();
     }
 
     public StockTableModel getWarehouseTableModel() {
         return warehouseTableModel;
     }
     
     public PurchaseInfoTableModel getCurrentPurchaseTableModel() {
         return currentPurchaseTableModel;
     }
 
     public PurchaseHistoryTableModel getPurchaseHistoryTableModel() {
     	return historyTableModel;
     }
     
     public SalesDomainController getDomainController() {
     	return domainController;
     }

	public void updatePurchaseHistoryTableModel() {
		historyTableModel.clear();
		historyTableModel.populateWithData(domainController.getSales());
	}

	public void updateWarehouseTableModel() {
		warehouseTableModel.clear();
		warehouseTableModel.populateWithData(domainController.loadWarehouseState());
	}
 }
