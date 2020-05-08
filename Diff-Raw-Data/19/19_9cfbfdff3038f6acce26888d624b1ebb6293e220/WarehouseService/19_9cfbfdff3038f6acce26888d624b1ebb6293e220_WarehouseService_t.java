 package scstool.proc;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import scstool.obj.Material;
 import scstool.obj.Material.PartTypes;
 import scstool.utils.Repository;
 
 public class WarehouseService {
 
 	// Lagerwert dieses Artikels
 	private Double warehouseStock = 0.0;
 	// Lagerwert gesamt (aller Materialien)
 	private Double warehouseStockAll = 0.0;
 
 	public Double calcWarehouseStock(Material material) {
 
 		warehouseStock = warehouseStock + material.getAmount()
 				* material.getPrice();
 
 		return warehouseStock;
 	}
 
 	public Double getWarehouseStock() {
 		return warehouseStock;
 	}
 
 	public void setWarehouseStock(Double warehouseStock) {
 		this.warehouseStock = warehouseStock;
 	}
 
 	public void setWarehouseStockAll(Double warehouseStockAll) {
 		this.warehouseStockAll = warehouseStockAll;
 	}
 
 	public Double getFutureWarehouseStock() {
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		List<Material> allesMaterial = dbch.getAllMaterial();
 		LinkedHashMap<Material, Double> futureWarehouse = new LinkedHashMap<>();
 		for (Material m : allesMaterial) {
 			int amount = 0;
 			if (PartTypes.PRODUCT.equals(m.getPartType())
 					|| PartTypes.SUBASSEMBLY.equals(m.getPartType())) {
 				// Sicherheitsbestand am Ende
 				amount += Repository.getInstance().getStafetyStock(m.getId());
 			} else {
 				// Eingehende Bestellungen
 				amount += Repository.getInstance().getArrivalAmountOfMaterial(
 						m, 1);
 				// Lagermenge
 				amount += m.getAmount();
 				// Bedarfsmenge
 				amount -= Repository.getInstance().getNeeds(m, 0);
 			}
 			double stockValue = 0.0;
 			if (amount > 0) {
 				stockValue = amount * m.getPrice();
 				warehouseStockAll = warehouseStockAll + stockValue;
 			}
 			futureWarehouse.put(m, stockValue);
 			
 		}
 		Repository.getInstance().setFutureWarehouse(futureWarehouse);
 		return warehouseStockAll;
 	}
 
 	public Double getProfit() {
 		Repository repo = Repository.getInstance();
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		double ret = 0.0;
 		for (int i = 0; i < 3; i++) {
 			int sellwish = repo.getSellWish(i + 1).getN();
 			double p = dbch.getAllMaterial().get(i).getPrice();
 
 			ret = ret + ((sellwish * 200) - (sellwish * p));
 		}
 
 		// Lagerkosten abziehen;
 		Double stock = getFutureWarehouseStock();
 
 		if (stock >= 250000.0) {
 			ret = ret - ((250000.0 * 0.006) + ((stock - 250000.0) * 0.012))
 					- 5000;
 		} else {
 			ret = ret - stock * 0.006;
 		}
 
		return ret/2;
 	}
 }
