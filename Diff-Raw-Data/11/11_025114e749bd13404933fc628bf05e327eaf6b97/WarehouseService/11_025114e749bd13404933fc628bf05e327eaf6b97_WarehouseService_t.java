 package scstool.proc;
 
 import java.util.List;
 
 import scstool.obj.Material;
 
 public class WarehouseService {
 	
	
 	//Lagerwert dieses Artikels
 	private Double warehouseStock = 0.0;
 	//Lagerwert gesamt (aller Materialien)
 	private Double warehouseStockAll = 0.0;
 
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
		DatabaseContentHandler dbch = DatabaseContentHandler.get();
		List<Material> allesMaterial = dbch.getAllMaterial();
 		for (Material m : allesMaterial) {
 			warehouseStockAll = warehouseStockAll + m.getAmount() * m.getPrice();
 		}
 		return warehouseStockAll;
 	}
 
 }
