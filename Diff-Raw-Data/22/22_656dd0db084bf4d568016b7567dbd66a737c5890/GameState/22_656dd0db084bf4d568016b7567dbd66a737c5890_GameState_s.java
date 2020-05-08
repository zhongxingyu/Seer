 package cz.cvut.fit.dpo.adventure.model;
 
 import java.util.List;
 
 import cz.cvut.fit.dpo.adventure.model.builder.WorldDefinition;
 import cz.cvut.fit.dpo.adventure.model.facade.GameModelSpiFacade;
 
 public class GameState {
 
 	private final GameModelSpiFacade modelSpi;
 	
 	private ILocation currentLocation;
 	private Container inventory;
 	
 	public GameState(GameModelSpiFacade modelSpi, WorldDefinition builder) {
 		this.modelSpi = modelSpi;
 		this.currentLocation = builder.initialLocation();
 		this.inventory = new Container();
 		for (IGameObject item : builder.initialInventory()) {
 			inventory.spawn(item);
 		}
 	}
 	
 	public void examine(IGameObject item) {
 		item.examine(modelSpi);
 	}
 
 	public void exitTo(ILocation exit) {
 		currentLocation = exit;
 		modelSpi.createMajorEvent();
 	}
 
 	public void pickUp(IGameObject item) {
		if (item.pickUp(modelSpi)) {
			currentLocation.transferItemTo(item, inventory);
 		}
 	}
 
 	public void drop(IGameObject item) {
		inventory.transferItemTo(item, currentLocation);
		modelSpi.createSimpleEvent("You dropped the " + item.name());
 	}
 
 	public void useOn(IGameObject what, IGameObject target) {
 		target.useOn(what, modelSpi);
 	}
 	
 	public List<IGameObject> itemsCarried() {
 		return inventory.content();
 	}
 	
 	public void removeFromInventory(IGameObject item) {
 		inventory.destroy(item);
 	}
 	
 	public ILocation currentLocation() {
 		return currentLocation;
 	}
 	
 	
 }
