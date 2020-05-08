 package de.bplaced.mopfsoft.blocks;
 
 import org.newdawn.slick.Color;
 
 import de.bplaced.mopfsoft.items.Item;
 
 public class Air extends Block{
 
 	public Air(int x, int y) {
 		super(x, y);
 	}
 
 	@Override
 	public int getStrength(Item item) {
 		return -1;
 	}
 
 	@Override
 	public int getDrop() {
 		return -1;
 	}
 
 	@Override
 	public Color getColor() {
		return Color.transparent;
 	}
 
 	@Override
 	public int getBid() {
 		return 0;
 	}
 
 }
