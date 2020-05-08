 package org.concordia.kingdoms.board;
 
 import org.concordia.kingdoms.domain.Component;
 import org.concordia.kingdoms.domain.Tile;
 import org.concordia.kingdoms.domain.TileType;
 
 public class GoldMineDecorator implements IDecorator {
 
 	private Component component;
 
 	public GoldMineDecorator(Component component) {
 		this.component = component;
 	}
 
 	public Integer getValue() {
 		if (this.component == null) {
 			return null;
 		}
 		if (this.component instanceof Tile) {
 			final Tile tile = (Tile) this.component;
 			if (tile.getType().equals(TileType.RESOURCE)
 					|| tile.getType().equals(TileType.HAZARD)) {
 				return 2 * this.component.getValue();
 			}
 		}
		return 0;
 	}
 
 	public String getName() {
 		return this.component.getName();
 	}
 
 	public IDecorator setComponent(Component component) {
 		this.component = component;
 		return this;
 	}
 
 }
