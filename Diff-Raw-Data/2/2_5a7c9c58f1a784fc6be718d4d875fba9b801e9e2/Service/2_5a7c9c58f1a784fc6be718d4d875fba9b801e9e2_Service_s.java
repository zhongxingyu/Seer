 package service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 
 import javax.swing.ImageIcon;
 
 import model.*;
 import model.Process;
 
 import dao.Dao;
 import dao.DaoDb4o;
 import dao.DaoList;
 
 /** 
  * @author Brian, M. C. Høj
  */
 
 public class Service {
 	private static Service service = null;
 	private Dao dao = null;
 
 	private Service() {
 
 		dao = DaoList.getDao();
 		createTestListData();
 
 //		boolean isCreated = new File("db.db4o").exists();
 //		dao = DaoDb4o.getDao();
 //		if (!isCreated) {
//			createTestListData();
 //		}
 
 	}
 
 	public static Service getService() {
 		if (service == null) {
 			service = new Service();
 		}
 		return service;
 	}
 
 	//Depot
 	public List<Depot> getAllDepots() {
 		return dao.getAllDepots();
 	}
 
 	public Depot createDepot(String name, String description,int maxX, int maxY) {
 		Depot depot = new Depot(name, description,maxX,maxY);
 		dao.store(depot);
 		return depot;
 	}
 
 	public void deleteDepot(Depot depot) {
 		dao.delete(depot);
 	}
 
 	//IntermediateProduct
 	public List<IntermediateProduct> getAllIntermediateProducts() {
 		return dao.getAllIntermediateProducts();
 	}
 
 	public IntermediateProduct createIntermediateProduct(String id, ProductType productType, double quantity) {
 		IntermediateProduct intermediateProduct = new IntermediateProduct(id, productType, quantity);
 		dao.store(intermediateProduct);
 		return intermediateProduct;
 	}
 
 	public void StoreIntermediateProduct(IntermediateProduct intermediateProduct) {
 		dao.store(intermediateProduct);
 	}
 
 	public void deleteIntermediateProduct(IntermediateProduct intermediateProduct) {
 		dao.delete(intermediateProduct);
 	}
 
 	public List<IntermediateProduct> getActiveIntermediateProducts(){
 		List<IntermediateProduct> activeP= new ArrayList<IntermediateProduct>();
 		List<IntermediateProduct> allP= dao.getAllIntermediateProducts();
 		for (int i = 0; i < allP.size(); i++) {
 			if (!allP.get(i).isFinished() && !allP.get(i).isDiscarded()){
 				activeP.add(allP.get(i));
 			}
 		}
 
 		return activeP;
 	}
 
 	//ProductType
 	public List<ProductType> getAllProductTypes() {
 		return dao.getAllProductTypes();
 	}
 
 	public ProductType createProductType(String name) {
 		ProductType productType = new ProductType(name);
 		dao.store(productType);
 		return productType;
 	}
 
 	public void storeProductType(ProductType productType){
 		dao.store(productType);
 	}
 
 	public void deleteProductType(ProductType productType) {
 		dao.delete(productType);
 	}
 	
 	public void closeDao(){
 		dao.close();
 	}
 
 	/**
 	 * oprettelse af test data
 	 */
 	public void createTestListData() {
 
 		Depot depot1 = createDepot("Lager 1","Hovedlageret",5,8);
 		Depot depot2 = createDepot("Lager 2","Lager til lort",4,5);
 
 		ProductType pteSkumbananer = createProductType("Skumbananer");
 		pteSkumbananer.setPicture(new ImageIcon("gui/icons/skumbananer.jpg"));
 		ProcessLine plSkumbananer = new ProcessLine("Skumbananer", "Skum", pteSkumbananer);
 		plSkumbananer.createSubProcess(1, "Tilfoej skum", "siger sig selv", 2, 24);
 		Drying d1 = plSkumbananer.createDrying(2, 1, 2*60*10000, 3*60*10000);
 		d1.addDepot(depot1); d1.addDepot(depot2); 
 		plSkumbananer.createSubProcess(3, "Tilsaet chokolade", "siger sig selv", 1, 100);
 		Drying d2 = plSkumbananer.createDrying(4, 1, 2*60*10000, 3*60*10000);
 		d2.addDepot(depot2); 
 
 		ProductType pteChokoKaramelLys = createProductType("Choko Karamel Lys");
 		pteChokoKaramelLys.setPicture(new ImageIcon("gui/icons/choko karamel lys.jpg"));
 		ProcessLine plChokoKaramelLys = new ProcessLine("Choko Karamel Lys", "asdf", pteChokoKaramelLys);
 		plChokoKaramelLys.createSubProcess(1, "Tilsaet karamel", "Lys karamel", 1, 2);
 		Drying d3 = plChokoKaramelLys.createDrying(21, 1*60*10000, 2*60*10000, 3*60*10000);
 		d3.addDepot(depot1); d3.addDepot(depot2);
 
 		ProductType pteChokoKaramelMoerk = createProductType("Choko Karamel Moerk");
 		pteChokoKaramelMoerk.setPicture(new ImageIcon("gui/icons/choko karamel moerk.jpg"));
 		ProcessLine plChokoKaramelMoerk = new ProcessLine("Choko Karamel Moerk", "asdf2", pteChokoKaramelMoerk);
 		plChokoKaramelMoerk.createSubProcess(1, "Tilsaet karamel", "Moerk karamel", 1, 2);
 		Drying d4 = plChokoKaramelMoerk.createDrying(2, 3*60*10000, 4*60*10000, 5*60*10000);
 		d4.addDepot(depot1); d4.addDepot(depot2);
 
 		ProductType pteChokoladelinser = createProductType("Chokoladelinser");
 		pteChokoladelinser.setPicture(new ImageIcon("gui/icons/chokoladelinser.jpg"));
 		ProcessLine plChokoladelinser = new ProcessLine("Chokoladelinser", "nam", pteChokoladelinser);
 		plChokoladelinser.createSubProcess(1, "Tilsaetter chokolade", "tilfoejer chokolade",24, -3);
 		Drying d5 = plChokoladelinser.createDrying(2, 30*10000, 60*10000, 120*10000);
 		d5.addDepot(depot1); d5.addDepot(depot2); 
 		plChokoladelinser.createSubProcess(3, "Tilsaetter linser", "Tilsaetter linser fra optikkeren", 3, 13);
 		Drying d6 = plChokoladelinser.createDrying(4, 1*60*10000, 2*60*10000, 3*60*10000);
 		d6.addDepot(depot1); d6.addDepot(depot2); 
 
 		ProductType pteCitronDrage = createProductType("Citron Drage");
 		pteCitronDrage.setPicture(new ImageIcon("gui/icons/citron drage.jpg"));
 		ProcessLine plCitronDrage = new ProcessLine("Citron Dragé", "ild", pteCitronDrage);
 		plCitronDrage.createSubProcess(1, "Tilsaetter citron", "press en eller to citroner og put dem i",21, -45);
 		Drying d7 = plCitronDrage.createDrying(2, 1*60*10000, 2*60*10000, 3*60*10000);
 		d7.addDepot(depot1); d7.addDepot(depot2); 
 		plCitronDrage.createSubProcess(3, "Tilsaetter linser", "Tilsaetter linser fra optikkeren", 17, 87);
 		Drying d8 = plCitronDrage.createDrying(4, 4*60*10000, 7*60*10000, 8*60*10000);
 		d8.addDepot(depot1); d8.addDepot(depot2); 
 
 		createIntermediateProduct("011", pteSkumbananer, 80);
 		createIntermediateProduct("012", pteCitronDrage, 80);
 		createIntermediateProduct("013", pteChokoladelinser, 100);
 		createIntermediateProduct("014", pteChokoKaramelMoerk, 100);
 		createIntermediateProduct("015", pteChokoKaramelLys, 140);
 
 		createIntermediateProduct("021", pteSkumbananer, 80);
 		createIntermediateProduct("022", pteCitronDrage, 80);
 		createIntermediateProduct("023", pteChokoladelinser, 100);
 		createIntermediateProduct("024", pteChokoKaramelMoerk, 100);
 		createIntermediateProduct("025", pteChokoKaramelLys, 140);
 
 
 		for (int i = 0; i < 5; i++) {
 			getAllIntermediateProducts().get(i).sendToNextProcess(null);
 			getAllIntermediateProducts().get(i).sendToNextProcess(depot1.getStoringSpaces().get(i));
 		}
 
 		for (int i = 5; i < 10; i++) {
 			getAllIntermediateProducts().get(i).sendToNextProcess(null);
 			getAllIntermediateProducts().get(i).sendToNextProcess(depot2.getStoringSpaces().get(i-5));
 		}
 
 	}
 
 	public void createTestDB40Data() {
 		Depot depot1 = createDepot("Lager 1","Hovedlageret",5,8);
 		Depot depot2 = createDepot("Lager 2","Lager til lort",4,5);
 	
 		ProductType pteSkumbananer = createProductType("Skumbananer");
 		pteSkumbananer.setPicture(new ImageIcon("gui/icons/skumbananer.jpg"));
 		ProcessLine plSkumbananer = new ProcessLine("Skumbananer", "Skum", pteSkumbananer);
 		plSkumbananer.createSubProcess(1, "Tilfoej skum", "siger sig selv", 2, 24);
 		Drying d1 = plSkumbananer.createDrying(2, 1, 2*60*1000, 3*60*1000);
 		d1.addDepot(depot1); d1.addDepot(depot2); 
 		plSkumbananer.createSubProcess(3, "Tilsaet chokolade", "siger sig selv", 1, 100);
 		Drying d2 = plSkumbananer.createDrying(4, 1, 2*60*1000, 3*60*1000);
 		d2.addDepot(depot2); 
 
 		storeProductType(pteSkumbananer);
 		
 		ProductType pteChokoKaramelLys = createProductType("Choko Karamel Lys");
 		pteChokoKaramelLys.setPicture(new ImageIcon("gui/icons/choko karamel lys.jpg"));
 		ProcessLine plChokoKaramelLys = new ProcessLine("Choko Karamel Lys", "asdf", pteChokoKaramelLys);
 		plChokoKaramelLys.createSubProcess(1, "Tilsaet karamel", "Lys karamel", 1, 2);
 		Drying d3 = plChokoKaramelLys.createDrying(21, 1*60*1000, 2*60*1000, 3*60*1000);
 		d3.addDepot(depot1); d3.addDepot(depot2);
 
 		storeProductType(pteChokoKaramelLys);
 		
 		ProductType pteChokoKaramelMoerk = createProductType("Choko Karamel Moerk");
 		pteChokoKaramelMoerk.setPicture(new ImageIcon("gui/icons/choko karamel moerk.jpg"));
 		ProcessLine plChokoKaramelMoerk = new ProcessLine("Choko Karamel Moerk", "asdf2", pteChokoKaramelMoerk);
 		plChokoKaramelMoerk.createSubProcess(1, "Tilsaet karamel", "Moerk karamel", 1, 2);
 		Drying d4 = plChokoKaramelMoerk.createDrying(2, 3*60*1000, 4*60*1000, 5*60*1000);
 		d4.addDepot(depot1); d4.addDepot(depot2);
 
 		storeProductType(pteChokoKaramelMoerk);
 		
 		ProductType pteChokoladelinser = createProductType("Chokoladelinser");
 		pteChokoladelinser.setPicture(new ImageIcon("gui/icons/chokoladelinser.jpg"));
 		ProcessLine plChokoladelinser = new ProcessLine("Chokoladelinser", "nam", pteChokoladelinser);
 		plChokoladelinser.createSubProcess(1, "Tilsaetter chokolade", "tilfoejer chokolade",24, -3);
 		Drying d5 = plChokoladelinser.createDrying(2, 30*1000, 60*1000, 120*1000);
 		d5.addDepot(depot1); d5.addDepot(depot2); 
 		plChokoladelinser.createSubProcess(3, "Tilsaetter linser", "Tilsaetter linser fra optikkeren", 3, 13);
 		Drying d6 = plChokoladelinser.createDrying(4, 1*60*1000, 2*60*1000, 3*60*1000);
 		d6.addDepot(depot1); d6.addDepot(depot2); 
 
 		storeProductType(pteChokoladelinser);
 		
 		ProductType pteCitronDrage = createProductType("Citron Drage");
 		pteCitronDrage.setPicture(new ImageIcon("gui/icons/citron drage.jpg"));
 		ProcessLine plCitronDrage = new ProcessLine("Citron Dragé", "ild", pteCitronDrage);
 		plCitronDrage.createSubProcess(1, "Tilsaetter citron", "press en eller to citroner og put dem i",21, -45);
 		Drying d7 = plCitronDrage.createDrying(2, 1*60*1000, 2*60*1000, 3*60*1000);
 		d7.addDepot(depot1); d7.addDepot(depot2); 
 		plCitronDrage.createSubProcess(3, "Tilsaetter linser", "Tilsaetter linser fra optikkeren", 17, 87);
 		Drying d8 = plCitronDrage.createDrying(4, 4*60*1000, 7*60*1000, 8*60*1000);
 		d8.addDepot(depot1); d8.addDepot(depot2);
 		
 		storeProductType(pteCitronDrage);
 		
 	}
 
 }
