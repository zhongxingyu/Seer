 package de.dakror.liturfaliarcest.layer;
 
import java.awt.AlphaComposite;
import java.awt.Composite;
 import java.awt.Graphics2D;
 
import de.dakror.gamesetup.layer.Layer;
import de.dakror.gamesetup.util.Helper;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.settings.Attributes.Attribute;
 import de.dakror.liturfaliarcest.ui.ItemSlot;
 import de.dakror.liturfaliarcest.ui.ToolbarItemSlot;
 
 /**
  * @author Dakror
  */
 public class HUDLayer extends Layer
 {
 	@Override
 	public void init()
 	{
 		int w = Game.player.getInventory().getWidth();
 		for (int i = 0; i < w; i++)
 		{
 			ItemSlot slot = new ToolbarItemSlot((Game.getWidth() - w * ItemSlot.SIZE) / 2 + i * ItemSlot.SIZE, Game.getHeight() - ItemSlot.SIZE);
 			slot.setItemStack(Game.player.getInventory().getSlot(i, 0));
 			components.add(slot);
 		}
 	}
 	
 	@Override
 	public void draw(Graphics2D g)
 	{
		Composite c = g.getComposite();
		if (Game.player.getY() + Game.world.y > Game.getHeight() - 200) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		
 		int s = ItemSlot.SIZE;
 		int w = Game.player.getInventory().getWidth() * s;
 		int x = (Game.getWidth() - w) / 2;
 		
 		Helper.drawProgressBar(x - 4, Game.getHeight() - s - 40, w / 2 + 7, Game.player.getAttributes().get(Attribute.HEALTH) / Game.player.getAttributes().get(Attribute.HEALTH_MAX), "ff3232", g);
 		if (Game.player.getAttributes().get(Attribute.MANA_MAX) > 0) Helper.drawProgressBar(Game.getWidth() / 2 - 3, Game.getHeight() - s - 22, w / 2 + 7, Game.player.getAttributes().get(Attribute.MANA) / Game.player.getAttributes().get(Attribute.MANA_MAX), "009ab8", g);
 		
 		Helper.drawProgressBar(x - 4, Game.getHeight() - s - 22, w / 2 + 7, Game.player.getAttributes().get(Attribute.STAMINA) / Game.player.getAttributes().get(Attribute.STAMINA_MAX), "ffc744", g);
 		
 		Helper.drawContainer(x, Game.getHeight() - s, w, s, false, false, g);
 		
 		drawComponents(g);
		
		g.setComposite(c);
 	}
 	
 	@Override
 	public void update(int tick)
 	{
 		int w = Game.player.getInventory().getWidth();
 		for (int i = 0; i < w; i++)
 			((ItemSlot) components.get(i)).setItemStack(Game.player.getInventory().getSlot(i, 0));
 		
 		updateComponents(tick);
 	}
 }
