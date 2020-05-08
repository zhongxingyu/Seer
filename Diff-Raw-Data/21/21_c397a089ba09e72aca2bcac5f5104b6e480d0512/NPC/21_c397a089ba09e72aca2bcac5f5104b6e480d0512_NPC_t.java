 package de.dakror.liturfaliarcest.game.entity.creature;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.dakror.gamesetup.ui.Component;
 import de.dakror.gamesetup.util.Helper;
 import de.dakror.gamesetup.util.Vector;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.game.entity.Entity;
 import de.dakror.liturfaliarcest.game.entity.EntityType;
 import de.dakror.liturfaliarcest.game.world.World;
 import de.dakror.liturfaliarcest.settings.Attributes.Attribute;
 
 /**
  * @author Dakror
  */
 public class NPC extends Creature
 {
 	EntityType type;
 	JSONObject meta;
 	
 	boolean roam;
 	int sTick, roamTimeout;
 	
 	public NPC(int x, int y, EntityType type, JSONObject meta) throws JSONException
 	{
 		super(x, y, 0, 0);
 		this.meta = meta;
 		tex = meta.getString("texture");
 		BufferedImage img = Game.getImage(tex);
 		width = img.getWidth() / 4 * (World.TILE_SIZE / 32);
 		height = img.getHeight() / 4 * (World.TILE_SIZE / 32);
 		bumpY = Math.round(70 * height / 96f);
 		bumpX = Math.round(16 * width / 64f);
 		bumpWidth = width / 2;
 		bumpHeight = Math.round(24 * height / 96f);
 		attr.set(Attribute.SPEED, (float) (meta.has("speed") ? meta.getDouble("speed") : 2f));
 		roam = meta.has("roam") && meta.getBoolean("roam");
 		roamTimeout = (int) (Math.random() * 60) + 60;
		if (meta.has("dir")) dir = meta.getInt("dir");
 	}
 	
 	@Override
 	protected void tick(int tick)
 	{
 		super.tick(tick);
 		
 		if (sTick == 0) sTick = tick;
 		if (target == null && roam && (tick - sTick) % roamTimeout == 0 && sTick != tick) roam();
 	}
 	
 	private void roam()
 	{
 		Vector t = pos.clone().add(getRandomTarget());
 		Rectangle2D r = getBumpFromPosToVector(t);
 		if (Game.world.getBump().contains(r))
 		{
 			boolean free = true;
 			for (Component c : Game.world.components)
 			{
 				if (((Entity) c).getBump().intersects(r) && !c.equals(this))
 				{
 					free = false;
 					break;
 				}
 			}
 			
 			if (free) target = t;
 		}
 		
 		roamTimeout = (int) (Math.random() * 60) + 60;
 	}
 	
 	private Vector getRandomTarget()
 	{
 		int length = (int) (Math.random() * 5 + 1) * bumpWidth;
 		return new Vector[] { new Vector(-length, 0), new Vector(0, -length), new Vector(length, 0), new Vector(0, length) }[(int) Math.floor(Math.random() * 4)];
 	}
 	
 	@Override
 	public void drawTooltip(int x, int y, Graphics2D g)
 	{
 		if (meta.has("name"))
 		{
 			try
 			{
				Helper.drawShadow(x, y, g.getFontMetrics(g.getFont().deriveFont(35f)).stringWidth(meta.getString("name")) + 30, 64, g);
				Helper.drawString(meta.getString("name"), x + 10, y + 44, g, 35);
 			}
 			catch (JSONException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private Rectangle2D getBumpFromPosToVector(Vector target)
 	{
 		return new Rectangle2D.Float(Math.min(target.x, pos.x) + bumpX, Math.min(target.y, pos.y) + bumpY, (Math.max(target.x, pos.x) - Math.min(target.x, pos.x)) + bumpWidth, (Math.max(target.y, pos.y) - Math.min(target.y, pos.y)) + bumpHeight);
 	}
 	
 	public JSONObject getMeta()
 	{
 		return meta;
 	}
 	
 	public JSONArray getTalk()
 	{
 		try
 		{
 			return meta.has("talk") ? meta.getJSONArray("talk") : null;
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	@Override
 	protected void onReachTarget()
 	{
 		super.onReachTarget();
 		target = null;
 		frame = 0;
 	}
 	
 }
