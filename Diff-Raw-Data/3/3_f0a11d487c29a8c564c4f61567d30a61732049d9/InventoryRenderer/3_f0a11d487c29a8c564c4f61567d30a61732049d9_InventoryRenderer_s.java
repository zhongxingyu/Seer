 package ui.isometric.sublayers;
 
 import game.Container;
 import game.GameThing;
 import game.things.EquipmentGameThing;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import ui.isometric.IsoCanvas;
 import ui.isometric.IsoInterface;
 import ui.isometric.abstractions.IsoPlayer;
 import ui.isometric.libraries.IsoCharacterImageLibrary;
 import ui.isometric.libraries.IsoInventoryImageLibrary;
 import util.Resources;
 
 /**
  * Renderer the players inventory
  * 
  * @author melby
  *
  */
 public class InventoryRenderer extends LargePanel {
 	private IsoPlayer player;
 	private IsoInterface inter;
 	
 	private static BufferedImage weapon_slot = null;
 	private static BufferedImage armour_slot = null;
 	private static BufferedImage helmet_slot = null;
 	private static BufferedImage gauntlets_slot = null;
 	private static BufferedImage boots_slot = null;
 	private static BufferedImage cloak_slot = null;
 	private static BufferedImage shield_slot = null;
 	
 	private static BufferedImage empty_slot = null;
 	
 	private static int imageSize = 44;
 	
 	private Set<ClickArea> permAreas = new HashSet<ClickArea>();
 	private Set<ClickArea> dynmAreas = new HashSet<ClickArea>();
 	
 	/**
 	 * Private data structure for storing click areas
 	 * 
 	 * @author melby
 	 *
 	 */
 	private static class ClickArea {
 		/**
 		 * An action to be performed when a ClickArea is selected
 		 * 
 		 * @author melby
 		 *
 		 */
 		private static interface ClickAction {
 			/**
 			 * A default ClickAction that deals with GameThings
 			 * This will show a popup with interactions etc
 			 * 
 			 * @author melby
 			 *
 			 */
 			static class GameThingAction implements ClickAction {
 				private GameThing thing;
 				
 				/**
 				 * Create a GameThingAction with a given GameThing
 				 * @param thing
 				 */
 				public GameThingAction(GameThing thing) {
 					this.thing = thing;
 				}
 				
 				@Override
 				public void perform(MouseEvent e, Point p, final InventoryRenderer inven) {
 					if(e.getButton() == MouseEvent.BUTTON3) { // Right click
 						JPopupMenu popup = new JPopupMenu();
 						for(String s : thing.interactions()) {
 							JMenuItem item = new JMenuItem(s);
 							item.addActionListener(new ActionListener() {
 								@Override
 								public void actionPerformed(ActionEvent e) {
 									Object s = e.getSource();
 									
 									if(s instanceof JMenuItem) {
 										JMenuItem m = (JMenuItem)s;
 										inven.ui().performActionOn(m.getText(), thing);
 									}
 								}
 							});
 							popup.add(item);
 						}
 						popup.show(inven.superview(), e.getPoint().x, e.getPoint().y);
 					}
 					else {
 						inven.ui().performActionOn(thing.defaultInteraction(), thing);
 					}
 				}
 			}
 			
 			/**
 			 * Perform this action
 			 * @param e - the event that caused this action
 			 * @param p - the point in the InventoryRenderer this happened
 			 * @param inven - the InventoryRenderer that this action is in
 			 */
 			void perform(MouseEvent e, Point p, InventoryRenderer inven);
 		}
 		
 		private int x;
 		private int y;
 		private int w;
 		private int h;
 		private ClickAction action;
 		
 		/**
 		 * Create a ClickArea with a given rect and action
 		 * @param x
 		 * @param y
 		 * @param w
 		 * @param h
 		 * @param action
 		 */
 		public ClickArea(int x, int y, int w, int h, ClickAction action) {
 			this.x = x;
 			this.y = y;
 			this.w = w;
 			this.h = h;
 			this.action = action;
 		}
 		
 		/**
 		 * Get the x coord
 		 * @return
 		 */
 		public int x() { return x; }
 		
 		/**
 		 * Get the y coord
 		 * @return
 		 */
 		public int y() { return y; };
 		
 		/**
 		 * Get the width
 		 * @return
 		 */
 		public int width() { return w; }
 		
 		/**
 		 * Get the height
 		 * @return
 		 */
 		public int height() { return h; }
 		
 		/**
 		 * Perform the action this ClickArea contains
 		 * @param e
 		 * @param p
 		 * @param inven
 		 */
 		public void perform(MouseEvent e, Point p, InventoryRenderer inven) {
 			if(action != null) {
 				action.perform(e, p, inven);
 			}
 		}
 	}
 	
 	/**
 	 * Create an InventoryRenderer with a given ui and percentage x/y coord
 	 * @param inter
 	 * @param x
 	 * @param y
 	 */
 	public InventoryRenderer(IsoInterface inter, double x, double y) {
 		super(x, y);
 		
 		synchronized(InventoryRenderer.class) {
 			if(weapon_slot == null) {
 				try {
 					weapon_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_weapon.png");
 					armour_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_armour.png");
 					helmet_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_helmet.png");
 					gauntlets_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_gauntlets.png");
 					boots_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_boots.png");
 					cloak_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_cloak.png");
 					shield_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_shield.png");
 					empty_slot = Resources.readImageResourceUnfliped("/resources/ui/slot_empty.png");
 					
 //					permAreas.add(new ClickArea(230, 30, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Helmet");
 //						}
 //						
 //					})); // Helmet
 //					permAreas.add(new ClickArea(120, 50, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Cloak");
 //						}
 //						
 //					})); // Cloak
 //					permAreas.add(new ClickArea(100, 100, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Armour");
 //						}
 //						
 //					})); // Armour
 //					permAreas.add(new ClickArea(110, 150, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Weapon");
 //						}
 //						
 //					})); // Weapon
 //					permAreas.add(new ClickArea(270, 120, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Shield");
 //						}
 //						
 //					})); // Shield
 //					permAreas.add(new ClickArea(260, 200, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Boots");
 //						}
 //						
 //					})); // Boots
 //					permAreas.add(new ClickArea(50, 150, imageSize+2, imageSize+2, new ClickArea.ClickAction() {
 //						@Override
 //						public void perform(MouseEvent e, Point p,
 //								InventoryRenderer inven) {
 //							System.out.println("Gauntlets");
 //						}
 //						
 //					})); // Gauntlets
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		this.inter = inter;
 		this.player = inter.player();
 	}
 
 	@Override
 	protected void drawContents(Graphics g) { // TODO: add scrolling / clipping
 		dynmAreas.clear();
 		
 		drawEquipment(g);
 		drawInventory(g);
 		drawContainer(g);
 	}
 
 	/**
 	 * Draw the container view
 	 * @param g
 	 */
 	private void drawContainer(Graphics g) {
 		int origx = 425;
 		int x = origx;
 		int y = 10;
 		int width = 150;
 		int height = 590;
 		int spacing = 46;
 		
 		g.drawRect(x, y, width, height);
 		
 		x++;
 		y++;
 		
 		Container container = player.openContainer();
 		if(container != null) {
 			for(GameThing thing : container) {
 				this.drawThingAt(g, thing, x, y);
 				
 				x += spacing;
 				if(x >= origx + width) {
 					y += spacing;
 					x = origx;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draw the equipment
 	 * @param g
 	 */
 	private void drawEquipment(Graphics g) {
 		GameThing thing;
 		
 		g.drawRect(10, 10, 400, 280);
 		g.drawImage(IsoCharacterImageLibrary.imageForCharacterName(player.characterName()), 110, 20, null);
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.HELMET);
 		if(thing != null) {
 			g.drawImage(empty_slot, 230, 30, null);
 			this.drawThingAt(g, thing, 231, 31);
 		}
 		else {
 			g.drawImage(helmet_slot, 230, 30, null);
 		}
 		
 //		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.CLOAK);
 //		if(thing != null) {
 //			g.drawImage(empty_slot, 230, 30, null);
 //			this.drawThingAt(g, thing, 121, 51);
 //		}
 //		else {
 //			g.drawImage(cloak_slot, 120, 50, null);
 //		}
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.ARMOUR);
 		if(thing != null) {
 			g.drawImage(empty_slot, 100, 100, null);
 			this.drawThingAt(g, thing, 101, 101);
 		}
 		else {
 			g.drawImage(armour_slot, 100, 100, null);
 		}
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.WEAPON);
 		if(thing != null) {
 			g.drawImage(empty_slot, 110, 150, null);
 			this.drawThingAt(g, thing, 111, 151);
 		}
 		else {
 			g.drawImage(weapon_slot, 110, 150, null);
 		}
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.SHIELD);
 		if(thing != null) {
 			g.drawImage(empty_slot, 270, 120, null);
 			this.drawThingAt(g, thing, 271, 121);
 		}
 		else {
 			g.drawImage(shield_slot, 270, 120, null);
 		}
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.BOOTS);
 		if(thing != null) {
 			g.drawImage(empty_slot, 260, 200, null);
 			this.drawThingAt(g, thing, 261, 201);
 		}
 		else {
 			g.drawImage(boots_slot, 260, 200, null);
 		}
 		
 		thing = player.getEquipmentForSlot(EquipmentGameThing.Slot.GAUNTLET);
 		if(thing != null) {
 			g.drawImage(empty_slot, 50, 150, null);
 			this.drawThingAt(g, thing, 51, 151);
 		}
 		else {
 			g.drawImage(gauntlets_slot, 50, 150, null);
 		}
 	}
 
 	/**
 	 * Draw the inventory
 	 * @param g
 	 */
 	private void drawInventory(Graphics g) {
 		int xorig = 10;
 		int x = xorig;
 		int y = 305;
 		int width = 400;
 		int height = 295;
 		int spacing = 46;
 		
 		g.drawRect(x, y, width, height);
 		
 		x++;
 		y++;
 		
 		Container inventory = player.inventory();
 		if(inventory != null) {
 			for(GameThing thing : inventory) {
 				this.drawThingAt(g, thing, x, y);
 				
 				x += spacing;
 				if(x >= xorig + width) {
 					y += spacing;
 					x = xorig;
 				}
 			}
 		}
 	}
 
 	private void drawThingAt(Graphics g, GameThing thing, int x, int y) {
 		BufferedImage i = IsoInventoryImageLibrary.imageForName(thing.renderer());
 		
 		if(i != null) { // TODO: placeholder image '?'?
 			g.drawImage(i, x, y, null);
 			dynmAreas.add(new ClickArea(x, y, imageSize, imageSize, new ClickArea.ClickAction.GameThingAction(thing)));
 		}
 		else {
 			System.out.println("null image for " + thing.renderer());
 		}
 	}
 
 	@Override
 	protected void mouseDown(MouseEvent e, Point p, IsoCanvas canvas) {
 		for(ClickArea a : dynmAreas) {
 			if(this.pointInRect(p, a.x(), a.y(), a.width(), a.height())) {
 				a.perform(e, p, this);
 				return;
 			}
 		}
 		for(ClickArea a : permAreas) {
 			if(this.pointInRect(p, a.x(), a.y(), a.width(), a.height())) {
 				a.perform(e, p, this);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Get the IsoInterface that is managing this Object
 	 * @return
 	 */
 	private IsoInterface ui() {
 		return inter;
 	}
 }
