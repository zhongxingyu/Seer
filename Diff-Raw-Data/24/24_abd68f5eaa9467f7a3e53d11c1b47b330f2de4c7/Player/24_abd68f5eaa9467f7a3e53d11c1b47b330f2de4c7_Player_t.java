 package de.dakror.liturfaliarcest.game.entity.creature;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
import org.json.JSONObject;

import de.dakror.gamesetup.util.Vector;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.game.entity.Entity;
 import de.dakror.liturfaliarcest.game.entity.object.ItemDrop;
 import de.dakror.liturfaliarcest.game.world.World;
import de.dakror.liturfaliarcest.layer.HUDLayer;
 import de.dakror.liturfaliarcest.layer.TalkLayer;
 import de.dakror.liturfaliarcest.settings.Attributes.Attribute;
import de.dakror.liturfaliarcest.settings.Inventory;
 
 /**
  * @author Dakror
  */
 public class Player extends Creature
 {
 	/**
 	 * a, w, d, s
 	 */
 	boolean[] dirs = { false, false, false, false };
 	boolean sprint = true;
 	
 	private Entity clickTarget;
 	
 	public Player(int x, int y)
 	{
 		super(x, y, 64, 96, new JSONObject());
 		tex = "char/chars/105-Civilian05.png";
 		attr.set(Attribute.SPEED, 2);
 		inv = new Inventory(8, 5);
 		
 		bumpY = 70;
 		bumpX = 16;
 		bumpWidth = width / 2;
 		bumpHeight = 24;
 		sprint = false;
 		
 		attr.setWithMax(Attribute.HEALTH, 10);
 		attr.setWithMax(Attribute.STAMINA, 10);
 		attr.set(Attribute.SPEED, 2);
 		
 		uid = 0;
 	}
 	
 	@Override
 	protected void tick(int tick)
 	{
 		if (dirs[0] || dirs[1] || dirs[2] || dirs[3]) target = null;
 		
 		if (Game.currentGame.alpha != 0)
 		{
 			dirs = new boolean[] { false, false, false, false };
 			target = null;
 		}
 		
 		if (target != null && startTick == 0)
 		{
 			startTick = tick;
 			return;
 		}
 		
 		if (target != null && (tick - startTick) % (30 / attr.get(Attribute.SPEED)) == 0) frame = (frame + 1) % 4;
 		
 		float spe = 0.025f;
 		if (sprint && attr.get(Attribute.STAMINA) > 0 && (dirs[0] || dirs[1] || dirs[2] || dirs[3] || target != null))
 		{
 			attr.set(Attribute.SPEED, 5);
 			attr.add(Attribute.STAMINA, -spe);
 		}
 		else
 		{
 			attr.set(Attribute.SPEED, 2);
			if (attr.get(Attribute.STAMINA) < attr.get(Attribute.STAMINA_MAX) && Game.currentGame.getActiveLayer() instanceof HUDLayer && Game.currentGame.alpha == 0)
 			{
 				if ((dirs[0] || dirs[1] || dirs[2] || dirs[3] || target != null) && sprint) sprint = false;
 				
 				float dif = attr.get(Attribute.STAMINA_MAX) - attr.get(Attribute.STAMINA);
 				attr.add(Attribute.STAMINA, dif > 2 * spe ? 2 * spe : dif);
 			}
 		}
 		
 		if (!frozen)
 		{
 			if ((dirs[0] || dirs[1] || dirs[2] || dirs[3]))
 			{
 				Vector lastPos = pos.clone();
 				
 				float speed = attr.get(Attribute.SPEED);
 				
 				if (dirs[0] && isFree(-speed, 0)) pos.x -= speed;
 				if (dirs[2] && isFree(speed, 0)) pos.x += speed;
 				
 				if (dirs[1] && isFree(0, -speed)) pos.y -= speed;
 				if (dirs[3] && isFree(0, speed)) pos.y += speed;
 				
 				Vector dist = pos.clone().sub(lastPos);
 				if (dist.getLength() > 1)
 				{
 					dist.setLength(speed);
 					pos = lastPos.add(dist);
 				}
 				
 				checkForOnEnterEvent();
 				
 				if (tick % (30 / attr.get(Attribute.SPEED)) == 0) frame = (frame + 1) % 4;
 			}
 			else if (target == null) frame = 0;
 			
 			if (Game.world.width > Game.getWidth())
 			{
 				Game.world.x = (int) (Game.getWidth() / 2 - pos.x - width / 2);
 				if (Game.world.x > 0) Game.world.x = 0;
 				if (Game.world.x + Game.world.width < Game.getWidth()) Game.world.x += Game.getWidth() - (Game.world.x + Game.world.width);
 			}
 			else Game.world.x = (Game.getWidth() - Game.world.width) / 2;
 			
 			if (Game.world.height > Game.getHeight())
 			{
 				Game.world.y = (int) (Game.getHeight() / 2 - pos.y - height / 2);
 				if (Game.world.y > bumpY) Game.world.y = bumpY;
 				if (Game.world.y + Game.world.height < Game.getHeight()) Game.world.y += Game.getHeight() - (Game.world.y + Game.world.height);
 			}
 			else Game.world.y = (Game.getHeight() - Game.world.height) / 2;
 		}
 		else frame = 0;
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e)
 	{
 		super.mouseMoved(e);
 		
 		if (!frozen)
 		{
 			float degs = pos.clone().add(new Vector(width / 2, height / 2)).add(new Vector(Game.world.x, Game.world.y)).sub(new Vector(Game.currentGame.mouse)).getAngleOnXAxis();
 			if (degs < 0) degs += 360;
 			
 			if (degs < 45 || degs > 315) dir = 1;
 			else if (degs > 135 && degs < 225) dir = 2;
 			else if (degs > 45 && degs < 135) dir = 3;
 			else dir = 0;
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		if (e.getKeyCode() == KeyEvent.VK_A) dirs[0] = true;
 		if (e.getKeyCode() == KeyEvent.VK_W) dirs[1] = true;
 		if (e.getKeyCode() == KeyEvent.VK_D) dirs[2] = true;
 		if (e.getKeyCode() == KeyEvent.VK_S) dirs[3] = true;
 		if (e.getKeyCode() == KeyEvent.VK_SHIFT) sprint = true;
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
 		if (e.getKeyCode() == KeyEvent.VK_A) dirs[0] = false;
 		if (e.getKeyCode() == KeyEvent.VK_W) dirs[1] = false;
 		if (e.getKeyCode() == KeyEvent.VK_D) dirs[2] = false;
 		if (e.getKeyCode() == KeyEvent.VK_S) dirs[3] = false;
 		if (e.getKeyCode() == KeyEvent.VK_SHIFT) sprint = false;
 	}
 	
 	@Override
 	public void setTarget(Vector v)
 	{
 		target = v;
 		if (frozen) target = null;
 	}
 	
 	public Entity getClickTarget()
 	{
 		return clickTarget;
 	}
 	
 	public void setClickTarget(Entity clickTarget)
 	{
 		this.clickTarget = clickTarget;
 		
 		checkForOnClickReachEvent();
 	}
 	
 	@Override
 	protected void onReachTarget()
 	{
 		super.onReachTarget();
 		
 		checkForOnClickReachEvent();
 		
 		target = null;
 	}
 	
 	protected void checkForOnClickReachEvent()
 	{
 		if (clickTarget != null && getDistance(clickTarget) <= getBumpRadius() + clickTarget.getBumpRadius() + World.TILE_SIZE / 4) onClickReach(clickTarget);
 	}
 	
 	@Override
 	protected void onClickReach(Entity entity)
 	{
 		super.onClickReach(entity);
 		if (entity.getTalk() != null && !(Game.currentGame.getActiveLayer() instanceof TalkLayer)) startTalk(entity);
 	}
 	
 	public void startTalk(Entity entity)
 	{
 		frozen = true;
 		Game.currentGame.addLayer(new TalkLayer(entity.getTalk(), entity));
 		entity.setFrozen(true);
 	}
 	
 	@Override
 	protected void onEnter(Entity entity)
 	{
 		super.onEnter(entity);
 		
 		if (entity instanceof ItemDrop)
 		{
 			if (inv.put(((ItemDrop) entity).getItemStack()))
 			{
 				entity.onPickup();
 				entity.kill();
 			}
 		}
 	}
 }
