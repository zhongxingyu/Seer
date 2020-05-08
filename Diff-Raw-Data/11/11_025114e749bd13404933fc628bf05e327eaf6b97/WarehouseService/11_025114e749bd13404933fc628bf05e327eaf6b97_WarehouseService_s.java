 package scstool.proc;
 
 import java.util.List;
 
 import scstool.obj.Material;
 
 public class WarehouseService {
 	
	DatabaseContentHandler dbch = DatabaseContentHandler.get();
 	//Lagerwert dieses Artikels
 	private Double warehouseStock = 0.0;
 	//Lagerwert gesamt (aller Materialien)
 	private Double warehouseStockAll = 0.0;
	//Liste aller Materiealien
	private List<Material> allesMaterial = dbch.getAllMaterial();
 
 	public Double calcWarehouseStock(Material material){
 		
 		warehouseStock = warehouseStock + material.getAmount() * material.getPrice();
 		
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
 	
 	public Double getWarehouseStockAll() {
		
 		for (Material m : allesMaterial) {
 			warehouseStockAll = warehouseStockAll + m.getAmount() * m.getPrice();
 		}
 		return warehouseStockAll;
 	}
 
 }
