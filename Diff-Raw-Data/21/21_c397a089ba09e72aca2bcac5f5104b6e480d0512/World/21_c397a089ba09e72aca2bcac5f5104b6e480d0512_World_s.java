 package de.dakror.liturfaliarcest.game.world;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Path2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.dakror.gamesetup.GameFrame;
 import de.dakror.gamesetup.layer.Layer;
 import de.dakror.gamesetup.ui.Component;
 import de.dakror.gamesetup.util.Compressor;
 import de.dakror.gamesetup.util.Helper;
 import de.dakror.gamesetup.util.Vector;
 import de.dakror.liturfaliarcest.game.Game;
 import de.dakror.liturfaliarcest.game.entity.Entity;
 import de.dakror.liturfaliarcest.game.entity.EntityType;
 import de.dakror.liturfaliarcest.game.entity.creature.NPC;
 import de.dakror.liturfaliarcest.game.entity.object.ItemDrop;
 import de.dakror.liturfaliarcest.game.entity.object.Object;
 import de.dakror.liturfaliarcest.game.item.Item;
 import de.dakror.liturfaliarcest.settings.FlagManager;
 
 public class World extends Layer
 {
 	public static final int TILE_SIZE = 64;
 	
 	public int x, y, width, height;
 	
 	String name;
 	Area bump;
 	
 	boolean init;
 	
 	boolean groundLayer, aboveLayer;
 	
 	public int drawn;
 	
 	public World(String name)
 	{
 		this.name = name;
 		x = y = 0;
 		
 		init = false;
 		
 		Game.worlds.put(name, this);
 	}
 	
 	@Override
 	public void init()
 	{
 		if (!init)
 		{
 			groundLayer = Game.getImage("/maps/" + name + "/" + name + "-0.png") != null;
 			aboveLayer = Game.getImage("/maps/" + name + "/" + name + "-1.png") != null;
 			
 			Image img = Game.getImage("/maps/" + name + "/" + name + "-0.png");
 			width = img.getWidth(null) / 32 * TILE_SIZE;
 			height = img.getHeight(null) / 32 * TILE_SIZE;
 			
 			try
 			{
 				if (getClass().getResource("/maps/" + name + "/" + name + ".bump") != null)
 				{
 					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Compressor.decompress(Compressor.getURLContentAsByteArray(getClass().getResource("/maps/" + name + "/" + name + ".bump")))));
 					Path2D p = (Path2D) ois.readObject();
 					bump = new Area(AffineTransform.getScaleInstance(TILE_SIZE / 32, TILE_SIZE / 32).createTransformedShape(new Area(p)));
 				}
 				else
 				{
 					int size = 4;
 					
 					BufferedImage bumpImage = Game.getImage("/maps/" + name + "/" + name + "-2.png");
 					bumpImage = Helper.toBufferedImage(bumpImage.getScaledInstance(bumpImage.getWidth() / 32 * TILE_SIZE, bumpImage.getHeight() / 32 * TILE_SIZE, BufferedImage.SCALE_FAST));
 					
 					bump = new Area();
 					for (int i = 0; i < bumpImage.getWidth(); i += size)
 						for (int j = 0; j < bumpImage.getHeight(); j += size)
 							if (new Color(bumpImage.getRGB(i, j)).equals(Color.white)) bump.add(new Area(new Rectangle(i, j, size, size)));
 				}
 				
 				init = true;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 		}
 		try
 		{
 			initEntities();
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void initEntities() throws JSONException
 	{
 		components.clear();
 		
 		if (getClass().getResource("/maps/" + name + "/" + name + ".ent") != null)
 		{
 			JSONArray e = new JSONArray(Helper.getURLContent(getClass().getResource("/maps/" + name + "/" + name + ".ent")));
 			for (int i = 0; i < e.length(); i++)
 			{
 				JSONObject o = e.getJSONObject(i);
 				Entity entity = null;
 				
 				if (o.has("m"))
 				{
 					if (o.getJSONObject("m").has("flags") && !FlagManager.matchesFlags(o.getJSONObject("m").getString("flags"))) continue;
 					
 					if (o.getJSONObject("m").has("npc") && o.getJSONObject("m").getBoolean("npc")) entity = new NPC(o.getInt("x") * (World.TILE_SIZE / 32), o.getInt("y") * (World.TILE_SIZE / 32), EntityType.entityTypes.get(o.getInt("i")), o.getJSONObject("m"));
 					else if (o.getJSONObject("m").has("itemID")) entity = new ItemDrop(o.getInt("x") * (World.TILE_SIZE / 32), o.getInt("y") * (World.TILE_SIZE / 32), Item.items.get(o.getJSONObject("m").getInt("itemID")));
 				}
 				else entity = new Object(o.getInt("x") * (World.TILE_SIZE / 32), o.getInt("y") * (World.TILE_SIZE / 32), EntityType.entityTypes.get(o.getInt("i")));
 				
 				if (o.has("e")) entity.setEventFunctions(o.getJSONObject("e"));
 				
 				entity.uid = o.getInt("uid");
 				
 				addEntity(entity);
 			}
 		}
 	}
 	
 	@Override
 	public void draw(Graphics2D g)
 	{
 		if (groundLayer)
 		{
 			Helper.setRenderingHints(g, false);
 			
 			g.drawImage(Game.getImage("/maps/" + name + "/" + name + "-0.png"), x, y, width, height, Game.w);
 			
 			Helper.setRenderingHints(g, true);
 		}
 		
 		AffineTransform old = g.getTransform();
 		AffineTransform at = g.getTransform();
 		at.translate(x, y);
 		g.setTransform(at);
 		
 		drawn = 0;
 		
 		Component hovered = null;
 		for (Component c : components)
 		{
 			if (!new Rectangle(0, 0, Game.getWidth(), Game.getHeight()).intersects(((Entity) c).getArea2())) continue;
 			
 			drawn++;
 			c.draw(g);
 			if (c.state != 0) hovered = c;
 		}
		if (hovered != null) hovered.drawTooltip(GameFrame.currentFrame.mouse.x - x, GameFrame.currentFrame.mouse.y - y, g);
 		
 		g.setTransform(old);
 		
 		if (aboveLayer)
 		{
 			Helper.setRenderingHints(g, false);
 			
 			g.drawImage(Game.getImage("/maps/" + name + "/" + name + "-1.png"), x, y, width, height, Game.w);
 			
 			Helper.setRenderingHints(g, true);
 		}
 	}
 	
 	@Override
 	public void update(int tick)
 	{
 		if (!enabled) return;
 		
 		for (Component c : components)
 		{
 			c.update(tick);
 			if (((Entity) c).isDead()) components.remove(c);
 		}
 		
 		ArrayList<Component> c = new ArrayList<>(components);
 		Collections.sort(c, new Comparator<Component>()
 		{
 			@Override
 			public int compare(Component o1, Component o2)
 			{
 				return Integer.compare(o1.getY() + ((Entity) o1).bumpY + ((Entity) o1).bumpHeight, o2.getY() + ((Entity) o2).bumpY + ((Entity) o2).bumpHeight);
 			}
 		});
 		components = new CopyOnWriteArrayList<>(c);
 	}
 	
 	public void addEntity(Entity e)
 	{
 		components.add(e);
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public JSONObject getData() throws JSONException
 	{
 		JSONObject o = new JSONObject();
 		o.put("n", name);
 		JSONArray e = new JSONArray();
 		for (Component c : components)
 		{
 			JSONObject d = ((Entity) c).getData();
 			if (d.length() > 0) e.put(d);
 		}
 		o.put("e", e);
 		return o;
 	}
 	
 	public Entity getEntityForUID(int uid)
 	{
 		for (Component c : components)
 			if (c instanceof Entity && ((Entity) c).uid == uid) return (Entity) c;
 		
 		return null;
 	}
 	
 	public Area getBump()
 	{
 		return bump;
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e)
 	{
 		Game.player.setClickTarget(null);
 		
 		super.mouseReleased(e);
 		
 		if (AffineTransform.getTranslateInstance(x, y).createTransformedShape(bump).contains(e.getPoint()) || Game.player.getClickTarget() != null) Game.player.setTarget(new Vector(e.getX() - x - Game.player.bumpX - Game.player.bumpWidth / 2, e.getY() - y - Game.player.bumpY - Game.player.bumpHeight / 2));
 	}
 }
